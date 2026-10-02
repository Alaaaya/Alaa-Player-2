package com.streamvault.app.ui.themes.purplegalaxy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.streamvault.app.ui.components.SearchInput
import com.streamvault.app.ui.screens.favorites.SavedLibraryFilter
import com.streamvault.app.ui.screens.favorites.SavedLibraryPreset
import com.streamvault.app.ui.screens.favorites.SavedLibrarySort
import com.streamvault.app.ui.screens.search.SearchTab
import com.streamvault.app.ui.themes.bespoke.FavoritesParams
import com.streamvault.app.ui.themes.bespoke.MovieDetailParams
import com.streamvault.app.ui.themes.bespoke.SearchParams
import com.streamvault.app.ui.themes.bespoke.SeriesDetailParams
import com.streamvault.app.ui.themes.bespoke.SettingsNavParams

@Composable
private fun DetailBackdrop(imageUrl: String?, content: @Composable () -> Unit) {
    GalaxyBackdrop {
        imageUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, alpha = 0.35f, modifier = Modifier.fillMaxSize()) }
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(PG.Void, PG.Void.copy(alpha = 0.75f), Color.Transparent))))
        content()
    }
}

@Composable
private fun MetaLine(vararg parts: String?) {
    Text(parts.filterNot { it.isNullOrBlank() }.joinToString("  ✦  "), color = PG.Comet, fontSize = 13.sp, maxLines = 1)
}

@Composable
internal fun PurpleGalaxyMovieDetail(p: MovieDetailParams) {
    val m = p.movie
    val play = remember { FocusRequester() }
    LaunchedEffect(m.id) { runCatching { play.requestFocus() } }
    DetailBackdrop(m.backdropUrl ?: m.posterUrl) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(56.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(36.dp)) {
                    Box(Modifier.width(220.dp).height(320.dp).clip(PG.Arch).background(PG.Nebula)) {
                        m.posterUrl?.let { AsyncImage(it, m.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GalaxyLabel("Film · observation log")
                        GalaxyTitle(m.name, size = 40)
                        MetaLine(m.year, m.duration, m.genre, if (m.rating > 0) "★ %.1f".format(m.rating) else null, m.variantLabel)
                        Text(m.plot.orEmpty(), color = PG.Dust, fontSize = 15.sp, maxLines = 5, overflow = TextOverflow.Ellipsis)
                        m.director?.takeIf { it.isNotBlank() }?.let { Text("Director · $it", color = PG.Muted, fontSize = 13.sp) }
                        m.cast?.takeIf { it.isNotBlank() }?.let { Text("Crew · $it", color = PG.Muted, fontSize = 13.sp, maxLines = 2) }
                        Spacer(Modifier.height(4.dp))
                        if (p.hasResume) Text("Resume from ${formatDuration(p.resumePositionMs)}", color = PG.Flare, fontSize = 13.sp)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            item { GalaxyChip(if (p.hasResume) "▶  Resume" else "▶  Launch", true, p.onPlay, Modifier.focusRequester(play)) }
                            p.onPlayTrailer?.let { t -> item { GalaxyChip("Trailer", false, t) } }
                            item { GalaxyChip(if (m.isFavorite) "★ Saved" else "☆ Save", m.isFavorite, p.onToggleFavorite) }
                            item { GalaxyChip(if (p.isCasting) "Casting…" else "Cast", p.isCasting, p.onCast) }
                            item { GalaxyChip("Download", false, p.onDownload) }
                            item { GalaxyChip("Copy URL", false, p.onCopyUrl) }
                            item { GalaxyChip("Back", false, p.onBack) }
                        }
                        if (m.variants.size > 1) LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(m.variants) { v -> GalaxyChip(v.label, v.rawMovieId == m.selectedVariantId, { p.onSelectVariant(v.rawMovieId) }) }
                        }
                    }
                }
            }
            if (p.relatedContent.isNotEmpty()) item {
                GalaxyRowHeader("Nearby", "Related films")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    items(p.relatedContent, key = { it.id }) { r -> ArchPoster(r.name, r.posterUrl, r.year, { p.onRelatedClick(r) }, Modifier.width(150.dp)) }
                }
            }
        }
    }
}

@Composable
internal fun PurpleGalaxySeriesDetail(p: SeriesDetailParams) {
    val s = p.series
    val primary = remember { FocusRequester() }
    LaunchedEffect(s.id) { runCatching { primary.requestFocus() } }
    DetailBackdrop(s.backdropUrl ?: s.posterUrl) {
        Row(Modifier.fillMaxSize().padding(48.dp), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
            Column(Modifier.width(440.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GalaxyLabel("Series · constellation")
                GalaxyTitle(s.name, size = 36)
                MetaLine(s.releaseDate, s.genre, if (s.rating > 0) "★ %.1f".format(s.rating) else null, "${s.seasons.size} seasons")
                if (!p.isLoadingExternalRatings) {
                    val r = p.externalRatings
                    val extras = listOf("IMDb" to r.imdb, "RT" to r.rottenTomatoes, "MC" to r.metacritic).filter { it.second.available }.map { "${it.first} ${it.second.displayValue}" }
                    if (extras.isNotEmpty()) Text(extras.joinToString("  ·  "), color = PG.Muted, fontSize = 12.sp)
                }
                Text(s.plot.orEmpty(), color = PG.Dust, fontSize = 14.sp, maxLines = 6, overflow = TextOverflow.Ellipsis)
                if (p.unwatchedEpisodeCount > 0) Text("${p.unwatchedEpisodeCount} unexplored episodes", color = PG.Flare, fontSize = 13.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    val resume = p.resumeEpisode
                    item {
                        GalaxyChip(
                            if (resume != null) "▶  S${resume.seasonNumber} E${resume.episodeNumber}" else "▶  Start", true,
                            { (resume ?: p.selectedSeason?.episodes?.firstOrNull())?.let(p.onResumeClick) }, Modifier.focusRequester(primary)
                        )
                    }
                    item { GalaxyChip(if (s.isFavorite) "★ Saved" else "☆ Save", s.isFavorite, p.onToggleFavorite) }
                    if (resume != null) item { GalaxyChip(if (p.isCasting) "Casting…" else "Cast", p.isCasting, p.onCastResumeEpisode) }
                    item { GalaxyChip("Back", false, p.onBack) }
                }
                if (s.variants.size > 1) LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(s.variants) { v -> GalaxyChip(v.label, v.rawSeriesId == s.selectedVariantId, { p.onSelectVariant(v.rawSeriesId) }) }
                }
            }
            Column(Modifier.weight(1f).fillMaxHeight()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(s.seasons, key = { it.seasonNumber }) { season ->
                        GalaxyChip(season.name.ifBlank { "Season ${season.seasonNumber}" }, season.seasonNumber == p.selectedSeason?.seasonNumber, { p.onSeasonSelected(season) })
                    }
                }
                Spacer(Modifier.height(14.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                    items(p.selectedSeason?.episodes.orEmpty(), key = { it.id }) { e ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GalaxySurface(onClick = { p.onEpisodeClick(e) }, scale = 1.02f, modifier = Modifier.weight(1f)) {
                                Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.width(128.dp).height(72.dp).clip(PG.Card).background(PG.Nebula)) {
                                        e.coverUrl?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                                        Text("E${e.episodeNumber}", color = PG.Star, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.BottomStart).padding(6.dp))
                                    }
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text(e.title, color = PG.Star, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                        Text(e.plot.orEmpty(), color = PG.Dust, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                        if (e.watchProgress > 0 && e.durationSeconds > 0) OrbitProgress(e.watchProgress / (e.durationSeconds * 1000f), height = 2.dp)
                                    }
                                    e.duration?.let { Text(it, color = PG.Muted, fontSize = 12.sp) }
                                }
                            }
                            GalaxyChip("Cast", false, { p.onCastEpisode(e) })
                            GalaxyChip("⤓", false, { p.onDownloadEpisode(e) })
                            GalaxyChip("URL", false, { p.onCopyEpisodeUrl(e) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun PurpleGalaxySearch(p: SearchParams) {
    LaunchedEffect(Unit) { runCatching { p.searchFocusRequester.requestFocus() } }
    val s = p.uiState
    Column(Modifier.fillMaxSize()) {
        GalaxyLabel("Deep scan")
        Spacer(Modifier.height(8.dp))
        SearchInput(p.query, p.onQueryChange, "Scan the galaxy for channels, films, series", Modifier.fillMaxWidth(), focusRequester = p.searchFocusRequester, onSearch = p.onSearch)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SearchTab.entries.forEach { t -> GalaxyChip(t.name.lowercase().replaceFirstChar { it.uppercase() }, t == p.selectedTab, { p.onTabSelected(t) }) }
            Spacer(Modifier.weight(1f))
            GalaxyChip("Build full index", false, p.onBuildCompleteIndex)
        }
        Spacer(Modifier.height(16.dp))
        when {
            p.query.isBlank() && p.recentQueries.isNotEmpty() -> {
                GalaxyRowHeader("Log", "Recent scans")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(p.recentQueries) { q -> GalaxyChip(q, false, { p.onRecentQuerySelected(q) }) }
                    item { GalaxyChip("Clear log", false, p.onClearRecentQueries) }
                }
            }
            s.isLoading -> GalaxyEmpty("Scanning…")
            s.hasSearchError -> GalaxyEmpty("Scan failed, try again")
            s.isEmpty -> GalaxyEmpty("Nothing detected for “${p.query}”")
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(24.dp), contentPadding = PaddingValues(bottom = 40.dp)) {
                if (s.channels.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.LIVE)) item {
                    GalaxyRowHeader("Live", "${s.channels.size} transmissions")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(s.channels, key = { it.id }) { c ->
                            val locked = p.isChannelLocked(c)
                            GalaxySurface(onClick = { p.onChannelClick(c) }, onLongClick = { p.onChannelLongClick(c) }, modifier = Modifier.width(200.dp)) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    PlanetLogo(c.name, if (locked) null else c.logoUrl, 40.dp)
                                    Column {
                                        Text(c.name, color = PG.Star, fontSize = 14.sp, maxLines = 1)
                                        if (c.id in p.recordingChannelIds) Text("● REC", color = PG.Live, fontSize = 11.sp)
                                        else if (c.id in p.scheduledChannelIds) Text("◷ Scheduled", color = PG.Comet, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
                if (s.movies.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.MOVIES)) item {
                    GalaxyRowHeader("Films", "${s.movies.size} found")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                        items(s.movies, key = { it.id }) { m -> val l = p.isMovieLocked(m); ArchPoster(m.name, if (l) null else m.posterUrl, m.year, { p.onMovieClick(m) }, Modifier.width(150.dp), l, { p.onMovieLongClick(m) }) }
                    }
                }
                if (s.series.isNotEmpty() && p.selectedTab in setOf(SearchTab.ALL, SearchTab.SERIES)) item {
                    GalaxyRowHeader("Series", "${s.series.size} found")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                        items(s.series, key = { it.id }) { m -> val l = p.isSeriesLocked(m); ArchPoster(m.name, if (l) null else m.posterUrl, m.genre, { p.onSeriesClick(m) }, Modifier.width(150.dp), l, { p.onSeriesLongClick(m) }) }
                    }
                }
            }
        }
    }
}

private val presetLabels = mapOf(
    SavedLibraryPreset.ALL_SAVED to "Everything", SavedLibraryPreset.HOME_SHELF to "Home shelf", SavedLibraryPreset.WATCH_NEXT to "Watch next",
    SavedLibraryPreset.LIVE_RECALL to "Live recall", SavedLibraryPreset.MOVIES to "Films", SavedLibraryPreset.SERIES to "Series", SavedLibraryPreset.CUSTOM_GROUPS to "Groups"
)

@Composable
internal fun PurpleGalaxyFavorites(p: FavoritesParams) {
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(22.dp), contentPadding = PaddingValues(bottom = 40.dp)) {
        item {
            GalaxyLabel("Saved constellations")
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SavedLibraryPreset.entries) { pr -> GalaxyChip(presetLabels[pr] ?: pr.name, pr == p.selectedPreset, { p.onPresetSelected(pr) }) }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SavedLibraryFilter.entries.forEach { f -> GalaxyChip(f.name.lowercase().replaceFirstChar { it.uppercase() }, f == p.selectedFilter, { p.onFilterSelected(f) }) }
                Spacer(Modifier.width(16.dp))
                SavedLibrarySort.entries.forEach { o -> GalaxyChip("Sort " + o.name.lowercase(), o == p.selectedSort, { p.onSortSelected(o) }) }
            }
        }
        if (p.continueWatching.isNotEmpty()) item {
            GalaxyRowHeader("Resume", "Unfinished journeys")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                items(p.continueWatching, key = { "c${it.history.id}" }) { h ->
                    ArchPoster(h.title, h.history.posterUrl, h.subtitle, { p.onHistoryClick(h) }, Modifier.width(150.dp),
                        progress = if (h.history.totalDurationMs > 0) h.history.resumePositionMs.toFloat() / h.history.totalDurationMs else null)
                }
            }
        }
        if (p.recentLive.isNotEmpty()) item {
            GalaxyRowHeader("Recent", "Live recall")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                items(p.recentLive, key = { "l${it.history.id}" }) { h ->
                    GalaxySurface(onClick = { p.onHistoryClick(h) }, modifier = Modifier.width(220.dp)) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            PlanetLogo(h.title, h.history.posterUrl, 40.dp)
                            Column { Text(h.title, color = PG.Star, fontSize = 14.sp, maxLines = 1); Text(h.subtitle, color = PG.Muted, fontSize = 11.sp, maxLines = 1) }
                        }
                    }
                }
            }
        }
        if (p.sections.all { it.items.isEmpty() } && p.continueWatching.isEmpty() && p.recentLive.isEmpty()) item { GalaxyEmpty("Nothing saved yet. Long-press anything to save it.") }
        p.sections.filter { it.items.isNotEmpty() }.forEach { section ->
            item(key = section.key) {
                GalaxyRowHeader(section.subtitle.ifBlank { "Saved" }, section.title)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    items(section.items, key = { "${section.key}${it.favorite.contentType}${it.favorite.contentId}" }) { f ->
                        ArchPoster(f.title, f.favorite.let { null }, f.subtitle, { p.onItemClick(f) }, Modifier.width(150.dp), onLongClick = { p.onItemLongClick(f) })
                    }
                }
            }
        }
    }
}

@Composable
internal fun PurpleGalaxySettingsNav(p: SettingsNavParams) {
    LazyColumn(Modifier.width(260.dp).fillMaxHeight().padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        item { GalaxyLabel("Control deck", Modifier.padding(start = 12.dp, bottom = 10.dp)) }
        items(p.entries.size) { i ->
            val (label, icon) = p.entries[i]
            val selected = i == p.selectedCategory
            GalaxySurface(
                onClick = { p.onCategorySelected(i) }, shape = PG.Pill, container = if (selected) PG.Plasma.copy(alpha = 0.45f) else Color.Transparent,
                modifier = Modifier.fillMaxWidth().then(if (selected) Modifier.focusRequester(p.focusRequester) else Modifier)
            ) {
                Row(Modifier.padding(horizontal = 18.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(icon, fontSize = 16.sp, color = PG.Dust)
                    Text(label, color = PG.Star, fontSize = 15.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, maxLines = 1)
                }
            }
        }
    }
}

@Composable
internal fun PurpleGalaxySettingsFrame(navigation: @Composable () -> Unit, content: @Composable () -> Unit) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        Box(Modifier.fillMaxHeight().clip(PG.Panel).background(PG.Deep.copy(alpha = 0.8f))) { navigation() }
        Box(Modifier.weight(1f).fillMaxHeight().clip(PG.Panel).background(PG.Nebula.copy(alpha = 0.6f)).padding(20.dp)) { content() }
    }
}
