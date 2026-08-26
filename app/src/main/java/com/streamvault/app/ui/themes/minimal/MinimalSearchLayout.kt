package com.streamvault.app.ui.themes.minimal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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

/**
 * فهرس بحث Minimal مستقل: أوامر نصية وتبويبات ثابتة وسجل قابل للمسح؛ بينما تبقى
 * عملية الفهرسة، القيود العائلية وإجراءات الضغط المطوّل ملكاً لـSearchScreen.
 */
@Composable
internal fun MinimalSearchLayout(
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
        modifier = Modifier.fillMaxSize().background(MinimalCanvas),
        contentPadding = PaddingValues(34.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item("minimal_search_heading") {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("SEARCH INDEX", style = MaterialTheme.typography.headlineMedium, color = MinimalText)
                Text("FILTER · HISTORY · ACTIONS", style = MaterialTheme.typography.labelSmall, color = MinimalMuted)
            }
        }
        item("minimal_search_desk") {
            Column(
                modifier = Modifier.fillMaxWidth().background(MinimalPaper).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SearchInput(
                    value = query,
                    onValueChange = onQueryChange,
                    onSearch = onSearch,
                    placeholder = stringResource(R.string.search_hint),
                    focusRequester = searchFocusRequester
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(SearchTab.entries.toList(), key = { it.name }) { tab ->
                        MinimalSearchCommand(
                            label = stringResource(tab.titleRes),
                            selected = tab == selectedTab,
                            onClick = { onTabSelected(tab) }
                        )
                    }
                }
            }
        }
        if (recentQueries.isNotEmpty() && query.isBlank()) {
            item("minimal_search_history") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.search_recent_title).uppercase(),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelLarge,
                            color = MinimalMuted
                        )
                        MinimalSearchCommand(
                            label = stringResource(R.string.search_clear_history),
                            selected = false,
                            onClick = onClearRecentQueries
                        )
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(recentQueries, key = { it }) { recent ->
                            MinimalSearchCommand(label = recent, selected = false) {
                                onRecentQuerySelected(recent)
                            }
                        }
                    }
                }
            }
        }
        when {
            !uiState.hasActiveProvider -> item("minimal_search_no_provider") {
                MinimalSearchState("NO ACTIVE PROVIDER", stringResource(R.string.search_no_provider_subtitle))
            }
            uiState.queryLength < 2 -> item("minimal_search_ready") {
                MinimalSearchState("SEARCH READY", stringResource(R.string.search_type_to_search))
            }
            uiState.isLoading -> item("minimal_search_loading") {
                MinimalSearchState("SEARCHING", stringResource(R.string.search_loading_subtitle))
            }
            uiState.isEmpty && uiState.hasSearchError -> item("minimal_search_error") {
                MinimalSearchState("SEARCH ERROR", stringResource(R.string.search_error_subtitle))
            }
            uiState.isEmpty -> item("minimal_search_empty") {
                MinimalSearchState("NO RESULTS", stringResource(R.string.search_no_results, query))
            }
            else -> {
                item("minimal_search_summary") {
                    MinimalSearchSummary(uiState = uiState, onBuildCompleteIndex = onBuildCompleteIndex)
                }
                if ((selectedTab == SearchTab.ALL || selectedTab == SearchTab.LIVE) && uiState.channels.isNotEmpty()) {
                    item("minimal_search_channels_heading") { MinimalSearchSection("CHANNELS") }
                    items(uiState.channels, key = { it.id }) { channel ->
                        val activity = buildList {
                            if (channel.id in recordingChannelIds) add("RECORDING")
                            if (channel.id in scheduledChannelIds) add("SCHEDULED")
                            if (isChannelLocked(channel)) add("LOCKED")
                        }.joinToString(" · ")
                        MinimalSearchResult(
                            label = "${channel.number}  ${channel.name}",
                            status = activity,
                            onClick = { onChannelClick(channel) },
                            onLongClick = { onChannelLongClick(channel) }
                        )
                    }
                }
                if ((selectedTab == SearchTab.ALL || selectedTab == SearchTab.MOVIES) && uiState.movies.isNotEmpty()) {
                    item("minimal_search_movies_heading") { MinimalSearchSection("FILMS") }
                    items(uiState.movies, key = { it.id }) { movie ->
                        MinimalSearchResult(
                            label = movie.name,
                            status = listOfNotNull(movie.year, movie.genre, if (isMovieLocked(movie)) "LOCKED" else null).joinToString(" · "),
                            onClick = { onMovieClick(movie) },
                            onLongClick = { onMovieLongClick(movie) }
                        )
                    }
                }
                if ((selectedTab == SearchTab.ALL || selectedTab == SearchTab.SERIES) && uiState.series.isNotEmpty()) {
                    item("minimal_search_series_heading") { MinimalSearchSection("SERIES") }
                    items(uiState.series, key = { it.id }) { series ->
                        MinimalSearchResult(
                            label = series.name,
                            status = listOfNotNull(series.genre, if (isSeriesLocked(series)) "LOCKED" else null).joinToString(" · "),
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
private fun MinimalSearchCommand(label: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(0.dp)
    TvClickableSurface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) MinimalCanvas else MinimalPaper,
            focusedContainerColor = MinimalCanvas,
            contentColor = if (selected) MinimalText else MinimalMuted,
            focusedContentColor = MinimalText
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(border = BorderStroke(1.dp, MinimalFocus), shape = shape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = MinimalFocusedScale)
    ) {
        Text(
            text = "[ ${label.uppercase()} ]",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MinimalSearchSummary(uiState: SearchUiState, onBuildCompleteIndex: () -> Unit) {
    val status = when (uiState.catalogCompleteness) {
        CatalogCompleteness.COMPLETE -> stringResource(R.string.search_catalog_complete)
        CatalogCompleteness.PARTIAL -> stringResource(R.string.search_catalog_downloaded_only)
        CatalogCompleteness.INDEXING -> stringResource(R.string.search_catalog_indexing)
        CatalogCompleteness.TRUNCATED -> stringResource(R.string.search_catalog_truncated)
    }
    Row(
        modifier = Modifier.fillMaxWidth().background(MinimalPaper).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(R.string.search_results_title, uiState.totalResults), style = MaterialTheme.typography.titleSmall, color = MinimalText)
            Text(status, style = MaterialTheme.typography.bodySmall, color = MinimalMuted)
        }
        if (uiState.catalogCompleteness != CatalogCompleteness.COMPLETE) {
            MinimalSearchCommand(stringResource(R.string.search_build_complete_index), false, onBuildCompleteIndex)
        }
    }
}

@Composable
private fun MinimalSearchState(title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxWidth().background(MinimalPaper).padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = MinimalText)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MinimalMuted)
    }
}

@Composable
private fun MinimalSearchSection(label: String) {
    Text(label, style = MaterialTheme.typography.labelLarge, color = MinimalMuted)
}

@Composable
private fun MinimalSearchResult(
    label: String,
    status: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val shape = RoundedCornerShape(0.dp)
    TvClickableSurface(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = Modifier.fillMaxWidth(),
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = MinimalPaper,
            focusedContainerColor = MinimalCanvas,
            contentColor = MinimalText,
            focusedContentColor = MinimalText
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(border = BorderStroke(1.dp, MinimalFocus), shape = shape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = MinimalFocusedScale)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("/", style = MaterialTheme.typography.labelLarge, color = MinimalMuted)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(label, style = MaterialTheme.typography.titleSmall, color = MinimalText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (status.isNotBlank()) Text(status, style = MaterialTheme.typography.labelSmall, color = MinimalMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text("HOLD", style = MaterialTheme.typography.labelSmall, color = MinimalMuted)
        }
    }
}
