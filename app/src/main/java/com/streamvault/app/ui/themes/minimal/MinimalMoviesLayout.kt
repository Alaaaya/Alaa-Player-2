package com.streamvault.app.ui.themes.minimal

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.app.ui.components.SearchInput
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.app.ui.screens.movies.MoviesUiState
import com.streamvault.domain.model.Category
import com.streamvault.domain.model.LibraryFilterType
import com.streamvault.domain.model.LibrarySortBy
import com.streamvault.domain.model.Movie
import androidx.compose.ui.focus.FocusRequester

/** Minimal film library deliberately uses an editorial list instead of poster shelves. */
@Composable
internal fun MinimalMoviesLayout(
    uiState: MoviesUiState,
    initialFocusRequester: FocusRequester,
    isCategoryLocked: (Category) -> Boolean,
    isMovieLocked: (Movie) -> Boolean,
    onCategoryClick: (Category) -> Unit,
    onCategoryLongClick: (Category) -> Unit,
    onMovieClick: (Movie) -> Unit,
    onMovieLongClick: (Movie) -> Unit,
    onQueryChange: (String) -> Unit,
    onFilterChange: (LibraryFilterType) -> Unit,
    onSortChange: (LibrarySortBy) -> Unit,
    onLoadMoreSelected: () -> Unit,
    onLoadMorePreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val films = if (uiState.selectedCategory == null) uiState.moviesByCategory.values.flatten().distinctBy { it.id } else uiState.selectedCategoryItems
    val loading = if (uiState.selectedCategory == null) uiState.isLoadingPreviewRows else uiState.isLoadingSelectedCategory || uiState.isLoadingMoreSelectedCategory
    val canLoadMore = if (uiState.selectedCategory == null) uiState.hasMorePreviewRows else uiState.canLoadMoreSelectedCategory
    Column(modifier = modifier.fillMaxSize().background(MinimalCanvas).padding(30.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("FILM INDEX", style = MaterialTheme.typography.headlineMedium, color = MinimalText)
            Text("${films.size} TITLES", style = MaterialTheme.typography.labelMedium, color = MinimalMuted)
        }
        SearchInput(value = uiState.searchQuery, onValueChange = onQueryChange, placeholder = "Search films", focusRequester = initialFocusRequester)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MinimalLibraryAction("FILTER: ${uiState.selectedLibraryFilterType.name}") { onFilterChange(LibraryFilterType.entries[(LibraryFilterType.entries.indexOf(uiState.selectedLibraryFilterType) + 1) % LibraryFilterType.entries.size]) }
            MinimalLibraryAction("SORT: ${uiState.selectedLibrarySortBy.name}") { onSortChange(LibrarySortBy.entries[(LibrarySortBy.entries.indexOf(uiState.selectedLibrarySortBy) + 1) % LibrarySortBy.entries.size]) }
        }
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            LazyColumn(modifier = Modifier.width(210.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                item { Text("CATEGORIES", style = MaterialTheme.typography.labelLarge, color = MinimalMuted, modifier = Modifier.padding(bottom = 8.dp)) }
                items(uiState.categories, key = { it.id }) { category ->
                    MinimalListItem(category.name, category.name == uiState.selectedCategory, isCategoryLocked(category), { onCategoryClick(category) }, { onCategoryLongClick(category) })
                }
            }
            LazyColumn(modifier = Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (films.isEmpty() && !loading) item { Text("No films match this selection.", color = MinimalMuted, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(16.dp)) }
                items(films, key = { it.id }) { film ->
                    MinimalListItem(film.name, false, isMovieLocked(film), { onMovieClick(film) }, { onMovieLongClick(film) })
                }
                if (loading) item { Text("Loading catalogue…", color = MinimalMuted, modifier = Modifier.padding(12.dp)) }
                if (canLoadMore && !loading) item { MinimalLibraryAction("LOAD MORE") { if (uiState.selectedCategory == null) onLoadMorePreview() else onLoadMoreSelected() } }
            }
        }
    }
}

@Composable
private fun MinimalLibraryAction(label: String, onClick: () -> Unit) = MinimalListItem(label, false, false, onClick, onClick)

@Composable
private fun MinimalListItem(label: String, selected: Boolean, locked: Boolean, onClick: () -> Unit, onLongClick: () -> Unit, detail: String? = null) {
    val shape = RoundedCornerShape(0.dp)
    TvClickableSurface(onClick = onClick, onLongClick = onLongClick, modifier = Modifier.fillMaxWidth(), shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = if (selected) MinimalPaper else Color.Transparent, focusedContainerColor = MinimalPaper, contentColor = MinimalText, focusedContentColor = MinimalText), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(1.dp, MinimalFocus), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = MinimalFocusedScale)) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(if (locked) "LOCKED" else label, style = MaterialTheme.typography.titleSmall, color = if (locked) MinimalMuted else MinimalText, maxLines = 1, overflow = TextOverflow.Ellipsis)
            detail?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MinimalMuted, maxLines = 2, overflow = TextOverflow.Ellipsis) }
        }
    }
}
