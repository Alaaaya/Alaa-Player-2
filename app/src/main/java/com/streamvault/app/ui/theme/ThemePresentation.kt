package com.streamvault.app.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.streamvault.domain.model.AppHomeTheme
import com.streamvault.app.ui.themes.minimal.MinimalCanvas
import com.streamvault.app.ui.themes.minimal.MinimalFocus
import com.streamvault.app.ui.themes.minimal.MinimalFocusMotionMs
import com.streamvault.app.ui.themes.minimal.MinimalMuted
import com.streamvault.app.ui.themes.minimal.MinimalPaper
import com.streamvault.app.ui.themes.minimal.MinimalText
import com.streamvault.app.ui.themes.glass.GlassAccent
import com.streamvault.app.ui.themes.glass.GlassCanvas
import com.streamvault.app.ui.themes.glass.GlassFocus
import com.streamvault.app.ui.themes.glass.GlassFocusMotionMs
import com.streamvault.app.ui.themes.glass.GlassMuted
import com.streamvault.app.ui.themes.glass.GlassPane
import com.streamvault.app.ui.themes.glass.GlassPaneFocused
import com.streamvault.app.ui.themes.glass.GlassText
import com.streamvault.app.ui.themes.streaming.StreamingAccent
import com.streamvault.app.ui.themes.streaming.StreamingCanvas
import com.streamvault.app.ui.themes.streaming.StreamingFocus
import com.streamvault.app.ui.themes.streaming.StreamingFocusMotionMs
import com.streamvault.app.ui.themes.streaming.StreamingMuted
import com.streamvault.app.ui.themes.streaming.StreamingPanel
import com.streamvault.app.ui.themes.streaming.StreamingPanelFocused
import com.streamvault.app.ui.themes.streaming.StreamingText
import com.streamvault.app.ui.themes.premium.PremiumCanvas
import com.streamvault.app.ui.themes.premium.PremiumFocus
import com.streamvault.app.ui.themes.premium.PremiumFocusMotionMs
import com.streamvault.app.ui.themes.premium.PremiumGold
import com.streamvault.app.ui.themes.premium.PremiumMuted
import com.streamvault.app.ui.themes.premium.PremiumPanel
import com.streamvault.app.ui.themes.premium.PremiumPanelFocused
import com.streamvault.app.ui.themes.premium.PremiumText

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

data class ThemeCatalogEntry(
    val theme: AppHomeTheme,
    val title: String,
    val description: String
)

object ThemeCatalog {
    private val entries = listOf(
        ThemeCatalogEntry(AppHomeTheme.CLASSIC, "Classic", "واجهة Alaa Player الافتراضية الحالية."),
        ThemeCatalogEntry(AppHomeTheme.ALAA, "Signature Alaa", "هوية Alaa الخاصة الثابتة."),
        ThemeCatalogEntry(AppHomeTheme.CINEMATIC, "Cinematic", "تجربة سينمائية بلوحات عرض كبيرة."),
        ThemeCatalogEntry(AppHomeTheme.NEON_FUTURE, "Neon Future", "تجربة نيون مستقبلية بلوحات HUD."),
        ThemeCatalogEntry(AppHomeTheme.MINIMAL, "Minimal", "تجربة بسيطة بطباعة قوية ومساحات هادئة."),
        ThemeCatalogEntry(AppHomeTheme.GLASSMORPHISM, "Glassmorphism", "تجربة زجاجية بطبقات شفافة."),
        ThemeCatalogEntry(AppHomeTheme.STREAMING_PLATFORM, "Streaming Platform", "تجربة صفوف محتوى وHero واسع."),
        ThemeCatalogEntry(AppHomeTheme.PREMIUM_BLACK, "Premium Black", "تجربة سوداء فاخرة بمؤشرات معدنية."),
        ThemeCatalogEntry(AppHomeTheme.BLUE_OCEAN, "Blue Ocean", "تجربة محيطية حديثة."),
        ThemeCatalogEntry(AppHomeTheme.RED_CINEMA, "Red Cinema", "تجربة سينمائية درامية."),
        ThemeCatalogEntry(AppHomeTheme.PURPLE_GALAXY, "Purple Galaxy", "تجربة فضائية بنفسجية."),
        ThemeCatalogEntry(AppHomeTheme.TECH_DASHBOARD, "Tech Dashboard", "تجربة معلومات وEPG كثيفة."),
        ThemeCatalogEntry(AppHomeTheme.MODERN_TV, "Modern TV", "تجربة تلفاز حديثة عالية الوضوح."),
        ThemeCatalogEntry(AppHomeTheme.CARD_STACK, "Card Stack", "تجربة بطاقات طبقية."),
        ThemeCatalogEntry(AppHomeTheme.MEDIA_CENTER, "Media Center", "تجربة مركز وسائط منظمة."),
        ThemeCatalogEntry(AppHomeTheme.FUTURISTIC_HUD, "Futuristic HUD", "تجربة HUD تقنية."),
        ThemeCatalogEntry(AppHomeTheme.SOFT_MODERN, "Soft Modern", "تجربة هادئة وتدرجات مريحة."),
        ThemeCatalogEntry(AppHomeTheme.SPORTS_TV, "Sports TV", "تجربة متابعة رياضية مباشرة."),
        ThemeCatalogEntry(AppHomeTheme.DARK_GLASS, "Dark Glass", "تجربة زجاج داكن مضيئة."),
        ThemeCatalogEntry(AppHomeTheme.MAGAZINE_MEDIA, "Magazine Media", "تجربة تحريرية غنية بالصور."),
        ThemeCatalogEntry(AppHomeTheme.NEXT_GEN_TV, "Next Gen TV", "تجربة تلفاز مكانية متقدمة."),
        ThemeCatalogEntry(AppHomeTheme.AURORA_LOUNGE, "Aurora Lounge", "تجربة صالة ليلية دافئة.")
    )

    fun entry(theme: AppHomeTheme): ThemeCatalogEntry =
        entries.first { it.theme == theme }

    fun selectableEntries(): List<ThemeCatalogEntry> =
        ThemePresentationRegistry.selectableThemes().map(::entry)
}

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

private val cinematicPresentation = ThemePresentation(
    id = AppHomeTheme.CINEMATIC,
    navigationLayout = ThemeNavigationLayout.SIDE_RAIL,
    liveTvLayout = ThemeLiveTvLayout.CATEGORIES_CHANNELS_PREVIEW,
    surfaces = ThemeSurfaceSpec(
        canvas = Color(0xFF08070B),
        browseRail = Color(0xFF151016),
        browseContent = Color(0xFF151016),
        focusedSurface = Color(0xFF261925),
        textPrimary = Color(0xFFF7F1F2),
        textSecondary = Color(0xFFB9ACB1),
        accent = Color(0xFFF0C98A),
        selectedAccent = Color(0x73D74457),
        focusBorderWidth = 2.dp,
        cornerMedium = 18.dp,
        cornerLarge = 28.dp
    ),
    focus = ThemeFocusSpec(
        focusedScale = 1.03f,
        pressedScale = 0.98f,
        motionDurationMs = 180
    ),
    replacesHomeWhenOpeningSections = true
)

private val neonFuturePresentation = ThemePresentation(
    id = AppHomeTheme.NEON_FUTURE,
    navigationLayout = ThemeNavigationLayout.SIDE_RAIL,
    liveTvLayout = ThemeLiveTvLayout.CATEGORIES_CHANNELS_PREVIEW,
    surfaces = ThemeSurfaceSpec(
        canvas = Color(0xFF040812),
        browseRail = Color(0xFF0A1324),
        browseContent = Color(0xFF0A1324),
        focusedSurface = Color(0xFF10243B),
        textPrimary = Color(0xFFE8FCFF),
        textSecondary = Color(0xFF8EAEBD),
        accent = Color(0xFF5BF4FF),
        selectedAccent = Color(0x295BF4FF),
        focusBorderWidth = 2.dp,
        cornerMedium = 8.dp,
        cornerLarge = 18.dp
    ),
    focus = ThemeFocusSpec(
        focusedScale = 1.018f,
        pressedScale = 0.98f,
        motionDurationMs = 160
    ),
    replacesHomeWhenOpeningSections = true
)

private val minimalPresentation = ThemePresentation(
    id = AppHomeTheme.MINIMAL,
    navigationLayout = ThemeNavigationLayout.SIDE_RAIL,
    liveTvLayout = ThemeLiveTvLayout.CATEGORIES_CHANNELS_PREVIEW,
    surfaces = ThemeSurfaceSpec(
        canvas = MinimalCanvas,
        browseRail = MinimalPaper,
        browseContent = MinimalPaper,
        focusedSurface = MinimalCanvas,
        textPrimary = MinimalText,
        textSecondary = MinimalMuted,
        accent = MinimalFocus,
        selectedAccent = MinimalFocus.copy(alpha = 0.12f),
        focusBorderWidth = 1.dp,
        cornerMedium = 0.dp,
        cornerLarge = 0.dp
    ),
    focus = ThemeFocusSpec(
        focusedScale = 1f,
        pressedScale = 1f,
        motionDurationMs = MinimalFocusMotionMs
    ),
    replacesHomeWhenOpeningSections = true
)

private val glassmorphismPresentation = ThemePresentation(
    id = AppHomeTheme.GLASSMORPHISM,
    navigationLayout = ThemeNavigationLayout.SIDE_RAIL,
    liveTvLayout = ThemeLiveTvLayout.CATEGORIES_CHANNELS_PREVIEW,
    surfaces = ThemeSurfaceSpec(
        canvas = GlassCanvas,
        browseRail = GlassPane,
        browseContent = GlassPane,
        focusedSurface = GlassPaneFocused,
        textPrimary = GlassText,
        textSecondary = GlassMuted,
        accent = GlassAccent,
        selectedAccent = GlassAccent.copy(alpha = 0.18f),
        focusBorderWidth = 2.dp,
        cornerMedium = 18.dp,
        cornerLarge = 28.dp
    ),
    focus = ThemeFocusSpec(
        focusedScale = 1.03f,
        pressedScale = 0.98f,
        motionDurationMs = GlassFocusMotionMs
    ),
    replacesHomeWhenOpeningSections = true
)

private val streamingPlatformPresentation = ThemePresentation(
    id = AppHomeTheme.STREAMING_PLATFORM,
    navigationLayout = ThemeNavigationLayout.TOP_BAR,
    liveTvLayout = ThemeLiveTvLayout.CATEGORIES_CHANNELS_PREVIEW,
    surfaces = ThemeSurfaceSpec(
        canvas = StreamingCanvas,
        browseRail = StreamingPanel,
        browseContent = StreamingPanel,
        focusedSurface = StreamingPanelFocused,
        textPrimary = StreamingText,
        textSecondary = StreamingMuted,
        accent = StreamingAccent,
        selectedAccent = StreamingAccent.copy(alpha = 0.18f),
        focusBorderWidth = 2.dp,
        cornerMedium = 12.dp,
        cornerLarge = 24.dp
    ),
    focus = ThemeFocusSpec(
        focusedScale = 1.025f,
        pressedScale = 0.98f,
        motionDurationMs = StreamingFocusMotionMs
    ),
    replacesHomeWhenOpeningSections = true
)

private val premiumBlackPresentation = ThemePresentation(
    id = AppHomeTheme.PREMIUM_BLACK,
    navigationLayout = ThemeNavigationLayout.SIDE_RAIL,
    liveTvLayout = ThemeLiveTvLayout.CATEGORIES_CHANNELS_PREVIEW,
    surfaces = ThemeSurfaceSpec(
        canvas = PremiumCanvas,
        browseRail = PremiumPanel,
        browseContent = PremiumPanel,
        focusedSurface = PremiumPanelFocused,
        textPrimary = PremiumText,
        textSecondary = PremiumMuted,
        accent = PremiumGold,
        selectedAccent = PremiumGold.copy(alpha = 0.16f),
        focusBorderWidth = 1.dp,
        cornerMedium = 4.dp,
        cornerLarge = 8.dp
    ),
    focus = ThemeFocusSpec(
        focusedScale = 1.02f,
        pressedScale = 0.98f,
        motionDurationMs = PremiumFocusMotionMs
    ),
    replacesHomeWhenOpeningSections = true
)

private fun futurePresentation(
    id: AppHomeTheme,
    canvas: Color,
    rail: Color,
    content: Color,
    focused: Color,
    text: Color,
    muted: Color,
    accent: Color,
    navigation: ThemeNavigationLayout,
    cornerMedium: Dp,
    cornerLarge: Dp,
    focusedScale: Float,
    motionDurationMs: Int
): ThemePresentation = ThemePresentation(
    id = id,
    navigationLayout = navigation,
    liveTvLayout = ThemeLiveTvLayout.THEME_DEFINED,
    surfaces = ThemeSurfaceSpec(
        canvas = canvas,
        browseRail = rail,
        browseContent = content,
        focusedSurface = focused,
        textPrimary = text,
        textSecondary = muted,
        accent = accent,
        selectedAccent = accent.copy(alpha = 0.18f),
        focusBorderWidth = 2.dp,
        cornerMedium = cornerMedium,
        cornerLarge = cornerLarge
    ),
    focus = ThemeFocusSpec(
        focusedScale = focusedScale,
        pressedScale = 0.98f,
        motionDurationMs = motionDurationMs
    ),
    replacesHomeWhenOpeningSections = true
)

private val expansionPresentations = listOf(
    futurePresentation(AppHomeTheme.BLUE_OCEAN, Color(0xFF061923), Color(0xFF0B2A39), Color(0xFF0D2230), Color(0xFF164A5F), Color(0xFFE7FAFF), Color(0xFF91B8C7), Color(0xFF4DD2E8), ThemeNavigationLayout.SIDE_RAIL, 18.dp, 34.dp, 1.025f, 190),
    futurePresentation(AppHomeTheme.RED_CINEMA, Color(0xFF19090C), Color(0xFF2A0B12), Color(0xFF241014), Color(0xFF521522), Color(0xFFFFF2F0), Color(0xFFD3A5A8), Color(0xFFE44252), ThemeNavigationLayout.TOP_BAR, 0.dp, 0.dp, 1.018f, 210),
    futurePresentation(AppHomeTheme.PURPLE_GALAXY, Color(0xFF100A22), Color(0xFF1F133A), Color(0xFF181130), Color(0xFF3A2362), Color(0xFFF5F0FF), Color(0xFFC2B2DD), Color(0xFFA87CFF), ThemeNavigationLayout.SIDE_RAIL, 28.dp, 56.dp, 1.03f, 220),
    futurePresentation(AppHomeTheme.TECH_DASHBOARD, Color(0xFF07100F), Color(0xFF0D1E1B), Color(0xFF0A1815), Color(0xFF123B32), Color(0xFFE7FFF7), Color(0xFF8BBDB0), Color(0xFF48E0A4), ThemeNavigationLayout.SIDE_RAIL, 4.dp, 8.dp, 1.012f, 140),
    futurePresentation(AppHomeTheme.MODERN_TV, Color(0xFF101114), Color(0xFF1B1D23), Color(0xFF15171B), Color(0xFF30333C), Color(0xFFF8F7F4), Color(0xFFB9BBC4), Color(0xFFFFB74A), ThemeNavigationLayout.TOP_BAR, 16.dp, 28.dp, 1.03f, 180),
    futurePresentation(AppHomeTheme.CARD_STACK, Color(0xFF141116), Color(0xFF221A25), Color(0xFF1B1720), Color(0xFF44314A), Color(0xFFFFF4FD), Color(0xFFCBB7C7), Color(0xFFFF6AAE), ThemeNavigationLayout.SIDE_RAIL, 22.dp, 36.dp, 1.04f, 240),
    futurePresentation(AppHomeTheme.MEDIA_CENTER, Color(0xFF17130D), Color(0xFF2B2216), Color(0xFF211B12), Color(0xFF4D3C22), Color(0xFFFFF7E8), Color(0xFFD0BE9C), Color(0xFFE7B84F), ThemeNavigationLayout.SIDE_RAIL, 8.dp, 14.dp, 1.02f, 170),
    futurePresentation(AppHomeTheme.FUTURISTIC_HUD, Color(0xFF050C16), Color(0xFF0B1727), Color(0xFF08111F), Color(0xFF12335A), Color(0xFFE6F4FF), Color(0xFF86B0D5), Color(0xFF6AA9FF), ThemeNavigationLayout.SIDE_RAIL, 999.dp, 24.dp, 1.015f, 120),
    futurePresentation(AppHomeTheme.SOFT_MODERN, Color(0xFFF1EEE9), Color(0xFFE3DED6), Color(0xFFFAF7F2), Color(0xFFD9E7DA), Color(0xFF26342A), Color(0xFF69766C), Color(0xFF3D8660), ThemeNavigationLayout.TOP_BAR, 20.dp, 40.dp, 1.015f, 200),
    futurePresentation(AppHomeTheme.SPORTS_TV, Color(0xFF0A1620), Color(0xFF102736), Color(0xFF0D1E2A), Color(0xFF16465D), Color(0xFFF4FBFF), Color(0xFFA9C6D3), Color(0xFF9FE231), ThemeNavigationLayout.TOP_BAR, 10.dp, 18.dp, 1.035f, 155),
    futurePresentation(AppHomeTheme.DARK_GLASS, Color(0xFF080A12), Color(0xFF141827), Color(0xFF0E1120), Color(0xFF263049), Color(0xFFF2F5FF), Color(0xFFB0B8CF), Color(0xFFB69CFF), ThemeNavigationLayout.SIDE_RAIL, 24.dp, 44.dp, 1.022f, 190),
    futurePresentation(AppHomeTheme.MAGAZINE_MEDIA, Color(0xFFF2EFE8), Color(0xFFE2DDD2), Color(0xFFFBF8F2), Color(0xFFE7D7B7), Color(0xFF29251F), Color(0xFF766E61), Color(0xFFC2572F), ThemeNavigationLayout.TOP_BAR, 0.dp, 0.dp, 1.01f, 210),
    futurePresentation(AppHomeTheme.NEXT_GEN_TV, Color(0xFF0B0D13), Color(0xFF151824), Color(0xFF10131E), Color(0xFF273254), Color(0xFFF1F5FF), Color(0xFFAEB9D1), Color(0xFF61C4FF), ThemeNavigationLayout.SIDE_RAIL, 12.dp, 26.dp, 1.05f, 160),
    futurePresentation(AppHomeTheme.AURORA_LOUNGE, Color(0xFF0C0B19), Color(0xFF19152C), Color(0xFF141126), Color(0xFF332854), Color(0xFFF8F3FF), Color(0xFFC4B6D2), Color(0xFFE6AD68), ThemeNavigationLayout.TOP_BAR, 32.dp, 64.dp, 1.018f, 280)
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

    init {
        // Registration is deterministic: additions reach Settings only after their complete
        // presentation surface has been implemented and tested.
        registerAdditional(cinematicPresentation)
        registerAdditional(neonFuturePresentation)
        registerAdditional(minimalPresentation)
        registerAdditional(glassmorphismPresentation)
        registerAdditional(streamingPlatformPresentation)
        registerAdditional(premiumBlackPresentation)
        registerAdditional(expansionPresentations.first { it.id == AppHomeTheme.RED_CINEMA })
    }

    fun resolve(theme: AppHomeTheme): ThemePresentation =
        fixedPresentations[theme] ?: additionalPresentations[theme]
            ?: error("No complete presentation is registered for ${theme.storageValue}")

    fun resolveOrClassic(theme: AppHomeTheme): ThemePresentation =
        fixedPresentations[theme] ?: additionalPresentations[theme] ?: classicPresentation

    fun selectableThemes(): List<AppHomeTheme> =
        (fixedPresentations.keys + additionalPresentations.keys)
            .sortedBy(AppHomeTheme::ordinal)

    fun isSelectable(theme: AppHomeTheme): Boolean = theme in fixedPresentations || theme in additionalPresentations

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
