package com.streamvault.app.ui.themes.cinematic

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.streamvault.app.navigation.Routes
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.app.ui.screens.dashboard.DashboardFeatureAction
import com.streamvault.app.ui.screens.dashboard.DashboardUiState
import com.streamvault.domain.model.Channel
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.PlaybackHistory
import com.streamvault.domain.model.Series

/**
 * Cinematic's home presentation deliberately uses a screening desk instead of
 * the shared dashboard shelf visuals. It consumes DashboardUiState only.
 */
@Composable
internal fun CinematicDashboard(
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
    val featureAction = {
        when (uiState.feature.actionType) {
            DashboardFeatureAction.LIVE -> onNavigate(Routes.LIVE_TV)
            DashboardFeatureAction.CONTINUE_WATCHING -> uiState.continueWatching.firstOrNull()
                ?.let(onContinueWatchingItemClick)
                ?: onNavigate(Routes.MOVIES)
            DashboardFeatureAction.MOVIES -> onNavigate(Routes.MOVIES)
            DashboardFeatureAction.SERIES -> onNavigate(Routes.SERIES)
        }
    }
    val heroTitle = uiState.feature.title.ifBlank { "Tonight on Alaa" }
    val heroSummary = uiState.feature.summary.ifBlank {
        "Choose a live channel, continue an unfinished story, or explore your library."
    }
    val heroAction = uiState.feature.actionLabel.ifBlank { "START WATCHING" }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CinematicCanvas)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 42.dp, top = 30.dp, end = 42.dp, bottom = 42.dp),
            verticalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            item(key = "cinematic_dashboard_hero") {
                CinematicDashboardHero(
                    title = heroTitle,
                    summary = heroSummary,
                    artworkUrl = uiState.feature.artworkUrl,
                    actionLabel = heroAction,
                    onAction = featureAction
                )
            }
            item(key = "cinematic_dashboard_portals") {
                CinematicPortals(onNavigate = onNavigate)
            }
            if (uiState.continueWatching.isNotEmpty()) {
                item(key = "cinematic_dashboard_continue") {
                    CinematicHistoryShelf(
                        title = "CONTINUE THE STORY",
                        subtitle = "Unfinished screenings in your library",
                        entries = uiState.continueWatching,
                        onEntryClick = onContinueWatchingItemClick
                    )
                }
            }
            if (uiState.favoriteChannels.isNotEmpty()) {
                item(key = "cinematic_dashboard_favourites") {
                    CinematicChannelShelf(
                        title = "YOUR LIVE SEATS",
                        subtitle = "Favourite channels",
                        channels = uiState.favoriteChannels,
                        recordingChannelIds = recordingChannelIds,
                        scheduledChannelIds = scheduledChannelIds,
                        onChannelClick = { onFavoriteChannelClick(it, uiState.currentCombinedProfileId) },
                        onSeeAll = { onNavigate(Routes.liveTv(com.streamvault.domain.model.VirtualCategoryIds.FAVORITES)) }
                    )
                }
            }
            if (uiState.recentChannels.isNotEmpty()) {
                item(key = "cinematic_dashboard_recent_live") {
                    CinematicChannelShelf(
                        title = "RECENTLY TUNED",
                        subtitle = "Return to a live channel",
                        channels = uiState.recentChannels,
                        recordingChannelIds = recordingChannelIds,
                        scheduledChannelIds = scheduledChannelIds,
                        onChannelClick = { onRecentChannelClick(it, uiState.currentCombinedProfileId) },
                        onSeeAll = { onNavigate(Routes.liveTv(com.streamvault.domain.model.VirtualCategoryIds.RECENT)) }
                    )
                }
            }
            if (uiState.topRatedMovies.isNotEmpty() || uiState.recommendedMovies.isNotEmpty()) {
                item(key = "cinematic_dashboard_movies") {
                    CinematicMovieShelf(
                        title = "FEATURE PRESENTATIONS",
                        subtitle = "Selected from your movie library",
                        movies = (uiState.topRatedMovies + uiState.recommendedMovies).distinctBy { it.id },
                        onMovieClick = onMovieClick,
                        onSeeAll = { onNavigate(Routes.MOVIES) }
                    )
                }
            }
            if (uiState.recentSeries.isNotEmpty() || uiState.favoriteSeries.isNotEmpty()) {
                item(key = "cinematic_dashboard_series") {
                    CinematicSeriesShelf(
                        title = "SERIES MARATHON",
                        subtitle = "Stories waiting for their next episode",
                        series = (uiState.recentSeries + uiState.favoriteSeries).distinctBy { it.id },
                        onSeriesClick = onSeriesClick,
                        onSeeAll = { onNavigate(Routes.SERIES) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CinematicDashboardHero(
    title: String,
    summary: String,
    artworkUrl: String?,
    actionLabel: String,
    onAction: () -> Unit
) {
    val shape = RoundedCornerShape(28.dp)
    Surface(
        modifier = Modifier.fillMaxWidth().height(360.dp),
        shape = shape,
        colors = SurfaceDefaults.colors(containerColor = CinematicPanel),
        border = Border(
            border = BorderStroke(1.dp, CinematicWine.copy(alpha = 0.5f)),
            shape = shape
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!artworkUrl.isNullOrBlank()) {
                AsyncImage(
                    model = artworkUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(CinematicCanvas.copy(alpha = 0.98f), CinematicCanvas.copy(alpha = 0.68f), Color.Transparent)
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(38.dp)
                    .fillMaxWidth(0.6f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "ALAA CINEMATIC SELECTION",
                    style = MaterialTheme.typography.labelMedium,
                    color = CinematicGold,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.displaySmall,
                    color = CinematicText,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyLarge,
                    color = CinematicMuted,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                CinematicHomeAction(label = actionLabel, onClick = onAction)
            }
        }
    }
}

@Composable
private fun CinematicPortals(onNavigate: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
        CinematicPortal(
            label = "LIVE\nTRANSMISSIONS",
            detail = "Channels, guide and live preview",
            tone = CinematicWine,
            onClick = { onNavigate(Routes.LIVE_TV) },
            modifier = Modifier.weight(1f)
        )
        CinematicPortal(
            label = "FEATURE\nFILMS",
            detail = "Browse your movie library",
            tone = CinematicGold,
            onClick = { onNavigate(Routes.MOVIES) },
            modifier = Modifier.weight(1f)
        )
        CinematicPortal(
            label = "SERIES\nROOM",
            detail = "Pick up a season or discover a series",
            tone = Color(0xFF9DA3FF),
            onClick = { onNavigate(Routes.SERIES) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CinematicPortal(
    label: String,
    detail: String,
    tone: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)
    TvClickableSurface(
        onClick = onClick,
        modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = CinematicPanel,
            focusedContainerColor = CinematicPanelRaised,
            contentColor = CinematicText,
            focusedContentColor = CinematicText
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(border = BorderStroke(2.dp, tone), shape = shape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.025f)
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = label, style = MaterialTheme.typography.titleLarge, color = tone, fontWeight = FontWeight.Black)
            Text(text = detail, style = MaterialTheme.typography.bodyMedium, color = CinematicMuted, maxLines = 2)
        }
    }
}

@Composable
private fun CinematicHistoryShelf(
    title: String,
    subtitle: String,
    entries: List<PlaybackHistory>,
    onEntryClick: (PlaybackHistory) -> Unit
) {
    CinematicShelfHeader(title, subtitle, seeAllLabel = null, onSeeAll = null)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(top = 14.dp)) {
        items(entries, key = { it.id.takeIf { id -> id > 0 } ?: it.contentId }) { entry ->
            CinematicHistoryCard(entry = entry, onClick = { onEntryClick(entry) })
        }
    }
}

@Composable
private fun CinematicChannelShelf(
    title: String,
    subtitle: String,
    channels: List<Channel>,
    recordingChannelIds: Set<Long>,
    scheduledChannelIds: Set<Long>,
    onChannelClick: (Channel) -> Unit,
    onSeeAll: () -> Unit
) {
    CinematicShelfHeader(title, subtitle, seeAllLabel = "OPEN GUIDE", onSeeAll = onSeeAll)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(top = 14.dp)) {
        items(channels, key = { it.id }) { channel ->
            CinematicChannelCard(
                channel = channel,
                recording = channel.id in recordingChannelIds,
                scheduled = channel.id in scheduledChannelIds,
                onClick = { onChannelClick(channel) }
            )
        }
    }
}

@Composable
private fun CinematicMovieShelf(
    title: String,
    subtitle: String,
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit,
    onSeeAll: () -> Unit
) {
    CinematicShelfHeader(title, subtitle, seeAllLabel = "OPEN FILMS", onSeeAll = onSeeAll)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(top = 14.dp)) {
        items(movies.take(24), key = { it.id }) { movie ->
            CinematicCatalogueCard(
                title = movie.name,
                detail = listOfNotNull(movie.year, movie.genre).joinToString(" · ").ifBlank { "FILM" },
                posterUrl = movie.posterUrl,
                rating = movie.rating,
                onClick = { onMovieClick(movie) }
            )
        }
    }
}

@Composable
private fun CinematicSeriesShelf(
    title: String,
    subtitle: String,
    series: List<Series>,
    onSeriesClick: (Series) -> Unit,
    onSeeAll: () -> Unit
) {
    CinematicShelfHeader(title, subtitle, seeAllLabel = "OPEN SERIES", onSeeAll = onSeeAll)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(top = 14.dp)) {
        items(series.take(24), key = { it.id }) { item ->
            CinematicCatalogueCard(
                title = item.name,
                detail = item.genre?.takeIf { it.isNotBlank() } ?: "SERIES",
                posterUrl = item.posterUrl,
                rating = item.rating,
                onClick = { onSeriesClick(item) }
            )
        }
    }
}

@Composable
private fun CinematicShelfHeader(
    title: String,
    subtitle: String,
    seeAllLabel: String?,
    onSeeAll: (() -> Unit)?
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleLarge, color = CinematicText, fontWeight = FontWeight.Black)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = CinematicMuted)
        }
        if (seeAllLabel != null && onSeeAll != null) {
            CinematicHomeAction(label = seeAllLabel, compact = true, onClick = onSeeAll)
        }
    }
}

@Composable
private fun CinematicHistoryCard(entry: PlaybackHistory, onClick: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    TvClickableSurface(
        onClick = onClick,
        modifier = Modifier.width(218.dp),
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = CinematicPanel,
            focusedContainerColor = CinematicPanelRaised,
            contentColor = CinematicText,
            focusedContentColor = CinematicText
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(border = BorderStroke(2.dp, CinematicWine), shape = shape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.03f)
    ) {
        Column {
            AsyncImage(
                model = entry.posterUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(122.dp).clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = entry.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                LinearProgressIndicator(
                    progress = { if (entry.totalDurationMs > 0L) (entry.resumePositionMs.toFloat() / entry.totalDurationMs).coerceIn(0f, 1f) else 0f },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = CinematicWine,
                    trackColor = CinematicMuted.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
private fun CinematicChannelCard(channel: Channel, recording: Boolean, scheduled: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    TvClickableSurface(
        onClick = onClick,
        modifier = Modifier.width(238.dp),
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = CinematicPanel,
            focusedContainerColor = CinematicPanelRaised,
            contentColor = CinematicText,
            focusedContentColor = CinematicText
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(border = BorderStroke(2.dp, CinematicGold), shape = shape)
        )
    ) {
        Row(modifier = Modifier.padding(15.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(10.dp)).background(CinematicCanvas), contentAlignment = Alignment.Center) {
                AsyncImage(model = channel.logoUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = channel.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = when {
                        recording -> "RECORDING"
                        scheduled -> "SCHEDULED"
                        else -> channel.currentProgram?.title ?: "LIVE CHANNEL"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (recording) CinematicWine else CinematicMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CinematicCatalogueCard(title: String, detail: String, posterUrl: String?, rating: Float, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    TvClickableSurface(
        onClick = onClick,
        modifier = Modifier.width(172.dp),
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = CinematicPanel,
            focusedContainerColor = CinematicPanelRaised,
            contentColor = CinematicText,
            focusedContentColor = CinematicText
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(border = BorderStroke(2.dp, CinematicGold), shape = shape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.04f)
    ) {
        Column {
            AsyncImage(
                model = posterUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(224.dp).clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = if (rating > 0f) "$detail · ${"%.1f".format(rating)}" else detail, style = MaterialTheme.typography.labelSmall, color = CinematicMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun CinematicHomeAction(label: String, onClick: () -> Unit, compact: Boolean = false) {
    val shape = RoundedCornerShape(999.dp)
    TvClickableSurface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = CinematicWine,
            focusedContainerColor = CinematicGold,
            contentColor = CinematicText,
            focusedContentColor = CinematicCanvas
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(border = BorderStroke(2.dp, CinematicText), shape = shape)
        )
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = if (compact) 15.dp else 21.dp, vertical = if (compact) 9.dp else 12.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Black
        )
    }
}
