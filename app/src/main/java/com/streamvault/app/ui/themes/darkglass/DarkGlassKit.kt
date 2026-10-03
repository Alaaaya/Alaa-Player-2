package com.streamvault.app.ui.themes.darkglass

import com.streamvault.app.ui.themes.bespoke.onDpadToward
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxHeight
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

/** DarkGlass: frosted glass panes floating over a deep blue-black backdrop with violet/indigo aurora light.
 *  Panels are translucent white with a hairline rim; focus = violet glow rim + lifted glass, not a solid fill. */
internal object DG {
    val Bg = Color(0xFF080A12)
    val Raised = Color(0x1AFFFFFF)       // glass pane
    val Card = Color(0x14FFFFFF)         // glass card
    val Line = Color(0x2EFFFFFF)         // rim
    val Text = Color(0xFFF2F5FF)
    val Sub = Color(0xFFB0B8CF)
    val Faint = Color(0xFF6E7794)
    val Amber = Color(0xFFB69CFF)        // violet accent (kept name for shared helpers)
    val AmberDeep = Color(0xFF7B5CFF)
    val Live = Color(0xFFFF5C8A)
    val Blue = Color(0xFF6FD3FF)
    val Solid = Color(0xFF141827)
    val R = RoundedCornerShape(24.dp)
    val RSmall = RoundedCornerShape(14.dp)
    val Pill = RoundedCornerShape(50)
}

/** Backdrop: near-black with two soft aurora blobs (violet top-start, cyan bottom-end). */
@Composable
internal fun DgBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier.fillMaxSize().background(DG.Bg)) {
        Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(DG.AmberDeep.copy(alpha = 0.35f), Color.Transparent), center = androidx.compose.ui.geometry.Offset(200f, 120f), radius = 900f)))
        Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(DG.Blue.copy(alpha = 0.16f), Color.Transparent), center = androidx.compose.ui.geometry.Offset(1800f, 1000f), radius = 1000f)))
        content()
    }
}

/** Static frosted pane: translucent fill + diagonal sheen + hairline rim. */
internal fun Modifier.dgGlass(shape: Shape = DG.R, alpha: Float = 0.08f): Modifier = this
    .clip(shape)
    .background(Brush.linearGradient(listOf(Color.White.copy(alpha = alpha + 0.05f), Color.White.copy(alpha = alpha * 0.5f))))
    .border(1.dp, Brush.linearGradient(listOf(Color.White.copy(alpha = 0.32f), Color.White.copy(alpha = 0.06f))), shape)

/** Focusable glass card: on focus the pane brightens and gets a 2dp violet rim, small lift. */
@Composable
internal fun DgCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = DG.R,
    container: Color = DG.Card,
    focusedContainer: Color = Color(0x33B69CFF),
    onLongClick: (() -> Unit)? = null,
    zoom: Float = 1.04f,
    content: @Composable BoxScope.() -> Unit
) {
    TvClickableSurface(
        onClick = onClick, onLongClick = onLongClick, modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = container, focusedContainerColor = focusedContainer, contentColor = DG.Text, focusedContentColor = DG.Text),
        border = ClickableSurfaceDefaults.border(
            border = Border(androidx.compose.foundation.BorderStroke(1.dp, DG.Line), shape = shape),
            focusedBorder = Border(androidx.compose.foundation.BorderStroke(2.dp, DG.Amber), shape = shape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = zoom),
        glow = ClickableSurfaceDefaults.glow(focusedGlow = androidx.tv.material3.Glow(DG.AmberDeep.copy(alpha = 0.6f), 14.dp)),
        content = content
    )
}

/** Glass pill button. Primary = violet gradient glass; secondary = clear glass that lights violet on focus. */
@Composable
internal fun DgButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, primary: Boolean = false, icon: String? = null) {
    DgCard(onClick = onClick, modifier = modifier, shape = DG.Pill, zoom = 1.05f,
        container = if (primary) DG.AmberDeep.copy(alpha = 0.55f) else DG.Card, focusedContainer = if (primary) DG.AmberDeep else Color(0x40B69CFF)) {
        Row(Modifier.padding(horizontal = 22.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            icon?.let { Text(it, color = DG.Text, fontSize = 15.sp, fontWeight = FontWeight.Bold) }
            Text(label, color = DG.Text, fontSize = 15.sp, fontWeight = FontWeight.Medium, maxLines = 1)
        }
    }
}

/** Glass orb icon button used by the players. */
@Composable
internal fun DgRound(glyph: String, onClick: () -> Unit, modifier: Modifier = Modifier, size: Dp = 56.dp, active: Boolean = false) {
    DgCard(onClick = onClick, modifier = modifier.size(size), shape = CircleShape, zoom = 1.12f,
        container = if (active) DG.AmberDeep.copy(alpha = 0.55f) else DG.Raised, focusedContainer = Color(0x55B69CFF)) {
        Text(glyph, color = DG.Text, fontSize = (size.value / 2.6f).sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
internal fun DgTab(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    DgCard(onClick = onClick, modifier = modifier, shape = DG.Pill, zoom = 1.04f,
        container = if (selected) Color(0x33B69CFF) else Color.Transparent, focusedContainer = Color(0x40FFFFFF)) {
        Text(label, color = if (selected) DG.Text else DG.Sub, fontSize = 14.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, maxLines = 1, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
    }
}

/** Section title: thin light weight with a short violet glow line before it. */
@Composable
internal fun DgRowTitle(text: String, modifier: Modifier = Modifier, trailing: String? = null) {
    Row(modifier.padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.width(18.dp).height(2.dp).clip(DG.Pill).background(Brush.horizontalGradient(listOf(DG.Amber, DG.Blue))))
        Text(text, color = DG.Text, fontSize = 19.sp, fontWeight = FontWeight.Light, letterSpacing = 0.5.sp)
        trailing?.let { Text(it, color = DG.Faint, fontSize = 12.sp, modifier = Modifier.dgGlass(DG.Pill, 0.04f).padding(horizontal = 8.dp, vertical = 2.dp)) }
    }
}

/** Glowing gradient progress inside a glass track. */
@Composable
internal fun DgProgress(progress: Float, modifier: Modifier = Modifier, height: Dp = 4.dp, track: Color = Color.White.copy(alpha = 0.12f)) {
    Box(modifier.fillMaxWidth().height(height).clip(DG.Pill).background(track)) {
        Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).height(height).clip(DG.Pill).background(Brush.horizontalGradient(listOf(DG.AmberDeep, DG.Amber, DG.Blue))))
    }
}

/** Poster with a glass caption plate overlapping the bottom of the art. */
@Composable
internal fun DgPoster(title: String, imageUrl: String?, caption: String?, onClick: () -> Unit, modifier: Modifier = Modifier, locked: Boolean = false, onLongClick: (() -> Unit)? = null) {
    DgCard(onClick = onClick, onLongClick = onLongClick, modifier = modifier.aspectRatio(2f / 3f)) {
        if (!locked && imageUrl != null) AsyncImage(imageUrl, title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        else Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(DG.AmberDeep.copy(alpha = 0.3f), DG.Solid))), contentAlignment = Alignment.Center) {
            Text(if (locked) "🔒" else title.take(1).uppercase(), color = DG.Sub, fontSize = 34.sp, fontWeight = FontWeight.Thin)
        }
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(6.dp).clip(DG.RSmall).background(Color(0xB30E1120)).border(1.dp, DG.Line, DG.RSmall).padding(horizontal = 8.dp, vertical = 5.dp)) {
            Text(title, color = DG.Text, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            caption?.takeIf { it.isNotBlank() }?.let { Text(it, color = DG.Sub, fontSize = 10.sp, maxLines = 1) }
        }
    }
}

/** Wide card: art with glass bottom strip carrying title and glowing progress. */
@Composable
internal fun DgWide(title: String, imageUrl: String?, sub: String?, progress: Float?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    DgCard(onClick = onClick, modifier = modifier.aspectRatio(16f / 9f)) {
        imageUrl?.let { AsyncImage(it, title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Color(0xB30E1120)).padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = DG.Text, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            sub?.let { Text(it, color = DG.Sub, fontSize = 10.sp, maxLines = 1) }
            progress?.let { DgProgress(it, height = 3.dp) }
        }
    }
}

/** Channel logo in a glass orb. */
@Composable
internal fun DgLogo(name: String, logoUrl: String?, size: Dp, modifier: Modifier = Modifier) {
    Box(modifier.size(size).dgGlass(CircleShape, 0.07f), contentAlignment = Alignment.Center) {
        Text(name.take(2).uppercase(), color = DG.Sub, fontSize = (size.value / 3.2f).sp, fontWeight = FontWeight.Light)
        logoUrl?.let { AsyncImage(it, name, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(size / 6)) }
    }
}

/** Glass chip badge with a tinted rim. */
@Composable
internal fun DgBadge(text: String, color: Color = DG.Amber, filled: Boolean = false) {
    Text(
        text, color = if (filled) Color.White else color, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1,
        modifier = Modifier.clip(DG.Pill).background(if (filled) color.copy(alpha = 0.75f) else color.copy(alpha = 0.12f)).border(1.dp, color.copy(alpha = 0.5f), DG.Pill).padding(horizontal = 7.dp, vertical = 2.dp)
    )
}

@Composable
internal fun DgEmpty(text: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(60.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.size(64.dp).dgGlass(CircleShape), contentAlignment = Alignment.Center) { Text("◇", color = DG.Amber, fontSize = 26.sp) }
        Text(text, color = DG.Sub, fontSize = 16.sp, fontWeight = FontWeight.Light)
    }
}

internal fun dgClock(ms: Long): String {
    val c = java.util.Calendar.getInstance().apply { timeInMillis = ms }
    return "%02d:%02d".format(c.get(java.util.Calendar.HOUR_OF_DAY), c.get(java.util.Calendar.MINUTE))
}

internal fun dgDuration(ms: Long): String {
    val t = (ms / 1000L).coerceAtLeast(0L)
    val h = t / 3600; val m = (t % 3600) / 60; val s = t % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}

internal fun Modifier.dgKey(code: Int, handler: () -> Boolean): Modifier = this.then(
    Modifier.onPreviewKeyEvent { e -> e.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN && e.nativeKeyEvent.keyCode == code && handler() }
)
internal fun Modifier.dgRight(h: () -> Boolean): Modifier = onDpadToward(end = true, h)
internal fun Modifier.dgLeft(h: () -> Boolean): Modifier = onDpadToward(end = false, h)
