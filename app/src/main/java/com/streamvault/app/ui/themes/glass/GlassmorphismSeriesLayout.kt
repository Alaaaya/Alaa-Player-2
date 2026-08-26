package com.streamvault.app.ui.themes.glass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.streamvault.app.ui.components.SearchInput
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.app.ui.screens.series.SeriesUiState
import com.streamvault.domain.model.Category
import com.streamvault.domain.model.LibraryFilterType
import com.streamvault.domain.model.LibrarySortBy
import com.streamvault.domain.model.Series

/** مكتبة مسلسلات Glass: لوح فئات عائم وتدفق حلقات زجاجي عمودي. */
@Composable
internal fun GlassmorphismSeriesLayout(
    uiState: SeriesUiState,
    initialFocusRequester: FocusRequester,
    isCategoryLocked: (Category) -> Boolean,
    isSeriesLocked: (Series) -> Boolean,
    onCategoryClick: (Category) -> Unit,
    onCategoryLongClick: (Category) -> Unit,
    onSeriesClick: (Series) -> Unit,
    onSeriesLongClick: (Series) -> Unit,
    onQueryChange: (String) -> Unit,
    onFilterChange: (LibraryFilterType) -> Unit,
    onSortChange: (LibrarySortBy) -> Unit,
    onLoadMoreSelected: () -> Unit,
    onLoadMorePreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val series = if (uiState.selectedCategory == null) uiState.seriesByCategory.values.flatten().distinctBy { it.id } else uiState.selectedCategoryItems
    val loading = if (uiState.selectedCategory == null) uiState.isLoadingPreviewRows else uiState.isLoadingSelectedCategory || uiState.isLoadingMoreSelectedCategory
    val canLoadMore = if (uiState.selectedCategory == null) uiState.hasMorePreviewRows else uiState.canLoadMoreSelectedCategory
    Column(modifier = modifier.fillMaxSize().background(GlassCanvas).padding(28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("SERIES GLASS LIBRARY", style = MaterialTheme.typography.headlineMedium, color = GlassText)
                Text("SEASONS IN A TRANSLUCENT STREAM / ${series.size} TITLES", style = MaterialTheme.typography.labelMedium, color = GlassMuted)
            }
            SearchInput(value = uiState.searchQuery, onValueChange = onQueryChange, placeholder = "Search series", focusRequester = initialFocusRequester)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GlassSeriesAction("FILTER / ${uiState.selectedLibraryFilterType.name}") { onFilterChange(LibraryFilterType.entries[(LibraryFilterType.entries.indexOf(uiState.selectedLibraryFilterType) + 1) % LibraryFilterType.entries.size]) }
            GlassSeriesAction("SORT / ${uiState.selectedLibrarySortBy.name}") { onSortChange(LibrarySortBy.entries[(LibrarySortBy.entries.indexOf(uiState.selectedLibrarySortBy) + 1) % LibrarySortBy.entries.size]) }
        }
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            GlassSeriesPane(Modifier.width(238.dp).fillMaxHeight()) {
                Text("SEASON GROUPS", style = MaterialTheme.typography.labelLarge, color = GlassAccent)
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    items(uiState.categories, key = { it.id }) { category ->
                        GlassSeriesLine(if (isCategoryLocked(category)) "LOCKED" else category.name, if (category.name == uiState.selectedCategory) "ACTIVE" else "", category.name == uiState.selectedCategory, { onCategoryClick(category) }, { onCategoryLongClick(category) })
                    }
                }
            }
            GlassSeriesPane(Modifier.weight(1f).fillMaxHeight()) {
                Text("SERIES STREAM", style = MaterialTheme.typography.labelLarge, color = GlassAccent)
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    if (!uiState.hasActiveProvider) item { GlassSeriesState("NO PROVIDER", "Choose an active provider before opening series.") }
                    else if (!uiState.errorMessage.isNullOrBlank()) item { GlassSeriesState("CATALOGUE ERROR", uiState.errorMessage) }
                    else if (series.isEmpty() && !loading) item { GlassSeriesState("EMPTY SELECTION", "No series match this selection.") }
                    items(series, key = { it.id }) { entry ->
                        GlassSeriesLine(if (isSeriesLocked(entry)) "LOCKED" else entry.name, if (isSeriesLocked(entry)) "Protected" else entry.genre ?: entry.releaseDate.orEmpty(), false, { onSeriesClick(entry) }, { onSeriesLongClick(entry) })
                    }
                    if (loading) item { GlassSeriesState("LOADING", "Loading series entries…") }
                    if (canLoadMore && !loading) item { GlassSeriesAction("LOAD MORE") { if (uiState.selectedCategory == null) onLoadMorePreview() else onLoadMoreSelected() } }
                }
            }
        }
    }
}

@Composable private fun GlassSeriesPane(modifier: Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier = modifier, shape = RoundedCornerShape(26.dp), colors = SurfaceDefaults.colors(containerColor = GlassPane), border = Border(border = BorderStroke(1.dp, GlassRule), shape = RoundedCornerShape(26.dp))) { Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content) }
}
@Composable private fun GlassSeriesAction(label: String, onClick: () -> Unit) = GlassSeriesLine(label, "", false, onClick, onClick)
@Composable private fun GlassSeriesLine(label: String, detail: String, selected: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    TvClickableSurface(onClick = onClick, onLongClick = onLongClick, modifier = Modifier.fillMaxWidth(), shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = if (selected) GlassAccent.copy(alpha = .22f) else Color.Transparent, focusedContainerColor = GlassPaneFocused, contentColor = GlassText, focusedContentColor = GlassText), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, GlassFocus), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.025f)) { Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) { Text(label, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis); if (detail.isNotBlank()) Text(detail, style = MaterialTheme.typography.labelSmall, color = GlassMuted, maxLines = 1, overflow = TextOverflow.Ellipsis) } }
}
@Composable private fun GlassSeriesState(title: String, subtitle: String) { Column(modifier = Modifier.fillMaxWidth().background(GlassCanvasDeep, RoundedCornerShape(18.dp)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) { Text(title, style = MaterialTheme.typography.labelLarge, color = GlassText); Text(subtitle, style = MaterialTheme.typography.bodySmall, color = GlassMuted) } }
