package com.streamvault.app.update

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GitHubReleaseCheckerTest {
    @Test
    fun parseReleaseAssetSha256DigestAcceptsGitHubDigestShape() {
        val digest = "sha256:ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789"

        assertThat(parseReleaseAssetSha256Digest(digest))
            .isEqualTo("abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789")
    }

    @Test
    fun parseReleaseAssetSha256DigestRejectsUnsupportedDigest() {
        assertThat(parseReleaseAssetSha256Digest("sha1:abcdef")).isNull()
        assertThat(parseReleaseAssetSha256Digest("sha256:not-a-hash")).isNull()
        assertThat(parseReleaseAssetSha256Digest("")).isNull()
    }
}
