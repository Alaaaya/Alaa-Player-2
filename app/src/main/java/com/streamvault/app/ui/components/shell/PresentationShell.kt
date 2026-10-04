package com.streamvault.app.ui.components.shell

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import com.streamvault.app.ui.theme.ThemePresentation
import com.streamvault.domain.model.AppHomeTheme

/**
 * Themes whose shell (canvas, rail/top bar, padding, radii, focus) is rendered by the shared
 * AppShell using only their ThemePresentation tokens. Blue Ocean has a dedicated rail.
 */
internal val PresentationShellThemes: Set<AppHomeTheme> = setOf(
    AppHomeTheme.RED_CINEMA,
    AppHomeTheme.PURPLE_GALAXY,
    AppHomeTheme.TECH_DASHBOARD,
    AppHomeTheme.MODERN_TV,
    AppHomeTheme.CARD_STACK,
    AppHomeTheme.MEDIA_CENTER,
    AppHomeTheme.FUTURISTIC_HUD,
    AppHomeTheme.SOFT_MODERN,
    AppHomeTheme.SPORTS_TV,
    AppHomeTheme.DARK_GLASS,
    AppHomeTheme.MAGAZINE_MEDIA,
    AppHomeTheme.NEXT_GEN_TV,
    AppHomeTheme.AURORA_LOUNGE,
    AppHomeTheme.CHAT_GPT,
    AppHomeTheme.CHAT_GPT_2
)

/** Per-theme canvas treatment built from palette tokens, so each theme reads distinctly. */
internal fun presentationCanvasBrush(presentation: ThemePresentation): Brush {
    val s = presentation.surfaces
    val glow = lerp(s.canvas, s.accent, 0.16f)
    val soft = lerp(s.canvas, s.focusedSurface, 0.5f)
    return when (presentation.id) {
        // Nebula/aurora light pooling from the top corner.
        AppHomeTheme.PURPLE_GALAXY, AppHomeTheme.AURORA_LOUNGE ->
            Brush.radialGradient(listOf(glow, s.canvas), center = Offset(1400f, -120f), radius = 1500f)
        // Instrument consoles: flat, cool, a faint horizon line of light.
        AppHomeTheme.TECH_DASHBOARD, AppHomeTheme.FUTURISTIC_HUD ->
            Brush.verticalGradient(listOf(s.canvas, s.browseContent, s.canvas, glow))
        // Refracted diagonal panes.
        AppHomeTheme.DARK_GLASS, AppHomeTheme.NEXT_GEN_TV ->
            Brush.linearGradient(listOf(s.canvas, soft, s.canvas, glow))
        // Paper themes stay calm and nearly flat.
        AppHomeTheme.SOFT_MODERN, AppHomeTheme.MAGAZINE_MEDIA ->
            Brush.verticalGradient(listOf(s.browseContent, s.canvas))
        // Stadium-light wash from below.
        AppHomeTheme.SPORTS_TV ->
            Brush.verticalGradient(listOf(s.canvas, s.browseRail, glow))
        AppHomeTheme.RED_CINEMA ->
            Brush.verticalGradient(listOf(s.canvas, lerp(s.canvas, s.accent, 0.1f), s.canvas))
        else -> Brush.linearGradient(listOf(s.canvas, s.browseRail, s.browseContent))
    }
}
