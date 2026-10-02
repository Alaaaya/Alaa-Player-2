package com.streamvault.app.ui.themes.nextgentv

import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.streamvault.app.ui.components.PlayerRenderView
import com.streamvault.app.ui.model.guideLookupKey
import com.streamvault.app.ui.themes.bespoke.EpgParams
import com.streamvault.app.ui.themes.bespoke.LibraryParams
import com.streamvault.app.ui.themes.bespoke.tr
import com.streamvault.domain.model.LibraryFilterType
import com.streamvault.domain.model.LibrarySortBy
import com.streamvault.player.PlayerSurfaceResizeMode

@Composable
private fun filterLabel(f: LibraryFilterType) = when (f) {
    LibraryFilterType.ALL -> tr("All", "الكل")
    LibraryFilterType.FAVORITES -> tr("My List", "قائمتي")
    LibraryFilterType.IN_PROGRESS -> tr("In progress", "قيد المشاهدة")
    LibraryFilterType.UNWATCHED -> tr("Unwatched", "لم تشاهد")
    LibraryFilterType.TOP_RATED -> tr("Top rated", "الأعلى تقييماً")
    LibraryFilterType.RECENTLY_UPDATED -> tr("New", "جديد")
}

@Composable
private fun sortLabel(s: LibrarySortBy) = when (s) {
    LibrarySortBy.LIBRARY -> tr("Featured", "مميز")
    LibrarySortBy.TITLE -> tr("A–Z", "أ–ي")
    LibrarySortBy.RELEASE -> tr("Release", "الإصدار")
    LibrarySortBy.UPDATED -> tr("Updated", "التحديث")
    LibrarySortBy.RATING -> tr("Rating", "التقييم")
    LibrarySortBy.WATCH_COUNT -> tr("Popular", "الأكثر مشاهدة")
}

/** Library: poster grid floating in the window with a capsule toolbar ornament; genre pane tilted in from the END side. */
@Composable
internal fun <T> NextGenTvLibrary(
    kindEn: String, kindAr: String,
    p: LibraryParams<T>,
    key: (T) -> Long,
    title: (T) -> String,
    image: (T) -> String?,
    caption: (T) -> String?
) {
    val s = p.uiState
    var sortIndex by remember(s.selectedSort) { mutableStateOf(LibrarySortBy.entries.indexOf(s.selectedSort)) }
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.clip(NG.Pill).background(NG.Raised).border(1.dp, Color.White.copy(alpha = 0.1f), NG.Pill).padding(6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NgSearchField(s.searchQuery, p.onQueryChange, tr("Search $kindEn", "ابحث في $kindAr"), Modifier.width(300.dp).focusRequester(p.initialFocusRequester))
                LazyRow(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(LibraryFilterType.entries) { f -> NgTab(filterLabel(f), f == s.selectedFilter, { p.onFilterChange(f) }) }
                }
                NgButton("⇅ " + sortLabel(s.selectedSort), {
                    sortIndex = (sortIndex + 1) % LibrarySortBy.entries.size
                    p.onSortChange(LibrarySortBy.entries[sortIndex])
                })
            }
            s.selectedCategory?.let { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) { Box(Modifier.size(8.dp).clip(NG.Pill).background(NG.Amber)); Text(it, color = NG.Text, fontSize = 18.sp, fontWeight = FontWeight.Bold) } }
            val items = s.visibleItems
            Box(Modifier.fillMaxSize()) {
                if (items.isEmpty() && !s.isLoadingSelectedCategory) NgEmpty(tr("Nothing here yet", "لا يوجد محتوى هنا"))
                LazyVerticalGrid(
                    GridCells.Adaptive(150.dp), Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp, start = 6.dp, end = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(18.dp), verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(items.size, key = { key(items[it]) }) { index ->
                        val item = items[index]
                        if (index >= items.size - 12) LaunchedEffect(items.size) {
                            if (s.selectedCategory != null && s.canLoadMoreSelectedCategory) p.onLoadMoreSelected()
                            else if (s.selectedCategory == null && s.hasMorePreviewRows) p.onLoadMorePreview()
                        }
                        val locked = p.isItemLocked(item)
                        NgPoster(title(item), if (locked) null else image(item), caption(item), { p.onItemClick(item) }, locked = locked, onLongClick = { p.onItemLongClick(item) })
                    }
                }
            }
        }
        Column(Modifier.width(250.dp).fillMaxHeight().ngTilt(-7f).clip(NG.R).background(NG.Raised).border(1.dp, Color.White.copy(alpha = 0.08f), NG.R).padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(tr(kindEn, kindAr), color = NG.Text, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text("${s.libraryCount} " + tr("titles", "عنوان"), color = NG.Faint, fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
            NgCard(
                onClick = { p.onCategoryClick(s.categories.firstOrNull() ?: return@NgCard) }, shape = NG.RSmall, zoom = 1.03f,
                container = if (s.selectedCategory == null) Color.White.copy(alpha = 0.1f) else Color.Transparent, modifier = Modifier.fillMaxWidth()
            ) { Text(tr("Browse all genres", "كل التصنيفات"), color = if (s.selectedCategory == null) NG.Amber else NG.Sub, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp), contentPadding = PaddingValues(bottom = 40.dp)) {
                items(s.categoryNames, key = { it }) { name ->
                    val cat = s.categoryFor(name)
                    val sel = name == s.selectedCategory
                    val locked = cat?.let(p.isCategoryLocked) == true
                    NgCard(
                        onClick = { cat?.let(p.onCategoryClick) }, onLongClick = { cat?.let(p.onCategoryLongClick) }, shape = NG.RSmall, zoom = 1.03f,
                        container = if (sel) NG.Amber.copy(alpha = 0.18f) else Color.Transparent, focusedContainer = NG.Card, modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text((if (locked) "🔒 " else "") + name, color = if (sel) NG.Amber else NG.Text, fontSize = 14.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            s.categoryCounts[name]?.let { Text("$it", color = NG.Faint, fontSize = 12.sp) }
                        }
                    }
                }
            }
        }
    }
}

/** Guide: floating timeline window with a capsule ornament under it; preview pane angled in from the END. */
@Composable
internal fun NextGenTvEpg(p: EpgParams, modifier: Modifier) {
    Row(modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { NgEpgGrid(p) }
            Row(
                Modifier.align(Alignment.CenterHorizontally).clip(NG.Pill).background(NG.Raised).border(1.dp, Color.White.copy(alpha = 0.12f), NG.Pill).padding(6.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                NgTab("▤ " + p.selectedCategoryName, true, { p.onGuideInteract(); p.onOpenCategoryPicker() })
                NgTab(tr("Now", "الآن"), false, { p.onGuideInteract(); p.onJumpToNow() })
                NgTab("⌕ " + tr("Search", "بحث"), false, { p.onGuideInteract(); p.onOpenSearch() })
                NgTab("⋯ " + tr("Options", "خيارات"), false, { p.onGuideInteract(); p.onOpenOptions() })
                Text("${p.channels.size} " + tr("channels", "قناة"), color = NG.Faint, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 10.dp))
            }
        }
        Column(
            Modifier.width(340.dp).fillMaxHeight().ngTilt(-7f).clip(NG.R).background(NG.Raised).border(1.dp, Color.White.copy(alpha = 0.08f), NG.R).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(NG.RSmall).background(Color.Black)) {
                p.previewPlayerEngine?.let { PlayerRenderView(it, PlayerSurfaceResizeMode.FIT, Modifier.fillMaxSize()) }
                if (p.isPreviewLoading) Text(tr("Loading…", "جار التحميل…"), color = NG.Sub, modifier = Modifier.align(Alignment.Center))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                p.focusedChannel?.let { NgLogo(it.name, it.logoUrl, 34.dp) }
                Text(p.focusedChannel?.name ?: p.selectedCategoryName, color = NG.Amber, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                if (p.isRefreshing) NgBadge(tr("UPDATING", "تحديث"), NG.Blue)
            }
            val prog = p.focusedProgram
            Column(Modifier.fillMaxWidth().clip(NG.RSmall).background(NG.Card).padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(prog?.title ?: tr("TV Guide", "دليل البرامج"), color = NG.Text, fontSize = 20.sp, fontWeight = FontWeight.Black, maxLines = 2, overflow = TextOverflow.Ellipsis)
                prog?.let {
                    Text("${ngClock(it.startTime)} – ${ngClock(it.endTime)}", color = NG.Sub, fontSize = 13.sp)
                    val now = System.currentTimeMillis()
                    if (now in it.startTime..it.endTime && it.endTime > it.startTime) NgProgress((now - it.startTime).toFloat() / (it.endTime - it.startTime))
                    Text(it.description, color = NG.Faint, fontSize = 12.sp, maxLines = 6, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

/** NextGenTv grid: a floating time ruler capsule, channels as squircle logo keys on a depth rail, each row a
 *  floating strip with its own depth plate, programmes as hovering glass slabs, and one glowing now-beam across all rows. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NgEpgGrid(p: EpgParams) {
    val start = p.guideWindowStart
    val end = p.guideWindowEnd.coerceAtLeast(start + 60_000)
    val dpPerMin = when (p.density.name) { "COMPACT" -> 4.5f; "CINEMATIC" -> 7f; else -> 5.5f }
    val rowH = when (p.density.name) { "COMPACT" -> 52.dp; "CINEMATIC" -> 76.dp; else -> 64.dp }
    val keyW = rowH + 24.dp
    val totalW = (((end - start) / 60_000f) * dpPerMin).dp
    val hs = rememberScrollState()
    val now = System.currentTimeMillis()
    val nowX = (((now - start) / 60_000f) * dpPerMin).dp
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            // floating ruler capsule
            Row(Modifier.padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(keyW), contentAlignment = Alignment.Center) { Text(ngClock(now), color = NG.Amber, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                Box(Modifier.weight(1f).clip(NG.Pill).background(NG.Raised.copy(alpha = 0.85f)).border(1.dp, Color.White.copy(alpha = 0.12f), NG.Pill).horizontalScroll(hs)) {
                    Box(Modifier.width(totalW).height(30.dp)) {
                        var tick = start - (start % 1_800_000) + 1_800_000
                        while (tick < end) {
                            val x = (((tick - start) / 60_000f) * dpPerMin).dp
                            Row(Modifier.padding(start = x).align(Alignment.CenterStart), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(Modifier.size(5.dp).clip(CircleShape).background(NG.Violet))
                                Text(ngClock(tick), color = NG.Sub, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            tick += 1_800_000
                        }
                        if (now in start..end) Box(Modifier.padding(start = nowX).align(Alignment.CenterStart).size(10.dp).clip(CircleShape).background(NG.Amber))
                    }
                }
            }
            LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 12.dp)) {
                itemsIndexed(p.channels, key = { _, c -> c.id }) { index, c ->
                    if (index >= p.channels.size - 15) LaunchedEffect(p.channels.size) { p.onRequestMoreChannels() }
                    val programs = c.guideLookupKey()?.let { p.programsByChannel[it] }.orEmpty()
                    val current = programs.firstOrNull { it.startTime <= now && it.endTime > now }
                    val first = index == 0
                    Row(Modifier.fillMaxWidth().height(rowH), verticalAlignment = Alignment.CenterVertically) {
                        var chFocus by remember { mutableStateOf(false) }
                        // channel key: squircle logo floating on a rail, number on its edge, name revealed on focus
                        Box(Modifier.width(keyW).fillMaxHeight(), contentAlignment = Alignment.Center) {
                            Box(
                                Modifier.size(rowH).graphicsLayer { val z = if (chFocus) 1.12f else 1f; scaleX = z; scaleY = z }
                                    .clip(RoundedCornerShape(rowH * 0.3f))
                                    .background(if (chFocus) NG.Amber else NG.Card)
                                    .border(if (chFocus) 2.dp else 1.dp, if (chFocus) Color.White else Color.White.copy(alpha = 0.14f), RoundedCornerShape(rowH * 0.3f))
                                    .onFocusChanged { chFocus = it.isFocused; if (it.isFocused) p.onChannelFocused(c, current, first) }
                                    .combinedClickable(onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c, current) }),
                                contentAlignment = Alignment.Center
                            ) {
                                NgLogo(c.name, c.logoUrl, rowH - 14.dp)
                                if (c.number > 0) Text("${c.number}", color = NG.Bg, fontSize = 10.sp, fontWeight = FontWeight.Black,
                                    modifier = Modifier.align(Alignment.BottomEnd).clip(NG.Pill).background(NG.Amber).padding(horizontal = 5.dp))
                                if (c.id in p.favoriteChannelIds) Text("♥", color = NG.Live, fontSize = 11.sp, modifier = Modifier.align(Alignment.TopEnd).padding(3.dp))
                            }
                        }
                        // floating strip on its own depth plate
                        Box(Modifier.weight(1f).fillMaxHeight()) {
                            Box(Modifier.matchParentSize().offset(y = 5.dp).clip(NG.RSmall).background(Color.Black.copy(alpha = 0.4f)))
                            Box(Modifier.matchParentSize().clip(NG.RSmall).background(NG.Raised.copy(alpha = 0.55f)).border(1.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.16f), Color.Transparent)), NG.RSmall))
                            Box(Modifier.fillMaxSize().horizontalScroll(hs)) {
                                Box(Modifier.width(totalW).fillMaxHeight().padding(vertical = 5.dp)) {
                                    if (programs.isEmpty()) Text(if (chFocus) c.name else tr("No guide data", "لا يوجد دليل"), color = NG.Faint, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterStart).padding(start = 14.dp))
                                    programs.filter { it.endTime > start && it.startTime < end }.forEach { prog ->
                                        val s = maxOf(prog.startTime, start); val e = minOf(prog.endTime, end)
                                        val x = (((s - start) / 60_000f) * dpPerMin).dp + 3.dp
                                        val w = ((((e - s) / 60_000f) * dpPerMin).dp - 6.dp).coerceAtLeast(8.dp)
                                        var f by remember { mutableStateOf(false) }
                                        val live = prog.startTime <= now && prog.endTime > now
                                        Box(
                                            Modifier.padding(start = x).width(w).fillMaxHeight()
                                                .graphicsLayer { val z = if (f) 1.06f else 1f; scaleX = z; scaleY = z; translationY = if (f) -4f * density else 0f }
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (f) Brush.verticalGradient(listOf(NG.Text, Color(0xFFD6E4FF))) else if (live) Brush.verticalGradient(listOf(Color(0xFF2A3A66), Color(0xFF1E2848))) else Brush.verticalGradient(listOf(NG.Card.copy(alpha = 0.9f), NG.Card.copy(alpha = 0.6f))))
                                                .border(1.dp, if (live && !f) NG.Amber.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                                .onFocusChanged { f = it.isFocused; if (it.isFocused) p.onProgramFocused(c, prog, first) }
                                                .combinedClickable(onClick = { p.onProgramClick(c, prog) }, onLongClick = { p.onChannelLongClick(c, prog) })
                                                .padding(horizontal = 12.dp)
                                        ) {
                                            Column(Modifier.align(Alignment.CenterStart)) {
                                                Text(prog.title, color = if (f) NG.Bg else NG.Text, fontSize = 13.sp, fontWeight = if (live) FontWeight.Bold else FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                if (rowH >= 64.dp) Text("${ngClock(prog.startTime)} – ${ngClock(prog.endTime)}", color = if (f) NG.Bg.copy(alpha = 0.6f) else NG.Faint, fontSize = 11.sp, maxLines = 1)
                                            }
                                            if (live && !f) Box(Modifier.align(Alignment.TopStart).fillMaxWidth(prog.progressFraction(now)).height(2.dp).background(Brush.horizontalGradient(listOf(NG.AmberDeep, NG.Amber))))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        // one glowing now-beam across every row
        if (now in start..end) Box(Modifier.fillMaxHeight().padding(top = 40.dp).horizontalScroll(hs, enabled = false).padding(start = keyW)) {
            Box(Modifier.width(totalW).fillMaxHeight()) {
                Box(Modifier.padding(start = nowX - 3.dp).width(6.dp).fillMaxHeight().background(Brush.horizontalGradient(listOf(Color.Transparent, NG.Amber.copy(alpha = 0.35f), Color.Transparent))))
                Box(Modifier.padding(start = nowX).width(1.5.dp).fillMaxHeight().background(NG.Amber))
            }
        }
    }
}

private fun com.streamvault.domain.model.Program.progressFraction(now: Long): Float =
    if (endTime > startTime) ((now - startTime).toFloat() / (endTime - startTime)).coerceIn(0f, 1f) else 0f
