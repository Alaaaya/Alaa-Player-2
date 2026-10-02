package com.streamvault.app.ui.themes.auroralounge

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

/** Lounge room with the artwork blurred-dim behind and an aurora wash; artwork never runs edge-to-edge like a streamer. */
@Composable
private fun AlRoom(url: String?, content: @Composable BoxScope.() -> Unit) {
    AlBackground {
        url?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, alpha = 0.18f, modifier = Modifier.fillMaxSize()) }
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(AL.Bg.copy(alpha = 0.4f), AL.Bg.copy(alpha = 0.85f), AL.Bg))))
        content()
    }
}

@Composable
private fun Meta(vararg parts: String?) {
    Text(parts.filterNot { it.isNullOrBlank() }.joinToString("  ☾  "), color = AL.Sub, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

/** Framed poster pebble with a gold candle halo behind it: the lounge's "painting on the wall". */
@Composable
private fun Framed(url: String?, title: String, width: androidx.compose.ui.unit.Dp) {
    Box(contentAlignment = Alignment.Center) {
        Box(Modifier.width(width + 60.dp).aspectRatio(2f / 3f).background(Brush.radialGradient(listOf(AL.Amber.copy(alpha = 0.28f), Color.Transparent))))
        Box(Modifier.width(width).aspectRatio(2f / 3f).clip(AL.R).background(Brush.linearGradient(listOf(AL.Rose.copy(alpha = 0.35f), AL.Line, AL.Blue.copy(alpha = 0.25f))))) {
            Text(title.take(1).uppercase(), color = AL.Sub, fontSize = 48.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.Center))
            url?.let { AsyncImage(it, title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
        }
    }
}

/** Movie: two-part lounge. START = framed poster with halo; END = a tall rounded "menu card" with serif title, meta,
 *  plot, credits and a vertical stack of candle actions beside a moon column. Versions + related below the card. */
@Composable
internal fun AuroraLoungeMovieDetail(p: MovieDetailParams) {
    val m = p.movie
    val play = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(m.id) { runCatching { play.requestFocus() } }
    AlRoom(m.backdropUrl ?: m.posterUrl) {
        Row(Modifier.fillMaxSize().padding(horizontal = 56.dp, vertical = 40.dp), horizontalArrangement = Arrangement.spacedBy(44.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxHeight(), ) {
                Framed(m.posterUrl, m.name, 300.dp)
                if (m.rating > 0) AlBadge("★ %.1f".format(m.rating), AL.Amber, filled = true)
                if (p.hasResume) Text(tr("Resume from", "استكمال من") + " " + alDuration(p.resumePositionMs), color = AL.Amber, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            LazyColumn(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(bottom = 32.dp)) {
                item {
                    Column(Modifier.fillMaxWidth().clip(AL.R).background(AL.Raised.copy(alpha = 0.82f)).padding(30.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(m.name, color = AL.Text, fontSize = 40.sp, fontWeight = FontWeight.Light, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, maxLines = 2, lineHeight = 46.sp)
                        Box(Modifier.width(160.dp).height(1.dp).background(Brush.horizontalGradient(listOf(AL.Amber, Color.Transparent))))
                        Meta(m.year, m.duration, m.genre, m.variantLabel)
                        Text(m.plot.orEmpty(), color = AL.Sub, fontSize = 15.sp, maxLines = 5, overflow = TextOverflow.Ellipsis, lineHeight = 22.sp)
                        m.director?.takeIf { it.isNotBlank() }?.let { Text(tr("Director", "المخرج") + "  $it", color = AL.Faint, fontSize = 13.sp, maxLines = 1) }
                        m.cast?.takeIf { it.isNotBlank() }?.let { Text(tr("Starring", "بطولة") + "  $it", color = AL.Faint, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 6.dp)) {
                            AlButton(if (p.hasResume) tr("Resume", "استكمال") else tr("Play", "تشغيل"), p.onPlay, Modifier.focusRequester(play), primary = true, icon = "▶")
                            p.onPlayTrailer?.let { AlRound("▷", it, size = 48.dp) }
                            AlRound(if (m.isFavorite) "♥" else "♡", p.onToggleFavorite, size = 48.dp, active = m.isFavorite)
                            AlRound("⎚", p.onCast, size = 48.dp, active = p.isCasting)
                            AlRound("↓", p.onDownload, size = 48.dp)
                            AlRound("⧉", p.onCopyUrl, size = 48.dp)
                            AlRound("←", p.onBack, size = 48.dp)
                        }
                    }
                }
                if (m.variants.size > 1) item {
                    Column {
                        AlRowTitle(tr("Versions", "النسخ"))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(m.variants) { v -> AlTab(v.label, v.rawMovieId == m.selectedVariantId, { p.onSelectVariant(v.rawMovieId) }) }
                        }
                    }
                }
                if (p.relatedContent.isNotEmpty()) item {
                    Column {
                        AlRowTitle(tr("For the same mood", "بنفس المزاج"))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                            items(p.relatedContent, key = { it.id }) { r -> AlPoster(r.name, r.posterUrl, r.year, { p.onRelatedClick(r) }, Modifier.width(130.dp)) }
                        }
                    }
                }
            }
        }
    }
}

/** Series: a header lamp strip (small framed poster, serif title, meta, actions) then two lounge columns:
 *  START = season moons stacked vertically, END = episode list as soft wide rows with a moon action trio. */
@Composable
internal fun AuroraLoungeSeriesDetail(p: SeriesDetailParams) {
    val s = p.series
    val primary = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(s.id) { runCatching { primary.requestFocus() } }
    AlRoom(s.backdropUrl ?: s.posterUrl) {
        Column(Modifier.fillMaxSize().padding(horizontal = 56.dp, vertical = 32.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(Modifier.fillMaxWidth().clip(AL.R).background(AL.Raised.copy(alpha = 0.82f)).padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Box(Modifier.width(110.dp).aspectRatio(2f / 3f).clip(AL.RSmall).background(AL.Card)) { (s.posterUrl ?: s.backdropUrl)?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) } }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(s.name, color = AL.Text, fontSize = 34.sp, fontWeight = FontWeight.Light, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (s.rating > 0) AlBadge("★ %.1f".format(s.rating), AL.Amber, filled = true)
                        if (p.unwatchedEpisodeCount > 0) AlBadge("${p.unwatchedEpisodeCount} " + tr("new", "جديد"), AL.Blue)
                        Meta(s.releaseDate, s.genre, "${s.seasons.size} " + tr("seasons", "مواسم"))
                    }
                    if (!p.isLoadingExternalRatings) {
                        val r = p.externalRatings
                        val ext = listOf("IMDb" to r.imdb, "Rotten Tomatoes" to r.rottenTomatoes, "Metacritic" to r.metacritic).filter { it.second.available }
                        if (ext.isNotEmpty()) Text(ext.joinToString("   ") { "${it.first} ${it.second.displayValue}" }, color = AL.Faint, fontSize = 13.sp)
                    }
                    Text(s.plot.orEmpty(), color = AL.Sub, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val resume = p.resumeEpisode
                    AlButton(
                        if (resume != null) tr("Continue", "متابعة") + " S${resume.seasonNumber}:E${resume.episodeNumber}" else tr("Play", "تشغيل"),
                        { (resume ?: p.selectedSeason?.episodes?.firstOrNull())?.let(p.onResumeClick) }, Modifier.focusRequester(primary), primary = true, icon = "▶"
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AlRound(if (s.isFavorite) "♥" else "♡", p.onToggleFavorite, size = 44.dp, active = s.isFavorite)
                        if (resume != null) AlRound("⎚", p.onCastResumeEpisode, size = 44.dp, active = p.isCasting)
                        AlRound("←", p.onBack, size = 44.dp)
                    }
                }
            }
            if (s.variants.size > 1) LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(s.variants) { v -> AlTab(v.label, v.rawSeriesId == s.selectedVariantId, { p.onSelectVariant(v.rawSeriesId) }) } }
            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                LazyColumn(Modifier.width(150.dp).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 6.dp)) {
                    items(s.seasons, key = { it.seasonNumber }) { season ->
                        val sel = season.seasonNumber == p.selectedSeason?.seasonNumber
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            AlRound("${season.seasonNumber}", { p.onSeasonSelected(season) }, size = 58.dp, active = sel)
                            Text(season.name.ifBlank { tr("Season", "الموسم") + " ${season.seasonNumber}" }, color = if (sel) AL.Amber else AL.Faint, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                LazyColumn(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 32.dp)) {
                    items(p.selectedSeason?.episodes.orEmpty(), key = { it.id }) { e ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            AlCard(onClick = { p.onEpisodeClick(e) }, modifier = Modifier.weight(1f), shape = AL.Pill, container = AL.Raised.copy(alpha = 0.8f), zoom = 1.02f) {
                                Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Box(Modifier.size(72.dp).clip(androidx.compose.foundation.shape.CircleShape).background(AL.Card)) {
                                        e.coverUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                                        Text("${e.episodeNumber}", color = AL.Text, fontSize = 20.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.Center))
                                    }
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text(e.title, color = AL.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(listOfNotNull(e.duration, e.plot).joinToString("  ☾  "), color = AL.Faint, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        if (e.watchProgress > 0 && e.durationSeconds > 0) AlProgress(e.watchProgress / (e.durationSeconds * 1000f), Modifier.fillMaxWidth(0.6f), 3.dp)
                                    }
                                    Text("▶", color = AL.Amber, fontSize = 18.sp, modifier = Modifier.padding(end = 16.dp))
                                }
                            }
                            AlRound("⎚", { p.onCastEpisode(e) }, size = 38.dp)
                            AlRound("↓", { p.onDownloadEpisode(e) }, size = 38.dp)
                            AlRound("⧉", { p.onCopyEpisodeUrl(e) }, size = 38.dp)
                        }
                    }
                }
            }
        }
    }
}

/** Search: a START "lamp desk" (field, vertical scope lamps, recent searches stacked) and END results as a single
 *  scrolling list of lounge sections: channels as moon pills, movies/series as poster shelves. */
@Composable
internal fun AuroraLoungeSearch(p: SearchParams) {
    LaunchedEffect(Unit) { runCatching { p.searchFocusRequester.requestFocus() } }
    val s = p.uiState
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
        LazyColumn(Modifier.width(340.dp).fillMaxHeight().clip(AL.R).background(AL.Raised.copy(alpha = 0.8f)), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(20.dp)) {
            item { Text(tr("Find something", "ابحث عن شيء"), color = AL.Text, fontSize = 24.sp, fontWeight = FontWeight.Light, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, modifier = Modifier.padding(bottom = 6.dp)) }
            item { AlSearchField(p.query, p.onQueryChange, tr("Channels, movies, series", "قنوات، أفلام، مسلسلات"), Modifier.fillMaxWidth().focusRequester(p.searchFocusRequester), onSubmit = p.onSearch) }
            items(SearchTab.entries) { t ->
                val label = when (t.name) { "ALL" -> tr("Everything", "الكل"); "LIVE" -> tr("Live", "مباشر"); "MOVIES" -> tr("Movies", "أفلام"); "SERIES" -> tr("Series", "مسلسلات"); else -> t.name }
                AlTab(label, t == p.selectedTab, { p.onTabSelected(t) }, Modifier.fillMaxWidth())
            }
            item { AlTab("↻ " + tr("Full index", "فهرسة كاملة"), false, p.onBuildCompleteIndex, Modifier.fillMaxWidth()) }
            if (p.recentQueries.isNotEmpty()) {
                item { AlRowTitle(tr("Recently", "مؤخراً"), Modifier.padding(top = 10.dp)) }
                items(p.recentQueries) { q -> AlTab("↺ $q", false, { p.onRecentQuerySelected(q) }, Modifier.fillMaxWidth()) }
                item { AlTab(tr("Clear", "مسح"), false, p.onClearRecentQueries, Modifier.fillMaxWidth()) }
            }
        }
        LazyColumn(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(22.dp), contentPadding = PaddingValues(bottom = 48.dp)) {
            when {
                p.query.isBlank() -> item { AlEmpty(tr("Type to search the lounge", "اكتب للبحث")) }
                s.isLoading -> item { AlEmpty(tr("Searching…", "جار البحث…")) }
                s.hasSearchError -> item { AlEmpty(tr("Search failed. Try again.", "فشل البحث. حاول مرة أخرى.")) }
                s.isEmpty -> item { AlEmpty(tr("No results for", "لا نتائج لـ") + " “${p.query}”") }
                else -> {
                    if (s.channels.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.LIVE)) {
                        item { AlRowTitle(tr("Live channels", "قنوات مباشرة"), trailing = "${s.channels.size}") }
                        items(s.channels, key = { "c" + it.id }) { c ->
                            val locked = p.isChannelLocked(c)
                            AlCard(onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c) }, shape = AL.Pill, modifier = Modifier.fillMaxWidth(), container = AL.Raised.copy(alpha = 0.7f), zoom = 1.02f) {
                                Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                    AlLogo(c.name, if (locked) null else c.logoUrl, 46.dp)
                                    Text(c.name, color = AL.Text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(260.dp))
                                    Text(if (locked) "🔒" else c.currentProgram?.title.orEmpty(), color = AL.Faint, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                    if (c.id in p.recordingChannelIds) AlBadge("REC", AL.Live, filled = true)
                                    else if (c.id in p.scheduledChannelIds) AlBadge("◷", AL.Blue)
                                    c.qualityBadge()?.let { AlBadge(it, AL.Text) }
                                }
                            }
                        }
                    }
                    if (s.movies.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.MOVIES)) item {
                        Column {
                            AlRowTitle(tr("Movies", "أفلام"), trailing = "${s.movies.size}")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                                items(s.movies, key = { it.id }) { m -> val l = p.isMovieLocked(m); AlPoster(m.name, if (l) null else m.posterUrl, m.year, { p.onMovieClick(m) }, Modifier.width(130.dp), locked = l, onLongClick = { p.onMovieLongClick(m) }) }
                            }
                        }
                    }
                    if (s.series.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.SERIES)) item {
                        Column {
                            AlRowTitle(tr("Series", "مسلسلات"), trailing = "${s.series.size}")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                                items(s.series, key = { it.id }) { m -> val l = p.isSeriesLocked(m); AlPoster(m.name, if (l) null else m.posterUrl, m.genre, { p.onSeriesClick(m) }, Modifier.width(130.dp), locked = l, onLongClick = { p.onSeriesLongClick(m) }) }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** My List: START crescent rail of presets (vertical lamps), END = filter/sort lamps over a poster grid with
 *  serif section headings; continue watching + recent live sit as shelves at the top of the grid. */
@Composable
internal fun AuroraLoungeFavorites(p: FavoritesParams) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        LazyColumn(Modifier.width(230.dp).fillMaxHeight().clip(AL.R).background(AL.Raised.copy(alpha = 0.75f)), verticalArrangement = Arrangement.spacedBy(6.dp), contentPadding = PaddingValues(16.dp)) {
            item { Text("☾ " + tr("My lounge", "صالتي"), color = AL.Text, fontSize = 22.sp, fontWeight = FontWeight.Light, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, modifier = Modifier.padding(bottom = 8.dp)) }
            items(SavedLibraryPreset.entries) { pr -> AlTab(presetLabel(pr), pr == p.selectedPreset, { p.onPresetSelected(pr) }, Modifier.fillMaxWidth()) }
        }
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                items(SavedLibraryFilter.entries) { f -> AlTab(f.name.lowercase().replaceFirstChar { it.uppercase() }, f == p.selectedFilter, { p.onFilterSelected(f) }) }
                item { Text("   ⇅", color = AL.Faint, fontSize = 14.sp) }
                items(SavedLibrarySort.entries) { o -> AlTab(o.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }, o == p.selectedSort, { p.onSortSelected(o) }) }
            }
            LazyVerticalGrid(GridCells.Adaptive(140.dp), Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(18.dp), verticalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(6.dp, 6.dp, 6.dp, 48.dp)) {
                if (p.continueWatching.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) { AlRowTitle(tr("Still watching", "ما زلت تشاهد")) }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(p.continueWatching, key = { "c${it.history.id}" }) { h ->
                                val prog = if (h.history.totalDurationMs > 0) h.history.resumePositionMs.toFloat() / h.history.totalDurationMs else null
                                AlWide(h.title, h.history.posterUrl, h.subtitle, prog, { p.onHistoryClick(h) }, Modifier.width(250.dp))
                            }
                        }
                    }
                }
                if (p.recentLive.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) { AlRowTitle(tr("Recent channels", "قنوات حديثة")) }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                            items(p.recentLive, key = { "l${it.history.id}" }) { h ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(96.dp)) {
                                    AlCard(onClick = { p.onHistoryClick(h) }, shape = androidx.compose.foundation.shape.CircleShape, modifier = Modifier.size(76.dp), container = AL.Raised) {
                                        AlLogo(h.title, h.history.posterUrl, 76.dp)
                                    }
                                    Text(h.title, color = AL.Sub, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 6.dp))
                                }
                            }
                        }
                    }
                }
                val sections = p.sections.filter { it.items.isNotEmpty() }
                if (sections.isEmpty() && p.continueWatching.isEmpty() && p.recentLive.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
                    AlEmpty(tr("Your lounge is empty. Long-press any title to add it.", "صالتك فارغة. اضغط مطولاً على أي عنصر لإضافته."))
                }
                sections.forEach { section ->
                    item(key = "h${section.key}", span = { GridItemSpan(maxLineSpan) }) { AlRowTitle(section.title, trailing = "${section.items.size}") }
                    items(section.items, key = { "${section.key}${it.favorite.contentType}${it.favorite.contentId}" }) { f ->
                        AlPoster(f.title, null, f.subtitle, { p.onItemClick(f) }, onLongClick = { p.onItemLongClick(f) })
                    }
                }
            }
        }
    }
}

/** Settings: categories as a TOP row of moon lamps (icon moon + caption), the page below in one wide rounded room. */
@Composable
internal fun AuroraLoungeSettingsNav(p: SettingsNavParams) {
    LazyRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)) {
        item { Text(tr("Settings", "الإعدادات"), color = AL.Text, fontSize = 26.sp, fontWeight = FontWeight.Light, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, modifier = Modifier.padding(end = 18.dp)) }
        items(p.entries.size) { i ->
            val (label, icon) = p.entries[i]
            val sel = i == p.selectedCategory
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.width(96.dp)) {
                AlRound(icon, { p.onCategorySelected(i) }, Modifier.then(if (sel) Modifier.focusRequester(p.focusRequester) else Modifier), size = 54.dp, active = sel)
                Text(label, color = if (sel) AL.Amber else AL.Sub, fontSize = 12.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
internal fun AuroraLoungeSettingsFrame(navigation: @Composable () -> Unit, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        navigation()
        Box(Modifier.weight(1f).fillMaxWidth().clip(AL.R).background(Brush.verticalGradient(listOf(AL.Raised, AL.Bg.copy(alpha = 0.9f)))).padding(26.dp)) { content() }
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

