package com.streamvault.data.watchlater

import com.streamvault.data.local.DatabaseTransactionRunner
import com.streamvault.data.repository.PanelAuxiliaryPayloadGuard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.combine
import com.streamvault.data.local.dao.WatchLaterDao
import com.streamvault.data.local.entity.WatchLaterEntity
import com.streamvault.domain.model.ContentType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchLaterRepository @Inject constructor(
    private val dao: WatchLaterDao,
    private val payloadGuard: PanelAuxiliaryPayloadGuard,
    private val transactions: DatabaseTransactionRunner
) {
    fun observeAll(): Flow<List<WatchLaterEntity>> = combine(dao.observeAll(), payloadGuard.changes) { rows, _ ->
        rows.map { payloadGuard.watchLater(it) }
    }.flowOn(Dispatchers.IO)
    fun observeContains(providerId: Long, contentId: Long, type: ContentType): Flow<Boolean> =
        dao.observeContains(providerId, contentId, type)

    suspend fun add(providerId: Long, contentId: Long, type: ContentType, title: String, posterUrl: String?) {
        transactions.inTransaction {
            val ticket = payloadGuard.capture(providerId)
            val row = WatchLaterEntity(providerId = providerId, contentId = contentId, contentType = type, title = title, posterUrl = posterUrl)
            dao.insert(payloadGuard.watchLater(row, ticket))
            payloadGuard.requireCurrent(ticket)
        }
    }

    suspend fun remove(providerId: Long, contentId: Long, type: ContentType) {
        dao.delete(providerId, contentId, type)
    }

    /** Returns the new state (true = now in list). */
    suspend fun toggle(providerId: Long, contentId: Long, type: ContentType, title: String, posterUrl: String?): Boolean =
        if (dao.delete(providerId, contentId, type) > 0) false
        else { add(providerId, contentId, type, title, posterUrl); true }
}
