package com.streamvault.app.ui.screens.devicecontrol

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.streamvault.app.devicecontrol.DeviceControlManager
import com.streamvault.app.devicecontrol.DeviceControlResult
import com.streamvault.app.devicecontrol.DeviceControlSyncManager
import com.streamvault.app.ui.design.AppColors
import com.streamvault.app.ui.interaction.TvButton
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DeviceLinkUiState(
    val configured: Boolean = false,
    val tvId: String? = null,
    val pairingCode: String? = null,
    val recoveryCode: String = "",
    val isWorking: Boolean = false,
    val statusMessage: String = "",
    val errorMessage: String? = null,
)

@HiltViewModel
class DeviceLinkViewModel @Inject constructor(
    private val deviceControlManager: DeviceControlManager,
    private val deviceControlSyncManager: DeviceControlSyncManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        DeviceLinkUiState(
            configured = deviceControlManager.isConfigured(),
            tvId = deviceControlManager.currentTvId(),
        )
    )
    val uiState: StateFlow<DeviceLinkUiState> = _uiState.asStateFlow()

    fun requestPairing() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isWorking = true, errorMessage = null, statusMessage = "Requesting a secure pairing code…")
        when (val result = deviceControlManager.requestPairing()) {
            is DeviceControlResult.Success -> _uiState.value = _uiState.value.copy(
                isWorking = false,
                pairingCode = result.value.code,
                statusMessage = "Enter this code in the Alaa control center before it expires.",
            )
            is DeviceControlResult.Failure -> _uiState.value = _uiState.value.copy(isWorking = false, errorMessage = result.message)
        }
    }

    fun updateRecoveryCode(value: String) {
        _uiState.value = _uiState.value.copy(recoveryCode = value)
    }

    fun claimRecoveryCode() = viewModelScope.launch {
        val code = _uiState.value.recoveryCode.trim()
        if (code.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Enter the recovery code from the administrator.")
            return@launch
        }
        _uiState.value = _uiState.value.copy(isWorking = true, errorMessage = null, statusMessage = "Claiming recovery…")
        when (val result = deviceControlManager.claimRecoveryCode(code)) {
            is DeviceControlResult.Success -> _uiState.value = _uiState.value.copy(
                isWorking = false,
                recoveryCode = "",
                statusMessage = "Recovery requested for ${result.value}. Ask the administrator to approve this TV.",
            )
            is DeviceControlResult.Failure -> _uiState.value = _uiState.value.copy(isWorking = false, errorMessage = result.message)
        }
    }

    fun checkApprovalAndSync(onLinked: () -> Unit) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isWorking = true, errorMessage = null, statusMessage = "Checking approval…")
        when (val activation = deviceControlManager.checkAndActivatePairing()) {
            is DeviceControlResult.Failure -> _uiState.value = _uiState.value.copy(isWorking = false, errorMessage = activation.message)
            is DeviceControlResult.Success -> sync(onLinked)
        }
    }

    fun syncNow(onLinked: () -> Unit) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isWorking = true, errorMessage = null, statusMessage = "Syncing approved configuration…")
        sync(onLinked)
    }

    private suspend fun sync(onLinked: () -> Unit) {
        when (val result = deviceControlSyncManager.syncFromControlCenter()) {
            is DeviceControlResult.Success -> {
                _uiState.value = _uiState.value.copy(
                    configured = true,
                    tvId = result.value.tvId,
                    pairingCode = null,
                    isWorking = false,
                    statusMessage = if (result.value.failures.isEmpty()) {
                        "Linked as ${result.value.tvId}. ${result.value.appliedProviderCount} source(s) synchronized."
                    } else {
                        "Linked, but ${result.value.failures.size} source(s) need attention."
                    },
                )
                onLinked()
            }
            is DeviceControlResult.Failure -> _uiState.value = _uiState.value.copy(isWorking = false, errorMessage = result.message)
        }
    }
}

@Composable
fun DeviceLinkScreen(
    onBack: () -> Unit,
    onLinked: () -> Unit,
    viewModel: DeviceLinkViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    Box(
        modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(AppColors.HeroTop, AppColors.HeroBottom))),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 760.dp).fillMaxWidth().padding(32.dp),
            shape = RoundedCornerShape(28.dp),
            colors = SurfaceDefaults.colors(containerColor = AppColors.Surface.copy(alpha = 0.94f)),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 42.dp, vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("ALAA CONTROL CENTER", style = MaterialTheme.typography.labelLarge, color = AppColors.Brand)
                Text("Link this TV", style = MaterialTheme.typography.headlineMedium, color = AppColors.TextPrimary)
                Text(
                    text = if (state.configured) "This TV receives its subscription and sources from the secure control center." else "Link this TV to receive its subscription and sources securely from the control center.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppColors.TextSecondary,
                    textAlign = TextAlign.Center,
                )
                state.tvId?.let { Text("TV ID: $it", style = MaterialTheme.typography.titleLarge, color = AppColors.Brand) }
                state.pairingCode?.let { Text(it, style = MaterialTheme.typography.displaySmall, color = Color.White) }
                if (state.statusMessage.isNotBlank()) Text(state.statusMessage, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary, textAlign = TextAlign.Center)
                state.errorMessage?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFFF7B9C), textAlign = TextAlign.Center) }
                Spacer(Modifier.height(4.dp))
                if (!state.configured) {
                    TvButton(onClick = viewModel::requestPairing, enabled = !state.isWorking, modifier = Modifier.fillMaxWidth()) { Text(if (state.isWorking) "Working…" else "Request pairing code") }
                    if (state.pairingCode != null) {
                        OutlinedTextField(
                            value = state.recoveryCode,
                            onValueChange = viewModel::updateRecoveryCode,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isWorking,
                            label = { androidx.compose.material3.Text("Recovery code after factory reset") },
                            singleLine = true,
                        )
                        TvButton(onClick = viewModel::claimRecoveryCode, enabled = !state.isWorking && state.recoveryCode.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Claim recovery code") }
                    }
                    TvButton(onClick = { viewModel.checkApprovalAndSync(onLinked) }, enabled = !state.isWorking && state.pairingCode != null, modifier = Modifier.fillMaxWidth()) { Text("Check approval and sync") }
                } else {
                    TvButton(onClick = { viewModel.syncNow(onLinked) }, enabled = !state.isWorking, modifier = Modifier.fillMaxWidth()) { Text(if (state.isWorking) "Syncing…" else "Sync configuration") }
                }
                TvButton(onClick = onBack, enabled = !state.isWorking, modifier = Modifier.fillMaxWidth()) { Text("Back") }
            }
        }
    }
}
