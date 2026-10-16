package com.streamvault.app.ui.themes.goldnight

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.app.ui.components.PlayerRenderView
import com.streamvault.domain.model.Category
import com.streamvault.domain.model.Channel
import com.streamvault.domain.model.Program
import com.streamvault.player.PlayerEngine
import com.streamvault.player.PlayerRenderSurfaceType
import com.streamvault.player.PlayerSurfaceResizeMode

/**
 * Gold Night Live TV: ثلاثة أعمدة عمودية فقط —
 * الفئات | القنوات (رقم، شعار دائري، اسم، شريط تقدم ذهبي للبرنامج الحالي) | معاينة مباشرة + البرنامج الحالي + القادم.
 * عقود التركيز وD-pad مطابقة لبقية الثيمات (يمين من الفئات → القنوات → المعاينة).
 */
@Composable
internal fun GoldNightLiveTvLayout(
    sourceTitle: String, categories: List<Category>, selectedCategoryId: Long?, categorySearchQuery: String, channelSearchQuery: String,
    channels: List<Channel>, previewChannel: Channel?, previewPlayerEngine: PlayerEngine?, isPreviewLoading: Boolean, previewErrorMessage: String?,
    isCategoryLocked: (Category) -> Boolean, isChannelLocked: (Channel) -> Boolean, categoryFocusRequesters: MutableMap<Long, FocusRequester>,
    channelFocusRequesters: MutableMap<Long, FocusRequester>, previewFocusRequester: FocusRequester, onCategorySearchChange: (String) -> Unit,
    onChannelSearchChange: (String) -> Unit, onCategoryClick: (Category) -> Unit, onCategoryLongClick: (Category) -> Unit,
    onChannelClick: (Channel) -> Unit, onChannelLongClick: (Channel) -> Unit, onCategoryFocused: (Category) -> Unit,
    onChannelFocused: (Channel) -> Unit, onRequestChannelsFromCategory: () -> Boolean, onRequestPreviewFromChannel: () -> Boolean,
    onRequestChannelsFromPreview: () -> Boolean, modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GoldNightCanvasBrush)
            .padding(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        GoldNightScreenHeader(title = "Live TV", breadcrumb = sourceTitle.takeIf { it.isNotBlank() })
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            GoldNightCategoryColumn(
                categories = categories,
                selectedId = selectedCategoryId,
                query = categorySearchQuery,
                locked = isCategoryLocked,
                requesters = categoryFocusRequesters,
                onQuery = onCategorySearchChange,
                onClick = onCategoryClick,
                onLongClick = onCategoryLongClick,
                onFocused = onCategoryFocused,
                onRight = onRequestChannelsFromCategory,
                modifier = Modifier.width(250.dp).fillMaxHeight()
            )
            GoldNightColumnDivider()
            GoldNightChannelColumn(
                channels = channels,
                selectedChannelId = previewChannel?.id,
                query = channelSearchQuery,
                locked = isChannelLocked,
                requesters = channelFocusRequesters,
                onQuery = onChannelSearchChange,
                onClick = onChannelClick,
                onLongClick = onChannelLongClick,
                onFocused = onChannelFocused,
                onRight = onRequestPreviewFromChannel,
                modifier = Modifier.weight(1.15f).fillMaxHeight()
            )
            GoldNightPreviewColumn(
                channel = previewChannel,
                engine = previewPlayerEngine,
                loading = isPreviewLoading,
                error = previewErrorMessage,
                locked = previewChannel?.let(isChannelLocked) == true,
                onBackToChannels = onRequestChannelsFromPreview,
                previewFocusRequester = previewFocusRequester,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
    }
}

@Composable
private fun GoldNightCategoryColumn(
    categories: List<Category>,
    selectedId: Long?,
    query: String,
    locked: (Category) -> Boolean,
    requesters: MutableMap<Long, FocusRequester>,
    onQuery: (String) -> Unit,
    onClick: (Category) -> Unit,
    onLongClick: (Category) -> Unit,
    onFocused: (Category) -> Unit,
    onRight: () -> Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        GoldNightColumnTitle(title = "Categories")
        GoldNightQueryField(value = query, placeholder = "Filter categories", onChange = onQuery)
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(categories, key = { it.id }) { category ->
                val requester = requesters.getOrPut(category.id) { FocusRequester() }
                val selected = category.id == selectedId
                GoldNightCard(
                    onClick = { onClick(category) },
                    onLongClick = { onLongClick(category) },
                    selected = selected,
                    containerColor = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(requester)
                        .onFocusChanged { if (it.isFocused) onFocused(category) }
                        .goldNightOnDpadRight(onRight)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (locked(category)) "🔒 ${category.name}" else category.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = if (selected) GoldNightGold else GoldNightText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (category.count > 0) {
                            Text(text = category.count.toString(), style = MaterialTheme.typography.labelSmall, color = GoldNightMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GoldNightChannelColumn(
    channels: List<Channel>,
    selectedChannelId: Long?,
    query: String,
    locked: (Channel) -> Boolean,
    requesters: MutableMap<Long, FocusRequester>,
    onQuery: (String) -> Unit,
    onClick: (Channel) -> Unit,
    onLongClick: (Channel) -> Unit,
    onFocused: (Channel) -> Unit,
    onRight: () -> Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        GoldNightColumnTitle(title = "Channels", trailing = channels.size.toString())
        GoldNightQueryField(value = query, placeholder = "Search channels", onChange = onQuery)
        if (channels.isEmpty()) {
            GoldNightStatePanel("No channels", "This category has no channels to show.")
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(vertical = 4.dp, horizontal = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(channels, key = { it.id }) { channel ->
                val requester = requesters.getOrPut(channel.id) { FocusRequester() }
                val isLocked = locked(channel)
                GoldNightCard(
                    onClick = { onClick(channel) },
                    onLongClick = { onLongClick(channel) },
                    selected = channel.id == selectedChannelId,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(requester)
                        .onFocusChanged { if (it.isFocused) onFocused(channel) }
                        .goldNightOnDpadRight(onRight)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (channel.number > 0) channel.number.toString() else "–",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (channel.id == selectedChannelId) GoldNightGold else GoldNightMuted,
                            modifier = Modifier.width(40.dp)
                        )
                        GoldNightRoundLogo(channelName = channel.name, logoUrl = channel.logoUrl, size = 44.dp)
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(
                                text = if (isLocked) "Locked channel" else channel.name,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val program = channel.currentProgram
                            if (!isLocked && program != null) {
                                Text(
                                    text = program.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GoldNightMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            GoldNightProgressBar(
                                progress = if (isLocked) 0f else goldNightProgramProgress(program),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GoldNightPreviewColumn(
    channel: Channel?,
    engine: PlayerEngine?,
    loading: Boolean,
    error: String?,
    locked: Boolean,
    onBackToChannels: () -> Boolean,
    previewFocusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GoldNightCard(
            onClick = { onBackToChannels() },
            focusedScale = 1.015f,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .focusRequester(previewFocusRequester)
                .onPreviewKeyEvent { event ->
                    event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN &&
                        event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_LEFT &&
                        onBackToChannels()
                }
        ) {
            Box(
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)).background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (engine != null && channel != null && !locked && !loading && error.isNullOrBlank()) {
                    PlayerRenderView(
                        playerEngine = engine,
                        surfaceType = PlayerRenderSurfaceType.TEXTURE_VIEW,
                        resizeMode = PlayerSurfaceResizeMode.FIT,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = when {
                            locked -> "Locked"
                            loading -> "Loading preview…"
                            !error.isNullOrBlank() -> error.orEmpty()
                            else -> "Select a channel"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = GoldNightMuted
                    )
                }
                if (channel != null) {
                    Text(
                        text = "LIVE",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldNightOnGold,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                            .background(GoldNightGold, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
        val program = channel?.currentProgram
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(GoldNightPanel)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (channel != null) GoldNightRoundLogo(channelName = channel.name, logoUrl = channel.logoUrl, size = 34.dp)
                Text(
                    text = channel?.name ?: "No channel selected",
                    style = MaterialTheme.typography.titleMedium,
                    color = GoldNightText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = if (locked) "Protected content" else program?.title ?: "No programme information",
                style = MaterialTheme.typography.titleLarge,
                color = GoldNightText,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (!locked && program != null) {
                Text(text = goldNightProgramWindow(program), style = MaterialTheme.typography.labelMedium, color = GoldNightGold)
                GoldNightProgressBar(progress = goldNightProgramProgress(program), modifier = Modifier.fillMaxWidth(), height = 4.dp)
                if (program.description.isNotBlank()) {
                    Text(
                        text = program.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = GoldNightMuted,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        GoldNightColumnTitle(title = "Up Next")
        val upcoming = listOfNotNull(channel?.nextProgram).filterNot { locked }
        if (upcoming.isEmpty()) {
            Text(text = "No upcoming programmes in the guide.", style = MaterialTheme.typography.bodySmall, color = GoldNightMuted)
        } else {
            upcoming.forEach { next -> GoldNightUpcomingRow(next) }
        }
    }
}

@Composable
private fun GoldNightUpcomingRow(program: Program) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(GoldNightPanel)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(3.dp).height(22.dp).background(GoldNightGold))
        Text(
            text = goldNightClock(program.startTime),
            style = MaterialTheme.typography.labelLarge,
            color = GoldNightGold,
            modifier = Modifier.width(56.dp)
        )
        Text(
            text = program.title,
            style = MaterialTheme.typography.bodyMedium,
            color = GoldNightText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun GoldNightQueryField(
    value: String,
    placeholder: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = GoldNightText),
        cursorBrush = SolidColor(GoldNightGold),
        modifier = modifier
            .fillMaxWidth()
            .background(GoldNightPanel, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        decorationBox = { field ->
            Box {
                if (value.isBlank()) Text(text = placeholder, style = MaterialTheme.typography.bodySmall, color = GoldNightMuted)
                field()
            }
        }
    )
}
