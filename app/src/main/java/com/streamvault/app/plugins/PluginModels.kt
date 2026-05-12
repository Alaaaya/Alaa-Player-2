package com.streamvault.app.plugins

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StreamVaultPluginManifest(
    val schemaVersion: Int = 1,
    val id: String,
    val name: String,
    val versionName: String = "",
    val versionCode: Long = 0L,
    val description: String = "",
    val capabilities: List<String> = emptyList(),
    val configurationActivityAction: String? = null,
    val providerName: String? = null
) {
    fun hasCapability(capability: String): Boolean = capability in capabilities
}

data class InstalledStreamVaultPlugin(
    val packageName: String,
    val serviceClassName: String,
    val appLabel: String,
    val manifest: StreamVaultPluginManifest,
    val enabled: Boolean,
    val statusLabel: String = "",
    val lastMessage: String = ""
) {
    val displayName: String
        get() = manifest.name.ifBlank { appLabel.ifBlank { packageName } }
}

data class PluginActionResult(
    val success: Boolean,
    val message: String
)
