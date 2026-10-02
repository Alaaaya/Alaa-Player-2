package com.streamvault.app.ui.themes.mediacenter

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
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

private const val STEP = 10_000L

private class McAct(val glyph: String, val label: String, val active: Boolean = false, val onClick: () -> Unit)

/** Receiver front-panel key: rectangular brass-ruled button, glyph + engraved uppercase caption side by side. */
@Composable
private fun McCtl(a: McAct, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 52.dp) {
    McCard(onClick = a.onClick, shape = MC.RSmall, container = if (a.active) Color(0xFF33281A) else Color(0xFF120E09), modifier = modifier.height(size * 0.8f)) {
        Row(Modifier.fillMaxHeight().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(a.glyph, color = MC.Amber, fontSize = (size.value * 0.3f).sp)
            Text(a.label.uppercase(), color = if (a.active) MC.Amber else MC.Sub, fontSize = 10.sp, letterSpacing = 1.2.sp, maxLines = 1)
        }
    }
}

@Composable
private fun NowNext(now: Program?, next: Program?, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (now != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                McBadge(tr("NOW", "الآن"), MC.Amber, filled = true)
                Text(now.title, color = MC.Text, fontSize = 18.sp, fontFamily = MC.Serif, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, false))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(mcClock(now.startTime), color = MC.Sub, fontSize = 12.sp)
                McProgress(now.progressAt(), Modifier.weight(1f))
                Text(mcClock(now.endTime), color = MC.Sub, fontSize = 12.sp)
            }
        } else Text(tr("No programme information", "لا توجد معلومات عن البرنامج"), color = MC.Faint, fontSize = 14.sp)
        next?.let { Text(tr("Next", "التالي") + "  ${mcClock(it.startTime)}  ·  ${it.title}", color = MC.Faint, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
    }
}

@Composable
private fun ChannelHead(channel: Channel?, name: String?, number: Int, resolution: String?, timeshift: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(Modifier.size(64.dp).border(1.dp, MC.Amber, MC.RSmall).background(Color(0xFF120E09)), contentAlignment = Alignment.Center) {
            Text(if (number > 0) "%03d".format(number) else "—", color = MC.Amber, fontSize = 20.sp, fontFamily = MC.Serif)
        }
        McLogo(channel?.name ?: name.orEmpty(), channel?.logoUrl, 64.dp)
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(channel?.name ?: name.orEmpty(), color = MC.Text, fontSize = 24.sp, fontFamily = MC.Serif, maxLines = 1)
                if (channel?.isFavorite == true) Text("★", color = MC.Amber, fontSize = 16.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                McBadge(if (timeshift) tr("PAUSED LIVE", "مباشر مؤجل") else "● LIVE", MC.Live, filled = !timeshift)
                (resolution ?: channel?.qualityBadge())?.let { McBadge(it, MC.Text) }
                if (channel?.catchUpSupported == true) McBadge(tr("CATCH-UP", "أرشيف"), MC.Blue)
            }
        }
    }
}



@Composable
internal fun MediaCenterPlayerOverlay(p: PlayerOverlayParams) {
    if (!p.visible) return
    if (p.isLive && !p.isCatchUpPlayback) McLive(p) else McVod(p)
}

/** Live: receiver OSD. Top walnut band (number box, logo, name, clock), bottom front panel with now/next + full-width key strip. */
@Composable
private fun McLive(p: PlayerOverlayParams) {
    LaunchedEffect(Unit) { runCatching { p.playButtonFocusRequester.requestFocus() } }
    val rec = p.currentRecordingStatus == RecordingStatus.RECORDING
    val acts = buildList {
        add(McAct(if (p.isPlaying) "❚❚" else "▶", if (p.isPlaying) tr("Pause", "إيقاف") else tr("Play", "تشغيل"), true) { p.onUserInteraction(); p.onTogglePlayPause() })
        add(McAct("☰", tr("Channels", "القنوات")) { p.onOpenLiveChannels() })
        add(McAct("▦", tr("Guide", "الدليل")) { p.onOpenLiveGuide() })
        add(McAct(if (p.currentChannel?.isFavorite == true) "♥" else "♡", tr("My List", "قائمتي"), p.currentChannel?.isFavorite == true) { p.onToggleLiveFavorite() })
        add(McAct("⏪", "-10s") { p.onUserInteraction(); p.onSeekBackward() })
        add(McAct("⏩", "+10s") { p.onUserInteraction(); p.onSeekForward() })
        add(McAct("⏮", tr("Restart", "من البداية")) { p.onRestartProgram() })
        add(McAct("⟲", tr("Catch-up", "الأرشيف")) { p.onOpenArchive() })
        if (p.timeshiftUiState.canSeekToLive) add(McAct("⇥", tr("Go live", "مباشر")) { p.onSeekToLiveEdge() })
        add(McAct("CC", tr("Subtitles", "الترجمة") + " ${p.subtitleTrackCount}") { p.onOpenSubtitleTracks() })
        add(McAct("♪", tr("Audio", "الصوت") + " ${p.audioTrackCount}") { p.onOpenAudioTracks() })
        add(McAct("HD", p.resolutionBadgeLabel ?: tr("Quality", "الجودة")) { p.onOpenVideoTracks() })
        add(McAct(if (p.isMuted) "🔇" else "🔊", tr("Mute", "كتم"), p.isMuted) { p.onToggleMute() })
        add(McAct(if (rec) "■" else "●", if (rec) tr("Stop rec", "إيقاف التسجيل") else tr("Record", "تسجيل"), rec) { if (rec) p.onStopRecording() else p.onStartRecording() })
        add(McAct("◷", tr("Schedule", "جدولة")) { p.onScheduleRecording() })
        add(McAct("◷", tr("Daily", "يومي")) { p.onScheduleDailyRecording() })
        add(McAct("◷", tr("Weekly", "أسبوعي")) { p.onScheduleWeeklyRecording() })
        add(McAct("▭", p.aspectRatioLabel) { p.onToggleAspectRatio() })
        add(McAct("»", "${p.playbackSpeed}×") { p.onOpenPlaybackSpeed() })
        if (p.audioVideoSyncEnabled) add(McAct("⇄", tr("A/V sync", "مزامنة")) { p.onOpenAudioVideoSync() })
        add(McAct("◫", tr("Multiview", "تقسيم")) { p.onOpenSplitScreen() })
        add(McAct("⏲", if (p.sleepTimerUiState.stopTimerActive) mcDuration(p.sleepTimerUiState.stopRemainingMs) else tr("Sleep", "مؤقت"), p.sleepTimerUiState.stopTimerActive) { p.onOpenStopPlaybackTimer() })
        add(McAct("☾", if (p.sleepTimerUiState.idleTimerActive) mcDuration(p.sleepTimerUiState.idleRemainingMs) else tr("Standby", "استعداد"), p.sleepTimerUiState.idleTimerActive) { p.onOpenIdleStandbyTimer() })
        add(McAct("⧉", "PiP") { p.onEnterPictureInPicture() })
        add(McAct("⎚", if (p.isCastConnected) tr("Stop cast", "إيقاف البث") else tr("Cast", "بث"), p.isCastConnected) { if (p.isCastConnected) p.onStopCasting() else p.onCast() })
        add(McAct("✕", tr("Close", "إغلاق")) { p.onClose() })
    }
    Box(p.modifier.fillMaxSize()) {
        // top OSD: channel head + clock on a walnut band with brass underline
        Column(Modifier.align(Alignment.TopCenter).fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().background(MC.Bg.copy(alpha = 0.92f)).padding(horizontal = 40.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) { ChannelHead(p.currentChannel, p.currentChannelName, p.displayChannelNumber, p.resolutionBadgeLabel, p.timeshiftUiState.enabledForSession && p.timeshiftUiState.bufferedBehindLiveMs > 0) }
                if (rec) McBadge("● REC", MC.Live, filled = true)
                Text("  " + mcClock(System.currentTimeMillis()), color = MC.Amber, fontSize = 30.sp, fontFamily = MC.Serif)
            }
            Box(Modifier.fillMaxWidth().height(2.dp).background(MC.Amber))
        }
        // bottom front panel: now/next + full-width key strip
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
            Box(Modifier.fillMaxWidth().height(2.dp).background(MC.Amber))
            Column(Modifier.fillMaxWidth().background(MC.Bg.copy(alpha = 0.94f)).padding(horizontal = 40.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                NowNext(p.currentProgram, p.nextProgram, Modifier.fillMaxWidth())
                LazyRow(Modifier.fillMaxWidth().focusRequester(p.quickActionsFocusRequester), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(acts.size) { i -> McCtl(acts[i], if (i == 0) Modifier.focusRequester(p.playButtonFocusRequester) else Modifier) }
                }
            }
        }
    }
}

/** VOD: serif title above a brass-ruled front panel: inline back/-10/play/+10 keys + scrubber, option keys below, setup menu from START. */
@Composable
private fun McVod(p: PlayerOverlayParams) {
    var sheet by remember { mutableStateOf(false) }
    val sheetFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { p.playButtonFocusRequester.requestFocus() } }
    LaunchedEffect(sheet) { if (sheet) runCatching { sheetFocus.requestFocus() } }
    fun seekBy(d: Long) { p.onUserInteraction(); p.onSeekToPosition((p.currentPosition + d).coerceIn(0L, (p.duration - 1_000L).coerceAtLeast(0L))) }
    Box(p.modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.85f))))) {
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 40.dp, vertical = 10.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Column(Modifier.weight(1f)) {
                    McHeading(if (p.isCatchUpPlayback) tr("Catch-up", "من الأرشيف") else tr("Now playing", "يعرض الآن"), size = 11)
                    Text(p.displayTitle, color = MC.Text, fontSize = 30.sp, fontFamily = MC.Serif, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    p.episodeLine?.let { Text(it, color = MC.Sub, fontSize = 14.sp, maxLines = 1) }
                }
                if (p.currentRecordingStatus == RecordingStatus.RECORDING) McBadge("● REC", MC.Live, filled = true)
                if (p.playbackSpeed != 1f) McBadge("${p.playbackSpeed}×", MC.Amber)
                Text(mcClock(System.currentTimeMillis()), color = MC.Amber, fontSize = 24.sp, fontFamily = MC.Serif)
            }
            Box(Modifier.fillMaxWidth().height(2.dp).background(MC.Amber))
        Column(Modifier.fillMaxWidth().background(MC.Bg.copy(alpha = 0.94f)).padding(horizontal = 40.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                McCtl(McAct("←", tr("Back", "رجوع")) { p.onNavigateBack() })
                McCtl(McAct("↺", "-10s") { seekBy(-STEP) })
                McCtl(McAct(if (p.isPlaying) "❚❚" else "▶", if (p.isPlaying) tr("Pause", "إيقاف") else tr("Play", "تشغيل"), true) { p.onUserInteraction(); p.onTogglePlayPause() }, Modifier.focusRequester(p.playButtonFocusRequester))
                McCtl(McAct("↻", "+10s") { seekBy(STEP) })
                Box(Modifier.weight(1f).padding(start = 10.dp)) { Scrubber(p, ::seekBy) }
            }
            LazyRow(Modifier.focusRequester(p.quickActionsFocusRequester), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                item { McButton(tr("Audio", "الصوت") + " · ${p.audioTrackCount}", p.onOpenAudioTracks, icon = "♪") }
                item { McButton(tr("Subtitles", "الترجمة") + " · ${p.subtitleTrackCount}", p.onOpenSubtitleTracks, icon = "CC") }
                item { McButton(p.resolutionBadgeLabel ?: tr("Quality", "الجودة"), p.onOpenVideoTracks, icon = "HD") }
                if (p.showEpisodesAction) item { McButton(tr("Episodes", "الحلقات"), p.onOpenEpisodes, icon = "≣") }
                item { McButton(tr("Start over", "من البداية"), { p.onSeekToPosition(0L) }, icon = "⏮") }
                item { McButton(tr("More", "المزيد"), { sheet = !sheet }, primary = sheet, icon = "⚙") }
                item { McButton(tr("Close", "إغلاق"), p.onClose, icon = "✕") }
            }
        }
        }
        if (sheet) SideSheet(p, sheetFocus) { sheet = false }
    }
}

@Composable
private fun Scrubber(p: PlayerOverlayParams, seekBy: (Long) -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val pos = if (p.seekPreview.visible) p.seekPreview.positionMs else p.currentPosition
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        McCard(
            onClick = { p.onUserInteraction(); p.onTogglePlayPause() }, shape = MC.Pill, container = Color.Transparent, focusedContainer = Color.White.copy(alpha = 0.08f), zoom = 1f,
            modifier = Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused; p.onSetScrubbingMode(it.isFocused) }.onPreviewKeyEvent { e ->
                val n = e.nativeKeyEvent
                if (n.action != android.view.KeyEvent.ACTION_DOWN) return@onPreviewKeyEvent false
                when (n.keyCode) {
                    android.view.KeyEvent.KEYCODE_DPAD_LEFT -> { seekBy(-STEP); true }
                    android.view.KeyEvent.KEYCODE_DPAD_RIGHT -> { seekBy(STEP); true }
                    else -> false
                }
            }
        ) {
            Box(Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 10.dp)) {
                McProgress(if (p.duration > 0) (pos.toFloat() / p.duration).coerceIn(0f, 1f) else 0f, Modifier.fillMaxWidth(), if (focused) 8.dp else 4.dp)
            }
        }
        Row {
            Text(mcDuration(pos), color = if (p.seekPreview.visible) MC.Amber else MC.Text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Text("-" + mcDuration((p.duration - pos).coerceAtLeast(0)), color = MC.Sub, fontSize = 14.sp)
        }
    }
}

@Composable
private fun BoxScope.SideSheet(p: PlayerOverlayParams, focus: FocusRequester, onDismiss: () -> Unit) {
    val rows = buildList<Triple<String, String, () -> Unit>> {
        add(Triple(tr("Video quality", "جودة الفيديو"), p.resolutionBadgeLabel ?: "${p.videoQualityCount}", p.onOpenVideoTracks))
        add(Triple(tr("Audio track", "مسار الصوت"), "${p.audioTrackCount}", p.onOpenAudioTracks))
        add(Triple(tr("Subtitles", "الترجمات"), "${p.subtitleTrackCount}", p.onOpenSubtitleTracks))
        add(Triple(tr("Aspect ratio", "نسبة العرض"), p.aspectRatioLabel, p.onToggleAspectRatio))
        add(Triple(tr("Playback speed", "سرعة التشغيل"), "${p.playbackSpeed}×", p.onOpenPlaybackSpeed))
        if (p.audioVideoSyncEnabled) add(Triple(tr("Audio/video sync", "مزامنة الصوت"), "", p.onOpenAudioVideoSync))
        add(Triple(tr("Mute", "كتم الصوت"), if (p.isMuted) tr("On", "مفعل") else tr("Off", "معطل"), p.onToggleMute))
        add(Triple(tr("Sleep timer", "مؤقت النوم"), if (p.sleepTimerUiState.stopTimerActive) mcDuration(p.sleepTimerUiState.stopRemainingMs) else tr("Off", "معطل"), p.onOpenStopPlaybackTimer))
        add(Triple(tr("Standby timer", "مؤقت الاستعداد"), if (p.sleepTimerUiState.idleTimerActive) mcDuration(p.sleepTimerUiState.idleRemainingMs) else tr("Off", "معطل"), p.onOpenIdleStandbyTimer))
        add(Triple(tr("Multiview", "تقسيم الشاشة"), "", p.onOpenSplitScreen))
        add(Triple(tr("Picture in picture", "صورة داخل صورة"), "", p.onEnterPictureInPicture))
        add(Triple(tr("Cast", "البث"), if (p.isCastConnected) tr("Connected", "متصل") else "", if (p.isCastConnected) p.onStopCasting else p.onCast))
    }
    Column(
        Modifier.align(Alignment.CenterStart).fillMaxHeight().width(400.dp).background(MC.Bg.copy(alpha = 0.97f)).border(1.dp, MC.Amber).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            McHeading(tr("Playback setup", "إعداد التشغيل"), size = 14, modifier = Modifier.weight(1f))
            McRound("✕", onDismiss, size = 36.dp)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(rows.size) { i ->
                val (label, value, action) = rows[i]
                McCard(onClick = action, shape = MC.RSmall, container = Color.Transparent, focusedContainer = MC.Card, zoom = 1.02f, modifier = Modifier.fillMaxWidth().then(if (i == 0) Modifier.focusRequester(focus) else Modifier)) {
                    Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(label, color = MC.Text, fontSize = 15.sp, fontFamily = MC.Serif, modifier = Modifier.weight(1f), maxLines = 1)
                        Text(value, color = MC.Amber, fontSize = 14.sp, maxLines = 1)
                        Text("  ›", color = MC.Faint, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

/** Channel list: END-side walnut ledger panel, count header, ruled rows (number/logo/name/quality/archive/Now), now/next card bottom START. */
@Composable
internal fun MediaCenterLiveChannelList(p: LiveChannelListParams) {
    val idx = remember(p.channels, p.currentChannelId) { p.channels.indexOfFirst { it.id == p.currentChannelId }.coerceAtLeast(0) }
    val state = rememberLazyListState(idx)
    var focused by remember(p.currentChannelId) { mutableStateOf(p.channels.getOrNull(idx)) }
    LaunchedEffect(Unit) { runCatching { p.focusRequester.requestFocus() } }
    Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f), Color.Black.copy(alpha = 0.92f))))) {
        Column(Modifier.align(Alignment.TopEnd).fillMaxHeight().width(540.dp).background(MC.Bg.copy(alpha = 0.95f)).border(1.dp, MC.Line).padding(start = 18.dp, top = 24.dp, bottom = 18.dp, end = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    McHeading(tr("Channel list", "قائمة القنوات"), size = 11)
                    Text(p.lastVisitedCategoryName ?: tr("All channels", "كل القنوات"), color = MC.Text, fontSize = 22.sp, fontFamily = MC.Serif, maxLines = 1)
                    Text("${p.channels.size} " + tr("channels", "قناة"), color = MC.Faint, fontSize = 13.sp)
                }
                McRound("✕", p.onDismiss, size = 40.dp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                McTab(tr("Categories", "الفئات"), false, p.onOpenCategories)
                if (p.lastVisitedCategoryName != null) McTab(tr("Last group", "آخر مجموعة"), false, p.onOpenLastGroup)
                McTab(tr("Guide", "الدليل"), false, p.onOpenGuide)
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(MC.Amber))
            LazyColumn(state = state, verticalArrangement = Arrangement.spacedBy(1.dp), modifier = Modifier.weight(1f)) {
                items(p.channels, key = { it.id }) { c ->
                    val cur = c.id == p.currentChannelId
                    McCard(
                        onClick = { p.onInteracted(); p.onSelectChannel(c.id) }, onLongClick = { p.onChannelLongPress(c) }, shape = MC.RSmall, container = if (cur) Color(0xFF33281A) else Color.Transparent,
                        modifier = Modifier.fillMaxWidth().then(if (cur) Modifier.focusRequester(p.focusRequester) else Modifier).onFocusChanged { if (it.isFocused) { focused = c; p.onInteracted() } }
                    ) {
                        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("%03d".format(p.numberOf(c)), color = MC.Amber, fontSize = 14.sp, fontFamily = MC.Serif, modifier = Modifier.width(40.dp))
                            McLogo(c.name, c.logoUrl, 36.dp)
                            Column(Modifier.weight(1f)) {
                                Text(if (c.id == p.movingChannelId) "⇅  ${c.name}" else c.name, color = MC.Text, fontSize = 15.sp, fontFamily = MC.Serif, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                c.currentProgram?.let { Text(it.title, color = MC.Faint, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                            }
                            if (cur) McBadge(tr("Now", "الآن"), MC.Amber, filled = true)
                            c.qualityBadge()?.let { McBadge(it, MC.Text) }
                            if (c.catchUpSupported) McBadge("⟲ " + tr("Archive", "أرشيف"), MC.Blue)
                            Text(if (c.isFavorite) "★" else "", color = MC.Amber, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
        focused?.let { c ->
            Column(Modifier.align(Alignment.BottomStart).padding(40.dp).width(520.dp).background(MC.Bg.copy(alpha = 0.92f)).border(1.dp, MC.Amber, MC.RSmall).padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                McHeading(c.name, size = 12)
                NowNext(c.currentProgram, c.nextProgram)
            }
        }
    }
}

/** Zap banner: full-width bottom OSD band with brass rule, number box head, now/next and key strip. */
@Composable
internal fun MediaCenterLiveChannelInfo(p: LiveChannelInfoParams) {
    LaunchedEffect(Unit) { runCatching { p.focusRequester.requestFocus() } }
    val rec = p.currentRecordingStatus == RecordingStatus.RECORDING
    // Alaa's required order first: Channels, Favorites, Audio, Aspect, Settings, Quality, Subs, Guide.
    val acts = buildList {
        add(McAct("☰", tr("Channels", "القنوات"), true) { p.onInteracted(); p.onOpenChannelList() })
        add(McAct(if (p.channel?.isFavorite == true) "♥" else "♡", tr("Favorites", "المفضلة"), p.channel?.isFavorite == true) { p.onInteracted(); p.onToggleFavorite() })
        add(McAct("♪", tr("Audio", "الصوت")) { p.onInteracted(); p.onOpenAudioTracks() })
        add(McAct("▭", tr("Aspect", "العرض") + " " + p.aspectRatioLabel) { p.onInteracted(); p.onToggleAspectRatio() })
        add(McAct("⚙", tr("Settings", "الإعدادات")) { p.onInteracted(); p.onOpenSettings() })
        add(McAct("HD", p.resolutionLabel ?: tr("Quality", "الجودة")) { p.onInteracted(); p.onOpenVideoTracks() })
        add(McAct("CC", tr("Subs", "الترجمة")) { p.onInteracted(); p.onOpenSubtitleTracks() })
        add(McAct("▦", tr("Guide", "الدليل")) { p.onInteracted(); p.onOpenFullEpg() })
        add(McAct(if (p.isPlaying) "❚❚" else "▶", if (p.isPlaying) tr("Pause", "إيقاف") else tr("Play", "تشغيل")) { p.onInteracted(); p.onTogglePlayPause() })
        add(McAct(if (p.isMuted) "🔇" else "🔊", tr("Mute", "كتم"), p.isMuted) { p.onInteracted(); p.onToggleMute() })
        if (p.variantCount > 1) add(McAct("⇋", tr("Source", "المصدر")) { p.onInteracted(); p.onOpenVariants() })
        add(McAct("⏮", tr("Restart", "من البداية")) { p.onInteracted(); p.onRestartProgram() })
        add(McAct("⟲", tr("Catch-up", "الأرشيف")) { p.onInteracted(); p.onOpenArchive() })
        if (p.canSeekToLive) add(McAct("⇥", tr("Go live", "مباشر")) { p.onInteracted(); p.onSeekToLiveEdge() })
        add(McAct(if (rec) "■" else "●", if (rec) tr("Stop rec", "إيقاف") else tr("Record", "تسجيل"), rec) { p.onInteracted(); if (rec) p.onStopRecording() else p.onStartRecording() })
        add(McAct("◷", tr("Schedule", "جدولة")) { p.onInteracted(); p.onScheduleRecording() })
        add(McAct("⇄", "A/V") { p.onInteracted(); p.onOpenAudioVideoSync() })
        add(McAct("◫", tr("Multiview", "تقسيم")) { p.onInteracted(); p.onOpenSplitScreen() })
        add(McAct("∿", tr("Stats", "إحصاءات"), p.isDiagnosticsEnabled) { p.onInteracted(); p.onToggleDiagnostics() })
        add(McAct("⧉", "PiP") { p.onEnterPictureInPicture() })
        add(McAct("⎚", if (p.isCastConnected) tr("Stop cast", "إيقاف البث") else tr("Cast", "بث"), p.isCastConnected) { if (p.isCastConnected) p.onStopCasting() else p.onCast() })
    }
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(MC.Bg.copy(alpha = 0.94f)),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(Modifier.fillMaxWidth().height(2.dp).background(MC.Amber))
            Column(Modifier.padding(horizontal = 40.dp).padding(bottom = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                ChannelHead(p.channel, null, p.displayChannelNumber, p.resolutionLabel, false)
                NowNext(p.currentProgram, p.nextProgram, Modifier.weight(1f))
                Text(mcClock(System.currentTimeMillis()), color = MC.Amber, fontSize = 28.sp, fontFamily = MC.Serif)
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                items(acts.size) { i -> McCtl(acts[i], if (i == 0) Modifier.focusRequester(p.focusRequester) else Modifier, size = 46.dp) }
            }
            }
        }
    }
}
