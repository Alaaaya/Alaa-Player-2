package com.streamvault.app.ui.theme

import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.lerp
import androidx.tv.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.tv.material3.ColorScheme
import androidx.tv.material3.darkColorScheme
import androidx.tv.material3.lightColorScheme
import com.streamvault.app.ui.design.AppColors
import com.streamvault.app.ui.design.AppPalette
import com.streamvault.app.ui.design.AppShapes
import com.streamvault.app.ui.design.LocalAppShapes
import com.streamvault.app.ui.design.LocalAppSpacing
import com.streamvault.app.ui.design.rememberAppTypography
import com.streamvault.domain.model.AppHomeTheme

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.ClassicPalette.brand,
    onPrimary = OnPrimary,
    surface = AppColors.ClassicPalette.surface,
    onSurface = AppColors.ClassicPalette.textPrimary,
    surfaceVariant = AppColors.ClassicPalette.surfaceElevated,
    onSurfaceVariant = AppColors.ClassicPalette.textSecondary,
    background = AppColors.ClassicPalette.canvasElevated,
    onBackground = AppColors.ClassicPalette.textPrimary,
    error = AppColors.Live,
    onError = OnPrimary
)

private val AlaaColorScheme = darkColorScheme(
    primary = AlaaThemeColors.Accent,
    onPrimary = AlaaThemeColors.TextPrimary,
    surface = AlaaThemeColors.Surface,
    onSurface = AlaaThemeColors.TextPrimary,
    surfaceVariant = AlaaThemeColors.SurfaceElevated,
    onSurfaceVariant = AlaaThemeColors.TextSecondary,
    background = AlaaThemeColors.Canvas,
    onBackground = AlaaThemeColors.TextPrimary,
    error = AlaaThemeColors.AccentStrong,
    onError = AlaaThemeColors.TextPrimary
)

/**
 * Maps a presentation palette into the shared TV Material scheme so shared controls
 * follow the selected theme. Classic and Alaa keep their fixed foundations.
 */
internal fun ThemePresentation.toColorScheme(): ColorScheme {
    val s = surfaces
    val isLight = s.canvas.luminance() > 0.5f
    val onAccent = if (s.accent.luminance() > 0.45f) Color(0xFF101010) else Color.White
    val error = if (isLight) Color(0xFFB3261E) else AppColors.Live
    return if (isLight) {
        lightColorScheme(
            primary = s.accent, onPrimary = onAccent,
            secondary = s.selectedAccent, onSecondary = onAccent,
            surface = s.browseContent, onSurface = s.textPrimary,
            surfaceVariant = s.focusedSurface, onSurfaceVariant = s.textSecondary,
            background = s.canvas, onBackground = s.textPrimary,
            border = s.textSecondary, error = error, onError = Color.White
        )
    } else {
        darkColorScheme(
            primary = s.accent, onPrimary = onAccent,
            secondary = s.selectedAccent, onSecondary = onAccent,
            surface = s.browseContent, onSurface = s.textPrimary,
            surfaceVariant = s.focusedSurface, onSurfaceVariant = s.textSecondary,
            background = s.canvas, onBackground = s.textPrimary,
            border = s.textSecondary, error = error, onError = Color.White
        )
    }
}

/** Shared-screen palette derived from a presentation's surface tokens. */
internal fun ThemePresentation.toAppPalette(): AppPalette {
    val s = surfaces
    val isLight = s.canvas.luminance() > 0.5f
    return AppPalette(
        canvas = s.canvas,
        canvasElevated = s.browseRail,
        surface = s.browseContent,
        surfaceElevated = lerp(s.browseContent, s.focusedSurface, 0.45f),
        surfaceEmphasis = s.focusedSurface,
        surfaceAccent = lerp(s.focusedSurface, s.accent, 0.22f),
        brand = s.accent,
        brandMuted = s.accent.copy(alpha = 0.2f),
        brandStrong = s.selectedAccent,
        focus = if (isLight) s.accent else s.textPrimary,
        textPrimary = s.textPrimary,
        textSecondary = s.textSecondary,
        textTertiary = lerp(s.textSecondary, s.canvas, 0.3f),
        textDisabled = lerp(s.textSecondary, s.canvas, 0.55f),
        divider = s.textPrimary.copy(alpha = 0.1f),
        outline = s.textSecondary.copy(alpha = 0.18f),
        heroTop = s.canvas.copy(alpha = 0.8f),
        heroBottom = s.canvas.copy(alpha = 0.95f),
        isLight = isLight
    )
}

/** Fixed and pre-existing themes keep the stock radii; palette-driven themes use their tokens. */
internal fun ThemePresentation.toAppShapes(): AppShapes {
    if (id.isFixedFoundation) return AppShapes()
    val m = surfaces.cornerMedium.coerceAtMost(28.dp)
    val l = surfaces.cornerLarge.coerceAtMost(40.dp)
    return AppShapes(
        small = RoundedCornerShape(m * 0.66f),
        medium = RoundedCornerShape(m),
        large = RoundedCornerShape(l * 0.7f),
        xSmall = RoundedCornerShape(m * 0.44f),
        xLarge = RoundedCornerShape(l)
    )
}

@Composable
fun StreamVaultTheme(
    appHomeTheme: AppHomeTheme = AppHomeTheme.CLASSIC,
    content: @Composable () -> Unit
) {
    val typography = rememberAppTypography()
    val presentation = ThemePresentationRegistry.resolveOrClassic(appHomeTheme)
    val isAlaa = presentation.id == AppHomeTheme.ALAA
    val appPalette = remember(presentation.id) {
        if (presentation.id.isFixedFoundation) AppColors.ClassicPalette else presentation.toAppPalette()
    }
    // Written before any descendant reads it, so the first frame is already themed.
    if (AppColors.palette != appPalette) AppColors.palette = appPalette
    CompositionLocalProvider(
        LocalAppSpacing provides com.streamvault.app.ui.design.AppSpacing(),
        LocalAppShapes provides remember(presentation.id) { presentation.toAppShapes() },
        LocalAppHomeTheme provides presentation.id,
        LocalThemePresentation provides presentation,
        LocalIsAlaaTheme provides isAlaa
    ) {
        MaterialTheme(
            colorScheme = when {
                isAlaa -> AlaaColorScheme
                presentation.id == AppHomeTheme.CLASSIC -> DarkColorScheme
                else -> presentation.toColorScheme()
            },
            typography = typography,
            content = content
        )
    }
}
