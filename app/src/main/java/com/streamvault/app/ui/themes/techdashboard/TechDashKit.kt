package com.streamvault.app.ui.themes.techdashboard

import com.streamvault.app.ui.themes.bespoke.onDpadToward
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.draw.drawBehind
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
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
import kotlin.random.Random

/** Purple Galaxy: a deep-space observatory. Orbit rail, star-chart grids, nebula glass. */
/** Tech Dashboard: a mission-control terminal. Grid lines, mono read-outs, bracket frames, mint signal. */
internal object TD {
    val Void = Color(0xFF040A09)
    val Deep = Color(0xFF0A1714)
    val Nebula = Color(0xFF10251F)
    val Glass = Color(0x2248E0A4)
    val GlassStrong = Color(0x4448E0A4)
    val Star = Color(0xFFE7FFF7)
    val Dust = Color(0xFF8BBDB0)
    val Muted = Color(0xFF5A8378)
    val Plasma = Color(0xFF48E0A4)
    val Comet = Color(0xFF3FB8FF)
    val Flare = Color(0xFFFFC94A)
    val Live = Color(0xFFFF5A5A)
    val Pill = RoundedCornerShape(3.dp)
    val Panel = RoundedCornerShape(6.dp)
    val Card = RoundedCornerShape(4.dp)
    val Poster = RoundedCornerShape(2.dp)
    val Mono = FontFamily.Monospace
    const val BRAND = "▣ TVP//DASH"
    const val SECTOR = "MODULE"
}

/** Engineering grid with a slow scan line sweeping down the screen. */
@Composable
internal fun TechDashBackdrop(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val drift = rememberInfiniteTransition(label = "td-scan")
    val phase by drift.animateFloat(0f, 1f, infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Restart), label = "td-phase")
    Box(modifier.fillMaxSize().background(TD.Void)) {
        Canvas(Modifier.fillMaxSize()) {
            val step = 48.dp.toPx()
            var x = 0f
            while (x < size.width) { drawLine(TD.Plasma.copy(alpha = 0.05f), Offset(x, 0f), Offset(x, size.height)); x += step }
            var y = 0f
            while (y < size.height) { drawLine(TD.Plasma.copy(alpha = 0.05f), Offset(0f, y), Offset(size.width, y)); y += step }
            val sy = size.height * phase
            drawRect(Brush.verticalGradient(listOf(Color.Transparent, TD.Plasma.copy(alpha = 0.06f), Color.Transparent), startY = sy - 120f, endY = sy + 4f), topLeft = Offset(0f, sy - 120f), size = androidx.compose.ui.geometry.Size(size.width, 124f))
            drawRect(Brush.radialGradient(listOf(Color(0x3348E0A4), Color.Transparent), center = Offset(size.width * 0.9f, 0f), radius = size.minDimension * 0.8f))
        }
        content()
    }
}



/** Flat terminal cell: hairline border, solid mint frame + corner ticks on focus, almost no scale. */
@Composable
internal fun TechDashSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = TD.Card,
    container: Color = TD.Glass,
    focusedContainer: Color = TD.GlassStrong,
    onLongClick: (() -> Unit)? = null,
    scale: Float = 1.02f,
    content: @Composable BoxScope.() -> Unit
) {
    TvClickableSurface(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = container, focusedContainerColor = focusedContainer, contentColor = TD.Star, focusedContentColor = TD.Void),
        border = ClickableSurfaceDefaults.border(
            border = Border(androidx.compose.foundation.BorderStroke(1.dp, TD.Plasma.copy(alpha = 0.2f)), shape = shape),
            focusedBorder = Border(androidx.compose.foundation.BorderStroke(2.dp, TD.Plasma), shape = shape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f + (scale - 1f) * 0.5f),
        glow = ClickableSurfaceDefaults.glow(),
        content = content
    )
}


@Composable
internal fun TechDashChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TechDashSurface(onClick = onClick, modifier = modifier, shape = TD.Pill, container = if (selected) TD.Plasma.copy(alpha = 0.55f) else TD.Glass, scale = 1.08f) {
        Text(label, color = TD.Star, fontSize = 13.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), maxLines = 1)
    }
}


@Composable
internal fun TechDashLabel(text: String, modifier: Modifier = Modifier, color: Color = TD.Plasma) {
    Text("// " + text.uppercase(), color = color, fontSize = 11.sp, letterSpacing = 1.5.sp, fontFamily = TD.Mono, fontWeight = FontWeight.Bold, modifier = modifier)
}


@Composable
internal fun TechDashTitle(text: String, modifier: Modifier = Modifier, size: Int = 30) {
    Text(text, color = TD.Star, fontSize = size.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = modifier)
}

/** Segmented bar meter, like a signal-strength read-out. */
@Composable
internal fun TechDashProgress(progress: Float, modifier: Modifier = Modifier, height: Dp = 4.dp) {
    Canvas(modifier.fillMaxWidth().height(height + 4.dp)) {
        val p = progress.coerceIn(0f, 1f)
        val segments = 40
        val gap = 2.dp.toPx()
        val w = (size.width - gap * (segments - 1)) / segments
        val lit = (p * segments).toInt()
        for (i in 0 until segments) {
            drawRect(if (i < lit) (if (i > segments * 0.85f) TD.Flare else TD.Plasma) else TD.Dust.copy(alpha = 0.15f), topLeft = Offset(i * (w + gap), 2.dp.toPx()), size = androidx.compose.ui.geometry.Size(w, height.toPx()))
        }
    }
}


/** Poster as a data card: image, then a mono spec strip underneath. */
@Composable
internal fun TechDashPoster(
    title: String,
    imageUrl: String?,
    caption: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    locked: Boolean = false,
    onLongClick: (() -> Unit)? = null,
    progress: Float? = null
) {
    TechDashSurface(onClick = onClick, onLongClick = onLongClick, shape = TD.Poster, container = TD.Deep, modifier = modifier) {
        Column(Modifier.fillMaxWidth()) {
            Box(Modifier.fillMaxWidth().height(196.dp).background(TD.Nebula)) {
                if (!locked && imageUrl != null) AsyncImage(model = imageUrl, contentDescription = title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                if (locked) Text("🔒", fontSize = 28.sp, modifier = Modifier.align(Alignment.Center))
                if (locked || imageUrl == null) Text(title.take(3).uppercase(), color = TD.Plasma, fontSize = 28.sp, fontFamily = TD.Mono, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
            }
            if (progress != null) TechDashProgress(progress, Modifier.padding(horizontal = 6.dp), 3.dp)
            Column(Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                Text(title, color = TD.Star, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("> " + (caption?.takeIf { it.isNotBlank() } ?: "--"), color = TD.Plasma, fontSize = 11.sp, fontFamily = TD.Mono, maxLines = 1)
            }
        }
    }
}


/** Channel logo in a square bracket frame. */
@Composable
internal fun TechDashLogo(name: String, logoUrl: String?, size: Dp, modifier: Modifier = Modifier) {
    Box(modifier.size(size).background(TD.Deep, TD.Card).drawBehind {
        val l = this.size.minDimension * 0.25f; val s = 2.dp.toPx(); val c = TD.Plasma
        drawLine(c, Offset(0f, 0f), Offset(l, 0f), s); drawLine(c, Offset(0f, 0f), Offset(0f, l), s)
        drawLine(c, Offset(this.size.width, this.size.height), Offset(this.size.width - l, this.size.height), s); drawLine(c, Offset(this.size.width, this.size.height), Offset(this.size.width, this.size.height - l), s)
    }, contentAlignment = Alignment.Center) {
        Text(name.take(3).uppercase(), color = TD.Dust, fontSize = (size.value / 4).sp, fontWeight = FontWeight.Bold, fontFamily = TD.Mono)
        if (logoUrl != null) AsyncImage(model = logoUrl, contentDescription = name, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(size / 7))
    }
}


@Composable
internal fun TechDashEmpty(text: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(48.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("▣", color = TD.Plasma, fontSize = 40.sp)
        Text(text, color = TD.Dust, fontSize = 16.sp)
    }
}

@Composable
internal fun TechDashRowHeader(label: String, title: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().padding(bottom = 10.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        TechDashLabel(label)
        Text(title, color = TD.Star, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

internal fun formatClock(ms: Long): String {
    val c = java.util.Calendar.getInstance().apply { timeInMillis = ms }
    return "%02d:%02d".format(c.get(java.util.Calendar.HOUR_OF_DAY), c.get(java.util.Calendar.MINUTE))
}

internal fun formatDuration(ms: Long): String {
    val total = (ms / 1000L).coerceAtLeast(0L)
    val h = total / 3600; val m = (total % 3600) / 60; val s = total % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}

/** D-pad edge hooks: run [handler] on a RIGHT/LEFT key press; consume it only when the handler moved focus. */
internal fun Modifier.onRight(handler: () -> Boolean): Modifier = onDpadToward(end = true, handler)
internal fun Modifier.onLeft(handler: () -> Boolean): Modifier = onDpadToward(end = false, handler)



