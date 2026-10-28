package com.streamvault.app.panel

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.security.MessageDigest
import java.security.SecureRandom

/** Validated policy, independent of provider setup fingerprint. Null on unsupported/malformed input. */
data class PanelCategorySettings(
    val providerGeneration: String,
    val revision: String,
    val hidden: Map<String, Set<String>>,
) {
    companion object {
        val LEGACY = PanelCategorySettings("legacy-empty", "legacy-empty", mapOf("live" to emptySet(), "vod" to emptySet(), "series" to emptySet()))
    }
}

/** Server-side device state returned by `get_device_config`. */
enum class PanelStatus { UNKNOWN, INACTIVE, EXPIRED, ACTIVE }

enum class PanelPlaylistType { M3U, XTREAM }

data class PanelPlaylist(
    val id: String,
    val name: String,
    val type: PanelPlaylistType,
    val url: String?,
    val epgUrl: String?,
    val server: String?,
    val username: String?,
    val password: String?,
    val categorySettings: PanelCategorySettings? = PanelCategorySettings.LEGACY,
) {
    /** Usable = has everything its type needs to become a provider. */
    val isUsable: Boolean
        get() = when (type) {
            PanelPlaylistType.M3U -> !url.isNullOrBlank()
            PanelPlaylistType.XTREAM -> !server.isNullOrBlank() && !username.isNullOrBlank() && !password.isNullOrBlank()
        }

    /** Hash of every field that matters for the local provider; a change means "update". Never stored in clear. */
    fun fingerprint(): String {
        val raw = listOf(name, type.name, url, epgUrl, server, username, password).joinToString("\u0001") { it.orEmpty() }
        return MessageDigest.getInstance("SHA-256").digest(raw.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }

    override fun toString(): String = "PanelPlaylist(id=$id, name=$name, type=$type)" // keep secrets out of logs
}

data class PanelConfig(
    val status: PanelStatus,
    val deviceCode: String?,
    val expiresAt: String?,
    val playlists: List<PanelPlaylist>,
    val protocolStatus: String = status.name.lowercase(),
)

class PanelParseException(message: String) : Exception(message)

object PanelConfigParser {
    fun parse(json: String, previous: PanelConfig? = null): PanelConfig {
        val root = runCatching { JsonParser.parseString(json) }.getOrNull()
            ?.takeIf { it.isJsonObject }?.asJsonObject
            ?: throw PanelParseException("Panel response is not a JSON object")
        val status = when (root.str("status")?.lowercase()) {
            "active" -> PanelStatus.ACTIVE
            "inactive", "pending", "registration_expired", "unauthorized", "upgrade_required" -> PanelStatus.INACTIVE
            "expired" -> PanelStatus.EXPIRED
            "unknown" -> PanelStatus.UNKNOWN
            else -> throw PanelParseException("Unknown panel status")
        }
        val playlists = if (status == PanelStatus.ACTIVE) {
            root.get("playlists")?.takeIf { it.isJsonArray }?.asJsonArray
                ?.mapNotNull { it.takeIf(JsonElement::isJsonObject)?.asJsonObject?.toPlaylist() }
                ?.distinctBy { it.id }
                .orEmpty()
        } else emptyList()
        val resolved = playlists.map { p ->
            if (p.categorySettings != null) p else {
                val old = previous?.playlists?.firstOrNull { it.id == p.id && it.fingerprint() == p.fingerprint() }
                val rawPlaylist = root.getAsJsonArray("playlists")?.firstOrNull {
                    it.isJsonObject && it.asJsonObject.str("id") == p.id
                }?.asJsonObject
                val incomingGeneration = rawPlaylist?.str("category_provider_generation")
                val sameEpoch = incomingGeneration == null || incomingGeneration == old?.categorySettings?.providerGeneration
                p.copy(categorySettings = old?.categorySettings?.takeIf { sameEpoch })
            }
        }
        return PanelConfig(status, root.str("device_code"), root.str("expires_at"), resolved, root.str("status").orEmpty())
    }

    private fun JsonObject.toPlaylist(): PanelPlaylist? {
        val id = str("id") ?: return null
        val type = when (str("type")?.lowercase()) {
            "m3u" -> PanelPlaylistType.M3U
            "xtream" -> PanelPlaylistType.XTREAM
            else -> return null
        }
        return PanelPlaylist(
            id = id,
            name = str("name") ?: "Playlist",
            type = type,
            url = str("url"),
            epgUrl = str("epg_url"),
            server = str("server"),
            username = str("username"),
            password = str("password"),
            categorySettings = if (type == PanelPlaylistType.M3U) PanelCategorySettings.LEGACY else categorySettings(),
        )
    }

    private fun JsonObject.categorySettings(): PanelCategorySettings? {
        val names = listOf("category_settings_version", "category_provider_generation", "category_config_revision", "hidden_category_ids")
        if (names.none(::has)) return PanelCategorySettings.LEGACY
        return runCatching {
            val version = get("category_settings_version")
            require(version.isJsonPrimitive && version.asJsonPrimitive.isNumber && version.toString() == "1")
            fun text(key: String): String {
                val v = get(key)
                require(v.isJsonPrimitive && v.asJsonPrimitive.isString)
                return v.asString
            }
            val generation = text("category_provider_generation")
            require(generation.matches(Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")))
            val revision = text("category_config_revision")
            require(revision.matches(Regex("hc1_[0-9a-f]{64}")))
            val obj = getAsJsonObject("hidden_category_ids")
            val hidden = listOf("live", "vod", "series").associateWith { media ->
                val ids = obj.get(media)
                require(ids.isJsonArray && ids.asJsonArray.size() <= 10000)
                ids.asJsonArray.map { id ->
                    require(id.isJsonPrimitive && id.asJsonPrimitive.isString)
                    id.asString.also { require(it.matches(Regex("[0-9]{1,64}"))) }
                }.toSet()
            }
            PanelCategorySettings(generation, revision, hidden)
        }.getOrNull()
    }

    /** Persist effective known policy, not a malformed response that would lose it next startup. */
    fun cacheJson(root: JsonObject, config: PanelConfig): String {
        root.getAsJsonArray("playlists")?.forEach { item ->
            if (!item.isJsonObject) return@forEach
            val obj = item.asJsonObject
            val p = config.playlists.firstOrNull { it.id == obj.str("id") } ?: return@forEach
            val policy = p.categorySettings ?: return@forEach
            val names = listOf("category_settings_version", "category_provider_generation", "category_config_revision", "hidden_category_ids")
            if (policy == PanelCategorySettings.LEGACY) names.forEach(obj::remove)
            else {
                obj.addProperty("category_settings_version", 1)
                obj.addProperty("category_provider_generation", policy.providerGeneration)
                obj.addProperty("category_config_revision", policy.revision)
                obj.add("hidden_category_ids", JsonObject().apply {
                    policy.hidden.forEach { (key, values) -> add(key, com.google.gson.JsonArray().apply { values.forEach(::add) }) }
                })
            }
        }
        return root.toString()
    }

    private fun JsonObject.str(name: String): String? =
        get(name)?.takeIf { it.isJsonPrimitive }?.asString?.trim()?.takeIf(String::isNotEmpty)
}

/** Link between a panel playlist and the local provider it created. */
data class PanelMapping(val panelId: String, val localProviderId: Long, val fingerprint: String)

sealed class PanelSyncAction {
    data class Add(val playlist: PanelPlaylist) : PanelSyncAction()
    data class Update(val playlist: PanelPlaylist, val localProviderId: Long) : PanelSyncAction()
    data class Remove(val panelId: String, val localProviderId: Long) : PanelSyncAction()
}

/**
 * Pure diff between what the panel says and what we created before. Only touches providers that
 * appear in [mappings] (panel-managed); manually added providers are never in there.
 *
 * - ACTIVE: add new, update changed, re-add if the user deleted the local copy, remove vanished.
 *   An unusable playlist (missing url/creds) is left as-is if already mapped, skipped if new.
 * - INACTIVE / EXPIRED: remove every panel-managed provider.
 * - UNKNOWN: no changes (the device must register first).
 */
fun planPanelSync(
    config: PanelConfig,
    mappings: List<PanelMapping>,
    existingLocalProviderIds: Set<Long>,
): List<PanelSyncAction> {
    return when (config.status) {
        PanelStatus.UNKNOWN -> emptyList()
        PanelStatus.INACTIVE, PanelStatus.EXPIRED ->
            mappings.map { PanelSyncAction.Remove(it.panelId, it.localProviderId) }
        PanelStatus.ACTIVE -> {
            val byId = mappings.associateBy { it.panelId }
            val remoteIds = config.playlists.map { it.id }.toSet()
            val upserts = config.playlists.mapNotNull { p ->
                val mapped = byId[p.id]
                when {
                    !p.isUsable || p.categorySettings == null -> null
                    mapped == null || mapped.localProviderId !in existingLocalProviderIds -> PanelSyncAction.Add(p)
                    mapped.fingerprint != p.fingerprint() -> PanelSyncAction.Update(p, mapped.localProviderId)
                    else -> null
                }
            }
            val removals = mappings.filter { it.panelId !in remoteIds }
                .map { PanelSyncAction.Remove(it.panelId, it.localProviderId) }
            removals + upserts
        }
    }
}

object PanelDeviceCode {
    const val ALPHABET = "0123456789ABCDEF"
    const val LENGTH = 12
    fun generate(random: SecureRandom = SecureRandom()): String =
        ByteArray(6).also(random::nextBytes).joinToString("") { "%02X".format(it) }
    fun isValid(code: String?): Boolean = code != null && code.length == LENGTH && code.all { it in ALPHABET }
    fun display(code: String): String = if (isValid(code)) code.chunked(4).joinToString("-") else code
}

/** Ownership-scoped authorization diff. A changed/unusable grant cannot keep old playback alive. */
fun panelProviderGrants(config: PanelConfig, mappings: List<PanelMapping>): Pair<Set<Long>, Set<Long>> {
    val remote = if (config.status == PanelStatus.ACTIVE) config.playlists.associateBy { it.id } else emptyMap()
    val authorized = mappings.filter { m ->
        remote[m.panelId]?.let { it.isUsable && it.categorySettings != null && it.fingerprint() == m.fingerprint } == true
    }.map { it.localProviderId }.toSet()
    return (mappings.map { it.localProviderId }.toSet() - authorized) to authorized
}
