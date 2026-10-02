package com.streamvault.app.ui.themes.magazinemedia

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

/** MagazineMedia: an editorial print look. Warm paper, ink-black serif headlines, rust accent, hairline
 *  column rules, square-cornered "photo plates" with captions set like a magazine, ink underline on focus. */
internal object MZ {
    val Bg = Color(0xFFF2EFE8)
    val Raised = Color(0xFFFBF8F2)
    val Card = Color(0xFFE2DDD2)
    val Line = Color(0xFFCFC7B8)
    val Text = Color(0xFF29251F)
    val Sub = Color(0xFF5E574B)
    val Faint = Color(0xFF8F8676)
    val Amber = Color(0xFFC2572F)      // rust accent (name kept for shared call sites)
    val AmberDeep = Color(0xFF9C3F1E)
    val Live = Color(0xFFC2272F)
    val Blue = Color(0xFF2F5E7A)
    val Gold = Color(0xFFE7D7B7)
    val Serif = androidx.compose.ui.text.font.FontFamily.Serif
    val R = RoundedCornerShape(2.dp)
    val RSmall = RoundedCornerShape(2.dp)
    val Pill = RoundedCornerShape(2.dp)
}

@Composable
internal fun MzBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier.fillMaxSize().background(MZ.Bg), content = content)
}

/** Hairline horizontal rule, like a column divider in print. */
@Composable
internal fun MzRule(modifier: Modifier = Modifier, color: Color = MZ.Text, thick: Dp = 1.dp) {
    Box(modifier.fillMaxWidth().height(thick).background(color))
}

/** Kicker: small uppercase rust label that sits above a headline. */
@Composable
internal fun MzKicker(text: String, modifier: Modifier = Modifier, color: Color = MZ.Amber) {
    Text(text.uppercase(), color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, maxLines = 1, modifier = modifier)
}

/** Focusable plate: square corners, ink 2dp frame on focus, tiny lift; paper container. */
@Composable
internal fun MzCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MZ.R,
    container: Color = MZ.Raised,
    focusedContainer: Color = MZ.Raised,
    onLongClick: (() -> Unit)? = null,
    zoom: Float = 1.03f,
    content: @Composable BoxScope.() -> Unit
) {
    TvClickableSurface(
        onClick = onClick, onLongClick = onLongClick, modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = container, focusedContainerColor = focusedContainer, contentColor = MZ.Text, focusedContentColor = MZ.Text),
        border = ClickableSurfaceDefaults.border(
            border = Border(androidx.compose.foundation.BorderStroke(1.dp, MZ.Line), shape = shape),
            focusedBorder = Border(androidx.compose.foundation.BorderStroke(3.dp, MZ.Text), shape = shape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = zoom),
        glow = ClickableSurfaceDefaults.glow(),
        content = content
    )
}

/** Text-link button: uppercase tracking, primary = rust block, secondary = ink outline that inverts on focus. */
@Composable
internal fun MzButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, primary: Boolean = false, icon: String? = null) {
    TvClickableSurface(
        onClick = onClick, modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(MZ.R),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (primary) MZ.Amber else MZ.Raised,
            focusedContainerColor = MZ.Text,
            contentColor = if (primary) MZ.Raised else MZ.Text, focusedContentColor = MZ.Raised
        ),
        border = ClickableSurfaceDefaults.border(border = Border(androidx.compose.foundation.BorderStroke(1.dp, if (primary) MZ.Amber else MZ.Text), shape = MZ.R)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f),
        glow = ClickableSurfaceDefaults.glow()
    ) {
        Row(Modifier.padding(horizontal = 18.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            icon?.let { Text(it, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
            Text(label.uppercase(), fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, maxLines = 1)
        }
    }
}

/** Square ink-outlined glyph box used by the players (a printed "index" box, not a round orb). */
@Composable
internal fun MzRound(glyph: String, onClick: () -> Unit, modifier: Modifier = Modifier, size: Dp = 56.dp, active: Boolean = false) {
    TvClickableSurface(
        onClick = onClick, modifier = modifier.size(size),
        shape = ClickableSurfaceDefaults.shape(MZ.R),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (active) MZ.Amber else MZ.Raised, focusedContainerColor = MZ.Text,
            contentColor = if (active) MZ.Raised else MZ.Text, focusedContentColor = MZ.Raised
        ),
        border = ClickableSurfaceDefaults.border(border = Border(androidx.compose.foundation.BorderStroke(1.dp, MZ.Text), shape = MZ.R)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.06f),
        glow = ClickableSurfaceDefaults.glow()
    ) { Text(glyph, fontSize = (size.value / 2.8f).sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center)) }
}

/** Section tab: serif word with a rust underline when selected; inverts to ink block on focus. */
@Composable
internal fun MzTab(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TvClickableSurface(
        onClick = onClick, modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(MZ.R),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent, focusedContainerColor = MZ.Text,
            contentColor = if (selected) MZ.Text else MZ.Sub, focusedContentColor = MZ.Raised
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f),
        glow = ClickableSurfaceDefaults.glow()
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
            Text(label, fontSize = 15.sp, fontFamily = MZ.Serif, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, maxLines = 1)
            if (selected) Box(Modifier.padding(top = 3.dp).fillMaxWidth().height(2.dp).background(MZ.Amber))
        }
    }
}

/** Section headline: serif title, rule under it, small "page" counter on the end. */
@Composable
internal fun MzRowTitle(text: String, modifier: Modifier = Modifier, trailing: String? = null) {
    Column(modifier.padding(bottom = 10.dp)) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text, color = MZ.Text, fontSize = 22.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f, false))
            trailing?.let { Text("— $it", color = MZ.Faint, fontSize = 12.sp, fontFamily = MZ.Serif) }
        }
        MzRule(Modifier.padding(top = 6.dp))
    }
}

/** Progress: hairline track with a rust bar, square ends. */
@Composable
internal fun MzProgress(progress: Float, modifier: Modifier = Modifier, height: Dp = 3.dp, track: Color = MZ.Line) {
    Box(modifier.fillMaxWidth().height(height).background(track)) {
        Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).height(height).background(MZ.Amber))
    }
}

/** Poster plate: photo inside a thin paper mat, serif caption + italic credit line below. */
@Composable
internal fun MzPoster(title: String, imageUrl: String?, caption: String?, onClick: () -> Unit, modifier: Modifier = Modifier, locked: Boolean = false, onLongClick: (() -> Unit)? = null) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        MzCard(onClick = onClick, onLongClick = onLongClick, modifier = Modifier.fillMaxWidth().aspectRatio(2f / 3f)) {
            Box(Modifier.fillMaxSize().padding(5.dp).background(MZ.Card)) {
                if (!locked && imageUrl != null) AsyncImage(imageUrl, title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                else Text(if (locked) "🔒" else title.take(1).uppercase(), color = MZ.Sub, fontSize = 44.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
            }
        }
        Text(title, color = MZ.Text, fontSize = 14.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        caption?.takeIf { it.isNotBlank() }?.let { Text(it, color = MZ.Faint, fontSize = 11.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, maxLines = 1) }
    }
}

/** Wide feature plate: photo on top, kicker+serif headline below on paper, rust progress rule at the seam. */
@Composable
internal fun MzWide(title: String, imageUrl: String?, sub: String?, progress: Float?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    MzCard(onClick = onClick, modifier = modifier) {
        Column {
            Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f).background(MZ.Card)) {
                imageUrl?.let { AsyncImage(it, title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
            }
            progress?.let { MzProgress(it, height = 4.dp) }
            Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                Text(title, color = MZ.Text, fontSize = 14.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                sub?.let { Text(it, color = MZ.Faint, fontSize = 11.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, maxLines = 1) }
            }
        }
    }
}

/** Channel mark: square paper stamp with ink frame. */
@Composable
internal fun MzLogo(name: String, logoUrl: String?, size: Dp, modifier: Modifier = Modifier) {
    Box(modifier.size(size).background(MZ.Raised).border(1.dp, MZ.Text), contentAlignment = Alignment.Center) {
        Text(name.take(2).uppercase(), color = MZ.Sub, fontSize = (size.value / 3.2f).sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Bold)
        logoUrl?.let { AsyncImage(it, name, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(size / 8)) }
    }
}

@Composable
internal fun MzBadge(text: String, color: Color = MZ.Amber, filled: Boolean = false) {
    Text(
        text.uppercase(), color = if (filled) MZ.Raised else color, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, maxLines = 1,
        modifier = Modifier.background(if (filled) color else Color.Transparent).border(1.dp, color).padding(horizontal = 5.dp, vertical = 1.dp)
    )
}

@Composable
internal fun MzEmpty(text: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(60.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("❦", color = MZ.Amber, fontSize = 40.sp)
        Text(text, color = MZ.Sub, fontSize = 17.sp, fontFamily = MZ.Serif, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
    }
}

internal fun mzClock(ms: Long): String {
    val c = java.util.Calendar.getInstance().apply { timeInMillis = ms }
    return "%02d:%02d".format(c.get(java.util.Calendar.HOUR_OF_DAY), c.get(java.util.Calendar.MINUTE))
}

internal fun mzDuration(ms: Long): String {
    val t = (ms / 1000L).coerceAtLeast(0L)
    val h = t / 3600; val m = (t % 3600) / 60; val s = t % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}

internal fun Modifier.mzKey(code: Int, handler: () -> Boolean): Modifier = this.then(
    Modifier.onPreviewKeyEvent { e -> e.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN && e.nativeKeyEvent.keyCode == code && handler() }
)
internal fun Modifier.mzRight(h: () -> Boolean) = mzKey(android.view.KeyEvent.KEYCODE_DPAD_RIGHT, h)
internal fun Modifier.mzLeft(h: () -> Boolean) = mzKey(android.view.KeyEvent.KEYCODE_DPAD_LEFT, h)
