package com.moneo.app.ai.parser

import com.moneo.app.ai.model.ParseResult

interface TransactionParser {
    suspend fun parse(text: String): ParseResult
}
