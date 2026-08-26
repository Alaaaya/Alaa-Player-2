package com.streamvault.app.ui.themes.minimal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import com.streamvault.app.ui.screens.favorites.FavoriteSectionUiModel
import com.streamvault.app.ui.screens.favorites.FavoriteUiModel
import com.streamvault.app.ui.screens.favorites.SavedHistoryUiModel
import com.streamvault.app.ui.screens.favorites.SavedLibraryFilter
import com.streamvault.app.ui.screens.favorites.SavedLibraryPreset
import com.streamvault.app.ui.screens.favorites.SavedLibrarySort

@Composable
internal fun MinimalFavoritesLayout(
    sections: List<FavoriteSectionUiModel>, continueWatching: List<SavedHistoryUiModel>, recentLive: List<SavedHistoryUiModel>,
    selectedPreset: SavedLibraryPreset, selectedFilter: SavedLibraryFilter, selectedSort: SavedLibrarySort,
    onPresetSelected: (SavedLibraryPreset) -> Unit, onFilterSelected: (SavedLibraryFilter) -> Unit, onSortSelected: (SavedLibrarySort) -> Unit,
    onItemClick: (FavoriteUiModel) -> Unit, onItemLongClick: (FavoriteUiModel) -> Unit, onHistoryClick: (SavedHistoryUiModel) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize().background(MinimalCanvas).padding(34.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        item { Text("SAVED INDEX", style = MaterialTheme.typography.headlineMedium, color = MinimalText) }
        item { MinimalFavoriteLine(label = "VIEW: ${selectedPreset.name}", onClick = { onPresetSelected(SavedLibraryPreset.entries[(SavedLibraryPreset.entries.indexOf(selectedPreset) + 1) % SavedLibraryPreset.entries.size]) }) }
        item { MinimalFavoriteLine(label = "TYPE: ${selectedFilter.name}", onClick = { onFilterSelected(SavedLibraryFilter.entries[(SavedLibraryFilter.entries.indexOf(selectedFilter) + 1) % SavedLibraryFilter.entries.size]) }) }
        item { MinimalFavoriteLine(label = "ORDER: ${selectedSort.name}", onClick = { onSortSelected(SavedLibrarySort.entries[(SavedLibrarySort.entries.indexOf(selectedSort) + 1) % SavedLibrarySort.entries.size]) }) }
        if (continueWatching.isNotEmpty()) { item { Text("CONTINUE", style = MaterialTheme.typography.labelLarge, color = MinimalMuted) }; items(continueWatching, key = { it.history.id }) { item -> MinimalFavoriteLine(label = item.title, onClick = { onHistoryClick(item) }) } }
        if (recentLive.isNotEmpty()) { item { Text("RECENT LIVE", style = MaterialTheme.typography.labelLarge, color = MinimalMuted) }; items(recentLive, key = { it.history.id }) { item -> MinimalFavoriteLine(label = item.title, onClick = { onHistoryClick(item) }) } }
        sections.forEach { section -> item("minimal_${section.key}") { Column { Text(section.title, style = MaterialTheme.typography.titleMedium, color = MinimalText); Text(section.subtitle, style = MaterialTheme.typography.bodySmall, color = MinimalMuted); section.items.forEach { item -> MinimalFavoriteLine(item.title, { onItemClick(item) }, { onItemLongClick(item) }) } } } }
    }
}

@Composable private fun MinimalFavoriteLine(label: String, onClick: () -> Unit, onLongClick: () -> Unit = onClick) {
    val shape = RoundedCornerShape(0.dp)
    TvClickableSurface(onClick = onClick, onLongClick = onLongClick, modifier = Modifier.fillMaxWidth(), shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = Color.Transparent, focusedContainerColor = MinimalPaper, contentColor = MinimalText, focusedContentColor = MinimalText), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(1.dp, MinimalFocus), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = MinimalFocusedScale)) { Text(label, Modifier.padding(horizontal = 12.dp, vertical = 12.dp), style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis) }
}
