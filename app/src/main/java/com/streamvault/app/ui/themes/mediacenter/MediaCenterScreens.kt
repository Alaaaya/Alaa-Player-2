package com.streamvault.app.ui.themes.mediacenter

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.border
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
private fun McBackdrop(url: String?, content: @Composable BoxScope.() -> Unit) {
    McBackground {
        url?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(MC.Bg, MC.Bg.copy(alpha = 0.85f), MC.Bg.copy(alpha = 0.2f)))))
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, MC.Bg))))
        content()
    }
}

@Composable
private fun Meta(vararg parts: String?) {
    Text(parts.filterNot { it.isNullOrBlank() }.joinToString("  •  "), color = MC.Sub, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

/** Movie: receiver "info" page. Framed poster START, serif info sheet middle (ruled spec rows), vertical
 *  brass action menu at the END. Versions + "more like this" fanart strip along the bottom. */
@Composable
internal fun MediaCenterMovieDetail(p: MovieDetailParams) {
    val m = p.movie
    val play = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(m.id) { runCatching { play.requestFocus() } }
    val specs = listOf(
        tr("Year", "السنة") to m.year, tr("Runtime", "المدة") to m.duration,
        tr("Rating", "التقييم") to m.rating.takeIf { it > 0 }?.let { "★ %.1f".format(it) },
        tr("Director", "المخرج") to m.director, tr("Cast", "الممثلون") to m.cast,
        tr("Version", "النسخة") to m.variantLabel,
        tr("Resume", "استكمال") to if (p.hasResume) mcDuration(p.resumePositionMs) else null
    ).filter { !it.second.isNullOrBlank() }
    val acts = buildList<Triple<String, String, () -> Unit>> {
        add(Triple("▶", if (p.hasResume) tr("Resume", "استكمال") else tr("Play", "تشغيل"), p.onPlay))
        p.onPlayTrailer?.let { add(Triple("▷", tr("Trailer", "الإعلان"), it)) }
        add(Triple(if (m.isFavorite) "★" else "☆", if (m.isFavorite) tr("Remove favorite", "إزالة من المفضلة") else tr("Add favorite", "أضف للمفضلة"), p.onToggleFavorite))
        add(Triple("⎚", if (p.isCasting) tr("Casting…", "جار البث…") else tr("Cast", "بث"), p.onCast))
        add(Triple("↓", tr("Download", "تنزيل"), p.onDownload))
        add(Triple("⧉", tr("Copy link", "نسخ الرابط"), p.onCopyUrl))
        add(Triple("←", tr("Back", "رجوع"), p.onBack))
    }
    val tKind = tr("Movie", "فيلم"); val tOpt = tr("Options", "خيارات"); val tVer = tr("Versions", "النسخ"); val tMore = tr("More like this", "مشابه")
    McBackdrop(m.backdropUrl ?: m.posterUrl) {
        Column(Modifier.fillMaxSize().padding(start = 56.dp, end = 56.dp, top = 56.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                Box(Modifier.width(230.dp).aspectRatio(2f / 3f).border(1.dp, MC.Amber, MC.RSmall).padding(4.dp).background(MC.Raised)) {
                    m.posterUrl?.let { AsyncImage(it, m.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                }
                Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    McHeading(tKind + (m.genre?.takeIf { it.isNotBlank() }?.let { "  ·  $it" } ?: ""), size = 11)
                    Text(m.name, color = MC.Text, fontSize = 40.sp, fontFamily = MC.Serif, maxLines = 2, lineHeight = 44.sp)
                    Text(m.plot.orEmpty(), color = MC.Sub, fontSize = 14.sp, maxLines = 4, overflow = TextOverflow.Ellipsis, lineHeight = 21.sp)
                    Spacer(Modifier.height(4.dp))
                    specs.forEach { (k, v) ->
                        Row(Modifier.widthIn(max = 620.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(k.uppercase(), color = MC.Faint, fontSize = 11.sp, letterSpacing = 1.5.sp, modifier = Modifier.width(110.dp))
                            Text(v.orEmpty(), color = MC.Text, fontSize = 14.sp, fontFamily = MC.Serif, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Box(Modifier.widthIn(max = 620.dp).fillMaxWidth().height(1.dp).background(MC.Line.copy(alpha = 0.6f)))
                    }
                }
                Column(Modifier.width(220.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    McHeading(tOpt, size = 11, color = MC.Faint)
                    acts.forEachIndexed { i, (g, l, a) ->
                        McCard(onClick = a, container = if (i == 0) MC.AmberDeep.copy(alpha = 0.35f) else MC.Raised.copy(alpha = 0.8f), shape = MC.RSmall,
                            modifier = Modifier.fillMaxWidth().then(if (i == 0) Modifier.focusRequester(play) else Modifier)) {
                            Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(g, color = MC.Amber, fontSize = 15.sp, modifier = Modifier.width(18.dp))
                                Text(l, color = MC.Text, fontSize = 14.sp, fontFamily = MC.Serif, maxLines = 1)
                            }
                        }
                    }
                }
            }
            if (m.variants.size > 1) Row(verticalAlignment = Alignment.CenterVertically) {
                McHeading(tVer, size = 11, modifier = Modifier.padding(end = 10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(2.dp)) { items(m.variants) { v -> McTab(v.label, v.rawMovieId == m.selectedVariantId, { p.onSelectVariant(v.rawMovieId) }) } }
            }
            if (p.relatedContent.isNotEmpty()) Column {
                McRowTitle(tMore)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(p.relatedContent, key = { it.id }) { r -> McWide(r.name, r.backdropUrl ?: r.posterUrl, r.year, null, { p.onRelatedClick(r) }, Modifier.width(200.dp)) }
                }
            }
        }
    }
}

/** Series: receiver "TV show" page. Info band on top over fanart; below three columns:
 *  seasons (serif list) | episode ledger (number, title, runtime, progress, cast/download/copy keys) | actions. */
@Composable
internal fun MediaCenterSeriesDetail(p: SeriesDetailParams) {
    val s = p.series
    val primary = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(s.id) { runCatching { primary.requestFocus() } }
    val tHead = tr("TV show", "مسلسل") + "  ·  ${s.seasons.size} " + tr("seasons", "مواسم")
    val tSeasons = tr("Seasons", "المواسم"); val tSeason = tr("Season", "الموسم"); val tVer = tr("Versions", "النسخ"); val tNew = tr("new", "جديد")
    McBackdrop(s.backdropUrl ?: s.posterUrl) {
        Column(Modifier.fillMaxSize().padding(start = 56.dp, end = 56.dp, top = 44.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.widthIn(max = 760.dp)) {
                McHeading(tHead + (s.genre?.takeIf { it.isNotBlank() }?.let { "  ·  $it" } ?: ""), size = 11)
                Text(s.name, color = MC.Text, fontSize = 36.sp, fontFamily = MC.Serif, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (s.rating > 0) McBadge("★ %.1f".format(s.rating), MC.Amber)
                    if (p.unwatchedEpisodeCount > 0) McBadge("${p.unwatchedEpisodeCount} $tNew", MC.Blue)
                    s.releaseDate?.takeIf { it.isNotBlank() }?.let { Text(it, color = MC.Sub, fontSize = 12.sp) }
                    if (!p.isLoadingExternalRatings) {
                        val r = p.externalRatings
                        val ext = listOf("IMDb" to r.imdb, "RT" to r.rottenTomatoes, "MC" to r.metacritic).filter { it.second.available }
                        if (ext.isNotEmpty()) Text(ext.joinToString("   ") { "${it.first} ${it.second.displayValue}" }, color = MC.Faint, fontSize = 12.sp)
                    }
                }
                Text(s.plot.orEmpty(), color = MC.Sub, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                LazyColumn(Modifier.width(200.dp).fillMaxHeight().background(Color(0xCC120E09), MC.R).border(1.dp, MC.Line, MC.R).padding(8.dp)) {
                    item { McHeading(tSeasons, size = 11, modifier = Modifier.padding(8.dp)) }
                    items(s.seasons, key = { it.seasonNumber }) { season ->
                        val sel = season.seasonNumber == p.selectedSeason?.seasonNumber
                        McCard(onClick = { p.onSeasonSelected(season) }, shape = MC.RSmall, container = if (sel) Color(0xFF33281A) else Color.Transparent, modifier = Modifier.fillMaxWidth()) {
                            Text(season.name.ifBlank { "$tSeason ${season.seasonNumber}" }, color = if (sel) MC.Amber else MC.Text, fontSize = 15.sp, fontFamily = MC.Serif, maxLines = 1, modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp))
                        }
                    }
                    if (s.variants.size > 1) {
                        item { McHeading(tVer, size = 10, color = MC.Faint, modifier = Modifier.padding(start = 8.dp, top = 12.dp, bottom = 4.dp)) }
                        items(s.variants) { v -> McTab(v.label, v.rawSeriesId == s.selectedVariantId, { p.onSelectVariant(v.rawSeriesId) }) }
                    }
                }
                LazyColumn(Modifier.weight(1f).fillMaxHeight().background(Color(0xCC120E09), MC.R).border(1.dp, MC.Line, MC.R)) {
                    items(p.selectedSeason?.episodes.orEmpty(), key = { it.id }) { e ->
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(end = 8.dp)) {
                                McCard(onClick = { p.onEpisodeClick(e) }, container = Color.Transparent, shape = MC.RSmall, modifier = Modifier.weight(1f)) {
                                    Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                        Text("%02d".format(e.episodeNumber), color = MC.Amber, fontSize = 18.sp, fontFamily = MC.Serif, modifier = Modifier.width(32.dp))
                                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                            Text(e.title, color = MC.Text, fontSize = 15.sp, fontFamily = MC.Serif, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(e.plot.orEmpty(), color = MC.Faint, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            if (e.watchProgress > 0 && e.durationSeconds > 0) McProgress(e.watchProgress / (e.durationSeconds * 1000f), Modifier.widthIn(max = 260.dp), 3.dp)
                                        }
                                        e.duration?.let { Text(it, color = MC.Sub, fontSize = 12.sp) }
                                    }
                                }
                                McRound("⎚", { p.onCastEpisode(e) }, size = 34.dp)
                                McRound("↓", { p.onDownloadEpisode(e) }, size = 34.dp)
                                McRound("⧉", { p.onCopyEpisodeUrl(e) }, size = 34.dp)
                            }
                            Box(Modifier.fillMaxWidth().height(1.dp).background(MC.Line.copy(alpha = 0.5f)))
                        }
                    }
                }
                Column(Modifier.width(200.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val resume = p.resumeEpisode
                    McButton(
                        if (resume != null) tr("Continue", "متابعة") + " S${resume.seasonNumber}E${resume.episodeNumber}" else tr("Play", "تشغيل"),
                        { (resume ?: p.selectedSeason?.episodes?.firstOrNull())?.let(p.onResumeClick) }, Modifier.fillMaxWidth().focusRequester(primary), primary = true, icon = "▶"
                    )
                    McButton(if (s.isFavorite) tr("Favorited", "في المفضلة") else tr("Favorite", "مفضلة"), p.onToggleFavorite, Modifier.fillMaxWidth(), icon = if (s.isFavorite) "★" else "☆")
                    if (resume != null) McButton(if (p.isCasting) tr("Casting…", "جار البث…") else tr("Cast", "بث"), p.onCastResumeEpisode, Modifier.fillMaxWidth(), icon = "⎚")
                    McButton(tr("Back", "رجوع"), p.onBack, Modifier.fillMaxWidth(), icon = "←")
                }
            }
        }
    }
}

/** Search: receiver "find" page. START column = field, vertical scope menu, recent queries; END = results
 *  as a ruled channel ledger plus framed poster shelves for movies and series. */
@Composable
internal fun MediaCenterSearch(p: SearchParams) {
    LaunchedEffect(Unit) { runCatching { p.searchFocusRequester.requestFocus() } }
    val s = p.uiState
    val tLive = tr("Live channels", "قنوات مباشرة"); val tMov = tr("Movies", "أفلام"); val tSer = tr("Series", "مسلسلات")
    val tAll = tr("Everything", "الكل"); val tLv = tr("Live", "مباشر")
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(26.dp)) {
        Column(Modifier.width(280.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            McHeading(tr("Find", "بحث"), size = 13)
            McSearchField(p.query, p.onQueryChange, tr("Title or channel", "عنوان أو قناة"), Modifier.fillMaxWidth().focusRequester(p.searchFocusRequester), onSubmit = p.onSearch)
            Spacer(Modifier.height(6.dp))
            SearchTab.entries.forEach { t ->
                val label = when (t.name) { "ALL" -> tAll; "LIVE" -> tLv; "MOVIES" -> tMov; "SERIES" -> tSer; else -> t.name }
                McCard(onClick = { p.onTabSelected(t) }, shape = MC.RSmall, container = if (t == p.selectedTab) Color(0xFF33281A) else Color.Transparent, modifier = Modifier.fillMaxWidth()) {
                    Text(label, color = if (t == p.selectedTab) MC.Amber else MC.Text, fontSize = 16.sp, fontFamily = MC.Serif, modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp))
                }
            }
            McButton(tr("Build full index", "فهرسة كاملة"), p.onBuildCompleteIndex, Modifier.fillMaxWidth(), icon = "↻")
            if (p.recentQueries.isNotEmpty()) {
                McRowTitle(tr("Recent", "الأخيرة"), Modifier.padding(top = 10.dp))
                LazyColumn(Modifier.weight(1f)) {
                    items(p.recentQueries) { q -> McTab(q, false, { p.onRecentQuerySelected(q) }) }
                    item { McTab(tr("Clear", "مسح"), false, p.onClearRecentQueries) }
                }
            }
        }
        Box(Modifier.weight(1f).fillMaxHeight()) {
            when {
                p.query.isBlank() -> McEmpty(tr("Type to search", "اكتب للبحث"))
                s.isLoading -> McEmpty(tr("Searching…", "جار البحث…"))
                s.hasSearchError -> McEmpty(tr("Search failed. Try again.", "فشل البحث. حاول مرة أخرى."))
                s.isEmpty -> McEmpty(tr("No results for", "لا نتائج لـ") + " “${p.query}”")
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(bottom = 40.dp)) {
                    if (s.channels.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.LIVE)) {
                        item { McRowTitle(tLive, trailing = "${s.channels.size}") }
                        items(s.channels.take(if (p.selectedTab == SearchTab.LIVE) 300 else 6), key = { "c${it.id}" }) { c ->
                            val locked = p.isChannelLocked(c)
                            McCard(onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c) }, container = Color.Transparent, shape = MC.RSmall, modifier = Modifier.fillMaxWidth()) {
                                Row(Modifier.padding(horizontal = 14.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    McLogo(c.name, if (locked) null else c.logoUrl, 36.dp)
                                    Text(c.name, color = MC.Text, fontSize = 15.sp, fontFamily = MC.Serif, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                    Text(if (locked) "🔒" else c.currentProgram?.title.orEmpty(), color = MC.Faint, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                    if (c.id in p.recordingChannelIds) McBadge("REC", MC.Live, filled = true) else if (c.id in p.scheduledChannelIds) McBadge("◷", MC.Blue)
                                    c.qualityBadge()?.let { McBadge(it, MC.Sub) }
                                }
                            }
                        }
                    }
                    if (s.movies.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.MOVIES)) item {
                        Column {
                            McRowTitle(tMov, trailing = "${s.movies.size}")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(s.movies, key = { it.id }) { m -> val l = p.isMovieLocked(m); McPoster(m.name, if (l) null else m.posterUrl, m.year, { p.onMovieClick(m) }, Modifier.width(120.dp), locked = l, onLongClick = { p.onMovieLongClick(m) }) }
                            }
                        }
                    }
                    if (s.series.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.SERIES)) item {
                        Column {
                            McRowTitle(tSer, trailing = "${s.series.size}")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(s.series, key = { it.id }) { m -> val l = p.isSeriesLocked(m); McPoster(m.name, if (l) null else m.posterUrl, m.genre, { p.onSeriesClick(m) }, Modifier.width(120.dp), locked = l, onLongClick = { p.onSeriesLongClick(m) }) }
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

/** Favorites: receiver "bookmarks". START serif collection list + filter/sort text menus; END = framed grid
 *  with continue-watching fanart tiles and recent live plates on top. */
@Composable
internal fun MediaCenterFavorites(p: FavoritesParams) {
    val tCw = tr("Continue watching", "متابعة المشاهدة"); val tLive = tr("Recent live", "مباشر مؤخراً")
    val tEmpty = tr("Nothing saved yet. Long-press any title to add it.", "لا يوجد شيء محفوظ. اضغط مطولاً على أي عنصر لإضافته.")
    val tCol = tr("Collections", "المجموعات"); val tFil = tr("Filter", "تصفية"); val tSort = tr("Sort", "ترتيب")
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        LazyColumn(Modifier.width(240.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            item { McHeading(tCol, size = 12, modifier = Modifier.padding(bottom = 6.dp)) }
            items(SavedLibraryPreset.entries) { pr ->
                val sel = pr == p.selectedPreset
                McCard(onClick = { p.onPresetSelected(pr) }, shape = MC.RSmall, container = if (sel) Color(0xFF33281A) else Color.Transparent, modifier = Modifier.fillMaxWidth()) {
                    Text(presetLabel(pr), color = if (sel) MC.Amber else MC.Text, fontSize = 16.sp, fontFamily = MC.Serif, modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp))
                }
            }
            item { McHeading(tFil, size = 10, color = MC.Faint, modifier = Modifier.padding(top = 14.dp, bottom = 2.dp)) }
            items(SavedLibraryFilter.entries) { f -> McTab(f.name.lowercase().replaceFirstChar { it.uppercase() }, f == p.selectedFilter, { p.onFilterSelected(f) }) }
            item { McHeading(tSort, size = 10, color = MC.Faint, modifier = Modifier.padding(top = 14.dp, bottom = 2.dp)) }
            items(SavedLibrarySort.entries) { o -> McTab(o.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }, o == p.selectedSort, { p.onSortSelected(o) }) }
        }
        LazyVerticalGrid(GridCells.Adaptive(130.dp), Modifier.weight(1f).fillMaxHeight(), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(4.dp, 4.dp, 4.dp, 40.dp)) {
            if (p.continueWatching.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) { McRowTitle(tCw) }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(p.continueWatching, key = { "c${it.history.id}" }) { h ->
                            val prog = if (h.history.totalDurationMs > 0) h.history.resumePositionMs.toFloat() / h.history.totalDurationMs else null
                            McWide(h.title, h.history.posterUrl, h.subtitle, prog, { p.onHistoryClick(h) }, Modifier.width(230.dp))
                        }
                    }
                }
            }
            if (p.recentLive.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) { McRowTitle(tLive) }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(p.recentLive, key = { "l${it.history.id}" }) { h ->
                            McCard(onClick = { p.onHistoryClick(h) }, modifier = Modifier.width(210.dp), container = MC.Raised) {
                                Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    McLogo(h.title, h.history.posterUrl, 38.dp)
                                    Column { Text(h.title, color = MC.Text, fontSize = 13.sp, fontFamily = MC.Serif, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(h.subtitle, color = MC.Faint, fontSize = 11.sp, maxLines = 1) }
                                }
                            }
                        }
                    }
                }
            }
            val sections = p.sections.filter { it.items.isNotEmpty() }
            if (sections.isEmpty() && p.continueWatching.isEmpty() && p.recentLive.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) { McEmpty(tEmpty) }
            sections.forEach { section ->
                item(key = "h${section.key}", span = { GridItemSpan(maxLineSpan) }) { McRowTitle(section.title, trailing = "${section.items.size}") }
                items(section.items, key = { "${section.key}${it.favorite.contentType}${it.favorite.contentId}" }) { f ->
                    McPoster(f.title, null, f.subtitle, { p.onItemClick(f) }, onLongClick = { p.onItemLongClick(f) })
                }
            }
        }
    }
}

/** Settings: receiver setup menu. START column of roman-numbered serif entries; content in a framed panel
 *  with a gold header bar, separated by a brass gradient rule. */
@Composable
internal fun MediaCenterSettingsNav(p: SettingsNavParams) {
    val roman = listOf("I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII", "XIII", "XIV", "XV", "XVI")
    val tSetup = tr("Setup", "الإعداد")
    LazyColumn(Modifier.width(280.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(1.dp), contentPadding = PaddingValues(vertical = 6.dp)) {
        item { McHeading(tSetup, size = 13, modifier = Modifier.padding(start = 8.dp, bottom = 10.dp)) }
        items(p.entries.size) { i ->
            val (label, _) = p.entries[i]
            val sel = i == p.selectedCategory
            McCard(onClick = { p.onCategorySelected(i) }, shape = MC.RSmall, container = if (sel) Color(0xFF33281A) else Color.Transparent,
                modifier = Modifier.fillMaxWidth().then(if (sel) Modifier.focusRequester(p.focusRequester) else Modifier)) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(roman.getOrElse(i) { "${i + 1}" }, color = MC.Amber, fontSize = 13.sp, fontFamily = MC.Serif, modifier = Modifier.width(40.dp))
                    Text(label, color = if (sel) MC.Amber else MC.Text, fontSize = 16.sp, fontFamily = MC.Serif, maxLines = 1, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
internal fun MediaCenterSettingsFrame(navigation: @Composable () -> Unit, content: @Composable () -> Unit) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        navigation()
        Box(Modifier.width(1.dp).fillMaxHeight().background(Brush.verticalGradient(listOf(Color.Transparent, MC.Amber, Color.Transparent))))
        Column(Modifier.weight(1f).fillMaxHeight().border(1.dp, MC.Line, MC.R)) {
            Box(Modifier.fillMaxWidth().height(4.dp).background(MC.Amber))
            Box(Modifier.fillMaxSize().background(Color(0xFF120E09)).padding(22.dp)) { content() }
        }
    }
}
