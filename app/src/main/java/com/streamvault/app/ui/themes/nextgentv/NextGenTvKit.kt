package com.streamvault.app.ui.themes.nextgentv

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
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

/** NextGenTv: a spatial-computing look. Deep space navy, layered "floating" panes that sit on a visible depth
 *  plate, cyan focus halo + lift, slight perspective tilt on side panes, 26dp radii, thin light top edges. */
internal object NG {
    val Bg = Color(0xFF0B0D13)
    val Raised = Color(0xFF151824)
    val Card = Color(0xFF1C2132)
    val Line = Color(0xFF273254)
    val Text = Color(0xFFF1F5FF)
    val Sub = Color(0xFFAEB9D1)
    val Faint = Color(0xFF6D7894)
    val Amber = Color(0xFF61C4FF)      // cyan accent (name kept for shared call sites)
    val AmberDeep = Color(0xFF3A7BFF)
    val Violet = Color(0xFF9B7BFF)
    val Live = Color(0xFFFF5A7A)
    val Blue = Color(0xFF7BE0C3)
    val R = RoundedCornerShape(26.dp)
    val RSmall = RoundedCornerShape(16.dp)
    val Pill = RoundedCornerShape(50)
}

/** Space backdrop: navy with two soft light sources (cyan top-start, violet bottom-end) and a horizon line. */
@Composable
internal fun NgBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier.fillMaxSize().background(NG.Bg)) {
        Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(NG.Amber.copy(alpha = 0.10f), Color.Transparent), center = androidx.compose.ui.geometry.Offset(0f, 0f), radius = 1400f)))
        Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(NG.Violet.copy(alpha = 0.10f), Color.Transparent), center = androidx.compose.ui.geometry.Offset(2400f, 1400f), radius = 1300f)))
        content()
    }
}

/** Perspective tilt for side panes (spatial depth). */
internal fun Modifier.ngTilt(degY: Float): Modifier = this.graphicsLayer { rotationY = degY; cameraDistance = 14f * density }

/** Floating pane: translucent navy, light top edge, sits on a darker depth plate offset below it. */
@Composable
internal fun NgPane(modifier: Modifier = Modifier, depth: Dp = 6.dp, content: @Composable BoxScope.() -> Unit) {
    Box(modifier) {
        Box(Modifier.matchParentSize().offset(y = depth).clip(NG.R).background(Color.Black.copy(alpha = 0.45f)))
        Box(
            Modifier.matchParentSize().clip(NG.R).background(Brush.verticalGradient(listOf(NG.Card.copy(alpha = 0.92f), NG.Raised.copy(alpha = 0.92f))))
                .border(1.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.22f), Color.Transparent)), NG.R)
        )
        Box(Modifier.padding(0.dp), content = content)
    }
}

/** Focusable card: cyan 2dp halo ring + lift to 1.08 on focus (the pane floats toward you). */
@Composable
internal fun NgCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = NG.R,
    container: Color = NG.Card,
    focusedContainer: Color = Color(0xFF243052),
    onLongClick: (() -> Unit)? = null,
    zoom: Float = 1.08f,
    content: @Composable BoxScope.() -> Unit
) {
    TvClickableSurface(
        onClick = onClick, onLongClick = onLongClick, modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = container, focusedContainerColor = focusedContainer, contentColor = NG.Text, focusedContentColor = NG.Text),
        border = ClickableSurfaceDefaults.border(
            border = Border(androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), shape = shape),
            focusedBorder = Border(androidx.compose.foundation.BorderStroke(2.dp, NG.Amber), shape = shape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = zoom),
        glow = ClickableSurfaceDefaults.glow(focusedGlow = androidx.tv.material3.Glow(NG.Amber.copy(alpha = 0.55f), 18.dp)),
        content = content
    )
}

/** Capsule button: primary = cyan-to-blue gradient look (solid cyan), secondary = frosted capsule; focus = white. */
@Composable
internal fun NgButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, primary: Boolean = false, icon: String? = null) {
    TvClickableSurface(
        onClick = onClick, modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(NG.RSmall),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (primary) NG.Amber else Color.White.copy(alpha = 0.08f),
            focusedContainerColor = NG.Text,
            contentColor = if (primary) NG.Bg else NG.Text, focusedContentColor = NG.Bg
        ),
        border = ClickableSurfaceDefaults.border(border = Border(androidx.compose.foundation.BorderStroke(1.dp, if (primary) NG.Amber else Color.White.copy(alpha = 0.16f)), shape = NG.RSmall)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.07f),
        glow = ClickableSurfaceDefaults.glow(focusedGlow = androidx.tv.material3.Glow(NG.Amber.copy(alpha = 0.5f), 14.dp))
    ) {
        Row(Modifier.padding(horizontal = 20.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            icon?.let { Text(it, fontSize = 15.sp, fontWeight = FontWeight.Bold) }
            Text(label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
    }
}

/** Squircle glyph tile used by the players (floating key, not a round orb). */
@Composable
internal fun NgRound(glyph: String, onClick: () -> Unit, modifier: Modifier = Modifier, size: Dp = 56.dp, active: Boolean = false) {
    val shape = RoundedCornerShape(size * 0.32f)
    TvClickableSurface(
        onClick = onClick, modifier = modifier.size(size),
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (active) NG.Amber else NG.Card.copy(alpha = 0.85f), focusedContainerColor = NG.Text,
            contentColor = if (active) NG.Bg else NG.Text, focusedContentColor = NG.Bg
        ),
        border = ClickableSurfaceDefaults.border(border = Border(androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.14f),
        glow = ClickableSurfaceDefaults.glow(focusedGlow = androidx.tv.material3.Glow(NG.Amber.copy(alpha = 0.6f), 16.dp))
    ) { Text(glyph, fontSize = (size.value / 2.6f).sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center)) }
}

/** Segment chip: selected gets a cyan dot + light fill. */
@Composable
internal fun NgTab(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TvClickableSurface(
        onClick = onClick, modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(NG.RSmall),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) NG.Amber.copy(alpha = 0.16f) else Color.Transparent, focusedContainerColor = NG.Text,
            contentColor = if (selected) NG.Amber else NG.Sub, focusedContentColor = NG.Bg
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
        glow = ClickableSurfaceDefaults.glow()
    ) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (selected) Box(Modifier.size(6.dp).clip(CircleShape).background(NG.Amber))
            Text(label, fontSize = 14.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, maxLines = 1)
        }
    }
}

/** Row title: small cyan index dot-line + bold title, light count. */
@Composable
internal fun NgRowTitle(text: String, modifier: Modifier = Modifier, trailing: String? = null) {
    Row(modifier.padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.width(18.dp).height(3.dp).clip(NG.Pill).background(Brush.horizontalGradient(listOf(NG.Amber, NG.Violet))))
        Text(text, color = NG.Text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        trailing?.let { Text(it, color = NG.Faint, fontSize = 13.sp) }
    }
}

/** Progress: light-beam line, cyan to violet, with a bright head dot. */
@Composable
internal fun NgProgress(progress: Float, modifier: Modifier = Modifier, height: Dp = 4.dp, track: Color = Color.White.copy(alpha = 0.12f)) {
    Box(modifier.fillMaxWidth().height(height).clip(NG.Pill).background(track)) {
        Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).height(height).clip(NG.Pill).background(Brush.horizontalGradient(listOf(NG.AmberDeep, NG.Amber))))
    }
}

/** Poster: floating card on a depth plate; title + caption below. */
@Composable
internal fun NgPoster(title: String, imageUrl: String?, caption: String?, onClick: () -> Unit, modifier: Modifier = Modifier, locked: Boolean = false, onLongClick: (() -> Unit)? = null) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box {
            Box(Modifier.matchParentSize().padding(horizontal = 8.dp).offset(y = 8.dp).clip(NG.R).background(NG.Amber.copy(alpha = 0.10f)))
            NgCard(onClick = onClick, onLongClick = onLongClick, modifier = Modifier.fillMaxWidth().aspectRatio(2f / 3f)) {
                if (!locked && imageUrl != null) AsyncImage(imageUrl, title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                else Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(NG.Line, NG.Raised))), contentAlignment = Alignment.Center) {
                    Text(if (locked) "🔒" else title.take(1).uppercase(), color = NG.Sub, fontSize = 34.sp, fontWeight = FontWeight.Black)
                }
            }
        }
        Text(title, color = NG.Text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        caption?.takeIf { it.isNotBlank() }?.let { Text(it, color = NG.Faint, fontSize = 11.sp, maxLines = 1) }
    }
}

/** Wide card: art with the title floating in a frosted capsule inside the bottom, beam progress on the edge. */
@Composable
internal fun NgWide(title: String, imageUrl: String?, sub: String?, progress: Float?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    NgCard(onClick = onClick, modifier = modifier.aspectRatio(16f / 9f)) {
        imageUrl?.let { AsyncImage(it, title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
        Column(Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(8.dp).clip(NG.RSmall).background(NG.Bg.copy(alpha = 0.78f)).padding(horizontal = 10.dp, vertical = 6.dp)) {
            Text(title, color = NG.Text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            sub?.let { Text(it, color = NG.Sub, fontSize = 11.sp, maxLines = 1) }
            progress?.let { NgProgress(it, Modifier.padding(top = 4.dp), 3.dp) }
        }
    }
}

/** Channel mark: squircle with a thin light edge. */
@Composable
internal fun NgLogo(name: String, logoUrl: String?, size: Dp, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(size * 0.3f)
    Box(modifier.size(size).clip(shape).background(Color(0xFF202842)).border(1.dp, Color.White.copy(alpha = 0.14f), shape), contentAlignment = Alignment.Center) {
        Text(name.take(2).uppercase(), color = NG.Sub, fontSize = (size.value / 3.2f).sp, fontWeight = FontWeight.Bold)
        logoUrl?.let { AsyncImage(it, name, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(size / 8)) }
    }
}

@Composable
internal fun NgBadge(text: String, color: Color = NG.Amber, filled: Boolean = false) {
    Text(
        text, color = if (filled) NG.Bg else color, fontSize = 10.sp, fontWeight = FontWeight.Black, maxLines = 1,
        modifier = Modifier.clip(NG.Pill).background(if (filled) color else color.copy(alpha = 0.14f)).border(1.dp, color.copy(alpha = 0.5f), NG.Pill).padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

@Composable
internal fun NgEmpty(text: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(60.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.size(56.dp).clip(CircleShape).border(2.dp, Brush.linearGradient(listOf(NG.Amber, NG.Violet)), CircleShape))
        Text(text, color = NG.Sub, fontSize = 16.sp)
    }
}

internal fun ngClock(ms: Long): String {
    val c = java.util.Calendar.getInstance().apply { timeInMillis = ms }
    return "%02d:%02d".format(c.get(java.util.Calendar.HOUR_OF_DAY), c.get(java.util.Calendar.MINUTE))
}

internal fun ngDuration(ms: Long): String {
    val t = (ms / 1000L).coerceAtLeast(0L)
    val h = t / 3600; val m = (t % 3600) / 60; val s = t % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}

internal fun Modifier.ngKey(code: Int, handler: () -> Boolean): Modifier = this.then(
    Modifier.onPreviewKeyEvent { e -> e.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN && e.nativeKeyEvent.keyCode == code && handler() }
)
internal fun Modifier.ngRight(h: () -> Boolean) = ngKey(android.view.KeyEvent.KEYCODE_DPAD_RIGHT, h)
internal fun Modifier.ngLeft(h: () -> Boolean) = ngKey(android.view.KeyEvent.KEYCODE_DPAD_LEFT, h)
