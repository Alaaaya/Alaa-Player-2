package com.streamvault.app.ui.themes.cinematic

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.streamvault.app.R
import com.streamvault.app.ui.components.SearchInput
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.app.ui.screens.search.SearchTab
import com.streamvault.app.ui.screens.search.SearchUiState
import com.streamvault.domain.model.CatalogCompleteness
import com.streamvault.domain.model.Channel
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.Series

/**
 * Presentation-only search desk for Cinematic.
 *
 * The layout intentionally uses a search desk, filter strip and screening shelves rather
 * than the shared search rails and cards. Querying, history, PIN protection and item actions
 * remain owned by SearchScreen and SearchViewModel.
 */
@Composable
internal fun CinematicSearchLayout(
    query: String,
    selectedTab: SearchTab,
    recentQueries: List<String>,
    uiState: SearchUiState,
    recordingChannelIds: Set<Long>,
    scheduledChannelIds: Set<Long>,
    searchFocusRequester: FocusRequester,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onTabSelected: (SearchTab) -> Unit,
    onRecentQuerySelected: (String) -> Unit,
    onClearRecentQueries: () -> Unit,
    onBuildCompleteIndex: () -> Unit,
    onChannelClick: (Channel) -> Unit,
    onChannelLongClick: (Channel) -> Unit,
    onMovieClick: (Movie) -> Unit,
    onMovieLongClick: (Movie) -> Unit,
    onSeriesClick: (Series) -> Unit,
    onSeriesLongClick: (Series) -> Unit,
    isChannelLocked: (Channel) -> Boolean,
    isMovieLocked: (Movie) -> Boolean,
    isSeriesLocked: (Series) -> Boolean
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CinematicCanvas),
        contentPadding = PaddingValues(start = 42.dp, top = 30.dp, end = 42.dp, bottom = 42.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item("cinematic_search_heading") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "SEARCH ARCHIVE",
                    style = MaterialTheme.typography.displaySmall,
                    color = CinematicText,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Find a channel, film or series without leaving your screening room.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = CinematicMuted
                )
            }
        }

        item("cinematic_search_desk") {
            CinematicSearchDesk(
                query = query,
                selectedTab = selectedTab,
                focusRequester = searchFocusRequester,
                onQueryChange = onQueryChange,
                onSearch = onSearch,
                onTabSelected = onTabSelected
            )
        }

        if (recentQueries.isNotEmpty() && query.isBlank()) {
            item("cinematic_search_recent") {
                CinematicRecentSearches(
                    recentQueries = recentQueries,
                    onRecentQuerySelected = onRecentQuerySelected,
                    onClearRecentQueries = onClearRecentQueries
                )
            }
        }

        when {
            !uiState.hasActiveProvider -> item("cinematic_search_no_provider") {
                CinematicSearchState(
                    title = stringResource(R.string.search_no_provider_title),
                    subtitle = stringResource(R.string.search_no_provider_subtitle)
                )
            }

            uiState.queryLength < 2 -> item("cinematic_search_ready") {
                CinematicSearchState(
                    title = stringResource(R.string.search_ready_title),
                    subtitle = stringResource(R.string.search_type_to_search)
                )
            }

            uiState.isLoading -> item("cinematic_search_loading") {
                CinematicSearchState(
                    title = stringResource(R.string.search_loading_title),
                    subtitle = stringResource(R.string.search_loading_subtitle)
                )
            }

            uiState.isEmpty && uiState.hasSearchError -> item("cinematic_search_error") {
                CinematicSearchState(
                    title = stringResource(R.string.search_error_title),
                    subtitle = stringResource(R.string.search_error_subtitle)
                )
            }

            uiState.isEmpty -> item("cinematic_search_empty") {
                CinematicSearchState(
                    title = stringResource(R.string.search_no_results_title),
                    subtitle = stringResource(R.string.search_no_results, query)
                )
            }

            else -> {
                item("cinematic_search_summary") {
                    CinematicSearchSummary(
                        uiState = uiState,
                        onBuildCompleteIndex = onBuildCompleteIndex
                    )
                }

                if (selectedTab == SearchTab.ALL || selectedTab == SearchTab.LIVE) {
                    if (uiState.channels.isNotEmpty()) {
                        item("cinematic_search_live") {
                            CinematicSearchChannelShelf(
                                channels = uiState.channels,
                                recordingChannelIds = recordingChannelIds,
                                scheduledChannelIds = scheduledChannelIds,
                                onClick = onChannelClick,
                                onLongClick = onChannelLongClick,
                                isLocked = isChannelLocked
                            )
                        }
                    }
                }

                if (selectedTab == SearchTab.ALL || selectedTab == SearchTab.MOVIES) {
                    if (uiState.movies.isNotEmpty()) {
                        item("cinematic_search_films") {
                            CinematicSearchMovieShelf(
                                movies = uiState.movies,
                                onClick = onMovieClick,
                                onLongClick = onMovieLongClick,
                                isLocked = isMovieLocked
                            )
                        }
                    }
                }

                if (selectedTab == SearchTab.ALL || selectedTab == SearchTab.SERIES) {
                    if (uiState.series.isNotEmpty()) {
                        item("cinematic_search_series") {
                            CinematicSearchSeriesShelf(
                                series = uiState.series,
                                onClick = onSeriesClick,
                                onLongClick = onSeriesLongClick,
                                isLocked = isSeriesLocked
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CinematicSearchDesk(
    query: String,
    selectedTab: SearchTab,
    focusRequester: FocusRequester,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onTabSelected: (SearchTab) -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        colors = SurfaceDefaults.colors(containerColor = CinematicPanel),
        border = Border(
            border = BorderStroke(1.dp, CinematicWine.copy(alpha = 0.52f)),
            shape = shape
        )
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "THE SCREENING DESK",
                style = MaterialTheme.typography.labelLarge,
                color = CinematicGold,
                fontWeight = FontWeight.Black
            )
            SearchInput(
                value = query,
                onValueChange = onQueryChange,
                placeholder = stringResource(R.string.search_hint),
                focusRequester = focusRequester,
                onSearch = onSearch
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(SearchTab.entries.toList(), key = { it.name }) { tab ->
                    CinematicSearchTab(
                        label = stringResource(tab.titleRes),
                        selected = tab == selectedTab,
                        onClick = { onTabSelected(tab) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CinematicSearchTab(label: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(999.dp)
    TvClickableSurface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) CinematicWine.copy(alpha = 0.55f) else CinematicCanvas,
            focusedContainerColor = CinematicPanelRaised,
            contentColor = if (selected) CinematicGold else CinematicMuted,
            focusedContentColor = CinematicText
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(border = BorderStroke(2.dp, CinematicGold), shape = shape)
        )
    ) {
        Text(
            text = label.uppercase(),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CinematicRecentSearches(
    recentQueries: List<String>,
    onRecentQuerySelected: (String) -> Unit,
    onClearRecentQueries: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.search_recent_title).uppercase(),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                color = CinematicText,
                fontWeight = FontWeight.Black
            )
            CinematicTextAction(label = stringResource(R.string.search_clear_history), onClick = onClearRecentQueries)
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(recentQueries, key = { it }) { recent ->
                CinematicSearchTab(label = recent, selected = false) { onRecentQuerySelected(recent) }
            }
        }
    }
}

@Composable
private fun CinematicSearchSummary(uiState: SearchUiState, onBuildCompleteIndex: () -> Unit) {
    val catalogMessageRes = when (uiState.catalogCompleteness) {
        CatalogCompleteness.COMPLETE -> R.string.search_catalog_complete
        CatalogCompleteness.PARTIAL -> R.string.search_catalog_downloaded_only
        CatalogCompleteness.INDEXING -> R.string.search_catalog_indexing
        CatalogCompleteness.TRUNCATED -> R.string.search_catalog_truncated
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = SurfaceDefaults.colors(containerColor = CinematicPanelRaised),
        border = Border(
            border = BorderStroke(1.dp, CinematicGold.copy(alpha = .36f)),
            shape = RoundedCornerShape(18.dp)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.search_results_title, uiState.totalResults),
                    style = MaterialTheme.typography.titleMedium,
                    color = CinematicText,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = stringResource(catalogMessageRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = CinematicMuted
                )
            }
            if (uiState.catalogCompleteness != CatalogCompleteness.COMPLETE) {
                CinematicTextAction(
                    label = stringResource(R.string.search_build_complete_index),
                    onClick = onBuildCompleteIndex
                )
            }
        }
    }
}

@Composable
private fun CinematicSearchState(title: String, subtitle: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = SurfaceDefaults.colors(containerColor = CinematicPanel)
    ) {
        Column(
            modifier = Modifier.padding(30.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = CinematicText, fontWeight = FontWeight.Black)
            Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = CinematicMuted)
        }
    }
}

@Composable
private fun CinematicSearchChannelShelf(
    channels: List<Channel>,
    recordingChannelIds: Set<Long>,
    scheduledChannelIds: Set<Long>,
    onClick: (Channel) -> Unit,
    onLongClick: (Channel) -> Unit,
    isLocked: (Channel) -> Boolean
) {
    CinematicSearchShelfHeader(
        title = stringResource(R.string.search_live_tv).uppercase(),
        subtitle = "Live channels matched in the archive"
    )
    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(top = 12.dp)) {
        items(channels, key = { it.id }) { channel ->
            CinematicSearchChannelCard(
                channel = channel,
                isRecording = channel.id in recordingChannelIds,
                isScheduled = channel.id in scheduledChannelIds,
                locked = isLocked(channel),
                onClick = { onClick(channel) },
                onLongClick = { onLongClick(channel) }
            )
        }
    }
}

@Composable
private fun CinematicSearchMovieShelf(
    movies: List<Movie>,
    onClick: (Movie) -> Unit,
    onLongClick: (Movie) -> Unit,
    isLocked: (Movie) -> Boolean
) {
    CinematicSearchShelfHeader(
        title = stringResource(R.string.search_movies).uppercase(),
        subtitle = "Feature films matched in the archive"
    )
    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(top = 12.dp)) {
        items(movies, key = { it.id }) { movie ->
            CinematicSearchPosterCard(
                title = movie.name,
                detail = listOfNotNull(movie.year, movie.genre).joinToString(" · ").ifBlank { "FILM" },
                posterUrl = movie.posterUrl ?: movie.backdropUrl,
                rating = movie.rating,
                locked = isLocked(movie),
                onClick = { onClick(movie) },
                onLongClick = { onLongClick(movie) }
            )
        }
    }
}

@Composable
private fun CinematicSearchSeriesShelf(
    series: List<Series>,
    onClick: (Series) -> Unit,
    onLongClick: (Series) -> Unit,
    isLocked: (Series) -> Boolean
) {
    CinematicSearchShelfHeader(
        title = stringResource(R.string.search_series).uppercase(),
        subtitle = "Series collections matched in the archive"
    )
    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(top = 12.dp)) {
        items(series, key = { it.id }) { seriesItem ->
            CinematicSearchPosterCard(
                title = seriesItem.name,
                detail = seriesItem.genre?.takeIf { it.isNotBlank() } ?: "SERIES",
                posterUrl = seriesItem.posterUrl ?: seriesItem.backdropUrl,
                rating = seriesItem.rating,
                locked = isLocked(seriesItem),
                onClick = { onClick(seriesItem) },
                onLongClick = { onLongClick(seriesItem) }
            )
        }
    }
}

@Composable
private fun CinematicSearchShelfHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = CinematicText, fontWeight = FontWeight.Black)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = CinematicMuted)
    }
}

@Composable
private fun CinematicSearchChannelCard(
    channel: Channel,
    isRecording: Boolean,
    isScheduled: Boolean,
    locked: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    TvClickableSurface(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = Modifier.width(280.dp),
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = CinematicPanel,
            focusedContainerColor = CinematicPanelRaised,
            contentColor = CinematicText,
            focusedContentColor = CinematicText
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(border = BorderStroke(2.dp, CinematicGold), shape = shape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.03f)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CinematicCanvas),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = channel.logoUrl,
                        contentDescription = channel.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "${channel.number.takeIf { it > 0 }?.toString().orEmpty()}  ${channel.name}".trim(),
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = when {
                            locked -> "LOCKED CHANNEL"
                            isRecording -> "RECORDING NOW"
                            isScheduled -> "RECORDING SCHEDULED"
                            else -> channel.currentProgram?.title ?: "LIVE TRANSMISSION"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (locked || isRecording) CinematicWine else CinematicMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Text("HOLD FOR ACTIONS", style = MaterialTheme.typography.labelSmall, color = CinematicGold)
        }
    }
}

@Composable
private fun CinematicSearchPosterCard(
    title: String,
    detail: String,
    posterUrl: String?,
    rating: Float,
    locked: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    TvClickableSurface(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = Modifier.width(184.dp),
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = CinematicPanel,
            focusedContainerColor = CinematicPanelRaised,
            contentColor = CinematicText,
            focusedContentColor = CinematicText
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(border = BorderStroke(2.dp, CinematicGold), shape = shape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.04f)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(244.dp).background(CinematicCanvas)) {
                AsyncImage(
                    model = posterUrl,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)),
                    contentScale = ContentScale.Crop
                )
                if (locked) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(CinematicCanvas.copy(alpha = .70f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("LOCKED", style = MaterialTheme.typography.labelLarge, color = CinematicGold, fontWeight = FontWeight.Black)
                    }
                }
            }
            Column(modifier = Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = if (rating > 0f) "$detail · ${"%.1f".format(rating)}" else detail,
                    style = MaterialTheme.typography.labelSmall,
                    color = CinematicMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text("HOLD FOR ACTIONS", style = MaterialTheme.typography.labelSmall, color = CinematicGold)
            }
        }
    }
}

@Composable
private fun CinematicTextAction(label: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(999.dp)
    TvClickableSurface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = CinematicWine,
            focusedContainerColor = CinematicGold,
            contentColor = CinematicText,
            focusedContentColor = CinematicCanvas
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(border = BorderStroke(2.dp, CinematicText), shape = shape)
        )
    ) {
        Text(
            text = label.uppercase(),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}
