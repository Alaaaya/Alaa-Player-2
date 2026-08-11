package com.streamvault.data.local

import androidx.room.Room
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
                type = ProviderType.STALKER_PORTAL,
                serverUrl = "https://portal.test",
                stalkerMacAddress = "00:11:22:33:44:55"
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
                type = ProviderType.STALKER_PORTAL,
                serverUrl = "https://portal.test",
                stalkerMacAddress = "00:11:22:33:44:55"
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

    private fun config(generation: Long) = ProviderConfigEntity(
        providerId = 7,
        type = ProviderType.STALKER_PORTAL,
        schemaVersion = 1,
        configurationGeneration = generation,
        identityKey = "identity",
        encryptedConfigJson = "{}",
        updatedAt = generation
    )
}
