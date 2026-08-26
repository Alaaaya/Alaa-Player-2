package com.streamvault.app.ui.themes.cinematic

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.app.ui.screens.favorites.FavoriteSectionUiModel
import com.streamvault.app.ui.screens.favorites.FavoriteUiModel
import com.streamvault.app.ui.screens.favorites.SavedHistoryUiModel
import com.streamvault.app.ui.screens.favorites.SavedLibraryFilter
import com.streamvault.app.ui.screens.favorites.SavedLibraryPreset
import com.streamvault.app.ui.screens.favorites.SavedLibrarySort
import com.streamvault.domain.model.ContentType

/** Presentation-only saved-library view for Cinematic. */
@Composable
internal fun CinematicFavoritesLayout(
    sections: List<FavoriteSectionUiModel>,
    continueWatching: List<SavedHistoryUiModel>,
    recentLive: List<SavedHistoryUiModel>,
    selectedPreset: SavedLibraryPreset,
    selectedFilter: SavedLibraryFilter,
    selectedSort: SavedLibrarySort,
    onPresetSelected: (SavedLibraryPreset) -> Unit,
    onFilterSelected: (SavedLibraryFilter) -> Unit,
    onSortSelected: (SavedLibrarySort) -> Unit,
    onItemClick: (FavoriteUiModel) -> Unit,
    onItemLongClick: (FavoriteUiModel) -> Unit,
    onHistoryClick: (SavedHistoryUiModel) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(CinematicCanvas),
        contentPadding = PaddingValues(start = 42.dp, top = 30.dp, end = 42.dp, bottom = 42.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item("cinematic_saved_header") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("PERSONAL ARCHIVE", style = MaterialTheme.typography.displaySmall, color = CinematicText, fontWeight = FontWeight.Black)
                Text("Your saved channels, films and series — organised as a screening collection.", style = MaterialTheme.typography.bodyLarge, color = CinematicMuted)
            }
        }
        item("cinematic_saved_controls") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CinematicSavedControlRow(
                    label = "VIEW",
                    values = SavedLibraryPreset.entries.toList(),
                    selected = selectedPreset,
                    text = { it.name.replace('_', ' ') },
                    onSelected = onPresetSelected
                )
                CinematicSavedControlRow(
                    label = "TYPE",
                    values = SavedLibraryFilter.entries.toList(),
                    selected = selectedFilter,
                    text = { it.name },
                    onSelected = onFilterSelected
                )
                CinematicSavedControlRow(
                    label = "ORDER",
                    values = SavedLibrarySort.entries.toList(),
                    selected = selectedSort,
                    text = { it.name },
                    onSelected = onSortSelected
                )
            }
        }
        if (continueWatching.isNotEmpty()) item("cinematic_saved_continue") {
            CinematicHistoryShelf("CONTINUE WATCHING", continueWatching, onHistoryClick)
        }
        if (recentLive.isNotEmpty()) item("cinematic_saved_recent_live") {
            CinematicHistoryShelf("LIVE RECALL", recentLive, onHistoryClick)
        }
        sections.forEach { section ->
            item("cinematic_saved_section_${section.key}") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(section.title.uppercase(), style = MaterialTheme.typography.titleLarge, color = CinematicText, fontWeight = FontWeight.Black)
                    Text(section.subtitle, style = MaterialTheme.typography.bodyMedium, color = CinematicMuted)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(section.items, key = { "${section.key}:${it.favorite.id}" }) { item ->
                            CinematicSavedCard(item, onClick = { onItemClick(item) }, onLongClick = { onItemLongClick(item) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> CinematicSavedControlRow(label: String, values: List<T>, selected: T, text: (T) -> String, onSelected: (T) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(76.dp), style = MaterialTheme.typography.labelLarge, color = CinematicGold, fontWeight = FontWeight.Black)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(values, key = { text(it) }) { value ->
                CinematicSavedChip(text(value), selected = value == selected) { onSelected(value) }
            }
        }
    }
}

@Composable
private fun CinematicHistoryShelf(title: String, items: List<SavedHistoryUiModel>, onClick: (SavedHistoryUiModel) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = CinematicText, fontWeight = FontWeight.Black)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items, key = { it.history.id.takeIf { id -> id > 0 } ?: it.history.contentId }) { item ->
                CinematicSavedHistoryCard(item, onClick = { onClick(item) })
            }
        }
    }
}

@Composable
private fun CinematicSavedCard(item: FavoriteUiModel, onClick: () -> Unit, onLongClick: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    TvClickableSurface(
        onClick = onClick, onLongClick = onLongClick, modifier = Modifier.width(220.dp),
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = CinematicPanel, focusedContainerColor = CinematicPanelRaised, contentColor = CinematicText, focusedContentColor = CinematicText),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(2.dp, CinematicGold))),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.03f)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text(contentTypeLabel(item.favorite.contentType), style = MaterialTheme.typography.labelMedium, color = CinematicWine, fontWeight = FontWeight.Black)
            Text(item.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(item.subtitle.orEmpty().ifBlank { "Saved collection" }, style = MaterialTheme.typography.bodySmall, color = CinematicMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text("HOLD FOR ACTIONS", style = MaterialTheme.typography.labelSmall, color = CinematicGold)
        }
    }
}

@Composable
private fun CinematicSavedHistoryCard(item: SavedHistoryUiModel, onClick: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    TvClickableSurface(
        onClick = onClick, modifier = Modifier.width(220.dp), shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = CinematicPanel, focusedContainerColor = CinematicPanelRaised, contentColor = CinematicText, focusedContentColor = CinematicText),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(2.dp, CinematicWine)))
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text(contentTypeLabel(item.history.contentType), style = MaterialTheme.typography.labelMedium, color = CinematicWine, fontWeight = FontWeight.Black)
            Text(item.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = CinematicMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun CinematicSavedChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(999.dp)
    TvClickableSurface(onClick = onClick, shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = if (selected) CinematicWine.copy(alpha = .45f) else CinematicPanel, focusedContainerColor = CinematicPanelRaised, contentColor = if (selected) CinematicGold else CinematicMuted, focusedContentColor = CinematicText), border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(2.dp, CinematicGold)))) {
        Text(label, modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
}

private fun contentTypeLabel(contentType: ContentType): String = when (contentType) { ContentType.LIVE -> "LIVE"; ContentType.MOVIE, ContentType.VOD -> "FILM"; ContentType.SERIES, ContentType.SERIES_EPISODE -> "SERIES" }
