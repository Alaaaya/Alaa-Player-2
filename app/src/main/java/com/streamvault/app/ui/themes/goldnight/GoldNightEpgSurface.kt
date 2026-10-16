package com.streamvault.app.ui.themes.goldnight

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.app.ui.components.PlayerRenderView
import com.streamvault.app.ui.screens.epg.EpgGrid
import com.streamvault.app.ui.screens.epg.GuideNowProvider
import com.streamvault.app.ui.screens.epg.currentGuideNow
import com.streamvault.domain.model.Channel
import com.streamvault.app.ui.screens.epg.GuideDensity
import com.streamvault.domain.model.Program
import com.streamvault.player.PlayerEngine
import com.streamvault.player.PlayerRenderSurfaceType
import com.streamvault.player.PlayerSurfaceResizeMode

/**
 * Gold Night EPG: لوحة علوية (معاينة + البرنامج المركّز + أدوات الدليل)، ثم الدليل المشترك:
 * عمود قنوات ثابت عمودي مع خط زمني للبرامج وخط «الآن» ذهبي.
 */
@Composable
internal fun GoldNightEpgSurface(
    selectedCategoryName: String,
    previewPlayerEngine: PlayerEngine?,
    isPreviewLoading: Boolean,
    focusedChannel: Channel?,
    focusedProgram: Program?,
    isRefreshing: Boolean,
    channels: List<Channel>,
    favoriteChannelIds: Set<Long>,
    programsByChannel: Map<String, List<Program>>,
    guideWindowStart: Long,
    guideWindowEnd: Long,
    density: GuideDensity,
    onOpenCategoryPicker: () -> Unit,
    onJumpToNow: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenOptions: () -> Unit,
    onGuideInteract: () -> Unit,
    onChannelClick: (Channel) -> Unit,
    onChannelLongClick: (Channel, Program?) -> Unit,
    onProgramClick: (Channel, Program) -> Unit,
    onChannelFocused: (Channel, Program?, Boolean) -> Unit,
    onProgramFocused: (Channel, Program, Boolean) -> Unit,
    onRequestMoreChannels: () -> Unit,
    modifier: Modifier = Modifier
) {
    GuideNowProvider {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(GoldNightCanvasBrush)
                .padding(horizontal = 22.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GoldNightScreenHeader(title = "TV Guide", breadcrumb = selectedCategoryName)
            Row(
                modifier = Modifier.fillMaxWidth().height(168.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GoldNightGuidePreview(
                    engine = previewPlayerEngine,
                    loading = isPreviewLoading,
                    modifier = Modifier.fillMaxHeight().aspectRatio(16f / 9f)
                )
                GoldNightGuideProgramCard(
                    channel = focusedChannel,
                    program = focusedProgram,
                    isRefreshing = isRefreshing,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                Column(
                    modifier = Modifier.width(220.dp).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GoldNightGuideControl(label = selectedCategoryName, onClick = onOpenCategoryPicker, onFocused = onGuideInteract)
                    GoldNightGuideControl(label = "Now", onClick = onJumpToNow, onFocused = onGuideInteract)
                    GoldNightGuideControl(label = "Search programmes", onClick = onOpenSearch, onFocused = onGuideInteract)
                    GoldNightGuideControl(label = "Guide options", onClick = onOpenOptions, onFocused = onGuideInteract)
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GoldNightPanel.copy(alpha = 0.55f))
            ) {
                EpgGrid(
                    modifier = Modifier.fillMaxSize(),
                    channels = channels,
                    favoriteChannelIds = favoriteChannelIds,
                    programsByChannel = programsByChannel,
                    guideWindowStart = guideWindowStart,
                    guideWindowEnd = guideWindowEnd,
                    density = density,
                    transparentOverlay = true,
                    onChannelClick = onChannelClick,
                    onChannelLongClick = onChannelLongClick,
                    onProgramClick = onProgramClick,
                    onChannelFocused = onChannelFocused,
                    onProgramFocused = onProgramFocused,
                    onRequestMoreChannels = onRequestMoreChannels
                )
            }
        }
    }
}

@Composable
private fun GoldNightGuidePreview(
    engine: PlayerEngine?,
    loading: Boolean,
    modifier: Modifier = Modifier
) {
    val renderSurfaceType by (engine?.renderSurfaceType)?.collectAsStateWithLifecycle(
        initialValue = PlayerRenderSurfaceType.SURFACE_VIEW
    ) ?: remember { mutableStateOf(PlayerRenderSurfaceType.SURFACE_VIEW) }
    Box(
        modifier = modifier.clip(RoundedCornerShape(12.dp)).background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (engine == null) {
            Text(text = "Preview", style = MaterialTheme.typography.labelLarge, color = GoldNightMuted)
        } else {
            PlayerRenderView(
                playerEngine = engine,
                resizeMode = PlayerSurfaceResizeMode.FIT,
                surfaceType = renderSurfaceType,
                modifier = Modifier.fillMaxSize()
            )
            if (loading) Text(text = "Loading…", style = MaterialTheme.typography.labelLarge, color = GoldNightGold)
        }
    }
}

@Composable
private fun GoldNightGuideProgramCard(
    channel: Channel?,
    program: Program?,
    isRefreshing: Boolean,
    modifier: Modifier = Modifier
) {
    val now = currentGuideNow()
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(GoldNightPanel)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (channel != null) GoldNightRoundLogo(channelName = channel.name, logoUrl = channel.logoUrl, size = 34.dp)
            Text(
                text = channel?.let { if (it.number > 0) "${it.number}  ${it.name}" else it.name } ?: "Choose a channel in the guide",
                style = MaterialTheme.typography.titleMedium,
                color = GoldNightText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (isRefreshing) Text(text = "Updating…", style = MaterialTheme.typography.labelSmall, color = GoldNightGold)
        }
        Text(
            text = program?.title ?: "No programme information",
            style = MaterialTheme.typography.titleLarge,
            color = GoldNightText,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (program != null) {
            Text(text = goldNightProgramWindow(program), style = MaterialTheme.typography.labelMedium, color = GoldNightGold)
            GoldNightProgressBar(progress = goldNightProgramProgress(program, now), modifier = Modifier.fillMaxWidth(), height = 4.dp)
            if (program.description.isNotBlank()) {
                Text(
                    text = program.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = GoldNightMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun GoldNightGuideControl(
    label: String,
    onClick: () -> Unit,
    onFocused: () -> Unit,
    modifier: Modifier = Modifier
) {
    var focused by remember { mutableStateOf(false) }
    GoldNightCard(
        onClick = onClick,
        cornerRadius = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged {
                if (it.isFocused && !focused) onFocused()
                focused = it.isFocused
            }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
        )
    }
}
