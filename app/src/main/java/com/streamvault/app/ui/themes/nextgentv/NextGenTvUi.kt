package com.streamvault.app.ui.themes.nextgentv

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.streamvault.app.ui.themes.bespoke.*
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.Series

object NextGenTvUi : BespokeThemeUi {
    @Composable override fun Shell(p: ShellParams) = NextGenTvShell(p)
    @Composable override fun Dashboard(p: DashboardParams) = NextGenTvDashboard(p)
    @Composable override fun LiveTv(p: LiveTvParams) = NextGenTvLiveTv(p)
    @Composable override fun Epg(p: EpgParams, modifier: Modifier) = NextGenTvEpg(p, modifier)
    @Composable override fun Movies(p: LibraryParams<Movie>) =
        NextGenTvLibrary("Movies", "الأفلام", p, { it.id }, { it.name }, { it.posterUrl }, { it.year })
    @Composable override fun Series(p: LibraryParams<Series>) =
        NextGenTvLibrary("Series", "المسلسلات", p, { it.id }, { it.name }, { it.posterUrl }, { it.genre })
    @Composable override fun MovieDetail(p: MovieDetailParams) = NextGenTvMovieDetail(p)
    @Composable override fun SeriesDetail(p: SeriesDetailParams) = NextGenTvSeriesDetail(p)
    @Composable override fun Search(p: SearchParams) = NextGenTvSearch(p)
    @Composable override fun Favorites(p: FavoritesParams) = NextGenTvFavorites(p)
    @Composable override fun SettingsNav(p: SettingsNavParams) = NextGenTvSettingsNav(p)
    @Composable override fun SettingsFrame(navigation: @Composable () -> Unit, content: @Composable () -> Unit) = NextGenTvSettingsFrame(navigation, content)
    @Composable override fun PlayerOverlay(p: PlayerOverlayParams) = NextGenTvPlayerOverlay(p)
    @Composable override fun LiveChannelList(p: LiveChannelListParams) = NextGenTvLiveChannelList(p)
    @Composable override fun LiveChannelInfo(p: LiveChannelInfoParams) = NextGenTvLiveChannelInfo(p)
    @Composable override fun ChannelOptions(p: com.streamvault.app.ui.themes.bespoke.ChannelOptionsParams) = NextGenTvChannelOptions(p)
}
