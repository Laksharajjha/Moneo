package com.moneo.app.ai.parser

import com.moneo.app.ai.engine.LocalAiEngine
import com.moneo.app.ai.model.ParseResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartTransactionParser @Inject constructor(
    private val deterministicParser: DeterministicParser,
    private val aiEngine: LocalAiEngine
) : TransactionParser {

    override suspend fun parse(text: String): ParseResult {
        // 1. Try deterministic parser first
        val deterministicResult = deterministicParser.parse(text)
        
        // If deterministic parser is highly confident and found transactions, use it
        if (deterministicResult.isSuccess && !deterministicResult.requiresConfirmation) {
            return deterministicResult
        }

        // 2. If deterministic parser fails or is unsure, try AI
        if (aiEngine.isAvailable()) {
            return try {
                aiEngine.extractFinancialEvent(text)
            } catch (e: Exception) {
                // 3. Fallback to deterministic on failure
                deterministicResult
            }
        }

        // 3. Fallback to deterministic if AI is unavailable
        return deterministicResult
    }
}
