package com.streamvault.app.ui.themes.redcinema

/** Red Cinema film library contract: a vertical screening ledger with ticket categories rather than a card shelf. */

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
import com.streamvault.app.ui.screens.movies.MoviesUiState
import com.streamvault.app.ui.theme.LocalThemePresentation
import com.streamvault.domain.model.Category
import com.streamvault.domain.model.LibraryFilterType
import com.streamvault.domain.model.LibrarySortBy
import com.streamvault.domain.model.Movie

@Composable
internal fun RedCinemaScreeningLedger(
    uiState: MoviesUiState, initialFocusRequester: FocusRequester, isCategoryLocked: (Category) -> Boolean, isMovieLocked: (Movie) -> Boolean,
    onCategoryClick: (Category) -> Unit, onCategoryLongClick: (Category) -> Unit, onMovieClick: (Movie) -> Unit, onMovieLongClick: (Movie) -> Unit,
    onQueryChange: (String) -> Unit, onFilterChange: (LibraryFilterType) -> Unit, onSortChange: (LibrarySortBy) -> Unit, onLoadMoreSelected: () -> Unit, onLoadMorePreview: () -> Unit, modifier: Modifier = Modifier
) {
    val surfaces = LocalThemePresentation.current.surfaces
    val films = if (uiState.selectedCategory == null) uiState.moviesByCategory.values.flatten().distinctBy { it.id } else uiState.selectedCategoryItems
    val loading = if (uiState.selectedCategory == null) uiState.isLoadingPreviewRows else uiState.isLoadingSelectedCategory || uiState.isLoadingMoreSelectedCategory
    val canLoadMore = if (uiState.selectedCategory == null) uiState.hasMorePreviewRows else uiState.canLoadMoreSelectedCategory
    Column(modifier.fillMaxSize().background(surfaces.canvas).padding(26.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Column { Text("CINEMA LEDGER", style = MaterialTheme.typography.displaySmall); Text("SCREENINGS / ${uiState.selectedCategory ?: "ALL ACTS"}", style = MaterialTheme.typography.labelMedium, color = surfaces.accent) }
            SearchInput(value = uiState.searchQuery, onValueChange = onQueryChange, placeholder = "Search showings", modifier = Modifier.width(350.dp), focusRequester = initialFocusRequester)
        }
        LazyColumn(modifier = Modifier.fillMaxWidth().weight(.25f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(uiState.categories, key = { it.id }) { category ->
                RedCinemaLedgerTicket("ACT", if (isCategoryLocked(category)) "RESTRICTED" else category.name, category.name == uiState.selectedCategory, { onCategoryClick(category) }, { onCategoryLongClick(category) })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RedCinemaLedgerControl("FILTER · ${uiState.selectedLibraryFilterType.name}") { onFilterChange(LibraryFilterType.entries[(LibraryFilterType.entries.indexOf(uiState.selectedLibraryFilterType) + 1) % LibraryFilterType.entries.size]) }
            RedCinemaLedgerControl("SORT · ${uiState.selectedLibrarySortBy.name}") { onSortChange(LibrarySortBy.entries[(LibrarySortBy.entries.indexOf(uiState.selectedLibrarySortBy) + 1) % LibrarySortBy.entries.size]) }
        }
        when {
            !uiState.hasActiveProvider -> RedCinemaLedgerState("NO ACTIVE PROVIDER", "Choose a provider before opening the cinema ledger.")
            !uiState.errorMessage.isNullOrBlank() -> RedCinemaLedgerState("LEDGER INTERRUPTED", uiState.errorMessage)
            films.isEmpty() && !loading -> RedCinemaLedgerState("NO SCREENINGS", "Try another act or a different search.")
            else -> LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                items(films, key = { it.id }) { movie -> RedCinemaFilmListing(movie, isMovieLocked(movie), { onMovieClick(movie) }, { onMovieLongClick(movie) }) }
                if (loading) item { RedCinemaLedgerState("LOADING PROGRAMME", "Gathering new screenings.") }
                if (canLoadMore && !loading) item { RedCinemaLedgerControl("NEXT PAGE") { if (uiState.selectedCategory == null) onLoadMorePreview() else onLoadMoreSelected() } }
            }
        }
    }
}

@Composable
private fun RedCinemaLedgerTicket(number: String, title: String, selected: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) { val s = LocalThemePresentation.current.surfaces; val shape = RoundedCornerShape(2.dp); TvClickableSurface(onClick = onClick, onLongClick = onLongClick, modifier = Modifier.fillMaxWidth(), shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = if (selected) s.selectedAccent else s.browseContent, focusedContainerColor = s.focusedSurface, contentColor = s.textPrimary, focusedContentColor = s.textPrimary), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, s.accent), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.018f)) { Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) { Text(number, style = MaterialTheme.typography.labelLarge, color = s.accent); Text(title, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis) } } }

@Composable
private fun RedCinemaLedgerControl(label: String, onClick: () -> Unit) { val s = LocalThemePresentation.current.surfaces; val shape = RoundedCornerShape(2.dp); TvClickableSurface(onClick = onClick, shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = s.browseContent, focusedContainerColor = s.focusedSurface, contentColor = s.textPrimary, focusedContentColor = s.textPrimary), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, s.accent), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.018f)) { Text(label, Modifier.padding(horizontal = 12.dp, vertical = 10.dp), style = MaterialTheme.typography.labelMedium) } }

@Composable
private fun RedCinemaFilmListing(movie: Movie, locked: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) { val s = LocalThemePresentation.current.surfaces; val shape = RoundedCornerShape(2.dp); TvClickableSurface(onClick = onClick, onLongClick = onLongClick, modifier = Modifier.fillMaxWidth(), shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = s.browseContent, focusedContainerColor = s.focusedSurface, contentColor = s.textPrimary, focusedContentColor = s.textPrimary), border = ClickableSurfaceDefaults.border(border = Border(border = BorderStroke(1.dp, s.textSecondary.copy(alpha = .25f)), shape = shape), focusedBorder = Border(border = BorderStroke(2.dp, s.accent), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.018f)) { Row(Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) { Text(movie.year?.toString() ?: "—", style = MaterialTheme.typography.labelLarge, color = s.accent); Column(Modifier.weight(1f)) { Text(if (locked) "RESTRICTED FEATURE" else movie.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(if (locked) "Protected showing" else listOfNotNull(movie.genre, movie.rating?.let { "★ $it" }).joinToString(" · ").ifBlank { "Feature presentation" }, style = MaterialTheme.typography.bodySmall, color = s.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis) }; Text("TICKET", style = MaterialTheme.typography.labelMedium, color = s.accent) } } }

@Composable
private fun RedCinemaLedgerState(title: String, subtitle: String?) { val s = LocalThemePresentation.current.surfaces; Column(Modifier.fillMaxWidth().background(s.browseContent, RoundedCornerShape(2.dp)).padding(20.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) { Text(title, style = MaterialTheme.typography.titleLarge); if (!subtitle.isNullOrBlank()) Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = s.textSecondary) } }
