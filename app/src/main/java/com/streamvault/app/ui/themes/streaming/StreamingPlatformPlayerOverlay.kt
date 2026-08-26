package com.streamvault.app.ui.themes.streaming

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onPreviewKeyEvent
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

private data class StreamingPlayerAction(val label: String, val onClick: () -> Unit)
private enum class StreamingPlayerPanel(val label: String) { WATCH("WATCH"), STREAM("STREAM"), SESSION("SESSION") }

/** مشغل Streaming Platform: محطة تحكم سفلية مقسمة، لا rail زجاجي ولا شريط Minimal/Neon. */
@Composable
internal fun StreamingPlatformPlayerOverlay(
    visible: Boolean, title: String, contentType: String, isCatchUpPlayback: Boolean, isPlaying: Boolean,
    currentProgram: Program?, currentChannel: Channel?, currentChannelName: String?, displayChannelNumber: Int,
    currentPosition: Long, duration: Long, aspectRatioLabel: String, subtitleTrackCount: Int, liveTranslationAvailable: Boolean,
    audioTrackCount: Int, videoQualityCount: Int, currentRecordingStatus: RecordingStatus?, isMuted: Boolean, playbackSpeed: Float,
    mediaTitle: String?, sleepTimerUiState: SleepTimerUiState, timeshiftUiState: PlayerTimeshiftUiState,
    playButtonFocusRequester: FocusRequester, quickActionsFocusRequester: FocusRequester, modifier: Modifier = Modifier,
    onClose: () -> Unit, onTogglePlayPause: () -> Unit, onSeekBackward: () -> Unit, onSeekForward: () -> Unit,
    onRestartProgram: () -> Unit, onOpenArchive: () -> Unit, onStartRecording: () -> Unit, onStopRecording: () -> Unit,
    onScheduleRecording: () -> Unit, onScheduleDailyRecording: () -> Unit, onScheduleWeeklyRecording: () -> Unit,
    onToggleAspectRatio: () -> Unit, onOpenSubtitleTracks: () -> Unit, onOpenAudioTracks: () -> Unit, onOpenVideoTracks: () -> Unit,
    onOpenPlaybackSpeed: () -> Unit, onOpenStopPlaybackTimer: () -> Unit, onOpenIdleStandbyTimer: () -> Unit,
    onOpenAudioVideoSync: () -> Unit, audioVideoSyncEnabled: Boolean, showEpisodesAction: Boolean, onOpenEpisodes: () -> Unit,
    onOpenSplitScreen: () -> Unit, onEnterPictureInPicture: () -> Unit, onToggleMute: () -> Unit,
    isCastConnected: Boolean, onCast: () -> Unit, onStopCasting: () -> Unit, onSeekToLiveEdge: () -> Unit,
    onSeekToPosition: (Long) -> Unit, onSetScrubbingMode: (Boolean) -> Unit, seekPreview: SeekPreviewState,
    onSeekPreviewPositionChanged: (Long?) -> Unit, onUserInteraction: () -> Unit
) {
    val isVod = contentType != "LIVE" || isCatchUpPlayback
    val displayTitle = currentProgram?.title?.takeIf { contentType == "LIVE" } ?: mediaTitle?.takeIf { it.isNotBlank() } ?: title
    var panel by remember { mutableStateOf(StreamingPlayerPanel.WATCH) }
    val actions = remember(panel, isVod, currentProgram, timeshiftUiState.available, isMuted, playbackSpeed, subtitleTrackCount, liveTranslationAvailable, audioTrackCount, videoQualityCount, currentRecordingStatus, showEpisodesAction, audioVideoSyncEnabled, isCastConnected) {
        when (panel) {
            StreamingPlayerPanel.WATCH -> buildList {
                if (contentType == "LIVE" && currentProgram != null) add(StreamingPlayerAction("RESTART", onRestartProgram))
                if (contentType == "LIVE" && currentProgram != null) add(StreamingPlayerAction("ARCHIVE", onOpenArchive))
                if (timeshiftUiState.available) add(StreamingPlayerAction("LIVE EDGE", onSeekToLiveEdge))
                if (isVod) add(StreamingPlayerAction("SPEED ${playbackSpeed}×", onOpenPlaybackSpeed))
                if (showEpisodesAction) add(StreamingPlayerAction("EPISODES", onOpenEpisodes))
                add(StreamingPlayerAction(if (isMuted) "UNMUTE" else "MUTE", onToggleMute))
            }
            StreamingPlayerPanel.STREAM -> buildList {
                add(StreamingPlayerAction("DISPLAY $aspectRatioLabel", onToggleAspectRatio))
                if (subtitleTrackCount > 0 || liveTranslationAvailable) add(StreamingPlayerAction("SUBTITLES", onOpenSubtitleTracks))
                if (audioTrackCount > 0) add(StreamingPlayerAction("AUDIO", onOpenAudioTracks))
                if (videoQualityCount > 0) add(StreamingPlayerAction("QUALITY", onOpenVideoTracks))
                add(StreamingPlayerAction(if (isCastConnected) "STOP CAST" else "CAST", if (isCastConnected) onStopCasting else onCast))
                add(StreamingPlayerAction("PICTURE IN PICTURE", onEnterPictureInPicture))
            }
            StreamingPlayerPanel.SESSION -> buildList {
                if (currentRecordingStatus == RecordingStatus.RECORDING) add(StreamingPlayerAction("STOP RECORDING", onStopRecording))
                else if (contentType == "LIVE") { add(StreamingPlayerAction("RECORD", onStartRecording)); add(StreamingPlayerAction("SCHEDULE", onScheduleRecording)); add(StreamingPlayerAction("DAILY SCHEDULE", onScheduleDailyRecording)); add(StreamingPlayerAction("WEEKLY SCHEDULE", onScheduleWeeklyRecording)) }
                add(StreamingPlayerAction("STOP TIMER", onOpenStopPlaybackTimer)); add(StreamingPlayerAction("IDLE TIMER", onOpenIdleStandbyTimer))
                if (audioVideoSyncEnabled && !isCastConnected) add(StreamingPlayerAction("A/V SYNC", onOpenAudioVideoSync))
                add(StreamingPlayerAction("MULTIVIEW", onOpenSplitScreen))
            }
        } + StreamingPlayerAction("EXIT", onClose)
    }
    AnimatedVisibility(visible = visible, enter = fadeIn(tween(180)), exit = fadeOut(tween(180)), modifier = modifier) {
        Box(Modifier.fillMaxSize().background(StreamingCanvas.copy(alpha = .58f)).onPreviewKeyEvent { event -> if (event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN) onUserInteraction(); false }) {
            Column(Modifier.align(Alignment.TopStart).padding(start = 34.dp, top = 28.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("STREAMING PLAYER / ${if (isVod) "ON DEMAND" else "LIVE ${displayChannelNumber.toString().padStart(3, '0')}"}", style = MaterialTheme.typography.labelMedium, color = StreamingAccent)
                Text(displayTitle, style = MaterialTheme.typography.titleLarge, color = StreamingText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(currentChannelName ?: currentChannel?.name.orEmpty(), style = MaterialTheme.typography.bodySmall, color = StreamingMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            StreamingTransportStation(isPlaying, currentPosition, duration, seekPreview, playButtonFocusRequester, onSeekBackward, onTogglePlayPause, onSeekForward, onSeekToPosition, onSetScrubbingMode, onSeekPreviewPositionChanged, Modifier.align(Alignment.Center))
            StreamingBottomStation(panel, { panel = it }, actions, quickActionsFocusRequester, sleepTimerUiState, timeshiftUiState, currentRecordingStatus, Modifier.align(Alignment.BottomCenter).padding(horizontal = 28.dp, vertical = 24.dp))
        }
    }
}

@Composable private fun StreamingTransportStation(isPlaying: Boolean, currentPosition: Long, duration: Long, seekPreview: SeekPreviewState, playRequester: FocusRequester, onBack: () -> Unit, onToggle: () -> Unit, onForward: () -> Unit, onSeekTo: (Long) -> Unit, onScrub: (Boolean) -> Unit, onPreview: (Long?) -> Unit, modifier: Modifier) { var pending by remember { mutableFloatStateOf(0f) }; val safeDuration = duration.coerceAtLeast(0L); val current = if (safeDuration > 0) (currentPosition.toFloat() / safeDuration).coerceIn(0f, 1f) else 0f; Column(modifier.background(StreamingPanel, RoundedCornerShape(22.dp)).padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) { Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) { StreamingTransportButton("−10", onBack); StreamingTransportButton(if (isPlaying) "PAUSE" else "PLAY", onToggle, Modifier.focusRequester(playRequester), primary = true); StreamingTransportButton("+10", onForward) }; if (safeDuration > 0) Slider(value = if (pending == 0f) current else pending, onValueChange = { value -> pending = value; onScrub(true); onPreview((value * safeDuration).toLong()) }, onValueChangeFinished = { val value = if (pending == 0f) current else pending; onSeekTo((value * safeDuration).toLong()); onPreview(null); onScrub(false); pending = 0f }, colors = SliderDefaults.colors(thumbColor = StreamingFocus, activeTrackColor = StreamingAccent, inactiveTrackColor = StreamingRule), modifier = Modifier.fillMaxWidth()); Text(if (seekPreview.visible) "SEEK PREVIEW / ${(seekPreview.positionMs / 1_000L)}s" else "TRANSPORT / ${(currentPosition / 1_000L)}s", style = MaterialTheme.typography.labelSmall, color = StreamingMuted) } }
@Composable private fun StreamingBottomStation(panel: StreamingPlayerPanel, onPanel: (StreamingPlayerPanel) -> Unit, actions: List<StreamingPlayerAction>, focusRequester: FocusRequester, timers: SleepTimerUiState, timeshift: PlayerTimeshiftUiState, recording: RecordingStatus?, modifier: Modifier) { Column(modifier.fillMaxWidth().background(StreamingPanel, RoundedCornerShape(18.dp)).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { StreamingPlayerPanel.entries.forEach { entry -> StreamingDockTab(entry, entry == panel) { onPanel(entry) } }; Text(when { recording == RecordingStatus.RECORDING -> "RECORDING ACTIVE"; timers.stopTimerActive -> "STOP TIMER ACTIVE"; timers.idleTimerActive -> "IDLE TIMER ACTIVE"; timeshift.available -> timeshift.statusMessage.ifBlank { "LIVE BUFFER READY" }; else -> "CONTROL STATION" }, Modifier.padding(start = 14.dp, top = 10.dp), style = MaterialTheme.typography.labelSmall, color = StreamingMuted) }; LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) { itemsIndexed(actions, key = { _, action -> "${panel.name}_${action.label}" }) { index, action -> StreamingDockAction(action, if (index == 0) Modifier.focusRequester(focusRequester) else Modifier) } } } }
@Composable private fun StreamingDockTab(panel: StreamingPlayerPanel, selected: Boolean, onClick: () -> Unit) { val shape = RoundedCornerShape(10.dp); TvClickableSurface(onClick = onClick, shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = if (selected) StreamingPanelFocused else StreamingCanvasRaised, focusedContainerColor = StreamingPanelFocused, contentColor = StreamingText, focusedContentColor = StreamingText), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, StreamingFocus), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.025f)) { Text(panel.label, Modifier.padding(horizontal = 15.dp, vertical = 9.dp), style = MaterialTheme.typography.labelMedium) } }
@Composable private fun StreamingDockAction(action: StreamingPlayerAction, modifier: Modifier = Modifier) { val shape = RoundedCornerShape(12.dp); TvClickableSurface(onClick = action.onClick, modifier = modifier, shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = StreamingCanvasRaised, focusedContainerColor = StreamingPanelFocused, contentColor = StreamingText, focusedContentColor = StreamingText), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, StreamingFocus), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.035f)) { Text(action.label, Modifier.padding(horizontal = 15.dp, vertical = 12.dp), style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis) } }
@Composable private fun StreamingTransportButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, primary: Boolean = false) { val shape = RoundedCornerShape(12.dp); TvClickableSurface(onClick = onClick, modifier = modifier, shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = if (primary) StreamingPanelFocused else StreamingCanvasRaised, focusedContainerColor = StreamingPanelFocused, contentColor = StreamingText, focusedContentColor = StreamingText), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, StreamingFocus), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.07f)) { Text(label, Modifier.padding(horizontal = 22.dp, vertical = 14.dp), style = MaterialTheme.typography.titleMedium) } }
