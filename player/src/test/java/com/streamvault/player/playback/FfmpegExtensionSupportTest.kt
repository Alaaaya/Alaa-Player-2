package com.streamvault.player.playback

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FfmpegExtensionSupportTest {

    @Test
    fun `availability is unavailable when ffmpeg class is missing`() {
        val library = ReflectiveFfmpegLibrary(libraryClassProvider = { null })

        assertThat(library.availability()).isEqualTo(FfmpegExtensionAvailability(false, null))
    }

    @Test
    fun `availability reports version when ffmpeg library is available`() {
        FakeFfmpegLibrary.available = true
        FakeFfmpegLibrary.reportedVersion = "6.0-test"

        val library = ReflectiveFfmpegLibrary(libraryClassProvider = { FakeFfmpegLibrary::class.java })

        assertThat(library.availability()).isEqualTo(FfmpegExtensionAvailability(true, "6.0-test"))
    }

    @Test
    fun `supports format delegates reflectively`() {
        FakeFfmpegLibrary.available = true
        FakeFfmpegLibrary.supportedMimeTypes = mutableSetOf("audio/ac3", "audio/eac3")
        val support = FfmpegExtensionSupport(
            ReflectiveFfmpegLibrary(libraryClassProvider = { FakeFfmpegLibrary::class.java })
        )

        assertThat(support.supportsFormat("audio/ac3")).isTrue()
        assertThat(support.supportsFormat("audio/mp4a-latm")).isFalse()
        assertThat(support.supportedAudioMimeTypes(listOf("audio/eac3", "audio/eac3", " ")))
            .containsExactly("audio/eac3")
    }

    object FakeFfmpegLibrary {
        @JvmStatic var available: Boolean = false
        @JvmStatic var reportedVersion: String? = null
        @JvmStatic var supportedMimeTypes: MutableSet<String> = mutableSetOf()

        @JvmStatic
        fun isAvailable(): Boolean = available

        @JvmStatic
        fun getVersion(): String? = reportedVersion

        @JvmStatic
        fun supportsFormat(mimeType: String): Boolean = available && mimeType in supportedMimeTypes
    }
}
