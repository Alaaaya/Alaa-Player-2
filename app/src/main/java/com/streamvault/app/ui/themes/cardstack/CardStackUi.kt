package com.streamvault.app.ui.themes.cardstack

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.streamvault.app.ui.themes.bespoke.*
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.Series

object CardStackUi : BespokeThemeUi {
    @Composable override fun Shell(p: ShellParams) = CardStackShell(p)
    @Composable override fun Dashboard(p: DashboardParams) = CardStackDashboard(p)
    @Composable override fun LiveTv(p: LiveTvParams) = CardStackLiveTv(p)
    @Composable override fun Epg(p: EpgParams, modifier: Modifier) = CardStackEpg(p, modifier)
    @Composable override fun Movies(p: LibraryParams<Movie>) =
        CardStackLibrary("Movies", "الأفلام", p, { it.id }, { it.name }, { it.posterUrl }, { it.year })
    @Composable override fun Series(p: LibraryParams<Series>) =
        CardStackLibrary("Series", "المسلسلات", p, { it.id }, { it.name }, { it.posterUrl }, { it.genre })
    @Composable override fun MovieDetail(p: MovieDetailParams) = CardStackMovieDetail(p)
    @Composable override fun SeriesDetail(p: SeriesDetailParams) = CardStackSeriesDetail(p)
    @Composable override fun Search(p: SearchParams) = CardStackSearch(p)
    @Composable override fun Favorites(p: FavoritesParams) = CardStackFavorites(p)
    @Composable override fun SettingsNav(p: SettingsNavParams) = CardStackSettingsNav(p)
    @Composable override fun SettingsFrame(navigation: @Composable () -> Unit, content: @Composable () -> Unit) = CardStackSettingsFrame(navigation, content)
    @Composable override fun PlayerOverlay(p: PlayerOverlayParams) = CardStackPlayerOverlay(p)
    @Composable override fun LiveChannelList(p: LiveChannelListParams) = CardStackLiveChannelList(p)
    @Composable override fun LiveChannelInfo(p: LiveChannelInfoParams) = CardStackLiveChannelInfo(p)
    @Composable override fun ChannelOptions(p: com.streamvault.app.ui.themes.bespoke.ChannelOptionsParams) = CardStackChannelOptions(p)
}
