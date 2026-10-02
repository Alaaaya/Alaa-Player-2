package com.streamvault.app.ui.themes.auroralounge

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

private class AlAct(val glyph: String, val label: String, val active: Boolean = false, val onClick: () -> Unit)

/** Circular icon with caption under it: AuroraLounge's player control. */
@Composable
private fun AlCtl(a: AlAct, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 52.dp) {
    Column(modifier.width(size + 22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        AlRound(a.glyph, a.onClick, size = size, active = a.active)
        Text(a.label, color = AL.Sub, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun NowNext(now: Program?, next: Program?, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (now != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AlBadge(tr("NOW", "الآن"), AL.Amber, filled = true)
                Text(now.title, color = AL.Text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, false))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(alClock(now.startTime), color = AL.Sub, fontSize = 12.sp)
                AlProgress(now.progressAt(), Modifier.weight(1f))
                Text(alClock(now.endTime), color = AL.Sub, fontSize = 12.sp)
            }
        } else Text(tr("No programme information", "لا توجد معلومات عن البرنامج"), color = AL.Faint, fontSize = 14.sp)
        next?.let { Text(tr("Next", "التالي") + "  ${alClock(it.startTime)}  ·  ${it.title}", color = AL.Faint, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
    }
}

@Composable
private fun ChannelHead(channel: Channel?, name: String?, number: Int, resolution: String?, timeshift: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        AlLogo(channel?.name ?: name.orEmpty(), channel?.logoUrl, 64.dp)
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (number > 0) Text("$number", color = AL.Amber, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text(channel?.name ?: name.orEmpty(), color = AL.Text, fontSize = 24.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                if (channel?.isFavorite == true) Text("♥", color = AL.Amber, fontSize = 16.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AlBadge(if (timeshift) tr("PAUSED LIVE", "مباشر مؤجل") else "● LIVE", AL.Live, filled = !timeshift)
                (resolution ?: channel?.qualityBadge())?.let { AlBadge(it, AL.Text) }
                if (channel?.catchUpSupported == true) AlBadge(tr("CATCH-UP", "أرشيف"), AL.Blue)
            }
        }
    }
}



@Composable
internal fun AuroraLoungePlayerOverlay(p: PlayerOverlayParams) {
    if (!p.visible) return
    if (p.isLive && !p.isCatchUpPlayback) AlLive(p) else AlVod(p)
}

/** Centered crest: a big moon logo with a gold rim, number above, name + LIVE beneath, all centered on the stage. */
@Composable
private fun Crest(channel: Channel?, name: String?, number: Int, resolution: String?, timeshift: Boolean, logo: androidx.compose.ui.unit.Dp = 84.dp) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Box(Modifier.size(logo + 26.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Brush.radialGradient(listOf(AL.Amber.copy(alpha = 0.35f), Color.Transparent))))
            AlLogo(channel?.name ?: name.orEmpty(), channel?.logoUrl, logo)
            if (number > 0) Text("$number", color = AL.Bg, fontSize = 13.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.TopEnd).clip(AL.Pill).background(AL.Amber).padding(horizontal = 8.dp, vertical = 2.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(channel?.name ?: name.orEmpty(), color = AL.Text, fontSize = 24.sp, fontWeight = FontWeight.Light, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, maxLines = 1)
            if (channel?.isFavorite == true) Text("♥", color = AL.Amber, fontSize = 15.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AlBadge(if (timeshift) tr("PAUSED LIVE", "مباشر مؤجل") else "● LIVE", AL.Live, filled = !timeshift)
            (resolution ?: channel?.qualityBadge())?.let { AlBadge(it, AL.Text) }
            if (channel?.catchUpSupported == true) AlBadge(tr("CATCH-UP", "أرشيف"), AL.Blue)
        }
    }
}

/** Centered now line: title, a rose-to-gold arc progress with times at both ends, "next" whispered beneath. */
@Composable
private fun CenterNow(now: Program?, next: Program?, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (now != null) {
            Text(now.title, color = AL.Text, fontSize = 19.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(alClock(now.startTime), color = AL.Sub, fontSize = 12.sp)
                AlProgress(now.progressAt(), Modifier.weight(1f), 5.dp)
                Text(alClock(now.endTime), color = AL.Sub, fontSize = 12.sp)
            }
        } else Text(tr("No programme information", "لا توجد معلومات عن البرنامج"), color = AL.Faint, fontSize = 14.sp)
        next?.let { Text("☾ " + tr("Later", "لاحقاً") + "  ${alClock(it.startTime)}  ${it.title}", color = AL.Faint, fontSize = 13.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, maxLines = 1, overflow = TextOverflow.Ellipsis) }
    }
}

/** Live: a lounge "stage". Clock + REC float as a small pill at top-center; the crest, now line and a
 *  centered arc of moon controls rise from the bottom-center over an aurora floor glow. */
@Composable
private fun AlLive(p: PlayerOverlayParams) {
    LaunchedEffect(Unit) { runCatching { p.playButtonFocusRequester.requestFocus() } }
    val rec = p.currentRecordingStatus == RecordingStatus.RECORDING
    val acts = buildList {
        add(AlAct(if (p.isPlaying) "❚❚" else "▶", if (p.isPlaying) tr("Pause", "إيقاف") else tr("Play", "تشغيل"), true) { p.onUserInteraction(); p.onTogglePlayPause() })
        add(AlAct("☰", tr("Channels", "القنوات")) { p.onOpenLiveChannels() })
        add(AlAct(if (p.currentChannel?.isFavorite == true) "♥" else "♡", tr("Favorites", "المفضلة"), p.currentChannel?.isFavorite == true) { p.onToggleLiveFavorite() })
        add(AlAct("♪", tr("Audio", "الصوت") + " ${p.audioTrackCount}") { p.onOpenAudioTracks() })
        add(AlAct("▭", p.aspectRatioLabel) { p.onToggleAspectRatio() })
        add(AlAct("HD", p.resolutionBadgeLabel ?: tr("Quality", "الجودة")) { p.onOpenVideoTracks() })
        add(AlAct("CC", tr("Subs", "الترجمة") + " ${p.subtitleTrackCount}") { p.onOpenSubtitleTracks() })
        add(AlAct("▦", tr("Guide", "الدليل")) { p.onOpenLiveGuide() })
        add(AlAct("⏪", "-10s") { p.onUserInteraction(); p.onSeekBackward() })
        add(AlAct("⏩", "+10s") { p.onUserInteraction(); p.onSeekForward() })
        add(AlAct("⏮", tr("Restart", "من البداية")) { p.onRestartProgram() })
        add(AlAct("⟲", tr("Catch-up", "الأرشيف")) { p.onOpenArchive() })
        if (p.timeshiftUiState.canSeekToLive) add(AlAct("⇥", tr("Go live", "مباشر")) { p.onSeekToLiveEdge() })
        add(AlAct(if (p.isMuted) "🔇" else "🔊", tr("Mute", "كتم"), p.isMuted) { p.onToggleMute() })
        add(AlAct(if (rec) "■" else "●", if (rec) tr("Stop rec", "إيقاف التسجيل") else tr("Record", "تسجيل"), rec) { if (rec) p.onStopRecording() else p.onStartRecording() })
        add(AlAct("◷", tr("Schedule", "جدولة")) { p.onScheduleRecording() })
        add(AlAct("◷", tr("Daily", "يومي")) { p.onScheduleDailyRecording() })
        add(AlAct("◷", tr("Weekly", "أسبوعي")) { p.onScheduleWeeklyRecording() })
        add(AlAct("»", "${p.playbackSpeed}×") { p.onOpenPlaybackSpeed() })
        if (p.audioVideoSyncEnabled) add(AlAct("⇄", tr("A/V sync", "مزامنة")) { p.onOpenAudioVideoSync() })
        add(AlAct("◫", tr("Multiview", "تقسيم")) { p.onOpenSplitScreen() })
        add(AlAct("⏲", if (p.sleepTimerUiState.stopTimerActive) alDuration(p.sleepTimerUiState.stopRemainingMs) else tr("Sleep", "مؤقت"), p.sleepTimerUiState.stopTimerActive) { p.onOpenStopPlaybackTimer() })
        add(AlAct("☾", if (p.sleepTimerUiState.idleTimerActive) alDuration(p.sleepTimerUiState.idleRemainingMs) else tr("Standby", "استعداد"), p.sleepTimerUiState.idleTimerActive) { p.onOpenIdleStandbyTimer() })
        add(AlAct("⧉", "PiP") { p.onEnterPictureInPicture() })
        add(AlAct("⎚", if (p.isCastConnected) tr("Stop cast", "إيقاف البث") else tr("Cast", "بث"), p.isCastConnected) { if (p.isCastConnected) p.onStopCasting() else p.onCast() })
        add(AlAct("✕", tr("Close", "إغلاق")) { p.onClose() })
    }
    Box(p.modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(AL.Bg.copy(alpha = 0.95f), AL.Bg.copy(alpha = 0.6f), Color.Transparent), center = androidx.compose.ui.geometry.Offset(960f, 1400f), radius = 1100f)))
        Row(
            Modifier.align(Alignment.TopCenter).padding(top = 24.dp).clip(AL.Pill).background(AL.Raised.copy(alpha = 0.85f)).padding(horizontal = 22.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("☾", color = AL.Amber, fontSize = 16.sp)
            Text(alClock(System.currentTimeMillis()), color = AL.Text, fontSize = 20.sp, fontWeight = FontWeight.Light)
            if (rec) AlBadge("● REC", AL.Live, filled = true)
        }
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 64.dp, vertical = 26.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Crest(p.currentChannel, p.currentChannelName, p.displayChannelNumber, p.resolutionBadgeLabel, p.timeshiftUiState.enabledForSession && p.timeshiftUiState.bufferedBehindLiveMs > 0)
            CenterNow(p.currentProgram, p.nextProgram, Modifier.fillMaxWidth(0.55f))
            LazyRow(Modifier.focusRequester(p.quickActionsFocusRequester), horizontalArrangement = Arrangement.spacedBy(4.dp), contentPadding = PaddingValues(horizontal = 12.dp)) {
                items(acts.size) { i -> AlCtl(acts[i], if (i == 0) Modifier.focusRequester(p.playButtonFocusRequester) else Modifier, size = 48.dp) }
            }
        }
    }
}

/** VOD: a floating lounge "menu card" at the bottom: serif title + episode line on top, three moon transport
 *  buttons centered, the arc scrubber, then the pill actions. The options sheet slides in from the START side. */
@Composable
private fun AlVod(p: PlayerOverlayParams) {
    var sheet by remember { mutableStateOf(false) }
    val sheetFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { p.playButtonFocusRequester.requestFocus() } }
    LaunchedEffect(sheet) { if (sheet) runCatching { sheetFocus.requestFocus() } }
    fun seekBy(d: Long) { p.onUserInteraction(); p.onSeekToPosition((p.currentPosition + d).coerceIn(0L, (p.duration - 1_000L).coerceAtLeast(0L))) }
    Box(p.modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, AL.Bg.copy(alpha = 0.7f))))) {
        Row(Modifier.align(Alignment.TopEnd).padding(28.dp).clip(AL.Pill).background(AL.Raised.copy(alpha = 0.85f)).padding(horizontal = 18.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            if (p.currentRecordingStatus == RecordingStatus.RECORDING) AlBadge("● REC", AL.Live, filled = true)
            if (p.playbackSpeed != 1f) AlBadge("${p.playbackSpeed}×", AL.Amber)
            Text(alClock(System.currentTimeMillis()), color = AL.Text, fontSize = 18.sp, fontWeight = FontWeight.Light)
        }
        Column(
            Modifier.align(Alignment.BottomCenter).padding(start = if (sheet) 420.dp else 56.dp, end = 56.dp, bottom = 28.dp).fillMaxWidth().clip(AL.R)
                .background(Brush.verticalGradient(listOf(AL.Raised.copy(alpha = 0.92f), AL.Bg.copy(alpha = 0.96f)))).padding(horizontal = 28.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                AlRound("←", { p.onNavigateBack() }, size = 40.dp)
                Column(Modifier.weight(1f)) {
                    if (p.isCatchUpPlayback) Text(tr("From the archive", "من الأرشيف"), color = AL.Amber, fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    Text(p.displayTitle, color = AL.Text, fontSize = 26.sp, fontWeight = FontWeight.Light, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    p.episodeLine?.let { Text(it, color = AL.Sub, fontSize = 14.sp, maxLines = 1) }
                }
                AlCtl(AlAct("↺", "-10s") { seekBy(-STEP) }, size = 52.dp)
                AlCtl(AlAct(if (p.isPlaying) "❚❚" else "▶", if (p.isPlaying) tr("Pause", "إيقاف") else tr("Play", "تشغيل"), true) { p.onUserInteraction(); p.onTogglePlayPause() }, Modifier.focusRequester(p.playButtonFocusRequester), size = 72.dp)
                AlCtl(AlAct("↻", "+10s") { seekBy(STEP) }, size = 52.dp)
            }
            Scrubber(p, ::seekBy)
            LazyRow(Modifier.focusRequester(p.quickActionsFocusRequester), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item { AlButton(tr("Audio", "الصوت") + " · ${p.audioTrackCount}", p.onOpenAudioTracks, icon = "♪") }
                item { AlButton(tr("Subtitles", "الترجمة") + " · ${p.subtitleTrackCount}", p.onOpenSubtitleTracks, icon = "CC") }
                item { AlButton(p.resolutionBadgeLabel ?: tr("Quality", "الجودة"), p.onOpenVideoTracks, icon = "HD") }
                if (p.showEpisodesAction) item { AlButton(tr("Episodes", "الحلقات"), p.onOpenEpisodes, icon = "≣") }
                item { AlButton(tr("Start over", "من البداية"), { p.onSeekToPosition(0L) }, icon = "⏮") }
                item { AlButton(tr("More", "المزيد"), { sheet = !sheet }, primary = sheet, icon = "⚙") }
                item { AlButton(tr("Close", "إغلاق"), p.onClose, icon = "✕") }
            }
        }
        if (sheet) SideSheet(p, sheetFocus) { sheet = false }
    }
}

@Composable
private fun Scrubber(p: PlayerOverlayParams, seekBy: (Long) -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val pos = if (p.seekPreview.visible) p.seekPreview.positionMs else p.currentPosition
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(alDuration(pos), color = if (p.seekPreview.visible) AL.Amber else AL.Text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        AlCard(
            onClick = { p.onUserInteraction(); p.onTogglePlayPause() }, shape = AL.Pill, container = Color.Transparent, focusedContainer = AL.Amber.copy(alpha = 0.12f), zoom = 1f,
            modifier = Modifier.weight(1f).onFocusChanged { focused = it.isFocused; p.onSetScrubbingMode(it.isFocused) }.onPreviewKeyEvent { e ->
                val n = e.nativeKeyEvent
                if (n.action != android.view.KeyEvent.ACTION_DOWN) return@onPreviewKeyEvent false
                when (n.keyCode) {
                    android.view.KeyEvent.KEYCODE_DPAD_LEFT -> { seekBy(-STEP); true }
                    android.view.KeyEvent.KEYCODE_DPAD_RIGHT -> { seekBy(STEP); true }
                    else -> false
                }
            }
        ) {
            Box(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp)) {
                AlProgress(if (p.duration > 0) (pos.toFloat() / p.duration).coerceIn(0f, 1f) else 0f, Modifier.fillMaxWidth(), if (focused) 8.dp else 4.dp)
            }
        }
        Text("-" + alDuration((p.duration - pos).coerceAtLeast(0)), color = AL.Sub, fontSize = 14.sp)
    }
}

/** START-side lounge sheet: tall rounded drawer, each option a soft row with a gold value. */
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
        add(Triple(tr("Sleep timer", "مؤقت النوم"), if (p.sleepTimerUiState.stopTimerActive) alDuration(p.sleepTimerUiState.stopRemainingMs) else tr("Off", "معطل"), p.onOpenStopPlaybackTimer))
        add(Triple(tr("Standby timer", "مؤقت الاستعداد"), if (p.sleepTimerUiState.idleTimerActive) alDuration(p.sleepTimerUiState.idleRemainingMs) else tr("Off", "معطل"), p.onOpenIdleStandbyTimer))
        add(Triple(tr("Multiview", "تقسيم الشاشة"), "", p.onOpenSplitScreen))
        add(Triple(tr("Picture in picture", "صورة داخل صورة"), "", p.onEnterPictureInPicture))
        add(Triple(tr("Cast", "البث"), if (p.isCastConnected) tr("Connected", "متصل") else "", if (p.isCastConnected) p.onStopCasting else p.onCast))
    }
    Column(
        Modifier.align(Alignment.CenterStart).padding(24.dp).fillMaxHeight().width(370.dp).clip(AL.R)
            .background(Brush.verticalGradient(listOf(AL.Raised.copy(alpha = 0.98f), AL.Bg.copy(alpha = 0.98f)))).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(tr("Lounge settings", "إعدادات الجلسة"), color = AL.Text, fontSize = 21.sp, fontWeight = FontWeight.Light, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, modifier = Modifier.weight(1f))
            AlRound("✕", onDismiss, size = 36.dp)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(rows.size) { i ->
                val (label, value, action) = rows[i]
                AlCard(onClick = action, shape = AL.Pill, container = Color.Transparent, focusedContainer = AL.Card, zoom = 1.02f, modifier = Modifier.fillMaxWidth().then(if (i == 0) Modifier.focusRequester(focus) else Modifier)) {
                    Row(Modifier.padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(label, color = AL.Text, fontSize = 15.sp, modifier = Modifier.weight(1f), maxLines = 1)
                        Text(value, color = AL.Amber, fontSize = 14.sp, maxLines = 1)
                    }
                }
            }
        }
    }
}

/** Channel list: END-side lounge drawer. The focused channel's crest + now/next card sits at the TOP of the drawer,
 *  channels below as soft pills with moon logos; the current one glows gold. Count in the header, no recents. */
@Composable
internal fun AuroraLoungeLiveChannelList(p: LiveChannelListParams) {
    val idx = remember(p.channels, p.currentChannelId) { p.channels.indexOfFirst { it.id == p.currentChannelId }.coerceAtLeast(0) }
    val state = rememberLazyListState(idx)
    var focused by remember(p.currentChannelId) { mutableStateOf(p.channels.getOrNull(idx)) }
    LaunchedEffect(Unit) { runCatching { p.focusRequester.requestFocus() } }
    Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.Transparent, AL.Bg.copy(alpha = 0.55f), AL.Bg.copy(alpha = 0.94f))))) {
        Column(
            Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(540.dp).padding(top = 24.dp, bottom = 24.dp, end = 32.dp).clip(AL.R)
                .background(Brush.verticalGradient(listOf(AL.Raised.copy(alpha = 0.95f), AL.Bg.copy(alpha = 0.97f)))).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("☾", color = AL.Amber, fontSize = 20.sp)
                Column(Modifier.weight(1f)) {
                    Text(p.lastVisitedCategoryName ?: tr("All channels", "كل القنوات"), color = AL.Text, fontSize = 22.sp, fontWeight = FontWeight.Light, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, maxLines = 1)
                    Text("${p.channels.size} " + tr("channels", "قناة"), color = AL.Amber, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                AlRound("✕", p.onDismiss, size = 36.dp)
            }
            focused?.let { c ->
                Row(Modifier.fillMaxWidth().clip(AL.RSmall).background(AL.Card.copy(alpha = 0.8f)).padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    AlLogo(c.name, c.logoUrl, 52.dp)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(c.name, color = AL.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        NowNext(c.currentProgram, c.nextProgram)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AlTab(tr("Categories", "الفئات"), false, p.onOpenCategories)
                if (p.lastVisitedCategoryName != null) AlTab(tr("Last group", "آخر مجموعة"), false, p.onOpenLastGroup)
                AlTab(tr("Guide", "الدليل"), false, p.onOpenGuide)
            }
            LazyColumn(state = state, verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f), contentPadding = PaddingValues(4.dp)) {
                items(p.channels, key = { it.id }) { c ->
                    val cur = c.id == p.currentChannelId
                    AlCard(
                        onClick = { p.onInteracted(); p.onSelectChannel(c.id) }, onLongClick = { p.onChannelLongPress(c) }, shape = AL.Pill, zoom = 1.03f,
                        container = if (cur) AL.Amber.copy(alpha = 0.18f) else Color.Transparent, focusedContainer = AL.Card,
                        modifier = Modifier.fillMaxWidth().then(if (cur) Modifier.focusRequester(p.focusRequester) else Modifier).onFocusChanged { if (it.isFocused) { focused = c; p.onInteracted() } }
                    ) {
                        Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AlLogo(c.name, c.logoUrl, 38.dp)
                            Text("${p.numberOf(c)}", color = if (cur) AL.Amber else AL.Faint, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(34.dp))
                            Column(Modifier.weight(1f)) {
                                Text(if (c.id == p.movingChannelId) "⇅  ${c.name}" else c.name, color = AL.Text, fontSize = 15.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                c.currentProgram?.let { Text(it.title, color = AL.Faint, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                            }
                            if (cur) AlBadge(tr("Now", "الآن"), AL.Amber, filled = true)
                            c.qualityBadge()?.let { AlBadge(it, AL.Text) }
                            if (c.catchUpSupported) AlBadge("⟲", AL.Blue)
                            if (c.isFavorite) Text("♥", color = AL.Rose, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

/** Zap banner: a wide floating lounge pill at the bottom with the channel moon breaking through its top edge at the
 *  START, number/name/LIVE and the now arc in the middle, "later" + clock at the END, moon actions in a row beneath. */
@Composable
internal fun AuroraLoungeLiveChannelInfo(p: LiveChannelInfoParams) {
    LaunchedEffect(Unit) { runCatching { p.focusRequester.requestFocus() } }
    val rec = p.currentRecordingStatus == RecordingStatus.RECORDING
    val acts = buildList {
        add(AlAct("☰", tr("Channels", "القنوات"), true) { p.onInteracted(); p.onOpenChannelList() })
        add(AlAct(if (p.channel?.isFavorite == true) "♥" else "♡", tr("Favorites", "المفضلة"), p.channel?.isFavorite == true) { p.onInteracted(); p.onToggleFavorite() })
        add(AlAct("♪", tr("Audio", "الصوت")) { p.onInteracted(); p.onOpenAudioTracks() })
        add(AlAct("▭", tr("Aspect", "العرض") + " " + p.aspectRatioLabel) { p.onInteracted(); p.onToggleAspectRatio() })
        add(AlAct("⚙", tr("Settings", "الإعدادات")) { p.onInteracted(); p.onOpenSettings() })
        add(AlAct("HD", p.resolutionLabel ?: tr("Quality", "الجودة")) { p.onInteracted(); p.onOpenVideoTracks() })
        add(AlAct("CC", tr("Subs", "الترجمة")) { p.onInteracted(); p.onOpenSubtitleTracks() })
        add(AlAct("▦", tr("Guide", "الدليل")) { p.onInteracted(); p.onOpenFullEpg() })
        add(AlAct(if (p.isPlaying) "❚❚" else "▶", if (p.isPlaying) tr("Pause", "إيقاف") else tr("Play", "تشغيل")) { p.onInteracted(); p.onTogglePlayPause() })
        add(AlAct(if (p.isMuted) "🔇" else "🔊", tr("Mute", "كتم"), p.isMuted) { p.onInteracted(); p.onToggleMute() })
        if (p.variantCount > 1) add(AlAct("⇋", tr("Source", "المصدر")) { p.onInteracted(); p.onOpenVariants() })
        add(AlAct("⏮", tr("Restart", "من البداية")) { p.onInteracted(); p.onRestartProgram() })
        add(AlAct("⟲", tr("Catch-up", "الأرشيف")) { p.onInteracted(); p.onOpenArchive() })
        if (p.canSeekToLive) add(AlAct("⇥", tr("Go live", "مباشر")) { p.onInteracted(); p.onSeekToLiveEdge() })
        add(AlAct(if (rec) "■" else "●", if (rec) tr("Stop rec", "إيقاف") else tr("Record", "تسجيل"), rec) { p.onInteracted(); if (rec) p.onStopRecording() else p.onStartRecording() })
        add(AlAct("◷", tr("Schedule", "جدولة")) { p.onInteracted(); p.onScheduleRecording() })
        add(AlAct("⇄", "A/V") { p.onInteracted(); p.onOpenAudioVideoSync() })
        add(AlAct("◫", tr("Multiview", "تقسيم")) { p.onInteracted(); p.onOpenSplitScreen() })
        add(AlAct("∿", tr("Stats", "إحصاءات"), p.isDiagnosticsEnabled) { p.onInteracted(); p.onToggleDiagnostics() })
        add(AlAct("⧉", "PiP") { p.onEnterPictureInPicture() })
        add(AlAct("⎚", if (p.isCastConnected) tr("Stop cast", "إيقاف البث") else tr("Cast", "بث"), p.isCastConnected) { if (p.isCastConnected) p.onStopCasting() else p.onCast() })
    }
    val ch = p.channel
    Box(Modifier.fillMaxSize()) {
        Box(Modifier.align(Alignment.BottomCenter).padding(horizontal = 40.dp, vertical = 24.dp).fillMaxWidth()) {
            Column(
                Modifier.padding(top = 40.dp).fillMaxWidth().clip(AL.R).background(Brush.horizontalGradient(listOf(AL.Raised.copy(alpha = 0.96f), AL.Bg.copy(alpha = 0.94f), AL.Raised.copy(alpha = 0.96f))))
                    .padding(start = 150.dp, end = 24.dp, top = 16.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (p.displayChannelNumber > 0) Text("${p.displayChannelNumber}", color = AL.Amber, fontSize = 20.sp, fontWeight = FontWeight.Black)
                            Text(ch?.name.orEmpty(), color = AL.Text, fontSize = 22.sp, fontWeight = FontWeight.Light, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            AlBadge("● LIVE", AL.Live, filled = true)
                            (p.resolutionLabel ?: ch?.qualityBadge())?.let { AlBadge(it, AL.Text) }
                            if (ch?.isFavorite == true) Text("♥", color = AL.Rose, fontSize = 14.sp)
                        }
                        p.currentProgram?.let { now ->
                            Text(now.title, color = AL.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(alClock(now.startTime), color = AL.Sub, fontSize = 12.sp)
                                AlProgress(now.progressAt(), Modifier.weight(1f))
                                Text(alClock(now.endTime), color = AL.Sub, fontSize = 12.sp)
                            }
                        } ?: Text(tr("No programme information", "لا توجد معلومات عن البرنامج"), color = AL.Faint, fontSize = 14.sp)
                    }
                    Column(Modifier.width(260.dp), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(alClock(System.currentTimeMillis()), color = AL.Text, fontSize = 28.sp, fontWeight = FontWeight.Light)
                        p.nextProgram?.let { Text("☾ ${alClock(it.startTime)}  ${it.title}", color = AL.Faint, fontSize = 13.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    items(acts.size) { i -> AlCtl(acts[i], if (i == 0) Modifier.focusRequester(p.focusRequester) else Modifier, size = 42.dp) }
                }
            }
            Box(Modifier.align(Alignment.TopStart).padding(start = 24.dp)) {
                Box(Modifier.size(116.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Brush.radialGradient(listOf(AL.Amber.copy(alpha = 0.4f), Color.Transparent))))
                AlLogo(ch?.name.orEmpty(), ch?.logoUrl, 96.dp, Modifier.align(Alignment.Center))
            }
        }
    }
}
