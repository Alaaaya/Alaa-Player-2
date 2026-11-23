package com.streamvault.app.ui.themes.goldnight

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.domain.model.Episode
import com.streamvault.domain.model.ExternalRatings
import com.streamvault.domain.model.Season
import com.streamvault.domain.model.Series

/** Gold Night Series Detail: خلفية ومعلومات أعلى الشاشة، ثم عمود المواسم | عمود الحلقات. */
@Composable
internal fun GoldNightSeriesDetail(
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
    var commandEpisode by remember(series.id) { mutableStateOf<Episode?>(null) }
    Column(modifier = Modifier.fillMaxSize().background(GoldNightCanvasBrush)) {
        Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
            GoldNightArtwork(
                imageUrl = series.backdropUrl ?: series.posterUrl,
                title = series.name,
                modifier = Modifier.fillMaxSize(),
                cornerRadius = 0.dp
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(GoldNightCanvas, GoldNightCanvas.copy(alpha = 0.85f), Color.Transparent)
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, GoldNightCanvas)))
            )
            GoldNightTopStatus(modifier = Modifier.align(Alignment.TopEnd).padding(24.dp))
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(760.dp)
                    .padding(start = 36.dp, top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GoldNightButton(label = "‹ Back to Series", onClick = onBack)
                Text(
                    text = series.name,
                    style = MaterialTheme.typography.displaySmall,
                    color = GoldNightText,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = goldNightSeriesMeta(series), style = MaterialTheme.typography.titleSmall, color = GoldNightMuted)
                    Text(text = goldNightRating(series.rating), style = MaterialTheme.typography.titleSmall, color = GoldNightGold)
                    Text(
                        text = when {
                            isLoadingExternalRatings -> "Ratings syncing…"
                            externalRatings.imdb.available -> "IMDb ${externalRatings.imdb.displayValue}"
                            else -> ""
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = GoldNightMuted
                    )
                    if (unwatchedEpisodeCount > 0) {
                        Text(text = "$unwatchedEpisodeCount unwatched", style = MaterialTheme.typography.labelMedium, color = GoldNightGold)
                    }
                }
                Text(
                    text = series.plot.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoldNightText.copy(alpha = 0.85f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    resumeEpisode?.let { episode ->
                        GoldNightButton(
                            label = "Resume S${episode.seasonNumber} E${episode.episodeNumber}",
                            onClick = { onResumeClick(episode) },
                            primary = true,
                            icon = Icons.Default.PlayArrow
                        )
                        GoldNightButton(label = if (isCasting) "Casting" else "Cast", onClick = onCastResumeEpisode)
                    }
                    GoldNightButton(
                        label = if (series.isFavorite) "In Favorites" else "Add to Favorites",
                        onClick = onToggleFavorite,
                        primary = resumeEpisode == null
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 36.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(modifier = Modifier.width(280.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                GoldNightColumnTitle(title = "Seasons", trailing = series.seasons.size.takeIf { it > 0 }?.toString())
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (series.seasons.isEmpty()) {
                        item("gn_no_seasons") { GoldNightStatePanel("No seasons", "Season information is not available.") }
                    }
                    items(series.seasons, key = { "gn_season_${it.seasonNumber}" }) { season ->
                        val selected = season.seasonNumber == selectedSeason?.seasonNumber
                        GoldNightCard(
                            onClick = { onSeasonSelected(season) },
                            selected = selected,
                            containerColor = Color.Transparent,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = season.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (selected) GoldNightGold else GoldNightText,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val count = season.episodeCount.takeIf { it > 0 } ?: season.episodes.size
                                if (count > 0) Text(text = "$count ep", style = MaterialTheme.typography.labelSmall, color = GoldNightMuted)
                            }
                        }
                    }
                    if (series.variants.size > 1) {
                        item("gn_series_versions") {
                            Text(text = "Versions", style = MaterialTheme.typography.titleSmall, color = GoldNightMuted, modifier = Modifier.padding(top = 10.dp))
                        }
                        items(series.variants, key = { "gn_series_variant_${it.rawSeriesId}" }) { variant ->
                            GoldNightCard(onClick = { onSelectVariant(variant.rawSeriesId) }, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = variant.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
            GoldNightColumnDivider()
            Column(modifier = Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                GoldNightColumnTitle(title = selectedSeason?.let { "Episodes · ${it.name}" } ?: "Episodes", trailing = "Hold OK for options")
                commandEpisode?.let { episode ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(GoldNightPanel)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "E${episode.episodeNumber} · ${episode.title}",
                            style = MaterialTheme.typography.titleSmall,
                            color = GoldNightGold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            GoldNightButton(label = "Copy link", onClick = { onCopyEpisodeUrl(episode) })
                            GoldNightButton(label = "Download", onClick = { onDownloadEpisode(episode) })
                            GoldNightButton(label = if (isCasting) "Casting" else "Cast", onClick = { onCastEpisode(episode) })
                            GoldNightButton(label = "Close", onClick = { commandEpisode = null })
                        }
                    }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val episodes = selectedSeason?.episodes.orEmpty()
                    if (selectedSeason == null) {
                        item("gn_pick_season") { GoldNightStatePanel("Choose a season", "Select a season to list its episodes.") }
                    } else if (episodes.isEmpty()) {
                        item("gn_no_episodes") { GoldNightStatePanel("No episodes", "No episodes are available for this season.") }
                    }
                    items(episodes, key = { "gn_episode_${it.id}" }) { episode ->
                        GoldNightEpisodeRow(
                            episode = episode,
                            isResume = episode.id == resumeEpisode?.id,
                            onClick = { onEpisodeClick(episode) },
                            onLongClick = { commandEpisode = episode }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GoldNightEpisodeRow(
    episode: Episode,
    isResume: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GoldNightCard(onClick = onClick, onLongClick = onLongClick, selected = isResume, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = episode.episodeNumber.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = if (isResume) GoldNightGold else GoldNightMuted,
                modifier = Modifier.width(32.dp)
            )
            GoldNightArtwork(imageUrl = episode.coverUrl, title = episode.title, modifier = Modifier.width(120.dp).height(68.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = episode.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                val meta = listOfNotNull(
                    episode.duration?.takeIf { it.isNotBlank() }
                        ?: episode.durationSeconds.takeIf { it > 0 }?.let { goldNightDuration(it * 1000L) },
                    episode.releaseDate?.takeIf { it.isNotBlank() }
                ).joinToString(" • ")
                if (meta.isNotBlank()) Text(text = meta, style = MaterialTheme.typography.labelSmall, color = GoldNightMuted)
                if (!episode.plot.isNullOrBlank()) {
                    Text(
                        text = episode.plot.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = GoldNightMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (episode.watchProgress > 0 && episode.durationSeconds > 0) {
                    GoldNightProgressBar(
                        progress = episode.watchProgress.toFloat() / (episode.durationSeconds * 1000f),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
