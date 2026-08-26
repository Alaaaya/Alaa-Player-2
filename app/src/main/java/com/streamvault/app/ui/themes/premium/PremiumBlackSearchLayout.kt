package com.streamvault.app.ui.themes.premium

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

@Composable
internal fun PremiumBlackSearchLayout(query: String, selectedTab: SearchTab, recentQueries: List<String>, uiState: SearchUiState, recordingChannelIds: Set<Long>, scheduledChannelIds: Set<Long>, searchFocusRequester: FocusRequester, onQueryChange: (String) -> Unit, onSearch: () -> Unit, onTabSelected: (SearchTab) -> Unit, onRecentQuerySelected: (String) -> Unit, onClearRecentQueries: () -> Unit, onBuildCompleteIndex: () -> Unit, onChannelClick: (Channel) -> Unit, onChannelLongClick: (Channel) -> Unit, onMovieClick: (Movie) -> Unit, onMovieLongClick: (Movie) -> Unit, onSeriesClick: (Series) -> Unit, onSeriesLongClick: (Series) -> Unit, isChannelLocked: (Channel) -> Boolean, isMovieLocked: (Movie) -> Boolean, isSeriesLocked: (Series) -> Boolean) {
    LazyColumn(Modifier.fillMaxSize().background(PremiumCanvas).padding(30.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
        item("premium_search_head") { Column(verticalArrangement = Arrangement.spacedBy(7.dp)) { Text("SEARCH", style = MaterialTheme.typography.displaySmall, color = PremiumText); Text("PREMIUM LIBRARY INDEX", style = MaterialTheme.typography.labelLarge, color = PremiumMuted); SearchInput(value = query, onValueChange = onQueryChange, onSearch = onSearch, placeholder = stringResource(R.string.search_hint), focusRequester = searchFocusRequester, modifier = Modifier.fillMaxWidth()); LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(SearchTab.entries.toList(), key = { it.name }) { tab -> PremiumSearchChip(stringResource(tab.titleRes), tab == selectedTab) { onTabSelected(tab) } } } } }
        if (recentQueries.isNotEmpty() && query.isBlank()) item("premium_search_history") { Column(verticalArrangement = Arrangement.spacedBy(7.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Text("RECENT SEARCHES", Modifier.weight(1f), style = MaterialTheme.typography.titleSmall, color = PremiumText); PremiumSearchChip(stringResource(R.string.search_clear_history), false, onClearRecentQueries) }; LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(recentQueries, key = { it }) { recent -> PremiumSearchChip(recent, false) { onRecentQuerySelected(recent) } } } } }
        when { !uiState.hasActiveProvider -> item("premium_search_provider") { PremiumSearchState("NO ACTIVE PROVIDER", stringResource(R.string.search_no_provider_subtitle)) }; uiState.queryLength < 2 -> item("premium_search_ready") { PremiumSearchState("SEARCH READY", stringResource(R.string.search_type_to_search)) }; uiState.isLoading -> item("premium_search_loading") { PremiumSearchState("SEARCHING", stringResource(R.string.search_loading_subtitle)) }; uiState.isEmpty && uiState.hasSearchError -> item("premium_search_error") { PremiumSearchState("SEARCH ERROR", stringResource(R.string.search_error_subtitle)) }; uiState.isEmpty -> item("premium_search_empty") { PremiumSearchState("NO RESULTS", stringResource(R.string.search_no_results, query)) }; else -> {
            item("premium_search_summary") { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(stringResource(R.string.search_results_title, uiState.totalResults), style = MaterialTheme.typography.titleMedium, color = PremiumText); Text("CATALOGUE / ${uiState.catalogCompleteness.name}", style = MaterialTheme.typography.bodySmall, color = PremiumMuted) }; if (uiState.catalogCompleteness != CatalogCompleteness.COMPLETE) PremiumSearchChip(stringResource(R.string.search_build_complete_index), false, onBuildCompleteIndex) } }
            if ((selectedTab == SearchTab.ALL || selectedTab == SearchTab.LIVE) && uiState.channels.isNotEmpty()) item("premium_search_live") { PremiumSearchSection("LIVE CHANNELS") { uiState.channels.forEach { channel -> val meta = buildList { if (channel.id in recordingChannelIds) add("RECORDING"); if (channel.id in scheduledChannelIds) add("SCHEDULED"); if (isChannelLocked(channel)) add("LOCKED") }.joinToString(" · "); PremiumSearchRow("${channel.number?.toString().orEmpty()}  ${channel.name}", meta, { onChannelClick(channel) }, { onChannelLongClick(channel) }) } } }
            if ((selectedTab == SearchTab.ALL || selectedTab == SearchTab.MOVIES) && uiState.movies.isNotEmpty()) item("premium_search_movies") { PremiumSearchSection("FILMS") { uiState.movies.forEach { movie -> PremiumSearchRow(movie.name, listOfNotNull(movie.year?.toString(), movie.genre, if (isMovieLocked(movie)) "LOCKED" else null).joinToString(" · "), { onMovieClick(movie) }, { onMovieLongClick(movie) }) } } }
            if ((selectedTab == SearchTab.ALL || selectedTab == SearchTab.SERIES) && uiState.series.isNotEmpty()) item("premium_search_series") { PremiumSearchSection("SERIES") { uiState.series.forEach { series -> PremiumSearchRow(series.name, listOfNotNull(series.genre, if (isSeriesLocked(series)) "LOCKED" else null).joinToString(" · "), { onSeriesClick(series) }, { onSeriesLongClick(series) }) } } }
        } }
    }
}
@Composable private fun PremiumSearchSection(title: String, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) = Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(title, style = MaterialTheme.typography.titleLarge, color = PremiumText); content() }
@Composable private fun PremiumSearchState(title: String, subtitle: String) = Column(Modifier.fillMaxWidth().background(PremiumPanel, RoundedCornerShape(10.dp)).padding(20.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) { Text(title, style = MaterialTheme.typography.titleLarge, color = PremiumText); Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = PremiumMuted) }
@Composable private fun PremiumSearchChip(label: String, selected: Boolean, onClick: () -> Unit) { val shape = RoundedCornerShape(6.dp); TvClickableSurface(onClick = onClick, shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = if (selected) PremiumPanelFocused else PremiumPanel, focusedContainerColor = PremiumPanelFocused, contentColor = PremiumText, focusedContentColor = PremiumText), border = ClickableSurfaceDefaults.border(border = Border(border = BorderStroke(1.dp, if (selected) PremiumGold else PremiumMetal), shape = shape), focusedBorder = Border(border = BorderStroke(2.dp, PremiumFocus), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f)) { Text(label.uppercase(), Modifier.padding(horizontal = 14.dp, vertical = 10.dp), style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis) } }
@Composable private fun PremiumSearchRow(title: String, subtitle: String, onClick: () -> Unit, onLongClick: () -> Unit) { val shape = RoundedCornerShape(7.dp); TvClickableSurface(onClick = onClick, onLongClick = onLongClick, modifier = Modifier.fillMaxWidth(), shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = PremiumPanel, focusedContainerColor = PremiumPanelFocused, contentColor = PremiumText, focusedContentColor = PremiumText), border = ClickableSurfaceDefaults.border(border = Border(border = BorderStroke(1.dp, PremiumMetal), shape = shape), focusedBorder = Border(border = BorderStroke(2.dp, PremiumFocus), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.015f)) { Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis); if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.labelSmall, color = PremiumMuted, maxLines = 1, overflow = TextOverflow.Ellipsis) }; Text("HOLD", style = MaterialTheme.typography.labelSmall, color = PremiumGold) } } }
