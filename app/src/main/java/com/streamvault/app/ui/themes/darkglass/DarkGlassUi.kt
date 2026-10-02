package com.streamvault.app.ui.themes.darkglass

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.streamvault.app.ui.themes.bespoke.*
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.Series

object DarkGlassUi : BespokeThemeUi {
    @Composable override fun Shell(p: ShellParams) = DarkGlassShell(p)
    @Composable override fun Dashboard(p: DashboardParams) = DarkGlassDashboard(p)
    @Composable override fun LiveTv(p: LiveTvParams) = DarkGlassLiveTv(p)
    @Composable override fun Epg(p: EpgParams, modifier: Modifier) = DarkGlassEpg(p, modifier)
    @Composable override fun Movies(p: LibraryParams<Movie>) =
        DarkGlassLibrary("Movies", "الأفلام", p, { it.id }, { it.name }, { it.posterUrl }, { it.year })
    @Composable override fun Series(p: LibraryParams<Series>) =
        DarkGlassLibrary("Series", "المسلسلات", p, { it.id }, { it.name }, { it.posterUrl }, { it.genre })
    @Composable override fun MovieDetail(p: MovieDetailParams) = DarkGlassMovieDetail(p)
    @Composable override fun SeriesDetail(p: SeriesDetailParams) = DarkGlassSeriesDetail(p)
    @Composable override fun Search(p: SearchParams) = DarkGlassSearch(p)
    @Composable override fun Favorites(p: FavoritesParams) = DarkGlassFavorites(p)
    @Composable override fun SettingsNav(p: SettingsNavParams) = DarkGlassSettingsNav(p)
    @Composable override fun SettingsFrame(navigation: @Composable () -> Unit, content: @Composable () -> Unit) = DarkGlassSettingsFrame(navigation, content)
    @Composable override fun PlayerOverlay(p: PlayerOverlayParams) = DarkGlassPlayerOverlay(p)
    @Composable override fun LiveChannelList(p: LiveChannelListParams) = DarkGlassLiveChannelList(p)
    @Composable override fun LiveChannelInfo(p: LiveChannelInfoParams) = DarkGlassLiveChannelInfo(p)
    @Composable override fun ChannelOptions(p: com.streamvault.app.ui.themes.bespoke.ChannelOptionsParams) = DarkGlassChannelOptions(p)
}
