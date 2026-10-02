package com.streamvault.app.ui.themes.purplegalaxy

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

object PurpleGalaxyUi : BespokeThemeUi {
    @Composable override fun Shell(p: ShellParams) = PurpleGalaxyShell(p)
    @Composable override fun Dashboard(p: DashboardParams) = PurpleGalaxyDashboard(p)
    @Composable override fun LiveTv(p: LiveTvParams) = PurpleGalaxyLiveTv(p)
    @Composable override fun Epg(p: EpgParams, modifier: Modifier) = PurpleGalaxyEpg(p, modifier)
    @Composable override fun Movies(p: LibraryParams<Movie>) =
        PurpleGalaxyLibrary("Film", p, { it.id }, { it.name }, { it.posterUrl }, { it.year })
    @Composable override fun Series(p: LibraryParams<Series>) =
        PurpleGalaxyLibrary("Series", p, { it.id }, { it.name }, { it.posterUrl }, { it.genre })
    @Composable override fun MovieDetail(p: MovieDetailParams) = PurpleGalaxyMovieDetail(p)
    @Composable override fun SeriesDetail(p: SeriesDetailParams) = PurpleGalaxySeriesDetail(p)
    @Composable override fun Search(p: SearchParams) = PurpleGalaxySearch(p)
    @Composable override fun Favorites(p: FavoritesParams) = PurpleGalaxyFavorites(p)
    @Composable override fun SettingsNav(p: SettingsNavParams) = PurpleGalaxySettingsNav(p)
    @Composable override fun SettingsFrame(navigation: @Composable () -> Unit, content: @Composable () -> Unit) = PurpleGalaxySettingsFrame(navigation, content)
    @Composable override fun PlayerOverlay(p: PlayerOverlayParams) = PurpleGalaxyPlayerOverlay(p)
    @Composable override fun LiveChannelList(p: LiveChannelListParams) = PurpleGalaxyLiveChannelList(p)
    @Composable override fun LiveChannelInfo(p: LiveChannelInfoParams) = PurpleGalaxyLiveChannelInfo(p)
}
