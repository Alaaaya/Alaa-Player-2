package com.streamvault.app.ui.themes.streaming

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

/** مكتبة Streaming Platform للمسلسلات: رفوف أفقية موسمية لا قائمة Glass أو شكل Minimal التحريري. */
@Composable
internal fun StreamingPlatformSeriesLayout(
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
    val series = if (uiState.selectedCategory == null) uiState.seriesByCategory.values.flatten().distinctBy { it.id } else uiState.selectedCategoryItems
    val loading = if (uiState.selectedCategory == null) uiState.isLoadingPreviewRows else uiState.isLoadingSelectedCategory || uiState.isLoadingMoreSelectedCategory
    val canLoadMore = if (uiState.selectedCategory == null) uiState.hasMorePreviewRows else uiState.canLoadMoreSelectedCategory
    LazyColumn(modifier = modifier.fillMaxSize().background(StreamingCanvas).padding(28.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item("streaming_series_head") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("SERIES", style = MaterialTheme.typography.displaySmall, color = StreamingText)
                    Text("STREAMING CATALOGUE / ${series.size} TITLES", style = MaterialTheme.typography.labelMedium, color = StreamingMuted)
                }
                SearchInput(value = uiState.searchQuery, onValueChange = onQueryChange, placeholder = "Search series", focusRequester = initialFocusRequester, modifier = Modifier.width(320.dp))
            }
        }
        item("streaming_series_controls") {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StreamingSeriesControl(label = "FILTER / ${uiState.selectedLibraryFilterType.name}", onClick = { onFilterChange(LibraryFilterType.entries[(LibraryFilterType.entries.indexOf(uiState.selectedLibraryFilterType) + 1) % LibraryFilterType.entries.size]) })
                StreamingSeriesControl(label = "SORT / ${uiState.selectedLibrarySortBy.name}", onClick = { onSortChange(LibrarySortBy.entries[(LibrarySortBy.entries.indexOf(uiState.selectedLibrarySortBy) + 1) % LibrarySortBy.entries.size]) })
            }
        }
        item("streaming_series_categories") {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("BROWSE COLLECTIONS", style = MaterialTheme.typography.titleMedium, color = StreamingText)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    items(uiState.categories, key = { it.id }) { category ->
                        StreamingSeriesControl(label = if (isCategoryLocked(category)) "LOCKED" else category.name, selected = category.name == uiState.selectedCategory, onClick = { onCategoryClick(category) }, onLongClick = { onCategoryLongClick(category) })
                    }
                }
            }
        }
        when {
            !uiState.hasActiveProvider -> item("streaming_series_no_provider") { StreamingSeriesState("NO PROVIDER", "Choose an active provider before opening series.") }
            !uiState.errorMessage.isNullOrBlank() -> item("streaming_series_error") { StreamingSeriesState("CATALOGUE ERROR", uiState.errorMessage) }
            series.isEmpty() && !loading -> item("streaming_series_empty") { StreamingSeriesState("EMPTY SELECTION", "No series match this selection.") }
            else -> item("streaming_series_shelf") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(if (uiState.selectedCategory == null) "SERIES FOR YOU" else uiState.selectedCategory.orEmpty(), style = MaterialTheme.typography.titleLarge, color = StreamingText)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                        items(series, key = { it.id }) { entry -> StreamingSeriesCard(entry, isSeriesLocked(entry), { onSeriesClick(entry) }, { onSeriesLongClick(entry) }) }
                    }
                }
            }
        }
        if (loading) item("streaming_series_loading") { StreamingSeriesState("LOADING", "Loading streaming series entries…") }
        if (canLoadMore && !loading) item("streaming_series_more") { StreamingSeriesControl(label = "LOAD MORE", onClick = { if (uiState.selectedCategory == null) onLoadMorePreview() else onLoadMoreSelected() }) }
    }
}

@Composable
private fun StreamingSeriesControl(label: String, selected: Boolean = false, onClick: () -> Unit, onLongClick: () -> Unit = onClick) {
    val shape = RoundedCornerShape(12.dp)
    TvClickableSurface(onClick = onClick, onLongClick = onLongClick, shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = if (selected) StreamingPanelFocused else StreamingPanel, focusedContainerColor = StreamingPanelFocused, contentColor = StreamingText, focusedContentColor = StreamingText), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, StreamingFocus), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.025f)) {
        Text(label, Modifier.padding(horizontal = 15.dp, vertical = 11.dp), style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun StreamingSeriesCard(series: Series, locked: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    TvClickableSurface(onClick = onClick, onLongClick = onLongClick, modifier = Modifier.width(242.dp).height(218.dp), shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = StreamingPanel, focusedContainerColor = StreamingPanelFocused, contentColor = StreamingText, focusedContentColor = StreamingText), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, StreamingFocus), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.04f)) {
        Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(if (locked) "LOCKED" else series.name, style = MaterialTheme.typography.titleMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Text(if (locked) "Protected title" else series.genre ?: series.releaseDate.orEmpty(), style = MaterialTheme.typography.bodySmall, color = StreamingMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text("SEASONS & EPISODES", style = MaterialTheme.typography.labelSmall, color = StreamingAccent)
        }
    }
}

@Composable
private fun StreamingSeriesState(title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth().background(StreamingPanel, RoundedCornerShape(16.dp)).padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = StreamingText)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = StreamingMuted)
    }
}
