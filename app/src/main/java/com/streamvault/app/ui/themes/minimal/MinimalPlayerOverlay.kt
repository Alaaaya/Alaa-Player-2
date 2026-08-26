package com.streamvault.app.ui.themes.minimal

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
import androidx.compose.ui.graphics.Color
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

private data class MinimalPlayerAction(val label: String, val onClick: () -> Unit)

private enum class MinimalPlayerCommandPage(val label: String) {
    PLAYBACK("PLAYBACK"),
    OPTIONS("OPTIONS"),
    SESSION("SESSION")
}

/**
 * مشغل Minimal لا يعيد استخدام شريط Neon أو Cinematic: النقل يبقى في المنتصف،
 * بينما يظهر في الأسفل فهرس أوامر نصي ذو صفحات، بلا كبسولات أو تكبير تركيز.
 */
@Composable
internal fun MinimalPlayerOverlay(
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
    var commandPage by remember { mutableStateOf(MinimalPlayerCommandPage.PLAYBACK) }
    val pageActions = remember(
        commandPage,
        isVod,
        currentProgram,
        isMuted,
        isCastConnected,
        aspectRatioLabel,
        subtitleTrackCount,
        liveTranslationAvailable,
        audioTrackCount,
        videoQualityCount,
        playbackSpeed,
        currentRecordingStatus,
        showEpisodesAction,
        timeshiftUiState.available,
        audioVideoSyncEnabled
    ) {
        when (commandPage) {
            MinimalPlayerCommandPage.PLAYBACK -> buildList {
                if (contentType == "LIVE" && currentProgram != null) add(MinimalPlayerAction("Restart", onRestartProgram))
                if (contentType == "LIVE" && currentProgram != null) add(MinimalPlayerAction("Archive", onOpenArchive))
                if (timeshiftUiState.available) add(MinimalPlayerAction("Live edge", onSeekToLiveEdge))
                if (isVod) add(MinimalPlayerAction("Speed ${playbackSpeed}×", onOpenPlaybackSpeed))
                if (showEpisodesAction) add(MinimalPlayerAction("Episodes", onOpenEpisodes))
                add(MinimalPlayerAction(if (isMuted) "Unmute" else "Mute", onToggleMute))
                add(MinimalPlayerAction("Options", { commandPage = MinimalPlayerCommandPage.OPTIONS }))
            }
            MinimalPlayerCommandPage.OPTIONS -> buildList {
                add(MinimalPlayerAction("Display $aspectRatioLabel", onToggleAspectRatio))
                if (subtitleTrackCount > 0 || liveTranslationAvailable) add(MinimalPlayerAction("Subtitles", onOpenSubtitleTracks))
                if (audioTrackCount > 0) add(MinimalPlayerAction("Audio", onOpenAudioTracks))
                if (videoQualityCount > 0) add(MinimalPlayerAction("Quality", onOpenVideoTracks))
                add(MinimalPlayerAction(if (isCastConnected) "Stop cast" else "Cast", if (isCastConnected) onStopCasting else onCast))
                add(MinimalPlayerAction("PiP", onEnterPictureInPicture))
                add(MinimalPlayerAction("Session", { commandPage = MinimalPlayerCommandPage.SESSION }))
                add(MinimalPlayerAction("Playback", { commandPage = MinimalPlayerCommandPage.PLAYBACK }))
            }
            MinimalPlayerCommandPage.SESSION -> buildList {
                if (currentRecordingStatus == RecordingStatus.RECORDING) {
                    add(MinimalPlayerAction("Stop recording", onStopRecording))
                } else if (contentType == "LIVE") {
                    add(MinimalPlayerAction("Record", onStartRecording))
                    add(MinimalPlayerAction("Schedule", onScheduleRecording))
                    add(MinimalPlayerAction("Daily schedule", onScheduleDailyRecording))
                    add(MinimalPlayerAction("Weekly schedule", onScheduleWeeklyRecording))
                }
                add(MinimalPlayerAction("Stop timer", onOpenStopPlaybackTimer))
                add(MinimalPlayerAction("Idle timer", onOpenIdleStandbyTimer))
                if (audioVideoSyncEnabled && !isCastConnected) add(MinimalPlayerAction("A/V sync", onOpenAudioVideoSync))
                add(MinimalPlayerAction("Multiview", onOpenSplitScreen))
                add(MinimalPlayerAction("Playback", { commandPage = MinimalPlayerCommandPage.PLAYBACK }))
            }
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(MinimalFocusMotionMs)),
        exit = fadeOut(tween(MinimalFocusMotionMs)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = .38f))
                .onPreviewKeyEvent { event ->
                    if (event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN) onUserInteraction()
                    false
                }
        ) {
            Column(
                modifier = Modifier.align(Alignment.TopStart).padding(30.dp).width(520.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${if (isVod) "PLAYBACK" else "LIVE"} / ${displayChannelNumber.toString().padStart(3, '0')}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MinimalMuted
                )
                Text(displayTitle, style = MaterialTheme.typography.titleLarge, color = MinimalText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(currentChannelName ?: currentChannel?.name.orEmpty(), style = MaterialTheme.typography.bodySmall, color = MinimalMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = when {
                        currentRecordingStatus == RecordingStatus.RECORDING -> "RECORDING ACTIVE"
                        sleepTimerUiState.stopTimerActive -> "STOP TIMER ACTIVE"
                        sleepTimerUiState.idleTimerActive -> "IDLE TIMER ACTIVE"
                        timeshiftUiState.available -> timeshiftUiState.statusMessage.ifBlank { "LIVE BUFFER READY" }
                        else -> "DPAD COMMAND MODE"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MinimalMuted
                )
            }

            MinimalCentralTransport(
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

            MinimalBottomActionStrip(
                page = commandPage,
                actions = pageActions + MinimalPlayerAction("Close", onClose),
                focusRequester = quickActionsFocusRequester,
                modifier = Modifier.align(Alignment.BottomCenter).padding(start = 34.dp, end = 34.dp, bottom = 26.dp)
            )
        }
    }
}

@Composable
private fun MinimalCentralTransport(
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
    modifier: Modifier = Modifier
) {
    var pendingSeekFraction by remember { mutableFloatStateOf(0f) }
    val resolvedDuration = duration.coerceAtLeast(0L)
    val currentFraction = if (resolvedDuration > 0L) (currentPosition.toFloat() / resolvedDuration.toFloat()).coerceIn(0f, 1f) else 0f
    Column(modifier = modifier.width(480.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            MinimalPlayerButton("−10", onSeekBackward)
            MinimalPlayerButton(if (isPlaying) "Pause" else "Play", onTogglePlayPause, Modifier.focusRequester(playButtonFocusRequester))
            MinimalPlayerButton("+10", onSeekForward)
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
                    val position = ((if (pendingSeekFraction == 0f) currentFraction else pendingSeekFraction) * resolvedDuration).toLong()
                    onSeekToPosition(position)
                    onSeekPreviewPositionChanged(null)
                    onSetScrubbingMode(false)
                    pendingSeekFraction = 0f
                },
                colors = SliderDefaults.colors(
                    thumbColor = MinimalText,
                    activeTrackColor = MinimalText.copy(alpha = .55f),
                    inactiveTrackColor = MinimalRule
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Text(
            text = if (seekPreview.visible) "SEEK PREVIEW / ${(seekPreview.positionMs / 1_000L)}s" else "TRANSPORT / ${(currentPosition / 1_000L)}s",
            style = MaterialTheme.typography.labelSmall,
            color = MinimalMuted
        )
    }
}

/** Fullscreen-only Bottom Action Bar: a textual index with pages, never shared capsules or tiles. */
@Composable
private fun MinimalBottomActionStrip(
    page: MinimalPlayerCommandPage,
    actions: List<MinimalPlayerAction>,
    focusRequester: FocusRequester,
    modifier: Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text("COMMAND INDEX / ${page.label}", style = MaterialTheme.typography.labelSmall, color = MinimalMuted)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            itemsIndexed(actions, key = { _, action -> action.label }) { index, action ->
                MinimalPlayerButton(
                    label = action.label,
                    onClick = action.onClick,
                    modifier = if (index == 0) Modifier.focusRequester(focusRequester) else Modifier
                )
            }
        }
    }
}

@Composable
private fun MinimalPlayerButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(0.dp)
    TvClickableSurface(
        onClick = onClick,
        modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = MinimalPaper.copy(alpha = .88f),
            contentColor = MinimalText,
            focusedContentColor = MinimalText
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(border = BorderStroke(1.dp, MinimalFocus), shape = shape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = MinimalFocusedScale)
    ) {
        Text("[ ${label.uppercase()} ]", modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), style = MaterialTheme.typography.labelMedium, maxLines = 1)
    }
}
