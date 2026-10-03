package com.streamvault.app.panel

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.security.SecureRandom

class PanelSyncTest {
    private val activeJson = """
        {"status":"active","device_code":"7KQ2M9XD","expires_at":"2027-01-01T22:59:59+00:00",
         "playlists":[
           {"id":"a","name":"Main","type":"m3u","url":"http://x/list.m3u","epg_url":null,"server":null,"username":null,"password":null},
           {"id":"b","name":"XC","type":"xtream","server":"http://host:8080","username":"u","password":"p","epg_url":null,"url":null},
           {"id":"c","name":"Weird","type":"stalker","url":"http://s"},
           {"name":"NoId","type":"m3u","url":"http://y"}]}
    """.trimIndent()

    @Test fun parsesActiveAndSkipsUnsupportedOrIdless() {
        val c = PanelConfigParser.parse(activeJson)
        assertThat(c.status).isEqualTo(PanelStatus.ACTIVE)
        assertThat(c.expiresAt).startsWith("2027-01-01")
        assertThat(c.playlists.map { it.id }).containsExactly("a", "b").inOrder()
        assertThat(c.playlists[1].type).isEqualTo(PanelPlaylistType.XTREAM)
        assertThat(c.playlists[0].epgUrl).isNull()
    }

    @Test fun parsesOtherStatuses() {
        assertThat(PanelConfigParser.parse("""{"status":"unknown"}""").status).isEqualTo(PanelStatus.UNKNOWN)
        assertThat(PanelConfigParser.parse("""{"status":"inactive","device_code":"X"}""").status).isEqualTo(PanelStatus.INACTIVE)
        val e = PanelConfigParser.parse("""{"status":"expired","expires_at":"2020-01-01T00:00:00+00:00","playlists":[{"id":"a","type":"m3u","url":"u"}]}""")
        assertThat(e.status).isEqualTo(PanelStatus.EXPIRED)
        assertThat(e.playlists).isEmpty()
    }

    @Test fun rejectsGarbage() {
        for (bad in listOf("", "[]", "null", "{\"status\":\"lol\"}", "<html>")) {
            val r = runCatching { PanelConfigParser.parse(bad) }
            assertThat(r.exceptionOrNull()).isInstanceOf(PanelParseException::class.java)
        }
    }

    @Test fun toStringHidesPassword() {
        assertThat(PanelConfigParser.parse(activeJson).playlists[1].toString()).doesNotContain("p,")
        assertThat(PanelConfigParser.parse(activeJson).playlists[1].toString()).doesNotContain("password")
    }

    @Test fun addsNewPlaylists() {
        val plan = planPanelSync(PanelConfigParser.parse(activeJson), emptyList(), setOf(1L, 2L))
        assertThat(plan.map { (it as PanelSyncAction.Add).playlist.id }).containsExactly("a", "b")
    }

    @Test fun unchangedIsNoOpAndChangedIsUpdate() {
        val c = PanelConfigParser.parse(activeJson)
        val maps = listOf(PanelMapping("a", 10, c.playlists[0].fingerprint()), PanelMapping("b", 11, "stale"))
        val plan = planPanelSync(c, maps, setOf(10L, 11L, 99L))
        assertThat(plan).containsExactly(PanelSyncAction.Update(c.playlists[1], 11L))
    }

    @Test fun reAddsWhenUserDeletedLocalCopy() {
        val c = PanelConfigParser.parse(activeJson)
        val maps = listOf(PanelMapping("a", 10, c.playlists[0].fingerprint()), PanelMapping("b", 11, c.playlists[1].fingerprint()))
        val plan = planPanelSync(c, maps, setOf(11L))
        assertThat(plan).containsExactly(PanelSyncAction.Add(c.playlists[0]))
    }

    @Test fun removesVanishedButNeverTouchesManualProviders() {
        val c = PanelConfigParser.parse(activeJson)
        val maps = listOf(
            PanelMapping("a", 10, c.playlists[0].fingerprint()),
            PanelMapping("b", 11, c.playlists[1].fingerprint()),
            PanelMapping("gone", 12, "x"),
        )
        // 99 = a manually added provider, not in mappings
        val plan = planPanelSync(c, maps, setOf(10L, 11L, 12L, 99L))
        assertThat(plan).containsExactly(PanelSyncAction.Remove("gone", 12L))
        assertThat(plan.none { it is PanelSyncAction.Remove && it.localProviderId == 99L }).isTrue()
    }

    @Test fun inactiveOrExpiredRemovesAllPanelManagedOnly() {
        val maps = listOf(PanelMapping("a", 10, "f"), PanelMapping("b", 11, "g"))
        for (json in listOf("""{"status":"inactive"}""", """{"status":"expired"}""")) {
            val plan = planPanelSync(PanelConfigParser.parse(json), maps, setOf(10L, 11L, 99L))
            assertThat(plan).containsExactly(PanelSyncAction.Remove("a", 10L), PanelSyncAction.Remove("b", 11L))
        }
    }

    @Test fun unknownChangesNothing() {
        val maps = listOf(PanelMapping("a", 10, "f"))
        assertThat(planPanelSync(PanelConfigParser.parse("""{"status":"unknown"}"""), maps, setOf(10L))).isEmpty()
    }

    @Test fun unusableNewPlaylistSkippedButMappedOneKept() {
        val c = PanelConfigParser.parse("""{"status":"active","playlists":[
            {"id":"a","type":"xtream","server":"http://h","username":"u","password":null},
            {"id":"b","type":"m3u","url":null}]}""")
        val plan = planPanelSync(c, listOf(PanelMapping("b", 5, "old")), setOf(5L))
        assertThat(plan).isEmpty()
    }

    @Test fun deviceCodeIsReadableAndStableFormat() {
        val r = SecureRandom()
        repeat(200) {
            val code = PanelDeviceCode.generate(r)
            assertThat(PanelDeviceCode.isValid(code)).isTrue()
            assertThat(code.none { it in "0O1IL" }).isTrue()
        }
        assertThat(PanelDeviceCode.display("A7K29QXD")).isEqualTo("A7K2-9QXD")
        assertThat(PanelDeviceCode.isValid("a7k29qxd")).isFalse()
        assertThat(PanelDeviceCode.isValid(null)).isFalse()
    }
}
