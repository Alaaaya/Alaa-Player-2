package com.streamvault.app.ui.themes.techdashboard

import com.streamvault.app.ui.themes.bespoke.tr
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.streamvault.app.ui.components.PlayerRenderView
import com.streamvault.app.ui.model.guideLookupKey
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.streamvault.app.ui.themes.bespoke.EpgParams
import com.streamvault.app.ui.themes.bespoke.PlayerOverlayParams
import com.streamvault.domain.model.RecordingStatus
import com.streamvault.player.PlayerSurfaceResizeMode

/** Star map guide: telescope viewport and program log on top, transparent grid over the starfield. */
@Composable
internal fun TechDashEpg(p: EpgParams, modifier: Modifier) {
    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("EPG://" + p.selectedCategoryName.uppercase(), color = TD.Plasma, fontSize = 13.sp, fontFamily = TD.Mono, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            if (p.isRefreshing) Text("SYNC…", color = TD.Flare, fontSize = 11.sp, fontFamily = TD.Mono)
            Text("${formatClock(p.guideWindowStart)}–${formatClock(p.guideWindowEnd)}", color = TD.Muted, fontSize = 11.sp, fontFamily = TD.Mono)
            TechDashChip(tr("group", "المجموعة"), false, { p.onGuideInteract(); p.onOpenCategoryPicker() })
            TechDashChip(tr("now", "الآن"), true, { p.onGuideInteract(); p.onJumpToNow() })
            TechDashChip(tr("find", "بحث"), false, { p.onGuideInteract(); p.onOpenSearch() })
            TechDashChip(tr("opts", "خيارات"), false, { p.onGuideInteract(); p.onOpenOptions() })
        }
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TdModule(tr("Schedule", "جدول البرامج"), Modifier.weight(1f).fillMaxHeight(), meta = "${p.channels.size} ch") {
                TdEpgGrid(p)
            }
            Column(Modifier.width(320.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TdModule(tr("Monitor", "معاينة"), Modifier.fillMaxWidth().height(200.dp)) {
                    Box(Modifier.fillMaxSize().background(TD.Void)) {
                        val engine = p.previewPlayerEngine
                        if (engine != null) PlayerRenderView(engine, PlayerSurfaceResizeMode.FIT, Modifier.fillMaxSize())
                        if (p.isPreviewLoading) Text("BUFFER…", color = TD.Plasma, fontFamily = TD.Mono, fontSize = 12.sp, modifier = Modifier.align(Alignment.Center))
                    }
                }
                TdModule(tr("Selected", "المحدد"), Modifier.fillMaxWidth().weight(1f)) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(p.focusedChannel?.name ?: "--", color = TD.Plasma, fontSize = 12.sp, fontFamily = TD.Mono, maxLines = 1)
                        val prog = p.focusedProgram
                        Text(prog?.title ?: tr("Move through the grid", "تنقل في الجدول"), color = TD.Star, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 2)
                        prog?.let {
                            Text("${formatClock(it.startTime)} → ${formatClock(it.endTime)}", color = TD.Comet, fontSize = 12.sp, fontFamily = TD.Mono)
                            val now = System.currentTimeMillis()
                            if (now in it.startTime..it.endTime && it.endTime > it.startTime) TechDashProgress((now - it.startTime).toFloat() / (it.endTime - it.startTime), height = 3.dp)
                            Text(it.description, color = TD.Dust, fontSize = 12.sp, maxLines = 8, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}



/** Tech Dashboard's own guide: a mono timetable. Channel register column, a shared
 *  horizontally scrolled time axis with 30-min tick labels, and flat bordered cells. */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun TdEpgGrid(p: com.streamvault.app.ui.themes.bespoke.EpgParams) {
    val start = p.guideWindowStart
    val end = p.guideWindowEnd.coerceAtLeast(start + 60_000)
    val dpPerMin = when (p.density.name) { "COMPACT" -> 4f; "CINEMATIC" -> 6f; else -> 5f }
    val rowH = when (p.density.name) { "COMPACT" -> 34.dp; "CINEMATIC" -> 48.dp; else -> 40.dp }
    val totalW = (((end - start) / 60_000f) * dpPerMin).dp
    val hs = rememberScrollState()
    Column(Modifier.fillMaxSize()) {
        Row {
            Text("CH", color = TD.Muted, fontSize = 10.sp, fontFamily = TD.Mono, modifier = Modifier.width(150.dp))
            Box(Modifier.weight(1f).horizontalScroll(hs)) {
                Box(Modifier.width(totalW).height(18.dp)) {
                    var tick = start - (start % 1_800_000) + 1_800_000
                    while (tick < end) {
                        val x = (((tick - start) / 60_000f) * dpPerMin).dp
                        Text("|" + formatClock(tick), color = TD.Plasma.copy(alpha = 0.7f), fontSize = 10.sp, fontFamily = TD.Mono, modifier = Modifier.padding(start = x))
                        tick += 1_800_000
                    }
                }
            }
        }
        LazyColumn(Modifier.fillMaxSize()) {
            itemsIndexed(p.channels, key = { _, c -> c.id }) { index, c ->
                if (index >= p.channels.size - 15) androidx.compose.runtime.LaunchedEffect(p.channels.size) { p.onRequestMoreChannels() }
                val programs = c.guideLookupKey()?.let { p.programsByChannel[it] }.orEmpty()
                val now = System.currentTimeMillis()
                val current = programs.firstOrNull { it.startTime <= now && it.endTime > now }
                val first = index == 0
                Row(Modifier.fillMaxWidth().height(rowH).border(0.5.dp, TD.Plasma.copy(alpha = 0.12f)), verticalAlignment = Alignment.CenterVertically) {
                    var chFocus by remember { mutableStateOf(false) }
                    Row(
                        Modifier.width(150.dp).fillMaxHeight().background(if (chFocus) TD.Plasma else Color.Transparent)
                            .onFocusChanged { chFocus = it.isFocused; if (it.isFocused) p.onChannelFocused(c, current, first) }
                            .combinedClickable(onClick = { p.onChannelClick(c) }, onLongClick = p.onChannelLongClick?.let { l -> { l(c, current) } })
                            .padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(if (c.id in p.favoriteChannelIds) "★" else " ", color = TD.Flare, fontSize = 11.sp)
                        Text(c.name, color = if (chFocus) TD.Void else TD.Star, fontSize = 12.sp, fontFamily = TD.Mono, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Box(Modifier.weight(1f).fillMaxHeight().horizontalScroll(hs)) {
                        Box(Modifier.width(totalW).fillMaxHeight()) {
                            if (programs.isEmpty()) Text("-- no data --", color = TD.Muted, fontSize = 11.sp, fontFamily = TD.Mono, modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp))
                            programs.filter { it.endTime > start && it.startTime < end }.forEach { prog ->
                                val s = maxOf(prog.startTime, start); val e = minOf(prog.endTime, end)
                                val x = (((s - start) / 60_000f) * dpPerMin).dp
                                val w = ((((e - s) / 60_000f) * dpPerMin).dp - 2.dp).coerceAtLeast(6.dp)
                                var f by remember { mutableStateOf(false) }
                                val live = prog.startTime <= now && prog.endTime > now
                                Box(
                                    Modifier.padding(start = x, top = 3.dp, bottom = 3.dp).width(w).fillMaxHeight()
                                        .background(if (f) TD.Plasma else if (live) TD.Plasma.copy(alpha = 0.14f) else Color.Transparent)
                                        .border(1.dp, if (f) TD.Plasma else TD.Plasma.copy(alpha = 0.3f))
                                        .onFocusChanged { f = it.isFocused; if (it.isFocused) p.onProgramFocused(c, prog, first) }
                                        .combinedClickable(onClick = { p.onProgramClick(c, prog) }, onLongClick = p.onChannelLongClick?.let { l -> { l(c, prog) } })
                                        .padding(horizontal = 5.dp)
                                ) {
                                    Text((if (live) "> " else "") + prog.title, color = if (f) TD.Void else TD.Star, fontSize = 11.sp, fontFamily = TD.Mono, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.align(Alignment.CenterStart))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
