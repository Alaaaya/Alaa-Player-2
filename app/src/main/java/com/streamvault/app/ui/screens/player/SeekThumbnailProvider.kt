package com.streamvault.app.ui.screens.player

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.LruCache
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@Singleton
class SeekThumbnailProvider @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        private const val FRAME_BUCKET_MS = 10_000L
        private const val MAX_PREVIEW_WIDTH = 480
        /**
         * Maximum time to wait for [MediaMetadataRetriever] to open a remote URL and extract
         * a frame. Without this, [MediaMetadataRetriever.setDataSource] on a slow or live HTTP
         * stream can block a [kotlinx.coroutines.Dispatchers.IO] thread indefinitely.
         */
        private const val RETRIEVER_TIMEOUT_MS = 8_000L
        /** Wedged native extractions tolerated before thumbnails are skipped until one returns. */
        internal const val MAX_STUCK_WORKERS = 2
    }

    private val stuckWorkers = java.util.concurrent.atomic.AtomicInteger(0)

    private val bitmapCache = object : LruCache<String, Bitmap>((Runtime.getRuntime().maxMemory() / 1024L / 24L).toInt()) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount / 1024
    }

    fun bucketPositionMs(positionMs: Long): Long =
        (positionMs.coerceAtLeast(0L) / FRAME_BUCKET_MS) * FRAME_BUCKET_MS

    fun supportsFrameExtraction(streamUrl: String): Boolean {
        if (streamUrl.isBlank()) return false
        val normalized = streamUrl.lowercase()
        val scheme = runCatching { Uri.parse(streamUrl).scheme?.lowercase() }.getOrNull()
        if (normalized.contains(".m3u8") || normalized.contains(".mpd")) return false
        return scheme == null || scheme in setOf("http", "https", "file", "content")
    }

    suspend fun loadFrame(streamUrl: String, positionMs: Long): Bitmap? = withContext(Dispatchers.IO) {
        if (!supportsFrameExtraction(streamUrl)) {
            return@withContext null
        }

        val bucketPosition = bucketPositionMs(positionMs)
        val cacheKey = "$streamUrl#$bucketPosition"
        bitmapCache.get(cacheKey)?.let { return@withContext it }

        // MediaMetadataRetriever calls are native and ignore coroutine cancellation, so a timeout around them
        // does not free the thread. Run each extraction on its own daemon worker, wait with a real deadline,
        // release() the retriever on timeout (unblocks the native socket on most builds) and refuse new work
        // while MAX_STUCK_WORKERS are still wedged, so a stalled server can never exhaust a shared pool.
        if (stuckWorkers.get() >= MAX_STUCK_WORKERS) return@withContext null
        val retriever = MediaMetadataRetriever()
        val result = java.util.concurrent.FutureTask<Bitmap?> {
            try {
                val uri = Uri.parse(streamUrl)
                when (uri.scheme?.lowercase()) {
                    "content", "file" -> retriever.setDataSource(context, uri)
                    else -> retriever.setDataSource(streamUrl, emptyMap())
                }
                val rawBitmap = retriever.getFrameAtTime(bucketPosition * 1000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    ?: retriever.getFrameAtTime(bucketPosition * 1000L)
                rawBitmap?.scaleDown(MAX_PREVIEW_WIDTH)
            } catch (_: Exception) {
                null
            } finally {
                runCatching { retriever.release() }
            }
        }
        val worker = Thread(result, "seek-thumbnail").apply { isDaemon = true }
        worker.start()
        val bitmap = try {
            result.get(RETRIEVER_TIMEOUT_MS, java.util.concurrent.TimeUnit.MILLISECONDS)
        } catch (_: java.util.concurrent.TimeoutException) {
            result.cancel(true)
            runCatching { retriever.release() }
            if (worker.isAlive) {
                stuckWorkers.incrementAndGet()
                Thread({ runCatching { worker.join() }; stuckWorkers.decrementAndGet() }, "seek-thumbnail-reaper")
                    .apply { isDaemon = true }.start()
            }
            null
        } catch (_: Exception) {
            null
        }
        bitmap?.also { bitmapCache.put(cacheKey, it) }
    }

    fun clearCache() {
        bitmapCache.evictAll()
    }

    private fun Bitmap.scaleDown(maxWidth: Int): Bitmap {
        if (width <= maxWidth || width <= 0 || height <= 0) return this
        val scaledHeight = (height * (maxWidth.toFloat() / width.toFloat())).roundToInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(this, maxWidth, scaledHeight, true)
    }
}
