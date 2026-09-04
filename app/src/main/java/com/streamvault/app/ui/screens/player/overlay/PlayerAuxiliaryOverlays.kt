package com.streamvault.app.ui.screens.player.overlay

import androidx.compose.foundation.border
import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.LiveTv
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.app.R
import com.streamvault.app.device.rememberIsTelevisionDevice
import com.streamvault.app.ui.components.ChannelLogoBadge
import com.streamvault.app.ui.components.shell.StatusPill
import com.streamvault.app.ui.design.AppColors
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.app.ui.model.archivePlaybackCapability
import com.streamvault.app.ui.screens.player.PlayerDiagnosticsUiState
import com.streamvault.app.ui.time.LocalAppTimeFormat
import com.streamvault.app.ui.time.createTimeFormat
import com.streamvault.domain.model.Channel
import com.streamvault.domain.model.Program
import com.streamvault.player.PlayerStats
import java.util.Date
import kotlinx.coroutines.launch

import com.streamvault.app.ui.design.AppColors.Brand as Primary
import com.streamvault.app.ui.design.AppColors.SurfaceElevated as SurfaceVariant
import com.streamvault.app.ui.design.AppColors.TextSecondary as TextSecondary
import com.streamvault.app.ui.design.AppColors.TextTertiary as OnSurfaceDim


@Composable
fun ChannelListOverlay(
    channels: List<Channel>,
    recentChannels: List<Channel>,
    currentChannelId: Long,
    overlayFocusRequester: FocusRequester = remember {
        FocusRequester()
    },
    lastVisitedCategoryName: String? = null,
    onOpenLastGroup: () -> Unit = {},
    onOpenCategories: () -> Unit = {},
    onSelectChannel: (Long) -> Unit,
    onDismiss: () -> Unit,
    onOverlayInteracted: () -> Unit = {}
) {
    val listState = rememberLazyListState()

    val currentIndex = remember(
        channels,
        currentChannelId
    ) {
        channels
            .indexOfFirst {
                it.id == currentChannelId
            }
            .coerceAtLeast(0)
    }

    /*
     * IMPORTANT:
     *
     * recentChannels is intentionally NOT rendered.
     *
     * lastVisitedCategoryName is also intentionally NOT rendered.
     *
     * Both parameters stay in the function signature so the
     * existing PlayerScreen integration does not break.
     */
    val headerItemCount = 1

    val canScrollUp by remember {
        derivedStateOf {
            listState.canScrollBackward
        }
    }

    val canScrollDown by remember {
        derivedStateOf {
            listState.canScrollForward
        }
    }

    val isRtl =
        LocalLayoutDirection.current ==
            LayoutDirection.Rtl

    LaunchedEffect(
        channels,
        currentIndex
    ) {
        if (channels.isNotEmpty()) {
            listState.scrollToItem(
                headerItemCount + currentIndex
            )
        }
    }

    BackHandler {
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color.Black.copy(
                    alpha = 0.28f
                )
            )
    ) {

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {

            val isTelevisionDevice =
                rememberIsTelevisionDevice()

            val panelModifier =
                when {
                    maxWidth < 700.dp -> {
                        Modifier
                            .fillMaxWidth(0.94f)
                            .fillMaxHeight()
                            .padding(14.dp)
                    }

                    !isTelevisionDevice &&
                        maxWidth < 1280.dp -> {
                        Modifier
                            .fillMaxWidth(0.58f)
                            .fillMaxHeight()
                            .padding(18.dp)
                    }

                    else -> {
                        Modifier
                            .width(680.dp)
                            .fillMaxHeight()
                            .padding(
                                top = 18.dp,
                                bottom = 18.dp,
                                start = 18.dp,
                                end = 18.dp
                            )
                    }
                }

            Box(
                modifier = panelModifier
            ) {

                PlayerOverlayPanel(
                    modifier = Modifier.fillMaxSize()
                ) {

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding =
                            PaddingValues(
                                top = 10.dp,
                                bottom = 22.dp,
                                start = 8.dp,
                                end = 8.dp
                            ),
                        verticalArrangement =
                            Arrangement.spacedBy(7.dp)
                    ) {

                        /*
                         * ==================================================
                         * HEADER
                         * ==================================================
                         */
                        item {

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = 8.dp,
                                        vertical = 10.dp
                                    ),
                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {

                                Box(
                                    modifier = Modifier
                                        .width(46.dp)
                                        .height(46.dp)
                                        .clip(
                                            RoundedCornerShape(
                                                14.dp
                                            )
                                        )
                                        .background(
                                            Primary.copy(
                                                alpha = 0.12f
                                            )
                                        )
                                        .border(
                                            1.dp,
                                            Primary.copy(
                                                alpha = 0.32f
                                            ),
                                            RoundedCornerShape(
                                                14.dp
                                            )
                                        ),
                                    contentAlignment =
                                        Alignment.Center
                                ) {

                                    Icon(
                                        imageVector =
                                            Icons.Outlined.LiveTv,
                                        contentDescription =
                                            null,
                                        tint =
                                            Primary,
                                        modifier =
                                            Modifier.width(
                                                25.dp
                                            )
                                    )
                                }

                                Spacer(
                                    modifier =
                                        Modifier.width(
                                            14.dp
                                        )
                                )

                                Column(
                                    modifier =
                                        Modifier.weight(1f)
                                ) {

                                    Text(
                                        text =
                                            stringResource(
                                                R.string.player_channel_list_title,
                                                channels.size
                                            ),
                                        style =
                                            MaterialTheme
                                                .typography
                                                .titleLarge,
                                        color =
                                            Color.White,
                                        fontWeight =
                                            FontWeight.Bold
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.height(
                                                3.dp
                                            )
                                    )

                                    Text(
                                        text =
                                            stringResource(
                                                R.string.player_channel_list_hint
                                            ),
                                        style =
                                            MaterialTheme
                                                .typography
                                                .bodySmall,
                                        color =
                                            OnSurfaceDim
                                    )
                                }

                                /*
                                 * CHANNEL COUNT
                                 */
                                Box(
                                    modifier = Modifier
                                        .clip(
                                            RoundedCornerShape(
                                                12.dp
                                            )
                                        )
                                        .background(
                                            Primary.copy(
                                                alpha = 0.12f
                                            )
                                        )
                                        .border(
                                            1.dp,
                                            Primary.copy(
                                                alpha = 0.30f
                                            ),
                                            RoundedCornerShape(
                                                12.dp
                                            )
                                        )
                                        .padding(
                                            horizontal = 12.dp,
                                            vertical = 8.dp
                                        )
                                ) {

                                    Column(
                                        horizontalAlignment =
                                            Alignment.CenterHorizontally
                                    ) {

                                        Text(
                                            text =
                                                channels.size
                                                    .toString(),
                                            style =
                                                MaterialTheme
                                                    .typography
                                                    .titleMedium,
                                            color =
                                                Primary,
                                            fontWeight =
                                                FontWeight.Bold
                                        )

                                        Text(
                                            text =
                                                "قناة",
                                            style =
                                                MaterialTheme
                                                    .typography
                                                    .labelSmall,
                                            color =
                                                Color.White.copy(
                                                    alpha = 0.55f
                                                )
                                        )
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = 8.dp
                                    )
                                    .height(1.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Primary.copy(
                                                    alpha = 0.85f
                                                ),
                                                Primary.copy(
                                                    alpha = 0.25f
                                                ),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                        }

                        /*
                         * ==================================================
                         * CHANNELS
                         * ==================================================
                         */
                        items(
                            count = channels.size,
                            key = { index ->
                                channels[index].id
                            }
                        ) { index ->

                            val channel =
                                channels[index]

                            val isSelected =
                                channel.id ==
                                    currentChannelId

                            val channelNumber =
                                channel.number
                                    .takeIf {
                                        it > 0
                                    }
                                    ?: (
                                        index + 1
                                    )

                            var isFocused by
                                remember {
                                    mutableStateOf(
                                        false
                                    )
                                }

                            val containerColor =
                                when {
                                    isFocused ->
                                        Primary.copy(
                                            alpha = 0.18f
                                        )

                                    isSelected ->
                                        Primary.copy(
                                            alpha = 0.08f
                                        )

                                    else ->
                                        AppColors
                                            .Surface
                                            .copy(
                                                alpha = 0.74f
                                            )
                                }

                            val borderColor =
                                when {
                                    isFocused ->
                                        Primary.copy(
                                            alpha = 0.95f
                                        )

                                    isSelected ->
                                        Primary.copy(
                                            alpha = 0.40f
                                        )

                                    else ->
                                        Color.White.copy(
                                            alpha = 0.055f
                                        )
                                }

                            TvClickableSurface(
                                onClick = {
                                    onOverlayInteracted()

                                    onSelectChannel(
                                        channel.id
                                    )

                                    onDismiss()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged {
                                        focusState ->

                                        isFocused =
                                            focusState
                                                .isFocused

                                        if (
                                            focusState
                                                .isFocused
                                        ) {
                                            onOverlayInteracted()
                                        }
                                    }
                                    .then(
                                        if (
                                            isSelected
                                        ) {
                                            Modifier
                                                .focusRequester(
                                                    overlayFocusRequester
                                                )
                                        } else {
                                            Modifier
                                        }
                                    ),
                                scale =
                                    ClickableSurfaceDefaults
                                        .scale(
                                            focusedScale =
                                                1.015f
                                        ),
                                shape =
                                    ClickableSurfaceDefaults
                                        .shape(
                                            RoundedCornerShape(
                                                16.dp
                                            )
                                        ),
                                colors =
                                    ClickableSurfaceDefaults
                                        .colors(
                                            containerColor =
                                                containerColor,
                                            focusedContainerColor =
                                                containerColor
                                        )
                            ) {

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width =
                                                if (
                                                    isFocused
                                                ) {
                                                    2.dp
                                                } else {
                                                    1.dp
                                                },
                                            color =
                                                borderColor,
                                            shape =
                                                RoundedCornerShape(
                                                    16.dp
                                                )
                                        )
                                        .padding(
                                            horizontal = 12.dp,
                                            vertical = 10.dp
                                        ),
                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {

                                    /*
                                     * CHANNEL LOGO
                                     */
                                    ChannelLogoBadge(
                                        channelName =
                                            channel.name,
                                        logoUrl =
                                            channel.logoUrl,
                                        modifier =
                                            Modifier
                                                .width(72.dp)
                                                .height(54.dp),
                                        shape =
                                            RoundedCornerShape(
                                                12.dp
                                            ),
                                        backgroundColor =
                                            Color.Black.copy(
                                                alpha = 0.42f
                                            ),
                                        contentPadding =
                                            PaddingValues(
                                                6.dp
                                            ),
                                        textStyle =
                                            MaterialTheme
                                                .typography
                                                .labelMedium,
                                        textColor =
                                            Color.White.copy(
                                                alpha = 0.65f
                                            )
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.width(
                                                12.dp
                                            )
                                    )

                                    /*
                                     * CHANNEL NUMBER
                                     */
                                    Box(
                                        modifier = Modifier
                                            .width(44.dp)
                                            .height(42.dp)
                                            .clip(
                                                RoundedCornerShape(
                                                    11.dp
                                                )
                                            )
                                            .background(
                                                if (
                                                    isFocused
                                                ) {
                                                    Primary.copy(
                                                        alpha =
                                                            0.20f
                                                    )
                                                } else {
                                                    Color.Black.copy(
                                                        alpha =
                                                            0.28f
                                                    )
                                                }
                                            )
                                            .border(
                                                1.dp,
                                                if (
                                                    isFocused
                                                ) {
                                                    Primary.copy(
                                                        alpha =
                                                            0.60f
                                                    )
                                                } else {
                                                    Color.White.copy(
                                                        alpha =
                                                            0.06f
                                                    )
                                                },
                                                RoundedCornerShape(
                                                    11.dp
                                                )
                                            ),
                                        contentAlignment =
                                            Alignment.Center
                                    ) {

                                        Text(
                                            text =
                                                channelNumber
                                                    .toString()
                                                    .padStart(
                                                        2,
                                                        '0'
                                                    ),
                                            style =
                                                MaterialTheme
                                                    .typography
                                                    .titleMedium,
                                            color =
                                                if (
                                                    isFocused
                                                ) {
                                                    Primary
                                                } else {
                                                    Color.White.copy(
                                                        alpha =
                                                            0.82f
                                                    )
                                                },
                                            fontWeight =
                                                FontWeight.Bold
                                        )
                                    }

                                    Spacer(
                                        modifier =
                                            Modifier.width(
                                                12.dp
                                            )
                                    )

                                    /*
                                     * CHANNEL NAME
                                     */
                                    Column(
                                        modifier =
                                            Modifier.weight(
                                                1f
                                            )
                                    ) {

                                        Text(
                                            text =
                                                channel.name,
                                            style =
                                                MaterialTheme
                                                    .typography
                                                    .bodyLarge
                                                    .copy(
                                                        fontSize =
                                                            17.sp
                                                    ),
                                            color =
                                                Color.White,
                                            fontWeight =
                                                if (
                                                    isFocused ||
                                                    isSelected
                                                ) {
                                                    FontWeight.Bold
                                                } else {
                                                    FontWeight.Normal
                                                },
                                            maxLines = 1,
                                            overflow =
                                                if (
                                                    isFocused
                                                ) {
                                                    TextOverflow
                                                        .Clip
                                                } else {
                                                    TextOverflow
                                                        .Ellipsis
                                                },
                                            modifier =
                                                if (
                                                    isFocused
                                                ) {
                                                    Modifier
                                                        .basicMarquee(
                                                            iterations =
                                                                Int.MAX_VALUE,
                                                            initialDelayMillis =
                                                                600,
                                                            repeatDelayMillis =
                                                                900,
                                                            velocity =
                                                                20.dp
                                                        )
                                                } else {
                                                    Modifier
                                                }
                                        )

                                        /*
                                         * CURRENT CHANNEL
                                         */
                                        if (
                                            isSelected
                                        ) {

                                            Spacer(
                                                modifier =
                                                    Modifier.height(
                                                        4.dp
                                                    )
                                            )

                                            Row(
                                                verticalAlignment =
                                                    Alignment.CenterVertically
                                            ) {

                                                Box(
                                                    modifier =
                                                        Modifier
                                                            .width(
                                                                7.dp
                                                            )
                                                            .height(
                                                                7.dp
                                                            )
                                                            .clip(
                                                                RoundedCornerShape(
                                                                    999.dp
                                                                )
                                                            )
                                                            .background(
                                                                Primary
                                                            )
                                                )

                                                Spacer(
                                                    modifier =
                                                        Modifier.width(
                                                            5.dp
                                                        )
                                                )

                                                Text(
                                                    text =
                                                        stringResource(
                                                            R.string.player_channel_selected
                                                        ),
                                                    style =
                                                        MaterialTheme
                                                            .typography
                                                            .labelSmall,
                                                    color =
                                                        Primary,
                                                    fontWeight =
                                                        FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(
                                        modifier =
                                            Modifier.width(
                                                8.dp
                                            )
                                    )

                                    /*
                                     * ARCHIVE
                                     */
                                    if (
                                        channel
                                            .archivePlaybackCapability()
                                            .canBuildReplayCandidate
                                    ) {

                                        StatusPill(
                                            label =
                                                stringResource(
                                                    R.string.player_archive_badge
                                                ),
                                            containerColor =
                                                if (
                                                    isFocused
                                                ) {
                                                    Color.Black.copy(
                                                        alpha =
                                                            0.28f
                                                    )
                                                } else {
                                                    AppColors
                                                        .Warning
                                                },
                                            contentColor =
                                                if (
                                                    isFocused
                                                ) {
                                                    Color.White
                                                } else {
                                                    Color.Black
                                                }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            /*
             * ============================================================
             * TOP SCROLL INDICATOR
             * ============================================================
             */
            AnimatedVisibility(
                visible =
                    canScrollUp,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier =
                    Modifier.align(
                        Alignment.TopCenter
                    )
            ) {

                Box(
                    modifier =
                        Modifier
                            .width(52.dp)
                            .height(30.dp)
                            .clip(
                                RoundedCornerShape(
                                    bottomStart = 12.dp,
                                    bottomEnd = 12.dp
                                )
                            )
                            .background(
                                Color.Black.copy(
                                    alpha = 0.70f
                                )
                            ),
                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(
                        imageVector =
                            Icons.Outlined.KeyboardArrowUp,
                        contentDescription =
                            null,
                        tint =
                            Primary,
                        modifier =
                            Modifier.width(
                                22.dp
                            )
                    )
                }
            }

            /*
             * ============================================================
             * BOTTOM SCROLL INDICATOR
             * ============================================================
             */
            AnimatedVisibility(
                visible =
                    canScrollDown,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier =
                    Modifier.align(
                        Alignment.BottomCenter
                    )
            ) {

                Box(
                    modifier =
                        Modifier
                            .width(52.dp)
                            .height(30.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp
                                )
                            )
                            .background(
                                Color.Black.copy(
                                    alpha = 0.70f
                                )
                            ),
                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(
                        imageVector =
                            Icons.Outlined.KeyboardArrowDown,
                        contentDescription =
                            null,
                        tint =
                            Primary,
                        modifier =
                            Modifier.width(
                                22.dp
                            )
                    )
                }
            }

            /*
             * ============================================================
             * CATEGORY TAB
             * ============================================================
             */
            TvClickableSurface(
                onClick = {
                    onOverlayInteracted()
                    onOpenCategories()
                },
                modifier = Modifier
                    .align(
                        if (
                            isRtl
                        ) {
                            Alignment.CenterEnd
                        } else {
                            Alignment.CenterStart
                        }
                    )
                    .onFocusChanged {
                        if (
                            it.isFocused
                        ) {
                            onOverlayInteracted()
                        }
                    },
                shape =
                    ClickableSurfaceDefaults.shape(
                        RoundedCornerShape(
                            14.dp
                        )
                    ),
                colors =
                    ClickableSurfaceDefaults.colors(
                        containerColor =
                            AppColors
                                .SurfaceEmphasis
                                .copy(
                                    alpha = 0.94f
                                ),
                        focusedContainerColor =
                            Primary
                    )
            ) {

                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(100.dp),
                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(
                        imageVector =
                            Icons.Outlined.KeyboardArrowLeft,
                        contentDescription =
                            null,
                        tint =
                            Color.White.copy(
                                alpha = 0.92f
                            ),
                        modifier =
                            Modifier.width(
                                25.dp
                            )
                    )
                }
            }
        }
    }
}


@Composable
fun EpgOverlay(
    currentChannel: Channel?,
    displayChannelNumber: Int,
    currentProgram: Program?,
    nextProgram: Program?,
    upcomingPrograms: List<Program>,
    onDismiss: () -> Unit,
    overlayFocusRequester: FocusRequester = remember {
        FocusRequester()
    },
    onOpenFullGuide: (() -> Unit)? = null,
    onOpenArchiveBrowser: (() -> Unit)? = null,
    onOverlayInteracted: () -> Unit = {}
) {
    val appTimeFormat = LocalAppTimeFormat.current

    val timeFormat =
        remember(appTimeFormat) {
            appTimeFormat.createTimeFormat()
        }

    val listState =
        rememberLazyListState()

    val coroutineScope =
        rememberCoroutineScope()

    val layoutDirection =
        LocalLayoutDirection.current

    val openFullGuideKeyCode =
        if (
            layoutDirection ==
                LayoutDirection.Rtl
        ) {
            KeyEvent.KEYCODE_DPAD_LEFT
        } else {
            KeyEvent.KEYCODE_DPAD_RIGHT
        }

    val filteredUpcoming =
        remember(
            upcomingPrograms,
            currentProgram,
            nextProgram
        ) {
            upcomingPrograms.filter {
                it.id != currentProgram?.id &&
                    it.id != nextProgram?.id
            }
        }

    val displayPrograms =
        remember(
            filteredUpcoming,
            nextProgram
        ) {
            if (
                nextProgram != null
            ) {
                listOf(nextProgram) +
                    filteredUpcoming
            } else {
                filteredUpcoming
            }
        }

    BackHandler {
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(
                overlayFocusRequester
            )
            .focusable()
            .onPreviewKeyEvent {
                event ->

                if (
                    event.nativeKeyEvent.action !=
                        KeyEvent.ACTION_DOWN
                ) {
                    return@onPreviewKeyEvent false
                }

                if (
                    event.nativeKeyEvent.keyCode ==
                        openFullGuideKeyCode &&
                    onOpenFullGuide != null
                ) {
                    onOverlayInteracted()
                    onOpenFullGuide()
                    true
                } else {
                    when (
                        event.nativeKeyEvent.keyCode
                    ) {

                        KeyEvent.KEYCODE_DPAD_DOWN -> {
                            onOverlayInteracted()

                            coroutineScope.launch {

                                val nextIndex =
                                    (
                                        listState
                                            .firstVisibleItemIndex +
                                            1
                                    ).coerceAtMost(
                                        listState
                                            .layoutInfo
                                            .totalItemsCount -
                                            1
                                    )

                                if (
                                    nextIndex >= 0
                                ) {
                                    listState.animateScrollToItem(
                                        nextIndex,
                                        listState
                                            .firstVisibleItemScrollOffset
                                    )
                                }
                            }

                            true
                        }

                        KeyEvent.KEYCODE_DPAD_UP -> {
                            onOverlayInteracted()

                            coroutineScope.launch {

                                val previousIndex =
                                    (
                                        listState
                                            .firstVisibleItemIndex -
                                            1
                                    ).coerceAtLeast(
                                        0
                                    )

                                listState.animateScrollToItem(
                                    previousIndex,
                                    listState
                                        .firstVisibleItemScrollOffset
                                )
                            }

                            true
                        }

                        else -> false
                    }
                }
            }
            .background(
                Color.Black.copy(
                    alpha = 0.18f
                )
            )
    ) {

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .align(
                    Alignment.CenterEnd
                )
        ) {

            val isTelevisionDevice =
                rememberIsTelevisionDevice()

            val panelModifier =
                if (
                    maxWidth < 700.dp
                ) {
                    Modifier
                        .fillMaxWidth(0.9f)
                        .padding(24.dp)
                } else if (
                    !isTelevisionDevice &&
                    maxWidth < 1280.dp
                ) {
                    Modifier
                        .fillMaxWidth(0.54f)
                        .padding(24.dp)
                } else {
                    Modifier
                        .width(520.dp)
                        .padding(24.dp)
                }

            PlayerOverlayPanel(
                modifier =
                    panelModifier
            ) {

                LazyColumn(
                    state =
                        listState,
                    modifier =
                        Modifier.fillMaxSize(),
                    verticalArrangement =
                        Arrangement.spacedBy(
                            16.dp
                        )
                ) {

                    item {

                        Text(
                            text =
                                stringResource(
                                    R.string.epg_title
                                ),
                            style =
                                MaterialTheme
                                    .typography
                                    .titleMedium,
                            color =
                                Primary,
                            fontWeight =
                                FontWeight.Bold
                        )

                        if (
                            currentChannel != null
                        ) {

                            Spacer(
                                Modifier.height(
                                    8.dp
                                )
                            )

                            Text(
                                text =
                                    stringResource(
                                        R.string.channel_number_name_format,
                                        displayChannelNumber,
                                        currentChannel.name
                                    ),
                                style =
                                    MaterialTheme
                                        .typography
                                        .headlineSmall,
                                color =
                                    Color.White,
                                fontWeight =
                                    FontWeight.Bold
                            )

                            val archiveCapability =
                                currentChannel
                                    .archivePlaybackCapability()

                            if (
                                archiveCapability
                                    .canBuildReplayCandidate
                            ) {

                                val catchUpLabel =
                                    archiveCapability
                                        .windowDays
                                        ?.let {
                                            days ->
                                            stringResource(
                                                R.string.epg_catchup_available,
                                                days
                                            )
                                        }
                                        ?: stringResource(
                                            R.string.epg_catchup_available_unknown
                                        )

                                Spacer(
                                    Modifier.height(
                                        8.dp
                                    )
                                )

                                if (
                                    onOpenArchiveBrowser !=
                                        null
                                ) {

                                    QuickActionButton(
                                        icon =
                                            stringResource(
                                                R.string.player_catchup_badge
                                            ),
                                        label =
                                            catchUpLabel,
                                        onClick = {
                                            onOverlayInteracted()
                                            onOpenArchiveBrowser()
                                        },
                                        onInteraction =
                                            onOverlayInteracted
                                    )

                                } else {

                                    StatusPill(
                                        label =
                                            catchUpLabel,
                                        containerColor =
                                            AppColors
                                                .BrandMuted
                                    )
                                }
                            }
                        }

                        Spacer(
                            Modifier.height(
                                8.dp
                            )
                        )

                        Text(
                            text =
                                stringResource(
                                    R.string.player_epg_overlay_hint
                                ),
                            style =
                                MaterialTheme
                                    .typography
                                    .bodySmall,
                            color =
                                OnSurfaceDim
                        )
                    }

                    item {

                        androidx.compose.material3
                            .HorizontalDivider(
                                color =
                                    SurfaceVariant
                            )

                        Spacer(
                            Modifier.height(
                                8.dp
                            )
                        )

                        Text(
                            text =
                                stringResource(
                                    R.string.epg_now_playing
                                ),
                            style =
                                MaterialTheme
                                    .typography
                                    .labelMedium,
                            color =
                                Primary,
                            fontWeight =
                                FontWeight.Bold
                        )

                        Spacer(
                            Modifier.height(
                                8.dp
                            )
                        )

                        if (
                            currentProgram != null
                        ) {

                            Text(
                                currentProgram.title,
                                style =
                                    MaterialTheme
                                        .typography
                                        .titleMedium,
                                color =
                                    AppColors.TextPrimary,
                                fontWeight =
                                    FontWeight.Bold
                            )

                            Spacer(
                                Modifier.height(
                                    4.dp
                                )
                            )

                            Row(
                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {

                                Text(
                                    text =
                                        stringResource(
                                            R.string.time_range_format,
                                            timeFormat.format(
                                                Date(
                                                    currentProgram
                                                        .startTime
                                                )
                                            ),
                                            timeFormat.format(
                                                Date(
                                                    currentProgram
                                                        .endTime
                                                )
                                            )
                                        ),
                                    style =
                                        MaterialTheme
                                            .typography
                                            .bodyMedium,
                                    color =
                                        TextSecondary
                                )

                                Spacer(
                                    Modifier.width(
                                        12.dp
                                    )
                                )

                                Text(
                                    text =
                                        stringResource(
                                            R.string.label_duration_min,
                                            currentProgram
                                                .durationMinutes
                                        ),
                                    style =
                                        MaterialTheme
                                            .typography
                                            .bodySmall,
                                    color =
                                        OnSurfaceDim
                                )

                                if (
                                    currentProgram
                                        .lang
                                        .isNotEmpty()
                                ) {

                                    Spacer(
                                        Modifier.width(
                                            8.dp
                                        )
                                    )

                                    Text(
                                        text =
                                            currentProgram
                                                .lang
                                                .uppercase(),
                                        style =
                                            MaterialTheme
                                                .typography
                                                .labelSmall,
                                        color =
                                            Primary.copy(
                                                alpha =
                                                    0.7f
                                            )
                                    )
                                }
                            }

                            Spacer(
                                Modifier.height(
                                    4.dp
                                )
                            )

                            val now =
                                System.currentTimeMillis()

                            val start =
                                currentProgram
                                    .startTime

                            val end =
                                currentProgram
                                    .endTime

                            if (
                                start in 1..<end
                            ) {

                                val progress =
                                    (
                                        now - start
                                    ).toFloat() /
                                        (end - start)

                                val remainingMin =
                                    (
                                        (end - now) /
                                            60000
                                    ).toInt()
                                        .coerceAtLeast(
                                            0
                                        )

                                androidx.compose.material3
                                    .LinearProgressIndicator(
                                        progress = {
                                            progress.coerceIn(
                                                0f,
                                                1f
                                            )
                                        },
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .height(
                                                    4.dp
                                                ),
                                        color =
                                            Primary,
                                        trackColor =
                                            AppColors
                                                .SurfaceEmphasis
                                    )

                                Spacer(
                                    Modifier.height(
                                        4.dp
                                    )
                                )

                                Text(
                                    text =
                                        stringResource(
                                            R.string.player_minutes_remaining,
                                            remainingMin
                                        ),
                                    style =
                                        MaterialTheme
                                            .typography
                                            .bodySmall,
                                    color =
                                        OnSurfaceDim
                                )
                            }

                            if (
                                !currentProgram
                                    .description
                                    .isNullOrEmpty()
                            ) {

                                val description =
                                    currentProgram
                                        .description
                                        .orEmpty()

                                Spacer(
                                    Modifier.height(
                                        12.dp
                                    )
                                )

                                Text(
                                    description,
                                    style =
                                        MaterialTheme
                                            .typography
                                            .bodyMedium,
                                    color =
                                        Color.White.copy(
                                            alpha =
                                                0.7f
                                        ),
                                    maxLines =
                                        6,
                                    overflow =
                                        TextOverflow.Ellipsis
                                )
                            }

                        } else {

                            Text(
                                stringResource(
                                    R.string.epg_no_info
                                ),
                                color =
                                    OnSurfaceDim
                            )
                        }
                    }

                    if (
                        upcomingPrograms.isNotEmpty()
                    ) {

                        item {

                            Spacer(
                                Modifier.height(
                                    8.dp
                                )
                            )

                            androidx.compose.material3
                                .HorizontalDivider(
                                    color =
                                        SurfaceVariant
                                )

                            Spacer(
                                Modifier.height(
                                    8.dp
                                )
                            )

                            Text(
                                text =
                                    stringResource(
                                        R.string.epg_upcoming_schedule
                                    ),
                                style =
                                    MaterialTheme
                                        .typography
                                        .labelMedium,
                                color =
                                    Primary,
                                fontWeight =
                                    FontWeight.Bold
                            )
                        }

                        items(
                            displayPrograms.size
                        ) { index ->

                            val program =
                                displayPrograms[index]

                            val isNext =
                                index == 0 &&
                                    nextProgram != null

                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clip(
                                            RoundedCornerShape(
                                                12.dp
                                            )
                                        )
                                        .background(
                                            if (
                                                isNext
                                            ) {
                                                Primary.copy(
                                                    alpha =
                                                        0.08f
                                                )
                                            } else {
                                                Color.Transparent
                                            }
                                        )
                            ) {

                                Column(
                                    modifier =
                                        Modifier.padding(
                                            12.dp
                                        )
                                ) {

                                    if (
                                        isNext
                                    ) {

                                        Text(
                                            text =
                                                stringResource(
                                                    R.string.epg_up_next
                                                ),
                                            style =
                                                MaterialTheme
                                                    .typography
                                                    .labelSmall,
                                            color =
                                                Primary,
                                            fontWeight =
                                                FontWeight.Bold
                                        )

                                        Spacer(
                                            Modifier.height(
                                                4.dp
                                            )
                                        )
                                    }

                                    Text(
                                        text =
                                            program.title,
                                        style =
                                            MaterialTheme
                                                .typography
                                                .bodyLarge,
                                        color =
                                            if (
                                                isNext
                                            ) {
                                                Color.White
                                            } else {
                                                Color.White.copy(
                                                    alpha =
                                                        0.8f
                                                )
                                            },
                                        fontWeight =
                                            if (
                                                isNext
                                            ) {
                                                FontWeight.Bold
                                            } else {
                                                FontWeight.Normal
                                            },
                                        maxLines =
                                            1,
                                        overflow =
                                            TextOverflow
                                                .Ellipsis
                                    )

                                    Spacer(
                                        Modifier.height(
                                            2.dp
                                        )
                                    )

                                    Row {

                                        Text(
                                            text =
                                                stringResource(
                                                    R.string.time_range_format,
                                                    timeFormat.format(
                                                        Date(
                                                            program
                                                                .startTime
                                                        )
                                                    ),
                                                    timeFormat.format(
                                                        Date(
                                                            program
                                                                .endTime
                                                        )
                                                    )
                                                ),
                                            style =
                                                MaterialTheme
                                                    .typography
                                                    .bodySmall,
                                            color =
                                                TextSecondary
                                        )

                                        Spacer(
                                            Modifier.width(
                                                8.dp
                                            )
                                        )

                                        Text(
                                            text =
                                                stringResource(
                                                    R.string.label_duration_min,
                                                    program
                                                        .durationMinutes
                                                ),
                                            style =
                                                MaterialTheme
                                                    .typography
                                                    .bodySmall,
                                            color =
                                                OnSurfaceDim
                                        )

                                        if (
                                            program.hasArchive
                                        ) {

                                            Spacer(
                                                Modifier.width(
                                                    8.dp
                                                )
                                            )

                                            StatusPill(
                                                label =
                                                    stringResource(
                                                        R.string.player_archive_badge
                                                    ),
                                                containerColor =
                                                    AppColors
                                                        .Warning,
                                                contentColor =
                                                    Color.Black
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (
            onOpenFullGuide != null
        ) {

            Box(
                modifier =
                    Modifier
                        .align(
                            if (
                                layoutDirection ==
                                    LayoutDirection.Rtl
                            ) {
                                Alignment.CenterStart
                            } else {
                                Alignment.CenterEnd
                            }
                        )
                        .padding(
                            horizontal = 8.dp
                        )
                        .width(24.dp)
                        .height(72.dp)
                        .clip(
                            RoundedCornerShape(
                                8.dp
                            )
                        )
                        .background(
                            Primary.copy(
                                alpha = 0.58f
                            )
                        ),
                contentAlignment =
                    Alignment.Center
            ) {

                Icon(
                    imageVector =
                        Icons.Outlined.KeyboardArrowLeft,
                    contentDescription =
                        null,
                    tint =
                        Color.White.copy(
                            alpha = 0.92f
                        ),
                    modifier =
                        Modifier.width(
                            22.dp
                        )
                )
            }
        }
    }
}
@Composable
fun DiagnosticsOverlay(
    stats: PlayerStats,
    diagnostics: PlayerDiagnosticsUiState,
    modifier: Modifier = Modifier
) {
    val scrollState =
        rememberScrollState()

    val focusRequester =
        remember {
            FocusRequester()
        }

    val coroutineScope =
        rememberCoroutineScope()

    val canScrollUp by remember {
        derivedStateOf {
            scrollState.value > 0
        }
    }

    val canScrollDown by remember {
        derivedStateOf {
            scrollState.value <
                scrollState.maxValue
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    PlayerOverlayPanel(
        modifier =
            modifier.width(
                680.dp
            )
    ) {

        Box(
            modifier =
                Modifier
                    .heightIn(
                        max = 420.dp
                    )
                    .fillMaxWidth()
        ) {

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .focusRequester(
                            focusRequester
                        )
                        .focusable()
                        .onPreviewKeyEvent {
                            event ->

                            if (
                                event.nativeKeyEvent.action !=
                                    android.view.KeyEvent.ACTION_DOWN
                            ) {
                                return@onPreviewKeyEvent false
                            }

                            when (
                                event.nativeKeyEvent.keyCode
                            ) {

                                android.view.KeyEvent
                                    .KEYCODE_DPAD_UP -> {

                                    coroutineScope.launch {

                                        scrollState
                                            .animateScrollTo(
                                                (
                                                    scrollState.value -
                                                        120
                                                ).coerceAtLeast(
                                                    0
                                                )
                                            )
                                    }

                                    true
                                }

                                android.view.KeyEvent
                                    .KEYCODE_DPAD_DOWN -> {

                                    coroutineScope.launch {

                                        scrollState
                                            .animateScrollTo(
                                                (
                                                    scrollState.value +
                                                        120
                                                ).coerceAtMost(
                                                    scrollState.maxValue
                                                )
                                            )
                                    }

                                    true
                                }

                                else ->
                                    false
                            }
                        }
                        .verticalScroll(
                            scrollState
                        )
                        .padding(
                            top = 14.dp,
                            bottom = 14.dp
                        ),
                verticalArrangement =
                    Arrangement.spacedBy(
                        10.dp
                    )
            ) {

                Text(
                    text =
                        stringResource(
                            R.string.player_diagnostics_title
                        ),
                    color =
                        Primary,
                    style =
                        MaterialTheme
                            .typography
                            .labelMedium
                            .copy(
                                fontSize = 11.sp
                            ),
                    fontWeight =
                        FontWeight.Bold
                )

                val avSyncPathLabel =
                    when {

                        !diagnostics
                            .audioVideoSyncEnabled ->

                            stringResource(
                                R.string.player_diagnostics_av_sync_stock
                            )

                        diagnostics
                            .audioVideoSyncSinkActive ->

                            stringResource(
                                R.string.player_diagnostics_av_sync_custom
                            )

                        else ->

                            stringResource(
                                R.string.player_diagnostics_av_sync_waiting
                            )
                    }

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            20.dp
                        )
                ) {

                    Column(
                        modifier =
                            Modifier.fillMaxWidth(
                                0.48f
                            ),
                        verticalArrangement =
                            Arrangement.spacedBy(
                                3.dp
                            )
                    ) {

                        PlayerOverlaySectionLabel(
                            stringResource(
                                R.string.player_diagnostics_section_source
                            )
                        )

                        if (
                            diagnostics
                                .providerName
                                .isNotBlank()
                        ) {

                            PlayerMetaRow(
                                stringResource(
                                    R.string.player_diagnostics_provider
                                ),
                                diagnostics
                                    .providerName
                            )
                        }

                        if (
                            diagnostics
                                .providerSourceLabel
                                .isNotBlank()
                        ) {

                            PlayerMetaRow(
                                stringResource(
                                    R.string.player_diagnostics_source
                                ),
                                diagnostics
                                    .providerSourceLabel
                            )
                        }

                        PlayerOverlaySectionLabel(
                            stringResource(
                                R.string.player_diagnostics_section_playback
                            )
                        )

                        PlayerMetaRow(
                            stringResource(
                                R.string.player_diagnostics_audio_decoder_mode
                            ),
                            diagnostics
                                .audioDecoderMode
                                .name
                        )

                        PlayerMetaRow(
                            stringResource(
                                R.string.player_diagnostics_video_decoder_mode
                            ),
                            diagnostics
                                .videoDecoderMode
                                .name
                        )

                        PlayerMetaRow(
                            stringResource(
                                R.string.player_diagnostics_active_decoder
                            ),
                            diagnostics
                                .activeDecoderName
                        )

                        PlayerMetaRow(
                            stringResource(
                                R.string.player_diagnostics_surface
                            ),
                            diagnostics
                                .renderSurfaceType
                        )

                        PlayerMetaRow(
                            stringResource(
                                R.string.player_diagnostics_stream_class
                            ),
                            diagnostics
                                .streamClassLabel
                        )

                        PlayerMetaRow(
                            stringResource(
                                R.string.player_diagnostics_playback_state
                            ),
                            diagnostics
                                .playbackStateLabel
                        )

                        if (
                            diagnostics
                                .archiveSupportLabel
                                .isNotBlank()
                        ) {

                            PlayerMetaRow(
                                stringResource(
                                    R.string.player_diagnostics_archive
                                ),
                                diagnostics
                                    .archiveSupportLabel
                            )
                        }

                        PlayerMetaRow(
                            stringResource(
                                R.string.player_diagnostics_alternates
                            ),
                            diagnostics
                                .alternativeStreamCount
                                .toString()
                        )

                        if (
                            diagnostics
                                .channelErrorCount > 0
                        ) {

                            PlayerMetaRow(
                                stringResource(
                                    R.string.player_diagnostics_channel_errors
                                ),
                                diagnostics
                                    .channelErrorCount
                                    .toString()
                            )
                        }

                        PlayerOverlaySectionLabel(
                            stringResource(
                                R.string.player_diagnostics_section_video
                            )
                        )

                        PlayerMetaRow(
                            stringResource(
                                R.string.player_diagnostics_resolution
                            ),
                            "${stats.width}x${stats.height}"
                        )

                        PlayerMetaRow(
                            stringResource(
                                R.string.player_diagnostics_video_codec
                            ),
                            stats.videoCodec
                        )

                        PlayerMetaRow(
                            stringResource(
                                R.string.player_diagnostics_video_bitrate
                            ),
                            "${stats.videoBitrate / 1000} kbps"
                        )

                        PlayerMetaRow(
                            stringResource(
                                R.string.player_diagnostics_dropped_frames
                            ),
                            stats.droppedFrames.toString()
                        )

                        PlayerMetaRow(
                            stringResource(
                                R.string.player_diagnostics_video_stalls
                            ),
                            diagnostics
                                .videoStallCount
                                .toString()
                        )

                        if (
                            diagnostics
                                .lastVideoFrameAgoMs > 0L
                        ) {

                            PlayerMetaRow(
                                stringResource(
                                    R.string.player_diagnostics_last_frame
                                ),
                                "${diagnostics.lastVideoFrameAgoMs} ms"
                            )
                        }

                        if (
                            stats.ttffMs > 0L
                        ) {

                            PlayerMetaRow(
                                "TTFF",
                                "${stats.ttffMs} ms"
                            )
                        }
                    }

                    Column(
                        modifier =
                            Modifier.fillMaxWidth(
                                0.48f
                            ),
                        verticalArrangement =
                            Arrangement.spacedBy(
                                3.dp
                            )
                    ) {

                        PlayerOverlaySectionLabel(
                            stringResource(
                                R.string.player_diagnostics_section_audio
                            )
                        )

                        PlayerMetaRow(
                            stringResource(
                                R.string.player_diagnostics_audio_codec
                            ),
                            stats.audioCodec
                        )

                        PlayerMetaRow(
                            stringResource(
                                R.string.player_diagnostics_audio_decoder
                            ),
                            diagnostics
                                .activeAudioDecoderName
                        )

                        PlayerMetaRow(
                            stringResource(
                                R.string.player_diagnostics_audio_output_path
                            ),
                            diagnostics
                                .audioOutputPath
                        )

                        PlayerMetaRow(
                            stringResource(
                                R.string.player_diagnostics_ffmpeg
                            ),
                            if (
                                diagnostics
                                    .ffmpegAvailable
                            ) {

                                diagnostics
                                    .ffmpegVersion
                                    ?.let {
                                        "Available ($it)"
                                    }
                                    ?: "Available"

                            } else {
                                "Unavailable"
                            }
                        )

                        PlayerMetaRow(
                            stringResource(
                                R.string.player_diagnostics_compatibility_source
                            ),
                            diagnostics
                                .compatibilityDecisionSource
                        )

                        PlayerMetaRow(
                            stringResource(
                                R.string.player_diagnostics_av_sync
                            ),
                            avSyncPathLabel
                        )

                        PlayerMetaRow(
                            stringResource(
                                R.string.player_diagnostics_av_offset
                            ),
                            formatOffsetLabel(
                                diagnostics
                                    .audioVideoOffsetMs
                            )
                        )

                        if (
                            diagnostics
                                .compatibilityDecisionSource !=
                                "DEFAULT"
                        ) {

                            PlayerMetaRow(
                                stringResource(
                                    R.string.player_diagnostics_compatibility_note
                                ),
                                stringResource(
                                    R.string.player_diagnostics_compatibility_note_value
                                ),
                                maxLines = 3
                            )
                        }
                    }
                }

                PlayerOverlaySectionLabel(
                    stringResource(
                        R.string.player_diagnostics_section_recovery
                    )
                )

                diagnostics
                    .lastFailureReason
                    ?.let { reason ->

                        PlayerMetaRow(
                            stringResource(
                                R.string.player_diagnostics_last_failure
                            ),
                            reason,
                            maxLines = 3
                        )
                    }

                if (
                    diagnostics
                        .recentRecoveryActions
                        .isNotEmpty()
                ) {

                    Text(
                        text =
                            stringResource(
                                R.string.player_diagnostics_recovery_actions
                            ),
                        color =
                            Primary,
                        style =
                            MaterialTheme
                                .typography
                                .labelSmall
                                .copy(
                                    fontSize = 10.sp
                                ),
                        fontWeight =
                            FontWeight.Bold
                    )

                    diagnostics
                        .recentRecoveryActions
                        .forEach { action ->

                            Text(
                                text =
                                    action,
                                color =
                                    AppColors
                                        .TextSecondary,
                                style =
                                    MaterialTheme
                                        .typography
                                        .labelSmall
                                        .copy(
                                            fontSize =
                                                10.sp
                                        ),
                                maxLines =
                                    1,
                                overflow =
                                    TextOverflow
                                        .Ellipsis
                            )
                        }
                }

                if (
                    diagnostics
                        .troubleshootingHints
                        .isNotEmpty()
                ) {

                    Text(
                        text =
                            stringResource(
                                R.string.player_diagnostics_troubleshooting
                            ),
                        color =
                            Primary,
                        style =
                            MaterialTheme
                                .typography
                                .labelSmall
                                .copy(
                                    fontSize = 10.sp
                                ),
                        fontWeight =
                            FontWeight.Bold
                    )

                    diagnostics
                        .troubleshootingHints
                        .forEach { hint ->

                            Text(
                                text =
                                    hint,
                                color =
                                    AppColors
                                        .TextSecondary,
                                style =
                                    MaterialTheme
                                        .typography
                                        .labelSmall
                                        .copy(
                                            fontSize =
                                                10.sp
                                        ),
                                maxLines =
                                    1,
                                overflow =
                                    TextOverflow
                                        .Ellipsis
                            )
                        }
                }
            }

            if (canScrollUp) {
    DiagnosticsScrollCue(
        label = "↑"
    )
}

if (canScrollDown) {
    DiagnosticsScrollCue(
        label = "↓"
    )
}
        }
    }
}


@Composable
private fun DiagnosticsScrollCue(
    label: String
) {
    Box(
        modifier =
            Modifier
                .clip(
                    RoundedCornerShape(
                        999.dp
                    )
                )
                .background(
                    AppColors.Canvas.copy(
                        alpha = 0.78f
                    )
                )
                .padding(
                    horizontal = 10.dp,
                    vertical = 3.dp
                )
    ) {

        Text(
            text =
                label,
            color =
                AppColors.TextSecondary,
            style =
                MaterialTheme
                    .typography
                    .labelSmall
                    .copy(
                        fontSize = 11.sp
                    ),
            fontWeight =
                FontWeight.Bold,
            textAlign =
                TextAlign.Center
        )
    }
}


private fun formatOffsetLabel(
    offsetMs: Int
): String =
    when {

        offsetMs > 0 ->
            "+$offsetMs ms"

        offsetMs < 0 ->
            "$offsetMs ms"

        else ->
            "0 ms"
    }


@Composable
fun CategoryListOverlay(
    categories:
        List<com.streamvault.domain.model.Category>,
    currentCategoryId: Long,
    overlayFocusRequester:
        FocusRequester = remember {
            FocusRequester()
        },
    isCategoryLocked:
        (
            com.streamvault.domain.model.Category
        ) -> Boolean = {
            false
        },
    onSelectCategory:
        (
            com.streamvault.domain.model.Category
        ) -> Unit,
    onDismiss: () -> Unit,
    onOverlayInteracted: () -> Unit = {}
) {

    val listState =
        rememberLazyListState()

    val currentIndex =
        remember(
            categories,
            currentCategoryId
        ) {

            categories
                .indexOfFirst {
                    it.id ==
                        currentCategoryId
                }
                .coerceAtLeast(0)
        }

    val canScrollUp by remember {
        derivedStateOf {
            listState.canScrollBackward
        }
    }

    val canScrollDown by remember {
        derivedStateOf {
            listState.canScrollForward
        }
    }

    LaunchedEffect(
        categories,
        currentIndex
    ) {

        if (
            categories.isNotEmpty()
        ) {

            listState.scrollToItem(
                currentIndex
            )
        }
    }

    BackHandler {
        onDismiss()
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Color.Black.copy(
                        alpha = 0.28f
                    )
                )
    ) {

        BoxWithConstraints(
            modifier =
                Modifier.fillMaxSize()
        ) {

            val isTelevisionDevice =
                rememberIsTelevisionDevice()

            val panelModifier =
                when {

                    maxWidth < 700.dp -> {

                        Modifier
                            .fillMaxWidth(0.94f)
                            .fillMaxHeight()
                            .padding(14.dp)
                    }

                    !isTelevisionDevice &&
                        maxWidth < 1280.dp -> {

                        Modifier
                            .fillMaxWidth(0.58f)
                            .fillMaxHeight()
                            .padding(18.dp)
                    }

                    else -> {

                        Modifier
                            .width(620.dp)
                            .fillMaxHeight()
                            .padding(18.dp)
                    }
                }

            Box(
                modifier =
                    panelModifier
            ) {

                PlayerOverlayPanel(
                    modifier =
                        Modifier.fillMaxSize()
                ) {

                    LazyColumn(
                        state =
                            listState,
                        modifier =
                            Modifier.fillMaxSize(),
                        contentPadding =
                            PaddingValues(
                                top = 10.dp,
                                bottom = 22.dp,
                                start = 8.dp,
                                end = 8.dp
                            ),
                        verticalArrangement =
                            Arrangement.spacedBy(
                                7.dp
                            )
                    ) {

                        /*
                         * ==================================================
                         * CATEGORY HEADER
                         * ==================================================
                         */
                        item {

                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = 8.dp,
                                            vertical = 10.dp
                                        ),
                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {

                                Box(
                                    modifier =
                                        Modifier
                                            .width(
                                                46.dp
                                            )
                                            .height(
                                                46.dp
                                            )
                                            .clip(
                                                RoundedCornerShape(
                                                    14.dp
                                                )
                                            )
                                            .background(
                                                Primary.copy(
                                                    alpha =
                                                        0.12f
                                                )
                                            )
                                            .border(
                                                1.dp,
                                                Primary.copy(
                                                    alpha =
                                                        0.32f
                                                ),
                                                RoundedCornerShape(
                                                    14.dp
                                                )
                                            ),
                                    contentAlignment =
                                        Alignment.Center
                                ) {

                                    Icon(
                                        imageVector =
                                            Icons
                                                .Outlined
                                                .Folder,
                                        contentDescription =
                                            null,
                                        tint =
                                            Primary,
                                        modifier =
                                            Modifier.width(
                                                25.dp
                                            )
                                    )
                                }

                                Spacer(
                                    modifier =
                                        Modifier.width(
                                            14.dp
                                        )
                                )

                                Column(
                                    modifier =
                                        Modifier.weight(
                                            1f
                                        )
                                ) {

                                    Text(
                                        text =
                                            stringResource(
                                                R.string.label_categories
                                            ),
                                        style =
                                            MaterialTheme
                                                .typography
                                                .titleLarge,
                                        color =
                                            Color.White,
                                        fontWeight =
                                            FontWeight.Bold
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.height(
                                                3.dp
                                            )
                                    )

                                    Text(
                                        text =
                                            "اختر الفئة لعرض القنوات",
                                        style =
                                            MaterialTheme
                                                .typography
                                                .bodySmall,
                                        color =
                                            OnSurfaceDim
                                    )
                                }

                                Box(
                                    modifier =
                                        Modifier
                                            .clip(
                                                RoundedCornerShape(
                                                    12.dp
                                                )
                                            )
                                            .background(
                                                Primary.copy(
                                                    alpha =
                                                        0.12f
                                                )
                                            )
                                            .border(
                                                1.dp,
                                                Primary.copy(
                                                    alpha =
                                                        0.30f
                                                ),
                                                RoundedCornerShape(
                                                    12.dp
                                                )
                                            )
                                            .padding(
                                                horizontal =
                                                    13.dp,
                                                vertical =
                                                    9.dp
                                            )
                                ) {

                                    Text(
                                        text =
                                            categories
                                                .size
                                                .toString(),
                                        style =
                                            MaterialTheme
                                                .typography
                                                .titleMedium,
                                        color =
                                            Primary,
                                        fontWeight =
                                            FontWeight.Bold
                                    )
                                }
                            }

                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal =
                                                8.dp
                                        )
                                        .height(
                                            1.dp
                                        )
                                        .background(
                                            Brush.horizontalGradient(
                                                colors =
                                                    listOf(
                                                        Primary.copy(
                                                            alpha =
                                                                0.85f
                                                        ),
                                                        Primary.copy(
                                                            alpha =
                                                                0.25f
                                                        ),
                                                        Color.Transparent
                                                    )
                                            )
                                        )
                            )
                        }

                        /*
                         * ==================================================
                         * CATEGORIES
                         * ==================================================
                         */
                        items(
                            count =
                                categories.size,
                            key = { index ->
                                categories[index]
                                    .id
                            }
                        ) { index ->

                            val category =
                                categories[index]

                            val isSelected =
                                category.id ==
                                    currentCategoryId

                            val isLocked =
                                isCategoryLocked(
                                    category
                                )

                            var isFocused by
                                remember {
                                    mutableStateOf(
                                        false
                                    )
                                }

                            val backgroundColor =
                                when {

                                    isFocused ->
                                        Primary.copy(
                                            alpha =
                                                0.18f
                                        )

                                    isSelected ->
                                        Primary.copy(
                                            alpha =
                                                0.08f
                                        )

                                    else ->
                                        AppColors
                                            .Surface
                                            .copy(
                                                alpha =
                                                    0.74f
                                            )
                                }

                            val borderColor =
                                when {

                                    isFocused ->
                                        Primary.copy(
                                            alpha =
                                                0.95f
                                        )

                                    isSelected ->
                                        Primary.copy(
                                            alpha =
                                                0.40f
                                        )

                                    else ->
                                        Color.White.copy(
                                            alpha =
                                                0.055f
                                        )
                                }

                            TvClickableSurface(
                                onClick = {

                                    onOverlayInteracted()

                                    onSelectCategory(
                                        category
                                    )
                                },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .onFocusChanged {
                                            focusState ->

                                            isFocused =
                                                focusState
                                                    .isFocused

                                            if (
                                                focusState
                                                    .isFocused
                                            ) {

                                                onOverlayInteracted()
                                            }
                                        }
                                        .then(
                                            if (
                                                isSelected
                                            ) {

                                                Modifier
                                                    .focusRequester(
                                                        overlayFocusRequester
                                                    )

                                            } else {

                                                Modifier
                                            }
                                        ),
                                scale =
                                    ClickableSurfaceDefaults
                                        .scale(
                                            focusedScale =
                                                1.015f
                                        ),
                                shape =
                                    ClickableSurfaceDefaults
                                        .shape(
                                            RoundedCornerShape(
                                                16.dp
                                            )
                                        ),
                                colors =
                                    ClickableSurfaceDefaults
                                        .colors(
                                            containerColor =
                                                backgroundColor,
                                            focusedContainerColor =
                                                backgroundColor
                                        )
                            ) {

                                Row(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .border(
                                                width =
                                                    if (
                                                        isFocused
                                                    ) {
                                                        2.dp
                                                    } else {
                                                        1.dp
                                                    },
                                                color =
                                                    borderColor,
                                                shape =
                                                    RoundedCornerShape(
                                                        16.dp
                                                    )
                                            )
                                            .padding(
                                                horizontal =
                                                    14.dp,
                                                vertical =
                                                    12.dp
                                            ),
                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {

                                    /*
                                     * FOLDER
                                     */
                                    Box(
                                        modifier =
                                            Modifier
                                                .width(
                                                    42.dp
                                                )
                                                .height(
                                                    42.dp
                                                )
                                                .clip(
                                                    RoundedCornerShape(
                                                        12.dp
                                                    )
                                                )
                                                .background(
                                                    if (
                                                        isFocused
                                                    ) {

                                                        Primary.copy(
                                                            alpha =
                                                                0.18f
                                                        )

                                                    } else {

                                                        Color.Black.copy(
                                                            alpha =
                                                                0.28f
                                                        )
                                                    }
                                                ),
                                        contentAlignment =
                                            Alignment.Center
                                    ) {

                                        Icon(
                                            imageVector =
                                                Icons
                                                    .Outlined
                                                    .Folder,
                                            contentDescription =
                                                null,
                                            tint =
                                                if (
                                                    isFocused
                                                ) {

                                                    Primary

                                                } else {

                                                    Color.White.copy(
                                                        alpha =
                                                            0.62f
                                                    )
                                                },
                                            modifier =
                                                Modifier.width(
                                                    23.dp
                                                )
                                        )
                                    }

                                    Spacer(
                                        modifier =
                                            Modifier.width(
                                                13.dp
                                            )
                                    )

                                    /*
                                     * CATEGORY NAME
                                     */
                                    Column(
                                        modifier =
                                            Modifier.weight(
                                                1f
                                            )
                                    ) {

                                        Text(
                                            text =
                                                category.name,
                                            style =
                                                MaterialTheme
                                                    .typography
                                                    .bodyLarge
                                                    .copy(
                                                        fontSize =
                                                            17.sp
                                                    ),
                                            color =
                                                Color.White,
                                            fontWeight =
                                                if (
                                                    isFocused ||
                                                    isSelected
                                                ) {

                                                    FontWeight.Bold

                                                } else {

                                                    FontWeight.Normal
                                                },
                                            maxLines =
                                                1,
                                            overflow =
                                                TextOverflow
                                                    .Ellipsis
                                        )

                                        if (
                                            isSelected
                                        ) {

                                            Spacer(
                                                modifier =
                                                    Modifier.height(
                                                        3.dp
                                                    )
                                            )

                                            Text(
                                                text =
                                                    "الفئة الحالية",
                                                style =
                                                    MaterialTheme
                                                        .typography
                                                        .labelSmall,
                                                color =
                                                    Primary,
                                                fontWeight =
                                                    FontWeight.Bold
                                            )
                                        }
                                    }

                                    /*
                                     * LOCK
                                     */
                                    if (
                                        isLocked
                                    ) {

                                        Icon(
                                            imageVector =
                                                Icons
                                                    .Outlined
                                                    .Lock,
                                            contentDescription =
                                                null,
                                            tint =
                                                Color.White.copy(
                                                    alpha =
                                                        0.60f
                                                ),
                                            modifier =
                                                Modifier.width(
                                                    20.dp
                                                )
                                        )

                                        Spacer(
                                            modifier =
                                                Modifier.width(
                                                    10.dp
                                                )
                                        )
                                    }

                                    /*
                                     * CHANNEL COUNT
                                     */
                                    if (
                                        category.count > 0
                                    ) {

                                        Box(
                                            modifier =
                                                Modifier
                                                    .clip(
                                                        RoundedCornerShape(
                                                            10.dp
                                                        )
                                                    )
                                                    .background(
                                                        Color.Black.copy(
                                                            alpha =
                                                                0.24f
                                                        )
                                                    )
                                                    .border(
                                                        1.dp,
                                                        if (
                                                            isFocused
                                                        ) {

                                                            Color.White.copy(
                                                                alpha =
                                                                    0.16f
                                                            )

                                                        } else {

                                                            Color.White.copy(
                                                                alpha =
                                                                    0.06f
                                                            )
                                                        },
                                                        RoundedCornerShape(
                                                            10.dp
                                                        )
                                                    )
                                                    .padding(
                                                        horizontal =
                                                            10.dp,
                                                        vertical =
                                                            7.dp
                                                    )
                                        ) {

                                            Text(
                                                text =
                                                    category
                                                        .count
                                                        .toString(),
                                                style =
                                                    MaterialTheme
                                                        .typography
                                                        .labelMedium,
                                                color =
                                                    if (
                                                        isFocused
                                                    ) {

                                                        Color.White

                                                    } else {

                                                        Color.White.copy(
                                                            alpha =
                                                                0.58f
                                                        )
                                                    },
                                                fontWeight =
                                                    FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            /*
             * ============================================================
             * CATEGORY SCROLL CUES
             * ============================================================
             */

            AnimatedVisibility(
                visible =
                    canScrollUp,
                enter =
                    fadeIn(),
                exit =
                    fadeOut(),
                modifier =
                    Modifier.align(
                        Alignment.TopCenter
                    )
            ) {

                Box(
                    modifier =
                        Modifier
                            .width(52.dp)
                            .height(30.dp)
                            .clip(
                                RoundedCornerShape(
                                    bottomStart =
                                        12.dp,
                                    bottomEnd =
                                        12.dp
                                )
                            )
                            .background(
                                Color.Black.copy(
                                    alpha =
                                        0.70f
                                )
                            ),
                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(
                        imageVector =
                            Icons
                                .Outlined
                                .KeyboardArrowUp,
                        contentDescription =
                            null,
                        tint =
                            Primary,
                        modifier =
                            Modifier.width(
                                22.dp
                            )
                    )
                }
            }

            AnimatedVisibility(
                visible =
                    canScrollDown,
                enter =
                    fadeIn(),
                exit =
                    fadeOut(),
                modifier =
                    Modifier.align(
                        Alignment.BottomCenter
                    )
            ) {

                Box(
                    modifier =
                        Modifier
                            .width(52.dp)
                            .height(30.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart =
                                        12.dp,
                                    topEnd =
                                        12.dp
                                )
                            )
                            .background(
                                Color.Black.copy(
                                    alpha =
                                        0.70f
                                )
                            ),
                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(
                        imageVector =
                            Icons
                                .Outlined
                                .KeyboardArrowDown,
                        contentDescription =
                            null,
                        tint =
                            Primary,
                        modifier =
                            Modifier.width(
                                22.dp
                            )
                    )
                }
            }
        }
    }
}