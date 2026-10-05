package com.streamvault.app.ui.themes.ivano

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import com.streamvault.app.ui.interaction.TvClickableSurface
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.horizontalScroll
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.animation.core.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Path

private val IvBlue = Color(0xFF9B5CFF)
private val IvNavy = Color(0xFF0E0C12)

/** Deep-blue radial background with concentric glowing rings (home). */
@Composable
internal fun IvRings(modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition(label = "uvRings")
    val phase by t.animateFloat(0f, 1f, infiniteRepeatable(tween(9000, easing = LinearEasing)), label = "ph")
    androidx.compose.foundation.Canvas(modifier.fillMaxSize()) {
        drawRect(Brush.radialGradient(listOf(Color(0xFF1A3A8A), Color(0xFF0F0B16), Color(0xFF0A0A0A)), center = center, radius = size.maxDimension * 0.62f))
        val maxR = size.maxDimension * 0.75f
        for (i in 0 until 14) {
            val f = ((i + phase) / 14f)
            drawCircle(Color(0xFFB48CFF).copy(alpha = (0.16f * (1f - f)).coerceAtLeast(0f)), radius = maxR * f, center = center, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.2f))
        }
    }
}

/** Navy background with a glowing abstract wave curve on the trailing side (inner screens). */
@Composable
internal fun IvWaveBg(modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition(label = "uvWave")
    val s by t.animateFloat(0f, 1f, infiniteRepeatable(tween(7000, easing = LinearEasing), RepeatMode.Reverse), label = "s")
    androidx.compose.foundation.Canvas(modifier.fillMaxSize()) {
        drawRect(IvNavy)
        drawRect(Brush.radialGradient(listOf(Color(0x559B5CFF), Color.Transparent), center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.45f), radius = size.width * 0.45f))
        for (k in 0 until 7) {
            val path = Path()
            val x0 = size.width * (0.55f + k * 0.035f)
            path.moveTo(x0 + 120f * s, 0f)
            path.cubicTo(size.width * (0.95f - k * 0.02f), size.height * (0.25f + 0.05f * s), size.width * (0.55f + k * 0.03f), size.height * 0.65f, size.width * (1.02f - k * 0.015f), size.height)
            drawPath(path, Brush.verticalGradient(listOf(Color(0x00B48CFF), Color(0xFFB48CFF).copy(alpha = 0.55f - k * 0.06f), Color(0x009B5CFF))), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f - k * 0.3f))
        }
    }
}

@Composable
internal fun IvLogo(modifier: Modifier = Modifier, size: Int = 30) {
    Row(modifier, verticalAlignment = Alignment.Top) {
        Text("Ivano", color = Color.White, fontSize = size.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, fontWeight = FontWeight.Light, letterSpacing = (size / 14f).sp)
        Text("✦", color = Color(0xFFC4A3FF), fontSize = (size * 0.38f).sp, modifier = Modifier.padding(start = 2.dp))
    }
}

/** Shell: no sidebar. Home is full-bleed; other screens get the wave bg + title bar (title start, search + logo end). */
@Composable
internal fun IvShell(p: ShellParams) {
    CompositionLocalProvider(LocalCpNavigate provides p.onNavigate) {
        Box(p.modifier.fillMaxSize()) {
            if (onRoute(p.currentRoute, Routes.HOME)) {
                IvRings()
                Column(Modifier.fillMaxSize(), content = p.content)
            } else {
                IvWaveBg()
                Column(Modifier.fillMaxSize().padding(p.contentPadding).padding(horizontal = 40.dp, vertical = 22.dp)) {
                    if (p.topBarVisible) Row(Modifier.fillMaxWidth().padding(bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(p.title, color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        p.topBarActions?.let { Row(horizontalArrangement = Arrangement.spacedBy(10.dp), content = it) }
                        Spacer(Modifier.width(12.dp))
                        IvIconKey(Icons.Outlined.Search) { p.onNavigate(Routes.SEARCH) }
                        Spacer(Modifier.width(18.dp))
                        IvLogo(size = 24)
                    }
                    p.header?.let { Column(content = it) }
                    Column(Modifier.fillMaxSize(), content = p.content)
                }
            }
        }
    }
}

@Composable
internal fun IvIconKey(icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    TvClickableSurface(onClick = onClick, modifier = modifier.size(46.dp), shape = ClickableSurfaceDefaults.shape(CircleShape),
        colors = ClickableSurfaceDefaults.colors(containerColor = Color.Transparent, focusedContainerColor = IvBlue, contentColor = Color.White, focusedContentColor = Color.White),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
        glow = ClickableSurfaceDefaults.glow(focusedGlow = androidx.tv.material3.Glow(IvBlue.copy(alpha = 0.6f), 14.dp))) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { androidx.compose.material3.Icon(icon, null, tint = androidx.tv.material3.LocalContentColor.current, modifier = Modifier.size(26.dp)) }
    }
}

private class IvTile(val icon: androidx.compose.ui.graphics.vector.ImageVector, val ar: String, val en: String, val go: () -> Unit)

/** Home: top info bar, centered Ivano logo, row of six big translucent tiles. */
@Composable
internal fun IvHome(p: DashboardParams) {
    val s = p.uiState
    val first = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { first.requestFocus() } }
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { kotlinx.coroutines.delay(20_000); now = System.currentTimeMillis() } }
    val exp = s.providerHealth.expirationDate
    val days = exp?.takeIf { it > 0 }?.let { ((it - now) / 86_400_000L).coerceAtLeast(0) }
    val tiles = listOf(
        IvTile(Icons.Outlined.LiveTv, "القنوات", "Live TV") { p.onNavigate(Routes.LIVE_TV) },
        IvTile(Icons.Outlined.Movie, "الافلام", "Movies") { p.onNavigate(Routes.MOVIES) },
        IvTile(Icons.Outlined.VideoLibrary, "المسلسلات", "Series") { p.onNavigate(Routes.SERIES) },
        IvTile(Icons.Outlined.Layers, "قائمة التشغيل", "Playlist") { IvRecent.active = false; p.onNavigate(Routes.FAVORITES) },
        IvTile(Icons.Outlined.Settings, "الإعدادات", "Settings") { p.onNavigate(Routes.SETTINGS) },
        IvTile(Icons.Outlined.Refresh, "تحديث قائمة التشغيل", "Refresh playlist") { p.onNavigate(Routes.SETTINGS) },
    )
    Box(Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 30.dp)) {
        Row(Modifier.fillMaxWidth().align(Alignment.TopCenter), verticalAlignment = Alignment.Top) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(s.provider?.name ?: "Ivano", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(cgClock(now), color = Color.White, fontSize = 18.sp)
                    Text(cg2ArabicDate(now), color = Color(0xFFC9B8E8), fontSize = 16.sp)
                    if (days != null) Text(tr("(Days Left: $days)", "(الأيام المتبقية: $days)"), color = if (days <= 7) Color(0xFFFFB74D) else Color(0xFFC4A3FF), fontSize = 16.sp)
                }
            }
            Spacer(Modifier.weight(1f))
            // no weather source in the app yet: nothing fake is shown here
        }
        IvLogo(Modifier.align(Alignment.Center).offset(y = (-50).dp), size = 84)
        Row(Modifier.align(Alignment.BottomCenter).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            tiles.forEachIndexed { i, t -> HomeTile(t, Modifier.weight(1f).then(if (i == 0) Modifier.focusRequester(first) else Modifier)) }
        }
    }
}

@Composable
private fun HomeTile(t: IvTile, modifier: Modifier) {
    val shape = RoundedCornerShape(18.dp)
    TvClickableSurface(onClick = t.go, modifier = modifier.height(190.dp), shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = Color(0x8C0A1438), focusedContainerColor = IvBlue, contentColor = Color.White, focusedContentColor = Color.White),
        border = ClickableSurfaceDefaults.border(border = Border(androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), shape = shape), focusedBorder = Border(androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFC4A3FF)), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
        glow = ClickableSurfaceDefaults.glow(focusedGlow = androidx.tv.material3.Glow(IvBlue.copy(alpha = 0.7f), 24.dp))) {
        Column(Modifier.fillMaxSize().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            androidx.compose.material3.Icon(t.icon, null, tint = androidx.tv.material3.LocalContentColor.current, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text(tr(t.en, t.ar), fontSize = 20.sp, fontWeight = FontWeight.Medium, maxLines = 2, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

/** Blue-bar row: numbered, focused = solid #1E6FD9. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun IvRow(selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, onLongClick: (() -> Unit)? = null, content: @Composable RowScope.() -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    TvClickableSurface(onClick = onClick, onLongClick = onLongClick, modifier = modifier.fillMaxWidth(), shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = if (selected) Color(0x339B5CFF) else Color.Transparent, focusedContainerColor = IvBlue, contentColor = Color.White, focusedContentColor = Color.White),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.0f)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp), content = content)
    }
}

@Composable
private fun IvHint(text: String, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        androidx.compose.material3.Icon(Icons.Outlined.Info, null, tint = Color(0xFFC9B8E8), modifier = Modifier.size(20.dp))
        Text(text, color = Color(0xFFC9B8E8), fontSize = 15.sp)
    }
}

/** Live: stage 1 = categories + giant clock; stage 2 = channel rows + 16:9 preview. BACK returns to categories. */
@Composable
internal fun IvLiveTv(p: LiveTvParams) {
    var channels by rememberSaveable { mutableStateOf(false) }
    androidx.activity.compose.BackHandler(enabled = channels) { channels = false }
    if (!channels) IvCategories(p) { channels = true } else IvChannels(p) { channels = false }
}

@Composable
private fun IvCategories(p: LiveTvParams, open: () -> Unit) {
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { kotlinx.coroutines.delay(10_000); now = System.currentTimeMillis() } }
    val first = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { first.requestFocus() } }
    Box(Modifier.fillMaxSize()) {
        CpLtrRow(Modifier.fillMaxSize().padding(bottom = 40.dp), spacing = 30.dp) { rtl ->
            rtl {
                LazyColumn(Modifier.width(620.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(p.categories.size, key = { p.categories[it].id }) { i ->
                        val cat = p.categories[i]
                        IvRow(cat.id == p.selectedCategoryId, onClick = { p.onCategoryClick(cat); open() }, onLongClick = { p.onCategoryLongClick(cat) },
                            modifier = Modifier.focusRequester(if (i == 0) first else p.categoryRequester(cat.id)).onFocusChanged { if (it.isFocused) p.onCategoryFocused(cat) }) {
                            Text("${i + 1}", fontSize = 20.sp, color = Color(0xFFC9B8E8), modifier = Modifier.width(36.dp))
                            if (p.isCategoryLocked(cat)) androidx.compose.material3.Icon(Icons.Outlined.Lock, null, tint = androidx.tv.material3.LocalContentColor.current, modifier = Modifier.size(20.dp))
                            Text(cat.name, fontSize = 21.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            if (cat.count > 0) Text("${cat.count}", fontSize = 18.sp, color = Color.White.copy(alpha = 0.8f))
                            androidx.compose.material3.Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, tint = androidx.tv.material3.LocalContentColor.current, modifier = Modifier.size(24.dp))
                        }
                    }
                    if (p.categories.isEmpty()) item { CgEmpty(tr("No categories", "لا توجد فئات")) }
                }
            }
            rtl {
                Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(java.text.SimpleDateFormat("HH:mm", java.util.Locale.US).format(java.util.Date(now)), color = Color.White, fontSize = 150.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        Text(cg2ArabicDate(now), color = Color(0xFFC9B8E8), fontSize = 24.sp)
                    }
                }
            }
        }
        IvHint(tr("Long press \"OK\" to move categories", "اضغط مطولاً على \"OK\" لنقل الفئات"), Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun IvChannels(p: LiveTvParams, back: () -> Unit) {
    val catName = p.categories.firstOrNull { it.id == p.selectedCategoryId }?.name ?: p.sourceTitle
    val first = remember { FocusRequester() }
    LaunchedEffect(p.selectedCategoryId) { runCatching { first.requestFocus() } }
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(bottom = 40.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 10.dp)) {
                IvIconKey(Icons.AutoMirrored.Outlined.ArrowBack, onClick = back)
                Spacer(Modifier.width(14.dp))
                Text(catName, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Text("${p.channels.size} " + tr("channels", "قناة"), color = Color(0xFFC9B8E8), fontSize = 16.sp)
            }
            CpLtrRow(Modifier.fillMaxSize(), spacing = 30.dp) { rtl ->
                rtl {
                    LazyColumn(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(p.channels.size, key = { p.channels[it].id }) { i ->
                            val c = p.channels[i]
                            val locked = p.isChannelLocked(c)
                            val moving = c.id == p.movingChannelId
                            IvRow(moving || c.id == p.previewChannel?.id, onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c) },
                                modifier = Modifier.focusRequester(if (i == 0) first else p.channelRequester(c.id)).onFocusChanged { if (it.isFocused) p.onChannelFocused(c) }.cgRight { p.onRequestPreviewFromChannel() }) {
                                Text(if (c.number > 0) "${c.number}" else "${i + 1}", fontSize = 19.sp, color = Color(0xFFC9B8E8), modifier = Modifier.width(44.dp))
                                CpLogoBox(c.name, if (locked) null else c.logoUrl, 54.dp, 36.dp)
                                Text((if (moving) "⇅ " else "") + c.name, fontSize = 20.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                (if (locked) null else c.qualityBadge())?.let {
                                    Text(it, color = Color(0xFF2A1A00), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.background(Color(0xFFE6B84C), RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 2.dp))
                                }
                            }
                        }
                        if (p.channels.isEmpty()) item { CgEmpty(tr("No channels", "لا توجد قنوات")) }
                    }
                }
                rtl {
                    val pc = p.previewChannel
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        CgCard(onClick = { pc?.let(p.onChannelClick) }, container = Color.Black, shape = RoundedCornerShape(14.dp), zoom = 1.0f,
                            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).focusRequester(p.previewFocusRequester).cgRight { p.onRequestChannelsFromPreview() }) {
                            val engine = p.previewPlayerEngine
                            if (engine != null && pc != null) PlayerRenderView(engine, PlayerSurfaceResizeMode.FIT, Modifier.fillMaxSize())
                            else pc?.logoUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(50.dp)) }
                            if (p.isPreviewLoading) Text("…", color = Color.White, fontSize = 26.sp, modifier = Modifier.align(Alignment.Center))
                            p.previewErrorMessage?.let { Text(it, color = Color(0xFFC9B8E8), fontSize = 13.sp, maxLines = 2, modifier = Modifier.align(Alignment.BottomCenter).padding(10.dp)) }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(pc?.name ?: tr("Pick a channel", "اختر قناة"), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                pc?.currentProgram?.let { Text(it.title + "  " + cgClock(it.startTime) + " - " + cgClock(it.endTime), color = Color(0xFFC9B8E8), fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                            }
                            androidx.compose.material3.Icon(if (pc?.isFavorite == true) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, null, tint = if (pc?.isFavorite == true) Color(0xFFFF5A7A) else Color.White, modifier = Modifier.size(32.dp))
                        }
                    }
                }
            }
        }
        IvHint(tr("Long press \"OK\" to move channels", "اضغط مطولاً على \"OK\" لنقل القنوات"), Modifier.align(Alignment.BottomCenter))
    }
}
