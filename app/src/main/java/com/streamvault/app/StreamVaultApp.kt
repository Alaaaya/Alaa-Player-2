package com.streamvault.app

import android.app.Application
import android.util.Log
import com.streamvault.app.panel.PanelArtworkNetworkFence
import com.streamvault.app.panel.PanelArtworkInterceptor
import com.streamvault.app.panel.RegistryArtworkAccess
import com.streamvault.domain.model.PanelArtworkPolicyRegistry
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import com.streamvault.app.diagnostics.CrashReportStore
import com.streamvault.app.diagnostics.RuntimeDiagnosticsManager
import com.streamvault.app.plugins.StreamVaultPluginManager
import com.streamvault.app.ui.accessibility.isReducedMotionEnabled
import com.streamvault.data.remote.jellyfin.JellyfinImageAuthInterceptor
import com.streamvault.domain.repository.DownloadManager
import com.streamvault.domain.manager.ProgramReminderManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import okio.Path.Companion.toOkioPath

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.streamvault.data.manager.recording.RecordingReconcileWorker
import com.streamvault.data.manager.PendingBackupRestoreCoordinator
import com.streamvault.data.sync.ProviderSyncWorker
import com.streamvault.data.sync.XtreamIndexWorker
import com.streamvault.data.sync.ProviderSyncLifecycle
import com.streamvault.player.timeshift.TimeshiftDiskManager
import javax.inject.Inject
import okhttp3.OkHttpClient

@HiltAndroidApp
class StreamVaultApp : Application(), SingletonImageLoader.Factory {
    private val runtimeDiagnosticsManager by lazy { RuntimeDiagnosticsManager(this) }
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Inject
    lateinit var okHttpClient: OkHttpClient

    @Inject
    lateinit var jellyfinImageAuthInterceptor: JellyfinImageAuthInterceptor

    @Inject
    lateinit var providerSyncLifecycle: ProviderSyncLifecycle

    @Inject
    lateinit var downloadManager: DownloadManager

    @Inject
    lateinit var streamVaultPluginManager: StreamVaultPluginManager

    @Inject
    lateinit var programReminderManager: ProgramReminderManager

    @Inject
    lateinit var startupWorkRegistry: StartupWorkRegistry

    @Inject
    lateinit var pendingBackupRestoreCoordinator: PendingBackupRestoreCoordinator

    @Inject
    lateinit var panelManager: dagger.Lazy<com.streamvault.app.panel.PanelManager>

    @Inject
    lateinit var databaseStartupCoordinator: DatabaseStartupCoordinator

    private val imageOkHttpClient: OkHttpClient by lazy {
        okHttpClient.newBuilder()
            .cache(null) // No independent HTTP cache may retain hidden artwork.
            .addInterceptor(PanelArtworkNetworkFence())
            .addInterceptor(jellyfinImageAuthInterceptor)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        CrashReportStore.install(this)
        runtimeDiagnosticsManager.start()
        // Workers can start this process without an Activity opening DatabaseStartupCoordinator.
        // Restore exact persisted policy independently; failure must never release either fence.
        applicationScope.launch {
            try {
                panelManager.get().restoreCatalogPolicy()
            } catch (error: kotlinx.coroutines.CancellationException) {
                throw error
            } catch (_: Exception) {
                Log.e("StreamVaultStartup", "Secure catalog policy restoration failed")
            }
        }
        applicationScope.launch {
            databaseStartupCoordinator.state
                .filterIsInstance<DatabaseStartupState.Ready>()
                .first()
            runDatabaseReadyStartupTasks()
        }
    }

    private suspend fun runDatabaseReadyStartupTasks() {
        runContainedStartupTasks(
            tasks = listOf(
                StartupTask("timeshift-cleanup") {
                    TimeshiftDiskManager(applicationContext)
                        .cleanupStaleDirectories(activeSessionDir = null)
                },
                StartupTask("download-recovery") {
                    downloadManager.recoverInterruptedDownloads()
                },
                StartupTask("plugin-reconcile") {
                    streamVaultPluginManager.reconcilePluginProviders()
                },
                StartupTask("reminder-restore") {
                    programReminderManager.restoreScheduledReminders()
                },
                StartupTask("stalker-work-reconcile") {
                    providerSyncLifecycle.reconcileStalkerIndexWorkAtStartup()
                },
                StartupTask("pending-backup-restore") {
                    pendingBackupRestoreCoordinator.applyAllAvailable()
                },
                StartupTask("work-registration") {
                    startupWorkRegistry.register()
                }
            ),
            onFailure = { name, error ->
                Log.e("StreamVaultStartup", "Startup task failed: $name", error)
            }
        )
    }

    override fun onTerminate() {
        runtimeDiagnosticsManager.stop()
        super.onTerminate()
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        val loader = ImageLoader.Builder(context)
            .components {
                add(PanelArtworkInterceptor(RegistryArtworkAccess))
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
        // Clear old unowned cache entries before any catalog reader is opened. Tracked URLs
        // never write to these caches, so a late fetch/decode cannot repopulate them.
        loader.memoryCache?.clear()
        loader.diskCache?.clear()
        PanelArtworkPolicyRegistry.addInvalidationListener {
            loader.memoryCache?.clear()
            loader.diskCache?.clear()
        }
        return loader
    }
}

internal fun dataMaintenanceConstraints(): Constraints = Constraints.Builder()
    .setRequiresBatteryNotLow(true)
    .setRequiresDeviceIdle(true)
    .build()
