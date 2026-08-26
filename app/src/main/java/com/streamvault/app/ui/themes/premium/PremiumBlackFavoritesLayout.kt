package com.streamvault.app.ui.themes.premium

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import com.streamvault.app.ui.screens.favorites.FavoriteSectionUiModel
import com.streamvault.app.ui.screens.favorites.FavoriteUiModel
import com.streamvault.app.ui.screens.favorites.SavedHistoryUiModel
import com.streamvault.app.ui.screens.favorites.SavedLibraryFilter
import com.streamvault.app.ui.screens.favorites.SavedLibraryPreset
import com.streamvault.app.ui.screens.favorites.SavedLibrarySort

@Composable
internal fun PremiumBlackFavoritesLayout(sections: List<FavoriteSectionUiModel>, continueWatching: List<SavedHistoryUiModel>, recentLive: List<SavedHistoryUiModel>, selectedPreset: SavedLibraryPreset, selectedFilter: SavedLibraryFilter, selectedSort: SavedLibrarySort, onPresetSelected: (SavedLibraryPreset) -> Unit, onFilterSelected: (SavedLibraryFilter) -> Unit, onSortSelected: (SavedLibrarySort) -> Unit, onItemClick: (FavoriteUiModel) -> Unit, onItemLongClick: (FavoriteUiModel) -> Unit, onHistoryClick: (SavedHistoryUiModel) -> Unit) {
    val empty = sections.all { it.items.isEmpty() } && continueWatching.isEmpty() && recentLive.isEmpty()
    LazyColumn(Modifier.fillMaxSize().background(PremiumCanvas).padding(30.dp), verticalArrangement = Arrangement.spacedBy(17.dp)) {
        item("premium_saved_head") { Column(verticalArrangement = Arrangement.spacedBy(5.dp)) { Text("MY COLLECTION", style = MaterialTheme.typography.displaySmall, color = PremiumText); Text("SAVED TITLES AND PLAYBACK HISTORY", style = MaterialTheme.typography.labelLarge, color = PremiumMuted) } }
        item("premium_saved_controls") { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { PremiumSavedControl("VIEW", selectedPreset.name) { onPresetSelected(SavedLibraryPreset.entries[(SavedLibraryPreset.entries.indexOf(selectedPreset) + 1) % SavedLibraryPreset.entries.size]) }; PremiumSavedControl("TYPE", selectedFilter.name) { onFilterSelected(SavedLibraryFilter.entries[(SavedLibraryFilter.entries.indexOf(selectedFilter) + 1) % SavedLibraryFilter.entries.size]) }; PremiumSavedControl("ORDER", selectedSort.name) { onSortSelected(SavedLibrarySort.entries[(SavedLibrarySort.entries.indexOf(selectedSort) + 1) % SavedLibrarySort.entries.size]) } } }
        if (empty) item("premium_saved_empty") { PremiumSavedState("YOUR COLLECTION IS EMPTY", "Save a channel, film or series to build a personal collection.") }
        if (continueWatching.isNotEmpty()) item("premium_saved_continue") { PremiumHistoryShelf("CONTINUE WATCHING", continueWatching, onHistoryClick) }
        if (recentLive.isNotEmpty()) item("premium_saved_recent") { PremiumHistoryShelf("RECENT LIVE", recentLive, onHistoryClick) }
        sections.forEach { section -> if (section.items.isNotEmpty()) item("premium_saved_${section.key}") { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(section.title, style = MaterialTheme.typography.titleLarge, color = PremiumText); Text(section.subtitle, style = MaterialTheme.typography.bodySmall, color = PremiumMuted); LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) { items(section.items, key = { "${section.key}_${it.favorite.id}" }) { item -> PremiumSavedCard(item.title, item.subtitle.orEmpty(), { onItemClick(item) }, { onItemLongClick(item) }) } } } } }
    }
}
@Composable private fun RowScope.PremiumSavedControl(label: String, value: String, onClick: () -> Unit) { val shape = RoundedCornerShape(7.dp); TvClickableSurface(onClick = onClick, modifier = Modifier.weight(1f), shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = PremiumPanel, focusedContainerColor = PremiumPanelFocused, contentColor = PremiumText, focusedContentColor = PremiumText), border = ClickableSurfaceDefaults.border(border = Border(border = BorderStroke(1.dp, PremiumMetal), shape = shape), focusedBorder = Border(border = BorderStroke(2.dp, PremiumFocus), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.015f)) { Column(Modifier.padding(14.dp)) { Text(label, style = MaterialTheme.typography.labelSmall, color = PremiumMuted); Text(value.replace('_', ' '), style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis) } } }
@Composable private fun PremiumHistoryShelf(title: String, entries: List<SavedHistoryUiModel>, onClick: (SavedHistoryUiModel) -> Unit) = Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(title, style = MaterialTheme.typography.titleLarge, color = PremiumText); LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) { items(entries, key = { it.history.id }) { entry -> PremiumSavedCard(entry.title, entry.subtitle.orEmpty(), { onClick(entry) }, { onClick(entry) }) } } }
@Composable private fun PremiumSavedCard(title: String, subtitle: String, onClick: () -> Unit, onLongClick: () -> Unit) { val shape = RoundedCornerShape(8.dp); TvClickableSurface(onClick = onClick, onLongClick = onLongClick, modifier = Modifier.fillMaxWidth(), shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = PremiumPanel, focusedContainerColor = PremiumPanelFocused, contentColor = PremiumText, focusedContentColor = PremiumText), border = ClickableSurfaceDefaults.border(border = Border(border = BorderStroke(1.dp, PremiumMetal), shape = shape), focusedBorder = Border(border = BorderStroke(2.dp, PremiumFocus), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f)) { Column(Modifier.padding(17.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis); Text(subtitle, style = MaterialTheme.typography.bodySmall, color = PremiumMuted, maxLines = 2, overflow = TextOverflow.Ellipsis); Text("OPEN", style = MaterialTheme.typography.labelSmall, color = PremiumGold) } } }
@Composable private fun PremiumSavedState(title: String, subtitle: String) = Column(Modifier.fillMaxWidth().background(PremiumPanel, RoundedCornerShape(8.dp)).padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text(title, style = MaterialTheme.typography.titleLarge, color = PremiumText); Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = PremiumMuted) }
