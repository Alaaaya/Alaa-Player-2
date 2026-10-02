package com.streamvault.app.ui.themes.magazinemedia

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

/** Library: a "contents page". START column = table of contents (genre · dotted leader · count),
 *  end side = toolbar set as a byline strip, then a ruled poster grid. */
@Composable
internal fun <T> MagazineMediaLibrary(
    kindEn: String, kindAr: String,
    p: LibraryParams<T>,
    key: (T) -> Long,
    title: (T) -> String,
    image: (T) -> String?,
    caption: (T) -> String?
) {
    val s = p.uiState
    var sortIndex by remember(s.selectedSort) { mutableStateOf(LibrarySortBy.entries.indexOf(s.selectedSort)) }
    Row(Modifier.fillMaxSize()) {
        Column(Modifier.width(250.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            MzKicker(tr("Contents", "المحتويات"))
            Text(tr(kindEn, kindAr), color = MZ.Text, fontSize = 34.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Black)
            Text("${s.libraryCount} " + tr("titles in this issue", "عنوان"), color = MZ.Faint, fontSize = 12.sp, fontFamily = MZ.Serif, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            MzRule(Modifier.padding(vertical = 6.dp), thick = 2.dp)
            MzCard(
                onClick = { p.onCategoryClick(s.categories.firstOrNull() ?: return@MzCard) }, zoom = 1.0f,
                container = Color.Transparent, focusedContainer = MZ.Raised, modifier = Modifier.fillMaxWidth()
            ) { Text(tr("All sections", "كل التصنيفات"), color = if (s.selectedCategory == null) MZ.Amber else MZ.Text, fontSize = 15.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp)) }
            LazyColumn(contentPadding = PaddingValues(bottom = 40.dp)) {
                items(s.categoryNames, key = { it }) { name ->
                    val cat = s.categoryFor(name)
                    val sel = name == s.selectedCategory
                    val locked = cat?.let(p.isCategoryLocked) == true
                    MzCard(
                        onClick = { cat?.let(p.onCategoryClick) }, onLongClick = { cat?.let(p.onCategoryLongClick) }, zoom = 1.0f,
                        container = if (sel) MZ.Gold.copy(alpha = 0.5f) else Color.Transparent, focusedContainer = MZ.Raised, modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.Bottom) {
                            Text((if (locked) "🔒 " else "") + name, color = if (sel) MZ.Amber else MZ.Text, fontSize = 14.sp, fontFamily = MZ.Serif, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, false))
                            Text(" ····················", color = MZ.Line, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Clip, modifier = Modifier.weight(1f))
                            s.categoryCounts[name]?.let { Text("$it", color = MZ.Sub, fontSize = 12.sp, fontFamily = MZ.Serif) }
                        }
                    }
                }
            }
        }
        Box(Modifier.padding(horizontal = 22.dp).width(1.dp).fillMaxHeight().background(MZ.Text))
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MzSearchField(s.searchQuery, p.onQueryChange, tr("Search $kindEn…", "ابحث في $kindAr…"), Modifier.width(280.dp).focusRequester(p.initialFocusRequester))
                LazyRow(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    items(LibraryFilterType.entries) { f -> MzTab(filterLabel(f), f == s.selectedFilter, { p.onFilterChange(f) }) }
                }
                MzButton(tr("Sort", "ترتيب") + ": " + sortLabel(s.selectedSort), {
                    sortIndex = (sortIndex + 1) % LibrarySortBy.entries.size
                    p.onSortChange(LibrarySortBy.entries[sortIndex])
                })
            }
            MzRule(color = MZ.Line)
            Text(s.selectedCategory ?: tr("The full collection", "المجموعة الكاملة"), color = MZ.Text, fontSize = 22.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Bold)
            val items = s.visibleItems
            Box(Modifier.fillMaxSize()) {
                if (items.isEmpty() && !s.isLoadingSelectedCategory) MzEmpty(tr("Nothing in this section yet", "لا يوجد محتوى هنا"))
                LazyVerticalGrid(
                    GridCells.Adaptive(140.dp), Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 6.dp, bottom = 48.dp, start = 4.dp, end = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp), verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    items(items.size, key = { key(items[it]) }) { index ->
                        val item = items[index]
                        if (index >= items.size - 12) LaunchedEffect(items.size) {
                            if (s.selectedCategory != null && s.canLoadMoreSelectedCategory) p.onLoadMoreSelected()
                            else if (s.selectedCategory == null && s.hasMorePreviewRows) p.onLoadMorePreview()
                        }
                        val locked = p.isItemLocked(item)
                        MzPoster(title(item), if (locked) null else image(item), caption(item), { p.onItemClick(item) }, locked = locked, onLongClick = { p.onItemLongClick(item) })
                    }
                }
            }
        }
    }
}

/** Guide: the newspaper TV-listings page. Top: headline block (focused programme) beside a captioned
 *  photo plate (preview), then a ruled action line, then the grid printed as ink-ruled text cells. */
@Composable
internal fun MagazineMediaEpg(p: EpgParams, modifier: Modifier) {
    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth().height(180.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MzKicker(p.focusedChannel?.name ?: p.selectedCategoryName, Modifier.weight(1f))
                    if (p.isRefreshing) MzBadge(tr("Updating", "تحديث"), MZ.Blue)
                }
                val prog = p.focusedProgram
                Text(prog?.title ?: tr("Tonight's listings", "دليل البرامج"), color = MZ.Text, fontSize = 32.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                prog?.let {
                    Text("${mzClock(it.startTime)} – ${mzClock(it.endTime)}", color = MZ.Amber, fontSize = 14.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Bold)
                    val now = System.currentTimeMillis()
                    if (now in it.startTime..it.endTime && it.endTime > it.startTime) MzProgress((now - it.startTime).toFloat() / (it.endTime - it.startTime), Modifier.widthIn(max = 360.dp))
                    Text(it.description, color = MZ.Sub, fontSize = 13.sp, fontFamily = MZ.Serif, lineHeight = 19.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                }
            }
            Column {
                Box(Modifier.aspectRatio(16f / 9f).weight(1f).border(1.dp, MZ.Text).padding(4.dp).background(Color.Black)) {
                    p.previewPlayerEngine?.let { PlayerRenderView(it, PlayerSurfaceResizeMode.FIT, Modifier.fillMaxSize()) }
                    if (p.isPreviewLoading) Text(tr("Loading…", "جار التحميل…"), color = MZ.Gold, modifier = Modifier.align(Alignment.Center))
                }
                Text(tr("Pictured: live", "صورة مباشرة"), color = MZ.Faint, fontSize = 11.sp, fontFamily = MZ.Serif, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            }
        }
        MzRule(thick = 2.dp)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            MzTab(tr("Section", "قسم") + ": " + p.selectedCategoryName, true, { p.onGuideInteract(); p.onOpenCategoryPicker() })
            MzTab(tr("Now", "الآن"), false, { p.onGuideInteract(); p.onJumpToNow() })
            MzTab(tr("Search", "بحث"), false, { p.onGuideInteract(); p.onOpenSearch() })
            MzTab(tr("Options", "خيارات"), false, { p.onGuideInteract(); p.onOpenOptions() })
            Spacer(Modifier.weight(1f))
            Text("${p.channels.size} " + tr("channels", "قناة"), color = MZ.Faint, fontSize = 12.sp, fontFamily = MZ.Serif, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
        }
        MzRule()
        MzEpgGrid(p)
    }
}

/** Listings grid: ink-ruled square cells like a printed TV page, gold tint on what is airing, rust now-rule, ink inversion on focus. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MzEpgGrid(p: EpgParams) {
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
                        Text(mzClock(tick), color = MZ.Sub, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = x))
                        tick += 1_800_000
                    }
                    if (now in start..end) Box(Modifier.padding(start = nowX).width(2.dp).fillMaxHeight().background(MZ.Amber))
                }
            }
        }
        LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(0.dp)) {
            itemsIndexed(p.channels, key = { _, c -> c.id }) { index, c ->
                if (index >= p.channels.size - 15) LaunchedEffect(p.channels.size) { p.onRequestMoreChannels() }
                val programs = c.guideLookupKey()?.let { p.programsByChannel[it] }.orEmpty()
                val current = programs.firstOrNull { it.startTime <= now && it.endTime > now }
                val first = index == 0
                Row(Modifier.fillMaxWidth().height(rowH), verticalAlignment = Alignment.CenterVertically) {
                    var chFocus by remember { mutableStateOf(false) }
                    Row(
                        Modifier.width(192.dp).fillMaxHeight()
                            .background(if (chFocus) MZ.Text else Color.Transparent)
                            .onFocusChanged { chFocus = it.isFocused; if (it.isFocused) p.onChannelFocused(c, current, first) }
                            .combinedClickable(onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c, current) })
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MzLogo(c.name, c.logoUrl, rowH - 18.dp)
                        Column(Modifier.weight(1f)) {
                            Text(c.name, color = if (chFocus) MZ.Bg else MZ.Text, fontSize = 13.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (c.number > 0) Text("${c.number}", color = if (chFocus) MZ.Bg.copy(alpha = 0.6f) else MZ.Faint, fontSize = 11.sp)
                        }
                        if (c.id in p.favoriteChannelIds) Text("♥", color = MZ.Amber, fontSize = 12.sp)
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.weight(1f).fillMaxHeight().horizontalScroll(hs)) {
                        Box(Modifier.width(totalW).fillMaxHeight()) {
                            if (programs.isEmpty()) Box(Modifier.fillMaxSize().clip(MZ.RSmall).background(MZ.Raised.copy(alpha = 0.5f))) {
                                Text(tr("No guide data", "لا يوجد دليل"), color = MZ.Faint, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterStart).padding(start = 14.dp))
                            }
                            programs.filter { it.endTime > start && it.startTime < end }.forEach { prog ->
                                val s = maxOf(prog.startTime, start); val e = minOf(prog.endTime, end)
                                val x = (((s - start) / 60_000f) * dpPerMin).dp
                                val w = ((((e - s) / 60_000f) * dpPerMin).dp - 4.dp).coerceAtLeast(8.dp)
                                var f by remember { mutableStateOf(false) }
                                val live = prog.startTime <= now && prog.endTime > now
                                Box(
                                    Modifier.padding(start = x).width(w).fillMaxHeight().clip(MZ.RSmall)
                                        .background(if (f) MZ.Text else if (live) MZ.Gold.copy(alpha = 0.55f) else MZ.Raised)
                                        .border(1.dp, MZ.Line)
                                        .onFocusChanged { f = it.isFocused; if (it.isFocused) p.onProgramFocused(c, prog, first) }
                                        .combinedClickable(onClick = { p.onProgramClick(c, prog) }, onLongClick = { p.onChannelLongClick(c, prog) })
                                        .padding(horizontal = 10.dp)
                                ) {
                                    Column(Modifier.align(Alignment.CenterStart)) {
                                        Text(prog.title, color = if (f) MZ.Bg else MZ.Text, fontSize = 13.sp, fontFamily = MZ.Serif, fontWeight = if (live) FontWeight.Bold else FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        if (rowH >= 58.dp) Text("${mzClock(prog.startTime)} – ${mzClock(prog.endTime)}", color = if (f) MZ.Bg.copy(alpha = 0.6f) else MZ.Faint, fontSize = 11.sp, maxLines = 1)
                                    }
                                    if (live && !f) Box(Modifier.align(Alignment.BottomStart).fillMaxWidth(prog.progressFraction(now)).height(3.dp).background(MZ.Amber))
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
