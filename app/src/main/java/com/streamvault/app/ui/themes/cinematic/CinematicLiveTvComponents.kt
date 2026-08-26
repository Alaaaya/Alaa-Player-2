package com.streamvault.app.ui.themes.cinematic

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.streamvault.app.R
import com.streamvault.app.ui.components.PlayerRenderView
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.domain.model.Category
import com.streamvault.domain.model.Channel
import com.streamvault.player.PlayerEngine
import com.streamvault.player.PlayerRenderSurfaceType
import com.streamvault.player.PlayerSurfaceResizeMode

internal val CinematicCanvas = Color(0xFF08070B)
internal val CinematicPanel = Color(0xFF151016)
internal val CinematicPanelRaised = Color(0xFF261925)
internal val CinematicWine = Color(0xFFD74457)
internal val CinematicGold = Color(0xFFF0C98A)
internal val CinematicText = Color(0xFFF7F1F2)
internal val CinematicMuted = Color(0xFFB9ACB1)
internal val CinematicShape = RoundedCornerShape(18.dp)

@Composable
internal fun CinematicCategoryCard(
    category: Category,
    isSelected: Boolean,
    isLocked: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isFocused by remember(category.id) { mutableStateOf(false) }
    TvClickableSurface(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused },
        shape = ClickableSurfaceDefaults.shape(CinematicShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = when {
                isSelected -> CinematicWine.copy(alpha = 0.32f)
                else -> Color.Transparent
            },
            focusedContainerColor = CinematicPanelRaised,
            contentColor = if (isSelected) CinematicGold else CinematicMuted,
            focusedContentColor = CinematicText
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(2.dp, CinematicGold),
                shape = CinematicShape
            )
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = category.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = category.count.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = if (isFocused) CinematicGold else CinematicMuted
            )
            if (isLocked) {
                Text(
                    text = "LOCK",
                    style = MaterialTheme.typography.labelSmall,
                    color = CinematicGold
                )
            }
        }
    }
}

@Composable
internal fun CinematicChannelRow(
    channel: Channel,
    isLocked: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onFocused: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember(channel.id) { mutableStateOf(false) }
    val program = channel.currentProgram
    val shape = RoundedCornerShape(14.dp)

    TvClickableSurface(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged {
                isFocused = it.isFocused
                if (it.isFocused) onFocused()
            },
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = CinematicPanel.copy(alpha = 0.82f),
            focusedContainerColor = CinematicPanelRaised,
            contentColor = CinematicText,
            focusedContentColor = CinematicText
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(2.dp, CinematicWine),
                shape = shape
            )
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.018f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(82.dp)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = channel.number.takeIf { it > 0 }?.toString()?.padStart(2, '0') ?: "--",
                style = MaterialTheme.typography.titleMedium,
                color = CinematicGold,
                modifier = Modifier.width(34.dp),
                textAlign = TextAlign.End
            )
            Box(
                modifier = Modifier
                    .size(width = 66.dp, height = 48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CinematicCanvas),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = channel.logoUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "LIVE",
                        style = MaterialTheme.typography.labelSmall,
                        color = CinematicWine,
                        fontWeight = FontWeight.Black
                    )
                    if (channel.isFavorite) {
                        Text(
                            text = "SAVED",
                            style = MaterialTheme.typography.labelSmall,
                            color = CinematicGold
                        )
                    }
                }
                Text(
                    text = channel.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = program?.title ?: "No programme information",
                    style = MaterialTheme.typography.bodySmall,
                    color = CinematicMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (program != null) {
                    LinearProgressIndicator(
                        progress = { program.progressPercent() },
                        modifier = Modifier.fillMaxWidth().height(3.dp),
                        color = CinematicWine,
                        trackColor = Color.White.copy(alpha = 0.14f)
                    )
                }
            }
            if (isLocked) {
                Text(
                    text = "LOCK",
                    style = MaterialTheme.typography.labelSmall,
                    color = CinematicGold
                )
            }
        }
    }
}

@Composable
internal fun CinematicPreviewPanel(
    channel: Channel?,
    playerEngine: PlayerEngine?,
    isLoading: Boolean,
    errorMessage: String?,
    focusRequester: FocusRequester,
    onJumpToChannels: () -> Boolean,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val renderSurfaceType by (playerEngine?.renderSurfaceType)?.collectAsStateWithLifecycle(
        initialValue = PlayerRenderSurfaceType.SURFACE_VIEW
    ) ?: remember { mutableStateOf(PlayerRenderSurfaceType.SURFACE_VIEW) }
    val shape = RoundedCornerShape(24.dp)

    Surface(
        modifier = modifier
            .focusRequester(focusRequester)
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .onPreviewKeyEvent { event ->
                event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN &&
                    event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_RIGHT &&
                    onJumpToChannels()
            },
        shape = shape,
        colors = SurfaceDefaults.colors(containerColor = CinematicPanel),
        border = Border(
            border = BorderStroke(
                if (isFocused) 2.dp else 1.dp,
                if (isFocused) CinematicGold else CinematicWine.copy(alpha = 0.3f)
            ),
            shape = shape
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(
                        Brush.verticalGradient(listOf(Color.Black, CinematicCanvas)),
                        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (playerEngine != null && errorMessage == null) {
                    PlayerRenderView(
                        playerEngine = playerEngine,
                        resizeMode = PlayerSurfaceResizeMode.FIT,
                        surfaceType = renderSurfaceType,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = errorMessage ?: "Select a channel to begin",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CinematicMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
                if (isLoading) {
                    Text(
                        text = "PREPARING LIVE PREVIEW",
                        style = MaterialTheme.typography.labelMedium,
                        color = CinematicGold,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.68f), RoundedCornerShape(999.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = channel?.name ?: "LIVE SELECTION",
                    style = MaterialTheme.typography.headlineSmall,
                    color = CinematicText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                channel?.currentProgram?.let { programme ->
                    Text(
                        text = programme.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = CinematicGold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = programme.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = CinematicMuted,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                } ?: Text(
                    text = "Programme details will appear here when supplied by your EPG.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CinematicMuted
                )
                Text(
                    text = "PRESS RIGHT TO RETURN TO CHANNELS",
                    style = MaterialTheme.typography.labelSmall,
                    color = CinematicWine,
                    fontSize = 10.sp
                )
            }
        }
    }
}
