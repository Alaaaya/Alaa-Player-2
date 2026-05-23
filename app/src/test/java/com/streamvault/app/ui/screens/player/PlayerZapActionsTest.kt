package com.streamvault.app.ui.screens.player

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PlayerZapActionsTest {

    @Test
    fun `appendNumericChannelDigit keeps up to six digits`() {
        var buffer = ""

        (1..6).forEach { digit ->
            buffer = appendNumericChannelDigit(buffer, digit)
        }

        assertThat(buffer).isEqualTo("123456")
    }

    @Test
    fun `appendNumericChannelDigit restarts after sixth digit`() {
        var buffer = ""

        (1..6).forEach { digit ->
            buffer = appendNumericChannelDigit(buffer, digit)
        }
        buffer = appendNumericChannelDigit(buffer, 7)

        assertThat(buffer).isEqualTo("7")
    }

    @Test
    fun `withScopedScrubbingMode disables scrubbing after success`() = runTest {
        val states = mutableListOf<Boolean>()

        withScopedScrubbingMode(states::add) {
            "done"
        }

        assertThat(states).containsExactly(true, false).inOrder()
    }

    @Test
    fun `withScopedScrubbingMode disables scrubbing after failure`() = runTest {
        val states = mutableListOf<Boolean>()

        runCatching {
            withScopedScrubbingMode(states::add) {
                error("boom")
            }
        }

        assertThat(states).containsExactly(true, false).inOrder()
    }

    @Test
    fun `releaseOutgoingLiveZapPlayback closes active fetchers before clearing preload`() {
        val calls = mutableListOf<String>()

        releaseOutgoingLiveZapPlayback(
            stopPlayback = { calls += "stopPlayback" },
            stopLiveTimeshift = { calls += "stopLiveTimeshift" },
            clearPreload = { calls += "clearPreload" }
        )

        assertThat(calls).containsExactly("stopPlayback", "stopLiveTimeshift", "clearPreload").inOrder()
    }

    @Test
    fun `shouldPreloadAdjacentChannel skips Stalker internal streams`() {
        val shouldPreload = shouldPreloadAdjacentChannel(
            streamUrl = "stalker://1/live/99?cmd=ffmpeg%20http%3A%2F%2Fportal.example.com%2Fch%2F99",
            isStalkerInternalUrl = { it.startsWith("stalker://") }
        )

        assertThat(shouldPreload).isFalse()
    }

    @Test
    fun `shouldPreloadAdjacentChannel allows regular streams`() {
        val shouldPreload = shouldPreloadAdjacentChannel(
            streamUrl = "http://cdn.example.com/live/stream.ts",
            isStalkerInternalUrl = { it.startsWith("stalker://") }
        )

        assertThat(shouldPreload).isTrue()
    }
}
