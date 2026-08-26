package com.streamvault.app.ui.themes.redcinema

/** Style contract: Red Cinema settings are a box-office ledger: numbered counter on the left and a production sheet on the right. */

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.app.ui.theme.LocalThemePresentation

@Composable
internal fun RedCinemaSettingsSurface(
    navigation: @Composable () -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val s = LocalThemePresentation.current.surfaces
    Row(
        modifier = modifier.fillMaxSize().background(s.canvas).padding(28.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().width(244.dp).background(s.browseContent, RoundedCornerShape(2.dp)).padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text("BOX OFFICE", style = MaterialTheme.typography.labelMedium, color = s.accent)
            Text("SETTINGS LEDGER", style = MaterialTheme.typography.headlineSmall, color = s.textPrimary)
            Text("Set the house rules, then return to the programme.", style = MaterialTheme.typography.bodySmall, color = s.textSecondary)
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.TopStart) { navigation() }
            Text("SELECT: OPEN\nBACK: CURTAIN", style = MaterialTheme.typography.labelSmall, color = s.textSecondary)
        }
        Column(
            modifier = Modifier.fillMaxHeight().weight(1f).clip(RoundedCornerShape(2.dp)).background(s.browseContent).padding(10.dp)
        ) {
            Text("PRODUCTION FILE", modifier = Modifier.padding(start = 12.dp, top = 4.dp), style = MaterialTheme.typography.labelMedium, color = s.accent)
            content()
        }
    }
}
