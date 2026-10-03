package com.streamvault.app.ui.themes.auroralounge

import com.streamvault.app.ui.themes.bespoke.onDpadToward
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.streamvault.app.ui.interaction.TvClickableSurface

/** Aurora Lounge: a late-night lounge. Midnight violet room, slow aurora ribbons of teal and rose in the sky,
 *  warm candle-gold accent, very soft 32dp pebbles, focus = a warm gold halo that blooms instead of a hard ring. */
internal object AL {
    val Bg = Color(0xFF0C0B19)
    val Raised = Color(0xFF19152C)
    val Card = Color(0xFF241E3D)
    val Line = Color(0xFF332854)
    val Text = Color(0xFFF8F3FF)
    val Sub = Color(0xFFC4B9E0)
    val Faint = Color(0xFF8A7FAD)
    val Amber = Color(0xFFE6AD68)
    val AmberDeep = Color(0xFFB97E3E)
    val Live = Color(0xFFFF6F91)
    val Blue = Color(0xFF7FE3D0)
    val Rose = Color(0xFFE48BD8)
    val R = RoundedCornerShape(32.dp)
    val RSmall = RoundedCornerShape(20.dp)
    val Pill = RoundedCornerShape(50)
}

/** Night room with two aurora ribbons drifting across the top and a warm floor glow. */
@Composable
internal fun AlBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier.fillMaxSize().background(AL.Bg)) {
        Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(AL.Blue.copy(alpha = 0.16f), Color.Transparent), center = androidx.compose.ui.geometry.Offset(300f, -80f), radius = 900f)))
        Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(AL.Rose.copy(alpha = 0.13f), Color.Transparent), center = androidx.compose.ui.geometry.Offset(1500f, 40f), radius = 1000f)))
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, AL.Amber.copy(alpha = 0.05f)))))
        content()
    }
}

/** Focusable pebble: gold 2dp halo + soft gold glow + gentle 1.05 bloom. */
@Composable
internal fun AlCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = AL.R,
    container: Color = AL.Card,
    focusedContainer: Color = AL.Line,
    onLongClick: (() -> Unit)? = null,
    zoom: Float = 1.05f,
    content: @Composable BoxScope.() -> Unit
) {
    TvClickableSurface(
        onClick = onClick, onLongClick = onLongClick, modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = container, focusedContainerColor = focusedContainer, contentColor = AL.Text, focusedContentColor = AL.Text),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(androidx.compose.foundation.BorderStroke(2.dp, AL.Amber), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = zoom),
        glow = ClickableSurfaceDefaults.glow(focusedGlow = androidx.tv.material3.Glow(AL.Amber.copy(alpha = 0.55f), 18.dp)),
        content = content
    )
}

/** Lounge button: primary = candle gold, secondary = smoked violet glass that warms to gold on focus. */
@Composable
internal fun AlButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, primary: Boolean = false, icon: String? = null) {
    TvClickableSurface(
        onClick = onClick, modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(AL.Pill),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (primary) AL.Amber else AL.Line.copy(alpha = 0.7f),
            focusedContainerColor = if (primary) Color(0xFFF4C88E) else AL.Amber,
            contentColor = if (primary) AL.Bg else AL.Text, focusedContentColor = AL.Bg
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.06f),
        glow = ClickableSurfaceDefaults.glow(focusedGlow = androidx.tv.material3.Glow(AL.Amber.copy(alpha = 0.5f), 14.dp))
    ) {
        Row(Modifier.padding(horizontal = 26.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            icon?.let { Text(it, fontSize = 15.sp, fontWeight = FontWeight.Bold) }
            Text(label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, letterSpacing = 0.4.sp)
        }
    }
}

/** Round "moon" button used by the players: violet glass orb, gold when on. */
@Composable
internal fun AlRound(glyph: String, onClick: () -> Unit, modifier: Modifier = Modifier, size: Dp = 56.dp, active: Boolean = false) {
    TvClickableSurface(
        onClick = onClick, modifier = modifier.size(size),
        shape = ClickableSurfaceDefaults.shape(CircleShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (active) AL.Amber else AL.Line.copy(alpha = 0.75f), focusedContainerColor = AL.Amber,
            contentColor = if (active) AL.Bg else AL.Text, focusedContentColor = AL.Bg
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.14f),
        glow = ClickableSurfaceDefaults.glow(focusedGlow = androidx.tv.material3.Glow(AL.Amber.copy(alpha = 0.6f), 16.dp))
    ) { Text(glyph, fontSize = (size.value / 2.6f).sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center)) }
}

/** Tab = a soft lamp: selected shows a gold underglow dot, focus warms the whole pill. */
@Composable
internal fun AlTab(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TvClickableSurface(
        onClick = onClick, modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(AL.Pill),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) AL.Line else Color.Transparent, focusedContainerColor = AL.Amber,
            contentColor = if (selected) AL.Amber else AL.Sub, focusedContentColor = AL.Bg
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
        glow = ClickableSurfaceDefaults.glow()
    ) {
        Row(Modifier.padding(horizontal = 18.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (selected) Box(Modifier.size(6.dp).clip(CircleShape).background(AL.Amber))
            Text(label, fontSize = 14.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, maxLines = 1)
        }
    }
}

/** Section title: light italic serif label with a fading gold hairline after it. */
@Composable
internal fun AlRowTitle(text: String, modifier: Modifier = Modifier, trailing: String? = null) {
    Row(modifier.padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text, color = AL.Text, fontSize = 21.sp, fontWeight = FontWeight.Light, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif)
        trailing?.let { Text(it, color = AL.Amber, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
        Box(Modifier.width(120.dp).height(1.dp).background(Brush.horizontalGradient(listOf(AL.Amber.copy(alpha = 0.6f), Color.Transparent))))
    }
}

/** Thin rounded progress line with amber fill. */
@Composable
internal fun AlProgress(progress: Float, modifier: Modifier = Modifier, height: Dp = 4.dp, track: Color = AL.Line) {
    Box(modifier.fillMaxWidth().height(height).clip(AL.Pill).background(track)) {
        Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).height(height).clip(AL.Pill).background(Brush.horizontalGradient(listOf(AL.Rose, AL.Amber))))
    }
}

/** Portrait pebble poster: 20dp corners, aurora-tinted fallback, title centred below like a lounge menu card. */
@Composable
internal fun AlPoster(title: String, imageUrl: String?, caption: String?, onClick: () -> Unit, modifier: Modifier = Modifier, locked: Boolean = false, onLongClick: (() -> Unit)? = null) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        AlCard(onClick = onClick, onLongClick = onLongClick, shape = AL.RSmall, modifier = Modifier.fillMaxWidth().aspectRatio(2f / 3f)) {
            if (!locked && imageUrl != null) AsyncImage(imageUrl, title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            else Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(AL.Rose.copy(alpha = 0.35f), AL.Line, AL.Blue.copy(alpha = 0.25f)))), contentAlignment = Alignment.Center) {
                Text(if (locked) "🔒" else title.take(1).uppercase(), color = AL.Sub, fontSize = 34.sp, fontWeight = FontWeight.Black)
            }
        }
        Text(title, color = AL.Text, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth())
        caption?.takeIf { it.isNotBlank() }?.let { Text(it, color = AL.Amber.copy(alpha = 0.8f), fontSize = 11.sp, maxLines = 1, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
    }
}

/** Wide 16:9 landscape card with a progress line pinned to the bottom edge. */
@Composable
internal fun AlWide(title: String, imageUrl: String?, sub: String?, progress: Float?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        AlCard(onClick = onClick, shape = AL.RSmall, modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
            imageUrl?.let { AsyncImage(it, title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, AL.Bg.copy(alpha = 0.85f)))))
            Box(Modifier.align(Alignment.Center).size(44.dp).clip(CircleShape).background(AL.Amber.copy(alpha = 0.9f)), contentAlignment = Alignment.Center) { Text("▶", color = AL.Bg, fontSize = 18.sp) }
            progress?.let { AlProgress(it, Modifier.align(Alignment.BottomCenter).padding(10.dp), 4.dp) }
        }
        Text(title, color = AL.Text, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        sub?.let { Text(it, color = AL.Faint, fontSize = 11.sp, maxLines = 1) }
    }
}

/** Channel "moon": logo inside a circle with a faint gold rim, the lounge's signature channel mark. */
@Composable
internal fun AlLogo(name: String, logoUrl: String?, size: Dp, modifier: Modifier = Modifier) {
    Box(modifier.size(size).clip(CircleShape).background(Brush.radialGradient(listOf(AL.Line, AL.Raised))).border(1.dp, AL.Amber.copy(alpha = 0.35f), CircleShape), contentAlignment = Alignment.Center) {
        Text(name.take(2).uppercase(), color = AL.Sub, fontSize = (size.value / 3.2f).sp, fontWeight = FontWeight.Bold)
        logoUrl?.let { AsyncImage(it, name, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(size / 5)) }
    }
}

@Composable
internal fun AlBadge(text: String, color: Color = AL.Amber, filled: Boolean = false) {
    Text(
        text, color = if (filled) AL.Bg else color, fontSize = 10.sp, fontWeight = FontWeight.Black, maxLines = 1,
        modifier = Modifier.clip(AL.Pill).background(if (filled) color else color.copy(alpha = 0.16f)).padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

@Composable
internal fun AlEmpty(text: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(60.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("☾", color = AL.Amber, fontSize = 44.sp)
        Text(text, color = AL.Sub, fontSize = 16.sp)
    }
}

internal fun alClock(ms: Long): String {
    val c = java.util.Calendar.getInstance().apply { timeInMillis = ms }
    return "%02d:%02d".format(c.get(java.util.Calendar.HOUR_OF_DAY), c.get(java.util.Calendar.MINUTE))
}

internal fun alDuration(ms: Long): String {
    val t = (ms / 1000L).coerceAtLeast(0L)
    val h = t / 3600; val m = (t % 3600) / 60; val s = t % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}

internal fun Modifier.alKey(code: Int, handler: () -> Boolean): Modifier = this.then(
    Modifier.onPreviewKeyEvent { e -> e.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN && e.nativeKeyEvent.keyCode == code && handler() }
)
internal fun Modifier.alRight(h: () -> Boolean): Modifier = onDpadToward(end = true, h)
internal fun Modifier.alLeft(h: () -> Boolean): Modifier = onDpadToward(end = false, h)
