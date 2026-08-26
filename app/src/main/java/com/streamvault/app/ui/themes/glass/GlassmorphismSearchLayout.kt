package com.streamvault.app.ui.themes.glass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import com.streamvault.domain.model.CatalogCompleteness
import com.streamvault.domain.model.Channel
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.Series

/** لوحة بحث Glass مستقلة بصرياً، وتعتمد على الحالة والتفاعلات المشتركة فقط. */
@Composable
internal fun GlassmorphismSearchLayout(
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
        modifier = Modifier.fillMaxSize().background(GlassCanvas).padding(horizontal = 42.dp, vertical = 30.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item("glass_search_heading") {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("DISCOVERY GLASS", style = MaterialTheme.typography.headlineMedium, color = GlassText)
                Text("Search across the active catalogue without leaving the glass layer.", style = MaterialTheme.typography.bodyMedium, color = GlassMuted)
            }
        }
        item("glass_search_desk") {
            GlassSearchPane {
                SearchInput(
                    value = query,
                    onValueChange = onQueryChange,
                    onSearch = onSearch,
                    placeholder = stringResource(R.string.search_hint),
                    focusRequester = searchFocusRequester
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(SearchTab.entries.toList(), key = { it.name }) { tab ->
                        GlassSearchChip(
                            label = stringResource(tab.titleRes),
                            selected = tab == selectedTab,
                            onClick = { onTabSelected(tab) }
                        )
                    }
                }
            }
        }
        if (recentQueries.isNotEmpty() && query.isBlank()) {
            item("glass_search_history") {
                GlassSearchPane {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("RECENT QUERIES", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge, color = GlassAccent)
                        GlassSearchChip(label = stringResource(R.string.search_clear_history), selected = false, onClick = onClearRecentQueries)
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(recentQueries, key = { it }) { recent ->
                            GlassSearchChip(label = recent, selected = false, onClick = { onRecentQuerySelected(recent) })
                        }
                    }
                }
            }
        }
        when {
            !uiState.hasActiveProvider -> item("glass_search_no_provider") {
                GlassSearchState("NO ACTIVE PROVIDER", stringResource(R.string.search_no_provider_subtitle))
            }
            uiState.queryLength < 2 -> item("glass_search_ready") {
                GlassSearchState("SEARCH READY", stringResource(R.string.search_type_to_search))
            }
            uiState.isLoading -> item("glass_search_loading") {
                GlassSearchState("SEARCHING GLASS INDEX", stringResource(R.string.search_loading_subtitle))
            }
            uiState.isEmpty && uiState.hasSearchError -> item("glass_search_error") {
                GlassSearchState("SEARCH ERROR", stringResource(R.string.search_error_subtitle))
            }
            uiState.isEmpty -> item("glass_search_empty") {
                GlassSearchState("NO RESULTS", stringResource(R.string.search_no_results, query))
            }
            else -> {
                item("glass_search_summary") { GlassSearchSummary(uiState, onBuildCompleteIndex) }
                if ((selectedTab == SearchTab.ALL || selectedTab == SearchTab.LIVE) && uiState.channels.isNotEmpty()) {
                    item("glass_search_channels_label") { GlassSearchSection("LIVE CHANNELS") }
                    items(uiState.channels, key = { "glass_channel_${it.id}" }) { channel ->
                        val status = buildList {
                            if (channel.id in recordingChannelIds) add("RECORDING")
                            if (channel.id in scheduledChannelIds) add("SCHEDULED")
                            if (isChannelLocked(channel)) add("LOCKED")
                        }.joinToString(" · ")
                        GlassSearchResult(
                            title = listOfNotNull(channel.number?.toString(), channel.name).joinToString("  "),
                            subtitle = status,
                            onClick = { onChannelClick(channel) },
                            onLongClick = { onChannelLongClick(channel) }
                        )
                    }
                }
                if ((selectedTab == SearchTab.ALL || selectedTab == SearchTab.MOVIES) && uiState.movies.isNotEmpty()) {
                    item("glass_search_movies_label") { GlassSearchSection("FILMS") }
                    items(uiState.movies, key = { "glass_movie_${it.id}" }) { movie ->
                        GlassSearchResult(
                            title = movie.name,
                            subtitle = listOfNotNull(movie.year?.toString(), movie.genre, if (isMovieLocked(movie)) "LOCKED" else null).joinToString(" · "),
                            onClick = { onMovieClick(movie) },
                            onLongClick = { onMovieLongClick(movie) }
                        )
                    }
                }
                if ((selectedTab == SearchTab.ALL || selectedTab == SearchTab.SERIES) && uiState.series.isNotEmpty()) {
                    item("glass_search_series_label") { GlassSearchSection("SERIES") }
                    items(uiState.series, key = { "glass_series_${it.id}" }) { series ->
                        GlassSearchResult(
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

@Composable
private fun GlassSearchSummary(uiState: SearchUiState, onBuildCompleteIndex: () -> Unit) {
    val status = when (uiState.catalogCompleteness) {
        CatalogCompleteness.COMPLETE -> stringResource(R.string.search_catalog_complete)
        CatalogCompleteness.PARTIAL -> stringResource(R.string.search_catalog_downloaded_only)
        CatalogCompleteness.INDEXING -> stringResource(R.string.search_catalog_indexing)
        CatalogCompleteness.TRUNCATED -> stringResource(R.string.search_catalog_truncated)
    }
    GlassSearchPane {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(stringResource(R.string.search_results_title, uiState.totalResults), style = MaterialTheme.typography.titleSmall, color = GlassText)
                Text(status, style = MaterialTheme.typography.bodySmall, color = GlassMuted)
            }
            if (uiState.catalogCompleteness != CatalogCompleteness.COMPLETE) {
                GlassSearchChip(stringResource(R.string.search_build_complete_index), false, onBuildCompleteIndex)
            }
        }
    }
}

@Composable
private fun GlassSearchState(title: String, subtitle: String) {
    GlassSearchPane {
        Text(title, style = MaterialTheme.typography.titleLarge, color = GlassText)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = GlassMuted)
    }
}

@Composable
private fun GlassSearchSection(title: String) {
    Text(title, style = MaterialTheme.typography.labelLarge, color = GlassAccent, modifier = Modifier.padding(top = 8.dp))
}

@Composable
private fun GlassSearchChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(15.dp)
    TvClickableSurface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) GlassPaneFocused else GlassPane,
            focusedContainerColor = GlassPaneFocused,
            contentColor = if (selected) GlassFocus else GlassText,
            focusedContentColor = GlassText
        ),
        border = ClickableSurfaceDefaults.border(
            border = Border(border = BorderStroke(1.dp, GlassRule), shape = shape),
            focusedBorder = Border(border = BorderStroke(2.dp, GlassFocus), shape = shape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.035f)
    ) {
        Text(label.uppercase(), Modifier.padding(horizontal = 13.dp, vertical = 9.dp), style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun GlassSearchResult(title: String, subtitle: String, onClick: () -> Unit, onLongClick: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    TvClickableSurface(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = Modifier.fillMaxWidth(),
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = GlassPane, focusedContainerColor = GlassPaneFocused, contentColor = GlassText, focusedContentColor = GlassText),
        border = ClickableSurfaceDefaults.border(
            border = Border(border = BorderStroke(1.dp, GlassRule), shape = shape),
            focusedBorder = Border(border = BorderStroke(2.dp, GlassFocus), shape = shape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f)
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.labelSmall, color = GlassMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text("HOLD", style = MaterialTheme.typography.labelSmall, color = GlassAccent)
        }
    }
}

@Composable
private fun GlassSearchPane(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().background(GlassPane, RoundedCornerShape(22.dp)).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}
