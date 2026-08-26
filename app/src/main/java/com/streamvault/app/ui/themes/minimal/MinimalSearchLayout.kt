package com.streamvault.app.ui.themes.minimal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.app.ui.components.SearchInput
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.app.ui.screens.search.SearchTab
import com.streamvault.app.ui.screens.search.SearchUiState
import com.streamvault.domain.model.Channel
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.Series

@Composable
internal fun MinimalSearchLayout(
    query: String,
    selectedTab: SearchTab,
    uiState: SearchUiState,
    searchFocusRequester: FocusRequester,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onChannelClick: (Channel) -> Unit,
    onMovieClick: (Movie) -> Unit,
    onSeriesClick: (Series) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize().background(MinimalCanvas), contentPadding = PaddingValues(34.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("SEARCH INDEX", style = MaterialTheme.typography.headlineMedium, color = MinimalText) }
        item { SearchInput(value = query, onValueChange = onQueryChange, onSearch = onSearch, placeholder = "Search active catalogue", focusRequester = searchFocusRequester) }
        item { Text(selectedTab.name, style = MaterialTheme.typography.labelLarge, color = MinimalMuted) }
        when {
            !uiState.hasActiveProvider -> item { Text("No active provider.", color = MinimalMuted) }
            uiState.queryLength < 2 -> item { Text("Enter at least two characters.", color = MinimalMuted) }
            uiState.isLoading -> item { Text("Searching…", color = MinimalMuted) }
            uiState.isEmpty -> item { Text("No results.", color = MinimalMuted) }
            else -> {
                if (uiState.channels.isNotEmpty()) item { Text("CHANNELS", style = MaterialTheme.typography.labelLarge, color = MinimalMuted) }
                items(uiState.channels, key = { it.id }) { channel -> MinimalSearchResult("${channel.number}  ${channel.name}") { onChannelClick(channel) } }
                if (uiState.movies.isNotEmpty()) item { Text("FILMS", style = MaterialTheme.typography.labelLarge, color = MinimalMuted) }
                items(uiState.movies, key = { it.id }) { movie -> MinimalSearchResult(movie.name) { onMovieClick(movie) } }
                if (uiState.series.isNotEmpty()) item { Text("SERIES", style = MaterialTheme.typography.labelLarge, color = MinimalMuted) }
                items(uiState.series, key = { it.id }) { series -> MinimalSearchResult(series.name) { onSeriesClick(series) } }
            }
        }
    }
}

@Composable
private fun MinimalSearchResult(label: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(0.dp)
    TvClickableSurface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = Color.Transparent, focusedContainerColor = MinimalPaper, contentColor = MinimalText, focusedContentColor = MinimalText),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(1.dp, MinimalFocus), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = MinimalFocusedScale)
    ) {
        Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp), style = MaterialTheme.typography.titleSmall, maxLines = 1)
    }
}
