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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.streamvault.domain.model.Series

@Composable
internal fun CinematicSeriesLayout(
    categories: List<Category>,
    selectedCategory: String?,
    series: List<Series>,
    isCategoryLocked: (Category) -> Boolean,
    isSeriesLocked: (Series) -> Boolean,
    onCategoryClick: (Category) -> Unit,
    onCategoryLongClick: (Category) -> Unit,
    onSeriesClick: (Series) -> Unit,
    onSeriesLongClick: (Series) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxSize().background(CinematicCanvas),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(
            modifier = Modifier.weight(0.72f).fillMaxHeight().background(CinematicPanel, RoundedCornerShape(24.dp)).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("SERIES INDEX", style = MaterialTheme.typography.labelMedium, color = CinematicGold)
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
        Column(modifier = Modifier.weight(1.28f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("SERIES / ${selectedCategory ?: "DISCOVER"}", style = MaterialTheme.typography.headlineSmall, color = CinematicText, fontWeight = FontWeight.Black)
            LazyVerticalGrid(
                columns = GridCells.Adaptive(232.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(series, key = { it.id }) { item ->
                    CinematicSeriesTitleCard(
                        series = item,
                        isLocked = isSeriesLocked(item),
                        onClick = { onSeriesClick(item) },
                        onLongClick = { onSeriesLongClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CinematicSeriesTitleCard(series: Series, isLocked: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
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
            focusedBorder = Border(border = androidx.compose.foundation.BorderStroke(2.dp, CinematicGold), shape = shape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.035f)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(190.dp)) {
            AsyncImage(
                model = series.backdropUrl ?: series.posterUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().clip(shape),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().background(CinematicCanvas.copy(alpha = 0.88f)).padding(13.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(series.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = listOfNotNull(series.genre, series.rating.takeIf { it > 0f }?.let { "★ $it" }).joinToString("  "),
                    style = MaterialTheme.typography.labelSmall,
                    color = CinematicGold,
                    maxLines = 1
                )
                if (isLocked) Text("LOCKED", style = MaterialTheme.typography.labelSmall, color = CinematicWine)
            }
        }
    }
}
