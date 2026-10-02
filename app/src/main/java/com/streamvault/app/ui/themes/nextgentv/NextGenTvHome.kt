package com.streamvault.app.ui.themes.nextgentv

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

/** NextGenTv's own rounded search field: magnifier inside a soft pill, white outline on focus. */
@Composable
internal fun NgSearchField(value: String, onChange: (String) -> Unit, hint: String, modifier: Modifier = Modifier, onSubmit: (() -> Unit)? = null) {
    var focused by remember { mutableStateOf(false) }
    Row(
        modifier.clip(NG.Pill).background(if (focused) NG.Card else NG.Raised).border(2.dp, if (focused) NG.Text else Color.Transparent, NG.Pill)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("⌕", color = if (focused) NG.Amber else NG.Faint, fontSize = 16.sp)
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) Text(hint, color = NG.Faint, fontSize = 14.sp, maxLines = 1)
            BasicTextField(
                value = value, onValueChange = onChange, singleLine = true,
                textStyle = TextStyle(color = NG.Text, fontSize = 14.sp), cursorBrush = SolidColor(NG.Amber),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { onSubmit?.invoke() }, onDone = { onSubmit?.invoke() }),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                modifier = Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused }
            )
        }
    }
}

private class NgDest(val route: String, val glyph: String, val en: String, val ar: String)

private val ngDestinations = listOf(
    NgDest(Routes.HOME, "⌂", "Home", "الرئيسية"),
    NgDest(Routes.LIVE_TV, "◉", "Live TV", "البث المباشر"),
    NgDest(Routes.EPG, "▦", "Guide", "الدليل"),
    NgDest(Routes.MOVIES, "▶", "Movies", "أفلام"),
    NgDest(Routes.SERIES, "❏", "Series", "مسلسلات"),
    NgDest(Routes.FAVORITES, "♥", "My List", "قائمتي"),
    NgDest(Routes.SEARCH, "⌕", "Search", "بحث"),
    NgDest(Routes.SETTINGS, "⚙", "Settings", "الإعدادات")
)

private fun onRoute(current: String, route: String) = current == route || current.startsWith("$route?") || current.startsWith("$route/")

/** Shell: a detached floating "ornament" capsule of glyph keys hovering beside a big spatial window.
 *  The focused key shows its label as a floating tag; the content sits inside a window pane with a grab bar under it. */
@Composable
internal fun NextGenTvShell(p: ShellParams) {
    NgBackground(p.modifier) {
        Row(Modifier.fillMaxSize().padding(start = 18.dp, end = 26.dp, top = 18.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            if (p.topBarVisible) {
                Column(
                    Modifier.ngTilt(10f).clip(NG.Pill).background(NG.Raised.copy(alpha = 0.92f))
                        .border(1.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.25f), Color.Transparent)), NG.Pill)
                        .padding(horizontal = 8.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(Modifier.size(40.dp).clip(CircleShape).background(Brush.linearGradient(listOf(NG.Amber, NG.Violet))), contentAlignment = Alignment.Center) {
                        Text("A", color = NG.Bg, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    }
                    ngDestinations.forEach { d ->
                        val active = onRoute(p.currentRoute, d.route)
                        var f by remember { mutableStateOf(false) }
                        Box(contentAlignment = Alignment.CenterStart) {
                            NgCard(
                                onClick = { if (!active) p.onNavigate(d.route) }, shape = CircleShape, zoom = 1.18f,
                                container = if (active) NG.Amber.copy(alpha = 0.22f) else Color.Transparent, focusedContainer = NG.Text,
                                modifier = Modifier.size(46.dp).onFocusChanged { f = it.isFocused }
                            ) {
                                Text(d.glyph, color = if (f) NG.Bg else if (active) NG.Amber else NG.Sub, fontSize = 18.sp, modifier = Modifier.align(Alignment.Center))
                            }
                        }
                        if (f) Text(
                            tr(d.en, d.ar), color = NG.Bg, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1,
                            modifier = Modifier.clip(NG.Pill).background(NG.Amber).padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(Modifier.width(22.dp))
            }
            Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                NgPane(Modifier.weight(1f).fillMaxWidth()) {
                    Column(Modifier.fillMaxSize().padding(p.contentPadding).padding(horizontal = 28.dp, vertical = 20.dp)) {
                        Row(Modifier.fillMaxWidth().padding(bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (p.showScreenHeader) Column(Modifier.weight(1f)) {
                                Text(p.title, color = NG.Text, fontSize = 28.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                p.subtitle?.takeIf { it.isNotBlank() }?.let { Text(it, color = NG.Sub, fontSize = 13.sp, maxLines = 1) }
                            } else Spacer(Modifier.weight(1f))
                            p.topBarActions?.let { Row(horizontalArrangement = Arrangement.spacedBy(10.dp), content = it) }
                            Text(ngClock(System.currentTimeMillis()), color = NG.Sub, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(start = 14.dp).clip(NG.Pill).background(Color.White.copy(alpha = 0.06f)).padding(horizontal = 12.dp, vertical = 5.dp))
                        }
                        p.header?.let { Column(content = it) }
                        Column(Modifier.fillMaxSize(), content = p.content)
                    }
                }
                // window grab bar
                Box(Modifier.padding(top = 8.dp).width(120.dp).height(5.dp).clip(NG.Pill).background(Color.White.copy(alpha = 0.25f)))
            }
        }
    }
}

/** Home: spatial stage. Hero artwork on the left of the window, a tilted stack of floating "resume" panes on the right,
 *  then depth shelves (each row lifts on a faint plate). */
@Composable
internal fun NextGenTvDashboard(p: DashboardParams) {
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
    val tYour = tr("Your channels", "قنواتك")
    val tRecentCh = tr("Recently watched channels", "شوهدت مؤخراً")
    val tRec = tr("Recommended for you", "مقترح لك")
    val tTop = tr("Top rated", "الأعلى تقييماً")
    val tNew = tr("New arrivals", "وصل حديثاً")
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 40.dp, top = 4.dp), verticalArrangement = Arrangement.spacedBy(26.dp)) {
        item {
            Row(Modifier.fillMaxWidth().height(340.dp), horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                Box(Modifier.weight(1f).fillMaxHeight().clip(NG.R)) {
                    s.feature.artworkUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, NG.Bg.copy(alpha = 0.95f)))))
                    Column(
                        Modifier.align(Alignment.BottomStart).padding(18.dp).widthIn(max = 560.dp).clip(NG.R).background(NG.Bg.copy(alpha = 0.72f))
                            .border(1.dp, Color.White.copy(alpha = 0.12f), NG.R).padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            NgBadge(tr("SPOTLIGHT", "مميز"), NG.Amber, filled = true)
                            s.provider?.name?.let { Text(it, color = NG.Sub, fontSize = 12.sp) }
                        }
                        Text(s.feature.title.ifBlank { s.provider?.name ?: "Next Gen TV" }, color = NG.Text, fontSize = 36.sp, fontWeight = FontWeight.Black, maxLines = 2, lineHeight = 40.sp)
                        Text(s.feature.summary, color = NG.Sub, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            NgButton(s.feature.actionLabel.ifBlank { tr("Watch now", "شاهد الآن") }, featureAction, Modifier.focusRequester(first), primary = true, icon = "▶")
                            NgButton(tr("Live TV", "البث المباشر"), { p.onNavigate(Routes.LIVE_TV) }, icon = "◉")
                            NgButton(tr("Guide", "الدليل"), { p.onNavigate(Routes.EPG) }, icon = "▦")
                        }
                    }
                }
                // floating stats + resume stack, tilted toward the hero
                Column(Modifier.width(300.dp).fillMaxHeight().ngTilt(-8f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(s.stats.liveChannelCount to tr("live", "مباشر"), s.stats.movieLibraryCount to tr("movies", "أفلام"), s.stats.seriesLibraryCount to tr("series", "مسلسلات")).forEach { (n, l) ->
                            Column(Modifier.weight(1f).clip(NG.RSmall).background(NG.Card).border(1.dp, Color.White.copy(alpha = 0.1f), NG.RSmall).padding(10.dp)) {
                                Text("$n", color = NG.Amber, fontSize = 18.sp, fontWeight = FontWeight.Black)
                                Text(l, color = NG.Faint, fontSize = 11.sp)
                            }
                        }
                    }
                    Text(tr("Resume", "متابعة"), color = NG.Sub, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    if (s.continueWatching.isEmpty()) Text(tr("Nothing in progress", "لا يوجد شيء قيد المشاهدة"), color = NG.Faint, fontSize = 13.sp)
                    s.continueWatching.take(3).forEach { h ->
                        val prog = if (h.totalDurationMs > 0) h.resumePositionMs.toFloat() / h.totalDurationMs else null
                        NgCard(onClick = { p.onContinueWatchingItemClick(h) }, shape = NG.RSmall, container = NG.Card, modifier = Modifier.fillMaxWidth().height(70.dp)) {
                            Row(Modifier.fillMaxSize().padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(Modifier.fillMaxHeight().aspectRatio(16f / 9f).clip(RoundedCornerShape(10.dp)).background(NG.Line)) {
                                    h.posterUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                                }
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(h.title, color = NG.Text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    prog?.let { NgProgress(it, height = 3.dp) }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (s.continueWatching.size > 3) item {
            NgShelf(tr("Continue watching", "متابعة المشاهدة")) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(12.dp)) {
                    items(s.continueWatching.drop(3), key = { "cw${it.id}" }) { h ->
                        val prog = if (h.totalDurationMs > 0) h.resumePositionMs.toFloat() / h.totalDurationMs else null
                        val left = if (h.totalDurationMs > 0) ngDuration(h.totalDurationMs - h.resumePositionMs) + " " + tr("left", "متبقي") else null
                        NgWide(h.title, h.posterUrl, left, prog, { p.onContinueWatchingItemClick(h) }, Modifier.width(260.dp))
                    }
                }
            }
        }
        listOf(
            Triple(tYour, s.favoriteChannels, p.onFavoriteChannelClick),
            Triple(tRecentCh, s.recentChannels, p.onRecentChannelClick)
        ).forEach { (title, channels, click) ->
            if (channels.isNotEmpty()) item {
                NgShelf(title, "${channels.size}") {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(12.dp)) {
                        items(channels, key = { "$title${it.id}" }) { c -> NgChannelOrb(c, c.id in p.recordingChannelIds) { click(c, s.currentCombinedProfileId) } }
                    }
                }
            }
        }
        listOf(tRec to s.recommendedMovies, tTop to s.topRatedMovies, tNew to s.recentMovies).forEach { (title, movies) ->
            if (movies.isNotEmpty()) item {
                NgShelf(title) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(12.dp)) {
                        items(movies, key = { "$title${it.id}" }) { m -> NgPoster(m.name, m.posterUrl, m.year, { p.onMovieClick(m) }, Modifier.width(140.dp)) }
                    }
                }
            }
        }
        if (s.recentSeries.isNotEmpty()) item {
            NgShelf(tr("New series", "مسلسلات جديدة")) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(12.dp)) {
                    items(s.recentSeries, key = { "rs${it.id}" }) { m -> NgPoster(m.name, m.posterUrl, m.genre, { p.onSeriesClick(m) }, Modifier.width(140.dp)) }
                }
            }
        }
    }
}

/** Shelf: title, then the row floats on a faint depth plate. */
@Composable
private fun NgShelf(title: String, trailing: String? = null, content: @Composable () -> Unit) {
    Column {
        NgRowTitle(title, trailing = trailing)
        Box(Modifier.fillMaxWidth().clip(NG.R).background(Color.White.copy(alpha = 0.025f))) { content() }
    }
}

/** Channel orb: round logo disc floating over a name capsule, beam progress ring under it. */
@Composable
private fun NgChannelOrb(c: Channel, recording: Boolean, onClick: () -> Unit) {
    val now = c.currentProgram
    Column(Modifier.width(130.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        NgCard(onClick = onClick, shape = CircleShape, container = NG.Card, modifier = Modifier.size(104.dp)) {
            NgLogo(c.name, c.logoUrl, 60.dp, Modifier.align(Alignment.Center))
            if (recording) Box(Modifier.align(Alignment.TopEnd).padding(10.dp).size(10.dp).clip(CircleShape).background(NG.Live))
        }
        Text((if (c.number > 0) "${c.number}  " else "") + c.name, color = NG.Text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        now?.let { NgProgress((System.currentTimeMillis() - it.startTime).toFloat() / (it.endTime - it.startTime).coerceAtLeast(1), Modifier.width(70.dp), 3.dp) }
        Text(now?.title ?: (c.qualityBadge() ?: ""), color = NG.Faint, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Live TV: three floating panes in space. Categories pane tilted in from the start, the channel column centre stage,
 *  and the preview pane (video + now/next) tilted in from the end. */
@Composable
internal fun NextGenTvLiveTv(p: LiveTvParams) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        // categories pane
        Column(
            Modifier.width(230.dp).fillMaxHeight().ngTilt(6f).clip(NG.R).background(NG.Raised).border(1.dp, Color.White.copy(alpha = 0.08f), NG.R).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(tr("Spaces", "المجموعات"), color = NG.Faint, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 6.dp))
            NgSearchField(p.categorySearchQuery, p.onCategorySearchChange, tr("Categories", "التصنيفات"), Modifier.fillMaxWidth())
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
                items(p.categories, key = { it.id }) { cat ->
                    val sel = cat.id == p.selectedCategoryId
                    NgCard(
                        onClick = { p.onCategoryClick(cat) }, onLongClick = { p.onCategoryLongClick(cat) }, shape = NG.RSmall, zoom = 1.05f,
                        container = if (sel) NG.Amber.copy(alpha = 0.16f) else Color.Transparent,
                        modifier = Modifier.fillMaxWidth().focusRequester(p.categoryRequester(cat.id)).onFocusChanged { if (it.isFocused) p.onCategoryFocused(cat) }
                            .ngRight { p.onRequestChannelsFromCategory() }
                    ) {
                        Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.size(8.dp).clip(CircleShape).background(if (sel) NG.Amber else Color.White.copy(alpha = 0.15f)))
                            Text((if (p.isCategoryLocked(cat)) "🔒 " else "") + cat.name, color = if (sel) NG.Amber else NG.Text, fontSize = 14.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            if (cat.count > 0) Text("${cat.count}", color = NG.Faint, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
        // channel column, centre stage
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(p.sourceTitle.ifBlank { tr("Live TV", "البث المباشر") }, color = NG.Faint, fontSize = 11.sp)
                    Text("${p.channels.size} " + tr("channels", "قناة"), color = NG.Text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                NgSearchField(p.channelSearchQuery, p.onChannelSearchChange, tr("Find a channel", "ابحث عن قناة"), Modifier.width(240.dp))
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 8.dp, horizontal = 10.dp)) {
                items(p.channels, key = { it.id }) { c ->
                    val locked = p.isChannelLocked(c)
                    val current = c.id == p.previewChannel?.id
                    NgCard(
                        onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c) }, zoom = 1.04f, shape = NG.RSmall,
                        container = if (current) NG.Amber.copy(alpha = 0.12f) else NG.Card.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth().focusRequester(p.channelRequester(c.id)).onFocusChanged { if (it.isFocused) p.onChannelFocused(c) }
                            .ngRight { p.onRequestPreviewFromChannel() }
                    ) {
                        Row(Modifier.padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            NgLogo(c.name, if (locked) null else c.logoUrl, 42.dp)
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (c.number > 0) Text("${c.number}", color = NG.Amber, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                    Text(if (c.id == p.movingChannelId) "⇅  ${c.name}" else c.name, color = NG.Text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                                    if (current) NgBadge(tr("NOW", "الآن"), NG.Live)
                                }
                                Text(if (locked) tr("Locked", "مقفل") else c.currentProgram?.title ?: tr("No guide data", "لا يوجد دليل"), color = NG.Faint, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            if (c.isFavorite) Text("♥", color = NG.Live, fontSize = 14.sp)
                            if (!locked) c.qualityBadge()?.let { NgBadge(it, NG.Sub) }
                            if (c.catchUpSupported) NgBadge("⟲", NG.Blue)
                        }
                    }
                }
            }
        }
        // preview pane
        val c = p.previewChannel
        Column(
            Modifier.width(360.dp).fillMaxHeight().ngTilt(-6f).clip(NG.R).background(NG.Raised).border(1.dp, Color.White.copy(alpha = 0.08f), NG.R).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            NgCard(
                onClick = { c?.let(p.onChannelClick) }, container = Color.Black, zoom = 1.03f, shape = NG.RSmall,
                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).focusRequester(p.previewFocusRequester).ngLeft { p.onRequestChannelsFromPreview() }
            ) {
                val engine = p.previewPlayerEngine
                if (engine != null && c != null) PlayerRenderView(engine, PlayerSurfaceResizeMode.FIT, Modifier.fillMaxSize())
                when {
                    p.previewErrorMessage != null -> Text(p.previewErrorMessage, color = NG.Live, fontSize = 12.sp, modifier = Modifier.align(Alignment.Center).padding(12.dp))
                    p.isPreviewLoading -> Text(tr("Loading…", "جار التحميل…"), color = NG.Sub, modifier = Modifier.align(Alignment.Center))
                    c == null -> Text(tr("Pick a channel", "اختر قناة"), color = NG.Faint, modifier = Modifier.align(Alignment.Center))
                }
                if (c != null) Box(Modifier.align(Alignment.TopStart).padding(8.dp)) { NgBadge("● LIVE", NG.Live, filled = true) }
            }
            if (c != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    NgLogo(c.name, c.logoUrl, 40.dp)
                    Column(Modifier.weight(1f)) {
                        Text(c.name, color = NG.Text, fontSize = 17.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (c.number > 0) NgBadge("CH ${c.number}", NG.Sub)
                            c.qualityBadge()?.let { NgBadge(it, NG.Amber) }
                            if (c.catchUpSupported) NgBadge(tr("ARCHIVE", "أرشيف"), NG.Blue)
                        }
                    }
                }
                c.currentProgram?.let { now ->
                    Column(Modifier.fillMaxWidth().clip(NG.RSmall).background(NG.Card).padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(tr("NOW", "الآن"), color = NG.Amber, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Text(now.title, color = NG.Text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        NgProgress((System.currentTimeMillis() - now.startTime).toFloat() / (now.endTime - now.startTime).coerceAtLeast(1))
                        Text("${ngClock(now.startTime)} – ${ngClock(now.endTime)}", color = NG.Faint, fontSize = 11.sp)
                        if (now.description.isNotBlank()) Text(now.description, color = NG.Sub, fontSize = 12.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    }
                } ?: Text(tr("No guide data", "لا يوجد دليل"), color = NG.Faint)
                c.nextProgram?.let {
                    Column(Modifier.fillMaxWidth().clip(NG.RSmall).background(Color.White.copy(alpha = 0.04f)).padding(12.dp)) {
                        Text(tr("UP NEXT", "التالي"), color = NG.Violet, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Text("${ngClock(it.startTime)}  ${it.title}", color = NG.Sub, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}
