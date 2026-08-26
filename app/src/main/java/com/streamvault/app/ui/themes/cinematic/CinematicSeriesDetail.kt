package com.streamvault.app.ui.themes.cinematic

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.streamvault.app.ui.design.requestFocusSafely
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.app.util.formatPositionMs
import com.streamvault.domain.model.Episode
import com.streamvault.domain.model.ExternalRatings
import com.streamvault.domain.model.Season
import com.streamvault.domain.model.Series

/** Presentation-only series detail for the Cinematic theme. */
@Composable
internal fun CinematicSeriesDetail(
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
    var visibleEpisodeLimit by remember(selectedSeason?.seasonNumber) { mutableIntStateOf(100) }
    val visibleEpisodes = selectedSeason?.episodes.orEmpty().take(visibleEpisodeLimit)
    LaunchedEffect(series.id, selectedSeason?.seasonNumber) {
        primaryFocusRequester.requestFocusSafely(tag = "CinematicSeriesDetail", target = "Primary series action")
    }

    Box(modifier = Modifier.fillMaxSize().background(CinematicCanvas)) {
        AsyncImage(
            model = series.backdropUrl ?: series.posterUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(510.dp),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(560.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(CinematicCanvas.copy(alpha = 0.22f), CinematicCanvas.copy(alpha = 0.8f), CinematicCanvas)
                    )
                )
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 46.dp, top = 34.dp, end = 46.dp, bottom = 42.dp),
            verticalArrangement = Arrangement.spacedBy(25.dp)
        ) {
            item(key = "cinematic_series_back") {
                CinematicSeriesAction(label = "BACK TO SERIES ROOM", tone = CinematicMuted, onClick = onBack)
            }
            item(key = "cinematic_series_hero") {
                Row(horizontalArrangement = Arrangement.spacedBy(28.dp), verticalAlignment = Alignment.Bottom) {
                    val posterShape = RoundedCornerShape(28.dp)
                    Surface(
                        modifier = Modifier.width(232.dp).height(348.dp),
                        shape = posterShape,
                        colors = SurfaceDefaults.colors(containerColor = CinematicPanel),
                        border = Border(border = BorderStroke(1.dp, Color(0xFF9DA3FF).copy(alpha = 0.65f)), shape = posterShape)
                    ) {
                        AsyncImage(
                            model = series.posterUrl ?: series.backdropUrl,
                            contentDescription = series.name,
                            modifier = Modifier.fillMaxSize().clip(posterShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "SERIES ROOM // ${selectedSeason?.name ?: "ALL SEASONS"}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF9DA3FF),
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = series.name,
                            style = MaterialTheme.typography.displayMedium,
                            color = CinematicText,
                            fontWeight = FontWeight.Black,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = listOfNotNull(series.releaseDate, series.genre, series.episodeRunTime)
                                .filter { it.isNotBlank() }
                                .joinToString("  ·  ")
                                .ifBlank { "SERIES" },
                            style = MaterialTheme.typography.titleSmall,
                            color = CinematicMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (series.rating > 0f) CinematicSeriesMarker("RATING ${"%.1f".format(series.rating)} / 10", CinematicGold)
                            if (unwatchedEpisodeCount > 0) CinematicSeriesMarker("$unwatchedEpisodeCount UNWATCHED", CinematicWine)
                        }
                        CinematicExternalRatings(externalRatings, isLoadingExternalRatings)
                        Text(
                            text = series.plot?.takeIf { it.isNotBlank() }
                                ?: "No synopsis is available from this catalogue entry.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = CinematicMuted,
                            maxLines = 5,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (resumeEpisode != null) {
                                CinematicSeriesAction(
                                    label = if (resumeEpisode.watchProgress > 5_000L) {
                                        "RESUME S${resumeEpisode.seasonNumber} E${resumeEpisode.episodeNumber} · ${formatPositionMs(resumeEpisode.watchProgress)}"
                                    } else {
                                        "PLAY S${resumeEpisode.seasonNumber} E${resumeEpisode.episodeNumber}"
                                    },
                                    tone = CinematicWine,
                                    modifier = Modifier.focusRequester(primaryFocusRequester),
                                    onClick = { onResumeClick(resumeEpisode) }
                                )
                                CinematicSeriesAction(
                                    label = if (isCasting) "CAST CONNECTING" else "CAST EPISODE",
                                    tone = Color(0xFF9DA3FF),
                                    onClick = { if (!isCasting) onCastResumeEpisode() }
                                )
                            } else {
                                CinematicSeriesAction(
                                    label = "BROWSE EPISODES",
                                    tone = CinematicWine,
                                    modifier = Modifier.focusRequester(primaryFocusRequester),
                                    onClick = { selectedSeason?.episodes?.firstOrNull()?.let(onEpisodeClick) }
                                )
                            }
                            CinematicSeriesAction(
                                label = if (series.isFavorite) "SAVED" else "SAVE SERIES",
                                tone = CinematicGold,
                                onClick = onToggleFavorite
                            )
                        }
                    }
                }
            }
            if (series.variants.size > 1) {
                item(key = "cinematic_series_versions") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CinematicSeriesSectionTitle("AVAILABLE CUTS", "Select the preferred provider variant")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(series.variants, key = { it.rawSeriesId }) { variant ->
                                CinematicSeriesAction(
                                    label = variant.label,
                                    tone = if (variant.rawSeriesId == (series.selectedVariantId ?: series.id)) CinematicWine else CinematicMuted,
                                    onClick = { onSelectVariant(variant.rawSeriesId) }
                                )
                            }
                        }
                    }
                }
            }
            if (series.seasons.isNotEmpty()) {
                item(key = "cinematic_series_seasons") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CinematicSeriesSectionTitle("SELECT A SEASON", "Navigate with the remote, then choose an episode below")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(series.seasons, key = { it.seasonNumber }) { season ->
                                CinematicSeriesAction(
                                    label = "${season.name.uppercase()} · ${season.episodeCount.takeIf { it > 0 } ?: season.episodes.size}",
                                    tone = if (season.seasonNumber == selectedSeason?.seasonNumber) Color(0xFF9DA3FF) else CinematicMuted,
                                    onClick = { onSeasonSelected(season) }
                                )
                            }
                        }
                    }
                }
            }
            if (selectedSeason != null) {
                item(key = "cinematic_series_episode_heading") {
                    CinematicSeriesSectionTitle(
                        title = "${selectedSeason.name.uppercase()} · EPISODES",
                        detail = "${selectedSeason.episodes.size} episodes from the selected provider"
                    )
                }
                items(visibleEpisodes, key = { it.id }) { episode ->
                    CinematicEpisodeCard(
                        episode = episode,
                        fallbackArtworkUrl = series.posterUrl ?: series.backdropUrl,
                        isCasting = isCasting,
                        onPlay = { onEpisodeClick(episode) },
                        onCopyUrl = { onCopyEpisodeUrl(episode) },
                        onDownload = { onDownloadEpisode(episode) },
                        onCast = { if (!isCasting) onCastEpisode(episode) }
                    )
                }
                if (visibleEpisodes.size < selectedSeason.episodes.size) {
                    item(key = "cinematic_series_more_episodes") {
                        CinematicSeriesAction(
                            label = "LOAD MORE EPISODES (${visibleEpisodes.size}/${selectedSeason.episodes.size})",
                            tone = CinematicGold,
                            onClick = { visibleEpisodeLimit = (visibleEpisodeLimit + 100).coerceAtMost(selectedSeason.episodes.size) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CinematicEpisodeCard(
    episode: Episode,
    fallbackArtworkUrl: String?,
    isCasting: Boolean,
    onPlay: () -> Unit,
    onCopyUrl: () -> Unit,
    onDownload: () -> Unit,
    onCast: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        colors = SurfaceDefaults.colors(containerColor = CinematicPanel),
        border = Border(border = BorderStroke(1.dp, CinematicMuted.copy(alpha = 0.25f)), shape = shape)
    ) {
        Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            TvClickableSurface(
                onClick = onPlay,
                modifier = Modifier.width(184.dp).height(104.dp),
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = CinematicCanvas,
                    focusedContainerColor = CinematicPanelRaised,
                    contentColor = CinematicText,
                    focusedContentColor = CinematicText
                ),
                border = ClickableSurfaceDefaults.border(
                    focusedBorder = Border(border = BorderStroke(2.dp, CinematicWine), shape = RoundedCornerShape(12.dp))
                )
            ) {
                AsyncImage(
                    model = episode.coverUrl ?: fallbackArtworkUrl,
                    contentDescription = episode.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "E${episode.episodeNumber.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.labelMedium,
                    color = CinematicGold,
                    fontWeight = FontWeight.Black
                )
                Text(text = episode.title, style = MaterialTheme.typography.titleMedium, color = CinematicText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = episode.plot?.takeIf { it.isNotBlank() } ?: episode.duration.orEmpty().ifBlank { "Episode details are unavailable." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = CinematicMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CinematicSeriesAction(label = "PLAY", tone = CinematicWine, onClick = onPlay)
                CinematicSeriesAction(label = "DOWNLOAD", tone = CinematicGold, onClick = onDownload)
                CinematicSeriesAction(label = "COPY", tone = CinematicMuted, onClick = onCopyUrl)
                CinematicSeriesAction(label = if (isCasting) "CASTING" else "CAST", tone = Color(0xFF9DA3FF), onClick = onCast)
            }
        }
    }
}

@Composable
private fun CinematicExternalRatings(ratings: ExternalRatings, isLoading: Boolean) {
    val values = listOf(
        "IMDb" to ratings.imdb,
        "TMDb" to ratings.tmdb,
        "RT" to ratings.rottenTomatoes,
        "MC" to ratings.metacritic
    ).filter { it.second.available }
    if (values.isEmpty() && !isLoading) return
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (isLoading) {
            CinematicSeriesMarker("RATINGS LOADING", CinematicMuted)
        } else {
            values.forEach { (label, value) -> CinematicSeriesMarker("$label ${value.displayValue}", CinematicGold) }
        }
    }
}

@Composable
private fun CinematicSeriesSectionTitle(title: String, detail: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleLarge, color = CinematicText, fontWeight = FontWeight.Black)
        Text(text = detail, style = MaterialTheme.typography.bodyMedium, color = CinematicMuted)
    }
}

@Composable
private fun CinematicSeriesMarker(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        colors = SurfaceDefaults.colors(containerColor = color.copy(alpha = 0.17f)),
        border = Border(border = BorderStroke(1.dp, color), shape = RoundedCornerShape(999.dp))
    ) {
        Text(text = label, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun CinematicSeriesAction(label: String, tone: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(999.dp)
    TvClickableSurface(
        onClick = onClick,
        modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = CinematicPanel,
            focusedContainerColor = tone.copy(alpha = 0.3f),
            contentColor = CinematicText,
            focusedContentColor = CinematicText
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(border = BorderStroke(2.dp, tone), shape = shape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.025f)
    ) {
        Text(text = label, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
