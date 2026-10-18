package com.streamvault.app.update

import androidx.core.net.toUri
import androidx.core.content.edit
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.FileProvider
import com.streamvault.app.BuildConfig
import com.streamvault.data.preferences.PreferencesRepository
import com.streamvault.domain.model.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

data class AppUpdateDownloadState(
    val status: AppUpdateDownloadStatus = AppUpdateDownloadStatus.Idle,
    val versionName: String? = null,
    val downloadId: Long? = null,
    val installPermissionRequired: Boolean = false,
    val progress: Float? = null
)

enum class AppUpdateDownloadStatus {
    Idle,
    Downloading,
    Downloaded,
    Failed
}

@Singleton
class AppUpdateInstaller @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: PreferencesRepository
) {
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _downloadState = MutableStateFlow(AppUpdateDownloadState())
    private var downloadPollingJob: Job? = null
    private val installMutex = kotlinx.coroutines.sync.Mutex()
    private val metadata = context.getSharedPreferences("update-integrity", Context.MODE_PRIVATE)
    private val downloadCompleteReceiver = object : BroadcastReceiver() {
        override fun onReceive(receiverContext: Context?, intent: Intent?) {
            if (intent?.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) return
            val completedDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            val trackedDownloadId = _downloadState.value.downloadId ?: return
            if (completedDownloadId != trackedDownloadId) return
            scope.launch {
                refreshState()
            }
        }
    }

    val downloadState: StateFlow<AppUpdateDownloadState> = _downloadState.asStateFlow()

    init {
        registerDownloadReceiver()
        scope.launch {
            refreshState()
        }
    }

    suspend fun refreshState(): AppUpdateDownloadState = withContext(Dispatchers.IO) {
        val downloadId = preferencesRepository.appUpdateDownloadId.first()
        val downloadingVersionName = preferencesRepository.appUpdateDownloadVersionName.first()
        val downloadedVersionName = preferencesRepository.downloadedAppUpdateVersionName.first()
        val apkFile = downloadedVersionName?.let(::apkFileForVersion)

        if (downloadId == null) {
            preferencesRepository.setAppUpdateDownloadVersionName(null)
            val restoredState = if (downloadedVersionName != null && apkFile?.exists() == true) {
                downloadedState(downloadedVersionName)
            } else {
                if (downloadedVersionName != null) {
                    preferencesRepository.setDownloadedAppUpdateVersionName(null)
                }
                AppUpdateDownloadState()
            }
            _downloadState.value = restoredState
            return@withContext restoredState
        }

        val trackedVersionName = downloadingVersionName ?: downloadedVersionName
        val query = DownloadManager.Query().setFilterById(downloadId)
        downloadManager.query(query).use { cursor ->
            if (!cursor.moveToFirst()) {
                preferencesRepository.setAppUpdateDownloadId(null)
                preferencesRepository.setAppUpdateDownloadVersionName(null)
                val fallbackState = if (downloadedVersionName != null && apkFile?.exists() == true) {
                    downloadedState(downloadedVersionName)
                } else {
                    preferencesRepository.setDownloadedAppUpdateVersionName(null)
                    AppUpdateDownloadState(status = AppUpdateDownloadStatus.Failed)
                }
                _downloadState.value = fallbackState
                return@withContext fallbackState
            }

            val statusColumn = cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
            val status = cursor.getInt(statusColumn)
            val state = when (status) {
                DownloadManager.STATUS_PENDING,
                DownloadManager.STATUS_PAUSED,
                DownloadManager.STATUS_RUNNING -> AppUpdateDownloadState(
                    status = AppUpdateDownloadStatus.Downloading,
                    versionName = trackedVersionName,
                    downloadId = downloadId,
                    progress = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        .takeIf { it > 0 }?.let { total ->
                            (cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                                .toFloat() / total).coerceIn(0f, 1f)
                        }
                )

                DownloadManager.STATUS_SUCCESSFUL -> {
                    preferencesRepository.setAppUpdateDownloadId(null)
                    preferencesRepository.setAppUpdateDownloadVersionName(null)
                    val completedApkFile = trackedVersionName?.let(::apkFileForVersion)
                    if (trackedVersionName != null && completedApkFile?.exists() == true) {
                        preferencesRepository.setDownloadedAppUpdateVersionName(trackedVersionName)
                        downloadedState(trackedVersionName)
                    } else {
                        preferencesRepository.setDownloadedAppUpdateVersionName(null)
                        AppUpdateDownloadState(
                            status = AppUpdateDownloadStatus.Failed,
                            versionName = trackedVersionName,
                            downloadId = null
                        )
                    }
                }

                else -> {
                    preferencesRepository.setAppUpdateDownloadId(null)
                    preferencesRepository.setAppUpdateDownloadVersionName(null)
                    if (trackedVersionName == downloadedVersionName) {
                        preferencesRepository.setDownloadedAppUpdateVersionName(null)
                    }
                    AppUpdateDownloadState(
                        status = AppUpdateDownloadStatus.Failed,
                        versionName = trackedVersionName,
                        downloadId = null
                    )
                }
            }
            _downloadState.value = state
            syncPollingForState(state)
            return@withContext state
        }
    }

    suspend fun startDownload(releaseInfo: GitHubReleaseInfo): Result<Unit> = withContext(Dispatchers.IO) {
        val downloadUrl = releaseInfo.downloadUrl
            ?: return@withContext Result.error("Update download is unavailable for this release")
        if (!isHttpsUrl(downloadUrl)) {
            return@withContext Result.error("Update download is unavailable because the download URL is not HTTPS")
        }

        if (releaseInfo.downloadSha256?.matches(Regex("[a-fA-F0-9]{64}")) != true) {
            return@withContext Result.error("Update checksum is unavailable")
        }
        try {
            val targetFile = apkFileForVersion(releaseInfo.versionName)
            targetFile.parentFile?.mkdirs()
            if (targetFile.exists()) {
                targetFile.delete()
            }

            val existingVersion = preferencesRepository.downloadedAppUpdateVersionName.first()
            if (existingVersion != null && existingVersion != releaseInfo.versionName) {
                apkFileForVersion(existingVersion).delete()
            }
            preferencesRepository.appUpdateDownloadId.first()?.let { oldDownloadId ->
                runCatching { downloadManager.remove(oldDownloadId) }
            }

            val request = DownloadManager.Request(Uri.parse(downloadUrl))
                .setTitle("Alaa Player 2 ${releaseInfo.versionName}")
                .setDescription("Downloading the latest Alaa Player 2 update")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setMimeType("application/vnd.android.package-archive")
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setDestinationInExternalFilesDir(
                    context,
                    Environment.DIRECTORY_DOWNLOADS,
                    targetFile.name
                )

            // Persist integrity metadata before enqueuing work that can outlive this process.
            metadata.edit(commit = true) { putString(releaseInfo.versionName, releaseInfo.downloadSha256) }
            val downloadId = downloadManager.enqueue(request)
            preferencesRepository.setAppUpdateDownloadId(downloadId)
            preferencesRepository.setAppUpdateDownloadVersionName(releaseInfo.versionName)
            preferencesRepository.setDownloadedAppUpdateVersionName(null)
            val state = AppUpdateDownloadState(
                status = AppUpdateDownloadStatus.Downloading,
                versionName = releaseInfo.versionName,
                downloadId = downloadId
            )
            _downloadState.value = state
            syncPollingForState(state)
            Result.success(Unit)
        } catch (error: IllegalArgumentException) {
            Result.error("Failed to start update download", error)
        } catch (error: SecurityException) {
            Result.error("Update download requires additional permissions", error)
        }
    }

    suspend fun installDownloadedUpdate(expectedSha256: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        if (!installMutex.tryLock()) return@withContext Result.error("Installation already in progress")
        try {
        val activeId = metadata.getInt("active-session", -1)
        if (activeId >= 0 && context.packageManager.packageInstaller.getSessionInfo(activeId) != null) {
            return@withContext Result.error("Installation awaiting Android confirmation")
        }
        metadata.edit(commit = true) { remove("active-session") }
        val currentState = refreshState()
        if (currentState.status != AppUpdateDownloadStatus.Downloaded || currentState.versionName.isNullOrBlank()) {
            return@withContext Result.error("No downloaded update is ready to install")
        }

        if (requiresInstallPermission()) {
            metadata.edit(commit = true) { putBoolean("pending-permission", true) }
            val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = "package:${context.packageName}".toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val permissionRequiredState = currentState.copy(installPermissionRequired = true)
            _downloadState.value = permissionRequiredState
            return@withContext try {
                context.startActivity(settingsIntent)
                Result.error("Allow installs from this app, then return to the app")
            } catch (error: ActivityNotFoundException) {
                _downloadState.value = currentState.copy(installPermissionRequired = false)
                metadata.edit(commit = true) { putBoolean("pending-permission", false) }
                Result.error("Open Android settings, allow installs from this app, then return to the app", error)
            } catch (error: SecurityException) {
                _downloadState.value = currentState.copy(installPermissionRequired = false)
                metadata.edit(commit = true) { putBoolean("pending-permission", false) }
                Result.error("The install permission settings screen could not be opened", error)
            }
        }

        metadata.edit(commit = true) { putBoolean("pending-permission", false) }
        val apkFile = apkFileForVersion(currentState.versionName)
        if (!apkFile.exists()) {
            preferencesRepository.setAppUpdateDownloadId(null)
            preferencesRepository.setAppUpdateDownloadVersionName(null)
            preferencesRepository.setDownloadedAppUpdateVersionName(null)
            return@withContext Result.error("Downloaded update file is missing")
        }

        // SEC-L02: Verify SHA-256 integrity before handing the APK to the package manager.
        // This guards against a truncated download, a network MITM, or a tampered file in
        // the external storage directory (which is world-readable on unencrypted devices).
        val trustedHash = metadata.getString(currentState.versionName, null) ?: expectedSha256
        if (trustedHash?.matches(Regex("[a-fA-F0-9]{64}")) != true) {
            return@withContext Result.error("Update checksum is unavailable; please download again")
        }
        if (trustedHash.isNotBlank()) {
            val actualHash = computeSha256Hex(apkFile)
            if (!actualHash.equals(trustedHash.trim(), ignoreCase = true)) {
                android.util.Log.e(
                    "AppUpdateInstaller",
                    "APK SHA-256 mismatch for ${apkFile.name}: expected=${trustedHash.trim()} actual=$actualHash"
                )
                apkFile.delete()
                preferencesRepository.setAppUpdateDownloadId(null)
                preferencesRepository.setAppUpdateDownloadVersionName(null)
                preferencesRepository.setDownloadedAppUpdateVersionName(null)
                return@withContext Result.error(
                    "Downloaded update failed integrity check. The file has been removed; please download again."
                )
            }
            android.util.Log.i("AppUpdateInstaller", "APK SHA-256 verified OK for ${apkFile.name}")
        }

        val apkUri = FileProvider.getUriForFile(
            context,
            "${BuildConfig.APPLICATION_ID}.fileprovider",
            apkFile
        )

        val installer = context.packageManager.packageInstaller
        var sessionId: Int? = null
        try {
            val params = android.content.pm.PackageInstaller.SessionParams(
                android.content.pm.PackageInstaller.SessionParams.MODE_FULL_INSTALL
            ).apply { setAppPackageName(context.packageName) }
            val id = installer.createSession(params)
            sessionId = id
            installer.openSession(id).use { session ->
                context.contentResolver.openInputStream(apkUri).use { input ->
                    requireNotNull(input) { "Downloaded update could not be opened" }
                    session.openWrite("base.apk", 0, apkFile.length()).use { output ->
                        input.copyTo(output)
                        session.fsync(output)
                    }
                }
                val callback = Intent(context, UpdateInstallResultActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val flags = android.app.PendingIntent.FLAG_UPDATE_CURRENT or
                    (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) android.app.PendingIntent.FLAG_MUTABLE else 0)
                val options = if (Build.VERSION.SDK_INT >= 35) {
                    android.app.ActivityOptions.makeBasic().apply {
                        setPendingIntentCreatorBackgroundActivityStartMode(
                            if (Build.VERSION.SDK_INT >= 36)
                                android.app.ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOW_IF_VISIBLE
                            else android.app.ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                        )
                    }.toBundle()
                } else null
                val result = android.app.PendingIntent.getActivity(context, id, callback, flags, options)
                metadata.edit(commit = true) { putInt("active-session", id) }
                session.commit(result.intentSender)
            }
            Result.success(Unit)
        } catch (error: Exception) {
            sessionId?.let { runCatching { installer.abandonSession(it) } }
            Result.error("The package installer could not be launched", error)
        }
        } catch (error: Exception) {
            Result.error("Downloaded update could not be read or installed", error)
        } finally { installMutex.unlock() }
    }

    suspend fun resumeAfterInstallPermission() {
        if (metadata.getBoolean("pending-permission", false) && !requiresInstallPermission()) {
            installDownloadedUpdate()
        }
    }

    private fun isHttpsUrl(url: String): Boolean {
        val normalized = url.trim()
        if (normalized.isBlank()) return false
        return runCatching {
            val parsed = URI(normalized)
            parsed.scheme.equals("https", ignoreCase = true) && !parsed.host.isNullOrBlank()
        }.getOrDefault(false)
    }

    private fun computeSha256Hex(file: File): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        file.inputStream().buffered(DEFAULT_BUFFER_SIZE).use { stream ->
            val buffer = ByteArray(8192)
            var read: Int
            while (stream.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun apkFileForVersion(versionName: String): File {
        val sanitizedVersion = versionName.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: File(context.cacheDir, "downloads")
        return File(downloadsDir, "Alaa Player 2-$sanitizedVersion.apk")
    }

    private fun downloadedState(versionName: String): AppUpdateDownloadState {
        return AppUpdateDownloadState(
            status = AppUpdateDownloadStatus.Downloaded,
            versionName = versionName,
            downloadId = null,
            installPermissionRequired = requiresInstallPermission()
        )
    }

    private fun requiresInstallPermission(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !context.packageManager.canRequestPackageInstalls()
    }

    private fun registerDownloadReceiver() {
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(downloadCompleteReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            context.registerReceiver(downloadCompleteReceiver, filter)
        }
    }

    /**
     * Unregisters the [DownloadManager] broadcast receiver.
     * Must be called when this singleton is torn down (e.g. in instrumentation tests that
     * destroy and recreate the DI graph) to prevent duplicate receiver registrations.
     */
    fun unregister() {
        runCatching { context.unregisterReceiver(downloadCompleteReceiver) }
    }

    private fun syncPollingForState(state: AppUpdateDownloadState) {
        if (state.status != AppUpdateDownloadStatus.Downloading || state.downloadId == null) {
            downloadPollingJob?.cancel()
            downloadPollingJob = null
            return
        }

        if (downloadPollingJob?.isActive == true) return

        downloadPollingJob = scope.launch {
            while (isActive) {
                delay(1500)
                val refreshed = refreshState()
                if (refreshed.status != AppUpdateDownloadStatus.Downloading) {
                    break
                }
            }
        }
    }
}
