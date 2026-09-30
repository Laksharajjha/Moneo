package com.moneo.app.ai.parser

import com.moneo.app.ai.model.ParsedFinancialEvent

interface MessageTransactionParser {
    suspend fun parseMessage(message: String): ParsedFinancialEvent
}
