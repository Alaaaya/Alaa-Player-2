package com.streamvault.app.ui.themes.goldnight

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.outlined.FavoriteBorder
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Glow
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.app.ui.screens.player.PlayerTimeshiftUiState
import com.streamvault.app.ui.screens.player.SeekPreviewState
import com.streamvault.app.ui.screens.player.SleepTimerUiState
import com.streamvault.domain.model.Channel
import com.streamvault.domain.model.Program
import com.streamvault.domain.model.RecordingStatus

private data class GoldNightPlayerAction(val label: String, val onClick: () -> Unit)

/**
 * Gold Night fullscreen player: فيديو بملء الشاشة، عمود قنوات عمودي شفاف يساراً (للبث المباشر)،
 * وشريط سفلي خاص: شعار، البرنامج، الوقت، تقدم ذهبي، البرنامج التالي، وأيقونات الصوت/الترجمة/المفضلة/الأبعاد.
 * رف إجراءات ثانوي يُفتح من «More» ويصل إلى بقية أوامر المشغّل المشتركة (تسجيل، جدولة، سرعة، مؤقتات، PiP...).
 */
@Composable
internal fun GoldNightPlayerOverlay(
    visible: Boolean, title: String, contentType: String, isCatchUpPlayback: Boolean, isPlaying: Boolean,
    currentProgram: Program?, nextProgram: Program?, currentChannel: Channel?, currentChannelName: String?, displayChannelNumber: Int,
    liveChannels: List<Channel>, currentPosition: Long, duration: Long, aspectRatioLabel: String, subtitleTrackCount: Int,
    liveTranslationAvailable: Boolean, audioTrackCount: Int, videoQualityCount: Int, currentRecordingStatus: RecordingStatus?,
    isMuted: Boolean, playbackSpeed: Float, mediaTitle: String?, sleepTimerUiState: SleepTimerUiState,
    timeshiftUiState: PlayerTimeshiftUiState, playButtonFocusRequester: FocusRequester, quickActionsFocusRequester: FocusRequester,
    modifier: Modifier = Modifier, onClose: () -> Unit, onTogglePlayPause: () -> Unit, onSeekBackward: () -> Unit,
    onSeekForward: () -> Unit, onRestartProgram: () -> Unit, onOpenArchive: () -> Unit, onStartRecording: () -> Unit,
    onStopRecording: () -> Unit, onScheduleRecording: () -> Unit, onScheduleDailyRecording: () -> Unit,
    onScheduleWeeklyRecording: () -> Unit, onToggleAspectRatio: () -> Unit, onOpenSubtitleTracks: () -> Unit,
    onOpenAudioTracks: () -> Unit, onOpenVideoTracks: () -> Unit, onOpenPlaybackSpeed: () -> Unit,
    onOpenStopPlaybackTimer: () -> Unit, onOpenIdleStandbyTimer: () -> Unit, onOpenAudioVideoSync: () -> Unit,
    audioVideoSyncEnabled: Boolean, showEpisodesAction: Boolean, onOpenEpisodes: () -> Unit,
    onOpenLiveChannels: () -> Unit, onOpenLiveGuide: () -> Unit, onToggleLiveFavorite: () -> Unit, onZapToChannel: (Long) -> Unit,
    onOpenSplitScreen: () -> Unit, onEnterPictureInPicture: () -> Unit, onToggleMute: () -> Unit, isCastConnected: Boolean,
    onCast: () -> Unit, onStopCasting: () -> Unit, onSeekToLiveEdge: () -> Unit, onSeekToPosition: (Long) -> Unit,
    onSetScrubbingMode: (Boolean) -> Unit, seekPreview: SeekPreviewState, onSeekPreviewPositionChanged: (Long?) -> Unit,
    onUserInteraction: () -> Unit
) {
    val isLive = contentType == "LIVE" && !isCatchUpPlayback
    val vod = !isLive
    val displayTitle = currentProgram?.title?.takeIf { contentType == "LIVE" } ?: mediaTitle?.takeIf { it.isNotBlank() } ?: title
    var showMore by remember { mutableStateOf(false) }
    val moreActions = remember(
        vod, contentType, currentProgram, timeshiftUiState.available, isMuted, playbackSpeed, videoQualityCount,
        currentRecordingStatus, showEpisodesAction, audioVideoSyncEnabled, isCastConnected
    ) {
        buildList {
            if (contentType == "LIVE" && currentProgram != null) {
                add(GoldNightPlayerAction("Restart", onRestartProgram))
                add(GoldNightPlayerAction("Archive", onOpenArchive))
            }
            if (timeshiftUiState.available) add(GoldNightPlayerAction("Live edge", onSeekToLiveEdge))
            if (contentType == "LIVE") {
                add(GoldNightPlayerAction("Channels", onOpenLiveChannels))
                add(GoldNightPlayerAction("Guide", onOpenLiveGuide))
            }
            if (vod) add(GoldNightPlayerAction("Speed ${playbackSpeed}x", onOpenPlaybackSpeed))
            if (showEpisodesAction) add(GoldNightPlayerAction("Episodes", onOpenEpisodes))
            if (videoQualityCount > 0) add(GoldNightPlayerAction("Quality", onOpenVideoTracks))
            add(GoldNightPlayerAction(if (isMuted) "Unmute" else "Mute", onToggleMute))
            if (currentRecordingStatus == RecordingStatus.RECORDING) {
                add(GoldNightPlayerAction("Stop recording", onStopRecording))
            } else if (contentType == "LIVE") {
                add(GoldNightPlayerAction("Record", onStartRecording))
                add(GoldNightPlayerAction("Schedule", onScheduleRecording))
                add(GoldNightPlayerAction("Daily", onScheduleDailyRecording))
                add(GoldNightPlayerAction("Weekly", onScheduleWeeklyRecording))
            }
            add(GoldNightPlayerAction("Stop timer", onOpenStopPlaybackTimer))
            add(GoldNightPlayerAction("Idle timer", onOpenIdleStandbyTimer))
            if (audioVideoSyncEnabled && !isCastConnected) add(GoldNightPlayerAction("A/V sync", onOpenAudioVideoSync))
            add(GoldNightPlayerAction("Multiview", onOpenSplitScreen))
            add(GoldNightPlayerAction("Picture in picture", onEnterPictureInPicture))
            add(GoldNightPlayerAction(if (isCastConnected) "Stop cast" else "Cast", if (isCastConnected) onStopCasting else onCast))
            add(GoldNightPlayerAction("Exit", onClose))
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(GoldNightFocusMotionMs)),
        exit = fadeOut(tween(GoldNightFocusMotionMs)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onPreviewKeyEvent { event ->
                    if (event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN) onUserInteraction()
                    false
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, GoldNightCanvas.copy(alpha = 0.9f))))
            )
            GoldNightTopStatus(modifier = Modifier.align(Alignment.TopEnd).padding(top = 24.dp, end = 32.dp))
            if (isLive) {
                GoldNightPlayerChannelColumn(
                    channels = liveChannels,
                    currentChannel = currentChannel,
                    onZapToChannel = onZapToChannel,
                    onOpenLiveChannels = onOpenLiveChannels,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 24.dp, top = 24.dp, bottom = 200.dp)
                        .width(340.dp)
                        .fillMaxHeight()
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (showMore) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                    ) {
                        items(moreActions, key = { "gn_more_${it.label}" }) { action ->
                            GoldNightButton(label = action.label, onClick = action.onClick)
                        }
                    }
                }
                GoldNightPlayerBottomBar(
                    isLive = isLive,
                    isPlaying = isPlaying,
                    displayTitle = displayTitle,
                    channelName = currentChannelName ?: currentChannel?.name,
                    channelLogo = currentChannel?.logoUrl,
                    channelNumber = displayChannelNumber,
                    currentProgram = currentProgram,
                    nextProgram = nextProgram,
                    currentPosition = currentPosition,
                    duration = duration,
                    isFavorite = currentChannel?.isFavorite == true,
                    aspectRatioLabel = aspectRatioLabel,
                    hasSubtitles = subtitleTrackCount > 0 || liveTranslationAvailable,
                    hasAudioTracks = audioTrackCount > 0,
                    statusLine = when {
                        currentRecordingStatus == RecordingStatus.RECORDING -> "● REC"
                        sleepTimerUiState.stopTimerActive -> "Stop timer on"
                        sleepTimerUiState.idleTimerActive -> "Idle timer on"
                        timeshiftUiState.available -> timeshiftUiState.statusMessage
                        else -> ""
                    },
                    seekPreview = seekPreview,
                    playButtonFocusRequester = playButtonFocusRequester,
                    quickActionsFocusRequester = quickActionsFocusRequester,
                    showMore = showMore,
                    onToggleMore = { showMore = !showMore },
                    onTogglePlayPause = onTogglePlayPause,
                    onSeekBackward = onSeekBackward,
                    onSeekForward = onSeekForward,
                    onOpenAudioTracks = onOpenAudioTracks,
                    onOpenSubtitleTracks = onOpenSubtitleTracks,
                    onToggleFavorite = onToggleLiveFavorite,
                    onToggleAspectRatio = onToggleAspectRatio,
                    onSeekToPosition = onSeekToPosition,
                    onSetScrubbingMode = onSetScrubbingMode,
                    onSeekPreviewPositionChanged = onSeekPreviewPositionChanged
                )
            }
        }
    }
}

@Composable
private fun GoldNightPlayerChannelColumn(
    channels: List<Channel>,
    currentChannel: Channel?,
    onZapToChannel: (Long) -> Unit,
    onOpenLiveChannels: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentIndex = channels.indexOfFirst { it.id == currentChannel?.id }.coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = (currentIndex - 2).coerceAtLeast(0))
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(GoldNightCanvas.copy(alpha = 0.72f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = currentChannel?.categoryName ?: "Channels",
            style = MaterialTheme.typography.titleMedium,
            color = GoldNightGold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (channels.isEmpty()) {
            GoldNightButton(label = "Show channels", onClick = onOpenLiveChannels, modifier = Modifier.fillMaxWidth())
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(channels, key = { "gn_player_ch_${it.id}" }) { channel ->
                    val isCurrent = channel.id == currentChannel?.id
                    GoldNightCard(
                        onClick = { if (!isCurrent) onZapToChannel(channel.id) },
                        selected = isCurrent,
                        containerColor = GoldNightPanel.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (channel.number > 0) channel.number.toString() else "–",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isCurrent) GoldNightGold else GoldNightMuted,
                                modifier = Modifier.width(32.dp)
                            )
                            GoldNightRoundLogo(channelName = channel.name, logoUrl = channel.logoUrl, size = 34.dp)
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(text = channel.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                GoldNightProgressBar(progress = goldNightProgramProgress(channel.currentProgram), modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GoldNightPlayerBottomBar(
    isLive: Boolean,
    isPlaying: Boolean,
    displayTitle: String,
    channelName: String?,
    channelLogo: String?,
    channelNumber: Int,
    currentProgram: Program?,
    nextProgram: Program?,
    currentPosition: Long,
    duration: Long,
    isFavorite: Boolean,
    aspectRatioLabel: String,
    hasSubtitles: Boolean,
    hasAudioTracks: Boolean,
    statusLine: String,
    seekPreview: SeekPreviewState,
    playButtonFocusRequester: FocusRequester,
    quickActionsFocusRequester: FocusRequester,
    showMore: Boolean,
    onToggleMore: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekBackward: () -> Unit,
    onSeekForward: () -> Unit,
    onOpenAudioTracks: () -> Unit,
    onOpenSubtitleTracks: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleAspectRatio: () -> Unit,
    onSeekToPosition: (Long) -> Unit,
    onSetScrubbingMode: (Boolean) -> Unit,
    onSeekPreviewPositionChanged: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    var pending by remember { mutableFloatStateOf(-1f) }
    val safeDuration = duration.coerceAtLeast(0L)
    val vodProgress = if (safeDuration > 0) (currentPosition.toFloat() / safeDuration).coerceIn(0f, 1f) else 0f
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GoldNightPanel.copy(alpha = 0.92f))
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLive) {
                GoldNightRoundLogo(channelName = channelName.orEmpty(), logoUrl = channelLogo, size = 56.dp)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (isLive && channelNumber > 0) {
                        Text(text = channelNumber.toString(), style = MaterialTheme.typography.titleMedium, color = GoldNightGold)
                    }
                    if (isLive && !channelName.isNullOrBlank()) {
                        Text(text = channelName.orEmpty(), style = MaterialTheme.typography.titleMedium, color = GoldNightMuted, maxLines = 1)
                    }
                    if (statusLine.isNotBlank()) {
                        Text(text = statusLine, style = MaterialTheme.typography.labelMedium, color = GoldNightGold, maxLines = 1)
                    }
                }
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    color = GoldNightText,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isLive) {
                        goldNightProgramWindow(currentProgram)
                    } else if (safeDuration > 0) {
                        "${goldNightTimecode(if (seekPreview.visible) seekPreview.positionMs else currentPosition)} / ${goldNightTimecode(safeDuration)}"
                    } else "",
                    style = MaterialTheme.typography.labelLarge,
                    color = GoldNightMuted
                )
            }
            if (isLive && nextProgram != null) {
                Column(modifier = Modifier.width(260.dp), horizontalAlignment = Alignment.End) {
                    Text(text = "Next · ${goldNightClock(nextProgram.startTime)}", style = MaterialTheme.typography.labelMedium, color = GoldNightGold)
                    Text(
                        text = nextProgram.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = GoldNightText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        if (isLive || safeDuration <= 0) {
            GoldNightProgressBar(progress = goldNightProgramProgress(currentProgram), modifier = Modifier.fillMaxWidth(), height = 5.dp)
        } else {
            Slider(
                value = if (pending < 0f) vodProgress else pending,
                onValueChange = { value ->
                    pending = value
                    onSetScrubbingMode(true)
                    onSeekPreviewPositionChanged((value * safeDuration).toLong())
                },
                onValueChangeFinished = {
                    val value = if (pending < 0f) vodProgress else pending
                    onSeekToPosition((value * safeDuration).toLong())
                    onSeekPreviewPositionChanged(null)
                    onSetScrubbingMode(false)
                    pending = -1f
                },
                colors = SliderDefaults.colors(
                    thumbColor = GoldNightGold,
                    activeTrackColor = GoldNightGold,
                    inactiveTrackColor = GoldNightRule
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isLive) GoldNightPlayerIcon(icon = Icons.Default.Replay10, label = "Back 10 seconds", onClick = onSeekBackward)
            GoldNightPlayerIcon(
                icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                label = if (isPlaying) "Pause" else "Play",
                onClick = onTogglePlayPause,
                primary = true,
                modifier = Modifier.focusRequester(playButtonFocusRequester)
            )
            if (!isLive) GoldNightPlayerIcon(icon = Icons.Default.Forward10, label = "Forward 10 seconds", onClick = onSeekForward)
            Spacer(Modifier.weight(1f))
            GoldNightPlayerIcon(
                icon = Icons.Default.MusicNote,
                label = "Audio",
                onClick = onOpenAudioTracks,
                enabled = hasAudioTracks,
                modifier = Modifier.focusRequester(quickActionsFocusRequester)
            )
            GoldNightPlayerIcon(icon = Icons.Default.ClosedCaption, label = "Subtitles", onClick = onOpenSubtitleTracks, enabled = hasSubtitles)
            if (isLive) {
                GoldNightPlayerIcon(
                    icon = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    label = if (isFavorite) "Remove favourite" else "Add favourite",
                    onClick = onToggleFavorite,
                    tint = if (isFavorite) GoldNightGold else null
                )
            }
            GoldNightPlayerIcon(icon = Icons.Default.AspectRatio, label = "Aspect $aspectRatioLabel", onClick = onToggleAspectRatio)
            Text(text = aspectRatioLabel, style = MaterialTheme.typography.labelSmall, color = GoldNightMuted, maxLines = 1)
            GoldNightPlayerIcon(icon = Icons.Default.MoreVert, label = if (showMore) "Hide actions" else "More actions", onClick = onToggleMore, primary = showMore)
        }
    }
}

@Composable
private fun GoldNightPlayerIcon(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    enabled: Boolean = true,
    tint: Color? = null
) {
    TvClickableSurface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(52.dp),
        shape = ClickableSurfaceDefaults.shape(CircleShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (primary) GoldNightGold else GoldNightCanvas.copy(alpha = 0.6f),
            focusedContainerColor = if (primary) GoldNightGoldBright else GoldNightPanelFocused,
            contentColor = if (primary) GoldNightOnGold else GoldNightText,
            focusedContentColor = if (primary) GoldNightOnGold else GoldNightGold,
            disabledContainerColor = GoldNightCanvas.copy(alpha = 0.4f),
            disabledContentColor = GoldNightMuted.copy(alpha = 0.5f)
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(border = BorderStroke(GoldNightFocusBorderWidth, GoldNightGold), shape = CircleShape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
        glow = ClickableSurfaceDefaults.glow(
            focusedGlow = Glow(elevationColor = GoldNightGold.copy(alpha = 0.5f), elevation = 10.dp)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint ?: androidx.tv.material3.LocalContentColor.current,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

private fun goldNightTimecode(positionMs: Long): String {
    val totalSeconds = (positionMs / 1000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    val mm = minutes.toString().padStart(2, '0')
    val ss = seconds.toString().padStart(2, '0')
    return if (hours > 0) "$hours:$mm:$ss" else "$mm:$ss"
}
