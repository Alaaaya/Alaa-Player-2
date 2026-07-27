package com.streamvault.app.update

internal object AppUpdateCheckPolicy {
    private const val SUCCESS_INTERVAL_MS = 24L * 60L * 60L * 1000L
    private const val FAILURE_BACKOFF_MS = 15L * 60L * 1000L

    fun shouldAutoCheck(
        now: Long,
        lastSuccessfulCheckAt: Long?,
        lastFailedCheckAt: Long?,
    ): Boolean {
        if (lastSuccessfulCheckAt != null && now - lastSuccessfulCheckAt < SUCCESS_INTERVAL_MS) {
            return false
        }
        return lastFailedCheckAt == null || now - lastFailedCheckAt >= FAILURE_BACKOFF_MS
    }
}
