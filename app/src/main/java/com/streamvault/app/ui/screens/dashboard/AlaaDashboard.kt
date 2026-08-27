package com.streamvault.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ClickableSurfaceDefaults
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
import com.streamvault.app.ui.interaction.TvButton
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.app.ui.theme.AlaaThemeColors
import com.streamvault.app.ui.theme.AlaaThemeDimensions
import com.streamvault.app.ui.theme.AlaaThemeTypography
import com.streamvault.domain.model.Category
import com.streamvault.domain.model.Channel
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.PlaybackHistory
import com.streamvault.domain.model.Series
import com.streamvault.domain.model.VirtualCategoryIds
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val AlaaAccent = AlaaThemeColors.Accent
private val AlaaSidebarBg = AlaaThemeColors.Sidebar

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
    val liveCategories = remember(uiState.liveCategories) {
        uiState.liveCategories
            .filter { !it.isVirtual && it.name.isNotBlank() }
            .take(12)
    }

    Row(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        AlaaSidebar(onNavigate = onNavigate)
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item(key = "alaa_hero") {
                AlaaHero(
                    title = heroTitle,
                    summary = heroSummary,
                    artworkUrl = uiState.feature.artworkUrl,
                    onWatchNow = { onNavigate(Routes.LIVE_TV) }
                )
            }
            if (uiState.isLoading &&
                uiState.favoriteChannels.isEmpty() &&
                uiState.recentChannels.isEmpty() &&
                liveCategories.isEmpty()
            ) {
                item(key = "alaa_loading") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
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
                        onSeeAll = { onNavigate(Routes.liveTv(VirtualCategoryIds.FAVORITES)) }
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
                        onSeeAll = { onNavigate(Routes.liveTv(VirtualCategoryIds.RECENT)) }
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
                            subtitle = series.releaseDate
                                ?: stringResource(R.string.dashboard_updated_series_badge),
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
        AlaaHomeLibraryColumn(
            uiState = uiState,
            liveCategories = liveCategories,
            onNavigate = onNavigate
        )
    }
}

@Composable
private fun AlaaHomeLibraryColumn(
    uiState: DashboardUiState,
    liveCategories: List<Category>,
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .width(286.dp)
            .fillMaxHeight()
            .background(AlaaThemeColors.Surface.copy(alpha = 0.88f), RoundedCornerShape(20.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "المكتبة",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = uiState.provider?.name ?: "لا يوجد مزود نشط",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.62f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        AlaaHomeRouteAction(
            icon = Icons.Default.Tv,
            label = stringResource(R.string.nav_live_tv),
            detail = "${uiState.stats.liveChannelCount} قناة",
            onClick = { onNavigate(Routes.LIVE_TV) }
        )
        AlaaHomeRouteAction(
            icon = Icons.Default.Movie,
            label = stringResource(R.string.nav_movies),
            detail = "${uiState.stats.movieLibraryCount} عنوان",
            onClick = { onNavigate(Routes.MOVIES) }
        )
        AlaaHomeRouteAction(
            icon = Icons.Default.VideoLibrary,
            label = stringResource(R.string.nav_series),
            detail = "${uiState.stats.seriesLibraryCount} مسلسل",
            onClick = { onNavigate(Routes.SERIES) }
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "فئات البث",
            style = MaterialTheme.typography.titleSmall,
            color = AlaaAccent,
            fontWeight = FontWeight.Bold
        )
        if (liveCategories.isEmpty()) {
            Text(
                text = "لا توجد فئات بث متاحة حالياً",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.58f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                items(liveCategories, key = { it.id }) { category ->
                    TvClickableSurface(
                        onClick = { onNavigate(Routes.liveTv(category.id)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(10.dp)),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = AlaaThemeColors.BrowseRail,
                            focusedContainerColor = AlaaAccent.copy(alpha = 0.48f),
                            contentColor = Color.White,
                            focusedContentColor = Color.White
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Category, contentDescription = null, tint = AlaaAccent, modifier = Modifier.size(16.dp))
                            Text(category.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (category.count > 0) Text(category.count.toString(), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.62f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlaaHomeRouteAction(
    icon: ImageVector,
    label: String,
    detail: String,
    onClick: () -> Unit
) {
    TvClickableSurface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = AlaaThemeColors.BrowseRail,
            focusedContainerColor = AlaaThemeColors.SurfaceFocused,
            contentColor = Color.White,
            focusedContentColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(icon, contentDescription = null, tint = AlaaAccent, modifier = Modifier.size(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(detail, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.60f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun AlaaSidebar(onNavigate: (String) -> Unit) {
    val currentTime = remember {
        LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"))
    }
    val currentDate = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy"))
    }

    Column(
        modifier = Modifier
            .width(248.dp)
            .fillMaxHeight()
            .background(AlaaSidebarBg)
            .padding(vertical = 24.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Alaa",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = " Player",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = AlaaAccent
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
            SidebarItem(
                icon = Icons.Default.Home,
                label = stringResource(R.string.nav_home),
                isSelected = true,
                onClick = { onNavigate(Routes.HOME) }
            )
            SidebarItem(
                icon = Icons.Default.Tv,
                label = stringResource(R.string.nav_live_tv),
                onClick = { onNavigate(Routes.LIVE_TV) }
            )
            SidebarItem(
                icon = Icons.Default.Movie,
                label = stringResource(R.string.nav_movies),
                onClick = { onNavigate(Routes.MOVIES) }
            )
            SidebarItem(
                icon = Icons.Default.VideoLibrary,
                label = stringResource(R.string.nav_series),
                onClick = { onNavigate(Routes.SERIES) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            SidebarItem(
                icon = Icons.Default.Favorite,
                label = stringResource(R.string.dashboard_favorite_channels),
                onClick = { onNavigate(Routes.liveTv(VirtualCategoryIds.FAVORITES)) }
            )
            SidebarItem(
                icon = Icons.Default.History,
                label = stringResource(R.string.dashboard_recent_channels),
                onClick = { onNavigate(Routes.liveTv(VirtualCategoryIds.RECENT)) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            SidebarItem(
                icon = Icons.Default.Settings,
                label = stringResource(R.string.nav_settings),
                onClick = { onNavigate(Routes.SETTINGS) }
            )
            SidebarItem(
                icon = Icons.Default.SwapHoriz,
                label = stringResource(R.string.settings_providers),
                onClick = { onNavigate(Routes.providerSetup()) }
            )
        }
        Column {
            Text(
                text = currentTime,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = currentDate,
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SidebarItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val background = if (isSelected) AlaaAccent.copy(alpha = 0.2f) else Color.Transparent
    TvClickableSurface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = background,
            focusedContainerColor = AlaaAccent.copy(alpha = 0.35f),
            pressedContainerColor = AlaaAccent.copy(alpha = 0.45f),
            contentColor = if (isSelected) Color.White else Color.Gray,
            focusedContentColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) AlaaAccent else Color.Gray,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = label,
                color = if (isSelected) Color.White else Color.Gray,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AlaaHero(
    title: String,
    summary: String,
    artworkUrl: String?,
    onWatchNow: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(AlaaThemeDimensions.CornerLarge))
            .background(Brush.linearGradient(listOf(AlaaThemeColors.CanvasRaised, AlaaThemeColors.SurfaceElevated)))
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
                        listOf(Color(0xFF06080F).copy(alpha = 0.95f), Color.Transparent)
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(horizontal = 36.dp)
                .fillMaxWidth(0.7f)
        ) {
            Text(
                text = stringResource(R.string.app_name).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = AlaaAccent
            )
            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall.copy(fontSize = AlaaThemeTypography.HeroTitleSize),
                fontWeight = AlaaThemeTypography.HeroTitleWeight,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 8.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(24.dp))
            TvButton(
                onClick = onWatchNow,
                colors = ButtonDefaults.colors(
                    containerColor = AlaaAccent,
                    contentColor = Color.White,
                    focusedContainerColor = Color(0xFFFF5267),
                    focusedContentColor = Color.White
                ),
                shape = ButtonDefaults.shape(shape = RoundedCornerShape(50))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Text(
                        text = stringResource(R.string.nav_live_tv),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun AlaaLiveCategories(
    title: String,
    categories: List<Category>,
    onCategoryClick: (Category) -> Unit,
    onSeeAll: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            TvClickableSurface(
                onClick = onSeeAll,
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = Color.Transparent,
                    focusedContainerColor = AlaaAccent.copy(alpha = 0.2f),
                    contentColor = AlaaAccent,
                    focusedContentColor = Color.White
                )
            ) {
                Text(
                    text = stringResource(R.string.action_see_all),
                    color = AlaaAccent,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(categories, key = { it.id }) { category ->
                val color = AlaaThemeColors.CategoryPalette[
                    (kotlin.math.abs(category.id % AlaaThemeColors.CategoryPalette.size.toLong())).toInt()
                ]
                TvClickableSurface(
                    onClick = { onCategoryClick(category) },
                    modifier = Modifier
                        .width(168.dp)
                        .height(104.dp),
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(16.dp)),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = color.copy(alpha = 0.22f),
                        focusedContainerColor = color.copy(alpha = 0.45f),
                        pressedContainerColor = color.copy(alpha = 0.55f),
                        contentColor = Color.White,
                        focusedContentColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = category.name,
                                color = Color.White,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (category.count > 0) {
                                Text(
                                    text = category.count.toString(),
                                    color = Color.White.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
