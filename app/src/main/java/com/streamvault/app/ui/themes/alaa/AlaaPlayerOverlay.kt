package com.streamvault.app.ui.themes.alaa

import com.streamvault.app.ui.screens.player.SeekPreviewState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay


/*
 * ============================================================
 * ALAA IPTV PLAYER DESIGN SYSTEM
 * ============================================================
 */

private object AlaaPlayerTokens {

    // Main cinematic orange
    val Accent = Color(0xFFFF8A00)
    val AccentLight = Color(0xFFFFA52F)
    val AccentDark = Color(0xFFE86F00)

    // Backgrounds
    val Background = Color(0xFF050505)
    val Surface = Color(0xFF0D0D0D)
    val SurfaceElevated = Color(0xFF151515)
    val SurfaceGlass = Color(0xD90D0D0D)
    val SurfaceSoft = Color(0x99151515)

    // Borders
    val Border = Color.White.copy(alpha = 0.075f)
    val BorderStrong = Color.White.copy(alpha = 0.13f)

    // Text
    val TextPrimary = Color.White
    val TextSecondary = Color.White.copy(alpha = 0.68f)
    val TextMuted = Color.White.copy(alpha = 0.44f)

    // Focus
    val FocusBackground = Color.White.copy(alpha = 0.11f)
    val FocusBorder = Accent

    // Sizes
    val PrimaryButtonSize = 136.dp
    val SecondaryButtonSize = 92.dp
    val BottomPanelRadius = 26.dp
    val ControlRadius = 18.dp
    val SettingsWidth = 350.dp
}


/*
 * ============================================================
 * SEEK PREVIEW STATE
 *
 * إذا كان عندك SeekPreviewState معرف بملف آخر:
 * احذف هذا التعريف من هنا.
 * ============================================================
 */
/*
 * ============================================================
 * MAIN PLAYER OVERLAY
 * ============================================================
 */

@Composable
internal fun AlaaPlayerOverlay(
    visible: Boolean,
    title: String,
    contentType: String,
    mediaTitle: String?,
    seriesTitle: String?,
    episodeTitle: String?,
    seasonNumber: Int?,
    episodeNumber: Int?,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    subtitleTrackCount: Int,
    audioTrackCount: Int,
    videoQualityCount: Int,
    isLocked: Boolean,
    showEpisodesAction: Boolean,
    showSettings: Boolean,
    playButtonFocusRequester: FocusRequester,
    lockButtonFocusRequester: FocusRequester,
    settingsCloseFocusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekBackward: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekToPosition: (Long) -> Unit,
    onSetScrubbingMode: (Boolean) -> Unit,
    onSeekPreviewPositionChanged: (Long?) -> Unit,
    seekPreview: SeekPreviewState,
    onOpenSubtitleTracks: () -> Unit,
    onOpenAudioTracks: () -> Unit,
    onOpenEpisodes: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismissSettings: () -> Unit,
    onOpenVideoTracks: () -> Unit,
    onOpenPlaybackSpeed: () -> Unit,
    onToggleAspectRatio: () -> Unit,
    onToggleLock: () -> Unit,
    onUserInteraction: () -> Unit
) {
    if (!visible) return

    val backgroundBrush = remember {
        Brush.verticalGradient(
            colorStops = arrayOf(
                0.00f to Color.Black.copy(alpha = 0.86f),
                0.12f to Color.Black.copy(alpha = 0.52f),
                0.27f to Color.Black.copy(alpha = 0.16f),
                0.43f to Color.Transparent,
                0.60f to Color.Black.copy(alpha = 0.05f),
                0.76f to Color.Black.copy(alpha = 0.47f),
                0.88f to Color.Black.copy(alpha = 0.78f),
                1.00f to Color.Black.copy(alpha = 0.96f)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {

        /*
         * ========================================================
         * TOP BAR
         * ========================================================
         */

        AlaaPlayerTopBar(
            title = title,
            contentType = contentType,
            mediaTitle = mediaTitle,
            seriesTitle = seriesTitle,
            episodeTitle = episodeTitle,
            seasonNumber = seasonNumber,
            episodeNumber = episodeNumber,
            onBack = {
                onUserInteraction()
                onBack()
            }
        )


        /*
         * ========================================================
         * CENTER TRANSPORT CONTROLS
         * ========================================================
         */

        if (!isLocked && !showSettings) {

            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(58.dp)
            ) {

                AlaaTransportButton(
                    icon = Icons.Default.Replay10,
                    contentDescription = "Rewind 10 seconds",
                    onClick = {
                        onUserInteraction()
                        onSeekBackward()
                    }
                )

                AlaaPrimaryPlayButton(
                    isPlaying = isPlaying,
                    focusRequester = playButtonFocusRequester,
                    onClick = {
                        onUserInteraction()
                        onTogglePlayPause()
                    }
                )

                AlaaTransportButton(
                    icon = Icons.Default.Forward10,
                    contentDescription = "Forward 10 seconds",
                    onClick = {
                        onUserInteraction()
                        onSeekForward()
                    }
                )
            }
        }


        /*
         * ========================================================
         * BOTTOM CONTROL PANEL
         * ========================================================
         */

        if (!isLocked && !showSettings) {

            AlaaPlayerBottomPanel(
                modifier = Modifier.align(Alignment.BottomCenter),
                currentPosition = currentPosition,
                duration = duration,
                subtitleTrackCount = subtitleTrackCount,
                audioTrackCount = audioTrackCount,
                videoQualityCount = videoQualityCount,
                showEpisodesAction = showEpisodesAction,
                seekPreview = seekPreview,
                lockButtonFocusRequester = lockButtonFocusRequester,

                onSeekToPosition = onSeekToPosition,
                onSetScrubbingMode = onSetScrubbingMode,
                onSeekPreviewPositionChanged = onSeekPreviewPositionChanged,

                onOpenSubtitleTracks = {
                    onUserInteraction()
                    onOpenSubtitleTracks()
                },

                onOpenAudioTracks = {
                    onUserInteraction()
                    onOpenAudioTracks()
                },

                onOpenEpisodes = {
                    onUserInteraction()
                    onOpenEpisodes()
                },

                onOpenSettings = {
                    onUserInteraction()
                    onOpenSettings()
                },

                onOpenVideoTracks = {
                    onUserInteraction()
                    onOpenVideoTracks()
                },

                onToggleLock = {
                    onUserInteraction()
                    onToggleLock()
                }
            )
        }


        /*
         * ========================================================
         * SETTINGS PANEL
         * ========================================================
         */

        if (showSettings) {

            AlaaPlayerSettingsPanel(
                modifier = Modifier.align(Alignment.CenterEnd),

                subtitleTrackCount = subtitleTrackCount,
                audioTrackCount = audioTrackCount,
                videoQualityCount = videoQualityCount,

                closeFocusRequester = settingsCloseFocusRequester,

                onDismiss = {
                    onUserInteraction()
                    onDismissSettings()
                },

                onOpenSubtitleTracks = {
                    onUserInteraction()
                    onOpenSubtitleTracks()
                },

                onOpenAudioTracks = {
                    onUserInteraction()
                    onOpenAudioTracks()
                },

                onOpenVideoTracks = {
                    onUserInteraction()
                    onOpenVideoTracks()
                },

                onOpenPlaybackSpeed = {
                    onUserInteraction()
                    onOpenPlaybackSpeed()
                },

                onToggleAspectRatio = {
                    onUserInteraction()
                    onToggleAspectRatio()
                }
            )
        }


        /*
         * ========================================================
         * LOCKED MESSAGE
         * ========================================================
         */

        if (isLocked && !showSettings) {

            AlaaLockedMessage(
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}


/*
 * ============================================================
 * TOP BAR
 * ============================================================
 */

@Composable
private fun AlaaPlayerTopBar(
    title: String,
    contentType: String,
    mediaTitle: String?,
    seriesTitle: String?,
    episodeTitle: String?,
    seasonNumber: Int?,
    episodeNumber: Int?,
    onBack: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 30.dp,
                end = 34.dp,
                top = 25.dp
            ),
        verticalAlignment = Alignment.Top
    ) {

        AlaaBackButton(
            onClick = onBack
        )

        Spacer(
            modifier = Modifier.width(22.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 4.dp)
        ) {

            val mainTitle = when {
                !mediaTitle.isNullOrBlank() -> mediaTitle
                !seriesTitle.isNullOrBlank() -> seriesTitle
                title.isNotBlank() -> title
                else -> "Player"
            }

            val secondaryTitle = when {

                !episodeTitle.isNullOrBlank() -> buildString {

                    append(episodeTitle)

                    if (seasonNumber != null || episodeNumber != null) {

                        append("  •  ")

                        if (seasonNumber != null) {
                            append("S$seasonNumber")
                        }

                        if (episodeNumber != null) {
                            append(" E$episodeNumber")
                        }
                    }
                }

                contentType.isNotBlank() -> contentType

                else -> null
            }

            Text(
                text = mainTitle,
                color = AlaaPlayerTokens.TextPrimary,
                fontSize = 27.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (!secondaryTitle.isNullOrBlank()) {

                Spacer(
                    modifier = Modifier.height(5.dp)
                )

                Text(
                    text = secondaryTitle,
                    color = AlaaPlayerTokens.TextSecondary,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        AlaaClock()
    }
}


/*
 * ============================================================
 * BACK BUTTON
 * ============================================================
 */

@Composable
private fun AlaaBackButton(
    onClick: () -> Unit
) {

    var focused by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier
            .size(
                width = 88.dp,
                height = 70.dp
            )
            .clip(
                RoundedCornerShape(
                    AlaaPlayerTokens.ControlRadius
                )
            )
            .background(
                if (focused) {
                    AlaaPlayerTokens.FocusBackground
                } else {
                    Color.Black.copy(alpha = 0.28f)
                }
            )
            .border(
                width = if (focused) 2.dp else 1.dp,
                color = if (focused) {
                    AlaaPlayerTokens.FocusBorder
                } else {
                    AlaaPlayerTokens.Border
                },
                shape = RoundedCornerShape(
                    AlaaPlayerTokens.ControlRadius
                )
            )
            .onFocusChanged {
                focused = it.isFocused
            }
            .focusable()
            .clickable(
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {

        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back",
            tint = Color.White,
            modifier = Modifier.size(30.dp)
        )
    }
}


/*
 * ============================================================
 * CLOCK
 * ============================================================
 */

@Composable
private fun AlaaClock() {

    val currentTime by produceState(
        initialValue = Date()
    ) {

        while (true) {

            value = Date()

            delay(1000)
        }
    }

    val timeFormatter = remember {
        SimpleDateFormat(
            "HH:mm",
            Locale.getDefault()
        )
    }

    val dateFormatter = remember {
        SimpleDateFormat(
            "EEE, dd MMM",
            Locale.getDefault()
        )
    }

    Column(
        horizontalAlignment = Alignment.End
    ) {

        Text(
            text = timeFormatter.format(currentTime),
            color = Color.White,
            fontSize = 27.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(
            modifier = Modifier.height(3.dp)
        )

        Text(
            text = dateFormatter.format(currentTime),
            color = AlaaPlayerTokens.TextMuted,
            fontSize = 13.sp
        )
    }
}


/*
 * ============================================================
 * TRANSPORT BUTTON
 * ============================================================
 */

@Composable
private fun AlaaTransportButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {

    var focused by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier
            .size(
                AlaaPlayerTokens.SecondaryButtonSize
            )
            .shadow(
                elevation = if (focused) 20.dp else 10.dp,
                shape = CircleShape
            )
            .clip(CircleShape)
            .background(
                if (focused) {
                    Color.White.copy(alpha = 0.12f)
                } else {
                    Color.Black.copy(alpha = 0.42f)
                }
            )
            .border(
                width = if (focused) 2.dp else 1.dp,
                color = if (focused) {
                    AlaaPlayerTokens.FocusBorder
                } else {
                    AlaaPlayerTokens.Border
                },
                shape = CircleShape
            )
            .onFocusChanged {
                focused = it.isFocused
            }
            .focusable()
            .clickable(
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {

        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(42.dp)
        )
    }
}


/*
 * ============================================================
 * PRIMARY PLAY BUTTON
 * ============================================================
 */

@Composable
private fun AlaaPrimaryPlayButton(
    isPlaying: Boolean,
    focusRequester: FocusRequester,
    onClick: () -> Unit
) {

    var focused by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier
            .size(
                AlaaPlayerTokens.PrimaryButtonSize
            )
            .shadow(
                elevation = if (focused) 34.dp else 24.dp,
                shape = CircleShape
            )
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        AlaaPlayerTokens.AccentLight,
                        AlaaPlayerTokens.Accent,
                        AlaaPlayerTokens.AccentDark
                    )
                )
            )
            .border(
                width = if (focused) 4.dp else 2.dp,
                color = if (focused) {
                    Color.White
                } else {
                    Color.White.copy(alpha = 0.18f)
                },
                shape = CircleShape
            )
            .focusRequester(focusRequester)
            .onFocusChanged {
                focused = it.isFocused
            }
            .focusable()
            .clickable(
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .size(114.dp)
                .clip(CircleShape)
                .background(
                    Color.Black.copy(alpha = 0.09f)
                ),
            contentAlignment = Alignment.Center
        ) {

            Icon(
                imageVector = if (isPlaying) {
                    Icons.Default.Pause
                } else {
                    Icons.Default.PlayArrow
                },
                contentDescription = if (isPlaying) {
                    "Pause"
                } else {
                    "Play"
                },
                tint = Color.White,
                modifier = Modifier.size(62.dp)
            )
        }
    }
}


/*
 * ============================================================
 * BOTTOM PANEL
 * ============================================================
 */

@Composable
private fun AlaaPlayerBottomPanel(
    modifier: Modifier = Modifier,
    currentPosition: Long,
    duration: Long,
    subtitleTrackCount: Int,
    audioTrackCount: Int,
    videoQualityCount: Int,
    showEpisodesAction: Boolean,
    seekPreview: SeekPreviewState,
    lockButtonFocusRequester: FocusRequester,

    onSeekToPosition: (Long) -> Unit,
    onSetScrubbingMode: (Boolean) -> Unit,
    onSeekPreviewPositionChanged: (Long?) -> Unit,

    onOpenSubtitleTracks: () -> Unit,
    onOpenAudioTracks: () -> Unit,
    onOpenEpisodes: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenVideoTracks: () -> Unit,
    onToggleLock: () -> Unit
) {

    Box(
        modifier = modifier
            .fillMaxWidth(0.78f)
            .widthIn(
                min = 900.dp,
                max = 1180.dp
            )
            .padding(
                horizontal = 16.dp,
                vertical = 28.dp
            )
            .clip(
                RoundedCornerShape(
                    AlaaPlayerTokens.BottomPanelRadius
                )
            )
            .background(
                AlaaPlayerTokens.SurfaceGlass
            )
            .border(
                width = 1.dp,
                color = AlaaPlayerTokens.Border,
                shape = RoundedCornerShape(
                    AlaaPlayerTokens.BottomPanelRadius
                )
            )
            .padding(
                start = 30.dp,
                end = 30.dp,
                top = 19.dp,
                bottom = 14.dp
            )
    ) {

        Column {

            AlaaTimeline(
                currentPosition = currentPosition,
                duration = duration,
                seekPreview = seekPreview,

                onSeekToPosition = onSeekToPosition,
                onSetScrubbingMode = onSetScrubbingMode,
                onSeekPreviewPositionChanged = onSeekPreviewPositionChanged
            )

            Spacer(
                modifier = Modifier.height(11.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(76.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                AlaaBottomAction(
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(
                            lockButtonFocusRequester
                        ),
                    icon = Icons.Default.Lock,
                    label = "قفل",
                    onClick = onToggleLock
                )

                AlaaBottomAction(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.ClosedCaption,
                    label = "ترجمة",
                    detail = if (subtitleTrackCount > 0) {
                        subtitleTrackCount.toString()
                    } else {
                        null
                    },
                    onClick = onOpenSubtitleTracks
                )

                AlaaBottomAction(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.VolumeUp,
                    label = "الصوت",
                    detail = if (audioTrackCount > 0) {
                        audioTrackCount.toString()
                    } else {
                        null
                    },
                    onClick = onOpenAudioTracks
                )

                if (showEpisodesAction) {

                    AlaaBottomAction(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.List,
                        label = "حلقات",
                        onClick = onOpenEpisodes
                    )
                }

                AlaaBottomAction(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Settings,
                    label = "إعدادات",
                    onClick = onOpenSettings
                )

                AlaaBottomAction(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.HighQuality,
                    label = "الجودة",
                    detail = if (videoQualityCount > 0) {
                        videoQualityCount.toString()
                    } else {
                        null
                    },
                    onClick = onOpenVideoTracks
                )
            }
        }
    }
}


/*
 * ============================================================
 * TIMELINE
 * ============================================================
 */

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun AlaaTimeline(
    currentPosition: Long,
    duration: Long,
    seekPreview: SeekPreviewState,

    onSeekToPosition: (Long) -> Unit,
    onSetScrubbingMode: (Boolean) -> Unit,
    onSeekPreviewPositionChanged: (Long?) -> Unit
) {

    val safeDuration = duration.coerceAtLeast(1L)

    val progress = (
        currentPosition.toFloat() /
            safeDuration.toFloat()
        ).coerceIn(0f, 1f)

    var sliderValue by remember {
        mutableStateOf(progress)
    }

    var isScrubbing by remember {
        mutableStateOf(false)
    }


    /*
     * Keep slider synchronized with player
     * when the user is not dragging.
     */

    LaunchedEffect(
        progress,
        isScrubbing
    ) {

        if (!isScrubbing) {
            sliderValue = progress
        }
    }


    Column {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {

            Slider(
                value = sliderValue,

                onValueChange = { value ->

                    isScrubbing = true

                    sliderValue = value

                    onSetScrubbingMode(true)

                    val position = (
                        value * safeDuration
                    ).toLong()

                    onSeekPreviewPositionChanged(
                        position
                    )
                },

                onValueChangeFinished = {

                    val position = (
                        sliderValue * safeDuration
                    ).toLong()

                    onSeekToPosition(
                        position
                    )

                    isScrubbing = false

                    onSetScrubbingMode(false)

                    onSeekPreviewPositionChanged(
                        null
                    )
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .focusable(),

                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = AlaaPlayerTokens.Accent,
                    inactiveTrackColor = Color.White.copy(
                        alpha = 0.18f
                    )
                ),

                thumb = {

                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = CircleShape
                            )
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                },

                track = { sliderState ->

                    val fraction = sliderState.value
                        .coerceIn(0f, 1f)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(
                                RoundedCornerShape(50)
                            )
                            .background(
                                Color.White.copy(
                                    alpha = 0.17f
                                )
                            )
                    ) {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction)
                                .fillMaxHeight()
                                .background(
                                    AlaaPlayerTokens.Accent
                                )
                        )
                    }
                }
            )


            /*
             * SEEK PREVIEW
             */

            if (seekPreview.visible) {
                val position = seekPreview.positionMs

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .clip(
                            RoundedCornerShape(8.dp)
                        )
                        .background(
                            Color.Black.copy(alpha = 0.90f)
                        )
                        .padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        )
                ) {

                    Text(
                        text = formatDuration(position),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }


        /*
         * TIME LABELS
         */

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = formatDuration(
                    currentPosition
                ),
                color = AlaaPlayerTokens.TextSecondary,
                fontSize = 12.sp
            )

            Text(
                text = formatDuration(
                    duration
                ),
                color = AlaaPlayerTokens.TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}


/*
 * ============================================================
 * BOTTOM ACTION
 * ============================================================
 */

@Composable
private fun AlaaBottomAction(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    detail: String? = null,
    onClick: () -> Unit
) {

    var focused by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = modifier
            .height(76.dp)
            .clip(
                RoundedCornerShape(
                    AlaaPlayerTokens.ControlRadius
                )
            )
            .background(
                if (focused) {
                    AlaaPlayerTokens.FocusBackground
                } else {
                    Color.Transparent
                }
            )
            .border(
                width = if (focused) 2.dp else 1.dp,
                color = if (focused) {
                    AlaaPlayerTokens.FocusBorder
                } else {
                    Color.Transparent
                },
                shape = RoundedCornerShape(
                    AlaaPlayerTokens.ControlRadius
                )
            )
            .onFocusChanged {
                focused = it.isFocused
            }
            .focusable()
            .clickable(
                onClick = onClick
            )
            .padding(
                horizontal = 7.dp,
                vertical = 6.dp
            ),

        horizontalAlignment = Alignment.CenterHorizontally,

        verticalArrangement = Arrangement.Center
    ) {

        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (focused) {
                Color.White
            } else {
                Color.White.copy(alpha = 0.91f)
            },
            modifier = Modifier.size(27.dp)
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = label,
                color = Color.White.copy(
                    alpha = if (focused) 1f else 0.84f
                ),
                fontSize = 12.sp,
                fontWeight = if (focused) {
                    FontWeight.SemiBold
                } else {
                    FontWeight.Medium
                },
                maxLines = 1
            )

            if (!detail.isNullOrBlank()) {

                Spacer(
                    modifier = Modifier.width(4.dp)
                )

                Text(
                    text = "($detail)",
                    color = Color.White.copy(
                        alpha = 0.42f
                    ),
                    fontSize = 9.sp,
                    maxLines = 1
                )
            }
        }
    }
}


/*
 * ============================================================
 * SETTINGS PANEL
 * ============================================================
 */

@Composable
private fun AlaaPlayerSettingsPanel(
    modifier: Modifier = Modifier,
    subtitleTrackCount: Int,
    audioTrackCount: Int,
    videoQualityCount: Int,
    closeFocusRequester: FocusRequester,

    onDismiss: () -> Unit,
    onOpenSubtitleTracks: () -> Unit,
    onOpenAudioTracks: () -> Unit,
    onOpenVideoTracks: () -> Unit,
    onOpenPlaybackSpeed: () -> Unit,
    onToggleAspectRatio: () -> Unit
) {

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        /*
         * BACKDROP
         */

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Color.Black.copy(alpha = 0.27f)
                )
                .clickable(
                    onClick = onDismiss
                )
        )


        /*
         * PANEL
         */

        Column(
            modifier = modifier
                .padding(
                    end = 34.dp,
                    top = 22.dp,
                    bottom = 105.dp
                )
                .width(
                    AlaaPlayerTokens.SettingsWidth
                )
                .clip(
                    RoundedCornerShape(26.dp)
                )
                .background(
                    Color(0xFF0C0C0C).copy(
                        alpha = 0.97f
                    )
                )
                .border(
                    width = 1.dp,
                    color = AlaaPlayerTokens.Border,
                    shape = RoundedCornerShape(26.dp)
                )
                .padding(
                    horizontal = 18.dp,
                    vertical = 18.dp
                )
        ) {

            /*
             * HEADER
             */

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = "إعدادات المشغل",
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(
                        modifier = Modifier.height(3.dp)
                    )

                    Text(
                        text = "الصوت والصورة والتشغيل",
                        color = AlaaPlayerTokens.TextMuted,
                        fontSize = 11.sp
                    )
                }

                AlaaCloseButton(
                    focusRequester = closeFocusRequester,
                    onClick = onDismiss
                )
            }


            Spacer(
                modifier = Modifier.height(12.dp)
            )

            AlaaSettingsDivider()


            /*
             * SETTINGS ITEMS
             */

            AlaaSettingsItem(
                icon = Icons.Default.ClosedCaption,
                title = "الترجمة",
                detail = if (subtitleTrackCount > 0) {
                    "$subtitleTrackCount مسارات متاحة"
                } else {
                    "لا توجد مسارات"
                },
                onClick = onOpenSubtitleTracks
            )

            AlaaSettingsItem(
                icon = Icons.Default.VolumeUp,
                title = "الصوت",
                detail = if (audioTrackCount > 0) {
                    "$audioTrackCount مسارات متاحة"
                } else {
                    "لا توجد مسارات"
                },
                onClick = onOpenAudioTracks
            )

            AlaaSettingsItem(
                icon = Icons.Default.HighQuality,
                title = "جودة الفيديو",
                detail = if (videoQualityCount > 0) {
                    "$videoQualityCount خيارات"
                } else {
                    "تلقائي"
                },
                onClick = onOpenVideoTracks
            )

            AlaaSettingsItem(
                icon = Icons.Default.Speed,
                title = "سرعة التشغيل",
                detail = "1.0×",
                onClick = onOpenPlaybackSpeed
            )

            AlaaSettingsItem(
                icon = Icons.Default.AspectRatio,
                title = "نسبة العرض",
                detail = "تلقائي",
                onClick = onToggleAspectRatio
            )
        }
    }
}


/*
 * ============================================================
 * CLOSE BUTTON
 * ============================================================
 */

@Composable
private fun AlaaCloseButton(
    focusRequester: FocusRequester,
    onClick: () -> Unit
) {

    var focused by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(
                if (focused) {
                    Color.White.copy(alpha = 0.12f)
                } else {
                    Color.White.copy(alpha = 0.055f)
                }
            )
            .border(
                width = if (focused) 2.dp else 1.dp,
                color = if (focused) {
                    AlaaPlayerTokens.FocusBorder
                } else {
                    Color.Transparent
                },
                shape = CircleShape
            )
            .focusRequester(
                focusRequester
            )
            .onFocusChanged {
                focused = it.isFocused
            }
            .focusable()
            .clickable(
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {

        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
    }
}


/*
 * ============================================================
 * SETTINGS ITEM
 * ============================================================
 */

@Composable
private fun AlaaSettingsItem(
    icon: ImageVector,
    title: String,
    detail: String,
    onClick: () -> Unit
) {

    var focused by remember {
        mutableStateOf(false)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .clip(
                RoundedCornerShape(16.dp)
            )
            .background(
                if (focused) {
                    Color.White.copy(alpha = 0.09f)
                } else {
                    Color.Transparent
                }
            )
            .border(
                width = if (focused) 2.dp else 1.dp,
                color = if (focused) {
                    AlaaPlayerTokens.FocusBorder
                } else {
                    Color.Transparent
                },
                shape = RoundedCornerShape(16.dp)
            )
            .onFocusChanged {
                focused = it.isFocused
            }
            .focusable()
            .clickable(
                onClick = onClick
            )
            .padding(
                horizontal = 8.dp
            ),

        verticalAlignment = Alignment.CenterVertically
    ) {

        /*
         * ICON CONTAINER
         */

        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(
                    RoundedCornerShape(14.dp)
                )
                .background(
                    if (focused) {
                        AlaaPlayerTokens.Accent.copy(
                            alpha = 0.14f
                        )
                    } else {
                        Color.White.copy(
                            alpha = 0.055f
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {

            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (focused) {
                    AlaaPlayerTokens.AccentLight
                } else {
                    Color.White.copy(alpha = 0.92f)
                },
                modifier = Modifier.size(23.dp)
            )
        }


        Spacer(
            modifier = Modifier.width(13.dp)
        )


        /*
         * TEXT
         */

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = if (focused) {
                    FontWeight.SemiBold
                } else {
                    FontWeight.Medium
                }
            )

            Spacer(
                modifier = Modifier.height(2.dp)
            )

            Text(
                text = detail,
                color = AlaaPlayerTokens.TextMuted,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }


        /*
         * ARROW
         */

        Icon(
            imageVector = Icons.Default.ChevronLeft,
            contentDescription = null,
            tint = if (focused) {
                AlaaPlayerTokens.Accent
            } else {
                Color.White.copy(alpha = 0.30f)
            },
            modifier = Modifier.size(22.dp)
        )
    }
}


/*
 * ============================================================
 * SETTINGS DIVIDER
 * ============================================================
 */

@Composable
private fun AlaaSettingsDivider() {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                Color.White.copy(alpha = 0.07f)
            )
    )
}


/*
 * ============================================================
 * LOCKED MESSAGE
 * ============================================================
 */

@Composable
private fun AlaaLockedMessage(
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier
            .padding(
                bottom = 38.dp
            )
            .clip(
                RoundedCornerShape(18.dp)
            )
            .background(
                Color.Black.copy(alpha = 0.78f)
            )
            .border(
                width = 1.dp,
                color = AlaaPlayerTokens.Border,
                shape = RoundedCornerShape(18.dp)
            )
            .padding(
                horizontal = 21.dp,
                vertical = 11.dp
            )
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )

            Spacer(
                modifier = Modifier.width(9.dp)
            )

            Text(
                text = "الشاشة مقفلة",
                color = Color.White.copy(alpha = 0.90f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


/*
 * ============================================================
 * DURATION FORMAT
 * ============================================================
 */

private fun formatDuration(
    durationMillis: Long
): String {

    val totalSeconds = (
        durationMillis / 1000L
    ).coerceAtLeast(0L)

    val hours = totalSeconds / 3600L

    val minutes = (
        totalSeconds % 3600L
    ) / 60L

    val seconds = (
        totalSeconds % 60L
    )


    return if (hours > 0) {

        String.format(
            Locale.getDefault(),
            "%d:%02d:%02d",
            hours,
            minutes,
            seconds
        )

    } else {

        String.format(
            Locale.getDefault(),
            "%02d:%02d",
            minutes,
            seconds
        )
    }
}