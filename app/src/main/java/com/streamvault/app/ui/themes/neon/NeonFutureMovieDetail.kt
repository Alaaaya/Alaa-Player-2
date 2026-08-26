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
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.streamvault.app.ui.design.requestFocusSafely
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.app.util.formatPositionMs
import com.streamvault.domain.model.Movie

/** Neon Future movie detail is presentation-only and retains every existing movie detail callback. */
@Composable
internal fun NeonFutureMovieDetail(
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
    LaunchedEffect(movie.id) { playFocusRequester.requestFocusSafely(tag = "NeonFutureMovieDetail", target = "Play film") }
    Box(modifier = Modifier.fillMaxSize().background(NeonCanvas)) {
        AsyncImage(movie.backdropUrl ?: movie.posterUrl, null, Modifier.fillMaxWidth().height(410.dp), contentScale = ContentScale.Crop)
        Box(modifier = Modifier.fillMaxWidth().height(430.dp).background(Brush.verticalGradient(listOf(NeonCanvas.copy(alpha = .18f), NeonCanvas.copy(alpha = .76f), NeonCanvas))))
        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(22.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 34.dp, top = 28.dp, end = 34.dp, bottom = 34.dp)) {
            item("neon_movie_detail_back") { NeonFutureMovieDetailAction("← ARCHIVE", NeonCyan, onBack) }
            item("neon_movie_detail_hero") {
                Row(horizontalArrangement = Arrangement.spacedBy(22.dp), verticalAlignment = Alignment.Bottom) {
                    AsyncImage(movie.posterUrl ?: movie.backdropUrl, movie.name, Modifier.width(208.dp).height(318.dp).clip(RoundedCornerShape(10.dp)), contentScale = ContentScale.Crop)
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("FILM NODE / ${movie.year ?: "LIBRARY"}", style = MaterialTheme.typography.labelLarge, color = NeonPink, fontWeight = FontWeight.Black)
                        Text(movie.name, style = MaterialTheme.typography.displayMedium, color = NeonText, fontWeight = FontWeight.Black, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(listOfNotNull(movie.releaseDate, movie.duration, movie.genre).filter { it.isNotBlank() }.joinToString(" / ").ifBlank { "Film metadata node" }, style = MaterialTheme.typography.titleSmall, color = NeonCyan)
                        Text(movie.plot?.takeIf { it.isNotBlank() } ?: "No synopsis is available from this active catalogue node.", style = MaterialTheme.typography.bodyLarge, color = NeonMuted, maxLines = 5, overflow = TextOverflow.Ellipsis)
                        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                            NeonFutureMovieDetailAction(if (hasResume) "RESUME ${formatPositionMs(resumePositionMs)}" else "PLAY", NeonLime, onPlay, Modifier.focusRequester(playFocusRequester))
                            NeonFutureMovieDetailAction(if (movie.isFavorite) "SAVED" else "SAVE", NeonPink, onToggleFavorite)
                            onPlayTrailer?.let { NeonFutureMovieDetailAction("TRAILER", NeonCyan, it) }
                        }
                    }
                    NeonFutureMovieDataPanel(movie, Modifier.width(230.dp))
                }
            }
            item("neon_movie_detail_utilities") {
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    NeonFutureMovieDetailAction("COPY URL", NeonCyan, onCopyUrl)
                    NeonFutureMovieDetailAction("DOWNLOAD", NeonLime, onDownload)
                    NeonFutureMovieDetailAction(if (isCasting) "CAST ACTIVE" else "CAST", NeonPink, onCast)
                }
            }
            if (movie.variants.size > 1) item("neon_movie_detail_variants") {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    NeonFutureMovieDetailHeading("PROVIDER VARIANTS", "Select an available stream node")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(movie.variants, key = { it.rawMovieId }) { variant ->
                            NeonFutureMovieDetailAction(
                                label = variant.label,
                                tone = if (variant.rawMovieId == (movie.selectedVariantId ?: movie.id)) NeonLime else NeonMuted,
                                onClick = { onSelectVariant(variant.rawMovieId) }
                            )
                        }
                    }
                }
            }
            if (relatedContent.isNotEmpty()) item("neon_movie_detail_related") {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    NeonFutureMovieDetailHeading("RELATED SIGNALS", "More films delivered by this provider")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(relatedContent, key = { it.id }) { related ->
                            val shape = RoundedCornerShape(8.dp)
                            TvClickableSurface(onClick = { onRelatedClick(related) }, modifier = Modifier.width(150.dp), shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = NeonPanel, focusedContainerColor = NeonPanelRaised, contentColor = NeonText, focusedContentColor = NeonText), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, NeonCyan), shape = shape))) {
                                Column { AsyncImage(related.posterUrl ?: related.backdropUrl, related.name, Modifier.fillMaxWidth().height(194.dp), contentScale = ContentScale.Crop); Text(related.name, Modifier.padding(9.dp), style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NeonFutureMovieDataPanel(movie: Movie, modifier: Modifier) {
    Column(modifier = modifier.background(NeonPanel, RoundedCornerShape(10.dp)).padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("NODE READOUT", style = MaterialTheme.typography.labelLarge, color = NeonCyan, fontWeight = FontWeight.Black)
        Text("RATING ${if (movie.rating > 0f) "%.1f".format(movie.rating) else "—"}", style = MaterialTheme.typography.titleMedium, color = NeonLime)
        Text("ID ${movie.id}", style = MaterialTheme.typography.labelSmall, color = NeonMuted)
        Text(movie.genre ?: "GENRE UNKNOWN", style = MaterialTheme.typography.labelMedium, color = NeonPink, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun NeonFutureMovieDetailHeading(title: String, subtitle: String) { Column(verticalArrangement = Arrangement.spacedBy(3.dp)) { Text(title, style = MaterialTheme.typography.titleLarge, color = NeonText, fontWeight = FontWeight.Black); Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = NeonMuted) } }

@Composable
private fun NeonFutureMovieDetailAction(label: String, tone: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(7.dp)
    TvClickableSurface(onClick = onClick, modifier = modifier, shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = NeonPanel, focusedContainerColor = tone.copy(alpha = .25f), contentColor = tone, focusedContentColor = NeonText), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, tone), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.03f)) {
        Text(label, Modifier.padding(horizontal = 13.dp, vertical = 10.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
