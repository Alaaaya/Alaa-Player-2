package com.streamvault.app.ui.themes.kat

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
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.Series
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
 *  while children restore the app's real direction via [KatDir]. */
@Composable
internal fun KatLtrRow(modifier: Modifier = Modifier, spacing: androidx.compose.ui.unit.Dp = 0.dp, content: @Composable RowScope.(@Composable (@Composable () -> Unit) -> Unit) -> Unit) {
    val dir = LocalLayoutDirection.current
    val restore: @Composable (@Composable () -> Unit) -> Unit = { inner -> CompositionLocalProvider(LocalLayoutDirection provides dir) { inner() } }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(modifier, horizontalArrangement = Arrangement.spacedBy(spacing)) { content(restore) }
    }
}

private class KatDest(val route: String, val glyph: String, val en: String, val ar: String)

private val cg2Destinations = listOf(
    KatDest(Routes.HOME, "⌂", "Home", "الرئيسية"),
    KatDest(Routes.LIVE_TV, "📺", "Live TV", "البث المباشر"),
    KatDest(Routes.MOVIES, "film", "Movies", "الأفلام"),
    KatDest(Routes.SERIES, "clap", "Series", "المسلسلات"),
    KatDest(Routes.FAVORITES, "♡", "Favorites", "المفضلة"),
    KatDest("${Routes.FAVORITES}?recent", "◷", "Recently watched", "المشاهدة الأخيرة"),
    KatDest(Routes.EPG, "playlist", "Guide", "قائمة التشغيل"),
    KatDest(Routes.SEARCH, "⌕", "Search", "البحث"),
    KatDest(Routes.SETTINGS, "⚙", "Settings", "الإعدادات")
)

private fun onRoute(current: String, route: String) = current == route || current.startsWith("$route?") || current.startsWith("$route/")

/** Shell: rounded glass sidebar on the visual left (logo, line icons, red glowing pill for the active route,
 *  server card at the bottom). On the dense Live/Series screens it collapses to an icon rail until focused. */
@Composable
internal fun KatShell(p: ShellParams) {
    var railFocused by remember { mutableStateOf(false) }
    val expanded = railFocused
    CompositionLocalProvider(LocalKatNavigate provides p.onNavigate) {
        Box(p.modifier.fillMaxSize()) {
            KatAurora()
            KatLtrRow(Modifier.fillMaxSize()) { rtl ->
                if (p.topBarVisible) rtl {
                    Column(
                        Modifier.padding(start = 16.dp, top = 40.dp, bottom = 40.dp).width(if (expanded) 220.dp else 72.dp).fillMaxHeight()
                            .animateContentSize(androidx.compose.animation.core.spring(dampingRatio = 0.8f, stiffness = 380f))
                            .onFocusChanged { railFocused = it.hasFocus }.cg2Panel().padding(horizontal = 10.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
                    ) {
                        KatLogo(Modifier.padding(start = 9.dp, bottom = 14.dp), compact = !expanded)
                        cg2Destinations.forEach { d ->
                            val active = d.route == Routes.HOME && onRoute(p.currentRoute, Routes.HOME) || d.route != Routes.HOME && p.currentRoute == d.route ||
                                (d.route == Routes.LIVE_TV || d.route == Routes.MOVIES || d.route == Routes.SERIES || d.route == Routes.SETTINGS || d.route == Routes.SEARCH || d.route == Routes.EPG) && onRoute(p.currentRoute, d.route)
                            val isRecent = d.route.endsWith("?recent")
                            val active2 = if (d.route == Routes.FAVORITES || isRecent) onRoute(p.currentRoute, Routes.FAVORITES) && KatRecent.active == isRecent else active
                            KatNavItem(d, active2, expanded) {
                                KatRecent.active = isRecent
                                if (!onRoute(p.currentRoute, Routes.FAVORITES) || !(d.route == Routes.FAVORITES || isRecent)) p.onNavigate(if (isRecent) Routes.FAVORITES else d.route)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        if (expanded) Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0x4D000000)).border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(16.dp)).padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CgGlyph("server", 18.dp, tint = CG.Sub)
                            Column(Modifier.weight(1f)) {
                                Text(tr("Current server", "السيرفر الحالي"), color = CG.Sub, fontSize = 11.sp)
                                Text(KatServer.name.ifBlank { tr("Not connected", "غير متصل") }, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            KatServer.healthy?.let { ok -> Box(Modifier.size(10.dp).clip(CircleShape).background(if (ok) Color(0xFF22C55E) else Color(0xFFEF4444))) }
                        }
                    }
                }
                rtl {
                    Column(Modifier.weight(1f).fillMaxHeight().padding(p.contentPadding).padding(start = 22.dp, end = 22.dp, top = 14.dp)) {
                        Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            KatClock()
                            if (p.showScreenHeader) Text(p.title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.padding(start = 12.dp))
                            Spacer(Modifier.weight(1f))
                            p.topBarActions?.let { Row(horizontalArrangement = Arrangement.spacedBy(10.dp), content = it) }
                            KatIconKey("user", { p.onNavigate(Routes.SETTINGS) })
                            KatIconKey("bell", { p.onNavigate(Routes.SETTINGS) }, dot = true)
                            KatIconKey("⌕", { p.onNavigate(Routes.SEARCH) })
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
private fun KatNavItem(d: KatDest, active: Boolean, expanded: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    TvClickableSurface(
        onClick = onClick, modifier = Modifier.fillMaxWidth(),
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = Color.Transparent, focusedContainerColor = CG.Amber, contentColor = Color.White, focusedContentColor = Color(0xFF14102A)),
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

/** Kat home: full-bleed backdrop that crossfades to whatever is focused, big title/meta/buttons over it,
 *  then glass shelves: continue watching (progress), latest movies, series, favorite channels (wide, live now). */
private class KatFocusArt(val title: String, val image: String?, val meta: String, val plot: String?, val open: () -> Unit)

@Composable
internal fun KatDashboard(p: DashboardParams) {
    val s = p.uiState
    LaunchedEffect(s.provider?.id, s.providerHealth.status) {
        KatServer.name = s.provider?.name.orEmpty()
        KatServer.healthy = when (s.providerHealth.status.name) { "ACTIVE" -> true; "ERROR", "EXPIRED", "DISABLED" -> false; else -> null }
    }
    val first = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { first.requestFocus() } }
    val movies = remember(s.recentMovies, s.recommendedMovies) { (s.recentMovies.ifEmpty { s.recommendedMovies }).take(20) }
    val series = remember(s.recentSeries) { s.recentSeries.take(20) }
    fun stars(r: Float) = r.takeIf { it > 0f }?.let { "★ %.1f".format(if (it > 10f) it / 10f else it) }
    fun movieArt(m: Movie) = KatFocusArt(m.name, m.backdropUrl ?: m.posterUrl, listOfNotNull(m.year, m.genre?.substringBefore(","), stars(m.rating)).joinToString("  ·  "), m.plot) { p.onMovieClick(m) }
    fun seriesArt(m: Series) = KatFocusArt(m.name, m.backdropUrl ?: m.posterUrl, listOfNotNull(m.releaseDate?.take(4), m.genre?.substringBefore(","), stars(m.rating)).joinToString("  ·  "), m.plot) { p.onSeriesClick(m) }
    val initial = remember(movies, series) { movies.firstOrNull { it.backdropUrl != null }?.let(::movieArt) ?: series.firstOrNull()?.let(::seriesArt) ?: movies.firstOrNull()?.let(::movieArt) }
    var art by remember { mutableStateOf<KatFocusArt?>(null) }
    val shown = art ?: initial
    Box(Modifier.fillMaxSize().clip(RoundedCornerShape(26.dp))) {
        androidx.compose.animation.Crossfade(shown?.image ?: s.feature.artworkUrl, animationSpec = androidx.compose.animation.core.tween(700), label = "bd") { url ->
            if (url != null) AsyncImage(url, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) else Box(Modifier.fillMaxSize())
        }
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0x3307071A), Color(0x9907071A), Color(0xF207071A)))))
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.25f to Color.Transparent, 0.62f to Color(0xD907071A), 1f to Color(0xFA07071A))))
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 36.dp), verticalArrangement = Arrangement.spacedBy(22.dp)) {
            Column(Modifier.fillMaxWidth(0.6f).padding(top = 40.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                androidx.compose.animation.AnimatedContent(shown?.title ?: s.feature.title.ifBlank { "Alaa Player" }, label = "t") { t ->
                    Text(t, color = Color.White, fontSize = 46.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                shown?.meta?.takeIf { it.isNotBlank() }?.let { Text(it, color = CG.Amber, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                Text(shown?.plot?.takeIf { it.isNotBlank() } ?: s.feature.summary, color = Color.White.copy(alpha = 0.85f), fontSize = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 22.sp)
                Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    KatButton(tr("Watch now", "مشاهدة الآن"), "▶", { shown?.open?.invoke() ?: p.onNavigate(Routes.MOVIES) }, Modifier.focusRequester(first))
                    KatButton(tr("Details", "التفاصيل"), "info", { shown?.open?.invoke() ?: p.onNavigate(Routes.MOVIES) }, primary = false)
                    KatButton(tr("Live TV", "البث المباشر"), "📺", { p.onNavigate(Routes.LIVE_TV) }, primary = false)
                }
            }
            if (s.continueWatching.isNotEmpty()) KatShelf(tr("Continue watching", "متابعة المشاهدة"), s.continueWatching.size.toString()) {
                items(s.continueWatching.take(20), key = { "cw" + it.id }) { h ->
                    val cwLabel = tr("Continue watching", "متابعة المشاهدة")
                    val prog = if (h.totalDurationMs > 0) h.resumePositionMs.toFloat() / h.totalDurationMs else null
                    val sub = listOfNotNull(h.seasonNumber?.let { "S" + it }, h.episodeNumber?.let { "E" + it }, if (h.totalDurationMs > 0) cgDuration(h.totalDurationMs - h.resumePositionMs) + " " + tr("left", "متبقي") else null).joinToString(" · ")
                    KatWideCard(h.title, h.posterUrl, sub, prog,
                        Modifier.width(300.dp).onFocusChanged { if (it.hasFocus) art = KatFocusArt(h.title, h.posterUrl, cwLabel, null) { p.onContinueWatchingItemClick(h) } }) { p.onContinueWatchingItemClick(h) }
                }
            }
            if (movies.isNotEmpty()) KatShelf(tr("Latest movies", "أحدث الأفلام"), s.stats.movieLibraryCount.toString(), { p.onNavigate(Routes.MOVIES) }) {
                items(movies, key = { "m" + it.id }) { m ->
                    KatPosterCard(m.name, m.posterUrl, m.year, Modifier.width(150.dp).onFocusChanged { if (it.hasFocus) art = movieArt(m) }) { p.onMovieClick(m) }
                }
            }
            if (series.isNotEmpty()) KatShelf(tr("Series", "المسلسلات"), s.stats.seriesLibraryCount.toString(), { p.onNavigate(Routes.SERIES) }) {
                items(series, key = { "s" + it.id }) { m ->
                    KatPosterCard(m.name, m.posterUrl, m.genre?.substringBefore(","), Modifier.width(150.dp).onFocusChanged { if (it.hasFocus) art = seriesArt(m) }) { p.onSeriesClick(m) }
                }
            }
            val favs = s.favoriteChannels.ifEmpty { s.recentChannels }
            if (favs.isNotEmpty()) KatShelf(if (s.favoriteChannels.isNotEmpty()) tr("Favorite channels", "القنوات المفضلة") else tr("Recent channels", "قنوات شوهدت مؤخراً"), favs.size.toString(), { p.onNavigate(Routes.LIVE_TV) }) {
                items(favs.take(20), key = { "c" + it.id }) { c ->
                    KatChannelWide(c, Modifier.width(340.dp)) { if (s.favoriteChannels.isNotEmpty()) p.onFavoriteChannelClick(c, null) else p.onRecentChannelClick(c, null) }
                }
            }
            if (movies.isEmpty() && series.isEmpty() && s.continueWatching.isEmpty() && favs.isEmpty()) CgEmpty(tr("Add a playlist to get started", "أضف قائمة تشغيل للبدء"))
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
internal fun KatShelf(title: String, count: String? = null, onAll: (() -> Unit)? = null, content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit) {
    Column {
        Row(Modifier.padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(4.dp, 20.dp).clip(CircleShape).background(Brush.verticalGradient(listOf(CG.Amber, CG.Violet))))
            Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            count?.let { Text(it, color = CG.Faint, fontSize = 14.sp) }
            Spacer(Modifier.weight(1f))
            onAll?.let { KatSectionTitle("", Modifier.width(110.dp), onShowAll = it) }
        }
        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(vertical = 10.dp, horizontal = 4.dp), content = content)
    }
}

@Composable
internal fun KatPosterCard(title: String, image: String?, sub: String?, modifier: Modifier = Modifier, onLongClick: (() -> Unit)? = null, onClick: () -> Unit) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        CgCard(onClick = onClick, onLongClick = onLongClick, shape = RoundedCornerShape(16.dp), container = CG.Glass, modifier = Modifier.fillMaxWidth().aspectRatio(2f / 3f)) {
            if (image != null) AsyncImage(image, title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            else Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF2A1F5A), Color(0xFF120F30)))), contentAlignment = Alignment.Center) { Text(title.take(1), color = CG.Amber, fontSize = 40.sp, fontWeight = FontWeight.Black) }
        }
        Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(horizontal = 4.dp))
        sub?.takeIf { it.isNotBlank() }?.let { Text(it, color = CG.Faint, fontSize = 12.sp, maxLines = 1, modifier = Modifier.padding(horizontal = 4.dp)) }
    }
}

@Composable
internal fun KatWideCard(title: String, image: String?, sub: String?, progress: Float?, modifier: Modifier = Modifier, onClick: () -> Unit) {
    CgCard(onClick = onClick, shape = RoundedCornerShape(18.dp), container = CG.Glass, modifier = modifier) {
        Column {
            Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
                image?.let { AsyncImage(it, title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.5f to Color.Transparent, 1f to Color(0xCC07071A))))
                Box(Modifier.align(Alignment.Center).size(46.dp).clip(CircleShape).background(Color(0x66FFFFFF)).border(1.dp, Color(0x88FFFFFF), CircleShape), contentAlignment = Alignment.Center) { CgGlyph("▶", 22.dp, tint = Color.White) }
                progress?.let { Box(Modifier.align(Alignment.BottomCenter).padding(10.dp)) { KatThinProgress(it) } }
            }
            Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                sub?.takeIf { it.isNotBlank() }?.let { Text(it, color = CG.Sub, fontSize = 12.sp, maxLines = 1) }
            }
        }
    }
}

@Composable
internal fun KatThinProgress(f: Float, modifier: Modifier = Modifier, height: androidx.compose.ui.unit.Dp = 4.dp) {
    Box(modifier.fillMaxWidth().height(height).clip(CircleShape).background(Color(0x33FFFFFF))) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(f.coerceIn(0f, 1f)).clip(CircleShape).background(Brush.horizontalGradient(listOf(CG.Violet, CG.Amber))))
    }
}

internal fun katProgress(pr: com.streamvault.domain.model.Program?, now: Long = System.currentTimeMillis()): Float? =
    pr?.takeIf { it.endTime > it.startTime && it.startTime <= now && it.endTime > now }?.let { (now - it.startTime).toFloat() / (it.endTime - it.startTime) }

@Composable
internal fun KatChannelWide(c: Channel, modifier: Modifier = Modifier, onClick: () -> Unit) {
    CgCard(onClick = onClick, shape = RoundedCornerShape(18.dp), container = CG.Glass, modifier = modifier) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            KatLogoBox(c.name, c.logoUrl, 72.dp, 54.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(c.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                    if (katProgress(c.currentProgram) != null) KatTag("LIVE", bg = CG.Live, fg = Color.White)
                }
                Text(c.currentProgram?.title ?: tr("No guide data", "لا يوجد دليل"), color = CG.Sub, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                katProgress(c.currentProgram)?.let { KatThinProgress(it, height = 3.dp) }
            }
        }
    }
}

/** LIVE (deliberately NOT a grid): category chips column | vertical channel list (number, logo, name, now + thin
 *  progress, next revealed on focus) | big preview panel with now/next and a mini EPG timeline strip. */
@Composable
internal fun KatLiveTv(p: LiveTvParams) {
    var focusedId by remember { mutableStateOf<Long?>(null) }
    val catName = p.categories.firstOrNull { it.id == p.selectedCategoryId }?.name ?: p.sourceTitle
    KatLtrRow(Modifier.fillMaxSize().padding(top = 4.dp), spacing = 16.dp) { rtl ->
        rtl {
            Column(Modifier.width(220.dp).fillMaxHeight()) {
                Text(tr("Categories", "الفئات"), color = CG.Faint, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 10.dp, bottom = 8.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), contentPadding = PaddingValues(4.dp, 4.dp, 4.dp, 40.dp)) {
                    items(p.categories, key = { it.id }) { cat ->
                        val sel = cat.id == p.selectedCategoryId
                        KatChip(sel, { p.onCategoryClick(cat) }, { p.onCategoryLongClick(cat) },
                            Modifier.focusRequester(p.categoryRequester(cat.id)).onFocusChanged { if (it.isFocused) p.onCategoryFocused(cat) }.cgRight { p.onRequestChannelsFromCategory() }) {
                            CgCategoryGlyph(cat.name, p.isCategoryLocked(cat), androidx.tv.material3.LocalContentColor.current, 18.dp)
                            Text(cat.name, fontSize = 14.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            if (cat.count > 0) Text(cat.count.toString(), fontSize = 12.sp, color = androidx.tv.material3.LocalContentColor.current.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
        rtl {
            Column(Modifier.width(470.dp).fillMaxHeight().cg2Panel().padding(10.dp)) {
                Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(catName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Text(p.channels.size.toString() + " " + tr("channels", "قناة"), color = CG.Faint, fontSize = 13.sp)
                }
                KatSearchBox(p.channelSearchQuery, p.onChannelSearchChange, tr("Search channels…", "ابحث في القنوات…"), Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp), contentPadding = PaddingValues(4.dp, 6.dp, 4.dp, 40.dp)) {
                    items(p.channels.size, key = { p.channels[it].id }) { i ->
                        val c = p.channels[i]
                        val locked = p.isChannelLocked(c)
                        val moving = c.id == p.movingChannelId
                        val isFocused = focusedId == c.id
                        KatListRow(
                            selected = moving || (c.id == p.previewChannel?.id && !isFocused), onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c) }, vertical = 8.dp,
                            modifier = Modifier.focusRequester(p.channelRequester(c.id)).onFocusChanged { if (it.isFocused) { focusedId = c.id; p.onChannelFocused(c) } else if (focusedId == c.id) focusedId = null }.cgRight { p.onRequestPreviewFromChannel() }
                        ) {
                            Text(if (c.number > 0) c.number.toString() else (i + 1).toString(), color = CG.Amber, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(34.dp))
                            KatLogoBox(c.name, if (locked) null else c.logoUrl, 56.dp, 40.dp)
                            Column(Modifier.weight(1f).animateContentSize(), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text((if (moving) "⇅ " else "") + c.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                                    if (c.isFavorite) CgGlyph("♥", 14.dp, tint = CG.Amber)
                                    (if (locked) null else c.qualityBadge())?.let { KatTag(it, fg = Color.White, outlined = true) }
                                }
                                Text(c.currentProgram?.title ?: "—", color = CG.Sub, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                katProgress(c.currentProgram)?.let { KatThinProgress(it, height = 3.dp) }
                                if (isFocused) c.nextProgram?.let { n -> Text(tr("Next", "التالي") + "  " + cgClock(n.startTime) + "  " + n.title, color = CG.Amber.copy(alpha = 0.9f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                            }
                        }
                    }
                    if (p.channels.isEmpty()) item { CgEmpty(tr("No channels", "لا توجد قنوات")) }
                }
            }
        }
        rtl {
            val pc = p.channels.firstOrNull { it.id == focusedId } ?: p.previewChannel
            Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                CgCard(
                    onClick = { pc?.let(p.onChannelClick) }, container = Color.Black, shape = RoundedCornerShape(22.dp), zoom = 1.02f,
                    modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).focusRequester(p.previewFocusRequester).cgRight { p.onRequestChannelsFromPreview() }
                ) {
                    val engine = p.previewPlayerEngine
                    if (engine != null && p.previewChannel != null) PlayerRenderView(engine, PlayerSurfaceResizeMode.FIT, Modifier.fillMaxSize())
                    else pc?.logoUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(60.dp)) }
                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.6f to Color.Transparent, 1f to Color(0xCC000000))))
                    if (pc != null) Row(Modifier.align(Alignment.TopStart).padding(14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { KatTag("LIVE", bg = CG.Live, fg = Color.White) }
                    if (p.isPreviewLoading) Text("…", color = Color.White, fontSize = 22.sp, modifier = Modifier.align(Alignment.Center))
                    p.previewErrorMessage?.let { Text(it, color = CG.Sub, fontSize = 12.sp, maxLines = 2, modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp)) }
                }
                Column(Modifier.fillMaxWidth().weight(1f).cg2Panel().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        KatLogoBox(pc?.name ?: "TV", pc?.logoUrl, 54.dp, 40.dp)
                        Text(pc?.name ?: tr("Pick a channel", "اختر قناة"), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        pc?.number?.takeIf { it > 0 }?.let { Text("#" + it, color = CG.Amber, fontSize = 15.sp, fontWeight = FontWeight.Bold) }
                    }
                    val now = pc?.currentProgram; val next = pc?.nextProgram
                    Text(now?.title ?: tr("No guide data for this channel", "لا يوجد دليل برامج لهذه القناة"), color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (now != null && now.endTime > now.startTime) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(cgClock(now.startTime), color = CG.Sub, fontSize = 12.sp)
                        Box(Modifier.weight(1f)) { KatThinProgress(katProgress(now) ?: 0f) }
                        Text(cgClock(now.endTime), color = CG.Sub, fontSize = 12.sp)
                    }
                    next?.let { Text(tr("Next", "التالي") + " · " + cgClock(it.startTime) + "  " + it.title, color = CG.Sub, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    Spacer(Modifier.weight(1f))
                    KatMiniEpg(now, next)
                }
            }
        }
    }
}

/** Vertical pill chip used for the live category column. */
@Composable
internal fun KatChip(selected: Boolean, onClick: () -> Unit, onLongClick: (() -> Unit)?, modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    val shape = RoundedCornerShape(50)
    TvClickableSurface(
        onClick = onClick, onLongClick = onLongClick, modifier = modifier.fillMaxWidth(),
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = if (selected) Color(0x4DFFB547) else Color(0x40241F52), focusedContainerColor = CG.Amber, contentColor = Color.White, focusedContentColor = Color(0xFF14102A)),
        border = ClickableSurfaceDefaults.border(border = Border(androidx.compose.foundation.BorderStroke(1.dp, if (selected) Color(0x99FFB547) else Color(0x1FFFFFFF)), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.06f),
        glow = ClickableSurfaceDefaults.glow(focusedGlow = androidx.tv.material3.Glow(CG.Amber.copy(alpha = 0.55f), 14.dp))
    ) { Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), content = content) }
}

/** Mini EPG strip: a 2h window from now-30m with half-hour ticks, now and next blocks, and a now line. */
@Composable
internal fun KatMiniEpg(now: com.streamvault.domain.model.Program?, next: com.streamvault.domain.model.Program?) {
    val t = System.currentTimeMillis()
    val start = t - 30 * 60_000L - (t % (30 * 60_000L)); val span = 2 * 60 * 60_000L
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth()) { (0..3).forEach { i -> Text(cgClock(start + i * 30 * 60_000L), color = CG.Faint, fontSize = 11.sp, modifier = Modifier.weight(1f)) } }
        androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(12.dp)).background(Color(0x33000000))) {
            val w = maxWidth
            fun x(ms: Long) = w * ((ms - start).toFloat() / span).coerceIn(0f, 1f)
            listOfNotNull(now, next).forEachIndexed { i, pr ->
                val a = x(pr.startTime); val b = x(pr.endTime.takeIf { it > pr.startTime } ?: (pr.startTime + 30 * 60_000L))
                if (b > a) Box(Modifier.offset(x = a).width(b - a).fillMaxHeight().padding(2.dp).clip(RoundedCornerShape(10.dp))
                    .background(if (i == 0) Brush.horizontalGradient(listOf(Color(0x99FFB547), Color(0x668B5CF6))) else Brush.horizontalGradient(listOf(Color(0x40FFFFFF), Color(0x26FFFFFF))))
                    .padding(horizontal = 10.dp), contentAlignment = Alignment.CenterStart) {
                    Text(pr.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            if (now == null && next == null) Text(tr("No guide", "لا يوجد دليل"), color = CG.Faint, fontSize = 12.sp, modifier = Modifier.align(Alignment.Center))
            Box(Modifier.offset(x = x(t)).width(2.dp).fillMaxHeight().background(CG.Amber))
        }
    }
}

/** List row used by every column: transparent, red-glow selected pill, red border on focus. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun KatListRow(
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
internal fun KatLogoBox(name: String, logo: String?, w: androidx.compose.ui.unit.Dp, h: androidx.compose.ui.unit.Dp) {
    Box(Modifier.size(w, h).clip(RoundedCornerShape(10.dp)).background(Color(0x4D000000)), contentAlignment = Alignment.Center) {
        Text(name.take(3).uppercase(), color = Color.White, fontSize = (h.value / 2.8f).sp, fontWeight = FontWeight.Black, maxLines = 1)
        logo?.let { AsyncImage(it, name, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(4.dp)) }
    }
}
