package com.streamvault.app.ui.themes.sabhiya

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.streamvault.app.navigation.Routes
import com.streamvault.app.ui.components.PlayerRenderView
import com.streamvault.app.ui.themes.bespoke.*
import com.streamvault.domain.model.Category
import com.streamvault.domain.model.Channel
import com.streamvault.player.PlayerSurfaceResizeMode

/* Sabhiya (صبحية): charcoal #0B0D11, Netflix red #E50914 glow. Bespoke shell, home and live (A cards / B three columns). */

internal object SBX {
    val Bg = Color(0xFF0B0D11)
    val Panel = Color(0xFF0F1115)
    val Card = Color(0xFF161920)
    val Red = Color(0xFFE50914)
    val RedSoft = Color(0x33E50914)
    val Green = Color(0xFF22C55E)
    val Line = Color(0x1FFFFFFF)
    val Sub = Color(0xFFB9BEC8)
    val Faint = Color(0xFF6B717C)
    val PanelShape = RoundedCornerShape(16.dp)
}

private class SbsNav(val route: String, val icon: ImageVector, val en: String, val ar: String)

@Composable
private fun SbsLogo() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(SBX.Red), contentAlignment = Alignment.Center) { SbIcon(Icons.Outlined.PlayArrow, size = 28.dp) }
        Column {
            Text("صبحية", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Text("Sabhiya IPTV", color = SBX.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
internal fun SbsShell(p: ShellParams) {
    val navs = listOf(
        SbsNav(Routes.HOME, Icons.Outlined.Home, "Home", "الرئيسية"),
        SbsNav(Routes.LIVE_TV, Icons.Outlined.LiveTv, "Live TV", "البث المباشر"),
        SbsNav(Routes.MOVIES, Icons.Outlined.Movie, "Movies", "الأفلام"),
        SbsNav(Routes.SERIES, Icons.Outlined.VideoLibrary, "Series", "المسلسلات"),
        SbsNav(Routes.FAVORITES, Icons.Outlined.FavoriteBorder, "Favorites", "المفضلة"),
        SbsNav(Routes.FAVORITES + "#recent", Icons.Outlined.History, "Recently watched", "المشاهدة الأخيرة"),
        SbsNav(Routes.EPG, Icons.Outlined.PlaylistPlay, "Playlist", "قائمة التشغيل"),
        SbsNav(Routes.SEARCH, Icons.Outlined.Search, "Search", "البحث"),
        SbsNav(Routes.SETTINGS, Icons.Outlined.Settings, "Settings", "الإعدادات")
    )
    CompositionLocalProvider(LocalCpNavigate provides p.onNavigate) {
        Box(p.modifier.fillMaxSize().background(SBX.Bg)) {
            Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0x24E50914), Color.Transparent), center = androidx.compose.ui.geometry.Offset(0f, 0f), radius = 1100f)))
            Row(Modifier.fillMaxSize()) {
                // sidebar sits at the start edge (right side in RTL)
                Column(
                    Modifier.fillMaxHeight().width(250.dp).background(SBX.Panel).padding(vertical = 22.dp, horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(Modifier.padding(start = 6.dp, bottom = 18.dp)) { SbsLogo() }
                    navs.forEach { n ->
                        val target = n.route.substringBefore('#')
                        val isRec = n.route.contains('#')
                        val sel = onRoute(p.currentRoute, target) && (target != Routes.FAVORITES || SbRecent.active == isRec)
                        SbFocus({ SbRecent.active = isRec; p.onNavigate(target) }, Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp),
                            color = if (sel) SBX.Red else Color.Transparent, focusedColor = if (sel) SBX.Red else Color(0x26E50914), scale = 1.04f) {
                            Row(Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                SbIcon(n.icon, Color.White, 24.dp)
                                Text(tr(n.en, n.ar), color = Color.White, fontSize = 18.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium, maxLines = 1)
                            }
                            if (sel) Box(Modifier.align(Alignment.CenterEnd).width(4.dp).fillMaxHeight(0.6f).clip(CircleShape).background(Color.White))
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0x0FFFFFFF)).padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        val ok = CpServer.healthy
                        Box(Modifier.size(10.dp).clip(CircleShape).background(when (ok) { true -> SBX.Green; false -> Color(0xFFEF4444); null -> Color(0xFF9CA3AF) }))
                        Column {
                            Text(when (ok) { true -> tr("Server connected", "الخادم متصل"); false -> tr("Server problem", "مشكلة في الخادم"); null -> tr("Checking server…", "جارٍ فحص الخادم…") }, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(p.subtitle?.takeIf { it.isNotBlank() } ?: "Alaa IPTV", color = SBX.Faint, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                Column(Modifier.weight(1f).fillMaxHeight().padding(p.contentPadding)) {
                    if (p.topBarVisible) SbsTopBar(p)
                    Column(Modifier.fillMaxSize().padding(horizontal = 28.dp)) {
                        if (p.showScreenHeader && !onRoute(p.currentRoute, Routes.HOME)) p.header?.let { Column(content = it) }
                        p.content(this)
                    }
                }
            }
        }
    }
}

@Composable
private fun SbsTopBar(p: ShellParams) {
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { kotlinx.coroutines.delay(30_000); now = System.currentTimeMillis() } }
    val date = remember(now / 60_000) { java.text.SimpleDateFormat("EEEE d MMMM yyyy", java.util.Locale("ar")).format(java.util.Date(now)) }
    Box(Modifier.fillMaxWidth().height(80.dp).padding(horizontal = 28.dp)) {
        Row(Modifier.align(Alignment.CenterStart), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(if (onRoute(p.currentRoute, Routes.HOME)) "" else p.title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 260.dp))
        }
        SbFocus({ p.onNavigate(Routes.SEARCH) }, Modifier.align(Alignment.Center).width(460.dp).height(50.dp), shape = RoundedCornerShape(50), color = Color(0x14FFFFFF), focusedColor = Color(0x22FFFFFF), scale = 1.03f) {
            Row(Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SbIcon(Icons.Outlined.Search, SBX.Sub, 22.dp)
                Text(tr("Search movies, series, channels…", "ابحث عن فيلم، مسلسل أو قناة…"), color = SBX.Faint, fontSize = 16.sp, maxLines = 1)
            }
        }
        Row(Modifier.align(Alignment.CenterEnd), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            p.topBarActions?.let { Row(horizontalArrangement = Arrangement.spacedBy(8.dp), content = it) }
            Box { SbRound(Icons.Outlined.Notifications, 44.dp) { p.onNavigate(Routes.SETTINGS) }; Box(Modifier.align(Alignment.TopEnd).padding(7.dp).size(9.dp).clip(CircleShape).background(SBX.Red)) }
            SbRound(Icons.Outlined.Person, 44.dp) { p.onNavigate(Routes.SETTINGS) }
            SbRound(Icons.Outlined.Settings, 44.dp) { p.onNavigate(Routes.SETTINGS) }
            Column(horizontalAlignment = Alignment.End) {
                Text(sbTime(now), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text(date, color = SBX.Sub, fontSize = 12.sp, maxLines = 1)
            }
        }
    }
}

// ─────────── HOME ───────────

private class SbsCat(val en: String, val ar: String, val icon: ImageVector, val c1: Color, val c2: Color, val count: Int, val route: String)

@Composable
internal fun SbsHome(p: DashboardParams) {
    val s = p.uiState
    androidx.compose.runtime.LaunchedEffect(s.providerHealth.status) { CpServer.healthy = when (s.providerHealth.status.name) { "ACTIVE" -> true; "ERROR", "EXPIRED", "DISABLED" -> false; else -> null } }
    val first = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { first.requestFocus() } }
    val movie = (s.recommendedMovies + s.recentMovies).firstOrNull { it.backdropUrl != null || it.posterUrl != null }
    val cats = listOf(
        SbsCat("Live TV", "البث المباشر", Icons.Outlined.LiveTv, Color(0xFFE50914), Color(0xFF7A0410), s.stats.liveChannelCount, Routes.LIVE_TV),
        SbsCat("Movies", "الأفلام", Icons.Outlined.Movie, Color(0xFF7C3AED), Color(0xFF3B1A78), s.stats.movieLibraryCount, Routes.MOVIES),
        SbsCat("Series", "المسلسلات", Icons.Outlined.VideoLibrary, Color(0xFF2563EB), Color(0xFF12306E), s.stats.seriesLibraryCount, Routes.SERIES),
        SbsCat("Kids", "أطفال", Icons.Outlined.ChildCare, Color(0xFFF59E0B), Color(0xFF7A4A04), 0, Routes.LIVE_TV),
        SbsCat("News", "أخبار", Icons.Outlined.Newspaper, Color(0xFF0EA5E9), Color(0xFF07506F), 0, Routes.LIVE_TV),
        SbsCat("Sports", "رياضة", Icons.Outlined.SportsSoccer, Color(0xFF16A34A), Color(0xFF0A4F24), 0, Routes.LIVE_TV)
    )
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(Modifier.fillMaxWidth().height(250.dp).shadow(20.dp, SBX.PanelShape).clip(SBX.PanelShape).background(SBX.Panel)) {
            (movie?.backdropUrl ?: movie?.posterUrl ?: s.feature.artworkUrl)?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
            Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xF20B0D11), Color(0x990B0D11), Color.Transparent))))
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.6f to Color.Transparent, 1f to SBX.Bg)))
            Column(Modifier.align(Alignment.CenterStart).fillMaxWidth(0.55f).padding(start = 44.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SbTag(tr("Featured", "مميز اليوم"), SBX.Red)
                Text(movie?.name ?: s.feature.title.ifBlank { "صبحية" }, color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis, lineHeight = 44.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    movie?.year?.let { Text(it, color = SBX.Sub, fontSize = 17.sp) }
                    movie?.rating?.takeIf { it > 0 }?.let { Text("★ %.1f".format(it), color = Color(0xFFF5C518), fontSize = 17.sp, fontWeight = FontWeight.Bold) }
                    movie?.genre?.split(",")?.firstOrNull()?.let { Text(it.trim(), color = SBX.Sub, fontSize = 17.sp) }
                }
                Text(movie?.plot?.takeIf { it.isNotBlank() } ?: s.feature.summary, color = Color(0xE6FFFFFF), fontSize = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 22.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    SbButton(tr("Watch now", "مشاهدة الآن"), Icons.Outlined.PlayArrow, { movie?.let(p.onMovieClick) ?: p.onNavigate(Routes.MOVIES) }, Modifier.focusRequester(first))
                    SbButton(tr("More info", "مزيد من المعلومات"), Icons.Outlined.Info, { movie?.let(p.onMovieClick) ?: p.onNavigate(Routes.MOVIES) }, primary = false)
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            cats.forEach { c ->
                SbFocus({ p.onNavigate(c.route) }, Modifier.weight(1f).height(104.dp), shape = SBX.PanelShape, color = Color.Transparent, focusedColor = Color.Transparent, scale = 1.08f) {
                    Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(c.c1, c.c2))))
                    Box(Modifier.align(Alignment.TopEnd).padding(12.dp).size(70.dp).clip(CircleShape).background(Color(0x1FFFFFFF)))
                    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                        SbIcon(c.icon, Color.White, 36.dp)
                        Column {
                            Text(tr(c.en, c.ar), color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text(if (c.count > 0) "${c.count}" else tr("Browse", "تصفح"), color = Color(0xCCFFFFFF), fontSize = 14.sp)
                        }
                    }
                }
            }
        }
        val cont = s.continueWatching.take(10)
        SbRail(tr("Continue watching", "متابعة المشاهدة"), cont.size.takeIf { it > 0 }) {
            if (cont.isEmpty()) { if (s.isLoading) repeat(4) { SbSkeleton(Modifier.width(320.dp).aspectRatio(16f / 9f)) } else Text(tr("Nothing yet", "لا يوجد شيء بعد"), color = SBX.Faint, fontSize = 17.sp) }
            cont.forEach { h ->
                val pr = if (h.totalDurationMs > 0) h.resumePositionMs.toFloat() / h.totalDurationMs else null
                SbWide(h.title, h.posterUrl, if (h.seasonNumber != null) "S${h.seasonNumber} • E${h.episodeNumber ?: 1}" else null, pr, { p.onContinueWatchingItemClick(h) })
            }
        }
        val movies = s.recentMovies.ifEmpty { s.recommendedMovies }.take(14)
        SbRail(tr("New movies", "أحدث الأفلام")) {
            if (movies.isEmpty()) repeat(7) { SbSkeleton(Modifier.width(170.dp).aspectRatio(2f / 3f)) }
            movies.forEach { m -> SbPoster(m.name, m.posterUrl, m.rating, m.year, sbQuality(m.name), { p.onMovieClick(m) }) }
        }
        SbRail(tr("Series", "المسلسلات")) {
            if (s.recentSeries.isEmpty()) repeat(7) { SbSkeleton(Modifier.width(170.dp).aspectRatio(2f / 3f)) }
            s.recentSeries.take(14).forEach { m -> SbPoster(m.name, m.posterUrl, m.rating, m.releaseDate?.take(4), m.seasons.size.takeIf { it > 0 }?.let { "S$it" }, { p.onSeriesClick(m) }) }
        }
        Spacer(Modifier.height(30.dp))
    }
}

// ─────────── LIVE ───────────

private fun catStyle(name: String): Pair<ImageVector, Color> {
    val n = name.lowercase()
    return when {
        listOf("sport", "رياض", "bein", "ssc").any { it in n } -> Icons.Outlined.SportsSoccer to Color(0xFF16A34A)
        listOf("news", "أخبار", "اخبار").any { it in n } -> Icons.Outlined.Newspaper to Color(0xFF0EA5E9)
        listOf("kid", "أطفال", "اطفال", "cartoon").any { it in n } -> Icons.Outlined.ChildCare to Color(0xFFF59E0B)
        listOf("movie", "أفلام", "افلام", "cinema").any { it in n } -> Icons.Outlined.Movie to Color(0xFF7C3AED)
        listOf("series", "مسلسل").any { it in n } -> Icons.Outlined.VideoLibrary to Color(0xFF2563EB)
        listOf("music", "موسيق").any { it in n } -> Icons.Outlined.MusicNote to Color(0xFFEC4899)
        listOf("doc", "وثائق").any { it in n } -> Icons.Outlined.Public to Color(0xFF14B8A6)
        listOf("relig", "islam", "إسلام", "اسلام", "quran", "قرآن").any { it in n } -> Icons.Outlined.Mosque to Color(0xFF84CC16)
        else -> Icons.Outlined.LiveTv to Color(0xFFE50914)
    }
}

internal object SbLiveMode { var startB = true }

@Composable
internal fun SbsLive(p: LiveTvParams) {
    var modeB by rememberSaveable { mutableStateOf(SbLiveMode.startB) }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(p.sourceTitle.ifBlank { tr("Live TV", "البث المباشر") }, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), maxLines = 1)
            SbChip(tr("Categories", "الأقسام"), !modeB, icon = Icons.Outlined.GridView) { modeB = false }
            SbChip(tr("Channels", "القنوات"), modeB, icon = Icons.Outlined.ViewColumn) { modeB = true }
        }
        if (modeB) SbsLiveColumns(p) else SbsLiveCards(p) { modeB = true }
    }
}

@Composable
private fun SbsLiveCards(p: LiveTvParams, openChannels: () -> Unit) {
    if (p.categories.isEmpty()) {
        LazyVerticalGrid(GridCells.Fixed(5), horizontalArrangement = Arrangement.spacedBy(18.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) { items(List(10) { it }) { SbSkeleton(Modifier.fillMaxWidth().height(150.dp)) } }
        return
    }
    LazyVerticalGrid(GridCells.Adaptive(220.dp), Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(10.dp, 10.dp, 10.dp, 48.dp)) {
        items(p.categories, key = { "c${it.id}" }) { cat ->
            val (ic, col) = catStyle(cat.name)
            SbFocus({ p.onCategoryClick(cat); openChannels() }, Modifier.fillMaxWidth().height(118.dp).focusRequester(p.categoryRequester(cat.id)), shape = SBX.PanelShape, color = Color.Transparent, focusedColor = Color.Transparent, scale = 1.08f,
                onLongClick = { p.onCategoryLongClick(cat) }, onFocus = { if (it) p.onCategoryFocused(cat) }) {
                Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(col, col.copy(alpha = 0.35f), SBX.Card))))
                Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(42.dp).clip(CircleShape).background(Color(0x33FFFFFF)), contentAlignment = Alignment.Center) { SbIcon(ic, Color.White, 24.dp) }
                        Spacer(Modifier.weight(1f))
                        if (p.isCategoryLocked(cat)) SbIcon(Icons.Outlined.Lock, Color.White, 20.dp)
                    }
                    Column {
                        Text(cat.name, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${cat.count} ${tr("channels", "قناة")}", color = Color(0xCCFFFFFF), fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SbsLiveColumns(p: LiveTvParams) {
    var focused by remember { mutableStateOf<Channel?>(null) }
    val foc = focused ?: p.previewChannel ?: p.channels.firstOrNull()
    val selCat = p.categories.firstOrNull { it.id == p.selectedCategoryId }
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        // column 1: categories with icons + counts
        Column(Modifier.width(150.dp).fillMaxHeight().clip(SBX.PanelShape).background(SBX.Panel).border(1.dp, SBX.Line, SBX.PanelShape).padding(10.dp)) {
            Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SbIcon(Icons.Outlined.LiveTv, Color.White, 24.dp)
                Text(tr("Categories", "الفئات"), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
                if (p.categories.isEmpty()) items(8) { SbSkeleton(Modifier.fillMaxWidth().height(46.dp)) }
                itemsIndexed(p.categories, key = { i, c -> "k${c.id}-$i" }) { _, cat ->
                    val sel = cat.id == p.selectedCategoryId
                    val (ic, col) = catStyle(cat.name)
                    SbFocus({ p.onCategoryClick(cat) }, Modifier.fillMaxWidth().height(48.dp).focusRequester(p.categoryRequester(cat.id)), shape = RoundedCornerShape(10.dp),
                        color = if (sel) SBX.RedSoft else Color.Transparent, focusedColor = Color(0x40E50914), scale = 1.03f,
                        onLongClick = { p.onCategoryLongClick(cat) }, onFocus = { if (it) p.onCategoryFocused(cat) }) {
                        Row(Modifier.fillMaxSize().padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SbIcon(ic, if (sel) Color.White else col, 22.dp)
                            Text(cat.name, color = Color.White, fontSize = 16.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            if (p.isCategoryLocked(cat)) SbIcon(Icons.Outlined.Lock, SBX.Faint, 16.dp)
                            Text("${cat.count}", color = if (sel) Color.White else SBX.Faint, fontSize = 13.sp)
                        }
                        if (sel) Box(Modifier.fillMaxSize().border(1.5.dp, SBX.Red, RoundedCornerShape(10.dp)))
                    }
                }
            }
        }
        // column 2: channel list
        Column(Modifier.weight(1f).fillMaxHeight()) {
            Column(Modifier.fillMaxSize().clip(SBX.PanelShape).background(SBX.Panel).border(1.dp, SBX.Line, SBX.PanelShape).padding(10.dp)) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(selCat?.name ?: tr("All channels", "جميع القنوات"), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("${p.channels.size}", color = SBX.Faint, fontSize = 14.sp)
                    Spacer(Modifier.weight(1f))
                    Row(Modifier.width(96.dp).height(38.dp).clip(RoundedCornerShape(50)).background(Color(0x14FFFFFF)).padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SbIcon(Icons.Outlined.Search, SBX.Sub, 18.dp)
                        Box(Modifier.weight(1f)) {
                            
                            BasicTextField(p.channelSearchQuery, p.onChannelSearchChange, singleLine = true, textStyle = TextStyle(color = Color.White, fontSize = 15.sp), cursorBrush = SolidColor(SBX.Red), modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Text("#", color = SBX.Faint, fontSize = 13.sp, modifier = Modifier.width(26.dp))
                    Text(tr("Channel", "القناة"), color = SBX.Faint, fontSize = 13.sp, modifier = Modifier.weight(1f))
                }
                when {
                    p.channels.isEmpty() && p.categories.isEmpty() -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { repeat(7) { SbSkeleton(Modifier.fillMaxWidth().height(50.dp)) } }
                    p.channels.isEmpty() -> SbEmpty(Icons.Outlined.TvOff, tr("No channels here", "لا توجد قنوات هنا"), tr("Pick another category", "اختر فئة أخرى"))
                    else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp), contentPadding = PaddingValues(4.dp)) {
                        itemsIndexed(p.channels, key = { i, c -> "ch${c.id}-$i" }) { i, c ->
                            SbFocus({ p.onChannelClick(c) }, Modifier.fillMaxWidth().height(58.dp).focusRequester(p.channelRequester(c.id)), shape = RoundedCornerShape(10.dp),
                                color = if (p.movingChannelId == c.id) SBX.RedSoft else Color.Transparent, focusedColor = Color(0x40E50914), scale = 1.02f,
                                onLongClick = { p.onChannelLongClick(c) }, onFocus = { if (it) { focused = c; p.onChannelFocused(c) } }) { f ->
                                Row(Modifier.fillMaxSize().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("${if (c.number > 0) c.number else i + 1}", color = SBX.Sub, fontSize = 14.sp, modifier = Modifier.width(26.dp))
                                    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Box(Modifier.size(40.dp, 30.dp).clip(RoundedCornerShape(6.dp)).background(Color(0x14FFFFFF)), contentAlignment = Alignment.Center) {
                                            c.logoUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(3.dp)) } ?: Text(c.name.take(2), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Column(Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(c.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                                                
                                            }
                                            Text(listOfNotNull(c.currentProgram?.let { sbTime(it.startTime) }, c.currentProgram?.title).joinToString("  "), color = SBX.Sub, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (c.isFavorite) SbIcon(Icons.Outlined.Favorite, SBX.Red, 16.dp)
                                    }
                                }
                                if (f) Box(Modifier.fillMaxSize().border(1.5.dp, SBX.Red, RoundedCornerShape(10.dp)))
                            }
                        }
                    }
                }
            }
        }
        // column 3: big preview (~45%) + info + actions
        Column(Modifier.weight(1.4f).fillMaxHeight().clip(SBX.PanelShape).background(SBX.Panel).border(1.dp, SBX.Line, SBX.PanelShape).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SbFocus({ foc?.let(p.onChannelClick) }, Modifier.fillMaxWidth().aspectRatio(16f / 9f).focusRequester(p.previewFocusRequester), shape = RoundedCornerShape(12.dp), color = Color.Black, focusedColor = Color.Black, scale = 1.02f) {
                val eng = p.previewPlayerEngine
                if (eng != null && p.previewChannel != null) PlayerRenderView(eng, PlayerSurfaceResizeMode.FIT, Modifier.fillMaxSize())
                else foc?.logoUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(40.dp)) }
                if (p.isPreviewLoading) Text("…", color = Color.White, fontSize = 26.sp, modifier = Modifier.align(Alignment.Center))
                Box(Modifier.align(Alignment.TopStart).padding(10.dp)) { SbTag("LIVE", SBX.Red) }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(54.dp, 40.dp).clip(RoundedCornerShape(8.dp)).background(Color(0x14FFFFFF))) { foc?.logoUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(3.dp)) } }
                Text(foc?.name ?: tr("Pick a channel", "اختر قناة"), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            }
            Text(foc?.currentProgram?.title ?: tr("No guide info", "لا يوجد دليل برامج"), color = SBX.Sub, fontSize = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            foc?.currentProgram?.let { pr ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(sbTime(pr.startTime), color = SBX.Sub, fontSize = 13.sp)
                    Box(Modifier.weight(1f).height(5.dp).clip(CircleShape).background(Color(0x22FFFFFF))) { Box(Modifier.fillMaxHeight().fillMaxWidth(sbProgress(foc) ?: 0f).background(SBX.Red)) }
                    Text(sbTime(pr.endTime), color = SBX.Sub, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.weight(1f))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SbsMini(Icons.Outlined.Info, tr("Info", "معلومات"), Modifier.weight(1f)) { foc?.let(p.onChannelLongClick) }
                SbsMini(Icons.Outlined.FiberManualRecord, tr("Record", "تسجيل"), Modifier.weight(1f)) { foc?.let(p.onChannelLongClick) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SbsMini(Icons.Outlined.FavoriteBorder, tr("Favorite", "مفضلة"), Modifier.weight(1f)) { foc?.let(p.onChannelLongClick) }
                SbsMini(Icons.Outlined.SwapHoriz, tr("Switch", "تبديل القناة"), Modifier.weight(1f)) { foc?.let(p.onChannelClick) }
            }
        }
    }
}

@Composable
private fun SbsMini(icon: ImageVector, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    SbFocus(onClick, modifier.height(42.dp), shape = RoundedCornerShape(10.dp), color = Color(0x14FFFFFF), focusedColor = SBX.Red, scale = 1.06f) {
        Row(Modifier.align(Alignment.Center).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SbIcon(icon, Color.White, 18.dp); Text(label, color = Color.White, fontSize = 13.sp, maxLines = 1)
        }
    }
}

/** المشاهدة الأخيرة: real playback history (continue watching + recently watched channels). */
@Composable
internal fun SbsRecent(p: com.streamvault.app.ui.themes.bespoke.FavoritesParams) {
    val df = remember { java.text.SimpleDateFormat("d MMM  HH:mm", java.util.Locale("ar")) }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SbIcon(Icons.Outlined.History, SBX.Red, 28.dp)
            Text(tr("Recently watched", "المشاهدة الأخيرة"), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("${p.continueWatching.size + p.recentLive.size} " + tr("items", "عنصر"), color = SBX.Faint, fontSize = 14.sp)
        }
        val later = rememberSbWatchLaterItems()
        if (p.continueWatching.isEmpty() && p.recentLive.isEmpty() && later.isEmpty()) {
            SbEmpty(Icons.Outlined.History, tr("No history yet", "لا يوجد سجل مشاهدة بعد"), tr("What you watch shows up here", "ما تشاهده سيظهر هنا")); return
        }
        LazyVerticalGrid(GridCells.Fixed(4), Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(6.dp, 6.dp, 6.dp, 48.dp)) {
            if (later.isNotEmpty()) {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SbIcon(Icons.Outlined.WatchLater, SBX.Red, 20.dp)
                        Text(tr("Watch later", "المشاهدة لاحقاً"), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("${later.size}", color = SBX.Faint, fontSize = 14.sp)
                    }
                }
                items(later, key = { "wl${it.id}" }) { w ->
                    val open = {
                        p.onItemClick(com.streamvault.app.ui.screens.favorites.FavoriteUiModel(
                            com.streamvault.domain.model.Favorite(providerId = w.providerId, contentId = w.contentId, contentType = w.contentType), w.title, providerId = w.providerId))
                    }
                    SbFocus(open, Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = SBX.Card, focusedColor = SBX.Card, scale = 1.05f, fill = false) { f ->
                        Column {
                            Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f).background(Color(0x14FFFFFF))) {
                                w.posterUrl?.let { AsyncImage(it, w.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xCC000000)))))
                                Box(Modifier.align(Alignment.TopStart).padding(8.dp).size(30.dp).clip(CircleShape).background(SBX.Red), contentAlignment = Alignment.Center) { SbIcon(Icons.Outlined.WatchLater, Color.White, 18.dp) }
                            }
                            Column(Modifier.padding(10.dp)) {
                                Text(w.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(if (w.contentType == com.streamvault.domain.model.ContentType.SERIES) tr("Series", "مسلسل") else tr("Movie", "فيلم"), color = SBX.Faint, fontSize = 12.sp, maxLines = 1)
                            }
                        }
                        if (f) Box(Modifier.matchParentSize().border(2.dp, SBX.Red, RoundedCornerShape(12.dp)))
                    }
                }
            }
            if (p.continueWatching.isNotEmpty()) {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) { Text(tr("Continue watching", "متابعة المشاهدة"), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                items(p.continueWatching, key = { "rc${it.history.id}" }) { h ->
                    val tot = h.history.totalDurationMs
                    val prog = if (tot > 0) (h.history.resumePositionMs.toFloat() / tot).coerceIn(0f, 1f) else 0f
                    SbFocus({ p.onHistoryClick(h) }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = SBX.Card, focusedColor = SBX.Card, scale = 1.05f, fill = false) { f ->
                        Column {
                            Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f).background(Color(0x14FFFFFF))) {
                                h.history.posterUrl?.let { AsyncImage(it, h.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xCC000000)))))
                                Box(Modifier.align(Alignment.Center).size(44.dp).clip(CircleShape).background(SBX.Red), contentAlignment = Alignment.Center) { SbIcon(Icons.Outlined.PlayArrow, Color.White, 26.dp) }
                                if (tot > 0) Text("${((tot - h.history.resumePositionMs).coerceAtLeast(0) / 60000)} " + tr("min left", "د متبقية"), color = Color.White, fontSize = 11.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp))
                                Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(4.dp).background(Color(0x44FFFFFF))) { Box(Modifier.fillMaxHeight().fillMaxWidth(prog).background(SBX.Red)) }
                            }
                            Column(Modifier.padding(10.dp)) {
                                Text(h.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(h.subtitle, color = SBX.Faint, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        if (f) Box(Modifier.matchParentSize().border(2.dp, SBX.Red, RoundedCornerShape(12.dp)))
                    }
                }
            }
            if (p.recentLive.isNotEmpty()) {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) { Text(tr("Recently watched channels", "قنوات شوهدت مؤخراً"), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                items(p.recentLive, key = { "rl${it.history.id}" }) { h ->
                    SbFocus({ p.onHistoryClick(h) }, Modifier.fillMaxWidth().height(76.dp), shape = RoundedCornerShape(12.dp), color = SBX.Card, focusedColor = Color(0x40E50914), scale = 1.04f) { f ->
                        Row(Modifier.fillMaxSize().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.size(56.dp, 40.dp).clip(RoundedCornerShape(8.dp)).background(Color(0x14FFFFFF)), contentAlignment = Alignment.Center) {
                                h.history.posterUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(3.dp)) } ?: Text(h.title.take(2), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(Modifier.weight(1f)) {
                                Text(h.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(if (h.history.lastWatchedAt > 0) df.format(java.util.Date(h.history.lastWatchedAt)) else h.subtitle, color = SBX.Faint, fontSize = 12.sp, maxLines = 1)
                            }
                            SbTag("LIVE", SBX.Red)
                        }
                        if (f) Box(Modifier.matchParentSize().border(1.5.dp, SBX.Red, RoundedCornerShape(12.dp)))
                    }
                }
            }
        }
    }
}
