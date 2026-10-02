package com.streamvault.app.ui.themes.nextgentv

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

private class NgAct(val glyph: String, val label: String, val active: Boolean = false, val onClick: () -> Unit)

/** Circular icon with caption under it: NextGenTv's player control. */
@Composable
private fun NgCtl(a: NgAct, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 52.dp) {
    Column(modifier.width(size + 22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        NgRound(a.glyph, a.onClick, size = size, active = a.active)
        Text(a.label, color = NG.Sub, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun NowNext(now: Program?, next: Program?, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (now != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NgBadge(tr("NOW", "الآن"), NG.Amber, filled = true)
                Text(now.title, color = NG.Text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, false))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(ngClock(now.startTime), color = NG.Sub, fontSize = 12.sp)
                NgProgress(now.progressAt(), Modifier.weight(1f))
                Text(ngClock(now.endTime), color = NG.Sub, fontSize = 12.sp)
            }
        } else Text(tr("No programme information", "لا توجد معلومات عن البرنامج"), color = NG.Faint, fontSize = 14.sp)
        next?.let { Text(tr("Next", "التالي") + "  ${ngClock(it.startTime)}  ·  ${it.title}", color = NG.Faint, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
    }
}

@Composable
private fun ChannelHead(channel: Channel?, name: String?, number: Int, resolution: String?, timeshift: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        NgLogo(channel?.name ?: name.orEmpty(), channel?.logoUrl, 64.dp)
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (number > 0) Text("$number", color = NG.Amber, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text(channel?.name ?: name.orEmpty(), color = NG.Text, fontSize = 24.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                if (channel?.isFavorite == true) Text("♥", color = NG.Amber, fontSize = 16.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                NgBadge(if (timeshift) tr("PAUSED LIVE", "مباشر مؤجل") else "● LIVE", NG.Live, filled = !timeshift)
                (resolution ?: channel?.qualityBadge())?.let { NgBadge(it, NG.Text) }
                if (channel?.catchUpSupported == true) NgBadge(tr("CATCH-UP", "أرشيف"), NG.Blue)
            }
        }
    }
}



@Composable
internal fun NextGenTvPlayerOverlay(p: PlayerOverlayParams) {
    if (!p.visible) return
    if (p.isLive && !p.isCatchUpPlayback) NgLive(p) else NgVod(p)
}

/** Live: spatial panes. Tilted identity pane + deep programme pane, clock orb top end, keys hovering on a light horizon. */
@Composable
private fun NgLive(p: PlayerOverlayParams) {
    LaunchedEffect(Unit) { runCatching { p.playButtonFocusRequester.requestFocus() } }
    val rec = p.currentRecordingStatus == RecordingStatus.RECORDING
    val acts = buildList {
        add(NgAct(if (p.isPlaying) "❚❚" else "▶", if (p.isPlaying) tr("Pause", "إيقاف") else tr("Play", "تشغيل"), true) { p.onUserInteraction(); p.onTogglePlayPause() })
        add(NgAct("☰", tr("Channels", "القنوات")) { p.onOpenLiveChannels() })
        add(NgAct("▦", tr("Guide", "الدليل")) { p.onOpenLiveGuide() })
        add(NgAct(if (p.currentChannel?.isFavorite == true) "♥" else "♡", tr("My List", "قائمتي"), p.currentChannel?.isFavorite == true) { p.onToggleLiveFavorite() })
        add(NgAct("⏪", "-10s") { p.onUserInteraction(); p.onSeekBackward() })
        add(NgAct("⏩", "+10s") { p.onUserInteraction(); p.onSeekForward() })
        add(NgAct("⏮", tr("Restart", "من البداية")) { p.onRestartProgram() })
        add(NgAct("⟲", tr("Catch-up", "الأرشيف")) { p.onOpenArchive() })
        if (p.timeshiftUiState.canSeekToLive) add(NgAct("⇥", tr("Go live", "مباشر")) { p.onSeekToLiveEdge() })
        add(NgAct("CC", tr("Subtitles", "الترجمة") + " ${p.subtitleTrackCount}") { p.onOpenSubtitleTracks() })
        add(NgAct("♪", tr("Audio", "الصوت") + " ${p.audioTrackCount}") { p.onOpenAudioTracks() })
        add(NgAct("HD", p.resolutionBadgeLabel ?: tr("Quality", "الجودة")) { p.onOpenVideoTracks() })
        add(NgAct(if (p.isMuted) "🔇" else "🔊", tr("Mute", "كتم"), p.isMuted) { p.onToggleMute() })
        add(NgAct(if (rec) "■" else "●", if (rec) tr("Stop rec", "إيقاف التسجيل") else tr("Record", "تسجيل"), rec) { if (rec) p.onStopRecording() else p.onStartRecording() })
        add(NgAct("◷", tr("Schedule", "جدولة")) { p.onScheduleRecording() })
        add(NgAct("◷", tr("Daily", "يومي")) { p.onScheduleDailyRecording() })
        add(NgAct("◷", tr("Weekly", "أسبوعي")) { p.onScheduleWeeklyRecording() })
        add(NgAct("▭", p.aspectRatioLabel) { p.onToggleAspectRatio() })
        add(NgAct("»", "${p.playbackSpeed}×") { p.onOpenPlaybackSpeed() })
        if (p.audioVideoSyncEnabled) add(NgAct("⇄", tr("A/V sync", "مزامنة")) { p.onOpenAudioVideoSync() })
        add(NgAct("◫", tr("Multiview", "تقسيم")) { p.onOpenSplitScreen() })
        add(NgAct("⏲", if (p.sleepTimerUiState.stopTimerActive) ngDuration(p.sleepTimerUiState.stopRemainingMs) else tr("Sleep", "مؤقت"), p.sleepTimerUiState.stopTimerActive) { p.onOpenStopPlaybackTimer() })
        add(NgAct("☾", if (p.sleepTimerUiState.idleTimerActive) ngDuration(p.sleepTimerUiState.idleRemainingMs) else tr("Standby", "استعداد"), p.sleepTimerUiState.idleTimerActive) { p.onOpenIdleStandbyTimer() })
        add(NgAct("⧉", "PiP") { p.onEnterPictureInPicture() })
        add(NgAct("⎚", if (p.isCastConnected) tr("Stop cast", "إيقاف البث") else tr("Cast", "بث"), p.isCastConnected) { if (p.isCastConnected) p.onStopCasting() else p.onCast() })
        add(NgAct("✕", tr("Close", "إغلاق")) { p.onClose() })
    }
    Box(p.modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, NG.Bg.copy(alpha = 0.35f), NG.Bg.copy(alpha = 0.9f))))) {
        // floating clock orb, top end
        NgPane(Modifier.align(Alignment.TopEnd).padding(32.dp)) {
            Column(Modifier.padding(horizontal = 22.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(ngClock(System.currentTimeMillis()), color = NG.Text, fontSize = 30.sp, fontWeight = FontWeight.Light)
                if (rec) NgBadge("● REC", NG.Live, filled = true)
            }
        }
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 40.dp, vertical = 26.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.Bottom) {
                // tilted identity pane (leans toward the viewer from the start side)
                NgPane(Modifier.width(420.dp).ngTilt(10f)) {
                    Box(Modifier.padding(18.dp)) { ChannelHead(p.currentChannel, p.currentChannelName, p.displayChannelNumber, p.resolutionBadgeLabel, p.timeshiftUiState.enabledForSession && p.timeshiftUiState.bufferedBehindLiveMs > 0) }
                }
                // flat programme pane, center depth
                NgPane(Modifier.weight(1f), depth = 10.dp) {
                    Box(Modifier.padding(18.dp)) { NowNext(p.currentProgram, p.nextProgram, Modifier.fillMaxWidth()) }
                }
            }
            // floating control dock: a horizon line with keys hovering on it
            Box {
                Box(Modifier.align(Alignment.Center).fillMaxWidth().height(2.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, NG.Amber.copy(alpha = 0.6f), NG.Violet.copy(alpha = 0.6f), Color.Transparent))))
                LazyRow(Modifier.fillMaxWidth().focusRequester(p.quickActionsFocusRequester), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally), contentPadding = PaddingValues(horizontal = 24.dp, vertical = 6.dp)) {
                    items(acts.size) { i -> NgCtl(acts[i], if (i == 0) Modifier.focusRequester(p.playButtonFocusRequester) else Modifier) }
                }
            }
        }
    }
}

/** VOD: tilted title pane, floating transport pane in the center, light-beam scrubber, END angled options pane. */
@Composable
private fun NgVod(p: PlayerOverlayParams) {
    var sheet by remember { mutableStateOf(false) }
    val sheetFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { p.playButtonFocusRequester.requestFocus() } }
    LaunchedEffect(sheet) { if (sheet) runCatching { sheetFocus.requestFocus() } }
    fun seekBy(d: Long) { p.onUserInteraction(); p.onSeekToPosition((p.currentPosition + d).coerceIn(0L, (p.duration - 1_000L).coerceAtLeast(0L))) }
    Box(p.modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.75f), Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.9f))))) {
        NgPane(Modifier.align(Alignment.TopStart).padding(32.dp).fillMaxWidth(0.72f).ngTilt(6f)) {
        Row(Modifier.padding(horizontal = 20.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            NgRound("←", { p.onNavigateBack() }, size = 44.dp)
            Column(Modifier.weight(1f)) {
                if (p.isCatchUpPlayback) Text(tr("Catch-up", "من الأرشيف"), color = NG.Amber, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(p.displayTitle, color = NG.Text, fontSize = 28.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                p.episodeLine?.let { Text(it, color = NG.Sub, fontSize = 15.sp, maxLines = 1) }
            }
            if (p.currentRecordingStatus == RecordingStatus.RECORDING) NgBadge("● REC", NG.Live, filled = true)
            if (p.playbackSpeed != 1f) NgBadge("${p.playbackSpeed}×", NG.Amber)
            Text(ngClock(System.currentTimeMillis()), color = NG.Sub, fontSize = 20.sp)
        }
        }
        NgPane(Modifier.align(Alignment.Center), depth = 14.dp) {
            Row(Modifier.padding(horizontal = 36.dp, vertical = 22.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(36.dp)) {
                NgCtl(NgAct("↺", "10s") { seekBy(-STEP) }, size = 60.dp)
                NgCtl(NgAct(if (p.isPlaying) "❚❚" else "▶", if (p.isPlaying) tr("Pause", "إيقاف") else tr("Play", "تشغيل"), true) { p.onUserInteraction(); p.onTogglePlayPause() }, Modifier.focusRequester(p.playButtonFocusRequester), size = 88.dp)
                NgCtl(NgAct("↻", "10s") { seekBy(STEP) }, size = 60.dp)
            }
        }
        Column(Modifier.align(Alignment.BottomStart).fillMaxWidth(if (sheet) 0.64f else 1f).padding(horizontal = 48.dp, vertical = 28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Scrubber(p, ::seekBy)
            LazyRow(Modifier.focusRequester(p.quickActionsFocusRequester), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item { NgButton(tr("Audio", "الصوت") + " · ${p.audioTrackCount}", p.onOpenAudioTracks, icon = "♪") }
                item { NgButton(tr("Subtitles", "الترجمة") + " · ${p.subtitleTrackCount}", p.onOpenSubtitleTracks, icon = "CC") }
                item { NgButton(p.resolutionBadgeLabel ?: tr("Quality", "الجودة"), p.onOpenVideoTracks, icon = "HD") }
                if (p.showEpisodesAction) item { NgButton(tr("Episodes", "الحلقات"), p.onOpenEpisodes, icon = "≣") }
                item { NgButton(tr("Start over", "من البداية"), { p.onSeekToPosition(0L) }, icon = "⏮") }
                item { NgButton(tr("More", "المزيد"), { sheet = !sheet }, primary = sheet, icon = "⚙") }
                item { NgButton(tr("Close", "إغلاق"), p.onClose, icon = "✕") }
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
        NgCard(
            onClick = { p.onUserInteraction(); p.onTogglePlayPause() }, shape = NG.Pill, container = Color.Transparent, focusedContainer = Color.White.copy(alpha = 0.08f), zoom = 1f,
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
                NgProgress(if (p.duration > 0) (pos.toFloat() / p.duration).coerceIn(0f, 1f) else 0f, Modifier.fillMaxWidth(), if (focused) 8.dp else 4.dp)
            }
        }
        Row {
            Text(ngDuration(pos), color = if (p.seekPreview.visible) NG.Amber else NG.Text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Text("-" + ngDuration((p.duration - pos).coerceAtLeast(0)), color = NG.Sub, fontSize = 14.sp)
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
        add(Triple(tr("Sleep timer", "مؤقت النوم"), if (p.sleepTimerUiState.stopTimerActive) ngDuration(p.sleepTimerUiState.stopRemainingMs) else tr("Off", "معطل"), p.onOpenStopPlaybackTimer))
        add(Triple(tr("Standby timer", "مؤقت الاستعداد"), if (p.sleepTimerUiState.idleTimerActive) ngDuration(p.sleepTimerUiState.idleRemainingMs) else tr("Off", "معطل"), p.onOpenIdleStandbyTimer))
        add(Triple(tr("Multiview", "تقسيم الشاشة"), "", p.onOpenSplitScreen))
        add(Triple(tr("Picture in picture", "صورة داخل صورة"), "", p.onEnterPictureInPicture))
        add(Triple(tr("Cast", "البث"), if (p.isCastConnected) tr("Connected", "متصل") else "", if (p.isCastConnected) p.onStopCasting else p.onCast))
    }
    Column(
        Modifier.align(Alignment.CenterEnd).padding(24.dp).fillMaxHeight(0.9f).width(380.dp).ngTilt(-8f).clip(NG.R).background(NG.Raised.copy(alpha = 0.97f)).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(tr("Playback options", "خيارات التشغيل"), color = NG.Text, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            NgRound("✕", onDismiss, size = 36.dp)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(rows.size) { i ->
                val (label, value, action) = rows[i]
                NgCard(onClick = action, shape = NG.RSmall, container = Color.Transparent, focusedContainer = NG.Card, zoom = 1.02f, modifier = Modifier.fillMaxWidth().then(if (i == 0) Modifier.focusRequester(focus) else Modifier)) {
                    Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(label, color = NG.Text, fontSize = 15.sp, modifier = Modifier.weight(1f), maxLines = 1)
                        Text(value, color = NG.Amber, fontSize = 14.sp, maxLines = 1)
                        Text("  ›", color = NG.Faint, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

/** Channel list: END-side pane angled toward the viewer, numbered squircle keys; focused now/next floats START. */
@Composable
internal fun NextGenTvLiveChannelList(p: LiveChannelListParams) {
    val idx = remember(p.channels, p.currentChannelId) { p.channels.indexOfFirst { it.id == p.currentChannelId }.coerceAtLeast(0) }
    val state = rememberLazyListState(idx)
    var focused by remember(p.currentChannelId) { mutableStateOf(p.channels.getOrNull(idx)) }
    LaunchedEffect(Unit) { runCatching { p.focusRequester.requestFocus() } }
    Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.Transparent, NG.Bg.copy(alpha = 0.55f), NG.Bg.copy(alpha = 0.95f))))) {
        Column(Modifier.align(Alignment.TopEnd).fillMaxHeight().width(540.dp).padding(end = 36.dp, top = 28.dp, bottom = 24.dp, start = 12.dp).ngTilt(-7f).clip(NG.R).background(NG.Raised.copy(alpha = 0.9f)).border(1.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.22f), Color.Transparent)), NG.R).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(p.lastVisitedCategoryName ?: tr("All channels", "كل القنوات"), color = NG.Text, fontSize = 24.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text("${p.channels.size} " + tr("channels", "قناة"), color = NG.Faint, fontSize = 13.sp)
                }
                NgRound("✕", p.onDismiss, size = 40.dp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NgTab(tr("Categories", "الفئات"), false, p.onOpenCategories)
                if (p.lastVisitedCategoryName != null) NgTab(tr("Last group", "آخر مجموعة"), false, p.onOpenLastGroup)
                NgTab(tr("Guide", "الدليل"), false, p.onOpenGuide)
            }
            LazyColumn(state = state, verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f), contentPadding = PaddingValues(4.dp)) {
                items(p.channels, key = { it.id }) { c ->
                    val cur = c.id == p.currentChannelId
                    NgCard(
                        onClick = { p.onInteracted(); p.onSelectChannel(c.id) }, onLongClick = { p.onChannelLongPress(c) }, zoom = 1.03f, container = if (cur) NG.Amber.copy(alpha = 0.2f) else NG.Raised.copy(alpha = 0.7f), focusedContainer = NG.Card,
                        modifier = Modifier.fillMaxWidth().then(if (cur) Modifier.focusRequester(p.focusRequester) else Modifier).onFocusChanged { if (it.isFocused) { focused = c; p.onInteracted() } }
                    ) {
                        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.size(34.dp).clip(NG.RSmall).background(if (cur) NG.Amber else Color.White.copy(alpha = 0.06f)), contentAlignment = Alignment.Center) { Text("${p.numberOf(c)}", color = if (cur) NG.Bg else NG.Sub, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                            NgLogo(c.name, c.logoUrl, 40.dp)
                            Column(Modifier.weight(1f)) {
                                Text(if (c.id == p.movingChannelId) "⇅  ${c.name}" else c.name, color = NG.Text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                c.currentProgram?.let { Text(it.title, color = NG.Faint, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                            }
                            if (cur) NgBadge(tr("Now", "الآن"), NG.Amber, filled = true)
                            c.qualityBadge()?.let { NgBadge(it, NG.Text) }
                            if (c.catchUpSupported) NgBadge("⟲ " + tr("Archive", "أرشيف"), NG.Blue)
                            Text(if (c.isFavorite) "♥" else "", color = NG.Amber, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
        focused?.let { c ->
            Column(Modifier.align(Alignment.BottomStart).padding(40.dp).width(460.dp).ngTilt(9f).clip(NG.R).background(NG.Card.copy(alpha = 0.92f)).border(1.dp, NG.Amber.copy(alpha = 0.4f), NG.R).padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(c.name, color = NG.Text, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                NowNext(c.currentProgram, c.nextProgram)
            }
        }
    }
}

/** Zap banner: three floating panes (identity tilted in, programme deep, clock tilted in) over a capsule dock. */
@Composable
internal fun NextGenTvLiveChannelInfo(p: LiveChannelInfoParams) {
    LaunchedEffect(Unit) { runCatching { p.focusRequester.requestFocus() } }
    val rec = p.currentRecordingStatus == RecordingStatus.RECORDING
    // Alaa's required order first: Channels, Favorites, Audio, Aspect, Settings, Quality, Subs, Guide.
    val acts = buildList {
        add(NgAct("☰", tr("Channels", "القنوات"), true) { p.onInteracted(); p.onOpenChannelList() })
        add(NgAct(if (p.channel?.isFavorite == true) "♥" else "♡", tr("Favorites", "المفضلة"), p.channel?.isFavorite == true) { p.onInteracted(); p.onToggleFavorite() })
        add(NgAct("♪", tr("Audio", "الصوت")) { p.onInteracted(); p.onOpenAudioTracks() })
        add(NgAct("▭", tr("Aspect", "العرض") + " " + p.aspectRatioLabel) { p.onInteracted(); p.onToggleAspectRatio() })
        add(NgAct("⚙", tr("Settings", "الإعدادات")) { p.onInteracted(); p.onOpenSettings() })
        add(NgAct("HD", p.resolutionLabel ?: tr("Quality", "الجودة")) { p.onInteracted(); p.onOpenVideoTracks() })
        add(NgAct("CC", tr("Subs", "الترجمة")) { p.onInteracted(); p.onOpenSubtitleTracks() })
        add(NgAct("▦", tr("Guide", "الدليل")) { p.onInteracted(); p.onOpenFullEpg() })
        add(NgAct(if (p.isPlaying) "❚❚" else "▶", if (p.isPlaying) tr("Pause", "إيقاف") else tr("Play", "تشغيل")) { p.onInteracted(); p.onTogglePlayPause() })
        add(NgAct(if (p.isMuted) "🔇" else "🔊", tr("Mute", "كتم"), p.isMuted) { p.onInteracted(); p.onToggleMute() })
        if (p.variantCount > 1) add(NgAct("⇋", tr("Source", "المصدر")) { p.onInteracted(); p.onOpenVariants() })
        add(NgAct("⏮", tr("Restart", "من البداية")) { p.onInteracted(); p.onRestartProgram() })
        add(NgAct("⟲", tr("Catch-up", "الأرشيف")) { p.onInteracted(); p.onOpenArchive() })
        if (p.canSeekToLive) add(NgAct("⇥", tr("Go live", "مباشر")) { p.onInteracted(); p.onSeekToLiveEdge() })
        add(NgAct(if (rec) "■" else "●", if (rec) tr("Stop rec", "إيقاف") else tr("Record", "تسجيل"), rec) { p.onInteracted(); if (rec) p.onStopRecording() else p.onStartRecording() })
        add(NgAct("◷", tr("Schedule", "جدولة")) { p.onInteracted(); p.onScheduleRecording() })
        add(NgAct("⇄", "A/V") { p.onInteracted(); p.onOpenAudioVideoSync() })
        add(NgAct("◫", tr("Multiview", "تقسيم")) { p.onInteracted(); p.onOpenSplitScreen() })
        add(NgAct("∿", tr("Stats", "إحصاءات"), p.isDiagnosticsEnabled) { p.onInteracted(); p.onToggleDiagnostics() })
        add(NgAct("⧉", "PiP") { p.onEnterPictureInPicture() })
        add(NgAct("⎚", if (p.isCastConnected) tr("Stop cast", "إيقاف البث") else tr("Cast", "بث"), p.isCastConnected) { if (p.isCastConnected) p.onStopCasting() else p.onCast() })
    }
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 36.dp, vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                NgPane(Modifier.ngTilt(8f)) { Box(Modifier.padding(16.dp)) { ChannelHead(p.channel, null, p.displayChannelNumber, p.resolutionLabel, false) } }
                NgPane(Modifier.weight(1f), depth = 10.dp) { Box(Modifier.padding(16.dp)) { NowNext(p.currentProgram, p.nextProgram, Modifier.fillMaxWidth()) } }
                NgPane(Modifier.ngTilt(-8f)) { Text(ngClock(System.currentTimeMillis()), color = NG.Text, fontSize = 28.sp, fontWeight = FontWeight.Light, modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally), modifier = Modifier.fillMaxWidth().clip(NG.Pill).background(NG.Bg.copy(alpha = 0.7f)).padding(vertical = 6.dp)) {
                items(acts.size) { i -> NgCtl(acts[i], if (i == 0) Modifier.focusRequester(p.focusRequester) else Modifier, size = 44.dp) }
            }
        }
    }
}
