package com.streamvault.app.ui.theme

import com.google.common.truth.Truth.assertThat
import com.streamvault.domain.model.AppHomeTheme
import com.streamvault.app.ui.themes.bespoke.bespokeThemeFor
import org.junit.Test

class HorizonThemeTest {
    @Test fun `landscape themes persist and supply complete dedicated presentations`() {
        listOf(AppHomeTheme.GHURUB, AppHomeTheme.THALJ).forEach { theme ->
            assertThat(AppHomeTheme.fromStorage(theme.storageValue)).isEqualTo(theme)
            assertThat(ThemePresentationRegistry.isSelectable(theme)).isTrue()
            assertThat(ThemePresentationRegistry.resolve(theme).navigationLayout).isEqualTo(ThemeNavigationLayout.TOP_BAR)
            assertThat(ThemeCatalog.entry(theme).description).isNotEmpty()
            assertThat(bespokeThemeFor(theme)).isNotNull()
        }
        assertThat(bespokeThemeFor(AppHomeTheme.GHURUB)).isNotSameInstanceAs(bespokeThemeFor(AppHomeTheme.THALJ))
        assertThat(ThemePresentationRegistry.resolve(AppHomeTheme.GHURUB).surfaces.accent)
            .isNotEqualTo(ThemePresentationRegistry.resolve(AppHomeTheme.THALJ).surfaces.accent)
    }
}
