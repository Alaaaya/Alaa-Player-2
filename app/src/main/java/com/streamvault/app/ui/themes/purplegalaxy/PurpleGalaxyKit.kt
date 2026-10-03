package com.streamvault.app.ui.themes.purplegalaxy

import com.streamvault.app.ui.themes.bespoke.onDpadToward
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
internal object PG {
    val Void = Color(0xFF07040F)
    val Deep = Color(0xFF100A22)
    val Nebula = Color(0xFF1F133A)
    val Glass = Color(0x33A87CFF)
    val GlassStrong = Color(0x55A87CFF)
    val Star = Color(0xFFF5F0FF)
    val Dust = Color(0xFFC2B2DD)
    val Muted = Color(0xFF8C7CAE)
    val Plasma = Color(0xFFA87CFF)
    val Comet = Color(0xFF5CE1FF)
    val Flare = Color(0xFFFF6FD8)
    val Live = Color(0xFFFF4F7B)
    val Pill = RoundedCornerShape(50)
    val Panel = RoundedCornerShape(28.dp)
    val Card = RoundedCornerShape(20.dp)
    val Arch = RoundedCornerShape(topStart = 80.dp, topEnd = 80.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
}

/** Static starfield + slowly drifting nebula glow. Drawn behind every Purple Galaxy surface. */
@Composable
internal fun GalaxyBackdrop(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val stars = remember {
        val r = Random(42)
        List(140) { Triple(r.nextFloat(), r.nextFloat(), r.nextFloat()) }
    }
    val drift = rememberInfiniteTransition(label = "pg-drift")
    val phase by drift.animateFloat(0f, 1f, infiniteRepeatable(tween(24000, easing = LinearEasing), RepeatMode.Reverse), label = "pg-phase")
    Box(modifier.fillMaxSize().background(PG.Void)) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(Brush.radialGradient(listOf(Color(0x66562A9E), Color.Transparent), center = Offset(size.width * (0.2f + 0.15f * phase), size.height * 0.25f), radius = size.minDimension * 0.9f))
            drawRect(Brush.radialGradient(listOf(Color(0x44FF6FD8), Color.Transparent), center = Offset(size.width * (0.85f - 0.1f * phase), size.height * 0.85f), radius = size.minDimension * 0.7f))
            stars.forEach { (x, y, s) ->
                drawCircle(PG.Star.copy(alpha = 0.25f + s * 0.6f), radius = 0.6f + s * 1.8f, center = Offset(x * size.width, y * size.height))
            }
        }
        content()
    }
}


/** Focusable glass surface with plasma ring focus and gentle scale, the base interaction primitive. */
@Composable
internal fun GalaxySurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = PG.Card,
    container: Color = PG.Glass,
    focusedContainer: Color = PG.GlassStrong,
    onLongClick: (() -> Unit)? = null,
    scale: Float = 1.05f,
    content: @Composable BoxScope.() -> Unit
) {
    TvClickableSurface(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = container, focusedContainerColor = focusedContainer, contentColor = PG.Star, focusedContentColor = PG.Star),
        border = ClickableSurfaceDefaults.border(
            border = Border(androidx.compose.foundation.BorderStroke(1.dp, PG.Plasma.copy(alpha = 0.18f)), shape = shape),
            focusedBorder = Border(androidx.compose.foundation.BorderStroke(2.5.dp, Brush.linearGradient(listOf(PG.Comet, PG.Plasma, PG.Flare))), shape = shape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = scale),
        glow = ClickableSurfaceDefaults.glow(),
        content = content
    )
}

@Composable
internal fun GalaxyChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    GalaxySurface(onClick = onClick, modifier = modifier, shape = PG.Pill, container = if (selected) PG.Plasma.copy(alpha = 0.55f) else PG.Glass, scale = 1.08f) {
        Text(label, color = PG.Star, fontSize = 13.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), maxLines = 1)
    }
}

@Composable
internal fun GalaxyLabel(text: String, modifier: Modifier = Modifier, color: Color = PG.Comet) {
    Text(text.uppercase(), color = color, fontSize = 11.sp, letterSpacing = 3.sp, fontWeight = FontWeight.Bold, modifier = modifier)
}

@Composable
internal fun GalaxyTitle(text: String, modifier: Modifier = Modifier, size: Int = 30) {
    Text(text, color = PG.Star, fontSize = size.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = modifier)
}

/** Orbital progress line with a glowing "planet" marker. */
@Composable
internal fun OrbitProgress(progress: Float, modifier: Modifier = Modifier, height: Dp = 4.dp) {
    Canvas(modifier.fillMaxWidth().height(height + 8.dp)) {
        val y = size.height / 2
        val p = progress.coerceIn(0f, 1f)
        drawLine(PG.Dust.copy(alpha = 0.2f), Offset(0f, y), Offset(size.width, y), strokeWidth = height.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(Brush.horizontalGradient(listOf(PG.Comet, PG.Plasma, PG.Flare)), Offset(0f, y), Offset(size.width * p, y), strokeWidth = height.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        drawCircle(PG.Star, radius = height.toPx() * 1.4f, center = Offset(size.width * p, y))
    }
}

/** Arch-framed poster ("observatory window"), the signature VOD card. */
@Composable
internal fun ArchPoster(
    title: String,
    imageUrl: String?,
    caption: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    locked: Boolean = false,
    onLongClick: (() -> Unit)? = null,
    progress: Float? = null
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        GalaxySurface(onClick = onClick, onLongClick = onLongClick, shape = PG.Arch, modifier = Modifier.fillMaxWidth().height(210.dp)) {
            if (!locked && imageUrl != null) {
                AsyncImage(model = imageUrl, contentDescription = title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(PG.Arch))
            }
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, PG.Void.copy(alpha = 0.85f)))))
            if (locked) Text("🔒", fontSize = 30.sp, modifier = Modifier.align(Alignment.Center))
            if (locked || imageUrl == null) Text(title.take(2).uppercase(), color = PG.Plasma, fontSize = 34.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.TopCenter).padding(top = 48.dp))
            if (progress != null) OrbitProgress(progress, Modifier.align(Alignment.BottomCenter).padding(10.dp), 3.dp)
        }
        Text(title, color = PG.Star, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (!caption.isNullOrBlank()) Text(caption, color = PG.Muted, fontSize = 12.sp, maxLines = 1)
    }
}

/** Circular "planet" logo used for channels. */
@Composable
internal fun PlanetLogo(name: String, logoUrl: String?, size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier.size(size).clip(CircleShape).background(Brush.radialGradient(listOf(PG.Nebula, PG.Deep))).border(1.dp, PG.Plasma.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(name.take(2).uppercase(), color = PG.Dust, fontSize = (size.value / 3).sp, fontWeight = FontWeight.Bold)
        if (logoUrl != null) AsyncImage(model = logoUrl, contentDescription = name, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(size / 6))
    }
}

@Composable
internal fun GalaxyEmpty(text: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(48.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("✦", color = PG.Plasma, fontSize = 40.sp)
        Text(text, color = PG.Dust, fontSize = 16.sp)
    }
}

@Composable
internal fun GalaxyRowHeader(label: String, title: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().padding(bottom = 10.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        GalaxyLabel(label)
        Text(title, color = PG.Star, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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

