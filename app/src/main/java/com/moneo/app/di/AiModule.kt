package com.moneo.app.di

import com.moneo.app.ai.engine.LocalAiEngine
import com.moneo.app.ai.engine.StubLocalAiEngine
import com.moneo.app.ai.parser.SmartTransactionParser
import com.moneo.app.ai.parser.TransactionParser
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {

    @Binds
    abstract fun bindLocalAiEngine(
        engine: StubLocalAiEngine
    ): LocalAiEngine

    @Binds
    abstract fun bindTransactionParser(
        parser: SmartTransactionParser
    ): TransactionParser
}
