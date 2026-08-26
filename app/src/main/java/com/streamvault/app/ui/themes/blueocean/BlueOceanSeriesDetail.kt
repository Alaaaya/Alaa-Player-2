package com.streamvault.app.ui.themes.blueocean

/** Style contract: Blue Ocean seasons and episodes use a vertical voyage ledger with a docked episode command chart. */

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.app.ui.theme.LocalThemePresentation
import com.streamvault.domain.model.Episode
import com.streamvault.domain.model.ExternalRatings
import com.streamvault.domain.model.Season
import com.streamvault.domain.model.Series

@Composable
internal fun BlueOceanSeriesDetail(series: Series, selectedSeason: Season?, resumeEpisode: Episode?, unwatchedEpisodeCount: Int, isCasting: Boolean, externalRatings: ExternalRatings, isLoadingExternalRatings: Boolean, onToggleFavorite: () -> Unit, onSelectVariant: (Long) -> Unit, onSeasonSelected: (Season) -> Unit, onEpisodeClick: (Episode) -> Unit, onResumeClick: (Episode) -> Unit, onCopyEpisodeUrl: (Episode) -> Unit, onDownloadEpisode: (Episode) -> Unit, onCastResumeEpisode: () -> Unit, onCastEpisode: (Episode) -> Unit, onBack: () -> Unit) {
    var dockedEpisode by remember(series.id) { mutableStateOf<Episode?>(null) }
    val p = LocalThemePresentation.current
    val s = p.surfaces
    LazyColumn(Modifier.fillMaxSize().background(s.canvas).padding(30.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item("blue_ocean_series_back") { BlueOceanSeriesAction("← RETURN TO ESTUARY", onBack) }
        item("blue_ocean_series_file") { Column(Modifier.fillMaxWidth().background(s.browseContent, androidx.compose.foundation.shape.RoundedCornerShape(32.dp)).padding(28.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) { Text("SERIES VOYAGE · ${selectedSeason?.name ?: "ALL SEASONS"}", style = MaterialTheme.typography.labelMedium, color = s.accent); Text(series.name, style = MaterialTheme.typography.displayMedium, color = s.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis); Text(series.plot.orEmpty(), style = MaterialTheme.typography.bodyLarge, color = s.textPrimary, maxLines = 6, overflow = TextOverflow.Ellipsis); Text("$unwatchedEpisodeCount UNWATCHED · ${if (isLoadingExternalRatings) "RATINGS TIDE" else if (externalRatings.imdb.available) "RATINGS READY" else "RATINGS UNAVAILABLE"}", style = MaterialTheme.typography.labelMedium, color = s.textSecondary); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { BlueOceanSeriesAction(if (series.isFavorite) "ANCHORED" else "ANCHOR", onToggleFavorite, Modifier.weight(1f), true); resumeEpisode?.let { episode -> BlueOceanSeriesAction("RESUME S${episode.seasonNumber} E${episode.episodeNumber}", { onResumeClick(episode) }, Modifier.weight(1f)) }; resumeEpisode?.let { BlueOceanSeriesAction(if (isCasting) "CAST ACTIVE" else "CAST RESUME", onCastResumeEpisode, Modifier.weight(1f)) } } } }
        if (series.variants.size > 1) item("blue_ocean_series_variants") { Column(verticalArrangement = Arrangement.spacedBy(7.dp)) { Text("ROUTE VARIANTS", style = MaterialTheme.typography.titleLarge); LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(series.variants, key = { it.rawSeriesId }) { variant -> BlueOceanSeriesAction(variant.label, { onSelectVariant(variant.rawSeriesId) }) } } } }
        if (series.seasons.isNotEmpty()) item("blue_ocean_series_seasons") { Column(verticalArrangement = Arrangement.spacedBy(7.dp)) { Text("SEASON CHANNELS", style = MaterialTheme.typography.titleLarge); LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(series.seasons, key = { it.seasonNumber }) { season -> BlueOceanSeriesAction(season.name, { onSeasonSelected(season) }, selected = season.seasonNumber == selectedSeason?.seasonNumber) } } } }
        dockedEpisode?.let { episode -> item("blue_ocean_episode_dock") { Column(Modifier.fillMaxWidth().background(s.browseContent, androidx.compose.foundation.shape.RoundedCornerShape(22.dp)).padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("EPISODE DOCK · E${episode.episodeNumber}", style = MaterialTheme.typography.titleMedium, color = s.accent); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { BlueOceanSeriesAction("COPY ROUTE", { onCopyEpisodeUrl(episode) }, Modifier.weight(1f)); BlueOceanSeriesAction("DOWNLOAD", { onDownloadEpisode(episode) }, Modifier.weight(1f)); BlueOceanSeriesAction(if (isCasting) "CAST ACTIVE" else "CAST", { onCastEpisode(episode) }, Modifier.weight(1f)) }; BlueOceanSeriesAction("CLOSE DOCK", { dockedEpisode = null }) } } }
        selectedSeason?.let { season -> item("blue_ocean_episode_head") { Text("EPISODE WAKE · HOLD FOR DOCK", style = MaterialTheme.typography.titleLarge, color = s.textPrimary) }; if (season.episodes.isEmpty()) item("blue_ocean_episode_empty") { BlueOceanSeriesState("NO EPISODES", "No episodes are available on this season route.") }; items(season.episodes, key = { it.id }) { episode -> BlueOceanSeriesAction("E${episode.episodeNumber}  ${episode.title}", { onEpisodeClick(episode) }, supporting = episode.plot.orEmpty(), onLongClick = { dockedEpisode = episode }) } }
    }
}

@Composable
private fun BlueOceanSeriesAction(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, primary: Boolean = false, selected: Boolean = false, supporting: String = "", onLongClick: (() -> Unit)? = null) { val p = LocalThemePresentation.current; val s = p.surfaces; val shape = androidx.compose.foundation.shape.RoundedCornerShape(if (primary) 23.dp else 16.dp); TvClickableSurface(onClick = onClick, onLongClick = onLongClick, modifier = modifier.fillMaxWidth(), shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = if (primary || selected) s.selectedAccent else s.browseContent, focusedContainerColor = s.focusedSurface, contentColor = s.textPrimary, focusedContentColor = s.textPrimary), border = ClickableSurfaceDefaults.border(border = Border(border = BorderStroke(1.dp, if (selected) s.accent else s.textSecondary.copy(alpha = .25f)), shape = shape), focusedBorder = Border(border = BorderStroke(2.dp, s.accent), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f)) { Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) { Text(label, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis); if (supporting.isNotBlank()) Text(supporting, style = MaterialTheme.typography.bodySmall, color = s.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis) } } }

@Composable
private fun BlueOceanSeriesState(title: String, subtitle: String) { val s = LocalThemePresentation.current.surfaces; Column(Modifier.fillMaxWidth().background(s.browseContent, androidx.compose.foundation.shape.RoundedCornerShape(22.dp)).padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text(title, style = MaterialTheme.typography.titleLarge, color = s.textPrimary); Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = s.textSecondary) } }
