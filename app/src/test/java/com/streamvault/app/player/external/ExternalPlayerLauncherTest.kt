package com.streamvault.app.player.external

import android.content.Intent
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ExternalPlayerLauncherTest {

    @Test
    fun `blank URL returns null`() {
        assertThat(ExternalPlayerLauncher.buildExternalPlayerIntent("")).isNull()
        assertThat(ExternalPlayerLauncher.buildExternalPlayerIntent("   ")).isNull()
        assertThat(ExternalPlayerLauncher.buildExternalPlayerIntent("\t")).isNull()
    }

    @Test
    fun `ftp URL returns null`() {
        assertThat(
            ExternalPlayerLauncher.buildExternalPlayerIntent("ftp://example.com/stream.m3u8")
        ).isNull()
    }

    @Test
    fun `https m3u8 URL returns ACTION_VIEW intent with HLS MIME type`() {
        val intent = checkNotNull(ExternalPlayerLauncher.buildExternalPlayerIntent(
            "https://example.com/stream.m3u8"
        ))

        assertThat(intent.action).isEqualTo(Intent.ACTION_VIEW)
        assertThat(intent.type).isEqualTo("application/x-mpegURL")
        assertThat(intent.categories).contains(Intent.CATEGORY_BROWSABLE)
    }

    @Test
    fun `https ts URL returns ACTION_VIEW intent with transport stream MIME type`() {
        val intent = checkNotNull(ExternalPlayerLauncher.buildExternalPlayerIntent(
            "https://example.com/stream.ts"
        ))

        assertThat(intent.action).isEqualTo(Intent.ACTION_VIEW)
        assertThat(intent.type).isEqualTo("video/mp2t")
        assertThat(intent.categories).contains(Intent.CATEGORY_BROWSABLE)
    }
}
