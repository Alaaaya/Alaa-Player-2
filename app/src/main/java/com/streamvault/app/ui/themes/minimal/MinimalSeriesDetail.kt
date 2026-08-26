package com.streamvault.app.ui.themes.minimal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.domain.model.Episode
import com.streamvault.domain.model.ExternalRatings
import com.streamvault.domain.model.Season
import com.streamvault.domain.model.Series

@Composable
internal fun MinimalSeriesDetail(
    series: Series, selectedSeason: Season?, resumeEpisode: Episode?, unwatchedEpisodeCount: Int, isCasting: Boolean,
    externalRatings: ExternalRatings, isLoadingExternalRatings: Boolean, onToggleFavorite: () -> Unit,
    onSelectVariant: (Long) -> Unit, onSeasonSelected: (Season) -> Unit, onEpisodeClick: (Episode) -> Unit,
    onResumeClick: (Episode) -> Unit, onCopyEpisodeUrl: (Episode) -> Unit, onDownloadEpisode: (Episode) -> Unit,
    onCastResumeEpisode: () -> Unit, onCastEpisode: (Episode) -> Unit, onBack: () -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize().background(MinimalCanvas), contentPadding = PaddingValues(38.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        item { MinimalSeriesAction("← Back", onBack) }
        item { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("SERIES / ${selectedSeason?.name ?: "ALL SEASONS"}", style = MaterialTheme.typography.labelLarge, color = MinimalMuted); Text(series.name, style = MaterialTheme.typography.displayMedium, color = MinimalText); Text(series.plot.orEmpty(), style = MaterialTheme.typography.bodyLarge, color = MinimalText, maxLines = 5, overflow = TextOverflow.Ellipsis); Text("$unwatchedEpisodeCount unwatched", color = MinimalMuted) } }
        item { MinimalSeriesAction(if (series.isFavorite) "Saved" else "Save", onToggleFavorite) }
        resumeEpisode?.let { episode -> item { MinimalSeriesAction("Resume S${episode.seasonNumber} E${episode.episodeNumber}") { onResumeClick(episode) } } }
        if (series.variants.size > 1) { item { Text("VERSIONS", style = MaterialTheme.typography.labelLarge, color = MinimalMuted) }; items(series.variants, key = { it.rawSeriesId }) { variant -> MinimalSeriesAction(variant.label) { onSelectVariant(variant.rawSeriesId) } } }
        if (series.seasons.isNotEmpty()) { item { Text("SEASONS", style = MaterialTheme.typography.labelLarge, color = MinimalMuted) }; items(series.seasons, key = { it.seasonNumber }) { season -> MinimalSeriesAction(season.name) { onSeasonSelected(season) } } }
        selectedSeason?.let { season -> item { Text("EPISODES", style = MaterialTheme.typography.labelLarge, color = MinimalMuted) }; items(season.episodes, key = { it.id }) { episode -> MinimalSeriesAction("E${episode.episodeNumber}  ${episode.title}") { onEpisodeClick(episode) } } }
    }
}

@Composable private fun MinimalSeriesAction(label: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(0.dp)
    TvClickableSurface(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = Color.Transparent, focusedContainerColor = MinimalPaper, contentColor = MinimalText, focusedContentColor = MinimalText), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(1.dp, MinimalFocus), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = MinimalFocusedScale)) { Text(label, Modifier.padding(horizontal = 12.dp, vertical = 11.dp), style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis) }
}
