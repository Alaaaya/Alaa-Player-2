package com.streamvault.app.panel

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
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
import java.net.NetworkInterface
import java.security.MessageDigest
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
    okHttpClient: OkHttpClient,
    private val validateAndAddProvider: ValidateAndAddProvider,
    private val providerRepository: ProviderRepository,
) {
    private val http = okHttpClient.newBuilder()
        .connectTimeout(10, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).callTimeout(25, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val mutex = Mutex()
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

    @Volatile private var lastAttemptMs = 0L
    @Volatile private var identityLoaded = false

    /** Cheap, safe to call from the UI thread's coroutine; loads identity + cached status off-main. */
    suspend fun ensureIdentity() = withContext(Dispatchers.IO) {
        if (identityLoaded) return@withContext
        val code = plainPrefs.getString(KEY_CODE, null)?.takeIf(PanelDeviceCode::isValid)
            ?: PanelDeviceCode.generate().also { plainPrefs.edit().putString(KEY_CODE, it).apply() }
        val mac = plainPrefs.getString(KEY_MAC, null)
            ?: resolveMac().also { plainPrefs.edit().putString(KEY_MAC, it).apply() }
        val model = listOf(Build.MANUFACTURER, Build.MODEL).filter { !it.isNullOrBlank() }.joinToString(" ").take(120)
        val cached = runCatching { securePrefs.getString(KEY_LAST_CONFIG, null)?.let(PanelConfigParser::parse) }.getOrNull()
        _state.update {
            it.copy(
                deviceCode = code, mac = mac, model = model,
                status = cached?.status, expiresAt = cached?.expiresAt, playlistCount = cached?.playlists?.size ?: 0,
                lastSyncedAtMs = plainPrefs.getLong(KEY_LAST_SYNC, 0L),
            )
        }
        identityLoaded = true
    }

    /**
     * Register (idempotent) + fetch config + apply to providers. Never throws. Offline: keeps the
     * last known status and leaves all providers untouched.
     */
    suspend fun sync(force: Boolean = false) {
        if (!_state.value.configured) return
        ensureIdentity()
        val now = System.currentTimeMillis()
        if (!force && now - lastAttemptMs < MIN_INTERVAL_MS) return
        if (!mutex.tryLock()) return
        try {
            lastAttemptMs = now
            _state.update { it.copy(syncing = true) }
            withContext(Dispatchers.IO) { runSync() }
        } catch (t: Throwable) {
            Log.w(TAG, "panel sync failed: ${t.javaClass.simpleName}")
            _state.update { it.copy(offline = t is IOException, failures = listOf(t.javaClass.simpleName)) }
        } finally {
            _state.update { it.copy(syncing = false) }
            mutex.unlock()
        }
    }

    private suspend fun runSync() {
        val s = _state.value
        val registerBody = JsonObject().apply {
            addProperty("p_device_code", s.deviceCode); addProperty("p_mac", s.mac); addProperty("p_model", s.model)
        }
        rpc("register_device", registerBody) // idempotent upsert, also refreshes model/last_seen
        val codeBody = JsonObject().apply { addProperty("p_device_code", s.deviceCode) }
        var config = PanelConfigParser.parse(rpc("get_device_config", codeBody))
        if (config.status == PanelStatus.UNKNOWN) {
            // Row was deleted on the panel (or first registration was lost): register again.
            rpc("register_device", registerBody)
            config = PanelConfigParser.parse(rpc("get_device_config", codeBody))
        }
        val failures = apply(config)
        securePrefs.edit().putString(KEY_LAST_CONFIG, cacheJson(config)).apply()
        val at = System.currentTimeMillis()
        plainPrefs.edit().putLong(KEY_LAST_SYNC, at).apply()
        _state.update {
            it.copy(
                status = config.status, expiresAt = config.expiresAt, playlistCount = config.playlists.size,
                offline = false, lastSyncedAtMs = at, failures = failures,
            )
        }
    }

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
        val result = runCatching {
            when (p.type) {
                PanelPlaylistType.XTREAM -> validateAndAddProvider.loginXtream(
                    XtreamProviderSetupCommand(
                        serverUrl = p.server.orEmpty(), username = p.username.orEmpty(), password = p.password.orEmpty(),
                        name = p.name, existingProviderId = existingId,
                    )
                )
                PanelPlaylistType.M3U -> validateAndAddProvider.addM3u(
                    M3uProviderSetupCommand(url = p.url.orEmpty(), name = p.name, existingProviderId = existingId)
                )
            }
        }.getOrElse { failures += "${p.name}: ${it.javaClass.simpleName}"; return }
        val provider = when (result) {
            is ValidateAndAddProviderResult.Success -> result.provider
            is ValidateAndAddProviderResult.SavedWithWarning -> result.provider.also { failures += "${p.name}: ${result.warning}" }
            is ValidateAndAddProviderResult.ValidationError -> { failures += "${p.name}: ${result.message}"; return }
            is ValidateAndAddProviderResult.Error -> { failures += "${p.name}: ${result.message}"; return }
            is ValidateAndAddProviderResult.VerificationInconclusive -> { failures += "${p.name}: ${result.message}"; return }
            is ValidateAndAddProviderResult.TransportConsentRequired -> { failures += "${p.name}: transport confirmation required"; return }
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
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw IOException("panel HTTP ${response.code}")
            if (text.length > MAX_RESPONSE_CHARS) throw IOException("panel response too large")
            return text
        }
    }

    private fun loadMappings(): List<PanelMapping> = runCatching {
        securePrefs.getString(KEY_MAPPINGS, null)?.let {
            gson.fromJson<List<PanelMapping>>(it, object : TypeToken<List<PanelMapping>>() {}.type)
        }
    }.getOrNull().orEmpty()

    private fun saveMappings(list: List<PanelMapping>) { securePrefs.edit().putString(KEY_MAPPINGS, gson.toJson(list)).apply() }

    private fun cacheJson(c: PanelConfig): String = JsonObject().apply {
        addProperty("status", c.status.name.lowercase()); addProperty("device_code", c.deviceCode); addProperty("expires_at", c.expiresAt)
        add("playlists", com.google.gson.JsonArray().also { arr ->
            c.playlists.forEach { p ->
                arr.add(JsonObject().apply {
                    addProperty("id", p.id); addProperty("name", p.name); addProperty("type", p.type.name.lowercase())
                    addProperty("url", p.url); addProperty("epg_url", p.epgUrl); addProperty("server", p.server)
                    addProperty("username", p.username); addProperty("password", p.password)
                })
            }
        })
    }.toString()

    /**
     * Real MAC when the OS exposes it (Ethernet on many TV boxes, pre-Android 11); otherwise a stable
     * locally-administered pseudo-MAC derived from ANDROID_ID, which is what the panel shows.
     */
    @SuppressLint("HardwareIds") // deliberate: the panel identifies the box to its owner by MAC-like id
    private fun resolveMac(): String {
        runCatching {
            NetworkInterface.getNetworkInterfaces()?.toList().orEmpty()
                .sortedBy { if (it.name.startsWith("eth")) 0 else if (it.name.startsWith("wlan")) 1 else 2 }
                .firstNotNullOfOrNull { nic ->
                    nic.hardwareAddress?.takeIf { it.size == 6 && !it.all { b -> b == 0.toByte() } }
                        ?.joinToString(":") { "%02X".format(it) }
                        ?.takeIf { it != "02:00:00:00:00:00" }
                }
        }.getOrNull()?.let { return it }
        val seed = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: PanelDeviceCode.generate()
        val h = MessageDigest.getInstance("SHA-256").digest(seed.toByteArray())
        h[0] = ((h[0].toInt() or 0x02) and 0xFE).toByte() // locally administered, unicast
        return h.take(6).joinToString(":") { "%02X".format(it) }
    }

    companion object {
        private const val TAG = "AlaaPanel"
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
