package com.streamvault.app.ui.themes.blueocean

/** Style contract: Blue Ocean film detail is a vertical voyage file, not a cinematic hero or card sheet. */

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import com.streamvault.app.ui.theme.LocalThemePresentation
import com.streamvault.domain.model.Movie

@Composable
internal fun BlueOceanMovieDetail(movie: Movie, hasResume: Boolean, resumePositionMs: Long, isCasting: Boolean, relatedContent: List<Movie>, onPlay: () -> Unit, onCopyUrl: () -> Unit, onDownload: () -> Unit, onCast: () -> Unit, onToggleFavorite: () -> Unit, onSelectVariant: (Long) -> Unit, onRelatedClick: (Movie) -> Unit, onBack: () -> Unit, onPlayTrailer: (() -> Unit)?) {
    val p = LocalThemePresentation.current
    val s = p.surfaces
    LazyColumn(Modifier.fillMaxSize().background(s.canvas).padding(30.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
        item("blue_ocean_film_return") { BlueOceanDetailAction("← RETURN TO CURRENT", onBack) }
        item("blue_ocean_film_file") { Column(Modifier.fillMaxWidth().background(s.browseContent, RoundedCornerShape(34.dp)).padding(28.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Text("VOYAGE FILE · ${movie.year?.toString() ?: "LIBRARY"}", style = MaterialTheme.typography.labelMedium, color = s.accent); Text(movie.name, style = MaterialTheme.typography.displayMedium, color = s.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis); Text(listOfNotNull(movie.releaseDate, movie.duration, movie.genre).filter { it.isNotBlank() }.joinToString(" · "), style = MaterialTheme.typography.titleSmall, color = s.textSecondary); Text(movie.plot?.takeIf { it.isNotBlank() } ?: "No voyage notes are available for this title.", style = MaterialTheme.typography.bodyLarge, color = s.textPrimary, maxLines = 7, overflow = TextOverflow.Ellipsis); Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) { BlueOceanDetailAction(if (hasResume) "RESUME ${blueOceanResume(resumePositionMs)}" else "BEGIN VOYAGE", onPlay, Modifier.weight(1f), true); BlueOceanDetailAction(if (movie.isFavorite) "ANCHORED" else "ANCHOR", onToggleFavorite, Modifier.weight(1f)); onPlayTrailer?.let { BlueOceanDetailAction("TRAILER", it, Modifier.weight(1f)) } } } }
        item("blue_ocean_film_tools") { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("NAVIGATION TOOLS", style = MaterialTheme.typography.titleLarge); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { BlueOceanDetailAction("COPY ROUTE", onCopyUrl, Modifier.weight(1f)); BlueOceanDetailAction("DOWNLOAD", onDownload, Modifier.weight(1f)); BlueOceanDetailAction(if (isCasting) "CASTING" else "CAST", onCast, Modifier.weight(1f)) } } }
        if (movie.variants.size > 1) item("blue_ocean_film_versions") { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("ROUTE VARIANTS", style = MaterialTheme.typography.titleLarge); movie.variants.forEach { variant -> BlueOceanDetailAction(variant.label, { onSelectVariant(variant.rawMovieId) }, supporting = "Select stream version") } } }
        if (relatedContent.isNotEmpty()) item("blue_ocean_film_related") { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("NEARBY VOYAGES", style = MaterialTheme.typography.titleLarge); relatedContent.take(12).forEach { related -> BlueOceanDetailAction(related.name, { onRelatedClick(related) }, supporting = related.genre ?: related.year?.toString().orEmpty()) } } }
    }
}

@Composable
private fun BlueOceanDetailAction(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, primary: Boolean = false, supporting: String = "") { val p = LocalThemePresentation.current; val s = p.surfaces; val shape = RoundedCornerShape(if (primary) 22.dp else 16.dp); TvClickableSurface(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = if (primary) s.selectedAccent else s.browseContent, focusedContainerColor = s.focusedSurface, contentColor = s.textPrimary, focusedContentColor = s.textPrimary), border = ClickableSurfaceDefaults.border(border = Border(border = BorderStroke(1.dp, if (primary) s.accent else s.textSecondary.copy(alpha = .26f)), shape = shape), focusedBorder = Border(border = BorderStroke(2.dp, s.accent), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f)) { Column(Modifier.padding(horizontal = 16.dp, vertical = 13.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) { Text(label, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis); if (supporting.isNotBlank()) Text(supporting, style = MaterialTheme.typography.bodySmall, color = s.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis) } } }

private fun blueOceanResume(positionMs: Long): String { val seconds = (positionMs / 1000).coerceAtLeast(0); val hours = seconds / 3600; val minutes = (seconds % 3600) / 60; return if (hours > 0) "%d:%02d".format(hours, minutes) else "%dm".format(minutes) }
