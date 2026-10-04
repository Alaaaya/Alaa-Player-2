package com.streamvault.app.ui.themes.chatgpt2

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Glow
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.app.ui.themes.bespoke.InnerPanelBackScope
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
private fun CgBackdrop(url: String?, content: @Composable BoxScope.() -> Unit) {
    CgBackground {
        url?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(CG.Bg, CG.Bg.copy(alpha = 0.85f), CG.Bg.copy(alpha = 0.2f)))))
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, CG.Bg))))
        content()
    }
}

@Composable
private fun Meta(vararg parts: String?) {
    Text(parts.filterNot { it.isNullOrBlank() }.joinToString("  •  "), color = CG.Sub, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

/** Glossy blue pill "Watch Now" CTA with a soft glow. */
@Composable
private fun CgWatchNow(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TvClickableSurface(
        onClick = onClick, modifier = modifier.height(54.dp),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(27.dp)),
        colors = ClickableSurfaceDefaults.colors(containerColor = CG.AmberDeep, focusedContainerColor = CG.Amber, contentColor = Color.White, focusedContentColor = Color.White),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(androidx.compose.foundation.BorderStroke(2.dp, CG.Blue), shape = RoundedCornerShape(27.dp))),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
        glow = ClickableSurfaceDefaults.glow(glow = Glow(CG.Amber.copy(alpha = 0.35f), 10.dp), focusedGlow = Glow(CG.Blue.copy(alpha = 0.7f), 22.dp))
    ) {
        Row(Modifier.fillMaxHeight().padding(horizontal = 30.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("▶", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(label, fontSize = 17.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

/** Round glass icon key with a caption underneath. */
@Composable
private fun CgIconKey(glyph: String, label: String, onClick: () -> Unit, active: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.width(72.dp)) {
        TvClickableSurface(
            onClick = onClick, modifier = Modifier.size(52.dp),
            shape = ClickableSurfaceDefaults.shape(CircleShape),
            colors = ClickableSurfaceDefaults.colors(containerColor = Color(0x99222228), focusedContainerColor = CG.Amber, contentColor = if (active) Color(0xFFFF5A7A) else CG.Text, focusedContentColor = Color.White),
            border = ClickableSurfaceDefaults.border(border = Border(androidx.compose.foundation.BorderStroke(1.dp, CG.Line), shape = CircleShape)),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
            glow = ClickableSurfaceDefaults.glow(focusedGlow = Glow(CG.Amber.copy(alpha = 0.6f), 16.dp))
        ) { CgGlyph(glyph, 24.dp, Modifier.align(Alignment.Center)) }
        Text(label, color = CG.Sub, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun CgChip(text: String) {
    Text(text, color = CG.Text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1,
        modifier = Modifier.background(Color(0x66222228), RoundedCornerShape(50)).border(1.dp, CG.Line, RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 4.dp))
}

/** Movie: cinematic full-bleed backdrop, info stack bottom START, blue Watch Now pill + round icon keys
 *  (heart, trailer, cast, download, copy, back). Versions + "more like this" strip underneath. */
@Composable
internal fun ChatGpt2MovieDetail(p: MovieDetailParams) {
    val m = p.movie
    val play = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(m.id) { runCatching { play.requestFocus() } }
    val tVer = tr("Versions", "النسخ"); val tMore = tr("More like this", "مشابه")
    CgBackdrop(m.backdropUrl ?: m.posterUrl) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 56.dp, end = 56.dp, top = 70.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.widthIn(max = 720.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    CgBadge(tr("MOVIE", "فيلم"), CG.Blue)
                    m.genre?.split(',', '/', '|')?.map { it.trim() }?.filter { it.isNotBlank() }?.take(3)?.forEach { CgChip(it) }
                }
                Text(m.name, color = Color.White, fontSize = 46.sp, fontWeight = FontWeight.Black, maxLines = 2, lineHeight = 50.sp, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (m.rating > 0) Text("★ %.1f".format(m.rating), color = Color(0xFFFFC94D), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Meta(m.year, m.duration, m.variantLabel)
                }
                Text(m.plot.orEmpty(), color = CG.Sub, fontSize = 15.sp, maxLines = 4, overflow = TextOverflow.Ellipsis, lineHeight = 22.sp)
                m.director?.takeIf { it.isNotBlank() }?.let { Text(tr("Director", "المخرج") + ": $it", color = CG.Sub, fontSize = 14.sp, maxLines = 1) }
                m.cast?.takeIf { it.isNotBlank() }?.let { Text(tr("Cast", "الممثلون") + ": $it", color = CG.Sub, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                if (p.hasResume) Text(tr("Resume from", "استكمال من") + " " + cgDuration(p.resumePositionMs), color = CG.Blue, fontSize = 13.sp)
            }
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                CgWatchNow(if (p.hasResume) tr("Resume", "استكمال") else tr("Watch Now", "شاهد الآن"), p.onPlay, Modifier.padding(top = 0.dp).focusRequester(play))
                CgIconKey(if (m.isFavorite) "♥" else "♡", if (m.isFavorite) tr("Saved", "محفوظ") else tr("My List", "قائمتي"), p.onToggleFavorite, active = m.isFavorite)
                p.onPlayTrailer?.let { CgIconKey("▷", tr("Trailer", "الإعلان"), it) }
                CgIconKey("⎚", if (p.isCasting) tr("Casting…", "جار البث…") else tr("Cast", "بث"), p.onCast)
                CgIconKey("↓", tr("Download", "تنزيل"), p.onDownload)
                CgIconKey("⧉", tr("Copy link", "نسخ الرابط"), p.onCopyUrl)
                CgIconKey("←", tr("Back", "رجوع"), p.onBack)
            }
            if (m.variants.size > 1) Row(verticalAlignment = Alignment.CenterVertically) {
                CgHeading(tVer, size = 11, modifier = Modifier.padding(end = 10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(2.dp)) { items(m.variants) { v -> CgTab(v.label, v.rawMovieId == m.selectedVariantId, { p.onSelectVariant(v.rawMovieId) }) } }
            }
            if (p.relatedContent.isNotEmpty()) Column {
                CgRowTitle(tMore)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 6.dp)) {
                    items(p.relatedContent, key = { it.id }) { r -> CgWide(r.name, r.backdropUrl ?: r.posterUrl, r.year, null, { p.onRelatedClick(r) }, Modifier.width(220.dp)) }
                }
            }
        }
    }
}

/** Series: cinematic hero (title, chips, ratings, Watch Now + heart), then a season dropdown and a
 *  thumbnail episode list (16:9 still, number, title, plot, runtime, progress, cast/download/copy keys). */
@Composable
internal fun ChatGpt2SeriesDetail(p: SeriesDetailParams) {
    val s = p.series
    val primary = remember { androidx.compose.ui.focus.FocusRequester() }
    val dropFocus = remember { androidx.compose.ui.focus.FocusRequester() }
    val firstSeasonFocus = remember { androidx.compose.ui.focus.FocusRequester() }
    var dropdown by remember { mutableStateOf(false) }
    LaunchedEffect(s.id) { runCatching { primary.requestFocus() } }
    LaunchedEffect(dropdown) { if (dropdown) runCatching { firstSeasonFocus.requestFocus() } }
    val tSeason = tr("Season", "الموسم"); val tVer = tr("Versions", "النسخ"); val tNew = tr("new", "جديد"); val tEp = tr("Episodes", "الحلقات")
    val seasonLabel: (com.streamvault.domain.model.Season) -> String = { it.name.ifBlank { "$tSeason ${it.seasonNumber}" } }
    CgBackdrop(s.backdropUrl ?: s.posterUrl) {
        Column(Modifier.fillMaxSize().padding(start = 56.dp, end = 56.dp, top = 44.dp, bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.widthIn(max = 760.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    CgBadge(tr("SERIES", "مسلسل"), CG.Blue)
                    Text("${s.seasons.size} " + tr("seasons", "مواسم"), color = CG.Sub, fontSize = 12.sp)
                    s.genre?.split(',', '/', '|')?.map { it.trim() }?.filter { it.isNotBlank() }?.take(3)?.forEach { CgChip(it) }
                }
                Text(s.name, color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (s.rating > 0) Text("★ %.1f".format(s.rating), color = Color(0xFFFFC94D), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    if (p.unwatchedEpisodeCount > 0) CgBadge("${p.unwatchedEpisodeCount} $tNew", CG.Blue, filled = true)
                    s.releaseDate?.takeIf { it.isNotBlank() }?.let { Text(it, color = CG.Sub, fontSize = 12.sp) }
                    if (!p.isLoadingExternalRatings) {
                        val r = p.externalRatings
                        val ext = listOf("IMDb" to r.imdb, "RT" to r.rottenTomatoes, "MC" to r.metacritic).filter { it.second.available }
                        if (ext.isNotEmpty()) Text(ext.joinToString("   ") { "${it.first} ${it.second.displayValue}" }, color = CG.Faint, fontSize = 12.sp)
                    }
                }
                Text(s.plot.orEmpty(), color = CG.Sub, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                val resume = p.resumeEpisode
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.Top) {
                    CgWatchNow(
                        if (resume != null) tr("Continue", "متابعة") + " S${resume.seasonNumber}E${resume.episodeNumber}" else tr("Watch Now", "شاهد الآن"),
                        { (resume ?: p.selectedSeason?.episodes?.firstOrNull())?.let(p.onResumeClick) }, Modifier.focusRequester(primary)
                    )
                    CgIconKey(if (s.isFavorite) "♥" else "♡", if (s.isFavorite) tr("Saved", "محفوظ") else tr("My List", "قائمتي"), p.onToggleFavorite, active = s.isFavorite)
                    if (resume != null) CgIconKey("⎚", if (p.isCasting) tr("Casting…", "جار البث…") else tr("Cast", "بث"), p.onCastResumeEpisode)
                    CgIconKey("←", tr("Back", "رجوع"), p.onBack)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CgHeading(tEp, size = 13)
                Box(Modifier.focusRequester(dropFocus)) {
                    CgCard(onClick = { dropdown = !dropdown }, shape = RoundedCornerShape(50), container = Color(0xCC2A1014), zoom = 1.03f) {
                        Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(p.selectedSeason?.let(seasonLabel) ?: tSeason, color = CG.Text, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text(if (dropdown) "▲" else "▼", color = CG.Blue, fontSize = 11.sp)
                        }
                    }
                }
                p.selectedSeason?.let { Text("${it.episodes.size} " + tr("episodes", "حلقة"), color = CG.Faint, fontSize = 12.sp) }
                if (s.variants.size > 1) {
                    Spacer(Modifier.width(12.dp))
                    CgHeading(tVer, size = 10, color = CG.Faint)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(2.dp)) { items(s.variants) { v -> CgTab(v.label, v.rawSeriesId == s.selectedVariantId, { p.onSelectVariant(v.rawSeriesId) }) } }
                }
            }
            Box(Modifier.weight(1f).fillMaxWidth()) {
                LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
                    items(p.selectedSeason?.episodes.orEmpty(), key = { it.id }) { e ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CgCard(onClick = { p.onEpisodeClick(e) }, container = Color(0xB3141418), shape = CG.R, zoom = 1.01f, modifier = Modifier.weight(1f)) {
                                Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                    Box(Modifier.width(168.dp).aspectRatio(16f / 9f).clip(CG.RSmall).background(CG.Card)) {
                                        val thumb = e.coverUrl ?: p.selectedSeason?.coverUrl ?: s.backdropUrl
                                        thumb?.let { AsyncImage(it, e.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                                        Box(Modifier.align(Alignment.Center).size(34.dp).background(Color(0x99000000), CircleShape), contentAlignment = Alignment.Center) { Text("▶", color = Color.White, fontSize = 13.sp) }
                                        if (e.watchProgress > 0 && e.durationSeconds > 0) CgProgress(e.watchProgress / (e.durationSeconds * 1000f), Modifier.align(Alignment.BottomCenter), 3.dp)
                                    }
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("${e.episodeNumber}. ${e.title}", color = CG.Text, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(e.plot.orEmpty(), color = CG.Faint, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 17.sp)
                                    }
                                    e.duration?.let { Text(it, color = CG.Sub, fontSize = 12.sp) }
                                }
                            }
                            CgRound("⎚", { p.onCastEpisode(e) }, size = 36.dp)
                            CgRound("↓", { p.onDownloadEpisode(e) }, size = 36.dp)
                            CgRound("⧉", { p.onCopyEpisodeUrl(e) }, size = 36.dp)
                        }
                    }
                }
                if (dropdown) InnerPanelBackScope(onClose = { dropdown = false }, opener = dropFocus) {
                    LazyColumn(Modifier.width(260.dp).heightIn(max = 320.dp).background(CG.Raised.copy(alpha = 0.98f), CG.R).border(1.dp, CG.Amber.copy(alpha = 0.6f), CG.R).padding(6.dp)) {
                        items(s.seasons.size) { i ->
                            val season = s.seasons[i]
                            val sel = season.seasonNumber == p.selectedSeason?.seasonNumber
                            CgCard(onClick = { p.onSeasonSelected(season); dropdown = false; runCatching { dropFocus.requestFocus() } }, shape = CG.RSmall,
                                container = if (sel) CG.AmberDeep else Color.Transparent, modifier = Modifier.fillMaxWidth().then(if (i == 0) Modifier.focusRequester(firstSeasonFocus) else Modifier)) {
                                Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(seasonLabel(season), color = CG.Text, fontSize = 14.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, maxLines = 1, modifier = Modifier.weight(1f))
                                    if (sel) Text("✓", color = CG.Blue, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Search: receiver "find" page. START column = field, vertical scope menu, recent queries; END = results
 *  as a ruled channel ledger plus framed poster shelves for movies and series. */
@Composable
internal fun ChatGpt2Search(p: SearchParams) {
    LaunchedEffect(Unit) { runCatching { p.searchFocusRequester.requestFocus() } }
    val s = p.uiState
    val tLive = tr("Live channels", "قنوات مباشرة"); val tMov = tr("Movies", "أفلام"); val tSer = tr("Series", "مسلسلات")
    val tAll = tr("Everything", "الكل"); val tLv = tr("Live", "مباشر")
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(26.dp)) {
        Column(Modifier.width(380.dp).fillMaxHeight().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            CgSearchField(p.query, p.onQueryChange, tr("Search movies, series, channels…", "ابحث عن أفلام، مسلسلات، قنوات…"), Modifier.fillMaxWidth().focusRequester(p.searchFocusRequester), onSubmit = p.onSearch)
            CgArabicKeyboard(
                onKey = { p.onQueryChange(p.query + it) },
                onBackspace = { p.onQueryChange(p.query.dropLast(1)) },
                onSearch = p.onSearch
            )
            Spacer(Modifier.height(6.dp))
            SearchTab.entries.forEach { t ->
                val label = when (t.name) { "ALL" -> tAll; "LIVE" -> tLv; "MOVIES" -> tMov; "SERIES" -> tSer; else -> t.name }
                CgCard(onClick = { p.onTabSelected(t) }, shape = CG.RSmall, container = if (t == p.selectedTab) CG.AmberDeep else Color.Transparent, modifier = Modifier.fillMaxWidth()) {
                    Text(label, color = Color.White, fontWeight = if (t == p.selectedTab) FontWeight.Bold else FontWeight.Normal, fontSize = 16.sp, fontFamily = CG.Serif, modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp))
                }
            }
            CgButton(tr("Build full index", "فهرسة كاملة"), p.onBuildCompleteIndex, Modifier.fillMaxWidth(), icon = "↻")
            if (p.recentQueries.isNotEmpty()) {
                CgRowTitle(tr("Recent", "الأخيرة"), Modifier.padding(top = 10.dp))
                p.recentQueries.forEach { q -> CgTab(q, false, { p.onRecentQuerySelected(q) }) }
                CgTab(tr("Clear", "مسح"), false, p.onClearRecentQueries)
            }
        }
        Box(Modifier.weight(1f).fillMaxHeight()) {
            when {
                p.query.isBlank() -> CgEmpty(tr("Type to search", "اكتب للبحث"))
                s.isLoading -> CgEmpty(tr("Searching…", "جار البحث…"))
                s.hasSearchError -> CgEmpty(tr("Search failed. Try again.", "فشل البحث. حاول مرة أخرى."))
                s.isEmpty -> CgEmpty(tr("No results for", "لا نتائج لـ") + " “${p.query}”")
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(bottom = 40.dp)) {
                    if (s.channels.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.LIVE)) {
                        item { CgRowTitle(tLive, trailing = "${s.channels.size}") }
                        items(s.channels.take(if (p.selectedTab == SearchTab.LIVE) 300 else 6), key = { "c${it.id}" }) { c ->
                            val locked = p.isChannelLocked(c)
                            CgCard(onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c) }, container = Color.Transparent, shape = CG.RSmall, modifier = Modifier.fillMaxWidth()) {
                                Row(Modifier.padding(horizontal = 14.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    CgLogo(c.name, if (locked) null else c.logoUrl, 36.dp)
                                    Text(c.name, color = CG.Text, fontSize = 15.sp, fontFamily = CG.Serif, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                    Text(if (locked) "🔒" else c.currentProgram?.title.orEmpty(), color = CG.Faint, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                    if (c.id in p.recordingChannelIds) CgBadge("REC", CG.Live, filled = true) else if (c.id in p.scheduledChannelIds) CgBadge("◷", CG.Blue)
                                    c.qualityBadge()?.let { CgBadge(it, CG.Sub) }
                                }
                            }
                        }
                    }
                    if (s.movies.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.MOVIES)) item {
                        Column {
                            CgRowTitle(tMov, trailing = "${s.movies.size}")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(s.movies, key = { it.id }) { m -> val l = p.isMovieLocked(m); CgPoster(m.name, if (l) null else m.posterUrl, m.year, { p.onMovieClick(m) }, Modifier.width(120.dp), locked = l, onLongClick = { p.onMovieLongClick(m) }) }
                            }
                        }
                    }
                    if (s.series.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.SERIES)) item {
                        Column {
                            CgRowTitle(tSer, trailing = "${s.series.size}")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(s.series, key = { it.id }) { m -> val l = p.isSeriesLocked(m); CgPoster(m.name, if (l) null else m.posterUrl, m.genre, { p.onSeriesClick(m) }, Modifier.width(120.dp), locked = l, onLongClick = { p.onSeriesLongClick(m) }) }
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

/** Favorites: receiver "bookmarks". START serif collection list + filter/sort text menus; END = framed grid
 *  with continue-watching fanart tiles and recent live plates on top. */
@Composable
internal fun ChatGpt2Favorites(p: FavoritesParams) {
    val tCw = tr("Continue watching", "متابعة المشاهدة"); val tLive = tr("Recent live", "مباشر مؤخراً")
    val tEmpty = tr("Nothing saved yet. Long-press any title to add it.", "لا يوجد شيء محفوظ. اضغط مطولاً على أي عنصر لإضافته.")
    val tCol = tr("Collections", "المجموعات"); val tFil = tr("Filter", "تصفية"); val tSort = tr("Sort", "ترتيب")
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        LazyColumn(Modifier.width(240.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            item { CgHeading(tCol, size = 12, modifier = Modifier.padding(bottom = 6.dp)) }
            items(SavedLibraryPreset.entries) { pr ->
                val sel = pr == p.selectedPreset
                CgCard(onClick = { p.onPresetSelected(pr) }, shape = CG.RSmall, container = if (sel) CG.AmberDeep else Color.Transparent, modifier = Modifier.fillMaxWidth()) {
                    Text(presetLabel(pr), color = if (sel) CG.Amber else CG.Text, fontSize = 16.sp, fontFamily = CG.Serif, modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp))
                }
            }
            item { CgHeading(tFil, size = 10, color = CG.Faint, modifier = Modifier.padding(top = 14.dp, bottom = 2.dp)) }
            items(SavedLibraryFilter.entries) { f -> CgTab(f.name.lowercase().replaceFirstChar { it.uppercase() }, f == p.selectedFilter, { p.onFilterSelected(f) }) }
            item { CgHeading(tSort, size = 10, color = CG.Faint, modifier = Modifier.padding(top = 14.dp, bottom = 2.dp)) }
            items(SavedLibrarySort.entries) { o -> CgTab(o.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }, o == p.selectedSort, { p.onSortSelected(o) }) }
        }
        LazyVerticalGrid(GridCells.Adaptive(130.dp), Modifier.weight(1f).fillMaxHeight(), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(4.dp, 4.dp, 4.dp, 40.dp)) {
            if (p.continueWatching.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) { CgRowTitle(tCw) }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(p.continueWatching, key = { "c${it.history.id}" }) { h ->
                            val prog = if (h.history.totalDurationMs > 0) h.history.resumePositionMs.toFloat() / h.history.totalDurationMs else null
                            CgWide(h.title, h.history.posterUrl, h.subtitle, prog, { p.onHistoryClick(h) }, Modifier.width(230.dp))
                        }
                    }
                }
            }
            if (p.recentLive.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) { CgRowTitle(tLive) }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(p.recentLive, key = { "l${it.history.id}" }) { h ->
                            CgCard(onClick = { p.onHistoryClick(h) }, modifier = Modifier.width(210.dp), container = CG.Raised) {
                                Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    CgLogo(h.title, h.history.posterUrl, 38.dp)
                                    Column { Text(h.title, color = CG.Text, fontSize = 13.sp, fontFamily = CG.Serif, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(h.subtitle, color = CG.Faint, fontSize = 11.sp, maxLines = 1) }
                                }
                            }
                        }
                    }
                }
            }
            val sections = p.sections.filter { it.items.isNotEmpty() }
            if (sections.isEmpty() && p.continueWatching.isEmpty() && p.recentLive.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) { CgEmpty(tEmpty) }
            sections.forEach { section ->
                item(key = "h${section.key}", span = { GridItemSpan(maxLineSpan) }) { CgRowTitle(section.title, trailing = "${section.items.size}") }
                items(section.items, key = { "${section.key}${it.favorite.contentType}${it.favorite.contentId}" }) { f ->
                    CgPoster(f.title, null, f.subtitle, { p.onItemClick(f) }, onLongClick = { p.onItemLongClick(f) })
                }
            }
        }
    }
}

/** Settings: receiver setup menu. START column of roman-numbered serif entries; content in a framed panel
 *  with a gold header bar, separated by a brass gradient rule. */
@Composable
internal fun ChatGpt2SettingsNav(p: SettingsNavParams) {
    val roman = listOf("I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII", "XIII", "XIV", "XV", "XVI")
    val tSetup = tr("Setup", "الإعداد")
    LazyColumn(Modifier.width(280.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(1.dp), contentPadding = PaddingValues(vertical = 6.dp)) {
        item { CgHeading(tSetup, size = 13, modifier = Modifier.padding(start = 8.dp, bottom = 10.dp)) }
        items(p.entries.size) { i ->
            val (label, _) = p.entries[i]
            val sel = i == p.selectedCategory
            CgCard(onClick = { p.onCategorySelected(i) }, shape = CG.RSmall, container = if (sel) CG.AmberDeep else Color.Transparent,
                modifier = Modifier.fillMaxWidth().then(if (sel) Modifier.focusRequester(p.focusRequester) else Modifier)) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(roman.getOrElse(i) { "${i + 1}" }, color = CG.Amber, fontSize = 13.sp, fontFamily = CG.Serif, modifier = Modifier.width(40.dp))
                    Text(label, color = if (sel) CG.Amber else CG.Text, fontSize = 16.sp, fontFamily = CG.Serif, maxLines = 1, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
internal fun ChatGpt2SettingsFrame(navigation: @Composable () -> Unit, content: @Composable () -> Unit) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        navigation()
        Box(Modifier.width(1.dp).fillMaxHeight().background(Brush.verticalGradient(listOf(Color.Transparent, CG.Amber, Color.Transparent))))
        Column(Modifier.weight(1f).fillMaxHeight().border(1.dp, CG.Line, CG.R)) {
            Box(Modifier.fillMaxWidth().height(4.dp).background(CG.Amber))
            Box(Modifier.fillMaxSize().background(CG.Raised).padding(22.dp)) { content() }
        }
    }
}


private val cgArabicRows = listOf(
    listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج"),
    listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م", "ك", "ط"),
    listOf("ئ", "ء", "ؤ", "ر", "ى", "ة", "و", "ز", "ظ", "د", "ذ")
)
private val cgDigitRow = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "أ")

/** On-screen Arabic keyboard (TV remotes have no letters): glossy navy keys, blue glow on focus. */
@Composable
private fun CgArabicKeyboard(onKey: (String) -> Unit, onBackspace: () -> Unit, onSearch: () -> Unit) {
    var digits by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().background(CG.Raised.copy(alpha = 0.7f), CG.R).padding(8.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        (if (digits) listOf(cgDigitRow) + cgArabicRows.drop(1) else cgArabicRows).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEach { k -> CgKey(k, Modifier.weight(1f)) { onKey(k) } }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            CgKey(if (digits) "ابج" else "123", Modifier.weight(1.4f)) { digits = !digits }
            CgKey("␣", Modifier.weight(3f)) { onKey(" ") }
            CgKey("⌫", Modifier.weight(1.3f)) { onBackspace() }
            CgKey("⌕", Modifier.weight(1.3f), accent = true) { onSearch() }
        }
    }
}

@Composable
private fun CgKey(label: String, modifier: Modifier, accent: Boolean = false, onClick: () -> Unit) {
    CgCard(onClick = onClick, container = if (accent) CG.AmberDeep else CG.Card, shape = CG.RSmall, zoom = 1.08f, modifier = modifier.height(34.dp)) {
        Text(label, color = CG.Text, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
    }
}
