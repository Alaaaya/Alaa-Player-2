package com.streamvault.app.ui.themes.neon

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
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
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
import com.streamvault.app.ui.screens.search.SearchTab
import com.streamvault.app.ui.screens.search.SearchUiState
import com.streamvault.domain.model.Channel
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.Series

/** Presentation-only Neon Future search console. Querying, locks and actions remain in SearchScreen. */
@Composable
internal fun NeonFutureSearchLayout(
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
        modifier = Modifier.fillMaxSize().background(NeonCanvas),
        contentPadding = PaddingValues(start = 34.dp, top = 28.dp, end = 34.dp, bottom = 34.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        item("neon_search_console") {
            NeonFutureSearchConsole(
                query = query,
                selectedTab = selectedTab,
                searchFocusRequester = searchFocusRequester,
                onQueryChange = onQueryChange,
                onSearch = onSearch,
                onTabSelected = onTabSelected
            )
        }
        if (recentQueries.isNotEmpty() && query.isBlank()) item("neon_search_history") {
            NeonFutureSearchHistory(recentQueries, onRecentQuerySelected, onClearRecentQueries)
        }
        when {
            !uiState.hasActiveProvider -> item("neon_search_no_provider") {
                NeonFutureSearchState(stringResource(R.string.search_no_provider_title), stringResource(R.string.search_no_provider_subtitle))
            }
            uiState.queryLength < 2 -> item("neon_search_ready") {
                NeonFutureSearchState(stringResource(R.string.search_ready_title), stringResource(R.string.search_type_to_search))
            }
            uiState.isLoading -> item("neon_search_loading") {
                NeonFutureSearchState(stringResource(R.string.search_loading_title), stringResource(R.string.search_loading_subtitle))
            }
            uiState.isEmpty && uiState.hasSearchError -> item("neon_search_error") {
                NeonFutureSearchState(stringResource(R.string.search_error_title), stringResource(R.string.search_error_subtitle))
            }
            uiState.isEmpty -> item("neon_search_empty") {
                NeonFutureSearchState(stringResource(R.string.search_no_results_title), stringResource(R.string.search_no_results, query))
            }
            else -> {
                item("neon_search_summary") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("RESULT GRID / ${uiState.channels.size + uiState.movies.size + uiState.series.size} NODES", style = MaterialTheme.typography.labelLarge, color = NeonCyan, fontWeight = FontWeight.Black)
                        NeonFutureSearchAction("REFRESH INDEX", NeonLime, onBuildCompleteIndex)
                    }
                }
                if ((selectedTab == SearchTab.ALL || selectedTab == SearchTab.LIVE) && uiState.channels.isNotEmpty()) item("neon_search_live") {
                    NeonFutureSearchChannelResults(uiState.channels, recordingChannelIds, scheduledChannelIds, onChannelClick, onChannelLongClick, isChannelLocked)
                }
                if ((selectedTab == SearchTab.ALL || selectedTab == SearchTab.MOVIES) && uiState.movies.isNotEmpty()) item("neon_search_movies") {
                    NeonFutureSearchMediaResults("FILM NODES", uiState.movies, { it.name }, { it.posterUrl }, { onMovieClick(it) }, { onMovieLongClick(it) }, isMovieLocked)
                }
                if ((selectedTab == SearchTab.ALL || selectedTab == SearchTab.SERIES) && uiState.series.isNotEmpty()) item("neon_search_series") {
                    NeonFutureSearchMediaResults("SERIES NODES", uiState.series, { it.name }, { it.posterUrl }, { onSeriesClick(it) }, { onSeriesLongClick(it) }, isSeriesLocked)
                }
            }
        }
    }
}

@Composable
private fun NeonFutureSearchConsole(query: String, selectedTab: SearchTab, searchFocusRequester: FocusRequester, onQueryChange: (String) -> Unit, onSearch: () -> Unit, onTabSelected: (SearchTab) -> Unit) {
    Column(modifier = Modifier.background(NeonPanel, RoundedCornerShape(18.dp)).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("QUERY TERMINAL", style = MaterialTheme.typography.displaySmall, color = NeonText, fontWeight = FontWeight.Black)
        Text("Search active provider data across live channels, films and series.", style = MaterialTheme.typography.bodyLarge, color = NeonMuted)
        SearchInput(value = query, onValueChange = onQueryChange, onSearch = onSearch, placeholder = stringResource(R.string.search_hint), focusRequester = searchFocusRequester)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            items(SearchTab.entries.toList(), key = { it.name }) { tab ->
                NeonFutureSearchAction(tab.name, if (tab == selectedTab) NeonPink else NeonCyan) { onTabSelected(tab) }
            }
        }
    }
}

@Composable
private fun NeonFutureSearchHistory(recentQueries: List<String>, onRecentQuerySelected: (String) -> Unit, onClearRecentQueries: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("RECENT QUERIES", style = MaterialTheme.typography.titleLarge, color = NeonText, fontWeight = FontWeight.Black)
            NeonFutureSearchAction("CLEAR", NeonPink, onClearRecentQueries)
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(recentQueries, key = { it }) { query -> NeonFutureSearchAction(query.uppercase(), NeonCyan) { onRecentQuerySelected(query) } }
        }
    }
}

@Composable
private fun NeonFutureSearchChannelResults(channels: List<Channel>, recordingChannelIds: Set<Long>, scheduledChannelIds: Set<Long>, onClick: (Channel) -> Unit, onLongClick: (Channel) -> Unit, isLocked: (Channel) -> Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("LIVE SIGNAL NODES", style = MaterialTheme.typography.titleLarge, color = NeonText, fontWeight = FontWeight.Black)
        LazyColumn(modifier = Modifier.height(290.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(channels, key = { it.id }) { channel ->
                val shape = RoundedCornerShape(10.dp)
                TvClickableSurface(
                    onClick = { onClick(channel) }, onLongClick = { onLongClick(channel) }, shape = ClickableSurfaceDefaults.shape(shape),
                    colors = ClickableSurfaceDefaults.colors(containerColor = NeonPanel, focusedContainerColor = NeonPanelRaised, contentColor = NeonText, focusedContentColor = NeonText),
                    border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, NeonCyan), shape = shape))
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(channel.number.toString().padStart(3, '0'), style = MaterialTheme.typography.labelMedium, color = NeonLime)
                        Box(modifier = Modifier.size(42.dp).background(NeonCanvas, RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) { AsyncImage(channel.logoUrl, null, Modifier.fillMaxSize(), contentScale = ContentScale.Fit) }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(channel.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(when { isLocked(channel) -> "ACCESS GATED"; channel.id in recordingChannelIds -> "RECORDING"; channel.id in scheduledChannelIds -> "SCHEDULED"; else -> channel.currentProgram?.title ?: "LIVE" }, style = MaterialTheme.typography.labelSmall, color = NeonMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> NeonFutureSearchMediaResults(label: String, items: List<T>, title: (T) -> String, poster: (T) -> String?, onClick: (T) -> Unit, onLongClick: (T) -> Unit, isLocked: (T) -> Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(label, style = MaterialTheme.typography.titleLarge, color = NeonText, fontWeight = FontWeight.Black)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items) { item ->
                val shape = RoundedCornerShape(8.dp)
                TvClickableSurface(
                    onClick = { onClick(item) }, onLongClick = { onLongClick(item) }, modifier = Modifier.width(154.dp), shape = ClickableSurfaceDefaults.shape(shape),
                    colors = ClickableSurfaceDefaults.colors(containerColor = NeonPanel, focusedContainerColor = NeonPanelRaised, contentColor = NeonText, focusedContentColor = NeonText),
                    border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, NeonLime), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.04f)
                ) {
                    Column {
                        Box(modifier = Modifier.fillMaxWidth().height(204.dp)) {
                            AsyncImage(poster(item), null, Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)), contentScale = ContentScale.Crop)
                            if (isLocked(item)) Text("LOCK", modifier = Modifier.align(Alignment.TopEnd).padding(8.dp), style = MaterialTheme.typography.labelSmall, color = NeonPink)
                        }
                        Text(title(item), modifier = Modifier.padding(10.dp), style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
private fun NeonFutureSearchState(title: String, subtitle: String) {
    Column(modifier = Modifier.fillMaxWidth().background(NeonPanel, RoundedCornerShape(14.dp)).padding(28.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = NeonText, fontWeight = FontWeight.Black)
        Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = NeonMuted)
    }
}

@Composable
private fun NeonFutureSearchAction(label: String, tone: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    val shape = RoundedCornerShape(999.dp)
    TvClickableSurface(
        onClick = onClick, shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = NeonCanvas, focusedContainerColor = tone.copy(alpha = .24f), contentColor = tone, focusedContentColor = NeonText),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, tone), shape = shape))
    ) {
        Text(label, modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
