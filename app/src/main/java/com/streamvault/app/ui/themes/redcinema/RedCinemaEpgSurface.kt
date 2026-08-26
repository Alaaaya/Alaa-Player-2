package com.streamvault.app.ui.themes.redcinema

/** Style contract: Red Cinema EPG is a theatre programme board: act stubs, a central stage preview, and a screened timeline. */

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
internal fun RedCinemaEpgSurface(
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
    val s = LocalThemePresentation.current.surfaces
    GuideNowProvider {
        Row(
            modifier = modifier.fillMaxSize().background(s.canvas).padding(28.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RedCinemaActIndex(
                selectedCategoryName = selectedCategoryName,
                onOpenCategoryPicker = onOpenCategoryPicker,
                onJumpToNow = onJumpToNow,
                onOpenSearch = onOpenSearch,
                onOpenOptions = onOpenOptions,
                onGuideInteract = onGuideInteract,
                modifier = Modifier.width(230.dp).fillMaxHeight()
            )
            Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                RedCinemaStagePreview(previewPlayerEngine, isPreviewLoading, focusedChannel, focusedProgram, isRefreshing)
                EpgGrid(
                    modifier = Modifier.weight(1f),
                    channels = channels,
                    favoriteChannelIds = favoriteChannelIds,
                    programsByChannel = programsByChannel,
                    guideWindowStart = guideWindowStart,
                    guideWindowEnd = guideWindowEnd,
                    density = density,
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
private fun RedCinemaActIndex(
    selectedCategoryName: String,
    onOpenCategoryPicker: () -> Unit,
    onJumpToNow: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenOptions: () -> Unit,
    onGuideInteract: () -> Unit,
    modifier: Modifier
) {
    val s = LocalThemePresentation.current.surfaces
    Column(
        modifier = modifier.background(s.browseContent, RoundedCornerShape(2.dp)).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text("TONIGHT'S PROGRAMME", style = MaterialTheme.typography.labelMedium, color = s.accent)
        Text("ACT INDEX", style = MaterialTheme.typography.headlineSmall, color = s.textPrimary)
        Text("Choose a playbill, then take your seat.", style = MaterialTheme.typography.bodySmall, color = s.textSecondary)
        Spacer(Modifier.height(4.dp))
        RedCinemaActStub("01", selectedCategoryName, onOpenCategoryPicker, onGuideInteract)
        RedCinemaActStub("02", "CURTAIN / NOW", onJumpToNow, onGuideInteract)
        RedCinemaActStub("03", "ARCHIVE OFFICE", onOpenSearch, onGuideInteract)
        RedCinemaActStub("04", "HOUSE CONTROLS", onOpenOptions, onGuideInteract)
        Spacer(Modifier.weight(1f))
        Text("SELECT: OPEN\nHOLD: PROGRAMME FILE", style = MaterialTheme.typography.labelSmall, color = s.textSecondary)
    }
}

@Composable
private fun RedCinemaActStub(number: String, label: String, onClick: () -> Unit, onFocused: () -> Unit) {
    val s = LocalThemePresentation.current.surfaces
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(1.dp)
    TvClickableSurface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().onFocusChanged {
            if (it.isFocused && !focused) onFocused()
            focused = it.isFocused
        },
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = s.canvas,
            focusedContainerColor = s.focusedSurface,
            contentColor = s.textPrimary,
            focusedContentColor = s.textPrimary
        ),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, s.accent), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.01f)
    ) {
        Row(Modifier.padding(11.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(number, style = MaterialTheme.typography.labelMedium, color = s.accent)
            Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun RedCinemaStagePreview(
    previewPlayerEngine: PlayerEngine?,
    isPreviewLoading: Boolean,
    focusedChannel: Channel?,
    focusedProgram: Program?,
    isRefreshing: Boolean
) {
    val s = LocalThemePresentation.current.surfaces
    val surfaceType by (previewPlayerEngine?.renderSurfaceType)?.collectAsStateWithLifecycle(
        initialValue = PlayerRenderSurfaceType.SURFACE_VIEW
    ) ?: remember { mutableStateOf(PlayerRenderSurfaceType.SURFACE_VIEW) }
    val now = currentGuideNow()
    val progress = focusedProgram?.let { p -> ((now - p.startTime).toFloat() / (p.endTime - p.startTime).coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f) } ?: 0f
    Row(
        modifier = Modifier.fillMaxWidth().height(182.dp).background(s.browseContent, RoundedCornerShape(2.dp)).padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.fillMaxHeight().width(278.dp).clip(RoundedCornerShape(1.dp)).background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (previewPlayerEngine == null) {
                Text("STAGE DARK", style = MaterialTheme.typography.labelMedium, color = s.textSecondary)
            } else {
                PlayerRenderView(
                    playerEngine = previewPlayerEngine,
                    resizeMode = PlayerSurfaceResizeMode.FIT,
                    surfaceType = surfaceType,
                    modifier = Modifier.fillMaxSize()
                )
                if (isPreviewLoading) CircularProgressIndicator(modifier = Modifier.size(28.dp), color = s.accent, strokeWidth = 2.dp)
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("NOW SHOWING", style = MaterialTheme.typography.labelMedium, color = s.accent)
            focusedChannel?.let { channel ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    ChannelLogoBadge(channel.name, channel.logoUrl, Modifier.size(34.dp))
                    Text(if (channel.number > 0) "${channel.number}. ${channel.name}" else channel.name, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            } ?: Text("Awaiting a stage selection", style = MaterialTheme.typography.titleMedium, color = s.textPrimary)
            Text(focusedProgram?.title ?: "No programme has been posted for this stage.", style = MaterialTheme.typography.bodyLarge, color = s.textSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(4.dp), color = s.accent, trackColor = s.focusedSurface)
            if (isRefreshing) Text("UPDATING THE PLAYBILL", style = MaterialTheme.typography.labelSmall, color = s.accent)
        }
    }
}
