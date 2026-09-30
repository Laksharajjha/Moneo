package com.moneo.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.moneo.app.data.local.dao.TransactionDao
import com.moneo.app.data.local.entity.TransactionEntity

import com.moneo.app.data.local.dao.InboxEventDao
import com.moneo.app.data.local.entity.InboxEventEntity

@Database(
    entities = [TransactionEntity::class, InboxEventEntity::class],
    version = 3,
    exportSchema = true
)
abstract class MoneoDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun inboxEventDao(): InboxEventDao

    companion object {
        const val DATABASE_NAME = "moneo_database"
    }
}
