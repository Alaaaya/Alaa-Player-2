package com.streamvault.domain.model

/**
 * Process-local provider grant generations. Revocation invalidates already resolved streams,
 * even after a provider is authorized again. Untracked/manual streams remain allowed.
 */
object PanelPlaybackAuthorization {
    private val versions = mutableMapOf<Long, Long>()
    private val denied = mutableSetOf<Long>()

    @Synchronized
    fun version(providerId: Long?): Long = providerId?.let { versions[it] } ?: 0L

    @Synchronized
    fun revoke(ids: Set<Long>) {
        ids.forEach { id ->
            versions[id] = (versions[id] ?: 0L) + 1L
            denied.add(id)
        }
    }

    @Synchronized
    fun authorize(ids: Set<Long>) {
        denied.removeAll(ids)
    }

    @Synchronized
    fun allows(streamInfo: StreamInfo): Boolean {
        val id = streamInfo.providerId ?: return true
        return id !in denied && streamInfo.panelAuthorizationVersion == (versions[id] ?: 0L)
    }

    @Synchronized
    internal fun resetForTests() {
        versions.clear()
        denied.clear()
    }
}
