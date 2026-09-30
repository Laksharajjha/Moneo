package com.moneo.app.ai.parser

import com.moneo.app.ai.model.ParsedFinancialEvent
import com.moneo.app.ai.model.ParsedTransaction
import com.moneo.app.domain.model.TransactionType
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalMessageParser @Inject constructor(
    private val categoryClassifier: MerchantCategoryClassifier
) : MessageTransactionParser {

    private val negativeKeywords = listOf(
        "otp", "verification", "cashback", "shipped", "dispatched", 
        "offer", "loan", "balance is", "reminder"
    )

    // Regexes
    private val amountRegex = Regex("(?i)(?:rs\\.?|inr|₹)\\s?([0-9,.]+)")
    private val upiRegex = Regex("(?i)paid to (.*?)(?: via| on|\\.)")
    private val bankDebitRegex = Regex("(?i)(?:debited by|spent on) (?:.*? )?(?:rs\\.?|inr|₹)\\s?([0-9,.]+)")
    private val bankCreditRegex = Regex("(?i)(?:credited|received)")
    private val transferRegex = Regex("(?i)transferred to (.*?)(?:\\.|$)")
    private val refundRegex = Regex("(?i)refunded(?: by (.*?))?(?:\\.|$)")

    override suspend fun parseMessage(message: String): ParsedFinancialEvent {
        val lowerMessage = message.lowercase()

        // 1. Check negative filters
        if (negativeKeywords.any { lowerMessage.contains(it) }) {
            return ParsedFinancialEvent(
                isFinancial = false,
                transaction = null,
                confidence = 0.99f,
                rawMessage = message
            )
        }

        // 2. Extract amount
        val amountMatch = amountRegex.find(message)
        if (amountMatch == null) {
            return ParsedFinancialEvent(
                isFinancial = false,
                transaction = null,
                confidence = 0.8f,
                rawMessage = message
            )
        }

        val amountStr = amountMatch.groupValues[1].replace(",", "")
        val amountInPaise = (amountStr.toDoubleOrNull()?.times(100))?.toLong() ?: 0L

        if (amountInPaise <= 0L) {
            return ParsedFinancialEvent(false, null, 0.9f, false, message)
        }

        var type = TransactionType.EXPENSE
        var merchant: String? = null
        var account: String? = null
        var toAccount: String? = null
        var paymentMethod: String? = null

        // 3. Determine Event Type & Entities
        if (lowerMessage.contains("refunded")) {
            type = TransactionType.REFUND
            merchant = refundRegex.find(message)?.groupValues?.getOrNull(1)?.trim()
        } else if (lowerMessage.contains("transferred to")) {
            type = TransactionType.TRANSFER
            toAccount = transferRegex.find(message)?.groupValues?.getOrNull(1)?.trim()
        } else if (bankCreditRegex.containsMatchIn(message)) {
            type = TransactionType.INCOME
        } else if (lowerMessage.contains("paid to") || lowerMessage.contains("sent to")) {
            type = TransactionType.EXPENSE
            merchant = upiRegex.find(message)?.groupValues?.getOrNull(1)?.trim()
            paymentMethod = "UPI"
        } else if (lowerMessage.contains("debited") || lowerMessage.contains("withdrawn")) {
            type = TransactionType.EXPENSE
        } else {
            // Ambiguous
            return ParsedFinancialEvent(
                isFinancial = true,
                transaction = ParsedTransaction(
                    amountInPaise = amountInPaise,
                    type = type,
                    category = categoryClassifier.classify(merchant ?: ""),
                    merchant = merchant,
                    date = LocalDate.now(),
                    confidence = 0.5f
                ),
                confidence = 0.5f,
                rawMessage = message
            )
        }

        // Cleanup merchant names
        merchant = merchant?.takeIf { it.isNotBlank() && it.length < 30 }

        val parsedTx = ParsedTransaction(
            amountInPaise = amountInPaise,
            type = type,
            category = categoryClassifier.classify(merchant ?: ""),
            merchant = merchant,
            account = account,
            toAccount = toAccount,
            date = LocalDate.now(), // Fallback to today for now
            confidence = 0.95f,
            isRecurring = false,
            billingCycle = null,
            person = null
        )

        return ParsedFinancialEvent(
            isFinancial = true,
            transaction = parsedTx,
            confidence = 0.95f,
            rawMessage = message
        )
    }
}
