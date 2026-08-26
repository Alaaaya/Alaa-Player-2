package com.streamvault.app.ui.themes.neon

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.tv.material3.Text
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.app.ui.screens.player.PlayerTimeshiftUiState
import com.streamvault.app.ui.screens.player.SeekPreviewState
import com.streamvault.app.ui.screens.player.SleepTimerUiState
import com.streamvault.domain.model.Channel
import com.streamvault.domain.model.Program
import com.streamvault.domain.model.RecordingStatus

private data class NeonPlayerAction(val label: String, val tone: Color = NeonCyan, val onClick: () -> Unit)

/** Neon Future's player presentation: floating HUD nodes instead of Cinematic's full-width lower control desk. */
@Composable
internal fun NeonFuturePlayerOverlay(
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
    val quickActions = buildList {
        add(NeonPlayerAction(if (isMuted) "UNMUTE" else "MUTE", NeonPink, onToggleMute))
        add(NeonPlayerAction(if (isCastConnected) "END CAST" else "CAST", NeonCyan, if (isCastConnected) onStopCasting else onCast))
        add(NeonPlayerAction("PIP", NeonLime, onEnterPictureInPicture))
        add(NeonPlayerAction("DISPLAY $aspectRatioLabel", NeonCyan, onToggleAspectRatio))
        if (subtitleTrackCount > 0 || liveTranslationAvailable) add(NeonPlayerAction("SUBTITLES", NeonPink, onOpenSubtitleTracks))
        if (audioTrackCount > 0) add(NeonPlayerAction("AUDIO", NeonCyan, onOpenAudioTracks))
        if (videoQualityCount > 0) add(NeonPlayerAction("QUALITY", NeonLime, onOpenVideoTracks))
        if (isVod) add(NeonPlayerAction("SPEED ${playbackSpeed}×", NeonCyan, onOpenPlaybackSpeed))
        if (showEpisodesAction) add(NeonPlayerAction("EPISODES", NeonPink, onOpenEpisodes))
        if (timeshiftUiState.available) add(NeonPlayerAction("LIVE EDGE", NeonLime, onSeekToLiveEdge))
        if (contentType == "LIVE" && currentProgram != null) {
            add(NeonPlayerAction("RESTART", NeonCyan, onRestartProgram))
            add(NeonPlayerAction("ARCHIVE", NeonPink, onOpenArchive))
        }
        if (currentRecordingStatus == RecordingStatus.RECORDING) {
            add(NeonPlayerAction("STOP REC", NeonPink, onStopRecording))
        } else if (contentType == "LIVE") {
            add(NeonPlayerAction("RECORD", NeonPink, onStartRecording))
            add(NeonPlayerAction("SCHEDULE", NeonCyan, onScheduleRecording))
            add(NeonPlayerAction("DAILY", NeonCyan, onScheduleDailyRecording))
            add(NeonPlayerAction("WEEKLY", NeonCyan, onScheduleWeeklyRecording))
        }
        add(NeonPlayerAction("STOP TIMER", NeonCyan, onOpenStopPlaybackTimer))
        add(NeonPlayerAction("IDLE TIMER", NeonCyan, onOpenIdleStandbyTimer))
        if (audioVideoSyncEnabled && !isCastConnected) add(NeonPlayerAction("A/V SYNC", NeonLime, onOpenAudioVideoSync))
        add(NeonPlayerAction("MULTIVIEW", NeonLime, onOpenSplitScreen))
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(0f to NeonCanvas.copy(alpha = .72f), .45f to Color.Transparent, 1f to NeonCanvas.copy(alpha = .86f)))
                .onPreviewKeyEvent { event ->
                    if (event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN) onUserInteraction()
                    false
                }
        ) {
            NeonFuturePlayerIdentity(
                title = displayTitle,
                contentType = contentType,
                currentChannel = currentChannel,
                currentChannelName = currentChannelName,
                displayChannelNumber = displayChannelNumber,
                currentProgram = currentProgram,
                recording = currentRecordingStatus == RecordingStatus.RECORDING,
                onClose = onClose,
                modifier = Modifier.align(Alignment.TopStart).padding(28.dp)
            )
            NeonFutureTransportCluster(
                isPlaying = isPlaying,
                isVod = isVod,
                currentPosition = currentPosition,
                duration = duration,
                currentProgram = currentProgram,
                timeshiftUiState = timeshiftUiState,
                seekPreview = seekPreview,
                playButtonFocusRequester = playButtonFocusRequester,
                onSeekBackward = onSeekBackward,
                onTogglePlayPause = onTogglePlayPause,
                onSeekForward = onSeekForward,
                onSeekToPosition = onSeekToPosition,
                onSetScrubbingMode = onSetScrubbingMode,
                onSeekPreviewPositionChanged = onSeekPreviewPositionChanged,
                modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = 42.dp, vertical = 24.dp)
            )
            LazyRow(
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 26.dp).width(300.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickActions, key = { it.label }) { action ->
                    NeonFuturePlayerActionNode(action, Modifier.focusRequester(quickActionsFocusRequester))
                }
            }
            Text(
                text = when {
                    sleepTimerUiState.stopTimerActive -> "STOP TIMER ACTIVE"
                    sleepTimerUiState.idleTimerActive -> "IDLE TIMER ACTIVE"
                    timeshiftUiState.available -> timeshiftUiState.statusMessage.ifBlank { "LIVE BUFFER AVAILABLE" }
                    else -> "NEON HUD / DPAD CONTROL"
                },
                modifier = Modifier.align(Alignment.TopEnd).padding(32.dp),
                style = MaterialTheme.typography.labelMedium,
                color = NeonMuted
            )
        }
    }
}

@Composable
private fun NeonFuturePlayerIdentity(title: String, contentType: String, currentChannel: Channel?, currentChannelName: String?, displayChannelNumber: Int, currentProgram: Program?, recording: Boolean, onClose: () -> Unit, modifier: Modifier) {
    val shape = RoundedCornerShape(12.dp)
    Column(modifier = modifier.width(420.dp).background(NeonPanel.copy(alpha = .94f), shape).padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text("${if (recording) "● REC" else "SIGNAL"} / $contentType", style = MaterialTheme.typography.labelMedium, color = if (recording) NeonPink else NeonCyan, fontWeight = FontWeight.Black)
        Text(title, style = MaterialTheme.typography.titleLarge, color = NeonText, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            listOfNotNull(currentChannelName ?: currentChannel?.name, currentProgram?.title).joinToString(" · ").ifBlank { "NODE ${displayChannelNumber.toString().padStart(3, '0')}" },
            style = MaterialTheme.typography.bodySmall,
            color = NeonMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        NeonFuturePlayerActionNode(NeonPlayerAction("CLOSE", NeonPink, onClose))
    }
}

@Composable
private fun NeonFutureTransportCluster(
    isPlaying: Boolean,
    isVod: Boolean,
    currentPosition: Long,
    duration: Long,
    currentProgram: Program?,
    timeshiftUiState: PlayerTimeshiftUiState,
    seekPreview: SeekPreviewState,
    playButtonFocusRequester: FocusRequester,
    onSeekBackward: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekToPosition: (Long) -> Unit,
    onSetScrubbingMode: (Boolean) -> Unit,
    onSeekPreviewPositionChanged: (Long?) -> Unit,
    modifier: Modifier
) {
    val shape = RoundedCornerShape(18.dp)
    Column(modifier = modifier.fillMaxWidth().background(NeonPanel.copy(alpha = .95f), shape).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (isVod && duration > 0L) {
            var scrubPosition by remember(currentPosition) { mutableFloatStateOf(currentPosition.toFloat()) }
            Slider(
                value = scrubPosition.coerceIn(0f, duration.toFloat()),
                onValueChange = { position ->
                    onSetScrubbingMode(true)
                    scrubPosition = position
                    onSeekPreviewPositionChanged(position.toLong())
                },
                onValueChangeFinished = {
                    onSeekToPosition(scrubPosition.toLong())
                    onSeekPreviewPositionChanged(null)
                    onSetScrubbingMode(false)
                },
                valueRange = 0f..duration.toFloat(),
                colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan, inactiveTrackColor = NeonMuted.copy(alpha = .25f)),
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "${formatNeonTime(currentPosition)} / ${formatNeonTime(duration)}${seekPreview.positionMs?.let { " · ${formatNeonTime(it)}" }.orEmpty()}",
                style = MaterialTheme.typography.labelSmall,
                color = NeonMuted
            )
        } else if (currentProgram != null) {
            LinearProgressIndicator(progress = { currentProgram.progressPercent() }, modifier = Modifier.fillMaxWidth().height(4.dp), color = NeonPink, trackColor = NeonMuted.copy(alpha = .22f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            NeonFuturePlayerActionNode(NeonPlayerAction("−10", NeonCyan, onSeekBackward))
            NeonFuturePlayerActionNode(NeonPlayerAction(if (isPlaying) "PAUSE" else "PLAY", NeonLime, onTogglePlayPause), Modifier.focusRequester(playButtonFocusRequester))
            NeonFuturePlayerActionNode(NeonPlayerAction("+10", NeonCyan, onSeekForward))
            Text(if (timeshiftUiState.available) timeshiftUiState.statusMessage.ifBlank { "LIVE BUFFER" } else "TRANSPORT READY", style = MaterialTheme.typography.labelMedium, color = NeonMuted)
        }
    }
}

@Composable
private fun NeonFuturePlayerActionNode(action: NeonPlayerAction, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(8.dp)
    TvClickableSurface(
        onClick = action.onClick,
        modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = NeonCanvas, focusedContainerColor = action.tone.copy(alpha = .24f), contentColor = action.tone, focusedContentColor = NeonText),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, action.tone), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.035f)
    ) {
        Text(action.label, modifier = Modifier.padding(horizontal = 13.dp, vertical = 10.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

private fun formatNeonTime(milliseconds: Long): String {
    val seconds = (milliseconds.coerceAtLeast(0L) / 1000L)
    val hours = seconds / 3600L
    val minutes = (seconds % 3600L) / 60L
    val remaining = seconds % 60L
    return if (hours > 0L) "%d:%02d:%02d".format(hours, minutes, remaining) else "%02d:%02d".format(minutes, remaining)
}
