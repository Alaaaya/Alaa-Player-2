package com.streamvault.app.panel

import com.streamvault.domain.model.PanelArtworkPolicyRegistry as Registry
import org.junit.Assert.*
import org.junit.Test

class PanelArtworkRegistryTest {
    @Test fun ownershipTransitionSharedManualAndStaleWrites() {
        Registry.completeStartup()
        val url = "https://artwork.invalid/unique-panel-test"
        Registry.record(91001, url, "vod:1", true)
        var cancelled = false
        val lease = Registry.acquire(url) { cancelled = true }
        assertTrue(lease.tracked)
        assertTrue(lease.allowed())
        val oldEpoch = Registry.epoch(91001)
        Registry.beginTransition(91001)
        assertTrue(cancelled)
        assertFalse(lease.allowed())
        assertFalse(Registry.record(91001, url, "vod:1", true, oldEpoch))
        assertThrows(IllegalStateException::class.java) { Registry.acquire(url) {} }
        // Shared physical URL becomes usable only with an explicitly allowed owner.
        Registry.record(91002, url, "series:2", true)
        Registry.acquire(url) {}.use { assertTrue(it.allowed()) }
        Registry.record(0, url, "manual:logo", true)
        Registry.beginTransition(91002)
        Registry.acquire(url) {}.use { assertTrue(it.allowed()) }
        val manual = Registry.acquire("https://manual.invalid/unowned-test") {}
        Registry.beginTransition(91001)
        assertFalse(manual.tracked)
        assertTrue(manual.allowed())
        lease.close()
        manual.close()
    }
}
