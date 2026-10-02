package com.streamvault.app.ui.themes.softmodern

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

/** Soft Modern: a centered cream card with the channel's round logo on top and two stacked pill choices. */
@Composable
internal fun SoftModernChannelOptions(p: ChannelOptionsParams) {
    if (p.moving) {
        Box(Modifier.fillMaxSize().padding(top = 28.dp), contentAlignment = Alignment.TopCenter) {
            Row(Modifier.clip(SM.Pill).background(SM.Card).border(2.dp, SM.Amber, SM.Pill).padding(horizontal = 22.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(28.dp).clip(CircleShape).background(SM.Amber), contentAlignment = Alignment.Center) { Text("⇅", color = SM.Card, fontSize = 14.sp) }
                Text(p.channel.name, color = SM.Text, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(tr("▲▼ move · OK save · BACK cancel", "▲▼ تحريك · OK حفظ · رجوع إلغاء"), color = SM.Sub, fontSize = 14.sp)
            }
        }
        return
    }
    Box(Modifier.fillMaxSize().background(SM.Text.copy(alpha = 0.35f)), contentAlignment = Alignment.Center) {
        Column(Modifier.width(380.dp).clip(SM.R).background(SM.Bg).padding(26.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(84.dp).clip(CircleShape).background(SM.Card).padding(10.dp), contentAlignment = Alignment.Center) { SmLogo(p.channel.name, p.channel.logoUrl, 60.dp) }
            Text(p.channel.name, color = SM.Text, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            SmCard(onClick = p.onToggleFavorite, shape = SM.Pill, container = SM.Card, focusedContainer = SM.Sage, modifier = Modifier.fillMaxWidth().height(54.dp).focusRequester(p.focusRequester)) {
                Text(if (p.channel.isFavorite) tr("♥  Remove from favorites", "♥  إزالة من المفضلة") else tr("♡  Add to favorites", "♡  إضافة للمفضلة"), color = SM.Text, fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.align(Alignment.Center))
            }
            SmCard(onClick = p.onStartMove, shape = SM.Pill, container = SM.Card, focusedContainer = SM.Sage, modifier = Modifier.fillMaxWidth().height(54.dp)) {
                Text(tr("⇅  Move channel", "⇅  نقل القناة"), color = SM.Text, fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.align(Alignment.Center))
            }
            Text(tr("BACK to close", "رجوع للإغلاق"), color = SM.Faint, fontSize = 12.sp)
        }
    }
}
