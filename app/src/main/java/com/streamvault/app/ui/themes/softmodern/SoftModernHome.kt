package com.streamvault.app.ui.themes.softmodern

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

/** SoftModern's own rounded search field: magnifier inside a soft pill, white outline on focus. */
@Composable
internal fun SmSearchField(value: String, onChange: (String) -> Unit, hint: String, modifier: Modifier = Modifier, onSubmit: (() -> Unit)? = null) {
    var focused by remember { mutableStateOf(false) }
    Row(
        modifier.clip(SM.Pill).background(if (focused) SM.Card else SM.Raised).border(2.dp, if (focused) SM.Text else Color.Transparent, SM.Pill)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("⌕", color = if (focused) SM.Amber else SM.Faint, fontSize = 16.sp)
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) Text(hint, color = SM.Faint, fontSize = 14.sp, maxLines = 1)
            BasicTextField(
                value = value, onValueChange = onChange, singleLine = true,
                textStyle = TextStyle(color = SM.Text, fontSize = 14.sp), cursorBrush = SolidColor(SM.Amber),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { onSubmit?.invoke() }, onDone = { onSubmit?.invoke() }),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                modifier = Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused }
            )
        }
    }
}

private class SmDest(val route: String, val glyph: String, val en: String, val ar: String)

private val smDestinations = listOf(
    SmDest(Routes.SEARCH, "⌕", "Search", "بحث"),
    SmDest(Routes.HOME, "⌂", "Home", "الرئيسية"),
    SmDest(Routes.LIVE_TV, "◉", "Live TV", "البث المباشر"),
    SmDest(Routes.EPG, "▦", "Guide", "الدليل"),
    SmDest(Routes.MOVIES, "▶", "Movies", "أفلام"),
    SmDest(Routes.SERIES, "❏", "Series", "مسلسلات"),
    SmDest(Routes.FAVORITES, "♥", "My List", "قائمتي"),
    SmDest(Routes.SETTINGS, "⚙", "Settings", "الإعدادات")
)

private fun onRoute(current: String, route: String) = current == route || current.startsWith("$route?") || current.startsWith("$route/")

/** Shell: floating white pill bar across the top (logo, centered destinations, clock); content sits on cream paper below. */
@Composable
internal fun SoftModernShell(p: ShellParams) {
    SmBackground(p.modifier) {
        Column(Modifier.fillMaxSize()) {
            if (p.topBarVisible) {
                Row(
                    Modifier.fillMaxWidth().padding(start = 40.dp, end = 40.dp, top = 20.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.size(40.dp).clip(SM.Pill).background(SM.Amber), contentAlignment = Alignment.Center) {
                            Text("a", color = SM.Card, fontSize = 22.sp, fontWeight = FontWeight.Black)
                        }
                        Text("Alaa", color = SM.Text, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.weight(1f))
                    Row(
                        Modifier.clip(SM.Pill).background(SM.Card).padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically
                    ) {
                        smDestinations.forEach { d ->
                            val active = onRoute(p.currentRoute, d.route)
                            SmCard(
                                onClick = { if (!active) p.onNavigate(d.route) }, shape = SM.Pill, zoom = 1.05f,
                                container = if (active) SM.Amber else Color.Transparent, focusedContainer = if (active) SM.AmberDeep else SM.Sage
                            ) {
                                Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(d.glyph, color = if (active) SM.Card else SM.Sub, fontSize = 14.sp)
                                    Text(tr(d.en, d.ar), color = if (active) SM.Card else SM.Text, fontSize = 14.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium, maxLines = 1)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Text(smClock(System.currentTimeMillis()), color = SM.Sub, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                }
            }
            Column(Modifier.weight(1f).fillMaxWidth().padding(p.contentPadding).padding(start = 40.dp, end = 40.dp, top = 20.dp)) {
                if (p.showScreenHeader || p.topBarActions != null) {
                    Row(Modifier.fillMaxWidth().padding(bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (p.showScreenHeader) Column(Modifier.weight(1f)) {
                            Text(p.title, color = SM.Text, fontSize = 28.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            p.subtitle?.takeIf { it.isNotBlank() }?.let { Text(it, color = SM.Sub, fontSize = 14.sp, maxLines = 1) }
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

/** Home: full-bleed cinematic hero with amber CTA, then continue-watching landscape row and poster shelves. */
@Composable
internal fun SoftModernDashboard(p: DashboardParams) {
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
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 48.dp), verticalArrangement = Arrangement.spacedBy(28.dp)) {
        item {
            Row(Modifier.fillMaxWidth().height(330.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Row(Modifier.weight(1f).fillMaxHeight().clip(SM.R).background(SM.Card)) {
                    Column(Modifier.weight(1f).fillMaxHeight().padding(30.dp), verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            SmBadge(tr("Today's pick", "اختيار اليوم"), SM.Amber, filled = true)
                            s.provider?.name?.let { Text(it, color = SM.Faint, fontSize = 12.sp) }
                        }
                        Text(s.feature.title.ifBlank { s.provider?.name ?: "Alaa" }, color = SM.Text, fontSize = 36.sp, fontWeight = FontWeight.Bold, maxLines = 2, lineHeight = 40.sp)
                        Text(s.feature.summary, color = SM.Sub, fontSize = 14.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SmButton(s.feature.actionLabel.ifBlank { tr("Watch now", "شاهد الآن") }, featureAction, Modifier.focusRequester(first), primary = true, icon = "▶")
                            SmButton(tr("Live TV", "البث المباشر"), { p.onNavigate(Routes.LIVE_TV) }, icon = "◉")
                            SmButton(tr("Guide", "الدليل"), { p.onNavigate(Routes.EPG) }, icon = "▦")
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SmBadge("${s.stats.liveChannelCount} " + tr("channels", "قناة"), SM.Sub)
                            SmBadge("${s.stats.movieLibraryCount} " + tr("movies", "فيلم"), SM.Sub)
                            SmBadge("${s.stats.seriesLibraryCount} " + tr("series", "مسلسل"), SM.Sub)
                        }
                    }
                    Box(Modifier.weight(0.9f).fillMaxHeight().padding(10.dp).clip(SM.RSmall).background(SM.Sage)) {
                        s.feature.artworkUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                    }
                }
                if (s.continueWatching.isNotEmpty()) Column(Modifier.width(340.dp).fillMaxHeight().clip(SM.R).background(SM.Sage).padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(tr("Continue watching", "متابعة المشاهدة"), color = SM.Text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(4.dp)) {
                        items(s.continueWatching, key = { "cw${it.id}" }) { h ->
                            val prog = if (h.totalDurationMs > 0) h.resumePositionMs.toFloat() / h.totalDurationMs else null
                            val left = if (h.totalDurationMs > 0) smDuration(h.totalDurationMs - h.resumePositionMs) + " " + tr("left", "متبقي") else null
                            SmWide(h.title, h.posterUrl, left, prog, { p.onContinueWatchingItemClick(h) }, Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }
        listOf(
            Triple(tYour, s.favoriteChannels, p.onFavoriteChannelClick),
            Triple(tRecentCh, s.recentChannels, p.onRecentChannelClick)
        ).forEach { (title, channels, click) ->
            if (channels.isNotEmpty()) item {
                Column {
                    SmRowTitle(title, trailing = "${channels.size}")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                        items(channels, key = { "$title${it.id}" }) { c -> SmChannelTile(c, c.id in p.recordingChannelIds) { click(c, s.currentCombinedProfileId) } }
                    }
                }
            }
        }
        listOf(
            tRec to s.recommendedMovies,
            tTop to s.topRatedMovies,
            tNew to s.recentMovies
        ).forEach { (title, movies) ->
            if (movies.isNotEmpty()) item {
                Column {
                    SmRowTitle(title)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                        items(movies, key = { "$title${it.id}" }) { m -> SmPoster(m.name, m.posterUrl, m.year, { p.onMovieClick(m) }, Modifier.width(150.dp)) }
                    }
                }
            }
        }
        if (s.recentSeries.isNotEmpty()) item {
            Column {
                SmRowTitle(tr("New series", "مسلسلات جديدة"))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(s.recentSeries, key = { "rs${it.id}" }) { m -> SmPoster(m.name, m.posterUrl, m.genre, { p.onSeriesClick(m) }, Modifier.width(150.dp)) }
                }
            }
        }
    }
}

/** Landscape channel tile: logo centered on a dark card, now-playing title and progress underneath. */
@Composable
private fun SmChannelTile(c: Channel, recording: Boolean, onClick: () -> Unit) {
    val now = c.currentProgram
    Column(Modifier.width(220.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SmCard(onClick = onClick, modifier = Modifier.fillMaxWidth().height(124.dp), container = SM.Card) {
            SmLogo(c.name, c.logoUrl, 64.dp, Modifier.align(Alignment.Center))
            Row(Modifier.align(Alignment.TopEnd).padding(8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (recording) SmBadge("REC", SM.Live, filled = true)
                c.qualityBadge()?.let { SmBadge(it, SM.Text) }
            }
            if (c.number > 0) Text("${c.number}", color = SM.Faint, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.TopStart).padding(10.dp))
            now?.let { SmProgress(((System.currentTimeMillis() - it.startTime).toFloat() / (it.endTime - it.startTime).coerceAtLeast(1)), Modifier.align(Alignment.BottomCenter).padding(10.dp), 3.dp) }
        }
        Text(c.name, color = SM.Text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(now?.title ?: tr("No guide data", "لا يوجد دليل"), color = SM.Faint, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Live TV: sage categories column | white channel pebbles column | preview card with now/next on the end. */
@Composable
internal fun SoftModernLiveTv(p: LiveTvParams) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            Column(Modifier.width(250.dp).fillMaxHeight().clip(SM.R).background(SM.Sage).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SmSearchField(p.categorySearchQuery, p.onCategorySearchChange, tr("Categories", "التصنيفات"), Modifier.fillMaxWidth())
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(p.categories, key = { it.id }) { cat ->
                        val sel = cat.id == p.selectedCategoryId
                        SmCard(
                            onClick = { p.onCategoryClick(cat) }, onLongClick = { p.onCategoryLongClick(cat) }, zoom = 1.03f,
                            shape = SM.Pill, container = if (sel) SM.Card else Color.Transparent, focusedContainer = SM.Card,
                            modifier = Modifier.fillMaxWidth().focusRequester(p.categoryRequester(cat.id)).onFocusChanged { if (it.isFocused) p.onCategoryFocused(cat) }
                                .smRight { p.onRequestChannelsFromCategory() }
                        ) {
                            Row(Modifier.padding(horizontal = 14.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (sel) Box(Modifier.size(6.dp).clip(SM.Pill).background(SM.Amber))
                                Text((if (p.isCategoryLocked(cat)) "🔒 " else "") + cat.name, color = if (sel) SM.Amber else SM.Text, fontSize = 14.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f).padding(start = if (sel) 8.dp else 0.dp))
                                if (cat.count > 0) Text("${cat.count}", color = SM.Faint, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
            Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${p.channels.size} " + tr("channels", "قناة"), color = SM.Sub, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    SmSearchField(p.channelSearchQuery, p.onChannelSearchChange, tr("Find a channel", "ابحث عن قناة"), Modifier.width(280.dp))
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 6.dp, horizontal = 4.dp)) {
                    items(p.channels, key = { it.id }) { c ->
                        run {
                            run {
                                val locked = p.isChannelLocked(c)
                                SmCard(
                                    onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c) }, zoom = 1.03f,
                                    container = if (c.id == p.previewChannel?.id) SM.Sage else SM.Card,
                                    modifier = Modifier.fillMaxWidth().focusRequester(p.channelRequester(c.id)).onFocusChanged { if (it.isFocused) p.onChannelFocused(c) }
                                        .smRight { p.onRequestPreviewFromChannel() }
                                ) {
                                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text(if (c.number > 0) "${c.number}" else "", color = SM.Faint, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(34.dp))
                                        SmLogo(c.name, if (locked) null else c.logoUrl, 46.dp)
                                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                            Text(if (c.id == p.movingChannelId) "⇅  ${c.name}" else c.name, color = SM.Text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(if (locked) tr("Locked", "مقفل") else c.currentProgram?.title ?: tr("No guide data", "لا يوجد دليل"), color = SM.Faint, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            if (!locked) c.currentProgram?.let { SmProgress((System.currentTimeMillis() - it.startTime).toFloat() / (it.endTime - it.startTime).coerceAtLeast(1), height = 3.dp) }
                                        }
                                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(if (c.isFavorite) "♥" else "♡", color = if (c.isFavorite) SM.Amber else SM.Faint, fontSize = 15.sp)
                                            if (!locked) c.qualityBadge()?.let { SmBadge(it, SM.Text) }
                                            if (c.catchUpSupported) SmBadge("⟲", SM.Blue)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Column(Modifier.width(400.dp).fillMaxHeight().clip(SM.R).background(SM.Card).padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SmCard(
                    onClick = { p.previewChannel?.let(p.onChannelClick) }, container = SM.Text, zoom = 1.02f,
                    shape = SM.RSmall, modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).focusRequester(p.previewFocusRequester).smLeft { p.onRequestChannelsFromPreview() }
                ) {
                    val engine = p.previewPlayerEngine
                    if (engine != null && p.previewChannel != null) PlayerRenderView(engine, PlayerSurfaceResizeMode.FIT, Modifier.fillMaxSize())
                    when {
                        p.previewErrorMessage != null -> Text(p.previewErrorMessage, color = SM.Live, fontSize = 13.sp, modifier = Modifier.align(Alignment.Center).padding(16.dp))
                        p.isPreviewLoading -> Text(tr("Loading…", "جار التحميل…"), color = SM.Sub, modifier = Modifier.align(Alignment.Center))
                        p.previewChannel == null -> Text(tr("Pick a channel to preview", "اختر قناة للمعاينة"), color = SM.Faint, modifier = Modifier.align(Alignment.Center))
                    }
                    if (p.previewChannel != null) Box(Modifier.align(Alignment.TopStart).padding(10.dp)) { SmBadge("● LIVE", SM.Live, filled = true) }
                }
                val c = p.previewChannel
                Column(Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(p.sourceTitle.ifBlank { tr("Live TV", "البث المباشر") }, color = SM.Faint, fontSize = 13.sp)
                    if (c == null) Text(tr("Browse channels below", "تصفح القنوات بالأسفل"), color = SM.Text, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    else {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SmLogo(c.name, c.logoUrl, 48.dp)
                            Column {
                                Text(c.name, color = SM.Text, fontSize = 24.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (c.number > 0) SmBadge("CH ${c.number}", SM.Sub)
                                    c.qualityBadge()?.let { SmBadge(it, SM.Text) }
                                    if (c.catchUpSupported) SmBadge(tr("CATCH-UP", "أرشيف"), SM.Blue)
                                }
                            }
                        }
                        c.currentProgram?.let { now ->
                            Text(now.title, color = SM.Text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(smClock(now.startTime), color = SM.Sub, fontSize = 12.sp)
                                SmProgress((System.currentTimeMillis() - now.startTime).toFloat() / (now.endTime - now.startTime).coerceAtLeast(1), Modifier.weight(1f))
                                Text(smClock(now.endTime), color = SM.Sub, fontSize = 12.sp)
                            }
                            Text(now.description, color = SM.Faint, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        } ?: Text(tr("No guide data", "لا يوجد دليل"), color = SM.Faint)
                        c.nextProgram?.let { Text(tr("Up next", "التالي") + "  ${smClock(it.startTime)}  ${it.title}", color = SM.Sub, fontSize = 13.sp, maxLines = 1) }
                    }
                }
            }
        }
}
