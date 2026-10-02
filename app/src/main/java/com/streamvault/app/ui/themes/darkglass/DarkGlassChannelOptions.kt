package com.streamvault.app.ui.themes.darkglass

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

/** DarkGlass: a centered floating frosted lens (circular logo on top) with two stacked glass pills. */
@Composable
internal fun DarkGlassChannelOptions(p: ChannelOptionsParams) {
    if (p.moving) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Row(Modifier.padding(top = 28.dp).dgGlass(DG.Pill, 0.14f).border(1.dp, DG.Amber, DG.Pill).padding(horizontal = 26.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                Text("⇅ " + p.channel.name, color = DG.Text, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Text(tr("▲▼ Move   OK Save   BACK Cancel", "▲▼ تحريك   OK حفظ   رجوع إلغاء"), color = DG.Sub, fontSize = 14.sp, fontWeight = FontWeight.Light)
            }
        }
        return
    }
    Box(Modifier.fillMaxSize().background(DG.Bg.copy(alpha = 0.55f)), contentAlignment = Alignment.Center) {
        Column(Modifier.width(380.dp).dgGlass(RoundedCornerShape(32.dp), 0.12f).padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DgLogo(p.channel.name, p.channel.logoUrl, 72.dp)
            Text(p.channel.name, color = DG.Text, fontSize = 22.sp, fontWeight = FontWeight.Thin, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Box(Modifier.fillMaxWidth().height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, DG.Amber, DG.Blue, Color.Transparent))))
            DgCard(onClick = p.onToggleFavorite, shape = DG.Pill, modifier = Modifier.fillMaxWidth().height(56.dp).focusRequester(p.focusRequester)) {
                Text(if (p.channel.isFavorite) tr("★  Remove favorite", "★  إزالة من المفضلة") else tr("☆  Add favorite", "☆  إضافة للمفضلة"), color = DG.Text, fontSize = 16.sp, modifier = Modifier.align(Alignment.Center))
            }
            DgCard(onClick = p.onStartMove, shape = DG.Pill, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Text(tr("⇅  Move", "⇅  نقل"), color = DG.Text, fontSize = 16.sp, modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}
