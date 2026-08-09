package com.streamvault.data.manager

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.common.truth.Truth.assertThat
import com.streamvault.data.local.DatabaseTransactionRunner
import com.streamvault.data.local.dao.EpisodeDao
import com.streamvault.data.local.dao.EpgSourceDao
import com.streamvault.data.local.dao.FavoriteDao
import com.streamvault.data.local.dao.MovieDao
import com.streamvault.data.local.dao.PlaybackHistoryDao
import com.streamvault.data.local.dao.ProviderDao
import com.streamvault.data.local.dao.BackupRestoreCheckpointDao
import com.streamvault.data.local.dao.ChannelDao
import com.streamvault.data.local.dao.RecordingScheduleDao
import com.streamvault.data.local.dao.VirtualGroupDao
import com.streamvault.data.local.entity.ProviderEntity
import com.streamvault.data.local.entity.EpgSourceEntity
import com.streamvault.data.local.entity.BackupRestoreCheckpointEntity
import com.streamvault.data.local.entity.ChannelEntity
import com.streamvault.data.local.entity.RecordingScheduleEntity
import com.streamvault.data.local.entity.VirtualGroupEntity
import com.streamvault.data.preferences.PreferencesRepository
import com.streamvault.data.security.CredentialCrypto
import com.streamvault.domain.manager.BackupData
import com.streamvault.domain.manager.BackupConflictStrategy
import com.streamvault.domain.manager.BackupImportPlan
import com.streamvault.domain.manager.BackupRestoreOutcome
import com.streamvault.domain.manager.BackupProviderReference
import com.streamvault.domain.manager.PortableCategoryReference
import com.streamvault.domain.manager.PortableChannelReference
import com.streamvault.domain.manager.PortableProviderPreferencesBackup
import com.streamvault.domain.manager.PortableVirtualGroupReference
import com.streamvault.domain.manager.RecordingManager
import com.streamvault.domain.manager.RecordingScheduleImportDisposition
import com.streamvault.domain.manager.ScheduledRecordingBackup
import com.streamvault.domain.model.AppHomeDashboardShelf
import com.streamvault.domain.model.AppTopLevelDestination
import com.streamvault.domain.model.ContentType
import com.streamvault.domain.model.Category
import com.streamvault.domain.model.DecoderMode
import com.streamvault.domain.model.Favorite
import com.streamvault.domain.model.EpgSource
import com.streamvault.domain.model.PlaybackHistory
import com.streamvault.domain.model.Provider
import com.streamvault.domain.model.ProviderStatus
import com.streamvault.domain.model.ProviderType
import com.streamvault.domain.model.RecordingItem
import com.streamvault.domain.model.RecordingRecurrence
import com.streamvault.domain.model.RecordingStatus
import com.streamvault.domain.model.Result
import com.streamvault.domain.model.StalkerTransportMode
import com.streamvault.domain.model.XmltvTimezonePolicy
import com.streamvault.domain.repository.CategoryRepository
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis
import org.junit.Assert.assertThrows
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class BackupManagerImplTest {

    @Test
    fun `importConfig preserves explicit EPG source timezone interpretation`() = runBlocking {
        val context: Context = mock()
        val providerDao: ProviderDao = mock()
        val epgSourceDao: EpgSourceDao = mock()
        val file = File.createTempFile("streamvault-epg-timezone", ".json")
        val backupData = BackupData(
            version = 10,
            epgSources = listOf(
                EpgSource(
                    id = 91L,
                    name = "Local-time guide",
                    url = "https://example.com/guide.xml",
                    timezonePolicy = XmltvTimezonePolicy.EXPLICIT_ZONE,
                    timezoneId = "Europe/Amsterdam"
                )
            )
        )
        FileOutputStream(file).use { it.write(Gson().toJson(backupData).toByteArray()) }
        whenever(providerDao.getAllSync()).thenReturn(emptyList())
        whenever(epgSourceDao.getByUrl("https://example.com/guide.xml")).thenReturn(
            EpgSourceEntity(
                id = 7L,
                name = "Existing",
                url = "https://example.com/guide.xml"
            )
        )

        try {
            val result = backupManagerForValidation(
                context = context,
                providerDao = providerDao,
                epgSourceDao = epgSourceDao
            ).importConfig(
                uriString = file.toURI().toString(),
                plan = BackupImportPlan(
                    importPreferences = false,
                    importProviders = true,
                    importSavedLibrary = false,
                    importPlaybackHistory = false,
                    importMultiViewPresets = false,
                    importRecordingSchedules = false,
                    conflictStrategy = BackupConflictStrategy.REPLACE_EXISTING
                )
            )

            assertThat(result).isInstanceOf(Result.Success::class.java)
            verify(epgSourceDao).update(argThat<EpgSourceEntity> {
                id == 7L &&
                    timezonePolicy == XmltvTimezonePolicy.EXPLICIT_ZONE &&
                    timezoneId == "Europe/Amsterdam"
            })
        } finally {
            file.delete()
        }
        Unit
    }

    @Test
    fun `inspectBackup rejects oversized non seekable input with typed byte limit`() = runBlocking {
        val context: Context = mock()
        val contentResolver: ContentResolver = mock()
        val uri = Uri.parse("content://oversized-stream")
        whenever(context.contentResolver).thenReturn(contentResolver)
        whenever(contentResolver.openInputStream(uri)).thenReturn(
            PrefixThenPaddingInputStream(
                prefix = """{"version":9,"preferences":{"key":"value"}}""".toByteArray(),
                totalBytes = 16 * 1024 * 1024 + 1
            )
        )

        val result = backupManagerForValidation(context).inspectBackup("content://oversized-stream")

        val error = result as Result.Error
        assertThat(error.exception).isInstanceOf(BackupAdmissionException::class.java)
        assertThat((error.exception as BackupAdmissionException).reason)
            .isEqualTo(BackupAdmissionReason.BYTE_LIMIT)
    }

    @Test
    fun `importConfig rejects oversized seekable file before mutation`() = runBlocking {
        val file = File.createTempFile("streamvault-oversized-backup", ".json")
        try {
            FileOutputStream(file).buffered().use { output ->
                output.write("""{"version":9,"preferences":{"key":"value"}}""".toByteArray())
                val padding = ByteArray(64 * 1024) { ' '.code.toByte() }
                repeat(257) { output.write(padding) }
            }
            val preferencesRepository: PreferencesRepository = mock()

            val result = backupManagerForValidation(
                context = mock(),
                preferencesRepository = preferencesRepository
            ).importConfig(file.toURI().toString(), preferencesOnlyPlan())

            val error = result as Result.Error
            assertThat((error.exception as BackupAdmissionException).reason)
                .isEqualTo(BackupAdmissionReason.BYTE_LIMIT)
            verify(preferencesRepository, never()).setParentalControlLevel(any())
        } finally {
            file.delete()
        }
    }

    @Test
    fun `inspectBackup rejects deeply nested json while streaming`() = runBlocking {
        val nested = buildString {
            append("""{"version":9,"unknown":""")
            repeat(65) { append('[') }
            append('0')
            repeat(65) { append(']') }
            append('}')
        }

        val error = inspectAdmissionFailure("deep-json", nested)

        assertThat(error.reason).isEqualTo(BackupAdmissionReason.DEPTH_LIMIT)
    }

    @Test
    fun `inspectBackup rejects million entry section before materializing it`() = runBlocking {
        val json = buildString(2_100_000) {
            append("""{"version":9,"multiViewPresets":{"preset_1":[""")
            repeat(1_000_000) { index ->
                if (index > 0) append(',')
                append('0')
            }
            append("]}}")
        }

        val error = inspectAdmissionFailure("million-entry", json)

        assertThat(error.reason).isEqualTo(BackupAdmissionReason.SECTION_LIMIT)
        assertThat(error.message).contains("preset entries")
    }

    @Test
    fun `inspectBackup rejects overlong strings before object allocation`() = runBlocking {
        val json = """{"version":9,"preferences":{"key":"${"x".repeat(8_193)}"}}"""

        val error = inspectAdmissionFailure("long-string", json)

        assertThat(error.reason).isEqualTo(BackupAdmissionReason.FIELD_LIMIT)
    }

    @Test
    fun `inspectBackup reports malformed and truncated json as typed admission failures`() = runBlocking {
        listOf(
            """{"version":9,"preferences":""",
            """{"version":9,"preferences":{"key":]}"""
        ).forEachIndexed { index, json ->
            val error = inspectAdmissionFailure("malformed-$index", json)
            assertThat(error.reason).isEqualTo(BackupAdmissionReason.MALFORMED)
        }
    }

    @Test
    fun `inspectBackup rejects unsupported version before reading sections`() = runBlocking {
        val error = inspectAdmissionFailure(
            "unsupported-version",
            """{"version":11,"preferences":{"value":"${"x".repeat(8_193)}"}}"""
        )

        assertThat(error.reason).isEqualTo(BackupAdmissionReason.UNSUPPORTED_VERSION)
    }

    @Test
    fun `inspectBackup rejects a missing or displaced version header`() = runBlocking {
        listOf(
            "{}",
            """{"preferences":{},"version":9}"""
        ).forEachIndexed { index, json ->
            val error = inspectAdmissionFailure("missing-header-$index", json)
            assertThat(error.reason).isEqualTo(BackupAdmissionReason.MALFORMED)
        }
    }

    @Test
    fun `inspectBackup rejects duplicate top level fields`() = runBlocking {
        val error = inspectAdmissionFailure(
            "duplicate-field",
            """{"version":9,"preferences":{},"preferences":{}}"""
        )

        assertThat(error.reason).isEqualTo(BackupAdmissionReason.DUPLICATE_FIELD)
    }

    @Test
    fun `inspectBackup propagates cancellation during streaming read`() {
        val context: Context = mock()
        val contentResolver: ContentResolver = mock()
        whenever(context.contentResolver).thenReturn(contentResolver)
        whenever(contentResolver.openInputStream(Uri.parse("content://cancel-mid-read"))).thenReturn(
            CancellingInputStream("""{"version":9,"preferences":{""".toByteArray())
        )

        assertThrows(CancellationException::class.java) {
            runBlocking {
                backupManagerForValidation(context).inspectBackup("content://cancel-mid-read")
            }
        }
    }

    @Test
    fun `recording conflict benchmark remains linear for duplicate heavy sections`() {
        val provider = ProviderEntity(
            id = 7L,
            name = "Provider",
            type = ProviderType.M3U,
            serverUrl = "https://example.com",
            username = "user"
        )
        val incomingItem = ScheduledRecordingBackup(
            providerServerUrl = provider.serverUrl,
            providerUsername = provider.username,
            channelId = 100L,
            channelName = "News",
            streamUrl = "https://example.com/live",
            scheduledStartMs = 1_700_000_000_000L,
            scheduledEndMs = 1_700_000_060_000L
        )
        val existingItem = RecordingItem(
            id = "existing",
            providerId = provider.id,
            channelId = incomingItem.channelId,
            channelName = incomingItem.channelName,
            streamUrl = incomingItem.streamUrl,
            scheduledStartMs = incomingItem.scheduledStartMs,
            scheduledEndMs = incomingItem.scheduledEndMs,
            status = RecordingStatus.SCHEDULED
        )
        val incoming = List(20_000) { incomingItem }
        val existing = List(20_000) { existingItem }
        var conflicts = 0

        val elapsedMs = measureTimeMillis {
            conflicts = backupManagerForValidation(mock()).countScheduledRecordingConflicts(
                incoming = incoming,
                providersByIdentity = mapOf(provider.backupIdentityForTest() to provider),
                existing = existing
            )
        }

        assertThat(conflicts).isEqualTo(incoming.size)
        assertThat(elapsedMs).isLessThan(5_000L)
    }

    @Test
    fun `inspectBackup rejects structurally empty json instead of previewing version zero`() = runBlocking {
        val context: Context = mock()
        val contentResolver: ContentResolver = mock()
        whenever(context.contentResolver).thenReturn(contentResolver)
        whenever(contentResolver.openInputStream(Uri.parse("content://empty-backup"))).thenReturn(
            ByteArrayInputStream("{}".toByteArray())
        )

        val manager = backupManagerForValidation(context = context)

        val result = manager.inspectBackup("content://empty-backup")

        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat((result as Result.Error).exception).isInstanceOf(BackupAdmissionException::class.java)
        assertThat((result.exception as BackupAdmissionException).reason)
            .isEqualTo(BackupAdmissionReason.MALFORMED)
    }

    @Test
    fun `importConfig rejects structurally empty json before mutating data`() = runBlocking {
        val context: Context = mock()
        val contentResolver: ContentResolver = mock()
        val preferencesRepository: PreferencesRepository = mock()
        whenever(context.contentResolver).thenReturn(contentResolver)
        whenever(contentResolver.openInputStream(Uri.parse("content://empty-backup-import"))).thenReturn(
            ByteArrayInputStream("{}".toByteArray())
        )

        val manager = backupManagerForValidation(
            context = context,
            preferencesRepository = preferencesRepository
        )

        val result = manager.importConfig("content://empty-backup-import")

        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat((result as Result.Error).exception).isInstanceOf(BackupAdmissionException::class.java)
        assertThat((result.exception as BackupAdmissionException).reason)
            .isEqualTo(BackupAdmissionReason.MALFORMED)
        verify(preferencesRepository, never()).setParentalControlLevel(any())
    }

    @Test
    fun `import strips Stalker transport consent and requires attention for HTTP`() = runBlocking {
        val context: Context = mock()
        val contentResolver: ContentResolver = mock()
        val providerDao: ProviderDao = mock()
        val credentialCrypto: CredentialCrypto = mock()
        val gson = Gson()
        val backup = BackupData(
            providers = listOf(
                Provider(
                    id = 9L,
                    name = "MAG",
                    type = ProviderType.STALKER_PORTAL,
                    serverUrl = "http://portal.example.com/c/",
                    stalkerMacAddress = "00:1A:79:12:34:56",
                    stalkerTransportMode = StalkerTransportMode.USER_ACCEPTED_HTTP,
                    stalkerTransportOrigin = "http://portal.example.com",
                    stalkerTlsSpkiSha256 = "sha256/must-not-survive",
                    stalkerTransportConsentAt = 1234L,
                    isActive = true,
                    status = ProviderStatus.ACTIVE
                )
            )
        )
        whenever(context.contentResolver).thenReturn(contentResolver)
        whenever(contentResolver.openInputStream(Uri.parse("content://stalker-backup"))).thenReturn(
            ByteArrayInputStream(gson.toJson(backup).toByteArray())
        )
        whenever(providerDao.getAllSync()).thenReturn(emptyList())
        whenever(credentialCrypto.encryptIfNeeded(any())).thenAnswer { invocation ->
            invocation.arguments.first() as String
        }
        val manager = BackupManagerImpl(
            context = context,
            preferencesRepository = mock(),
            credentialCrypto = credentialCrypto,
            providerDao = providerDao,
            favoriteDao = mock(),
            virtualGroupDao = mock(),
            playbackHistoryDao = mock(),
            movieDao = mock(),
            episodeDao = mock(),
            channelDao = mock(),
            categoryRepository = mock(),
            recordingScheduleDao = mock(),
            recordingManager = mock(),
            transactionRunner = object : DatabaseTransactionRunner {
                override suspend fun <T> inTransaction(block: suspend () -> T): T = block()
            },
            gson = gson
        )

        val result = manager.importConfig(
            uriString = "content://stalker-backup",
            plan = BackupImportPlan(
                importPreferences = false,
                importProviders = true,
                importSavedLibrary = false,
                importPlaybackHistory = false,
                importMultiViewPresets = false,
                importRecordingSchedules = false
            )
        )

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val inserted = org.mockito.kotlin.argumentCaptor<ProviderEntity>()
        verify(providerDao).insert(inserted.capture())
        assertThat(inserted.firstValue.stalkerTransportMode)
            .isEqualTo(StalkerTransportMode.AUTO_STRICT)
        assertThat(inserted.firstValue.stalkerTransportOrigin).isEmpty()
        assertThat(inserted.firstValue.stalkerTlsSpkiSha256).isEmpty()
        assertThat(inserted.firstValue.stalkerTransportConsentAt).isEqualTo(0L)
        assertThat(inserted.firstValue.isActive).isFalse()
        assertThat(inserted.firstValue.status).isEqualTo(ProviderStatus.PARTIAL)
    }

    @Test
    fun `importConfig replace history only deletes imported providers and resyncs them in transaction`() = runBlocking {
        val context: Context = mock()
        val contentResolver: ContentResolver = mock()
        whenever(context.contentResolver).thenReturn(contentResolver)

        val providerDao: ProviderDao = mock()
        val playbackHistoryDao: PlaybackHistoryDao = mock()
        val movieDao: MovieDao = mock()
        val episodeDao: EpisodeDao = mock()
        val transactionRunner = RecordingTransactionRunner()
        val gson = Gson()

        val backupProvider = Provider(
            id = 100L,
            name = "Provider",
            type = ProviderType.M3U,
            serverUrl = "https://example.com",
            username = "user",
            password = "",
            stalkerMacAddress = ""
        )
        val backupData = BackupData(
            providers = listOf(backupProvider),
            playbackHistory = listOf(
                PlaybackHistory(
                    contentId = 55L,
                    contentType = ContentType.MOVIE,
                    providerId = 100L,
                    title = "Movie",
                    streamUrl = "https://stream.example.test/movie.mp4",
                    resumePositionMs = 12_000L,
                    totalDurationMs = 5_400_000L
                )
            )
        )
        whenever(contentResolver.openInputStream(Uri.parse("content://backup"))).thenReturn(
            ByteArrayInputStream(gson.toJson(backupData).toByteArray())
        )
        whenever(providerDao.getAllSync()).thenReturn(
            listOf(
                ProviderEntity(
                    id = 7L,
                    name = "Stored Provider",
                    type = ProviderType.M3U,
                    serverUrl = "https://example.com",
                    username = "user"
                )
            )
        )

        val manager = BackupManagerImpl(
            context = context,
            preferencesRepository = mock<PreferencesRepository>(),
            credentialCrypto = mock<CredentialCrypto>(),
            providerDao = providerDao,
            favoriteDao = mock<FavoriteDao>(),
            virtualGroupDao = mock<VirtualGroupDao>(),
            playbackHistoryDao = playbackHistoryDao,
            movieDao = movieDao,
            episodeDao = episodeDao,
            categoryRepository = mock<CategoryRepository>(),
            recordingScheduleDao = mock<RecordingScheduleDao>(),
            recordingManager = mock<RecordingManager>(),
            transactionRunner = transactionRunner,
            gson = gson,
            channelDao = mock()
        )

        val result = manager.importConfig(
            uriString = "content://backup",
            plan = BackupImportPlan(
                importPreferences = false,
                importProviders = false,
                importSavedLibrary = false,
                importPlaybackHistory = true,
                importMultiViewPresets = false,
                importRecordingSchedules = false,
                conflictStrategy = BackupConflictStrategy.REPLACE_EXISTING
            )
        )

        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertThat(transactionRunner.calls).isEqualTo(1)
        verify(playbackHistoryDao).deleteByProvider(7L)
        verify(playbackHistoryDao, never()).deleteAll()
        verify(playbackHistoryDao).insertOrUpdate(argThat {
            providerId == 7L &&
                contentId == 55L &&
                contentType == ContentType.MOVIE
        })
        verify(movieDao).syncWatchProgressFromHistoryByProvider(7L)
        verify(episodeDao).syncWatchProgressFromHistoryByProvider(7L)
        verify(movieDao, never()).syncAllWatchProgressFromHistory()
        verify(episodeDao, never()).syncAllWatchProgressFromHistory()
    }

    @Test
    fun `importConfig keeps saved library and history writes inside one room transaction`() = runBlocking {
        val context: Context = mock()
        val contentResolver: ContentResolver = mock()
        whenever(context.contentResolver).thenReturn(contentResolver)

        val providerDao: ProviderDao = mock()
        val favoriteDao: FavoriteDao = mock()
        val playbackHistoryDao: PlaybackHistoryDao = mock()
        val movieDao: MovieDao = mock()
        val episodeDao: EpisodeDao = mock()
        val transactionRunner = RecordingTransactionRunner()
        val gson = Gson()
        val backupProvider = Provider(
            id = 100L,
            name = "Provider",
            type = ProviderType.M3U,
            serverUrl = "https://example.com",
            username = "user",
            password = "",
            stalkerMacAddress = ""
        )
        val backupData = BackupData(
            providers = listOf(backupProvider),
            favorites = listOf(
                Favorite(
                    providerId = 100L,
                    contentId = 88L,
                    contentType = ContentType.MOVIE,
                    position = 0
                )
            ),
            playbackHistory = listOf(
                PlaybackHistory(
                    contentId = 55L,
                    contentType = ContentType.MOVIE,
                    providerId = 100L,
                    title = "Movie",
                    streamUrl = "https://stream.example.test/movie.mp4",
                    watchCount = 1
                )
            )
        )
        whenever(contentResolver.openInputStream(Uri.parse("content://backup-transaction"))).thenReturn(
            ByteArrayInputStream(gson.toJson(backupData).toByteArray())
        )
        whenever(providerDao.getAllSync()).thenReturn(
            listOf(
                ProviderEntity(
                    id = 7L,
                    name = "Stored Provider",
                    type = ProviderType.M3U,
                    serverUrl = "https://example.com",
                    username = "user"
                )
            )
        )
        whenever(favoriteDao.get(any(), any(), any(), any())).thenReturn(null)
        doAnswer {
            assertThat(transactionRunner.isInTransaction).isTrue()
            1L
        }.whenever(favoriteDao).insert(any())
        doAnswer {
            assertThat(transactionRunner.isInTransaction).isTrue()
            Unit
        }.whenever(playbackHistoryDao).deleteByProvider(any())
        doAnswer {
            assertThat(transactionRunner.isInTransaction).isTrue()
            Unit
        }.whenever(playbackHistoryDao).insertOrUpdate(any())
        doAnswer {
            assertThat(transactionRunner.isInTransaction).isTrue()
            Unit
        }.whenever(movieDao).syncWatchProgressFromHistoryByProvider(any())
        doAnswer {
            assertThat(transactionRunner.isInTransaction).isTrue()
            Unit
        }.whenever(episodeDao).syncWatchProgressFromHistoryByProvider(any())

        val manager = BackupManagerImpl(
            context = context,
            preferencesRepository = mock<PreferencesRepository>(),
            credentialCrypto = mock<CredentialCrypto>(),
            providerDao = providerDao,
            favoriteDao = favoriteDao,
            virtualGroupDao = mock<VirtualGroupDao>(),
            playbackHistoryDao = playbackHistoryDao,
            movieDao = movieDao,
            episodeDao = episodeDao,
            categoryRepository = mock<CategoryRepository>(),
            recordingScheduleDao = mock<RecordingScheduleDao>(),
            recordingManager = mock<RecordingManager>(),
            transactionRunner = transactionRunner,
            gson = gson,
            channelDao = mock()
        )

        val result = manager.importConfig(
            uriString = "content://backup-transaction",
            plan = BackupImportPlan(
                importPreferences = false,
                importProviders = false,
                importSavedLibrary = true,
                importPlaybackHistory = true,
                importMultiViewPresets = false,
                importRecordingSchedules = false,
                conflictStrategy = BackupConflictStrategy.REPLACE_EXISTING
            )
        )

        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertThat(transactionRunner.calls).isEqualTo(1)
        verify(favoriteDao).insert(any())
        verify(playbackHistoryDao).insertOrUpdate(any())
    }

    @Test
    fun `importConfig does not restore preferences before room-backed import succeeds`() = runBlocking {
        val context: Context = mock()
        val contentResolver: ContentResolver = mock()
        whenever(context.contentResolver).thenReturn(contentResolver)

        val providerDao: ProviderDao = mock()
        val favoriteDao: FavoriteDao = mock()
        val preferencesRepository: PreferencesRepository = mock()
        val gson = Gson()
        val backupProvider = Provider(
            id = 100L,
            name = "Provider",
            type = ProviderType.M3U,
            serverUrl = "https://example.com",
            username = "user",
            password = "",
            stalkerMacAddress = ""
        )
        val backupData = BackupData(
            preferences = mapOf("parentalControlLevel" to "4"),
            providers = listOf(backupProvider),
            favorites = listOf(
                Favorite(
                    providerId = 100L,
                    contentId = 88L,
                    contentType = ContentType.MOVIE,
                    position = 0
                )
            )
        )
        whenever(contentResolver.openInputStream(Uri.parse("content://backup-preferences-order"))).thenReturn(
            ByteArrayInputStream(gson.toJson(backupData).toByteArray())
        )
        whenever(providerDao.getAllSync()).thenReturn(
            listOf(
                ProviderEntity(
                    id = 7L,
                    name = "Stored Provider",
                    type = ProviderType.M3U,
                    serverUrl = "https://example.com",
                    username = "user"
                )
            )
        )
        whenever(favoriteDao.get(any(), any(), any(), any())).thenReturn(null)
        whenever(favoriteDao.insert(any())).thenThrow(IllegalStateException("favorite insert failed"))

        val manager = BackupManagerImpl(
            context = context,
            preferencesRepository = preferencesRepository,
            credentialCrypto = mock<CredentialCrypto>(),
            providerDao = providerDao,
            favoriteDao = favoriteDao,
            virtualGroupDao = mock<VirtualGroupDao>(),
            playbackHistoryDao = mock<PlaybackHistoryDao>(),
            movieDao = mock<MovieDao>(),
            episodeDao = mock<EpisodeDao>(),
            categoryRepository = mock<CategoryRepository>(),
            recordingScheduleDao = mock<RecordingScheduleDao>(),
            recordingManager = mock<RecordingManager>(),
            transactionRunner = object : DatabaseTransactionRunner {
                override suspend fun <T> inTransaction(block: suspend () -> T): T = block()
            },
            gson = gson,
            channelDao = mock()
        )

        val result = manager.importConfig(
            uriString = "content://backup-preferences-order",
            plan = BackupImportPlan(
                importPreferences = true,
                importProviders = false,
                importSavedLibrary = true,
                importPlaybackHistory = false,
                importMultiViewPresets = false,
                importRecordingSchedules = false,
                conflictStrategy = BackupConflictStrategy.REPLACE_EXISTING
            )
        )

        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertThat((result as Result.Success).data.outcome)
            .isEqualTo(BackupRestoreOutcome.FAILED_BEFORE_COMMIT)
        verify(preferencesRepository, never()).setParentalControlLevel(any())
    }

    @Test
    fun `importConfig reports a preference write failure as partial after room commit`() = runBlocking {
        val context: Context = mock()
        val contentResolver: ContentResolver = mock()
        val preferencesRepository: PreferencesRepository = mock()
        val providerDao: ProviderDao = mock()
        whenever(context.contentResolver).thenReturn(contentResolver)
        whenever(providerDao.getAllSync()).thenReturn(emptyList())
        whenever(contentResolver.openInputStream(Uri.parse("content://backup-preference-partial"))).thenReturn(
            ByteArrayInputStream(Gson().toJson(BackupData(preferences = mapOf("parentalControlLevel" to "4"))).toByteArray())
        )
        doThrow(IllegalStateException("DataStore unavailable"))
            .whenever(preferencesRepository).setParentalControlLevel(4)

        val result = backupManagerForValidation(
            context = context,
            preferencesRepository = preferencesRepository,
            providerDao = providerDao
        ).importConfig(
            uriString = "content://backup-preference-partial",
            plan = BackupImportPlan(
                importPreferences = true,
                importProviders = false,
                importSavedLibrary = false,
                importPlaybackHistory = false,
                importMultiViewPresets = false,
                importRecordingSchedules = false
            )
        )

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val import = (result as Result.Success).data
        assertThat(import.outcome).isEqualTo(BackupRestoreOutcome.PARTIAL)
        assertThat(import.failedSections.single()).contains("Preferences: DataStore unavailable")
    }

    @Test
    fun `importConfig clears optional guide preferences when backup has no selected value`() = runBlocking {
        val context: Context = mock()
        val contentResolver: ContentResolver = mock()
        val preferencesRepository: PreferencesRepository = mock()
        val providerDao: ProviderDao = mock()
        whenever(context.contentResolver).thenReturn(contentResolver)
        whenever(providerDao.getAllSync()).thenReturn(emptyList())
        whenever(contentResolver.openInputStream(Uri.parse("content://backup-guide-defaults"))).thenReturn(
            ByteArrayInputStream(
                Gson().toJson(
                    BackupData(
                        preferences = mapOf(
                            "guideDefaultCategoryId" to "0",
                            "guideAnchorTime" to "0"
                        )
                    )
                ).toByteArray()
            )
        )

        val result = backupManagerForValidation(
            context = context,
            preferencesRepository = preferencesRepository,
            providerDao = providerDao
        ).importConfig(
            uriString = "content://backup-guide-defaults",
            plan = BackupImportPlan(
                importPreferences = true,
                importProviders = false,
                importSavedLibrary = false,
                importPlaybackHistory = false,
                importMultiViewPresets = false,
                importRecordingSchedules = false
            )
        )

        assertThat(result).isInstanceOf(Result.Success::class.java)
        verify(preferencesRepository).clearGuideDefaultCategoryId()
        verify(preferencesRepository).clearGuideAnchorTime()
        Unit
    }

    @Test
    fun `importConfig propagates cancellation without reporting a false outcome`() = runBlocking {
        val context: Context = mock()
        val contentResolver: ContentResolver = mock()
        whenever(context.contentResolver).thenReturn(contentResolver)
        doThrow(CancellationException("cancelled"))
            .whenever(contentResolver).openInputStream(Uri.parse("content://backup-cancelled"))
        val manager = backupManagerForValidation(context = context)

        assertThrows(CancellationException::class.java) {
            runBlocking { manager.importConfig("content://backup-cancelled") }
        }
        Unit
    }

    @Test
    fun `importConfig reports unresolved portable provider preferences without applying raw ids`() = runBlocking {
        val context: Context = mock()
        val contentResolver: ContentResolver = mock()
        val providerDao: ProviderDao = mock()
        val preferencesRepository: PreferencesRepository = mock()
        whenever(context.contentResolver).thenReturn(contentResolver)
        whenever(providerDao.getAllSync()).thenReturn(emptyList())
        whenever(contentResolver.openInputStream(Uri.parse("content://portable-unresolved"))).thenReturn(
            ByteArrayInputStream(
                Gson().toJson(
                    BackupData(
                        preferences = mapOf("hiddenChannels_2" to "99"),
                        portableProviderPreferences = PortableProviderPreferencesBackup(
                            providers = listOf(BackupProviderReference("https://missing", "user"))
                        )
                    )
                ).toByteArray()
            )
        )

        val result = backupManagerForValidation(
            context = context,
            preferencesRepository = preferencesRepository,
            providerDao = providerDao,
            channelDao = mock()
        ).importConfig(
            "content://portable-unresolved",
            BackupImportPlan(
                importPreferences = true,
                importProviders = false,
                importSavedLibrary = false,
                importPlaybackHistory = false,
                importMultiViewPresets = false,
                importRecordingSchedules = false
            )
        )

        val imported = (result as Result.Success).data
        assertThat(imported.outcome).isEqualTo(BackupRestoreOutcome.PARTIAL)
        assertThat(imported.unresolvedReferences).contains("Provider https://missing (user)")
        verify(preferencesRepository, never()).setHiddenChannelIds(2L, setOf(99L))
        Unit
    }

    @Test
    fun `portable export uses semantic identities instead of local provider category group and channel ids`() = runBlocking {
        val context: Context = mock()
        val preferencesRepository: PreferencesRepository = mock()
        val categoryRepository: CategoryRepository = mock()
        val virtualGroupDao: VirtualGroupDao = mock()
        val channelDao: ChannelDao = mock()
        val provider = ProviderEntity(
            id = 2L,
            name = "Source",
            type = ProviderType.XTREAM_CODES,
            serverUrl = "https://example.com",
            username = "user"
        )
        val category = Category(
            id = 50L,
            roomId = 500L,
            name = "News",
            type = ContentType.LIVE
        )
        whenever(preferencesRepository.lastActiveProviderId).thenReturn(flowOf(provider.id))
        whenever(preferencesRepository.guideDefaultCategoryId).thenReturn(flowOf(category.id))
        whenever(preferencesRepository.promotedLiveGroupIds).thenReturn(flowOf(setOf(60L)))
        whenever(preferencesRepository.getHiddenChannelIds(provider.id)).thenReturn(flowOf(setOf(40L)))
        whenever(preferencesRepository.getHiddenCategoryIds(eq(provider.id), any())).thenReturn(flowOf(emptySet()))
        whenever(preferencesRepository.getHiddenCategoryIds(provider.id, ContentType.LIVE))
            .thenReturn(flowOf(setOf(category.id)))
        whenever(categoryRepository.getCategories(provider.id)).thenReturn(flowOf(listOf(category)))
        whenever(virtualGroupDao.getById(60L)).thenReturn(
            VirtualGroupEntity(
                id = 60L,
                providerId = provider.id,
                name = "My News",
                contentType = ContentType.LIVE
            )
        )
        whenever(channelDao.getById(40L)).thenReturn(
            ChannelEntity(
                id = 40L,
                streamId = 400L,
                name = "World News",
                streamUrl = "https://example.com/live/400",
                providerId = provider.id
            )
        )

        val portable = backupManagerForValidation(
            context = context,
            preferencesRepository = preferencesRepository,
            categoryRepository = categoryRepository,
            virtualGroupDao = virtualGroupDao,
            channelDao = channelDao
        ).buildPortableProviderPreferences(listOf(provider))

        val providerReference = BackupProviderReference(provider.serverUrl, provider.username)
        assertThat(portable.providers).containsExactly(providerReference)
        assertThat(portable.activeProvider).isEqualTo(providerReference)
        assertThat(portable.guideDefaultCategory?.remoteCategoryId).isEqualTo(50L)
        assertThat(portable.guideDefaultCategory?.name).isEqualTo("News")
        assertThat(portable.promotedLiveGroups.single().name).isEqualTo("My News")
        assertThat(portable.hiddenChannels.single().streamId).isEqualTo(400L)
        assertThat(portable.hiddenChannels.single().name).isEqualTo("World News")
        assertThat(portable.hiddenCategories.single().remoteCategoryId).isEqualTo(50L)
        assertThat(portable.unresolvedReferences).isEmpty()
    }

    @Test
    fun `semantic json round trip resolves shifted target ids after room restore`() = runBlocking {
        val context: Context = mock()
        val contentResolver: ContentResolver = mock()
        val preferencesRepository: PreferencesRepository = mock()
        val providerDao: ProviderDao = mock()
        val categoryRepository: CategoryRepository = mock()
        val virtualGroupDao: VirtualGroupDao = mock()
        val channelDao: ChannelDao = mock()
        val providerReference = BackupProviderReference("https://example.com", "user")
        val targetProvider = ProviderEntity(
            id = 7L,
            name = "Target",
            type = ProviderType.XTREAM_CODES,
            serverUrl = providerReference.serverUrl,
            username = providerReference.username
        )
        val portable = PortableProviderPreferencesBackup(
            providers = listOf(providerReference),
            activeProvider = providerReference,
            guideDefaultCategory = PortableCategoryReference(
                provider = providerReference,
                name = "News",
                type = ContentType.LIVE,
                remoteCategoryId = 50L
            ),
            guideDefaultCategorySpecified = true,
            promotedLiveGroups = listOf(
                PortableVirtualGroupReference(providerReference, "My News", ContentType.LIVE)
            ),
            hiddenChannels = listOf(
                PortableChannelReference(
                    provider = providerReference,
                    streamId = 400L,
                    name = "World News",
                    streamUrl = "https://example.com/live/400"
                )
            ),
            hiddenCategories = listOf(
                PortableCategoryReference(providerReference, "News", ContentType.LIVE, 50L)
            )
        )
        whenever(context.contentResolver).thenReturn(contentResolver)
        whenever(providerDao.getAllSync()).thenReturn(listOf(targetProvider))
        whenever(categoryRepository.getCategories(targetProvider.id)).thenReturn(
            flowOf(listOf(Category(id = 150L, roomId = 1_500L, name = "News", type = ContentType.LIVE)))
        )
        whenever(virtualGroupDao.getByType(targetProvider.id, ContentType.LIVE.name)).thenReturn(
            flowOf(
                listOf(
                    VirtualGroupEntity(
                        id = 160L,
                        providerId = targetProvider.id,
                        name = "My News",
                        contentType = ContentType.LIVE
                    )
                )
            )
        )
        whenever(channelDao.getByProviderSync(targetProvider.id)).thenReturn(
            listOf(
                ChannelEntity(
                    id = 140L,
                    streamId = 400L,
                    name = "World News",
                    streamUrl = "https://example.com/live/400",
                    providerId = targetProvider.id
                )
            )
        )
        whenever(contentResolver.openInputStream(Uri.parse("content://portable-shifted"))).thenReturn(
            ByteArrayInputStream(
                Gson().toJson(
                    BackupData(
                        version = 9,
                        preferences = mapOf(
                            "guideDefaultCategoryId" to "50",
                            "promotedLiveGroupIds" to "60",
                            "hiddenChannels_2" to "40",
                            "hiddenCategories_2_LIVE" to "50"
                        ),
                        portableProviderPreferences = portable
                    )
                ).toByteArray()
            )
        )

        val result = backupManagerForValidation(
            context = context,
            preferencesRepository = preferencesRepository,
            providerDao = providerDao,
            categoryRepository = categoryRepository,
            virtualGroupDao = virtualGroupDao,
            channelDao = channelDao
        ).importConfig("content://portable-shifted", preferencesOnlyPlan())

        val imported = (result as Result.Success).data
        assertThat(imported.outcome).isEqualTo(BackupRestoreOutcome.COMPLETE)
        assertThat(imported.unresolvedReferences).isEmpty()
        verify(preferencesRepository).setLastActiveProviderId(7L)
        verify(preferencesRepository).setGuideDefaultCategoryId(150L)
        verify(preferencesRepository).setPromotedLiveGroupIds(setOf(160L))
        verify(preferencesRepository).setHiddenChannelIds(7L, setOf(140L))
        verify(preferencesRepository).setHiddenCategoryIds(7L, ContentType.LIVE, setOf(150L))
        verify(preferencesRepository, never()).setHiddenChannelIds(2L, setOf(40L))
    }

    @Test
    fun `duplicate semantic group names remain unresolved and are not guessed`() = runBlocking {
        val context: Context = mock()
        val contentResolver: ContentResolver = mock()
        val preferencesRepository: PreferencesRepository = mock()
        val providerDao: ProviderDao = mock()
        val categoryRepository: CategoryRepository = mock()
        val virtualGroupDao: VirtualGroupDao = mock()
        val channelDao: ChannelDao = mock()
        val reference = BackupProviderReference("https://example.com", "user")
        val targetProvider = ProviderEntity(
            id = 7L,
            name = "Target",
            type = ProviderType.XTREAM_CODES,
            serverUrl = reference.serverUrl,
            username = reference.username
        )
        whenever(context.contentResolver).thenReturn(contentResolver)
        whenever(providerDao.getAllSync()).thenReturn(listOf(targetProvider))
        whenever(categoryRepository.getCategories(targetProvider.id)).thenReturn(flowOf(emptyList()))
        whenever(channelDao.getByProviderSync(targetProvider.id)).thenReturn(emptyList())
        whenever(virtualGroupDao.getByType(targetProvider.id, ContentType.LIVE.name)).thenReturn(
            flowOf(
                listOf(
                    VirtualGroupEntity(10L, targetProvider.id, "News", contentType = ContentType.LIVE),
                    VirtualGroupEntity(11L, targetProvider.id, "NEWS", contentType = ContentType.LIVE)
                )
            )
        )
        whenever(contentResolver.openInputStream(Uri.parse("content://portable-duplicate-group"))).thenReturn(
            ByteArrayInputStream(
                Gson().toJson(
                    BackupData(
                        portableProviderPreferences = PortableProviderPreferencesBackup(
                            providers = listOf(reference),
                            promotedLiveGroups = listOf(
                                PortableVirtualGroupReference(reference, "News", ContentType.LIVE)
                            )
                        )
                    )
                ).toByteArray()
            )
        )

        val result = backupManagerForValidation(
            context = context,
            preferencesRepository = preferencesRepository,
            providerDao = providerDao,
            categoryRepository = categoryRepository,
            virtualGroupDao = virtualGroupDao,
            channelDao = channelDao
        ).importConfig("content://portable-duplicate-group", preferencesOnlyPlan())

        val imported = (result as Result.Success).data
        assertThat(imported.outcome).isEqualTo(BackupRestoreOutcome.PARTIAL)
        assertThat(imported.unresolvedReferences)
            .contains("Group News [LIVE] at https://example.com")
        verify(preferencesRepository).setPromotedLiveGroupIds(emptySet())
    }

    @Test
    fun `legacy v8 provider scoped ids remain restorable`() = runBlocking {
        val context: Context = mock()
        val contentResolver: ContentResolver = mock()
        val preferencesRepository: PreferencesRepository = mock()
        val providerDao: ProviderDao = mock()
        whenever(context.contentResolver).thenReturn(contentResolver)
        whenever(providerDao.getAllSync()).thenReturn(emptyList())
        whenever(contentResolver.openInputStream(Uri.parse("content://legacy-v8"))).thenReturn(
            ByteArrayInputStream(
                Gson().toJson(
                    BackupData(
                        version = 8,
                        preferences = mapOf(
                            "guideDefaultCategoryId" to "50",
                            "promotedLiveGroupIds" to "60",
                            "hiddenChannels_2" to "40",
                            "hiddenCategories_2_LIVE" to "50"
                        )
                    )
                ).toByteArray()
            )
        )

        val result = backupManagerForValidation(
            context = context,
            preferencesRepository = preferencesRepository,
            providerDao = providerDao
        ).importConfig("content://legacy-v8", preferencesOnlyPlan())

        assertThat((result as Result.Success).data.outcome).isEqualTo(BackupRestoreOutcome.COMPLETE)
        verify(preferencesRepository).setGuideDefaultCategoryId(50L)
        verify(preferencesRepository).setPromotedLiveGroupIds(setOf(60L))
        verify(preferencesRepository).setHiddenChannelIds(2L, setOf(40L))
        verify(preferencesRepository).setHiddenCategoryIds(2L, ContentType.LIVE, setOf(50L))
    }

    @Test
    fun `portable preferences map to the target provider for keep and replace conflicts`() = runBlocking {
        BackupConflictStrategy.entries.forEach { conflictStrategy ->
            val context: Context = mock()
            val contentResolver: ContentResolver = mock()
            val preferencesRepository: PreferencesRepository = mock()
            val providerDao: ProviderDao = mock()
            val categoryRepository: CategoryRepository = mock()
            val virtualGroupDao: VirtualGroupDao = mock()
            val channelDao: ChannelDao = mock()
            val credentialCrypto: CredentialCrypto = mock()
            val sourceProvider = Provider(
                id = 2L,
                name = "Source",
                type = ProviderType.XTREAM_CODES,
                serverUrl = "https://example.com",
                username = "user"
            )
            val targetProvider = ProviderEntity(
                id = 7L,
                name = "Target",
                type = ProviderType.XTREAM_CODES,
                serverUrl = sourceProvider.serverUrl,
                username = sourceProvider.username
            )
            val reference = BackupProviderReference(sourceProvider.serverUrl, sourceProvider.username)
            val uriString = "content://portable-conflict-${conflictStrategy.name}"
            var insertCalls = 0
            whenever(context.contentResolver).thenReturn(contentResolver)
            whenever(providerDao.getAllSync()).thenReturn(listOf(targetProvider))
            doAnswer {
                insertCalls += 1
                targetProvider.id
            }.whenever(providerDao).insert(any())
            whenever(credentialCrypto.encryptIfNeeded(any())).thenReturn("")
            whenever(categoryRepository.getCategories(targetProvider.id)).thenReturn(flowOf(emptyList()))
            whenever(channelDao.getByProviderSync(targetProvider.id)).thenReturn(emptyList())
            whenever(contentResolver.openInputStream(Uri.parse(uriString))).thenReturn(
                ByteArrayInputStream(
                    Gson().toJson(
                        BackupData(
                            providers = listOf(sourceProvider),
                            portableProviderPreferences = PortableProviderPreferencesBackup(
                                providers = listOf(reference)
                            )
                        )
                    ).toByteArray()
                )
            )

            val result = backupManagerForValidation(
                context = context,
                preferencesRepository = preferencesRepository,
                providerDao = providerDao,
                categoryRepository = categoryRepository,
                virtualGroupDao = virtualGroupDao,
                channelDao = channelDao,
                credentialCrypto = credentialCrypto
            ).importConfig(
                uriString,
                preferencesOnlyPlan().copy(
                    importProviders = true,
                    conflictStrategy = conflictStrategy
                )
            )

            assertThat((result as Result.Success).data.outcome)
                .isEqualTo(BackupRestoreOutcome.COMPLETE)
            verify(preferencesRepository).setHiddenChannelIds(targetProvider.id, emptySet())
            assertThat(insertCalls).isEqualTo(
                if (conflictStrategy == BackupConflictStrategy.KEEP_EXISTING) 0 else 1
            )
        }
    }

    @Test
    fun `importConfig checkpoints room completion in the same durable restore`() = runBlocking {
        val context: Context = mock()
        val contentResolver: ContentResolver = mock()
        val providerDao: ProviderDao = mock()
        val checkpointDao: BackupRestoreCheckpointDao = mock()
        val gson = Gson()
        var checkpoint: BackupRestoreCheckpointEntity? = null
        whenever(context.contentResolver).thenReturn(contentResolver)
        whenever(providerDao.getAllSync()).thenReturn(emptyList())
        whenever(contentResolver.openInputStream(Uri.parse("content://backup-checkpoint"))).thenReturn(
            ByteArrayInputStream(
                gson.toJson(
                    BackupData(
                        providers = listOf(
                            Provider(
                                id = 9L,
                                name = "backup provider",
                                type = ProviderType.M3U,
                                serverUrl = "https://example.com"
                            )
                        )
                    )
                ).toByteArray()
            )
        )
        whenever(checkpointDao.get(any())).thenAnswer { checkpoint }
        doAnswer { invocation ->
            checkpoint = invocation.getArgument(0)
            1L
        }.whenever(checkpointDao).insertIfAbsent(any())
        doAnswer { invocation ->
            checkpoint = checkpoint!!.copy(
                roomComplete = invocation.getArgument(1),
                preferencesComplete = invocation.getArgument(2),
                presetsComplete = invocation.getArgument(3),
                schedulesComplete = invocation.getArgument(4),
                state = invocation.getArgument(5),
                lastError = invocation.getArgument(6),
                updatedAt = invocation.getArgument(7)
            )
            1
        }.whenever(checkpointDao).update(any(), any(), any(), any(), any(), any(), anyOrNull(), any())

        val result = backupManagerForValidation(
            context = context,
            providerDao = providerDao,
            checkpointDao = checkpointDao
        ).importConfig(
            uriString = "content://backup-checkpoint",
            plan = BackupImportPlan(
                importPreferences = false,
                importProviders = false,
                importSavedLibrary = true,
                importPlaybackHistory = false,
                importMultiViewPresets = false,
                importRecordingSchedules = false
            )
        )

        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertThat(checkpoint!!.roomComplete).isTrue()
        assertThat(checkpoint!!.state).isEqualTo("COMPLETE")
    }

    @Test
    fun `importConfig retry is a no-op after the same restore checkpoint completed`() = runBlocking {
        val context: Context = mock()
        val contentResolver: ContentResolver = mock()
        val providerDao: ProviderDao = mock()
        val checkpointDao: BackupRestoreCheckpointDao = mock()
        whenever(context.contentResolver).thenReturn(contentResolver)
        whenever(contentResolver.openInputStream(Uri.parse("content://backup-complete-retry"))).thenReturn(
            ByteArrayInputStream(
                Gson().toJson(
                    BackupData(
                        providers = listOf(
                            Provider(
                                id = 3L,
                                name = "provider",
                                type = ProviderType.M3U,
                                serverUrl = "https://example.com"
                            )
                        )
                    )
                ).toByteArray()
            )
        )
        whenever(checkpointDao.get(any())).thenReturn(
            BackupRestoreCheckpointEntity(
                restoreKey = "existing",
                roomComplete = true,
                state = "COMPLETE",
                createdAt = 1L,
                updatedAt = 1L
            )
        )

        val result = backupManagerForValidation(
            context = context,
            providerDao = providerDao,
            checkpointDao = checkpointDao
        ).importConfig("content://backup-complete-retry")

        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertThat((result as Result.Success).data.outcome).isEqualTo(BackupRestoreOutcome.COMPLETE)
        verify(providerDao, never()).getAllSync()
        Unit
    }

    @Test
    fun `importConfig restores audio video sync enabled preference`() = runBlocking {
        val context: Context = mock()
        val contentResolver: ContentResolver = mock()
        whenever(context.contentResolver).thenReturn(contentResolver)

        val providerDao: ProviderDao = mock()
        val preferencesRepository: PreferencesRepository = mock()
        val gson = Gson()
        val backupData = BackupData(
            preferences = mapOf(
                "playerAudioVideoSyncEnabled" to "true",
                "playerAudioVideoOffsetMs" to "150"
            )
        )
        whenever(contentResolver.openInputStream(Uri.parse("content://backup-av-sync-preferences"))).thenReturn(
            ByteArrayInputStream(gson.toJson(backupData).toByteArray())
        )
        whenever(providerDao.getAllSync()).thenReturn(emptyList())

        val manager = BackupManagerImpl(
            context = context,
            preferencesRepository = preferencesRepository,
            credentialCrypto = mock<CredentialCrypto>(),
            providerDao = providerDao,
            favoriteDao = mock<FavoriteDao>(),
            virtualGroupDao = mock<VirtualGroupDao>(),
            playbackHistoryDao = mock<PlaybackHistoryDao>(),
            movieDao = mock<MovieDao>(),
            episodeDao = mock<EpisodeDao>(),
            categoryRepository = mock<CategoryRepository>(),
            recordingScheduleDao = mock<RecordingScheduleDao>(),
            recordingManager = mock<RecordingManager>(),
            transactionRunner = object : DatabaseTransactionRunner {
                override suspend fun <T> inTransaction(block: suspend () -> T): T = block()
            },
            gson = gson,
            channelDao = mock()
        )

        val result = manager.importConfig(
            uriString = "content://backup-av-sync-preferences",
            plan = BackupImportPlan(
                importPreferences = true,
                importProviders = false,
                importSavedLibrary = false,
                importPlaybackHistory = false,
                importMultiViewPresets = false,
                importRecordingSchedules = false,
                conflictStrategy = BackupConflictStrategy.KEEP_EXISTING
            )
        )

        assertThat(result).isInstanceOf(Result.Success::class.java)
        verify(preferencesRepository).setPlayerAudioVideoSyncEnabled(true)
        verify(preferencesRepository).setPlayerAudioVideoOffsetMs(150)
    }

    @Test
    fun `importConfig restores separate audio and video decoder preferences`() = runBlocking {
        val context: Context = mock()
        val contentResolver: ContentResolver = mock()
        val providerDao: ProviderDao = mock()
        val preferencesRepository: PreferencesRepository = mock()
        val gson = Gson()
        whenever(context.contentResolver).thenReturn(contentResolver)
        whenever(providerDao.getAllSync()).thenReturn(emptyList())
        whenever(contentResolver.openInputStream(Uri.parse("content://backup-decoder-preferences"))).thenReturn(
            ByteArrayInputStream(
                gson.toJson(
                    BackupData(
                        preferences = mapOf(
                            "playerAudioDecoderMode" to "SOFTWARE",
                            "playerVideoDecoderMode" to "HARDWARE",
                            "playerDecoderMode" to "COMPATIBILITY"
                        )
                    )
                ).toByteArray()
            )
        )
        val manager = backupManagerForValidation(
            context = context,
            preferencesRepository = preferencesRepository,
            providerDao = providerDao
        )

        val result = manager.importConfig(
            uriString = "content://backup-decoder-preferences",
            plan = BackupImportPlan(
                importPreferences = true,
                importProviders = false,
                importSavedLibrary = false,
                importPlaybackHistory = false,
                importMultiViewPresets = false,
                importRecordingSchedules = false,
                conflictStrategy = BackupConflictStrategy.KEEP_EXISTING
            )
        )

        assertThat(result).isInstanceOf(Result.Success::class.java)
        verify(preferencesRepository).setPlayerAudioDecoderMode(DecoderMode.SOFTWARE)
        verify(preferencesRepository).setPlayerVideoDecoderMode(DecoderMode.HARDWARE)
    }

    @Test
    fun `importConfig restores legacy decoder preference to both axes`() = runBlocking {
        val context: Context = mock()
        val contentResolver: ContentResolver = mock()
        val providerDao: ProviderDao = mock()
        val preferencesRepository: PreferencesRepository = mock()
        val gson = Gson()
        whenever(context.contentResolver).thenReturn(contentResolver)
        whenever(providerDao.getAllSync()).thenReturn(emptyList())
        whenever(contentResolver.openInputStream(Uri.parse("content://backup-legacy-decoder-preference"))).thenReturn(
            ByteArrayInputStream(
                gson.toJson(
                    BackupData(
                        preferences = mapOf("playerDecoderMode" to "COMPATIBILITY")
                    )
                ).toByteArray()
            )
        )
        val manager = backupManagerForValidation(
            context = context,
            preferencesRepository = preferencesRepository,
            providerDao = providerDao
        )

        val result = manager.importConfig(
            uriString = "content://backup-legacy-decoder-preference",
            plan = BackupImportPlan(
                importPreferences = true,
                importProviders = false,
                importSavedLibrary = false,
                importPlaybackHistory = false,
                importMultiViewPresets = false,
                importRecordingSchedules = false,
                conflictStrategy = BackupConflictStrategy.KEEP_EXISTING
            )
        )

        assertThat(result).isInstanceOf(Result.Success::class.java)
        verify(preferencesRepository).setPlayerAudioDecoderMode(DecoderMode.COMPATIBILITY)
        verify(preferencesRepository).setPlayerVideoDecoderMode(DecoderMode.COMPATIBILITY)
    }

    @Test
    fun `importConfig restores top navigation destinations preference`() = runBlocking {
        val context: Context = mock()
        val contentResolver: ContentResolver = mock()
        whenever(context.contentResolver).thenReturn(contentResolver)

        val providerDao: ProviderDao = mock()
        val preferencesRepository: PreferencesRepository = mock()
        val gson = Gson()
        val backupData = BackupData(
            preferences = mapOf(
                "appTopLevelDestinations" to "home,search,settings"
            )
        )
        whenever(contentResolver.openInputStream(Uri.parse("content://backup-top-navigation"))).thenReturn(
            ByteArrayInputStream(gson.toJson(backupData).toByteArray())
        )
        whenever(providerDao.getAllSync()).thenReturn(emptyList())

        val manager = BackupManagerImpl(
            context = context,
            preferencesRepository = preferencesRepository,
            credentialCrypto = mock<CredentialCrypto>(),
            providerDao = providerDao,
            favoriteDao = mock<FavoriteDao>(),
            virtualGroupDao = mock<VirtualGroupDao>(),
            playbackHistoryDao = mock<PlaybackHistoryDao>(),
            movieDao = mock<MovieDao>(),
            episodeDao = mock<EpisodeDao>(),
            categoryRepository = mock<CategoryRepository>(),
            recordingScheduleDao = mock<RecordingScheduleDao>(),
            recordingManager = mock<RecordingManager>(),
            transactionRunner = object : DatabaseTransactionRunner {
                override suspend fun <T> inTransaction(block: suspend () -> T): T = block()
            },
            gson = gson,
            channelDao = mock()
        )

        val result = manager.importConfig(
            uriString = "content://backup-top-navigation",
            plan = BackupImportPlan(
                importPreferences = true,
                importProviders = false,
                importSavedLibrary = false,
                importPlaybackHistory = false,
                importMultiViewPresets = false,
                importRecordingSchedules = false,
                conflictStrategy = BackupConflictStrategy.KEEP_EXISTING
            )
        )

        assertThat(result).isInstanceOf(Result.Success::class.java)
        verify(preferencesRepository).setAppTopLevelDestinations(
            listOf(
                AppTopLevelDestination.HOME,
                AppTopLevelDestination.SEARCH,
                AppTopLevelDestination.SETTINGS
            )
        )
    }

    @Test
    fun `importConfig restores home dashboard shelves preference`() = runBlocking {
        val context: Context = mock()
        val contentResolver: ContentResolver = mock()
        val preferencesRepository: PreferencesRepository = mock()
        val providerDao: ProviderDao = mock()
        whenever(context.contentResolver).thenReturn(contentResolver)
        val gson = Gson()
        val backupData = BackupData(
            preferences = mapOf(
                "appHomeDashboardShelves" to "favorite_channels,recommended_movies,top_rated_movies"
            )
        )
        whenever(contentResolver.openInputStream(Uri.parse("content://backup-home-dashboard"))).thenReturn(
            ByteArrayInputStream(gson.toJson(backupData).toByteArray())
        )
        whenever(providerDao.getAllSync()).thenReturn(emptyList())

        val manager = BackupManagerImpl(
            context = context,
            preferencesRepository = preferencesRepository,
            credentialCrypto = mock<CredentialCrypto>(),
            providerDao = providerDao,
            favoriteDao = mock<FavoriteDao>(),
            virtualGroupDao = mock<VirtualGroupDao>(),
            playbackHistoryDao = mock<PlaybackHistoryDao>(),
            movieDao = mock<MovieDao>(),
            episodeDao = mock<EpisodeDao>(),
            categoryRepository = mock<CategoryRepository>(),
            recordingScheduleDao = mock<RecordingScheduleDao>(),
            recordingManager = mock<RecordingManager>(),
            transactionRunner = object : DatabaseTransactionRunner {
                override suspend fun <T> inTransaction(block: suspend () -> T): T = block()
            },
            gson = gson,
            channelDao = mock()
        )

        val result = manager.importConfig(
            uriString = "content://backup-home-dashboard",
            plan = BackupImportPlan(
                importPreferences = true,
                importProviders = false,
                importSavedLibrary = false,
                importPlaybackHistory = false,
                importMultiViewPresets = false,
                importRecordingSchedules = false,
                conflictStrategy = BackupConflictStrategy.KEEP_EXISTING
            )
        )

        assertThat(result).isInstanceOf(Result.Success::class.java)
        verify(preferencesRepository).setAppHomeDashboardShelves(
            listOf(
                AppHomeDashboardShelf.FAVORITE_CHANNELS,
                AppHomeDashboardShelf.RECOMMENDED_MOVIES,
                AppHomeDashboardShelf.TOP_RATED_MOVIES
            )
        )
    }

    @Test
    fun `toScheduledRecordingBackup stores requested window and padding separately`() {
        val provider = Provider(
            id = 7L,
            name = "Provider",
            type = ProviderType.M3U,
            serverUrl = "https://example.com",
            username = "user",
            stalkerMacAddress = ""
        )
        val item = RecordingItem(
            id = "scheduled-1",
            scheduleId = 21L,
            providerId = 7L,
            channelId = 100L,
            channelName = "BBC One",
            streamUrl = "https://example.com/live.ts",
            scheduledStartMs = 1_700_000_000_000L,
            scheduledEndMs = 1_700_000_540_000L,
            programTitle = "World News",
            recurrence = RecordingRecurrence.DAILY,
            status = RecordingStatus.SCHEDULED
        )
        val schedule = RecordingScheduleEntity(
            id = 21L,
            providerId = 7L,
            channelId = 100L,
            channelName = "BBC One",
            streamUrl = "https://example.com/live.ts",
            programTitle = "World News",
            requestedStartMs = 1_700_000_120_000L,
            requestedEndMs = 1_700_000_480_000L,
            recurrence = RecordingRecurrence.DAILY
        )

        val backup = item.toScheduledRecordingBackup(provider, schedule)

        assertThat(backup.scheduledStartMs).isEqualTo(item.scheduledStartMs)
        assertThat(backup.scheduledEndMs).isEqualTo(item.scheduledEndMs)
        assertThat(backup.requestedStartMs).isEqualTo(schedule.requestedStartMs)
        assertThat(backup.requestedEndMs).isEqualTo(schedule.requestedEndMs)
        assertThat(backup.paddingBeforeMs).isEqualTo(120_000L)
        assertThat(backup.paddingAfterMs).isEqualTo(60_000L)
        assertThat(backup.recurringRuleId).isEqualTo(schedule.recurringRuleId)
    }

    @Test
    fun `toRecordingRequest preserves legacy effective backup windows`() {
        val backup = ScheduledRecordingBackup(
            providerServerUrl = "https://example.com",
            providerUsername = "user",
            channelId = 100L,
            channelName = "BBC One",
            streamUrl = "https://example.com/live.ts",
            scheduledStartMs = 1_700_000_000_000L,
            scheduledEndMs = 1_700_000_540_000L,
            programTitle = "World News",
            recurrence = RecordingRecurrence.NONE
        )

        val request = backup.toRecordingRequest(providerId = 7L)

        assertThat(request.scheduledStartMs).isEqualTo(backup.scheduledStartMs)
        assertThat(request.scheduledEndMs).isEqualTo(backup.scheduledEndMs)
        assertThat(request.paddingBeforeMs).isEqualTo(0L)
        assertThat(request.paddingAfterMs).isEqualTo(0L)
    }

    @Test
    fun `toRecordingRequest restores requested window and explicit padding from new backups`() {
        val backup = ScheduledRecordingBackup(
            providerServerUrl = "https://example.com",
            providerUsername = "user",
            channelId = 100L,
            channelName = "BBC One",
            streamUrl = "https://example.com/live.ts",
            scheduledStartMs = 1_700_000_000_000L,
            scheduledEndMs = 1_700_000_540_000L,
            requestedStartMs = 1_700_000_120_000L,
            requestedEndMs = 1_700_000_480_000L,
            paddingBeforeMs = 120_000L,
            paddingAfterMs = 60_000L,
            programTitle = "World News",
            recurrence = RecordingRecurrence.WEEKLY,
            recurringRuleId = "rule-1"
        )

        val request = backup.toRecordingRequest(providerId = 7L)

        assertThat(request.scheduledStartMs).isEqualTo(backup.requestedStartMs)
        assertThat(request.scheduledEndMs).isEqualTo(backup.requestedEndMs)
        assertThat(request.paddingBeforeMs).isEqualTo(backup.paddingBeforeMs)
        assertThat(request.paddingAfterMs).isEqualTo(backup.paddingAfterMs)
        assertThat(request.recurrence).isEqualTo(RecordingRecurrence.WEEKLY)
        assertThat(request.recurringRuleId).isEqualTo("rule-1")
    }

    @Test
    fun `normalizedRecurringBackups collapses duplicate occurrences for the same recurring rule`() {
        val recurringFirst = ScheduledRecordingBackup(
            providerServerUrl = "https://example.com",
            providerUsername = "user",
            channelId = 100L,
            channelName = "BBC One",
            streamUrl = "https://example.com/live.ts",
            scheduledStartMs = 1_700_000_000_000L,
            scheduledEndMs = 1_700_000_540_000L,
            requestedStartMs = 1_700_000_120_000L,
            requestedEndMs = 1_700_000_480_000L,
            programTitle = "World News",
            recurrence = RecordingRecurrence.DAILY,
            recurringRuleId = "rule-1"
        )
        val recurringSecond = recurringFirst.copy(
            scheduledStartMs = 1_700_086_400_000L,
            scheduledEndMs = 1_700_086_940_000L,
            requestedStartMs = 1_700_086_520_000L,
            requestedEndMs = 1_700_086_880_000L
        )
        val oneShot = ScheduledRecordingBackup(
            providerServerUrl = "https://example.com",
            providerUsername = "user",
            channelId = 101L,
            channelName = "BBC Two",
            streamUrl = "https://example.com/other.ts",
            scheduledStartMs = 1_700_010_000_000L,
            scheduledEndMs = 1_700_010_540_000L,
            programTitle = "Documentary",
            recurrence = RecordingRecurrence.NONE
        )

        val normalized = listOf(recurringSecond, oneShot, recurringFirst).normalizedRecurringBackups()

        assertThat(normalized).hasSize(2)
        assertThat(normalized).contains(recurringFirst)
        assertThat(normalized).contains(oneShot)
    }

    @Test
    fun `normalizedRecurringBackups keeps recurring entries without stable identity`() {
        val first = ScheduledRecordingBackup(
            providerServerUrl = "https://example.com",
            providerUsername = "user",
            channelId = 100L,
            channelName = "BBC One",
            streamUrl = "https://example.com/live.ts",
            scheduledStartMs = 1_700_000_000_000L,
            scheduledEndMs = 1_700_000_540_000L,
            programTitle = "World News",
            recurrence = RecordingRecurrence.DAILY
        )
        val second = first.copy(
            scheduledStartMs = 1_700_086_400_000L,
            scheduledEndMs = 1_700_086_940_000L
        )

        val normalized = listOf(first, second).normalizedRecurringBackups()

        assertThat(normalized).containsExactly(first, second)
    }

    @Test
    fun `importScheduledRecordingBackups reports skipped and failed outcomes`() {
        val recordingManager: RecordingManager = mock()
        val provider = ProviderEntity(
            id = 7L,
            name = "Provider",
            type = ProviderType.M3U,
            serverUrl = "https://example.com",
            username = "user"
        )
        val existingSchedule = RecordingItem(
            id = "existing-1",
            providerId = 7L,
            channelId = 100L,
            channelName = "BBC One",
            streamUrl = "https://example.com/live.ts",
            scheduledStartMs = 1_700_000_000_000L,
            scheduledEndMs = 1_700_000_540_000L,
            status = RecordingStatus.SCHEDULED
        )
        val keepExisting = ScheduledRecordingBackup(
            providerServerUrl = provider.serverUrl,
            providerUsername = provider.username,
            channelId = 100L,
            channelName = "BBC One",
            streamUrl = "https://example.com/live.ts",
            scheduledStartMs = existingSchedule.scheduledStartMs,
            scheduledEndMs = existingSchedule.scheduledEndMs,
            programTitle = "World News"
        )
        val validationFailure = keepExisting.copy(
            channelId = 101L,
            channelName = "BBC Two",
            streamUrl = "https://example.com/other.ts",
            scheduledStartMs = 1_700_001_000_000L,
            scheduledEndMs = 1_700_001_540_000L
        )

        runBlocking {
            whenever(recordingManager.scheduleRecording(any()))
                .thenReturn(Result.error("Recording conflicts with an existing active recording for World News."))
        }

        val summary = kotlinx.coroutines.runBlocking {
            importScheduledRecordingBackups(
                recordings = listOf(keepExisting, validationFailure),
                storedProviders = listOf(provider),
                existingSchedules = mutableListOf(existingSchedule),
                conflictStrategy = BackupConflictStrategy.KEEP_EXISTING,
                recordingManager = recordingManager,
                nowMs = 1_699_999_000_000L
            )
        }

        assertThat(summary.importedCount).isEqualTo(0)
        assertThat(summary.skippedCount).isEqualTo(1)
        assertThat(summary.failedCount).isEqualTo(1)
        assertThat(summary.outcomes.map { it.disposition }).containsExactly(
            RecordingScheduleImportDisposition.SKIPPED_EXISTING,
            RecordingScheduleImportDisposition.FAILED
        )
        assertThat(summary.outcomes.last().reason).contains("conflicts")
        runBlocking {
            verify(recordingManager).scheduleRecording(any())
        }
    }

    @Test
    fun `importScheduledRecordingBackups reports replaced existing schedules`() {
        val recordingManager: RecordingManager = mock()
        val provider = ProviderEntity(
            id = 7L,
            name = "Provider",
            type = ProviderType.M3U,
            serverUrl = "https://example.com",
            username = "user"
        )
        val existingSchedule = RecordingItem(
            id = "existing-1",
            providerId = 7L,
            channelId = 100L,
            channelName = "BBC One",
            streamUrl = "https://example.com/live.ts",
            scheduledStartMs = 1_700_000_000_000L,
            scheduledEndMs = 1_700_000_540_000L,
            status = RecordingStatus.SCHEDULED
        )
        val imported = ScheduledRecordingBackup(
            providerServerUrl = provider.serverUrl,
            providerUsername = provider.username,
            channelId = 100L,
            channelName = "BBC One",
            streamUrl = "https://example.com/live.ts",
            scheduledStartMs = existingSchedule.scheduledStartMs,
            scheduledEndMs = existingSchedule.scheduledEndMs,
            programTitle = "World News"
        )
        val importedItem = existingSchedule.copy(id = "imported-1")

        runBlocking {
            whenever(recordingManager.cancelRecording(existingSchedule.id)).thenReturn(Result.success(Unit))
            whenever(recordingManager.scheduleRecording(any())).thenReturn(Result.success(importedItem))
        }

        val summary = kotlinx.coroutines.runBlocking {
            importScheduledRecordingBackups(
                recordings = listOf(imported),
                storedProviders = listOf(provider),
                existingSchedules = mutableListOf(existingSchedule),
                conflictStrategy = BackupConflictStrategy.REPLACE_EXISTING,
                recordingManager = recordingManager,
                nowMs = 1_699_999_000_000L
            )
        }

        assertThat(summary.importedCount).isEqualTo(1)
        assertThat(summary.failedCount).isEqualTo(0)
        assertThat(summary.outcomes.single().disposition).isEqualTo(RecordingScheduleImportDisposition.REPLACED_EXISTING)
        runBlocking {
            verify(recordingManager).cancelRecording(existingSchedule.id)
            verify(recordingManager).scheduleRecording(any())
        }
    }

    @Test
    fun `importScheduledRecordingBackups keeps old schedule when replacement cancellation fails`() = runBlocking {
        val recordingManager: RecordingManager = mock()
        val provider = ProviderEntity(
            id = 7L,
            name = "Provider",
            type = ProviderType.M3U,
            serverUrl = "https://example.com",
            username = "user"
        )
        val existingSchedule = RecordingItem(
            id = "existing-1",
            providerId = 7L,
            channelId = 100L,
            channelName = "BBC One",
            streamUrl = "https://example.com/live.ts",
            scheduledStartMs = 1_700_000_000_000L,
            scheduledEndMs = 1_700_000_540_000L,
            status = RecordingStatus.SCHEDULED
        )
        val imported = ScheduledRecordingBackup(
            providerServerUrl = provider.serverUrl,
            providerUsername = provider.username,
            channelId = 100L,
            channelName = "BBC One",
            streamUrl = "https://example.com/live.ts",
            scheduledStartMs = existingSchedule.scheduledStartMs,
            scheduledEndMs = existingSchedule.scheduledEndMs
        )
        val replacement = existingSchedule.copy(id = "replacement-1")
        whenever(recordingManager.scheduleRecording(any())).thenReturn(Result.success(replacement))
        whenever(recordingManager.cancelRecording(existingSchedule.id)).thenReturn(
            Result.error("old alarm cancellation failed")
        )
        whenever(recordingManager.cancelRecording(replacement.id)).thenReturn(Result.success(Unit))

        val schedules = mutableListOf(existingSchedule)
        val summary = importScheduledRecordingBackups(
            recordings = listOf(imported),
            storedProviders = listOf(provider),
            existingSchedules = schedules,
            conflictStrategy = BackupConflictStrategy.REPLACE_EXISTING,
            recordingManager = recordingManager,
            nowMs = 1_699_999_000_000L
        )

        assertThat(summary.importedCount).isEqualTo(0)
        assertThat(summary.failedCount).isEqualTo(1)
        assertThat(summary.outcomes.single().reason).contains("Could not replace existing schedule")
        assertThat(schedules).containsExactly(existingSchedule)
        verify(recordingManager).cancelRecording(replacement.id)
        Unit
    }

    private class RecordingTransactionRunner : DatabaseTransactionRunner {
        var calls: Int = 0
        private var depth: Int = 0

        val isInTransaction: Boolean
            get() = depth > 0

        override suspend fun <T> inTransaction(block: suspend () -> T): T {
            calls += 1
            depth += 1
            return try {
                block()
            } finally {
                depth -= 1
            }
        }
    }

    private suspend fun inspectAdmissionFailure(name: String, json: String): BackupAdmissionException {
        val context: Context = mock()
        val contentResolver: ContentResolver = mock()
        whenever(context.contentResolver).thenReturn(contentResolver)
        whenever(contentResolver.openInputStream(Uri.parse("content://$name"))).thenReturn(
            ByteArrayInputStream(json.toByteArray())
        )
        val result = backupManagerForValidation(context).inspectBackup("content://$name")
        val error = result as Result.Error
        assertThat(error.exception).isInstanceOf(BackupAdmissionException::class.java)
        return error.exception as BackupAdmissionException
    }

    private class PrefixThenPaddingInputStream(
        private val prefix: ByteArray,
        private val totalBytes: Int
    ) : InputStream() {
        private var position = 0

        override fun read(): Int {
            if (position >= totalBytes) return -1
            val value = if (position < prefix.size) prefix[position].toInt() and 0xff else ' '.code
            position += 1
            return value
        }
    }

    private class CancellingInputStream(
        private val prefix: ByteArray
    ) : InputStream() {
        private var position = 0

        override fun read(): Int {
            if (position >= prefix.size) throw CancellationException("cancelled during read")
            return prefix[position++].toInt() and 0xff
        }
    }

    private fun ProviderEntity.backupIdentityForTest(): Triple<String, String, String> =
        Triple(serverUrl, username, stalkerMacAddress)

    private fun backupManagerForValidation(
        context: Context,
        preferencesRepository: PreferencesRepository = mock(),
        providerDao: ProviderDao = mock(),
        checkpointDao: BackupRestoreCheckpointDao? = null,
        channelDao: ChannelDao = mock(),
        categoryRepository: CategoryRepository = mock(),
        virtualGroupDao: VirtualGroupDao = mock(),
        credentialCrypto: CredentialCrypto = mock(),
        epgSourceDao: EpgSourceDao? = null,
    ): BackupManagerImpl = BackupManagerImpl(
        context = context,
        preferencesRepository = preferencesRepository,
        credentialCrypto = credentialCrypto,
        providerDao = providerDao,
        favoriteDao = mock<FavoriteDao>(),
        virtualGroupDao = virtualGroupDao,
        playbackHistoryDao = mock<PlaybackHistoryDao>(),
        movieDao = mock<MovieDao>(),
        episodeDao = mock<EpisodeDao>(),
        categoryRepository = categoryRepository,
        recordingScheduleDao = mock<RecordingScheduleDao>(),
        recordingManager = mock<RecordingManager>(),
        transactionRunner = object : DatabaseTransactionRunner {
            override suspend fun <T> inTransaction(block: suspend () -> T): T = block()
        },
        gson = Gson(),
        backupRestoreCheckpointDao = checkpointDao,
        channelDao = channelDao,
        epgSourceDao = epgSourceDao
    )

    private fun preferencesOnlyPlan() = BackupImportPlan(
        importPreferences = true,
        importProviders = false,
        importSavedLibrary = false,
        importPlaybackHistory = false,
        importMultiViewPresets = false,
        importRecordingSchedules = false
    )
}
