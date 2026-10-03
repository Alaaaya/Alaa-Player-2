package com.streamvault.app.ui.themes.sportstv

import com.streamvault.app.ui.themes.bespoke.onDpadToward
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.layout.width
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

/** Sports TV: broadcast-graphics look. Deep navy, electric lime accent, slanted cut-corner plates
 *  (like TV score bugs), uppercase condensed labels, lime focus bar instead of zoom-heavy cards. */
internal object ST {
    val Bg = Color(0xFF0A1620)
    val Raised = Color(0xFF102736)
    val Card = Color(0xFF0D1E2A)
    val Line = Color(0xFF16465D)
    val Text = Color(0xFFF4FBFF)
    val Sub = Color(0xFFA9C6D3)
    val Faint = Color(0xFF5F8394)
    val Amber = Color(0xFF9FE231)      // lime (kept name for helper reuse)
    val AmberDeep = Color(0xFF6FAE12)
    val Live = Color(0xFFFF3B3B)
    val Blue = Color(0xFF2EC5FF)
    val R = CutCornerShape(topStart = 0.dp, topEnd = 14.dp, bottomEnd = 0.dp, bottomStart = 14.dp)
    val RSmall = CutCornerShape(topStart = 0.dp, topEnd = 8.dp, bottomEnd = 0.dp, bottomStart = 8.dp)
    val Pill = CutCornerShape(6.dp)
    val Plate = CutCornerShape(topEnd = 18.dp)
}

@Composable
internal fun StBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF0E2433), ST.Bg, Color(0xFF060E15)))), content = content)
}

/** Focusable plate: lime 3dp frame + small zoom; slanted corners. */
@Composable
internal fun StCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = ST.R,
    container: Color = ST.Card,
    focusedContainer: Color = ST.Raised,
    onLongClick: (() -> Unit)? = null,
    zoom: Float = 1.035f,
    content: @Composable BoxScope.() -> Unit
) {
    TvClickableSurface(
        onClick = onClick, onLongClick = onLongClick, modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = container, focusedContainerColor = focusedContainer, contentColor = ST.Text, focusedContentColor = ST.Text),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(androidx.compose.foundation.BorderStroke(3.dp, ST.Amber), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = zoom),
        glow = ClickableSurfaceDefaults.glow(),
        content = content
    )
}

/** Slanted plate button. Primary = lime fill with navy text; secondary = navy plate turning lime on focus. */
@Composable
internal fun StButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, primary: Boolean = false, icon: String? = null) {
    TvClickableSurface(
        onClick = onClick, modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(ST.RSmall),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (primary) ST.Amber else ST.Raised,
            focusedContainerColor = if (primary) Color(0xFFC2FF5C) else ST.Amber,
            contentColor = if (primary) ST.Bg else ST.Text, focusedContentColor = ST.Bg
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.04f),
        glow = ClickableSurfaceDefaults.glow()
    ) {
        Row(Modifier.padding(horizontal = 20.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            icon?.let { Text(it, fontSize = 15.sp, fontWeight = FontWeight.Bold) }
            Text(label.uppercase(), fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, maxLines = 1)
        }
    }
}

/** Square-ish cut button used by the players. */
@Composable
internal fun StRound(glyph: String, onClick: () -> Unit, modifier: Modifier = Modifier, size: Dp = 56.dp, active: Boolean = false) {
    TvClickableSurface(
        onClick = onClick, modifier = modifier.size(size),
        shape = ClickableSurfaceDefaults.shape(ST.Pill),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (active) ST.Amber else ST.Raised, focusedContainerColor = ST.Amber,
            contentColor = if (active) ST.Bg else ST.Text, focusedContentColor = ST.Bg
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
        glow = ClickableSurfaceDefaults.glow()
    ) { Text(glyph, fontSize = (size.value / 2.6f).sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center)) }
}

/** Tab with a lime underline bar when selected (broadcast menu style). */
@Composable
internal fun StTab(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TvClickableSurface(
        onClick = onClick, modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(ST.Pill),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent, focusedContainerColor = ST.Amber,
            contentColor = if (selected) ST.Amber else ST.Sub, focusedContentColor = ST.Bg
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.03f),
        glow = ClickableSurfaceDefaults.glow()
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label.uppercase(), fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 0.8.sp, maxLines = 1)
            Box(Modifier.padding(top = 3.dp).width(22.dp).height(3.dp).background(if (selected) ST.Amber else Color.Transparent))
        }
    }
}

/** Score-bug style header: lime slash + uppercase title. */
@Composable
internal fun StRowTitle(text: String, modifier: Modifier = Modifier, trailing: String? = null) {
    Row(modifier.padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.width(6.dp).height(20.dp).background(ST.Amber))
        Text(text.uppercase(), color = ST.Text, fontSize = 18.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        trailing?.let { Text(it, color = ST.Faint, fontSize = 13.sp) }
    }
}

/** Segmented "match clock" bar: square ends, lime fill. */
@Composable
internal fun StProgress(progress: Float, modifier: Modifier = Modifier, height: Dp = 4.dp, track: Color = Color.White.copy(alpha = 0.2f)) {
    Box(modifier.fillMaxWidth().height(height).background(track)) {
        Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).height(height).background(ST.Amber))
    }
}

/** Portrait poster plate: art fills the plate, a navy name strip slides across the bottom edge with a lime tick. */
@Composable
internal fun StPoster(title: String, imageUrl: String?, caption: String?, onClick: () -> Unit, modifier: Modifier = Modifier, locked: Boolean = false, onLongClick: (() -> Unit)? = null) {
    StCard(onClick = onClick, onLongClick = onLongClick, modifier = modifier.fillMaxWidth().aspectRatio(2f / 3f)) {
        if (!locked && imageUrl != null) AsyncImage(imageUrl, title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        else Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(ST.Line, ST.Raised))), contentAlignment = Alignment.Center) {
            Text(if (locked) "🔒" else title.take(1).uppercase(), color = ST.Sub, fontSize = 34.sp, fontWeight = FontWeight.Black)
        }
        Row(Modifier.align(Alignment.BottomStart).fillMaxWidth().background(ST.Bg.copy(alpha = 0.92f)), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(4.dp).height(34.dp).background(ST.Amber))
            Column(Modifier.padding(horizontal = 8.dp, vertical = 3.dp)) {
                Text(title, color = ST.Text, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                caption?.takeIf { it.isNotBlank() }?.let { Text(it.uppercase(), color = ST.Faint, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
            }
        }
    }
}

/** Wide "replay" plate: art left, text plate right with a match-clock bar. */
@Composable
internal fun StWide(title: String, imageUrl: String?, sub: String?, progress: Float?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    StCard(onClick = onClick, modifier = modifier.height(96.dp)) {
        Row(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxHeight().aspectRatio(16f / 9f).background(ST.Raised)) {
                imageUrl?.let { AsyncImage(it, title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                Text("▶", color = ST.Amber, fontSize = 20.sp, modifier = Modifier.align(Alignment.Center))
            }
            Column(Modifier.weight(1f).fillMaxHeight().padding(10.dp), verticalArrangement = Arrangement.SpaceBetween) {
                Text(title, color = ST.Text, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                sub?.let { Text(it.uppercase(), color = ST.Sub, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                progress?.let { StProgress(it, height = 3.dp) }
            }
        }
    }
}

/** Circular channel logo on a white disc, the way streaming apps show networks. */
@Composable
internal fun StLogo(name: String, logoUrl: String?, size: Dp, modifier: Modifier = Modifier) {
    Box(modifier.size(size).clip(ST.Pill).background(Color(0xFF17384A)), contentAlignment = Alignment.Center) {
        Text(name.take(2).uppercase(), color = ST.Sub, fontSize = (size.value / 3.2f).sp, fontWeight = FontWeight.Bold)
        logoUrl?.let { AsyncImage(it, name, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(size / 8)) }
    }
}

@Composable
internal fun StBadge(text: String, color: Color = ST.Amber, filled: Boolean = false) {
    Text(
        text, color = if (filled) ST.Bg else color, fontSize = 10.sp, fontWeight = FontWeight.Black, maxLines = 1,
        modifier = Modifier.clip(CutCornerShape(topEnd = 5.dp)).background(if (filled) color else color.copy(alpha = 0.16f)).padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
internal fun StEmpty(text: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(60.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("◌", color = ST.Faint, fontSize = 44.sp)
        Text(text, color = ST.Sub, fontSize = 16.sp)
    }
}

internal fun stClock(ms: Long): String {
    val c = java.util.Calendar.getInstance().apply { timeInMillis = ms }
    return "%02d:%02d".format(c.get(java.util.Calendar.HOUR_OF_DAY), c.get(java.util.Calendar.MINUTE))
}

internal fun stDuration(ms: Long): String {
    val t = (ms / 1000L).coerceAtLeast(0L)
    val h = t / 3600; val m = (t % 3600) / 60; val s = t % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}

internal fun Modifier.stKey(code: Int, handler: () -> Boolean): Modifier = this.then(
    Modifier.onPreviewKeyEvent { e -> e.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN && e.nativeKeyEvent.keyCode == code && handler() }
)
internal fun Modifier.stRight(h: () -> Boolean): Modifier = onDpadToward(end = true, h)
internal fun Modifier.stLeft(h: () -> Boolean): Modifier = onDpadToward(end = false, h)
