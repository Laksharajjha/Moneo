package com.moneo.app.ai.model

import com.moneo.app.domain.model.Category
import com.moneo.app.domain.model.TransactionType
import java.time.LocalDate

/**
 * Output contract for the AI/parser pipeline.
 * The AI/parser must NEVER write directly to the DB.
 * It produces this structure which goes through validation before persistence.
 */
data class ParseResult(
    val transactions: List<ParsedTransaction>,
    val rawText: String,
    val confidence: Float,
    val parseError: String? = null
) {
    val isSuccess: Boolean get() = transactions.isNotEmpty() && parseError == null
    val requiresConfirmation: Boolean get() = confidence < 0.90f
}

data class ParsedTransaction(
    val type: TransactionType,
    val amountInPaise: Long,
    val currency: String = "INR",
    val merchant: String? = null,
    val category: Category,
    val date: LocalDate,
    val description: String? = null,
    val person: String? = null,
    val account: String? = null,
    val toAccount: String? = null,
    val isRecurring: Boolean = false,
    val billingCycle: String? = null,
    val confidence: Float = 1.0f
)
