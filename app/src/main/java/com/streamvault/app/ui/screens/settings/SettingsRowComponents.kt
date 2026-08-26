package com.streamvault.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.app.R
import com.streamvault.app.ui.components.dialogs.PremiumDialog
import com.streamvault.app.ui.components.dialogs.PremiumDialogActionButton
import com.streamvault.app.ui.components.dialogs.PremiumDialogFooterButton
import com.streamvault.app.ui.interaction.TvButton
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.app.ui.interaction.mouseClickable
import com.streamvault.app.ui.theme.LocalAppHomeTheme
import com.streamvault.app.ui.theme.OnBackground
import com.streamvault.app.ui.theme.OnSurface
import com.streamvault.app.ui.theme.OnSurfaceDim
import com.streamvault.app.ui.theme.Primary
import com.streamvault.app.ui.themes.cinematic.CinematicGold
import com.streamvault.app.ui.themes.cinematic.CinematicMuted
import com.streamvault.app.ui.themes.cinematic.CinematicPanel
import com.streamvault.app.ui.themes.cinematic.CinematicPanelRaised
import com.streamvault.app.ui.themes.cinematic.CinematicText
import com.streamvault.app.ui.themes.neon.NeonCyan
import com.streamvault.app.ui.themes.neon.NeonMuted
import com.streamvault.app.ui.themes.neon.NeonPanel
import com.streamvault.app.ui.themes.neon.NeonPanelRaised
import com.streamvault.app.ui.themes.neon.NeonText
import com.streamvault.domain.model.AppHomeTheme

@Composable
private fun isCinematicSettingsPresentation(): Boolean =
    LocalAppHomeTheme.current == AppHomeTheme.CINEMATIC

@Composable
private fun isNeonSettingsPresentation(): Boolean =
    LocalAppHomeTheme.current == AppHomeTheme.NEON_FUTURE

@Composable
internal fun SettingsSectionHeader(
    title: String,
    subtitle: String
) {
    val isCinematic = isCinematicSettingsPresentation()
    val isNeon = isNeonSettingsPresentation()
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = when { isCinematic -> CinematicGold; isNeon -> NeonCyan; else -> Primary },
            fontWeight = if (isCinematic || isNeon) FontWeight.Black else FontWeight.Normal
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = when { isCinematic -> CinematicMuted; isNeon -> NeonMuted; else -> OnSurfaceDim }
        )
    }
}

@Composable
internal fun SettingsRow(label: String, value: String) {
    val focusRequester = remember { FocusRequester() }
    val isCinematic = isCinematicSettingsPresentation()
    val isNeon = isNeonSettingsPresentation()
    TvClickableSurface(
        onClick = {},
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(if (isCinematic) 16.dp else if (isNeon) 6.dp else 8.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isCinematic) CinematicPanel else if (isNeon) NeonPanel else Color.Transparent,
            focusedContainerColor = if (isCinematic) CinematicPanelRaised else if (isNeon) NeonPanelRaised else Primary.copy(alpha = 0.15f)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = if (isCinematic || isNeon) 1.01f else 1f),
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .mouseClickable(
                focusRequester = focusRequester,
                onClick = {}
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isCinematic || isNeon) 16.dp else 8.dp, vertical = if (isCinematic || isNeon) 12.dp else 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = if (isCinematic) CinematicText else if (isNeon) NeonText else OnSurface)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, color = if (isCinematic) CinematicGold else if (isNeon) NeonCyan else OnBackground)
        }
    }
}

@Composable
internal fun ClickableSettingsRow(
    label: String,
    value: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    indent: Dp = 0.dp
) {
    val focusRequester = remember { FocusRequester() }
    val isCinematic = isCinematicSettingsPresentation()
    val isNeon = isNeonSettingsPresentation()
    TvClickableSurface(
        onClick = { if (enabled) onClick() },
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(if (isCinematic) 16.dp else if (isNeon) 6.dp else 8.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isCinematic) CinematicPanel else if (isNeon) NeonPanel else Color.Transparent,
            focusedContainerColor = if (isCinematic && enabled) CinematicPanelRaised else if (isNeon && enabled) NeonPanelRaised else if (enabled) Primary.copy(alpha = 0.15f) else Color.Transparent
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = if (isCinematic || isNeon) 1.01f else 1f),
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .mouseClickable(
                focusRequester = focusRequester,
                onClick = { if (enabled) onClick() }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = (if (isCinematic) 16.dp else 8.dp) + indent,
                    end = if (isCinematic) 16.dp else 8.dp,
                    top = if (isCinematic) 14.dp else 12.dp,
                    bottom = if (isCinematic) 14.dp else 12.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) if (isCinematic) CinematicText else OnSurface else OnSurfaceDim
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) if (isCinematic) CinematicGold else Primary else OnSurfaceDim
            )
        }
    }
}

@Composable
internal fun SwitchSettingsRow(
    label: String,
    value: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    indent: Dp = 0.dp
) {
    val focusRequester = remember { FocusRequester() }
    val isCinematic = isCinematicSettingsPresentation()
    TvClickableSurface(
        onClick = { if (enabled) onCheckedChange(!checked) },
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(if (isCinematic) 16.dp else 8.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isCinematic) CinematicPanel else Color.Transparent,
            focusedContainerColor = if (isCinematic && enabled) CinematicPanelRaised else if (enabled) Primary.copy(alpha = 0.15f) else Color.Transparent
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = if (isCinematic) 1.01f else 1f),
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .mouseClickable(
                focusRequester = focusRequester,
                onClick = { if (enabled) onCheckedChange(!checked) }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = (if (isCinematic) 16.dp else 8.dp) + indent,
                    end = if (isCinematic) 16.dp else 8.dp,
                    top = if (isCinematic) 14.dp else 12.dp,
                    bottom = if (isCinematic) 14.dp else 12.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (enabled) if (isCinematic) CinematicText else OnSurface else OnSurfaceDim
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCinematic) CinematicMuted else OnSurfaceDim
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = { if (enabled) onCheckedChange(it) },
                enabled = enabled
            )
        }
    }
}

@Composable
internal fun LiveTvQuickFiltersDialog(
    filters: List<String>,
    onDismiss: () -> Unit,
    onAddFilter: (String) -> Unit,
    onRemoveFilter: (String) -> Unit
) {
    var pendingFilter by rememberSaveable { mutableStateOf("") }

    PremiumDialog(
        title = stringResource(R.string.settings_live_tv_quick_filters_dialog_title),
        subtitle = stringResource(R.string.settings_live_tv_quick_filters_dialog_subtitle),
        onDismissRequest = onDismiss,
        widthFraction = 0.5f,
        content = {
            EpgSourceTextField(
                value = pendingFilter,
                onValueChange = { pendingFilter = it },
                placeholder = stringResource(R.string.settings_live_tv_quick_filters_placeholder)
            )
            PremiumDialogActionButton(
                label = stringResource(R.string.settings_live_tv_quick_filters_add),
                enabled = pendingFilter.isNotBlank(),
                onClick = {
                    onAddFilter(pendingFilter)
                    pendingFilter = ""
                }
            )
            if (filters.isEmpty()) {
                Text(
                    text = stringResource(R.string.settings_live_tv_quick_filters_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceDim
                )
            } else {
                Text(
                    text = stringResource(R.string.settings_live_tv_quick_filters_saved),
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceDim
                )
                LazyColumn(
                    modifier = Modifier.heightIn(max = 260.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filters, key = { it }) { filter ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = filter,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = OnSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                TvButton(onClick = { onRemoveFilter(filter) }) {
                                    Text(stringResource(R.string.settings_live_tv_quick_filters_remove))
                                }
                            }
                        }
                    }
                }
            }
        },
        footer = {
            PremiumDialogFooterButton(
                label = stringResource(R.string.settings_cancel),
                onClick = onDismiss
            )
        }
    )
}

internal fun formatLiveTvQuickFiltersValue(filters: List<String>, context: android.content.Context): String {
    if (filters.isEmpty()) {
        return context.getString(R.string.settings_live_tv_quick_filters_none)
    }
    return context.resources.getQuantityString(
        R.plurals.settings_live_tv_quick_filters_count,
        filters.size,
        filters.size
    )
}

@Composable
internal fun SettingsNavItem(
    label: String,
    badgeChar: String,
    accentColor: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    TvClickableSurface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(0.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected) Primary.copy(alpha = 0.11f) else Color.Transparent,
            focusedContainerColor = Primary.copy(alpha = 0.22f)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .mouseClickable(focusRequester = focusRequester, onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                Modifier
                    .width(3.dp)
                    .height(22.dp)
                    .background(
                        color = if (isSelected) Primary else Color.Transparent,
                        shape = RoundedCornerShape(2.dp)
                    )
            )
            Box(
                Modifier
                    .size(28.dp)
                    .background(accentColor.copy(alpha = 0.18f), RoundedCornerShape(7.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeChar,
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected) Primary else OnBackground,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}
