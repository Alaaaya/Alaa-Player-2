package com.streamvault.app.ui.themes.nour

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.streamvault.app.ui.themes.bespoke.*
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.Series

object NourUi : BespokeThemeUi {
    @Composable override fun Shell(p: ShellParams) = NourShell(p)
    @Composable override fun Dashboard(p: DashboardParams) = NourDashboard(p)
    @Composable override fun LiveTv(p: LiveTvParams) = NourLiveTv(p)
    @Composable override fun Epg(p: EpgParams, modifier: Modifier) = NourEpg(p, modifier)
    @Composable override fun Movies(p: LibraryParams<Movie>) =
        NourLibrary("Movies", "الأفلام", p, { it.id }, { it.name }, { it.posterUrl }, { it.year }, { it.backdropUrl }, { it.plot })
    @Composable override fun Series(p: LibraryParams<Series>) =
        NourLibrary("Series", "المسلسلات", p, { it.id }, { it.name }, { it.posterUrl }, { it.genre }, { it.backdropUrl }, { it.plot })
    @Composable override fun MovieDetail(p: MovieDetailParams) = NourMovieDetail(p)
    @Composable override fun SeriesDetail(p: SeriesDetailParams) = NourSeriesDetail(p)
    @Composable override fun Search(p: SearchParams) = NourSearch(p)
    @Composable override fun Favorites(p: FavoritesParams) = NourFavorites(p)
    @Composable override fun SettingsNav(p: SettingsNavParams) = NourSettingsNav(p)
    @Composable override fun SettingsFrame(navigation: @Composable () -> Unit, content: @Composable () -> Unit) = NourSettingsFrame(navigation, content)
    @Composable override fun PlayerOverlay(p: PlayerOverlayParams) = NourPlayerOverlay(p)
    @Composable override fun LiveChannelList(p: LiveChannelListParams) = NourLiveChannelList(p)
    @Composable override fun LiveChannelInfo(p: LiveChannelInfoParams) = NourLiveChannelInfo(p)
    @Composable override fun ChannelOptions(p: com.streamvault.app.ui.themes.bespoke.ChannelOptionsParams) = NourChannelOptions(p)
}
