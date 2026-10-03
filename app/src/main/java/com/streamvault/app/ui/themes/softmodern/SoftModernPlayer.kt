package com.streamvault.app.ui.themes.softmodern

import com.streamvault.app.ui.themes.bespoke.InnerPanelBackScope
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
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

private class SmAct(val glyph: String, val label: String, val active: Boolean = false, val onClick: () -> Unit)

/** Circular icon with caption under it: SoftModern's player control. */
@Composable
private fun SmCtl(a: SmAct, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 52.dp) {
    Column(modifier.width(size + 22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        SmRound(a.glyph, a.onClick, size = size, active = a.active)
        Text(a.label, color = SM.Sub, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun NowNext(now: Program?, next: Program?, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (now != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SmBadge(tr("NOW", "الآن"), SM.Amber, filled = true)
                Text(now.title, color = SM.Text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, false))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(smClock(now.startTime), color = SM.Sub, fontSize = 12.sp)
                SmProgress(now.progressAt(), Modifier.weight(1f))
                Text(smClock(now.endTime), color = SM.Sub, fontSize = 12.sp)
            }
        } else Text(tr("No programme information", "لا توجد معلومات عن البرنامج"), color = SM.Faint, fontSize = 14.sp)
        next?.let { Text(tr("Next", "التالي") + "  ${smClock(it.startTime)}  ·  ${it.title}", color = SM.Faint, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
    }
}

@Composable
private fun ChannelHead(channel: Channel?, name: String?, number: Int, resolution: String?, timeshift: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        SmLogo(channel?.name ?: name.orEmpty(), channel?.logoUrl, 64.dp)
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (number > 0) Text("$number", color = SM.Amber, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text(channel?.name ?: name.orEmpty(), color = SM.Text, fontSize = 24.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                if (channel?.isFavorite == true) Text("♥", color = SM.Amber, fontSize = 16.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SmBadge(if (timeshift) tr("PAUSED LIVE", "مباشر مؤجل") else "● LIVE", SM.Live, filled = !timeshift)
                (resolution ?: channel?.qualityBadge())?.let { SmBadge(it, SM.Text) }
                if (channel?.catchUpSupported == true) SmBadge(tr("CATCH-UP", "أرشيف"), SM.Blue)
            }
        }
    }
}



@Composable
internal fun SoftModernPlayerOverlay(p: PlayerOverlayParams) {
    if (!p.visible) return
    if (p.isLive && !p.isCatchUpPlayback) SmLive(p) else SmVod(p)
}

/** Live (Soft Modern): floating channel pebble + clock pill on top, cream console card at the bottom with now/next and a sage control strip; channel head + now/next on the start, clock on the end, one row of round controls. */
@Composable
private fun SmLive(p: PlayerOverlayParams) {
    LaunchedEffect(Unit) { runCatching { p.playButtonFocusRequester.requestFocus() } }
    val rec = p.currentRecordingStatus == RecordingStatus.RECORDING
    val acts = buildList {
        add(SmAct(if (p.isPlaying) "❚❚" else "▶", if (p.isPlaying) tr("Pause", "إيقاف") else tr("Play", "تشغيل"), true) { p.onUserInteraction(); p.onTogglePlayPause() })
        add(SmAct("☰", tr("Channels", "القنوات")) { p.onOpenLiveChannels() })
        add(SmAct("▦", tr("Guide", "الدليل")) { p.onOpenLiveGuide() })
        add(SmAct(if (p.currentChannel?.isFavorite == true) "♥" else "♡", tr("My List", "قائمتي"), p.currentChannel?.isFavorite == true) { p.onToggleLiveFavorite() })
        add(SmAct("⏪", "-10s") { p.onUserInteraction(); p.onSeekBackward() })
        add(SmAct("⏩", "+10s") { p.onUserInteraction(); p.onSeekForward() })
        add(SmAct("⏮", tr("Restart", "من البداية")) { p.onRestartProgram() })
        add(SmAct("⟲", tr("Catch-up", "الأرشيف")) { p.onOpenArchive() })
        if (p.timeshiftUiState.canSeekToLive) add(SmAct("⇥", tr("Go live", "مباشر")) { p.onSeekToLiveEdge() })
        add(SmAct("CC", tr("Subtitles", "الترجمة") + " ${p.subtitleTrackCount}") { p.onOpenSubtitleTracks() })
        add(SmAct("♪", tr("Audio", "الصوت") + " ${p.audioTrackCount}") { p.onOpenAudioTracks() })
        add(SmAct("HD", p.resolutionBadgeLabel ?: tr("Quality", "الجودة")) { p.onOpenVideoTracks() })
        add(SmAct(if (p.isMuted) "🔇" else "🔊", tr("Mute", "كتم"), p.isMuted) { p.onToggleMute() })
        add(SmAct(if (rec) "■" else "●", if (rec) tr("Stop rec", "إيقاف التسجيل") else tr("Record", "تسجيل"), rec) { if (rec) p.onStopRecording() else p.onStartRecording() })
        add(SmAct("◷", tr("Schedule", "جدولة")) { p.onScheduleRecording() })
        add(SmAct("◷", tr("Daily", "يومي")) { p.onScheduleDailyRecording() })
        add(SmAct("◷", tr("Weekly", "أسبوعي")) { p.onScheduleWeeklyRecording() })
        add(SmAct("▭", p.aspectRatioLabel) { p.onToggleAspectRatio() })
        add(SmAct("»", "${p.playbackSpeed}×") { p.onOpenPlaybackSpeed() })
        if (p.audioVideoSyncEnabled) add(SmAct("⇄", tr("A/V sync", "مزامنة")) { p.onOpenAudioVideoSync() })
        add(SmAct("◫", tr("Multiview", "تقسيم")) { p.onOpenSplitScreen() })
        add(SmAct("⏲", if (p.sleepTimerUiState.stopTimerActive) smDuration(p.sleepTimerUiState.stopRemainingMs) else tr("Sleep", "مؤقت"), p.sleepTimerUiState.stopTimerActive) { p.onOpenStopPlaybackTimer() })
        add(SmAct("☾", if (p.sleepTimerUiState.idleTimerActive) smDuration(p.sleepTimerUiState.idleRemainingMs) else tr("Standby", "استعداد"), p.sleepTimerUiState.idleTimerActive) { p.onOpenIdleStandbyTimer() })
        add(SmAct("⧉", "PiP") { p.onEnterPictureInPicture() })
        add(SmAct("⎚", if (p.isCastConnected) tr("Stop cast", "إيقاف البث") else tr("Cast", "بث"), p.isCastConnected) { if (p.isCastConnected) p.onStopCasting() else p.onCast() })
        add(SmAct("✕", tr("Close", "إغلاق")) { p.onClose() })
    }
    Box(p.modifier.fillMaxSize()) {
        // Top-start floating channel pebble + clock pill top-end; bottom: cream console card with now/next and sage control strip.
        Row(Modifier.align(Alignment.TopStart).fillMaxWidth().padding(horizontal = 48.dp, vertical = 28.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.clip(SM.R).background(SM.Bg.copy(alpha = 0.95f)).padding(horizontal = 18.dp, vertical = 12.dp)) {
                ChannelHead(p.currentChannel, p.currentChannelName, p.displayChannelNumber, p.resolutionBadgeLabel, p.timeshiftUiState.enabledForSession && p.timeshiftUiState.bufferedBehindLiveMs > 0)
            }
            Spacer(Modifier.weight(1f))
            if (rec) SmBadge("● REC", SM.Live, filled = true)
            Spacer(Modifier.width(10.dp))
            Box(Modifier.clip(SM.Pill).background(SM.Bg.copy(alpha = 0.95f)).padding(horizontal = 18.dp, vertical = 8.dp)) {
                Text(smClock(System.currentTimeMillis()), color = SM.AmberDeep, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 48.dp, vertical = 28.dp).clip(SM.R).background(SM.Bg.copy(alpha = 0.96f)).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NowNext(p.currentProgram, p.nextProgram, Modifier.fillMaxWidth())
            LazyRow(Modifier.fillMaxWidth().clip(SM.Pill).background(SM.Sage).padding(6.dp).focusRequester(p.quickActionsFocusRequester), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                items(acts.size) { i -> SmCtl(acts[i], if (i == 0) Modifier.focusRequester(p.playButtonFocusRequester) else Modifier, size = 46.dp) }
            }
        }
    }
}

/** VOD (Soft Modern): cream title pill on top, transport in a cream capsule, bottom sheet with scrubber, options drawer on the START side; giant centered transport (-10 / play / +10), thin amber scrubber, pill row + side sheet. */
@Composable
private fun SmVod(p: PlayerOverlayParams) {
    var sheet by remember { mutableStateOf(false) }
    val sheetFocus = remember { FocusRequester() }
    val moreFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { p.playButtonFocusRequester.requestFocus() } }
    LaunchedEffect(sheet) { if (sheet) runCatching { sheetFocus.requestFocus() } }
    fun seekBy(d: Long) { p.onUserInteraction(); p.onSeekToPosition((p.currentPosition + d).coerceIn(0L, (p.duration - 1_000L).coerceAtLeast(0L))) }
    Box(p.modifier.fillMaxSize()) {
        Row(Modifier.align(Alignment.TopStart).fillMaxWidth().padding(horizontal = 48.dp, vertical = 28.dp).clip(SM.Pill).background(SM.Bg.copy(alpha = 0.95f)).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SmRound("←", { p.onNavigateBack() }, size = 44.dp)
            Column(Modifier.weight(1f)) {
                if (p.isCatchUpPlayback) Text(tr("Catch-up", "من الأرشيف"), color = SM.Amber, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(p.displayTitle, color = SM.Text, fontSize = 28.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                p.episodeLine?.let { Text(it, color = SM.Sub, fontSize = 15.sp, maxLines = 1) }
            }
            if (p.currentRecordingStatus == RecordingStatus.RECORDING) SmBadge("● REC", SM.Live, filled = true)
            if (p.playbackSpeed != 1f) SmBadge("${p.playbackSpeed}×", SM.Amber)
            Text(smClock(System.currentTimeMillis()), color = SM.Sub, fontSize = 20.sp)
        }
        Row(Modifier.align(Alignment.Center).clip(SM.Pill).background(SM.Bg.copy(alpha = 0.9f)).padding(horizontal = 28.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(40.dp)) {
            SmCtl(SmAct("↺", "10s") { seekBy(-STEP) }, size = 64.dp)
            SmCtl(SmAct(if (p.isPlaying) "❚❚" else "▶", if (p.isPlaying) tr("Pause", "إيقاف") else tr("Play", "تشغيل"), true) { p.onUserInteraction(); p.onTogglePlayPause() }, Modifier.focusRequester(p.playButtonFocusRequester), size = 92.dp)
            SmCtl(SmAct("↻", "10s") { seekBy(STEP) }, size = 64.dp)
        }
        Column(Modifier.align(Alignment.BottomEnd).fillMaxWidth(if (sheet) 0.64f else 1f).padding(horizontal = 48.dp, vertical = 28.dp).clip(SM.R).background(SM.Bg.copy(alpha = 0.96f)).padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Scrubber(p, ::seekBy)
            LazyRow(Modifier.focusRequester(p.quickActionsFocusRequester), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item { SmButton(tr("Audio", "الصوت") + " · ${p.audioTrackCount}", p.onOpenAudioTracks, icon = "♪") }
                item { SmButton(tr("Subtitles", "الترجمة") + " · ${p.subtitleTrackCount}", p.onOpenSubtitleTracks, icon = "CC") }
                item { SmButton(p.resolutionBadgeLabel ?: tr("Quality", "الجودة"), p.onOpenVideoTracks, icon = "HD") }
                if (p.showEpisodesAction) item { SmButton(tr("Episodes", "الحلقات"), p.onOpenEpisodes, icon = "≣") }
                item { SmButton(tr("Start over", "من البداية"), { p.onSeekToPosition(0L) }, icon = "⏮") }
                item { androidx.compose.foundation.layout.Box(Modifier.focusRequester(moreFocus)) { SmButton(tr("More", "المزيد"), { sheet = !sheet }, primary = sheet, icon = "⚙") } }
                item { SmButton(tr("Close", "إغلاق"), p.onClose, icon = "✕") }
            }
        }
        if (sheet) InnerPanelBackScope(onClose = { sheet = false }, opener = moreFocus) { SideSheet(p, sheetFocus) { sheet = false; runCatching { moreFocus.requestFocus() } } }
    }
}

@Composable
private fun Scrubber(p: PlayerOverlayParams, seekBy: (Long) -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val pos = if (p.seekPreview.visible) p.seekPreview.positionMs else p.currentPosition
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SmCard(
            onClick = { p.onUserInteraction(); p.onTogglePlayPause() }, shape = SM.Pill, container = Color.Transparent, focusedContainer = Color.White.copy(alpha = 0.08f), zoom = 1f,
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
                SmProgress(if (p.duration > 0) (pos.toFloat() / p.duration).coerceIn(0f, 1f) else 0f, Modifier.fillMaxWidth(), if (focused) 8.dp else 4.dp)
            }
        }
        Row {
            Text(smDuration(pos), color = if (p.seekPreview.visible) SM.Amber else SM.Text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Text("-" + smDuration((p.duration - pos).coerceAtLeast(0)), color = SM.Sub, fontSize = 14.sp)
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
        add(Triple(tr("Sleep timer", "مؤقت النوم"), if (p.sleepTimerUiState.stopTimerActive) smDuration(p.sleepTimerUiState.stopRemainingMs) else tr("Off", "معطل"), p.onOpenStopPlaybackTimer))
        add(Triple(tr("Standby timer", "مؤقت الاستعداد"), if (p.sleepTimerUiState.idleTimerActive) smDuration(p.sleepTimerUiState.idleRemainingMs) else tr("Off", "معطل"), p.onOpenIdleStandbyTimer))
        add(Triple(tr("Multiview", "تقسيم الشاشة"), "", p.onOpenSplitScreen))
        add(Triple(tr("Picture in picture", "صورة داخل صورة"), "", p.onEnterPictureInPicture))
        add(Triple(tr("Cast", "البث"), if (p.isCastConnected) tr("Connected", "متصل") else "", if (p.isCastConnected) p.onStopCasting else p.onCast))
    }
    Column(
        Modifier.align(Alignment.CenterStart).padding(24.dp).fillMaxHeight(0.9f).width(380.dp).clip(SM.R).background(SM.Raised.copy(alpha = 0.97f)).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(tr("Playback options", "خيارات التشغيل"), color = SM.Text, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            SmRound("✕", onDismiss, size = 36.dp)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(rows.size) { i ->
                val (label, value, action) = rows[i]
                SmCard(onClick = action, shape = SM.RSmall, container = Color.Transparent, focusedContainer = SM.Card, zoom = 1.02f, modifier = Modifier.fillMaxWidth().then(if (i == 0) Modifier.focusRequester(focus) else Modifier)) {
                    Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(label, color = SM.Text, fontSize = 15.sp, modifier = Modifier.weight(1f), maxLines = 1)
                        Text(value, color = SM.Amber, fontSize = 14.sp, maxLines = 1)
                        Text("  ›", color = SM.Faint, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

/** Channel list: cream panel floating on the END side, round logos, sage highlight for the playing row, now/next card docked at its foot. */
@Composable
internal fun SoftModernLiveChannelList(p: LiveChannelListParams) {
    val idx = remember(p.channels, p.currentChannelId) { p.channels.indexOfFirst { it.id == p.currentChannelId }.coerceAtLeast(0) }
    val state = rememberLazyListState(idx)
    var focused by remember(p.currentChannelId) { mutableStateOf(p.channels.getOrNull(idx)) }
    LaunchedEffect(Unit) { runCatching { p.focusRequester.requestFocus() } }
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.align(Alignment.CenterEnd).padding(24.dp).fillMaxHeight().width(500.dp).clip(SM.R).background(SM.Bg.copy(alpha = 0.97f)).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.clip(SM.Pill).background(SM.Amber).padding(horizontal = 12.dp, vertical = 4.dp)) { Text("${p.channels.size}", color = SM.Card, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                Text(p.lastVisitedCategoryName ?: tr("All channels", "كل القنوات"), color = SM.Text, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.weight(1f))
                SmRound("✕", p.onDismiss, size = 36.dp)
            }
            Row(Modifier.clip(SM.Pill).background(SM.Sage).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                SmTab(tr("Categories", "الفئات"), false, p.onOpenCategories)
                if (p.lastVisitedCategoryName != null) SmTab(tr("Last group", "آخر مجموعة"), false, p.onOpenLastGroup)
                SmTab(tr("Guide", "الدليل"), false, p.onOpenGuide)
            }
            LazyColumn(state = state, verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f), contentPadding = PaddingValues(4.dp)) {
                items(p.channels, key = { it.id }) { c ->
                    val cur = c.id == p.currentChannelId
                    SmCard(
                        onClick = { p.onInteracted(); p.onSelectChannel(c.id) }, onLongClick = { p.onChannelLongPress(c) }, shape = SM.Pill, zoom = 1.03f,
                        container = if (cur) SM.Sage else SM.Card, focusedContainer = SM.Card,
                        modifier = Modifier.fillMaxWidth().then(if (cur) Modifier.focusRequester(p.focusRequester) else Modifier).onFocusChanged { if (it.isFocused) { focused = c; p.onInteracted() } }
                    ) {
                        Row(Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(Modifier.size(40.dp).clip(CircleShape).background(SM.Raised), contentAlignment = Alignment.Center) { SmLogo(c.name, c.logoUrl, 30.dp) }
                            Text("${p.numberOf(c)}", color = SM.Faint, fontSize = 13.sp, modifier = Modifier.width(32.dp))
                            Text(if (c.id == p.movingChannelId) "⇅  ${c.name}" else c.name, color = SM.Text, fontSize = 15.sp, fontWeight = if (cur) FontWeight.Bold else FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            if (cur) SmBadge(tr("Now", "الآن"), SM.Amber, filled = true)
                            c.qualityBadge()?.let { SmBadge(it, SM.Sub) }
                            if (c.catchUpSupported) SmBadge("⟲", SM.Blue)
                            if (c.isFavorite) Text("♥", color = SM.Amber, fontSize = 14.sp)
                        }
                    }
                }
            }
            focused?.let { c ->
                Column(Modifier.fillMaxWidth().clip(SM.RSmall).background(SM.Card).padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(c.name, color = SM.Text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    NowNext(c.currentProgram, c.nextProgram)
                }
            }
        }
    }
}

/** Zap banner (Soft Modern): cream card with a sage pill action strip, at the bottom with channel head, now/next and a compact round-control row. */
@Composable
internal fun SoftModernLiveChannelInfo(p: LiveChannelInfoParams) {
    LaunchedEffect(Unit) { runCatching { p.focusRequester.requestFocus() } }
    val rec = p.currentRecordingStatus == RecordingStatus.RECORDING
    // Alaa's required order first: Channels, Favorites, Audio, Aspect, Settings, Quality, Subs, Guide.
    val acts = buildList {
        add(SmAct("☰", tr("Channels", "القنوات"), true) { p.onInteracted(); p.onOpenChannelList() })
        add(SmAct(if (p.channel?.isFavorite == true) "♥" else "♡", tr("Favorites", "المفضلة"), p.channel?.isFavorite == true) { p.onInteracted(); p.onToggleFavorite() })
        add(SmAct("♪", tr("Audio", "الصوت")) { p.onInteracted(); p.onOpenAudioTracks() })
        add(SmAct("▭", tr("Aspect", "العرض") + " " + p.aspectRatioLabel) { p.onInteracted(); p.onToggleAspectRatio() })
        add(SmAct("⚙", tr("Settings", "الإعدادات")) { p.onInteracted(); p.onOpenSettings() })
        add(SmAct("HD", p.resolutionLabel ?: tr("Quality", "الجودة")) { p.onInteracted(); p.onOpenVideoTracks() })
        add(SmAct("CC", tr("Subs", "الترجمة")) { p.onInteracted(); p.onOpenSubtitleTracks() })
        add(SmAct("▦", tr("Guide", "الدليل")) { p.onInteracted(); p.onOpenFullEpg() })
        add(SmAct(if (p.isPlaying) "❚❚" else "▶", if (p.isPlaying) tr("Pause", "إيقاف") else tr("Play", "تشغيل")) { p.onInteracted(); p.onTogglePlayPause() })
        add(SmAct(if (p.isMuted) "🔇" else "🔊", tr("Mute", "كتم"), p.isMuted) { p.onInteracted(); p.onToggleMute() })
        if (p.variantCount > 1) add(SmAct("⇋", tr("Source", "المصدر")) { p.onInteracted(); p.onOpenVariants() })
        add(SmAct("⏮", tr("Restart", "من البداية")) { p.onInteracted(); p.onRestartProgram() })
        add(SmAct("⟲", tr("Catch-up", "الأرشيف")) { p.onInteracted(); p.onOpenArchive() })
        if (p.canSeekToLive) add(SmAct("⇥", tr("Go live", "مباشر")) { p.onInteracted(); p.onSeekToLiveEdge() })
        add(SmAct(if (rec) "■" else "●", if (rec) tr("Stop rec", "إيقاف") else tr("Record", "تسجيل"), rec) { p.onInteracted(); if (rec) p.onStopRecording() else p.onStartRecording() })
        add(SmAct("◷", tr("Schedule", "جدولة")) { p.onInteracted(); p.onScheduleRecording() })
        add(SmAct("⇄", "A/V") { p.onInteracted(); p.onOpenAudioVideoSync() })
        add(SmAct("◫", tr("Multiview", "تقسيم")) { p.onInteracted(); p.onOpenSplitScreen() })
        add(SmAct("∿", tr("Stats", "إحصاءات"), p.isDiagnosticsEnabled) { p.onInteracted(); p.onToggleDiagnostics() })
        add(SmAct("⧉", "PiP") { p.onEnterPictureInPicture() })
        add(SmAct("⎚", if (p.isCastConnected) tr("Stop cast", "إيقاف البث") else tr("Cast", "بث"), p.isCastConnected) { if (p.isCastConnected) p.onStopCasting() else p.onCast() })
    }
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.align(Alignment.BottomCenter).padding(horizontal = 48.dp, vertical = 28.dp).fillMaxWidth().clip(SM.R).background(SM.Bg.copy(alpha = 0.96f)).border(1.dp, SM.Line, SM.R).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                ChannelHead(p.channel, null, p.displayChannelNumber, p.resolutionLabel, false)
                NowNext(p.currentProgram, p.nextProgram, Modifier.weight(1f))
                Box(Modifier.clip(SM.Pill).background(SM.Card).padding(horizontal = 16.dp, vertical = 8.dp)) { Text(smClock(System.currentTimeMillis()), color = SM.AmberDeep, fontSize = 22.sp, fontWeight = FontWeight.SemiBold) }
            }
            LazyRow(Modifier.fillMaxWidth().clip(SM.Pill).background(SM.Sage).padding(6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                items(acts.size) { i -> SmCtl(acts[i], if (i == 0) Modifier.focusRequester(p.focusRequester) else Modifier, size = 44.dp) }
            }
        }
    }
}
