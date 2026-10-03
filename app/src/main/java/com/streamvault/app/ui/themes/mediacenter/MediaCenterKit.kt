package com.streamvault.app.ui.themes.mediacenter

import com.streamvault.app.ui.themes.bespoke.onDpadToward
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.runtime.*
import androidx.compose.foundation.border
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

/** Media Center: a home-theater receiver look. Walnut-dark background, brass/gold accents, serif uppercase
 *  letter-spaced headings, square 6dp corners. Focus = solid brass fill + gold edge bar, NO zoom (feels like a
 *  hardware menu, not a streaming app). Lists are the primary navigation; artwork is "fanart" behind content. */
internal object MC {
    val Bg = Color(0xFF17130D)
    val Raised = Color(0xFF211B12)
    val Card = Color(0xFF2B2216)
    val Line = Color(0xFF4D3C22)
    val Text = Color(0xFFFFF7E8)
    val Sub = Color(0xFFD0BE9C)
    val Faint = Color(0xFF8E7D60)
    val Amber = Color(0xFFE7B84F)
    val AmberDeep = Color(0xFFB58A2E)
    val Live = Color(0xFFE5533D)
    val Blue = Color(0xFF8FB7C9)
    val R = RoundedCornerShape(6.dp)
    val RSmall = RoundedCornerShape(3.dp)
    val Pill = RoundedCornerShape(3.dp)
    val Serif = androidx.compose.ui.text.font.FontFamily.Serif
}

/** Uppercase letter-spaced serif heading used for every section title in this theme. */
@Composable
internal fun McHeading(text: String, modifier: Modifier = Modifier, size: Int = 14, color: Color = MC.Amber) {
    Text(text.uppercase(), color = color, fontSize = size.sp, fontFamily = MC.Serif, fontWeight = FontWeight.Bold,
        letterSpacing = (size / 5f).sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = modifier)
}

@Composable
internal fun McBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0xFF2A2013), MC.Bg, Color(0xFF0D0A06)), radius = 1800f)), content = content)
}

/** Focusable panel: brass fill + 4dp gold bar on the START edge when focused, no zoom. */
@Composable
internal fun McCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MC.R,
    container: Color = MC.Card,
    focusedContainer: Color = MC.Card,
    onLongClick: (() -> Unit)? = null,
    zoom: Float = 1.0f,
    content: @Composable BoxScope.() -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    TvClickableSurface(
        onClick = onClick, onLongClick = onLongClick, modifier = modifier.onFocusChanged { focused = it.isFocused },
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = container, focusedContainerColor = Color(0xFF4A3818), contentColor = MC.Text, focusedContentColor = MC.Text),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(androidx.compose.foundation.BorderStroke(1.dp, MC.Amber), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = zoom.coerceAtMost(1.02f)),
        glow = ClickableSurfaceDefaults.glow()
    ) {
        content()
        if (focused) Box(Modifier.align(Alignment.CenterStart).width(4.dp).fillMaxHeight().background(MC.Amber))
    }
}

/** Rectangular receiver-style button: outlined brass, fills gold on focus. Primary = filled gold. */
@Composable
internal fun McButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, primary: Boolean = false, icon: String? = null) {
    TvClickableSurface(
        onClick = onClick, modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(MC.RSmall),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (primary) MC.AmberDeep else Color.Transparent,
            focusedContainerColor = MC.Amber,
            contentColor = MC.Text, focusedContentColor = MC.Bg
        ),
        border = ClickableSurfaceDefaults.border(border = Border(androidx.compose.foundation.BorderStroke(1.dp, MC.Line), shape = MC.RSmall)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
        glow = ClickableSurfaceDefaults.glow()
    ) {
        Row(Modifier.padding(horizontal = 18.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            icon?.let { Text(it, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
            Text(label.uppercase(), fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, maxLines = 1)
        }
    }
}

/** Square transport key (like a remote/receiver front panel). */
@Composable
internal fun McRound(glyph: String, onClick: () -> Unit, modifier: Modifier = Modifier, size: Dp = 56.dp, active: Boolean = false) {
    TvClickableSurface(
        onClick = onClick, modifier = modifier.size(size),
        shape = ClickableSurfaceDefaults.shape(MC.RSmall),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (active) MC.AmberDeep else Color(0xCC211B12), focusedContainerColor = MC.Amber,
            contentColor = MC.Text, focusedContentColor = MC.Bg
        ),
        border = ClickableSurfaceDefaults.border(border = Border(androidx.compose.foundation.BorderStroke(1.dp, MC.Line), shape = MC.RSmall)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
        glow = ClickableSurfaceDefaults.glow()
    ) { Text(glyph, fontSize = (size.value / 2.8f).sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center)) }
}

/** Text-only menu entry: gold underline when selected, gold fill when focused. */
@Composable
internal fun McTab(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TvClickableSurface(
        onClick = onClick, modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(MC.RSmall),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent, focusedContainerColor = MC.Amber,
            contentColor = if (selected) MC.Amber else MC.Sub, focusedContentColor = MC.Bg
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
        glow = ClickableSurfaceDefaults.glow()
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
            Text(label.uppercase(), fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp, maxLines = 1)
            if (selected) Box(Modifier.padding(top = 3.dp).width(24.dp).height(2.dp).background(MC.Amber))
        }
    }
}

@Composable
internal fun McRowTitle(text: String, modifier: Modifier = Modifier, trailing: String? = null) {
    Row(modifier.padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        McHeading(text, size = 15)
        Box(Modifier.weight(1f).height(1.dp).background(MC.Line))
        trailing?.let { Text(it, color = MC.Faint, fontSize = 12.sp) }
    }
}

/** Segmented VU-meter style progress: brass segments. */
@Composable
internal fun McProgress(progress: Float, modifier: Modifier = Modifier, height: Dp = 4.dp, track: Color = MC.Line) {
    Box(modifier.fillMaxWidth().height(height).background(track)) {
        Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).height(height).background(Brush.horizontalGradient(listOf(MC.AmberDeep, MC.Amber))))
    }
}

/** Poster in a thin brass frame (like a framed one-sheet); title + caption below. */
@Composable
internal fun McPoster(title: String, imageUrl: String?, caption: String?, onClick: () -> Unit, modifier: Modifier = Modifier, locked: Boolean = false, onLongClick: (() -> Unit)? = null) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        McCard(onClick = onClick, onLongClick = onLongClick, modifier = Modifier.fillMaxWidth().aspectRatio(2f / 3f), shape = MC.RSmall) {
            if (!locked && imageUrl != null) AsyncImage(imageUrl, title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().padding(3.dp))
            else Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(MC.Card, MC.Raised))), contentAlignment = Alignment.Center) {
                Text(if (locked) "🔒" else title.take(1).uppercase(), color = MC.Amber, fontSize = 34.sp, fontFamily = MC.Serif, fontWeight = FontWeight.Bold)
            }
        }
        Text(title, color = MC.Text, fontSize = 13.sp, fontFamily = MC.Serif, maxLines = 1, overflow = TextOverflow.Ellipsis)
        caption?.takeIf { it.isNotBlank() }?.let { Text(it, color = MC.Faint, fontSize = 11.sp, maxLines = 1) }
    }
}

/** Landscape "fanart" tile with the title in a brass caption strip below the art (inside the frame). */
@Composable
internal fun McWide(title: String, imageUrl: String?, sub: String?, progress: Float?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    McCard(onClick = onClick, modifier = modifier, shape = MC.RSmall) {
        Column {
            Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f).background(MC.Raised)) {
                imageUrl?.let { AsyncImage(it, title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                progress?.let { McProgress(it, Modifier.align(Alignment.BottomCenter), 4.dp) }
            }
            Column(Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
                Text(title, color = MC.Text, fontSize = 13.sp, fontFamily = MC.Serif, maxLines = 1, overflow = TextOverflow.Ellipsis)
                sub?.let { Text(it, color = MC.Faint, fontSize = 11.sp, maxLines = 1) }
            }
        }
    }
}

/** Channel logo on a brass-framed square plate. */
@Composable
internal fun McLogo(name: String, logoUrl: String?, size: Dp, modifier: Modifier = Modifier) {
    Box(modifier.size(size).clip(MC.RSmall).background(Color(0xFF0F0C08)).border(1.dp, MC.Line, MC.RSmall), contentAlignment = Alignment.Center) {
        Text(name.take(2).uppercase(), color = MC.Faint, fontSize = (size.value / 3.2f).sp, fontFamily = MC.Serif, fontWeight = FontWeight.Bold)
        logoUrl?.let { AsyncImage(it, name, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(size / 8)) }
    }
}

/** Outlined engraved-label badge. */
@Composable
internal fun McBadge(text: String, color: Color = MC.Amber, filled: Boolean = false) {
    Text(
        text.uppercase(), color = if (filled) MC.Bg else color, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, maxLines = 1,
        modifier = Modifier.then(if (filled) Modifier.background(color, MC.RSmall) else Modifier.border(1.dp, color.copy(alpha = 0.7f), MC.RSmall)).padding(horizontal = 5.dp, vertical = 1.dp)
    )
}

@Composable
internal fun McEmpty(text: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(60.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("— ✦ —", color = MC.Line, fontSize = 30.sp)
        Text(text, color = MC.Sub, fontSize = 16.sp, fontFamily = MC.Serif)
    }
}

internal fun mcClock(ms: Long): String {
    val c = java.util.Calendar.getInstance().apply { timeInMillis = ms }
    return "%02d:%02d".format(c.get(java.util.Calendar.HOUR_OF_DAY), c.get(java.util.Calendar.MINUTE))
}

internal fun mcDuration(ms: Long): String {
    val t = (ms / 1000L).coerceAtLeast(0L)
    val h = t / 3600; val m = (t % 3600) / 60; val s = t % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}

internal fun Modifier.mcKey(code: Int, handler: () -> Boolean): Modifier = this.then(
    Modifier.onPreviewKeyEvent { e -> e.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN && e.nativeKeyEvent.keyCode == code && handler() }
)
internal fun Modifier.mcRight(h: () -> Boolean): Modifier = onDpadToward(end = true, h)
internal fun Modifier.mcLeft(h: () -> Boolean): Modifier = onDpadToward(end = false, h)
