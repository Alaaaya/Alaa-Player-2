package com.streamvault.app.ui.themes.cardstack

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.zIndex
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.graphicsLayer
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

private class CsAct(val glyph: String, val label: String, val active: Boolean = false, val onClick: () -> Unit)

/** Circular icon with caption under it: CardStack's player control. */
@Composable
private fun CsCtl(a: CsAct, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 52.dp) {
    CsCard(a.onClick, modifier.width(size + 30.dp).height(size + 22.dp), shape = CS.RSmall, container = if (a.active) CS.Amber else CS.Raised.copy(alpha = 0.92f), focusedContainer = if (a.active) CS.Amber else CS.Card, zoom = 1.08f) {
        Column(Modifier.fillMaxSize().padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(a.glyph, color = if (a.active) CS.Bg else CS.Amber, fontSize = (size.value * 0.36f).sp, fontWeight = FontWeight.Black, maxLines = 1)
            Text(a.label, color = if (a.active) CS.Bg else CS.Sub, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun NowNext(now: Program?, next: Program?, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (now != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CsBadge(tr("NOW", "الآن"), CS.Amber, filled = true)
                Text(now.title, color = CS.Text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, false))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(csClock(now.startTime), color = CS.Sub, fontSize = 12.sp)
                CsProgress(now.progressAt(), Modifier.weight(1f))
                Text(csClock(now.endTime), color = CS.Sub, fontSize = 12.sp)
            }
        } else Text(tr("No programme information", "لا توجد معلومات عن البرنامج"), color = CS.Faint, fontSize = 14.sp)
        next?.let { Text(tr("Next", "التالي") + "  ${csClock(it.startTime)}  ·  ${it.title}", color = CS.Faint, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
    }
}

@Composable
private fun ChannelHead(channel: Channel?, name: String?, number: Int, resolution: String?, timeshift: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        CsLogo(channel?.name ?: name.orEmpty(), channel?.logoUrl, 64.dp)
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (number > 0) Text("$number", color = CS.Amber, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text(channel?.name ?: name.orEmpty(), color = CS.Text, fontSize = 24.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                if (channel?.isFavorite == true) Text("♥", color = CS.Amber, fontSize = 16.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CsBadge(if (timeshift) tr("PAUSED LIVE", "مباشر مؤجل") else "● LIVE", CS.Live, filled = !timeshift)
                (resolution ?: channel?.qualityBadge())?.let { CsBadge(it, CS.Text) }
                if (channel?.catchUpSupported == true) CsBadge(tr("CATCH-UP", "أرشيف"), CS.Blue)
            }
        }
    }
}



@Composable
internal fun CardStackPlayerOverlay(p: PlayerOverlayParams) {
    if (!p.visible) return
    if (p.isLive && !p.isCatchUpPlayback) CsLive(p) else CsVod(p)
}

/** Live: soft bottom gradient, channel head + now/next on the start, clock on the end, one row of round controls. */
@Composable
private fun CsLive(p: PlayerOverlayParams) {
    LaunchedEffect(Unit) { runCatching { p.playButtonFocusRequester.requestFocus() } }
    val rec = p.currentRecordingStatus == RecordingStatus.RECORDING
    val acts = buildList {
        add(CsAct(if (p.isPlaying) "❚❚" else "▶", if (p.isPlaying) tr("Pause", "إيقاف") else tr("Play", "تشغيل"), true) { p.onUserInteraction(); p.onTogglePlayPause() })
        add(CsAct("☰", tr("Channels", "القنوات")) { p.onOpenLiveChannels() })
        add(CsAct("▦", tr("Guide", "الدليل")) { p.onOpenLiveGuide() })
        add(CsAct(if (p.currentChannel?.isFavorite == true) "♥" else "♡", tr("My List", "قائمتي"), p.currentChannel?.isFavorite == true) { p.onToggleLiveFavorite() })
        add(CsAct("⏪", "-10s") { p.onUserInteraction(); p.onSeekBackward() })
        add(CsAct("⏩", "+10s") { p.onUserInteraction(); p.onSeekForward() })
        add(CsAct("⏮", tr("Restart", "من البداية")) { p.onRestartProgram() })
        add(CsAct("⟲", tr("Catch-up", "الأرشيف")) { p.onOpenArchive() })
        if (p.timeshiftUiState.canSeekToLive) add(CsAct("⇥", tr("Go live", "مباشر")) { p.onSeekToLiveEdge() })
        add(CsAct("CC", tr("Subtitles", "الترجمة") + " ${p.subtitleTrackCount}") { p.onOpenSubtitleTracks() })
        add(CsAct("♪", tr("Audio", "الصوت") + " ${p.audioTrackCount}") { p.onOpenAudioTracks() })
        add(CsAct("HD", p.resolutionBadgeLabel ?: tr("Quality", "الجودة")) { p.onOpenVideoTracks() })
        add(CsAct(if (p.isMuted) "🔇" else "🔊", tr("Mute", "كتم"), p.isMuted) { p.onToggleMute() })
        add(CsAct(if (rec) "■" else "●", if (rec) tr("Stop rec", "إيقاف التسجيل") else tr("Record", "تسجيل"), rec) { if (rec) p.onStopRecording() else p.onStartRecording() })
        add(CsAct("◷", tr("Schedule", "جدولة")) { p.onScheduleRecording() })
        add(CsAct("◷", tr("Daily", "يومي")) { p.onScheduleDailyRecording() })
        add(CsAct("◷", tr("Weekly", "أسبوعي")) { p.onScheduleWeeklyRecording() })
        add(CsAct("▭", p.aspectRatioLabel) { p.onToggleAspectRatio() })
        add(CsAct("»", "${p.playbackSpeed}×") { p.onOpenPlaybackSpeed() })
        if (p.audioVideoSyncEnabled) add(CsAct("⇄", tr("A/V sync", "مزامنة")) { p.onOpenAudioVideoSync() })
        add(CsAct("◫", tr("Multiview", "تقسيم")) { p.onOpenSplitScreen() })
        add(CsAct("⏲", if (p.sleepTimerUiState.stopTimerActive) csDuration(p.sleepTimerUiState.stopRemainingMs) else tr("Sleep", "مؤقت"), p.sleepTimerUiState.stopTimerActive) { p.onOpenStopPlaybackTimer() })
        add(CsAct("☾", if (p.sleepTimerUiState.idleTimerActive) csDuration(p.sleepTimerUiState.idleRemainingMs) else tr("Standby", "استعداد"), p.sleepTimerUiState.idleTimerActive) { p.onOpenIdleStandbyTimer() })
        add(CsAct("⧉", "PiP") { p.onEnterPictureInPicture() })
        add(CsAct("⎚", if (p.isCastConnected) tr("Stop cast", "إيقاف البث") else tr("Cast", "بث"), p.isCastConnected) { if (p.isCastConnected) p.onStopCasting() else p.onCast() })
        add(CsAct("✕", tr("Close", "إغلاق")) { p.onClose() })
    }
    Box(p.modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.92f))))) {
        Column(Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(horizontal = 40.dp, vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                Box(Modifier.width(130.dp).height(176.dp).graphicsLayer { rotationZ = -5f }.clip(CS.R).background(CS.Amber).padding(4.dp).clip(CS.R).background(CS.Raised)) {
                    Text(if (p.displayChannelNumber > 0) "${p.displayChannelNumber}" else "◆", color = CS.Amber, fontSize = 20.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.TopStart).padding(10.dp))
                    CsLogo(p.currentChannel?.name ?: p.currentChannelName.orEmpty(), p.currentChannel?.logoUrl, 72.dp, Modifier.align(Alignment.Center))
                }
                Column(Modifier.weight(1f).clip(CS.R).background(CS.Bg.copy(alpha = 0.9f)).padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ChannelHead(p.currentChannel, p.currentChannelName, p.displayChannelNumber, p.resolutionBadgeLabel, p.timeshiftUiState.enabledForSession && p.timeshiftUiState.bufferedBehindLiveMs > 0)
                    NowNext(p.currentProgram, p.nextProgram)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(csClock(System.currentTimeMillis()), color = CS.Amber, fontSize = 34.sp, fontWeight = FontWeight.Black)
                    if (rec) CsBadge("● REC", CS.Live, filled = true)
                }
            }
            LazyRow(Modifier.fillMaxWidth().clip(CS.R).background(CS.Bg.copy(alpha = 0.8f)).padding(10.dp).focusRequester(p.quickActionsFocusRequester), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(acts.size) { i -> CsCtl(acts[i], if (i == 0) Modifier.focusRequester(p.playButtonFocusRequester) else Modifier, size = 40.dp) }
            }
        }
    }
}

/** VOD: title top, transport as a stacked card on the END edge (-10 / play / +10), scrubber + pill row bottom, options deck slides in from the START. */
@Composable
private fun CsVod(p: PlayerOverlayParams) {
    var sheet by remember { mutableStateOf(false) }
    val sheetFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { p.playButtonFocusRequester.requestFocus() } }
    LaunchedEffect(sheet) { if (sheet) runCatching { sheetFocus.requestFocus() } }
    fun seekBy(d: Long) { p.onUserInteraction(); p.onSeekToPosition((p.currentPosition + d).coerceIn(0L, (p.duration - 1_000L).coerceAtLeast(0L))) }
    Box(p.modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.75f), Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.9f))))) {
        Row(Modifier.align(Alignment.TopStart).fillMaxWidth().padding(horizontal = 48.dp, vertical = 32.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            CsRound("←", { p.onNavigateBack() }, size = 44.dp)
            Column(Modifier.weight(1f)) {
                if (p.isCatchUpPlayback) Text(tr("Catch-up", "من الأرشيف"), color = CS.Amber, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(p.displayTitle, color = CS.Text, fontSize = 28.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                p.episodeLine?.let { Text(it, color = CS.Sub, fontSize = 15.sp, maxLines = 1) }
            }
            if (p.currentRecordingStatus == RecordingStatus.RECORDING) CsBadge("● REC", CS.Live, filled = true)
            if (p.playbackSpeed != 1f) CsBadge("${p.playbackSpeed}×", CS.Amber)
            Text(csClock(System.currentTimeMillis()), color = CS.Sub, fontSize = 20.sp)
        }
        Row(Modifier.align(Alignment.CenterEnd).padding(end = 60.dp).clip(CS.R).background(CS.Bg.copy(alpha = 0.75f)).padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            CsCtl(CsAct("↺", "10s") { seekBy(-STEP) }, size = 52.dp)
            CsCtl(CsAct(if (p.isPlaying) "❚❚" else "▶", if (p.isPlaying) tr("Pause", "إيقاف") else tr("Play", "تشغيل"), true) { p.onUserInteraction(); p.onTogglePlayPause() }, Modifier.focusRequester(p.playButtonFocusRequester), size = 72.dp)
            CsCtl(CsAct("↻", "10s") { seekBy(STEP) }, size = 52.dp)
        }
        Column(Modifier.align(Alignment.BottomEnd).fillMaxWidth(if (sheet) 0.64f else 1f).padding(horizontal = 48.dp, vertical = 28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Scrubber(p, ::seekBy)
            LazyRow(Modifier.focusRequester(p.quickActionsFocusRequester), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item { CsButton(tr("Audio", "الصوت") + " · ${p.audioTrackCount}", p.onOpenAudioTracks, icon = "♪") }
                item { CsButton(tr("Subtitles", "الترجمة") + " · ${p.subtitleTrackCount}", p.onOpenSubtitleTracks, icon = "CC") }
                item { CsButton(p.resolutionBadgeLabel ?: tr("Quality", "الجودة"), p.onOpenVideoTracks, icon = "HD") }
                if (p.showEpisodesAction) item { CsButton(tr("Episodes", "الحلقات"), p.onOpenEpisodes, icon = "≣") }
                item { CsButton(tr("Start over", "من البداية"), { p.onSeekToPosition(0L) }, icon = "⏮") }
                item { CsButton(tr("More", "المزيد"), { sheet = !sheet }, primary = sheet, icon = "⚙") }
                item { CsButton(tr("Close", "إغلاق"), p.onClose, icon = "✕") }
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
        CsCard(
            onClick = { p.onUserInteraction(); p.onTogglePlayPause() }, shape = CS.Pill, container = Color.Transparent, focusedContainer = Color.White.copy(alpha = 0.08f), zoom = 1f,
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
                CsProgress(if (p.duration > 0) (pos.toFloat() / p.duration).coerceIn(0f, 1f) else 0f, Modifier.fillMaxWidth(), if (focused) 8.dp else 4.dp)
            }
        }
        Row {
            Text(csDuration(pos), color = if (p.seekPreview.visible) CS.Amber else CS.Text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Text("-" + csDuration((p.duration - pos).coerceAtLeast(0)), color = CS.Sub, fontSize = 14.sp)
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
        add(Triple(tr("Sleep timer", "مؤقت النوم"), if (p.sleepTimerUiState.stopTimerActive) csDuration(p.sleepTimerUiState.stopRemainingMs) else tr("Off", "معطل"), p.onOpenStopPlaybackTimer))
        add(Triple(tr("Standby timer", "مؤقت الاستعداد"), if (p.sleepTimerUiState.idleTimerActive) csDuration(p.sleepTimerUiState.idleRemainingMs) else tr("Off", "معطل"), p.onOpenIdleStandbyTimer))
        add(Triple(tr("Multiview", "تقسيم الشاشة"), "", p.onOpenSplitScreen))
        add(Triple(tr("Picture in picture", "صورة داخل صورة"), "", p.onEnterPictureInPicture))
        add(Triple(tr("Cast", "البث"), if (p.isCastConnected) tr("Connected", "متصل") else "", if (p.isCastConnected) p.onStopCasting else p.onCast))
    }
    Column(
        Modifier.align(Alignment.CenterStart).padding(24.dp).fillMaxHeight(0.9f).width(380.dp).clip(CS.R).background(CS.Raised.copy(alpha = 0.97f)).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(tr("Playback options", "خيارات التشغيل"), color = CS.Text, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            CsRound("✕", onDismiss, size = 36.dp)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(rows.size) { i ->
                val (label, value, action) = rows[i]
                CsCard(onClick = action, shape = CS.RSmall, container = Color.Transparent, focusedContainer = CS.Card, zoom = 1.02f, modifier = Modifier.fillMaxWidth().then(if (i == 0) Modifier.focusRequester(focus) else Modifier)) {
                    Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(label, color = CS.Text, fontSize = 15.sp, modifier = Modifier.weight(1f), maxLines = 1)
                        Text(value, color = CS.Amber, fontSize = 14.sp, maxLines = 1)
                        Text("  ›", color = CS.Faint, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

/** Channel list: END-side deck. Rows are overlapping cards; the focused card slides out toward the video.
 *  Header shows group + count, a now/next card sits at the bottom of the deck. No recents. */
@Composable
internal fun CardStackLiveChannelList(p: LiveChannelListParams) {
    val idx = remember(p.channels, p.currentChannelId) { p.channels.indexOfFirst { it.id == p.currentChannelId }.coerceAtLeast(0) }
    val state = rememberLazyListState(idx)
    var focused by remember(p.currentChannelId) { mutableStateOf(p.channels.getOrNull(idx)) }
    LaunchedEffect(Unit) { runCatching { p.focusRequester.requestFocus() } }
    Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f), Color.Black.copy(alpha = 0.92f))))) {
        Column(Modifier.align(Alignment.TopEnd).fillMaxHeight().width(500.dp).padding(end = 36.dp, top = 28.dp, bottom = 24.dp, start = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.clip(CS.R).background(CS.Amber).padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(p.lastVisitedCategoryName ?: tr("All channels", "كل القنوات"), color = CS.Bg, fontSize = 20.sp, fontWeight = FontWeight.Black, maxLines = 1)
                    Text("${p.channels.size} " + tr("channels", "قناة"), color = CS.Bg.copy(alpha = 0.7f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                CsRound("✕", p.onDismiss, size = 36.dp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CsTab(tr("Groups", "المجموعات"), false, p.onOpenCategories)
                if (p.lastVisitedCategoryName != null) CsTab(tr("Last group", "آخر مجموعة"), false, p.onOpenLastGroup)
                CsTab(tr("Guide", "الدليل"), false, p.onOpenGuide)
            }
            LazyColumn(state = state, verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f), contentPadding = PaddingValues(vertical = 6.dp, horizontal = 10.dp)) {
                items(p.channels, key = { it.id }) { c ->
                    val cur = c.id == p.currentChannelId
                    val isF = focused?.id == c.id
                    CsCard(
                        onClick = { p.onInteracted(); p.onSelectChannel(c.id) }, container = if (cur) Color(0xFF4A2A48) else CS.Raised,
                        modifier = Modifier.fillMaxWidth().height(62.dp).offset(x = if (isF) (-14).dp else 0.dp)
                            .then(if (cur) Modifier.focusRequester(p.focusRequester) else Modifier).onFocusChanged { if (it.isFocused) { focused = c; p.onInteracted() } }
                    ) {
                        Row(Modifier.fillMaxSize().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("${p.numberOf(c)}", color = CS.Amber, fontSize = 15.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(38.dp))
                            CsLogo(c.name, c.logoUrl, 38.dp)
                            Text(c.name, color = CS.Text, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            if (cur) CsBadge(tr("Now", "الآن"), CS.Amber, filled = true)
                            c.qualityBadge()?.let { CsBadge(it, CS.Blue) }
                            if (c.catchUpSupported) CsBadge("⟲", CS.Blue)
                            if (c.isFavorite) Text("★", color = CS.Amber, fontSize = 14.sp)
                        }
                    }
                }
            }
            focused?.let { c ->
                Column(Modifier.fillMaxWidth().clip(CS.R).background(CS.Bg.copy(alpha = 0.95f)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(c.name, color = CS.Text, fontSize = 16.sp, fontWeight = FontWeight.Black, maxLines = 1)
                    NowNext(c.currentProgram, c.nextProgram)
                }
            }
        }
    }
}

/** Zap banner (bottom, auto-hides): a wide card with a channel "playing card" on the start overlapping its edge,
 *  now/next in the middle, clock on the end; controls as a row of small stacked chips. */
@Composable
internal fun CardStackLiveChannelInfo(p: LiveChannelInfoParams) {
    LaunchedEffect(Unit) { runCatching { p.focusRequester.requestFocus() } }
    val rec = p.currentRecordingStatus == RecordingStatus.RECORDING
    val acts = buildList {
        add(CsAct("☰", tr("Channels", "القنوات"), true) { p.onInteracted(); p.onOpenChannelList() })
        add(CsAct(if (p.channel?.isFavorite == true) "★" else "☆", tr("Favorites", "المفضلة"), p.channel?.isFavorite == true) { p.onInteracted(); p.onToggleFavorite() })
        add(CsAct("♪", tr("Audio", "الصوت")) { p.onInteracted(); p.onOpenAudioTracks() })
        add(CsAct("▭", p.aspectRatioLabel) { p.onInteracted(); p.onToggleAspectRatio() })
        add(CsAct("⚙", tr("Settings", "الإعدادات")) { p.onInteracted(); p.onOpenSettings() })
        add(CsAct("HD", p.resolutionLabel ?: tr("Quality", "الجودة")) { p.onInteracted(); p.onOpenVideoTracks() })
        add(CsAct("CC", tr("Subs", "الترجمة")) { p.onInteracted(); p.onOpenSubtitleTracks() })
        add(CsAct("▦", tr("Guide", "الدليل")) { p.onInteracted(); p.onOpenFullEpg() })
        add(CsAct(if (p.isPlaying) "❚❚" else "▶", if (p.isPlaying) tr("Pause", "إيقاف") else tr("Play", "تشغيل")) { p.onInteracted(); p.onTogglePlayPause() })
        add(CsAct(if (p.isMuted) "🔇" else "🔊", tr("Mute", "كتم"), p.isMuted) { p.onInteracted(); p.onToggleMute() })
        if (p.variantCount > 1) add(CsAct("⇋", tr("Source", "المصدر")) { p.onInteracted(); p.onOpenVariants() })
        add(CsAct("⏮", tr("Restart", "من البداية")) { p.onInteracted(); p.onRestartProgram() })
        add(CsAct("⟲", tr("Catch-up", "الأرشيف")) { p.onInteracted(); p.onOpenArchive() })
        if (p.canSeekToLive) add(CsAct("⇥", tr("Go live", "مباشر")) { p.onInteracted(); p.onSeekToLiveEdge() })
        add(CsAct(if (rec) "■" else "●", if (rec) tr("Stop rec", "إيقاف") else tr("Record", "تسجيل"), rec) { p.onInteracted(); if (rec) p.onStopRecording() else p.onStartRecording() })
        add(CsAct("◷", tr("Schedule", "جدولة")) { p.onInteracted(); p.onScheduleRecording() })
        add(CsAct("⇄", "A/V") { p.onInteracted(); p.onOpenAudioVideoSync() })
        add(CsAct("◫", tr("Multiview", "تقسيم")) { p.onInteracted(); p.onOpenSplitScreen() })
        add(CsAct("∿", tr("Stats", "إحصاءات"), p.isDiagnosticsEnabled) { p.onInteracted(); p.onToggleDiagnostics() })
        add(CsAct("⧉", "PiP") { p.onEnterPictureInPicture() })
        add(CsAct("⎚", if (p.isCastConnected) tr("Stop cast", "إيقاف البث") else tr("Cast", "بث"), p.isCastConnected) { if (p.isCastConnected) p.onStopCasting() else p.onCast() })
    }
    val c = p.channel
    Box(Modifier.fillMaxSize()) {
        Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(start = 40.dp, end = 40.dp, bottom = 28.dp)) {
            Column(
                Modifier.padding(start = 70.dp, top = 30.dp).fillMaxWidth().clip(CS.R).background(CS.Bg.copy(alpha = 0.93f)).padding(start = 110.dp, end = 20.dp, top = 16.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(c?.name.orEmpty(), color = CS.Text, fontSize = 22.sp, fontWeight = FontWeight.Black, maxLines = 1)
                            CsBadge("● LIVE", CS.Live, filled = true)
                            (p.resolutionLabel ?: c?.qualityBadge())?.let { CsBadge(it, CS.Blue) }
                            if (c?.catchUpSupported == true) CsBadge(tr("ARCHIVE", "أرشيف"), CS.Blue)
                        }
                        NowNext(p.currentProgram, p.nextProgram)
                    }
                    Text(csClock(System.currentTimeMillis()), color = CS.Amber, fontSize = 30.sp, fontWeight = FontWeight.Black)
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(acts.size) { i -> CsCtl(acts[i], if (i == 0) Modifier.focusRequester(p.focusRequester) else Modifier, size = 42.dp) }
                }
            }
            Box(Modifier.width(140.dp).height(190.dp).graphicsLayer { rotationZ = -6f }.clip(CS.R).background(CS.Amber).padding(4.dp).clip(CS.R).background(CS.Raised)) {
                Text(if (p.displayChannelNumber > 0) "${p.displayChannelNumber}" else "◆", color = CS.Amber, fontSize = 20.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.TopStart).padding(10.dp))
                CsLogo(c?.name.orEmpty(), c?.logoUrl, 80.dp, Modifier.align(Alignment.Center))
            }
        }
    }
}
