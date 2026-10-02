package com.streamvault.app.ui.themes.techdashboard

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.border
import com.streamvault.app.ui.themes.bespoke.qualityBadge
import com.streamvault.app.ui.themes.bespoke.tr
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.streamvault.app.navigation.Routes
import com.streamvault.app.ui.components.PlayerRenderView
import com.streamvault.app.ui.components.SearchInput
import com.streamvault.app.ui.screens.dashboard.DashboardFeatureAction
import com.streamvault.app.ui.themes.bespoke.DashboardParams
import com.streamvault.app.ui.themes.bespoke.LibraryParams
import com.streamvault.app.ui.themes.bespoke.LiveTvParams
import com.streamvault.app.ui.themes.bespoke.ShellParams
import com.streamvault.domain.model.LibraryFilterType
import com.streamvault.domain.model.LibrarySortBy
import com.streamvault.player.PlayerSurfaceResizeMode

private class TechDashDestination(val route: String, val glyph: String, val label: String)

private val destinations = listOf(
    TechDashDestination(Routes.HOME, "▣", "Overview"),
    TechDashDestination(Routes.LIVE_TV, "◉", "Live"),
    TechDashDestination(Routes.EPG, "▤", "Guide"),
    TechDashDestination(Routes.MOVIES, "▶", "Movies"),
    TechDashDestination(Routes.SERIES, "≡", "Series"),
    TechDashDestination(Routes.FAVORITES, "★", "Saved"),
    TechDashDestination(Routes.SEARCH, "⌕", "Query"),
    TechDashDestination(Routes.SETTINGS, "⚙", "Config")
)

private fun isOnRoute(current: String, route: String): Boolean =
    current == route || current.startsWith("$route?") || current.startsWith("$route/")

/** Top command bar: brand mark, horizontal destination tabs and a live clock. */
@Composable
internal fun TechDashShell(p: ShellParams) {
    TechDashBackdrop(p.modifier) {
        Column(Modifier.fillMaxSize()) {
            if (p.topBarVisible) {
                Row(
                    Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(TD.Deep.copy(alpha = 0.95f), TD.Deep.copy(alpha = 0.6f))))
                        .padding(horizontal = 32.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(TD.BRAND, color = TD.Plasma, fontSize = 20.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp, modifier = Modifier.padding(end = 18.dp))
                    destinations.forEach { d ->
                        val active = isOnRoute(p.currentRoute, d.route)
                        TechDashSurface(
                            onClick = { if (!active) p.onNavigate(d.route) }, shape = TD.Pill,
                            container = if (active) TD.Plasma.copy(alpha = 0.3f) else Color.Transparent, scale = 1.06f
                        ) {
                            Row(Modifier.padding(horizontal = 14.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(d.glyph, color = if (active) TD.Plasma else TD.Dust, fontSize = 15.sp)
                                Text(d.label, color = if (active) TD.Star else TD.Dust, fontSize = 14.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal, maxLines = 1)
                            }
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Text(formatClock(System.currentTimeMillis()), color = TD.Star, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(TD.Plasma.copy(alpha = 0.35f)))
            }
            Column(Modifier.weight(1f).fillMaxWidth().padding(p.contentPadding).padding(start = 32.dp, end = 32.dp, top = 18.dp)) {
                if (p.showScreenHeader || p.topBarActions != null) {
                    Row(Modifier.fillMaxWidth().padding(bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (p.showScreenHeader) Column(Modifier.weight(1f)) {
                            TechDashLabel(TD.SECTOR)
                            TechDashTitle(p.title, size = 26)
                            p.subtitle?.takeIf { it.isNotBlank() }?.let { Text(it, color = TD.Muted, fontSize = 13.sp, maxLines = 1) }
                        } else Spacer(Modifier.weight(1f))
                        p.topBarActions?.let { Row(horizontalArrangement = Arrangement.spacedBy(8.dp), content = it) }
                    }
                }
                p.header?.let { Column(content = it) }
                Column(Modifier.fillMaxSize(), content = p.content)
            }
        }
    }
}




private val filterLabels = mapOf(
    LibraryFilterType.ALL to "All", LibraryFilterType.FAVORITES to "Saved", LibraryFilterType.IN_PROGRESS to "Active",
    LibraryFilterType.UNWATCHED to "No data", LibraryFilterType.TOP_RATED to "Brightest", LibraryFilterType.RECENTLY_UPDATED to "Fresh"
)
private val sortLabels = mapOf(
    LibrarySortBy.LIBRARY to "Default", LibrarySortBy.TITLE to "A–Z", LibrarySortBy.RELEASE to "Release",
    LibrarySortBy.UPDATED to "Updated", LibrarySortBy.RATING to "Rating", LibrarySortBy.WATCH_COUNT to "Most watched"
)

/** Library as a data browser: query toolbar with segmented filters, dense grid, numbered genre column on the START side. */
@Composable
internal fun <T> TechDashLibrary(
    kind: String,
    p: LibraryParams<T>,
    key: (T) -> Long,
    title: (T) -> String,
    image: (T) -> String?,
    caption: (T) -> String?
) {
    val s = p.uiState
    var sortIndex by remember(s.selectedSort) { mutableStateOf(LibrarySortBy.entries.indexOf(s.selectedSort)) }
    // Alaa rule: genre/category column on the START side, poster grid beside it.
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        TdModule(tr("Genres", "التصنيفات"), Modifier.width(230.dp).fillMaxHeight(), meta = "${s.categoryNames.size}") {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                item {
                    TechDashSurface(onClick = { s.categoryFor(s.categoryNames.firstOrNull() ?: "")?.let(p.onCategoryClick) }, shape = TD.Pill,
                        container = if (s.selectedCategory == null) TD.Plasma.copy(alpha = 0.3f) else Color.Transparent, modifier = Modifier.fillMaxWidth()) {
                        Text("*  " + tr("ALL", "الكل") + "  [${s.libraryCount}]", color = TD.Plasma, fontSize = 12.sp, fontFamily = TD.Mono, modifier = Modifier.padding(6.dp))
                    }
                }
                itemsIndexed(s.categoryNames) { i, name ->
                    val cat = s.categoryFor(name)
                    val locked = cat?.let(p.isCategoryLocked) == true
                    val selected = name == s.selectedCategory
                    TechDashSurface(
                        onClick = { cat?.let(p.onCategoryClick) }, onLongClick = { cat?.let(p.onCategoryLongClick) }, shape = TD.Pill,
                        container = if (selected) TD.Plasma.copy(alpha = 0.3f) else Color.Transparent, modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(horizontal = 6.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(if (locked) "🔒" else "%02d".format(i + 1), color = if (selected) TD.Plasma else TD.Muted, fontSize = 11.sp, fontFamily = TD.Mono, modifier = Modifier.width(26.dp))
                            Text(name, color = TD.Star, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            s.categoryCounts[name]?.let { Text("$it", color = TD.Muted, fontSize = 10.sp, fontFamily = TD.Mono) }
                        }
                    }
                }
            }
        }
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                Modifier.fillMaxWidth().clip(TD.Panel).background(TD.Deep.copy(alpha = 0.85f)).border(1.dp, TD.Plasma.copy(alpha = 0.22f), TD.Panel).padding(8.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("SELECT * FROM ${kind.uppercase()}", color = TD.Plasma, fontSize = 12.sp, fontFamily = TD.Mono, fontWeight = FontWeight.Bold)
                Text("[${s.libraryCount}]", color = TD.Muted, fontSize = 12.sp, fontFamily = TD.Mono)
                SearchInput(s.searchQuery, p.onQueryChange, "WHERE title LIKE …", Modifier.width(260.dp).focusRequester(p.initialFocusRequester))
                Row(Modifier.weight(1f).clip(TD.Pill).border(1.dp, TD.Plasma.copy(alpha = 0.3f), TD.Pill)) {
                    LibraryFilterType.entries.forEach { f ->
                        val sel = f == s.selectedFilter
                        TechDashSurface(onClick = { p.onFilterChange(f) }, shape = TD.Pill, container = if (sel) TD.Plasma else Color.Transparent, modifier = Modifier.weight(1f)) {
                            Text(filterLabels[f] ?: f.name, color = if (sel) TD.Void else TD.Dust, fontSize = 11.sp, fontFamily = TD.Mono, maxLines = 1, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp))
                        }
                    }
                }
                TechDashChip("ORDER BY ${sortLabels[s.selectedSort]}", false, onClick = {
                    sortIndex = (sortIndex + 1) % LibrarySortBy.entries.size
                    p.onSortChange(LibrarySortBy.entries[sortIndex])
                })
            }
            Box(Modifier.fillMaxSize()) {
                val items = s.visibleItems
                if (items.isEmpty() && !s.isLoadingSelectedCategory) TechDashEmpty(tr("Query returned 0 rows", "لا يوجد محتوى هنا"))
                LazyVerticalGrid(GridCells.Adaptive(128.dp), Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 40.dp, top = 4.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    itemsGrid(items, key) { index, item ->
                        if (index >= items.size - 12) LaunchedEffect(items.size) {
                            if (s.selectedCategory != null && s.canLoadMoreSelectedCategory) p.onLoadMoreSelected()
                            else if (s.selectedCategory == null && s.hasMorePreviewRows) p.onLoadMorePreview()
                        }
                        val locked = p.isItemLocked(item)
                        TechDashPoster(title(item), if (locked) null else image(item), caption(item), { p.onItemClick(item) }, locked = locked, onLongClick = { p.onItemLongClick(item) })
                    }
                }
            }
        }
    }
}

private fun <T> androidx.compose.foundation.lazy.grid.LazyGridScope.itemsGrid(items: List<T>, key: (T) -> Long, content: @Composable (Int, T) -> Unit) {
    items(items.size, key = { key(items[it]) }) { i -> content(i, items[i]) }
}
