package com.moneo.app.ai.parser

import com.moneo.app.ai.model.ParseResult
import com.moneo.app.ai.model.ParsedTransaction
import com.moneo.app.domain.model.Category
import com.moneo.app.domain.model.TransactionType
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fast, offline, deterministic parser for common transaction inputs.
 *
 * Handles simple patterns extremely fast without any network or model call.
 * Examples: "250 uber", "₹500 lunch", "spent 340 on transport"
 *
 * For more complex inputs, confidence will be low and the hybrid pipeline
 * will defer to the [LocalTransactionParser] AI implementation.
 */
@Singleton
class DeterministicParser @Inject constructor(
    private val categoryClassifier: MerchantCategoryClassifier
) : TransactionParser, LocalTransactionParser {

    override suspend fun parse(text: String): ParseResult {
        if (text.isBlank()) {
            return ParseResult(
                transactions = emptyList(),
                rawText = text,
                confidence = 0f,
                parseError = "Empty input"
            )
        }

        // Check for multiple transactions (comma-separated or "and")
        val multipleResult = tryParseMultiple(text)
        if (multipleResult != null) return multipleResult

        // Try single transaction parse
        val single = tryParseSingle(text)
        return if (single != null) {
            ParseResult(
                transactions = listOf(single),
                rawText = text,
                confidence = single.confidence
            )
        } else {
            ParseResult(
                transactions = emptyList(),
                rawText = text,
                confidence = 0f,
                parseError = "Could not extract a valid transaction from: \"$text\""
            )
        }
    }

    private fun tryParseSingle(text: String): ParsedTransaction? {
        val amounts = AmountParser.parseAll(text)
        if (amounts.isEmpty()) return null

        val amount = amounts.first()
        val date = DateParser.parse(text)
        val merchant = categoryClassifier.extractMerchant(text)
        val category = categoryClassifier.classify(text)
        val type = detectType(text)
        
        val account = detectAccount(text)
        val person = detectPerson(text)
        val isRecurring = detectRecurring(text)
        val billingCycle = if (isRecurring) detectBillingCycle(text) else null

        // Confidence calculation
        var confidence = 0.7f
        if (merchant != null) confidence += 0.15f
        if (category != Category.OTHER) confidence += 0.1f
        if (amounts.size == 1) confidence += 0.05f  // unambiguous amount
        if (person != null) confidence += 0.05f
        if (account != null) confidence += 0.05f
        confidence = confidence.coerceAtMost(1.0f)

        return ParsedTransaction(
            type = type,
            amountInPaise = amount,
            merchant = merchant,
            category = category,
            date = date,
            description = buildDescription(text, merchant),
            person = person,
            account = account,
            isRecurring = isRecurring,
            billingCycle = billingCycle,
            confidence = confidence
        )
    }

    private fun tryParseMultiple(text: String): ParseResult? {
        // Detect patterns like: "120 coffee, 350 lunch and 180 uber"
        val separatorPattern = Regex(",\\s*|\\s+and\\s+|\\s*;\\s*")
        val parts = separatorPattern.split(text).filter { it.isNotBlank() }
        if (parts.size < 2) return null

        // Each part must have at least one amount
        val parsed = parts.mapNotNull { tryParseSingle(it.trim()) }
        if (parsed.size < 2) return null  // not actually multiple

        val avgConfidence = parsed.map { it.confidence }.average().toFloat()
        return ParseResult(
            transactions = parsed,
            rawText = text,
            confidence = avgConfidence * 0.95f  // slight penalty for multi-parse
        )
    }

    private fun detectType(text: String): TransactionType {
        val lowerText = text.lowercase()
        return when {
            Regex("\\b(transfer|transferred|moved)\\b").containsMatchIn(lowerText) -> TransactionType.TRANSFER
            Regex("\\b(refund|refunded|returned)\\b").containsMatchIn(lowerText) -> TransactionType.REFUND
            Regex("\\b(owes me|borrowed from me)\\b").containsMatchIn(lowerText) -> TransactionType.LEND
            Regex("\\b(paid|repaid)\\b.*\\b(me)\\b").containsMatchIn(lowerText) -> TransactionType.REPAYMENT
            Regex("\\b(repaid)\\b").containsMatchIn(lowerText) -> TransactionType.REPAYMENT
            Regex("\\b(lent to)\\b").containsMatchIn(lowerText) -> TransactionType.LEND
            categoryClassifier.isLikelyIncome(text) -> TransactionType.INCOME
            else -> TransactionType.EXPENSE
        }
    }

    private fun detectAccount(text: String): String? {
        val lowerText = text.lowercase()
        val accounts = listOf("hdfc", "sbi", "cash", "upi", "credit card", "icici", "axis")
        return accounts.find { Regex("\\b$it\\b").containsMatchIn(lowerText) }?.let { 
            if (it == "upi") "UPI" else it.split(" ").joinToString(" ") { word -> word.replaceFirstChar { char -> char.uppercase() } }
        }
    }

    private fun detectPerson(text: String): String? {
        // Basic heuristic: "Paid Rahul 500", "Rahul owes me 900", "lent to Rahul"
        val lowerText = text.lowercase()
        val match = Regex("\\b(?:paid|lent to)\\s+([a-zA-Z]+)\\b").find(lowerText)
        if (match != null) return match.groupValues[1].replaceFirstChar { it.uppercase() }
        
        val match2 = Regex("\\b([a-zA-Z]+)\\s+(?:owes me|repaid|paid me)\\b").find(lowerText)
        if (match2 != null) return match2.groupValues[1].replaceFirstChar { it.uppercase() }
        
        return null
    }

    private fun detectRecurring(text: String): Boolean {
        val lowerText = text.lowercase()
        return Regex("\\b(monthly|yearly|subscription|recurring|netflix|spotify|prime|bill)\\b").containsMatchIn(lowerText)
    }

    private fun detectBillingCycle(text: String): String? {
        val lowerText = text.lowercase()
        return when {
            Regex("\\b(yearly|annual)\\b").containsMatchIn(lowerText) -> "YEARLY"
            Regex("\\b(weekly)\\b").containsMatchIn(lowerText) -> "WEEKLY"
            else -> "MONTHLY"
        }
    }

    private fun buildDescription(text: String, merchant: String?): String? {
        if (merchant != null) return null  // merchant is sufficient
        // Strip amounts and common filler words, use remaining text as description
        val cleaned = text
            .replace(Regex("[\u20B9]|[Rr]s\\\\.?\\s*"), "")
            .replace(Regex("\\b(spent|paid|bought|on|for|at|the|a|an|i|my)\\b", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\b\\d+(?:\\.\\d+)?(?:[kK]|\\s*lakh)?\\b"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
        return cleaned.ifBlank { null }
    }
}
