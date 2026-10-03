package com.streamvault.app.ui.themes.sportstv

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

/** Library: "league table" layout. Toolbar plate across the top (search, filter tabs, sort), poster grid in the
 *  middle and a ranked GENRES standings table on the END side (rank block, name, count). */
@Composable
internal fun <T> SportsTvLibrary(
    kindEn: String, kindAr: String,
    p: LibraryParams<T>,
    key: (T) -> Long,
    title: (T) -> String,
    image: (T) -> String?,
    caption: (T) -> String?
) {
    val s = p.uiState
    var sortIndex by remember(s.selectedSort) { mutableStateOf(LibrarySortBy.entries.indexOf(s.selectedSort)) }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth().clip(ST.R).background(ST.Card).padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.clip(ST.Plate).background(ST.Amber).padding(horizontal = 14.dp, vertical = 8.dp)) {
                Text(tr(kindEn, kindAr).uppercase() + "  ${s.libraryCount}", color = ST.Bg, fontSize = 15.sp, fontWeight = FontWeight.Black)
            }
            StSearchField(s.searchQuery, p.onQueryChange, tr("Search $kindEn", "ابحث في $kindAr"), Modifier.width(260.dp).focusRequester(p.initialFocusRequester))
            LazyRow(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                items(LibraryFilterType.entries) { f -> StTab(filterLabel(f), f == s.selectedFilter, { p.onFilterChange(f) }) }
            }
            StButton("⇅ " + sortLabel(s.selectedSort), {
                sortIndex = (sortIndex + 1) % LibrarySortBy.entries.size
                p.onSortChange(LibrarySortBy.entries[sortIndex])
            })
        }
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.weight(1f).fillMaxHeight()) {
                s.selectedCategory?.let { StRowTitle(it) }
                val items = s.visibleItems
                Box(Modifier.fillMaxSize()) {
                    if (items.isEmpty() && !s.isLoadingSelectedCategory) StEmpty(tr("Nothing here yet", "لا يوجد محتوى هنا"))
                    LazyVerticalGrid(
                        GridCells.Adaptive(140.dp), Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 6.dp, bottom = 48.dp, start = 4.dp, end = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items.size, key = { key(items[it]) }) { index ->
                            val item = items[index]
                            if (index >= items.size - 12) LaunchedEffect(items.size) {
                                if (s.selectedCategory != null && s.canLoadMoreSelectedCategory) p.onLoadMoreSelected()
                                else if (s.selectedCategory == null && s.hasMorePreviewRows) p.onLoadMorePreview()
                            }
                            val locked = p.isItemLocked(item)
                            StPoster(title(item), if (locked) null else image(item), caption(item), { p.onItemClick(item) }, locked = locked, onLongClick = { p.onItemLongClick(item) })
                        }
                    }
                }
            }
            Column(Modifier.width(260.dp).fillMaxHeight().clip(ST.R).background(ST.Card)) {
                Row(Modifier.fillMaxWidth().background(ST.Line).padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text("#", color = ST.Amber, fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(30.dp))
                    Text(tr("GENRES", "التصنيفات"), color = ST.Text, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, modifier = Modifier.weight(1f))
                    Text(tr("TITLES", "عدد"), color = ST.Sub, fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
                StCard(
                    onClick = { p.onShowAll() }, shape = ST.Pill, zoom = 1.02f,
                    container = if (s.selectedCategory == null) ST.Raised else Color.Transparent, focusedContainer = ST.Line, modifier = Modifier.fillMaxWidth().padding(6.dp)
                ) { Text(tr("ALL GENRES", "كل التصنيفات"), color = if (s.selectedCategory == null) ST.Amber else ST.Sub, fontSize = 13.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp), contentPadding = PaddingValues(start = 6.dp, end = 6.dp, bottom = 40.dp)) {
                    itemsIndexed(s.categoryNames, key = { _, n -> n }) { i, name ->
                        val cat = s.categoryFor(name)
                        val sel = name == s.selectedCategory
                        val locked = cat?.let(p.isCategoryLocked) == true
                        StCard(
                            onClick = { cat?.let(p.onCategoryClick) }, onLongClick = { cat?.let(p.onCategoryLongClick) }, shape = ST.Pill, zoom = 1.02f,
                            container = if (sel) ST.Raised else Color.Transparent, focusedContainer = ST.Line, modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(Modifier.height(38.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.width(30.dp).fillMaxHeight().background(if (sel) ST.Amber else ST.Bg), contentAlignment = Alignment.Center) {
                                    Text("${i + 1}", color = if (sel) ST.Bg else ST.Faint, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                }
                                Text((if (locked) "🔒 " else "") + name, color = if (sel) ST.Amber else ST.Text, fontSize = 13.sp, fontWeight = if (sel) FontWeight.Black else FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f).padding(horizontal = 8.dp))
                                s.categoryCounts[name]?.let { Text("$it", color = ST.Sub, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Guide: broadcast lower-third across the top (lime title plate, time, clock bar, synopsis) with a small live
 *  monitor on its END edge; ticker action strip; then the fixtures grid with number blocks. */
@Composable
internal fun SportsTvEpg(p: EpgParams, modifier: Modifier) {
    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth().height(150.dp), horizontalArrangement = Arrangement.spacedBy(0.dp)) {
            Column(Modifier.weight(1f).fillMaxHeight().clip(ST.R).background(ST.Card)) {
                val prog = p.focusedProgram
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.background(ST.Amber).padding(horizontal = 14.dp, vertical = 8.dp)) {
                        Text((p.focusedChannel?.name ?: p.selectedCategoryName).uppercase(), color = ST.Bg, fontSize = 14.sp, fontWeight = FontWeight.Black, maxLines = 1)
                    }
                    prog?.let { Box(Modifier.background(ST.Line).padding(horizontal = 12.dp, vertical = 8.dp)) { Text("${stClock(it.startTime)} – ${stClock(it.endTime)}", color = ST.Text, fontSize = 14.sp, fontWeight = FontWeight.Black) } }
                    if (p.isRefreshing) Box(Modifier.padding(start = 8.dp)) { StBadge(tr("UPDATING", "تحديث"), ST.Blue) }
                }
                Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text((prog?.title ?: tr("TV Guide", "دليل البرامج")).uppercase(), color = ST.Text, fontSize = 24.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    prog?.let {
                        val now = System.currentTimeMillis()
                        if (now in it.startTime..it.endTime && it.endTime > it.startTime) StProgress((now - it.startTime).toFloat() / (it.endTime - it.startTime), Modifier.widthIn(max = 420.dp), height = 3.dp)
                        Text(it.description, color = ST.Sub, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            Box(Modifier.aspectRatio(16f / 9f).fillMaxHeight().background(Color.Black)) {
                p.previewPlayerEngine?.let { PlayerRenderView(it, PlayerSurfaceResizeMode.FIT, Modifier.fillMaxSize()) }
                if (p.isPreviewLoading) Text(tr("Loading…", "جار التحميل…"), color = ST.Sub, modifier = Modifier.align(Alignment.Center))
                Box(Modifier.align(Alignment.TopEnd).background(ST.Live).padding(horizontal = 6.dp, vertical = 2.dp)) { Text("● LIVE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black) }
            }
        }
        Row(Modifier.fillMaxWidth().background(ST.Raised), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.background(ST.Live).padding(horizontal = 10.dp, vertical = 8.dp)) { Text(tr("GUIDE", "الدليل"), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black) }
            StTab("▤ " + p.selectedCategoryName, true, { p.onGuideInteract(); p.onOpenCategoryPicker() })
            StTab(tr("Now", "الآن"), false, { p.onGuideInteract(); p.onJumpToNow() })
            StTab("⌕ " + tr("Search", "بحث"), false, { p.onGuideInteract(); p.onOpenSearch() })
            StTab("⋯ " + tr("Options", "خيارات"), false, { p.onGuideInteract(); p.onOpenOptions() })
            Spacer(Modifier.weight(1f))
            Text("${p.channels.size} " + tr("channels", "قناة").uppercase(), color = ST.Amber, fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(end = 12.dp))
        }
        StEpgGrid(p)
    }
}

/** Fixtures grid: number+logo plates on the channel column, slanted programme plates, red now line. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StEpgGrid(p: EpgParams) {
    val start = p.guideWindowStart
    val end = p.guideWindowEnd.coerceAtLeast(start + 60_000)
    val dpPerMin = when (p.density.name) { "COMPACT" -> 4.5f; "CINEMATIC" -> 7f; else -> 5.5f }
    val rowH = when (p.density.name) { "COMPACT" -> 48.dp; "CINEMATIC" -> 68.dp; else -> 58.dp }
    val totalW = (((end - start) / 60_000f) * dpPerMin).dp
    val hs = rememberScrollState()
    val now = System.currentTimeMillis()
    val nowX = (((now - start) / 60_000f) * dpPerMin).dp
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.padding(bottom = 6.dp)) {
            Spacer(Modifier.width(200.dp))
            Box(Modifier.weight(1f).horizontalScroll(hs)) {
                Box(Modifier.width(totalW).height(22.dp)) {
                    var tick = start - (start % 1_800_000) + 1_800_000
                    while (tick < end) {
                        val x = (((tick - start) / 60_000f) * dpPerMin).dp
                        Text(stClock(tick), color = ST.Sub, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = x))
                        tick += 1_800_000
                    }
                    if (now in start..end) Box(Modifier.padding(start = nowX).width(3.dp).fillMaxHeight().background(ST.Live))
                }
            }
        }
        LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            itemsIndexed(p.channels, key = { _, c -> c.id }) { index, c ->
                if (index >= p.channels.size - 15) LaunchedEffect(p.channels.size) { p.onRequestMoreChannels() }
                val programs = c.guideLookupKey()?.let { p.programsByChannel[it] }.orEmpty()
                val current = programs.firstOrNull { it.startTime <= now && it.endTime > now }
                val first = index == 0
                Row(Modifier.fillMaxWidth().height(rowH), verticalAlignment = Alignment.CenterVertically) {
                    var chFocus by remember { mutableStateOf(false) }
                    Row(
                        Modifier.width(192.dp).fillMaxHeight().clip(ST.RSmall)
                            .background(if (chFocus) ST.Amber else ST.Raised)
                            .onFocusChanged { chFocus = it.isFocused; if (it.isFocused) p.onChannelFocused(c, current, first) }
                            .combinedClickable(onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c, current) })
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(if (c.number > 0) "${c.number}" else "–", color = if (chFocus) ST.Bg else ST.Amber, fontSize = 13.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(30.dp))
                        StLogo(c.name, c.logoUrl, rowH - 22.dp)
                        Column(Modifier.weight(1f)) {
                            Text(c.name, color = if (chFocus) ST.Bg else ST.Text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (c.catchUpSupported) Text("⟲ ARCHIVE", color = if (chFocus) ST.Bg.copy(alpha = 0.6f) else ST.Blue, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                        if (c.id in p.favoriteChannelIds) Text("★", color = if (chFocus) ST.Bg else ST.Amber, fontSize = 12.sp)
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.weight(1f).fillMaxHeight().horizontalScroll(hs)) {
                        Box(Modifier.width(totalW).fillMaxHeight()) {
                            if (programs.isEmpty()) Box(Modifier.fillMaxSize().clip(ST.RSmall).background(ST.Raised.copy(alpha = 0.5f))) {
                                Text(tr("No guide data", "لا يوجد دليل"), color = ST.Faint, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterStart).padding(start = 14.dp))
                            }
                            programs.filter { it.endTime > start && it.startTime < end }.forEach { prog ->
                                val s = maxOf(prog.startTime, start); val e = minOf(prog.endTime, end)
                                val x = (((s - start) / 60_000f) * dpPerMin).dp
                                val w = ((((e - s) / 60_000f) * dpPerMin).dp - 4.dp).coerceAtLeast(8.dp)
                                var f by remember { mutableStateOf(false) }
                                val live = prog.startTime <= now && prog.endTime > now
                                Box(
                                    Modifier.padding(start = x).width(w).fillMaxHeight().clip(ST.RSmall)
                                        .background(if (f) ST.Amber else if (live) ST.Line else ST.Raised)
                                        .border(if (live && !f) 1.dp else 0.dp, ST.Amber.copy(alpha = 0.5f), ST.RSmall)
                                        .onFocusChanged { f = it.isFocused; if (it.isFocused) p.onProgramFocused(c, prog, first) }
                                        .combinedClickable(onClick = { p.onProgramClick(c, prog) }, onLongClick = { p.onChannelLongClick(c, prog) })
                                        .padding(horizontal = 10.dp)
                                ) {
                                    Column(Modifier.align(Alignment.CenterStart)) {
                                        Text(prog.title, color = if (f) ST.Bg else ST.Text, fontSize = 13.sp, fontWeight = if (live) FontWeight.Bold else FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        if (rowH >= 58.dp) Text("${stClock(prog.startTime)} – ${stClock(prog.endTime)}", color = if (f) ST.Bg.copy(alpha = 0.6f) else ST.Faint, fontSize = 11.sp, maxLines = 1)
                                    }
                                    if (live && !f) Box(Modifier.align(Alignment.BottomStart).fillMaxWidth(prog.progressFraction(now)).height(3.dp).background(ST.Amber))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun com.streamvault.domain.model.Program.progressFraction(now: Long): Float =
    if (endTime > startTime) ((now - startTime).toFloat() / (endTime - startTime)).coerceIn(0f, 1f) else 0f
