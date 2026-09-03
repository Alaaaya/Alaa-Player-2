package com.streamvault.app.ui.themes.alaa

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamvault.app.ui.components.ChannelLogoBadge
import com.streamvault.app.ui.theme.AlaaThemeColors
import com.streamvault.domain.model.Channel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay


/*
 * ============================================================
 * ALAA LIVE PLAYER
 * PREMIUM CINEMATIC LIVE TV DESIGN
 *
 * Existing functionality preserved.
 *
 * NO NEW FEATURES.
 *
 * Existing controls:
 * - Channels
 * - Favorite
 * - Audio
 * - Aspect Ratio
 * - Settings
 * - Quality
 * - Subtitles
 *
 * Visual redesign only.
 * ============================================================
 */


/*
 * ============================================================
 * DESIGN TOKENS
 * ============================================================
 */

private object AlaaLiveTokens {

    val Accent = Color(0xFFFF7A00)
    val AccentLight = Color(0xFFFFA13A)
    val AccentDark = Color(0xFFE76400)

    val Background = Color(0xFF050505)

    val Glass = Color(0xE80A0A0A)
    val GlassTop = Color(0xF0141414)
    val GlassBottom = Color(0xE8070707)

    val Surface = Color(0xFF121212)
    val SurfaceElevated = Color(0xFF181818)

    val Border = Color.White.copy(alpha = 0.10f)
    val BorderStrong = Color.White.copy(alpha = 0.18f)

    val TextPrimary = Color.White
    val TextSecondary = Color.White.copy(alpha = 0.72f)
    val TextMuted = Color.White.copy(alpha = 0.46f)

    val FocusBackground = Color(0x24FF7A00)
    val FocusBorder = Accent

    val MainBoxRadius = 28.dp
    val ActionRadius = 15.dp
}


/*
 * ============================================================
 * MAIN LIVE OVERLAY
 * ============================================================
 */

@Composable
internal fun AlaaLivePlayerOverlay(
    visible: Boolean,

    channel: Channel?,

    /*
     * Kept for compatibility with the existing caller.
     * Intentionally not rendered as a new EPG interface.
     */
    currentProgram: Any? = null,
    nextProgram: Any? = null,
    upcomingPrograms: List<Any> = emptyList(),

    displayChannelNumber: Int,

    resolutionBadgeLabel: String?,

    isPlaying: Boolean,
    isFavorite: Boolean,

    /*
     * Kept for compatibility.
     * No new replay controls are introduced.
     */
    replayAvailable: Boolean = false,

    isMuted: Boolean,

    showSettings: Boolean,

    actionBarFocusRequester: FocusRequester,
    settingsCloseFocusRequester: FocusRequester,

    modifier: Modifier = Modifier,

    onBack: () -> Unit,

    onOpenChannels: () -> Unit,

    onToggleFavorite: () -> Unit,

    onRestartProgram: () -> Unit = {},

    onOpenGuide: () -> Unit = {},

    onOpenAudioTracks: () -> Unit,

    onToggleAspectRatio: () -> Unit,

    onOpenSettings: () -> Unit,

    onDismissSettings: () -> Unit,

    onOpenSubtitleTracks: () -> Unit,

    onOpenVideoTracks: () -> Unit,

    onOpenPlaybackSpeed: () -> Unit,

    onUserInteraction: () -> Unit
) {

    if (!visible) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.00f to Color.Black.copy(alpha = 0.48f),
                        0.15f to Color.Black.copy(alpha = 0.12f),
                        0.50f to Color.Transparent,
                        0.70f to Color.Transparent,
                        0.84f to Color.Black.copy(alpha = 0.16f),
                        1.00f to Color.Black.copy(alpha = 0.82f)
                    )
                )
            )
            .onPreviewKeyEvent { event ->

                if (
                    event.nativeKeyEvent.action ==
                        android.view.KeyEvent.ACTION_DOWN
                ) {
                    onUserInteraction()
                }

                false
            }
    ) {

        /*
         * ========================================================
         * BACK
         * ========================================================
         */

        AlaaLiveBackButton(
            onClick = {
                onUserInteraction()
                onBack()
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(
                    start = 34.dp,
                    top = 28.dp
                )
        )


        /*
         * ========================================================
         * CLOCK
         * ========================================================
         */

        AlaaLiveClock(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(
                    end = 38.dp,
                    top = 28.dp
                )
        )


        /*
         * ========================================================
         * MAIN GLASS PANEL
         * ========================================================
         */

        AlaaLiveMainPanel(
            channel = channel,

            displayChannelNumber =
                displayChannelNumber,

            resolutionBadgeLabel =
                resolutionBadgeLabel,

            isPlaying =
                isPlaying,

            isFavorite =
                isFavorite,

            isMuted =
                isMuted,

            actionBarFocusRequester =
                actionBarFocusRequester,

            onOpenChannels = {
                onUserInteraction()
                onOpenChannels()
            },

            onToggleFavorite = {
                onUserInteraction()
                onToggleFavorite()
            },

            onOpenAudioTracks = {
                onUserInteraction()
                onOpenAudioTracks()
            },

            onToggleAspectRatio = {
                onUserInteraction()
                onToggleAspectRatio()
            },

            onOpenSettings = {
                onUserInteraction()
                onOpenSettings()
            },

            onOpenSubtitleTracks = {
                onUserInteraction()
                onOpenSubtitleTracks()
            },

            onOpenVideoTracks = {
                onUserInteraction()
                onOpenVideoTracks()
            },

            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    start = 44.dp,
                    end = 44.dp,
                    bottom = 32.dp
                )
        )


        /*
         * ========================================================
         * SETTINGS
         * ========================================================
         */

        if (showSettings) {

            AlaaLiveSettingsPanel(
                closeFocusRequester =
                    settingsCloseFocusRequester,

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
                },

                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 42.dp)
            )
        }
    }
}


/*
 * ============================================================
 * MAIN SINGLE GLASS PANEL
 * ============================================================
 */

@Composable
private fun AlaaLiveMainPanel(
    channel: Channel?,

    displayChannelNumber: Int,

    resolutionBadgeLabel: String?,

    isPlaying: Boolean,

    isFavorite: Boolean,

    isMuted: Boolean,

    actionBarFocusRequester: FocusRequester,

    onOpenChannels: () -> Unit,

    onToggleFavorite: () -> Unit,

    onOpenAudioTracks: () -> Unit,

    onToggleAspectRatio: () -> Unit,

    onOpenSettings: () -> Unit,

    onOpenSubtitleTracks: () -> Unit,

    onOpenVideoTracks: () -> Unit,

    modifier: Modifier = Modifier
) {

    val channelNumber = (
        channel?.number
            ?: displayChannelNumber
        ).takeIf {
            it > 0
        }


    Box(
        modifier = modifier
            .fillMaxWidth(0.91f)
            .widthIn(
                min = 960.dp,
                max = 1400.dp
            )
            .clip(
                RoundedCornerShape(
                    AlaaLiveTokens.MainBoxRadius
                )
            )
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AlaaLiveTokens.GlassTop,
                        AlaaLiveTokens.GlassBottom
                    )
                )
            )
            .border(
                width = 1.dp,
                color = AlaaLiveTokens.BorderStrong,
                shape = RoundedCornerShape(
                    AlaaLiveTokens.MainBoxRadius
                )
            )
            .shadow(
                elevation = 28.dp,
                shape = RoundedCornerShape(
                    AlaaLiveTokens.MainBoxRadius
                )
            )
            .padding(
                start = 26.dp,
                end = 26.dp,
                top = 20.dp,
                bottom = 18.dp
            )
    ) {

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            /*
             * ====================================================
             * CHANNEL INFORMATION
             * ====================================================
             */

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(104.dp),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                /*
                 * CHANNEL LOGO
                 */

                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(
                            RoundedCornerShape(16.dp)
                        )
                        .background(
                            Color.Black.copy(
                                alpha = 0.58f
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(
                                alpha = 0.14f
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(8.dp)
                ) {

                    ChannelLogoBadge(
                        channelName =
                            channel?.name.orEmpty(),

                        logoUrl =
                            channel?.logoUrl,

                        backgroundColor =
                            AlaaThemeColors.BrowseRail,

                        textColor =
                            Color.White,

                        modifier =
                            Modifier.fillMaxSize()
                    )
                }


                Spacer(
                    modifier =
                        Modifier.width(20.dp)
                )


                /*
                 * CHANNEL INFORMATION
                 */

                Column(
                    modifier =
                        Modifier.weight(1f),

                    verticalArrangement =
                        Arrangement.Center
                ) {

                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        /*
                         * CHANNEL NUMBER BADGE
                         */

                        channelNumber?.let { number ->

                            Box(
                                modifier = Modifier
                                    .clip(
                                        RoundedCornerShape(8.dp)
                                    )
                                    .background(
                                        AlaaLiveTokens.Accent.copy(
                                            alpha = 0.08f
                                        )
                                    )
                                    .border(
                                        width = 1.5.dp,
                                        color =
                                            AlaaLiveTokens.Accent.copy(
                                                alpha = 0.88f
                                            ),
                                        shape =
                                            RoundedCornerShape(8.dp)
                                    )
                                    .padding(
                                        horizontal = 12.dp,
                                        vertical = 6.dp
                                    )
                            ) {

                                Text(
                                    text =
                                        "CH " +
                                            number
                                                .toString()
                                                .padStart(
                                                    2,
                                                    '0'
                                                ),

                                    color =
                                        AlaaLiveTokens.AccentLight,

                                    fontSize = 15.sp,

                                    fontWeight =
                                        FontWeight.Bold
                                )
                            }


                            Spacer(
                                modifier =
                                    Modifier.width(14.dp)
                            )
                        }


                        /*
                         * CHANNEL NAME
                         */

                        Text(
                            text =
                                channel?.name
                                    ?.takeIf {
                                        it.isNotBlank()
                                    }
                                    ?: "قناة مباشرة",

                            color =
                                AlaaLiveTokens.TextPrimary,

                            fontSize = 26.sp,

                            fontWeight =
                                FontWeight.Bold,

                            maxLines = 1,

                            overflow =
                                TextOverflow.Ellipsis
                        )


                        /*
                         * QUALITY BADGE
                         */

                        resolutionBadgeLabel
                            ?.takeIf {
                                it.isNotBlank()
                            }
                            ?.let { quality ->

                                Spacer(
                                    modifier =
                                        Modifier.width(16.dp)
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(
                                            RoundedCornerShape(7.dp)
                                        )
                                        .background(
                                            Color.Transparent
                                        )
                                        .border(
                                            width = 1.dp,
                                            color =
                                                AlaaLiveTokens.Accent.copy(
                                                    alpha = 0.75f
                                                ),
                                            shape =
                                                RoundedCornerShape(7.dp)
                                        )
                                        .padding(
                                            horizontal = 9.dp,
                                            vertical = 5.dp
                                        )
                                ) {

                                    Text(
                                        text =
                                            quality.uppercase(
                                                Locale.getDefault()
                                            ),

                                        color =
                                            AlaaLiveTokens.AccentLight,

                                        fontSize = 12.sp,

                                        fontWeight =
                                            FontWeight.Bold
                                    )
                                }
                            }
                    }


                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )


                    /*
                     * LIVE STATUS
                     */

                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isPlaying) {
                                        Color(0xFFFF3131)
                                    } else {
                                        Color.White.copy(
                                            alpha = 0.30f
                                        )
                                    }
                                )
                        )


                        Spacer(
                            modifier =
                                Modifier.width(7.dp)
                        )


                        Text(
                            text =
                                if (isPlaying) {
                                    "مباشر"
                                } else {
                                    "غير متصل"
                                },

                            color =
                                if (isPlaying) {
                                    Color(0xFFFF6262)
                                } else {
                                    AlaaLiveTokens.TextMuted
                                },

                            fontSize = 13.sp,

                            fontWeight =
                                FontWeight.SemiBold
                        )


                        /*
                         * SUBTLE SEPARATOR
                         */

                        Box(
                            modifier = Modifier
                                .padding(
                                    horizontal = 11.dp
                                )
                                .width(1.dp)
                                .height(14.dp)
                                .background(
                                    Color.White.copy(
                                        alpha = 0.18f
                                    )
                                )
                        )


                        Text(
                            text =
                                if (isMuted) {
                                    "الصوت مكتوم"
                                } else {
                                    "البث المباشر"
                                },

                            color =
                                AlaaLiveTokens.TextMuted,

                            fontSize = 12.sp,

                            maxLines = 1,

                            overflow =
                                TextOverflow.Ellipsis
                        )
                    }
                }
            }


            /*
             * ====================================================
             * DIVIDER
             * ====================================================
             */

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Color.White.copy(
                            alpha = 0.10f
                        )
                    )
            )


            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )


            /*
             * ====================================================
             * ACTION BAR
             * ====================================================
             */

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(82.dp),

                horizontalArrangement =
                    Arrangement.spacedBy(9.dp),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                /*
                 * CHANNELS
                 */

                AlaaLiveAction(
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(
                            actionBarFocusRequester
                        ),

                    icon =
                        Icons.Default.List,

                    label =
                        "القنوات",

                    onClick =
                        onOpenChannels
                )


                /*
                 * FAVORITE
                 */

                AlaaLiveAction(
                    modifier =
                        Modifier.weight(1f),

                    icon =
                        Icons.Default.Favorite,

                    label =
                        if (isFavorite) {
                            "إزالة المفضلة"
                        } else {
                            "المفضلة"
                        },

                    selected =
                        isFavorite,

                    onClick =
                        onToggleFavorite
                )


                /*
                 * AUDIO
                 */

                AlaaLiveAction(
                    modifier =
                        Modifier.weight(1f),

                    icon =
                        Icons.Default.VolumeUp,

                    label =
                        if (isMuted) {
                            "الصوت مكتوم"
                        } else {
                            "الصوت"
                        },

                    onClick =
                        onOpenAudioTracks
                )


                /*
                 * ASPECT RATIO
                 */

                AlaaLiveAction(
                    modifier =
                        Modifier.weight(1f),

                    icon =
                        Icons.Default.AspectRatio,

                    label =
                        "حجم الشاشة",

                    onClick =
                        onToggleAspectRatio
                )


                /*
                 * SETTINGS
                 */

                AlaaLiveAction(
                    modifier =
                        Modifier.weight(1f),

                    icon =
                        Icons.Default.Settings,

                    label =
                        "الإعدادات",

                    onClick =
                        onOpenSettings
                )


                /*
                 * QUALITY
                 */

                AlaaLiveAction(
                    modifier =
                        Modifier.weight(1f),

                    icon =
                        Icons.Default.HighQuality,

                    label =
                        "الجودة",

                    onClick =
                        onOpenVideoTracks
                )


                /*
                 * SUBTITLES
                 */

                AlaaLiveAction(
                    modifier =
                        Modifier.weight(1f),

                    icon =
                        Icons.Default.ClosedCaption,

                    label =
                        "الترجمة",

                    onClick =
                        onOpenSubtitleTracks
                )
            }
        }
    }
}


/*
 * ============================================================
 * LIVE ACTION BUTTON
 * ============================================================
 */

@Composable
private fun AlaaLiveAction(
    icon: ImageVector,

    label: String,

    onClick: () -> Unit,

    modifier: Modifier = Modifier,

    selected: Boolean = false
) {

    var focused by remember {
        mutableStateOf(false)
    }


    Column(
        modifier = modifier
            .height(82.dp)

            .clip(
                RoundedCornerShape(
                    AlaaLiveTokens.ActionRadius
                )
            )

            .background(
                when {

                    focused ->
                        AlaaLiveTokens.FocusBackground

                    selected ->
                        AlaaLiveTokens.Accent.copy(
                            alpha = 0.12f
                        )

                    else ->
                        Color.White.copy(
                            alpha = 0.025f
                        )
                }
            )

            .border(
                width =
                    if (focused) {
                        2.dp
                    } else {
                        1.dp
                    },

                color =
                    when {

                        focused ->
                            AlaaLiveTokens.FocusBorder

                        selected ->
                            AlaaLiveTokens.Accent.copy(
                                alpha = 0.48f
                            )

                        else ->
                            Color.White.copy(
                                alpha = 0.09f
                            )
                    },

                shape =
                    RoundedCornerShape(
                        AlaaLiveTokens.ActionRadius
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
                horizontal = 5.dp,
                vertical = 7.dp
            ),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center
    ) {

        /*
         * ICON
         */

        Icon(
            imageVector =
                icon,

            contentDescription =
                label,

            tint =
                if (focused) {
                    AlaaLiveTokens.AccentLight
                } else {
                    Color.White.copy(
                        alpha = 0.92f
                    )
                },

            modifier =
                Modifier.size(27.dp)
        )


        Spacer(
            modifier =
                Modifier.height(6.dp)
        )


        /*
         * LABEL
         */

        Text(
            text =
                label,

            color =
                if (focused) {
                    Color.White
                } else {
                    Color.White.copy(
                        alpha = 0.82f
                    )
                },

            fontSize = 11.sp,

            fontWeight =
                if (focused) {
                    FontWeight.Bold
                } else {
                    FontWeight.Medium
                },

            maxLines = 1,

            overflow =
                TextOverflow.Ellipsis
        )
    }
}


/*
 * ============================================================
 * BACK BUTTON
 * ============================================================
 */

@Composable
private fun AlaaLiveBackButton(
    onClick: () -> Unit,

    modifier: Modifier = Modifier
) {

    var focused by remember {
        mutableStateOf(false)
    }


    Box(
        modifier = modifier
            .size(
                width = 72.dp,
                height = 60.dp
            )

            .clip(
                RoundedCornerShape(18.dp)
            )

            .background(
                if (focused) {
                    AlaaLiveTokens.FocusBackground
                } else {
                    Color.Black.copy(
                        alpha = 0.34f
                    )
                }
            )

            .border(
                width =
                    if (focused) {
                        2.dp
                    } else {
                        1.dp
                    },

                color =
                    if (focused) {
                        AlaaLiveTokens.FocusBorder
                    } else {
                        AlaaLiveTokens.Border
                    },

                shape =
                    RoundedCornerShape(18.dp)
            )

            .onFocusChanged {
                focused = it.isFocused
            }

            .focusable()

            .clickable(
                onClick = onClick
            ),

        contentAlignment =
            Alignment.Center
    ) {

        Icon(
            imageVector =
                Icons.Default.ArrowBack,

            contentDescription =
                "رجوع",

            tint =
                Color.White,

            modifier =
                Modifier.size(29.dp)
        )
    }
}


/*
 * ============================================================
 * CLOCK
 * ============================================================
 */

@Composable
private fun AlaaLiveClock(
    modifier: Modifier = Modifier
) {

    val now by produceState(
        initialValue = Date()
    ) {

        while (true) {

            value = Date()

            delay(1_000)
        }
    }


    val timeFormat = remember {
        SimpleDateFormat(
            "HH:mm",
            Locale.getDefault()
        )
    }


    val dateFormat = remember {
        SimpleDateFormat(
            "EEE, dd MMM",
            Locale.getDefault()
        )
    }


    Column(
        modifier = modifier,

        horizontalAlignment =
            Alignment.End
    ) {

        Text(
            text =
                timeFormat.format(now),

            color =
                Color.White,

            fontSize = 27.sp,

            fontWeight =
                FontWeight.Medium
        )


        Spacer(
            modifier =
                Modifier.height(3.dp)
        )


        Text(
            text =
                dateFormat.format(now),

            color =
                Color.White.copy(
                    alpha = 0.58f
                ),

            fontSize = 12.sp
        )
    }
}


/*
 * ============================================================
 * SETTINGS PANEL
 * ============================================================
 */

@Composable
private fun AlaaLiveSettingsPanel(
    closeFocusRequester: FocusRequester,

    onDismiss: () -> Unit,

    onOpenSubtitleTracks: () -> Unit,

    onOpenAudioTracks: () -> Unit,

    onOpenVideoTracks: () -> Unit,

    onOpenPlaybackSpeed: () -> Unit,

    onToggleAspectRatio: () -> Unit,

    modifier: Modifier = Modifier
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onPreviewKeyEvent { event ->

                if (
                    event.nativeKeyEvent.keyCode ==
                        android.view.KeyEvent.KEYCODE_BACK &&

                    event.nativeKeyEvent.action ==
                        android.view.KeyEvent.ACTION_DOWN
                ) {

                    onDismiss()

                    true

                } else {

                    false
                }
            }
    ) {

        /*
         * ========================================================
         * BACKDROP
         * ========================================================
         */

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Color.Black.copy(
                        alpha = 0.34f
                    )
                )
                .clickable(
                    onClick = onDismiss
                )
        )


        /*
         * ========================================================
         * SETTINGS CARD
         * ========================================================
         */

        Column(
            modifier = modifier
                .width(360.dp)

                .clip(
                    RoundedCornerShape(26.dp)
                )

                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF171717).copy(
                                alpha = 0.98f
                            ),
                            Color(0xFF090909).copy(
                                alpha = 0.98f
                            )
                        )
                    )
                )

                .border(
                    width = 1.dp,

                    color =
                        AlaaLiveTokens.BorderStrong,

                    shape =
                        RoundedCornerShape(26.dp)
                )

                .shadow(
                    elevation = 28.dp,

                    shape =
                        RoundedCornerShape(26.dp)
                )

                .padding(
                    horizontal = 18.dp,
                    vertical = 18.dp
                )
        ) {

            /*
             * ====================================================
             * HEADER
             * ====================================================
             */

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(
                        text =
                            "إعدادات المشغل",

                        color =
                            Color.White,

                        fontSize = 19.sp,

                        fontWeight =
                            FontWeight.SemiBold
                    )


                    Spacer(
                        modifier =
                            Modifier.height(3.dp)
                    )


                    Text(
                        text =
                            "الصوت والصورة والتشغيل",

                        color =
                            AlaaLiveTokens.TextMuted,

                        fontSize = 11.sp
                    )
                }


                AlaaLiveCloseButton(
                    focusRequester =
                        closeFocusRequester,

                    onClick =
                        onDismiss
                )
            }


            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )


            /*
             * DIVIDER
             */

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Color.White.copy(
                            alpha = 0.08f
                        )
                    )
            )


            /*
             * SETTINGS
             */

            AlaaLiveSettingItem(
                icon =
                    Icons.Default.ClosedCaption,

                title =
                    "الترجمة",

                detail =
                    "اختيار مسار الترجمة",

                onClick =
                    onOpenSubtitleTracks
            )


            AlaaLiveSettingItem(
                icon =
                    Icons.Default.VolumeUp,

                title =
                    "الصوت",

                detail =
                    "اختيار مسار الصوت",

                onClick =
                    onOpenAudioTracks
            )


            AlaaLiveSettingItem(
                icon =
                    Icons.Default.HighQuality,

                title =
                    "جودة الفيديو",

                detail =
                    "اختيار الجودة",

                onClick =
                    onOpenVideoTracks
            )


            AlaaLiveSettingItem(
                icon =
                    Icons.Default.Speed,

                title =
                    "سرعة التشغيل",

                detail =
                    "1.0×",

                onClick =
                    onOpenPlaybackSpeed
            )


            AlaaLiveSettingItem(
                icon =
                    Icons.Default.AspectRatio,

                title =
                    "نسبة العرض",

                detail =
                    "تلقائي",

                onClick =
                    onToggleAspectRatio
            )
        }
    }
}


/*
 * ============================================================
 * SETTINGS ITEM
 * ============================================================
 */

@Composable
private fun AlaaLiveSettingItem(
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
                    AlaaLiveTokens.FocusBackground
                } else {
                    Color.Transparent
                }
            )

            .border(
                width =
                    if (focused) {
                        2.dp
                    } else {
                        1.dp
                    },

                color =
                    if (focused) {
                        AlaaLiveTokens.FocusBorder
                    } else {
                        Color.Transparent
                    },

                shape =
                    RoundedCornerShape(16.dp)
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

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        /*
         * ICON BOX
         */

        Box(
            modifier = Modifier
                .size(46.dp)

                .clip(
                    RoundedCornerShape(14.dp)
                )

                .background(
                    if (focused) {
                        AlaaLiveTokens.Accent.copy(
                            alpha = 0.14f
                        )
                    } else {
                        Color.White.copy(
                            alpha = 0.055f
                        )
                    }
                ),

            contentAlignment =
                Alignment.Center
        ) {

            Icon(
                imageVector =
                    icon,

                contentDescription =
                    title,

                tint =
                    if (focused) {
                        AlaaLiveTokens.AccentLight
                    } else {
                        Color.White.copy(
                            alpha = 0.90f
                        )
                    },

                modifier =
                    Modifier.size(23.dp)
            )
        }


        Spacer(
            modifier =
                Modifier.width(13.dp)
        )


        /*
         * TEXT
         */

        Column(
            modifier =
                Modifier.weight(1f)
        ) {

            Text(
                text =
                    title,

                color =
                    Color.White,

                fontSize = 14.sp,

                fontWeight =
                    if (focused) {
                        FontWeight.SemiBold
                    } else {
                        FontWeight.Medium
                    }
            )


            Spacer(
                modifier =
                    Modifier.height(2.dp)
            )


            Text(
                text =
                    detail,

                color =
                    AlaaLiveTokens.TextMuted,

                fontSize = 11.sp,

                maxLines = 1,

                overflow =
                    TextOverflow.Ellipsis
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
private fun AlaaLiveCloseButton(
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
                    Color.White.copy(
                        alpha = 0.12f
                    )
                } else {
                    Color.White.copy(
                        alpha = 0.055f
                    )
                }
            )

            .border(
                width =
                    if (focused) {
                        2.dp
                    } else {
                        1.dp
                    },

                color =
                    if (focused) {
                        AlaaLiveTokens.FocusBorder
                    } else {
                        Color.Transparent
                    },

                shape =
                    CircleShape
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

        contentAlignment =
            Alignment.Center
    ) {

        Icon(
            imageVector =
                Icons.Default.Close,

            contentDescription =
                "إغلاق",

            tint =
                Color.White,

            modifier =
                Modifier.size(22.dp)
        )
    }
}