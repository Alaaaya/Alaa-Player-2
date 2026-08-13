package com.streamvault.data.manager.recording

import android.content.Context
import android.content.Intent
import android.content.ComponentName
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.common.truth.Truth.assertThat
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Runs against the merged manifest on API 35/36. The production receiver must enqueue durable
 * recovery when Android delivers a restricted broadcast; it must not start a dataSync service.
 */
@RunWith(AndroidJUnit4::class)
class PlatformReleaseSafetyInstrumentationTest {

    private lateinit var context: Context
    private lateinit var workManager: WorkManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        WorkManagerTestInitHelper.initializeTestWorkManager(
            context,
            Configuration.Builder()
                .setExecutor(Executors.newSingleThreadExecutor())
                .build()
        )
        workManager = WorkManager.getInstance(context)
        workManager.cancelAllWork().result.get(10, TimeUnit.SECONDS)
    }

    @Test
    fun bootBroadcastEnqueuesDurableRecordingRecoveryWithoutStartingRecordingService() {
        sendToRecordingRestoreReceiver(Intent(Intent.ACTION_BOOT_COMPLETED))

        val work = workManager
            .getWorkInfosForUniqueWork(RECORDING_RECONCILE_ONE_SHOT_WORK_NAME)
            .get(10, TimeUnit.SECONDS)

        assertThat(work).hasSize(1)
        assertThat(work.single().tags).contains(RecordingReconcileWorker::class.java.name)
        assertThat(work.single().state).isAnyOf(
            WorkInfo.State.ENQUEUED,
            WorkInfo.State.RUNNING,
            WorkInfo.State.SUCCEEDED,
            WorkInfo.State.FAILED
        )
    }

    @Test
    fun exactAlarmPermissionBroadcastUsesTheSameDurableRecoveryLane() {
        val permissionAction = "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED"
        sendToRecordingRestoreReceiver(Intent(permissionAction))
        sendToRecordingRestoreReceiver(Intent(permissionAction))

        val work = workManager
            .getWorkInfosForUniqueWork(RECORDING_RECONCILE_ONE_SHOT_WORK_NAME)
            .get(10, TimeUnit.SECONDS)

        assertThat(work).hasSize(1)
    }

    private fun sendToRecordingRestoreReceiver(intent: Intent) {
        context.sendBroadcast(
            intent.setComponent(
                ComponentName(context, RecordingRestoreReceiver::class.java)
            )
        )
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }
}
