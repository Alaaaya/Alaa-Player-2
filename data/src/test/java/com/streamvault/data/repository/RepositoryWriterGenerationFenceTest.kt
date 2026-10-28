package com.streamvault.data.repository

import com.streamvault.data.local.DatabaseTransactionRunner
import com.streamvault.data.panel.PanelCategoryExecutionContext
import com.streamvault.data.sync.ProviderWorkflowCommitFence
import com.streamvault.domain.model.PanelArtworkPolicyRegistry
import com.streamvault.domain.model.PanelCategoryPolicy
import com.streamvault.domain.model.PanelCategoryPolicyRegistry
import java.lang.reflect.InvocationTargetException
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import sun.misc.Unsafe

/** Exercises the actual private repository entry/transaction helpers, not a duplicate fence. */
class RepositoryWriterGenerationFenceTest {
    private val repositoryClasses = listOf(MovieRepositoryImpl::class.java, SeriesRepositoryImpl::class.java)

    private fun policy(revision: String) = PanelCategoryPolicy(
        "writer-test-device", "playlist", "provider-generation", revision,
        mapOf("live" to emptySet(), "vod" to emptySet(), "series" to emptySet())
    )

    private class Transactions(val beforeTransaction: () -> Unit = {}) : DatabaseTransactionRunner {
        var inside = false
        override suspend fun <T> inTransaction(block: suspend () -> T): T {
            beforeTransaction()
            inside = true
            return try { block() } finally { inside = false }
        }
    }

    // Avoid constructing unrelated network clients: these helpers only use the two fields below.
    private fun repository(type: Class<*>, transactions: Transactions): Any {
        val unsafeField = Unsafe::class.java.getDeclaredField("theUnsafe").apply { isAccessible = true }
        val instance = (unsafeField.get(null) as Unsafe).allocateInstance(type)
        type.getDeclaredField("transactionRunner").apply { isAccessible = true }.set(instance, transactions)
        type.getDeclaredField("workflowCommitFence").apply { isAccessible = true }
            .set(instance, ProviderWorkflowCommitFence())
        return instance
    }

    private suspend fun invoke(instance: Any, method: String, id: Long, block: suspend () -> Any?): Any? =
        suspendCoroutineUninterceptedOrReturn { continuation ->
            try {
                instance.javaClass.declaredMethods.single { it.name == method }
                    .apply { isAccessible = true }.invoke(instance, id, block, continuation)
            } catch (error: InvocationTargetException) {
                throw error.targetException
            }
        }

    @Test fun staleHideUnhideIsRejectedInsideTransactionInBothRepositories() = runBlocking {
        repositoryClasses.forEachIndexed { index, type ->
            val id = 8_410L + index
            PanelCategoryPolicyRegistry.install(id, policy("initial"))
            val captured = PanelCategoryExecutionContext(PanelCategoryPolicyRegistry.snapshot(id))
            val tx = Transactions {
                PanelCategoryPolicyRegistry.block(id)
                PanelCategoryPolicyRegistry.install(id, policy("visible-again"))
            }
            var writes = 0
            val error = runCatching {
                withContext(captured) {
                    invoke(repository(type, tx), "catalogTransaction", id) { writes++; Unit }
                }
            }.exceptionOrNull()
            assertTrue(error is IllegalStateException)
            assertEquals(0, writes)
            PanelCategoryPolicyRegistry.remove(id)
        }
    }

    @Test fun artworkReplacementAfterFetchRejectsPublicationInBothRepositories() = runBlocking {
        repositoryClasses.forEachIndexed { index, type ->
            val id = 8_420L + index
            PanelCategoryPolicyRegistry.install(id, policy("stable"))
            val captured = PanelCategoryExecutionContext(PanelCategoryPolicyRegistry.snapshot(id))
            val tx = Transactions { PanelArtworkPolicyRegistry.beginTransition(id) }
            var writes = 0
            val error = runCatching {
                withContext(captured) {
                    invoke(repository(type, tx), "catalogTransaction", id) { writes++; Unit }
                }
            }.exceptionOrNull()
            assertTrue(error is IllegalStateException)
            assertEquals(0, writes)
            PanelCategoryPolicyRegistry.remove(id)
        }
    }

    @Test fun inheritedStaleTokenIsNotRecapturedByRepositoryEntry() = runBlocking {
        repositoryClasses.forEachIndexed { index, type ->
            val id = 8_430L + index
            PanelCategoryPolicyRegistry.install(id, policy("initial"))
            val captured = PanelCategoryExecutionContext(PanelCategoryPolicyRegistry.snapshot(id))
            PanelCategoryPolicyRegistry.install(id, policy("new"))
            var fetched = false
            val error = runCatching {
                withContext(captured) {
                    invoke(repository(type, Transactions()), "withCatalogPolicy", id) { fetched = true; Unit }
                }
            }.exceptionOrNull()
            assertTrue(error is IllegalStateException)
            assertTrue(!fetched)
            PanelCategoryPolicyRegistry.remove(id)
        }
    }

    @Test fun currentPublicationRunsInsideTransactionAndCancellationEscapes() = runBlocking {
        repositoryClasses.forEachIndexed { index, type ->
            val id = 8_440L + index
            PanelCategoryPolicyRegistry.install(id, policy("current"))
            val tx = Transactions()
            val repo = repository(type, tx)
            withContext(PanelCategoryExecutionContext(PanelCategoryPolicyRegistry.snapshot(id))) {
                invoke(repo, "catalogTransaction", id) { assertTrue(tx.inside); Unit }
                val cancelled = runCatching {
                    invoke(repo, "withCatalogPolicy", id) { throw CancellationException("stop hydration") }
                }.exceptionOrNull()
                assertTrue(cancelled is CancellationException)
            }
            PanelCategoryPolicyRegistry.remove(id)
        }
    }
    @Test fun episodeAuthorizationUsesTypedNamespaceAndAllDurableParents() {
        val id = 8_450L
        PanelCategoryPolicyRegistry.install(id, policy("episode"))
        try {
            val token = PanelCategoryPolicyRegistry.snapshot(id)
            PanelCategoryPolicyRegistry.recordMembership(token, "series", 7L, setOf("visible"))
            PanelCategoryPolicyRegistry.recordMembership(token, "series", 8L, setOf("visible"))
            PanelCategoryPolicyRegistry.recordEpisodeParent(token, 7L, 7001L)
            val stream = com.streamvault.domain.model.StreamInfo(
                url = "https://example.invalid/episode",
                title = "Episode",
                providerId = id,
                streamId = 7001L,
                contentType = com.streamvault.domain.model.ContentType.SERIES_EPISODE,
                panelCategoryPolicyVersion = PanelCategoryPolicyRegistry.version(id),
                panelAuthorizationVersion = com.streamvault.domain.model.PanelPlaybackAuthorization.version(id)
            )
            assertEquals("episode", PanelCategoryPolicyRegistry.media(stream.contentType!!))
            assertTrue(com.streamvault.domain.model.PanelPlaybackAuthorization.allows(stream))
            // Parent discovery can deny an already resolved episode without a generation change.
            PanelCategoryPolicyRegistry.recordMembership(token, "series", 8L, setOf("hidden"))
            PanelCategoryPolicyRegistry.recordEpisodeParent(token, 8L, 7001L)
            PanelCategoryPolicyRegistry.install(id, com.streamvault.domain.model.PanelCategoryPolicy(
                "writer-test-device", "playlist", "provider-generation", "hidden",
                mapOf("series" to setOf("hidden"))
            ))
            assertTrue(!com.streamvault.domain.model.PanelPlaybackAuthorization.allows(
                stream.copy(panelCategoryPolicyVersion = PanelCategoryPolicyRegistry.version(id))
            ))
        } finally {
            PanelCategoryPolicyRegistry.remove(id)
        }
    }

}
