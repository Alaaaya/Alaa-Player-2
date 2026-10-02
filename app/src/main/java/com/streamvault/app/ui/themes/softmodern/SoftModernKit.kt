package com.streamvault.app.ui.themes.softmodern

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
import androidx.compose.foundation.layout.width
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

/** SoftModern: a calm, light "paper" look. Warm cream canvas, white pebble cards with 20dp corners,
 *  sage panels, deep-green accent. Focus = green 3dp ring + soft lift; nothing glows, nothing is dark. */
internal object SM {
    val Bg = Color(0xFFF1EEE9)
    val Raised = Color(0xFFFAF7F2)
    val Card = Color(0xFFFFFFFF)
    val Sage = Color(0xFFD9E7DA)
    val Line = Color(0xFFDCD6CC)
    val Text = Color(0xFF26342A)
    val Sub = Color(0xFF5A665D)
    val Faint = Color(0xFF8E978F)
    val Amber = Color(0xFF3D8660)
    val AmberDeep = Color(0xFF2C6A4A)
    val Live = Color(0xFFD9534F)
    val Blue = Color(0xFF4F7FA8)
    val R = RoundedCornerShape(20.dp)
    val RSmall = RoundedCornerShape(14.dp)
    val Pill = RoundedCornerShape(50)
}

@Composable
internal fun SmBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFF6F3EE), SM.Bg, Color(0xFFEAE5DD)))), content = content)
}

/** Focusable pebble: green 3dp ring + gentle 1.04 lift, container brightens to white. */
@Composable
internal fun SmCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = SM.R,
    container: Color = SM.Card,
    focusedContainer: Color = SM.Card,
    onLongClick: (() -> Unit)? = null,
    zoom: Float = 1.04f,
    content: @Composable BoxScope.() -> Unit
) {
    TvClickableSurface(
        onClick = onClick, onLongClick = onLongClick, modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = container, focusedContainerColor = focusedContainer, contentColor = SM.Text, focusedContentColor = SM.Text),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(androidx.compose.foundation.BorderStroke(3.dp, SM.Amber), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = zoom),
        glow = ClickableSurfaceDefaults.glow(),
        content = content
    )
}

/** Solid pill button. Primary = amber fill; secondary = translucent white that turns white on focus. */
@Composable
internal fun SmButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, primary: Boolean = false, icon: String? = null) {
    TvClickableSurface(
        onClick = onClick, modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(SM.Pill),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (primary) SM.Amber else SM.Card,
            focusedContainerColor = if (primary) SM.AmberDeep else SM.Sage,
            contentColor = if (primary) SM.Card else SM.Text, focusedContentColor = if (primary) SM.Card else SM.Text
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
internal fun SmRound(glyph: String, onClick: () -> Unit, modifier: Modifier = Modifier, size: Dp = 56.dp, active: Boolean = false) {
    TvClickableSurface(
        onClick = onClick, modifier = modifier.size(size),
        shape = ClickableSurfaceDefaults.shape(CircleShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (active) SM.Amber else SM.Card, focusedContainerColor = SM.Sage,
            contentColor = if (active) SM.Card else SM.Text, focusedContentColor = SM.Text
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.12f),
        glow = ClickableSurfaceDefaults.glow()
    ) { Text(glyph, fontSize = (size.value / 2.6f).sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center)) }
}

@Composable
internal fun SmTab(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TvClickableSurface(
        onClick = onClick, modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(SM.Pill),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) SM.Amber else SM.Card.copy(alpha = 0.6f), focusedContainerColor = SM.Sage,
            contentColor = if (selected) SM.Card else SM.Sub, focusedContentColor = SM.Text
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.04f),
        glow = ClickableSurfaceDefaults.glow()
    ) { Text(label, fontSize = 14.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, maxLines = 1, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) }
}

@Composable
internal fun SmRowTitle(text: String, modifier: Modifier = Modifier, trailing: String? = null) {
    Row(modifier.padding(bottom = 10.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(text, color = SM.Text, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        trailing?.let { Text(it, color = SM.Faint, fontSize = 13.sp) }
    }
}

/** Thin rounded progress line with amber fill. */
@Composable
internal fun SmProgress(progress: Float, modifier: Modifier = Modifier, height: Dp = 4.dp, track: Color = SM.Line) {
    Box(modifier.fillMaxWidth().height(height).clip(SM.Pill).background(track)) {
        Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).height(height).clip(SM.Pill).background(SM.Amber))
    }
}

/** Portrait pebble: art on top, title + caption inside the same white card (label pocket). */
@Composable
internal fun SmPoster(title: String, imageUrl: String?, caption: String?, onClick: () -> Unit, modifier: Modifier = Modifier, locked: Boolean = false, onLongClick: (() -> Unit)? = null) {
    SmCard(onClick = onClick, onLongClick = onLongClick, modifier = modifier) {
        Column {
            Box(Modifier.fillMaxWidth().aspectRatio(2f / 3f).padding(6.dp).clip(SM.RSmall).background(SM.Sage)) {
                if (!locked && imageUrl != null) AsyncImage(imageUrl, title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                else Text(if (locked) "🔒" else title.take(1).uppercase(), color = SM.AmberDeep, fontSize = 34.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.Center))
            }
            Column(Modifier.padding(start = 12.dp, end = 12.dp, bottom = 10.dp)) {
                Text(title, color = SM.Text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(caption?.takeIf { it.isNotBlank() } ?: " ", color = SM.Faint, fontSize = 11.sp, maxLines = 1)
            }
        }
    }
}

/** Landscape pebble: art left, text + progress right, all in one white card. */
@Composable
internal fun SmWide(title: String, imageUrl: String?, sub: String?, progress: Float?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    SmCard(onClick = onClick, modifier = modifier) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(120.dp).aspectRatio(16f / 10f).clip(SM.RSmall).background(SM.Sage)) {
                imageUrl?.let { AsyncImage(it, title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
            }
            Column(Modifier.padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(title, color = SM.Text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                sub?.let { Text(it, color = SM.Faint, fontSize = 11.sp, maxLines = 1) }
                progress?.let { SmProgress(it, height = 5.dp) }
            }
        }
    }
}

/** Circular channel logo on a white disc, the way streaming apps show networks. */
@Composable
internal fun SmLogo(name: String, logoUrl: String?, size: Dp, modifier: Modifier = Modifier) {
    Box(modifier.size(size).clip(SM.RSmall).background(SM.Card), contentAlignment = Alignment.Center) {
        Text(name.take(2).uppercase(), color = SM.Sub, fontSize = (size.value / 3.2f).sp, fontWeight = FontWeight.Bold)
        logoUrl?.let { AsyncImage(it, name, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(size / 8)) }
    }
}

@Composable
internal fun SmBadge(text: String, color: Color = SM.Amber, filled: Boolean = false) {
    Text(
        text, color = if (filled) SM.Card else color, fontSize = 10.sp, fontWeight = FontWeight.Black, maxLines = 1,
        modifier = Modifier.clip(SM.Pill).background(if (filled) color else color.copy(alpha = 0.14f)).padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Composable
internal fun SmEmpty(text: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(60.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("◌", color = SM.Faint, fontSize = 44.sp)
        Text(text, color = SM.Sub, fontSize = 16.sp)
    }
}

internal fun smClock(ms: Long): String {
    val c = java.util.Calendar.getInstance().apply { timeInMillis = ms }
    return "%02d:%02d".format(c.get(java.util.Calendar.HOUR_OF_DAY), c.get(java.util.Calendar.MINUTE))
}

internal fun smDuration(ms: Long): String {
    val t = (ms / 1000L).coerceAtLeast(0L)
    val h = t / 3600; val m = (t % 3600) / 60; val s = t % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}

internal fun Modifier.smKey(code: Int, handler: () -> Boolean): Modifier = this.then(
    Modifier.onPreviewKeyEvent { e -> e.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN && e.nativeKeyEvent.keyCode == code && handler() }
)
internal fun Modifier.smRight(h: () -> Boolean) = smKey(android.view.KeyEvent.KEYCODE_DPAD_RIGHT, h)
internal fun Modifier.smLeft(h: () -> Boolean) = smKey(android.view.KeyEvent.KEYCODE_DPAD_LEFT, h)
