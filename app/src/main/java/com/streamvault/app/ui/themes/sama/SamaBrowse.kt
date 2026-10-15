package com.streamvault.app.ui.themes.sama

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

@Composable
internal fun <T> SamaLibrary(
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
    val nav = LocalSamaNavigate.current
    var sortIndex by remember(s.selectedSort) { mutableStateOf(LibrarySortBy.entries.indexOf(s.selectedSort)) }
    var focusedItem by remember { mutableStateOf<T?>(null) }
    val items = s.visibleItems
    val hero = focusedItem ?: items.firstOrNull { backdrop(it) != null } ?: items.firstOrNull()
    SamaLtrRow(Modifier.fillMaxSize(), spacing = 16.dp) { rtl ->
        rtl {
            Column(Modifier.width(200.dp).fillMaxHeight().cg2Panel().padding(8.dp)) {
                Row(Modifier.padding(bottom = 8.dp).fillMaxWidth().clip(RoundedCornerShape(50)).background(Color(0x4D000000)).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(true to tr("Movies", "الأفلام"), false to tr("Series", "المسلسلات")).forEach { (m, label) ->
                        SamaListRow(selected = m == isMovies, onClick = { if (m != isMovies) nav(if (m) Routes.MOVIES else Routes.SERIES) }, vertical = 7.dp, modifier = Modifier.weight(1f)) {
                            Text(label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    item {
                        SamaListRow(selected = s.selectedCategory == null, onClick = { p.onShowAll() }) {
                            CgGlyph("apps", 22.dp, tint = Color.White)
                            Text(tr("All", "الكل"), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("${s.libraryCount}", color = CG.Sub, fontSize = 13.sp)
                        }
                    }
                    items(s.categoryNames, key = { it }) { name ->
                        val cat = s.categoryFor(name)
                        val sel = name == s.selectedCategory
                        SamaListRow(selected = sel, onClick = { cat?.let(p.onCategoryClick) }, onLongClick = { cat?.let(p.onCategoryLongClick) }) {
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
                Box(Modifier.fillMaxWidth().height(170.dp).clip(RoundedCornerShape(12.dp)).background(CG.Raised)) {
                    androidx.compose.animation.Crossfade(hero?.let { backdrop(it) ?: image(it) }, label = "lh") { u -> u?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) } }
                    Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xF2080B0E), Color(0x99080B0E), Color.Transparent))))
                    Column(Modifier.align(Alignment.CenterStart).fillMaxWidth(0.55f).padding(horizontal = 28.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(hero?.let(title) ?: tr(kindEn, kindAr), color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            hero?.let { SamaRating(rating(it), 14) }
                            hero?.let { cg2QualityTags(quality(it)).forEach { t -> t() } }
                            hero?.let(caption)?.takeIf { it.isNotBlank() }?.let { Text(it, color = CG.Sub, fontSize = 13.sp, maxLines = 1) }
                        }
                        hero?.let(plot)?.takeIf { it.isNotBlank() }?.let { Text(it, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis) }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SamaButton(tr("Watch now", "مشاهدة الآن"), "▶", { hero?.let(p.onItemClick) })
                            SamaButton(tr("Details", "التفاصيل"), "info", { hero?.let(p.onItemClick) }, primary = false)
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text((s.selectedCategory ?: tr("All $kindEn", "جميع " + kindAr)) + "  (${s.libraryCount})", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.weight(1f))
                    SamaSearchBox(s.searchQuery, p.onQueryChange, tr("Search", "بحث"), Modifier.width(230.dp).focusRequester(p.initialFocusRequester))
                    SamaButton(tr("Sort: ", "الترتيب: ") + sortLabel(s.selectedSort), "sort", {
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
                                        SamaPosterCard(title(item), if (locked) null else image(item), caption(item),
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
internal fun SamaGridPoster(title: String, image: String?, sub: String?, rating: Float, onClick: () -> Unit, onLongClick: () -> Unit, modifier: Modifier = Modifier, quality: String? = null) {
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
                    SamaRating(rating, 11)
                }
            }
        }
    }
}

@Composable
internal fun SamaEpg(p: EpgParams, modifier: Modifier) {
    Column(modifier.fillMaxSize().padding(28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(tr("Live channels", "القنوات المباشرة"), color = CG.Text, fontSize = 28.sp)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(p.channels.size) { i ->
                val c = p.channels[i]
                CgButton(c.name, { p.onChannelClick(c) }, Modifier.fillMaxWidth())
            }
        }
    }
}
