package com.streamvault.app.navigation

import com.google.common.truth.Truth.assertThat
import com.streamvault.app.ui.theme.ThemePresentationRegistry
import com.streamvault.domain.model.AppHomeTheme
import org.junit.Test

class AlaaSectionNavigationTest {
    @Test
    fun `Alaa sections replace the home destination when opened from dashboard`() {
        val alaa = ThemePresentationRegistry.resolve(AppHomeTheme.ALAA)

        assertThat(shouldReplaceHomeStack(alaa, Routes.HOME, Routes.LIVE_TV)).isTrue()
        assertThat(shouldReplaceHomeStack(alaa, Routes.HOME, Routes.liveTv(47L))).isTrue()
        assertThat(shouldReplaceHomeStack(alaa, Routes.HOME, Routes.MOVIES)).isTrue()
        assertThat(shouldReplaceHomeStack(alaa, Routes.HOME, Routes.SERIES)).isTrue()
    }

    @Test
    fun `classic theme and non home routes preserve regular navigation state`() {
        val classic = ThemePresentationRegistry.resolve(AppHomeTheme.CLASSIC)
        val alaa = ThemePresentationRegistry.resolve(AppHomeTheme.ALAA)

        assertThat(shouldReplaceHomeStack(classic, Routes.HOME, Routes.LIVE_TV)).isTrue()
        assertThat(shouldReplaceHomeStack(alaa, Routes.LIVE_TV, Routes.MOVIES)).isFalse()
        assertThat(shouldReplaceHomeStack(alaa, Routes.HOME, Routes.SETTINGS)).isFalse()
    }
}
