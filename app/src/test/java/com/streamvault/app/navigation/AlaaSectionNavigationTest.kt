package com.streamvault.app.navigation

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AlaaSectionNavigationTest {
    @Test
    fun `Alaa sections replace the home destination when opened from dashboard`() {
        assertThat(shouldReplaceAlaaHomeStack(true, Routes.HOME, Routes.LIVE_TV)).isTrue()
        assertThat(shouldReplaceAlaaHomeStack(true, Routes.HOME, Routes.liveTv(47L))).isTrue()
        assertThat(shouldReplaceAlaaHomeStack(true, Routes.HOME, Routes.MOVIES)).isTrue()
        assertThat(shouldReplaceAlaaHomeStack(true, Routes.HOME, Routes.SERIES)).isTrue()
    }

    @Test
    fun `classic theme and non home routes preserve regular navigation state`() {
        assertThat(shouldReplaceAlaaHomeStack(false, Routes.HOME, Routes.LIVE_TV)).isFalse()
        assertThat(shouldReplaceAlaaHomeStack(true, Routes.LIVE_TV, Routes.MOVIES)).isFalse()
        assertThat(shouldReplaceAlaaHomeStack(true, Routes.HOME, Routes.SETTINGS)).isFalse()
    }
}
