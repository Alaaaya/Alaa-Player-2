package com.streamvault.app.ui.themes.sama

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
import com.streamvault.app.ui.themes.bespoke.progressAt
import com.streamvault.app.ui.themes.bespoke.qualityBadge
import com.streamvault.app.ui.themes.bespoke.tr
import com.streamvault.domain.model.Channel
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.Series
import com.streamvault.player.PlayerSurfaceResizeMode

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

@Composable
internal fun SamaLtrRow(modifier: Modifier = Modifier, spacing: androidx.compose.ui.unit.Dp = 0.dp, content: @Composable RowScope.(@Composable (@Composable () -> Unit) -> Unit) -> Unit) {
    val dir = LocalLayoutDirection.current
    val restore: @Composable (@Composable () -> Unit) -> Unit = { inner -> CompositionLocalProvider(LocalLayoutDirection provides dir) { inner() } }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(modifier, horizontalArrangement = Arrangement.spacedBy(spacing)) { content(restore) }
    }
}

private class SamaDest(val route: String, val glyph: String, val en: String, val ar: String)

private val cg2Destinations = listOf(
    SamaDest(Routes.HOME, "⌂", "Home", "الرئيسية"),
    SamaDest(Routes.LIVE_TV, "📺", "Live TV", "البث المباشر"),
    SamaDest(Routes.MOVIES, "film", "Movies", "الأفلام"),
    SamaDest(Routes.SERIES, "clap", "Series", "المسلسلات"),
    SamaDest(Routes.FAVORITES, "♡", "Favorites", "المفضلة"),
    SamaDest("${Routes.FAVORITES}?recent", "◷", "Recently watched", "المشاهدة الأخيرة"),
    SamaDest(Routes.SEARCH, "⌕", "Search", "البحث"),
    SamaDest(Routes.SETTINGS, "⚙", "Settings", "الإعدادات")
)

private fun onRoute(current: String, route: String) = current == route || current.startsWith("$route?") || current.startsWith("$route/")

@Composable
internal fun SamaShell(p: ShellParams) {
    CompositionLocalProvider(LocalSamaNavigate provides p.onNavigate) {
        Box(p.modifier.fillMaxSize()) {
            SamaAurora()
            Column(Modifier.fillMaxSize()) {
                if (p.topBarVisible) Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("S A M A", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Light)
                    Spacer(Modifier.width(8.dp))
                    listOf(
                        Routes.HOME to tr("Home", "الرئيسية"),
                        Routes.LIVE_TV to tr("Live TV", "مباشر"),
                        Routes.MOVIES to tr("Movies", "أفلام"),
                        Routes.SERIES to tr("Series", "مسلسلات"),
                        Routes.FAVORITES to tr("Saved", "محفوظ"),
                        Routes.SEARCH to tr("Search", "بحث"),
                        Routes.SETTINGS to tr("Settings", "إعدادات")
                    ).forEach { (route, label) ->
                        val selected = onRoute(p.currentRoute, route)
                        CgCard({ SamaRecent.active = false; p.onNavigate(route) }, shape = CG.Pill,
                            container = if (selected) Color(0x30FFFFFF) else Color.Transparent,
                            modifier = Modifier) {
                            Text(label, color = Color.White, fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp))
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    SamaClock()
                    p.topBarActions?.let { Row(content = it) }
                }
                Column(Modifier.weight(1f).fillMaxWidth().padding(p.contentPadding)) {
                    p.header?.let { Column(content = it) }
                    Column(Modifier.fillMaxSize(), content = p.content)
                }
            }
        }
    }
}

@Composable
private fun SamaNavItem(d: SamaDest, active: Boolean, expanded: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    TvClickableSurface(
        onClick = onClick, modifier = Modifier.fillMaxWidth(),
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = Color.Transparent, focusedContainerColor = CG.Amber, contentColor = Color.White, focusedContentColor = Color(0xFF14191D)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.06f),
        glow = ClickableSurfaceDefaults.glow(focusedGlow = androidx.tv.material3.Glow(CG.Amber.copy(alpha = 0.5f), 12.dp))
    ) {
        Row(
            Modifier.fillMaxWidth().cg2Selected(active).padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CgGlyph(d.glyph, 24.dp, tint = androidx.tv.material3.LocalContentColor.current)
            if (expanded) Text(tr(d.en, d.ar), color = androidx.tv.material3.LocalContentColor.current, fontSize = 16.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium, maxLines = 1)
        }
    }
}

internal object SamaGenreTarget { var requested by mutableStateOf<String?>(null) }

private class SamaFocusArt(val title: String, val image: String?, val meta: String, val plot: String?, val open: () -> Unit)

@Composable
internal fun SamaDashboard(p: DashboardParams) {
    val s = p.uiState
    LaunchedEffect(s.provider?.id, s.providerHealth.status) {
        SamaServer.name = s.provider?.name.orEmpty()
        SamaServer.healthy = when (s.providerHealth.status.name) { "ACTIVE" -> true; "ERROR", "EXPIRED", "DISABLED" -> false; else -> null }
    }
    val first = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { first.requestFocus() } }
    val movies = remember(s.recentMovies, s.recommendedMovies) { (s.recentMovies.ifEmpty { s.recommendedMovies }).take(20) }
    val series = remember(s.recentSeries) { s.recentSeries.take(20) }
    fun stars(r: Float) = r.takeIf { it > 0f }?.let { "★ %.1f".format(if (it > 10f) it / 10f else it) }
    fun movieArt(m: Movie) = SamaFocusArt(m.name, m.backdropUrl ?: m.posterUrl, listOfNotNull(m.year, m.genre?.substringBefore(","), stars(m.rating)).joinToString("  ·  "), m.plot) { p.onMovieClick(m) }
    fun seriesArt(m: Series) = SamaFocusArt(m.name, m.backdropUrl ?: m.posterUrl, listOfNotNull(m.releaseDate?.take(4), m.genre?.substringBefore(","), stars(m.rating)).joinToString("  ·  "), m.plot) { p.onSeriesClick(m) }
    val initial = remember(movies, series) { movies.firstOrNull { it.backdropUrl != null }?.let(::movieArt) ?: series.firstOrNull()?.let(::seriesArt) ?: movies.firstOrNull()?.let(::movieArt) }
    var art by remember { mutableStateOf<SamaFocusArt?>(null) }
    val shown = art ?: initial
    Box(Modifier.fillMaxSize().clip(RoundedCornerShape(0.dp))) {
        androidx.compose.animation.Crossfade(shown?.image ?: s.feature.artworkUrl, animationSpec = androidx.compose.animation.core.tween(700), label = "bd") { url ->
            if (url != null) AsyncImage(url, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) else Box(Modifier.fillMaxSize())
        }
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0x33080B0E), Color(0x99080B0E), Color(0xF2080B0E)))))
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.25f to Color.Transparent, 0.62f to Color(0xD9080B0E), 1f to Color(0xFA080B0E))))
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 36.dp), verticalArrangement = Arrangement.spacedBy(22.dp)) {
            Column(Modifier.fillMaxWidth(0.6f).padding(top = 40.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                androidx.compose.animation.AnimatedContent(shown?.title ?: s.feature.title.ifBlank { "Alaa Player" }, label = "t") { t ->
                    Text(t, color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                shown?.meta?.takeIf { it.isNotBlank() }?.let { Text(it, color = CG.Amber, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                Text(shown?.plot?.takeIf { it.isNotBlank() } ?: s.feature.summary, color = Color.White.copy(alpha = 0.85f), fontSize = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 22.sp)
                Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SamaButton(tr("Watch now", "مشاهدة الآن"), "▶", { shown?.open?.invoke() ?: p.onNavigate(Routes.MOVIES) }, Modifier.focusRequester(first))
                    SamaButton(tr("Details", "التفاصيل"), "info", { shown?.open?.invoke() ?: p.onNavigate(Routes.MOVIES) }, primary = false)
                    SamaButton(tr("Live TV", "البث المباشر"), "📺", { p.onNavigate(Routes.LIVE_TV) }, primary = false)
                }
            }
            SamaShelf(tr("Explore live TV", "اكتشف البث المباشر"), onAll = { p.onNavigate(Routes.LIVE_TV) }) {
                items(listOf(
                    "News" to "الأخبار", "Sports" to "الرياضة", "Movies" to "الأفلام",
                    "Kids" to "الأطفال", "Documentary" to "وثائقي", "Music" to "موسيقى"
                )) { (en, ar) ->
                    CgCard({ SamaGenreTarget.requested = en; p.onNavigate(Routes.LIVE_TV) }, modifier = Modifier.width(170.dp).height(100.dp),
                        shape = RoundedCornerShape(12.dp), container = Color(0x351E3440)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            SamaTag("LIVE", bg = CG.Live, fg = Color.White)
                            Text(tr(en, ar), color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
            if (s.continueWatching.isNotEmpty()) SamaShelf(tr("Continue watching", "متابعة المشاهدة"), s.continueWatching.size.toString()) {
                items(s.continueWatching.take(20), key = { "cw" + it.id }) { h ->
                    val cwLabel = tr("Continue watching", "متابعة المشاهدة")
                    val prog = if (h.totalDurationMs > 0) h.resumePositionMs.toFloat() / h.totalDurationMs else null
                    val sub = listOfNotNull(h.seasonNumber?.let { "S" + it }, h.episodeNumber?.let { "E" + it }, if (h.totalDurationMs > 0) cgDuration(h.totalDurationMs - h.resumePositionMs) + " " + tr("left", "متبقي") else null).joinToString(" · ")
                    SamaWideCard(h.title, h.posterUrl, sub, prog,
                        Modifier.width(300.dp).onFocusChanged { if (it.hasFocus) art = SamaFocusArt(h.title, h.posterUrl, cwLabel, null) { p.onContinueWatchingItemClick(h) } }) { p.onContinueWatchingItemClick(h) }
                }
            }
            if (movies.isNotEmpty()) SamaShelf(tr("Latest movies", "أحدث الأفلام"), s.stats.movieLibraryCount.toString(), { p.onNavigate(Routes.MOVIES) }) {
                items(movies, key = { "m" + it.id }) { m ->
                    SamaPosterCard(m.name, m.posterUrl, m.year, Modifier.width(150.dp).onFocusChanged { if (it.hasFocus) art = movieArt(m) }) { p.onMovieClick(m) }
                }
            }
            if (series.isNotEmpty()) SamaShelf(tr("Series", "المسلسلات"), s.stats.seriesLibraryCount.toString(), { p.onNavigate(Routes.SERIES) }) {
                items(series, key = { "s" + it.id }) { m ->
                    SamaPosterCard(m.name, m.posterUrl, m.genre?.substringBefore(","), Modifier.width(150.dp).onFocusChanged { if (it.hasFocus) art = seriesArt(m) }) { p.onSeriesClick(m) }
                }
            }
            val favs = s.favoriteChannels.ifEmpty { s.recentChannels }
            if (favs.isNotEmpty()) SamaShelf(if (s.favoriteChannels.isNotEmpty()) tr("Favorite channels", "القنوات المفضلة") else tr("Recent channels", "قنوات شوهدت مؤخراً"), favs.size.toString(), { p.onNavigate(Routes.LIVE_TV) }) {
                items(favs.take(20), key = { "c" + it.id }) { c ->
                    SamaChannelWide(c, Modifier.width(340.dp)) { if (s.favoriteChannels.isNotEmpty()) p.onFavoriteChannelClick(c, null) else p.onRecentChannelClick(c, null) }
                }
            }
            if (movies.isEmpty() && series.isEmpty() && s.continueWatching.isEmpty() && favs.isEmpty()) CgEmpty(tr("Add a playlist to get started", "أضف قائمة تشغيل للبدء"))
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
internal fun SamaShelf(title: String, count: String? = null, onAll: (() -> Unit)? = null, content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit) {
    Column {
        Row(Modifier.padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(4.dp, 20.dp).clip(CircleShape).background(Brush.verticalGradient(listOf(CG.Amber, CG.Violet))))
            Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            count?.let { Text(it, color = CG.Faint, fontSize = 14.sp) }
            Spacer(Modifier.weight(1f))
            onAll?.let { SamaSectionTitle("", Modifier.width(110.dp), onShowAll = it) }
        }
        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(vertical = 10.dp, horizontal = 4.dp), content = content)
    }
}

@Composable
internal fun SamaPosterCard(title: String, image: String?, sub: String?, modifier: Modifier = Modifier, onLongClick: (() -> Unit)? = null, onClick: () -> Unit) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        CgCard(onClick = onClick, onLongClick = onLongClick, shape = RoundedCornerShape(16.dp), container = CG.Glass, modifier = Modifier.fillMaxWidth().aspectRatio(2f / 3f)) {
            if (image != null) AsyncImage(image, title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            else Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF283A46), Color(0xFF11181E)))), contentAlignment = Alignment.Center) { Text(title.take(1), color = CG.Amber, fontSize = 40.sp, fontWeight = FontWeight.Black) }
        }
        Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(horizontal = 4.dp))
        sub?.takeIf { it.isNotBlank() }?.let { Text(it, color = CG.Faint, fontSize = 12.sp, maxLines = 1, modifier = Modifier.padding(horizontal = 4.dp)) }
    }
}

@Composable
internal fun SamaWideCard(title: String, image: String?, sub: String?, progress: Float?, modifier: Modifier = Modifier, onClick: () -> Unit) {
    CgCard(onClick = onClick, shape = RoundedCornerShape(18.dp), container = CG.Glass, modifier = modifier) {
        Column {
            Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
                image?.let { AsyncImage(it, title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.5f to Color.Transparent, 1f to Color(0xCC080B0E))))
                Box(Modifier.align(Alignment.Center).size(46.dp).clip(CircleShape).background(Color(0x66FFFFFF)).border(1.dp, Color(0x88FFFFFF), CircleShape), contentAlignment = Alignment.Center) { CgGlyph("▶", 22.dp, tint = Color.White) }
                progress?.let { Box(Modifier.align(Alignment.BottomCenter).padding(10.dp)) { SamaThinProgress(it) } }
            }
            Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                sub?.takeIf { it.isNotBlank() }?.let { Text(it, color = CG.Sub, fontSize = 12.sp, maxLines = 1) }
            }
        }
    }
}

@Composable
internal fun SamaThinProgress(f: Float, modifier: Modifier = Modifier, height: androidx.compose.ui.unit.Dp = 4.dp) {
    Box(modifier.fillMaxWidth().height(height).clip(CircleShape).background(Color(0x33FFFFFF))) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(f.coerceIn(0f, 1f)).clip(CircleShape).background(Brush.horizontalGradient(listOf(CG.Live, CG.Live))))
    }
}

internal fun samaProgress(pr: com.streamvault.domain.model.Program?, now: Long = System.currentTimeMillis()): Float? =
    pr?.takeIf { it.endTime > it.startTime && it.startTime <= now && it.endTime > now }?.let { (now - it.startTime).toFloat() / (it.endTime - it.startTime) }

@Composable
internal fun SamaChannelWide(c: Channel, modifier: Modifier = Modifier, onClick: () -> Unit) {
    CgCard(onClick = onClick, shape = RoundedCornerShape(18.dp), container = CG.Glass, modifier = modifier) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            SamaLogoBox(c.name, c.logoUrl, 72.dp, 54.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(c.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                    if (samaProgress(c.currentProgram) != null) SamaTag("LIVE", bg = CG.Live, fg = Color.White)
                }
                Text(c.currentProgram?.title ?: tr("No guide data", "لا يوجد دليل"), color = CG.Sub, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                samaProgress(c.currentProgram)?.let { SamaThinProgress(it, height = 3.dp) }
            }
        }
    }
}

@Composable
internal fun SamaLiveTv(p: LiveTvParams) {
    var categoriesOpen by remember { mutableStateOf(false) }
    var focused by remember { mutableStateOf<Channel?>(null) }
    val target = SamaGenreTarget.requested
    LaunchedEffect(target, p.categories) {
        if (target != null && p.categories.isNotEmpty()) {
            val terms = when (target) {
                "News" -> listOf("news", "nachrichten", "أخبار", "اخبار")
                "Sports" -> listOf("sport", "رياض")
                "Movies" -> listOf("movie", "cinema", "film", "أفلام", "افلام")
                "Kids" -> listOf("kids", "children", "kinder", "أطفال", "اطفال")
                "Documentary" -> listOf("document", "doku", "وثائق")
                else -> listOf("music", "musik", "موسيقى")
            }
            p.categories.firstOrNull { c -> terms.any { c.name.contains(it, ignoreCase = true) } }
                ?.let(p.onCategoryClick)
            categoriesOpen = true
            SamaGenreTarget.requested = null
        }
    }
    val pc = p.previewChannel ?: focused
    Box(Modifier.fillMaxSize()) {
        p.previewPlayerEngine?.let { engine ->
            PlayerRenderView(engine, PlayerSurfaceResizeMode.FIT, Modifier.fillMaxSize())
        }
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(
            listOf(Color(0xF0080B0E), Color(0xA6080B0E), Color.Transparent))))
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
            0.5f to Color.Transparent, 1f to Color(0xE6000000))))
        Row(Modifier.fillMaxSize().padding(24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.width(340.dp).fillMaxHeight().samaGlass().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(p.categories.firstOrNull { it.id == p.selectedCategoryId }?.name ?: p.sourceTitle,
                        color = Color.White, fontSize = 20.sp, maxLines = 1, modifier = Modifier.weight(1f))
                    CgRound("☰", { categoriesOpen = !categoriesOpen }, size = 38.dp)
                }
                SamaSearchBox(p.channelSearchQuery, p.onChannelSearchChange,
                    tr("Search channels", "ابحث بالقنوات"))
                androidx.compose.animation.AnimatedVisibility(categoriesOpen) {
                    LazyColumn(Modifier.heightIn(max = 200.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(p.categories, key = { it.id }) { cat ->
                            SamaChip(cat.id == p.selectedCategoryId,
                                { p.onCategoryClick(cat); categoriesOpen = false },
                                { p.onCategoryLongClick(cat) },
                                Modifier.focusRequester(p.categoryRequester(cat.id)).onFocusChanged {
                                    if (it.isFocused) p.onCategoryFocused(cat)
                                }) { Text(cat.name, fontSize = 14.sp, maxLines = 1) }
                        }
                    }
                }
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(p.channels, key = { it.id }) { c ->
                        SamaListRow(c.id == pc?.id,
                            { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c) },
                            modifier = Modifier.focusRequester(p.channelRequester(c.id)).onFocusChanged {
                                if (it.isFocused) { focused = c; p.onChannelFocused(c) }
                            }) {
                            CgLogo(c.name, if (p.isChannelLocked(c)) null else c.logoUrl, 38.dp)
                            Column(Modifier.weight(1f)) {
                                Text(c.name, color = Color.White, fontSize = 15.sp, maxLines = 1,
                                    overflow = TextOverflow.Ellipsis)
                                Text(c.currentProgram?.title ?: "", color = CG.Sub, fontSize = 12.sp,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            if (c.isFavorite) Text("♡", color = Color.White)
                        }
                    }
                }
            }
            Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.Bottom) {
                Spacer(Modifier.weight(1f))
                CgCard({ pc?.let(p.onChannelClick) }, container = Color.Transparent,
                    modifier = Modifier.fillMaxWidth().focusRequester(p.previewFocusRequester)) {
                    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SamaTag("● LIVE", bg = CG.Live, fg = Color.White)
                        Text(pc?.currentProgram?.title ?: pc?.name ?: tr("Choose a channel", "اختر قناة"),
                            color = Color.White, fontSize = 34.sp, maxLines = 2)
                        Text(pc?.name.orEmpty(), color = CG.Sub, fontSize = 18.sp)
                        pc?.currentProgram?.let { SamaThinProgress(it.progressAt()) }
                        pc?.nextProgram?.let { Text(tr("Next", "التالي") + " · " + it.title,
                            color = CG.Sub, fontSize = 14.sp) }
                        if (p.isPreviewLoading) Text(tr("Loading…", "جار التحميل…"), color = Color.White)
                        p.previewErrorMessage?.let { Text(it, color = CG.Sub, maxLines = 2) }
                    }
                }
            }
        }
    }
}

@Composable
internal fun SamaChip(selected: Boolean, onClick: () -> Unit, onLongClick: (() -> Unit)?, modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    val shape = RoundedCornerShape(50)
    TvClickableSurface(
        onClick = onClick, onLongClick = onLongClick, modifier = modifier.fillMaxWidth(),
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = if (selected) Color(0x4DFFFFFF) else Color(0x40253039), focusedContainerColor = CG.Amber, contentColor = Color.White, focusedContentColor = Color(0xFF14191D)),
        border = ClickableSurfaceDefaults.border(border = Border(androidx.compose.foundation.BorderStroke(1.dp, if (selected) Color(0x99FFFFFF) else Color(0x1FFFFFFF)), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.06f),
        glow = ClickableSurfaceDefaults.glow(focusedGlow = androidx.tv.material3.Glow(CG.Amber.copy(alpha = 0.55f), 14.dp))
    ) { Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), content = content) }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun SamaListRow(
    selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, onLongClick: (() -> Unit)? = null, vertical: androidx.compose.ui.unit.Dp = 9.dp,
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    TvClickableSurface(
        onClick = onClick, onLongClick = onLongClick, modifier = modifier.fillMaxWidth(),
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = Color.Transparent, focusedContainerColor = CG.GlassFocus, contentColor = Color.White, focusedContentColor = Color.White),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(androidx.compose.foundation.BorderStroke(1.5.dp, CG.Amber), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.03f),
        glow = ClickableSurfaceDefaults.glow(focusedGlow = androidx.tv.material3.Glow(CG.Amber.copy(alpha = 0.55f), 12.dp))
    ) {
        Row(Modifier.fillMaxWidth().cg2Selected(selected).padding(horizontal = 12.dp, vertical = vertical), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

@Composable
internal fun SamaLogoBox(name: String, logo: String?, w: androidx.compose.ui.unit.Dp, h: androidx.compose.ui.unit.Dp) {
    Box(Modifier.size(w, h).clip(RoundedCornerShape(10.dp)).background(Color(0x4D000000)), contentAlignment = Alignment.Center) {
        Text(name.take(3).uppercase(), color = Color.White, fontSize = (h.value / 2.8f).sp, fontWeight = FontWeight.Black, maxLines = 1)
        logo?.let { AsyncImage(it, name, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(4.dp)) }
    }
}
