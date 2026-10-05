package com.streamvault.app.ui.themes.sabhiya

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.streamvault.data.local.entity.WatchLaterEntity
import com.streamvault.data.watchlater.WatchLaterRepository
import com.streamvault.domain.model.ContentType
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface SbWatchLaterEntryPoint {
    fun watchLaterRepository(): WatchLaterRepository
}

/** Null when Hilt is unavailable (previews / screenshot tests). */
internal fun sbWatchLaterRepo(context: Context): WatchLaterRepository? = if (SbWatchLaterPreview.force) null else runCatching {
    EntryPointAccessors.fromApplication(context.applicationContext, SbWatchLaterEntryPoint::class.java).watchLaterRepository()
}.getOrNull()

/** Test/preview hook: items shown when no repository is available. */
internal object SbWatchLaterPreview { var force = false; var items: List<WatchLaterEntity> = emptyList() }

/** Returns (isInWatchLater, toggle). Backed by the separate watch_later table, not favorites. */
@Composable
internal fun rememberSbWatchLater(providerId: Long, contentId: Long, type: ContentType, title: String, posterUrl: String?): Pair<Boolean, () -> Unit> {
    val ctx = LocalContext.current
    val repo = remember { sbWatchLaterRepo(ctx) }
    val scope = rememberCoroutineScope()
    if (repo == null) {
        var local by remember(contentId) { mutableStateOf(false) }
        return local to { local = !local }
    }
    val flow = remember(providerId, contentId, type) { repo.observeContains(providerId, contentId, type) }
    val inList by flow.collectAsState(initial = false)
    return inList to { scope.launch { repo.toggle(providerId, contentId, type, title, posterUrl) } }
}

@Composable
internal fun rememberSbWatchLaterItems(): List<WatchLaterEntity> {
    val ctx = LocalContext.current
    val repo = remember { sbWatchLaterRepo(ctx) }
    val flow = remember { repo?.observeAll() ?: flowOf(SbWatchLaterPreview.items) }
    val items by flow.collectAsState(initial = if (repo == null) SbWatchLaterPreview.items else emptyList())
    return items
}
