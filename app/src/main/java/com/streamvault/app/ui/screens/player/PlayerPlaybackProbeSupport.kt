package com.streamvault.app.ui.screens.player

internal data class PlaybackProbeFailure(
    val message: String,
    val recoveryType: PlayerRecoveryType
)

internal fun resolvePlaybackProbeFailure(responseCode: Int): PlaybackProbeFailure? = when (responseCode) {
    204 -> PlaybackProbeFailure(
        message = "Portal issued an empty temporary link for this stream (HTTP 204). Retrying the channel may refresh the portal session.",
        recoveryType = PlayerRecoveryType.SOURCE
    )

    401, 403 -> PlaybackProbeFailure(
        message = "This provider stream was rejected ($responseCode Unauthorized/Forbidden).",
        recoveryType = PlayerRecoveryType.SOURCE
    )

    456 -> PlaybackProbeFailure(
        message = "This provider rejected playback for this channel (HTTP 456). The MAC or subscription may not have access to this stream.",
        recoveryType = PlayerRecoveryType.SOURCE
    )

    404 -> PlaybackProbeFailure(
        message = "This provider stream is unavailable right now (404).",
        recoveryType = PlayerRecoveryType.SOURCE
    )

    in 500..599 -> PlaybackProbeFailure(
        message = "The provider returned a server error for this stream ($responseCode).",
        recoveryType = PlayerRecoveryType.NETWORK
    )

    else -> null
}
