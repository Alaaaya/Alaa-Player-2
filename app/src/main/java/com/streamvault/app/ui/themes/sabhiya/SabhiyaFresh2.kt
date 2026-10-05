package com.streamvault.app.ui.themes.sabhiya

import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.streamvault.app.ui.screens.favorites.SavedLibraryPreset
import com.streamvault.app.ui.screens.search.SearchTab
import com.streamvault.app.ui.themes.bespoke.*
import com.streamvault.domain.model.Episode

// ───────────────────────────── LIBRARY (Netflix rows) ─────────────────────────────

/** Movies / Series: genre chips, featured backdrop banner, Netflix-style rows per category, grid when a genre is selected. */
@Composable
internal fun <T> SbFreshLibrary(
    en: String, ar: String, p: LibraryParams<T>,
    id: (T) -> Any, name: (T) -> String, poster: (T) -> String?, backdrop: (T) -> String?,
    year: (T) -> String?, rating: (T) -> Float?, plot: (T) -> String?, badge: (T) -> String?, isMovie: Boolean
) {
    val s = p.uiState
    val nav = LocalCpNavigate.current
    val regions = if (isMovie) emptyList() else listOf("عربية" to listOf("arab", "عرب"), "تركية" to listOf("turk", "ترك"), "أجنبية" to listOf("foreign", "english", "us ", "أجنب"), "كورية" to listOf("korea", "كور"), "أنمي" to listOf("anime", "أنمي", "انمي"), "تاريخية" to listOf("histor", "تاريخ"))
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SbChip(tr("Movies", "الأفلام"), isMovie) { if (!isMovie) nav(com.streamvault.app.navigation.Routes.MOVIES) }
            SbChip(tr("Series", "المسلسلات"), !isMovie) { if (isMovie) nav(com.streamvault.app.navigation.Routes.SERIES) }
            if (regions.isNotEmpty()) Box(Modifier.width(1.dp).height(30.dp).background(SB.Line))
            regions.forEach { (label, keys) ->
                val match = s.categories.firstOrNull { c -> keys.any { k -> k in c.name.lowercase() } }
                SbChip(label, match != null && s.selectedCategory == match.name) { match?.let(p.onCategoryClick) }
            }
        }
      Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        Column(Modifier.width(240.dp).fillMaxHeight().clip(SB.R).background(SB.Panel).border(1.dp, SB.Line, SB.R).padding(10.dp)) {
            Text(tr("Genres", "التصنيفات"), color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                item { SbGenreRow(tr("All", "الكل"), s.isShowingAll, s.libraryCount, Modifier.focusRequester(p.initialFocusRequester), false) { p.onShowAll() } }
                items(s.categories, key = { "g${it.id}" }) { c -> SbGenreRow(c.name, s.selectedCategory == c.name, s.categoryCounts[c.name] ?: c.count, Modifier, p.isCategoryLocked(c)) { p.onCategoryClick(c) } }
            }
        }
      Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (s.isShowingAll) {
            val rows = s.categoryNames.mapNotNull { n -> s.itemsByCategory[n]?.takeIf { it.isNotEmpty() }?.let { n to it } }
            if (rows.isEmpty()) {
                if (s.libraryCount == 0 && s.categories.isEmpty()) SbEmpty(if (isMovie) Icons.Outlined.Movie else Icons.Outlined.VideoLibrary, tr("Nothing here yet", "لا يوجد محتوى بعد"), tr("Add or refresh a playlist", "أضف أو حدّث قائمة التشغيل"))
                else Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) { repeat(7) { SbSkeleton(Modifier.width(170.dp).aspectRatio(2f / 3f)) } }
                return@Column
            }
            val feat = rows.flatMap { it.second }.firstOrNull { backdrop(it) != null || poster(it) != null }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(26.dp), contentPadding = PaddingValues(bottom = 40.dp)) {
                feat?.let { f ->
                    item {
                        Box(Modifier.fillMaxWidth().height(300.dp).shadow(20.dp, SB.R).clip(SB.R).background(SB.Panel)) {
                            AsyncImage(backdrop(f) ?: poster(f), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xF20B0D11), Color(0x990B0D11), Color.Transparent))))
                            Column(Modifier.align(Alignment.CenterStart).fillMaxWidth(0.5f).padding(start = 40.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                SbTag(tr("Featured", "مميز"))
                                Text(name(f), color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Light, letterSpacing = 2.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    rating(f)?.takeIf { it > 0 }?.let { Text("★ %.1f".format(it), color = SB.Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                                    year(f)?.let { Text(it, color = SB.Sub, fontSize = 16.sp) }
                                    badge(f)?.let { SbTag(it, filled = false) }
                                }
                                plot(f)?.takeIf { it.isNotBlank() }?.let { Text(it, color = SB.Sub, fontSize = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis) }
                                SbButton(tr("Play", "تشغيل"), Icons.Outlined.PlayArrow, { p.onItemClick(f) })
                            }
                        }
                    }
                }
                if (s.continueWatching.isNotEmpty()) item {
                    SbRail(tr("Continue watching", "متابعة المشاهدة")) {
                        s.continueWatching.take(10).forEach { h -> SbWide(h.title, h.posterUrl, null, if (h.totalDurationMs > 0) h.resumePositionMs.toFloat() / h.totalDurationMs else null, { s.visibleItems.firstOrNull { name(it) == h.title }?.let(p.onItemClick) }) }
                    }
                }
                items(rows, key = { it.first }) { (cat, list) ->
                    Column {
                        SbRowTitle(cat, s.categoryCounts[cat])
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(vertical = 10.dp, horizontal = 6.dp)) {
                            items(list.take(30), key = { id(it) }) { m -> SbPoster(name(m), poster(m), rating(m), year(m), badge(m), { p.onItemClick(m) }, onLongClick = { p.onItemLongClick(m) }) }
                        }
                    }
                }
                if (s.hasMorePreviewRows) item { SbButton(tr("Load more", "تحميل المزيد"), Icons.Outlined.ExpandMore, p.onLoadMorePreview, primary = false) }
            }
        } else {
            val list = s.visibleItems
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(s.selectedCategory.orEmpty(), color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text("${list.size}", color = SB.Faint, fontSize = 16.sp)
            }
            when {
                list.isEmpty() && s.isLoadingSelectedCategory -> LazyVerticalGrid(GridCells.Adaptive(170.dp), horizontalArrangement = Arrangement.spacedBy(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) { items(List(12) { it }) { SbSkeleton(Modifier.fillMaxWidth().aspectRatio(2f / 3f)) } }
                list.isEmpty() -> SbEmpty(Icons.Outlined.SearchOff, tr("Empty category", "القسم فارغ"), tr("Pick another genre", "اختر تصنيفاً آخر"))
                else -> LazyVerticalGrid(GridCells.Adaptive(176.dp), horizontalArrangement = Arrangement.spacedBy(20.dp), verticalArrangement = Arrangement.spacedBy(22.dp), contentPadding = PaddingValues(10.dp)) {
                    items(list, key = { id(it) }) { m -> SbPoster(name(m), poster(m), rating(m), year(m), badge(m), { p.onItemClick(m) }, Modifier.fillMaxWidth(), onLongClick = { p.onItemLongClick(m) }) }
                    if (s.canLoadMoreSelectedCategory) item { SbButton(tr("More", "المزيد"), Icons.Outlined.ExpandMore, p.onLoadMoreSelected, primary = false) }
                }
            }
        }
    }
      }
      }
}

// ───────────────────────────── DETAILS (ref: full-bleed backdrop right) ─────────────────────────────

@Composable
private fun SbRoundLabeled(icon: ImageVector, label: String, active: Boolean = false, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SbRound(icon, 58.dp, active, onClick)
        Text(label, color = SB.Sub, fontSize = 14.sp)
    }
}

@Composable
private fun SbBackdrop(url: String?) {
    Box(Modifier.fillMaxSize()) {
        url?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
        val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
        Box(Modifier.fillMaxSize().background(if (rtl) Brush.horizontalGradient(0f to Color(0x220B0D11), 0.4f to Color(0xAA0B0D11), 0.62f to SB.Bg, 1f to SB.Bg) else Brush.horizontalGradient(0f to SB.Bg, 0.38f to SB.Bg, 0.6f to Color(0xAA0B0D11), 1f to Color(0x220B0D11))))
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.5f to Color.Transparent, 1f to SB.Bg)))
    }
}

@Composable
private fun SbMeta(rating: Float?, year: String?, dur: String?, genre: String?, extra: String? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        rating?.takeIf { it > 0 }?.let { Text("★ %.1f".format(it), color = SB.Gold, fontSize = 19.sp, fontWeight = FontWeight.Bold) }
        year?.let { Text(it, color = Color.White, fontSize = 19.sp) }
        dur?.takeIf { it.isNotBlank() }?.let { Text(it, color = Color.White, fontSize = 19.sp) }
        extra?.let { Box(Modifier.clip(RoundedCornerShape(6.dp)).background(Color(0x26FFFFFF)).padding(horizontal = 8.dp, vertical = 2.dp)) { Text(it, color = Color.White, fontSize = 16.sp) } }
        genre?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }?.take(3)?.joinToString("  •  ")?.let { Text(it, color = SB.Sub, fontSize = 19.sp, maxLines = 1) }
    }
}

@Composable
private fun SbCast(cast: String?) {
    val people = cast?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }?.take(8).orEmpty()
    if (people.isEmpty()) return
    Column {
        Text(tr("Cast", "طاقم التمثيل"), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(36.dp)) {
            people.forEach { n ->
                Column(Modifier.width(120.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.size(84.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Color(0xFF3A2466), Color(0xFF1A1A22)))).border(2.dp, Color(0x22FFFFFF), CircleShape), contentAlignment = Alignment.Center) {
                        Text(n.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""), color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(n, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
internal fun SbFreshMovieDetail(p: MovieDetailParams) {
    val m = p.movie
    val play = remember { FocusRequester() }
    LaunchedEffect(m.id) { runCatching { play.requestFocus() } }
    Box(Modifier.fillMaxSize().background(SB.Bg)) {
        SbBackdrop(m.backdropUrl ?: m.posterUrl)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 56.dp, vertical = 40.dp), verticalArrangement = Arrangement.spacedBy(22.dp)) {
            SbRound(Icons.Outlined.ArrowBack, 46.dp, onClick = p.onBack)
            Text(m.name, color = Color.White, fontSize = 58.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth(0.55f), lineHeight = 64.sp)
            SbMeta(m.rating, m.year ?: m.releaseDate?.take(4), m.duration, m.genre, sbQuality(m.name) ?: if (m.isAdult) "18+" else null)
            m.plot?.takeIf { it.isNotBlank() }?.let { Text(it, color = Color(0xE6FFFFFF), fontSize = 19.sp, lineHeight = 28.sp, maxLines = 4, overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth(0.5f)) }
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(26.dp)) {
                SbFocus(p.onPlay, Modifier.width(240.dp).height(60.dp).focusRequester(play), shape = RoundedCornerShape(12.dp), color = SB.Purple, focusedColor = Color(0xFFFF2A36), scale = 1.06f) {
                    Row(Modifier.align(Alignment.Center), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SbIcon(Icons.Outlined.PlayArrow, size = 30.dp)
                        Text(if (p.hasResume) tr("Resume ", "استئناف ") + cgDuration(p.resumePositionMs) else tr("Watch now", "مشاهدة الآن"), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
                SbRoundLabeled(if (m.isFavorite) Icons.Outlined.Check else Icons.Outlined.Add, tr("Add to favorites", "إضافة إلى المفضلة"), m.isFavorite, p.onToggleFavorite)
                SbRoundLabeled(Icons.Outlined.WatchLater, tr("Watch later", "المشاهدة لاحقاً"), m.isFavorite, p.onToggleFavorite)
                p.onPlayTrailer?.let { SbRoundLabeled(Icons.Outlined.Movie, tr("Trailer", "الإعلان"), onClick = it) }
                SbRoundLabeled(Icons.Outlined.Download, tr("Download", "تحميل"), onClick = p.onDownload)
                SbRoundLabeled(Icons.Outlined.Cast, tr("Cast", "بث"), p.isCasting, p.onCast)
                SbRoundLabeled(Icons.Outlined.Share, tr("Share", "مشاركة"), onClick = p.onCopyUrl)
            }
            SbCast(m.cast)
            m.director?.takeIf { it.isNotBlank() }?.let { Text(tr("Director: ", "إخراج: ") + it, color = SB.Sub, fontSize = 16.sp) }
            if (p.relatedContent.isNotEmpty()) Column {
                Text(tr("Similar movies", "أفلام مشابهة"), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(vertical = 10.dp, horizontal = 6.dp)) {
                    items(p.relatedContent.take(20), key = { it.id }) { r -> SbWide(r.name, r.backdropUrl ?: r.posterUrl, r.year, null, { p.onRelatedClick(r) }, Modifier.width(280.dp)) }
                }
            }
        }
    }
}

@Composable
internal fun SbFreshSeriesDetail(p: SeriesDetailParams) {
    val sr = p.series
    val play = remember { FocusRequester() }
    LaunchedEffect(sr.id) { runCatching { play.requestFocus() } }
    val eps = p.selectedSeason?.episodes.orEmpty()
    val target: Episode? = p.resumeEpisode ?: eps.firstOrNull()
    Box(Modifier.fillMaxSize().background(SB.Bg)) {
        SbBackdrop(sr.backdropUrl ?: sr.posterUrl)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 56.dp, vertical = 40.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            SbRound(Icons.Outlined.ArrowBack, 46.dp, onClick = p.onBack)
            Text(sr.name, color = Color.White, fontSize = 56.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth(0.55f), lineHeight = 62.sp)
            SbMeta(sr.rating, sr.releaseDate?.take(4), sr.seasons.size.takeIf { it > 0 }?.let { "$it ${tr("seasons", "مواسم")}" }, sr.genre, if (p.unwatchedEpisodeCount > 0) "${p.unwatchedEpisodeCount} ${tr("new", "جديدة")}" else null)
            sr.plot?.takeIf { it.isNotBlank() }?.let { Text(it, color = Color(0xE6FFFFFF), fontSize = 19.sp, lineHeight = 28.sp, maxLines = 3, overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth(0.5f)) }
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(26.dp)) {
                SbFocus({ target?.let { if (p.resumeEpisode != null) p.onResumeClick(it) else p.onEpisodeClick(it) } }, Modifier.width(280.dp).height(60.dp).focusRequester(play), shape = RoundedCornerShape(12.dp), color = SB.Purple, focusedColor = Color(0xFFFF2A36), scale = 1.06f) {
                    Row(Modifier.align(Alignment.Center), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SbIcon(Icons.Outlined.PlayArrow, size = 30.dp)
                        Text(target?.let { (if (p.resumeEpisode != null) tr("Resume", "استئناف") else tr("Play", "تشغيل")) + " S${it.seasonNumber} E${it.episodeNumber}" } ?: tr("Play", "تشغيل"), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
                SbRoundLabeled(if (sr.isFavorite) Icons.Outlined.Check else Icons.Outlined.Add, tr("Add to favorites", "إضافة إلى المفضلة"), sr.isFavorite, p.onToggleFavorite)
                SbRoundLabeled(Icons.Outlined.WatchLater, tr("Watch later", "المشاهدة لاحقاً"), sr.isFavorite, p.onToggleFavorite)
                SbRoundLabeled(Icons.Outlined.Cast, tr("Cast", "بث"), p.isCasting, p.onCastResumeEpisode)
                SbRoundLabeled(Icons.Outlined.Share, tr("Share", "مشاركة")) { target?.let(p.onCopyEpisodeUrl) }
            }
            if (sr.seasons.isNotEmpty()) Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 6.dp, horizontal = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                sr.seasons.forEach { se -> SbChip(se.name.ifBlank { "${tr("Season", "الموسم")} ${se.seasonNumber}" }, se.seasonNumber == p.selectedSeason?.seasonNumber, count = (se.episodeCount.takeIf { it > 0 } ?: se.episodes.size).takeIf { it > 0 }) { p.onSeasonSelected(se) } }
            }
            if (eps.isEmpty()) SbEmpty(Icons.Outlined.VideoLibrary, tr("No episodes", "لا توجد حلقات"), tr("Pick another season", "اختر موسماً آخر"))
            else LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(vertical = 10.dp, horizontal = 6.dp)) {
                items(eps, key = { it.id }) { e ->
                    val pr = if (e.durationSeconds > 0 && e.watchProgress > 0) e.watchProgress / 1000f / e.durationSeconds else null
                    Column(Modifier.width(320.dp)) {
                        SbWide("${e.episodeNumber}. ${e.title}", e.coverUrl ?: sr.backdropUrl, e.duration, pr, { p.onEpisodeClick(e) }, Modifier.fillMaxWidth())
                        e.plot?.takeIf { it.isNotBlank() }?.let { Text(it, color = SB.Faint, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis) }
                    }
                }
            }
            SbCast(sr.cast)
        }
    }
}

// ───────────────────────────── SEARCH (ref: glowing field, keyboard, mic, top results) ─────────────────────────────

private val SB_KB_EN = listOf("1234567890", "qwertyuiop", "asdfghjkl", "zxcvbnm")
private val SB_KB_AR = listOf("1234567890", "ضصثقفغعهخح", "شسيبلاتنمك", "ئءؤرىةوزظ")

@Composable
internal fun SbFreshSearch(p: SearchParams) {
    val s = p.uiState
    var arabic by remember { mutableStateOf(java.util.Locale.getDefault().language == "ar") }
    val keyFirst = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { keyFirst.requestFocus() } }
    val top = remember(s.movies, s.series) { s.movies.map { Triple(it.name, it.posterUrl, "${tr0(arabic, "Movie", "فيلم")} • ${it.year ?: ""}") to { p.onMovieClick(it) } } + s.series.map { Triple(it.name, it.posterUrl, "${tr0(arabic, "Series", "مسلسل")} • ${it.releaseDate?.take(4) ?: ""}") to { p.onSeriesClick(it) } } }
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(36.dp)) {
        Column(Modifier.weight(1.15f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(Modifier.weight(1f).height(64.dp).shadow(18.dp, RoundedCornerShape(50), ambientColor = SB.Purple, spotColor = SB.Purple).clip(RoundedCornerShape(50)).background(Color(0xFF181214)).border(2.dp, SB.Purple, RoundedCornerShape(50)).padding(horizontal = 22.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SbIcon(Icons.Outlined.Search, SB.Sub, 26.dp)
                    Box(Modifier.weight(1f)) {
                        if (p.query.isEmpty()) Text(tr("Search movies, series, channels", "ابحث عن أفلام، مسلسلات، قنوات"), color = SB.Faint, fontSize = 19.sp)
                        BasicTextField(p.query, p.onQueryChange, singleLine = true, textStyle = TextStyle(color = Color.White, fontSize = 21.sp), cursorBrush = SolidColor(SB.Purple), modifier = Modifier.fillMaxWidth().focusRequester(p.searchFocusRequester))
                    }
                }
                SbRound(Icons.Outlined.Mic, 64.dp, active = true) { runCatching { p.searchFocusRequester.requestFocus() } }
            }
            // suggestions = recent queries, or tabs when searching
            Row(Modifier.horizontalScroll(rememberScrollState()).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (p.query.isBlank() && p.recentQueries.isNotEmpty()) {
                    p.recentQueries.take(6).forEach { q -> SbChip(q, false) { p.onRecentQuerySelected(q) } }
                    SbChip(tr("Clear", "مسح"), false, icon = Icons.Outlined.Close) { p.onClearRecentQueries() }
                } else SearchTab.entries.forEach { t ->
                    val l = when (t) { SearchTab.ALL -> tr("All", "الكل"); SearchTab.LIVE -> tr("Live", "مباشر"); SearchTab.MOVIES -> tr("Movies", "أفلام"); SearchTab.SERIES -> tr("Series", "مسلسلات") }
                    SbChip(l, p.selectedTab == t) { p.onTabSelected(t) }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                Column(Modifier.weight(1f).clip(SB.R).background(Color(0xFF121218)).border(1.dp, SB.Line, SB.R).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    (if (arabic) SB_KB_AR else SB_KB_EN).forEachIndexed { ri, row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (ri == 3) SbKey(Modifier.weight(1.3f), icon = Icons.Outlined.Language) { arabic = !arabic }
                            row.forEachIndexed { ci, ch -> SbKey(if (ri == 2 && ci == 0) Modifier.weight(1f).focusRequester(keyFirst) else Modifier.weight(1f), ch.toString()) { p.onQueryChange(p.query + ch) } }
                            if (ri == 3) SbKey(Modifier.weight(1.5f), icon = Icons.Outlined.Backspace) { p.onQueryChange(p.query.dropLast(1)) }
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SbKey(Modifier.weight(1.5f), if (arabic) "EN" else "ع") { arabic = !arabic }
                        SbKey(Modifier.weight(5f), tr("space", "مسافة")) { p.onQueryChange(p.query + " ") }
                        SbKey(Modifier.weight(2f), icon = Icons.Outlined.Search, accent = true) { p.onSearch() }
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(64.dp).shadow(20.dp, CircleShape, ambientColor = SB.Purple, spotColor = SB.Purple).clip(CircleShape).background(SB.PurpleDeep).border(3.dp, SB.Purple, CircleShape), contentAlignment = Alignment.Center) { SbIcon(Icons.Outlined.Mic, size = 28.dp) }
                    Text(tr("Tap to speak", "اضغط للتحدث"), color = SB.Sub, fontSize = 15.sp)
                }
            }
            if (s.channels.isNotEmpty()) Column {
                Text(tr("Channels", "القنوات"), color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(8.dp)) {
                    items(s.channels.take(20), key = { it.id }) { c -> SbChannelCard(c, false, Modifier.width(250.dp), onLongClick = { p.onChannelLongClick(c) }) { p.onChannelClick(c) } }
                }
            }
        }
        Column(Modifier.weight(1f).fillMaxHeight()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(bottom = 12.dp)) {
                Text(tr("Top results", "أفضل النتائج"), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                if (s.totalResults > 0) Text("${s.totalResults}", color = SB.Faint, fontSize = 15.sp)
            }
            when {
                s.isLoading -> LazyVerticalGrid(GridCells.Fixed(3), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { items(List(6) { it }) { SbSkeleton(Modifier.fillMaxWidth().aspectRatio(2f / 3f)) } }
                s.isEmpty -> SbEmpty(Icons.Outlined.SearchOff, tr("No results", "لا توجد نتائج"), tr("Try another word", "جرّب كلمة أخرى"))
                top.isEmpty() -> SbEmpty(Icons.Outlined.Search, tr("Start typing", "ابدأ الكتابة"), tr("Results appear live as you type", "تظهر النتائج مباشرة أثناء الكتابة"))
                else -> LazyVerticalGrid(GridCells.Fixed(3), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(8.dp)) {
                    items(top.take(60)) { (t, open) ->
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SbFocus(open, Modifier.fillMaxWidth().aspectRatio(2f / 3f), shape = SB.RS) { t.second?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) } ?: Text(t.first.take(2), color = SB.Sub, fontSize = 30.sp, modifier = Modifier.align(Alignment.Center)) }
                            Text(t.first, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Row(verticalAlignment = Alignment.CenterVertically) { Text(t.third, color = SB.Faint, fontSize = 12.sp, modifier = Modifier.weight(1f), maxLines = 1); sbQuality(t.first)?.let { SbTag(it, Color(0x55FFFFFF), false) } ?: SbTag("HD", Color(0x55FFFFFF), false) }
                        }
                    }
                }
            }
        }
    }
}

private fun tr0(ar: Boolean, en: String, a: String) = if (ar) a else en

@Composable
private fun SbKey(modifier: Modifier = Modifier, label: String? = null, icon: ImageVector? = null, accent: Boolean = false, onClick: () -> Unit) {
    SbFocus(onClick, modifier.height(38.dp), shape = RoundedCornerShape(10.dp), color = if (accent) SB.Purple else Color(0xFF22222C), focusedColor = if (accent) Color(0xFFFF2A36) else Color(0xFF401A1D), scale = 1.12f) {
        Box(Modifier.align(Alignment.Center)) { if (icon != null) SbIcon(icon, size = 22.dp) else Text(label.orEmpty(), color = Color.White, fontSize = 16.sp) }
    }
}

// ───────────────────────────── FAVORITES (grid / list) ─────────────────────────────

@Composable
internal fun SbFreshFavorites(p: FavoritesParams) {
    if (SbRecent.active) { SbsRecent(p); return }
    var grid by remember { mutableStateOf(true) }
    val all = p.sections.flatMap { it.items }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf(SavedLibraryPreset.ALL_SAVED to tr("All", "الكل"), SavedLibraryPreset.LIVE_RECALL to tr("Channels", "القنوات"), SavedLibraryPreset.MOVIES to tr("Movies", "الأفلام"), SavedLibraryPreset.SERIES to tr("Series", "المسلسلات"), SavedLibraryPreset.WATCH_NEXT to tr("Watch next", "التالي"))
                .forEach { (k, l) -> SbChip(l, p.selectedPreset == k) { p.onPresetSelected(k) } }
            Spacer(Modifier.weight(1f))
            SbRound(Icons.Outlined.GridView, active = grid) { grid = true }
            SbRound(Icons.Outlined.ViewList, active = !grid) { grid = false }
        }
        if (p.continueWatching.isNotEmpty()) SbRail(tr("Continue watching", "متابعة المشاهدة")) {
            p.continueWatching.take(10).forEach { h -> SbWide(h.title, h.history.posterUrl, h.subtitle, if (h.history.totalDurationMs > 0) h.history.resumePositionMs.toFloat() / h.history.totalDurationMs else null, { p.onHistoryClick(h) }, Modifier.width(280.dp)) }
        }
        if (all.isEmpty()) { SbEmpty(Icons.Outlined.FavoriteBorder, tr("No favorites yet", "لا توجد مفضلة بعد"), tr("Long-press any channel, movie or series to add it", "اضغط مطولاً على أي قناة أو فيلم لإضافته")); return@Column }
        if (grid) LazyVerticalGrid(GridCells.Adaptive(250.dp), horizontalArrangement = Arrangement.spacedBy(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp), contentPadding = PaddingValues(10.dp)) {
            gridItemsIndexed(all, key = { i, f -> "${f.favorite.contentType}-${f.favorite.id}-$i" }) { _, f ->
                SbFocus({ p.onItemClick(f) }, Modifier.fillMaxWidth().aspectRatio(16f / 9f), shape = SB.R, onLongClick = { p.onItemLongClick(f) }) {
                    Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF3A1416), SB.Card))))
                    Text(f.title.take(2).uppercase(), color = Color(0x22FFFFFF), fontSize = 64.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.Center))
                    Column(Modifier.align(Alignment.BottomStart).padding(14.dp)) {
                        Text(f.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        f.subtitle?.let { Text(it, color = SB.Sub, fontSize = 13.sp, maxLines = 1) }
                    }
                    Box(Modifier.align(Alignment.TopEnd).padding(10.dp)) { SbIcon(Icons.Outlined.Star, SB.Gold, 22.dp) }
                }
            }
        } else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(8.dp)) {
            itemsIndexed(all, key = { i, f -> "${f.favorite.contentType}-${f.favorite.id}-$i" }) { i, f ->
                SbFocus({ p.onItemClick(f) }, Modifier.fillMaxWidth().height(76.dp), shape = SB.RS, scale = 1.02f, onLongClick = { p.onItemLongClick(f) }) {
                    Row(Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("${i + 1}", color = SB.Faint, fontSize = 16.sp, modifier = Modifier.width(32.dp))
                        Box(Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(SB.PurpleSoft), contentAlignment = Alignment.Center) { SbIcon(Icons.Outlined.Star, SB.Purple, 22.dp) }
                        Text(f.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        f.subtitle?.let { Text(it, color = SB.Sub, fontSize = 14.sp, maxLines = 1) }
                    }
                }
            }
        }
    }
}

// ───────────────────────────── SETTINGS ─────────────────────────────

private fun sbSettingIcon(title: String): ImageVector {
    val t = title.lowercase()
    return when {
        "account" in t || "حساب" in t || "provider" in t || "مزود" in t -> Icons.Outlined.Person
        "video" in t || "player" in t || "فيديو" in t || "مشغل" in t -> Icons.Outlined.HighQuality
        "parent" in t || "أبوي" in t || "رقابة" in t || "pin" in t -> Icons.Outlined.Lock
        "language" in t || "لغة" in t -> Icons.Outlined.Language
        "subtitle" in t || "ترجمة" in t -> Icons.Outlined.ClosedCaption
        "about" in t || "حول" in t -> Icons.Outlined.Info
        "theme" in t || "appearance" in t || "مظهر" in t || "ثيم" in t -> Icons.Outlined.Palette
        "epg" in t || "guide" in t || "دليل" in t -> Icons.Outlined.CalendarMonth
        "backup" in t || "نسخ" in t -> Icons.Outlined.Backup
        else -> Icons.Outlined.Tune
    }
}

@Composable
internal fun SbFreshSettingsNav(p: SettingsNavParams) {
    LazyColumn(Modifier.width(380.dp).fillMaxHeight().clip(SB.R).background(SB.Glass).border(1.dp, SB.Line, SB.R).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        itemsIndexed(p.entries) { i, (title, sub) ->
            val sel = i == p.selectedCategory
            SbFocus({ p.onCategorySelected(i) }, Modifier.fillMaxWidth().height(74.dp).let { if (sel) it.focusRequester(p.focusRequester) else it }, shape = RoundedCornerShape(14.dp), color = if (sel) SB.PurpleSoft else Color.Transparent, focusedColor = SB.Purple, scale = 1.03f) {
                Row(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(if (sel) SB.Purple else Color(0x14FFFFFF)), contentAlignment = Alignment.Center) { SbIcon(sbSettingIcon(title), size = 22.dp) }
                    Column(Modifier.weight(1f)) {
                        Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (sub.isNotBlank()) Text(sub, color = SB.Faint, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
internal fun SbFreshSettingsFrame(navigation: @Composable () -> Unit, content: @Composable () -> Unit) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        navigation()
        Box(Modifier.weight(1f).fillMaxHeight().clip(SB.R).background(Color(0xCC0F1115)).border(1.dp, SB.Line, SB.R).padding(24.dp)) { content() }
    }
}

// ───────────────────────────── PLAYER (ref: top title, round actions, purple seek, centered transport, pills) ─────────────────────────────

@Composable
private fun SbPill(icon: ImageVector, label: String, onClick: () -> Unit) {
    SbFocus(onClick, Modifier.height(54.dp), shape = RoundedCornerShape(50), color = Color(0x40000000), focusedColor = SB.Purple, scale = 1.06f) {
        Row(Modifier.align(Alignment.Center).padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SbIcon(icon, size = 22.dp); Text(label, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold); SbIcon(Icons.Outlined.ExpandMore, size = 20.dp)
        }
    }
}

@Composable
internal fun SbFreshPlayer(p: PlayerOverlayParams) {
    val live = p.currentChannel != null && p.duration <= 0L && !p.isCatchUpPlayback
    AnimatedVisibility(p.visible, enter = fadeIn(), exit = fadeOut(), modifier = p.modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0f to Color(0xB3000000), 0.25f to Color.Transparent, 0.6f to Color.Transparent, 1f to Color(0xE6000000))))
            // top-left: back + title + episode line
            Row(Modifier.align(Alignment.TopStart).padding(36.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                SbRound(Icons.Outlined.ArrowBack, 50.dp) { p.onNavigateBack(); p.onClose() }
                Column {
                    Text(p.mediaTitle ?: p.title, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    val line = p.episodeLine ?: if (live) listOfNotNull(p.displayChannelNumber.takeIf { it > 0 }?.let { "#$it" }, p.currentChannelName, p.currentProgram?.title).joinToString("  •  ") else null
                    line?.takeIf { it.isNotBlank() }?.let { Text(it, color = SB.Sub, fontSize = 18.sp, maxLines = 1) }
                }
            }
            // top-right: cast / pip / settings circles
            Row(Modifier.align(Alignment.TopEnd).padding(36.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                SbRound(Icons.Outlined.Cast, 56.dp, p.isCastConnected) { if (p.isCastConnected) p.onStopCasting() else p.onCast() }
                SbRound(Icons.Outlined.PictureInPicture, 56.dp, onClick = p.onEnterPictureInPicture)
                SbRound(Icons.Outlined.Settings, 56.dp, onClick = p.onOpenPlaybackSpeed)
            }
            Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 44.dp, vertical = 32.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                if (live) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SbTag(tr("LIVE", "مباشر"), SB.Live)
                        p.currentProgram?.let { pr -> Text("${sbTime(pr.startTime)} – ${sbTime(pr.endTime)}  ${pr.title}", color = Color.White, fontSize = 18.sp, maxLines = 1, modifier = Modifier.weight(1f)) }
                        p.nextProgram?.let { n -> Text("${tr("Next", "التالي")}: ${sbTime(n.startTime)} ${n.title}", color = SB.Sub, fontSize = 16.sp, maxLines = 1) }
                    }
                    val pr = p.currentProgram
                    val f = if (pr != null && pr.endTime > pr.startTime) ((System.currentTimeMillis() - pr.startTime).toFloat() / (pr.endTime - pr.startTime)).coerceIn(0f, 1f) else 1f
                    SbSeek(f)
                } else {
                    Text(cgDuration(p.currentPosition) + "  /  " + cgDuration(p.duration), color = Color.White, fontSize = 20.sp)
                    SbSeek(if (p.duration > 0) (p.currentPosition.toFloat() / p.duration).coerceIn(0f, 1f) else 0f)
                }
                Row(Modifier.fillMaxWidth().focusRequester(p.quickActionsFocusRequester), horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)) {
                    SbPill(Icons.Outlined.HighQuality, p.resolutionBadgeLabel ?: tr("Auto", "تلقائي"), p.onOpenVideoTracks)
                    SbPill(Icons.Outlined.VolumeUp, tr("Audio", "الصوت"), p.onOpenAudioTracks)
                    SbPill(Icons.Outlined.ClosedCaption, tr("Subtitles", "الترجمة"), p.onOpenSubtitleTracks)
                }
                Box(Modifier.fillMaxWidth()) {
                    Row(Modifier.align(Alignment.CenterStart), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SbRound(if (p.isMuted) Icons.Outlined.VolumeOff else Icons.Outlined.VolumeUp, 50.dp, p.isMuted, p.onToggleMute)
                        Box(Modifier.width(110.dp).height(5.dp).clip(CircleShape).background(Color(0x44FFFFFF))) { Box(Modifier.fillMaxHeight().fillMaxWidth(if (p.isMuted) 0f else 0.6f).background(SB.Purple)) }
                        if (live) { SbRound(Icons.Outlined.List, 50.dp, onClick = p.onOpenLiveChannels); SbRound(Icons.Outlined.CalendarMonth, 50.dp, onClick = p.onOpenLiveGuide) }
                    }
                    Row(Modifier.align(Alignment.Center), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                        SbRound(Icons.Outlined.Replay10, 54.dp, onClick = p.onSeekBackward)
                        SbFocus(p.onTogglePlayPause, Modifier.size(72.dp).focusRequester(p.playButtonFocusRequester), shape = CircleShape, color = SB.Purple, focusedColor = Color(0xFFFF2A36), scale = 1.1f) {
                            Box(Modifier.align(Alignment.Center)) { SbIcon(if (p.isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow, size = 46.dp) }
                        }
                        SbRound(Icons.Outlined.Forward10, 54.dp, onClick = p.onSeekForward)
                        if (p.showEpisodesAction) SbRound(Icons.Outlined.SkipNext, 56.dp, onClick = p.onOpenEpisodes)
                        if (live) SbRound(Icons.Outlined.Star, 56.dp, p.currentChannel?.isFavorite == true, p.onToggleLiveFavorite)
                    }
                }
            }
        }
    }
}

@Composable
private fun SbSeek(f: Float) {
    BoxWithConstraints(Modifier.fillMaxWidth().height(20.dp)) {
        Box(Modifier.align(Alignment.CenterStart).fillMaxWidth().height(6.dp).clip(CircleShape).background(Color(0x44FFFFFF)))
        Box(Modifier.align(Alignment.CenterStart).fillMaxWidth(f.coerceAtLeast(0.005f)).height(6.dp).clip(CircleShape).background(SB.Grad))
        Box(Modifier.align(Alignment.CenterStart).offset(x = (maxWidth - 20.dp) * f).size(20.dp).clip(CircleShape).background(Color.White).border(3.dp, SB.Purple, CircleShape))
    }
}

@Composable
internal fun SbGenreRow(label: String, sel: Boolean, count: Int, modifier: Modifier, locked: Boolean, onClick: () -> Unit) {
    SbFocus(onClick, modifier.fillMaxWidth().height(46.dp), shape = RoundedCornerShape(10.dp), color = if (sel) SB.Purple else Color.Transparent, focusedColor = if (sel) SB.Purple else Color(0x40E50914), scale = 1.03f) {
        Row(Modifier.fillMaxSize().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(label, color = Color.White, fontSize = 16.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            if (locked) SbIcon(Icons.Outlined.Lock, SB.Faint, 16.dp)
            if (count > 0) Text("$count", color = if (sel) Color.White else SB.Faint, fontSize = 13.sp)
        }
    }
}
