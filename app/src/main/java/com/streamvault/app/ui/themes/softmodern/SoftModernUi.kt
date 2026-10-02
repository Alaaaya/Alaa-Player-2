package com.streamvault.app.ui.themes.softmodern

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.streamvault.app.ui.themes.bespoke.*
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.Series

object SoftModernUi : BespokeThemeUi {
    @Composable override fun Shell(p: ShellParams) = SoftModernShell(p)
    @Composable override fun Dashboard(p: DashboardParams) = SoftModernDashboard(p)
    @Composable override fun LiveTv(p: LiveTvParams) = SoftModernLiveTv(p)
    @Composable override fun Epg(p: EpgParams, modifier: Modifier) = SoftModernEpg(p, modifier)
    @Composable override fun Movies(p: LibraryParams<Movie>) =
        SoftModernLibrary("Movies", "الأفلام", p, { it.id }, { it.name }, { it.posterUrl }, { it.year })
    @Composable override fun Series(p: LibraryParams<Series>) =
        SoftModernLibrary("Series", "المسلسلات", p, { it.id }, { it.name }, { it.posterUrl }, { it.genre })
    @Composable override fun MovieDetail(p: MovieDetailParams) = SoftModernMovieDetail(p)
    @Composable override fun SeriesDetail(p: SeriesDetailParams) = SoftModernSeriesDetail(p)
    @Composable override fun Search(p: SearchParams) = SoftModernSearch(p)
    @Composable override fun Favorites(p: FavoritesParams) = SoftModernFavorites(p)
    @Composable override fun SettingsNav(p: SettingsNavParams) = SoftModernSettingsNav(p)
    @Composable override fun SettingsFrame(navigation: @Composable () -> Unit, content: @Composable () -> Unit) = SoftModernSettingsFrame(navigation, content)
    @Composable override fun PlayerOverlay(p: PlayerOverlayParams) = SoftModernPlayerOverlay(p)
    @Composable override fun LiveChannelList(p: LiveChannelListParams) = SoftModernLiveChannelList(p)
    @Composable override fun LiveChannelInfo(p: LiveChannelInfoParams) = SoftModernLiveChannelInfo(p)
    @Composable override fun ChannelOptions(p: com.streamvault.app.ui.themes.bespoke.ChannelOptionsParams) = SoftModernChannelOptions(p)
}
