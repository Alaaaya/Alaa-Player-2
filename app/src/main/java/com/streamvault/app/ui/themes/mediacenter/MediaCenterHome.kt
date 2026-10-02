package com.streamvault.app.ui.themes.mediacenter

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

/** Media Center search: engraved underline field (no pill), gold rule turns bright on focus. */
@Composable
internal fun McSearchField(value: String, onChange: (String) -> Unit, hint: String, modifier: Modifier = Modifier, onSubmit: (() -> Unit)? = null) {
    var focused by remember { mutableStateOf(false) }
    Column(modifier) {
        Row(Modifier.padding(horizontal = 4.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("⌕", color = if (focused) MC.Amber else MC.Faint, fontSize = 16.sp)
            Box(Modifier.weight(1f)) {
                if (value.isEmpty()) Text(hint.uppercase(), color = MC.Faint, fontSize = 12.sp, letterSpacing = 1.5.sp, maxLines = 1)
                BasicTextField(
                    value = value, onValueChange = onChange, singleLine = true,
                    textStyle = TextStyle(color = MC.Text, fontSize = 15.sp, fontFamily = MC.Serif), cursorBrush = SolidColor(MC.Amber),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { onSubmit?.invoke() }, onDone = { onSubmit?.invoke() }),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                    modifier = Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused }
                )
            }
        }
        Box(Modifier.fillMaxWidth().height(if (focused) 2.dp else 1.dp).background(if (focused) MC.Amber else MC.Line))
    }
}

private class McDest(val route: String, val glyph: String, val en: String, val ar: String)

private val mcDestinations = listOf(
    McDest(Routes.SEARCH, "⌕", "Search", "بحث"),
    McDest(Routes.HOME, "⌂", "Home", "الرئيسية"),
    McDest(Routes.LIVE_TV, "◉", "Live TV", "البث المباشر"),
    McDest(Routes.EPG, "▦", "Guide", "الدليل"),
    McDest(Routes.MOVIES, "▶", "Movies", "أفلام"),
    McDest(Routes.SERIES, "❏", "Series", "مسلسلات"),
    McDest(Routes.FAVORITES, "♥", "My List", "قائمتي"),
    McDest(Routes.SETTINGS, "⚙", "Settings", "الإعدادات")
)

private fun onRoute(current: String, route: String) = current == route || current.startsWith("$route?") || current.startsWith("$route/")

/** Shell: receiver front-panel. Content on top; a BOTTOM brass-ruled menu strip with uppercase serif entries,
 *  a large serif clock at the END and the source name at the START (like a home-theater display). */
@Composable
internal fun MediaCenterShell(p: ShellParams) {
    McBackground(p.modifier) {
        Column(Modifier.fillMaxSize()) {
            Column(Modifier.weight(1f).fillMaxWidth().padding(p.contentPadding).padding(start = 40.dp, end = 40.dp, top = 22.dp)) {
                if (p.showScreenHeader || p.topBarActions != null) {
                    Row(Modifier.fillMaxWidth().padding(bottom = 14.dp), verticalAlignment = Alignment.Bottom) {
                        if (p.showScreenHeader) Column(Modifier.weight(1f)) {
                            McHeading(p.subtitle?.takeIf { it.isNotBlank() } ?: tr("Media Center", "مركز الوسائط"), size = 11, color = MC.Faint)
                            Text(p.title, color = MC.Text, fontSize = 30.sp, fontFamily = MC.Serif, maxLines = 1)
                        } else Spacer(Modifier.weight(1f))
                        p.topBarActions?.let { Row(horizontalArrangement = Arrangement.spacedBy(10.dp), content = it) }
                    }
                }
                p.header?.let { Column(content = it) }
                Column(Modifier.fillMaxSize(), content = p.content)
            }
            if (p.topBarVisible) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, MC.Amber, Color.Transparent))))
                Row(
                    Modifier.fillMaxWidth().background(Color(0xFF0D0A06)).padding(horizontal = 32.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("✦", color = MC.Amber, fontSize = 18.sp, modifier = Modifier.padding(end = 14.dp))
                    mcDestinations.forEach { d ->
                        val active = onRoute(p.currentRoute, d.route)
                        McTab(tr(d.en, d.ar), active, { if (!active) p.onNavigate(d.route) })
                    }
                    Spacer(Modifier.weight(1f))
                    Text(mcClock(System.currentTimeMillis()), color = MC.Amber, fontSize = 26.sp, fontFamily = MC.Serif)
                }
            }
        }
    }
}

/** Home: receiver main menu. START = big serif vertical menu over fanart; END = the focused section's
 *  "widgets": feature card, continue-watching list (vertical, receiver style), channel plates and poster shelves. */
@Composable
internal fun MediaCenterDashboard(p: DashboardParams) {
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
    val tFav = tr("Favorite channels", "القنوات المفضلة")
    val tRecCh = tr("Recent channels", "القنوات الأخيرة")
    val tRec = tr("Recommended", "مقترح")
    val tTop = tr("Top rated", "الأعلى تقييماً")
    val tNew = tr("Recently added", "أضيف حديثاً")
    Box(Modifier.fillMaxSize()) {
        s.feature.artworkUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(MC.R)) }
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(MC.Bg, MC.Bg.copy(alpha = 0.92f), MC.Bg.copy(alpha = 0.55f)))))
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(36.dp)) {
            Column(Modifier.width(300.dp).fillMaxHeight().padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                McHeading(s.provider?.name ?: tr("Media Center", "مركز الوسائط"), size = 11, color = MC.Faint)
                Spacer(Modifier.height(10.dp))
                listOf(
                    Triple(tr("Live TV", "البث المباشر"), "${s.stats.liveChannelCount}", Routes.LIVE_TV),
                    Triple(tr("Movies", "أفلام"), "${s.stats.movieLibraryCount}", Routes.MOVIES),
                    Triple(tr("Series", "مسلسلات"), "${s.stats.seriesLibraryCount}", Routes.SERIES),
                    Triple(tr("TV Guide", "دليل البرامج"), "", Routes.EPG),
                    Triple(tr("Favorites", "المفضلة"), "", Routes.FAVORITES),
                    Triple(tr("Search", "بحث"), "", Routes.SEARCH)
                ).forEachIndexed { i, (label, count, route) ->
                    McCard(onClick = { p.onNavigate(route) }, container = Color.Transparent, modifier = Modifier.fillMaxWidth().then(if (i == 0) Modifier.focusRequester(first) else Modifier)) {
                        Row(Modifier.padding(start = 18.dp, end = 12.dp, top = 10.dp, bottom = 10.dp), verticalAlignment = Alignment.Bottom) {
                            Text(label, color = MC.Text, fontSize = 28.sp, fontFamily = MC.Serif, maxLines = 1, modifier = Modifier.weight(1f))
                            if (count.isNotBlank() && count != "0") Text(count, color = MC.Amber, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            LazyColumn(Modifier.weight(1f).fillMaxHeight(), contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.widthIn(max = 640.dp)) {
                        McHeading(tr("Now showing", "يعرض الآن"), size = 11)
                        Text(s.feature.title.ifBlank { s.provider?.name ?: "Media Center" }, color = MC.Text, fontSize = 38.sp, fontFamily = MC.Serif, maxLines = 2, lineHeight = 42.sp)
                        Text(s.feature.summary, color = MC.Sub, fontSize = 14.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 4.dp)) {
                            McButton(s.feature.actionLabel.ifBlank { tr("Play", "تشغيل") }, featureAction, primary = true, icon = "▶")
                            McButton(tr("Guide", "الدليل"), { p.onNavigate(Routes.EPG) })
                        }
                    }
                }
                if (s.continueWatching.isNotEmpty()) item {
                    Column {
                        McRowTitle(tr("Continue watching", "متابعة المشاهدة"), trailing = "${s.continueWatching.size}")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            items(s.continueWatching, key = { "cw${it.id}" }) { h ->
                                val prog = if (h.totalDurationMs > 0) h.resumePositionMs.toFloat() / h.totalDurationMs else null
                                val left = if (h.totalDurationMs > 0) mcDuration(h.totalDurationMs - h.resumePositionMs) + " " + tr("left", "متبقي") else null
                                McWide(h.title, h.posterUrl, left, prog, { p.onContinueWatchingItemClick(h) }, Modifier.width(250.dp))
                            }
                        }
                    }
                }
                listOf(
                    Triple(tFav, s.favoriteChannels, p.onFavoriteChannelClick),
                    Triple(tRecCh, s.recentChannels, p.onRecentChannelClick)
                ).forEach { (title, channels, click) ->
                    if (channels.isNotEmpty()) item {
                        Column {
                            McRowTitle(title, trailing = "${channels.size}")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(channels, key = { "$title${it.id}" }) { c -> McChannelPlate(c, c.id in p.recordingChannelIds) { click(c, s.currentCombinedProfileId) } }
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
                            McRowTitle(title)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                items(movies, key = { "$title${it.id}" }) { m -> McPoster(m.name, m.posterUrl, m.year, { p.onMovieClick(m) }, Modifier.width(130.dp)) }
                            }
                        }
                    }
                }
                if (s.recentSeries.isNotEmpty()) item {
                    Column {
                        McRowTitle(tr("New series", "مسلسلات جديدة"))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            items(s.recentSeries, key = { "rs${it.id}" }) { m -> McPoster(m.name, m.posterUrl, m.genre, { p.onSeriesClick(m) }, Modifier.width(130.dp)) }
                        }
                    }
                }
            }
        }
    }
}

/** Compact horizontal plate: framed logo + number/name, like a receiver preset button. */
@Composable
private fun McChannelPlate(c: Channel, recording: Boolean, onClick: () -> Unit) {
    McCard(onClick = onClick, container = MC.Raised, modifier = Modifier.width(230.dp)) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            McLogo(c.name, c.logoUrl, 44.dp)
            Column(Modifier.weight(1f)) {
                Text((if (c.number > 0) "${c.number}  " else "") + c.name, color = MC.Text, fontSize = 13.sp, fontFamily = MC.Serif, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(c.currentProgram?.title ?: "—", color = MC.Faint, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (recording) McBadge("REC", MC.Live, filled = true)
        }
    }
}

/** Live TV: three receiver columns. Categories (plain serif list w/ counts) | channel ledger (number, name,
 *  now title, timeline; separated by brass rules) | END tall preview column with now/next "program card". */
@Composable
internal fun MediaCenterLiveTv(p: LiveTvParams) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        Column(Modifier.width(230.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            McHeading(tr("Groups", "المجموعات"), size = 12)
            McSearchField(p.categorySearchQuery, p.onCategorySearchChange, tr("Filter", "تصفية"), Modifier.fillMaxWidth())
            LazyColumn(verticalArrangement = Arrangement.spacedBy(1.dp), contentPadding = PaddingValues(vertical = 6.dp)) {
                items(p.categories, key = { it.id }) { cat ->
                    val sel = cat.id == p.selectedCategoryId
                    McCard(
                        onClick = { p.onCategoryClick(cat) }, onLongClick = { p.onCategoryLongClick(cat) }, shape = MC.RSmall,
                        container = if (sel) Color(0xFF33281A) else Color.Transparent,
                        modifier = Modifier.fillMaxWidth().focusRequester(p.categoryRequester(cat.id)).onFocusChanged { if (it.isFocused) p.onCategoryFocused(cat) }
                            .mcRight { p.onRequestChannelsFromCategory() }
                    ) {
                        Row(Modifier.padding(start = 14.dp, end = 10.dp, top = 9.dp, bottom = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text((if (p.isCategoryLocked(cat)) "🔒 " else "") + cat.name, color = if (sel) MC.Amber else MC.Text, fontSize = 15.sp, fontFamily = MC.Serif, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            if (cat.count > 0) Text("${cat.count}", color = MC.Faint, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                McHeading(p.sourceTitle.ifBlank { tr("Channels", "القنوات") } + "  ·  ${p.channels.size}", size = 12, modifier = Modifier.weight(1f))
                McSearchField(p.channelSearchQuery, p.onChannelSearchChange, tr("Find", "بحث"), Modifier.width(220.dp))
            }
            LazyColumn(contentPadding = PaddingValues(vertical = 6.dp)) {
                items(p.channels, key = { it.id }) { c ->
                    val locked = p.isChannelLocked(c)
                    val moving = c.id == p.movingChannelId
                    McCard(
                        onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c) }, shape = MC.RSmall,
                        container = when { moving -> MC.AmberDeep.copy(alpha = 0.45f); c.id == p.previewChannel?.id -> Color(0xFF2A2116); else -> Color.Transparent },
                        modifier = Modifier.fillMaxWidth().focusRequester(p.channelRequester(c.id)).onFocusChanged { if (it.isFocused) p.onChannelFocused(c) }
                            .mcRight { p.onRequestPreviewFromChannel() }
                    ) {
                        Column {
                            Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(if (c.number > 0) "%03d".format(c.number) else "···", color = MC.Amber, fontSize = 14.sp, fontFamily = MC.Serif, modifier = Modifier.width(40.dp))
                                McLogo(c.name, if (locked) null else c.logoUrl, 38.dp)
                                Column(Modifier.weight(1f)) {
                                    Text(if (moving) "⇅  ${c.name}" else c.name, color = MC.Text, fontSize = 15.sp, fontFamily = MC.Serif, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(if (locked) tr("Locked", "مقفل") else c.currentProgram?.let { "${mcClock(it.startTime)}  ${it.title}" } ?: "—", color = MC.Faint, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                if (!locked) c.currentProgram?.let { Box(Modifier.width(70.dp)) { McProgress((System.currentTimeMillis() - it.startTime).toFloat() / (it.endTime - it.startTime).coerceAtLeast(1), height = 3.dp) } }
                                if (!locked) c.qualityBadge()?.let { McBadge(it, MC.Sub) }
                                if (c.catchUpSupported) McBadge(tr("Archive", "أرشيف"), MC.Blue)
                                Text(if (c.isFavorite) "★" else "☆", color = if (c.isFavorite) MC.Amber else MC.Line, fontSize = 15.sp)
                            }
                            Box(Modifier.fillMaxWidth().height(1.dp).background(MC.Line.copy(alpha = 0.5f)))
                        }
                    }
                }
            }
        }
        Column(Modifier.width(330.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            McCard(
                onClick = { p.previewChannel?.let(p.onChannelClick) }, container = Color.Black, shape = MC.RSmall,
                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).focusRequester(p.previewFocusRequester).mcLeft { p.onRequestChannelsFromPreview() }
            ) {
                val engine = p.previewPlayerEngine
                if (engine != null && p.previewChannel != null) PlayerRenderView(engine, PlayerSurfaceResizeMode.FIT, Modifier.fillMaxSize())
                when {
                    p.previewErrorMessage != null -> Text(p.previewErrorMessage, color = MC.Live, fontSize = 12.sp, modifier = Modifier.align(Alignment.Center).padding(12.dp))
                    p.isPreviewLoading -> Text(tr("Tuning…", "جار الضبط…"), color = MC.Sub, fontFamily = MC.Serif, modifier = Modifier.align(Alignment.Center))
                    p.previewChannel == null -> Text(tr("No signal", "لا إشارة"), color = MC.Faint, fontFamily = MC.Serif, modifier = Modifier.align(Alignment.Center))
                }
            }
            val c = p.previewChannel
            if (c != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    McLogo(c.name, c.logoUrl, 42.dp)
                    Column(Modifier.weight(1f)) {
                        Text(c.name, color = MC.Text, fontSize = 18.sp, fontFamily = MC.Serif, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            McBadge("Live", MC.Live, filled = true)
                            if (c.number > 0) McBadge("CH ${c.number}", MC.Sub)
                            c.qualityBadge()?.let { McBadge(it, MC.Sub) }
                        }
                    }
                }
                Column(Modifier.fillMaxWidth().background(MC.Raised, MC.R).border(1.dp, MC.Line, MC.R).padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    McHeading(tr("Now", "الآن"), size = 10)
                    c.currentProgram?.let { now ->
                        Text(now.title, color = MC.Text, fontSize = 15.sp, fontFamily = MC.Serif, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(mcClock(now.startTime), color = MC.Sub, fontSize = 11.sp)
                            McProgress((System.currentTimeMillis() - now.startTime).toFloat() / (now.endTime - now.startTime).coerceAtLeast(1), Modifier.weight(1f))
                            Text(mcClock(now.endTime), color = MC.Sub, fontSize = 11.sp)
                        }
                        Text(now.description, color = MC.Faint, fontSize = 12.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    } ?: Text(tr("No guide data", "لا يوجد دليل"), color = MC.Faint, fontSize = 13.sp)
                    Box(Modifier.fillMaxWidth().height(1.dp).background(MC.Line))
                    McHeading(tr("Next", "التالي"), size = 10)
                    Text(c.nextProgram?.let { "${mcClock(it.startTime)}  ${it.title}" } ?: "—", color = MC.Sub, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}
