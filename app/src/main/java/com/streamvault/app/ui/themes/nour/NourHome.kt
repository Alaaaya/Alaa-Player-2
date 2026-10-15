package com.streamvault.app.ui.themes.nour

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

@Composable
internal fun NourSearchField(value: String, onChange: (String) -> Unit, hint: String, modifier: Modifier = Modifier, onSubmit: (() -> Unit)? = null) {
    var focused by remember { mutableStateOf(false) }
    Row(
        modifier.clip(NR.Pill).background(if (focused) NR.Line else NR.Card.copy(alpha = 0.7f)).border(1.5.dp, if (focused) NR.Amber else NR.Line, NR.Pill)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("⌕", color = if (focused) NR.Amber else NR.Faint, fontSize = 16.sp)
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) Text(hint, color = NR.Faint, fontSize = 14.sp, maxLines = 1)
            BasicTextField(
                value = value, onValueChange = onChange, singleLine = true,
                textStyle = TextStyle(color = NR.Text, fontSize = 14.sp), cursorBrush = SolidColor(NR.Amber),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { onSubmit?.invoke() }, onDone = { onSubmit?.invoke() }),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                modifier = Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused }
            )
        }
    }
}

private class NourDest(val route: String, val glyph: String, val en: String, val ar: String)

private val nourDestinations = listOf(
    NourDest(Routes.SEARCH, "⌕", "Search", "بحث"),
    NourDest(Routes.HOME, "⌂", "Home", "الرئيسية"),
    NourDest(Routes.MOVIES, "▶", "Movies", "الأفلام"),
    NourDest(Routes.SERIES, "▤", "Series", "المسلسلات"),
    NourDest(Routes.LIVE_TV, "◉", "Live TV", "مباشر"),
    NourDest(Routes.FAVORITES, "♡", "My List", "قائمتي"),
    NourDest(Routes.EPG, "▦", "Guide", "الدليل"),
    NourDest(Routes.SETTINGS, "⚙", "Settings", "الإعدادات")
)

private fun onRoute(current: String, route: String) = current == route || current.startsWith("$route?") || current.startsWith("$route/")

@Composable
internal fun NourShell(p: ShellParams) {
    NourBackground(p.modifier) {
        Column(Modifier.fillMaxSize().padding(p.contentPadding)) {
            if (p.topBarVisible) Spacer(Modifier.height(80.dp))
            if (p.showScreenHeader || p.topBarActions != null) Row(
                Modifier.fillMaxWidth().padding(horizontal = 36.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (p.showScreenHeader) Text(p.title, color = NR.Text, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                else Spacer(Modifier.weight(1f))
                p.topBarActions?.let { Row(horizontalArrangement = Arrangement.spacedBy(8.dp), content = it) }
            }
            p.header?.let { Column(content = it) }
            Column(Modifier.fillMaxSize(), content = p.content)
        }
        if (p.topBarVisible) Row(
            Modifier.align(Alignment.TopCenter).padding(top = 18.dp)
                .clip(NR.Pill).background(Brush.verticalGradient(listOf(Color(0xE638434C), Color(0xD51B242C))))
                .border(1.dp, Color(0x40FFFFFF), NR.Pill).padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            nourDestinations.forEach { d ->
                NourTab(if (d.route in listOf(Routes.SEARCH, Routes.SETTINGS, Routes.EPG, Routes.FAVORITES)) d.glyph else tr(d.en, d.ar),
                    onRoute(p.currentRoute, d.route), { if (!onRoute(p.currentRoute, d.route)) p.onNavigate(d.route) })
            }
        }
    }
}

@Composable
internal fun NourDashboard(p: DashboardParams) {
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
    val tRecentCh = tr("Recently watched channels", "قنوات شاهدتها مؤخراً")
    val tRec = tr("Trending movies", "أفلام رائجة")
    val tTop = tr("Top rated", "الأعلى تقييماً")
    val tNew = tr("New arrivals", "وصل حديثاً")
    val features = remember(s.recommendedMovies, s.recentMovies) {
        (s.recommendedMovies + s.recentMovies).distinctBy { it.id }.take(5)
    }
    var heroIndex by remember { mutableIntStateOf(0) }
    var heroHasFocus by remember { mutableStateOf(false) }
    LaunchedEffect(features, heroHasFocus) {
        if (features.size > 1 && !heroHasFocus) while (true) {
            kotlinx.coroutines.delay(8000)
            heroIndex = (heroIndex + 1) % features.size
        }
    }
    val movie = features.getOrNull(heroIndex % features.size.coerceAtLeast(1))
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 48.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        item {
            Box(Modifier.fillMaxWidth().height(370.dp).onFocusChanged { heroHasFocus = it.hasFocus }) {
                androidx.compose.animation.Crossfade(targetState = movie?.backdropUrl ?: movie?.posterUrl ?: s.feature.artworkUrl,
                    animationSpec = androidx.compose.animation.core.tween(1100), label = "nourHero") { url ->
                    url?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                }
                Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(NR.Bg.copy(alpha = 0.92f), NR.Bg.copy(alpha = 0.3f), Color.Transparent))))
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, NR.Bg))))
                Column(Modifier.align(Alignment.CenterStart).padding(horizontal = 42.dp).widthIn(max = 610.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(tr("FEATURED", "مختارات"), color = NR.Sub, fontSize = 11.sp, letterSpacing = 3.sp, fontWeight = FontWeight.SemiBold)
                    Text((movie?.name ?: s.feature.title.ifBlank { "NOUR" }).uppercase(), color = NR.Text, fontSize = 48.sp,
                        fontWeight = FontWeight.Black, fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                        lineHeight = 51.sp, letterSpacing = (-1.5).sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(movie?.plot ?: s.feature.summary, color = NR.Sub, fontSize = 14.sp, maxLines = 3,
                        lineHeight = 20.sp, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 460.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        NourButton(tr("Details", "التفاصيل"), { movie?.let(p.onMovieClick) ?: featureAction() }, Modifier.focusRequester(first), primary = true, icon = "▶")
                        NourButton(tr("My list", "قائمتي"), { p.onNavigate(Routes.FAVORITES) }, icon = "♡")
                    }
                }
                Row(Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    features.forEachIndexed { i, _ ->
                        NourCard({ heroIndex = i }, shape = NR.Pill, container = if (i == heroIndex) NR.Text else NR.Line,
                            modifier = Modifier.width(if (i == heroIndex) 24.dp else 12.dp).height(6.dp)) {}
                    }
                }
            }
        }
        if (s.continueWatching.isNotEmpty()) item {
            Column(Modifier.padding(horizontal = 36.dp)) {
                NourRowTitle(tr("Continue watching", "متابعة المشاهدة"))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(8.dp)) {
                    items(s.continueWatching, key = { it.id }) { h ->
                        NourWide(h.title, h.posterUrl, if (h.totalDurationMs > 0) nourDuration((h.totalDurationMs - h.resumePositionMs).coerceAtLeast(0)) + " " + tr("left", "متبقي") else null,
                            if (h.totalDurationMs > 0) h.resumePositionMs.toFloat() / h.totalDurationMs else null,
                            { p.onContinueWatchingItemClick(h) }, Modifier.width(240.dp))
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
                    NourRowTitle(title, trailing = "${channels.size}")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(22.dp), contentPadding = PaddingValues(vertical = 10.dp, horizontal = 36.dp)) {
                        items(channels, key = { "$title${it.id}" }) { c -> NourChannelMoon(c, c.id in p.recordingChannelIds) { click(c, s.currentCombinedProfileId) } }
                    }
                }
            }
        }
        listOf(tRec to s.recommendedMovies, tTop to s.topRatedMovies, tNew to s.recentMovies).forEach { (title, movies) ->
            if (movies.isNotEmpty()) item {
                Column {
                    NourRowTitle(title, Modifier.padding(horizontal = 36.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(vertical = 10.dp, horizontal = 36.dp)) {
                        items(movies, key = { "$title${it.id}" }) { m -> NourPoster(m.name, m.posterUrl, m.year, { p.onMovieClick(m) }, Modifier.width(150.dp)) }
                    }
                }
            }
        }
        if (s.recentSeries.isNotEmpty()) item {
            Column {
                NourRowTitle(tr("New series", "مسلسلات جديدة"))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(vertical = 10.dp, horizontal = 36.dp)) {
                    items(s.recentSeries, key = { "rs${it.id}" }) { m -> NourPoster(m.name, m.posterUrl, m.genre, { p.onSeriesClick(m) }, Modifier.width(150.dp)) }
                }
            }
        }
    }
}

@Composable
private fun NourChannelMoon(c: Channel, recording: Boolean, onClick: () -> Unit) {
    val now = c.currentProgram
    Column(Modifier.width(132.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        NourCard(onClick = onClick, shape = CircleShape, container = NR.Raised, zoom = 1.1f, modifier = Modifier.size(104.dp)) {
            NourLogo(c.name, c.logoUrl, 84.dp, Modifier.align(Alignment.Center))
            if (recording) Box(Modifier.align(Alignment.TopEnd).padding(10.dp).size(12.dp).clip(CircleShape).background(NR.Live))
            now?.let {
                val f = ((System.currentTimeMillis() - it.startTime).toFloat() / (it.endTime - it.startTime).coerceAtLeast(1)).coerceIn(0f, 1f)
                androidx.compose.foundation.Canvas(Modifier.fillMaxSize().padding(3.dp)) {
                    drawArc(NR.Amber, -90f, 360f * f, false, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
                }
            }
        }
        Text(c.name, color = NR.Text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(now?.title ?: (if (c.number > 0) "CH ${c.number}" else ""), color = NR.Faint, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
internal fun NourLiveTv(p: LiveTvParams) {
    Column(Modifier.fillMaxSize().padding(horizontal = 36.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            NourSearchField(p.categorySearchQuery, p.onCategorySearchChange, tr("Categories", "التصنيفات"), Modifier.width(210.dp))
            LazyRow(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(6.dp)) {
                items(p.categories, key = { it.id }) { cat ->
                    val sel = cat.id == p.selectedCategoryId
                    NourCard(
                        onClick = { p.onCategoryClick(cat) }, onLongClick = { p.onCategoryLongClick(cat) }, shape = NR.Pill, zoom = 1.03f,
                        container = if (sel) NR.Line else Color.Transparent, focusedContainer = NR.Line,
                        modifier = Modifier.widthIn(min = 140.dp, max = 220.dp).focusRequester(p.categoryRequester(cat.id)).onFocusChanged { if (it.isFocused) p.onCategoryFocused(cat) }
                            .nourRight { p.onRequestChannelsFromCategory() }
                    ) {
                        Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.size(7.dp).clip(CircleShape).background(if (sel) NR.Amber else NR.Line))
                            Text((if (p.isCategoryLocked(cat)) "🔒 " else "") + cat.name, color = if (sel) NR.Amber else NR.Text, fontSize = 14.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            if (cat.count > 0) Text("${cat.count}", color = NR.Faint, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(p.sourceTitle.ifBlank { tr("Live TV", "البث المباشر") }, color = NR.Text, fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif, maxLines = 1, modifier = Modifier.weight(1f))
                Text("${p.channels.size}", color = NR.Amber, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 10.dp))
            }
            NourSearchField(p.channelSearchQuery, p.onChannelSearchChange, tr("Find a channel", "ابحث عن قناة"), Modifier.fillMaxWidth())
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 6.dp, horizontal = 4.dp)) {
                items(p.channels, key = { it.id }) { c ->
                    val locked = p.isChannelLocked(c)
                    val previewing = c.id == p.previewChannel?.id
                    NourCard(
                        onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c) }, shape = NR.Pill, zoom = 1.03f,
                        container = if (previewing) NR.Line else NR.Raised.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth().focusRequester(p.channelRequester(c.id)).onFocusChanged { if (it.isFocused) p.onChannelFocused(c) }
                            .nourRight { p.onRequestPreviewFromChannel() }
                    ) {
                        Row(Modifier.padding(start = 6.dp, end = 18.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            NourLogo(c.name, if (locked) null else c.logoUrl, 48.dp)
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text((if (c.id == p.movingChannelId) "⇅  " else "") + (if (c.number > 0) "${c.number}  " else "") + c.name, color = NR.Text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(if (locked) tr("Locked", "مقفل") else c.currentProgram?.title ?: tr("No guide data", "لا يوجد دليل"), color = NR.Faint, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            if (!locked) c.qualityBadge()?.let { NourBadge(it, NR.Blue) }
                            if (c.catchUpSupported) Text("⟲", color = NR.Blue, fontSize = 14.sp)
                            if (c.isFavorite) Text("♥", color = NR.Rose, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
        Column(Modifier.weight(1.7f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            NourCard(
                onClick = { p.previewChannel?.let(p.onChannelClick) }, container = Color.Black, zoom = 1.02f,
                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).focusRequester(p.previewFocusRequester).nourLeft { p.onRequestChannelsFromPreview() }
            ) {
                val engine = p.previewPlayerEngine
                if (engine != null && p.previewChannel != null) PlayerRenderView(engine, PlayerSurfaceResizeMode.FIT, Modifier.fillMaxSize())
                when {
                    p.previewErrorMessage != null -> Text(p.previewErrorMessage, color = NR.Live, fontSize = 13.sp, modifier = Modifier.align(Alignment.Center).padding(16.dp))
                    p.isPreviewLoading -> Text(tr("Loading…", "جار التحميل…"), color = NR.Sub, modifier = Modifier.align(Alignment.Center))
                    p.previewChannel == null -> Text("◈  " + tr("Pick a channel to preview", "اختر قناة للمعاينة"), color = NR.Faint, modifier = Modifier.align(Alignment.Center))
                }
                if (p.previewChannel != null) Box(Modifier.align(Alignment.TopStart).padding(14.dp)) { NourBadge("● LIVE", NR.Live, filled = true) }
            }
            val c = p.previewChannel
            Column(Modifier.fillMaxWidth().weight(1f).clip(NR.R).background(Brush.verticalGradient(listOf(NR.Raised, NR.Card.copy(alpha = 0.6f)))).border(1.dp, NR.Line, NR.R).padding(22.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (c == null) Text(tr("Your evening, live.", "سهرتك، مباشرة."), color = NR.Sub, fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif)
                else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        NourLogo(c.name, c.logoUrl, 44.dp)
                        Column(Modifier.weight(1f)) {
                            Text(c.name, color = NR.Text, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (c.number > 0) NourBadge("CH ${c.number}", NR.Sub)
                                c.qualityBadge()?.let { NourBadge(it, NR.Blue) }
                                if (c.catchUpSupported) NourBadge(tr("CATCH-UP", "أرشيف"), NR.Blue)
                            }
                        }
                    }
                    c.currentProgram?.let { now ->
                        Text(tr("NOW", "الآن"), color = NR.Amber, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Text(now.title, color = NR.Text, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif, maxLines = 1)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(nourClock(now.startTime), color = NR.Faint, fontSize = 12.sp)
                            NourProgress((System.currentTimeMillis() - now.startTime).toFloat() / (now.endTime - now.startTime).coerceAtLeast(1), Modifier.weight(1f))
                            Text(nourClock(now.endTime), color = NR.Faint, fontSize = 12.sp)
                        }
                        Text(now.description, color = NR.Faint, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    } ?: Text(tr("No guide data", "لا يوجد دليل"), color = NR.Faint)
                    c.nextProgram?.let { Text(tr("LATER", "لاحقاً") + "   ${nourClock(it.startTime)}  ${it.title}", color = NR.Sub, fontSize = 13.sp, maxLines = 1) }
                }
            }
        }
    }
}
}
