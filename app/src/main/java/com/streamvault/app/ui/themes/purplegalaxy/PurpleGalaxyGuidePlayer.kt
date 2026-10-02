package com.streamvault.app.ui.themes.purplegalaxy

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.streamvault.app.ui.components.PlayerRenderView
import com.streamvault.app.ui.screens.epg.EpgGrid
import com.streamvault.app.ui.themes.bespoke.EpgParams
import com.streamvault.app.ui.themes.bespoke.PlayerOverlayParams
import com.streamvault.domain.model.RecordingStatus
import com.streamvault.player.PlayerSurfaceResizeMode

/** Star map guide: telescope viewport and program log on top, transparent grid over the starfield. */
@Composable
internal fun PurpleGalaxyEpg(p: EpgParams, modifier: Modifier) {
    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(Modifier.fillMaxWidth().height(170.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            Box(Modifier.width(300.dp).fillMaxSize().clip(PG.Panel).background(PG.Deep)) {
                val engine = p.previewPlayerEngine
                if (engine != null) PlayerRenderView(engine, PlayerSurfaceResizeMode.FIT, Modifier.fillMaxSize())
                if (p.isPreviewLoading) Text("Aligning…", color = PG.Dust, modifier = Modifier.align(Alignment.Center))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                GalaxyLabel("Star map · ${p.selectedCategoryName}")
                val prog = p.focusedProgram
                GalaxyTitle(prog?.title ?: p.focusedChannel?.name ?: "Navigate the map", size = 24)
                prog?.let {
                    Text("${p.focusedChannel?.name.orEmpty()}  ·  ${formatClock(it.startTime)} – ${formatClock(it.endTime)}", color = PG.Comet, fontSize = 13.sp)
                    Text(it.description, color = PG.Dust, fontSize = 13.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                }
                if (p.isRefreshing) Text("Refreshing star data…", color = PG.Muted, fontSize = 12.sp)
            }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { GalaxyChip("✦ ${p.selectedCategoryName}", true, { p.onGuideInteract(); p.onOpenCategoryPicker() }) }
            item { GalaxyChip("Now", false, { p.onGuideInteract(); p.onJumpToNow() }) }
            item { GalaxyChip("Scan", false, { p.onGuideInteract(); p.onOpenSearch() }) }
            item { GalaxyChip("Options", false, { p.onGuideInteract(); p.onOpenOptions() }) }
        }
        Box(Modifier.weight(1f).fillMaxWidth().clip(PG.Panel).background(PG.Deep.copy(alpha = 0.55f))) {
            EpgGrid(
                modifier = Modifier.fillMaxSize(),
                channels = p.channels,
                favoriteChannelIds = p.favoriteChannelIds,
                programsByChannel = p.programsByChannel,
                guideWindowStart = p.guideWindowStart,
                guideWindowEnd = p.guideWindowEnd,
                density = p.density,
                transparentOverlay = true,
                onChannelClick = p.onChannelClick,
                onChannelLongClick = p.onChannelLongClick,
                onProgramClick = p.onProgramClick,
                onChannelFocused = p.onChannelFocused,
                onProgramFocused = p.onProgramFocused,
                onRequestMoreChannels = p.onRequestMoreChannels
            )
        }
    }
}

/**
 * Player HUD: "mission control" layout. Top telemetry strip, central orbit scrubber, and a two-deck
 * command console (transport deck + systems deck) exposing every Classic player action.
 */
@Composable
internal fun PurpleGalaxyPlayerOverlay(p: PlayerOverlayParams) {
    if (!p.visible) return
    LaunchedEffect(Unit) { runCatching { p.playButtonFocusRequester.requestFocus() } }
    Box(p.modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(PG.Void.copy(alpha = 0.85f), Color.Transparent, Color.Transparent, PG.Void.copy(alpha = 0.95f)))))
        Row(Modifier.align(Alignment.TopStart).fillMaxWidth().padding(36.dp), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                GalaxyLabel(if (p.isLive && !p.isCatchUpPlayback) "● Live transmission" else if (p.isCatchUpPlayback) "Archive replay" else "Now observing", color = if (p.isLive) PG.Live else PG.Comet)
                GalaxyTitle(p.displayTitle, size = 30)
                p.currentChannelName?.let { Text((if (p.displayChannelNumber > 0) "${p.displayChannelNumber}  " else "") + it, color = PG.Dust, fontSize = 14.sp) }
                p.currentProgram?.let { Text("${formatClock(it.startTime)} – ${formatClock(it.endTime)}", color = PG.Muted, fontSize = 12.sp) }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(formatClock(System.currentTimeMillis()), color = PG.Star, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                if (p.currentRecordingStatus == RecordingStatus.RECORDING) Text("● REC", color = PG.Live, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                if (p.playbackSpeed != 1f) Text("${p.playbackSpeed}×", color = PG.Flare, fontSize = 13.sp)
                if (p.sleepTimerUiState.stopTimerActive) Text("Stop in ${formatDuration(p.sleepTimerUiState.stopRemainingMs)}", color = PG.Muted, fontSize = 12.sp)
                if (p.sleepTimerUiState.idleTimerActive) Text("Standby in ${formatDuration(p.sleepTimerUiState.idleRemainingMs)}", color = PG.Muted, fontSize = 12.sp)
                if (p.isCastConnected) Text("Casting", color = PG.Comet, fontSize = 12.sp)
            }
        }
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 40.dp, vertical = 28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (p.seekPreview.visible) Text("⟶ ${formatDuration(p.seekPreview.positionMs)}", color = PG.Flare, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            when {
                p.isVod && p.duration > 0 -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(formatDuration(p.currentPosition), color = PG.Star, fontSize = 13.sp)
                    OrbitProgress(p.currentPosition.toFloat() / p.duration, Modifier.weight(1f), 5.dp)
                    Text("-" + formatDuration(p.duration - p.currentPosition), color = PG.Dust, fontSize = 13.sp)
                }
                p.isLive && p.currentProgram != null -> {
                    val prog = p.currentProgram
                    OrbitProgress((System.currentTimeMillis() - prog.startTime).toFloat() / (prog.endTime - prog.startTime).coerceAtLeast(1), height = 5.dp)
                    if (p.timeshiftUiState.enabledForSession && p.timeshiftUiState.bufferedBehindLiveMs > 0)
                        Text("Behind live ${formatDuration(p.timeshiftUiState.bufferedBehindLiveMs)}", color = PG.Flare, fontSize = 12.sp)
                }
            }
            // Transport deck
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item { DeckButton("⏪", "Back") { p.onUserInteraction(); p.onSeekBackward() } }
                item { DeckButton(if (p.isPlaying) "❚❚" else "▶", if (p.isPlaying) "Pause" else "Play", primary = true, modifier = Modifier.focusRequester(p.playButtonFocusRequester)) { p.onUserInteraction(); p.onTogglePlayPause() } }
                item { DeckButton("⏩", "Forward") { p.onUserInteraction(); p.onSeekForward() } }
                if (p.isLive) {
                    item { DeckButton("⟲", "Restart") { p.onRestartProgram() } }
                    item { DeckButton("☰", "Archive") { p.onOpenArchive() } }
                    if (p.timeshiftUiState.canSeekToLive) item { DeckButton("⇥", "Live edge") { p.onSeekToLiveEdge() } }
                } else {
                    item { DeckButton("⏮", "Start") { p.onSeekToPosition(0L) } }
                }
                if (p.showEpisodesAction) item { DeckButton("≣", "Episodes") { p.onOpenEpisodes() } }
                item { DeckButton("✕", "Close") { p.onClose() } }
            }
            // Systems deck
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.focusRequester(p.quickActionsFocusRequester)) {
                if (p.isLive) {
                    if (p.currentRecordingStatus == RecordingStatus.RECORDING) item { GalaxyChip("■ Stop rec", true, p.onStopRecording) }
                    else item { GalaxyChip("● Record", false, p.onStartRecording) }
                    item { GalaxyChip("◷ Schedule", false, p.onScheduleRecording) }
                    item { GalaxyChip("Daily", false, p.onScheduleDailyRecording) }
                    item { GalaxyChip("Weekly", false, p.onScheduleWeeklyRecording) }
                }
                item { GalaxyChip("Speed ${p.playbackSpeed}×", false, p.onOpenPlaybackSpeed) }
                if (p.subtitleTrackCount > 0 || p.liveTranslationAvailable) item { GalaxyChip("Subtitles", false, p.onOpenSubtitleTracks) }
                if (p.audioTrackCount > 1) item { GalaxyChip("Audio", false, p.onOpenAudioTracks) }
                if (p.videoQualityCount > 1) item { GalaxyChip("Quality", false, p.onOpenVideoTracks) }
                item { GalaxyChip("Aspect ${p.aspectRatioLabel}", false, p.onToggleAspectRatio) }
                if (p.audioVideoSyncEnabled) item { GalaxyChip("A/V sync", false, p.onOpenAudioVideoSync) }
                item { GalaxyChip(if (p.isMuted) "Unmute" else "Mute", p.isMuted, p.onToggleMute) }
                item { GalaxyChip("Stop timer", p.sleepTimerUiState.stopTimerActive, p.onOpenStopPlaybackTimer) }
                item { GalaxyChip("Standby", p.sleepTimerUiState.idleTimerActive, p.onOpenIdleStandbyTimer) }
                item { GalaxyChip("Multiview", false, p.onOpenSplitScreen) }
                item { GalaxyChip("PiP", false, p.onEnterPictureInPicture) }
                item { GalaxyChip(if (p.isCastConnected) "Stop cast" else "Cast", p.isCastConnected, if (p.isCastConnected) p.onStopCasting else p.onCast) }
            }
        }
    }
}

@Composable
private fun DeckButton(glyph: String, label: String, modifier: Modifier = Modifier, primary: Boolean = false, onClick: () -> Unit) {
    GalaxySurface(onClick = onClick, shape = PG.Pill, container = if (primary) PG.Plasma.copy(alpha = 0.6f) else PG.Glass, scale = 1.1f, modifier = modifier) {
        Row(Modifier.padding(horizontal = if (primary) 26.dp else 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(glyph, color = PG.Star, fontSize = if (primary) 20.sp else 16.sp)
            Text(label, color = PG.Dust, fontSize = 12.sp)
        }
    }
    Spacer(Modifier.width(0.dp))
}
