package com.streamvault.app.ui.themes.redcinema

/** Red Cinema search contract: an archive office with ticketed result registers and act filters. */

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
internal fun RedCinemaArchiveSearch(
    query: String, selectedTab: SearchTab, recentQueries: List<String>, uiState: SearchUiState, recordingChannelIds: Set<Long>, scheduledChannelIds: Set<Long>, searchFocusRequester: FocusRequester,
    onQueryChange: (String) -> Unit, onSearch: () -> Unit, onTabSelected: (SearchTab) -> Unit, onRecentQuerySelected: (String) -> Unit, onClearRecentQueries: () -> Unit, onBuildCompleteIndex: () -> Unit,
    onChannelClick: (Channel) -> Unit, onChannelLongClick: (Channel) -> Unit, onMovieClick: (Movie) -> Unit, onMovieLongClick: (Movie) -> Unit, onSeriesClick: (Series) -> Unit, onSeriesLongClick: (Series) -> Unit,
    isChannelLocked: (Channel) -> Boolean, isMovieLocked: (Movie) -> Boolean, isSeriesLocked: (Series) -> Boolean
) {
    val s = LocalThemePresentation.current.surfaces
    LazyColumn(Modifier.fillMaxSize().background(s.canvas).padding(28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item("red_cinema_archive_header") { Column(Modifier.fillMaxWidth().background(s.browseContent, RoundedCornerShape(2.dp)).padding(20.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) { Text("ARCHIVE OFFICE", style = MaterialTheme.typography.displaySmall); Text("FIND A SCREENING", style = MaterialTheme.typography.labelMedium, color = s.accent); SearchInput(value = query, onValueChange = onQueryChange, onSearch = onSearch, placeholder = stringResource(R.string.search_hint), focusRequester = searchFocusRequester, modifier = Modifier.fillMaxWidth()); LazyRow(horizontalArrangement = Arrangement.spacedBy(5.dp)) { items(SearchTab.entries.toList(), key = { it.name }) { tab -> RedCinemaArchiveTicket(stringResource(tab.titleRes), tab == selectedTab) { onTabSelected(tab) } } } } }
        if (recentQueries.isNotEmpty() && query.isBlank()) item("red_cinema_recent_queries") { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) { Text("RECENT", style = MaterialTheme.typography.titleSmall); recentQueries.take(4).forEach { recent -> RedCinemaArchiveTicket(recent, false) { onRecentQuerySelected(recent) } }; RedCinemaArchiveTicket("CLEAR", false, onClearRecentQueries) } }
        when { !uiState.hasActiveProvider -> item { RedCinemaArchiveState("NO ACTIVE PROVIDER", stringResource(R.string.search_no_provider_subtitle)) }; uiState.queryLength < 2 -> item { RedCinemaArchiveState("ARCHIVE READY", stringResource(R.string.search_type_to_search)) }; uiState.isLoading -> item { RedCinemaArchiveState("SEARCHING REELS", stringResource(R.string.search_loading_subtitle)) }; uiState.isEmpty && uiState.hasSearchError -> item { RedCinemaArchiveState("ARCHIVE ERROR", stringResource(R.string.search_error_subtitle)) }; uiState.isEmpty -> item { RedCinemaArchiveState("NO TICKETS FOUND", stringResource(R.string.search_no_results, query)) }; else -> {
            item("red_cinema_search_summary") { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("${uiState.totalResults} LISTINGS", style = MaterialTheme.typography.titleLarge); if (uiState.catalogCompleteness != CatalogCompleteness.COMPLETE) RedCinemaArchiveTicket(stringResource(R.string.search_build_complete_index), false, onBuildCompleteIndex) } }
            if ((selectedTab == SearchTab.ALL || selectedTab == SearchTab.LIVE) && uiState.channels.isNotEmpty()) item("red_cinema_live_results") { RedCinemaResultRegister("LIVE SCREENINGS") { uiState.channels.forEach { channel -> RedCinemaArchiveRow("${channel.number?.toString().orEmpty()}  ${channel.name}", buildList { if (channel.id in recordingChannelIds) add("RECORDING"); if (channel.id in scheduledChannelIds) add("SCHEDULED"); if (isChannelLocked(channel)) add("RESTRICTED") }.joinToString(" · "), { onChannelClick(channel) }, { onChannelLongClick(channel) }) } } }
            if ((selectedTab == SearchTab.ALL || selectedTab == SearchTab.MOVIES) && uiState.movies.isNotEmpty()) item("red_cinema_movie_results") { RedCinemaResultRegister("FEATURE FILMS") { uiState.movies.forEach { movie -> RedCinemaArchiveRow(movie.name, listOfNotNull(movie.year?.toString(), movie.genre, if (isMovieLocked(movie)) "RESTRICTED" else null).joinToString(" · "), { onMovieClick(movie) }, { onMovieLongClick(movie) }) } } }
            if ((selectedTab == SearchTab.ALL || selectedTab == SearchTab.SERIES) && uiState.series.isNotEmpty()) item("red_cinema_series_results") { RedCinemaResultRegister("SERIAL PRODUCTIONS") { uiState.series.forEach { series -> RedCinemaArchiveRow(series.name, listOfNotNull(series.genre, if (isSeriesLocked(series)) "RESTRICTED" else null).joinToString(" · "), { onSeriesClick(series) }, { onSeriesLongClick(series) }) } } }
        } }
    }
}

@Composable
private fun RedCinemaResultRegister(title: String, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) { val s = LocalThemePresentation.current.surfaces; Column(Modifier.fillMaxWidth().background(s.browseContent, RoundedCornerShape(2.dp)).padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) { Text(title, style = MaterialTheme.typography.titleLarge, color = s.accent); content() } }
@Composable
private fun RedCinemaArchiveState(title: String, subtitle: String) { val s = LocalThemePresentation.current.surfaces; Column(Modifier.fillMaxWidth().background(s.browseContent, RoundedCornerShape(2.dp)).padding(20.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) { Text(title, style = MaterialTheme.typography.titleLarge); Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = s.textSecondary) } }
@Composable
private fun RedCinemaArchiveTicket(label: String, selected: Boolean, onClick: () -> Unit) { val s = LocalThemePresentation.current.surfaces; val shape = RoundedCornerShape(2.dp); TvClickableSurface(onClick = onClick, shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = if (selected) s.selectedAccent else s.browseContent, focusedContainerColor = s.focusedSurface, contentColor = s.textPrimary, focusedContentColor = s.textPrimary), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, s.accent), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.018f)) { Text(label, Modifier.padding(horizontal = 12.dp, vertical = 9.dp), style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis) } }
@Composable
private fun RedCinemaArchiveRow(title: String, subtitle: String, onClick: () -> Unit, onLongClick: () -> Unit) { val s = LocalThemePresentation.current.surfaces; val shape = RoundedCornerShape(2.dp); TvClickableSurface(onClick = onClick, onLongClick = onLongClick, modifier = Modifier.fillMaxWidth(), shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = s.canvas, focusedContainerColor = s.focusedSurface, contentColor = s.textPrimary, focusedContentColor = s.textPrimary), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, s.accent), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.018f)) { Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis); if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.labelSmall, color = s.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis) }; Text("TICKET", style = MaterialTheme.typography.labelSmall, color = s.accent) } } }
