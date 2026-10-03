package com.streamvault.app.ui.themes.magazinemedia

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

private class MzAct(val glyph: String, val label: String, val active: Boolean = false, val onClick: () -> Unit)

/** Index entry: a printed text link (glyph + small caps word) on paper; rust when active, ink block on focus. */
@Composable
private fun MzCtl(a: MzAct, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 52.dp) {
    androidx.compose.foundation.layout.Box(modifier) { MzButton(a.label, a.onClick, primary = a.active, icon = a.glyph) }
}

@Composable
private fun NowNext(now: Program?, next: Program?, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (now != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MzBadge(tr("NOW", "الآن"), MZ.Amber, filled = true)
                Text(now.title, color = MZ.Text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, false))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(mzClock(now.startTime), color = MZ.Sub, fontSize = 12.sp)
                MzProgress(now.progressAt(), Modifier.weight(1f))
                Text(mzClock(now.endTime), color = MZ.Sub, fontSize = 12.sp)
            }
        } else Text(tr("No programme information", "لا توجد معلومات عن البرنامج"), color = MZ.Faint, fontSize = 14.sp)
        next?.let { Text(tr("Next", "التالي") + "  ${mzClock(it.startTime)}  ·  ${it.title}", color = MZ.Faint, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
    }
}

@Composable
private fun ChannelHead(channel: Channel?, name: String?, number: Int, resolution: String?, timeshift: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        MzLogo(channel?.name ?: name.orEmpty(), channel?.logoUrl, 64.dp)
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (number > 0) Text("$number", color = MZ.Amber, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text(channel?.name ?: name.orEmpty(), color = MZ.Text, fontSize = 24.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Black, maxLines = 1)
                if (channel?.isFavorite == true) Text("♥", color = MZ.Amber, fontSize = 16.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MzBadge(if (timeshift) tr("PAUSED LIVE", "مباشر مؤجل") else "● LIVE", MZ.Live, filled = !timeshift)
                (resolution ?: channel?.qualityBadge())?.let { MzBadge(it, MZ.Text) }
                if (channel?.catchUpSupported == true) MzBadge(tr("CATCH-UP", "أرشيف"), MZ.Blue)
            }
        }
    }
}



@Composable
internal fun MagazineMediaPlayerOverlay(p: PlayerOverlayParams) {
    if (!p.visible) return
    if (p.isLive && !p.isCatchUpPlayback) MzLive(p) else MzVod(p)
}

/** Live: soft bottom gradient, channel head + now/next on the start, clock on the end, one row of round controls. */
@Composable
private fun MzLive(p: PlayerOverlayParams) {
    LaunchedEffect(Unit) { runCatching { p.playButtonFocusRequester.requestFocus() } }
    val rec = p.currentRecordingStatus == RecordingStatus.RECORDING
    val acts = buildList {
        add(MzAct(if (p.isPlaying) "❚❚" else "▶", if (p.isPlaying) tr("Pause", "إيقاف") else tr("Play", "تشغيل"), true) { p.onUserInteraction(); p.onTogglePlayPause() })
        add(MzAct("☰", tr("Channels", "القنوات")) { p.onOpenLiveChannels() })
        add(MzAct("▦", tr("Guide", "الدليل")) { p.onOpenLiveGuide() })
        add(MzAct(if (p.currentChannel?.isFavorite == true) "♥" else "♡", tr("My List", "قائمتي"), p.currentChannel?.isFavorite == true) { p.onToggleLiveFavorite() })
        add(MzAct("⏪", "-10s") { p.onUserInteraction(); p.onSeekBackward() })
        add(MzAct("⏩", "+10s") { p.onUserInteraction(); p.onSeekForward() })
        add(MzAct("⏮", tr("Restart", "من البداية")) { p.onRestartProgram() })
        add(MzAct("⟲", tr("Catch-up", "الأرشيف")) { p.onOpenArchive() })
        if (p.timeshiftUiState.canSeekToLive) add(MzAct("⇥", tr("Go live", "مباشر")) { p.onSeekToLiveEdge() })
        add(MzAct("CC", tr("Subtitles", "الترجمة") + " ${p.subtitleTrackCount}") { p.onOpenSubtitleTracks() })
        add(MzAct("♪", tr("Audio", "الصوت") + " ${p.audioTrackCount}") { p.onOpenAudioTracks() })
        add(MzAct("HD", p.resolutionBadgeLabel ?: tr("Quality", "الجودة")) { p.onOpenVideoTracks() })
        add(MzAct(if (p.isMuted) "🔇" else "🔊", tr("Mute", "كتم"), p.isMuted) { p.onToggleMute() })
        add(MzAct(if (rec) "■" else "●", if (rec) tr("Stop rec", "إيقاف التسجيل") else tr("Record", "تسجيل"), rec) { if (rec) p.onStopRecording() else p.onStartRecording() })
        add(MzAct("◷", tr("Schedule", "جدولة")) { p.onScheduleRecording() })
        add(MzAct("◷", tr("Daily", "يومي")) { p.onScheduleDailyRecording() })
        add(MzAct("◷", tr("Weekly", "أسبوعي")) { p.onScheduleWeeklyRecording() })
        add(MzAct("▭", p.aspectRatioLabel) { p.onToggleAspectRatio() })
        add(MzAct("»", "${p.playbackSpeed}×") { p.onOpenPlaybackSpeed() })
        if (p.audioVideoSyncEnabled) add(MzAct("⇄", tr("A/V sync", "مزامنة")) { p.onOpenAudioVideoSync() })
        add(MzAct("◫", tr("Multiview", "تقسيم")) { p.onOpenSplitScreen() })
        add(MzAct("⏲", if (p.sleepTimerUiState.stopTimerActive) mzDuration(p.sleepTimerUiState.stopRemainingMs) else tr("Sleep", "مؤقت"), p.sleepTimerUiState.stopTimerActive) { p.onOpenStopPlaybackTimer() })
        add(MzAct("☾", if (p.sleepTimerUiState.idleTimerActive) mzDuration(p.sleepTimerUiState.idleRemainingMs) else tr("Standby", "استعداد"), p.sleepTimerUiState.idleTimerActive) { p.onOpenIdleStandbyTimer() })
        add(MzAct("⧉", "PiP") { p.onEnterPictureInPicture() })
        add(MzAct("⎚", if (p.isCastConnected) tr("Stop cast", "إيقاف البث") else tr("Cast", "بث"), p.isCastConnected) { if (p.isCastConnected) p.onStopCasting() else p.onCast() })
        add(MzAct("✕", tr("Close", "إغلاق")) { p.onClose() })
    }
    // Broadsheet: a paper page slides over the lower third; masthead rule, three ruled columns, printed index of actions.
    Box(p.modifier.fillMaxSize()) {
        Column(Modifier.align(Alignment.BottomStart).fillMaxWidth().background(MZ.Bg.copy(alpha = 0.97f))) {
            MzRule(thick = 4.dp)
            Row(Modifier.fillMaxWidth().padding(horizontal = 48.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                MzKicker(tr("Live edition", "الطبعة المباشرة"), Modifier.weight(1f))
                if (rec) MzBadge("● REC", MZ.Live, filled = true)
                Text("  " + mzClock(System.currentTimeMillis()), color = MZ.Text, fontSize = 16.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Bold)
            }
            MzRule(Modifier.padding(horizontal = 48.dp))
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(horizontal = 48.dp, vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Box(Modifier.weight(0.42f)) { ChannelHead(p.currentChannel, p.currentChannelName, p.displayChannelNumber, p.resolutionBadgeLabel, p.timeshiftUiState.enabledForSession && p.timeshiftUiState.bufferedBehindLiveMs > 0) }
                Box(Modifier.width(1.dp).fillMaxHeight().background(MZ.Line))
                NowNext(p.currentProgram, p.nextProgram, Modifier.weight(0.58f))
            }
            MzRule(Modifier.padding(horizontal = 48.dp), color = MZ.Line)
            LazyRow(Modifier.fillMaxWidth().focusRequester(p.quickActionsFocusRequester), contentPadding = PaddingValues(horizontal = 48.dp, vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(acts.size) { i -> MzCtl(acts[i], if (i == 0) Modifier.focusRequester(p.playButtonFocusRequester) else Modifier) }
            }
        }
    }
}

/** VOD: title top-left over a gradient, giant centered transport (-10 / play / +10), thin amber scrubber, pill row + side sheet. */
@Composable
private fun MzVod(p: PlayerOverlayParams) {
    var sheet by remember { mutableStateOf(false) }
    val sheetFocus = remember { FocusRequester() }
    val moreFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { p.playButtonFocusRequester.requestFocus() } }
    LaunchedEffect(sheet) { if (sheet) runCatching { sheetFocus.requestFocus() } }
    fun seekBy(d: Long) { p.onUserInteraction(); p.onSeekToPosition((p.currentPosition + d).coerceIn(0L, (p.duration - 1_000L).coerceAtLeast(0L))) }
    Box(p.modifier.fillMaxSize()) {
        Row(Modifier.align(Alignment.TopStart).fillMaxWidth().background(MZ.Bg.copy(alpha = 0.96f)).padding(horizontal = 48.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MzRound("←", { p.onNavigateBack() }, size = 44.dp)
            Column(Modifier.weight(1f)) {
                if (p.isCatchUpPlayback) Text(tr("Catch-up", "من الأرشيف"), color = MZ.Amber, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(p.displayTitle, color = MZ.Text, fontSize = 30.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                p.episodeLine?.let { Text(it, color = MZ.Sub, fontSize = 15.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, maxLines = 1) }
            }
            if (p.currentRecordingStatus == RecordingStatus.RECORDING) MzBadge("● REC", MZ.Live, filled = true)
            if (p.playbackSpeed != 1f) MzBadge("${p.playbackSpeed}×", MZ.Amber)
            Text(mzClock(System.currentTimeMillis()), color = MZ.Sub, fontSize = 20.sp)
        }
        Column(Modifier.align(Alignment.BottomStart).fillMaxWidth(if (sheet) 0.64f else 1f).background(MZ.Bg.copy(alpha = 0.96f))) {
            MzRule(thick = 4.dp)
            Column(Modifier.padding(horizontal = 48.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MzButton("10s", { seekBy(-STEP) }, icon = "↺")
                MzButton(if (p.isPlaying) tr("Pause", "إيقاف") else tr("Play", "تشغيل"), { p.onUserInteraction(); p.onTogglePlayPause() }, Modifier.focusRequester(p.playButtonFocusRequester), primary = true, icon = if (p.isPlaying) "❚❚" else "▶")
                MzButton("10s", { seekBy(STEP) }, icon = "↻")
                Spacer(Modifier.width(12.dp))
                Box(Modifier.weight(1f)) { Scrubber(p, ::seekBy) }
            }
            MzRule(color = MZ.Line)
            LazyRow(Modifier.focusRequester(p.quickActionsFocusRequester), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item { MzButton(tr("Audio", "الصوت") + " · ${p.audioTrackCount}", p.onOpenAudioTracks, icon = "♪") }
                item { MzButton(tr("Subtitles", "الترجمة") + " · ${p.subtitleTrackCount}", p.onOpenSubtitleTracks, icon = "CC") }
                item { MzButton(p.resolutionBadgeLabel ?: tr("Quality", "الجودة"), p.onOpenVideoTracks, icon = "HD") }
                if (p.showEpisodesAction) item { MzButton(tr("Episodes", "الحلقات"), p.onOpenEpisodes, icon = "≣") }
                item { MzButton(tr("Start over", "من البداية"), { p.onSeekToPosition(0L) }, icon = "⏮") }
                item { androidx.compose.foundation.layout.Box(Modifier.focusRequester(moreFocus)) { MzButton(tr("More", "المزيد"), { sheet = !sheet }, primary = sheet, icon = "⚙") } }
                item { MzButton(tr("Close", "إغلاق"), p.onClose, icon = "✕") }
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
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        MzCard(
            onClick = { p.onUserInteraction(); p.onTogglePlayPause() }, shape = MZ.Pill, container = Color.Transparent, focusedContainer = MZ.Gold, zoom = 1f,
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
                MzProgress(if (p.duration > 0) (pos.toFloat() / p.duration).coerceIn(0f, 1f) else 0f, Modifier.fillMaxWidth(), if (focused) 8.dp else 4.dp)
            }
        }
        Row {
            Text(mzDuration(pos), color = if (p.seekPreview.visible) MZ.Amber else MZ.Text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Text("-" + mzDuration((p.duration - pos).coerceAtLeast(0)), color = MZ.Sub, fontSize = 14.sp)
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
        add(Triple(tr("Sleep timer", "مؤقت النوم"), if (p.sleepTimerUiState.stopTimerActive) mzDuration(p.sleepTimerUiState.stopRemainingMs) else tr("Off", "معطل"), p.onOpenStopPlaybackTimer))
        add(Triple(tr("Standby timer", "مؤقت الاستعداد"), if (p.sleepTimerUiState.idleTimerActive) mzDuration(p.sleepTimerUiState.idleRemainingMs) else tr("Off", "معطل"), p.onOpenIdleStandbyTimer))
        add(Triple(tr("Multiview", "تقسيم الشاشة"), "", p.onOpenSplitScreen))
        add(Triple(tr("Picture in picture", "صورة داخل صورة"), "", p.onEnterPictureInPicture))
        add(Triple(tr("Cast", "البث"), if (p.isCastConnected) tr("Connected", "متصل") else "", if (p.isCastConnected) p.onStopCasting else p.onCast))
    }
    Column(
        Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(400.dp).background(MZ.Raised).border(2.dp, MZ.Text).padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { MzKicker(tr("Index", "الفهرس")); Text(tr("Playback options", "خيارات التشغيل"), color = MZ.Text, fontSize = 22.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Bold) }
            MzRound("✕", onDismiss, size = 36.dp)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(rows.size) { i ->
                val (label, value, action) = rows[i]
                MzCard(onClick = action, shape = MZ.RSmall, container = Color.Transparent, focusedContainer = MZ.Card, zoom = 1.02f, modifier = Modifier.fillMaxWidth().then(if (i == 0) Modifier.focusRequester(focus) else Modifier)) {
                    Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("%02d  ".format(i + 1), color = MZ.Amber, fontSize = 13.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Bold)
                        Text(label, color = MZ.Text, fontSize = 15.sp, fontFamily = MZ.Serif, modifier = Modifier.weight(1f), maxLines = 1)
                        Text(value, color = MZ.Amber, fontSize = 14.sp, maxLines = 1)
                        Text("  ›", color = MZ.Faint, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

/** Channel list: a printed TV-listings page on the START edge. Masthead with count, ruled rows with big serif
 *  numerals in the margin, small-caps badges, and a "today" clipping below for the focused channel's now/next. */
@Composable
internal fun MagazineMediaLiveChannelList(p: LiveChannelListParams) {
    val idx = remember(p.channels, p.currentChannelId) { p.channels.indexOfFirst { it.id == p.currentChannelId }.coerceAtLeast(0) }
    val state = rememberLazyListState(idx)
    var focused by remember(p.currentChannelId) { mutableStateOf(p.channels.getOrNull(idx)) }
    LaunchedEffect(Unit) { runCatching { p.focusRequester.requestFocus() } }
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.align(Alignment.TopStart).fillMaxHeight().width(560.dp).background(MZ.Bg.copy(alpha = 0.98f)).padding(start = 36.dp, top = 26.dp, bottom = 20.dp, end = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f)) {
                    MzKicker(tr("Tonight's listings", "قائمة القنوات"))
                    Text(p.lastVisitedCategoryName ?: tr("All channels", "كل القنوات"), color = MZ.Text, fontSize = 28.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text("${p.channels.size} " + tr("channels", "قناة"), color = MZ.Sub, fontSize = 13.sp, fontFamily = MZ.Serif, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            }
            MzRule(thick = 3.dp)
            LazyColumn(state = state, modifier = Modifier.weight(1f), contentPadding = PaddingValues(vertical = 4.dp)) {
                items(p.channels, key = { it.id }) { c ->
                    val cur = c.id == p.currentChannelId
                    Column {
                        MzCard(
                            onClick = { p.onInteracted(); p.onSelectChannel(c.id) }, onLongClick = { p.onChannelLongPress(c) }, zoom = 1.01f,
                            container = if (cur) MZ.Gold else Color.Transparent, focusedContainer = MZ.Raised,
                            modifier = Modifier.fillMaxWidth().then(if (cur) Modifier.focusRequester(p.focusRequester) else Modifier).onFocusChanged { if (it.isFocused) { focused = c; p.onInteracted() } }
                        ) {
                            Row(Modifier.padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("${p.numberOf(c)}", color = if (cur) MZ.Amber else MZ.Text, fontSize = 22.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Black, modifier = Modifier.width(52.dp))
                                MzLogo(c.name, c.logoUrl, 36.dp)
                                Column(Modifier.weight(1f)) {
                                    Text(if (c.id == p.movingChannelId) "⇅  ${c.name}" else c.name, color = MZ.Text, fontSize = 16.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    c.currentProgram?.let { Text(it.title, color = MZ.Sub, fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                                }
                                if (cur) MzBadge(tr("Now", "الآن"), MZ.Amber, filled = true)
                                c.qualityBadge()?.let { MzBadge(it, MZ.Text) }
                                if (c.catchUpSupported) MzBadge("⟲ " + tr("Archive", "أرشيف"), MZ.Blue)
                                if (c.isFavorite) Text("♥", color = MZ.Amber, fontSize = 14.sp)
                            }
                        }
                        MzRule(color = MZ.Line)
                    }
                }
            }
            focused?.let { c ->
                Column(Modifier.fillMaxWidth().background(MZ.Raised).border(1.dp, MZ.Text).padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    MzKicker(tr("On now", "يعرض الآن") + " · " + c.name)
                    NowNext(c.currentProgram, c.nextProgram)
                }
            }
        }
        Box(Modifier.align(Alignment.TopStart).padding(start = 560.dp).width(4.dp).fillMaxHeight().background(MZ.Text))
    }
}

/** Zap banner: a "stop press" paper strip pinned to the bottom edge; ink rule on top, three ruled columns
 *  (stamp + headline | now/next | clock), then a printed index of actions. */
@Composable
internal fun MagazineMediaLiveChannelInfo(p: LiveChannelInfoParams) {
    LaunchedEffect(Unit) { runCatching { p.focusRequester.requestFocus() } }
    val rec = p.currentRecordingStatus == RecordingStatus.RECORDING
    // Alaa's required order first: Channels, Favorites, Audio, Aspect, Settings, Quality, Subs, Guide.
    val acts = buildList {
        add(MzAct("☰", tr("Channels", "القنوات"), true) { p.onInteracted(); p.onOpenChannelList() })
        add(MzAct(if (p.channel?.isFavorite == true) "♥" else "♡", tr("Favorites", "المفضلة"), p.channel?.isFavorite == true) { p.onInteracted(); p.onToggleFavorite() })
        add(MzAct("♪", tr("Audio", "الصوت")) { p.onInteracted(); p.onOpenAudioTracks() })
        add(MzAct("▭", tr("Aspect", "العرض") + " " + p.aspectRatioLabel) { p.onInteracted(); p.onToggleAspectRatio() })
        add(MzAct("⚙", tr("Settings", "الإعدادات")) { p.onInteracted(); p.onOpenSettings() })
        add(MzAct("HD", p.resolutionLabel ?: tr("Quality", "الجودة")) { p.onInteracted(); p.onOpenVideoTracks() })
        add(MzAct("CC", tr("Subs", "الترجمة")) { p.onInteracted(); p.onOpenSubtitleTracks() })
        add(MzAct("▦", tr("Guide", "الدليل")) { p.onInteracted(); p.onOpenFullEpg() })
        add(MzAct(if (p.isPlaying) "❚❚" else "▶", if (p.isPlaying) tr("Pause", "إيقاف") else tr("Play", "تشغيل")) { p.onInteracted(); p.onTogglePlayPause() })
        add(MzAct(if (p.isMuted) "🔇" else "🔊", tr("Mute", "كتم"), p.isMuted) { p.onInteracted(); p.onToggleMute() })
        if (p.variantCount > 1) add(MzAct("⇋", tr("Source", "المصدر")) { p.onInteracted(); p.onOpenVariants() })
        add(MzAct("⏮", tr("Restart", "من البداية")) { p.onInteracted(); p.onRestartProgram() })
        add(MzAct("⟲", tr("Catch-up", "الأرشيف")) { p.onInteracted(); p.onOpenArchive() })
        if (p.canSeekToLive) add(MzAct("⇥", tr("Go live", "مباشر")) { p.onInteracted(); p.onSeekToLiveEdge() })
        add(MzAct(if (rec) "■" else "●", if (rec) tr("Stop rec", "إيقاف") else tr("Record", "تسجيل"), rec) { p.onInteracted(); if (rec) p.onStopRecording() else p.onStartRecording() })
        add(MzAct("◷", tr("Schedule", "جدولة")) { p.onInteracted(); p.onScheduleRecording() })
        add(MzAct("⇄", "A/V") { p.onInteracted(); p.onOpenAudioVideoSync() })
        add(MzAct("◫", tr("Multiview", "تقسيم")) { p.onInteracted(); p.onOpenSplitScreen() })
        add(MzAct("∿", tr("Stats", "إحصاءات"), p.isDiagnosticsEnabled) { p.onInteracted(); p.onToggleDiagnostics() })
        add(MzAct("⧉", "PiP") { p.onEnterPictureInPicture() })
        add(MzAct("⎚", if (p.isCastConnected) tr("Stop cast", "إيقاف البث") else tr("Cast", "بث"), p.isCastConnected) { if (p.isCastConnected) p.onStopCasting() else p.onCast() })
    }
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(MZ.Bg.copy(alpha = 0.97f))) {
            MzRule(thick = 4.dp)
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(horizontal = 40.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                MzBadge(tr("Stop press", "عاجل"), MZ.Live, filled = true)
                ChannelHead(p.channel, null, p.displayChannelNumber, p.resolutionLabel, false)
                Box(Modifier.width(1.dp).fillMaxHeight().background(MZ.Line))
                NowNext(p.currentProgram, p.nextProgram, Modifier.weight(1f))
                Box(Modifier.width(1.dp).fillMaxHeight().background(MZ.Line))
                Text(mzClock(System.currentTimeMillis()), color = MZ.Text, fontSize = 30.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Bold)
            }
            MzRule(Modifier.padding(horizontal = 40.dp), color = MZ.Line)
            LazyRow(contentPadding = PaddingValues(horizontal = 40.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(acts.size) { i -> MzCtl(acts[i], if (i == 0) Modifier.focusRequester(p.focusRequester) else Modifier) }
            }
        }
    }
}
