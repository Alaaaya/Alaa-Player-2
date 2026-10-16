package com.streamvault.app.ui.themes.goldnight

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.app.R
import com.streamvault.app.ui.components.SearchInput
import com.streamvault.app.ui.screens.search.SearchTab
import com.streamvault.app.ui.screens.search.SearchUiState
import com.streamvault.domain.model.CatalogCompleteness
import com.streamvault.domain.model.Channel
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.Series

private val GoldNightKeyboardRows = listOf(
    listOf("A", "B", "C", "D", "E", "F"),
    listOf("G", "H", "I", "J", "K", "L"),
    listOf("M", "N", "O", "P", "Q", "R"),
    listOf("S", "T", "U", "V", "W", "X"),
    listOf("Y", "Z", "1", "2", "3", "4"),
    listOf("5", "6", "7", "8", "9", "0")
)

/**
 * Gold Night Search: عمود لوحة مفاتيح على الشاشة مع حقل البحث، ثم ثلاثة أعمدة نتائج: القنوات | الأفلام | المسلسلات.
 * يعرض كل الأنواع معاً لذلك يثبّت التبويب على ALL عبر callback الشاشة المشتركة.
 */
@Composable
internal fun GoldNightSearchLayout(
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
    LaunchedEffect(selectedTab) {
        if (selectedTab != SearchTab.ALL) onTabSelected(SearchTab.ALL)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GoldNightCanvasBrush)
            .padding(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        GoldNightScreenHeader(title = stringResource(R.string.search_title), breadcrumb = "Home")
        Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            Column(modifier = Modifier.width(300.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SearchInput(
                    value = query,
                    onValueChange = onQueryChange,
                    onSearch = onSearch,
                    placeholder = stringResource(R.string.search_hint),
                    focusRequester = searchFocusRequester,
                    modifier = Modifier.fillMaxWidth()
                )
                GoldNightKeyboard(
                    onKey = { key -> onQueryChange(query + key.lowercase()) },
                    onSpace = { onQueryChange("$query ") },
                    onBackspace = { if (query.isNotEmpty()) onQueryChange(query.dropLast(1)) },
                    onClear = { onQueryChange("") },
                    onSearch = onSearch
                )
                if (recentQueries.isNotEmpty() && query.isBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Recent", style = MaterialTheme.typography.titleSmall, color = GoldNightText, modifier = Modifier.weight(1f))
                        GoldNightLibraryChip(label = stringResource(R.string.search_clear_history), onClick = onClearRecentQueries)
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        items(recentQueries, key = { "gn_recent_$it" }) { recent ->
                            GoldNightCard(onClick = { onRecentQuerySelected(recent) }, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = recent,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
            GoldNightColumnDivider()
            Column(modifier = Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val state: Pair<String, String>? = when {
                    !uiState.hasActiveProvider -> "No active provider" to stringResource(R.string.search_no_provider_subtitle)
                    uiState.queryLength < 2 -> "Search ready" to stringResource(R.string.search_type_to_search)
                    uiState.isLoading -> "Searching" to stringResource(R.string.search_loading_subtitle)
                    uiState.isEmpty && uiState.hasSearchError -> "Search error" to stringResource(R.string.search_error_subtitle)
                    uiState.isEmpty -> "No results" to stringResource(R.string.search_no_results, query)
                    else -> null
                }
                if (state != null) {
                    GoldNightStatePanel(title = state.first, subtitle = state.second)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.search_results_title, uiState.totalResults),
                            style = MaterialTheme.typography.titleSmall,
                            color = GoldNightMuted,
                            modifier = Modifier.weight(1f)
                        )
                        if (uiState.catalogCompleteness != CatalogCompleteness.COMPLETE) {
                            GoldNightLibraryChip(label = stringResource(R.string.search_build_complete_index), onClick = onBuildCompleteIndex)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        GoldNightResultColumn(title = "Channels", count = uiState.channels.size) {
                            items(uiState.channels, key = { "gn_s_ch_${it.id}" }) { channel ->
                                val locked = isChannelLocked(channel)
                                GoldNightSearchChannelRow(
                                    channel = channel,
                                    locked = locked,
                                    status = when (channel.id) {
                                        in recordingChannelIds -> "REC"
                                        in scheduledChannelIds -> "SCHEDULED"
                                        else -> null
                                    },
                                    onClick = { onChannelClick(channel) },
                                    onLongClick = { onChannelLongClick(channel) }
                                )
                            }
                        }
                        GoldNightResultColumn(title = "Movies", count = uiState.movies.size) {
                            items(uiState.movies, key = { "gn_s_mv_${it.id}" }) { movie ->
                                val locked = isMovieLocked(movie)
                                GoldNightPosterRow(
                                    title = if (locked) "Locked title" else movie.name,
                                    meta = if (locked) "" else listOfNotNull(movie.year, movie.genre?.substringBefore(',')).joinToString(" • "),
                                    rating = "",
                                    posterUrl = if (locked) null else movie.posterUrl,
                                    onClick = { onMovieClick(movie) },
                                    onLongClick = { onMovieLongClick(movie) }
                                )
                            }
                        }
                        GoldNightResultColumn(title = "Series", count = uiState.series.size) {
                            items(uiState.series, key = { "gn_s_sr_${it.id}" }) { entry ->
                                val locked = isSeriesLocked(entry)
                                GoldNightPosterRow(
                                    title = if (locked) "Locked title" else entry.name,
                                    meta = if (locked) "" else entry.genre?.substringBefore(',').orEmpty(),
                                    rating = "",
                                    posterUrl = if (locked) null else entry.posterUrl,
                                    onClick = { onSeriesClick(entry) },
                                    onLongClick = { onSeriesLongClick(entry) }
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
private fun GoldNightKeyboard(
    onKey: (String) -> Unit,
    onSpace: () -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        GoldNightKeyboardRows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { key -> GoldNightKey(label = key, onClick = { onKey(key) }) }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            GoldNightKey(label = ".", onClick = { onKey(".") })
            GoldNightKey(label = "space", onClick = onSpace, weight = 2f)
            GoldNightKey(label = "⌫", onClick = onBackspace)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            GoldNightKey(label = "clear", onClick = onClear, weight = 1f)
            GoldNightKey(label = "search", onClick = onSearch, weight = 1f)
        }
    }
}

@Composable
private fun RowScope.GoldNightKey(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    weight: Float = 1f
) {
    GoldNightCard(
        onClick = onClick,
        cornerRadius = 8.dp,
        focusedScale = 1.06f,
        modifier = modifier.weight(weight).height(42.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = label, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center, maxLines = 1)
        }
    }
}

@Composable
private fun RowScope.GoldNightResultColumn(
    title: String,
    count: Int,
    modifier: Modifier = Modifier,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit
) {
    Column(modifier = modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        GoldNightColumnTitle(title = title, trailing = count.takeIf { it > 0 }?.toString())
        if (count == 0) {
            Text(text = "No matches", style = MaterialTheme.typography.bodySmall, color = GoldNightMuted)
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(vertical = 4.dp, horizontal = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}

@Composable
private fun GoldNightSearchChannelRow(
    channel: Channel,
    locked: Boolean,
    status: String?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GoldNightCard(onClick = onClick, onLongClick = onLongClick, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (channel.number > 0) {
                Text(text = channel.number.toString(), style = MaterialTheme.typography.labelLarge, color = GoldNightMuted, modifier = Modifier.width(34.dp))
            }
            GoldNightRoundLogo(channelName = channel.name, logoUrl = channel.logoUrl, size = 40.dp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = if (locked) "Locked channel" else channel.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                GoldNightProgressBar(progress = if (locked) 0f else goldNightProgramProgress(channel.currentProgram), modifier = Modifier.fillMaxWidth())
            }
            if (status != null) Text(text = status, style = MaterialTheme.typography.labelSmall, color = GoldNightGold)
        }
    }
}
