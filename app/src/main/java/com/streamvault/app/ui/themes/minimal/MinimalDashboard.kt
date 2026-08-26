package com.streamvault.app.ui.themes.minimal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.app.navigation.Routes
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.app.ui.screens.dashboard.DashboardUiState
import com.streamvault.domain.model.Channel
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.PlaybackHistory
import com.streamvault.domain.model.Series
import com.streamvault.domain.model.VirtualCategoryIds

/** Minimal home is an editorial command list, not a poster shelf or a visual dashboard. */
@Composable
internal fun MinimalDashboard(
    uiState: DashboardUiState,
    recordingChannelIds: Set<Long>,
    scheduledChannelIds: Set<Long>,
    onNavigate: (String) -> Unit,
    onRecentChannelClick: (Channel, Long?) -> Unit,
    onFavoriteChannelClick: (Channel, Long?) -> Unit,
    onMovieClick: (Movie) -> Unit,
    onSeriesClick: (Series) -> Unit,
    onContinueWatchingItemClick: (PlaybackHistory) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MinimalCanvas),
        contentPadding = PaddingValues(horizontal = 42.dp, vertical = 34.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item("minimal_dashboard_heading") {
            Column(modifier = Modifier.padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("ALAA PLAYER", style = MaterialTheme.typography.displaySmall, color = MinimalText, fontWeight = FontWeight.Black)
                Text(uiState.feature.summary.ifBlank { "Choose a destination to continue." }, style = MaterialTheme.typography.bodyLarge, color = MinimalMuted)
            }
        }
        item("minimal_dashboard_live") {
            MinimalCommandLine("01", "Live television", "Browse categories and channels", onClick = { onNavigate(Routes.LIVE_TV) })
        }
        if (uiState.continueWatching.isNotEmpty()) item("minimal_dashboard_continue") {
            MinimalCommandLine("02", "Continue watching", "Resume your latest program", onClick = { onContinueWatchingItemClick(uiState.continueWatching.first()) })
        }
        if (uiState.favoriteChannels.isNotEmpty()) item("minimal_dashboard_favorites") {
            MinimalCommandLine("03", "Favourite channels", "${uiState.favoriteChannels.size} saved channels", onClick = { onNavigate(Routes.liveTv(VirtualCategoryIds.FAVORITES)) })
        }
        if (uiState.recentChannels.isNotEmpty()) item("minimal_dashboard_recent") {
            MinimalCommandLine("04", "Recent channels", "${uiState.recentChannels.size} recently viewed", onClick = { onNavigate(Routes.liveTv(VirtualCategoryIds.RECENT)) })
        }
        item("minimal_dashboard_movies") { MinimalCommandLine("05", "Films", "Explore the film catalogue", onClick = { onNavigate(Routes.MOVIES) }) }
        item("minimal_dashboard_series") { MinimalCommandLine("06", "Series", "Explore series and episodes", onClick = { onNavigate(Routes.SERIES) }) }
        item("minimal_dashboard_search") { MinimalCommandLine("07", "Search", "Find channels, films and series", onClick = { onNavigate(Routes.SEARCH) }) }
        item("minimal_dashboard_settings") { MinimalCommandLine("08", "Settings", "Providers, playback and preferences", onClick = { onNavigate(Routes.SETTINGS) }) }
        val movies = (uiState.topRatedMovies + uiState.recommendedMovies).distinctBy { it.id }.take(4)
        if (movies.isNotEmpty()) item("minimal_dashboard_film_notes") {
            MinimalEditorialSection("Film notes", movies.map { it.name }) { movie -> movies.firstOrNull { it.name == movie }?.let(onMovieClick) }
        }
        val series = (uiState.recentSeries + uiState.favoriteSeries).distinctBy { it.id }.take(4)
        if (series.isNotEmpty()) item("minimal_dashboard_series_notes") {
            MinimalEditorialSection("Series notes", series.map { it.name }) { entry -> series.firstOrNull { it.name == entry }?.let(onSeriesClick) }
        }
    }
}

@Composable
private fun MinimalCommandLine(index: String, label: String, subtitle: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(0.dp)
    TvClickableSurface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = Color.Transparent, focusedContainerColor = MinimalPaper, contentColor = MinimalText, focusedContentColor = MinimalText),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(1.dp, MinimalFocus), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = MinimalFocusedScale)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(index, style = MaterialTheme.typography.labelMedium, color = MinimalMuted)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(label, style = MaterialTheme.typography.titleMedium, color = MinimalText)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MinimalMuted)
            }
            Text("→", style = MaterialTheme.typography.titleMedium, color = MinimalMuted)
        }
    }
}

@Composable
private fun MinimalEditorialSection(title: String, entries: List<String>, onClick: (String) -> Unit) {
    Column(modifier = Modifier.padding(top = 28.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title.uppercase(), style = MaterialTheme.typography.labelLarge, color = MinimalMuted, fontWeight = FontWeight.Bold)
        entries.forEach { entry ->
            MinimalCommandLine("—", entry, "Open details", onClick = { onClick(entry) })
        }
    }
}
