package com.streamvault.app.update

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class InAppUpdatePolicyTest {
    @Test fun `simple release tags compare numerically`() {
        assertThat(compareVersionNamesStatic("v2.1", "2.0")).isGreaterThan(0)
        assertThat(compareVersionNamesStatic("2.10", "2.9")).isGreaterThan(0)
        assertThat(compareVersionNamesStatic("2.0.1", "2.0")).isGreaterThan(0)
        assertThat(compareVersionNamesStatic("2.0", "2.0.0")).isEqualTo(0)
        assertThat(compareVersionNamesStatic("v1.9", "2.0")).isLessThan(0)
    }

    @Test fun `stable updates only prompt for newer stable versions`() {
        fun newer(remote: String) = isRemoteVersionNewerForBuild(
            null, remote, null, 200, "2.0", 0L, AppUpdateChannel.Stable
        )
        assertThat(newer("v2.1")).isTrue()
        assertThat(newer("2.0")).isFalse()
        assertThat(newer("1.9")).isFalse()
        assertThat(newer("2.1-beta")).isFalse()
    }

    @Test fun `checksum assets support plain and sha256sum formats`() {
        val hash = "ab".repeat(32)
        assertThat(parseChecksumFile(hash.uppercase(), "player.apk")).isEqualTo(hash)
        assertThat(parseChecksumFile("$hash  player.apk\n", "player.apk")).isEqualTo(hash)
        assertThat(parseChecksumFile("$hash *player.apk", "player.apk")).isEqualTo(hash)
        assertThat(parseChecksumFile("$hash  other.apk", "player.apk")).isNull()
        assertThat(parseChecksumFile("bad player.apk", "player.apk")).isNull()
    }
}
