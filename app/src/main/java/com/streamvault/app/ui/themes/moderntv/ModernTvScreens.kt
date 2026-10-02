package com.streamvault.app.ui.themes.moderntv

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
private fun MtBackdrop(url: String?, content: @Composable BoxScope.() -> Unit) {
    MtBackground {
        url?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(MT.Bg, MT.Bg.copy(alpha = 0.85f), MT.Bg.copy(alpha = 0.2f)))))
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, MT.Bg))))
        content()
    }
}

@Composable
private fun Meta(vararg parts: String?) {
    Text(parts.filterNot { it.isNullOrBlank() }.joinToString("  •  "), color = MT.Sub, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

/** Movie: hero over backdrop with title, meta line, plot and a row of pill actions; versions + "More like this" below. */
@Composable
internal fun ModernTvMovieDetail(p: MovieDetailParams) {
    val m = p.movie
    val play = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(m.id) { runCatching { play.requestFocus() } }
    MtBackdrop(m.backdropUrl ?: m.posterUrl) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 64.dp, end = 64.dp, top = 80.dp, bottom = 48.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Column(Modifier.widthIn(max = 640.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(m.name, color = MT.Text, fontSize = 46.sp, fontWeight = FontWeight.Black, maxLines = 2, lineHeight = 50.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (m.rating > 0) MtBadge("★ %.1f".format(m.rating), MT.Amber, filled = true)
                        Meta(m.year, m.duration, m.genre, m.variantLabel)
                    }
                    Text(m.plot.orEmpty(), color = MT.Sub, fontSize = 15.sp, maxLines = 4, overflow = TextOverflow.Ellipsis, lineHeight = 22.sp)
                    m.director?.takeIf { it.isNotBlank() }?.let { Text(tr("Director", "المخرج") + "  $it", color = MT.Faint, fontSize = 13.sp, maxLines = 1) }
                    m.cast?.takeIf { it.isNotBlank() }?.let { Text(tr("Starring", "بطولة") + "  $it", color = MT.Faint, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    if (p.hasResume) Text(tr("Resume from", "استكمال من") + " " + mtDuration(p.resumePositionMs), color = MT.Amber, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                    item { MtButton(if (p.hasResume) tr("Resume", "استكمال") else tr("Play", "تشغيل"), p.onPlay, Modifier.focusRequester(play), primary = true, icon = "▶") }
                    p.onPlayTrailer?.let { item { MtButton(tr("Trailer", "الإعلان"), it, icon = "▷") } }
                    item { MtButton(if (m.isFavorite) tr("In My List", "في قائمتي") else tr("My List", "قائمتي"), p.onToggleFavorite, icon = if (m.isFavorite) "✓" else "+") }
                    item { MtButton(if (p.isCasting) tr("Casting…", "جار البث…") else tr("Cast", "بث"), p.onCast, icon = "⎚") }
                    item { MtButton(tr("Download", "تنزيل"), p.onDownload, icon = "↓") }
                    item { MtButton(tr("Copy link", "نسخ الرابط"), p.onCopyUrl, icon = "⧉") }
                    item { MtButton(tr("Back", "رجوع"), p.onBack, icon = "←") }
                }
            }
            if (m.variants.size > 1) item {
                Column {
                    MtRowTitle(tr("Versions", "النسخ"))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(m.variants) { v -> MtTab(v.label, v.rawMovieId == m.selectedVariantId, { p.onSelectVariant(v.rawMovieId) }) }
                    }
                }
            }
            if (p.relatedContent.isNotEmpty()) item {
                Column {
                    MtRowTitle(tr("More like this", "مشابه"))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                        items(p.relatedContent, key = { it.id }) { r -> MtPoster(r.name, r.posterUrl, r.year, { p.onRelatedClick(r) }, Modifier.width(140.dp)) }
                    }
                }
            }
        }
    }
}

/** Series: backdrop hero with play/resume, season pills, landscape episode cards in a horizontal shelf + episode list. */
@Composable
internal fun ModernTvSeriesDetail(p: SeriesDetailParams) {
    val s = p.series
    val primary = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(s.id) { runCatching { primary.requestFocus() } }
    MtBackdrop(s.backdropUrl ?: s.posterUrl) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 64.dp, end = 64.dp, top = 64.dp, bottom = 48.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Column(Modifier.widthIn(max = 640.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(s.name, color = MT.Text, fontSize = 42.sp, fontWeight = FontWeight.Black, maxLines = 2)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (s.rating > 0) MtBadge("★ %.1f".format(s.rating), MT.Amber, filled = true)
                        if (p.unwatchedEpisodeCount > 0) MtBadge("${p.unwatchedEpisodeCount} " + tr("new", "جديد"), MT.Blue)
                        Meta(s.releaseDate, s.genre, "${s.seasons.size} " + tr("seasons", "مواسم"))
                    }
                    if (!p.isLoadingExternalRatings) {
                        val r = p.externalRatings
                        val ext = listOf("IMDb" to r.imdb, "Rotten Tomatoes" to r.rottenTomatoes, "Metacritic" to r.metacritic).filter { it.second.available }
                        if (ext.isNotEmpty()) Text(ext.joinToString("   ") { "${it.first} ${it.second.displayValue}" }, color = MT.Faint, fontSize = 13.sp)
                    }
                    Text(s.plot.orEmpty(), color = MT.Sub, fontSize = 15.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                }
            }
            item {
                val resume = p.resumeEpisode
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MtButton(
                        if (resume != null) tr("Continue", "متابعة") + " S${resume.seasonNumber}:E${resume.episodeNumber}" else tr("Play", "تشغيل"),
                        { (resume ?: p.selectedSeason?.episodes?.firstOrNull())?.let(p.onResumeClick) }, Modifier.focusRequester(primary), primary = true, icon = "▶"
                    )
                    MtButton(if (s.isFavorite) tr("In My List", "في قائمتي") else tr("My List", "قائمتي"), p.onToggleFavorite, icon = if (s.isFavorite) "✓" else "+")
                    if (resume != null) MtButton(if (p.isCasting) tr("Casting…", "جار البث…") else tr("Cast", "بث"), p.onCastResumeEpisode, icon = "⎚")
                    MtButton(tr("Back", "رجوع"), p.onBack, icon = "←")
                }
            }
            if (s.variants.size > 1) item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(s.variants) { v -> MtTab(v.label, v.rawSeriesId == s.selectedVariantId, { p.onSelectVariant(v.rawSeriesId) }) } }
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 6.dp)) {
                    items(s.seasons, key = { it.seasonNumber }) { season ->
                        MtTab(season.name.ifBlank { tr("Season", "الموسم") + " ${season.seasonNumber}" }, season.seasonNumber == p.selectedSeason?.seasonNumber, { p.onSeasonSelected(season) })
                    }
                }
            }
            items(p.selectedSeason?.episodes.orEmpty(), key = { it.id }) { e ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MtCard(onClick = { p.onEpisodeClick(e) }, modifier = Modifier.weight(1f), container = MT.Raised.copy(alpha = 0.85f), zoom = 1.02f) {
                        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Box(Modifier.width(200.dp).aspectRatio(16f / 9f).clip(MT.RSmall).background(MT.Card)) {
                                e.coverUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                                Text("▶", color = MT.Text, fontSize = 22.sp, modifier = Modifier.align(Alignment.Center))
                                if (e.watchProgress > 0 && e.durationSeconds > 0) MtProgress(e.watchProgress / (e.durationSeconds * 1000f), Modifier.align(Alignment.BottomCenter).padding(8.dp), 3.dp)
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("${e.episodeNumber}. ${e.title}", color = MT.Text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                e.duration?.let { Text(it, color = MT.Faint, fontSize = 12.sp) }
                                Text(e.plot.orEmpty(), color = MT.Sub, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        MtRound("⎚", { p.onCastEpisode(e) }, size = 40.dp)
                        MtRound("↓", { p.onDownloadEpisode(e) }, size = 40.dp)
                        MtRound("⧉", { p.onCopyEpisodeUrl(e) }, size = 40.dp)
                    }
                }
            }
        }
    }
}

/** Search: centered big field, recent searches as pills, results as titled shelves (channels wide, posters tall). */
@Composable
internal fun ModernTvSearch(p: SearchParams) {
    LaunchedEffect(Unit) { runCatching { p.searchFocusRequester.requestFocus() } }
    val s = p.uiState
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(22.dp), contentPadding = PaddingValues(bottom = 48.dp)) {
        item {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                MtSearchField(p.query, p.onQueryChange, tr("Search channels, movies and series", "ابحث عن قنوات وأفلام ومسلسلات"), Modifier.fillMaxWidth(0.6f).focusRequester(p.searchFocusRequester), onSubmit = p.onSearch)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SearchTab.entries.forEach { t ->
                        val label = when (t.name) { "ALL" -> tr("All", "الكل"); "LIVE" -> tr("Live", "مباشر"); "MOVIES" -> tr("Movies", "أفلام"); "SERIES" -> tr("Series", "مسلسلات"); else -> t.name }
                        MtTab(label, t == p.selectedTab, { p.onTabSelected(t) })
                    }
                    MtTab("↻ " + tr("Full index", "فهرسة كاملة"), false, p.onBuildCompleteIndex)
                }
            }
        }
        when {
            p.query.isBlank() -> if (p.recentQueries.isNotEmpty()) item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    MtRowTitle(tr("Recent searches", "عمليات البحث الأخيرة"))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(p.recentQueries) { q -> MtTab("↺ $q", false, { p.onRecentQuerySelected(q) }) }
                        item { MtTab(tr("Clear", "مسح"), false, p.onClearRecentQueries) }
                    }
                }
            }
            s.isLoading -> item { MtEmpty(tr("Searching…", "جار البحث…")) }
            s.hasSearchError -> item { MtEmpty(tr("Search failed. Try again.", "فشل البحث. حاول مرة أخرى.")) }
            s.isEmpty -> item { MtEmpty(tr("No results for", "لا نتائج لـ") + " “${p.query}”") }
            else -> {
                if (s.channels.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.LIVE)) item {
                    Column {
                        MtRowTitle(tr("Live channels", "قنوات مباشرة"), trailing = "${s.channels.size}")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                            items(s.channels, key = { it.id }) { c ->
                                val locked = p.isChannelLocked(c)
                                MtCard(onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c) }, modifier = Modifier.width(240.dp).height(110.dp), container = MT.Raised) {
                                    Row(Modifier.fillMaxSize().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        MtLogo(c.name, if (locked) null else c.logoUrl, 54.dp)
                                        Column(Modifier.weight(1f)) {
                                            Text(c.name, color = MT.Text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(if (locked) "🔒" else c.currentProgram?.title.orEmpty(), color = MT.Faint, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) {
                                                if (c.id in p.recordingChannelIds) MtBadge("REC", MT.Live, filled = true)
                                                else if (c.id in p.scheduledChannelIds) MtBadge("◷", MT.Blue)
                                                c.qualityBadge()?.let { MtBadge(it, MT.Text) }
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
                        MtRowTitle(tr("Movies", "أفلام"), trailing = "${s.movies.size}")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                            items(s.movies, key = { it.id }) { m -> val l = p.isMovieLocked(m); MtPoster(m.name, if (l) null else m.posterUrl, m.year, { p.onMovieClick(m) }, Modifier.width(140.dp), locked = l, onLongClick = { p.onMovieLongClick(m) }) }
                        }
                    }
                }
                if (s.series.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.SERIES)) item {
                    Column {
                        MtRowTitle(tr("Series", "مسلسلات"), trailing = "${s.series.size}")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                            items(s.series, key = { it.id }) { m -> val l = p.isSeriesLocked(m); MtPoster(m.name, if (l) null else m.posterUrl, m.genre, { p.onSeriesClick(m) }, Modifier.width(140.dp), locked = l, onLongClick = { p.onSeriesLongClick(m) }) }
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
internal fun ModernTvFavorites(p: FavoritesParams) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(SavedLibraryPreset.entries) { pr -> MtTab(presetLabel(pr), pr == p.selectedPreset, { p.onPresetSelected(pr) }) }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            items(SavedLibraryFilter.entries) { f -> MtTab(f.name.lowercase().replaceFirstChar { it.uppercase() }, f == p.selectedFilter, { p.onFilterSelected(f) }) }
            item { Text("   ⇅", color = MT.Faint, fontSize = 14.sp) }
            items(SavedLibrarySort.entries) { o -> MtTab(o.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }, o == p.selectedSort, { p.onSortSelected(o) }) }
        }
        LazyVerticalGrid(GridCells.Adaptive(150.dp), Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(18.dp), verticalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(6.dp, 6.dp, 6.dp, 48.dp)) {
            if (p.continueWatching.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) { MtRowTitle(tr("Continue watching", "متابعة المشاهدة")) }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(p.continueWatching, key = { "c${it.history.id}" }) { h ->
                            val prog = if (h.history.totalDurationMs > 0) h.history.resumePositionMs.toFloat() / h.history.totalDurationMs else null
                            MtWide(h.title, h.history.posterUrl, h.subtitle, prog, { p.onHistoryClick(h) }, Modifier.width(260.dp))
                        }
                    }
                }
            }
            if (p.recentLive.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) { MtRowTitle(tr("Recently watched live", "مباشر شوهد مؤخراً")) }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(p.recentLive, key = { "l${it.history.id}" }) { h ->
                            MtCard(onClick = { p.onHistoryClick(h) }, modifier = Modifier.width(200.dp), container = MT.Raised) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    MtLogo(h.title, h.history.posterUrl, 40.dp)
                                    Column { Text(h.title, color = MT.Text, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(h.subtitle, color = MT.Faint, fontSize = 11.sp, maxLines = 1) }
                                }
                            }
                        }
                    }
                }
            }
            val sections = p.sections.filter { it.items.isNotEmpty() }
            if (sections.isEmpty() && p.continueWatching.isEmpty() && p.recentLive.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
                MtEmpty(tr("Your list is empty. Long-press any title to add it.", "قائمتك فارغة. اضغط مطولاً على أي عنصر لإضافته."))
            }
            sections.forEach { section ->
                item(key = "h${section.key}", span = { GridItemSpan(maxLineSpan) }) { MtRowTitle(section.title, trailing = "${section.items.size}") }
                items(section.items, key = { "${section.key}${it.favorite.contentType}${it.favorite.contentId}" }) { f ->
                    MtPoster(f.title, null, f.subtitle, { p.onItemClick(f) }, onLongClick = { p.onItemLongClick(f) })
                }
            }
        }
    }
}

/** Settings: large rounded category cards stacked on the start side (icon in an amber circle). */
@Composable
internal fun ModernTvSettingsNav(p: SettingsNavParams) {
    LazyColumn(Modifier.width(300.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(6.dp), contentPadding = PaddingValues(vertical = 8.dp, horizontal = 6.dp)) {
        item { Text(tr("Settings", "الإعدادات"), color = MT.Text, fontSize = 28.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)) }
        items(p.entries.size) { i ->
            val (label, icon) = p.entries[i]
            val sel = i == p.selectedCategory
            MtCard(
                onClick = { p.onCategorySelected(i) }, zoom = 1.03f, container = if (sel) MT.Card else Color.Transparent, focusedContainer = MT.Card,
                modifier = Modifier.fillMaxWidth().then(if (sel) Modifier.focusRequester(p.focusRequester) else Modifier)
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(Modifier.size(36.dp).clip(MT.Pill).background(if (sel) MT.Amber else MT.Raised), contentAlignment = Alignment.Center) { Text(icon, fontSize = 16.sp, color = if (sel) MT.Bg else MT.Sub) }
                    Text(label, color = MT.Text, fontSize = 16.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium, maxLines = 1, modifier = Modifier.weight(1f))
                    if (sel) Text("›", color = MT.Amber, fontSize = 20.sp)
                }
            }
        }
    }
}

@Composable
internal fun ModernTvSettingsFrame(navigation: @Composable () -> Unit, content: @Composable () -> Unit) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
        navigation()
        Box(Modifier.weight(1f).fillMaxHeight().clip(MT.R).background(MT.Raised).padding(24.dp)) { content() }
    }
}
