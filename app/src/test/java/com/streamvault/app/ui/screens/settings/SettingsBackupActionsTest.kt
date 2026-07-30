package com.streamvault.app.ui.screens.settings

import com.google.common.truth.Truth.assertThat
import com.streamvault.domain.manager.BackupImportPlan
import com.streamvault.domain.manager.BackupImportResult
import com.streamvault.domain.manager.BackupRestoreOutcome
import com.streamvault.domain.usecase.ExportBackup
import com.streamvault.domain.usecase.ImportBackup
import com.streamvault.domain.usecase.ImportBackupResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsBackupActionsTest {

    private val exportBackup: ExportBackup = mock()
    private val importBackup: ImportBackup = mock()

    @Test
    fun partialImport_retainsSourceAndPlanForCheckpointRetry() = runTest(StandardTestDispatcher()) {
        val plan = BackupImportPlan(importPreferences = true, importProviders = false)
        val uiState = MutableStateFlow(
            SettingsUiState(
                pendingBackupUri = "content://backup",
                backupImportPlan = plan
            )
        )
        val actions = SettingsBackupActions(exportBackup, importBackup, uiState)
        whenever(importBackup.confirm(org.mockito.kotlin.any())).thenReturn(
            ImportBackupResult.Success(
                BackupImportResult(
                    outcome = BackupRestoreOutcome.PARTIAL,
                    importedSections = listOf("Providers"),
                    failedSections = listOf("Preferences: unavailable")
                )
            )
        )
        var onSuccessCalled = false

        actions.confirmBackupImport(this, onSuccess = { onSuccessCalled = true })
        advanceUntilIdle()

        assertThat(uiState.value.pendingBackupUri).isEqualTo("content://backup")
        assertThat(uiState.value.backupImportPlan).isEqualTo(plan)
        assertThat(uiState.value.isImportingBackup).isFalse()
        assertThat(onSuccessCalled).isFalse()
    }

    @Test
    fun completeImport_clearsSourceAndRunsFollowUp() = runTest(StandardTestDispatcher()) {
        val uiState = MutableStateFlow(
            SettingsUiState(pendingBackupUri = "content://backup")
        )
        val actions = SettingsBackupActions(exportBackup, importBackup, uiState)
        whenever(importBackup.confirm(org.mockito.kotlin.any())).thenReturn(
            ImportBackupResult.Success(BackupImportResult(outcome = BackupRestoreOutcome.COMPLETE))
        )
        var onSuccessCalled = false

        actions.confirmBackupImport(this, onSuccess = { onSuccessCalled = true })
        advanceUntilIdle()

        assertThat(uiState.value.pendingBackupUri).isNull()
        assertThat(uiState.value.backupImportPlan).isEqualTo(BackupImportPlan())
        assertThat(onSuccessCalled).isTrue()
    }
}
