package com.streamvault.app.ui.theme

import com.google.common.truth.Truth.assertThat
import com.streamvault.domain.model.AppHomeTheme
import org.junit.Test
import org.junit.Assert.assertThrows

class ThemePresentationRegistryTest {
    @Test
    fun `Alaa resolves to its complete section and live tv presentation contract`() {
        val presentation = ThemePresentationRegistry.resolve(AppHomeTheme.ALAA)

        assertThat(presentation.navigationLayout).isEqualTo(ThemeNavigationLayout.TOP_BAR)
        assertThat(presentation.liveTvLayout).isEqualTo(ThemeLiveTvLayout.CATEGORIES_CHANNELS_PREVIEW)
        assertThat(presentation.replacesHomeWhenOpeningSections).isTrue()
        assertThat(presentation.surfaces.accent).isEqualTo(AlaaThemeColors.Accent)
    }

    @Test
    fun `classic preserves its existing adaptive presentation contract`() {
        val presentation = ThemePresentationRegistry.resolve(AppHomeTheme.CLASSIC)

        assertThat(presentation.navigationLayout).isEqualTo(ThemeNavigationLayout.ADAPTIVE)
        assertThat(presentation.replacesHomeWhenOpeningSections).isFalse()
    }

    @Test
    fun `classic and alaa remain the fixed presentation foundations`() {
        assertThat(AppHomeTheme.fixedFoundations)
            .containsExactly(AppHomeTheme.CLASSIC, AppHomeTheme.ALAA)
    }

    @Test
    fun `registry rejects attempts to replace fixed theme foundations`() {
        val classicReplacement = ThemePresentationRegistry.resolve(AppHomeTheme.CLASSIC)
            .copy(navigationLayout = ThemeNavigationLayout.TOP_BAR)

        val error = assertThrows(IllegalArgumentException::class.java) {
            ThemePresentationRegistry.registerAdditional(classicReplacement)
        }

        assertThat(error).hasMessageThat().contains("Fixed theme foundations cannot be replaced")
        assertThat(ThemePresentationRegistry.resolve(AppHomeTheme.CLASSIC).navigationLayout)
            .isEqualTo(ThemeNavigationLayout.ADAPTIVE)
    }

    @Test
    fun `completed additional presentations are registered in the approved order`() {
        assertThat(ThemePresentationRegistry.selectableThemes())
            .containsExactly(
                AppHomeTheme.CLASSIC,
                AppHomeTheme.ALAA,
                AppHomeTheme.CINEMATIC,
                AppHomeTheme.NEON_FUTURE,
                AppHomeTheme.MINIMAL,
                AppHomeTheme.GLASSMORPHISM,
                AppHomeTheme.STREAMING_PLATFORM,
                AppHomeTheme.PREMIUM_BLACK,
                AppHomeTheme.RED_CINEMA
            )
            .inOrder()
        assertThat(ThemePresentationRegistry.isSelectable(AppHomeTheme.CINEMATIC)).isTrue()
        val cinematic = ThemePresentationRegistry.resolve(AppHomeTheme.CINEMATIC)
        assertThat(cinematic.id).isEqualTo(AppHomeTheme.CINEMATIC)
        assertThat(cinematic.navigationLayout).isEqualTo(ThemeNavigationLayout.SIDE_RAIL)
        assertThat(cinematic.liveTvLayout).isEqualTo(ThemeLiveTvLayout.CATEGORIES_CHANNELS_PREVIEW)
        assertThat(cinematic.replacesHomeWhenOpeningSections).isTrue()
        assertThat(ThemePresentationRegistry.isSelectable(AppHomeTheme.NEON_FUTURE)).isTrue()
        val neon = ThemePresentationRegistry.resolve(AppHomeTheme.NEON_FUTURE)
        assertThat(neon.navigationLayout).isEqualTo(ThemeNavigationLayout.SIDE_RAIL)
        assertThat(neon.liveTvLayout).isEqualTo(ThemeLiveTvLayout.CATEGORIES_CHANNELS_PREVIEW)
        assertThat(neon.surfaces.accent).isEqualTo(androidx.compose.ui.graphics.Color(0xFF5BF4FF))
        assertThat(neon.replacesHomeWhenOpeningSections).isTrue()
        assertThat(ThemePresentationRegistry.isSelectable(AppHomeTheme.MINIMAL)).isTrue()
        val minimal = ThemePresentationRegistry.resolve(AppHomeTheme.MINIMAL)
        assertThat(minimal.navigationLayout).isEqualTo(ThemeNavigationLayout.SIDE_RAIL)
        assertThat(minimal.liveTvLayout).isEqualTo(ThemeLiveTvLayout.CATEGORIES_CHANNELS_PREVIEW)
        assertThat(minimal.focus.focusedScale).isEqualTo(1f)
        assertThat(minimal.focus.motionDurationMs).isEqualTo(140)
        assertThat(minimal.replacesHomeWhenOpeningSections).isTrue()
        assertThat(ThemePresentationRegistry.isSelectable(AppHomeTheme.GLASSMORPHISM)).isTrue()
        val glass = ThemePresentationRegistry.resolve(AppHomeTheme.GLASSMORPHISM)
        assertThat(glass.navigationLayout).isEqualTo(ThemeNavigationLayout.SIDE_RAIL)
        assertThat(glass.liveTvLayout).isEqualTo(ThemeLiveTvLayout.CATEGORIES_CHANNELS_PREVIEW)
        assertThat(glass.focus.motionDurationMs).isEqualTo(180)
        assertThat(glass.replacesHomeWhenOpeningSections).isTrue()
        assertThat(ThemePresentationRegistry.isSelectable(AppHomeTheme.STREAMING_PLATFORM)).isTrue()
        val streaming = ThemePresentationRegistry.resolve(AppHomeTheme.STREAMING_PLATFORM)
        assertThat(streaming.navigationLayout).isEqualTo(ThemeNavigationLayout.TOP_BAR)
        assertThat(streaming.liveTvLayout).isEqualTo(ThemeLiveTvLayout.CATEGORIES_CHANNELS_PREVIEW)
        assertThat(streaming.replacesHomeWhenOpeningSections).isTrue()
        assertThat(ThemePresentationRegistry.isSelectable(AppHomeTheme.PREMIUM_BLACK)).isTrue()
        val premiumBlack = ThemePresentationRegistry.resolve(AppHomeTheme.PREMIUM_BLACK)
        assertThat(premiumBlack.navigationLayout).isEqualTo(ThemeNavigationLayout.SIDE_RAIL)
        assertThat(premiumBlack.liveTvLayout).isEqualTo(ThemeLiveTvLayout.CATEGORIES_CHANNELS_PREVIEW)
        assertThat(premiumBlack.focus.focusedScale).isEqualTo(1.02f)
        assertThat(premiumBlack.focus.motionDurationMs).isEqualTo(220)
        assertThat(premiumBlack.replacesHomeWhenOpeningSections).isTrue()
        assertThat(ThemeCatalog.selectableEntries().map { it.theme })
            .containsExactly(
                AppHomeTheme.CLASSIC,
                AppHomeTheme.ALAA,
                AppHomeTheme.CINEMATIC,
                AppHomeTheme.NEON_FUTURE,
                AppHomeTheme.MINIMAL,
                AppHomeTheme.GLASSMORPHISM,
                AppHomeTheme.STREAMING_PLATFORM,
                AppHomeTheme.PREMIUM_BLACK,
                AppHomeTheme.RED_CINEMA
            )
            .inOrder()
    }
}
