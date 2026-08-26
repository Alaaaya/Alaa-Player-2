package com.streamvault.app.ui.themes.blueocean

/**
 * Style contract: Blue Ocean fullscreen playback is a tide dossier: right-aligned programme data,
 * a left transport dock, a wave timeline, and separated dock modules in the lower action bar.
 */

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.streamvault.app.ui.theme.LocalThemePresentation
import com.streamvault.domain.model.Channel
import com.streamvault.domain.model.Program
import com.streamvault.domain.model.RecordingStatus

private data class BlueOceanAction(val label: String, val detail: String, val onClick: () -> Unit)

@Composable
internal fun BlueOceanPlayerOverlay(
    visible: Boolean, title: String, contentType: String, isCatchUpPlayback: Boolean, isPlaying: Boolean, currentProgram: Program?, currentChannel: Channel?, currentChannelName: String?, displayChannelNumber: Int, currentPosition: Long, duration: Long, aspectRatioLabel: String, subtitleTrackCount: Int, liveTranslationAvailable: Boolean, audioTrackCount: Int, videoQualityCount: Int, currentRecordingStatus: RecordingStatus?, isMuted: Boolean, playbackSpeed: Float, mediaTitle: String?, sleepTimerUiState: SleepTimerUiState, timeshiftUiState: PlayerTimeshiftUiState, playButtonFocusRequester: FocusRequester, quickActionsFocusRequester: FocusRequester, modifier: Modifier = Modifier, onClose: () -> Unit, onTogglePlayPause: () -> Unit, onSeekBackward: () -> Unit, onSeekForward: () -> Unit, onRestartProgram: () -> Unit, onOpenArchive: () -> Unit, onStartRecording: () -> Unit, onStopRecording: () -> Unit, onScheduleRecording: () -> Unit, onScheduleDailyRecording: () -> Unit, onScheduleWeeklyRecording: () -> Unit, onToggleAspectRatio: () -> Unit, onOpenSubtitleTracks: () -> Unit, onOpenAudioTracks: () -> Unit, onOpenVideoTracks: () -> Unit, onOpenPlaybackSpeed: () -> Unit, onOpenStopPlaybackTimer: () -> Unit, onOpenIdleStandbyTimer: () -> Unit, onOpenAudioVideoSync: () -> Unit, audioVideoSyncEnabled: Boolean, showEpisodesAction: Boolean, onOpenEpisodes: () -> Unit, onOpenSplitScreen: () -> Unit, onEnterPictureInPicture: () -> Unit, onToggleMute: () -> Unit, isCastConnected: Boolean, onCast: () -> Unit, onStopCasting: () -> Unit, onSeekToLiveEdge: () -> Unit, onSeekToPosition: (Long) -> Unit, onSetScrubbingMode: (Boolean) -> Unit, seekPreview: SeekPreviewState, onSeekPreviewPositionChanged: (Long?) -> Unit, onUserInteraction: () -> Unit
) {
    val p = LocalThemePresentation.current
    val s = p.surfaces
    val displayTitle = currentProgram?.title?.takeIf { contentType == "LIVE" } ?: mediaTitle?.takeIf { it.isNotBlank() } ?: title
    val actions = buildList {
        add(BlueOceanAction("GUIDE", "programme tide", onOpenArchive))
        add(BlueOceanAction("AUDIO", if (audioTrackCount > 0) "$audioTrackCount tracks" else "default track", onOpenAudioTracks))
        add(BlueOceanAction("CAPTIONS", if (subtitleTrackCount > 0 || liveTranslationAvailable) "available" else "none", onOpenSubtitleTracks))
        add(BlueOceanAction("VIEW", aspectRatioLabel, onToggleAspectRatio))
        add(BlueOceanAction(if (isMuted) "UNMUTE" else "MUTE", "sound", onToggleMute))
        if (videoQualityCount > 0) add(BlueOceanAction("QUALITY", "$videoQualityCount levels", onOpenVideoTracks))
        if (contentType == "LIVE") add(BlueOceanAction(if (currentRecordingStatus == RecordingStatus.RECORDING) "STOP RECORD" else "RECORD", "session", if (currentRecordingStatus == RecordingStatus.RECORDING) onStopRecording else onStartRecording))
        if (showEpisodesAction) add(BlueOceanAction("EPISODES", "next wave", onOpenEpisodes))
        add(BlueOceanAction("EXIT", "return", onClose))
    }
    AnimatedVisibility(visible, enter = fadeIn(tween(210)), exit = fadeOut(tween(170)), modifier = modifier) { Box(Modifier.fillMaxSize().background(s.canvas.copy(alpha = .73f)).onPreviewKeyEvent { event -> if (event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN) onUserInteraction(); false }) {
        BlueOceanDossierHeader(displayTitle, currentChannelName ?: currentChannel?.name.orEmpty(), displayChannelNumber, Modifier.align(Alignment.TopEnd).padding(34.dp))
        BlueOceanTransportDock(isPlaying, onSeekBackward, onTogglePlayPause, onSeekForward, playButtonFocusRequester, Modifier.align(Alignment.CenterStart).padding(start = 34.dp))
        BlueOceanWaveTimeline(currentPosition, duration, seekPreview, onSeekToPosition, onSetScrubbingMode, onSeekPreviewPositionChanged, timeshiftUiState, Modifier.align(Alignment.BottomCenter).padding(start = 230.dp, end = 34.dp, bottom = 124.dp))
        BlueOceanDockBar(actions, quickActionsFocusRequester, sleepTimerUiState, isCastConnected, onCast, onStopCasting, Modifier.align(Alignment.BottomCenter).padding(horizontal = 34.dp, vertical = 24.dp))
    } }
}

@Composable
private fun BlueOceanDossierHeader(title: String, channel: String, number: Int, modifier: Modifier) { val s = LocalThemePresentation.current.surfaces; Column(modifier.width(460.dp).background(s.browseContent, RoundedCornerShape(26.dp)).padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) { Text("LIVE DOSSIER · ${number.toString().padStart(3, '0')}", style = MaterialTheme.typography.labelMedium, color = s.accent); Text(title, style = MaterialTheme.typography.titleLarge, color = s.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis); Text(channel, style = MaterialTheme.typography.bodyMedium, color = s.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis) } }

@Composable
private fun BlueOceanTransportDock(playing: Boolean, onBack: () -> Unit, onToggle: () -> Unit, onForward: () -> Unit, requester: FocusRequester, modifier: Modifier) { val s = LocalThemePresentation.current.surfaces; Column(modifier.background(s.browseContent, RoundedCornerShape(28.dp)).padding(11.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) { BlueOceanDockButton("−10", onBack); BlueOceanDockButton(if (playing) "PAUSE" else "PLAY", onToggle, Modifier.focusRequester(requester), true); BlueOceanDockButton("+10", onForward) } }

@Composable
private fun BlueOceanWaveTimeline(position: Long, duration: Long, preview: SeekPreviewState, onSeek: (Long) -> Unit, onScrub: (Boolean) -> Unit, onPreview: (Long?) -> Unit, timeshift: PlayerTimeshiftUiState, modifier: Modifier) { val s = LocalThemePresentation.current.surfaces; var pending by remember { mutableFloatStateOf(0f) }; val safe = duration.coerceAtLeast(0L); val progress = if (safe > 0) (position.toFloat() / safe).coerceIn(0f, 1f) else 0f; Column(modifier.background(s.browseContent, RoundedCornerShape(22.dp)).padding(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("TIDE LINE", style = MaterialTheme.typography.labelSmall, color = s.accent); Text(if (timeshift.available) timeshift.statusMessage.ifBlank { "LIVE BUFFER" } else if (preview.visible) "SEEK ${(preview.positionMs / 1000L)}s" else "${position / 1000L}s", style = MaterialTheme.typography.labelSmall, color = s.textSecondary) }; if (safe > 0) Slider(value = if (pending == 0f) progress else pending, onValueChange = { value -> pending = value; onScrub(true); onPreview((value * safe).toLong()) }, onValueChangeFinished = { val value = if (pending == 0f) progress else pending; onSeek((value * safe).toLong()); onPreview(null); onScrub(false); pending = 0f }, colors = SliderDefaults.colors(thumbColor = s.accent, activeTrackColor = s.accent, inactiveTrackColor = s.textSecondary.copy(alpha = .24f)), modifier = Modifier.fillMaxWidth()) } }

@Composable
private fun BlueOceanDockBar(actions: List<BlueOceanAction>, requester: FocusRequester, timers: SleepTimerUiState, isCasting: Boolean, onCast: () -> Unit, onStopCast: () -> Unit, modifier: Modifier) { val s = LocalThemePresentation.current.surfaces; Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(7.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("HARBOUR ACTIONS", style = MaterialTheme.typography.labelMedium, color = s.accent); Text(when { isCasting -> "CASTING"; timers.stopTimerActive -> "STOP TIMER"; timers.idleTimerActive -> "IDLE TIMER"; else -> "REMOTE READY" }, style = MaterialTheme.typography.labelSmall, color = s.textSecondary) }; LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) { itemsIndexed(actions, key = { index, action -> "blue_ocean_${index}_${action.label}" }) { index, action -> BlueOceanActionModule(action, if (index == 0) Modifier.focusRequester(requester) else Modifier) }; item { BlueOceanActionModule(BlueOceanAction(if (isCasting) "STOP CAST" else "CAST", "output", if (isCasting) onStopCast else onCast)) } } } }

@Composable
private fun BlueOceanActionModule(action: BlueOceanAction, modifier: Modifier = Modifier) { val p = LocalThemePresentation.current; val s = p.surfaces; val shape = RoundedCornerShape(16.dp); TvClickableSurface(onClick = action.onClick, modifier = modifier.width(146.dp), shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = s.browseContent, focusedContainerColor = s.focusedSurface, contentColor = s.textPrimary, focusedContentColor = s.textPrimary), border = ClickableSurfaceDefaults.border(border = Border(border = BorderStroke(1.dp, s.textSecondary.copy(alpha = .25f)), shape = shape), focusedBorder = Border(border = BorderStroke(2.dp, s.accent), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.025f)) { Column(Modifier.padding(horizontal = 13.dp, vertical = 11.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) { Text(action.label, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(action.detail, style = MaterialTheme.typography.labelSmall, color = s.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis) } } }

@Composable
private fun BlueOceanDockButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, primary: Boolean = false) { val p = LocalThemePresentation.current; val s = p.surfaces; val shape = RoundedCornerShape(17.dp); TvClickableSurface(onClick = onClick, modifier = modifier.width(112.dp), shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = if (primary) s.selectedAccent else s.canvas, focusedContainerColor = s.focusedSurface, contentColor = s.textPrimary, focusedContentColor = s.textPrimary), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, s.accent), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.025f)) { Text(label, Modifier.padding(vertical = 12.dp), style = MaterialTheme.typography.labelLarge) } }
