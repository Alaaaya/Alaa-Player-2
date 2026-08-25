package com.streamvault.domain.model

/**
 * Controls the application-wide presentation layer without changing providers, categories,
 * channel ordering, playback behavior, persistence, or other IPTV business logic.
 */
enum class AppHomeTheme(
    val storageValue: String
) {
    CLASSIC("classic"),
    ALAA("alaa");

    companion object {
        fun fromStorage(value: String?): AppHomeTheme =
            entries.firstOrNull { it.storageValue.equals(value, ignoreCase = true) } ?: CLASSIC
    }
}
