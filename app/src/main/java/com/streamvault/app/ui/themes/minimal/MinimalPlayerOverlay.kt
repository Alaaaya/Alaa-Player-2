package com.streamvault.app.ui.themes.minimal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.domain.model.Channel
import com.streamvault.domain.model.Program

private data class MinimalPlayerAction(val label: String, val onClick: () -> Unit)

/** Minimal fullscreen player: quiet central transport plus a monochrome text action strip. */
@Composable
internal fun MinimalPlayerOverlay(
    visible: Boolean,
    title: String,
    isPlaying: Boolean,
    currentProgram: Program?,
    currentChannel: Channel?,
    displayChannelNumber: Int,
    playButtonFocusRequester: FocusRequester,
    quickActionsFocusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekBackward: () -> Unit,
    onSeekForward: () -> Unit,
    onOpenArchive: () -> Unit,
    onOpenSubtitleTracks: () -> Unit,
    onOpenAudioTracks: () -> Unit,
    onOpenVideoTracks: () -> Unit,
    onToggleAspectRatio: () -> Unit,
    onToggleMute: () -> Unit,
    onSeekToLiveEdge: () -> Unit,
    onUserInteraction: () -> Unit
) {
    val actions = listOf(
        MinimalPlayerAction("Guide", onOpenArchive),
        MinimalPlayerAction("Audio", onOpenAudioTracks),
        MinimalPlayerAction("Subtitles", onOpenSubtitleTracks),
        MinimalPlayerAction("Quality", onOpenVideoTracks),
        MinimalPlayerAction("Display", onToggleAspectRatio),
        MinimalPlayerAction("Mute", onToggleMute),
        MinimalPlayerAction("Live", onSeekToLiveEdge),
        MinimalPlayerAction("Close", onClose)
    )
    AnimatedVisibility(visible = visible, enter = fadeIn(tween(MinimalFocusMotionMs)), exit = fadeOut(tween(MinimalFocusMotionMs)), modifier = modifier) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = .38f)).onPreviewKeyEvent { event ->
                if (event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN) onUserInteraction()
                false
            }
        ) {
            Column(modifier = Modifier.align(Alignment.TopStart).padding(30.dp).width(460.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("LIVE ${displayChannelNumber.toString().padStart(3, '0')}", style = MaterialTheme.typography.labelMedium, color = MinimalMuted)
                Text(currentProgram?.title ?: title, style = MaterialTheme.typography.titleLarge, color = MinimalText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(currentChannel?.name ?: "", style = MaterialTheme.typography.bodySmall, color = MinimalMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Row(modifier = Modifier.align(Alignment.Center), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                MinimalPlayerButton("−10", onSeekBackward)
                MinimalPlayerButton(if (isPlaying) "Pause" else "Play", onTogglePlayPause, Modifier.focusRequester(playButtonFocusRequester))
                MinimalPlayerButton("+10", onSeekForward)
            }
            MinimalBottomActionStrip(actions, quickActionsFocusRequester, Modifier.align(Alignment.BottomCenter).padding(start = 34.dp, end = 34.dp, bottom = 26.dp))
        }
    }
}

/** Fullscreen-only bottom bar: a single thin, transparent typographic strip — not capsules or tiles. */
@Composable
private fun MinimalBottomActionStrip(actions: List<MinimalPlayerAction>, focusRequester: FocusRequester, modifier: Modifier) {
    LazyRow(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        items(actions, key = { it.label }) { action -> MinimalPlayerButton(action.label, action.onClick, Modifier.focusRequester(focusRequester)) }
    }
}

@Composable
private fun MinimalPlayerButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(0.dp)
    TvClickableSurface(onClick = onClick, modifier = modifier, shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = Color.Transparent, focusedContainerColor = MinimalPaper.copy(alpha = .88f), contentColor = MinimalText, focusedContentColor = MinimalText), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(1.dp, MinimalFocus), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = MinimalFocusedScale)) {
        Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), style = MaterialTheme.typography.labelMedium, maxLines = 1)
    }
}
