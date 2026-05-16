package com.streamvault.app.ui.screens.player

import com.google.common.truth.Truth.assertThat
import com.streamvault.domain.model.ProviderType
import org.junit.Test

class PlayerPlaybackProbeSupportTest {

    @Test
    fun `204 maps to empty temporary link failure`() {
        val failure = resolvePlaybackProbeFailure(204)

        assertThat(failure).isNotNull()
        assertThat(failure?.recoveryType).isEqualTo(PlayerRecoveryType.SOURCE)
        assertThat(failure?.message).contains("empty temporary link")
    }

    @Test
    fun `stalker temp-link playback probe is skipped`() {
        val skipped = shouldSkipPlaybackProbe(
            providerType = ProviderType.STALKER_PORTAL,
            url = "http://fdox.org:8080/play/live.php?mac=00:1A:79:BA:73:FA&stream=228556&extension=ts&play_token=abc123"
        )

        assertThat(skipped).isTrue()
    }

    @Test
    fun `non stalker playback probe is not skipped`() {
        val skipped = shouldSkipPlaybackProbe(
            providerType = ProviderType.XTREAM_CODES,
            url = "http://fdox.org:8080/play/live.php?stream=228556"
        )

        assertThat(skipped).isFalse()
    }
}
