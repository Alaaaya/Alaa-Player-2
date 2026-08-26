package com.streamvault.app.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.streamvault.app.R
import com.streamvault.app.ui.theme.OnBackground
import com.streamvault.app.ui.theme.ThemeCatalog
import com.streamvault.app.ui.theme.ThemeCatalogEntry
import com.streamvault.app.ui.theme.ThemePresentationRegistry

internal fun LazyListScope.settingsThemesSection(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel
) {
    item(key = "settings_themes") {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_themes_title),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = stringResource(R.string.settings_themes_subtitle),
                modifier = Modifier.padding(top = 4.dp, bottom = 14.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = OnBackground.copy(alpha = 0.72f)
            )
            val selectedTheme = uiState.appHomeTheme.takeIf(ThemePresentationRegistry::isSelectable)
                ?: ThemePresentationRegistry.selectableThemes().first()
            ThemeCatalog.selectableEntries().forEach { entry ->
                ThemeChoiceRow(
                    entry = entry,
                    selected = selectedTheme == entry.theme,
                    onClick = { viewModel.setAppHomeTheme(entry.theme) }
                )
            }
        }
    }
}

@Composable
private fun ThemeChoiceRow(
    entry: ThemeCatalogEntry,
    selected: Boolean,
    onClick: () -> Unit
) {
    ClickableSettingsRow(
        label = entry.title,
        value = if (selected) stringResource(R.string.settings_theme_selected) else entry.description,
        onClick = onClick
    )
}
