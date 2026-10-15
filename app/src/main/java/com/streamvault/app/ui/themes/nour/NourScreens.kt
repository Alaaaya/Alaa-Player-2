package com.streamvault.app.ui.themes.nour

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

@Composable
private fun NourRoom(url: String?, content: @Composable BoxScope.() -> Unit) {
    NourBackground {
        url?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, alpha = 0.85f, modifier = Modifier.fillMaxSize()) }
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(NR.Bg.copy(alpha = 0.15f), NR.Bg.copy(alpha = 0.65f), NR.Bg))))
        content()
    }
}

@Composable
private fun Meta(vararg parts: String?) {
    Text(parts.filterNot { it.isNullOrBlank() }.joinToString("  ◈  "), color = NR.Sub, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

@Composable
private fun Framed(url: String?, title: String, width: androidx.compose.ui.unit.Dp) {
    Box(contentAlignment = Alignment.Center) {
        Box(Modifier.width(width + 60.dp).aspectRatio(2f / 3f).background(Brush.radialGradient(listOf(NR.Amber.copy(alpha = 0.28f), Color.Transparent))))
        Box(Modifier.width(width).aspectRatio(2f / 3f).clip(NR.R).background(Brush.linearGradient(listOf(NR.Rose.copy(alpha = 0.35f), NR.Line, NR.Blue.copy(alpha = 0.25f))))) {
            Text(title.take(1).uppercase(), color = NR.Sub, fontSize = 48.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.Center))
            url?.let { AsyncImage(it, title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
        }
    }
}

@Composable
internal fun NourMovieDetail(p: MovieDetailParams) {
    val m = p.movie
    val play = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(m.id) { runCatching { play.requestFocus() } }
    NourRoom(m.backdropUrl ?: m.posterUrl) {
        Row(Modifier.fillMaxSize().padding(horizontal = 56.dp, vertical = 40.dp), horizontalArrangement = Arrangement.spacedBy(44.dp)) {
            LazyColumn(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(bottom = 32.dp)) {
                item {
                    Column(Modifier.fillMaxWidth().clip(NR.R).background(Color.Transparent).padding(30.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(m.name, color = NR.Text, fontSize = 48.sp, fontWeight = FontWeight.Bold, fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif, maxLines = 2, lineHeight = 46.sp)
                        Box(Modifier.width(160.dp).height(1.dp).background(Brush.horizontalGradient(listOf(NR.Amber, Color.Transparent))))
                        Meta(m.year, m.duration, m.genre, m.variantLabel)
                        Text(m.plot.orEmpty(), color = NR.Sub, fontSize = 15.sp, maxLines = 5, overflow = TextOverflow.Ellipsis, lineHeight = 22.sp)
                        m.director?.takeIf { it.isNotBlank() }?.let { Text(tr("Director", "المخرج") + "  $it", color = NR.Faint, fontSize = 13.sp, maxLines = 1) }
                        m.cast?.takeIf { it.isNotBlank() }?.let { Text(tr("Starring", "بطولة") + "  $it", color = NR.Faint, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 6.dp)) {
                            NourButton(if (p.hasResume) tr("Resume", "استكمال") else tr("Play", "تشغيل"), p.onPlay, Modifier.focusRequester(play), primary = true, icon = "▶")
                            p.onPlayTrailer?.let { NourRound("▷", it, size = 48.dp) }
                            NourRound(if (m.isFavorite) "♥" else "♡", p.onToggleFavorite, size = 48.dp, active = m.isFavorite)
                            NourRound("⎚", p.onCast, size = 48.dp, active = p.isCasting)
                            NourRound("↓", p.onDownload, size = 48.dp)
                            NourRound("⧉", p.onCopyUrl, size = 48.dp)
                            NourRound("←", p.onBack, size = 48.dp)
                        }
                    }
                }
                m.cast?.takeIf { it.isNotBlank() }?.let { cast ->
                    item {
                        NourRowTitle(tr("Cast", "طاقم العمل"))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(24.dp), contentPadding = PaddingValues(8.dp)) {
                            items(cast.split(",", "،").map { it.trim() }.filter { it.isNotBlank() }.take(12)) { name ->
                                Column(Modifier.width(100.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(Modifier.size(66.dp).clip(androidx.compose.foundation.shape.CircleShape).background(NR.Line), contentAlignment = Alignment.Center) {
                                        Text(name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""), color = NR.Text, fontSize = 22.sp)
                                    }
                                    Text(name, color = NR.Sub, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
                if (m.variants.size > 1) item {
                    Column {
                        NourRowTitle(tr("Versions", "النسخ"))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(m.variants) { v -> NourTab(v.label, v.rawMovieId == m.selectedVariantId, { p.onSelectVariant(v.rawMovieId) }) }
                        }
                    }
                }
                if (p.relatedContent.isNotEmpty()) item {
                    Column {
                        NourRowTitle(tr("More like this", "محتوى مشابه"))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                            items(p.relatedContent, key = { it.id }) { r -> NourPoster(r.name, r.posterUrl, r.year, { p.onRelatedClick(r) }, Modifier.width(130.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun NourSeriesDetail(p: SeriesDetailParams) {
    val s = p.series
    val primary = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(s.id) { runCatching { primary.requestFocus() } }
    NourRoom(s.backdropUrl ?: s.posterUrl) {
        Column(Modifier.fillMaxSize().padding(horizontal = 56.dp, vertical = 32.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(Modifier.fillMaxWidth().clip(NR.R).background(NR.Raised.copy(alpha = 0.82f)).padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Box(Modifier.width(110.dp).aspectRatio(2f / 3f).clip(NR.RSmall).background(NR.Card)) { (s.posterUrl ?: s.backdropUrl)?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) } }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(s.name, color = NR.Text, fontSize = 34.sp, fontWeight = FontWeight.Bold, fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (s.rating > 0) NourBadge("★ %.1f".format(s.rating), NR.Amber, filled = true)
                        if (p.unwatchedEpisodeCount > 0) NourBadge("${p.unwatchedEpisodeCount} " + tr("new", "جديد"), NR.Blue)
                        Meta(s.releaseDate, s.genre, "${s.seasons.size} " + tr("seasons", "مواسم"))
                    }
                    if (!p.isLoadingExternalRatings) {
                        val r = p.externalRatings
                        val ext = listOf("IMDb" to r.imdb, "Rotten Tomatoes" to r.rottenTomatoes, "Metacritic" to r.metacritic).filter { it.second.available }
                        if (ext.isNotEmpty()) Text(ext.joinToString("   ") { "${it.first} ${it.second.displayValue}" }, color = NR.Faint, fontSize = 13.sp)
                    }
                    Text(s.plot.orEmpty(), color = NR.Sub, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val resume = p.resumeEpisode
                    NourButton(
                        if (resume != null) tr("Continue", "متابعة") + " S${resume.seasonNumber}:E${resume.episodeNumber}" else tr("Play", "تشغيل"),
                        { (resume ?: p.selectedSeason?.episodes?.firstOrNull())?.let(p.onResumeClick) }, Modifier.focusRequester(primary), primary = true, icon = "▶"
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NourRound(if (s.isFavorite) "♥" else "♡", p.onToggleFavorite, size = 44.dp, active = s.isFavorite)
                        if (resume != null) NourRound("⎚", p.onCastResumeEpisode, size = 44.dp, active = p.isCasting)
                        NourRound("←", p.onBack, size = 44.dp)
                    }
                }
            }
            if (s.variants.size > 1) LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(s.variants) { v -> NourTab(v.label, v.rawSeriesId == s.selectedVariantId, { p.onSelectVariant(v.rawSeriesId) }) } }
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 6.dp)) {
                    items(s.seasons, key = { it.seasonNumber }) { season ->
                        NourTab(season.name.ifBlank { tr("Season", "الموسم") + " ${season.seasonNumber}" },
                            season.seasonNumber == p.selectedSeason?.seasonNumber, { p.onSeasonSelected(season) })
                    }
                }
                LazyColumn(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 32.dp)) {
                    items(p.selectedSeason?.episodes.orEmpty(), key = { it.id }) { e ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            NourCard(onClick = { p.onEpisodeClick(e) }, modifier = Modifier.weight(1f), shape = NR.Pill, container = NR.Raised.copy(alpha = 0.8f), zoom = 1.02f) {
                                Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Box(Modifier.width(128.dp).height(72.dp).clip(NR.RSmall).background(NR.Card)) {
                                        e.coverUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                                        Text("${e.episodeNumber}", color = NR.Text, fontSize = 20.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.Center))
                                    }
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text(e.title, color = NR.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(listOfNotNull(e.duration, e.plot).joinToString("  ◈  "), color = NR.Faint, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        if (e.watchProgress > 0 && e.durationSeconds > 0) NourProgress(e.watchProgress / (e.durationSeconds * 1000f), Modifier.fillMaxWidth(0.6f), 3.dp)
                                    }
                                    Text("▶", color = NR.Amber, fontSize = 18.sp, modifier = Modifier.padding(end = 16.dp))
                                }
                            }
                            NourRound("⎚", { p.onCastEpisode(e) }, size = 38.dp)
                            NourRound("↓", { p.onDownloadEpisode(e) }, size = 38.dp)
                            NourRound("⧉", { p.onCopyEpisodeUrl(e) }, size = 38.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun NourSearch(p: SearchParams) {
    LaunchedEffect(Unit) { runCatching { p.searchFocusRequester.requestFocus() } }
    val s = p.uiState
    Row(Modifier.fillMaxSize().padding(horizontal = 36.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        LazyColumn(Modifier.width(340.dp).fillMaxHeight().clip(NR.R).background(NR.Raised.copy(alpha = 0.8f)), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(20.dp)) {
            item { Text(tr("Find something", "ابحث عن شيء"), color = NR.Text, fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif, modifier = Modifier.padding(bottom = 6.dp)) }
            item { NourSearchField(p.query, p.onQueryChange, tr("Channels, movies, series", "قنوات، أفلام، مسلسلات"), Modifier.fillMaxWidth().focusRequester(p.searchFocusRequester), onSubmit = p.onSearch) }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    listOf("ABCDEFG", "HIJKLMN", "OPQRSTU", "VWXYZ01", "23456789").forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            row.forEach { char ->
                                NourCard({ p.onQueryChange(p.query + char.lowercaseChar()) }, shape = NR.RSmall,
                                    modifier = Modifier.weight(1f).height(32.dp)) {
                                    Text(char.toString(), color = NR.Text, fontSize = 13.sp, modifier = Modifier.align(Alignment.Center))
                                }
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NourTab(tr("Space", "مسافة"), false, { p.onQueryChange(p.query + " ") }, Modifier.weight(1f))
                        NourTab("⌫", false, { p.onQueryChange(p.query.dropLast(1)) })
                        NourTab(tr("Search", "بحث"), false, p.onSearch)
                    }
                }
            }
            items(SearchTab.entries) { t ->
                val label = when (t.name) { "ALL" -> tr("Everything", "الكل"); "LIVE" -> tr("Live", "مباشر"); "MOVIES" -> tr("Movies", "أفلام"); "SERIES" -> tr("Series", "مسلسلات"); else -> t.name }
                NourTab(label, t == p.selectedTab, { p.onTabSelected(t) }, Modifier.fillMaxWidth())
            }
            item { NourTab("↻ " + tr("Full index", "فهرسة كاملة"), false, p.onBuildCompleteIndex, Modifier.fillMaxWidth()) }
            if (p.recentQueries.isNotEmpty()) {
                item { NourRowTitle(tr("Recently", "مؤخراً"), Modifier.padding(top = 10.dp)) }
                items(p.recentQueries) { q -> NourTab("↺ $q", false, { p.onRecentQuerySelected(q) }, Modifier.fillMaxWidth()) }
                item { NourTab(tr("Clear", "مسح"), false, p.onClearRecentQueries, Modifier.fillMaxWidth()) }
            }
        }
        LazyVerticalGrid(GridCells.Adaptive(125.dp), Modifier.weight(1f).fillMaxHeight(),
            horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 48.dp, top = 8.dp, start = 8.dp, end = 8.dp)) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                NourRowTitle(tr("Top results", "نتائج البحث"), trailing = "${s.movies.size + s.series.size + s.channels.size}")
            }
            when {
                p.query.isBlank() -> item(span = { GridItemSpan(maxLineSpan) }) { NourEmpty(tr("Search your library", "ابحث في مكتبتك")) }
                s.isLoading -> item(span = { GridItemSpan(maxLineSpan) }) { NourEmpty(tr("Searching…", "جار البحث…")) }
                s.hasSearchError -> item(span = { GridItemSpan(maxLineSpan) }) { NourEmpty(tr("Search failed. Try again.", "فشل البحث. حاول مرة أخرى.")) }
                s.isEmpty -> item(span = { GridItemSpan(maxLineSpan) }) { NourEmpty(tr("No results", "لا توجد نتائج")) }
                else -> {
                    if (p.selectedTab in setOf(SearchTab.ALL, SearchTab.MOVIES)) items(s.movies, key = { "m${it.id}" }) { m ->
                        val locked = p.isMovieLocked(m)
                        NourPoster(m.name, if (locked) null else m.posterUrl, m.year, { p.onMovieClick(m) },
                            locked = locked, onLongClick = { p.onMovieLongClick(m) })
                    }
                    if (p.selectedTab in setOf(SearchTab.ALL, SearchTab.SERIES)) items(s.series, key = { "s${it.id}" }) { m ->
                        val locked = p.isSeriesLocked(m)
                        NourPoster(m.name, if (locked) null else m.posterUrl, m.genre, { p.onSeriesClick(m) },
                            locked = locked, onLongClick = { p.onSeriesLongClick(m) })
                    }
                    if (s.channels.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.LIVE)) {
                        item(span = { GridItemSpan(maxLineSpan) }) { NourRowTitle(tr("Live channels", "قنوات مباشرة")) }
                        items(s.channels, key = { "c${it.id}" }, span = { GridItemSpan(maxLineSpan) }) { c ->
                            val locked = p.isChannelLocked(c)
                            NourCard({ p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c) }, modifier = Modifier.fillMaxWidth()) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    NourLogo(c.name, if (locked) null else c.logoUrl, 44.dp)
                                    Text(c.name, color = NR.Text, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                    Text(if (locked) "🔒" else c.currentProgram?.title.orEmpty(), color = NR.Sub, maxLines = 1, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun NourFavorites(p: FavoritesParams) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        LazyColumn(Modifier.width(230.dp).fillMaxHeight().clip(NR.R).background(NR.Raised.copy(alpha = 0.75f)), verticalArrangement = Arrangement.spacedBy(6.dp), contentPadding = PaddingValues(16.dp)) {
            item { Text("◈ " + tr("My library", "مكتبتي"), color = NR.Text, fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif, modifier = Modifier.padding(bottom = 8.dp)) }
            items(SavedLibraryPreset.entries) { pr -> NourTab(presetLabel(pr), pr == p.selectedPreset, { p.onPresetSelected(pr) }, Modifier.fillMaxWidth()) }
        }
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                items(SavedLibraryFilter.entries) { f -> NourTab(f.name.lowercase().replaceFirstChar { it.uppercase() }, f == p.selectedFilter, { p.onFilterSelected(f) }) }
                item { Text("   ⇅", color = NR.Faint, fontSize = 14.sp) }
                items(SavedLibrarySort.entries) { o -> NourTab(o.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }, o == p.selectedSort, { p.onSortSelected(o) }) }
            }
            LazyVerticalGrid(GridCells.Adaptive(140.dp), Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(18.dp), verticalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(6.dp, 6.dp, 6.dp, 48.dp)) {
                if (p.continueWatching.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) { NourRowTitle(tr("Still watching", "ما زلت تشاهد")) }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(p.continueWatching, key = { "c${it.history.id}" }) { h ->
                                val prog = if (h.history.totalDurationMs > 0) h.history.resumePositionMs.toFloat() / h.history.totalDurationMs else null
                                NourWide(h.title, h.history.posterUrl, h.subtitle, prog, { p.onHistoryClick(h) }, Modifier.width(250.dp))
                            }
                        }
                    }
                }
                if (p.recentLive.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) { NourRowTitle(tr("Recent channels", "قنوات حديثة")) }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                            items(p.recentLive, key = { "l${it.history.id}" }) { h ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(96.dp)) {
                                    NourCard(onClick = { p.onHistoryClick(h) }, shape = androidx.compose.foundation.shape.CircleShape, modifier = Modifier.size(76.dp), container = NR.Raised) {
                                        NourLogo(h.title, h.history.posterUrl, 76.dp)
                                    }
                                    Text(h.title, color = NR.Sub, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 6.dp))
                                }
                            }
                        }
                    }
                }
                val sections = p.sections.filter { it.items.isNotEmpty() }
                if (sections.isEmpty() && p.continueWatching.isEmpty() && p.recentLive.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
                    NourEmpty(tr("Your lounge is empty. Long-press any title to add it.", "صالتك فارغة. اضغط مطولاً على أي عنصر لإضافته."))
                }
                sections.forEach { section ->
                    item(key = "h${section.key}", span = { GridItemSpan(maxLineSpan) }) { NourRowTitle(section.title, trailing = "${section.items.size}") }
                    items(section.items, key = { "${section.key}${it.favorite.contentType}${it.favorite.contentId}" }) { f ->
                        NourPoster(f.title, null, f.subtitle, { p.onItemClick(f) }, onLongClick = { p.onItemLongClick(f) })
                    }
                }
            }
        }
    }
}

@Composable
internal fun NourSettingsNav(p: SettingsNavParams) {
    LazyColumn(Modifier.width(235.dp).fillMaxHeight().clip(NR.R).background(NR.Raised).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Text(tr("Settings", "الإعدادات"), color = NR.Text, fontSize = 25.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(12.dp)) }
        items(p.entries.size) { i ->
            val (label, icon) = p.entries[i]
            NourTab("$icon  $label", i == p.selectedCategory, { p.onCategorySelected(i) },
                Modifier.fillMaxWidth().then(if (i == p.selectedCategory) Modifier.focusRequester(p.focusRequester) else Modifier))
        }
    }
}

@Composable
internal fun NourSettingsFrame(navigation: @Composable () -> Unit, content: @Composable () -> Unit) {
    Row(Modifier.fillMaxSize().padding(horizontal = 36.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(22.dp)) {
        navigation()
        Box(Modifier.weight(1f).fillMaxHeight().clip(NR.R).background(NR.Card).padding(26.dp)) { content() }
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

