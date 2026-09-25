package com.streamvault.app.ui.themes.blueocean

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Dedicated colour tokens for the Blue Ocean theme.
 *
 * These tokens give every Blue Ocean composable a single source of truth for the
 * oceanic palette — deep navy canvas, teal harbour panels, aqua accents, and
 * wave-gradient surfaces — independent from any other theme's visual architecture.
 *
 * The values intentionally mirror the ThemeSurfaceSpec registered in
 * ThemePresentation so that code using dedicated tokens and code using the
 * presentation spec stay in sync.
 */

/** Deep ocean navy — the canvas behind every Blue Ocean screen. */
internal val BlueOceanCanvas: Color = Color(0xFF061923)

/** Dark teal panel used for harbour rails and collection columns. */
internal val BlueOceanPanel: Color = Color(0xFF0B2A39)

/** Navy content surface for browse and detail areas. */
internal val BlueOceanContent: Color = Color(0xFF0D2230)

/** Raised teal surface shown when a card or row gains focus. */
internal val BlueOceanPanelRaised: Color = Color(0xFF164A5F)

/** Ice-blue primary text — high contrast against navy. */
internal val BlueOceanText: Color = Color(0xFFE7FAFF)

/** Muted sky — secondary text and captions. */
internal val BlueOceanMuted: Color = Color(0xFF91B8C7)

/** Aqua accent — tide markers, focus borders, and active indicators. */
internal val BlueOceanAccent: Color = Color(0xFF4DD2E8)

/** Selected accent overlay — translucent aqua for selected states. */
internal val BlueOceanSelectedAccent: Color = Color(0x4C4DD2E8)

/** Deep tide marker — darker aqua for tide timetable labels. */
internal val BlueOceanTide: Color = Color(0xFF2A8FA5)

/** Foam white — brightest highlight, used for wave crests and active tide markers. */
internal val BlueOceanFoam: Color = Color(0xFFB8F4FC)

/** Abyss — near-black navy for deepest shadows and gradient ends. */
internal val BlueOceanAbyss: Color = Color(0xFF03101A)

/** Wave crest gradient — horizontal sweep from abyss to aqua. */
internal val BlueOceanWaveGradient: List<Color> = listOf(
    BlueOceanAbyss,
    BlueOceanCanvas,
    BlueOceanTide,
    BlueOceanAccent
)

/** Vertical surface gradient — navy to teal, used on panels and dock surfaces. */
internal val BlueOceanSurfaceGradient: List<Color> = listOf(
    BlueOceanCanvas,
    BlueOceanPanel
)

/** Tide-line gradient — accent to foam, for the wave timeline track. */
internal val BlueOceanTideGradient: List<Color> = listOf(
    BlueOceanTide,
    BlueOceanAccent,
    BlueOceanFoam
)

/** Brush helpers for gradients commonly used across Blue Ocean composables. */
internal val BlueOceanWaveBrush: Brush = Brush.horizontalGradient(BlueOceanWaveGradient)
internal val BlueOceanSurfaceBrush: Brush = Brush.verticalGradient(BlueOceanSurfaceGradient)
internal val BlueOceanTideBrush: Brush = Brush.horizontalGradient(BlueOceanTideGradient)

/** Shared corner shapes — medium for cards, large for panels and dock. */
internal val BlueOceanShape = RoundedCornerShape(18.dp)
internal val BlueOceanShapeLarge = RoundedCornerShape(34.dp)
internal val BlueOceanShapeDock = RoundedCornerShape(28.dp)
