package com.streamvault.app.ui.themes.kat

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
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
import androidx.compose.foundation.shape.RoundedCornerShape
import com.streamvault.app.navigation.Routes
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
internal fun <T> KatLibrary(
    kindEn: String, kindAr: String,
    p: LibraryParams<T>,
    key: (T) -> Long,
    title: (T) -> String,
    image: (T) -> String?,
    caption: (T) -> String?,
    rating: (T) -> Float = { 0f },
    backdrop: (T) -> String? = { null },
    plot: (T) -> String? = { null },
    isMovies: Boolean = true,
    quality: (T) -> String? = { null }
) {
    val s = p.uiState
    val nav = LocalKatNavigate.current
    var sortIndex by remember(s.selectedSort) { mutableStateOf(LibrarySortBy.entries.indexOf(s.selectedSort)) }
    var focusedItem by remember { mutableStateOf<T?>(null) }
    val items = s.visibleItems
    val hero = focusedItem ?: items.firstOrNull { backdrop(it) != null } ?: items.firstOrNull()
    KatLtrRow(Modifier.fillMaxSize(), spacing = 16.dp) { rtl ->
        rtl {
            Column(Modifier.width(250.dp).fillMaxHeight().cg2Panel().padding(8.dp)) {
                Row(Modifier.padding(bottom = 8.dp).fillMaxWidth().clip(RoundedCornerShape(50)).background(Color(0x4D000000)).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(true to tr("Movies", "الأفلام"), false to tr("Series", "المسلسلات")).forEach { (m, label) ->
                        KatListRow(selected = m == isMovies, onClick = { if (m != isMovies) nav(if (m) Routes.MOVIES else Routes.SERIES) }, vertical = 7.dp, modifier = Modifier.weight(1f)) {
                            Text(label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    item {
                        KatListRow(selected = s.selectedCategory == null, onClick = { p.onShowAll() }) {
                            CgGlyph("apps", 22.dp, tint = Color.White)
                            Text(tr("All", "الكل"), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("${s.libraryCount}", color = CG.Sub, fontSize = 13.sp)
                        }
                    }
                    items(s.categoryNames, key = { it }) { name ->
                        val cat = s.categoryFor(name)
                        val sel = name == s.selectedCategory
                        KatListRow(selected = sel, onClick = { cat?.let(p.onCategoryClick) }, onLongClick = { cat?.let(p.onCategoryLongClick) }) {
                            CgCategoryGlyph(name, cat?.let(p.isCategoryLocked) == true, Color.White, 22.dp)
                            Text(name, color = Color.White, fontSize = 14.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            s.categoryCounts[name]?.let { Text("$it", color = CG.Sub, fontSize = 12.sp) }
                            CgGlyph("prev", 16.dp, tint = CG.Faint)
                        }
                    }
                }
            }
        }
        rtl {
            Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(24.dp)).background(CG.Raised)) {
                    androidx.compose.animation.Crossfade(hero?.let { backdrop(it) ?: image(it) }, label = "lh") { u -> u?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) } }
                    Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xF207071A), Color(0x9907071A), Color.Transparent))))
                    Column(Modifier.align(Alignment.CenterStart).fillMaxWidth(0.55f).padding(horizontal = 28.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(hero?.let(title) ?: tr(kindEn, kindAr), color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            hero?.let { KatRating(rating(it), 14) }
                            hero?.let { cg2QualityTags(quality(it)).forEach { t -> t() } }
                            hero?.let(caption)?.takeIf { it.isNotBlank() }?.let { Text(it, color = CG.Sub, fontSize = 13.sp, maxLines = 1) }
                        }
                        hero?.let(plot)?.takeIf { it.isNotBlank() }?.let { Text(it, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis) }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            KatButton(tr("Watch now", "مشاهدة الآن"), "▶", { hero?.let(p.onItemClick) })
                            KatButton(tr("More info", "مزيد من المعلومات"), "info", { hero?.let(p.onItemClick) }, primary = false)
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text((s.selectedCategory ?: tr("All $kindEn", "جميع " + kindAr)) + "  (${s.libraryCount})", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.weight(1f))
                    KatSearchBox(s.searchQuery, p.onQueryChange, tr("Search", "بحث"), Modifier.width(230.dp).focusRequester(p.initialFocusRequester))
                    KatButton(tr("Sort: ", "الترتيب: ") + sortLabel(s.selectedSort), "sort", {
                        sortIndex = (sortIndex + 1) % LibrarySortBy.entries.size
                        p.onSortChange(LibrarySortBy.entries[sortIndex])
                    }, primary = false)
                }
                Box(Modifier.fillMaxSize()) {
                    if (items.isEmpty() && !s.isLoadingSelectedCategory) CgEmpty(tr("Nothing here yet", "لا يوجد محتوى هنا"))
                    val shelves: List<Pair<String, List<T>>> = remember(items, s.itemsByCategory, s.selectedCategory, s.searchQuery) {
                        if (s.selectedCategory == null && s.searchQuery.isBlank() && s.itemsByCategory.isNotEmpty())
                            s.categoryNames.mapNotNull { n -> s.itemsByCategory[n]?.takeIf { it.isNotEmpty() }?.let { n to it } }
                        else items.chunked(8).mapIndexed { i, c -> (if (i == 0) (s.selectedCategory ?: "") else "") to c }
                    }
                    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(top = 4.dp, bottom = 60.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(shelves.size, key = { "sh" + it }) { si ->
                            val (name, row) = shelves[si]
                            if (si >= shelves.size - 2) LaunchedEffect(shelves.size) {
                                if (s.selectedCategory != null && s.canLoadMoreSelectedCategory) p.onLoadMoreSelected()
                                else if (s.selectedCategory == null && s.hasMorePreviewRows) p.onLoadMorePreview()
                            }
                            Column {
                                if (name.isNotBlank()) Row(Modifier.padding(start = 6.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Box(Modifier.size(4.dp, 18.dp).clip(CircleShape).background(Brush.verticalGradient(listOf(CG.Amber, CG.Violet))))
                                    Text(name, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    s.categoryCounts[name]?.let { Text(it.toString(), color = CG.Faint, fontSize = 13.sp) }
                                }
                                androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp)) {
                                    items(row.size, key = { key(row[it]) }) { i ->
                                        val item = row[i]
                                        val locked = p.isItemLocked(item)
                                        KatPosterCard(title(item), if (locked) null else image(item), caption(item),
                                            Modifier.width(148.dp).onFocusChanged { if (it.hasFocus) focusedItem = item }, onLongClick = { p.onItemLongClick(item) }) { p.onItemClick(item) }
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

@Composable
internal fun KatGridPoster(title: String, image: String?, sub: String?, rating: Float, onClick: () -> Unit, onLongClick: () -> Unit, modifier: Modifier = Modifier, quality: String? = null) {
    CgCard(onClick = onClick, onLongClick = onLongClick, container = CG.Glass, zoom = 1.06f, shape = RoundedCornerShape(18.dp), modifier = modifier) {
        Column {
            Box(Modifier.fillMaxWidth().aspectRatio(0.7f).background(CG.Raised)) {
                if (image != null) AsyncImage(image, title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                else Text(title.take(1), color = CG.Amber, fontSize = 34.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.Center))
                cg2QualityTags(quality).takeIf { it.isNotEmpty() }?.let { tags -> Row(Modifier.align(Alignment.TopStart).padding(6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) { tags.forEach { it() } } }
            }
            Column(Modifier.fillMaxWidth().padding(8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(sub ?: "", color = CG.Sub, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    KatRating(rating, 11)
                }
            }
        }
    }
}

@Composable
internal fun KatEpg(p: EpgParams, modifier: Modifier) {
    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            CgGlyph("guide", 28.dp, tint = CG.Amber); Spacer(Modifier.width(10.dp))
            Text(tr("Programme guide", "دليل البرامج"), color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(end = 18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KatPill(p.selectedCategoryName, true) { p.onGuideInteract(); p.onOpenCategoryPicker() }
                KatPill(tr("Now", "الآن"), false) { p.onGuideInteract(); p.onJumpToNow() }
                KatPill(tr("Search", "بحث"), false) { p.onGuideInteract(); p.onOpenSearch() }
                KatPill(tr("Options", "خيارات"), false) { p.onGuideInteract(); p.onOpenOptions() }
            }
            Spacer(Modifier.weight(1f))
            if (p.isRefreshing) CgBadge(tr("Updating", "تحديث"), CG.Blue)
            Text("  ${p.channels.size} " + tr("channels", "قناة"), color = CG.Faint, fontSize = 12.sp)
        }
        Box(Modifier.weight(1f).fillMaxWidth()) { CgEpgGrid(p) }
        Row(Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(18.dp)).background(Brush.horizontalGradient(listOf(Color(0xFF2A1A40), Color(0xFF13132E)))).border(1.dp, Color(0x66FFB547), RoundedCornerShape(18.dp)).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            Box(Modifier.aspectRatio(16f / 9f).fillMaxHeight().background(Color.Black).border(1.dp, CG.Line)) {
                p.previewPlayerEngine?.let { PlayerRenderView(it, PlayerSurfaceResizeMode.FIT, Modifier.fillMaxSize()) }
                if (p.isPreviewLoading) Text(tr("Tuning…", "جار الضبط…"), color = CG.Sub, fontFamily = CG.Serif, modifier = Modifier.align(Alignment.Center))
            }
            val prog = p.focusedProgram
            Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(p.focusedChannel?.name ?: p.selectedCategoryName, color = CG.Amber, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    val n = System.currentTimeMillis()
                    if (prog != null && prog.startTime <= n && prog.endTime > n) KatTag("LIVE", bg = CG.Live, fg = Color.White)
                }
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
                            Text(c.name, color = if (chFocus) Color.White else CG.Text, fontSize = 13.sp, fontFamily = CG.Serif, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (c.number > 0) Text("${c.number}", color = if (chFocus) Color.White.copy(alpha = 0.8f) else CG.Sub, fontSize = 15.sp, fontWeight = FontWeight.Bold)
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
                                        .background(if (f) CG.Amber else if (live) Color(0xFF2E2448) else CG.Raised)
                                        .border(1.dp, if (live && !f) CG.Amber.copy(alpha = 0.6f) else CG.Line.copy(alpha = 0.6f), CG.RSmall)
                                        .onFocusChanged { f = it.isFocused; if (it.isFocused) p.onProgramFocused(c, prog, first) }
                                        .combinedClickable(onClick = { p.onProgramClick(c, prog) }, onLongClick = { p.onChannelLongClick(c, prog) })
                                        .padding(horizontal = 10.dp)
                                ) {
                                    Column(Modifier.align(Alignment.CenterStart)) {
                                        Text(prog.title, color = if (f) Color.White else CG.Text, fontSize = 13.sp, fontFamily = CG.Serif, fontWeight = if (live) FontWeight.Bold else FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        if (rowH >= 58.dp) Text("${cgClock(prog.startTime)} – ${cgClock(prog.endTime)}", color = if (f) Color.White.copy(alpha = 0.75f) else CG.Faint, fontSize = 11.sp, maxLines = 1)
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
