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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
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
import com.streamvault.app.navigation.Routes
import com.streamvault.app.ui.screens.dashboard.DashboardFeatureAction
import com.streamvault.app.ui.screens.dashboard.DashboardUiState
import com.streamvault.domain.model.Channel
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.PlaybackHistory
import com.streamvault.domain.model.Series

/** Home Gold Night: Hero علوي ثم ثلاثة أعمدة عمودية: متابعة المشاهدة، القنوات المفضلة، المباشر الآن. */
@Composable
internal fun GoldNightDashboard(
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
            DashboardFeatureAction.CONTINUE_WATCHING ->
                uiState.continueWatching.firstOrNull()?.let(onContinueWatchingItemClick) ?: onNavigate(Routes.MOVIES)
            DashboardFeatureAction.MOVIES -> onNavigate(Routes.MOVIES)
            DashboardFeatureAction.SERIES -> onNavigate(Routes.SERIES)
        }
    }
    val liveNow = (uiState.recentChannels + uiState.favoriteChannels)
        .distinctBy { it.id }
        .sortedByDescending { it.currentProgram != null }
        .take(20)
    val films = (uiState.recommendedMovies + uiState.topRatedMovies + uiState.recentMovies).distinctBy { it.id }
    val series = (uiState.recentSeries + uiState.favoriteSeries).distinctBy { it.id }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GoldNightCanvasBrush)
            .padding(horizontal = 28.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        GoldNightScreenHeader(title = "Home")
        GoldNightHero(
            title = uiState.feature.title.ifBlank { films.firstOrNull()?.name ?: "Alaa Player" },
            summary = uiState.feature.summary.ifBlank { "Live channels, films and series in one golden night." },
            artworkUrl = uiState.feature.artworkUrl ?: films.firstOrNull()?.backdropUrl ?: films.firstOrNull()?.posterUrl,
            actionLabel = uiState.feature.actionLabel.ifBlank { "Play" },
            onAction = featureAction,
            onLiveTv = { onNavigate(Routes.LIVE_TV) }
        )
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            GoldNightHomeColumn(title = "Continue Watching", modifier = Modifier.weight(1f).fillMaxHeight()) {
                if (uiState.continueWatching.isEmpty()) {
                    item("gn_cw_empty") {
                        if (films.isNotEmpty() || series.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                films.take(6).forEach { movie ->
                                    GoldNightMediaRow(
                                        title = movie.name,
                                        subtitle = listOfNotNull(movie.year, movie.genre).joinToString(" • "),
                                        imageUrl = movie.posterUrl,
                                        progress = null,
                                        onClick = { onMovieClick(movie) }
                                    )
                                }
                                series.take(6).forEach { entry ->
                                    GoldNightMediaRow(
                                        title = entry.name,
                                        subtitle = entry.genre.orEmpty(),
                                        imageUrl = entry.posterUrl,
                                        progress = null,
                                        onClick = { onSeriesClick(entry) }
                                    )
                                }
                            }
                        } else {
                            GoldNightStatePanel("Nothing to resume", "Start a film or an episode and it will appear here.")
                        }
                    }
                } else {
                    items(uiState.continueWatching.take(20), key = { "gn_cw_${it.id}_${it.contentId}" }) { history ->
                        val progress = if (history.totalDurationMs > 0) {
                            history.resumePositionMs.toFloat() / history.totalDurationMs.toFloat()
                        } else null
                        val episodeLabel = if (history.seasonNumber != null && history.episodeNumber != null) {
                            "S${history.seasonNumber} E${history.episodeNumber}"
                        } else history.contentType.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
                        GoldNightMediaRow(
                            title = history.title,
                            subtitle = if (history.totalDurationMs > 0) {
                                "$episodeLabel • ${goldNightDuration(history.totalDurationMs - history.resumePositionMs)} left"
                            } else episodeLabel,
                            imageUrl = history.posterUrl,
                            progress = progress,
                            onClick = { onContinueWatchingItemClick(history) }
                        )
                    }
                }
            }
            GoldNightHomeColumn(title = "Favorite Channels", modifier = Modifier.weight(1f).fillMaxHeight()) {
                if (uiState.favoriteChannels.isEmpty()) {
                    item("gn_fav_empty") {
                        GoldNightStatePanel("No favourites yet", "Hold OK on any channel in Live TV to add it.")
                    }
                } else {
                    items(uiState.favoriteChannels.take(30), key = { "gn_fav_${it.id}" }) { channel ->
                        GoldNightHomeChannelRow(
                            channel = channel,
                            status = when (channel.id) {
                                in recordingChannelIds -> "REC"
                                in scheduledChannelIds -> "SCHEDULED"
                                else -> null
                            },
                            onClick = { onFavoriteChannelClick(channel, uiState.currentCombinedProfileId) }
                        )
                    }
                }
            }
            GoldNightHomeColumn(title = "Live Now", modifier = Modifier.weight(1f).fillMaxHeight()) {
                if (liveNow.isEmpty()) {
                    item("gn_live_empty") {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            GoldNightStatePanel("Live TV", "Open the channel columns to start watching.")
                            GoldNightButton(label = "Open Live TV", onClick = { onNavigate(Routes.LIVE_TV) }, primary = true)
                        }
                    }
                } else {
                    items(liveNow, key = { "gn_live_${it.id}" }) { channel ->
                        GoldNightHomeChannelRow(
                            channel = channel,
                            status = if (channel.id in recordingChannelIds) "REC" else null,
                            showProgram = true,
                            onClick = { onRecentChannelClick(channel, uiState.currentCombinedProfileId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GoldNightHero(
    title: String,
    summary: String,
    artworkUrl: String?,
    actionLabel: String,
    onAction: () -> Unit,
    onLiveTv: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(GoldNightPanel)
    ) {
        GoldNightArtwork(imageUrl = artworkUrl, title = title, modifier = Modifier.fillMaxSize(), cornerRadius = 16.dp)
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(GoldNightCanvas.copy(alpha = 0.96f), GoldNightCanvas.copy(alpha = 0.7f), Color.Transparent)
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(560.dp)
                .padding(start = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = "FEATURED TONIGHT", style = MaterialTheme.typography.labelLarge, color = GoldNightGold)
            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall,
                color = GoldNightText,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyLarge,
                color = GoldNightMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GoldNightButton(label = actionLabel, onClick = onAction, primary = true, icon = Icons.Default.PlayArrow)
                GoldNightButton(label = "Live TV", onClick = onLiveTv)
            }
        }
    }
}

@Composable
private fun GoldNightHomeColumn(
    title: String,
    modifier: Modifier = Modifier,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        GoldNightColumnTitle(title = title)
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(vertical = 6.dp, horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}

@Composable
internal fun GoldNightMediaRow(
    title: String,
    subtitle: String,
    imageUrl: String?,
    progress: Float?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null
) {
    GoldNightCard(onClick = onClick, onLongClick = onLongClick, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GoldNightArtwork(imageUrl = imageUrl, title = title, modifier = Modifier.width(88.dp).height(52.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = GoldNightMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (progress != null) GoldNightProgressBar(progress = progress, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun GoldNightHomeChannelRow(
    channel: Channel,
    status: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showProgram: Boolean = false
) {
    GoldNightCard(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (channel.number > 0) {
                Text(
                    text = channel.number.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = GoldNightMuted,
                    modifier = Modifier.width(34.dp)
                )
            }
            GoldNightRoundLogo(channelName = channel.name, logoUrl = channel.logoUrl, size = 40.dp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = channel.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                val program = channel.currentProgram
                if (showProgram && program != null) {
                    Text(
                        text = program.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = GoldNightMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                GoldNightProgressBar(progress = goldNightProgramProgress(program), modifier = Modifier.fillMaxWidth())
            }
            if (status != null) {
                Text(
                    text = status,
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldNightGold,
                    modifier = Modifier.size(width = 72.dp, height = 18.dp)
                )
            }
        }
    }
}
