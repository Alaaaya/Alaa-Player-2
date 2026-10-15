package com.streamvault.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.google.common.truth.Truth.assertThat
import com.streamvault.app.ui.themes.bespoke.bespokeThemeFor
import com.streamvault.app.ui.themes.sama.SamaUi
import com.streamvault.domain.model.AppHomeTheme
import org.junit.Test

class SamaThemeTest {
    @Test fun storageRoundTrip() {
        assertThat(AppHomeTheme.fromStorage("sama")).isEqualTo(AppHomeTheme.SAMA)
        assertThat(AppHomeTheme.fromStorage("SAMA")).isEqualTo(AppHomeTheme.SAMA)
        assertThat(AppHomeTheme.SAMA.isFixedFoundation).isFalse()
    }

    @Test fun completeThemeIsSelectableAndHasItsOwnScreens() {
        assertThat(ThemePresentationRegistry.isSelectable(AppHomeTheme.SAMA)).isTrue()
        assertThat(bespokeThemeFor(AppHomeTheme.SAMA)).isSameInstanceAs(SamaUi)
        assertThat(ThemeCatalog.entry(AppHomeTheme.SAMA).title).contains("Sama")
        val presentation = ThemePresentationRegistry.resolve(AppHomeTheme.SAMA)
        assertThat(presentation.navigationLayout).isEqualTo(ThemeNavigationLayout.TOP_BAR)
        assertThat(presentation.surfaces.accent).isEqualTo(Color.White)
        assertThat(presentation.replacesHomeWhenOpeningSections).isTrue()
    }

    @Test fun addingSamaKeepsExistingThemes() {
        assertThat(ThemePresentationRegistry.isSelectable(AppHomeTheme.KAT)).isTrue()
        assertThat(ThemePresentationRegistry.isSelectable(AppHomeTheme.IVANO)).isTrue()
        assertThat(ThemePresentationRegistry.isSelectable(AppHomeTheme.SABHIYA)).isTrue()
        assertThat(ThemePresentationRegistry.selectableThemes().last()).isEqualTo(AppHomeTheme.SAMA)
    }
}
