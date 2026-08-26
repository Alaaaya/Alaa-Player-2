package com.streamvault.app.ui.themes.streaming

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
import androidx.compose.ui.Alignment
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

/** مفضلة Streaming Platform: رفوف للمتابعة والمحطات والمجموعات، لا قائمة Glass أو واجهة Minimal. */
@Composable
internal fun StreamingPlatformFavoritesLayout(
    sections: List<FavoriteSectionUiModel>, continueWatching: List<SavedHistoryUiModel>, recentLive: List<SavedHistoryUiModel>,
    selectedPreset: SavedLibraryPreset, selectedFilter: SavedLibraryFilter, selectedSort: SavedLibrarySort,
    onPresetSelected: (SavedLibraryPreset) -> Unit, onFilterSelected: (SavedLibraryFilter) -> Unit, onSortSelected: (SavedLibrarySort) -> Unit,
    onItemClick: (FavoriteUiModel) -> Unit, onItemLongClick: (FavoriteUiModel) -> Unit, onHistoryClick: (SavedHistoryUiModel) -> Unit
) {
    val isEmpty = sections.all { it.items.isEmpty() } && continueWatching.isEmpty() && recentLive.isEmpty()
    LazyColumn(modifier = Modifier.fillMaxSize().background(StreamingCanvas).padding(horizontal = 36.dp, vertical = 28.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item("streaming_saved_head") {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("MY LIST", style = MaterialTheme.typography.displaySmall, color = StreamingText)
                Text("SAVED TITLES AND PLAYBACK HISTORY", style = MaterialTheme.typography.labelLarge, color = StreamingMuted)
            }
        }
        item("streaming_saved_controls") { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StreamingSavedControl("VIEW", selectedPreset.name) { onPresetSelected(SavedLibraryPreset.entries[(SavedLibraryPreset.entries.indexOf(selectedPreset) + 1) % SavedLibraryPreset.entries.size]) }
            StreamingSavedControl("TYPE", selectedFilter.name) { onFilterSelected(SavedLibraryFilter.entries[(SavedLibraryFilter.entries.indexOf(selectedFilter) + 1) % SavedLibraryFilter.entries.size]) }
            StreamingSavedControl("ORDER", selectedSort.name) { onSortSelected(SavedLibrarySort.entries[(SavedLibrarySort.entries.indexOf(selectedSort) + 1) % SavedLibrarySort.entries.size]) }
        } }
        if (isEmpty) item("streaming_saved_empty") { StreamingSavedState("YOUR LIST IS EMPTY", "Save a channel, film or series to build a personal collection.") }
        if (continueWatching.isNotEmpty()) item("streaming_saved_continue") { StreamingHistoryShelf("CONTINUE WATCHING", "Resume from your latest position", continueWatching, onHistoryClick) }
        if (recentLive.isNotEmpty()) item("streaming_saved_recent") { StreamingHistoryShelf("RECENT LIVE", "Return to a recent channel", recentLive, onHistoryClick) }
        sections.forEach { section ->
            if (section.items.isNotEmpty()) item("streaming_saved_${section.key}") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(section.title, style = MaterialTheme.typography.titleLarge, color = StreamingText)
                    Text(section.subtitle, style = MaterialTheme.typography.bodySmall, color = StreamingMuted)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(section.items, key = { "${section.key}_${it.favorite.id}" }) { item -> StreamingSavedCard(item.title, item.subtitle.orEmpty(), { onItemClick(item) }, { onItemLongClick(item) }) }
                    }
                }
            }
        }
    }
}

@Composable private fun RowScope.StreamingSavedControl(label: String, value: String, onClick: () -> Unit) { val shape = RoundedCornerShape(12.dp); TvClickableSurface(onClick = onClick, modifier = Modifier.weight(1f), shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = StreamingPanel, focusedContainerColor = StreamingPanelFocused, contentColor = StreamingText, focusedContentColor = StreamingText), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, StreamingFocus), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.025f)) { Column(Modifier.padding(horizontal = 15.dp, vertical = 12.dp)) { Text(label, style = MaterialTheme.typography.labelSmall, color = StreamingMuted); Text(value.replace('_', ' '), style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis) } } }
@Composable private fun StreamingHistoryShelf(title: String, subtitle: String, entries: List<SavedHistoryUiModel>, onClick: (SavedHistoryUiModel) -> Unit) = Column(verticalArrangement = Arrangement.spacedBy(9.dp)) { Text(title, style = MaterialTheme.typography.titleLarge, color = StreamingText); Text(subtitle, style = MaterialTheme.typography.bodySmall, color = StreamingMuted); LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) { items(entries, key = { it.history.id }) { entry -> StreamingSavedCard(entry.title, entry.subtitle.orEmpty(), { onClick(entry) }, { onClick(entry) }) } } }
@Composable private fun StreamingSavedCard(title: String, subtitle: String, onClick: () -> Unit, onLongClick: () -> Unit) { val shape = RoundedCornerShape(15.dp); TvClickableSurface(onClick = onClick, onLongClick = onLongClick, modifier = Modifier.fillMaxWidth().padding(end = 0.dp), shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = StreamingPanel, focusedContainerColor = StreamingPanelFocused, contentColor = StreamingText, focusedContentColor = StreamingText), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, StreamingFocus), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.035f)) { Column(Modifier.padding(17.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis); Text(subtitle, style = MaterialTheme.typography.bodySmall, color = StreamingMuted, maxLines = 2, overflow = TextOverflow.Ellipsis); Text("OPEN", style = MaterialTheme.typography.labelSmall, color = StreamingAccent) } } }
@Composable private fun StreamingSavedState(title: String, subtitle: String) = Column(Modifier.fillMaxWidth().background(StreamingPanel, RoundedCornerShape(16.dp)).padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text(title, style = MaterialTheme.typography.titleLarge, color = StreamingText); Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = StreamingMuted) }
