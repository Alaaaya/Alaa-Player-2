package com.streamvault.app.ui.themes.premium

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

private data class PremiumPlayerAction(val label: String, val onClick: () -> Unit)
private enum class PremiumDeck { PLAYBACK, OUTPUT, SESSION }

/** Premium Black: لوحة تحكم معدنية مقسمة أسفل الشاشة، لا rail زجاجي ولا dock Streaming. */
@Composable
internal fun PremiumBlackPlayerOverlay(
    visible: Boolean, title: String, contentType: String, isCatchUpPlayback: Boolean, isPlaying: Boolean, currentProgram: Program?, currentChannel: Channel?, currentChannelName: String?, displayChannelNumber: Int, currentPosition: Long, duration: Long, aspectRatioLabel: String, subtitleTrackCount: Int, liveTranslationAvailable: Boolean, audioTrackCount: Int, videoQualityCount: Int, currentRecordingStatus: RecordingStatus?, isMuted: Boolean, playbackSpeed: Float, mediaTitle: String?, sleepTimerUiState: SleepTimerUiState, timeshiftUiState: PlayerTimeshiftUiState, playButtonFocusRequester: FocusRequester, quickActionsFocusRequester: FocusRequester, modifier: Modifier = Modifier, onClose: () -> Unit, onTogglePlayPause: () -> Unit, onSeekBackward: () -> Unit, onSeekForward: () -> Unit, onRestartProgram: () -> Unit, onOpenArchive: () -> Unit, onStartRecording: () -> Unit, onStopRecording: () -> Unit, onScheduleRecording: () -> Unit, onScheduleDailyRecording: () -> Unit, onScheduleWeeklyRecording: () -> Unit, onToggleAspectRatio: () -> Unit, onOpenSubtitleTracks: () -> Unit, onOpenAudioTracks: () -> Unit, onOpenVideoTracks: () -> Unit, onOpenPlaybackSpeed: () -> Unit, onOpenStopPlaybackTimer: () -> Unit, onOpenIdleStandbyTimer: () -> Unit, onOpenAudioVideoSync: () -> Unit, audioVideoSyncEnabled: Boolean, showEpisodesAction: Boolean, onOpenEpisodes: () -> Unit, onOpenSplitScreen: () -> Unit, onEnterPictureInPicture: () -> Unit, onToggleMute: () -> Unit, isCastConnected: Boolean, onCast: () -> Unit, onStopCasting: () -> Unit, onSeekToLiveEdge: () -> Unit, onSeekToPosition: (Long) -> Unit, onSetScrubbingMode: (Boolean) -> Unit, seekPreview: SeekPreviewState, onSeekPreviewPositionChanged: (Long?) -> Unit, onUserInteraction: () -> Unit
) {
    val vod = contentType != "LIVE" || isCatchUpPlayback
    val displayTitle = currentProgram?.title?.takeIf { contentType == "LIVE" } ?: mediaTitle?.takeIf { it.isNotBlank() } ?: title
    var deck by remember { mutableStateOf(PremiumDeck.PLAYBACK) }
    val actions = remember(deck, vod, currentProgram, timeshiftUiState.available, isMuted, playbackSpeed, subtitleTrackCount, liveTranslationAvailable, audioTrackCount, videoQualityCount, currentRecordingStatus, showEpisodesAction, audioVideoSyncEnabled, isCastConnected) { when (deck) {
        PremiumDeck.PLAYBACK -> buildList { if (contentType == "LIVE" && currentProgram != null) { add(PremiumPlayerAction("RESTART", onRestartProgram)); add(PremiumPlayerAction("ARCHIVE", onOpenArchive)) }; if (timeshiftUiState.available) add(PremiumPlayerAction("LIVE EDGE", onSeekToLiveEdge)); if (vod) add(PremiumPlayerAction("SPEED ${playbackSpeed}×", onOpenPlaybackSpeed)); if (showEpisodesAction) add(PremiumPlayerAction("EPISODES", onOpenEpisodes)); add(PremiumPlayerAction(if (isMuted) "UNMUTE" else "MUTE", onToggleMute)) }
        PremiumDeck.OUTPUT -> buildList { add(PremiumPlayerAction("DISPLAY $aspectRatioLabel", onToggleAspectRatio)); if (subtitleTrackCount > 0 || liveTranslationAvailable) add(PremiumPlayerAction("SUBTITLES", onOpenSubtitleTracks)); if (audioTrackCount > 0) add(PremiumPlayerAction("AUDIO", onOpenAudioTracks)); if (videoQualityCount > 0) add(PremiumPlayerAction("QUALITY", onOpenVideoTracks)); add(PremiumPlayerAction(if (isCastConnected) "STOP CAST" else "CAST", if (isCastConnected) onStopCasting else onCast)); add(PremiumPlayerAction("PICTURE IN PICTURE", onEnterPictureInPicture)) }
        PremiumDeck.SESSION -> buildList { if (currentRecordingStatus == RecordingStatus.RECORDING) add(PremiumPlayerAction("STOP RECORDING", onStopRecording)) else if (contentType == "LIVE") { add(PremiumPlayerAction("RECORD", onStartRecording)); add(PremiumPlayerAction("SCHEDULE", onScheduleRecording)); add(PremiumPlayerAction("DAILY SCHEDULE", onScheduleDailyRecording)); add(PremiumPlayerAction("WEEKLY SCHEDULE", onScheduleWeeklyRecording)) }; add(PremiumPlayerAction("STOP TIMER", onOpenStopPlaybackTimer)); add(PremiumPlayerAction("IDLE TIMER", onOpenIdleStandbyTimer)); if (audioVideoSyncEnabled && !isCastConnected) add(PremiumPlayerAction("A/V SYNC", onOpenAudioVideoSync)); add(PremiumPlayerAction("MULTIVIEW", onOpenSplitScreen)) }
    } + PremiumPlayerAction("EXIT", onClose) }
    AnimatedVisibility(visible, enter = fadeIn(tween(PremiumFocusMotionMs)), exit = fadeOut(tween(PremiumFocusMotionMs)), modifier = modifier) { Box(Modifier.fillMaxSize().background(PremiumCanvas.copy(alpha = .62f)).onPreviewKeyEvent { event -> if (event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN) onUserInteraction(); false }) {
        Column(Modifier.align(Alignment.TopStart).padding(32.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) { Text("PREMIUM PLAYBACK / ${if (vod) "ON DEMAND" else "LIVE ${displayChannelNumber.toString().padStart(3, '0')}"}", style = MaterialTheme.typography.labelMedium, color = PremiumGold); Text(displayTitle, style = MaterialTheme.typography.titleLarge, color = PremiumText, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(currentChannelName ?: currentChannel?.name.orEmpty(), style = MaterialTheme.typography.bodySmall, color = PremiumMuted) }
        PremiumTransport(isPlaying, currentPosition, duration, seekPreview, playButtonFocusRequester, onSeekBackward, onTogglePlayPause, onSeekForward, onSeekToPosition, onSetScrubbingMode, onSeekPreviewPositionChanged, Modifier.align(Alignment.Center))
        PremiumMetalConsole(deck, { deck = it }, actions, quickActionsFocusRequester, sleepTimerUiState, timeshiftUiState, currentRecordingStatus, Modifier.align(Alignment.BottomCenter).padding(24.dp))
    } }
}

@Composable private fun PremiumTransport(playing: Boolean, position: Long, duration: Long, preview: SeekPreviewState, focusRequester: FocusRequester, onBack: () -> Unit, onToggle: () -> Unit, onForward: () -> Unit, onSeek: (Long) -> Unit, onScrub: (Boolean) -> Unit, onPreview: (Long?) -> Unit, modifier: Modifier) { var pending by remember { mutableFloatStateOf(0f) }; val safe = duration.coerceAtLeast(0); val current = if (safe > 0) (position.toFloat() / safe).coerceIn(0f, 1f) else 0f; Column(modifier.background(PremiumPanel, RoundedCornerShape(10.dp)).padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) { Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) { PremiumPlayerButton("−10", onBack); PremiumPlayerButton(if (playing) "PAUSE" else "PLAY", onToggle, Modifier.focusRequester(focusRequester), true); PremiumPlayerButton("+10", onForward) }; if (safe > 0) Slider(value = if (pending == 0f) current else pending, onValueChange = { value -> pending = value; onScrub(true); onPreview((value * safe).toLong()) }, onValueChangeFinished = { val value = if (pending == 0f) current else pending; onSeek((value * safe).toLong()); onPreview(null); onScrub(false); pending = 0f }, colors = SliderDefaults.colors(thumbColor = PremiumFocus, activeTrackColor = PremiumGold, inactiveTrackColor = PremiumMetal), modifier = Modifier.fillMaxWidth()); Text(if (preview.visible) "SEEK PREVIEW / ${(preview.positionMs / 1000L)}s" else "TRANSPORT / ${(position / 1000L)}s", style = MaterialTheme.typography.labelSmall, color = PremiumMuted) } }
@Composable private fun PremiumMetalConsole(deck: PremiumDeck, onDeck: (PremiumDeck) -> Unit, actions: List<PremiumPlayerAction>, requester: FocusRequester, timers: SleepTimerUiState, timeshift: PlayerTimeshiftUiState, recording: RecordingStatus?, modifier: Modifier) { Column(modifier.fillMaxWidth().background(PremiumPanel, RoundedCornerShape(10.dp)).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) { PremiumDeck.entries.forEach { entry -> PremiumPlayerButton(entry.name, { onDeck(entry) }, primary = entry == deck) }; Text(when { recording == RecordingStatus.RECORDING -> "RECORDING ACTIVE"; timers.stopTimerActive -> "STOP TIMER ACTIVE"; timers.idleTimerActive -> "IDLE TIMER ACTIVE"; timeshift.available -> timeshift.statusMessage.ifBlank { "LIVE BUFFER READY" }; else -> "METAL CONTROL CONSOLE" }, Modifier.padding(start = 12.dp, top = 10.dp), style = MaterialTheme.typography.labelSmall, color = PremiumMuted) }; LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) { itemsIndexed(actions, key = { _, action -> "${deck.name}_${action.label}" }) { index, action -> PremiumPlayerButton(action.label, action.onClick, if (index == 0) Modifier.focusRequester(requester) else Modifier) } } } }
@Composable private fun PremiumPlayerButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, primary: Boolean = false) { val shape = RoundedCornerShape(6.dp); TvClickableSurface(onClick = onClick, modifier = modifier, shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = if (primary) PremiumPanelFocused else PremiumCanvasRaised, focusedContainerColor = PremiumPanelFocused, contentColor = PremiumText, focusedContentColor = PremiumText), border = ClickableSurfaceDefaults.border(border = Border(border = BorderStroke(1.dp, if (primary) PremiumGold else PremiumMetal), shape = shape), focusedBorder = Border(border = BorderStroke(2.dp, PremiumFocus), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f)) { Text(label, Modifier.padding(horizontal = 15.dp, vertical = 11.dp), style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis) } }
