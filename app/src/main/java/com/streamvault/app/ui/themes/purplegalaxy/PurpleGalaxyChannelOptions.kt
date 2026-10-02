package com.streamvault.app.ui.themes.purplegalaxy

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

/** Purple Galaxy: a centered orbit card with two glowing pill actions over a dimmed starfield. */
@Composable
internal fun PurpleGalaxyChannelOptions(p: ChannelOptionsParams) {
    if (p.moving) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Row(Modifier.padding(top = 24.dp).clip(PG.Pill).background(Brush.horizontalGradient(listOf(PG.Comet.copy(alpha = 0.85f), PG.Flare.copy(alpha = 0.85f)))).padding(horizontal = 22.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("⇅", color = PG.Star, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(tr("Moving ${p.channel.name}  •  ▲▼ move  •  OK save  •  BACK cancel", "نقل ${p.channel.name}  •  ▲▼ تحريك  •  OK حفظ  •  رجوع إلغاء"), color = PG.Star, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        return
    }
    Box(Modifier.fillMaxSize().background(PG.Void.copy(alpha = 0.6f)), contentAlignment = Alignment.Center) {
        Column(Modifier.width(420.dp).clip(PG.Card).background(Brush.verticalGradient(listOf(PG.Deep, PG.Void))).border(1.dp, PG.Comet.copy(alpha = 0.6f), PG.Card).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            PlanetLogo(p.channel.name, p.channel.logoUrl, 64.dp)
            Text(p.channel.name, color = PG.Star, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            GalaxySurface(onClick = p.onToggleFavorite, shape = PG.Pill, modifier = Modifier.fillMaxWidth().focusRequester(p.focusRequester)) {
                Text(if (p.channel.isFavorite) tr("★  Remove from favorites", "★  إزالة من المفضلة") else tr("☆  Add to favorites", "☆  إضافة للمفضلة"), color = PG.Star, fontSize = 15.sp, modifier = Modifier.align(Alignment.Center).padding(12.dp))
            }
            GalaxySurface(onClick = p.onStartMove, shape = PG.Pill, modifier = Modifier.fillMaxWidth()) {
                Text(tr("⇅  Move channel", "⇅  نقل القناة"), color = PG.Star, fontSize = 15.sp, modifier = Modifier.align(Alignment.Center).padding(12.dp))
            }
        }
    }
}
