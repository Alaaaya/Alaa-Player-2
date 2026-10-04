package com.streamvault.app.ui.themes.chatgpt

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

/** Library: receiver "media library". END-side genre index (serif, brass ruled), START = focused-title fanart
 *  info strip on top + framed poster grid. Filters are a text menu row, sort is a cycling brass key. */
@Composable
internal fun <T> ChatGptLibrary(
    kindEn: String, kindAr: String,
    p: LibraryParams<T>,
    key: (T) -> Long,
    title: (T) -> String,
    image: (T) -> String?,
    caption: (T) -> String?
) {
    val s = p.uiState
    var sortIndex by remember(s.selectedSort) { mutableStateOf(LibrarySortBy.entries.indexOf(s.selectedSort)) }
    var focusedItem by remember { mutableStateOf<T?>(null) }
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(22.dp)) {
        Column(Modifier.width(240.dp).fillMaxHeight().background(CG.Raised.copy(alpha = 0.7f), CG.R).padding(vertical = 12.dp)) {
            CgHeading(tr("Genres", "التصنيفات"), size = 12, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(CG.Line))
            LazyColumn(contentPadding = PaddingValues(vertical = 6.dp, horizontal = 6.dp)) {
                item {
                    CgCard(onClick = { p.onShowAll() }, shape = CG.RSmall,
                        container = if (s.selectedCategory == null) CG.AmberDeep else Color.Transparent, modifier = Modifier.fillMaxWidth()) {
                        Text(tr("All", "الكل"), color = if (s.selectedCategory == null) Color.White else CG.Sub, fontSize = 15.sp, fontFamily = CG.Serif, modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp))
                    }
                }
                items(s.categoryNames, key = { it }) { name ->
                    val cat = s.categoryFor(name)
                    val sel = name == s.selectedCategory
                    val locked = cat?.let(p.isCategoryLocked) == true
                    CgCard(onClick = { cat?.let(p.onCategoryClick) }, onLongClick = { cat?.let(p.onCategoryLongClick) }, shape = CG.RSmall,
                        container = if (sel) CG.AmberDeep else Color.Transparent, modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text((if (locked) "🔒 " else "") + name, color = if (sel) Color.White else CG.Sub, fontSize = 14.sp, fontFamily = CG.Serif, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            s.categoryCounts[name]?.let { Text("$it", color = CG.Faint, fontSize = 11.sp) }
                        }
                    }
                }
            }
        }
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.fillMaxWidth().height(84.dp).clip(CG.R).background(CG.Raised)) {
                focusedItem?.let { image(it) }?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(CG.Bg, CG.Bg.copy(alpha = 0.8f), Color.Transparent))))
                Column(Modifier.align(Alignment.CenterStart).padding(horizontal = 20.dp)) {
                    CgHeading(tr(kindEn, kindAr) + "  ·  ${s.libraryCount}", size = 11)
                    Text(focusedItem?.let(title) ?: (s.selectedCategory ?: tr("All titles", "كل العناوين")), color = CG.Text, fontSize = 26.sp, fontFamily = CG.Serif, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    focusedItem?.let(caption)?.takeIf { it.isNotBlank() }?.let { Text(it, color = CG.Sub, fontSize = 12.sp, maxLines = 1) }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CgSearchField(s.searchQuery, p.onQueryChange, tr("Search", "بحث"), Modifier.width(220.dp).focusRequester(p.initialFocusRequester))
                LazyRow(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    items(LibraryFilterType.entries) { f -> CgTab(filterLabel(f), f == s.selectedFilter, { p.onFilterChange(f) }) }
                }
                CgButton(sortLabel(s.selectedSort), {
                    sortIndex = (sortIndex + 1) % LibrarySortBy.entries.size
                    p.onSortChange(LibrarySortBy.entries[sortIndex])
                }, icon = "⇅")
            }
            val items = s.visibleItems
            Box(Modifier.fillMaxSize()) {
                if (items.isEmpty() && !s.isLoadingSelectedCategory) CgEmpty(tr("Nothing here yet", "لا يوجد محتوى هنا"))
                LazyVerticalGrid(
                    GridCells.Fixed(5), Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 6.dp, bottom = 40.dp, start = 4.dp, end = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(items.size, key = { key(items[it]) }) { index ->
                        val item = items[index]
                        if (index >= items.size - 12) LaunchedEffect(items.size) {
                            if (s.selectedCategory != null && s.canLoadMoreSelectedCategory) p.onLoadMoreSelected()
                            else if (s.selectedCategory == null && s.hasMorePreviewRows) p.onLoadMorePreview()
                        }
                        val locked = p.isItemLocked(item)
                        CgPoster(title(item), if (locked) null else image(item), caption(item), { p.onItemClick(item) },
                            Modifier.onFocusChanged { if (it.hasFocus) focusedItem = item }, locked = locked, onLongClick = { p.onItemLongClick(item) })
                    }
                }
            }
        }
    }
}

/** Guide: receiver EPG. Text-menu toolbar on top, the grid fills the middle, and a BOTTOM "info panel"
 *  (preview window START, programme card with serif title + brass progress END) like a satellite receiver. */
@Composable
internal fun ChatGptEpg(p: EpgParams, modifier: Modifier) {
    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            CgHeading(tr("Programme guide", "دليل البرامج"), size = 13, modifier = Modifier.padding(end = 14.dp))
            CgTab(p.selectedCategoryName, true, { p.onGuideInteract(); p.onOpenCategoryPicker() })
            CgTab(tr("Now", "الآن"), false, { p.onGuideInteract(); p.onJumpToNow() })
            CgTab(tr("Search", "بحث"), false, { p.onGuideInteract(); p.onOpenSearch() })
            CgTab(tr("Options", "خيارات"), false, { p.onGuideInteract(); p.onOpenOptions() })
            Spacer(Modifier.weight(1f))
            if (p.isRefreshing) CgBadge(tr("Updating", "تحديث"), CG.Blue)
            Text("  ${p.channels.size} " + tr("channels", "قناة"), color = CG.Faint, fontSize = 12.sp)
        }
        Box(Modifier.weight(1f).fillMaxWidth()) { CgEpgGrid(p) }
        Row(Modifier.fillMaxWidth().height(150.dp).background(Color(0xFF0F0C08), CG.R).border(1.dp, CG.Line, CG.R).padding(10.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            Box(Modifier.aspectRatio(16f / 9f).fillMaxHeight().background(Color.Black).border(1.dp, CG.Line)) {
                p.previewPlayerEngine?.let { PlayerRenderView(it, PlayerSurfaceResizeMode.FIT, Modifier.fillMaxSize()) }
                if (p.isPreviewLoading) Text(tr("Tuning…", "جار الضبط…"), color = CG.Sub, fontFamily = CG.Serif, modifier = Modifier.align(Alignment.Center))
            }
            val prog = p.focusedProgram
            Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                CgHeading(p.focusedChannel?.name ?: p.selectedCategoryName, size = 11)
                Text(prog?.title ?: tr("Select a programme", "اختر برنامجاً"), color = CG.Text, fontSize = 24.sp, fontFamily = CG.Serif, maxLines = 1, overflow = TextOverflow.Ellipsis)
                prog?.let {
                    val now = System.currentTimeMillis()
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("${cgClock(it.startTime)} – ${cgClock(it.endTime)}", color = CG.Sub, fontSize = 13.sp)
                        if (now in it.startTime..it.endTime && it.endTime > it.startTime) CgProgress((now - it.startTime).toFloat() / (it.endTime - it.startTime), Modifier.width(240.dp))
                    }
                    Text(it.description, color = CG.Faint, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

/** Media Center grid: ruled ledger rows, square programme cells with brass rules, gold fill on focus. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CgEpgGrid(p: EpgParams) {
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
                        Text(cgClock(tick), color = CG.Sub, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = x))
                        tick += 1_800_000
                    }
                    if (now in start..end) Box(Modifier.padding(start = nowX).width(2.dp).fillMaxHeight().background(CG.Amber))
                }
            }
        }
        LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            itemsIndexed(p.channels, key = { _, c -> c.id }) { index, c ->
                if (index >= p.channels.size - 15) LaunchedEffect(p.channels.size) { p.onRequestMoreChannels() }
                val programs = c.guideLookupKey()?.let { p.programsByChannel[it] }.orEmpty()
                val current = programs.firstOrNull { it.startTime <= now && it.endTime > now }
                val first = index == 0
                Row(Modifier.fillMaxWidth().height(rowH), verticalAlignment = Alignment.CenterVertically) {
                    var chFocus by remember { mutableStateOf(false) }
                    Row(
                        Modifier.width(192.dp).fillMaxHeight().clip(CG.RSmall)
                            .background(if (chFocus) CG.Amber else CG.Raised)
                            .onFocusChanged { chFocus = it.isFocused; if (it.isFocused) p.onChannelFocused(c, current, first) }
                            .combinedClickable(onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c, current) })
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CgLogo(c.name, c.logoUrl, rowH - 18.dp)
                        Column(Modifier.weight(1f)) {
                            Text(c.name, color = if (chFocus) CG.Bg else CG.Text, fontSize = 13.sp, fontFamily = CG.Serif, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (c.number > 0) Text("${c.number}", color = if (chFocus) CG.Bg.copy(alpha = 0.6f) else CG.Faint, fontSize = 11.sp)
                        }
                        if (c.id in p.favoriteChannelIds) Text("★", color = CG.Amber, fontSize = 12.sp)
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.weight(1f).fillMaxHeight().horizontalScroll(hs)) {
                        Box(Modifier.width(totalW).fillMaxHeight()) {
                            if (programs.isEmpty()) Box(Modifier.fillMaxSize().clip(CG.RSmall).background(CG.Raised.copy(alpha = 0.5f))) {
                                Text(tr("No guide data", "لا يوجد دليل"), color = CG.Faint, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterStart).padding(start = 14.dp))
                            }
                            programs.filter { it.endTime > start && it.startTime < end }.forEach { prog ->
                                val s = maxOf(prog.startTime, start); val e = minOf(prog.endTime, end)
                                val x = (((s - start) / 60_000f) * dpPerMin).dp
                                val w = ((((e - s) / 60_000f) * dpPerMin).dp - 4.dp).coerceAtLeast(8.dp)
                                var f by remember { mutableStateOf(false) }
                                val live = prog.startTime <= now && prog.endTime > now
                                Box(
                                    Modifier.padding(start = x).width(w).fillMaxHeight().clip(CG.RSmall)
                                        .background(if (f) CG.Amber else if (live) CG.AmberDeep else CG.Raised)
                                        .border(1.dp, if (live && !f) CG.Amber.copy(alpha = 0.6f) else CG.Line.copy(alpha = 0.6f), CG.RSmall)
                                        .onFocusChanged { f = it.isFocused; if (it.isFocused) p.onProgramFocused(c, prog, first) }
                                        .combinedClickable(onClick = { p.onProgramClick(c, prog) }, onLongClick = { p.onChannelLongClick(c, prog) })
                                        .padding(horizontal = 10.dp)
                                ) {
                                    Column(Modifier.align(Alignment.CenterStart)) {
                                        Text(prog.title, color = if (f) CG.Bg else CG.Text, fontSize = 13.sp, fontFamily = CG.Serif, fontWeight = if (live) FontWeight.Bold else FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        if (rowH >= 58.dp) Text("${cgClock(prog.startTime)} – ${cgClock(prog.endTime)}", color = if (f) CG.Bg.copy(alpha = 0.6f) else CG.Faint, fontSize = 11.sp, maxLines = 1)
                                    }
                                    if (live && !f) Box(Modifier.align(Alignment.BottomStart).fillMaxWidth(prog.progressFraction(now)).height(3.dp).background(CG.Amber))
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
