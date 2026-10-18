package com.streamvault.app.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamvault.data.preferences.PreferencesRepository
import com.streamvault.domain.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StartupUpdateState(
    val release: GitHubReleaseInfo? = null,
    val accepted: Boolean = false,
    val error: Boolean = false,
    val waitingForPermission: Boolean = false
)

@HiltViewModel
class StartupUpdateViewModel @Inject constructor(
    checker: GitHubReleaseChecker,
    private val installer: AppUpdateInstaller,
    preferences: PreferencesRepository
) : ViewModel() {
    private val _state = MutableStateFlow(StartupUpdateState())
    val state = _state.asStateFlow()
    val downloadState = installer.downloadState
    private var installing = false

    init {
        viewModelScope.launch {
            if (preferences.autoCheckAppUpdates.first()) {
                val result = checker.fetchLatestRelease()
                if (result is Result.Success && result.data.downloadUrl != null &&
                    isRemoteVersionNewer(result.data.versionCode, result.data.versionName, result.data.publishedAt)) {
                    _state.value = StartupUpdateState(release = result.data)
                }
            }
        }
        viewModelScope.launch {
            downloadState.collect { download ->
                if (_state.value.accepted && download.versionName == _state.value.release?.versionName) {
                    if (download.status == AppUpdateDownloadStatus.Downloaded && !_state.value.waitingForPermission) {
                        install()
                    } else if (download.status == AppUpdateDownloadStatus.Failed) {
                        _state.value = _state.value.copy(accepted = false, error = true)
                    }
                }
            }
        }
    }

    fun later() { if (!_state.value.accepted) _state.value = StartupUpdateState() }

    fun update() {
        val release = _state.value.release ?: return
        if (_state.value.accepted) return
        _state.value = _state.value.copy(accepted = true, error = false)
        viewModelScope.launch {
            val current = installer.refreshState()
            if (current.versionName == release.versionName && current.status == AppUpdateDownloadStatus.Downloaded) {
                install()
            } else if (installer.startDownload(release) is Result.Error) {
                _state.value = _state.value.copy(accepted = false, error = true)
            }
        }
    }

    fun resumed() {
        if (!_state.value.waitingForPermission) return
        viewModelScope.launch {
            val current = installer.refreshState()
            if (!current.installPermissionRequired) {
                _state.value = _state.value.copy(waitingForPermission = false)
                install()
            } else {
                _state.value = _state.value.copy(accepted = false, waitingForPermission = false, error = true)
            }
        }
    }

    private fun install() {
        if (installing) return
        installing = true
        viewModelScope.launch {
            try {
                val result = installer.installDownloadedUpdate(_state.value.release?.downloadSha256)
                _state.value = when {
                    result is Result.Success -> StartupUpdateState()
                    installer.downloadState.value.installPermissionRequired ->
                        _state.value.copy(waitingForPermission = true)
                    else -> _state.value.copy(accepted = false, error = true)
                }
            } finally { installing = false }
        }
    }
}
