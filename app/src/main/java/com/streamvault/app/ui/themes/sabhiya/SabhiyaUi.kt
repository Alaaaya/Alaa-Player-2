package com.streamvault.app.ui.themes.sabhiya

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.streamvault.app.ui.themes.bespoke.*
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.Series

object SabhiyaUi : BespokeThemeUi {
    @Composable override fun Shell(p: ShellParams) = SbsShell(p)
    @Composable override fun Dashboard(p: DashboardParams) = SbsHome(p)
    @Composable override fun LiveTv(p: LiveTvParams) = SbsLive(p)
    @Composable override fun Epg(p: EpgParams, modifier: Modifier) = SabhiyaEpg(p, modifier)
    @Composable override fun Movies(p: LibraryParams<Movie>) =
        SbFreshLibrary("Movies", "الأفلام", p, { it.id }, { it.name }, { it.posterUrl }, { it.backdropUrl }, { it.year }, { it.rating }, { it.plot }, { sbQuality(it.name) }, true)
    @Composable override fun Series(p: LibraryParams<Series>) =
        SbFreshLibrary("Series", "المسلسلات", p, { it.id }, { it.name }, { it.posterUrl }, { it.backdropUrl }, { it.releaseDate?.take(4) }, { it.rating }, { it.plot }, { s -> s.seasons.size.takeIf { it > 0 }?.let { "S$it" } }, false)
    @Composable override fun MovieDetail(p: MovieDetailParams) = SbFreshMovieDetail(p)
    @Composable override fun SeriesDetail(p: SeriesDetailParams) = SbFreshSeriesDetail(p)
    @Composable override fun Search(p: SearchParams) = SbFreshSearch(p)
    @Composable override fun Favorites(p: FavoritesParams) = SbFreshFavorites(p)
    @Composable override fun SettingsNav(p: SettingsNavParams) = SbFreshSettingsNav(p)
    @Composable override fun SettingsFrame(navigation: @Composable () -> Unit, content: @Composable () -> Unit) = SbFreshSettingsFrame(navigation, content)
    @Composable override fun PlayerOverlay(p: PlayerOverlayParams) = SbsPlayer(p)
    @Composable override fun LiveChannelList(p: LiveChannelListParams) = SabhiyaLiveChannelList(p)
    @Composable override fun LiveChannelInfo(p: LiveChannelInfoParams) = SabhiyaLiveChannelInfo(p)
    @Composable override fun ChannelOptions(p: com.streamvault.app.ui.themes.bespoke.ChannelOptionsParams) = SabhiyaChannelOptions(p)
}
