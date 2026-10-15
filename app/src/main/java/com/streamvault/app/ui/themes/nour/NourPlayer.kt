package com.streamvault.app.ui.themes.nour

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

private class NourAct(val glyph: String, val label: String, val active: Boolean = false, val onClick: () -> Unit)

@Composable
private fun NourCtl(a: NourAct, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 52.dp) {
    Column(modifier.width(size + 22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        NourRound(a.glyph, a.onClick, size = size, active = a.active)
        Text(a.label, color = NR.Sub, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun NowNext(now: Program?, next: Program?, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (now != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NourBadge(tr("NOW", "الآن"), NR.Amber, filled = true)
                Text(now.title, color = NR.Text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, false))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(nourClock(now.startTime), color = NR.Sub, fontSize = 12.sp)
                NourProgress(now.progressAt(), Modifier.weight(1f))
                Text(nourClock(now.endTime), color = NR.Sub, fontSize = 12.sp)
            }
        } else Text(tr("No programme information", "لا توجد معلومات عن البرنامج"), color = NR.Faint, fontSize = 14.sp)
        next?.let { Text(tr("Next", "التالي") + "  ${nourClock(it.startTime)}  ·  ${it.title}", color = NR.Faint, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
    }
}

@Composable
private fun ChannelHead(channel: Channel?, name: String?, number: Int, resolution: String?, timeshift: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        NourLogo(channel?.name ?: name.orEmpty(), channel?.logoUrl, 64.dp)
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (number > 0) Text("$number", color = NR.Amber, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text(channel?.name ?: name.orEmpty(), color = NR.Text, fontSize = 24.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                if (channel?.isFavorite == true) Text("♥", color = NR.Amber, fontSize = 16.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                NourBadge(if (timeshift) tr("PAUSED LIVE", "مباشر مؤجل") else "● LIVE", NR.Live, filled = !timeshift)
                (resolution ?: channel?.qualityBadge())?.let { NourBadge(it, NR.Text) }
                if (channel?.catchUpSupported == true) NourBadge(tr("CATCH-UP", "أرشيف"), NR.Blue)
            }
        }
    }
}



@Composable
internal fun NourPlayerOverlay(p: PlayerOverlayParams) {
    if (!p.visible) return
    if (p.isLive && !p.isCatchUpPlayback) NourLive(p) else NourVod(p)
}

@Composable
private fun Crest(channel: Channel?, name: String?, number: Int, resolution: String?, timeshift: Boolean, logo: androidx.compose.ui.unit.Dp = 84.dp) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Box(Modifier.size(logo + 26.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Brush.radialGradient(listOf(NR.Amber.copy(alpha = 0.35f), Color.Transparent))))
            NourLogo(channel?.name ?: name.orEmpty(), channel?.logoUrl, logo)
            if (number > 0) Text("$number", color = NR.Bg, fontSize = 13.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.TopEnd).clip(NR.Pill).background(NR.Amber).padding(horizontal = 8.dp, vertical = 2.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(channel?.name ?: name.orEmpty(), color = NR.Text, fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif, maxLines = 1)
            if (channel?.isFavorite == true) Text("♥", color = NR.Amber, fontSize = 15.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            NourBadge(if (timeshift) tr("PAUSED LIVE", "مباشر مؤجل") else "● LIVE", NR.Live, filled = !timeshift)
            (resolution ?: channel?.qualityBadge())?.let { NourBadge(it, NR.Text) }
            if (channel?.catchUpSupported == true) NourBadge(tr("CATCH-UP", "أرشيف"), NR.Blue)
        }
    }
}

@Composable
private fun CenterNow(now: Program?, next: Program?, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (now != null) {
            Text(now.title, color = NR.Text, fontSize = 19.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(nourClock(now.startTime), color = NR.Sub, fontSize = 12.sp)
                NourProgress(now.progressAt(), Modifier.weight(1f), 5.dp)
                Text(nourClock(now.endTime), color = NR.Sub, fontSize = 12.sp)
            }
        } else Text(tr("No programme information", "لا توجد معلومات عن البرنامج"), color = NR.Faint, fontSize = 14.sp)
        next?.let { Text("☾ " + tr("Later", "لاحقاً") + "  ${nourClock(it.startTime)}  ${it.title}", color = NR.Faint, fontSize = 13.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis) }
    }
}

@Composable
private fun NourLive(p: PlayerOverlayParams) {
    LaunchedEffect(Unit) { runCatching { p.playButtonFocusRequester.requestFocus() } }
    val rec = p.currentRecordingStatus == RecordingStatus.RECORDING
    val acts = buildList {
        add(NourAct(if (p.isPlaying) "❚❚" else "▶", if (p.isPlaying) tr("Pause", "إيقاف") else tr("Play", "تشغيل"), true) { p.onUserInteraction(); p.onTogglePlayPause() })
        add(NourAct("☰", tr("Channels", "القنوات")) { p.onOpenLiveChannels() })
        add(NourAct(if (p.currentChannel?.isFavorite == true) "♥" else "♡", tr("Favorites", "المفضلة"), p.currentChannel?.isFavorite == true) { p.onToggleLiveFavorite() })
        add(NourAct("♪", tr("Audio", "الصوت") + " ${p.audioTrackCount}") { p.onOpenAudioTracks() })
        add(NourAct("▭", p.aspectRatioLabel) { p.onToggleAspectRatio() })
        add(NourAct("HD", p.resolutionBadgeLabel ?: tr("Quality", "الجودة")) { p.onOpenVideoTracks() })
        add(NourAct("CC", tr("Subs", "الترجمة") + " ${p.subtitleTrackCount}") { p.onOpenSubtitleTracks() })
        add(NourAct("▦", tr("Guide", "الدليل")) { p.onOpenLiveGuide() })
        add(NourAct("⏪", "-10s") { p.onUserInteraction(); p.onSeekBackward() })
        add(NourAct("⏩", "+10s") { p.onUserInteraction(); p.onSeekForward() })
        add(NourAct("⏮", tr("Restart", "من البداية")) { p.onRestartProgram() })
        add(NourAct("⟲", tr("Catch-up", "الأرشيف")) { p.onOpenArchive() })
        if (p.timeshiftUiState.canSeekToLive) add(NourAct("⇥", tr("Go live", "مباشر")) { p.onSeekToLiveEdge() })
        add(NourAct(if (p.isMuted) "🔇" else "🔊", tr("Mute", "كتم"), p.isMuted) { p.onToggleMute() })
        add(NourAct(if (rec) "■" else "●", if (rec) tr("Stop rec", "إيقاف التسجيل") else tr("Record", "تسجيل"), rec) { if (rec) p.onStopRecording() else p.onStartRecording() })
        add(NourAct("◷", tr("Schedule", "جدولة")) { p.onScheduleRecording() })
        add(NourAct("◷", tr("Daily", "يومي")) { p.onScheduleDailyRecording() })
        add(NourAct("◷", tr("Weekly", "أسبوعي")) { p.onScheduleWeeklyRecording() })
        add(NourAct("»", "${p.playbackSpeed}×") { p.onOpenPlaybackSpeed() })
        if (p.audioVideoSyncEnabled) add(NourAct("⇄", tr("A/V sync", "مزامنة")) { p.onOpenAudioVideoSync() })
        add(NourAct("◫", tr("Multiview", "تقسيم")) { p.onOpenSplitScreen() })
        add(NourAct("⏲", if (p.sleepTimerUiState.stopTimerActive) nourDuration(p.sleepTimerUiState.stopRemainingMs) else tr("Sleep", "مؤقت"), p.sleepTimerUiState.stopTimerActive) { p.onOpenStopPlaybackTimer() })
        add(NourAct("☾", if (p.sleepTimerUiState.idleTimerActive) nourDuration(p.sleepTimerUiState.idleRemainingMs) else tr("Standby", "استعداد"), p.sleepTimerUiState.idleTimerActive) { p.onOpenIdleStandbyTimer() })
        add(NourAct("⧉", "PiP") { p.onEnterPictureInPicture() })
        add(NourAct("⎚", if (p.isCastConnected) tr("Stop cast", "إيقاف البث") else tr("Cast", "بث"), p.isCastConnected) { if (p.isCastConnected) p.onStopCasting() else p.onCast() })
        add(NourAct("✕", tr("Close", "إغلاق")) { p.onClose() })
    }
    Box(p.modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(NR.Bg.copy(alpha = 0.75f), NR.Bg.copy(alpha = 0.2f), Color.Transparent), center = androidx.compose.ui.geometry.Offset(960f, 1400f), radius = 1100f)))
        Row(
            Modifier.align(Alignment.TopCenter).padding(top = 24.dp).clip(NR.Pill).background(NR.Raised.copy(alpha = 0.85f)).padding(horizontal = 22.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("☾", color = NR.Amber, fontSize = 16.sp)
            Text(nourClock(System.currentTimeMillis()), color = NR.Text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            if (rec) NourBadge("● REC", NR.Live, filled = true)
        }
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 42.dp, vertical = 22.dp).clip(NR.R).background(NR.Raised.copy(alpha = 0.88f)).padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Crest(p.currentChannel, p.currentChannelName, p.displayChannelNumber, p.resolutionBadgeLabel, p.timeshiftUiState.enabledForSession && p.timeshiftUiState.bufferedBehindLiveMs > 0)
            CenterNow(p.currentProgram, p.nextProgram, Modifier.fillMaxWidth(0.55f))
            LazyRow(Modifier.focusRequester(p.quickActionsFocusRequester), horizontalArrangement = Arrangement.spacedBy(4.dp), contentPadding = PaddingValues(horizontal = 12.dp)) {
                items(acts.size) { i -> NourCtl(acts[i], if (i == 0) Modifier.focusRequester(p.playButtonFocusRequester) else Modifier, size = 48.dp) }
            }
        }
    }
}

@Composable
private fun NourVod(p: PlayerOverlayParams) {
    var sheet by remember { mutableStateOf(false) }
    val sheetFocus = remember { FocusRequester() }
    val moreFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { p.playButtonFocusRequester.requestFocus() } }
    LaunchedEffect(sheet) { if (sheet) runCatching { sheetFocus.requestFocus() } }
    fun seekBy(d: Long) { p.onUserInteraction(); p.onSeekToPosition((p.currentPosition + d).coerceIn(0L, (p.duration - 1_000L).coerceAtLeast(0L))) }
    Box(p.modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, NR.Bg.copy(alpha = 0.7f))))) {
        Row(Modifier.align(Alignment.TopEnd).padding(28.dp).clip(NR.Pill).background(NR.Raised.copy(alpha = 0.85f)).padding(horizontal = 18.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            if (p.currentRecordingStatus == RecordingStatus.RECORDING) NourBadge("● REC", NR.Live, filled = true)
            if (p.playbackSpeed != 1f) NourBadge("${p.playbackSpeed}×", NR.Amber)
            Text(nourClock(System.currentTimeMillis()), color = NR.Text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Column(
            Modifier.align(Alignment.BottomCenter).padding(start = if (sheet) 420.dp else 56.dp, end = 56.dp, bottom = 28.dp).fillMaxWidth().clip(NR.R)
                .background(Brush.verticalGradient(listOf(NR.Raised.copy(alpha = 0.92f), NR.Bg.copy(alpha = 0.96f)))).padding(horizontal = 28.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                NourRound("←", { p.onNavigateBack() }, size = 40.dp)
                Column(Modifier.weight(1f)) {
                    if (p.isCatchUpPlayback) Text(tr("From the archive", "من الأرشيف"), color = NR.Amber, fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Normal)
                    Text(p.displayTitle, color = NR.Text, fontSize = 26.sp, fontWeight = FontWeight.Bold, fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    p.episodeLine?.let { Text(it, color = NR.Sub, fontSize = 14.sp, maxLines = 1) }
                }
                NourCtl(NourAct("↺", "-10s") { seekBy(-STEP) }, size = 52.dp)
                NourCtl(NourAct(if (p.isPlaying) "❚❚" else "▶", if (p.isPlaying) tr("Pause", "إيقاف") else tr("Play", "تشغيل"), true) { p.onUserInteraction(); p.onTogglePlayPause() }, Modifier.focusRequester(p.playButtonFocusRequester), size = 72.dp)
                NourCtl(NourAct("↻", "+10s") { seekBy(STEP) }, size = 52.dp)
            }
            Scrubber(p, ::seekBy)
            LazyRow(Modifier.focusRequester(p.quickActionsFocusRequester), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item { NourButton(tr("Audio", "الصوت") + " · ${p.audioTrackCount}", p.onOpenAudioTracks, icon = "♪") }
                item { NourButton(tr("Subtitles", "الترجمة") + " · ${p.subtitleTrackCount}", p.onOpenSubtitleTracks, icon = "CC") }
                item { NourButton(p.resolutionBadgeLabel ?: tr("Quality", "الجودة"), p.onOpenVideoTracks, icon = "HD") }
                if (p.showEpisodesAction) item { NourButton(tr("Episodes", "الحلقات"), p.onOpenEpisodes, icon = "≣") }
                item { NourButton(tr("Start over", "من البداية"), { p.onSeekToPosition(0L) }, icon = "⏮") }
                item { androidx.compose.foundation.layout.Box(Modifier.focusRequester(moreFocus)) { NourButton(tr("More", "المزيد"), { sheet = !sheet }, primary = sheet, icon = "⚙") } }
                item { NourButton(tr("Close", "إغلاق"), p.onClose, icon = "✕") }
            }
        }
        if (sheet) InnerPanelBackScope(onClose = { sheet = false }, opener = moreFocus) { SideSheet(p, sheetFocus) { sheet = false; runCatching { moreFocus.requestFocus() } } }
    }
}

@Composable
private fun Scrubber(p: PlayerOverlayParams, seekBy: (Long) -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val pos = if (p.seekPreview.visible) p.seekPreview.positionMs else p.currentPosition
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(nourDuration(pos), color = if (p.seekPreview.visible) NR.Amber else NR.Text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        NourCard(
            onClick = { p.onUserInteraction(); p.onTogglePlayPause() }, shape = NR.Pill, container = Color.Transparent, focusedContainer = NR.Amber.copy(alpha = 0.12f), zoom = 1f,
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
                NourProgress(if (p.duration > 0) (pos.toFloat() / p.duration).coerceIn(0f, 1f) else 0f, Modifier.fillMaxWidth(), if (focused) 8.dp else 4.dp)
            }
        }
        Text("-" + nourDuration((p.duration - pos).coerceAtLeast(0)), color = NR.Sub, fontSize = 14.sp)
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
        add(Triple(tr("Sleep timer", "مؤقت النوم"), if (p.sleepTimerUiState.stopTimerActive) nourDuration(p.sleepTimerUiState.stopRemainingMs) else tr("Off", "معطل"), p.onOpenStopPlaybackTimer))
        add(Triple(tr("Standby timer", "مؤقت الاستعداد"), if (p.sleepTimerUiState.idleTimerActive) nourDuration(p.sleepTimerUiState.idleRemainingMs) else tr("Off", "معطل"), p.onOpenIdleStandbyTimer))
        add(Triple(tr("Multiview", "تقسيم الشاشة"), "", p.onOpenSplitScreen))
        add(Triple(tr("Picture in picture", "صورة داخل صورة"), "", p.onEnterPictureInPicture))
        add(Triple(tr("Cast", "البث"), if (p.isCastConnected) tr("Connected", "متصل") else "", if (p.isCastConnected) p.onStopCasting else p.onCast))
    }
    Column(
        Modifier.align(Alignment.CenterStart).padding(24.dp).fillMaxHeight().width(370.dp).clip(NR.R)
            .background(Brush.verticalGradient(listOf(NR.Raised.copy(alpha = 0.98f), NR.Bg.copy(alpha = 0.98f)))).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(tr("Playback settings", "إعدادات التشغيل"), color = NR.Text, fontSize = 21.sp, fontWeight = FontWeight.Bold, fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif, modifier = Modifier.weight(1f))
            NourRound("✕", onDismiss, size = 36.dp)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(rows.size) { i ->
                val (label, value, action) = rows[i]
                NourCard(onClick = action, shape = NR.Pill, container = Color.Transparent, focusedContainer = NR.Card, zoom = 1.02f, modifier = Modifier.fillMaxWidth().then(if (i == 0) Modifier.focusRequester(focus) else Modifier)) {
                    Row(Modifier.padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(label, color = NR.Text, fontSize = 15.sp, modifier = Modifier.weight(1f), maxLines = 1)
                        Text(value, color = NR.Amber, fontSize = 14.sp, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
internal fun NourLiveChannelList(p: LiveChannelListParams) {
    val idx = remember(p.channels, p.currentChannelId) { p.channels.indexOfFirst { it.id == p.currentChannelId }.coerceAtLeast(0) }
    val state = rememberLazyListState(idx)
    var focused by remember(p.currentChannelId) { mutableStateOf(p.channels.getOrNull(idx)) }
    LaunchedEffect(Unit) { runCatching { p.focusRequester.requestFocus() } }
    Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.Transparent, NR.Bg.copy(alpha = 0.55f), NR.Bg.copy(alpha = 0.94f))))) {
        Column(
            Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(540.dp).padding(top = 24.dp, bottom = 24.dp, end = 32.dp).clip(NR.R)
                .background(Brush.verticalGradient(listOf(NR.Raised.copy(alpha = 0.95f), NR.Bg.copy(alpha = 0.97f)))).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("☾", color = NR.Amber, fontSize = 20.sp)
                Column(Modifier.weight(1f)) {
                    Text(p.lastVisitedCategoryName ?: tr("All channels", "كل القنوات"), color = NR.Text, fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif, maxLines = 1)
                    Text("${p.channels.size} " + tr("channels", "قناة"), color = NR.Amber, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                NourRound("✕", p.onDismiss, size = 36.dp)
            }
            focused?.let { c ->
                Row(Modifier.fillMaxWidth().clip(NR.RSmall).background(NR.Card.copy(alpha = 0.8f)).padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    NourLogo(c.name, c.logoUrl, 52.dp)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(c.name, color = NR.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        NowNext(c.currentProgram, c.nextProgram)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                NourTab(tr("Categories", "الفئات"), false, p.onOpenCategories)
                if (p.lastVisitedCategoryName != null) NourTab(tr("Last group", "آخر مجموعة"), false, p.onOpenLastGroup)
                NourTab(tr("Guide", "الدليل"), false, p.onOpenGuide)
            }
            LazyColumn(state = state, verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f), contentPadding = PaddingValues(4.dp)) {
                items(p.channels, key = { it.id }) { c ->
                    val cur = c.id == p.currentChannelId
                    NourCard(
                        onClick = { p.onInteracted(); p.onSelectChannel(c.id) }, onLongClick = { p.onChannelLongPress(c) }, shape = NR.Pill, zoom = 1.03f,
                        container = if (cur) NR.Amber.copy(alpha = 0.18f) else Color.Transparent, focusedContainer = NR.Card,
                        modifier = Modifier.fillMaxWidth().then(if (cur) Modifier.focusRequester(p.focusRequester) else Modifier).onFocusChanged { if (it.isFocused) { focused = c; p.onInteracted() } }
                    ) {
                        Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            NourLogo(c.name, c.logoUrl, 38.dp)
                            Text("${p.numberOf(c)}", color = if (cur) NR.Amber else NR.Faint, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(34.dp))
                            Column(Modifier.weight(1f)) {
                                Text(if (c.id == p.movingChannelId) "⇅  ${c.name}" else c.name, color = NR.Text, fontSize = 15.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                c.currentProgram?.let { Text(it.title, color = NR.Faint, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                            }
                            if (cur) NourBadge(tr("Now", "الآن"), NR.Amber, filled = true)
                            c.qualityBadge()?.let { NourBadge(it, NR.Text) }
                            if (c.catchUpSupported) NourBadge("⟲", NR.Blue)
                            if (c.isFavorite) Text("♥", color = NR.Rose, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun NourLiveChannelInfo(p: LiveChannelInfoParams) {
    LaunchedEffect(Unit) { runCatching { p.focusRequester.requestFocus() } }
    val rec = p.currentRecordingStatus == RecordingStatus.RECORDING
    val acts = buildList {
        add(NourAct("☰", tr("Channels", "القنوات"), true) { p.onInteracted(); p.onOpenChannelList() })
        add(NourAct(if (p.channel?.isFavorite == true) "♥" else "♡", tr("Favorites", "المفضلة"), p.channel?.isFavorite == true) { p.onInteracted(); p.onToggleFavorite() })
        add(NourAct("♪", tr("Audio", "الصوت")) { p.onInteracted(); p.onOpenAudioTracks() })
        add(NourAct("▭", tr("Aspect", "العرض") + " " + p.aspectRatioLabel) { p.onInteracted(); p.onToggleAspectRatio() })
        add(NourAct("⚙", tr("Settings", "الإعدادات")) { p.onInteracted(); p.onOpenSettings() })
        add(NourAct("HD", p.resolutionLabel ?: tr("Quality", "الجودة")) { p.onInteracted(); p.onOpenVideoTracks() })
        add(NourAct("CC", tr("Subs", "الترجمة")) { p.onInteracted(); p.onOpenSubtitleTracks() })
        add(NourAct("▦", tr("Guide", "الدليل")) { p.onInteracted(); p.onOpenFullEpg() })
        add(NourAct(if (p.isPlaying) "❚❚" else "▶", if (p.isPlaying) tr("Pause", "إيقاف") else tr("Play", "تشغيل")) { p.onInteracted(); p.onTogglePlayPause() })
        add(NourAct(if (p.isMuted) "🔇" else "🔊", tr("Mute", "كتم"), p.isMuted) { p.onInteracted(); p.onToggleMute() })
        if (p.variantCount > 1) add(NourAct("⇋", tr("Source", "المصدر")) { p.onInteracted(); p.onOpenVariants() })
        add(NourAct("⏮", tr("Restart", "من البداية")) { p.onInteracted(); p.onRestartProgram() })
        add(NourAct("⟲", tr("Catch-up", "الأرشيف")) { p.onInteracted(); p.onOpenArchive() })
        if (p.canSeekToLive) add(NourAct("⇥", tr("Go live", "مباشر")) { p.onInteracted(); p.onSeekToLiveEdge() })
        add(NourAct(if (rec) "■" else "●", if (rec) tr("Stop rec", "إيقاف") else tr("Record", "تسجيل"), rec) { p.onInteracted(); if (rec) p.onStopRecording() else p.onStartRecording() })
        add(NourAct("◷", tr("Schedule", "جدولة")) { p.onInteracted(); p.onScheduleRecording() })
        add(NourAct("⇄", "A/V") { p.onInteracted(); p.onOpenAudioVideoSync() })
        add(NourAct("◫", tr("Multiview", "تقسيم")) { p.onInteracted(); p.onOpenSplitScreen() })
        add(NourAct("∿", tr("Stats", "إحصاءات"), p.isDiagnosticsEnabled) { p.onInteracted(); p.onToggleDiagnostics() })
        add(NourAct("⧉", "PiP") { p.onEnterPictureInPicture() })
        add(NourAct("⎚", if (p.isCastConnected) tr("Stop cast", "إيقاف البث") else tr("Cast", "بث"), p.isCastConnected) { if (p.isCastConnected) p.onStopCasting() else p.onCast() })
    }
    val ch = p.channel
    Box(Modifier.fillMaxSize()) {
        Box(Modifier.align(Alignment.BottomCenter).padding(horizontal = 40.dp, vertical = 24.dp).fillMaxWidth()) {
            Column(
                Modifier.padding(top = 40.dp).fillMaxWidth().clip(NR.R).background(Brush.horizontalGradient(listOf(NR.Raised.copy(alpha = 0.96f), NR.Bg.copy(alpha = 0.94f), NR.Raised.copy(alpha = 0.96f))))
                    .padding(start = 150.dp, end = 24.dp, top = 16.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (p.displayChannelNumber > 0) Text("${p.displayChannelNumber}", color = NR.Amber, fontSize = 20.sp, fontWeight = FontWeight.Black)
                            Text(ch?.name.orEmpty(), color = NR.Text, fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            NourBadge("● LIVE", NR.Live, filled = true)
                            (p.resolutionLabel ?: ch?.qualityBadge())?.let { NourBadge(it, NR.Text) }
                            if (ch?.isFavorite == true) Text("♥", color = NR.Rose, fontSize = 14.sp)
                        }
                        p.currentProgram?.let { now ->
                            Text(now.title, color = NR.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(nourClock(now.startTime), color = NR.Sub, fontSize = 12.sp)
                                NourProgress(now.progressAt(), Modifier.weight(1f))
                                Text(nourClock(now.endTime), color = NR.Sub, fontSize = 12.sp)
                            }
                        } ?: Text(tr("No programme information", "لا توجد معلومات عن البرنامج"), color = NR.Faint, fontSize = 14.sp)
                    }
                    Column(Modifier.width(260.dp), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(nourClock(System.currentTimeMillis()), color = NR.Text, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        p.nextProgram?.let { Text("☾ ${nourClock(it.startTime)}  ${it.title}", color = NR.Faint, fontSize = 13.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    items(acts.size) { i -> NourCtl(acts[i], if (i == 0) Modifier.focusRequester(p.focusRequester) else Modifier, size = 42.dp) }
                }
            }
            Box(Modifier.align(Alignment.TopStart).padding(start = 24.dp)) {
                Box(Modifier.size(116.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Brush.radialGradient(listOf(NR.Amber.copy(alpha = 0.4f), Color.Transparent))))
                NourLogo(ch?.name.orEmpty(), ch?.logoUrl, 96.dp, Modifier.align(Alignment.Center))
            }
        }
    }
}
