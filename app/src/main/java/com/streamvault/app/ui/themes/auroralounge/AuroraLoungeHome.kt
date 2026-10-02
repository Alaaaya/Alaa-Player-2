package com.streamvault.app.ui.themes.auroralounge

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

/** AuroraLounge's own rounded search field: magnifier inside a soft pill, white outline on focus. */
@Composable
internal fun AlSearchField(value: String, onChange: (String) -> Unit, hint: String, modifier: Modifier = Modifier, onSubmit: (() -> Unit)? = null) {
    var focused by remember { mutableStateOf(false) }
    Row(
        modifier.clip(AL.Pill).background(if (focused) AL.Line else AL.Card.copy(alpha = 0.7f)).border(1.5.dp, if (focused) AL.Amber else AL.Line, AL.Pill)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("⌕", color = if (focused) AL.Amber else AL.Faint, fontSize = 16.sp)
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) Text(hint, color = AL.Faint, fontSize = 14.sp, maxLines = 1)
            BasicTextField(
                value = value, onValueChange = onChange, singleLine = true,
                textStyle = TextStyle(color = AL.Text, fontSize = 14.sp), cursorBrush = SolidColor(AL.Amber),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { onSubmit?.invoke() }, onDone = { onSubmit?.invoke() }),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                modifier = Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused }
            )
        }
    }
}

private class AlDest(val route: String, val glyph: String, val en: String, val ar: String)

private val alDestinations = listOf(
    AlDest(Routes.SEARCH, "⌕", "Search", "بحث"),
    AlDest(Routes.HOME, "⌂", "Home", "الرئيسية"),
    AlDest(Routes.LIVE_TV, "◉", "Live TV", "البث المباشر"),
    AlDest(Routes.EPG, "▦", "Guide", "الدليل"),
    AlDest(Routes.MOVIES, "▶", "Movies", "أفلام"),
    AlDest(Routes.SERIES, "❏", "Series", "مسلسلات"),
    AlDest(Routes.FAVORITES, "♥", "My List", "قائمتي"),
    AlDest(Routes.SETTINGS, "⚙", "Settings", "الإعدادات")
)

private fun onRoute(current: String, route: String) = current == route || current.startsWith("$route?") || current.startsWith("$route/")

/** Shell: a floating lounge bar across the TOP. Crescent wordmark start, centred lamp-pill destinations, clock end. */
@Composable
internal fun AuroraLoungeShell(p: ShellParams) {
    AlBackground(p.modifier) {
        Column(Modifier.fillMaxSize()) {
            if (p.topBarVisible) {
                Row(Modifier.fillMaxWidth().padding(start = 40.dp, end = 40.dp, top = 20.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.size(36.dp).clip(AL.Pill).background(Brush.linearGradient(listOf(AL.Rose, AL.Amber))), contentAlignment = Alignment.Center) { Text("☾", color = AL.Bg, fontSize = 20.sp, fontWeight = FontWeight.Black) }
                        Text("aurora", color = AL.Text, fontSize = 20.sp, fontWeight = FontWeight.Light, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif)
                    }
                    Row(
                        Modifier.clip(AL.Pill).background(AL.Raised.copy(alpha = 0.85f)).border(1.dp, AL.Line, AL.Pill).padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically
                    ) {
                        alDestinations.forEach { d ->
                            val active = onRoute(p.currentRoute, d.route)
                            AlTab(if (d.route == Routes.SEARCH) d.glyph else tr(d.en, d.ar), active, { if (!active) p.onNavigate(d.route) })
                        }
                    }
                    Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                        Text(alClock(System.currentTimeMillis()), color = AL.Amber, fontSize = 22.sp, fontWeight = FontWeight.Light, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif)
                    }
                }
            }
            Column(Modifier.fillMaxSize().padding(p.contentPadding).padding(start = 40.dp, end = 40.dp, top = 14.dp)) {
                if (p.showScreenHeader || p.topBarActions != null) {
                    Row(Modifier.fillMaxWidth().padding(bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (p.showScreenHeader) Column(Modifier.weight(1f)) {
                            Text(p.title, color = AL.Text, fontSize = 32.sp, fontWeight = FontWeight.Light, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, maxLines = 1)
                            p.subtitle?.takeIf { it.isNotBlank() }?.let { Text(it, color = AL.Faint, fontSize = 14.sp, maxLines = 1) }
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

/** Home: an evening greeting, then a split "lounge table": the feature as a tall rounded window on the start side and
 *  continue-watching stacked as cushions on the end side; below, a row of channel moons and serif-titled shelves. */
@Composable
internal fun AuroraLoungeDashboard(p: DashboardParams) {
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
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val greet = when (hour) { in 5..11 -> tr("Good morning", "صباح الخير"); in 12..17 -> tr("Good afternoon", "نهارك سعيد"); else -> tr("Good evening", "مساء الخير") }
    val tYour = tr("Your channels", "قنواتك")
    val tRecentCh = tr("Recently watched channels", "شوهدت مؤخراً")
    val tRec = tr("Recommended for you", "مقترح لك")
    val tTop = tr("Top rated", "الأعلى تقييماً")
    val tNew = tr("New arrivals", "وصل حديثاً")
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 48.dp), verticalArrangement = Arrangement.spacedBy(30.dp)) {
        item {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(greet, color = AL.Text, fontSize = 34.sp, fontWeight = FontWeight.Light, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif)
                Text("${s.stats.liveChannelCount} " + tr("channels", "قناة") + "  ·  ${s.stats.movieLibraryCount} " + tr("movies", "فيلم") + "  ·  ${s.stats.seriesLibraryCount} " + tr("series", "مسلسل"),
                    color = AL.Faint, fontSize = 13.sp, modifier = Modifier.padding(bottom = 6.dp))
            }
        }
        item {
            Row(Modifier.fillMaxWidth().height(340.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Box(Modifier.weight(1.6f).fillMaxHeight().clip(AL.R).background(AL.Raised)) {
                    s.feature.artworkUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(AL.Rose.copy(alpha = 0.15f), Color.Transparent, AL.Bg.copy(alpha = 0.95f)))))
                    Column(Modifier.align(Alignment.BottomStart).padding(30.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            AlBadge("✦ " + tr("TONIGHT", "الليلة"), AL.Amber, filled = true)
                            s.provider?.name?.let { Text(it, color = AL.Sub, fontSize = 13.sp) }
                        }
                        Text(s.feature.title.ifBlank { s.provider?.name ?: "Aurora" }, color = AL.Text, fontSize = 38.sp, fontWeight = FontWeight.Light, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, maxLines = 2, lineHeight = 42.sp)
                        Text(s.feature.summary, color = AL.Sub, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 560.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 4.dp)) {
                            AlButton(s.feature.actionLabel.ifBlank { tr("Watch now", "شاهد الآن") }, featureAction, Modifier.focusRequester(first), primary = true, icon = "▶")
                            AlButton(tr("Live TV", "البث المباشر"), { p.onNavigate(Routes.LIVE_TV) }, icon = "◉")
                            AlButton(tr("Guide", "الدليل"), { p.onNavigate(Routes.EPG) }, icon = "▦")
                        }
                    }
                }
                Column(Modifier.weight(1f).fillMaxHeight().clip(AL.R).background(AL.Raised.copy(alpha = 0.7f)).border(1.dp, AL.Line, AL.R).padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AlRowTitle(tr("Continue watching", "متابعة المشاهدة"), trailing = if (s.continueWatching.isNotEmpty()) "${s.continueWatching.size}" else null)
                    if (s.continueWatching.isEmpty()) Text(tr("Nothing in progress. Pour a drink and start something.", "لا يوجد شيء قيد المشاهدة بعد."), color = AL.Faint, fontSize = 13.sp)
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(s.continueWatching, key = { "cw${it.id}" }) { h ->
                            val prog = if (h.totalDurationMs > 0) h.resumePositionMs.toFloat() / h.totalDurationMs else null
                            AlCard(onClick = { p.onContinueWatchingItemClick(h) }, shape = AL.RSmall, container = AL.Card, modifier = Modifier.fillMaxWidth()) {
                                Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Box(Modifier.width(96.dp).aspectRatio(16f / 9f).clip(RoundedCornerShape(14.dp)).background(AL.Line)) {
                                        h.posterUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                                    }
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(h.title, color = AL.Text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        if (h.totalDurationMs > 0) Text(alDuration(h.totalDurationMs - h.resumePositionMs) + " " + tr("left", "متبقي"), color = AL.Faint, fontSize = 11.sp)
                                        prog?.let { AlProgress(it, height = 3.dp) }
                                    }
                                }
                            }
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
                    AlRowTitle(title, trailing = "${channels.size}")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(22.dp), contentPadding = PaddingValues(vertical = 10.dp, horizontal = 6.dp)) {
                        items(channels, key = { "$title${it.id}" }) { c -> AlChannelMoon(c, c.id in p.recordingChannelIds) { click(c, s.currentCombinedProfileId) } }
                    }
                }
            }
        }
        listOf(tRec to s.recommendedMovies, tTop to s.topRatedMovies, tNew to s.recentMovies).forEach { (title, movies) ->
            if (movies.isNotEmpty()) item {
                Column {
                    AlRowTitle(title)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(vertical = 10.dp, horizontal = 6.dp)) {
                        items(movies, key = { "$title${it.id}" }) { m -> AlPoster(m.name, m.posterUrl, m.year, { p.onMovieClick(m) }, Modifier.width(150.dp)) }
                    }
                }
            }
        }
        if (s.recentSeries.isNotEmpty()) item {
            Column {
                AlRowTitle(tr("New series", "مسلسلات جديدة"))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(vertical = 10.dp, horizontal = 6.dp)) {
                    items(s.recentSeries, key = { "rs${it.id}" }) { m -> AlPoster(m.name, m.posterUrl, m.genre, { p.onSeriesClick(m) }, Modifier.width(150.dp)) }
                }
            }
        }
    }
}

/** Channel moon: a big circular logo that blooms gold on focus, name and now-title centred beneath. */
@Composable
private fun AlChannelMoon(c: Channel, recording: Boolean, onClick: () -> Unit) {
    val now = c.currentProgram
    Column(Modifier.width(132.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        AlCard(onClick = onClick, shape = CircleShape, container = AL.Raised, zoom = 1.1f, modifier = Modifier.size(104.dp)) {
            AlLogo(c.name, c.logoUrl, 84.dp, Modifier.align(Alignment.Center))
            if (recording) Box(Modifier.align(Alignment.TopEnd).padding(10.dp).size(12.dp).clip(CircleShape).background(AL.Live))
            now?.let {
                val f = ((System.currentTimeMillis() - it.startTime).toFloat() / (it.endTime - it.startTime).coerceAtLeast(1)).coerceIn(0f, 1f)
                androidx.compose.foundation.Canvas(Modifier.fillMaxSize().padding(3.dp)) {
                    drawArc(AL.Amber, -90f, 360f * f, false, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
                }
            }
        }
        Text(c.name, color = AL.Text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(now?.title ?: (if (c.number > 0) "CH ${c.number}" else ""), color = AL.Faint, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Live TV: three lounge columns. Start = category pills, middle = channel moons list, end = a rounded "window" preview
 *  with the now/next programme card underneath. No grid, no tabs. */
@Composable
internal fun AuroraLoungeLiveTv(p: LiveTvParams) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(22.dp)) {
        Column(Modifier.width(230.dp).fillMaxHeight().clip(AL.R).background(AL.Raised.copy(alpha = 0.75f)).padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(tr("Rooms", "الغرف"), color = AL.Amber, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, modifier = Modifier.padding(start = 6.dp))
            AlSearchField(p.categorySearchQuery, p.onCategorySearchChange, tr("Categories", "التصنيفات"), Modifier.fillMaxWidth())
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(p.categories, key = { it.id }) { cat ->
                    val sel = cat.id == p.selectedCategoryId
                    AlCard(
                        onClick = { p.onCategoryClick(cat) }, onLongClick = { p.onCategoryLongClick(cat) }, shape = AL.Pill, zoom = 1.03f,
                        container = if (sel) AL.Line else Color.Transparent, focusedContainer = AL.Line,
                        modifier = Modifier.fillMaxWidth().focusRequester(p.categoryRequester(cat.id)).onFocusChanged { if (it.isFocused) p.onCategoryFocused(cat) }
                            .alRight { p.onRequestChannelsFromCategory() }
                    ) {
                        Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.size(7.dp).clip(CircleShape).background(if (sel) AL.Amber else AL.Line))
                            Text((if (p.isCategoryLocked(cat)) "🔒 " else "") + cat.name, color = if (sel) AL.Amber else AL.Text, fontSize = 14.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            if (cat.count > 0) Text("${cat.count}", color = AL.Faint, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(p.sourceTitle.ifBlank { tr("Live TV", "البث المباشر") }, color = AL.Text, fontSize = 22.sp, fontWeight = FontWeight.Light, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, maxLines = 1, modifier = Modifier.weight(1f))
                Text("${p.channels.size}", color = AL.Amber, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 10.dp))
            }
            AlSearchField(p.channelSearchQuery, p.onChannelSearchChange, tr("Find a channel", "ابحث عن قناة"), Modifier.fillMaxWidth())
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 6.dp, horizontal = 4.dp)) {
                items(p.channels, key = { it.id }) { c ->
                    val locked = p.isChannelLocked(c)
                    val previewing = c.id == p.previewChannel?.id
                    AlCard(
                        onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c) }, shape = AL.Pill, zoom = 1.03f,
                        container = if (previewing) AL.Line else AL.Raised.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth().focusRequester(p.channelRequester(c.id)).onFocusChanged { if (it.isFocused) p.onChannelFocused(c) }
                            .alRight { p.onRequestPreviewFromChannel() }
                    ) {
                        Row(Modifier.padding(start = 6.dp, end = 18.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            AlLogo(c.name, if (locked) null else c.logoUrl, 48.dp)
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text((if (c.id == p.movingChannelId) "⇅  " else "") + (if (c.number > 0) "${c.number}  " else "") + c.name, color = AL.Text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(if (locked) tr("Locked", "مقفل") else c.currentProgram?.title ?: tr("No guide data", "لا يوجد دليل"), color = AL.Faint, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            if (!locked) c.qualityBadge()?.let { AlBadge(it, AL.Blue) }
                            if (c.catchUpSupported) Text("⟲", color = AL.Blue, fontSize = 14.sp)
                            if (c.isFavorite) Text("♥", color = AL.Rose, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
        Column(Modifier.weight(1.15f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            AlCard(
                onClick = { p.previewChannel?.let(p.onChannelClick) }, container = Color.Black, zoom = 1.02f,
                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).focusRequester(p.previewFocusRequester).alLeft { p.onRequestChannelsFromPreview() }
            ) {
                val engine = p.previewPlayerEngine
                if (engine != null && p.previewChannel != null) PlayerRenderView(engine, PlayerSurfaceResizeMode.FIT, Modifier.fillMaxSize())
                when {
                    p.previewErrorMessage != null -> Text(p.previewErrorMessage, color = AL.Live, fontSize = 13.sp, modifier = Modifier.align(Alignment.Center).padding(16.dp))
                    p.isPreviewLoading -> Text(tr("Loading…", "جار التحميل…"), color = AL.Sub, modifier = Modifier.align(Alignment.Center))
                    p.previewChannel == null -> Text("☾  " + tr("Pick a channel to preview", "اختر قناة للمعاينة"), color = AL.Faint, modifier = Modifier.align(Alignment.Center))
                }
                if (p.previewChannel != null) Box(Modifier.align(Alignment.TopStart).padding(14.dp)) { AlBadge("● LIVE", AL.Live, filled = true) }
            }
            val c = p.previewChannel
            Column(Modifier.fillMaxWidth().weight(1f).clip(AL.R).background(Brush.verticalGradient(listOf(AL.Raised, AL.Card.copy(alpha = 0.6f)))).border(1.dp, AL.Line, AL.R).padding(22.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (c == null) Text(tr("Your evening, live.", "سهرتك، مباشرة."), color = AL.Sub, fontSize = 22.sp, fontWeight = FontWeight.Light, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif)
                else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AlLogo(c.name, c.logoUrl, 44.dp)
                        Column(Modifier.weight(1f)) {
                            Text(c.name, color = AL.Text, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (c.number > 0) AlBadge("CH ${c.number}", AL.Sub)
                                c.qualityBadge()?.let { AlBadge(it, AL.Blue) }
                                if (c.catchUpSupported) AlBadge(tr("CATCH-UP", "أرشيف"), AL.Blue)
                            }
                        }
                    }
                    c.currentProgram?.let { now ->
                        Text(tr("NOW", "الآن"), color = AL.Amber, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Text(now.title, color = AL.Text, fontSize = 18.sp, fontWeight = FontWeight.Light, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, maxLines = 1)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(alClock(now.startTime), color = AL.Faint, fontSize = 12.sp)
                            AlProgress((System.currentTimeMillis() - now.startTime).toFloat() / (now.endTime - now.startTime).coerceAtLeast(1), Modifier.weight(1f))
                            Text(alClock(now.endTime), color = AL.Faint, fontSize = 12.sp)
                        }
                        Text(now.description, color = AL.Faint, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    } ?: Text(tr("No guide data", "لا يوجد دليل"), color = AL.Faint)
                    c.nextProgram?.let { Text(tr("LATER", "لاحقاً") + "   ${alClock(it.startTime)}  ${it.title}", color = AL.Sub, fontSize = 13.sp, maxLines = 1) }
                }
            }
        }
    }
}
