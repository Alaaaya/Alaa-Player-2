package com.streamvault.app.ui.themes.redcinema

/** Red Cinema detail contract: a feature file with a programme card, ticket actions, versions, and related screenings. */

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import com.streamvault.app.ui.theme.LocalThemePresentation
import com.streamvault.domain.model.Movie

@Composable
internal fun RedCinemaFeatureFile(movie: Movie, hasResume: Boolean, resumePositionMs: Long, isCasting: Boolean, relatedContent: List<Movie>, onPlay: () -> Unit, onCopyUrl: () -> Unit, onDownload: () -> Unit, onCast: () -> Unit, onToggleFavorite: () -> Unit, onSelectVariant: (Long) -> Unit, onRelatedClick: (Movie) -> Unit, onBack: () -> Unit, onPlayTrailer: (() -> Unit)?) {
    val s = LocalThemePresentation.current.surfaces
    LazyColumn(Modifier.fillMaxSize().background(s.canvas).padding(28.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
        item("red_cinema_feature_return") { RedCinemaFeatureTicket("← RETURN TO PROGRAMME", onBack) }
        item("red_cinema_feature_card") { Column(Modifier.fillMaxWidth().background(s.browseContent, RoundedCornerShape(2.dp)).padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("FEATURE FILE · ${movie.year?.toString() ?: "LIBRARY"}", style = MaterialTheme.typography.labelMedium, color = s.accent); Text(movie.name, style = MaterialTheme.typography.displayMedium, maxLines = 2, overflow = TextOverflow.Ellipsis); Text(listOfNotNull(movie.releaseDate, movie.duration, movie.genre).filter { it.isNotBlank() }.joinToString(" · "), style = MaterialTheme.typography.titleSmall, color = s.textSecondary); Text(movie.plot?.takeIf { it.isNotBlank() } ?: "No programme notes are available for this feature.", style = MaterialTheme.typography.bodyLarge, color = s.textPrimary, maxLines = 7, overflow = TextOverflow.Ellipsis); Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { RedCinemaFeatureTicket(if (hasResume) "RESUME ${redCinemaResume(resumePositionMs)}" else "BEGIN FEATURE", onPlay, Modifier.weight(1f), true); RedCinemaFeatureTicket(if (movie.isFavorite) "RESERVED" else "RESERVE SEAT", onToggleFavorite, Modifier.weight(1f)); onPlayTrailer?.let { RedCinemaFeatureTicket("TRAILER", it, Modifier.weight(1f)) } } } }
        item("red_cinema_feature_tools") { Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { Text("BOX OFFICE", style = MaterialTheme.typography.titleLarge, color = s.accent); Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { RedCinemaFeatureTicket("COPY TICKET", onCopyUrl, Modifier.weight(1f)); RedCinemaFeatureTicket("DOWNLOAD PRINT", onDownload, Modifier.weight(1f)); RedCinemaFeatureTicket(if (isCasting) "CASTING" else "CAST", onCast, Modifier.weight(1f)) } } }
        if (movie.variants.size > 1) item("red_cinema_feature_versions") { Column(verticalArrangement = Arrangement.spacedBy(5.dp)) { Text("ALTERNATE PRINTS", style = MaterialTheme.typography.titleLarge, color = s.accent); movie.variants.forEach { variant -> RedCinemaFeatureTicket(variant.label, { onSelectVariant(variant.rawMovieId) }, supporting = "Select print") } } }
        if (relatedContent.isNotEmpty()) item("red_cinema_feature_related") { Column(verticalArrangement = Arrangement.spacedBy(5.dp)) { Text("NEXT SCREENINGS", style = MaterialTheme.typography.titleLarge, color = s.accent); relatedContent.take(12).forEach { related -> RedCinemaFeatureTicket(related.name, { onRelatedClick(related) }, supporting = related.genre ?: related.year?.toString().orEmpty()) } } }
    }
}

@Composable
private fun RedCinemaFeatureTicket(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, primary: Boolean = false, supporting: String = "") { val s = LocalThemePresentation.current.surfaces; val shape = RoundedCornerShape(2.dp); TvClickableSurface(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = if (primary) s.selectedAccent else s.browseContent, focusedContainerColor = s.focusedSurface, contentColor = s.textPrimary, focusedContentColor = s.textPrimary), border = ClickableSurfaceDefaults.border(border = Border(border = BorderStroke(1.dp, if (primary) s.accent else s.textSecondary.copy(alpha = .26f)), shape = shape), focusedBorder = Border(border = BorderStroke(2.dp, s.accent), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.018f)) { Column(Modifier.padding(horizontal = 14.dp, vertical = 11.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) { Text(label, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis); if (supporting.isNotBlank()) Text(supporting, style = MaterialTheme.typography.bodySmall, color = s.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis) } } }

private fun redCinemaResume(positionMs: Long): String { val seconds = (positionMs / 1000).coerceAtLeast(0); val hours = seconds / 3600; val minutes = (seconds % 3600) / 60; return if (hours > 0) "%d:%02d".format(hours, minutes) else "${minutes}m" }
