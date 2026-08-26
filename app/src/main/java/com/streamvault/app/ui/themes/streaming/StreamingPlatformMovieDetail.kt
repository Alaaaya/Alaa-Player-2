package com.streamvault.app.ui.themes.streaming

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.domain.model.Movie

/** تفاصيل Streaming Platform: صفحة عنوان غامرة ورفوف إجراءات، وليست لوحة Glass أو Cinematic. */
@Composable
internal fun StreamingPlatformMovieDetail(
    movie: Movie, hasResume: Boolean, resumePositionMs: Long, isCasting: Boolean, relatedContent: List<Movie>,
    onPlay: () -> Unit, onCopyUrl: () -> Unit, onDownload: () -> Unit, onCast: () -> Unit, onToggleFavorite: () -> Unit,
    onSelectVariant: (Long) -> Unit, onRelatedClick: (Movie) -> Unit, onBack: () -> Unit, onPlayTrailer: (() -> Unit)?
) {
    LazyColumn(modifier = Modifier.fillMaxSize().background(StreamingCanvas).padding(horizontal = 38.dp, vertical = 28.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item("streaming_movie_back") { StreamingMovieDetailButton("← BACK TO FILMS", onBack) }
        item("streaming_movie_hero") {
            Column(Modifier.fillMaxWidth().background(StreamingPanel, RoundedCornerShape(24.dp)).padding(30.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("FEATURED FILM / ${movie.year?.toString() ?: "LIBRARY"}", style = MaterialTheme.typography.labelLarge, color = StreamingAccent)
                Text(movie.name, style = MaterialTheme.typography.displayMedium, color = StreamingText, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(listOfNotNull(movie.releaseDate, movie.duration, movie.genre).filter { it.isNotBlank() }.joinToString(" · "), style = MaterialTheme.typography.titleSmall, color = StreamingMuted)
                Text(movie.plot?.takeIf { it.isNotBlank() } ?: "No description is available for this title.", style = MaterialTheme.typography.bodyLarge, color = StreamingText, maxLines = 7, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StreamingMovieDetailButton(if (hasResume) "RESUME ${formatStreamingResume(resumePositionMs)}" else "PLAY NOW", onPlay, Modifier.weight(1f), primary = true)
                    StreamingMovieDetailButton(if (movie.isFavorite) "SAVED" else "SAVE", onToggleFavorite, Modifier.weight(1f))
                    onPlayTrailer?.let { StreamingMovieDetailButton("TRAILER", it, Modifier.weight(1f)) }
                }
            }
        }
        item("streaming_movie_actions") {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("STREAM OPTIONS", style = MaterialTheme.typography.titleLarge, color = StreamingText)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StreamingMovieDetailButton("COPY LINK", onCopyUrl, Modifier.weight(1f))
                    StreamingMovieDetailButton("DOWNLOAD", onDownload, Modifier.weight(1f))
                    StreamingMovieDetailButton(if (isCasting) "CASTING" else "CAST", onCast, Modifier.weight(1f))
                }
            }
        }
        if (movie.variants.size > 1) item("streaming_movie_variants") {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("AVAILABLE VERSIONS", style = MaterialTheme.typography.titleLarge, color = StreamingText)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) { items(movie.variants, key = { it.rawMovieId }) { variant -> StreamingMovieDetailButton(variant.label, { onSelectVariant(variant.rawMovieId) }) } }
            }
        }
        if (relatedContent.isNotEmpty()) item("streaming_movie_related") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("MORE LIKE THIS", style = MaterialTheme.typography.titleLarge, color = StreamingText)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) { items(relatedContent.take(12), key = { it.id }) { related -> StreamingMovieDetailButton(related.name, { onRelatedClick(related) }, supporting = related.genre ?: related.year?.toString().orEmpty()) } }
            }
        }
    }
}

@Composable private fun StreamingMovieDetailButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, primary: Boolean = false, supporting: String = "") { val shape = RoundedCornerShape(14.dp); TvClickableSurface(onClick = onClick, modifier = modifier, shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = if (primary) StreamingPanelFocused else StreamingPanel, focusedContainerColor = StreamingPanelFocused, contentColor = StreamingText, focusedContentColor = StreamingText), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, StreamingFocus), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.03f)) { Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) { Text(label, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis); if (supporting.isNotBlank()) Text(supporting, style = MaterialTheme.typography.bodySmall, color = StreamingMuted, maxLines = 1, overflow = TextOverflow.Ellipsis) } } }
private fun formatStreamingResume(positionMs: Long): String { val seconds = (positionMs / 1000).coerceAtLeast(0); val hours = seconds / 3600; val minutes = (seconds % 3600) / 60; return if (hours > 0) "%d:%02d".format(hours, minutes) else "%dm".format(minutes) }
