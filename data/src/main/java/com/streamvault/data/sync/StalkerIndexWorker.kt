package com.streamvault.data.sync

import android.content.Context
import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import com.streamvault.data.local.dao.ProviderDao
import com.streamvault.data.local.dao.StalkerIndexJobDao
import com.streamvault.domain.model.ContentType
import com.streamvault.domain.model.ProviderType
import com.streamvault.domain.model.StalkerCatalogMode
import com.streamvault.data.remote.stalker.StalkerTelemetry
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException

internal fun stalkerIndexExistingWorkPolicy(force: Boolean, appendSuccessor: Boolean): ExistingWorkPolicy = when {
    force -> ExistingWorkPolicy.REPLACE
    appendSuccessor -> ExistingWorkPolicy.APPEND_OR_REPLACE
    else -> ExistingWorkPolicy.KEEP
}

class StalkerIndexWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface StalkerIndexWorkerEntryPoint {
        fun providerDao(): ProviderDao
        fun stalkerIndexJobDao(): StalkerIndexJobDao
        fun syncManager(): SyncManager
    }

    override suspend fun doWork(): Result {
        if (applicationContext.isCurrentlyLowOnMemoryForSync()) {
            Log.w(TAG, "Deferring Stalker index work: device low on memory")
            return Result.retry()
        }

        val force = inputData.getBoolean(KEY_FORCE, false)
        val requestedProviderId = inputData.getLong(KEY_PROVIDER_ID, INVALID_PROVIDER_ID)
        val requestedSection = inputData.getString(KEY_SECTION)?.toContentTypeOrNull()

        return try {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                StalkerIndexWorkerEntryPoint::class.java
            )
            val providers = if (requestedProviderId > 0L) {
                entryPoint.providerDao().getById(requestedProviderId)?.let(::listOf).orEmpty()
            } else {
                entryPoint.providerDao().getAllSync()
                    .filter { provider ->
                        provider.isActive &&
                            provider.type == ProviderType.STALKER_PORTAL
                    }
            }

            var sawRetryableFailure = false
            providers
                .filter { provider -> provider.type == ProviderType.STALKER_PORTAL }
                .forEach { provider ->
                    val pendingOneTime = listOf(ContentType.MOVIE, ContentType.SERIES).any { contentType ->
                        entryPoint.stalkerIndexJobDao().get(provider.id, contentType.name)?.state in setOf(
                            com.streamvault.domain.model.StalkerIndexState.QUEUED,
                            com.streamvault.domain.model.StalkerIndexState.RUNNING,
                            com.streamvault.domain.model.StalkerIndexState.RETRY_WAIT,
                            com.streamvault.domain.model.StalkerIndexState.PARTIAL
                        )
                    }
                    if (provider.stalkerCatalogMode != StalkerCatalogMode.BACKGROUND_INDEX && !pendingOneTime) {
                        return@forEach
                    }
                    when (val result = entryPoint.syncManager().processQueuedStalkerIndexJobs(
                        providerId = provider.id,
                        section = requestedSection,
                        force = force,
                        maxCategoriesPerSection = CATEGORY_SLICE_SIZE
                    )) {
                        is com.streamvault.domain.model.Result.Error -> {
                            Log.w(TAG, "Stalker index worker failed for provider ${provider.id}: ${result.message}")
                            if (shouldRetry(result.exception)) {
                                sawRetryableFailure = true
                            }
                        }
                        else -> Unit
                    }
                    listOf(ContentType.MOVIE, ContentType.SERIES).forEach { contentType ->
                        entryPoint.stalkerIndexJobDao().get(provider.id, contentType.name)?.let { job ->
                            StalkerTelemetry.indexProgress(
                                providerId = provider.id,
                                workId = id.toString(),
                                section = contentType.name,
                                state = job.state.name,
                                completedCategories = job.completedCategories,
                                totalCategories = job.totalCategories,
                                indexedRows = job.indexedRows
                            )
                        }
                    }
                }

            if (sawRetryableFailure) Result.retry() else Result.success()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Log.e(TAG, "Stalker index worker failed", error)
            if (shouldRetry(error)) Result.retry() else Result.failure()
        }
    }

    private fun shouldRetry(error: Throwable?): Boolean {
        return when (error) {
            is IOException -> true
            is SQLiteException -> error.message.orEmpty().contains("locked", ignoreCase = true) ||
                error.message.orEmpty().contains("busy", ignoreCase = true)
            else -> false
        }
    }

    companion object {
        private const val TAG = "StalkerIndexWorker"
        private const val KEY_PROVIDER_ID = "provider_id"
        private const val KEY_SECTION = "section"
        private const val KEY_FORCE = "force"
        private const val INVALID_PROVIDER_ID = -1L
        private const val CATEGORY_SLICE_SIZE = 32
        private const val UNIQUE_WORK_PREFIX = "stalker-index-worker-"

        fun enqueue(
            context: Context,
            providerId: Long,
            section: String? = null,
            force: Boolean = false,
            initialDelaySeconds: Long = 0L,
            appendSuccessor: Boolean = false
        ) {
            if (providerId <= 0L) return
            val request = OneTimeWorkRequestBuilder<StalkerIndexWorker>()
                .setInputData(
                    Data.Builder()
                        .putLong(KEY_PROVIDER_ID, providerId)
                        .putBoolean(KEY_FORCE, force)
                        .also { builder ->
                            section?.let { builder.putString(KEY_SECTION, it) }
                        }
                        .build()
                )
                .setConstraints(defaultConstraints())
                .setInitialDelay(initialDelaySeconds.coerceAtLeast(0L), TimeUnit.SECONDS)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                uniqueWorkName(providerId),
                stalkerIndexExistingWorkPolicy(force, appendSuccessor),
                request
            )
        }

        fun cancel(context: Context, providerId: Long) {
            if (providerId <= 0L) return
            val workManager = WorkManager.getInstance(context)
            workManager.cancelUniqueWork(uniqueWorkName(providerId))
            // Names used by older builds had a section suffix.
            workManager.cancelUniqueWork("$UNIQUE_WORK_PREFIX$providerId-")
            workManager.cancelUniqueWork("$UNIQUE_WORK_PREFIX$providerId-MOVIE")
            workManager.cancelUniqueWork("$UNIQUE_WORK_PREFIX$providerId-SERIES")
        }

        private fun defaultConstraints(): Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        private fun uniqueWorkName(providerId: Long): String =
            "$UNIQUE_WORK_PREFIX$providerId"

        private fun String.toContentTypeOrNull(): ContentType? =
            runCatching { ContentType.valueOf(this) }.getOrNull()
    }
}
