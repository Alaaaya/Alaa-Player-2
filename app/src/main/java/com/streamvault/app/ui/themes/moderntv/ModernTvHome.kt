package com.streamvault.app.ui.themes.moderntv

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

/** Modern TV's own rounded search field: magnifier inside a soft pill, white outline on focus. */
@Composable
internal fun MtSearchField(value: String, onChange: (String) -> Unit, hint: String, modifier: Modifier = Modifier, onSubmit: (() -> Unit)? = null) {
    var focused by remember { mutableStateOf(false) }
    Row(
        modifier.clip(MT.Pill).background(if (focused) MT.Card else MT.Raised).border(2.dp, if (focused) MT.Text else Color.Transparent, MT.Pill)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("⌕", color = if (focused) MT.Amber else MT.Faint, fontSize = 16.sp)
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) Text(hint, color = MT.Faint, fontSize = 14.sp, maxLines = 1)
            BasicTextField(
                value = value, onValueChange = onChange, singleLine = true,
                textStyle = TextStyle(color = MT.Text, fontSize = 14.sp), cursorBrush = SolidColor(MT.Amber),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { onSubmit?.invoke() }, onDone = { onSubmit?.invoke() }),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                modifier = Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused }
            )
        }
    }
}

private class MtDest(val route: String, val glyph: String, val en: String, val ar: String)

private val mtDestinations = listOf(
    MtDest(Routes.SEARCH, "⌕", "Search", "بحث"),
    MtDest(Routes.HOME, "⌂", "Home", "الرئيسية"),
    MtDest(Routes.LIVE_TV, "◉", "Live TV", "البث المباشر"),
    MtDest(Routes.EPG, "▦", "Guide", "الدليل"),
    MtDest(Routes.MOVIES, "▶", "Movies", "أفلام"),
    MtDest(Routes.SERIES, "❏", "Series", "مسلسلات"),
    MtDest(Routes.FAVORITES, "♥", "My List", "قائمتي"),
    MtDest(Routes.SETTINGS, "⚙", "Settings", "الإعدادات")
)

private fun onRoute(current: String, route: String) = current == route || current.startsWith("$route?") || current.startsWith("$route/")

/** Shell: slim icon rail on the start edge that widens to show labels while it has focus. */
@Composable
internal fun ModernTvShell(p: ShellParams) {
    MtBackground(p.modifier) {
        Row(Modifier.fillMaxSize()) {
            if (p.topBarVisible) {
                var railFocused by remember { mutableStateOf(false) }
                Column(
                    Modifier.fillMaxHeight().animateContentSize()
                        .background(Brush.horizontalGradient(listOf(Color.Black.copy(alpha = 0.85f), Color.Black.copy(alpha = 0.0f))))
                        .onFocusChanged { railFocused = it.hasFocus }
                        .padding(horizontal = 14.dp, vertical = 26.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(Modifier.size(44.dp).clip(MT.RSmall).background(Brush.linearGradient(listOf(MT.Amber, MT.AmberDeep))), contentAlignment = Alignment.Center) {
                        Text("A", color = MT.Bg, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.height(18.dp))
                    mtDestinations.forEach { d ->
                        val active = onRoute(p.currentRoute, d.route)
                        MtCard(
                            onClick = { if (!active) p.onNavigate(d.route) }, shape = MT.Pill, zoom = 1.04f,
                            container = if (active) Color.White.copy(alpha = 0.12f) else Color.Transparent, focusedContainer = Color.White.copy(alpha = 0.22f)
                        ) {
                            Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text(d.glyph, color = if (active) MT.Amber else MT.Sub, fontSize = 18.sp, modifier = Modifier.width(22.dp))
                                if (railFocused) Text(tr(d.en, d.ar), color = MT.Text, fontSize = 15.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium, maxLines = 1, modifier = Modifier.width(120.dp))
                            }
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Text(mtClock(System.currentTimeMillis()), color = MT.Sub, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 8.dp))
                }
            }
            Column(Modifier.weight(1f).fillMaxHeight().padding(p.contentPadding).padding(start = 20.dp, end = 40.dp, top = 24.dp)) {
                if (p.showScreenHeader || p.topBarActions != null) {
                    Row(Modifier.fillMaxWidth().padding(bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (p.showScreenHeader) Column(Modifier.weight(1f)) {
                            Text(p.title, color = MT.Text, fontSize = 30.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            p.subtitle?.takeIf { it.isNotBlank() }?.let { Text(it, color = MT.Sub, fontSize = 14.sp, maxLines = 1) }
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
internal fun ModernTvDashboard(p: DashboardParams) {
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
            Box(Modifier.fillMaxWidth().height(380.dp).clip(MT.R)) {
                s.feature.artworkUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(MT.Bg, MT.Bg.copy(alpha = 0.75f), Color.Transparent))))
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, MT.Bg))))
                Column(Modifier.align(Alignment.CenterStart).padding(start = 40.dp).widthIn(max = 560.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        MtBadge(tr("FEATURED", "مميز"), MT.Amber, filled = true)
                        s.provider?.name?.let { Text(it, color = MT.Sub, fontSize = 13.sp) }
                    }
                    Text(s.feature.title.ifBlank { s.provider?.name ?: "Modern TV" }, color = MT.Text, fontSize = 44.sp, fontWeight = FontWeight.Black, maxLines = 2, lineHeight = 48.sp)
                    Text(s.feature.summary, color = MT.Sub, fontSize = 15.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 6.dp)) {
                        MtButton(s.feature.actionLabel.ifBlank { tr("Watch now", "شاهد الآن") }, featureAction, Modifier.focusRequester(first), primary = true, icon = "▶")
                        MtButton(tr("Live TV", "البث المباشر"), { p.onNavigate(Routes.LIVE_TV) }, icon = "◉")
                        MtButton(tr("Guide", "الدليل"), { p.onNavigate(Routes.EPG) }, icon = "▦")
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.padding(top = 4.dp)) {
                        Text("${s.stats.liveChannelCount} " + tr("channels", "قناة"), color = MT.Faint, fontSize = 12.sp)
                        Text("${s.stats.movieLibraryCount} " + tr("movies", "فيلم"), color = MT.Faint, fontSize = 12.sp)
                        Text("${s.stats.seriesLibraryCount} " + tr("series", "مسلسل"), color = MT.Faint, fontSize = 12.sp)
                    }
                }
            }
        }
        if (s.continueWatching.isNotEmpty()) item {
            Column {
                MtRowTitle(tr("Continue watching", "متابعة المشاهدة"))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(s.continueWatching, key = { "cw${it.id}" }) { h ->
                        val prog = if (h.totalDurationMs > 0) h.resumePositionMs.toFloat() / h.totalDurationMs else null
                        val left = if (h.totalDurationMs > 0) mtDuration(h.totalDurationMs - h.resumePositionMs) + " " + tr("left", "متبقي") else null
                        MtWide(h.title, h.posterUrl, left, prog, { p.onContinueWatchingItemClick(h) }, Modifier.width(280.dp))
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
                    MtRowTitle(title, trailing = "${channels.size}")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                        items(channels, key = { "$title${it.id}" }) { c -> MtChannelTile(c, c.id in p.recordingChannelIds) { click(c, s.currentCombinedProfileId) } }
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
                    MtRowTitle(title)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                        items(movies, key = { "$title${it.id}" }) { m -> MtPoster(m.name, m.posterUrl, m.year, { p.onMovieClick(m) }, Modifier.width(150.dp)) }
                    }
                }
            }
        }
        if (s.recentSeries.isNotEmpty()) item {
            Column {
                MtRowTitle(tr("New series", "مسلسلات جديدة"))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(s.recentSeries, key = { "rs${it.id}" }) { m -> MtPoster(m.name, m.posterUrl, m.genre, { p.onSeriesClick(m) }, Modifier.width(150.dp)) }
                }
            }
        }
    }
}

/** Landscape channel tile: logo centered on a dark card, now-playing title and progress underneath. */
@Composable
private fun MtChannelTile(c: Channel, recording: Boolean, onClick: () -> Unit) {
    val now = c.currentProgram
    Column(Modifier.width(220.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        MtCard(onClick = onClick, modifier = Modifier.fillMaxWidth().height(124.dp), container = MT.Raised) {
            MtLogo(c.name, c.logoUrl, 64.dp, Modifier.align(Alignment.Center))
            Row(Modifier.align(Alignment.TopEnd).padding(8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (recording) MtBadge("REC", MT.Live, filled = true)
                c.qualityBadge()?.let { MtBadge(it, MT.Text) }
            }
            if (c.number > 0) Text("${c.number}", color = MT.Faint, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.TopStart).padding(10.dp))
            now?.let { MtProgress(((System.currentTimeMillis() - it.startTime).toFloat() / (it.endTime - it.startTime).coerceAtLeast(1)), Modifier.align(Alignment.BottomCenter).padding(10.dp), 3.dp) }
        }
        Text(c.name, color = MT.Text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(now?.title ?: tr("No guide data", "لا يوجد دليل"), color = MT.Faint, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Live TV: wide preview hero across the top (video + now/next), then a categories column beside a single channels column (no grid). */
@Composable
internal fun ModernTvLiveTv(p: LiveTvParams) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(Modifier.fillMaxWidth().height(230.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            MtCard(
                onClick = { p.previewChannel?.let(p.onChannelClick) }, container = Color.Black, zoom = 1.02f,
                modifier = Modifier.aspectRatio(16f / 9f).fillMaxHeight().focusRequester(p.previewFocusRequester).mtLeft { p.onRequestChannelsFromPreview() }
            ) {
                val engine = p.previewPlayerEngine
                if (engine != null && p.previewChannel != null) PlayerRenderView(engine, PlayerSurfaceResizeMode.FIT, Modifier.fillMaxSize())
                when {
                    p.previewErrorMessage != null -> Text(p.previewErrorMessage, color = MT.Live, fontSize = 13.sp, modifier = Modifier.align(Alignment.Center).padding(16.dp))
                    p.isPreviewLoading -> Text(tr("Loading…", "جار التحميل…"), color = MT.Sub, modifier = Modifier.align(Alignment.Center))
                    p.previewChannel == null -> Text(tr("Pick a channel to preview", "اختر قناة للمعاينة"), color = MT.Faint, modifier = Modifier.align(Alignment.Center))
                }
                if (p.previewChannel != null) Box(Modifier.align(Alignment.TopStart).padding(10.dp)) { MtBadge("● LIVE", MT.Live, filled = true) }
            }
            val c = p.previewChannel
            Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(p.sourceTitle.ifBlank { tr("Live TV", "البث المباشر") }, color = MT.Faint, fontSize = 13.sp)
                if (c == null) Text(tr("Browse channels below", "تصفح القنوات بالأسفل"), color = MT.Text, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MtLogo(c.name, c.logoUrl, 48.dp)
                        Column {
                            Text(c.name, color = MT.Text, fontSize = 24.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (c.number > 0) MtBadge("CH ${c.number}", MT.Sub)
                                c.qualityBadge()?.let { MtBadge(it, MT.Text) }
                                if (c.catchUpSupported) MtBadge(tr("CATCH-UP", "أرشيف"), MT.Blue)
                            }
                        }
                    }
                    c.currentProgram?.let { now ->
                        Text(now.title, color = MT.Text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(mtClock(now.startTime), color = MT.Sub, fontSize = 12.sp)
                            MtProgress((System.currentTimeMillis() - now.startTime).toFloat() / (now.endTime - now.startTime).coerceAtLeast(1), Modifier.weight(1f))
                            Text(mtClock(now.endTime), color = MT.Sub, fontSize = 12.sp)
                        }
                        Text(now.description, color = MT.Faint, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    } ?: Text(tr("No guide data", "لا يوجد دليل"), color = MT.Faint)
                    c.nextProgram?.let { Text(tr("Up next", "التالي") + "  ${mtClock(it.startTime)}  ${it.title}", color = MT.Sub, fontSize = 13.sp, maxLines = 1) }
                }
            }
        }
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Column(Modifier.width(240.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MtSearchField(p.categorySearchQuery, p.onCategorySearchChange, tr("Categories", "التصنيفات"), Modifier.fillMaxWidth())
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(p.categories, key = { it.id }) { cat ->
                        val sel = cat.id == p.selectedCategoryId
                        MtCard(
                            onClick = { p.onCategoryClick(cat) }, onLongClick = { p.onCategoryLongClick(cat) }, shape = MT.RSmall, zoom = 1.03f,
                            container = if (sel) MT.Amber.copy(alpha = 0.18f) else Color.Transparent, focusedContainer = MT.Card,
                            modifier = Modifier.fillMaxWidth().focusRequester(p.categoryRequester(cat.id)).onFocusChanged { if (it.isFocused) p.onCategoryFocused(cat) }
                                .mtRight { p.onRequestChannelsFromCategory() }
                        ) {
                            Row(Modifier.padding(horizontal = 14.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (sel) Box(Modifier.size(6.dp).clip(MT.Pill).background(MT.Amber))
                                Text((if (p.isCategoryLocked(cat)) "🔒 " else "") + cat.name, color = if (sel) MT.Amber else MT.Text, fontSize = 14.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f).padding(start = if (sel) 8.dp else 0.dp))
                                if (cat.count > 0) Text("${cat.count}", color = MT.Faint, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
            Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${p.channels.size} " + tr("channels", "قناة"), color = MT.Sub, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    MtSearchField(p.channelSearchQuery, p.onChannelSearchChange, tr("Find a channel", "ابحث عن قناة"), Modifier.width(280.dp))
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 6.dp, horizontal = 4.dp)) {
                    items(p.channels, key = { it.id }) { c ->
                        run {
                            run {
                                val locked = p.isChannelLocked(c)
                                MtCard(
                                    onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c) }, zoom = 1.03f,
                                    container = if (c.id == p.previewChannel?.id) MT.Card else MT.Raised,
                                    modifier = Modifier.fillMaxWidth().focusRequester(p.channelRequester(c.id)).onFocusChanged { if (it.isFocused) p.onChannelFocused(c) }
                                        .mtRight { p.onRequestPreviewFromChannel() }
                                ) {
                                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text(if (c.number > 0) "${c.number}" else "", color = MT.Faint, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(34.dp))
                                        MtLogo(c.name, if (locked) null else c.logoUrl, 46.dp)
                                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                            Text(if (c.id == p.movingChannelId) "⇅  ${c.name}" else c.name, color = MT.Text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(if (locked) tr("Locked", "مقفل") else c.currentProgram?.title ?: tr("No guide data", "لا يوجد دليل"), color = MT.Faint, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            if (!locked) c.currentProgram?.let { MtProgress((System.currentTimeMillis() - it.startTime).toFloat() / (it.endTime - it.startTime).coerceAtLeast(1), height = 3.dp) }
                                        }
                                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(if (c.isFavorite) "♥" else "♡", color = if (c.isFavorite) MT.Amber else MT.Faint, fontSize = 15.sp)
                                            if (!locked) c.qualityBadge()?.let { MtBadge(it, MT.Text) }
                                            if (c.catchUpSupported) MtBadge("⟲", MT.Blue)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
