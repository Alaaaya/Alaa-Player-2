package com.streamvault.app.ui.themes.cyanpro

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.Text

/** Maps the theme's legacy glyph keys to Material outlined line icons so every key reads as one icon family. */
internal fun cgIcon(glyph: String): ImageVector? = when (glyph) {
    "⌕" -> Icons.Outlined.Search
    "⌂" -> Icons.Outlined.Home
    "◉", "📺" -> Icons.Outlined.LiveTv
    "▦" -> Icons.Outlined.CalendarViewMonth
    "🎞" -> Icons.Outlined.Movie
    "❏", "🎬" -> Icons.Outlined.VideoLibrary
    "♥" -> Icons.Outlined.Favorite
    "♡" -> Icons.Outlined.FavoriteBorder
    "☆", "★" -> Icons.Outlined.StarOutline
    "⚙" -> Icons.Outlined.Settings
    "▶", "▷" -> Icons.Outlined.PlayArrow
    "❚❚" -> Icons.Outlined.Pause
    "☰", "≣" -> Icons.AutoMirrored.Outlined.FormatListBulleted
    "⏪", "↺" -> Icons.Outlined.Replay10
    "⏩", "↻" -> Icons.Outlined.Forward10
    "⏮" -> Icons.Outlined.SkipPrevious
    "⟲" -> Icons.Outlined.History
    "⇥" -> Icons.Outlined.FastForward
    "CC" -> Icons.Outlined.ClosedCaption
    "HD" -> Icons.Outlined.HighQuality
    "♪" -> Icons.Outlined.Audiotrack
    "🔇" -> Icons.AutoMirrored.Outlined.VolumeOff
    "🔊" -> Icons.AutoMirrored.Outlined.VolumeUp
    "■" -> Icons.Outlined.Stop
    "●" -> Icons.Outlined.FiberManualRecord
    "◷" -> Icons.Outlined.Schedule
    "▭" -> Icons.Outlined.AspectRatio
    "»" -> Icons.Outlined.Speed
    "⇄" -> Icons.Outlined.SyncAlt
    "◫" -> Icons.Outlined.GridView
    "⏲" -> Icons.Outlined.Timer
    "☾" -> Icons.Outlined.Bedtime
    "⧉" -> Icons.Outlined.PictureInPicture
    "⎚" -> Icons.Outlined.Cast
    "✕" -> Icons.Outlined.Close
    "⋯" -> Icons.Outlined.MoreHoriz
    "⇋" -> Icons.Outlined.SwapHoriz
    "∿" -> Icons.Outlined.QueryStats
    "↓" -> Icons.Outlined.Download
    "←" -> Icons.AutoMirrored.Outlined.ArrowBack
    "📁" -> Icons.Outlined.Folder
    "🔒" -> Icons.Outlined.Lock
    "film" -> Icons.Outlined.Theaters
    "clap" -> Icons.Outlined.Movie
    "playlist" -> Icons.AutoMirrored.Outlined.PlaylistPlay
    "bell" -> Icons.Outlined.Notifications
    "user" -> Icons.Outlined.AccountCircle
    "info" -> Icons.Outlined.Info
    "kids" -> Icons.Outlined.ChildCare
    "news" -> Icons.Outlined.Newspaper
    "sport" -> Icons.Outlined.SportsBasketball
    "prev" -> Icons.Outlined.ChevronLeft
    "next" -> Icons.Outlined.ChevronRight
    "sort" -> Icons.Outlined.FilterList
    "server" -> Icons.Outlined.Dns
    "clock" -> Icons.Outlined.Schedule
    "apps" -> Icons.Outlined.GridView
    "star" -> Icons.Outlined.Star
    "filter" -> Icons.Outlined.Tune
    "lock" -> Icons.Outlined.Lock
    "theme" -> Icons.Outlined.Palette
    "lang" -> Icons.Outlined.Translate
    "rec" -> Icons.Outlined.FiberManualRecord
    "backup" -> Icons.Outlined.Backup
    "guide" -> Icons.Outlined.CalendarViewMonth
    "refresh" -> Icons.Outlined.Refresh
    "!" -> Icons.Outlined.ErrorOutline
    "‹" -> Icons.Outlined.ChevronLeft
    "⌫" -> Icons.AutoMirrored.Outlined.Backspace
    else -> null
}

/** Draws [glyph] as a line icon when mapped, otherwise as bold text (e.g. "A/V"). */
@Composable
internal fun CgGlyph(glyph: String, size: Dp, modifier: Modifier = Modifier, tint: Color = LocalContentColor.current) {
    val v = cgIcon(glyph)
    if (v != null) Icon(v, contentDescription = null, tint = tint, modifier = modifier.size(size))
    else Text(glyph, color = tint, fontSize = (size.value * 0.7f).sp, fontWeight = FontWeight.Bold, maxLines = 1, modifier = modifier)
}

/** Picks a meaningful line icon for a channel/VOD category from its (Arabic or Latin) name. */
internal fun cgCategoryIcon(name: String): ImageVector {
    val n = name.lowercase()
    fun has(vararg k: String) = k.any { it in n }
    return when {
        has("news", "أخبار", "اخبار", "خبر") -> Icons.Outlined.Newspaper
        has("sport", "رياض", "كرة", "bein", "football") -> Icons.Outlined.SportsSoccer
        has("kid", "child", "أطفال", "اطفال", "كرتون", "cartoon", "toon") -> Icons.Outlined.ChildCare
        has("movie", "film", "cinema", "أفلام", "افلام", "سينما") -> Icons.Outlined.Movie
        has("series", "مسلسل", "drama", "دراما") -> Icons.Outlined.VideoLibrary
        has("music", "موسيق", "أغاني", "اغاني", "song") -> Icons.Outlined.MusicNote
        has("islam", "quran", "قرآن", "قران", "دين", "إسلام", "اسلام", "relig") -> Icons.Outlined.Mosque
        has("doc", "وثائق", "nature", "طبيع") -> Icons.Outlined.Public
        has("anime", "أنمي", "انمي") -> Icons.Outlined.AutoAwesome
        has("action", "أكشن", "اكشن") -> Icons.Outlined.LocalFireDepartment
        has("comedy", "كوميد") -> Icons.Outlined.SentimentVerySatisfied
        has("horror", "رعب") -> Icons.Outlined.DarkMode
        has("crime", "جريم") -> Icons.Outlined.Gavel
        has("roman", "رومانس", "رومانسي") -> Icons.Outlined.FavoriteBorder
        has("family", "عائل") -> Icons.Outlined.FamilyRestroom
        has("histor", "تاريخ") -> Icons.Outlined.AccountBalance
        has("war", "حرب") -> Icons.Outlined.Shield
        has("sci", "خيال") -> Icons.Outlined.RocketLaunch
        has("advent", "مغامر") -> Icons.Outlined.Explore
        has("thrill", "تشويق", "إثارة", "اثارة") -> Icons.Outlined.Psychology
        has("arab", "عرب", "turk", "ترك", "korea", "كور", "india", "هند", "foreign", "أجنب", "اجنب", "kurd", "كرد", "iran", "إيران") -> Icons.Outlined.Flag
        has("all", "الكل", "كل ", "جميع") -> Icons.Outlined.Apps
        has("fav", "مفضل") -> Icons.Outlined.Favorite
        has("recent", "أخير", "اخير") -> Icons.Outlined.History
        else -> Icons.Outlined.LiveTv
    }
}

@Composable
internal fun CgCategoryGlyph(name: String, locked: Boolean, tint: Color, size: Dp) {
    Icon(if (locked) Icons.Outlined.Lock else cgCategoryIcon(name), contentDescription = null, tint = tint, modifier = Modifier.size(size))
}
