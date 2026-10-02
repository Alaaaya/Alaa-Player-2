package com.streamvault.app.ui.themes.purplegalaxy

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

private class OrbitDestination(val route: String, val glyph: String, val label: String)

private val orbitDestinations = listOf(
    OrbitDestination(Routes.HOME, "◉", "Observatory"),
    OrbitDestination(Routes.LIVE_TV, "✺", "Live"),
    OrbitDestination(Routes.EPG, "☰", "Star map"),
    OrbitDestination(Routes.MOVIES, "◐", "Movies"),
    OrbitDestination(Routes.SERIES, "◑", "Series"),
    OrbitDestination(Routes.FAVORITES, "★", "Saved"),
    OrbitDestination(Routes.SEARCH, "⌕", "Scan"),
    OrbitDestination(Routes.SETTINGS, "⚙", "Control")
)

private fun isOnRoute(current: String, route: String): Boolean =
    current == route || current.startsWith("$route?") || current.startsWith("$route/")

/** Orbit rail: a column of planets that expands into labels while focus is inside it. */
@Composable
internal fun PurpleGalaxyShell(p: ShellParams) {
    var railFocused by remember { mutableStateOf(false) }
    GalaxyBackdrop(p.modifier) {
        Row(Modifier.fillMaxSize()) {
            if (p.topBarVisible) {
                Column(
                    Modifier.fillMaxHeight().width(if (railFocused) 208.dp else 88.dp)
                        .background(Brush.horizontalGradient(listOf(PG.Deep.copy(alpha = 0.92f), Color.Transparent)))
                        .onFocusChanged { railFocused = it.hasFocus }
                        .padding(vertical = 28.dp, horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("✦", color = PG.Flare, fontSize = 26.sp, modifier = Modifier.padding(start = 14.dp, bottom = 18.dp))
                    orbitDestinations.forEach { d ->
                        val active = isOnRoute(p.currentRoute, d.route)
                        GalaxySurface(
                            onClick = { if (!active) p.onNavigate(d.route) },
                            shape = PG.Pill,
                            container = if (active) PG.Plasma.copy(alpha = 0.45f) else Color.Transparent,
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) {
                            Row(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text(d.glyph, color = if (active) PG.Star else PG.Dust, fontSize = 20.sp)
                                if (railFocused) Text(d.label, color = PG.Star, fontSize = 15.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal, maxLines = 1)
                            }
                        }
                    }
                }
            }
            Column(Modifier.weight(1f).fillMaxHeight().padding(p.contentPadding).padding(start = 12.dp, end = 28.dp, top = 22.dp)) {
                if (p.showScreenHeader || p.topBarActions != null) {
                    Row(Modifier.fillMaxWidth().padding(bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (p.showScreenHeader) Column(Modifier.weight(1f)) {
                            GalaxyLabel("Sector")
                            GalaxyTitle(p.title, size = 28)
                            p.subtitle?.takeIf { it.isNotBlank() }?.let { Text(it, color = PG.Muted, fontSize = 13.sp, maxLines = 1) }
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

@Composable
internal fun PurpleGalaxyDashboard(p: DashboardParams) {
    val s = p.uiState
    val first = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { first.requestFocus() } }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 48.dp), verticalArrangement = Arrangement.spacedBy(28.dp)) {
        item {
            Box(Modifier.fillMaxWidth().height(300.dp).clip(PG.Panel).background(PG.Nebula)) {
                s.feature.artworkUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize(), alpha = 0.55f) }
                Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(PG.Void, PG.Void.copy(alpha = 0.4f), Color.Transparent))))
                Column(Modifier.align(Alignment.CenterStart).padding(36.dp).width(520.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    GalaxyLabel("Tonight in orbit", color = PG.Flare)
                    GalaxyTitle(s.feature.title.ifBlank { s.provider?.name ?: "Purple Galaxy" }, size = 38)
                    Text(s.feature.summary, color = PG.Dust, fontSize = 15.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GalaxyChip(s.feature.actionLabel.ifBlank { "Launch" }, true, onClick = {
                            when (s.feature.actionType) {
                                DashboardFeatureAction.LIVE -> p.onNavigate(Routes.LIVE_TV)
                                DashboardFeatureAction.CONTINUE_WATCHING -> s.continueWatching.firstOrNull()?.let(p.onContinueWatchingItemClick) ?: p.onNavigate(Routes.MOVIES)
                                else -> p.onNavigate(Routes.MOVIES)
                            }
                        }, modifier = Modifier.focusRequester(first))
                        GalaxyChip("Star map", false, onClick = { p.onNavigate(Routes.EPG) })
                        GalaxyChip("Scan", false, onClick = { p.onNavigate(Routes.SEARCH) })
                    }
                }
                Column(Modifier.align(Alignment.BottomEnd).padding(24.dp), horizontalAlignment = Alignment.End) {
                    GalaxyLabel("Telemetry")
                    Text("${s.stats.liveChannelCount} live · ${s.stats.movieLibraryCount} films · ${s.stats.seriesLibraryCount} series", color = PG.Dust, fontSize = 13.sp)
                }
            }
        }
        if (s.continueWatching.isNotEmpty()) item {
            GalaxyRowHeader("Resume", "Return trajectory")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                items(s.continueWatching, key = { "cw${it.id}" }) { h ->
                    ArchPoster(h.title, h.posterUrl, formatDuration(h.resumePositionMs), { p.onContinueWatchingItemClick(h) }, Modifier.width(150.dp),
                        progress = if (h.totalDurationMs > 0) h.resumePositionMs.toFloat() / h.totalDurationMs else null)
                }
            }
        }
        val channelRows = listOf(Triple("Docked", "Favorite channels", s.favoriteChannels to p.onFavoriteChannelClick), Triple("Recent", "Last transmissions", s.recentChannels to p.onRecentChannelClick))
        channelRows.forEach { (label, title, pair) ->
            val (channels, click) = pair
            if (channels.isNotEmpty()) item {
                GalaxyRowHeader(label, title)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    items(channels, key = { "$label${it.id}" }) { c ->
                        GalaxySurface(onClick = { click(c, s.currentCombinedProfileId) }, shape = CircleShape, container = Color.Transparent, scale = 1.12f, modifier = Modifier.size(96.dp)) {
                            PlanetLogo(c.name, c.logoUrl, 96.dp)
                            if (c.id in p.recordingChannelIds) Box(Modifier.align(Alignment.TopEnd).size(14.dp).clip(CircleShape).background(PG.Live))
                        }
                    }
                }
            }
        }
        val vodRows = listOf("Recommended" to s.recommendedMovies, "Top rated" to s.topRatedMovies, "Recently added" to s.recentMovies)
        vodRows.forEach { (title, movies) ->
            if (movies.isNotEmpty()) item {
                GalaxyRowHeader("Films", title)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    items(movies, key = { "$title${it.id}" }) { m -> ArchPoster(m.name, m.posterUrl, m.year, { p.onMovieClick(m) }, Modifier.width(150.dp)) }
                }
            }
        }
        if (s.recentSeries.isNotEmpty()) item {
            GalaxyRowHeader("Series", "New constellations")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                items(s.recentSeries, key = { "rs${it.id}" }) { m -> ArchPoster(m.name, m.posterUrl, m.genre, { p.onSeriesClick(m) }, Modifier.width(150.dp)) }
            }
        }
    }
}

/** Live TV as a three-band observatory: constellations (categories), planets (channels), viewport. */
@Composable
internal fun PurpleGalaxyLiveTv(p: LiveTvParams) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        Column(Modifier.width(230.dp).fillMaxHeight()) {
            GalaxyLabel("Constellations")
            Spacer(Modifier.height(8.dp))
            SearchInput(p.categorySearchQuery, p.onCategorySearchChange, "Filter", Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(p.categories, key = { it.id }) { c ->
                    val selected = c.id == p.selectedCategoryId
                    GalaxySurface(
                        onClick = { p.onCategoryClick(c) }, onLongClick = { p.onCategoryLongClick(c) }, shape = PG.Pill,
                        container = if (selected) PG.Plasma.copy(alpha = 0.4f) else Color.Transparent,
                        modifier = Modifier.fillMaxWidth().focusRequester(p.categoryRequester(c.id)).onFocusChanged { if (it.isFocused) p.onCategoryFocused(c) }
                            .onRight { p.onRequestChannelsFromCategory() }
                    ) {
                        Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(if (p.isCategoryLocked(c)) "🔒" else if (selected) "✦" else "·", color = PG.Flare, fontSize = 14.sp, modifier = Modifier.width(20.dp))
                            Text(c.name, color = PG.Star, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            if (c.count > 0) Text("${c.count}", color = PG.Muted, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        Column(Modifier.weight(1f).fillMaxHeight()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { GalaxyLabel(p.sourceTitle.ifBlank { "Transmissions" }); Text("${p.channels.size} planets in range", color = PG.Muted, fontSize = 12.sp) }
                SearchInput(p.channelSearchQuery, p.onChannelSearchChange, "Find channel", Modifier.width(240.dp))
            }
            Spacer(Modifier.height(10.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 6.dp)) {
                items(p.channels, key = { it.id }) { c ->
                    val now = c.currentProgram
                    val progress = now?.let { ((System.currentTimeMillis() - it.startTime).toFloat() / (it.endTime - it.startTime).coerceAtLeast(1)) }
                    GalaxySurface(
                        onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c) }, scale = 1.02f,
                        container = if (c.id == p.previewChannel?.id) PG.GlassStrong else PG.Glass,
                        modifier = Modifier.fillMaxWidth().focusRequester(p.channelRequester(c.id)).onFocusChanged { if (it.isFocused) p.onChannelFocused(c) }
                            .onRight { p.onRequestPreviewFromChannel() }
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text(if (c.number > 0) "${c.number}" else "", color = PG.Muted, fontSize = 13.sp, modifier = Modifier.width(34.dp))
                            PlanetLogo(c.name, if (p.isChannelLocked(c)) null else c.logoUrl, 46.dp)
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(c.name, color = PG.Star, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                Text(if (p.isChannelLocked(c)) "Locked" else now?.title ?: "No signal data", color = PG.Dust, fontSize = 12.sp, maxLines = 1)
                                if (progress != null && !p.isChannelLocked(c)) OrbitProgress(progress, height = 2.dp)
                            }
                            if (c.isFavorite) Text("★", color = PG.Flare)
                        }
                    }
                }
            }
        }
        Column(Modifier.width(360.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            GalaxyLabel("Viewport")
            GalaxySurface(
                onClick = { p.previewChannel?.let(p.onChannelClick) }, shape = PG.Panel, container = PG.Deep, scale = 1.02f,
                modifier = Modifier.fillMaxWidth().height(204.dp).focusRequester(p.previewFocusRequester).onLeft { p.onRequestChannelsFromPreview() }
            ) {
                val engine = p.previewPlayerEngine
                if (engine != null && p.previewChannel != null) PlayerRenderView(engine, PlayerSurfaceResizeMode.FIT, Modifier.fillMaxSize().clip(PG.Panel))
                when {
                    p.previewErrorMessage != null -> Text(p.previewErrorMessage, color = PG.Live, fontSize = 13.sp, modifier = Modifier.align(Alignment.Center).padding(16.dp))
                    p.isPreviewLoading -> Text("Aligning antenna…", color = PG.Dust, modifier = Modifier.align(Alignment.Center))
                    p.previewChannel == null -> Text("Select a planet", color = PG.Muted, modifier = Modifier.align(Alignment.Center))
                }
            }
            p.previewChannel?.let { c ->
                GalaxyTitle(c.name, size = 22)
                c.currentProgram?.let { now ->
                    Text("${formatClock(now.startTime)} – ${formatClock(now.endTime)}", color = PG.Comet, fontSize = 12.sp)
                    Text(now.title, color = PG.Star, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text(now.description, color = PG.Dust, fontSize = 13.sp, maxLines = 4, overflow = TextOverflow.Ellipsis)
                }
                c.nextProgram?.let { next -> Text("Next · ${formatClock(next.startTime)} ${next.title}", color = PG.Muted, fontSize = 12.sp, maxLines = 1) }
            }
        }
    }
}

private val filterLabels = mapOf(
    LibraryFilterType.ALL to "All", LibraryFilterType.FAVORITES to "Saved", LibraryFilterType.IN_PROGRESS to "In orbit",
    LibraryFilterType.UNWATCHED to "Unexplored", LibraryFilterType.TOP_RATED to "Brightest", LibraryFilterType.RECENTLY_UPDATED to "Fresh"
)
private val sortLabels = mapOf(
    LibrarySortBy.LIBRARY to "Default", LibrarySortBy.TITLE to "A–Z", LibrarySortBy.RELEASE to "Release",
    LibrarySortBy.UPDATED to "Updated", LibrarySortBy.RATING to "Rating", LibrarySortBy.WATCH_COUNT to "Most watched"
)

/** Star chart library: constellation chips across the top, arch-window grid below. */
@Composable
internal fun <T> PurpleGalaxyLibrary(
    kind: String,
    p: LibraryParams<T>,
    key: (T) -> Long,
    title: (T) -> String,
    image: (T) -> String?,
    caption: (T) -> String?
) {
    val s = p.uiState
    var sortIndex by remember(s.selectedSort) { mutableStateOf(LibrarySortBy.entries.indexOf(s.selectedSort)) }
    Column(Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) { GalaxyLabel("$kind star chart"); Text("${s.libraryCount} objects catalogued", color = PG.Muted, fontSize = 12.sp) }
            SearchInput(s.searchQuery, p.onQueryChange, "Scan $kind", Modifier.width(280.dp).focusRequester(p.initialFocusRequester))
            GalaxyChip("Sort · ${sortLabels[s.selectedSort]}", false, onClick = {
                sortIndex = (sortIndex + 1) % LibrarySortBy.entries.size
                p.onSortChange(LibrarySortBy.entries[sortIndex])
            })
        }
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(LibraryFilterType.entries) { f -> GalaxyChip(filterLabels[f] ?: f.name, f == s.selectedFilter, { p.onFilterChange(f) }) }
        }
        Spacer(Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(s.categoryNames) { name ->
                val cat = s.categoryFor(name)
                val locked = cat?.let(p.isCategoryLocked) == true
                GalaxySurface(
                    onClick = { cat?.let(p.onCategoryClick) }, onLongClick = { cat?.let(p.onCategoryLongClick) }, shape = PG.Pill,
                    container = if (name == s.selectedCategory) PG.Flare.copy(alpha = 0.4f) else PG.Glass, scale = 1.08f
                ) {
                    Text((if (locked) "🔒 " else "✦ ") + name + (s.categoryCounts[name]?.let { "  $it" } ?: ""), color = PG.Star, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), maxLines = 1)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        val items = s.visibleItems
        if (items.isEmpty() && !s.isLoadingSelectedCategory) GalaxyEmpty("No objects in this sector")
        LazyVerticalGrid(GridCells.Adaptive(150.dp), Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 40.dp, top = 6.dp), horizontalArrangement = Arrangement.spacedBy(18.dp), verticalArrangement = Arrangement.spacedBy(22.dp)) {
            itemsGrid(items, key) { index, item ->
                if (index >= items.size - 12) LaunchedEffect(items.size) {
                    if (s.selectedCategory != null && s.canLoadMoreSelectedCategory) p.onLoadMoreSelected()
                    else if (s.selectedCategory == null && s.hasMorePreviewRows) p.onLoadMorePreview()
                }
                val locked = p.isItemLocked(item)
                ArchPoster(title(item), if (locked) null else image(item), caption(item), { p.onItemClick(item) }, locked = locked, onLongClick = { p.onItemLongClick(item) })
            }
        }
    }
}

private fun <T> androidx.compose.foundation.lazy.grid.LazyGridScope.itemsGrid(items: List<T>, key: (T) -> Long, content: @Composable (Int, T) -> Unit) {
    items(items.size, key = { key(items[it]) }) { i -> content(i, items[i]) }
}
