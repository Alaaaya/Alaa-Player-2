package com.streamvault.app.ui.themes.darkglass

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
import androidx.compose.ui.draw.blur
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
private fun DgBackdrop(url: String?, content: @Composable BoxScope.() -> Unit) {
    DgBackground {
        url?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, alpha = 0.45f, modifier = Modifier.fillMaxSize().blur(28.dp)) }
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(DG.Bg.copy(alpha = 0.3f), DG.Bg.copy(alpha = 0.85f)))))
        content()
    }
}

@Composable
private fun Meta(vararg parts: String?) {
    Text(parts.filterNot { it.isNullOrBlank() }.joinToString("  •  "), color = DG.Sub, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

/** Movie: hero over backdrop with title, meta line, plot and a row of pill actions; versions + "More like this" below. */
@Composable
internal fun DarkGlassMovieDetail(p: MovieDetailParams) {
    val m = p.movie
    val play = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(m.id) { runCatching { play.requestFocus() } }
    DgBackdrop(m.backdropUrl ?: m.posterUrl) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 48.dp, end = 48.dp, top = 40.dp, bottom = 48.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
              Row(Modifier.fillMaxWidth().dgGlass(DG.R, 0.07f).padding(24.dp), horizontalArrangement = Arrangement.spacedBy(26.dp)) {
                Box(Modifier.width(220.dp).aspectRatio(2f / 3f).dgGlass(DG.R, 0.05f).padding(6.dp).clip(DG.RSmall)) { m.posterUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) } }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(m.name, color = DG.Text, fontSize = 42.sp, fontWeight = FontWeight.Thin, maxLines = 2, lineHeight = 46.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (m.rating > 0) DgBadge("★ %.1f".format(m.rating), DG.Amber, filled = true)
                        Meta(m.year, m.duration, m.genre, m.variantLabel)
                    }
                    Text(m.plot.orEmpty(), color = DG.Sub, fontSize = 15.sp, maxLines = 4, overflow = TextOverflow.Ellipsis, lineHeight = 22.sp)
                    m.director?.takeIf { it.isNotBlank() }?.let { Text(tr("Director", "المخرج") + "  $it", color = DG.Faint, fontSize = 13.sp, maxLines = 1) }
                    m.cast?.takeIf { it.isNotBlank() }?.let { Text(tr("Starring", "بطولة") + "  $it", color = DG.Faint, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    if (p.hasResume) Text(tr("Resume from", "استكمال من") + " " + dgDuration(p.resumePositionMs), color = DG.Amber, fontSize = 13.sp)
                }
                Column(Modifier.width(200.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DgButton(if (p.hasResume) tr("Resume", "استكمال") else tr("Play", "تشغيل"), p.onPlay, Modifier.fillMaxWidth().focusRequester(play), primary = true, icon = "▶")
                    p.onPlayTrailer?.let { DgButton(tr("Trailer", "الإعلان"), it, Modifier.fillMaxWidth(), icon = "▷") }
                    DgButton(if (m.isFavorite) tr("In My List", "في قائمتي") else tr("My List", "قائمتي"), p.onToggleFavorite, Modifier.fillMaxWidth(), icon = if (m.isFavorite) "✓" else "+")
                    DgButton(if (p.isCasting) tr("Casting…", "جار البث…") else tr("Cast", "بث"), p.onCast, Modifier.fillMaxWidth(), icon = "⎚")
                    DgButton(tr("Download", "تنزيل"), p.onDownload, Modifier.fillMaxWidth(), icon = "↓")
                    DgButton(tr("Copy link", "نسخ الرابط"), p.onCopyUrl, Modifier.fillMaxWidth(), icon = "⧉")
                    DgButton(tr("Back", "رجوع"), p.onBack, Modifier.fillMaxWidth(), icon = "←")
                }
              }
            }
            if (m.variants.size > 1) item {
                Column {
                    DgRowTitle(tr("Versions", "النسخ"))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(m.variants) { v -> DgTab(v.label, v.rawMovieId == m.selectedVariantId, { p.onSelectVariant(v.rawMovieId) }) }
                    }
                }
            }
            if (p.relatedContent.isNotEmpty()) item {
                Column {
                    DgRowTitle(tr("More like this", "مشابه"))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                        items(p.relatedContent, key = { it.id }) { r -> DgPoster(r.name, r.posterUrl, r.year, { p.onRelatedClick(r) }, Modifier.width(140.dp)) }
                    }
                }
            }
        }
    }
}

/** Series: backdrop hero with play/resume, season pills, landscape episode cards in a horizontal shelf + episode list. */
@Composable
internal fun DarkGlassSeriesDetail(p: SeriesDetailParams) {
    val s = p.series
    val primary = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(s.id) { runCatching { primary.requestFocus() } }
    DgBackdrop(s.backdropUrl ?: s.posterUrl) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 64.dp, end = 64.dp, top = 64.dp, bottom = 48.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Column(Modifier.fillMaxWidth().dgGlass(DG.R, 0.06f).padding(22.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(s.name, color = DG.Text, fontSize = 40.sp, fontWeight = FontWeight.Thin, maxLines = 2)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (s.rating > 0) DgBadge("★ %.1f".format(s.rating), DG.Amber, filled = true)
                        if (p.unwatchedEpisodeCount > 0) DgBadge("${p.unwatchedEpisodeCount} " + tr("new", "جديد"), DG.Blue)
                        Meta(s.releaseDate, s.genre, "${s.seasons.size} " + tr("seasons", "مواسم"))
                    }
                    if (!p.isLoadingExternalRatings) {
                        val r = p.externalRatings
                        val ext = listOf("IMDb" to r.imdb, "Rotten Tomatoes" to r.rottenTomatoes, "Metacritic" to r.metacritic).filter { it.second.available }
                        if (ext.isNotEmpty()) Text(ext.joinToString("   ") { "${it.first} ${it.second.displayValue}" }, color = DG.Faint, fontSize = 13.sp)
                    }
                    Text(s.plot.orEmpty(), color = DG.Sub, fontSize = 15.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                }
            }
            item {
                val resume = p.resumeEpisode
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DgButton(
                        if (resume != null) tr("Continue", "متابعة") + " S${resume.seasonNumber}:E${resume.episodeNumber}" else tr("Play", "تشغيل"),
                        { (resume ?: p.selectedSeason?.episodes?.firstOrNull())?.let(p.onResumeClick) }, Modifier.focusRequester(primary), primary = true, icon = "▶"
                    )
                    DgButton(if (s.isFavorite) tr("In My List", "في قائمتي") else tr("My List", "قائمتي"), p.onToggleFavorite, icon = if (s.isFavorite) "✓" else "+")
                    if (resume != null) DgButton(if (p.isCasting) tr("Casting…", "جار البث…") else tr("Cast", "بث"), p.onCastResumeEpisode, icon = "⎚")
                    DgButton(tr("Back", "رجوع"), p.onBack, icon = "←")
                }
            }
            if (s.variants.size > 1) item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(s.variants) { v -> DgTab(v.label, v.rawSeriesId == s.selectedVariantId, { p.onSelectVariant(v.rawSeriesId) }) } }
            }
            item {
                Row(Modifier.fillMaxWidth().height(520.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LazyColumn(Modifier.width(220.dp).fillMaxHeight().dgGlass(DG.R, 0.05f).padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        item { Text(tr("Seasons", "المواسم"), color = DG.Faint, fontSize = 12.sp, modifier = Modifier.padding(6.dp)) }
                        items(s.seasons, key = { it.seasonNumber }) { season ->
                            val sel = season.seasonNumber == p.selectedSeason?.seasonNumber
                            DgCard(onClick = { p.onSeasonSelected(season) }, shape = DG.Pill, zoom = 1.03f, container = if (sel) Color(0x33B69CFF) else Color.Transparent, modifier = Modifier.fillMaxWidth()) {
                                Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                    Text(season.name.ifBlank { tr("Season", "الموسم") + " ${season.seasonNumber}" }, color = DG.Text, fontSize = 14.sp, fontWeight = if (sel) FontWeight.Medium else FontWeight.Light, maxLines = 1, modifier = Modifier.weight(1f))
                                    Text("${season.episodes.size}", color = DG.Amber, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                    LazyColumn(Modifier.weight(1f).fillMaxHeight().dgGlass(DG.R, 0.03f).padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(p.selectedSeason?.episodes.orEmpty(), key = { it.id }) { e ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                DgCard(onClick = { p.onEpisodeClick(e) }, modifier = Modifier.weight(1f), zoom = 1.02f) {
                                    Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                        Box(Modifier.width(170.dp).aspectRatio(16f / 9f).clip(DG.RSmall).background(DG.Solid)) {
                                            e.coverUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                                            Text("${e.episodeNumber}", color = DG.Text, fontSize = 13.sp, modifier = Modifier.align(Alignment.TopStart).padding(6.dp).dgGlass(DG.Pill, 0.15f).padding(horizontal = 8.dp, vertical = 2.dp))
                                            if (e.watchProgress > 0 && e.durationSeconds > 0) DgProgress(e.watchProgress / (e.durationSeconds * 1000f), Modifier.align(Alignment.BottomCenter).padding(6.dp), 3.dp)
                                        }
                                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(e.title, color = DG.Text, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            e.duration?.let { Text(it, color = DG.Amber, fontSize = 11.sp) }
                                            Text(e.plot.orEmpty(), color = DG.Sub, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    DgRound("⎚", { p.onCastEpisode(e) }, size = 38.dp)
                                    DgRound("↓", { p.onDownloadEpisode(e) }, size = 38.dp)
                                    DgRound("⧉", { p.onCopyEpisodeUrl(e) }, size = 38.dp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Search: centered big field, recent searches as pills, results as titled shelves (channels wide, posters tall). */
@Composable
internal fun DarkGlassSearch(p: SearchParams) {
    LaunchedEffect(Unit) { runCatching { p.searchFocusRequester.requestFocus() } }
    val s = p.uiState
  Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
    Column(Modifier.width(300.dp).fillMaxHeight().dgGlass(DG.R, 0.06f).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(tr("Search", "بحث"), color = DG.Text, fontSize = 26.sp, fontWeight = FontWeight.Thin)
        DgSearchField(p.query, p.onQueryChange, tr("Channels, movies, series", "قنوات، أفلام، مسلسلات"), Modifier.fillMaxWidth().focusRequester(p.searchFocusRequester), onSubmit = p.onSearch)
        SearchTab.entries.forEach { t ->
            val label = when (t.name) { "ALL" -> tr("All", "الكل"); "LIVE" -> tr("Live", "مباشر"); "MOVIES" -> tr("Movies", "أفلام"); "SERIES" -> tr("Series", "مسلسلات"); else -> t.name }
            DgTab(label, t == p.selectedTab, { p.onTabSelected(t) }, Modifier.fillMaxWidth())
        }
        DgTab("↻ " + tr("Full index", "فهرسة كاملة"), false, p.onBuildCompleteIndex, Modifier.fillMaxWidth())
        if (p.recentQueries.isNotEmpty()) {
            Text(tr("Recent", "الأخيرة"), color = DG.Faint, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                items(p.recentQueries) { q -> DgTab("↺ $q", false, { p.onRecentQuerySelected(q) }, Modifier.fillMaxWidth()) }
                item { DgTab(tr("Clear", "مسح"), false, p.onClearRecentQueries, Modifier.fillMaxWidth()) }
            }
        }
    }
    LazyColumn(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(bottom = 48.dp)) {
        when {
            p.query.isBlank() -> item { DgEmpty(tr("Type to search", "اكتب للبحث")) }
            s.isLoading -> item { DgEmpty(tr("Searching…", "جار البحث…")) }
            s.hasSearchError -> item { DgEmpty(tr("Search failed. Try again.", "فشل البحث. حاول مرة أخرى.")) }
            s.isEmpty -> item { DgEmpty(tr("No results for", "لا نتائج لـ") + " “${p.query}”") }
            else -> {
                if (s.channels.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.LIVE)) item {
                    Column {
                        DgRowTitle(tr("Live channels", "قنوات مباشرة"), trailing = "${s.channels.size}")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                            items(s.channels, key = { it.id }) { c ->
                                val locked = p.isChannelLocked(c)
                                DgCard(onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c) }, modifier = Modifier.width(240.dp).height(110.dp), container = DG.Raised) {
                                    Row(Modifier.fillMaxSize().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        DgLogo(c.name, if (locked) null else c.logoUrl, 54.dp)
                                        Column(Modifier.weight(1f)) {
                                            Text(c.name, color = DG.Text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(if (locked) "🔒" else c.currentProgram?.title.orEmpty(), color = DG.Faint, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) {
                                                if (c.id in p.recordingChannelIds) DgBadge("REC", DG.Live, filled = true)
                                                else if (c.id in p.scheduledChannelIds) DgBadge("◷", DG.Blue)
                                                c.qualityBadge()?.let { DgBadge(it, DG.Text) }
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
                        DgRowTitle(tr("Movies", "أفلام"), trailing = "${s.movies.size}")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                            items(s.movies, key = { it.id }) { m -> val l = p.isMovieLocked(m); DgPoster(m.name, if (l) null else m.posterUrl, m.year, { p.onMovieClick(m) }, Modifier.width(140.dp), locked = l, onLongClick = { p.onMovieLongClick(m) }) }
                        }
                    }
                }
                if (s.series.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.SERIES)) item {
                    Column {
                        DgRowTitle(tr("Series", "مسلسلات"), trailing = "${s.series.size}")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                            items(s.series, key = { it.id }) { m -> val l = p.isSeriesLocked(m); DgPoster(m.name, if (l) null else m.posterUrl, m.genre, { p.onSeriesClick(m) }, Modifier.width(140.dp), locked = l, onLongClick = { p.onSeriesLongClick(m) }) }
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

/** My List: preset tabs across the top, filter/sort pills, then a single dense poster grid with section headers. */
@Composable
internal fun DarkGlassFavorites(p: FavoritesParams) {
  Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
    Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            items(SavedLibraryFilter.entries) { f -> DgTab(f.name.lowercase().replaceFirstChar { it.uppercase() }, f == p.selectedFilter, { p.onFilterSelected(f) }) }
            item { Text("   ⇅", color = DG.Faint, fontSize = 14.sp) }
            items(SavedLibrarySort.entries) { o -> DgTab(o.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }, o == p.selectedSort, { p.onSortSelected(o) }) }
        }
        LazyVerticalGrid(GridCells.Adaptive(150.dp), Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(18.dp), verticalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(6.dp, 6.dp, 6.dp, 48.dp)) {
            if (p.continueWatching.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) { DgRowTitle(tr("Continue watching", "متابعة المشاهدة")) }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(p.continueWatching, key = { "c${it.history.id}" }) { h ->
                            val prog = if (h.history.totalDurationMs > 0) h.history.resumePositionMs.toFloat() / h.history.totalDurationMs else null
                            DgWide(h.title, h.history.posterUrl, h.subtitle, prog, { p.onHistoryClick(h) }, Modifier.width(260.dp))
                        }
                    }
                }
            }
            if (p.recentLive.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) { DgRowTitle(tr("Recently watched live", "مباشر شوهد مؤخراً")) }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(p.recentLive, key = { "l${it.history.id}" }) { h ->
                            DgCard(onClick = { p.onHistoryClick(h) }, modifier = Modifier.width(200.dp), container = DG.Raised) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    DgLogo(h.title, h.history.posterUrl, 40.dp)
                                    Column { Text(h.title, color = DG.Text, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(h.subtitle, color = DG.Faint, fontSize = 11.sp, maxLines = 1) }
                                }
                            }
                        }
                    }
                }
            }
            val sections = p.sections.filter { it.items.isNotEmpty() }
            if (sections.isEmpty() && p.continueWatching.isEmpty() && p.recentLive.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
                DgEmpty(tr("Your list is empty. Long-press any title to add it.", "قائمتك فارغة. اضغط مطولاً على أي عنصر لإضافته."))
            }
            sections.forEach { section ->
                item(key = "h${section.key}", span = { GridItemSpan(maxLineSpan) }) { DgRowTitle(section.title, trailing = "${section.items.size}") }
                items(section.items, key = { "${section.key}${it.favorite.contentType}${it.favorite.contentId}" }) { f ->
                    DgPoster(f.title, null, f.subtitle, { p.onItemClick(f) }, onLongClick = { p.onItemLongClick(f) })
                }
            }
        }
    }
    Column(Modifier.width(220.dp).fillMaxHeight().dgGlass(DG.R, 0.06f).padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(tr("My List", "قائمتي"), color = DG.Text, fontSize = 24.sp, fontWeight = FontWeight.Thin, modifier = Modifier.padding(6.dp))
        SavedLibraryPreset.entries.forEach { pr -> DgTab(presetLabel(pr), pr == p.selectedPreset, { p.onPresetSelected(pr) }, Modifier.fillMaxWidth()) }
    }
  }
}

/** Settings: large rounded category cards stacked on the start side (icon in an amber circle). */
@Composable
internal fun DarkGlassSettingsNav(p: SettingsNavParams) {
    LazyColumn(Modifier.width(300.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(6.dp), contentPadding = PaddingValues(vertical = 8.dp, horizontal = 6.dp)) {
        item { Text(tr("Settings", "الإعدادات"), color = DG.Text, fontSize = 28.sp, fontWeight = FontWeight.Thin, modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)) }
        items(p.entries.size) { i ->
            val (label, icon) = p.entries[i]
            val sel = i == p.selectedCategory
            DgCard(
                onClick = { p.onCategorySelected(i) }, zoom = 1.03f, shape = DG.Pill, container = if (sel) Color(0x33B69CFF) else Color.Transparent,
                modifier = Modifier.fillMaxWidth().then(if (sel) Modifier.focusRequester(p.focusRequester) else Modifier)
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(Modifier.size(36.dp).dgGlass(DG.Pill, if (sel) 0.2f else 0.05f), contentAlignment = Alignment.Center) { Text(icon, fontSize = 16.sp, color = DG.Text) }
                    Text(label, color = DG.Text, fontSize = 16.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium, maxLines = 1, modifier = Modifier.weight(1f))
                    if (sel) Text("›", color = DG.Amber, fontSize = 20.sp)
                }
            }
        }
    }
}

@Composable
internal fun DarkGlassSettingsFrame(navigation: @Composable () -> Unit, content: @Composable () -> Unit) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        Box(Modifier.fillMaxHeight().dgGlass(DG.R, 0.06f).padding(8.dp)) { navigation() }
        Box(Modifier.weight(1f).fillMaxHeight().dgGlass(DG.R, 0.04f).padding(24.dp)) { content() }
    }
}
