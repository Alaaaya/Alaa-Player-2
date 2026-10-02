package com.streamvault.app.ui.themes.softmodern

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

/** Paper page with the artwork softly washed out behind a cream veil (no dark scrims). */
@Composable
private fun SmBackdrop(url: String?, content: @Composable BoxScope.() -> Unit) {
    SmBackground {
        url?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize(), alpha = 0.22f) }
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(SM.Bg.copy(alpha = 0.6f), SM.Bg))))
        content()
    }
}

@Composable
private fun Meta(vararg parts: String?) {
    Text(parts.filterNot { it.isNullOrBlank() }.joinToString("  •  "), color = SM.Sub, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

/** Movie: framed poster pebble beside a white info card; pill actions, versions and related shelf below. */
@Composable
internal fun SoftModernMovieDetail(p: MovieDetailParams) {
    val m = p.movie
    val play = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(m.id) { runCatching { play.requestFocus() } }
    SmBackdrop(m.backdropUrl ?: m.posterUrl) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 64.dp, end = 64.dp, top = 80.dp, bottom = 48.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
              Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                Box(Modifier.width(240.dp).aspectRatio(2f / 3f).clip(SM.R).background(SM.Card).padding(8.dp)) {
                    Box(Modifier.fillMaxSize().clip(SM.RSmall).background(SM.Sage)) { (m.posterUrl ?: m.backdropUrl)?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) } }
                }
                Column(Modifier.weight(1f).clip(SM.R).background(SM.Card).padding(28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(m.name, color = SM.Text, fontSize = 38.sp, fontWeight = FontWeight.Bold, maxLines = 2, lineHeight = 42.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (m.rating > 0) SmBadge("★ %.1f".format(m.rating), SM.Amber, filled = true)
                        Meta(m.year, m.duration, m.genre, m.variantLabel)
                    }
                    Text(m.plot.orEmpty(), color = SM.Sub, fontSize = 15.sp, maxLines = 4, overflow = TextOverflow.Ellipsis, lineHeight = 22.sp)
                    m.director?.takeIf { it.isNotBlank() }?.let { Text(tr("Director", "المخرج") + "  $it", color = SM.Faint, fontSize = 13.sp, maxLines = 1) }
                    m.cast?.takeIf { it.isNotBlank() }?.let { Text(tr("Starring", "بطولة") + "  $it", color = SM.Faint, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    if (p.hasResume) Text(tr("Resume from", "استكمال من") + " " + smDuration(p.resumePositionMs), color = SM.Amber, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
              }
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                    item { SmButton(if (p.hasResume) tr("Resume", "استكمال") else tr("Play", "تشغيل"), p.onPlay, Modifier.focusRequester(play), primary = true, icon = "▶") }
                    p.onPlayTrailer?.let { item { SmButton(tr("Trailer", "الإعلان"), it, icon = "▷") } }
                    item { SmButton(if (m.isFavorite) tr("In My List", "في قائمتي") else tr("My List", "قائمتي"), p.onToggleFavorite, icon = if (m.isFavorite) "✓" else "+") }
                    item { SmButton(if (p.isCasting) tr("Casting…", "جار البث…") else tr("Cast", "بث"), p.onCast, icon = "⎚") }
                    item { SmButton(tr("Download", "تنزيل"), p.onDownload, icon = "↓") }
                    item { SmButton(tr("Copy link", "نسخ الرابط"), p.onCopyUrl, icon = "⧉") }
                    item { SmButton(tr("Back", "رجوع"), p.onBack, icon = "←") }
                }
            }
            if (m.variants.size > 1) item {
                Column {
                    SmRowTitle(tr("Versions", "النسخ"))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(m.variants) { v -> SmTab(v.label, v.rawMovieId == m.selectedVariantId, { p.onSelectVariant(v.rawMovieId) }) }
                    }
                }
            }
            if (p.relatedContent.isNotEmpty()) item {
                Column {
                    SmRowTitle(tr("More like this", "مشابه"))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                        items(p.relatedContent, key = { it.id }) { r -> SmPoster(r.name, r.posterUrl, r.year, { p.onRelatedClick(r) }, Modifier.width(140.dp)) }
                    }
                }
            }
        }
    }
}

/** Series: white info card + pill actions, then a sage seasons column beside numbered episode pebbles. */
@Composable
internal fun SoftModernSeriesDetail(p: SeriesDetailParams) {
    val s = p.series
    val primary = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(s.id) { runCatching { primary.requestFocus() } }
    SmBackdrop(s.backdropUrl ?: s.posterUrl) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 64.dp, end = 64.dp, top = 64.dp, bottom = 48.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
              Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Box(Modifier.width(150.dp).aspectRatio(2f / 3f).clip(SM.R).background(SM.Card).padding(6.dp)) {
                    Box(Modifier.fillMaxSize().clip(SM.RSmall).background(SM.Sage)) { (s.posterUrl ?: s.backdropUrl)?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) } }
                }
                Column(Modifier.weight(1f).clip(SM.R).background(SM.Card).padding(26.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(s.name, color = SM.Text, fontSize = 34.sp, fontWeight = FontWeight.Bold, maxLines = 2)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (s.rating > 0) SmBadge("★ %.1f".format(s.rating), SM.Amber, filled = true)
                        if (p.unwatchedEpisodeCount > 0) SmBadge("${p.unwatchedEpisodeCount} " + tr("new", "جديد"), SM.Blue)
                        Meta(s.releaseDate, s.genre, "${s.seasons.size} " + tr("seasons", "مواسم"))
                    }
                    if (!p.isLoadingExternalRatings) {
                        val r = p.externalRatings
                        val ext = listOf("IMDb" to r.imdb, "Rotten Tomatoes" to r.rottenTomatoes, "Metacritic" to r.metacritic).filter { it.second.available }
                        if (ext.isNotEmpty()) Text(ext.joinToString("   ") { "${it.first} ${it.second.displayValue}" }, color = SM.Faint, fontSize = 13.sp)
                    }
                    Text(s.plot.orEmpty(), color = SM.Sub, fontSize = 15.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                }
              }
            }
            item {
                val resume = p.resumeEpisode
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SmButton(
                        if (resume != null) tr("Continue", "متابعة") + " S${resume.seasonNumber}:E${resume.episodeNumber}" else tr("Play", "تشغيل"),
                        { (resume ?: p.selectedSeason?.episodes?.firstOrNull())?.let(p.onResumeClick) }, Modifier.focusRequester(primary), primary = true, icon = "▶"
                    )
                    SmButton(if (s.isFavorite) tr("In My List", "في قائمتي") else tr("My List", "قائمتي"), p.onToggleFavorite, icon = if (s.isFavorite) "✓" else "+")
                    if (resume != null) SmButton(if (p.isCasting) tr("Casting…", "جار البث…") else tr("Cast", "بث"), p.onCastResumeEpisode, icon = "⎚")
                    SmButton(tr("Back", "رجوع"), p.onBack, icon = "←")
                }
            }
            if (s.variants.size > 1) item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(s.variants) { v -> SmTab(v.label, v.rawSeriesId == s.selectedVariantId, { p.onSelectVariant(v.rawSeriesId) }) } }
            }
            item {
                // Seasons live in a sage column on the start side; the selected season's episodes stack beside it.
                Row(Modifier.fillMaxWidth().heightIn(max = 620.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    LazyColumn(Modifier.width(210.dp).clip(SM.R).background(SM.Sage).padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        item { Text(tr("Seasons", "المواسم"), color = SM.AmberDeep, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(6.dp)) }
                        items(s.seasons, key = { it.seasonNumber }) { season ->
                            val sel = season.seasonNumber == p.selectedSeason?.seasonNumber
                            SmCard(onClick = { p.onSeasonSelected(season) }, shape = SM.Pill, container = if (sel) SM.Card else Color.Transparent, focusedContainer = SM.Card, zoom = 1.03f, modifier = Modifier.fillMaxWidth()) {
                                Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(season.name.ifBlank { tr("Season", "الموسم") + " ${season.seasonNumber}" }, color = SM.Text, fontSize = 15.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium, maxLines = 1, modifier = Modifier.weight(1f))
                                    Text("${season.episodes.size}", color = if (sel) SM.Amber else SM.Faint, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                    LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(4.dp)) {
                        items(p.selectedSeason?.episodes.orEmpty(), key = { it.id }) { e ->
                            SmCard(onClick = { p.onEpisodeClick(e) }, modifier = Modifier.fillMaxWidth(), container = SM.Card, zoom = 1.02f) {
                                Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                    Box(Modifier.size(44.dp).clip(SM.Pill).background(SM.Sage), contentAlignment = Alignment.Center) { Text("${e.episodeNumber}", color = SM.AmberDeep, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                                    Box(Modifier.width(160.dp).aspectRatio(16f / 9f).clip(SM.RSmall).background(SM.Raised)) {
                                        e.coverUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                                        if (e.watchProgress > 0 && e.durationSeconds > 0) SmProgress(e.watchProgress / (e.durationSeconds * 1000f), Modifier.align(Alignment.BottomCenter).padding(6.dp), 3.dp)
                                    }
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text(e.title, color = SM.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        e.duration?.let { Text(it, color = SM.Faint, fontSize = 12.sp) }
                                        Text(e.plot.orEmpty(), color = SM.Sub, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        SmRound("⎚", { p.onCastEpisode(e) }, size = 36.dp)
                                        SmRound("↓", { p.onDownloadEpisode(e) }, size = 36.dp)
                                        SmRound("⧉", { p.onCopyEpisodeUrl(e) }, size = 36.dp)
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

/** Search (Soft Modern): white query panel on the start side (field, stacked type pills, recent list);
 *  results on the right as a pill list of channels and a poster grid of titles. */
@Composable
internal fun SoftModernSearch(p: SearchParams) {
    LaunchedEffect(Unit) { runCatching { p.searchFocusRequester.requestFocus() } }
    val s = p.uiState
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        Column(Modifier.width(340.dp).fillMaxHeight().clip(SM.R).background(SM.Card).padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(tr("Find something", "ابحث عن شيء"), color = SM.Text, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            SmSearchField(p.query, p.onQueryChange, tr("Channels, movies, series", "قنوات، أفلام، مسلسلات"), Modifier.fillMaxWidth().focusRequester(p.searchFocusRequester), onSubmit = p.onSearch)
            SearchTab.entries.forEach { t ->
                val label = when (t.name) { "ALL" -> tr("Everything", "الكل"); "LIVE" -> tr("Live channels", "قنوات مباشرة"); "MOVIES" -> tr("Movies", "أفلام"); "SERIES" -> tr("Series", "مسلسلات"); else -> t.name }
                val sel = t == p.selectedTab
                SmCard(onClick = { p.onTabSelected(t) }, shape = SM.Pill, container = if (sel) SM.Sage else Color.Transparent, focusedContainer = SM.Sage, zoom = 1.03f, modifier = Modifier.fillMaxWidth()) {
                    Text(label, color = if (sel) SM.AmberDeep else SM.Text, fontSize = 15.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
                }
            }
            if (p.recentQueries.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(tr("Recent", "الأخيرة"), color = SM.Faint, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    SmTab(tr("Clear", "مسح"), false, p.onClearRecentQueries)
                }
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    items(p.recentQueries) { q ->
                        SmCard(onClick = { p.onRecentQuerySelected(q) }, shape = SM.Pill, container = Color.Transparent, focusedContainer = SM.Raised, zoom = 1.02f, modifier = Modifier.fillMaxWidth()) {
                            Text("↺  $q", color = SM.Sub, fontSize = 14.sp, maxLines = 1, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
                        }
                    }
                }
            } else Spacer(Modifier.weight(1f))
            SmButton(tr("Build full index", "فهرسة كاملة"), p.onBuildCompleteIndex, Modifier.fillMaxWidth(), icon = "↻")
        }
        Box(Modifier.weight(1f).fillMaxHeight()) {
            when {
                p.query.isBlank() -> SmEmpty(tr("Type to search your playlists", "اكتب للبحث في قوائمك"))
                s.isLoading -> SmEmpty(tr("Searching…", "جار البحث…"))
                s.hasSearchError -> SmEmpty(tr("Search failed. Try again.", "فشل البحث. حاول مرة أخرى."))
                s.isEmpty -> SmEmpty(tr("No results for", "لا نتائج لـ") + " “${p.query}”")
                else -> LazyVerticalGrid(GridCells.Adaptive(150.dp), Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(6.dp, 0.dp, 6.dp, 48.dp)) {
                    if (s.channels.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.LIVE)) {
                        item(span = { GridItemSpan(maxLineSpan) }) { SmRowTitle(tr("Live channels", "قنوات مباشرة"), trailing = "${s.channels.size}") }
                        items(s.channels, key = { "c${it.id}" }, span = { GridItemSpan(maxLineSpan) }) { c ->
                            val locked = p.isChannelLocked(c)
                            SmCard(onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c) }, shape = SM.Pill, container = SM.Card, focusedContainer = SM.Sage, zoom = 1.01f, modifier = Modifier.fillMaxWidth()) {
                                Row(Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Box(Modifier.size(40.dp).clip(SM.Pill).background(SM.Raised), contentAlignment = Alignment.Center) { SmLogo(c.name, if (locked) null else c.logoUrl, 30.dp) }
                                    Text(c.name, color = SM.Text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(0.4f))
                                    Text(if (locked) "🔒" else c.currentProgram?.title.orEmpty(), color = SM.Faint, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(0.6f))
                                    if (c.id in p.recordingChannelIds) SmBadge("REC", SM.Live, filled = true) else if (c.id in p.scheduledChannelIds) SmBadge("◷", SM.Blue)
                                    c.qualityBadge()?.let { SmBadge(it, SM.Sub) }
                                }
                            }
                        }
                    }
                    if (s.movies.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.MOVIES)) {
                        item(span = { GridItemSpan(maxLineSpan) }) { SmRowTitle(tr("Movies", "أفلام"), trailing = "${s.movies.size}") }
                        items(s.movies, key = { "m${it.id}" }) { m -> val l = p.isMovieLocked(m); SmPoster(m.name, if (l) null else m.posterUrl, m.year, { p.onMovieClick(m) }, locked = l, onLongClick = { p.onMovieLongClick(m) }) }
                    }
                    if (s.series.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.SERIES)) {
                        item(span = { GridItemSpan(maxLineSpan) }) { SmRowTitle(tr("Series", "مسلسلات"), trailing = "${s.series.size}") }
                        items(s.series, key = { "s${it.id}" }) { m -> val l = p.isSeriesLocked(m); SmPoster(m.name, if (l) null else m.posterUrl, m.genre, { p.onSeriesClick(m) }, locked = l, onLongClick = { p.onSeriesLongClick(m) }) }
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

/** My List (Soft Modern): sage preset rail on the start side, segmented white filter/sort capsules, poster grid with section headers. */
@Composable
internal fun SoftModernFavorites(p: FavoritesParams) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
      Column(Modifier.width(220.dp).fillMaxHeight().clip(SM.R).background(SM.Sage).padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(tr("My List", "قائمتي"), color = SM.AmberDeep, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(6.dp))
        SavedLibraryPreset.entries.forEach { pr ->
            val sel = pr == p.selectedPreset
            SmCard(onClick = { p.onPresetSelected(pr) }, shape = SM.Pill, container = if (sel) SM.Card else Color.Transparent, focusedContainer = SM.Card, zoom = 1.03f, modifier = Modifier.fillMaxWidth()) {
                Text(presetLabel(pr), color = SM.Text, fontSize = 14.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium, maxLines = 1, modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp))
            }
        }
      }
      Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.clip(SM.Pill).background(SM.Card).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                SavedLibraryFilter.entries.forEach { f -> SmTab(f.name.lowercase().replaceFirstChar { it.uppercase() }, f == p.selectedFilter, { p.onFilterSelected(f) }) }
            }
            Row(Modifier.clip(SM.Pill).background(SM.Card).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(" ⇅ ", color = SM.Faint, fontSize = 13.sp)
                SavedLibrarySort.entries.forEach { o -> SmTab(o.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }, o == p.selectedSort, { p.onSortSelected(o) }) }
            }
        }
        LazyVerticalGrid(GridCells.Adaptive(150.dp), Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(18.dp), verticalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(6.dp, 6.dp, 6.dp, 48.dp)) {
            if (p.continueWatching.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) { SmRowTitle(tr("Continue watching", "متابعة المشاهدة")) }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(p.continueWatching, key = { "c${it.history.id}" }) { h ->
                            val prog = if (h.history.totalDurationMs > 0) h.history.resumePositionMs.toFloat() / h.history.totalDurationMs else null
                            SmWide(h.title, h.history.posterUrl, h.subtitle, prog, { p.onHistoryClick(h) }, Modifier.width(260.dp))
                        }
                    }
                }
            }
            if (p.recentLive.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) { SmRowTitle(tr("Recently watched live", "مباشر شوهد مؤخراً")) }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(p.recentLive, key = { "l${it.history.id}" }) { h ->
                            SmCard(onClick = { p.onHistoryClick(h) }, modifier = Modifier.width(200.dp), container = SM.Card) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    SmLogo(h.title, h.history.posterUrl, 40.dp)
                                    Column { Text(h.title, color = SM.Text, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(h.subtitle, color = SM.Faint, fontSize = 11.sp, maxLines = 1) }
                                }
                            }
                        }
                    }
                }
            }
            val sections = p.sections.filter { it.items.isNotEmpty() }
            if (sections.isEmpty() && p.continueWatching.isEmpty() && p.recentLive.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
                SmEmpty(tr("Your list is empty. Long-press any title to add it.", "قائمتك فارغة. اضغط مطولاً على أي عنصر لإضافته."))
            }
            sections.forEach { section ->
                item(key = "h${section.key}", span = { GridItemSpan(maxLineSpan) }) { SmRowTitle(section.title, trailing = "${section.items.size}") }
                items(section.items, key = { "${section.key}${it.favorite.contentType}${it.favorite.contentId}" }) { f ->
                    SmPoster(f.title, null, f.subtitle, { p.onItemClick(f) }, onLongClick = { p.onItemLongClick(f) })
                }
            }
        }
    }
  }
}

/** Settings: large rounded category cards stacked on the start side (icon in an amber circle). */
@Composable
internal fun SoftModernSettingsNav(p: SettingsNavParams) {
    LazyColumn(Modifier.width(300.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(6.dp), contentPadding = PaddingValues(vertical = 8.dp, horizontal = 6.dp)) {
        item { Text(tr("Settings", "الإعدادات"), color = SM.Text, fontSize = 28.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)) }
        items(p.entries.size) { i ->
            val (label, icon) = p.entries[i]
            val sel = i == p.selectedCategory
            SmCard(
                onClick = { p.onCategorySelected(i) }, zoom = 1.03f, shape = SM.Pill, container = if (sel) SM.Card else Color.Transparent, focusedContainer = SM.Sage,
                modifier = Modifier.fillMaxWidth().then(if (sel) Modifier.focusRequester(p.focusRequester) else Modifier)
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(Modifier.size(36.dp).clip(SM.Pill).background(if (sel) SM.Amber else SM.Sage), contentAlignment = Alignment.Center) { Text(icon, fontSize = 16.sp, color = if (sel) SM.Card else SM.AmberDeep) }
                    Text(label, color = SM.Text, fontSize = 16.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium, maxLines = 1, modifier = Modifier.weight(1f))
                    if (sel) Text("›", color = SM.Amber, fontSize = 20.sp)
                }
            }
        }
    }
}

@Composable
internal fun SoftModernSettingsFrame(navigation: @Composable () -> Unit, content: @Composable () -> Unit) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
        navigation()
        Box(Modifier.weight(1f).fillMaxHeight().clip(SM.R).background(SM.Card).padding(24.dp)) { content() }
    }
}
