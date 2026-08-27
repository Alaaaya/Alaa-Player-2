package com.streamvault.app.ui.themes.alaa

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.streamvault.app.ui.components.rememberCrossfadeImageModel
import com.streamvault.app.ui.interaction.TvButton
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.app.ui.interaction.TvIconButton
import com.streamvault.app.ui.theme.AlaaThemeColors
import com.streamvault.app.ui.theme.AlaaThemeDimensions
import com.streamvault.app.ui.theme.AlaaThemeFocus
import com.streamvault.domain.model.Episode
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.Season
import com.streamvault.domain.model.Series

/**
 * تصميم ALAA لتفاصيل VOD. لا ينشئ بيانات بديلة ولا مشغلاً موازياً؛ كل الأحداث تمرر
 * إلى callbacks الحقيقية التي تستخدمها الشاشة الأصلية وطبقات IPTV الحالية.
 */
@Composable
fun AlaaMovieDetail(
    movie: Movie,
    hasResume: Boolean,
    onPlay: () -> Unit,
    onPlayFromBeginning: () -> Unit,
    onToggleFavorite: () -> Unit,
    onBack: () -> Unit
) {
    var showMoreInformation by rememberSaveable(movie.id) { mutableStateOf(false) }
    val watchLater = rememberAlaaWatchLaterState(contentType = "movie", contentId = movie.id)
    AlaaDetailBackdrop(imageUrl = movie.backdropUrl ?: movie.posterUrl) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(32.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            item {
                AlaaBackButton(onBack = onBack)
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(26.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    AlaaPoster(
                        imageUrl = movie.posterUrl ?: movie.backdropUrl,
                        title = movie.name,
                        status = if (hasResume) "متابعة المشاهدة" else "فيلم"
                    )
                    Column(
                        modifier = Modifier.weight(0.76f),
                        verticalArrangement = Arrangement.spacedBy(13.dp)
                    ) {
                        AlaaTitleAndFacts(
                            title = movie.name,
                            facts = listOfNotNull(
                                movie.rating.takeIf { it > 0f }?.let { "★ ${"%.1f".format(it)}" },
                                movie.year?.takeIf { it.isNotBlank() },
                                movie.genre?.takeIf { it.isNotBlank() },
                                movie.duration?.takeIf { it.isNotBlank() }
                            )
                        )
                        AlaaDescription(
                            text = movie.plot,
                            expanded = showMoreInformation,
                            additionalFacts = listOfNotNull(
                                movie.director?.takeIf { it.isNotBlank() }?.let { "المخرج: $it" },
                                movie.cast?.takeIf { it.isNotBlank() }?.let { "الطاقم: $it" },
                                movie.variantLabel?.takeIf { it.isNotBlank() }?.let { "الإصدار: $it" }
                            )
                        )
                        AlaaActionRow(
                            primaryLabel = if (hasResume) "متابعة المشاهدة" else "تشغيل",
                            onPrimaryClick = onPlay,
                            onRestartClick = onPlayFromBeginning,
                            isFavorite = movie.isFavorite,
                            onToggleFavorite = onToggleFavorite,
                            isWatchLater = watchLater.isSaved,
                            onToggleWatchLater = watchLater::toggle,
                            showMoreInformation = showMoreInformation,
                            onToggleMoreInformation = { showMoreInformation = !showMoreInformation }
                        )
                    }
                    Spacer(modifier = Modifier.weight(0.24f))
                }
            }
        }
    }
}

@Composable
fun AlaaSeriesDetail(
    series: Series,
    selectedSeason: Season?,
    selectedEpisode: Episode?,
    resumeEpisode: Episode?,
    unwatchedEpisodeCount: Int,
    onToggleFavorite: () -> Unit,
    onSeasonSelected: (Season) -> Unit,
    onEpisodeSelected: (Episode) -> Unit,
    onResumeEpisode: (Episode) -> Unit,
    onPlayEpisodeFromBeginning: (Episode) -> Unit,
    onBack: () -> Unit
) {
    val selectedSeasonEpisodes = selectedSeason?.episodes.orEmpty()
    val activeEpisode = selectedSeasonEpisodes.firstOrNull { it.id == selectedEpisode?.id }
        ?: resumeEpisode?.takeIf { it.seasonNumber == selectedSeason?.seasonNumber }
        ?: selectedSeasonEpisodes.firstOrNull()
    var showMoreInformation by rememberSaveable(series.id) { mutableStateOf(false) }
    val watchLater = rememberAlaaWatchLaterState(contentType = "series", contentId = series.id)
    val startEpisode = resumeEpisode ?: activeEpisode ?: selectedSeasonEpisodes.firstOrNull()

    AlaaDetailBackdrop(imageUrl = series.backdropUrl ?: series.posterUrl) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                AlaaBackButton(onBack = onBack)
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(26.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    AlaaPoster(
                        imageUrl = series.posterUrl ?: series.backdropUrl,
                        title = series.name,
                        status = startEpisode?.let { "الموسم ${it.seasonNumber} · الحلقة ${it.episodeNumber}" } ?: "مسلسل"
                    )
                    Column(
                        modifier = Modifier.weight(0.76f),
                        verticalArrangement = Arrangement.spacedBy(13.dp)
                    ) {
                        AlaaTitleAndFacts(
                            title = series.name,
                            facts = listOfNotNull(
                                series.rating.takeIf { it > 0f }?.let { "★ ${"%.1f".format(it)}" },
                                series.releaseDate?.take(4)?.takeIf { it.isNotBlank() },
                                series.genre?.takeIf { it.isNotBlank() },
                                series.seasons.size.takeIf { it > 0 }?.let { "$it مواسم" },
                                unwatchedEpisodeCount.takeIf { it > 0 }?.let { "$it حلقات غير مشاهدة" }
                            )
                        )
                        AlaaDescription(
                            text = series.plot,
                            expanded = showMoreInformation,
                            additionalFacts = listOfNotNull(
                                series.director?.takeIf { it.isNotBlank() }?.let { "المخرج: $it" },
                                series.cast?.takeIf { it.isNotBlank() }?.let { "الطاقم: $it" },
                                series.variantLabel?.takeIf { it.isNotBlank() }?.let { "الإصدار: $it" }
                            )
                        )
                        AlaaActionRow(
                            primaryLabel = if (resumeEpisode?.watchProgress ?: 0L > 5_000L) "متابعة المشاهدة" else "تشغيل",
                            onPrimaryClick = { startEpisode?.let(onResumeEpisode) },
                            onRestartClick = { startEpisode?.let(onPlayEpisodeFromBeginning) },
                            primaryEnabled = startEpisode != null,
                            isFavorite = series.isFavorite,
                            onToggleFavorite = onToggleFavorite,
                            isWatchLater = watchLater.isSaved,
                            onToggleWatchLater = watchLater::toggle,
                            showMoreInformation = showMoreInformation,
                            onToggleMoreInformation = { showMoreInformation = !showMoreInformation }
                        )
                    }
                    Spacer(modifier = Modifier.weight(0.24f))
                }
            }

            if (series.seasons.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "المواسم والحلقات",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = AlaaThemeColors.TextPrimary
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(series.seasons, key = { it.seasonNumber }) { season ->
                                AlaaSeasonChip(
                                    season = season,
                                    isSelected = season == selectedSeason,
                                    onClick = { onSeasonSelected(season) }
                                )
                            }
                        }
                    }
                }
            }

            selectedSeason?.let { season ->
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(0.56f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "حلقات ${season.name}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AlaaThemeColors.TextPrimary
                            )
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 230.dp, max = 360.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(selectedSeasonEpisodes, key = { it.id }) { episode ->
                                    AlaaEpisodeRow(
                                        episode = episode,
                                        isSelected = episode.id == activeEpisode?.id,
                                        onClick = {
                                            onEpisodeSelected(episode)
                                        }
                                    )
                                }
                            }
                        }
                        AlaaEpisodeInformationPanel(
                            episode = activeEpisode,
                            onPlay = { activeEpisode?.let(onResumeEpisode) },
                            onPlayFromBeginning = { activeEpisode?.let(onPlayEpisodeFromBeginning) },
                            modifier = Modifier.weight(0.44f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AlaaDetailBackdrop(
    imageUrl: String?,
    content: @Composable () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(AlaaThemeColors.Canvas)
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = rememberCrossfadeImageModel(imageUrl),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (maxWidth < 900.dp) 360.dp else 500.dp)
                    .align(Alignment.TopCenter),
                contentScale = ContentScale.Crop
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (maxWidth < 900.dp) 460.dp else 600.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            AlaaThemeColors.Canvas.copy(alpha = 0.98f),
                            AlaaThemeColors.Canvas.copy(alpha = 0.74f),
                            AlaaThemeColors.Canvas.copy(alpha = 0.18f)
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (maxWidth < 900.dp) 560.dp else 660.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, AlaaThemeColors.Canvas)
                    )
                )
        )
        content()
    }
}

@Composable
private fun AlaaBackButton(onBack: () -> Unit) {
    TvButton(
        onClick = onBack,
        colors = ButtonDefaults.colors(
            containerColor = AlaaThemeColors.BrowseRail,
            focusedContainerColor = AlaaThemeColors.SurfaceFocused,
            contentColor = AlaaThemeColors.TextPrimary,
            focusedContentColor = AlaaThemeColors.TextPrimary
        ),
        border = ButtonDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(AlaaThemeDimensions.FocusBorder, AlaaThemeColors.Accent),
                shape = RoundedCornerShape(AlaaThemeDimensions.CornerMedium)
            )
        ),
        shape = ButtonDefaults.shape(shape = RoundedCornerShape(AlaaThemeDimensions.CornerMedium))
    ) {
        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("رجوع")
    }
}

@Composable
private fun AlaaPoster(imageUrl: String?, title: String, status: String) {
    val shape = RoundedCornerShape(AlaaThemeDimensions.CornerLarge)
    Box(
        modifier = Modifier
            .width(220.dp)
            .aspectRatio(2f / 3f)
            .clip(shape)
            .background(AlaaThemeColors.Surface)
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = rememberCrossfadeImageModel(imageUrl),
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = title.take(1).uppercase(),
                color = AlaaThemeColors.TextSecondary,
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Text(
            text = status,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
                .background(AlaaThemeColors.Accent, RoundedCornerShape(8.dp))
                .padding(horizontal = 9.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun AlaaTitleAndFacts(title: String, facts: List<String>) {
    Text(
        text = title,
        style = MaterialTheme.typography.displaySmall,
        color = AlaaThemeColors.TextPrimary,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
    if (facts.isNotEmpty()) {
        Text(
            text = facts.joinToString("   •   "),
            style = MaterialTheme.typography.titleSmall,
            color = AlaaThemeColors.TextSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AlaaDescription(text: String?, expanded: Boolean, additionalFacts: List<String> = emptyList()) {
    val description = text?.takeIf { it.isNotBlank() }
        ?: "لا تتوفر معلومات إضافية لهذا المحتوى من المصدر الحالي."
    val displayedText = if (expanded && additionalFacts.isNotEmpty()) {
        listOf(description, additionalFacts.joinToString("\n")).joinToString("\n\n")
    } else {
        description
    }
    Text(
        text = displayedText,
        style = MaterialTheme.typography.bodyLarge,
        color = AlaaThemeColors.TextSecondary,
        maxLines = if (expanded) 9 else 4,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun AlaaActionRow(
    primaryLabel: String,
    onPrimaryClick: () -> Unit,
    onRestartClick: () -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    isWatchLater: Boolean,
    onToggleWatchLater: () -> Unit,
    showMoreInformation: Boolean,
    onToggleMoreInformation: () -> Unit,
    primaryEnabled: Boolean = true
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TvButton(
            onClick = onPrimaryClick,
            enabled = primaryEnabled,
            colors = ButtonDefaults.colors(
                containerColor = AlaaThemeColors.Accent,
                focusedContainerColor = AlaaThemeColors.AccentStrong,
                contentColor = Color.White,
                focusedContentColor = Color.White
            ),
            shape = ButtonDefaults.shape(shape = RoundedCornerShape(AlaaThemeDimensions.CornerMedium))
        ) {
            Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(7.dp))
            Text(primaryLabel)
        }
        TvButton(
            onClick = onRestartClick,
            enabled = primaryEnabled,
            colors = ButtonDefaults.colors(
                containerColor = AlaaThemeColors.BrowseRail,
                focusedContainerColor = AlaaThemeColors.SurfaceFocused,
                contentColor = AlaaThemeColors.TextPrimary,
                focusedContentColor = AlaaThemeColors.TextPrimary
            ),
            shape = ButtonDefaults.shape(shape = RoundedCornerShape(AlaaThemeDimensions.CornerMedium))
        ) { Text("من البداية") }
        TvButton(
            onClick = onToggleMoreInformation,
            colors = ButtonDefaults.colors(
                containerColor = AlaaThemeColors.BrowseRail,
                focusedContainerColor = AlaaThemeColors.SurfaceFocused,
                contentColor = AlaaThemeColors.TextPrimary,
                focusedContentColor = AlaaThemeColors.TextPrimary
            ),
            shape = ButtonDefaults.shape(shape = RoundedCornerShape(AlaaThemeDimensions.CornerMedium))
        ) { Text(if (showMoreInformation) "معلومات أقل" else "المزيد من المعلومات") }
        TvIconButton(
            onClick = onToggleFavorite,
            colors = ButtonDefaults.colors(
                containerColor = if (isFavorite) AlaaThemeColors.Accent else AlaaThemeColors.BrowseRail,
                focusedContainerColor = AlaaThemeColors.SurfaceFocused,
                contentColor = Color.White,
                focusedContentColor = Color.White
            )
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (isFavorite) "إزالة من المفضلة" else "إضافة إلى المفضلة"
            )
        }
        TvIconButton(
            onClick = onToggleWatchLater,
            colors = ButtonDefaults.colors(
                containerColor = if (isWatchLater) AlaaThemeColors.Accent else AlaaThemeColors.BrowseRail,
                focusedContainerColor = AlaaThemeColors.SurfaceFocused,
                contentColor = Color.White,
                focusedContentColor = Color.White
            )
        ) {
            Icon(
                imageVector = if (isWatchLater) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                contentDescription = if (isWatchLater) "إزالة من المشاهدة لاحقاً" else "إضافة إلى المشاهدة لاحقاً"
            )
        }
    }
}

private class AlaaWatchLaterState(
    initialValue: Boolean,
    private val persist: (Boolean) -> Unit
) {
    var isSaved by mutableStateOf(initialValue)
        private set

    fun toggle() {
        isSaved = !isSaved
        persist(isSaved)
    }
}

@Composable
private fun rememberAlaaWatchLaterState(contentType: String, contentId: Long): AlaaWatchLaterState {
    val context = LocalContext.current
    val preferences = remember(context) {
        context.getSharedPreferences("alaa_watch_later", android.content.Context.MODE_PRIVATE)
    }
    val key = remember(contentType, contentId) { "$contentType:$contentId" }
    return remember(key, preferences) {
        AlaaWatchLaterState(
            initialValue = preferences.getBoolean(key, false),
            persist = { value -> preferences.edit().putBoolean(key, value).apply() }
        )
    }
}

@Composable
private fun AlaaSeasonChip(season: Season, isSelected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(AlaaThemeDimensions.CornerMedium)
    TvClickableSurface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected) AlaaThemeColors.AccentMuted else AlaaThemeColors.BrowseRail,
            focusedContainerColor = AlaaThemeColors.SurfaceFocused,
            contentColor = if (isSelected) AlaaThemeColors.AccentStrong else AlaaThemeColors.TextPrimary,
            focusedContentColor = AlaaThemeColors.TextPrimary
        ),
        border = ClickableSurfaceDefaults.border(
            border = Border(
                border = BorderStroke(1.dp, if (isSelected) AlaaThemeColors.Accent else AlaaThemeColors.Outline),
                shape = shape
            ),
            focusedBorder = Border(
                border = BorderStroke(AlaaThemeDimensions.FocusBorder, AlaaThemeColors.Accent),
                shape = shape
            )
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = AlaaThemeFocus.FocusedScale)
    ) {
        Text(
            text = season.name,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 11.dp),
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AlaaEpisodeRow(episode: Episode, isSelected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(AlaaThemeDimensions.CornerMedium)
    TvClickableSurface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected) AlaaThemeColors.AccentMuted else AlaaThemeColors.BrowseRail,
            focusedContainerColor = AlaaThemeColors.SurfaceFocused,
            contentColor = AlaaThemeColors.TextPrimary,
            focusedContentColor = AlaaThemeColors.TextPrimary
        ),
        border = ClickableSurfaceDefaults.border(
            border = Border(
                border = BorderStroke(1.dp, if (isSelected) AlaaThemeColors.Accent else Color.Transparent),
                shape = shape
            ),
            focusedBorder = Border(
                border = BorderStroke(AlaaThemeDimensions.FocusBorder, AlaaThemeColors.Accent),
                shape = shape
            )
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null, tint = AlaaThemeColors.Accent)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "الحلقة ${episode.episodeNumber} · ${episode.title}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val facts = listOfNotNull(
                    episode.releaseDate?.takeIf { it.isNotBlank() },
                    episode.duration?.takeIf { it.isNotBlank() },
                    episode.watchProgress.takeIf { it > 5_000L }?.let { "قيد المشاهدة" }
                )
                if (facts.isNotEmpty()) {
                    Text(
                        text = facts.joinToString(" · "),
                        style = MaterialTheme.typography.labelSmall,
                        color = AlaaThemeColors.TextTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun AlaaEpisodeInformationPanel(
    episode: Episode?,
    onPlay: () -> Unit,
    onPlayFromBeginning: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(AlaaThemeDimensions.CornerLarge)
    Column(
        modifier = modifier
            .heightIn(min = 230.dp, max = 360.dp)
            .clip(shape)
            .background(AlaaThemeColors.Surface)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "معلومات الحلقة",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AlaaThemeColors.TextPrimary
        )
        if (episode == null) {
            Text(
                text = "اختر حلقة لعرض معلوماتها.",
                style = MaterialTheme.typography.bodyMedium,
                color = AlaaThemeColors.TextSecondary
            )
            return@Column
        }
        Text(
            text = "الحلقة ${episode.episodeNumber} · ${episode.title}",
            style = MaterialTheme.typography.titleMedium,
            color = AlaaThemeColors.TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        val metadata = listOfNotNull(
            "الموسم ${episode.seasonNumber}",
            episode.releaseDate?.takeIf { it.isNotBlank() },
            episode.duration?.takeIf { it.isNotBlank() },
            episode.rating.takeIf { it > 0f }?.let { "★ ${"%.1f".format(it)}" }
        )
        Text(
            text = metadata.joinToString(" · "),
            style = MaterialTheme.typography.labelMedium,
            color = AlaaThemeColors.TextSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = episode.plot?.takeIf { it.isNotBlank() } ?: "لا يتوفر وصف لهذه الحلقة من المصدر الحالي.",
            style = MaterialTheme.typography.bodySmall,
            color = AlaaThemeColors.TextSecondary,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TvButton(
                onClick = onPlay,
                colors = ButtonDefaults.colors(containerColor = AlaaThemeColors.Accent, contentColor = Color.White)
            ) {
                Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(5.dp))
                Text("تشغيل")
            }
            TvButton(
                onClick = onPlayFromBeginning,
                colors = ButtonDefaults.colors(containerColor = AlaaThemeColors.BrowseRail, contentColor = AlaaThemeColors.TextPrimary)
            ) { Text("من البداية") }
        }
    }
}
