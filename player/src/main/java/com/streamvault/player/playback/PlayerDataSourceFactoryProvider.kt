package com.streamvault.player.playback

import android.content.Context
import android.util.Log
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import com.streamvault.domain.model.StreamInfo
import java.net.URI
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import okhttp3.Interceptor
import okhttp3.OkHttpClient

class PlayerDataSourceFactoryProvider(
    private val context: Context,
    private val baseClient: OkHttpClient
) {
    private companion object {
        private const val TAG = "PlayerDataSource"
    }

    private val clientsByProfile = ConcurrentHashMap<PlayerTimeoutProfile, OkHttpClient>()

    fun createFactory(
        streamInfo: StreamInfo,
        resolvedStreamType: ResolvedStreamType,
        preload: Boolean = false
    ): Pair<PlayerTimeoutProfile, DataSource.Factory> {
        val profile = PlayerTimeoutProfile.resolve(streamInfo, resolvedStreamType, preload)
        val headers = streamInfo.headers
        logRequestShape(streamInfo, headers, preload)
        val client = clientsByProfile.computeIfAbsent(profile) {
            baseClient.newBuilder()
                .addInterceptor(StalkerPlaybackRequestLoggingInterceptor)
                .connectTimeout(profile.connectTimeoutMs, TimeUnit.MILLISECONDS)
                .readTimeout(profile.readTimeoutMs, TimeUnit.MILLISECONDS)
                .writeTimeout(profile.writeTimeoutMs, TimeUnit.MILLISECONDS)
                .build()
        }
        val upstreamFactory = OkHttpDataSource.Factory(client).apply {
            streamInfo.userAgent?.takeIf { it.isNotBlank() }?.let(::setUserAgent)
            if (headers.isNotEmpty()) {
                setDefaultRequestProperties(headers)
            }
        }
        val factory = DefaultDataSource.Factory(context, upstreamFactory)
        return profile to factory
    }

    private fun logRequestShape(
        streamInfo: StreamInfo,
        headers: Map<String, String>,
        preload: Boolean
    ) {
        val hasStalkerHeaders = headers.containsKey("X-User-Agent") ||
            headers.containsKey("Authorization") ||
            headers["Cookie"]?.contains("mac=", ignoreCase = true) == true
        if (!hasStalkerHeaders) {
            return
        }
        val uri = runCatching { URI(streamInfo.url) }.getOrNull()
        Log.d(
            TAG,
            "Playback request headers preload=$preload host=${uri?.host.orEmpty()} path=${uri?.path.orEmpty()} " +
                "ua=${!streamInfo.userAgent.isNullOrBlank()} referer=${headers.containsKey("Referer")} " +
                "cookie=${headers.containsKey("Cookie")} auth=${headers.containsKey("Authorization")} " +
                "xua=${headers.containsKey("X-User-Agent")}"
        )
    }
}

private object StalkerPlaybackRequestLoggingInterceptor : Interceptor {
    private const val TAG = "PlayerDataSource"

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        if (!request.hasStalkerPlaybackShape()) {
            return chain.proceed(request)
        }
        Log.d(
            TAG,
            "Playback request actual method=${request.method} target=${PlaybackLogSanitizer.sanitizeUrl(request.url.toString())} " +
                "ua=${request.header("User-Agent") != null} referer=${request.header("Referer") != null} " +
                "cookie=${request.header("Cookie") != null} auth=${request.header("Authorization") != null} " +
                "xua=${request.header("X-User-Agent") != null} range=${request.header("Range") != null} " +
                "acceptEncoding=${request.header("Accept-Encoding")?.take(24).orEmpty()} cookieKeys=${request.cookieKeySummary()}"
        )
        val response = chain.proceed(request)
        Log.d(
            TAG,
            "Playback response actual target=${PlaybackLogSanitizer.sanitizeUrl(request.url.toString())} " +
                "code=${response.code} length=${response.header("Content-Length").orEmpty()} " +
                "type=${response.header("Content-Type").orEmpty()}"
        )
        return response
    }

    private fun okhttp3.Request.hasStalkerPlaybackShape(): Boolean {
        val path = url.encodedPath.lowercase()
        return header("X-User-Agent") != null ||
            header("Authorization") != null ||
            header("Cookie")?.contains("mac=", ignoreCase = true) == true ||
            path.endsWith("/play/live.php") ||
            path.endsWith("/play/movie.php")
    }

    private fun okhttp3.Request.cookieKeySummary(): String {
        val cookie = header("Cookie") ?: return ""
        return cookie.split(';')
            .mapNotNull { part -> part.substringBefore('=', missingDelimiterValue = "").trim().takeIf(String::isNotBlank) }
            .take(12)
            .joinToString("|")
    }
}

