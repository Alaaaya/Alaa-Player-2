package com.streamvault.data.remote.xtream

import com.streamvault.data.remote.http.HttpRequestProfile
import com.streamvault.domain.model.PanelCategoryPolicy
import com.streamvault.domain.model.PanelCategoryPolicyRegistry as Policies
import org.junit.After
import org.junit.Assert.*
import org.junit.Test

class PanelXtreamRequestFenceTest {
    private val id = 734982L
    private val profile = HttpRequestProfile(ownerTag = "provider:$id/xtream")
    private fun install(revision: String = "hc1_test") = Policies.install(id,
        PanelCategoryPolicy("device", "playlist", "generation", revision,
            mapOf("live" to setOf("007"), "vod" to setOf("9"), "series" to setOf("11"))))
    private fun fence(action: String, extra: String = "") = PanelXtreamRequestFence(
        "https://example.invalid/player_api.php?action=$action$extra", profile)
    private fun denied(block: () -> Unit) { try { block(); fail("Expected denial") } catch (_: IllegalStateException) {} }
    @After fun clear() { Policies.remove(id) }
    @Test fun noGlobalCatalogRequestsForAnyPanelMedia() {
        install()
        listOf("get_live_streams", "get_vod_streams", "get_series").forEach { action -> denied { fence(action).checkRequest() } }
    }
    @Test fun hiddenCategoryCannotReachTransportButAllowedOneCan() {
        install()
        denied { fence("get_live_streams", "&category_id=007").checkRequest() }
        fence("get_live_streams", "&category_id=7").checkRequest()
        denied { fence("get_vod_streams", "&category_id=9").checkRequest() }
        denied { fence("get_series", "&category_id=11").checkRequest() }
    }
    @Test fun staleResponseAndThinCallbackAreRejected() {
        install()
        val old = fence("get_live_streams", "&category_id=7")
        old.checkRequest()
        install("hc1_changed")
        denied { old.filter(listOf("cached-response")) }
        denied { old.accepts("thin-decoder-item") }
    }
    @Test fun unknownDetailsAndEpgDeniedUntilMembershipKnown() {
        install()
        listOf("get_vod_info&vod_id=5", "get_series_info&series_id=5", "get_short_epg&stream_id=5").forEach { action -> denied { fence(action).checkRequest() } }
        assertTrue(Policies.recordMembership(Policies.snapshot(id), "vod", 5, setOf("8")))
        fence("get_vod_info", "&vod_id=5").checkRequest()
    }
    @Test fun manualTrafficAndOtherProviderUnaffected() {
        PanelXtreamRequestFence("https://example.invalid/player_api.php?action=get_vod_streams", HttpRequestProfile()).checkRequest()
        install()
        PanelXtreamRequestFence("https://example.invalid/player_api.php?action=get_vod_streams", HttpRequestProfile(ownerTag = "provider:${id + 1}/xtream")).checkRequest()
    }
}
