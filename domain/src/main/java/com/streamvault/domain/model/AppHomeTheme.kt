package com.streamvault.domain.model

/**
 * Controls the visual composition of the live-TV home screen without changing providers,
 * categories, channel ordering, or playback behavior.
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
