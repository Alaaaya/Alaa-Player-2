package com.streamvault.app.ui.themes.chatgpt2

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.streamvault.app.ui.themes.bespoke.*
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.Series

object ChatGpt2Ui : BespokeThemeUi {
    @Composable override fun Shell(p: ShellParams) = ChatGpt2Shell(p)
    @Composable override fun Dashboard(p: DashboardParams) = ChatGpt2Dashboard(p)
    @Composable override fun LiveTv(p: LiveTvParams) = ChatGpt2LiveTv(p)
    @Composable override fun Epg(p: EpgParams, modifier: Modifier) = ChatGpt2Epg(p, modifier)
    @Composable override fun Movies(p: LibraryParams<Movie>) =
        ChatGpt2Library("Movies", "الأفلام", p, { it.id }, { it.name }, { it.posterUrl }, { it.year }, { it.rating }, { it.backdropUrl }, { it.plot }, true)
    @Composable override fun Series(p: LibraryParams<Series>) =
        ChatGpt2Library("Series", "المسلسلات", p, { it.id }, { it.name }, { it.posterUrl }, { it.genre?.substringBefore(",") }, { it.rating }, { it.backdropUrl }, { it.plot }, false)
    @Composable override fun MovieDetail(p: MovieDetailParams) = ChatGpt2MovieDetail(p)
    @Composable override fun SeriesDetail(p: SeriesDetailParams) = ChatGpt2SeriesDetail(p)
    @Composable override fun Search(p: SearchParams) = ChatGpt2Search(p)
    @Composable override fun Favorites(p: FavoritesParams) = ChatGpt2Favorites(p)
    @Composable override fun SettingsNav(p: SettingsNavParams) = ChatGpt2SettingsNav(p)
    @Composable override fun SettingsFrame(navigation: @Composable () -> Unit, content: @Composable () -> Unit) = ChatGpt2SettingsFrame(navigation, content)
    @Composable override fun PlayerOverlay(p: PlayerOverlayParams) = ChatGpt2PlayerOverlay(p)
    @Composable override fun LiveChannelList(p: LiveChannelListParams) = ChatGpt2LiveChannelList(p)
    @Composable override fun LiveChannelInfo(p: LiveChannelInfoParams) = ChatGpt2LiveChannelInfo(p)
    @Composable override fun ChannelOptions(p: com.streamvault.app.ui.themes.bespoke.ChannelOptionsParams) = ChatGpt2ChannelOptions(p)
}
