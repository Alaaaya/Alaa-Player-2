package com.streamvault.app.ui.themes.ghurub

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.streamvault.app.ui.themes.bespoke.*
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.Series

object GhurubUi : BespokeThemeUi {
    @Composable override fun Shell(p: ShellParams) {
        val artwork = remember { mutableStateOf<String?>(null) }
        CompositionLocalProvider(LocalHorizonArtwork provides artwork) { IvFreshShell(p) }
    }
    @Composable override fun Dashboard(p: DashboardParams) = IvFreshHome(p)
    @Composable override fun LiveTv(p: LiveTvParams) = IvFreshLiveTv(p)
    @Composable override fun Epg(p: EpgParams, modifier: Modifier) = GhurubEpg(p, modifier)
    @Composable override fun Movies(p: LibraryParams<Movie>) =
        IvFreshLibrary("Movies", "الأفلام", p, { it.id }, { it.name }, { it.posterUrl }, { it.backdropUrl }, { it.year }, { it.rating }, { it.plot }, { ivQuality(it.name) }, true)
    @Composable override fun Series(p: LibraryParams<Series>) =
        IvFreshLibrary("Series", "المسلسلات", p, { it.id }, { it.name }, { it.posterUrl }, { it.backdropUrl }, { it.releaseDate?.take(4) }, { it.rating }, { it.plot }, { s -> s.seasons.size.takeIf { it > 0 }?.let { "S$it" } }, false)
    @Composable override fun MovieDetail(p: MovieDetailParams) = IvFreshMovieDetail(p)
    @Composable override fun SeriesDetail(p: SeriesDetailParams) = IvFreshSeriesDetail(p)
    @Composable override fun Search(p: SearchParams) = IvFreshSearch(p)
    @Composable override fun Favorites(p: FavoritesParams) = IvFreshFavorites(p)
    @Composable override fun SettingsNav(p: SettingsNavParams) = IvFreshSettingsNav(p)
    @Composable override fun SettingsFrame(navigation: @Composable () -> Unit, content: @Composable () -> Unit) = IvFreshSettingsFrame(navigation, content)
    @Composable override fun PlayerOverlay(p: PlayerOverlayParams) = androidx.compose.animation.AnimatedVisibility(p.visible) { IvGlassPlayer(p) }
    @Composable override fun LiveChannelList(p: LiveChannelListParams) = GhurubLiveChannelList(p)
    @Composable override fun LiveChannelInfo(p: LiveChannelInfoParams) = GhurubLiveChannelInfo(p)
    @Composable override fun ChannelOptions(p: com.streamvault.app.ui.themes.bespoke.ChannelOptionsParams) = GhurubChannelOptions(p)
}
