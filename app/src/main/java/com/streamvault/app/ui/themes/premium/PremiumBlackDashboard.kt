package com.streamvault.app.ui.themes.premium

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.app.navigation.Routes
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.app.ui.screens.dashboard.DashboardUiState
import com.streamvault.domain.model.Channel
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.PlaybackHistory
import com.streamvault.domain.model.Series
import com.streamvault.domain.model.VirtualCategoryIds

/** Home Premium Black: هوامش سوداء ومحتوى أحادي كبير وmetadata موجز، وليس Hero/rows الخاصة بـStreaming Platform. */
@Composable
internal fun PremiumBlackDashboard(
    uiState: DashboardUiState,
    recordingChannelIds: Set<Long>,
    scheduledChannelIds: Set<Long>,
    onNavigate: (String) -> Unit,
    onRecentChannelClick: (Channel, Long?) -> Unit,
    onFavoriteChannelClick: (Channel, Long?) -> Unit,
    onMovieClick: (Movie) -> Unit,
    onSeriesClick: (Series) -> Unit,
    onContinueWatchingItemClick: (PlaybackHistory) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize().background(PremiumCanvas), contentPadding = PaddingValues(horizontal = 32.dp, vertical = 26.dp), verticalArrangement = Arrangement.spacedBy(28.dp)) {
        item("premium_hero") { PremiumHero(uiState.feature.title.ifBlank { "Alaa Player" }, uiState.feature.summary.ifBlank { "A refined way to watch live channels, films and series." }, { onNavigate(Routes.LIVE_TV) }, { onNavigate(Routes.SEARCH) }) }
        item("premium_shortcuts") { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { PremiumShortcut("LIVE", Modifier.weight(1f)) { onNavigate(Routes.LIVE_TV) }; PremiumShortcut("FILMS", Modifier.weight(1f)) { onNavigate(Routes.MOVIES) }; PremiumShortcut("SERIES", Modifier.weight(1f)) { onNavigate(Routes.SERIES) }; PremiumShortcut("SAVED", Modifier.weight(1f)) { onNavigate(Routes.liveTv(VirtualCategoryIds.FAVORITES)) } } }
        if (uiState.continueWatching.isNotEmpty()) item("premium_continue") { PremiumShelf("CONTINUE WATCHING", "Resume", uiState.continueWatching.take(10), { it.title }, { it.contentType.name.replace('_', ' ') }, onContinueWatchingItemClick) }
        if (uiState.favoriteChannels.isNotEmpty()) item("premium_favorites") { PremiumShelf("FAVOURITE CHANNELS", "Live collection", uiState.favoriteChannels.take(12), { it.name }, { channel -> when { channel.id in recordingChannelIds -> "RECORDING"; channel.id in scheduledChannelIds -> "SCHEDULED"; else -> channel.currentProgram?.title ?: "LIVE" } }, { onFavoriteChannelClick(it, uiState.currentCombinedProfileId) }) }
        if (uiState.recentChannels.isNotEmpty()) item("premium_recent") { PremiumShelf("RECENT LIVE", "Last stations", uiState.recentChannels.take(12), { it.name }, { it.currentProgram?.title ?: "LIVE" }, { onRecentChannelClick(it, uiState.currentCombinedProfileId) }) }
        val films = (uiState.recommendedMovies + uiState.topRatedMovies + uiState.recentMovies).distinctBy { it.id }.take(12)
        if (films.isNotEmpty()) item("premium_films") { PremiumShelf("FEATURED FILMS", "Selected catalogue", films, { it.name }, { it.genre ?: "FILM" }, onMovieClick) }
        val series = (uiState.recentSeries + uiState.favoriteSeries).distinctBy { it.id }.take(12)
        if (series.isNotEmpty()) item("premium_series") { PremiumShelf("SERIES COLLECTION", "Seasons and episodes", series, { it.name }, { it.genre ?: "SERIES" }, onSeriesClick) }
    }
}

@Composable private fun PremiumHero(title: String, summary: String, onLive: () -> Unit, onSearch: () -> Unit) { val shape = RoundedCornerShape(18.dp); TvClickableSurface(onClick = onLive, modifier = Modifier.fillMaxWidth().height(300.dp), shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = PremiumPanel, focusedContainerColor = PremiumPanelFocused, contentColor = PremiumText, focusedContentColor = PremiumText), border = ClickableSurfaceDefaults.border(border = Border(border = BorderStroke(1.dp, PremiumMetal), shape = shape), focusedBorder = Border(border = BorderStroke(2.dp, PremiumFocus), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.015f)) { Box(Modifier.fillMaxSize().padding(30.dp)) { Column(Modifier.align(Alignment.BottomStart), verticalArrangement = Arrangement.spacedBy(9.dp)) { Text("PREMIUM BLACK / LIVE LIBRARY", style = MaterialTheme.typography.labelLarge, color = PremiumGold); Text(title, style = MaterialTheme.typography.displaySmall, maxLines = 2, overflow = TextOverflow.Ellipsis); Text(summary, style = MaterialTheme.typography.bodyLarge, color = PremiumMuted, maxLines = 2, overflow = TextOverflow.Ellipsis); Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) { Text("OPEN LIVE", style = MaterialTheme.typography.labelLarge, color = PremiumFocus); Text("SEARCH", style = MaterialTheme.typography.labelLarge, color = PremiumMuted) } } } }
}

@Composable private fun PremiumShortcut(label: String, modifier: Modifier, onClick: () -> Unit) { val shape = RoundedCornerShape(10.dp); TvClickableSurface(onClick = onClick, modifier = modifier, shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = PremiumCanvasRaised, focusedContainerColor = PremiumPanelFocused, contentColor = PremiumText, focusedContentColor = PremiumText), border = ClickableSurfaceDefaults.border(focusedBorder = Border(border = BorderStroke(1.dp, PremiumFocus), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.015f)) { Text(label, Modifier.padding(16.dp), style = MaterialTheme.typography.labelLarge, color = PremiumGold) } }
@Composable private fun <T> PremiumShelf(title: String, subtitle: String, entries: List<T>, label: (T) -> String, detail: (T) -> String, onClick: (T) -> Unit) { Column(verticalArrangement = Arrangement.spacedBy(9.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(title, style = MaterialTheme.typography.titleLarge, color = PremiumText); Text(subtitle.uppercase(), style = MaterialTheme.typography.labelMedium, color = PremiumMuted) }; LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) { items(entries, key = { "$title-${label(it)}" }) { entry -> val shape = RoundedCornerShape(12.dp); TvClickableSurface(onClick = { onClick(entry) }, modifier = Modifier.width(204.dp).height(252.dp), shape = ClickableSurfaceDefaults.shape(shape), colors = ClickableSurfaceDefaults.colors(containerColor = PremiumPanel, focusedContainerColor = PremiumPanelFocused, contentColor = PremiumText, focusedContentColor = PremiumText), border = ClickableSurfaceDefaults.border(border = Border(border = BorderStroke(1.dp, PremiumMetal), shape = shape), focusedBorder = Border(border = BorderStroke(2.dp, PremiumFocus), shape = shape)), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.025f)) { Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.SpaceBetween) { Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { Text(label(entry), style = MaterialTheme.typography.titleMedium, maxLines = 3, overflow = TextOverflow.Ellipsis); Text(detail(entry), style = MaterialTheme.typography.bodySmall, color = PremiumMuted, maxLines = 2, overflow = TextOverflow.Ellipsis) }; Text("DETAILS", style = MaterialTheme.typography.labelSmall, color = PremiumGold) } } } } } }
