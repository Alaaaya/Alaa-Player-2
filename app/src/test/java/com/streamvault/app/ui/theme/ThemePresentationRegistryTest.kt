package com.streamvault.app.ui.theme

import com.google.common.truth.Truth.assertThat
import com.streamvault.domain.model.AppHomeTheme
import org.junit.Test

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
}
