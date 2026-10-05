package com.streamvault.app.ui.themes.sabhiya

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
internal fun SabhiyaPlayerOverlay(p: PlayerOverlayParams) {
    if (!p.visible) return
    SbGlassPlayer(p)
}

@Composable
internal fun BoxScope.SbPlayerSheet(p: PlayerOverlayParams, focus: FocusRequester, onDismiss: () -> Unit) {
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
        Modifier.align(Alignment.CenterStart).fillMaxHeight().width(400.dp).background(Brush.horizontalGradient(listOf(Color(0xF0061214), Color(0xF70E0C12)))).border(1.dp, Color(0x66E50914)).padding(20.dp),
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
internal fun SabhiyaLiveChannelList(p: LiveChannelListParams) {
    val idx = remember(p.channels, p.currentChannelId) { p.channels.indexOfFirst { it.id == p.currentChannelId }.coerceAtLeast(0) }
    val state = rememberLazyListState(idx)
    var focused by remember(p.currentChannelId) { mutableStateOf(p.channels.getOrNull(idx)) }
    LaunchedEffect(Unit) { runCatching { p.focusRequester.requestFocus() } }
    Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f), Color.Black.copy(alpha = 0.92f))))) {
        Column(Modifier.align(Alignment.TopEnd).fillMaxHeight().width(540.dp).background(Brush.verticalGradient(listOf(Color(0xE6061214), Color(0xD90D0D0D)))).border(1.dp, Color(0x55E50914)).padding(start = 18.dp, top = 24.dp, bottom = 18.dp, end = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                        onClick = { p.onInteracted(); p.onSelectChannel(c.id) }, onLongClick = { p.onChannelLongPress(c) }, shape = CG.RSmall, container = if (cur) Color(0xFFE50914) else Color.Transparent,
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
internal fun SabhiyaLiveChannelInfo(p: LiveChannelInfoParams) {
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
