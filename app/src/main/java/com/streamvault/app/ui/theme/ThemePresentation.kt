package com.streamvault.app.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.streamvault.domain.model.AppHomeTheme

/**
 * مواصفات عرض الثيم فقط. لا تحمل بيانات مزود أو قناة أو حالة تشغيل؛ تلك تبقى في طبقات
 * repositories وViewModels والمشغّل المشتركة بين كل الثيمات.
 */
enum class ThemeNavigationLayout {
    SIDE_RAIL,
    TOP_BAR,
    ADAPTIVE
}

enum class ThemeLiveTvLayout {
    CATEGORIES_CHANNELS_PREVIEW,
    CATEGORIES_CHANNELS,
    THEME_DEFINED
}

data class ThemeSurfaceSpec(
    val canvas: Color,
    val browseRail: Color,
    val browseContent: Color,
    val focusedSurface: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color,
    val selectedAccent: Color,
    val focusBorderWidth: Dp,
    val cornerMedium: Dp,
    val cornerLarge: Dp
)

data class ThemeFocusSpec(
    val focusedScale: Float,
    val pressedScale: Float,
    val motionDurationMs: Int
)

data class ThemePresentation(
    val id: AppHomeTheme,
    val navigationLayout: ThemeNavigationLayout,
    val liveTvLayout: ThemeLiveTvLayout,
    val surfaces: ThemeSurfaceSpec,
    val focus: ThemeFocusSpec,
    val replacesHomeWhenOpeningSections: Boolean
)

private val classicPresentation = ThemePresentation(
    id = AppHomeTheme.CLASSIC,
    navigationLayout = ThemeNavigationLayout.ADAPTIVE,
    liveTvLayout = ThemeLiveTvLayout.THEME_DEFINED,
    surfaces = ThemeSurfaceSpec(
        canvas = Color(0xFF0B1020),
        browseRail = Color(0xFF151A2D),
        browseContent = Color(0xFF111827),
        focusedSurface = Color(0xFF253047),
        textPrimary = Color(0xFFF5F7FB),
        textSecondary = Color(0xFFABB4C6),
        accent = Color(0xFF4BA3FF),
        selectedAccent = Color(0x334BA3FF),
        focusBorderWidth = 2.dp,
        cornerMedium = 14.dp,
        cornerLarge = 28.dp
    ),
    focus = ThemeFocusSpec(
        focusedScale = 1.03f,
        pressedScale = 0.98f,
        motionDurationMs = 180
    ),
    replacesHomeWhenOpeningSections = false
)

private val alaaPresentation = ThemePresentation(
    id = AppHomeTheme.ALAA,
    navigationLayout = ThemeNavigationLayout.TOP_BAR,
    liveTvLayout = ThemeLiveTvLayout.CATEGORIES_CHANNELS_PREVIEW,
    surfaces = ThemeSurfaceSpec(
        canvas = AlaaThemeColors.Canvas,
        browseRail = AlaaThemeColors.BrowseRail,
        browseContent = AlaaThemeColors.BrowseContent,
        focusedSurface = AlaaThemeColors.BrowseContentFocused,
        textPrimary = AlaaThemeColors.TextPrimary,
        textSecondary = AlaaThemeColors.TextSecondary,
        accent = AlaaThemeColors.Accent,
        selectedAccent = AlaaThemeColors.AccentMuted,
        focusBorderWidth = AlaaThemeDimensions.FocusBorder,
        cornerMedium = AlaaThemeDimensions.CornerMedium,
        cornerLarge = AlaaThemeDimensions.CornerLarge
    ),
    focus = ThemeFocusSpec(
        focusedScale = AlaaThemeFocus.FocusedScale,
        pressedScale = AlaaThemeFocus.PressedScale,
        motionDurationMs = AlaaThemeFocus.AnimationDurationMs
    ),
    replacesHomeWhenOpeningSections = true
)

/**
 * السجل الوحيد للثيمات المكتملة وظيفياً. لا يضاف ثيم إلى هذا السجل أو Selector الإعدادات
 * قبل توفير شاشاته ومشغّله وتنقله الفعليين.
 */
object ThemePresentationRegistry {
    private val fixedPresentations = mapOf(
        AppHomeTheme.CLASSIC to classicPresentation,
        AppHomeTheme.ALAA to alaaPresentation
    )
    private val additionalPresentations = mutableMapOf<AppHomeTheme, ThemePresentation>()

    fun resolve(theme: AppHomeTheme): ThemePresentation =
        fixedPresentations[theme] ?: additionalPresentations[theme]
            ?: error("No complete presentation is registered for ${theme.storageValue}")

    /**
     * نقطة التسجيل الوحيدة للثيمات الإضافية المكتملة. يمنع الحارس استبدال الثيمين
     * الثابتين حتى لا يتحول التوسّع إلى تعديل هوية Classic أو Signature Alaa.
     */
    fun registerAdditional(presentation: ThemePresentation) {
        require(!presentation.id.isFixedFoundation) {
            "Fixed theme foundations cannot be replaced: ${presentation.id.storageValue}"
        }
        require(presentation.id !in fixedPresentations) {
            "Fixed theme foundations cannot be registered as additions: ${presentation.id.storageValue}"
        }
        additionalPresentations[presentation.id] = presentation
    }
}

val LocalThemePresentation = staticCompositionLocalOf {
    ThemePresentationRegistry.resolve(AppHomeTheme.CLASSIC)
}
