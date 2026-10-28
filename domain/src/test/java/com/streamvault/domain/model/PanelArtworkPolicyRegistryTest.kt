package com.streamvault.domain.model

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PanelArtworkPolicyRegistryTest {
    private val registry = PanelArtworkPolicyRegistry
    private val url = "https://images.invalid/poster"
    @Before fun reset() { registry.resetForTests() }

    @Test fun startupDeniesKnownAndUnknownRemoteEvenIfCached() {
        registry.record(1, url, "vod:1", true)
        assertThrows(IllegalStateException::class.java) { registry.acquire(url) {} }
        assertThrows(IllegalStateException::class.java) { registry.acquire("https://manual.invalid/cache") {} }
        registry.completeStartup()
        registry.acquire(url) {}.use { assertTrue(it.allowed()) }
    }
    @Test fun lateDenialInvalidatesLeaseBeforeCancellationAndPurgeOutsideMonitor() {
        registry.completeStartup()
        registry.record(1, url, "vod:1", true)
        var cancelled = false
        lateinit var lease: PanelArtworkPolicyRegistry.Lease
        lease = registry.acquire(url) {
            assertFalse(Thread.holdsLock(registry))
            assertFalse(lease.allowed())
            cancelled = true
        }
        var purged = false
        registry.addInvalidationListener {
            assertFalse(Thread.holdsLock(registry)); assertFalse(lease.allowed()); purged = true
        }
        registry.record(1, url, "vod:1", false)
        assertTrue(cancelled); assertTrue(purged); assertFalse(lease.allowed())
        registry.record(1, url, "vod:1", true)
        assertFalse("Old result stays obsolete after reallow", lease.allowed())
    }
    @Test fun sharedVisibleOwnerSurvivesDenialAndTransition() {
        registry.completeStartup()
        registry.record(1, url, "vod:1", true)
        registry.record(2, url, "series:2", true)
        var cancelled = false
        val lease = registry.acquire(url) { cancelled = true }
        registry.record(1, url, "vod:1", false)
        assertTrue(lease.allowed()); assertFalse(cancelled)
        registry.beginTransition(1)
        assertTrue(lease.allowed()); assertFalse(cancelled)
        registry.beginTransition(2)
        assertFalse(lease.allowed()); assertTrue(cancelled)
    }
    @Test fun unrelatedProviderAndManualLeasesRemainValid() {
        registry.completeStartup()
        registry.record(1, url, "vod:1", true)
        registry.record(2, "https://other.invalid/logo", "live:2", true)
        val other = registry.acquire("https://other.invalid/logo") { fail("Unrelated lease cancelled") }
        val manual = registry.acquire("https://manual.invalid/logo") { fail("Manual lease cancelled") }
        registry.beginTransition(1)
        assertTrue(other.allowed()); assertTrue(manual.allowed()); assertFalse(manual.tracked)
    }
    @Test fun staleEpochCannotRestoreDeniedOwnership() {
        registry.completeStartup()
        val epoch = registry.epoch(1)
        registry.record(1, url, "vod:1", true, epoch)
        registry.beginTransition(1)
        assertFalse(registry.record(1, url, "vod:1", true, epoch))
        assertThrows(IllegalStateException::class.java) { registry.acquire(url) {} }
    }
    @Test fun unknownLeaseAlsoInvalidatedWhenFirstDiscoveredAsDenied() {
        registry.completeStartup()
        var cancelled = false
        val lease = registry.acquire(url) { cancelled = true }
        registry.record(1, url, "vod:1", false)
        assertTrue(cancelled); assertFalse(lease.allowed())
    }
    @Test fun failingCallbackDoesNotSkipOtherCancellationOrCachePurge() {
        registry.completeStartup(); registry.record(1, url, "vod:1", true)
        registry.acquire(url) { throw IllegalStateException("test callback") }
        var purged = false
        registry.addInvalidationListener { purged = true }
        assertThrows(IllegalStateException::class.java) { registry.record(1, url, "vod:1", false) }
        assertTrue(purged)
        assertThrows(IllegalStateException::class.java) { registry.acquire(url) {} }
    }
    @Test fun nestedDeferralRevokesImmediatelyButDispatchesAfterDurableBoundary() {
        registry.completeStartup(); registry.record(1, url, "vod:1", true)
        val policyMonitor = Any()
        var committed = false
        var cancelled = 0
        val lease = registry.acquire(url) {
            assertFalse(Thread.holdsLock(policyMonitor))
            assertFalse(Thread.holdsLock(registry))
            assertTrue(committed)
            cancelled++
        }
        registry.deferInvalidationCallbacks {
            synchronized(policyMonitor) {
                registry.deferInvalidationCallbacks { registry.record(1, url, "vod:1", false) }
                assertFalse(lease.allowed())
                assertEquals(0, cancelled)
                committed = true
            }
            assertEquals(0, cancelled)
        }
        assertEquals(1, cancelled)
    }

    @Test fun exceptionalExitStillFlushesAndPreservesPrimaryFailure() {
        registry.completeStartup(); registry.record(1, url, "vod:1", true)
        val primary = IllegalStateException("transaction failed")
        val failingLease = registry.acquire(url) { throw IllegalArgumentException("cancel failed") }
        var purged = false
        registry.addInvalidationListener { purged = true }
        val thrown = assertThrows(IllegalStateException::class.java) {
            registry.deferInvalidationCallbacks {
                registry.record(1, url, "vod:1", false)
                throw primary
            }
        }
        assertSame(primary, thrown)
        assertEquals(1, thrown.suppressed.size)
        assertTrue(purged)
        // Thread-local scope was removed despite both failures.
        failingLease.close()
        registry.record(1, url, "vod:1", true)
        var cancelled = false
        registry.acquire(url) { cancelled = true }
        registry.record(1, url, "vod:1", false)
        assertTrue(cancelled)
    }

    @Test fun transitionCallbacksAlsoRespectOuterDeferral() {
        registry.completeStartup(); registry.record(1, url, "vod:1", true)
        var cancelled = false
        val lease = registry.acquire(url) { cancelled = true }
        registry.deferInvalidationCallbacks {
            registry.beginTransition(1)
            assertFalse(lease.allowed()); assertFalse(cancelled)
        }
        assertTrue(cancelled)
    }

    @Test fun persistedMembershipDenialDispatchesAfterActualPolicyMonitorExit() {
        val policies = PanelCategoryPolicyRegistry
        val id = 994100L
        registry.completeStartup()
        policies.install(id, PanelCategoryPolicy("device","playlist","generation","rev",
            mapOf("live" to emptySet(), "vod" to setOf("007"), "series" to emptySet())))
        var committed = false
        policies.attach(id, object : PanelCategoryPolicyRegistry.Persistence {
            override fun membership(providerId: Long, media: String, contentId: Long, categories: Set<String>, allowed: Boolean) {
                assertTrue(Thread.holdsLock(policies))
                registry.record(providerId, url, "vod:1", allowed)
                committed = true
            }
            override fun artwork(providerId: Long, media: String, contentId: Long, urls: List<String?>, expectedEpoch: Long) {}
            override fun categoryKey(providerId: Long, media: String, rawId: String) = 1L
            override fun rawCategory(providerId: Long, media: String, key: Long): String? = null
        })
        registry.record(id, url, "vod:1", true)
        var cancelled = false
        val lease = registry.acquire(url) {
            assertFalse(Thread.holdsLock(policies))
            assertTrue(committed)
            assertFalse(policies.allowsContent(id, "vod", 1))
            cancelled = true
        }
        try {
            assertFalse(policies.recordMembership(policies.snapshot(id), "vod", 1, setOf("007")))
            assertFalse(lease.allowed()); assertTrue(cancelled)
        } finally { lease.close(); policies.remove(id) }
    }

}
