package com.streamvault.app.panel

import com.google.gson.JsonObject
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class PanelProtocolTest {
    private class Store : PanelBindingStore {
        var b: PanelBinding? = null
        var fail = false
        override fun read() = b
        override fun write(binding: PanelBinding) { check(!fail); b = binding }
    }
    private class Fixture {
        val store = Store()
        var now = 1_000_000L
        val calls = mutableListOf<Pair<String, JsonObject>>()
        var response: (String, JsonObject) -> String = { name, _ ->
            if (name == "register_device_v2") """{"status":"registered"}""" else """{"status":"pending"}"""
        }
        val client = PanelProtocol(store, PanelTransport { name, body ->
            assertNotNull("identity persisted before transport", store.b)
            assertEquals(store.b!!.token, body["p_device_token"].asString)
            calls += name to body
            response(name, body)
        }, "TV", { now })
        fun advance() { now += 4_000_000 }
        fun active() = """{"status":"active","device_code":"${store.b!!.code}","app_type":"alaa_player","playlists":[{"id":"p","type":"xtream","server":"https://provider.invalid","username":"dummy","password":"secret"}]}"""
    }
    @Test fun randomIdentityHasRequiredEntropyFormatAndSurvivesRestart() {
        val f = Fixture(); val a = f.client.identity()
        assertTrue(a.code.matches(Regex("[A-F0-9]{12}")))
        assertTrue(a.token.matches(Regex("[a-f0-9]{64}")))
        assertEquals(a, f.client.identity()); assertFalse(a.toString().contains(a.token))
        assertEquals(100, (1..100).map { Fixture().client.identity().token }.toSet().size)
    }
    @Test fun persistenceFailurePreventsNetwork() {
        val f = Fixture(); f.store.fail = true
        assertTrue(runCatching { f.client.refresh() }.isFailure); assertTrue(f.calls.isEmpty())
    }
    @Test fun registersAppLabelAndPollsPendingWithoutReregistering() {
        val f = Fixture(); assertEquals("pending", f.client.refresh()!!.protocolStatus)
        assertEquals("alaa_player", f.calls[0].second["p_app_type"].asString)
        assertTrue(f.store.b!!.sawPending); val before = f.calls.size
        assertNull(f.client.refresh()); assertEquals(before, f.calls.size)
        f.advance(); f.client.refresh()
        assertEquals(listOf("register_device_v2", "get_device_config_v2", "get_device_config_v2"), f.calls.map { it.first })
    }
    @Test fun activeCredentialsCachedAndActivationProofPersisted() {
        val f = Fixture(); f.client.identity()
        f.response = { name, _ -> if (name == "register_device_v2") """{"status":"registered"}""" else f.active() }
        assertEquals(PanelStatus.ACTIVE, f.client.refresh()!!.status)
        assertTrue(f.store.b!!.everActivated); assertNotNull(f.client.cached())
    }
    @Test fun authoritativeDenialsClearCacheAndNeverRotate() {
        for (status in listOf("inactive", "expired", "unauthorized", "registration_expired")) {
            val f = Fixture(); f.client.identity(); f.store.b = f.store.b!!.copy(registered = true, everActivated = true, cached = f.active())
            val identity = f.store.b!!
            f.response = { _, _ -> """{"status":"$status","playlists":[{"id":"leak","type":"m3u","url":"secret"}]}""" }
            val c = f.client.refresh()!!
            assertTrue(c.playlists.isEmpty()); assertNull(f.client.cached())
            assertEquals(identity.code, f.store.b!!.code); assertEquals(identity.token, f.store.b!!.token)
        }
    }
    @Test fun rateLimitRetainsActiveCacheAndHonorsRetryAfter() {
        val f = Fixture(); f.client.identity(); f.store.b = f.store.b!!.copy(registered = true, cached = f.active())
        f.response = { _, _ -> """{"status":"rate_limited","retry_after":3600}""" }
        assertTrue(runCatching { f.client.refresh() }.exceptionOrNull() is PanelRetryException)
        assertNotNull(f.client.cached()); assertTrue(f.store.b!!.retryAtMs >= f.now + 3_600_000)
        val count = f.calls.size; f.now += 60_000; assertNull(f.client.refresh()); assertEquals(count, f.calls.size)
    }
    @Test fun temporaryNetworkFailurePreservesCredentialsAndBinding() {
        val f = Fixture(); val b = f.client.identity(); f.store.b = b.copy(registered = true, cached = f.active())
        f.response = { _, _ -> throw IOException("offline") }
        assertTrue(runCatching { f.client.refresh() }.isFailure)
        assertEquals(b.token, f.store.b!!.token); assertNotNull(f.client.cached()); assertTrue(f.store.b!!.retryAtMs > f.now)
    }
    @Test fun registrationNetworkRetryUsesSameIdentity() {
        val f = Fixture(); f.response = { _, _ -> throw IOException() }
        runCatching { f.client.refresh() }; val first = f.store.b!!
        f.advance(); runCatching { f.client.refresh() }
        assertEquals(first.code, f.store.b!!.code); assertEquals(first.token, f.store.b!!.token)
    }
    @Test fun httpUnauthorizedFailsClosed() {
        val f = Fixture(); f.client.identity(); f.store.b = f.store.b!!.copy(registered = true, cached = f.active())
        f.response = { _, _ -> throw PanelHttpException(403) }
        assertEquals("unauthorized", f.client.refresh()!!.protocolStatus); assertNull(f.client.cached())
    }
    @Test fun http429BackoffSurvivesProtocolRecreation() {
        val f = Fixture(); f.response = { _, _ -> throw PanelHttpException(429, 600) }
        runCatching { f.client.refresh() }; assertTrue(f.store.b!!.retryAtMs >= f.now + 600_000)
        val client = PanelProtocol(f.store, PanelTransport { _, _ -> fail("backoff bypassed"); "" }, "TV", { f.now })
        assertNull(client.refresh())
    }
    @Test fun legacyIdentityPreservedAndNeverPubliclyRegistered() {
        val f = Fixture(); val old = f.client.identity("7KQ2M9XD", true)
        assertTrue(old.legacy); assertEquals("7KQ2M9XD", old.code)
        f.response = { _, _ -> """{"status":"unauthorized"}""" }; f.client.refresh()
        assertEquals(listOf("get_device_config_v2"), f.calls.map { it.first })
        assertEquals(old.token, f.store.b!!.token); assertTrue(f.store.b!!.uncertain)
    }
    @Test fun expiredNeverActivatedPendingCanRenewOnlyWithProof() {
        val f = Fixture(); f.client.refresh(); val old = f.store.b!!
        f.advance(); f.response = { _, _ -> """{"status":"registration_expired"}""" }
        f.client.refresh(); assertNotEquals(old.code, f.store.b!!.code); assertNotEquals(old.token, f.store.b!!.token)
        assertFalse(f.store.b!!.registered); assertNull(f.store.b!!.cached)
    }
    @Test fun renewalProofRejectsLegacyUnseenUncertainOrEverActivated() {
        val b = PanelBinding("A1B2C3D4E5F6", "a".repeat(64), registered = true, sawPending = true)
        assertTrue(PanelProtocol.canRenewPending(b))
        for (bad in listOf(b.copy(legacy = true), b.copy(uncertain = true), b.copy(everActivated = true), b.copy(sawPending = false), b.copy(registered = false)))
            assertFalse(PanelProtocol.canRenewPending(bad))
    }
    @Test fun inactiveIsNotProofOfNeverActivated() {
        val f = Fixture(); f.client.refresh(); f.advance(); f.response = { _, _ -> """{"status":"inactive"}""" }
        f.client.refresh(); val b = f.store.b!!; assertTrue(b.uncertain)
        f.advance(); f.response = { _, _ -> """{"status":"registration_expired"}""" }; f.client.refresh()
        assertEquals(b.code, f.store.b!!.code)
    }
    @Test fun collisionDoesNotTakeOverOrRotate() {
        val f = Fixture(); val b = f.client.identity(); f.response = { _, _ -> """{"status":"unavailable"}""" }
        assertEquals("unauthorized", f.client.refresh()!!.protocolStatus)
        assertEquals(b.code, f.store.b!!.code); assertEquals(b.token, f.store.b!!.token)
    }
    @Test fun malformedResponsesBackoffWithoutSecretException() {
        for (bad in listOf("", "[]", "null", "<html>", "{\"status\":\"surprise\"}")) {
            val f = Fixture(); f.response = { _, _ -> bad }
            val e = runCatching { f.client.refresh() }.exceptionOrNull()!!
            assertFalse(e.message.orEmpty().contains(f.store.b!!.token)); assertTrue(f.store.b!!.retryAtMs > f.now)
        }
    }
    @Test fun mismatchedActiveDeviceOrAppFailsClosed() {
        val f = Fixture(); f.client.identity(); f.store.b = f.store.b!!.copy(registered = true)
        f.response = { _, _ -> f.active().replace("alaa_player", "universe") }
        assertEquals("unauthorized", f.client.refresh()!!.protocolStatus); assertNull(f.client.cached())
    }
    @Test fun invalidStoredIdentityCannotSilentlyRecreate() {
        val f = Fixture(); f.store.b = PanelBinding("CODE", "bad")
        assertTrue(runCatching { f.client.refresh() }.isFailure); assertTrue(f.calls.isEmpty())
    }
}
