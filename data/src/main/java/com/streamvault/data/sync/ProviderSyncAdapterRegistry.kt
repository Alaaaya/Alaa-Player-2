package com.streamvault.data.sync

import com.streamvault.data.provider.toLegacyProvider
import com.streamvault.domain.model.ProviderSnapshot
import com.streamvault.domain.model.ProviderType
import com.streamvault.domain.provider.CapabilityResolution

internal data class FullProviderSyncRequest(
    val snapshot: ProviderSnapshot,
    val force: Boolean,
    val onProgress: ((String) -> Unit)?,
    val trackInitialLiveOnboarding: Boolean,
    val deferProviderStateUntilCatalogCommit: Boolean,
    val afterCatalogApply: suspend () -> Unit
)

internal data class SectionProviderSyncRequest(
    val snapshot: ProviderSnapshot,
    val section: SyncRepairSection,
    val syncReason: XtreamLiveSyncReason,
    val onProgress: ((String) -> Unit)?
)

internal data class ProviderGuideSyncRequest(
    val snapshot: ProviderSnapshot,
    val force: Boolean,
    val now: Long,
    val onProgress: ((String) -> Unit)?
)

internal data class ProviderGuideSyncResult(
    val warnings: List<String>,
    val hasRetryableFailure: Boolean
)

internal fun ProviderSnapshot.toSyncCompatibilityProvider() =
    toLegacyProvider()

internal interface ProviderSyncAdapter {
    val providerType: ProviderType
    suspend fun syncFull(request: FullProviderSyncRequest): SyncOutcome
    suspend fun syncSection(request: SectionProviderSyncRequest): CapabilityResolution<SyncOutcome>
    suspend fun syncGuide(request: ProviderGuideSyncRequest): CapabilityResolution<ProviderGuideSyncResult>
}

internal class ProviderSyncAdapterRegistry(adapters: Collection<ProviderSyncAdapter>) {
    private val adaptersByType: Map<ProviderType, ProviderSyncAdapter>

    init {
        val duplicates = adapters.groupingBy { it.providerType }.eachCount().filterValues { it != 1 }.keys
        require(duplicates.isEmpty()) { "Duplicate provider sync adapters: $duplicates" }
        val missing = ProviderType.entries.toSet() - adapters.mapTo(mutableSetOf()) { it.providerType }
        require(missing.isEmpty()) { "Missing provider sync adapters: $missing" }
        adaptersByType = adapters.associateBy { it.providerType }
    }

    fun resolve(snapshot: ProviderSnapshot): CapabilityResolution<ProviderSyncAdapter> {
        if (snapshot.provider.type != snapshot.configuration.type) {
            return CapabilityResolution.ConfigurationError("Provider/configuration type mismatch")
        }
        return CapabilityResolution.Available(adaptersByType.getValue(snapshot.provider.type))
    }
}

internal class LambdaProviderSyncAdapter(
    override val providerType: ProviderType,
    private val full: suspend (FullProviderSyncRequest) -> SyncOutcome,
    private val section: suspend (SectionProviderSyncRequest) -> CapabilityResolution<SyncOutcome>,
    private val guide: suspend (ProviderGuideSyncRequest) -> CapabilityResolution<ProviderGuideSyncResult>
) : ProviderSyncAdapter {
    override suspend fun syncFull(request: FullProviderSyncRequest): SyncOutcome = full(request)
    override suspend fun syncSection(request: SectionProviderSyncRequest): CapabilityResolution<SyncOutcome> = section(request)
    override suspend fun syncGuide(request: ProviderGuideSyncRequest): CapabilityResolution<ProviderGuideSyncResult> =
        guide(request)
}
