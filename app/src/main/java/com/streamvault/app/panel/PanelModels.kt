package com.streamvault.app.panel

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.security.MessageDigest
import java.security.SecureRandom

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
)

class PanelParseException(message: String) : Exception(message)

object PanelConfigParser {
    fun parse(json: String): PanelConfig {
        val root = runCatching { JsonParser.parseString(json) }.getOrNull()
            ?.takeIf { it.isJsonObject }?.asJsonObject
            ?: throw PanelParseException("Panel response is not a JSON object")
        val status = when (root.str("status")?.lowercase()) {
            "active" -> PanelStatus.ACTIVE
            "inactive" -> PanelStatus.INACTIVE
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
        return PanelConfig(status, root.str("device_code"), root.str("expires_at"), playlists)
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
        )
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
                    !p.isUsable -> null
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
    /** No 0/O, 1/I/L: easy to read off a TV and type into the panel. */
    const val ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"
    const val LENGTH = 8

    fun generate(random: SecureRandom = SecureRandom()): String =
        buildString(LENGTH) { repeat(LENGTH) { append(ALPHABET[random.nextInt(ALPHABET.length)]) } }

    fun isValid(code: String?): Boolean = code != null && code.length == LENGTH && code.all { it in ALPHABET }

    /** "A7K29QXD" -> "A7K2-9QXD" for display only; the server gets the raw code. */
    fun display(code: String): String = if (code.length == LENGTH) code.substring(0, 4) + "-" + code.substring(4) else code
}
