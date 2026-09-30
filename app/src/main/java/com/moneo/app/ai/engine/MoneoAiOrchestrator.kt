package com.moneo.app.ai.engine

import com.moneo.app.ai.model.ParseResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoneoAiOrchestrator @Inject constructor(
    private val nanoEngine: GeminiNanoEngine,
    private val mediaPipeEngine: MediaPipeEngine
) : LocalAiEngine {

    override suspend fun isAvailable(): Boolean {
        return nanoEngine.isAvailable() || mediaPipeEngine.isAvailable()
    }

    override suspend fun extractFinancialEvent(input: String): ParseResult {
        if (nanoEngine.isAvailable()) {
            val nanoResult = nanoEngine.extractFinancialEvent(input)
            // If it succeeds (returns transactions), use it
            if (nanoResult.transactions.isNotEmpty()) {
                return nanoResult
            }
        }
        
        if (mediaPipeEngine.isAvailable()) {
            return mediaPipeEngine.extractFinancialEvent(input)
        }
        
        return ParseResult(emptyList(), input, 0f, "All Local AI engines unavailable or failed")
    }

    override suspend fun interpretQuery(query: String): QueryIntent {
        if (nanoEngine.isAvailable()) {
            val intent = nanoEngine.interpretQuery(query)
            if (intent.intent != "UNKNOWN") {
                return intent
            }
        }

        if (mediaPipeEngine.isAvailable()) {
            return mediaPipeEngine.interpretQuery(query)
        }

        return QueryIntent("UNKNOWN", null, null)
    }
}
