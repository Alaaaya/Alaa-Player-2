package com.streamvault.app.ui.themes.redcinema

/** Red Cinema fullscreen contract: centred reels, a film-strip timeline, and a ticket-window action bar. */

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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
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

private data class RedCinemaAction(val label: String, val detail: String, val onClick: () -> Unit)

@Composable
internal fun RedCinemaPlayerOverlay(
    visible: Boolean, title: String, contentType: String, isCatchUpPlayback: Boolean, isPlaying: Boolean, currentProgram: Program?, currentChannel: Channel?, currentChannelName: String?, displayChannelNumber: Int, currentPosition: Long, duration: Long, aspectRatioLabel: String, subtitleTrackCount: Int, liveTranslationAvailable: Boolean, audioTrackCount: Int, videoQualityCount: Int, currentRecordingStatus: RecordingStatus?, isMuted: Boolean, playbackSpeed: Float, mediaTitle: String?, sleepTimerUiState: SleepTimerUiState, timeshiftUiState: PlayerTimeshiftUiState, playButtonFocusRequester: FocusRequester, quickActionsFocusRequester: FocusRequester, modifier: Modifier = Modifier, onClose: () -> Unit, onTogglePlayPause: () -> Unit, onSeekBackward: () -> Unit, onSeekForward: () -> Unit, onRestartProgram: () -> Unit, onOpenArchive: () -> Unit, onStartRecording: () -> Unit, onStopRecording: () -> Unit, onScheduleRecording: () -> Unit, onScheduleDailyRecording: () -> Unit, onScheduleWeeklyRecording: () -> Unit, onToggleAspectRatio: () -> Unit, onOpenSubtitleTracks: () -> Unit, onOpenAudioTracks: () -> Unit, onOpenVideoTracks: () -> Unit, onOpenPlaybackSpeed: () -> Unit, onOpenStopPlaybackTimer: () -> Unit, onOpenIdleStandbyTimer: () -> Unit, onOpenAudioVideoSync: () -> Unit, audioVideoSyncEnabled: Boolean, showEpisodesAction: Boolean, onOpenEpisodes: () -> Unit, onOpenSplitScreen: () -> Unit, onEnterPictureInPicture: () -> Unit, onToggleMute: () -> Unit, isCastConnected: Boolean, onCast: () -> Unit, onStopCasting: () -> Unit, onSeekToLiveEdge: () -> Unit, onSeekToPosition: (Long) -> Unit, onSetScrubbingMode: (Boolean) -> Unit, seekPreview: SeekPreviewState, onSeekPreviewPositionChanged: (Long?) -> Unit, onUserInteraction: () -> Unit
) {
    val surfaces = LocalThemePresentation.current.surfaces
    val showTitle = currentProgram?.title?.takeIf { contentType == "LIVE" } ?: mediaTitle?.takeIf { it.isNotBlank() } ?: title
    val actions = buildList {
        add(RedCinemaAction("GUIDE", "programme", onOpenArchive))
        add(RedCinemaAction("AUDIO", "$audioTrackCount track(s)", onOpenAudioTracks))
        add(RedCinemaAction("SUBTITLES", if (subtitleTrackCount > 0 || liveTranslationAvailable) "available" else "none", onOpenSubtitleTracks))
        add(RedCinemaAction("FRAME", aspectRatioLabel, onToggleAspectRatio))
        add(RedCinemaAction(if (isMuted) "UNMUTE" else "MUTE", "sound", onToggleMute))
        if (videoQualityCount > 0) add(RedCinemaAction("QUALITY", "$videoQualityCount prints", onOpenVideoTracks))
        if (contentType == "LIVE") add(RedCinemaAction(if (currentRecordingStatus == RecordingStatus.RECORDING) "STOP RECORD" else "RECORD", "screening", if (currentRecordingStatus == RecordingStatus.RECORDING) onStopRecording else onStartRecording))
        if (showEpisodesAction) add(RedCinemaAction("EPISODES", "serial", onOpenEpisodes))
        add(RedCinemaAction("EXIT", "curtain", onClose))
    }
    AnimatedVisibility(visible, enter = fadeIn(tween(180)), exit = fadeOut(tween(160)), modifier = modifier) {
        Box(Modifier.fillMaxSize().background(surfaces.canvas.copy(alpha = .8f)).onPreviewKeyEvent { event -> if (event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN) onUserInteraction(); false }) {
            RedCinemaMarquee(showTitle, currentChannelName ?: currentChannel?.name.orEmpty(), displayChannelNumber, Modifier.align(Alignment.TopCenter).padding(top = 30.dp))
            RedCinemaReelTransport(isPlaying, onSeekBackward, onTogglePlayPause, onSeekForward, playButtonFocusRequester, Modifier.align(Alignment.Center))
            RedCinemaFilmStrip(currentPosition, duration, seekPreview, onSeekToPosition, onSetScrubbingMode, onSeekPreviewPositionChanged, timeshiftUiState, Modifier.align(Alignment.BottomCenter).padding(horizontal = 170.dp, bottom = 118.dp))
            RedCinemaTicketWindow(actions, quickActionsFocusRequester, sleepTimerUiState, isCastConnected, onCast, onStopCasting, Modifier.align(Alignment.BottomCenter).padding(horizontal = 28.dp, vertical = 22.dp))
        }
    }
}

@Composable
private fun RedCinemaMarquee(title: String, channel: String, number: Int, modifier: Modifier) { val s = LocalThemePresentation.current.surfaces; Column(modifier.background(s.browseContent, RoundedCornerShape(2.dp)).padding(horizontal = 24.dp, vertical = 15.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) { Text("SCREEN ${number.toString().padStart(3, '0')} · NOW SHOWING", style = MaterialTheme.typography.labelMedium, color = s.accent); Text(title, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(channel, style = MaterialTheme.typography.bodySmall, color = s.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis) } }

@Composable
private fun RedCinemaReelTransport(playing: Boolean, onBack: () -> Unit, onToggle: () -> Unit, onForward: () -> Unit, requester: FocusRequester, modifier: Modifier) { Row(modifier, horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) { RedCinemaReel("−10", onBack); RedCinemaReel(if (playing) "PAUSE" else "PLAY", onToggle, Modifier.focusRequester(requester), true); RedCinemaReel("+10", onForward) } }

@Composable
private fun RedCinemaFilmStrip(position: Long, duration: Long, preview: SeekPreviewState, onSeek: (Long) -> Unit, onScrub: (Boolean) -> Unit, onPreview: (Long?) -> Unit, timeshift: PlayerTimeshiftUiState, modifier: Modifier) { val s = LocalThemePresentation.current.surfaces; var pending by remember { mutableFloatStateOf(0f) }; val safeDuration = duration.coerceAtLeast(0L); val current = if (safeDuration > 0) (position.toFloat() / safeDuration).coerceIn(0f, 1f) else 0f; Column(modifier.background(s.browseContent, RoundedCornerShape(2.dp)).padding(horizontal = 22.dp, vertical = 11.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("FILM STRIP", style = MaterialTheme.typography.labelSmall, color = s.accent); Text(if (timeshift.available) timeshift.statusMessage.ifBlank { "LIVE BUFFER" } else if (preview.visible) "FRAME ${(preview.positionMs / 1000L)}s" else "${position / 1000L}s", style = MaterialTheme.typography.labelSmall, color = s.textSecondary) }; if (safeDuration > 0) Slider(value = if (pending == 0f) current else pending, onValueChange = { value -> pending = value; onScrub(true); onPreview((value * safeDuration).toLong()) }, onValueChangeFinished = { val value = if (pending == 0f) current else pending; onSeek((value * safeDuration).toLong()); onPreview(null); onScrub(false); pending = 0f }, colors = SliderDefaults.colors(thumbColor = s.accent, activeTrackColor = s.accent, inactiveTrackColor = s.textSecondary.copy(alpha = .28f)), modifier = Modifier.fillMaxWidth()) } }

@Composable
private fun RedCinemaTicketWindow(actions: List<RedCinemaAction>, requester: FocusRequester, timers: SleepTimerUiState, isCasting: Boolean, onCast: () -> Unit, onStopCast: () -> Unit, modifier: Modifier) { val s = LocalThemePresentation.current.surfaces; Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("TICKET WINDOW", style = MaterialTheme.typography.labelMedium, color = s.accent); Text(when { isCasting -> "CASTING"; timers.stopTimerActive -> "STOP TIMER"; timers.idleTimerActive -> "IDLE TIMER"; else -> "INTERMISSION READY" }, style = MaterialTheme.typography.labelSmall, color = s.textSecondary) }; LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { itemsIndexed(actions, key = { index, action -> "red_cinema_${index}_${action.label}" }) { index, action -> RedCinemaStub(action, if (index == 0) Modifier.focusRequester(requester) else Modifier) }; item { RedCinemaStub(RedCinemaAction(if (isCasting) "STOP CAST" else "CAST", "output", if (isCasting) onStopCast else onCast)) } } } }

@Composable
private fun RedCinemaStub(action: RedCinemaAction, modifier: Modifier = Modifier) { val s = LocalThemePresentation.current.surfaces; val shape = RoundedCornerShape(2.dp); TvClickableSurface(onClick = action.onClick, modifier = modifier.width(138.dp), shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = s.browseContent, focusedContainerColor = s.focusedSurface, contentColor = s.textPrimary, focusedContentColor = s.textPrimary), border = ClickableSurfaceDefaults.border(border = Border(border = BorderStroke(1.dp, s.textSecondary.copy(alpha = .26f)), shape = shape), focusedBorder = Border(border = BorderStroke(2.dp, s.accent), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.018f)) { Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) { Text(action.label, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(action.detail, style = MaterialTheme.typography.labelSmall, color = s.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis) } } }

@Composable
private fun RedCinemaReel(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, primary: Boolean = false) { val s = LocalThemePresentation.current.surfaces; TvClickableSurface(onClick = onClick, modifier = modifier.width(if (primary) 122.dp else 86.dp), shape = ClickableSurfaceDefaults.shape(CircleShape), colors = ClickableSurfaceDefaults.colors(containerColor = if (primary) s.selectedAccent else s.browseContent, focusedContainerColor = s.focusedSurface, contentColor = s.textPrimary, focusedContentColor = s.textPrimary), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, s.accent), shape = CircleShape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.04f)) { Text(label, Modifier.padding(vertical = 18.dp), style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center) } }
