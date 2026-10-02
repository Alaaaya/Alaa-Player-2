package com.streamvault.app.ui.themes.techdashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.streamvault.app.navigation.Routes
import com.streamvault.app.ui.components.PlayerRenderView
import com.streamvault.app.ui.components.SearchInput
import com.streamvault.app.ui.screens.dashboard.DashboardFeatureAction
import com.streamvault.app.ui.themes.bespoke.DashboardParams
import com.streamvault.app.ui.themes.bespoke.LiveTvParams
import com.streamvault.app.ui.themes.bespoke.qualityBadge
import com.streamvault.app.ui.themes.bespoke.tr
import com.streamvault.domain.model.Channel
import com.streamvault.player.PlayerSurfaceResizeMode

internal fun Modifier.onDown(handler: () -> Boolean): Modifier = this.then(
    Modifier.onPreviewKeyEvent { e ->
        e.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN && e.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_DOWN && handler()
    }
)

/** Bordered module panel with a mono header strip, the building block of every dashboard page. */
@Composable
internal fun TdModule(title: String, modifier: Modifier = Modifier, meta: String? = null, content: @Composable () -> Unit) {
    Column(modifier.clip(TD.Panel).background(TD.Deep.copy(alpha = 0.82f)).border(1.dp, TD.Plasma.copy(alpha = 0.22f), TD.Panel)) {
        Row(Modifier.fillMaxWidth().background(TD.Plasma.copy(alpha = 0.08f)).padding(horizontal = 12.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("■ " + title.uppercase(), color = TD.Plasma, fontSize = 11.sp, fontFamily = TD.Mono, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.weight(1f))
            if (meta != null) Text(meta, color = TD.Muted, fontSize = 11.sp, fontFamily = TD.Mono)
        }
        Box(Modifier.padding(10.dp)) { content() }
    }
}

@Composable
private fun KpiTile(value: String, label: String, accent: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TechDashSurface(onClick = onClick, container = TD.Deep, modifier = modifier) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(value, color = accent, fontSize = 30.sp, fontFamily = TD.Mono, fontWeight = FontWeight.Bold)
            Text(label.uppercase(), color = TD.Dust, fontSize = 10.sp, fontFamily = TD.Mono, letterSpacing = 1.sp)
        }
    }
}

/** Channel table row: CH | logo | name / now | meter | badges. Shared by home and live. */
@Composable
internal fun TdChannelRow(c: Channel, locked: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, onLongClick: (() -> Unit)? = null, highlighted: Boolean = false, recording: Boolean = false, moving: Boolean = false) {
    val now = c.currentProgram
    val progress = now?.let { ((System.currentTimeMillis() - it.startTime).toFloat() / (it.endTime - it.startTime).coerceAtLeast(1)) }
    TechDashSurface(onClick = onClick, onLongClick = onLongClick, shape = TD.Pill, container = if (highlighted) TD.GlassStrong else Color.Transparent, modifier = modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(if (c.number > 0) "%03d".format(c.number) else "---", color = TD.Plasma, fontSize = 12.sp, fontFamily = TD.Mono, modifier = Modifier.width(36.dp))
            TechDashLogo(c.name, if (locked) null else c.logoUrl, 34.dp)
            Column(Modifier.weight(1f)) {
                Text(if (moving) "⇅  ${c.name}" else c.name, color = TD.Star, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(if (locked) tr("Locked", "مقفل") else now?.title ?: "EPG: n/a", color = TD.Dust, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (progress != null && !locked) Box(Modifier.width(90.dp)) { TechDashProgress(progress, height = 3.dp) }
            if (recording) TechDashBadge("REC", TD.Live)
            if (!locked) c.qualityBadge()?.let { TechDashBadge(it, TD.Comet) }
            if (c.catchUpSupported) TechDashBadge("⟲", TD.Flare)
            Text(if (c.isFavorite) "★" else "☆", color = if (c.isFavorite) TD.Flare else TD.Muted.copy(alpha = 0.5f), fontSize = 14.sp)
        }
    }
}

/** Home as an operations dashboard: KPI strip, featured feed console, session table and channel tables side by side. */
@Composable
internal fun TechDashDashboard(p: DashboardParams) {
    val s = p.uiState
    val first = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { first.requestFocus() } }
    val pinnedLabel = tr("Pinned channels", "القنوات المفضلة")
    val recentLabel = tr("Recent channels", "القنوات الأخيرة")
    val vodRows = listOf(
        tr("Recommended", "مقترح") to s.recommendedMovies,
        tr("Top rated", "الأعلى تقييماً") to s.topRatedMovies,
        tr("Recently added", "أضيف حديثاً") to s.recentMovies
    )
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 40.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiTile("${s.stats.liveChannelCount}", tr("Live channels", "قنوات مباشرة"), TD.Plasma, { p.onNavigate(Routes.LIVE_TV) }, Modifier.weight(1f).focusRequester(first))
                KpiTile("${s.stats.movieLibraryCount}", tr("Movies", "أفلام"), TD.Comet, { p.onNavigate(Routes.MOVIES) }, Modifier.weight(1f))
                KpiTile("${s.stats.seriesLibraryCount}", tr("Series", "مسلسلات"), TD.Flare, { p.onNavigate(Routes.SERIES) }, Modifier.weight(1f))
                KpiTile("${s.continueWatching.size}", tr("Open sessions", "متابعة"), TD.Star, { s.continueWatching.firstOrNull()?.let(p.onContinueWatchingItemClick) ?: p.onNavigate(Routes.MOVIES) }, Modifier.weight(1f))
                KpiTile(formatClock(System.currentTimeMillis()), tr("Guide", "الدليل"), TD.Dust, { p.onNavigate(Routes.EPG) }, Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth().height(250.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                TdModule(tr("Featured feed", "المميز"), Modifier.weight(1.4f).fillMaxHeight(), meta = s.provider?.name) {
                    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Box(Modifier.width(300.dp).fillMaxHeight().clip(TD.Card).background(TD.Nebula)) {
                            s.feature.artworkUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                            Text("[ FEED ]", color = TD.Plasma, fontSize = 10.sp, fontFamily = TD.Mono, modifier = Modifier.padding(6.dp).background(TD.Void.copy(alpha = 0.7f)).padding(horizontal = 4.dp))
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("$ play --featured", color = TD.Muted, fontSize = 11.sp, fontFamily = TD.Mono)
                            TechDashTitle(s.feature.title.ifBlank { s.provider?.name ?: "Tech Dashboard" }, size = 24)
                            Text(s.feature.summary, color = TD.Dust, fontSize = 13.sp, maxLines = 4, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.weight(1f))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TechDashChip("▶ " + s.feature.actionLabel.ifBlank { tr("Execute", "تشغيل") }, true, onClick = {
                                    when (s.feature.actionType) {
                                        DashboardFeatureAction.LIVE -> p.onNavigate(Routes.LIVE_TV)
                                        DashboardFeatureAction.CONTINUE_WATCHING -> s.continueWatching.firstOrNull()?.let(p.onContinueWatchingItemClick) ?: p.onNavigate(Routes.MOVIES)
                                        else -> p.onNavigate(Routes.MOVIES)
                                    }
                                })
                                TechDashChip("⌕ " + tr("Query", "بحث"), false, onClick = { p.onNavigate(Routes.SEARCH) })
                            }
                        }
                    }
                }
                TdModule(tr("Continue watching", "متابعة المشاهدة"), Modifier.weight(1f).fillMaxHeight(), meta = "${s.continueWatching.size} rows") {
                    if (s.continueWatching.isEmpty()) Text("> 0 open sessions", color = TD.Muted, fontSize = 12.sp, fontFamily = TD.Mono)
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(s.continueWatching, key = { "cw${it.id}" }) { h ->
                            TechDashSurface(onClick = { p.onContinueWatchingItemClick(h) }, shape = TD.Pill, container = Color.Transparent, modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(horizontal = 8.dp, vertical = 5.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(h.title, color = TD.Star, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                        Text(formatDuration(h.resumePositionMs), color = TD.Plasma, fontSize = 11.sp, fontFamily = TD.Mono)
                                    }
                                    if (h.totalDurationMs > 0) TechDashProgress(h.resumePositionMs.toFloat() / h.totalDurationMs, height = 3.dp)
                                }
                            }
                        }
                    }
                }
            }
        }
        if (s.favoriteChannels.isNotEmpty() || s.recentChannels.isNotEmpty()) item {
            Row(Modifier.fillMaxWidth().height(260.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                listOf(
                    Triple(pinnedLabel, s.favoriteChannels, p.onFavoriteChannelClick),
                    Triple(recentLabel, s.recentChannels, p.onRecentChannelClick)
                ).forEach { (title, channels, click) ->
                    TdModule(title, Modifier.weight(1f).fillMaxHeight(), meta = "${channels.size}") {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            items(channels, key = { "$title${it.id}" }) { c ->
                                TdChannelRow(c, false, { click(c, s.currentCombinedProfileId) }, recording = c.id in p.recordingChannelIds)
                            }
                        }
                    }
                }
            }
        }
        vodRows.forEach { (title, movies) ->
            if (movies.isNotEmpty()) item {
                TdModule(title, meta = "${movies.size} rows") {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(movies, key = { "$title${it.id}" }) { m -> TechDashPoster(m.name, m.posterUrl, m.year, { p.onMovieClick(m) }, Modifier.width(132.dp)) }
                    }
                }
            }
        }
        if (s.recentSeries.isNotEmpty()) item {
            TdModule(tr("New series", "مسلسلات جديدة"), meta = "${s.recentSeries.size} rows") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(s.recentSeries, key = { "rs${it.id}" }) { m -> TechDashPoster(m.name, m.posterUrl, m.genre, { p.onSeriesClick(m) }, Modifier.width(132.dp)) }
                }
            }
        }
    }
}

/** Live TV as a channel table: group tabs on top, dense table left, monitor + EPG read-out stacked on the right. */
@Composable
internal fun TechDashLiveTv(p: LiveTvParams) {
    // Alaa rule: categories column + channels column side by side (no tabs, no grid), monitor/now-next beside them.
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        TdModule(tr("Groups", "الفئات"), Modifier.width(280.dp).fillMaxHeight(), meta = "${p.categories.size}") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SearchInput(p.categorySearchQuery, p.onCategorySearchChange, tr("filter groups", "فلترة"), Modifier.fillMaxWidth())
                LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    items(p.categories, key = { it.id }) { c ->
                        val selected = c.id == p.selectedCategoryId
                        TechDashSurface(
                            onClick = { p.onCategoryClick(c) }, onLongClick = { p.onCategoryLongClick(c) },
                            container = if (selected) TD.Plasma else TD.Glass,
                            modifier = Modifier.fillMaxWidth().focusRequester(p.categoryRequester(c.id)).onFocusChanged { if (it.isFocused) p.onCategoryFocused(c) }
                                .onRight { p.onRequestChannelsFromCategory() }
                        ) {
                            Row(Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text((if (selected) "> " else "  ") + (if (p.isCategoryLocked(c)) "🔒 " else "") + c.name, color = if (selected) TD.Void else TD.Star,
                                    fontSize = 12.sp, fontFamily = TD.Mono, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                if (c.count > 0) Text("${c.count}", color = if (selected) TD.Void else TD.Muted, fontSize = 11.sp, fontFamily = TD.Mono)
                            }
                        }
                    }
                }
            }
        }
            TdModule(p.sourceTitle.ifBlank { tr("Channel index", "القنوات") }, Modifier.weight(1f).fillMaxHeight(), meta = "${p.channels.size} online") {
                Column {
                    Row(Modifier.padding(bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("CH   LOGO  NAME / NOW", color = TD.Muted, fontSize = 10.sp, fontFamily = TD.Mono, modifier = Modifier.weight(1f))
                        SearchInput(p.channelSearchQuery, p.onChannelSearchChange, "grep channel", Modifier.width(220.dp))
                    }
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        items(p.channels, key = { it.id }) { c ->
                            TdChannelRow(
                                c, p.isChannelLocked(c), { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c) }, moving = c.id == p.movingChannelId, highlighted = c.id == p.previewChannel?.id,
                                modifier = Modifier.focusRequester(p.channelRequester(c.id)).onFocusChanged { if (it.isFocused) p.onChannelFocused(c) }.onRight { p.onRequestPreviewFromChannel() }
                            )
                        }
                    }
                }
            }
            Column(Modifier.width(400.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TdModule(tr("Monitor", "المعاينة"), meta = if (p.previewChannel != null) "● LIVE" else "IDLE") {
                    TechDashSurface(
                        onClick = { p.previewChannel?.let(p.onChannelClick) }, container = TD.Void,
                        modifier = Modifier.fillMaxWidth().height(210.dp).focusRequester(p.previewFocusRequester).onLeft { p.onRequestChannelsFromPreview() }
                    ) {
                        val engine = p.previewPlayerEngine
                        if (engine != null && p.previewChannel != null) PlayerRenderView(engine, PlayerSurfaceResizeMode.FIT, Modifier.fillMaxSize())
                        when {
                            p.previewErrorMessage != null -> Text("ERR: " + p.previewErrorMessage, color = TD.Live, fontSize = 12.sp, fontFamily = TD.Mono, modifier = Modifier.align(Alignment.Center).padding(16.dp))
                            p.isPreviewLoading -> Text("buffering…", color = TD.Dust, fontFamily = TD.Mono, modifier = Modifier.align(Alignment.Center))
                            p.previewChannel == null -> Text(tr("Select a channel", "اختر قناة"), color = TD.Muted, modifier = Modifier.align(Alignment.Center))
                        }
                    }
                }
                TdModule(tr("Now / next", "الآن / التالي"), Modifier.fillMaxWidth().weight(1f)) {
                    val c = p.previewChannel
                    if (c == null) Text("> no channel selected", color = TD.Muted, fontSize = 12.sp, fontFamily = TD.Mono)
                    else Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(c.name, color = TD.Star, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        c.currentProgram?.let { now ->
                            Row { Text("NOW ", color = TD.Plasma, fontSize = 11.sp, fontFamily = TD.Mono); Text("${formatClock(now.startTime)}–${formatClock(now.endTime)}", color = TD.Dust, fontSize = 11.sp, fontFamily = TD.Mono) }
                            Text(now.title, color = TD.Star, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            TechDashProgress((System.currentTimeMillis() - now.startTime).toFloat() / (now.endTime - now.startTime).coerceAtLeast(1), height = 4.dp)
                            Text(now.description, color = TD.Dust, fontSize = 12.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                        } ?: Text("EPG: n/a", color = TD.Muted, fontFamily = TD.Mono, fontSize = 12.sp)
                        c.nextProgram?.let { next ->
                            Row { Text("NXT ", color = TD.Flare, fontSize = 11.sp, fontFamily = TD.Mono); Text("${formatClock(next.startTime)} ${next.title}", color = TD.Dust, fontSize = 12.sp, maxLines = 1) }
                        }
                    }
                }
            }
        }
}
