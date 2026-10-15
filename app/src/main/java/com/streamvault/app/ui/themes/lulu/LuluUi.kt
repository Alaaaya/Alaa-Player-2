package com.streamvault.app.ui.themes.lulu

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.streamvault.app.ui.themes.bespoke.*
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.Series

object LuluUi : BespokeThemeUi {
    @Composable override fun Shell(p: ShellParams) = LuFreshShell(p)
    @Composable override fun Dashboard(p: DashboardParams) = LuFreshHome(p)
    @Composable override fun LiveTv(p: LiveTvParams) = LuFreshLiveTv(p)
    @Composable override fun Epg(p: EpgParams, modifier: Modifier) = LuluEpg(p, modifier)
    @Composable override fun Movies(p: LibraryParams<Movie>) =
        LuFreshLibrary("Movies", "الأفلام", p, { it.id }, { it.name }, { it.posterUrl }, { it.backdropUrl }, { it.year }, { it.rating }, { it.plot }, { luQuality(it.name) }, true)
    @Composable override fun Series(p: LibraryParams<Series>) =
        LuFreshLibrary("Series", "المسلسلات", p, { it.id }, { it.name }, { it.posterUrl }, { it.backdropUrl }, { it.releaseDate?.take(4) }, { it.rating }, { it.plot }, { s -> s.seasons.size.takeIf { it > 0 }?.let { "S$it" } }, false)
    @Composable override fun MovieDetail(p: MovieDetailParams) = LuFreshMovieDetail(p)
    @Composable override fun SeriesDetail(p: SeriesDetailParams) = LuFreshSeriesDetail(p)
    @Composable override fun Search(p: SearchParams) = LuFreshSearch(p)
    @Composable override fun Favorites(p: FavoritesParams) = LuFreshFavorites(p)
    @Composable override fun SettingsNav(p: SettingsNavParams) = LuFreshSettingsNav(p)
    @Composable override fun SettingsFrame(navigation: @Composable () -> Unit, content: @Composable () -> Unit) = LuFreshSettingsFrame(navigation, content)
    @Composable override fun PlayerOverlay(p: PlayerOverlayParams) = androidx.compose.animation.AnimatedVisibility(p.visible, enter = androidx.compose.animation.fadeIn(), exit = androidx.compose.animation.fadeOut()) { LuGlassPlayer(p) }
    @Composable override fun LiveChannelList(p: LiveChannelListParams) = LuluLiveChannelList(p)
    @Composable override fun LiveChannelInfo(p: LiveChannelInfoParams) = LuluLiveChannelInfo(p)
    @Composable override fun ChannelOptions(p: com.streamvault.app.ui.themes.bespoke.ChannelOptionsParams) = LuluChannelOptions(p)
}
