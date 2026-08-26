package com.streamvault.app.ui.themes.blueocean

/** Style contract: Blue Ocean search is a flowing research station with vertical result currents. */

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.app.R
import com.streamvault.app.ui.components.SearchInput
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.app.ui.screens.search.SearchTab
import com.streamvault.app.ui.screens.search.SearchUiState
import com.streamvault.app.ui.theme.LocalThemePresentation
import com.streamvault.domain.model.CatalogCompleteness
import com.streamvault.domain.model.Channel
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.Series

@Composable
internal fun BlueOceanSearchLayout(
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
    val surfaces = LocalThemePresentation.current.surfaces
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(surfaces.canvas).padding(30.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "blue_ocean_search_station") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("DISCOVERY STATION", style = MaterialTheme.typography.displaySmall)
                Text("SEARCH ACROSS THE TIDE", style = MaterialTheme.typography.labelMedium, color = surfaces.accent)
                SearchInput(
                    value = query,
                    onValueChange = onQueryChange,
                    onSearch = onSearch,
                    placeholder = stringResource(R.string.search_hint),
                    focusRequester = searchFocusRequester,
                    modifier = Modifier.fillMaxWidth()
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(SearchTab.entries.toList(), key = { it.name }) { tab ->
                        BlueOceanSearchChip(
                            label = stringResource(tab.titleRes),
                            selected = tab == selectedTab,
                            onClick = { onTabSelected(tab) }
                        )
                    }
                }
            }
        }

        if (recentQueries.isNotEmpty() && query.isBlank()) {
            item(key = "blue_ocean_search_history") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("RECENT WAKE", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                    recentQueries.take(4).forEach { recent ->
                        BlueOceanSearchChip(recent, false) { onRecentQuerySelected(recent) }
                    }
                    BlueOceanSearchChip("CLEAR", false, onClearRecentQueries)
                }
            }
        }

        when {
            !uiState.hasActiveProvider -> item(key = "blue_ocean_search_provider") {
                BlueOceanSearchState("NO ACTIVE PROVIDER", stringResource(R.string.search_no_provider_subtitle))
            }

            uiState.queryLength < 2 -> item(key = "blue_ocean_search_ready") {
                BlueOceanSearchState("READY TO EXPLORE", stringResource(R.string.search_type_to_search))
            }

            uiState.isLoading -> item(key = "blue_ocean_search_loading") {
                BlueOceanSearchState("SCANNING CURRENTS", stringResource(R.string.search_loading_subtitle))
            }

            uiState.isEmpty && uiState.hasSearchError -> item(key = "blue_ocean_search_error") {
                BlueOceanSearchState("SEARCH CURRENT INTERRUPTED", stringResource(R.string.search_error_subtitle))
            }

            uiState.isEmpty -> item(key = "blue_ocean_search_empty") {
                BlueOceanSearchState("NO CURRENT FOUND", stringResource(R.string.search_no_results, query))
            }

            else -> {
                item(key = "blue_ocean_search_summary") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("${uiState.totalResults} RESULTS", style = MaterialTheme.typography.titleLarge)
                            Text("INDEX · ${uiState.catalogCompleteness.name}", style = MaterialTheme.typography.labelMedium, color = surfaces.textSecondary)
                        }
                        if (uiState.catalogCompleteness != CatalogCompleteness.COMPLETE) {
                            BlueOceanSearchChip(stringResource(R.string.search_build_complete_index), false, onBuildCompleteIndex)
                        }
                    }
                }

                if ((selectedTab == SearchTab.ALL || selectedTab == SearchTab.LIVE) && uiState.channels.isNotEmpty()) {
                    item(key = "blue_ocean_search_live") {
                        BlueOceanResultCurrent("LIVE ROUTES") {
                            uiState.channels.forEach { channel ->
                                val details = buildList {
                                    if (channel.id in recordingChannelIds) add("RECORDING")
                                    if (channel.id in scheduledChannelIds) add("SCHEDULED")
                                    if (isChannelLocked(channel)) add("LOCKED")
                                }.joinToString(" · ")
                                BlueOceanResultRow(
                                    title = "${channel.number?.toString().orEmpty()}  ${channel.name}",
                                    subtitle = details,
                                    onClick = { onChannelClick(channel) },
                                    onLongClick = { onChannelLongClick(channel) }
                                )
                            }
                        }
                    }
                }

                if ((selectedTab == SearchTab.ALL || selectedTab == SearchTab.MOVIES) && uiState.movies.isNotEmpty()) {
                    item(key = "blue_ocean_search_movies") {
                        BlueOceanResultCurrent("FILM CURRENTS") {
                            uiState.movies.forEach { movie ->
                                BlueOceanResultRow(
                                    title = movie.name,
                                    subtitle = listOfNotNull(movie.year?.toString(), movie.genre, if (isMovieLocked(movie)) "LOCKED" else null).joinToString(" · "),
                                    onClick = { onMovieClick(movie) },
                                    onLongClick = { onMovieLongClick(movie) }
                                )
                            }
                        }
                    }
                }

                if ((selectedTab == SearchTab.ALL || selectedTab == SearchTab.SERIES) && uiState.series.isNotEmpty()) {
                    item(key = "blue_ocean_search_series") {
                        BlueOceanResultCurrent("SERIES TRIBUTARIES") {
                            uiState.series.forEach { series ->
                                BlueOceanResultRow(
                                    title = series.name,
                                    subtitle = listOfNotNull(series.genre, if (isSeriesLocked(series)) "LOCKED" else null).joinToString(" · "),
                                    onClick = { onSeriesClick(series) },
                                    onLongClick = { onSeriesLongClick(series) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BlueOceanResultCurrent(title: String, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    val surfaces = LocalThemePresentation.current.surfaces
    Column(
        modifier = Modifier.fillMaxWidth().background(surfaces.browseContent, RoundedCornerShape(26.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = surfaces.accent)
        content()
    }
}

@Composable
private fun BlueOceanSearchState(title: String, subtitle: String) {
    val surfaces = LocalThemePresentation.current.surfaces
    Column(
        modifier = Modifier.fillMaxWidth().background(surfaces.browseContent, RoundedCornerShape(26.dp)).padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = surfaces.textPrimary)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = surfaces.textSecondary)
    }
}

@Composable
private fun BlueOceanSearchChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val surfaces = LocalThemePresentation.current.surfaces
    val shape = RoundedCornerShape(18.dp)
    TvClickableSurface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) surfaces.selectedAccent else surfaces.browseContent,
            focusedContainerColor = surfaces.focusedSurface,
            contentColor = surfaces.textPrimary,
            focusedContentColor = surfaces.textPrimary
        ),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, surfaces.accent), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f)
    ) {
        Text(label, Modifier.padding(horizontal = 13.dp, vertical = 10.dp), style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun BlueOceanResultRow(title: String, subtitle: String, onClick: () -> Unit, onLongClick: () -> Unit) {
    val surfaces = LocalThemePresentation.current.surfaces
    val shape = RoundedCornerShape(16.dp)
    TvClickableSurface(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = Modifier.fillMaxWidth(),
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = surfaces.canvas,
            focusedContainerColor = surfaces.focusedSurface,
            contentColor = surfaces.textPrimary,
            focusedContentColor = surfaces.textPrimary
        ),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, surfaces.accent), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f)
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.labelSmall, color = surfaces.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text("→", style = MaterialTheme.typography.titleMedium, color = surfaces.accent)
        }
    }
}
