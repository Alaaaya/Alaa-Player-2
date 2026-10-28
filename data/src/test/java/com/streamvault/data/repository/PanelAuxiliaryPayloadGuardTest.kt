package com.streamvault.data.repository

import com.streamvault.data.local.StreamVaultDatabase
import com.streamvault.domain.model.ContentType
import com.streamvault.domain.model.PlaybackHistory
import com.streamvault.domain.model.PanelCategoryPolicyRegistry as Policies
import com.streamvault.data.local.entity.WatchLaterEntity
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.mock

class PanelAuxiliaryPayloadGuardTest {
    private val guard = PanelAuxiliaryPayloadGuard(mock<StreamVaultDatabase>())
    @Test fun blockedHistoryKeepsProgressButDropsPayload() {
        val id = 830001L
        Policies.block(id)
        val input = PlaybackHistory(contentId=7, providerId=id, contentType=ContentType.MOVIE,
            title="secret", posterUrl="https://secret/poster", streamUrl="https://secret/stream",
            resumePositionMs=123, totalDurationMs=456)
        val output=guard.history(input)
        assertEquals("",output.title); assertNull(output.posterUrl); assertEquals("",output.streamUrl)
        assertEquals(123L,output.resumePositionMs); assertEquals(7L,output.contentId)
    }
    @Test fun blockedWatchLaterKeepsMembershipWithoutPayload() {
        val id=830002L
        Policies.block(id)
        val output=guard.watchLater(WatchLaterEntity(providerId=id,contentId=8,contentType=ContentType.SERIES,
            title="secret",posterUrl="https://secret/poster"))
        assertEquals(8L,output.contentId); assertEquals("",output.title); assertNull(output.posterUrl)
    }
    @Test fun oldTicketCannotAuthorizeNewPublication() {
        val id=830003L
        Policies.markStartupReady()
        val ticket=guard.capture(id)
        Policies.block(id)
        assertThrows(IllegalStateException::class.java) { guard.requireCurrent(ticket) }
    }
    @Test fun staleWatchLaterAddWritesOnlyOpaqueMembership() = kotlinx.coroutines.test.runTest {
        val id = 830005L
        Policies.block(id)
        val dao = mock<com.streamvault.data.local.dao.WatchLaterDao>()
        val transactions = object : com.streamvault.data.local.DatabaseTransactionRunner {
            override suspend fun <T> inTransaction(block: suspend () -> T): T = block()
        }
        val repo = com.streamvault.data.watchlater.WatchLaterRepository(dao, guard, transactions)
        repo.add(id, 9, ContentType.MOVIE, "secret", "https://secret/poster")
        val rows = org.mockito.kotlin.argumentCaptor<WatchLaterEntity>()
        org.mockito.kotlin.verify(dao).insert(rows.capture())
        assertEquals(9L,rows.firstValue.contentId)
        assertEquals("",rows.firstValue.title)
        assertNull(rows.firstValue.posterUrl)
    }
    @Test fun manualProviderPayloadIsPreserved() {
        Policies.markStartupReady()
        val input=PlaybackHistory(contentId=7,providerId=830004,contentType=ContentType.MOVIE,
            title="manual",posterUrl="https://manual/poster",streamUrl="manual")
        assertEquals(input,guard.history(input))
    }
}
