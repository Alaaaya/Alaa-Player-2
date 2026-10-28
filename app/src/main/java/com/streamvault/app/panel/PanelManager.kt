package com.streamvault.app.panel

import android.content.Context
import android.os.Build
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.streamvault.app.BuildConfig
import com.streamvault.domain.repository.ProviderRepository
import com.streamvault.domain.usecase.M3uProviderSetupCommand
import com.streamvault.domain.usecase.ValidateAndAddProvider
import com.streamvault.domain.usecase.ValidateAndAddProviderResult
import com.streamvault.domain.usecase.XtreamProviderSetupCommand
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class PanelUiState(
    val deviceCode: String = "",
    val mac: String = "",
    val model: String = "",
    /** null until the panel has answered at least once (live or cached). */
    val status: PanelStatus? = null,
    val expiresAt: String? = null,
    val playlistCount: Int = 0,
    val offline: Boolean = false,
    val syncing: Boolean = false,
    val lastSyncedAtMs: Long = 0L,
    val failures: List<String> = emptyList(),
) {
    val configured: Boolean get() = BuildConfig.PANEL_BASE_URL.startsWith("https://") && BuildConfig.PANEL_ANON_KEY.isNotBlank()
    /** The server explicitly said this device may not use panel playlists. */
    val needsActivation: Boolean get() = status == PanelStatus.INACTIVE || status == PanelStatus.EXPIRED
}

@Singleton
class PanelManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val validateAndAddProvider: ValidateAndAddProvider,
    private val providerRepository: ProviderRepository,
    private val playbackGuard: PanelPlaybackGuard,
) {
    // Dedicated client: never inherits provider logging, interceptors, caches, or unsafe TLS.
    private val http = OkHttpClient.Builder().followRedirects(false).followSslRedirects(false)
        .connectTimeout(10, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).callTimeout(25, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val mutex = Mutex()
    private val identityMutex = Mutex()
    private val json = "application/json; charset=utf-8".toMediaType()

    /** Device code / MAC are not secret; playlists cache + mappings are (they hold credentials). */
    private val plainPrefs by lazy { context.getSharedPreferences("alaa_panel_identity", Context.MODE_PRIVATE) }
    private val securePrefs by lazy {
        val key = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
        EncryptedSharedPreferences.create(
            context, "alaa_panel_secure", key,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    private val _state = MutableStateFlow(PanelUiState())
    val state: StateFlow<PanelUiState> = _state.asStateFlow()

    val nextPollIntervalMs: Long get() {
        val normal = if (_state.value.status == PanelStatus.ACTIVE) PERIODIC_INTERVAL_MS else MIN_INTERVAL_MS
        val untilExpiry = _state.value.expiresAt?.let {
            runCatching { java.time.Instant.parse(it).toEpochMilli() - System.currentTimeMillis() }.getOrDefault(0)
        } ?: normal
        return minOf(normal, untilExpiry.coerceAtLeast(1_000))
    }

    @Volatile private var lastAttemptMs = 0L
    @Volatile private var identityLoaded = false

    private val protocol by lazy {
        PanelProtocol(object : PanelBindingStore {
            override fun read(): PanelBinding? = securePrefs.getString(KEY_BINDING, null)?.let {
                gson.fromJson(it, PanelBinding::class.java)
            }
            override fun write(binding: PanelBinding) {
                check(securePrefs.edit().putString(KEY_BINDING, gson.toJson(binding)).commit()) { "Secure identity persistence failed" }
            }
        }, PanelTransport(::rpc), listOf(Build.MANUFACTURER, Build.MODEL).joinToString(" ").take(120))
    }

    /** Identity/cache always loaded off-main, serialized, and committed before first registration. */
    suspend fun ensureIdentity() = withContext(Dispatchers.IO) {
        identityMutex.withLock {
            if (identityLoaded) return@withLock
            try {
                val legacyCode = plainPrefs.getString(KEY_CODE, null)
                val legacyMappings = loadMappings()
                check(!plainPrefs.getBoolean("v2_initialized", false) || securePrefs.contains(KEY_BINDING)) { "Identity lost; admin recovery required" }
                val denied = loadDeniedProviderIds()
                playbackGuard.revoke(denied)
                check(legacyCode != null || legacyMappings.isEmpty() || securePrefs.contains(KEY_BINDING)) { "Identity lost; admin recovery required" }
                val b = protocol.identity(legacyCode, legacyMappings.isNotEmpty())
                check(plainPrefs.edit().putBoolean("v2_initialized", true).commit())
                // Legacy code-only cache is NOT authorization under v2. Remove it before navigation.
                securePrefs.edit().remove(KEY_LAST_CONFIG).commit()
                var cached = protocol.cached()
                if (cached != null && isLocallyExpired(cached)) {
                    cached = PanelConfig(PanelStatus.EXPIRED, b.code, cached.expiresAt, emptyList())
                    protocol.discardExpiredCache()
                }
                if (cached?.status == PanelStatus.ACTIVE) {
                    // Reconcile active cached grants before opening the startup gate.
                    enforce(cached, cached)
                } else {
                    val deny = PanelConfig(PanelStatus.INACTIVE, b.code, null, emptyList())
                    // Clear stale providers even if the first network request is offline.
                    if (legacyMappings.isNotEmpty()) {
                        val ids = legacyMappings.map { it.localProviderId }.toSet()
                        saveDeniedProviderIds(denied + ids)
                        playbackGuard.revoke(ids)
                        val removalFailures = apply(deny)
                        check(removalFailures.isEmpty()) { "Panel provider cleanup failed" }
                    }
                    playbackGuard.ready()
                }
                _state.update { it.copy(deviceCode = b.code, mac = "", model = listOf(Build.MANUFACTURER, Build.MODEL).joinToString(" "),
                    status = cached?.status ?: PanelStatus.INACTIVE, expiresAt = cached?.expiresAt,
                    playlistCount = cached?.playlists?.size ?: 0,
                    failures = if (b.legacy) listOf("Admin-assisted secure re-pair required") else emptyList()) }
                identityLoaded = true
            } catch (e: Exception) {
                // Keep playback gate closed if Keystore/identity/mapping integrity is unavailable.
                playbackGuard.failClosed()
                _state.update { it.copy(status = PanelStatus.INACTIVE, failures = listOf("Secure storage unavailable; admin recovery required")) }
            }
        }
    }

    /** Retry/force never bypasses server backoff. Offline cache is retained until expiry/revocation. */
    suspend fun sync(force: Boolean = false) {
        if (!_state.value.configured) { playbackGuard.ready(); return }
        ensureIdentity()
        if (!identityLoaded) return
        val now = System.currentTimeMillis()
        val interval = if (_state.value.status == PanelStatus.ACTIVE) PERIODIC_INTERVAL_MS else MIN_INTERVAL_MS
        val knownExpired = _state.value.expiresAt?.let {
            runCatching { java.time.Instant.parse(it).toEpochMilli() <= now }.getOrDefault(true)
        } ?: false
        if (!force && !knownExpired && now - lastAttemptMs < interval) return
        if (!mutex.tryLock()) return
        try {
            lastAttemptMs = now
            _state.update { it.copy(syncing = true) }
            withContext(Dispatchers.IO) { runSync() }
        } catch (e: CancellationException) { throw e
        } catch (e: Exception) {
            // Logs contain no device code, token, URL, response, or provider credentials.
            _state.update { it.copy(offline = true, failures = listOf("Panel temporarily unavailable")) }
        } finally {
            _state.update { it.copy(syncing = false) }; mutex.unlock()
        }
    }

    private suspend fun runSync() {
        val cached = protocol.cached()
        if (cached != null && isLocallyExpired(cached)) {
            enforce(PanelConfig(PanelStatus.EXPIRED, _state.value.deviceCode, cached.expiresAt, emptyList()), cached)
            protocol.discardExpiredCache()
        }
        val config = protocol.refresh() ?: return
        enforce(config, cached)
        val b = protocol.identity()
        _state.update { it.copy(deviceCode = b.code) }
    }

    private suspend fun enforce(config: PanelConfig, previous: PanelConfig?) {
        try { enforceKnownConfig(config) }
        catch (e: Exception) {
            playbackGuard.failClosedAndStop()
            throw e
        }
    }

    private suspend fun enforceKnownConfig(config: PanelConfig) {
        // Revoke removed/replaced panel grants BEFORE modifying rows or network setup.
        // Provider IDs survive URL/CDN redirects and distinguish manual accounts on the same host.
        val before = loadMappings()
        val revoked = panelProviderGrants(config, before).first
        playbackGuard.revoke(revoked)
        saveDeniedProviderIds(loadDeniedProviderIds() + revoked)
        val failures = apply(config)
        // Failed/partial setup does NOT restore access to a previous account configuration.
        val authorized = panelProviderGrants(config, loadMappings()).second
        saveDeniedProviderIds(loadDeniedProviderIds() - authorized)
        playbackGuard.authorize(authorized)
        val at = System.currentTimeMillis()
        plainPrefs.edit().putLong(KEY_LAST_SYNC, at).commit()
        _state.update { it.copy(status = config.status, expiresAt = config.expiresAt,
            playlistCount = config.playlists.size, offline = false, lastSyncedAtMs = at,
            failures = if (protocol.identity().legacy && config.protocolStatus == "unauthorized")
                failures + "Admin-assisted secure re-pair required" else failures) }
        if (failures.isNotEmpty() && config.status != PanelStatus.ACTIVE) {
            playbackGuard.failClosed()
            error("Panel provider cleanup incomplete")
        }
        playbackGuard.ready()
    }

    private fun isLocallyExpired(config: PanelConfig): Boolean = config.expiresAt?.let {
        runCatching { java.time.Instant.parse(it).toEpochMilli() <= System.currentTimeMillis() }.getOrDefault(true)
    } ?: false

    private suspend fun apply(config: PanelConfig): List<String> {
        val mappings = loadMappings().toMutableList()
        val localIds = providerRepository.getProviders().first().map { it.id }.toSet()
        val failures = mutableListOf<String>()
        for (action in planPanelSync(config, mappings, localIds)) {
            when (action) {
                is PanelSyncAction.Remove -> {
                    if (action.localProviderId in localIds) {
                        val r = providerRepository.deleteProvider(action.localProviderId)
                        if (r.isError) { failures += "remove ${action.panelId}"; continue }
                    }
                    mappings.removeAll { it.panelId == action.panelId }
                }
                is PanelSyncAction.Add -> upsert(action.playlist, null, mappings, failures)
                is PanelSyncAction.Update -> upsert(action.playlist, action.localProviderId, mappings, failures)
            }
            saveMappings(mappings) // persist after every step so a crash mid-sync can't orphan providers
        }
        return failures
    }

    private suspend fun upsert(p: PanelPlaylist, existingId: Long?, mappings: MutableList<PanelMapping>, failures: MutableList<String>) {
        // Reserve an inert, unique identity first. Real account identity is installed only by the
        // edit path, which checks for collisions BEFORE mutation. No real credentials are
        // installed until the ownership mapping has been committed.
        val targetId = existingId ?: run {
            val reservationUrl = "https://panel-reservation-${java.util.UUID.randomUUID()}.invalid"
            val draft = com.streamvault.domain.model.LegacyProvider(
                name = p.name,
                type = if (p.type == PanelPlaylistType.XTREAM) com.streamvault.domain.model.ProviderType.XTREAM_CODES else com.streamvault.domain.model.ProviderType.M3U,
                serverUrl = reservationUrl,
                m3uUrl = if (p.type == PanelPlaylistType.M3U) reservationUrl else "",
                isActive = false,
            )
            val added = providerRepository.addProvider(draft)
            val id = (added as? com.streamvault.domain.model.Result.Success)?.data
            if (id == null) { failures += "Panel account conflicts with existing provider"; return }
            mappings += PanelMapping(p.id, id, "")
            saveMappings(mappings) // Ownership committed before any subsequent setup/network mutation.
            saveDeniedProviderIds(loadDeniedProviderIds() + id)
            playbackGuard.revoke(setOf(id))
            id
        }
        val result = runCatching {
            when (p.type) {
                PanelPlaylistType.XTREAM -> validateAndAddProvider.loginXtream(
                    XtreamProviderSetupCommand(
                        serverUrl = p.server.orEmpty(), username = p.username.orEmpty(), password = p.password.orEmpty(),
                        name = p.name, existingProviderId = targetId,
                    )
                )
                PanelPlaylistType.M3U -> validateAndAddProvider.addM3u(
                    M3uProviderSetupCommand(url = p.url.orEmpty(), name = p.name, existingProviderId = targetId)
                )
            }
        }.getOrElse { failures += "Provider setup failed"; return }
        val provider = when (result) {
            is ValidateAndAddProviderResult.Success -> result.provider
            is ValidateAndAddProviderResult.SavedWithWarning -> result.provider.also { failures += "Provider saved with warning" }
            is ValidateAndAddProviderResult.ValidationError -> { failures += "Provider validation failed"; return }
            is ValidateAndAddProviderResult.Error -> { failures += "Provider validation failed"; return }
            is ValidateAndAddProviderResult.VerificationInconclusive -> { failures += "Provider validation failed"; return }
            is ValidateAndAddProviderResult.TransportConsentRequired -> { failures += "Provider transport confirmation required"; return }
        }
        mappings.removeAll { it.panelId == p.id }
        mappings += PanelMapping(p.id, provider.id, p.fingerprint())
    }

    private fun rpc(function: String, body: JsonObject): String {
        val request = Request.Builder()
            .url("${BuildConfig.PANEL_BASE_URL.trimEnd('/')}/rest/v1/rpc/$function")
            .header("apikey", BuildConfig.PANEL_ANON_KEY)
            .header("Authorization", "Bearer ${BuildConfig.PANEL_ANON_KEY}")
            .header("Accept", "application/json")
            .post(body.toString().toRequestBody(json))
            .build()
        http.newCall(request).execute().use { response ->
            val source = response.body?.source() ?: throw IOException("Empty panel response")
            if (source.request(MAX_RESPONSE_CHARS.toLong() + 1)) throw IOException("Panel response too large")
            val text = source.readUtf8()
            if (!response.isSuccessful) throw PanelHttpException(response.code, response.header("Retry-After")?.toLongOrNull() ?: 0)
            if (text.length > MAX_RESPONSE_CHARS) throw IOException("panel response too large")
            return text
        }
    }

    private fun loadDeniedProviderIds(): Set<Long> =
        securePrefs.getStringSet("denied_provider_ids", emptySet()).orEmpty().map {
            it.toLongOrNull() ?: error("Invalid panel ownership state")
        }.toSet()

    private fun saveDeniedProviderIds(ids: Set<Long>) {
        check(securePrefs.edit().putStringSet("denied_provider_ids", ids.map(Long::toString).toSet())
            .remove("denied_origins").commit())
    }

    private fun loadMappings(): List<PanelMapping> = runCatching {
        securePrefs.getString(KEY_MAPPINGS, null)?.let {
            gson.fromJson<List<PanelMapping>>(it, object : TypeToken<List<PanelMapping>>() {}.type)
        }
    }.getOrThrow().orEmpty()

    private fun saveMappings(list: List<PanelMapping>) { check(securePrefs.edit().putString(KEY_MAPPINGS, gson.toJson(list)).commit()) }

    companion object {
        private const val KEY_BINDING = "v2_binding"
        const val PERIODIC_INTERVAL_MS = 30 * 60 * 1000L
        private const val MIN_INTERVAL_MS = 60 * 1000L
        private const val MAX_RESPONSE_CHARS = 512 * 1024
        private const val KEY_CODE = "device_code"
        private const val KEY_MAC = "mac"
        private const val KEY_LAST_SYNC = "last_sync_ms"
        private const val KEY_LAST_CONFIG = "last_config"
        private const val KEY_MAPPINGS = "mappings"
    }
}
