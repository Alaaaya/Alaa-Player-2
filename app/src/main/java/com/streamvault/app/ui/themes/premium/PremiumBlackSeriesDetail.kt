package com.streamvault.app.ui.themes.premium

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

@Composable
internal fun PremiumBlackSeriesDetail(series: Series, selectedSeason: Season?, resumeEpisode: Episode?, unwatchedEpisodeCount: Int, isCasting: Boolean, externalRatings: ExternalRatings, isLoadingExternalRatings: Boolean, onToggleFavorite: () -> Unit, onSelectVariant: (Long) -> Unit, onSeasonSelected: (Season) -> Unit, onEpisodeClick: (Episode) -> Unit, onResumeClick: (Episode) -> Unit, onCopyEpisodeUrl: (Episode) -> Unit, onDownloadEpisode: (Episode) -> Unit, onCastResumeEpisode: () -> Unit, onCastEpisode: (Episode) -> Unit, onBack: () -> Unit) {
    var commandEpisode by remember(series.id) { mutableStateOf<Episode?>(null) }
    LazyColumn(Modifier.fillMaxSize().background(PremiumCanvas).padding(32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item("premium_series_back") { PremiumSeriesDetailButton("← BACK TO SERIES", onBack) }
        item("premium_series_hero") { Column(Modifier.fillMaxWidth().background(PremiumPanel, RoundedCornerShape(12.dp)).padding(30.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Text("PREMIUM SERIES / ${selectedSeason?.name ?: "ALL SEASONS"}", style = MaterialTheme.typography.labelLarge, color = PremiumGold); Text(series.name, style = MaterialTheme.typography.displayMedium, color = PremiumText, maxLines = 2, overflow = TextOverflow.Ellipsis); Text(series.plot.orEmpty(), style = MaterialTheme.typography.bodyLarge, color = PremiumText, maxLines = 6, overflow = TextOverflow.Ellipsis); Text("$unwatchedEpisodeCount UNWATCHED · ${if (isLoadingExternalRatings) "RATINGS SYNCING" else if (externalRatings.imdb.available) "RATINGS AVAILABLE" else "RATINGS UNAVAILABLE"}", style = MaterialTheme.typography.labelMedium, color = PremiumMuted); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { PremiumSeriesDetailButton(if (series.isFavorite) "SAVED" else "SAVE", onToggleFavorite, Modifier.weight(1f), primary = true); resumeEpisode?.let { episode -> PremiumSeriesDetailButton("RESUME S${episode.seasonNumber} E${episode.episodeNumber}", { onResumeClick(episode) }, Modifier.weight(1f)) }; resumeEpisode?.let { PremiumSeriesDetailButton(if (isCasting) "CAST ACTIVE" else "CAST RESUME", onCastResumeEpisode, Modifier.weight(1f)) } } } }
        if (series.variants.size > 1) item("premium_series_variants") { PremiumDetailShelf("AVAILABLE VERSIONS") { LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(series.variants, key = { it.rawSeriesId }) { variant -> PremiumSeriesDetailButton(variant.label, { onSelectVariant(variant.rawSeriesId) }) } } } }
        if (series.seasons.isNotEmpty()) item("premium_series_seasons") { PremiumDetailShelf("SEASONS") { LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(series.seasons, key = { it.seasonNumber }) { season -> PremiumSeriesDetailButton(season.name, { onSeasonSelected(season) }, selected = season.seasonNumber == selectedSeason?.seasonNumber) } } } }
        commandEpisode?.let { episode -> item("premium_series_commands") { Column(Modifier.fillMaxWidth().background(PremiumPanel, RoundedCornerShape(8.dp)).padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) { Text("EPISODE COMMANDS / E${episode.episodeNumber}", style = MaterialTheme.typography.titleMedium, color = PremiumText); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { PremiumSeriesDetailButton("COPY URL", { onCopyEpisodeUrl(episode) }, Modifier.weight(1f)); PremiumSeriesDetailButton("DOWNLOAD", { onDownloadEpisode(episode) }, Modifier.weight(1f)); PremiumSeriesDetailButton(if (isCasting) "CAST ACTIVE" else "CAST", { onCastEpisode(episode) }, Modifier.weight(1f)) }; PremiumSeriesDetailButton("CLOSE", { commandEpisode = null }) } } }
        selectedSeason?.let { season -> item("premium_series_episodes_head") { Text("EPISODES / HOLD FOR COMMANDS", style = MaterialTheme.typography.titleLarge, color = PremiumText) }; if (season.episodes.isEmpty()) item("premium_series_no_episodes") { PremiumSeriesState("NO EPISODES", "No episodes are available for the selected season.") }; items(season.episodes, key = { it.id }) { episode -> PremiumSeriesDetailButton("E${episode.episodeNumber}  ${episode.title}", { onEpisodeClick(episode) }, Modifier.fillMaxWidth(), supporting = episode.plot.orEmpty(), onLongClick = { commandEpisode = episode }) } }
    }
}
@Composable private fun PremiumDetailShelf(title: String, content: @Composable () -> Unit) = Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(title, style = MaterialTheme.typography.titleLarge, color = PremiumText); content() }
@Composable private fun PremiumSeriesDetailButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, primary: Boolean = false, selected: Boolean = false, supporting: String = "", onLongClick: (() -> Unit)? = null) { val shape = RoundedCornerShape(7.dp); TvClickableSurface(onClick = onClick, onLongClick = onLongClick, modifier = modifier, shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = if (primary || selected) PremiumPanelFocused else PremiumPanel, focusedContainerColor = PremiumPanelFocused, contentColor = PremiumText, focusedContentColor = PremiumText), border = ClickableSurfaceDefaults.border(border = Border(border = BorderStroke(1.dp, if (selected) PremiumGold else PremiumMetal), shape = shape), focusedBorder = Border(border = BorderStroke(2.dp, PremiumFocus), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f)) { Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) { Text(label, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis); if (supporting.isNotBlank()) Text(supporting, style = MaterialTheme.typography.bodySmall, color = PremiumMuted, maxLines = 1, overflow = TextOverflow.Ellipsis) } } }
@Composable private fun PremiumSeriesState(title: String, subtitle: String) = Column(Modifier.fillMaxWidth().background(PremiumPanel, RoundedCornerShape(8.dp)).padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text(title, style = MaterialTheme.typography.titleLarge, color = PremiumText); Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = PremiumMuted) }
