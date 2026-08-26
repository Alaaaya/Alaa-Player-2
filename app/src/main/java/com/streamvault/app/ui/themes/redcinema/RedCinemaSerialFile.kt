package com.streamvault.app.ui.themes.redcinema

/** Red Cinema serial detail contract: a production file with season acts, episode playbills, and a contextual ticket desk. */

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
internal fun RedCinemaSerialFile(series: Series, selectedSeason: Season?, resumeEpisode: Episode?, unwatchedEpisodeCount: Int, isCasting: Boolean, externalRatings: ExternalRatings, isLoadingExternalRatings: Boolean, onToggleFavorite: () -> Unit, onSelectVariant: (Long) -> Unit, onSeasonSelected: (Season) -> Unit, onEpisodeClick: (Episode) -> Unit, onResumeClick: (Episode) -> Unit, onCopyEpisodeUrl: (Episode) -> Unit, onDownloadEpisode: (Episode) -> Unit, onCastResumeEpisode: () -> Unit, onCastEpisode: (Episode) -> Unit, onBack: () -> Unit) {
    var selectedEpisode by remember(series.id) { mutableStateOf<Episode?>(null) }
    val s = LocalThemePresentation.current.surfaces
    LazyColumn(Modifier.fillMaxSize().background(s.canvas).padding(28.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
        item("red_cinema_serial_back") { RedCinemaSerialTicket("← RETURN TO PLAYBILL", onBack) }
        item("red_cinema_serial_file") { Column(Modifier.fillMaxWidth().background(s.browseContent, androidx.compose.foundation.shape.RoundedCornerShape(2.dp)).padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("PRODUCTION FILE · ${selectedSeason?.name ?: "ALL SEASONS"}", style = MaterialTheme.typography.labelMedium, color = s.accent); Text(series.name, style = MaterialTheme.typography.displayMedium, maxLines = 2, overflow = TextOverflow.Ellipsis); Text(series.plot.orEmpty(), style = MaterialTheme.typography.bodyLarge, color = s.textPrimary, maxLines = 6, overflow = TextOverflow.Ellipsis); Text("$unwatchedEpisodeCount UNWATCHED · ${if (isLoadingExternalRatings) "RATINGS PENDING" else if (externalRatings.imdb.available) "RATINGS READY" else "RATINGS UNAVAILABLE"}", style = MaterialTheme.typography.labelMedium, color = s.textSecondary); Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { RedCinemaSerialTicket(if (series.isFavorite) "RESERVED" else "RESERVE", onToggleFavorite, Modifier.weight(1f), true); resumeEpisode?.let { episode -> RedCinemaSerialTicket("RESUME S${episode.seasonNumber} E${episode.episodeNumber}", { onResumeClick(episode) }, Modifier.weight(1f)); RedCinemaSerialTicket(if (isCasting) "CAST ACTIVE" else "CAST RESUME", onCastResumeEpisode, Modifier.weight(1f)) } } } }
        if (series.variants.size > 1) item("red_cinema_serial_versions") { Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { Text("ALTERNATE PRINTS", style = MaterialTheme.typography.titleLarge, color = s.accent); LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { items(series.variants, key = { it.rawSeriesId }) { variant -> RedCinemaSerialTicket(variant.label, { onSelectVariant(variant.rawSeriesId) }) } } } }
        if (series.seasons.isNotEmpty()) item("red_cinema_serial_seasons") { Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { Text("SEASON ACTS", style = MaterialTheme.typography.titleLarge, color = s.accent); LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { items(series.seasons, key = { it.seasonNumber }) { season -> RedCinemaSerialTicket(season.name, { onSeasonSelected(season) }, selected = season.seasonNumber == selectedSeason?.seasonNumber) } } } }
        selectedEpisode?.let { episode -> item("red_cinema_episode_ticket_desk") { Column(Modifier.fillMaxWidth().background(s.browseContent, androidx.compose.foundation.shape.RoundedCornerShape(2.dp)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text("TICKET DESK · E${episode.episodeNumber}", style = MaterialTheme.typography.titleMedium, color = s.accent); Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { RedCinemaSerialTicket("COPY TICKET", { onCopyEpisodeUrl(episode) }, Modifier.weight(1f)); RedCinemaSerialTicket("DOWNLOAD", { onDownloadEpisode(episode) }, Modifier.weight(1f)); RedCinemaSerialTicket(if (isCasting) "CAST ACTIVE" else "CAST", { onCastEpisode(episode) }, Modifier.weight(1f)) }; RedCinemaSerialTicket("CLOSE DESK", { selectedEpisode = null }) } } }
        selectedSeason?.let { season -> item("red_cinema_episode_header") { Text("EPISODE PLAYBILL · HOLD FOR TICKET DESK", style = MaterialTheme.typography.titleLarge, color = s.accent) }; if (season.episodes.isEmpty()) item("red_cinema_episode_empty") { RedCinemaSerialState("NO EPISODES", "No episodes are available for this season.") }; items(season.episodes, key = { it.id }) { episode -> RedCinemaSerialTicket("E${episode.episodeNumber}  ${episode.title}", { onEpisodeClick(episode) }, supporting = episode.plot.orEmpty(), onLongClick = { selectedEpisode = episode }) } }
    }
}

@Composable
private fun RedCinemaSerialTicket(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, primary: Boolean = false, selected: Boolean = false, supporting: String = "", onLongClick: (() -> Unit)? = null) { val s = LocalThemePresentation.current.surfaces; val shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp); TvClickableSurface(onClick = onClick, onLongClick = onLongClick, modifier = modifier.fillMaxWidth(), shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = if (primary || selected) s.selectedAccent else s.browseContent, focusedContainerColor = s.focusedSurface, contentColor = s.textPrimary, focusedContentColor = s.textPrimary), border = ClickableSurfaceDefaults.border(border = Border(border = BorderStroke(1.dp, if (selected) s.accent else s.textSecondary.copy(alpha = .25f)), shape = shape), focusedBorder = Border(border = BorderStroke(2.dp, s.accent), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.018f)) { Column(Modifier.padding(horizontal = 14.dp, vertical = 11.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) { Text(label, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis); if (supporting.isNotBlank()) Text(supporting, style = MaterialTheme.typography.bodySmall, color = s.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis) } } }
@Composable
private fun RedCinemaSerialState(title: String, subtitle: String) { val s = LocalThemePresentation.current.surfaces; Column(Modifier.fillMaxWidth().background(s.browseContent, androidx.compose.foundation.shape.RoundedCornerShape(2.dp)).padding(20.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) { Text(title, style = MaterialTheme.typography.titleLarge); Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = s.textSecondary) } }
