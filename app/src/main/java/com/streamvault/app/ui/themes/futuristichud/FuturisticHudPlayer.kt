package com.streamvault.app.ui.themes.futuristichud

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

private class FhAct(val glyph: String, val label: String, val active: Boolean = false, val onClick: () -> Unit)

/** Circular icon with caption under it: FuturisticHud's player control. */
@Composable
private fun FhCtl(a: FhAct, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 52.dp) {
    Column(modifier.width(size + 22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        FhRound(a.glyph, a.onClick, size = size, active = a.active)
        Text(a.label.uppercase(), color = FH.Sub, fontFamily = FH.Mono, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun NowNext(now: Program?, next: Program?, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (now != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FhBadge(tr("NOW", "الآن"), FH.Amber, filled = true)
                Text(now.title, color = FH.Text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, false))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(fhClock(now.startTime), color = FH.Sub, fontSize = 12.sp)
                FhProgress(now.progressAt(), Modifier.weight(1f))
                Text(fhClock(now.endTime), color = FH.Sub, fontSize = 12.sp)
            }
        } else Text(tr("No programme information", "لا توجد معلومات عن البرنامج"), color = FH.Faint, fontSize = 14.sp)
        next?.let { Text(tr("Next", "التالي") + "  ${fhClock(it.startTime)}  ·  ${it.title}", color = FH.Faint, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
    }
}

@Composable
private fun ChannelHead(channel: Channel?, name: String?, number: Int, resolution: String?, timeshift: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        FhLogo(channel?.name ?: name.orEmpty(), channel?.logoUrl, 64.dp)
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (number > 0) Text("$number", color = FH.Amber, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text(channel?.name ?: name.orEmpty(), color = FH.Text, fontSize = 24.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                if (channel?.isFavorite == true) Text("♥", color = FH.Amber, fontSize = 16.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FhBadge(if (timeshift) tr("PAUSED LIVE", "مباشر مؤجل") else "● LIVE", FH.Live, filled = !timeshift)
                (resolution ?: channel?.qualityBadge())?.let { FhBadge(it, FH.Text) }
                if (channel?.catchUpSupported == true) FhBadge(tr("CATCH-UP", "أرشيف"), FH.Blue)
            }
        }
    }
}



@Composable
internal fun FuturisticHudPlayerOverlay(p: PlayerOverlayParams) {
    if (!p.visible) return
    if (p.isLive && !p.isCatchUpPlayback) FhLive(p) else FhVod(p)
}

/** Live: soft bottom gradient, channel head + now/next on the start, clock on the end, one row of round controls. */
@Composable
private fun FhLive(p: PlayerOverlayParams) {
    LaunchedEffect(Unit) { runCatching { p.playButtonFocusRequester.requestFocus() } }
    val rec = p.currentRecordingStatus == RecordingStatus.RECORDING
    val acts = buildList {
        add(FhAct(if (p.isPlaying) "❚❚" else "▶", if (p.isPlaying) tr("Pause", "إيقاف") else tr("Play", "تشغيل"), true) { p.onUserInteraction(); p.onTogglePlayPause() })
        add(FhAct("☰", tr("Channels", "القنوات")) { p.onOpenLiveChannels() })
        add(FhAct("▦", tr("Guide", "الدليل")) { p.onOpenLiveGuide() })
        add(FhAct(if (p.currentChannel?.isFavorite == true) "♥" else "♡", tr("My List", "قائمتي"), p.currentChannel?.isFavorite == true) { p.onToggleLiveFavorite() })
        add(FhAct("⏪", "-10s") { p.onUserInteraction(); p.onSeekBackward() })
        add(FhAct("⏩", "+10s") { p.onUserInteraction(); p.onSeekForward() })
        add(FhAct("⏮", tr("Restart", "من البداية")) { p.onRestartProgram() })
        add(FhAct("⟲", tr("Catch-up", "الأرشيف")) { p.onOpenArchive() })
        if (p.timeshiftUiState.canSeekToLive) add(FhAct("⇥", tr("Go live", "مباشر")) { p.onSeekToLiveEdge() })
        add(FhAct("CC", tr("Subtitles", "الترجمة") + " ${p.subtitleTrackCount}") { p.onOpenSubtitleTracks() })
        add(FhAct("♪", tr("Audio", "الصوت") + " ${p.audioTrackCount}") { p.onOpenAudioTracks() })
        add(FhAct("HD", p.resolutionBadgeLabel ?: tr("Quality", "الجودة")) { p.onOpenVideoTracks() })
        add(FhAct(if (p.isMuted) "🔇" else "🔊", tr("Mute", "كتم"), p.isMuted) { p.onToggleMute() })
        add(FhAct(if (rec) "■" else "●", if (rec) tr("Stop rec", "إيقاف التسجيل") else tr("Record", "تسجيل"), rec) { if (rec) p.onStopRecording() else p.onStartRecording() })
        add(FhAct("◷", tr("Schedule", "جدولة")) { p.onScheduleRecording() })
        add(FhAct("◷", tr("Daily", "يومي")) { p.onScheduleDailyRecording() })
        add(FhAct("◷", tr("Weekly", "أسبوعي")) { p.onScheduleWeeklyRecording() })
        add(FhAct("▭", p.aspectRatioLabel) { p.onToggleAspectRatio() })
        add(FhAct("»", "${p.playbackSpeed}×") { p.onOpenPlaybackSpeed() })
        if (p.audioVideoSyncEnabled) add(FhAct("⇄", tr("A/V sync", "مزامنة")) { p.onOpenAudioVideoSync() })
        add(FhAct("◫", tr("Multiview", "تقسيم")) { p.onOpenSplitScreen() })
        add(FhAct("⏲", if (p.sleepTimerUiState.stopTimerActive) fhDuration(p.sleepTimerUiState.stopRemainingMs) else tr("Sleep", "مؤقت"), p.sleepTimerUiState.stopTimerActive) { p.onOpenStopPlaybackTimer() })
        add(FhAct("☾", if (p.sleepTimerUiState.idleTimerActive) fhDuration(p.sleepTimerUiState.idleRemainingMs) else tr("Standby", "استعداد"), p.sleepTimerUiState.idleTimerActive) { p.onOpenIdleStandbyTimer() })
        add(FhAct("⧉", "PiP") { p.onEnterPictureInPicture() })
        add(FhAct("⎚", if (p.isCastConnected) tr("Stop cast", "إيقاف البث") else tr("Cast", "بث"), p.isCastConnected) { if (p.isCastConnected) p.onStopCasting() else p.onCast() })
        add(FhAct("✕", tr("Close", "إغلاق")) { p.onClose() })
    }
    Box(p.modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.92f))))) {
        Column(Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(horizontal = 36.dp, vertical = 24.dp).background(FH.Bg.copy(alpha = 0.85f), FH.R).border(1.dp, FH.Line, FH.R).fhBrackets(FH.Amber, 22.dp).padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ChannelHead(p.currentChannel, p.currentChannelName, p.displayChannelNumber, p.resolutionBadgeLabel, p.timeshiftUiState.enabledForSession && p.timeshiftUiState.bufferedBehindLiveMs > 0)
                    NowNext(p.currentProgram, p.nextProgram, Modifier.fillMaxWidth(0.7f))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("T+" + fhClock(System.currentTimeMillis()), color = FH.Amber, fontFamily = FH.Mono, fontSize = 30.sp)
                    if (rec) FhBadge("● REC", FH.Live, filled = true)
                }
            }
            LazyRow(Modifier.fillMaxWidth().focusRequester(p.quickActionsFocusRequester), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(acts.size) { i -> FhCtl(acts[i], if (i == 0) Modifier.focusRequester(p.playButtonFocusRequester) else Modifier) }
            }
        }
    }
}

/** VOD: title top-left over a gradient, giant centered transport (-10 / play / +10), thin amber scrubber, pill row + side sheet. */
@Composable
private fun FhVod(p: PlayerOverlayParams) {
    var sheet by remember { mutableStateOf(false) }
    val sheetFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { p.playButtonFocusRequester.requestFocus() } }
    LaunchedEffect(sheet) { if (sheet) runCatching { sheetFocus.requestFocus() } }
    fun seekBy(d: Long) { p.onUserInteraction(); p.onSeekToPosition((p.currentPosition + d).coerceIn(0L, (p.duration - 1_000L).coerceAtLeast(0L))) }
    Box(p.modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.75f), Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.9f))))) {
        Row(Modifier.align(Alignment.TopStart).fillMaxWidth().padding(horizontal = 48.dp, vertical = 32.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            FhRound("←", { p.onNavigateBack() }, size = 44.dp)
            Column(Modifier.weight(1f)) {
                if (p.isCatchUpPlayback) Text(tr("Catch-up", "من الأرشيف"), color = FH.Amber, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(p.displayTitle, color = FH.Text, fontSize = 28.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                p.episodeLine?.let { Text(it, color = FH.Sub, fontSize = 15.sp, maxLines = 1) }
            }
            if (p.currentRecordingStatus == RecordingStatus.RECORDING) FhBadge("● REC", FH.Live, filled = true)
            if (p.playbackSpeed != 1f) FhBadge("${p.playbackSpeed}×", FH.Amber)
            Text(fhClock(System.currentTimeMillis()), color = FH.Sub, fontSize = 20.sp)
        }
        Row(Modifier.align(Alignment.Center), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(48.dp)) {
            FhCtl(FhAct("↺", "10s") { seekBy(-STEP) }, size = 64.dp)
            FhCtl(FhAct(if (p.isPlaying) "❚❚" else "▶", if (p.isPlaying) tr("Pause", "إيقاف") else tr("Play", "تشغيل"), true) { p.onUserInteraction(); p.onTogglePlayPause() }, Modifier.focusRequester(p.playButtonFocusRequester), size = 92.dp)
            FhCtl(FhAct("↻", "10s") { seekBy(STEP) }, size = 64.dp)
        }
        Column(Modifier.align(Alignment.BottomStart).fillMaxWidth(if (sheet) 0.64f else 1f).align(if (sheet) Alignment.BottomEnd else Alignment.BottomStart).padding(horizontal = 48.dp, vertical = 28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Scrubber(p, ::seekBy)
            LazyRow(Modifier.focusRequester(p.quickActionsFocusRequester), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item { FhButton(tr("Audio", "الصوت") + " · ${p.audioTrackCount}", p.onOpenAudioTracks, icon = "♪") }
                item { FhButton(tr("Subtitles", "الترجمة") + " · ${p.subtitleTrackCount}", p.onOpenSubtitleTracks, icon = "CC") }
                item { FhButton(p.resolutionBadgeLabel ?: tr("Quality", "الجودة"), p.onOpenVideoTracks, icon = "HD") }
                if (p.showEpisodesAction) item { FhButton(tr("Episodes", "الحلقات"), p.onOpenEpisodes, icon = "≣") }
                item { FhButton(tr("Start over", "من البداية"), { p.onSeekToPosition(0L) }, icon = "⏮") }
                item { FhButton(tr("More", "المزيد"), { sheet = !sheet }, primary = sheet, icon = "⚙") }
                item { FhButton(tr("Close", "إغلاق"), p.onClose, icon = "✕") }
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
        FhCard(
            onClick = { p.onUserInteraction(); p.onTogglePlayPause() }, shape = FH.Pill, container = Color.Transparent, focusedContainer = Color.White.copy(alpha = 0.08f), zoom = 1f,
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
                FhProgress(if (p.duration > 0) (pos.toFloat() / p.duration).coerceIn(0f, 1f) else 0f, Modifier.fillMaxWidth(), if (focused) 8.dp else 4.dp)
            }
        }
        Row {
            Text(fhDuration(pos), color = if (p.seekPreview.visible) FH.Amber else FH.Text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Text("-" + fhDuration((p.duration - pos).coerceAtLeast(0)), color = FH.Sub, fontSize = 14.sp)
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
        add(Triple(tr("Sleep timer", "مؤقت النوم"), if (p.sleepTimerUiState.stopTimerActive) fhDuration(p.sleepTimerUiState.stopRemainingMs) else tr("Off", "معطل"), p.onOpenStopPlaybackTimer))
        add(Triple(tr("Standby timer", "مؤقت الاستعداد"), if (p.sleepTimerUiState.idleTimerActive) fhDuration(p.sleepTimerUiState.idleRemainingMs) else tr("Off", "معطل"), p.onOpenIdleStandbyTimer))
        add(Triple(tr("Multiview", "تقسيم الشاشة"), "", p.onOpenSplitScreen))
        add(Triple(tr("Picture in picture", "صورة داخل صورة"), "", p.onEnterPictureInPicture))
        add(Triple(tr("Cast", "البث"), if (p.isCastConnected) tr("Connected", "متصل") else "", if (p.isCastConnected) p.onStopCasting else p.onCast))
    }
    Column(
        Modifier.align(Alignment.CenterStart).padding(24.dp).fillMaxHeight(0.9f).width(380.dp).clip(FH.R).background(FH.Bg.copy(alpha = 0.97f)).border(1.dp, FH.Amber.copy(alpha = 0.5f), FH.R).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(tr("Playback options", "خيارات التشغيل"), color = FH.Text, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            FhRound("✕", onDismiss, size = 36.dp)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(rows.size) { i ->
                val (label, value, action) = rows[i]
                FhCard(onClick = action, shape = FH.RSmall, container = Color.Transparent, focusedContainer = FH.Card, zoom = 1.02f, modifier = Modifier.fillMaxWidth().then(if (i == 0) Modifier.focusRequester(focus) else Modifier)) {
                    Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(label, color = FH.Text, fontSize = 15.sp, modifier = Modifier.weight(1f), maxLines = 1)
                        Text(value, color = FH.Amber, fontSize = 14.sp, maxLines = 1)
                        Text("  ›", color = FH.Faint, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

/** Channel list: start-side translucent sheet with recents as round logo bubbles and big rounded channel rows. */
@Composable
internal fun FuturisticHudLiveChannelList(p: LiveChannelListParams) {
    val idx = remember(p.channels, p.currentChannelId) { p.channels.indexOfFirst { it.id == p.currentChannelId }.coerceAtLeast(0) }
    val state = rememberLazyListState(idx)
    var focused by remember(p.currentChannelId) { mutableStateOf(p.channels.getOrNull(idx)) }
    LaunchedEffect(Unit) { runCatching { p.focusRequester.requestFocus() } }
    Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.Transparent, FH.Bg.copy(alpha = 0.6f), FH.Bg.copy(alpha = 0.95f))))) {
        Column(Modifier.align(Alignment.TopEnd).fillMaxHeight().width(540.dp).padding(end = 36.dp, top = 28.dp, bottom = 24.dp, start = 12.dp).border(1.dp, FH.Line).fhBrackets(FH.Amber, 22.dp, 3.dp).padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("// " + (p.lastVisitedCategoryName ?: tr("All channels", "كل القنوات")).uppercase(), color = FH.Amber, fontFamily = FH.Mono, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text("[${p.channels.size}] " + tr("SIGNALS", "قناة"), color = FH.Faint, fontFamily = FH.Mono, fontSize = 12.sp)
                }
                FhRound("✕", p.onDismiss, size = 40.dp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FhTab(tr("Categories", "الفئات"), false, p.onOpenCategories)
                if (p.lastVisitedCategoryName != null) FhTab(tr("Last group", "آخر مجموعة"), false, p.onOpenLastGroup)
                FhTab(tr("Guide", "الدليل"), false, p.onOpenGuide)
            }
            LazyColumn(state = state, verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f), contentPadding = PaddingValues(4.dp)) {
                items(p.channels, key = { it.id }) { c ->
                    val cur = c.id == p.currentChannelId
                    FhCard(
                        onClick = { p.onInteracted(); p.onSelectChannel(c.id) }, onLongClick = { p.onChannelLongPress(c) }, zoom = 1.03f, container = if (cur) FH.Amber.copy(alpha = 0.2f) else FH.Raised.copy(alpha = 0.7f), focusedContainer = FH.Card,
                        modifier = Modifier.fillMaxWidth().then(if (cur) Modifier.focusRequester(p.focusRequester) else Modifier).onFocusChanged { if (it.isFocused) { focused = c; p.onInteracted() } }
                    ) {
                        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("${p.numberOf(c)}", color = if (cur) FH.Amber else FH.Faint, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp))
                            FhLogo(c.name, c.logoUrl, 40.dp)
                            Column(Modifier.weight(1f)) {
                                Text(if (c.id == p.movingChannelId) "⇅  ${c.name}" else c.name, color = FH.Text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                c.currentProgram?.let { Text(it.title, color = FH.Faint, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                            }
                            if (cur) FhBadge(tr("Now", "الآن"), FH.Amber, filled = true)
                            c.qualityBadge()?.let { FhBadge(it, FH.Text) }
                            if (c.catchUpSupported) FhBadge("⟲ " + tr("Archive", "أرشيف"), FH.Blue)
                            Text(if (c.isFavorite) "♥" else "", color = FH.Amber, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
        focused?.let { c ->
            Column(Modifier.align(Alignment.BottomStart).padding(40.dp).width(480.dp).clip(FH.R).background(FH.Bg.copy(alpha = 0.88f)).border(1.dp, FH.Amber.copy(alpha = 0.6f), FH.R).padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(c.name, color = FH.Text, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                NowNext(c.currentProgram, c.nextProgram)
            }
        }
    }
}

/** Zap banner: floating rounded card at the bottom with channel head, now/next and a compact round-control row. */
@Composable
internal fun FuturisticHudLiveChannelInfo(p: LiveChannelInfoParams) {
    LaunchedEffect(Unit) { runCatching { p.focusRequester.requestFocus() } }
    val rec = p.currentRecordingStatus == RecordingStatus.RECORDING
    // Alaa's required order first: Channels, Favorites, Audio, Aspect, Settings, Quality, Subs, Guide.
    val acts = buildList {
        add(FhAct("☰", tr("Channels", "القنوات"), true) { p.onInteracted(); p.onOpenChannelList() })
        add(FhAct(if (p.channel?.isFavorite == true) "♥" else "♡", tr("Favorites", "المفضلة"), p.channel?.isFavorite == true) { p.onInteracted(); p.onToggleFavorite() })
        add(FhAct("♪", tr("Audio", "الصوت")) { p.onInteracted(); p.onOpenAudioTracks() })
        add(FhAct("▭", tr("Aspect", "العرض") + " " + p.aspectRatioLabel) { p.onInteracted(); p.onToggleAspectRatio() })
        add(FhAct("⚙", tr("Settings", "الإعدادات")) { p.onInteracted(); p.onOpenSettings() })
        add(FhAct("HD", p.resolutionLabel ?: tr("Quality", "الجودة")) { p.onInteracted(); p.onOpenVideoTracks() })
        add(FhAct("CC", tr("Subs", "الترجمة")) { p.onInteracted(); p.onOpenSubtitleTracks() })
        add(FhAct("▦", tr("Guide", "الدليل")) { p.onInteracted(); p.onOpenFullEpg() })
        add(FhAct(if (p.isPlaying) "❚❚" else "▶", if (p.isPlaying) tr("Pause", "إيقاف") else tr("Play", "تشغيل")) { p.onInteracted(); p.onTogglePlayPause() })
        add(FhAct(if (p.isMuted) "🔇" else "🔊", tr("Mute", "كتم"), p.isMuted) { p.onInteracted(); p.onToggleMute() })
        if (p.variantCount > 1) add(FhAct("⇋", tr("Source", "المصدر")) { p.onInteracted(); p.onOpenVariants() })
        add(FhAct("⏮", tr("Restart", "من البداية")) { p.onInteracted(); p.onRestartProgram() })
        add(FhAct("⟲", tr("Catch-up", "الأرشيف")) { p.onInteracted(); p.onOpenArchive() })
        if (p.canSeekToLive) add(FhAct("⇥", tr("Go live", "مباشر")) { p.onInteracted(); p.onSeekToLiveEdge() })
        add(FhAct(if (rec) "■" else "●", if (rec) tr("Stop rec", "إيقاف") else tr("Record", "تسجيل"), rec) { p.onInteracted(); if (rec) p.onStopRecording() else p.onStartRecording() })
        add(FhAct("◷", tr("Schedule", "جدولة")) { p.onInteracted(); p.onScheduleRecording() })
        add(FhAct("⇄", "A/V") { p.onInteracted(); p.onOpenAudioVideoSync() })
        add(FhAct("◫", tr("Multiview", "تقسيم")) { p.onInteracted(); p.onOpenSplitScreen() })
        add(FhAct("∿", tr("Stats", "إحصاءات"), p.isDiagnosticsEnabled) { p.onInteracted(); p.onToggleDiagnostics() })
        add(FhAct("⧉", "PiP") { p.onEnterPictureInPicture() })
        add(FhAct("⎚", if (p.isCastConnected) tr("Stop cast", "إيقاف البث") else tr("Cast", "بث"), p.isCastConnected) { if (p.isCastConnected) p.onStopCasting() else p.onCast() })
    }
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.align(Alignment.BottomCenter).padding(28.dp).fillMaxWidth().clip(FH.R).background(FH.Bg.copy(alpha = 0.92f)).border(1.dp, FH.Line, FH.R).fhBrackets(FH.Amber, 24.dp, 3.dp).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                ChannelHead(p.channel, null, p.displayChannelNumber, p.resolutionLabel, false)
                NowNext(p.currentProgram, p.nextProgram, Modifier.weight(1f))
                Text("T+" + fhClock(System.currentTimeMillis()), color = FH.Amber, fontFamily = FH.Mono, fontSize = 24.sp)
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                items(acts.size) { i -> FhCtl(acts[i], if (i == 0) Modifier.focusRequester(p.focusRequester) else Modifier, size = 44.dp) }
            }
        }
    }
}
