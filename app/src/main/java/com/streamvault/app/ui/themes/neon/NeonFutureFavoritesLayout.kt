package com.streamvault.app.ui.themes.neon

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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

/** Neon Future saved library is a compact HUD vault that consumes the existing filtered models. */
@Composable
internal fun NeonFutureFavoritesLayout(
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
    LazyColumn(modifier = Modifier.fillMaxSize().background(NeonCanvas), verticalArrangement = Arrangement.spacedBy(18.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 34.dp, top = 28.dp, end = 34.dp, bottom = 34.dp)) {
        item("neon_vault_header") { Column(verticalArrangement = Arrangement.spacedBy(5.dp)) { Text("SAVED VAULT", style = MaterialTheme.typography.displaySmall, color = NeonText, fontWeight = FontWeight.Black); Text("Pinned signals, history and library nodes for this provider.", style = MaterialTheme.typography.bodyLarge, color = NeonMuted) } }
        item("neon_vault_filters") {
            Column(modifier = Modifier.fillMaxWidth().background(NeonPanel, RoundedCornerShape(12.dp)).padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                NeonVaultControl("VIEW", SavedLibraryPreset.entries.toList(), selectedPreset, { it.name.replace('_', ' ') }, onPresetSelected)
                NeonVaultControl("TYPE", SavedLibraryFilter.entries.toList(), selectedFilter, { it.name }, onFilterSelected)
                NeonVaultControl("ORDER", SavedLibrarySort.entries.toList(), selectedSort, { it.name }, onSortSelected)
            }
        }
        if (continueWatching.isNotEmpty()) item("neon_vault_continue") { NeonHistoryShelf("RESUME BUFFER", continueWatching, onHistoryClick) }
        if (recentLive.isNotEmpty()) item("neon_vault_recent") { NeonHistoryShelf("LIVE RECALL", recentLive, onHistoryClick) }
        sections.forEach { section -> item("neon_vault_${section.key}") {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text(section.title.uppercase(), style = MaterialTheme.typography.titleLarge, color = NeonText, fontWeight = FontWeight.Black)
                Text(section.subtitle, style = MaterialTheme.typography.bodyMedium, color = NeonMuted)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) { items(section.items, key = { "${section.key}:${it.favorite.id}" }) { item -> NeonFavoriteNode(item, { onItemClick(item) }, { onItemLongClick(item) }) } }
            }
        } }
    }
}

@Composable private fun <T> NeonVaultControl(label: String, values: List<T>, selected: T, text: (T) -> String, onSelected: (T) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(9.dp), verticalAlignment = Alignment.CenterVertically) { Text(label, Modifier.width(72.dp), style = MaterialTheme.typography.labelMedium, color = NeonCyan, fontWeight = FontWeight.Black); LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) { items(values, key = { text(it) }) { value -> NeonVaultChip(text(value), value == selected) { onSelected(value) } } } }
}
@Composable private fun NeonHistoryShelf(title: String, items: List<SavedHistoryUiModel>, onClick: (SavedHistoryUiModel) -> Unit) { Column(verticalArrangement = Arrangement.spacedBy(9.dp)) { Text(title, style = MaterialTheme.typography.titleLarge, color = NeonText, fontWeight = FontWeight.Black); LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) { items(items, key = { it.history.id.takeIf { id -> id > 0 } ?: it.history.contentId }) { item -> NeonHistoryNode(item) { onClick(item) } } } } }
@Composable private fun NeonFavoriteNode(item: FavoriteUiModel, onClick: () -> Unit, onLongClick: () -> Unit) { val shape = RoundedCornerShape(8.dp); TvClickableSurface(onClick = onClick, onLongClick = onLongClick, modifier = Modifier.width(210.dp), shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = NeonPanel, focusedContainerColor = NeonPanelRaised, contentColor = NeonText, focusedContentColor = NeonText), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, NeonLime), shape = shape))) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) { Text(neonContentType(item.favorite.contentType), style = MaterialTheme.typography.labelMedium, color = NeonPink, fontWeight = FontWeight.Black); Text(item.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis); Text(item.subtitle.orEmpty().ifBlank { "Saved provider node" }, style = MaterialTheme.typography.bodySmall, color = NeonMuted, maxLines = 2, overflow = TextOverflow.Ellipsis); Text("HOLD FOR ACTIONS", style = MaterialTheme.typography.labelSmall, color = NeonCyan) } } }
@Composable private fun NeonHistoryNode(item: SavedHistoryUiModel, onClick: () -> Unit) { val shape = RoundedCornerShape(8.dp); TvClickableSurface(onClick = onClick, modifier = Modifier.width(210.dp), shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = NeonPanel, focusedContainerColor = NeonPanelRaised, contentColor = NeonText, focusedContentColor = NeonText), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, NeonPink), shape = shape))) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) { Text(neonContentType(item.history.contentType), style = MaterialTheme.typography.labelMedium, color = NeonPink, fontWeight = FontWeight.Black); Text(item.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis); Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = NeonMuted, maxLines = 2, overflow = TextOverflow.Ellipsis) } } }
@Composable private fun NeonVaultChip(label: String, selected: Boolean, onClick: () -> Unit) { val shape = RoundedCornerShape(999.dp); TvClickableSurface(onClick = onClick, shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = if (selected) NeonCyan.copy(alpha = .2f) else NeonCanvas, focusedContainerColor = NeonPanelRaised, contentColor = if (selected) NeonCyan else NeonMuted, focusedContentColor = NeonText), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, NeonCyan), shape = shape))) { Text(label, Modifier.padding(horizontal = 13.dp, vertical = 8.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold) } }
private fun neonContentType(type: ContentType): String = when (type) { ContentType.LIVE -> "LIVE"; ContentType.MOVIE, ContentType.VOD -> "FILM"; ContentType.SERIES, ContentType.SERIES_EPISODE -> "SERIES" }
