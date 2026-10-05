package com.streamvault.app.ui.themes.ivano

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay

import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Glow
import coil3.compose.AsyncImage
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.app.ui.themes.bespoke.qualityBadge
import com.streamvault.app.ui.themes.bespoke.tr
import com.streamvault.app.ui.themes.bespoke.InnerPanelBackScope
import com.streamvault.app.ui.themes.bespoke.PlayerOverlayParams

private const val GSTEP = 10_000L
private val Cyan = Color(0xFF9B5CFF)
private val Grey = Color(0xFFC9B8E8)

/**
 * Cyan Premium player: top brand bar + clock/wifi/settings, bottom glass info card (poster, title,
 * episode line, 2-line synopsis, 4K/HDR/Dolby badges), full-width glowing seek bar, and a control row
 * Quality/Audio/Subtitles | -10 / big ring play / +10 / Next Episode | PiP/Cast. Live hides seek + next.
 */
@Composable
internal fun IvGlassPlayer(p: PlayerOverlayParams) {
    val live = p.isLive && !p.isCatchUpPlayback
    var sheet by remember { mutableStateOf(false) }
    val sheetFocus = remember { FocusRequester() }
    val gear = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { p.playButtonFocusRequester.requestFocus() } }
    LaunchedEffect(sheet) { if (sheet) runCatching { sheetFocus.requestFocus() } }
    fun seekBy(d: Long) { p.onUserInteraction(); p.onSeekToPosition((p.currentPosition + d).coerceIn(0L, (p.duration - 1_000L).coerceAtLeast(0L))) }

    Box(p.modifier.fillMaxSize()) {
        // soft gradients: thin at top, deeper at bottom; video stays clear in between
        Box(Modifier.fillMaxWidth().height(120.dp).align(Alignment.TopCenter).background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.55f), Color.Transparent))))
        Box(Modifier.fillMaxWidth().fillMaxHeight(0.55f).align(Alignment.BottomCenter).background(Brush.verticalGradient(listOf(Color.Transparent, Color(0x99050B28), Color(0xEE050B28)))))

        // TOP BAR
        Row(Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(horizontal = 36.dp, vertical = 22.dp), verticalAlignment = Alignment.CenterVertically) {
            IvLogo(size = 26)
            Box(Modifier.padding(horizontal = 14.dp).width(1.dp).height(20.dp).background(Color.White.copy(alpha = 0.35f)))
            Text(if (live) tr("Live TV", "البث المباشر") else tr("Premium Movies & Series", "أفلام ومسلسلات مميزة"), color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
            Spacer(Modifier.weight(1f))
            Text(cgClock(System.currentTimeMillis()), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.width(20.dp))
            Icon(Icons.Outlined.Wifi, null, tint = Color.White, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(14.dp))
            GlassIconButton(Icons.Outlined.Settings, Modifier.focusRequester(gear)) { sheet = !sheet }
        }

        // BOTTOM
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 36.dp).padding(bottom = 22.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            InfoCard(p, live, Modifier.fillMaxWidth())
            if (!live && p.duration > 0) SeekBar(p, ::seekBy)
            else if (live) LiveProgress(p)
            Row(Modifier.fillMaxWidth().padding(top = 6.dp).clip(RoundedCornerShape(28.dp)).background(Brush.horizontalGradient(listOf(Color(0xCC0A1440), Color(0xE6122A78), Color(0xCC0A1440)))).border(1.dp, Color(0x33B48CFF), RoundedCornerShape(28.dp)).padding(horizontal = 18.dp, vertical = 6.dp).focusRequester(p.quickActionsFocusRequester), verticalAlignment = Alignment.CenterVertically) {
                LabeledAction(Icons.Outlined.Settings, tr("Quality", "الجودة"), p.resolutionBadgeLabel ?: "${p.videoQualityCount}", p.onOpenVideoTracks)
                LabeledAction(Icons.AutoMirrored.Outlined.VolumeUp, tr("Audio", "الصوت"), if (p.isMuted) tr("Muted", "مكتوم") else "${p.audioTrackCount} " + tr("tracks", "مسارات"), p.onOpenAudioTracks)
                LabeledAction(Icons.Outlined.ClosedCaption, tr("Subtitles", "الترجمة"), if (p.subtitleTrackCount > 0) "${p.subtitleTrackCount}" else tr("Off", "معطلة"), p.onOpenSubtitleTracks)
                Spacer(Modifier.weight(1f))
                if (!live || p.timeshiftUiState.enabledForSession) RoundSeek(Icons.Outlined.Replay10) { if (live) p.onSeekBackward() else seekBy(-GSTEP) }
                Spacer(Modifier.width(22.dp))
                BigPlay(p.isPlaying, Modifier.focusRequester(p.playButtonFocusRequester)) { p.onUserInteraction(); p.onTogglePlayPause() }
                Spacer(Modifier.width(22.dp))
                if (!live || p.timeshiftUiState.enabledForSession) RoundSeek(Icons.Outlined.Forward10) { if (live) p.onSeekForward() else seekBy(GSTEP) }
                Spacer(Modifier.width(26.dp))
                when {
                    !live && p.showEpisodesAction -> CyanPill(Icons.Filled.SkipNext, tr("Next Episode", "الحلقة التالية"), p.onOpenEpisodes)
                    live -> CyanPill(Icons.AutoMirrored.Outlined.PlaylistPlay, tr("Channels", "القنوات"), p.onOpenLiveChannels)
                }
                Spacer(Modifier.weight(1f))
                LabeledAction(Icons.Outlined.PictureInPicture, "PiP", tr("Off", "معطل"), p.onEnterPictureInPicture)
                LabeledAction(Icons.Outlined.Cast, tr("Cast", "بث"), if (p.isCastConnected) tr("Connected", "متصل") else tr("Off", "معطل"), if (p.isCastConnected) p.onStopCasting else p.onCast)
            }
        }
        if (sheet) InnerPanelBackScope(onClose = { sheet = false }, opener = gear) { IvPlayerSheet(p, sheetFocus) { sheet = false; runCatching { gear.requestFocus() } } }
    }
}

@Composable
private fun InfoCard(p: PlayerOverlayParams, live: Boolean, modifier: Modifier) {
    val ch = p.currentChannel
    val title = if (live) ch?.name ?: p.currentChannelName ?: p.displayTitle else p.displayTitle
    val sub = if (live) p.currentProgram?.title else p.episodeLine
    val desc = p.currentProgram?.description?.takeIf { it.isNotBlank() }
    Row(
        modifier.padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(92.dp).height(92.dp).clip(CircleShape).background(Color(0xFF1A1424)).border(2.dp, Cyan, CircleShape), contentAlignment = Alignment.Center) {
            val img = ch?.logoUrl
            if (img != null) AsyncImage(img, null, contentScale = if (live) ContentScale.Fit else ContentScale.Crop, modifier = Modifier.fillMaxSize().padding(if (live) 8.dp else 0.dp))
            else Text(title.take(1), color = Cyan, fontSize = 34.sp, fontWeight = FontWeight.Black)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = Color.White, fontSize = 40.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, fontWeight = FontWeight.Light, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                if (live) LiveDot()
                qualityBadges(p.resolutionBadgeLabel ?: ch?.qualityBadge()).forEach { OutlineBadge(it) }
                Text("  Dolby Vision", color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
            sub?.takeIf { it.isNotBlank() }?.let { Text(it, color = Color.White.copy(alpha = 0.82f), fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
            desc?.let { Text(it, color = Grey, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 18.sp) }
        }
    }
}

private fun qualityBadges(label: String?): List<String> {
    val l = label?.uppercase().orEmpty()
    return when {
        "4K" in l || "2160" in l || "UHD" in l -> listOf("4K", "HDR")
        "1080" in l || "FHD" in l -> listOf("FHD")
        "HD" in l || "720" in l -> listOf("HD")
        else -> listOf("4K", "HDR")
    }
}

@Composable
private fun OutlineBadge(t: String) {
    Text(t, color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 8.dp).border(1.dp, Cyan.copy(alpha = 0.55f), RoundedCornerShape(5.dp)).padding(horizontal = 7.dp, vertical = 2.dp))
}

@Composable
private fun LiveDot() {
    Text("● " + tr("LIVE", "مباشر"), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold,
        modifier = Modifier.background(Color(0xFFE53935), RoundedCornerShape(5.dp)).padding(horizontal = 7.dp, vertical = 2.dp))
}

@Composable
private fun Track(frac: Float, focused: Boolean, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier.height(22.dp), contentAlignment = Alignment.CenterStart) {
        val h = if (focused) 6.dp else 4.dp
        Box(Modifier.fillMaxWidth().height(h).clip(CircleShape).background(Color.White.copy(alpha = 0.2f))) {
            Box(Modifier.fillMaxWidth(frac).fillMaxHeight().background(Brush.horizontalGradient(listOf(Color(0xFF6E3BD1), Cyan))))
        }
        val k = if (focused) 20.dp else 16.dp
        Box(Modifier.offset(x = (maxWidth - k) * frac).size(k).shadow(if (focused) 16.dp else 10.dp, CircleShape, ambientColor = Cyan, spotColor = Cyan).background(Cyan, CircleShape).border(2.dp, Color.White.copy(alpha = 0.85f), CircleShape))
    }
}

@Composable
private fun SeekBar(p: PlayerOverlayParams, seekBy: (Long) -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val pos = if (p.seekPreview.visible) p.seekPreview.positionMs else p.currentPosition
    val frac = if (p.duration > 0) (pos.toFloat() / p.duration).coerceIn(0f, 1f) else 0f
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(cgDuration(pos), color = Cyan, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        TvClickableSurface(
            onClick = { p.onUserInteraction(); p.onTogglePlayPause() },
            modifier = Modifier.weight(1f).onFocusChanged { focused = it.isFocused; p.onSetScrubbingMode(it.isFocused) }.onPreviewKeyEvent { e ->
                val n = e.nativeKeyEvent
                if (n.action != android.view.KeyEvent.ACTION_DOWN) return@onPreviewKeyEvent false
                when (n.keyCode) {
                    android.view.KeyEvent.KEYCODE_DPAD_LEFT -> { seekBy(-GSTEP); true }
                    android.view.KeyEvent.KEYCODE_DPAD_RIGHT -> { seekBy(GSTEP); true }
                    else -> false
                }
            },
            shape = ClickableSurfaceDefaults.shape(CircleShape),
            colors = ClickableSurfaceDefaults.colors(containerColor = Color.Transparent, focusedContainerColor = Color.Transparent),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1f)
        ) { Track(frac, focused, Modifier.fillMaxWidth().padding(horizontal = 4.dp)) }
        Text(cgDuration(p.duration), color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
    }
}

@Composable
private fun LiveProgress(p: PlayerOverlayParams) {
    val pr = p.currentProgram ?: return
    val now = System.currentTimeMillis()
    if (pr.endTime <= pr.startTime) return
    val frac = ((now - pr.startTime).toFloat() / (pr.endTime - pr.startTime)).coerceIn(0f, 1f)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(cgClock(pr.startTime), color = Cyan, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Track(frac, false, Modifier.weight(1f))
        Text(cgClock(pr.endTime), color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
    }
}

@Composable
private fun focusStyle(shape: androidx.compose.ui.graphics.Shape, scale: Float = 1.08f) = Triple(
    ClickableSurfaceDefaults.shape(shape),
    ClickableSurfaceDefaults.border(focusedBorder = Border(androidx.compose.foundation.BorderStroke(2.dp, Cyan), shape = shape)),
    ClickableSurfaceDefaults.glow(focusedGlow = Glow(Cyan.copy(alpha = 0.55f), 16.dp))
)

@Composable
private fun LabeledAction(icon: ImageVector, label: String, value: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    val (s, b, g) = focusStyle(shape)
    TvClickableSurface(onClick = onClick, shape = s, border = b, glow = g,
        colors = ClickableSurfaceDefaults.colors(containerColor = Color.Transparent, focusedContainerColor = Color(0x339B5CFF), contentColor = Color.White, focusedContentColor = Color.White),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f)) {
        Column(Modifier.width(104.dp).padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(26.dp))
            Text(label, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(value, color = Color(0xFFD6DEE6), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun RoundSeek(icon: ImageVector, onClick: () -> Unit) {
    val (s, b, g) = focusStyle(CircleShape)
    TvClickableSurface(onClick = onClick, shape = s, border = b, glow = g, modifier = Modifier.size(54.dp),
        colors = ClickableSurfaceDefaults.colors(containerColor = Color.Transparent, focusedContainerColor = Color(0x339B5CFF), contentColor = Color.White, focusedContentColor = Color.White),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.12f)) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(34.dp)) }
    }
}

@Composable
private fun BigPlay(playing: Boolean, modifier: Modifier, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    TvClickableSurface(onClick = onClick, modifier = modifier.size(84.dp).onFocusChanged { focused = it.isFocused },
        shape = ClickableSurfaceDefaults.shape(CircleShape),
        border = ClickableSurfaceDefaults.border(
            border = Border(androidx.compose.foundation.BorderStroke(3.dp, Cyan), shape = CircleShape),
            focusedBorder = Border(androidx.compose.foundation.BorderStroke(4.dp, Cyan), shape = CircleShape)),
        glow = ClickableSurfaceDefaults.glow(glow = Glow(Cyan.copy(alpha = 0.45f), 14.dp), focusedGlow = Glow(Cyan.copy(alpha = 0.8f), 24.dp)),
        colors = ClickableSurfaceDefaults.colors(containerColor = Color(0x660A1438), focusedContainerColor = Color(0x449B5CFF), contentColor = Color.White, focusedContentColor = Color.White),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f)) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.size(44.dp))
        }
    }
}

@Composable
private fun CyanPill(icon: ImageVector, label: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    var pf by remember { mutableStateOf(false) }
    val fg = Color.White
    TvClickableSurface(onClick = onClick, modifier = Modifier.onFocusChanged { pf = it.isFocused }, shape = ClickableSurfaceDefaults.shape(shape),
        border = ClickableSurfaceDefaults.border(
            border = Border(androidx.compose.foundation.BorderStroke(1.5.dp, Cyan), shape = shape),
            focusedBorder = Border(androidx.compose.foundation.BorderStroke(2.5.dp, Cyan), shape = shape)),
        glow = ClickableSurfaceDefaults.glow(focusedGlow = Glow(Cyan.copy(alpha = 0.6f), 16.dp)),
        colors = ClickableSurfaceDefaults.colors(containerColor = Color(0x269B5CFF), focusedContainerColor = Cyan, contentColor = Color.White, focusedContentColor = Color(0xFFFFFFFF)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.06f)) {
        Row(Modifier.padding(horizontal = 24.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(icon, null, tint = fg, modifier = Modifier.size(22.dp))
            Text(label, color = fg, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun GlassIconButton(icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    val (s, b, g) = focusStyle(CircleShape)
    TvClickableSurface(onClick = onClick, shape = s, border = b, glow = g, modifier = modifier.size(40.dp),
        colors = ClickableSurfaceDefaults.colors(containerColor = Color.Transparent, focusedContainerColor = Color(0x339B5CFF), contentColor = Color.White, focusedContentColor = Color.White),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f)) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(26.dp)) }
    }
}
