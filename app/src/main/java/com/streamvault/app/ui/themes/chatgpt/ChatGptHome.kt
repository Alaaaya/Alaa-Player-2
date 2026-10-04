package com.streamvault.app.ui.themes.chatgpt

import androidx.compose.animation.animateContentSize
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

private class CgDest(val route: String, val glyph: String, val en: String, val ar: String)

private val cgDestinations = listOf(
    CgDest(Routes.SEARCH, "⌕", "Search", "بحث"),
    CgDest(Routes.HOME, "⌂", "Home", "الرئيسية"),
    CgDest(Routes.LIVE_TV, "◉", "Live TV", "البث المباشر"),
    CgDest(Routes.EPG, "▦", "Guide", "الدليل"),
    CgDest(Routes.MOVIES, "▶", "Movies", "أفلام"),
    CgDest(Routes.SERIES, "❏", "Series", "مسلسلات"),
    CgDest(Routes.FAVORITES, "♥", "My List", "قائمتي"),
    CgDest(Routes.SETTINGS, "⚙", "Settings", "الإعدادات")
)

private fun onRoute(current: String, route: String) = current == route || current.startsWith("$route?") || current.startsWith("$route/")

/** Shell: ChatGPT look. Glossy navy sidebar on the START edge (right in Arabic) with logo, icon+label
 *  entries and a blue glowing pill for the active route; clock + wifi on the top bar of the content. */
@Composable
internal fun ChatGptShell(p: ShellParams) {
    CgBackground(p.modifier) {
        Row(Modifier.fillMaxSize()) {
            if (p.topBarVisible) {
                Column(
                    Modifier.width(210.dp).fillMaxHeight().background(Brush.verticalGradient(listOf(Color(0xFF0B1730), Color(0xFF050A16))))
                        .padding(horizontal = 14.dp, vertical = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(start = 6.dp, bottom = 18.dp)) {
                        Text("▶", color = CG.Blue, fontSize = 22.sp, fontWeight = FontWeight.Black)
                        Text("Alaa IPTV", color = CG.Text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    cgDestinations.forEach { d ->
                        val active = onRoute(p.currentRoute, d.route)
                        CgCard(onClick = { if (!active) p.onNavigate(d.route) }, container = if (active) CG.AmberDeep else Color.Transparent, modifier = Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                CgGlyph(d.glyph, 20.dp, tint = if (active) Color.White else CG.Sub)
                                Text(tr(d.en, d.ar), color = if (active) Color.White else CG.Sub, fontSize = 15.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal, maxLines = 1)
                            }
                        }
                    }
                }
                Box(Modifier.width(1.dp).fillMaxHeight().background(CG.Line))
            }
            Column(Modifier.weight(1f).fillMaxHeight().padding(p.contentPadding).padding(start = 28.dp, end = 28.dp, top = 18.dp)) {
                Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (p.showScreenHeader) Text(p.title, color = CG.Text, fontSize = 26.sp, fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.weight(1f))
                    else Spacer(Modifier.weight(1f))
                    p.topBarActions?.let { Row(horizontalArrangement = Arrangement.spacedBy(10.dp), content = it) }
                    Text("  ⌔  " + cgClock(System.currentTimeMillis()), color = CG.Sub, fontSize = 16.sp)
                }
                p.header?.let { Column(content = it) }
                Column(Modifier.fillMaxSize(), content = p.content)
            }
        }
    }
}

/** Home: big rounded hero carousel with "watch now", four glossy gradient tiles (Live blue, Movies crimson,
 *  Series purple, Favorites green), then continue-watching and content rows. */
@Composable
internal fun ChatGptDashboard(p: DashboardParams) {
    val s = p.uiState
    val first = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { first.requestFocus() } }
    val featureAction = {
        when (s.feature.actionType) {
            DashboardFeatureAction.LIVE -> p.onNavigate(Routes.LIVE_TV)
            DashboardFeatureAction.CONTINUE_WATCHING -> s.continueWatching.firstOrNull()?.let(p.onContinueWatchingItemClick) ?: p.onNavigate(Routes.MOVIES)
            else -> p.onNavigate(Routes.MOVIES)
        }
    }
    val heroes = remember(s.recommendedMovies, s.recentMovies) { (s.recommendedMovies + s.recentMovies).distinctBy { it.id }.filter { it.backdropUrl != null || it.posterUrl != null }.take(5) }
    var heroIdx by remember { mutableStateOf(0) }
    LaunchedEffect(heroes.size) { while (heroes.size > 1) { kotlinx.coroutines.delay(7000); heroIdx = (heroIdx + 1) % heroes.size } }
    val hero = heroes.getOrNull(heroIdx)
    val tFav = tr("Favorite channels", "القنوات المفضلة"); val tRecent = tr("Recent channels", "القنوات الأخيرة"); val tRec = tr("Recommended", "مقترح"); val tAdded = tr("Recently added", "أضيف حديثاً")
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(22.dp)) {
        item {
            CgCard(onClick = { hero?.let(p.onMovieClick) ?: featureAction() }, container = CG.Raised, zoom = 1.01f, modifier = Modifier.fillMaxWidth().height(260.dp).focusRequester(first)) {
                androidx.compose.animation.Crossfade(hero?.backdropUrl ?: hero?.posterUrl ?: s.feature.artworkUrl, label = "hero") { url ->
                    url?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                }
                Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.Transparent, CG.Bg.copy(alpha = 0.85f)))))
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, CG.Bg.copy(alpha = 0.9f)))))
                Column(Modifier.align(Alignment.BottomStart).padding(26.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(hero?.name ?: s.feature.title.ifBlank { "Alaa IPTV" }, color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(listOfNotNull(hero?.year, hero?.genre).joinToString("  •  ").ifBlank { s.feature.summary }, color = CG.Sub, fontSize = 14.sp, maxLines = 1)
                    Row(Modifier.background(Color.White, CG.RSmall).padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("▶", color = Color.Black, fontSize = 13.sp)
                        Text(tr("Watch now", "مشاهدة الآن"), color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (heroes.size > 1) Row(Modifier.align(Alignment.BottomEnd).padding(26.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    heroes.indices.forEach { i -> Box(Modifier.size(if (i == heroIdx) 18.dp else 7.dp, 7.dp).clip(CircleShape).background(if (i == heroIdx) Color.White else CG.Faint)) }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                listOf(
                    CgTile("📺", tr("Live", "مباشر"), if (s.stats.liveChannelCount > 0) tr("${s.stats.liveChannelCount} channels", "${s.stats.liveChannelCount} قناة") else tr("Watch live TV", "شاهد البث المباشر"), Routes.LIVE_TV, Color(0xFF1E5BFF), Color(0xFF0A2A8A)),
                    CgTile("🎞", tr("Movies", "أفلام"), tr("Latest movies", "أحدث الأفلام"), Routes.MOVIES, Color(0xFFE0284F), Color(0xFF6E0A26)),
                    CgTile("🎬", tr("Series", "مسلسلات"), tr("Top series", "أشهر المسلسلات"), Routes.SERIES, Color(0xFF8C3BFF), Color(0xFF3B0E8A)),
                    CgTile("☆", tr("Favorites", "المفضلة"), tr("My list", "قائمتي الخاصة"), Routes.FAVORITES, Color(0xFF14B88A), Color(0xFF07513F))
                ).forEach { t ->
                    CgCard(onClick = { p.onNavigate(t.route) }, container = Color.Transparent, modifier = Modifier.weight(1f).height(130.dp)) {
                        Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(t.a, t.b))))
                        Box(Modifier.fillMaxWidth().height(50.dp).background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.18f), Color.Transparent))))
                        Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                            CgGlyph(t.glyph, 34.dp, tint = Color.White)
                            Spacer(Modifier.height(6.dp))
                            Text(t.title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text(t.sub, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, maxLines = 1)
                        }
                    }
                }
            }
        }
        if (s.continueWatching.isNotEmpty()) item {
            Column {
                CgRowTitle(tr("Continue watching", "تابع المشاهدة"))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(s.continueWatching, key = { "cw${it.id}" }) { h ->
                        val prog = if (h.totalDurationMs > 0) h.resumePositionMs.toFloat() / h.totalDurationMs else null
                        CgPoster(h.title, h.posterUrl, prog?.let { "${(it * 100).toInt()}%" }, { p.onContinueWatchingItemClick(h) }, Modifier.width(140.dp))
                    }
                }
            }
        }
        listOf(tFav to s.favoriteChannels, tRecent to s.recentChannels).forEach { (title, channels) ->
            if (channels.isNotEmpty()) item {
                Column {
                    CgRowTitle(title)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(channels, key = { "$title${it.id}" }) { c -> CgChannelPlate(c, c.id in p.recordingChannelIds) { (if (channels === s.favoriteChannels) p.onFavoriteChannelClick else p.onRecentChannelClick)(c, s.currentCombinedProfileId) } }
                    }
                }
            }
        }
        listOf(tRec to s.recommendedMovies, tAdded to s.recentMovies).forEach { (title, movies) ->
            if (movies.isNotEmpty()) item {
                Column {
                    CgRowTitle(title)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) { items(movies, key = { "$title${it.id}" }) { m -> CgPoster(m.name, m.posterUrl, m.year, { p.onMovieClick(m) }, Modifier.width(130.dp)) } }
                }
            }
        }
        if (s.recentSeries.isNotEmpty()) item {
            Column {
                CgRowTitle(tr("New series", "مسلسلات جديدة"))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) { items(s.recentSeries, key = { "rs${it.id}" }) { m -> CgPoster(m.name, m.posterUrl, m.genre, { p.onSeriesClick(m) }, Modifier.width(130.dp)) } }
            }
        }
    }
}

private class CgTile(val glyph: String, val title: String, val sub: String, val route: String, val a: Color, val b: Color)

/** Compact horizontal plate: framed logo + number/name, like a receiver preset button. */
@Composable
private fun CgChannelPlate(c: Channel, recording: Boolean, onClick: () -> Unit) {
    CgCard(onClick = onClick, container = CG.Raised, modifier = Modifier.width(230.dp)) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CgLogo(c.name, c.logoUrl, 44.dp)
            Column(Modifier.weight(1f)) {
                Text((if (c.number > 0) "${c.number}  " else "") + c.name, color = CG.Text, fontSize = 13.sp, fontFamily = CG.Serif, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(c.currentProgram?.title ?: "—", color = CG.Faint, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (recording) CgBadge("REC", CG.Live, filled = true)
        }
    }
}

/** Live TV: category sidebar with counts and a blue glowing selected pill, then a 4-column grid of
 *  channel logo cards (logo, name, HD badge, heart). Right of the categories in RTL, as in the reference. */
@Composable
internal fun ChatGptLiveTv(p: LiveTvParams) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        Column(Modifier.width(250.dp).fillMaxHeight().background(CG.Raised.copy(alpha = 0.7f), CG.R).padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            CgSearchField(p.categorySearchQuery, p.onCategorySearchChange, tr("Filter", "تصفية"), Modifier.fillMaxWidth())
            LazyColumn(verticalArrangement = Arrangement.spacedBy(3.dp), contentPadding = PaddingValues(vertical = 6.dp)) {
                items(p.categories, key = { it.id }) { cat ->
                    val sel = cat.id == p.selectedCategoryId
                    CgCard(
                        onClick = { p.onCategoryClick(cat) }, onLongClick = { p.onCategoryLongClick(cat) }, shape = CG.RSmall,
                        container = if (sel) CG.AmberDeep else Color.Transparent,
                        modifier = Modifier.fillMaxWidth().focusRequester(p.categoryRequester(cat.id)).onFocusChanged { if (it.isFocused) p.onCategoryFocused(cat) }
                            .cgRight { p.onRequestChannelsFromCategory() }
                    ) {
                        Row(Modifier.padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                            CgCategoryGlyph(cat.name, p.isCategoryLocked(cat), if (sel) Color.White else CG.Blue, 18.dp)
                            Spacer(Modifier.width(10.dp))
                            Text(cat.name, color = if (sel) Color.White else CG.Sub, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            if (cat.count > 0) Text("${cat.count}", color = if (sel) Color.White else CG.Faint, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(p.sourceTitle.ifBlank { tr("Channels", "القنوات") } + "  ·  ${p.channels.size}", color = CG.Text, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                CgSearchField(p.channelSearchQuery, p.onChannelSearchChange, tr("Find", "بحث"), Modifier.width(240.dp))
            }
            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(6.dp)
            ) {
                items(p.channels.size, key = { p.channels[it].id }) { i ->
                    val c = p.channels[i]
                    val locked = p.isChannelLocked(c)
                    val moving = c.id == p.movingChannelId
                    CgCard(
                        onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c) },
                        container = if (moving) CG.AmberDeep.copy(alpha = 0.6f) else CG.Card,
                        modifier = Modifier.fillMaxWidth().aspectRatio(1.15f).focusRequester(p.channelRequester(c.id)).onFocusChanged { if (it.isFocused) p.onChannelFocused(c) }
                    ) {
                        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.06f), Color.Transparent))))
                        Column(Modifier.fillMaxSize().padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                if (!locked && c.logoUrl != null) AsyncImage(c.logoUrl, c.name, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize(0.75f))
                                else Text(if (locked) "🔒" else c.name.take(3).uppercase(), color = CG.Blue, fontSize = 22.sp, fontWeight = FontWeight.Black)
                            }
                            Text((if (moving) "⇅ " else "") + c.name, color = CG.Text, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Row(Modifier.align(Alignment.TopEnd).padding(6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (!locked) c.qualityBadge()?.let { CgBadge(it, Color.White, filled = false) }
                        }
                        if (c.isFavorite) CgGlyph("♥", 16.dp, tint = Color(0xFFFF4D7A), modifier = Modifier.align(Alignment.TopStart).padding(6.dp))
                        if (c.number > 0) Text("${c.number}", color = CG.Sub, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.BottomStart).padding(horizontal = 8.dp, vertical = 6.dp).background(Color.Black.copy(alpha = 0.35f), CG.RSmall).padding(horizontal = 6.dp, vertical = 1.dp))
                    }
                }
            }
        }
    }
}
