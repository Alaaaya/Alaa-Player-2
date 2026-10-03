package com.streamvault.app.ui.themes.bespoke

import android.view.KeyEvent
import com.google.common.truth.Truth.assertThat
import com.streamvault.domain.model.LibraryFilterType
import com.streamvault.domain.model.LibrarySortBy
import org.junit.Test

class BespokeInteractionContractTest {
    private fun state(selected: String?) = LibraryState(
        itemsByCategory = mapOf("Action" to listOf("a1", "a2"), "Drama" to listOf("d1", "a1")),
        categoryNames = listOf("Action", "Drama"), categoryCounts = emptyMap(), libraryCount = 3,
        categories = emptyList(), selectedCategory = selected,
        selectedCategoryItems = if (selected == "Drama") listOf("d1", "a1") else listOf("a1", "a2"),
        canLoadMoreSelectedCategory = false, isLoadingSelectedCategory = false, hasMorePreviewRows = false,
        searchQuery = "", filteredItems = emptyList(), selectedFilter = LibraryFilterType.ALL,
        selectedSort = LibrarySortBy.LIBRARY, continueWatching = emptyList()
    )

    @Test
    fun allOverview_isNullSelection_andShowsEveryGenre() {
        val overview = state(null)
        assertThat(overview.isShowingAll).isTrue()
        assertThat(overview.visibleItems).containsExactly("a1", "a2", "d1")
    }

    @Test
    fun afterLaterGenre_showAllRestoresOverviewNotFirstGenre() {
        var selected: String? = "Drama"
        val onShowAll = { selected = null }
        onShowAll()
        assertThat(selected).isNull()
        assertThat(state(selected).visibleItems).containsExactly("a1", "a2", "d1")
    }

    @Test
    fun columnRouting_isMirroredInRtl() {
        assertThat(dpadKeyTowardEnd(rtl = false)).isEqualTo(KeyEvent.KEYCODE_DPAD_RIGHT)
        assertThat(dpadKeyTowardStart(rtl = false)).isEqualTo(KeyEvent.KEYCODE_DPAD_LEFT)
        assertThat(dpadKeyTowardEnd(rtl = true)).isEqualTo(KeyEvent.KEYCODE_DPAD_LEFT)
        assertThat(dpadKeyTowardStart(rtl = true)).isEqualTo(KeyEvent.KEYCODE_DPAD_RIGHT)
    }
}
