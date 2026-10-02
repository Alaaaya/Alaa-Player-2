package com.streamvault.app.ui.themes.futuristichud

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.itemsIndexed
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
private fun FhBackdrop(url: String?, content: @Composable BoxScope.() -> Unit) {
    FhBackground {
        url?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize(), alpha = 0.22f) }
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(FH.Bg.copy(alpha = 0.6f), FH.Bg))))
        content()
    }
}

/** One telemetry row: KEY ....... value */
@Composable
private fun Spec(k: String, v: String?) {
    if (v.isNullOrBlank()) return
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(k.uppercase(), color = FH.Faint, fontSize = 11.sp, fontFamily = FH.Mono, modifier = Modifier.width(96.dp))
        Text(v, color = FH.Text, fontSize = 13.sp, fontFamily = FH.Mono, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
    }
}

/** Movie: three HUD panels side by side: poster in reticle frame (START), dossier with spec rows + plot (middle),
 *  vertical command list (END). Versions as bracket tabs and related posters below. */
@Composable
internal fun FuturisticHudMovieDetail(p: MovieDetailParams) {
    val m = p.movie
    val play = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(m.id) { runCatching { play.requestFocus() } }
    val tYear = tr("Year", "السنة"); val tRun = tr("Runtime", "المدة"); val tGenre = tr("Genre", "النوع"); val tRate = tr("Rating", "التقييم")
    val tDir = tr("Director", "المخرج"); val tCast = tr("Cast", "بطولة"); val tVer = tr("Version", "النسخة"); val tRes = tr("Resume", "استكمال")
    FhBackdrop(m.backdropUrl ?: m.posterUrl) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 48.dp, end = 48.dp, top = 40.dp, bottom = 48.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            item {
                Row(Modifier.fillMaxWidth().height(400.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    Box(Modifier.width(260.dp).fillMaxHeight().background(FH.Card).border(1.dp, FH.Line).fhBrackets(FH.Amber, 20.dp, 3.dp).padding(8.dp)) {
                        m.posterUrl?.let { AsyncImage(it, m.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                        Text("ID#${m.id}", color = FH.Amber, fontSize = 9.sp, fontFamily = FH.Mono, modifier = Modifier.align(Alignment.BottomEnd).background(FH.Bg).padding(3.dp))
                    }
                    FhPanel(Modifier.weight(1f).fillMaxHeight(), title = tr("Dossier", "الملف")) {
                        Text(m.name.uppercase(), color = FH.Text, fontSize = 30.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Black, maxLines = 2, lineHeight = 34.sp)
                        Spec(tYear, m.year); Spec(tRun, m.duration); Spec(tGenre, m.genre)
                        Spec(tRate, if (m.rating > 0) "★ %.1f / 10".format(m.rating) else null)
                        Spec(tDir, m.director); Spec(tCast, m.cast); Spec(tVer, m.variantLabel)
                        if (p.hasResume) Spec(tRes, fhDuration(p.resumePositionMs))
                        Box(Modifier.fillMaxWidth().height(1.dp).background(FH.Line))
                        Text(m.plot.orEmpty(), color = FH.Sub, fontSize = 13.sp, maxLines = 5, overflow = TextOverflow.Ellipsis, lineHeight = 19.sp)
                    }
                    Column(Modifier.width(230.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        FhLabel(tr("Commands", "الأوامر"))
                        FhButton(if (p.hasResume) tr("Resume", "استكمال") else tr("Play", "تشغيل"), p.onPlay, Modifier.fillMaxWidth().focusRequester(play), primary = true, icon = "▶")
                        p.onPlayTrailer?.let { FhButton(tr("Trailer", "الإعلان"), it, Modifier.fillMaxWidth(), icon = "▷") }
                        FhButton(if (m.isFavorite) tr("Saved", "في قائمتي") else tr("Save", "قائمتي"), p.onToggleFavorite, Modifier.fillMaxWidth(), icon = if (m.isFavorite) "★" else "☆")
                        FhButton(if (p.isCasting) tr("Casting…", "جار البث…") else tr("Cast", "بث"), p.onCast, Modifier.fillMaxWidth(), icon = "⎚")
                        FhButton(tr("Download", "تنزيل"), p.onDownload, Modifier.fillMaxWidth(), icon = "↓")
                        FhButton(tr("Copy link", "نسخ الرابط"), p.onCopyUrl, Modifier.fillMaxWidth(), icon = "⧉")
                        FhButton(tr("Back", "رجوع"), p.onBack, Modifier.fillMaxWidth(), icon = "◂")
                    }
                }
            }
            if (m.variants.size > 1) item {
                Column {
                    FhRowTitle(tr("Versions", "النسخ"))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(m.variants) { v -> FhTab(v.label, v.rawMovieId == m.selectedVariantId, { p.onSelectVariant(v.rawMovieId) }) }
                    }
                }
            }
            if (p.relatedContent.isNotEmpty()) item {
                Column {
                    FhRowTitle(tr("Related targets", "مشابه"), trailing = "${p.relatedContent.size}")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(vertical = 6.dp)) {
                        items(p.relatedContent, key = { it.id }) { r -> FhPoster(r.name, r.posterUrl, r.year, { p.onRelatedClick(r) }, Modifier.width(130.dp)) }
                    }
                }
            }
        }
    }
}

/** Series: header strip (title, telemetry, commands) across the top; below a START season column (S01, S02…)
 *  beside an episode log where each row is code | title | duration | progress with inline cast/download/copy cells. */
@Composable
internal fun FuturisticHudSeriesDetail(p: SeriesDetailParams) {
    val s = p.series
    val primary = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(s.id) { runCatching { primary.requestFocus() } }
    FhBackdrop(s.backdropUrl ?: s.posterUrl) {
        Column(Modifier.fillMaxSize().padding(start = 48.dp, end = 48.dp, top = 36.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(110.dp).aspectRatio(2f / 3f).border(1.dp, FH.Line).fhBrackets(FH.Amber)) { s.posterUrl?.let { AsyncImage(it, s.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) } }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(s.name.uppercase(), color = FH.Text, fontSize = 28.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (s.rating > 0) FhBadge("★ %.1f".format(s.rating), FH.Warn)
                        if (p.unwatchedEpisodeCount > 0) FhBadge("${p.unwatchedEpisodeCount} " + tr("new", "جديد"), FH.Blue)
                        Text(listOfNotNull(s.releaseDate, s.genre, "${s.seasons.size} SEASONS").filter { it.isNotBlank() }.joinToString(" | ").uppercase(), color = FH.Sub, fontSize = 11.sp, fontFamily = FH.Mono, maxLines = 1)
                    }
                    if (!p.isLoadingExternalRatings) {
                        val r = p.externalRatings
                        val ext = listOf("IMDB" to r.imdb, "RT" to r.rottenTomatoes, "MC" to r.metacritic).filter { it.second.available }
                        if (ext.isNotEmpty()) Text(ext.joinToString("  ") { "${it.first}:${it.second.displayValue}" }, color = FH.Amber, fontSize = 11.sp, fontFamily = FH.Mono)
                    }
                    Text(s.plot.orEmpty(), color = FH.Sub, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Column(Modifier.width(240.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val resume = p.resumeEpisode
                    FhButton(
                        if (resume != null) tr("Continue", "متابعة") + " S${resume.seasonNumber}E${resume.episodeNumber}" else tr("Play", "تشغيل"),
                        { (resume ?: p.selectedSeason?.episodes?.firstOrNull())?.let(p.onResumeClick) }, Modifier.fillMaxWidth().focusRequester(primary), primary = true, icon = "▶"
                    )
                    FhButton(if (s.isFavorite) tr("Saved", "في قائمتي") else tr("Save", "قائمتي"), p.onToggleFavorite, Modifier.fillMaxWidth(), icon = if (s.isFavorite) "★" else "☆")
                    if (resume != null) FhButton(if (p.isCasting) tr("Casting…", "جار البث…") else tr("Cast", "بث"), p.onCastResumeEpisode, Modifier.fillMaxWidth(), icon = "⎚")
                    FhButton(tr("Back", "رجوع"), p.onBack, Modifier.fillMaxWidth(), icon = "◂")
                }
            }
            if (s.variants.size > 1) LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(s.variants) { v -> FhTab(v.label, v.rawSeriesId == s.selectedVariantId, { p.onSelectVariant(v.rawSeriesId) }) } }
            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(Modifier.width(170.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    FhLabel(tr("Seasons", "المواسم"))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        items(s.seasons, key = { it.seasonNumber }) { season ->
                            val sel = season.seasonNumber == p.selectedSeason?.seasonNumber
                            FhCard(onClick = { p.onSeasonSelected(season) }, shape = FH.RSmall, zoom = 1.0f, modifier = Modifier.fillMaxWidth(),
                                container = if (sel) FH.Amber.copy(alpha = 0.18f) else FH.Card.copy(alpha = 0.6f), focusedContainer = FH.Amber.copy(alpha = 0.32f)) {
                                Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("S%02d".format(season.seasonNumber), color = FH.Amber, fontSize = 13.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                                    Text("${season.episodes.size} EP", color = FH.Faint, fontSize = 10.sp, fontFamily = FH.Mono)
                                }
                            }
                        }
                    }
                }
                Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    FhLabel(tr("Episode log", "الحلقات") + " [${p.selectedSeason?.episodes?.size ?: 0}]")
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                        items(p.selectedSeason?.episodes.orEmpty(), key = { it.id }) { e ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FhCard(onClick = { p.onEpisodeClick(e) }, modifier = Modifier.weight(1f), shape = FH.RSmall, zoom = 1.0f, container = FH.Card.copy(alpha = 0.75f), focusedContainer = FH.Amber.copy(alpha = 0.28f)) {
                                    Row(Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text("E%02d".format(e.episodeNumber), color = FH.Amber, fontSize = 13.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Black)
                                        Box(Modifier.width(96.dp).aspectRatio(16f / 9f).background(FH.Raised)) { e.coverUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) } }
                                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(e.title.uppercase(), color = FH.Text, fontSize = 13.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(e.plot.orEmpty(), color = FH.Sub, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            if (e.watchProgress > 0 && e.durationSeconds > 0) FhProgress(e.watchProgress / (e.durationSeconds * 1000f), Modifier.widthIn(max = 240.dp), 3.dp)
                                        }
                                        e.duration?.let { Text(it, color = FH.Faint, fontSize = 11.sp, fontFamily = FH.Mono) }
                                    }
                                }
                                FhRound("⎚", { p.onCastEpisode(e) }, size = 36.dp)
                                FhRound("↓", { p.onDownloadEpisode(e) }, size = 36.dp)
                                FhRound("⧉", { p.onCopyEpisodeUrl(e) }, size = 36.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Search: START scanner console (query field, scope list, recent queries, full-index command), END results
 *  as stacked shelves; live channels shown as signal rows, VOD as posters. */
@Composable
internal fun FuturisticHudSearch(p: SearchParams) {
    LaunchedEffect(Unit) { runCatching { p.searchFocusRequester.requestFocus() } }
    val s = p.uiState
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        Column(Modifier.width(280.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FhLabel(tr("Scanner", "الماسح"))
            FhSearchField(p.query, p.onQueryChange, tr("Channels, movies, series", "قنوات، أفلام، مسلسلات"), Modifier.fillMaxWidth().focusRequester(p.searchFocusRequester), onSubmit = p.onSearch)
            FhLabel(tr("Scope", "النطاق"), color = FH.Faint)
            SearchTab.entries.forEach { t ->
                val label = when (t.name) { "ALL" -> tr("All", "الكل"); "LIVE" -> tr("Live", "مباشر"); "MOVIES" -> tr("Movies", "أفلام"); "SERIES" -> tr("Series", "مسلسلات"); else -> t.name }
                FhTab((if (t == p.selectedTab) "◉ " else "○ ") + label, t == p.selectedTab, { p.onTabSelected(t) }, Modifier.fillMaxWidth())
            }
            FhTab("↻ " + tr("Full index", "فهرسة كاملة"), false, p.onBuildCompleteIndex, Modifier.fillMaxWidth())
            if (p.recentQueries.isNotEmpty()) {
                FhLabel(tr("History", "الأخيرة"), color = FH.Faint)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    items(p.recentQueries) { q -> FhTab("› $q", false, { p.onRecentQuerySelected(q) }, Modifier.fillMaxWidth()) }
                    item { FhTab("✕ " + tr("Clear", "مسح"), false, p.onClearRecentQueries, Modifier.fillMaxWidth()) }
                }
            }
        }
        LazyColumn(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(bottom = 48.dp)) {
            when {
                p.query.isBlank() -> item { FhEmpty(tr("Awaiting query", "اكتب للبحث")) }
                s.isLoading -> item { FhEmpty(tr("Scanning…", "جار البحث…")) }
                s.hasSearchError -> item { FhEmpty(tr("Search failed. Try again.", "فشل البحث. حاول مرة أخرى.")) }
                s.isEmpty -> item { FhEmpty(tr("No results for", "لا نتائج لـ") + " “${p.query}”") }
                else -> {
                    if (s.channels.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.LIVE)) {
                        item { FhRowTitle(tr("Live signals", "قنوات مباشرة"), trailing = "${s.channels.size}") }
                        items(s.channels.take(40), key = { "c${it.id}" }) { c ->
                            val locked = p.isChannelLocked(c)
                            FhCard(onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c) }, modifier = Modifier.fillMaxWidth(), shape = FH.RSmall, zoom = 1.0f, container = FH.Card.copy(alpha = 0.75f), focusedContainer = FH.Amber.copy(alpha = 0.28f)) {
                                Row(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    FhLogo(c.name, if (locked) null else c.logoUrl, 36.dp)
                                    Text(c.name.uppercase(), color = FH.Text, fontSize = 13.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(260.dp))
                                    Text(if (locked) "⊘" else c.currentProgram?.title.orEmpty(), color = FH.Faint, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                    if (c.id in p.recordingChannelIds) FhBadge("REC", FH.Live) else if (c.id in p.scheduledChannelIds) FhBadge("SCHED", FH.Blue)
                                    c.qualityBadge()?.let { FhBadge(it, FH.Text) }
                                }
                            }
                        }
                    }
                    if (s.movies.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.MOVIES)) item {
                        Column {
                            FhRowTitle(tr("Movies", "أفلام"), trailing = "${s.movies.size}")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(vertical = 6.dp)) {
                                items(s.movies, key = { it.id }) { m -> val l = p.isMovieLocked(m); FhPoster(m.name, if (l) null else m.posterUrl, m.year, { p.onMovieClick(m) }, Modifier.width(128.dp), locked = l, onLongClick = { p.onMovieLongClick(m) }) }
                            }
                        }
                    }
                    if (s.series.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.SERIES)) item {
                        Column {
                            FhRowTitle(tr("Series", "مسلسلات"), trailing = "${s.series.size}")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(vertical = 6.dp)) {
                                items(s.series, key = { it.id }) { m -> val l = p.isSeriesLocked(m); FhPoster(m.name, if (l) null else m.posterUrl, m.genre, { p.onSeriesClick(m) }, Modifier.width(128.dp), locked = l, onLongClick = { p.onSeriesLongClick(m) }) }
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

/** Saved: START control column (presets, filters, sort as radio lists), main grid with resume log + sections. */
@Composable
internal fun FuturisticHudFavorites(p: FavoritesParams) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        LazyColumn(Modifier.width(220.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            item { FhLabel(tr("Preset", "المجموعة")) }
            items(SavedLibraryPreset.entries) { pr -> FhTab((if (pr == p.selectedPreset) "◉ " else "○ ") + presetLabel(pr), pr == p.selectedPreset, { p.onPresetSelected(pr) }, Modifier.fillMaxWidth()) }
            item { Spacer(Modifier.height(8.dp)); FhLabel(tr("Filter", "تصفية"), color = FH.Faint) }
            items(SavedLibraryFilter.entries) { f -> FhTab((if (f == p.selectedFilter) "◉ " else "○ ") + f.name.replace('_', ' '), f == p.selectedFilter, { p.onFilterSelected(f) }, Modifier.fillMaxWidth()) }
            item { Spacer(Modifier.height(8.dp)); FhLabel(tr("Sort", "ترتيب"), color = FH.Faint) }
            items(SavedLibrarySort.entries) { o -> FhTab((if (o == p.selectedSort) "◉ " else "○ ") + o.name.replace('_', ' '), o == p.selectedSort, { p.onSortSelected(o) }, Modifier.fillMaxWidth()) }
        }
        LazyVerticalGrid(GridCells.Adaptive(132.dp), Modifier.weight(1f).fillMaxHeight(), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(4.dp, 4.dp, 4.dp, 48.dp)) {
            if (p.continueWatching.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) { FhRowTitle(tr("Resume log", "متابعة المشاهدة"), trailing = "${p.continueWatching.size}") }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(p.continueWatching, key = { "c${it.history.id}" }) { h ->
                            val prog = if (h.history.totalDurationMs > 0) h.history.resumePositionMs.toFloat() / h.history.totalDurationMs else null
                            FhWide(h.title, h.history.posterUrl, h.subtitle, prog, { p.onHistoryClick(h) }, Modifier.width(240.dp))
                        }
                    }
                }
            }
            if (p.recentLive.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) { FhRowTitle(tr("Recent signals", "مباشر شوهد مؤخراً")) }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(p.recentLive, key = { "l${it.history.id}" }) { h ->
                            FhCard(onClick = { p.onHistoryClick(h) }, modifier = Modifier.width(220.dp), container = FH.Card, shape = FH.RSmall) {
                                Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    FhLogo(h.title, h.history.posterUrl, 36.dp)
                                    Column { Text(h.title.uppercase(), color = FH.Text, fontSize = 12.sp, fontFamily = FH.Mono, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(h.subtitle, color = FH.Faint, fontSize = 10.sp, maxLines = 1) }
                                }
                            }
                        }
                    }
                }
            }
            val sections = p.sections.filter { it.items.isNotEmpty() }
            if (sections.isEmpty() && p.continueWatching.isEmpty() && p.recentLive.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
                FhEmpty(tr("Your list is empty. Long-press any title to add it.", "قائمتك فارغة. اضغط مطولاً على أي عنصر لإضافته."))
            }
            sections.forEach { section ->
                item(key = "h${section.key}", span = { GridItemSpan(maxLineSpan) }) { FhRowTitle(section.title, trailing = "${section.items.size}") }
                items(section.items, key = { "${section.key}${it.favorite.contentType}${it.favorite.contentId}" }) { f ->
                    FhPoster(f.title, null, f.subtitle, { p.onItemClick(f) }, onLongClick = { p.onItemLongClick(f) })
                }
            }
        }
    }
}

/** Config: numbered mono list "0x01 …" on a ticked scale line, active entry gets a pointer. */
@Composable
internal fun FuturisticHudSettingsNav(p: SettingsNavParams) {
    LazyColumn(Modifier.width(280.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(3.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
        item { Text("// CONFIG", color = FH.Amber, fontSize = 20.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 6.dp, bottom = 12.dp)) }
        items(p.entries.size) { i ->
            val (label, icon) = p.entries[i]
            val sel = i == p.selectedCategory
            FhCard(
                onClick = { p.onCategorySelected(i) }, zoom = 1.0f, shape = FH.RSmall,
                container = if (sel) FH.Amber.copy(alpha = 0.16f) else Color.Transparent, focusedContainer = FH.Amber.copy(alpha = 0.32f),
                modifier = Modifier.fillMaxWidth().then(if (sel) Modifier.focusRequester(p.focusRequester) else Modifier)
            ) {
                Row(Modifier.padding(horizontal = 8.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (sel) "▶" else " ", color = FH.Amber, fontSize = 10.sp, fontFamily = FH.Mono)
                    Text("0x%02X".format(i + 1), color = FH.Faint, fontSize = 10.sp, fontFamily = FH.Mono)
                    Text(icon, fontSize = 13.sp, color = if (sel) FH.Amber else FH.Sub)
                    Text(label.uppercase(), color = if (sel) FH.Text else FH.Sub, fontSize = 12.sp, fontFamily = FH.Mono, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
internal fun FuturisticHudSettingsFrame(navigation: @Composable () -> Unit, content: @Composable () -> Unit) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        navigation()
        Box(Modifier.width(1.dp).fillMaxHeight().background(FH.Amber.copy(alpha = 0.4f)))
        Box(Modifier.weight(1f).fillMaxHeight().clip(FH.R).background(FH.Card.copy(alpha = 0.9f)).border(1.dp, FH.Line, FH.R).fhBrackets(FH.Amber, 18.dp).padding(22.dp)) { content() }
    }
}
