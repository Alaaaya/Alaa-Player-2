package com.streamvault.app.ui.themes.chatgpt

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.streamvault.app.ui.themes.bespoke.*
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.Series

object ChatGptUi : BespokeThemeUi {
    @Composable override fun Shell(p: ShellParams) = ChatGptShell(p)
    @Composable override fun Dashboard(p: DashboardParams) = ChatGptDashboard(p)
    @Composable override fun LiveTv(p: LiveTvParams) = ChatGptLiveTv(p)
    @Composable override fun Epg(p: EpgParams, modifier: Modifier) = ChatGptEpg(p, modifier)
    @Composable override fun Movies(p: LibraryParams<Movie>) =
        ChatGptLibrary("Movies", "الأفلام", p, { it.id }, { it.name }, { it.posterUrl }, { it.year })
    @Composable override fun Series(p: LibraryParams<Series>) =
        ChatGptLibrary("Series", "المسلسلات", p, { it.id }, { it.name }, { it.posterUrl }, { it.genre })
    @Composable override fun MovieDetail(p: MovieDetailParams) = ChatGptMovieDetail(p)
    @Composable override fun SeriesDetail(p: SeriesDetailParams) = ChatGptSeriesDetail(p)
    @Composable override fun Search(p: SearchParams) = ChatGptSearch(p)
    @Composable override fun Favorites(p: FavoritesParams) = ChatGptFavorites(p)
    @Composable override fun SettingsNav(p: SettingsNavParams) = ChatGptSettingsNav(p)
    @Composable override fun SettingsFrame(navigation: @Composable () -> Unit, content: @Composable () -> Unit) = ChatGptSettingsFrame(navigation, content)
    @Composable override fun PlayerOverlay(p: PlayerOverlayParams) = ChatGptPlayerOverlay(p)
    @Composable override fun LiveChannelList(p: LiveChannelListParams) = ChatGptLiveChannelList(p)
    @Composable override fun LiveChannelInfo(p: LiveChannelInfoParams) = ChatGptLiveChannelInfo(p)
    @Composable override fun ChannelOptions(p: com.streamvault.app.ui.themes.bespoke.ChannelOptionsParams) = ChatGptChannelOptions(p)
}
