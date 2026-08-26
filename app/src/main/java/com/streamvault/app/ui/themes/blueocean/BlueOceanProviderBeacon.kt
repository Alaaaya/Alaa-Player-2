package com.streamvault.app.ui.themes.blueocean

/** Style contract: Blue Ocean provider onboarding begins at a connection harbour, with the setup form below the signal beacon. */

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.app.ui.theme.LocalThemePresentation

@Composable
internal fun BlueOceanProviderBeacon(
    isEditing: Boolean,
    modifier: Modifier = Modifier
) {
    val surfaces = LocalThemePresentation.current.surfaces
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 22.dp, bottomEnd = 22.dp, topEnd = 10.dp, bottomStart = 10.dp))
            .background(surfaces.browseContent)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Text("CONNECTION HARBOUR", style = MaterialTheme.typography.labelMedium, color = surfaces.accent)
        Text(
            if (isEditing) "Update the route without losing the signal." else "Choose a source, then bring the signal ashore.",
            style = MaterialTheme.typography.bodyMedium,
            color = surfaces.textPrimary
        )
    }
}
