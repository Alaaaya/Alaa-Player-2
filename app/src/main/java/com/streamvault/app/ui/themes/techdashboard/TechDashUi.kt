package com.streamvault.app.ui.themes.techdashboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.streamvault.app.ui.themes.bespoke.BespokeThemeUi
import com.streamvault.app.ui.themes.bespoke.DashboardParams
import com.streamvault.app.ui.themes.bespoke.EpgParams
import com.streamvault.app.ui.themes.bespoke.FavoritesParams
import com.streamvault.app.ui.themes.bespoke.LibraryParams
import com.streamvault.app.ui.themes.bespoke.LiveTvParams
import com.streamvault.app.ui.themes.bespoke.LiveChannelInfoParams
import com.streamvault.app.ui.themes.bespoke.LiveChannelListParams
import com.streamvault.app.ui.themes.bespoke.MovieDetailParams
import com.streamvault.app.ui.themes.bespoke.PlayerOverlayParams
import com.streamvault.app.ui.themes.bespoke.SearchParams
import com.streamvault.app.ui.themes.bespoke.SeriesDetailParams
import com.streamvault.app.ui.themes.bespoke.SettingsNavParams
import com.streamvault.app.ui.themes.bespoke.ShellParams
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.Series

object TechDashUi : BespokeThemeUi {
    @Composable override fun Shell(p: ShellParams) = TechDashShell(p)
    @Composable override fun Dashboard(p: DashboardParams) = TechDashDashboard(p)
    @Composable override fun LiveTv(p: LiveTvParams) = TechDashLiveTv(p)
    @Composable override fun Epg(p: EpgParams, modifier: Modifier) = TechDashEpg(p, modifier)
    @Composable override fun Movies(p: LibraryParams<Movie>) =
        TechDashLibrary("Film", p, { it.id }, { it.name }, { it.posterUrl }, { it.year })
    @Composable override fun Series(p: LibraryParams<Series>) =
        TechDashLibrary("Series", p, { it.id }, { it.name }, { it.posterUrl }, { it.genre })
    @Composable override fun MovieDetail(p: MovieDetailParams) = TechDashMovieDetail(p)
    @Composable override fun SeriesDetail(p: SeriesDetailParams) = TechDashSeriesDetail(p)
    @Composable override fun Search(p: SearchParams) = TechDashSearch(p)
    @Composable override fun Favorites(p: FavoritesParams) = TechDashFavorites(p)
    @Composable override fun SettingsNav(p: SettingsNavParams) = TechDashSettingsNav(p)
    @Composable override fun SettingsFrame(navigation: @Composable () -> Unit, content: @Composable () -> Unit) = TechDashSettingsFrame(navigation, content)
    @Composable override fun PlayerOverlay(p: PlayerOverlayParams) = TechDashPlayerOverlay(p)
    @Composable override fun LiveChannelList(p: LiveChannelListParams) = TechDashLiveChannelList(p)
    @Composable override fun LiveChannelInfo(p: LiveChannelInfoParams) = TechDashLiveChannelInfo(p)
    @Composable override fun ChannelOptions(p: com.streamvault.app.ui.themes.bespoke.ChannelOptionsParams) = TechDashChannelOptions(p)
}
