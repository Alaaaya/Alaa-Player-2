package com.streamvault.app.ui.themes.magazinemedia

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

/** Search field set like a printed form line: italic hint, ink underline that thickens to rust on focus. */
@Composable
internal fun MzSearchField(value: String, onChange: (String) -> Unit, hint: String, modifier: Modifier = Modifier, onSubmit: (() -> Unit)? = null) {
    var focused by remember { mutableStateOf(false) }
    Column(modifier.background(if (focused) MZ.Raised else Color.Transparent).padding(horizontal = 6.dp, vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("⌕", color = if (focused) MZ.Amber else MZ.Text, fontSize = 15.sp)
            Box(Modifier.weight(1f)) {
                if (value.isEmpty()) Text(hint, color = MZ.Faint, fontSize = 14.sp, fontFamily = MZ.Serif, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, maxLines = 1)
                BasicTextField(
                    value = value, onValueChange = onChange, singleLine = true,
                    textStyle = TextStyle(color = MZ.Text, fontSize = 14.sp, fontFamily = MZ.Serif), cursorBrush = SolidColor(MZ.Amber),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { onSubmit?.invoke() }, onDone = { onSubmit?.invoke() }),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                    modifier = Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused }
                )
            }
        }
        Box(Modifier.padding(top = 4.dp).fillMaxWidth().height(if (focused) 3.dp else 1.dp).background(if (focused) MZ.Amber else MZ.Text))
    }
}

private class MzDest(val route: String, val en: String, val ar: String)

private val mzDestinations = listOf(
    MzDest(Routes.HOME, "Front Page", "الصفحة الأولى"),
    MzDest(Routes.LIVE_TV, "Live", "مباشر"),
    MzDest(Routes.EPG, "Listings", "الدليل"),
    MzDest(Routes.MOVIES, "Film", "أفلام"),
    MzDest(Routes.SERIES, "Series", "مسلسلات"),
    MzDest(Routes.FAVORITES, "Saved", "المحفوظات"),
    MzDest(Routes.SEARCH, "Search", "بحث"),
    MzDest(Routes.SETTINGS, "Settings", "الإعدادات")
)

private fun onRoute(current: String, route: String) = current == route || current.startsWith("$route?") || current.startsWith("$route/")

/** Shell: newspaper masthead across the top. Dateline left, big serif title centered, clock right,
 *  then a double rule and a row of section names in small caps. */
@Composable
internal fun MagazineMediaShell(p: ShellParams) {
    MzBackground(p.modifier) {
        Column(Modifier.fillMaxSize()) {
            if (p.topBarVisible) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 48.dp).padding(top = 14.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        val cal = java.util.Calendar.getInstance()
                        Text(java.text.SimpleDateFormat("EEEE, d MMMM yyyy", java.util.Locale.getDefault()).format(cal.time), color = MZ.Sub, fontSize = 12.sp, fontFamily = MZ.Serif, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, modifier = Modifier.weight(1f))
                        Text("The Alaa Review", color = MZ.Text, fontSize = 30.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Black)
                        Text(mzClock(System.currentTimeMillis()), color = MZ.Sub, fontSize = 13.sp, fontFamily = MZ.Serif, textAlign = androidx.compose.ui.text.style.TextAlign.End, modifier = Modifier.weight(1f))
                    }
                    MzRule(Modifier.padding(top = 6.dp), thick = 2.dp)
                    MzRule(Modifier.padding(top = 2.dp))
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.Center) {
                        mzDestinations.forEachIndexed { i, d ->
                            val active = onRoute(p.currentRoute, d.route)
                            if (i > 0) Text("·", color = MZ.Faint, modifier = Modifier.align(Alignment.CenterVertically))
                            MzTab(tr(d.en, d.ar), active, { if (!active) p.onNavigate(d.route) })
                        }
                    }
                    MzRule()
                }
            }
            Column(Modifier.weight(1f).fillMaxWidth().padding(p.contentPadding).padding(start = 48.dp, end = 48.dp, top = 16.dp)) {
                if (p.showScreenHeader || p.topBarActions != null) {
                    Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.Bottom) {
                        if (p.showScreenHeader) Column(Modifier.weight(1f)) {
                            MzKicker(tr("Section", "قسم"))
                            Text(p.title, color = MZ.Text, fontSize = 32.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Bold, maxLines = 1)
                            p.subtitle?.takeIf { it.isNotBlank() }?.let { Text(it, color = MZ.Sub, fontSize = 14.sp, fontFamily = MZ.Serif, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, maxLines = 1) }
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

/** Front page: lead story (big photo left, headline column right), a three-column "In brief" strip of
 *  continue-watching, then sections laid out as ruled columns of posters and channel listings. */
@Composable
internal fun MagazineMediaDashboard(p: DashboardParams) {
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
    val tRecentCh = tr("Recently tuned", "شوهدت مؤخراً")
    val tRec = tr("Critics' picks", "مقترح لك")
    val tTop = tr("Top rated", "الأعلى تقييماً")
    val tNew = tr("New this week", "وصل حديثاً")
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 48.dp), verticalArrangement = Arrangement.spacedBy(26.dp)) {
        item {
            Row(Modifier.fillMaxWidth().height(340.dp), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                Box(Modifier.weight(1.5f).fillMaxHeight().background(MZ.Card)) {
                    s.feature.artworkUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                }
                Box(Modifier.width(1.dp).fillMaxHeight().background(MZ.Line))
                Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    MzKicker(tr("Cover story", "قصة الغلاف") + (s.provider?.name?.let { " · $it" } ?: ""))
                    Text(s.feature.title.ifBlank { s.provider?.name ?: "MagazineMedia" }, color = MZ.Text, fontSize = 40.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Black, maxLines = 3, lineHeight = 44.sp, overflow = TextOverflow.Ellipsis)
                    MzRule(Modifier.width(60.dp), MZ.Amber, 3.dp)
                    Text(s.feature.summary, color = MZ.Sub, fontSize = 15.sp, fontFamily = MZ.Serif, lineHeight = 22.sp, maxLines = 4, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.weight(1f))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MzButton(s.feature.actionLabel.ifBlank { tr("Read on · Watch", "شاهد الآن") }, featureAction, Modifier.focusRequester(first), primary = true, icon = "▶")
                        MzButton(tr("Live", "مباشر"), { p.onNavigate(Routes.LIVE_TV) })
                        MzButton(tr("Listings", "الدليل"), { p.onNavigate(Routes.EPG) })
                    }
                    Text(
                        "${s.stats.liveChannelCount} " + tr("channels", "قناة") + "  ·  ${s.stats.movieLibraryCount} " + tr("films", "فيلم") + "  ·  ${s.stats.seriesLibraryCount} " + tr("series", "مسلسل"),
                        color = MZ.Faint, fontSize = 12.sp, fontFamily = MZ.Serif, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }
        if (s.continueWatching.isNotEmpty()) item {
            Column {
                MzRowTitle(tr("Continue reading · watching", "متابعة المشاهدة"), trailing = "${s.continueWatching.size}")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(20.dp), contentPadding = PaddingValues(vertical = 6.dp)) {
                    items(s.continueWatching, key = { "cw${it.id}" }) { h ->
                        val prog = if (h.totalDurationMs > 0) h.resumePositionMs.toFloat() / h.totalDurationMs else null
                        val left = if (h.totalDurationMs > 0) mzDuration(h.totalDurationMs - h.resumePositionMs) + " " + tr("left", "متبقي") else null
                        MzWide(h.title, h.posterUrl, left, prog, { p.onContinueWatchingItemClick(h) }, Modifier.width(300.dp))
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
                    MzRowTitle(title, trailing = "${channels.size}")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(0.dp), contentPadding = PaddingValues(vertical = 6.dp)) {
                        items(channels, key = { "$title${it.id}" }) { c -> MzChannelTile(c, c.id in p.recordingChannelIds) { click(c, s.currentCombinedProfileId) } }
                    }
                }
            }
        }
        listOf(tRec to s.recommendedMovies, tTop to s.topRatedMovies, tNew to s.recentMovies).forEach { (title, movies) ->
            if (movies.isNotEmpty()) item {
                Column {
                    MzRowTitle(title)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(vertical = 6.dp)) {
                        items(movies, key = { "$title${it.id}" }) { m -> MzPoster(m.name, m.posterUrl, m.year, { p.onMovieClick(m) }, Modifier.width(140.dp)) }
                    }
                }
            }
        }
        if (s.recentSeries.isNotEmpty()) item {
            Column {
                MzRowTitle(tr("Serials", "مسلسلات جديدة"))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(vertical = 6.dp)) {
                    items(s.recentSeries, key = { "rs${it.id}" }) { m -> MzPoster(m.name, m.posterUrl, m.genre, { p.onSeriesClick(m) }, Modifier.width(140.dp)) }
                }
            }
        }
    }
}

/** Channel as a listings column: stamp + name, now-showing headline, vertical rule on the end edge. */
@Composable
private fun MzChannelTile(c: Channel, recording: Boolean, onClick: () -> Unit) {
    val now = c.currentProgram
    Row {
        MzCard(onClick = onClick, modifier = Modifier.width(240.dp), container = Color.Transparent, focusedContainer = MZ.Raised) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MzLogo(c.name, c.logoUrl, 40.dp)
                    Column(Modifier.weight(1f)) {
                        Text(c.name, color = MZ.Text, fontSize = 14.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (c.number > 0) Text("No. ${c.number}", color = MZ.Faint, fontSize = 11.sp)
                            if (recording) MzBadge("REC", MZ.Live, filled = true)
                            c.qualityBadge()?.let { MzBadge(it, MZ.Sub) }
                        }
                    }
                }
                Text(now?.title ?: tr("No listing", "لا يوجد دليل"), color = MZ.Sub, fontSize = 13.sp, fontFamily = MZ.Serif, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, maxLines = 1, overflow = TextOverflow.Ellipsis)
                now?.let { MzProgress((System.currentTimeMillis() - it.startTime).toFloat() / (it.endTime - it.startTime).coerceAtLeast(1)) }
            }
        }
        Box(Modifier.padding(horizontal = 8.dp).width(1.dp).height(96.dp).background(MZ.Line))
    }
}

/** Live: three ruled newspaper columns. Section index (categories) | the listings page (channels, set like
 *  TV-listings text with time + programme) | "Now showing" feature with the preview photo on top. */
@Composable
internal fun MagazineMediaLiveTv(p: LiveTvParams) {
    Row(Modifier.fillMaxSize()) {
        Column(Modifier.width(230.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            MzKicker(tr("Index", "الفهرس"))
            MzSearchField(p.categorySearchQuery, p.onCategorySearchChange, tr("Sections…", "التصنيفات…"), Modifier.fillMaxWidth())
            LazyColumn(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                items(p.categories, key = { it.id }) { cat ->
                    val sel = cat.id == p.selectedCategoryId
                    MzCard(
                        onClick = { p.onCategoryClick(cat) }, onLongClick = { p.onCategoryLongClick(cat) }, zoom = 1.0f,
                        container = Color.Transparent, focusedContainer = MZ.Raised,
                        modifier = Modifier.fillMaxWidth().focusRequester(p.categoryRequester(cat.id)).onFocusChanged { if (it.isFocused) p.onCategoryFocused(cat) }
                            .mzRight { p.onRequestChannelsFromCategory() }
                    ) {
                        Column {
                            Row(Modifier.padding(horizontal = 8.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (sel) Text("▸ ", color = MZ.Amber, fontSize = 14.sp)
                                Text((if (p.isCategoryLocked(cat)) "🔒 " else "") + cat.name, color = if (sel) MZ.Amber else MZ.Text, fontSize = 14.sp, fontFamily = MZ.Serif, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                if (cat.count > 0) Text("p.${cat.count}", color = MZ.Faint, fontSize = 11.sp, fontFamily = MZ.Serif, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                            }
                            MzRule(color = MZ.Line)
                        }
                    }
                }
            }
        }
        Box(Modifier.padding(horizontal = 18.dp).width(1.dp).fillMaxHeight().background(MZ.Text))
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f)) {
                    MzKicker(tr("Listings", "القنوات"))
                    Text("${p.channels.size} " + tr("channels tonight", "قناة"), color = MZ.Text, fontSize = 20.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Bold)
                }
                MzSearchField(p.channelSearchQuery, p.onChannelSearchChange, tr("Find a channel", "ابحث عن قناة"), Modifier.width(240.dp))
            }
            MzRule(thick = 2.dp)
            LazyColumn(contentPadding = PaddingValues(vertical = 4.dp)) {
                items(p.channels, key = { it.id }) { c ->
                    val locked = p.isChannelLocked(c)
                    val current = c.id == p.previewChannel?.id
                    MzCard(
                        onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c) }, zoom = 1.0f,
                        container = if (current) MZ.Gold.copy(alpha = 0.5f) else Color.Transparent, focusedContainer = MZ.Raised,
                        modifier = Modifier.fillMaxWidth().focusRequester(p.channelRequester(c.id)).onFocusChanged { if (it.isFocused) p.onChannelFocused(c) }
                            .mzRight { p.onRequestPreviewFromChannel() }
                    ) {
                        Column {
                            Row(Modifier.padding(horizontal = 8.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(if (c.number > 0) "${c.number}" else "·", color = MZ.Amber, fontSize = 18.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Black, modifier = Modifier.width(44.dp))
                                MzLogo(c.name, if (locked) null else c.logoUrl, 38.dp)
                                Column(Modifier.weight(1f)) {
                                    Text(if (c.id == p.movingChannelId) "⇅  ${c.name}" else c.name, color = MZ.Text, fontSize = 15.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    val prog = c.currentProgram
                                    Text(
                                        if (locked) tr("Locked", "مقفل") else prog?.let { "${mzClock(it.startTime)}  ${it.title}" } ?: tr("No listing", "لا يوجد دليل"),
                                        color = MZ.Sub, fontSize = 12.sp, fontFamily = MZ.Serif, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, maxLines = 1, overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (c.isFavorite) Text("★", color = MZ.Amber, fontSize = 14.sp)
                                if (!locked) c.qualityBadge()?.let { MzBadge(it, MZ.Sub) }
                                if (c.catchUpSupported) MzBadge(tr("Archive", "أرشيف"), MZ.Blue)
                            }
                            MzRule(color = MZ.Line)
                        }
                    }
                }
            }
        }
        Box(Modifier.padding(horizontal = 18.dp).width(1.dp).fillMaxHeight().background(MZ.Text))
        Column(Modifier.width(380.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MzKicker(tr("Now showing", "يعرض الآن"))
            MzCard(
                onClick = { p.previewChannel?.let(p.onChannelClick) }, container = Color.Black, zoom = 1.0f,
                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).focusRequester(p.previewFocusRequester).mzLeft { p.onRequestChannelsFromPreview() }
            ) {
                val engine = p.previewPlayerEngine
                if (engine != null && p.previewChannel != null) PlayerRenderView(engine, PlayerSurfaceResizeMode.FIT, Modifier.fillMaxSize())
                when {
                    p.previewErrorMessage != null -> Text(p.previewErrorMessage, color = MZ.Gold, fontSize = 13.sp, modifier = Modifier.align(Alignment.Center).padding(16.dp))
                    p.isPreviewLoading -> Text(tr("Loading…", "جار التحميل…"), color = MZ.Gold, fontFamily = MZ.Serif, modifier = Modifier.align(Alignment.Center))
                    p.previewChannel == null -> Text(tr("Select a channel", "اختر قناة"), color = MZ.Gold, fontFamily = MZ.Serif, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, modifier = Modifier.align(Alignment.Center))
                }
            }
            Text(tr("Fig. 1 — live picture", "صورة مباشرة"), color = MZ.Faint, fontSize = 11.sp, fontFamily = MZ.Serif, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            val c = p.previewChannel
            if (c != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MzBadge("● LIVE", MZ.Live, filled = true)
                    if (c.number > 0) MzBadge("No. ${c.number}", MZ.Sub)
                    c.qualityBadge()?.let { MzBadge(it, MZ.Sub) }
                }
                Text(c.name, color = MZ.Text, fontSize = 24.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Black, maxLines = 1)
                MzRule()
                c.currentProgram?.let { now ->
                    Text(now.title, color = MZ.Text, fontSize = 17.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Bold, maxLines = 2)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(mzClock(now.startTime), color = MZ.Sub, fontSize = 12.sp)
                        MzProgress((System.currentTimeMillis() - now.startTime).toFloat() / (now.endTime - now.startTime).coerceAtLeast(1), Modifier.weight(1f))
                        Text(mzClock(now.endTime), color = MZ.Sub, fontSize = 12.sp)
                    }
                    Text(now.description, color = MZ.Sub, fontSize = 13.sp, fontFamily = MZ.Serif, lineHeight = 19.sp, maxLines = 4, overflow = TextOverflow.Ellipsis)
                } ?: Text(tr("No listing available", "لا يوجد دليل"), color = MZ.Faint, fontFamily = MZ.Serif, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                c.nextProgram?.let {
                    MzRule(color = MZ.Line)
                    MzKicker(tr("Later", "التالي"), color = MZ.Sub)
                    Text("${mzClock(it.startTime)}  ${it.title}", color = MZ.Text, fontSize = 14.sp, fontFamily = MZ.Serif, maxLines = 1)
                }
            }
        }
    }
}
