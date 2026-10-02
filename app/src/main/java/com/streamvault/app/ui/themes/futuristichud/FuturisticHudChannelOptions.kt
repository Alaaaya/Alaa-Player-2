package com.streamvault.app.ui.themes.futuristichud

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

/** FuturisticHud: a warm bottom sheet rising from the bottom edge with two large amber tiles. */
@Composable
internal fun FuturisticHudChannelOptions(p: ChannelOptionsParams) {
    if (p.moving) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Row(Modifier.padding(bottom = 24.dp).clip(FH.R).background(FH.Amber).padding(horizontal = 24.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                Text(p.channel.name, color = FH.Bg, fontSize = 15.sp, fontWeight = FontWeight.Black)
                Text(tr("▲▼ Move   OK Save   BACK Cancel", "▲▼ تحريك   OK حفظ   رجوع إلغاء"), color = FH.Bg, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        return
    }
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, FH.Bg.copy(alpha = 0.95f)))), contentAlignment = Alignment.BottomCenter) {
        Row(Modifier.fillMaxWidth().clip(FH.R).background(FH.Bg).fhBrackets(FH.Amber, 22.dp, 3.dp).padding(28.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            FhLogo(p.channel.name, p.channel.logoUrl, 56.dp)
            Text(p.channel.name, color = FH.Text, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            FhCard(onClick = p.onToggleFavorite, container = FH.Card, focusedContainer = FH.Amber, modifier = Modifier.width(260.dp).height(64.dp).focusRequester(p.focusRequester)) {
                Text(if (p.channel.isFavorite) tr("♥  Remove favorite", "♥  إزالة من المفضلة") else tr("♡  Add favorite", "♡  إضافة للمفضلة"), color = FH.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.Center))
            }
            FhCard(onClick = p.onStartMove, container = FH.Card, focusedContainer = FH.Amber, modifier = Modifier.width(220.dp).height(64.dp)) {
                Text(tr("⇅  Move", "⇅  نقل"), color = FH.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}
