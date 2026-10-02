package com.streamvault.app.ui.themes.mediacenter

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

/** Media Center: centered receiver "channel edit" dialog; move mode = framed hint plate at bottom. */
@Composable
internal fun MediaCenterChannelOptions(p: ChannelOptionsParams) {
    if (p.moving) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Row(Modifier.padding(bottom = 24.dp).background(MC.Bg).border(1.dp, MC.Amber).padding(horizontal = 24.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                Text(p.channel.name, color = MC.Amber, fontSize = 15.sp, fontFamily = MC.Serif)
                Text(tr("▲▼ Move   OK Save   BACK Cancel", "▲▼ تحريك   OK حفظ   رجوع إلغاء"), color = MC.Sub, fontSize = 15.sp)
            }
        }
        return
    }
    // receiver "channel edit" dialog: centered framed menu, serif entries, gold header bar
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)), contentAlignment = Alignment.Center) {
        Column(Modifier.width(420.dp).background(MC.Bg).border(1.dp, MC.Amber)) {
            Row(Modifier.fillMaxWidth().background(MC.Amber).padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(tr("CHANNEL EDIT", "تعديل القناة"), color = MC.Bg, fontSize = 12.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
            }
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                McLogo(p.channel.name, p.channel.logoUrl, 48.dp)
                Text(p.channel.name, color = MC.Text, fontSize = 20.sp, fontFamily = MC.Serif, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(MC.Line))
            Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                McCard(onClick = p.onToggleFavorite, shape = MC.RSmall, container = Color.Transparent, modifier = Modifier.fillMaxWidth().focusRequester(p.focusRequester)) {
                    Text(if (p.channel.isFavorite) tr("I.   Remove from favorites", "١.  إزالة من المفضلة") else tr("I.   Add to favorites", "١.  إضافة للمفضلة"), color = MC.Text, fontSize = 16.sp, fontFamily = MC.Serif, modifier = Modifier.padding(14.dp))
                }
                McCard(onClick = p.onStartMove, shape = MC.RSmall, container = Color.Transparent, modifier = Modifier.fillMaxWidth()) {
                    Text(tr("II.  Move channel", "٢.  نقل القناة"), color = MC.Text, fontSize = 16.sp, fontFamily = MC.Serif, modifier = Modifier.padding(14.dp))
                }
            }
        }
    }
}
