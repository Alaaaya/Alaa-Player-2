package com.streamvault.app.ui.themes.redcinema

/** Red Cinema favourites contract: saved seats, encores, and recent live screenings in a vertical seat ledger. */

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
internal fun RedCinemaSeatLedger(
    sections: List<FavoriteSectionUiModel>, continueWatching: List<SavedHistoryUiModel>, recentLive: List<SavedHistoryUiModel>, selectedPreset: SavedLibraryPreset, selectedFilter: SavedLibraryFilter, selectedSort: SavedLibrarySort,
    onPresetSelected: (SavedLibraryPreset) -> Unit, onFilterSelected: (SavedLibraryFilter) -> Unit, onSortSelected: (SavedLibrarySort) -> Unit, onItemClick: (FavoriteUiModel) -> Unit, onItemLongClick: (FavoriteUiModel) -> Unit, onHistoryClick: (SavedHistoryUiModel) -> Unit
) {
    val s = LocalThemePresentation.current.surfaces
    val empty = sections.all { it.items.isEmpty() } && continueWatching.isEmpty() && recentLive.isEmpty()
    LazyColumn(Modifier.fillMaxSize().background(s.canvas).padding(28.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
        item("red_cinema_seats_header") { Column(Modifier.fillMaxWidth().background(s.browseContent, RoundedCornerShape(2.dp)).padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) { Text("SAVED SEATS", style = MaterialTheme.typography.displaySmall); Text("FAVOURITES / ENCORES / LIVE RECALL", style = MaterialTheme.typography.labelMedium, color = s.accent) } }
        item("red_cinema_seats_controls") { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) { RedCinemaSeatControl("VIEW", selectedPreset.name) { onPresetSelected(SavedLibraryPreset.entries[(SavedLibraryPreset.entries.indexOf(selectedPreset) + 1) % SavedLibraryPreset.entries.size]) }; RedCinemaSeatControl("TYPE", selectedFilter.name) { onFilterSelected(SavedLibraryFilter.entries[(SavedLibraryFilter.entries.indexOf(selectedFilter) + 1) % SavedLibraryFilter.entries.size]) }; RedCinemaSeatControl("ORDER", selectedSort.name) { onSortSelected(SavedLibrarySort.entries[(SavedLibrarySort.entries.indexOf(selectedSort) + 1) % SavedLibrarySort.entries.size]) } } }
        if (empty) item("red_cinema_seats_empty") { RedCinemaSeatState("NO SEATS RESERVED", "Save a channel, film, or series to reserve it here.") }
        if (continueWatching.isNotEmpty()) item("red_cinema_seats_encore") { RedCinemaHistoryRegister("ENCORE", continueWatching, onHistoryClick) }
        if (recentLive.isNotEmpty()) item("red_cinema_seats_live") { RedCinemaHistoryRegister("LAST SCREENINGS", recentLive, onHistoryClick) }
        sections.forEach { section -> if (section.items.isNotEmpty()) item("red_cinema_seats_${section.key}") { Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { Text(section.title, style = MaterialTheme.typography.titleLarge, color = s.accent); Text(section.subtitle, style = MaterialTheme.typography.bodySmall, color = s.textSecondary); section.items.forEachIndexed { index, item -> RedCinemaSavedSeat((index + 1).toString().padStart(2, '0'), item.title, item.subtitle.orEmpty(), { onItemClick(item) }, { onItemLongClick(item) }) } } } }
    }
}

@Composable
private fun RowScope.RedCinemaSeatControl(label: String, value: String, onClick: () -> Unit) { val s = LocalThemePresentation.current.surfaces; val shape = RoundedCornerShape(2.dp); TvClickableSurface(onClick = onClick, modifier = Modifier.weight(1f), shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = s.browseContent, focusedContainerColor = s.focusedSurface, contentColor = s.textPrimary, focusedContentColor = s.textPrimary), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, s.accent), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.018f)) { Column(Modifier.padding(12.dp)) { Text(label, style = MaterialTheme.typography.labelSmall, color = s.textSecondary); Text(value.replace('_', ' '), style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis) } } }

@Composable
private fun RedCinemaHistoryRegister(title: String, entries: List<SavedHistoryUiModel>, onClick: (SavedHistoryUiModel) -> Unit) { val s = LocalThemePresentation.current.surfaces; Column(Modifier.fillMaxWidth().background(s.browseContent, RoundedCornerShape(2.dp)).padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) { Text(title, style = MaterialTheme.typography.titleLarge, color = s.accent); entries.forEachIndexed { index, entry -> RedCinemaSavedSeat((index + 1).toString().padStart(2, '0'), entry.title, entry.subtitle.orEmpty(), { onClick(entry) }, { onClick(entry) }) } } }

@Composable
private fun RedCinemaSavedSeat(number: String, title: String, subtitle: String, onClick: () -> Unit, onLongClick: () -> Unit) { val s = LocalThemePresentation.current.surfaces; val shape = RoundedCornerShape(2.dp); TvClickableSurface(onClick = onClick, onLongClick = onLongClick, modifier = Modifier.fillMaxWidth(), shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = s.browseContent, focusedContainerColor = s.focusedSurface, contentColor = s.textPrimary, focusedContentColor = s.textPrimary), border = ClickableSurfaceDefaults.border(border = Border(border = BorderStroke(1.dp, s.textSecondary.copy(alpha = .24f)), shape = shape), focusedBorder = Border(border = BorderStroke(2.dp, s.accent), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.018f)) { Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 11.dp), horizontalArrangement = Arrangement.spacedBy(13.dp)) { Text(number, style = MaterialTheme.typography.labelLarge, color = s.accent); Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(subtitle, style = MaterialTheme.typography.bodySmall, color = s.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis) }; Text("SEAT", style = MaterialTheme.typography.labelSmall, color = s.accent) } } }

@Composable
private fun RedCinemaSeatState(title: String, subtitle: String) { val s = LocalThemePresentation.current.surfaces; Column(Modifier.fillMaxWidth().background(s.browseContent, RoundedCornerShape(2.dp)).padding(20.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) { Text(title, style = MaterialTheme.typography.titleLarge); Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = s.textSecondary) } }
