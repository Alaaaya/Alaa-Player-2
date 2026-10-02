package com.streamvault.app.ui.themes.nextgentv

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.streamvault.app.ui.themes.bespoke.ChannelOptionsParams
import com.streamvault.app.ui.themes.bespoke.tr

/** NextGenTv: centered floating pane with depth-stacked keys; move mode = cyan-ringed capsule. */
@Composable
internal fun NextGenTvChannelOptions(p: ChannelOptionsParams) {
    if (p.moving) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Row(Modifier.padding(bottom = 28.dp).clip(NG.Pill).background(NG.Raised).border(2.dp, NG.Amber, NG.Pill).padding(horizontal = 24.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                Text(p.channel.name, color = NG.Amber, fontSize = 15.sp, fontWeight = FontWeight.Black)
                Text(tr("▲▼ Move   OK Save   BACK Cancel", "▲▼ تحريك   OK حفظ   رجوع إلغاء"), color = NG.Text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        return
    }
    // Spatial: a floating pane in the middle of the room with two keys stacked in depth.
    Box(Modifier.fillMaxSize().background(NG.Bg.copy(alpha = 0.7f)), contentAlignment = Alignment.Center) {
        NgPane(Modifier.width(520.dp), depth = 16.dp) {
            Column(Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                NgLogo(p.channel.name, p.channel.logoUrl, 72.dp)
                Text(p.channel.name, color = NG.Text, fontSize = 22.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Box(Modifier.width(60.dp).height(3.dp).clip(NG.Pill).background(Brush.horizontalGradient(listOf(NG.Amber, NG.Violet))))
                NgCard(onClick = p.onToggleFavorite, focusedContainer = Color(0xFF243052), modifier = Modifier.fillMaxWidth().height(62.dp).focusRequester(p.focusRequester)) {
                    Text(if (p.channel.isFavorite) tr("♥  Remove favorite", "♥  إزالة من المفضلة") else tr("♡  Add favorite", "♡  إضافة للمفضلة"), color = NG.Text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.Center))
                }
                NgCard(onClick = p.onStartMove, focusedContainer = Color(0xFF243052), modifier = Modifier.fillMaxWidth(0.86f).height(56.dp)) {
                    Text(tr("⇅  Move channel", "⇅  نقل القناة"), color = NG.Sub, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.Center))
                }
                Text(tr("BACK to close", "رجوع للإغلاق"), color = NG.Faint, fontSize = 12.sp)
            }
        }
    }
}
