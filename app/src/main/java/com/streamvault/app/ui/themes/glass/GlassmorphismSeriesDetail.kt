package com.streamvault.app.ui.themes.glass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.app.ui.interaction.TvClickableSurface
import com.streamvault.domain.model.Episode
import com.streamvault.domain.model.ExternalRatings
import com.streamvault.domain.model.Season
import com.streamvault.domain.model.Series

/** تفاصيل مسلسل Glass: التحديد والحالة والأوامر تمرر من الشاشة المشتركة ولا تعاد إدارتها هنا. */
@Composable
internal fun GlassmorphismSeriesDetail(
    series: Series,
    selectedSeason: Season?,
    resumeEpisode: Episode?,
    unwatchedEpisodeCount: Int,
    isCasting: Boolean,
    externalRatings: ExternalRatings,
    isLoadingExternalRatings: Boolean,
    onToggleFavorite: () -> Unit,
    onSelectVariant: (Long) -> Unit,
    onSeasonSelected: (Season) -> Unit,
    onEpisodeClick: (Episode) -> Unit,
    onResumeClick: (Episode) -> Unit,
    onCopyEpisodeUrl: (Episode) -> Unit,
    onDownloadEpisode: (Episode) -> Unit,
    onCastResumeEpisode: () -> Unit,
    onCastEpisode: (Episode) -> Unit,
    onBack: () -> Unit
) {
    var commandEpisode by remember(series.id) { mutableStateOf<Episode?>(null) }
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(GlassCanvas).padding(horizontal = 46.dp, vertical = 30.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        item("glass_series_back") { GlassSeriesAction("← BACK TO LIBRARY", onBack) }
        item("glass_series_hero") {
            GlassSeriesPane {
                Text("SERIES GLASS / ${selectedSeason?.name ?: "ALL SEASONS"}", style = MaterialTheme.typography.labelLarge, color = GlassAccent)
                Text(series.name, style = MaterialTheme.typography.displayMedium, color = GlassText, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(series.plot.orEmpty(), style = MaterialTheme.typography.bodyLarge, color = GlassText, maxLines = 6, overflow = TextOverflow.Ellipsis)
                Text("$unwatchedEpisodeCount UNWATCHED", style = MaterialTheme.typography.labelMedium, color = GlassMuted)
                Text(
                    when {
                        isLoadingExternalRatings -> "EXTERNAL RATINGS / SYNCING"
                        externalRatings.imdb.available -> "EXTERNAL RATINGS / AVAILABLE"
                        else -> "EXTERNAL RATINGS / UNAVAILABLE"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = GlassMuted
                )
            }
        }
        item("glass_series_primary_actions") {
            GlassSeriesPane {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlassSeriesAction(if (series.isFavorite) "SAVED" else "SAVE", onToggleFavorite, Modifier.weight(1f))
                    resumeEpisode?.let { episode ->
                        GlassSeriesAction("RESUME S${episode.seasonNumber} E${episode.episodeNumber}", { onResumeClick(episode) }, Modifier.weight(1f))
                    }
                    resumeEpisode?.let {
                        GlassSeriesAction(if (isCasting) "CAST ACTIVE" else "CAST RESUME", onCastResumeEpisode, Modifier.weight(1f))
                    }
                }
            }
        }
        if (series.variants.size > 1) {
            item("glass_series_versions") {
                GlassSeriesPane {
                    Text("AVAILABLE VERSIONS", style = MaterialTheme.typography.labelLarge, color = GlassAccent)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        items(series.variants, key = { it.rawSeriesId }) { variant ->
                            GlassSeriesAction(variant.label, { onSelectVariant(variant.rawSeriesId) })
                        }
                    }
                }
            }
        }
        if (series.seasons.isNotEmpty()) {
            item("glass_series_seasons") {
                GlassSeriesPane {
                    Text("SEASON SELECTOR", style = MaterialTheme.typography.labelLarge, color = GlassAccent)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        items(series.seasons, key = { it.seasonNumber }) { season ->
                            GlassSeriesAction(
                                label = season.name,
                                onClick = { onSeasonSelected(season) },
                                highlighted = season.seasonNumber == selectedSeason?.seasonNumber
                            )
                        }
                    }
                }
            }
        }
        commandEpisode?.let { episode ->
            item("glass_series_episode_commands") {
                GlassSeriesPane {
                    Text("EPISODE COMMANDS / E${episode.episodeNumber}", style = MaterialTheme.typography.labelLarge, color = GlassAccent)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GlassSeriesAction("COPY URL", { onCopyEpisodeUrl(episode) }, Modifier.weight(1f))
                        GlassSeriesAction("DOWNLOAD", { onDownloadEpisode(episode) }, Modifier.weight(1f))
                        GlassSeriesAction(if (isCasting) "CAST ACTIVE" else "CAST", { onCastEpisode(episode) }, Modifier.weight(1f))
                    }
                    GlassSeriesAction("CLOSE COMMANDS", { commandEpisode = null }, Modifier.fillMaxWidth())
                }
            }
        }
        selectedSeason?.let { season ->
            item("glass_series_episodes_label") {
                Text("EPISODES / HOLD FOR COMMANDS", style = MaterialTheme.typography.labelLarge, color = GlassAccent)
            }
            if (season.episodes.isEmpty()) {
                item("glass_series_no_episodes") {
                    GlassSeriesPane {
                        Text("NO EPISODES", style = MaterialTheme.typography.titleLarge, color = GlassText)
                        Text("No episodes are available for the selected season.", style = MaterialTheme.typography.bodyMedium, color = GlassMuted)
                    }
                }
            }
            items(season.episodes, key = { "glass_episode_${it.id}" }) { episode ->
                GlassSeriesAction(
                    label = "E${episode.episodeNumber}  ${episode.title}",
                    onClick = { onEpisodeClick(episode) },
                    modifier = Modifier.fillMaxWidth(),
                    supporting = episode.plot.orEmpty(),
                    onLongClick = { commandEpisode = episode }
                )
            }
        }
    }
}

@Composable
private fun GlassSeriesAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supporting: String = "",
    highlighted: Boolean = false,
    onLongClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(18.dp)
    TvClickableSurface(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (highlighted) GlassPaneFocused else GlassPane,
            focusedContainerColor = GlassPaneFocused,
            contentColor = GlassText,
            focusedContentColor = GlassText
        ),
        border = ClickableSurfaceDefaults.border(
            border = Border(border = BorderStroke(1.dp, if (highlighted) GlassAccent else GlassRule), shape = shape),
            focusedBorder = Border(border = BorderStroke(2.dp, GlassFocus), shape = shape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.03f)
    ) {
        Column(Modifier.padding(horizontal = 17.dp, vertical = 13.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (supporting.isNotBlank()) Text(supporting, style = MaterialTheme.typography.bodySmall, color = GlassMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun GlassSeriesPane(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().background(GlassPane, RoundedCornerShape(26.dp)).padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
        content = content
    )
}
