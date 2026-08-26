package com.streamvault.app.ui.themes.redcinema

/** Style contract: Red Cinema provider setup opens as a curtain call, not a generic configuration form. */

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.app.ui.theme.LocalThemePresentation

@Composable
internal fun RedCinemaProviderBanner(isEditing: Boolean, modifier: Modifier = Modifier) {
    val s = LocalThemePresentation.current.surfaces
    Column(
        modifier = modifier.fillMaxWidth().background(s.browseContent, RoundedCornerShape(2.dp)).padding(horizontal = 18.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Text("CURTAIN CALL / CONNECTION", style = MaterialTheme.typography.labelMedium, color = s.accent)
        Text(
            if (isEditing) "Revise the house connection before the next showing." else "Choose a distributor and raise the curtain on your programme.",
            style = MaterialTheme.typography.bodyMedium,
            color = s.textPrimary
        )
    }
}
