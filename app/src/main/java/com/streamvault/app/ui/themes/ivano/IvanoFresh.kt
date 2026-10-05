package com.streamvault.app.ui.themes.ivano

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.streamvault.app.navigation.Routes
import com.streamvault.app.ui.components.PlayerRenderView
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.app.ui.themes.bespoke.*
import com.streamvault.domain.model.Channel
import com.streamvault.player.PlayerSurfaceResizeMode

/** Ivano design system: cinema black, vibrant purple, glass layers, scale + purple glow focus. */
internal object IV {
    val Bg = Color(0xFF0A0A0A)
    val Panel = Color(0xFF111111)
    val Card = Color(0xFF181818)
    val Glass = Color(0x99161320)
    val Line = Color(0x22FFFFFF)
    val Text = Color.White
    val Sub = Color(0xFFB8B8C8)
    val Faint = Color(0xFF6E6E7A)
    val Purple = Color(0xFF9B5CFF)
    val PurpleDeep = Color(0xFF6A2BD9)
    val PurpleSoft = Color(0x339B5CFF)
    val Live = Color(0xFFFF3B5C)
    val Gold = Color(0xFFF5C518)
    val Grad = Brush.horizontalGradient(listOf(Color(0xFFB57BFF), Color(0xFF7B3CFF)))
    val R = RoundedCornerShape(18.dp)
    val RS = RoundedCornerShape(12.dp)
}

internal fun ivQuality(name: String): String? {
    val n = name.uppercase()
    return when { "4K" in n || "UHD" in n -> "4K"; "FHD" in n || "1080" in n -> "FHD"; "HD" in n || "720" in n -> "HD"; else -> null }
}

internal fun ivProgress(c: Channel): Float? = c.currentProgram?.let { pr ->
    if (pr.endTime > pr.startTime) ((System.currentTimeMillis() - pr.startTime).toFloat() / (pr.endTime - pr.startTime)).coerceIn(0f, 1f) else null
}

internal fun ivTime(ms: Long): String = java.text.SimpleDateFormat("HH:mm", java.util.Locale.US).format(java.util.Date(ms))

/** Generic Ivano focusable surface: scale 1.07 + purple glow + purple ring. */
@Composable
internal fun IvFocus(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = IV.R,
    color: Color = IV.Card,
    focusedColor: Color = IV.Card,
    scale: Float = 1.07f,
    onLongClick: (() -> Unit)? = null,
    onFocus: ((Boolean) -> Unit)? = null,
    content: @Composable BoxScope.(Boolean) -> Unit
) {
    var f by remember { mutableStateOf(false) }
    TvClickableSurface(
        onClick = onClick, onLongClick = onLongClick,
        modifier = modifier.onFocusChanged { f = it.isFocused || it.hasFocus; onFocus?.invoke(f) },
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = color, focusedContainerColor = focusedColor, contentColor = Color.White, focusedContentColor = Color.White),
        scale = ClickableSurfaceDefaults.scale(focusedScale = scale),
        border = ClickableSurfaceDefaults.border(focusedBorder = androidx.tv.material3.Border(androidx.compose.foundation.BorderStroke(2.5.dp, IV.Purple), shape = shape)),
        glow = ClickableSurfaceDefaults.glow(focusedGlow = androidx.tv.material3.Glow(IV.Purple.copy(alpha = 0.55f), 22.dp))
    ) { Box(Modifier.fillMaxSize()) { content(f) } }
}

@Composable
internal fun IvIcon(icon: ImageVector, tint: Color = Color.White, size: Dp = 24.dp) =
    androidx.compose.material3.Icon(icon, null, tint = tint, modifier = Modifier.size(size))

@Composable
internal fun IvBrand(size: Int = 26) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size((size * 1.3f).dp).clip(RoundedCornerShape(10.dp)).background(IV.Grad), contentAlignment = Alignment.Center) {
            Text("▶", color = Color.White, fontSize = (size * 0.6f).sp)
        }
        Text("Ivano", color = Color.White, fontSize = size.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
    }
}

@Composable
internal fun IvChip(label: String, selected: Boolean, modifier: Modifier = Modifier, icon: ImageVector? = null, count: Int? = null, onClick: () -> Unit) {
    IvFocus(onClick, modifier.height(52.dp), shape = RoundedCornerShape(50), color = if (selected) IV.Purple else Color(0x14FFFFFF), focusedColor = if (selected) IV.Purple else Color(0x33FFFFFF), scale = 1.06f) {
        Row(Modifier.align(Alignment.Center).padding(horizontal = 22.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            icon?.let { IvIcon(it, size = 20.dp) }
            Text(label, color = Color.White, fontSize = 18.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, maxLines = 1)
            count?.let { Text("$it", color = if (selected) Color(0xDDFFFFFF) else IV.Faint, fontSize = 14.sp) }
        }
    }
}

@Composable
internal fun IvTag(text: String, color: Color = IV.Purple, filled: Boolean = true) {
    Box(Modifier.clip(RoundedCornerShape(6.dp)).background(if (filled) color else Color.Transparent).border(1.dp, color, RoundedCornerShape(6.dp)).padding(horizontal = 7.dp, vertical = 2.dp)) {
        Text(text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
internal fun IvSkeleton(modifier: Modifier) {
    val t = rememberInfiniteTransition(label = "ivSk")
    val a by t.animateFloat(0.05f, 0.14f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "a")
    Box(modifier.clip(IV.RS).background(Color.White.copy(alpha = a)))
}

@Composable
internal fun IvEmpty(icon: ImageVector, title: String, sub: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(50.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(96.dp).clip(CircleShape).background(IV.PurpleSoft), contentAlignment = Alignment.Center) { IvIcon(icon, IV.Purple, 46.dp) }
        Text(title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(sub, color = IV.Sub, fontSize = 16.sp)
    }
}

@Composable
internal fun IvRowTitle(title: String, count: Int? = null) {
    Row(Modifier.fillMaxWidth().padding(bottom = 8.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        count?.let { Text("$it", color = IV.Faint, fontSize = 15.sp) }
        Spacer(Modifier.weight(1f))
        Text(tr("See All", "عرض الكل") + "  ›", color = IV.Purple, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
internal fun IvRail(title: String, count: Int? = null, content: @Composable RowScope.() -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        IvRowTitle(title, count)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 10.dp, horizontal = 6.dp), horizontalArrangement = Arrangement.spacedBy(18.dp), content = content)
    }
}

/** 2:3 poster: rating star, year, quality badge, optional season count. */
@Composable
internal fun IvPoster(title: String, image: String?, rating: Float?, year: String?, badge: String?, onClick: () -> Unit, modifier: Modifier = Modifier, onLongClick: (() -> Unit)? = null) {
    Column(modifier.width(170.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        IvFocus(onClick, Modifier.fillMaxWidth().aspectRatio(2f / 3f), shape = IV.RS, onLongClick = onLongClick) { f ->
            if (image != null) AsyncImage(image, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            else Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF2A1A44), IV.Card))), contentAlignment = Alignment.Center) { Text(title.take(2), color = IV.Sub, fontSize = 34.sp, fontWeight = FontWeight.Black) }
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.6f to Color.Transparent, 1f to Color(0xCC000000))))
            rating?.takeIf { it > 0 }?.let { r ->
                Row(Modifier.align(Alignment.TopStart).padding(8.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xCC000000)).padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("IMDb ", color = IV.Gold, fontSize = 11.sp, fontWeight = FontWeight.Black); Text("%.1f".format(r), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            badge?.let { Box(Modifier.align(Alignment.TopEnd).padding(8.dp)) { IvTag(it) } }
            if (f) Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(4.dp).background(IV.Grad))
        }
        Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        year?.let { Text(it, color = IV.Faint, fontSize = 13.sp, maxLines = 1) }
    }
}

/** 16:9 landscape card with title and optional progress. */
@Composable
internal fun IvWide(title: String, image: String?, sub: String?, progress: Float?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.width(320.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        IvFocus(onClick, Modifier.fillMaxWidth().aspectRatio(16f / 9f), shape = IV.RS) { f ->
            image?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) } ?: Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF2A1A44), IV.Card))))
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.5f to Color.Transparent, 1f to Color(0xDD000000))))
            if (f) Box(Modifier.align(Alignment.Center).size(58.dp).clip(CircleShape).background(IV.Purple), contentAlignment = Alignment.Center) { IvIcon(Icons.Outlined.PlayArrow, size = 34.dp) }
            progress?.let { pr ->
                Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(10.dp).height(5.dp).clip(CircleShape).background(Color(0x44FFFFFF))) {
                    Box(Modifier.fillMaxHeight().fillMaxWidth(pr.coerceIn(0.02f, 1f)).background(IV.Grad))
                }
            }
        }
        Text(title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        sub?.let { Text(it, color = IV.Faint, fontSize = 13.sp, maxLines = 1) }
    }
}

@Composable
internal fun IvButton(label: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier, primary: Boolean = true) {
    IvFocus(onClick, modifier.height(58.dp), shape = RoundedCornerShape(50), color = if (primary) IV.Purple else Color(0x26FFFFFF), focusedColor = if (primary) Color(0xFFAE7BFF) else Color(0x55FFFFFF), scale = 1.06f) {
        Row(Modifier.align(Alignment.Center).padding(horizontal = 28.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            IvIcon(icon, size = 26.dp); Text(label, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ───────────────────────────── SHELL ─────────────────────────────

private class IvNav(val route: String, val icon: ImageVector, val en: String, val ar: String)

@Composable
internal fun IvFreshShell(p: ShellParams) {
    val navs = listOf(
        IvNav(Routes.HOME, Icons.Outlined.Home, "Home", "الرئيسية"),
        IvNav(Routes.LIVE_TV, Icons.Outlined.LiveTv, "Live TV", "البث المباشر"),
        IvNav(Routes.SERIES, Icons.Outlined.VideoLibrary, "Series", "المسلسلات"),
        IvNav(Routes.MOVIES, Icons.Outlined.Movie, "Movies", "الأفلام"),
        IvNav(Routes.FAVORITES, Icons.Outlined.FavoriteBorder, "Favorites", "المفضلة"),
        IvNav(Routes.EPG, Icons.Outlined.Category, "Categories", "الأقسام"),
        IvNav(Routes.SEARCH, Icons.Outlined.Search, "Search", "البحث"),
        IvNav(Routes.SETTINGS, Icons.Outlined.Settings, "Settings", "الإعدادات")
    )
    val dir = LocalLayoutDirection.current
    CompositionLocalProvider(LocalCpNavigate provides p.onNavigate) {
        Box(p.modifier.fillMaxSize().background(IV.Bg)) {
            // ambient purple bloom
            Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0x2E7B3CFF), Color.Transparent), center = androidx.compose.ui.geometry.Offset(1400f, -100f), radius = 1300f)))
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(Modifier.fillMaxSize()) {
                    // left sidebar: icons, expands with labels while focused
                    var railFocus by remember { mutableStateOf(false) }
                    Column(
                        Modifier.fillMaxHeight().width(270.dp)
                            .background(Brush.horizontalGradient(listOf(Color(0xFF0E0B16), Color(0xF20A0A0A))))
                            .onFocusChanged { railFocus = it.hasFocus }.padding(vertical = 26.dp, horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(Modifier.padding(start = 6.dp, bottom = 22.dp)) { IvBrand(22) }
                        navs.forEach { n ->
                            val sel = onRoute(p.currentRoute, n.route)
                            IvFocus({ p.onNavigate(n.route) }, Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(14.dp), color = if (sel) IV.PurpleSoft else Color.Transparent, focusedColor = IV.Purple, scale = 1.04f) {
                                Row(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    IvIcon(n.icon, if (sel) Color(0xFFD9C2FF) else Color.White, 28.dp)
                                    CompositionLocalProvider(LocalLayoutDirection provides dir) { Text(tr(n.en, n.ar), color = Color.White, fontSize = 19.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium, maxLines = 1) }
                                }
                                if (sel) Box(Modifier.align(Alignment.CenterStart).width(4.dp).height(26.dp).clip(CircleShape).background(IV.Purple))
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        Box(Modifier.fillMaxWidth().height(1.dp).background(IV.Line))
                        Row(Modifier.padding(top = 14.dp, start = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.size(44.dp).clip(CircleShape).background(IV.PurpleDeep), contentAlignment = Alignment.Center) { Text("A", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black) }
                            CompositionLocalProvider(LocalLayoutDirection provides dir) { Column { Text("Alaa", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold); Text("Premium", color = IV.Purple, fontSize = 13.sp) } }
                        }
                    }
                    CompositionLocalProvider(LocalLayoutDirection provides dir) {
                        Column(Modifier.weight(1f).fillMaxHeight().padding(p.contentPadding)) {
                            if (p.topBarVisible) IvTopBar(p)
                            Column(Modifier.fillMaxSize().padding(horizontal = 36.dp)) {
                                if (p.showScreenHeader && !onRoute(p.currentRoute, Routes.HOME)) p.header?.let { Column(content = it) }
                                p.content(this)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IvTopBar(p: ShellParams) {
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { kotlinx.coroutines.delay(30_000); now = System.currentTimeMillis() } }
    Row(Modifier.fillMaxWidth().height(78.dp).padding(horizontal = 36.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(Modifier.weight(1f)) {
            Text(if (onRoute(p.currentRoute, Routes.HOME)) "" else p.title, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
            p.subtitle?.takeIf { it.isNotBlank() }?.let { Text(it, color = IV.Faint, fontSize = 13.sp, maxLines = 1) }
        }
        p.topBarActions?.let { Row(horizontalArrangement = Arrangement.spacedBy(10.dp), content = it) }
        IvRound(Icons.Outlined.Search) { p.onNavigate(Routes.SEARCH) }
        Box { IvRound(Icons.Outlined.Notifications) { p.onNavigate(Routes.SETTINGS) }; Box(Modifier.align(Alignment.TopEnd).padding(8.dp).size(9.dp).clip(CircleShape).background(IV.Purple)) }
        Text(ivTime(now), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        IvFocus({ p.onNavigate(Routes.SETTINGS) }, Modifier.size(48.dp), shape = CircleShape, color = IV.PurpleDeep, focusedColor = IV.Purple) {
            Text("A", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
internal fun IvRound(icon: ImageVector, size: Dp = 48.dp, active: Boolean = false, onClick: () -> Unit) {
    IvFocus(onClick, Modifier.size(size), shape = CircleShape, color = if (active) IV.Purple else Color(0x1AFFFFFF), focusedColor = IV.Purple, scale = 1.1f) {
        Box(Modifier.align(Alignment.Center)) { IvIcon(icon, size = size * 0.5f) }
    }
}

// ───────────────────────────── HOME ─────────────────────────────

@Composable
internal fun IvFreshHome(p: DashboardParams) {
    val s = p.uiState
    val first = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { first.requestFocus() } }
    val kMovie = tr("Movie", "فيلم"); val kSeries = tr("Series", "مسلسل")
    val heroes = remember(s.recommendedMovies, s.recentMovies, s.recentSeries) {
        val m = (s.recommendedMovies + s.recentMovies).distinctBy { it.id }.filter { it.backdropUrl != null || it.posterUrl != null }.take(4)
            .map { IvHeroItem(it.name, it.backdropUrl ?: it.posterUrl, it.year, it.rating, it.genre, it.plot, kMovie) { p.onMovieClick(it) } }
        val sr = s.recentSeries.filter { it.backdropUrl != null || it.posterUrl != null }.take(2)
            .map { IvHeroItem(it.name, it.backdropUrl ?: it.posterUrl, it.releaseDate?.take(4), it.rating, it.genre, it.plot, kSeries) { p.onSeriesClick(it) } }
        (m.take(2) + sr + m.drop(2)).take(6)
    }
    var idx by remember { mutableStateOf(0) }
    LaunchedEffect(heroes.size) { while (heroes.size > 1) { kotlinx.coroutines.delay(9000); idx = (idx + 1) % heroes.size } }
    val hero = heroes.getOrNull(idx.coerceIn(0, (heroes.size - 1).coerceAtLeast(0)))
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(26.dp)) {
        Box(Modifier.fillMaxWidth().height(400.dp).shadow(24.dp, IV.R).clip(IV.R).background(IV.Panel)) {
            androidx.compose.animation.Crossfade(hero?.image ?: s.feature.artworkUrl, label = "ivHero") { u -> u?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) } }
            Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xF50A0A0A), Color(0xAA0A0A0A), Color.Transparent))))
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.55f to Color.Transparent, 1f to IV.Bg)))
            Column(Modifier.align(Alignment.CenterStart).fillMaxWidth(0.55f).padding(start = 48.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { IvTag(hero?.kind ?: tr("Featured", "مميز")); IvTag(tr("Trending", "رائج"), IV.Purple, filled = false) }
                Text(hero?.title ?: s.feature.title.ifBlank { "Ivano" }, color = Color.White, fontSize = 54.sp, fontWeight = FontWeight.Light, letterSpacing = 4.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 60.sp)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    hero?.rating?.takeIf { it > 0 }?.let { Text("★ %.1f".format(it), color = IV.Gold, fontSize = 17.sp, fontWeight = FontWeight.Bold) }
                    hero?.year?.let { Text(it, color = IV.Sub, fontSize = 17.sp) }
                    hero?.genre?.split(",")?.take(2)?.joinToString(" • ")?.let { Text(it.trim(), color = IV.Sub, fontSize = 17.sp, maxLines = 1) }
                }
                Text(hero?.plot?.takeIf { it.isNotBlank() } ?: s.feature.summary, color = Color(0xE6FFFFFF), fontSize = 17.sp, maxLines = 3, overflow = TextOverflow.Ellipsis, lineHeight = 25.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.padding(top = 6.dp)) {
                    IvButton(tr("Watch now", "شاهد الآن"), Icons.Outlined.PlayArrow, { hero?.open?.invoke() ?: p.onNavigate(Routes.MOVIES) }, Modifier.focusRequester(first))
                    IvButton(tr("Details", "التفاصيل"), Icons.Outlined.Info, { hero?.open?.invoke() ?: p.onNavigate(Routes.MOVIES) }, primary = false)
                    IvButton(tr("My list", "قائمتي"), Icons.Outlined.Add, { p.onNavigate(Routes.FAVORITES) }, primary = false)
                }
            }
            if (heroes.size > 1) Row(Modifier.align(Alignment.BottomEnd).padding(28.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                heroes.indices.forEach { i -> Box(Modifier.size(if (i == idx) 30.dp else 10.dp, 10.dp).clip(CircleShape).background(if (i == idx) IV.Purple else Color(0x55FFFFFF))) }
            }
        }
        val cont = s.continueWatching.take(10)
        IvRail(tr("Continue watching", "متابعة المشاهدة"), cont.size.takeIf { it > 0 }) {
            if (cont.isEmpty()) { if (s.isLoading) repeat(4) { IvSkeleton(Modifier.width(320.dp).aspectRatio(16f / 9f)) } else Text(tr("Nothing yet, start watching something", "لا يوجد شيء بعد، ابدأ المشاهدة"), color = IV.Faint, fontSize = 17.sp) }
            cont.forEach { h ->
                val pr = if (h.totalDurationMs > 0) h.resumePositionMs.toFloat() / h.totalDurationMs else null
                IvWide(h.title, h.posterUrl, if (h.seasonNumber != null) "S${h.seasonNumber} • E${h.episodeNumber ?: 1}" else cgDuration(h.resumePositionMs), pr, { p.onContinueWatchingItemClick(h) })
            }
        }
        val live = (s.favoriteChannels + s.recentChannels).distinctBy { it.id }.take(12)
        IvRail(tr("Live now", "مباشر الآن"), live.size.takeIf { it > 0 }) {
            if (live.isEmpty()) { if (s.isLoading) repeat(5) { IvSkeleton(Modifier.width(260.dp).height(146.dp)) } else IvButton(tr("Open Live TV", "افتح البث المباشر"), Icons.Outlined.LiveTv, { p.onNavigate(Routes.LIVE_TV) }, primary = false) }
            live.forEach { c -> IvChannelCard(c, false, Modifier.width(280.dp), onClick = { p.onRecentChannelClick(c, null) }) }
        }
        val trending = s.topRatedMovies.ifEmpty { s.recentMovies }.take(12)
        IvRail(tr("Trending", "الأكثر رواجاً")) {
            if (trending.isEmpty()) repeat(7) { IvSkeleton(Modifier.width(170.dp).aspectRatio(2f / 3f)) }
            trending.forEachIndexed { i, m ->
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("${i + 1}", color = Color(0x33FFFFFF), fontSize = 96.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(end = 4.dp))
                    IvPoster(m.name, m.posterUrl, m.rating, m.year, ivQuality(m.name), { p.onMovieClick(m) })
                }
            }
        }
        val rec = s.recommendedMovies.ifEmpty { s.recentMovies }.take(14)
        IvRail(tr("Recommended for you", "مقترح لك")) {
            if (rec.isEmpty()) repeat(7) { IvSkeleton(Modifier.width(170.dp).aspectRatio(2f / 3f)) }
            rec.forEach { m -> IvPoster(m.name, m.posterUrl, m.rating, m.year, ivQuality(m.name), { p.onMovieClick(m) }) }
        }
        if (s.recentSeries.isNotEmpty()) IvRail(tr("New series", "مسلسلات جديدة")) {
            s.recentSeries.take(14).forEach { m -> IvPoster(m.name, m.posterUrl, m.rating, m.releaseDate?.take(4), null, { p.onSeriesClick(m) }) }
        }
        Spacer(Modifier.height(30.dp))
    }
}

private class IvHeroItem(val title: String, val image: String?, val year: String?, val rating: Float?, val genre: String?, val plot: String?, val kind: String, val open: () -> Unit)

// ───────────────────────────── LIVE TV (grid) ─────────────────────────────

/** 16:9 channel card: logo, name, current program, progress, quality, star. Grows on focus with program detail. */
@Composable
internal fun IvChannelCard(c: Channel, moving: Boolean, modifier: Modifier = Modifier, focusRequester: FocusRequester? = null, onFocus: () -> Unit = {}, onLongClick: (() -> Unit)? = null, onClick: () -> Unit) {
    var f by remember { mutableStateOf(false) }
    val pr = c.currentProgram
    IvFocus(onClick, modifier.aspectRatio(16f / 9f).let { if (focusRequester != null) it.focusRequester(focusRequester) else it }, shape = IV.R,
        color = if (moving) IV.PurpleSoft else IV.Card, focusedColor = Color(0xFF1E1730), scale = 1.1f, onLongClick = onLongClick,
        onFocus = { f = it; if (it) onFocus() }) { _ ->
        Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0x229B5CFF), Color.Transparent))))
        Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(56.dp, 40.dp).clip(RoundedCornerShape(8.dp)).background(Color(0x14FFFFFF)), contentAlignment = Alignment.Center) {
                    c.logoUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(4.dp)) } ?: Text(c.name.take(2), color = Color.White, fontWeight = FontWeight.Bold)
                }
                Column(Modifier.weight(1f)) {
                    Text(c.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (c.number > 0) Text("#${c.number}", color = IV.Faint, fontSize = 12.sp)
                }
                if (c.isFavorite) IvIcon(Icons.Outlined.Star, IV.Gold, 20.dp)
            }
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IvTag(tr("LIVE", "مباشر"), IV.Live)
                    ivQuality(c.name)?.let { IvTag(it, IV.Purple, filled = false) }
                    Text(pr?.title ?: tr("No guide info", "لا يوجد دليل"), color = IV.Sub, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                }
                if (f && pr != null) Text("${ivTime(pr.startTime)} - ${ivTime(pr.endTime)}" + (c.nextProgram?.let { "  •  ${tr("Next", "التالي")}: ${it.title}" } ?: ""), color = IV.Faint, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Box(Modifier.fillMaxWidth().height(4.dp).clip(CircleShape).background(Color(0x22FFFFFF))) {
                    Box(Modifier.fillMaxHeight().fillMaxWidth(ivProgress(c) ?: 0f).background(IV.Grad))
                }
            }
        }
    }
}

@Composable
internal fun IvFreshLiveTv(p: LiveTvParams) {
    var quality by remember { mutableStateOf("ALL") }
    var favOnly by remember { mutableStateOf(false) }
    var grid by remember { mutableStateOf(true) }
    var focused by remember { mutableStateOf<Channel?>(null) }
    val shown = remember(p.channels, quality, favOnly) {
        p.channels.filter { c -> (!favOnly || c.isFavorite) && (quality == "ALL" || ivQuality(c.name) == quality) }
    }
    val foc = focused ?: p.previewChannel ?: shown.firstOrNull()
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // category chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)) {
            items(p.categories, key = { it.id }) { cat ->
                IvChip(cat.name, cat.id == p.selectedCategoryId, Modifier.focusRequester(p.categoryRequester(cat.id)).onFocusChanged { if (it.isFocused) p.onCategoryFocused(cat) },
                    icon = if (p.isCategoryLocked(cat)) Icons.Outlined.Lock else null, count = cat.count.takeIf { it > 0 }) { p.onCategoryClick(cat) }
            }
        }
        // toolbar
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.width(360.dp).height(52.dp).clip(RoundedCornerShape(50)).background(Color(0x14FFFFFF)).padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                IvIcon(Icons.Outlined.Search, IV.Sub, 22.dp)
                Box(Modifier.weight(1f)) {
                    if (p.channelSearchQuery.isEmpty()) Text(tr("Quick channel search", "بحث سريع عن قناة"), color = IV.Faint, fontSize = 16.sp)
                    BasicTextField(p.channelSearchQuery, p.onChannelSearchChange, singleLine = true, textStyle = TextStyle(color = Color.White, fontSize = 17.sp), cursorBrush = SolidColor(IV.Purple), modifier = Modifier.fillMaxWidth())
                }
            }
            listOf("ALL" to tr("All", "الكل"), "HD" to "HD", "FHD" to "FHD", "4K" to "4K").forEach { (k, l) -> IvChip(l, quality == k) { quality = k } }
            IvChip(tr("Favorites", "المفضلة"), favOnly, icon = Icons.Outlined.Star) { favOnly = !favOnly }
            Spacer(Modifier.weight(1f))
            IvRound(Icons.Outlined.GridView, active = grid) { grid = true }
            IvRound(Icons.Outlined.ViewList, active = !grid) { grid = false }
        }
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Column(Modifier.weight(1f).fillMaxHeight()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                    Box(Modifier.size(10.dp).clip(CircleShape).background(IV.Live))
                    Text(tr("On air now", "الآن يُبث"), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("${shown.size} ${tr("channels", "قناة")}", color = IV.Faint, fontSize = 15.sp)
                }
                when {
                    shown.isEmpty() && p.channels.isEmpty() -> LazyVerticalGrid(GridCells.Fixed(3), horizontalArrangement = Arrangement.spacedBy(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) { items(List(9) { it }) { IvSkeleton(Modifier.fillMaxWidth().aspectRatio(16f / 9f)) } }
                    shown.isEmpty() -> IvEmpty(Icons.Outlined.TvOff, tr("No channels match", "لا توجد قنوات مطابقة"), tr("Try another quality or category", "جرّب جودة أو قسماً آخر"))
                    grid -> LazyVerticalGrid(GridCells.Fixed(3), horizontalArrangement = Arrangement.spacedBy(22.dp), verticalArrangement = Arrangement.spacedBy(22.dp), contentPadding = PaddingValues(12.dp)) {
                        items(shown, key = { it.id }) { c ->
                            IvChannelCard(c, p.movingChannelId == c.id, Modifier.fillMaxWidth(), p.channelRequester(c.id), onFocus = { focused = c; p.onChannelFocused(c) }, onLongClick = { p.onChannelLongClick(c) }) { p.onChannelClick(c) }
                        }
                    }
                    else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(8.dp)) {
                        items(shown, key = { it.id }) { c ->
                            IvFocus({ p.onChannelClick(c) }, Modifier.fillMaxWidth().height(78.dp).focusRequester(p.channelRequester(c.id)), shape = IV.RS, color = if (p.movingChannelId == c.id) IV.PurpleSoft else IV.Card, focusedColor = Color(0xFF1E1730), scale = 1.02f,
                                onLongClick = { p.onChannelLongClick(c) }, onFocus = { if (it) { focused = c; p.onChannelFocused(c) } }) {
                                Row(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Text(if (c.number > 0) "${c.number}" else "•", color = IV.Faint, fontSize = 16.sp, modifier = Modifier.width(40.dp))
                                    Box(Modifier.size(64.dp, 44.dp).clip(RoundedCornerShape(8.dp)).background(Color(0x14FFFFFF))) { c.logoUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(4.dp)) } }
                                    Text(c.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                    Text(c.currentProgram?.title.orEmpty(), color = IV.Sub, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                    ivQuality(c.name)?.let { IvTag(it, IV.Purple, false) }
                                    if (c.isFavorite) IvIcon(Icons.Outlined.Star, IV.Gold, 20.dp)
                                }
                            }
                        }
                    }
                }
            }
            // focused preview + program info (glass)
            Column(Modifier.width(400.dp).fillMaxHeight().clip(IV.R).background(IV.Glass).border(1.dp, IV.Line, IV.R).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                IvFocus({ foc?.let(p.onChannelClick) }, Modifier.fillMaxWidth().aspectRatio(16f / 9f).focusRequester(p.previewFocusRequester), shape = IV.RS, color = Color.Black, focusedColor = Color.Black, scale = 1.03f) {
                    val eng = p.previewPlayerEngine
                    if (eng != null && p.previewChannel != null) PlayerRenderView(eng, PlayerSurfaceResizeMode.FIT, Modifier.fillMaxSize())
                    else foc?.logoUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(40.dp)) }
                    if (p.isPreviewLoading) Text("…", color = Color.White, fontSize = 26.sp, modifier = Modifier.align(Alignment.Center))
                    Box(Modifier.align(Alignment.TopStart).padding(10.dp)) { IvTag(tr("LIVE", "مباشر"), IV.Live) }
                }
                Text(foc?.name ?: tr("Pick a channel", "اختر قناة"), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                foc?.currentProgram?.let { pr ->
                    Text(pr.title, color = Color(0xFFD9C2FF), fontSize = 18.sp, fontWeight = FontWeight.SemiBold, maxLines = 2)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(ivTime(pr.startTime), color = IV.Sub, fontSize = 14.sp)
                        Box(Modifier.weight(1f).height(5.dp).clip(CircleShape).background(Color(0x22FFFFFF))) { Box(Modifier.fillMaxHeight().fillMaxWidth(ivProgress(foc) ?: 0f).background(IV.Grad)) }
                        Text(ivTime(pr.endTime), color = IV.Sub, fontSize = 14.sp)
                    }
                    if (pr.description.isNotBlank()) Text(pr.description, color = IV.Sub, fontSize = 14.sp, maxLines = 4, overflow = TextOverflow.Ellipsis, lineHeight = 20.sp)
                } ?: Text(tr("No program guide for this channel", "لا يوجد دليل برامج لهذه القناة"), color = IV.Faint, fontSize = 15.sp)
                foc?.nextProgram?.let { n -> Text("${tr("Next", "التالي")} • ${ivTime(n.startTime)}  ${n.title}", color = IV.Faint, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                p.previewErrorMessage?.let { Text(it, color = IV.Live, fontSize = 13.sp, maxLines = 2) }
                Spacer(Modifier.weight(1f))
                IvButton(tr("Watch with guide", "شاهد مع الدليل"), Icons.Outlined.PlayArrow, { foc?.let(p.onChannelClick) }, Modifier.fillMaxWidth())
            }
        }
    }
}
