package com.streamvault.app.ui.themes.cardstack

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

/** Library: a poster grid of stacked cards on the start, and on the END edge a tall "index box" of genre tab cards
 *  (like a recipe card box) with filter chips on top of it. Sort is a deck "shuffle" button. */
@Composable
internal fun <T> CardStackLibrary(
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
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(tr(kindEn, kindAr), color = CS.Text, fontSize = 30.sp, fontWeight = FontWeight.Black)
                CsBadge("${s.libraryCount}", CS.Amber, filled = true)
                Text(s.selectedCategory ?: tr("All genres", "كل التصنيفات"), color = CS.Sub, fontSize = 16.sp, modifier = Modifier.weight(1f), maxLines = 1)
                CsSearchField(s.searchQuery, p.onQueryChange, tr("Search", "بحث"), Modifier.width(260.dp).focusRequester(p.initialFocusRequester))
                CsButton("🂠 " + sortLabel(s.selectedSort), {
                    sortIndex = (sortIndex + 1) % LibrarySortBy.entries.size
                    p.onSortChange(LibrarySortBy.entries[sortIndex])
                })
            }
            val items = s.visibleItems
            Box(Modifier.fillMaxSize()) {
                if (items.isEmpty() && !s.isLoadingSelectedCategory) CsEmpty(tr("Empty deck", "لا يوجد محتوى"))
                LazyVerticalGrid(
                    GridCells.Adaptive(150.dp), Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 56.dp, start = 8.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(22.dp), verticalArrangement = Arrangement.spacedBy(28.dp)
                ) {
                    items(items.size, key = { key(items[it]) }) { index ->
                        val item = items[index]
                        if (index >= items.size - 12) LaunchedEffect(items.size) {
                            if (s.selectedCategory != null && s.canLoadMoreSelectedCategory) p.onLoadMoreSelected()
                            else if (s.selectedCategory == null && s.hasMorePreviewRows) p.onLoadMorePreview()
                        }
                        val locked = p.isItemLocked(item)
                        CsPoster(title(item), if (locked) null else image(item), caption(item), { p.onItemClick(item) }, locked = locked, onLongClick = { p.onItemLongClick(item) })
                    }
                }
            }
        }
        Column(Modifier.width(250.dp).fillMaxHeight().clip(CS.R).background(CS.Raised.copy(alpha = 0.7f)).padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(tr("Genre index", "فهرس التصنيفات"), color = CS.Amber, fontSize = 13.sp, fontWeight = FontWeight.Black)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(LibraryFilterType.entries) { f -> CsTab(filterLabel(f), f == s.selectedFilter, { p.onFilterChange(f) }) }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 6.dp, horizontal = 2.dp)) {
                item {
                    CsCard({ s.categories.firstOrNull()?.let(p.onCategoryClick) }, Modifier.fillMaxWidth().height(44.dp), shape = CS.RSmall,
                        container = if (s.selectedCategory == null) CS.Amber else CS.Card) {
                        Text(tr("All", "الكل"), color = if (s.selectedCategory == null) CS.Bg else CS.Text, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterStart).padding(horizontal = 14.dp))
                    }
                }
                items(s.categoryNames, key = { it }) { name ->
                    val cat = s.categoryFor(name)
                    val sel = name == s.selectedCategory
                    val locked = cat?.let(p.isCategoryLocked) == true
                    CsCard({ cat?.let(p.onCategoryClick) }, Modifier.fillMaxWidth().height(44.dp), shape = CS.RSmall, onLongClick = { cat?.let(p.onCategoryLongClick) },
                        container = if (sel) CS.Amber else CS.Card, focusedContainer = if (sel) CS.Amber else Color(0xFF3A2A40)) {
                        Row(Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text((if (locked) "🔒 " else "") + name, color = if (sel) CS.Bg else CS.Text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            s.categoryCounts[name]?.let { Text("$it", color = if (sel) CS.Bg else CS.Faint, fontSize = 12.sp) }
                        }
                    }
                }
            }
        }
    }
}

/** Guide: timeline grid fills the start; on the END a tall column holds the preview card, the focused-programme card
 *  and the guide actions as stacked deck cards. */
@Composable
internal fun CardStackEpg(p: EpgParams, modifier: Modifier) {
    Row(modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(p.selectedCategoryName, color = CS.Text, fontSize = 22.sp, fontWeight = FontWeight.Black, maxLines = 1, modifier = Modifier.weight(1f))
                if (p.isRefreshing) CsBadge(tr("UPDATING", "تحديث"), CS.Blue)
                Text("  ${p.channels.size} " + tr("channels", "قناة"), color = CS.Faint, fontSize = 13.sp)
            }
            CsEpgGrid(p)
        }
        Column(Modifier.width(340.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(CS.R).background(Color.Black)) {
                p.previewPlayerEngine?.let { PlayerRenderView(it, PlayerSurfaceResizeMode.FIT, Modifier.fillMaxSize()) }
                if (p.isPreviewLoading) Text(tr("Loading…", "جار التحميل…"), color = CS.Sub, modifier = Modifier.align(Alignment.Center))
            }
            Column(Modifier.fillMaxWidth().weight(1f).clip(CS.R).background(CS.Raised).padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(p.focusedChannel?.name ?: p.selectedCategoryName, color = CS.Amber, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                val prog = p.focusedProgram
                Text(prog?.title ?: tr("TV Guide", "دليل البرامج"), color = CS.Text, fontSize = 20.sp, fontWeight = FontWeight.Black, maxLines = 2, overflow = TextOverflow.Ellipsis)
                prog?.let {
                    Text("${csClock(it.startTime)} – ${csClock(it.endTime)}", color = CS.Sub, fontSize = 13.sp)
                    val now = System.currentTimeMillis()
                    if (now in it.startTime..it.endTime && it.endTime > it.startTime) CsProgress((now - it.startTime).toFloat() / (it.endTime - it.startTime))
                    Text(it.description, color = CS.Faint, fontSize = 12.sp, maxLines = 5, overflow = TextOverflow.Ellipsis)
                }
            }
            listOf(
                "▤ " + tr("Groups", "المجموعات") to { p.onGuideInteract(); p.onOpenCategoryPicker() },
                "◉ " + tr("Jump to now", "الآن") to { p.onGuideInteract(); p.onJumpToNow() },
                "⌕ " + tr("Search", "بحث") to { p.onGuideInteract(); p.onOpenSearch() },
                "⋯ " + tr("Options", "خيارات") to { p.onGuideInteract(); p.onOpenOptions() }
            ).forEach { (l, a) ->
                CsCard(a, Modifier.fillMaxWidth().height(44.dp), shape = CS.RSmall, container = CS.Raised) {
                    Text(l, color = CS.Text, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterStart).padding(horizontal = 14.dp))
                }
            }
        }
    }
}

/** CardStack's own grid: rounded logo cards on the channel column, pill-shaped programmes, amber "now" line. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CsEpgGrid(p: EpgParams) {
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
                        Text(csClock(tick), color = CS.Sub, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = x))
                        tick += 1_800_000
                    }
                    if (now in start..end) Box(Modifier.padding(start = nowX).width(2.dp).fillMaxHeight().background(CS.Amber))
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
                        Modifier.width(192.dp).fillMaxHeight().clip(CS.RSmall)
                            .background(if (chFocus) CS.Text else CS.Raised)
                            .onFocusChanged { chFocus = it.isFocused; if (it.isFocused) p.onChannelFocused(c, current, first) }
                            .combinedClickable(onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c, current) })
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CsLogo(c.name, c.logoUrl, rowH - 18.dp)
                        Column(Modifier.weight(1f)) {
                            Text(c.name, color = if (chFocus) CS.Bg else CS.Text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (c.number > 0) Text("${c.number}", color = if (chFocus) CS.Bg.copy(alpha = 0.6f) else CS.Faint, fontSize = 11.sp)
                        }
                        if (c.id in p.favoriteChannelIds) Text("♥", color = CS.Amber, fontSize = 12.sp)
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.weight(1f).fillMaxHeight().horizontalScroll(hs)) {
                        Box(Modifier.width(totalW).fillMaxHeight()) {
                            if (programs.isEmpty()) Box(Modifier.fillMaxSize().clip(CS.RSmall).background(CS.Raised.copy(alpha = 0.5f))) {
                                Text(tr("No guide data", "لا يوجد دليل"), color = CS.Faint, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterStart).padding(start = 14.dp))
                            }
                            programs.filter { it.endTime > start && it.startTime < end }.forEach { prog ->
                                val s = maxOf(prog.startTime, start); val e = minOf(prog.endTime, end)
                                val x = (((s - start) / 60_000f) * dpPerMin).dp
                                val w = ((((e - s) / 60_000f) * dpPerMin).dp - 4.dp).coerceAtLeast(8.dp)
                                var f by remember { mutableStateOf(false) }
                                val live = prog.startTime <= now && prog.endTime > now
                                Box(
                                    Modifier.padding(start = x).width(w).fillMaxHeight().clip(CS.RSmall)
                                        .background(if (f) CS.Text else if (live) CS.Card else CS.Raised)
                                        .border(if (live && !f) 1.dp else 0.dp, CS.Amber.copy(alpha = 0.5f), CS.RSmall)
                                        .onFocusChanged { f = it.isFocused; if (it.isFocused) p.onProgramFocused(c, prog, first) }
                                        .combinedClickable(onClick = { p.onProgramClick(c, prog) }, onLongClick = { p.onChannelLongClick(c, prog) })
                                        .padding(horizontal = 10.dp)
                                ) {
                                    Column(Modifier.align(Alignment.CenterStart)) {
                                        Text(prog.title, color = if (f) CS.Bg else CS.Text, fontSize = 13.sp, fontWeight = if (live) FontWeight.Bold else FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        if (rowH >= 58.dp) Text("${csClock(prog.startTime)} – ${csClock(prog.endTime)}", color = if (f) CS.Bg.copy(alpha = 0.6f) else CS.Faint, fontSize = 11.sp, maxLines = 1)
                                    }
                                    if (live && !f) Box(Modifier.align(Alignment.BottomStart).fillMaxWidth(prog.progressFraction(now)).height(3.dp).background(CS.Amber))
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
