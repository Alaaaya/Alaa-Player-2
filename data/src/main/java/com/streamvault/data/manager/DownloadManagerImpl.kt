package com.streamvault.data.manager

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import com.streamvault.data.local.dao.DownloadDao
import com.streamvault.data.local.entity.DownloadEntity
import com.streamvault.data.preferences.PreferencesRepository
import com.streamvault.domain.model.DownloadContentType
import com.streamvault.domain.model.DownloadItem
import com.streamvault.domain.model.DownloadRequest
import com.streamvault.domain.model.DownloadStatus
import com.streamvault.domain.model.DownloadStorageConfig
import com.streamvault.domain.model.Result
import com.streamvault.domain.repository.DownloadManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.OutputStream
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

@Singleton
class DownloadManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadDao: DownloadDao,
    private val preferencesRepository: PreferencesRepository,
    private val okHttpClient: OkHttpClient,
    private val applicationScope: CoroutineScope
) : DownloadManager {

    private val activeJobs = ConcurrentHashMap<String, Job>()

    override fun observeAllDownloads(): Flow<List<DownloadItem>> {
        return downloadDao.getAll().map { downloads -> downloads.map { it.toDomain() } }
    }

    override fun observeDownload(id: String): Flow<DownloadItem?> {
        return downloadDao.getById(id).map { it?.toDomain() }
    }

    override fun observeStorageState(): Flow<DownloadStorageConfig> {
        return preferencesRepository.downloadTreeUri.combine(observeAllDownloads()) { treeUri, _ ->
            DownloadStorageConfig(
                treeUri = treeUri,
                displayName = treeUri?.substringAfterLast('/')?.ifBlank { treeUri },
                outputDirectory = null,
                availableBytes = null,
                isWritable = !treeUri.isNullOrBlank()
            )
        }
    }

    override suspend fun enqueueDownload(request: DownloadRequest): Result<DownloadItem> {
        return runCatching {
            val entity = DownloadEntity.fromRequest(
                request = request,
                outputUri = null,
                outputDisplayPath = null
            )
            downloadDao.insert(entity)
            activeJobs[entity.id] = applicationScope.launch(Dispatchers.IO) {
                captureDownload(entity)
            }
            entity.toDomain()
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.error("Failed to enqueue download", it) }
        )
    }

    override suspend fun cancelDownload(id: String): Result<Unit> {
        return runCatching {
            activeJobs.remove(id)?.cancel()
            downloadDao.getByIdOnce(id)?.let { entity ->
                downloadDao.update(entity.copy(status = DownloadStatus.CANCELLED))
            }
        }.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.error("Failed to cancel download", it) }
        )
    }

    override suspend fun deleteDownload(id: String): Result<Unit> {
        return runCatching {
            activeJobs.remove(id)?.cancel()
            downloadDao.getByIdOnce(id)?.let { deleteOutput(it) }
            downloadDao.deleteById(id)
        }.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.error("Failed to delete download", it) }
        )
    }

    override suspend fun updateStorageConfig(
        treeUri: String?,
        displayName: String?
    ): Result<DownloadStorageConfig> {
        return runCatching {
            preferencesRepository.setDownloadTreeUri(treeUri)
            DownloadStorageConfig(
                treeUri = treeUri,
                displayName = displayName,
                outputDirectory = null,
                availableBytes = null,
                isWritable = !treeUri.isNullOrBlank()
            )
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.error("Failed to update download folder", it) }
        )
    }

    private suspend fun captureDownload(initial: DownloadEntity) {
        var current = initial.copy(status = DownloadStatus.DOWNLOADING)
        downloadDao.update(current)

        try {
            okHttpClient.newCall(Request.Builder().url(initial.streamUrl).get().build()).execute().use { response ->
                if (!response.isSuccessful) error("HTTP ${response.code}")
                val body = response.body ?: error("Empty response body")
                val totalBytes = body.contentLength().takeIf { it > 0L }
                val target = createOutputTarget(initial, response.header("Content-Type"))
                var bytesWritten = 0L
                var lastProgressUpdate = 0L

                target.output.use { output ->
                    body.byteStream().use { input ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        while (true) {
                            coroutineContext.ensureActive()
                            val read = input.read(buffer)
                            if (read == -1) break
                            output.write(buffer, 0, read)
                            bytesWritten += read.toLong()

                            if (bytesWritten - lastProgressUpdate >= PROGRESS_UPDATE_BYTES) {
                                current = current.copy(
                                    bytesWritten = bytesWritten,
                                    totalBytes = totalBytes,
                                    status = DownloadStatus.DOWNLOADING
                                )
                                downloadDao.update(current)
                                lastProgressUpdate = bytesWritten
                            }
                        }
                    }
                }

                downloadDao.update(
                    current.copy(
                        outputUri = target.uri.toString(),
                        outputDisplayPath = target.displayPath,
                        status = DownloadStatus.COMPLETED,
                        bytesWritten = bytesWritten,
                        totalBytes = totalBytes ?: bytesWritten,
                        completedAt = System.currentTimeMillis(),
                        failureReason = null
                    )
                )
            }
        } catch (cancelled: CancellationException) {
            downloadDao.getByIdOnce(initial.id)?.let { entity ->
                downloadDao.update(entity.copy(status = DownloadStatus.CANCELLED))
            }
            throw cancelled
        } catch (error: Throwable) {
            downloadDao.getByIdOnce(initial.id)?.let { entity ->
                downloadDao.update(
                    entity.copy(
                        status = DownloadStatus.FAILED,
                        failureReason = error.message ?: error::class.java.simpleName
                    )
                )
            }
        } finally {
            activeJobs.remove(initial.id)
        }
    }

    private suspend fun createOutputTarget(
        entity: DownloadEntity,
        contentTypeHeader: String?
    ): OutputTarget = withContext(Dispatchers.IO) {
        val mimeType = contentTypeHeader
            ?.substringBefore(';')
            ?.trim()
            ?.takeIf { it.startsWith("video/") }
            ?: "video/mp4"
        val fileName = buildFileName(entity, mimeType)
        val treeUri = preferencesRepository.downloadTreeUri.first()

        if (!treeUri.isNullOrBlank()) {
            val tree = DocumentFile.fromTreeUri(context, Uri.parse(treeUri))
            val file = tree?.createFile(mimeType, fileName)
                ?: error("Could not create download file in selected folder")
            val output = context.contentResolver.openOutputStream(file.uri)
                ?: error("Could not open selected download file")
            return@withContext OutputTarget(
                uri = file.uri,
                displayPath = file.name ?: fileName,
                output = output
            )
        }

        val directory = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir,
            "StreamVault"
        ).apply { mkdirs() }
        val file = uniqueFile(directory, fileName)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        OutputTarget(
            uri = uri,
            displayPath = file.absolutePath,
            output = file.outputStream()
        )
    }

    private fun deleteOutput(entity: DownloadEntity) {
        val displayPath = entity.outputDisplayPath
        if (!displayPath.isNullOrBlank()) {
            runCatching { File(displayPath).delete() }
        }

        val outputUri = entity.outputUri ?: return
        runCatching {
            context.contentResolver.delete(Uri.parse(outputUri), null, null)
        }
    }

    private fun buildFileName(entity: DownloadEntity, mimeType: String): String {
        val extension = extensionFromUrl(entity.streamUrl)
            ?: MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
            ?: "mp4"
        val prefix = when (entity.contentType) {
            DownloadContentType.MOVIE -> entity.contentName
            DownloadContentType.SERIES_EPISODE -> listOfNotNull(
                entity.contentName,
                entity.seasonNumber?.let { "S$it" },
                entity.episodeNumber?.let { "E$it" }
            ).joinToString(" ")
        }
        val safeName = prefix
            .replace(Regex("[\\\\/:*?\"<>|]+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .ifBlank { entity.id }
        return "$safeName.$extension"
    }

    private fun extensionFromUrl(url: String): String? {
        val segment = Uri.parse(url).lastPathSegment ?: return null
        val extension = segment.substringAfterLast('.', missingDelimiterValue = "")
            .lowercase()
            .takeIf { it.length in 2..5 }
        return extension?.takeIf { ext -> ext.all { it.isLetterOrDigit() } }
    }

    private fun uniqueFile(directory: File, fileName: String): File {
        val baseName = fileName.substringBeforeLast('.', fileName)
        val extension = fileName.substringAfterLast('.', missingDelimiterValue = "")
        var candidate = File(directory, fileName)
        var index = 2
        while (candidate.exists()) {
            val suffix = if (extension.isBlank()) " ($index)" else " ($index).$extension"
            candidate = File(directory, "$baseName$suffix")
            index += 1
        }
        return candidate
    }

    private data class OutputTarget(
        val uri: Uri,
        val displayPath: String,
        val output: OutputStream
    )

    private companion object {
        const val DEFAULT_BUFFER_SIZE = 64 * 1024
        const val PROGRESS_UPDATE_BYTES = 512 * 1024
    }
}
