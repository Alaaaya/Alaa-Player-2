package com.streamvault.app.ui.themes.darkglass

import androidx.compose.ui.draw.blur
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

/** Library: floating glass genre pane START; on the right a glass "lens" strip that previews the focused title
 *  (blurred art behind glass, title/caption), a glass toolbar pill, and the poster grid inside a glass tray. */
@Composable
internal fun <T> DarkGlassLibrary(
    kindEn: String, kindAr: String,
    p: LibraryParams<T>,
    key: (T) -> Long,
    title: (T) -> String,
    image: (T) -> String?,
    caption: (T) -> String?
) {
    val s = p.uiState
    var sortIndex by remember(s.selectedSort) { mutableStateOf(LibrarySortBy.entries.indexOf(s.selectedSort)) }
    var lens by remember { mutableStateOf<T?>(null) }
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        Column(Modifier.width(240.dp).fillMaxHeight().dgGlass(DG.R, 0.05f).padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(tr(kindEn, kindAr), color = DG.Text, fontSize = 26.sp, fontWeight = FontWeight.Thin)
            Text("${s.libraryCount} " + tr("titles", "عنوان"), color = DG.Amber, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
            DgCard(
                onClick = { p.onShowAll() }, shape = DG.Pill, zoom = 1.03f,
                container = if (s.selectedCategory == null) Color(0x33B69CFF) else Color.Transparent, modifier = Modifier.fillMaxWidth()
            ) { Text("◇ " + tr("All genres", "كل التصنيفات"), color = DG.Text, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)) }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp), contentPadding = PaddingValues(bottom = 30.dp)) {
                items(s.categoryNames, key = { it }) { name ->
                    val cat = s.categoryFor(name)
                    val sel = name == s.selectedCategory
                    val locked = cat?.let(p.isCategoryLocked) == true
                    DgCard(
                        onClick = { cat?.let(p.onCategoryClick) }, onLongClick = { cat?.let(p.onCategoryLongClick) }, shape = DG.Pill, zoom = 1.03f,
                        container = if (sel) Color(0x33B69CFF) else Color.Transparent, modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(horizontal = 14.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text((if (locked) "🔒 " else "") + name, color = if (sel) DG.Text else DG.Sub, fontSize = 14.sp, fontWeight = if (sel) FontWeight.Medium else FontWeight.Light, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            s.categoryCounts[name]?.let { Text("$it", color = if (sel) DG.Amber else DG.Faint, fontSize = 11.sp) }
                        }
                    }
                }
            }
        }
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.fillMaxWidth().height(120.dp).clip(DG.R)) {
                val l = lens
                val art = l?.let { if (p.isItemLocked(it)) null else image(it) }
                art?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, alpha = 0.5f, modifier = Modifier.fillMaxSize().blur(18.dp)) }
                Row(Modifier.fillMaxSize().dgGlass(DG.R, 0.06f).padding(horizontal = 22.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(s.selectedCategory ?: tr("All genres", "كل التصنيفات"), color = DG.Amber, fontSize = 12.sp)
                        Text(l?.let(title) ?: tr(kindEn, kindAr), color = DG.Text, fontSize = 26.sp, fontWeight = FontWeight.Thin, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        l?.let(caption)?.takeIf { it.isNotBlank() }?.let { Text(it, color = DG.Sub, fontSize = 13.sp, maxLines = 1) }
                    }
                    art?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.height(96.dp).aspectRatio(2f / 3f).clip(DG.RSmall)) }
                }
            }
            Row(Modifier.fillMaxWidth().dgGlass(DG.Pill, 0.05f).padding(6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DgSearchField(s.searchQuery, p.onQueryChange, tr("Search $kindEn", "ابحث في $kindAr"), Modifier.width(280.dp).focusRequester(p.initialFocusRequester))
                LazyRow(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(LibraryFilterType.entries) { f -> DgTab(filterLabel(f), f == s.selectedFilter, { p.onFilterChange(f) }) }
                }
                DgButton("⇅ " + sortLabel(s.selectedSort), {
                    sortIndex = (sortIndex + 1) % LibrarySortBy.entries.size
                    p.onSortChange(LibrarySortBy.entries[sortIndex])
                })
            }
            val items = s.visibleItems
            Box(Modifier.fillMaxSize().dgGlass(DG.R, 0.03f)) {
                if (items.isEmpty() && !s.isLoadingSelectedCategory) DgEmpty(tr("Nothing here yet", "لا يوجد محتوى هنا"))
                LazyVerticalGrid(
                    GridCells.Adaptive(140.dp), Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(items.size, key = { key(items[it]) }) { index ->
                        val item = items[index]
                        if (index >= items.size - 12) LaunchedEffect(items.size) {
                            if (s.selectedCategory != null && s.canLoadMoreSelectedCategory) p.onLoadMoreSelected()
                            else if (s.selectedCategory == null && s.hasMorePreviewRows) p.onLoadMorePreview()
                        }
                        val locked = p.isItemLocked(item)
                        DgPoster(title(item), if (locked) null else image(item), caption(item), { p.onItemClick(item) }, Modifier.onFocusChanged { if (it.hasFocus) lens = item }, locked = locked, onLongClick = { p.onItemLongClick(item) })
                    }
                }
            }
        }
    }
}

/** Guide: two floating glass panes on top (programme details START, round-cornered preview END) with vertical
 *  glass action pills between them; the grid below uses glass channel orbs and glass programme capsules. */
@Composable
internal fun DarkGlassEpg(p: EpgParams, modifier: Modifier) {
    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(Modifier.fillMaxWidth().height(196.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(Modifier.weight(1f).fillMaxHeight().dgGlass(DG.R, 0.06f).padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    p.focusedChannel?.let { DgLogo(it.name, it.logoUrl, 30.dp) }
                    Text(p.focusedChannel?.name ?: p.selectedCategoryName, color = DG.Amber, fontSize = 13.sp, maxLines = 1, modifier = Modifier.weight(1f))
                    if (p.isRefreshing) DgBadge(tr("UPDATING", "تحديث"), DG.Blue)
                }
                val prog = p.focusedProgram
                Text(prog?.title ?: tr("TV Guide", "دليل البرامج"), color = DG.Text, fontSize = 26.sp, fontWeight = FontWeight.Thin, maxLines = 1, overflow = TextOverflow.Ellipsis)
                prog?.let {
                    val now = System.currentTimeMillis()
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("${dgClock(it.startTime)} – ${dgClock(it.endTime)}", color = DG.Sub, fontSize = 12.sp)
                        if (now in it.startTime..it.endTime && it.endTime > it.startTime) DgProgress((now - it.startTime).toFloat() / (it.endTime - it.startTime), Modifier.width(200.dp))
                    }
                    Text(it.description, color = DG.Faint, fontSize = 12.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                }
            }
            Column(Modifier.width(170.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                DgTab("▤ " + p.selectedCategoryName, true, { p.onGuideInteract(); p.onOpenCategoryPicker() }, Modifier.fillMaxWidth())
                DgTab("◷ " + tr("Now", "الآن"), false, { p.onGuideInteract(); p.onJumpToNow() }, Modifier.fillMaxWidth())
                DgTab("⌕ " + tr("Search", "بحث"), false, { p.onGuideInteract(); p.onOpenSearch() }, Modifier.fillMaxWidth())
                DgTab("⋯ " + tr("Options", "خيارات"), false, { p.onGuideInteract(); p.onOpenOptions() }, Modifier.fillMaxWidth())
                Text("${p.channels.size} " + tr("channels", "قناة"), color = DG.Faint, fontSize = 11.sp, modifier = Modifier.padding(start = 14.dp))
            }
            Box(Modifier.aspectRatio(16f / 9f).fillMaxHeight().dgGlass(DG.R, 0.05f).padding(6.dp).clip(DG.RSmall).background(Color.Black)) {
                p.previewPlayerEngine?.let { PlayerRenderView(it, PlayerSurfaceResizeMode.FIT, Modifier.fillMaxSize()) }
                if (p.isPreviewLoading) Text(tr("Loading…", "جار التحميل…"), color = DG.Sub, modifier = Modifier.align(Alignment.Center))
            }
        }
        DgEpgGrid(p)
    }
}

/** DarkGlass's own grid: rounded logo cards on the channel column, pill-shaped programmes, amber "now" line. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DgEpgGrid(p: EpgParams) {
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
                        Text(dgClock(tick), color = DG.Sub, fontSize = 12.sp, fontWeight = FontWeight.Light, modifier = Modifier.padding(start = x))
                        tick += 1_800_000
                    }
                    if (now in start..end) Box(Modifier.padding(start = nowX).width(3.dp).fillMaxHeight().clip(DG.Pill).background(Brush.verticalGradient(listOf(DG.Amber, DG.Blue))))
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
                        Modifier.width(192.dp).fillMaxHeight().dgGlass(DG.Pill, if (chFocus) 0.16f else 0.05f)
                            .border(if (chFocus) 2.dp else 0.dp, if (chFocus) DG.Amber else Color.Transparent, DG.Pill)
                            .onFocusChanged { chFocus = it.isFocused; if (it.isFocused) p.onChannelFocused(c, current, first) }
                            .combinedClickable(onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c, current) })
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DgLogo(c.name, c.logoUrl, rowH - 18.dp)
                        Column(Modifier.weight(1f)) {
                            Text(c.name, color = DG.Text, fontSize = 13.sp, fontWeight = FontWeight.Light, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (c.number > 0) Text("${c.number}", color = DG.Amber, fontSize = 11.sp)
                        }
                        if (c.id in p.favoriteChannelIds) Text("★", color = DG.Amber, fontSize = 12.sp)
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.weight(1f).fillMaxHeight().horizontalScroll(hs)) {
                        Box(Modifier.width(totalW).fillMaxHeight()) {
                            if (programs.isEmpty()) Box(Modifier.fillMaxSize().dgGlass(DG.Pill, 0.02f)) {
                                Text(tr("No guide data", "لا يوجد دليل"), color = DG.Faint, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterStart).padding(start = 14.dp))
                            }
                            programs.filter { it.endTime > start && it.startTime < end }.forEach { prog ->
                                val s = maxOf(prog.startTime, start); val e = minOf(prog.endTime, end)
                                val x = (((s - start) / 60_000f) * dpPerMin).dp
                                val w = ((((e - s) / 60_000f) * dpPerMin).dp - 4.dp).coerceAtLeast(8.dp)
                                var f by remember { mutableStateOf(false) }
                                val live = prog.startTime <= now && prog.endTime > now
                                Box(
                                    Modifier.padding(start = x).width(w).fillMaxHeight()
                                        .dgGlass(DG.Pill, if (f) 0.2f else if (live) 0.1f else 0.04f)
                                        .border(if (f) 2.dp else if (live) 1.dp else 0.dp, if (f) DG.Amber else DG.Amber.copy(alpha = 0.4f), DG.Pill)
                                        .onFocusChanged { f = it.isFocused; if (it.isFocused) p.onProgramFocused(c, prog, first) }
                                        .combinedClickable(onClick = { p.onProgramClick(c, prog) }, onLongClick = { p.onChannelLongClick(c, prog) })
                                        .padding(horizontal = 10.dp)
                                ) {
                                    Column(Modifier.align(Alignment.CenterStart)) {
                                        Text(prog.title, color = DG.Text, fontSize = 13.sp, fontWeight = if (live) FontWeight.Medium else FontWeight.Light, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        if (rowH >= 58.dp) Text("${dgClock(prog.startTime)} – ${dgClock(prog.endTime)}", color = DG.Faint, fontSize = 11.sp, maxLines = 1)
                                    }
                                    if (live) Box(Modifier.align(Alignment.BottomStart).padding(bottom = 4.dp).fillMaxWidth(prog.progressFraction(now)).height(2.dp).clip(DG.Pill).background(Brush.horizontalGradient(listOf(DG.AmberDeep, DG.Blue))))
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
