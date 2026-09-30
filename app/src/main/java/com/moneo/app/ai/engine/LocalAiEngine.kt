package com.moneo.app.ai.engine

import com.moneo.app.ai.model.ParseResult

data class QueryIntent(
    val intent: String,
    val category: String?,
    val period: String?
)

interface LocalAiEngine {
    suspend fun isAvailable(): Boolean
    suspend fun extractFinancialEvent(input: String): ParseResult
    suspend fun interpretQuery(query: String): QueryIntent
}
