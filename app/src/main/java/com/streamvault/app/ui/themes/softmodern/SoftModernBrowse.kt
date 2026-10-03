package com.streamvault.app.ui.themes.softmodern

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

/** Library: sage genre panel on the start side (pill rows), filter chips + search above a grid of white label-pocket pebbles. */
@Composable
internal fun <T> SoftModernLibrary(
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
        Column(Modifier.width(250.dp).fillMaxHeight().clip(SM.R).background(SM.Sage).padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(tr(kindEn, kindAr), color = SM.Text, fontSize = 28.sp, fontWeight = FontWeight.Black)
            Text("${s.libraryCount} " + tr("titles", "عنوان"), color = SM.Faint, fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
            SmCard(
                onClick = { p.onShowAll() }, zoom = 1.03f,
                shape = SM.Pill, container = if (s.selectedCategory == null) SM.Card else Color.Transparent, modifier = Modifier.fillMaxWidth()
            ) { Text(tr("Browse all genres", "كل التصنيفات"), color = if (s.selectedCategory == null) SM.Amber else SM.Sub, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp), contentPadding = PaddingValues(bottom = 40.dp)) {
                items(s.categoryNames, key = { it }) { name ->
                    val cat = s.categoryFor(name)
                    val sel = name == s.selectedCategory
                    val locked = cat?.let(p.isCategoryLocked) == true
                    SmCard(
                        onClick = { cat?.let(p.onCategoryClick) }, onLongClick = { cat?.let(p.onCategoryLongClick) }, zoom = 1.03f,
                        container = if (sel) SM.Card else Color.Transparent, focusedContainer = SM.Card, modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text((if (locked) "🔒 " else "") + name, color = if (sel) SM.Amber else SM.Text, fontSize = 14.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            s.categoryCounts[name]?.let { Text("$it", color = SM.Faint, fontSize = 12.sp) }
                        }
                    }
                }
            }
        }
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SmSearchField(s.searchQuery, p.onQueryChange, tr("Search $kindEn", "ابحث في $kindAr"), Modifier.width(300.dp).focusRequester(p.initialFocusRequester))
                LazyRow(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(LibraryFilterType.entries) { f -> SmTab(filterLabel(f), f == s.selectedFilter, { p.onFilterChange(f) }) }
                }
                SmButton("⇅ " + sortLabel(s.selectedSort), {
                    sortIndex = (sortIndex + 1) % LibrarySortBy.entries.size
                    p.onSortChange(LibrarySortBy.entries[sortIndex])
                })
            }
            s.selectedCategory?.let { Text(it, color = SM.Sub, fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
            val items = s.visibleItems
            Box(Modifier.fillMaxSize()) {
                if (items.isEmpty() && !s.isLoadingSelectedCategory) SmEmpty(tr("Nothing here yet", "لا يوجد محتوى هنا"))
                LazyVerticalGrid(
                    GridCells.Adaptive(160.dp), Modifier.fillMaxSize(),
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
                        SmPoster(title(item), if (locked) null else image(item), caption(item), { p.onItemClick(item) }, locked = locked, onLongClick = { p.onItemLongClick(item) })
                    }
                }
            }
        }
    }
}

/** Guide (Soft Modern "day planner"): timeline on the start side, a tall white side card on the END with preview video,
 *  the focused programme and stacked pill actions. */
@Composable
internal fun SoftModernEpg(p: EpgParams, modifier: Modifier) {
    Row(modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.clip(SM.Pill).background(SM.Amber).padding(horizontal = 14.dp, vertical = 6.dp)) {
                    Text(p.selectedCategoryName, color = SM.Card, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                }
                Text("${p.channels.size} " + tr("channels", "قناة"), color = SM.Faint, fontSize = 13.sp)
                Spacer(Modifier.weight(1f))
                if (p.isRefreshing) SmBadge(tr("Updating", "تحديث"), SM.Blue)
            }
            SmEpgGrid(p)
        }
        Column(Modifier.width(340.dp).fillMaxHeight().clip(SM.R).background(SM.Card).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(SM.RSmall).background(SM.Sage)) {
                p.previewPlayerEngine?.let { PlayerRenderView(it, PlayerSurfaceResizeMode.FIT, Modifier.fillMaxSize()) }
                if (p.isPreviewLoading) Text(tr("Loading…", "جار التحميل…"), color = SM.Sub, modifier = Modifier.align(Alignment.Center))
            }
            Text(p.focusedChannel?.name ?: tr("TV Guide", "دليل البرامج"), color = SM.AmberDeep, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            val prog = p.focusedProgram
            Text(prog?.title ?: tr("Pick a programme", "اختر برنامجاً"), color = SM.Text, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            prog?.let {
                Text("${smClock(it.startTime)} – ${smClock(it.endTime)}", color = SM.Sub, fontSize = 13.sp)
                val now = System.currentTimeMillis()
                if (now in it.startTime..it.endTime && it.endTime > it.startTime) SmProgress((now - it.startTime).toFloat() / (it.endTime - it.startTime), Modifier.fillMaxWidth())
                Text(it.description, color = SM.Faint, fontSize = 12.sp, maxLines = 4, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.weight(1f))
            SmButton(tr("Categories", "الفئات"), { p.onGuideInteract(); p.onOpenCategoryPicker() }, Modifier.fillMaxWidth(), icon = "▤")
            SmButton(tr("Jump to now", "الآن"), { p.onGuideInteract(); p.onJumpToNow() }, Modifier.fillMaxWidth(), primary = true, icon = "◉")
            SmButton(tr("Search", "بحث"), { p.onGuideInteract(); p.onOpenSearch() }, Modifier.fillMaxWidth(), icon = "⌕")
            SmButton(tr("Options", "خيارات"), { p.onGuideInteract(); p.onOpenOptions() }, Modifier.fillMaxWidth(), icon = "⋯")
        }
    }
}

/** Soft Modern timeline: each channel is one white pill strip (round logo + name), programmes are pill chips; live = sage with soft progress wash. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SmEpgGrid(p: EpgParams) {
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
            Spacer(Modifier.width(196.dp))
            Box(Modifier.weight(1f).horizontalScroll(hs)) {
                Box(Modifier.width(totalW).height(22.dp)) {
                    var tick = start - (start % 1_800_000) + 1_800_000
                    while (tick < end) {
                        val x = (((tick - start) / 60_000f) * dpPerMin).dp
                        Text(smClock(tick), color = SM.Sub, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = x))
                        tick += 1_800_000
                    }
                    if (now in start..end) Box(Modifier.padding(start = nowX).size(10.dp).clip(SM.Pill).background(SM.Amber).align(Alignment.BottomStart))
                }
            }
        }
        LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            itemsIndexed(p.channels, key = { _, c -> c.id }) { index, c ->
                if (index >= p.channels.size - 15) LaunchedEffect(p.channels.size) { p.onRequestMoreChannels() }
                val programs = c.guideLookupKey()?.let { p.programsByChannel[it] }.orEmpty()
                val current = programs.firstOrNull { it.startTime <= now && it.endTime > now }
                val first = index == 0
                Row(Modifier.fillMaxWidth().height(rowH).clip(SM.Pill).background(SM.Card).padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    var chFocus by remember { mutableStateOf(false) }
                    Row(
                        Modifier.width(188.dp).fillMaxHeight().clip(SM.Pill)
                            .background(if (chFocus) SM.Amber else Color.Transparent)
                            .onFocusChanged { chFocus = it.isFocused; if (it.isFocused) p.onChannelFocused(c, current, first) }
                            .combinedClickable(onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c, current) })
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(Modifier.size(rowH - 14.dp).clip(SM.Pill).background(SM.Raised), contentAlignment = Alignment.Center) { SmLogo(c.name, c.logoUrl, rowH - 24.dp) }
                        Column(Modifier.weight(1f)) {
                            Text(c.name, color = if (chFocus) SM.Card else SM.Text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (c.number > 0) Text("${c.number}", color = if (chFocus) SM.Card.copy(alpha = 0.8f) else SM.Faint, fontSize = 11.sp)
                        }
                        if (c.id in p.favoriteChannelIds) Text("♥", color = SM.Amber, fontSize = 12.sp)
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.weight(1f).fillMaxHeight().horizontalScroll(hs)) {
                        Box(Modifier.width(totalW).fillMaxHeight()) {
                            if (programs.isEmpty()) Box(Modifier.fillMaxSize().clip(SM.Pill).background(SM.Raised)) {
                                Text(tr("No guide data", "لا يوجد دليل"), color = SM.Faint, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterStart).padding(start = 14.dp))
                            }
                            programs.filter { it.endTime > start && it.startTime < end }.forEach { prog ->
                                val s = maxOf(prog.startTime, start); val e = minOf(prog.endTime, end)
                                val x = (((s - start) / 60_000f) * dpPerMin).dp
                                val w = ((((e - s) / 60_000f) * dpPerMin).dp - 4.dp).coerceAtLeast(8.dp)
                                var f by remember { mutableStateOf(false) }
                                val live = prog.startTime <= now && prog.endTime > now
                                Box(
                                    Modifier.padding(start = x, top = 2.dp, bottom = 2.dp).width(w).fillMaxHeight().clip(SM.Pill)
                                        .background(if (f) SM.Amber else if (live) SM.Sage else SM.Raised)
                                        .onFocusChanged { f = it.isFocused; if (it.isFocused) p.onProgramFocused(c, prog, first) }
                                        .combinedClickable(onClick = { p.onProgramClick(c, prog) }, onLongClick = { p.onChannelLongClick(c, prog) })
                                        .padding(horizontal = 10.dp)
                                ) {
                                    Column(Modifier.align(Alignment.CenterStart)) {
                                        Text(prog.title, color = if (f) SM.Card else SM.Text, fontSize = 13.sp, fontWeight = if (live) FontWeight.Bold else FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        if (rowH >= 58.dp) Text("${smClock(prog.startTime)} – ${smClock(prog.endTime)}", color = if (f) SM.Card.copy(alpha = 0.8f) else SM.Faint, fontSize = 11.sp, maxLines = 1)
                                    }
                                    if (live && !f) Box(Modifier.align(Alignment.CenterStart).padding(start = 0.dp).fillMaxWidth(prog.progressFraction(now)).fillMaxHeight().background(SM.Amber.copy(alpha = 0.12f)))
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
