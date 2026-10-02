package com.streamvault.app.ui.themes.futuristichud

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

/** Library: START "index" column of genres (bracketed, numbered), main area = readout header + dense poster grid,
 *  and a bottom command bar where filters and sort are F-key style cells. */
@Composable
internal fun <T> FuturisticHudLibrary(
    kindEn: String, kindAr: String,
    p: LibraryParams<T>,
    key: (T) -> Long,
    title: (T) -> String,
    image: (T) -> String?,
    caption: (T) -> String?
) {
    val s = p.uiState
    var sortIndex by remember(s.selectedSort) { mutableStateOf(LibrarySortBy.entries.indexOf(s.selectedSort)) }
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        Column(Modifier.width(220.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            FhLabel(tr("Index", "التصنيفات") + " [${s.categoryNames.size}]")
            FhCard(
                onClick = { p.onCategoryClick(s.categories.firstOrNull() ?: return@FhCard) }, shape = FH.RSmall, zoom = 1.0f,
                container = if (s.selectedCategory == null) FH.Amber.copy(alpha = 0.16f) else Color.Transparent, focusedContainer = FH.Amber.copy(alpha = 0.3f), modifier = Modifier.fillMaxWidth()
            ) { Text("00 " + tr("ALL", "الكل"), color = if (s.selectedCategory == null) FH.Amber else FH.Sub, fontSize = 12.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp)) }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp), contentPadding = PaddingValues(bottom = 40.dp)) {
                itemsIndexed(s.categoryNames, key = { _, n -> n }) { i, name ->
                    val cat = s.categoryFor(name)
                    val sel = name == s.selectedCategory
                    val locked = cat?.let(p.isCategoryLocked) == true
                    FhCard(
                        onClick = { cat?.let(p.onCategoryClick) }, onLongClick = { cat?.let(p.onCategoryLongClick) }, shape = FH.RSmall, zoom = 1.0f,
                        container = if (sel) FH.Amber.copy(alpha = 0.16f) else Color.Transparent, focusedContainer = FH.Amber.copy(alpha = 0.3f), modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("%02d".format(i + 1), color = if (sel) FH.Amber else FH.Faint, fontSize = 10.sp, fontFamily = FH.Mono)
                            Text((if (locked) "⊘ " else "") + name.uppercase(), color = if (sel) FH.Amber else FH.Text, fontSize = 12.sp, fontFamily = FH.Mono, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            s.categoryCounts[name]?.let { Text("$it", color = FH.Faint, fontSize = 10.sp, fontFamily = FH.Mono) }
                        }
                    }
                }
            }
        }
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("// " + tr(kindEn, kindAr).uppercase(), color = FH.Text, fontSize = 20.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Black)
                Text((s.selectedCategory?.uppercase() ?: "ALL") + " · ${s.libraryCount}", color = FH.Amber, fontSize = 12.sp, fontFamily = FH.Mono, modifier = Modifier.weight(1f), maxLines = 1)
                FhSearchField(s.searchQuery, p.onQueryChange, tr("Search $kindEn", "ابحث في $kindAr"), Modifier.width(300.dp).focusRequester(p.initialFocusRequester))
            }
            val items = s.visibleItems
            Box(Modifier.weight(1f).fillMaxWidth()) {
                if (items.isEmpty() && !s.isLoadingSelectedCategory) FhEmpty(tr("Nothing here yet", "لا يوجد محتوى هنا"))
                LazyVerticalGrid(
                    GridCells.Adaptive(132.dp), Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 6.dp, bottom = 16.dp, start = 4.dp, end = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(items.size, key = { key(items[it]) }) { index ->
                        val item = items[index]
                        if (index >= items.size - 12) LaunchedEffect(items.size) {
                            if (s.selectedCategory != null && s.canLoadMoreSelectedCategory) p.onLoadMoreSelected()
                            else if (s.selectedCategory == null && s.hasMorePreviewRows) p.onLoadMorePreview()
                        }
                        val locked = p.isItemLocked(item)
                        FhPoster(title(item), if (locked) null else image(item), caption(item), { p.onItemClick(item) }, locked = locked, onLongClick = { p.onItemLongClick(item) })
                    }
                }
            }
            Row(Modifier.fillMaxWidth().border(1.dp, FH.Line, FH.RSmall).padding(6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                LazyRow(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    itemsIndexed(LibraryFilterType.entries) { i, f -> FhTab("F${i + 1} " + filterLabel(f), f == s.selectedFilter, { p.onFilterChange(f) }) }
                }
                FhButton("SORT " + sortLabel(s.selectedSort), {
                    sortIndex = (sortIndex + 1) % LibrarySortBy.entries.size
                    p.onSortChange(LibrarySortBy.entries[sortIndex])
                })
            }
        }
    }
}

/** Guide: top "command" column of bracketed cells at START, program telemetry readout in the middle,
 *  preview monitor with reticle at the END; the timeline below has a cyan scan line for "now". */
@Composable
internal fun FuturisticHudEpg(p: EpgParams, modifier: Modifier) {
    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth().height(176.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.width(200.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                FhTab("▤ " + p.selectedCategoryName, true, { p.onGuideInteract(); p.onOpenCategoryPicker() }, Modifier.fillMaxWidth())
                FhTab("◎ " + tr("Now", "الآن"), false, { p.onGuideInteract(); p.onJumpToNow() }, Modifier.fillMaxWidth())
                FhTab("QRY " + tr("Search", "بحث"), false, { p.onGuideInteract(); p.onOpenSearch() }, Modifier.fillMaxWidth())
                FhTab("CFG " + tr("Options", "خيارات"), false, { p.onGuideInteract(); p.onOpenOptions() }, Modifier.fillMaxWidth())
            }
            FhPanel(Modifier.weight(1f).fillMaxHeight()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FhLabel((p.focusedChannel?.name ?: p.selectedCategoryName) + " · ${p.channels.size} CH", Modifier.weight(1f))
                    if (p.isRefreshing) FhBadge(tr("Sync", "تحديث"), FH.Blue)
                }
                val prog = p.focusedProgram
                Text((prog?.title ?: tr("TV Guide", "دليل البرامج")).uppercase(), color = FH.Text, fontSize = 22.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                prog?.let {
                    Text("T ${fhClock(it.startTime)} › ${fhClock(it.endTime)}", color = FH.Amber, fontSize = 12.sp, fontFamily = FH.Mono)
                    val now = System.currentTimeMillis()
                    if (now in it.startTime..it.endTime && it.endTime > it.startTime) FhProgress((now - it.startTime).toFloat() / (it.endTime - it.startTime), Modifier.widthIn(max = 360.dp), 5.dp)
                    Text(it.description, color = FH.Sub, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            Box(Modifier.aspectRatio(16f / 9f).fillMaxHeight().background(Color.Black).border(1.dp, FH.Line).fhBrackets(FH.Amber, 14.dp)) {
                p.previewPlayerEngine?.let { PlayerRenderView(it, PlayerSurfaceResizeMode.FIT, Modifier.fillMaxSize()) }
                if (p.isPreviewLoading) Text("ACQUIRING…", color = FH.Amber, fontFamily = FH.Mono, modifier = Modifier.align(Alignment.Center))
            }
        }
        FhEpgGrid(p)
    }
}

/** FuturisticHud's own grid: rounded logo cards on the channel column, pill-shaped programmes, amber "now" line. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FhEpgGrid(p: EpgParams) {
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
                        Text("|" + fhClock(tick), color = FH.Amber, fontSize = 11.sp, fontFamily = FH.Mono, modifier = Modifier.padding(start = x))
                        tick += 1_800_000
                    }
                    if (now in start..end) Text("▼NOW", color = FH.Live, fontSize = 9.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = nowX))
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
                        Modifier.width(192.dp).fillMaxHeight().clip(FH.RSmall)
                            .background(if (chFocus) FH.Amber else FH.Card).border(1.dp, FH.Line, FH.RSmall)
                            .onFocusChanged { chFocus = it.isFocused; if (it.isFocused) p.onChannelFocused(c, current, first) }
                            .combinedClickable(onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c, current) })
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FhLogo(c.name, c.logoUrl, rowH - 18.dp)
                        Column(Modifier.weight(1f)) {
                            Text(c.name.uppercase(), color = if (chFocus) FH.Bg else FH.Text, fontSize = 12.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (c.number > 0) Text("CH%03d".format(c.number), color = if (chFocus) FH.Bg.copy(alpha = 0.6f) else FH.Faint, fontSize = 11.sp)
                        }
                        if (c.id in p.favoriteChannelIds) Text("★", color = FH.Warn, fontSize = 12.sp)
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.weight(1f).fillMaxHeight().horizontalScroll(hs)) {
                        Box(Modifier.width(totalW).fillMaxHeight()) {
                            if (programs.isEmpty()) Box(Modifier.fillMaxSize().clip(FH.RSmall).background(FH.Raised.copy(alpha = 0.5f))) {
                                Text(tr("No guide data", "لا يوجد دليل"), color = FH.Faint, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterStart).padding(start = 14.dp))
                            }
                            programs.filter { it.endTime > start && it.startTime < end }.forEach { prog ->
                                val s = maxOf(prog.startTime, start); val e = minOf(prog.endTime, end)
                                val x = (((s - start) / 60_000f) * dpPerMin).dp
                                val w = ((((e - s) / 60_000f) * dpPerMin).dp - 4.dp).coerceAtLeast(8.dp)
                                var f by remember { mutableStateOf(false) }
                                val live = prog.startTime <= now && prog.endTime > now
                                Box(
                                    Modifier.padding(start = x).width(w).fillMaxHeight().clip(FH.RSmall)
                                        .background(if (f) FH.Amber else if (live) FH.Amber.copy(alpha = 0.12f) else FH.Card.copy(alpha = 0.8f))
                                        .border(1.dp, if (live && !f) FH.Amber.copy(alpha = 0.7f) else FH.Line, FH.RSmall)
                                        .onFocusChanged { f = it.isFocused; if (it.isFocused) p.onProgramFocused(c, prog, first) }
                                        .combinedClickable(onClick = { p.onProgramClick(c, prog) }, onLongClick = { p.onChannelLongClick(c, prog) })
                                        .padding(horizontal = 10.dp)
                                ) {
                                    Column(Modifier.align(Alignment.CenterStart)) {
                                        Text(prog.title.uppercase(), color = if (f) FH.Bg else FH.Text, fontSize = 12.sp, fontFamily = FH.Mono, fontWeight = if (live) FontWeight.Bold else FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        if (rowH >= 58.dp) Text("${fhClock(prog.startTime)} – ${fhClock(prog.endTime)}", color = if (f) FH.Bg.copy(alpha = 0.6f) else FH.Faint, fontSize = 11.sp, maxLines = 1)
                                    }
                                    if (live && !f) Box(Modifier.align(Alignment.BottomStart).fillMaxWidth(prog.progressFraction(now)).height(3.dp).background(FH.Amber))
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
