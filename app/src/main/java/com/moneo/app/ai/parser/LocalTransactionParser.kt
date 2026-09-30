package com.moneo.app.ai.parser

import com.moneo.app.ai.model.ParseResult

/**
 * Abstraction for the NLP/AI transaction parsing pipeline.
 * Implementations can range from deterministic rule-based parsers to
 * local LLMs (e.g., Gemma Nano via MediaPipe / Google AI Edge).
 *
 * The AI must NEVER write directly to the database.
 * ParseResult goes through TransactionValidator before persistence.
 */
interface LocalTransactionParser {
    /**
     * Parses a natural language text input into one or more transactions.
     * @param text Raw user input (from voice-to-text or typed).
     * @return [ParseResult] containing parsed transactions and confidence score.
     */
    suspend fun parse(text: String): ParseResult
}
