package com.streamvault.data.remote.stalker

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class StalkerAdvancedOptions(
    val hwVersion: String = "",
    val apiUserAgent: String = "",
    val playerUserAgent: String = "",
    val xUserAgentLink: String = LINK_ETHERNET,
    val proxyEnabled: Boolean = false,
    val proxyHost: String = "",
    val proxyPort: Int? = null,
    val requestRules: List<StalkerRequestRule> = emptyList()
) {
    val normalizedLink: String
        get() = when {
            xUserAgentLink.equals(LINK_WIFI, ignoreCase = true) -> LINK_WIFI
            else -> LINK_ETHERNET
        }

    val proxy: StalkerHttpProxy?
        get() = if (proxyEnabled && proxyHost.isNotBlank() && proxyPort != null) {
            StalkerHttpProxy(proxyHost.trim(), proxyPort)
        } else {
            null
        }

    companion object {
        const val LINK_ETHERNET = "Ethernet"
        const val LINK_WIFI = "WiFi"
    }
}

@Serializable
data class StalkerRequestRule(
    val action: String = "",
    val blockRequest: Boolean = false,
    val paramOverrides: List<StalkerParamOverride> = emptyList()
)

@Serializable
data class StalkerParamOverride(
    val name: String = "",
    val value: String = ""
)

data class StalkerHttpProxy(
    val host: String,
    val port: Int
)

object StalkerAdvancedOptionsCodec {
    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    fun decode(raw: String): StalkerAdvancedOptions {
        if (raw.isBlank()) return StalkerAdvancedOptions()
        return runCatching { json.decodeFromString<StalkerAdvancedOptions>(raw) }
            .getOrElse { StalkerAdvancedOptions() }
    }

    fun decodeStrict(raw: String): StalkerAdvancedOptions {
        if (raw.isBlank()) return StalkerAdvancedOptions()
        return json.decodeFromString(raw)
    }

    fun encode(options: StalkerAdvancedOptions): String {
        if (options == StalkerAdvancedOptions()) return ""
        return json.encodeToString(options)
    }
}
