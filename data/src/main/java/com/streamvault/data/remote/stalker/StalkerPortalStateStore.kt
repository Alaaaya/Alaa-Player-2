package com.streamvault.data.remote.stalker

import com.streamvault.data.local.dao.StalkerPortalStateDao
import com.streamvault.data.local.entity.StalkerPortalStateEntity
import com.streamvault.domain.model.ContentType
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Singleton
class StalkerPortalStateStore @Inject constructor(
    private val dao: StalkerPortalStateDao
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getValidated(providerId: Long, now: Long = System.currentTimeMillis()): StalkerPortalStateEntity? =
        dao.get(providerId)?.takeIf { state ->
            state.validatedAt > 0L && now - state.validatedAt <= VALIDATION_TTL_MILLIS
        }

    suspend fun get(providerId: Long): StalkerPortalStateEntity? = dao.get(providerId)

    suspend fun recordAuthentication(
        providerId: Long,
        session: StalkerSession,
        profile: StalkerProviderProfile,
        now: Long = System.currentTimeMillis()
    ) {
        if (providerId <= 0L) return
        val existing = dao.get(providerId)
        dao.upsert(
            (existing ?: StalkerPortalStateEntity(providerId)).copy(
                workingEndpoint = session.loadUrl,
                bootstrapRecipe = profile.bootstrapRecipe.name,
                epgSupported = true.takeIf { profile.moduleNames.any { module ->
                    module.contains("epg", ignoreCase = true) || module.contains("itv", ignoreCase = true)
                } } ?: existing?.epgSupported,
                endpointHealthJson = clearHealthFailures(
                    existing?.endpointHealthJson,
                    endpointKey(session.loadUrl),
                    recipeKey(profile.bootstrapRecipe.name)
                ),
                endpointFailedUntil = 0L,
                validatedAt = now
            )
        )
        StalkerTelemetry.capabilityChanged(providerId, "AUTH_RECIPE", "VALIDATED")
    }

    suspend fun recordBulkLive(
        providerId: Long,
        supported: Boolean,
        categoryFidelity: Boolean? = null,
        now: Long = System.currentTimeMillis()
    ) = update(providerId) { state ->
        state.copy(
            bulkLiveSupported = supported,
            bulkLiveCategoryFidelity = categoryFidelity ?: state.bulkLiveCategoryFidelity,
            validatedAt = now
        )
    }.also {
        StalkerTelemetry.capabilityChanged(providerId, "BULK_LIVE", if (supported) "SUPPORTED" else "UNSUPPORTED")
    }

    suspend fun recordWildcard(
        providerId: Long,
        contentType: ContentType,
        supported: Boolean,
        now: Long = System.currentTimeMillis()
    ) = update(providerId) { state ->
        when (contentType) {
            ContentType.MOVIE -> state.copy(movieWildcardSupported = supported, validatedAt = now)
            ContentType.SERIES -> state.copy(seriesWildcardSupported = supported, validatedAt = now)
            else -> state
        }
    }.also {
        StalkerTelemetry.capabilityChanged(
            providerId,
            "${contentType.name}_WILDCARD",
            if (supported) "SUPPORTED" else "UNSUPPORTED"
        )
    }

    suspend fun recordEpg(
        providerId: Long,
        supported: Boolean,
        now: Long = System.currentTimeMillis()
    ) = update(providerId) { state ->
        state.copy(epgSupported = supported, validatedAt = now)
    }.also {
        StalkerTelemetry.capabilityChanged(providerId, "EPG", if (supported) "SUPPORTED" else "UNSUPPORTED")
    }

    suspend fun recordStressCooldown(
        providerId: Long,
        cooldownUntil: Long,
        now: Long = System.currentTimeMillis()
    ) = update(providerId) { state ->
        state.copy(
            safeMetadataConcurrency = 1,
            stressCooldownUntil = cooldownUntil.coerceAtLeast(now),
            validatedAt = state.validatedAt.takeIf { it > 0L } ?: now
        )
    }.also {
        StalkerTelemetry.capabilityChanged(providerId, "METADATA_CONCURRENCY", "DOWNGRADED")
    }

    suspend fun recordHealthyMetadataProbe(
        providerId: Long,
        now: Long = System.currentTimeMillis()
    ) {
        val state = dao.get(providerId) ?: return
        if (state.safeMetadataConcurrency == 1 && state.stressCooldownUntil in 1..now) {
            dao.upsert(state.copy(safeMetadataConcurrency = 2, stressCooldownUntil = 0L))
            StalkerTelemetry.capabilityChanged(providerId, "METADATA_CONCURRENCY", "RESTORED")
        }
    }

    suspend fun markEndpointUnhealthy(
        providerId: Long,
        endpoint: String,
        now: Long = System.currentTimeMillis()
    ) {
        if (providerId <= 0L || endpoint.isBlank()) return
        val until = now + ENDPOINT_COOLDOWN_MILLIS
        update(providerId) { state ->
            val health = decodeEndpointHealth(state.endpointHealthJson)
                .filterValues { expiry -> expiry > now }
                .toMutableMap()
            health[endpointKey(endpoint)] = until
            val bounded = health.entries.sortedByDescending { it.value }.take(MAX_ENDPOINT_HEALTH_ENTRIES)
                .associate { it.key to it.value }
            state.copy(
                endpointHealthJson = json.encodeToString(bounded),
                endpointFailedUntil = bounded.values.maxOrNull() ?: 0L
            )
        }
        StalkerTelemetry.capabilityChanged(providerId, "ENDPOINT", "COOLDOWN")
    }

    fun isEndpointHealthy(
        state: StalkerPortalStateEntity,
        endpoint: String,
        now: Long = System.currentTimeMillis()
    ): Boolean = decodeEndpointHealth(state.endpointHealthJson)[endpointKey(endpoint)]?.let { it <= now } ?: true

    suspend fun markRecipeUnhealthy(
        providerId: Long,
        recipe: String,
        now: Long = System.currentTimeMillis()
    ) {
        if (providerId <= 0L || recipe.isBlank()) return
        val until = now + RECIPE_COOLDOWN_MILLIS
        update(providerId) { state ->
            val health = decodeEndpointHealth(state.endpointHealthJson)
                .filterValues { expiry -> expiry > now }
                .toMutableMap()
            health[recipeKey(recipe)] = until
            state.copy(
                endpointHealthJson = json.encodeToString(
                    health.entries.sortedByDescending { it.value }.take(MAX_ENDPOINT_HEALTH_ENTRIES)
                        .associate { it.key to it.value }
                )
            )
        }
        StalkerTelemetry.capabilityChanged(providerId, "AUTH_RECIPE", "COOLDOWN")
    }

    fun isRecipeHealthy(
        state: StalkerPortalStateEntity,
        recipe: String,
        now: Long = System.currentTimeMillis()
    ): Boolean = decodeEndpointHealth(state.endpointHealthJson)[recipeKey(recipe)]?.let { it <= now } ?: true

    suspend fun invalidateAuthentication(providerId: Long) = update(providerId) { state ->
        state.copy(workingEndpoint = null, bootstrapRecipe = null, validatedAt = 0L)
    }

    suspend fun invalidateCapabilities(providerId: Long) = update(providerId) { state ->
        state.copy(
            bulkLiveSupported = null,
            bulkLiveCategoryFidelity = null,
            movieWildcardSupported = null,
            seriesWildcardSupported = null,
            epgSupported = null,
            validatedAt = 0L
        )
    }

    suspend fun invalidate(providerId: Long) {
        if (providerId > 0L) dao.invalidate(providerId)
    }

    suspend fun restore(providerId: Long, state: StalkerPortalStateEntity?) {
        if (providerId <= 0L) return
        if (state == null) {
            dao.invalidate(providerId)
        } else {
            dao.upsert(state.copy(providerId = providerId))
        }
    }

    private suspend fun update(
        providerId: Long,
        transform: (StalkerPortalStateEntity) -> StalkerPortalStateEntity
    ) {
        if (providerId <= 0L) return
        dao.upsert(transform(dao.get(providerId) ?: StalkerPortalStateEntity(providerId)))
    }

    private fun clearHealthFailures(jsonValue: String?, vararg keys: String): String {
        val health = decodeEndpointHealth(jsonValue).toMutableMap()
        keys.forEach(health::remove)
        return json.encodeToString(health)
    }

    private fun decodeEndpointHealth(value: String?): Map<String, Long> = runCatching {
        json.decodeFromString<Map<String, Long>>(value.orEmpty().ifBlank { "{}" })
    }.getOrDefault(emptyMap())

    private fun endpointKey(endpoint: String): String = healthKey("endpoint", StalkerUrlFactory.normalizePortalUrl(endpoint))

    private fun recipeKey(recipe: String): String = healthKey("recipe", recipe.uppercase())

    private fun healthKey(kind: String, value: String): String = MessageDigest.getInstance("SHA-256")
        .digest("$kind:$value".toByteArray(Charsets.UTF_8))
        .take(12)
        .joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }

    private companion object {
        const val VALIDATION_TTL_MILLIS = 7L * 24L * 60L * 60L * 1000L
        const val ENDPOINT_COOLDOWN_MILLIS = 10L * 60L * 1000L
        const val RECIPE_COOLDOWN_MILLIS = 10L * 60L * 1000L
        const val MAX_ENDPOINT_HEALTH_ENTRIES = 8
    }
}
