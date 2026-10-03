package com.streamvault.data.provider

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.streamvault.data.security.CredentialCrypto
import com.streamvault.domain.model.*
import org.junit.Test

class ProviderConfigurationCodecTest {
    private val crypto = object : CredentialCrypto {
        override fun encryptIfNeeded(value: String) = if (value.isBlank() || value.startsWith("enc:test:")) value else "enc:test:$value"
        override fun decryptIfNeeded(value: String) = value.removePrefix("enc:test:")
    }
    private val codec = ProviderConfigurationCodec(Gson(), crypto)

    @Test
    fun `round trips every subtype without plaintext credentials`() {
        val configurations = listOf<ProviderConfiguration>(
            XtreamConfig("https://x.test", "alice", "secret"),
            M3uConfig("https://m.test/list.m3u", epgUrl = "https://m.test/epg.xml"),
            StalkerConfig(
                portalUrl = "https://s.test",
                device = StalkerDeviceIdentity("00:11:22:33:44:55", serialNumber = "serial"),
                username = "bob",
                password = "secret2"
            ),
            JellyfinConfig("https://j.test", "carol", "token")
        )

        configurations.forEach { configuration ->
            val encoded = codec.encode(configuration)
            if (configuration !is M3uConfig) assertThat(encoded).doesNotContain(when (configuration) {
                is XtreamConfig -> "\"password\":\"secret\""
                is StalkerConfig -> "\"password\":\"secret2\""
                is JellyfinConfig -> "\"credential\":\"token\""
                else -> error("unreachable")
            })
            assertThat(codec.decode(configuration.type, encoded)).isEqualTo(configuration)
        }
    }

    @Test
    fun `identity key is stable across secrets and cosmetic origin changes`() {
        val first = XtreamConfig("HTTPS://Example.COM", "alice", "one")
        val second = XtreamConfig("https://example.com:443/", "alice", "two")
        assertThat(codec.identityKey(first)).isEqualTo(codec.identityKey(second))
        assertThat(codec.identityKey(first)).hasLength(64)
    }

    @Test
    fun `token bearing m3u urls and headers are encrypted at rest, plain urls stay readable`() {
        val config = M3uConfig(
            playlistUrl = "http://iptv.test/get.php?username=alice&password=s3cret&type=m3u_plus",
            epgUrl = "https://iptv.test/epg.xml",
            httpHeaders = "Authorization: Bearer abc"
        )
        val encoded = codec.encode(config)
        val stored = Gson().fromJson(encoded, M3uConfig::class.java)
        // Fake crypto marks ciphertext with "enc:test:"; real crypto replaces the value entirely.
        assertThat(stored.playlistUrl).startsWith("enc:test:")
        assertThat(stored.httpHeaders).startsWith("enc:test:")
        assertThat(stored.epgUrl).isEqualTo("https://iptv.test/epg.xml")
        assertThat(codec.decode(ProviderType.M3U, encoded)).isEqualTo(config)
    }

    @Test
    fun `legacy plaintext m3u rows still decode`() {
        val legacy = Gson().toJson(M3uConfig("http://iptv.test/list.m3u?token=t0k"))
        assertThat((codec.decode(ProviderType.M3U, legacy) as M3uConfig).playlistUrl)
            .isEqualTo("http://iptv.test/list.m3u?token=t0k")
    }

    @Test
    fun `credential url detection`() {
        assertThat(isCredentialBearingUrl("http://u:p@host/list.m3u")).isTrue()
        assertThat(isCredentialBearingUrl("http://host/list.m3u?Token=x")).isTrue()
        assertThat(isCredentialBearingUrl("https://host/list.m3u?type=m3u")).isFalse()
        assertThat(isCredentialBearingUrl("")).isFalse()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `decode rejects stored type mismatch`() {
        codec.decode(ProviderType.M3U, codec.encode(XtreamConfig("https://x.test", "u", "p")))
    }
}
