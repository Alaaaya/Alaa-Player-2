package com.streamvault.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.google.common.truth.Truth.assertThat
import com.streamvault.app.ui.themes.bespoke.bespokeThemeFor
import com.streamvault.app.ui.themes.najma.NajmaUi
import com.streamvault.domain.model.AppHomeTheme
import org.junit.Test

class NajmaThemeTest {
    @Test fun najmaIsCompleteSelectableAndPersistent() {
        val theme=AppHomeTheme.NAJMA
        assertThat(AppHomeTheme.fromStorage("najma")).isEqualTo(theme)
        assertThat(ThemePresentationRegistry.isSelectable(theme)).isTrue()
        assertThat(ThemeCatalog.entry(theme).title).contains("نجمة")
        assertThat(bespokeThemeFor(theme)).isSameInstanceAs(NajmaUi)
        val presentation=ThemePresentationRegistry.resolve(theme)
        assertThat(presentation.navigationLayout).isEqualTo(ThemeNavigationLayout.TOP_BAR)
        assertThat(presentation.liveTvLayout).isEqualTo(ThemeLiveTvLayout.CATEGORIES_CHANNELS_PREVIEW)
        assertThat(presentation.surfaces.accent).isEqualTo(Color(0xFFFF3B4C))
        assertThat(bespokeThemeFor(theme)).isNotSameInstanceAs(bespokeThemeFor(AppHomeTheme.GHURUB))
    }
}
