package com.streamvault.app.ui.themes.blueocean

/**
 * Style contract: Blue Ocean is a tide-atlas. Categories remain a vertical harbour rail;
 * channels remain a vertical current list; the video dossier occupies the central landmark.
 */

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
import androidx.tv.material3.Text
import com.streamvault.app.ui.components.PlayerRenderView
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.app.ui.theme.LocalThemePresentation
import com.streamvault.domain.model.Category
import com.streamvault.domain.model.Channel
import com.streamvault.player.PlayerEngine
import com.streamvault.player.PlayerRenderSurfaceType
import com.streamvault.player.PlayerSurfaceResizeMode

@Composable
internal fun BlueOceanLiveTvLayout(
    sourceTitle: String, categories: List<Category>, selectedCategoryId: Long?, categorySearchQuery: String, channelSearchQuery: String,
    channels: List<Channel>, previewChannel: Channel?, previewPlayerEngine: PlayerEngine?, isPreviewLoading: Boolean, previewErrorMessage: String?,
    isCategoryLocked: (Category) -> Boolean, isChannelLocked: (Channel) -> Boolean, categoryFocusRequesters: MutableMap<Long, FocusRequester>,
    channelFocusRequesters: MutableMap<Long, FocusRequester>, previewFocusRequester: FocusRequester, onCategorySearchChange: (String) -> Unit,
    onChannelSearchChange: (String) -> Unit, onCategoryClick: (Category) -> Unit, onCategoryLongClick: (Category) -> Unit,
    onChannelClick: (Channel) -> Unit, onChannelLongClick: (Channel) -> Unit, onCategoryFocused: (Category) -> Unit,
    onChannelFocused: (Channel) -> Unit, onRequestChannelsFromCategory: () -> Boolean, onRequestPreviewFromChannel: () -> Boolean,
    onRequestChannelsFromPreview: () -> Boolean, modifier: Modifier = Modifier
) {
    val p = LocalThemePresentation.current
    val s = p.surfaces
    Row(modifier.fillMaxSize().background(s.canvas).padding(26.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        BlueOceanHarbourRail(sourceTitle, categories, selectedCategoryId, categorySearchQuery, isCategoryLocked, categoryFocusRequesters, onCategorySearchChange, onCategoryClick, onCategoryLongClick, onCategoryFocused, onRequestChannelsFromCategory, Modifier.width(196.dp).fillMaxHeight())
        BlueOceanProgramDossier(previewChannel, previewPlayerEngine, isPreviewLoading, previewErrorMessage, onRequestChannelsFromPreview, Modifier.weight(1.1f).fillMaxHeight().focusRequester(previewFocusRequester))
        BlueOceanCurrentList(channels, channelSearchQuery, isChannelLocked, channelFocusRequesters, onChannelSearchChange, onChannelClick, onChannelLongClick, onChannelFocused, onRequestPreviewFromChannel, Modifier.width(356.dp).fillMaxHeight())
    }
}

@Composable
private fun BlueOceanHarbourRail(source: String, categories: List<Category>, selectedId: Long?, query: String, locked: (Category) -> Boolean, requesters: MutableMap<Long, FocusRequester>, onQuery: (String) -> Unit, onClick: (Category) -> Unit, onLongClick: (Category) -> Unit, onFocused: (Category) -> Unit, onRight: () -> Boolean, modifier: Modifier) {
    val p = LocalThemePresentation.current
    val s = p.surfaces
    Column(modifier.background(s.browseRail, RoundedCornerShape(30.dp)).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("TIDE ATLAS", style = MaterialTheme.typography.labelMedium, color = s.accent)
        Text(source, style = MaterialTheme.typography.titleSmall, color = s.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        BlueOceanQuery(query, "Filter routes", onQuery)
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            items(categories, key = { it.id }) { category ->
                val requester = requesters.getOrPut(category.id) { FocusRequester() }
                val selected = category.id == selectedId
                val shape = RoundedCornerShape(18.dp)
                TvClickableSurface(
                    onClick = { onClick(category) }, onLongClick = { onLongClick(category) },
                    modifier = Modifier.fillMaxWidth().focusRequester(requester).onFocusChanged { if (it.isFocused) onFocused(category) }.onPreviewKeyEvent { event -> event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN && event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_RIGHT && onRight() },
                    shape = ClickableSurfaceDefaults.shape(shape),
                    colors = ClickableSurfaceDefaults.colors(containerColor = if (selected) s.selectedAccent else s.canvas, focusedContainerColor = s.focusedSurface, contentColor = s.textPrimary, focusedContentColor = s.textPrimary),
                    border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(s.focusBorderWidth, s.accent), shape = shape)),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.025f)
                ) { Column(Modifier.padding(horizontal = 12.dp, vertical = 13.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) { Text(if (locked(category)) "LOCKED ROUTE" else category.name, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(if (selected) "CURRENT TIDE" else "ROUTE", style = MaterialTheme.typography.labelSmall, color = s.textSecondary) } }
            }
        }
    }
}

@Composable
private fun BlueOceanProgramDossier(channel: Channel?, engine: PlayerEngine?, loading: Boolean, error: String?, onChannels: () -> Boolean, modifier: Modifier) {
    val p = LocalThemePresentation.current
    val s = p.surfaces
    val shape = RoundedCornerShape(34.dp)
    TvClickableSurface(onClick = { onChannels() }, modifier = modifier, shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = s.browseContent, focusedContainerColor = s.focusedSurface, contentColor = s.textPrimary, focusedContentColor = s.textPrimary), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(s.focusBorderWidth, s.accent), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f)) { Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("LIVE DOSSIER", style = MaterialTheme.typography.labelMedium, color = s.accent); Text("ON AIR", style = MaterialTheme.typography.labelMedium, color = s.textSecondary) }; Box(Modifier.fillMaxWidth().height(290.dp).background(s.canvas, RoundedCornerShape(22.dp))) { if (engine != null && channel != null && !loading && error.isNullOrBlank()) PlayerRenderView(playerEngine = engine, surfaceType = PlayerRenderSurfaceType.TEXTURE_VIEW, resizeMode = PlayerSurfaceResizeMode.FIT, modifier = Modifier.fillMaxSize()); Text(if (loading) "TUNING CURRENT…" else error ?: "VIDEO CURRENT", Modifier.align(Alignment.Center), style = MaterialTheme.typography.labelMedium, color = s.textSecondary) }; Text(channel?.name ?: "Select a station", style = MaterialTheme.typography.headlineMedium, maxLines = 2, overflow = TextOverflow.Ellipsis); Text(channel?.currentProgram?.title ?: "The programme dossier will appear once a route is selected.", style = MaterialTheme.typography.titleMedium, color = s.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis); Text(channel?.nextProgram?.title?.let { "NEXT WAVE · $it" } ?: "NEXT WAVE · Awaiting EPG", style = MaterialTheme.typography.bodyMedium, color = s.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis) } }
}

@Composable
private fun BlueOceanCurrentList(channels: List<Channel>, query: String, locked: (Channel) -> Boolean, requesters: MutableMap<Long, FocusRequester>, onQuery: (String) -> Unit, onClick: (Channel) -> Unit, onLongClick: (Channel) -> Unit, onFocused: (Channel) -> Unit, onPreview: () -> Boolean, modifier: Modifier) {
    val p = LocalThemePresentation.current
    val s = p.surfaces
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("CURRENT LIST", style = MaterialTheme.typography.titleLarge, color = s.textPrimary); Text("${channels.size}", style = MaterialTheme.typography.labelLarge, color = s.accent) }; BlueOceanQuery(query, "Search stations", onQuery); LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) { items(channels, key = { it.id }) { channel -> val requester = requesters.getOrPut(channel.id) { FocusRequester() }; val shape = RoundedCornerShape(16.dp); TvClickableSurface(onClick = { onClick(channel) }, onLongClick = { onLongClick(channel) }, modifier = Modifier.fillMaxWidth().focusRequester(requester).onFocusChanged { if (it.isFocused) onFocused(channel) }.onPreviewKeyEvent { event -> event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN && event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_LEFT && onPreview() }, shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = s.browseContent, focusedContainerColor = s.focusedSurface, contentColor = s.textPrimary, focusedContentColor = s.textPrimary), border = ClickableSurfaceDefaults.border(border = Border(border = BorderStroke(1.dp, s.textSecondary.copy(alpha = .22f)), shape = shape), focusedBorder = Border(border = BorderStroke(s.focusBorderWidth, s.accent), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.025f)) { Row(Modifier.fillMaxWidth().padding(15.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) { Text(channel.number.toString().padStart(3, '0'), style = MaterialTheme.typography.labelLarge, color = s.accent); Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) { Text(if (locked(channel)) "LOCKED STATION" else channel.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(if (locked(channel)) "Protected route" else channel.currentProgram?.title ?: "Live now", style = MaterialTheme.typography.bodySmall, color = s.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis) }; Text("≈", style = MaterialTheme.typography.titleLarge, color = s.accent) } } } } }
}

@Composable
private fun BlueOceanQuery(value: String, placeholder: String, onChange: (String) -> Unit) { val s = LocalThemePresentation.current.surfaces; BasicTextField(value = value, onValueChange = onChange, singleLine = true, textStyle = MaterialTheme.typography.bodySmall.copy(color = s.textPrimary), cursorBrush = SolidColor(s.accent), modifier = Modifier.fillMaxWidth().background(s.canvas, RoundedCornerShape(14.dp)).padding(horizontal = 11.dp, vertical = 10.dp), decorationBox = { field -> Box { if (value.isBlank()) Text(placeholder, style = MaterialTheme.typography.labelSmall, color = s.textSecondary); field() } }) }
