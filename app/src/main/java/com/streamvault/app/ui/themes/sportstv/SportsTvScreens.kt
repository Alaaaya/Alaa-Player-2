package com.streamvault.app.ui.themes.sportstv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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

/** Backdrop dimmed hard into navy with diagonal lime rule, broadcast replay look. */
@Composable
private fun StBackdrop(url: String?, content: @Composable BoxScope.() -> Unit) {
    StBackground {
        url?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(ST.Bg.copy(alpha = 0.95f), ST.Bg.copy(alpha = 0.85f), ST.Bg.copy(alpha = 0.55f)))))
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, ST.Bg))))
        content()
    }
}

@Composable
private fun Meta(vararg parts: String?) {
    Text(parts.filterNot { it.isNullOrBlank() }.joinToString("  •  "), color = ST.Sub, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

/** Scoreboard stat cell for detail screens. */
@Composable
private fun StStatCell(value: String, label: String) {
    Column(Modifier.clip(ST.RSmall).background(ST.Bg.copy(alpha = 0.85f)).padding(horizontal = 14.dp, vertical = 8.dp)) {
        Text(value, color = ST.Amber, fontSize = 18.sp, fontWeight = FontWeight.Black, maxLines = 1)
        Text(label.uppercase(), color = ST.Sub, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
    }
}

/** Vertical broadcast-menu action row: lime glyph block + uppercase label. */
@Composable
private fun StMenuAction(glyph: String, label: String, onClick: () -> Unit, modifier: Modifier = Modifier, primary: Boolean = false) {
    StCard(onClick = onClick, shape = ST.RSmall, zoom = 1.02f, container = if (primary) ST.Amber.copy(alpha = 0.25f) else ST.Raised.copy(alpha = 0.9f), focusedContainer = ST.Line, modifier = modifier.fillMaxWidth()) {
        Row(Modifier.height(44.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(44.dp).fillMaxHeight().background(if (primary) ST.Amber else ST.Bg), contentAlignment = Alignment.Center) {
                Text(glyph, color = if (primary) ST.Bg else ST.Amber, fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
            Text(label.uppercase(), color = ST.Text, fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 0.8.sp, maxLines = 1, modifier = Modifier.padding(horizontal = 12.dp))
        }
    }
}

/** Movie "match card": vertical action menu column on the START, title plate + stat scoreboard + plot in the middle,
 *  framed poster on the END; versions as tabs and related titles shelf underneath. */
@Composable
internal fun SportsTvMovieDetail(p: MovieDetailParams) {
    val m = p.movie
    val play = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(m.id) { runCatching { play.requestFocus() } }
    StBackdrop(m.backdropUrl ?: m.posterUrl) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 48.dp, end = 48.dp, top = 48.dp, bottom = 48.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Column(Modifier.width(240.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        StMenuAction("▶", if (p.hasResume) tr("Resume", "استكمال") else tr("Play", "تشغيل"), p.onPlay, Modifier.focusRequester(play), primary = true)
                        p.onPlayTrailer?.let { StMenuAction("▷", tr("Trailer", "الإعلان"), it) }
                        StMenuAction(if (m.isFavorite) "★" else "☆", if (m.isFavorite) tr("In My List", "في قائمتي") else tr("My List", "قائمتي"), p.onToggleFavorite)
                        StMenuAction("⎚", if (p.isCasting) tr("Casting…", "جار البث…") else tr("Cast", "بث"), p.onCast)
                        StMenuAction("↓", tr("Download", "تنزيل"), p.onDownload)
                        StMenuAction("⧉", tr("Copy link", "نسخ الرابط"), p.onCopyUrl)
                        StMenuAction("←", tr("Back", "رجوع"), p.onBack)
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.clip(ST.Plate).background(ST.Amber).padding(horizontal = 18.dp, vertical = 8.dp)) {
                            Text(m.name.uppercase(), color = ST.Bg, fontSize = 30.sp, fontWeight = FontWeight.Black, maxLines = 2, lineHeight = 32.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (m.rating > 0) StStatCell("%.1f".format(m.rating), tr("rating", "تقييم"))
                            m.year?.takeIf { it.isNotBlank() }?.let { StStatCell(it, tr("year", "سنة")) }
                            m.duration?.takeIf { it.isNotBlank() }?.let { StStatCell(it, tr("runtime", "المدة")) }
                            m.genre?.takeIf { it.isNotBlank() }?.let { StStatCell(it.take(18), tr("genre", "النوع")) }
                        }
                        Text(m.plot.orEmpty(), color = ST.Text, fontSize = 15.sp, maxLines = 5, overflow = TextOverflow.Ellipsis, lineHeight = 22.sp)
                        m.director?.takeIf { it.isNotBlank() }?.let { Text(tr("DIRECTOR", "المخرج") + "  $it", color = ST.Sub, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                        m.cast?.takeIf { it.isNotBlank() }?.let { Text(tr("CAST", "بطولة") + "  $it", color = ST.Sub, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis) }
                        if (p.hasResume) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.background(ST.Live).padding(horizontal = 6.dp, vertical = 2.dp)) { Text(tr("RESUME", "استكمال"), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black) }
                            Text(stDuration(p.resumePositionMs), color = ST.Amber, fontSize = 14.sp, fontWeight = FontWeight.Black)
                        }
                        if (m.variants.size > 1) Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            m.variants.forEach { v -> StTab(v.label, v.rawMovieId == m.selectedVariantId, { p.onSelectVariant(v.rawMovieId) }) }
                        }
                    }
                    Box(Modifier.width(220.dp).aspectRatio(2f / 3f).clip(ST.R).background(ST.Raised)) {
                        m.posterUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                        Box(Modifier.align(Alignment.TopEnd).width(60.dp).height(8.dp).background(ST.Amber))
                    }
                }
            }
            if (p.relatedContent.isNotEmpty()) item {
                Column {
                    StRowTitle(tr("More like this", "مشابه"), trailing = "${p.relatedContent.size}")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 6.dp)) {
                        items(p.relatedContent, key = { it.id }) { r -> StPoster(r.name, r.posterUrl, r.year, { p.onRelatedClick(r) }, Modifier.width(130.dp)) }
                    }
                }
            }
        }
    }
}

/** Series: header plate (poster + lime title plate + stat scoreboard + actions), then a "matchday" strip of square
 *  numbered season blocks and episodes as fixture rows (number block, still, title, clock bar, inline actions). */
@Composable
internal fun SportsTvSeriesDetail(p: SeriesDetailParams) {
    val s = p.series
    val primary = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(s.id) { runCatching { primary.requestFocus() } }
    StBackdrop(s.backdropUrl ?: s.posterUrl) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 48.dp, end = 48.dp, top = 40.dp, bottom = 48.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.Top) {
                    Box(Modifier.width(150.dp).aspectRatio(2f / 3f).clip(ST.R).background(ST.Raised)) {
                        s.posterUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.clip(ST.Plate).background(ST.Amber).padding(horizontal = 16.dp, vertical = 6.dp)) {
                            Text(s.name.uppercase(), color = ST.Bg, fontSize = 26.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (s.rating > 0) StStatCell("%.1f".format(s.rating), tr("rating", "تقييم"))
                            StStatCell("${s.seasons.size}", tr("seasons", "مواسم"))
                            if (p.unwatchedEpisodeCount > 0) StStatCell("${p.unwatchedEpisodeCount}", tr("new", "جديد"))
                            s.releaseDate?.takeIf { it.isNotBlank() }?.let { StStatCell(it.take(10), tr("released", "الإصدار")) }
                            if (!p.isLoadingExternalRatings) {
                                val r = p.externalRatings
                                listOf("IMDb" to r.imdb, "RT" to r.rottenTomatoes, "Meta" to r.metacritic).filter { it.second.available }.forEach { StStatCell(it.second.displayValue, it.first) }
                            }
                        }
                        Text(s.plot.orEmpty(), color = ST.Sub, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        val resume = p.resumeEpisode
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StButton(
                                if (resume != null) tr("Continue", "متابعة") + " S${resume.seasonNumber}:E${resume.episodeNumber}" else tr("Play", "تشغيل"),
                                { (resume ?: p.selectedSeason?.episodes?.firstOrNull())?.let(p.onResumeClick) }, Modifier.focusRequester(primary), primary = true, icon = "▶"
                            )
                            StButton(if (s.isFavorite) tr("In My List", "في قائمتي") else tr("My List", "قائمتي"), p.onToggleFavorite, icon = if (s.isFavorite) "★" else "☆")
                            if (resume != null) StButton(if (p.isCasting) tr("Casting…", "جار البث…") else tr("Cast", "بث"), p.onCastResumeEpisode, icon = "⎚")
                            StButton(tr("Back", "رجوع"), p.onBack, icon = "←")
                        }
                        if (s.variants.size > 1) Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) { s.variants.forEach { v -> StTab(v.label, v.rawSeriesId == s.selectedVariantId, { p.onSelectVariant(v.rawSeriesId) }) } }
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth().padding(top = 6.dp).background(ST.Raised), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.background(ST.Live).padding(horizontal = 12.dp, vertical = 14.dp)) { Text(tr("SEASONS", "المواسم"), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black) }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp), contentPadding = PaddingValues(6.dp)) {
                        items(s.seasons, key = { it.seasonNumber }) { season ->
                            val sel = season.seasonNumber == p.selectedSeason?.seasonNumber
                            StCard(onClick = { p.onSeasonSelected(season) }, shape = ST.Pill, zoom = 1.06f, container = if (sel) ST.Amber else ST.Bg, focusedContainer = ST.Line, modifier = Modifier.size(width = 64.dp, height = 46.dp)) {
                                Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("S${season.seasonNumber}", color = if (sel) ST.Bg else ST.Text, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                    Text("${season.episodes.size} EP", color = if (sel) ST.Bg.copy(alpha = 0.7f) else ST.Faint, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            items(p.selectedSeason?.episodes.orEmpty(), key = { it.id }) { e ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    StCard(onClick = { p.onEpisodeClick(e) }, modifier = Modifier.weight(1f), shape = ST.RSmall, container = ST.Card.copy(alpha = 0.92f), focusedContainer = ST.Line, zoom = 1.01f) {
                        Row(Modifier.height(76.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.width(54.dp).fillMaxHeight().background(ST.Amber), contentAlignment = Alignment.Center) {
                                Text("${e.episodeNumber}", color = ST.Bg, fontSize = 20.sp, fontWeight = FontWeight.Black)
                            }
                            Box(Modifier.fillMaxHeight().aspectRatio(16f / 9f).background(ST.Raised)) {
                                e.coverUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                            }
                            Column(Modifier.weight(1f).padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(e.title.uppercase(), color = ST.Text, fontSize = 14.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(e.plot.orEmpty(), color = ST.Sub, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (e.watchProgress > 0 && e.durationSeconds > 0) StProgress(e.watchProgress / (e.durationSeconds * 1000f), Modifier.widthIn(max = 300.dp), 3.dp)
                            }
                            e.duration?.let { Text(it, color = ST.Amber, fontSize = 13.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(end = 14.dp)) }
                        }
                    }
                    StRound("⎚", { p.onCastEpisode(e) }, size = 44.dp)
                    StRound("↓", { p.onDownloadEpisode(e) }, size = 44.dp)
                    StRound("⧉", { p.onCopyEpisodeUrl(e) }, size = 44.dp)
                }
            }
        }
    }
}

/** Results column header: lime slash + title + count. */
@Composable
private fun StColHead(title: String, count: Int) {
    Row(Modifier.fillMaxWidth().background(ST.Line).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title.uppercase(), color = ST.Text, fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, modifier = Modifier.weight(1f))
        Text("$count", color = ST.Amber, fontSize = 13.sp, fontWeight = FontWeight.Black)
    }
}

/** Search: scoreboard bar on top (field + type tabs + full index), then results split into side-by-side
 *  CHANNELS / MOVIES / SERIES result columns (rows, not shelves). Recent searches as a ticker list. */
@Composable
internal fun SportsTvSearch(p: SearchParams) {
    LaunchedEffect(Unit) { runCatching { p.searchFocusRequester.requestFocus() } }
    val s = p.uiState
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth().clip(ST.R).background(ST.Card).padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.clip(ST.Plate).background(ST.Amber).padding(horizontal = 14.dp, vertical = 8.dp)) { Text("⌕ " + tr("SEARCH", "بحث"), color = ST.Bg, fontSize = 15.sp, fontWeight = FontWeight.Black) }
            StSearchField(p.query, p.onQueryChange, tr("Channels, movies, series", "قنوات، أفلام، مسلسلات"), Modifier.weight(1f).focusRequester(p.searchFocusRequester), onSubmit = p.onSearch)
            SearchTab.entries.forEach { t ->
                val label = when (t.name) { "ALL" -> tr("All", "الكل"); "LIVE" -> tr("Live", "مباشر"); "MOVIES" -> tr("Movies", "أفلام"); "SERIES" -> tr("Series", "مسلسلات"); else -> t.name }
                StTab(label, t == p.selectedTab, { p.onTabSelected(t) })
            }
            StButton(tr("Full index", "فهرسة كاملة"), p.onBuildCompleteIndex, icon = "↻")
        }
        when {
            p.query.isBlank() -> Column(Modifier.width(420.dp).clip(ST.R).background(ST.Card)) {
                StColHead(tr("Recent searches", "عمليات البحث الأخيرة"), p.recentQueries.size)
                if (p.recentQueries.isEmpty()) StEmpty(tr("Type to search", "اكتب للبحث"))
                LazyColumn(contentPadding = PaddingValues(6.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    itemsIndexed(p.recentQueries) { i, q ->
                        StCard(onClick = { p.onRecentQuerySelected(q) }, shape = ST.Pill, zoom = 1.02f, container = ST.Raised, focusedContainer = ST.Line, modifier = Modifier.fillMaxWidth()) {
                            Row(Modifier.height(38.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.width(32.dp).fillMaxHeight().background(ST.Bg), contentAlignment = Alignment.Center) { Text("${i + 1}", color = ST.Amber, fontSize = 12.sp, fontWeight = FontWeight.Black) }
                                Text(q, color = ST.Text, fontSize = 13.sp, maxLines = 1, modifier = Modifier.padding(horizontal = 10.dp))
                            }
                        }
                    }
                    if (p.recentQueries.isNotEmpty()) item { StButton(tr("Clear", "مسح"), p.onClearRecentQueries, Modifier.padding(top = 6.dp), icon = "✕") }
                }
            }
            s.isLoading -> StEmpty(tr("Searching…", "جار البحث…"))
            s.hasSearchError -> StEmpty(tr("Search failed. Try again.", "فشل البحث. حاول مرة أخرى."))
            s.isEmpty -> StEmpty(tr("No results for", "لا نتائج لـ") + " “${p.query}”")
            else -> Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val showLive = s.channels.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.LIVE)
                val showMovies = s.movies.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.MOVIES)
                val showSeries = s.series.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.SERIES)
                if (showLive) Column(Modifier.weight(1f).fillMaxHeight().clip(ST.R).background(ST.Card)) {
                    StColHead(tr("Channels", "قنوات"), s.channels.size)
                    LazyColumn(contentPadding = PaddingValues(6.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        items(s.channels, key = { it.id }) { c ->
                            val locked = p.isChannelLocked(c)
                            StCard(onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c) }, shape = ST.RSmall, zoom = 1.02f, container = ST.Raised, focusedContainer = ST.Line, modifier = Modifier.fillMaxWidth()) {
                                Row(Modifier.height(54.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.width(40.dp).fillMaxHeight().background(ST.Bg), contentAlignment = Alignment.Center) { Text(if (c.number > 0) "${c.number}" else "–", color = ST.Amber, fontSize = 12.sp, fontWeight = FontWeight.Black) }
                                    StLogo(c.name, if (locked) null else c.logoUrl, 36.dp, Modifier.padding(horizontal = 8.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(c.name.uppercase(), color = ST.Text, fontSize = 12.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(if (locked) "🔒" else c.currentProgram?.title.orEmpty(), color = ST.Sub, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    Row(Modifier.padding(end = 8.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                        if (c.id in p.recordingChannelIds) StBadge("REC", ST.Live, filled = true)
                                        else if (c.id in p.scheduledChannelIds) StBadge("◷", ST.Blue)
                                        c.qualityBadge()?.let { StBadge(it, ST.Text) }
                                    }
                                }
                            }
                        }
                    }
                }
                if (showMovies) Column(Modifier.weight(1f).fillMaxHeight().clip(ST.R).background(ST.Card)) {
                    StColHead(tr("Movies", "أفلام"), s.movies.size)
                    LazyVerticalGrid(GridCells.Adaptive(110.dp), contentPadding = PaddingValues(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(s.movies, key = { it.id }) { m -> val l = p.isMovieLocked(m); StPoster(m.name, if (l) null else m.posterUrl, m.year, { p.onMovieClick(m) }, locked = l, onLongClick = { p.onMovieLongClick(m) }) }
                    }
                }
                if (showSeries) Column(Modifier.weight(1f).fillMaxHeight().clip(ST.R).background(ST.Card)) {
                    StColHead(tr("Series", "مسلسلات"), s.series.size)
                    LazyVerticalGrid(GridCells.Adaptive(110.dp), contentPadding = PaddingValues(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(s.series, key = { it.id }) { m -> val l = p.isSeriesLocked(m); StPoster(m.name, if (l) null else m.posterUrl, m.genre, { p.onSeriesClick(m) }, locked = l, onLongClick = { p.onSeriesLongClick(m) }) }
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

/** My List: red ticker bar of presets across the top, main sectioned grid, and an END "control room" column
 *  with FILTER and SORT lists plus recently watched live channels. */
@Composable
internal fun SportsTvFavorites(p: FavoritesParams) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth().background(ST.Raised), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.background(ST.Live).padding(horizontal = 12.dp, vertical = 10.dp)) { Text("★ " + tr("MY LIST", "قائمتي"), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black) }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(2.dp), contentPadding = PaddingValues(horizontal = 6.dp)) {
                items(SavedLibraryPreset.entries) { pr -> StTab(presetLabel(pr), pr == p.selectedPreset, { p.onPresetSelected(pr) }) }
            }
        }
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            LazyVerticalGrid(GridCells.Adaptive(140.dp), Modifier.weight(1f).fillMaxHeight(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(4.dp, 4.dp, 4.dp, 48.dp)) {
                if (p.continueWatching.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) { StRowTitle(tr("Continue watching", "متابعة المشاهدة"), trailing = "${p.continueWatching.size}") }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(p.continueWatching, key = { "c${it.history.id}" }) { h ->
                                val prog = if (h.history.totalDurationMs > 0) h.history.resumePositionMs.toFloat() / h.history.totalDurationMs else null
                                StWide(h.title, h.history.posterUrl, h.subtitle, prog, { p.onHistoryClick(h) }, Modifier.width(320.dp))
                            }
                        }
                    }
                }
                val sections = p.sections.filter { it.items.isNotEmpty() }
                if (sections.isEmpty() && p.continueWatching.isEmpty() && p.recentLive.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
                    StEmpty(tr("Your list is empty. Long-press any title to add it.", "قائمتك فارغة. اضغط مطولاً على أي عنصر لإضافته."))
                }
                sections.forEach { section ->
                    item(key = "h${section.key}", span = { GridItemSpan(maxLineSpan) }) { StRowTitle(section.title, trailing = "${section.items.size}") }
                    items(section.items, key = { "${section.key}${it.favorite.contentType}${it.favorite.contentId}" }) { f ->
                        StPoster(f.title, null, f.subtitle, { p.onItemClick(f) }, onLongClick = { p.onItemLongClick(f) })
                    }
                }
            }
            LazyColumn(Modifier.width(250.dp).fillMaxHeight().clip(ST.R).background(ST.Card), verticalArrangement = Arrangement.spacedBy(3.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                item { StColHead(tr("Filter", "تصفية"), SavedLibraryFilter.entries.size) }
                items(SavedLibraryFilter.entries) { f ->
                    val sel = f == p.selectedFilter
                    StCard(onClick = { p.onFilterSelected(f) }, shape = ST.Pill, zoom = 1.02f, container = if (sel) ST.Raised else Color.Transparent, focusedContainer = ST.Line, modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp)) {
                        Row(Modifier.height(36.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.width(4.dp).fillMaxHeight().background(if (sel) ST.Amber else Color.Transparent))
                            Text(f.name.lowercase().replaceFirstChar { it.uppercase() }, color = if (sel) ST.Amber else ST.Text, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp))
                        }
                    }
                }
                item { StColHead(tr("Sort", "ترتيب"), SavedLibrarySort.entries.size) }
                items(SavedLibrarySort.entries) { o ->
                    val sel = o == p.selectedSort
                    StCard(onClick = { p.onSortSelected(o) }, shape = ST.Pill, zoom = 1.02f, container = if (sel) ST.Raised else Color.Transparent, focusedContainer = ST.Line, modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp)) {
                        Row(Modifier.height(36.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.width(4.dp).fillMaxHeight().background(if (sel) ST.Amber else Color.Transparent))
                            Text(o.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }, color = if (sel) ST.Amber else ST.Text, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp))
                        }
                    }
                }
                if (p.recentLive.isNotEmpty()) {
                    item { StColHead(tr("Recent live", "مباشر مؤخراً"), p.recentLive.size) }
                    items(p.recentLive, key = { "l${it.history.id}" }) { h ->
                        StCard(onClick = { p.onHistoryClick(h) }, shape = ST.RSmall, zoom = 1.02f, container = ST.Raised, focusedContainer = ST.Line, modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp)) {
                            Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                StLogo(h.title, h.history.posterUrl, 32.dp)
                                Column { Text(h.title.uppercase(), color = ST.Text, fontSize = 11.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(h.subtitle, color = ST.Faint, fontSize = 10.sp, maxLines = 1) }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Settings: numbered broadcast menu on the START (number block + uppercase label, lime marker for current),
 *  content in a slanted panel with a lime top rule. */
@Composable
internal fun SportsTvSettingsNav(p: SettingsNavParams) {
    LazyColumn(Modifier.width(300.dp).fillMaxHeight().clip(ST.R).background(ST.Card), verticalArrangement = Arrangement.spacedBy(3.dp), contentPadding = PaddingValues(bottom = 8.dp)) {
        item {
            Box(Modifier.fillMaxWidth().background(ST.Amber).padding(horizontal = 14.dp, vertical = 10.dp)) { Text("⚙ " + tr("SETTINGS", "الإعدادات"), color = ST.Bg, fontSize = 16.sp, fontWeight = FontWeight.Black) }
        }
        items(p.entries.size) { i ->
            val (label, icon) = p.entries[i]
            val sel = i == p.selectedCategory
            StCard(
                onClick = { p.onCategorySelected(i) }, shape = ST.Pill, zoom = 1.02f, container = if (sel) ST.Raised else Color.Transparent, focusedContainer = ST.Line,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp).then(if (sel) Modifier.focusRequester(p.focusRequester) else Modifier)
            ) {
                Row(Modifier.height(44.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(40.dp).fillMaxHeight().background(if (sel) ST.Amber else ST.Bg), contentAlignment = Alignment.Center) { Text(icon, fontSize = 15.sp, color = if (sel) ST.Bg else ST.Sub) }
                    Text(label.uppercase(), color = if (sel) ST.Amber else ST.Text, fontSize = 13.sp, fontWeight = FontWeight.Black, maxLines = 1, modifier = Modifier.weight(1f).padding(horizontal = 12.dp))
                    Text("%02d".format(i + 1), color = ST.Faint, fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(end = 10.dp))
                }
            }
        }
    }
}

@Composable
internal fun SportsTvSettingsFrame(navigation: @Composable () -> Unit, content: @Composable () -> Unit) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        navigation()
        Column(Modifier.weight(1f).fillMaxHeight().clip(ST.R).background(ST.Card)) {
            Box(Modifier.fillMaxWidth().height(4.dp).background(Brush.horizontalGradient(listOf(ST.Amber, ST.Line))))
            Box(Modifier.fillMaxSize().padding(22.dp)) { content() }
        }
    }
}
