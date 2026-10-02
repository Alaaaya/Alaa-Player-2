package com.streamvault.app.ui.themes.magazinemedia

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.streamvault.app.ui.themes.bespoke.*
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.Series

object MagazineMediaUi : BespokeThemeUi {
    @Composable override fun Shell(p: ShellParams) = MagazineMediaShell(p)
    @Composable override fun Dashboard(p: DashboardParams) = MagazineMediaDashboard(p)
    @Composable override fun LiveTv(p: LiveTvParams) = MagazineMediaLiveTv(p)
    @Composable override fun Epg(p: EpgParams, modifier: Modifier) = MagazineMediaEpg(p, modifier)
    @Composable override fun Movies(p: LibraryParams<Movie>) =
        MagazineMediaLibrary("Movies", "الأفلام", p, { it.id }, { it.name }, { it.posterUrl }, { it.year })
    @Composable override fun Series(p: LibraryParams<Series>) =
        MagazineMediaLibrary("Series", "المسلسلات", p, { it.id }, { it.name }, { it.posterUrl }, { it.genre })
    @Composable override fun MovieDetail(p: MovieDetailParams) = MagazineMediaMovieDetail(p)
    @Composable override fun SeriesDetail(p: SeriesDetailParams) = MagazineMediaSeriesDetail(p)
    @Composable override fun Search(p: SearchParams) = MagazineMediaSearch(p)
    @Composable override fun Favorites(p: FavoritesParams) = MagazineMediaFavorites(p)
    @Composable override fun SettingsNav(p: SettingsNavParams) = MagazineMediaSettingsNav(p)
    @Composable override fun SettingsFrame(navigation: @Composable () -> Unit, content: @Composable () -> Unit) = MagazineMediaSettingsFrame(navigation, content)
    @Composable override fun PlayerOverlay(p: PlayerOverlayParams) = MagazineMediaPlayerOverlay(p)
    @Composable override fun LiveChannelList(p: LiveChannelListParams) = MagazineMediaLiveChannelList(p)
    @Composable override fun LiveChannelInfo(p: LiveChannelInfoParams) = MagazineMediaLiveChannelInfo(p)
    @Composable override fun ChannelOptions(p: com.streamvault.app.ui.themes.bespoke.ChannelOptionsParams) = MagazineMediaChannelOptions(p)
}
