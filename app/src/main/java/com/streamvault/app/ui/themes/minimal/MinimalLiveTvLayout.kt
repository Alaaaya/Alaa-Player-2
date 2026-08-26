package com.streamvault.app.ui.themes.minimal

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.app.ui.components.PlayerRenderView
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.domain.model.Category
import com.streamvault.domain.model.Channel
import com.streamvault.player.PlayerEngine
import com.streamvault.player.PlayerRenderSurfaceType
import com.streamvault.player.PlayerSurfaceResizeMode

/**
 * Minimal Live TV uses an editorial reading order rather than shelves or HUD cards.
 * Data, parental locks, preview hand-off and D-pad callbacks stay owned by HomeScreen.
 */
@Composable
internal fun MinimalLiveTvLayout(
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
        modifier = modifier.fillMaxSize().background(MinimalCanvas).padding(28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("LIVE / ${sourceTitle.ifBlank { "CURRENT SOURCE" }}", style = MaterialTheme.typography.labelLarge, color = MinimalText, fontWeight = FontWeight.Bold)
                Text("BROWSE CHANNELS", style = MaterialTheme.typography.bodySmall, color = MinimalMuted)
            }
            MinimalTextQuery(value = categorySearchQuery, placeholder = "filter categories", onValueChange = onCategorySearchChange, modifier = Modifier.width(230.dp))
        }
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Column(modifier = Modifier.width(272.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("CATEGORIES", style = MaterialTheme.typography.labelLarge, color = MinimalText, fontWeight = FontWeight.Bold)
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    items(categories, key = { it.id }) { category ->
                        val requester = categoryFocusRequesters.getOrPut(category.id) { FocusRequester() }
                        MinimalCategoryLine(
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
                                        event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_RIGHT &&
                                        onRequestChannelsFromCategory()
                                }
                        )
                    }
                }
            }
            Column(modifier = Modifier.width(320.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("CHANNEL LIST", style = MaterialTheme.typography.labelLarge, color = MinimalText, fontWeight = FontWeight.Bold)
                    Text("${channels.size} ITEMS", style = MaterialTheme.typography.labelSmall, color = MinimalMuted)
                }
                MinimalTextQuery(value = channelSearchQuery, placeholder = "filter channels", onValueChange = onChannelSearchChange)
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    items(channels, key = { it.id }) { channel ->
                        val requester = channelFocusRequesters.getOrPut(channel.id) { FocusRequester() }
                        MinimalChannelLine(
                            channel = channel,
                            locked = isChannelLocked(channel),
                            onClick = { onChannelClick(channel) },
                            onLongClick = { onChannelLongClick(channel) },
                            modifier = Modifier
                                .focusRequester(requester)
                                .onFocusChanged { if (it.isFocused) onChannelFocused(channel) }
                                .onPreviewKeyEvent { event ->
                                    event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN &&
                                        event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_RIGHT &&
                                        onRequestPreviewFromChannel()
                                }
                        )
                    }
                }
            }
            MinimalPreviewPane(
                channel = previewChannel,
                playerEngine = previewPlayerEngine,
                isLoading = isPreviewLoading,
                errorMessage = previewErrorMessage,
                onJumpToChannels = onRequestChannelsFromPreview,
                modifier = Modifier.weight(1f).fillMaxHeight().focusRequester(previewFocusRequester)
            )
        }
    }
}

@Composable
private fun MinimalTextQuery(value: String, placeholder: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodySmall.copy(color = MinimalText),
        cursorBrush = SolidColor(MinimalFocus),
        modifier = modifier.fillMaxWidth().background(MinimalPaper).padding(horizontal = 10.dp, vertical = 9.dp),
        decorationBox = { field -> Box { if (value.isBlank()) Text(placeholder, style = MaterialTheme.typography.bodySmall, color = MinimalMuted); field() } }
    )
}

@Composable
private fun MinimalChannelLine(channel: Channel, locked: Boolean, onClick: () -> Unit, onLongClick: () -> Unit, modifier: Modifier) {
    val shape = RoundedCornerShape(0.dp)
    TvClickableSurface(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier.fillMaxWidth(),
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = Color.Transparent, focusedContainerColor = MinimalPaper, contentColor = MinimalText, focusedContentColor = MinimalText),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(1.dp, MinimalFocus), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = MinimalFocusedScale)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 13.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(channel.number.toString().padStart(3, '0'), style = MaterialTheme.typography.labelMedium, color = MinimalMuted)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(channel.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(if (locked) "LOCKED" else channel.currentProgram?.title ?: "Live", style = MaterialTheme.typography.labelSmall, color = MinimalMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text("›", style = MaterialTheme.typography.titleLarge, color = MinimalMuted)
        }
    }
}

@Composable
private fun MinimalCategoryLine(category: Category, selected: Boolean, locked: Boolean, onClick: () -> Unit, onLongClick: () -> Unit, modifier: Modifier) {
    val shape = RoundedCornerShape(0.dp)
    TvClickableSurface(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier.fillMaxWidth(),
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = if (selected) MinimalPaper else Color.Transparent, focusedContainerColor = MinimalPaper, contentColor = MinimalText, focusedContentColor = MinimalText),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(1.dp, MinimalFocus), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = MinimalFocusedScale)
    ) {
        Text(if (locked) "LOCKED" else category.name, modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 12.dp), style = MaterialTheme.typography.bodyMedium, color = if (selected) MinimalText else MinimalMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun MinimalPreviewPane(channel: Channel?, playerEngine: PlayerEngine?, isLoading: Boolean, errorMessage: String?, onJumpToChannels: () -> Boolean, modifier: Modifier) {
    val shape = RoundedCornerShape(0.dp)
    TvClickableSurface(
        onClick = { onJumpToChannels() },
        modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = MinimalPaper, focusedContainerColor = MinimalPaper, contentColor = MinimalText, focusedContentColor = MinimalText),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(1.dp, MinimalFocus), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = MinimalFocusedScale)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("PREVIEW", style = MaterialTheme.typography.labelLarge, color = MinimalMuted, fontWeight = FontWeight.Bold)
            Box(modifier = Modifier.fillMaxWidth().height(245.dp).background(Color.Black), contentAlignment = Alignment.Center) {
                if (playerEngine != null && channel != null && !isLoading && errorMessage.isNullOrBlank()) {
                    PlayerRenderView(playerEngine = playerEngine, surfaceType = PlayerRenderSurfaceType.TEXTURE_VIEW, resizeMode = PlayerSurfaceResizeMode.FIT, modifier = Modifier.fillMaxSize())
                } else {
                    Text(errorMessage ?: if (isLoading) "Loading…" else "Select a channel", style = MaterialTheme.typography.bodyMedium, color = MinimalMuted)
                }
            }
            Text(channel?.name ?: "No channel selected", style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(channel?.nextProgram?.title ?: "Use LEFT to return to the channel list.", style = MaterialTheme.typography.bodySmall, color = MinimalMuted, maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
    }
}
