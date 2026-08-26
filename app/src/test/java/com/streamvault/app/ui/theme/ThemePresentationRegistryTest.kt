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
    fun `only complete foundation themes are selectable before additions are registered`() {
        assertThat(ThemePresentationRegistry.selectableThemes())
            .containsExactly(AppHomeTheme.CLASSIC, AppHomeTheme.ALAA)
            .inOrder()
        assertThat(ThemePresentationRegistry.isSelectable(AppHomeTheme.CINEMATIC)).isFalse()
        assertThat(ThemePresentationRegistry.resolveOrClassic(AppHomeTheme.CINEMATIC).id)
            .isEqualTo(AppHomeTheme.CLASSIC)
    }
}
