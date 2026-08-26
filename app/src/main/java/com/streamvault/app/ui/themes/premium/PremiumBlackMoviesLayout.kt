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
import com.streamvault.app.ui.screens.movies.MoviesUiState
import com.streamvault.domain.model.Category
import com.streamvault.domain.model.LibraryFilterType
import com.streamvault.domain.model.LibrarySortBy
import com.streamvault.domain.model.Movie

@Composable
internal fun PremiumBlackMoviesLayout(uiState: MoviesUiState, initialFocusRequester: FocusRequester, isCategoryLocked: (Category) -> Boolean, isMovieLocked: (Movie) -> Boolean, onCategoryClick: (Category) -> Unit, onCategoryLongClick: (Category) -> Unit, onMovieClick: (Movie) -> Unit, onMovieLongClick: (Movie) -> Unit, onQueryChange: (String) -> Unit, onFilterChange: (LibraryFilterType) -> Unit, onSortChange: (LibrarySortBy) -> Unit, onLoadMoreSelected: () -> Unit, onLoadMorePreview: () -> Unit, modifier: Modifier = Modifier) {
    val films = if (uiState.selectedCategory == null) uiState.moviesByCategory.values.flatten().distinctBy { it.id } else uiState.selectedCategoryItems
    val loading = if (uiState.selectedCategory == null) uiState.isLoadingPreviewRows else uiState.isLoadingSelectedCategory || uiState.isLoadingMoreSelectedCategory
    val canLoadMore = if (uiState.selectedCategory == null) uiState.hasMorePreviewRows else uiState.canLoadMoreSelectedCategory
    LazyColumn(modifier.fillMaxSize().background(PremiumCanvas).padding(28.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item("premium_movies_head") { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) { Column(verticalArrangement = Arrangement.spacedBy(5.dp)) { Text("FILMS", style = MaterialTheme.typography.displaySmall, color = PremiumText); Text("PREMIUM COLLECTION / ${films.size} TITLES", style = MaterialTheme.typography.labelMedium, color = PremiumMuted) }; SearchInput(value = uiState.searchQuery, onValueChange = onQueryChange, placeholder = "Search films", modifier = Modifier.width(320.dp), focusRequester = initialFocusRequester) } }
        item("premium_movies_tools") { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { PremiumMovieControl(label = "FILTER / ${uiState.selectedLibraryFilterType.name}", onClick = { onFilterChange(LibraryFilterType.entries[(LibraryFilterType.entries.indexOf(uiState.selectedLibraryFilterType) + 1) % LibraryFilterType.entries.size]) }); PremiumMovieControl(label = "SORT / ${uiState.selectedLibrarySortBy.name}", onClick = { onSortChange(LibrarySortBy.entries[(LibrarySortBy.entries.indexOf(uiState.selectedLibrarySortBy) + 1) % LibrarySortBy.entries.size]) }) } }
        item("premium_movies_categories") { LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(uiState.categories, key = { it.id }) { category -> PremiumMovieControl(if (isCategoryLocked(category)) "LOCKED" else category.name, category.name == uiState.selectedCategory, { onCategoryClick(category) }, { onCategoryLongClick(category) }) } } }
        when { !uiState.hasActiveProvider -> item("premium_movies_no_provider") { PremiumMovieState("NO PROVIDER", "Choose an active provider before opening films.") }; !uiState.errorMessage.isNullOrBlank() -> item("premium_movies_error") { PremiumMovieState("CATALOGUE ERROR", uiState.errorMessage) }; films.isEmpty() && !loading -> item("premium_movies_empty") { PremiumMovieState("EMPTY SELECTION", "No films match this selection.") }; else -> item("premium_movies_cards") { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { Text(if (uiState.selectedCategory == null) "FEATURED FILMS" else uiState.selectedCategory.orEmpty(), style = MaterialTheme.typography.titleLarge, color = PremiumText); LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) { items(films, key = { it.id }) { movie -> PremiumMovieCard(movie, isMovieLocked(movie), { onMovieClick(movie) }, { onMovieLongClick(movie) }) } } } } }
        if (loading) item("premium_movies_loading") { PremiumMovieState("LOADING", "Loading premium catalogue entries…") }
        if (canLoadMore && !loading) item("premium_movies_more") { PremiumMovieControl(label = "LOAD MORE", onClick = { if (uiState.selectedCategory == null) onLoadMorePreview() else onLoadMoreSelected() }) }
    }
}

@Composable private fun PremiumMovieControl(label: String, selected: Boolean = false, onClick: () -> Unit, onLongClick: () -> Unit = onClick) { val shape = RoundedCornerShape(8.dp); TvClickableSurface(onClick = onClick, onLongClick = onLongClick, shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = if (selected) PremiumPanelFocused else PremiumPanel, focusedContainerColor = PremiumPanelFocused, contentColor = PremiumText, focusedContentColor = PremiumText), border = ClickableSurfaceDefaults.border(border = Border(border = BorderStroke(1.dp, if (selected) PremiumGold else PremiumMetal), shape = shape), focusedBorder = Border(border = BorderStroke(2.dp, PremiumFocus), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f)) { Text(label, Modifier.padding(horizontal = 15.dp, vertical = 11.dp), style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis) } }
@Composable private fun PremiumMovieCard(movie: Movie, locked: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) { val shape = RoundedCornerShape(10.dp); TvClickableSurface(onClick = onClick, onLongClick = onLongClick, modifier = Modifier.width(210.dp).height(296.dp), shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = PremiumPanel, focusedContainerColor = PremiumPanelFocused, contentColor = PremiumText, focusedContentColor = PremiumText), border = ClickableSurfaceDefaults.border(border = Border(border = BorderStroke(1.dp, PremiumMetal), shape = shape), focusedBorder = Border(border = BorderStroke(2.dp, PremiumFocus), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.03f)) { Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.SpaceBetween) { Column(verticalArrangement = Arrangement.spacedBy(7.dp)) { Text(if (locked) "LOCKED" else movie.name, style = MaterialTheme.typography.titleMedium, maxLines = 3, overflow = TextOverflow.Ellipsis); Text(if (locked) "Protected title" else movie.genre ?: movie.year?.toString().orEmpty(), style = MaterialTheme.typography.bodySmall, color = PremiumMuted, maxLines = 2, overflow = TextOverflow.Ellipsis) }; Text("DETAILS", style = MaterialTheme.typography.labelSmall, color = PremiumGold) } } }
@Composable private fun PremiumMovieState(title: String, subtitle: String) { Column(Modifier.fillMaxWidth().background(PremiumPanel, RoundedCornerShape(10.dp)).padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text(title, style = MaterialTheme.typography.titleMedium, color = PremiumText); Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = PremiumMuted) } }
