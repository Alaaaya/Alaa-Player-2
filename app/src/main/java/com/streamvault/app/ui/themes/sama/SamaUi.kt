package com.streamvault.app.ui.themes.sama

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.streamvault.app.ui.themes.bespoke.*
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.Series

object SamaUi : BespokeThemeUi {
    @Composable override fun Shell(p: ShellParams) = SamaShell(p)
    @Composable override fun Dashboard(p: DashboardParams) = SamaDashboard(p)
    @Composable override fun LiveTv(p: LiveTvParams) = SamaLiveTv(p)
    @Composable override fun Epg(p: EpgParams, modifier: Modifier) = SamaEpg(p, modifier)
    @Composable override fun Movies(p: LibraryParams<Movie>) =
        SamaLibrary("Movies", "الأفلام", p, { it.id }, { it.name }, { it.posterUrl }, { it.year }, { it.rating }, { it.backdropUrl }, { it.plot }, true, { listOfNotNull(it.variantLabel, it.name, it.containerExtension).joinToString(" ") })
    @Composable override fun Series(p: LibraryParams<Series>) =
        SamaLibrary("Series", "المسلسلات", p, { it.id }, { it.name }, { it.posterUrl }, { it.genre?.substringBefore(",") }, { it.rating }, { it.backdropUrl }, { it.plot }, false, { it.name })
    @Composable override fun MovieDetail(p: MovieDetailParams) = SamaMovieDetail(p)
    @Composable override fun SeriesDetail(p: SeriesDetailParams) = SamaSeriesDetail(p)
    @Composable override fun Search(p: SearchParams) = SamaSearch(p)
    @Composable override fun Favorites(p: FavoritesParams) = SamaFavorites(p)
    @Composable override fun SettingsNav(p: SettingsNavParams) = SamaSettingsNav(p)
    @Composable override fun SettingsFrame(navigation: @Composable () -> Unit, content: @Composable () -> Unit) = SamaSettingsFrame(navigation, content)
    @Composable override fun PlayerOverlay(p: PlayerOverlayParams) = SamaPlayerOverlay(p)
    @Composable override fun LiveChannelList(p: LiveChannelListParams) = SamaLiveChannelList(p)
    @Composable override fun LiveChannelInfo(p: LiveChannelInfoParams) = SamaLiveChannelInfo(p)
    @Composable override fun ChannelOptions(p: com.streamvault.app.ui.themes.bespoke.ChannelOptionsParams) = SamaChannelOptions(p)
}
