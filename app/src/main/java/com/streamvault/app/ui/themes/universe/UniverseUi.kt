package com.streamvault.app.ui.themes.universe

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.streamvault.app.ui.themes.bespoke.*
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.Series

object UniverseUi : BespokeThemeUi {
    @Composable override fun Shell(p: ShellParams) = UvShell(p)
    @Composable override fun Dashboard(p: DashboardParams) = UvHome(p)
    @Composable override fun LiveTv(p: LiveTvParams) = UvLiveTv(p)
    @Composable override fun Epg(p: EpgParams, modifier: Modifier) = UniverseEpg(p, modifier)
    @Composable override fun Movies(p: LibraryParams<Movie>) =
        UvLibrary("Movies", "الأفلام", p, { it.id }, { it.name }, { it.posterUrl }, { it.year }, { it.rating }, { it.plot }, true)
    @Composable override fun Series(p: LibraryParams<Series>) =
        UvLibrary("Series", "المسلسلات", p, { it.id }, { it.name }, { it.posterUrl }, { it.genre?.substringBefore(",") }, { it.rating }, { it.plot }, false)
    @Composable override fun MovieDetail(p: MovieDetailParams) = UvMovieDetail(p)
    @Composable override fun SeriesDetail(p: SeriesDetailParams) = UvSeriesDetail(p)
    @Composable override fun Search(p: SearchParams) = UvSearch(p)
    @Composable override fun Favorites(p: FavoritesParams) = UniverseFavorites(p)
    @Composable override fun SettingsNav(p: SettingsNavParams) = UvSettingsNav(p)
    @Composable override fun SettingsFrame(navigation: @Composable () -> Unit, content: @Composable () -> Unit) = UvSettingsFrame(navigation, content)
    @Composable override fun PlayerOverlay(p: PlayerOverlayParams) = UniversePlayerOverlay(p)
    @Composable override fun LiveChannelList(p: LiveChannelListParams) = UniverseLiveChannelList(p)
    @Composable override fun LiveChannelInfo(p: LiveChannelInfoParams) = UniverseLiveChannelInfo(p)
    @Composable override fun ChannelOptions(p: com.streamvault.app.ui.themes.bespoke.ChannelOptionsParams) = UniverseChannelOptions(p)
}
