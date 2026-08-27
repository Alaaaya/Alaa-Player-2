package com.streamvault.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.app.ui.theme.AlaaThemeColors
import com.streamvault.app.ui.theme.AlaaThemeDimensions
import com.streamvault.app.ui.theme.AlaaThemeFocus
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.Series

/**
 * بطاقات مكتبة ALAA فقط. تحافظ على بيانات IPTV الأصلية وتستعمل تركيز Android TV نفسه،
 * لكنها تقدم الملصق والعنوان والبيانات في بلاطة مريحة للقراءة داخل شبكة المكتبة المستقلة.
 */
@Composable
fun AlaaMovieBrowseCard(
    movie: Movie,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isLocked: Boolean,
    isReorderMode: Boolean,
    isDragging: Boolean,
    modifier: Modifier = Modifier
) {
    val metadata = listOfNotNull(
        movie.year?.takeIf { it.isNotBlank() },
        movie.rating.takeIf { it > 0f }?.let { "★ ${"%.1f".format(it)}" },
        movie.duration?.takeIf { it.isNotBlank() }
    ).joinToString(" · ")
    AlaaVodBrowseCard(
        imageUrl = movie.posterUrl,
        title = movie.name,
        metadata = metadata,
        isLocked = isLocked,
        isReorderMode = isReorderMode,
        isDragging = isDragging,
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier
    )
}

@Composable
fun AlaaSeriesBrowseCard(
    series: Series,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isLocked: Boolean,
    isReorderMode: Boolean,
    isDragging: Boolean,
    modifier: Modifier = Modifier
) {
    val episodeCount = series.seasons.sumOf { season ->
        season.episodeCount.takeIf { it > 0 } ?: season.episodes.size
    }
    val metadata = listOfNotNull(
        series.releaseDate?.take(4)?.takeIf { it.isNotBlank() },
        series.rating.takeIf { it > 0f }?.let { "★ ${"%.1f".format(it)}" },
        series.seasons.size.takeIf { it > 0 }?.let { "$it seasons" },
        episodeCount.takeIf { it > 0 }?.let { "$it episodes" }
    ).joinToString(" · ")
    AlaaVodBrowseCard(
        imageUrl = series.posterUrl,
        title = series.name,
        metadata = metadata,
        isLocked = isLocked,
        isReorderMode = isReorderMode,
        isDragging = isDragging,
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier
    )
}

@Composable
private fun AlaaVodBrowseCard(
    imageUrl: String?,
    title: String,
    metadata: String,
    isLocked: Boolean,
    isReorderMode: Boolean,
    isDragging: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier
) {
    val shape = RoundedCornerShape(AlaaThemeDimensions.CornerMedium)
    TvClickableSurface(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = AlaaThemeColors.Surface,
            focusedContainerColor = AlaaThemeColors.SurfaceFocused,
            contentColor = AlaaThemeColors.TextPrimary,
            focusedContentColor = AlaaThemeColors.TextPrimary
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = androidx.compose.foundation.BorderStroke(
                    if (isDragging) 3.dp else AlaaThemeDimensions.FocusBorder,
                    if (isDragging) AlaaThemeColors.AccentStrong else AlaaThemeColors.Accent
                ),
                shape = shape
            )
        ),
        scale = ClickableSurfaceDefaults.scale(
            focusedScale = if (isReorderMode && !isDragging) 1f else AlaaThemeFocus.FocusedScale,
            pressedScale = AlaaThemeFocus.PressedScale
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .clip(shape)
                    .background(AlaaThemeColors.CanvasRaised),
                contentAlignment = Alignment.Center
            ) {
                if (!imageUrl.isNullOrBlank() && !isLocked) {
                    AsyncImage(
                        model = rememberCrossfadeImageModel(imageUrl),
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = if (isLocked) "LOCKED" else title.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = AlaaThemeColors.TextSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f))
                            )
                        )
                        .padding(top = 24.dp, bottom = 7.dp, start = 8.dp, end = 8.dp)
                ) {
                    Text(
                        text = if (isLocked) "PROTECTED" else title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = AlaaThemeColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Text(
                text = if (isLocked) "Protected content" else metadata.ifBlank { "Library item" },
                style = MaterialTheme.typography.labelSmall,
                color = if (isLocked) AlaaThemeColors.AccentStrong else AlaaThemeColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .padding(bottom = 3.dp)
            )
        }
    }
}
