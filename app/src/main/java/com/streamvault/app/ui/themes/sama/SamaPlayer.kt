package com.streamvault.app.ui.themes.sama

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

@Composable
private fun CgCtl(a: CgAct, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 52.dp) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.widthIn(min = size + 16.dp)) {
        TvClickableSurface(
            onClick = a.onClick, modifier = modifier.size(size),
            shape = ClickableSurfaceDefaults.shape(CircleShape),
            colors = ClickableSurfaceDefaults.colors(containerColor = Color.White, focusedContainerColor = CG.Amber, contentColor = Color(0xFF14191D), focusedContentColor = Color(0xFF14191D)),
            border = ClickableSurfaceDefaults.border(border = Border(androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF)), shape = CircleShape)),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.06f),
            glow = ClickableSurfaceDefaults.glow(focusedGlow = Glow(CG.Amber.copy(alpha = 0.55f), 16.dp))
        ) { CgGlyph(a.glyph, size * 0.44f, Modifier.align(Alignment.Center)) }
        Text(a.label, color = if (a.active) CG.Amber else CG.Sub, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = size + 28.dp))
    }
}

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
                CgBadge(tr("NOW", "الآن"), CG.Live, filled = true)
                Text(now.title, color = CG.Text, fontSize = 18.sp, fontFamily = CG.Serif, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, false))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(cgClock(now.startTime), color = CG.Sub, fontSize = 12.sp)
                CgProgress(now.progressAt(), Modifier.weight(1f))
                Text(cgClock(now.endTime), color = CG.Sub, fontSize = 12.sp)
            }
        } else Text(tr("No programme information", "لا توجد معلومات عن البرنامج"), color = CG.Faint, fontSize = 14.sp)
        next?.let { Text(tr("Next", "التالي") + "  ${cgClock(it.startTime)}  ·  ${it.title}", color = CG.Sub, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
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
internal fun SamaPlayerOverlay(p: PlayerOverlayParams) {
    androidx.compose.animation.AnimatedVisibility(
        visible = p.visible,
        enter = androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(240)) +
            androidx.compose.animation.slideInVertically { it / 12 },
        exit = androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(180))
    ) {
        if (p.isLive && !p.isCatchUpPlayback) SamaBroadcastPlayer(p) else CgVod(p)
    }
}

@Composable
private fun SamaBroadcastPlayer(p: PlayerOverlayParams) {
    var setup by remember { mutableStateOf(false) }
    val opener = remember { FocusRequester() }
    val panelFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { p.playButtonFocusRequester.requestFocus() } }
    Box(p.modifier.fillMaxSize()) {
        Row(Modifier.align(Alignment.TopCenter).fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(0xB3000000), Color.Transparent)))
            .padding(32.dp), verticalAlignment = Alignment.CenterVertically) {
            CgLogo(p.currentChannel?.name ?: p.title, p.currentChannel?.logoUrl, 42.dp)
            Text(p.currentChannelName ?: p.currentChannel?.name.orEmpty(), color = Color.White,
                fontSize = 19.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 12.dp))
            Spacer(Modifier.weight(1f))
            SamaClock()
        }
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xD9000000))))
            .padding(start = 40.dp, end = 40.dp, top = 90.dp, bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SamaTag("● LIVE", bg = CG.Live, fg = Color.White)
            Text(p.displayTitle, color = Color.White, fontSize = 36.sp,
                fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(p.currentChannel?.name ?: p.currentChannelName.orEmpty(), color = CG.Sub, fontSize = 17.sp)
            p.currentProgram?.let { now ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(cgClock(now.startTime), color = CG.Sub, fontSize = 12.sp)
                    Box(Modifier.weight(1f)) { SamaThinProgress(now.progressAt(), height = 3.dp) }
                    Text(cgClock(now.endTime), color = CG.Sub, fontSize = 12.sp)
                }
            }
            Row(Modifier.fillMaxWidth().focusRequester(p.quickActionsFocusRequester),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                CgCtl(CgAct("☰", tr("Channels", "القنوات")) { p.onOpenLiveChannels() }, size = 44.dp)
                CgCtl(CgAct(if (p.currentChannel?.isFavorite == true) "♥" else "♡",
                    tr("Favorite", "مفضلة")) { p.onToggleLiveFavorite() }, size = 44.dp)
                Spacer(Modifier.weight(1f))
                CgCtl(CgAct("⏪", "-10s") { p.onUserInteraction(); p.onSeekBackward() }, size = 48.dp)
                CgCtl(CgAct(if (p.isPlaying) "❚❚" else "▶", tr("Play / Pause", "تشغيل / إيقاف"))
                    { p.onUserInteraction(); p.onTogglePlayPause() },
                    Modifier.focusRequester(p.playButtonFocusRequester), size = 68.dp)
                CgCtl(CgAct("⏩", "+10s") { p.onUserInteraction(); p.onSeekForward() }, size = 48.dp)
                Spacer(Modifier.weight(1f))
                CgCtl(CgAct("CC", tr("Subtitles", "الترجمة")) { p.onOpenSubtitleTracks() }, size = 44.dp)
                CgCtl(CgAct("⚙", tr("Settings", "الإعدادات")) { setup = true },
                    Modifier.focusRequester(opener), size = 44.dp)
            }
        }
        if (setup) InnerPanelBackScope({ setup = false }, opener) {
            Box(Modifier.fillMaxSize()) { SideSheet(p, panelFocus) { setup = false } }
            LaunchedEffect(Unit) { runCatching { panelFocus.requestFocus() } }
        }
    }
}

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
            Text("  /  " + cgDuration(p.duration), color = CG.Sub, fontSize = 13.sp)
            Spacer(Modifier.weight(1f))
            if (p.duration > 0) Text(tr("Ends at", "ينتهي") + " " + cgClock(System.currentTimeMillis() + remaining) + "   ", color = CG.Sub, fontSize = 12.sp)
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
        Modifier.align(Alignment.CenterStart).fillMaxHeight().width(400.dp).background(Brush.horizontalGradient(listOf(Color(0xF710171C), Color(0xF7080B0E)))).border(1.dp, Color(0x66FFFFFF)).padding(20.dp),
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

@Composable
internal fun SamaLiveChannelList(p: LiveChannelListParams) {
    val idx = remember(p.channels, p.currentChannelId) { p.channels.indexOfFirst { it.id == p.currentChannelId }.coerceAtLeast(0) }
    val state = rememberLazyListState(idx)
    var focused by remember(p.currentChannelId) { mutableStateOf(p.channels.getOrNull(idx)) }
    LaunchedEffect(Unit) { runCatching { p.focusRequester.requestFocus() } }
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }
    val slide by androidx.compose.animation.core.animateFloatAsState(
        if (entered) 0f else -420f, androidx.compose.animation.core.tween(260), label = "sama-channel-drawer")
    Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0x66000000), Color.Transparent)))) {
        Column(Modifier.align(Alignment.TopStart).offset(x = slide.dp).fillMaxHeight().width(400.dp).background(Brush.verticalGradient(listOf(Color(0xF210171C), Color(0xF2080B0E)))).border(1.dp, Color(0x33FFFFFF)).padding(start = 18.dp, top = 24.dp, bottom = 18.dp, end = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(CG.Amber))
            LazyColumn(state = state, verticalArrangement = Arrangement.spacedBy(1.dp), modifier = Modifier.weight(1f)) {
                items(p.channels, key = { it.id }) { c ->
                    val cur = c.id == p.currentChannelId
                    CgCard(
                        onClick = { p.onInteracted(); p.onSelectChannel(c.id) }, onLongClick = { p.onChannelLongPress(c) }, shape = CG.RSmall, container = if (cur) Color(0xFF35414A) else Color.Transparent,
                        modifier = Modifier.fillMaxWidth().then(if (cur) Modifier.focusRequester(p.focusRequester) else Modifier).onFocusChanged { if (it.isFocused) { focused = c; p.onInteracted() } }
                    ) {
                        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("${p.numberOf(c)}", color = CG.Blue, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp))
                            CgLogo(c.name, c.logoUrl, 36.dp)
                            Column(Modifier.weight(1f)) {
                                Text(if (c.id == p.movingChannelId) "⇅  ${c.name}" else c.name, color = CG.Text, fontSize = 15.sp, fontFamily = CG.Serif, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                c.currentProgram?.let { Text(it.title, color = CG.Faint, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                            }
                            if (cur) SamaTag(tr("Now", "الآن"))
                            c.qualityBadge()?.let { SamaTag(it, fg = Color.White, outlined = true) }
                            if (c.catchUpSupported) CgBadge("⟲ " + tr("Archive", "أرشيف"), CG.Blue)
                            if (c.isFavorite) CgGlyph("♥", 16.dp, tint = CG.Amber)
                        }
                    }
                }
            }
        }
        focused?.let { c ->
            Column(Modifier.align(Alignment.BottomEnd).padding(end = 24.dp, bottom = 32.dp).width(320.dp).clip(RoundedCornerShape(18.dp)).background(Color(0xEE12191E)).border(1.dp, CG.Amber, RoundedCornerShape(18.dp)).padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CgHeading(c.name, size = 12)
                NowNext(c.currentProgram, c.nextProgram)
            }
        }
    }
}

@Composable
internal fun SamaLiveChannelInfo(p: LiveChannelInfoParams) {
    LaunchedEffect(Unit) { runCatching { p.focusRequester.requestFocus() } }
    val rec = p.currentRecordingStatus == RecordingStatus.RECORDING
    Box(Modifier.fillMaxSize()) {
        Row(Modifier.align(Alignment.TopEnd).padding(30.dp)) { SamaClock() }
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xE6000000))))
            .padding(start = 40.dp, end = 40.dp, top = 80.dp, bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column(Modifier.weight(1.25f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SamaTag("● LIVE", bg = CG.Live, fg = Color.White)
                    Text(p.currentProgram?.title ?: p.channel?.name.orEmpty(), color = Color.White,
                        fontSize = 30.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(p.channel?.name.orEmpty(), color = CG.Sub, fontSize = 16.sp)
                    p.currentProgram?.description?.takeIf { it.isNotBlank() }?.let {
                        Text(it, color = CG.Sub, fontSize = 13.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    }
                }
                Box(Modifier.width(1.dp).height(135.dp).background(CG.Line))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(tr("Now", "الآن"), color = CG.Sub, fontSize = 14.sp)
                    Text(p.currentProgram?.title ?: tr("No programme information", "لا توجد معلومات"),
                        color = Color.White, fontSize = 20.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    p.currentProgram?.let { Text(cgClock(it.startTime) + " - " + cgClock(it.endTime),
                        color = CG.Sub, fontSize = 12.sp) }
                }
                Box(Modifier.width(1.dp).height(135.dp).background(CG.Line))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(tr("Next", "التالي"), color = CG.Sub, fontSize = 14.sp)
                    Text(p.nextProgram?.title ?: "—", color = Color.White, fontSize = 20.sp,
                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                    p.nextProgram?.let { Text(cgClock(it.startTime) + " - " + cgClock(it.endTime),
                        color = CG.Sub, fontSize = 12.sp) }
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    p.currentProgram?.let { now ->
                        SamaThinProgress(now.progressAt(), height = 3.dp)
                        Row(Modifier.fillMaxWidth()) {
                            Text(cgClock(now.startTime), color = CG.Sub, fontSize = 12.sp)
                            Spacer(Modifier.weight(1f))
                            Text(cgClock(now.endTime), color = CG.Sub, fontSize = 12.sp)
                        }
                    }
                }
                CgCtl(CgAct(if (rec) "■" else "●",
                    if (rec) tr("Stop rec", "إيقاف") else tr("Record", "تسجيل")) {
                    p.onInteracted(); if (rec) p.onStopRecording() else p.onStartRecording()
                }, Modifier.focusRequester(p.focusRequester), size = 48.dp)
                CgCtl(CgAct(if (p.channel?.isFavorite == true) "♥" else "♡", tr("Favorite", "مفضلة")) {
                    p.onInteracted(); p.onToggleFavorite()
                }, size = 48.dp)
                CgCtl(CgAct("♪", tr("Audio", "الصوت")) { p.onInteracted(); p.onOpenAudioTracks() }, size = 48.dp)
                CgCtl(CgAct("☰", tr("Channels", "القنوات")) { p.onInteracted(); p.onOpenChannelList() }, size = 40.dp)
                CgCtl(CgAct("⚙", tr("Settings", "الإعدادات")) { p.onInteracted(); p.onOpenSettings() }, size = 40.dp)
            }
        }
    }
}
