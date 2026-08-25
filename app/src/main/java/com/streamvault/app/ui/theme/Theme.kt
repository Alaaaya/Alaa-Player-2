package com.streamvault.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme
import com.streamvault.app.ui.design.AppColors
import com.streamvault.app.ui.design.AppShapes
import com.streamvault.app.ui.design.LocalAppShapes
import com.streamvault.app.ui.design.LocalAppSpacing
import com.streamvault.app.ui.design.rememberAppTypography
import com.streamvault.domain.model.AppHomeTheme

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.Brand,
    onPrimary = OnPrimary,
    surface = AppColors.Surface,
    onSurface = AppColors.TextPrimary,
    surfaceVariant = AppColors.SurfaceElevated,
    onSurfaceVariant = AppColors.TextSecondary,
    background = AppColors.CanvasElevated,
    onBackground = AppColors.TextPrimary,
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

@Composable
fun StreamVaultTheme(
    appHomeTheme: AppHomeTheme = AppHomeTheme.CLASSIC,
    content: @Composable () -> Unit
) {
    val typography = rememberAppTypography()
    val isAlaa = appHomeTheme == AppHomeTheme.ALAA
    CompositionLocalProvider(
        LocalAppSpacing provides com.streamvault.app.ui.design.AppSpacing(),
        LocalAppShapes provides AppShapes(),
        LocalAppHomeTheme provides appHomeTheme,
        LocalIsAlaaTheme provides isAlaa
    ) {
        MaterialTheme(
            colorScheme = if (isAlaa) AlaaColorScheme else DarkColorScheme,
            typography = typography,
            content = content
        )
    }
}
