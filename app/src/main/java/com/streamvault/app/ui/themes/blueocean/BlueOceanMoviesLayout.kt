package com.streamvault.app.ui.themes.blueocean

/** Style contract: Blue Ocean films are a vertical route chart, not a shared poster shelf. */

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.streamvault.app.ui.screens.movies.MoviesUiState
import com.streamvault.app.ui.theme.LocalThemePresentation
import com.streamvault.domain.model.Category
import com.streamvault.domain.model.LibraryFilterType
import com.streamvault.domain.model.LibrarySortBy
import com.streamvault.domain.model.Movie

@Composable
internal fun BlueOceanMoviesLayout(uiState: MoviesUiState, initialFocusRequester: FocusRequester, isCategoryLocked: (Category) -> Boolean, isMovieLocked: (Movie) -> Boolean, onCategoryClick: (Category) -> Unit, onCategoryLongClick: (Category) -> Unit, onMovieClick: (Movie) -> Unit, onMovieLongClick: (Movie) -> Unit, onQueryChange: (String) -> Unit, onFilterChange: (LibraryFilterType) -> Unit, onSortChange: (LibrarySortBy) -> Unit, onLoadMoreSelected: () -> Unit, onLoadMorePreview: () -> Unit, modifier: Modifier = Modifier) {
    val p = LocalThemePresentation.current
    val s = p.surfaces
    val films = if (uiState.selectedCategory == null) uiState.moviesByCategory.values.flatten().distinctBy { it.id } else uiState.selectedCategoryItems
    val loading = if (uiState.selectedCategory == null) uiState.isLoadingPreviewRows else uiState.isLoadingSelectedCategory || uiState.isLoadingMoreSelectedCategory
    val canLoadMore = if (uiState.selectedCategory == null) uiState.hasMorePreviewRows else uiState.canLoadMoreSelectedCategory
    Row(modifier.fillMaxSize().background(s.canvas).padding(26.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        Column(Modifier.width(226.dp).fillMaxHeight().background(s.browseRail, RoundedCornerShape(30.dp)).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Text("FILM ROUTES", style = MaterialTheme.typography.titleMedium, color = s.textPrimary); Text("VERTICAL INDEX", style = MaterialTheme.typography.labelMedium, color = s.accent); LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) { items(uiState.categories, key = { it.id }) { category -> val selected = category.name == uiState.selectedCategory; BlueOceanFilmControl(if (isCategoryLocked(category)) "LOCKED" else category.name, selected, { onCategoryClick(category) }, { onCategoryLongClick(category) }) } } }
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Column { Text("FILM CURRENT", style = MaterialTheme.typography.displaySmall, color = s.textPrimary); Text("${films.size} TITLES / ${uiState.selectedCategory ?: "ALL ROUTES"}", style = MaterialTheme.typography.labelMedium, color = s.textSecondary) }; SearchInput(value = uiState.searchQuery, onValueChange = onQueryChange, placeholder = "Search film currents", modifier = Modifier.width(340.dp), focusRequester = initialFocusRequester) }; Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { BlueOceanFilmControl("FILTER · ${uiState.selectedLibraryFilterType.name}", false, { onFilterChange(LibraryFilterType.entries[(LibraryFilterType.entries.indexOf(uiState.selectedLibraryFilterType) + 1) % LibraryFilterType.entries.size]) }); BlueOceanFilmControl("SORT · ${uiState.selectedLibrarySortBy.name}", false, { onSortChange(LibrarySortBy.entries[(LibrarySortBy.entries.indexOf(uiState.selectedLibrarySortBy) + 1) % LibrarySortBy.entries.size]) }) }; when { !uiState.hasActiveProvider -> BlueOceanFilmState("NO ACTIVE PROVIDER", "Choose a connected provider before browsing the current."); !uiState.errorMessage.isNullOrBlank() -> BlueOceanFilmState("CATALOGUE CURRENT INTERRUPTED", uiState.errorMessage); films.isEmpty() && !loading -> BlueOceanFilmState("NO FILMS ON THIS ROUTE", "Try another route or search term."); else -> LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) { items(films, key = { it.id }) { movie -> BlueOceanFilmRow(movie, isMovieLocked(movie), { onMovieClick(movie) }, { onMovieLongClick(movie) }) }; if (loading) item { BlueOceanFilmState("LOADING CURRENT", "Gathering titles from the selected route.") }; if (canLoadMore && !loading) item { BlueOceanFilmControl("LOAD NEXT CURRENT", false, { if (uiState.selectedCategory == null) onLoadMorePreview() else onLoadMoreSelected() }) } } } }
    }
}

@Composable
private fun BlueOceanFilmControl(label: String, selected: Boolean, onClick: () -> Unit, onLongClick: () -> Unit = onClick) { val p = LocalThemePresentation.current; val s = p.surfaces; val shape = RoundedCornerShape(16.dp); TvClickableSurface(onClick = onClick, onLongClick = onLongClick, shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = if (selected) s.selectedAccent else s.browseContent, focusedContainerColor = s.focusedSurface, contentColor = s.textPrimary, focusedContentColor = s.textPrimary), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, s.accent), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f)) { Text(label, Modifier.padding(horizontal = 12.dp, vertical = 11.dp), style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis) } }

@Composable
private fun BlueOceanFilmRow(movie: Movie, locked: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) { val p = LocalThemePresentation.current; val s = p.surfaces; val shape = RoundedCornerShape(22.dp); TvClickableSurface(onClick = onClick, onLongClick = onLongClick, modifier = Modifier.fillMaxWidth(), shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = s.browseContent, focusedContainerColor = s.focusedSurface, contentColor = s.textPrimary, focusedContentColor = s.textPrimary), border = ClickableSurfaceDefaults.border(border = Border(border = BorderStroke(1.dp, s.textSecondary.copy(alpha = .2f)), shape = shape), focusedBorder = Border(border = BorderStroke(2.dp, s.accent), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f)) { Row(Modifier.fillMaxWidth().padding(17.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) { Text(movie.year?.toString() ?: "—", style = MaterialTheme.typography.labelLarge, color = s.accent); Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) { Text(if (locked) "LOCKED FILM" else movie.name, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(if (locked) "Protected route" else listOfNotNull(movie.genre, movie.rating?.let { "★ $it" }).joinToString(" · ").ifBlank { "Film current" }, style = MaterialTheme.typography.bodyMedium, color = s.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis) }; Text("OPEN →", style = MaterialTheme.typography.labelLarge, color = s.accent) } } }

@Composable
private fun BlueOceanFilmState(title: String, subtitle: String?) { val s = LocalThemePresentation.current.surfaces; Column(Modifier.fillMaxWidth().background(s.browseContent, RoundedCornerShape(24.dp)).padding(22.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text(title, style = MaterialTheme.typography.titleLarge, color = s.textPrimary); if (!subtitle.isNullOrBlank()) Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = s.textSecondary) } }
