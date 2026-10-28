package com.streamvault.data.panel

import com.streamvault.domain.model.PanelCategoryPolicyRegistry
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/** Captured at workflow entry, not at cache commit. */
class PanelCategoryExecutionContext(val token: PanelCategoryPolicyRegistry.Snapshot) : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<PanelCategoryExecutionContext>
}
