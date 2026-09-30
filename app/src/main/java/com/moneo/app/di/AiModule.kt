package com.moneo.app.di

import com.moneo.app.ai.engine.LocalAiEngine
import com.moneo.app.ai.engine.GeminiNanoEngine
import com.moneo.app.ai.parser.SmartTransactionParser
import com.moneo.app.ai.parser.TransactionParser
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

import com.moneo.app.ai.engine.MoneoAiOrchestrator

@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {

    @Binds
    abstract fun bindLocalAiEngine(
        engine: MoneoAiOrchestrator
    ): LocalAiEngine

    @Binds
    abstract fun bindTransactionParser(
        parser: SmartTransactionParser
    ): TransactionParser

    companion object {
        @dagger.Provides
        @javax.inject.Singleton
        fun provideGson(): com.google.gson.Gson {
            return com.google.gson.Gson()
        }
    }
}
