package com.streamvault.app.ui.themes.streaming

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
import com.streamvault.domain.model.CatalogCompleteness
import com.streamvault.domain.model.Channel
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.Series

/** بحث Streaming Platform: مكتب بحث بأعلى ثابت ورفوف نتائج، مع العقود الأصلية كاملة. */
@Composable
internal fun StreamingPlatformSearchLayout(
    query: String, selectedTab: SearchTab, recentQueries: List<String>, uiState: SearchUiState,
    recordingChannelIds: Set<Long>, scheduledChannelIds: Set<Long>, searchFocusRequester: FocusRequester,
    onQueryChange: (String) -> Unit, onSearch: () -> Unit, onTabSelected: (SearchTab) -> Unit,
    onRecentQuerySelected: (String) -> Unit, onClearRecentQueries: () -> Unit, onBuildCompleteIndex: () -> Unit,
    onChannelClick: (Channel) -> Unit, onChannelLongClick: (Channel) -> Unit,
    onMovieClick: (Movie) -> Unit, onMovieLongClick: (Movie) -> Unit,
    onSeriesClick: (Series) -> Unit, onSeriesLongClick: (Series) -> Unit,
    isChannelLocked: (Channel) -> Boolean, isMovieLocked: (Movie) -> Boolean, isSeriesLocked: (Series) -> Boolean
) {
    LazyColumn(modifier = Modifier.fillMaxSize().background(StreamingCanvas).padding(horizontal = 36.dp, vertical = 26.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item("streaming_search_head") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("SEARCH", style = MaterialTheme.typography.displaySmall, color = StreamingText)
                Text("DISCOVER LIVE, FILMS AND SERIES", style = MaterialTheme.typography.labelLarge, color = StreamingMuted)
                SearchInput(value = query, onValueChange = onQueryChange, onSearch = onSearch, placeholder = stringResource(R.string.search_hint), focusRequester = searchFocusRequester, modifier = Modifier.fillMaxWidth())
                LazyRow(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    items(SearchTab.entries.toList(), key = { it.name }) { tab -> StreamingSearchChip(stringResource(tab.titleRes), tab == selectedTab) { onTabSelected(tab) } }
                }
            }
        }
        if (recentQueries.isNotEmpty() && query.isBlank()) item("streaming_search_history") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("RECENT SEARCHES", Modifier.weight(1f), style = MaterialTheme.typography.titleSmall, color = StreamingText)
                    StreamingSearchChip(stringResource(R.string.search_clear_history), false, onClearRecentQueries)
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(9.dp)) { items(recentQueries, key = { it }) { recent -> StreamingSearchChip(recent, false) { onRecentQuerySelected(recent) } } }
            }
        }
        when {
            !uiState.hasActiveProvider -> item("streaming_search_provider") { StreamingSearchState("NO ACTIVE PROVIDER", stringResource(R.string.search_no_provider_subtitle)) }
            uiState.queryLength < 2 -> item("streaming_search_ready") { StreamingSearchState("SEARCH READY", stringResource(R.string.search_type_to_search)) }
            uiState.isLoading -> item("streaming_search_loading") { StreamingSearchState("SEARCHING", stringResource(R.string.search_loading_subtitle)) }
            uiState.isEmpty && uiState.hasSearchError -> item("streaming_search_error") { StreamingSearchState("SEARCH ERROR", stringResource(R.string.search_error_subtitle)) }
            uiState.isEmpty -> item("streaming_search_empty") { StreamingSearchState("NO RESULTS", stringResource(R.string.search_no_results, query)) }
            else -> {
                item("streaming_search_summary") {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.search_results_title, uiState.totalResults), style = MaterialTheme.typography.titleMedium, color = StreamingText)
                            Text("CATALOGUE / ${uiState.catalogCompleteness.name}", style = MaterialTheme.typography.bodySmall, color = StreamingMuted)
                        }
                        if (uiState.catalogCompleteness != CatalogCompleteness.COMPLETE) StreamingSearchChip(stringResource(R.string.search_build_complete_index), false, onBuildCompleteIndex)
                    }
                }
                if ((selectedTab == SearchTab.ALL || selectedTab == SearchTab.LIVE) && uiState.channels.isNotEmpty()) item("streaming_search_live") {
                    StreamingSearchSection("LIVE CHANNELS") {
                        uiState.channels.forEach { channel ->
                            val meta = buildList { if (channel.id in recordingChannelIds) add("RECORDING"); if (channel.id in scheduledChannelIds) add("SCHEDULED"); if (isChannelLocked(channel)) add("LOCKED") }.joinToString(" · ")
                            StreamingSearchRow("${channel.number?.toString().orEmpty()}  ${channel.name}", meta, { onChannelClick(channel) }, { onChannelLongClick(channel) })
                        }
                    }
                }
                if ((selectedTab == SearchTab.ALL || selectedTab == SearchTab.MOVIES) && uiState.movies.isNotEmpty()) item("streaming_search_movies") {
                    StreamingSearchSection("FILMS") { uiState.movies.forEach { movie -> StreamingSearchRow(movie.name, listOfNotNull(movie.year?.toString(), movie.genre, if (isMovieLocked(movie)) "LOCKED" else null).joinToString(" · "), { onMovieClick(movie) }, { onMovieLongClick(movie) }) } }
                }
                if ((selectedTab == SearchTab.ALL || selectedTab == SearchTab.SERIES) && uiState.series.isNotEmpty()) item("streaming_search_series") {
                    StreamingSearchSection("SERIES") { uiState.series.forEach { series -> StreamingSearchRow(series.name, listOfNotNull(series.genre, if (isSeriesLocked(series)) "LOCKED" else null).joinToString(" · "), { onSeriesClick(series) }, { onSeriesLongClick(series) }) } }
                }
            }
        }
    }
}

@Composable private fun StreamingSearchSection(title: String, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) = Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(title, style = MaterialTheme.typography.titleLarge, color = StreamingText); content() }
@Composable private fun StreamingSearchState(title: String, subtitle: String) = Column(Modifier.fillMaxWidth().background(StreamingPanel, RoundedCornerShape(16.dp)).padding(20.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) { Text(title, style = MaterialTheme.typography.titleLarge, color = StreamingText); Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = StreamingMuted) }
@Composable private fun StreamingSearchChip(label: String, selected: Boolean, onClick: () -> Unit) { val shape = RoundedCornerShape(10.dp); TvClickableSurface(onClick = onClick, shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = if (selected) StreamingPanelFocused else StreamingPanel, focusedContainerColor = StreamingPanelFocused, contentColor = StreamingText, focusedContentColor = StreamingText), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, StreamingFocus), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.025f)) { Text(label.uppercase(), Modifier.padding(horizontal = 14.dp, vertical = 10.dp), style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis) } }
@Composable private fun StreamingSearchRow(title: String, subtitle: String, onClick: () -> Unit, onLongClick: () -> Unit) { val shape = RoundedCornerShape(12.dp); TvClickableSurface(onClick = onClick, onLongClick = onLongClick, modifier = Modifier.fillMaxWidth(), shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = StreamingPanel, focusedContainerColor = StreamingPanelFocused, contentColor = StreamingText, focusedContentColor = StreamingText), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, StreamingFocus), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f)) { Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis); if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.labelSmall, color = StreamingMuted, maxLines = 1, overflow = TextOverflow.Ellipsis) }; Text("HOLD", style = MaterialTheme.typography.labelSmall, color = StreamingAccent) } } }
