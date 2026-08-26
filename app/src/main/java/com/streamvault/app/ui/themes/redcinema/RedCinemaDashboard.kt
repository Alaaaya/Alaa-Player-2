package com.streamvault.app.ui.themes.redcinema

/** Red Cinema home contract: marquee hero followed by an act-based programme, not rows of media shelves. */

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
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
internal fun RedCinemaDashboard(
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
            DashboardFeatureAction.MOVIES -> onNavigate(Routes.MOVIES)
            DashboardFeatureAction.SERIES -> onNavigate(Routes.SERIES)
            DashboardFeatureAction.CONTINUE_WATCHING -> uiState.continueWatching.firstOrNull()?.let(onContinueWatchingItemClick) ?: onNavigate(Routes.MOVIES)
        }
    }
    Column(Modifier.fillMaxSize().background(surfaces.canvas).padding(28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        RedCinemaMarqueeHome(uiState.feature.title.ifBlank { "Alaa Cinema" }, uiState.feature.summary.ifBlank { "The programme begins whenever you are ready." }, uiState.feature.actionLabel.ifBlank { "OPEN PROGRAMME" }, featureAction)
        Text("TONIGHT'S ACTS", style = MaterialTheme.typography.titleLarge, color = surfaces.accent)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("ACT I\nLIVE" to Routes.LIVE_TV, "ACT II\nGUIDE" to Routes.EPG, "ACT III\nFILMS" to Routes.MOVIES, "ACT IV\nSERIES" to Routes.SERIES, "CURTAIN\nSETTINGS" to Routes.SETTINGS).forEachIndexed { index, (label, route) ->
                RedCinemaActTicket((index + 1).toString().padStart(2, '0'), label, { onNavigate(route) }, Modifier.weight(1f))
            }
        }
        if (uiState.continueWatching.isNotEmpty()) {
            RedCinemaHomeLedger("ENCORE", uiState.continueWatching.take(4).map { it.title })
        }
        if (uiState.favoriteChannels.isNotEmpty()) {
            RedCinemaHomeLedger("SAVED SEATS", uiState.favoriteChannels.take(4).map { it.name })
        }
        if (uiState.recommendedMovies.isNotEmpty()) {
            RedCinemaHomeLedger("FEATURES", uiState.recommendedMovies.take(4).map { it.name })
        }
    }
}

@Composable
private fun RedCinemaMarqueeHome(title: String, summary: String, action: String, onClick: () -> Unit) { val s = LocalThemePresentation.current.surfaces; val shape = RoundedCornerShape(3.dp); TvClickableSurface(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = s.browseContent, focusedContainerColor = s.focusedSurface, contentColor = s.textPrimary, focusedContentColor = s.textPrimary), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, s.accent), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.015f)) { Column(Modifier.padding(26.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) { Text("NOW SHOWING", style = MaterialTheme.typography.labelMedium, color = s.accent); Text(title, style = MaterialTheme.typography.displaySmall, maxLines = 2, overflow = TextOverflow.Ellipsis); Text(summary, style = MaterialTheme.typography.bodyLarge, color = s.textSecondary, maxLines = 3, overflow = TextOverflow.Ellipsis); Text(action, style = MaterialTheme.typography.titleMedium, color = s.accent) } } }

@Composable
private fun RedCinemaActTicket(number: String, label: String, onClick: () -> Unit, modifier: Modifier) { val s = LocalThemePresentation.current.surfaces; val shape = RoundedCornerShape(2.dp); TvClickableSurface(onClick = onClick, modifier = modifier, shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = s.browseContent, focusedContainerColor = s.focusedSurface, contentColor = s.textPrimary, focusedContentColor = s.textPrimary), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, s.accent), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.018f)) { Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) { Text(number, style = MaterialTheme.typography.labelMedium, color = s.accent); Text(label, style = MaterialTheme.typography.labelLarge, maxLines = 2, overflow = TextOverflow.Ellipsis) } } }

@Composable
private fun RedCinemaHomeLedger(title: String, entries: List<String>) { val s = LocalThemePresentation.current.surfaces; Column(Modifier.fillMaxWidth().background(s.browseContent, RoundedCornerShape(2.dp)).padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) { Text(title, style = MaterialTheme.typography.titleMedium, color = s.accent); entries.forEachIndexed { index, entry -> Text("${(index + 1).toString().padStart(2, '0')}  $entry", style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis) } } }
