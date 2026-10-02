package com.streamvault.app.ui.themes.cardstack

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

/** Card Stack: two tilted playing cards dealt from the END edge; the channel is the "face" card. */
@Composable
internal fun CardStackChannelOptions(p: ChannelOptionsParams) {
    if (p.moving) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopEnd) {
            Column(Modifier.padding(24.dp).rotate(3f).clip(CS.RSmall).background(CS.Amber).padding(horizontal = 18.dp, vertical = 12.dp)) {
                Text(tr("Shuffling ${p.channel.name}", "ترتيب ${p.channel.name}"), color = CS.Bg, fontSize = 15.sp, fontWeight = FontWeight.Black)
                Text(tr("▲▼ move  ·  OK place  ·  BACK cancel", "▲▼ تحريك  ·  OK حفظ  ·  رجوع إلغاء"), color = CS.Bg, fontSize = 13.sp)
            }
        }
        return
    }
    Box(Modifier.fillMaxSize().background(CS.Bg.copy(alpha = 0.55f)), contentAlignment = Alignment.CenterEnd) {
        Row(Modifier.padding(end = 48.dp), horizontalArrangement = Arrangement.spacedBy((-18).dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.width(200.dp).height(260.dp).rotate(-6f).clip(CS.R).background(CS.Card).border(2.dp, CS.Line, CS.R).padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
                Text("${p.channel.number.takeIf { it > 0 } ?: ""}", color = CS.Amber, fontSize = 22.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.Start))
                CsLogo(p.channel.name, p.channel.logoUrl, 72.dp)
                Text(p.channel.name, color = CS.Text, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Column(Modifier.width(260.dp).rotate(4f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CsCard({ p.onToggleFavorite() }, Modifier.fillMaxWidth().height(72.dp).focusRequester(p.focusRequester), container = CS.Raised) {
                    Text(if (p.channel.isFavorite) tr("♠  Remove favorite", "♠  إزالة من المفضلة") else tr("♤  Add favorite", "♤  إضافة للمفضلة"), color = CS.Text, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterStart).padding(horizontal = 16.dp))
                }
                CsCard({ p.onStartMove() }, Modifier.fillMaxWidth().height(72.dp), container = CS.Raised) {
                    Text(tr("⇅  Move channel", "⇅  نقل القناة"), color = CS.Text, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterStart).padding(horizontal = 16.dp))
                }
            }
        }
    }
}
