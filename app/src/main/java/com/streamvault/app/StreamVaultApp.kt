package com.streamvault.app

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import com.streamvault.app.diagnostics.CrashReportStore
import com.streamvault.app.diagnostics.RuntimeDiagnosticsManager
import com.streamvault.app.update.GitHubReleaseChecker
import com.streamvault.app.update.AppUpdateCheckPolicy
import com.streamvault.app.plugins.StreamVaultPluginManager
import com.streamvault.app.ui.accessibility.isReducedMotionEnabled
import com.streamvault.data.remote.jellyfin.JellyfinImageAuthInterceptor
import com.streamvault.data.preferences.PreferencesRepository
import com.streamvault.domain.repository.DownloadManager
import com.streamvault.domain.model.Result
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okio.Path.Companion.toOkioPath

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.streamvault.data.manager.recording.RecordingReconcileWorker
import com.streamvault.data.repository.ProviderDeletionCleanupWorker
import com.streamvault.data.sync.ProviderSyncWorker
import com.streamvault.data.sync.XtreamIndexWorker
import com.streamvault.player.timeshift.TimeshiftDiskManager
import javax.inject.Inject
import okhttp3.OkHttpClient

@HiltAndroidApp
class StreamVaultApp : Application(), SingletonImageLoader.Factory {
    private val runtimeDiagnosticsManager by lazy { RuntimeDiagnosticsManager(this) }
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    @Inject
    lateinit var gitHubReleaseChecker: GitHubReleaseChecker

    @Inject
    lateinit var okHttpClient: OkHttpClient

    @Inject
    lateinit var jellyfinImageAuthInterceptor: JellyfinImageAuthInterceptor

    @Inject
    lateinit var downloadManager: DownloadManager

    @Inject
    lateinit var streamVaultPluginManager: StreamVaultPluginManager

    private val imageOkHttpClient: OkHttpClient by lazy {
        okHttpClient.newBuilder()
            .addInterceptor(jellyfinImageAuthInterceptor)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        CrashReportStore.install(this)
        runtimeDiagnosticsManager.start()
        applicationScope.launch {
            // Clean up any timeshift temp directories left behind by crashes, OOM kills, or
            // force-stops from the previous run. activeSessionDir = null means wipe everything.
            TimeshiftDiskManager(applicationContext).cleanupStaleDirectories(activeSessionDir = null)
        }
        applicationScope.launch {
            refreshCachedAppUpdateIfNeeded()
        }
        applicationScope.launch {
            downloadManager.recoverInterruptedDownloads()
        }
        applicationScope.launch {
            streamVaultPluginManager.reconcilePluginProviders()
        }
        
        // Schedule daily data maintenance: EPG pruning, stale-favorite cleanup, and DB compaction checks.
        // This work is local-only and must also run for offline/local-playlist devices.
        val gcConstraints = dataMaintenanceConstraints()

        val gcWorkRequest = PeriodicWorkRequestBuilder<com.streamvault.data.sync.SyncWorker>(24, java.util.concurrent.TimeUnit.HOURS)
            .setConstraints(gcConstraints)
            .build()
            
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DataMaintenanceWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            gcWorkRequest
        )

        ProviderSyncWorker.enqueuePeriodic(this)
        ProviderSyncWorker.enqueueLaunchStaleCheck(this)
        XtreamIndexWorker.enqueuePeriodic(this)
        XtreamIndexWorker.enqueueLaunchStaleCheck(this)
        RecordingReconcileWorker.enqueuePeriodic(this)
        RecordingReconcileWorker.enqueueOneShot(this)
        ProviderDeletionCleanupWorker.enqueue(this)
    }

    override fun onTerminate() {
        runtimeDiagnosticsManager.stop()
        super.onTerminate()
    }

    private suspend fun refreshCachedAppUpdateIfNeeded() {
        val autoCheckEnabled = preferencesRepository.autoCheckAppUpdates.first()
        if (!autoCheckEnabled) {
            return
        }

        val lastSuccessfulCheckAt = preferencesRepository.lastAppUpdateCheckTimestamp.first()
        val lastFailedCheckAt = preferencesRepository.lastAppUpdateFailureTimestamp.first()
        val now = System.currentTimeMillis()
        if (!AppUpdateCheckPolicy.shouldAutoCheck(now, lastSuccessfulCheckAt, lastFailedCheckAt)) {
            return
        }

        when (val result = gitHubReleaseChecker.fetchLatestRelease()) {
            is Result.Success -> {
                preferencesRepository.setCachedAppUpdateRelease(
                    versionName = result.data.versionName,
                    versionCode = result.data.versionCode,
                    releaseUrl = result.data.releaseUrl,
                    downloadUrl = result.data.downloadUrl,
                    downloadSha256 = result.data.downloadSha256,
                    releaseNotes = result.data.releaseNotes,
                    publishedAt = result.data.publishedAt
                )
                preferencesRepository.setLastAppUpdateCheckTimestamp(now)
                preferencesRepository.setLastAppUpdateFailureTimestamp(null)
            }
            else -> preferencesRepository.setLastAppUpdateFailureTimestamp(now)
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(
                    OkHttpNetworkFetcherFactory(
                        callFactory = { imageOkHttpClient }
                    )
                )
            }
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.15) // Conservative TV memory cache
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(this.cacheDir.resolve("image_cache").toOkioPath())
                    .maxSizeBytes(1024L * 1024L * 100L) // 100MB disk cache
                    .build()
            }
            // Limit concurrent decoding and fetching to 6 for TV hardware constraints
            .fetcherCoroutineContext(Dispatchers.IO.limitedParallelism(6))
            .decoderCoroutineContext(Dispatchers.Default.limitedParallelism(4))
            .crossfade(!isReducedMotionEnabled(context))
            .build()
    }
}

internal fun dataMaintenanceConstraints(): Constraints = Constraints.Builder()
    .setRequiresBatteryNotLow(true)
    .setRequiresDeviceIdle(true)
    .build()
