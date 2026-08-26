package com.streamvault.app.ui.themes.redcinema

/** Red Cinema series contract: seasons and episodes are organised as a vertical stage playbill. */

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
internal fun RedCinemaSeasonPlaybill(
    uiState: SeriesUiState, initialFocusRequester: FocusRequester, isCategoryLocked: (Category) -> Boolean, isSeriesLocked: (Series) -> Boolean,
    onCategoryClick: (Category) -> Unit, onCategoryLongClick: (Category) -> Unit, onSeriesClick: (Series) -> Unit, onSeriesLongClick: (Series) -> Unit,
    onQueryChange: (String) -> Unit, onFilterChange: (LibraryFilterType) -> Unit, onSortChange: (LibrarySortBy) -> Unit, onLoadMoreSelected: () -> Unit, onLoadMorePreview: () -> Unit, modifier: Modifier = Modifier
) {
    val s = LocalThemePresentation.current.surfaces
    val serials = if (uiState.selectedCategory == null) uiState.seriesByCategory.values.flatten().distinctBy { it.id } else uiState.selectedCategoryItems
    val loading = if (uiState.selectedCategory == null) uiState.isLoadingPreviewRows else uiState.isLoadingSelectedCategory || uiState.isLoadingMoreSelectedCategory
    val canLoad = if (uiState.selectedCategory == null) uiState.hasMorePreviewRows else uiState.canLoadMoreSelectedCategory
    Column(modifier.fillMaxSize().background(s.canvas).padding(26.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) { Column { Text("SERIAL PLAYBILL", style = MaterialTheme.typography.displaySmall); Text("SEASONS AND EPISODES", style = MaterialTheme.typography.labelMedium, color = s.accent) }; SearchInput(value = uiState.searchQuery, onValueChange = onQueryChange, placeholder = "Search productions", modifier = Modifier.width(350.dp), focusRequester = initialFocusRequester) }
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.width(250.dp).background(s.browseRail, RoundedCornerShape(2.dp)).padding(13.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text("ACT INDEX", style = MaterialTheme.typography.titleMedium, color = s.accent); LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) { items(uiState.categories, key = { it.id }) { category -> RedCinemaSeriesTicket(if (isCategoryLocked(category)) "RESTRICTED" else category.name, category.name == uiState.selectedCategory, { onCategoryClick(category) }, { onCategoryLongClick(category) }) } }; RedCinemaSeriesTicket("FILTER · ${uiState.selectedLibraryFilterType.name}", false, { onFilterChange(LibraryFilterType.entries[(LibraryFilterType.entries.indexOf(uiState.selectedLibraryFilterType) + 1) % LibraryFilterType.entries.size]) }); RedCinemaSeriesTicket("SORT · ${uiState.selectedLibrarySortBy.name}", false, { onSortChange(LibrarySortBy.entries[(LibrarySortBy.entries.indexOf(uiState.selectedLibrarySortBy) + 1) % LibrarySortBy.entries.size]) }) }
            when { !uiState.hasActiveProvider -> RedCinemaSeriesState("NO ACTIVE PROVIDER", "Choose a provider before opening the playbill.", Modifier.weight(1f)); !uiState.errorMessage.isNullOrBlank() -> RedCinemaSeriesState("PLAYBILL INTERRUPTED", uiState.errorMessage, Modifier.weight(1f)); serials.isEmpty() && !loading -> RedCinemaSeriesState("NO PRODUCTIONS", "Try another act or search term.", Modifier.weight(1f)); else -> LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) { items(serials, key = { it.id }) { serial -> RedCinemaSerialListing(serial, isSeriesLocked(serial), { onSeriesClick(serial) }, { onSeriesLongClick(serial) }) }; if (loading) item { RedCinemaSeriesState("LOADING ACTS", "Gathering seasons and episodes.") }; if (canLoad && !loading) item { RedCinemaSeriesTicket("NEXT PAGE", false, { if (uiState.selectedCategory == null) onLoadMorePreview() else onLoadMoreSelected() }) } } }
        }
    }
}

@Composable
private fun RedCinemaSeriesTicket(label: String, selected: Boolean, onClick: () -> Unit, onLongClick: () -> Unit = onClick) { val s = LocalThemePresentation.current.surfaces; val shape = RoundedCornerShape(2.dp); TvClickableSurface(onClick = onClick, onLongClick = onLongClick, modifier = Modifier.fillMaxWidth(), shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = if (selected) s.selectedAccent else s.browseContent, focusedContainerColor = s.focusedSurface, contentColor = s.textPrimary, focusedContentColor = s.textPrimary), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, s.accent), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.018f)) { Text(label, Modifier.padding(horizontal = 11.dp, vertical = 10.dp), style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis) } }

@Composable
private fun RedCinemaSerialListing(series: Series, locked: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) { val s = LocalThemePresentation.current.surfaces; val shape = RoundedCornerShape(2.dp); TvClickableSurface(onClick = onClick, onLongClick = onLongClick, modifier = Modifier.fillMaxWidth(), shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = s.browseContent, focusedContainerColor = s.focusedSurface, contentColor = s.textPrimary, focusedContentColor = s.textPrimary), border = ClickableSurfaceDefaults.border(border = Border(border = BorderStroke(1.dp, s.textSecondary.copy(alpha = .24f)), shape = shape), focusedBorder = Border(border = BorderStroke(2.dp, s.accent), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.018f)) { Row(Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) { Text("ACT", style = MaterialTheme.typography.labelMedium, color = s.accent); Column(Modifier.weight(1f)) { Text(if (locked) "RESTRICTED SERIAL" else series.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(if (locked) "Protected production" else listOfNotNull(series.genre, series.releaseDate).joinToString(" · ").ifBlank { "Open seasons and episodes" }, style = MaterialTheme.typography.bodySmall, color = s.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis) }; Text("SEASONS", style = MaterialTheme.typography.labelMedium, color = s.accent) } } }

@Composable
private fun RedCinemaSeriesState(title: String, subtitle: String?, modifier: Modifier = Modifier) { val s = LocalThemePresentation.current.surfaces; Column(modifier.fillMaxWidth().background(s.browseContent, RoundedCornerShape(2.dp)).padding(20.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) { Text(title, style = MaterialTheme.typography.titleLarge); if (!subtitle.isNullOrBlank()) Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = s.textSecondary) } }
