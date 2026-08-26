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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.streamvault.app.navigation.Routes
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.app.ui.screens.dashboard.DashboardFeatureAction
import com.streamvault.app.ui.screens.dashboard.DashboardUiState
import com.streamvault.domain.model.Channel
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.PlaybackHistory
import com.streamvault.domain.model.Series
import com.streamvault.domain.model.VirtualCategoryIds

/** Neon Future's HUD dashboard uses a floating command dock rather than Cinematic shelves and hero cards. */
@Composable
internal fun NeonFutureDashboard(
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
    val primaryAction = {
        when (uiState.feature.actionType) {
            DashboardFeatureAction.LIVE -> onNavigate(Routes.LIVE_TV)
            DashboardFeatureAction.CONTINUE_WATCHING -> uiState.continueWatching.firstOrNull()?.let(onContinueWatchingItemClick) ?: onNavigate(Routes.MOVIES)
            DashboardFeatureAction.MOVIES -> onNavigate(Routes.MOVIES)
            DashboardFeatureAction.SERIES -> onNavigate(Routes.SERIES)
        }
    }
    Box(modifier = Modifier.fillMaxSize().background(NeonCanvas)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 34.dp, top = 28.dp, end = 34.dp, bottom = 34.dp),
            verticalArrangement = Arrangement.spacedBy(26.dp)
        ) {
            item("neon_dashboard_command_dock") { NeonFutureCommandDock(onNavigate = onNavigate) }
            item("neon_dashboard_signal_hero") {
                NeonFutureSignalHero(
                    title = uiState.feature.title.ifBlank { "SYSTEM ONLINE" },
                    summary = uiState.feature.summary.ifBlank { "Your live signal, library and viewing history are available." },
                    artworkUrl = uiState.feature.artworkUrl,
                    actionLabel = uiState.feature.actionLabel.ifBlank { "OPEN SIGNAL" },
                    libraryCount = uiState.topRatedMovies.size + uiState.recommendedMovies.size + uiState.recentSeries.size + uiState.favoriteSeries.size,
                    onAction = primaryAction
                )
            }
            if (uiState.continueWatching.isNotEmpty()) item("neon_dashboard_continue") {
                NeonFutureHistoryShelf(uiState.continueWatching, onContinueWatchingItemClick)
            }
            if (uiState.favoriteChannels.isNotEmpty()) item("neon_dashboard_favorites") {
                NeonFutureChannelShelf(
                    label = "PRIORITY SIGNALS",
                    channels = uiState.favoriteChannels,
                    recordingChannelIds = recordingChannelIds,
                    scheduledChannelIds = scheduledChannelIds,
                    onChannelClick = { onFavoriteChannelClick(it, uiState.currentCombinedProfileId) },
                    onOpen = { onNavigate(Routes.liveTv(VirtualCategoryIds.FAVORITES)) }
                )
            }
            if (uiState.recentChannels.isNotEmpty()) item("neon_dashboard_recent") {
                NeonFutureChannelShelf(
                    label = "RECENT TRANSMISSIONS",
                    channels = uiState.recentChannels,
                    recordingChannelIds = recordingChannelIds,
                    scheduledChannelIds = scheduledChannelIds,
                    onChannelClick = { onRecentChannelClick(it, uiState.currentCombinedProfileId) },
                    onOpen = { onNavigate(Routes.liveTv(VirtualCategoryIds.RECENT)) }
                )
            }
            val movies = (uiState.topRatedMovies + uiState.recommendedMovies).distinctBy { it.id }
            if (movies.isNotEmpty()) item("neon_dashboard_movies") {
                NeonFutureMediaShelf("FILM CACHE", movies.take(24), { it.name }, { it.posterUrl }, { onMovieClick(it) }, { onNavigate(Routes.MOVIES) })
            }
            val series = (uiState.recentSeries + uiState.favoriteSeries).distinctBy { it.id }
            if (series.isNotEmpty()) item("neon_dashboard_series") {
                NeonFutureMediaShelf("SERIES QUEUE", series.take(24), { it.name }, { it.posterUrl }, { onSeriesClick(it) }, { onNavigate(Routes.SERIES) })
            }
        }
    }
}

@Composable
private fun NeonFutureCommandDock(onNavigate: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.background(NeonPanel, RoundedCornerShape(999.dp)).padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NeonFutureDockAction("LIVE", NeonCyan) { onNavigate(Routes.LIVE_TV) }
            NeonFutureDockAction("FILMS", NeonPink) { onNavigate(Routes.MOVIES) }
            NeonFutureDockAction("SERIES", NeonLime) { onNavigate(Routes.SERIES) }
            NeonFutureDockAction("SEARCH", NeonCyan) { onNavigate(Routes.SEARCH) }
        }
    }
}

@Composable
private fun NeonFutureDockAction(label: String, tone: Color, onClick: () -> Unit) {
    val shape = RoundedCornerShape(999.dp)
    TvClickableSurface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = Color.Transparent, focusedContainerColor = tone.copy(alpha = .24f), contentColor = tone, focusedContentColor = NeonText),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, tone), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.04f)
    ) {
        Text(label, modifier = Modifier.padding(horizontal = 18.dp, vertical = 11.dp), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun NeonFutureSignalHero(title: String, summary: String, artworkUrl: String?, actionLabel: String, libraryCount: Int, onAction: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Box(modifier = Modifier.fillMaxWidth().height(320.dp).clip(shape).background(NeonPanel)) {
        AsyncImage(model = artworkUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(NeonCanvas.copy(alpha = .98f), NeonCanvas.copy(alpha = .72f), Color.Transparent))))
        Row(modifier = Modifier.fillMaxSize().padding(28.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("PRIMARY HUD / $libraryCount ASSETS", style = MaterialTheme.typography.labelMedium, color = NeonCyan, fontWeight = FontWeight.Black)
                Text(title, style = MaterialTheme.typography.displaySmall, color = NeonText, fontWeight = FontWeight.Black, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(summary, style = MaterialTheme.typography.bodyLarge, color = NeonMuted, maxLines = 3, overflow = TextOverflow.Ellipsis)
                NeonFutureDockAction(actionLabel.uppercase(), NeonPink, onAction)
            }
            NeonFutureStatusPanel(modifier = Modifier.width(210.dp))
        }
    }
}

@Composable
private fun NeonFutureStatusPanel(modifier: Modifier = Modifier) {
    Column(modifier = modifier.background(NeonPanel.copy(alpha = .9f), RoundedCornerShape(12.dp)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
        Text("SYSTEM PULSE", style = MaterialTheme.typography.labelMedium, color = NeonLime, fontWeight = FontWeight.Black)
        NeonFutureStatusLine("LIVE UPLINK", "ACTIVE", NeonCyan)
        NeonFutureStatusLine("LIBRARY CACHE", "READY", NeonLime)
        NeonFutureStatusLine("FOCUS MODE", "DPAD", NeonPink)
        Text("Data remains tied to the active provider.", style = MaterialTheme.typography.bodySmall, color = NeonMuted)
    }
}

@Composable
private fun NeonFutureStatusLine(label: String, value: String, tone: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = NeonMuted)
        Text(value, style = MaterialTheme.typography.labelSmall, color = tone, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun NeonFutureHistoryShelf(entries: List<PlaybackHistory>, onClick: (PlaybackHistory) -> Unit) {
    NeonFutureShelfHeader("RESUME BUFFER", "VIEW ALL", null)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(entries, key = { it.id.takeIf { id -> id > 0L } ?: it.contentId }) { entry ->
            val shape = RoundedCornerShape(10.dp)
            TvClickableSurface(
                onClick = { onClick(entry) }, modifier = Modifier.width(224.dp), shape = ClickableSurfaceDefaults.shape(shape),
                colors = ClickableSurfaceDefaults.colors(containerColor = NeonPanel, focusedContainerColor = NeonPanelRaised, contentColor = NeonText, focusedContentColor = NeonText),
                border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, NeonCyan), shape = shape))
            ) {
                Column {
                    AsyncImage(model = entry.posterUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().height(118.dp), contentScale = ContentScale.Crop)
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(entry.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        LinearProgressIndicator(progress = { if (entry.totalDurationMs > 0L) (entry.resumePositionMs.toFloat() / entry.totalDurationMs).coerceIn(0f, 1f) else 0f }, modifier = Modifier.fillMaxWidth().height(3.dp), color = NeonPink, trackColor = NeonMuted.copy(alpha = .18f))
                    }
                }
            }
        }
    }
}

@Composable
private fun NeonFutureChannelShelf(label: String, channels: List<Channel>, recordingChannelIds: Set<Long>, scheduledChannelIds: Set<Long>, onChannelClick: (Channel) -> Unit, onOpen: () -> Unit) {
    NeonFutureShelfHeader(label, "OPEN GRID", onOpen)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(channels, key = { it.id }) { channel ->
            val shape = RoundedCornerShape(10.dp)
            TvClickableSurface(
                onClick = { onChannelClick(channel) }, modifier = Modifier.width(238.dp), shape = ClickableSurfaceDefaults.shape(shape),
                colors = ClickableSurfaceDefaults.colors(containerColor = NeonPanel, focusedContainerColor = NeonPanelRaised, contentColor = NeonText, focusedContentColor = NeonText),
                border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, NeonPink), shape = shape))
            ) {
                Row(modifier = Modifier.padding(13.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(46.dp).background(NeonCanvas, RoundedCornerShape(7.dp)), contentAlignment = Alignment.Center) { AsyncImage(channel.logoUrl, null, Modifier.fillMaxSize(), contentScale = ContentScale.Fit) }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(channel.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(when { channel.id in recordingChannelIds -> "RECORDING"; channel.id in scheduledChannelIds -> "SCHEDULED"; else -> channel.currentProgram?.title ?: "LIVE" }, style = MaterialTheme.typography.labelSmall, color = NeonMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> NeonFutureMediaShelf(label: String, items: List<T>, title: (T) -> String, artwork: (T) -> String?, onClick: (T) -> Unit, onOpen: () -> Unit) {
    NeonFutureShelfHeader(label, "OPEN ARCHIVE", onOpen)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(items) { item ->
            val shape = RoundedCornerShape(8.dp)
            TvClickableSurface(
                onClick = { onClick(item) }, modifier = Modifier.width(152.dp), shape = ClickableSurfaceDefaults.shape(shape),
                colors = ClickableSurfaceDefaults.colors(containerColor = NeonPanel, focusedContainerColor = NeonPanelRaised, contentColor = NeonText, focusedContentColor = NeonText),
                border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, NeonLime), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.04f)
            ) {
                Column {
                    AsyncImage(artwork(item), null, Modifier.fillMaxWidth().height(190.dp), contentScale = ContentScale.Crop)
                    Text(title(item), modifier = Modifier.padding(10.dp), style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun NeonFutureShelfHeader(label: String, action: String, onAction: (() -> Unit)?) {
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 9.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.titleLarge, color = NeonText, fontWeight = FontWeight.Black)
        if (onAction != null) NeonFutureDockAction(action, NeonCyan, onAction)
    }
}
