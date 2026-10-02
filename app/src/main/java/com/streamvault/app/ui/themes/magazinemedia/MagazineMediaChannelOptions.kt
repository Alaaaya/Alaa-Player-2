package com.streamvault.app.ui.themes.magazinemedia

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.streamvault.app.ui.themes.bespoke.ChannelOptionsParams
import com.streamvault.app.ui.themes.bespoke.tr

/** MagazineMedia: a "clipping" pinned near the END edge: paper card, ink frame, kicker + serif headline,
 *  two numbered editorial entries. Move mode = a correction-notice strip along the top edge. */
@Composable
internal fun MagazineMediaChannelOptions(p: ChannelOptionsParams) {
    if (p.moving) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.padding(top = 24.dp).background(MZ.Raised).border(2.dp, MZ.Text).padding(horizontal = 24.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                MzKicker(tr("Correction · rearranging", "تعديل · إعادة ترتيب"))
                Text(p.channel.name, color = MZ.Text, fontSize = 18.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Black)
                Text(tr("▲▼ move   ·   OK save   ·   BACK cancel", "▲▼ تحريك   ·   OK حفظ   ·   رجوع إلغاء"), color = MZ.Sub, fontSize = 13.sp, fontStyle = FontStyle.Italic)
            }
        }
        return
    }
    Box(Modifier.fillMaxSize().background(MZ.Text.copy(alpha = 0.35f)), contentAlignment = Alignment.CenterEnd) {
        Column(Modifier.padding(end = 64.dp).width(440.dp).background(MZ.Bg).border(2.dp, MZ.Text).padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MzKicker(tr("Channel desk", "مكتب القناة"))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                MzLogo(p.channel.name, p.channel.logoUrl, 52.dp)
                Text(p.channel.name, color = MZ.Text, fontSize = 24.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Black, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            MzRule(thick = 3.dp)
            Entry("01", if (p.channel.isFavorite) tr("Remove from favorites", "إزالة من المفضلة") else tr("Add to favorites", "إضافة للمفضلة"), if (p.channel.isFavorite) "♥" else "♡", p.onToggleFavorite, Modifier.focusRequester(p.focusRequester))
            MzRule(color = MZ.Line)
            Entry("02", tr("Move channel", "نقل القناة"), "⇅", p.onStartMove)
            MzRule(color = MZ.Line)
            Text(tr("BACK to close", "رجوع للإغلاق"), color = MZ.Faint, fontSize = 12.sp, fontStyle = FontStyle.Italic)
        }
    }
}

@Composable
private fun Entry(num: String, label: String, glyph: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    MzCard(onClick = onClick, container = Color.Transparent, focusedContainer = MZ.Gold, zoom = 1.02f, modifier = modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(num, color = MZ.Amber, fontSize = 22.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Black)
            Text(label, color = MZ.Text, fontSize = 17.sp, fontFamily = MZ.Serif, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text(glyph, color = MZ.Amber, fontSize = 18.sp)
        }
    }
}
