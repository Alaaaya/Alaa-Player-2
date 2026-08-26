package com.streamvault.app.ui.themes.glass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.streamvault.app.ui.components.PlayerRenderView
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.domain.model.Category
import com.streamvault.domain.model.Channel
import com.streamvault.player.PlayerEngine
import com.streamvault.player.PlayerRenderSurfaceType
import com.streamvault.player.PlayerSurfaceResizeMode

/** Glass Live TV: preview وراء الواجهة، قناة عمودية وسط، وفئات bottom-sheet أفقية. */
@Composable
internal fun GlassmorphismLiveTvLayout(
    sourceTitle: String,
    categories: List<Category>,
    selectedCategoryId: Long?,
    categorySearchQuery: String,
    channelSearchQuery: String,
    channels: List<Channel>,
    previewChannel: Channel?,
    previewPlayerEngine: PlayerEngine?,
    isPreviewLoading: Boolean,
    previewErrorMessage: String?,
    isCategoryLocked: (Category) -> Boolean,
    isChannelLocked: (Channel) -> Boolean,
    categoryFocusRequesters: MutableMap<Long, FocusRequester>,
    channelFocusRequesters: MutableMap<Long, FocusRequester>,
    previewFocusRequester: FocusRequester,
    onCategorySearchChange: (String) -> Unit,
    onChannelSearchChange: (String) -> Unit,
    onCategoryClick: (Category) -> Unit,
    onCategoryLongClick: (Category) -> Unit,
    onChannelClick: (Channel) -> Unit,
    onChannelLongClick: (Channel) -> Unit,
    onCategoryFocused: (Category) -> Unit,
    onChannelFocused: (Channel) -> Unit,
    onRequestChannelsFromCategory: () -> Boolean,
    onRequestPreviewFromChannel: () -> Boolean,
    onRequestChannelsFromPreview: () -> Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().background(GlassCanvas).padding(26.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        GlassPreviewPane(
            channel = previewChannel,
            playerEngine = previewPlayerEngine,
            isLoading = isPreviewLoading,
            errorMessage = previewErrorMessage,
            onJumpToChannels = onRequestChannelsFromPreview,
            modifier = Modifier.fillMaxWidth().height(286.dp).focusRequester(previewFocusRequester)
        )
        Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            Surface(
                modifier = Modifier.width(250.dp).fillMaxHeight(),
                shape = RoundedCornerShape(24.dp),
                colors = SurfaceDefaults.colors(containerColor = GlassPane),
                border = Border(border = BorderStroke(1.dp, GlassRule), shape = RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("LIVE SOURCE", style = MaterialTheme.typography.labelLarge, color = GlassMuted)
                    Text(sourceTitle.ifBlank { "CURRENT SOURCE" }, style = MaterialTheme.typography.titleMedium, color = GlassText, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    GlassQuery(categorySearchQuery, "Filter categories", onCategorySearchChange)
                    Text("${categories.size} CATEGORIES", style = MaterialTheme.typography.labelSmall, color = GlassMuted)
                }
            }
            Surface(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                shape = RoundedCornerShape(28.dp),
                colors = SurfaceDefaults.colors(containerColor = GlassPane),
                border = Border(border = BorderStroke(1.dp, GlassRule), shape = RoundedCornerShape(28.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("CHANNEL STREAM", style = MaterialTheme.typography.titleMedium, color = GlassText)
                        Text("${channels.size} LIVE", style = MaterialTheme.typography.labelSmall, color = GlassAccent)
                    }
                    GlassQuery(channelSearchQuery, "Filter channels", onChannelSearchChange)
                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        items(channels, key = { it.id }) { channel ->
                            val requester = channelFocusRequesters.getOrPut(channel.id) { FocusRequester() }
                            GlassChannelRow(
                                channel = channel,
                                locked = isChannelLocked(channel),
                                onClick = { onChannelClick(channel) },
                                onLongClick = { onChannelLongClick(channel) },
                                modifier = Modifier
                                    .focusRequester(requester)
                                    .onFocusChanged { if (it.isFocused) onChannelFocused(channel) }
                                    .onPreviewKeyEvent { event ->
                                        event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN &&
                                            event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_UP &&
                                            onRequestPreviewFromChannel()
                                    }
                            )
                        }
                    }
                }
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth().height(92.dp),
            shape = RoundedCornerShape(24.dp),
            colors = SurfaceDefaults.colors(containerColor = GlassPane),
            border = Border(border = BorderStroke(1.dp, GlassRule), shape = RoundedCornerShape(24.dp))
        ) {
            LazyRow(
                modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(categories, key = { it.id }) { category ->
                    val requester = categoryFocusRequesters.getOrPut(category.id) { FocusRequester() }
                    GlassCategoryChip(
                        category = category,
                        selected = category.id == selectedCategoryId,
                        locked = isCategoryLocked(category),
                        onClick = { onCategoryClick(category) },
                        onLongClick = { onCategoryLongClick(category) },
                        modifier = Modifier
                            .focusRequester(requester)
                            .onFocusChanged { if (it.isFocused) onCategoryFocused(category) }
                            .onPreviewKeyEvent { event ->
                                event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN &&
                                    event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_UP &&
                                    onRequestChannelsFromCategory()
                            }
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassQuery(value: String, placeholder: String, onValueChange: (String) -> Unit) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodySmall.copy(color = GlassText),
        cursorBrush = SolidColor(GlassFocus),
        modifier = Modifier.fillMaxWidth().background(GlassCanvasDeep, RoundedCornerShape(14.dp)).padding(horizontal = 12.dp, vertical = 10.dp),
        decorationBox = { field -> Box { if (value.isBlank()) Text(placeholder, style = MaterialTheme.typography.bodySmall, color = GlassMuted); field() } }
    )
}

@Composable
private fun GlassChannelRow(channel: Channel, locked: Boolean, onClick: () -> Unit, onLongClick: () -> Unit, modifier: Modifier) {
    val shape = RoundedCornerShape(18.dp)
    TvClickableSurface(
        onClick = onClick, onLongClick = onLongClick, modifier = modifier.fillMaxWidth(),
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = GlassPane, focusedContainerColor = GlassPaneFocused, contentColor = GlassText, focusedContentColor = GlassText),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(2.dp, GlassFocus), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 13.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(channel.number.toString().padStart(3, '0'), style = MaterialTheme.typography.labelMedium, color = GlassAccent)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(if (locked) "LOCKED" else channel.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(if (locked) "Protected category" else channel.currentProgram?.title ?: "Live now", style = MaterialTheme.typography.labelSmall, color = GlassMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text("⋮", style = MaterialTheme.typography.titleMedium, color = GlassMuted)
        }
    }
}

@Composable
private fun GlassCategoryChip(category: Category, selected: Boolean, locked: Boolean, onClick: () -> Unit, onLongClick: () -> Unit, modifier: Modifier) {
    val shape = RoundedCornerShape(18.dp)
    TvClickableSurface(
        onClick = onClick, onLongClick = onLongClick, modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = if (selected) GlassAccent.copy(alpha = .24f) else GlassCanvasDeep, focusedContainerColor = GlassPaneFocused, contentColor = GlassText, focusedContentColor = GlassText),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(2.dp, GlassFocus), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.03f)
    ) {
        Text(if (locked) "LOCKED" else category.name, modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp), style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun GlassPreviewPane(channel: Channel?, playerEngine: PlayerEngine?, isLoading: Boolean, errorMessage: String?, onJumpToChannels: () -> Boolean, modifier: Modifier) {
    val shape = RoundedCornerShape(28.dp)
    TvClickableSurface(
        onClick = { onJumpToChannels() }, modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = GlassPane, focusedContainerColor = GlassPaneFocused, contentColor = GlassText, focusedContentColor = GlassText),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(2.dp, GlassFocus), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.01f)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(14.dp), contentAlignment = Alignment.Center) {
            if (playerEngine != null && channel != null && !isLoading && errorMessage.isNullOrBlank()) {
                PlayerRenderView(
                    playerEngine = playerEngine,
                    surfaceType = PlayerRenderSurfaceType.TEXTURE_VIEW,
                    resizeMode = PlayerSurfaceResizeMode.FIT,
                    modifier = Modifier.fillMaxSize()
                )
            } else Text(errorMessage ?: if (isLoading) "Loading preview…" else "Select a channel", color = GlassMuted)
            Column(modifier = Modifier.align(Alignment.BottomStart).background(GlassCanvasDeep.copy(alpha = .78f), RoundedCornerShape(14.dp)).padding(12.dp)) {
                Text("LIVE PREVIEW", style = MaterialTheme.typography.labelSmall, color = GlassAccent)
                Text(channel?.name ?: "NO CHANNEL", style = MaterialTheme.typography.titleMedium, color = GlassText, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
