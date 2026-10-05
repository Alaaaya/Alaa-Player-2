package com.streamvault.app.ui.themes.sabhiya

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.streamvault.app.ui.themes.bespoke.*

/** Sabhiya player: glass side panel (subtitles/audio/episodes) + one bottom bar with info card (current/next), progress and controls. */
@Composable
internal fun SbsPlayer(p: PlayerOverlayParams) {
    val live = p.currentChannel != null && p.duration <= 0L && !p.isCatchUpPlayback
    AnimatedVisibility(p.visible, enter = fadeIn(), exit = fadeOut(), modifier = p.modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0f to Color(0x99000000), 0.2f to Color.Transparent, 0.55f to Color.Transparent, 1f to Color(0xF2000000))))
            // top: back + title, clock-free (shell shows time)
            Row(Modifier.align(Alignment.TopStart).padding(32.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SbRound(Icons.Outlined.ArrowBack, 48.dp) { p.onNavigateBack(); p.onClose() }
                Text(p.mediaTitle ?: p.title, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 900.dp))
            }
            // side panel on the end edge
            Column(Modifier.align(Alignment.CenterEnd).padding(end = 32.dp, bottom = 120.dp).width(250.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xCC0F1115)).border(1.dp, SBX.Line, RoundedCornerShape(16.dp)).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(tr("Options", "الخيارات"), color = SBX.Sub, fontSize = 14.sp, modifier = Modifier.padding(start = 6.dp, bottom = 2.dp))
                SbsSide(Icons.Outlined.ClosedCaption, tr("Subtitles", "الترجمة"), if (p.subtitleTrackCount > 0) "${p.subtitleTrackCount}" else tr("Off", "إيقاف"), p.onOpenSubtitleTracks)
                SbsSide(Icons.Outlined.Audiotrack, tr("Audio", "الصوت"), if (p.audioTrackCount > 0) "${p.audioTrackCount}" else "1", p.onOpenAudioTracks)
                if (p.showEpisodesAction) SbsSide(Icons.Outlined.VideoLibrary, tr("Episodes", "الحلقات"), "", p.onOpenEpisodes)
                if (live) SbsSide(Icons.Outlined.List, tr("Channels", "القنوات"), "", p.onOpenLiveChannels)
                if (live) SbsSide(Icons.Outlined.CalendarMonth, tr("Guide", "الدليل"), "", p.onOpenLiveGuide)
            }
            // bottom bar
            Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 32.dp, vertical = 26.dp).clip(RoundedCornerShape(18.dp)).background(Color(0xD90F1115)).border(1.dp, SBX.Line, RoundedCornerShape(18.dp)).padding(horizontal = 22.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    if (live) Box(Modifier.size(64.dp, 44.dp).clip(RoundedCornerShape(8.dp)).background(Color(0x14FFFFFF))) { p.currentChannel?.logoUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(4.dp)) } }
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (live) SbTag("LIVE", SBX.Red)
                            Text(if (live) (p.currentProgram?.title ?: p.currentChannelName.orEmpty()) else (p.episodeLine ?: p.title), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        val next = if (live) p.nextProgram?.let { "${tr("Next", "التالي")} ${sbTime(it.startTime)}  ${it.title}" } else null
                        Text(next ?: (if (live) listOfNotNull(p.displayChannelNumber.takeIf { it > 0 }?.let { "#$it" }, p.currentChannelName).joinToString(" • ") else ""), color = SBX.Sub, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    SbTag(p.resolutionBadgeLabel ?: "AUTO", Color(0x66FFFFFF), filled = false)
                }
                val frac = if (live) p.currentProgram?.let { pr -> if (pr.endTime > pr.startTime) ((System.currentTimeMillis() - pr.startTime).toFloat() / (pr.endTime - pr.startTime)).coerceIn(0f, 1f) else 1f } ?: 1f
                           else if (p.duration > 0) (p.currentPosition.toFloat() / p.duration).coerceIn(0f, 1f) else 0f
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(if (live) p.currentProgram?.let { sbTime(it.startTime) } ?: "" else cgDuration(p.currentPosition), color = SBX.Sub, fontSize = 14.sp)
                    BoxWithConstraints(Modifier.weight(1f).height(16.dp)) {
                        Box(Modifier.align(Alignment.CenterStart).fillMaxWidth().height(5.dp).clip(CircleShape).background(Color(0x33FFFFFF)))
                        Box(Modifier.align(Alignment.CenterStart).fillMaxWidth(frac.coerceAtLeast(0.005f)).height(5.dp).clip(CircleShape).background(SBX.Red))
                        Box(Modifier.align(Alignment.CenterStart).offset(x = (maxWidth - 16.dp) * frac).size(16.dp).clip(CircleShape).background(SBX.Red))
                    }
                    Text(if (live) p.currentProgram?.let { sbTime(it.endTime) } ?: "" else cgDuration(p.duration), color = SBX.Sub, fontSize = 14.sp)
                }
                Box(Modifier.fillMaxWidth().focusRequester(p.quickActionsFocusRequester)) {
                    Row(Modifier.align(Alignment.CenterStart), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SbsCtl(if (p.isMuted) Icons.Outlined.VolumeOff else Icons.Outlined.VolumeUp, 46.dp, p.isMuted, p.onToggleMute)
                        if (live) SbsCtl(if (p.currentChannel?.isFavorite == true) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder, 46.dp, p.currentChannel?.isFavorite == true, p.onToggleLiveFavorite)
                    }
                    Row(Modifier.align(Alignment.Center), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                        SbsCtl(Icons.Outlined.Replay10, 50.dp, false, p.onSeekBackward)
                        SbFocus(p.onTogglePlayPause, Modifier.size(64.dp).focusRequester(p.playButtonFocusRequester), shape = CircleShape, color = SBX.Red, focusedColor = Color(0xFFFF2A36), scale = 1.12f) {
                            Box(Modifier.align(Alignment.Center)) { SbIcon(if (p.isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow, size = 40.dp) }
                        }
                        SbsCtl(Icons.Outlined.Forward10, 50.dp, false, p.onSeekForward)
                    }
                    Row(Modifier.align(Alignment.CenterEnd), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SbsCtl(Icons.Outlined.HighQuality, 46.dp, false, p.onOpenVideoTracks)
                        SbsCtl(Icons.Outlined.AspectRatio, 46.dp, false, p.onToggleAspectRatio)
                        SbsCtl(Icons.Outlined.PictureInPicture, 46.dp, false, p.onEnterPictureInPicture)
                        SbsCtl(Icons.Outlined.Settings, 46.dp, false, p.onOpenPlaybackSpeed)
                    }
                }
            }
        }
    }
}

@Composable
private fun SbsCtl(icon: ImageVector, size: Dp, active: Boolean, onClick: () -> Unit) {
    SbFocus(onClick, Modifier.size(size), shape = CircleShape, color = if (active) SBX.RedSoft else Color(0x1AFFFFFF), focusedColor = SBX.Red, scale = 1.12f) {
        Box(Modifier.align(Alignment.Center)) { SbIcon(icon, size = size * 0.5f) }
    }
}

@Composable
private fun SbsSide(icon: ImageVector, label: String, value: String, onClick: () -> Unit) {
    SbFocus(onClick, Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(10.dp), color = Color(0x0FFFFFFF), focusedColor = SBX.Red, scale = 1.03f) {
        Row(Modifier.fillMaxSize().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SbIcon(icon, Color.White, 20.dp)
            Text(label, color = Color.White, fontSize = 15.sp, modifier = Modifier.weight(1f), maxLines = 1)
            if (value.isNotBlank()) Text(value, color = SBX.Sub, fontSize = 13.sp)
        }
    }
}
