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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
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
import com.streamvault.app.ui.screens.movies.MoviesUiState
import com.streamvault.domain.model.Category
import com.streamvault.domain.model.LibraryFilterType
import com.streamvault.domain.model.LibrarySortBy
import com.streamvault.domain.model.Movie

/** Neon Future film catalogue: HUD bands + angled media nodes + a persistent data pane. */
@Composable
internal fun NeonFutureMoviesLayout(
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
    val shownMovies = if (uiState.selectedCategory == null) {
        uiState.moviesByCategory.values.flatten().distinctBy { it.id }
    } else uiState.selectedCategoryItems
    var focusedMovie by remember(shownMovies) { mutableStateOf(shownMovies.firstOrNull()) }
    val canLoadMore = if (uiState.selectedCategory == null) uiState.hasMorePreviewRows else uiState.canLoadMoreSelectedCategory
    val loading = if (uiState.selectedCategory == null) uiState.isLoadingPreviewRows else uiState.isLoadingSelectedCategory || uiState.isLoadingMoreSelectedCategory
    val onLoadMore = if (uiState.selectedCategory == null) onLoadMorePreview else onLoadMoreSelected

    Column(modifier = modifier.fillMaxSize().background(NeonCanvas), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        NeonFutureMovieConsole(uiState, initialFocusRequester, onQueryChange, onFilterChange, onSortChange)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(uiState.categories, key = { it.id }) { category ->
                NeonFutureMovieBand(
                    category = category,
                    selected = category.name == uiState.selectedCategory,
                    locked = isCategoryLocked(category),
                    onClick = { onCategoryClick(category) },
                    onLongClick = { onCategoryLongClick(category) }
                )
            }
        }
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            Column(modifier = Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text((uiState.selectedCategory ?: "FILM SIGNALS").uppercase(), style = MaterialTheme.typography.titleLarge, color = NeonText, fontWeight = FontWeight.Black)
                if (shownMovies.isEmpty() && !loading) {
                    NeonFutureMovieEmpty("No film nodes match this signal band.")
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(158.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(shownMovies, key = { it.id }) { movie ->
                            NeonFutureMovieNode(
                                movie = movie,
                                locked = isMovieLocked(movie),
                                onClick = { onMovieClick(movie) },
                                onLongClick = { onMovieLongClick(movie) },
                                onFocused = { focusedMovie = movie }
                            )
                        }
                    }
                }
                if (loading) Text("SYNCING FILM NODES…", style = MaterialTheme.typography.labelMedium, color = NeonMuted)
                if (canLoadMore && !loading) NeonFutureMovieAction("LOAD MORE", NeonCyan, onLoadMore)
            }
            NeonFutureMovieInspector(focusedMovie, isMovieLocked(focusedMovie ?: return@Row), modifier = Modifier.width(276.dp).fillMaxHeight())
        }
    }
}

@Composable
private fun NeonFutureMovieConsole(uiState: MoviesUiState, focusRequester: FocusRequester, onQueryChange: (String) -> Unit, onFilterChange: (LibraryFilterType) -> Unit, onSortChange: (LibrarySortBy) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(NeonPanel, RoundedCornerShape(14.dp)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("FILM MATRIX", style = MaterialTheme.typography.titleLarge, color = NeonCyan, fontWeight = FontWeight.Black)
            Text("${uiState.selectedCategoryItems.size} ACTIVE", style = MaterialTheme.typography.labelMedium, color = NeonMuted)
        }
        SearchInput(value = uiState.searchQuery, onValueChange = onQueryChange, placeholder = androidx.compose.ui.res.stringResource(R.string.search_hint), focusRequester = focusRequester)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NeonFutureMovieAction("FILTER ${uiState.selectedLibraryFilterType.name.replace('_', ' ')}", NeonPink) {
                val values = LibraryFilterType.entries
                onFilterChange(values[(values.indexOf(uiState.selectedLibraryFilterType) + 1) % values.size])
            }
            NeonFutureMovieAction("ORDER ${uiState.selectedLibrarySortBy.name.replace('_', ' ')}", NeonLime) {
                val values = LibrarySortBy.entries
                onSortChange(values[(values.indexOf(uiState.selectedLibrarySortBy) + 1) % values.size])
            }
        }
    }
}

@Composable
private fun NeonFutureMovieBand(category: Category, selected: Boolean, locked: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    val shape = RoundedCornerShape(7.dp)
    TvClickableSurface(
        onClick = onClick, onLongClick = onLongClick, shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = if (selected) NeonCyan.copy(alpha = .18f) else NeonPanel, focusedContainerColor = NeonPanelRaised, contentColor = NeonText, focusedContentColor = NeonText),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, NeonCyan), shape = shape))
    ) {
        Text(if (locked) "LOCKED" else category.name.uppercase(), modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp), style = MaterialTheme.typography.labelMedium, color = if (selected) NeonCyan else NeonMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun NeonFutureMovieNode(movie: Movie, locked: Boolean, onClick: () -> Unit, onLongClick: () -> Unit, onFocused: () -> Unit) {
    val shape = RoundedCornerShape(7.dp)
    TvClickableSurface(
        onClick = onClick, onLongClick = onLongClick, modifier = Modifier.width(158.dp).onFocusChanged { if (it.isFocused) onFocused() }, shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = NeonPanel, focusedContainerColor = NeonPanelRaised, contentColor = NeonText, focusedContentColor = NeonText),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, NeonLime), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.045f)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(218.dp).clip(RoundedCornerShape(topStart = 7.dp, topEnd = 7.dp)).background(NeonCanvas)) {
                AsyncImage(movie.posterUrl ?: movie.backdropUrl, null, Modifier.fillMaxSize().rotate(-1f), contentScale = ContentScale.Crop)
                if (locked) Text("LOCK", modifier = Modifier.align(Alignment.TopEnd).padding(8.dp), style = MaterialTheme.typography.labelSmall, color = NeonPink)
            }
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(movie.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(listOfNotNull(movie.year, movie.rating.takeIf { it > 0f }?.let { "★ $it" }).joinToString(" "), style = MaterialTheme.typography.labelSmall, color = NeonMuted, maxLines = 1)
            }
        }
    }
}

@Composable
private fun NeonFutureMovieInspector(movie: Movie?, locked: Boolean, modifier: Modifier) {
    Column(modifier = modifier.background(NeonPanel, RoundedCornerShape(12.dp)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("DATA INSPECTOR", style = MaterialTheme.typography.labelLarge, color = NeonPink, fontWeight = FontWeight.Black)
        AsyncImage(movie?.backdropUrl ?: movie?.posterUrl, null, Modifier.fillMaxWidth().height(138.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
        Text(movie?.name ?: "AWAITING TARGET", style = MaterialTheme.typography.titleLarge, color = NeonText, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text(listOfNotNull(movie?.year, movie?.genre).joinToString(" / ").ifBlank { "No metadata selected" }, style = MaterialTheme.typography.labelMedium, color = NeonCyan)
        Text(if (locked) "ACCESS GATED" else movie?.plot.orEmpty().ifBlank { "Focus a film node to inspect metadata." }, style = MaterialTheme.typography.bodyMedium, color = if (locked) NeonPink else NeonMuted, maxLines = 7, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun NeonFutureMovieEmpty(text: String) { Text(text, modifier = Modifier.fillMaxWidth().background(NeonPanel, RoundedCornerShape(10.dp)).padding(24.dp), style = MaterialTheme.typography.bodyLarge, color = NeonMuted) }

@Composable
private fun NeonFutureMovieAction(label: String, tone: Color, onClick: () -> Unit) {
    val shape = RoundedCornerShape(999.dp)
    TvClickableSurface(onClick = onClick, shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = NeonCanvas, focusedContainerColor = tone.copy(alpha = .25f), contentColor = tone, focusedContentColor = NeonText), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, tone), shape = shape))) {
        Text(label, modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black)
    }
}
