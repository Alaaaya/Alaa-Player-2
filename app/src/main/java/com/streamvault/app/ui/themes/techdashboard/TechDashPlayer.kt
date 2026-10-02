package com.streamvault.app.ui.themes.techdashboard

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
internal fun TechDashBadge(text: String, color: Color = TD.Comet, filled: Boolean = false) {
    Text(
        text, color = if (filled) TD.Void else color, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1,
        modifier = Modifier.clip(TD.Pill).background(if (filled) color else color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.6f), TD.Pill).padding(horizontal = 9.dp, vertical = 3.dp)
    )
}

/** Channel number as a bracketed mono register. */
@Composable
internal fun TechDashNumber(number: Int, size: Int = 40) {
    Text(if (number > 0) "[%03d]".format(number) else "[---]", color = TD.Plasma, fontSize = (size / 2.6f).sp, fontFamily = TD.Mono, fontWeight = FontWeight.Bold, maxLines = 1)
}

@Composable
private fun ClockBlock() {
    val locale = Locale.getDefault()
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(remember { SimpleDateFormat("yyyy-MM-dd", locale).format(Date()) }, color = TD.Muted, fontSize = 11.sp, fontFamily = TD.Mono)
        Text(formatClock(System.currentTimeMillis()), color = TD.Plasma, fontSize = 18.sp, fontFamily = TD.Mono, fontWeight = FontWeight.Bold)
    }
}

/** Now / next as a two-row schedule table with a segmented meter on the NOW row. */
@Composable
internal fun NowNextCard(now: Program?, next: Program?, modifier: Modifier = Modifier) {
    Column(
        modifier.clip(TD.Panel).background(TD.Void.copy(alpha = 0.88f)).border(1.dp, TD.Plasma.copy(alpha = 0.3f), TD.Panel).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(tr("NOW ", "الآن"), color = TD.Void, fontSize = 10.sp, fontFamily = TD.Mono, fontWeight = FontWeight.Bold, modifier = Modifier.background(TD.Plasma).padding(horizontal = 5.dp, vertical = 2.dp))
            Text(now?.let { "${formatClock(it.startTime)}-${formatClock(it.endTime)}" } ?: "--:--", color = TD.Muted, fontSize = 11.sp, fontFamily = TD.Mono)
            Text(now?.title ?: tr("No guide data", "لا توجد بيانات دليل"), color = TD.Star, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            if (now != null) Text("-" + formatDuration((now.endTime - System.currentTimeMillis()).coerceAtLeast(0)), color = TD.Flare, fontSize = 11.sp, fontFamily = TD.Mono)
        }
        if (now != null) TechDashProgress(now.progressAt(), Modifier.fillMaxWidth(), 4.dp)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(tr("NEXT", "التالي"), color = TD.Plasma, fontSize = 10.sp, fontFamily = TD.Mono, fontWeight = FontWeight.Bold, modifier = Modifier.border(1.dp, TD.Plasma).padding(horizontal = 5.dp, vertical = 2.dp))
            Text(next?.let { "${formatClock(it.startTime)}-${formatClock(it.endTime)}" } ?: "--:--", color = TD.Muted, fontSize = 11.sp, fontFamily = TD.Mono)
            Text(next?.title ?: "--", color = TD.Dust, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        }
    }
}

/** Command cell: flat bordered mono key, label with value underneath. */
@Composable
private fun ConsoleTile(glyph: String, title: String, sub: String, onClick: () -> Unit, modifier: Modifier = Modifier, active: Boolean = false) {
    TechDashSurface(onClick = onClick, shape = TD.Pill, container = if (active) TD.Plasma else Color.Transparent, scale = 1.04f, modifier = modifier.border(1.dp, TD.Plasma.copy(alpha = 0.35f), TD.Pill)) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(glyph, color = if (active) TD.Void else TD.Plasma, fontSize = 13.sp, fontFamily = TD.Mono)
            Column {
                Text(title.uppercase(), color = if (active) TD.Void else TD.Star, fontSize = 11.sp, fontFamily = TD.Mono, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(sub, color = if (active) TD.Void else TD.Muted, fontSize = 9.sp, fontFamily = TD.Mono, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

private class Tile(val glyph: String, val title: String, val sub: String, val active: Boolean = false, val onClick: () -> Unit)

@Composable
private fun ConsoleBar(tiles: List<Tile>, modifier: Modifier = Modifier, firstFocus: FocusRequester? = null) {
    LazyRow(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp), contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp)) {
        items(tiles.size) { i ->
            val t = tiles[i]
            ConsoleTile(t.glyph, t.title, t.sub, t.onClick, if (i == 0 && firstFocus != null) Modifier.focusRequester(firstFocus) else Modifier, t.active)
        }
    }
}

/** Channel identity as a telemetry line: register number, name, status flags. */
@Composable
private fun ChannelIdentity(channel: Channel?, name: String?, number: Int, resolution: String?, isTimeshifted: Boolean = false, trailing: @Composable () -> Unit = {}) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        TechDashNumber(number, 44)
        TechDashLogo(channel?.name ?: name.orEmpty(), channel?.logoUrl, 40.dp)
        Text(channel?.name ?: name.orEmpty(), color = TD.Star, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
        TechDashBadge(if (isTimeshifted) "TSHIFT" else "LIVE", TD.Live, filled = true)
        (resolution ?: channel?.qualityBadge())?.let { TechDashBadge(it, TD.Comet) }
        if (channel?.catchUpSupported == true) TechDashBadge("ARCHIVE", TD.Flare)
        if (channel?.isFavorite == true) TechDashBadge("★ FAV", TD.Flare)
        Spacer(Modifier.weight(1f))
        trailing()
    }
}

// ---------------------------------------------------------------- player overlay

@Composable
internal fun TechDashPlayerOverlay(p: PlayerOverlayParams) {
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
        Row(
            Modifier.align(Alignment.TopCenter).fillMaxWidth().background(TD.Void.copy(alpha = 0.85f)).border(1.dp, TD.Plasma.copy(alpha = 0.25f)).padding(horizontal = 28.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.weight(1f)) {
                ChannelIdentity(p.currentChannel, p.currentChannelName, p.displayChannelNumber, p.resolutionBadgeLabel, p.timeshiftUiState.enabledForSession && p.timeshiftUiState.bufferedBehindLiveMs > 0) { ClockBlock() }
            }
        }
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(TD.Void.copy(alpha = 0.9f)).padding(horizontal = 28.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            NowNextCard(p.currentProgram, p.nextProgram, Modifier.fillMaxWidth(0.6f))
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
        // top strip
        Row(Modifier.align(Alignment.TopStart).fillMaxWidth().background(TD.Void.copy(alpha = 0.85f)).padding(horizontal = 28.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ConsoleTile("<-", tr("exit", "خروج"), "BACK", { p.onNavigateBack() })
            Column(Modifier.weight(1f)) {
                TechDashLabel(if (p.isCatchUpPlayback) tr("Archive replay", "إعادة من الأرشيف") else tr("Now playing", "قيد المشاهدة"))
                TechDashTitle(p.displayTitle, size = 28)
                p.episodeLine?.let { Text(it, color = TD.Dust, fontSize = 14.sp, maxLines = 1) }
            }
            if (p.currentRecordingStatus == RecordingStatus.RECORDING) TechDashBadge("● REC", TD.Live)
            if (p.playbackSpeed != 1f) TechDashBadge("${p.playbackSpeed}×", TD.Flare)
            ClockBlock()
        }
        // bottom deck
        Column(Modifier.align(Alignment.BottomStart).fillMaxWidth(if (panel == VodPanel.SETTINGS) 0.66f else 1f).background(TD.Void.copy(alpha = 0.88f)).padding(horizontal = 28.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(formatDuration(if (p.seekPreview.visible) p.seekPreview.positionMs else p.currentPosition), color = if (p.seekPreview.visible) TD.Flare else TD.Plasma, fontSize = 40.sp, fontFamily = TD.Mono, fontWeight = FontWeight.Bold)
                Text("/ " + formatDuration(p.duration), color = TD.Muted, fontSize = 16.sp, fontFamily = TD.Mono, modifier = Modifier.padding(bottom = 6.dp))
                Spacer(Modifier.weight(1f))
                ConsoleTile("<<", "-10s", tr("back", "رجوع"), { seekBy(-SEEK_STEP_MS) })
                ConsoleTile(if (p.isPlaying) "||" else ">", if (p.isPlaying) tr("pause", "إيقاف") else tr("play", "تشغيل"), "OK", { p.onUserInteraction(); p.onTogglePlayPause() }, Modifier.focusRequester(p.playButtonFocusRequester), active = true)
                ConsoleTile(">>", "+10s", tr("fwd", "تقديم"), { seekBy(SEEK_STEP_MS) })
            }
            SeekTrack(p, Modifier.fillMaxWidth().focusRequester(seekFocus), ::seekBy)
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
private fun TechDashButton(glyph: String, caption: String?, size: Int, modifier: Modifier = Modifier, primary: Boolean = false, onClick: () -> Unit) {
    TechDashSurface(onClick = onClick, shape = CircleShape, container = if (primary) TD.Plasma.copy(alpha = 0.55f) else TD.Void.copy(alpha = 0.55f), scale = 1.12f, modifier = modifier.size(size.dp)) {
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(glyph, color = TD.Star, fontSize = (size / 3).sp)
            caption?.let { Text(it, color = TD.Dust, fontSize = 11.sp) }
        }
    }
}

@Composable
private fun DeckPill(glyph: String, label: String, onClick: () -> Unit, active: Boolean = false) = ConsoleTile(glyph, label, if (active) "ON" else "--", onClick, active = active)

@Composable
private fun DeckPill(glyph: String, label: String, active: Boolean = false, onClick: () -> Unit) = DeckPill(glyph, label, onClick, active)

/** Focusable seek track: LEFT/RIGHT jump 10s, OK toggles play/pause. Thicker and glowing while focused. */
@Composable
private fun SeekTrack(p: PlayerOverlayParams, modifier: Modifier, seekBy: (Long) -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val progress = if (p.duration > 0) p.currentPosition.toFloat() / p.duration else 0f
    TechDashSurface(
        onClick = { p.onUserInteraction(); p.onTogglePlayPause() }, shape = TD.Pill, container = Color.Transparent, focusedContainer = TD.Glass, scale = 1.0f,
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
            TechDashProgress(progress.coerceIn(0f, 1f), Modifier.fillMaxWidth(), if (focused) 8.dp else 5.dp)
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
        Modifier.align(Alignment.BottomEnd).padding(16.dp).fillMaxHeight(0.86f).width(400.dp).background(TD.Void.copy(alpha = 0.95f)).border(1.dp, TD.Plasma.copy(alpha = 0.4f))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("~/playback.cfg", color = TD.Plasma, fontSize = 14.sp, fontFamily = TD.Mono, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            ConsoleTile("x", tr("close", "إغلاق"), "ESC", onDismiss)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(rows.size) { i ->
                val (label, value, action) = rows[i]
                TechDashSurface(onClick = action, shape = TD.Pill, container = Color.Transparent, scale = 1.02f, modifier = Modifier.fillMaxWidth().then(if (i == 0) Modifier.focusRequester(focus) else Modifier)) {
                    Row(Modifier.padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(label.lowercase().replace(' ', '_'), color = TD.Star, fontSize = 13.sp, fontFamily = TD.Mono, modifier = Modifier.weight(1f), maxLines = 1)
                        Text("= " + value.ifBlank { "open" }, color = TD.Plasma, fontSize = 12.sp, fontFamily = TD.Mono, maxLines = 1)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- live channel list

@Composable
internal fun TechDashLiveChannelList(p: LiveChannelListParams) {
    val currentIndex = remember(p.channels, p.currentChannelId) { p.channels.indexOfFirst { it.id == p.currentChannelId }.coerceAtLeast(0) }
    val listState = rememberLazyListState(currentIndex)
    var focused by remember(p.currentChannelId) { mutableStateOf(p.channels.getOrNull(currentIndex)) }
    LaunchedEffect(Unit) { runCatching { p.focusRequester.requestFocus() } }
    Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.Transparent, TD.Void.copy(alpha = 0.4f), TD.Void.copy(alpha = 0.9f))))) {
        Column(
            Modifier.align(Alignment.TopEnd).fillMaxHeight().width(600.dp)
                .background(TD.Void.copy(alpha = 0.94f)).border(1.dp, TD.Plasma.copy(alpha = 0.35f)).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("CHANNELS // ${p.channels.size}", color = TD.Plasma, fontSize = 16.sp, fontFamily = TD.Mono, fontWeight = FontWeight.Bold)
                    Text(p.lastVisitedCategoryName ?: tr("Press OK to select a channel", "اضغط OK لاختيار القناة"), color = TD.Muted, fontSize = 12.sp, maxLines = 1)
                }
                ConsoleTile("x", tr("close", "إغلاق"), "BACK", p.onDismiss)
            }
            if (p.recentChannels.isNotEmpty()) {
                Text("> " + tr("recent", "الأخيرة"), color = TD.Muted, fontSize = 11.sp, fontFamily = TD.Mono)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(p.recentChannels, key = { "r${it.id}" }) { c ->
                        TechDashSurface(onClick = { p.onInteracted(); p.onSelectChannel(c.id) }, shape = TD.Pill, container = Color.Transparent, scale = 1.05f,
                            modifier = Modifier.onFocusChanged { if (it.isFocused) { focused = c; p.onInteracted() } }) {
                            Text("%03d ".format(p.numberOf(c)) + c.name, color = TD.Star, fontSize = 11.sp, fontFamily = TD.Mono, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.border(1.dp, TD.Plasma.copy(alpha = 0.3f), TD.Pill).padding(horizontal = 8.dp, vertical = 6.dp).width(130.dp))
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TechDashChip(tr("Categories", "الفئات"), false, p.onOpenCategories)
                if (p.lastVisitedCategoryName != null) TechDashChip(tr("Last group", "آخر مجموعة"), false, p.onOpenLastGroup)
                TechDashChip(tr("Guide", "الدليل"), false, p.onOpenGuide)
            }
            LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                items(p.channels, key = { it.id }) { c ->
                    val isCurrent = c.id == p.currentChannelId
                    TechDashSurface(
                        onClick = { p.onInteracted(); p.onSelectChannel(c.id) }, shape = TD.Pill, scale = 1.02f,
                        container = if (isCurrent) TD.Plasma.copy(alpha = 0.3f) else Color.Transparent,
                        modifier = Modifier.fillMaxWidth().then(if (isCurrent) Modifier.focusRequester(p.focusRequester) else Modifier)
                            .onFocusChanged { if (it.isFocused) { focused = c; p.onInteracted() } }
                    ) {
                        Row(Modifier.padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(if (isCurrent) ">" else " ", color = TD.Plasma, fontSize = 13.sp, fontFamily = TD.Mono)
                            Text("%03d".format(p.numberOf(c)), color = TD.Plasma, fontSize = 13.sp, fontFamily = TD.Mono, modifier = Modifier.width(36.dp))
                            TechDashLogo(c.name, c.logoUrl, 28.dp)
                            Text(c.name, color = TD.Star, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            Text(c.qualityBadge() ?: "--", color = TD.Comet, fontSize = 10.sp, fontFamily = TD.Mono, modifier = Modifier.width(34.dp))
                            Text(if (c.catchUpSupported) "ARC" else "---", color = if (c.catchUpSupported) TD.Flare else TD.Muted, fontSize = 10.sp, fontFamily = TD.Mono)
                            Text(if (c.isFavorite) "★" else "·", color = TD.Flare, fontSize = 13.sp)
                        }
                    }
                }
            }
            Text("[OK] " + tr("select", "اختيار") + "  [BACK] " + tr("close", "إغلاق"), color = TD.Muted, fontSize = 10.sp, fontFamily = TD.Mono)
        }
        focused?.let { c ->
            NowNextCard(c.currentProgram, c.nextProgram, Modifier.align(Alignment.BottomStart).padding(24.dp).width(560.dp))
        }
    }
}

// ---------------------------------------------------------------- live zap banner

@Composable
internal fun TechDashLiveChannelInfo(p: LiveChannelInfoParams) {
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
            Modifier.align(Alignment.TopCenter).fillMaxWidth().background(TD.Void.copy(alpha = 0.9f)).border(1.dp, TD.Plasma.copy(alpha = 0.3f))
                .padding(horizontal = 28.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ChannelIdentity(p.channel, null, p.displayChannelNumber, p.resolutionLabel) { ClockBlock() }
            NowNextCard(p.currentProgram, p.nextProgram, Modifier.fillMaxWidth())
            ConsoleBar(tiles, firstFocus = first)
        }
    }
}
