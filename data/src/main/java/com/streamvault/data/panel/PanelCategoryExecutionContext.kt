package com.streamvault.data.panel

import com.streamvault.domain.model.PanelCategoryPolicyRegistry
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/** Captured at workflow entry, not at cache commit. */
class PanelCategoryExecutionContext(
    val token: PanelCategoryPolicyRegistry.Snapshot,
    val artworkEpoch: Long = com.streamvault.domain.model.PanelArtworkPolicyRegistry.epoch(token.providerId)
) : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<PanelCategoryExecutionContext>
}
