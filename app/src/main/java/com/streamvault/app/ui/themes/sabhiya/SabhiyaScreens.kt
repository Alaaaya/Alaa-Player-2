package com.streamvault.app.ui.themes.sabhiya

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
            colors = ClickableSurfaceDefaults.colors(containerColor = Color(0x99182A5C), focusedContainerColor = CG.Amber, contentColor = if (active) Color(0xFFFF8A90) else CG.Text, focusedContentColor = Color.White),
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
        modifier = Modifier.background(Color(0x66182A5C), RoundedCornerShape(50)).border(1.dp, CG.Line, RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 4.dp))
}

/** Movie (Cyan Premium): full-bleed backdrop, framed poster card on the visual side, title block with
 *  4K/HDR/HD badges, red "مشاهدة الآن" + dark buttons row, info grid, versions pills and a poster rail. */
@Composable
internal fun SabhiyaMovieDetail(p: MovieDetailParams) {
    val m = p.movie
    val play = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(m.id) { runCatching { play.requestFocus() } }
    CpDetailBackdrop(m.backdropUrl ?: m.posterUrl) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 56.dp, vertical = 36.dp), verticalArrangement = Arrangement.spacedBy(22.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(36.dp), verticalAlignment = Alignment.Top) {
                CpPosterFrame(m.posterUrl ?: m.backdropUrl, m.name)
                Column(Modifier.weight(1f).padding(top = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        CpTag(tr("MOVIE", "فيلم"))
                        cg2QualityTags(m.variantLabel ?: m.name).forEach { it() }
                    }
                    Text(m.name, color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Black, maxLines = 2, lineHeight = 54.sp, overflow = TextOverflow.Ellipsis)
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        CpRating(m.rating, 16)
                        Meta(m.year, m.duration, m.genre?.split(',', '/', '|')?.map { it.trim() }?.filter { it.isNotBlank() }?.take(3)?.joinToString(" · "))
                    }
                    Text(m.plot.orEmpty(), color = Color.White.copy(alpha = 0.86f), fontSize = 16.sp, maxLines = 4, overflow = TextOverflow.Ellipsis, lineHeight = 24.sp, modifier = Modifier.widthIn(max = 860.dp))
                    if (p.hasResume) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CgGlyph("◷", 18.dp, tint = CG.Amber)
                        Text(tr("Resume from", "استكمال من") + " " + cgDuration(p.resumePositionMs), color = Color.White, fontSize = 14.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        CpButton(if (p.hasResume) tr("Resume", "استكمال") else tr("Watch now", "مشاهدة الآن"), "▶", p.onPlay, Modifier.focusRequester(play))
                        p.onPlayTrailer?.let { CpButton(tr("Trailer", "الإعلان"), "film", it, primary = false) }
                        CpButton(if (m.isFavorite) tr("In favorites", "في المفضلة") else tr("Add to favorites", "أضف للمفضلة"), if (m.isFavorite) "♥" else "♡", p.onToggleFavorite, primary = false)
                        CpIconKey("⎚", p.onCast); CpIconKey("↓", p.onDownload); CpIconKey("⧉", p.onCopyUrl); CpIconKey("←", p.onBack)
                    }
                    Column(Modifier.widthIn(max = 860.dp).cg2Panel().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CpInfoLine(tr("Director", "المخرج"), m.director)
                        CpInfoLine(tr("Cast", "الممثلون"), m.cast)
                        CpInfoLine(tr("Genre", "النوع"), m.genre)
                    }
                }
            }
            if (m.variants.size > 1) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(tr("Versions", "النسخ"), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                m.variants.forEach { v -> CpPill(v.label, v.rawMovieId == m.selectedVariantId) { p.onSelectVariant(v.rawMovieId) } }
            }
            if (p.relatedContent.isNotEmpty()) Column {
                CpSectionTitle(tr("More like this", "أفلام مشابهة"))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 6.dp)) {
                    items(p.relatedContent, key = { it.id }) { r -> CpGridPoster(r.name, r.posterUrl ?: r.backdropUrl, r.year, r.rating, { p.onRelatedClick(r) }, {}, Modifier.width(150.dp)) }
                }
            }
        }
    }
}

/** Series (Cyan Premium): backdrop hero row (poster frame + info + red buttons), season pills row,
 *  then episodes as a 4-column grid of 16:9 stills with number badge, progress and title. */
@Composable
internal fun SabhiyaSeriesDetail(p: SeriesDetailParams) {
    val s = p.series
    val primary = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(s.id) { runCatching { primary.requestFocus() } }
    val tSeason = tr("Season", "الموسم")
    val seasonLabel: (com.streamvault.domain.model.Season) -> String = { it.name.ifBlank { "$tSeason ${it.seasonNumber}" } }
    val resume = p.resumeEpisode
    CpDetailBackdrop(s.backdropUrl ?: s.posterUrl) {
        LazyVerticalGrid(
            GridCells.Fixed(4), Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 56.dp, vertical = 30.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                    CpPosterFrame(s.posterUrl ?: s.backdropUrl, s.name, 230.dp)
                    Column(Modifier.weight(1f).padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            CpTag(tr("SERIES", "مسلسل"))
                            if (p.unwatchedEpisodeCount > 0) CpTag("${p.unwatchedEpisodeCount} " + tr("new", "جديد"), bg = Color(0xFF22C55E))
                            Text("${s.seasons.size} " + tr("seasons", "مواسم"), color = CG.Sub, fontSize = 13.sp)
                        }
                        Text(s.name, color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            CpRating(s.rating, 15)
                            Meta(s.releaseDate?.take(4), s.genre?.split(',', '/', '|')?.map { it.trim() }?.filter { it.isNotBlank() }?.take(3)?.joinToString(" · "))
                            if (!p.isLoadingExternalRatings) {
                                val r = p.externalRatings
                                listOf("IMDb" to r.imdb, "RT" to r.rottenTomatoes, "MC" to r.metacritic).filter { it.second.available }
                                    .forEach { CpTag("${it.first} ${it.second.displayValue}", fg = Color.White, outlined = true) }
                            }
                        }
                        Text(s.plot.orEmpty(), color = Color.White.copy(alpha = 0.86f), fontSize = 15.sp, maxLines = 3, overflow = TextOverflow.Ellipsis, lineHeight = 22.sp, modifier = Modifier.widthIn(max = 900.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            CpButton(
                                if (resume != null) tr("Continue", "متابعة") + " S${resume.seasonNumber}E${resume.episodeNumber}" else tr("Watch now", "مشاهدة الآن"), "▶",
                                { (resume ?: p.selectedSeason?.episodes?.firstOrNull())?.let(p.onResumeClick) }, Modifier.focusRequester(primary)
                            )
                            CpButton(if (s.isFavorite) tr("In favorites", "في المفضلة") else tr("Add to favorites", "أضف للمفضلة"), if (s.isFavorite) "♥" else "♡", p.onToggleFavorite, primary = false)
                            if (resume != null) CpIconKey("⎚", p.onCastResumeEpisode)
                            CpIconKey("←", p.onBack)
                        }
                    }
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(tr("Seasons", "المواسم"), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    LazyRow(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(s.seasons, key = { it.seasonNumber }) { season -> CpPill(seasonLabel(season), season.seasonNumber == p.selectedSeason?.seasonNumber) { p.onSeasonSelected(season) } }
                    }
                    p.selectedSeason?.let { Text("${it.episodes.size} " + tr("episodes", "حلقة"), color = CG.Sub, fontSize = 13.sp) }
                }
            }
            if (s.variants.size > 1) item(span = { GridItemSpan(maxLineSpan) }) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(tr("Versions", "النسخ"), color = CG.Sub, fontSize = 14.sp)
                    s.variants.forEach { v -> CpPill(v.label, v.rawSeriesId == s.selectedVariantId) { p.onSelectVariant(v.rawSeriesId) } }
                }
            }
            items(p.selectedSeason?.episodes.orEmpty(), key = { it.id }) { e ->
                CgCard(onClick = { p.onEpisodeClick(e) }, onLongClick = { p.onCopyEpisodeUrl(e) }, container = Color(0xFF17121F), shape = RoundedCornerShape(10.dp), zoom = 1.05f) {
                    Column {
                        Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f).background(CG.Card)) {
                            (e.coverUrl ?: p.selectedSeason?.coverUrl ?: s.backdropUrl)?.let { AsyncImage(it, e.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xAA000000)))))
                            Box(Modifier.align(Alignment.TopStart).padding(8.dp)) { CpTag("E${e.episodeNumber}") }
                            Box(Modifier.align(Alignment.Center).size(42.dp).clip(CircleShape).background(Color(0xCCE50914)), contentAlignment = Alignment.Center) { CgGlyph("▶", 24.dp, tint = Color.White) }
                            e.duration?.let { Text(it, color = Color.White, fontSize = 11.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)) }
                            if (e.watchProgress > 0 && e.durationSeconds > 0) Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(3.dp).background(Color(0x55FFFFFF))) {
                                Box(Modifier.fillMaxHeight().fillMaxWidth((e.watchProgress / (e.durationSeconds * 1000f)).coerceIn(0f, 1f)).background(CG.Amber))
                            }
                        }
                        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(e.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(e.plot.orEmpty(), color = CG.Sub, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 15.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CpDetailBackdrop(url: String?, content: @Composable BoxScope.() -> Unit) {
    Box(Modifier.fillMaxSize().background(CG.Bg)) {
        url?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xF50E0C12), Color(0xD90E0C12), Color(0x660E0C12)))))
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.4f to Color.Transparent, 1f to CG.Bg)))
        Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0x33E50914), Color.Transparent), radius = 1400f)))
        content()
    }
}

@Composable
private fun CpPosterFrame(url: String?, title: String, width: androidx.compose.ui.unit.Dp = 280.dp) {
    Box(Modifier.width(width).aspectRatio(0.68f).clip(RoundedCornerShape(14.dp)).background(CG.Card).border(1.dp, Color(0x55FF8A90), RoundedCornerShape(14.dp))) {
        if (url != null) AsyncImage(url, title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        else Text(title.take(1), color = CG.Amber, fontSize = 60.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
private fun CpInfoLine(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, color = CG.Amber, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(90.dp))
        Text(value, color = Color.White, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

/** Rounded selectable pill: filled red when selected, dark outline otherwise, red glow on focus. */
@Composable
internal fun CpPill(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    TvClickableSurface(
        onClick = onClick, modifier = modifier, shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = if (selected) CG.Amber else Color(0xCC14224E), focusedContainerColor = if (selected) Color(0xFFFF4D57) else Color(0xFFE50914), contentColor = Color.White, focusedContentColor = Color.White),
        border = ClickableSurfaceDefaults.border(
            border = Border(androidx.compose.foundation.BorderStroke(1.dp, if (selected) Color.Transparent else Color(0xFF332848)), shape = shape),
            focusedBorder = Border(androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFF8A90)), shape = shape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
        glow = ClickableSurfaceDefaults.glow(focusedGlow = Glow(CG.Amber.copy(alpha = 0.55f), 12.dp))
    ) { Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) }
}

/** Quality badges parsed from a stream/variant label: yellow 4K, outlined HDR, red HD/FHD. Nothing if unknown. */
internal fun cg2QualityTags(label: String?): List<@Composable () -> Unit> {
    val l = label?.uppercase() ?: return emptyList()
    val out = mutableListOf<@Composable () -> Unit>()
    when {
        "4K" in l || "UHD" in l || "2160" in l -> out += { CpTag("4K", bg = Color(0xFFFFC107), fg = Color.Black) }
        "FHD" in l || "1080" in l -> out += { CpTag("FHD") }
        Regex("\\bHD\\b").containsMatchIn(l) || "720" in l -> out += { CpTag("HD") }
    }
    if ("HDR" in l || "DOLBY" in l) out += { CpTag("HDR", fg = Color.White, outlined = true) }
    return out
}

/** Search (Cyan Premium): wide red-focus field + scope pills on top; START = big on-screen Arabic keyboard
 *  with recent-query chips; END = results (channel rows with now/LIVE/quality, then poster grids). */
@Composable
internal fun SabhiyaSearch(p: SearchParams) {
    LaunchedEffect(Unit) { runCatching { p.searchFocusRequester.requestFocus() } }
    val s = p.uiState
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CgSearchField(p.query, p.onQueryChange, tr("Search movies, series, channels…", "ابحث عن فيلم، مسلسل أو قناة…"), Modifier.weight(1f).focusRequester(p.searchFocusRequester), onSubmit = p.onSearch)
            SearchTab.entries.forEach { t ->
                val label = when (t.name) { "ALL" -> tr("All", "الكل"); "LIVE" -> tr("Live", "مباشر"); "MOVIES" -> tr("Movies", "أفلام"); "SERIES" -> tr("Series", "مسلسلات"); else -> t.name }
                val count = when (t.name) { "LIVE" -> s.channels.size; "MOVIES" -> s.movies.size; "SERIES" -> s.series.size; else -> s.channels.size + s.movies.size + s.series.size }
                CpPill(if (p.query.isNotBlank() && !s.isLoading) "$label  $count" else label, t == p.selectedTab) { p.onTabSelected(t) }
            }
        }
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(22.dp)) {
            Column(Modifier.width(560.dp).fillMaxHeight().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CgArabicKeyboard(onKey = { p.onQueryChange(p.query + it) }, onBackspace = { p.onQueryChange(p.query.dropLast(1)) }, onSearch = p.onSearch, onClear = { p.onQueryChange("") })
                if (p.recentQueries.isNotEmpty()) Column(Modifier.fillMaxWidth().cg2Panel().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CgGlyph("◷", 18.dp, tint = CG.Amber); Spacer(Modifier.width(8.dp))
                        Text(tr("Recent searches", "عمليات البحث الأخيرة"), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        CpPill(tr("Clear", "مسح"), false, onClick = p.onClearRecentQueries)
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(p.recentQueries) { q -> CpPill(q, false) { p.onRecentQuerySelected(q) } } }
                }
                CpButton(tr("Build full index", "فهرسة كاملة للمحتوى"), "refresh", p.onBuildCompleteIndex, primary = false)
            }
            Box(Modifier.weight(1f).fillMaxHeight()) {
                when {
                    p.query.isBlank() -> CpEmptyState("⌕", tr("Type to search", "اكتب للبحث"), tr("Use the keyboard or your remote's voice search", "استخدم لوحة المفاتيح أو البحث الصوتي"))
                    s.isLoading -> CpEmptyState("refresh", tr("Searching…", "جار البحث…"), null)
                    s.hasSearchError -> CpEmptyState("!", tr("Search failed. Try again.", "فشل البحث. حاول مرة أخرى."), null)
                    s.isEmpty -> CpEmptyState("⌕", tr("No results for", "لا نتائج لـ") + " “${p.query}”", null)
                    else -> LazyVerticalGrid(GridCells.Fixed(5), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 40.dp)) {
                        if (s.channels.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.LIVE)) {
                            item(span = { GridItemSpan(maxLineSpan) }) { CpSectionTitle(tr("Live channels", "القنوات المباشرة") + "  (${s.channels.size})") }
                            items(s.channels.take(if (p.selectedTab == SearchTab.LIVE) 300 else 5), key = { "c${it.id}" }, span = { GridItemSpan(maxLineSpan) }) { c ->
                                val locked = p.isChannelLocked(c)
                                val prog = c.currentProgram
                                val now = System.currentTimeMillis()
                                CgCard(onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c) }, container = Color(0xFF17121F), shape = RoundedCornerShape(10.dp), zoom = 1.01f, modifier = Modifier.fillMaxWidth()) {
                                    Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                        CgLogo(c.name, if (locked) null else c.logoUrl, 40.dp)
                                        Text(c.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(240.dp))
                                        if (locked) CgGlyph("lock", 16.dp, tint = CG.Sub)
                                        Text(prog?.title.orEmpty(), color = CG.Sub, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                        if (prog != null && prog.startTime <= now && prog.endTime > now) CpTag("LIVE")
                                        if (c.id in p.recordingChannelIds) CpTag("REC") else if (c.id in p.scheduledChannelIds) CgGlyph("◷", 16.dp, tint = CG.Amber)
                                        c.qualityBadge()?.let { CpTag(it, fg = Color.White, outlined = true) }
                                    }
                                }
                            }
                        }
                        if (s.movies.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.MOVIES)) {
                            item(span = { GridItemSpan(maxLineSpan) }) { CpSectionTitle(tr("Movies", "الأفلام") + "  (${s.movies.size})") }
                            items(s.movies.take(if (p.selectedTab == SearchTab.MOVIES) 300 else 10), key = { "m${it.id}" }) { m ->
                                val l = p.isMovieLocked(m)
                                CpGridPoster(m.name, if (l) null else m.posterUrl, m.year, m.rating, { p.onMovieClick(m) }, { p.onMovieLongClick(m) }, quality = if (l) null else listOfNotNull(m.variantLabel, m.name).joinToString(" "))
                            }
                        }
                        if (s.series.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.SERIES)) {
                            item(span = { GridItemSpan(maxLineSpan) }) { CpSectionTitle(tr("Series", "المسلسلات") + "  (${s.series.size})") }
                            items(s.series.take(if (p.selectedTab == SearchTab.SERIES) 300 else 10), key = { "s${it.id}" }) { m ->
                                val l = p.isSeriesLocked(m)
                                CpGridPoster(m.name, if (l) null else m.posterUrl, m.genre?.substringBefore(","), m.rating, { p.onSeriesClick(m) }, { p.onSeriesLongClick(m) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CpEmptyState(glyph: String, title: String, sub: String?) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(Modifier.size(96.dp).clip(CircleShape).background(Color(0x22E50914)).border(1.dp, Color(0x66E50914), CircleShape), contentAlignment = Alignment.Center) { CgGlyph(glyph, 42.dp, tint = CG.Amber) }
        Spacer(Modifier.height(16.dp))
        Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        sub?.let { Text(it, color = CG.Sub, fontSize = 14.sp, modifier = Modifier.padding(top = 6.dp)) }
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

@Composable
private fun filterLabel(f: SavedLibraryFilter) = when (f.name) {
    "ALL" -> tr("All", "الكل"); "LIVE" -> tr("Live", "مباشر"); "MOVIE", "MOVIES" -> tr("Movies", "أفلام"); "SERIES" -> tr("Series", "مسلسلات")
    else -> f.name.lowercase().replaceFirstChar { it.uppercase() }
}

@Composable
private fun sortLabel(o: SavedLibrarySort) = when (o.name) {
    "SAVED_ORDER" -> tr("Saved order", "ترتيب الحفظ"); "RECENTLY_WATCHED" -> tr("Recently watched", "آخر مشاهدة")
    "TITLE" -> tr("Title", "الاسم"); "TITLE_ASC" -> tr("A-Z", "أ-ي")
    else -> o.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }
}

private fun favGlyph(t: com.streamvault.domain.model.ContentType) = when (t.name) { "LIVE" -> "📺"; "SERIES", "SERIES_EPISODE" -> "clap"; else -> "film" }

/** Favorites / Recent (Cyan Premium). When opened from "المشاهدة الأخيرة" this renders the real playback history
 *  (continue-watching cards with progress + recently watched live channels); otherwise the saved library with
 *  collection pills, filter/sort pills and a red-focus grid. */
@Composable
internal fun SabhiyaFavorites(p: FavoritesParams) {
    if (SbRecent.active) { CpRecentScreen(p); return }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CgGlyph("♥", 30.dp, tint = CG.Amber)
            Text(tr("Favorites", "المفضلة"), color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
            Text("${p.sections.sumOf { it.items.size }} " + tr("items", "عنصر"), color = CG.Sub, fontSize = 15.sp)
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(SavedLibraryPreset.entries) { pr -> CpPill(presetLabel(pr), pr == p.selectedPreset) { p.onPresetSelected(pr) } } }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CgGlyph("filter", 18.dp, tint = CG.Sub)
            SavedLibraryFilter.entries.forEach { f -> CpPill(filterLabel(f), f == p.selectedFilter) { p.onFilterSelected(f) } }
            Spacer(Modifier.width(18.dp)); CgGlyph("sort", 18.dp, tint = CG.Sub)
            SavedLibrarySort.entries.forEach { o -> CpPill(sortLabel(o), o == p.selectedSort) { p.onSortSelected(o) } }
        }
        val sections = p.sections.filter { it.items.isNotEmpty() }
        if (sections.isEmpty()) CpEmptyState("♡", tr("Nothing saved yet", "لا يوجد شيء في المفضلة"), tr("Long-press any title or channel to add it", "اضغط مطولاً على أي فيلم أو قناة لإضافتها"))
        else LazyVerticalGrid(GridCells.Fixed(4), Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(4.dp, 4.dp, 4.dp, 40.dp)) {
            sections.forEach { section ->
                item(key = "h${section.key}", span = { GridItemSpan(maxLineSpan) }) { CpSectionTitle(section.title + "  (${section.items.size})") }
                items(section.items, key = { "${section.key}${it.favorite.contentType}${it.favorite.contentId}" }) { f ->
                    CgCard(onClick = { p.onItemClick(f) }, onLongClick = { p.onItemLongClick(f) }, container = Color(0xFF17121F), shape = RoundedCornerShape(12.dp), zoom = 1.05f) {
                        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                          Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Box(Modifier.size(46.dp).clip(RoundedCornerShape(10.dp)).background(Brush.linearGradient(listOf(Color(0xFFB20710), Color(0xFF14101C)))), contentAlignment = Alignment.Center) {
                                CgGlyph(favGlyph(f.favorite.contentType), 24.dp, tint = Color.White)
                            }
                            CgGlyph("♥", 18.dp, tint = CG.Amber)
                          }
                            Column(Modifier.fillMaxWidth()) {
                                Text(f.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                f.subtitle?.let { Text(it, color = CG.Sub, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CpRecentScreen(p: FavoritesParams) {
    val df = remember { java.text.SimpleDateFormat("d MMM  HH:mm", java.util.Locale("ar")) }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CgGlyph("◷", 30.dp, tint = CG.Amber)
            Text(tr("Recently watched", "المشاهدة الأخيرة"), color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
            Text("${p.continueWatching.size + p.recentLive.size} " + tr("items", "عنصر"), color = CG.Sub, fontSize = 15.sp)
        }
        if (p.continueWatching.isEmpty() && p.recentLive.isEmpty()) { CpEmptyState("◷", tr("No history yet", "لا يوجد سجل مشاهدة بعد"), tr("What you watch shows up here", "ما تشاهده سيظهر هنا")); return }
        LazyVerticalGrid(GridCells.Fixed(4), Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(4.dp, 4.dp, 4.dp, 40.dp)) {
            if (p.continueWatching.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) { CpSectionTitle(tr("Continue watching", "متابعة المشاهدة")) }
                items(p.continueWatching, key = { "c${it.history.id}" }) { h ->
                    val prog = if (h.history.totalDurationMs > 0) (h.history.resumePositionMs.toFloat() / h.history.totalDurationMs).coerceIn(0f, 1f) else 0f
                    CgCard(onClick = { p.onHistoryClick(h) }, container = Color(0xFF17121F), shape = RoundedCornerShape(12.dp), zoom = 1.05f) {
                        Column {
                            Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f).background(CG.Raised)) {
                                h.history.posterUrl?.let { AsyncImage(it, h.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xCC000000)))))
                                Box(Modifier.align(Alignment.Center).size(46.dp).clip(CircleShape).background(Color(0xCCE50914)), contentAlignment = Alignment.Center) { CgGlyph("▶", 26.dp, tint = Color.White) }
                                if (h.history.totalDurationMs > 0) Text(cgDuration((h.history.totalDurationMs - h.history.resumePositionMs).coerceAtLeast(0)) + " " + tr("left", "متبقي"), color = Color.White, fontSize = 11.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp))
                                Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(4.dp).background(Color(0x55FFFFFF))) { Box(Modifier.fillMaxHeight().fillMaxWidth(prog).background(CG.Amber)) }
                            }
                            Column(Modifier.padding(10.dp)) {
                                Text(h.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(h.subtitle, color = CG.Sub, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
            if (p.recentLive.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) { CpSectionTitle(tr("Recently watched channels", "قنوات شوهدت مؤخراً")) }
                items(p.recentLive, key = { "l${it.history.id}" }) { h ->
                    CgCard(onClick = { p.onHistoryClick(h) }, container = Color(0xFF17121F), shape = RoundedCornerShape(12.dp), zoom = 1.05f) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CgLogo(h.title, h.history.posterUrl, 48.dp)
                            Column(Modifier.weight(1f)) {
                                Text(h.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(if (h.history.lastWatchedAt > 0) df.format(java.util.Date(h.history.lastWatchedAt)) else h.subtitle, color = CG.Sub, fontSize = 12.sp, maxLines = 1)
                            }
                            CpTag("LIVE")
                        }
                    }
                }
            }
        }
    }
}

private fun settingsGlyph(label: String): String {
    val l = label.lowercase()
    return when {
        "playlist" in l || "provider" in l || "قائم" in l || "مزود" in l || "حساب" in l -> "playlist"
        "play" in l || "تشغيل" in l || "مشغل" in l -> "▶"
        "epg" in l || "guide" in l || "دليل" in l -> "guide"
        "parent" in l || "lock" in l || "أبو" in l || "قفل" in l -> "lock"
        "theme" in l || "appear" in l || "مظهر" in l || "ثيم" in l -> "theme"
        "lang" in l || "لغة" in l -> "lang"
        "record" in l || "تسجيل" in l -> "rec"
        "backup" in l || "نسخ" in l -> "backup"
        "about" in l || "حول" in l || "info" in l -> "info"
        "panel" in l || "device" in l || "جهاز" in l -> "server"
        else -> "⚙"
    }
}

/** Settings nav (Cyan Premium): header with logo + gear, entries as icon rows; selected = cyan glowing pill. */
@Composable
internal fun SabhiyaSettingsNav(p: SettingsNavParams) {
    LazyColumn(Modifier.width(330.dp).fillMaxHeight().cg2Panel().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        item {
            Row(Modifier.padding(start = 8.dp, top = 4.dp, bottom = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CgGlyph("⚙", 26.dp, tint = CG.Amber)
                Text(tr("Settings", "الإعدادات"), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
            }
        }
        items(p.entries.size) { i ->
            val (label, sub) = p.entries[i]
            val sel = i == p.selectedCategory
            val shape = RoundedCornerShape(12.dp)
            TvClickableSurface(
                onClick = { p.onCategorySelected(i) }, modifier = Modifier.fillMaxWidth().then(if (sel) Modifier.focusRequester(p.focusRequester) else Modifier),
                shape = ClickableSurfaceDefaults.shape(shape),
                colors = ClickableSurfaceDefaults.colors(containerColor = if (sel) CG.Amber else Color.Transparent, focusedContainerColor = if (sel) Color(0xFFFF4D57) else Color(0xFFE50914), contentColor = Color.White, focusedContentColor = Color.White),
                border = ClickableSurfaceDefaults.border(focusedBorder = Border(androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFF8A90)), shape = shape)),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1.03f),
                glow = ClickableSurfaceDefaults.glow(glow = if (sel) Glow(CG.Amber.copy(alpha = 0.4f), 10.dp) else Glow.None, focusedGlow = Glow(CG.Amber.copy(alpha = 0.55f), 14.dp))
            ) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CgGlyph(settingsGlyph(label + " " + sub), 22.dp, tint = Color.White)
                    Column(Modifier.weight(1f)) {
                        Text(label, fontSize = 16.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (sub.isNotBlank()) Text(sub, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    CgGlyph("‹", 18.dp, tint = Color.White.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
internal fun SabhiyaSettingsFrame(navigation: @Composable () -> Unit, content: @Composable () -> Unit) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        navigation()
        Box(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(16.dp)).background(Color(0xFF121016)).border(1.dp, Color(0xFF401A1D), RoundedCornerShape(16.dp))) {
            Box(Modifier.fillMaxWidth().height(160.dp).background(Brush.verticalGradient(listOf(Color(0x33E50914), Color.Transparent))))
            Box(Modifier.fillMaxSize().padding(26.dp)) { content() }
        }
    }
}

private val cgArabicRows = listOf(
    listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج"),
    listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م", "ك", "ط"),
    listOf("ئ", "ء", "ؤ", "ر", "ى", "ة", "و", "ز", "ظ", "د", "ذ")
)
private val cgDigitRow = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "أ")
private val cgLatinRows = listOf(
    listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"),
    listOf("A", "S", "D", "F", "G", "H", "J", "K", "L", "-"),
    listOf("Z", "X", "C", "V", "B", "N", "M", ".", "'", "&")
)

/** On-screen keyboard: Arabic (default) / English / digits, dark keys with red focus, red search key. */
@Composable
private fun CgArabicKeyboard(onKey: (String) -> Unit, onBackspace: () -> Unit, onSearch: () -> Unit, onClear: () -> Unit) {
    var mode by remember { mutableStateOf(0) } // 0 ar, 1 en
    var digits by remember { mutableStateOf(false) }
    val rows = (if (digits) listOf(cgDigitRow) else emptyList()) + (if (mode == 0) cgArabicRows else cgLatinRows)
    Column(Modifier.fillMaxWidth().cg2Panel().padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        rows.forEach { row -> Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { row.forEach { k -> CgKey(k, Modifier.weight(1f)) { onKey(if (mode == 1) k.lowercase() else k) } } } }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CgKey(if (mode == 0) "EN" else "ع", Modifier.weight(1.1f)) { mode = 1 - mode }
            CgKey("123", Modifier.weight(1.1f), on = digits) { digits = !digits }
            CgKey(tr("space", "مسافة"), Modifier.weight(3f)) { onKey(" ") }
            CgKey("⌫", Modifier.weight(1.1f)) { onBackspace() }
            CgKey(tr("Clear", "مسح"), Modifier.weight(1.2f)) { onClear() }
            CgKey("⌕", Modifier.weight(1.4f), accent = true) { onSearch() }
        }
    }
}

@Composable
private fun CgKey(label: String, modifier: Modifier, accent: Boolean = false, on: Boolean = false, onClick: () -> Unit) {
    CgCard(onClick = onClick, container = if (accent || on) CG.Amber else Color(0xFF1A1424), shape = RoundedCornerShape(8.dp), zoom = 1.1f, modifier = modifier.height(46.dp)) {
        if (label == "⌕" || label == "⌫") CgGlyph(label, 22.dp, Modifier.align(Alignment.Center), tint = Color.White)
        else Text(label, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.align(Alignment.Center))
    }
}
