package com.streamvault.app.ui.themes.bespoke

/**
 * Contract for themes that replace the entire UI (shell, every browse screen, details, search,
 * settings frame and the player overlay) while reusing the shared ViewModels and data layer.
 *
 * Call sites build one params object and hand it to the active theme; a theme that returns
 * `null` from [bespokeThemeFor] falls back to the existing per-theme branches.
 */

import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.composed
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import com.streamvault.app.ui.model.guideLookupKey
import com.streamvault.app.ui.screens.dashboard.DashboardUiState
import com.streamvault.app.ui.screens.epg.GuideDensity
import com.streamvault.app.ui.screens.favorites.FavoriteSectionUiModel
import com.streamvault.app.ui.screens.favorites.FavoriteUiModel
import com.streamvault.app.ui.screens.favorites.SavedHistoryUiModel
import com.streamvault.app.ui.screens.favorites.SavedLibraryFilter
import com.streamvault.app.ui.screens.favorites.SavedLibraryPreset
import com.streamvault.app.ui.screens.favorites.SavedLibrarySort
import com.streamvault.app.ui.screens.movies.MoviesUiState
import com.streamvault.app.ui.screens.player.PlayerTimeshiftUiState
import com.streamvault.app.ui.screens.player.SeekPreviewState
import com.streamvault.app.ui.screens.player.SleepTimerUiState
import com.streamvault.app.ui.screens.search.SearchTab
import com.streamvault.app.ui.screens.search.SearchUiState
import com.streamvault.app.ui.screens.series.SeriesUiState
import com.streamvault.domain.model.AppHomeTheme
import com.streamvault.domain.model.Category
import com.streamvault.domain.model.Channel
import com.streamvault.domain.model.Episode
import com.streamvault.domain.model.ExternalRatings
import com.streamvault.domain.model.LibraryFilterType
import com.streamvault.domain.model.LibrarySortBy
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.PlaybackHistory
import com.streamvault.domain.model.Program
import com.streamvault.domain.model.RecordingStatus
import com.streamvault.domain.model.Season
import com.streamvault.domain.model.Series
import com.streamvault.player.PlayerEngine

class ShellParams(
    val currentRoute: String,
    val onNavigate: (String) -> Unit,
    val title: String,
    val subtitle: String?,
    val topBarVisible: Boolean,
    val showScreenHeader: Boolean,
    val header: (@Composable ColumnScope.() -> Unit)?,
    val topBarActions: (@Composable RowScope.() -> Unit)?,
    val contentPadding: PaddingValues,
    val modifier: Modifier,
    val content: @Composable ColumnScope.() -> Unit
)

class DashboardParams(
    val uiState: DashboardUiState,
    val recordingChannelIds: Set<Long>,
    val scheduledChannelIds: Set<Long>,
    val onNavigate: (String) -> Unit,
    val onRecentChannelClick: (Channel, Long?) -> Unit,
    val onFavoriteChannelClick: (Channel, Long?) -> Unit,
    val onMovieClick: (Movie) -> Unit,
    val onSeriesClick: (Series) -> Unit,
    val onContinueWatchingItemClick: (PlaybackHistory) -> Unit
)

class LiveTvParams(
    val sourceTitle: String,
    val categories: List<Category>,
    val selectedCategoryId: Long?,
    val categorySearchQuery: String,
    val channelSearchQuery: String,
    val channels: List<Channel>,
    val previewChannel: Channel?,
    val previewPlayerEngine: PlayerEngine?,
    val isPreviewLoading: Boolean,
    val previewErrorMessage: String?,
    val isCategoryLocked: (Category) -> Boolean,
    val isChannelLocked: (Channel) -> Boolean,
    val categoryFocusRequesters: MutableMap<Long, FocusRequester>,
    val channelFocusRequesters: MutableMap<Long, FocusRequester>,
    val previewFocusRequester: FocusRequester,
    val onCategorySearchChange: (String) -> Unit,
    val onChannelSearchChange: (String) -> Unit,
    val onCategoryClick: (Category) -> Unit,
    val onCategoryLongClick: (Category) -> Unit,
    val onChannelClick: (Channel) -> Unit,
    val onChannelLongClick: (Channel) -> Unit,
    val onCategoryFocused: (Category) -> Unit,
    val onChannelFocused: (Channel) -> Unit,
    val onRequestChannelsFromCategory: () -> Boolean,
    val onRequestPreviewFromChannel: () -> Boolean,
    val onRequestChannelsFromPreview: () -> Boolean,
    /** Channel currently being moved (long-press > Move); themes highlight it. UP/DOWN/OK/BACK handled by host. */
    val movingChannelId: Long? = null
) {
    /** Focus requester for a category, registered with the host so its focus routing keeps working. */
    fun categoryRequester(id: Long): FocusRequester = categoryFocusRequesters.getOrPut(id) { FocusRequester() }
    fun channelRequester(id: Long): FocusRequester = channelFocusRequesters.getOrPut(id) { FocusRequester() }
}

class EpgParams(
    val selectedCategoryName: String,
    val previewPlayerEngine: PlayerEngine?,
    val isPreviewLoading: Boolean,
    val focusedChannel: Channel?,
    val focusedProgram: Program?,
    val isRefreshing: Boolean,
    val channels: List<Channel>,
    val favoriteChannelIds: Set<Long>,
    val programsByChannel: Map<String, List<Program>>,
    val guideWindowStart: Long,
    val guideWindowEnd: Long,
    val density: GuideDensity,
    val onOpenCategoryPicker: () -> Unit,
    val onJumpToNow: () -> Unit,
    val onOpenSearch: () -> Unit,
    val onOpenOptions: () -> Unit,
    val onGuideInteract: () -> Unit,
    val onChannelClick: (Channel) -> Unit,
    val onChannelLongClick: (Channel, Program?) -> Unit,
    val onProgramClick: (Channel, Program) -> Unit,
    val onChannelFocused: (Channel, Program?, Boolean) -> Unit,
    val onProgramFocused: (Channel, Program, Boolean) -> Unit,
    val onRequestMoreChannels: () -> Unit
) {
    fun programsFor(channel: Channel): List<Program> =
        channel.guideLookupKey()?.let { programsByChannel[it] }.orEmpty()
}

class LibraryParams<T>(
    val uiState: LibraryState<T>,
    val initialFocusRequester: FocusRequester,
    val isCategoryLocked: (Category) -> Boolean,
    val isItemLocked: (T) -> Boolean,
    val onCategoryClick: (Category) -> Unit,
    val onCategoryLongClick: (Category) -> Unit,
    val onItemClick: (T) -> Unit,
    val onItemLongClick: (T) -> Unit,
    val onQueryChange: (String) -> Unit,
    val onFilterChange: (LibraryFilterType) -> Unit,
    val onSortChange: (LibrarySortBy) -> Unit,
    val onLoadMoreSelected: () -> Unit,
    val onLoadMorePreview: () -> Unit,
    /** Return to the all-genres overview (selectedCategory = null). Never select a real category for "All". */
    val onShowAll: () -> Unit = {}
)

/** The subset of Movies/Series ui state the bespoke libraries need, unified across both. */
class LibraryState<T>(
    val itemsByCategory: Map<String, List<T>>,
    val categoryNames: List<String>,
    val categoryCounts: Map<String, Int>,
    val libraryCount: Int,
    val categories: List<Category>,
    val selectedCategory: String?,
    val selectedCategoryItems: List<T>,
    val canLoadMoreSelectedCategory: Boolean,
    val isLoadingSelectedCategory: Boolean,
    val hasMorePreviewRows: Boolean,
    val searchQuery: String,
    val filteredItems: List<T>,
    val selectedFilter: LibraryFilterType,
    val selectedSort: LibrarySortBy,
    val continueWatching: List<PlaybackHistory>
) {
    fun categoryFor(name: String): Category? = categories.firstOrNull { it.name == name }

    /** True when the overview ("All") is showing, i.e. no genre is selected. */
    val isShowingAll: Boolean get() = selectedCategory == null

    /** What the browse surface should currently show as a flat list. */
    val visibleItems: List<T>
        get() = when {
            searchQuery.isNotBlank() -> filteredItems
            selectedCategory != null -> selectedCategoryItems
            else -> itemsByCategory.values.flatten().distinct()
        }
}

fun MoviesUiState.toLibraryState() = LibraryState(
    moviesByCategory, categoryNames, categoryCounts, libraryCount, categories, selectedCategory,
    selectedCategoryItems, canLoadMoreSelectedCategory, isLoadingSelectedCategory, hasMorePreviewRows,
    searchQuery, filteredMovies, selectedLibraryFilterType, selectedLibrarySortBy, continueWatching
)

fun SeriesUiState.toLibraryState() = LibraryState(
    seriesByCategory, categoryNames, categoryCounts, libraryCount, categories, selectedCategory,
    selectedCategoryItems, canLoadMoreSelectedCategory, isLoadingSelectedCategory, hasMorePreviewRows,
    searchQuery, filteredSeries, selectedLibraryFilterType, selectedLibrarySortBy, continueWatching
)

class MovieDetailParams(
    val movie: Movie,
    val hasResume: Boolean,
    val resumePositionMs: Long,
    val isCasting: Boolean,
    val relatedContent: List<Movie>,
    val onPlay: () -> Unit,
    val onCopyUrl: () -> Unit,
    val onDownload: () -> Unit,
    val onCast: () -> Unit,
    val onToggleFavorite: () -> Unit,
    val onSelectVariant: (Long) -> Unit,
    val onRelatedClick: (Movie) -> Unit,
    val onBack: () -> Unit,
    val onPlayTrailer: (() -> Unit)?
)

class SeriesDetailParams(
    val series: Series,
    val selectedSeason: Season?,
    val resumeEpisode: Episode?,
    val unwatchedEpisodeCount: Int,
    val isCasting: Boolean,
    val externalRatings: ExternalRatings,
    val isLoadingExternalRatings: Boolean,
    val onToggleFavorite: () -> Unit,
    val onSelectVariant: (Long) -> Unit,
    val onSeasonSelected: (Season) -> Unit,
    val onEpisodeClick: (Episode) -> Unit,
    val onResumeClick: (Episode) -> Unit,
    val onCopyEpisodeUrl: (Episode) -> Unit,
    val onDownloadEpisode: (Episode) -> Unit,
    val onCastResumeEpisode: () -> Unit,
    val onCastEpisode: (Episode) -> Unit,
    val onBack: () -> Unit
)

class SearchParams(
    val query: String,
    val selectedTab: SearchTab,
    val recentQueries: List<String>,
    val uiState: SearchUiState,
    val recordingChannelIds: Set<Long>,
    val scheduledChannelIds: Set<Long>,
    val searchFocusRequester: FocusRequester,
    val onQueryChange: (String) -> Unit,
    val onSearch: () -> Unit,
    val onTabSelected: (SearchTab) -> Unit,
    val onRecentQuerySelected: (String) -> Unit,
    val onClearRecentQueries: () -> Unit,
    val onBuildCompleteIndex: () -> Unit,
    val onChannelClick: (Channel) -> Unit,
    val onChannelLongClick: (Channel) -> Unit,
    val onMovieClick: (Movie) -> Unit,
    val onMovieLongClick: (Movie) -> Unit,
    val onSeriesClick: (Series) -> Unit,
    val onSeriesLongClick: (Series) -> Unit,
    val isChannelLocked: (Channel) -> Boolean,
    val isMovieLocked: (Movie) -> Boolean,
    val isSeriesLocked: (Series) -> Boolean
)

class FavoritesParams(
    val sections: List<FavoriteSectionUiModel>,
    val continueWatching: List<SavedHistoryUiModel>,
    val recentLive: List<SavedHistoryUiModel>,
    val selectedPreset: SavedLibraryPreset,
    val selectedFilter: SavedLibraryFilter,
    val selectedSort: SavedLibrarySort,
    val onPresetSelected: (SavedLibraryPreset) -> Unit,
    val onFilterSelected: (SavedLibraryFilter) -> Unit,
    val onSortSelected: (SavedLibrarySort) -> Unit,
    val onItemClick: (FavoriteUiModel) -> Unit,
    val onItemLongClick: (FavoriteUiModel) -> Unit,
    val onHistoryClick: (SavedHistoryUiModel) -> Unit
)

class SettingsNavParams(
    val entries: List<Pair<String, String>>,
    val selectedCategory: Int,
    val focusRequester: FocusRequester,
    val onCategorySelected: (Int) -> Unit
)

class PlayerOverlayParams(
    val visible: Boolean, val title: String, val contentType: String, val isCatchUpPlayback: Boolean,
    val isPlaying: Boolean, val currentProgram: Program?, val currentChannel: Channel?, val currentChannelName: String?,
    val displayChannelNumber: Int, val currentPosition: Long, val duration: Long, val aspectRatioLabel: String,
    val subtitleTrackCount: Int, val liveTranslationAvailable: Boolean, val audioTrackCount: Int, val videoQualityCount: Int,
    val currentRecordingStatus: RecordingStatus?, val isMuted: Boolean, val playbackSpeed: Float, val mediaTitle: String?,
    val sleepTimerUiState: SleepTimerUiState, val timeshiftUiState: PlayerTimeshiftUiState,
    val playButtonFocusRequester: FocusRequester, val quickActionsFocusRequester: FocusRequester, val modifier: Modifier,
    val onClose: () -> Unit, val onTogglePlayPause: () -> Unit, val onSeekBackward: () -> Unit, val onSeekForward: () -> Unit,
    val onRestartProgram: () -> Unit, val onOpenArchive: () -> Unit, val onStartRecording: () -> Unit, val onStopRecording: () -> Unit,
    val onScheduleRecording: () -> Unit, val onScheduleDailyRecording: () -> Unit, val onScheduleWeeklyRecording: () -> Unit,
    val onToggleAspectRatio: () -> Unit, val onOpenSubtitleTracks: () -> Unit, val onOpenAudioTracks: () -> Unit,
    val onOpenVideoTracks: () -> Unit, val onOpenPlaybackSpeed: () -> Unit, val onOpenStopPlaybackTimer: () -> Unit,
    val onOpenIdleStandbyTimer: () -> Unit, val onOpenAudioVideoSync: () -> Unit, val audioVideoSyncEnabled: Boolean,
    val showEpisodesAction: Boolean, val onOpenEpisodes: () -> Unit, val onOpenSplitScreen: () -> Unit,
    val onEnterPictureInPicture: () -> Unit, val onToggleMute: () -> Unit, val isCastConnected: Boolean,
    val onCast: () -> Unit, val onStopCasting: () -> Unit, val onSeekToLiveEdge: () -> Unit,
    val onSeekToPosition: (Long) -> Unit, val onSetScrubbingMode: (Boolean) -> Unit, val seekPreview: SeekPreviewState,
    val onSeekPreviewPositionChanged: (Long?) -> Unit, val onUserInteraction: () -> Unit,
    val nextProgram: Program? = null, val resolutionBadgeLabel: String? = null, val episodeLine: String? = null,
    val onOpenLiveChannels: () -> Unit = {}, val onToggleLiveFavorite: () -> Unit = {}, val onOpenLiveGuide: () -> Unit = {},
    val onNavigateBack: () -> Unit = {}
) {
    val isLive: Boolean get() = contentType == "LIVE"
    val isVod: Boolean get() = contentType != "LIVE" || isCatchUpPlayback
    val displayTitle: String
        get() = currentProgram?.title?.takeIf { isLive } ?: mediaTitle?.takeIf { it.isNotBlank() } ?: title
}

/** In-player channel list (zap list) with recents and a now/next card for the focused channel. */
class LiveChannelListParams(
    val channels: List<Channel>,
    val recentChannels: List<Channel>,
    val currentChannelId: Long,
    val focusRequester: FocusRequester,
    val lastVisitedCategoryName: String?,
    val onOpenLastGroup: () -> Unit,
    val onOpenCategories: () -> Unit,
    val onOpenGuide: () -> Unit,
    val onSelectChannel: (Long) -> Unit,
    val onDismiss: () -> Unit,
    val onInteracted: () -> Unit,
    /** Long-press OK on a row opens the theme's channel options menu. */
    val onChannelLongPress: (Channel) -> Unit = {},
    val movingChannelId: Long? = null
) {
    fun numberOf(channel: Channel): Int =
        channel.number.takeIf { it > 0 } ?: (channels.indexOfFirst { it.id == channel.id } + 1)
}

/** Live zap banner shown on OK: channel identity, now/next with progress and the live action bar. */
class LiveChannelInfoParams(
    val channel: Channel?, val displayChannelNumber: Int, val currentProgram: Program?, val nextProgram: Program?,
    val focusRequester: FocusRequester, val isPlaying: Boolean, val isMuted: Boolean, val resolutionLabel: String?,
    val currentRecordingStatus: RecordingStatus?, val aspectRatioLabel: String, val isDiagnosticsEnabled: Boolean,
    val subtitleTrackCount: Int, val audioTrackCount: Int, val videoQualityCount: Int, val variantCount: Int,
    val isCastConnected: Boolean, val canSeekToLive: Boolean,
    val onDismiss: () -> Unit, val onInteracted: () -> Unit, val onOpenFullEpg: () -> Unit, val onOpenChannelList: () -> Unit,
    val onTogglePlayPause: () -> Unit, val onToggleMute: () -> Unit, val onOpenSubtitleTracks: () -> Unit,
    val onOpenAudioTracks: () -> Unit, val onOpenVideoTracks: () -> Unit, val onOpenVariants: () -> Unit,
    val onToggleAspectRatio: () -> Unit, val onToggleDiagnostics: () -> Unit, val onOpenSplitScreen: () -> Unit,
    val onStartRecording: () -> Unit, val onStopRecording: () -> Unit, val onScheduleRecording: () -> Unit,
    val onRestartProgram: () -> Unit, val onOpenArchive: () -> Unit, val onSeekToLiveEdge: () -> Unit,
    val onEnterPictureInPicture: () -> Unit, val onCast: () -> Unit, val onStopCasting: () -> Unit,
    val onOpenAudioVideoSync: () -> Unit,
    /** Toggle favorite for the playing channel. */
    val onToggleFavorite: () -> Unit = {},
    /** Open the theme's full player settings (controls overlay). */
    val onOpenSettings: () -> Unit = {}
)

/** Short quality badge (4K / FHD / HD / SD) derived from declared quality options or the channel name. */
fun Channel.qualityBadge(): String? {
    val h = (qualityOptions.mapNotNull { it.height } + listOfNotNull(variants.maxOfOrNull { it.attributes.declaredHeight ?: 0 })).maxOrNull() ?: 0
    if (h >= 2160) return "4K"
    if (h >= 1080) return "FHD"
    if (h >= 720) return "HD"
    val n = name.uppercase()
    return when {
        "4K" in n || "UHD" in n -> "4K"
        "FHD" in n || "1080" in n -> "FHD"
        Regex("\\bHD\\b").containsMatchIn(n) -> "HD"
        "SD" in n.split(' ') -> "SD"
        else -> null
    }
}

/** Fraction of a program already aired, clamped to 0..1. */
fun Program.progressAt(now: Long = System.currentTimeMillis()): Float =
    ((now - startTime).toFloat() / (endTime - startTime).coerceAtLeast(1)).coerceIn(0f, 1f)

/**
 * Physical D-pad key that moves toward the layout END (next column). In RTL rows the END column is
 * on the visible left, so physical LEFT must advance there. Pure for unit testing.
 */
fun dpadKeyTowardEnd(rtl: Boolean): Int =
    if (rtl) android.view.KeyEvent.KEYCODE_DPAD_LEFT else android.view.KeyEvent.KEYCODE_DPAD_RIGHT

fun dpadKeyTowardStart(rtl: Boolean): Int =
    if (rtl) android.view.KeyEvent.KEYCODE_DPAD_RIGHT else android.view.KeyEvent.KEYCODE_DPAD_LEFT

/** Layout-aware column hook: runs [handler] when the key toward END (or START) is pressed; consumes only if it returns true. */
fun Modifier.onDpadToward(end: Boolean, handler: () -> Boolean): Modifier = this.then(
    Modifier.composed {
        val rtl = androidx.compose.ui.platform.LocalLayoutDirection.current == androidx.compose.ui.unit.LayoutDirection.Rtl
        val code = if (end) dpadKeyTowardEnd(rtl) else dpadKeyTowardStart(rtl)
        Modifier.onPreviewKeyEvent { e ->
            e.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN && e.nativeKeyEvent.keyCode == code && handler()
        }
    }
)

/**
 * Wraps an inner player panel (VOD More/settings). BACK closes the panel first (consumed here, before the
 * shared PlayerScreen handler hides the whole overlay) and then focus returns to [opener].
 */
@Composable
fun InnerPanelBackScope(onClose: () -> Unit, opener: androidx.compose.ui.focus.FocusRequester, content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.Box(Modifier.onPreviewKeyEvent { e ->
        if (e.nativeKeyEvent.keyCode != android.view.KeyEvent.KEYCODE_BACK) return@onPreviewKeyEvent false
        if (e.nativeKeyEvent.action == android.view.KeyEvent.ACTION_UP) {
            onClose(); runCatching { opener.requestFocus() }
        }
        true
    }) { content() }
}

/** Picks the Arabic label only when the UI language is Arabic (other RTL locales get English, not Arabic). */
@Composable
fun tr(en: String, ar: String): String {
    val lang = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]?.language
    return if (lang == "ar") ar else en
}

interface BespokeThemeUi {
    @Composable fun Shell(p: ShellParams)
    @Composable fun Dashboard(p: DashboardParams)
    @Composable fun LiveTv(p: LiveTvParams)
    @Composable fun Epg(p: EpgParams, modifier: Modifier)
    @Composable fun Movies(p: LibraryParams<Movie>)
    @Composable fun Series(p: LibraryParams<Series>)
    @Composable fun MovieDetail(p: MovieDetailParams)
    @Composable fun SeriesDetail(p: SeriesDetailParams)
    @Composable fun Search(p: SearchParams)
    @Composable fun Favorites(p: FavoritesParams)
    @Composable fun SettingsNav(p: SettingsNavParams)
    @Composable fun SettingsFrame(navigation: @Composable () -> Unit, content: @Composable () -> Unit)
    @Composable fun PlayerOverlay(p: PlayerOverlayParams)
    @Composable fun LiveChannelList(p: LiveChannelListParams)
    @Composable fun LiveChannelInfo(p: LiveChannelInfoParams)
    /** Long-press menu (favorite + move) or, when [ChannelOptionsParams.moving], the move-mode hint. */
    @Composable fun ChannelOptions(p: ChannelOptionsParams)
}

/**
 * Channel options from long-press OK. Menu: toggle favorite, start move. While moving the host
 * handles UP/DOWN (shift), OK (save, persisted per category) and BACK (cancel); the theme only
 * draws the hint. Rendered by the host over the live column or the fullscreen channel list.
 */
class ChannelOptionsParams(
    val channel: Channel,
    val moving: Boolean,
    val focusRequester: FocusRequester,
    val onToggleFavorite: () -> Unit,
    val onStartMove: () -> Unit,
    val onDismiss: () -> Unit
)

private val registry: Map<AppHomeTheme, BespokeThemeUi> by lazy {
    mapOf(
        AppHomeTheme.PURPLE_GALAXY to com.streamvault.app.ui.themes.purplegalaxy.PurpleGalaxyUi,
        AppHomeTheme.TECH_DASHBOARD to com.streamvault.app.ui.themes.techdashboard.TechDashUi,
        AppHomeTheme.MODERN_TV to com.streamvault.app.ui.themes.moderntv.ModernTvUi,
        AppHomeTheme.CARD_STACK to com.streamvault.app.ui.themes.cardstack.CardStackUi,
        AppHomeTheme.MEDIA_CENTER to com.streamvault.app.ui.themes.mediacenter.MediaCenterUi,
        AppHomeTheme.FUTURISTIC_HUD to com.streamvault.app.ui.themes.futuristichud.FuturisticHudUi,
        AppHomeTheme.SOFT_MODERN to com.streamvault.app.ui.themes.softmodern.SoftModernUi,
        AppHomeTheme.SPORTS_TV to com.streamvault.app.ui.themes.sportstv.SportsTvUi,
        AppHomeTheme.DARK_GLASS to com.streamvault.app.ui.themes.darkglass.DarkGlassUi,
        AppHomeTheme.MAGAZINE_MEDIA to com.streamvault.app.ui.themes.magazinemedia.MagazineMediaUi,
        AppHomeTheme.NEXT_GEN_TV to com.streamvault.app.ui.themes.nextgentv.NextGenTvUi,
        AppHomeTheme.AURORA_LOUNGE to com.streamvault.app.ui.themes.auroralounge.AuroraLoungeUi
    )
}

fun bespokeThemeFor(theme: AppHomeTheme): BespokeThemeUi? = registry[theme]

val BespokeThemes: Set<AppHomeTheme> get() = registry.keys
