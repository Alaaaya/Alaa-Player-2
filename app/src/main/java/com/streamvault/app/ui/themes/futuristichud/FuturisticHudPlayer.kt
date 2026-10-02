package com.streamvault.app.ui.themes.futuristichud

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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

/** HUD command cell: square cut-corner key with index code, glyph and label stacked (no round buttons in this theme). */
@Composable
private fun FhKey(a: FhAct, code: String, modifier: Modifier = Modifier, wide: Boolean = false) {
    FhCard(onClick = a.onClick, shape = FH.RSmall, zoom = 1.0f, container = if (a.active) FH.Amber.copy(alpha = 0.22f) else FH.Card.copy(alpha = 0.85f), focusedContainer = FH.Amber.copy(alpha = 0.4f),
        modifier = modifier.then(if (wide) Modifier.fillMaxWidth() else Modifier.width(92.dp)).height(58.dp)) {
        Column(Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 4.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row { Text(code, color = FH.Faint, fontSize = 8.sp, fontFamily = FH.Mono, modifier = Modifier.weight(1f)); Text(a.glyph, color = if (a.active) FH.Amber else FH.Text, fontSize = 13.sp) }
            Text(a.label.uppercase(), color = FH.Text, fontSize = 9.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/** Telemetry line: [T+clock] ── channel id ── status flags. */
@Composable
private fun TopStrip(left: String, flags: List<Pair<String, Color>>) {
    Row(Modifier.fillMaxWidth().background(FH.Bg.copy(alpha = 0.85f)).border(1.dp, FH.Line).padding(horizontal = 16.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("◢ " + left.uppercase(), color = FH.Amber, fontSize = 12.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        flags.forEach { (t, c) -> FhBadge(t, c) }
        Text("T+" + fhClock(System.currentTimeMillis()), color = FH.Text, fontSize = 13.sp, fontFamily = FH.Mono)
    }
}

/** NOW/NEXT as a two-line telemetry readout with 24-segment progress. */
@Composable
private fun Readout(now: Program?, next: Program?, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (now != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("NOW>", color = FH.Amber, fontSize = 11.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Black)
                Text(now.title, color = FH.Text, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, false))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(fhClock(now.startTime), color = FH.Sub, fontSize = 10.sp, fontFamily = FH.Mono)
                FhProgress(now.progressAt(), Modifier.weight(1f), 6.dp)
                Text(fhClock(now.endTime), color = FH.Sub, fontSize = 10.sp, fontFamily = FH.Mono)
                Text("${(now.progressAt() * 100).toInt()}%", color = FH.Amber, fontSize = 10.sp, fontFamily = FH.Mono)
            }
        } else Text("NOW> " + tr("NO GUIDE DATA", "لا يوجد دليل"), color = FH.Faint, fontSize = 12.sp, fontFamily = FH.Mono)
        next?.let { Text("NXT> ${fhClock(it.startTime)}  ${it.title}", color = FH.Faint, fontSize = 11.sp, fontFamily = FH.Mono, maxLines = 1, overflow = TextOverflow.Ellipsis) }
    }
}

/** ID block: big mono channel number in a bracket box with logo + name + flags under it. */
@Composable
private fun IdBlock(channel: Channel?, name: String?, number: Int, resolution: String?, timeshift: Boolean, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(76.dp).border(1.dp, FH.Line).fhBrackets(FH.Amber, 14.dp, 2.dp), contentAlignment = Alignment.Center) {
            Text(if (number > 0) "%03d".format(number) else "---", color = FH.Amber, fontSize = 24.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Black)
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FhLogo(channel?.name ?: name.orEmpty(), channel?.logoUrl, 34.dp)
                Text((channel?.name ?: name.orEmpty()).uppercase(), color = FH.Text, fontSize = 18.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 320.dp))
                if (channel?.isFavorite == true) Text("★", color = FH.Warn, fontSize = 14.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FhBadge(if (timeshift) tr("DELAYED", "مؤجل") else "● LIVE", FH.Live, filled = !timeshift)
                (resolution ?: channel?.qualityBadge())?.let { FhBadge(it, FH.Text) }
                if (channel?.catchUpSupported == true) FhBadge(tr("ARCHIVE", "أرشيف"), FH.Blue)
                channel?.let { FhSignal(it.fhSignalLevel()) }
            }
        }
    }
}

@Composable
internal fun FuturisticHudPlayerOverlay(p: PlayerOverlayParams) {
    if (!p.visible) return
    if (p.isLive && !p.isCatchUpPlayback) FhLive(p) else FhVod(p)
}

/** Live: cockpit frame. Top telemetry strip; END-side command bank (3-column key grid, scrolls);
 *  bottom-START target panel with ID block + readout. Video stays clear in the centre. */
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
    Box(p.modifier.fillMaxSize()) {
        Column(Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(24.dp)) {
            TopStrip("CH ${p.displayChannelNumber} // ${p.currentChannel?.name ?: p.currentChannelName.orEmpty()}", buildList {
                if (rec) add("● REC" to FH.Live)
                if (p.timeshiftUiState.enabledForSession && p.timeshiftUiState.bufferedBehindLiveMs > 0) add("-" + fhDuration(p.timeshiftUiState.bufferedBehindLiveMs) to FH.Warn)
                if (p.isMuted) add("MUTE" to FH.Warn)
            })
        }
        Column(Modifier.align(Alignment.CenterEnd).padding(end = 24.dp, top = 70.dp, bottom = 24.dp).width(304.dp).fillMaxHeight().background(FH.Bg.copy(alpha = 0.88f)).border(1.dp, FH.Line).fhBrackets(FH.Amber, 18.dp).padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FhLabel(tr("Command bank", "الأوامر") + " [${acts.size}]")
            LazyVerticalGrid(GridCells.Fixed(3), Modifier.fillMaxSize().focusRequester(p.quickActionsFocusRequester), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(acts.size) { i -> FhKey(acts[i], "K%02d".format(i + 1), if (i == 0) Modifier.focusRequester(p.playButtonFocusRequester) else Modifier, wide = true) }
            }
        }
        Column(Modifier.align(Alignment.BottomStart).padding(24.dp).width(620.dp).background(FH.Bg.copy(alpha = 0.88f)).border(1.dp, FH.Line).fhBrackets(FH.Amber, 22.dp, 3.dp).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FhLabel(tr("Target", "الهدف"))
            IdBlock(p.currentChannel, p.currentChannelName, p.displayChannelNumber, p.resolutionBadgeLabel, p.timeshiftUiState.enabledForSession && p.timeshiftUiState.bufferedBehindLiveMs > 0)
            Readout(p.currentProgram, p.nextProgram, Modifier.fillMaxWidth())
        }
    }
}

/** VOD: reticle in the centre (play inside a bracket box, ±10 as chevron keys beside it), tick-ruler scrubber
 *  with large mono timecodes at the bottom, vertical command column at START, options as numbered system table END. */
@Composable
private fun FhVod(p: PlayerOverlayParams) {
    var sheet by remember { mutableStateOf(false) }
    val sheetFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { p.playButtonFocusRequester.requestFocus() } }
    LaunchedEffect(sheet) { if (sheet) runCatching { sheetFocus.requestFocus() } }
    fun seekBy(d: Long) { p.onUserInteraction(); p.onSeekToPosition((p.currentPosition + d).coerceIn(0L, (p.duration - 1_000L).coerceAtLeast(0L))) }
    Box(p.modifier.fillMaxSize().background(FH.Bg.copy(alpha = 0.35f))) {
        Column(Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            TopStrip((if (p.isCatchUpPlayback) "ARCHIVE // " else "VOD // ") + p.displayTitle, buildList {
                if (p.currentRecordingStatus == RecordingStatus.RECORDING) add("● REC" to FH.Live)
                if (p.playbackSpeed != 1f) add("${p.playbackSpeed}×" to FH.Amber)
            })
            p.episodeLine?.let { Text("  › " + it.uppercase(), color = FH.Sub, fontSize = 12.sp, fontFamily = FH.Mono, maxLines = 1) }
        }
        Column(Modifier.align(Alignment.CenterStart).padding(start = 24.dp).width(200.dp).background(FH.Bg.copy(alpha = 0.85f)).border(1.dp, FH.Line).padding(8.dp).focusRequester(p.quickActionsFocusRequester), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            FhLabel(tr("Commands", "الأوامر"))
            val cmds = buildList {
                add(FhAct("♪", tr("Audio", "الصوت") + " ${p.audioTrackCount}") { p.onOpenAudioTracks() })
                add(FhAct("CC", tr("Subtitles", "الترجمة") + " ${p.subtitleTrackCount}") { p.onOpenSubtitleTracks() })
                add(FhAct("HD", p.resolutionBadgeLabel ?: tr("Quality", "الجودة")) { p.onOpenVideoTracks() })
                if (p.showEpisodesAction) add(FhAct("≣", tr("Episodes", "الحلقات")) { p.onOpenEpisodes() })
                add(FhAct("⏮", tr("Start over", "من البداية")) { p.onSeekToPosition(0L) })
                add(FhAct("⚙", tr("Systems", "المزيد"), sheet) { sheet = !sheet })
                add(FhAct("◂", tr("Back", "رجوع")) { p.onNavigateBack() })
                add(FhAct("✕", tr("Close", "إغلاق")) { p.onClose() })
            }
            cmds.forEachIndexed { i, a -> FhKey(a, "F%d".format(i + 1), wide = true) }
        }
        Row(Modifier.align(Alignment.Center), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            FhCard(onClick = { seekBy(-STEP) }, shape = FH.RSmall, zoom = 1.0f, container = FH.Bg.copy(alpha = 0.7f), focusedContainer = FH.Amber.copy(alpha = 0.35f), modifier = Modifier.size(84.dp, 64.dp)) {
                Text("«10", color = FH.Text, fontSize = 20.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.Center))
            }
            FhCard(onClick = { p.onUserInteraction(); p.onTogglePlayPause() }, shape = FH.R, zoom = 1.0f, container = FH.Bg.copy(alpha = 0.75f), focusedContainer = FH.Amber.copy(alpha = 0.35f),
                modifier = Modifier.size(128.dp).fhBrackets(FH.Amber, 26.dp, 3.dp).focusRequester(p.playButtonFocusRequester)) {
                Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (p.isPlaying) "❚❚" else "▶", color = FH.Amber, fontSize = 40.sp)
                    Text(if (p.isPlaying) "HOLD" else "ENGAGE", color = FH.Sub, fontSize = 10.sp, fontFamily = FH.Mono)
                }
            }
            FhCard(onClick = { seekBy(STEP) }, shape = FH.RSmall, zoom = 1.0f, container = FH.Bg.copy(alpha = 0.7f), focusedContainer = FH.Amber.copy(alpha = 0.35f), modifier = Modifier.size(84.dp, 64.dp)) {
                Text("10»", color = FH.Text, fontSize = 20.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.Center))
            }
        }
        Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(start = 240.dp, end = if (sheet) 440.dp else 24.dp, bottom = 24.dp)) { Ruler(p, ::seekBy) }
        if (sheet) SystemsTable(p, sheetFocus) { sheet = false }
    }
}

/** Tick ruler: major/minor ticks over the track, cyan fill to the head, timecodes as big mono numerals. */
@Composable
private fun Ruler(p: PlayerOverlayParams, seekBy: (Long) -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val pos = if (p.seekPreview.visible) p.seekPreview.positionMs else p.currentPosition
    val frac = if (p.duration > 0) (pos.toFloat() / p.duration).coerceIn(0f, 1f) else 0f
    Column(Modifier.background(FH.Bg.copy(alpha = 0.85f)).border(1.dp, if (focused) FH.Amber else FH.Line).padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(fhDuration(pos), color = if (p.seekPreview.visible) FH.Warn else FH.Amber, fontSize = 30.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Black)
            Text("  / " + fhDuration(p.duration), color = FH.Sub, fontSize = 14.sp, fontFamily = FH.Mono)
            Spacer(Modifier.weight(1f))
            Text("REM -" + fhDuration((p.duration - pos).coerceAtLeast(0)), color = FH.Sub, fontSize = 13.sp, fontFamily = FH.Mono)
        }
        FhCard(
            onClick = { p.onUserInteraction(); p.onTogglePlayPause() }, shape = FH.RSmall, container = Color.Transparent, focusedContainer = FH.Amber.copy(alpha = 0.08f), zoom = 1f,
            modifier = Modifier.fillMaxWidth().height(30.dp).onFocusChanged { focused = it.isFocused; p.onSetScrubbingMode(it.isFocused) }.onPreviewKeyEvent { e ->
                val n = e.nativeKeyEvent
                if (n.action != android.view.KeyEvent.ACTION_DOWN) return@onPreviewKeyEvent false
                when (n.keyCode) {
                    android.view.KeyEvent.KEYCODE_DPAD_LEFT -> { seekBy(-STEP); true }
                    android.view.KeyEvent.KEYCODE_DPAD_RIGHT -> { seekBy(STEP); true }
                    else -> false
                }
            }
        ) {
            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                repeat(41) { i -> Box(Modifier.width(1.dp).fillMaxHeight(if (i % 10 == 0) 0.9f else if (i % 5 == 0) 0.6f else 0.35f).background(if (i / 40f <= frac) FH.Amber else FH.Line)) }
            }
            Box(Modifier.align(Alignment.BottomStart).fillMaxWidth(frac).height(3.dp).background(FH.Amber))
        }
    }
}

@Composable
private fun BoxScope.SystemsTable(p: PlayerOverlayParams, focus: FocusRequester, onDismiss: () -> Unit) {
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
        Modifier.align(Alignment.CenterEnd).padding(24.dp).fillMaxHeight(0.86f).width(400.dp).background(FH.Bg.copy(alpha = 0.97f)).border(1.dp, FH.Line).fhBrackets(FH.Amber, 20.dp, 3.dp).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("// " + tr("SYSTEMS", "خيارات التشغيل"), color = FH.Amber, fontSize = 16.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            FhKey(FhAct("✕", tr("Close", "إغلاق"), onClick = onDismiss), "ESC", Modifier.width(80.dp))
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            Text("ID", color = FH.Faint, fontSize = 9.sp, fontFamily = FH.Mono, modifier = Modifier.width(34.dp)); Text("PARAM", color = FH.Faint, fontSize = 9.sp, fontFamily = FH.Mono, modifier = Modifier.weight(1f)); Text("VALUE", color = FH.Faint, fontSize = 9.sp, fontFamily = FH.Mono)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            items(rows.size) { i ->
                val (label, value, action) = rows[i]
                FhCard(onClick = action, shape = FH.RSmall, container = Color.Transparent, focusedContainer = FH.Amber.copy(alpha = 0.25f), zoom = 1.0f, modifier = Modifier.fillMaxWidth().then(if (i == 0) Modifier.focusRequester(focus) else Modifier)) {
                    Row(Modifier.padding(horizontal = 8.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("%02d".format(i + 1), color = FH.Faint, fontSize = 11.sp, fontFamily = FH.Mono, modifier = Modifier.width(34.dp))
                        Text(label.uppercase(), color = FH.Text, fontSize = 12.sp, fontFamily = FH.Mono, modifier = Modifier.weight(1f), maxLines = 1)
                        Text(value.ifBlank { "—" }, color = FH.Amber, fontSize = 12.sp, fontFamily = FH.Mono, maxLines = 1)
                    }
                }
            }
        }
    }
}

/** Channel list: END-docked radar column. Target-lock card for the focused channel sits at the TOP;
 *  below it a dense numbered target log (index bar, code, name, meter, flags). Category commands at the bottom. */
@Composable
internal fun FuturisticHudLiveChannelList(p: LiveChannelListParams) {
    val idx = remember(p.channels, p.currentChannelId) { p.channels.indexOfFirst { it.id == p.currentChannelId }.coerceAtLeast(0) }
    val state = rememberLazyListState(idx)
    var focused by remember(p.currentChannelId) { mutableStateOf(p.channels.getOrNull(idx)) }
    LaunchedEffect(Unit) { runCatching { p.focusRequester.requestFocus() } }
    Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent, FH.Bg.copy(alpha = 0.8f))))) {
        Column(Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(560.dp).padding(top = 20.dp, bottom = 20.dp, end = 20.dp).background(FH.Bg.copy(alpha = 0.94f)).border(1.dp, FH.Line).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("◢ " + (p.lastVisitedCategoryName ?: tr("All channels", "كل القنوات")).uppercase(), color = FH.Amber, fontSize = 15.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Text("[%03d] ".format(p.channels.size) + tr("SIGNALS", "قناة"), color = FH.Sub, fontSize = 11.sp, fontFamily = FH.Mono)
            }
            focused?.let { c ->
                Column(Modifier.fillMaxWidth().border(1.dp, FH.Amber.copy(alpha = 0.6f)).fhBrackets(FH.Amber, 16.dp, 2.dp).padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("LOCK", color = FH.Live, fontSize = 10.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Black)
                        FhLogo(c.name, c.logoUrl, 30.dp)
                        Text(c.name.uppercase(), color = FH.Text, fontSize = 13.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    }
                    Readout(c.currentProgram, c.nextProgram, Modifier.fillMaxWidth())
                }
            }
            LazyColumn(state = state, verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                items(p.channels, key = { it.id }) { c ->
                    val cur = c.id == p.currentChannelId
                    var f by remember { mutableStateOf(false) }
                    FhCard(
                        onClick = { p.onInteracted(); p.onSelectChannel(c.id) }, onLongClick = { p.onChannelLongPress(c) }, zoom = 1.0f, shape = FH.RSmall,
                        container = if (cur) FH.Amber.copy(alpha = 0.16f) else Color.Transparent, focusedContainer = FH.Amber.copy(alpha = 0.3f),
                        modifier = Modifier.fillMaxWidth().then(if (cur) Modifier.focusRequester(p.focusRequester) else Modifier).onFocusChanged { f = it.isFocused; if (it.isFocused) { focused = c; p.onInteracted() } }
                    ) {
                        Row(Modifier.height(40.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.width(3.dp).fillMaxHeight().background(if (f || cur) FH.Amber else FH.Line))
                            Text("%03d".format(p.numberOf(c)), color = if (cur) FH.Amber else FH.Faint, fontSize = 12.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Bold)
                            FhLogo(c.name, c.logoUrl, 28.dp)
                            Text(if (c.id == p.movingChannelId) "⇅ ${c.name.uppercase()}" else c.name.uppercase(), color = FH.Text, fontSize = 12.sp, fontFamily = FH.Mono, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            if (cur) FhBadge(tr("NOW", "الآن"), FH.Live, filled = true)
                            c.qualityBadge()?.let { FhBadge(it, FH.Text) }
                            if (c.catchUpSupported) FhBadge("⟲", FH.Blue)
                            FhSignal(c.fhSignalLevel())
                            Text(if (c.isFavorite) "★" else " ", color = FH.Warn, fontSize = 12.sp, modifier = Modifier.padding(end = 6.dp))
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FhKey(FhAct("▤", tr("Groups", "الفئات"), onClick = p.onOpenCategories), "G1", Modifier.weight(1f), wide = true)
                if (p.lastVisitedCategoryName != null) FhKey(FhAct("↩", tr("Last group", "آخر مجموعة"), onClick = p.onOpenLastGroup), "G2", Modifier.weight(1f), wide = true)
                FhKey(FhAct("▦", tr("Guide", "الدليل"), onClick = p.onOpenGuide), "G3", Modifier.weight(1f), wide = true)
                FhKey(FhAct("✕", tr("Close", "إغلاق"), onClick = p.onDismiss), "ESC", Modifier.weight(1f), wide = true)
            }
        }
    }
}

/** Zap banner: full-width bottom console split into three bays (ID block | readout | clock+flags),
 *  then a row of F-key cells: Alaa's 8 first (F1..F8), extras after. */
@Composable
internal fun FuturisticHudLiveChannelInfo(p: LiveChannelInfoParams) {
    LaunchedEffect(Unit) { runCatching { p.focusRequester.requestFocus() } }
    val rec = p.currentRecordingStatus == RecordingStatus.RECORDING
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
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(20.dp).background(FH.Bg.copy(alpha = 0.93f)).border(1.dp, FH.Line).fhBrackets(FH.Amber, 24.dp, 3.dp).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
                IdBlock(p.channel, null, p.displayChannelNumber, p.resolutionLabel, false)
                Box(Modifier.padding(horizontal = 16.dp).width(1.dp).fillMaxHeight().background(FH.Line))
                Readout(p.currentProgram, p.nextProgram, Modifier.weight(1f))
                Box(Modifier.padding(horizontal = 16.dp).width(1.dp).fillMaxHeight().background(FH.Line))
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(fhClock(System.currentTimeMillis()), color = FH.Amber, fontSize = 28.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Black)
                    if (rec) FhBadge("● REC", FH.Live, filled = true)
                    if (p.isMuted) FhBadge("MUTE", FH.Warn)
                }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                items(acts.size) { i -> FhKey(acts[i], if (i < 8) "F${i + 1}" else "X${i - 7}", if (i == 0) Modifier.focusRequester(p.focusRequester) else Modifier) }
            }
        }
    }
}
