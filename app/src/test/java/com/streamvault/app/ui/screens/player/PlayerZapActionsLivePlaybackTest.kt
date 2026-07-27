package com.streamvault.app.ui.screens.player

import com.google.common.truth.Truth.assertThat
import com.streamvault.domain.model.Channel
import com.streamvault.domain.model.ContentType
import org.junit.Test

class PlayerZapActionsLivePlaybackTest {

    @Test
    fun `live playback tracker retries a failed write and deduplicates a successful one`() {
        val tracker = LivePlaybackRecordTracker()
        val key = 9L to 42L

        val firstAttempt = tracker.begin(key)
        assertThat(firstAttempt).isNotNull()
        assertThat(tracker.begin(key)).isNull()

        tracker.complete(key, firstAttempt!!, succeeded = false)
        val retry = tracker.begin(key)
        assertThat(retry).isNotNull()

        tracker.complete(key, retry!!, succeeded = true)
        assertThat(tracker.begin(key)).isNull()
    }

    @Test
    fun `live playback tracker ignores stale completion when a newer channel started`() {
        val tracker = LivePlaybackRecordTracker()
        val firstKey = 9L to 42L
        val secondKey = 9L to 43L

        val firstAttempt = tracker.begin(firstKey)!!
        val secondAttempt = tracker.begin(secondKey)!!
        tracker.complete(firstKey, firstAttempt, succeeded = true)
        tracker.complete(secondKey, secondAttempt, succeeded = true)

        assertThat(tracker.begin(firstKey)).isNotNull()
        assertThat(tracker.begin(secondKey)).isNull()
    }

    @Test
    fun `buildLivePlaybackRecordCandidate uses channel metadata when available`() {
        val channel = Channel(
            id = 42L,
            name = "News HD",
            streamUrl = "https://example.com/live/news.m3u8",
            providerId = 9L
        )

        val candidate = buildLivePlaybackRecordCandidate(
            currentProviderId = 9L,
            currentContentType = ContentType.LIVE,
            currentContentId = 7L,
            currentTitle = "Fallback title",
            currentResolvedPlaybackUrl = "https://example.com/fallback",
            currentStreamUrl = "https://example.com/fallback-raw",
            channel = channel
        )

        assertThat(candidate).isNotNull()
        assertThat(candidate!!.playbackKey).isEqualTo(9L to 42L)
        assertThat(candidate.history.contentId).isEqualTo(42L)
        assertThat(candidate.history.title).isEqualTo("News HD")
        assertThat(candidate.history.streamUrl).isEqualTo("https://example.com/live/news.m3u8")
    }

    @Test
    fun `buildLivePlaybackRecordCandidate falls back to active playback session`() {
        val candidate = buildLivePlaybackRecordCandidate(
            currentProviderId = 11L,
            currentContentType = ContentType.LIVE,
            currentContentId = 77L,
            currentTitle = "Channel 77",
            currentResolvedPlaybackUrl = "https://example.com/resolved",
            currentStreamUrl = "https://example.com/raw",
            channel = null
        )

        assertThat(candidate).isNotNull()
        assertThat(candidate!!.playbackKey).isEqualTo(11L to 77L)
        assertThat(candidate.history.contentId).isEqualTo(77L)
        assertThat(candidate.history.title).isEqualTo("Channel 77")
        assertThat(candidate.history.streamUrl).isEqualTo("https://example.com/resolved")
    }

    @Test
    fun `buildLivePlaybackRecordCandidate rejects non live sessions`() {
        val candidate = buildLivePlaybackRecordCandidate(
            currentProviderId = 11L,
            currentContentType = ContentType.MOVIE,
            currentContentId = 77L,
            currentTitle = "Movie",
            currentResolvedPlaybackUrl = "https://example.com/resolved",
            currentStreamUrl = "https://example.com/raw",
            channel = null
        )

        assertThat(candidate).isNull()
    }
}
