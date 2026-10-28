package com.streamvault.domain.model

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch

class PanelPlaybackAuthorizationTest {
    @Before fun setUp() = PanelPlaybackAuthorization.resetForTests()
    @After fun tearDown() = PanelPlaybackAuthorization.resetForTests()

    private fun stream(id: Long? = null, version: Long = 0L) =
        StreamInfo("https://cdn.example/stream", providerId = id, panelAuthorizationVersion = version)

    @Test fun initialAndManualStreamsAreAllowed() {
        assertEquals(0L, PanelPlaybackAuthorization.version(null))
        assertEquals(0L, PanelPlaybackAuthorization.version(1L))
        assertTrue(PanelPlaybackAuthorization.allows(stream()))
        assertTrue(PanelPlaybackAuthorization.allows(stream(1L)))
        PanelPlaybackAuthorization.revoke(setOf(1L))
        assertTrue(PanelPlaybackAuthorization.allows(stream()))
        assertTrue(PanelPlaybackAuthorization.allows(stream(2L)))
    }

    @Test fun revokeDeniesEvenFreshStreamAndAuthorizeDoesNotRestoreStaleStream() {
        val prepared = stream(1L)
        PanelPlaybackAuthorization.revoke(setOf(1L))
        assertEquals(1L, PanelPlaybackAuthorization.version(1L))
        assertFalse(PanelPlaybackAuthorization.allows(prepared))
        val fresh = stream(1L, PanelPlaybackAuthorization.version(1L))
        assertFalse(PanelPlaybackAuthorization.allows(fresh))
        PanelPlaybackAuthorization.authorize(setOf(1L))
        assertEquals(1L, PanelPlaybackAuthorization.version(1L))
        assertFalse(PanelPlaybackAuthorization.allows(prepared))
        assertTrue(PanelPlaybackAuthorization.allows(fresh))
        PanelPlaybackAuthorization.authorize(setOf(1L))
        assertEquals(1L, PanelPlaybackAuthorization.version(1L))
    }

    @Test fun repeatedRevocationsInvalidateEachGenerationWithoutAffectingOtherIds() {
        PanelPlaybackAuthorization.revoke(setOf(1L, 2L))
        PanelPlaybackAuthorization.authorize(setOf(1L, 2L))
        val firstGeneration = stream(1L, 1L)
        PanelPlaybackAuthorization.revoke(setOf(1L))
        PanelPlaybackAuthorization.authorize(setOf(1L))
        assertEquals(2L, PanelPlaybackAuthorization.version(1L))
        assertFalse(PanelPlaybackAuthorization.allows(firstGeneration))
        assertTrue(PanelPlaybackAuthorization.allows(stream(1L, 2L)))
        assertTrue(PanelPlaybackAuthorization.allows(stream(2L, 1L)))
        assertFalse(PanelPlaybackAuthorization.allows(stream(1L, 3L)))
    }

    @Test fun concurrentRevocationsDoNotLoseGenerationIncrements() {
        val start = CountDownLatch(1)
        val threads = List(8) {
            Thread {
                start.await()
                repeat(100) { PanelPlaybackAuthorization.revoke(setOf(1L)) }
            }.apply { start() }
        }
        start.countDown()
        threads.forEach { it.join() }
        assertEquals(800L, PanelPlaybackAuthorization.version(1L))
        assertFalse(PanelPlaybackAuthorization.allows(stream(1L, 800L)))
        PanelPlaybackAuthorization.authorize(setOf(1L))
        assertTrue(PanelPlaybackAuthorization.allows(stream(1L, 800L)))
    }
}
