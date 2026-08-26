package com.streamvault.app.ui.themes.streaming

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

/** Home Streaming Platform: Hero واسع ثم رفوف carousel أفقية، وليس mosaic أو قائمة تحريرية. */
@Composable
internal fun StreamingPlatformDashboard(
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
        modifier = Modifier.fillMaxSize().background(StreamingCanvas),
        contentPadding = PaddingValues(horizontal = 36.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item("streaming_hero") {
            StreamingHero(
                title = uiState.feature.title.ifBlank { "Alaa Player" },
                summary = uiState.feature.summary.ifBlank { "Live channels, films and series in one place." },
                onOpenLive = { onNavigate(Routes.LIVE_TV) },
                onOpenSearch = { onNavigate(Routes.SEARCH) }
            )
        }
        item("streaming_primary") {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                StreamingQuickTile("MOVIES", "Browse films", Modifier.weight(1f)) { onNavigate(Routes.MOVIES) }
                StreamingQuickTile("SERIES", "Find episodes", Modifier.weight(1f)) { onNavigate(Routes.SERIES) }
                StreamingQuickTile("SAVED LIVE", "Saved channels", Modifier.weight(1f)) { onNavigate(Routes.liveTv(VirtualCategoryIds.FAVORITES)) }
                StreamingQuickTile("LIVE GUIDE", "Channels and EPG", Modifier.weight(1f)) { onNavigate(Routes.LIVE_TV) }
            }
        }
        if (uiState.continueWatching.isNotEmpty()) item("streaming_continue") {
            StreamingShelf(
                title = "Continue watching",
                subtitle = "Resume where you left off",
                items = uiState.continueWatching.take(12),
                label = { it.title },
                detail = { it.contentType.name.replace('_', ' ') },
                onClick = onContinueWatchingItemClick
            )
        }
        if (uiState.favoriteChannels.isNotEmpty()) item("streaming_favourites") {
            StreamingShelf(
                title = "Favourite channels",
                subtitle = "Your saved live stations",
                items = uiState.favoriteChannels.take(14),
                label = { it.name },
                detail = { channel ->
                    when {
                        channel.id in recordingChannelIds -> "RECORDING"
                        channel.id in scheduledChannelIds -> "SCHEDULED"
                        else -> channel.currentProgram?.title ?: "LIVE CHANNEL"
                    }
                },
                onClick = { onFavoriteChannelClick(it, uiState.currentCombinedProfileId) }
            )
        }
        if (uiState.recentChannels.isNotEmpty()) item("streaming_recent_live") {
            StreamingShelf(
                title = "Recently watched live",
                subtitle = "Return to a live channel",
                items = uiState.recentChannels.take(14),
                label = { it.name },
                detail = { it.currentProgram?.title ?: "LIVE CHANNEL" },
                onClick = { onRecentChannelClick(it, uiState.currentCombinedProfileId) }
            )
        }
        val movies = (uiState.recommendedMovies + uiState.topRatedMovies + uiState.recentMovies).distinctBy { it.id }.take(14)
        if (movies.isNotEmpty()) item("streaming_movies") {
            StreamingShelf("Recommended films", "Curated from your catalogue", movies, { it.name }, { it.genre ?: "FILM" }, onMovieClick)
        }
        val series = (uiState.recentSeries + uiState.favoriteSeries).distinctBy { it.id }.take(14)
        if (series.isNotEmpty()) item("streaming_series") {
            StreamingShelf("Series to explore", "Seasons and episodes", series, { it.name }, { it.genre ?: "SERIES" }, onSeriesClick)
        }
        item("streaming_footer_actions") {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                StreamingQuickTile("SEARCH", "Search all content", Modifier.weight(1f)) { onNavigate(Routes.SEARCH) }
                StreamingQuickTile("SETTINGS", "Providers and preferences", Modifier.weight(1f)) { onNavigate(Routes.SETTINGS) }
                StreamingQuickTile("LIVE FAVOURITES", "Open saved channels", Modifier.weight(1f)) { onNavigate(Routes.liveTv(VirtualCategoryIds.FAVORITES)) }
            }
        }
    }
}

@Composable
private fun StreamingHero(title: String, summary: String, onOpenLive: () -> Unit, onOpenSearch: () -> Unit) {
    val shape = RoundedCornerShape(24.dp)
    TvClickableSurface(
        onClick = onOpenLive,
        modifier = Modifier.fillMaxWidth().height(260.dp),
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = StreamingPanel, focusedContainerColor = StreamingPanelFocused, contentColor = StreamingText, focusedContentColor = StreamingText),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, StreamingFocus), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.015f)
    ) {
        Box(Modifier.fillMaxSize().padding(30.dp)) {
            Column(modifier = Modifier.align(Alignment.BottomStart), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("FEATURED / STREAMING PLATFORM", style = MaterialTheme.typography.labelLarge, color = StreamingAccent)
                Text(title, style = MaterialTheme.typography.displaySmall, color = StreamingText, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(summary, style = MaterialTheme.typography.bodyLarge, color = StreamingMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("OPEN LIVE TV", style = MaterialTheme.typography.labelLarge, color = StreamingFocus)
                    Text("SEARCH", style = MaterialTheme.typography.labelLarge, color = StreamingMuted, modifier = Modifier
                        .padding(start = 10.dp)
                        .then(Modifier))
                }
            }
        }
    }
}

@Composable
private fun StreamingQuickTile(title: String, subtitle: String, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    TvClickableSurface(
        onClick = onClick,
        modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = StreamingPanel, focusedContainerColor = StreamingPanelFocused, contentColor = StreamingText, focusedContentColor = StreamingText),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, StreamingFocus), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.025f)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = StreamingText, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = StreamingMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun <T> StreamingShelf(title: String, subtitle: String, items: List<T>, label: (T) -> String, detail: (T) -> String, onClick: (T) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = StreamingText)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = StreamingMuted)
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items, key = { item -> "$title-${label(item)}" }) { item ->
                val shape = RoundedCornerShape(14.dp)
                TvClickableSurface(
                    onClick = { onClick(item) },
                    modifier = Modifier.width(218.dp).height(122.dp),
                    shape = ClickableSurfaceDefaults.shape(shape),
                    colors = ClickableSurfaceDefaults.colors(containerColor = StreamingPanel, focusedContainerColor = StreamingPanelFocused, contentColor = StreamingText, focusedContentColor = StreamingText),
                    border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, StreamingFocus), shape = shape)),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.035f)
                ) {
                    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text(label(item), style = MaterialTheme.typography.titleSmall, color = StreamingText, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(detail(item), style = MaterialTheme.typography.bodySmall, color = StreamingMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text("OPEN", style = MaterialTheme.typography.labelSmall, color = StreamingAccent)
                    }
                }
            }
        }
    }
}
