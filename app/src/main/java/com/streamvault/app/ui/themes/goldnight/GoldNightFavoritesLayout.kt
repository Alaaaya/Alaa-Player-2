package com.streamvault.app.ui.themes.goldnight

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.app.ui.screens.favorites.FavoriteSectionUiModel
import com.streamvault.app.ui.screens.favorites.FavoriteUiModel
import com.streamvault.app.ui.screens.favorites.SavedHistoryUiModel
import com.streamvault.app.ui.screens.favorites.SavedLibraryFilter
import com.streamvault.app.ui.screens.favorites.SavedLibraryPreset
import com.streamvault.app.ui.screens.favorites.SavedLibrarySort

/** Gold Night Favorites: أدوات العرض أعلى الشاشة ثم أعمدة عمودية: متابعة، آخر المباشر، وكل قسم محفوظ في عمود. */
@Composable
internal fun GoldNightFavoritesLayout(
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
    val visibleSections = sections.filter { it.items.isNotEmpty() }
    val empty = visibleSections.isEmpty() && continueWatching.isEmpty() && recentLive.isEmpty()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GoldNightCanvasBrush)
            .padding(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        GoldNightScreenHeader(title = "Favorites")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GoldNightLibraryChip(label = "View: ${selectedPreset.name.goldNightLabel()}", onClick = {
                val all = SavedLibraryPreset.entries
                onPresetSelected(all[(all.indexOf(selectedPreset) + 1) % all.size])
            })
            GoldNightLibraryChip(label = "Type: ${selectedFilter.name.goldNightLabel()}", onClick = {
                val all = SavedLibraryFilter.entries
                onFilterSelected(all[(all.indexOf(selectedFilter) + 1) % all.size])
            })
            GoldNightLibraryChip(label = "Order: ${selectedSort.name.goldNightLabel()}", onClick = {
                val all = SavedLibrarySort.entries
                onSortSelected(all[(all.indexOf(selectedSort) + 1) % all.size])
            })
        }
        if (empty) {
            GoldNightStatePanel("Your collection is empty", "Save a channel, film or series to build your golden list.")
        }
        LazyRow(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            if (continueWatching.isNotEmpty()) {
                item("gn_saved_continue") {
                    GoldNightHistoryColumn(title = "Continue Watching", entries = continueWatching, onClick = onHistoryClick)
                }
            }
            if (recentLive.isNotEmpty()) {
                item("gn_saved_recent") {
                    GoldNightHistoryColumn(title = "Recent Live", entries = recentLive, onClick = onHistoryClick)
                }
            }
            items(visibleSections, key = { "gn_saved_${it.key}" }) { section ->
                Column(modifier = Modifier.width(340.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    GoldNightColumnTitle(title = section.title, trailing = section.items.size.toString())
                    Text(text = section.subtitle, style = MaterialTheme.typography.bodySmall, color = GoldNightMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(section.items, key = { "gn_fav_${section.key}_${it.favorite.id}" }) { item ->
                            GoldNightCard(
                                onClick = { onItemClick(item) },
                                onLongClick = { onItemLongClick(item) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                                    Text(text = item.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    if (!item.subtitle.isNullOrBlank()) {
                                        Text(
                                            text = item.subtitle.orEmpty(),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = GoldNightMuted,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GoldNightHistoryColumn(
    title: String,
    entries: List<SavedHistoryUiModel>,
    onClick: (SavedHistoryUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.width(340.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        GoldNightColumnTitle(title = title, trailing = entries.size.toString())
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(vertical = 4.dp, horizontal = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(entries, key = { "gn_hist_${title}_${it.history.id}" }) { entry ->
                val history = entry.history
                GoldNightMediaRow(
                    title = entry.title,
                    subtitle = entry.subtitle.orEmpty(),
                    imageUrl = history.posterUrl,
                    progress = if (history.totalDurationMs > 0) history.resumePositionMs.toFloat() / history.totalDurationMs.toFloat() else null,
                    onClick = { onClick(entry) }
                )
            }
        }
    }
}
