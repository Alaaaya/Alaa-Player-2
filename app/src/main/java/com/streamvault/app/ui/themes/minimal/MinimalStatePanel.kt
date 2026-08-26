package com.streamvault.app.ui.themes.minimal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

/** رسالة حالة Minimal مشتركة: ورقة تحريرية مستقيمة، لا بطاقة عامة معاد تلوينها. */
@Composable
internal fun MinimalStatePanel(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MinimalPaper)
            .border(BorderStroke(1.dp, MinimalRule))
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("STATE / $title", style = MaterialTheme.typography.labelLarge, color = MinimalText)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MinimalMuted)
    }
}
