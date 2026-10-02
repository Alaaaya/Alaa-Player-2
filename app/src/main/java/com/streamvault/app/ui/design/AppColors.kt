package com.streamvault.app.ui.design

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * Semantic colors used by the shared screens and components. Classic values are the
 * default; additional themes swap in a palette derived from their presentation so shared
 * screens follow the selected theme. Status colors stay fixed for recognisability.
 */
data class AppPalette(
    val canvas: Color,
    val canvasElevated: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceEmphasis: Color,
    val surfaceAccent: Color,
    val brand: Color,
    val brandMuted: Color,
    val brandStrong: Color,
    val focus: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textDisabled: Color,
    val divider: Color,
    val outline: Color,
    val heroTop: Color,
    val heroBottom: Color,
    val isLight: Boolean = false
)

object AppColors {
    val ClassicPalette = AppPalette(
        canvas = Color(0xFF07111B),
        canvasElevated = Color(0xFF0B1622),
        surface = Color(0xFF0F1B29),
        surfaceElevated = Color(0xFF162338),
        surfaceEmphasis = Color(0xFF1D2E46),
        surfaceAccent = Color(0xFF223754),
        brand = Color(0xFF69A8FF),
        brandMuted = Color(0x335FA4FF),
        brandStrong = Color(0xFF8BBCFF),
        focus = Color(0xFFF4F8FF),
        textPrimary = Color(0xFFF5F7FB),
        textSecondary = Color(0xFFBBC6D8),
        textTertiary = Color(0xFF7F8DA5),
        textDisabled = Color(0xFF566173),
        divider = Color(0x1AF4F8FF),
        outline = Color(0x264C6D95),
        heroTop = Color(0xCC07111B),
        heroBottom = Color(0xF207111B)
    )

    /** Set only by StreamVaultTheme. Snapshot state so readers recompose on theme change. */
    var palette: AppPalette by mutableStateOf(ClassicPalette)
        internal set

    val Canvas get() = palette.canvas
    val CanvasElevated get() = palette.canvasElevated
    val Surface get() = palette.surface
    val SurfaceElevated get() = palette.surfaceElevated
    val SurfaceEmphasis get() = palette.surfaceEmphasis
    val SurfaceAccent get() = palette.surfaceAccent

    val Brand get() = palette.brand
    val BrandMuted get() = palette.brandMuted
    val BrandStrong get() = palette.brandStrong
    val Focus get() = palette.focus

    val TextPrimary get() = palette.textPrimary
    val TextSecondary get() = palette.textSecondary
    val TextTertiary get() = palette.textTertiary
    val TextDisabled get() = palette.textDisabled

    val Live = Color(0xFFFF5C61)
    val Success = Color(0xFF4FD39A)
    val Warning = Color(0xFFFFC766)
    val Info = Color(0xFF57C9FF)

    val Divider get() = palette.divider
    val Outline get() = palette.outline

    val HeroTop get() = palette.heroTop
    val HeroBottom get() = palette.heroBottom
}
