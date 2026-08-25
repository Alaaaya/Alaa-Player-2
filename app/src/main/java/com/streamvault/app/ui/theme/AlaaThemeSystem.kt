package com.streamvault.app.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.streamvault.domain.model.AppHomeTheme

/**
 * طبقة العرض الخاصة بثيم Alaa. لا تحتوي هذه القيم على أي منطق IPTV أو حالة تنقل؛
 * هي رموز بصرية مشتركة تستعملها الواجهة عند اختيار الثيم من الإعدادات.
 */
object AlaaThemeColors {
    val Canvas = Color(0xFF050712)
    val CanvasRaised = Color(0xFF080B18)
    val Sidebar = Color(0xFF090C17)
    val Surface = Color(0xFF0D1020)
    val SurfaceElevated = Color(0xFF151A2D)
    val SurfaceFocused = Color(0xFF261327)
    val Accent = Color(0xFFFF3150)
    val AccentStrong = Color(0xFFFF5270)
    val AccentMuted = Color(0x33FF3150)
    val TextPrimary = Color(0xFFF8F7FB)
    val TextSecondary = Color(0xFFB6B6C5)
    val TextTertiary = Color(0xFF77788B)
    val Outline = Color(0x52FF3150)
    val HeroBlue = Color(0xFF0B2242)
    val HeroRed = Color(0xFF641026)
}

object AlaaThemeDimensions {
    val RailWidth = 276.dp
    val RailPadding = 20.dp
    val ContentPadding = 34.dp
    val SectionGap = 24.dp
    val CornerLarge = 22.dp
    val CornerMedium = 16.dp
    val FocusBorder = 2.dp
}

val LocalAppHomeTheme = staticCompositionLocalOf { AppHomeTheme.CLASSIC }

val LocalIsAlaaTheme = staticCompositionLocalOf { false }
