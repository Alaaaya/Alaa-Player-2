package com.streamvault.app.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.google.common.truth.Truth.assertThat
import com.streamvault.data.preferences.PreferencesRepository
import com.streamvault.domain.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.times
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class StartupUpdateViewModelTest {
    private val release = GitHubReleaseInfo("999.0", null, "https://example.org/release",
        "https://example.org/player.apk", "ab".repeat(32), "", null)

    private class Fixture(val model: StartupUpdateViewModel, val installer: AppUpdateInstaller,
        val checker: GitHubReleaseChecker, val download: MutableStateFlow<AppUpdateDownloadState>)

    private fun scenario(enabled: Boolean = true, block: suspend TestScope.(Fixture) -> Unit) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        try {
            val preferences = mock<PreferencesRepository>()
            val checker = mock<GitHubReleaseChecker>()
            val installer = mock<AppUpdateInstaller>()
            val download = MutableStateFlow(AppUpdateDownloadState())
            whenever(preferences.autoCheckAppUpdates).thenReturn(MutableStateFlow(enabled))
            whenever(checker.fetchLatestRelease()).thenReturn(Result.success(release))
            whenever(installer.downloadState).thenReturn(download)
            whenever(installer.refreshState()).thenAnswer { download.value }
            whenever(installer.startDownload(any())).thenReturn(Result.success(Unit))
            whenever(installer.installDownloadedUpdate(anyOrNull())).thenReturn(Result.success(Unit))
            val factory = object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    StartupUpdateViewModel(checker, installer, preferences) as T
            }
            val model = ViewModelProvider(store, factory)[StartupUpdateViewModel::class.java]
            advanceUntilIdle()
            block(Fixture(model, installer, checker, download))
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test fun `disabled automatic checks never call network`() = scenario(false) { f ->
        verify(f.checker, never()).fetchLatestRelease()
        assertThat(f.model.state.value.release).isNull()
    }

    @Test fun `later dismisses available release without downloading`() = scenario { f ->
        assertThat(f.model.state.value.release).isEqualTo(release)
        f.model.later()
        assertThat(f.model.state.value.release).isNull()
        verify(f.installer, never()).startDownload(any())
    }

    @Test fun `repeated update taps start one download and cannot dismiss progress`() = scenario { f ->
        f.model.update()
        f.model.update()
        f.model.later()
        advanceUntilIdle()
        verify(f.installer).startDownload(release)
        assertThat(f.model.state.value.accepted).isTrue()
        assertThat(f.model.state.value.release).isEqualTo(release)
    }

    @Test fun `download failure restores retry and dismiss controls`() = scenario { f ->
        whenever(f.installer.startDownload(release)).thenReturn(Result.error("offline"))
        f.model.update()
        advanceUntilIdle()
        assertThat(f.model.state.value.accepted).isFalse()
        assertThat(f.model.state.value.error).isTrue()
        f.model.later()
        assertThat(f.model.state.value.release).isNull()
    }

    @Test fun `completed download automatically installs using selected checksum`() = scenario { f ->
        f.model.update()
        advanceUntilIdle()
        f.download.value = AppUpdateDownloadState(AppUpdateDownloadStatus.Downloaded, release.versionName)
        advanceUntilIdle()
        verify(f.installer).installDownloadedUpdate(release.downloadSha256)
        assertThat(f.model.state.value.release).isNull()
    }

    @Test fun `existing completed download installs without downloading again`() = scenario { f ->
        f.download.value = AppUpdateDownloadState(AppUpdateDownloadStatus.Downloaded, release.versionName)
        advanceUntilIdle()
        f.model.update()
        advanceUntilIdle()
        verify(f.installer, never()).startDownload(any())
        verify(f.installer).installDownloadedUpdate(release.downloadSha256)
    }

    @Test fun `denied permission makes prompt dismissible instead of relaunching settings`() = scenario { f ->
        f.download.value = AppUpdateDownloadState(AppUpdateDownloadStatus.Downloaded, release.versionName,
            installPermissionRequired = true)
        whenever(f.installer.installDownloadedUpdate(anyOrNull())).thenReturn(Result.error("permission"))
        f.model.update()
        advanceUntilIdle()
        assertThat(f.model.state.value.waitingForPermission).isTrue()
        f.model.resumed()
        advanceUntilIdle()
        assertThat(f.model.state.value.waitingForPermission).isFalse()
        assertThat(f.model.state.value.accepted).isFalse()
        assertThat(f.model.state.value.error).isTrue()
        verify(f.installer).installDownloadedUpdate(release.downloadSha256)
    }

    @Test fun `granted permission resumes installation exactly once`() = scenario { f ->
        f.download.value = AppUpdateDownloadState(AppUpdateDownloadStatus.Downloaded, release.versionName,
            installPermissionRequired = true)
        whenever(f.installer.installDownloadedUpdate(anyOrNull()))
            .thenReturn(Result.error("permission"), Result.success(Unit))
        f.model.update()
        advanceUntilIdle()
        assertThat(f.model.state.value.waitingForPermission).isTrue()
        f.download.value = f.download.value.copy(installPermissionRequired = false)
        advanceUntilIdle()
        verify(f.installer).installDownloadedUpdate(release.downloadSha256)
        f.model.resumed()
        advanceUntilIdle()
        verify(f.installer, times(2)).installDownloadedUpdate(release.downloadSha256)
        assertThat(f.model.state.value.release).isNull()
    }

    @Test fun `download manager failure restores retry without installing`() = scenario { f ->
        f.model.update()
        advanceUntilIdle()
        f.download.value = AppUpdateDownloadState(AppUpdateDownloadStatus.Failed, release.versionName)
        advanceUntilIdle()
        assertThat(f.model.state.value.accepted).isFalse()
        assertThat(f.model.state.value.error).isTrue()
        verify(f.installer, never()).installDownloadedUpdate(anyOrNull())
    }

    @Test fun `unrelated download completion does not install`() = scenario { f ->
        f.model.update()
        advanceUntilIdle()
        f.download.value = AppUpdateDownloadState(AppUpdateDownloadStatus.Downloaded, "1.0")
        advanceUntilIdle()
        verify(f.installer, never()).installDownloadedUpdate(anyOrNull())
    }
}
