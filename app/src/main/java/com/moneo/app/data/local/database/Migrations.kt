package com.moneo.app.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `inbox_events` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `amountInPaise` INTEGER NOT NULL,
                    `currency` TEXT NOT NULL,
                    `type` TEXT NOT NULL,
                    `category` TEXT NOT NULL,
                    `merchant` TEXT,
                    `date` TEXT NOT NULL,
                    `sourcePackage` TEXT NOT NULL,
                    `confidence` REAL NOT NULL,
                    `rawText` TEXT NOT NULL,
                    `timestamp` INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }
    }
}
