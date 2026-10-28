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
}
