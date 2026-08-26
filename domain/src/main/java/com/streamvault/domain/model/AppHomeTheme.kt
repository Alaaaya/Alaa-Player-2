package com.streamvault.domain.model

/**
 * Controls the application-wide presentation layer without changing providers, categories,
 * channel ordering, playback behavior, persistence, or other IPTV business logic.
 */
enum class AppHomeTheme(
    val storageValue: String,
    val isFixedFoundation: Boolean
) {
    CLASSIC("classic", isFixedFoundation = true),
    ALAA("alaa", isFixedFoundation = true);

    companion object {
        val fixedFoundations: Set<AppHomeTheme> = entries.filterTo(linkedSetOf()) { it.isFixedFoundation }

        fun fromStorage(value: String?): AppHomeTheme =
            entries.firstOrNull { it.storageValue.equals(value, ignoreCase = true) } ?: CLASSIC
    }
}
