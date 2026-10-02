package com.streamvault.app.ui.themes.nextgentv

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

/** Space backdrop: the artwork blurred-dark far behind, so floating panes read as hovering in front of it. */
@Composable
private fun NgBackdrop(url: String?, content: @Composable BoxScope.() -> Unit) {
    NgBackground {
        url?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, alpha = 0.35f, modifier = Modifier.fillMaxSize()) }
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(NG.Bg.copy(alpha = 0.55f), NG.Bg.copy(alpha = 0.92f)))))
        content()
    }
}

@Composable
private fun Meta(vararg parts: String?) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        parts.filterNot { it.isNullOrBlank() }.forEach { NgBadge(it!!, NG.Sub) }
    }
}

/** Floating ornament capsule that hangs under a window (visionOS-style action bar). */
@Composable
private fun NgOrnament(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier.clip(NG.Pill).background(NG.Raised.copy(alpha = 0.95f)).border(1.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.25f), Color.Transparent)), NG.Pill).padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically, content = content
    )
}

/** Movie: a centred floating window (poster + info) with an action ornament hanging below it, related shelf further down. */
@Composable
internal fun NextGenTvMovieDetail(p: MovieDetailParams) {
    val m = p.movie
    val play = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(m.id) { runCatching { play.requestFocus() } }
    NgBackdrop(m.backdropUrl ?: m.posterUrl) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 90.dp, vertical = 48.dp), verticalArrangement = Arrangement.spacedBy(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            item {
                NgPane(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(24.dp), horizontalArrangement = Arrangement.spacedBy(26.dp)) {
                        Box(Modifier.width(200.dp).aspectRatio(2f / 3f).clip(NG.R).background(NG.Line)) {
                            m.posterUrl?.let { AsyncImage(it, m.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(m.name, color = NG.Text, fontSize = 38.sp, fontWeight = FontWeight.Black, maxLines = 2, lineHeight = 42.sp)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (m.rating > 0) NgBadge("★ %.1f".format(m.rating), NG.Amber, filled = true)
                                Meta(m.year, m.duration, m.genre, m.variantLabel)
                            }
                            Text(m.plot.orEmpty(), color = NG.Sub, fontSize = 15.sp, maxLines = 5, overflow = TextOverflow.Ellipsis, lineHeight = 22.sp)
                            m.director?.takeIf { it.isNotBlank() }?.let { Text(tr("Director", "المخرج") + "  $it", color = NG.Faint, fontSize = 13.sp, maxLines = 1) }
                            m.cast?.takeIf { it.isNotBlank() }?.let { Text(tr("Starring", "بطولة") + "  $it", color = NG.Faint, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                            if (p.hasResume) Text(tr("Resume from", "استكمال من") + " " + ngDuration(p.resumePositionMs), color = NG.Amber, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            if (m.variants.size > 1) LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(m.variants) { v -> NgTab(v.label, v.rawMovieId == m.selectedVariantId, { p.onSelectVariant(v.rawMovieId) }) }
                            }
                        }
                    }
                }
            }
            item {
                NgOrnament {
                    NgButton(if (p.hasResume) tr("Resume", "استكمال") else tr("Play", "تشغيل"), p.onPlay, Modifier.focusRequester(play), primary = true, icon = "▶")
                    p.onPlayTrailer?.let { NgRound("▷", it, size = 46.dp) }
                    NgRound(if (m.isFavorite) "✓" else "+", p.onToggleFavorite, size = 46.dp, active = m.isFavorite)
                    NgRound("⎚", p.onCast, size = 46.dp, active = p.isCasting)
                    NgRound("↓", p.onDownload, size = 46.dp)
                    NgRound("⧉", p.onCopyUrl, size = 46.dp)
                    NgRound("←", p.onBack, size = 46.dp)
                }
            }
            if (p.relatedContent.isNotEmpty()) item {
                Column(Modifier.fillMaxWidth()) {
                    NgRowTitle(tr("More like this", "مشابه"))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(vertical = 10.dp)) {
                        items(p.relatedContent, key = { it.id }) { r -> NgPoster(r.name, r.posterUrl, r.year, { p.onRelatedClick(r) }, Modifier.width(130.dp)) }
                    }
                }
            }
        }
    }
}

/** Series: three panes in space. Seasons pane tilted in from the start, episode column centre stage,
 *  info pane (poster, ratings, plot, actions) tilted in from the end. */
@Composable
internal fun NextGenTvSeriesDetail(p: SeriesDetailParams) {
    val s = p.series
    val primary = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(s.id) { runCatching { primary.requestFocus() } }
    NgBackdrop(s.backdropUrl ?: s.posterUrl) {
        Row(Modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 36.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Column(
                Modifier.width(210.dp).fillMaxHeight().ngTilt(7f).clip(NG.R).background(NG.Raised).border(1.dp, Color.White.copy(alpha = 0.08f), NG.R).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(tr("Seasons", "المواسم"), color = NG.Faint, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 6.dp, bottom = 4.dp))
                if (s.variants.size > 1) s.variants.forEach { v -> NgTab(v.label, v.rawSeriesId == s.selectedVariantId, { p.onSelectVariant(v.rawSeriesId) }) }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(s.seasons, key = { it.seasonNumber }) { season ->
                        val sel = season.seasonNumber == p.selectedSeason?.seasonNumber
                        NgCard(onClick = { p.onSeasonSelected(season) }, shape = NG.RSmall, zoom = 1.05f, container = if (sel) NG.Amber.copy(alpha = 0.16f) else Color.Transparent, modifier = Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("${season.seasonNumber}", color = if (sel) NG.Amber else NG.Faint, fontSize = 16.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(24.dp))
                                Text(season.name.ifBlank { tr("Season", "الموسم") + " ${season.seasonNumber}" }, color = if (sel) NG.Amber else NG.Text, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
            LazyColumn(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(8.dp)) {
                item { NgRowTitle(p.selectedSeason?.name?.ifBlank { null } ?: tr("Episodes", "الحلقات"), trailing = "${p.selectedSeason?.episodes?.size ?: 0}") }
                items(p.selectedSeason?.episodes.orEmpty(), key = { it.id }) { e ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        NgCard(onClick = { p.onEpisodeClick(e) }, modifier = Modifier.weight(1f), shape = NG.RSmall, container = NG.Card.copy(alpha = 0.85f), zoom = 1.03f) {
                            Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                Box(Modifier.width(160.dp).aspectRatio(16f / 9f).clip(RoundedCornerShape(12.dp)).background(NG.Line)) {
                                    e.coverUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                                    Text("E${e.episodeNumber}", color = NG.Text, fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.TopStart).padding(6.dp).clip(NG.Pill).background(NG.Bg.copy(alpha = 0.7f)).padding(horizontal = 8.dp, vertical = 2.dp))
                                    if (e.watchProgress > 0 && e.durationSeconds > 0) NgProgress(e.watchProgress / (e.durationSeconds * 1000f), Modifier.align(Alignment.BottomCenter).padding(6.dp), 3.dp)
                                }
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(e.title, color = NG.Text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    e.duration?.let { Text(it, color = NG.Faint, fontSize = 11.sp) }
                                    Text(e.plot.orEmpty(), color = NG.Sub, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            NgRound("⎚", { p.onCastEpisode(e) }, size = 36.dp)
                            NgRound("↓", { p.onDownloadEpisode(e) }, size = 36.dp)
                            NgRound("⧉", { p.onCopyEpisodeUrl(e) }, size = 36.dp)
                        }
                    }
                }
            }
            Column(
                Modifier.width(320.dp).fillMaxHeight().ngTilt(-7f).clip(NG.R).background(NG.Raised).border(1.dp, Color.White.copy(alpha = 0.08f), NG.R).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.width(90.dp).aspectRatio(2f / 3f).clip(NG.RSmall).background(NG.Line)) {
                        s.posterUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(s.name, color = NG.Text, fontSize = 20.sp, fontWeight = FontWeight.Black, maxLines = 3, overflow = TextOverflow.Ellipsis)
                        if (s.rating > 0) NgBadge("★ %.1f".format(s.rating), NG.Amber, filled = true)
                        if (p.unwatchedEpisodeCount > 0) NgBadge("${p.unwatchedEpisodeCount} " + tr("new", "جديد"), NG.Blue)
                    }
                }
                Text(listOfNotNull(s.releaseDate, s.genre, "${s.seasons.size} " + tr("seasons", "مواسم")).filter { it.isNotBlank() }.joinToString(" · "), color = NG.Sub, fontSize = 12.sp, maxLines = 2)
                if (!p.isLoadingExternalRatings) {
                    val r = p.externalRatings
                    val ext = listOf("IMDb" to r.imdb, "RT" to r.rottenTomatoes, "MC" to r.metacritic).filter { it.second.available }
                    if (ext.isNotEmpty()) Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { ext.forEach { NgBadge("${it.first} ${it.second.displayValue}", NG.Violet) } }
                }
                Text(s.plot.orEmpty(), color = NG.Faint, fontSize = 12.sp, maxLines = 6, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                val resume = p.resumeEpisode
                NgButton(
                    if (resume != null) tr("Continue", "متابعة") + " S${resume.seasonNumber}:E${resume.episodeNumber}" else tr("Play", "تشغيل"),
                    { (resume ?: p.selectedSeason?.episodes?.firstOrNull())?.let(p.onResumeClick) }, Modifier.fillMaxWidth().focusRequester(primary), primary = true, icon = "▶"
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NgRound(if (s.isFavorite) "✓" else "+", p.onToggleFavorite, size = 44.dp, active = s.isFavorite)
                    if (resume != null) NgRound("⎚", p.onCastResumeEpisode, size = 44.dp, active = p.isCasting)
                    NgRound("←", p.onBack, size = 44.dp)
                }
            }
        }
    }
}

/** Search: a floating search capsule at the top, then results as side-by-side floating panes (Live | Movies | Series). */
@Composable
internal fun NextGenTvSearch(p: SearchParams) {
    LaunchedEffect(Unit) { runCatching { p.searchFocusRequester.requestFocus() } }
    val s = p.uiState
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            NgSearchField(p.query, p.onQueryChange, tr("Search channels, movies and series", "ابحث عن قنوات وأفلام ومسلسلات"), Modifier.fillMaxWidth(0.55f).focusRequester(p.searchFocusRequester), onSubmit = p.onSearch)
            NgOrnament {
                SearchTab.entries.forEach { t ->
                    val label = when (t.name) { "ALL" -> tr("All", "الكل"); "LIVE" -> tr("Live", "مباشر"); "MOVIES" -> tr("Movies", "أفلام"); "SERIES" -> tr("Series", "مسلسلات"); else -> t.name }
                    NgTab(label, t == p.selectedTab, { p.onTabSelected(t) })
                }
                NgTab("↻ " + tr("Full index", "فهرسة كاملة"), false, p.onBuildCompleteIndex)
            }
        }
        when {
            p.query.isBlank() -> if (p.recentQueries.isNotEmpty()) Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                NgRowTitle(tr("Recent searches", "عمليات البحث الأخيرة"))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(p.recentQueries) { q -> NgTab("↺ $q", false, { p.onRecentQuerySelected(q) }) }
                    item { NgTab(tr("Clear", "مسح"), false, p.onClearRecentQueries) }
                }
            }
            s.isLoading -> NgEmpty(tr("Searching…", "جار البحث…"))
            s.hasSearchError -> NgEmpty(tr("Search failed. Try again.", "فشل البحث. حاول مرة أخرى."))
            s.isEmpty -> NgEmpty(tr("No results for", "لا نتائج لـ") + " “${p.query}”")
            else -> Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                val showLive = s.channels.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.LIVE)
                val showMov = s.movies.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.MOVIES)
                val showSer = s.series.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.SERIES)
                if (showLive) ResultPane(tr("Live channels", "قنوات مباشرة"), s.channels.size, Modifier.weight(1f)) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), contentPadding = PaddingValues(6.dp)) {
                        items(s.channels, key = { it.id }) { c ->
                            val locked = p.isChannelLocked(c)
                            NgCard(onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c) }, shape = NG.RSmall, zoom = 1.04f, modifier = Modifier.fillMaxWidth()) {
                                Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    NgLogo(c.name, if (locked) null else c.logoUrl, 40.dp)
                                    Column(Modifier.weight(1f)) {
                                        Text(c.name, color = NG.Text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(if (locked) "🔒" else c.currentProgram?.title.orEmpty(), color = NG.Faint, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    if (c.id in p.recordingChannelIds) NgBadge("REC", NG.Live, filled = true) else if (c.id in p.scheduledChannelIds) NgBadge("◷", NG.Blue)
                                    c.qualityBadge()?.let { NgBadge(it, NG.Sub) }
                                }
                            }
                        }
                    }
                }
                if (showMov) ResultPane(tr("Movies", "أفلام"), s.movies.size, Modifier.weight(if (showLive || showSer) 1.2f else 1f)) {
                    LazyVerticalGrid(GridCells.Adaptive(110.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(8.dp)) {
                        items(s.movies, key = { it.id }) { m -> val l = p.isMovieLocked(m); NgPoster(m.name, if (l) null else m.posterUrl, m.year, { p.onMovieClick(m) }, locked = l, onLongClick = { p.onMovieLongClick(m) }) }
                    }
                }
                if (showSer) ResultPane(tr("Series", "مسلسلات"), s.series.size, Modifier.weight(if (showLive || showMov) 1.2f else 1f)) {
                    LazyVerticalGrid(GridCells.Adaptive(110.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(8.dp)) {
                        items(s.series, key = { it.id }) { m -> val l = p.isSeriesLocked(m); NgPoster(m.name, if (l) null else m.posterUrl, m.genre, { p.onSeriesClick(m) }, locked = l, onLongClick = { p.onSeriesLongClick(m) }) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultPane(title: String, count: Int, modifier: Modifier, content: @Composable () -> Unit) {
    Column(modifier.fillMaxHeight().clip(NG.R).background(NG.Raised.copy(alpha = 0.8f)).border(1.dp, Color.White.copy(alpha = 0.08f), NG.R).padding(12.dp)) {
        NgRowTitle(title, trailing = "$count")
        content()
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

/** My List: presets as a vertical floating ornament tilted in from the start; filter/sort capsule on top of a poster grid. */
@Composable
internal fun NextGenTvFavorites(p: FavoritesParams) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        Column(
            Modifier.width(190.dp).ngTilt(8f).clip(NG.R).background(NG.Raised).border(1.dp, Color.White.copy(alpha = 0.1f), NG.R).padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(tr("Collections", "المجموعات"), color = NG.Faint, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(6.dp))
            SavedLibraryPreset.entries.forEach { pr ->
                val sel = pr == p.selectedPreset
                NgCard(onClick = { p.onPresetSelected(pr) }, shape = NG.RSmall, zoom = 1.05f, container = if (sel) NG.Amber.copy(alpha = 0.16f) else Color.Transparent, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.size(7.dp).clip(CircleShape).background(if (sel) NG.Amber else Color.White.copy(alpha = 0.15f)))
                        Text(presetLabel(pr), color = if (sel) NG.Amber else NG.Text, fontSize = 13.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, maxLines = 1)
                    }
                }
            }
        }
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            NgOrnament {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    items(SavedLibraryFilter.entries) { f -> NgTab(f.name.lowercase().replaceFirstChar { it.uppercase() }, f == p.selectedFilter, { p.onFilterSelected(f) }) }
                    item { Text("  ⇅", color = NG.Faint, fontSize = 14.sp) }
                    items(SavedLibrarySort.entries) { o -> NgTab(o.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }, o == p.selectedSort, { p.onSortSelected(o) }) }
                }
            }
            LazyVerticalGrid(GridCells.Adaptive(140.dp), Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(18.dp), verticalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(8.dp, 8.dp, 8.dp, 48.dp)) {
                if (p.continueWatching.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) { NgRowTitle(tr("Continue watching", "متابعة المشاهدة")) }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(p.continueWatching, key = { "c${it.history.id}" }) { h ->
                                val prog = if (h.history.totalDurationMs > 0) h.history.resumePositionMs.toFloat() / h.history.totalDurationMs else null
                                NgWide(h.title, h.history.posterUrl, h.subtitle, prog, { p.onHistoryClick(h) }, Modifier.width(250.dp))
                            }
                        }
                    }
                }
                if (p.recentLive.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) { NgRowTitle(tr("Recently watched live", "مباشر شوهد مؤخراً")) }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            items(p.recentLive, key = { "l${it.history.id}" }) { h ->
                                Column(Modifier.width(110.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    NgCard(onClick = { p.onHistoryClick(h) }, shape = CircleShape, modifier = Modifier.size(84.dp)) { NgLogo(h.title, h.history.posterUrl, 48.dp, Modifier.align(Alignment.Center)) }
                                    Text(h.title, color = NG.Text, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
                val sections = p.sections.filter { it.items.isNotEmpty() }
                if (sections.isEmpty() && p.continueWatching.isEmpty() && p.recentLive.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
                    NgEmpty(tr("Your list is empty. Long-press any title to add it.", "قائمتك فارغة. اضغط مطولاً على أي عنصر لإضافته."))
                }
                sections.forEach { section ->
                    item(key = "h${section.key}", span = { GridItemSpan(maxLineSpan) }) { NgRowTitle(section.title, trailing = "${section.items.size}") }
                    items(section.items, key = { "${section.key}${it.favorite.contentType}${it.favorite.contentId}" }) { f ->
                        NgPoster(f.title, null, f.subtitle, { p.onItemClick(f) }, onLongClick = { p.onItemLongClick(f) })
                    }
                }
            }
        }
    }
}

/** Settings: the category list is a floating pane tilted in from the END; content is the main window. */
@Composable
internal fun NextGenTvSettingsNav(p: SettingsNavParams) {
    LazyColumn(
        Modifier.width(280.dp).fillMaxHeight().ngTilt(-8f).clip(NG.R).background(NG.Raised).border(1.dp, Color.White.copy(alpha = 0.1f), NG.R),
        verticalArrangement = Arrangement.spacedBy(4.dp), contentPadding = PaddingValues(12.dp)
    ) {
        item { Text(tr("Settings", "الإعدادات"), color = NG.Text, fontSize = 22.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 8.dp, bottom = 10.dp)) }
        items(p.entries.size) { i ->
            val (label, icon) = p.entries[i]
            val sel = i == p.selectedCategory
            NgCard(
                onClick = { p.onCategorySelected(i) }, zoom = 1.05f, shape = NG.RSmall, container = if (sel) NG.Amber.copy(alpha = 0.16f) else Color.Transparent,
                modifier = Modifier.fillMaxWidth().then(if (sel) Modifier.focusRequester(p.focusRequester) else Modifier)
            ) {
                Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(if (sel) NG.Amber else NG.Card), contentAlignment = Alignment.Center) { Text(icon, fontSize = 15.sp, color = if (sel) NG.Bg else NG.Sub) }
                    Text(label, color = if (sel) NG.Amber else NG.Text, fontSize = 15.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium, maxLines = 1, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
internal fun NextGenTvSettingsFrame(navigation: @Composable () -> Unit, content: @Composable () -> Unit) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(22.dp)) {
        Box(Modifier.weight(1f).fillMaxHeight().clip(NG.R).background(NG.Card.copy(alpha = 0.6f)).border(1.dp, Color.White.copy(alpha = 0.08f), NG.R).padding(24.dp)) { content() }
        navigation()
    }
}
