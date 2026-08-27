/* ALAA LIVE PLAYER: A restrained black-and-crimson broadcast HUD. Geometry follows the supplied
 * 16:9 reference while each surface renders only real PlayerViewModel channel and EPG data. */
package com.streamvault.app.ui.themes.alaa

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material3.LinearProgressIndicator
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.app.ui.components.ChannelLogoBadge
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.app.ui.theme.AlaaThemeColors
import com.streamvault.app.ui.theme.AlaaThemeDimensions
import com.streamvault.app.ui.theme.AlaaThemeFocus
import com.streamvault.domain.model.Channel
import com.streamvault.domain.model.Program
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Live-only ALAA chrome. It is deliberately separate from the VOD controls: the background remains
 * the real playing channel while this component only renders data and invokes real player actions.
 */
@Composable
internal fun AlaaLivePlayerOverlay(
    visible: Boolean,
    channel: Channel?,
    currentProgram: Program?,
    nextProgram: Program?,
    upcomingPrograms: List<Program>,
    displayChannelNumber: Int,
    resolutionBadgeLabel: String?,
    isPlaying: Boolean,
    isFavorite: Boolean,
    replayAvailable: Boolean,
    isMuted: Boolean,
    showSettings: Boolean,
    actionBarFocusRequester: FocusRequester,
    settingsCloseFocusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    onOpenChannels: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRestartProgram: () -> Unit,
    onOpenGuide: () -> Unit,
    onOpenAudioTracks: () -> Unit,
    onToggleAspectRatio: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismissSettings: () -> Unit,
    onOpenSubtitleTracks: () -> Unit,
    onOpenVideoTracks: () -> Unit,
    onOpenPlaybackSpeed: () -> Unit,
    onUserInteraction: () -> Unit
) {
    val now by produceState(initialValue = Date()) {
        while (true) {
            value = Date()
            delay(1_000)
        }
    }
    val upcoming = remember(nextProgram, upcomingPrograms) {
        buildList {
            nextProgram?.let(::add)
            addAll(upcomingPrograms.filter { program ->
                program.id != nextProgram?.id ||
                    program.startTime != nextProgram?.startTime ||
                    program.channelId != nextProgram?.channelId
            })
        }.take(2)
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(180)),
        exit = fadeOut(tween(150)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.62f),
                        0.22f to Color.Transparent,
                        0.60f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.78f)
                    )
                )
                .onPreviewKeyEvent { event ->
                    if (event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN) onUserInteraction()
                    false
                }
        ) {
            AlaaLiveTopBar(
                channel = channel,
                displayChannelNumber = displayChannelNumber,
                isPlaying = isPlaying,
                now = now,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 42.dp, vertical = 28.dp)
            )

            AlaaLiveInformationPanel(
                channel = channel,
                currentProgram = currentProgram,
                upcomingPrograms = upcoming,
                displayChannelNumber = displayChannelNumber,
                resolutionBadgeLabel = resolutionBadgeLabel,
                isPlaying = isPlaying,
                isFavorite = isFavorite,
                replayAvailable = replayAvailable,
                isMuted = isMuted,
                onOpenChannels = onOpenChannels,
                onToggleFavorite = onToggleFavorite,
                onRestartProgram = onRestartProgram,
                onOpenGuide = onOpenGuide,
                onOpenAudioTracks = onOpenAudioTracks,
                onToggleAspectRatio = onToggleAspectRatio,
                onOpenSettings = onOpenSettings,
                actionBarFocusRequester = actionBarFocusRequester,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 42.dp, vertical = 30.dp)
            )

            if (showSettings) {
                AlaaLiveSettingsPanel(
                    closeFocusRequester = settingsCloseFocusRequester,
                    onDismiss = onDismissSettings,
                    onOpenSubtitleTracks = onOpenSubtitleTracks,
                    onOpenAudioTracks = onOpenAudioTracks,
                    onOpenVideoTracks = onOpenVideoTracks,
                    onOpenPlaybackSpeed = onOpenPlaybackSpeed,
                    onToggleAspectRatio = onToggleAspectRatio,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 42.dp, bottom = 190.dp)
                )
            }
        }
    }
}

@Composable
private fun AlaaLiveTopBar(
    channel: Channel?,
    displayChannelNumber: Int,
    isPlaying: Boolean,
    now: Date,
    modifier: Modifier = Modifier
) {
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEEE، d MMMM yyyy", Locale.getDefault()) }
    Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .background(Color.Black.copy(alpha = 0.58f), RoundedCornerShape(10.dp))
                    .padding(5.dp)
            ) {
                ChannelLogoBadge(
                    channelName = channel?.name.orEmpty(),
                    logoUrl = channel?.logoUrl,
                    backgroundColor = AlaaThemeColors.BrowseRail,
                    textStyle = MaterialTheme.typography.labelSmall,
                    textColor = Color.White,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                val number = (channel?.number ?: displayChannelNumber).takeIf { it > 0 }
                Text(
                    text = listOfNotNull(number?.toString()?.padStart(3, '0'), channel?.name).joinToString("  "),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isPlaying) "● مباشر" else "غير متصل",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isPlaying) AlaaThemeColors.AccentStrong else Color.White.copy(alpha = 0.58f)
                )
            }
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(timeFormat.format(now), style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Text(dateFormat.format(now), style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.80f))
        }
    }
}

@Composable
private fun AlaaLiveInformationPanel(
    channel: Channel?,
    currentProgram: Program?,
    upcomingPrograms: List<Program>,
    displayChannelNumber: Int,
    resolutionBadgeLabel: String?,
    isPlaying: Boolean,
    isFavorite: Boolean,
    replayAvailable: Boolean,
    isMuted: Boolean,
    onOpenChannels: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRestartProgram: () -> Unit,
    onOpenGuide: () -> Unit,
    onOpenAudioTracks: () -> Unit,
    onToggleAspectRatio: () -> Unit,
    onOpenSettings: () -> Unit,
    actionBarFocusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    val now by produceState(initialValue = System.currentTimeMillis()) {
        while (true) {
            value = System.currentTimeMillis()
            delay(15_000)
        }
    }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val channelNumber = (channel?.number ?: displayChannelNumber).takeIf { it > 0 }
    val currentProgress = currentProgram?.progressPercent(now) ?: 0f

    Column(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.82f), RoundedCornerShape(18.dp))
            .padding(top = 22.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(30.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1.15f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = channelNumber?.toString()?.padStart(3, '0').orEmpty(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .background(AlaaThemeColors.BrowseRail, RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    ChannelLogoBadge(
                        channelName = channel?.name.orEmpty(),
                        logoUrl = channel?.logoUrl,
                        backgroundColor = AlaaThemeColors.BrowseRail,
                        textStyle = MaterialTheme.typography.labelLarge,
                        textColor = Color.White,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        text = channel?.name.orEmpty(),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isPlaying) "● مباشر" else "غير متصل",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isPlaying) AlaaThemeColors.AccentStrong else Color.White.copy(alpha = 0.58f)
                    )
                    if (currentProgram != null) {
                        Text(
                            text = currentProgram.title,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (currentProgram.description.isNotBlank()) {
                            Text(
                                text = currentProgram.description,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.70f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(timeFormat.format(Date(currentProgram.startTime)), style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.82f))
                            LinearProgressIndicator(
                                progress = { currentProgress },
                                modifier = Modifier.weight(1f).height(5.dp),
                                color = AlaaThemeColors.Accent,
                                trackColor = Color.White.copy(alpha = 0.20f)
                            )
                            Text(timeFormat.format(Date(currentProgram.endTime)), style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.82f))
                        }
                    } else {
                        Text("لا توجد معلومات للبرنامج", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.72f))
                    }
                    resolutionBadgeLabel?.takeIf { it.isNotBlank() }?.let { quality ->
                        Text(quality, style = MaterialTheme.typography.labelSmall, color = AlaaThemeColors.AccentStrong)
                    }
                }
            }

            Column(
                modifier = Modifier.weight(0.85f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("البرامج القادمة", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.72f))
                if (upcomingPrograms.isEmpty()) {
                    Text("لا توجد معلومات للبرنامج", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.72f))
                } else {
                    upcomingPrograms.forEach { program ->
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "${timeFormat.format(Date(program.startTime))} – ${timeFormat.format(Date(program.endTime))}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.66f)
                            )
                            Text(program.title, style = MaterialTheme.typography.bodyLarge, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AlaaLiveAction("☰", "القنوات", onOpenChannels, Modifier.weight(1f).focusRequester(actionBarFocusRequester))
            AlaaLiveAction("♥", if (isFavorite) "إزالة المفضلة" else "المفضلة", onToggleFavorite, Modifier.weight(1f), selected = isFavorite)
            AlaaLiveAction("↻", "إعادة التشغيل", onRestartProgram, Modifier.weight(1f), enabled = replayAvailable)
            AlaaLiveAction("▦", "دليل البرامج", onOpenGuide, Modifier.weight(1f))
            AlaaLiveAction("♬", if (isMuted) "الصوت متوقف" else "الصوت", onOpenAudioTracks, Modifier.weight(1f))
            AlaaLiveAction("▣", "حجم الشاشة", onToggleAspectRatio, Modifier.weight(1f))
            AlaaLiveAction("⚙", "الإعدادات", onOpenSettings, Modifier.weight(1f))
        }
    }
}

@Composable
private fun AlaaLiveAction(
    icon: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false
) {
    TvClickableSurface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(60.dp),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) AlaaThemeColors.Accent.copy(alpha = 0.32f) else Color.Transparent,
            focusedContainerColor = if (selected) AlaaThemeColors.Accent else AlaaThemeColors.SurfaceFocused,
            contentColor = Color.White,
            focusedContentColor = Color.White
        ),
        border = ClickableSurfaceDefaults.border(
            border = Border(BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)), 12.dp),
            focusedBorder = Border(BorderStroke(AlaaThemeDimensions.FocusBorder, AlaaThemeColors.Accent), 12.dp)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = AlaaThemeFocus.FocusedScale)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, style = MaterialTheme.typography.titleMedium, color = Color.White)
            Spacer(Modifier.width(7.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

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
    Column(
        modifier = modifier
            .width(330.dp)
            .background(Color.Black.copy(alpha = 0.94f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("إعدادات المشغّل", style = MaterialTheme.typography.titleMedium, color = Color.White)
            TvClickableSurface(
                onClick = onDismiss,
                modifier = Modifier.focusRequester(closeFocusRequester).size(42.dp),
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(10.dp)),
                colors = ClickableSurfaceDefaults.colors(containerColor = AlaaThemeColors.BrowseRail, focusedContainerColor = AlaaThemeColors.Accent, contentColor = Color.White, focusedContentColor = Color.White)
            ) { Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { Text("×", style = MaterialTheme.typography.titleLarge) } }
        }
        AlaaLiveSetting("الترجمة", onDismiss, onOpenSubtitleTracks)
        AlaaLiveSetting("الصوت", onDismiss, onOpenAudioTracks)
        AlaaLiveSetting("الجودة", onDismiss, onOpenVideoTracks)
        AlaaLiveSetting("سرعة التشغيل", onDismiss, onOpenPlaybackSpeed)
        AlaaLiveSetting("أبعاد الفيديو", onDismiss, onToggleAspectRatio)
    }
}

@Composable
private fun AlaaLiveSetting(label: String, onDismiss: () -> Unit, onClick: () -> Unit) {
    TvClickableSurface(
        onClick = { onDismiss(); onClick() },
        modifier = Modifier.fillMaxWidth(),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(10.dp)),
        colors = ClickableSurfaceDefaults.colors(containerColor = AlaaThemeColors.BrowseRail, focusedContainerColor = AlaaThemeColors.SurfaceFocused, contentColor = Color.White, focusedContentColor = Color.White),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(AlaaThemeDimensions.FocusBorder, AlaaThemeColors.Accent), 10.dp)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = AlaaThemeFocus.FocusedScale)
    ) {
        Text(label, modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp), style = MaterialTheme.typography.labelLarge)
    }
}
