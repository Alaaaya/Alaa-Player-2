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
import androidx.compose.runtime.remember
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
import com.streamvault.domain.model.Movie

/** Cinematic movie detail is presentation-only and delegates all actions to MovieDetailViewModel callbacks. */
@Composable
internal fun CinematicMovieDetail(
    movie: Movie,
    hasResume: Boolean,
    resumePositionMs: Long,
    isCasting: Boolean,
    relatedContent: List<Movie>,
    onPlay: () -> Unit,
    onCopyUrl: () -> Unit,
    onDownload: () -> Unit,
    onCast: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSelectVariant: (Long) -> Unit,
    onRelatedClick: (Movie) -> Unit,
    onBack: () -> Unit,
    onPlayTrailer: (() -> Unit)?
) {
    val playFocusRequester = remember { FocusRequester() }
    LaunchedEffect(movie.id) {
        playFocusRequester.requestFocusSafely(tag = "CinematicMovieDetail", target = "Play movie")
    }
    val shape = RoundedCornerShape(28.dp)

    Box(modifier = Modifier.fillMaxSize().background(CinematicCanvas)) {
        AsyncImage(
            model = movie.backdropUrl ?: movie.posterUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(520.dp),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(560.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(CinematicCanvas.copy(alpha = 0.22f), CinematicCanvas.copy(alpha = 0.78f), CinematicCanvas)
                    )
                )
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 46.dp, top = 34.dp, end = 46.dp, bottom = 42.dp),
            verticalArrangement = Arrangement.spacedBy(26.dp)
        ) {
            item(key = "cinematic_movie_back") {
                CinematicDetailAction(label = "BACK TO LIBRARY", tone = CinematicMuted, onClick = onBack)
            }
            item(key = "cinematic_movie_hero") {
                Row(horizontalArrangement = Arrangement.spacedBy(28.dp), verticalAlignment = Alignment.Bottom) {
                    Surface(
                        modifier = Modifier.width(238.dp).height(356.dp),
                        shape = shape,
                        colors = SurfaceDefaults.colors(containerColor = CinematicPanel),
                        border = Border(border = BorderStroke(1.dp, CinematicGold.copy(alpha = 0.55f)), shape = shape)
                    ) {
                        AsyncImage(
                            model = movie.posterUrl ?: movie.backdropUrl,
                            contentDescription = movie.name,
                            modifier = Modifier.fillMaxSize().clip(shape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "FEATURE FILM // ${movie.year?.takeIf { it.isNotBlank() } ?: "LIBRARY"}",
                            style = MaterialTheme.typography.labelMedium,
                            color = CinematicGold,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = movie.name,
                            style = MaterialTheme.typography.displayMedium,
                            color = CinematicText,
                            fontWeight = FontWeight.Black,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = listOfNotNull(movie.releaseDate, movie.duration, movie.genre)
                                .filter { it.isNotBlank() }
                                .joinToString("  ·  ")
                                .ifBlank { "FILM" },
                            style = MaterialTheme.typography.titleSmall,
                            color = CinematicMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (movie.rating > 0f) {
                            CinematicDetailMarker("RATING ${"%.1f".format(movie.rating)} / 10", CinematicGold)
                        }
                        Text(
                            text = movie.plot?.takeIf { it.isNotBlank() }
                                ?: "No synopsis is available from this catalogue entry.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = CinematicMuted,
                            maxLines = 5,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            CinematicDetailAction(
                                label = if (hasResume) "RESUME · ${formatPositionMs(resumePositionMs)}" else "PLAY FEATURE",
                                tone = CinematicWine,
                                modifier = Modifier.focusRequester(playFocusRequester),
                                onClick = onPlay
                            )
                            CinematicDetailAction(
                                label = if (movie.isFavorite) "SAVED" else "SAVE",
                                tone = CinematicGold,
                                onClick = onToggleFavorite
                            )
                            if (onPlayTrailer != null) {
                                CinematicDetailAction(label = "TRAILER", tone = Color(0xFF9DA3FF), onClick = onPlayTrailer)
                            }
                        }
                    }
                }
            }
            item(key = "cinematic_movie_utility_actions") {
                CinematicDetailActionRow(
                    isCasting = isCasting,
                    onCopyUrl = onCopyUrl,
                    onDownload = onDownload,
                    onCast = onCast
                )
            }
            if (movie.variants.size > 1) {
                item(key = "cinematic_movie_versions") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CinematicDetailSectionTitle("AVAILABLE CUTS", "Select the preferred provider variant")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(movie.variants, key = { it.rawMovieId }) { variant ->
                                val selected = variant.rawMovieId == (movie.selectedVariantId ?: movie.id)
                                CinematicDetailAction(
                                    label = variant.label,
                                    tone = if (selected) CinematicWine else CinematicMuted,
                                    onClick = { onSelectVariant(variant.rawMovieId) }
                                )
                            }
                        }
                    }
                }
            }
            if (relatedContent.isNotEmpty()) {
                item(key = "cinematic_movie_related") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CinematicDetailSectionTitle("MORE FROM THE CATALOGUE", "Related films available through the selected provider")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            items(relatedContent, key = { it.id }) { related ->
                                CinematicRelatedMovieCard(movie = related, onClick = { onRelatedClick(related) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CinematicDetailActionRow(
    isCasting: Boolean,
    onCopyUrl: () -> Unit,
    onDownload: () -> Unit,
    onCast: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        CinematicDetailAction(label = "COPY STREAM URL", tone = CinematicMuted, onClick = onCopyUrl)
        CinematicDetailAction(label = "DOWNLOAD", tone = CinematicGold, onClick = onDownload)
        CinematicDetailAction(
            label = if (isCasting) "CAST CONNECTING" else "CAST FEATURE",
            tone = if (isCasting) CinematicMuted else Color(0xFF9DA3FF),
            onClick = onCast
        )
    }
}

@Composable
private fun CinematicDetailSectionTitle(title: String, detail: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleLarge, color = CinematicText, fontWeight = FontWeight.Black)
        Text(text = detail, style = MaterialTheme.typography.bodyMedium, color = CinematicMuted)
    }
}

@Composable
private fun CinematicRelatedMovieCard(movie: Movie, onClick: () -> Unit) {
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
                model = movie.posterUrl ?: movie.backdropUrl,
                contentDescription = movie.name,
                modifier = Modifier.fillMaxWidth().height(224.dp).clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale = ContentScale.Crop
            )
            Text(
                text = movie.name,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CinematicDetailMarker(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        colors = SurfaceDefaults.colors(containerColor = color.copy(alpha = 0.17f)),
        border = Border(border = BorderStroke(1.dp, color), shape = RoundedCornerShape(999.dp))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun CinematicDetailAction(
    label: String,
    tone: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 17.dp, vertical = 11.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
