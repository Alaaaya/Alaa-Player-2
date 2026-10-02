package com.streamvault.app.ui.themes.purplegalaxy

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.streamvault.app.ui.themes.bespoke.LiveChannelInfoParams
import com.streamvault.app.ui.themes.bespoke.LiveChannelListParams
import com.streamvault.app.ui.themes.bespoke.PlayerOverlayParams
import com.streamvault.app.ui.themes.bespoke.progressAt
import com.streamvault.app.ui.themes.bespoke.qualityBadge
import com.streamvault.app.ui.themes.bespoke.tr
import com.streamvault.domain.model.Channel
import com.streamvault.domain.model.Program
import com.streamvault.domain.model.RecordingStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val SEEK_STEP_MS = 10_000L

// ---------------------------------------------------------------- shared pieces

/** Small rounded badge: quality, LIVE, archive, "now". */
@Composable
internal fun GalaxyBadge(text: String, color: Color = PG.Comet, filled: Boolean = false) {
    Text(
        text, color = if (filled) PG.Void else color, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1,
        modifier = Modifier.clip(PG.Pill).background(if (filled) color else color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.6f), PG.Pill).padding(horizontal = 9.dp, vertical = 3.dp)
    )
}

/** Channel number set inside a thin orbit ring: the Purple Galaxy way of showing numbers. */
@Composable
internal fun OrbitNumber(number: Int, size: Int = 40) {
    Box(Modifier.size(size.dp).border(1.5.dp, Brush.sweepGradient(listOf(PG.Comet, PG.Plasma, PG.Flare, PG.Comet)), CircleShape), contentAlignment = Alignment.Center) {
        Text(if (number > 0) "$number" else "–", color = PG.Star, fontSize = (size / 3).sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun ClockBlock() {
    val locale = Locale.getDefault()
    Column(horizontalAlignment = Alignment.End) {
        Text(formatClock(System.currentTimeMillis()), color = PG.Star, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(remember { SimpleDateFormat("EEEE, d MMMM", locale).format(Date()) }, color = PG.Dust, fontSize = 12.sp)
    }
}

/** Now / next telescope card with progress and remaining time; used by the live HUD, zap banner and channel list. */
@Composable
internal fun NowNextCard(now: Program?, next: Program?, modifier: Modifier = Modifier) {
    Row(
        modifier.clip(PG.Panel).background(Brush.horizontalGradient(listOf(PG.Deep.copy(alpha = 0.92f), PG.Nebula.copy(alpha = 0.88f))))
            .border(1.dp, PG.Plasma.copy(alpha = 0.3f), PG.Panel).padding(horizontal = 22.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1.4f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            GalaxyBadge(tr("NOW", "الآن"), PG.Flare, filled = true)
            Text(now?.title ?: tr("No guide data", "لا توجد بيانات دليل"), color = PG.Star, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (now != null) {
                Text("${formatClock(now.startTime)} – ${formatClock(now.endTime)}", color = PG.Dust, fontSize = 12.sp)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OrbitProgress(now.progressAt(), Modifier.weight(1f), 4.dp)
                    Text(tr("Left ", "متبقي ") + formatDuration((now.endTime - System.currentTimeMillis()).coerceAtLeast(0)), color = PG.Muted, fontSize = 11.sp)
                }
            }
        }
        Box(Modifier.width(1.dp).height(64.dp).background(PG.Plasma.copy(alpha = 0.3f)))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(tr("NEXT", "التالي"), color = PG.Comet, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(next?.title ?: "—", color = PG.Star, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            next?.let { Text("${formatClock(it.startTime)} – ${formatClock(it.endTime)}", color = PG.Dust, fontSize = 12.sp) }
        }
    }
}

/** Full-width console tile: glyph, upper-case title and a small sub label (V sport style control bar). */
@Composable
private fun ConsoleTile(glyph: String, title: String, sub: String, onClick: () -> Unit, modifier: Modifier = Modifier, active: Boolean = false) {
    GalaxySurface(onClick = onClick, shape = PG.Card, container = if (active) PG.Plasma.copy(alpha = 0.35f) else PG.Deep.copy(alpha = 0.7f), scale = 1.08f, modifier = modifier.width(108.dp)) {
        Column(Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 6.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(glyph, color = if (active) PG.Flare else PG.Star, fontSize = 20.sp)
            Text(title.uppercase(), color = PG.Star, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(sub, color = PG.Muted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

private class Tile(val glyph: String, val title: String, val sub: String, val active: Boolean = false, val onClick: () -> Unit)

@Composable
private fun ConsoleBar(tiles: List<Tile>, modifier: Modifier = Modifier, firstFocus: FocusRequester? = null) {
    LazyRow(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)) {
        items(tiles.size) { i ->
            val t = tiles[i]
            ConsoleTile(t.glyph, t.title, t.sub, t.onClick, if (i == 0 && firstFocus != null) Modifier.focusRequester(firstFocus) else Modifier, t.active)
        }
    }
}

/** Channel identity row: planet logo, orbit number, name, quality/LIVE/archive badges. */
@Composable
private fun ChannelIdentity(channel: Channel?, name: String?, number: Int, resolution: String?, isTimeshifted: Boolean = false, trailing: @Composable () -> Unit = {}) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        PlanetLogo(channel?.name ?: name.orEmpty(), channel?.logoUrl, 64.dp)
        OrbitNumber(number, 44)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(channel?.name ?: name.orEmpty(), color = PG.Star, fontSize = 22.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                GalaxyBadge(if (isTimeshifted) tr("TIMESHIFT", "مؤجل") else tr("● LIVE", "● مباشر"), PG.Live)
                (resolution ?: channel?.qualityBadge())?.let { GalaxyBadge(it, PG.Comet) }
                if (channel?.catchUpSupported == true) GalaxyBadge("⟲ " + tr("Catch-up", "أرشيف"), PG.Flare)
                if (channel?.isFavorite == true) Text("★", color = PG.Flare, fontSize = 16.sp)
            }
        }
        trailing()
    }
}

// ---------------------------------------------------------------- player overlay

@Composable
internal fun PurpleGalaxyPlayerOverlay(p: PlayerOverlayParams) {
    if (!p.visible) return
    if (p.isLive && !p.isCatchUpPlayback) LiveHud(p) else VodHud(p)
}

/** Live HUD: bottom observatory console (identity + now/next) and a full-width control bar. */
@Composable
private fun LiveHud(p: PlayerOverlayParams) {
    LaunchedEffect(Unit) { runCatching { p.playButtonFocusRequester.requestFocus() } }
    val recording = p.currentRecordingStatus == RecordingStatus.RECORDING
    val tiles = buildList {
        add(Tile(if (p.isPlaying) "❚❚" else "▶", tr("Playback", "تشغيل"), if (p.isPlaying) tr("Pause", "إيقاف مؤقت") else tr("Play", "تشغيل"), true) { p.onUserInteraction(); p.onTogglePlayPause() })
        add(Tile("☰", tr("Channels", "القنوات"), tr("List", "القائمة")) { p.onOpenLiveChannels() })
        add(Tile(if (p.currentChannel?.isFavorite == true) "★" else "☆", tr("Favorite", "المفضلة"), if (p.currentChannel?.isFavorite == true) tr("Saved", "محفوظة") else tr("Add", "إضافة")) { p.onToggleLiveFavorite() })
        add(Tile("▦", tr("Guide", "الدليل"), "EPG") { p.onOpenLiveGuide() })
        add(Tile(if (p.isMuted) "🔇" else "🔊", tr("Mute", "كتم"), if (p.isMuted) tr("On", "مفعل") else tr("Off", "معطل"), p.isMuted) { p.onToggleMute() })
        add(Tile("💬", tr("Subs", "الترجمة"), "${p.subtitleTrackCount}") { p.onOpenSubtitleTracks() })
        add(Tile("🎧", tr("Audio", "الصوت"), "${p.audioTrackCount}") { p.onOpenAudioTracks() })
        add(Tile("HD", tr("Quality", "الجودة"), p.resolutionBadgeLabel ?: "${p.videoQualityCount}") { p.onOpenVideoTracks() })
        add(Tile(if (recording) "■" else "●", tr("Record", "تسجيل"), if (recording) tr("Stop", "إيقاف") else tr("Start", "بدء"), recording) { if (recording) p.onStopRecording() else p.onStartRecording() })
        add(Tile("◷", tr("Schedule", "جدولة"), tr("Once", "مرة")) { p.onScheduleRecording() })
        add(Tile("◷", tr("Daily", "يومي"), tr("Series", "متكرر")) { p.onScheduleDailyRecording() })
        add(Tile("◷", tr("Weekly", "أسبوعي"), tr("Series", "متكرر")) { p.onScheduleWeeklyRecording() })
        add(Tile("⟲", tr("Catch-up", "الأرشيف"), tr("Archive", "أرشيف")) { p.onOpenArchive() })
        add(Tile("⏮", tr("Restart", "من البداية"), tr("Program", "البرنامج")) { p.onRestartProgram() })
        add(Tile("⏪", "-10s", tr("Back", "رجوع")) { p.onUserInteraction(); p.onSeekBackward() })
        add(Tile("⏩", "+10s", tr("Forward", "تقديم")) { p.onUserInteraction(); p.onSeekForward() })
        if (p.timeshiftUiState.canSeekToLive) add(Tile("⇥", tr("Live", "مباشر"), tr("Jump", "انتقال")) { p.onSeekToLiveEdge() })
        add(Tile("▭", tr("Aspect", "نسبة العرض"), p.aspectRatioLabel) { p.onToggleAspectRatio() })
        add(Tile("»", tr("Speed", "السرعة"), "${p.playbackSpeed}×") { p.onOpenPlaybackSpeed() })
        if (p.audioVideoSyncEnabled) add(Tile("⇄", "A/V", tr("Sync", "مزامنة")) { p.onOpenAudioVideoSync() })
        add(Tile("◫", tr("Split", "تقسيم"), "Multiview") { p.onOpenSplitScreen() })
        add(Tile("⏲", tr("Stop timer", "مؤقت"), if (p.sleepTimerUiState.stopTimerActive) formatDuration(p.sleepTimerUiState.stopRemainingMs) else tr("Off", "معطل"), p.sleepTimerUiState.stopTimerActive) { p.onOpenStopPlaybackTimer() })
        add(Tile("☾", tr("Standby", "استعداد"), if (p.sleepTimerUiState.idleTimerActive) formatDuration(p.sleepTimerUiState.idleRemainingMs) else tr("Off", "معطل"), p.sleepTimerUiState.idleTimerActive) { p.onOpenIdleStandbyTimer() })
        add(Tile("⧉", "PiP", tr("Window", "نافذة")) { p.onEnterPictureInPicture() })
        add(Tile("⎚", tr("Cast", "بث"), if (p.isCastConnected) tr("Stop", "إيقاف") else tr("Connect", "اتصال"), p.isCastConnected) { if (p.isCastConnected) p.onStopCasting() else p.onCast() })
        add(Tile("✕", tr("Close", "إغلاق"), tr("Hide", "إخفاء")) { p.onClose() })
    }
    Box(p.modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(PG.Void.copy(alpha = 0.55f), Color.Transparent, Color.Transparent, PG.Void.copy(alpha = 0.95f)))))
        Row(Modifier.align(Alignment.TopStart).fillMaxWidth().padding(32.dp), verticalAlignment = Alignment.Top) {
            Spacer(Modifier.weight(1f))
            ClockBlock()
        }
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 32.dp, vertical = 24.dp)
                .clip(PG.Panel).background(PG.Void.copy(alpha = 0.72f)).border(1.dp, Brush.horizontalGradient(listOf(PG.Comet.copy(alpha = 0.5f), PG.Plasma.copy(alpha = 0.3f), PG.Flare.copy(alpha = 0.5f))), PG.Panel)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) {
                    ChannelIdentity(p.currentChannel, p.currentChannelName, p.displayChannelNumber, p.resolutionBadgeLabel, p.timeshiftUiState.enabledForSession && p.timeshiftUiState.bufferedBehindLiveMs > 0)
                }
                NowNextCard(p.currentProgram, p.nextProgram, Modifier.weight(1.2f))
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(PG.Plasma.copy(alpha = 0.2f)))
            ConsoleBar(tiles, Modifier.focusRequester(p.quickActionsFocusRequester), firstFocus = p.playButtonFocusRequester)
        }
    }
}

private enum class VodPanel { NONE, SETTINGS }

/** VOD HUD: title strip, ±10s orbit transport in the middle, focusable seek bar and an end-side settings drawer. */
@Composable
private fun VodHud(p: PlayerOverlayParams) {
    var panel by remember { mutableStateOf(VodPanel.NONE) }
    val seekFocus = remember { FocusRequester() }
    val panelFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { p.playButtonFocusRequester.requestFocus() } }
    LaunchedEffect(panel) { if (panel == VodPanel.SETTINGS) runCatching { panelFocus.requestFocus() } }
    fun seekBy(delta: Long) {
        p.onUserInteraction()
        val target = (p.currentPosition + delta).coerceIn(0L, (p.duration - 1_000L).coerceAtLeast(0L))
        p.onSeekToPosition(target)
    }
    Box(p.modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(PG.Void.copy(alpha = 0.85f), Color.Transparent, Color.Transparent, PG.Void.copy(alpha = 0.95f)))))
        // top strip
        Row(Modifier.align(Alignment.TopStart).fillMaxWidth().padding(32.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            GalaxySurface(onClick = { p.onNavigateBack() }, shape = CircleShape, container = PG.Glass, modifier = Modifier.size(48.dp)) {
                Text("←", color = PG.Star, fontSize = 22.sp, modifier = Modifier.align(Alignment.Center))
            }
            Column(Modifier.weight(1f)) {
                GalaxyLabel(if (p.isCatchUpPlayback) tr("Archive replay", "إعادة من الأرشيف") else tr("Now observing", "قيد المشاهدة"))
                GalaxyTitle(p.displayTitle, size = 28)
                p.episodeLine?.let { Text(it, color = PG.Dust, fontSize = 14.sp, maxLines = 1) }
            }
            if (p.currentRecordingStatus == RecordingStatus.RECORDING) GalaxyBadge("● REC", PG.Live)
            if (p.playbackSpeed != 1f) GalaxyBadge("${p.playbackSpeed}×", PG.Flare)
            ClockBlock()
        }
        // centre transport: three planets in a row
        if (panel == VodPanel.NONE) Row(Modifier.align(Alignment.Center), horizontalArrangement = Arrangement.spacedBy(40.dp), verticalAlignment = Alignment.CenterVertically) {
            OrbitButton("↺", "10", 72) { seekBy(-SEEK_STEP_MS) }
            OrbitButton(if (p.isPlaying) "❚❚" else "▶", null, 104, Modifier.focusRequester(p.playButtonFocusRequester), primary = true) { p.onUserInteraction(); p.onTogglePlayPause() }
            OrbitButton("↻", "10", 72) { seekBy(SEEK_STEP_MS) }
        }
        // bottom deck
        Column(Modifier.align(Alignment.BottomStart).fillMaxWidth(if (panel == VodPanel.SETTINGS) 0.66f else 1f).padding(horizontal = 40.dp, vertical = 28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (p.seekPreview.visible) Text("⟶ ${formatDuration(p.seekPreview.positionMs)}", color = PG.Flare, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(formatDuration(p.currentPosition), color = PG.Star, fontSize = 13.sp)
                SeekTrack(p, Modifier.weight(1f).focusRequester(seekFocus), ::seekBy)
                Text(formatDuration(p.duration), color = PG.Dust, fontSize = 13.sp)
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.focusRequester(p.quickActionsFocusRequester)) {
                item { DeckPill("🎧", tr("Audio", "الصوت"), p.onOpenAudioTracks) }
                item { DeckPill("CC", tr("Subtitles", "الترجمات"), p.onOpenSubtitleTracks) }
                item { DeckPill("HD", p.resolutionBadgeLabel ?: tr("Quality", "الجودة"), p.onOpenVideoTracks) }
                if (p.showEpisodesAction) item { DeckPill("≣", tr("Episodes", "الحلقات"), p.onOpenEpisodes) }
                item { DeckPill("⏮", tr("Start over", "من البداية")) { p.onSeekToPosition(0L) } }
                item { DeckPill("⚙", tr("Settings", "الإعدادات"), active = panel == VodPanel.SETTINGS) { panel = if (panel == VodPanel.SETTINGS) VodPanel.NONE else VodPanel.SETTINGS } }
                item { DeckPill("✕", tr("Close", "إغلاق"), p.onClose) }
            }
        }
        if (panel == VodPanel.SETTINGS) SettingsDrawer(p, panelFocus) { panel = VodPanel.NONE }
    }
}

@Composable
private fun OrbitButton(glyph: String, caption: String?, size: Int, modifier: Modifier = Modifier, primary: Boolean = false, onClick: () -> Unit) {
    GalaxySurface(onClick = onClick, shape = CircleShape, container = if (primary) PG.Plasma.copy(alpha = 0.55f) else PG.Void.copy(alpha = 0.55f), scale = 1.12f, modifier = modifier.size(size.dp)) {
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(glyph, color = PG.Star, fontSize = (size / 3).sp)
            caption?.let { Text(it, color = PG.Dust, fontSize = 11.sp) }
        }
    }
}

@Composable
private fun DeckPill(glyph: String, label: String, onClick: () -> Unit, active: Boolean = false) {
    GalaxySurface(onClick = onClick, shape = PG.Pill, container = if (active) PG.Plasma.copy(alpha = 0.45f) else PG.Glass, scale = 1.08f) {
        Row(Modifier.padding(horizontal = 18.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(glyph, color = PG.Star, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(label, color = PG.Dust, fontSize = 13.sp, maxLines = 1)
        }
    }
}

@Composable
private fun DeckPill(glyph: String, label: String, active: Boolean = false, onClick: () -> Unit) = DeckPill(glyph, label, onClick, active)

/** Focusable seek track: LEFT/RIGHT jump 10s, OK toggles play/pause. Thicker and glowing while focused. */
@Composable
private fun SeekTrack(p: PlayerOverlayParams, modifier: Modifier, seekBy: (Long) -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val progress = if (p.duration > 0) p.currentPosition.toFloat() / p.duration else 0f
    GalaxySurface(
        onClick = { p.onUserInteraction(); p.onTogglePlayPause() }, shape = PG.Pill, container = Color.Transparent, focusedContainer = PG.Glass, scale = 1.0f,
        modifier = modifier.onFocusChanged { focused = it.isFocused; p.onSetScrubbingMode(it.isFocused) }.onPreviewKeyEvent { e ->
            val n = e.nativeKeyEvent
            if (n.action != android.view.KeyEvent.ACTION_DOWN) return@onPreviewKeyEvent false
            when (n.keyCode) {
                android.view.KeyEvent.KEYCODE_DPAD_LEFT -> { seekBy(-SEEK_STEP_MS); true }
                android.view.KeyEvent.KEYCODE_DPAD_RIGHT -> { seekBy(SEEK_STEP_MS); true }
                else -> false
            }
        }
    ) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp)) {
            OrbitProgress(progress.coerceIn(0f, 1f), Modifier.fillMaxWidth(), if (focused) 8.dp else 5.dp)
        }
    }
}

/** End-side drawer listing every playback setting with its current value. */
@Composable
private fun BoxScope.SettingsDrawer(p: PlayerOverlayParams, focus: FocusRequester, onDismiss: () -> Unit) {
    val rows = buildList<Triple<String, String, () -> Unit>> {
        add(Triple(tr("Video quality", "جودة الفيديو"), p.resolutionBadgeLabel ?: "${p.videoQualityCount}", p.onOpenVideoTracks))
        add(Triple(tr("Audio", "الصوت"), "${p.audioTrackCount}", p.onOpenAudioTracks))
        add(Triple(tr("Subtitles", "الترجمات"), "${p.subtitleTrackCount}", p.onOpenSubtitleTracks))
        add(Triple(tr("Aspect ratio", "نسبة العرض"), p.aspectRatioLabel, p.onToggleAspectRatio))
        add(Triple(tr("Playback speed", "سرعة التشغيل"), "${p.playbackSpeed}×", p.onOpenPlaybackSpeed))
        if (p.audioVideoSyncEnabled) add(Triple(tr("A/V sync", "مزامنة الصوت"), "", p.onOpenAudioVideoSync))
        add(Triple(tr("Mute", "كتم الصوت"), if (p.isMuted) tr("On", "مفعل") else tr("Off", "معطل"), p.onToggleMute))
        add(Triple(tr("Stop timer", "مؤقت الإيقاف"), if (p.sleepTimerUiState.stopTimerActive) formatDuration(p.sleepTimerUiState.stopRemainingMs) else tr("Off", "معطل"), p.onOpenStopPlaybackTimer))
        add(Triple(tr("Standby timer", "مؤقت الاستعداد"), if (p.sleepTimerUiState.idleTimerActive) formatDuration(p.sleepTimerUiState.idleRemainingMs) else tr("Off", "معطل"), p.onOpenIdleStandbyTimer))
        add(Triple(tr("Multiview", "تقسيم الشاشة"), "", p.onOpenSplitScreen))
        add(Triple(tr("Picture in picture", "صورة داخل صورة"), "", p.onEnterPictureInPicture))
        add(Triple(tr("Cast", "البث"), if (p.isCastConnected) tr("Connected", "متصل") else "", if (p.isCastConnected) p.onStopCasting else p.onCast))
    }
    Column(
        Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(380.dp).background(Brush.horizontalGradient(listOf(PG.Deep.copy(alpha = 0.9f), PG.Void.copy(alpha = 0.97f))))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GalaxyTitle(tr("Settings", "الإعدادات"), Modifier.weight(1f), size = 24)
            GalaxySurface(onClick = onDismiss, shape = CircleShape, modifier = Modifier.size(40.dp)) { Text("✕", color = PG.Star, modifier = Modifier.align(Alignment.Center)) }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(rows.size) { i ->
                val (label, value, action) = rows[i]
                GalaxySurface(onClick = action, shape = PG.Card, container = Color.Transparent, scale = 1.03f, modifier = Modifier.fillMaxWidth().then(if (i == 0) Modifier.focusRequester(focus) else Modifier)) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(label, color = PG.Star, fontSize = 15.sp, modifier = Modifier.weight(1f), maxLines = 1)
                        Text(value.ifBlank { "›" }, color = PG.Comet, fontSize = 13.sp, maxLines = 1)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- live channel list

@Composable
internal fun PurpleGalaxyLiveChannelList(p: LiveChannelListParams) {
    val currentIndex = remember(p.channels, p.currentChannelId) { p.channels.indexOfFirst { it.id == p.currentChannelId }.coerceAtLeast(0) }
    val listState = rememberLazyListState(currentIndex)
    var focused by remember(p.currentChannelId) { mutableStateOf(p.channels.getOrNull(currentIndex)) }
    LaunchedEffect(Unit) { runCatching { p.focusRequester.requestFocus() } }
    Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(PG.Void.copy(alpha = 0.85f), PG.Void.copy(alpha = 0.35f), Color.Transparent)))) {
        Column(
            Modifier.align(Alignment.TopStart).fillMaxHeight().width(560.dp).padding(24.dp).clip(PG.Panel)
                .background(PG.Deep.copy(alpha = 0.88f)).border(1.dp, PG.Plasma.copy(alpha = 0.35f), PG.Panel).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("✦", color = PG.Flare, fontSize = 24.sp)
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(tr("Channels", "القنوات"), color = PG.Star, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("  (${p.channels.size})", color = PG.Flare, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(p.lastVisitedCategoryName ?: tr("Press OK to select a channel", "اضغط OK لاختيار القناة"), color = PG.Muted, fontSize = 12.sp, maxLines = 1)
                }
                GalaxySurface(onClick = p.onDismiss, shape = CircleShape, modifier = Modifier.size(40.dp)) { Text("✕", color = PG.Star, modifier = Modifier.align(Alignment.Center)) }
            }
            if (p.recentChannels.isNotEmpty()) {
                Text("◷ " + tr("Recent", "الأخيرة"), color = PG.Comet, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(p.recentChannels, key = { "r${it.id}" }) { c ->
                        GalaxySurface(onClick = { p.onInteracted(); p.onSelectChannel(c.id) }, shape = PG.Pill, container = PG.Glass, scale = 1.08f,
                            modifier = Modifier.onFocusChanged { if (it.isFocused) { focused = c; p.onInteracted() } }) {
                            Row(Modifier.padding(start = 6.dp, end = 14.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                PlanetLogo(c.name, c.logoUrl, 30.dp)
                                Column {
                                    Text(c.name, color = PG.Star, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(110.dp))
                                    Text("%03d".format(p.numberOf(c)), color = PG.Muted, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GalaxyChip(tr("Categories", "الفئات"), false, p.onOpenCategories)
                if (p.lastVisitedCategoryName != null) GalaxyChip(tr("Last group", "آخر مجموعة"), false, p.onOpenLastGroup)
                GalaxyChip(tr("Guide", "الدليل"), false, p.onOpenGuide)
            }
            LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                items(p.channels, key = { it.id }) { c ->
                    val isCurrent = c.id == p.currentChannelId
                    GalaxySurface(
                        onClick = { p.onInteracted(); p.onSelectChannel(c.id) }, shape = PG.Pill, scale = 1.02f,
                        container = if (isCurrent) PG.Plasma.copy(alpha = 0.3f) else Color.Transparent,
                        modifier = Modifier.fillMaxWidth().then(if (isCurrent) Modifier.focusRequester(p.focusRequester) else Modifier)
                            .onFocusChanged { if (it.isFocused) { focused = c; p.onInteracted() } }
                    ) {
                        Row(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            PlanetLogo(c.name, c.logoUrl, 40.dp)
                            Text("%02d".format(p.numberOf(c)), color = PG.Flare, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(44.dp))
                            Text(c.name, color = PG.Star, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            if (isCurrent) GalaxyBadge("• " + tr("Now", "الآن"), PG.Flare)
                            c.qualityBadge()?.let { GalaxyBadge(it, PG.Comet) }
                            if (c.catchUpSupported) Text("⟲ " + tr("Archive", "أرشيف"), color = PG.Flare, fontSize = 11.sp)
                            if (c.isFavorite) Text("★", color = PG.Flare, fontSize = 14.sp)
                        }
                    }
                }
            }
            Text("OK " + tr("select", "اختيار") + "   ·   BACK " + tr("close", "إغلاق"), color = PG.Muted, fontSize = 11.sp)
        }
        focused?.let { c ->
            NowNextCard(c.currentProgram, c.nextProgram, Modifier.align(Alignment.BottomEnd).padding(28.dp).width(620.dp))
        }
    }
}

// ---------------------------------------------------------------- live zap banner

@Composable
internal fun PurpleGalaxyLiveChannelInfo(p: LiveChannelInfoParams) {
    val first = p.focusRequester
    LaunchedEffect(Unit) { runCatching { first.requestFocus() } }
    val recording = p.currentRecordingStatus == RecordingStatus.RECORDING
    val tiles = buildList {
        add(Tile("☰", tr("Channels", "القنوات"), tr("List", "القائمة"), true) { p.onInteracted(); p.onOpenChannelList() })
        add(Tile(if (p.isPlaying) "❚❚" else "▶", tr("Playback", "تشغيل"), if (p.isPlaying) tr("Pause", "إيقاف مؤقت") else tr("Play", "تشغيل")) { p.onInteracted(); p.onTogglePlayPause() })
        add(Tile(if (p.isMuted) "🔇" else "🔊", tr("Mute", "كتم"), if (p.isMuted) tr("On", "مفعل") else tr("Off", "معطل"), p.isMuted) { p.onInteracted(); p.onToggleMute() })
        add(Tile("💬", tr("Subs", "الترجمة"), "${p.subtitleTrackCount}") { p.onInteracted(); p.onOpenSubtitleTracks() })
        add(Tile("🎧", tr("Audio", "الصوت"), "${p.audioTrackCount}") { p.onInteracted(); p.onOpenAudioTracks() })
        add(Tile("▦", tr("Guide", "الدليل"), "EPG") { p.onInteracted(); p.onOpenFullEpg() })
        add(Tile("◫", tr("Split", "تقسيم"), "Multiview") { p.onInteracted(); p.onOpenSplitScreen() })
        add(Tile("∿", tr("Diagnostics", "التشخيص"), tr("Stats", "إحصاءات"), p.isDiagnosticsEnabled) { p.onInteracted(); p.onToggleDiagnostics() })
        add(Tile("HD", tr("Quality", "الجودة"), p.resolutionLabel ?: "${p.videoQualityCount}") { p.onInteracted(); p.onOpenVideoTracks() })
        if (p.variantCount > 1) add(Tile("⇋", tr("Source", "المصدر"), "${p.variantCount}") { p.onInteracted(); p.onOpenVariants() })
        add(Tile(if (recording) "■" else "●", tr("Record", "تسجيل"), if (recording) tr("Stop", "إيقاف") else tr("Start", "بدء"), recording) { p.onInteracted(); if (recording) p.onStopRecording() else p.onStartRecording() })
        add(Tile("◷", tr("Schedule", "جدولة"), tr("Once", "مرة")) { p.onInteracted(); p.onScheduleRecording() })
        add(Tile("⟲", tr("Catch-up", "الأرشيف"), tr("Archive", "أرشيف")) { p.onInteracted(); p.onOpenArchive() })
        add(Tile("⏮", tr("Restart", "من البداية"), tr("Program", "البرنامج")) { p.onInteracted(); p.onRestartProgram() })
        if (p.canSeekToLive) add(Tile("⇥", tr("Live", "مباشر"), tr("Jump", "انتقال")) { p.onInteracted(); p.onSeekToLiveEdge() })
        add(Tile("▭", tr("Aspect", "نسبة العرض"), p.aspectRatioLabel) { p.onInteracted(); p.onToggleAspectRatio() })
        add(Tile("⇄", "A/V", tr("Sync", "مزامنة")) { p.onInteracted(); p.onOpenAudioVideoSync() })
        add(Tile("⧉", "PiP", tr("Window", "نافذة")) { p.onEnterPictureInPicture() })
        add(Tile("⎚", tr("Cast", "بث"), if (p.isCastConnected) tr("Stop", "إيقاف") else tr("Connect", "اتصال"), p.isCastConnected) { if (p.isCastConnected) p.onStopCasting() else p.onCast() })
    }
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 32.dp, vertical = 24.dp)
                .clip(PG.Panel).background(PG.Void.copy(alpha = 0.8f)).border(1.dp, Brush.horizontalGradient(listOf(PG.Comet.copy(alpha = 0.5f), PG.Flare.copy(alpha = 0.5f))), PG.Panel)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) { ChannelIdentity(p.channel, null, p.displayChannelNumber, p.resolutionLabel) { ClockBlock() } }
                NowNextCard(p.currentProgram, p.nextProgram, Modifier.weight(1.2f))
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(PG.Plasma.copy(alpha = 0.2f)))
            ConsoleBar(tiles, firstFocus = first)
        }
    }
}
