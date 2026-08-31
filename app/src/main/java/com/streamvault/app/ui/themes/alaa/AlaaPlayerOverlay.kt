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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.app.ui.screens.player.SeekPreviewState
import com.streamvault.app.ui.theme.AlaaThemeColors
import com.streamvault.app.ui.theme.AlaaThemeDimensions
import com.streamvault.app.ui.theme.AlaaThemeFocus
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * طبقة تحكم ALAA: الفيديو يبقى مملوكاً للمشغّل الحالي، وتعرض هذه الطبقة فقط أدوات
 * حقيقية تمرر كل حدث إلى PlayerViewModel من خلال callbacks.
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
    isImmersive: Boolean,
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
    onToggleImmersive: () -> Unit,
    onUserInteraction: () -> Unit
) {
    val now by produceState(initialValue = Date()) {
        while (true) {
            value = Date()
            delay(1_000)
        }
    }
    val topTitle = if (contentType == "SERIES_EPISODE" && !seriesTitle.isNullOrBlank()) {
        buildString {
            append(seriesTitle)
            seasonNumber?.let { append(" · S$it") }
            episodeNumber?.let { append(" E$it") }
        }
    } else {
        mediaTitle?.takeIf { it.isNotBlank() } ?: title
    }
    val subtitle = if (contentType == "SERIES_EPISODE") {
        episodeTitle?.takeIf { it.isNotBlank() } ?: title.substringBeforeLast(" - S")
    } else {
        null
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
                        0f to Color.Black.copy(alpha = 0.68f),
                        0.35f to Color.Transparent,
                        0.68f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.86f)
                    )
                )
                .onPreviewKeyEvent { event ->
                    if (event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN) onUserInteraction()
                    false
                }
        ) {
            AlaaPlayerTopInformation(
                title = topTitle,
                subtitle = subtitle,
                now = now,
                onBack = onBack,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 34.dp, vertical = 28.dp)
            )
            AlaaPlayerTransport(
                isPlaying = isPlaying,
                isLocked = isLocked,
                playButtonFocusRequester = playButtonFocusRequester,
                onSeekBackward = onSeekBackward,
                onTogglePlayPause = onTogglePlayPause,
                onSeekForward = onSeekForward,
                modifier = Modifier.align(Alignment.Center)
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 62.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                AlaaPlayerTimeline(
                    currentPosition = currentPosition,
                    duration = duration,
                    seekPreview = seekPreview,
                    enabled = !isLocked,
                    onSeekToPosition = onSeekToPosition,
                    onSetScrubbingMode = onSetScrubbingMode,
                    onSeekPreviewPositionChanged = onSeekPreviewPositionChanged
                )
                AlaaPlayerActionBar(
                    isLocked = isLocked,
                    isImmersive = isImmersive,
                    subtitleTrackCount = subtitleTrackCount,
                    audioTrackCount = audioTrackCount,
                    videoQualityCount = videoQualityCount,
                    showEpisodesAction = showEpisodesAction,
                    lockButtonFocusRequester = lockButtonFocusRequester,
                    onToggleLock = onToggleLock,
                    onOpenSubtitleTracks = onOpenSubtitleTracks,
                    onOpenAudioTracks = onOpenAudioTracks,
                    onOpenEpisodes = onOpenEpisodes,
                    onOpenSettings = onOpenSettings,
                    onOpenVideoTracks = onOpenVideoTracks,
                    onToggleImmersive = onToggleImmersive
                )
            }
            if (isLocked) {
                Text(
                    text = "تم قفل عناصر التحكم",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 116.dp)
                        .background(AlaaThemeColors.Accent.copy(alpha = 0.9f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
            }
            if (showSettings) {
                AlaaPlayerSettingsPanel(
                    onDismiss = onDismissSettings,
                    onOpenSubtitleTracks = onOpenSubtitleTracks,
                    onOpenAudioTracks = onOpenAudioTracks,
                    onOpenVideoTracks = onOpenVideoTracks,
                    onOpenPlaybackSpeed = onOpenPlaybackSpeed,
                    onToggleAspectRatio = onToggleAspectRatio,
                    closeFocusRequester = settingsCloseFocusRequester,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 58.dp, bottom = 128.dp)
                )
            }
        }
    }
}

@Composable
private fun AlaaPlayerTopInformation(
    title: String,
    subtitle: String?,
    now: Date,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeText = remember(now) { SimpleDateFormat("hh:mm a", Locale.getDefault()).format(now) }
    val dateText = remember(now) { SimpleDateFormat("EEEE، d MMMM yyyy", Locale("ar")).format(now) }
    Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.Top) {
            AlaaControlSurface(label = "←", caption = "رجوع", onClick = onBack, compact = true)
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                subtitle?.let {
                    Text(it, style = MaterialTheme.typography.titleSmall, color = AlaaThemeColors.Accent, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(timeText, style = MaterialTheme.typography.headlineSmall, color = Color.White)
            Text(dateText, style = MaterialTheme.typography.titleSmall, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun AlaaPlayerTransport(
    isPlaying: Boolean,
    isLocked: Boolean,
    playButtonFocusRequester: FocusRequester,
    onSeekBackward: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekForward: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(34.dp), verticalAlignment = Alignment.CenterVertically) {
        AlaaControlSurface(label = "↶10", caption = "رجوع", onClick = onSeekBackward, enabled = !isLocked)
        AlaaControlSurface(
            label = if (isPlaying) "Ⅱ" else "▶",
            caption = if (isPlaying) "إيقاف" else "تشغيل",
            onClick = onTogglePlayPause,
            enabled = !isLocked,
            modifier = Modifier.focusRequester(playButtonFocusRequester),
            primary = true
        )
        AlaaControlSurface(label = "10↷", caption = "تقدم", onClick = onSeekForward, enabled = !isLocked)
    }
}

@Composable
private fun AlaaPlayerTimeline(
    currentPosition: Long,
    duration: Long,
    seekPreview: SeekPreviewState,
    enabled: Boolean,
    onSeekToPosition: (Long) -> Unit,
    onSetScrubbingMode: (Boolean) -> Unit,
    onSeekPreviewPositionChanged: (Long?) -> Unit
) {
    val safeDuration = duration.coerceAtLeast(0L)
    val progress = if (safeDuration > 0L) (currentPosition.toFloat() / safeDuration).coerceIn(0f, 1f) else 0f
    var pendingProgress by remember { mutableFloatStateOf(progress) }
    var isScrubbing by remember { mutableStateOf(false) }
    
    // تحديث pendingProgress عند تغيير progress من الخارج
    LaunchedEffect(progress) {
        if (!isScrubbing) {
            pendingProgress = progress
        }
    }
    
    val displayedProgress = if (isScrubbing) pendingProgress else progress
    
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatDuration(currentPosition), style = MaterialTheme.typography.titleSmall, color = Color.White)
            Text(
                text = if (seekPreview.visible) formatDuration(seekPreview.positionMs) else formatDuration(safeDuration),
                style = MaterialTheme.typography.titleSmall,
                color = Color.White
            )
        }
        Slider(
            value = displayedProgress,
            onValueChange = { value ->
                if (safeDuration <= 0L) return@Slider
                isScrubbing = true
                pendingProgress = value
                onSetScrubbingMode(true)
                onSeekPreviewPositionChanged((value * safeDuration).toLong())
            },
            onValueChangeFinished = {
                if (safeDuration > 0L) {
                    val seekPosition = (pendingProgress * safeDuration).toLong()
                    onSeekToPosition(seekPosition)
                }
                onSeekPreviewPositionChanged(null)
                onSetScrubbingMode(false)
                isScrubbing = false
            },
            enabled = enabled && safeDuration > 0L,
            colors = SliderDefaults.colors(
                thumbColor = AlaaThemeColors.Accent,
                activeTrackColor = AlaaThemeColors.Accent,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f),
                disabledThumbColor = Color.White.copy(alpha = 0.45f),
                disabledActiveTrackColor = AlaaThemeColors.Accent.copy(alpha = 0.5f),
                disabledInactiveTrackColor = Color.White.copy(alpha = 0.15f)
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun AlaaPlayerActionBar(
    isLocked: Boolean,
    isImmersive: Boolean,
    subtitleTrackCount: Int,
    audioTrackCount: Int,
    videoQualityCount: Int,
    showEpisodesAction: Boolean,
    lockButtonFocusRequester: FocusRequester,
    onToggleLock: () -> Unit,
    onOpenSubtitleTracks: () -> Unit,
    onOpenAudioTracks: () -> Unit,
    onOpenEpisodes: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenVideoTracks: () -> Unit,
    onToggleImmersive: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        AlaaControlSurface(
            label = if (isLocked) "🔓" else "🔒",
            caption = if (isLocked) "فتح القفل" else "قفل",
            onClick = onToggleLock,
            modifier = Modifier.focusRequester(lockButtonFocusRequester),
            compact = true,
            selected = isLocked
        )
        AlaaControlSurface(label = "▤", caption = if (subtitleTrackCount > 0) "ترجمة" else "لا ترجمة", onClick = onOpenSubtitleTracks, enabled = !isLocked, compact = true)
        AlaaControlSurface(label = "◖", caption = if (audioTrackCount > 1) "الصوت" else "صوت", onClick = onOpenAudioTracks, enabled = !isLocked, compact = true)
        if (showEpisodesAction) AlaaControlSurface(label = "☷", caption = "الحلقات", onClick = onOpenEpisodes, enabled = !isLocked, compact = true)
        AlaaControlSurface(label = "⚙", caption = "الإعدادات", onClick = onOpenSettings, enabled = !isLocked, compact = true)
        AlaaControlSurface(label = "HD", caption = if (videoQualityCount > 0) "الجودة" else "تلقائي", onClick = onOpenVideoTracks, enabled = !isLocked, compact = true)
        AlaaControlSurface(label = if (isImmersive) "⛶" else "▣", caption = if (isImmersive) "الخروج من الملء" else "ملء الشاشة", onClick = onToggleImmersive, enabled = !isLocked, compact = true)
    }
}

@Composable
private fun AlaaPlayerSettingsPanel(
    onDismiss: () -> Unit,
    onOpenSubtitleTracks: () -> Unit,
    onOpenAudioTracks: () -> Unit,
    onOpenVideoTracks: () -> Unit,
    onOpenPlaybackSpeed: () -> Unit,
    onToggleAspectRatio: () -> Unit,
    closeFocusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(280.dp)
            .background(Color.Black.copy(alpha = 0.92f), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("إعدادات المشغّل", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
            AlaaControlSurface(
                label = "×",
                caption = "إغلاق",
                onClick = onDismiss,
                modifier = Modifier.focusRequester(closeFocusRequester),
                compact = true
            )
        }
        AlaaSettingsRow("الترجمة", "المسارات المتاحة") { onDismiss(); onOpenSubtitleTracks() }
        AlaaSettingsRow("الصوت", "المسارات المتاحة") { onDismiss(); onOpenAudioTracks() }
        AlaaSettingsRow("الجودة", "المسارات المتاحة") { onDismiss(); onOpenVideoTracks() }
        AlaaSettingsRow("سرعة التشغيل", "التحكم الفعلي") { onDismiss(); onOpenPlaybackSpeed() }
        AlaaSettingsRow("أبعاد الفيديو", "تغيير العرض") { onDismiss(); onToggleAspectRatio() }
    }
}

@Composable
private fun AlaaSettingsRow(title: String, detail: String, onClick: () -> Unit) {
    TvClickableSurface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(10.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = AlaaThemeColors.BrowseRail,
            focusedContainerColor = AlaaThemeColors.SurfaceFocused,
            contentColor = Color.White,
            focusedContentColor = Color.White
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(BorderStroke(AlaaThemeDimensions.FocusBorder, AlaaThemeColors.Accent), 10.dp)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = AlaaThemeFocus.FocusedScale)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(detail, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.65f))
        }
    }
}

@Composable
private fun AlaaControlSurface(
    label: String,
    caption: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    primary: Boolean = false,
    compact: Boolean = false,
    selected: Boolean = false
) {
    val cornerRadius = if (primary) 50.dp else if (compact) 12.dp else 18.dp
    val shape = if (primary) CircleShape else RoundedCornerShape(cornerRadius)
    TvClickableSurface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.then(if (primary) Modifier.size(94.dp) else Modifier),
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = when {
                primary || selected -> AlaaThemeColors.Accent
                else -> Color.Black.copy(alpha = 0.6f)
            },
            focusedContainerColor = if (primary) AlaaThemeColors.AccentStrong else AlaaThemeColors.SurfaceFocused,
            contentColor = Color.White,
            focusedContentColor = Color.White
        ),
        border = ClickableSurfaceDefaults.border(
            border = Border(BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)), cornerRadius),
            focusedBorder = Border(BorderStroke(AlaaThemeDimensions.FocusBorder, AlaaThemeColors.Accent), cornerRadius)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = if (primary) 1.06f else AlaaThemeFocus.FocusedScale)
    ) {
        Column(
            modifier = Modifier
                .padding(if (compact) 8.dp else 13.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(label, style = if (primary) MaterialTheme.typography.displaySmall else MaterialTheme.typography.titleMedium, color = Color.White, textAlign = TextAlign.Center, maxLines = 1)
            if (!primary) Text(caption, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.86f), textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

private fun formatDuration(positionMs: Long): String {
    val totalSeconds = (positionMs.coerceAtLeast(0L) / 1_000L)
    val hours = totalSeconds / 3_600L
    val minutes = (totalSeconds % 3_600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0L) "%d:%02d:%02d".format(hours, minutes, seconds) else "%d:%02d".format(minutes, seconds)
}
