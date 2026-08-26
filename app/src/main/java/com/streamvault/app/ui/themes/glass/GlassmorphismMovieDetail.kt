package com.streamvault.app.ui.themes.glass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.domain.model.Movie

/** تفاصيل فيلم Glassmorphism: طبقات شفافة مع منطقة أوامر، من دون تكرار أي حالة تشغيل. */
@Composable
internal fun GlassmorphismMovieDetail(
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
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(GlassCanvas).padding(horizontal = 46.dp, vertical = 30.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item("glass_movie_back") { GlassMovieAction("← BACK TO LIBRARY", onBack) }
        item("glass_movie_hero") {
            GlassMoviePane {
                Text("FILM GLASS / ${movie.year?.toString() ?: "LIBRARY"}", style = MaterialTheme.typography.labelLarge, color = GlassAccent)
                Text(movie.name, style = MaterialTheme.typography.displayMedium, color = GlassText, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(
                    listOfNotNull(movie.releaseDate, movie.duration, movie.genre).filter { it.isNotBlank() }.joinToString(" · "),
                    style = MaterialTheme.typography.titleSmall,
                    color = GlassMuted
                )
                Text(
                    movie.plot?.takeIf { it.isNotBlank() } ?: "No description is available for this title.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = GlassText,
                    maxLines = 7,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        item("glass_movie_primary_actions") {
            GlassMoviePane {
                Text("WATCH CONTROLS", style = MaterialTheme.typography.labelLarge, color = GlassAccent)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    GlassMovieAction(if (hasResume) "RESUME ${formatGlassResume(resumePositionMs)}" else "PLAY NOW", onPlay, Modifier.weight(1f))
                    GlassMovieAction(if (movie.isFavorite) "SAVED" else "SAVE", onToggleFavorite, Modifier.weight(1f))
                    onPlayTrailer?.let { trailer -> GlassMovieAction("TRAILER", trailer, Modifier.weight(1f)) }
                }
            }
        }
        item("glass_movie_utility_actions") {
            GlassMoviePane {
                Text("STREAM OPTIONS", style = MaterialTheme.typography.labelLarge, color = GlassAccent)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlassMovieAction("COPY LINK", onCopyUrl, Modifier.weight(1f))
                    GlassMovieAction("DOWNLOAD", onDownload, Modifier.weight(1f))
                    GlassMovieAction(if (isCasting) "CASTING" else "CAST", onCast, Modifier.weight(1f))
                }
            }
        }
        if (movie.variants.size > 1) {
            item("glass_movie_variants") {
                GlassMoviePane {
                    Text("AVAILABLE VERSIONS", style = MaterialTheme.typography.labelLarge, color = GlassAccent)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        items(movie.variants, key = { it.rawMovieId }) { variant ->
                            GlassMovieAction(variant.label, { onSelectVariant(variant.rawMovieId) })
                        }
                    }
                }
            }
        }
        if (relatedContent.isNotEmpty()) {
            item("glass_movie_related_label") { Text("RELATED TITLES", style = MaterialTheme.typography.labelLarge, color = GlassAccent) }
            items(relatedContent.take(8), key = { "glass_related_${it.id}" }) { related ->
                GlassMovieAction(related.name, { onRelatedClick(related) }, Modifier.fillMaxWidth(), related.genre ?: related.year?.toString().orEmpty())
            }
        }
    }
}

@Composable
private fun GlassMovieAction(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, supporting: String = "") {
    val shape = RoundedCornerShape(18.dp)
    TvClickableSurface(
        onClick = onClick,
        modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = GlassPane, focusedContainerColor = GlassPaneFocused, contentColor = GlassText, focusedContentColor = GlassText),
        border = ClickableSurfaceDefaults.border(
            border = Border(border = BorderStroke(1.dp, GlassRule), shape = shape),
            focusedBorder = Border(border = BorderStroke(2.dp, GlassFocus), shape = shape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.03f)
    ) {
        Column(Modifier.padding(horizontal = 17.dp, vertical = 13.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (supporting.isNotBlank()) Text(supporting, style = MaterialTheme.typography.bodySmall, color = GlassMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun GlassMoviePane(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().background(GlassPane, RoundedCornerShape(26.dp)).padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
        content = content
    )
}

private fun formatGlassResume(positionMs: Long): String {
    val totalSeconds = (positionMs / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    return if (hours > 0) "%d:%02d".format(hours, minutes) else "%dM".format(minutes)
}
