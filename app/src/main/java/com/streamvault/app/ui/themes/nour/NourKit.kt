package com.streamvault.app.ui.themes.nour

import com.streamvault.app.ui.themes.bespoke.onDpadToward
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

internal object NR {
    val Bg = Color(0xFF070A0D)
    val Raised = Color(0xCC20282F)
    val Card = Color(0xB31A2229)
    val Line = Color(0xFF3A444C)
    val Text = Color(0xFFFFFFFF)
    val Sub = Color(0xFFC4CBD2)
    val Faint = Color(0xFF929DA6)
    val Amber = Color(0xFFFFFFFF)
    val AmberDeep = Color(0xFFDCE5EC)
    val Live = Color(0xFFFF626C)
    val Blue = Color(0xFFB8CBD8)
    val Rose = Color(0xFF758795)
    val R = RoundedCornerShape(24.dp)
    val RSmall = RoundedCornerShape(14.dp)
    val Pill = RoundedCornerShape(50)
}

@Composable
internal fun NourBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier.fillMaxSize().background(NR.Bg)) {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF101A22), NR.Bg, NR.Bg))))
        content()
    }
}

@Composable
internal fun NourCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = NR.R,
    container: Color = NR.Card,
    focusedContainer: Color = NR.Line,
    onLongClick: (() -> Unit)? = null,
    zoom: Float = 1.05f,
    content: @Composable BoxScope.() -> Unit
) {
    TvClickableSurface(
        onClick = onClick, onLongClick = onLongClick, modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = container, focusedContainerColor = focusedContainer, contentColor = NR.Text, focusedContentColor = NR.Text),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(androidx.compose.foundation.BorderStroke(2.dp, NR.Amber), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = zoom),
        glow = ClickableSurfaceDefaults.glow(focusedGlow = androidx.tv.material3.Glow(NR.Amber.copy(alpha = 0.55f), 18.dp)),
        content = content
    )
}

@Composable
internal fun NourButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, primary: Boolean = false, icon: String? = null) {
    TvClickableSurface(
        onClick = onClick, modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(NR.Pill),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (primary) NR.Amber else NR.Line.copy(alpha = 0.7f),
            focusedContainerColor = if (primary) Color(0xFFE5EBF0) else NR.Amber,
            contentColor = if (primary) NR.Bg else NR.Text, focusedContentColor = NR.Bg
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.06f),
        glow = ClickableSurfaceDefaults.glow(focusedGlow = androidx.tv.material3.Glow(NR.Amber.copy(alpha = 0.5f), 14.dp))
    ) {
        Row(Modifier.padding(horizontal = 26.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            icon?.let { Text(it, fontSize = 15.sp, fontWeight = FontWeight.Bold) }
            Text(label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, letterSpacing = 0.4.sp)
        }
    }
}

@Composable
internal fun NourRound(glyph: String, onClick: () -> Unit, modifier: Modifier = Modifier, size: Dp = 56.dp, active: Boolean = false) {
    TvClickableSurface(
        onClick = onClick, modifier = modifier.size(size),
        shape = ClickableSurfaceDefaults.shape(CircleShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (active) NR.Amber else NR.Line.copy(alpha = 0.75f), focusedContainerColor = NR.Amber,
            contentColor = if (active) NR.Bg else NR.Text, focusedContentColor = NR.Bg
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.14f),
        glow = ClickableSurfaceDefaults.glow(focusedGlow = androidx.tv.material3.Glow(NR.Amber.copy(alpha = 0.6f), 16.dp))
    ) { Text(glyph, fontSize = (size.value / 2.6f).sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center)) }
}

@Composable
internal fun NourTab(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TvClickableSurface(
        onClick = onClick, modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(NR.Pill),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) NR.Line else Color.Transparent, focusedContainerColor = NR.Amber,
            contentColor = if (selected) NR.Amber else NR.Sub, focusedContentColor = NR.Bg
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
        glow = ClickableSurfaceDefaults.glow()
    ) {
        Row(Modifier.padding(horizontal = 18.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (selected) Box(Modifier.size(6.dp).clip(CircleShape).background(NR.Amber))
            Text(label, fontSize = 14.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, maxLines = 1)
        }
    }
}

@Composable
internal fun NourRowTitle(text: String, modifier: Modifier = Modifier, trailing: String? = null) {
    Row(modifier.padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text, color = NR.Text, fontSize = 21.sp, fontWeight = FontWeight.SemiBold, fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif)
        trailing?.let { Text(it, color = NR.Amber, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
        Box(Modifier.width(120.dp).height(1.dp).background(Brush.horizontalGradient(listOf(NR.Amber.copy(alpha = 0.6f), Color.Transparent))))
    }
}

@Composable
internal fun NourProgress(progress: Float, modifier: Modifier = Modifier, height: Dp = 4.dp, track: Color = NR.Line) {
    Box(modifier.fillMaxWidth().height(height).clip(NR.Pill).background(track)) {
        Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).height(height).clip(NR.Pill).background(Brush.horizontalGradient(listOf(NR.Rose, NR.Amber))))
    }
}

@Composable
internal fun NourPoster(title: String, imageUrl: String?, caption: String?, onClick: () -> Unit, modifier: Modifier = Modifier, locked: Boolean = false, onLongClick: (() -> Unit)? = null) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        NourCard(onClick = onClick, onLongClick = onLongClick, shape = NR.RSmall, modifier = Modifier.fillMaxWidth().aspectRatio(2f / 3f)) {
            if (!locked && imageUrl != null) AsyncImage(imageUrl, title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            else Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(NR.Rose.copy(alpha = 0.35f), NR.Line, NR.Blue.copy(alpha = 0.25f)))), contentAlignment = Alignment.Center) {
                Text(if (locked) "🔒" else title.take(1).uppercase(), color = NR.Sub, fontSize = 34.sp, fontWeight = FontWeight.Black)
            }
        }
        Text(title, color = NR.Text, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth())
        caption?.takeIf { it.isNotBlank() }?.let { Text(it, color = NR.Amber.copy(alpha = 0.8f), fontSize = 11.sp, maxLines = 1, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
    }
}

@Composable
internal fun NourWide(title: String, imageUrl: String?, sub: String?, progress: Float?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        NourCard(onClick = onClick, shape = NR.RSmall, modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
            imageUrl?.let { AsyncImage(it, title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, NR.Bg.copy(alpha = 0.85f)))))
            Box(Modifier.align(Alignment.Center).size(44.dp).clip(CircleShape).background(NR.Amber.copy(alpha = 0.9f)), contentAlignment = Alignment.Center) { Text("▶", color = NR.Bg, fontSize = 18.sp) }
            progress?.let { NourProgress(it, Modifier.align(Alignment.BottomCenter).padding(10.dp), 4.dp) }
        }
        Text(title, color = NR.Text, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        sub?.let { Text(it, color = NR.Faint, fontSize = 11.sp, maxLines = 1) }
    }
}

@Composable
internal fun NourLogo(name: String, logoUrl: String?, size: Dp, modifier: Modifier = Modifier) {
    Box(modifier.size(size).clip(CircleShape).background(Brush.radialGradient(listOf(NR.Line, NR.Raised))).border(1.dp, NR.Amber.copy(alpha = 0.35f), CircleShape), contentAlignment = Alignment.Center) {
        Text(name.take(2).uppercase(), color = NR.Sub, fontSize = (size.value / 3.2f).sp, fontWeight = FontWeight.Bold)
        logoUrl?.let { AsyncImage(it, name, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(size / 5)) }
    }
}

@Composable
internal fun NourBadge(text: String, color: Color = NR.Amber, filled: Boolean = false) {
    Text(
        text, color = if (filled) NR.Bg else color, fontSize = 10.sp, fontWeight = FontWeight.Black, maxLines = 1,
        modifier = Modifier.clip(NR.Pill).background(if (filled) color else color.copy(alpha = 0.16f)).padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

@Composable
internal fun NourEmpty(text: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(60.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("◈", color = NR.Amber, fontSize = 44.sp)
        Text(text, color = NR.Sub, fontSize = 16.sp)
    }
}

internal fun nourClock(ms: Long): String {
    val c = java.util.Calendar.getInstance().apply { timeInMillis = ms }
    return "%02d:%02d".format(c.get(java.util.Calendar.HOUR_OF_DAY), c.get(java.util.Calendar.MINUTE))
}

internal fun nourDuration(ms: Long): String {
    val t = (ms / 1000L).coerceAtLeast(0L)
    val h = t / 3600; val m = (t % 3600) / 60; val s = t % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}

internal fun Modifier.nourKey(code: Int, handler: () -> Boolean): Modifier = this.then(
    Modifier.onPreviewKeyEvent { e -> e.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN && e.nativeKeyEvent.keyCode == code && handler() }
)
internal fun Modifier.nourRight(h: () -> Boolean): Modifier = onDpadToward(end = true, h)
internal fun Modifier.nourLeft(h: () -> Boolean): Modifier = onDpadToward(end = false, h)
