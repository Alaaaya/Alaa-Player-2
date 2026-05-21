package com.streamvault.app.ui.screens.player

import com.google.common.truth.Truth.assertThat
import com.streamvault.player.PlayerError
import org.junit.Test

class PlayerPlaybackRecoverySupportTest {

    @Test
    fun `509 source error shows provider limit message`() {
        val error = PlayerError.SourceError(
            "Provider rejected playback, likely max connections or bandwidth limit (HTTP 509)."
        )

        assertThat(classifyPlaybackError(error)).isEqualTo(PlayerRecoveryType.SOURCE)
        assertThat(resolvePlaybackErrorMessage(error))
            .isEqualTo("Provider rejected playback, likely max connections or bandwidth limit.")
    }
}
