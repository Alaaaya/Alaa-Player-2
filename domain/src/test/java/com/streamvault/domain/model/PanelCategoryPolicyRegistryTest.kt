package com.streamvault.domain.model

import org.junit.After
import org.junit.Assert.*
import org.junit.Test

class PanelCategoryPolicyRegistryTest {
    private val id = 9834751L
    private fun policy(hidden: Map<String, Set<String>> = mapOf("live" to setOf("007"), "vod" to emptySet(), "series" to setOf("21"))) =
        PanelCategoryPolicy("device-a", "playlist-a", "generation-a", "opaque-revision", hidden)
    @After fun clear() { PanelCategoryPolicyRegistry.remove(id); PanelCategoryPolicyRegistry.remove(id + 1) }
    @Test fun manualProvidersRemainUnrestricted() {
        assertTrue(PanelCategoryPolicyRegistry.allowsCategory(id, "live", null))
        assertTrue(PanelCategoryPolicyRegistry.allowsContent(id, "series", 99))
    }
    @Test fun blockedTrackedIsNotManual() {
        PanelCategoryPolicyRegistry.block(id)
        assertNull(PanelCategoryPolicyRegistry.current(id))
        assertTrue(PanelCategoryPolicyRegistry.isTracked(id))
        assertFalse(PanelCategoryPolicyRegistry.allowsCategory(id, "vod", "1"))
        assertFalse(PanelCategoryPolicyRegistry.isCurrent(PanelCategoryPolicyRegistry.snapshot(id)))
    }
    @Test fun exactIdsAndMediaAreIsolated() {
        PanelCategoryPolicyRegistry.install(id, policy())
        assertFalse(PanelCategoryPolicyRegistry.allowsCategory(id, "live", "007"))
        assertTrue(PanelCategoryPolicyRegistry.allowsCategory(id, "live", "7"))
        assertTrue(PanelCategoryPolicyRegistry.allowsCategory(id, "vod", "007"))
        assertTrue(PanelCategoryPolicyRegistry.allowsCategory(id + 1, "live", "007"))
        assertFalse(PanelCategoryPolicyRegistry.allowsCategory(id, "live", null))
    }
    @Test fun unknownMembershipFailsClosedAndHiddenAlternateDenies() {
        PanelCategoryPolicyRegistry.install(id, policy())
        val token = PanelCategoryPolicyRegistry.snapshot(id)
        assertFalse(PanelCategoryPolicyRegistry.allowsContent(id, "live", 3))
        assertFalse(PanelCategoryPolicyRegistry.recordMembership(token, "live", 3, setOf("7", "007")))
        assertFalse(PanelCategoryPolicyRegistry.allowsContent(id, "live", 3))
        assertTrue(PanelCategoryPolicyRegistry.recordMembership(token, "live", 3, setOf("7")))
        assertTrue(PanelCategoryPolicyRegistry.allowsContent(id, "live", 3))
        assertFalse(PanelCategoryPolicyRegistry.allowsContent(id, "series", 3))
    }
    @Test fun changedScopeInvalidatesLateCallbacksEvenWhenRevisionReused() {
        PanelCategoryPolicyRegistry.install(id, policy())
        val old = PanelCategoryPolicyRegistry.snapshot(id)
        PanelCategoryPolicyRegistry.install(id, policy().copy(providerGeneration = "replacement"))
        assertFalse(PanelCategoryPolicyRegistry.isCurrent(old))
        assertFalse(PanelCategoryPolicyRegistry.recordMembership(old, "live", 10, setOf("7")))
        assertFalse(PanelCategoryPolicyRegistry.allowsContent(id, "live", 10))
    }
    @Test fun removeAndReinstallNeverResurrectOldToken() {
        PanelCategoryPolicyRegistry.install(id, policy())
        val old = PanelCategoryPolicyRegistry.snapshot(id)
        PanelCategoryPolicyRegistry.remove(id)
        PanelCategoryPolicyRegistry.install(id, policy())
        assertFalse(PanelCategoryPolicyRegistry.isCurrent(old))
    }
    @Test fun inputSetsAreDefensivelyCopied() {
        val ids = mutableSetOf("007")
        PanelCategoryPolicyRegistry.install(id, policy(mapOf("live" to ids, "vod" to emptySet(), "series" to emptySet())))
        ids.clear()
        assertFalse(PanelCategoryPolicyRegistry.allowsCategory(id, "live", "007"))
    }
}
