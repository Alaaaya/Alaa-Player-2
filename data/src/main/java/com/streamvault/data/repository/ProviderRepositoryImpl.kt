package com.streamvault.data.repository

import android.content.Context
import com.google.gson.Gson
import com.streamvault.data.local.dao.ProviderDeletionCleanupDao
import com.streamvault.data.local.dao.ProviderConfigRevisionDao
import com.streamvault.data.local.entity.ProviderDeletionCleanupEntity
import com.streamvault.data.local.entity.ProviderConfigRevisionEntity
import com.streamvault.data.local.entity.ProviderConfigRevisionState
import com.streamvault.data.local.DatabaseTransactionRunner
import com.streamvault.data.local.dao.*
import com.streamvault.data.local.entity.ProviderEntity
import com.streamvault.data.manager.recording.RecordingAlarmScheduler
import com.streamvault.data.manager.reminder.ProgramReminderAlarmScheduler
import com.streamvault.data.mapper.*
import com.streamvault.data.preferences.PreferencesRepository
import com.streamvault.data.remote.http.buildGenericProviderRequestProfile
import com.streamvault.data.remote.jellyfin.JellyfinProvider
import com.streamvault.data.remote.stalker.StalkerApiService
import com.streamvault.data.remote.stalker.StalkerPlaybackMode
import com.streamvault.data.remote.stalker.StalkerProvider
import com.streamvault.data.remote.xtream.XtreamApiService
import com.streamvault.data.remote.xtream.XtreamProvider
import com.streamvault.data.security.CredentialCrypto
import com.streamvault.data.security.CredentialDecryptionException
import com.streamvault.data.sync.SyncManager
import com.streamvault.data.sync.ProviderSyncWorker
import com.streamvault.data.sync.ProviderWorkflowDisposition
import com.streamvault.data.sync.ProviderWorkflowOutcome
import com.streamvault.data.sync.ProviderWorkflowRunner
import com.streamvault.data.sync.ProviderWorkflowCommitFence
import com.streamvault.data.sync.hasUsableLiveCatalogForActivation
import com.streamvault.data.local.entity.ProviderWorkflowPhase
import com.streamvault.data.local.entity.ProviderWorkflowReason
import com.streamvault.data.util.ProviderInputSanitizer
import com.streamvault.data.util.UrlSecurityPolicy
import com.streamvault.domain.manager.ProviderCredentials
import com.streamvault.domain.model.*
import com.streamvault.domain.provider.IptvProvider
import com.streamvault.domain.repository.LiveStreamProgramRequest
import com.streamvault.domain.repository.ProviderDeleteOutcome
import com.streamvault.domain.repository.ProviderDeleteProgress
import com.streamvault.domain.repository.ProviderRepository
import com.streamvault.domain.repository.SyncMetadataRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class ProviderRepositoryImpl @Inject constructor(
    private val providerDao: ProviderDao,
    private val categoryDao: CategoryDao,
    private val channelDao: ChannelDao,
    private val movieDao: MovieDao,
    private val seriesDao: SeriesDao,
    private val programDao: ProgramDao,
    private val recordingRunDao: RecordingRunDao,
    private val programReminderDao: ProgramReminderDao,
    private val stalkerApiService: StalkerApiService,
    private val xtreamApiService: XtreamApiService,
    private val credentialCrypto: CredentialCrypto,
    private val preferencesRepository: PreferencesRepository,
    private val syncManager: SyncManager,
    private val syncMetadataRepository: SyncMetadataRepository,
    private val transactionRunner: DatabaseTransactionRunner,
    private val recordingAlarmScheduler: RecordingAlarmScheduler,
    private val programReminderAlarmScheduler: ProgramReminderAlarmScheduler,
    private val jellyfinProvider: JellyfinProvider,
    private val providerDeletionCleanupDao: ProviderDeletionCleanupDao,
    private val providerDeletionCleanupEnqueuer: ProviderDeletionCleanupEnqueuer,
    private val providerConfigRevisionDao: ProviderConfigRevisionDao,
    private val gson: Gson,
    private val providerWorkflowRunner: ProviderWorkflowRunner,
    private val providerWorkflowCommitFence: ProviderWorkflowCommitFence,
    @param:ApplicationContext private val appContext: Context
) : ProviderRepository {
    private companion object {
        const val XTREAM_GUIDE_BATCH_CONCURRENCY = 4
        const val BACKGROUND_EPG_START_DELAY_MS = 15_000L
        // Row-equivalent weights for non-row delete steps so the progress bar still moves
        // meaningfully on providers with tiny (or empty) catalogs.
        const val ALARM_STEP_WEIGHT = 5
        const val PROVIDER_ROW_STEP_WEIGHT = 200
        const val FINALIZE_STEP_WEIGHT = 200
        const val MANUAL_REFRESH_PRIORITY = 50
        val logger: Logger = Logger.getLogger(ProviderRepositoryImpl::class.java.name)
    }

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private data class PendingProviderEdit(
        val revision: Long,
        val candidate: Provider,
        val secureCandidate: ProviderEntity
    )

    private data class InitialOnboardingTarget(
        val providerData: Provider,
        val providerForSync: Provider,
        val pendingEdit: PendingProviderEdit? = null
    )

    override fun getProviders(): Flow<List<Provider>> =
        providerDao.getAll().map { entities -> entities.map { it.toPublicDomain() } }

    override fun getActiveProvider(): Flow<Provider?> =
        providerDao.getActive().map { it?.toPublicDomain() }

    override suspend fun getProvider(id: Long): Provider? =
        providerDao.getById(id)?.toPublicDomain()

    override suspend fun addProvider(provider: Provider): Result<Long> = try {
        val id = providerDao.insert(provider.toSecureEntity())
        Result.success(id)
    } catch (e: Exception) {
        Result.error("Failed to add provider: ${e.message}", e)
    }

    override suspend fun updateProvider(provider: Provider): Result<Unit> = try {
        providerDao.update(provider.toSecureEntity())
        Result.success(Unit)
    } catch (e: Exception) {
        Result.error("Failed to update provider: ${e.message}", e)
    }

    override suspend fun getAllProviderCredentials(): List<ProviderCredentials> {
        return providerDao.getAllSync()
            .map { entity ->
                ProviderCredentials(
                    serverUrl = entity.serverUrl,
                    username = entity.username,
                    password = try {
                        credentialCrypto.decryptIfNeeded(entity.password)
                    } catch (e: Throwable) {
                        ""
                    },
                )
            }
            .filter { it.username.isNotBlank() && it.password.isNotBlank() }
    }

    override suspend fun updateProviderPassword(
        serverUrl: String,
        username: String,
        cleartextPassword: String,
    ): Boolean {
        val entity = providerDao.getAllSync().firstOrNull {
            it.serverUrl == serverUrl && it.username == username
        } ?: return false
        val encrypted = credentialCrypto.encryptIfNeeded(cleartextPassword)
        providerDao.update(entity.copy(password = encrypted))
        return true
    }

    override suspend fun deleteProvider(
        id: Long,
        onProgress: ((ProviderDeleteProgress) -> Unit)?
    ): Result<ProviderDeleteOutcome> = try {
        val estimatedRecordingRunIds = recordingRunDao.getIdsByProvider(id)
        val estimatedReminderIds = programReminderDao.getIdsByProvider(id)

        // Weight progress by the real row counts of the large child tables so the bar
        // advances proportionally to the work being done instead of in two big jumps.
        val programCount = programDao.countByProvider(id)
        val channelCount = channelDao.countByProvider(id)
        val movieCount = movieDao.countByProvider(id)
        val seriesCount = seriesDao.countByProvider(id)

        val totalWeight = (
                programCount + channelCount + movieCount + seriesCount +
                (estimatedRecordingRunIds.size + estimatedReminderIds.size) * ALARM_STEP_WEIGHT +
                PROVIDER_ROW_STEP_WEIGHT + FINALIZE_STEP_WEIGHT
            ).coerceAtLeast(1)
        var completedWeight = 0
        var pendingCleanupActions = 0

        fun reportProgress(message: String) {
            onProgress?.invoke(
                ProviderDeleteProgress(
                    message = message,
                    fraction = (completedWeight.toFloat() / totalWeight.toFloat()).coerceIn(0f, 1f)
                )
            )
        }

        reportProgress("Preparing to remove provider...")
        transactionRunner.inTransaction {
            // Re-read alarm identities under the same Room transaction that removes
            // their rows. This closes the window where a newly committed alarm could
            // be cascade-deleted without receiving a durable cancellation tombstone.
            val recordingRunIds = recordingRunDao.getIdsByProvider(id)
            val reminderIds = programReminderDao.getIdsByProvider(id)
            providerDeletionCleanupDao.insertAll(
                recordingRunIds.map { ProviderDeletionCleanupEntity(id = 0, providerId = id, action = ProviderDeletionCleanupWorker.RECORDING_ALARM, targetId = it) } +
                    reminderIds.map { ProviderDeletionCleanupEntity(id = 0, providerId = id, action = ProviderDeletionCleanupWorker.REMINDER_ALARM, targetId = it.toString()) } +
                    ProviderDeletionCleanupEntity(id = 0, providerId = id, action = ProviderDeletionCleanupWorker.SYNC_RUNTIME)
            )
            // ProgramEntity still has no provider FK, so it requires explicit cleanup.
            if (programCount > 0) reportProgress("Removing $programCount guide entries...")
            programDao.deleteByProvider(id)
            completedWeight += programCount

            if (channelCount > 0) reportProgress("Removing $channelCount channels...")
            channelDao.deleteByProvider(id)
            completedWeight += channelCount

            if (movieCount > 0) reportProgress("Removing $movieCount movies...")
            movieDao.deleteByProvider(id)
            completedWeight += movieCount

            if (seriesCount > 0) reportProgress("Removing $seriesCount series...")
            seriesDao.deleteByProvider(id)
            completedWeight += seriesCount

            reportProgress("Removing provider record...")
            providerDao.delete(id)
            completedWeight += PROVIDER_ROW_STEP_WEIGHT
            pendingCleanupActions = providerDeletionCleanupDao.countByProvider(id)
        }
        reportProgress("Provider library removed.")
        reportProgress("Finalizing provider cleanup...")
        val reconciliationRequested = runCatching {
            providerDeletionCleanupEnqueuer.enqueue()
            true
        }.getOrElse {
            logger.warning("Provider cleanup is pending but WorkManager enqueue failed: ${it.message}")
            false
        }
        completedWeight = totalWeight
        reportProgress(
            if (reconciliationRequested) {
                "Provider library deleted; final cleanup continues."
            } else {
                "Provider library deleted; cleanup is pending and will retry on startup."
            }
        )
        Result.success(
            ProviderDeleteOutcome(
                providerId = id,
                pendingCleanupActions = pendingCleanupActions,
                reconciliationRequested = reconciliationRequested
            )
        )
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (e: Exception) {
        Result.error("Failed to delete provider: ${e.message}", e)
    }

    override suspend fun setActiveProvider(id: Long): Result<Unit> {
        return try {
            val provider = providerDao.getById(id)
                ?: return Result.error("Provider not found")
            if (!hasUsableLiveCatalogForActivation(id, provider.type, channelDao, categoryDao, syncMetadataRepository)) {
                syncManager.scheduleProviderSyncResume(id)
                return Result.error("Provider is saved but no content has been committed yet. Sync will resume in background.")
            }
            providerDao.setActive(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Failed to set active provider: ${e.message}", e)
        }
    }

    override suspend fun loginXtream(
        serverUrl: String,
        username: String,
        password: String,
        name: String,
        httpUserAgent: String,
        httpHeaders: String,
        xtreamFastSyncEnabled: Boolean,
        epgSyncMode: ProviderEpgSyncMode,
        xtreamLiveSyncMode: com.streamvault.domain.model.ProviderXtreamLiveSyncMode,
        guideSourcePolicy: GuideSourcePolicy,
        channelLogoSourcePolicy: ChannelLogoSourcePolicy,
        onProgress: ((String) -> Unit)?,
        id: Long?
    ): Result<Provider> {
        val normalizedServerUrl = ProviderInputSanitizer.normalizeUrl(serverUrl)
        val normalizedUsername = ProviderInputSanitizer.normalizeUsername(username)
        val normalizedName = ProviderInputSanitizer.normalizeProviderName(name)
        val resolvedServerUrl = ProviderInputSanitizer.resolveUrlProtocol(normalizedServerUrl)

        ProviderInputSanitizer.validateUrl(resolvedServerUrl)?.let { message ->
            return Result.error(message)
        }
        UrlSecurityPolicy.validateXtreamServerUrl(resolvedServerUrl)?.let { message ->
            return Result.error(message)
        }
        onProgress?.invoke("Authenticating...")
        val existingProvider = if (id != null) {
            // Edit path: check that the new normalized identity does not collide with a
            // different provider before we commit the update.
            val collision = providerDao.getByUrlAndUser(resolvedServerUrl, normalizedUsername)
            if (collision != null && collision.id != id) {
                return Result.error("A provider with this server URL and username already exists.")
            }
            providerDao.getById(id)
        } else {
            providerDao.getByUrlAndUser(resolvedServerUrl, normalizedUsername)
        }
        val effectivePassword = try {
            password.takeIf { it.isNotBlank() }
                ?: existingProvider?.password?.let(credentialCrypto::decryptIfNeeded)
                ?: ""
        } catch (e: CredentialDecryptionException) {
            return Result.error(e.message ?: CredentialDecryptionException.MESSAGE, e)
        }
        val provider = createXtreamProvider(
            providerId = 0,
            serverUrl = resolvedServerUrl,
            username = normalizedUsername,
            password = effectivePassword,
            httpUserAgent = httpUserAgent,
            httpHeaders = httpHeaders
        )
        return when (val authResult = provider.authenticate()) {
            is Result.Success -> {
                val onboardingTarget = if (existingProvider != null) {
                    onProgress?.invoke("Updating existing provider...")
                    val updated = authResult.data.copy(
                        id = existingProvider.id,
                        name = normalizedName.ifBlank { existingProvider.name },
                        serverUrl = resolvedServerUrl,
                        username = normalizedUsername,
                        password = effectivePassword,
                        httpUserAgent = httpUserAgent,
                        httpHeaders = httpHeaders,
                        epgSyncMode = epgSyncMode,
                        xtreamLiveSyncMode = xtreamLiveSyncMode,
                        guideSourcePolicy = guideSourcePolicy,
                        channelLogoSourcePolicy = channelLogoSourcePolicy,
                        xtreamFastSyncEnabled = false,
                        isActive = false,
                        status = ProviderStatus.PARTIAL,
                        lastSyncedAt = 0,
                        createdAt = existingProvider.createdAt
                    )
                    InitialOnboardingTarget(
                        providerData = updated.copy(password = ""),
                        providerForSync = updated,
                        pendingEdit = stageProviderEdit(updated)
                    )
                } else {
                    val newData = authResult.data.copy(
                        name = normalizedName.ifBlank { authResult.data.name },
                        httpUserAgent = httpUserAgent,
                        httpHeaders = httpHeaders,
                        epgSyncMode = epgSyncMode,
                        xtreamLiveSyncMode = xtreamLiveSyncMode,
                        guideSourcePolicy = guideSourcePolicy,
                        channelLogoSourcePolicy = channelLogoSourcePolicy,
                        xtreamFastSyncEnabled = false,
                        isActive = false,
                        status = ProviderStatus.PARTIAL
                    )
                    val newId = providerDao.insert(newData.toSecureEntity())
                    InitialOnboardingTarget(
                        providerData = newData.copy(id = newId, password = ""),
                        providerForSync = newData.copy(id = newId)
                    )
                }

                handleInitialOnboardingSync(
                    providerData = onboardingTarget.providerData,
                    syncResult = syncInitialOnboarding(
                        onboardingTarget,
                        onProgress,
                        trackInitialLiveOnboarding = true
                    ),
                    syncFailurePrefix = "Provider login succeeded, but initial sync failed. The provider was saved and can be retried from Settings",
                    pendingEdit = onboardingTarget.pendingEdit
                )
            }
            is Result.Error -> Result.error(authResult.message, authResult.exception)
            is Result.Loading -> Result.error("Unexpected loading state")
        }
    }

    override suspend fun validateM3u(
        url: String,
        name: String,
        httpUserAgent: String,
        httpHeaders: String,
        epgSyncMode: ProviderEpgSyncMode,
        m3uVodClassificationEnabled: Boolean,
        guideSourcePolicy: GuideSourcePolicy,
        channelLogoSourcePolicy: ChannelLogoSourcePolicy,
        onProgress: ((String) -> Unit)?,
        id: Long?
    ): Result<Provider> = try {
        val normalizedUrl = ProviderInputSanitizer.resolveUrlProtocol(
            ProviderInputSanitizer.normalizeUrl(url)
        )
        val normalizedName = ProviderInputSanitizer.normalizeProviderName(name)

        ProviderInputSanitizer.validateUrl(normalizedUrl)?.let { message ->
            return Result.error(message)
        }
        UrlSecurityPolicy.validatePlaylistSourceUrl(normalizedUrl)?.let { message ->
            return Result.error(message)
        }
        onProgress?.invoke("Validating playlist URL...")
        val providerName = normalizedName.ifBlank {
            normalizedUrl.substringAfterLast("/").substringBefore("?").ifBlank { "M3U Playlist" }
        }

        val existingProvider = if (id != null) {
            // Edit path: check that the new normalized URL does not collide with a different
            // provider before we commit the update.
            val collision = providerDao.getByUrlAndUser(normalizedUrl, "")
            if (collision != null && collision.id != id) {
                return Result.error("A playlist provider with this URL already exists.")
            }
            providerDao.getById(id)
        } else {
            providerDao.getByUrlAndUser(normalizedUrl, "")
        }

        val onboardingTarget = if (existingProvider != null) {
            val updated = existingProvider.copy(
                name = if (normalizedName.isNotBlank()) normalizedName else existingProvider.name,
                serverUrl = normalizedUrl,
                m3uUrl = normalizedUrl,
                httpUserAgent = httpUserAgent,
                httpHeaders = httpHeaders,
                epgSyncMode = epgSyncMode,
                m3uVodClassificationEnabled = m3uVodClassificationEnabled,
                guideSourcePolicy = guideSourcePolicy,
                channelLogoSourcePolicy = channelLogoSourcePolicy,
                isActive = false,
                status = ProviderStatus.PARTIAL,
                lastSyncedAt = 0
            )
            InitialOnboardingTarget(
                providerData = updated.toPublicDomain(),
                providerForSync = updated.toDomain(),
                pendingEdit = stageProviderEdit(updated.toDomain())
            )
        } else {
            val provider = Provider(
                name = providerName,
                type = ProviderType.M3U,
                serverUrl = normalizedUrl,
                m3uUrl = normalizedUrl,
                httpUserAgent = httpUserAgent,
                httpHeaders = httpHeaders,
                epgSyncMode = epgSyncMode,
                m3uVodClassificationEnabled = m3uVodClassificationEnabled,
                guideSourcePolicy = guideSourcePolicy,
                channelLogoSourcePolicy = channelLogoSourcePolicy,
                isActive = false,
                status = ProviderStatus.PARTIAL
            )
            val newId = providerDao.insert(provider.toSecureEntity())
            InitialOnboardingTarget(
                providerData = provider.copy(id = newId, password = ""),
                providerForSync = provider.copy(id = newId)
            )
        }

        handleInitialOnboardingSync(
            providerData = onboardingTarget.providerData,
            syncResult = syncInitialOnboarding(onboardingTarget, onProgress),
            syncFailurePrefix = "Playlist saved, but initial sync failed. The provider was saved and can be retried from Settings",
            pendingEdit = onboardingTarget.pendingEdit
        )
    } catch (e: Exception) {
        Result.error("Failed to add M3U provider: ${e.message}", e)
    }

    override suspend fun loginJellyfin(
        serverUrl: String,
        username: String,
        password: String,
        name: String,
        onProgress: ((String) -> Unit)?,
        id: Long?
    ): Result<Provider> {
        return try {
            val normalizedServerUrl = ProviderInputSanitizer.resolveUrlProtocol(
                ProviderInputSanitizer.normalizeUrl(serverUrl)
            )
            val normalizedUsername = ProviderInputSanitizer.normalizeUsername(username)
            val normalizedPassword = ProviderInputSanitizer.normalizePassword(password)
            val normalizedName = ProviderInputSanitizer.normalizeProviderName(name)
            ProviderInputSanitizer.validateUrl(normalizedServerUrl)?.let { return Result.error(it) }
            if (normalizedUsername.isBlank()) return Result.error("Please enter Jellyfin username")
            val providerName = normalizedName.ifBlank {
                normalizedServerUrl.substringAfter("//").substringBefore("/").ifBlank { "Jellyfin" }
            }
            val existingProviderEntity = if (id != null) {
                val collision = providerDao.getByUrlAndUser(normalizedServerUrl, normalizedUsername)
                if (collision != null && collision.id != id) return Result.error("A Jellyfin provider with this server URL and username already exists.")
                providerDao.getById(id)
            } else {
                providerDao.getByUrlAndUser(normalizedServerUrl, normalizedUsername)
            }
            val existingProvider = existingProviderEntity?.toDomain()
            val authResult = when {
                normalizedPassword.isNotBlank() -> {
                    onProgress?.invoke("Signing in to Jellyfin...")
                    when (val loginResult = jellyfinProvider.authenticate(normalizedServerUrl, normalizedUsername, normalizedPassword)) {
                        is Result.Success -> loginResult.data
                        is Result.Error -> return Result.error(loginResult.message, loginResult.exception)
                        is Result.Loading -> return Result.error("Unexpected loading state")
                    }
                }
                existingProvider != null -> try { credentialCrypto.decryptIfNeeded(existingProvider.password) }
                    catch (e: CredentialDecryptionException) { return Result.error(e.message ?: CredentialDecryptionException.MESSAGE, e) }
                else -> return Result.error("Please enter Jellyfin password")
            }
            val onboardingTarget = if (existingProvider != null) {
                val updated = existingProvider.copy(
                    name = providerName.ifBlank { existingProvider.name }, type = ProviderType.JELLYFIN,
                    serverUrl = normalizedServerUrl, username = normalizedUsername, password = authResult,
                    m3uUrl = "", epgUrl = "", httpUserAgent = "", httpHeaders = "",
                    isActive = false, status = ProviderStatus.PARTIAL, lastSyncedAt = 0
                )
                InitialOnboardingTarget(
                    providerData = updated.copy(password = ""),
                    providerForSync = updated,
                    pendingEdit = stageProviderEdit(updated)
                )
            } else {
                val provider = Provider(name = providerName, type = ProviderType.JELLYFIN,
                    serverUrl = normalizedServerUrl, username = normalizedUsername, password = authResult,
                    isActive = false, status = ProviderStatus.PARTIAL)
                val newId = providerDao.insert(provider.toSecureEntity())
                InitialOnboardingTarget(
                    providerData = provider.copy(id = newId, password = ""),
                    providerForSync = provider.copy(id = newId)
                )
            }
            handleInitialOnboardingSync(
                providerData = onboardingTarget.providerData,
                syncResult = syncInitialOnboarding(onboardingTarget, onProgress),
                syncFailurePrefix = "Jellyfin provider saved, but initial sync failed. The provider was saved and can be retried from Settings",
                pendingEdit = onboardingTarget.pendingEdit
            )
        } catch (e: Exception) {
            Result.error("Failed to add Jellyfin provider: ${e.message}", e)
        }
    }

    override suspend fun loginJellyfinQuickConnect(
        serverUrl: String, name: String, onCode: ((String) -> Unit)?, onProgress: ((String) -> Unit)?, id: Long?
    ): Result<Provider> {
        return try {
            val normalizedServerUrl = ProviderInputSanitizer.resolveUrlProtocol(
                ProviderInputSanitizer.normalizeUrl(serverUrl)
            )
            val normalizedName = ProviderInputSanitizer.normalizeProviderName(name)
            ProviderInputSanitizer.validateUrl(normalizedServerUrl)?.let { return Result.error(it) }
            val providerName = normalizedName.ifBlank {
                normalizedServerUrl.substringAfter("//").substringBefore("/").ifBlank { "Jellyfin" }
            }
            val existingProvider = if (id != null) providerDao.getById(id)?.toDomain() else null
            onProgress?.invoke("Requesting Quick Connect code...")
            val quickConnect = when (val quickConnectResult = jellyfinProvider.authenticateQuickConnect(
                serverUrl = normalizedServerUrl, onCode = onCode, onProgress = onProgress
            )) {
                is Result.Success -> quickConnectResult.data
                is Result.Error -> return Result.error(quickConnectResult.message, quickConnectResult.exception)
                is Result.Loading -> return Result.error("Unexpected loading state")
            }
            val onboardingTarget = saveJellyfinProvider(providerName = providerName,
                serverUrl = normalizedServerUrl, username = quickConnect.userName.ifBlank { providerName },
                password = quickConnect.accessToken, existingProvider = existingProvider)
            handleInitialOnboardingSync(
                providerData = onboardingTarget.providerData,
                syncResult = syncInitialOnboarding(onboardingTarget, onProgress),
                syncFailurePrefix = "Jellyfin provider saved, but initial sync failed. The provider was saved and can be retried from Settings",
                pendingEdit = onboardingTarget.pendingEdit
            )
        } catch (e: Exception) {
            Result.error("Failed to add Jellyfin provider: ${e.message}", e)
        }
    }

    private suspend fun saveJellyfinProvider(
        providerName: String, serverUrl: String, username: String, password: String, existingProvider: Provider?
    ): InitialOnboardingTarget {
        return if (existingProvider != null) {
            val updated = existingProvider.copy(
                name = providerName.ifBlank { existingProvider.name }, type = ProviderType.JELLYFIN,
                serverUrl = serverUrl, username = username, password = password,
                m3uUrl = "", epgUrl = "", httpUserAgent = "", httpHeaders = "",
                isActive = false, status = ProviderStatus.PARTIAL, lastSyncedAt = 0
            )
            InitialOnboardingTarget(
                providerData = updated.copy(password = ""),
                providerForSync = updated,
                pendingEdit = stageProviderEdit(updated)
            )
        } else {
            val provider = Provider(name = providerName, type = ProviderType.JELLYFIN,
                serverUrl = serverUrl, username = username, password = password,
                isActive = false, status = ProviderStatus.PARTIAL)
            val newId = providerDao.insert(provider.toSecureEntity())
            InitialOnboardingTarget(
                providerData = provider.copy(id = newId, password = ""),
                providerForSync = provider.copy(id = newId)
            )
        }
    }


    override suspend fun loginStalker(
        portalUrl: String,
        macAddress: String,
        name: String,
        authMode: StalkerAuthMode,
        username: String,
        password: String,
        httpUserAgent: String,
        httpHeaders: String,
        deviceProfile: String,
        timezone: String,
        locale: String,
        serialNumber: String,
        deviceId: String,
        deviceId2: String,
        signature: String,
        stalkerAdvancedOptionsJson: String,
        epgSyncMode: ProviderEpgSyncMode,
        guideSourcePolicy: GuideSourcePolicy,
        channelLogoSourcePolicy: ChannelLogoSourcePolicy,
        onProgress: ((String) -> Unit)?,
        id: Long?
    ): Result<Provider> {
        val normalizedPortalUrl = ProviderInputSanitizer.normalizeUrl(portalUrl)
        val normalizedMacAddress = ProviderInputSanitizer.normalizeMacAddress(macAddress)
        val normalizedName = ProviderInputSanitizer.normalizeProviderName(name)
        val normalizedUsername = ProviderInputSanitizer.normalizeUsername(username)
        val resolvedPortalUrl = ProviderInputSanitizer.resolveUrlProtocol(normalizedPortalUrl)
        val normalizedDeviceProfile = ProviderInputSanitizer.normalizeDeviceProfile(deviceProfile)
        val normalizedTimezone = ProviderInputSanitizer.normalizeTimezone(timezone)
        val normalizedLocale = ProviderInputSanitizer.normalizeLocale(locale)
        val normalizedSerialNumber = ProviderInputSanitizer.normalizeStalkerSerial(serialNumber)
        val normalizedDeviceId = ProviderInputSanitizer.normalizeStalkerDeviceId(deviceId)
        val normalizedDeviceId2 = ProviderInputSanitizer.normalizeStalkerDeviceId(deviceId2)
        val normalizedSignature = ProviderInputSanitizer.normalizeStalkerSignature(signature)
        val normalizedAdvancedOptionsJson = stalkerAdvancedOptionsJson.trim()

        ProviderInputSanitizer.validateUrl(resolvedPortalUrl)?.let { message ->
            return Result.error(message)
        }
        UrlSecurityPolicy.validateStalkerPortalUrl(resolvedPortalUrl)?.let { message ->
            return Result.error(message)
        }
        if (normalizedMacAddress.isNotBlank()) {
            ProviderInputSanitizer.validateMacAddress(normalizedMacAddress)?.let { message ->
                return Result.error(message)
            }
        }

        onProgress?.invoke("Authenticating...")
        val existingProvider = if (id != null) {
            // Edit path: check that the new normalized identity does not collide with a
            // different provider before we commit the update.
            val collision = providerDao.getByUrlAndUser(resolvedPortalUrl, normalizedUsername, normalizedMacAddress)
            if (collision != null && collision.id != id) {
                return Result.error("A Stalker provider with this portal URL and identity already exists.")
            }
            providerDao.getById(id)
        } else {
            providerDao.getByUrlAndUser(resolvedPortalUrl, normalizedUsername, normalizedMacAddress)
        }
        val effectivePassword = try {
            password.takeIf { it.isNotBlank() }
                ?: existingProvider?.password?.let(credentialCrypto::decryptIfNeeded)
                ?: ""
        } catch (e: CredentialDecryptionException) {
            return Result.error(e.message ?: CredentialDecryptionException.MESSAGE, e)
        }

        val provider = createStalkerProvider(
            providerId = 0L,
            portalUrl = resolvedPortalUrl,
            macAddress = normalizedMacAddress,
            authMode = authMode,
            username = normalizedUsername,
            password = effectivePassword,
            httpUserAgent = httpUserAgent,
            httpHeaders = httpHeaders,
            deviceProfile = normalizedDeviceProfile,
            timezone = normalizedTimezone,
            locale = normalizedLocale,
            serialNumber = normalizedSerialNumber,
            deviceId = normalizedDeviceId,
            deviceId2 = normalizedDeviceId2,
            signature = normalizedSignature,
            stalkerAdvancedOptionsJson = normalizedAdvancedOptionsJson
        )

        return when (val authResult = provider.authenticate()) {
            is Result.Success -> {
                val onboardingTarget = if (existingProvider != null) {
                    onProgress?.invoke("Updating existing provider...")
                    val updated = authResult.data.copy(
                        id = existingProvider.id,
                        name = normalizedName.ifBlank { existingProvider.name },
                        serverUrl = resolvedPortalUrl,
                        username = normalizedUsername,
                        password = effectivePassword,
                        httpUserAgent = httpUserAgent,
                        httpHeaders = httpHeaders,
                        stalkerMacAddress = normalizedMacAddress,
                        stalkerDeviceProfile = normalizedDeviceProfile,
                        stalkerDeviceTimezone = normalizedTimezone,
                        stalkerDeviceLocale = normalizedLocale,
                        stalkerSerialNumber = normalizedSerialNumber,
                        stalkerDeviceId = normalizedDeviceId,
                        stalkerDeviceId2 = normalizedDeviceId2,
                        stalkerSignature = normalizedSignature,
                        stalkerAdvancedOptionsJson = normalizedAdvancedOptionsJson,
                        epgUrl = existingProvider.epgUrl,
                        epgSyncMode = epgSyncMode,
                        guideSourcePolicy = guideSourcePolicy,
                        channelLogoSourcePolicy = channelLogoSourcePolicy,
                        xtreamFastSyncEnabled = false,
                        m3uVodClassificationEnabled = false,
                        isActive = false,
                        status = ProviderStatus.PARTIAL,
                        lastSyncedAt = 0L,
                        createdAt = existingProvider.createdAt
                    )
                    InitialOnboardingTarget(
                        providerData = updated.copy(password = ""),
                        providerForSync = updated,
                        pendingEdit = stageProviderEdit(updated)
                    )
                } else {
                    val newData = authResult.data.copy(
                        name = normalizedName.ifBlank { authResult.data.name },
                        serverUrl = resolvedPortalUrl,
                        username = normalizedUsername,
                        password = effectivePassword,
                        httpUserAgent = httpUserAgent,
                        httpHeaders = httpHeaders,
                        stalkerMacAddress = normalizedMacAddress,
                        stalkerDeviceProfile = normalizedDeviceProfile,
                        stalkerDeviceTimezone = normalizedTimezone,
                        stalkerDeviceLocale = normalizedLocale,
                        stalkerSerialNumber = normalizedSerialNumber,
                        stalkerDeviceId = normalizedDeviceId,
                        stalkerDeviceId2 = normalizedDeviceId2,
                        stalkerSignature = normalizedSignature,
                        stalkerAdvancedOptionsJson = normalizedAdvancedOptionsJson,
                        epgSyncMode = epgSyncMode,
                        guideSourcePolicy = guideSourcePolicy,
                        channelLogoSourcePolicy = channelLogoSourcePolicy,
                        xtreamFastSyncEnabled = false,
                        m3uVodClassificationEnabled = false,
                        isActive = false,
                        status = ProviderStatus.PARTIAL
                    )
                    val newId = providerDao.insert(newData.toSecureEntity())
                    InitialOnboardingTarget(
                        providerData = newData.copy(id = newId, password = ""),
                        providerForSync = newData.copy(id = newId)
                    )
                }

                handleInitialOnboardingSync(
                    providerData = onboardingTarget.providerData,
                    syncResult = syncInitialOnboarding(onboardingTarget, onProgress),
                    syncFailurePrefix = "Provider login succeeded, but initial sync failed. The provider was saved and can be retried from Settings",
                    pendingEdit = onboardingTarget.pendingEdit
                )
            }
            is Result.Error -> Result.error(authResult.message, authResult.exception)
            is Result.Loading -> Result.error("Unexpected loading state")
        }
    }

    /**
     * Records an edit without touching the committed provider row. The password is encrypted
     * before serialization, so recovery state never introduces a new plaintext credential copy.
     */
    private suspend fun stageProviderEdit(candidate: Provider): PendingProviderEdit {
        require(candidate.id != 0L) { "A provider edit requires an existing provider ID." }
        val secureCandidate = candidate.toSecureEntity()
        val now = System.currentTimeMillis()
        val revision = transactionRunner.inTransaction {
            val nextRevision = providerConfigRevisionDao.latestRevision(candidate.id) + 1L
            providerConfigRevisionDao.supersedeOlder(candidate.id, nextRevision, now)
            providerConfigRevisionDao.upsert(
                ProviderConfigRevisionEntity(
                    providerId = candidate.id,
                    revision = nextRevision,
                    configJson = gson.toJson(secureCandidate),
                    state = ProviderConfigRevisionState.PENDING,
                    createdAt = now,
                    updatedAt = now
                )
            )
            check(providerConfigRevisionDao.claimForSync(candidate.id, nextRevision, now) == 1) {
                "Pending provider edit could not be claimed for synchronization."
            }
            nextRevision
        }
        return PendingProviderEdit(revision, candidate, secureCandidate).also { pendingEdit ->
            // If the process dies during the foreground sync, WorkManager retains this delayed
            // hand-off and resumes the staged candidate rather than the committed configuration.
            scheduleProviderEditRecovery(pendingEdit, immediate = false)
        }
    }

    /** Runs a candidate configuration without exposing it through the committed provider row. */
    private suspend fun syncInitialOnboarding(
        target: InitialOnboardingTarget,
        onProgress: ((String) -> Unit)?,
        trackInitialLiveOnboarding: Boolean = false
    ): Result<Unit> {
        return try {
        val pendingEdit = target.pendingEdit
        if (pendingEdit == null) {
            syncManager.sync(
                providerId = target.providerData.id,
                force = false,
                onProgress = onProgress,
                trackInitialLiveOnboarding = trackInitialLiveOnboarding
            )
        } else {
            syncManager.syncWithProviderOverride(
                providerId = target.providerData.id,
                force = false,
                onProgress = onProgress,
                trackInitialLiveOnboarding = trackInitialLiveOnboarding,
                providerOverride = pendingEdit.candidate,
                afterCatalogApply = { promoteProviderEdit(pendingEdit) }
            )
        }
    } catch (error: kotlinx.coroutines.CancellationException) {
        target.pendingEdit?.let { pendingEdit ->
            providerConfigRevisionDao.releaseForRetry(
                pendingEdit.candidate.id,
                pendingEdit.revision,
                System.currentTimeMillis()
            )
        }
        throw error
        }
    }

    /**
     * Called while the catalog transaction is open. A stale revision is
     * rejected before it can replace the provider row, making a late sync completion harmless.
     */
    private suspend fun promoteProviderEdit(pendingEdit: PendingProviderEdit) {
        val now = System.currentTimeMillis()
        check(
            providerConfigRevisionDao.markCommitted(
                pendingEdit.candidate.id,
                pendingEdit.revision,
                now
            ) == 1
        ) { "Provider edit ${pendingEdit.revision} was superseded before catalog commit." }
        providerDao.update(
            pendingEdit.secureCandidate.copy(
                isActive = true,
                status = ProviderStatus.PARTIAL,
                lastSyncedAt = now
            )
        )
    }

    private suspend fun handleInitialOnboardingSync(
        providerData: Provider,
        syncResult: Result<Unit>,
        syncFailurePrefix: String,
        pendingEdit: PendingProviderEdit? = null
    ): Result<Provider> {
        return when (syncResult) {
        is Result.Success -> {
            if (
                pendingEdit != null &&
                providerConfigRevisionDao.getState(providerData.id, pendingEdit.revision) != ProviderConfigRevisionState.COMMITTED
            ) {
                providerConfigRevisionDao.markFailed(
                    providerData.id,
                    pendingEdit.revision,
                    "Initial sync completed without committing catalog content.",
                    System.currentTimeMillis()
                )
                scheduleProviderEditRecovery(pendingEdit)
                val message = "$syncFailurePrefix: Sync did not finish with any committed content."
                return Result.error(
                    message,
                    ProviderSavedWithSyncErrorException(
                        provider = providerData.copy(status = ProviderStatus.PARTIAL, isActive = false),
                        message = message
                    )
                )
            }
            val finalStatus = if (syncManager.currentSyncState(providerData.id) is SyncState.Partial) {
                ProviderStatus.PARTIAL
            } else {
                ProviderStatus.ACTIVE
            }
                if (!hasUsableLiveCatalogForActivation(
                    providerData.id,
                    providerData.type,
                    channelDao,
                    categoryDao,
                    syncMetadataRepository
                )) {
                updateProviderSyncStatus(
                    providerData.id,
                    ProviderStatus.PARTIAL,
                    lastSyncedAt = System.currentTimeMillis(),
                    isActive = false
                )
                syncManager.scheduleProviderSyncResume(providerData.id)
                val message = "$syncFailurePrefix: Sync did not finish with any committed content."
                Result.error(
                    message,
                    ProviderSavedWithSyncErrorException(
                        provider = providerData.copy(status = ProviderStatus.PARTIAL, isActive = false),
                        message = message
                    )
                )
            } else {
                providerDao.setActive(providerData.id)
                updateProviderSyncStatus(
                    providerData.id,
                    finalStatus,
                    lastSyncedAt = System.currentTimeMillis()
                )
                maybeScheduleBackgroundEpgSync(providerData.id)
                Result.success(providerData.copy(status = finalStatus, isActive = true))
            }
        }
        is Result.Error -> {
            if (pendingEdit != null) {
                val state = providerConfigRevisionDao.getState(providerData.id, pendingEdit.revision)
                if (state != ProviderConfigRevisionState.COMMITTED) {
                    providerConfigRevisionDao.markFailed(
                        providerData.id,
                        pendingEdit.revision,
                        syncResult.message,
                        System.currentTimeMillis()
                    )
                    scheduleProviderEditRecovery(pendingEdit)
                    val message = "$syncFailurePrefix: ${syncResult.message}"
                    return Result.error(
                        message,
                        ProviderSavedWithSyncErrorException(
                            provider = providerData.copy(status = ProviderStatus.PARTIAL, isActive = false),
                            message = message,
                            cause = syncResult.exception
                        )
                    )
                }
            }
            updateProviderSyncStatus(
                providerData.id,
                ProviderStatus.PARTIAL,
                isActive = pendingEdit != null
            )
            syncManager.scheduleProviderSyncResume(providerData.id)
            val message = "$syncFailurePrefix: ${syncResult.message}"
            Result.error(
                message,
                ProviderSavedWithSyncErrorException(
                    provider = providerData.copy(status = ProviderStatus.PARTIAL, isActive = pendingEdit != null),
                    message = message,
                    cause = syncResult.exception
                )
            )
        }
        is Result.Loading -> Result.error("Unexpected loading state")
        }
    }

    private fun scheduleProviderEditRecovery(
        pendingEdit: PendingProviderEdit,
        immediate: Boolean = true
    ) {
        runCatching {
            ProviderSyncWorker.enqueueProviderConfigRevision(
                appContext,
                pendingEdit.candidate.id,
                pendingEdit.revision,
                immediate = immediate
            )
        }.onFailure { error ->
            logger.warning("Failed to schedule pending provider edit recovery: ${error.message}")
        }
    }

    /**
     * Delegates to [SyncManager] — the single source of truth for the full sync pipeline.
     */
    override suspend fun refreshProviderData(
        providerId: Long,
        force: Boolean,
        movieFastSyncOverride: Boolean?,
        epgSyncModeOverride: ProviderEpgSyncMode?,
        onProgress: ((String) -> Unit)?
    ): Result<Unit> {
        var syncResult: Result<Unit>? = null
        val disposition = providerWorkflowRunner.execute(
            providerId = providerId,
            phase = ProviderWorkflowPhase.PRIMARY_CATALOG,
            reason = ProviderWorkflowReason.MANUAL,
            force = force,
            supersede = force,
            priority = MANUAL_REFRESH_PRIORITY
        ) {
            val result = syncManager.sync(
                providerId,
                force = force,
                movieFastSyncOverride = movieFastSyncOverride,
                epgSyncModeOverride = epgSyncModeOverride,
                onProgress = onProgress
            )
            syncResult = result
            when (result) {
                is Result.Success -> {
                    finalizeSuccessfulManualSync(providerId)
                    ProviderWorkflowOutcome.Success(
                        partial = syncManager.currentSyncState(providerId) is SyncState.Partial
                    )
                }
                is Result.Error -> {
                    transactionRunner.inTransaction {
                        providerWorkflowCommitFence.assertCanCommit(providerId)
                        updateProviderSyncStatus(providerId, ProviderStatus.ERROR)
                    }
                    ProviderWorkflowOutcome.Failure(
                        code = "MANUAL_PROVIDER_SYNC",
                        message = result.message,
                        cause = result.exception
                    )
                }
                is Result.Loading -> ProviderWorkflowOutcome.Failure(
                    code = "MANUAL_PROVIDER_SYNC_LOADING",
                    message = "Provider refresh did not reach a terminal state.",
                    retryable = true
                )
            }
        }

        if (disposition == ProviderWorkflowDisposition.BUSY) {
            return Result.error("Provider refresh is already in progress.")
        }
        if (disposition == ProviderWorkflowDisposition.SUPERSEDED) {
            return Result.error("Provider refresh was superseded by a newer request.")
        }
        val terminalResult = syncResult ?: return when (disposition) {
            ProviderWorkflowDisposition.RETRY ->
                Result.error("Provider refresh was interrupted and will need to be retried.")
            ProviderWorkflowDisposition.FAILED ->
                Result.error("Provider refresh failed before synchronization started.")
            else -> Result.error("Provider refresh did not produce a result.")
        }

        return when (terminalResult) {
            is Result.Success -> terminalResult
            is Result.Error -> terminalResult
            is Result.Loading -> Result.error("Unexpected loading state")
        }
    }

    private suspend fun finalizeSuccessfulManualSync(providerId: Long) {
        var shouldResume = false
        var shouldScheduleEpg = false
        transactionRunner.inTransaction {
            providerWorkflowCommitFence.assertCanCommit(providerId)
            val finalStatus = if (syncManager.currentSyncState(providerId) is SyncState.Partial) {
                ProviderStatus.PARTIAL
            } else {
                ProviderStatus.ACTIVE
            }
            val provider = providerDao.getById(providerId)
            if (
                provider != null &&
                !hasUsableLiveCatalogForActivation(
                    providerId,
                    provider.type,
                    channelDao,
                    categoryDao,
                    syncMetadataRepository
                )
            ) {
                updateProviderSyncStatus(
                    providerId,
                    ProviderStatus.PARTIAL,
                    lastSyncedAt = System.currentTimeMillis(),
                    isActive = false
                )
                shouldResume = true
            } else {
                updateProviderSyncStatus(providerId, finalStatus, System.currentTimeMillis())
                shouldScheduleEpg = true
            }
        }
        if (shouldResume) {
            syncManager.scheduleProviderSyncResume(providerId)
        } else if (shouldScheduleEpg) {
            maybeScheduleBackgroundEpgSync(providerId)
        }
    }

    override suspend fun getProgramsForLiveStream(
        providerId: Long,
        streamId: Long,
        epgChannelId: String?,
        limit: Int
    ): Result<List<Program>> {
        val providerEntity = providerDao.getById(providerId)
            ?: return Result.error("Provider $providerId not found")
        if (!allowsOnDemandGuide(providerEntity.toPublicDomain())) {
            return Result.error("On-demand guide lookup is disabled for this provider.")
        }
        return when (providerEntity.type) {
            ProviderType.XTREAM_CODES -> when (val providerContextResult = createXtreamLiveProgramProviderContext(providerId)) {
                is Result.Success -> {
                    val result = fetchXtreamProgramsForLiveStream(
                        providerId = providerId,
                        streamId = streamId,
                        epgChannelId = epgChannelId,
                        limit = limit,
                        xtreamProvider = providerContextResult.data
                    )
                    if (result is Result.Success && result.data.isNotEmpty()) {
                        cacheProgramsForChannel(providerId, result.data)
                        refreshCachedEpgMetadata(providerId)
                    }
                    result
                }
                is Result.Error -> Result.error(providerContextResult.message, providerContextResult.exception)
                is Result.Loading -> Result.error("Unexpected loading state")
            }
            ProviderType.STALKER_PORTAL -> {
                val stalkerProvider = createStalkerProviderFromEntity(providerEntity)
                val channelKey = epgChannelId?.takeIf { it.isNotBlank() } ?: streamId.toString()
                when (val result = stalkerProvider.getShortEpg(channelKey, limit)) {
                    is Result.Success -> {
                        if (result.data.isNotEmpty()) {
                            cacheProgramsForChannel(providerId, result.data)
                            refreshCachedEpgMetadata(providerId)
                        }
                        result
                    }
                    is Result.Error -> Result.error(result.message, result.exception)
                    is Result.Loading -> Result.error("Unexpected loading state")
                }
            }
            ProviderType.M3U,
            ProviderType.JELLYFIN -> Result.error("On-demand guide lookup is unavailable for this provider.")
        }
    }

    override suspend fun getProgramsForLiveStreams(
        providerId: Long,
        requests: List<LiveStreamProgramRequest>,
        limit: Int
    ): Map<LiveStreamProgramRequest, Result<List<Program>>> {
        val normalizedRequests = requests
            .filter { it.streamId > 0L }
            .distinct()
        if (normalizedRequests.isEmpty()) {
            return emptyMap()
        }

        val providerEntity = providerDao.getById(providerId)
            ?: return normalizedRequests.associateWith { Result.error("Provider $providerId not found") }
        if (!allowsOnDemandGuide(providerEntity.toPublicDomain())) {
            return normalizedRequests.associateWith {
                Result.error("On-demand guide lookup is disabled for this provider.")
            }
        }

        return when (providerEntity.type) {
            ProviderType.XTREAM_CODES -> when (val providerContextResult = createXtreamLiveProgramProviderContext(providerId)) {
                is Result.Success -> coroutineScope {
                    val requestDispatcher = Dispatchers.IO.limitedParallelism(XTREAM_GUIDE_BATCH_CONCURRENCY)
                    normalizedRequests
                        .map { request ->
                            async(requestDispatcher) {
                                request to fetchXtreamProgramsForLiveStream(
                                    providerId = providerId,
                                    streamId = request.streamId,
                                    epgChannelId = request.epgChannelId,
                                    limit = limit,
                                    xtreamProvider = providerContextResult.data
                                )
                            }
                        }
                        .awaitAll()
                        .also { results ->
                            val cachedPrograms = results
                                .mapNotNull { (_, result) -> (result as? Result.Success)?.data }
                                .flatten()
                            if (cachedPrograms.isNotEmpty()) {
                                cacheProgramsForChannels(providerId, cachedPrograms)
                                refreshCachedEpgMetadata(providerId)
                            }
                        }
                        .toMap()
                }
                is Result.Error -> normalizedRequests.associateWith {
                    Result.error(providerContextResult.message, providerContextResult.exception)
                }
                is Result.Loading -> normalizedRequests.associateWith {
                    Result.error("Unexpected loading state")
                }
            }
            ProviderType.STALKER_PORTAL -> {
                val stalkerProvider = createStalkerProviderFromEntity(providerEntity)
                val results = normalizedRequests.associateWith { request ->
                    stalkerProvider.getShortEpg(
                        request.epgChannelId?.takeIf { it.isNotBlank() } ?: request.streamId.toString(),
                        limit
                    )
                }
                val cachedPrograms = results.values
                    .mapNotNull { (it as? Result.Success)?.data }
                    .flatten()
                if (cachedPrograms.isNotEmpty()) {
                    cacheProgramsForChannels(providerId, cachedPrograms)
                    refreshCachedEpgMetadata(providerId)
                }
                results
            }
            ProviderType.M3U,
            ProviderType.JELLYFIN -> normalizedRequests.associateWith {
                Result.error("On-demand guide lookup is unavailable for this provider.")
            }
        }
    }

    override suspend fun buildCatchUpUrl(providerId: Long, streamId: Long, start: Long, end: Long): String? {
        return buildCatchUpUrls(providerId, streamId, start, end).firstOrNull()
    }

    override suspend fun buildCatchUpUrls(providerId: Long, streamId: Long, start: Long, end: Long): List<String> {
        val providerEntity = providerDao.getById(providerId) ?: return emptyList()
        val provider = providerEntity.toPublicDomain()
        val providerPassword = credentialCrypto.decryptIfNeeded(providerEntity.password)
        val channel = channelDao.getById(streamId)
        val resolvedStreamId = channel?.streamId?.takeIf { it > 0 } ?: streamId
        return when (provider.type) {
            ProviderType.XTREAM_CODES -> createXtreamProvider(
                providerId = providerId,
                serverUrl = provider.serverUrl,
                username = provider.username,
                password = providerPassword,
                allowedOutputFormats = provider.allowedOutputFormats,
                httpUserAgent = provider.httpUserAgent,
                httpHeaders = provider.httpHeaders
            ).buildCatchUpUrls(resolvedStreamId, start, end)
            ProviderType.M3U -> {
                val source = channel?.catchUpSource ?: return emptyList()
                buildM3uCatchUpUrls(source, start, end)
            }
            ProviderType.STALKER_PORTAL -> createStalkerProviderFromEntity(providerEntity).buildCatchUpUrls(
                streamId = resolvedStreamId,
                start = start,
                end = end,
                sourceStreamUrl = channel?.streamUrl,
                sourceCatchUpSource = channel?.catchUpSource
            )
            ProviderType.JELLYFIN -> emptyList()
        }
    }

    suspend fun createXtreamProvider(
        providerId: Long,
        serverUrl: String,
        username: String,
        password: String,
        allowedOutputFormats: List<String> = emptyList(),
        httpUserAgent: String = "",
        httpHeaders: String = ""
    ): IptvProvider {
        val enableBase64TextCompatibility = preferencesRepository.xtreamBase64TextCompatibility.first()
        return XtreamProvider(
            providerId = providerId,
            api = xtreamApiService,
            serverUrl = serverUrl,
            username = username,
            password = password,
            allowedOutputFormats = allowedOutputFormats,
            enableBase64TextCompatibility = enableBase64TextCompatibility,
            requestProfile = buildGenericProviderRequestProfile(
                ownerTag = "provider:$providerId/xtream",
                httpUserAgent = httpUserAgent,
                httpHeaders = httpHeaders
            )
        )
    }

    private fun createStalkerProvider(
        providerId: Long,
        portalUrl: String,
        macAddress: String,
        authMode: StalkerAuthMode,
        username: String,
        password: String,
        httpUserAgent: String = "",
        httpHeaders: String = "",
        portalFingerprintHint: StalkerPortalFingerprint = StalkerPortalFingerprint.BASIC_MAC,
        magPresetHint: StalkerMagPreset = StalkerMagPreset.GENERIC_SAFE,
        bootstrapRecipeHint: StalkerBootstrapRecipe = StalkerBootstrapRecipe.GENERIC_SAFE,
        endpointPreferenceHint: StalkerEndpointPreference = StalkerEndpointPreference.AUTO,
        cookieModeHint: StalkerCookieMode = StalkerCookieMode.NONE,
        playbackBackendHint: StalkerPlaybackBackendHint = StalkerPlaybackBackendHint.AUTO,
        portalProfileHint: StalkerPortalProfile = StalkerPortalProfile.MAG_BASIC,
        preferredPlaybackMode: StalkerPlaybackMode? = null,
        deviceProfile: String,
        timezone: String,
        locale: String,
        serialNumber: String = "",
        deviceId: String = "",
        deviceId2: String = "",
        signature: String = "",
        stalkerAdvancedOptionsJson: String = ""
    ): StalkerProvider {
        return StalkerProvider(
            providerId = providerId,
            api = stalkerApiService,
            portalUrl = portalUrl,
            macAddress = macAddress,
            authMode = authMode,
            username = username,
            password = password,
            httpUserAgent = httpUserAgent,
            httpHeaders = httpHeaders,
            portalFingerprintHint = portalFingerprintHint,
            magPresetHint = magPresetHint,
            bootstrapRecipeHint = bootstrapRecipeHint,
            endpointPreferenceHint = endpointPreferenceHint,
            cookieModeHint = cookieModeHint,
            playbackBackendHint = playbackBackendHint,
            portalProfileHint = portalProfileHint,
            preferredPlaybackMode = preferredPlaybackMode,
            deviceProfile = deviceProfile,
            timezone = timezone,
            locale = locale,
            serialNumber = serialNumber,
            deviceId = deviceId,
            deviceId2 = deviceId2,
            signature = signature,
            stalkerAdvancedOptionsJson = stalkerAdvancedOptionsJson
        )
    }

    private fun createStalkerProviderFromEntity(entity: ProviderEntity): StalkerProvider {
        return createStalkerProvider(
            providerId = entity.id,
            portalUrl = entity.serverUrl,
            macAddress = entity.stalkerMacAddress,
            authMode = entity.stalkerAuthMode,
            username = entity.username,
            password = try {
                credentialCrypto.decryptIfNeeded(entity.password)
            } catch (_: Throwable) {
                ""
            },
            httpUserAgent = entity.httpUserAgent,
            httpHeaders = entity.httpHeaders,
            portalFingerprintHint = entity.stalkerPortalFingerprint,
            magPresetHint = entity.stalkerMagPreset,
            bootstrapRecipeHint = entity.stalkerLastBootstrapRecipe,
            endpointPreferenceHint = entity.stalkerEndpointPreference,
            cookieModeHint = entity.stalkerCookieMode,
            playbackBackendHint = entity.stalkerPlaybackBackendHint,
            portalProfileHint = entity.stalkerPortalProfile,
            preferredPlaybackMode = entity.stalkerLastPlaybackMode
                ?.let { runCatching { StalkerPlaybackMode.valueOf(it) }.getOrNull() },
            deviceProfile = entity.stalkerDeviceProfile,
            timezone = entity.stalkerDeviceTimezone,
            locale = entity.stalkerDeviceLocale,
            serialNumber = entity.stalkerSerialNumber,
            deviceId = entity.stalkerDeviceId,
            deviceId2 = entity.stalkerDeviceId2,
            signature = entity.stalkerSignature,
            stalkerAdvancedOptionsJson = entity.stalkerAdvancedOptionsJson
        )
    }

    private fun ProviderEntity.toPublicDomain(): Provider {
        return toDomain().copy(password = "")
    }

    private suspend fun createXtreamLiveProgramProviderContext(providerId: Long): Result<XtreamProvider> {
        if (providerId <= 0L) {
            return Result.error("Live stream context is unavailable.")
        }

        val providerEntity = providerDao.getById(providerId)
            ?: return Result.error("Provider $providerId not found")
        val provider = providerEntity.toPublicDomain()
        if (provider.type != ProviderType.XTREAM_CODES) {
            return Result.error("On-demand guide lookup is available only for Xtream providers.")
        }

        val providerPassword = try {
            credentialCrypto.decryptIfNeeded(providerEntity.password)
        } catch (e: CredentialDecryptionException) {
            return Result.error(e.message ?: CredentialDecryptionException.MESSAGE, e)
        }

        return Result.success(
            createXtreamProvider(
                providerId = providerId,
                serverUrl = provider.serverUrl,
                username = provider.username,
                password = providerPassword,
                allowedOutputFormats = provider.allowedOutputFormats,
                httpUserAgent = provider.httpUserAgent,
                httpHeaders = provider.httpHeaders
            ) as XtreamProvider
        )
    }

    private suspend fun fetchXtreamProgramsForLiveStream(
        providerId: Long,
        streamId: Long,
        epgChannelId: String?,
        limit: Int,
        xtreamProvider: XtreamProvider
    ): Result<List<Program>> {
        if (providerId <= 0L || streamId <= 0L) {
            return Result.error("Live stream context is unavailable.")
        }

        val shortProgramsResult = xtreamProvider.getShortEpg(
            channelId = streamId.toString(),
            limit = limit.coerceAtLeast(1)
        )
        val shortPrograms = (shortProgramsResult as? Result.Success)?.data
            ?.sortedBy { it.startTime }
            .orEmpty()
        if (shortPrograms.isNotEmpty()) {
            return Result.success(
                normalizeXtreamPrograms(
                    providerId = providerId,
                    channelId = epgChannelId ?: streamId.toString(),
                    programs = shortPrograms
                )
            )
        }

        return when (val fullProgramsResult = xtreamProvider.getEpg(streamId.toString())) {
            is Result.Success -> {
                val normalizedPrograms = normalizeXtreamPrograms(
                    providerId = providerId,
                    channelId = epgChannelId ?: streamId.toString(),
                    programs = fullProgramsResult.data.sortedBy { it.startTime }
                )
                Result.success(normalizedPrograms)
            }
            is Result.Error -> {
                val shortError = shortProgramsResult as? Result.Error
                val combinedMessage = listOfNotNull(
                    shortError?.message?.takeIf { it.isNotBlank() },
                    fullProgramsResult.message.takeIf { it.isNotBlank() }
                )
                    .distinct()
                    .joinToString(separator = " / ")
                    .ifBlank { "Failed to load on-demand guide" }
                Result.error(combinedMessage, fullProgramsResult.exception ?: shortError?.exception)
            }
            is Result.Loading -> Result.error("Unexpected loading state")
        }
    }

    private fun Provider.toSecureEntity(): ProviderEntity {
        val encryptedPassword = credentialCrypto.encryptIfNeeded(password)
        return copy(password = encryptedPassword).toEntity()
    }

    private suspend fun updateProviderSyncStatus(
        providerId: Long,
        status: ProviderStatus,
        lastSyncedAt: Long? = null,
        isActive: Boolean? = null
    ) {
        val current = providerDao.getById(providerId) ?: return
        val updated = current.copy(
            status = status,
            lastSyncedAt = lastSyncedAt ?: current.lastSyncedAt,
            isActive = isActive ?: current.isActive
        )
        providerDao.update(updated)
    }

    private suspend fun maybeScheduleBackgroundEpgSync(providerId: Long) {
        val provider = providerDao.getById(providerId) ?: return
        if (provider.epgSyncMode != ProviderEpgSyncMode.BACKGROUND) {
            return
        }
        // The previous implementation launched a coroutine that slept for 15s and then
        // scheduled the worker. That kept a coroutine alive (and held onto its captures)
        // even when the user immediately backed out of the screen. WorkManager's own
        // initialDelay is the right place for that wait — it's persisted, cancellable,
        // and doesn't pin any process state.
        syncManager.scheduleBackgroundEpgSync(providerId)
    }

    private fun normalizeXtreamPrograms(
        providerId: Long,
        channelId: String,
        programs: List<Program>
    ): List<Program> {
        return programs.map { program ->
            program.copy(
                providerId = providerId,
                channelId = channelId
            )
        }
    }

    private suspend fun cacheProgramsForChannel(providerId: Long, programs: List<Program>) {
        val channelId = programs.firstOrNull()?.channelId ?: return
        transactionRunner.inTransaction {
            programDao.deleteForChannel(providerId, channelId)
            programDao.insertAll(programs.map { it.toEntity().copy(providerId = providerId) })
        }
    }

    private suspend fun cacheProgramsForChannels(providerId: Long, programs: List<Program>) {
        if (programs.isEmpty()) return
        val programsByChannel = programs.groupBy { it.channelId }
        transactionRunner.inTransaction {
            programsByChannel.forEach { (channelId, channelPrograms) ->
                programDao.deleteForChannel(providerId, channelId)
                programDao.insertAll(channelPrograms.map { it.toEntity().copy(providerId = providerId) })
            }
        }
    }

    private suspend fun refreshCachedEpgMetadata(providerId: Long) {
        val now = System.currentTimeMillis()
        val metadata = (syncMetadataRepository.getMetadata(providerId) ?: SyncMetadata(providerId)).copy(
            lastEpgSync = now,
            lastEpgSuccess = now,
            epgCount = programDao.countByProvider(providerId)
        )
        syncMetadataRepository.updateMetadata(metadata)
    }

    private fun allowsOnDemandGuide(provider: Provider): Boolean = when (provider.guideSourcePolicy) {
        GuideSourcePolicy.AUTO,
        GuideSourcePolicy.PROVIDER_ONLY -> true
        GuideSourcePolicy.EXTERNAL_ONLY,
        GuideSourcePolicy.DISABLED -> provider.type != ProviderType.XTREAM_CODES && provider.type != ProviderType.STALKER_PORTAL
    }
}

internal fun buildM3uCatchUpUrls(source: String, start: Long, end: Long): List<String> {
    val trimmedSource = source.trim()
    if (trimmedSource.isBlank()) return emptyList()

    val durationSeconds = (end - start).coerceAtLeast(0L)
    val durationMinutes = (durationSeconds / 60L).coerceAtLeast(1L)
    val startDate = java.time.Instant.ofEpochSecond(start).atZone(java.time.ZoneOffset.UTC)
    val endDate = java.time.Instant.ofEpochSecond(end).atZone(java.time.ZoneOffset.UTC)
    val compactStart = startDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
    val compactEnd = endDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))

    val replacements = linkedMapOf(
        "{start}" to start.toString(),
        "{end}" to end.toString(),
        "{duration}" to durationSeconds.toString(),
        "{duration_seconds}" to durationSeconds.toString(),
        "{duration_minutes}" to durationMinutes.toString(),
        "{utc}" to start.toString(),
        "{utcend}" to end.toString(),
        "{lutc}" to end.toString(),
        "{timestamp}" to start.toString(),
        "{Y}" to startDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy")),
        "{m}" to startDate.format(java.time.format.DateTimeFormatter.ofPattern("MM")),
        "{d}" to startDate.format(java.time.format.DateTimeFormatter.ofPattern("dd")),
        "{H}" to startDate.format(java.time.format.DateTimeFormatter.ofPattern("HH")),
        "{M}" to startDate.format(java.time.format.DateTimeFormatter.ofPattern("mm")),
        "{S}" to startDate.format(java.time.format.DateTimeFormatter.ofPattern("ss")),
        "{Ymd}" to startDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")),
        "{YmdHis}" to compactStart,
        "{utc:yyyyMMddHHmmss}" to compactStart,
        "{utcend:yyyyMMddHHmmss}" to compactEnd
    )

    val expanded = replacements.entries.fold(trimmedSource) { current, (placeholder, value) ->
        current.replace(placeholder, value)
    }

    return listOf(expanded).distinct()
}
