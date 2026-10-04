package com.streamvault.app.ui.themes.chatgpt2

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import com.streamvault.app.ui.interaction.TvClickableSurface
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import com.streamvault.app.ui.screens.dashboard.DashboardFeatureAction
import com.streamvault.app.ui.themes.bespoke.DashboardParams
import com.streamvault.app.ui.themes.bespoke.LiveTvParams
import com.streamvault.app.ui.themes.bespoke.ShellParams
import com.streamvault.app.ui.themes.bespoke.qualityBadge
import com.streamvault.app.ui.themes.bespoke.tr
import com.streamvault.domain.model.Channel
import com.streamvault.player.PlayerSurfaceResizeMode

/** Media Center search: engraved underline field (no pill), gold rule turns bright on focus. */
@Composable
internal fun CgSearchField(value: String, onChange: (String) -> Unit, hint: String, modifier: Modifier = Modifier, onSubmit: (() -> Unit)? = null) {
    var focused by remember { mutableStateOf(false) }
    Column(modifier) {
        Row(Modifier.padding(horizontal = 4.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("⌕", color = if (focused) CG.Amber else CG.Faint, fontSize = 16.sp)
            Box(Modifier.weight(1f)) {
                if (value.isEmpty()) Text(hint.uppercase(), color = CG.Faint, fontSize = 12.sp, letterSpacing = 1.5.sp, maxLines = 1)
                BasicTextField(
                    value = value, onValueChange = onChange, singleLine = true,
                    textStyle = TextStyle(color = CG.Text, fontSize = 15.sp, fontFamily = CG.Serif), cursorBrush = SolidColor(CG.Amber),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { onSubmit?.invoke() }, onDone = { onSubmit?.invoke() }),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                    modifier = Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused }
                )
            }
        }
        Box(Modifier.fillMaxWidth().height(if (focused) 2.dp else 1.dp).background(if (focused) CG.Amber else CG.Line))
    }
}

/** Runs [content] with an LTR layout for column ORDER (references put the sidebar/categories on the visual left)
 *  while children restore the app's real direction via [Cg2Dir]. */
@Composable
internal fun Cg2LtrRow(modifier: Modifier = Modifier, spacing: androidx.compose.ui.unit.Dp = 0.dp, content: @Composable RowScope.(@Composable (@Composable () -> Unit) -> Unit) -> Unit) {
    val dir = LocalLayoutDirection.current
    val restore: @Composable (@Composable () -> Unit) -> Unit = { inner -> CompositionLocalProvider(LocalLayoutDirection provides dir) { inner() } }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(modifier, horizontalArrangement = Arrangement.spacedBy(spacing)) { content(restore) }
    }
}

private class Cg2Dest(val route: String, val glyph: String, val en: String, val ar: String)

private val cg2Destinations = listOf(
    Cg2Dest(Routes.HOME, "⌂", "Home", "الرئيسية"),
    Cg2Dest(Routes.LIVE_TV, "📺", "Live TV", "البث المباشر"),
    Cg2Dest(Routes.MOVIES, "film", "Movies", "الأفلام"),
    Cg2Dest(Routes.SERIES, "clap", "Series", "المسلسلات"),
    Cg2Dest(Routes.FAVORITES, "♡", "Favorites", "المفضلة"),
    Cg2Dest("${Routes.FAVORITES}?recent", "◷", "Recently watched", "المشاهدة الأخيرة"),
    Cg2Dest(Routes.EPG, "playlist", "Guide", "قائمة التشغيل"),
    Cg2Dest(Routes.SEARCH, "⌕", "Search", "البحث"),
    Cg2Dest(Routes.SETTINGS, "⚙", "Settings", "الإعدادات")
)

private fun onRoute(current: String, route: String) = current == route || current.startsWith("$route?") || current.startsWith("$route/")

/** Shell: rounded glass sidebar on the visual left (logo, line icons, red glowing pill for the active route,
 *  server card at the bottom). On the dense Live/Series screens it collapses to an icon rail until focused. */
@Composable
internal fun ChatGpt2Shell(p: ShellParams) {
    val dense = onRoute(p.currentRoute, Routes.LIVE_TV) || onRoute(p.currentRoute, Routes.SERIES) || onRoute(p.currentRoute, Routes.EPG)
    var railFocused by remember { mutableStateOf(false) }
    val expanded = !dense || railFocused
    CompositionLocalProvider(LocalCg2Navigate provides p.onNavigate) {
        Box(p.modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0xFF1E0C10), Color(0xFF0A0A0C), Color(0xFF050506)), center = androidx.compose.ui.geometry.Offset(1800f, 0f), radius = 2200f))) {
            Cg2LtrRow(Modifier.fillMaxSize()) { rtl ->
                if (p.topBarVisible) rtl {
                    Column(
                        Modifier.padding(start = 14.dp, top = 14.dp, bottom = 14.dp).width(if (expanded) 214.dp else 76.dp).fillMaxHeight().animateContentSize()
                            .onFocusChanged { railFocused = it.hasFocus }.cg2Panel().padding(horizontal = 10.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Cg2Logo(Modifier.padding(start = 8.dp, bottom = 16.dp), compact = !expanded)
                        cg2Destinations.forEach { d ->
                            val active = d.route == Routes.HOME && onRoute(p.currentRoute, Routes.HOME) || d.route != Routes.HOME && p.currentRoute == d.route ||
                                (d.route == Routes.LIVE_TV || d.route == Routes.MOVIES || d.route == Routes.SERIES || d.route == Routes.SETTINGS || d.route == Routes.SEARCH || d.route == Routes.EPG) && onRoute(p.currentRoute, d.route)
                            Cg2NavItem(d, active, expanded) { if (!active) p.onNavigate(d.route.substringBefore("?recent").let { r -> if (d.route.endsWith("?recent")) Routes.FAVORITES else r }) }
                        }
                        Spacer(Modifier.weight(1f))
                        if (expanded) Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0xFF111114)).border(1.dp, Color(0xFF2A2A31), RoundedCornerShape(10.dp)).padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CgGlyph("server", 18.dp, tint = CG.Sub)
                            Column(Modifier.weight(1f)) {
                                Text(tr("Current server", "السيرفر الحالي"), color = CG.Sub, fontSize = 11.sp)
                                Text("Server 1", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF22C55E)))
                        }
                    }
                }
                rtl {
                    Column(Modifier.weight(1f).fillMaxHeight().padding(p.contentPadding).padding(start = 22.dp, end = 22.dp, top = 14.dp)) {
                        Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Cg2Clock()
                            if (p.showScreenHeader) Text(p.title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.padding(start = 12.dp))
                            Spacer(Modifier.weight(1f))
                            p.topBarActions?.let { Row(horizontalArrangement = Arrangement.spacedBy(10.dp), content = it) }
                            Cg2IconKey("user", { p.onNavigate(Routes.SETTINGS) })
                            Cg2IconKey("bell", { p.onNavigate(Routes.SETTINGS) }, dot = true)
                            Cg2IconKey("⌕", { p.onNavigate(Routes.SEARCH) })
                        }
                        p.header?.let { Column(content = it) }
                        Column(Modifier.fillMaxSize(), content = p.content)
                    }
                }
            }
        }
    }
}

@Composable
private fun Cg2NavItem(d: Cg2Dest, active: Boolean, expanded: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    TvClickableSurface(
        onClick = onClick, modifier = Modifier.fillMaxWidth(),
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = Color.Transparent, focusedContainerColor = Color(0xFF2A0A0E), contentColor = Color.White, focusedContentColor = Color.White),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(androidx.compose.foundation.BorderStroke(1.5.dp, CG.Amber), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f),
        glow = ClickableSurfaceDefaults.glow(focusedGlow = androidx.tv.material3.Glow(CG.Amber.copy(alpha = 0.5f), 12.dp))
    ) {
        Row(
            Modifier.fillMaxWidth().cg2Selected(active).padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CgGlyph(d.glyph, 24.dp, tint = Color.White)
            if (expanded) Text(tr(d.en, d.ar), color = Color.White, fontSize = 16.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium, maxLines = 1)
        }
    }
}

/** Home: big cinematic hero carousel, six category tiles with art + counts, then split "latest movies" / "latest series". */
@Composable
internal fun ChatGpt2Dashboard(p: DashboardParams) {
    val s = p.uiState
    val first = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { first.requestFocus() } }
    val heroes = remember(s.recommendedMovies, s.recentMovies, s.recentSeries) {
        val m = (s.recommendedMovies + s.recentMovies).distinctBy { it.id }.filter { it.backdropUrl != null || it.posterUrl != null }.take(4)
            .map { Cg2Hero(it.name, it.backdropUrl ?: it.posterUrl, it.year, it.rating, it.genre, it.plot, { p.onMovieClick(it) }) }
        val sr = s.recentSeries.filter { it.backdropUrl != null || it.posterUrl != null }.take(2)
            .map { Cg2Hero(it.name, it.backdropUrl ?: it.posterUrl, it.releaseDate?.take(4), it.rating, it.genre, it.plot, { p.onSeriesClick(it) }) }
        (sr.take(1) + m + sr.drop(1)).take(6)
    }
    var heroIdx by remember { mutableStateOf(0) }
    LaunchedEffect(heroes.size) { while (heroes.size > 1) { kotlinx.coroutines.delay(8000); heroIdx = (heroIdx + 1) % heroes.size } }
    val hero = heroes.getOrNull(heroIdx.coerceAtMost((heroes.size - 1).coerceAtLeast(0)))
    fun catCount(vararg keys: String) = s.liveCategories.filter { c -> keys.any { it in c.name.lowercase() } }
    val kids = catCount("kid", "أطفال", "اطفال", "كرتون", "cartoon"); val news = catCount("news", "أخبار", "اخبار"); val sports = catCount("sport", "رياض", "bein")
    fun liveRoute(cats: List<com.streamvault.domain.model.Category>) = cats.firstOrNull()?.let { "live_tv?categoryId=${it.id}" } ?: Routes.LIVE_TV
    val ch = tr("channels", "قناة")
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // HERO
        Box(Modifier.fillMaxWidth().height(330.dp).clip(RoundedCornerShape(18.dp)).background(CG.Raised)) {
            androidx.compose.animation.Crossfade(hero?.image ?: s.feature.artworkUrl, label = "hero") { url ->
                url?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
            }
            Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xF00A0A0C), Color(0x990A0A0C), Color.Transparent))))
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.55f to Color.Transparent, 1f to Color(0xE60A0A0C))))
            Column(Modifier.align(Alignment.CenterStart).fillMaxWidth(0.55f).padding(start = 70.dp, end = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(hero?.title ?: s.feature.title.ifBlank { "Alaa IPTV" }, color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    hero?.genre?.split(",", "/", "|")?.map { it.trim() }?.filter { it.isNotEmpty() }?.take(3)?.joinToString("  ·  ")?.let { Text(it, color = CG.Sub, fontSize = 14.sp, maxLines = 1) }
                    hero?.rating?.let { Cg2Rating(it, 14) }
                    Cg2Tag("HD")
                    hero?.year?.let { Text(it, color = Color.White, fontSize = 14.sp) }
                }
                Text(hero?.plot?.takeIf { it.isNotBlank() } ?: s.feature.summary, color = Color.White.copy(alpha = 0.88f), fontSize = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 22.sp)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Cg2Button(tr("Watch now", "مشاهدة الآن"), "▶", { hero?.open?.invoke() ?: p.onNavigate(Routes.MOVIES) }, Modifier.focusRequester(first))
                    Cg2Button(tr("More info", "مزيد من المعلومات"), "info", { hero?.open?.invoke() ?: p.onNavigate(Routes.MOVIES) }, primary = false)
                }
            }
            if (heroes.size > 1) {
                Box(Modifier.align(Alignment.CenterStart).padding(start = 14.dp)) { Cg2IconKey("prev", { heroIdx = (heroIdx - 1 + heroes.size) % heroes.size }) }
                Box(Modifier.align(Alignment.CenterEnd).padding(end = 14.dp)) { Cg2IconKey("next", { heroIdx = (heroIdx + 1) % heroes.size }) }
                Row(Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    heroes.indices.forEach { i -> Box(Modifier.size(if (i == heroIdx) 22.dp else 14.dp, 5.dp).clip(CircleShape).background(if (i == heroIdx) CG.Amber else Color(0x66FFFFFF))) }
                }
            }
        }
        // SIX CATEGORY TILES
        val tiles = listOf(
            Cg2TileData("📺", tr("Live TV", "البث المباشر"), "${s.stats.liveChannelCount} $ch", Routes.LIVE_TV, s.recentChannels.firstOrNull()?.logoUrl, Color(0xFF7A0A12), Color(0xFF1A0A0C)),
            Cg2TileData("film", tr("Movies", "الأفلام"), "${s.stats.movieLibraryCount} ${tr("movies", "فيلم")}", Routes.MOVIES, (s.recentMovies.firstOrNull()?.let { it.backdropUrl ?: it.posterUrl }), Color(0xFF123A8A), Color(0xFF0A1430)),
            Cg2TileData("clap", tr("Series", "المسلسلات"), "${s.stats.seriesLibraryCount} ${tr("series", "مسلسل")}", Routes.SERIES, (s.recentSeries.firstOrNull()?.let { it.backdropUrl ?: it.posterUrl }), Color(0xFF5B1A9A), Color(0xFF1C0A30)),
            Cg2TileData("kids", tr("Kids", "الأطفال"), "${kids.sumOf { it.count }} $ch", liveRoute(kids), null, Color(0xFFB7791F), Color(0xFF3A2208)),
            Cg2TileData("news", tr("News", "الأخبار"), "${news.sumOf { it.count }} $ch", liveRoute(news), null, Color(0xFF1D4ED8), Color(0xFF0A1636)),
            Cg2TileData("sport", tr("Sports", "الرياضة"), "${sports.sumOf { it.count }} $ch", liveRoute(sports), null, Color(0xFF15803D), Color(0xFF082A14))
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            tiles.forEach { t -> Cg2CategoryTile(t, Modifier.weight(1f)) { p.onNavigate(t.route) } }
        }
        // LATEST MOVIES | LATEST SERIES
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(22.dp)) {
            Column(Modifier.weight(1.15f)) {
                Cg2SectionTitle(tr("Latest movies", "أحدث الأفلام"))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val movies = (s.recentMovies.ifEmpty { s.recommendedMovies }).take(6)
                    if (movies.isEmpty()) Text(tr("No movies yet", "لا توجد أفلام بعد"), color = CG.Faint, fontSize = 13.sp)
                    movies.forEachIndexed { i, m -> Cg2SmallPoster(m.name, m.posterUrl, m.year ?: "", if (i == 0) "HD" else null, Modifier.weight(1f)) { p.onMovieClick(m) } }
                    repeat((6 - movies.size).coerceAtLeast(0).let { if (movies.isEmpty()) 0 else it }) { Spacer(Modifier.weight(1f)) }
                }
            }
            Column(Modifier.weight(1f)) {
                Cg2SectionTitle(tr("Latest series", "أحدث المسلسلات"))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val series = s.recentSeries.take(5)
                    if (series.isEmpty()) Text(tr("No series yet", "لا توجد مسلسلات بعد"), color = CG.Faint, fontSize = 13.sp)
                    series.forEachIndexed { i, m -> Cg2SmallPoster(m.name, m.posterUrl, m.genre?.substringBefore(",") ?: m.releaseDate?.take(4) ?: "", if (i == 0) tr("NEW", "جديد") else null, Modifier.weight(1f)) { p.onSeriesClick(m) } }
                    repeat((5 - series.size).coerceAtLeast(0).let { if (series.isEmpty()) 0 else it }) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}

private class Cg2Hero(val title: String, val image: String?, val year: String?, val rating: Float, val genre: String?, val plot: String?, val open: () -> Unit)
private class Cg2TileData(val glyph: String, val title: String, val sub: String, val route: String, val art: String?, val a: Color, val b: Color)

@Composable
private fun Cg2CategoryTile(t: Cg2TileData, modifier: Modifier, onClick: () -> Unit) {
    CgCard(onClick = onClick, container = Color.Transparent, zoom = 1.05f, modifier = modifier.height(138.dp)) {
        Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(t.a, t.b))))
        t.art?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, alpha = 0.55f, modifier = Modifier.fillMaxSize()) }
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(t.b.copy(alpha = 0.92f), Color.Transparent))))
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.10f), Color.Transparent, Color.Black.copy(alpha = 0.35f)))))
        Column(Modifier.align(Alignment.CenterStart).padding(horizontal = 16.dp)) {
            CgGlyph(t.glyph, 40.dp, tint = Color.White)
            Spacer(Modifier.height(10.dp))
            Text(t.title, color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(t.sub, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp, maxLines = 1)
        }
    }
}

@Composable
internal fun Cg2SmallPoster(title: String, image: String?, sub: String, tag: String?, modifier: Modifier, onClick: () -> Unit) {
    CgCard(onClick = onClick, container = Color(0xFF15151A), zoom = 1.06f, shape = RoundedCornerShape(8.dp), modifier = modifier) {
        Column {
            Box(Modifier.fillMaxWidth().aspectRatio(0.78f).background(CG.Raised)) {
                if (image != null) AsyncImage(image, title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                else Text(title.take(1), color = CG.Amber, fontSize = 30.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.Center))
                Box(Modifier.fillMaxWidth().height(40.dp).align(Alignment.BottomCenter).background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xFF15151A)))))
                tag?.let { Box(Modifier.align(Alignment.TopStart).padding(6.dp)) { Cg2Tag(it) } }
            }
            Column(Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(sub, color = CG.Sub, fontSize = 11.sp, maxLines = 1)
            }
        }
    }
}

/** Live TV: three columns like the reference. Categories (icon/name/count) | groups of the selected category
 *  (derived from channel-name brands, logo + count) | preview player with LIVE info on top and the channel
 *  table below (#, logo, name, now program, LIVE, time range, quality, heart). */
@Composable
internal fun ChatGpt2LiveTv(p: LiveTvParams) {
    val groups = remember(p.channels) {
        p.channels.groupBy { c -> c.name.trim().split(" ", "-", "|", ":").firstOrNull { it.length >= 2 }?.uppercase() ?: "#" }
            .filter { it.value.size >= 2 }.entries.sortedByDescending { it.value.size }.take(14)
    }
    var group by remember(p.selectedCategoryId) { mutableStateOf<String?>(null) }
    val rows = remember(p.channels, group) { group?.let { g -> groups.firstOrNull { it.key == g }?.value } ?: p.channels }
    val catName = p.categories.firstOrNull { it.id == p.selectedCategoryId }?.name ?: p.sourceTitle
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Cg2SearchBox(p.channelSearchQuery, p.onChannelSearchChange, tr("Search channels…", "بحث عن القنوات …"), Modifier.width(320.dp))
        Cg2LtrRow(Modifier.fillMaxSize(), spacing = 12.dp) { rtl ->
            // COLUMN 1: categories
            rtl {
                Column(Modifier.width(236.dp).fillMaxHeight().cg2Panel().padding(8.dp)) {
                    Row(Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CgGlyph("📺", 26.dp, tint = Color.White)
                        Text(tr("Categories", "الفئات"), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
                        items(p.categories, key = { it.id }) { cat ->
                            val sel = cat.id == p.selectedCategoryId
                            Cg2ListRow(
                                selected = sel, onClick = { p.onCategoryClick(cat) }, onLongClick = { p.onCategoryLongClick(cat) },
                                modifier = Modifier.focusRequester(p.categoryRequester(cat.id)).onFocusChanged { if (it.isFocused) p.onCategoryFocused(cat) }.cgRight { p.onRequestChannelsFromCategory() }
                            ) {
                                CgCategoryGlyph(cat.name, p.isCategoryLocked(cat), Color.White, 22.dp)
                                Text(cat.name, color = Color.White, fontSize = 15.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                Text(if (cat.count > 0) "${cat.count}" else "", color = if (sel) Color.White else CG.Sub, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
            // COLUMN 2: groups inside the selected category
            rtl {
                Column(Modifier.width(300.dp).fillMaxHeight().cg2Panel().padding(8.dp)) {
                    Row(Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CgCategoryGlyph(catName, false, Color.White, 26.dp)
                        Text(catName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        Text("${p.channels.size}", color = CG.Sub, fontSize = 14.sp)
                    }
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
                        item {
                            Cg2ListRow(selected = group == null, onClick = { group = null }) {
                                CgGlyph("📺", 22.dp, tint = Color.White)
                                Text(tr("All channels", "جميع القنوات") + " · " + catName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                Text("${p.channels.size}", color = Color.White, fontSize = 13.sp)
                            }
                        }
                        items(groups, key = { "g" + it.key }) { (g, list) ->
                            Cg2ListRow(selected = group == g, onClick = { group = if (group == g) null else g }) {
                                Cg2LogoBox(list.first().name, list.firstNotNullOfOrNull { it.logoUrl }, 44.dp, 30.dp)
                                Text(g.lowercase().replaceFirstChar { it.uppercase() }, color = Color.White, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                Text("${list.size}", color = CG.Sub, fontSize = 13.sp)
                            }
                        }
                        if (groups.isEmpty()) item { Text(tr("No sub-groups in this category", "لا توجد مجموعات فرعية"), color = CG.Faint, fontSize = 13.sp, modifier = Modifier.padding(12.dp)) }
                    }
                }
            }
            // COLUMN 3: preview + table
            rtl {
                Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val pc = p.previewChannel
                    Row(Modifier.fillMaxWidth().height(186.dp).cg2Panel().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Cg2LogoBox(pc?.name ?: "TV", pc?.logoUrl, 56.dp, 56.dp)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(pc?.name ?: tr("Pick a channel", "اختر قناة"), color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    if (pc != null) Cg2Tag("LIVE")
                                }
                            }
                            val prog = pc?.currentProgram
                            Text(catName, color = Color.White.copy(alpha = 0.85f), fontSize = 15.sp, maxLines = 1)
                            Text(prog?.title ?: tr("No guide data", "لا يوجد دليل برامج"), color = Color.White, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.weight(1f))
                            if (prog != null && prog.endTime > prog.startTime) {
                                val now = System.currentTimeMillis()
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(cgClock(prog.startTime), color = Color.White, fontSize = 13.sp)
                                    Box(Modifier.weight(1f).height(5.dp).clip(CircleShape).background(Color(0xFF3A3A42))) {
                                        Box(Modifier.fillMaxHeight().fillMaxWidth(((now - prog.startTime).toFloat() / (prog.endTime - prog.startTime)).coerceIn(0f, 1f)).clip(CircleShape).background(CG.Amber))
                                    }
                                    Text(cgClock(prog.endTime), color = Color.White, fontSize = 13.sp)
                                }
                            }
                        }
                        CgCard(
                            onClick = { pc?.let(p.onChannelClick) }, container = Color.Black, shape = RoundedCornerShape(10.dp), zoom = 1.0f,
                            modifier = Modifier.fillMaxHeight().aspectRatio(16f / 9f).focusRequester(p.previewFocusRequester).cgRight { p.onRequestChannelsFromPreview() }
                        ) {
                            val engine = p.previewPlayerEngine
                            if (engine != null && pc != null) PlayerRenderView(engine, PlayerSurfaceResizeMode.FIT, Modifier.fillMaxSize())
                            else pc?.logoUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(30.dp)) }
                            if (p.isPreviewLoading) Text("…", color = Color.White, fontSize = 22.sp, modifier = Modifier.align(Alignment.Center))
                            p.previewErrorMessage?.let { Text(it, color = CG.Sub, fontSize = 12.sp, maxLines = 2, modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp)) }
                        }
                    }
                    Column(Modifier.fillMaxSize().cg2Panel().padding(horizontal = 8.dp, vertical = 6.dp)) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("#", color = CG.Sub, fontSize = 13.sp, modifier = Modifier.width(40.dp))
                            Text(tr("Channel", "القناة"), color = CG.Sub, fontSize = 13.sp, modifier = Modifier.width(250.dp))
                            Text(tr("Now playing", "البرنامج الحالي"), color = CG.Sub, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Text(tr("Time", "الوقت"), color = CG.Sub, fontSize = 13.sp, modifier = Modifier.width(190.dp))
                        }
                        Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF26262C)))
                        LazyColumn(contentPadding = PaddingValues(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            items(rows.size, key = { rows[it].id }) { i ->
                                val c = rows[i]
                                val locked = p.isChannelLocked(c)
                                val moving = c.id == p.movingChannelId
                                Cg2ListRow(
                                    selected = moving || c.id == pc?.id, onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c) }, vertical = 6.dp,
                                    modifier = Modifier.focusRequester(p.channelRequester(c.id)).onFocusChanged { if (it.isFocused) p.onChannelFocused(c) }.cgRight { p.onRequestPreviewFromChannel() }
                                ) {
                                    Text(if (c.number > 0) "${c.number}" else "${i + 1}", color = Color.White, fontSize = 15.sp, modifier = Modifier.width(28.dp))
                                    Cg2LogoBox(c.name, if (locked) null else c.logoUrl, 46.dp, 34.dp)
                                    Text((if (moving) "⇅ " else "") + c.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(190.dp))
                                    val prog = c.currentProgram
                                    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(prog?.title ?: "—", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                                        if (prog != null && i == 0 || c.id == pc?.id && prog != null) Cg2Tag("LIVE")
                                    }
                                    Text(prog?.takeIf { it.endTime > 0 }?.let { "${cgClock(it.startTime)} - ${cgClock(it.endTime)}" } ?: "", color = Color.White, fontSize = 13.sp, maxLines = 1, modifier = Modifier.width(110.dp))
                                    (if (locked) null else c.qualityBadge())?.let { Cg2Tag(it, fg = Color.White, outlined = true) } ?: Spacer(Modifier.width(30.dp))
                                    CgGlyph(if (c.isFavorite) "♥" else "♡", 22.dp, tint = if (c.isFavorite) CG.Amber else Color.White, modifier = Modifier.padding(start = 10.dp))
                                }
                            }
                            if (rows.isEmpty()) item { CgEmpty(tr("No channels", "لا توجد قنوات")) }
                        }
                    }
                }
            }
        }
    }
}

/** List row used by every column: transparent, red-glow selected pill, red border on focus. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun Cg2ListRow(
    selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, onLongClick: (() -> Unit)? = null, vertical: androidx.compose.ui.unit.Dp = 9.dp,
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(10.dp)
    TvClickableSurface(
        onClick = onClick, onLongClick = onLongClick, modifier = modifier.fillMaxWidth(),
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = Color.Transparent, focusedContainerColor = Color(0xFF3A0A0E), contentColor = Color.White, focusedContentColor = Color.White),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFF2A35)), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.01f),
        glow = ClickableSurfaceDefaults.glow(focusedGlow = androidx.tv.material3.Glow(CG.Amber.copy(alpha = 0.55f), 12.dp))
    ) {
        Row(Modifier.fillMaxWidth().cg2Selected(selected).padding(horizontal = 12.dp, vertical = vertical), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

@Composable
internal fun Cg2LogoBox(name: String, logo: String?, w: androidx.compose.ui.unit.Dp, h: androidx.compose.ui.unit.Dp) {
    Box(Modifier.size(w, h).clip(RoundedCornerShape(6.dp)).background(Color(0xFF1C1C22)), contentAlignment = Alignment.Center) {
        Text(name.take(3).uppercase(), color = Color.White, fontSize = (h.value / 2.8f).sp, fontWeight = FontWeight.Black, maxLines = 1)
        logo?.let { AsyncImage(it, name, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().background(Color(0xFF1C1C22)).padding(3.dp)) }
    }
}
