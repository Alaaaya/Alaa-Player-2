package com.streamvault.app.ui.themes.darkglass

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

/** DarkGlass's own rounded search field: magnifier inside a soft pill, white outline on focus. */
@Composable
internal fun DgSearchField(value: String, onChange: (String) -> Unit, hint: String, modifier: Modifier = Modifier, onSubmit: (() -> Unit)? = null) {
    var focused by remember { mutableStateOf(false) }
    Row(
        modifier.dgGlass(DG.Pill, if (focused) 0.14f else 0.06f).border(if (focused) 2.dp else 0.dp, if (focused) DG.Amber else Color.Transparent, DG.Pill)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("⌕", color = if (focused) DG.Amber else DG.Faint, fontSize = 16.sp)
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) Text(hint, color = DG.Faint, fontSize = 14.sp, maxLines = 1)
            BasicTextField(
                value = value, onValueChange = onChange, singleLine = true,
                textStyle = TextStyle(color = DG.Text, fontSize = 14.sp), cursorBrush = SolidColor(DG.Amber),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { onSubmit?.invoke() }, onDone = { onSubmit?.invoke() }),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                modifier = Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused }
            )
        }
    }
}

private class DgDest(val route: String, val glyph: String, val en: String, val ar: String)

private val dgDestinations = listOf(
    DgDest(Routes.SEARCH, "⌕", "Search", "بحث"),
    DgDest(Routes.HOME, "⌂", "Home", "الرئيسية"),
    DgDest(Routes.LIVE_TV, "◉", "Live TV", "البث المباشر"),
    DgDest(Routes.EPG, "▦", "Guide", "الدليل"),
    DgDest(Routes.MOVIES, "▶", "Movies", "أفلام"),
    DgDest(Routes.SERIES, "❏", "Series", "مسلسلات"),
    DgDest(Routes.FAVORITES, "♥", "My List", "قائمتي"),
    DgDest(Routes.SETTINGS, "⚙", "Settings", "الإعدادات")
)

private fun onRoute(current: String, route: String) = current == route || current.startsWith("$route?") || current.startsWith("$route/")

/** Shell: a floating glass capsule rail, detached from the edge and vertically centred. Icons only; the focused
 *  item shows its label in a glass tooltip pane beside the capsule. Clock + profile orb float top-end. */
@Composable
internal fun DarkGlassShell(p: ShellParams) {
    DgBackground(p.modifier) {
        Row(Modifier.fillMaxSize()) {
            if (p.topBarVisible) {
                var focusedDest by remember { mutableStateOf<String?>(null) }
                Box(Modifier.fillMaxHeight().padding(start = 18.dp), contentAlignment = Alignment.CenterStart) {
                    Column(Modifier.dgGlass(RoundedCornerShape(36.dp), 0.07f).padding(horizontal = 8.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.size(40.dp).clip(CircleShape).background(Brush.linearGradient(listOf(DG.Amber, DG.Blue))), contentAlignment = Alignment.Center) {
                            Text("◇", color = DG.Bg, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        }
                        Spacer(Modifier.height(8.dp))
                        dgDestinations.forEach { d ->
                            val active = onRoute(p.currentRoute, d.route)
                            DgCard(
                                onClick = { if (!active) p.onNavigate(d.route) }, shape = CircleShape, zoom = 1.1f,
                                container = if (active) Color(0x40B69CFF) else Color.Transparent,
                                modifier = Modifier.size(48.dp).onFocusChanged { if (it.isFocused) focusedDest = d.route else if (focusedDest == d.route) focusedDest = null }
                            ) {
                                Text(d.glyph, color = if (active) DG.Text else DG.Sub, fontSize = 18.sp, modifier = Modifier.align(Alignment.Center))
                            }
                        }
                    }
                }
                Box(Modifier.width(if (focusedDest != null) 150.dp else 0.dp).fillMaxHeight(), contentAlignment = Alignment.CenterStart) {
                    dgDestinations.firstOrNull { it.route == focusedDest }?.let { d ->
                        Text(tr(d.en, d.ar), color = DG.Text, fontSize = 15.sp, fontWeight = FontWeight.Light, modifier = Modifier.padding(start = 10.dp).dgGlass(DG.Pill, 0.1f).padding(horizontal = 16.dp, vertical = 8.dp))
                    }
                }
            }
            Column(Modifier.weight(1f).fillMaxHeight().padding(p.contentPadding).padding(start = 22.dp, end = 36.dp, top = 22.dp)) {
                Row(Modifier.fillMaxWidth().padding(bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (p.showScreenHeader) Column(Modifier.weight(1f)) {
                        Text(p.title, color = DG.Text, fontSize = 30.sp, fontWeight = FontWeight.Thin, maxLines = 1)
                        p.subtitle?.takeIf { it.isNotBlank() }?.let { Text(it, color = DG.Sub, fontSize = 13.sp, maxLines = 1) }
                    } else Spacer(Modifier.weight(1f))
                    p.topBarActions?.let { Row(horizontalArrangement = Arrangement.spacedBy(10.dp), content = it) }
                    if (p.topBarVisible) Text(dgClock(System.currentTimeMillis()), color = DG.Text, fontSize = 15.sp, fontWeight = FontWeight.Light, modifier = Modifier.padding(start = 12.dp).dgGlass(DG.Pill, 0.06f).padding(horizontal = 14.dp, vertical = 6.dp))
                }
                p.header?.let { Column(content = it) }
                Column(Modifier.fillMaxSize(), content = p.content)
            }
        }
    }
}

/** Home: the featured artwork fills the whole background (dimmed); a glass hero pane floats START with CTAs,
 *  a stacked glass "continue" column floats END. Shelves below sit in glass trays. */
@Composable
internal fun DarkGlassDashboard(p: DashboardParams) {
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
    Box(Modifier.fillMaxSize()) {
        s.feature.artworkUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, alpha = 0.35f, modifier = Modifier.fillMaxWidth().height(520.dp).clip(DG.R)) }
        Box(Modifier.fillMaxWidth().height(520.dp).background(Brush.verticalGradient(listOf(Color.Transparent, DG.Bg.copy(alpha = 0.6f), DG.Bg))))
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 48.dp), verticalArrangement = Arrangement.spacedBy(22.dp)) {
            item {
                Row(Modifier.fillMaxWidth().height(330.dp).padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Column(Modifier.weight(1.5f).fillMaxHeight().dgGlass(DG.R, 0.07f).padding(28.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            DgBadge(tr("FEATURED", "مميز"), DG.Amber)
                            s.provider?.name?.let { Text(it, color = DG.Sub, fontSize = 12.sp) }
                        }
                        Text(s.feature.title.ifBlank { s.provider?.name ?: "Dark Glass" }, color = DG.Text, fontSize = 40.sp, fontWeight = FontWeight.Thin, maxLines = 2, lineHeight = 44.sp)
                        Text(s.feature.summary, color = DG.Sub, fontSize = 14.sp, maxLines = 3, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            DgButton(s.feature.actionLabel.ifBlank { tr("Watch now", "شاهد الآن") }, featureAction, Modifier.focusRequester(first), primary = true, icon = "▶")
                            DgButton(tr("Live TV", "البث المباشر"), { p.onNavigate(Routes.LIVE_TV) }, icon = "◉")
                            DgButton(tr("Guide", "الدليل"), { p.onNavigate(Routes.EPG) }, icon = "▦")
                        }
                    }
                    Column(Modifier.weight(1f).fillMaxHeight().dgGlass(DG.R, 0.05f).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(tr("Continue watching", "متابعة المشاهدة"), color = DG.Text, fontSize = 15.sp, fontWeight = FontWeight.Light)
                        if (s.continueWatching.isEmpty()) {
                            listOf("${s.stats.liveChannelCount}" to tr("channels", "قناة"), "${s.stats.movieLibraryCount}" to tr("movies", "فيلم"), "${s.stats.seriesLibraryCount}" to tr("series", "مسلسل")).forEach { (n, l) ->
                                Row(Modifier.fillMaxWidth().dgGlass(DG.RSmall, 0.05f).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(n, color = DG.Amber, fontSize = 22.sp, fontWeight = FontWeight.Thin, modifier = Modifier.weight(1f)); Text(l, color = DG.Sub, fontSize = 13.sp)
                                }
                            }
                        } else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(s.continueWatching, key = { "cw${it.id}" }) { h ->
                                val prog = if (h.totalDurationMs > 0) h.resumePositionMs.toFloat() / h.totalDurationMs else 0f
                                DgCard(onClick = { p.onContinueWatchingItemClick(h) }, shape = DG.RSmall, zoom = 1.03f, modifier = Modifier.fillMaxWidth()) {
                                    Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Box(Modifier.width(96.dp).aspectRatio(16f / 9f).clip(DG.RSmall).background(DG.Solid)) { h.posterUrl?.let { AsyncImage(it, h.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) } }
                                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                            Text(h.title, color = DG.Text, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            if (h.totalDurationMs > 0) Text(dgDuration(h.totalDurationMs - h.resumePositionMs) + " " + tr("left", "متبقي"), color = DG.Faint, fontSize = 11.sp)
                                            DgProgress(prog, height = 3.dp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            listOf(Triple(tYour, s.favoriteChannels, p.onFavoriteChannelClick), Triple(tRecentCh, s.recentChannels, p.onRecentChannelClick)).forEach { (title, channels, click) ->
                if (channels.isNotEmpty()) item {
                    Column(Modifier.fillMaxWidth().dgGlass(DG.R, 0.03f).padding(16.dp)) {
                        DgRowTitle(title, trailing = "${channels.size}")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 6.dp)) {
                            items(channels, key = { "$title${it.id}" }) { c -> DgChannelOrb(c, c.id in p.recordingChannelIds) { click(c, s.currentCombinedProfileId) } }
                        }
                    }
                }
            }
            listOf(tRec to s.recommendedMovies, tTop to s.topRatedMovies, tNew to s.recentMovies).forEach { (title, movies) ->
                if (movies.isNotEmpty()) item {
                    Column(Modifier.fillMaxWidth().dgGlass(DG.R, 0.03f).padding(16.dp)) {
                        DgRowTitle(title)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(vertical = 6.dp)) {
                            items(movies, key = { "$title${it.id}" }) { m -> DgPoster(m.name, m.posterUrl, m.year, { p.onMovieClick(m) }, Modifier.width(140.dp)) }
                        }
                    }
                }
            }
            if (s.recentSeries.isNotEmpty()) item {
                Column(Modifier.fillMaxWidth().dgGlass(DG.R, 0.03f).padding(16.dp)) {
                    DgRowTitle(tr("New series", "مسلسلات جديدة"))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(vertical = 6.dp)) {
                        items(s.recentSeries, key = { "rs${it.id}" }) { m -> DgPoster(m.name, m.posterUrl, m.genre, { p.onSeriesClick(m) }, Modifier.width(140.dp)) }
                    }
                }
            }
        }
    }
}

/** Channel orb: logo inside a round glass bubble, name and a glowing arc-like progress below. */
@Composable
private fun DgChannelOrb(c: Channel, recording: Boolean, onClick: () -> Unit) {
    val now = c.currentProgram
    Column(Modifier.width(132.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        DgCard(onClick = onClick, shape = CircleShape, zoom = 1.1f, modifier = Modifier.size(104.dp)) {
            if (!c.logoUrl.isNullOrBlank()) AsyncImage(c.logoUrl, c.name, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(22.dp))
            else Text(c.name.take(2).uppercase(), color = DG.Sub, fontSize = 22.sp, fontWeight = FontWeight.Thin, modifier = Modifier.align(Alignment.Center))
            if (recording) Box(Modifier.align(Alignment.TopEnd).padding(10.dp).size(10.dp).clip(CircleShape).background(DG.Live))
        }
        Text((if (c.number > 0) "${c.number} · " else "") + c.name, color = DG.Text, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        now?.let { DgProgress(((System.currentTimeMillis() - it.startTime).toFloat() / (it.endTime - it.startTime).coerceAtLeast(1)), Modifier.width(80.dp), 2.dp) }
    }
}

/** Live TV: three floating glass panes. Categories pane (narrow) | channels pane (single column) |
 *  a tall END pane stacking the preview video over the now/next glass card. */
@Composable
internal fun DarkGlassLiveTv(p: LiveTvParams) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(Modifier.width(230.dp).fillMaxHeight().dgGlass(DG.R, 0.05f).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(p.sourceTitle.ifBlank { tr("Live TV", "البث المباشر") }, color = DG.Faint, fontSize = 12.sp, modifier = Modifier.padding(start = 6.dp))
            DgSearchField(p.categorySearchQuery, p.onCategorySearchChange, tr("Categories", "التصنيفات"), Modifier.fillMaxWidth())
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(p.categories, key = { it.id }) { cat ->
                    val sel = cat.id == p.selectedCategoryId
                    DgCard(
                        onClick = { p.onCategoryClick(cat) }, onLongClick = { p.onCategoryLongClick(cat) }, shape = DG.Pill, zoom = 1.03f,
                        container = if (sel) Color(0x33B69CFF) else Color.Transparent,
                        modifier = Modifier.fillMaxWidth().focusRequester(p.categoryRequester(cat.id)).onFocusChanged { if (it.isFocused) p.onCategoryFocused(cat) }
                            .dgRight { p.onRequestChannelsFromCategory() }
                    ) {
                        Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text((if (p.isCategoryLocked(cat)) "🔒 " else "") + cat.name, color = if (sel) DG.Text else DG.Sub, fontSize = 14.sp, fontWeight = if (sel) FontWeight.Medium else FontWeight.Light, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            if (cat.count > 0) Text("${cat.count}", color = if (sel) DG.Amber else DG.Faint, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
        Column(Modifier.weight(1f).fillMaxHeight().dgGlass(DG.R, 0.04f).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${p.channels.size}", color = DG.Amber, fontSize = 22.sp, fontWeight = FontWeight.Thin)
                Text(" " + tr("channels", "قناة"), color = DG.Sub, fontSize = 13.sp, modifier = Modifier.weight(1f))
                DgSearchField(p.channelSearchQuery, p.onChannelSearchChange, tr("Find a channel", "ابحث عن قناة"), Modifier.width(240.dp))
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp)) {
                items(p.channels, key = { it.id }) { c ->
                    val locked = p.isChannelLocked(c)
                    val cur = c.id == p.previewChannel?.id
                    DgCard(
                        onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c) }, shape = DG.Pill, zoom = 1.02f,
                        container = if (cur) Color(0x26B69CFF) else Color.Transparent,
                        modifier = Modifier.fillMaxWidth().focusRequester(p.channelRequester(c.id)).onFocusChanged { if (it.isFocused) p.onChannelFocused(c) }
                            .dgRight { p.onRequestPreviewFromChannel() }
                    ) {
                        Row(Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            DgLogo(c.name, if (locked) null else c.logoUrl, 42.dp)
                            Text(if (c.number > 0) "${c.number}" else "", color = DG.Amber, fontSize = 12.sp, fontWeight = FontWeight.Light, modifier = Modifier.width(30.dp))
                            Column(Modifier.weight(1f)) {
                                Text(if (c.id == p.movingChannelId) "⇅  ${c.name}" else c.name, color = DG.Text, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(if (locked) tr("Locked", "مقفل") else c.currentProgram?.title ?: tr("No guide data", "لا يوجد دليل"), color = DG.Faint, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            if (!locked) c.qualityBadge()?.let { DgBadge(it, DG.Blue) }
                            if (c.catchUpSupported) DgBadge("⟲", DG.Blue)
                            Text(if (c.isFavorite) "★" else "☆", color = if (c.isFavorite) DG.Amber else DG.Faint, fontSize = 14.sp, modifier = Modifier.padding(end = 6.dp))
                        }
                    }
                }
            }
        }
        val c = p.previewChannel
        Column(Modifier.width(400.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            DgCard(
                onClick = { c?.let(p.onChannelClick) }, container = Color.Black, zoom = 1.02f,
                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).focusRequester(p.previewFocusRequester).dgLeft { p.onRequestChannelsFromPreview() }
            ) {
                val engine = p.previewPlayerEngine
                if (engine != null && c != null) PlayerRenderView(engine, PlayerSurfaceResizeMode.FIT, Modifier.fillMaxSize())
                when {
                    p.previewErrorMessage != null -> Text(p.previewErrorMessage, color = DG.Live, fontSize = 13.sp, modifier = Modifier.align(Alignment.Center).padding(16.dp))
                    p.isPreviewLoading -> Text(tr("Loading…", "جار التحميل…"), color = DG.Sub, modifier = Modifier.align(Alignment.Center))
                    c == null -> Text(tr("Pick a channel to preview", "اختر قناة للمعاينة"), color = DG.Faint, modifier = Modifier.align(Alignment.Center))
                }
                if (c != null) Box(Modifier.align(Alignment.TopStart).padding(10.dp)) { DgBadge("● LIVE", DG.Live, filled = true) }
            }
            Column(Modifier.fillMaxWidth().weight(1f).dgGlass(DG.R, 0.06f).padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (c == null) Text(tr("Browse channels", "تصفح القنوات"), color = DG.Text, fontSize = 22.sp, fontWeight = FontWeight.Thin)
                else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DgLogo(c.name, c.logoUrl, 46.dp)
                        Column(Modifier.weight(1f)) {
                            Text(c.name, color = DG.Text, fontSize = 20.sp, fontWeight = FontWeight.Light, maxLines = 1)
                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                if (c.number > 0) DgBadge("CH ${c.number}", DG.Sub)
                                c.qualityBadge()?.let { DgBadge(it, DG.Blue) }
                                if (c.catchUpSupported) DgBadge(tr("CATCH-UP", "أرشيف"), DG.Blue)
                            }
                        }
                    }
                    c.currentProgram?.let { now ->
                        Text(tr("Now", "الآن"), color = DG.Amber, fontSize = 11.sp)
                        Text(now.title, color = DG.Text, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(dgClock(now.startTime), color = DG.Sub, fontSize = 11.sp)
                            DgProgress((System.currentTimeMillis() - now.startTime).toFloat() / (now.endTime - now.startTime).coerceAtLeast(1), Modifier.weight(1f))
                            Text(dgClock(now.endTime), color = DG.Sub, fontSize = 11.sp)
                        }
                        Text(now.description, color = DG.Faint, fontSize = 12.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    } ?: Text(tr("No guide data", "لا يوجد دليل"), color = DG.Faint)
                    c.nextProgram?.let {
                        Row(Modifier.fillMaxWidth().dgGlass(DG.RSmall, 0.05f).padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(tr("Next", "التالي") + " " + dgClock(it.startTime), color = DG.Amber, fontSize = 11.sp)
                            Text(it.title, color = DG.Sub, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}
