package com.streamvault.app.ui.themes.blueocean

/** Style contract: Blue Ocean home is a tide timetable with route portals and vertical content currents. */

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import com.streamvault.app.ui.screens.dashboard.DashboardFeatureAction
import com.streamvault.app.ui.screens.dashboard.DashboardUiState
import com.streamvault.app.ui.theme.LocalThemePresentation
import com.streamvault.domain.model.Channel
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.PlaybackHistory
import com.streamvault.domain.model.Series

@Composable
internal fun BlueOceanDashboard(
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
    val surfaces = LocalThemePresentation.current.surfaces
    val featureAction = {
        when (uiState.feature.actionType) {
            DashboardFeatureAction.LIVE -> onNavigate(Routes.LIVE_TV)
            DashboardFeatureAction.CONTINUE_WATCHING -> uiState.continueWatching.firstOrNull()?.let(onContinueWatchingItemClick) ?: onNavigate(Routes.MOVIES)
            DashboardFeatureAction.MOVIES -> onNavigate(Routes.MOVIES)
            DashboardFeatureAction.SERIES -> onNavigate(Routes.SERIES)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(surfaces.canvas).padding(30.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "blue_ocean_feature") {
            BlueOceanFeaturePanel(
                title = uiState.feature.title.ifBlank { "Alaa at high tide" },
                summary = uiState.feature.summary.ifBlank { "Choose a live route, resume a story, or enter the film current." },
                actionLabel = uiState.feature.actionLabel.ifBlank { "ENTER CURRENT" },
                onAction = featureAction
            )
        }
        item(key = "blue_ocean_routes") {
            BlueOceanRoutePortals(onNavigate)
        }
        if (uiState.continueWatching.isNotEmpty()) {
            item(key = "blue_ocean_resume") {
                BlueOceanHistoryCurrent(uiState.continueWatching, onContinueWatchingItemClick)
            }
        }
        if (uiState.favoriteChannels.isNotEmpty()) {
            item(key = "blue_ocean_favorites") {
                BlueOceanChannelCurrent(
                    title = "FAVOURITE HARBOURS",
                    channels = uiState.favoriteChannels,
                    recording = recordingChannelIds,
                    scheduled = scheduledChannelIds,
                    onClick = { channel -> onFavoriteChannelClick(channel, uiState.currentCombinedProfileId) }
                )
            }
        }
        if (uiState.recentChannels.isNotEmpty()) {
            item(key = "blue_ocean_recent") {
                BlueOceanChannelCurrent(
                    title = "RECENT WAVES",
                    channels = uiState.recentChannels,
                    recording = recordingChannelIds,
                    scheduled = scheduledChannelIds,
                    onClick = { channel -> onRecentChannelClick(channel, uiState.currentCombinedProfileId) }
                )
            }
        }
        if (uiState.recommendedMovies.isNotEmpty()) {
            item(key = "blue_ocean_films") {
                BlueOceanMovieCurrent(uiState.recommendedMovies, onMovieClick)
            }
        }
        if (uiState.recentSeries.isNotEmpty()) {
            item(key = "blue_ocean_series") {
                BlueOceanSeriesCurrent(uiState.recentSeries, onSeriesClick)
            }
        }
    }
}

@Composable
private fun BlueOceanFeaturePanel(title: String, summary: String, actionLabel: String, onAction: () -> Unit) {
    val surfaces = LocalThemePresentation.current.surfaces
    val shape = RoundedCornerShape(30.dp)
    TvClickableSurface(
        onClick = onAction,
        modifier = Modifier.fillMaxWidth(),
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = surfaces.browseContent,
            focusedContainerColor = surfaces.focusedSurface,
            contentColor = surfaces.textPrimary,
            focusedContentColor = surfaces.textPrimary
        ),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, surfaces.accent), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f)
    ) {
        Column(Modifier.padding(28.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("TIDE TABLE / NOW", style = MaterialTheme.typography.labelMedium, color = surfaces.accent)
            Text(title, style = MaterialTheme.typography.displaySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(summary, style = MaterialTheme.typography.bodyLarge, color = surfaces.textSecondary, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Text(actionLabel, style = MaterialTheme.typography.titleMedium, color = surfaces.accent)
        }
    }
}

@Composable
private fun BlueOceanRoutePortals(onNavigate: (String) -> Unit) {
    val routes = listOf(
        "LIVE ROUTES" to Routes.LIVE_TV,
        "GUIDE TIDE" to Routes.EPG,
        "FILM CURRENT" to Routes.MOVIES,
        "SERIES CURRENT" to Routes.SERIES,
        "HARBOUR SETTINGS" to Routes.SETTINGS
    )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        routes.forEachIndexed { index, (label, route) ->
            BlueOceanRoutePortal(index, label, { onNavigate(route) }, Modifier.weight(1f))
        }
    }
}

@Composable
private fun BlueOceanRoutePortal(index: Int, label: String, onClick: () -> Unit, modifier: Modifier) {
    val surfaces = LocalThemePresentation.current.surfaces
    val shape = RoundedCornerShape(if (index % 2 == 0) 20.dp else 12.dp)
    TvClickableSurface(
        onClick = onClick,
        modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (index == 0) surfaces.selectedAccent else surfaces.browseContent,
            focusedContainerColor = surfaces.focusedSurface,
            contentColor = surfaces.textPrimary,
            focusedContentColor = surfaces.textPrimary
        ),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, surfaces.accent), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text((index + 1).toString().padStart(2, '0'), style = MaterialTheme.typography.labelSmall, color = surfaces.accent)
            Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun BlueOceanHistoryCurrent(entries: List<PlaybackHistory>, onClick: (PlaybackHistory) -> Unit) {
    BlueOceanCurrentTitle("RESUME CURRENT", "Unfinished stories on your route")
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        entries.take(8).forEach { entry ->
            BlueOceanCurrentRow(entry.title, "CONTINUE", { onClick(entry) })
        }
    }
}

@Composable
private fun BlueOceanChannelCurrent(title: String, channels: List<Channel>, recording: Set<Long>, scheduled: Set<Long>, onClick: (Channel) -> Unit) {
    BlueOceanCurrentTitle(title, "Live stations in this current")
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        channels.take(10).forEach { channel ->
            val state = when {
                channel.id in recording -> "RECORDING"
                channel.id in scheduled -> "SCHEDULED"
                else -> channel.currentProgram?.title ?: "LIVE NOW"
            }
            BlueOceanCurrentRow("${channel.number.toString().padStart(3, '0')}  ${channel.name}", state, { onClick(channel) })
        }
    }
}

@Composable
private fun BlueOceanMovieCurrent(movies: List<Movie>, onClick: (Movie) -> Unit) {
    BlueOceanCurrentTitle("FILM TIDES", "Recommended films")
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        movies.take(8).forEach { movie ->
            BlueOceanCurrentRow(movie.name, movie.genre ?: movie.year?.toString().orEmpty(), { onClick(movie) })
        }
    }
}

@Composable
private fun BlueOceanSeriesCurrent(series: List<Series>, onClick: (Series) -> Unit) {
    BlueOceanCurrentTitle("SERIES CURRENTS", "Continue through seasons")
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        series.take(8).forEach { entry ->
            BlueOceanCurrentRow(entry.name, entry.genre ?: "SERIES", { onClick(entry) })
        }
    }
}

@Composable
private fun BlueOceanCurrentTitle(title: String, subtitle: String) {
    val surfaces = LocalThemePresentation.current.surfaces
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = surfaces.accent)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = surfaces.textSecondary)
    }
}

@Composable
private fun BlueOceanCurrentRow(title: String, subtitle: String, onClick: () -> Unit) {
    val surfaces = LocalThemePresentation.current.surfaces
    val shape = RoundedCornerShape(18.dp)
    TvClickableSurface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = surfaces.browseContent,
            focusedContainerColor = surfaces.focusedSurface,
            contentColor = surfaces.textPrimary,
            focusedContentColor = surfaces.textPrimary
        ),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, surfaces.accent), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f)
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = surfaces.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text("OPEN", style = MaterialTheme.typography.labelMedium, color = surfaces.accent)
        }
    }
}
