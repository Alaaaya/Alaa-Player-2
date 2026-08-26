package com.streamvault.app.ui.themes.glass

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

/** Home Glassmorphism: موزاييك زجاجي عائم، لا قائمة أو رفوف Cinema/Minimal. */
@Composable
internal fun GlassmorphismDashboard(
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
        modifier = Modifier.fillMaxSize().background(GlassCanvas),
        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item("glass_dashboard_header") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("ALAA PLAYER / GLASS HOME", style = MaterialTheme.typography.labelLarge, color = GlassAccent)
                Text(uiState.feature.title.ifBlank { "Your viewing space" }, style = MaterialTheme.typography.displaySmall, color = GlassText, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(uiState.feature.summary.ifBlank { "Float between live channels and your saved library." }, style = MaterialTheme.typography.bodyLarge, color = GlassMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
        item("glass_dashboard_primary_mosaic") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                GlassDashboardTile("LIVE TV", "Browse channels and categories", Modifier.weight(1.45f), onClick = { onNavigate(Routes.LIVE_TV) })
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    GlassDashboardTile("FILMS", "Catalogue", Modifier.fillMaxWidth(), onClick = { onNavigate(Routes.MOVIES) })
                    GlassDashboardTile("SERIES", "Seasons and episodes", Modifier.fillMaxWidth(), onClick = { onNavigate(Routes.SERIES) })
                }
            }
        }
        if (uiState.continueWatching.isNotEmpty()) item("glass_dashboard_continue") {
            GlassDashboardTile("CONTINUE WATCHING", uiState.continueWatching.first().title, Modifier.fillMaxWidth(), onClick = { onContinueWatchingItemClick(uiState.continueWatching.first()) })
        }
        if (uiState.favoriteChannels.isNotEmpty() || uiState.recentChannels.isNotEmpty()) item("glass_dashboard_channels") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                GlassDashboardTile("FAVOURITES", "${uiState.favoriteChannels.size} saved channels", Modifier.weight(1f), onClick = { onNavigate(Routes.liveTv(VirtualCategoryIds.FAVORITES)) })
                GlassDashboardTile("RECENT LIVE", "${uiState.recentChannels.size} recently viewed", Modifier.weight(1f), onClick = { onNavigate(Routes.liveTv(VirtualCategoryIds.RECENT)) })
            }
        }
        uiState.favoriteChannels.take(2).forEach { channel -> item("glass_favorite_${channel.id}") {
            GlassDashboardTile(
                title = channel.name,
                subtitle = when {
                    channel.id in recordingChannelIds -> "Recording active"
                    channel.id in scheduledChannelIds -> "Recording scheduled"
                    else -> channel.currentProgram?.title ?: "Favourite channel"
                },
                modifier = Modifier.fillMaxWidth(),
                onClick = { onFavoriteChannelClick(channel, uiState.currentCombinedProfileId) }
            )
        } }
        uiState.recentChannels.take(2).forEach { channel -> item("glass_recent_${channel.id}") {
            GlassDashboardTile(channel.name, channel.currentProgram?.title ?: "Recent channel", Modifier.fillMaxWidth(), onClick = { onRecentChannelClick(channel, uiState.currentCombinedProfileId) })
        } }
        val featuredMovie = (uiState.topRatedMovies + uiState.recommendedMovies + uiState.recentMovies).distinctBy { it.id }.firstOrNull()
        val featuredSeries = (uiState.recentSeries + uiState.favoriteSeries).distinctBy { it.id }.firstOrNull()
        if (featuredMovie != null || featuredSeries != null) item("glass_dashboard_library") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                featuredMovie?.let { GlassDashboardTile("FILM / ${it.name}", it.genre ?: "Open film details", Modifier.weight(1f), onClick = { onMovieClick(it) }) }
                featuredSeries?.let { GlassDashboardTile("SERIES / ${it.name}", it.genre ?: "Open series details", Modifier.weight(1f), onClick = { onSeriesClick(it) }) }
            }
        }
        item("glass_dashboard_tools") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                GlassDashboardTile("SEARCH", "Find your next stream", Modifier.weight(1f), onClick = { onNavigate(Routes.SEARCH) })
                GlassDashboardTile("SETTINGS", "Providers and preferences", Modifier.weight(1f), onClick = { onNavigate(Routes.SETTINGS) })
            }
        }
    }
}

@Composable
private fun GlassDashboardTile(title: String, subtitle: String, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(24.dp)
    TvClickableSurface(
        onClick = onClick,
        modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = GlassPane, focusedContainerColor = GlassPaneFocused, contentColor = GlassText, focusedContentColor = GlassText),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, GlassFocus), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.025f)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(7.dp), horizontalAlignment = Alignment.Start) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = GlassText, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = GlassMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text("OPEN", style = MaterialTheme.typography.labelSmall, color = GlassAccent)
        }
    }
}
