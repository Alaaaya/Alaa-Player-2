package com.streamvault.app.ui.themes.neon

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.streamvault.app.ui.design.requestFocusSafely
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.app.util.formatPositionMs
import com.streamvault.domain.model.Episode
import com.streamvault.domain.model.ExternalRatings
import com.streamvault.domain.model.Season
import com.streamvault.domain.model.Series

/** Neon Future's series detail is a presentation-only season signal monitor. */
@Composable
internal fun NeonFutureSeriesDetail(
    series: Series,
    selectedSeason: Season?,
    resumeEpisode: Episode?,
    unwatchedEpisodeCount: Int,
    isCasting: Boolean,
    externalRatings: ExternalRatings,
    isLoadingExternalRatings: Boolean,
    onToggleFavorite: () -> Unit,
    onSelectVariant: (Long) -> Unit,
    onSeasonSelected: (Season) -> Unit,
    onEpisodeClick: (Episode) -> Unit,
    onResumeClick: (Episode) -> Unit,
    onCopyEpisodeUrl: (Episode) -> Unit,
    onDownloadEpisode: (Episode) -> Unit,
    onCastResumeEpisode: () -> Unit,
    onCastEpisode: (Episode) -> Unit,
    onBack: () -> Unit
) {
    val primaryFocusRequester = remember { FocusRequester() }
    var episodeLimit by remember(selectedSeason?.seasonNumber) { mutableIntStateOf(100) }
    val visibleEpisodes = selectedSeason?.episodes.orEmpty().take(episodeLimit)
    LaunchedEffect(series.id, selectedSeason?.seasonNumber) {
        primaryFocusRequester.requestFocusSafely(tag = "NeonFutureSeriesDetail", target = "Primary series action")
    }

    Box(modifier = Modifier.fillMaxSize().background(NeonCanvas)) {
        AsyncImage(series.backdropUrl ?: series.posterUrl, null, Modifier.fillMaxWidth().height(398.dp), contentScale = ContentScale.Crop)
        Box(modifier = Modifier.fillMaxWidth().height(426.dp).background(Brush.verticalGradient(listOf(NeonCanvas.copy(alpha = .18f), NeonCanvas.copy(alpha = .76f), NeonCanvas))))
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 34.dp, top = 28.dp, end = 34.dp, bottom = 34.dp),
            verticalArrangement = Arrangement.spacedBy(21.dp)
        ) {
            item("neon_series_detail_back") { NeonSeriesAction("← SERIES FEED", NeonCyan, onBack) }
            item("neon_series_detail_hero") {
                Row(horizontalArrangement = Arrangement.spacedBy(22.dp), verticalAlignment = Alignment.Bottom) {
                    AsyncImage(series.posterUrl ?: series.backdropUrl, series.name, Modifier.width(204.dp).height(306.dp).clip(RoundedCornerShape(10.dp)), contentScale = ContentScale.Crop)
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                        Text("SERIES NODE / ${selectedSeason?.name ?: "ALL SEASONS"}", style = MaterialTheme.typography.labelLarge, color = NeonPink, fontWeight = FontWeight.Black)
                        Text(series.name, style = MaterialTheme.typography.displayMedium, color = NeonText, fontWeight = FontWeight.Black, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(listOfNotNull(series.releaseDate, series.genre, series.episodeRunTime).filter { it.isNotBlank() }.joinToString(" / ").ifBlank { "Series metadata node" }, style = MaterialTheme.typography.titleSmall, color = NeonCyan)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("RATING ${if (series.rating > 0f) "%.1f".format(series.rating) else "—"}", style = MaterialTheme.typography.labelMedium, color = NeonLime)
                            if (unwatchedEpisodeCount > 0) Text("$unwatchedEpisodeCount UNWATCHED", style = MaterialTheme.typography.labelMedium, color = NeonPink)
                            if (isLoadingExternalRatings) Text("RATINGS SYNC", style = MaterialTheme.typography.labelMedium, color = NeonMuted)
                            else if (externalRatings.imdb.available) Text("EXTERNAL RATINGS READY", style = MaterialTheme.typography.labelMedium, color = NeonMuted)
                        }
                        Text(series.plot?.takeIf { it.isNotBlank() } ?: "No synopsis is available from this active catalogue node.", style = MaterialTheme.typography.bodyLarge, color = NeonMuted, maxLines = 4, overflow = TextOverflow.Ellipsis)
                        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                            if (resumeEpisode != null) {
                                NeonSeriesAction(
                                    if (resumeEpisode.watchProgress > 5_000L) "RESUME S${resumeEpisode.seasonNumber} E${resumeEpisode.episodeNumber} ${formatPositionMs(resumeEpisode.watchProgress)}" else "PLAY S${resumeEpisode.seasonNumber} E${resumeEpisode.episodeNumber}",
                                    NeonLime,
                                    { onResumeClick(resumeEpisode) },
                                    Modifier.focusRequester(primaryFocusRequester)
                                )
                                NeonSeriesAction(if (isCasting) "CAST ACTIVE" else "CAST EPISODE", NeonPink, { if (!isCasting) onCastResumeEpisode() })
                            } else {
                                NeonSeriesAction("BROWSE EPISODES", NeonLime, { selectedSeason?.episodes?.firstOrNull()?.let(onEpisodeClick) }, Modifier.focusRequester(primaryFocusRequester))
                            }
                            NeonSeriesAction(if (series.isFavorite) "SAVED" else "SAVE", NeonCyan, onToggleFavorite)
                        }
                    }
                    NeonSeriesDataPanel(series, selectedSeason, modifier = Modifier.width(230.dp))
                }
            }
            if (series.variants.size > 1) item("neon_series_variants") {
                NeonSeriesSection("PROVIDER VARIANTS", "Select an available series stream node") {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(series.variants, key = { it.rawSeriesId }) { variant ->
                            NeonSeriesAction(variant.label, if (variant.rawSeriesId == (series.selectedVariantId ?: series.id)) NeonLime else NeonMuted, { onSelectVariant(variant.rawSeriesId) })
                        }
                    }
                }
            }
            if (series.seasons.isNotEmpty()) item("neon_series_seasons") {
                NeonSeriesSection("SEASON SWITCHBOARD", "Use the remote to choose the active episode feed") {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(series.seasons, key = { it.seasonNumber }) { season ->
                            NeonSeriesAction("${season.name.uppercase()} / ${season.episodeCount.takeIf { it > 0 } ?: season.episodes.size}", if (season.seasonNumber == selectedSeason?.seasonNumber) NeonPink else NeonMuted, { onSeasonSelected(season) })
                        }
                    }
                }
            }
            if (selectedSeason != null) {
                item("neon_series_episodes_heading") { NeonSeriesSection("${selectedSeason.name.uppercase()} EPISODES", "${selectedSeason.episodes.size} episode nodes from the selected provider") {} }
                items(visibleEpisodes, key = { it.id }) { episode ->
                    NeonEpisodeNode(
                        episode = episode,
                        fallbackArtworkUrl = series.posterUrl ?: series.backdropUrl,
                        isCasting = isCasting,
                        onPlay = { onEpisodeClick(episode) },
                        onCopy = { onCopyEpisodeUrl(episode) },
                        onDownload = { onDownloadEpisode(episode) },
                        onCast = { if (!isCasting) onCastEpisode(episode) }
                    )
                }
                if (visibleEpisodes.size < selectedSeason.episodes.size) item("neon_series_more") {
                    NeonSeriesAction(
                        label = "LOAD MORE ${visibleEpisodes.size}/${selectedSeason.episodes.size}",
                        tone = NeonCyan,
                        onClick = { episodeLimit = (episodeLimit + 100).coerceAtMost(selectedSeason.episodes.size) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NeonSeriesDataPanel(series: Series, selectedSeason: Season?, modifier: Modifier) {
    Column(modifier = modifier.background(NeonPanel, RoundedCornerShape(10.dp)).padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("SEASON MONITOR", style = MaterialTheme.typography.labelLarge, color = NeonCyan, fontWeight = FontWeight.Black)
        Text(selectedSeason?.name ?: "AWAITING SEASON", style = MaterialTheme.typography.titleMedium, color = NeonLime, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text("${selectedSeason?.episodes?.size ?: 0} EPISODES", style = MaterialTheme.typography.labelMedium, color = NeonPink)
        Text("NODE ${series.id}", style = MaterialTheme.typography.labelSmall, color = NeonMuted)
    }
}

@Composable
private fun NeonSeriesSection(title: String, subtitle: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = NeonText, fontWeight = FontWeight.Black)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = NeonMuted)
        content()
    }
}

@Composable
private fun NeonEpisodeNode(episode: Episode, fallbackArtworkUrl: String?, isCasting: Boolean, onPlay: () -> Unit, onCopy: () -> Unit, onDownload: () -> Unit, onCast: () -> Unit) {
    val shape = RoundedCornerShape(9.dp)
    Row(modifier = Modifier.fillMaxWidth().background(NeonPanel, shape).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
        TvClickableSurface(onClick = onPlay, modifier = Modifier.width(172.dp).height(96.dp), shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(7.dp)), colors = ClickableSurfaceDefaults.colors(containerColor = NeonCanvas, focusedContainerColor = NeonPanelRaised, contentColor = NeonText, focusedContentColor = NeonText), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, NeonLime), shape = RoundedCornerShape(7.dp)))) {
            AsyncImage(episode.coverUrl ?: fallbackArtworkUrl, episode.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("E${episode.episodeNumber.toString().padStart(2, '0')} / S${episode.seasonNumber}", style = MaterialTheme.typography.labelMedium, color = NeonCyan, fontWeight = FontWeight.Black)
            Text(episode.title, style = MaterialTheme.typography.titleMedium, color = NeonText, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(episode.plot?.takeIf { it.isNotBlank() } ?: episode.duration.orEmpty().ifBlank { "Episode metadata unavailable." }, style = MaterialTheme.typography.bodyMedium, color = NeonMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            NeonSeriesAction("PLAY", NeonLime, onPlay)
            NeonSeriesAction("DL", NeonCyan, onDownload)
            NeonSeriesAction("COPY", NeonMuted, onCopy)
            NeonSeriesAction(if (isCasting) "CASTING" else "CAST", NeonPink, onCast)
        }
    }
}

@Composable
private fun NeonSeriesAction(label: String, tone: androidx.compose.ui.graphics.Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(7.dp)
    TvClickableSurface(onClick = onClick, modifier = modifier, shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = NeonCanvas, focusedContainerColor = tone.copy(alpha = .25f), contentColor = tone, focusedContentColor = NeonText), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, tone), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.025f)) {
        Text(label, Modifier.padding(horizontal = 12.dp, vertical = 9.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
