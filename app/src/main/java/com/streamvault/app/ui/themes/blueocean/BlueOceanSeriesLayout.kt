package com.streamvault.app.ui.themes.blueocean

/** Style contract: Blue Ocean series use a vertical season-current ledger, not a film route layout. */

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import com.streamvault.app.ui.theme.LocalThemePresentation
import com.streamvault.domain.model.Category
import com.streamvault.domain.model.LibraryFilterType
import com.streamvault.domain.model.LibrarySortBy
import com.streamvault.domain.model.Series

@Composable
internal fun BlueOceanSeriesLayout(uiState: SeriesUiState, initialFocusRequester: FocusRequester, isCategoryLocked: (Category) -> Boolean, isSeriesLocked: (Series) -> Boolean, onCategoryClick: (Category) -> Unit, onCategoryLongClick: (Category) -> Unit, onSeriesClick: (Series) -> Unit, onSeriesLongClick: (Series) -> Unit, onQueryChange: (String) -> Unit, onFilterChange: (LibraryFilterType) -> Unit, onSortChange: (LibrarySortBy) -> Unit, onLoadMoreSelected: () -> Unit, onLoadMorePreview: () -> Unit, modifier: Modifier = Modifier) {
    val p = LocalThemePresentation.current
    val s = p.surfaces
    val titles = if (uiState.selectedCategory == null) uiState.seriesByCategory.values.flatten().distinctBy { it.id } else uiState.selectedCategoryItems
    val loading = if (uiState.selectedCategory == null) uiState.isLoadingPreviewRows else uiState.isLoadingSelectedCategory || uiState.isLoadingMoreSelectedCategory
    val canLoadMore = if (uiState.selectedCategory == null) uiState.hasMorePreviewRows else uiState.canLoadMoreSelectedCategory
    Column(modifier.fillMaxSize().background(s.canvas).padding(28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) { Column { Text("SERIES ESTUARY", style = MaterialTheme.typography.displaySmall, color = s.textPrimary); Text("SEASON CURRENTS / ${titles.size} STORIES", style = MaterialTheme.typography.labelMedium, color = s.accent) }; SearchInput(value = uiState.searchQuery, onValueChange = onQueryChange, placeholder = "Search season currents", modifier = Modifier.width(354.dp), focusRequester = initialFocusRequester) }
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.width(250.dp).fillMaxHeight().background(s.browseRail, RoundedCornerShape(26.dp)).padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("TRIBUTARIES", style = MaterialTheme.typography.titleMedium); Text("SELECT A ROUTE", style = MaterialTheme.typography.labelSmall, color = s.textSecondary); LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) { items(uiState.categories, key = { it.id }) { category -> BlueOceanSeriesChip(if (isCategoryLocked(category)) "LOCKED" else category.name, category.name == uiState.selectedCategory, { onCategoryClick(category) }, { onCategoryLongClick(category) }) } }; BlueOceanSeriesChip("FILTER · ${uiState.selectedLibraryFilterType.name}", false, { onFilterChange(LibraryFilterType.entries[(LibraryFilterType.entries.indexOf(uiState.selectedLibraryFilterType) + 1) % LibraryFilterType.entries.size]) }); BlueOceanSeriesChip("SORT · ${uiState.selectedLibrarySortBy.name}", false, { onSortChange(LibrarySortBy.entries[(LibrarySortBy.entries.indexOf(uiState.selectedLibrarySortBy) + 1) % LibrarySortBy.entries.size]) }) }
            when { !uiState.hasActiveProvider -> BlueOceanSeriesState("NO ACTIVE PROVIDER", "Choose an active provider before browsing series.", Modifier.weight(1f)); !uiState.errorMessage.isNullOrBlank() -> BlueOceanSeriesState("SERIES CURRENT INTERRUPTED", uiState.errorMessage, Modifier.weight(1f)); titles.isEmpty() && !loading -> BlueOceanSeriesState("NO SERIES ON THIS ROUTE", "Try another tributary or search term.", Modifier.weight(1f)); else -> LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(11.dp)) { items(titles, key = { it.id }) { series -> BlueOceanSeriesLedgerRow(series, isSeriesLocked(series), { onSeriesClick(series) }, { onSeriesLongClick(series) }) }; if (loading) item { BlueOceanSeriesState("LOADING SEASON CURRENTS", "Gathering stories from the selected tributary.") }; if (canLoadMore && !loading) item { BlueOceanSeriesChip("LOAD NEXT CURRENT", false, { if (uiState.selectedCategory == null) onLoadMorePreview() else onLoadMoreSelected() }) } } }
        }
    }
}

@Composable
private fun BlueOceanSeriesChip(label: String, selected: Boolean, onClick: () -> Unit, onLongClick: () -> Unit = onClick) { val p = LocalThemePresentation.current; val s = p.surfaces; val shape = RoundedCornerShape(15.dp); TvClickableSurface(onClick = onClick, onLongClick = onLongClick, modifier = Modifier.fillMaxWidth(), shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = if (selected) s.selectedAccent else s.browseContent, focusedContainerColor = s.focusedSurface, contentColor = s.textPrimary, focusedContentColor = s.textPrimary), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, s.accent), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f)) { Text(label, Modifier.padding(horizontal = 12.dp, vertical = 11.dp), style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis) } }

@Composable
private fun BlueOceanSeriesLedgerRow(series: Series, locked: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) { val p = LocalThemePresentation.current; val s = p.surfaces; val shape = RoundedCornerShape(24.dp); TvClickableSurface(onClick = onClick, onLongClick = onLongClick, modifier = Modifier.fillMaxWidth(), shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = s.browseContent, focusedContainerColor = s.focusedSurface, contentColor = s.textPrimary, focusedContentColor = s.textPrimary), border = ClickableSurfaceDefaults.border(border = Border(border = BorderStroke(1.dp, s.textSecondary.copy(alpha = .22f)), shape = shape), focusedBorder = Border(border = BorderStroke(2.dp, s.accent), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.025f)) { Row(Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) { Text("S", style = MaterialTheme.typography.displaySmall, color = s.accent); Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) { Text(if (locked) "LOCKED SERIES" else series.name, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(if (locked) "Protected tributary" else listOfNotNull(series.genre, series.releaseDate).joinToString(" · ").ifBlank { "Open seasons and episodes" }, style = MaterialTheme.typography.bodyMedium, color = s.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis) }; Text("SEASONS →", style = MaterialTheme.typography.labelLarge, color = s.accent) } } }

@Composable
private fun BlueOceanSeriesState(title: String, subtitle: String?, modifier: Modifier = Modifier) { val s = LocalThemePresentation.current.surfaces; Column(modifier.fillMaxWidth().background(s.browseContent, RoundedCornerShape(26.dp)).padding(22.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text(title, style = MaterialTheme.typography.titleLarge, color = s.textPrimary); if (!subtitle.isNullOrBlank()) Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = s.textSecondary) } }
