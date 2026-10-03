package com.streamvault.app.ui.themes.cardstack

import com.streamvault.app.ui.themes.bespoke.onDpadToward
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.layout.offset
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

/** Card Stack: layered plum "deck" look. Every focusable sits on two offset ghost cards (a stack),
 *  22dp corners, hot-pink accent, focus lifts the top card and fans the stack out. */
internal object CS {
    val Bg = Color(0xFF141116)
    val Raised = Color(0xFF221A25)
    val Card = Color(0xFF2B2030)
    val Line = Color(0xFF44314A)
    val Text = Color(0xFFFFF4FD)
    val Sub = Color(0xFFCBB7C7)
    val Faint = Color(0xFF8E7A8B)
    val Amber = Color(0xFFFF6AAE)      // accent (hot pink)
    val AmberDeep = Color(0xFFB8327A)
    val Live = Color(0xFFFF5470)
    val Blue = Color(0xFF8FD3FF)
    val R = RoundedCornerShape(22.dp)
    val RSmall = RoundedCornerShape(14.dp)
    val Pill = RoundedCornerShape(50)
}

@Composable
internal fun CsBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0xFF3A1F3A), CS.Bg, Color(0xFF0C0A0E)), radius = 1800f)), content = content)
}

/** Stacked card: two ghost layers peek out under the bottom edge; on focus they fan out and the top card gets a pink outline. */
@Composable
internal fun CsCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = CS.R,
    container: Color = CS.Card,
    focusedContainer: Color = Color(0xFF3A2A40),
    onLongClick: (() -> Unit)? = null,
    zoom: Float = 1.04f,
    content: @Composable BoxScope.() -> Unit
) {
    val focusedState = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val focused = focusedState.value
    val fan = if (focused) 8.dp else 4.dp
    Box(modifier.onFocusChanged { focusedState.value = it.hasFocus }, propagateMinConstraints = true) {
        if (shape != CS.Pill) {
            Box(Modifier.matchParentSize().padding(horizontal = 14.dp).offset(y = fan * 2).clip(shape).background(CS.Amber.copy(alpha = if (focused) 0.22f else 0.06f)))
            Box(Modifier.matchParentSize().padding(horizontal = 7.dp).offset(y = fan).clip(shape).background(CS.Line.copy(alpha = if (focused) 0.9f else 0.45f)))
        }
        TvClickableSurface(
            onClick = onClick, onLongClick = onLongClick, modifier = Modifier,
            shape = ClickableSurfaceDefaults.shape(shape),
            colors = ClickableSurfaceDefaults.colors(containerColor = container, focusedContainerColor = focusedContainer, contentColor = CS.Text, focusedContentColor = CS.Text),
            border = ClickableSurfaceDefaults.border(focusedBorder = Border(androidx.compose.foundation.BorderStroke(2.dp, CS.Amber), shape = shape)),
            scale = ClickableSurfaceDefaults.scale(focusedScale = zoom),
            glow = ClickableSurfaceDefaults.glow(),
            content = content
        )
    }
}

/** Solid pill button. Primary = amber fill; secondary = translucent white that turns white on focus. */
@Composable
internal fun CsButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, primary: Boolean = false, icon: String? = null) {
    TvClickableSurface(
        onClick = onClick, modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(CS.Pill),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (primary) CS.Amber else Color.White.copy(alpha = 0.12f),
            focusedContainerColor = if (primary) Color(0xFFFFCB78) else CS.Text,
            contentColor = if (primary) CS.Bg else CS.Text, focusedContentColor = CS.Bg
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
        glow = ClickableSurfaceDefaults.glow()
    ) {
        Row(Modifier.padding(horizontal = 22.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            icon?.let { Text(it, fontSize = 15.sp, fontWeight = FontWeight.Bold) }
            Text(label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
    }
}

/** Round icon button used by the players. */
@Composable
internal fun CsRound(glyph: String, onClick: () -> Unit, modifier: Modifier = Modifier, size: Dp = 56.dp, active: Boolean = false) {
    TvClickableSurface(
        onClick = onClick, modifier = modifier.size(size),
        shape = ClickableSurfaceDefaults.shape(CircleShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (active) CS.Amber else Color.White.copy(alpha = 0.14f), focusedContainerColor = CS.Text,
            contentColor = if (active) CS.Bg else CS.Text, focusedContentColor = CS.Bg
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.12f),
        glow = ClickableSurfaceDefaults.glow()
    ) { Text(glyph, fontSize = (size.value / 2.6f).sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center)) }
}

@Composable
internal fun CsTab(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TvClickableSurface(
        onClick = onClick, modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(CS.Pill),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) Color.White.copy(alpha = 0.16f) else Color.Transparent, focusedContainerColor = CS.Text,
            contentColor = if (selected) CS.Text else CS.Sub, focusedContentColor = CS.Bg
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.04f),
        glow = ClickableSurfaceDefaults.glow()
    ) { Text(label, fontSize = 14.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, maxLines = 1, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) }
}

@Composable
internal fun CsRowTitle(text: String, modifier: Modifier = Modifier, trailing: String? = null) {
    Row(modifier.padding(bottom = 10.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(text, color = CS.Text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        trailing?.let { Text(it, color = CS.Faint, fontSize = 13.sp) }
    }
}

/** Thin rounded progress line with amber fill. */
@Composable
internal fun CsProgress(progress: Float, modifier: Modifier = Modifier, height: Dp = 4.dp, track: Color = Color.White.copy(alpha = 0.2f)) {
    Box(modifier.fillMaxWidth().height(height).clip(CS.Pill).background(track)) {
        Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).height(height).clip(CS.Pill).background(CS.Amber))
    }
}

/** Portrait poster with rounded corners; title below the art, never on top of it. */
@Composable
internal fun CsPoster(title: String, imageUrl: String?, caption: String?, onClick: () -> Unit, modifier: Modifier = Modifier, locked: Boolean = false, onLongClick: (() -> Unit)? = null) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        CsCard(onClick = onClick, onLongClick = onLongClick, modifier = Modifier.fillMaxWidth().aspectRatio(2f / 3f)) {
            if (!locked && imageUrl != null) AsyncImage(imageUrl, title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            else Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(CS.Line, CS.Raised))), contentAlignment = Alignment.Center) {
                Text(if (locked) "🔒" else title.take(1).uppercase(), color = CS.Sub, fontSize = 34.sp, fontWeight = FontWeight.Black)
            }
        }
        Text(title, color = CS.Text, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        caption?.takeIf { it.isNotBlank() }?.let { Text(it, color = CS.Faint, fontSize = 11.sp, maxLines = 1) }
    }
}

/** Wide 16:9 landscape card with a progress line pinned to the bottom edge. */
@Composable
internal fun CsWide(title: String, imageUrl: String?, sub: String?, progress: Float?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        CsCard(onClick = onClick, modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
            imageUrl?.let { AsyncImage(it, title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)))))
            Text("▶", color = CS.Text, fontSize = 22.sp, modifier = Modifier.align(Alignment.Center))
            progress?.let { CsProgress(it, Modifier.align(Alignment.BottomCenter).padding(10.dp), 4.dp) }
        }
        Text(title, color = CS.Text, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        sub?.let { Text(it, color = CS.Faint, fontSize = 11.sp, maxLines = 1) }
    }
}

/** Circular channel logo on a white disc, the way streaming apps show networks. */
@Composable
internal fun CsLogo(name: String, logoUrl: String?, size: Dp, modifier: Modifier = Modifier) {
    Box(modifier.size(size).clip(CS.RSmall).background(Color(0xFF2A2D35)), contentAlignment = Alignment.Center) {
        Text(name.take(2).uppercase(), color = CS.Sub, fontSize = (size.value / 3.2f).sp, fontWeight = FontWeight.Bold)
        logoUrl?.let { AsyncImage(it, name, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(size / 8)) }
    }
}

@Composable
internal fun CsBadge(text: String, color: Color = CS.Amber, filled: Boolean = false) {
    Text(
        text, color = if (filled) CS.Bg else color, fontSize = 10.sp, fontWeight = FontWeight.Black, maxLines = 1,
        modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(if (filled) color else color.copy(alpha = 0.16f)).padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
internal fun CsEmpty(text: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(60.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("◌", color = CS.Faint, fontSize = 44.sp)
        Text(text, color = CS.Sub, fontSize = 16.sp)
    }
}

internal fun csClock(ms: Long): String {
    val c = java.util.Calendar.getInstance().apply { timeInMillis = ms }
    return "%02d:%02d".format(c.get(java.util.Calendar.HOUR_OF_DAY), c.get(java.util.Calendar.MINUTE))
}

internal fun csDuration(ms: Long): String {
    val t = (ms / 1000L).coerceAtLeast(0L)
    val h = t / 3600; val m = (t % 3600) / 60; val s = t % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}

internal fun Modifier.csKey(code: Int, handler: () -> Boolean): Modifier = this.then(
    Modifier.onPreviewKeyEvent { e -> e.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN && e.nativeKeyEvent.keyCode == code && handler() }
)
internal fun Modifier.csRight(h: () -> Boolean): Modifier = onDpadToward(end = true, h)
internal fun Modifier.csLeft(h: () -> Boolean): Modifier = onDpadToward(end = false, h)
