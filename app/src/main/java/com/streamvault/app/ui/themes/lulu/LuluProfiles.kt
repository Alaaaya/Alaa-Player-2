package com.streamvault.app.ui.themes.lulu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.streamvault.app.ui.themes.bespoke.tr

/** Real combined-source profiles, preserving selection versus activation semantics. */
@Composable
internal fun LuluProfileCard(name: String, playlistCount: Int, selected: Boolean, active: Boolean, onClick: () -> Unit) {
    LuFocus(onClick, Modifier.width(174.dp).height(208.dp), color = if(selected) LU.PurpleSoft else LU.Card) { focused ->
        Column(Modifier.fillMaxSize().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(82.dp).clip(CircleShape).background(Brush.linearGradient(listOf(LU.PurpleDeep, LU.Purple)))
                .border(if(selected || focused) 3.dp else 1.dp, if(selected || focused) Color.White else LU.Line, CircleShape), contentAlignment = Alignment.Center) {
                Text(name.trim().take(2).uppercase(), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
            Text(name, color = Color.White, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("$playlistCount " + tr("playlists", "قوائم"), color = LU.Sub, fontSize = 13.sp)
            Text(if(active) tr("● Active", "● نشط") else if(selected) tr("Selected", "محدد") else tr("Select", "اختيار"), color = if(active) LU.Gold else LU.Purple, fontSize = 12.sp)
        }
    }
}
