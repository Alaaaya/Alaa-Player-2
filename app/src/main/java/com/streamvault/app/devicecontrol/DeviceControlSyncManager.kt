package com.streamvault.app.devicecontrol

import com.streamvault.domain.model.StalkerAuthMode
import com.streamvault.domain.usecase.JellyfinProviderSetupCommand
import com.streamvault.domain.usecase.M3uProviderSetupCommand
import com.streamvault.domain.usecase.StalkerProviderSetupCommand
import com.streamvault.domain.usecase.ValidateAndAddProvider
import com.streamvault.domain.usecase.ValidateAndAddProviderResult
import com.streamvault.domain.usecase.XtreamProviderSetupCommand
import javax.inject.Inject
import javax.inject.Singleton

data class ControlSyncSummary(
    val tvId: String,
    val displayName: String,
    val appliedProviderCount: Int,
    val failures: List<String>,
)

@Singleton
class DeviceControlSyncManager @Inject constructor(
    private val deviceControlManager: DeviceControlManager,
    private val validateAndAddProvider: ValidateAndAddProvider,
) {
    suspend fun syncFromControlCenter(): DeviceControlResult<ControlSyncSummary> {
        val configuration = when (val result = deviceControlManager.fetchConfiguration()) {
            is DeviceControlResult.Success -> result.value
            is DeviceControlResult.Failure -> return result
        }
        var applied = 0
        val failures = mutableListOf<String>()
        configuration.providers.forEach { remote ->
            val existingProviderId = deviceControlManager.localProviderIdFor(remote.id)
            val outcome = when (remote.kind.lowercase()) {
                "xtream" -> validateAndAddProvider.loginXtream(
                    XtreamProviderSetupCommand(
                        serverUrl = remote.endpointUrl,
                        username = remote.username,
                        password = remote.password,
                        name = remote.displayName,
                        existingProviderId = existingProviderId,
                    )
                )
                "m3u" -> validateAndAddProvider.addM3u(
                    M3uProviderSetupCommand(
                        url = remote.endpointUrl,
                        name = remote.displayName,
                        existingProviderId = existingProviderId,
                    )
                )
                "stalker" -> validateAndAddProvider.loginStalker(
                    StalkerProviderSetupCommand(
                        portalUrl = remote.endpointUrl,
                        macAddress = remote.extra["macAddress"].orEmpty(),
                        name = remote.displayName,
                        authMode = StalkerAuthMode.AUTO,
                        username = remote.username,
                        password = remote.password,
                        existingProviderId = existingProviderId,
                    )
                )
                "jellyfin" -> validateAndAddProvider.loginJellyfin(
                    JellyfinProviderSetupCommand(
                        serverUrl = remote.endpointUrl,
                        username = remote.username,
                        password = remote.password,
                        name = remote.displayName,
                        existingProviderId = existingProviderId,
                    )
                )
                else -> {
                    failures += "${remote.displayName}: unsupported provider type ${remote.kind}"
                    null
                }
            }
            when (outcome) {
                is ValidateAndAddProviderResult.Success -> {
                    deviceControlManager.saveLocalProviderMapping(remote.id, outcome.provider.id)
                    applied += 1
                }
                is ValidateAndAddProviderResult.SavedWithWarning -> {
                    deviceControlManager.saveLocalProviderMapping(remote.id, outcome.provider.id)
                    applied += 1
                    failures += "${remote.displayName}: ${outcome.warning}"
                }
                is ValidateAndAddProviderResult.ValidationError -> failures += "${remote.displayName}: ${outcome.message}"
                is ValidateAndAddProviderResult.TransportConsentRequired -> failures += "${remote.displayName}: transport confirmation is required"
                is ValidateAndAddProviderResult.VerificationInconclusive -> failures += "${remote.displayName}: ${outcome.message}"
                is ValidateAndAddProviderResult.Error -> failures += "${remote.displayName}: ${outcome.message}"
                null -> Unit
            }
        }
        return DeviceControlResult.Success(
            ControlSyncSummary(
                tvId = configuration.tvId,
                displayName = configuration.displayName,
                appliedProviderCount = applied,
                failures = failures,
            )
        )
    }
}
