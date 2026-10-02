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
import com.streamvault.app.ui.screens.epg.EpgGrid
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
                EpgGrid(
                    modifier = Modifier.fillMaxSize(),
                    channels = p.channels,
                    favoriteChannelIds = p.favoriteChannelIds,
                    programsByChannel = p.programsByChannel,
                    guideWindowStart = p.guideWindowStart,
                    guideWindowEnd = p.guideWindowEnd,
                    density = p.density,
                    transparentOverlay = true,
                    onChannelClick = p.onChannelClick,
                    onChannelLongClick = p.onChannelLongClick,
                    onProgramClick = p.onProgramClick,
                    onChannelFocused = p.onChannelFocused,
                    onProgramFocused = p.onProgramFocused,
                    onRequestMoreChannels = p.onRequestMoreChannels
                )
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

