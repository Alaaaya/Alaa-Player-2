package com.streamvault.app.ui.themes.futuristichud

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.streamvault.app.ui.themes.bespoke.*
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.Series

object FuturisticHudUi : BespokeThemeUi {
    @Composable override fun Shell(p: ShellParams) = FuturisticHudShell(p)
    @Composable override fun Dashboard(p: DashboardParams) = FuturisticHudDashboard(p)
    @Composable override fun LiveTv(p: LiveTvParams) = FuturisticHudLiveTv(p)
    @Composable override fun Epg(p: EpgParams, modifier: Modifier) = FuturisticHudEpg(p, modifier)
    @Composable override fun Movies(p: LibraryParams<Movie>) =
        FuturisticHudLibrary("Movies", "الأفلام", p, { it.id }, { it.name }, { it.posterUrl }, { it.year })
    @Composable override fun Series(p: LibraryParams<Series>) =
        FuturisticHudLibrary("Series", "المسلسلات", p, { it.id }, { it.name }, { it.posterUrl }, { it.genre })
    @Composable override fun MovieDetail(p: MovieDetailParams) = FuturisticHudMovieDetail(p)
    @Composable override fun SeriesDetail(p: SeriesDetailParams) = FuturisticHudSeriesDetail(p)
    @Composable override fun Search(p: SearchParams) = FuturisticHudSearch(p)
    @Composable override fun Favorites(p: FavoritesParams) = FuturisticHudFavorites(p)
    @Composable override fun SettingsNav(p: SettingsNavParams) = FuturisticHudSettingsNav(p)
    @Composable override fun SettingsFrame(navigation: @Composable () -> Unit, content: @Composable () -> Unit) = FuturisticHudSettingsFrame(navigation, content)
    @Composable override fun PlayerOverlay(p: PlayerOverlayParams) = FuturisticHudPlayerOverlay(p)
    @Composable override fun LiveChannelList(p: LiveChannelListParams) = FuturisticHudLiveChannelList(p)
    @Composable override fun LiveChannelInfo(p: LiveChannelInfoParams) = FuturisticHudLiveChannelInfo(p)
    @Composable override fun ChannelOptions(p: com.streamvault.app.ui.themes.bespoke.ChannelOptionsParams) = FuturisticHudChannelOptions(p)
}
