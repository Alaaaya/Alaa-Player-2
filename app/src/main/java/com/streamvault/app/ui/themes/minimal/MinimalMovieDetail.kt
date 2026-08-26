package com.streamvault.app.ui.themes.minimal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.domain.model.Movie

@Composable
internal fun MinimalMovieDetail(
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
    LazyColumn(modifier = Modifier.fillMaxSize().background(MinimalCanvas), contentPadding = PaddingValues(38.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { MinimalDetailAction("← Back", onBack) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("FILM / ${movie.year ?: "LIBRARY"}", style = MaterialTheme.typography.labelLarge, color = MinimalMuted)
                Text(movie.name, style = MaterialTheme.typography.displayMedium, color = MinimalText, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(listOfNotNull(movie.releaseDate, movie.duration, movie.genre).filter { it.isNotBlank() }.joinToString(" · "), style = MaterialTheme.typography.titleSmall, color = MinimalMuted)
                Text(movie.plot?.takeIf { it.isNotBlank() } ?: "No description is available.", style = MaterialTheme.typography.bodyLarge, color = MinimalText, maxLines = 7, overflow = TextOverflow.Ellipsis)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MinimalDetailAction(if (hasResume) "Resume" else "Play", onPlay)
                MinimalDetailAction(if (movie.isFavorite) "Saved" else "Save", onToggleFavorite)
                onPlayTrailer?.let { MinimalDetailAction("Trailer", it) }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MinimalDetailAction("Copy link", onCopyUrl)
                MinimalDetailAction("Download", onDownload)
                MinimalDetailAction(if (isCasting) "Casting" else "Cast", onCast)
            }
        }
        if (movie.variants.size > 1) item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("AVAILABLE VERSIONS", style = MaterialTheme.typography.labelLarge, color = MinimalMuted)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(5.dp)) { items(movie.variants, key = { it.rawMovieId }) { variant -> MinimalDetailAction(variant.label) { onSelectVariant(variant.rawMovieId) } } }
            }
        }
        if (relatedContent.isNotEmpty()) item {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("RELATED", style = MaterialTheme.typography.labelLarge, color = MinimalMuted)
                relatedContent.take(8).forEach { related -> MinimalDetailAction(related.name) { onRelatedClick(related) } }
            }
        }
    }
}

@Composable
private fun MinimalDetailAction(label: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(0.dp)
    TvClickableSurface(onClick = onClick, modifier = Modifier, shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = Color.Transparent, focusedContainerColor = MinimalPaper, contentColor = MinimalText, focusedContentColor = MinimalText), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(1.dp, MinimalFocus), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = MinimalFocusedScale)) {
        Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
