package com.streamvault.app.ui.themes.neon

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
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.streamvault.app.ui.components.PlayerRenderView
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.app.ui.theme.rememberReferenceLiveTvColumnMetrics
import com.streamvault.domain.model.Category
import com.streamvault.domain.model.Channel
import com.streamvault.player.PlayerEngine
import com.streamvault.player.PlayerRenderSurfaceType
import com.streamvault.player.PlayerSurfaceResizeMode

internal val NeonCanvas = Color(0xFF040812)
internal val NeonPanel = Color(0xFF0A1324)
internal val NeonPanelRaised = Color(0xFF10243B)
internal val NeonCyan = Color(0xFF5BF4FF)
internal val NeonPink = Color(0xFFFF4FD8)
internal val NeonLime = Color(0xFFC6FF53)
internal val NeonText = Color(0xFFE8FCFF)
internal val NeonMuted = Color(0xFF8EAEBD)

/** Neon Future live TV: top HUD categories, vertical channel stream on the left, floating preview on the right. */
@Composable
internal fun NeonFutureLiveTvLayout(
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
    val columnMetrics = rememberReferenceLiveTvColumnMetrics()
    Column(
        modifier = modifier.fillMaxSize().background(NeonCanvas),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        NeonFutureHudHeader(sourceTitle = sourceTitle, categoryQuery = categorySearchQuery, onCategoryQueryChange = onCategorySearchChange)
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(columnMetrics.columnSpacing)) {
            Column(
                modifier = Modifier.width(columnMetrics.categoryWidth).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("CATEGORY BANDS", style = MaterialTheme.typography.labelLarge, color = NeonPink, fontWeight = FontWeight.Black)
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories, key = { it.id }) { category ->
                        val focusRequester = categoryFocusRequesters.getOrPut(category.id) { FocusRequester() }
                        NeonFutureCategoryChip(
                            category = category,
                            selected = category.id == selectedCategoryId,
                            locked = isCategoryLocked(category),
                            onClick = { onCategoryClick(category) },
                            onLongClick = { onCategoryLongClick(category) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
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
            Column(
                modifier = Modifier.weight(columnMetrics.channelWeight).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("SIGNAL STREAM", style = MaterialTheme.typography.labelLarge, color = NeonCyan, fontWeight = FontWeight.Black)
                    Text("${channels.size} NODES", style = MaterialTheme.typography.labelSmall, color = NeonMuted)
                }
                NeonFutureSearchField(channelSearchQuery, "Query signal stream", onChannelSearchChange)
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(channels, key = { it.id }) { channel ->
                        val focusRequester = channelFocusRequesters.getOrPut(channel.id) { FocusRequester() }
                        NeonFutureChannelNode(
                            channel = channel,
                            locked = isChannelLocked(channel),
                            onClick = { onChannelClick(channel) },
                            onLongClick = { onChannelLongClick(channel) },
                            modifier = Modifier
                                .focusRequester(focusRequester)
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
            NeonFuturePreviewWindow(
                channel = previewChannel,
                playerEngine = previewPlayerEngine,
                isLoading = isPreviewLoading,
                errorMessage = previewErrorMessage,
                focusRequester = previewFocusRequester,
                onJumpToChannels = onRequestChannelsFromPreview,
                modifier = Modifier.weight(columnMetrics.previewWeight).fillMaxHeight()
            )
        }
    }
}

@Composable
private fun NeonFutureHudHeader(sourceTitle: String, categoryQuery: String, onCategoryQueryChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(NeonPanel, NeonPanelRaised, NeonPanel)), RoundedCornerShape(16.dp))
            .padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("NEON FUTURE / LIVE", style = MaterialTheme.typography.labelLarge, color = NeonPink, fontWeight = FontWeight.Black)
            Text(sourceTitle.ifBlank { "ACTIVE SIGNAL" }, style = MaterialTheme.typography.titleMedium, color = NeonText, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        NeonFutureSearchField(categoryQuery, "Filter bands", onCategoryQueryChange, modifier = Modifier.width(230.dp))
    }
}

@Composable
private fun NeonFutureSearchField(value: String, placeholder: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodySmall.copy(color = NeonText),
        cursorBrush = SolidColor(NeonCyan),
        modifier = modifier
            .background(NeonCanvas, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        decorationBox = { innerTextField ->
            Box {
                if (value.isBlank()) Text(placeholder, style = MaterialTheme.typography.bodySmall, color = NeonMuted)
                innerTextField()
            }
        }
    )
}

@Composable
private fun NeonFutureCategoryChip(category: Category, selected: Boolean, locked: Boolean, onClick: () -> Unit, onLongClick: () -> Unit, modifier: Modifier) {
    val shape = RoundedCornerShape(8.dp)
    TvClickableSurface(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) NeonCyan.copy(alpha = .20f) else NeonPanel,
            focusedContainerColor = NeonPanelRaised,
            contentColor = NeonText,
            focusedContentColor = NeonText
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(border = BorderStroke(2.dp, NeonCyan), shape = shape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.015f)
    ) {
        Text(
            text = if (locked) "LOCKED" else category.name.uppercase(),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) NeonCyan else NeonMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun NeonFutureChannelNode(channel: Channel, locked: Boolean, onClick: () -> Unit, onLongClick: () -> Unit, modifier: Modifier) {
    val shape = RoundedCornerShape(10.dp)
    TvClickableSurface(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier.fillMaxWidth(),
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = NeonPanel, focusedContainerColor = NeonPanelRaised, contentColor = NeonText, focusedContentColor = NeonText),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(border = BorderStroke(2.dp, NeonPink), shape = shape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.012f)
    ) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(channel.number.toString().padStart(3, '0'), style = MaterialTheme.typography.labelMedium, color = NeonLime, fontWeight = FontWeight.Black)
            Box(modifier = Modifier.size(42.dp).background(NeonCanvas, RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
                AsyncImage(model = channel.logoUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(channel.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = when {
                        locked -> "ACCESS GATED"
                        else -> channel.currentProgram?.title ?: "LIVE DATA UNAVAILABLE"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (locked) NeonPink else NeonMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text("⋮", style = MaterialTheme.typography.titleMedium, color = NeonCyan)
        }
    }
}

@Composable
private fun NeonFuturePreviewWindow(
    channel: Channel?,
    playerEngine: PlayerEngine?,
    isLoading: Boolean,
    errorMessage: String?,
    focusRequester: FocusRequester,
    onJumpToChannels: () -> Boolean,
    modifier: Modifier
) {
    val shape = RoundedCornerShape(18.dp)
    TvClickableSurface(
        onClick = { onJumpToChannels() },
        modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = NeonPanel, focusedContainerColor = NeonPanelRaised, contentColor = NeonText, focusedContentColor = NeonText),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(border = BorderStroke(2.dp, NeonCyan), shape = shape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.01f)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("FLOATING PREVIEW", style = MaterialTheme.typography.labelMedium, color = NeonCyan, fontWeight = FontWeight.Black)
            Box(modifier = Modifier.fillMaxWidth().weight(1f).background(Color.Black, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                if (playerEngine != null && channel != null && !isLoading && errorMessage.isNullOrBlank()) {
                    PlayerRenderView(
                        playerEngine = playerEngine,
                        surfaceType = PlayerRenderSurfaceType.TEXTURE_VIEW,
                        resizeMode = PlayerSurfaceResizeMode.FIT,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(if (!errorMessage.isNullOrBlank()) errorMessage else if (isLoading) "SYNCING SIGNAL…" else "SELECT A CHANNEL", style = MaterialTheme.typography.bodyMedium, color = NeonMuted)
                }
            }
            Text(channel?.name ?: "NO ACTIVE NODE", style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(channel?.nextProgram?.title ?: "DOWN: RETURN TO SIGNAL STREAM", style = MaterialTheme.typography.bodySmall, color = NeonMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}
