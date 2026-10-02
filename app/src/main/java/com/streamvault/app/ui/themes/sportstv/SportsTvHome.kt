package com.streamvault.app.ui.themes.sportstv

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

/** Sports TV search field: slanted navy plate, lime frame on focus. */
@Composable
internal fun StSearchField(value: String, onChange: (String) -> Unit, hint: String, modifier: Modifier = Modifier, onSubmit: (() -> Unit)? = null) {
    var focused by remember { mutableStateOf(false) }
    Row(
        modifier.clip(ST.RSmall).background(if (focused) ST.Line else ST.Raised).border(2.dp, if (focused) ST.Amber else Color.Transparent, ST.RSmall)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("⌕", color = if (focused) ST.Amber else ST.Faint, fontSize = 16.sp)
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) Text(hint, color = ST.Faint, fontSize = 14.sp, maxLines = 1)
            BasicTextField(
                value = value, onValueChange = onChange, singleLine = true,
                textStyle = TextStyle(color = ST.Text, fontSize = 14.sp), cursorBrush = SolidColor(ST.Amber),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { onSubmit?.invoke() }, onDone = { onSubmit?.invoke() }),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                modifier = Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused }
            )
        }
    }
}

private class StDest(val route: String, val glyph: String, val en: String, val ar: String)

private val stDestinations = listOf(
    StDest(Routes.HOME, "⌂", "Home", "الرئيسية"),
    StDest(Routes.LIVE_TV, "◉", "Live", "مباشر"),
    StDest(Routes.EPG, "▦", "Guide", "الدليل"),
    StDest(Routes.MOVIES, "▶", "Movies", "أفلام"),
    StDest(Routes.SERIES, "❏", "Series", "مسلسلات"),
    StDest(Routes.FAVORITES, "★", "My List", "قائمتي"),
    StDest(Routes.SEARCH, "⌕", "Search", "بحث"),
    StDest(Routes.SETTINGS, "⚙", "Settings", "الإعدادات")
)

private fun onRoute(current: String, route: String) = current == route || current.startsWith("$route?") || current.startsWith("$route/")

/** Shell: broadcast top bar. Lime "score bug" logo plate on the start, uppercase menu with underline bars,
 *  clock bug on the end; the screen title sits in a slanted lower-third plate. */
@Composable
internal fun SportsTvShell(p: ShellParams) {
    StBackground(p.modifier) {
        Column(Modifier.fillMaxSize()) {
            if (p.topBarVisible) {
                Row(
                    Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Color(0xFF071119), ST.Bg.copy(alpha = 0.6f))))
                        .padding(start = 36.dp, end = 36.dp, top = 18.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(Modifier.clip(ST.Plate).background(ST.Amber).padding(horizontal = 14.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("ALAA", color = ST.Bg, fontSize = 18.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        Text(" SPORT", color = ST.Bg.copy(alpha = 0.7f), fontSize = 18.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.width(26.dp))
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                        stDestinations.forEach { d ->
                            val active = onRoute(p.currentRoute, d.route)
                            StTab(tr(d.en, d.ar), active, { if (!active) p.onNavigate(d.route) })
                        }
                    }
                    Row(Modifier.clip(ST.RSmall).background(ST.Raised), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.background(ST.Live).padding(horizontal = 8.dp, vertical = 6.dp)) { Text("● LIVE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black) }
                        Text(stClock(System.currentTimeMillis()), color = ST.Text, fontSize = 15.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 12.dp))
                    }
                }
                Box(Modifier.fillMaxWidth().height(2.dp).background(Brush.horizontalGradient(listOf(ST.Amber, ST.Line, Color.Transparent))))
            }
            Column(Modifier.weight(1f).fillMaxWidth().padding(p.contentPadding).padding(start = 36.dp, end = 36.dp, top = 16.dp)) {
                if (p.showScreenHeader || p.topBarActions != null) {
                    Row(Modifier.fillMaxWidth().padding(bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (p.showScreenHeader) Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.clip(ST.Plate).background(ST.Amber).padding(horizontal = 16.dp, vertical = 6.dp)) {
                                Text(p.title.uppercase(), color = ST.Bg, fontSize = 20.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, maxLines = 1)
                            }
                            p.subtitle?.takeIf { it.isNotBlank() }?.let {
                                Box(Modifier.background(ST.Raised).padding(horizontal = 14.dp, vertical = 8.dp)) { Text(it.uppercase(), color = ST.Sub, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                            }
                        } else Spacer(Modifier.weight(1f))
                        p.topBarActions?.let { Row(horizontalArrangement = Arrangement.spacedBy(10.dp), content = it) }
                    }
                }
                p.header?.let { Column(content = it) }
                Column(Modifier.fillMaxSize(), content = p.content)
            }
        }
    }
}

/** Scoreboard stat box: big number over a small uppercase label. */
@Composable
private fun StStat(value: Int, label: String, modifier: Modifier = Modifier) {
    Column(modifier.clip(ST.RSmall).background(ST.Bg.copy(alpha = 0.85f)).padding(horizontal = 14.dp, vertical = 8.dp)) {
        Text("$value", color = ST.Amber, fontSize = 24.sp, fontWeight = FontWeight.Black)
        Text(label.uppercase(), color = ST.Sub, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}

/** Home "match centre": hero plate with a diagonal lime band + stat scoreboard on the start, a LIVE NOW
 *  scoreboard column on the end; below, replay plates and fixture-style poster shelves. */
@Composable
internal fun SportsTvDashboard(p: DashboardParams) {
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
    val tRec = tr("Recommended", "مقترح لك")
    val tTop = tr("Top rated", "الأعلى تقييماً")
    val tNew = tr("New arrivals", "وصل حديثاً")
    val liveNow = (s.favoriteChannels + s.recentChannels).distinctBy { it.id }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 48.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        item {
            Row(Modifier.fillMaxWidth().height(360.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(Modifier.weight(1f).fillMaxHeight().clip(ST.R).background(ST.Raised)) {
                    s.feature.artworkUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                    Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(ST.Bg, ST.Bg.copy(alpha = 0.7f), Color.Transparent))))
                    Box(Modifier.align(Alignment.TopEnd).padding(top = 0.dp).width(140.dp).height(12.dp).background(ST.Amber))
                    Column(Modifier.align(Alignment.BottomStart).padding(28.dp).widthIn(max = 560.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(0.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.background(ST.Live).padding(horizontal = 8.dp, vertical = 4.dp)) { Text(tr("FEATURED", "مميز"), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black) }
                            s.provider?.name?.let { Box(Modifier.background(ST.Bg).padding(horizontal = 8.dp, vertical = 4.dp)) { Text(it.uppercase(), color = ST.Sub, fontSize = 11.sp, fontWeight = FontWeight.Bold) } }
                        }
                        Text(s.feature.title.ifBlank { s.provider?.name ?: "Sports TV" }.uppercase(), color = ST.Text, fontSize = 38.sp, fontWeight = FontWeight.Black, maxLines = 2, lineHeight = 40.sp)
                        Text(s.feature.summary, color = ST.Sub, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StButton(s.feature.actionLabel.ifBlank { tr("Watch now", "شاهد الآن") }, featureAction, Modifier.focusRequester(first), primary = true, icon = "▶")
                            StButton(tr("Live", "مباشر"), { p.onNavigate(Routes.LIVE_TV) }, icon = "◉")
                            StButton(tr("Guide", "الدليل"), { p.onNavigate(Routes.EPG) }, icon = "▦")
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StStat(s.stats.liveChannelCount, tr("channels", "قناة"))
                            StStat(s.stats.movieLibraryCount, tr("movies", "فيلم"))
                            StStat(s.stats.seriesLibraryCount, tr("series", "مسلسل"))
                        }
                    }
                }
                Column(Modifier.width(380.dp).fillMaxHeight().clip(ST.R).background(ST.Card)) {
                    Row(Modifier.fillMaxWidth().background(ST.Live).padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("● " + tr("LIVE NOW", "مباشر الآن"), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                        Text("${liveNow.size}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
                    }
                    if (liveNow.isEmpty()) StEmpty(tr("Watch or favourite channels to see them here", "شاهد أو أضف قنوات للمفضلة لتظهر هنا"))
                    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(liveNow, key = { "ln${it.id}" }) { c ->
                            val fav = s.favoriteChannels.any { it.id == c.id }
                            StScoreRow(c, c.id in p.recordingChannelIds) { if (fav) p.onFavoriteChannelClick(c, s.currentCombinedProfileId) else p.onRecentChannelClick(c, s.currentCombinedProfileId) }
                        }
                    }
                }
            }
        }
        if (s.continueWatching.isNotEmpty()) item {
            Column {
                StRowTitle(tr("Continue watching", "متابعة المشاهدة"), trailing = "${s.continueWatching.size}")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 6.dp)) {
                    items(s.continueWatching, key = { "cw${it.id}" }) { h ->
                        val prog = if (h.totalDurationMs > 0) h.resumePositionMs.toFloat() / h.totalDurationMs else null
                        val left = if (h.totalDurationMs > 0) stDuration(h.totalDurationMs - h.resumePositionMs) + " " + tr("left", "متبقي") else null
                        StWide(h.title, h.posterUrl, left, prog, { p.onContinueWatchingItemClick(h) }, Modifier.width(340.dp))
                    }
                }
            }
        }
        listOf(tRec to s.recommendedMovies, tTop to s.topRatedMovies, tNew to s.recentMovies).forEach { (title, movies) ->
            if (movies.isNotEmpty()) item {
                Column {
                    StRowTitle(title, trailing = "${movies.size}")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 6.dp)) {
                        items(movies, key = { "$title${it.id}" }) { m -> StPoster(m.name, m.posterUrl, m.year, { p.onMovieClick(m) }, Modifier.width(140.dp)) }
                    }
                }
            }
        }
        if (s.recentSeries.isNotEmpty()) item {
            Column {
                StRowTitle(tr("New series", "مسلسلات جديدة"))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 6.dp)) {
                    items(s.recentSeries, key = { "rs${it.id}" }) { m -> StPoster(m.name, m.posterUrl, m.genre, { p.onSeriesClick(m) }, Modifier.width(140.dp)) }
                }
            }
        }
    }
}

/** Scoreboard row: lime number block, logo, name + now title, match-clock minute on the end. */
@Composable
private fun StScoreRow(c: Channel, recording: Boolean, onClick: () -> Unit) {
    val now = c.currentProgram
    val t = System.currentTimeMillis()
    StCard(onClick = onClick, shape = ST.RSmall, container = ST.Raised, focusedContainer = ST.Line, zoom = 1.02f, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.height(54.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(40.dp).fillMaxHeight().background(ST.Amber), contentAlignment = Alignment.Center) {
                Text(if (c.number > 0) "${c.number}" else "•", color = ST.Bg, fontSize = 13.sp, fontWeight = FontWeight.Black)
            }
            StLogo(c.name, c.logoUrl, 36.dp, Modifier.padding(horizontal = 8.dp))
            Column(Modifier.weight(1f)) {
                Text(c.name.uppercase(), color = ST.Text, fontSize = 12.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(now?.title ?: tr("No guide data", "لا يوجد دليل"), color = ST.Sub, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (recording) StBadge("REC", ST.Live, filled = true)
            now?.let { val m = ((t - it.startTime) / 60_000L).coerceAtLeast(0); Text("$m'", color = ST.Amber, fontSize = 14.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 10.dp)) }
        }
    }
}

/** Live TV: three columns. LEAGUES table (categories) | scoreboard channel rows | preview monitor + NOW/NEXT stat card. */
@Composable
internal fun SportsTvLiveTv(p: LiveTvParams) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Column(Modifier.width(250.dp).fillMaxHeight().clip(ST.R).background(ST.Card)) {
            Row(Modifier.fillMaxWidth().background(ST.Line).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(tr("GROUPS", "المجموعات"), color = ST.Text, fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, modifier = Modifier.weight(1f))
                Text("${p.categories.size}", color = ST.Amber, fontSize = 13.sp, fontWeight = FontWeight.Black)
            }
            StSearchField(p.categorySearchQuery, p.onCategorySearchChange, tr("Filter", "تصفية"), Modifier.fillMaxWidth().padding(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp), contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)) {
                items(p.categories, key = { it.id }) { cat ->
                    val sel = cat.id == p.selectedCategoryId
                    StCard(
                        onClick = { p.onCategoryClick(cat) }, onLongClick = { p.onCategoryLongClick(cat) }, shape = ST.Pill, zoom = 1.02f,
                        container = if (sel) ST.Raised else Color.Transparent, focusedContainer = ST.Line,
                        modifier = Modifier.fillMaxWidth().focusRequester(p.categoryRequester(cat.id)).onFocusChanged { if (it.isFocused) p.onCategoryFocused(cat) }
                            .stRight { p.onRequestChannelsFromCategory() }
                    ) {
                        Row(Modifier.height(40.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.width(4.dp).fillMaxHeight().background(if (sel) ST.Amber else Color.Transparent))
                            Text((if (p.isCategoryLocked(cat)) "🔒 " else "") + cat.name, color = if (sel) ST.Amber else ST.Text, fontSize = 13.sp, fontWeight = if (sel) FontWeight.Black else FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f).padding(horizontal = 10.dp))
                            if (cat.count > 0) Box(Modifier.padding(end = 8.dp).background(if (sel) ST.Amber else ST.Line).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Text("${cat.count}", color = if (sel) ST.Bg else ST.Sub, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(p.sourceTitle.ifBlank { tr("Live", "مباشر") }.uppercase(), color = ST.Sub, fontSize = 12.sp, fontWeight = FontWeight.Black, maxLines = 1)
                Text("${p.channels.size}", color = ST.Amber, fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                StSearchField(p.channelSearchQuery, p.onChannelSearchChange, tr("Find a channel", "ابحث عن قناة"), Modifier.width(240.dp))
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(3.dp), contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp)) {
                items(p.channels, key = { it.id }) { c ->
                    val locked = p.isChannelLocked(c)
                    val previewing = c.id == p.previewChannel?.id
                    StCard(
                        onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c) }, shape = ST.RSmall, zoom = 1.015f,
                        container = if (previewing) ST.Line else ST.Raised, focusedContainer = ST.Line,
                        modifier = Modifier.fillMaxWidth().focusRequester(p.channelRequester(c.id)).onFocusChanged { if (it.isFocused) p.onChannelFocused(c) }
                            .stRight { p.onRequestPreviewFromChannel() }
                    ) {
                        Row(Modifier.height(58.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.width(48.dp).fillMaxHeight().background(if (previewing) ST.Amber else ST.Bg), contentAlignment = Alignment.Center) {
                                Text(if (c.number > 0) "${c.number}" else "–", color = if (previewing) ST.Bg else ST.Amber, fontSize = 14.sp, fontWeight = FontWeight.Black)
                            }
                            StLogo(c.name, if (locked) null else c.logoUrl, 40.dp, Modifier.padding(horizontal = 10.dp))
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text((if (c.id == p.movingChannelId) "⇅  " else "") + c.name.uppercase(), color = ST.Text, fontSize = 13.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(if (locked) tr("Locked", "مقفل") else c.currentProgram?.title ?: tr("No guide data", "لا يوجد دليل"), color = ST.Sub, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (!locked) c.currentProgram?.let { StProgress((System.currentTimeMillis() - it.startTime).toFloat() / (it.endTime - it.startTime).coerceAtLeast(1), Modifier.widthIn(max = 260.dp), height = 2.dp) }
                            }
                            Row(Modifier.padding(end = 10.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (c.catchUpSupported) StBadge("⟲", ST.Blue)
                                if (!locked) c.qualityBadge()?.let { StBadge(it, ST.Text) }
                                Text(if (c.isFavorite) "★" else "☆", color = if (c.isFavorite) ST.Amber else ST.Faint, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
        Column(Modifier.width(380.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            StCard(
                onClick = { p.previewChannel?.let(p.onChannelClick) }, container = Color.Black, zoom = 1.02f,
                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).focusRequester(p.previewFocusRequester).stLeft { p.onRequestChannelsFromPreview() }
            ) {
                val engine = p.previewPlayerEngine
                if (engine != null && p.previewChannel != null) PlayerRenderView(engine, PlayerSurfaceResizeMode.FIT, Modifier.fillMaxSize())
                when {
                    p.previewErrorMessage != null -> Text(p.previewErrorMessage, color = ST.Live, fontSize = 13.sp, modifier = Modifier.align(Alignment.Center).padding(16.dp))
                    p.isPreviewLoading -> Text(tr("Loading…", "جار التحميل…"), color = ST.Sub, modifier = Modifier.align(Alignment.Center))
                    p.previewChannel == null -> Text(tr("Pick a channel", "اختر قناة"), color = ST.Faint, modifier = Modifier.align(Alignment.Center))
                }
                if (p.previewChannel != null) Box(Modifier.align(Alignment.TopStart).background(ST.Live).padding(horizontal = 8.dp, vertical = 3.dp)) { Text("● LIVE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black) }
            }
            val c = p.previewChannel
            Column(Modifier.fillMaxWidth().weight(1f).clip(ST.R).background(ST.Card).padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (c == null) Text(tr("No channel selected", "لم يتم اختيار قناة"), color = ST.Faint, fontSize = 14.sp)
                else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StLogo(c.name, c.logoUrl, 44.dp)
                        Column(Modifier.weight(1f)) {
                            Text(c.name.uppercase(), color = ST.Text, fontSize = 16.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (c.number > 0) StBadge("CH ${c.number}", ST.Sub)
                                c.qualityBadge()?.let { StBadge(it, ST.Text) }
                                if (c.catchUpSupported) StBadge(tr("ARCHIVE", "أرشيف"), ST.Blue)
                            }
                        }
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(ST.Line))
                    c.currentProgram?.let { now ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.background(ST.Amber).padding(horizontal = 6.dp, vertical = 2.dp)) { Text(tr("NOW", "الآن"), color = ST.Bg, fontSize = 10.sp, fontWeight = FontWeight.Black) }
                            Text(now.title, color = ST.Text, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(stClock(now.startTime), color = ST.Sub, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            StProgress((System.currentTimeMillis() - now.startTime).toFloat() / (now.endTime - now.startTime).coerceAtLeast(1), Modifier.weight(1f))
                            Text(stClock(now.endTime), color = ST.Sub, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(now.description, color = ST.Faint, fontSize = 12.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    } ?: Text(tr("No guide data", "لا يوجد دليل"), color = ST.Faint)
                    c.nextProgram?.let {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.background(ST.Line).padding(horizontal = 6.dp, vertical = 2.dp)) { Text(tr("NEXT", "التالي"), color = ST.Sub, fontSize = 10.sp, fontWeight = FontWeight.Black) }
                            Text("${stClock(it.startTime)}  ${it.title}", color = ST.Sub, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}
