package com.streamvault.app.ui.themes.glass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.streamvault.app.ui.components.SearchInput
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.app.ui.screens.movies.MoviesUiState
import com.streamvault.domain.model.Category
import com.streamvault.domain.model.LibraryFilterType
import com.streamvault.domain.model.LibrarySortBy
import com.streamvault.domain.model.Movie

/** مكتبة أفلام Glass: أعمدة شفافة متراكبة، لا رفوف أو قائمة Minimal تحريرية. */
@Composable
internal fun GlassmorphismMoviesLayout(
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
    Column(modifier = modifier.fillMaxSize().background(GlassCanvas).padding(28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("FILM GLASS LIBRARY", style = MaterialTheme.typography.headlineMedium, color = GlassText)
                Text("TRANSLUCENT CATALOGUE / ${films.size} TITLES", style = MaterialTheme.typography.labelMedium, color = GlassMuted)
            }
            SearchInput(value = uiState.searchQuery, onValueChange = onQueryChange, placeholder = "Search films", focusRequester = initialFocusRequester)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GlassMovieAction("FILTER / ${uiState.selectedLibraryFilterType.name}") { onFilterChange(LibraryFilterType.entries[(LibraryFilterType.entries.indexOf(uiState.selectedLibraryFilterType) + 1) % LibraryFilterType.entries.size]) }
            GlassMovieAction("SORT / ${uiState.selectedLibrarySortBy.name}") { onSortChange(LibrarySortBy.entries[(LibrarySortBy.entries.indexOf(uiState.selectedLibrarySortBy) + 1) % LibrarySortBy.entries.size]) }
        }
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            GlassPane(modifier = Modifier.width(238.dp).fillMaxHeight()) {
                Text("CATEGORIES", style = MaterialTheme.typography.labelLarge, color = GlassAccent)
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    items(uiState.categories, key = { it.id }) { category ->
                        GlassMovieLine(
                            label = if (isCategoryLocked(category)) "LOCKED" else category.name,
                            detail = if (category.name == uiState.selectedCategory) "ACTIVE" else "",
                            selected = category.name == uiState.selectedCategory,
                            onClick = { onCategoryClick(category) },
                            onLongClick = { onCategoryLongClick(category) }
                        )
                    }
                }
            }
            GlassPane(modifier = Modifier.weight(1f).fillMaxHeight()) {
                Text("FILM STREAM", style = MaterialTheme.typography.labelLarge, color = GlassAccent)
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    if (!uiState.hasActiveProvider) item { GlassMovieState("NO PROVIDER", "Choose an active provider before opening films.") }
                    else if (!uiState.errorMessage.isNullOrBlank()) item { GlassMovieState("CATALOGUE ERROR", uiState.errorMessage) }
                    else if (films.isEmpty() && !loading) item { GlassMovieState("EMPTY SELECTION", "No films match this selection.") }
                    items(films, key = { it.id }) { film ->
                        GlassMovieLine(
                            label = if (isMovieLocked(film)) "LOCKED" else film.name,
                            detail = if (isMovieLocked(film)) "Protected" else film.genre ?: film.year?.toString().orEmpty(),
                            selected = false,
                            onClick = { onMovieClick(film) },
                            onLongClick = { onMovieLongClick(film) }
                        )
                    }
                    if (loading) item { GlassMovieState("LOADING", "Loading glass catalogue entries…") }
                    if (canLoadMore && !loading) item { GlassMovieAction("LOAD MORE") { if (uiState.selectedCategory == null) onLoadMorePreview() else onLoadMoreSelected() } }
                }
            }
        }
    }
}

@Composable
private fun GlassPane(modifier: Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier = modifier, shape = RoundedCornerShape(26.dp), colors = SurfaceDefaults.colors(containerColor = GlassPane), border = Border(border = BorderStroke(1.dp, GlassRule), shape = RoundedCornerShape(26.dp))) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
private fun GlassMovieAction(label: String, onClick: () -> Unit) {
    GlassMovieLine(label = label, detail = "", selected = false, onClick = onClick, onLongClick = onClick)
}

@Composable
private fun GlassMovieLine(label: String, detail: String, selected: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    TvClickableSurface(onClick = onClick, onLongClick = onLongClick, modifier = Modifier.fillMaxWidth(), shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = if (selected) GlassAccent.copy(alpha = .22f) else Color.Transparent, focusedContainerColor = GlassPaneFocused, contentColor = GlassText, focusedContentColor = GlassText), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, GlassFocus), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.025f)) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (detail.isNotBlank()) Text(detail, style = MaterialTheme.typography.labelSmall, color = GlassMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun GlassMovieState(title: String, subtitle: String) {
    Column(modifier = Modifier.fillMaxWidth().background(GlassCanvasDeep, RoundedCornerShape(18.dp)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = GlassText)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = GlassMuted)
    }
}
