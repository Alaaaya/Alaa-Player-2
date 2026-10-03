package com.streamvault.app.ui.themes.futuristichud

import com.streamvault.app.ui.themes.bespoke.onDpadToward
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.foundation.layout.fillMaxHeight
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

/** Futuristic HUD: deep navy cockpit, cyan scan lines, chamfered (cut-corner) panels, monospace telemetry text,
 *  corner-bracket focus frames and a faint grid backdrop. Nothing is rounded. */
internal object FH {
    val Bg = Color(0xFF050C16)
    val Raised = Color(0xFF0B1727)
    val Card = Color(0xFF08111F)
    val Line = Color(0xFF12335A)
    val Text = Color(0xFFE6F4FF)
    val Sub = Color(0xFF86B0D5)
    val Faint = Color(0xFF4F7193)
    val Amber = Color(0xFF3FE0FF)      // primary accent = HUD cyan
    val AmberDeep = Color(0xFF1C8FD6)
    val Live = Color(0xFFFF3B6B)
    val Blue = Color(0xFF6AA9FF)
    val Warn = Color(0xFFFFC94A)
    val R = androidx.compose.foundation.shape.CutCornerShape(topStart = 14.dp, bottomEnd = 14.dp)
    val RSmall = androidx.compose.foundation.shape.CutCornerShape(6.dp)
    val Pill = androidx.compose.foundation.shape.CutCornerShape(50)
    val Mono = androidx.compose.ui.text.font.FontFamily.Monospace
}

@Composable
internal fun FhBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0xFF0B2240), FH.Bg), radius = 1600f))
        .drawBehind {
            val step = 48.dp.toPx(); val c = FH.Line.copy(alpha = 0.22f)
            var x = 0f; while (x < size.width) { drawLine(c, androidx.compose.ui.geometry.Offset(x, 0f), androidx.compose.ui.geometry.Offset(x, size.height), 1f); x += step }
            var y = 0f; while (y < size.height) { drawLine(c, androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(size.width, y), 1f); y += step }
        }, content = content)
}

/** Corner-bracket overlay: four L brackets at the corners, drawn on a box. */
internal fun Modifier.fhBrackets(color: Color = FH.Amber, len: Dp = 12.dp, stroke: Dp = 2.dp): Modifier = this.drawWithContent {
    drawContent()
    val l = len.toPx(); val w = stroke.toPx(); val W = size.width; val H = size.height
    fun ln(x1: Float, y1: Float, x2: Float, y2: Float) = drawLine(color, androidx.compose.ui.geometry.Offset(x1, y1), androidx.compose.ui.geometry.Offset(x2, y2), w)
    ln(0f, 0f, l, 0f); ln(0f, 0f, 0f, l); ln(W, 0f, W - l, 0f); ln(W, 0f, W, l)
    ln(0f, H, l, H); ln(0f, H, 0f, H - l); ln(W, H, W - l, H); ln(W, H, W, H - l)
}

/** Chamfered HUD panel with thin cyan outline and bracket corners. */
@Composable
internal fun FhPanel(modifier: Modifier = Modifier, title: String? = null, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(modifier.clip(FH.R).background(FH.Card.copy(alpha = 0.86f))
        .border(1.dp, FH.Line, FH.R).fhBrackets(FH.Amber.copy(alpha = 0.7f)).padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        title?.let { FhLabel(it) }
        content()
    }
}

/** Uppercase monospace label prefixed with a cyan tick, e.g. "▸ SYS.CHANNELS". */
@Composable
internal fun FhLabel(text: String, modifier: Modifier = Modifier, color: Color = FH.Amber) {
    Text("▸ " + text.uppercase(), color = color, fontSize = 11.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, maxLines = 1, modifier = modifier)
}

/** Focusable card: white 3dp ring + 1.06 zoom on focus, no glow. */
@Composable
internal fun FhCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = FH.R,
    container: Color = FH.Card,
    focusedContainer: Color = FH.Card,
    onLongClick: (() -> Unit)? = null,
    zoom: Float = 1.06f,
    content: @Composable BoxScope.() -> Unit
) {
    TvClickableSurface(
        onClick = onClick, onLongClick = onLongClick, modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = container, focusedContainerColor = focusedContainer, contentColor = FH.Text, focusedContentColor = FH.Text),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(androidx.compose.foundation.BorderStroke(2.dp, FH.Amber), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = if (zoom > 1.03f) 1.03f else zoom),
        glow = ClickableSurfaceDefaults.glow(),
        content = content
    )
}

/** Solid pill button. Primary = amber fill; secondary = translucent white that turns white on focus. */
@Composable
internal fun FhButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, primary: Boolean = false, icon: String? = null) {
    TvClickableSurface(
        onClick = onClick, modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(FH.Pill),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (primary) FH.Amber.copy(alpha = 0.22f) else FH.Raised,
            focusedContainerColor = FH.Amber,
            contentColor = if (primary) FH.Amber else FH.Text, focusedContentColor = FH.Bg
        ),
        border = ClickableSurfaceDefaults.border(border = Border(androidx.compose.foundation.BorderStroke(1.dp, FH.Line))),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
        glow = ClickableSurfaceDefaults.glow()
    ) {
        Row(Modifier.padding(horizontal = 22.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            icon?.let { Text(it, fontSize = 15.sp, fontWeight = FontWeight.Bold) }
            Text(label.uppercase(), fontSize = 13.sp, fontFamily = FH.Mono, letterSpacing = 1.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

/** Round icon button used by the players. */
@Composable
internal fun FhRound(glyph: String, onClick: () -> Unit, modifier: Modifier = Modifier, size: Dp = 56.dp, active: Boolean = false) {
    TvClickableSurface(
        onClick = onClick, modifier = modifier.size(size),
        shape = ClickableSurfaceDefaults.shape(androidx.compose.foundation.shape.CutCornerShape(30)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (active) FH.Amber else FH.Raised.copy(alpha = 0.9f), focusedContainerColor = FH.Amber,
            contentColor = if (active) FH.Bg else FH.Text, focusedContentColor = FH.Bg
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.12f),
        glow = ClickableSurfaceDefaults.glow()
    ) { Text(glyph, fontSize = (size.value / 2.6f).sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center)) }
}

@Composable
internal fun FhTab(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TvClickableSurface(
        onClick = onClick, modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(FH.Pill),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) FH.Amber.copy(alpha = 0.18f) else Color.Transparent, focusedContainerColor = FH.Amber,
            contentColor = if (selected) FH.Text else FH.Sub, focusedContentColor = FH.Bg
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.04f),
        glow = ClickableSurfaceDefaults.glow()
    ) { Text(label.uppercase(), fontSize = 12.sp, fontFamily = FH.Mono, letterSpacing = 1.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, maxLines = 1, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) }
}

@Composable
internal fun FhRowTitle(text: String, modifier: Modifier = Modifier, trailing: String? = null) {
    Row(modifier.padding(bottom = 10.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("// " + text.uppercase(), color = FH.Amber, fontSize = 15.sp, fontFamily = FH.Mono, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
        Box(Modifier.weight(1f).height(1.dp).background(FH.Line))
        trailing?.let { Text(it, color = FH.Faint, fontSize = 11.sp, fontFamily = FH.Mono) }
    }
}

/** Thin rounded progress line with amber fill. */
@Composable
internal fun FhProgress(progress: Float, modifier: Modifier = Modifier, height: Dp = 4.dp, track: Color = Color.White.copy(alpha = 0.2f)) {
    val segs = 24; val on = (progress.coerceIn(0f, 1f) * segs).toInt()
    Row(modifier.fillMaxWidth().height(height), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(segs) { i -> Box(Modifier.weight(1f).fillMaxHeight().background(if (i < on) FH.Amber else FH.Line.copy(alpha = 0.6f))) }
    }
}

/** Portrait poster with rounded corners; title below the art, never on top of it. */
@Composable
internal fun FhPoster(title: String, imageUrl: String?, caption: String?, onClick: () -> Unit, modifier: Modifier = Modifier, locked: Boolean = false, onLongClick: (() -> Unit)? = null) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FhCard(onClick = onClick, onLongClick = onLongClick, modifier = Modifier.fillMaxWidth().aspectRatio(2f / 3f)) {
            if (!locked && imageUrl != null) AsyncImage(imageUrl, title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            else Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(FH.Line, FH.Raised))), contentAlignment = Alignment.Center) {
                Text(if (locked) "🔒" else title.take(1).uppercase(), color = FH.Sub, fontSize = 34.sp, fontWeight = FontWeight.Black)
            }
        }
        Text(title.uppercase(), color = FH.Text, fontSize = 12.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        caption?.takeIf { it.isNotBlank() }?.let { Text("› $it", color = FH.Sub, fontSize = 10.sp, fontFamily = FH.Mono, maxLines = 1) }
    }
}

/** Wide 16:9 landscape card with a progress line pinned to the bottom edge. */
@Composable
internal fun FhWide(title: String, imageUrl: String?, sub: String?, progress: Float?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FhCard(onClick = onClick, modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
            imageUrl?.let { AsyncImage(it, title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)))))
            Text("[ ▶ ]", color = FH.Amber, fontSize = 18.sp, fontFamily = FH.Mono, modifier = Modifier.align(Alignment.Center))
            progress?.let { FhProgress(it, Modifier.align(Alignment.BottomCenter).padding(10.dp), 4.dp) }
        }
        Text(title.uppercase(), color = FH.Text, fontSize = 12.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        sub?.let { Text(it, color = FH.Sub, fontSize = 10.sp, fontFamily = FH.Mono, maxLines = 1) }
    }
}

/** Circular channel logo on a white disc, the way streaming apps show networks. */
@Composable
internal fun FhLogo(name: String, logoUrl: String?, size: Dp, modifier: Modifier = Modifier) {
    Box(modifier.size(size).clip(FH.RSmall).background(FH.Raised).border(1.dp, FH.Line, FH.RSmall), contentAlignment = Alignment.Center) {
        Text(name.take(2).uppercase(), color = FH.Sub, fontSize = (size.value / 3.2f).sp, fontWeight = FontWeight.Bold)
        logoUrl?.let { AsyncImage(it, name, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(size / 8)) }
    }
}

@Composable
internal fun FhBadge(text: String, color: Color = FH.Amber, filled: Boolean = false) {
    Text(
        "[" + text.uppercase() + "]", color = if (filled) FH.Bg else color, fontSize = 10.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Black, maxLines = 1,
        modifier = Modifier.background(if (filled) color else Color.Transparent).padding(horizontal = 3.dp, vertical = 1.dp)
    )
}

@Composable
internal fun FhEmpty(text: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(60.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("[ NO SIGNAL ]", color = FH.Faint, fontSize = 28.sp, fontFamily = FH.Mono)
        Text(text, color = FH.Sub, fontSize = 14.sp, fontFamily = FH.Mono)
    }
}

internal fun fhClock(ms: Long): String {
    val c = java.util.Calendar.getInstance().apply { timeInMillis = ms }
    return "%02d:%02d".format(c.get(java.util.Calendar.HOUR_OF_DAY), c.get(java.util.Calendar.MINUTE))
}

internal fun fhDuration(ms: Long): String {
    val t = (ms / 1000L).coerceAtLeast(0L)
    val h = t / 3600; val m = (t % 3600) / 60; val s = t % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}

internal fun Modifier.fhKey(code: Int, handler: () -> Boolean): Modifier = this.then(
    Modifier.onPreviewKeyEvent { e -> e.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN && e.nativeKeyEvent.keyCode == code && handler() }
)
internal fun Modifier.fhRight(h: () -> Boolean): Modifier = onDpadToward(end = true, h)
internal fun Modifier.fhLeft(h: () -> Boolean): Modifier = onDpadToward(end = false, h)
