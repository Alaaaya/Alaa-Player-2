package com.streamvault.app.ui.themes.sportstv

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

/** SportsTv: lower-third "substitution board" sliding in at bottom-START: lime header, channel plate, two numbered options. */
@Composable
internal fun SportsTvChannelOptions(p: ChannelOptionsParams) {
    if (p.moving) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Row(Modifier.padding(top = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.background(ST.Live).padding(horizontal = 12.dp, vertical = 10.dp)) { Text("⇅ " + tr("MOVE", "نقل"), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black) }
                Box(Modifier.background(ST.Amber).padding(horizontal = 14.dp, vertical = 10.dp)) { Text(p.channel.name.uppercase(), color = ST.Bg, fontSize = 14.sp, fontWeight = FontWeight.Black) }
                Box(Modifier.background(ST.Bg).padding(horizontal = 14.dp, vertical = 10.dp)) { Text(tr("▲▼ MOVE   OK SAVE   BACK CANCEL", "▲▼ تحريك   OK حفظ   رجوع إلغاء"), color = ST.Text, fontSize = 13.sp, fontWeight = FontWeight.Black) }
            }
        }
        return
    }
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.BottomStart) {
        Column(Modifier.padding(start = 48.dp, bottom = 48.dp).width(440.dp).background(ST.Bg)) {
            Box(Modifier.fillMaxWidth().background(ST.Amber).padding(horizontal = 14.dp, vertical = 8.dp)) { Text(tr("CHANNEL OPTIONS", "خيارات القناة"), color = ST.Bg, fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp) }
            Row(Modifier.fillMaxWidth().background(ST.Raised).padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StLogo(p.channel.name, p.channel.logoUrl, 48.dp)
                Text(p.channel.name.uppercase(), color = ST.Text, fontSize = 17.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            }
            Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                StOpt("1", if (p.channel.isFavorite) tr("Remove from favorites", "إزالة من المفضلة") else tr("Add to favorites", "إضافة للمفضلة"), if (p.channel.isFavorite) "★" else "☆", p.onToggleFavorite, Modifier.focusRequester(p.focusRequester))
                StOpt("2", tr("Move channel", "نقل القناة"), "⇅", p.onStartMove)
            }
        }
    }
}

@Composable
private fun StOpt(num: String, label: String, glyph: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    StCard(onClick = onClick, shape = ST.Pill, zoom = 1.02f, container = ST.Card, focusedContainer = ST.Line, modifier = modifier.fillMaxWidth()) {
        Row(Modifier.height(50.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(46.dp).fillMaxHeight().background(ST.Amber), contentAlignment = Alignment.Center) { Text(num, color = ST.Bg, fontSize = 18.sp, fontWeight = FontWeight.Black) }
            Text(label.uppercase(), color = ST.Text, fontSize = 14.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f).padding(horizontal = 12.dp))
            Text(glyph, color = ST.Amber, fontSize = 18.sp, modifier = Modifier.padding(end = 14.dp))
        }
    }
}
