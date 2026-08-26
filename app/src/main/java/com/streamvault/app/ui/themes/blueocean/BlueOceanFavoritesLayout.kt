package com.streamvault.app.ui.themes.blueocean

/** Style contract: Blue Ocean favourites form a harbour ledger with vertical saved berths and playback wake. */

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
import com.streamvault.app.ui.theme.LocalThemePresentation

@Composable
internal fun BlueOceanFavoritesLayout(sections: List<FavoriteSectionUiModel>, continueWatching: List<SavedHistoryUiModel>, recentLive: List<SavedHistoryUiModel>, selectedPreset: SavedLibraryPreset, selectedFilter: SavedLibraryFilter, selectedSort: SavedLibrarySort, onPresetSelected: (SavedLibraryPreset) -> Unit, onFilterSelected: (SavedLibraryFilter) -> Unit, onSortSelected: (SavedLibrarySort) -> Unit, onItemClick: (FavoriteUiModel) -> Unit, onItemLongClick: (FavoriteUiModel) -> Unit, onHistoryClick: (SavedHistoryUiModel) -> Unit) {
    val p = LocalThemePresentation.current
    val s = p.surfaces
    val empty = sections.all { it.items.isEmpty() } && continueWatching.isEmpty() && recentLive.isEmpty()
    LazyColumn(Modifier.fillMaxSize().background(s.canvas).padding(28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item("blue_ocean_harbour_head") { Column(verticalArrangement = Arrangement.spacedBy(5.dp)) { Text("SAVED HARBOUR", style = MaterialTheme.typography.displaySmall, color = s.textPrimary); Text("FAVOURITES, RESUME WAKE, AND LIVE RECALL", style = MaterialTheme.typography.labelMedium, color = s.accent) } }
        item("blue_ocean_harbour_controls") { Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) { BlueOceanSavedControl("VIEW", selectedPreset.name) { onPresetSelected(SavedLibraryPreset.entries[(SavedLibraryPreset.entries.indexOf(selectedPreset) + 1) % SavedLibraryPreset.entries.size]) }; BlueOceanSavedControl("TYPE", selectedFilter.name) { onFilterSelected(SavedLibraryFilter.entries[(SavedLibraryFilter.entries.indexOf(selectedFilter) + 1) % SavedLibraryFilter.entries.size]) }; BlueOceanSavedControl("ORDER", selectedSort.name) { onSortSelected(SavedLibrarySort.entries[(SavedLibrarySort.entries.indexOf(selectedSort) + 1) % SavedLibrarySort.entries.size]) } } }
        if (empty) item("blue_ocean_harbour_empty") { BlueOceanSavedState("THE HARBOUR IS EMPTY", "Save a channel, film, or series to anchor it here.") }
        if (continueWatching.isNotEmpty()) item("blue_ocean_harbour_resume") { BlueOceanHistoryLedger("RESUME WAKE", continueWatching, onHistoryClick) }
        if (recentLive.isNotEmpty()) item("blue_ocean_harbour_live") { BlueOceanHistoryLedger("LIVE RECALL", recentLive, onHistoryClick) }
        sections.forEach { section -> if (section.items.isNotEmpty()) item("blue_ocean_harbour_${section.key}") { Column(verticalArrangement = Arrangement.spacedBy(7.dp)) { Text(section.title, style = MaterialTheme.typography.titleLarge); Text(section.subtitle, style = MaterialTheme.typography.bodySmall, color = s.textSecondary); Column(verticalArrangement = Arrangement.spacedBy(7.dp)) { section.items.forEach { item -> BlueOceanSavedBerth(item.title, item.subtitle.orEmpty(), { onItemClick(item) }, { onItemLongClick(item) }) } } } } }
    }
}

@Composable
private fun RowScope.BlueOceanSavedControl(label: String, value: String, onClick: () -> Unit) { val p = LocalThemePresentation.current; val s = p.surfaces; val shape = RoundedCornerShape(18.dp); TvClickableSurface(onClick = onClick, modifier = Modifier.weight(1f), shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = s.browseContent, focusedContainerColor = s.focusedSurface, contentColor = s.textPrimary, focusedContentColor = s.textPrimary), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, s.accent), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f)) { Column(Modifier.padding(13.dp)) { Text(label, style = MaterialTheme.typography.labelSmall, color = s.textSecondary); Text(value.replace('_', ' '), style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis) } } }

@Composable
private fun BlueOceanHistoryLedger(title: String, entries: List<SavedHistoryUiModel>, onClick: (SavedHistoryUiModel) -> Unit) { val s = LocalThemePresentation.current.surfaces; Column(verticalArrangement = Arrangement.spacedBy(7.dp)) { Text(title, style = MaterialTheme.typography.titleLarge, color = s.accent); Column(verticalArrangement = Arrangement.spacedBy(7.dp)) { entries.forEach { entry -> BlueOceanSavedBerth(entry.title, entry.subtitle.orEmpty(), { onClick(entry) }, { onClick(entry) }) } } } }

@Composable
private fun BlueOceanSavedBerth(title: String, subtitle: String, onClick: () -> Unit, onLongClick: () -> Unit) { val p = LocalThemePresentation.current; val s = p.surfaces; val shape = RoundedCornerShape(21.dp); TvClickableSurface(onClick = onClick, onLongClick = onLongClick, modifier = Modifier.fillMaxWidth(), shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = s.browseContent, focusedContainerColor = s.focusedSurface, contentColor = s.textPrimary, focusedContentColor = s.textPrimary), border = ClickableSurfaceDefaults.border(border = Border(border = BorderStroke(1.dp, s.textSecondary.copy(alpha = .22f)), shape = shape), focusedBorder = Border(border = BorderStroke(2.dp, s.accent), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f)) { Row(Modifier.fillMaxWidth().padding(horizontal = 17.dp, vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) { Text("≈", style = MaterialTheme.typography.titleLarge, color = s.accent); Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(subtitle, style = MaterialTheme.typography.bodySmall, color = s.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis) }; Text("OPEN", style = MaterialTheme.typography.labelLarge, color = s.accent) } } }

@Composable
private fun BlueOceanSavedState(title: String, subtitle: String) { val s = LocalThemePresentation.current.surfaces; Column(Modifier.fillMaxWidth().background(s.browseContent, RoundedCornerShape(24.dp)).padding(22.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text(title, style = MaterialTheme.typography.titleLarge, color = s.textPrimary); Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = s.textSecondary) } }
