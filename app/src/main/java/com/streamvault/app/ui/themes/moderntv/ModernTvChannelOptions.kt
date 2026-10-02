package com.streamvault.app.ui.themes.moderntv

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

/** Modern TV: a warm bottom sheet rising from the bottom edge with two large amber tiles. */
@Composable
internal fun ModernTvChannelOptions(p: ChannelOptionsParams) {
    if (p.moving) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Row(Modifier.padding(bottom = 24.dp).clip(MT.R).background(MT.Amber).padding(horizontal = 24.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                Text(p.channel.name, color = MT.Bg, fontSize = 15.sp, fontWeight = FontWeight.Black)
                Text(tr("▲▼ Move   OK Save   BACK Cancel", "▲▼ تحريك   OK حفظ   رجوع إلغاء"), color = MT.Bg, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        return
    }
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, MT.Bg.copy(alpha = 0.95f)))), contentAlignment = Alignment.BottomCenter) {
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(MT.Raised).padding(28.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            MtLogo(p.channel.name, p.channel.logoUrl, 56.dp)
            Text(p.channel.name, color = MT.Text, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            MtCard(onClick = p.onToggleFavorite, container = MT.Card, focusedContainer = MT.Amber, modifier = Modifier.width(260.dp).height(64.dp).focusRequester(p.focusRequester)) {
                Text(if (p.channel.isFavorite) tr("♥  Remove favorite", "♥  إزالة من المفضلة") else tr("♡  Add favorite", "♡  إضافة للمفضلة"), color = MT.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.Center))
            }
            MtCard(onClick = p.onStartMove, container = MT.Card, focusedContainer = MT.Amber, modifier = Modifier.width(220.dp).height(64.dp)) {
                Text(tr("⇅  Move", "⇅  نقل"), color = MT.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}
