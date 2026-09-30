package com.moneo.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.moneo.app.data.local.dao.TransactionDao
import com.moneo.app.data.local.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class],
    version = 2,
    exportSchema = true
)
abstract class MoneoDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        const val DATABASE_NAME = "moneo_database"
    }
}
