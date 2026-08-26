package com.streamvault.app.ui.themes.streaming

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
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

/** Live Streaming Platform: drawer فئات يسار، صفوف قنوات عمودية في الوسط، ومعاينة مثبتة يميناً. */
@Composable
internal fun StreamingPlatformLiveTvLayout(
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
    Row(
        modifier = modifier.fillMaxSize().background(StreamingCanvas).padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        StreamingCategoryDrawer(
            sourceTitle = sourceTitle,
            categories = categories,
            selectedCategoryId = selectedCategoryId,
            query = categorySearchQuery,
            isCategoryLocked = isCategoryLocked,
            focusRequesters = categoryFocusRequesters,
            onQueryChange = onCategorySearchChange,
            onClick = onCategoryClick,
            onLongClick = onCategoryLongClick,
            onFocused = onCategoryFocused,
            onRequestChannels = onRequestChannelsFromCategory,
            modifier = Modifier.width(272.dp).fillMaxHeight()
        )
        StreamingChannelList(
            channels = channels,
            query = channelSearchQuery,
            isChannelLocked = isChannelLocked,
            focusRequesters = channelFocusRequesters,
            onQueryChange = onChannelSearchChange,
            onClick = onChannelClick,
            onLongClick = onChannelLongClick,
            onFocused = onChannelFocused,
            onRequestPreview = onRequestPreviewFromChannel,
            modifier = Modifier.width(320.dp).fillMaxHeight()
        )
        StreamingPreviewPane(
            channel = previewChannel,
            playerEngine = previewPlayerEngine,
            isLoading = isPreviewLoading,
            errorMessage = previewErrorMessage,
            onJumpToChannels = onRequestChannelsFromPreview,
            modifier = Modifier.weight(1f).fillMaxHeight().focusRequester(previewFocusRequester)
        )
    }
}

@Composable
private fun StreamingCategoryDrawer(
    sourceTitle: String,
    categories: List<Category>,
    selectedCategoryId: Long?,
    query: String,
    isCategoryLocked: (Category) -> Boolean,
    focusRequesters: MutableMap<Long, FocusRequester>,
    onQueryChange: (String) -> Unit,
    onClick: (Category) -> Unit,
    onLongClick: (Category) -> Unit,
    onFocused: (Category) -> Unit,
    onRequestChannels: () -> Boolean,
    modifier: Modifier
) {
    val shape = RoundedCornerShape(18.dp)
    Surface(modifier = modifier, shape = shape, colors = SurfaceDefaults.colors(containerColor = StreamingPanel), border = Border(border = BorderStroke(1.dp, StreamingRule), shape = shape)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("CATEGORIES", style = MaterialTheme.typography.labelLarge, color = StreamingAccent)
            Text(sourceTitle.ifBlank { "LIVE SOURCE" }, style = MaterialTheme.typography.titleSmall, color = StreamingText, maxLines = 2, overflow = TextOverflow.Ellipsis)
            StreamingQuery(query, "Search categories", onQueryChange)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                items(categories, key = { it.id }) { category ->
                    val requester = focusRequesters.getOrPut(category.id) { FocusRequester() }
                    val rowShape = RoundedCornerShape(10.dp)
                    TvClickableSurface(
                        onClick = { onClick(category) }, onLongClick = { onLongClick(category) },
                        modifier = Modifier.fillMaxWidth().focusRequester(requester).onFocusChanged { if (it.isFocused) onFocused(category) }.onPreviewKeyEvent { event ->
                            event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN && event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_RIGHT && onRequestChannels()
                        },
                        shape = ClickableSurfaceDefaults.shape(rowShape),
                        colors = ClickableSurfaceDefaults.colors(containerColor = if (category.id == selectedCategoryId) StreamingPanelFocused else StreamingCanvasRaised, focusedContainerColor = StreamingPanelFocused, contentColor = StreamingText, focusedContentColor = StreamingText),
                        border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, StreamingFocus), shape = rowShape)),
                        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f)
                    ) {
                        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 11.dp)) {
                            Text(if (isCategoryLocked(category)) "LOCKED" else category.name, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StreamingChannelList(
    channels: List<Channel>, query: String, isChannelLocked: (Channel) -> Boolean, focusRequesters: MutableMap<Long, FocusRequester>,
    onQueryChange: (String) -> Unit, onClick: (Channel) -> Unit, onLongClick: (Channel) -> Unit, onFocused: (Channel) -> Unit,
    onRequestPreview: () -> Boolean, modifier: Modifier
) {
    val shape = RoundedCornerShape(18.dp)
    Surface(modifier = modifier, shape = shape, colors = SurfaceDefaults.colors(containerColor = StreamingPanel), border = Border(border = BorderStroke(1.dp, StreamingRule), shape = shape)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("LIVE CHANNELS", style = MaterialTheme.typography.titleMedium, color = StreamingText)
                Text("${channels.size} CHANNELS", style = MaterialTheme.typography.labelMedium, color = StreamingAccent)
            }
            StreamingQuery(query, "Search channels", onQueryChange)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.weight(1f)) {
                items(channels, key = { it.id }) { channel ->
                    val requester = focusRequesters.getOrPut(channel.id) { FocusRequester() }
                    val rowShape = RoundedCornerShape(12.dp)
                    TvClickableSurface(
                        onClick = { onClick(channel) }, onLongClick = { onLongClick(channel) },
                        modifier = Modifier.fillMaxWidth().focusRequester(requester).onFocusChanged { if (it.isFocused) onFocused(channel) }.onPreviewKeyEvent { event ->
                            event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN && event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_RIGHT && onRequestPreview()
                        },
                        shape = ClickableSurfaceDefaults.shape(rowShape),
                        colors = ClickableSurfaceDefaults.colors(containerColor = StreamingCanvasRaised, focusedContainerColor = StreamingPanelFocused, contentColor = StreamingText, focusedContentColor = StreamingText),
                        border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, StreamingFocus), shape = rowShape)),
                        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f)
                    ) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(channel.number.toString().padStart(3, '0'), style = MaterialTheme.typography.labelMedium, color = StreamingAccent)
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(if (isChannelLocked(channel)) "LOCKED" else channel.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(if (isChannelLocked(channel)) "Protected category" else channel.currentProgram?.title ?: "Live now", style = MaterialTheme.typography.bodySmall, color = StreamingMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Text("⋮", style = MaterialTheme.typography.titleMedium, color = StreamingMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StreamingPreviewPane(channel: Channel?, playerEngine: PlayerEngine?, isLoading: Boolean, errorMessage: String?, onJumpToChannels: () -> Boolean, modifier: Modifier) {
    val shape = RoundedCornerShape(20.dp)
    TvClickableSurface(
        onClick = { onJumpToChannels() }, modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = StreamingPanel, focusedContainerColor = StreamingPanelFocused, contentColor = StreamingText, focusedContentColor = StreamingText),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, StreamingFocus), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.015f)
    ) {
        Box(Modifier.fillMaxSize().padding(14.dp), contentAlignment = Alignment.Center) {
            if (playerEngine != null && channel != null && !isLoading && errorMessage.isNullOrBlank()) {
                PlayerRenderView(playerEngine = playerEngine, surfaceType = PlayerRenderSurfaceType.TEXTURE_VIEW, resizeMode = PlayerSurfaceResizeMode.FIT, modifier = Modifier.fillMaxWidth().height(236.dp).align(Alignment.TopCenter))
            } else Text(errorMessage ?: if (isLoading) "Loading preview…" else "Select a channel", color = StreamingMuted)
            Column(Modifier.align(Alignment.BottomStart).padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("PINNED PREVIEW", style = MaterialTheme.typography.labelLarge, color = StreamingAccent)
                Text(channel?.name ?: "NO CHANNEL", style = MaterialTheme.typography.titleMedium, color = StreamingText, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(channel?.currentProgram?.title ?: "Choose a channel to preview", style = MaterialTheme.typography.bodySmall, color = StreamingMuted, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun StreamingQuery(value: String, placeholder: String, onValueChange: (String) -> Unit) {
    BasicTextField(
        value = value, onValueChange = onValueChange, singleLine = true,
        textStyle = MaterialTheme.typography.bodySmall.copy(color = StreamingText), cursorBrush = SolidColor(StreamingFocus),
        modifier = Modifier.fillMaxWidth().background(StreamingCanvasRaised, RoundedCornerShape(10.dp)).padding(horizontal = 12.dp, vertical = 10.dp),
        decorationBox = { field -> Box { if (value.isBlank()) Text(placeholder, style = MaterialTheme.typography.bodySmall, color = StreamingMuted); field() } }
    )
}
