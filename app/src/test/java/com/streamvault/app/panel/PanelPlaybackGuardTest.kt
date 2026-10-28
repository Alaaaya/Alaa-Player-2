package com.streamvault.app.panel

import com.streamvault.domain.model.StreamInfo
import com.streamvault.player.PlayerEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.lang.reflect.Proxy

@OptIn(ExperimentalCoroutinesApi::class)
class PanelPlaybackGuardTest {
    @Before fun before() { Dispatchers.setMain(Dispatchers.Unconfined) }
    @After fun after() { Dispatchers.resetMain() }
    private fun engine(calls: MutableList<String>): PlayerEngine = Proxy.newProxyInstance(
        PlayerEngine::class.java.classLoader, arrayOf(PlayerEngine::class.java),
    ) { _, method, _ -> calls += method.name; null } as PlayerEngine

    @Test fun startupGateBlocksAllPreparationUntilSecureIdentityLoaded() {
        val calls = mutableListOf<String>(); val guard = PanelPlaybackGuard(); val p = guard.wrap(engine(calls))
        p.prepare(StreamInfo("https://p.invalid/live")); assertFalse(calls.contains("prepare"))
        guard.ready(); p.prepare(StreamInfo("https://p.invalid/different")); assertTrue(calls.contains("prepare"))
    }
    @Test fun denialStopsAllEnginesPreloadsAndTimeshiftBeforeReuse() = runBlocking {
        val a = mutableListOf<String>(); val b = mutableListOf<String>(); val guard = PanelPlaybackGuard(); guard.ready()
        val main = guard.wrap(engine(a)); val preview = guard.wrap(engine(b))
        main.prepare(StreamInfo("https://cdn.invalid/redirect")); preview.prepare(StreamInfo("https://p.invalid/live"))
        guard.revoke(setOf(PanelPlaybackGuard.origin("https://p.invalid")))
        for (calls in listOf(a,b)) assertEquals(listOf("preload", "stopLiveTimeshift", "stop"), calls.takeLast(3))
        a.clear(); b.clear(); main.play(); preview.play(); assertTrue(a.isEmpty()); assertTrue(b.isEmpty())
        preview.prepare(StreamInfo("https://p.invalid/new")); assertFalse(b.contains("prepare"))
        a.clear(); main.prepare(StreamInfo("https://manual.invalid/live")); assertTrue(a.contains("prepare"))
    }
    @Test fun repeatedSameResolvedCdnUrlStaysRevokedUntilNewAuthorization() = runBlocking {
        val calls = mutableListOf<String>(); val guard = PanelPlaybackGuard(); guard.ready(); val p = guard.wrap(engine(calls))
        val stream = StreamInfo("https://cdn.invalid/redirect")
        p.prepare(stream); guard.revoke(setOf("https://provider.invalid:443")); calls.clear()
        p.prepare(stream); assertFalse(calls.contains("prepare"))
        guard.authorize(setOf("https://cdn.invalid:443")); calls.clear()
        p.prepare(stream); assertTrue(calls.contains("prepare"))
    }
    @Test fun secureStorageFailureClosesGateAgain() {
        val guard = PanelPlaybackGuard(); guard.ready(); assertTrue(guard.allows("https://manual.invalid"))
        guard.failClosed(); assertFalse(guard.allows("https://manual.invalid"))
    }
    @Test fun originsNormalizeDefaultPortsAndDoNotContainCredentials() {
        assertEquals("https://example.invalid:443", PanelPlaybackGuard.origin("https://user:secret@EXAMPLE.invalid/list?token=secret"))
        assertEquals("http://example.invalid:8080", PanelPlaybackGuard.origin("http://example.invalid:8080/path"))
        assertFalse(PanelPlaybackGuard.origin("https://user:secret@example.invalid").contains("secret"))
    }
}
