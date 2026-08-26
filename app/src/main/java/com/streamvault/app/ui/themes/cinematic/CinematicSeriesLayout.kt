package com.streamvault.app.ui.themes.cinematic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
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

/** Presentation-only episodic index for Cinematic using the shared SeriesUiState and actions. */
@Composable
internal fun CinematicSeriesLayout(
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
    val previewRows = uiState.seriesByCategory.entries
        .filter { (_, series) -> series.isNotEmpty() }
        .map { (title, series) -> CinematicSeriesPreviewRow(title, series) }

    Row(
        modifier = modifier.fillMaxSize().background(CinematicCanvas),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        CinematicSeriesCollectionRail(
            categories = uiState.categories,
            selectedCategory = uiState.selectedCategory,
            isCategoryLocked = isCategoryLocked,
            onCategoryClick = onCategoryClick,
            onCategoryLongClick = onCategoryLongClick
        )
        Column(modifier = Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CinematicSeriesControlDesk(
                query = uiState.searchQuery,
                selectedFilter = uiState.selectedLibraryFilterType,
                selectedSort = uiState.selectedLibrarySortBy,
                focusRequester = initialFocusRequester,
                onQueryChange = onQueryChange,
                onFilterChange = onFilterChange,
                onSortChange = onSortChange
            )
            if (uiState.selectedCategory == null) {
                CinematicSeriesPreviewArchive(
                    rows = previewRows,
                    isSeriesLocked = isSeriesLocked,
                    onSeriesClick = onSeriesClick,
                    onSeriesLongClick = onSeriesLongClick,
                    isLoading = uiState.isLoadingPreviewRows,
                    canLoadMore = uiState.hasMorePreviewRows,
                    onLoadMore = onLoadMorePreview
                )
            } else {
                Text(
                    text = uiState.selectedCategory.uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = CinematicText,
                    fontWeight = FontWeight.Black
                )
                CinematicSelectedSeriesArchive(
                    series = uiState.selectedCategoryItems,
                    isSeriesLocked = isSeriesLocked,
                    onSeriesClick = onSeriesClick,
                    onSeriesLongClick = onSeriesLongClick,
                    isLoading = uiState.isLoadingSelectedCategory || uiState.isLoadingMoreSelectedCategory,
                    canLoadMore = uiState.canLoadMoreSelectedCategory,
                    onLoadMore = onLoadMoreSelected
                )
            }
        }
    }
}

private data class CinematicSeriesPreviewRow(val title: String, val series: List<Series>)

@Composable
private fun CinematicSeriesCollectionRail(
    categories: List<Category>,
    selectedCategory: String?,
    isCategoryLocked: (Category) -> Boolean,
    onCategoryClick: (Category) -> Unit,
    onCategoryLongClick: (Category) -> Unit
) {
    Column(
        modifier = Modifier.width(236.dp).fillMaxHeight().background(CinematicPanel, RoundedCornerShape(24.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("SERIES INDEX", style = MaterialTheme.typography.labelMedium, color = CinematicGold, fontWeight = FontWeight.Black)
        Text("Choose a collection", style = MaterialTheme.typography.bodySmall, color = CinematicMuted)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(categories, key = { it.id }) { category ->
                CinematicCategoryCard(
                    category = category,
                    isSelected = category.name == selectedCategory,
                    isLocked = isCategoryLocked(category),
                    onClick = { onCategoryClick(category) },
                    onLongClick = { onCategoryLongClick(category) }
                )
            }
        }
    }
}

@Composable
private fun CinematicSeriesControlDesk(
    query: String,
    selectedFilter: LibraryFilterType,
    selectedSort: LibrarySortBy,
    focusRequester: FocusRequester,
    onQueryChange: (String) -> Unit,
    onFilterChange: (LibraryFilterType) -> Unit,
    onSortChange: (LibrarySortBy) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().background(CinematicPanel, RoundedCornerShape(22.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SearchInput(
            value = query,
            onValueChange = onQueryChange,
            placeholder = androidx.compose.ui.res.stringResource(R.string.search_hint),
            focusRequester = focusRequester
        )
        CinematicSeriesControlRow("FILTER", LibraryFilterType.entries.toList(), selectedFilter, { it.name.replace('_', ' ') }, onFilterChange)
        CinematicSeriesControlRow("ORDER", LibrarySortBy.entries.toList(), selectedSort, { it.name.replace('_', ' ') }, onSortChange)
    }
}

@Composable
private fun <T> CinematicSeriesControlRow(label: String, values: List<T>, selected: T, text: (T) -> String, onSelected: (T) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(58.dp), style = MaterialTheme.typography.labelMedium, color = CinematicGold, fontWeight = FontWeight.Black)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(values, key = { text(it) }) { value ->
                val shape = RoundedCornerShape(999.dp)
                TvClickableSurface(
                    onClick = { onSelected(value) },
                    shape = ClickableSurfaceDefaults.shape(shape),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = if (value == selected) CinematicWine.copy(alpha = .52f) else CinematicCanvas,
                        focusedContainerColor = CinematicPanelRaised,
                        contentColor = if (value == selected) CinematicGold else CinematicMuted,
                        focusedContentColor = CinematicText
                    ),
                    border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = androidx.compose.foundation.BorderStroke(2.dp, CinematicGold), shape = shape))
                ) {
                    Text(text(value), modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CinematicSeriesPreviewArchive(
    rows: List<CinematicSeriesPreviewRow>,
    isSeriesLocked: (Series) -> Boolean,
    onSeriesClick: (Series) -> Unit,
    onSeriesLongClick: (Series) -> Unit,
    isLoading: Boolean,
    canLoadMore: Boolean,
    onLoadMore: () -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        if (rows.isEmpty() && !isLoading) item("cinematic_series_preview_empty") { CinematicSeriesEmptyState("No series collections match this search.") }
        items(rows, key = { it.title }) { row ->
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(row.title.uppercase(), style = MaterialTheme.typography.titleLarge, color = CinematicText, fontWeight = FontWeight.Black)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(row.series, key = { it.id }) { series ->
                        CinematicSeriesTitleCard(series, isSeriesLocked(series), { onSeriesClick(series) }, { onSeriesLongClick(series) })
                    }
                }
            }
        }
        if (isLoading) item("cinematic_series_preview_loading") { CinematicSeriesStatus("Loading more collections…") }
        if (canLoadMore && !isLoading) item("cinematic_series_preview_load_more") { CinematicSeriesLoadMore("LOAD MORE COLLECTIONS", onLoadMore) }
    }
}

@Composable
private fun CinematicSelectedSeriesArchive(
    series: List<Series>,
    isSeriesLocked: (Series) -> Boolean,
    onSeriesClick: (Series) -> Unit,
    onSeriesLongClick: (Series) -> Unit,
    isLoading: Boolean,
    canLoadMore: Boolean,
    onLoadMore: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (series.isEmpty() && !isLoading) {
            CinematicSeriesEmptyState("No series match this collection and filter.")
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(232.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(series, key = { it.id }) { item ->
                    CinematicSeriesTitleCard(item, isSeriesLocked(item), { onSeriesClick(item) }, { onSeriesLongClick(item) })
                }
            }
        }
        if (isLoading) CinematicSeriesStatus("Loading series…")
        if (canLoadMore && !isLoading) CinematicSeriesLoadMore("LOAD MORE SERIES", onLoadMore)
    }
}

@Composable
private fun CinematicSeriesTitleCard(series: Series, isLocked: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    TvClickableSurface(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = Modifier.width(232.dp),
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = CinematicPanel, focusedContainerColor = CinematicPanelRaised, contentColor = CinematicText, focusedContentColor = CinematicText),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = androidx.compose.foundation.BorderStroke(2.dp, CinematicGold), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.035f)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(190.dp)) {
            AsyncImage(model = series.backdropUrl ?: series.posterUrl, contentDescription = series.name, modifier = Modifier.fillMaxSize().clip(shape), contentScale = ContentScale.Crop)
            Column(
                modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().background(CinematicCanvas.copy(alpha = 0.88f)).padding(13.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(series.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(listOfNotNull(series.genre, series.rating.takeIf { it > 0f }?.let { "★ $it" }).joinToString("  "), style = MaterialTheme.typography.labelSmall, color = CinematicGold, maxLines = 1)
                if (isLocked) Text("LOCKED", style = MaterialTheme.typography.labelSmall, color = CinematicWine)
            }
        }
    }
}

@Composable
private fun CinematicSeriesEmptyState(message: String) {
    Box(modifier = Modifier.fillMaxWidth().background(CinematicPanel, RoundedCornerShape(18.dp)).padding(28.dp)) {
        Text(message, style = MaterialTheme.typography.bodyLarge, color = CinematicMuted)
    }
}

@Composable
private fun CinematicSeriesStatus(message: String) {
    Text(message, modifier = Modifier.fillMaxWidth().padding(16.dp), style = MaterialTheme.typography.bodyMedium, color = CinematicMuted)
}

@Composable
private fun CinematicSeriesLoadMore(label: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(999.dp)
    TvClickableSurface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = CinematicWine, focusedContainerColor = CinematicGold, contentColor = CinematicText, focusedContentColor = CinematicCanvas),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = androidx.compose.foundation.BorderStroke(2.dp, CinematicText), shape = shape))
    ) {
        Text(label, modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black)
    }
}
