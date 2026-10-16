package com.streamvault.app.ui.themes.goldnight

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.domain.model.Movie

/** Gold Night Movie Detail: خلفية سينمائية، عمود معلومات وأزرار (تشغيل/إعلان/مفضلة)، وعمود «قد يعجبك أيضاً». */
@Composable
internal fun GoldNightMovieDetail(
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
    Box(modifier = Modifier.fillMaxSize().background(GoldNightCanvas)) {
        GoldNightArtwork(
            imageUrl = movie.backdropUrl ?: movie.posterUrl,
            title = movie.name,
            modifier = Modifier.fillMaxSize(),
            cornerRadius = 0.dp
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(GoldNightCanvas, GoldNightCanvas.copy(alpha = 0.92f), GoldNightCanvas.copy(alpha = 0.55f))
                    )
                )
        )
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 36.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1.4f).fillMaxHeight(),
                contentPadding = PaddingValues(4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item("gn_movie_back") { GoldNightButton(label = "‹ Back to Movies", onClick = onBack) }
                item("gn_movie_title") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = movie.name,
                            style = MaterialTheme.typography.displaySmall,
                            color = GoldNightText,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text(
                                text = goldNightMovieMeta(movie),
                                style = MaterialTheme.typography.titleSmall,
                                color = GoldNightMuted
                            )
                            Text(text = goldNightRating(movie.rating), style = MaterialTheme.typography.titleSmall, color = GoldNightGold)
                        }
                        Text(
                            text = movie.plot?.takeIf { it.isNotBlank() } ?: "No description is available for this title.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = GoldNightText.copy(alpha = 0.86f),
                            maxLines = 6,
                            overflow = TextOverflow.Ellipsis
                        )
                        listOfNotNull(
                            movie.director?.takeIf { it.isNotBlank() }?.let { "Director: $it" },
                            movie.cast?.takeIf { it.isNotBlank() }?.let { "Cast: $it" }
                        ).forEach { line ->
                            Text(text = line, style = MaterialTheme.typography.bodySmall, color = GoldNightMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                item("gn_movie_actions") {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GoldNightButton(
                            label = if (hasResume) "Resume ${goldNightDuration(resumePositionMs)}" else "Play",
                            onClick = onPlay,
                            primary = true,
                            icon = Icons.Default.PlayArrow
                        )
                        if (onPlayTrailer != null) GoldNightButton(label = "Trailer", onClick = onPlayTrailer)
                        GoldNightButton(label = if (movie.isFavorite) "In Favorites" else "Add to Favorites", onClick = onToggleFavorite)
                    }
                }
                item("gn_movie_more_actions") {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GoldNightButton(label = "Copy link", onClick = onCopyUrl)
                        GoldNightButton(label = "Download", onClick = onDownload)
                        GoldNightButton(label = if (isCasting) "Casting" else "Cast", onClick = onCast)
                    }
                }
                if (movie.variants.size > 1) {
                    item("gn_movie_variants_title") {
                        Text(text = "Versions", style = MaterialTheme.typography.titleMedium, color = GoldNightText)
                    }
                    items(movie.variants, key = { "gn_variant_${it.rawMovieId}" }) { variant ->
                        GoldNightCard(onClick = { onSelectVariant(variant.rawMovieId) }, modifier = Modifier.width(420.dp)) {
                            Text(
                                text = variant.label,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
            Column(modifier = Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                GoldNightTopStatus(modifier = Modifier.padding(bottom = 8.dp))
                GoldNightColumnTitle(title = "You might also like")
                if (relatedContent.isEmpty()) {
                    GoldNightStatePanel("No suggestions", "Similar titles will appear here.")
                }
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(relatedContent.take(20), key = { "gn_related_${it.id}" }) { related ->
                        GoldNightPosterRow(
                            title = related.name,
                            meta = goldNightMovieMeta(related),
                            rating = goldNightRating(related.rating),
                            posterUrl = related.posterUrl,
                            onClick = { onRelatedClick(related) }
                        )
                    }
                }
            }
        }
    }
}
