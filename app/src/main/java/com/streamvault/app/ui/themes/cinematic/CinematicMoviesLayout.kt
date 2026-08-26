package com.streamvault.app.ui.themes.cinematic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
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
import com.streamvault.domain.model.Category
import com.streamvault.domain.model.Movie

@Composable
internal fun CinematicMoviesLayout(
    categories: List<Category>,
    selectedCategory: String?,
    movies: List<Movie>,
    isCategoryLocked: (Category) -> Boolean,
    isMovieLocked: (Movie) -> Boolean,
    onCategoryClick: (Category) -> Unit,
    onCategoryLongClick: (Category) -> Unit,
    onMovieClick: (Movie) -> Unit,
    onMovieLongClick: (Movie) -> Unit,
    modifier: Modifier = Modifier
) {
    val featured = remember(movies) { movies.firstOrNull() }
    Row(
        modifier = modifier.fillMaxSize().background(CinematicCanvas),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(
            modifier = Modifier
                .width(236.dp)
                .fillMaxHeight()
                .background(CinematicPanel, RoundedCornerShape(24.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "THE ARCHIVE",
                style = MaterialTheme.typography.labelMedium,
                color = CinematicGold
            )
            Text(
                text = "Choose a collection",
                style = MaterialTheme.typography.bodySmall,
                color = CinematicMuted
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(categories.size) { index ->
                    val category = categories[index]
                    CinematicCategoryCard(
                        category = category,
                        isSelected = category.name == selectedCategory,
                        isLocked = isCategoryLocked(category),
                        onClick = { onCategoryClick(category) },
                        onLongClick = { onCategoryLongClick(category) }
                    )
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            featured?.let { movie ->
                CinematicMovieHero(movie = movie, isLocked = isMovieLocked(movie), onClick = { onMovieClick(movie) })
            }
            Text(
                text = selectedCategory ?: "FEATURED FILMS",
                style = MaterialTheme.typography.titleLarge,
                color = CinematicText,
                fontWeight = FontWeight.Black
            )
            LazyVerticalGrid(
                columns = GridCells.Adaptive(154.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(movies, key = { it.id }) { movie ->
                    CinematicMoviePoster(
                        movie = movie,
                        isLocked = isMovieLocked(movie),
                        onClick = { onMovieClick(movie) },
                        onLongClick = { onMovieLongClick(movie) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CinematicMovieHero(movie: Movie, isLocked: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(26.dp)
    TvClickableSurface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = CinematicPanel,
            focusedContainerColor = CinematicPanelRaised,
            contentColor = CinematicText,
            focusedContentColor = CinematicText
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(border = androidx.compose.foundation.BorderStroke(2.dp, CinematicGold), shape = shape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.01f),
        modifier = Modifier.fillMaxWidth().height(248.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = movie.backdropUrl ?: movie.posterUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.horizontalGradient(listOf(CinematicCanvas.copy(alpha = 0.95f), Color.Transparent)))
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
                    .fillMaxWidth(0.62f),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text("CINEMATIC FEATURE", style = MaterialTheme.typography.labelSmall, color = CinematicGold)
                Text(movie.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, maxLines = 2)
                Text(
                    text = listOfNotNull(movie.year, movie.genre).joinToString(" · ").ifBlank { movie.plot.orEmpty() },
                    style = MaterialTheme.typography.bodySmall,
                    color = CinematicMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (isLocked) Text("LOCKED", style = MaterialTheme.typography.labelSmall, color = CinematicGold)
            }
        }
    }
}

@Composable
private fun CinematicMoviePoster(
    movie: Movie,
    isLocked: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    var focused by remember(movie.id) { mutableStateOf(false) }
    TvClickableSurface(
        onClick = onClick,
        onLongClick = onLongClick,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = CinematicPanel,
            focusedContainerColor = CinematicPanelRaised,
            contentColor = CinematicText,
            focusedContentColor = CinematicText
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(border = androidx.compose.foundation.BorderStroke(2.dp, CinematicWine), shape = shape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.045f),
        modifier = Modifier.onFocusChanged { focused = it.isFocused }
    ) {
        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Box(modifier = Modifier.fillMaxWidth().height(202.dp).clip(RoundedCornerShape(10.dp)).background(CinematicCanvas)) {
                AsyncImage(
                    model = movie.posterUrl ?: movie.backdropUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                if (isLocked) {
                    Text(
                        text = "LOCK",
                        style = MaterialTheme.typography.labelSmall,
                        color = CinematicGold,
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                    )
                }
            }
            Text(movie.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                text = listOfNotNull(movie.year, movie.rating.takeIf { it > 0f }?.let { "★ $it" }).joinToString("  "),
                style = MaterialTheme.typography.labelSmall,
                color = if (focused) CinematicGold else CinematicMuted,
                maxLines = 1
            )
        }
    }
}
