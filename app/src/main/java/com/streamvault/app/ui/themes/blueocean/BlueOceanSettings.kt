package com.streamvault.app.ui.themes.blueocean

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.app.ui.interaction.TvClickableSurface

/**
 * Blue Ocean settings navigation rail — a harbour operations index.
 *
 * This is deliberately independent from Cinematic, Neon, Minimal, and Premium
 * settings rails. It consumes the shared SettingsNavEntry model and callbacks
 * but presents them as a tide-styled operations desk.
 */

private data class BlueOceanSettingsEntry(
    val label: String,
    val icon: String,
    val accent: Color
)

/**
 * Renders the Blue Ocean settings navigation rail.
 * Called from SettingsNavigationRail.kt when the active theme is BLUE_OCEAN.
 */
@Composable
internal fun BlueOceanSettingsNavigationRail(
    entries: List<Pair<String, String>>,
    selectedCategory: Int,
    focusRequester: FocusRequester,
    onCategorySelected: (Int) -> Unit
) {
    val styledEntries = remember(entries) {
        entries.mapIndexed { index, (label, icon) ->
            val accent = when (index % 4) {
                0 -> BlueOceanAccent
                1 -> BlueOceanTide
                2 -> BlueOceanFoam
                else -> Color(0xFF6FE7F5)
            }
            BlueOceanSettingsEntry(label = label, icon = icon, accent = accent)
        }
    }

    LazyColumn(
        modifier = Modifier
            .width(268.dp)
            .fillMaxHeight()
            .background(BlueOceanSurfaceBrush),
        contentPadding = PaddingValues(start = 16.dp, top = 28.dp, end = 16.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item("blue_ocean_settings_rail_heading") {
            Column(
                modifier = Modifier.padding(start = 4.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "HARBOUR OPS",
                    style = MaterialTheme.typography.titleLarge,
                    color = BlueOceanFoam,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "SETTINGS DESK",
                    style = MaterialTheme.typography.labelSmall,
                    color = BlueOceanMuted
                )
            }
        }
        itemsIndexed(styledEntries) { index, entry ->
            val selected = selectedCategory == index
            val shape = RoundedCornerShape(16.dp)
            var isFocused by remember(index) { mutableStateOf(false) }

            TvClickableSurface(
                onClick = { onCategorySelected(index) },
                modifier = (if (selected) Modifier.focusRequester(focusRequester) else Modifier)
                    .fillMaxWidth()
                    .onFocusChanged { isFocused = it.isFocused },
                shape = ClickableSurfaceDefaults.shape(shape),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = if (selected) BlueOceanSelectedAccent else Color.Transparent,
                    focusedContainerColor = BlueOceanPanelRaised,
                    contentColor = if (selected || isFocused) BlueOceanText else BlueOceanMuted,
                    focusedContentColor = BlueOceanText
                ),
                border = ClickableSurfaceDefaults.border(
                    focusedBorder = Border(
                        border = BorderStroke(2.dp, entry.accent),
                        shape = shape
                    )
                ),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(30.dp)
                            .height(30.dp)
                            .background(
                                if (selected) entry.accent.copy(alpha = 0.18f) else BlueOceanPanel,
                                shape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = entry.icon,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected || isFocused) entry.accent else BlueOceanMuted,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Text(
                        text = entry.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected || isFocused) BlueOceanText else BlueOceanMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        item("blue_ocean_settings_rail_tide_line") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp)
                    .height(2.dp)
                    .background(BlueOceanTideBrush)
            )
        }
    }
}
