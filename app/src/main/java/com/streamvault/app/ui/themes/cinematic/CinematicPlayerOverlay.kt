package com.streamvault.app.ui.themes.cinematic

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.app.ui.screens.player.PlayerTimeshiftUiState
import com.streamvault.app.ui.screens.player.SeekPreviewState
import com.streamvault.app.ui.screens.player.SleepTimerUiState
import com.streamvault.domain.model.Channel
import com.streamvault.domain.model.Program
import com.streamvault.domain.model.RecordingStatus
import kotlin.math.roundToLong

private data class CinematicPlayerAction(
    val label: String,
    val tone: Color = CinematicGold,
    val onClick: () -> Unit
)

/**
 * A separate fullscreen control surface for the Cinematic application theme.
 *
 * This is deliberately presentation-only: playback position, transport, tracks,
 * recording, casting and timer operations remain owned by PlayerScreen/ViewModel.
 */
@Composable
internal fun CinematicPlayerOverlay(
    visible: Boolean,
    title: String,
    contentType: String,
    isCatchUpPlayback: Boolean,
    isPlaying: Boolean,
    currentProgram: Program?,
    currentChannel: Channel?,
    currentChannelName: String?,
    displayChannelNumber: Int,
    currentPosition: Long,
    duration: Long,
    aspectRatioLabel: String,
    subtitleTrackCount: Int,
    liveTranslationAvailable: Boolean,
    audioTrackCount: Int,
    videoQualityCount: Int,
    currentRecordingStatus: RecordingStatus?,
    isMuted: Boolean,
    playbackSpeed: Float,
    mediaTitle: String?,
    sleepTimerUiState: SleepTimerUiState,
    timeshiftUiState: PlayerTimeshiftUiState,
    playButtonFocusRequester: FocusRequester,
    quickActionsFocusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekBackward: () -> Unit,
    onSeekForward: () -> Unit,
    onRestartProgram: () -> Unit,
    onOpenArchive: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onScheduleRecording: () -> Unit,
    onScheduleDailyRecording: () -> Unit,
    onScheduleWeeklyRecording: () -> Unit,
    onToggleAspectRatio: () -> Unit,
    onOpenSubtitleTracks: () -> Unit,
    onOpenAudioTracks: () -> Unit,
    onOpenVideoTracks: () -> Unit,
    onOpenPlaybackSpeed: () -> Unit,
    onOpenStopPlaybackTimer: () -> Unit,
    onOpenIdleStandbyTimer: () -> Unit,
    onOpenAudioVideoSync: () -> Unit,
    audioVideoSyncEnabled: Boolean,
    showEpisodesAction: Boolean,
    onOpenEpisodes: () -> Unit,
    onOpenSplitScreen: () -> Unit,
    onEnterPictureInPicture: () -> Unit,
    onToggleMute: () -> Unit,
    isCastConnected: Boolean,
    onCast: () -> Unit,
    onStopCasting: () -> Unit,
    onSeekToLiveEdge: () -> Unit,
    onSeekToPosition: (Long) -> Unit,
    onSetScrubbingMode: (Boolean) -> Unit,
    seekPreview: SeekPreviewState,
    onSeekPreviewPositionChanged: (Long?) -> Unit,
    onUserInteraction: () -> Unit
) {
    val isVod = contentType != "LIVE" || isCatchUpPlayback
    val displayTitle = currentProgram?.title?.takeIf { contentType == "LIVE" }
        ?: mediaTitle?.takeIf { it.isNotBlank() }
        ?: title
    val primaryActions = buildList {
        add(CinematicPlayerAction(if (isMuted) "UNMUTE" else "MUTE", CinematicWine, onToggleMute))
        add(CinematicPlayerAction(if (isCastConnected) "STOP CAST" else "CAST", onClick = if (isCastConnected) onStopCasting else onCast))
        add(CinematicPlayerAction("PICTURE IN PICTURE", onClick = onEnterPictureInPicture))
        add(CinematicPlayerAction("DISPLAY · $aspectRatioLabel", onClick = onToggleAspectRatio))
        if (subtitleTrackCount > 0 || liveTranslationAvailable) {
            add(CinematicPlayerAction("SUBTITLES", onClick = onOpenSubtitleTracks))
        }
        if (audioTrackCount > 0) add(CinematicPlayerAction("AUDIO", onClick = onOpenAudioTracks))
        if (videoQualityCount > 0) add(CinematicPlayerAction("QUALITY", onClick = onOpenVideoTracks))
        if (isVod) add(CinematicPlayerAction("SPEED · ${playbackSpeed}×", onClick = onOpenPlaybackSpeed))
        if (showEpisodesAction) add(CinematicPlayerAction("EPISODES", onClick = onOpenEpisodes))
        if (timeshiftUiState.available) add(CinematicPlayerAction("RETURN TO LIVE", CinematicWine, onSeekToLiveEdge))
        if (contentType == "LIVE" && currentProgram != null) {
            add(CinematicPlayerAction("RESTART PROGRAM", onClick = onRestartProgram))
            add(CinematicPlayerAction("PROGRAM ARCHIVE", onClick = onOpenArchive))
        }
        if (currentRecordingStatus == RecordingStatus.RECORDING) {
            add(CinematicPlayerAction("STOP RECORDING", CinematicWine, onStopRecording))
        } else if (contentType == "LIVE") {
            add(CinematicPlayerAction("RECORD", CinematicWine, onStartRecording))
            add(CinematicPlayerAction("SCHEDULE RECORDING", onClick = onScheduleRecording))
            add(CinematicPlayerAction("DAILY RECORDING", onClick = onScheduleDailyRecording))
            add(CinematicPlayerAction("WEEKLY RECORDING", onClick = onScheduleWeeklyRecording))
        }
        add(CinematicPlayerAction("STOP TIMER", onClick = onOpenStopPlaybackTimer))
        add(CinematicPlayerAction("IDLE TIMER", onClick = onOpenIdleStandbyTimer))
        if (audioVideoSyncEnabled && !isCastConnected) add(CinematicPlayerAction("A/V SYNC", onClick = onOpenAudioVideoSync))
        add(CinematicPlayerAction("MULTIVIEW", onClick = onOpenSplitScreen))
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 5 }),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.58f),
                        0.42f to Color.Transparent,
                        1f to CinematicCanvas.copy(alpha = 0.94f)
                    )
                )
                .onPreviewKeyEvent { event ->
                    if (event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN) onUserInteraction()
                    false
                }
        ) {
            CinematicPlayerHeader(
                contentType = contentType,
                title = displayTitle,
                onClose = onClose,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 42.dp, vertical = 30.dp)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 42.dp, vertical = 30.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CinematicPlaybackIdentity(
                    isVod = isVod,
                    currentChannel = currentChannel,
                    currentChannelName = currentChannelName,
                    displayChannelNumber = displayChannelNumber,
                    currentProgram = currentProgram,
                    recording = currentRecordingStatus == RecordingStatus.RECORDING,
                    timeshiftAvailable = timeshiftUiState.available
                )
                if (isVod && duration > 0L) {
                    CinematicSeekRail(
                        currentPosition = currentPosition,
                        duration = duration,
                        seekPreview = seekPreview,
                        onSeekToPosition = onSeekToPosition,
                        onSetScrubbingMode = onSetScrubbingMode,
                        onSeekPreviewPositionChanged = onSeekPreviewPositionChanged
                    )
                } else if (currentProgram != null) {
                    LinearProgressIndicator(
                        progress = { currentProgram.progressPercent() },
                        modifier = Modifier.fillMaxWidth().height(5.dp),
                        color = CinematicWine,
                        trackColor = CinematicText.copy(alpha = 0.16f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CinematicTransportButton(
                        label = "−10",
                        onClick = onSeekBackward,
                        modifier = Modifier.width(94.dp)
                    )
                    CinematicTransportButton(
                        label = if (isPlaying) "PAUSE" else "PLAY",
                        accent = true,
                        onClick = onTogglePlayPause,
                        modifier = Modifier
                            .focusRequester(playButtonFocusRequester)
                            .width(150.dp)
                    )
                    CinematicTransportButton(
                        label = "+10",
                        onClick = onSeekForward,
                        modifier = Modifier.width(94.dp)
                    )
                    Text(
                        text = if (isVod && duration > 0L) {
                            "${formatCinematicTime(currentPosition)} / ${formatCinematicTime(duration)}"
                        } else if (timeshiftUiState.available) {
                            timeshiftUiState.statusMessage.ifBlank { "LIVE BUFFER AVAILABLE" }
                        } else {
                            "LIVE · NOW PLAYING"
                        },
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelLarge,
                        color = CinematicMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (sleepTimerUiState.stopTimerActive || sleepTimerUiState.idleTimerActive) {
                        Text(
                            text = "TIMER ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = CinematicGold
                        )
                    }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(primaryActions, key = { it.label }) { action ->
                        CinematicActionChip(
                            action = action,
                            modifier = if (action == primaryActions.firstOrNull()) {
                                Modifier.focusRequester(quickActionsFocusRequester)
                            } else {
                                Modifier
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CinematicPlayerHeader(
    contentType: String,
    title: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "CINEMA // ${if (contentType == "LIVE") "LIVE TRANSMISSION" else "FEATURE PRESENTATION"}",
                style = MaterialTheme.typography.labelMedium,
                color = CinematicGold,
                fontWeight = FontWeight.Black
            )
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = CinematicText,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        CinematicActionChip(
            action = CinematicPlayerAction("CLOSE", CinematicWine, onClose)
        )
    }
}

@Composable
private fun CinematicPlaybackIdentity(
    isVod: Boolean,
    currentChannel: Channel?,
    currentChannelName: String?,
    displayChannelNumber: Int,
    currentProgram: Program?,
    recording: Boolean,
    timeshiftAvailable: Boolean
) {
    if (isVod) return
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = displayChannelNumber.takeIf { it > 0 }?.toString()?.padStart(2, '0') ?: "LIVE",
            style = MaterialTheme.typography.headlineSmall,
            color = CinematicGold,
            fontWeight = FontWeight.Black
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = currentChannelName ?: currentChannel?.name ?: "LIVE CHANNEL",
                style = MaterialTheme.typography.titleMedium,
                color = CinematicText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = currentProgram?.title ?: "Live programme information is unavailable",
                style = MaterialTheme.typography.bodyMedium,
                color = CinematicMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (recording) CinematicStatusMarker("REC", CinematicWine)
        if (timeshiftAvailable) CinematicStatusMarker("REWIND", CinematicGold)
    }
}

@Composable
private fun CinematicSeekRail(
    currentPosition: Long,
    duration: Long,
    seekPreview: SeekPreviewState,
    onSeekToPosition: (Long) -> Unit,
    onSetScrubbingMode: (Boolean) -> Unit,
    onSeekPreviewPositionChanged: (Long?) -> Unit
) {
    val safeDuration = duration.coerceAtLeast(1L)
    var sliderPosition by remember(currentPosition, safeDuration) {
        mutableFloatStateOf((currentPosition.toFloat() / safeDuration).coerceIn(0f, 1f))
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (seekPreview.visible) {
            Text(
                text = seekPreview.title.ifBlank { formatCinematicTime(seekPreview.positionMs) },
                style = MaterialTheme.typography.labelMedium,
                color = CinematicGold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Slider(
            value = sliderPosition,
            onValueChange = { value ->
                sliderPosition = value
                onSeekPreviewPositionChanged((value * safeDuration).roundToLong())
            },
            onValueChangeFinished = {
                onSeekToPosition((sliderPosition * safeDuration).roundToLong())
                onSeekPreviewPositionChanged(null)
                onSetScrubbingMode(false)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = CinematicGold,
                activeTrackColor = CinematicWine,
                inactiveTrackColor = CinematicText.copy(alpha = 0.18f)
            )
        )
    }
}

@Composable
private fun CinematicTransportButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Boolean = false
) {
    val shape = RoundedCornerShape(14.dp)
    TvClickableSurface(
        onClick = onClick,
        modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (accent) CinematicWine else CinematicPanel,
            focusedContainerColor = if (accent) CinematicGold else CinematicPanelRaised,
            contentColor = CinematicText,
            focusedContentColor = if (accent) CinematicCanvas else CinematicText
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(2.dp, CinematicGold),
                shape = shape
            )
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.04f)
    ) {
        Box(modifier = Modifier.padding(vertical = 13.dp), contentAlignment = Alignment.Center) {
            Text(text = label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CinematicActionChip(
    action: CinematicPlayerAction,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(999.dp)
    TvClickableSurface(
        onClick = action.onClick,
        modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = CinematicPanel.copy(alpha = 0.92f),
            focusedContainerColor = action.tone.copy(alpha = 0.28f),
            contentColor = CinematicMuted,
            focusedContentColor = CinematicText
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(2.dp, action.tone),
                shape = shape
            )
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.025f)
    ) {
        Text(
            text = action.label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun CinematicStatusMarker(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        colors = SurfaceDefaults.colors(containerColor = color.copy(alpha = 0.2f)),
        border = Border(
            border = BorderStroke(1.dp, color),
            shape = RoundedCornerShape(999.dp)
        )
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Black
        )
    }
}

private fun formatCinematicTime(timeMs: Long): String {
    val safeSeconds = (timeMs / 1_000L).coerceAtLeast(0L)
    val hours = safeSeconds / 3_600L
    val minutes = (safeSeconds % 3_600L) / 60L
    val seconds = safeSeconds % 60L
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds) else "%02d:%02d".format(minutes, seconds)
}
