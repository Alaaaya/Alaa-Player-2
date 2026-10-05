package com.streamvault.app.ui.themes.sabhiya

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Glow
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.streamvault.app.navigation.Routes
import com.streamvault.app.ui.components.PlayerRenderView
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.app.ui.model.guideLookupKey
import com.streamvault.app.ui.screens.dashboard.DashboardFeatureAction
import com.streamvault.app.ui.screens.favorites.SavedLibraryFilter
import com.streamvault.app.ui.screens.favorites.SavedLibraryPreset
import com.streamvault.app.ui.screens.favorites.SavedLibrarySort
import com.streamvault.app.ui.screens.search.SearchTab
import com.streamvault.app.ui.themes.bespoke.DashboardParams
import com.streamvault.app.ui.themes.bespoke.EpgParams
import com.streamvault.app.ui.themes.bespoke.FavoritesParams
import com.streamvault.app.ui.themes.bespoke.InnerPanelBackScope
import com.streamvault.app.ui.themes.bespoke.LibraryParams
import com.streamvault.app.ui.themes.bespoke.LiveTvParams
import com.streamvault.app.ui.themes.bespoke.MovieDetailParams
import com.streamvault.app.ui.themes.bespoke.SearchParams
import com.streamvault.app.ui.themes.bespoke.SeriesDetailParams
import com.streamvault.app.ui.themes.bespoke.SettingsNavParams
import com.streamvault.app.ui.themes.bespoke.ShellParams
import com.streamvault.app.ui.themes.bespoke.qualityBadge
import com.streamvault.app.ui.themes.bespoke.tr
import com.streamvault.domain.model.Channel
import com.streamvault.domain.model.LibraryFilterType
import com.streamvault.domain.model.LibrarySortBy
import com.streamvault.player.PlayerSurfaceResizeMode
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.lazy.grid.*
import com.streamvault.app.ui.themes.bespoke.*
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay

// ===================== Sabhiya second pass: bespoke library / details / search / settings =====================

private val NBlue = Color(0xFFE50914)
private val NSub = Color(0xFFC9B8E8)
private val NGlass = Color(0x2EFF4D57)
private val NLine = Color(0x33FF4D57)

/** Poster: rounded, thin blue line, solid blue ring + glow on focus, title strip below. */
@Composable
internal fun SbPoster(title: String, url: String?, caption: String?, onClick: () -> Unit, modifier: Modifier = Modifier, locked: Boolean = false, onLongClick: (() -> Unit)? = null, onFocus: () -> Unit = {}) {
    var f by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(12.dp)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        TvClickableSurface(onClick = onClick, onLongClick = onLongClick,
            modifier = Modifier.fillMaxWidth().aspectRatio(2f / 3f).onFocusChanged { f = it.isFocused; if (it.isFocused) onFocus() },
            shape = ClickableSurfaceDefaults.shape(shape),
            colors = ClickableSurfaceDefaults.colors(containerColor = Color(0xFF171220), focusedContainerColor = Color(0xFF171220)),
            border = ClickableSurfaceDefaults.border(border = Border(androidx.compose.foundation.BorderStroke(1.dp, NLine), shape = shape), focusedBorder = Border(androidx.compose.foundation.BorderStroke(3.dp, NBlue), shape = shape)),
            glow = ClickableSurfaceDefaults.glow(focusedGlow = androidx.tv.material3.Glow(NBlue.copy(alpha = 0.7f), 18.dp)),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.07f)) {
            if (!locked && url != null) AsyncImage(url, title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            else Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF16307A), Color(0xFF0F0B16)))), contentAlignment = Alignment.Center) {
                if (locked) androidx.compose.material3.Icon(Icons.Outlined.Lock, null, tint = Color.White, modifier = Modifier.size(34.dp))
                else Text(title.take(1), color = Color.White, fontSize = 40.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif)
            }
        }
        Text(title, color = if (f) Color.White else Color.White.copy(alpha = 0.85f), fontSize = 14.sp, fontWeight = if (f) FontWeight.Bold else FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        caption?.takeIf { it.isNotBlank() }?.let { Text(it, color = NSub, fontSize = 12.sp, maxLines = 1) }
    }
}

/** Square translucent action tile (same language as the home tiles). */
@Composable
internal fun SbActionTile(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier, wide: Boolean = false) {
    val shape = RoundedCornerShape(14.dp)
    TvClickableSurface(onClick = onClick, modifier = modifier.width(if (wide) 190.dp else 118.dp).height(104.dp), shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = if (wide) NBlue.copy(alpha = 0.55f) else NGlass, focusedContainerColor = NBlue, contentColor = Color.White, focusedContentColor = Color.White),
        border = ClickableSurfaceDefaults.border(border = Border(androidx.compose.foundation.BorderStroke(1.dp, NLine), shape = shape)),
        glow = ClickableSurfaceDefaults.glow(focusedGlow = androidx.tv.material3.Glow(NBlue.copy(alpha = 0.6f), 16.dp)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f)) {
        Column(Modifier.fillMaxSize().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            androidx.compose.material3.Icon(icon, null, tint = Color.White, modifier = Modifier.size(34.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun SbSectionTitle(text: String, trailing: String? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(vertical = 6.dp)) {
        Box(Modifier.width(4.dp).height(22.dp).background(NBlue, RoundedCornerShape(2.dp)))
        Text(text, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        trailing?.let { Text(it, color = NSub, fontSize = 15.sp) }
    }
}

@Composable
private fun uvSortLabel(s: LibrarySortBy) = when (s.name) {
    "DEFAULT", "LIBRARY" -> tr("Default", "افتراضي"); "TITLE", "NAME", "A_Z" -> tr("Title", "الاسم")
    "RATING" -> tr("Rating", "التقييم"); "YEAR", "RELEASE", "NEWEST", "RECENT", "DATE_ADDED" -> tr("Newest", "الأحدث")
    else -> s.name.lowercase().replace('_', ' ')
}

/** Movies / Series: numbered blue-bar category list (like live categories) + poster grid with focused-title header. */
@Composable
internal fun <T> SbLibrary(
    kindEn: String, kindAr: String, p: LibraryParams<T>, key: (T) -> Long, title: (T) -> String, image: (T) -> String?,
    caption: (T) -> String?, rating: (T) -> Float, plot: (T) -> String?, isMovies: Boolean
) {
    val s = p.uiState
    val nav = LocalCpNavigate.current
    val items = s.visibleItems
    var focused by remember { mutableStateOf<T?>(null) }
    var sortIndex by remember(s.selectedSort) { mutableStateOf(LibrarySortBy.entries.indexOf(s.selectedSort).coerceAtLeast(0)) }
    CpLtrRow(Modifier.fillMaxSize(), spacing = 28.dp) { rtl ->
        rtl {
            Column(Modifier.width(400.dp).fillMaxHeight()) {
                Row(Modifier.padding(bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(true to tr("Movies", "الأفلام"), false to tr("Series", "المسلسلات")).forEach { (m, label) ->
                        SbRow(m == isMovies, onClick = { if (m != isMovies) nav(if (m) Routes.MOVIES else Routes.SERIES) }, modifier = Modifier.weight(1f)) {
                            androidx.compose.material3.Icon(if (m) Icons.Outlined.Movie else Icons.Outlined.VideoLibrary, null, tint = androidx.tv.material3.LocalContentColor.current, modifier = Modifier.size(22.dp))
                            Text(label, fontSize = 17.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    item {
                        SbRow(s.selectedCategory == null, onClick = { p.onShowAll() }) {
                            Text("1", fontSize = 18.sp, color = NSub, modifier = Modifier.width(32.dp))
                            Text(tr("All", "الكل"), fontSize = 19.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                            Text("${s.libraryCount}", fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f))
                            androidx.compose.material3.Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, tint = androidx.tv.material3.LocalContentColor.current, modifier = Modifier.size(22.dp))
                        }
                    }
                    items(s.categoryNames.size, key = { s.categoryNames[it] }) { i ->
                        val name = s.categoryNames[i]
                        val cat = s.categoryFor(name)
                        SbRow(name == s.selectedCategory, onClick = { cat?.let(p.onCategoryClick) }, onLongClick = { cat?.let(p.onCategoryLongClick) }) {
                            Text("${i + 2}", fontSize = 18.sp, color = NSub, modifier = Modifier.width(32.dp))
                            if (cat?.let(p.isCategoryLocked) == true) androidx.compose.material3.Icon(Icons.Outlined.Lock, null, tint = androidx.tv.material3.LocalContentColor.current, modifier = Modifier.size(18.dp))
                            Text(name, fontSize = 19.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            s.categoryCounts[name]?.let { Text("$it", fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f)) }
                            androidx.compose.material3.Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, tint = androidx.tv.material3.LocalContentColor.current, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }
        }
        rtl {
            Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(s.selectedCategory ?: tr("All $kindEn", "كل " + kindAr), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${items.size} " + tr("titles", "عنوان"), color = NSub, fontSize = 15.sp)
                    }
                    CgSearchField(s.searchQuery, p.onQueryChange, tr("Search", "بحث"), Modifier.width(260.dp).focusRequester(p.initialFocusRequester))
                    SbRow(false, onClick = { sortIndex = (sortIndex + 1) % LibrarySortBy.entries.size; p.onSortChange(LibrarySortBy.entries[sortIndex]) }, modifier = Modifier.width(210.dp)) {
                        androidx.compose.material3.Icon(Icons.Outlined.SwapVert, null, tint = androidx.tv.material3.LocalContentColor.current, modifier = Modifier.size(22.dp))
                        Text(uvSortLabel(s.selectedSort), fontSize = 16.sp, maxLines = 1)
                    }
                }
                // focused title strip: rating, plot
                val f = focused ?: items.firstOrNull()
                Box(Modifier.fillMaxWidth().height(58.dp)) {
                    if (f != null) Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(title(f), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (rating(f) > 0f) Text("★ " + String.format(java.util.Locale.US, "%.1f", rating(f)), color = Color(0xFFFFC94D), fontSize = 15.sp)
                            caption(f)?.let { Text(it, color = NSub, fontSize = 15.sp, maxLines = 1) }
                        }
                        plot(f)?.takeIf { it.isNotBlank() }?.let { Text(it, color = NSub, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    }
                }
                Box(Modifier.fillMaxSize()) {
                    if (items.isEmpty() && !s.isLoadingSelectedCategory) CgEmpty(tr("Nothing here yet", "لا يوجد محتوى هنا"))
                    LazyVerticalGrid(GridCells.Fixed(6), Modifier.fillMaxSize(), contentPadding = PaddingValues(6.dp, 6.dp, 6.dp, 40.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        items(items.size, key = { key(items[it]) }) { i ->
                            val item = items[i]
                            if (i >= items.size - 12) LaunchedEffect(items.size) { if (s.selectedCategory != null && s.canLoadMoreSelectedCategory) p.onLoadMoreSelected() else if (s.hasMorePreviewRows) p.onLoadMorePreview() }
                            SbPoster(title(item), image(item), null, { p.onItemClick(item) }, locked = p.isItemLocked(item), onLongClick = { p.onItemLongClick(item) }, onFocus = { focused = item })
                        }
                    }
                }
            }
        }
    }
}

/** Shared detail header: faded backdrop on the trailing side, serif title, meta dots, plot, tile actions. */
@Composable
private fun SbDetailHero(backdrop: String?, poster: String?, name: String, meta: List<String?>, plot: String?, badges: List<String>, actions: @Composable RowScope.() -> Unit) {
    Box(Modifier.fillMaxWidth().height(470.dp).clip(RoundedCornerShape(20.dp))) {
        (backdrop ?: poster)?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xF00E0C12), Color(0xC00E0C12), Color(0x400E0C12)))))
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.5f to Color.Transparent, 1f to Color(0xFF0E0C12))))
        SbRings(Modifier.fillMaxSize().alpha(0.28f))
        Row(Modifier.fillMaxSize().padding(34.dp), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
            Box(Modifier.width(250.dp).aspectRatio(2f / 3f).clip(RoundedCornerShape(14.dp)).background(Color(0xFF171220)).border(2.dp, NBlue.copy(alpha = 0.7f), RoundedCornerShape(14.dp))) {
                poster?.let { AsyncImage(it, name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                    ?: Text(name.take(1), color = Color.White, fontSize = 60.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, modifier = Modifier.align(Alignment.Center))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(name, color = Color.White, fontSize = 50.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, fontWeight = FontWeight.Light, maxLines = 2, lineHeight = 56.sp, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    badges.forEach { Text(it, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.background(NBlue, RoundedCornerShape(6.dp)).padding(horizontal = 9.dp, vertical = 3.dp)) }
                    Text(meta.filterNotNull().filter { it.isNotBlank() }.joinToString("  •  "), color = NSub, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text(plot.orEmpty(), color = Color.White.copy(alpha = 0.88f), fontSize = 16.sp, maxLines = 3, lineHeight = 24.sp, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 900.dp))
                Spacer(Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), content = actions)
            }
        }
    }
}


@Composable
internal fun SbMovieDetail(p: MovieDetailParams) {
    val m = p.movie
    val play = remember { FocusRequester() }
    LaunchedEffect(m.id) { runCatching { play.requestFocus() } }
    val badges = buildList { val l = (m.variantLabel ?: m.name).uppercase(); if ("4K" in l || "2160" in l) add("4K") else if ("1080" in l || "FHD" in l) add("FHD"); if (m.rating > 0f) add("★ " + String.format(java.util.Locale.US, "%.1f", m.rating)) }
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(bottom = 40.dp)) {
        item {
            SbDetailHero(m.backdropUrl, m.posterUrl, m.name, listOf(m.year, m.duration, m.genre?.split(',', '/', '|')?.take(2)?.joinToString(" · ") { it.trim() }), m.plot, badges) {
                SbActionTile(Icons.Outlined.PlayArrow, if (p.hasResume) tr("Resume", "استكمال") + " " + cgDuration(p.resumePositionMs) else tr("Watch now", "مشاهدة الآن"), p.onPlay, Modifier.focusRequester(play), wide = true)
                p.onPlayTrailer?.let { SbActionTile(Icons.Outlined.Movie, tr("Trailer", "الإعلان"), it) }
                SbActionTile(if (m.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, tr("Favorite", "المفضلة"), p.onToggleFavorite)
                SbActionTile(Icons.Outlined.Cast, tr("Cast", "بث"), p.onCast)
                SbActionTile(Icons.Outlined.Download, tr("Download", "تنزيل"), p.onDownload)
                SbActionTile(Icons.AutoMirrored.Outlined.ArrowBack, tr("Back", "رجوع"), p.onBack)
            }
        }
        if (!m.director.isNullOrBlank() || !m.cast.isNullOrBlank()) item {
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(NGlass).border(1.dp, NLine, RoundedCornerShape(14.dp)).padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                m.director?.takeIf { it.isNotBlank() }?.let { Text(tr("Director: ", "المخرج: ") + it, color = Color.White, fontSize = 16.sp, maxLines = 1) }
                m.cast?.takeIf { it.isNotBlank() }?.let { Text(tr("Cast: ", "الممثلون: ") + it, color = Color.White.copy(alpha = 0.85f), fontSize = 15.sp, maxLines = 2) }
            }
        }
        if (m.variants.size > 1) item {
            Column { SbSectionTitle(tr("Versions", "النسخ"))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(m.variants.size) { i -> val v = m.variants[i]
                    SbRow(v.rawMovieId == m.selectedVariantId, onClick = { p.onSelectVariant(v.rawMovieId) }, modifier = Modifier.width(220.dp)) { Text(v.label, fontSize = 16.sp, maxLines = 1) } } } }
        }
        if (p.relatedContent.isNotEmpty()) item {
            Column { SbSectionTitle(tr("More like this", "أفلام مشابهة"))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(8.dp)) {
                    items(p.relatedContent, key = { it.id }) { r -> SbPoster(r.name, r.posterUrl ?: r.backdropUrl, r.year, { p.onRelatedClick(r) }, Modifier.width(160.dp)) }
                } }
        }
    }
}

/** Series: same hero; below, numbered season list (blue bar) beside episode rows with stills. */
@Composable
internal fun SbSeriesDetail(p: SeriesDetailParams) {
    val s = p.series
    val primary = remember { FocusRequester() }
    LaunchedEffect(s.id) { runCatching { primary.requestFocus() } }
    val resume = p.resumeEpisode
    val tSeason = tr("Season", "الموسم")
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(bottom = 40.dp)) {
        item {
            val badges = buildList { add(tr("SERIES", "مسلسل")); if (p.unwatchedEpisodeCount > 0) add("${p.unwatchedEpisodeCount} " + tr("new", "جديد")); if (s.rating > 0f) add("★ " + String.format(java.util.Locale.US, "%.1f", s.rating)) }
            SbDetailHero(s.backdropUrl, s.posterUrl, s.name, listOf(s.releaseDate?.take(4), "${s.seasons.size} " + tr("seasons", "مواسم"), s.genre?.split(',', '/', '|')?.take(2)?.joinToString(" · ") { it.trim() }), s.plot, badges) {
                SbActionTile(Icons.Outlined.PlayArrow, if (resume != null) tr("Continue", "متابعة") + " S${resume.seasonNumber}E${resume.episodeNumber}" else tr("Watch now", "مشاهدة الآن"),
                    { (resume ?: p.selectedSeason?.episodes?.firstOrNull())?.let(p.onResumeClick) }, Modifier.focusRequester(primary), wide = true)
                SbActionTile(if (s.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, tr("Favorite", "المفضلة"), p.onToggleFavorite)
                if (resume != null) SbActionTile(Icons.Outlined.Cast, tr("Cast", "بث"), p.onCastResumeEpisode)
                SbActionTile(Icons.AutoMirrored.Outlined.ArrowBack, tr("Back", "رجوع"), p.onBack)
            }
        }
        item {
            CpLtrRow(Modifier.fillMaxWidth().height(560.dp), spacing = 24.dp) { rtl ->
                rtl {
                    Column(Modifier.width(320.dp).fillMaxHeight()) {
                        SbSectionTitle(tr("Seasons", "المواسم"))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            items(s.seasons.size, key = { s.seasons[it].seasonNumber }) { i -> val season = s.seasons[i]
                                SbRow(season.seasonNumber == p.selectedSeason?.seasonNumber, onClick = { p.onSeasonSelected(season) }) {
                                    Text("${i + 1}", fontSize = 18.sp, color = NSub, modifier = Modifier.width(28.dp))
                                    Text(season.name.ifBlank { "$tSeason ${season.seasonNumber}" }, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                    Text("${season.episodes.size}", fontSize = 15.sp, color = Color.White.copy(alpha = 0.8f))
                                    androidx.compose.material3.Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, tint = androidx.tv.material3.LocalContentColor.current, modifier = Modifier.size(22.dp))
                                }
                            }
                        }
                    }
                }
                rtl {
                    Column(Modifier.weight(1f).fillMaxHeight()) {
                        SbSectionTitle(tr("Episodes", "الحلقات"), p.selectedSeason?.let { "${it.episodes.size}" })
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(p.selectedSeason?.episodes.orEmpty(), key = { it.id }) { e ->
                                SbRow(resume?.id == e.id, onClick = { p.onEpisodeClick(e) }, onLongClick = { p.onCopyEpisodeUrl(e) }) {
                                    Text("${e.episodeNumber}", fontSize = 20.sp, color = NSub, modifier = Modifier.width(34.dp))
                                    Box(Modifier.width(150.dp).aspectRatio(16f / 9f).clip(RoundedCornerShape(8.dp)).background(Color(0xFF171220))) {
                                        (e.coverUrl ?: p.selectedSeason?.coverUrl ?: s.backdropUrl)?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                                        if (e.watchProgress > 0 && e.durationSeconds > 0) Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(3.dp).background(Color(0x55FFFFFF))) {
                                            Box(Modifier.fillMaxHeight().fillMaxWidth((e.watchProgress / (e.durationSeconds * 1000f)).coerceIn(0f, 1f)).background(NBlue))
                                        }
                                    }
                                    Column(Modifier.weight(1f)) {
                                        Text(e.title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        e.plot?.takeIf { it.isNotBlank() }?.let { Text(it, fontSize = 13.sp, color = Color.White.copy(alpha = 0.75f), maxLines = 2, overflow = TextOverflow.Ellipsis) }
                                    }
                                    e.duration?.let { Text(it, fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f)) }
                                    androidx.compose.material3.Icon(Icons.Outlined.PlayCircle, null, tint = androidx.tv.material3.LocalContentColor.current, modifier = Modifier.size(28.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private val uvAr = listOf(listOf("ض","ص","ث","ق","ف","غ","ع","ه","خ","ح","ج"), listOf("ش","س","ي","ب","ل","ا","ت","ن","م","ك","ط"), listOf("ئ","ء","ؤ","ر","ى","ة","و","ز","ظ","د","ذ"))
private val uvEn = listOf(listOf("q","w","e","r","t","y","u","i","o","p","-"), listOf("a","s","d","f","g","h","j","k","l","'","&"), listOf("z","x","c","v","b","n","m",".",",","!","?"))
private val uvDigits = listOf("1","2","3","4","5","6","7","8","9","0","أ")

@Composable
private fun SbKey(label: String, modifier: Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector? = null, accent: Boolean = false, onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    TvClickableSurface(onClick = onClick, modifier = modifier.height(50.dp), shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = if (accent) NBlue.copy(alpha = 0.7f) else NGlass, focusedContainerColor = NBlue, contentColor = Color.White, focusedContentColor = Color.White),
        border = ClickableSurfaceDefaults.border(border = Border(androidx.compose.foundation.BorderStroke(1.dp, NLine), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f)) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (icon != null) androidx.compose.material3.Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp)) else Text(label, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Medium, maxLines = 1)
        }
    }
}

/** Search: big glass query bar with star, tab rail, square-key keyboard on one side, sectioned results grid on the other. */
@Composable
internal fun SbSearch(p: SearchParams) {
    LaunchedEffect(Unit) { runCatching { p.searchFocusRequester.requestFocus() } }
    val s = p.uiState
    var en by remember { mutableStateOf(false) }
    var digits by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(NGlass).border(1.dp, NLine, RoundedCornerShape(16.dp)).padding(horizontal = 18.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("✦", color = Color(0xFFFF8A90), fontSize = 24.sp)
            CgSearchField(p.query, p.onQueryChange, tr("Search movies, series, channels…", "ابحث عن فيلم، مسلسل أو قناة…"), Modifier.weight(1f).focusRequester(p.searchFocusRequester), onSubmit = p.onSearch)
            if (s.isLoading) Text("…", color = NSub, fontSize = 22.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SearchTab.entries.forEach { t ->
                val label = when (t) { SearchTab.ALL -> tr("All", "الكل"); SearchTab.LIVE -> tr("Live", "القنوات"); SearchTab.MOVIES -> tr("Movies", "الأفلام"); SearchTab.SERIES -> tr("Series", "المسلسلات") }
                val count = when (t) { SearchTab.LIVE -> s.channels.size; SearchTab.MOVIES -> s.movies.size; SearchTab.SERIES -> s.series.size; SearchTab.ALL -> s.totalResults }
                SbRow(t == p.selectedTab, onClick = { p.onTabSelected(t) }, modifier = Modifier.width(190.dp)) {
                    Text(label, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    if (s.hasSearched) Text("$count", fontSize = 15.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }
        }
        CpLtrRow(Modifier.weight(1f).fillMaxWidth(), spacing = 24.dp) { rtl ->
            rtl {
                Column(Modifier.width(600.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val rows = (if (digits) listOf(uvDigits) else emptyList()) + (if (en) uvEn else uvAr)
                    rows.forEach { r -> Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { r.forEach { k -> SbKey(k, Modifier.weight(1f)) { p.onQueryChange(p.query + k) } } } }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SbKey(if (en) "ع" else "EN", Modifier.weight(1.2f)) { en = !en }
                        SbKey("123", Modifier.weight(1.2f)) { digits = !digits }
                        SbKey(tr("space", "مسافة"), Modifier.weight(3f)) { p.onQueryChange(p.query + " ") }
                        SbKey("", Modifier.weight(1.2f), icon = Icons.AutoMirrored.Outlined.Backspace) { p.onQueryChange(p.query.dropLast(1)) }
                        SbKey("", Modifier.weight(1.5f), icon = Icons.Outlined.Search, accent = true) { p.onSearch() }
                    }
                    if (p.recentQueries.isNotEmpty()) {
                        SbSectionTitle(tr("Recent", "عمليات البحث الأخيرة"))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(p.recentQueries) { q -> SbRow(false, onClick = { p.onRecentQuerySelected(q) }, modifier = Modifier.widthIn(min = 110.dp)) { androidx.compose.material3.Icon(Icons.Outlined.History, null, tint = androidx.tv.material3.LocalContentColor.current, modifier = Modifier.size(18.dp)); Text(q, fontSize = 15.sp, maxLines = 1) } }
                            item { SbRow(false, onClick = p.onClearRecentQueries, modifier = Modifier.width(120.dp)) { Text(tr("Clear", "مسح"), fontSize = 15.sp) } }
                        }
                    }
                }
            }
            rtl {
                Box(Modifier.weight(1f).fillMaxHeight()) {
                    val showLive = p.selectedTab == SearchTab.ALL || p.selectedTab == SearchTab.LIVE
                    val showMov = p.selectedTab == SearchTab.ALL || p.selectedTab == SearchTab.MOVIES
                    val showSer = p.selectedTab == SearchTab.ALL || p.selectedTab == SearchTab.SERIES
                    when {
                        !s.hasSearched && p.query.isBlank() -> Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                            SbLogo(size = 54)
                            Text(tr("Type to search everything", "اكتب للبحث في كل المحتوى"), color = NSub, fontSize = 18.sp)
                        }
                        s.isEmpty -> CgEmpty(tr("No results", "لا توجد نتائج"))
                        else -> LazyVerticalGrid(GridCells.Fixed(5), Modifier.fillMaxSize(), contentPadding = PaddingValues(6.dp, 4.dp, 6.dp, 40.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            if (showLive && s.channels.isNotEmpty()) {
                                item(span = { GridItemSpan(maxLineSpan) }) { SbSectionTitle(tr("Channels", "القنوات"), "${s.channels.size}") }
                                items(s.channels.size, key = { "c" + s.channels[it].id }) { i -> val c = s.channels[i]
                                    CgCard(onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c) }, container = NGlass, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().aspectRatio(1.4f)) {
                                        Column(Modifier.fillMaxSize().padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                            CpLogoBox(c.name, if (p.isChannelLocked(c)) null else c.logoUrl, 80.dp, 48.dp)
                                            Spacer(Modifier.height(6.dp))
                                            Text(c.name, color = Color.White, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                            if (showMov && s.movies.isNotEmpty()) {
                                item(span = { GridItemSpan(maxLineSpan) }) { SbSectionTitle(tr("Movies", "الأفلام"), "${s.movies.size}") }
                                items(s.movies.size, key = { "m" + s.movies[it].id }) { i -> val m = s.movies[i]
                                    SbPoster(m.name, m.posterUrl, m.year, { p.onMovieClick(m) }, locked = p.isMovieLocked(m), onLongClick = { p.onMovieLongClick(m) }) }
                            }
                            if (showSer && s.series.isNotEmpty()) {
                                item(span = { GridItemSpan(maxLineSpan) }) { SbSectionTitle(tr("Series", "المسلسلات"), "${s.series.size}") }
                                items(s.series.size, key = { "s" + s.series[it].id }) { i -> val x = s.series[i]
                                    SbPoster(x.name, x.posterUrl, x.releaseDate?.take(4), { p.onSeriesClick(x) }, locked = p.isSeriesLocked(x), onLongClick = { p.onSeriesLongClick(x) }) }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun uvSettingsIcon(t: String): androidx.compose.ui.graphics.vector.ImageVector {
    val l = t.lowercase()
    return when {
        "playlist" in l || "قائم" in l || "provider" in l -> Icons.Outlined.PlaylistPlay
        "player" in l || "playback" in l || "مشغل" in l || "تشغيل" in l -> Icons.Outlined.PlayCircle
        "epg" in l || "guide" in l || "دليل" in l -> Icons.Outlined.CalendarMonth
        "parent" in l || "lock" in l || "أبوي" in l || "رقاب" in l -> Icons.Outlined.Lock
        "theme" in l || "appear" in l || "مظهر" in l || "ثيم" in l -> Icons.Outlined.Palette
        "lang" in l || "لغة" in l -> Icons.Outlined.Language
        "backup" in l || "نسخ" in l -> Icons.Outlined.Backup
        "about" in l || "حول" in l -> Icons.Outlined.Info
        "record" in l || "تسجيل" in l -> Icons.Outlined.FiberManualRecord
        else -> Icons.Outlined.Settings
    }
}

/** Settings: numbered blue-bar list with line icons, Sabhiya logo on top. */
@Composable
internal fun SbSettingsNav(p: SettingsNavParams) {
    Column(Modifier.width(380.dp).fillMaxHeight()) {
        Row(Modifier.padding(bottom = 14.dp, start = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            androidx.compose.material3.Icon(Icons.Outlined.Settings, null, tint = Color.White, modifier = Modifier.size(30.dp))
            Text(tr("Settings", "الإعدادات"), color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        }
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            items(p.entries.size) { i ->
                val (label, sub) = p.entries[i]
                SbRow(i == p.selectedCategory, onClick = { p.onCategorySelected(i) }, modifier = if (i == p.selectedCategory) Modifier.focusRequester(p.focusRequester) else Modifier) {
                    Text("${i + 1}", fontSize = 17.sp, color = NSub, modifier = Modifier.width(26.dp))
                    androidx.compose.material3.Icon(uvSettingsIcon("$label $sub"), null, tint = androidx.tv.material3.LocalContentColor.current, modifier = Modifier.size(24.dp))
                    Column(Modifier.weight(1f)) {
                        Text(label, fontSize = 18.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (sub.isNotBlank()) Text(sub, fontSize = 12.sp, color = Color.White.copy(alpha = 0.65f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    androidx.compose.material3.Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, tint = androidx.tv.material3.LocalContentColor.current, modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}

@Composable
internal fun SbSettingsFrame(navigation: @Composable () -> Unit, content: @Composable () -> Unit) {
    CpLtrRow(Modifier.fillMaxSize(), spacing = 24.dp) { rtl ->
        rtl { navigation() }
        rtl {
            Box(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(20.dp)).background(Color(0x400A1440)).border(1.dp, NLine, RoundedCornerShape(20.dp))) {
                SbRings(Modifier.fillMaxSize().alpha(0.18f))
                Box(Modifier.fillMaxSize().padding(28.dp)) { content() }
            }
        }
    }
}
