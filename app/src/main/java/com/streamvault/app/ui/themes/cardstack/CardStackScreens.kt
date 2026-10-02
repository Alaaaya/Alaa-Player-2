package com.streamvault.app.ui.themes.cardstack

import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.streamvault.app.ui.screens.favorites.SavedLibraryFilter
import com.streamvault.app.ui.screens.favorites.SavedLibraryPreset
import com.streamvault.app.ui.screens.favorites.SavedLibrarySort
import com.streamvault.app.ui.screens.search.SearchTab
import com.streamvault.app.ui.themes.bespoke.FavoritesParams
import com.streamvault.app.ui.themes.bespoke.MovieDetailParams
import com.streamvault.app.ui.themes.bespoke.SearchParams
import com.streamvault.app.ui.themes.bespoke.SeriesDetailParams
import com.streamvault.app.ui.themes.bespoke.SettingsNavParams
import com.streamvault.app.ui.themes.bespoke.qualityBadge
import com.streamvault.app.ui.themes.bespoke.tr

/** Full-bleed backdrop with left scrim, the streaming-service detail look. */
@Composable
private fun CsBackdrop(url: String?, content: @Composable BoxScope.() -> Unit) {
    CsBackground {
        url?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(CS.Bg, CS.Bg.copy(alpha = 0.85f), CS.Bg.copy(alpha = 0.2f)))))
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, CS.Bg))))
        content()
    }
}

@Composable
private fun Meta(vararg parts: String?) {
    Text(parts.filterNot { it.isNullOrBlank() }.joinToString("  •  "), color = CS.Sub, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

/** Movie: fanned poster stack on the start, info in the middle, actions as a vertical deck of cards on the end; versions + related below. */
@Composable
internal fun CardStackMovieDetail(p: MovieDetailParams) {
    val m = p.movie
    val play = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(m.id) { runCatching { play.requestFocus() } }
    CsBackdrop(m.backdropUrl ?: m.posterUrl) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 64.dp, end = 64.dp, top = 80.dp, bottom = 48.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
              Row(horizontalArrangement = Arrangement.spacedBy(40.dp), verticalAlignment = Alignment.Top) {
                Box(Modifier.width(250.dp).height(370.dp)) {
                    Box(Modifier.matchParentSize().padding(start = 30.dp, top = 10.dp).graphicsLayer { rotationZ = 7f }.clip(CS.R).background(CS.AmberDeep.copy(alpha = 0.5f)))
                    Box(Modifier.matchParentSize().padding(start = 15.dp, top = 5.dp).graphicsLayer { rotationZ = 3.5f }.clip(CS.R).background(CS.Line))
                    Box(Modifier.matchParentSize().padding(end = 20.dp).clip(CS.R).background(CS.Card)) { m.posterUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) } }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(m.name, color = CS.Text, fontSize = 46.sp, fontWeight = FontWeight.Black, maxLines = 2, lineHeight = 50.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (m.rating > 0) CsBadge("★ %.1f".format(m.rating), CS.Amber, filled = true)
                        Meta(m.year, m.duration, m.genre, m.variantLabel)
                    }
                    Text(m.plot.orEmpty(), color = CS.Sub, fontSize = 15.sp, maxLines = 4, overflow = TextOverflow.Ellipsis, lineHeight = 22.sp)
                    m.director?.takeIf { it.isNotBlank() }?.let { Text(tr("Director", "المخرج") + "  $it", color = CS.Faint, fontSize = 13.sp, maxLines = 1) }
                    m.cast?.takeIf { it.isNotBlank() }?.let { Text(tr("Starring", "بطولة") + "  $it", color = CS.Faint, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    if (p.hasResume) Text(tr("Resume from", "استكمال من") + " " + csDuration(p.resumePositionMs), color = CS.Amber, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                LazyColumn(Modifier.width(230.dp).height(380.dp), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(6.dp)) {
                    item { CsButton(if (p.hasResume) tr("Resume", "استكمال") else tr("Play", "تشغيل"), p.onPlay, Modifier.focusRequester(play), primary = true, icon = "▶") }
                    p.onPlayTrailer?.let { item { CsButton(tr("Trailer", "الإعلان"), it, icon = "▷") } }
                    item { CsButton(if (m.isFavorite) tr("In My List", "في قائمتي") else tr("My List", "قائمتي"), p.onToggleFavorite, icon = if (m.isFavorite) "✓" else "+") }
                    item { CsButton(if (p.isCasting) tr("Casting…", "جار البث…") else tr("Cast", "بث"), p.onCast, icon = "⎚") }
                    item { CsButton(tr("Download", "تنزيل"), p.onDownload, icon = "↓") }
                    item { CsButton(tr("Copy link", "نسخ الرابط"), p.onCopyUrl, icon = "⧉") }
                    item { CsButton(tr("Back", "رجوع"), p.onBack, icon = "←") }
                }
              }
            }
            if (m.variants.size > 1) item {
                Column {
                    CsRowTitle(tr("Versions", "النسخ"))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(m.variants) { v -> CsTab(v.label, v.rawMovieId == m.selectedVariantId, { p.onSelectVariant(v.rawMovieId) }) }
                    }
                }
            }
            if (p.relatedContent.isNotEmpty()) item {
                Column {
                    CsRowTitle(tr("More like this", "مشابه"))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                        items(p.relatedContent, key = { it.id }) { r -> CsPoster(r.name, r.posterUrl, r.year, { p.onRelatedClick(r) }, Modifier.width(140.dp)) }
                    }
                }
            }
        }
    }
}

/** Series: hero on top, then a vertical deck of season cards beside a column of episode cards. */
@Composable
internal fun CardStackSeriesDetail(p: SeriesDetailParams) {
    val s = p.series
    val primary = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(s.id) { runCatching { primary.requestFocus() } }
    CsBackdrop(s.backdropUrl ?: s.posterUrl) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 64.dp, end = 64.dp, top = 64.dp, bottom = 48.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Column(Modifier.widthIn(max = 640.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(s.name, color = CS.Text, fontSize = 42.sp, fontWeight = FontWeight.Black, maxLines = 2)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (s.rating > 0) CsBadge("★ %.1f".format(s.rating), CS.Amber, filled = true)
                        if (p.unwatchedEpisodeCount > 0) CsBadge("${p.unwatchedEpisodeCount} " + tr("new", "جديد"), CS.Blue)
                        Meta(s.releaseDate, s.genre, "${s.seasons.size} " + tr("seasons", "مواسم"))
                    }
                    if (!p.isLoadingExternalRatings) {
                        val r = p.externalRatings
                        val ext = listOf("IMDb" to r.imdb, "Rotten Tomatoes" to r.rottenTomatoes, "Metacritic" to r.metacritic).filter { it.second.available }
                        if (ext.isNotEmpty()) Text(ext.joinToString("   ") { "${it.first} ${it.second.displayValue}" }, color = CS.Faint, fontSize = 13.sp)
                    }
                    Text(s.plot.orEmpty(), color = CS.Sub, fontSize = 15.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                }
            }
            item {
                val resume = p.resumeEpisode
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CsButton(
                        if (resume != null) tr("Continue", "متابعة") + " S${resume.seasonNumber}:E${resume.episodeNumber}" else tr("Play", "تشغيل"),
                        { (resume ?: p.selectedSeason?.episodes?.firstOrNull())?.let(p.onResumeClick) }, Modifier.focusRequester(primary), primary = true, icon = "▶"
                    )
                    CsButton(if (s.isFavorite) tr("In My List", "في قائمتي") else tr("My List", "قائمتي"), p.onToggleFavorite, icon = if (s.isFavorite) "✓" else "+")
                    if (resume != null) CsButton(if (p.isCasting) tr("Casting…", "جار البث…") else tr("Cast", "بث"), p.onCastResumeEpisode, icon = "⎚")
                    CsButton(tr("Back", "رجوع"), p.onBack, icon = "←")
                }
            }
            if (s.variants.size > 1) item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(s.variants) { v -> CsTab(v.label, v.rawSeriesId == s.selectedVariantId, { p.onSelectVariant(v.rawSeriesId) }) } }
            }
            item {
              Row(Modifier.height(560.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                LazyColumn(Modifier.width(200.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(6.dp)) {
                    items(s.seasons, key = { it.seasonNumber }) { season ->
                        val sel = season.seasonNumber == p.selectedSeason?.seasonNumber
                        CsCard({ p.onSeasonSelected(season) }, Modifier.fillMaxWidth().height(56.dp), shape = CS.RSmall, container = if (sel) CS.Amber else CS.Raised, focusedContainer = if (sel) CS.Amber else CS.Card) {
                            Column(Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalArrangement = Arrangement.Center) {
                                Text(season.name.ifBlank { tr("Season", "الموسم") + " ${season.seasonNumber}" }, color = if (sel) CS.Bg else CS.Text, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text("${season.episodes.size} " + tr("episodes", "حلقة"), color = if (sel) CS.Bg.copy(alpha = 0.7f) else CS.Faint, fontSize = 11.sp)
                            }
                        }
                    }
                }
                LazyColumn(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(6.dp)) {
            items(p.selectedSeason?.episodes.orEmpty(), key = { it.id }) { e ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CsCard(onClick = { p.onEpisodeClick(e) }, modifier = Modifier.weight(1f), container = CS.Raised.copy(alpha = 0.85f), zoom = 1.02f) {
                        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Box(Modifier.width(200.dp).aspectRatio(16f / 9f).clip(CS.RSmall).background(CS.Card)) {
                                e.coverUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                                Text("▶", color = CS.Text, fontSize = 22.sp, modifier = Modifier.align(Alignment.Center))
                                if (e.watchProgress > 0 && e.durationSeconds > 0) CsProgress(e.watchProgress / (e.durationSeconds * 1000f), Modifier.align(Alignment.BottomCenter).padding(8.dp), 3.dp)
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("${e.episodeNumber}. ${e.title}", color = CS.Text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                e.duration?.let { Text(it, color = CS.Faint, fontSize = 12.sp) }
                                Text(e.plot.orEmpty(), color = CS.Sub, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        CsRound("⎚", { p.onCastEpisode(e) }, size = 40.dp)
                        CsRound("↓", { p.onDownloadEpisode(e) }, size = 40.dp)
                        CsRound("⧉", { p.onCopyEpisodeUrl(e) }, size = 40.dp)
                    }
                }
            }
                }
              }
            }
        }
    }
}

/** Search: a side "draw" box with the field and section cards stacked vertically; results as shelves on the right. */
@Composable
internal fun CardStackSearch(p: SearchParams) {
    LaunchedEffect(Unit) { runCatching { p.searchFocusRequester.requestFocus() } }
    val s = p.uiState
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(26.dp)) {
    Column(Modifier.width(300.dp).fillMaxHeight().clip(CS.R).background(CS.Raised.copy(alpha = 0.7f)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(tr("Draw a card", "ابحث"), color = CS.Amber, fontSize = 13.sp, fontWeight = FontWeight.Black)
        CsSearchField(p.query, p.onQueryChange, tr("Search", "بحث"), Modifier.fillMaxWidth().focusRequester(p.searchFocusRequester), onSubmit = p.onSearch)
        SearchTab.entries.forEach { t ->
            val label = when (t.name) { "ALL" -> tr("All", "الكل"); "LIVE" -> tr("Live", "مباشر"); "MOVIES" -> tr("Movies", "أفلام"); "SERIES" -> tr("Series", "مسلسلات"); else -> t.name }
            val sel = t == p.selectedTab
            CsCard({ p.onTabSelected(t) }, Modifier.fillMaxWidth().height(46.dp), shape = CS.RSmall, container = if (sel) CS.Amber else CS.Card, focusedContainer = if (sel) CS.Amber else Color(0xFF3A2A40)) {
                Text(label, color = if (sel) CS.Bg else CS.Text, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterStart).padding(horizontal = 14.dp))
            }
        }
        CsTab("↻ " + tr("Full index", "فهرسة كاملة"), false, p.onBuildCompleteIndex)
    }
    LazyColumn(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(22.dp), contentPadding = PaddingValues(bottom = 48.dp)) {
        when {
            p.query.isBlank() -> if (p.recentQueries.isNotEmpty()) item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    CsRowTitle(tr("Recent searches", "عمليات البحث الأخيرة"))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(p.recentQueries) { q -> CsTab("↺ $q", false, { p.onRecentQuerySelected(q) }) }
                        item { CsTab(tr("Clear", "مسح"), false, p.onClearRecentQueries) }
                    }
                }
            }
            s.isLoading -> item { CsEmpty(tr("Searching…", "جار البحث…")) }
            s.hasSearchError -> item { CsEmpty(tr("Search failed. Try again.", "فشل البحث. حاول مرة أخرى.")) }
            s.isEmpty -> item { CsEmpty(tr("No results for", "لا نتائج لـ") + " “${p.query}”") }
            else -> {
                if (s.channels.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.LIVE)) item {
                    Column {
                        CsRowTitle(tr("Live channels", "قنوات مباشرة"), trailing = "${s.channels.size}")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                            items(s.channels, key = { it.id }) { c ->
                                val locked = p.isChannelLocked(c)
                                CsCard(onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c) }, modifier = Modifier.width(240.dp).height(110.dp), container = CS.Raised) {
                                    Row(Modifier.fillMaxSize().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        CsLogo(c.name, if (locked) null else c.logoUrl, 54.dp)
                                        Column(Modifier.weight(1f)) {
                                            Text(c.name, color = CS.Text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(if (locked) "🔒" else c.currentProgram?.title.orEmpty(), color = CS.Faint, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) {
                                                if (c.id in p.recordingChannelIds) CsBadge("REC", CS.Live, filled = true)
                                                else if (c.id in p.scheduledChannelIds) CsBadge("◷", CS.Blue)
                                                c.qualityBadge()?.let { CsBadge(it, CS.Text) }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (s.movies.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.MOVIES)) item {
                    Column {
                        CsRowTitle(tr("Movies", "أفلام"), trailing = "${s.movies.size}")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                            items(s.movies, key = { it.id }) { m -> val l = p.isMovieLocked(m); CsPoster(m.name, if (l) null else m.posterUrl, m.year, { p.onMovieClick(m) }, Modifier.width(140.dp), locked = l, onLongClick = { p.onMovieLongClick(m) }) }
                        }
                    }
                }
                if (s.series.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.SERIES)) item {
                    Column {
                        CsRowTitle(tr("Series", "مسلسلات"), trailing = "${s.series.size}")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                            items(s.series, key = { it.id }) { m -> val l = p.isSeriesLocked(m); CsPoster(m.name, if (l) null else m.posterUrl, m.genre, { p.onSeriesClick(m) }, Modifier.width(140.dp), locked = l, onLongClick = { p.onSeriesLongClick(m) }) }
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
private fun presetLabel(p: SavedLibraryPreset) = when (p) {
    SavedLibraryPreset.ALL_SAVED -> tr("Everything", "الكل")
    SavedLibraryPreset.HOME_SHELF -> tr("Home shelf", "رف الرئيسية")
    SavedLibraryPreset.WATCH_NEXT -> tr("Watch next", "التالي")
    SavedLibraryPreset.LIVE_RECALL -> tr("Live", "مباشر")
    SavedLibraryPreset.MOVIES -> tr("Movies", "أفلام")
    SavedLibraryPreset.SERIES -> tr("Series", "مسلسلات")
    SavedLibraryPreset.CUSTOM_GROUPS -> tr("Groups", "مجموعات")
}

/** My List: grid of saved cards on the start, preset decks as a vertical card column on the END. */
@Composable
internal fun CardStackFavorites(p: FavoritesParams) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
    Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            items(SavedLibraryFilter.entries) { f -> CsTab(f.name.lowercase().replaceFirstChar { it.uppercase() }, f == p.selectedFilter, { p.onFilterSelected(f) }) }
            item { Text("   ⇅", color = CS.Faint, fontSize = 14.sp) }
            items(SavedLibrarySort.entries) { o -> CsTab(o.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }, o == p.selectedSort, { p.onSortSelected(o) }) }
        }
        LazyVerticalGrid(GridCells.Adaptive(150.dp), Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(18.dp), verticalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(6.dp, 6.dp, 6.dp, 48.dp)) {
            if (p.continueWatching.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) { CsRowTitle(tr("Continue watching", "متابعة المشاهدة")) }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(p.continueWatching, key = { "c${it.history.id}" }) { h ->
                            val prog = if (h.history.totalDurationMs > 0) h.history.resumePositionMs.toFloat() / h.history.totalDurationMs else null
                            CsWide(h.title, h.history.posterUrl, h.subtitle, prog, { p.onHistoryClick(h) }, Modifier.width(260.dp))
                        }
                    }
                }
            }
            if (p.recentLive.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) { CsRowTitle(tr("Recently watched live", "مباشر شوهد مؤخراً")) }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(p.recentLive, key = { "l${it.history.id}" }) { h ->
                            CsCard(onClick = { p.onHistoryClick(h) }, modifier = Modifier.width(200.dp), container = CS.Raised) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    CsLogo(h.title, h.history.posterUrl, 40.dp)
                                    Column { Text(h.title, color = CS.Text, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(h.subtitle, color = CS.Faint, fontSize = 11.sp, maxLines = 1) }
                                }
                            }
                        }
                    }
                }
            }
            val sections = p.sections.filter { it.items.isNotEmpty() }
            if (sections.isEmpty() && p.continueWatching.isEmpty() && p.recentLive.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
                CsEmpty(tr("Your list is empty. Long-press any title to add it.", "قائمتك فارغة. اضغط مطولاً على أي عنصر لإضافته."))
            }
            sections.forEach { section ->
                item(key = "h${section.key}", span = { GridItemSpan(maxLineSpan) }) { CsRowTitle(section.title, trailing = "${section.items.size}") }
                items(section.items, key = { "${section.key}${it.favorite.contentType}${it.favorite.contentId}" }) { f ->
                    CsPoster(f.title, null, f.subtitle, { p.onItemClick(f) }, onLongClick = { p.onItemLongClick(f) })
                }
            }
        }
    LazyColumn(Modifier.width(220.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(6.dp)) {
        item { Text(tr("Decks", "المجموعات"), color = CS.Amber, fontSize = 13.sp, fontWeight = FontWeight.Black) }
        items(SavedLibraryPreset.entries) { pr ->
            val sel = pr == p.selectedPreset
            CsCard({ p.onPresetSelected(pr) }, Modifier.fillMaxWidth().height(50.dp), shape = CS.RSmall, container = if (sel) CS.Amber else CS.Raised, focusedContainer = if (sel) CS.Amber else CS.Card) {
                Text(presetLabel(pr), color = if (sel) CS.Bg else CS.Text, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterStart).padding(horizontal = 14.dp))
            }
        }
    }
    }
}
}

/** Settings: category cards as a stacked deck on the END side, the selected one tilts its suit card. */
@Composable
internal fun CardStackSettingsNav(p: SettingsNavParams) {
    LazyColumn(Modifier.width(300.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(6.dp), contentPadding = PaddingValues(vertical = 8.dp, horizontal = 6.dp)) {
        item { Text(tr("Settings", "الإعدادات"), color = CS.Text, fontSize = 28.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)) }
        items(p.entries.size) { i ->
            val (label, icon) = p.entries[i]
            val sel = i == p.selectedCategory
            CsCard(
                onClick = { p.onCategorySelected(i) }, zoom = 1.03f, container = if (sel) CS.Card else CS.Raised.copy(alpha = 0.5f), focusedContainer = CS.Card,
                modifier = Modifier.fillMaxWidth().then(if (sel) Modifier.focusRequester(p.focusRequester) else Modifier)
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(Modifier.size(width = 34.dp, height = 46.dp).graphicsLayer { rotationZ = if (sel) -8f else 0f }.clip(CS.RSmall).background(if (sel) CS.Amber else CS.Raised), contentAlignment = Alignment.Center) { Text(icon, fontSize = 16.sp, color = if (sel) CS.Bg else CS.Sub) }
                    Text(label, color = CS.Text, fontSize = 16.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium, maxLines = 1, modifier = Modifier.weight(1f))
                    if (sel) Text("›", color = CS.Amber, fontSize = 20.sp)
                }
            }
        }
    }
}

@Composable
internal fun CardStackSettingsFrame(navigation: @Composable () -> Unit, content: @Composable () -> Unit) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
        Box(Modifier.weight(1f).fillMaxHeight().clip(CS.R).background(CS.Raised).padding(24.dp)) { content() }
        navigation()
    }
}
