package com.streamvault.app.ui.themes.universe

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

/** Cyan Premium (cyan/black glass) shared pieces: logo, clock/date block, top icons, red buttons, rounded search box. */
internal val LocalCpNavigate = staticCompositionLocalOf<(String) -> Unit> { {} }

/** Cyan rounded "play" mark + Alaa IPTV wordmark. */
@Composable
internal fun CpLogo(modifier: Modifier = Modifier, compact: Boolean = false) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        androidx.compose.foundation.Canvas(Modifier.size(30.dp, 32.dp)) {
            val p = Path().apply {
                moveTo(size.width * 0.08f, size.height * 0.06f)
                lineTo(size.width * 0.98f, size.height * 0.5f)
                lineTo(size.width * 0.08f, size.height * 0.94f)
                close()
            }
            drawPath(p, Brush.verticalGradient(listOf(Color(0xFF8EC3FF), Color(0xFF1655B0))))
            val hole = Path().apply {
                moveTo(size.width * 0.32f, size.height * 0.34f)
                lineTo(size.width * 0.62f, size.height * 0.5f)
                lineTo(size.width * 0.32f, size.height * 0.66f)
                close()
            }
            drawPath(hole, Color(0xFF0A1230))
        }
        if (!compact) Text("Alaa IPTV", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

private val arDays = listOf("الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت")
private val arMonths = listOf("يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو", "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر")

internal fun cg2ArabicDate(ms: Long): String {
    val c = java.util.Calendar.getInstance().apply { timeInMillis = ms }
    return "${arDays[c.get(java.util.Calendar.DAY_OF_WEEK) - 1]} ${c.get(java.util.Calendar.DAY_OF_MONTH)} ${arMonths[c.get(java.util.Calendar.MONTH)]} ${c.get(java.util.Calendar.YEAR)}"
}

/** Clock + Arabic date, refreshed every 20s, with a thin divider before it (like the references). */
@Composable
internal fun CpClock(modifier: Modifier = Modifier) {
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { kotlinx.coroutines.delay(20_000); now = System.currentTimeMillis() } }
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(Modifier.width(1.dp).height(34.dp).background(CG.Line))
        Column(horizontalAlignment = Alignment.End) {
            Text(cgClock(now), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(cg2ArabicDate(now), color = CG.Sub, fontSize = 11.sp, maxLines = 1)
        }
    }
}

/** Plain focusable line icon (search / bell / profile / settings) for the top bar. */
@Composable
internal fun CpIconKey(glyph: String, onClick: () -> Unit, dot: Boolean = false, size: Dp = 44.dp) {
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

/** Filled red (primary) or dark glass (secondary) pill button with a line icon. */
@Composable
internal fun CpButton(label: String, glyph: String, onClick: () -> Unit, modifier: Modifier = Modifier, primary: Boolean = true) {
    val shape = RoundedCornerShape(8.dp)
    TvClickableSurface(
        onClick = onClick, modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (primary) CG.Amber else Color(0xCC1A2A5C), focusedContainerColor = if (primary) Color(0xFF5AA2FF) else Color(0xFF2A3A70),
            contentColor = Color.White, focusedContentColor = Color.White
        ),
        border = ClickableSurfaceDefaults.border(
            border = Border(androidx.compose.foundation.BorderStroke(1.dp, if (primary) Color.Transparent else Color(0x33FFFFFF)), shape = shape),
            focusedBorder = Border(androidx.compose.foundation.BorderStroke(2.dp, Color.White), shape = shape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
        glow = ClickableSurfaceDefaults.glow(focusedGlow = androidx.tv.material3.Glow(CG.Amber.copy(alpha = 0.7f), 16.dp))
    ) {
        Row(Modifier.padding(horizontal = 22.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(label, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            CgGlyph(glyph, 20.dp, tint = Color.White)
        }
    }
}

/** Rounded outlined search box (charcoal, magnifier at the start). */
@Composable
internal fun CpSearchBox(value: String, onChange: (String) -> Unit, hint: String, modifier: Modifier = Modifier) {
    var focused by remember { mutableStateOf(false) }
    Row(
        modifier.height(44.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xCC0E1838))
            .border(if (focused) 2.dp else 1.dp, if (focused) CG.Amber else Color(0xFF2A3A70), RoundedCornerShape(10.dp)).padding(horizontal = 14.dp),
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

/** Small filled badge, e.g. red HD / جديد / LIVE, yellow 4K, outlined HDR. */
@Composable
internal fun CpTag(text: String, bg: Color = CG.Amber, fg: Color = Color.White, outlined: Boolean = false) {
    Text(
        text, color = fg, fontSize = 10.sp, fontWeight = FontWeight.Black, maxLines = 1,
        modifier = Modifier.then(if (outlined) Modifier.border(1.dp, fg.copy(alpha = 0.8f), RoundedCornerShape(4.dp)) else Modifier.background(bg, RoundedCornerShape(4.dp)))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
internal fun CpRating(rating: Float, size: Int = 13) {
    if (rating <= 0f) return
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        CgGlyph("star", (size + 2).dp, tint = Color(0xFFFFC107))
        Text("%.1f".format(if (rating > 10f) rating / 10f else rating), color = Color.White, fontSize = size.sp, fontWeight = FontWeight.Bold)
    }
}

/** Panel background used for the three columns / sidebars. */
internal fun Modifier.cg2Panel(): Modifier = this.clip(RoundedCornerShape(14.dp))
    .background(Brush.verticalGradient(listOf(Color(0xE6101A44), Color(0xE60A1230))))
    .border(1.dp, Color(0xFF24346E), RoundedCornerShape(14.dp))

/** Selected-row look: dark-red fill with red glowing border. */
internal fun Modifier.cg2Selected(on: Boolean): Modifier =
    if (!on) this else this.background(Brush.horizontalGradient(listOf(Color(0xFF0A2A6A), Color(0xFF1655B0))), RoundedCornerShape(10.dp))
        .border(1.5.dp, Color(0xFF8EC3FF), RoundedCornerShape(10.dp))

@Composable
internal fun CpSectionTitle(text: String, modifier: Modifier = Modifier, onShowAll: (() -> Unit)? = null) {
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

/** Active server name/health, published by the dashboard (which owns provider state) for the shell card. */
internal object CpServer {
    var name by androidx.compose.runtime.mutableStateOf("")
    var healthy by androidx.compose.runtime.mutableStateOf<Boolean?>(null)
}

/** Whether the saved-library route was opened from "المشاهدة الأخيرة" (shows the real history view). */
internal object UvRecent {
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
