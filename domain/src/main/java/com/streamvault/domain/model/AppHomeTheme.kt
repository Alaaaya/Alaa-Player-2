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
    ALAA("alaa", isFixedFoundation = true),
    CINEMATIC("cinematic", isFixedFoundation = false),
    NEON_FUTURE("neon_future", isFixedFoundation = false),
    MINIMAL("minimal", isFixedFoundation = false),
    GLASSMORPHISM("glassmorphism", isFixedFoundation = false),
    STREAMING_PLATFORM("streaming_platform", isFixedFoundation = false),
    PREMIUM_BLACK("premium_black", isFixedFoundation = false),
    BLUE_OCEAN("blue_ocean", isFixedFoundation = false),
    RED_CINEMA("red_cinema", isFixedFoundation = false),
    PURPLE_GALAXY("purple_galaxy", isFixedFoundation = false),
    TECH_DASHBOARD("tech_dashboard", isFixedFoundation = false),
    MODERN_TV("modern_tv", isFixedFoundation = false),
    CARD_STACK("card_stack", isFixedFoundation = false),
    MEDIA_CENTER("media_center", isFixedFoundation = false),
    FUTURISTIC_HUD("futuristic_hud", isFixedFoundation = false),
    SOFT_MODERN("soft_modern", isFixedFoundation = false),
    SPORTS_TV("sports_tv", isFixedFoundation = false),
    DARK_GLASS("dark_glass", isFixedFoundation = false),
    MAGAZINE_MEDIA("magazine_media", isFixedFoundation = false),
    NEXT_GEN_TV("next_gen_tv", isFixedFoundation = false),
    AURORA_LOUNGE("aurora_lounge", isFixedFoundation = false);

    companion object {
        val fixedFoundations: Set<AppHomeTheme> = entries.filterTo(linkedSetOf()) { it.isFixedFoundation }

        fun fromStorage(value: String?): AppHomeTheme =
            entries.firstOrNull { it.storageValue.equals(value, ignoreCase = true) } ?: CLASSIC
    }
}
