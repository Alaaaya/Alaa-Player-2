package com.streamvault.player.playback

import com.streamvault.domain.model.PlaybackTransportMode
import com.streamvault.domain.model.PlaybackTransportPolicy
import java.io.IOException
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.Base64
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager
import okhttp3.OkHttpClient

fun OkHttpClient.Builder.applyUnsafeTlsBypass(): OkHttpClient.Builder {
    val trustAllManager = object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
    }
    val sslContext = SSLContext.getInstance("TLS").apply {
        init(null, arrayOf(trustAllManager), SecureRandom())
    }
    return sslSocketFactory(sslContext.socketFactory, trustAllManager)
        .hostnameVerifier(HostnameVerifier { _, _ -> true })
}

/**
 * Applies a user-approved Stalker transport decision to one exact playback origin.
 *
 * Unlike [applyUnsafeTlsBypass], this cannot trust another provider, host, port, or public key.
 */
fun OkHttpClient.Builder.applyPlaybackTransportPolicy(
    policy: PlaybackTransportPolicy
): OkHttpClient.Builder {
    addNetworkInterceptor { chain ->
        val requestOrigin = chain.request().url
        val expected = policy.origin
        if (!requestOrigin.scheme.equals(expected.scheme, ignoreCase = true) ||
            !requestOrigin.host.equals(expected.host, ignoreCase = true) ||
            requestOrigin.port != expected.port
        ) {
            throw IOException("Playback transport approval does not cover the redirected origin.")
        }
        chain.proceed(chain.request())
    }
    if (policy.mode != PlaybackTransportMode.USER_ACCEPTED_UNVERIFIED_HTTPS) {
        return this
    }

    val expectedPin = requireNotNull(policy.spkiSha256).trim()
    val trustManager = object : X509TrustManager {
        override fun checkClientTrusted(
            chain: Array<out X509Certificate>?,
            authType: String?
        ) = Unit

        override fun checkServerTrusted(
            chain: Array<out X509Certificate>?,
            authType: String?
        ) {
            val leaf = chain?.firstOrNull()
                ?: throw CertificateException("Playback server did not present a certificate.")
            if (leaf.playbackSpkiSha256() != expectedPin) {
                throw CertificateException("Playback server public key changed.")
            }
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
    }
    val sslContext = SSLContext.getInstance("TLS").apply {
        init(null, arrayOf(trustManager), SecureRandom())
    }
    return sslSocketFactory(sslContext.socketFactory, trustManager)
        .hostnameVerifier { hostname, session ->
            hostname.equals(policy.origin.host, ignoreCase = true) &&
                runCatching {
                    (session.peerCertificates.firstOrNull() as? X509Certificate)
                        ?.playbackSpkiSha256() == expectedPin
                }.getOrDefault(false)
        }
}

private fun X509Certificate.playbackSpkiSha256(): String =
    "sha256/" + Base64.getEncoder().encodeToString(
        MessageDigest.getInstance("SHA-256").digest(publicKey.encoded)
    )
