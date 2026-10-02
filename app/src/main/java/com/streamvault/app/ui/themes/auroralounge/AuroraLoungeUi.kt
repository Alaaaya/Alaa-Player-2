package com.streamvault.app.ui.themes.auroralounge

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.streamvault.app.ui.themes.bespoke.*
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.Series

object AuroraLoungeUi : BespokeThemeUi {
    @Composable override fun Shell(p: ShellParams) = AuroraLoungeShell(p)
    @Composable override fun Dashboard(p: DashboardParams) = AuroraLoungeDashboard(p)
    @Composable override fun LiveTv(p: LiveTvParams) = AuroraLoungeLiveTv(p)
    @Composable override fun Epg(p: EpgParams, modifier: Modifier) = AuroraLoungeEpg(p, modifier)
    @Composable override fun Movies(p: LibraryParams<Movie>) =
        AuroraLoungeLibrary("Movies", "الأفلام", p, { it.id }, { it.name }, { it.posterUrl }, { it.year })
    @Composable override fun Series(p: LibraryParams<Series>) =
        AuroraLoungeLibrary("Series", "المسلسلات", p, { it.id }, { it.name }, { it.posterUrl }, { it.genre })
    @Composable override fun MovieDetail(p: MovieDetailParams) = AuroraLoungeMovieDetail(p)
    @Composable override fun SeriesDetail(p: SeriesDetailParams) = AuroraLoungeSeriesDetail(p)
    @Composable override fun Search(p: SearchParams) = AuroraLoungeSearch(p)
    @Composable override fun Favorites(p: FavoritesParams) = AuroraLoungeFavorites(p)
    @Composable override fun SettingsNav(p: SettingsNavParams) = AuroraLoungeSettingsNav(p)
    @Composable override fun SettingsFrame(navigation: @Composable () -> Unit, content: @Composable () -> Unit) = AuroraLoungeSettingsFrame(navigation, content)
    @Composable override fun PlayerOverlay(p: PlayerOverlayParams) = AuroraLoungePlayerOverlay(p)
    @Composable override fun LiveChannelList(p: LiveChannelListParams) = AuroraLoungeLiveChannelList(p)
    @Composable override fun LiveChannelInfo(p: LiveChannelInfoParams) = AuroraLoungeLiveChannelInfo(p)
    @Composable override fun ChannelOptions(p: com.streamvault.app.ui.themes.bespoke.ChannelOptionsParams) = AuroraLoungeChannelOptions(p)
}
