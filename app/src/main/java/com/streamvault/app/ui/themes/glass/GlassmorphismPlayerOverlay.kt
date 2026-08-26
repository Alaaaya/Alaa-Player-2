package com.streamvault.app.ui.themes.glass

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
import androidx.compose.foundation.layout.height
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

private data class GlassPlayerAction(val label: String, val glyph: String, val onClick: () -> Unit)

private enum class GlassPlayerDeck(val railLabel: String, val glyph: String) {
    WATCH("WATCH", "▶"),
    STREAM("STREAM", "◈"),
    SESSION("SESSION", "◌")
}

/**
 * مشغل Glassmorphism ملء الشاشة: rail زجاجي عمودي يسار لاختيار طبقات الإجراءات،
 * وشريط إجراءات سفلي عائم؛ لا يعيد استخدام شريط Minimal النصي أو كبسولات Neon.
 */
@Composable
internal fun GlassmorphismPlayerOverlay(
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
    var activeDeck by remember { mutableStateOf(GlassPlayerDeck.WATCH) }
    val actions = remember(
        activeDeck,
        isVod,
        contentType,
        currentProgram,
        timeshiftUiState.available,
        isMuted,
        playbackSpeed,
        subtitleTrackCount,
        liveTranslationAvailable,
        audioTrackCount,
        videoQualityCount,
        currentRecordingStatus,
        showEpisodesAction,
        audioVideoSyncEnabled,
        isCastConnected
    ) {
        when (activeDeck) {
            GlassPlayerDeck.WATCH -> buildList {
                if (contentType == "LIVE" && currentProgram != null) add(GlassPlayerAction("Restart", "↺", onRestartProgram))
                if (contentType == "LIVE" && currentProgram != null) add(GlassPlayerAction("Archive", "◷", onOpenArchive))
                if (timeshiftUiState.available) add(GlassPlayerAction("Live edge", "●", onSeekToLiveEdge))
                if (isVod) add(GlassPlayerAction("Speed ${playbackSpeed}×", "⏩", onOpenPlaybackSpeed))
                if (showEpisodesAction) add(GlassPlayerAction("Episodes", "≡", onOpenEpisodes))
                add(GlassPlayerAction(if (isMuted) "Unmute" else "Mute", "⌁", onToggleMute))
            }
            GlassPlayerDeck.STREAM -> buildList {
                add(GlassPlayerAction("Display $aspectRatioLabel", "▣", onToggleAspectRatio))
                if (subtitleTrackCount > 0 || liveTranslationAvailable) add(GlassPlayerAction("Subtitles", "CC", onOpenSubtitleTracks))
                if (audioTrackCount > 0) add(GlassPlayerAction("Audio", "♪", onOpenAudioTracks))
                if (videoQualityCount > 0) add(GlassPlayerAction("Quality", "HD", onOpenVideoTracks))
                add(GlassPlayerAction(if (isCastConnected) "Stop cast" else "Cast", "◌", if (isCastConnected) onStopCasting else onCast))
                add(GlassPlayerAction("Picture in picture", "▧", onEnterPictureInPicture))
            }
            GlassPlayerDeck.SESSION -> buildList {
                if (currentRecordingStatus == RecordingStatus.RECORDING) {
                    add(GlassPlayerAction("Stop recording", "■", onStopRecording))
                } else if (contentType == "LIVE") {
                    add(GlassPlayerAction("Record", "●", onStartRecording))
                    add(GlassPlayerAction("Schedule", "◴", onScheduleRecording))
                    add(GlassPlayerAction("Daily schedule", "D", onScheduleDailyRecording))
                    add(GlassPlayerAction("Weekly schedule", "W", onScheduleWeeklyRecording))
                }
                add(GlassPlayerAction("Stop timer", "⌛", onOpenStopPlaybackTimer))
                add(GlassPlayerAction("Idle timer", "◌", onOpenIdleStandbyTimer))
                if (audioVideoSyncEnabled && !isCastConnected) add(GlassPlayerAction("A/V sync", "±", onOpenAudioVideoSync))
                add(GlassPlayerAction("Multiview", "▦", onOpenSplitScreen))
            }
        } + GlassPlayerAction("Close", "×", onClose)
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(GlassFocusMotionMs)),
        exit = fadeOut(tween(GlassFocusMotionMs)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GlassCanvas.copy(alpha = 0.46f))
                .onPreviewKeyEvent { event ->
                    if (event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN) onUserInteraction()
                    false
                }
        ) {
            GlassVerticalActionRail(
                activeDeck = activeDeck,
                onDeckSelected = { activeDeck = it },
                onClose = onClose,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 24.dp)
            )
            Column(
                modifier = Modifier.align(Alignment.TopStart).padding(start = 126.dp, top = 30.dp).width(520.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("GLASS PLAYER / ${if (isVod) "VOD" else "LIVE ${displayChannelNumber.toString().padStart(3, '0')}"}", style = MaterialTheme.typography.labelMedium, color = GlassAccent)
                Text(displayTitle, style = MaterialTheme.typography.titleLarge, color = GlassText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(currentChannelName ?: currentChannel?.name.orEmpty(), style = MaterialTheme.typography.bodySmall, color = GlassMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    when {
                        currentRecordingStatus == RecordingStatus.RECORDING -> "RECORDING ACTIVE"
                        sleepTimerUiState.stopTimerActive -> "STOP TIMER ACTIVE"
                        sleepTimerUiState.idleTimerActive -> "IDLE TIMER ACTIVE"
                        timeshiftUiState.available -> timeshiftUiState.statusMessage.ifBlank { "LIVE BUFFER READY" }
                        else -> "GLASS CONTROL LAYER"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = GlassMuted
                )
            }
            GlassCentralTransport(
                isPlaying = isPlaying,
                currentPosition = currentPosition,
                duration = duration,
                seekPreview = seekPreview,
                playButtonFocusRequester = playButtonFocusRequester,
                onSeekBackward = onSeekBackward,
                onTogglePlayPause = onTogglePlayPause,
                onSeekForward = onSeekForward,
                onSeekToPosition = onSeekToPosition,
                onSetScrubbingMode = onSetScrubbingMode,
                onSeekPreviewPositionChanged = onSeekPreviewPositionChanged,
                modifier = Modifier.align(Alignment.Center)
            )
            GlassBottomActionBar(
                activeDeck = activeDeck,
                actions = actions,
                focusRequester = quickActionsFocusRequester,
                modifier = Modifier.align(Alignment.BottomCenter).padding(start = 120.dp, end = 34.dp, bottom = 26.dp)
            )
        }
    }
}

@Composable
private fun GlassVerticalActionRail(activeDeck: GlassPlayerDeck, onDeckSelected: (GlassPlayerDeck) -> Unit, onClose: () -> Unit, modifier: Modifier) {
    Column(
        modifier = modifier.background(GlassPane, RoundedCornerShape(25.dp)).padding(horizontal = 9.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GlassRailButton("×", "EXIT", onClick = onClose, selected = false)
        GlassPlayerDeck.entries.forEach { deck ->
            GlassRailButton(deck.glyph, deck.railLabel, { onDeckSelected(deck) }, selected = deck == activeDeck)
        }
    }
}

@Composable
private fun GlassRailButton(glyph: String, label: String, onClick: () -> Unit, selected: Boolean, onLongClick: (() -> Unit)? = null) {
    val shape = RoundedCornerShape(17.dp)
    TvClickableSurface(
        onClick = onClick,
        onLongClick = onLongClick,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) GlassPaneFocused else GlassPane,
            focusedContainerColor = GlassPaneFocused,
            contentColor = if (selected) GlassFocus else GlassText,
            focusedContentColor = GlassFocus
        ),
        border = ClickableSurfaceDefaults.border(
            border = Border(border = BorderStroke(1.dp, if (selected) GlassAccent else GlassRule), shape = shape),
            focusedBorder = Border(border = BorderStroke(2.dp, GlassFocus), shape = shape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.06f)
    ) {
        Column(Modifier.width(64.dp).padding(vertical = 9.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(glyph, style = MaterialTheme.typography.titleMedium)
            Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun GlassCentralTransport(
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
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
    var pendingSeekFraction by remember { mutableFloatStateOf(0f) }
    val resolvedDuration = duration.coerceAtLeast(0L)
    val currentFraction = if (resolvedDuration > 0L) (currentPosition.toFloat() / resolvedDuration.toFloat()).coerceIn(0f, 1f) else 0f
    Column(
        modifier = modifier.width(500.dp).background(GlassPane, RoundedCornerShape(28.dp)).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            GlassTransportButton("−10", onSeekBackward)
            GlassTransportButton(if (isPlaying) "Ⅱ" else "▶", onTogglePlayPause, Modifier.focusRequester(playButtonFocusRequester), primary = true)
            GlassTransportButton("+10", onSeekForward)
        }
        if (resolvedDuration > 0L) {
            Slider(
                value = if (pendingSeekFraction == 0f) currentFraction else pendingSeekFraction,
                onValueChange = { fraction ->
                    pendingSeekFraction = fraction
                    onSetScrubbingMode(true)
                    onSeekPreviewPositionChanged((fraction * resolvedDuration).toLong())
                },
                onValueChangeFinished = {
                    val fraction = if (pendingSeekFraction == 0f) currentFraction else pendingSeekFraction
                    onSeekToPosition((fraction * resolvedDuration).toLong())
                    onSeekPreviewPositionChanged(null)
                    onSetScrubbingMode(false)
                    pendingSeekFraction = 0f
                },
                colors = SliderDefaults.colors(thumbColor = GlassFocus, activeTrackColor = GlassAccent, inactiveTrackColor = GlassRule),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Text(
            if (seekPreview.visible) "SEEK PREVIEW / ${(seekPreview.positionMs / 1_000L)}s" else "TRANSPORT / ${(currentPosition / 1_000L)}s",
            style = MaterialTheme.typography.labelSmall,
            color = GlassMuted
        )
    }
}

@Composable
private fun GlassBottomActionBar(activeDeck: GlassPlayerDeck, actions: List<GlassPlayerAction>, focusRequester: FocusRequester, modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().background(GlassPane.copy(alpha = .92f), RoundedCornerShape(26.dp)).padding(13.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("ACTION DECK", style = MaterialTheme.typography.labelSmall, color = GlassMuted)
            Text(" / ${activeDeck.railLabel}", style = MaterialTheme.typography.labelSmall, color = GlassAccent)
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            itemsIndexed(actions, key = { _, action -> "${activeDeck.name}_${action.label}" }) { index, action ->
                GlassBottomAction(
                    action = action,
                    modifier = if (index == 0) Modifier.focusRequester(focusRequester) else Modifier
                )
            }
        }
    }
}

@Composable
private fun GlassBottomAction(action: GlassPlayerAction, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(19.dp)
    TvClickableSurface(
        onClick = action.onClick,
        modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = GlassCanvasDeep, focusedContainerColor = GlassPaneFocused, contentColor = GlassText, focusedContentColor = GlassFocus),
        border = ClickableSurfaceDefaults.border(
            border = Border(border = BorderStroke(1.dp, GlassRule), shape = shape),
            focusedBorder = Border(border = BorderStroke(2.dp, GlassFocus), shape = shape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.045f)
    ) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 11.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(action.glyph, style = MaterialTheme.typography.titleSmall, color = GlassAccent)
            Text(action.label, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun GlassTransportButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, primary: Boolean = false) {
    val shape = RoundedCornerShape(50)
    TvClickableSurface(
        onClick = onClick,
        modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = if (primary) GlassPaneFocused else GlassPane, focusedContainerColor = GlassPaneFocused, contentColor = GlassText, focusedContentColor = GlassFocus),
        border = ClickableSurfaceDefaults.border(
            border = Border(border = BorderStroke(1.dp, if (primary) GlassAccent else GlassRule), shape = shape),
            focusedBorder = Border(border = BorderStroke(2.dp, GlassFocus), shape = shape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f)
    ) {
        Text(label, Modifier.padding(horizontal = if (primary) 24.dp else 18.dp, vertical = 14.dp), style = MaterialTheme.typography.titleMedium)
    }
}
