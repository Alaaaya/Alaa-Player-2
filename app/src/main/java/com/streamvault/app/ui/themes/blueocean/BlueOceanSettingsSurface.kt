package com.streamvault.app.ui.themes.blueocean

/** Style contract: Blue Ocean settings are a harbour operations desk, not a conventional settings rail. */

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
internal fun BlueOceanSettingsSurface(
    navigation: @Composable () -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val surfaces = LocalThemePresentation.current.surfaces
    Row(
        modifier = modifier
            .fillMaxSize()
            .background(surfaces.canvas)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(258.dp)
                .clip(RoundedCornerShape(topStart = 28.dp, bottomEnd = 28.dp, topEnd = 12.dp, bottomStart = 12.dp))
                .background(surfaces.browseContent)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("HARBOUR OPERATIONS", style = MaterialTheme.typography.labelMedium, color = surfaces.accent)
            Text("SETTINGS DESK", style = MaterialTheme.typography.headlineSmall, color = surfaces.textPrimary)
            Text("Manage providers, playback, profiles and your archive from one dock.", style = MaterialTheme.typography.bodySmall, color = surfaces.textSecondary)
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.TopCenter) {
                navigation()
            }
        }
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .clip(RoundedCornerShape(topStart = 12.dp, bottomEnd = 28.dp, topEnd = 28.dp, bottomStart = 12.dp))
                .background(surfaces.browseContent)
                .padding(8.dp)
        ) {
            content()
        }
    }
}
