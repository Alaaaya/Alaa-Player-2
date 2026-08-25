package com.streamvault.app.devicecontrol

import android.content.Context
import android.provider.Settings
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.streamvault.app.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

sealed class DeviceControlResult<out T> {
    data class Success<T>(val value: T) : DeviceControlResult<T>()
    data class Failure(val message: String) : DeviceControlResult<Nothing>()
}

data class DevicePairingRequest(
    val code: String,
    val expiresAt: String,
    val existingDevice: Boolean,
)

data class RemoteProviderConfiguration(
    val id: Int,
    val kind: String,
    val displayName: String,
    val endpointUrl: String,
    val username: String,
    val password: String,
    val extra: Map<String, String>,
)

data class DeviceControlConfiguration(
    val tvId: String,
    val displayName: String,
    val providers: List<RemoteProviderConfiguration>,
)

/**
 * The device-only transport for the control center. Access and refresh tokens are encrypted at
 * rest, while the private P-256 key remains in Android Keystore and is never exported.
 */
@Singleton
class DeviceControlManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient,
) {
    private val keyAlias = "alaa_device_control_p256"
    private val mediaType = "application/json; charset=utf-8".toMediaType()
    private val preferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "alaa_device_control",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    fun isConfigured(): Boolean = serviceUrl() != null

    fun currentTvId(): String? = preferences.getString(KEY_TV_ID, null)

    fun localProviderIdFor(remoteProviderId: Int): Long? =
        preferences.getLong(providerMappingKey(remoteProviderId), -1L).takeIf { it >= 0L }

    fun saveLocalProviderMapping(remoteProviderId: Int, localProviderId: Long) {
        preferences.edit().putLong(providerMappingKey(remoteProviderId), localProviderId).apply()
    }

    suspend fun requestPairing(): DeviceControlResult<DevicePairingRequest> = withContext(Dispatchers.IO) {
        val baseUrl = serviceUrl() ?: return@withContext DeviceControlResult.Failure(
            "Control center URL is not configured in this build."
        )
        runCatching {
            val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                ?.takeIf(String::isNotBlank)
                ?: error("Android system identifier is unavailable")
            val body = JsonObject().apply {
                addProperty("androidId", androidId)
                addProperty("publicKeyPem", publicKeyPem())
                addProperty("appVersion", BuildConfig.VERSION_NAME)
                addProperty("androidVersion", android.os.Build.VERSION.RELEASE ?: "unknown")
                addProperty("modelName", "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}".trim())
            }
            val response = executeJson(baseUrl, "api/device/v1/pairing/requests", "POST", body)
            if (response.code !in 200..299) error("Pairing request was rejected")
            val payload = response.body
            val request = DevicePairingRequest(
                code = payload.string("pairingCode"),
                expiresAt = payload.string("expiresAt"),
                existingDevice = payload.bool("existingDevice"),
            )
            preferences.edit().putString(KEY_PAIRING_CODE, request.code).apply()
            DeviceControlResult.Success(request)
        }.getOrElse { DeviceControlResult.Failure(it.safeMessage()) }
    }

    suspend fun checkAndActivatePairing(): DeviceControlResult<String> = withContext(Dispatchers.IO) {
        val baseUrl = serviceUrl() ?: return@withContext DeviceControlResult.Failure("Control center URL is not configured in this build.")
        val code = preferences.getString(KEY_PAIRING_CODE, null)
            ?: return@withContext DeviceControlResult.Failure("No active pairing request exists.")
        runCatching {
            val status = executeJson(baseUrl, "api/device/v1/pairing/$code", "GET", null)
            if (status.code == 404) error("The pairing code has expired. Request a new code.")
            if (status.code !in 200..299) error("Pairing status is unavailable")
            if (!status.body.bool("readyToActivate")) error("Waiting for administrator approval.")

            val activationPayload = "ALAA_ACTIVATE\n$code"
            val activation = JsonObject().apply {
                addProperty("pairingCode", code)
                addProperty("signature", sign(activationPayload))
            }
            val activationResponse = executeJson(baseUrl, "api/device/v1/pairing/activate", "POST", activation)
            if (activationResponse.code !in 200..299) error("Activation was rejected")
            val tvId = activationResponse.body.string("tvId")
            preferences.edit()
                .putString(KEY_TV_ID, tvId)
                .putString(KEY_ACCESS_TOKEN, activationResponse.body.string("accessToken"))
                .putString(KEY_REFRESH_TOKEN, activationResponse.body.string("refreshToken"))
                .remove(KEY_PAIRING_CODE)
                .apply()
            DeviceControlResult.Success(tvId)
        }.getOrElse { DeviceControlResult.Failure(it.safeMessage()) }
    }

    suspend fun claimRecoveryCode(recoveryCode: String): DeviceControlResult<String> = withContext(Dispatchers.IO) {
        val baseUrl = serviceUrl() ?: return@withContext DeviceControlResult.Failure("Control center URL is not configured in this build.")
        val pairingCode = preferences.getString(KEY_PAIRING_CODE, null)
            ?: return@withContext DeviceControlResult.Failure("Request a pairing code before using recovery.")
        runCatching {
            val body = JsonObject().apply {
                addProperty("pairingCode", pairingCode)
                addProperty("recoveryCode", recoveryCode.trim().uppercase())
            }
            val response = executeJson(baseUrl, "api/device/v1/pairing/recover", "POST", body)
            if (response.code !in 200..299) error("Recovery code was rejected or expired.")
            response.body.string("tvId")
        }.fold(
            onSuccess = { DeviceControlResult.Success(it) },
            onFailure = { DeviceControlResult.Failure(it.safeMessage()) },
        )
    }

    suspend fun fetchConfiguration(): DeviceControlResult<DeviceControlConfiguration> = withContext(Dispatchers.IO) {
        val baseUrl = serviceUrl() ?: return@withContext DeviceControlResult.Failure("Control center URL is not configured in this build.")
        runCatching {
            var accessToken = preferences.getString(KEY_ACCESS_TOKEN, null) ?: error("This device is not linked.")
            var response = executeJson(baseUrl, "api/device/v1/configuration", "GET", null, accessToken)
            if (response.code == 401) {
                accessToken = refreshAccessToken(baseUrl)
                response = executeJson(baseUrl, "api/device/v1/configuration", "GET", null, accessToken)
            }
            if (response.code !in 200..299) error("The device is not entitled to receive configuration.")
            val payload = response.body
            val providers = payload.array("providers").map { element ->
                val provider = element.asJsonObject
                val configuration = provider.getAsJsonObject("configuration")
                val extra = configuration.getAsJsonObject("extra")?.entrySet()?.associate { (key, value) ->
                    key to value.asString
                }.orEmpty()
                RemoteProviderConfiguration(
                    id = provider.get("id").asInt,
                    kind = provider.string("providerKind"),
                    displayName = provider.string("displayName"),
                    endpointUrl = configuration.string("endpointUrl"),
                    username = configuration.stringOrEmpty("username"),
                    password = configuration.stringOrEmpty("password"),
                    extra = extra,
                )
            }
            DeviceControlResult.Success(
                DeviceControlConfiguration(
                    tvId = payload.string("tvId"),
                    displayName = payload.string("displayName"),
                    providers = providers,
                )
            )
        }.getOrElse { DeviceControlResult.Failure(it.safeMessage()) }
    }

    private fun refreshAccessToken(baseUrl: String): String {
        val tvId = preferences.getString(KEY_TV_ID, null) ?: error("This device is not linked.")
        val refreshToken = preferences.getString(KEY_REFRESH_TOKEN, null) ?: error("This device session has expired.")
        val timestamp = System.currentTimeMillis()
        val payload = "ALAA_REFRESH\n$tvId\n$refreshToken\n$timestamp"
        val body = JsonObject().apply {
            addProperty("tvId", tvId)
            addProperty("refreshToken", refreshToken)
            addProperty("timestamp", timestamp)
            addProperty("signature", sign(payload))
        }
        val response = executeJson(baseUrl, "api/device/v1/sessions/refresh", "POST", body)
        if (response.code !in 200..299) error("The device session has expired. Re-link this device.")
        return response.body.string("accessToken").also { token ->
            preferences.edit().putString(KEY_ACCESS_TOKEN, token).apply()
        }
    }

    private fun serviceUrl(): String? = BuildConfig.DEVICE_CONTROL_API_BASE_URL
        .trim()
        .trimEnd('/')
        .takeIf { it.startsWith("https://") }
        ?.takeIf { it.toHttpUrlOrNull() != null }

    private fun executeJson(
        baseUrl: String,
        relativePath: String,
        method: String,
        body: JsonObject?,
        bearerToken: String? = null,
    ): HttpJsonResponse {
        val requestBuilder = Request.Builder()
            .url("$baseUrl/$relativePath")
            .header("Accept", "application/json")
        bearerToken?.let { requestBuilder.header("Authorization", "Bearer $it") }
        when (method) {
            "GET" -> requestBuilder.get()
            "POST" -> requestBuilder.post((body?.toString() ?: "{}").toRequestBody(mediaType))
            else -> error("Unsupported request method")
        }
        okHttpClient.newCall(requestBuilder.build()).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            val parsed = responseBody.takeIf(String::isNotBlank)?.let(JsonParser::parseString)?.asJsonObject ?: JsonObject()
            return HttpJsonResponse(response.code, parsed)
        }
    }

    private fun publicKeyPem(): String {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (!keyStore.containsAlias(keyAlias)) {
            KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore").apply {
                initialize(
                    KeyGenParameterSpec.Builder(
                        keyAlias,
                        KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY,
                    )
                        .setDigests(KeyProperties.DIGEST_SHA256)
                        .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
                        .build()
                )
            }.generateKeyPair()
        }
        val encoded = keyStore.getCertificate(keyAlias).publicKey.encoded
        return buildString {
            append("-----BEGIN PUBLIC KEY-----\n")
            append(Base64.encodeToString(encoded, Base64.NO_WRAP).chunked(64).joinToString("\n"))
            append("\n-----END PUBLIC KEY-----")
        }
    }

    private fun sign(payload: String): String {
        publicKeyPem()
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val privateKey = (keyStore.getEntry(keyAlias, null) as KeyStore.PrivateKeyEntry).privateKey as PrivateKey
        val signature = Signature.getInstance("SHA256withECDSA").apply {
            initSign(privateKey)
            update(payload.toByteArray(Charsets.UTF_8))
        }.sign()
        return Base64.encodeToString(signature, Base64.NO_WRAP)
    }

    private data class HttpJsonResponse(val code: Int, val body: JsonObject)

    private fun JsonObject.string(name: String): String = get(name)?.asString?.takeIf(String::isNotBlank)
        ?: error("Required response field '$name' is missing")
    private fun JsonObject.stringOrEmpty(name: String): String = get(name)?.asString.orEmpty()
    private fun JsonObject.bool(name: String): Boolean = get(name)?.asBoolean ?: false
    private fun JsonObject.array(name: String) = getAsJsonArray(name)?.toList().orEmpty()
    private fun Throwable.safeMessage(): String = message?.take(180) ?: "Control service request failed."
    private fun providerMappingKey(remoteProviderId: Int) = "remote_provider_$remoteProviderId"

    private companion object {
        const val KEY_TV_ID = "tv_id"
        const val KEY_PAIRING_CODE = "pairing_code"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
    }
}
