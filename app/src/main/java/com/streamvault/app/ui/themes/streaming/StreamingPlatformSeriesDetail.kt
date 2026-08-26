package com.streamvault.app.ui.themes.streaming

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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.streamvault.domain.model.Episode
import com.streamvault.domain.model.ExternalRatings
import com.streamvault.domain.model.Season
import com.streamvault.domain.model.Series

/** تفاصيل المسلسلات في Streaming Platform: Hero وseasons shelf وقائمة حلقات مع command tray. */
@Composable
internal fun StreamingPlatformSeriesDetail(
    series: Series, selectedSeason: Season?, resumeEpisode: Episode?, unwatchedEpisodeCount: Int, isCasting: Boolean,
    externalRatings: ExternalRatings, isLoadingExternalRatings: Boolean,
    onToggleFavorite: () -> Unit, onSelectVariant: (Long) -> Unit, onSeasonSelected: (Season) -> Unit,
    onEpisodeClick: (Episode) -> Unit, onResumeClick: (Episode) -> Unit, onCopyEpisodeUrl: (Episode) -> Unit,
    onDownloadEpisode: (Episode) -> Unit, onCastResumeEpisode: () -> Unit, onCastEpisode: (Episode) -> Unit, onBack: () -> Unit
) {
    var commandEpisode by remember(series.id) { mutableStateOf<Episode?>(null) }
    LazyColumn(modifier = Modifier.fillMaxSize().background(StreamingCanvas).padding(horizontal = 38.dp, vertical = 28.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item("streaming_series_back") { StreamingSeriesDetailButton("← BACK TO SERIES", onBack) }
        item("streaming_series_hero") {
            Column(Modifier.fillMaxWidth().background(StreamingPanel, RoundedCornerShape(24.dp)).padding(30.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("FEATURED SERIES / ${selectedSeason?.name ?: "ALL SEASONS"}", style = MaterialTheme.typography.labelLarge, color = StreamingAccent)
                Text(series.name, style = MaterialTheme.typography.displayMedium, color = StreamingText, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(series.plot.orEmpty(), style = MaterialTheme.typography.bodyLarge, color = StreamingText, maxLines = 6, overflow = TextOverflow.Ellipsis)
                Text("$unwatchedEpisodeCount UNWATCHED · ${if (isLoadingExternalRatings) "RATINGS SYNCING" else if (externalRatings.imdb.available) "RATINGS AVAILABLE" else "RATINGS UNAVAILABLE"}", style = MaterialTheme.typography.labelMedium, color = StreamingMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StreamingSeriesDetailButton(if (series.isFavorite) "SAVED" else "SAVE", onToggleFavorite, Modifier.weight(1f), primary = true)
                    resumeEpisode?.let { episode -> StreamingSeriesDetailButton("RESUME S${episode.seasonNumber} E${episode.episodeNumber}", { onResumeClick(episode) }, Modifier.weight(1f)) }
                    resumeEpisode?.let { StreamingSeriesDetailButton(if (isCasting) "CAST ACTIVE" else "CAST RESUME", onCastResumeEpisode, Modifier.weight(1f)) }
                }
            }
        }
        if (series.variants.size > 1) item("streaming_series_variants") { Column(verticalArrangement = Arrangement.spacedBy(9.dp)) { Text("AVAILABLE VERSIONS", style = MaterialTheme.typography.titleLarge, color = StreamingText); LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) { items(series.variants, key = { it.rawSeriesId }) { variant -> StreamingSeriesDetailButton(variant.label, { onSelectVariant(variant.rawSeriesId) }) } } } }
        if (series.seasons.isNotEmpty()) item("streaming_series_seasons") { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { Text("SEASONS", style = MaterialTheme.typography.titleLarge, color = StreamingText); LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) { items(series.seasons, key = { it.seasonNumber }) { season -> StreamingSeriesDetailButton(season.name, { onSeasonSelected(season) }, selected = season.seasonNumber == selectedSeason?.seasonNumber) } } } }
        commandEpisode?.let { episode -> item("streaming_series_episode_commands") { Column(Modifier.fillMaxWidth().background(StreamingPanel, RoundedCornerShape(16.dp)).padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Text("EPISODE COMMANDS / E${episode.episodeNumber}", style = MaterialTheme.typography.titleMedium, color = StreamingText); Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { StreamingSeriesDetailButton("COPY URL", { onCopyEpisodeUrl(episode) }, Modifier.weight(1f)); StreamingSeriesDetailButton("DOWNLOAD", { onDownloadEpisode(episode) }, Modifier.weight(1f)); StreamingSeriesDetailButton(if (isCasting) "CAST ACTIVE" else "CAST", { onCastEpisode(episode) }, Modifier.weight(1f)) }; StreamingSeriesDetailButton("CLOSE COMMANDS", { commandEpisode = null }) } } }
        selectedSeason?.let { season ->
            item("streaming_series_episodes_heading") { Text("EPISODES / HOLD FOR COMMANDS", style = MaterialTheme.typography.titleLarge, color = StreamingText) }
            if (season.episodes.isEmpty()) item("streaming_series_no_episodes") { StreamingSeriesDetailState("NO EPISODES", "No episodes are available for the selected season.") }
            items(season.episodes, key = { it.id }) { episode -> StreamingSeriesDetailButton("E${episode.episodeNumber}  ${episode.title}", { onEpisodeClick(episode) }, Modifier.fillMaxWidth(), supporting = episode.plot.orEmpty(), onLongClick = { commandEpisode = episode }) }
        }
    }
}

@Composable private fun StreamingSeriesDetailButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, primary: Boolean = false, selected: Boolean = false, supporting: String = "", onLongClick: (() -> Unit)? = null) { val shape = RoundedCornerShape(14.dp); TvClickableSurface(onClick = onClick, onLongClick = onLongClick, modifier = modifier, shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = if (primary || selected) StreamingPanelFocused else StreamingPanel, focusedContainerColor = StreamingPanelFocused, contentColor = StreamingText, focusedContentColor = StreamingText), border = ClickableSurfaceDefaults.border(border = Border(border = BorderStroke(1.dp, if (selected) StreamingAccent else StreamingPanel), shape = shape), focusedBorder = Border(border = BorderStroke(2.dp, StreamingFocus), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.03f)) { Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) { Text(label, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis); if (supporting.isNotBlank()) Text(supporting, style = MaterialTheme.typography.bodySmall, color = StreamingMuted, maxLines = 1, overflow = TextOverflow.Ellipsis) } } }
@Composable private fun StreamingSeriesDetailState(title: String, subtitle: String) = Column(Modifier.fillMaxWidth().background(StreamingPanel, RoundedCornerShape(16.dp)).padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text(title, style = MaterialTheme.typography.titleLarge, color = StreamingText); Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = StreamingMuted) }
