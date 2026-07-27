package com.streamvault.data.manager.recording

import androidx.work.ListenableWorker
import com.google.common.truth.Truth.assertThat
import com.streamvault.domain.model.Result
import org.junit.Test

class RecordingReconcileWorkerTest {
    @Test
    fun `permanent reconciliation failures stop after bounded retries`() {
        assertThat(reconciliationWorkResult(Result.error("bad row"), 0)).isEqualTo(ListenableWorker.Result.retry())
        assertThat(reconciliationWorkResult(Result.error("bad row"), 2)).isEqualTo(ListenableWorker.Result.failure())
        assertThat(reconciliationWorkResult(Result.Loading, 0)).isEqualTo(ListenableWorker.Result.failure())
    }
}
