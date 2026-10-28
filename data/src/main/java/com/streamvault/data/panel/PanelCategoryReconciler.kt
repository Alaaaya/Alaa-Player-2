package com.streamvault.data.panel

import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase
import com.streamvault.data.local.StreamVaultDatabase
import com.streamvault.domain.model.PanelCategoryPolicy
import com.streamvault.domain.model.PanelCategoryPolicyRegistry as Policies
import com.streamvault.domain.model.PanelArtworkPolicyRegistry as Artwork
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Durable raw ownership is separate from the legacy numeric catalog keys. No network work occurs here. */
@Singleton
class PanelCategoryReconciler @Inject constructor(private val database: StreamVaultDatabase) {
    private val mutex = Mutex()
    companion object { private var tombstonesRestored = false }
    /** Restore denied URL ownership even when the device has no current panel mappings. */
    suspend fun restoreArtworkTombstones() = mutex.withLock {
        Store(database.openHelper.writableDatabase, null).schema()
    }
    /** Must run on every startup, even with no remaining managed providers, before artwork ready. */
    suspend fun restoreStartup() = mutex.withLock {
        database.withTransaction { Store(database.openHelper.writableDatabase,null).schema() }
    }
    suspend fun reconcile(providerId: Long, policy: PanelCategoryPolicy) = mutex.withLock {
        require(providerId > 0)
        if (Policies.current(providerId) == policy) return@withLock
        Policies.block(providerId)
        Artwork.beginTransition(providerId)
        val db = database.openHelper.writableDatabase
        val store = Store(db, policy)
        database.withTransaction {
            store.schema()
            // Tombstones must precede deleting any URL-bearing row, including legacy cache rows.
            store.seedArtwork(providerId, false)
            val scope = listOf(policy.deviceScope, policy.playlistId, policy.providerGeneration)
                .joinToString("") { "${it.length}:$it" }
            val previous = db.query("SELECT scope FROM panel_cache_identity WHERE provider_id=?", arrayOf<Any>(providerId)).use {
                if (it.moveToFirst()) it.getString(0) else null
            }
            if (previous != scope) {
                // Explicit one-time migration of unprovable legacy cache, or provider replacement.
                store.purgeCatalog(providerId)
                db.execSQL("DELETE FROM panel_episode_parent WHERE provider_id=?", arrayOf<Any>(providerId))
                db.execSQL("DELETE FROM panel_raw_membership WHERE provider_id=?", arrayOf<Any>(providerId))
                db.execSQL("DELETE FROM panel_raw_category WHERE provider_id=?", arrayOf<Any>(providerId))
            }
            db.execSQL("INSERT OR REPLACE INTO panel_cache_identity(provider_id,scope,revision) VALUES(?,?,?)",
                arrayOf<Any>(providerId, scope, policy.revision))
            store.reconcileMembership(providerId)
            store.installTriggers(providerId)
        }
        Policies.attach(providerId, store)
        Policies.install(providerId, policy)
        store.restore(providerId)
        store.seedArtwork(providerId, true)
    }

    suspend fun remove(providerId: Long) = mutex.withLock {
        Policies.block(providerId)
        Artwork.beginTransition(providerId)
        val db = database.openHelper.writableDatabase
        database.withTransaction {
            val store = Store(db, null)
            store.schema()
            store.seedArtwork(providerId, false)
            store.purgeCatalog(providerId)
            listOf("panel_episode_parent", "panel_raw_membership", "panel_raw_category", "panel_cache_identity").forEach {
                db.execSQL("DELETE FROM $it WHERE provider_id=?", arrayOf<Any>(providerId))
            }
            val names = db.query("SELECT name FROM sqlite_master WHERE type='trigger' AND name LIKE 'panel_cat_${providerId}_%'").use { c ->
                buildList { while (c.moveToNext()) add(c.getString(0)) }
            }
            names.forEach { db.execSQL("DROP TRIGGER IF EXISTS `$it`") }
        }
        Policies.remove(providerId)
    }

    internal class Store(private val db: SupportSQLiteDatabase, private val policy: PanelCategoryPolicy?) : Policies.Persistence {
        private val artworkEpochs = mutableMapOf<Long,Long>()
        private fun expectedArtworkEpoch(providerId:Long):Long = artworkEpochs.getOrPut(providerId) { Artwork.epoch(providerId) }
        private val catalogs = listOf(Triple("channels", "live", "stream_id"), Triple("movies", "vod", "stream_id"),
            Triple("series", "series", "series_id"), Triple("episodes", "episode", "episode_id"),
            Triple("channel_import_stage", "live", "stream_id"), Triple("movie_import_stage", "vod", "stream_id"),
            Triple("series_import_stage", "series", "series_id"))
        private fun columns(table: String): Set<String> = db.query("PRAGMA table_info(`$table`)").use { c ->
            buildSet { while (c.moveToNext()) add(c.getString(1)) }
        }
        fun schema() {
            db.execSQL("CREATE TABLE IF NOT EXISTS panel_episode_parent(provider_id INTEGER NOT NULL,series_id INTEGER NOT NULL,episode_id INTEGER NOT NULL,PRIMARY KEY(provider_id,series_id,episode_id))")
            db.execSQL("CREATE TABLE IF NOT EXISTS panel_cache_identity(provider_id INTEGER PRIMARY KEY,scope TEXT NOT NULL,revision TEXT NOT NULL)")
            db.execSQL("CREATE TABLE IF NOT EXISTS panel_raw_category(provider_id INTEGER NOT NULL,media TEXT NOT NULL,raw_id TEXT NOT NULL,cache_key INTEGER NOT NULL,PRIMARY KEY(provider_id,media,raw_id),UNIQUE(provider_id,media,cache_key))")
            db.execSQL("CREATE TABLE IF NOT EXISTS panel_raw_membership(provider_id INTEGER NOT NULL,media TEXT NOT NULL,content_id INTEGER NOT NULL,raw_id TEXT NOT NULL,allowed INTEGER NOT NULL,PRIMARY KEY(provider_id,media,content_id,raw_id))")
            db.execSQL("CREATE TABLE IF NOT EXISTS panel_artwork_owner(provider_id INTEGER NOT NULL,url TEXT NOT NULL,owner_key TEXT NOT NULL,PRIMARY KEY(provider_id,url,owner_key))")
            // Retained URL tombstones survive both provider removal and process death.
            synchronized(PanelCategoryReconciler::class.java) {
                if (!tombstonesRestored) {
                    db.query("SELECT provider_id,url,owner_key FROM panel_artwork_owner").use { c ->
                        while(c.moveToNext()) check(Artwork.record(c.getLong(0), c.getString(1), c.getString(2), false)) { "Artwork restore rejected" }
                    }
                    tombstonesRestored = true
                }
            }
        }
        override fun artwork(providerId: Long, media: String, contentId: Long, urls: List<String?>, expectedEpoch: Long) {
            val epoch=expectedEpoch
            check(epoch == Artwork.epoch(providerId)) { "Stale artwork workflow" }
            urls.filterNotNull().filter { it.isNotBlank() }.forEach { url ->
                val key="$media:$contentId"
                db.execSQL("INSERT OR IGNORE INTO panel_artwork_owner VALUES(?,?,?)",arrayOf<Any>(providerId,url,key))
                check(Artwork.record(providerId,url,key,true,epoch)) { "Artwork generation changed" }
            }
        }
        override fun categoryKey(providerId: Long, media: String, rawId: String): Long {
            db.query("SELECT cache_key FROM panel_raw_category WHERE provider_id=? AND media=? AND raw_id=?", arrayOf<Any>(providerId,media,rawId)).use {
                if (it.moveToFirst()) return it.getLong(0)
            }
            // Canonical positive numbers retain their historic key. All other strings get distinct negative keys.
            val key = rawId.toLongOrNull()?.takeIf { it > 0 && it.toString() == rawId } ?: db.query(
                "SELECT MIN(cache_key) FROM panel_raw_category WHERE provider_id=? AND media=?", arrayOf<Any>(providerId,media)).use {
                it.moveToFirst(); minOf(it.getLong(0), 0L) - 1
            }
            db.execSQL("INSERT INTO panel_raw_category VALUES(?,?,?,?)",arrayOf<Any>(providerId,media,rawId,key))
            return key
        }
        override fun rawCategory(providerId: Long, media: String, key: Long): String? = db.query(
            "SELECT raw_id FROM panel_raw_category WHERE provider_id=? AND media=? AND cache_key=?",arrayOf<Any>(providerId,media,key)).use {
            if(it.moveToFirst()) it.getString(0) else null
        }
        override fun episodeParent(providerId: Long, seriesId: Long, episodeId: Long) {
            db.execSQL("INSERT OR IGNORE INTO panel_episode_parent VALUES(?,?,?)",arrayOf<Any>(providerId,seriesId,episodeId))
        }
        override fun membership(providerId: Long, media: String, contentId: Long, categories: Set<String>, allowed: Boolean) {
            categories.forEach { raw -> db.execSQL("INSERT OR REPLACE INTO panel_raw_membership VALUES(?,?,?,?,?)",arrayOf<Any>(providerId,media,contentId,raw,if(allowed) 1 else 0)) }
            if(!allowed) {
                // Sticky union revokes the version that may have been cached from an earlier category response.
                // Denials never advance the provider epoch; that would invalidate unrelated in-flight DTOs.
                seedArtwork(providerId, false, media, contentId)
                evict(providerId, media, contentId)
                if(Policies.allowsContent(providerId,media,contentId)) seedArtwork(providerId, true)
            }
        }
        fun restore(providerId: Long) {
            db.query("SELECT series_id,episode_id FROM panel_episode_parent WHERE provider_id=?",arrayOf<Any>(providerId)).use { c ->
                while(c.moveToNext()) Policies.restoreEpisodeParent(providerId,c.getLong(0),c.getLong(1))
            }
            val rows = mutableMapOf<Pair<String,Long>,MutableSet<String>>()
            db.query("SELECT media,content_id,raw_id FROM panel_raw_membership WHERE provider_id=?",arrayOf<Any>(providerId)).use { c ->
                while(c.moveToNext()) rows.getOrPut(c.getString(0) to c.getLong(1)){mutableSetOf()} += c.getString(2)
            }
            rows.forEach { (key, ids) -> Policies.restoreMembership(providerId,key.first,key.second,ids) }
        }
        fun reconcileMembership(providerId: Long) {
            // Legacy episode memberships had no trustworthy typed ownership. Never authorize them as episodes.
            // Durable edges created by detail hydration permit rebuilding the full parent union on restart.
            db.execSQL("INSERT OR REPLACE INTO panel_raw_membership SELECT e.provider_id,'episode',e.episode_id,m.raw_id,m.allowed FROM panel_episode_parent e JOIN panel_raw_membership m ON m.provider_id=e.provider_id AND m.media='series' AND m.content_id=e.series_id WHERE e.provider_id=?",arrayOf<Any>(providerId))
            val rows = mutableMapOf<Pair<String,Long>,MutableSet<String>>()
            db.query("SELECT media,content_id,raw_id FROM panel_raw_membership WHERE provider_id=?",arrayOf<Any>(providerId)).use { c ->
                while(c.moveToNext()) rows.getOrPut(c.getString(0) to c.getLong(1)){mutableSetOf()} += c.getString(2)
            }
            rows.forEach { (key, ids) ->
                val allowed = ids.isNotEmpty() && ids.none { it in policy!!.hidden[if(key.first=="episode") "series" else key.first].orEmpty() }
                db.execSQL("UPDATE panel_raw_membership SET allowed=? WHERE provider_id=? AND media=? AND content_id=?",arrayOf<Any>(if(allowed)1 else 0,providerId,key.first,key.second))
                if(!allowed) evict(providerId,key.first,key.second)
            }
            // Unknown rows cannot be restored as visible. This also catches old/stale uncaptured writers.
            catalogs.forEach { (table,media,id) ->
                if("provider_id" in columns(table)) db.execSQL("DELETE FROM `$table` WHERE provider_id=? AND NOT EXISTS (SELECT 1 FROM panel_raw_membership m WHERE m.provider_id=$table.provider_id AND m.media='$media' AND m.content_id=$table.$id AND m.allowed=1)", arrayOf<Any>(providerId))
            }
            listOf("xtream_index_jobs","xtream_category_index_state").forEach { table ->
                if("provider_id" in columns(table)) db.execSQL("DELETE FROM `$table` WHERE provider_id=?",arrayOf<Any>(providerId))
            }
            if("provider_id" in columns("xtream_content_index")) {
                (policy!!.hidden.keys + "episode").forEach { media ->
                    db.execSQL("DELETE FROM xtream_content_index WHERE provider_id=? AND content_type IN (${types(media)}) AND NOT EXISTS(SELECT 1 FROM panel_raw_membership m WHERE m.provider_id=xtream_content_index.provider_id AND m.media='$media' AND CAST(m.content_id AS TEXT)=xtream_content_index.remote_id AND m.allowed=1)",arrayOf<Any>(providerId))
                }
            }
            if("provider_id" in columns("vod_catalog_entries")) {
                db.execSQL("DELETE FROM vod_catalog_entries WHERE provider_id=? AND NOT EXISTS(SELECT 1 FROM panel_raw_membership m JOIN panel_raw_category c ON c.provider_id=m.provider_id AND c.media=m.media AND c.raw_id=m.raw_id WHERE m.provider_id=vod_catalog_entries.provider_id AND c.cache_key=vod_catalog_entries.category_id AND m.allowed=1)",arrayOf<Any>(providerId))
            }
            listOf("movie_category_hydration" to "vod","series_category_hydration" to "series","vod_category_hydration" to "vod").forEach { (table,media) ->
                if("provider_id" in columns(table)) policy!!.hidden[media].orEmpty().forEach { raw ->
                    db.execSQL("DELETE FROM `$table` WHERE provider_id=? AND category_id IN(SELECT cache_key FROM panel_raw_category WHERE provider_id=? AND media=? AND raw_id=?)",arrayOf<Any>(providerId,providerId,media,raw))
                }
            }
            // Category rows are projections of exact raw keys, never numeric policy comparisons.
            if("provider_id" in columns("categories")) {
                policy!!.hidden.forEach { (media, ids) -> ids.forEach { raw ->
                    val type = when(media){"live"->"LIVE";"vod"->"MOVIE";else->"SERIES"}
                    db.execSQL("DELETE FROM categories WHERE provider_id=? AND type=? AND category_id IN (SELECT cache_key FROM panel_raw_category WHERE provider_id=? AND media=? AND raw_id=?)",arrayOf<Any>(providerId,type,providerId,media,raw))
                } }
            }
        }
        private fun evict(providerId: Long, media: String, id: Long) {
            val localIds=mutableSetOf<Long>()
            catalogs.filter { it.second==media }.forEach { (table,_,column) ->
                val cols=columns(table)
                if("provider_id" in cols) {
                    if("id" in cols) db.query("SELECT id FROM `$table` WHERE provider_id=? AND `$column`=?",arrayOf<Any>(providerId,id)).use { c ->
                        while(c.moveToNext()) localIds += c.getLong(0)
                    }
                }
            }
            // Index payload uses remote stable IDs; never retain hidden names/posters in search.
            if("provider_id" in columns("xtream_content_index")) db.execSQL(
                "DELETE FROM xtream_content_index WHERE provider_id=? AND remote_id=? AND content_type IN (${types(media)})",arrayOf<Any>(providerId,id.toString()))
            val ids=localIds + id
            if(media=="live") {
                listOf("programs","channel_epg_mappings").forEach { table ->
                    val cols=columns(table)
                    if("provider_id" in cols && "channel_id" in cols) ids.forEach { local ->
                        db.execSQL("DELETE FROM `$table` WHERE provider_id=? AND channel_id=?",arrayOf<Any>(providerId,local))
                    }
                }
            }
            if("provider_id" in columns("vod_catalog_entries")) ids.forEach { local ->
                db.execSQL("DELETE FROM vod_catalog_entries WHERE provider_id=? AND target_id=? AND item_type IN (${types(media)})",arrayOf<Any>(providerId,local))
            }
            listOf("playback_history","watch_later").forEach { table ->
                val cols=columns(table)
                if("provider_id" in cols && "content_id" in cols) {
                    val updates=listOf("title" to "''","name" to "''","poster_url" to "NULL","stream_url" to "''").filter { it.first in cols }.joinToString(","){"${it.first}=${it.second}"}
                    val typeGuard=if("content_type" in cols) " AND content_type IN (${types(media)})" else ""
                    if(updates.isNotEmpty()) ids.forEach { local -> db.execSQL("UPDATE `$table` SET $updates WHERE provider_id=? AND content_id=?$typeGuard",arrayOf<Any>(providerId,local)) }
                }
            }
            catalogs.filter { it.second==media }.forEach { (table,_,column) ->
                if("provider_id" in columns(table)) db.execSQL("DELETE FROM `$table` WHERE provider_id=? AND `$column`=?",arrayOf<Any>(providerId,id))
            }
        }
        private fun types(media:String):String = when(media) {"live" -> "'LIVE'";"vod" -> "'MOVIE','VOD'";"episode" -> "'SERIES_EPISODE'";else -> "'SERIES'"}
        fun purgeCatalog(providerId: Long) {
            val tables = catalogs.map{it.first} + listOf("categories","category_import_stage","programs","channel_epg_mappings","vod_catalog_entries","movie_category_hydration","series_category_hydration","vod_category_hydration","xtream_content_index","xtream_index_jobs","xtream_category_index_state","sync_metadata")
            tables.forEach { table -> if("provider_id" in columns(table)) db.execSQL("DELETE FROM `$table` WHERE provider_id=?",arrayOf<Any>(providerId)) }
            listOf("playback_history","watch_later").forEach { table ->
                val cols=columns(table)
                if("provider_id" in cols) {
                    val updates=listOf("title" to "''","name" to "''","poster_url" to "NULL","stream_url" to "''").filter{it.first in cols}.joinToString(","){"${it.first}=${it.second}"}
                    if(updates.isNotEmpty()) db.execSQL("UPDATE `$table` SET $updates WHERE provider_id=?",arrayOf<Any>(providerId))
                }
            }
        }
        fun installTriggers(providerId: Long) {
            catalogs.forEach { (table,media,id) -> if("provider_id" in columns(table)) {
                listOf("INSERT","UPDATE").forEach { op ->
                    val name="panel_cat_${providerId}_${table}_${op.lowercase()}"
                    db.execSQL("DROP TRIGGER IF EXISTS `$name`")
                    db.execSQL("CREATE TRIGGER `$name` BEFORE $op ON `$table` WHEN NEW.provider_id=$providerId AND (NOT EXISTS(SELECT 1 FROM panel_raw_membership m WHERE m.provider_id=NEW.provider_id AND m.media='$media' AND m.content_id=NEW.$id AND m.allowed=1) OR EXISTS(SELECT 1 FROM panel_raw_membership m WHERE m.provider_id=NEW.provider_id AND m.media='$media' AND m.content_id=NEW.$id AND m.allowed=0)) BEGIN SELECT RAISE(ABORT,'Category membership denied'); END")
                }
            } }
        }
        fun seedArtwork(providerId: Long, allowed: Boolean, onlyMedia: String?=null, onlyId: Long?=null) {
            db.query("SELECT url,owner_key FROM panel_artwork_owner WHERE provider_id=?",arrayOf<Any>(providerId)).use { c ->
                while(c.moveToNext()) {
                    val key=c.getString(1)
                    val media=key.substringBefore(':')
                    val content=key.substringAfter(':').toLongOrNull() ?: continue
                    if(onlyMedia!=null && media!=onlyMedia || onlyId!=null && content!=onlyId) continue
                    check(Artwork.record(providerId,c.getString(0),key,allowed && Policies.allowsContent(providerId,media,content),if(allowed) expectedArtworkEpoch(providerId) else Artwork.epoch(providerId))) { "Artwork generation changed" }
                }
            }
            (catalogs + listOf(Triple("xtream_content_index","live","remote_id"))).filter { onlyMedia==null || it.second==onlyMedia }.forEach { (table,media,id) ->
                val cols=columns(table)
                if("provider_id" !in cols) return@forEach
                val urls=listOf("logo_url","poster_url","backdrop_url","image_url","cover_url").filter{it in cols}
                if(urls.isEmpty()) return@forEach
                val contentTypeColumn=if(table=="xtream_content_index") ",content_type" else ""
                db.query("SELECT `$id`,${urls.joinToString(",")}$contentTypeColumn FROM `$table` WHERE provider_id=?" + if(onlyId==null) "" else " AND `$id`=$onlyId",arrayOf<Any>(providerId)).use { c ->
                    while(c.moveToNext()) {
                        val content=c.getLong(0)
                        val actualMedia=if(table=="xtream_content_index") when(c.getString(urls.size+1)){"LIVE"->"live";"MOVIE","VOD"->"vod";else->"series"} else media
                        val key="$actualMedia:$content"
                        urls.indices.forEach { index -> if(!c.isNull(index+1)) {
                            val url=c.getString(index+1)
                            db.execSQL("INSERT OR IGNORE INTO panel_artwork_owner VALUES(?,?,?)",arrayOf<Any>(providerId,url,key))
                            check(Artwork.record(providerId,url,key,allowed && Policies.allowsContent(providerId,actualMedia,content),if(allowed) expectedArtworkEpoch(providerId) else Artwork.epoch(providerId))) { "Artwork generation changed" }
                        } }
                    }
                }
            }
        }
    }
}
