package com.streamvault.app.ui.themes.sportstv

import com.streamvault.app.ui.themes.bespoke.InnerPanelBackScope
import androidx.compose.foundation.background
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

private class StAct(val glyph: String, val label: String, val active: Boolean = false, val onClick: () -> Unit)

/** Square broadcast chip: glyph block on top, uppercase caption plate under it. */
@Composable
private fun StCtl(a: StAct, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 52.dp) {
    StCard(onClick = a.onClick, shape = ST.RSmall, zoom = 1.06f, container = if (a.active) ST.Amber.copy(alpha = 0.3f) else ST.Raised.copy(alpha = 0.92f), focusedContainer = ST.Amber, modifier = modifier.width(size + 26.dp)) {
        Column(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(a.glyph, color = if (a.active) ST.Amber else ST.Text, fontSize = (size.value * 0.36f).sp, fontWeight = FontWeight.Black, maxLines = 1)
            Text(a.label.uppercase(), color = ST.Sub, fontSize = 9.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/** Scoreboard now/next: NOW plate + title, clock-bar with start/end, NEXT ticker line. */
@Composable
private fun NowNext(now: Program?, next: Program?, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        if (now != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.background(ST.Amber).padding(horizontal = 8.dp, vertical = 3.dp)) { Text(tr("NOW", "الآن"), color = ST.Bg, fontSize = 11.sp, fontWeight = FontWeight.Black) }
                Text(now.title.uppercase(), color = ST.Text, fontSize = 16.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, false).padding(start = 10.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stClock(now.startTime), color = ST.Amber, fontSize = 12.sp, fontWeight = FontWeight.Black)
                StProgress(now.progressAt(), Modifier.weight(1f), 4.dp)
                Text(stClock(now.endTime), color = ST.Sub, fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
        } else Text(tr("No programme information", "لا توجد معلومات عن البرنامج").uppercase(), color = ST.Faint, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        next?.let {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.background(ST.Line).padding(horizontal = 8.dp, vertical = 2.dp)) { Text(tr("NEXT", "التالي") + " " + stClock(it.startTime), color = ST.Text, fontSize = 10.sp, fontWeight = FontWeight.Black) }
                Text(it.title, color = ST.Sub, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

/** Lower-third channel plate: number block, logo, name plate in lime, badges strip. */
@Composable
private fun ChannelHead(channel: Channel?, name: String?, number: Int, resolution: String?, timeshift: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(64.dp).background(ST.Amber), contentAlignment = Alignment.Center) {
            Text(if (number > 0) "$number" else "–", color = ST.Bg, fontSize = 24.sp, fontWeight = FontWeight.Black)
        }
        Box(Modifier.size(64.dp).background(ST.Raised), contentAlignment = Alignment.Center) { StLogo(channel?.name ?: name.orEmpty(), channel?.logoUrl, 48.dp) }
        Column(Modifier.background(ST.Bg.copy(alpha = 0.92f)).height(64.dp).padding(horizontal = 14.dp), verticalArrangement = Arrangement.Center) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text((channel?.name ?: name.orEmpty()).uppercase(), color = ST.Text, fontSize = 20.sp, fontWeight = FontWeight.Black, maxLines = 1)
                if (channel?.isFavorite == true) Text("★", color = ST.Amber, fontSize = 16.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                StBadge(if (timeshift) tr("PAUSED LIVE", "مباشر مؤجل") else "● LIVE", ST.Live, filled = !timeshift)
                (resolution ?: channel?.qualityBadge())?.let { StBadge(it, ST.Text) }
                if (channel?.catchUpSupported == true) StBadge(tr("ARCHIVE", "أرشيف"), ST.Blue)
            }
        }
    }
}

@Composable
internal fun SportsTvPlayerOverlay(p: PlayerOverlayParams) {
    if (!p.visible) return
    if (p.isLive && !p.isCatchUpPlayback) StLive(p) else StVod(p)
}

/** Live: soft bottom gradient, channel head + now/next on the start, clock on the end, one row of round controls. */
@Composable
private fun StLive(p: PlayerOverlayParams) {
    LaunchedEffect(Unit) { runCatching { p.playButtonFocusRequester.requestFocus() } }
    val rec = p.currentRecordingStatus == RecordingStatus.RECORDING
    val acts = buildList {
        add(StAct(if (p.isPlaying) "❚❚" else "▶", if (p.isPlaying) tr("Pause", "إيقاف") else tr("Play", "تشغيل"), true) { p.onUserInteraction(); p.onTogglePlayPause() })
        add(StAct("☰", tr("Channels", "القنوات")) { p.onOpenLiveChannels() })
        add(StAct("▦", tr("Guide", "الدليل")) { p.onOpenLiveGuide() })
        add(StAct(if (p.currentChannel?.isFavorite == true) "♥" else "♡", tr("My List", "قائمتي"), p.currentChannel?.isFavorite == true) { p.onToggleLiveFavorite() })
        add(StAct("⏪", "-10s") { p.onUserInteraction(); p.onSeekBackward() })
        add(StAct("⏩", "+10s") { p.onUserInteraction(); p.onSeekForward() })
        add(StAct("⏮", tr("Restart", "من البداية")) { p.onRestartProgram() })
        add(StAct("⟲", tr("Catch-up", "الأرشيف")) { p.onOpenArchive() })
        if (p.timeshiftUiState.canSeekToLive) add(StAct("⇥", tr("Go live", "مباشر")) { p.onSeekToLiveEdge() })
        add(StAct("CC", tr("Subtitles", "الترجمة") + " ${p.subtitleTrackCount}") { p.onOpenSubtitleTracks() })
        add(StAct("♪", tr("Audio", "الصوت") + " ${p.audioTrackCount}") { p.onOpenAudioTracks() })
        add(StAct("HD", p.resolutionBadgeLabel ?: tr("Quality", "الجودة")) { p.onOpenVideoTracks() })
        add(StAct(if (p.isMuted) "🔇" else "🔊", tr("Mute", "كتم"), p.isMuted) { p.onToggleMute() })
        add(StAct(if (rec) "■" else "●", if (rec) tr("Stop rec", "إيقاف التسجيل") else tr("Record", "تسجيل"), rec) { if (rec) p.onStopRecording() else p.onStartRecording() })
        add(StAct("◷", tr("Schedule", "جدولة")) { p.onScheduleRecording() })
        add(StAct("◷", tr("Daily", "يومي")) { p.onScheduleDailyRecording() })
        add(StAct("◷", tr("Weekly", "أسبوعي")) { p.onScheduleWeeklyRecording() })
        add(StAct("▭", p.aspectRatioLabel) { p.onToggleAspectRatio() })
        add(StAct("»", "${p.playbackSpeed}×") { p.onOpenPlaybackSpeed() })
        if (p.audioVideoSyncEnabled) add(StAct("⇄", tr("A/V sync", "مزامنة")) { p.onOpenAudioVideoSync() })
        add(StAct("◫", tr("Multiview", "تقسيم")) { p.onOpenSplitScreen() })
        add(StAct("⏲", if (p.sleepTimerUiState.stopTimerActive) stDuration(p.sleepTimerUiState.stopRemainingMs) else tr("Sleep", "مؤقت"), p.sleepTimerUiState.stopTimerActive) { p.onOpenStopPlaybackTimer() })
        add(StAct("☾", if (p.sleepTimerUiState.idleTimerActive) stDuration(p.sleepTimerUiState.idleRemainingMs) else tr("Standby", "استعداد"), p.sleepTimerUiState.idleTimerActive) { p.onOpenIdleStandbyTimer() })
        add(StAct("⧉", "PiP") { p.onEnterPictureInPicture() })
        add(StAct("⎚", if (p.isCastConnected) tr("Stop cast", "إيقاف البث") else tr("Cast", "بث"), p.isCastConnected) { if (p.isCastConnected) p.onStopCasting() else p.onCast() })
        add(StAct("✕", tr("Close", "إغلاق")) { p.onClose() })
    }
    Box(p.modifier.fillMaxSize()) {
        // corner score-bug: clock + REC
        Row(Modifier.align(Alignment.TopEnd).padding(28.dp)) {
            if (rec) Box(Modifier.background(ST.Live).padding(horizontal = 10.dp, vertical = 6.dp)) { Text("● REC", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black) }
            Box(Modifier.background(ST.Bg.copy(alpha = 0.9f)).padding(horizontal = 12.dp, vertical = 6.dp)) { Text(stClock(System.currentTimeMillis()), color = ST.Amber, fontSize = 18.sp, fontWeight = FontWeight.Black) }
        }
        Column(Modifier.align(Alignment.BottomStart).fillMaxWidth()) {
            Row(Modifier.padding(start = 40.dp, bottom = 10.dp, end = 40.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ChannelHead(p.currentChannel, p.currentChannelName, p.displayChannelNumber, p.resolutionBadgeLabel, p.timeshiftUiState.enabledForSession && p.timeshiftUiState.bufferedBehindLiveMs > 0)
                NowNext(p.currentProgram, p.nextProgram, Modifier.weight(1f).clip(ST.RSmall).background(ST.Bg.copy(alpha = 0.88f)).padding(10.dp))
            }
            // full-width scoreboard control bar
            Row(Modifier.fillMaxWidth().background(ST.Bg.copy(alpha = 0.96f)), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(6.dp).height(78.dp).background(ST.Amber))
                LazyRow(Modifier.weight(1f).focusRequester(p.quickActionsFocusRequester), horizontalArrangement = Arrangement.spacedBy(4.dp), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)) {
                    items(acts.size) { i -> StCtl(acts[i], if (i == 0) Modifier.focusRequester(p.playButtonFocusRequester) else Modifier, size = 44.dp) }
                }
            }
        }
    }
}

/** VOD: title top-left over a gradient, giant centered transport (-10 / play / +10), thin amber scrubber, pill row + side sheet. */
@Composable
private fun StVod(p: PlayerOverlayParams) {
    var sheet by remember { mutableStateOf(false) }
    val sheetFocus = remember { FocusRequester() }
    val moreFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { p.playButtonFocusRequester.requestFocus() } }
    LaunchedEffect(sheet) { if (sheet) runCatching { sheetFocus.requestFocus() } }
    fun seekBy(d: Long) { p.onUserInteraction(); p.onSeekToPosition((p.currentPosition + d).coerceIn(0L, (p.duration - 1_000L).coerceAtLeast(0L))) }
    Box(p.modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.75f), Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.9f))))) {
        Row(Modifier.align(Alignment.TopStart).fillMaxWidth().padding(horizontal = 40.dp, vertical = 28.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(0.dp)) {
            StRound("←", { p.onNavigateBack() }, size = 52.dp)
            Box(Modifier.height(52.dp).background(if (p.isCatchUpPlayback) ST.Blue else ST.Amber).padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
                Text(if (p.isCatchUpPlayback) tr("REPLAY", "أرشيف") else tr("VOD", "فيديو"), color = ST.Bg, fontSize = 14.sp, fontWeight = FontWeight.Black)
            }
            Column(Modifier.weight(1f).height(52.dp).background(ST.Bg.copy(alpha = 0.9f)).padding(horizontal = 14.dp), verticalArrangement = Arrangement.Center) {
                Text(p.displayTitle.uppercase(), color = ST.Text, fontSize = 20.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                p.episodeLine?.let { Text(it, color = ST.Sub, fontSize = 12.sp, maxLines = 1) }
            }
            Spacer(Modifier.width(10.dp))
            if (p.currentRecordingStatus == RecordingStatus.RECORDING) StBadge("● REC", ST.Live, filled = true)
            if (p.playbackSpeed != 1f) StBadge("${p.playbackSpeed}×", ST.Amber)
            Text(stClock(System.currentTimeMillis()), color = ST.Sub, fontSize = 20.sp)
        }
        Column(Modifier.align(Alignment.BottomEnd).fillMaxWidth(if (sheet) 0.66f else 1f).background(ST.Bg.copy(alpha = 0.95f)).padding(horizontal = 28.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Scrubber(p, ::seekBy)
            LazyRow(Modifier.focusRequester(p.quickActionsFocusRequester), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                item { StCtl(StAct("↺", "-10s") { seekBy(-STEP) }, size = 44.dp) }
                item { StCtl(StAct(if (p.isPlaying) "❚❚" else "▶", if (p.isPlaying) tr("Pause", "إيقاف") else tr("Play", "تشغيل"), true) { p.onUserInteraction(); p.onTogglePlayPause() }, Modifier.focusRequester(p.playButtonFocusRequester), size = 56.dp) }
                item { StCtl(StAct("↻", "+10s") { seekBy(STEP) }, size = 44.dp) }
                item { Box(Modifier.width(3.dp).height(40.dp).background(ST.Line)) }
                item { StButton(tr("Audio", "الصوت") + " · ${p.audioTrackCount}", p.onOpenAudioTracks, icon = "♪") }
                item { StButton(tr("Subtitles", "الترجمة") + " · ${p.subtitleTrackCount}", p.onOpenSubtitleTracks, icon = "CC") }
                item { StButton(p.resolutionBadgeLabel ?: tr("Quality", "الجودة"), p.onOpenVideoTracks, icon = "HD") }
                if (p.showEpisodesAction) item { StButton(tr("Episodes", "الحلقات"), p.onOpenEpisodes, icon = "≣") }
                item { StButton(tr("Start over", "من البداية"), { p.onSeekToPosition(0L) }, icon = "⏮") }
                item { androidx.compose.foundation.layout.Box(Modifier.focusRequester(moreFocus)) { StButton(tr("More", "المزيد"), { sheet = !sheet }, primary = sheet, icon = "⚙") } }
                item { StButton(tr("Close", "إغلاق"), p.onClose, icon = "✕") }
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
        StCard(
            onClick = { p.onUserInteraction(); p.onTogglePlayPause() }, shape = ST.Pill, container = Color.Transparent, focusedContainer = Color.White.copy(alpha = 0.08f), zoom = 1f,
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
                StProgress(if (p.duration > 0) (pos.toFloat() / p.duration).coerceIn(0f, 1f) else 0f, Modifier.fillMaxWidth(), if (focused) 8.dp else 4.dp)
            }
        }
        Row {
            Text(stDuration(pos), color = if (p.seekPreview.visible) ST.Amber else ST.Text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Text("-" + stDuration((p.duration - pos).coerceAtLeast(0)), color = ST.Sub, fontSize = 14.sp)
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
        add(Triple(tr("Sleep timer", "مؤقت النوم"), if (p.sleepTimerUiState.stopTimerActive) stDuration(p.sleepTimerUiState.stopRemainingMs) else tr("Off", "معطل"), p.onOpenStopPlaybackTimer))
        add(Triple(tr("Standby timer", "مؤقت الاستعداد"), if (p.sleepTimerUiState.idleTimerActive) stDuration(p.sleepTimerUiState.idleRemainingMs) else tr("Off", "معطل"), p.onOpenIdleStandbyTimer))
        add(Triple(tr("Multiview", "تقسيم الشاشة"), "", p.onOpenSplitScreen))
        add(Triple(tr("Picture in picture", "صورة داخل صورة"), "", p.onEnterPictureInPicture))
        add(Triple(tr("Cast", "البث"), if (p.isCastConnected) tr("Connected", "متصل") else "", if (p.isCastConnected) p.onStopCasting else p.onCast))
    }
    Column(
        Modifier.align(Alignment.CenterStart).fillMaxHeight().width(380.dp).background(ST.Bg.copy(alpha = 0.97f)).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f).background(ST.Amber).padding(horizontal = 12.dp, vertical = 8.dp)) { Text(tr("PLAYBACK OPTIONS", "خيارات التشغيل"), color = ST.Bg, fontSize = 15.sp, fontWeight = FontWeight.Black) }
            StRound("✕", onDismiss, size = 36.dp)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(rows.size) { i ->
                val (label, value, action) = rows[i]
                StCard(onClick = action, shape = ST.RSmall, container = Color.Transparent, focusedContainer = ST.Card, zoom = 1.02f, modifier = Modifier.fillMaxWidth().then(if (i == 0) Modifier.focusRequester(focus) else Modifier)) {
                    Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(label, color = ST.Text, fontSize = 15.sp, modifier = Modifier.weight(1f), maxLines = 1)
                        Text(value, color = ST.Amber, fontSize = 13.sp, fontWeight = FontWeight.Black, maxLines = 1)
                        Text("  ›", color = ST.Faint, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

/** Channel list: END-side "standings table" over the video: lime header with count, column header row,
 *  rows = number block | logo | name + now title | badges; now/next scoreboard pinned at the bottom of the panel. */
@Composable
internal fun SportsTvLiveChannelList(p: LiveChannelListParams) {
    val idx = remember(p.channels, p.currentChannelId) { p.channels.indexOfFirst { it.id == p.currentChannelId }.coerceAtLeast(0) }
    val state = rememberLazyListState(idx)
    var focused by remember(p.currentChannelId) { mutableStateOf(p.channels.getOrNull(idx)) }
    LaunchedEffect(Unit) { runCatching { p.focusRequester.requestFocus() } }
    Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f), Color.Black.copy(alpha = 0.9f))))) {
        Column(Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(560.dp).background(ST.Bg.copy(alpha = 0.95f))) {
            Row(Modifier.fillMaxWidth().background(ST.Amber).padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text((p.lastVisitedCategoryName ?: tr("All channels", "كل القنوات")).uppercase(), color = ST.Bg, fontSize = 18.sp, fontWeight = FontWeight.Black, maxLines = 1)
                }
                Box(Modifier.background(ST.Bg).padding(horizontal = 10.dp, vertical = 4.dp)) { Text("${p.channels.size}", color = ST.Amber, fontSize = 16.sp, fontWeight = FontWeight.Black) }
                Spacer(Modifier.width(8.dp))
                StRound("✕", p.onDismiss, size = 36.dp)
            }
            Row(Modifier.fillMaxWidth().background(ST.Raised), verticalAlignment = Alignment.CenterVertically) {
                StTab(tr("Categories", "الفئات"), false, p.onOpenCategories)
                if (p.lastVisitedCategoryName != null) StTab(tr("Last group", "آخر مجموعة"), false, p.onOpenLastGroup)
                StTab(tr("Guide", "الدليل"), false, p.onOpenGuide)
            }
            Row(Modifier.fillMaxWidth().background(ST.Line).padding(horizontal = 10.dp, vertical = 5.dp)) {
                Text("#", color = ST.Amber, fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(44.dp))
                Text(tr("CHANNEL", "القناة"), color = ST.Text, fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                Text(tr("INFO", "معلومات"), color = ST.Sub, fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
            LazyColumn(state = state, verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f), contentPadding = PaddingValues(6.dp)) {
                items(p.channels, key = { it.id }) { c ->
                    val cur = c.id == p.currentChannelId
                    StCard(
                        onClick = { p.onInteracted(); p.onSelectChannel(c.id) }, onLongClick = { p.onChannelLongPress(c) }, shape = ST.Pill, zoom = 1.02f,
                        container = if (cur) ST.Amber.copy(alpha = 0.18f) else ST.Card, focusedContainer = ST.Line,
                        modifier = Modifier.fillMaxWidth().then(if (cur) Modifier.focusRequester(p.focusRequester) else Modifier).onFocusChanged { if (it.isFocused) { focused = c; p.onInteracted() } }
                    ) {
                        Row(Modifier.height(52.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.width(44.dp).fillMaxHeight().background(if (cur) ST.Amber else ST.Raised), contentAlignment = Alignment.Center) {
                                Text("${p.numberOf(c)}", color = if (cur) ST.Bg else ST.Amber, fontSize = 13.sp, fontWeight = FontWeight.Black)
                            }
                            StLogo(c.name, c.logoUrl, 36.dp, Modifier.padding(horizontal = 8.dp))
                            Column(Modifier.weight(1f)) {
                                Text((if (c.id == p.movingChannelId) "⇅  " else "") + c.name.uppercase(), color = ST.Text, fontSize = 13.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                c.currentProgram?.let { Text(it.title, color = ST.Sub, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                            }
                            Row(Modifier.padding(end = 8.dp), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (cur) StBadge(tr("NOW", "الآن"), ST.Live, filled = true)
                                c.qualityBadge()?.let { StBadge(it, ST.Text) }
                                if (c.catchUpSupported) StBadge("⟲", ST.Blue)
                                if (c.isFavorite) Text("★", color = ST.Amber, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
            focused?.let { c ->
                Column(Modifier.fillMaxWidth().background(ST.Raised).padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(c.name.uppercase(), color = ST.Amber, fontSize = 13.sp, fontWeight = FontWeight.Black, maxLines = 1)
                    NowNext(c.currentProgram, c.nextProgram)
                }
            }
        }
    }
}

/** Zap banner: floating rounded card at the bottom with channel head, now/next and a compact round-control row. */
@Composable
internal fun SportsTvLiveChannelInfo(p: LiveChannelInfoParams) {
    LaunchedEffect(Unit) { runCatching { p.focusRequester.requestFocus() } }
    val rec = p.currentRecordingStatus == RecordingStatus.RECORDING
    // Alaa's required order first: Channels, Favorites, Audio, Aspect, Settings, Quality, Subs, Guide.
    val acts = buildList {
        add(StAct("☰", tr("Channels", "القنوات"), true) { p.onInteracted(); p.onOpenChannelList() })
        add(StAct(if (p.channel?.isFavorite == true) "♥" else "♡", tr("Favorites", "المفضلة"), p.channel?.isFavorite == true) { p.onInteracted(); p.onToggleFavorite() })
        add(StAct("♪", tr("Audio", "الصوت")) { p.onInteracted(); p.onOpenAudioTracks() })
        add(StAct("▭", tr("Aspect", "العرض") + " " + p.aspectRatioLabel) { p.onInteracted(); p.onToggleAspectRatio() })
        add(StAct("⚙", tr("Settings", "الإعدادات")) { p.onInteracted(); p.onOpenSettings() })
        add(StAct("HD", p.resolutionLabel ?: tr("Quality", "الجودة")) { p.onInteracted(); p.onOpenVideoTracks() })
        add(StAct("CC", tr("Subs", "الترجمة")) { p.onInteracted(); p.onOpenSubtitleTracks() })
        add(StAct("▦", tr("Guide", "الدليل")) { p.onInteracted(); p.onOpenFullEpg() })
        add(StAct(if (p.isPlaying) "❚❚" else "▶", if (p.isPlaying) tr("Pause", "إيقاف") else tr("Play", "تشغيل")) { p.onInteracted(); p.onTogglePlayPause() })
        add(StAct(if (p.isMuted) "🔇" else "🔊", tr("Mute", "كتم"), p.isMuted) { p.onInteracted(); p.onToggleMute() })
        if (p.variantCount > 1) add(StAct("⇋", tr("Source", "المصدر")) { p.onInteracted(); p.onOpenVariants() })
        add(StAct("⏮", tr("Restart", "من البداية")) { p.onInteracted(); p.onRestartProgram() })
        add(StAct("⟲", tr("Catch-up", "الأرشيف")) { p.onInteracted(); p.onOpenArchive() })
        if (p.canSeekToLive) add(StAct("⇥", tr("Go live", "مباشر")) { p.onInteracted(); p.onSeekToLiveEdge() })
        add(StAct(if (rec) "■" else "●", if (rec) tr("Stop rec", "إيقاف") else tr("Record", "تسجيل"), rec) { p.onInteracted(); if (rec) p.onStopRecording() else p.onStartRecording() })
        add(StAct("◷", tr("Schedule", "جدولة")) { p.onInteracted(); p.onScheduleRecording() })
        add(StAct("⇄", "A/V") { p.onInteracted(); p.onOpenAudioVideoSync() })
        add(StAct("◫", tr("Multiview", "تقسيم")) { p.onInteracted(); p.onOpenSplitScreen() })
        add(StAct("∿", tr("Stats", "إحصاءات"), p.isDiagnosticsEnabled) { p.onInteracted(); p.onToggleDiagnostics() })
        add(StAct("⧉", "PiP") { p.onEnterPictureInPicture() })
        add(StAct("⎚", if (p.isCastConnected) tr("Stop cast", "إيقاف البث") else tr("Cast", "بث"), p.isCastConnected) { if (p.isCastConnected) p.onStopCasting() else p.onCast() })
    }
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.align(Alignment.BottomStart).fillMaxWidth()) {
            Row(Modifier.padding(start = 40.dp, end = 40.dp, bottom = 8.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                ChannelHead(p.channel, null, p.displayChannelNumber, p.resolutionLabel, false)
                NowNext(p.currentProgram, p.nextProgram, Modifier.weight(1f).clip(ST.RSmall).background(ST.Bg.copy(alpha = 0.88f)).padding(10.dp))
                Box(Modifier.background(ST.Amber).padding(horizontal = 12.dp, vertical = 8.dp)) { Text(stClock(System.currentTimeMillis()), color = ST.Bg, fontSize = 20.sp, fontWeight = FontWeight.Black) }
            }
            Row(Modifier.fillMaxWidth().background(ST.Bg.copy(alpha = 0.95f)), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.background(ST.Live).padding(horizontal = 10.dp, vertical = 26.dp)) { Text("LIVE", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black) }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)) {
                    items(acts.size) { i -> StCtl(acts[i], if (i == 0) Modifier.focusRequester(p.focusRequester) else Modifier, size = 40.dp) }
                }
            }
        }
    }
}
