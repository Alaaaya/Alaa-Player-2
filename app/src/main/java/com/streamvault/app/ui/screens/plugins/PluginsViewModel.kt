package com.streamvault.app.ui.screens.plugins

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamvault.app.plugins.InstalledStreamVaultPlugin
import com.streamvault.app.plugins.StreamVaultPluginManager
import com.streamvault.domain.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PluginsUiState(
    val plugins: List<InstalledStreamVaultPlugin> = emptyList(),
    val installUrl: String = "",
    val isLoading: Boolean = false,
    val isInstalling: Boolean = false,
    val activePluginId: String? = null,
    val syncProgress: String? = null,
    val userMessage: String? = null
)

@HiltViewModel
class PluginsViewModel @Inject constructor(
    private val pluginManager: StreamVaultPluginManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(PluginsUiState(isLoading = true))
    val uiState: StateFlow<PluginsUiState> = _uiState.asStateFlow()

    init {
        refreshPlugins()
    }

    fun updateInstallUrl(value: String) {
        _uiState.update { it.copy(installUrl = value) }
    }

    fun refreshPlugins() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, syncProgress = null) }
            val result = runCatching { pluginManager.discoverPlugins() }
            _uiState.update {
                it.copy(
                    plugins = result.getOrDefault(emptyList()),
                    isLoading = false,
                    userMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun installFromUrl() {
        val url = _uiState.value.installUrl.trim()
        if (url.isBlank()) {
            _uiState.update { it.copy(userMessage = "Enter a plugin APK URL first") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isInstalling = true, userMessage = "Preparing plugin installer...") }
            val result = pluginManager.installApkFromUrl(url)
            _uiState.update {
                it.copy(
                    isInstalling = false,
                    userMessage = result.messageOr("Plugin installer opened")
                )
            }
            refreshPlugins()
        }
    }

    fun installFromLocalUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isInstalling = true, userMessage = "Preparing selected APK...") }
            val result = pluginManager.installApkFromUri(uri)
            _uiState.update {
                it.copy(
                    isInstalling = false,
                    userMessage = result.messageOr("Plugin installer opened")
                )
            }
            refreshPlugins()
        }
    }

    fun setPluginEnabled(plugin: InstalledStreamVaultPlugin, enabled: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    activePluginId = plugin.manifest.id,
                    syncProgress = if (enabled) "Activating ${plugin.displayName}..." else "Deactivating ${plugin.displayName}...",
                    userMessage = null
                )
            }
            val result = pluginManager.setPluginEnabled(plugin, enabled) { progress ->
                _uiState.update { it.copy(syncProgress = progress) }
            }
            val refreshed = runCatching { pluginManager.discoverPlugins() }.getOrDefault(_uiState.value.plugins)
            _uiState.update {
                it.copy(
                    plugins = refreshed,
                    activePluginId = null,
                    syncProgress = null,
                    userMessage = result.message
                )
            }
        }
    }

    fun openPluginConfiguration(plugin: InstalledStreamVaultPlugin) {
        val result = pluginManager.openPluginConfiguration(plugin)
        _uiState.update { it.copy(userMessage = result.message) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    private fun Result<Unit>.messageOr(successMessage: String): String = when (this) {
        is Result.Error -> message
        Result.Loading -> "Plugin operation is still running"
        is Result.Success -> successMessage
    }
}
