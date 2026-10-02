package com.streamvault.app.ui.themes.moderntv

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.streamvault.app.ui.themes.bespoke.*
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.Series

object ModernTvUi : BespokeThemeUi {
    @Composable override fun Shell(p: ShellParams) = ModernTvShell(p)
    @Composable override fun Dashboard(p: DashboardParams) = ModernTvDashboard(p)
    @Composable override fun LiveTv(p: LiveTvParams) = ModernTvLiveTv(p)
    @Composable override fun Epg(p: EpgParams, modifier: Modifier) = ModernTvEpg(p, modifier)
    @Composable override fun Movies(p: LibraryParams<Movie>) =
        ModernTvLibrary("Movies", "الأفلام", p, { it.id }, { it.name }, { it.posterUrl }, { it.year })
    @Composable override fun Series(p: LibraryParams<Series>) =
        ModernTvLibrary("Series", "المسلسلات", p, { it.id }, { it.name }, { it.posterUrl }, { it.genre })
    @Composable override fun MovieDetail(p: MovieDetailParams) = ModernTvMovieDetail(p)
    @Composable override fun SeriesDetail(p: SeriesDetailParams) = ModernTvSeriesDetail(p)
    @Composable override fun Search(p: SearchParams) = ModernTvSearch(p)
    @Composable override fun Favorites(p: FavoritesParams) = ModernTvFavorites(p)
    @Composable override fun SettingsNav(p: SettingsNavParams) = ModernTvSettingsNav(p)
    @Composable override fun SettingsFrame(navigation: @Composable () -> Unit, content: @Composable () -> Unit) = ModernTvSettingsFrame(navigation, content)
    @Composable override fun PlayerOverlay(p: PlayerOverlayParams) = ModernTvPlayerOverlay(p)
    @Composable override fun LiveChannelList(p: LiveChannelListParams) = ModernTvLiveChannelList(p)
    @Composable override fun LiveChannelInfo(p: LiveChannelInfoParams) = ModernTvLiveChannelInfo(p)
    @Composable override fun ChannelOptions(p: com.streamvault.app.ui.themes.bespoke.ChannelOptionsParams) = ModernTvChannelOptions(p)
}
