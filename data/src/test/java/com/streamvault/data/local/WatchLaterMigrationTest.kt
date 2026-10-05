package com.streamvault.data.local

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.streamvault.data.watchlater.WatchLaterRepository
import com.streamvault.domain.model.ContentType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class WatchLaterMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        StreamVaultDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun `77 to 78 adds watch_later and keeps existing favorites`() {
        val name = "wl-migration"
        helper.createDatabase(name, 77).apply {
            execSQL("PRAGMA foreign_keys=OFF")
            execSQL("INSERT INTO favorites (provider_id, content_id, content_type, position, group_id, group_key, added_at) VALUES (1, 42, 'MOVIE', 0, NULL, 0, 1000)")
            close()
        }
        val db = helper.runMigrationsAndValidate(name, 78, true, FeatureMigrationsV77To78.MIGRATION_77_78)
        db.query("SELECT content_id, content_type FROM favorites").use { c ->
            assertThat(c.count).isEqualTo(1)
            c.moveToFirst()
            assertThat(c.getLong(0)).isEqualTo(42L)
            assertThat(c.getString(1)).isEqualTo("MOVIE")
        }
        db.query("SELECT COUNT(*) FROM watch_later").use { c ->
            c.moveToFirst(); assertThat(c.getInt(0)).isEqualTo(0)
        }
        db.close()
    }

    @Test
    fun `repository toggles watch later independently of favorites`() = runTest {
        val database = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), StreamVaultDatabase::class.java)
            .allowMainThreadQueries().build()
        try {
            database.openHelper.writableDatabase.execSQL("PRAGMA foreign_keys=OFF")
            val repo = WatchLaterRepository(database.watchLaterDao())
            assertThat(repo.toggle(1, 7, ContentType.MOVIE, "Film", null)).isTrue()
            assertThat(repo.observeContains(1, 7, ContentType.MOVIE).first()).isTrue()
            assertThat(repo.observeAll().first().map { it.title }).containsExactly("Film")
            assertThat(repo.toggle(1, 7, ContentType.MOVIE, "Film", null)).isFalse()
            assertThat(repo.observeAll().first()).isEmpty()
        } finally {
            database.close()
        }
    }
}
