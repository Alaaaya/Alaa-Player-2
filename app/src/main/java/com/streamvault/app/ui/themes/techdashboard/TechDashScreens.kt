package com.streamvault.app.ui.themes.techdashboard

import com.streamvault.app.ui.themes.bespoke.tr
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
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
import com.streamvault.app.ui.components.SearchInput
import com.streamvault.app.ui.screens.favorites.SavedLibraryFilter
import com.streamvault.app.ui.screens.favorites.SavedLibraryPreset
import com.streamvault.app.ui.screens.favorites.SavedLibrarySort
import com.streamvault.app.ui.screens.search.SearchTab
import com.streamvault.app.ui.themes.bespoke.FavoritesParams
import com.streamvault.app.ui.themes.bespoke.MovieDetailParams
import com.streamvault.app.ui.themes.bespoke.SearchParams
import com.streamvault.app.ui.themes.bespoke.SeriesDetailParams
import com.streamvault.app.ui.themes.bespoke.SettingsNavParams

@Composable
private fun SpecRow(k: String, v: String?) {
    if (v.isNullOrBlank()) return
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(k.uppercase(), color = TD.Muted, fontSize = 11.sp, fontFamily = TD.Mono, modifier = Modifier.width(96.dp))
        Text(v, color = TD.Star, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

/** Vertical command list: "> verb" rows, the dashboard's action idiom. */
@Composable
private fun CommandRow(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, primary: Boolean = false) {
    TechDashSurface(onClick = onClick, shape = TD.Pill, container = if (primary) TD.Plasma else Color.Transparent, modifier = modifier.fillMaxWidth()) {
        Text("> $label", color = if (primary) TD.Void else TD.Star, fontSize = 13.sp, fontFamily = TD.Mono, fontWeight = if (primary) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp))
    }
}

/** Movie detail as a spec sheet: header strip, three modules (artwork, spec table, commands), related row. */
@Composable
internal fun TechDashMovieDetail(p: MovieDetailParams) {
    val m = p.movie
    val play = remember { FocusRequester() }
    LaunchedEffect(m.id) { runCatching { play.requestFocus() } }
    TechDashBackdrop {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(36.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("FILM://${m.id}  ", color = TD.Muted, fontSize = 11.sp, fontFamily = TD.Mono)
                    TechDashTitle(m.name, size = 30, modifier = Modifier.weight(1f))
                    if (m.rating > 0) Text("RTG %.1f".format(m.rating), color = TD.Flare, fontSize = 18.sp, fontFamily = TD.Mono, fontWeight = FontWeight.Bold)
                }
            }
            item {
                Row(Modifier.fillMaxWidth().height(380.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    TdModule(tr("Artwork", "الصورة"), Modifier.width(250.dp).fillMaxHeight()) {
                        Box(Modifier.fillMaxSize().background(TD.Nebula)) {
                            (m.backdropUrl ?: m.posterUrl)?.let { AsyncImage(m.posterUrl ?: it, m.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                        }
                    }
                    TdModule(tr("Spec", "التفاصيل"), Modifier.weight(1f).fillMaxHeight()) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            SpecRow(tr("Year", "السنة"), m.year)
                            SpecRow(tr("Runtime", "المدة"), m.duration)
                            SpecRow(tr("Genre", "النوع"), m.genre)
                            SpecRow(tr("Version", "النسخة"), m.variantLabel)
                            SpecRow(tr("Director", "المخرج"), m.director)
                            SpecRow(tr("Cast", "الممثلون"), m.cast)
                            if (p.hasResume) SpecRow(tr("Resume", "استكمال"), formatDuration(p.resumePositionMs))
                            Spacer(Modifier.height(8.dp))
                            Text(m.plot.orEmpty(), color = TD.Dust, fontSize = 13.sp, maxLines = 6, overflow = TextOverflow.Ellipsis)
                            if (m.variants.size > 1) LazyRow(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(m.variants) { v -> TechDashChip(v.label, v.rawMovieId == m.selectedVariantId, { p.onSelectVariant(v.rawMovieId) }) }
                            }
                        }
                    }
                    TdModule(tr("Commands", "الأوامر"), Modifier.width(240.dp).fillMaxHeight()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            CommandRow(if (p.hasResume) tr("resume", "استكمال") else tr("play", "تشغيل"), p.onPlay, Modifier.focusRequester(play), primary = true)
                            p.onPlayTrailer?.let { CommandRow(tr("trailer", "الإعلان"), it) }
                            CommandRow(if (m.isFavorite) tr("unpin ★", "إزالة من المفضلة") else tr("pin ☆", "أضف للمفضلة"), p.onToggleFavorite)
                            CommandRow(if (p.isCasting) "casting…" else tr("cast", "بث"), p.onCast)
                            CommandRow(tr("download", "تنزيل"), p.onDownload)
                            CommandRow(tr("copy url", "نسخ الرابط"), p.onCopyUrl)
                            CommandRow(tr("back", "رجوع"), p.onBack)
                        }
                    }
                }
            }
            if (p.relatedContent.isNotEmpty()) item {
                TdModule(tr("Related films", "أفلام مشابهة"), meta = "${p.relatedContent.size}") {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(p.relatedContent, key = { it.id }) { r -> TechDashPoster(r.name, r.posterUrl, r.year, { p.onRelatedClick(r) }, Modifier.width(128.dp)) }
                    }
                }
            }
        }
    }
}

/** Series detail: season index rail | episode table | info module. */
@Composable
internal fun TechDashSeriesDetail(p: SeriesDetailParams) {
    val s = p.series
    val primary = remember { FocusRequester() }
    LaunchedEffect(s.id) { runCatching { primary.requestFocus() } }
    TechDashBackdrop {
        Column(Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("SERIES://${s.id}  ", color = TD.Muted, fontSize = 11.sp, fontFamily = TD.Mono)
                TechDashTitle(s.name, size = 28, modifier = Modifier.weight(1f))
                if (p.unwatchedEpisodeCount > 0) TechDashBadge("${p.unwatchedEpisodeCount} NEW", TD.Flare)
            }
            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                TdModule(tr("Seasons", "المواسم"), Modifier.width(170.dp).fillMaxHeight(), meta = "${s.seasons.size}") {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        items(s.seasons, key = { it.seasonNumber }) { season ->
                            val sel = season.seasonNumber == p.selectedSeason?.seasonNumber
                            TechDashSurface(onClick = { p.onSeasonSelected(season) }, shape = TD.Pill, container = if (sel) TD.Plasma.copy(alpha = 0.3f) else Color.Transparent, modifier = Modifier.fillMaxWidth()) {
                                Text("S%02d ".format(season.seasonNumber) + season.name.ifBlank { "" }, color = if (sel) TD.Plasma else TD.Star, fontSize = 12.sp, fontFamily = TD.Mono, maxLines = 1, modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp))
                            }
                        }
                    }
                }
                TdModule(tr("Episodes", "الحلقات"), Modifier.weight(1f).fillMaxHeight(), meta = "${p.selectedSeason?.episodes?.size ?: 0} rows") {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                        items(p.selectedSeason?.episodes.orEmpty(), key = { it.id }) { e ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TechDashSurface(onClick = { p.onEpisodeClick(e) }, shape = TD.Pill, container = Color.Transparent, modifier = Modifier.weight(1f)) {
                                    Row(Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text("S%02dE%02d".format(e.seasonNumber, e.episodeNumber), color = TD.Plasma, fontSize = 11.sp, fontFamily = TD.Mono)
                                        Box(Modifier.width(80.dp).height(45.dp).background(TD.Nebula)) { e.coverUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) } }
                                        Column(Modifier.weight(1f)) {
                                            Text(e.title, color = TD.Star, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                            if (e.watchProgress > 0 && e.durationSeconds > 0) TechDashProgress(e.watchProgress / (e.durationSeconds * 1000f), height = 2.dp)
                                            else Text(e.plot.orEmpty(), color = TD.Muted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                        e.duration?.let { Text(it, color = TD.Muted, fontSize = 11.sp, fontFamily = TD.Mono) }
                                    }
                                }
                                TechDashChip("CAST", false, { p.onCastEpisode(e) })
                                TechDashChip("DL", false, { p.onDownloadEpisode(e) })
                                TechDashChip("URL", false, { p.onCopyEpisodeUrl(e) })
                            }
                        }
                    }
                }
                TdModule(tr("Info", "معلومات"), Modifier.width(300.dp).fillMaxHeight()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.fillMaxWidth().height(150.dp).background(TD.Nebula)) { (s.backdropUrl ?: s.posterUrl)?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) } }
                        val resume = p.resumeEpisode
                        CommandRow(
                            if (resume != null) "play S%02dE%02d".format(resume.seasonNumber, resume.episodeNumber) else tr("play", "تشغيل"),
                            { (resume ?: p.selectedSeason?.episodes?.firstOrNull())?.let(p.onResumeClick) }, Modifier.focusRequester(primary), primary = true
                        )
                        CommandRow(if (s.isFavorite) tr("unpin ★", "إزالة من المفضلة") else tr("pin ☆", "أضف للمفضلة"), p.onToggleFavorite)
                        if (resume != null) CommandRow(if (p.isCasting) "casting…" else tr("cast", "بث"), p.onCastResumeEpisode)
                        CommandRow(tr("back", "رجوع"), p.onBack)
                        SpecRow(tr("Released", "الإصدار"), s.releaseDate)
                        SpecRow(tr("Genre", "النوع"), s.genre)
                        if (s.rating > 0) SpecRow(tr("Rating", "التقييم"), "%.1f".format(s.rating))
                        if (!p.isLoadingExternalRatings) {
                            val r = p.externalRatings
                            listOf("IMDb" to r.imdb, "RT" to r.rottenTomatoes, "MC" to r.metacritic).filter { it.second.available }.forEach { SpecRow(it.first, it.second.displayValue) }
                        }
                        if (s.variants.size > 1) LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(s.variants) { v -> TechDashChip(v.label, v.rawSeriesId == s.selectedVariantId, { p.onSelectVariant(v.rawSeriesId) }) }
                        }
                        Text(s.plot.orEmpty(), color = TD.Dust, fontSize = 12.sp, maxLines = 5, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

/** Search as a terminal: prompt line, segmented scope, results split into three table modules. */
@Composable
internal fun TechDashSearch(p: SearchParams) {
    LaunchedEffect(Unit) { runCatching { p.searchFocusRequester.requestFocus() } }
    val s = p.uiState
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("$ find", color = TD.Plasma, fontSize = 16.sp, fontFamily = TD.Mono, fontWeight = FontWeight.Bold)
            SearchInput(p.query, p.onQueryChange, tr("channels, films, series", "قنوات، أفلام، مسلسلات"), Modifier.weight(1f), focusRequester = p.searchFocusRequester, onSearch = p.onSearch)
            SearchTab.entries.forEach { t -> TechDashChip("--" + t.name.lowercase(), t == p.selectedTab, { p.onTabSelected(t) }) }
            TechDashChip(tr("reindex", "فهرسة كاملة"), false, p.onBuildCompleteIndex)
        }
        when {
            p.query.isBlank() && p.recentQueries.isNotEmpty() -> TdModule(tr("Query history", "عمليات البحث الأخيرة"), Modifier.width(420.dp)) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    items(p.recentQueries) { q -> CommandRow(q, { p.onRecentQuerySelected(q) }) }
                    item { CommandRow(tr("clear history", "مسح السجل"), p.onClearRecentQueries) }
                }
            }
            s.isLoading -> TechDashEmpty("searching…")
            s.hasSearchError -> TechDashEmpty("ERR: search failed")
            s.isEmpty -> TechDashEmpty("0 rows for “${p.query}”")
            else -> Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (s.channels.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.LIVE)) TdModule(tr("Live", "مباشر"), Modifier.weight(1f).fillMaxHeight(), meta = "${s.channels.size}") {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        items(s.channels, key = { it.id }) { c ->
                            TdChannelRow(c, p.isChannelLocked(c), { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c) }, recording = c.id in p.recordingChannelIds)
                        }
                    }
                }
                if (s.movies.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.MOVIES)) TdModule(tr("Films", "أفلام"), Modifier.weight(1f).fillMaxHeight(), meta = "${s.movies.size}") {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        items(s.movies, key = { it.id }) { m -> ResultRow(m.name, if (p.isMovieLocked(m)) null else m.posterUrl, m.year, p.isMovieLocked(m), { p.onMovieClick(m) }, { p.onMovieLongClick(m) }) }
                    }
                }
                if (s.series.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.SERIES)) TdModule(tr("Series", "مسلسلات"), Modifier.weight(1f).fillMaxHeight(), meta = "${s.series.size}") {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        items(s.series, key = { it.id }) { m -> ResultRow(m.name, if (p.isSeriesLocked(m)) null else m.posterUrl, m.genre, p.isSeriesLocked(m), { p.onSeriesClick(m) }, { p.onSeriesLongClick(m) }) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultRow(title: String, image: String?, caption: String?, locked: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    TechDashSurface(onClick = onClick, onLongClick = onLongClick, shape = TD.Pill, container = Color.Transparent, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.width(40.dp).height(58.dp).background(TD.Nebula), contentAlignment = Alignment.Center) {
                if (locked) Text("🔒") else image?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
            }
            Column(Modifier.weight(1f)) {
                Text(title, color = TD.Star, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("> " + (caption?.takeIf { it.isNotBlank() } ?: "--"), color = TD.Plasma, fontSize = 11.sp, fontFamily = TD.Mono, maxLines = 1)
            }
        }
    }
}

private val presetLabels = mapOf(
    SavedLibraryPreset.ALL_SAVED to "all", SavedLibraryPreset.HOME_SHELF to "home", SavedLibraryPreset.WATCH_NEXT to "next",
    SavedLibraryPreset.LIVE_RECALL to "live", SavedLibraryPreset.MOVIES to "films", SavedLibraryPreset.SERIES to "series", SavedLibraryPreset.CUSTOM_GROUPS to "groups"
)

/** Favorites: preset rail on the start side, filters as a toolbar, saved sections as a two-column module grid. */
@Composable
internal fun TechDashFavorites(p: FavoritesParams) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        TdModule(tr("Views", "العرض"), Modifier.width(180.dp).fillMaxHeight()) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                SavedLibraryPreset.entries.forEach { pr ->
                    val sel = pr == p.selectedPreset
                    TechDashSurface(onClick = { p.onPresetSelected(pr) }, shape = TD.Pill, container = if (sel) TD.Plasma else Color.Transparent, modifier = Modifier.fillMaxWidth()) {
                        Text("/" + (presetLabels[pr] ?: pr.name.lowercase()), color = if (sel) TD.Void else TD.Star, fontSize = 13.sp, fontFamily = TD.Mono, modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp))
                    }
                }
            }
        }
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 40.dp)) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("FILTER", color = TD.Muted, fontSize = 11.sp, fontFamily = TD.Mono)
                    SavedLibraryFilter.entries.forEach { f -> TechDashChip(f.name.lowercase(), f == p.selectedFilter, { p.onFilterSelected(f) }) }
                    Spacer(Modifier.width(12.dp))
                    Text("SORT", color = TD.Muted, fontSize = 11.sp, fontFamily = TD.Mono)
                    SavedLibrarySort.entries.forEach { o -> TechDashChip(o.name.lowercase(), o == p.selectedSort, { p.onSortSelected(o) }) }
                }
            }
            if (p.continueWatching.isNotEmpty() || p.recentLive.isNotEmpty()) item {
                Row(Modifier.fillMaxWidth().height(230.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (p.continueWatching.isNotEmpty()) TdModule(tr("Open sessions", "متابعة المشاهدة"), Modifier.weight(1f).fillMaxHeight(), meta = "${p.continueWatching.size}") {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            items(p.continueWatching, key = { "c${it.history.id}" }) { h ->
                                TechDashSurface(onClick = { p.onHistoryClick(h) }, shape = TD.Pill, container = Color.Transparent, modifier = Modifier.fillMaxWidth()) {
                                    Column(Modifier.padding(horizontal = 8.dp, vertical = 5.dp)) {
                                        Text(h.title, color = TD.Star, fontSize = 13.sp, maxLines = 1)
                                        if (h.history.totalDurationMs > 0) TechDashProgress(h.history.resumePositionMs.toFloat() / h.history.totalDurationMs, height = 3.dp)
                                    }
                                }
                            }
                        }
                    }
                    if (p.recentLive.isNotEmpty()) TdModule(tr("Live recall", "آخر القنوات"), Modifier.weight(1f).fillMaxHeight(), meta = "${p.recentLive.size}") {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            items(p.recentLive, key = { "l${it.history.id}" }) { h ->
                                TechDashSurface(onClick = { p.onHistoryClick(h) }, shape = TD.Pill, container = Color.Transparent, modifier = Modifier.fillMaxWidth()) {
                                    Row(Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        TechDashLogo(h.title, h.history.posterUrl, 30.dp)
                                        Column { Text(h.title, color = TD.Star, fontSize = 13.sp, maxLines = 1); Text(h.subtitle, color = TD.Muted, fontSize = 11.sp, maxLines = 1) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (p.sections.all { it.items.isEmpty() } && p.continueWatching.isEmpty() && p.recentLive.isEmpty()) item { TechDashEmpty(tr("Nothing pinned yet. Long-press anything to save it.", "لا يوجد شيء محفوظ. اضغط مطولاً لحفظ أي عنصر.")) }
            p.sections.filter { it.items.isNotEmpty() }.forEach { section ->
                item(key = section.key) {
                    TdModule(section.title, meta = "${section.items.size} rows") {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(section.items, key = { "${section.key}${it.favorite.contentType}${it.favorite.contentId}" }) { f ->
                                TechDashPoster(f.title, null, f.subtitle, { p.onItemClick(f) }, Modifier.width(128.dp), onLongClick = { p.onItemLongClick(f) })
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Settings rail: numbered config index with a mono path header. */
@Composable
internal fun TechDashSettingsNav(p: SettingsNavParams) {
    LazyColumn(Modifier.width(250.dp).fillMaxHeight().padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        item { Text("~/config", color = TD.Plasma, fontSize = 13.sp, fontFamily = TD.Mono, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)) }
        items(p.entries.size) { i ->
            val (label, icon) = p.entries[i]
            val selected = i == p.selectedCategory
            TechDashSurface(
                onClick = { p.onCategorySelected(i) }, shape = TD.Pill, container = if (selected) TD.Plasma else Color.Transparent,
                modifier = Modifier.fillMaxWidth().then(if (selected) Modifier.focusRequester(p.focusRequester) else Modifier)
            ) {
                Row(Modifier.padding(horizontal = 10.dp, vertical = 9.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("%02d".format(i + 1), color = if (selected) TD.Void else TD.Muted, fontSize = 11.sp, fontFamily = TD.Mono)
                    Text(icon, fontSize = 14.sp, color = if (selected) TD.Void else TD.Dust)
                    Text(label, color = if (selected) TD.Void else TD.Star, fontSize = 14.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, maxLines = 1)
                }
            }
        }
    }
}

@Composable
internal fun TechDashSettingsFrame(navigation: @Composable () -> Unit, content: @Composable () -> Unit) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(Modifier.fillMaxHeight().clip(TD.Panel).background(TD.Deep.copy(alpha = 0.85f)).border(1.dp, TD.Plasma.copy(alpha = 0.22f), TD.Panel)) { navigation() }
        TdModule(tr("Configuration", "الإعدادات"), Modifier.weight(1f).fillMaxHeight(), meta = "rw") { content() }
    }
}
