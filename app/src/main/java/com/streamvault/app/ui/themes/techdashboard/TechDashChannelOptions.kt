package com.streamvault.app.ui.themes.techdashboard

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

/** Tech Dashboard: a docked terminal panel at the END edge with bracketed mono commands. */
@Composable
internal fun TechDashChannelOptions(p: ChannelOptionsParams) {
    if (p.moving) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomStart) {
            Text("> mv ${p.channel.name.lowercase().replace(' ', '_')}  [▲/▼ shift]  [OK commit]  [BACK abort]", color = TD.Plasma, fontFamily = TD.Mono, fontSize = 13.sp,
                modifier = Modifier.padding(16.dp).background(TD.Void.copy(alpha = 0.92f)).border(1.dp, TD.Plasma, RoundedCornerShape(2.dp)).padding(horizontal = 14.dp, vertical = 8.dp))
        }
        return
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
        Column(Modifier.fillMaxHeight().width(360.dp).background(TD.Void.copy(alpha = 0.96f)).border(1.dp, TD.Plasma.copy(alpha = 0.5f)).padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("~/channel/options", color = TD.Dust, fontFamily = TD.Mono, fontSize = 12.sp)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TechDashLogo(p.channel.name, p.channel.logoUrl, 34.dp)
                Text(p.channel.name, color = TD.Star, fontFamily = TD.Mono, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(8.dp))
            TechDashSurface(onClick = p.onToggleFavorite, shape = TD.Pill, modifier = Modifier.fillMaxWidth().focusRequester(p.focusRequester)) {
                Text(if (p.channel.isFavorite) "[1] fav --remove" else "[1] fav --add", color = TD.Plasma, fontFamily = TD.Mono, fontSize = 14.sp, modifier = Modifier.padding(12.dp))
            }
            TechDashSurface(onClick = p.onStartMove, shape = TD.Pill, modifier = Modifier.fillMaxWidth()) {
                Text("[2] mv --reorder", color = TD.Plasma, fontFamily = TD.Mono, fontSize = 14.sp, modifier = Modifier.padding(12.dp))
            }
            TechDashSurface(onClick = p.onDismiss, shape = TD.Pill, modifier = Modifier.fillMaxWidth()) {
                Text("[esc] exit", color = TD.Dust, fontFamily = TD.Mono, fontSize = 14.sp, modifier = Modifier.padding(12.dp))
            }
        }
    }
}
