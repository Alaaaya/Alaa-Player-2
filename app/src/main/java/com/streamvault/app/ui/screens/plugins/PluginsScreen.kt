package com.streamvault.app.ui.screens.plugins

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.app.navigation.Routes
import com.streamvault.app.plugins.InstalledStreamVaultPlugin
import com.streamvault.app.plugins.StreamVaultPluginContract
import com.streamvault.app.ui.components.shell.AppNavigationChrome
import com.streamvault.app.ui.components.shell.AppScreenScaffold
import com.streamvault.app.ui.components.shell.StatusPill
import com.streamvault.app.ui.design.AppColors
import com.streamvault.app.ui.interaction.TvButton

@Composable
fun PluginsScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PluginsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val apkPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) viewModel.installFromLocalUri(uri)
    }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    AppScreenScaffold(
        currentRoute = currentRoute,
        onNavigate = onNavigate,
        title = "Plugins",
        subtitle = "Install companion APKs, activate capabilities, and sync plugin providers.",
        modifier = modifier,
        navigationChrome = AppNavigationChrome.TopBar,
        compactHeader = true
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PluginInstallPanel(
                installUrl = uiState.installUrl,
                isInstalling = uiState.isInstalling,
                onInstallUrlChange = viewModel::updateInstallUrl,
                onInstallFromUrl = viewModel::installFromUrl,
                onInstallFromFile = {
                    apkPicker.launch(
                        arrayOf(
                            "application/vnd.android.package-archive",
                            "application/octet-stream",
                            "*/*"
                        )
                    )
                },
                onRefresh = viewModel::refreshPlugins
            )

            uiState.syncProgress?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.Brand
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (uiState.plugins.isEmpty() && !uiState.isLoading) {
                    item {
                        Text(
                            text = "No compatible StreamVault plugins are installed.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.TextSecondary
                        )
                    }
                }
                items(uiState.plugins, key = { it.manifest.id }) { plugin ->
                    PluginCard(
                        plugin = plugin,
                        busy = uiState.activePluginId == plugin.manifest.id,
                        onEnabledChange = { enabled -> viewModel.setPluginEnabled(plugin, enabled) },
                        onOpenConfiguration = { viewModel.openPluginConfiguration(plugin) }
                    )
                }
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
        SnackbarHost(hostState = snackbarHostState)
    }
}

@Composable
private fun PluginInstallPanel(
    installUrl: String,
    isInstalling: Boolean,
    onInstallUrlChange: (String) -> Unit,
    onInstallFromUrl: () -> Unit,
    onInstallFromFile: () -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.Surface.copy(alpha = 0.72f), RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = installUrl,
                onValueChange = onInstallUrlChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { androidx.compose.material3.Text("Plugin APK URL") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AppColors.TextPrimary,
                    unfocusedTextColor = AppColors.TextPrimary,
                    focusedBorderColor = AppColors.Brand,
                    unfocusedBorderColor = AppColors.Outline,
                    focusedLabelColor = AppColors.Brand,
                    unfocusedLabelColor = AppColors.TextSecondary,
                    cursorColor = AppColors.Brand
                )
            )
            TvButton(
                enabled = !isInstalling && installUrl.isNotBlank(),
                onClick = onInstallFromUrl
            ) {
                Text("Install URL")
            }
            TvButton(
                enabled = !isInstalling,
                onClick = onInstallFromFile
            ) {
                Text("Install file")
            }
            TvButton(onClick = onRefresh) {
                Text("Refresh")
            }
        }
        Text(
            text = "Manual installs are detected when this screen refreshes. Compatible plugins expose the StreamVault plugin service.",
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.TextTertiary
        )
    }
}

@Composable
private fun PluginCard(
    plugin: InstalledStreamVaultPlugin,
    busy: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onOpenConfiguration: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.Surface.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = plugin.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = AppColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    StatusPill(
                        label = if (plugin.enabled) "Enabled" else "Disabled",
                        containerColor = if (plugin.enabled) AppColors.BrandMuted else AppColors.SurfaceEmphasis,
                        contentColor = if (plugin.enabled) AppColors.Brand else AppColors.TextSecondary
                    )
                }
                Text(
                    text = plugin.manifest.description.ifBlank { plugin.packageName },
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                val version = plugin.manifest.versionName.ifBlank { "unknown" }
                Text(
                    text = "${plugin.packageName} · v$version",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (plugin.statusLabel.isNotBlank() || plugin.lastMessage.isNotBlank()) {
                    Text(
                        text = listOf(plugin.statusLabel, plugin.lastMessage)
                            .filter { it.isNotBlank() }
                            .joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextTertiary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                TvButton(
                    enabled = !busy && plugin.manifest.hasCapability(StreamVaultPluginContract.CAPABILITY_CONFIGURATION_ACTIVITY),
                    onClick = onOpenConfiguration
                ) {
                    Text("Configure")
                }
                Switch(
                    checked = plugin.enabled,
                    enabled = !busy,
                    onCheckedChange = onEnabledChange
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            plugin.manifest.capabilities.forEach { capability ->
                StatusPill(
                    label = capability,
                    containerColor = Color.White.copy(alpha = 0.08f),
                    contentColor = AppColors.TextSecondary
                )
            }
        }
    }
}
