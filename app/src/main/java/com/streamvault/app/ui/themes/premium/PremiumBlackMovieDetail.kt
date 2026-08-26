package com.streamvault.app.ui.themes.premium

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

@Composable
internal fun PremiumBlackMovieDetail(movie: Movie, hasResume: Boolean, resumePositionMs: Long, isCasting: Boolean, relatedContent: List<Movie>, onPlay: () -> Unit, onCopyUrl: () -> Unit, onDownload: () -> Unit, onCast: () -> Unit, onToggleFavorite: () -> Unit, onSelectVariant: (Long) -> Unit, onRelatedClick: (Movie) -> Unit, onBack: () -> Unit, onPlayTrailer: (() -> Unit)?) {
    LazyColumn(Modifier.fillMaxSize().background(PremiumCanvas).padding(32.dp), verticalArrangement = Arrangement.spacedBy(17.dp)) {
        item("premium_movie_back") { PremiumMovieDetailButton("← BACK TO FILMS", onBack) }
        item("premium_movie_hero") { Column(Modifier.fillMaxWidth().background(PremiumPanel, RoundedCornerShape(12.dp)).padding(30.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Text("PREMIUM FEATURE / ${movie.year?.toString() ?: "LIBRARY"}", style = MaterialTheme.typography.labelLarge, color = PremiumGold); Text(movie.name, style = MaterialTheme.typography.displayMedium, color = PremiumText, maxLines = 2, overflow = TextOverflow.Ellipsis); Text(listOfNotNull(movie.releaseDate, movie.duration, movie.genre).filter { it.isNotBlank() }.joinToString(" · "), style = MaterialTheme.typography.titleSmall, color = PremiumMuted); Text(movie.plot?.takeIf { it.isNotBlank() } ?: "No description is available for this title.", style = MaterialTheme.typography.bodyLarge, color = PremiumText, maxLines = 7, overflow = TextOverflow.Ellipsis); Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) { PremiumMovieDetailButton(if (hasResume) "RESUME ${premiumResume(resumePositionMs)}" else "PLAY NOW", onPlay, Modifier.weight(1f), true); PremiumMovieDetailButton(if (movie.isFavorite) "SAVED" else "SAVE", onToggleFavorite, Modifier.weight(1f)); onPlayTrailer?.let { PremiumMovieDetailButton("TRAILER", it, Modifier.weight(1f)) } } } }
        item("premium_movie_actions") { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("METAL ACTIONS", style = MaterialTheme.typography.titleLarge, color = PremiumText); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { PremiumMovieDetailButton("COPY LINK", onCopyUrl, Modifier.weight(1f)); PremiumMovieDetailButton("DOWNLOAD", onDownload, Modifier.weight(1f)); PremiumMovieDetailButton(if (isCasting) "CASTING" else "CAST", onCast, Modifier.weight(1f)) } } }
        if (movie.variants.size > 1) item("premium_movie_variants") { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("AVAILABLE VERSIONS", style = MaterialTheme.typography.titleLarge, color = PremiumText); LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(movie.variants, key = { it.rawMovieId }) { variant -> PremiumMovieDetailButton(variant.label, { onSelectVariant(variant.rawMovieId) }) } } } }
        if (relatedContent.isNotEmpty()) item("premium_movie_related") { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("MORE LIKE THIS", style = MaterialTheme.typography.titleLarge, color = PremiumText); LazyRow(horizontalArrangement = Arrangement.spacedBy(9.dp)) { items(relatedContent.take(12), key = { it.id }) { related -> PremiumMovieDetailButton(related.name, { onRelatedClick(related) }, supporting = related.genre ?: related.year?.toString().orEmpty()) } } } }
    }
}
@Composable private fun PremiumMovieDetailButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, primary: Boolean = false, supporting: String = "") { val shape = RoundedCornerShape(7.dp); TvClickableSurface(onClick = onClick, modifier = modifier, shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = if (primary) PremiumPanelFocused else PremiumPanel, focusedContainerColor = PremiumPanelFocused, contentColor = PremiumText, focusedContentColor = PremiumText), border = ClickableSurfaceDefaults.border(border = Border(border = BorderStroke(1.dp, if (primary) PremiumGold else PremiumMetal), shape = shape), focusedBorder = Border(border = BorderStroke(2.dp, PremiumFocus), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f)) { Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) { Text(label, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis); if (supporting.isNotBlank()) Text(supporting, style = MaterialTheme.typography.bodySmall, color = PremiumMuted, maxLines = 1, overflow = TextOverflow.Ellipsis) } } }
private fun premiumResume(positionMs: Long): String { val seconds = (positionMs / 1000).coerceAtLeast(0); val hours = seconds / 3600; val minutes = (seconds % 3600) / 60; return if (hours > 0) "%d:%02d".format(hours, minutes) else "%dm".format(minutes) }
