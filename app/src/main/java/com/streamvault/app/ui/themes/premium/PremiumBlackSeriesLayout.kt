package com.streamvault.app.ui.themes.premium

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.app.ui.components.SearchInput
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.app.ui.screens.series.SeriesUiState
import com.streamvault.domain.model.Category
import com.streamvault.domain.model.LibraryFilterType
import com.streamvault.domain.model.LibrarySortBy
import com.streamvault.domain.model.Series

@Composable
internal fun PremiumBlackSeriesLayout(uiState: SeriesUiState, initialFocusRequester: FocusRequester, isCategoryLocked: (Category) -> Boolean, isSeriesLocked: (Series) -> Boolean, onCategoryClick: (Category) -> Unit, onCategoryLongClick: (Category) -> Unit, onSeriesClick: (Series) -> Unit, onSeriesLongClick: (Series) -> Unit, onQueryChange: (String) -> Unit, onFilterChange: (LibraryFilterType) -> Unit, onSortChange: (LibrarySortBy) -> Unit, onLoadMoreSelected: () -> Unit, onLoadMorePreview: () -> Unit, modifier: Modifier = Modifier) {
    val entries = if (uiState.selectedCategory == null) uiState.seriesByCategory.values.flatten().distinctBy { it.id } else uiState.selectedCategoryItems
    val loading = if (uiState.selectedCategory == null) uiState.isLoadingPreviewRows else uiState.isLoadingSelectedCategory || uiState.isLoadingMoreSelectedCategory
    val canLoadMore = if (uiState.selectedCategory == null) uiState.hasMorePreviewRows else uiState.canLoadMoreSelectedCategory
    LazyColumn(modifier.fillMaxSize().background(PremiumCanvas).padding(28.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item("premium_series_head") { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) { Column(verticalArrangement = Arrangement.spacedBy(5.dp)) { Text("SERIES", style = MaterialTheme.typography.displaySmall, color = PremiumText); Text("PREMIUM COLLECTION / ${entries.size} TITLES", style = MaterialTheme.typography.labelMedium, color = PremiumMuted) }; SearchInput(value = uiState.searchQuery, onValueChange = onQueryChange, placeholder = "Search series", modifier = Modifier.width(320.dp), focusRequester = initialFocusRequester) } }
        item("premium_series_tools") { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { PremiumSeriesControl(label = "FILTER / ${uiState.selectedLibraryFilterType.name}", onClick = { onFilterChange(LibraryFilterType.entries[(LibraryFilterType.entries.indexOf(uiState.selectedLibraryFilterType) + 1) % LibraryFilterType.entries.size]) }); PremiumSeriesControl(label = "SORT / ${uiState.selectedLibrarySortBy.name}", onClick = { onSortChange(LibrarySortBy.entries[(LibrarySortBy.entries.indexOf(uiState.selectedLibrarySortBy) + 1) % LibrarySortBy.entries.size]) }) } }
        item("premium_series_categories") { LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(uiState.categories, key = { it.id }) { category -> PremiumSeriesControl(label = if (isCategoryLocked(category)) "LOCKED" else category.name, selected = category.name == uiState.selectedCategory, onClick = { onCategoryClick(category) }, onLongClick = { onCategoryLongClick(category) }) } } }
        when { !uiState.hasActiveProvider -> item("premium_series_no_provider") { PremiumSeriesState("NO PROVIDER", "Choose an active provider before opening series.") }; !uiState.errorMessage.isNullOrBlank() -> item("premium_series_error") { PremiumSeriesState("CATALOGUE ERROR", uiState.errorMessage) }; entries.isEmpty() && !loading -> item("premium_series_empty") { PremiumSeriesState("EMPTY SELECTION", "No series match this selection.") }; else -> item("premium_series_cards") { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { Text(if (uiState.selectedCategory == null) "SERIES COLLECTION" else uiState.selectedCategory.orEmpty(), style = MaterialTheme.typography.titleLarge, color = PremiumText); LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) { items(entries, key = { it.id }) { entry -> PremiumSeriesCard(entry, isSeriesLocked(entry), { onSeriesClick(entry) }, { onSeriesLongClick(entry) }) } } } } }
        if (loading) item("premium_series_loading") { PremiumSeriesState("LOADING", "Loading premium series entries…") }
        if (canLoadMore && !loading) item("premium_series_more") { PremiumSeriesControl(label = "LOAD MORE", onClick = { if (uiState.selectedCategory == null) onLoadMorePreview() else onLoadMoreSelected() }) }
    }
}

@Composable private fun PremiumSeriesControl(label: String, selected: Boolean = false, onClick: () -> Unit, onLongClick: () -> Unit = onClick) { val shape = RoundedCornerShape(8.dp); TvClickableSurface(onClick = onClick, onLongClick = onLongClick, shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = if (selected) PremiumPanelFocused else PremiumPanel, focusedContainerColor = PremiumPanelFocused, contentColor = PremiumText, focusedContentColor = PremiumText), border = ClickableSurfaceDefaults.border(border = Border(border = BorderStroke(1.dp, if (selected) PremiumGold else PremiumMetal), shape = shape), focusedBorder = Border(border = BorderStroke(2.dp, PremiumFocus), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f)) { Text(label, Modifier.padding(horizontal = 15.dp, vertical = 11.dp), style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis) } }
@Composable private fun PremiumSeriesCard(series: Series, locked: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) { val shape = RoundedCornerShape(10.dp); TvClickableSurface(onClick = onClick, onLongClick = onLongClick, modifier = Modifier.width(224.dp).height(262.dp), shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = PremiumPanel, focusedContainerColor = PremiumPanelFocused, contentColor = PremiumText, focusedContentColor = PremiumText), border = ClickableSurfaceDefaults.border(border = Border(border = BorderStroke(1.dp, PremiumMetal), shape = shape), focusedBorder = Border(border = BorderStroke(2.dp, PremiumFocus), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.03f)) { Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.SpaceBetween) { Column(verticalArrangement = Arrangement.spacedBy(7.dp)) { Text(if (locked) "LOCKED" else series.name, style = MaterialTheme.typography.titleMedium, maxLines = 3, overflow = TextOverflow.Ellipsis); Text(if (locked) "Protected title" else series.genre ?: series.releaseDate.orEmpty(), style = MaterialTheme.typography.bodySmall, color = PremiumMuted, maxLines = 2, overflow = TextOverflow.Ellipsis) }; Text("SEASONS & EPISODES", style = MaterialTheme.typography.labelSmall, color = PremiumGold) } } }
@Composable private fun PremiumSeriesState(title: String, subtitle: String) { Column(Modifier.fillMaxWidth().background(PremiumPanel, RoundedCornerShape(10.dp)).padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text(title, style = MaterialTheme.typography.titleMedium, color = PremiumText); Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = PremiumMuted) } }
