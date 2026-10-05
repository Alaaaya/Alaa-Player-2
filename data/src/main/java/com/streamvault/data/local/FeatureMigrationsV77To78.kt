package com.streamvault.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** v78: separate watch_later list (does not touch favorites or any existing data). */
object FeatureMigrationsV77To78 {
    val MIGRATION_77_78 = object : Migration(77, 78) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS watch_later (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    provider_id INTEGER NOT NULL,
                    content_id INTEGER NOT NULL,
                    content_type TEXT NOT NULL,
                    title TEXT NOT NULL,
                    poster_url TEXT,
                    added_at INTEGER NOT NULL,
                    FOREIGN KEY(provider_id) REFERENCES providers(id) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_watch_later_provider_id_content_id_content_type ON watch_later(provider_id, content_id, content_type)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_watch_later_added_at ON watch_later(added_at)")
        }
    }
}
