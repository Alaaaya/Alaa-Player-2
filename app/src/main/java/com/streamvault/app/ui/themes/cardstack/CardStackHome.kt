package com.streamvault.app.ui.themes.cardstack

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.zIndex
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

/** CardStack's own rounded search field: magnifier inside a soft pill, white outline on focus. */
@Composable
internal fun CsSearchField(value: String, onChange: (String) -> Unit, hint: String, modifier: Modifier = Modifier, onSubmit: (() -> Unit)? = null) {
    var focused by remember { mutableStateOf(false) }
    Row(
        modifier.clip(CS.Pill).background(if (focused) CS.Card else CS.Raised).border(2.dp, if (focused) CS.Text else Color.Transparent, CS.Pill)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("⌕", color = if (focused) CS.Amber else CS.Faint, fontSize = 16.sp)
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) Text(hint, color = CS.Faint, fontSize = 14.sp, maxLines = 1)
            BasicTextField(
                value = value, onValueChange = onChange, singleLine = true,
                textStyle = TextStyle(color = CS.Text, fontSize = 14.sp), cursorBrush = SolidColor(CS.Amber),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { onSubmit?.invoke() }, onDone = { onSubmit?.invoke() }),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                modifier = Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused }
            )
        }
    }
}

private class CsDest(val route: String, val glyph: String, val en: String, val ar: String)

private val csDestinations = listOf(
    CsDest(Routes.SEARCH, "⌕", "Search", "بحث"),
    CsDest(Routes.HOME, "⌂", "Home", "الرئيسية"),
    CsDest(Routes.LIVE_TV, "◉", "Live TV", "البث المباشر"),
    CsDest(Routes.EPG, "▦", "Guide", "الدليل"),
    CsDest(Routes.MOVIES, "▶", "Movies", "أفلام"),
    CsDest(Routes.SERIES, "❏", "Series", "مسلسلات"),
    CsDest(Routes.FAVORITES, "♥", "My List", "قائمتي"),
    CsDest(Routes.SETTINGS, "⚙", "Settings", "الإعدادات")
)

private fun onRoute(current: String, route: String) = current == route || current.startsWith("$route?") || current.startsWith("$route/")

/** Shell: a "deck" of nav cards stacked on the END edge. Collapsed it shows only the top card (current section)
 *  with the rest peeking as slivers; with focus the deck fans open into full labelled cards. Header sits in a tilted title card. */
@Composable
internal fun CardStackShell(p: ShellParams) {
    CsBackground(p.modifier) {
        Row(Modifier.fillMaxSize()) {
            Column(Modifier.weight(1f).fillMaxHeight().padding(p.contentPadding).padding(start = 40.dp, end = 20.dp, top = 26.dp)) {
                if (p.showScreenHeader || p.topBarActions != null) {
                    Row(Modifier.fillMaxWidth().padding(bottom = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (p.showScreenHeader) Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Box(Modifier.size(width = 8.dp, height = 40.dp).clip(CS.Pill).background(Brush.verticalGradient(listOf(CS.Amber, CS.AmberDeep))))
                            Column {
                                Text(p.title, color = CS.Text, fontSize = 30.sp, fontWeight = FontWeight.Black, maxLines = 1)
                                p.subtitle?.takeIf { it.isNotBlank() }?.let { Text(it, color = CS.Sub, fontSize = 14.sp, maxLines = 1) }
                            }
                        } else Spacer(Modifier.weight(1f))
                        p.topBarActions?.let { Row(horizontalArrangement = Arrangement.spacedBy(10.dp), content = it) }
                    }
                }
                p.header?.let { Column(content = it) }
                Column(Modifier.fillMaxSize(), content = p.content)
            }
            if (p.topBarVisible) {
                var open by remember { mutableStateOf(false) }
                Column(
                    Modifier.fillMaxHeight().animateContentSize().background(Brush.horizontalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))))
                        .onFocusChanged { open = it.hasFocus }.padding(horizontal = 16.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(if (open) 10.dp else (-26).dp, Alignment.CenterVertically)
                ) {
                    csDestinations.forEachIndexed { i, d ->
                        val active = onRoute(p.currentRoute, d.route)
                        CsCard(
                            onClick = { if (!active) p.onNavigate(d.route) }, shape = CS.RSmall, zoom = 1.06f,
                            container = if (active) CS.Amber else CS.Raised, focusedContainer = if (active) CS.Amber else CS.Card,
                            modifier = Modifier.width(if (open) 190.dp else 64.dp).height(52.dp).zIndex(if (active) 100f else i.toFloat())
                        ) {
                            Row(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(d.glyph, color = if (active) CS.Bg else CS.Amber, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                if (open) Text(tr(d.en, d.ar), color = if (active) CS.Bg else CS.Text, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                    }
                    if (open) Text(csClock(System.currentTimeMillis()), color = CS.Sub, fontSize = 13.sp, modifier = Modifier.padding(top = 12.dp))
                }
            }
        }
    }
}

/** Home: a fanned "hand" of featured cards (center card big, neighbours tilted behind), then stacked shelves:
 *  continue watching as overlapping index cards, channels as a deck strip, movies as poster stacks. */
@Composable
internal fun CardStackDashboard(p: DashboardParams) {
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
    val tCont = tr("Pick up where you left", "أكمل من حيث توقفت")
    val tYour = tr("Your deck of channels", "مجموعة قنواتك")
    val tRecentCh = tr("Recently played", "شغلت مؤخراً")
    val tRec = tr("Dealt for you", "اخترنا لك")
    val tTop = tr("Top rated", "الأعلى تقييماً")
    val tNew = tr("Fresh cards", "جديد")
    val fan = (s.recommendedMovies + s.recentMovies).distinctBy { it.id }.take(4)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 56.dp), verticalArrangement = Arrangement.spacedBy(30.dp)) {
        item {
            Row(Modifier.fillMaxWidth().height(360.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(36.dp)) {
                Box(Modifier.width(560.dp).fillMaxHeight()) {
                    fan.drop(1).take(3).forEachIndexed { i, m ->
                        Box(Modifier.align(Alignment.CenterStart).padding(start = (330 + i * 70).dp).width(170.dp).height(250.dp)
                            .graphicsLayer { rotationZ = 6f + i * 5f }.clip(CS.R).background(CS.Card)) {
                            m.posterUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                            Box(Modifier.fillMaxSize().background(CS.Bg.copy(alpha = 0.35f + i * 0.15f)))
                        }
                    }
                    CsCard(featureAction, Modifier.align(Alignment.CenterStart).width(420.dp).height(320.dp).focusRequester(first).graphicsLayer { rotationZ = -2f }, container = CS.Card) {
                        s.feature.artworkUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, CS.Bg.copy(alpha = 0.95f)))))
                        Column(Modifier.align(Alignment.BottomStart).padding(22.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            CsBadge(tr("TOP CARD", "البطاقة الأولى"), CS.Amber, filled = true)
                            Text(s.feature.title.ifBlank { s.provider?.name ?: "Card Stack" }, color = CS.Text, fontSize = 28.sp, fontWeight = FontWeight.Black, maxLines = 2)
                            Text("▶  " + s.feature.actionLabel.ifBlank { tr("Play", "تشغيل") }, color = CS.Amber, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(s.provider?.name ?: "Card Stack", color = CS.Sub, fontSize = 14.sp)
                    Text(s.feature.summary.ifBlank { tr("Shuffle through live TV, films and series.", "تنقل بين البث والأفلام والمسلسلات.") }, color = CS.Text, fontSize = 18.sp, maxLines = 4, overflow = TextOverflow.Ellipsis)
                    listOf(
                        Triple("◉", "${s.stats.liveChannelCount} " + tr("channels", "قناة"), Routes.LIVE_TV),
                        Triple("▶", "${s.stats.movieLibraryCount} " + tr("movies", "فيلم"), Routes.MOVIES),
                        Triple("❏", "${s.stats.seriesLibraryCount} " + tr("series", "مسلسل"), Routes.SERIES),
                        Triple("▦", tr("TV guide", "دليل البرامج"), Routes.EPG)
                    ).forEach { (g, l, r) ->
                        CsCard({ p.onNavigate(r) }, Modifier.fillMaxWidth(0.8f).height(48.dp), shape = CS.RSmall, container = CS.Raised) {
                            Row(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(g, color = CS.Amber, fontSize = 16.sp); Text(l, color = CS.Text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
        if (s.continueWatching.isNotEmpty()) item {
            Column {
                CsRowTitle(tCont, trailing = "${s.continueWatching.size}")
                LazyRow(horizontalArrangement = Arrangement.spacedBy((-40).dp), contentPadding = PaddingValues(vertical = 16.dp, horizontal = 4.dp)) {
                    itemsIndexed(s.continueWatching, key = { _, h -> "cw${h.id}" }) { i, h ->
                        val prog = if (h.totalDurationMs > 0) h.resumePositionMs.toFloat() / h.totalDurationMs else 0f
                        CsCard({ p.onContinueWatchingItemClick(h) }, Modifier.width(300.dp).height(170.dp).zIndex(-i.toFloat()), zoom = 1.08f) {
                            h.posterUrl?.let { AsyncImage(it, h.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, CS.Bg))))
                            Column(Modifier.align(Alignment.BottomStart).padding(14.dp).padding(end = 40.dp)) {
                                Text(h.title, color = CS.Text, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${(prog * 100).toInt()}%", color = CS.Amber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                CsProgress(prog, Modifier.padding(top = 4.dp), 3.dp)
                            }
                        }
                    }
                }
            }
        }
        listOf(Triple(tYour, s.favoriteChannels, p.onFavoriteChannelClick), Triple(tRecentCh, s.recentChannels, p.onRecentChannelClick)).forEach { (title, channels, click) ->
            if (channels.isNotEmpty()) item {
                Column {
                    CsRowTitle(title, trailing = "${channels.size}")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(vertical = 14.dp, horizontal = 4.dp)) {
                        items(channels, key = { "$title${it.id}" }) { c -> CsChannelTile(c, c.id in p.recordingChannelIds) { click(c, s.currentCombinedProfileId) } }
                    }
                }
            }
        }
        listOf(tRec to s.recommendedMovies, tTop to s.topRatedMovies, tNew to s.recentMovies).forEach { (title, movies) ->
            if (movies.isNotEmpty()) item {
                Column {
                    CsRowTitle(title)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(vertical = 14.dp, horizontal = 4.dp)) {
                        items(movies, key = { "$title${it.id}" }) { m -> CsPoster(m.name, m.posterUrl, m.year, { p.onMovieClick(m) }, Modifier.width(150.dp)) }
                    }
                }
            }
        }
        if (s.recentSeries.isNotEmpty()) item {
            Column {
                CsRowTitle(tr("New series", "مسلسلات جديدة"))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(vertical = 14.dp, horizontal = 4.dp)) {
                    items(s.recentSeries, key = { "rs${it.id}" }) { m -> CsPoster(m.name, m.posterUrl, m.genre, { p.onSeriesClick(m) }, Modifier.width(150.dp)) }
                }
            }
        }
    }
}

/** Playing-card channel tile: number in the corner like a card index, logo in the middle, programme at the foot. */
@Composable
private fun CsChannelTile(c: Channel, recording: Boolean, onClick: () -> Unit) {
    CsCard(onClick, Modifier.width(150.dp).height(200.dp), container = CS.Raised) {
        Text(if (c.number > 0) "${c.number}" else "◆", color = CS.Amber, fontSize = 16.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.TopStart).padding(12.dp))
        Text(if (c.number > 0) "${c.number}" else "◆", color = CS.Amber.copy(alpha = 0.5f), fontSize = 16.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp).graphicsLayer { rotationZ = 180f })
        Column(Modifier.align(Alignment.Center).padding(horizontal = 12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CsLogo(c.name, c.logoUrl, 64.dp)
            Text(c.name, color = CS.Text, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(c.currentProgram?.title ?: "", color = CS.Faint, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (recording) CsBadge("REC", CS.Live, filled = true)
                c.qualityBadge()?.let { CsBadge(it, CS.Blue) }
            }
        }
    }
}

/** Live TV: three columns. Categories as a vertical stack of tab-cards, channels as a column of overlapping
 *  playing cards (focused one slides out), and a tall preview card with now/next on the end. */
@Composable
internal fun CardStackLiveTv(p: LiveTvParams) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(22.dp)) {
        Column(Modifier.width(230.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(tr("Groups", "المجموعات"), color = CS.Amber, fontSize = 13.sp, fontWeight = FontWeight.Black)
            CsSearchField(p.categorySearchQuery, p.onCategorySearchChange, tr("Filter", "تصفية"), Modifier.fillMaxWidth())
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 6.dp, horizontal = 4.dp)) {
                items(p.categories, key = { it.id }) { cat ->
                    val sel = cat.id == p.selectedCategoryId
                    CsCard(
                        onClick = { p.onCategoryClick(cat) }, onLongClick = { p.onCategoryLongClick(cat) }, shape = CS.RSmall,
                        container = if (sel) CS.Amber else CS.Raised, focusedContainer = if (sel) CS.Amber else CS.Card,
                        modifier = Modifier.fillMaxWidth().height(46.dp).focusRequester(p.categoryRequester(cat.id)).onFocusChanged { if (it.isFocused) p.onCategoryFocused(cat) }
                            .csRight { p.onRequestChannelsFromCategory() }
                    ) {
                        Row(Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text((if (p.isCategoryLocked(cat)) "🔒 " else "") + cat.name, color = if (sel) CS.Bg else CS.Text, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            if (cat.count > 0) Text("${cat.count}", color = if (sel) CS.Bg else CS.Faint, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${p.channels.size} " + tr("cards", "قناة"), color = CS.Sub, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                CsSearchField(p.channelSearchQuery, p.onChannelSearchChange, tr("Find a channel", "ابحث عن قناة"), Modifier.width(260.dp))
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 6.dp, horizontal = 8.dp)) {
                items(p.channels, key = { it.id }) { c ->
                    val locked = p.isChannelLocked(c)
                    val cur = c.id == p.previewChannel?.id
                    CsCard(
                        onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c) },
                        container = if (cur) Color(0xFF3A2A40) else CS.Raised,
                        modifier = Modifier.fillMaxWidth().height(70.dp).focusRequester(p.channelRequester(c.id)).onFocusChanged { if (it.isFocused) p.onChannelFocused(c) }
                            .csRight { p.onRequestPreviewFromChannel() }
                    ) {
                        Row(Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.size(40.dp).clip(CS.RSmall).background(if (cur) CS.Amber else CS.Line), contentAlignment = Alignment.Center) {
                                Text(if (c.number > 0) "${c.number}" else "·", color = if (cur) CS.Bg else CS.Text, fontSize = 14.sp, fontWeight = FontWeight.Black)
                            }
                            CsLogo(c.name, if (locked) null else c.logoUrl, 44.dp)
                            Column(Modifier.weight(1f)) {
                                Text(if (c.id == p.movingChannelId) "⇅  ${c.name}" else c.name, color = CS.Text, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(if (locked) tr("Locked", "مقفل") else c.currentProgram?.title ?: tr("No guide data", "لا يوجد دليل"), color = CS.Faint, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            if (!locked) c.qualityBadge()?.let { CsBadge(it, CS.Blue) }
                            if (c.catchUpSupported) CsBadge("⟲", CS.Blue)
                            Text(if (c.isFavorite) "★" else "☆", color = if (c.isFavorite) CS.Amber else CS.Faint, fontSize = 18.sp)
                        }
                    }
                }
            }
        }
        val c = p.previewChannel
        Column(Modifier.width(360.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            CsCard(
                onClick = { c?.let(p.onChannelClick) }, container = Color.Black,
                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).focusRequester(p.previewFocusRequester).csLeft { p.onRequestChannelsFromPreview() }
            ) {
                val engine = p.previewPlayerEngine
                if (engine != null && c != null) PlayerRenderView(engine, PlayerSurfaceResizeMode.FIT, Modifier.fillMaxSize())
                when {
                    p.previewErrorMessage != null -> Text(p.previewErrorMessage, color = CS.Live, fontSize = 13.sp, modifier = Modifier.align(Alignment.Center).padding(16.dp))
                    p.isPreviewLoading -> Text(tr("Loading…", "جار التحميل…"), color = CS.Sub, modifier = Modifier.align(Alignment.Center))
                    c == null -> Text(tr("Pick a card", "اختر قناة"), color = CS.Faint, modifier = Modifier.align(Alignment.Center))
                }
                if (c != null) Box(Modifier.align(Alignment.TopStart).padding(10.dp)) { CsBadge("● LIVE", CS.Live, filled = true) }
            }
            if (c != null) Column(Modifier.fillMaxWidth().clip(CS.R).background(CS.Raised).padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(c.name, color = CS.Text, fontSize = 20.sp, fontWeight = FontWeight.Black, maxLines = 1)
                c.currentProgram?.let { now ->
                    Text(tr("NOW", "الآن") + "  " + now.title, color = CS.Amber, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    CsProgress((System.currentTimeMillis() - now.startTime).toFloat() / (now.endTime - now.startTime).coerceAtLeast(1))
                    Text("${csClock(now.startTime)} – ${csClock(now.endTime)}", color = CS.Faint, fontSize = 12.sp)
                } ?: Text(tr("No guide data", "لا يوجد دليل"), color = CS.Faint)
                c.nextProgram?.let { Text(tr("NEXT", "التالي") + "  ${csClock(it.startTime)}  ${it.title}", color = CS.Sub, fontSize = 13.sp, maxLines = 1) }
            }
        }
    }
}
