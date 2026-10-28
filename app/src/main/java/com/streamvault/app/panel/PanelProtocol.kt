package com.streamvault.app.panel

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.IOException
import java.security.SecureRandom

/** One encrypted record. commit() must finish before any request containing its identity. */
data class PanelBinding(
    val code: String, val token: String, val legacy: Boolean = false,
    val registered: Boolean = false, val sawPending: Boolean = false,
    val everActivated: Boolean = false, val uncertain: Boolean = false,
    val retryAtMs: Long = 0, val failures: Int = 0, val cached: String? = null,
) {
    override fun toString() = "PanelBinding(redacted)"
}
interface PanelBindingStore {
    fun read(): PanelBinding?
    fun write(binding: PanelBinding)
}
fun interface PanelTransport { fun call(function: String, body: JsonObject): String }
class PanelHttpException(val code: Int, val retrySeconds: Long = 0) : IOException("Panel HTTP failure")
class PanelRetryException(val seconds: Long) : IOException("Panel retry deferred")

/** Synchronous protocol; caller serializes and runs off-main. No code-only fallback or takeover. */
class PanelProtocol(
    private val store: PanelBindingStore,
    private val transport: PanelTransport,
    private val model: String,
    private val clock: () -> Long = System::currentTimeMillis,
    private val random: SecureRandom = SecureRandom(),
) {
    fun identity(legacyCode: String? = null, legacyHadProviders: Boolean = false): PanelBinding {
        store.read()?.let {
            require(it.code.matches(Regex("[A-Z0-9]{1,64}")) && it.token.matches(Regex("[a-f0-9]{64}"))) { "Invalid secure identity; admin re-pair required" }
            return it
        }
        val legacy = !legacyCode.isNullOrBlank()
        val binding = PanelBinding(
            code = if (legacy) legacyCode!! else PanelDeviceCode.generate(random),
            token = ByteArray(32).also(random::nextBytes).joinToString("") { "%02x".format(it) },
            legacy = legacy, everActivated = legacyHadProviders, uncertain = legacy,
        )
        store.write(binding)
        return binding
    }
    fun cached(): PanelConfig? = store.read()?.cached?.let(PanelConfigParser::parse)
    fun retryAtMs(): Long = store.read()?.retryAtMs ?: 0
    fun discardExpiredCache() {
        val b = store.read() ?: return
        store.write(b.copy(cached = null, uncertain = true))
    }

    fun refresh(): PanelConfig? {
        var b = identity()
        if (clock() < b.retryAtMs) return null // force refresh must not bypass server or network backoff
        try {
            if (!b.legacy && !b.registered) {
                val r = request("register_device_v2", b, register = true)
                when (r.get("status")?.asString) {
                    "registered" -> { b = b.copy(registered = true); store.write(b) }
                    "rate_limited" -> throw PanelRetryException(retrySeconds(r, 3600))
                    "unavailable", "invalid" -> {
                        // Never rotate on collision, lost binding, or ambiguous prior registration.
                        b = b.copy(uncertain = true, cached = null, retryAtMs = clock() + 60_000)
                        store.write(b)
                        return denied("unauthorized")
                    }
                    else -> throw PanelParseException("Unexpected registration status")
                }
            }
            val root = request("get_device_config_v2", b)
            if (root.get("status")?.asString == "rate_limited") throw PanelRetryException(retrySeconds(root, 60))
            if (root.get("status")?.asString == "active") {
                // Even invalid active payloads prove we cannot renew as a never-activated device.
                b = b.copy(everActivated = true, uncertain = true)
                store.write(b)
            }
            var config = PanelConfigParser.parse(root.toString())
            if (config.status == PanelStatus.ACTIVE &&
                (config.deviceCode != b.code || root.get("app_type")?.asString != "alaa_player")) {
                config = denied("unauthorized")
            }
            if (config.status == PanelStatus.ACTIVE && config.expiresAt != null &&
                runCatching { java.time.Instant.parse(config.expiresAt).toEpochMilli() <= clock() }.getOrDefault(true)) {
                config = denied("expired")
            }
            // Persist evidence BEFORE using provider credentials. INACTIVE is NOT proof of pending.
            b = when (config.protocolStatus) {
                "active" -> b.copy(everActivated = true, uncertain = false)
                "pending" -> b.copy(sawPending = true)
                "inactive", "expired", "unauthorized" -> b.copy(uncertain = true)
                "registration_expired" -> {
                    if (canRenewPending(b)) {
                        // Server guarantees this status only for an unactivated, playlist-free row.
                        val fresh = PanelBinding(PanelDeviceCode.generate(random),
                            ByteArray(32).also(random::nextBytes).joinToString("") { "%02x".format(it) })
                        store.write(fresh.copy(retryAtMs = clock() + 60_000))
                        return config
                    }
                    b.copy(uncertain = true)
                }
                else -> b.copy(uncertain = true)
            }
            store.write(b.copy(cached = if (config.status == PanelStatus.ACTIVE) root.toString() else null,
                retryAtMs = clock() + 60_000, failures = 0))
            return config
        } catch (e: Exception) {
            if (e is PanelHttpException && e.code in listOf(401, 403)) {
                store.write(b.copy(cached = null, uncertain = true, retryAtMs = clock() + 60_000))
                return denied("unauthorized")
            }
            val latest = store.read() ?: b
            val failures = (latest.failures + 1).coerceAtMost(10)
            val seconds = when (e) {
                is PanelRetryException -> e.seconds
                is PanelHttpException -> e.retrySeconds
                else -> 0
            }.coerceAtLeast(0).coerceAtMost(7 * 24 * 3600)
            val exponential = (5_000L * (1L shl failures)).coerceAtMost(30 * 60_000L)
            val delay = maxOf(seconds * 1000, exponential) + random.nextInt(5_001)
            store.write(latest.copy(retryAtMs = clock() + delay, failures = failures))
            throw e
        }
    }
    private fun request(name: String, b: PanelBinding, register: Boolean = false): JsonObject {
        val body = JsonObject().apply {
            addProperty("p_device_code", b.code); addProperty("p_device_token", b.token)
            if (register) { addProperty("p_app_type", "alaa_player"); addProperty("p_model", model) }
        }
        return runCatching { JsonParser.parseString(transport.call(name, body)).asJsonObject }
            .getOrElse { if (it is IOException) throw it; throw PanelParseException("Invalid panel response") }
    }
    private fun denied(status: String) = PanelConfigParser.parse("{\"status\":\"$status\"}")
    private fun retrySeconds(root: JsonObject, fallback: Long): Long =
        runCatching { root.get("retry_after").asLong }.getOrDefault(fallback).coerceAtLeast(1)
    companion object {
        fun canRenewPending(b: PanelBinding) = b.registered && b.sawPending && !b.everActivated && !b.uncertain && !b.legacy
    }
}
