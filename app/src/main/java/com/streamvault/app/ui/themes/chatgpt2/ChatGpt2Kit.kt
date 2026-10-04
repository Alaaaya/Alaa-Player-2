package com.streamvault.app.ui.themes.chatgpt2

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
internal object CG {
    val Bg = Color(0xFF0A0A0C)
    val Raised = Color(0xFF141418)
    val Card = Color(0xFF1A1A1F)
    val Line = Color(0xFF2A2A31)
    val Text = Color(0xFFFFFFFF)
    val Sub = Color(0xFFB8B8C0)
    val Faint = Color(0xFF7A7A84)
    val Amber = Color(0xFFE50914)
    val AmberDeep = Color(0xFFB0060F)
    val Live = Color(0xFFE5533D)
    val Blue = Color(0xFFFF4D57)
    val R = RoundedCornerShape(14.dp)
    val RSmall = RoundedCornerShape(10.dp)
    val Pill = RoundedCornerShape(10.dp)
    val Serif = androidx.compose.ui.text.font.FontFamily.SansSerif
}

/** Uppercase letter-spaced serif heading used for every section title in this theme. */
@Composable
internal fun CgHeading(text: String, modifier: Modifier = Modifier, size: Int = 14, color: Color = CG.Amber) {
    Text(text, color = color, fontSize = size.sp, fontFamily = CG.Serif, fontWeight = FontWeight.Bold,
        letterSpacing = (size / 5f).sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = modifier)
}

@Composable
internal fun CgBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0xFF1C0A0D), CG.Bg, Color(0xFF050506)), radius = 1800f)), content = content)
}

/** Focusable panel: brass fill + 4dp gold bar on the START edge when focused, no zoom. */
@Composable
internal fun CgCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = CG.R,
    container: Color = CG.Card,
    focusedContainer: Color = CG.Card,
    onLongClick: (() -> Unit)? = null,
    zoom: Float = 1.0f,
    content: @Composable BoxScope.() -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    TvClickableSurface(
        onClick = onClick, onLongClick = onLongClick, modifier = modifier.onFocusChanged { focused = it.isFocused },
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = container, focusedContainerColor = Color(0xFF3A0A0E), contentColor = CG.Text, focusedContentColor = CG.Text),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(androidx.compose.foundation.BorderStroke(2.dp, CG.Amber), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = zoom.coerceAtLeast(1.04f)),
        glow = ClickableSurfaceDefaults.glow(focusedGlow = androidx.tv.material3.Glow(CG.Amber.copy(alpha = 0.55f), 14.dp))
    ) {
        content()
        
    }
}

/** Rectangular receiver-style button: outlined brass, fills gold on focus. Primary = filled gold. */
@Composable
internal fun CgButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, primary: Boolean = false, icon: String? = null) {
    TvClickableSurface(
        onClick = onClick, modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(CG.RSmall),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (primary) CG.AmberDeep else Color.Transparent,
            focusedContainerColor = CG.Amber,
            contentColor = CG.Text, focusedContentColor = Color.White
        ),
        border = ClickableSurfaceDefaults.border(border = Border(androidx.compose.foundation.BorderStroke(1.dp, CG.Line), shape = CG.RSmall)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
        glow = ClickableSurfaceDefaults.glow(focusedGlow = androidx.tv.material3.Glow(CG.Amber.copy(alpha = 0.55f), 14.dp))
    ) {
        Row(Modifier.padding(horizontal = 18.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            icon?.let { CgGlyph(it, 18.dp) }
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, maxLines = 1)
        }
    }
}

/** Square transport key (like a remote/receiver front panel). */
@Composable
internal fun CgRound(glyph: String, onClick: () -> Unit, modifier: Modifier = Modifier, size: Dp = 56.dp, active: Boolean = false) {
    TvClickableSurface(
        onClick = onClick, modifier = modifier.size(size),
        shape = ClickableSurfaceDefaults.shape(CG.RSmall),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (active) CG.AmberDeep else Color(0xCC141418), focusedContainerColor = CG.Amber,
            contentColor = CG.Text, focusedContentColor = Color.White
        ),
        border = ClickableSurfaceDefaults.border(border = Border(androidx.compose.foundation.BorderStroke(1.dp, CG.Line), shape = CG.RSmall)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
        glow = ClickableSurfaceDefaults.glow(focusedGlow = androidx.tv.material3.Glow(CG.Amber.copy(alpha = 0.55f), 14.dp))
    ) { CgGlyph(glyph, size * 0.46f, Modifier.align(Alignment.Center)) }
}

/** Text-only menu entry: gold underline when selected, gold fill when focused. */
@Composable
internal fun CgTab(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TvClickableSurface(
        onClick = onClick, modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(CG.RSmall),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) CG.AmberDeep else Color.Transparent, focusedContainerColor = CG.Amber,
            contentColor = if (selected) Color.White else CG.Sub, focusedContentColor = Color.White
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
        glow = ClickableSurfaceDefaults.glow(focusedGlow = androidx.tv.material3.Glow(CG.Amber.copy(alpha = 0.55f), 14.dp))
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp, maxLines = 1)
            if (selected) Box(Modifier.padding(top = 3.dp).width(24.dp).height(2.dp).background(Color.White))
        }
    }
}

@Composable
internal fun CgRowTitle(text: String, modifier: Modifier = Modifier, trailing: String? = null) {
    Row(modifier.padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CgHeading(text, size = 15)
        Box(Modifier.weight(1f).height(1.dp).background(CG.Line))
        trailing?.let { Text(it, color = CG.Faint, fontSize = 12.sp) }
    }
}

/** Segmented VU-meter style progress: brass segments. */
@Composable
internal fun CgProgress(progress: Float, modifier: Modifier = Modifier, height: Dp = 4.dp, track: Color = CG.Line) {
    Box(modifier.fillMaxWidth().height(height).background(track)) {
        Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).height(height).background(Brush.horizontalGradient(listOf(CG.AmberDeep, CG.Amber))))
    }
}

/** Glossy poster card: rounded art with the title overlaid on a dark fade at the bottom (as in the reference). */
@Composable
internal fun CgPoster(title: String, imageUrl: String?, caption: String?, onClick: () -> Unit, modifier: Modifier = Modifier, locked: Boolean = false, onLongClick: (() -> Unit)? = null) {
    CgCard(onClick = onClick, onLongClick = onLongClick, modifier = modifier.fillMaxWidth().aspectRatio(2f / 3f), shape = CG.RSmall) {
        if (!locked && imageUrl != null) AsyncImage(imageUrl, title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        else Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(CG.Card, CG.Raised))), contentAlignment = Alignment.Center) {
            Text(if (locked) "🔒" else title.take(1).uppercase(), color = CG.Blue, fontSize = 34.sp, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xEE050506)))).padding(horizontal = 8.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            caption?.takeIf { it.isNotBlank() }?.let { Text(it, color = CG.Sub, fontSize = 10.sp, maxLines = 1) }
        }
    }
}

/** Landscape "fanart" tile with the title in a brass caption strip below the art (inside the frame). */
@Composable
internal fun CgWide(title: String, imageUrl: String?, sub: String?, progress: Float?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    CgCard(onClick = onClick, modifier = modifier, shape = CG.RSmall) {
        Column {
            Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f).background(CG.Raised)) {
                imageUrl?.let { AsyncImage(it, title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                progress?.let { CgProgress(it, Modifier.align(Alignment.BottomCenter), 4.dp) }
            }
            Column(Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
                Text(title, color = CG.Text, fontSize = 13.sp, fontFamily = CG.Serif, maxLines = 1, overflow = TextOverflow.Ellipsis)
                sub?.let { Text(it, color = CG.Faint, fontSize = 11.sp, maxLines = 1) }
            }
        }
    }
}

/** Channel logo on a brass-framed square plate. */
@Composable
internal fun CgLogo(name: String, logoUrl: String?, size: Dp, modifier: Modifier = Modifier) {
    Box(modifier.size(size).clip(CG.RSmall).background(Color(0xFF111114)).border(1.dp, CG.Line, CG.RSmall), contentAlignment = Alignment.Center) {
        Text(name.take(2).uppercase(), color = CG.Faint, fontSize = (size.value / 3.2f).sp, fontFamily = CG.Serif, fontWeight = FontWeight.Bold)
        logoUrl?.let { AsyncImage(it, name, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(size / 8)) }
    }
}

/** Outlined engraved-label badge. */
@Composable
internal fun CgBadge(text: String, color: Color = CG.Amber, filled: Boolean = false) {
    Text(
        text, color = if (filled) Color.White else color, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, maxLines = 1,
        modifier = Modifier.then(if (filled) Modifier.background(color, CG.RSmall) else Modifier.border(1.dp, color.copy(alpha = 0.7f), CG.RSmall)).padding(horizontal = 5.dp, vertical = 1.dp)
    )
}

@Composable
internal fun CgEmpty(text: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(60.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("— ✦ —", color = CG.Line, fontSize = 30.sp)
        Text(text, color = CG.Sub, fontSize = 16.sp, fontFamily = CG.Serif)
    }
}

internal fun cgClock(ms: Long): String {
    val c = java.util.Calendar.getInstance().apply { timeInMillis = ms }
    return "%02d:%02d".format(c.get(java.util.Calendar.HOUR_OF_DAY), c.get(java.util.Calendar.MINUTE))
}

internal fun cgDuration(ms: Long): String {
    val t = (ms / 1000L).coerceAtLeast(0L)
    val h = t / 3600; val m = (t % 3600) / 60; val s = t % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}

internal fun Modifier.cgKey(code: Int, handler: () -> Boolean): Modifier = this.then(
    Modifier.onPreviewKeyEvent { e -> e.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN && e.nativeKeyEvent.keyCode == code && handler() }
)
internal fun Modifier.cgRight(h: () -> Boolean): Modifier = onDpadToward(end = true, h)
internal fun Modifier.cgLeft(h: () -> Boolean): Modifier = onDpadToward(end = false, h)
