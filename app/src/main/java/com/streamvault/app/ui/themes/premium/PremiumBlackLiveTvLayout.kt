package com.streamvault.app.ui.themes.premium

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

/** Premium Black: عمود فئات أيقوني، شرائح قنوات معدنية عمودية، ومعاينة منخفضة يميناً. */
@Composable
internal fun PremiumBlackLiveTvLayout(
    sourceTitle: String, categories: List<Category>, selectedCategoryId: Long?, categorySearchQuery: String, channelSearchQuery: String,
    channels: List<Channel>, previewChannel: Channel?, previewPlayerEngine: PlayerEngine?, isPreviewLoading: Boolean, previewErrorMessage: String?,
    isCategoryLocked: (Category) -> Boolean, isChannelLocked: (Channel) -> Boolean, categoryFocusRequesters: MutableMap<Long, FocusRequester>,
    channelFocusRequesters: MutableMap<Long, FocusRequester>, previewFocusRequester: FocusRequester, onCategorySearchChange: (String) -> Unit,
    onChannelSearchChange: (String) -> Unit, onCategoryClick: (Category) -> Unit, onCategoryLongClick: (Category) -> Unit,
    onChannelClick: (Channel) -> Unit, onChannelLongClick: (Channel) -> Unit, onCategoryFocused: (Category) -> Unit,
    onChannelFocused: (Channel) -> Unit, onRequestChannelsFromCategory: () -> Boolean, onRequestPreviewFromChannel: () -> Boolean,
    onRequestChannelsFromPreview: () -> Boolean, modifier: Modifier = Modifier
) {
    Row(modifier.fillMaxSize().background(PremiumCanvas).padding(22.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        PremiumCategoryColumn(sourceTitle, categories, selectedCategoryId, categorySearchQuery, isCategoryLocked, categoryFocusRequesters, onCategorySearchChange, onCategoryClick, onCategoryLongClick, onCategoryFocused, onRequestChannelsFromCategory, Modifier.width(272.dp).fillMaxHeight())
        PremiumChannelMetalList(channels, channelSearchQuery, isChannelLocked, channelFocusRequesters, onChannelSearchChange, onChannelClick, onChannelLongClick, onChannelFocused, onRequestPreviewFromChannel, Modifier.width(320.dp).fillMaxHeight())
        PremiumPreviewWell(previewChannel, previewPlayerEngine, isPreviewLoading, previewErrorMessage, onRequestChannelsFromPreview, Modifier.weight(1f).fillMaxHeight().focusRequester(previewFocusRequester))
    }
}

@Composable private fun PremiumCategoryColumn(source: String, categories: List<Category>, selectedId: Long?, query: String, locked: (Category) -> Boolean, requesters: MutableMap<Long, FocusRequester>, onQuery: (String) -> Unit, onClick: (Category) -> Unit, onLongClick: (Category) -> Unit, onFocused: (Category) -> Unit, onRight: () -> Boolean, modifier: Modifier) {
    Surface(modifier, shape = RoundedCornerShape(10.dp), colors = SurfaceDefaults.colors(containerColor = PremiumPanel), border = Border(border = BorderStroke(1.dp, PremiumMetal), shape = RoundedCornerShape(10.dp))) {
        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text("CATEGORIES", style = MaterialTheme.typography.labelLarge, color = PremiumGold)
            Text(source, style = MaterialTheme.typography.labelSmall, color = PremiumMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            PremiumQuery(query, "FILTER", onQuery)
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(categories, key = { it.id }) { category ->
                    val requester = requesters.getOrPut(category.id) { FocusRequester() }
                    val shape = RoundedCornerShape(8.dp)
                    TvClickableSurface(onClick = { onClick(category) }, onLongClick = { onLongClick(category) }, modifier = Modifier.fillMaxWidth().focusRequester(requester).onFocusChanged { if (it.isFocused) onFocused(category) }.onPreviewKeyEvent { event -> event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN && event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_RIGHT && onRight() }, shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = if (category.id == selectedId) PremiumCanvasRaised else PremiumCanvas, focusedContainerColor = PremiumPanelFocused, contentColor = PremiumText, focusedContentColor = PremiumText), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(1.dp, PremiumFocus), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f)) {
                        Column(Modifier.padding(vertical = 11.dp, horizontal = 12.dp)) { Text(if (locked(category)) "LOCK" else category.name, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    }
                }
            }
        }
    }
}

@Composable private fun PremiumChannelMetalList(channels: List<Channel>, query: String, locked: (Channel) -> Boolean, requesters: MutableMap<Long, FocusRequester>, onQuery: (String) -> Unit, onClick: (Channel) -> Unit, onLongClick: (Channel) -> Unit, onFocused: (Channel) -> Unit, onRight: () -> Boolean, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("LIVE CHANNELS", style = MaterialTheme.typography.titleLarge, color = PremiumText); Text("${channels.size} STATIONS", style = MaterialTheme.typography.labelMedium, color = PremiumGold) }
        PremiumQuery(query, "SEARCH CHANNELS", onQuery)
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            items(channels, key = { it.id }) { channel ->
                val requester = requesters.getOrPut(channel.id) { FocusRequester() }
                val shape = RoundedCornerShape(8.dp)
                TvClickableSurface(onClick = { onClick(channel) }, onLongClick = { onLongClick(channel) }, modifier = Modifier.fillMaxWidth().focusRequester(requester).onFocusChanged { if (it.isFocused) onFocused(channel) }.onPreviewKeyEvent { event -> event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN && event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_RIGHT && onRight() }, shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = PremiumPanel, focusedContainerColor = PremiumPanelFocused, contentColor = PremiumText, focusedContentColor = PremiumText), border = ClickableSurfaceDefaults.border(border = Border(border = BorderStroke(1.dp, PremiumMetal), shape = shape), focusedBorder = Border(border = BorderStroke(2.dp, PremiumFocus), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.018f)) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 15.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) { Text(channel.number.toString().padStart(3, '0'), style = MaterialTheme.typography.labelLarge, color = PremiumGold); Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) { Text(if (locked(channel)) "LOCKED CHANNEL" else channel.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(if (locked(channel)) "Protected category" else channel.currentProgram?.title ?: "Live now", style = MaterialTheme.typography.bodySmall, color = PremiumMuted, maxLines = 1, overflow = TextOverflow.Ellipsis) }; Text("MORE", style = MaterialTheme.typography.labelSmall, color = PremiumMuted) }
                }
            }
        }
    }
}

@Composable private fun PremiumPreviewWell(channel: Channel?, engine: PlayerEngine?, loading: Boolean, error: String?, onClick: () -> Boolean, modifier: Modifier) { val shape = RoundedCornerShape(12.dp); TvClickableSurface(onClick = { onClick() }, modifier = modifier, shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = PremiumPanel, focusedContainerColor = PremiumPanelFocused, contentColor = PremiumText, focusedContentColor = PremiumText), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, PremiumFocus), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.015f)) { Box(Modifier.fillMaxSize().padding(12.dp)) { if (engine != null && channel != null && !loading && error.isNullOrBlank()) PlayerRenderView(playerEngine = engine, surfaceType = PlayerRenderSurfaceType.TEXTURE_VIEW, resizeMode = PlayerSurfaceResizeMode.FIT, modifier = Modifier.fillMaxWidth().height(196.dp).align(Alignment.BottomCenter)); Column(Modifier.align(Alignment.TopStart), verticalArrangement = Arrangement.spacedBy(5.dp)) { Text("PREVIEW", style = MaterialTheme.typography.labelLarge, color = PremiumGold); Text(channel?.name ?: "SELECT CHANNEL", style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis); Text(error ?: if (loading) "Loading preview…" else channel?.currentProgram?.title ?: "Preview appears here", style = MaterialTheme.typography.bodySmall, color = PremiumMuted, maxLines = 4, overflow = TextOverflow.Ellipsis) } } } }
@Composable private fun PremiumQuery(value: String, placeholder: String, onChange: (String) -> Unit) { BasicTextField(value = value, onValueChange = onChange, singleLine = true, textStyle = MaterialTheme.typography.bodySmall.copy(color = PremiumText), cursorBrush = SolidColor(PremiumFocus), modifier = Modifier.fillMaxWidth().background(PremiumCanvasRaised, RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 8.dp), decorationBox = { field -> Box { if (value.isBlank()) Text(placeholder, style = MaterialTheme.typography.labelSmall, color = PremiumMuted); field() } }) }
