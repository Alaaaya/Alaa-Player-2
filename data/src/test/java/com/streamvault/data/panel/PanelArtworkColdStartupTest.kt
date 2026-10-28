package com.streamvault.data.panel

import androidx.room.Room
import com.streamvault.data.local.StreamVaultDatabase
import com.streamvault.domain.model.PanelArtworkPolicyRegistry as Artwork
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.UUID

/** File-backed Room survives close/reopen; startup must restore even without a single provider. */
@RunWith(RobolectricTestRunner::class)
class PanelArtworkColdStartupTest {
    private fun resetProcessState() {
        // Domain's internal test seam is name-mangled across Gradle modules.
        Artwork.javaClass.declaredMethods.single { it.name.startsWith("resetForTests") }
            .apply { isAccessible=true }.invoke(Artwork)
        PanelCategoryReconciler::class.java.declaredFields
            .firstOrNull { it.name=="tombstonesRestored" }
            ?.apply { isAccessible=true }?.setBoolean(null,false)
    }

    @Test fun publicStartupHookRestoresDeletedProviderTombstonesWithZeroProviders() = runTest {
        val context=RuntimeEnvironment.getApplication()
        val name="artwork-cold-${UUID.randomUUID()}.db"
        fun open()=Room.databaseBuilder(context,StreamVaultDatabase::class.java,name)
            .allowMainThreadQueries().build()
        var database:StreamVaultDatabase?=null
        val denied="https://artwork.invalid/removed-provider.jpg"
        try {
            resetProcessState()
            database=open()
            PanelCategoryReconciler(database).restoreArtworkTombstones()
            database.openHelper.writableDatabase.execSQL(
                "INSERT INTO panel_artwork_owner(provider_id,url,owner_key) VALUES(?,?,?)",
                arrayOf<Any>(7654321L,denied,"episode:42"))
            database.close()
            database=null
            resetProcessState()
            database=open()
            database.openHelper.writableDatabase.query("SELECT count(*) FROM providers").use {
                assertTrue(it.moveToFirst()); assertEquals(0,it.getInt(0))
            }
            // Do not reconcile or remove anything here. This is the unconfigured startup branch.
            PanelCategoryReconciler(database).restoreArtworkTombstones()
            Artwork.completeStartup()
            assertTrue(runCatching { Artwork.acquire(denied) {} }.isFailure)
            Artwork.acquire("https://artwork.invalid/manual-unowned.jpg") {}.use {
                assertTrue(it.allowed())
            }
            database.openHelper.writableDatabase.query("SELECT count(*) FROM panel_artwork_owner").use {
                assertTrue(it.moveToFirst()); assertEquals(1,it.getInt(0))
            }
        } finally {
            database?.close()
            context.deleteDatabase(name)
            resetProcessState()
        }
    }
}
