package com.streamvault.app.ui.themes.cyanpro

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import com.streamvault.app.ui.themes.bespoke.InnerPanelBackScope
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
import androidx.compose.foundation.shape.CircleShape
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Glow
import com.streamvault.app.ui.interaction.TvClickableSurface
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

private class CgAct(val glyph: String, val label: String, val active: Boolean = false, val onClick: () -> Unit)

/** Round glossy transport key: circular glyph button with a small caption below, blue glow on focus. */
@Composable
private fun CgCtl(a: CgAct, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 52.dp) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.widthIn(min = size + 16.dp)) {
        TvClickableSurface(
            onClick = a.onClick, modifier = modifier.size(size),
            shape = ClickableSurfaceDefaults.shape(CircleShape),
            colors = ClickableSurfaceDefaults.colors(containerColor = if (a.active) CG.AmberDeep else Color(0xB3222228), focusedContainerColor = CG.Amber, contentColor = CG.Text, focusedContentColor = Color.White),
            border = ClickableSurfaceDefaults.border(border = Border(androidx.compose.foundation.BorderStroke(1.dp, CG.Line), shape = CircleShape)),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.12f),
            glow = ClickableSurfaceDefaults.glow(focusedGlow = Glow(CG.Blue.copy(alpha = 0.6f), 16.dp))
        ) { CgGlyph(a.glyph, size * 0.44f, Modifier.align(Alignment.Center)) }
        Text(a.label, color = if (a.active) CG.Blue else CG.Sub, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = size + 28.dp))
    }
}

/** Fixed-width action strip: up to [primary] keys centered, the rest paged behind a "More" key, so nothing is clipped. */
@Composable
private fun CgActionStrip(acts: List<CgAct>, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 50.dp, primary: Int = 10, firstModifier: Modifier = Modifier) {
    var page by remember { mutableStateOf(0) }
    val pages = remember(acts.size) { if (acts.size <= primary + 1) listOf(acts) else listOf(acts.take(primary)) + acts.drop(primary).chunked(primary) }
    val shown = pages[page.coerceIn(0, pages.lastIndex)]
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.Top) {
        shown.forEachIndexed { i, a -> CgCtl(a, if (page == 0 && i == 0) firstModifier else Modifier, size = size) }
        if (pages.size > 1) {
            val last = page >= pages.lastIndex
            CgCtl(CgAct(if (last) "←" else "⋯", if (last) tr("Back", "رجوع") else tr("More", "المزيد")) { page = if (last) 0 else page + 1 }, size = size)
        }
    }
}

@Composable
private fun NowNext(now: Program?, next: Program?, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (now != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CgBadge(tr("NOW", "الآن"), CG.Amber, filled = true)
                Text(now.title, color = CG.Text, fontSize = 18.sp, fontFamily = CG.Serif, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, false))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(cgClock(now.startTime), color = CG.Sub, fontSize = 12.sp)
                CgProgress(now.progressAt(), Modifier.weight(1f))
                Text(cgClock(now.endTime), color = CG.Sub, fontSize = 12.sp)
            }
        } else Text(tr("No programme information", "لا توجد معلومات عن البرنامج"), color = CG.Faint, fontSize = 14.sp)
        next?.let { Text(tr("Next", "التالي") + "  ${cgClock(it.startTime)}  ·  ${it.title}", color = CG.Faint, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
    }
}

@Composable
private fun ChannelHead(channel: Channel?, name: String?, number: Int, resolution: String?, timeshift: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(Modifier.size(64.dp).border(1.dp, CG.Amber, CG.RSmall).background(CG.Raised), contentAlignment = Alignment.Center) {
            Text(if (number > 0) "%03d".format(number) else "—", color = CG.Amber, fontSize = 20.sp, fontFamily = CG.Serif)
        }
        CgLogo(channel?.name ?: name.orEmpty(), channel?.logoUrl, 64.dp)
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(channel?.name ?: name.orEmpty(), color = CG.Text, fontSize = 24.sp, fontFamily = CG.Serif, maxLines = 1)
                if (channel?.isFavorite == true) Text("★", color = CG.Amber, fontSize = 16.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CgBadge(if (timeshift) tr("PAUSED LIVE", "مباشر مؤجل") else "● LIVE", CG.Live, filled = !timeshift)
                (resolution ?: channel?.qualityBadge())?.let { CgBadge(it, CG.Text) }
                if (channel?.catchUpSupported == true) CgBadge(tr("CATCH-UP", "أرشيف"), CG.Blue)
            }
        }
    }
}



@Composable
internal fun CyanProPlayerOverlay(p: PlayerOverlayParams) {
    if (!p.visible) return
    if (p.isLive && !p.isCatchUpPlayback) CgLive(p) else CgVod(p)
}

/** Live: receiver OSD. Top walnut band (number box, logo, name, clock), bottom front panel with now/next + full-width key strip. */
@Composable
private fun CgLive(p: PlayerOverlayParams) {
    LaunchedEffect(Unit) { runCatching { p.playButtonFocusRequester.requestFocus() } }
    val rec = p.currentRecordingStatus == RecordingStatus.RECORDING
    val acts = buildList {
        add(CgAct(if (p.isPlaying) "❚❚" else "▶", if (p.isPlaying) tr("Pause", "إيقاف") else tr("Play", "تشغيل"), true) { p.onUserInteraction(); p.onTogglePlayPause() })
        add(CgAct("☰", tr("Channels", "القنوات")) { p.onOpenLiveChannels() })
        add(CgAct("▦", tr("Guide", "الدليل")) { p.onOpenLiveGuide() })
        add(CgAct(if (p.currentChannel?.isFavorite == true) "♥" else "♡", tr("My List", "قائمتي"), p.currentChannel?.isFavorite == true) { p.onToggleLiveFavorite() })
        add(CgAct("⏪", "-10s") { p.onUserInteraction(); p.onSeekBackward() })
        add(CgAct("⏩", "+10s") { p.onUserInteraction(); p.onSeekForward() })
        add(CgAct("⏮", tr("Restart", "من البداية")) { p.onRestartProgram() })
        add(CgAct("⟲", tr("Catch-up", "الأرشيف")) { p.onOpenArchive() })
        if (p.timeshiftUiState.canSeekToLive) add(CgAct("⇥", tr("Go live", "مباشر")) { p.onSeekToLiveEdge() })
        add(CgAct("CC", tr("Subtitles", "الترجمة") + " ${p.subtitleTrackCount}") { p.onOpenSubtitleTracks() })
        add(CgAct("♪", tr("Audio", "الصوت") + " ${p.audioTrackCount}") { p.onOpenAudioTracks() })
        add(CgAct("HD", p.resolutionBadgeLabel ?: tr("Quality", "الجودة")) { p.onOpenVideoTracks() })
        add(CgAct(if (p.isMuted) "🔇" else "🔊", tr("Mute", "كتم"), p.isMuted) { p.onToggleMute() })
        add(CgAct(if (rec) "■" else "●", if (rec) tr("Stop rec", "إيقاف التسجيل") else tr("Record", "تسجيل"), rec) { if (rec) p.onStopRecording() else p.onStartRecording() })
        add(CgAct("◷", tr("Schedule", "جدولة")) { p.onScheduleRecording() })
        add(CgAct("◷", tr("Daily", "يومي")) { p.onScheduleDailyRecording() })
        add(CgAct("◷", tr("Weekly", "أسبوعي")) { p.onScheduleWeeklyRecording() })
        add(CgAct("▭", p.aspectRatioLabel) { p.onToggleAspectRatio() })
        add(CgAct("»", "${p.playbackSpeed}×") { p.onOpenPlaybackSpeed() })
        if (p.audioVideoSyncEnabled) add(CgAct("⇄", tr("A/V sync", "مزامنة")) { p.onOpenAudioVideoSync() })
        add(CgAct("◫", tr("Multiview", "تقسيم")) { p.onOpenSplitScreen() })
        add(CgAct("⏲", if (p.sleepTimerUiState.stopTimerActive) cgDuration(p.sleepTimerUiState.stopRemainingMs) else tr("Sleep", "مؤقت"), p.sleepTimerUiState.stopTimerActive) { p.onOpenStopPlaybackTimer() })
        add(CgAct("☾", if (p.sleepTimerUiState.idleTimerActive) cgDuration(p.sleepTimerUiState.idleRemainingMs) else tr("Standby", "استعداد"), p.sleepTimerUiState.idleTimerActive) { p.onOpenIdleStandbyTimer() })
        add(CgAct("⧉", "PiP") { p.onEnterPictureInPicture() })
        add(CgAct("⎚", if (p.isCastConnected) tr("Stop cast", "إيقاف البث") else tr("Cast", "بث"), p.isCastConnected) { if (p.isCastConnected) p.onStopCasting() else p.onCast() })
        add(CgAct("✕", tr("Close", "إغلاق")) { p.onClose() })
    }
    Box(p.modifier.fillMaxSize()) {
        // top OSD: channel head + clock on a walnut band with brass underline
        Column(Modifier.align(Alignment.TopCenter).fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.78f), Color.Black.copy(alpha = 0.35f), Color.Transparent))).padding(start = 40.dp, end = 40.dp, top = 22.dp, bottom = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) { ChannelHead(p.currentChannel, p.currentChannelName, p.displayChannelNumber, p.resolutionBadgeLabel, p.timeshiftUiState.enabledForSession && p.timeshiftUiState.bufferedBehindLiveMs > 0) }
                if (rec) CpTag("REC") else CpTag("LIVE")
                Spacer(Modifier.width(16.dp))
                Text(cgClock(System.currentTimeMillis()), color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(16.dp)); CpLogo(compact = true)
            }
        }
        // bottom: translucent gradient with now/next + paged key strip
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f), Color.Black.copy(alpha = 0.88f)))).padding(start = 40.dp, end = 40.dp, top = 70.dp, bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                NowNext(p.currentProgram, p.nextProgram, Modifier.fillMaxWidth())
                CgActionStrip(acts, Modifier.focusRequester(p.quickActionsFocusRequester), 50.dp, firstModifier = Modifier.focusRequester(p.playButtonFocusRequester))
            }
        }
    }
}

/** VOD: serif title above a brass-ruled front panel: inline back/-10/play/+10 keys + scrubber, option keys below, setup menu from START. */
@Composable
private fun CgVod(p: PlayerOverlayParams) {
    var sheet by remember { mutableStateOf(false) }
    val sheetFocus = remember { FocusRequester() }
    val moreFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { p.playButtonFocusRequester.requestFocus() } }
    LaunchedEffect(sheet) { if (sheet) runCatching { sheetFocus.requestFocus() } }
    fun seekBy(d: Long) { p.onUserInteraction(); p.onSeekToPosition((p.currentPosition + d).coerceIn(0L, (p.duration - 1_000L).coerceAtLeast(0L))) }
    Box(p.modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.45f), Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.55f), Color.Black.copy(alpha = 0.9f))))) {
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 40.dp, vertical = 10.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Column(Modifier.weight(1f)) {
                    CgHeading(if (p.isCatchUpPlayback) tr("Catch-up", "من الأرشيف") else tr("Now playing", "يعرض الآن"), size = 11)
                    Text(p.displayTitle, color = CG.Text, fontSize = 30.sp, fontFamily = CG.Serif, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    p.episodeLine?.let { Text(it, color = CG.Sub, fontSize = 14.sp, maxLines = 1) }
                }
                if (p.currentRecordingStatus == RecordingStatus.RECORDING) CgBadge("● REC", CG.Live, filled = true)
                if (p.playbackSpeed != 1f) CgBadge("${p.playbackSpeed}×", CG.Amber)
                Text(cgClock(System.currentTimeMillis()), color = CG.Amber, fontSize = 24.sp, fontFamily = CG.Serif)
            }
        Column(Modifier.fillMaxWidth().padding(horizontal = 40.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Scrubber(p, ::seekBy)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterHorizontally)) {
                CgCtl(CgAct("←", tr("Back", "رجوع")) { p.onNavigateBack() }, size = 48.dp)
                CgCtl(CgAct("↺", "-10s") { seekBy(-STEP) }, size = 56.dp)
                CgCtl(CgAct(if (p.isPlaying) "❚❚" else "▶", if (p.isPlaying) tr("Pause", "إيقاف") else tr("Play", "تشغيل"), true) { p.onUserInteraction(); p.onTogglePlayPause() }, Modifier.focusRequester(p.playButtonFocusRequester), size = 72.dp)
                CgCtl(CgAct("↻", "+10s") { seekBy(STEP) }, size = 56.dp)
                if (p.showEpisodesAction) CgCtl(CgAct("≣", tr("Episodes", "الحلقات")) { p.onOpenEpisodes() }, size = 48.dp)
            }
            LazyRow(Modifier.fillMaxWidth().focusRequester(p.quickActionsFocusRequester), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
                item { CgButton(tr("Audio", "الصوت") + " · ${p.audioTrackCount}", p.onOpenAudioTracks, icon = "♪") }
                item { CgButton(tr("Subtitles", "الترجمة") + " · ${p.subtitleTrackCount}", p.onOpenSubtitleTracks, icon = "CC") }
                item { CgButton(p.resolutionBadgeLabel ?: tr("Quality", "الجودة"), p.onOpenVideoTracks, icon = "HD") }
                item { CgButton(tr("Start over", "من البداية"), { p.onSeekToPosition(0L) }, icon = "⏮") }
                item { androidx.compose.foundation.layout.Box(Modifier.focusRequester(moreFocus)) { CgButton(tr("More", "المزيد"), { sheet = !sheet }, primary = sheet, icon = "⚙") } }
                item { CgButton(tr("Close", "إغلاق"), p.onClose, icon = "✕") }
            }
        }
        }
        if (sheet) InnerPanelBackScope(onClose = { sheet = false }, opener = moreFocus) { SideSheet(p, sheetFocus) { sheet = false; runCatching { moreFocus.requestFocus() } } }
    }
}

@Composable
private fun Scrubber(p: PlayerOverlayParams, seekBy: (Long) -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val pos = if (p.seekPreview.visible) p.seekPreview.positionMs else p.currentPosition
    val frac = if (p.duration > 0) (pos.toFloat() / p.duration).coerceIn(0f, 1f) else 0f
    val remaining = (p.duration - pos).coerceAtLeast(0)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        CgCard(
            onClick = { p.onUserInteraction(); p.onTogglePlayPause() }, shape = CG.Pill, container = Color.Transparent, focusedContainer = Color.White.copy(alpha = 0.06f), zoom = 1f,
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
            BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 12.dp), contentAlignment = Alignment.CenterStart) {
                val h = if (focused) 8.dp else 5.dp
                Box(Modifier.fillMaxWidth().height(h).clip(CG.Pill).background(Color.White.copy(alpha = 0.18f))) {
                    Box(Modifier.fillMaxWidth(frac).fillMaxHeight().background(Brush.horizontalGradient(listOf(CG.AmberDeep, CG.Blue))))
                }
                val thumb = if (focused) 18.dp else 12.dp
                Box(Modifier.offset(x = (maxWidth - thumb) * frac).size(thumb).background(Color.White, CircleShape).border(2.dp, CG.Blue, CircleShape))
            }
        }
        Row(Modifier.padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(cgDuration(pos), color = if (p.seekPreview.visible) CG.Blue else CG.Text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text("  /  " + cgDuration(p.duration), color = CG.Faint, fontSize = 13.sp)
            Spacer(Modifier.weight(1f))
            if (p.duration > 0) Text(tr("Ends at", "ينتهي") + " " + cgClock(System.currentTimeMillis() + remaining) + "   ", color = CG.Faint, fontSize = 12.sp)
            Text("-" + cgDuration(remaining), color = CG.Sub, fontSize = 14.sp)
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
        add(Triple(tr("Sleep timer", "مؤقت النوم"), if (p.sleepTimerUiState.stopTimerActive) cgDuration(p.sleepTimerUiState.stopRemainingMs) else tr("Off", "معطل"), p.onOpenStopPlaybackTimer))
        add(Triple(tr("Standby timer", "مؤقت الاستعداد"), if (p.sleepTimerUiState.idleTimerActive) cgDuration(p.sleepTimerUiState.idleRemainingMs) else tr("Off", "معطل"), p.onOpenIdleStandbyTimer))
        add(Triple(tr("Multiview", "تقسيم الشاشة"), "", p.onOpenSplitScreen))
        add(Triple(tr("Picture in picture", "صورة داخل صورة"), "", p.onEnterPictureInPicture))
        add(Triple(tr("Cast", "البث"), if (p.isCastConnected) tr("Connected", "متصل") else "", if (p.isCastConnected) p.onStopCasting else p.onCast))
    }
    Column(
        Modifier.align(Alignment.CenterStart).fillMaxHeight().width(400.dp).background(Brush.horizontalGradient(listOf(Color(0xF0061214), Color(0xF70A0A0C)))).border(1.dp, Color(0x6600E5FF)).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CgHeading(tr("Playback setup", "إعداد التشغيل"), size = 14, modifier = Modifier.weight(1f))
            CgRound("✕", onDismiss, size = 36.dp)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(rows.size) { i ->
                val (label, value, action) = rows[i]
                CgCard(onClick = action, shape = CG.RSmall, container = Color.Transparent, focusedContainer = CG.Card, zoom = 1.02f, modifier = Modifier.fillMaxWidth().then(if (i == 0) Modifier.focusRequester(focus) else Modifier)) {
                    Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(label, color = CG.Text, fontSize = 15.sp, fontFamily = CG.Serif, modifier = Modifier.weight(1f), maxLines = 1)
                        Text(value, color = CG.Amber, fontSize = 14.sp, maxLines = 1)
                        CgGlyph("‹", 16.dp, tint = CG.Faint)
                    }
                }
            }
        }
    }
}

/** Channel list: END-side walnut ledger panel, count header, ruled rows (number/logo/name/quality/archive/Now), now/next card bottom START. */
@Composable
internal fun CyanProLiveChannelList(p: LiveChannelListParams) {
    val idx = remember(p.channels, p.currentChannelId) { p.channels.indexOfFirst { it.id == p.currentChannelId }.coerceAtLeast(0) }
    val state = rememberLazyListState(idx)
    var focused by remember(p.currentChannelId) { mutableStateOf(p.channels.getOrNull(idx)) }
    LaunchedEffect(Unit) { runCatching { p.focusRequester.requestFocus() } }
    Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f), Color.Black.copy(alpha = 0.92f))))) {
        Column(Modifier.align(Alignment.TopEnd).fillMaxHeight().width(540.dp).background(Brush.verticalGradient(listOf(Color(0xE6061214), Color(0xD90D0D0D)))).border(1.dp, Color(0x5500E5FF)).padding(start = 18.dp, top = 24.dp, bottom = 18.dp, end = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    CgHeading(tr("Channel list", "قائمة القنوات"), size = 11)
                    Text(p.lastVisitedCategoryName ?: tr("All channels", "كل القنوات"), color = CG.Text, fontSize = 22.sp, fontFamily = CG.Serif, maxLines = 1)
                    Text("${p.channels.size} " + tr("channels", "قناة"), color = CG.Faint, fontSize = 13.sp)
                }
                CgRound("✕", p.onDismiss, size = 40.dp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CgTab(tr("Categories", "الفئات"), false, p.onOpenCategories)
                if (p.lastVisitedCategoryName != null) CgTab(tr("Last group", "آخر مجموعة"), false, p.onOpenLastGroup)
                CgTab(tr("Guide", "الدليل"), false, p.onOpenGuide)
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(CG.Amber))
            LazyColumn(state = state, verticalArrangement = Arrangement.spacedBy(1.dp), modifier = Modifier.weight(1f)) {
                items(p.channels, key = { it.id }) { c ->
                    val cur = c.id == p.currentChannelId
                    CgCard(
                        onClick = { p.onInteracted(); p.onSelectChannel(c.id) }, onLongClick = { p.onChannelLongPress(c) }, shape = CG.RSmall, container = if (cur) Color(0xFF06333B) else Color.Transparent,
                        modifier = Modifier.fillMaxWidth().then(if (cur) Modifier.focusRequester(p.focusRequester) else Modifier).onFocusChanged { if (it.isFocused) { focused = c; p.onInteracted() } }
                    ) {
                        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("${p.numberOf(c)}", color = CG.Blue, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp))
                            CgLogo(c.name, c.logoUrl, 36.dp)
                            Column(Modifier.weight(1f)) {
                                Text(if (c.id == p.movingChannelId) "⇅  ${c.name}" else c.name, color = CG.Text, fontSize = 15.sp, fontFamily = CG.Serif, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                c.currentProgram?.let { Text(it.title, color = CG.Faint, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                            }
                            if (cur) CpTag(tr("Now", "الآن"))
                            c.qualityBadge()?.let { CpTag(it, fg = Color.White, outlined = true) }
                            if (c.catchUpSupported) CgBadge("⟲ " + tr("Archive", "أرشيف"), CG.Blue)
                            if (c.isFavorite) CgGlyph("♥", 16.dp, tint = CG.Amber)
                        }
                    }
                }
            }
        }
        focused?.let { c ->
            Column(Modifier.align(Alignment.BottomStart).padding(start = 24.dp, bottom = 32.dp).width(360.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xEE141418)).border(1.dp, CG.Amber, RoundedCornerShape(14.dp)).padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CgHeading(c.name, size = 12)
                NowNext(c.currentProgram, c.nextProgram)
            }
        }
    }
}

/** Zap banner: full-width bottom OSD band with brass rule, number box head, now/next and key strip. */
@Composable
internal fun CyanProLiveChannelInfo(p: LiveChannelInfoParams) {
    LaunchedEffect(Unit) { runCatching { p.focusRequester.requestFocus() } }
    val rec = p.currentRecordingStatus == RecordingStatus.RECORDING
    // Alaa's required order first: Channels, Favorites, Audio, Aspect, Settings, Quality, Subs, Guide.
    val acts = buildList {
        add(CgAct("☰", tr("Channels", "القنوات"), true) { p.onInteracted(); p.onOpenChannelList() })
        add(CgAct(if (p.channel?.isFavorite == true) "♥" else "♡", tr("Favorites", "المفضلة"), p.channel?.isFavorite == true) { p.onInteracted(); p.onToggleFavorite() })
        add(CgAct("♪", tr("Audio", "الصوت")) { p.onInteracted(); p.onOpenAudioTracks() })
        add(CgAct("▭", tr("Aspect", "العرض") + " " + p.aspectRatioLabel) { p.onInteracted(); p.onToggleAspectRatio() })
        add(CgAct("⚙", tr("Settings", "الإعدادات")) { p.onInteracted(); p.onOpenSettings() })
        add(CgAct("HD", p.resolutionLabel ?: tr("Quality", "الجودة")) { p.onInteracted(); p.onOpenVideoTracks() })
        add(CgAct("CC", tr("Subs", "الترجمة")) { p.onInteracted(); p.onOpenSubtitleTracks() })
        add(CgAct("▦", tr("Guide", "الدليل")) { p.onInteracted(); p.onOpenFullEpg() })
        add(CgAct(if (p.isPlaying) "❚❚" else "▶", if (p.isPlaying) tr("Pause", "إيقاف") else tr("Play", "تشغيل")) { p.onInteracted(); p.onTogglePlayPause() })
        add(CgAct(if (p.isMuted) "🔇" else "🔊", tr("Mute", "كتم"), p.isMuted) { p.onInteracted(); p.onToggleMute() })
        if (p.variantCount > 1) add(CgAct("⇋", tr("Source", "المصدر")) { p.onInteracted(); p.onOpenVariants() })
        add(CgAct("⏮", tr("Restart", "من البداية")) { p.onInteracted(); p.onRestartProgram() })
        add(CgAct("⟲", tr("Catch-up", "الأرشيف")) { p.onInteracted(); p.onOpenArchive() })
        if (p.canSeekToLive) add(CgAct("⇥", tr("Go live", "مباشر")) { p.onInteracted(); p.onSeekToLiveEdge() })
        add(CgAct(if (rec) "■" else "●", if (rec) tr("Stop rec", "إيقاف") else tr("Record", "تسجيل"), rec) { p.onInteracted(); if (rec) p.onStopRecording() else p.onStartRecording() })
        add(CgAct("◷", tr("Schedule", "جدولة")) { p.onInteracted(); p.onScheduleRecording() })
        add(CgAct("⇄", "A/V") { p.onInteracted(); p.onOpenAudioVideoSync() })
        add(CgAct("◫", tr("Multiview", "تقسيم")) { p.onInteracted(); p.onOpenSplitScreen() })
        add(CgAct("∿", tr("Stats", "إحصاءات"), p.isDiagnosticsEnabled) { p.onInteracted(); p.onToggleDiagnostics() })
        add(CgAct("⧉", "PiP") { p.onEnterPictureInPicture() })
        add(CgAct("⎚", if (p.isCastConnected) tr("Stop cast", "إيقاف البث") else tr("Cast", "بث"), p.isCastConnected) { if (p.isCastConnected) p.onStopCasting() else p.onCast() })
    }
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f), Color.Black.copy(alpha = 0.9f)))).padding(top = 60.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(Modifier.padding(horizontal = 40.dp).padding(bottom = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                ChannelHead(p.channel, null, p.displayChannelNumber, p.resolutionLabel, false)
                NowNext(p.currentProgram, p.nextProgram, Modifier.weight(1f))
                Text(cgClock(System.currentTimeMillis()), color = CG.Amber, fontSize = 28.sp, fontFamily = CG.Serif)
            }
            CgActionStrip(acts, size = 48.dp, firstModifier = Modifier.focusRequester(p.focusRequester))
            }
        }
    }
}
