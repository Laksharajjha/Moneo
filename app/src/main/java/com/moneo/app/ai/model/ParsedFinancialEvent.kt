package com.moneo.app.ai.model

data class ParsedFinancialEvent(
    val isFinancial: Boolean,
    val transaction: ParsedTransaction?,
    val confidence: Float,
    val isDuplicate: Boolean = false,
    val rawMessage: String
)
