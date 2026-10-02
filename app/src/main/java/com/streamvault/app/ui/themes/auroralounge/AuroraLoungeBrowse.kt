package com.streamvault.app.ui.themes.auroralounge

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
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

/** Library: a lounge menu. Serif title + lamp filters across the top, roomy pebble poster grid, and the genres as a
 *  tall rounded "menu card" on the END side with dotted leaders to their counts. */
@Composable
internal fun <T> AuroraLoungeLibrary(
    kindEn: String, kindAr: String,
    p: LibraryParams<T>,
    key: (T) -> Long,
    title: (T) -> String,
    image: (T) -> String?,
    caption: (T) -> String?
) {
    val s = p.uiState
    var sortIndex by remember(s.selectedSort) { mutableStateOf(LibrarySortBy.entries.indexOf(s.selectedSort)) }
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(26.dp)) {
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(s.selectedCategory ?: tr(kindEn, kindAr), color = AL.Text, fontSize = 34.sp, fontWeight = FontWeight.Light, fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                Text("${s.libraryCount} " + tr("titles", "عنوان"), color = AL.Amber, fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AlSearchField(s.searchQuery, p.onQueryChange, tr("Search $kindEn", "ابحث في $kindAr"), Modifier.width(280.dp).focusRequester(p.initialFocusRequester))
                LazyRow(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(LibraryFilterType.entries) { f -> AlTab(filterLabel(f), f == s.selectedFilter, { p.onFilterChange(f) }) }
                }
                AlButton(sortLabel(s.selectedSort), {
                    sortIndex = (sortIndex + 1) % LibrarySortBy.entries.size
                    p.onSortChange(LibrarySortBy.entries[sortIndex])
                }, icon = "⇅")
            }
            val items = s.visibleItems
            Box(Modifier.fillMaxSize()) {
                if (items.isEmpty() && !s.isLoadingSelectedCategory) AlEmpty(tr("Nothing here yet", "لا يوجد محتوى هنا"))
                LazyVerticalGrid(
                    GridCells.Adaptive(150.dp), Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 48.dp, start = 8.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp), verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    items(items.size, key = { key(items[it]) }) { index ->
                        val item = items[index]
                        if (index >= items.size - 12) LaunchedEffect(items.size) {
                            if (s.selectedCategory != null && s.canLoadMoreSelectedCategory) p.onLoadMoreSelected()
                            else if (s.selectedCategory == null && s.hasMorePreviewRows) p.onLoadMorePreview()
                        }
                        val locked = p.isItemLocked(item)
                        AlPoster(title(item), if (locked) null else image(item), caption(item), { p.onItemClick(item) }, locked = locked, onLongClick = { p.onItemLongClick(item) })
                    }
                }
            }
        }
        Column(
            Modifier.width(250.dp).fillMaxHeight().padding(bottom = 20.dp).clip(AL.R)
                .background(Brush.verticalGradient(listOf(AL.Raised, AL.Card.copy(alpha = 0.5f)))).border(1.dp, AL.Line, AL.R).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(tr("Genres", "التصنيفات"), color = AL.Amber, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, modifier = Modifier.padding(start = 8.dp, bottom = 6.dp))
            AlCard(
                onClick = { p.onCategoryClick(s.categories.firstOrNull() ?: return@AlCard) }, shape = AL.Pill, zoom = 1.03f,
                container = if (s.selectedCategory == null) AL.Line else Color.Transparent, modifier = Modifier.fillMaxWidth()
            ) { Text("✦ " + tr("Everything", "الكل"), color = if (s.selectedCategory == null) AL.Amber else AL.Sub, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)) }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
                items(s.categoryNames, key = { it }) { name ->
                    val cat = s.categoryFor(name)
                    val sel = name == s.selectedCategory
                    val locked = cat?.let(p.isCategoryLocked) == true
                    AlCard(
                        onClick = { cat?.let(p.onCategoryClick) }, onLongClick = { cat?.let(p.onCategoryLongClick) }, shape = AL.Pill, zoom = 1.03f,
                        container = if (sel) AL.Line else Color.Transparent, focusedContainer = AL.Line, modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(horizontal = 14.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text((if (locked) "🔒 " else "") + name, color = if (sel) AL.Amber else AL.Text, fontSize = 14.sp, fontFamily = FontFamily.Serif, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                            Text("·····································", color = AL.Line, fontSize = 12.sp, maxLines = 1, softWrap = false, overflow = TextOverflow.Clip, modifier = Modifier.weight(1f))
                            s.categoryCounts[name]?.let { Text("$it", color = AL.Faint, fontSize = 12.sp) }
                        }
                    }
                }
            }
        }
    }
}

/** Guide: an "evening programme". A START-side lounge card (preview window, focused show in serif, actions stacked),
 *  beside a grid where channels are moons, shows are soft capsules and "now" is a vertical aurora ribbon. */
@Composable
internal fun AuroraLoungeEpg(p: EpgParams, modifier: Modifier) {
    Row(modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(22.dp)) {
        Column(
            Modifier.width(300.dp).fillMaxHeight().padding(bottom = 16.dp).clip(AL.R).background(AL.Raised.copy(alpha = 0.85f)).border(1.dp, AL.Line, AL.R).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(AL.RSmall).background(Color.Black)) {
                p.previewPlayerEngine?.let { PlayerRenderView(it, PlayerSurfaceResizeMode.FIT, Modifier.fillMaxSize()) }
                if (p.isPreviewLoading) Text(tr("Loading…", "جار التحميل…"), color = AL.Sub, modifier = Modifier.align(Alignment.Center))
                if (p.isRefreshing) Box(Modifier.align(Alignment.TopEnd).padding(8.dp)) { AlBadge(tr("UPDATING", "تحديث"), AL.Blue, filled = true) }
            }
            Text(p.focusedChannel?.name ?: p.selectedCategoryName, color = AL.Amber, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, maxLines = 1)
            val prog = p.focusedProgram
            Text(prog?.title ?: tr("Tonight's programme", "برنامج الليلة"), color = AL.Text, fontSize = 22.sp, fontWeight = FontWeight.Light, fontFamily = FontFamily.Serif, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 26.sp)
            prog?.let {
                Text("${alClock(it.startTime)} – ${alClock(it.endTime)}", color = AL.Sub, fontSize = 13.sp)
                val now = System.currentTimeMillis()
                if (now in it.startTime..it.endTime && it.endTime > it.startTime) AlProgress((now - it.startTime).toFloat() / (it.endTime - it.startTime))
                Text(it.description, color = AL.Faint, fontSize = 12.sp, maxLines = 4, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.weight(1f))
            AlTab("▤  " + p.selectedCategoryName, true, { p.onGuideInteract(); p.onOpenCategoryPicker() }, Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                AlTab(tr("Now", "الآن"), false, { p.onGuideInteract(); p.onJumpToNow() })
                AlTab("⌕", false, { p.onGuideInteract(); p.onOpenSearch() })
                AlTab("⋯", false, { p.onGuideInteract(); p.onOpenOptions() })
            }
            Text("${p.channels.size} " + tr("channels", "قناة"), color = AL.Faint, fontSize = 12.sp, modifier = Modifier.padding(start = 6.dp))
        }
        AlEpgGrid(p)
    }
}

/** Aurora Lounge grid: starry ruler, channel moons, capsule shows (live = rose→gold), aurora "now" ribbon. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AlEpgGrid(p: EpgParams) {
    val start = p.guideWindowStart
    val end = p.guideWindowEnd.coerceAtLeast(start + 60_000)
    val dpPerMin = when (p.density.name) { "COMPACT" -> 4.5f; "CINEMATIC" -> 7f; else -> 5.5f }
    val rowH = when (p.density.name) { "COMPACT" -> 50.dp; "CINEMATIC" -> 74.dp; else -> 60.dp }
    val moonW = rowH + 16.dp
    val totalW = (((end - start) / 60_000f) * dpPerMin).dp
    val hs = rememberScrollState()
    val now = System.currentTimeMillis()
    val nowX = (((now - start) / 60_000f) * dpPerMin).dp
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.padding(bottom = 8.dp)) {
                Spacer(Modifier.width(moonW))
                Box(Modifier.weight(1f).horizontalScroll(hs)) {
                    Box(Modifier.width(totalW).height(28.dp)) {
                        var tick = start - (start % 900_000) + 900_000
                        while (tick < end) {
                            val x = (((tick - start) / 60_000f) * dpPerMin).dp
                            val half = tick % 1_800_000 == 0L
                            if (half) Text("✦ " + alClock(tick), color = AL.Sub, fontSize = 12.sp, fontFamily = FontFamily.Serif, modifier = Modifier.padding(start = x).align(Alignment.CenterStart))
                            else Box(Modifier.padding(start = x).size(3.dp).clip(CircleShape).background(AL.Faint).align(Alignment.CenterStart))
                            tick += 900_000
                        }
                    }
                }
            }
            LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                itemsIndexed(p.channels, key = { _, c -> c.id }) { index, c ->
                    if (index >= p.channels.size - 15) LaunchedEffect(p.channels.size) { p.onRequestMoreChannels() }
                    val programs = c.guideLookupKey()?.let { p.programsByChannel[it] }.orEmpty()
                    val current = programs.firstOrNull { it.startTime <= now && it.endTime > now }
                    val first = index == 0
                    Row(Modifier.fillMaxWidth().height(rowH), verticalAlignment = Alignment.CenterVertically) {
                        var chFocus by remember { mutableStateOf(false) }
                        Box(Modifier.width(moonW).fillMaxHeight(), contentAlignment = Alignment.Center) {
                            Box(
                                Modifier.size(rowH).clip(CircleShape)
                                    .background(if (chFocus) AL.Amber else AL.Raised)
                                    .border(if (chFocus) 3.dp else 1.dp, if (chFocus) AL.Amber else AL.Line, CircleShape)
                                    .onFocusChanged { chFocus = it.isFocused; if (it.isFocused) p.onChannelFocused(c, current, first) }
                                    .combinedClickable(onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c, current) }),
                                contentAlignment = Alignment.Center
                            ) {
                                AlLogo(c.name, c.logoUrl, rowH - 10.dp)
                                if (c.id in p.favoriteChannelIds) Text("♥", color = AL.Rose, fontSize = 11.sp, modifier = Modifier.align(Alignment.TopEnd))
                            }
                        }
                        Box(Modifier.weight(1f).fillMaxHeight().horizontalScroll(hs)) {
                            Box(Modifier.width(totalW).fillMaxHeight()) {
                                if (programs.isEmpty()) Box(Modifier.fillMaxSize().clip(AL.Pill).background(AL.Raised.copy(alpha = 0.4f))) {
                                    Text((if (c.number > 0) "${c.number}  " else "") + c.name + "  ·  " + tr("no guide", "لا يوجد دليل"), color = AL.Faint, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterStart).padding(start = 18.dp))
                                }
                                programs.filter { it.endTime > start && it.startTime < end }.forEach { prog ->
                                    val s = maxOf(prog.startTime, start); val e = minOf(prog.endTime, end)
                                    val x = (((s - start) / 60_000f) * dpPerMin).dp + 3.dp
                                    val w = ((((e - s) / 60_000f) * dpPerMin).dp - 6.dp).coerceAtLeast(10.dp)
                                    var f by remember { mutableStateOf(false) }
                                    val live = prog.startTime <= now && prog.endTime > now
                                    Box(
                                        Modifier.padding(start = x).width(w).fillMaxHeight().padding(vertical = 4.dp).clip(AL.Pill)
                                            .background(
                                                if (f) Brush.horizontalGradient(listOf(AL.Amber, Color(0xFFF4C88E)))
                                                else if (live) Brush.horizontalGradient(listOf(AL.Rose.copy(alpha = 0.35f), AL.Amber.copy(alpha = 0.25f)))
                                                else Brush.horizontalGradient(listOf(AL.Card, AL.Raised))
                                            )
                                            .border(1.dp, if (live && !f) AL.Amber.copy(alpha = 0.55f) else AL.Line.copy(alpha = 0.6f), AL.Pill)
                                            .onFocusChanged { f = it.isFocused; if (it.isFocused) p.onProgramFocused(c, prog, first) }
                                            .combinedClickable(onClick = { p.onProgramClick(c, prog) }, onLongClick = { p.onChannelLongClick(c, prog) })
                                            .padding(horizontal = 16.dp)
                                    ) {
                                        Row(Modifier.align(Alignment.CenterStart), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            if (live) Box(Modifier.size(6.dp).clip(CircleShape).background(if (f) AL.Bg else AL.Live))
                                            Column {
                                                Text(prog.title, color = if (f) AL.Bg else AL.Text, fontSize = 13.sp, fontWeight = if (live) FontWeight.SemiBold else FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                if (rowH >= 60.dp) Text(alClock(prog.startTime), color = if (f) AL.Bg.copy(alpha = 0.6f) else AL.Faint, fontSize = 11.sp, maxLines = 1)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (now in start..end) Box(Modifier.fillMaxHeight().padding(start = moonW).horizontalScroll(hs, enabled = false)) {
            Box(Modifier.width(totalW).fillMaxHeight()) {
                Box(Modifier.padding(start = nowX - 7.dp).width(14.dp).fillMaxHeight().background(Brush.horizontalGradient(listOf(Color.Transparent, AL.Blue.copy(alpha = 0.18f), Color.Transparent))))
                Box(Modifier.padding(start = nowX).width(2.dp).fillMaxHeight().background(Brush.verticalGradient(listOf(AL.Blue, AL.Rose, AL.Amber))))
                Box(Modifier.padding(start = nowX - 4.dp, top = 2.dp).size(10.dp).clip(CircleShape).background(AL.Blue))
            }
        }
    }
}

private fun com.streamvault.domain.model.Program.progressFraction(now: Long): Float =
    if (endTime > startTime) ((now - startTime).toFloat() / (endTime - startTime)).coerceIn(0f, 1f) else 0f
