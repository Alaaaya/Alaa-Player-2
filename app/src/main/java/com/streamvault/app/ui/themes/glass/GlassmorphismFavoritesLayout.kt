package com.streamvault.app.ui.themes.glass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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

/** واجهة محفوظات زجاجية؛ تتلقى الحالة والإجراءات من عقد المفضلة المشترك فقط. */
@Composable
internal fun GlassmorphismFavoritesLayout(
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
    val isEmpty = sections.all { it.items.isEmpty() } && continueWatching.isEmpty() && recentLive.isEmpty()
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(GlassCanvas).padding(horizontal = 42.dp, vertical = 30.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item("glass_saved_header") {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("SAVED VAULT", style = MaterialTheme.typography.headlineMedium, color = GlassText)
                Text(
                    "Collections and playback history remain available in one translucent archive.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GlassMuted
                )
            }
        }
        item("glass_saved_controls") {
            GlassFavoritesControlPanel(
                selectedPreset = selectedPreset,
                selectedFilter = selectedFilter,
                selectedSort = selectedSort,
                onPresetSelected = onPresetSelected,
                onFilterSelected = onFilterSelected,
                onSortSelected = onSortSelected
            )
        }
        if (isEmpty) {
            item("glass_saved_empty") {
                GlassFavoritesPane {
                    Text("YOUR GLASS VAULT IS EMPTY", style = MaterialTheme.typography.titleLarge, color = GlassText)
                    Text(
                        "Save a channel, film or series to start building a personal collection.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GlassMuted
                    )
                }
            }
        }
        if (continueWatching.isNotEmpty()) {
            item("glass_continue_heading") { GlassFavoritesHeading("CONTINUE WATCHING", "Resume from your latest position") }
            items(continueWatching, key = { "glass_continue_${it.history.id}" }) { history ->
                GlassHistoryRow(history, onClick = { onHistoryClick(history) })
            }
        }
        if (recentLive.isNotEmpty()) {
            item("glass_recent_heading") { GlassFavoritesHeading("RECENT LIVE", "Return to your most recent channels") }
            items(recentLive, key = { "glass_recent_${it.history.id}" }) { history ->
                GlassHistoryRow(history, onClick = { onHistoryClick(history) })
            }
        }
        sections.forEach { section ->
            item("glass_section_${section.key}") { GlassFavoritesHeading(section.title, section.subtitle) }
            items(section.items, key = { "glass_${section.key}_${it.favorite.id}" }) { item ->
                GlassFavoriteRow(
                    item = item,
                    onClick = { onItemClick(item) },
                    onLongClick = { onItemLongClick(item) }
                )
            }
        }
    }
}

@Composable
private fun GlassFavoritesControlPanel(
    selectedPreset: SavedLibraryPreset,
    selectedFilter: SavedLibraryFilter,
    selectedSort: SavedLibrarySort,
    onPresetSelected: (SavedLibraryPreset) -> Unit,
    onFilterSelected: (SavedLibraryFilter) -> Unit,
    onSortSelected: (SavedLibrarySort) -> Unit
) {
    GlassFavoritesPane {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            GlassCycleControl("VIEW", selectedPreset.name) {
                onPresetSelected(SavedLibraryPreset.entries[(SavedLibraryPreset.entries.indexOf(selectedPreset) + 1) % SavedLibraryPreset.entries.size])
            }
            GlassCycleControl("TYPE", selectedFilter.name) {
                onFilterSelected(SavedLibraryFilter.entries[(SavedLibraryFilter.entries.indexOf(selectedFilter) + 1) % SavedLibraryFilter.entries.size])
            }
            GlassCycleControl("ORDER", selectedSort.name) {
                onSortSelected(SavedLibrarySort.entries[(SavedLibrarySort.entries.indexOf(selectedSort) + 1) % SavedLibrarySort.entries.size])
            }
        }
    }
}

@Composable
private fun RowScope.GlassCycleControl(label: String, value: String, onClick: () -> Unit) {
    TvClickableSurface(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(16.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = GlassPane,
            focusedContainerColor = GlassPaneFocused,
            contentColor = GlassText,
            focusedContentColor = GlassText
        ),
        border = ClickableSurfaceDefaults.border(
            border = Border(border = BorderStroke(1.dp, GlassRule), shape = RoundedCornerShape(16.dp)),
            focusedBorder = Border(border = BorderStroke(2.dp, GlassFocus), shape = RoundedCornerShape(16.dp))
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.04f)
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 13.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = GlassMuted)
            Text(value.replace('_', ' '), style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun GlassFavoritesHeading(title: String, subtitle: String) {
    Column(Modifier.padding(top = 8.dp, bottom = 1.dp)) {
        Text(title.uppercase(), style = MaterialTheme.typography.titleMedium, color = GlassText)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = GlassMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun GlassHistoryRow(history: SavedHistoryUiModel, onClick: () -> Unit) {
    GlassItemSurface(onClick = onClick) {
        Column(Modifier.weight(1f)) {
            Text(history.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(history.subtitle.orEmpty(), style = MaterialTheme.typography.bodySmall, color = GlassMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(12.dp))
        Text("RESUME", style = MaterialTheme.typography.labelMedium, color = GlassAccent)
    }
}

@Composable
private fun GlassFavoriteRow(item: FavoriteUiModel, onClick: () -> Unit, onLongClick: () -> Unit) {
    GlassItemSurface(onClick = onClick, onLongClick = onLongClick) {
        Column(Modifier.weight(1f)) {
            Text(item.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(item.subtitle.orEmpty(), style = MaterialTheme.typography.bodySmall, color = GlassMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(12.dp))
        Text("⋮", style = MaterialTheme.typography.titleLarge, color = GlassAccent)
    }
}

@Composable
private fun GlassItemSurface(onClick: () -> Unit, onLongClick: () -> Unit = onClick, content: @Composable RowScope.() -> Unit) {
    TvClickableSurface(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = Modifier.fillMaxWidth(),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(18.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = GlassPane,
            focusedContainerColor = GlassPaneFocused,
            contentColor = GlassText,
            focusedContentColor = GlassText
        ),
        border = ClickableSurfaceDefaults.border(
            border = Border(border = BorderStroke(1.dp, GlassRule), shape = RoundedCornerShape(18.dp)),
            focusedBorder = Border(border = BorderStroke(2.dp, GlassFocus), shape = RoundedCornerShape(18.dp))
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.025f)
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 15.dp), verticalAlignment = Alignment.CenterVertically, content = content)
    }
}

@Composable
private fun GlassFavoritesPane(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().background(GlassPane, RoundedCornerShape(22.dp)).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}
