package com.streamvault.app.ui.themes.purplegalaxy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
internal fun PurpleGalaxyEpg(p: EpgParams, modifier: Modifier) {
    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(Modifier.fillMaxWidth().height(170.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            Box(Modifier.width(300.dp).fillMaxSize().clip(PG.Panel).background(PG.Deep)) {
                val engine = p.previewPlayerEngine
                if (engine != null) PlayerRenderView(engine, PlayerSurfaceResizeMode.FIT, Modifier.fillMaxSize())
                if (p.isPreviewLoading) Text("Aligning…", color = PG.Dust, modifier = Modifier.align(Alignment.Center))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                GalaxyLabel("Star map · ${p.selectedCategoryName}")
                val prog = p.focusedProgram
                GalaxyTitle(prog?.title ?: p.focusedChannel?.name ?: "Navigate the map", size = 24)
                prog?.let {
                    Text("${p.focusedChannel?.name.orEmpty()}  ·  ${formatClock(it.startTime)} – ${formatClock(it.endTime)}", color = PG.Comet, fontSize = 13.sp)
                    Text(it.description, color = PG.Dust, fontSize = 13.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                }
                if (p.isRefreshing) Text("Refreshing star data…", color = PG.Muted, fontSize = 12.sp)
            }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { GalaxyChip("✦ ${p.selectedCategoryName}", true, { p.onGuideInteract(); p.onOpenCategoryPicker() }) }
            item { GalaxyChip("Now", false, { p.onGuideInteract(); p.onJumpToNow() }) }
            item { GalaxyChip("Scan", false, { p.onGuideInteract(); p.onOpenSearch() }) }
            item { GalaxyChip("Options", false, { p.onGuideInteract(); p.onOpenOptions() }) }
        }
        Box(Modifier.weight(1f).fillMaxWidth().clip(PG.Panel).background(PG.Deep.copy(alpha = 0.55f))) {
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
    }
}

