package com.streamvault.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.streamvault.app.R
import com.streamvault.app.navigation.Routes
import com.streamvault.app.ui.components.CategoryRow
import com.streamvault.app.ui.components.ChannelCard
import com.streamvault.app.ui.components.ContinueWatchingRow
import com.streamvault.app.ui.components.MovieCard
import com.streamvault.app.ui.components.SeriesCard
import com.streamvault.app.ui.components.rememberCrossfadeImageModel
import com.streamvault.app.ui.design.AppColors
import com.streamvault.app.ui.interaction.TvButton
import com.streamvault.domain.model.Channel
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.PlaybackHistory
import com.streamvault.domain.model.Series

/**
 * Alternate home composition inspired by the supplied visual references. It deliberately reuses
 * the app's existing content rows and navigation callbacks so that data, playback, and parental
 * protection behavior remain unchanged while the dashboard presentation changes completely.
 */
@Composable
internal fun AlaaDashboard(
    uiState: DashboardUiState,
    recordingChannelIds: Set<Long>,
    scheduledChannelIds: Set<Long>,
    onNavigate: (String) -> Unit,
    onRecentChannelClick: (Channel, Long?) -> Unit,
    onFavoriteChannelClick: (Channel, Long?) -> Unit,
    onMovieClick: (Movie) -> Unit,
    onSeriesClick: (Series) -> Unit,
    onPlaybackHistoryClick: (PlaybackHistory) -> Unit,
    onContinueWatchingItemClick: (PlaybackHistory) -> Unit
) {
    val providerName = uiState.provider?.name.orEmpty()
    val heroTitle = uiState.feature.title.ifBlank { stringResource(R.string.dashboard_title) }
    val heroSummary = uiState.feature.summary.ifBlank {
        stringResource(R.string.dashboard_subtitle, providerName)
    }
    val currentProfileId = uiState.currentCombinedProfileId

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item(key = "alaa_hero") {
            AlaaHero(
                providerName = providerName,
                title = heroTitle,
                summary = heroSummary,
                artworkUrl = uiState.feature.artworkUrl,
                onOpenLiveTv = { onNavigate(Routes.LIVE_TV) },
                onOpenMovies = { onNavigate(Routes.MOVIES) }
            )
        }
        item(key = "alaa_actions") {
            AlaaQuickActions(
                liveCount = uiState.stats.liveChannelCount,
                movieCount = uiState.stats.movieLibraryCount,
                seriesCount = uiState.stats.seriesLibraryCount,
                onOpenLiveTv = { onNavigate(Routes.LIVE_TV) },
                onOpenGuide = { onNavigate(Routes.EPG) },
                onOpenMovies = { onNavigate(Routes.MOVIES) },
                onOpenSeries = { onNavigate(Routes.SERIES) }
            )
        }
        if (uiState.isLoading && uiState.favoriteChannels.isEmpty() && uiState.recentChannels.isEmpty()) {
            item(key = "alaa_loading") {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AlaaAccent)
                }
            }
        }
        if (uiState.favoriteChannels.isNotEmpty()) {
            item(key = "alaa_favorite_channels") {
                CategoryRow(
                    title = stringResource(R.string.dashboard_favorite_channels),
                    items = uiState.favoriteChannels,
                    keySelector = { it.id },
                    onSeeAll = { onNavigate(Routes.liveTv(com.streamvault.domain.model.VirtualCategoryIds.FAVORITES)) }
                ) { channel ->
                    ChannelCard(
                        channel = channel,
                        isRecording = channel.id in recordingChannelIds,
                        isScheduledRecording = channel.id in scheduledChannelIds,
                        onClick = { onFavoriteChannelClick(channel, currentProfileId) }
                    )
                }
            }
        }
        if (uiState.recentChannels.isNotEmpty()) {
            item(key = "alaa_recent_channels") {
                CategoryRow(
                    title = stringResource(R.string.dashboard_recent_channels),
                    items = uiState.recentChannels,
                    keySelector = { it.id },
                    onSeeAll = { onNavigate(Routes.liveTv(com.streamvault.domain.model.VirtualCategoryIds.RECENT)) }
                ) { channel ->
                    ChannelCard(
                        channel = channel,
                        isRecording = channel.id in recordingChannelIds,
                        isScheduledRecording = channel.id in scheduledChannelIds,
                        onClick = { onRecentChannelClick(channel, currentProfileId) }
                    )
                }
            }
        }
        if (uiState.continueWatching.isNotEmpty()) {
            item(key = "alaa_continue_watching") {
                ContinueWatchingRow(
                    items = uiState.continueWatching,
                    onItemClick = onContinueWatchingItemClick
                )
            }
        }
        if (uiState.recentMovies.isNotEmpty()) {
            item(key = "alaa_recent_movies") {
                CategoryRow(
                    title = stringResource(R.string.dashboard_recent_movies),
                    items = uiState.recentMovies,
                    keySelector = { it.id },
                    onSeeAll = { onNavigate(Routes.MOVIES) }
                ) { movie ->
                    MovieCard(movie = movie, onClick = { onMovieClick(movie) })
                }
            }
        }
        if (uiState.recentSeries.isNotEmpty()) {
            item(key = "alaa_recent_series") {
                CategoryRow(
                    title = stringResource(R.string.dashboard_recent_series),
                    items = uiState.recentSeries,
                    keySelector = { it.id },
                    onSeeAll = { onNavigate(Routes.SERIES) }
                ) { series ->
                    SeriesCard(
                        series = series,
                        subtitle = series.releaseDate ?: stringResource(R.string.dashboard_updated_series_badge),
                        onClick = { onSeriesClick(series) }
                    )
                }
            }
        }
        if (uiState.topRatedMovies.isNotEmpty()) {
            item(key = "alaa_top_rated_movies") {
                CategoryRow(
                    title = stringResource(R.string.dashboard_top_rated_movies),
                    items = uiState.topRatedMovies,
                    keySelector = { it.id },
                    onSeeAll = { onNavigate(Routes.MOVIES) }
                ) { movie ->
                    MovieCard(movie = movie, onClick = { onMovieClick(movie) })
                }
            }
        }
        if (uiState.recommendedMovies.isNotEmpty()) {
            item(key = "alaa_recommended_movies") {
                CategoryRow(
                    title = stringResource(R.string.dashboard_recommended_movies),
                    items = uiState.recommendedMovies,
                    keySelector = { it.id },
                    onSeeAll = { onNavigate(Routes.MOVIES) }
                ) { movie ->
                    MovieCard(movie = movie, onClick = { onMovieClick(movie) })
                }
            }
        }
    }
}

private val AlaaAccent = Color(0xFFFF304A)

@Composable
private fun AlaaHero(
    providerName: String,
    title: String,
    summary: String,
    artworkUrl: String?,
    onOpenLiveTv: () -> Unit,
    onOpenMovies: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .height(288.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF080B14), Color(0xFF151B2D), Color(0xFF6D0E20))
                )
            )
    ) {
        if (!artworkUrl.isNullOrBlank()) {
            AsyncImage(
                model = rememberCrossfadeImageModel(artworkUrl),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF06080F).copy(alpha = 0.96f),
                            Color(0xFF0A0D18).copy(alpha = 0.82f),
                            Color(0xFF210B14).copy(alpha = 0.32f)
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(horizontal = 34.dp, vertical = 28.dp)
                .fillMaxWidth(0.62f)
        ) {
            Text(
                text = stringResource(R.string.app_name).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = AlaaAccent,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = summary,
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.78f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (providerName.isNotBlank()) {
                Text(
                    text = providerName,
                    modifier = Modifier.padding(top = 12.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.58f)
                )
            }
            Row(
                modifier = Modifier.padding(top = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AlaaActionButton(label = stringResource(R.string.nav_live_tv), primary = true, onClick = onOpenLiveTv)
                AlaaActionButton(label = stringResource(R.string.nav_movies), primary = false, onClick = onOpenMovies)
            }
        }
    }
}

@Composable
private fun AlaaQuickActions(
    liveCount: Int,
    movieCount: Int,
    seriesCount: Int,
    onOpenLiveTv: () -> Unit,
    onOpenGuide: () -> Unit,
    onOpenMovies: () -> Unit,
    onOpenSeries: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${liveCount} · ${movieCount} · ${seriesCount}",
            modifier = Modifier.padding(top = 2.dp, bottom = 10.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextTertiary
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AlaaActionButton(label = stringResource(R.string.nav_live_tv), primary = true, onClick = onOpenLiveTv)
            AlaaActionButton(label = stringResource(R.string.nav_epg), primary = false, onClick = onOpenGuide)
            AlaaActionButton(label = stringResource(R.string.nav_movies), primary = false, onClick = onOpenMovies)
            AlaaActionButton(label = stringResource(R.string.nav_series), primary = false, onClick = onOpenSeries)
        }
    }
}

@Composable
private fun AlaaActionButton(
    label: String,
    primary: Boolean,
    onClick: () -> Unit
) {
    TvButton(
        onClick = onClick,
        colors = ButtonDefaults.colors(
            containerColor = if (primary) AlaaAccent else Color.White.copy(alpha = 0.10f),
            contentColor = Color.White,
            focusedContainerColor = if (primary) Color(0xFFFF5267) else Color.White.copy(alpha = 0.20f),
            focusedContentColor = Color.White
        )
    ) {
        Text(text = label, maxLines = 1)
    }
}
