package com.streamvault.app.ui.themes.neon

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.streamvault.app.R
import com.streamvault.app.ui.components.SearchInput
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.app.ui.screens.series.SeriesUiState
import com.streamvault.domain.model.Category
import com.streamvault.domain.model.LibraryFilterType
import com.streamvault.domain.model.LibrarySortBy
import com.streamvault.domain.model.Series

/** Neon Future series browser: a wide signal feed and a dedicated metadata monitor. */
@Composable
internal fun NeonFutureSeriesLayout(
    uiState: SeriesUiState,
    initialFocusRequester: FocusRequester,
    isCategoryLocked: (Category) -> Boolean,
    isSeriesLocked: (Series) -> Boolean,
    onCategoryClick: (Category) -> Unit,
    onCategoryLongClick: (Category) -> Unit,
    onSeriesClick: (Series) -> Unit,
    onSeriesLongClick: (Series) -> Unit,
    onQueryChange: (String) -> Unit,
    onFilterChange: (LibraryFilterType) -> Unit,
    onSortChange: (LibrarySortBy) -> Unit,
    onLoadMoreSelected: () -> Unit,
    onLoadMorePreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shownSeries = if (uiState.selectedCategory == null) uiState.seriesByCategory.values.flatten().distinctBy { it.id } else uiState.selectedCategoryItems
    var focusedSeries by remember(shownSeries) { mutableStateOf(shownSeries.firstOrNull()) }
    val canLoadMore = if (uiState.selectedCategory == null) uiState.hasMorePreviewRows else uiState.canLoadMoreSelectedCategory
    val loading = if (uiState.selectedCategory == null) uiState.isLoadingPreviewRows else uiState.isLoadingSelectedCategory || uiState.isLoadingMoreSelectedCategory
    val onLoadMore = if (uiState.selectedCategory == null) onLoadMorePreview else onLoadMoreSelected
    Column(modifier = modifier.fillMaxSize().background(NeonCanvas), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        NeonFutureSeriesConsole(uiState, initialFocusRequester, onQueryChange, onFilterChange, onSortChange)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(uiState.categories, key = { it.id }) { category ->
                NeonFutureSeriesBand(category, category.name == uiState.selectedCategory, isCategoryLocked(category), { onCategoryClick(category) }, { onCategoryLongClick(category) })
            }
        }
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            Column(modifier = Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text((uiState.selectedCategory ?: "SERIES FEED").uppercase(), style = MaterialTheme.typography.titleLarge, color = NeonText, fontWeight = FontWeight.Black)
                if (shownSeries.isEmpty() && !loading) Text("No series nodes match this signal band.", modifier = Modifier.fillMaxWidth().background(NeonPanel, RoundedCornerShape(10.dp)).padding(24.dp), style = MaterialTheme.typography.bodyLarge, color = NeonMuted)
                else LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                    items(shownSeries, key = { it.id }) { series ->
                        NeonFutureSeriesNode(series, isSeriesLocked(series), { onSeriesClick(series) }, { onSeriesLongClick(series) }) { focusedSeries = series }
                    }
                }
                if (loading) Text("SYNCING SERIES FEED…", style = MaterialTheme.typography.labelMedium, color = NeonMuted)
                if (canLoadMore && !loading) NeonFutureSeriesAction("LOAD MORE", NeonCyan, onLoadMore)
            }
            NeonFutureSeriesInspector(focusedSeries, isSeriesLocked(focusedSeries ?: return@Row), Modifier.width(282.dp).fillMaxHeight())
        }
    }
}

@Composable
private fun NeonFutureSeriesConsole(uiState: SeriesUiState, focusRequester: FocusRequester, onQueryChange: (String) -> Unit, onFilterChange: (LibraryFilterType) -> Unit, onSortChange: (LibrarySortBy) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(NeonPanel, RoundedCornerShape(14.dp)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("SERIES MATRIX", style = MaterialTheme.typography.titleLarge, color = NeonPink, fontWeight = FontWeight.Black)
            Text("SEASON FEED", style = MaterialTheme.typography.labelMedium, color = NeonMuted)
        }
        SearchInput(value = uiState.searchQuery, onValueChange = onQueryChange, placeholder = androidx.compose.ui.res.stringResource(R.string.search_hint), focusRequester = focusRequester)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NeonFutureSeriesAction("FILTER ${uiState.selectedLibraryFilterType.name.replace('_', ' ')}", NeonCyan) { val values = LibraryFilterType.entries; onFilterChange(values[(values.indexOf(uiState.selectedLibraryFilterType) + 1) % values.size]) }
            NeonFutureSeriesAction("ORDER ${uiState.selectedLibrarySortBy.name.replace('_', ' ')}", NeonLime) { val values = LibrarySortBy.entries; onSortChange(values[(values.indexOf(uiState.selectedLibrarySortBy) + 1) % values.size]) }
        }
    }
}

@Composable
private fun NeonFutureSeriesBand(category: Category, selected: Boolean, locked: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    val shape = RoundedCornerShape(7.dp)
    TvClickableSurface(onClick = onClick, onLongClick = onLongClick, shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = if (selected) NeonPink.copy(alpha = .2f) else NeonPanel, focusedContainerColor = NeonPanelRaised, contentColor = NeonText, focusedContentColor = NeonText), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, NeonPink), shape = shape))) {
        Text(if (locked) "LOCKED" else category.name.uppercase(), modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp), style = MaterialTheme.typography.labelMedium, color = if (selected) NeonPink else NeonMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun NeonFutureSeriesNode(series: Series, locked: Boolean, onClick: () -> Unit, onLongClick: () -> Unit, onFocused: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    TvClickableSurface(onClick = onClick, onLongClick = onLongClick, modifier = Modifier.fillMaxWidth().onFocusChanged { if (it.isFocused) onFocused() }, shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = NeonPanel, focusedContainerColor = NeonPanelRaised, contentColor = NeonText, focusedContentColor = NeonText), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, NeonLime), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.012f)) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(series.backdropUrl ?: series.posterUrl, null, Modifier.width(178.dp).height(96.dp).clip(RoundedCornerShape(7.dp)), contentScale = ContentScale.Crop)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(series.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(listOfNotNull(series.genre, series.rating.takeIf { it > 0f }?.let { "★ $it" }).joinToString(" / "), style = MaterialTheme.typography.labelSmall, color = NeonCyan, maxLines = 1)
                Text(if (locked) "ACCESS GATED" else series.plot.orEmpty().ifBlank { "Series metadata ready for selection." }, style = MaterialTheme.typography.bodySmall, color = if (locked) NeonPink else NeonMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun NeonFutureSeriesInspector(series: Series?, locked: Boolean, modifier: Modifier) {
    Column(modifier = modifier.background(NeonPanel, RoundedCornerShape(12.dp)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("SEASON MONITOR", style = MaterialTheme.typography.labelLarge, color = NeonLime, fontWeight = FontWeight.Black)
        AsyncImage(series?.backdropUrl ?: series?.posterUrl, null, Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
        Text(series?.name ?: "AWAITING TARGET", style = MaterialTheme.typography.titleLarge, color = NeonText, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text(if (locked) "ACCESS GATED" else series?.plot.orEmpty().ifBlank { "Focus a series node to inspect metadata and seasons." }, style = MaterialTheme.typography.bodyMedium, color = if (locked) NeonPink else NeonMuted, maxLines = 8, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun NeonFutureSeriesAction(label: String, tone: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    val shape = RoundedCornerShape(999.dp)
    TvClickableSurface(onClick = onClick, shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = NeonCanvas, focusedContainerColor = tone.copy(alpha = .25f), contentColor = tone, focusedContentColor = NeonText), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, tone), shape = shape))) {
        Text(label, modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black)
    }
}
