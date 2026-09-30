package com.moneo.app.ai.engine

import com.moneo.app.ai.model.ParseResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StubLocalAiEngine @Inject constructor() : LocalAiEngine {
    
    override suspend fun isAvailable(): Boolean = false // Not available until we add ML Kit / Gemini Nano

    override suspend fun extractFinancialEvent(input: String): ParseResult {
        throw UnsupportedOperationException("AI not configured yet")
    }

    override suspend fun interpretQuery(query: String): QueryIntent {
        throw UnsupportedOperationException("AI not configured yet")
    }
}
