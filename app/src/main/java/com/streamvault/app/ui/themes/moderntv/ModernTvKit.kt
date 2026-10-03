package com.streamvault.app.ui.themes.moderntv

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

/** Modern TV: a premium streaming-service look. Warm charcoal, amber accent, soft 16dp cards,
 *  full-bleed artwork with cinematic gradients, white focus ring and gentle zoom. */
internal object MT {
    val Bg = Color(0xFF101114)
    val Raised = Color(0xFF1B1D23)
    val Card = Color(0xFF22252C)
    val Line = Color(0xFF30333C)
    val Text = Color(0xFFF8F7F4)
    val Sub = Color(0xFFB9BBC4)
    val Faint = Color(0xFF7C7F89)
    val Amber = Color(0xFFFFB74A)
    val AmberDeep = Color(0xFFE08A1E)
    val Live = Color(0xFFFF4D4D)
    val Blue = Color(0xFF6FA8FF)
    val R = RoundedCornerShape(16.dp)
    val RSmall = RoundedCornerShape(10.dp)
    val Pill = RoundedCornerShape(50)
}

@Composable
internal fun MtBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF15161A), MT.Bg, Color(0xFF0B0C0E)))), content = content)
}

/** Focusable card: white 3dp ring + 1.06 zoom on focus, no glow. */
@Composable
internal fun MtCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MT.R,
    container: Color = MT.Card,
    focusedContainer: Color = MT.Card,
    onLongClick: (() -> Unit)? = null,
    zoom: Float = 1.06f,
    content: @Composable BoxScope.() -> Unit
) {
    TvClickableSurface(
        onClick = onClick, onLongClick = onLongClick, modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = container, focusedContainerColor = focusedContainer, contentColor = MT.Text, focusedContentColor = MT.Text),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(androidx.compose.foundation.BorderStroke(3.dp, MT.Text), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = zoom),
        glow = ClickableSurfaceDefaults.glow(),
        content = content
    )
}

/** Solid pill button. Primary = amber fill; secondary = translucent white that turns white on focus. */
@Composable
internal fun MtButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, primary: Boolean = false, icon: String? = null) {
    TvClickableSurface(
        onClick = onClick, modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(MT.Pill),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (primary) MT.Amber else Color.White.copy(alpha = 0.12f),
            focusedContainerColor = if (primary) Color(0xFFFFCB78) else MT.Text,
            contentColor = if (primary) MT.Bg else MT.Text, focusedContentColor = MT.Bg
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
internal fun MtRound(glyph: String, onClick: () -> Unit, modifier: Modifier = Modifier, size: Dp = 56.dp, active: Boolean = false) {
    TvClickableSurface(
        onClick = onClick, modifier = modifier.size(size),
        shape = ClickableSurfaceDefaults.shape(CircleShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (active) MT.Amber else Color.White.copy(alpha = 0.14f), focusedContainerColor = MT.Text,
            contentColor = if (active) MT.Bg else MT.Text, focusedContentColor = MT.Bg
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.12f),
        glow = ClickableSurfaceDefaults.glow()
    ) { Text(glyph, fontSize = (size.value / 2.6f).sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center)) }
}

@Composable
internal fun MtTab(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TvClickableSurface(
        onClick = onClick, modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(MT.Pill),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) Color.White.copy(alpha = 0.16f) else Color.Transparent, focusedContainerColor = MT.Text,
            contentColor = if (selected) MT.Text else MT.Sub, focusedContentColor = MT.Bg
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.04f),
        glow = ClickableSurfaceDefaults.glow()
    ) { Text(label, fontSize = 14.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, maxLines = 1, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) }
}

@Composable
internal fun MtRowTitle(text: String, modifier: Modifier = Modifier, trailing: String? = null) {
    Row(modifier.padding(bottom = 10.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(text, color = MT.Text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        trailing?.let { Text(it, color = MT.Faint, fontSize = 13.sp) }
    }
}

/** Thin rounded progress line with amber fill. */
@Composable
internal fun MtProgress(progress: Float, modifier: Modifier = Modifier, height: Dp = 4.dp, track: Color = Color.White.copy(alpha = 0.2f)) {
    Box(modifier.fillMaxWidth().height(height).clip(MT.Pill).background(track)) {
        Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).height(height).clip(MT.Pill).background(MT.Amber))
    }
}

/** Portrait poster with rounded corners; title below the art, never on top of it. */
@Composable
internal fun MtPoster(title: String, imageUrl: String?, caption: String?, onClick: () -> Unit, modifier: Modifier = Modifier, locked: Boolean = false, onLongClick: (() -> Unit)? = null) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        MtCard(onClick = onClick, onLongClick = onLongClick, modifier = Modifier.fillMaxWidth().aspectRatio(2f / 3f)) {
            if (!locked && imageUrl != null) AsyncImage(imageUrl, title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            else Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(MT.Line, MT.Raised))), contentAlignment = Alignment.Center) {
                Text(if (locked) "🔒" else title.take(1).uppercase(), color = MT.Sub, fontSize = 34.sp, fontWeight = FontWeight.Black)
            }
        }
        Text(title, color = MT.Text, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        caption?.takeIf { it.isNotBlank() }?.let { Text(it, color = MT.Faint, fontSize = 11.sp, maxLines = 1) }
    }
}

/** Wide 16:9 landscape card with a progress line pinned to the bottom edge. */
@Composable
internal fun MtWide(title: String, imageUrl: String?, sub: String?, progress: Float?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        MtCard(onClick = onClick, modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
            imageUrl?.let { AsyncImage(it, title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)))))
            Text("▶", color = MT.Text, fontSize = 22.sp, modifier = Modifier.align(Alignment.Center))
            progress?.let { MtProgress(it, Modifier.align(Alignment.BottomCenter).padding(10.dp), 4.dp) }
        }
        Text(title, color = MT.Text, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        sub?.let { Text(it, color = MT.Faint, fontSize = 11.sp, maxLines = 1) }
    }
}

/** Circular channel logo on a white disc, the way streaming apps show networks. */
@Composable
internal fun MtLogo(name: String, logoUrl: String?, size: Dp, modifier: Modifier = Modifier) {
    Box(modifier.size(size).clip(MT.RSmall).background(Color(0xFF2A2D35)), contentAlignment = Alignment.Center) {
        Text(name.take(2).uppercase(), color = MT.Sub, fontSize = (size.value / 3.2f).sp, fontWeight = FontWeight.Bold)
        logoUrl?.let { AsyncImage(it, name, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(size / 8)) }
    }
}

@Composable
internal fun MtBadge(text: String, color: Color = MT.Amber, filled: Boolean = false) {
    Text(
        text, color = if (filled) MT.Bg else color, fontSize = 10.sp, fontWeight = FontWeight.Black, maxLines = 1,
        modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(if (filled) color else color.copy(alpha = 0.16f)).padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
internal fun MtEmpty(text: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(60.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("◌", color = MT.Faint, fontSize = 44.sp)
        Text(text, color = MT.Sub, fontSize = 16.sp)
    }
}

internal fun mtClock(ms: Long): String {
    val c = java.util.Calendar.getInstance().apply { timeInMillis = ms }
    return "%02d:%02d".format(c.get(java.util.Calendar.HOUR_OF_DAY), c.get(java.util.Calendar.MINUTE))
}

internal fun mtDuration(ms: Long): String {
    val t = (ms / 1000L).coerceAtLeast(0L)
    val h = t / 3600; val m = (t % 3600) / 60; val s = t % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}

internal fun Modifier.mtKey(code: Int, handler: () -> Boolean): Modifier = this.then(
    Modifier.onPreviewKeyEvent { e -> e.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN && e.nativeKeyEvent.keyCode == code && handler() }
)
internal fun Modifier.mtRight(h: () -> Boolean): Modifier = onDpadToward(end = true, h)
internal fun Modifier.mtLeft(h: () -> Boolean): Modifier = onDpadToward(end = false, h)
