package com.streamvault.app.ui.themes.futuristichud

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.streamvault.app.ui.themes.bespoke.ChannelOptionsParams
import com.streamvault.app.ui.themes.bespoke.tr

/** HUD target-lock dialog: centred bracket frame, channel ID header, numbered vertical command list.
 *  Move mode = slim top "REPOSITION" strip with key legend (list stays visible underneath). */
@Composable
internal fun FuturisticHudChannelOptions(p: ChannelOptionsParams) {
    if (p.moving) {
        Box(Modifier.fillMaxSize().padding(top = 24.dp), contentAlignment = Alignment.TopCenter) {
            Row(Modifier.background(FH.Bg.copy(alpha = 0.95f)).border(1.dp, FH.Amber).fhBrackets(FH.Amber, 12.dp).padding(horizontal = 20.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("◢ " + tr("REPOSITION", "نقل"), color = FH.Live, fontSize = 13.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Black)
                Text(p.channel.name.uppercase(), color = FH.Text, fontSize = 13.sp, fontFamily = FH.Mono, maxLines = 1)
                Text(tr("[▲▼] MOVE  [OK] SAVE  [BACK] ABORT", "[▲▼] تحريك  [OK] حفظ  [رجوع] إلغاء"), color = FH.Amber, fontSize = 12.sp, fontFamily = FH.Mono)
            }
        }
        return
    }
    Box(Modifier.fillMaxSize().background(FH.Bg.copy(alpha = 0.6f)), contentAlignment = Alignment.Center) {
        Column(Modifier.width(440.dp).background(FH.Bg).border(1.dp, FH.Line).fhBrackets(FH.Amber, 26.dp, 3.dp).padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("// " + tr("TARGET OPTIONS", "خيارات القناة"), color = FH.Amber, fontSize = 13.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Black)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FhLogo(p.channel.name, p.channel.logoUrl, 44.dp)
                Column(Modifier.weight(1f)) {
                    Text(p.channel.name.uppercase(), color = FH.Text, fontSize = 16.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(if (p.channel.isFavorite) "STATUS: ★ " + tr("SAVED", "مفضلة") else "STATUS: ○", color = FH.Sub, fontSize = 10.sp, fontFamily = FH.Mono)
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(FH.Line))
            Opt("01", if (p.channel.isFavorite) tr("Remove from favorites", "إزالة من المفضلة") else tr("Add to favorites", "إضافة للمفضلة"), if (p.channel.isFavorite) "★" else "☆", p.onToggleFavorite, Modifier.focusRequester(p.focusRequester))
            Opt("02", tr("Move channel", "نقل القناة"), "⇅", p.onStartMove)
            Opt("03", tr("Cancel", "إلغاء"), "✕", p.onDismiss)
        }
    }
}

@Composable
private fun Opt(code: String, label: String, glyph: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    FhCard(onClick = onClick, shape = FH.RSmall, zoom = 1.0f, container = Color.Transparent, focusedContainer = FH.Amber.copy(alpha = 0.3f), modifier = modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(code, color = FH.Faint, fontSize = 11.sp, fontFamily = FH.Mono)
            Text(glyph, color = FH.Amber, fontSize = 15.sp)
            Text(label.uppercase(), color = FH.Text, fontSize = 13.sp, fontFamily = FH.Mono, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}
