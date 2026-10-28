package com.streamvault.app.panel

import com.streamvault.app.ui.screens.player.resolvePlayerPlaybackStreamInfo
import com.streamvault.data.remote.xtream.ProviderPlaybackResolver
import com.streamvault.domain.model.Channel
import com.streamvault.domain.model.ContentType
import com.streamvault.domain.model.PanelCategoryPolicy
import com.streamvault.domain.model.PanelCategoryPolicyRegistry
import com.streamvault.domain.repository.ChannelRepository
import com.streamvault.domain.repository.MovieRepository
import com.streamvault.domain.repository.SeriesRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class PanelPlayerFallbackTest {
    private val provider = 92881L
    private val channels: ChannelRepository = mock()
    private val movies: MovieRepository = mock()
    private val series: SeriesRepository = mock()
    private val resolver: ProviderPlaybackResolver = mock()
    private fun policy(hidden: Set<String>) = PanelCategoryPolicy("device", "playlist", "generation", "revision",
        mapOf("live" to hidden, "vod" to emptySet(), "series" to emptySet()))

    @Before fun before() { PanelCategoryPolicyRegistry.remove(provider) }
    @After fun after() { PanelCategoryPolicyRegistry.remove(provider) }

    private suspend fun resolve() = resolvePlayerPlaybackStreamInfo(
        logicalUrl = "https://media.invalid/live/1.ts", internalContentId = 1,
        providerId = provider, contentType = ContentType.LIVE, currentTitle = "Test",
        currentSeries = null, currentEpisode = null, channelRepository = channels,
        movieRepository = movies, seriesRepository = series, xtreamStreamUrlResolver = resolver)

    @Test fun unknownMembershipCannotUseRawFallbackWhenAnyCategoryHidden(): Unit = runBlocking {
        PanelCategoryPolicyRegistry.install(provider, policy(setOf("007")))
        val result = resolve()
        assertNull(result.streamInfo)
        assertNotNull(result.resolutionFailureMessage)
        verifyNoInteractions(resolver)
    }

    @Test fun blockedProviderCannotUseRawFallback(): Unit = runBlocking {
        PanelCategoryPolicyRegistry.block(provider)
        assertNull(resolve().streamInfo)
        verifyNoInteractions(resolver)
    }

    @Test fun knownVisibleFallbackCarriesCurrentPolicyEpoch(): Unit = runBlocking {
        PanelCategoryPolicyRegistry.install(provider, policy(emptySet()))
        PanelCategoryPolicyRegistry.recordMembership(PanelCategoryPolicyRegistry.snapshot(provider), "live", 123, setOf("7"))
        whenever(channels.getChannel(1)).thenReturn(Channel(id = 1, name = "Test", streamId = 123, providerId = provider))
        val version = PanelCategoryPolicyRegistry.version(provider)
        val info = checkNotNull(resolve().streamInfo)
        assertEquals(provider, info.providerId)
        assertEquals(version, info.panelCategoryPolicyVersion)
        PanelCategoryPolicyRegistry.block(provider)
        assertNotEquals(info.panelCategoryPolicyVersion, PanelCategoryPolicyRegistry.version(provider))
    }
}
