package com.streamvault.data.panel

import androidx.room.Room
import com.streamvault.data.local.StreamVaultDatabase
import com.streamvault.data.local.entity.ProviderEntity
import com.streamvault.domain.model.PanelCategoryPolicy
import com.streamvault.domain.model.PanelCategoryPolicyRegistry as Policies
import com.streamvault.domain.model.ProviderType
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/** Real Room/SQLite tests. Reconciler has no provider/network dependency, so restart/unhide cannot fetch. */
@RunWith(RobolectricTestRunner::class)
class PanelCategoryPersistenceTest {
    private lateinit var database: StreamVaultDatabase
    private val id=9834751L
    private fun policy(hidden:Set<String> = setOf("007"), generation:String="generation") = PanelCategoryPolicy(
        "device", "playlist", generation, "revision-${hidden.joinToString()}",
        mapOf("live" to hidden,"vod" to emptySet(),"series" to emptySet()))
    @Before fun setup() = runTest {
        database=Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(),StreamVaultDatabase::class.java)
            .allowMainThreadQueries().build()
        database.providerDao().insert(ProviderEntity(id=id,name="test",type=ProviderType.XTREAM_CODES))
    }
    @After fun cleanup() { Policies.remove(id);database.close() }
    private suspend fun addChannel(content:Long,raw:String) {
        val key=Policies.categoryKey(id,"live",raw)!!
        assertTrue(Policies.recordMembership(Policies.snapshot(id),"live",content,setOf(raw)))
        database.channelDao().insert(com.streamvault.data.local.entity.ChannelEntity(streamId=content,name="visible",providerId=id,categoryId=key))
    }
    private fun count()=database.openHelper.writableDatabase.query("SELECT count(*) FROM channels WHERE provider_id=$id").use{it.moveToFirst();it.getInt(0)}
    @Test fun unchangedColdRestoreAndUnhidePreserveCache() = runTest {
        val reconciler=PanelCategoryReconciler(database)
        reconciler.reconcile(id,policy());addChannel(10,"7")
        assertEquals(1,count())
        Policies.block(id) // Discard process-local policy/membership, like startup identity restoration.
        PanelCategoryReconciler(database).reconcile(id,policy())
        assertEquals(1,count());assertTrue(Policies.allowsContent(id,"live",10))
        reconciler.reconcile(id,policy(emptySet()))
        assertEquals(1,count());assertTrue(Policies.allowsContent(id,"live",10))
    }
    @Test fun exactRawKeysPersistAcrossResetWithoutNumericConflation() = runTest {
        val reconciler=PanelCategoryReconciler(database)
        reconciler.reconcile(id,policy(emptySet()))
        val canonical=Policies.categoryKey(id,"live","7")!!
        val opaque=Policies.categoryKey(id,"live","007")!!
        val overflow=Policies.categoryKey(id,"live","999999999999999999999999999999999999")!!
        assertNotEquals(canonical,opaque);assertNotEquals(opaque,overflow)
        addChannel(10,"7");addChannel(11,"007")
        Policies.block(id);reconciler.reconcile(id,policy())
        assertEquals("7",Policies.rawCategory(id,"live",canonical))
        assertEquals("007",Policies.rawCategory(id,"live",opaque))
        assertEquals(1,count());assertTrue(Policies.allowsContent(id,"live",10));assertFalse(Policies.allowsContent(id,"live",11))
    }
    @Test fun hiddenAlternateEvictsAndBothOrdersStayDenied() = runTest {
        PanelCategoryReconciler(database).reconcile(id,policy())
        addChannel(10,"7")
        val token=Policies.snapshot(id)
        assertFalse(Policies.recordMembership(token,"live",10,setOf("007")))
        assertEquals(0,count())
        assertFalse(Policies.recordMembership(token,"live",10,setOf("7")))
        assertFalse(Policies.recordMembership(token,"live",11,setOf("007")))
        assertFalse(Policies.recordMembership(token,"live",11,setOf("7")))
        assertFalse(Policies.allowsContent(id,"live",10));assertFalse(Policies.allowsContent(id,"live",11))
    }
    @Test fun staleTokenCannotCommitAndProviderReplacementMigratesOnce() = runTest {
        val reconciler=PanelCategoryReconciler(database)
        reconciler.reconcile(id,policy());addChannel(10,"7")
        val token=Policies.snapshot(id)
        reconciler.reconcile(id,policy(generation="new-provider"))
        assertFalse(Policies.recordMembership(token,"live",10,setOf("7")))
        assertEquals(0,count())
        addChannel(12,"7");Policies.block(id)
        reconciler.reconcile(id,policy(generation="new-provider"))
        assertEquals(1,count())
    }
}
