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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
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

/** Presentation-only film archive for Cinematic. It consumes the existing movie state and callbacks. */
@Composable
internal fun CinematicMoviesLayout(
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
    val selectedMovies = uiState.selectedCategoryItems
    val previewRows = remember(uiState.moviesByCategory, uiState.favoriteCategoryName) {
        uiState.moviesByCategory.entries
            .filter { (_, movies) -> movies.isNotEmpty() }
            .map { (title, movies) -> CinematicMoviePreviewRow(title, movies) }
    }
    val featured = remember(selectedMovies) { selectedMovies.firstOrNull() }

    Row(
        modifier = modifier.fillMaxSize().background(CinematicCanvas),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        CinematicMovieCollectionRail(
            categories = uiState.categories,
            selectedCategory = uiState.selectedCategory,
            isCategoryLocked = isCategoryLocked,
            onCategoryClick = onCategoryClick,
            onCategoryLongClick = onCategoryLongClick
        )

        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CinematicMovieControlDesk(
                query = uiState.searchQuery,
                selectedFilter = uiState.selectedLibraryFilterType,
                selectedSort = uiState.selectedLibrarySortBy,
                focusRequester = initialFocusRequester,
                onQueryChange = onQueryChange,
                onFilterChange = onFilterChange,
                onSortChange = onSortChange
            )

            if (uiState.selectedCategory == null) {
                CinematicMoviePreviewArchive(
                    rows = previewRows,
                    isMovieLocked = isMovieLocked,
                    onMovieClick = onMovieClick,
                    onMovieLongClick = onMovieLongClick,
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
                CinematicSelectedMovieArchive(
                    featured = featured,
                    movies = selectedMovies,
                    isMovieLocked = isMovieLocked,
                    onMovieClick = onMovieClick,
                    onMovieLongClick = onMovieLongClick,
                    isLoading = uiState.isLoadingSelectedCategory || uiState.isLoadingMoreSelectedCategory,
                    canLoadMore = uiState.canLoadMoreSelectedCategory,
                    onLoadMore = onLoadMoreSelected
                )
            }
        }
    }
}

private data class CinematicMoviePreviewRow(val title: String, val movies: List<Movie>)

@Composable
private fun CinematicMovieCollectionRail(
    categories: List<Category>,
    selectedCategory: String?,
    isCategoryLocked: (Category) -> Boolean,
    onCategoryClick: (Category) -> Unit,
    onCategoryLongClick: (Category) -> Unit
) {
    Column(
        modifier = Modifier
            .width(236.dp)
            .fillMaxHeight()
            .background(CinematicPanel, RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("THE ARCHIVE", style = MaterialTheme.typography.labelMedium, color = CinematicGold, fontWeight = FontWeight.Black)
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
private fun CinematicMovieControlDesk(
    query: String,
    selectedFilter: LibraryFilterType,
    selectedSort: LibrarySortBy,
    focusRequester: FocusRequester,
    onQueryChange: (String) -> Unit,
    onFilterChange: (LibraryFilterType) -> Unit,
    onSortChange: (LibrarySortBy) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CinematicPanel, RoundedCornerShape(22.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SearchInput(
            value = query,
            onValueChange = onQueryChange,
            placeholder = androidx.compose.ui.res.stringResource(R.string.search_hint),
            focusRequester = focusRequester
        )
        CinematicMovieControlRow(
            label = "FILTER",
            values = LibraryFilterType.entries.toList(),
            selected = selectedFilter,
            text = { it.name.replace('_', ' ') },
            onSelected = onFilterChange
        )
        CinematicMovieControlRow(
            label = "ORDER",
            values = LibrarySortBy.entries.toList(),
            selected = selectedSort,
            text = { it.name.replace('_', ' ') },
            onSelected = onSortChange
        )
    }
}

@Composable
private fun <T> CinematicMovieControlRow(
    label: String,
    values: List<T>,
    selected: T,
    text: (T) -> String,
    onSelected: (T) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(58.dp), style = MaterialTheme.typography.labelMedium, color = CinematicGold, fontWeight = FontWeight.Black)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(values, key = { text(it) }) { value ->
                CinematicMovieControlChip(text(value), selected = value == selected) { onSelected(value) }
            }
        }
    }
}

@Composable
private fun CinematicMovieControlChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(999.dp)
    TvClickableSurface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) CinematicWine.copy(alpha = .52f) else CinematicCanvas,
            focusedContainerColor = CinematicPanelRaised,
            contentColor = if (selected) CinematicGold else CinematicMuted,
            focusedContentColor = CinematicText
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(border = androidx.compose.foundation.BorderStroke(2.dp, CinematicGold), shape = shape)
        )
    ) {
        Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CinematicMoviePreviewArchive(
    rows: List<CinematicMoviePreviewRow>,
    isMovieLocked: (Movie) -> Boolean,
    onMovieClick: (Movie) -> Unit,
    onMovieLongClick: (Movie) -> Unit,
    isLoading: Boolean,
    canLoadMore: Boolean,
    onLoadMore: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        if (rows.isEmpty() && !isLoading) {
            item("cinematic_movies_preview_empty") { CinematicMovieEmptyState("No film collections match this search.") }
        }
        items(rows, key = { it.title }) { row ->
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(row.title.uppercase(), style = MaterialTheme.typography.titleLarge, color = CinematicText, fontWeight = FontWeight.Black)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(row.movies, key = { it.id }) { movie ->
                        CinematicMoviePoster(movie, isMovieLocked(movie), { onMovieClick(movie) }, { onMovieLongClick(movie) })
                    }
                }
            }
        }
        if (isLoading) item("cinematic_movies_preview_loading") { CinematicMovieStatus("Loading more collections…") }
        if (canLoadMore && !isLoading) item("cinematic_movies_preview_load_more") { CinematicMovieLoadMore("LOAD MORE COLLECTIONS", onLoadMore) }
    }
}

@Composable
private fun CinematicSelectedMovieArchive(
    featured: Movie?,
    movies: List<Movie>,
    isMovieLocked: (Movie) -> Boolean,
    onMovieClick: (Movie) -> Unit,
    onMovieLongClick: (Movie) -> Unit,
    isLoading: Boolean,
    canLoadMore: Boolean,
    onLoadMore: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        featured?.let { movie -> CinematicMovieHero(movie, isMovieLocked(movie), { onMovieClick(movie) }) }
        if (movies.isEmpty() && !isLoading) {
            CinematicMovieEmptyState("No films match this collection and filter.")
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(154.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(movies, key = { it.id }) { movie ->
                    CinematicMoviePoster(movie, isMovieLocked(movie), { onMovieClick(movie) }, { onMovieLongClick(movie) })
                }
            }
        }
        if (isLoading) CinematicMovieStatus("Loading films…")
        if (canLoadMore && !isLoading) CinematicMovieLoadMore("LOAD MORE FILMS", onLoadMore)
    }
}

@Composable
private fun CinematicMovieHero(movie: Movie, isLocked: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(26.dp)
    TvClickableSurface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = CinematicPanel, focusedContainerColor = CinematicPanelRaised, contentColor = CinematicText, focusedContentColor = CinematicText),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = androidx.compose.foundation.BorderStroke(2.dp, CinematicGold), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.01f),
        modifier = Modifier.fillMaxWidth().height(224.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(model = movie.backdropUrl ?: movie.posterUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(CinematicCanvas.copy(alpha = 0.95f), Color.Transparent))))
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(22.dp).fillMaxWidth(.62f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text("CINEMATIC FEATURE", style = MaterialTheme.typography.labelSmall, color = CinematicGold)
                Text(movie.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, maxLines = 2)
                Text(listOfNotNull(movie.year, movie.genre).joinToString(" · ").ifBlank { movie.plot.orEmpty() }, style = MaterialTheme.typography.bodySmall, color = CinematicMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (isLocked) Text("LOCKED", style = MaterialTheme.typography.labelSmall, color = CinematicGold)
            }
        }
    }
}

@Composable
private fun CinematicMoviePoster(movie: Movie, isLocked: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    var focused by remember(movie.id) { mutableStateOf(false) }
    TvClickableSurface(
        onClick = onClick,
        onLongClick = onLongClick,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = CinematicPanel, focusedContainerColor = CinematicPanelRaised, contentColor = CinematicText, focusedContentColor = CinematicText),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = androidx.compose.foundation.BorderStroke(2.dp, CinematicWine), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.045f),
        modifier = Modifier.width(164.dp).onFocusChanged { focused = it.isFocused }
    ) {
        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Box(modifier = Modifier.fillMaxWidth().height(202.dp).clip(RoundedCornerShape(10.dp)).background(CinematicCanvas)) {
                AsyncImage(model = movie.posterUrl ?: movie.backdropUrl, contentDescription = movie.name, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                if (isLocked) Text("LOCK", style = MaterialTheme.typography.labelSmall, color = CinematicGold, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp))
            }
            Text(movie.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(listOfNotNull(movie.year, movie.rating.takeIf { it > 0f }?.let { "★ $it" }).joinToString("  "), style = MaterialTheme.typography.labelSmall, color = if (focused) CinematicGold else CinematicMuted, maxLines = 1)
        }
    }
}

@Composable
private fun CinematicMovieEmptyState(message: String) {
    Box(modifier = Modifier.fillMaxWidth().background(CinematicPanel, RoundedCornerShape(18.dp)).padding(28.dp)) {
        Text(message, style = MaterialTheme.typography.bodyLarge, color = CinematicMuted)
    }
}

@Composable
private fun CinematicMovieStatus(message: String) {
    Text(message, modifier = Modifier.fillMaxWidth().padding(16.dp), style = MaterialTheme.typography.bodyMedium, color = CinematicMuted)
}

@Composable
private fun CinematicMovieLoadMore(label: String, onClick: () -> Unit) {
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
