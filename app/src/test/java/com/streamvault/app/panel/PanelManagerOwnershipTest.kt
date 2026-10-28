package com.streamvault.app.panel

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.streamvault.domain.model.*
import com.streamvault.domain.repository.ProviderRepository
import com.streamvault.domain.usecase.*
import com.streamvault.player.PlayerEngine
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.mockito.kotlin.*
import java.lang.reflect.Proxy
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn

/** Exercises the real manager and guard; preferences bypass Android Keystore only. */
@OptIn(ExperimentalCoroutinesApi::class)
class PanelManagerOwnershipTest {
    private val id = 92001L
    private val manualId = 92002L
    private lateinit var repo: ProviderRepository
    private lateinit var setup: ValidateAndAddProvider
    private lateinit var guard: PanelPlaybackGuard
    private lateinit var manager: PanelManager
    private val values = mutableMapOf<String, Any?>()
    private val gson = Gson()
    private val old = PanelPlaylist("p", "Panel", PanelPlaylistType.XTREAM, null, null, "https://same.invalid", "old", "pass")
    private val changed get() = old.copy(username = "new")
    private fun provider(n: Long = id) = LegacyProvider(id = n, name = "Panel", type = ProviderType.XTREAM_CODES, serverUrl = "https://same.invalid")
    private fun stream(n: Long = id) = StreamInfo("https://cdn.invalid/live", providerId = n,
        panelAuthorizationVersion = PanelPlaybackAuthorization.version(n))
    private fun config(vararg p: PanelPlaylist) = PanelConfig(PanelStatus.ACTIVE, "TEST", null, p.toList())

    @Before fun before() = runBlocking {
        Dispatchers.setMain(Dispatchers.Unconfined)
        PanelPlaybackAuthorization.authorize(setOf(id, manualId))
        values.clear()
        repo = mock(); setup = mock(); guard = PanelPlaybackGuard(); guard.ready()
        whenever(repo.getProviders()).thenReturn(MutableStateFlow(listOf(provider(), provider(manualId))))
        manager = PanelManager(mock<Context>(), setup, repo, guard)
        val prefs = memoryPrefs()
        field("securePrefs" + "$" + "delegate", lazyOf(prefs))
        field("plainPrefs" + "$" + "delegate", lazyOf(prefs))
        val store = object : PanelBindingStore {
            private var binding = PanelBinding("TEST", "a".repeat(64))
            override fun read() = binding
            override fun write(binding: PanelBinding) { this.binding = binding }
        }
        field("protocol" + "$" + "delegate", lazyOf(PanelProtocol(store, PanelTransport { _, _ -> error("Unexpected network") }, "test")))
    }
    @After fun after() { PanelPlaybackAuthorization.authorize(setOf(id, manualId)); Dispatchers.resetMain() }
    private fun field(name: String, value: Any) { manager.javaClass.getDeclaredField(name).apply { isAccessible = true }.set(manager, value) }
    private fun seed() { values["mappings"] = gson.toJson(listOf(PanelMapping("p", id, old.fingerprint()))) }
    private fun mappings(): List<PanelMapping> = gson.fromJson(values["mappings"] as? String ?: "[]", Array<PanelMapping>::class.java).toList()
    private suspend fun enforce(c: PanelConfig): Unit = suspendCoroutineUninterceptedOrReturn { continuation ->
        manager.javaClass.getDeclaredMethod("enforceKnownConfig", PanelConfig::class.java, Continuation::class.java)
            .apply { isAccessible = true }.invoke(manager, c, continuation)
    }
    private fun engine(calls: MutableList<String>): PlayerEngine = Proxy.newProxyInstance(PlayerEngine::class.java.classLoader,
        arrayOf(PlayerEngine::class.java)) { _, method, _ -> calls += method.name; null } as PlayerEngine

    @Test fun savedWithWarningRetainsFingerprintAndDenial(): Unit = runBlocking {
        seed()
        whenever(setup.loginXtream(any(), isNull())).thenReturn(ValidateAndAddProviderResult.SavedWithWarning(provider(), "sync incomplete"))
        enforce(config(changed))
        assertEquals(old.fingerprint(), mappings().single().fingerprint)
        assertFalse(guard.allows(stream()))
        assertTrue(manager.state.value.failures.isNotEmpty())
        enforce(config(changed))
        verify(setup, times(2)).loginXtream(any(), isNull())
    }
    @Test fun reservationOwnershipIsPersistedBeforeOnboarding(): Unit = runBlocking {
        whenever(repo.addProvider(any())).thenReturn(Result.Success(id))
        whenever(setup.loginXtream(any(), isNull())).thenAnswer {
            assertEquals(PanelMapping("p", id, ""), mappings().single())
            assertTrue((values["denied_provider_ids"] as Set<*>).contains(id.toString()))
            assertFalse(guard.allows(stream()))
            ValidateAndAddProviderResult.Error("no network")
        }
        enforce(config(old))
        verify(repo).addProvider(check { assertFalse(it.isActive); assertTrue(it.serverUrl.endsWith(".invalid")); assertFalse(it.serverUrl.contains("same.invalid")) })
        verify(setup).loginXtream(check { assertEquals(id, it.existingProviderId) }, isNull())
        assertEquals("", mappings().single().fingerprint)
    }
    @Test fun emptyPendingDoesNotInterruptManualPlayback(): Unit = runBlocking {
        val calls = mutableListOf<String>(); val player = guard.wrap(engine(calls))
        player.prepare(stream(manualId)); calls.clear()
        repeat(2) { enforce(PanelConfig(PanelStatus.INACTIVE, "TEST", null, emptyList(), "pending")) }
        assertTrue(calls.isEmpty()); player.play(); assertEquals(listOf("play"), calls)
        verify(repo, never()).deleteProvider(any(), isNull())
    }
    @Test fun activeRemovalRevokesAndStopsBeforeDelete(): Unit = runBlocking {
        seed(); val calls = mutableListOf<String>(); val player = guard.wrap(engine(calls))
        player.prepare(stream()); calls.clear()
        whenever(repo.deleteProvider(id)).thenAnswer {
            assertFalse(guard.allows(stream()))
            assertEquals(listOf("preload", "stopLiveTimeshift", "stop"), calls)
            Result.Success(com.streamvault.domain.repository.ProviderDeleteOutcome(id, 0, false))
        }
        enforce(config())
        verify(repo).deleteProvider(id); assertTrue(mappings().isEmpty()); assertFalse(guard.allows(stream()))
        assertTrue(guard.allows(stream(manualId)))
    }
    @Test fun successfulUpdateAllowsOnlyFreshGeneration(): Unit = runBlocking {
        seed(); val stale = stream()
        whenever(setup.loginXtream(any(), isNull())).thenReturn(ValidateAndAddProviderResult.Success(provider()))
        enforce(config(changed))
        assertEquals(changed.fingerprint(), mappings().single().fingerprint)
        assertFalse(guard.allows(stale)); assertTrue(guard.allows(stream()))
        assertTrue(guard.allows(stream(manualId)))
    }

    private fun memoryPrefs(): SharedPreferences {
        val editor = Proxy.newProxyInstance(SharedPreferences.Editor::class.java.classLoader, arrayOf(SharedPreferences.Editor::class.java)) { proxy, method, args ->
            when (method.name) {
                "putString", "putStringSet", "putLong", "putBoolean" -> { values[args!![0] as String] = args[1]; proxy }
                "remove" -> { values.remove(args!![0]); proxy }
                "commit" -> true
                "apply" -> null
                else -> error("Unexpected editor " + method.name)
            }
        } as SharedPreferences.Editor
        return Proxy.newProxyInstance(SharedPreferences::class.java.classLoader, arrayOf(SharedPreferences::class.java)) { _, method, args ->
            when (method.name) {
                "edit" -> editor
                "getString", "getStringSet", "getLong", "getBoolean" -> values[args!![0]] ?: args[1]
                "contains" -> values.containsKey(args!![0])
                else -> error("Unexpected preferences " + method.name)
            }
        } as SharedPreferences
    }
}
