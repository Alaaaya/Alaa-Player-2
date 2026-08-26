package com.streamvault.app.ui.themes.blueocean

/** Style contract: Blue Ocean EPG is a harbour signal bridge: vertical control mast, live viewing bay, and tide chart. */

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.app.ui.components.ChannelLogoBadge
import com.streamvault.app.ui.components.PlayerRenderView
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.app.ui.screens.epg.EpgGrid
import com.streamvault.app.ui.screens.epg.GuideDensity
import com.streamvault.app.ui.screens.epg.GuideNowProvider
import com.streamvault.app.ui.screens.epg.currentGuideNow
import com.streamvault.app.ui.theme.LocalThemePresentation
import com.streamvault.domain.model.Channel
import com.streamvault.domain.model.Program
import com.streamvault.player.PlayerEngine
import com.streamvault.player.PlayerRenderSurfaceType
import com.streamvault.player.PlayerSurfaceResizeMode

@Composable
internal fun BlueOceanEpgSurface(
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
    val surfaces = LocalThemePresentation.current.surfaces
    GuideNowProvider {
        Row(
            modifier = modifier
                .fillMaxSize()
                .background(surfaces.canvas)
                .padding(horizontal = 24.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BlueOceanGuideMast(
                selectedCategoryName = selectedCategoryName,
                onOpenCategoryPicker = onOpenCategoryPicker,
                onJumpToNow = onJumpToNow,
                onOpenSearch = onOpenSearch,
                onOpenOptions = onOpenOptions,
                onGuideInteract = onGuideInteract,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(216.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BlueOceanGuideViewingBay(
                    previewPlayerEngine = previewPlayerEngine,
                    isPreviewLoading = isPreviewLoading,
                    focusedChannel = focusedChannel,
                    focusedProgram = focusedProgram,
                    isRefreshing = isRefreshing
                )
                EpgGrid(
                    modifier = Modifier.weight(1f),
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
private fun BlueOceanGuideMast(
    selectedCategoryName: String,
    onOpenCategoryPicker: () -> Unit,
    onJumpToNow: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenOptions: () -> Unit,
    onGuideInteract: () -> Unit,
    modifier: Modifier = Modifier
) {
    val surfaces = LocalThemePresentation.current.surfaces
    val shape = RoundedCornerShape(topStart = 30.dp, bottomEnd = 30.dp, topEnd = 12.dp, bottomStart = 12.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(surfaces.browseContent)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Text("GUIDE / HARBOUR", style = MaterialTheme.typography.labelMedium, color = surfaces.accent)
        Text("TIDE BOARD", style = MaterialTheme.typography.headlineSmall, color = surfaces.textPrimary)
        Text("Set your course, then follow the live signal.", style = MaterialTheme.typography.bodySmall, color = surfaces.textSecondary)
        Spacer(Modifier.height(4.dp))
        BlueOceanGuidePort(selectedCategoryName, onOpenCategoryPicker, onGuideInteract)
        BlueOceanGuidePort("RETURN TO NOW", onJumpToNow, onGuideInteract)
        BlueOceanGuidePort("SCAN PROGRAMMES", onOpenSearch, onGuideInteract)
        BlueOceanGuidePort("TIDE CONTROLS", onOpenOptions, onGuideInteract)
        Spacer(Modifier.weight(1f))
        Text("SELECT: OPEN\nHOLD: PROGRAM FILE", style = MaterialTheme.typography.labelSmall, color = surfaces.textSecondary)
    }
}

@Composable
private fun BlueOceanGuidePort(label: String, onClick: () -> Unit, onFocused: () -> Unit) {
    val surfaces = LocalThemePresentation.current.surfaces
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(12.dp)
    TvClickableSurface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged {
                if (it.isFocused && !focused) onFocused()
                focused = it.isFocused
            },
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = surfaces.canvas,
            focusedContainerColor = surfaces.focusedSurface,
            contentColor = surfaces.textPrimary,
            focusedContentColor = surfaces.textPrimary
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(border = BorderStroke(2.dp, surfaces.accent), shape = shape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.015f)
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun BlueOceanGuideViewingBay(
    previewPlayerEngine: PlayerEngine?,
    isPreviewLoading: Boolean,
    focusedChannel: Channel?,
    focusedProgram: Program?,
    isRefreshing: Boolean
) {
    val surfaces = LocalThemePresentation.current.surfaces
    val renderSurfaceType by (previewPlayerEngine?.renderSurfaceType)?.collectAsStateWithLifecycle(
        initialValue = PlayerRenderSurfaceType.SURFACE_VIEW
    ) ?: remember { mutableStateOf(PlayerRenderSurfaceType.SURFACE_VIEW) }
    val now = currentGuideNow()
    val progress = focusedProgram?.let { program ->
        ((now - program.startTime).toFloat() / (program.endTime - program.startTime).coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)
    } ?: 0f
    val shape = RoundedCornerShape(topStart = 12.dp, topEnd = 28.dp, bottomEnd = 12.dp, bottomStart = 28.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(174.dp)
            .clip(shape)
            .background(surfaces.browseContent)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(252.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (previewPlayerEngine == null) {
                Text("SIGNAL STANDBY", style = MaterialTheme.typography.labelMedium, color = surfaces.textSecondary)
            } else {
                PlayerRenderView(
                    playerEngine = previewPlayerEngine,
                    resizeMode = PlayerSurfaceResizeMode.FIT,
                    surfaceType = renderSurfaceType,
                    modifier = Modifier.fillMaxSize()
                )
                if (isPreviewLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), color = surfaces.accent, strokeWidth = 2.dp)
                }
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("LIVE SIGNAL / NOW", style = MaterialTheme.typography.labelMedium, color = surfaces.accent)
            focusedChannel?.let { channel ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    ChannelLogoBadge(channelName = channel.name, logoUrl = channel.logoUrl, modifier = Modifier.size(34.dp))
                    Text(
                        text = if (channel.number > 0) "${channel.number}. ${channel.name}" else channel.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = surfaces.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } ?: Text("Choose a station from the tide chart", style = MaterialTheme.typography.titleMedium, color = surfaces.textPrimary)
            Text(
                focusedProgram?.title ?: "No programme information has reached this harbour.",
                style = MaterialTheme.typography.bodyLarge,
                color = surfaces.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = surfaces.accent,
                trackColor = surfaces.focusedSurface
            )
            if (isRefreshing) {
                Text("UPDATING TIDE DATA", style = MaterialTheme.typography.labelSmall, color = surfaces.accent)
            }
        }
    }
}
