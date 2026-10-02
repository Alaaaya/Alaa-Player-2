package com.streamvault.app.ui.themes.sportstv

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.streamvault.app.ui.themes.bespoke.*
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.Series

object SportsTvUi : BespokeThemeUi {
    @Composable override fun Shell(p: ShellParams) = SportsTvShell(p)
    @Composable override fun Dashboard(p: DashboardParams) = SportsTvDashboard(p)
    @Composable override fun LiveTv(p: LiveTvParams) = SportsTvLiveTv(p)
    @Composable override fun Epg(p: EpgParams, modifier: Modifier) = SportsTvEpg(p, modifier)
    @Composable override fun Movies(p: LibraryParams<Movie>) =
        SportsTvLibrary("Movies", "الأفلام", p, { it.id }, { it.name }, { it.posterUrl }, { it.year })
    @Composable override fun Series(p: LibraryParams<Series>) =
        SportsTvLibrary("Series", "المسلسلات", p, { it.id }, { it.name }, { it.posterUrl }, { it.genre })
    @Composable override fun MovieDetail(p: MovieDetailParams) = SportsTvMovieDetail(p)
    @Composable override fun SeriesDetail(p: SeriesDetailParams) = SportsTvSeriesDetail(p)
    @Composable override fun Search(p: SearchParams) = SportsTvSearch(p)
    @Composable override fun Favorites(p: FavoritesParams) = SportsTvFavorites(p)
    @Composable override fun SettingsNav(p: SettingsNavParams) = SportsTvSettingsNav(p)
    @Composable override fun SettingsFrame(navigation: @Composable () -> Unit, content: @Composable () -> Unit) = SportsTvSettingsFrame(navigation, content)
    @Composable override fun PlayerOverlay(p: PlayerOverlayParams) = SportsTvPlayerOverlay(p)
    @Composable override fun LiveChannelList(p: LiveChannelListParams) = SportsTvLiveChannelList(p)
    @Composable override fun LiveChannelInfo(p: LiveChannelInfoParams) = SportsTvLiveChannelInfo(p)
    @Composable override fun ChannelOptions(p: com.streamvault.app.ui.themes.bespoke.ChannelOptionsParams) = SportsTvChannelOptions(p)
}
