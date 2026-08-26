package com.streamvault.app.ui.themes.redcinema

/**
 * Red Cinema Live TV contract: a theatre stage with stacked vertical playbill lists.
 * This file is deliberately independent from Blue Ocean and all earlier theme layouts.
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
import androidx.compose.ui.text.style.TextAlign
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
internal fun RedCinemaLiveTvLayout(
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
    val surfaces = LocalThemePresentation.current.surfaces
    Column(
        modifier = modifier.fillMaxSize().background(surfaces.canvas).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("RED CINEMA", style = MaterialTheme.typography.labelMedium, color = surfaces.accent)
                Text("TONIGHT'S PROGRAMME", style = MaterialTheme.typography.displaySmall)
            }
            Text(sourceTitle.ifBlank { "MAIN THEATRE" }, style = MaterialTheme.typography.titleSmall, color = surfaces.textSecondary)
        }
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            RedCinemaStage(
                channel = previewChannel,
                engine = previewPlayerEngine,
                loading = isPreviewLoading,
                error = previewErrorMessage,
                onOpenPlaybill = onRequestChannelsFromPreview,
                modifier = Modifier.weight(1.15f).fillMaxHeight().focusRequester(previewFocusRequester)
            )
            RedCinemaPlaybill(
                categories = categories,
                selectedCategoryId = selectedCategoryId,
                categoryQuery = categorySearchQuery,
                channelQuery = channelSearchQuery,
                channels = channels,
                isCategoryLocked = isCategoryLocked,
                isChannelLocked = isChannelLocked,
                categoryRequesters = categoryFocusRequesters,
                channelRequesters = channelFocusRequesters,
                onCategorySearchChange = onCategorySearchChange,
                onChannelSearchChange = onChannelSearchChange,
                onCategoryClick = onCategoryClick,
                onCategoryLongClick = onCategoryLongClick,
                onChannelClick = onChannelClick,
                onChannelLongClick = onChannelLongClick,
                onCategoryFocused = onCategoryFocused,
                onChannelFocused = onChannelFocused,
                onRequestChannelsFromCategory = onRequestChannelsFromCategory,
                onRequestPreviewFromChannel = onRequestPreviewFromChannel,
                modifier = Modifier.weight(.85f).fillMaxHeight()
            )
        }
    }
}

@Composable
private fun RedCinemaStage(channel: Channel?, engine: PlayerEngine?, loading: Boolean, error: String?, onOpenPlaybill: () -> Boolean, modifier: Modifier) {
    val surfaces = LocalThemePresentation.current.surfaces
    val shape = RoundedCornerShape(6.dp)
    TvClickableSurface(
        onClick = { onOpenPlaybill() },
        modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = surfaces.browseContent, focusedContainerColor = surfaces.focusedSurface, contentColor = surfaces.textPrimary, focusedContentColor = surfaces.textPrimary),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(2.dp, surfaces.accent), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.015f)
    ) {
        Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("ON STAGE", style = MaterialTheme.typography.labelLarge, color = surfaces.accent)
                Text("ACT I", style = MaterialTheme.typography.labelLarge, color = surfaces.textSecondary)
            }
            Box(Modifier.fillMaxWidth().weight(1f).background(surfaces.canvas, RoundedCornerShape(2.dp)), contentAlignment = Alignment.Center) {
                if (engine != null && channel != null && !loading && error.isNullOrBlank()) {
                    PlayerRenderView(playerEngine = engine, surfaceType = PlayerRenderSurfaceType.TEXTURE_VIEW, resizeMode = PlayerSurfaceResizeMode.FIT, modifier = Modifier.fillMaxSize())
                }
                Text(if (loading) "RAISING THE CURTAIN…" else error ?: "SELECT A SCREENING", style = MaterialTheme.typography.labelLarge, color = surfaces.textSecondary, textAlign = TextAlign.Center)
            }
            Text(channel?.name ?: "The stage is waiting", style = MaterialTheme.typography.headlineMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(channel?.currentProgram?.title ?: "Choose a ticket from the playbill to preview a programme.", style = MaterialTheme.typography.titleMedium, color = surfaces.textSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(channel?.nextProgram?.title?.let { "NEXT ACT · $it" } ?: "NEXT ACT · Awaiting guide", style = MaterialTheme.typography.bodyMedium, color = surfaces.accent, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun RedCinemaPlaybill(
    categories: List<Category>, selectedCategoryId: Long?, categoryQuery: String, channelQuery: String, channels: List<Channel>,
    isCategoryLocked: (Category) -> Boolean, isChannelLocked: (Channel) -> Boolean,
    categoryRequesters: MutableMap<Long, FocusRequester>, channelRequesters: MutableMap<Long, FocusRequester>,
    onCategorySearchChange: (String) -> Unit, onChannelSearchChange: (String) -> Unit,
    onCategoryClick: (Category) -> Unit, onCategoryLongClick: (Category) -> Unit,
    onChannelClick: (Channel) -> Unit, onChannelLongClick: (Channel) -> Unit,
    onCategoryFocused: (Category) -> Unit, onChannelFocused: (Channel) -> Unit,
    onRequestChannelsFromCategory: () -> Boolean, onRequestPreviewFromChannel: () -> Boolean, modifier: Modifier
) {
    val surfaces = LocalThemePresentation.current.surfaces
    Column(modifier.background(surfaces.browseRail, RoundedCornerShape(2.dp)).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("PLAYBILL", style = MaterialTheme.typography.titleLarge, color = surfaces.accent)
        Text("ACTS", style = MaterialTheme.typography.labelMedium, color = surfaces.textSecondary)
        RedCinemaQuery(categoryQuery, "Filter acts", onCategorySearchChange)
        LazyColumn(modifier = Modifier.weight(.42f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(categories, key = { it.id }) { category ->
                val requester = categoryRequesters.getOrPut(category.id) { FocusRequester() }
                RedCinemaTicket(
                    number = "ACT",
                    title = if (isCategoryLocked(category)) "RESTRICTED" else category.name,
                    selected = category.id == selectedCategoryId,
                    onClick = { onCategoryClick(category) },
                    onLongClick = { onCategoryLongClick(category) },
                    modifier = Modifier.fillMaxWidth().focusRequester(requester).onFocusChanged { if (it.isFocused) onCategoryFocused(category) }.onPreviewKeyEvent { event ->
                        event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN && event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_RIGHT && onRequestChannelsFromCategory()
                    }
                )
            }
        }
        Text("REEL LIST · ${channels.size}", style = MaterialTheme.typography.labelMedium, color = surfaces.textSecondary)
        RedCinemaQuery(channelQuery, "Search screenings", onChannelSearchChange)
        LazyColumn(modifier = Modifier.weight(.58f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(channels, key = { it.id }) { channel ->
                val requester = channelRequesters.getOrPut(channel.id) { FocusRequester() }
                RedCinemaTicket(
                    number = channel.number.toString().padStart(3, '0'),
                    title = if (isChannelLocked(channel)) "RESTRICTED SCREENING" else channel.name,
                    selected = false,
                    supporting = if (isChannelLocked(channel)) "Protected programme" else channel.currentProgram?.title ?: "Live now",
                    onClick = { onChannelClick(channel) },
                    onLongClick = { onChannelLongClick(channel) },
                    modifier = Modifier.fillMaxWidth().focusRequester(requester).onFocusChanged { if (it.isFocused) onChannelFocused(channel) }.onPreviewKeyEvent { event ->
                        event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN && event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_LEFT && onRequestPreviewFromChannel()
                    }
                )
            }
        }
    }
}

@Composable
private fun RedCinemaTicket(number: String, title: String, selected: Boolean, onClick: () -> Unit, onLongClick: () -> Unit, modifier: Modifier, supporting: String? = null) {
    val surfaces = LocalThemePresentation.current.surfaces
    val shape = RoundedCornerShape(2.dp)
    TvClickableSurface(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = if (selected) surfaces.selectedAccent else surfaces.canvas, focusedContainerColor = surfaces.focusedSurface, contentColor = surfaces.textPrimary, focusedContentColor = surfaces.textPrimary),
        border = ClickableSurfaceDefaults.border(border = Border(border = BorderStroke(1.dp, surfaces.textSecondary.copy(alpha = .24f)), shape = shape), focusedBorder = Border(border = BorderStroke(2.dp, surfaces.accent), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.018f)
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 11.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Text(number, style = MaterialTheme.typography.labelMedium, color = surfaces.accent)
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                supporting?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = surfaces.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis) }
            }
        }
    }
}

@Composable
private fun RedCinemaQuery(value: String, placeholder: String, onValueChange: (String) -> Unit) {
    val surfaces = LocalThemePresentation.current.surfaces
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodySmall.copy(color = surfaces.textPrimary),
        cursorBrush = SolidColor(surfaces.accent),
        modifier = Modifier.fillMaxWidth().background(surfaces.canvas, RoundedCornerShape(2.dp)).padding(horizontal = 10.dp, vertical = 9.dp),
        decorationBox = { field -> Box { if (value.isBlank()) Text(placeholder, style = MaterialTheme.typography.labelSmall, color = surfaces.textSecondary); field() } }
    )
}
