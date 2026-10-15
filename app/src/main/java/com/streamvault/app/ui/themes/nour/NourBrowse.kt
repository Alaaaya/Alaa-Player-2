package com.streamvault.app.ui.themes.nour

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

@Composable
internal fun <T> NourLibrary(
    kindEn: String, kindAr: String,
    p: LibraryParams<T>,
    key: (T) -> Long,
    title: (T) -> String,
    image: (T) -> String?,
    caption: (T) -> String?,
    backdrop: (T) -> String?,
    plot: (T) -> String?
) {
    val s = p.uiState
    var focusedItem by remember { mutableStateOf<T?>(null) }
    val hero = focusedItem?.takeIf { item -> s.visibleItems.any { key(it) == key(item) } } ?: s.visibleItems.firstOrNull()
    var sortIndex by remember(s.selectedSort) { mutableStateOf(LibrarySortBy.entries.indexOf(s.selectedSort)) }
    Row(Modifier.fillMaxSize().padding(horizontal = 36.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (hero != null && s.searchQuery.isBlank()) Box(Modifier.fillMaxWidth().height(180.dp).clip(NR.R)) {
                androidx.compose.animation.Crossfade(targetState = if (p.isItemLocked(hero)) null else backdrop(hero) ?: image(hero),
                    animationSpec = androidx.compose.animation.core.tween(700), label = "nourLibraryArt") { url ->
                    url?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                }
                Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(NR.Bg.copy(alpha = 0.94f), NR.Bg.copy(alpha = 0.25f)))))
                Column(Modifier.align(Alignment.CenterStart).padding(22.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(tr(kindEn, kindAr).uppercase(), color = NR.Sub, fontSize = 10.sp, letterSpacing = 2.sp)
                    Text(if (p.isItemLocked(hero)) tr("Locked", "مقفل") else title(hero), color = NR.Text, fontSize = 30.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth(0.7f))
                    if (!p.isItemLocked(hero)) Text(plot(hero).orEmpty(), color = NR.Sub, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth(0.65f))
                    NourButton(tr("Details", "التفاصيل"), { p.onItemClick(hero) }, primary = true)
                }
            }
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(s.selectedCategory ?: tr(kindEn, kindAr), color = NR.Text, fontSize = 34.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif, fontStyle = FontStyle.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                Text("${s.libraryCount} " + tr("titles", "عنوان"), color = NR.Amber, fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NourSearchField(s.searchQuery, p.onQueryChange, tr("Search $kindEn", "ابحث في $kindAr"), Modifier.width(280.dp).focusRequester(p.initialFocusRequester))
                LazyRow(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(LibraryFilterType.entries) { f -> NourTab(filterLabel(f), f == s.selectedFilter, { p.onFilterChange(f) }) }
                }
                NourButton(sortLabel(s.selectedSort), {
                    sortIndex = (sortIndex + 1) % LibrarySortBy.entries.size
                    p.onSortChange(LibrarySortBy.entries[sortIndex])
                }, icon = "⇅")
            }
            val items = s.visibleItems
            Box(Modifier.fillMaxSize()) {
                if (items.isEmpty() && !s.isLoadingSelectedCategory) NourEmpty(tr("Nothing here yet", "لا يوجد محتوى هنا"))
                LazyVerticalGrid(
                    GridCells.Adaptive(130.dp), Modifier.fillMaxSize(),
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
                        NourPoster(title(item), if (locked) null else image(item), caption(item), { p.onItemClick(item) }, Modifier.onFocusChanged { if (it.hasFocus) focusedItem = item }, locked = locked, onLongClick = { p.onItemLongClick(item) })
                    }
                }
            }
        }
        Column(
            Modifier.width(200.dp).fillMaxHeight().padding(bottom = 20.dp).clip(NR.R)
                .background(Brush.verticalGradient(listOf(NR.Raised, NR.Card.copy(alpha = 0.5f)))).border(1.dp, NR.Line, NR.R).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(tr("Genres", "التصنيفات"), color = NR.Amber, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, modifier = Modifier.padding(start = 8.dp, bottom = 6.dp))
            NourCard(
                onClick = { p.onShowAll() }, shape = NR.Pill, zoom = 1.03f,
                container = if (s.selectedCategory == null) NR.Line else Color.Transparent, modifier = Modifier.fillMaxWidth()
            ) { Text("✦ " + tr("Everything", "الكل"), color = if (s.selectedCategory == null) NR.Amber else NR.Sub, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)) }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
                items(s.categoryNames, key = { it }) { name ->
                    val cat = s.categoryFor(name)
                    val sel = name == s.selectedCategory
                    val locked = cat?.let(p.isCategoryLocked) == true
                    NourCard(
                        onClick = { cat?.let(p.onCategoryClick) }, onLongClick = { cat?.let(p.onCategoryLongClick) }, shape = NR.Pill, zoom = 1.03f,
                        container = if (sel) NR.Line else Color.Transparent, focusedContainer = NR.Line, modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(horizontal = 14.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text((if (locked) "🔒 " else "") + name, color = if (sel) NR.Amber else NR.Text, fontSize = 14.sp, fontFamily = FontFamily.SansSerif, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                            Spacer(Modifier.weight(1f))
                            s.categoryCounts[name]?.let { Text("$it", color = NR.Faint, fontSize = 12.sp) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun NourEpg(p: EpgParams, modifier: Modifier) {
    Row(modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(22.dp)) {
        Column(
            Modifier.width(300.dp).fillMaxHeight().padding(bottom = 16.dp).clip(NR.R).background(NR.Raised.copy(alpha = 0.85f)).border(1.dp, NR.Line, NR.R).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(NR.RSmall).background(Color.Black)) {
                p.previewPlayerEngine?.let { PlayerRenderView(it, PlayerSurfaceResizeMode.FIT, Modifier.fillMaxSize()) }
                if (p.isPreviewLoading) Text(tr("Loading…", "جار التحميل…"), color = NR.Sub, modifier = Modifier.align(Alignment.Center))
                if (p.isRefreshing) Box(Modifier.align(Alignment.TopEnd).padding(8.dp)) { NourBadge(tr("UPDATING", "تحديث"), NR.Blue, filled = true) }
            }
            Text(p.focusedChannel?.name ?: p.selectedCategoryName, color = NR.Amber, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, maxLines = 1)
            val prog = p.focusedProgram
            Text(prog?.title ?: tr("Tonight's programme", "برنامج الليلة"), color = NR.Text, fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 26.sp)
            prog?.let {
                Text("${nourClock(it.startTime)} – ${nourClock(it.endTime)}", color = NR.Sub, fontSize = 13.sp)
                val now = System.currentTimeMillis()
                if (now in it.startTime..it.endTime && it.endTime > it.startTime) NourProgress((now - it.startTime).toFloat() / (it.endTime - it.startTime))
                Text(it.description, color = NR.Faint, fontSize = 12.sp, maxLines = 4, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.weight(1f))
            NourTab("▤  " + p.selectedCategoryName, true, { p.onGuideInteract(); p.onOpenCategoryPicker() }, Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                NourTab(tr("Now", "الآن"), false, { p.onGuideInteract(); p.onJumpToNow() })
                NourTab("⌕", false, { p.onGuideInteract(); p.onOpenSearch() })
                NourTab("⋯", false, { p.onGuideInteract(); p.onOpenOptions() })
            }
            Text("${p.channels.size} " + tr("channels", "قناة"), color = NR.Faint, fontSize = 12.sp, modifier = Modifier.padding(start = 6.dp))
        }
        NourEpgGrid(p)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NourEpgGrid(p: EpgParams) {
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
                            if (half) Text("✦ " + nourClock(tick), color = NR.Sub, fontSize = 12.sp, fontFamily = FontFamily.SansSerif, modifier = Modifier.padding(start = x).align(Alignment.CenterStart))
                            else Box(Modifier.padding(start = x).size(3.dp).clip(CircleShape).background(NR.Faint).align(Alignment.CenterStart))
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
                                    .background(if (chFocus) NR.Amber else NR.Raised)
                                    .border(if (chFocus) 3.dp else 1.dp, if (chFocus) NR.Amber else NR.Line, CircleShape)
                                    .onFocusChanged { chFocus = it.isFocused; if (it.isFocused) p.onChannelFocused(c, current, first) }
                                    .combinedClickable(onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c, current) }),
                                contentAlignment = Alignment.Center
                            ) {
                                NourLogo(c.name, c.logoUrl, rowH - 10.dp)
                                if (c.id in p.favoriteChannelIds) Text("♥", color = NR.Rose, fontSize = 11.sp, modifier = Modifier.align(Alignment.TopEnd))
                            }
                        }
                        Box(Modifier.weight(1f).fillMaxHeight().horizontalScroll(hs)) {
                            Box(Modifier.width(totalW).fillMaxHeight()) {
                                if (programs.isEmpty()) Box(Modifier.fillMaxSize().clip(NR.Pill).background(NR.Raised.copy(alpha = 0.4f))) {
                                    Text((if (c.number > 0) "${c.number}  " else "") + c.name + "  ·  " + tr("no guide", "لا يوجد دليل"), color = NR.Faint, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterStart).padding(start = 18.dp))
                                }
                                programs.filter { it.endTime > start && it.startTime < end }.forEach { prog ->
                                    val s = maxOf(prog.startTime, start); val e = minOf(prog.endTime, end)
                                    val x = (((s - start) / 60_000f) * dpPerMin).dp + 3.dp
                                    val w = ((((e - s) / 60_000f) * dpPerMin).dp - 6.dp).coerceAtLeast(10.dp)
                                    var f by remember { mutableStateOf(false) }
                                    val live = prog.startTime <= now && prog.endTime > now
                                    Box(
                                        Modifier.padding(start = x).width(w).fillMaxHeight().padding(vertical = 4.dp).clip(NR.Pill)
                                            .background(
                                                if (f) Brush.horizontalGradient(listOf(NR.Amber, Color(0xFFF4C88E)))
                                                else if (live) Brush.horizontalGradient(listOf(NR.Rose.copy(alpha = 0.35f), NR.Amber.copy(alpha = 0.25f)))
                                                else Brush.horizontalGradient(listOf(NR.Card, NR.Raised))
                                            )
                                            .border(1.dp, if (live && !f) NR.Amber.copy(alpha = 0.55f) else NR.Line.copy(alpha = 0.6f), NR.Pill)
                                            .onFocusChanged { f = it.isFocused; if (it.isFocused) p.onProgramFocused(c, prog, first) }
                                            .combinedClickable(onClick = { p.onProgramClick(c, prog) }, onLongClick = { p.onChannelLongClick(c, prog) })
                                            .padding(horizontal = 16.dp)
                                    ) {
                                        Row(Modifier.align(Alignment.CenterStart), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            if (live) Box(Modifier.size(6.dp).clip(CircleShape).background(if (f) NR.Bg else NR.Live))
                                            Column {
                                                Text(prog.title, color = if (f) NR.Bg else NR.Text, fontSize = 13.sp, fontWeight = if (live) FontWeight.SemiBold else FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                if (rowH >= 60.dp) Text(nourClock(prog.startTime), color = if (f) NR.Bg.copy(alpha = 0.6f) else NR.Faint, fontSize = 11.sp, maxLines = 1)
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
                Box(Modifier.padding(start = nowX - 7.dp).width(14.dp).fillMaxHeight().background(Brush.horizontalGradient(listOf(Color.Transparent, NR.Blue.copy(alpha = 0.18f), Color.Transparent))))
                Box(Modifier.padding(start = nowX).width(2.dp).fillMaxHeight().background(Brush.verticalGradient(listOf(NR.Blue, NR.Rose, NR.Amber))))
                Box(Modifier.padding(start = nowX - 4.dp, top = 2.dp).size(10.dp).clip(CircleShape).background(NR.Blue))
            }
        }
    }
}

private fun com.streamvault.domain.model.Program.progressFraction(now: Long): Float =
    if (endTime > startTime) ((now - startTime).toFloat() / (endTime - startTime)).coerceIn(0f, 1f) else 0f
