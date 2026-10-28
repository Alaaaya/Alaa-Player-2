package com.streamvault.app.panel

import com.streamvault.domain.model.StreamInfo
import com.streamvault.domain.model.PanelPlaybackAuthorization
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
    private fun stream(id: Long, path: String = "cdn") = StreamInfo("https://shared.invalid/$path", providerId = id,
        panelAuthorizationVersion = PanelPlaybackAuthorization.version(id))

    @Test fun startupGateBlocksAllPreparationUntilSecureIdentityLoaded() {
        val calls = mutableListOf<String>(); val guard = PanelPlaybackGuard(); val p = guard.wrap(engine(calls))
        p.prepare(stream(100)); assertFalse(calls.contains("prepare"))
        guard.ready(); p.prepare(stream(100)); assertTrue(calls.contains("prepare"))
    }
    @Test fun denialStopsPanelEnginesPreloadsAndTimeshiftButNotManualSameHost() = runBlocking {
        val a = mutableListOf<String>(); val b = mutableListOf<String>(); val guard = PanelPlaybackGuard(); guard.ready()
        val panel = guard.wrap(engine(a)); val manual = guard.wrap(engine(b))
        panel.prepare(stream(101)); manual.prepare(stream(102)); a.clear(); b.clear()
        guard.revoke(setOf(101))
        assertEquals(listOf("preload", "stopLiveTimeshift", "stop"), a)
        assertTrue(b.isEmpty()); panel.play(); assertFalse(a.contains("play"))
        manual.play(); assertEquals(listOf("play"), b)
        panel.prepare(stream(101)); assertFalse(a.contains("prepare"))
        guard.authorize(setOf(101))
    }
    @Test fun staleCdnStreamRemainsDeniedAfterGrantReplacementUntilFreshResolution() = runBlocking {
        val calls = mutableListOf<String>(); val guard = PanelPlaybackGuard(); guard.ready(); val p = guard.wrap(engine(calls))
        val stale = stream(103); p.prepare(stale); guard.revoke(setOf(103)); guard.authorize(setOf(103)); calls.clear()
        p.prepare(stale); assertFalse(calls.contains("prepare"))
        calls.clear(); p.prepare(stream(103)); assertTrue(calls.contains("prepare"))
    }
    @Test fun pendingRefreshesNeverInterruptManualPlayback() = runBlocking {
        val calls = mutableListOf<String>(); val guard = PanelPlaybackGuard(); guard.ready(); val p = guard.wrap(engine(calls))
        val manual = stream(104); p.prepare(manual); calls.clear()
        repeat(3) { guard.revoke(emptySet()); guard.authorize(emptySet()); p.play() }
        assertEquals(listOf("play", "play", "play"), calls)
    }
    @Test fun panelPreloadIsClearedWithoutStoppingManualCurrentStream() = runBlocking {
        val calls = mutableListOf<String>(); val guard = PanelPlaybackGuard(); guard.ready(); val p = guard.wrap(engine(calls))
        p.prepare(stream(105)); p.preload(stream(106)); calls.clear(); guard.revoke(setOf(106))
        assertEquals(listOf("preload"), calls); p.play(); assertEquals("play", calls.last())
        guard.authorize(setOf(106))
    }
    @Test fun secureStorageFailureClosesGateAgain() {
        val guard = PanelPlaybackGuard(); guard.ready(); assertTrue(guard.allows(stream(107)))
        guard.failClosed(); assertFalse(guard.allows(stream(107)))
    }
}
