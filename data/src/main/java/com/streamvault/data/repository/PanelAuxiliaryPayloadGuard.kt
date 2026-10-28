package com.streamvault.data.repository

import com.streamvault.data.local.StreamVaultDatabase
import com.streamvault.domain.model.ContentType
import com.streamvault.domain.model.PlaybackHistory
import com.streamvault.data.local.entity.WatchLaterEntity
import com.streamvault.domain.model.PanelCategoryPolicyRegistry as Policies
import com.streamvault.domain.model.PanelArtworkPolicyRegistry as Artwork
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

/**
 * Auxiliary IDs are Room primary keys, NOT Xtream remote IDs. Missing catalog mappings are
 * deliberately payloadless; progress and list identity survive catalog eviction.
 */
@Singleton
class PanelAuxiliaryPayloadGuard @Inject constructor(private val database: StreamVaultDatabase) {
    data class Ticket(val policy: Policies.Snapshot, val artworkEpoch: Long)
    fun capture(providerId: Long) = Ticket(Policies.snapshot(providerId), Artwork.epoch(providerId))

    // Re-project mounted readers even if a pending-only item had no durable Room row to invalidate.
    // Artwork callbacks cover URL denial immediately; polling also covers title-only policy changes.
    val changes = flow {
        while (true) { emit(Unit); delay(250) }
    }

    fun requireCurrent(ticket: Ticket) {
        check(Policies.snapshot(ticket.policy.providerId) == ticket.policy) { "Auxiliary policy epoch changed" }
        check(ticket.artworkEpoch == Artwork.epoch(ticket.policy.providerId)) { "Auxiliary artwork epoch changed" }
    }

    private fun allow(providerId: Long, localId: Long, type: ContentType, poster: String?, ticket: Ticket): Boolean {
        if (!Policies.isCurrent(ticket.policy) || ticket.artworkEpoch != Artwork.epoch(providerId)) return false
        if (!Policies.isTracked(providerId)) return Policies.startupReady()
        val identity = PanelAuxiliaryIdentityResolver.resolve(database.openHelper.writableDatabase, providerId, localId, type) ?: return false
        val remote = identity.remoteId
        val media = identity.media
        if (!Policies.allowsContent(providerId, media, remote)) return false
        // Both the durable reconciler membership and current registry must authorize publication.
        val authorized = database.openHelper.writableDatabase.query(
            "SELECT COUNT(*),MIN(allowed) FROM panel_raw_membership WHERE provider_id=? AND media=? AND content_id=?",
            arrayOf<Any>(providerId, media, remote)
        ).use { it.moveToFirst() && it.getLong(0) > 0 && it.getInt(1) == 1 }
        if (!authorized) return false
        if (media == "episode") {
            // Durable parent edges must agree with the registry. An allowed episode row alone
            // is insufficient after a parent's membership changed or a restart lost its edges.
            val parents = database.openHelper.writableDatabase.query(
                "SELECT series_id FROM panel_episode_parent WHERE provider_id=? AND episode_id=?",
                arrayOf<Any>(providerId, remote)
            ).use { rows -> buildList { while (rows.moveToNext()) add(rows.getLong(0)) } }
            if (parents.isEmpty() || parents.any { parent ->
                !Policies.allowsContent(providerId, "series", parent) ||
                    !database.openHelper.writableDatabase.query(
                        "SELECT COUNT(*),MIN(allowed) FROM panel_raw_membership WHERE provider_id=? AND media='series' AND content_id=?",
                        arrayOf<Any>(providerId, parent)
                    ).use { rows -> rows.moveToFirst() && rows.getLong(0) > 0 && rows.getInt(1) == 1 }
            }) return false
        }
        Policies.recordArtwork(providerId, media, remote, listOf(poster), ticket.artworkEpoch)
        return Policies.isCurrent(ticket.policy) && ticket.artworkEpoch == Artwork.epoch(providerId)
    }

    fun history(value: PlaybackHistory, ticket: Ticket = capture(value.providerId)): PlaybackHistory =
        if (allow(value.providerId, value.contentId, value.contentType, value.posterUrl, ticket)) value
        else value.copy(title = "", posterUrl = null, streamUrl = "", remoteContentId = null, remoteSeriesId = null)

    fun watchLater(value: WatchLaterEntity, ticket: Ticket = capture(value.providerId)): WatchLaterEntity =
        if (allow(value.providerId, value.contentId, value.contentType, value.posterUrl, ticket)) value
        else value.copy(title = "", posterUrl = null)
}


/** Shared typed bridge for reconciliation and auxiliary readers. Never treats a Room ID as remote. */
object PanelAuxiliaryIdentityResolver {
    data class RemoteIdentity(val media: String, val remoteId: Long)

    fun resolve(
        db: androidx.sqlite.db.SupportSQLiteDatabase,
        providerId: Long,
        localId: Long,
        type: ContentType
    ): RemoteIdentity? {
        val (table, column) = when (type) {
            ContentType.LIVE -> "channels" to "stream_id"
            ContentType.MOVIE, ContentType.VOD -> "movies" to "stream_id"
            ContentType.SERIES -> "series" to "series_id"
            ContentType.SERIES_EPISODE -> "episodes" to "episode_id"
        }
        // Pre-migration/minimal fixtures may omit a catalog. Absence is unknown, never a fallback.
        val columns = db.query("PRAGMA table_info(`$table`)").use { rows ->
            buildSet { while (rows.moveToNext()) add(rows.getString(1)) }
        }
        if (!columns.containsAll(setOf("provider_id", "id", column))) return null
        return db.query(
            "SELECT `$column` FROM `$table` WHERE provider_id=? AND id=?",
            arrayOf<Any>(providerId, localId)
        ).use { rows ->
            if (!rows.moveToFirst() || rows.isNull(0)) null
            else RemoteIdentity(Policies.media(type), rows.getLong(0))
        }
    }
}
