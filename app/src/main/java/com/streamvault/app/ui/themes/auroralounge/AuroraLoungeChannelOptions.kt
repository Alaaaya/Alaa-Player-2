package com.streamvault.app.ui.themes.auroralounge

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

/** AuroraLounge: a round "moon" dialog in the middle of the room. Logo moon on top, serif name, two stacked
 *  candle pills (Favorite / Move). Move mode = a small crescent pill hanging at the TOP-center. */
@Composable
internal fun AuroraLoungeChannelOptions(p: ChannelOptionsParams) {
    if (p.moving) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Row(Modifier.padding(top = 28.dp).clip(AL.Pill).background(Brush.horizontalGradient(listOf(AL.Rose, AL.Amber))).padding(horizontal = 26.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("☾ " + p.channel.name, color = AL.Bg, fontSize = 15.sp, fontWeight = FontWeight.Black, maxLines = 1)
                Text(tr("▲▼ Move   OK Save   BACK Cancel", "▲▼ تحريك   OK حفظ   رجوع إلغاء"), color = AL.Bg, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        return
    }
    Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(AL.Bg.copy(alpha = 0.92f), AL.Bg.copy(alpha = 0.6f)))), contentAlignment = Alignment.Center) {
        Column(
            Modifier.size(440.dp).clip(CircleShape).background(Brush.radialGradient(listOf(AL.Card, AL.Raised, AL.Bg))).border(1.dp, AL.Amber.copy(alpha = 0.4f), CircleShape).padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically)
        ) {
            AlLogo(p.channel.name, p.channel.logoUrl, 72.dp)
            Text(p.channel.name, color = AL.Text, fontSize = 22.sp, fontWeight = FontWeight.Light, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, maxLines = 1, overflow = TextOverflow.Ellipsis)
            AlButton(if (p.channel.isFavorite) tr("Remove favorite", "إزالة من المفضلة") else tr("Add favorite", "إضافة للمفضلة"), p.onToggleFavorite, Modifier.width(260.dp).focusRequester(p.focusRequester), primary = true, icon = if (p.channel.isFavorite) "♥" else "♡")
            AlButton(tr("Move", "نقل"), p.onStartMove, Modifier.width(260.dp), icon = "⇅")
        }
    }
}
