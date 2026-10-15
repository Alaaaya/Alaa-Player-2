package com.streamvault.app.ui.themes.sama

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Text
import com.streamvault.app.ui.interaction.TvClickableSurface

internal val LocalSamaNavigate = staticCompositionLocalOf<(String) -> Unit> { {} }

@Composable
internal fun SamaLogo(modifier: Modifier = Modifier, compact: Boolean = false) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.size(32.dp).border(1.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center) { CgGlyph("▶", 16.dp, tint = Color.White) }
        if (!compact) Text("S A M A", color = Color.White, fontSize = 19.sp,
            fontWeight = FontWeight.Light, maxLines = 1)
    }
}

private val arDays = listOf("الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت")
private val arMonths = listOf("يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو", "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر")

internal fun cg2ArabicDate(ms: Long): String {
    val c = java.util.Calendar.getInstance().apply { timeInMillis = ms }
    return "${arDays[c.get(java.util.Calendar.DAY_OF_WEEK) - 1]} ${c.get(java.util.Calendar.DAY_OF_MONTH)} ${arMonths[c.get(java.util.Calendar.MONTH)]} ${c.get(java.util.Calendar.YEAR)}"
}

@Composable
internal fun SamaClock(modifier: Modifier = Modifier) {
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { kotlinx.coroutines.delay(20_000); now = System.currentTimeMillis() } }
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(Modifier.width(1.dp).height(34.dp).background(CG.Line))
        Column(horizontalAlignment = Alignment.End) {
            Text(cgClock(now), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM, androidx.compose.ui.platform.LocalConfiguration.current.locales[0]).format(java.util.Date(now)), color = CG.Sub, fontSize = 11.sp, maxLines = 1)
        }
    }
}

@Composable
internal fun SamaIconKey(glyph: String, onClick: () -> Unit, dot: Boolean = false, size: Dp = 44.dp) {
    TvClickableSurface(
        onClick = onClick, modifier = Modifier.size(size),
        shape = ClickableSurfaceDefaults.shape(CircleShape),
        colors = ClickableSurfaceDefaults.colors(containerColor = Color.Transparent, focusedContainerColor = CG.AmberDeep, contentColor = Color.White, focusedContentColor = Color.White),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
        glow = ClickableSurfaceDefaults.glow(focusedGlow = androidx.tv.material3.Glow(CG.Amber.copy(alpha = 0.6f), 12.dp))
    ) {
        CgGlyph(glyph, size * 0.6f, Modifier.align(Alignment.Center), tint = Color.White)
        if (dot) Box(Modifier.align(Alignment.TopEnd).padding(7.dp).size(8.dp).clip(CircleShape).background(CG.Amber))
    }
}

@Composable
internal fun SamaButton(label: String, glyph: String, onClick: () -> Unit, modifier: Modifier = Modifier, primary: Boolean = true) {
    val shape = RoundedCornerShape(50)
    TvClickableSurface(
        onClick = onClick, modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (primary) CG.Amber else Color(0x66253039), focusedContainerColor = if (primary) Color(0xFFE5EAED) else Color(0xCC35414A),
            contentColor = if (primary) Color(0xFF14191D) else Color.White, focusedContentColor = if (primary) Color(0xFF14191D) else Color.White
        ),
        border = ClickableSurfaceDefaults.border(
            border = Border(androidx.compose.foundation.BorderStroke(1.dp, if (primary) Color.Transparent else Color(0x33FFFFFF)), shape = shape),
            focusedBorder = Border(androidx.compose.foundation.BorderStroke(2.dp, Color.White), shape = shape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.06f),
        glow = ClickableSurfaceDefaults.glow(focusedGlow = androidx.tv.material3.Glow(CG.Amber.copy(alpha = 0.7f), 16.dp))
    ) {
        Row(Modifier.padding(horizontal = 22.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(label, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            CgGlyph(glyph, 20.dp, tint = androidx.tv.material3.LocalContentColor.current)
        }
    }
}

@Composable
internal fun SamaSearchBox(value: String, onChange: (String) -> Unit, hint: String, modifier: Modifier = Modifier) {
    var focused by remember { mutableStateOf(false) }
    Row(
        modifier.height(44.dp).clip(RoundedCornerShape(50)).background(Color(0x6611181E))
            .border(if (focused) 2.dp else 1.dp, if (focused) CG.Amber else Color(0x33FFFFFF), RoundedCornerShape(50)).padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CgGlyph("⌕", 20.dp, tint = if (focused) CG.Amber else CG.Sub)
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) Text(hint, color = CG.Faint, fontSize = 14.sp, maxLines = 1)
            BasicTextField(
                value = value, onValueChange = onChange, singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 15.sp), cursorBrush = SolidColor(CG.Amber),
                modifier = Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused }
            )
        }
    }
}

@Composable
internal fun SamaTag(text: String, bg: Color = CG.Amber, fg: Color = Color(0xFF14191D), outlined: Boolean = false) {
    Text(
        text, color = fg, fontSize = 10.sp, fontWeight = FontWeight.Black, maxLines = 1,
        modifier = Modifier.then(if (outlined) Modifier.border(1.dp, fg.copy(alpha = 0.8f), RoundedCornerShape(4.dp)) else Modifier.background(bg, RoundedCornerShape(4.dp)))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
internal fun SamaRating(rating: Float, size: Int = 13) {
    if (rating <= 0f) return
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        CgGlyph("star", (size + 2).dp, tint = Color(0xFFFFC107))
        Text("%.1f".format(if (rating > 10f) rating / 10f else rating), color = Color.White, fontSize = size.sp, fontWeight = FontWeight.Bold)
    }
}

internal fun Modifier.cg2Panel(): Modifier = this.clip(RoundedCornerShape(22.dp))
    .background(Brush.verticalGradient(listOf(Color(0x80253039), Color(0x5911181E))))
    .border(1.dp, Color(0x2EFFFFFF), RoundedCornerShape(22.dp))

internal fun Modifier.cg2Selected(on: Boolean): Modifier =
    if (!on) this else this.background(Brush.horizontalGradient(listOf(Color(0x55FFFFFF), Color(0x33E43742))), RoundedCornerShape(50))
        .border(1.dp, Color(0x99FFFFFF), RoundedCornerShape(50))

@Composable
internal fun SamaSectionTitle(text: String, modifier: Modifier = Modifier, onShowAll: (() -> Unit)? = null) {
    Row(modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        onShowAll?.let {
            TvClickableSurface(
                onClick = it, shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(6.dp)),
                colors = ClickableSurfaceDefaults.colors(containerColor = Color.Transparent, focusedContainerColor = CG.AmberDeep, contentColor = CG.Amber, focusedContentColor = Color.White),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1f)
            ) {
                Row(Modifier.padding(horizontal = 8.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("عرض الكل", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    CgGlyph("next", 16.dp)
                }
            }
        }
    }
}

internal object SamaServer {
    var name by androidx.compose.runtime.mutableStateOf("")
    var healthy by androidx.compose.runtime.mutableStateOf<Boolean?>(null)
}

internal object SamaRecent {
    var active by androidx.compose.runtime.mutableStateOf(false)
}

internal fun cg2QualityLabel(label: String?): String? {
    val l = label?.uppercase() ?: return null
    return when {
        "4K" in l || "UHD" in l || "2160" in l -> "4K"
        "FHD" in l || "1080" in l -> "FHD"
        Regex("\\bHD\\b").containsMatchIn(l) || "720" in l -> "HD"
        else -> null
    }
}
