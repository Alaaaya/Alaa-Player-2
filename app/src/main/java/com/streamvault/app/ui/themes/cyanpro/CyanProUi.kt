package com.streamvault.app.ui.themes.cyanpro

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.streamvault.app.ui.themes.bespoke.*
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.Series

object CyanProUi : BespokeThemeUi {
    @Composable override fun Shell(p: ShellParams) = CyanProShell(p)
    @Composable override fun Dashboard(p: DashboardParams) = CyanProDashboard(p)
    @Composable override fun LiveTv(p: LiveTvParams) = CyanProLiveTv(p)
    @Composable override fun Epg(p: EpgParams, modifier: Modifier) = CyanProEpg(p, modifier)
    @Composable override fun Movies(p: LibraryParams<Movie>) =
        CyanProLibrary("Movies", "الأفلام", p, { it.id }, { it.name }, { it.posterUrl }, { it.year }, { it.rating }, { it.backdropUrl }, { it.plot }, true, { listOfNotNull(it.variantLabel, it.name, it.containerExtension).joinToString(" ") })
    @Composable override fun Series(p: LibraryParams<Series>) =
        CyanProLibrary("Series", "المسلسلات", p, { it.id }, { it.name }, { it.posterUrl }, { it.genre?.substringBefore(",") }, { it.rating }, { it.backdropUrl }, { it.plot }, false, { it.name })
    @Composable override fun MovieDetail(p: MovieDetailParams) = CyanProMovieDetail(p)
    @Composable override fun SeriesDetail(p: SeriesDetailParams) = CyanProSeriesDetail(p)
    @Composable override fun Search(p: SearchParams) = CyanProSearch(p)
    @Composable override fun Favorites(p: FavoritesParams) = CyanProFavorites(p)
    @Composable override fun SettingsNav(p: SettingsNavParams) = CyanProSettingsNav(p)
    @Composable override fun SettingsFrame(navigation: @Composable () -> Unit, content: @Composable () -> Unit) = CyanProSettingsFrame(navigation, content)
    @Composable override fun PlayerOverlay(p: PlayerOverlayParams) = CyanProPlayerOverlay(p)
    @Composable override fun LiveChannelList(p: LiveChannelListParams) = CyanProLiveChannelList(p)
    @Composable override fun LiveChannelInfo(p: LiveChannelInfoParams) = CyanProLiveChannelInfo(p)
    @Composable override fun ChannelOptions(p: com.streamvault.app.ui.themes.bespoke.ChannelOptionsParams) = CyanProChannelOptions(p)
}
