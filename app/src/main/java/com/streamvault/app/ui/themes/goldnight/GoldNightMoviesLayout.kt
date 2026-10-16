package com.streamvault.app.ui.themes.goldnight

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.app.ui.components.SearchInput
import com.streamvault.app.ui.screens.movies.MoviesUiState
import com.streamvault.domain.model.Category
import com.streamvault.domain.model.LibraryFilterType
import com.streamvault.domain.model.LibrarySortBy
import com.streamvault.domain.model.Movie

/**
 * Gold Night Movies: عمود الأنواع | عمود قائمة الأفلام (ملصق صغير، عنوان، سنة، تقييم، مدة) | عمود تفاصيل الفيلم المركّز.
 * لا شبكات؛ الضغط يفتح صفحة التفاصيل المشتركة والضغط المطوّل يفتح خيارات العنوان كبقية الثيمات.
 */
@Composable
internal fun GoldNightMoviesLayout(
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
    val films = if (uiState.selectedCategory == null) {
        uiState.moviesByCategory.values.flatten().distinctBy { it.id }
    } else uiState.selectedCategoryItems
    val loading = if (uiState.selectedCategory == null) uiState.isLoadingPreviewRows else uiState.isLoadingSelectedCategory || uiState.isLoadingMoreSelectedCategory
    val canLoadMore = if (uiState.selectedCategory == null) uiState.hasMorePreviewRows else uiState.canLoadMoreSelectedCategory
    var focusedMovieId by remember { mutableStateOf<Long?>(null) }
    val focusedMovie = films.firstOrNull { it.id == focusedMovieId } ?: films.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GoldNightCanvasBrush)
            .padding(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        GoldNightScreenHeader(title = "Movies")
        Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            Column(modifier = Modifier.width(250.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                GoldNightColumnTitle(title = "Genres")
                SearchInput(
                    value = uiState.searchQuery,
                    onValueChange = onQueryChange,
                    placeholder = "Search movies",
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
                    title = uiState.selectedCategory ?: "All Movies",
                    trailing = if (films.isNotEmpty()) films.size.toString() else null
                )
                when {
                    !uiState.hasActiveProvider -> GoldNightStatePanel("No provider", "Choose an active provider before opening movies.")
                    !uiState.errorMessage.isNullOrBlank() -> GoldNightStatePanel("Catalogue error", uiState.errorMessage)
                    films.isEmpty() && !loading -> GoldNightStatePanel("Nothing here", "No movies match this selection.")
                }
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(films, key = { it.id }) { movie ->
                        val locked = isMovieLocked(movie)
                        GoldNightPosterRow(
                            title = if (locked) "Locked title" else movie.name,
                            meta = if (locked) "Protected" else goldNightMovieMeta(movie),
                            rating = if (locked) "" else goldNightRating(movie.rating),
                            posterUrl = if (locked) null else movie.posterUrl,
                            onClick = { onMovieClick(movie) },
                            onLongClick = { onMovieLongClick(movie) },
                            modifier = Modifier.onFocusChanged { if (it.isFocused) focusedMovieId = movie.id }
                        )
                    }
                    if (loading) item("gn_movies_loading") { GoldNightStatePanel("Loading", "Loading more movies…") }
                    if (canLoadMore && !loading) {
                        item("gn_movies_more") {
                            GoldNightButton(
                                label = "Load more",
                                onClick = { if (uiState.selectedCategory == null) onLoadMorePreview() else onLoadMoreSelected() },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
            GoldNightMovieSpotlight(
                movie = focusedMovie,
                locked = focusedMovie?.let(isMovieLocked) == true,
                related = films.filter { it.id != focusedMovie?.id }.take(3),
                onOpen = { focusedMovie?.let(onMovieClick) },
                onOptions = { focusedMovie?.let(onMovieLongClick) },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
    }
}

@Composable
private fun GoldNightMovieSpotlight(
    movie: Movie?,
    locked: Boolean,
    related: List<Movie>,
    onOpen: () -> Unit,
    onOptions: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (movie == null) {
            GoldNightStatePanel("Select a movie", "Details appear here as you move through the list.")
            return@Column
        }
        GoldNightArtwork(
            imageUrl = if (locked) null else movie.backdropUrl ?: movie.posterUrl,
            title = movie.name,
            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
            cornerRadius = 12.dp
        )
        Text(
            text = if (locked) "Locked title" else movie.name,
            style = MaterialTheme.typography.headlineSmall,
            color = GoldNightText,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = listOfNotNull(movie.year, movie.genre).filter { it.isNotBlank() }.joinToString(" • "),
                style = MaterialTheme.typography.bodyMedium,
                color = GoldNightMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Text(text = goldNightRating(movie.rating), style = MaterialTheme.typography.bodyMedium, color = GoldNightGold)
        }
        if (!locked) {
            Text(
                text = movie.plot?.takeIf { it.isNotBlank() } ?: "No description is available for this title.",
                style = MaterialTheme.typography.bodySmall,
                color = GoldNightMuted,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GoldNightButton(label = "Play", onClick = onOpen, primary = true, icon = Icons.Default.PlayArrow)
            GoldNightButton(label = if (movie.youtubeTrailer.isNullOrBlank()) "Details" else "Trailer", onClick = onOpen)
            GoldNightButton(label = if (movie.isFavorite) "In Favorites" else "Add to Favorites", onClick = onOptions)
        }
        if (related.isNotEmpty()) {
            Text(text = "You might also like", style = MaterialTheme.typography.titleMedium, color = GoldNightText)
            related.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.width(3.dp).height(34.dp).background(GoldNightGold))
                    Column {
                        Text(text = item.name, style = MaterialTheme.typography.bodyMedium, color = GoldNightText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(text = item.year.orEmpty(), style = MaterialTheme.typography.labelSmall, color = GoldNightMuted)
                    }
                }
            }
        }
    }
}

/** صف ملصق صغير مشترك بين الأفلام والمسلسلات والبحث. */
@Composable
internal fun GoldNightPosterRow(
    title: String,
    meta: String,
    rating: String,
    posterUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    selected: Boolean = false
) {
    GoldNightCard(onClick = onClick, onLongClick = onLongClick, selected = selected, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GoldNightArtwork(imageUrl = posterUrl, title = title, modifier = Modifier.width(46.dp).height(64.dp), cornerRadius = 6.dp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (meta.isNotBlank()) {
                        Text(
                            text = meta,
                            style = MaterialTheme.typography.bodySmall,
                            color = GoldNightMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                    if (rating.isNotBlank()) Text(text = rating, style = MaterialTheme.typography.bodySmall, color = GoldNightGold)
                }
            }
        }
    }
}

@Composable
internal fun GoldNightLibraryChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GoldNightCard(onClick = onClick, modifier = modifier, cornerRadius = 8.dp) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = GoldNightMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.clip(RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 8.dp)
        )
    }
}

internal fun goldNightMovieMeta(movie: Movie): String =
    listOfNotNull(
        movie.year?.takeIf { it.isNotBlank() },
        movie.genre?.takeIf { it.isNotBlank() }?.substringBefore(','),
        movie.duration?.takeIf { it.isNotBlank() }
            ?: movie.durationSeconds.takeIf { it > 0 }?.let { goldNightDuration(it * 1000L) }
    ).joinToString(" | ")

internal fun String.goldNightLabel(): String =
    lowercase().split('_').joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }
