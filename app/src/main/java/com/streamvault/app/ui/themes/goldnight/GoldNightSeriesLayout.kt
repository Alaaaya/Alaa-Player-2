package com.streamvault.app.ui.themes.goldnight

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.app.ui.components.SearchInput
import com.streamvault.app.ui.screens.series.SeriesUiState
import com.streamvault.domain.model.Category
import com.streamvault.domain.model.LibraryFilterType
import com.streamvault.domain.model.LibrarySortBy
import com.streamvault.domain.model.Series

/** Gold Night Series: عمود الأنواع | عمود المسلسلات | عمود معاينة المسلسل المركّز، ثم صفحة المواسم والحلقات. */
@Composable
internal fun GoldNightSeriesLayout(
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
    val entries = if (uiState.selectedCategory == null) {
        uiState.seriesByCategory.values.flatten().distinctBy { it.id }
    } else uiState.selectedCategoryItems
    val loading = if (uiState.selectedCategory == null) uiState.isLoadingPreviewRows else uiState.isLoadingSelectedCategory || uiState.isLoadingMoreSelectedCategory
    val canLoadMore = if (uiState.selectedCategory == null) uiState.hasMorePreviewRows else uiState.canLoadMoreSelectedCategory
    var focusedSeriesId by remember { mutableStateOf<Long?>(null) }
    val focused = entries.firstOrNull { it.id == focusedSeriesId } ?: entries.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GoldNightCanvasBrush)
            .padding(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        GoldNightScreenHeader(title = "Series")
        Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            Column(modifier = Modifier.width(250.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                GoldNightColumnTitle(title = "Genres")
                SearchInput(
                    value = uiState.searchQuery,
                    onValueChange = onQueryChange,
                    placeholder = "Search series",
                    modifier = Modifier.fillMaxWidth(),
                    focusRequester = initialFocusRequester
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    GoldNightLibraryChip(
                        label = uiState.selectedLibraryFilterType.name.goldNightLabel(),
                        onClick = {
                            val all = LibraryFilterType.entries
                            onFilterChange(all[(all.indexOf(uiState.selectedLibraryFilterType) + 1) % all.size])
                        },
                        modifier = Modifier.weight(1f)
                    )
                    GoldNightLibraryChip(
                        label = uiState.selectedLibrarySortBy.name.goldNightLabel(),
                        onClick = {
                            val all = LibrarySortBy.entries
                            onSortChange(all[(all.indexOf(uiState.selectedLibrarySortBy) + 1) % all.size])
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(uiState.categories, key = { it.id }) { category ->
                        val selected = category.name == uiState.selectedCategory
                        GoldNightCard(
                            onClick = { onCategoryClick(category) },
                            onLongClick = { onCategoryLongClick(category) },
                            selected = selected,
                            containerColor = Color.Transparent,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isCategoryLocked(category)) "🔒 ${category.name}" else category.name,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (selected) GoldNightGold else GoldNightText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                            )
                        }
                    }
                }
            }
            GoldNightColumnDivider()
            Column(modifier = Modifier.weight(1.2f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                GoldNightColumnTitle(
                    title = uiState.selectedCategory ?: "All Series",
                    trailing = if (entries.isNotEmpty()) entries.size.toString() else null
                )
                when {
                    !uiState.hasActiveProvider -> GoldNightStatePanel("No provider", "Choose an active provider before opening series.")
                    !uiState.errorMessage.isNullOrBlank() -> GoldNightStatePanel("Catalogue error", uiState.errorMessage)
                    entries.isEmpty() && !loading -> GoldNightStatePanel("Nothing here", "No series match this selection.")
                }
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(entries, key = { it.id }) { entry ->
                        val locked = isSeriesLocked(entry)
                        GoldNightPosterRow(
                            title = if (locked) "Locked title" else entry.name,
                            meta = if (locked) "Protected" else goldNightSeriesMeta(entry),
                            rating = if (locked) "" else goldNightRating(entry.rating),
                            posterUrl = if (locked) null else entry.posterUrl,
                            onClick = { onSeriesClick(entry) },
                            onLongClick = { onSeriesLongClick(entry) },
                            modifier = Modifier.onFocusChanged { if (it.isFocused) focusedSeriesId = entry.id }
                        )
                    }
                    if (loading) item("gn_series_loading") { GoldNightStatePanel("Loading", "Loading more series…") }
                    if (canLoadMore && !loading) {
                        item("gn_series_more") {
                            GoldNightButton(
                                label = "Load more",
                                onClick = { if (uiState.selectedCategory == null) onLoadMorePreview() else onLoadMoreSelected() },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
            Column(modifier = Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (focused == null) {
                    GoldNightStatePanel("Select a series", "Details appear here as you move through the list.")
                } else {
                    val locked = isSeriesLocked(focused)
                    GoldNightArtwork(
                        imageUrl = if (locked) null else focused.backdropUrl ?: focused.posterUrl,
                        title = focused.name,
                        modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
                        cornerRadius = 12.dp
                    )
                    Text(
                        text = if (locked) "Locked title" else focused.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = GoldNightText,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = goldNightSeriesMeta(focused),
                            style = MaterialTheme.typography.bodyMedium,
                            color = GoldNightMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Text(text = goldNightRating(focused.rating), style = MaterialTheme.typography.bodyMedium, color = GoldNightGold)
                    }
                    if (!locked) {
                        Text(
                            text = focused.plot?.takeIf { it.isNotBlank() } ?: "No description is available for this series.",
                            style = MaterialTheme.typography.bodySmall,
                            color = GoldNightMuted,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GoldNightButton(label = "Episodes", onClick = { onSeriesClick(focused) }, primary = true, icon = Icons.Default.PlayArrow)
                        GoldNightButton(label = if (focused.isFavorite) "In Favorites" else "Add to Favorites", onClick = { onSeriesLongClick(focused) })
                    }
                }
            }
        }
    }
}

internal fun goldNightSeriesMeta(series: Series): String =
    listOfNotNull(
        series.releaseDate?.takeIf { it.isNotBlank() }?.take(4),
        series.genre?.takeIf { it.isNotBlank() }?.substringBefore(','),
        series.seasons.size.takeIf { it > 0 }?.let { if (it == 1) "1 season" else "$it seasons" }
    ).joinToString(" | ")
