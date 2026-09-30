package com.moneo.app.di

import android.content.Context
import androidx.room.Room
import com.moneo.app.data.local.dao.TransactionDao
import com.moneo.app.data.local.database.MoneoDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMoneoDatabase(@ApplicationContext context: Context): MoneoDatabase =
        Room.databaseBuilder(
            context,
            MoneoDatabase::class.java,
            MoneoDatabase.DATABASE_NAME
        ).addMigrations(com.moneo.app.data.local.database.Migrations.MIGRATION_2_3)
         .fallbackToDestructiveMigration()
         .build()

    @Provides
    fun provideTransactionDao(database: MoneoDatabase): TransactionDao =
        database.transactionDao()
        
    @Provides
    fun provideInboxEventDao(database: MoneoDatabase): com.moneo.app.data.local.dao.InboxEventDao =
        database.inboxEventDao()
}
