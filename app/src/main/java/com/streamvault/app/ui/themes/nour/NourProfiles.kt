package com.streamvault.app.ui.themes.nour

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.streamvault.app.ui.themes.bespoke.tr

/** Styles real combined-source profiles; does not introduce fictitious user accounts. */
@Composable
internal fun NourProfileCard(name: String, playlistCount: Int, selected: Boolean, active: Boolean, onClick: () -> Unit) {
    NourCard(onClick, modifier = Modifier.width(164.dp),
        container = if (selected) NR.Raised else NR.Card) {
        Column(Modifier.fillMaxWidth().padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(70.dp).clip(CircleShape)
                .background(Brush.linearGradient(listOf(NR.Line, NR.Raised)))
                .border(if (selected) 2.dp else 1.dp, if (selected) NR.Text else NR.Line, CircleShape),
                contentAlignment = Alignment.Center) {
                Text(name.trim().take(2).uppercase(), color = NR.Text, fontSize = 25.sp, fontWeight = FontWeight.Bold)
            }
            Text(name, color = NR.Text, fontSize = 15.sp, maxLines = 1)
            Text("$playlistCount " + tr("playlists", "قوائم"), color = NR.Sub, fontSize = 12.sp)
            Text(if (active) tr("● Active", "● نشط") else if (selected) tr("Selected", "محدد") else tr("Select", "اختيار"),
                color = if (active || selected) NR.Text else NR.Faint, fontSize = 11.sp)
        }
    }
}
