package com.streamvault.app.ui.themes.cinematic

import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.domain.model.Category
import com.streamvault.domain.model.Channel
import com.streamvault.player.PlayerEngine

@Composable
internal fun CinematicLiveTvLayout(
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
        modifier = modifier.fillMaxSize().background(CinematicCanvas),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(
            modifier = Modifier.weight(1.52f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = sourceTitle.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = CinematicGold,
                letterSpacing = 1.6.sp,
                modifier = Modifier.padding(start = 6.dp)
            )
            CinematicPreviewPanel(
                channel = previewChannel,
                playerEngine = previewPlayerEngine,
                isLoading = isPreviewLoading,
                errorMessage = previewErrorMessage,
                focusRequester = previewFocusRequester,
                onJumpToChannels = onRequestChannelsFromPreview,
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
        }

        Column(
            modifier = Modifier
                .weight(0.92f)
                .fillMaxHeight()
                .background(CinematicPanel, RoundedCornerShape(24.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "COLLECTIONS",
                style = MaterialTheme.typography.labelMedium,
                color = CinematicGold,
                letterSpacing = 1.4.sp
            )
            CinematicSearchField(categorySearchQuery, "Filter collections", onCategorySearchChange)
            LazyColumn(
                modifier = Modifier.weight(0.34f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(categories, key = { it.id }) { category ->
                    val focusRequester = categoryFocusRequesters.getOrPut(category.id) { FocusRequester() }
                    CinematicCategoryCard(
                        category = category,
                        isSelected = category.id == selectedCategoryId,
                        isLocked = isCategoryLocked(category),
                        onClick = { onCategoryClick(category) },
                        onLongClick = { onCategoryLongClick(category) },
                        modifier = Modifier
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "NOW SHOWING",
                    style = MaterialTheme.typography.labelMedium,
                    color = CinematicGold,
                    letterSpacing = 1.4.sp
                )
                Text(channels.size.toString(), style = MaterialTheme.typography.labelMedium, color = CinematicMuted)
            }
            CinematicSearchField(channelSearchQuery, "Find a channel", onChannelSearchChange)
            LazyColumn(
                modifier = Modifier.weight(0.66f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(channels, key = { it.id }) { channel ->
                    val focusRequester = channelFocusRequesters.getOrPut(channel.id) { FocusRequester() }
                    CinematicChannelRow(
                        channel = channel,
                        isLocked = isChannelLocked(channel),
                        onClick = { onChannelClick(channel) },
                        onLongClick = { onChannelLongClick(channel) },
                        onFocused = { onChannelFocused(channel) },
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .onPreviewKeyEvent { event ->
                                event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN &&
                                    event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_LEFT &&
                                    onRequestPreviewFromChannel()
                            }
                    )
                }
            }
        }
    }
}

@Composable
private fun CinematicSearchField(value: String, placeholder: String, onValueChange: (String) -> Unit) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = CinematicText),
        cursorBrush = SolidColor(CinematicGold),
        modifier = Modifier
            .fillMaxWidth()
            .background(CinematicCanvas, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 11.dp),
        decorationBox = { innerTextField ->
            Box {
                if (value.isBlank()) {
                    Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = CinematicMuted)
                }
                innerTextField()
            }
        }
    )
}
