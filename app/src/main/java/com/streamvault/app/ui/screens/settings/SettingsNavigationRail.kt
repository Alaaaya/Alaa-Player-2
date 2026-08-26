package com.streamvault.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Text
import com.streamvault.app.R
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.app.ui.theme.LocalAppHomeTheme
import com.streamvault.app.ui.theme.Primary
import com.streamvault.app.ui.themes.cinematic.CinematicCanvas
import com.streamvault.app.ui.themes.cinematic.CinematicGold
import com.streamvault.app.ui.themes.cinematic.CinematicMuted
import com.streamvault.app.ui.themes.cinematic.CinematicPanel
import com.streamvault.app.ui.themes.cinematic.CinematicPanelRaised
import com.streamvault.app.ui.themes.cinematic.CinematicText
import com.streamvault.app.ui.themes.cinematic.CinematicWine
import com.streamvault.domain.model.AppHomeTheme

private data class SettingsNavEntry(
    val label: String,
    val icon: String,
    val accent: Color
)

@Composable
internal fun SettingsNavigationRail(
    selectedCategory: Int,
    focusRequester: FocusRequester,
    onCategorySelected: (Int) -> Unit
) {
    val entries = listOf(
        SettingsNavEntry(
            label = stringResource(R.string.settings_providers),
            icon = "P",
            accent = Primary
        ),
        SettingsNavEntry(
            label = stringResource(R.string.settings_playback),
            icon = ">",
            accent = Color(0xFF9E8FFF)
        ),
        SettingsNavEntry(
            label = stringResource(R.string.settings_browsing),
            icon = "#",
            accent = Color(0xFF26A69A)
        ),
        SettingsNavEntry(
            label = stringResource(R.string.settings_privacy),
            icon = "L",
            accent = Color(0xFFFFB74D)
        ),
        SettingsNavEntry(
            label = stringResource(R.string.settings_recording_title),
            icon = "R",
            accent = Color(0xFFEF5350)
        ),
        SettingsNavEntry(
            label = stringResource(R.string.settings_backup_restore),
            icon = "B",
            accent = Color(0xFF42A5F5)
        ),
        SettingsNavEntry(
            label = "EPG Sources",
            icon = "E",
            accent = Color(0xFF66BB6A)
        ),
        SettingsNavEntry(
            label = stringResource(R.string.settings_about),
            icon = "i",
            accent = Color(0xFF78909C)
        ),
        SettingsNavEntry(
            label = stringResource(R.string.settings_themes),
            icon = "A",
            accent = Color(0xFFE53935)
        )
    )

    if (LocalAppHomeTheme.current == AppHomeTheme.CINEMATIC) {
        CinematicSettingsNavigationRail(
            entries = entries,
            selectedCategory = selectedCategory,
            focusRequester = focusRequester,
            onCategorySelected = onCategorySelected
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .width(236.dp)
            .fillMaxHeight()
            .background(Color.Black.copy(alpha = 0.25f)),
        contentPadding = PaddingValues(top = 76.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        itemsIndexed(entries) { index, entry ->
            SettingsNavItem(
                label = entry.label,
                badgeChar = entry.icon,
                accentColor = entry.accent,
                isSelected = selectedCategory == index,
                modifier = if (selectedCategory == index) Modifier.focusRequester(focusRequester) else Modifier,
                onClick = { onCategorySelected(index) }
            )
        }
    }
}

@Composable
private fun CinematicSettingsNavigationRail(
    entries: List<SettingsNavEntry>,
    selectedCategory: Int,
    focusRequester: FocusRequester,
    onCategorySelected: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .width(288.dp)
            .fillMaxHeight()
            .background(CinematicPanel),
        contentPadding = PaddingValues(start = 18.dp, top = 26.dp, end = 18.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item("cinematic_settings_rail_heading") {
            androidx.tv.material3.Text(
                text = "CONTROL BOOTH",
                modifier = Modifier.padding(start = 4.dp, bottom = 10.dp),
                style = androidx.tv.material3.MaterialTheme.typography.titleLarge,
                color = CinematicGold,
                fontWeight = FontWeight.Black
            )
        }
        itemsIndexed(entries) { index, entry ->
            val selected = selectedCategory == index
            val shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
            TvClickableSurface(
                onClick = { onCategorySelected(index) },
                modifier = (if (selected) Modifier.focusRequester(focusRequester) else Modifier)
                    .fillMaxWidth(),
                shape = ClickableSurfaceDefaults.shape(shape),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = if (selected) CinematicWine.copy(alpha = .46f) else CinematicCanvas,
                    focusedContainerColor = CinematicPanelRaised,
                    contentColor = CinematicText,
                    focusedContentColor = CinematicText
                ),
                border = ClickableSurfaceDefaults.border(
                    focusedBorder = Border(border = BorderStroke(2.dp, CinematicGold), shape = shape)
                ),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1.015f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .background(if (selected) CinematicGold.copy(alpha = .18f) else CinematicPanelRaised, shape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = entry.icon,
                            modifier = Modifier.padding(vertical = 5.dp),
                            style = androidx.tv.material3.MaterialTheme.typography.labelMedium,
                            color = if (selected) CinematicGold else CinematicMuted,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Text(
                        text = entry.label,
                        style = androidx.tv.material3.MaterialTheme.typography.labelLarge,
                        color = CinematicText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
