package com.streamvault.app.ui.themes.lulu

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

/** Lulu design system: cinema black, vibrant teal, glass layers, scale + teal glow focus. */
internal object LU {
    val Bg = Color(0xFF040D12)
    val Panel = Color(0xFF0B1B22)
    val Card = Color(0xFF10242C)
    val Glass = Color(0x99102730)
    val Line = Color(0x22FFFFFF)
    val Text = Color.White
    val Sub = Color(0xFFA0B2B8)
    val Faint = Color(0xFF708B95)
    val Purple = Color(0xFF00B8BB)
    val PurpleDeep = Color(0xFF007D88)
    val PurpleSoft = Color(0x3300B8BB)
    val Live = Color(0xFFFF3B5C)
    val Gold = Color(0xFFF5C518)
    val Grad = Brush.horizontalGradient(listOf(Color(0xFF00D2D3), Color(0xFF008C99)))
    val R = RoundedCornerShape(18.dp)
    val RS = RoundedCornerShape(12.dp)
}

internal fun luQuality(name: String): String? {
    val n = name.uppercase()
    return when { "4K" in n || "UHD" in n -> "4K"; "FHD" in n || "1080" in n -> "FHD"; "HD" in n || "720" in n -> "HD"; else -> null }
}

internal fun luProgress(c: Channel): Float? = c.currentProgram?.let { pr ->
    if (pr.endTime > pr.startTime) ((System.currentTimeMillis() - pr.startTime).toFloat() / (pr.endTime - pr.startTime)).coerceIn(0f, 1f) else null
}

internal fun luTime(ms: Long): String = java.text.SimpleDateFormat("HH:mm", java.util.Locale.US).format(java.util.Date(ms))

/** Generic Lulu focusable surface: scale 1.07 + teal glow + teal ring. */
@Composable
internal fun LuFocus(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = LU.R,
    color: Color = LU.Card,
    focusedColor: Color = LU.Card,
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
        border = ClickableSurfaceDefaults.border(focusedBorder = androidx.tv.material3.Border(androidx.compose.foundation.BorderStroke(2.5.dp, Color.White), shape = shape)),
        glow = ClickableSurfaceDefaults.glow(focusedGlow = androidx.tv.material3.Glow(LU.Purple.copy(alpha = 0.22f), 10.dp))
    ) { Box(Modifier.fillMaxSize()) { content(f) } }
}

@Composable
internal fun LuIcon(icon: ImageVector, tint: Color = Color.White, size: Dp = 24.dp) =
    androidx.compose.material3.Icon(icon, null, tint = tint, modifier = Modifier.size(size))

@Composable
internal fun LuBrand(size: Int = 26) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size((size * 1.3f).dp).clip(RoundedCornerShape(10.dp)).background(LU.Grad), contentAlignment = Alignment.Center) {
            Text("▶", color = Color.White, fontSize = (size * 0.6f).sp)
        }
        Text("lulu", color = Color.White, fontSize = size.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
    }
}

@Composable
internal fun LuChip(label: String, selected: Boolean, modifier: Modifier = Modifier, icon: ImageVector? = null, count: Int? = null, onClick: () -> Unit) {
    LuFocus(onClick, modifier.height(52.dp), shape = RoundedCornerShape(50), color = if (selected) LU.Purple else Color(0x14FFFFFF), focusedColor = if (selected) LU.Purple else Color(0x33FFFFFF), scale = 1.06f) {
        Row(Modifier.align(Alignment.Center).padding(horizontal = 22.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            icon?.let { LuIcon(it, size = 20.dp) }
            Text(label, color = Color.White, fontSize = 18.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, maxLines = 1)
            count?.let { Text("$it", color = if (selected) Color(0xDDFFFFFF) else LU.Faint, fontSize = 14.sp) }
        }
    }
}

@Composable
internal fun LuTag(text: String, color: Color = LU.Purple, filled: Boolean = true) {
    Box(Modifier.clip(RoundedCornerShape(6.dp)).background(if (filled) color else Color.Transparent).border(1.dp, color, RoundedCornerShape(6.dp)).padding(horizontal = 7.dp, vertical = 2.dp)) {
        Text(text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
internal fun LuSkeleton(modifier: Modifier) {
    val t = rememberInfiniteTransition(label = "luSk")
    val a by t.animateFloat(0.05f, 0.14f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "a")
    Box(modifier.clip(LU.RS).background(Color.White.copy(alpha = a)))
}

@Composable
internal fun LuEmpty(icon: ImageVector, title: String, sub: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(50.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(96.dp).clip(CircleShape).background(LU.PurpleSoft), contentAlignment = Alignment.Center) { LuIcon(icon, LU.Purple, 46.dp) }
        Text(title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(sub, color = LU.Sub, fontSize = 16.sp)
    }
}

@Composable
internal fun LuRowTitle(title: String, count: Int? = null) {
    Row(Modifier.fillMaxWidth().padding(bottom = 8.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        count?.let { Text("$it", color = LU.Faint, fontSize = 15.sp) }
        Spacer(Modifier.weight(1f))
        Text(tr("See All", "عرض الكل") + "  ›", color = LU.Purple, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
internal fun LuRail(title: String, count: Int? = null, content: @Composable RowScope.() -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        LuRowTitle(title, count)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 10.dp, horizontal = 6.dp), horizontalArrangement = Arrangement.spacedBy(18.dp), content = content)
    }
}

/** 2:3 poster: rating star, year, quality badge, optional season count. */
@Composable
internal fun LuPoster(title: String, image: String?, rating: Float?, year: String?, badge: String?, onClick: () -> Unit, modifier: Modifier = Modifier, onLongClick: (() -> Unit)? = null) {
    Column(modifier.width(170.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LuFocus(onClick, Modifier.fillMaxWidth().aspectRatio(2f / 3f), shape = LU.RS, onLongClick = onLongClick) { f ->
            if (image != null) AsyncImage(image, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            else Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF123F48), LU.Card))), contentAlignment = Alignment.Center) { Text(title.take(2), color = LU.Sub, fontSize = 34.sp, fontWeight = FontWeight.Black) }
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.6f to Color.Transparent, 1f to Color(0xCC000000))))
            rating?.takeIf { it > 0 }?.let { r ->
                Row(Modifier.align(Alignment.TopStart).padding(8.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xCC000000)).padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("IMDb ", color = LU.Gold, fontSize = 11.sp, fontWeight = FontWeight.Black); Text("%.1f".format(r), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            badge?.let { Box(Modifier.align(Alignment.TopEnd).padding(8.dp)) { LuTag(it) } }
            if (f) Box(Modifier.align(Alignment.BottomStart).padding(12.dp).size(38.dp).clip(CircleShape).background(LU.Gold), contentAlignment = Alignment.Center) { LuIcon(Icons.Outlined.PlayArrow, LU.Bg, 26.dp) }
        }
        Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        year?.let { Text(it, color = LU.Faint, fontSize = 13.sp, maxLines = 1) }
    }
}

/** 16:9 landscape card with title and optional progress. */
@Composable
internal fun LuWide(title: String, image: String?, sub: String?, progress: Float?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.width(320.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LuFocus(onClick, Modifier.fillMaxWidth().aspectRatio(16f / 9f), shape = LU.RS) { f ->
            image?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) } ?: Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF123F48), LU.Card))))
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.5f to Color.Transparent, 1f to Color(0xDD000000))))
            if (f) Box(Modifier.align(Alignment.Center).size(58.dp).clip(CircleShape).background(LU.Gold), contentAlignment = Alignment.Center) { LuIcon(Icons.Outlined.PlayArrow, LU.Bg, 34.dp) }
            progress?.let { pr ->
                Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(10.dp).height(5.dp).clip(CircleShape).background(Color(0x44FFFFFF))) {
                    Box(Modifier.fillMaxHeight().fillMaxWidth(pr.coerceIn(0.02f, 1f)).background(LU.Grad))
                }
            }
        }
        Text(title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        sub?.let { Text(it, color = LU.Faint, fontSize = 13.sp, maxLines = 1) }
    }
}

@Composable
internal fun LuButton(label: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier, primary: Boolean = true) {
    LuFocus(onClick, modifier.height(58.dp), shape = RoundedCornerShape(50), color = if (primary) LU.Gold else Color(0x26FFFFFF), focusedColor = if (primary) Color(0xFFFFD05A) else Color(0x55FFFFFF), scale = 1.06f) {
        Row(Modifier.align(Alignment.Center).padding(horizontal = 28.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            LuIcon(icon, tint = if(primary) LU.Bg else Color.White, size = 26.dp); Text(label, color = if(primary) LU.Bg else Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ───────────────────────────── SHELL ─────────────────────────────

private class LuNav(val route: String, val icon: ImageVector, val en: String, val ar: String)

/** Floating playbox capsule keeps the artwork edge-to-edge, unlike the receiver side rail. */
@Composable
internal fun LuFreshShell(p: ShellParams) {
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    val drift = rememberInfiniteTransition(label = "luluAmbient")
    val bloom by drift.animateFloat(0.15f, 0.38f, infiniteRepeatable(tween(6500), RepeatMode.Reverse), label = "bloom")
    LaunchedEffect(Unit) { while (true) { kotlinx.coroutines.delay(30_000); now = System.currentTimeMillis() } }
    val tabs = listOf(
        LuNav(Routes.HOME, Icons.Outlined.Home, "Home", "الرئيسية"),
        LuNav(Routes.LIVE_TV, Icons.Outlined.LiveTv, "Live TV", "مباشر"),
        LuNav(Routes.MOVIES, Icons.Outlined.Movie, "Movies", "أفلام"),
        LuNav(Routes.SERIES, Icons.Outlined.VideoLibrary, "Series", "مسلسلات"),
        LuNav(Routes.FAVORITES, Icons.Outlined.FavoriteBorder, "My List", "قائمتي")
    )
    CompositionLocalProvider(LocalCpNavigate provides p.onNavigate) {
        Box(p.modifier.fillMaxSize().background(LU.Bg)) {
            Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(LU.Purple.copy(alpha = bloom), Color.Transparent), center = androidx.compose.ui.geometry.Offset(1100f, -180f), radius = 1800f)))
            Column(Modifier.fillMaxSize().padding(p.contentPadding)) {
                if (p.topBarVisible) Row(Modifier.fillMaxWidth().padding(horizontal = 30.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.clip(CircleShape).background(Color(0xC0092832)).border(1.dp, Color(0x2200D2D3), CircleShape).padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Box(Modifier.padding(horizontal = 12.dp)) { LuBrand(21) }
                        tabs.forEach { n ->
                            val selected = onRoute(p.currentRoute, n.route)
                            LuFocus({ p.onNavigate(n.route) }, Modifier.width(if(n.route == Routes.HOME) 46.dp else 86.dp).height(42.dp), shape = CircleShape, color = if(selected) LU.PurpleDeep else Color.Transparent, focusedColor = LU.Purple, scale = 1.04f) {
                                Box(Modifier.align(Alignment.Center)) {
                                    if(n.route == Routes.HOME) LuIcon(n.icon, size = 23.dp)
                                    else Text(tr(n.en,n.ar), color = Color.White, fontSize = 14.sp, fontWeight = if(selected) FontWeight.Bold else FontWeight.Medium)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Text(java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date(now)), color = LU.Sub, fontSize = 13.sp)
                    LuRound(Icons.Outlined.Search, 42.dp) { p.onNavigate(Routes.SEARCH) }
                    LuRound(Icons.Outlined.Settings, 42.dp) { p.onNavigate(Routes.SETTINGS) }
                    LuRound(Icons.Outlined.AccountCircle, 42.dp) { p.onNavigate(Routes.SETTINGS) }
                }
                Column(Modifier.fillMaxSize().padding(horizontal = 34.dp)) {
                    if(p.showScreenHeader && !onRoute(p.currentRoute,Routes.HOME)) p.header?.let { Column(content = it) }
                    p.content(this)
                }
            }
        }
    }
}

@Composable
private fun LuTopBar(p: ShellParams) {
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { kotlinx.coroutines.delay(30_000); now = System.currentTimeMillis() } }
    Row(Modifier.fillMaxWidth().height(78.dp).padding(horizontal = 36.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(Modifier.weight(1f)) {
            Text(if (onRoute(p.currentRoute, Routes.HOME)) "" else p.title, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
            p.subtitle?.takeIf { it.isNotBlank() }?.let { Text(it, color = LU.Faint, fontSize = 13.sp, maxLines = 1) }
        }
        p.topBarActions?.let { Row(horizontalArrangement = Arrangement.spacedBy(10.dp), content = it) }
        LuRound(Icons.Outlined.Search) { p.onNavigate(Routes.SEARCH) }
        Box { LuRound(Icons.Outlined.Notifications) { p.onNavigate(Routes.SETTINGS) }; Box(Modifier.align(Alignment.TopEnd).padding(8.dp).size(9.dp).clip(CircleShape).background(LU.Purple)) }
        Text(luTime(now), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        LuFocus({ p.onNavigate(Routes.SETTINGS) }, Modifier.size(48.dp), shape = CircleShape, color = LU.PurpleDeep, focusedColor = LU.Purple) {
            Text("A", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
internal fun LuRound(icon: ImageVector, size: Dp = 48.dp, active: Boolean = false, onClick: () -> Unit) {
    LuFocus(onClick, Modifier.size(size), shape = CircleShape, color = if (active) LU.Purple else Color(0x1AFFFFFF), focusedColor = LU.Purple, scale = 1.1f) {
        Box(Modifier.align(Alignment.Center)) { LuIcon(icon, size = size * 0.5f) }
    }
}

// ───────────────────────────── HOME ─────────────────────────────

@Composable
internal fun LuFreshHome(p: DashboardParams) {
    val s = p.uiState
    val first = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { first.requestFocus() } }
    val kMovie = tr("Movie", "فيلم"); val kSeries = tr("Series", "مسلسل")
    val heroes = remember(s.recommendedMovies, s.recentMovies, s.recentSeries) {
        val m = (s.recommendedMovies + s.recentMovies).distinctBy { it.id }.filter { it.backdropUrl != null || it.posterUrl != null }.take(4)
            .map { LuHeroItem(it.name, it.backdropUrl ?: it.posterUrl, it.year, it.rating, it.genre, it.plot, kMovie) { p.onMovieClick(it) } }
        val sr = s.recentSeries.filter { it.backdropUrl != null || it.posterUrl != null }.take(2)
            .map { LuHeroItem(it.name, it.backdropUrl ?: it.posterUrl, it.releaseDate?.take(4), it.rating, it.genre, it.plot, kSeries) { p.onSeriesClick(it) } }
        (m.take(2) + sr + m.drop(2)).take(6)
    }
    var idx by remember { mutableStateOf(0) }
    LaunchedEffect(heroes.size) { while (heroes.size > 1) { kotlinx.coroutines.delay(9000); idx = (idx + 1) % heroes.size } }
    val hero = heroes.getOrNull(idx.coerceIn(0, (heroes.size - 1).coerceAtLeast(0)))
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(26.dp)) {
        Box(Modifier.fillMaxWidth().height(325.dp).clip(LU.R).background(LU.Panel)) {
            androidx.compose.animation.Crossfade(hero?.image ?: s.feature.artworkUrl, label = "luHero") { u -> u?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) } }
            Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xF5040D12), Color(0xAA040D12), Color.Transparent))))
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.55f to Color.Transparent, 1f to LU.Bg)))
            Column(Modifier.align(Alignment.CenterStart).fillMaxWidth(0.55f).padding(start = 48.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { LuTag(hero?.kind ?: tr("Featured", "مميز"), Color(0xFF9BD446)); LuTag(tr("Featured", "مميز"), LU.Purple, filled = false) }
                Text(hero?.title ?: s.feature.title.ifBlank { "Lulu" }, color = Color.White, fontSize = 46.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 50.sp)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    hero?.rating?.takeIf { it > 0 }?.let { Text("★ %.1f".format(it), color = LU.Gold, fontSize = 17.sp, fontWeight = FontWeight.Bold) }
                    hero?.year?.let { Text(it, color = LU.Sub, fontSize = 17.sp) }
                    hero?.genre?.split(",")?.take(2)?.joinToString(" • ")?.let { Text(it.trim(), color = LU.Sub, fontSize = 17.sp, maxLines = 1) }
                }
                Text(hero?.plot?.takeIf { it.isNotBlank() } ?: s.feature.summary, color = Color(0xE6FFFFFF), fontSize = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 25.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.padding(top = 6.dp)) {
                    LuButton(tr("Play", "تشغيل"), Icons.Outlined.PlayArrow, { hero?.open?.invoke() ?: p.onNavigate(Routes.MOVIES) }, Modifier.focusRequester(first))
                    LuButton(tr("Details", "التفاصيل"), Icons.Outlined.Info, { hero?.open?.invoke() ?: p.onNavigate(Routes.MOVIES) }, primary = false)
                    LuButton(tr("My list", "قائمتي"), Icons.Outlined.Add, { p.onNavigate(Routes.FAVORITES) }, primary = false)
                }
            }
            if (heroes.size > 1) Row(Modifier.align(Alignment.BottomEnd).padding(28.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                heroes.indices.forEach { i -> Box(Modifier.size(if (i == idx) 30.dp else 10.dp, 10.dp).clip(CircleShape).background(if (i == idx) LU.Purple else Color(0x55FFFFFF))) }
            }
        }
        val cont = s.continueWatching.take(10)
        LuRail(tr("Continue watching", "متابعة المشاهدة"), cont.size.takeIf { it > 0 }) {
            if (cont.isEmpty()) { if (s.isLoading) repeat(4) { LuSkeleton(Modifier.width(320.dp).aspectRatio(16f / 9f)) } else Text(tr("Nothing yet, start watching something", "لا يوجد شيء بعد، ابدأ المشاهدة"), color = LU.Faint, fontSize = 17.sp) }
            cont.forEach { h ->
                val pr = if (h.totalDurationMs > 0) h.resumePositionMs.toFloat() / h.totalDurationMs else null
                LuWide(h.title, h.posterUrl, if (h.seasonNumber != null) "S${h.seasonNumber} • E${h.episodeNumber ?: 1}" else cgDuration(h.resumePositionMs), pr, { p.onContinueWatchingItemClick(h) })
            }
        }
        val live = (s.favoriteChannels + s.recentChannels).distinctBy { it.id }.take(12)
        LuRail(tr("Live now", "مباشر الآن"), live.size.takeIf { it > 0 }) {
            if (live.isEmpty()) { if (s.isLoading) repeat(5) { LuSkeleton(Modifier.width(260.dp).height(146.dp)) } else LuButton(tr("Open Live TV", "افتح البث المباشر"), Icons.Outlined.LiveTv, { p.onNavigate(Routes.LIVE_TV) }, primary = false) }
            live.forEach { c -> LuChannelCard(c, false, Modifier.width(280.dp), onClick = { p.onRecentChannelClick(c, null) }) }
        }
        val trending = s.topRatedMovies.ifEmpty { s.recentMovies }.take(12)
        LuRail(tr("Trending", "الأكثر رواجاً")) {
            if (trending.isEmpty()) repeat(7) { LuSkeleton(Modifier.width(170.dp).aspectRatio(2f / 3f)) }
            trending.forEachIndexed { i, m ->
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("${i + 1}", color = Color(0x33FFFFFF), fontSize = 96.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(end = 4.dp))
                    LuPoster(m.name, m.posterUrl, m.rating, m.year, luQuality(m.name), { p.onMovieClick(m) })
                }
            }
        }
        val rec = s.recommendedMovies.ifEmpty { s.recentMovies }.take(14)
        LuRail(tr("Recommended for you", "مقترح لك")) {
            if (rec.isEmpty()) repeat(7) { LuSkeleton(Modifier.width(170.dp).aspectRatio(2f / 3f)) }
            rec.forEach { m -> LuPoster(m.name, m.posterUrl, m.rating, m.year, luQuality(m.name), { p.onMovieClick(m) }) }
        }
        if (s.recentSeries.isNotEmpty()) LuRail(tr("New series", "مسلسلات جديدة")) {
            s.recentSeries.take(14).forEach { m -> LuPoster(m.name, m.posterUrl, m.rating, m.releaseDate?.take(4), null, { p.onSeriesClick(m) }) }
        }
        Spacer(Modifier.height(30.dp))
    }
}

private class LuHeroItem(val title: String, val image: String?, val year: String?, val rating: Float?, val genre: String?, val plot: String?, val kind: String, val open: () -> Unit)

// ───────────────────────────── LIVE TV (grid) ─────────────────────────────

/** 16:9 channel card: logo, name, current program, progress, quality, star. Grows on focus with program detail. */
@Composable
internal fun LuChannelCard(c: Channel, moving: Boolean, modifier: Modifier = Modifier, focusRequester: FocusRequester? = null, onFocus: () -> Unit = {}, onLongClick: (() -> Unit)? = null, onClick: () -> Unit) {
    var f by remember { mutableStateOf(false) }
    val pr = c.currentProgram
    LuFocus(onClick, modifier.aspectRatio(16f / 9f).let { if (focusRequester != null) it.focusRequester(focusRequester) else it }, shape = LU.R,
        color = if (moving) LU.PurpleSoft else LU.Card, focusedColor = Color(0xFF1E1730), scale = 1.1f, onLongClick = onLongClick,
        onFocus = { f = it; if (it) onFocus() }) { _ ->
        Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0x229B5CFF), Color.Transparent))))
        Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(56.dp, 40.dp).clip(RoundedCornerShape(8.dp)).background(Color(0x14FFFFFF)), contentAlignment = Alignment.Center) {
                    c.logoUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(4.dp)) } ?: Text(c.name.take(2), color = Color.White, fontWeight = FontWeight.Bold)
                }
                Column(Modifier.weight(1f)) {
                    Text(c.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (c.number > 0) Text("#${c.number}", color = LU.Faint, fontSize = 12.sp)
                }
                if (c.isFavorite) LuIcon(Icons.Outlined.Star, LU.Gold, 20.dp)
            }
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    LuTag(tr("LIVE", "مباشر"), LU.Live)
                    luQuality(c.name)?.let { LuTag(it, LU.Purple, filled = false) }
                    Text(pr?.title ?: tr("No guide info", "لا يوجد دليل"), color = LU.Sub, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                }
                if (f && pr != null) Text("${luTime(pr.startTime)} - ${luTime(pr.endTime)}" + (c.nextProgram?.let { "  •  ${tr("Next", "التالي")}: ${it.title}" } ?: ""), color = LU.Faint, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Box(Modifier.fillMaxWidth().height(4.dp).clip(CircleShape).background(Color(0x22FFFFFF))) {
                    Box(Modifier.fillMaxHeight().fillMaxWidth(luProgress(c) ?: 0f).background(LU.Grad))
                }
            }
        }
    }
}

@Composable
internal fun LuFreshLiveTv(p: LiveTvParams) {
    var quality by remember { mutableStateOf("ALL") }
    var favOnly by remember { mutableStateOf(false) }
    var grid by remember { mutableStateOf(true) }
    var focused by remember { mutableStateOf<Channel?>(null) }
    val shown = remember(p.channels, quality, favOnly) {
        p.channels.filter { c -> (!favOnly || c.isFavorite) && (quality == "ALL" || luQuality(c.name) == quality) }
    }
    val foc = focused ?: p.previewChannel ?: shown.firstOrNull()
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // category chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)) {
            items(p.categories, key = { it.id }) { cat ->
                LuChip(cat.name, cat.id == p.selectedCategoryId, Modifier.focusRequester(p.categoryRequester(cat.id)).onFocusChanged { if (it.isFocused) p.onCategoryFocused(cat) },
                    icon = if (p.isCategoryLocked(cat)) Icons.Outlined.Lock else null, count = cat.count.takeIf { it > 0 }) { p.onCategoryClick(cat) }
            }
        }
        // toolbar
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.width(360.dp).height(52.dp).clip(RoundedCornerShape(50)).background(Color(0x14FFFFFF)).padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LuIcon(Icons.Outlined.Search, LU.Sub, 22.dp)
                Box(Modifier.weight(1f)) {
                    if (p.channelSearchQuery.isEmpty()) Text(tr("Quick channel search", "بحث سريع عن قناة"), color = LU.Faint, fontSize = 16.sp)
                    BasicTextField(p.channelSearchQuery, p.onChannelSearchChange, singleLine = true, textStyle = TextStyle(color = Color.White, fontSize = 17.sp), cursorBrush = SolidColor(LU.Purple), modifier = Modifier.fillMaxWidth())
                }
            }
            listOf("ALL" to tr("All", "الكل"), "HD" to "HD", "FHD" to "FHD", "4K" to "4K").forEach { (k, l) -> LuChip(l, quality == k) { quality = k } }
            LuChip(tr("Favorites", "المفضلة"), favOnly, icon = Icons.Outlined.Star) { favOnly = !favOnly }
            Spacer(Modifier.weight(1f))
            LuRound(Icons.Outlined.GridView, active = grid) { grid = true }
            LuRound(Icons.Outlined.ViewList, active = !grid) { grid = false }
        }
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Column(Modifier.weight(1f).fillMaxHeight()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                    Box(Modifier.size(10.dp).clip(CircleShape).background(LU.Live))
                    Text(tr("On air now", "الآن يُبث"), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("${shown.size} ${tr("channels", "قناة")}", color = LU.Faint, fontSize = 15.sp)
                }
                when {
                    shown.isEmpty() && p.channels.isEmpty() -> LazyVerticalGrid(GridCells.Fixed(3), horizontalArrangement = Arrangement.spacedBy(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) { items(List(9) { it }) { LuSkeleton(Modifier.fillMaxWidth().aspectRatio(16f / 9f)) } }
                    shown.isEmpty() -> LuEmpty(Icons.Outlined.TvOff, tr("No channels match", "لا توجد قنوات مطابقة"), tr("Try another quality or category", "جرّب جودة أو قسماً آخر"))
                    grid -> LazyVerticalGrid(GridCells.Adaptive(180.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(12.dp)) {
                        items(shown, key = { it.id }) { c ->
                            LuChannelCard(c, p.movingChannelId == c.id, Modifier.fillMaxWidth(), p.channelRequester(c.id), onFocus = { focused = c; p.onChannelFocused(c) }, onLongClick = { p.onChannelLongClick(c) }) { p.onChannelClick(c) }
                        }
                    }
                    else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(8.dp)) {
                        items(shown, key = { it.id }) { c ->
                            LuFocus({ p.onChannelClick(c) }, Modifier.fillMaxWidth().height(78.dp).focusRequester(p.channelRequester(c.id)), shape = LU.RS, color = if (p.movingChannelId == c.id) LU.PurpleSoft else LU.Card, focusedColor = Color(0xFF1E1730), scale = 1.02f,
                                onLongClick = { p.onChannelLongClick(c) }, onFocus = { if (it) { focused = c; p.onChannelFocused(c) } }) {
                                Row(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Text(if (c.number > 0) "${c.number}" else "•", color = LU.Faint, fontSize = 16.sp, modifier = Modifier.width(40.dp))
                                    Box(Modifier.size(64.dp, 44.dp).clip(RoundedCornerShape(8.dp)).background(Color(0x14FFFFFF))) { c.logoUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(4.dp)) } }
                                    Text(c.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                    Text(c.currentProgram?.title.orEmpty(), color = LU.Sub, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                    luQuality(c.name)?.let { LuTag(it, LU.Purple, false) }
                                    if (c.isFavorite) LuIcon(Icons.Outlined.Star, LU.Gold, 20.dp)
                                }
                            }
                        }
                    }
                }
            }
            // focused preview + program info (glass)
            Column(Modifier.width(290.dp).fillMaxHeight().clip(LU.R).background(LU.Glass).border(1.dp, LU.Line, LU.R).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LuFocus({ foc?.let(p.onChannelClick) }, Modifier.fillMaxWidth().aspectRatio(16f / 9f).focusRequester(p.previewFocusRequester), shape = LU.RS, color = Color.Black, focusedColor = Color.Black, scale = 1.03f) {
                    val eng = p.previewPlayerEngine
                    if (eng != null && p.previewChannel != null) PlayerRenderView(eng, PlayerSurfaceResizeMode.FIT, Modifier.fillMaxSize())
                    else foc?.logoUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(40.dp)) }
                    if (p.isPreviewLoading) Text("…", color = Color.White, fontSize = 26.sp, modifier = Modifier.align(Alignment.Center))
                    Box(Modifier.align(Alignment.TopStart).padding(10.dp)) { LuTag(tr("LIVE", "مباشر"), LU.Live) }
                }
                Text(foc?.name ?: tr("Pick a channel", "اختر قناة"), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                foc?.currentProgram?.let { pr ->
                    Text(pr.title, color = Color(0xFFC5F5EF), fontSize = 18.sp, fontWeight = FontWeight.SemiBold, maxLines = 2)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(luTime(pr.startTime), color = LU.Sub, fontSize = 14.sp)
                        Box(Modifier.weight(1f).height(5.dp).clip(CircleShape).background(Color(0x22FFFFFF))) { Box(Modifier.fillMaxHeight().fillMaxWidth(luProgress(foc) ?: 0f).background(LU.Grad)) }
                        Text(luTime(pr.endTime), color = LU.Sub, fontSize = 14.sp)
                    }
                    if (pr.description.isNotBlank()) Text(pr.description, color = LU.Sub, fontSize = 14.sp, maxLines = 4, overflow = TextOverflow.Ellipsis, lineHeight = 20.sp)
                } ?: Text(tr("No program guide for this channel", "لا يوجد دليل برامج لهذه القناة"), color = LU.Faint, fontSize = 15.sp)
                foc?.nextProgram?.let { n -> Text("${tr("Next", "التالي")} • ${luTime(n.startTime)}  ${n.title}", color = LU.Faint, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                p.previewErrorMessage?.let { Text(it, color = LU.Live, fontSize = 13.sp, maxLines = 2) }
                Spacer(Modifier.weight(1f))
                LuButton(tr("Watch with guide", "شاهد مع الدليل"), Icons.Outlined.PlayArrow, { foc?.let(p.onChannelClick) }, Modifier.fillMaxWidth())
            }
        }
    }
}
