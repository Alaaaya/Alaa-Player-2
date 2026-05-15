package com.streamvault.app.ui.screens.player

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PlayerPlaybackProbeSupportTest {

    @Test
    fun `204 maps to empty temporary link failure`() {
        val failure = resolvePlaybackProbeFailure(204)

        assertThat(failure).isNotNull()
        assertThat(failure?.recoveryType).isEqualTo(PlayerRecoveryType.SOURCE)
        assertThat(failure?.message).contains("empty temporary link")
    }
}
