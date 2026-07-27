package com.streamvault.data.manager.recording

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit

class RecordingReconcileWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface RecordingWorkerEntryPoint {
        fun recordingManager(): com.streamvault.domain.manager.RecordingManager
    }

    override suspend fun doWork(): Result {
        val manager = EntryPointAccessors.fromApplication(
            applicationContext,
            RecordingWorkerEntryPoint::class.java
        ).recordingManager()
        return reconciliationWorkResult(manager.reconcileRecordingState(), runAttemptCount)
    }

    companion object {
        private const val PERIODIC_WORK_NAME = "RecordingReconcileWorker"
        private const val ONE_SHOT_WORK_NAME = "RecordingReconcileWorkerOneShot"
        private const val MAX_ONE_SHOT_ATTEMPTS = 3

        fun enqueuePeriodic(context: Context) {
            val request = PeriodicWorkRequestBuilder<RecordingReconcileWorker>(6, TimeUnit.HOURS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun enqueueOneShot(context: Context) {
            val request = OneTimeWorkRequestBuilder<RecordingReconcileWorker>()
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                ONE_SHOT_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}

internal fun reconciliationWorkResult(
    result: com.streamvault.domain.model.Result<Unit>,
    runAttemptCount: Int,
): androidx.work.ListenableWorker.Result = when (result) {
    is com.streamvault.domain.model.Result.Success -> androidx.work.ListenableWorker.Result.success()
    is com.streamvault.domain.model.Result.Error -> {
        if (runAttemptCount >= 2) androidx.work.ListenableWorker.Result.failure()
        else androidx.work.ListenableWorker.Result.retry()
    }
    com.streamvault.domain.model.Result.Loading -> androidx.work.ListenableWorker.Result.failure()
}
