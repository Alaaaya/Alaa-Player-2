package com.streamvault.app.ui.themes.mediacenter

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.streamvault.app.ui.themes.bespoke.*
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.Series

object MediaCenterUi : BespokeThemeUi {
    @Composable override fun Shell(p: ShellParams) = MediaCenterShell(p)
    @Composable override fun Dashboard(p: DashboardParams) = MediaCenterDashboard(p)
    @Composable override fun LiveTv(p: LiveTvParams) = MediaCenterLiveTv(p)
    @Composable override fun Epg(p: EpgParams, modifier: Modifier) = MediaCenterEpg(p, modifier)
    @Composable override fun Movies(p: LibraryParams<Movie>) =
        MediaCenterLibrary("Movies", "الأفلام", p, { it.id }, { it.name }, { it.posterUrl }, { it.year })
    @Composable override fun Series(p: LibraryParams<Series>) =
        MediaCenterLibrary("Series", "المسلسلات", p, { it.id }, { it.name }, { it.posterUrl }, { it.genre })
    @Composable override fun MovieDetail(p: MovieDetailParams) = MediaCenterMovieDetail(p)
    @Composable override fun SeriesDetail(p: SeriesDetailParams) = MediaCenterSeriesDetail(p)
    @Composable override fun Search(p: SearchParams) = MediaCenterSearch(p)
    @Composable override fun Favorites(p: FavoritesParams) = MediaCenterFavorites(p)
    @Composable override fun SettingsNav(p: SettingsNavParams) = MediaCenterSettingsNav(p)
    @Composable override fun SettingsFrame(navigation: @Composable () -> Unit, content: @Composable () -> Unit) = MediaCenterSettingsFrame(navigation, content)
    @Composable override fun PlayerOverlay(p: PlayerOverlayParams) = MediaCenterPlayerOverlay(p)
    @Composable override fun LiveChannelList(p: LiveChannelListParams) = MediaCenterLiveChannelList(p)
    @Composable override fun LiveChannelInfo(p: LiveChannelInfoParams) = MediaCenterLiveChannelInfo(p)
    @Composable override fun ChannelOptions(p: com.streamvault.app.ui.themes.bespoke.ChannelOptionsParams) = MediaCenterChannelOptions(p)
}
