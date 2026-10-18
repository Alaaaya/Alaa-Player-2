package com.streamvault.data.local

import androidx.room.Room
import com.google.gson.Gson
import com.streamvault.data.provider.ProviderConfigurationCodec
import com.streamvault.data.security.CredentialCrypto
import com.streamvault.domain.model.XtreamConfig
import com.google.common.truth.Truth.assertThat
import com.streamvault.data.local.entity.ProviderConfigEntity
import com.streamvault.data.local.entity.ProviderEntity
import com.streamvault.data.local.entity.StalkerPortalStateEntity
import com.streamvault.domain.model.ProviderType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ProviderSnapshotDaoTest {
    private lateinit var database: StreamVaultDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            StreamVaultDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun `configuration generation is monotonic and stale Stalker learning is rejected`() = runTest {
        database.providerDao().insert(
            ProviderEntity(
                id = 7,
                name = "Portal",
                type = ProviderType.STALKER_PORTAL
            )
        )
        val dao = database.providerSnapshotDao()
        assertThat(dao.commitConfiguration(config(generation = 1))).isTrue()
        assertThat(dao.commitConfiguration(config(generation = 1))).isFalse()
        assertThat(dao.commitConfiguration(config(generation = 2))).isTrue()

        assertThat(dao.compareAndSetStalkerLearning(
            StalkerPortalStateEntity(
                providerId = 7,
                workingEndpoint = "https://old.test/load.php",
                configurationGeneration = 1,
                observedAt = 10
            )
        )).isFalse()
        assertThat(dao.compareAndSetStalkerLearning(
            StalkerPortalStateEntity(
                providerId = 7,
                workingEndpoint = "https://new.test/load.php",
                configurationGeneration = 2,
                observedAt = 20
            )
        )).isTrue()
        assertThat(database.stalkerPortalStateDao().get(7)?.workingEndpoint)
            .isEqualTo("https://new.test/load.php")
    }

    @Test
    fun `concurrent learning from old generation cannot win after edit`() = runTest {
        database.providerDao().insert(
            ProviderEntity(
                id = 7,
                name = "Portal",
                type = ProviderType.STALKER_PORTAL
            )
        )
        val dao = database.providerSnapshotDao()
        dao.commitConfiguration(config(generation = 1))
        dao.commitConfiguration(config(generation = 2))

        val outcomes = withContext(Dispatchers.Default) {
            listOf(
                async {
                    dao.compareAndSetStalkerLearning(
                        StalkerPortalStateEntity(providerId = 7, workingEndpoint = "old", configurationGeneration = 1)
                    )
                },
                async {
                    dao.compareAndSetStalkerLearning(
                        StalkerPortalStateEntity(providerId = 7, workingEndpoint = "new", configurationGeneration = 2)
                    )
                }
            ).awaitAll()
        }
        assertThat(outcomes).containsExactly(false, true)
        assertThat(database.stalkerPortalStateDao().get(7)?.workingEndpoint).isEqualTo("new")
    }

    @Test
    fun `new provider cannot replace another provider that owns the canonical identity`() = runTest {
        insertProvider(7, "First")
        insertProvider(8, "Second")
        val dao = database.providerSnapshotDao()
        assertThat(dao.commitConfiguration(config(providerId = 7, generation = 1, identityKey = "canonical")))
            .isTrue()

        assertThat(dao.commitConfiguration(config(providerId = 8, generation = 1, identityKey = "canonical")))
            .isFalse()

        assertThat(dao.getConfig(7)?.identityKey).isEqualTo("canonical")
        assertThat(dao.getConfig(8)).isNull()
    }

    @Test
    fun `migrated duplicate keeps its disambiguated identity when edited`() = runTest {
        insertProvider(7, "First")
        insertProvider(8, "Migrated duplicate")
        val dao = database.providerSnapshotDao()
        dao.commitConfiguration(config(providerId = 7, generation = 1, identityKey = "canonical"))
        val migratedIdentity = disambiguatedMigrationIdentityKey("canonical", providerId = 8)
        dao.commitConfiguration(config(providerId = 8, generation = 1, identityKey = migratedIdentity))

        assertThat(dao.commitConfiguration(config(providerId = 8, generation = 2, identityKey = "canonical")))
            .isTrue()

        assertThat(dao.getConfig(7)?.identityKey).isEqualTo("canonical")
        assertThat(dao.getConfig(8)?.identityKey).isEqualTo(migratedIdentity)
        assertThat(dao.getConfig(8)?.configurationGeneration).isEqualTo(2)
    }

    @Test
    fun `ordinary existing provider cannot change to another providers identity`() = runTest {
        insertProvider(7, "First")
        insertProvider(8, "Second")
        val dao = database.providerSnapshotDao()
        dao.commitConfiguration(config(providerId = 7, generation = 1, identityKey = "first"))
        dao.commitConfiguration(config(providerId = 8, generation = 1, identityKey = "second"))

        assertThat(dao.commitConfiguration(config(providerId = 8, generation = 2, identityKey = "first")))
            .isFalse()
        assertThat(dao.getConfig(8)?.identityKey).isEqualTo("second")
        assertThat(dao.getConfig(8)?.configurationGeneration).isEqualTo(1)
    }

    @Test
    fun `credential encryption failure rolls back provider identity in real Room transaction`() = runTest {
        val failure = SecurityException("credential key unavailable")
        var cryptoInvoked = false
        val crypto = object : CredentialCrypto {
            override fun encryptIfNeeded(value: String): String {
                cryptoInvoked = true
                throw failure
            }
            override fun decryptIfNeeded(value: String): String = value
        }
        val codec = ProviderConfigurationCodec(Gson(), crypto)
        val caught = runCatching {
            RoomDatabaseTransactionRunner(database).inTransaction {
                val providerId = database.providerDao().insert(
                    ProviderEntity(id = 19, name = "Xtream", type = ProviderType.XTREAM_CODES)
                )
                // Match repository ordering: identity insert, then encrypted snapshot.
                val payload = codec.encode(XtreamConfig("https://example.com", "user", "secret"))
                database.providerSnapshotDao().commitConfiguration(
                    config(providerId = providerId, generation = 1).copy(
                        type = ProviderType.XTREAM_CODES,
                        encryptedConfigJson = payload
                    )
                )
            }
        }.exceptionOrNull()

        // Coroutine stack-trace recovery may copy exceptions across the Room dispatcher.
        // The original crypto exception must remain the root cause, not be replaced.
        assertThat(caught).isInstanceOf(SecurityException::class.java)
        assertThat(caught?.message).isEqualTo(failure.message)
        val original = generateSequence(caught) { it.cause }.last()
        assertThat(original).isSameInstanceAs(failure)
        assertThat(cryptoInvoked).isTrue()
        assertThat(database.providerDao().getById(19)).isNull()
        assertThat(database.providerSnapshotDao().getConfig(19)).isNull()
    }

    private suspend fun insertProvider(id: Long, name: String) {
        database.providerDao().insert(
            ProviderEntity(id = id, name = name, type = ProviderType.STALKER_PORTAL)
        )
    }

    private fun config(
        generation: Long,
        providerId: Long = 7,
        identityKey: String = "identity"
    ) = ProviderConfigEntity(
        providerId = providerId,
        type = ProviderType.STALKER_PORTAL,
        schemaVersion = 1,
        configurationGeneration = generation,
        identityKey = identityKey,
        encryptedConfigJson = "{}",
        updatedAt = generation
    )
}
