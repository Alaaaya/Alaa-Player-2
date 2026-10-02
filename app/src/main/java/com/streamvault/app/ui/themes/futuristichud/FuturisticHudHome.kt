package com.streamvault.app.ui.themes.futuristichud

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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
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

/** HUD input: chamfered bar, "QUERY>" prompt, cyan brackets when focused. */
@Composable
internal fun FhSearchField(value: String, onChange: (String) -> Unit, hint: String, modifier: Modifier = Modifier, onSubmit: (() -> Unit)? = null) {
    var focused by remember { mutableStateOf(false) }
    Row(
        modifier.clip(FH.RSmall).background(FH.Card).border(1.dp, if (focused) FH.Amber else FH.Line, FH.RSmall)
            .then(if (focused) Modifier.fhBrackets() else Modifier).padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("QRY>", color = if (focused) FH.Amber else FH.Faint, fontSize = 12.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Bold)
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) Text(hint.uppercase(), color = FH.Faint, fontSize = 12.sp, fontFamily = FH.Mono, maxLines = 1)
            BasicTextField(
                value = value, onValueChange = onChange, singleLine = true,
                textStyle = TextStyle(color = FH.Text, fontSize = 13.sp, fontFamily = FH.Mono), cursorBrush = SolidColor(FH.Amber),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { onSubmit?.invoke() }, onDone = { onSubmit?.invoke() }),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                modifier = Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused }
            )
        }
    }
}

private class FhDest(val route: String, val code: String, val en: String, val ar: String)

private val fhDestinations = listOf(
    FhDest(Routes.HOME, "00", "Base", "الرئيسية"),
    FhDest(Routes.LIVE_TV, "01", "Live", "مباشر"),
    FhDest(Routes.EPG, "02", "Guide", "الدليل"),
    FhDest(Routes.MOVIES, "03", "Movies", "أفلام"),
    FhDest(Routes.SERIES, "04", "Series", "مسلسلات"),
    FhDest(Routes.FAVORITES, "05", "Saved", "المفضلة"),
    FhDest(Routes.SEARCH, "06", "Scan", "بحث"),
    FhDest(Routes.SETTINGS, "07", "Config", "الإعدادات")
)

private fun onRoute(current: String, route: String) = current == route || current.startsWith("$route?") || current.startsWith("$route/")

/** Shell: a vertical "altimeter" rail on the start edge. Each destination is a numbered code + short label on a ticked
 *  scale line; the active one gets a cyan pointer. A status strip runs along the top of the content (clock, link status). */
@Composable
internal fun FuturisticHudShell(p: ShellParams) {
    FhBackground(p.modifier) {
        Row(Modifier.fillMaxSize()) {
            if (p.topBarVisible) {
                Column(
                    Modifier.fillMaxHeight().width(132.dp).background(FH.Bg.copy(alpha = 0.9f))
                        .drawBehind { drawLine(FH.Amber.copy(alpha = 0.5f), Offset(size.width - 1f, 0f), Offset(size.width - 1f, size.height), 2f) }
                        .padding(vertical = 22.dp, horizontal = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("◢◤ HUD", color = FH.Amber, fontSize = 16.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 6.dp))
                    Text("ALAA//SYS", color = FH.Faint, fontSize = 9.sp, fontFamily = FH.Mono, letterSpacing = 2.sp, modifier = Modifier.padding(start = 6.dp, bottom = 18.dp))
                    fhDestinations.forEach { d ->
                        val active = onRoute(p.currentRoute, d.route)
                        FhCard(
                            onClick = { if (!active) p.onNavigate(d.route) }, shape = FH.RSmall, zoom = 1.0f,
                            container = if (active) FH.Amber.copy(alpha = 0.14f) else Color.Transparent, focusedContainer = FH.Amber.copy(alpha = 0.3f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(Modifier.padding(horizontal = 6.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(if (active) "▶" else "–", color = if (active) FH.Amber else FH.Line, fontSize = 10.sp, fontFamily = FH.Mono)
                                Text(d.code, color = if (active) FH.Amber else FH.Faint, fontSize = 11.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Bold)
                                Text(tr(d.en, d.ar).uppercase(), color = if (active) FH.Text else FH.Sub, fontSize = 12.sp, fontFamily = FH.Mono, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    repeat(6) { i -> Box(Modifier.padding(start = 8.dp).width(if (i % 3 == 0) 22.dp else 10.dp).height(1.dp).background(FH.Line)) ; Spacer(Modifier.height(5.dp)) }
                }
            }
            Column(Modifier.weight(1f).fillMaxHeight().padding(p.contentPadding).padding(start = 24.dp, end = 36.dp, top = 14.dp)) {
                Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("● LINK", color = Color(0xFF3CFFA8), fontSize = 10.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Bold)
                    Box(Modifier.weight(1f).height(1.dp).background(FH.Line))
                    Text("T+" + fhClock(System.currentTimeMillis()), color = FH.Amber, fontSize = 12.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Bold)
                }
                if (p.showScreenHeader || p.topBarActions != null) {
                    Row(Modifier.fillMaxWidth().padding(bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (p.showScreenHeader) Column(Modifier.weight(1f)) {
                            Text("[ " + p.title.uppercase() + " ]", color = FH.Text, fontSize = 24.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Black, letterSpacing = 2.sp, maxLines = 1)
                            p.subtitle?.takeIf { it.isNotBlank() }?.let { Text("> $it", color = FH.Sub, fontSize = 12.sp, fontFamily = FH.Mono, maxLines = 1) }
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

/** Home: targeting hero (telemetry readout START, artwork inside a crosshair reticle frame END), then continue-watching
 *  as a horizontal "mission log" strip, channel signal chips, and poster shelves. */
@Composable
internal fun FuturisticHudDashboard(p: DashboardParams) {
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
    val tRecentCh = tr("Recent signals", "شوهدت مؤخراً")
    val tRec = tr("Recommended", "مقترح لك")
    val tTop = tr("Top rated", "الأعلى تقييماً")
    val tNew = tr("New arrivals", "وصل حديثاً")
    val tCh = tr("Channels", "قنوات"); val tMv = tr("Movies", "أفلام"); val tSr = tr("Series", "مسلسلات")
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 48.dp), verticalArrangement = Arrangement.spacedBy(26.dp)) {
        item {
            Row(Modifier.fillMaxWidth().height(340.dp), horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                Column(Modifier.weight(0.9f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FhLabel(tr("Target acquired", "الهدف") + " // " + (s.provider?.name ?: "SYS"))
                    Text(s.feature.title.ifBlank { s.provider?.name ?: "HUD" }.uppercase(), color = FH.Text, fontSize = 34.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Black, maxLines = 2, lineHeight = 38.sp)
                    Text(s.feature.summary, color = FH.Sub, fontSize = 13.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(tCh to s.stats.liveChannelCount, tMv to s.stats.movieLibraryCount, tSr to s.stats.seriesLibraryCount).forEach { (l, n) ->
                            Column(Modifier.border(1.dp, FH.Line, FH.RSmall).padding(horizontal = 12.dp, vertical = 6.dp)) {
                                Text("$n", color = FH.Amber, fontSize = 20.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Black)
                                Text(l.uppercase(), color = FH.Faint, fontSize = 9.sp, fontFamily = FH.Mono, letterSpacing = 1.sp)
                            }
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FhButton(s.feature.actionLabel.ifBlank { tr("Engage", "شاهد الآن") }, featureAction, Modifier.focusRequester(first), primary = true, icon = "▶")
                        FhButton(tr("Live", "مباشر"), { p.onNavigate(Routes.LIVE_TV) }, icon = "◉")
                        FhButton(tr("Guide", "الدليل"), { p.onNavigate(Routes.EPG) }, icon = "▦")
                    }
                }
                Box(Modifier.weight(1.1f).fillMaxHeight().clip(FH.R).background(FH.Card).border(1.dp, FH.Line, FH.R).fhBrackets(FH.Amber, 22.dp, 3.dp)) {
                    s.feature.artworkUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                    Box(Modifier.fillMaxSize().background(FH.Bg.copy(alpha = 0.25f)).drawBehind {
                        val c = FH.Amber.copy(alpha = 0.55f); val cx = size.width / 2; val cy = size.height / 2; val r = size.minDimension / 5
                        drawCircle(c, r, Offset(cx, cy), style = androidx.compose.ui.graphics.drawscope.Stroke(1.5f))
                        drawLine(c, Offset(cx - r * 1.6f, cy), Offset(cx - r * 0.4f, cy), 1.5f); drawLine(c, Offset(cx + r * 0.4f, cy), Offset(cx + r * 1.6f, cy), 1.5f)
                        drawLine(c, Offset(cx, cy - r * 1.6f), Offset(cx, cy - r * 0.4f), 1.5f); drawLine(c, Offset(cx, cy + r * 0.4f), Offset(cx, cy + r * 1.6f), 1.5f)
                        var y = 0f; while (y < size.height) { drawLine(Color.Black.copy(alpha = 0.18f), Offset(0f, y), Offset(size.width, y), 1f); y += 4f }
                    })
                    Text("LOCK ◎", color = FH.Amber, fontSize = 10.sp, fontFamily = FH.Mono, modifier = Modifier.align(Alignment.TopEnd).padding(12.dp))
                }
            }
        }
        if (s.continueWatching.isNotEmpty()) item {
            Column {
                FhRowTitle(tr("Mission log · resume", "متابعة المشاهدة"), trailing = "${s.continueWatching.size}")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(vertical = 6.dp)) {
                    items(s.continueWatching, key = { "cw${it.id}" }) { h ->
                        val prog = if (h.totalDurationMs > 0) h.resumePositionMs.toFloat() / h.totalDurationMs else 0f
                        FhCard(onClick = { p.onContinueWatchingItemClick(h) }, modifier = Modifier.width(340.dp).height(96.dp), container = FH.Card) {
                            Row(Modifier.fillMaxSize()) {
                                Box(Modifier.width(150.dp).fillMaxHeight()) { h.posterUrl?.let { AsyncImage(it, h.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) } }
                                Column(Modifier.weight(1f).padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(h.title.uppercase(), color = FH.Text, fontSize = 12.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    Spacer(Modifier.weight(1f))
                                    Text("${(prog * 100).toInt()}% " + (if (h.totalDurationMs > 0) "· -" + fhDuration(h.totalDurationMs - h.resumePositionMs) else ""), color = FH.Amber, fontSize = 10.sp, fontFamily = FH.Mono)
                                    FhProgress(prog, height = 4.dp)
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
                    FhRowTitle(title, trailing = "${channels.size}")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 6.dp)) {
                        items(channels, key = { "$title${it.id}" }) { c -> FhChannelTile(c, c.id in p.recordingChannelIds) { click(c, s.currentCombinedProfileId) } }
                    }
                }
            }
        }
        listOf(tRec to s.recommendedMovies, tTop to s.topRatedMovies, tNew to s.recentMovies).forEach { (title, movies) ->
            if (movies.isNotEmpty()) item {
                Column {
                    FhRowTitle(title, trailing = "${movies.size}")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(vertical = 6.dp)) {
                        items(movies, key = { "$title${it.id}" }) { m -> FhPoster(m.name, m.posterUrl, m.year, { p.onMovieClick(m) }, Modifier.width(138.dp)) }
                    }
                }
            }
        }
        if (s.recentSeries.isNotEmpty()) item {
            Column {
                FhRowTitle(tr("New series", "مسلسلات جديدة"))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(vertical = 6.dp)) {
                    items(s.recentSeries, key = { "rs${it.id}" }) { m -> FhPoster(m.name, m.posterUrl, m.genre, { p.onSeriesClick(m) }, Modifier.width(138.dp)) }
                }
            }
        }
    }
}

/** Four-bar signal meter; bars lit by quality. */
@Composable
internal fun FhSignal(level: Int, modifier: Modifier = Modifier) {
    Row(modifier.height(14.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(4) { i -> Box(Modifier.width(3.dp).fillMaxHeight((i + 1) / 4f).background(if (i < level) FH.Amber else FH.Line)) }
    }
}

internal fun Channel.fhSignalLevel(): Int = when (qualityBadge()?.uppercase()) { "4K", "UHD" -> 4; "FHD" -> 4; "HD" -> 3; "SD" -> 2; else -> 3 }

/** Signal chip: compact horizontal tag with logo, code and meter. */
@Composable
private fun FhChannelTile(c: Channel, recording: Boolean, onClick: () -> Unit) {
    val now = c.currentProgram
    FhCard(onClick = onClick, modifier = Modifier.width(250.dp), container = FH.Card, shape = FH.RSmall) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FhLogo(c.name, c.logoUrl, 42.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (c.number > 0) Text("CH%03d".format(c.number), color = FH.Amber, fontSize = 9.sp, fontFamily = FH.Mono)
                    if (recording) FhBadge("REC", FH.Live)
                    Spacer(Modifier.weight(1f)); FhSignal(c.fhSignalLevel())
                }
                Text(c.name.uppercase(), color = FH.Text, fontSize = 12.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(now?.title ?: tr("No guide data", "لا يوجد دليل"), color = FH.Faint, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

/** Live TV: three vertical columns. START "frequency band" categories, centre channel column (code, logo, name, signal, badges),
 *  END preview monitor in a reticle frame with now/next telemetry under it. No grid, no tabs. */
@Composable
internal fun FuturisticHudLiveTv(p: LiveTvParams) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(Modifier.width(220.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FhLabel(tr("Bands", "التصنيفات") + " [${p.categories.size}]")
            FhSearchField(p.categorySearchQuery, p.onCategorySearchChange, tr("Filter", "تصفية"), Modifier.fillMaxWidth())
            LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                items(p.categories, key = { it.id }) { cat ->
                    val sel = cat.id == p.selectedCategoryId
                    FhCard(
                        onClick = { p.onCategoryClick(cat) }, onLongClick = { p.onCategoryLongClick(cat) }, shape = FH.RSmall, zoom = 1.0f,
                        container = if (sel) FH.Amber.copy(alpha = 0.16f) else Color.Transparent, focusedContainer = FH.Amber.copy(alpha = 0.32f),
                        modifier = Modifier.fillMaxWidth().focusRequester(p.categoryRequester(cat.id)).onFocusChanged { if (it.isFocused) p.onCategoryFocused(cat) }
                            .fhRight { p.onRequestChannelsFromCategory() }
                    ) {
                        Row(Modifier.padding(horizontal = 8.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(Modifier.width(3.dp).height(16.dp).background(if (sel) FH.Amber else FH.Line))
                            Text((if (p.isCategoryLocked(cat)) "⊘ " else "") + cat.name.uppercase(), color = if (sel) FH.Amber else FH.Text, fontSize = 12.sp, fontFamily = FH.Mono, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            if (cat.count > 0) Text("%03d".format(cat.count), color = FH.Faint, fontSize = 10.sp, fontFamily = FH.Mono)
                        }
                    }
                }
            }
        }
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FhLabel(tr("Signals", "القنوات") + " [${p.channels.size}]", Modifier.weight(1f))
                FhSearchField(p.channelSearchQuery, p.onChannelSearchChange, tr("Find", "بحث"), Modifier.width(220.dp))
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
                items(p.channels, key = { it.id }) { c ->
                    val locked = p.isChannelLocked(c)
                    val previewing = c.id == p.previewChannel?.id
                    FhCard(
                        onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c) }, zoom = 1.0f, shape = FH.RSmall,
                        container = if (previewing) FH.Amber.copy(alpha = 0.12f) else FH.Card.copy(alpha = 0.7f), focusedContainer = FH.Amber.copy(alpha = 0.28f),
                        modifier = Modifier.fillMaxWidth().focusRequester(p.channelRequester(c.id)).onFocusChanged { if (it.isFocused) p.onChannelFocused(c) }
                            .fhRight { p.onRequestPreviewFromChannel() }
                    ) {
                        Row(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(if (c.number > 0) "%03d".format(c.number) else "---", color = FH.Amber, fontSize = 12.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Bold, modifier = Modifier.width(32.dp))
                            FhLogo(c.name, if (locked) null else c.logoUrl, 38.dp)
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text((if (c.id == p.movingChannelId) "⇅ " else "") + c.name.uppercase(), color = FH.Text, fontSize = 13.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(if (locked) tr("Locked", "مقفل") else c.currentProgram?.title ?: tr("No guide data", "لا يوجد دليل"), color = FH.Faint, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            if (!locked) c.qualityBadge()?.let { FhBadge(it, FH.Text) }
                            if (c.catchUpSupported) FhBadge("ARC", FH.Blue)
                            FhSignal(c.fhSignalLevel())
                            Text(if (c.isFavorite) "★" else "☆", color = if (c.isFavorite) FH.Warn else FH.Faint, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
        Column(Modifier.width(380.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            FhLabel(tr("Monitor", "المعاينة"))
            FhCard(
                onClick = { p.previewChannel?.let(p.onChannelClick) }, container = Color.Black, zoom = 1.0f,
                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).fhBrackets(FH.Amber, 16.dp, 2.dp).focusRequester(p.previewFocusRequester).fhLeft { p.onRequestChannelsFromPreview() }
            ) {
                val engine = p.previewPlayerEngine
                if (engine != null && p.previewChannel != null) PlayerRenderView(engine, PlayerSurfaceResizeMode.FIT, Modifier.fillMaxSize())
                when {
                    p.previewErrorMessage != null -> Text("ERR: " + p.previewErrorMessage, color = FH.Live, fontSize = 12.sp, fontFamily = FH.Mono, modifier = Modifier.align(Alignment.Center).padding(16.dp))
                    p.isPreviewLoading -> Text("ACQUIRING…", color = FH.Amber, fontFamily = FH.Mono, modifier = Modifier.align(Alignment.Center))
                    p.previewChannel == null -> Text("[ NO SIGNAL ]", color = FH.Faint, fontFamily = FH.Mono, modifier = Modifier.align(Alignment.Center))
                }
                if (p.previewChannel != null) Text("● REC LIVE", color = FH.Live, fontSize = 10.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.TopStart).padding(10.dp))
            }
            val c = p.previewChannel
            if (c != null) FhPanel(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FhLogo(c.name, c.logoUrl, 40.dp)
                    Column(Modifier.weight(1f)) {
                        Text(c.name.uppercase(), color = FH.Text, fontSize = 15.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Black, maxLines = 1)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (c.number > 0) FhBadge("CH${c.number}", FH.Sub)
                            c.qualityBadge()?.let { FhBadge(it, FH.Text) }
                            if (c.catchUpSupported) FhBadge(tr("Archive", "أرشيف"), FH.Blue)
                        }
                    }
                }
                c.currentProgram?.let { now ->
                    Text("NOW  " + now.title, color = FH.Text, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(fhClock(now.startTime), color = FH.Sub, fontSize = 10.sp, fontFamily = FH.Mono)
                        FhProgress((System.currentTimeMillis() - now.startTime).toFloat() / (now.endTime - now.startTime).coerceAtLeast(1), Modifier.weight(1f), 5.dp)
                        Text(fhClock(now.endTime), color = FH.Sub, fontSize = 10.sp, fontFamily = FH.Mono)
                    }
                } ?: Text(tr("No guide data", "لا يوجد دليل"), color = FH.Faint, fontSize = 12.sp)
                c.nextProgram?.let { Text("NEXT " + fhClock(it.startTime) + "  " + it.title, color = FH.Sub, fontSize = 12.sp, fontFamily = FH.Mono, maxLines = 1) }
            } else Text(p.sourceTitle.ifBlank { tr("Live TV", "البث المباشر") }.uppercase(), color = FH.Faint, fontSize = 12.sp, fontFamily = FH.Mono)
        }
    }
}
