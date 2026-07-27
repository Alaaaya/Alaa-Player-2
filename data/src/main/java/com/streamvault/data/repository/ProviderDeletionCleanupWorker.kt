package com.streamvault.data.repository

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.streamvault.data.local.dao.ProviderDeletionCleanupDao
import com.streamvault.data.manager.recording.RecordingAlarmScheduler
import com.streamvault.data.manager.reminder.ProgramReminderAlarmScheduler
import com.streamvault.data.sync.SyncManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CancellationException

class ProviderDeletionCleanupWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    @EntryPoint @InstallIn(SingletonComponent::class)
    interface Entry {
        fun providerDeletionCleanupDao(): ProviderDeletionCleanupDao
        fun recordingAlarmScheduler(): RecordingAlarmScheduler
        fun programReminderAlarmScheduler(): ProgramReminderAlarmScheduler
        fun syncManager(): SyncManager
    }

    override suspend fun doWork(): Result {
        val entry = EntryPointAccessors.fromApplication(applicationContext, Entry::class.java)
        val dao = entry.providerDeletionCleanupDao()
        var failed = false
        dao.getAll().forEach { item ->
            try {
                when (item.action) {
                    RECORDING_ALARM -> entry.recordingAlarmScheduler().cancel(item.targetId)
                    REMINDER_ALARM -> entry.programReminderAlarmScheduler().cancel(item.targetId.toLong())
                    SYNC_RUNTIME -> entry.syncManager().onProviderDeleted(item.providerId)
                }
                dao.delete(item.id)
            } catch (t: Throwable) {
                if (t is CancellationException) throw t
                dao.recordFailure(item.id, t.message ?: t.javaClass.simpleName)
                failed = true
            }
        }
        return if (failed) Result.retry() else Result.success()
    }

    companion object {
        const val RECORDING_ALARM = "RECORDING_ALARM"
        const val REMINDER_ALARM = "REMINDER_ALARM"
        const val SYNC_RUNTIME = "SYNC_RUNTIME"
        private const val WORK_NAME = "ProviderDeletionCleanup"
        fun enqueue(context: Context) = WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME, ExistingWorkPolicy.KEEP, OneTimeWorkRequestBuilder<ProviderDeletionCleanupWorker>().build()
        )
    }
}
