package com.streamvault.app.ui.themes.magazinemedia

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
private fun Byline(vararg parts: String?) {
    Text(parts.filterNot { it.isNullOrBlank() }.joinToString("  ·  "), color = MZ.Sub, fontSize = 13.sp, fontFamily = MZ.Serif, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

/** Body copy with a rust drop cap on the first letter. */
@Composable
private fun DropCap(text: String, maxLines: Int = 7) {
    if (text.isBlank()) return
    Row {
        Text(text.take(1), color = MZ.Amber, fontSize = 52.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Black, lineHeight = 52.sp, modifier = Modifier.padding(end = 6.dp))
        Text(text.drop(1), color = MZ.Text, fontSize = 15.sp, fontFamily = MZ.Serif, lineHeight = 23.sp, maxLines = maxLines, overflow = TextOverflow.Ellipsis)
    }
}

/** A row in a fact-box sidebar: ink-ruled, label left, glyph right, inverts on focus. */
@Composable
private fun FactAction(label: String, glyph: String, onClick: () -> Unit, modifier: Modifier = Modifier, primary: Boolean = false) {
    MzCard(onClick = onClick, modifier = modifier.fillMaxWidth(), zoom = 1.0f, container = if (primary) MZ.Amber else Color.Transparent, focusedContainer = MZ.Gold) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label.uppercase(), color = if (primary) MZ.Raised else MZ.Text, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, maxLines = 1, modifier = Modifier.weight(1f))
            Text(glyph, color = if (primary) MZ.Raised else MZ.Amber, fontSize = 15.sp)
        }
    }
}

/** Movie: a feature article. START photo plate with caption, centre article column (kicker, headline,
 *  byline, drop-cap plot, credits), END "fact box" sidebar holding every action as a ruled list. */
@Composable
internal fun MagazineMediaMovieDetail(p: MovieDetailParams) {
    val m = p.movie
    val play = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(m.id) { runCatching { play.requestFocus() } }
    MzBackground {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 56.dp, vertical = 36.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                    Column(Modifier.width(260.dp)) {
                        Box(Modifier.fillMaxWidth().aspectRatio(2f / 3f).border(1.dp, MZ.Text).padding(6.dp).background(MZ.Card)) {
                            (m.posterUrl ?: m.backdropUrl)?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                        }
                        Text(listOfNotNull(m.name, m.year).joinToString(", "), color = MZ.Faint, fontSize = 11.sp, fontFamily = MZ.Serif, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, maxLines = 1, modifier = Modifier.padding(top = 4.dp))
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        MzKicker(listOfNotNull(tr("Film", "فيلم"), m.genre).joinToString(" · "))
                        Text(m.name, color = MZ.Text, fontSize = 46.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Black, maxLines = 3, lineHeight = 50.sp, overflow = TextOverflow.Ellipsis)
                        Byline(m.year, m.duration, m.variantLabel, if (m.rating > 0) "★ %.1f".format(m.rating) else null)
                        MzRule(thick = 2.dp)
                        DropCap(m.plot.orEmpty())
                        m.director?.takeIf { it.isNotBlank() }?.let { Text(tr("Directed by", "إخراج") + " $it", color = MZ.Sub, fontSize = 13.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Bold, maxLines = 1) }
                        m.cast?.takeIf { it.isNotBlank() }?.let { Text(tr("With", "بطولة") + " $it", color = MZ.Sub, fontSize = 13.sp, fontFamily = MZ.Serif, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, maxLines = 2, overflow = TextOverflow.Ellipsis) }
                    }
                    Column(Modifier.width(250.dp).background(MZ.Raised).border(1.dp, MZ.Text).padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        MzKicker(tr("Fact box", "معلومات"))
                        Text(tr("How to watch", "المشاهدة"), color = MZ.Text, fontSize = 18.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Bold)
                        MzRule(Modifier.padding(vertical = 6.dp))
                        if (p.hasResume) Text(tr("You stopped at", "توقفت عند") + " " + mzDuration(p.resumePositionMs), color = MZ.Amber, fontSize = 12.sp, fontFamily = MZ.Serif, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, modifier = Modifier.padding(bottom = 4.dp))
                        FactAction(if (p.hasResume) tr("Resume", "استكمال") else tr("Play", "تشغيل"), "▶", p.onPlay, Modifier.focusRequester(play), primary = true)
                        p.onPlayTrailer?.let { FactAction(tr("Trailer", "الإعلان"), "▷", it) }
                        FactAction(if (m.isFavorite) tr("Saved", "محفوظ") else tr("Save", "حفظ"), if (m.isFavorite) "★" else "☆", p.onToggleFavorite)
                        FactAction(if (p.isCasting) tr("Casting…", "جار البث…") else tr("Cast", "بث"), "⎚", p.onCast)
                        FactAction(tr("Download", "تنزيل"), "↓", p.onDownload)
                        FactAction(tr("Copy link", "نسخ الرابط"), "⧉", p.onCopyUrl)
                        FactAction(tr("Back", "رجوع"), "←", p.onBack)
                    }
                }
            }
            if (m.variants.size > 1) item {
                Column {
                    MzRowTitle(tr("Editions", "النسخ"))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(m.variants) { v -> MzTab(v.label, v.rawMovieId == m.selectedVariantId, { p.onSelectVariant(v.rawMovieId) }) }
                    }
                }
            }
            if (p.relatedContent.isNotEmpty()) item {
                Column {
                    MzRowTitle(tr("Further reading", "مشابه"))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(vertical = 6.dp)) {
                        items(p.relatedContent, key = { it.id }) { r -> MzPoster(r.name, r.posterUrl, r.year, { p.onRelatedClick(r) }, Modifier.width(130.dp)) }
                    }
                }
            }
        }
    }
}

/** Series: a serialised feature. Masthead-style header band (headline, byline, ratings, actions in a row
 *  of ruled links), then START "Chapters" column of seasons beside a numbered episode article list. */
@Composable
internal fun MagazineMediaSeriesDetail(p: SeriesDetailParams) {
    val s = p.series
    val primary = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(s.id) { runCatching { primary.requestFocus() } }
    MzBackground {
        Column(Modifier.fillMaxSize().padding(horizontal = 56.dp, vertical = 30.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.Top) {
                Box(Modifier.width(150.dp).aspectRatio(2f / 3f).border(1.dp, MZ.Text).padding(4.dp).background(MZ.Card)) {
                    (s.posterUrl ?: s.backdropUrl)?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    MzKicker(listOfNotNull(tr("Serial", "مسلسل"), s.genre).joinToString(" · "))
                    Text(s.name, color = MZ.Text, fontSize = 40.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Black, maxLines = 2, lineHeight = 44.sp, overflow = TextOverflow.Ellipsis)
                    Byline(s.releaseDate, "${s.seasons.size} " + tr("seasons", "مواسم"), if (s.rating > 0) "★ %.1f".format(s.rating) else null, if (p.unwatchedEpisodeCount > 0) "${p.unwatchedEpisodeCount} " + tr("unread episodes", "حلقات جديدة") else null)
                    if (!p.isLoadingExternalRatings) {
                        val r = p.externalRatings
                        val ext = listOf("IMDb" to r.imdb, "Rotten Tomatoes" to r.rottenTomatoes, "Metacritic" to r.metacritic).filter { it.second.available }
                        if (ext.isNotEmpty()) Text(tr("The critics", "النقاد") + ":  " + ext.joinToString("   ") { "${it.first} ${it.second.displayValue}" }, color = MZ.Sub, fontSize = 12.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Bold)
                    }
                    Text(s.plot.orEmpty(), color = MZ.Text, fontSize = 14.sp, fontFamily = MZ.Serif, lineHeight = 21.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    val resume = p.resumeEpisode
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 4.dp)) {
                        MzButton(
                            if (resume != null) tr("Continue", "متابعة") + " S${resume.seasonNumber}·E${resume.episodeNumber}" else tr("Begin", "ابدأ"),
                            { (resume ?: p.selectedSeason?.episodes?.firstOrNull())?.let(p.onResumeClick) }, Modifier.focusRequester(primary), primary = true, icon = "▶"
                        )
                        MzButton(if (s.isFavorite) tr("Saved", "محفوظ") else tr("Save", "حفظ"), p.onToggleFavorite, icon = if (s.isFavorite) "★" else "☆")
                        if (resume != null) MzButton(if (p.isCasting) tr("Casting…", "جار البث…") else tr("Cast", "بث"), p.onCastResumeEpisode, icon = "⎚")
                        MzButton(tr("Back", "رجوع"), p.onBack, icon = "←")
                    }
                    if (s.variants.size > 1) LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) { items(s.variants) { v -> MzTab(v.label, v.rawSeriesId == s.selectedVariantId, { p.onSelectVariant(v.rawSeriesId) }) } }
                }
            }
            MzRule(thick = 2.dp)
            Row(Modifier.fillMaxSize()) {
                Column(Modifier.width(210.dp).fillMaxHeight()) {
                    MzKicker(tr("Chapters", "المواسم"), Modifier.padding(bottom = 4.dp))
                    LazyColumn {
                        items(s.seasons, key = { it.seasonNumber }) { season ->
                            val sel = season.seasonNumber == p.selectedSeason?.seasonNumber
                            MzCard(onClick = { p.onSeasonSelected(season) }, zoom = 1.0f, container = if (sel) MZ.Gold.copy(alpha = 0.5f) else Color.Transparent, focusedContainer = MZ.Raised, modifier = Modifier.fillMaxWidth()) {
                                Column {
                                    Row(Modifier.padding(horizontal = 8.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(romanOf(season.seasonNumber), color = MZ.Amber, fontSize = 16.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Black, modifier = Modifier.width(44.dp))
                                        Text(season.name.ifBlank { tr("Season", "الموسم") + " ${season.seasonNumber}" }, color = MZ.Text, fontSize = 14.sp, fontFamily = MZ.Serif, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    MzRule(color = MZ.Line)
                                }
                            }
                        }
                    }
                }
                Box(Modifier.padding(horizontal = 20.dp).width(1.dp).fillMaxHeight().background(MZ.Text))
                LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 40.dp)) {
                    items(p.selectedSeason?.episodes.orEmpty(), key = { it.id }) { e ->
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                MzCard(onClick = { p.onEpisodeClick(e) }, modifier = Modifier.weight(1f), zoom = 1.0f, container = Color.Transparent, focusedContainer = MZ.Raised) {
                                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Text("%02d".format(e.episodeNumber), color = MZ.Amber, fontSize = 30.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Black, modifier = Modifier.width(52.dp))
                                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                            Text(e.title, color = MZ.Text, fontSize = 16.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(e.plot.orEmpty(), color = MZ.Sub, fontSize = 13.sp, fontFamily = MZ.Serif, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                            if (e.watchProgress > 0 && e.durationSeconds > 0) MzProgress(e.watchProgress / (e.durationSeconds * 1000f), Modifier.widthIn(max = 240.dp))
                                        }
                                        e.duration?.let { Text(it, color = MZ.Faint, fontSize = 12.sp, fontFamily = MZ.Serif, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic) }
                                        Box(Modifier.width(150.dp).aspectRatio(16f / 9f).background(MZ.Card)) {
                                            e.coverUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                                        }
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    MzRound("⎚", { p.onCastEpisode(e) }, size = 38.dp)
                                    MzRound("↓", { p.onDownloadEpisode(e) }, size = 38.dp)
                                    MzRound("⧉", { p.onCopyEpisodeUrl(e) }, size = 38.dp)
                                }
                            }
                            MzRule(color = MZ.Line)
                        }
                    }
                }
            }
        }
    }
}

private fun romanOf(n: Int): String {
    if (n !in 1..39) return "$n"
    val tens = listOf("", "X", "XX", "XXX"); val ones = listOf("", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX")
    return tens[n / 10] + ones[n % 10]
}

/** Search: START "Index desk" column (query line, sections, recent queries as a numbered list),
 *  END results set as ruled sections: channels as a listings column, films/series as poster rows. */
@Composable
internal fun MagazineMediaSearch(p: SearchParams) {
    LaunchedEffect(Unit) { runCatching { p.searchFocusRequester.requestFocus() } }
    val s = p.uiState
    Row(Modifier.fillMaxSize()) {
        Column(Modifier.width(300.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MzKicker(tr("Index desk", "البحث"))
            Text(tr("Look it up", "ابحث"), color = MZ.Text, fontSize = 30.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Black)
            MzSearchField(p.query, p.onQueryChange, tr("Channels, films, series…", "قنوات، أفلام، مسلسلات…"), Modifier.fillMaxWidth().focusRequester(p.searchFocusRequester), onSubmit = p.onSearch)
            Column {
                SearchTab.entries.forEach { t ->
                    val label = when (t.name) { "ALL" -> tr("Everything", "الكل"); "LIVE" -> tr("Live", "مباشر"); "MOVIES" -> tr("Films", "أفلام"); "SERIES" -> tr("Series", "مسلسلات"); else -> t.name }
                    MzTab(label, t == p.selectedTab, { p.onTabSelected(t) })
                }
                MzTab(tr("Rebuild full index", "فهرسة كاملة"), false, p.onBuildCompleteIndex)
            }
            if (p.recentQueries.isNotEmpty()) {
                MzRule(Modifier.padding(top = 6.dp))
                MzKicker(tr("Recently looked up", "عمليات البحث الأخيرة"), color = MZ.Sub)
                LazyColumn {
                    items(p.recentQueries.size) { i ->
                        val q = p.recentQueries[i]
                        MzCard(onClick = { p.onRecentQuerySelected(q) }, zoom = 1.0f, container = Color.Transparent, focusedContainer = MZ.Raised, modifier = Modifier.fillMaxWidth()) {
                            Text("${i + 1}.  $q", color = MZ.Text, fontSize = 14.sp, fontFamily = MZ.Serif, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, maxLines = 1, modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp))
                        }
                    }
                    item { MzTab(tr("Clear list", "مسح"), false, p.onClearRecentQueries) }
                }
            }
        }
        Box(Modifier.padding(horizontal = 22.dp).width(1.dp).fillMaxHeight().background(MZ.Text))
        LazyColumn(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(20.dp), contentPadding = PaddingValues(bottom = 48.dp)) {
            when {
                p.query.isBlank() -> item { MzEmpty(tr("Type a word to begin", "اكتب كلمة للبدء")) }
                s.isLoading -> item { MzEmpty(tr("Searching the archive…", "جار البحث…")) }
                s.hasSearchError -> item { MzEmpty(tr("Search failed. Try again.", "فشل البحث. حاول مرة أخرى.")) }
                s.isEmpty -> item { MzEmpty(tr("No entries for", "لا نتائج لـ") + " “${p.query}”") }
                else -> {
                    if (s.channels.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.LIVE)) item {
                        Column {
                            MzRowTitle(tr("Live channels", "قنوات مباشرة"), trailing = "${s.channels.size}")
                            s.channels.take(40).forEach { c ->
                                val locked = p.isChannelLocked(c)
                                MzCard(onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c) }, zoom = 1.0f, container = Color.Transparent, focusedContainer = MZ.Raised, modifier = Modifier.fillMaxWidth()) {
                                    Column {
                                        Row(Modifier.padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            MzLogo(c.name, if (locked) null else c.logoUrl, 36.dp)
                                            Text(c.name, color = MZ.Text, fontSize = 15.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(240.dp))
                                            Text(if (locked) "🔒" else c.currentProgram?.title.orEmpty(), color = MZ.Sub, fontSize = 13.sp, fontFamily = MZ.Serif, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                            if (c.id in p.recordingChannelIds) MzBadge("REC", MZ.Live, filled = true)
                                            else if (c.id in p.scheduledChannelIds) MzBadge("◷", MZ.Blue)
                                            c.qualityBadge()?.let { MzBadge(it, MZ.Sub) }
                                        }
                                        MzRule(color = MZ.Line)
                                    }
                                }
                            }
                        }
                    }
                    if (s.movies.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.MOVIES)) item {
                        Column {
                            MzRowTitle(tr("Films", "أفلام"), trailing = "${s.movies.size}")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(vertical = 6.dp)) {
                                items(s.movies, key = { it.id }) { m -> val l = p.isMovieLocked(m); MzPoster(m.name, if (l) null else m.posterUrl, m.year, { p.onMovieClick(m) }, Modifier.width(130.dp), locked = l, onLongClick = { p.onMovieLongClick(m) }) }
                            }
                        }
                    }
                    if (s.series.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.SERIES)) item {
                        Column {
                            MzRowTitle(tr("Series", "مسلسلات"), trailing = "${s.series.size}")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(vertical = 6.dp)) {
                                items(s.series, key = { it.id }) { m -> val l = p.isSeriesLocked(m); MzPoster(m.name, if (l) null else m.posterUrl, m.genre, { p.onSeriesClick(m) }, Modifier.width(130.dp), locked = l, onLongClick = { p.onSeriesLongClick(m) }) }
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

/** Saved: START "Departments" column (presets, then filter and sort as small-cap lists),
 *  END a clipping board: continue strip, live clippings, sections of ruled poster grids. */
@Composable
internal fun MagazineMediaFavorites(p: FavoritesParams) {
    Row(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.width(230.dp).fillMaxHeight()) {
            item { MzKicker(tr("Departments", "الأقسام")) }
            item { Text(tr("Saved", "المحفوظات"), color = MZ.Text, fontSize = 30.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Black, modifier = Modifier.padding(bottom = 6.dp)) }
            items(SavedLibraryPreset.entries) { pr ->
                val sel = pr == p.selectedPreset
                MzCard(onClick = { p.onPresetSelected(pr) }, zoom = 1.0f, container = if (sel) MZ.Gold.copy(alpha = 0.5f) else Color.Transparent, focusedContainer = MZ.Raised, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(presetLabel(pr), color = if (sel) MZ.Amber else MZ.Text, fontSize = 15.sp, fontFamily = MZ.Serif, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp))
                        MzRule(color = MZ.Line)
                    }
                }
            }
            item { MzKicker(tr("Filter", "تصفية"), Modifier.padding(top = 14.dp, bottom = 2.dp), MZ.Sub) }
            items(SavedLibraryFilter.entries) { f -> MzTab(f.name.lowercase().replaceFirstChar { it.uppercase() }, f == p.selectedFilter, { p.onFilterSelected(f) }) }
            item { MzKicker(tr("Order", "ترتيب"), Modifier.padding(top = 14.dp, bottom = 2.dp), MZ.Sub) }
            items(SavedLibrarySort.entries) { o -> MzTab(o.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }, o == p.selectedSort, { p.onSortSelected(o) }) }
        }
        Box(Modifier.padding(horizontal = 22.dp).width(1.dp).fillMaxHeight().background(MZ.Text))
        LazyVerticalGrid(GridCells.Adaptive(140.dp), Modifier.weight(1f).fillMaxHeight(), horizontalArrangement = Arrangement.spacedBy(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp), contentPadding = PaddingValues(4.dp, 4.dp, 4.dp, 48.dp)) {
            if (p.continueWatching.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) { MzRowTitle(tr("Where you left off", "متابعة المشاهدة")) }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                        items(p.continueWatching, key = { "c${it.history.id}" }) { h ->
                            val prog = if (h.history.totalDurationMs > 0) h.history.resumePositionMs.toFloat() / h.history.totalDurationMs else null
                            MzWide(h.title, h.history.posterUrl, h.subtitle, prog, { p.onHistoryClick(h) }, Modifier.width(260.dp))
                        }
                    }
                }
            }
            if (p.recentLive.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) { MzRowTitle(tr("Live clippings", "مباشر شوهد مؤخراً")) }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                        items(p.recentLive, key = { "l${it.history.id}" }) { h ->
                            Row {
                                MzCard(onClick = { p.onHistoryClick(h) }, modifier = Modifier.width(210.dp), zoom = 1.0f, container = Color.Transparent, focusedContainer = MZ.Raised) {
                                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        MzLogo(h.title, h.history.posterUrl, 36.dp)
                                        Column { Text(h.title, color = MZ.Text, fontSize = 13.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(h.subtitle, color = MZ.Faint, fontSize = 11.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, maxLines = 1) }
                                    }
                                }
                                Box(Modifier.padding(horizontal = 6.dp).width(1.dp).height(56.dp).background(MZ.Line))
                            }
                        }
                    }
                }
            }
            val sections = p.sections.filter { it.items.isNotEmpty() }
            if (sections.isEmpty() && p.continueWatching.isEmpty() && p.recentLive.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
                MzEmpty(tr("Nothing saved yet. Long-press any title to keep it here.", "قائمتك فارغة. اضغط مطولاً على أي عنصر لإضافته."))
            }
            sections.forEach { section ->
                item(key = "h${section.key}", span = { GridItemSpan(maxLineSpan) }) { MzRowTitle(section.title, trailing = "${section.items.size}") }
                items(section.items, key = { "${section.key}${it.favorite.contentType}${it.favorite.contentId}" }) { f ->
                    MzPoster(f.title, null, f.subtitle, { p.onItemClick(f) }, onLongClick = { p.onItemLongClick(f) })
                }
            }
        }
    }
}

/** Settings: a numbered "§" table of contents on the start side, page frame with ink border on the end. */
@Composable
internal fun MagazineMediaSettingsNav(p: SettingsNavParams) {
    LazyColumn(Modifier.width(290.dp).fillMaxHeight(), contentPadding = PaddingValues(vertical = 4.dp)) {
        item { MzKicker(tr("Masthead", "الإعدادات")) }
        item { Text(tr("Settings", "الإعدادات"), color = MZ.Text, fontSize = 30.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Black) }
        item { MzRule(Modifier.padding(vertical = 6.dp), thick = 2.dp) }
        items(p.entries.size) { i ->
            val (label, icon) = p.entries[i]
            val sel = i == p.selectedCategory
            MzCard(
                onClick = { p.onCategorySelected(i) }, zoom = 1.0f, container = if (sel) MZ.Gold.copy(alpha = 0.5f) else Color.Transparent, focusedContainer = MZ.Raised,
                modifier = Modifier.fillMaxWidth().then(if (sel) Modifier.focusRequester(p.focusRequester) else Modifier)
            ) {
                Column {
                    Row(Modifier.padding(horizontal = 8.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("§${i + 1}", color = MZ.Amber, fontSize = 14.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Black, modifier = Modifier.width(36.dp))
                        Text(label, color = MZ.Text, fontSize = 15.sp, fontFamily = MZ.Serif, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, maxLines = 1, modifier = Modifier.weight(1f))
                        Text(icon, fontSize = 14.sp, color = MZ.Sub)
                    }
                    MzRule(color = MZ.Line)
                }
            }
        }
    }
}

@Composable
internal fun MagazineMediaSettingsFrame(navigation: @Composable () -> Unit, content: @Composable () -> Unit) {
    Row(Modifier.fillMaxSize()) {
        navigation()
        Box(Modifier.padding(horizontal = 22.dp).width(1.dp).fillMaxHeight().background(MZ.Text))
        Box(Modifier.weight(1f).fillMaxHeight().background(MZ.Raised).border(1.dp, MZ.Line).padding(24.dp)) { content() }
    }
}
