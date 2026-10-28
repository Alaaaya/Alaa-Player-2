package com.streamvault.app.panel

import com.google.common.truth.Truth.assertThat
import com.google.gson.JsonParser
import org.junit.Test

class PanelCategorySettingsTest {
    private val generation = "11111111-1111-1111-1111-111111111111"
    private val revision = "hc1_" + "a".repeat(64)
    private fun config(fields: String = "", password: String = "p", type: String = "xtream"): String =
        """{"status":"active","device_code":"ABC","playlists":[{"id":"playlist","type":"$type","server":"https://provider.invalid","username":"u","password":"$password"$fields}]}"""
    private fun fields(ids: String = """{"live":["007"],"vod":["7"],"series":[]}""", version: String = "1") =
        """, "category_settings_version":$version,"category_provider_generation":"$generation","category_config_revision":"$revision","hidden_category_ids":$ids"""
    @Test fun exactStringIdsAndMediaScopesSurvive() {
        val p = PanelConfigParser.parse(config(fields())).playlists.single().categorySettings!!
        assertThat(p.hidden["live"]).containsExactly("007")
        assertThat(p.hidden["vod"]).containsExactly("7")
        assertThat(p.hidden["series"]).isEmpty()
        assertThat(p.revision).isEqualTo(revision)
    }
    @Test fun legacyMissingIsEmptyButPartialIsNot() {
        assertThat(PanelConfigParser.parse(config()).playlists.single().categorySettings).isEqualTo(PanelCategorySettings.LEGACY)
        assertThat(PanelConfigParser.parse(config(",\"category_settings_version\":1")).playlists.single().categorySettings).isNull()
    }
    @Test fun malformedUnknownTypesAndMissingArraysAreNotEmptyPolicy() {
        for (bad in listOf(fields(version="2"), fields(version="\"1\""), fields("""{"live":[7],"vod":[],"series":[]}"""), fields("""{"live":["x"],"vod":[],"series":[]}"""), fields("""{"live":[],"vod":[]}"""))) {
            assertThat(PanelConfigParser.parse(config(bad)).playlists.single().categorySettings).isNull()
        }
    }
    @Test fun invalidPolicyPreservesKnownValidPolicyAndSurvivesSecureCacheRoundTrip() {
        val old = PanelConfigParser.parse(config(fields()))
        val malformed = config(fields(version="2"))
        val resolved = PanelConfigParser.parse(malformed, old)
        assertThat(resolved.playlists.single().categorySettings).isEqualTo(old.playlists.single().categorySettings)
        val cached = PanelConfigParser.cacheJson(JsonParser.parseString(malformed).asJsonObject, resolved)
        assertThat(PanelConfigParser.parse(cached).playlists.single().categorySettings).isEqualTo(old.playlists.single().categorySettings)
    }
    @Test fun invalidReplacementCannotInheritOtherAccountPolicy() {
        val old = PanelConfigParser.parse(config(fields()))
        val changed = PanelConfigParser.parse(config(fields(version="2"), password="replacement"), old)
        assertThat(changed.playlists.single().categorySettings).isNull()
    }
    @Test fun rotatedProviderEpochCannotInheritOldPolicyOnMalformedConfig() {
        val old = PanelConfigParser.parse(config(fields()))
        val rotated = config(fields(version="2")).replace(generation, "22222222-2222-2222-2222-222222222222")
        assertThat(PanelConfigParser.parse(rotated, old).playlists.single().categorySettings).isNull()
    }
    @Test fun explicitEmptyAndLegacyClearPriorHides() {
        val old = PanelConfigParser.parse(config(fields()))
        val clear = PanelConfigParser.parse(config(fields("""{"live":[],"vod":[],"series":[]}""")), old)
        assertThat(clear.playlists.single().categorySettings!!.hidden.values.flatten()).isEmpty()
        assertThat(PanelConfigParser.parse(config(), old).playlists.single().categorySettings).isEqualTo(PanelCategorySettings.LEGACY)
    }
    @Test fun policyChangeDoesNotReconfigureProviderCredentials() {
        val old = PanelConfigParser.parse(config(fields()))
        val next = PanelConfigParser.parse(config(fields("""{"live":[],"vod":[],"series":[]}""")))
        assertThat(old.playlists.single().fingerprint()).isEqualTo(next.playlists.single().fingerprint())
        assertThat(planPanelSync(next,listOf(PanelMapping("playlist",1,old.playlists.single().fingerprint())),setOf(1))).isEmpty()
    }
    @Test fun unsupportedPolicyWithNoHistoryCannotGrantOrStartProviderSetup() {
        val bad = PanelConfigParser.parse(config(fields(version="2")))
        assertThat(planPanelSync(bad,emptyList(),emptySet())).isEmpty()
        assertThat(panelProviderGrants(bad,listOf(PanelMapping("playlist",1,bad.playlists.single().fingerprint()))).second).isEmpty()
    }
    @Test fun m3uIgnoresXtreamPolicyWithoutInventingGroupIds() {
        assertThat(PanelConfigParser.parse(config(fields(version="2"),type="m3u")).playlists.single().categorySettings).isEqualTo(PanelCategorySettings.LEGACY)
    }
}
