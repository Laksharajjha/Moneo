package com.moneo.app.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Core domain model for a financial transaction.
 *
 * [amountInPaise] stores amounts as Long in minor currency units to avoid
 * floating-point precision errors. For INR: ₹250.50 = 25050 paise.
 */
data class Transaction(
    val id: Long = 0L,
    /** Amount in minor currency units (paise for INR). */
    val amountInPaise: Long,
    val currency: String = "INR",
    val type: TransactionType,
    val category: Category,
    val merchant: String? = null,
    val description: String? = null,
    val date: LocalDate,
    val timestamp: LocalDateTime,
    val paymentMethod: String? = null,
    val person: String? = null,
    val account: String? = null,
    val toAccount: String? = null,
    val isRecurring: Boolean = false,
    val billingCycle: String? = null,
    val notes: String? = null,
    val source: TransactionSource = TransactionSource.MANUAL,
    val confidence: Float = 1.0f,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /** Amount in major currency units for display only. Never use for arithmetic. */
    val amountInRupees: Double get() = amountInPaise / 100.0

    /** Best display name: merchant > description > category name */
    val displayName: String get() = merchant ?: description ?: category.displayName

    companion object {
        /** Converts a rupee amount (as Double) to paise Long for storage. */
        fun rupeesToPaise(rupees: Double): Long = (rupees * 100).toLong()

        /** Converts a paise Long back to rupees String for display. */
        fun formatPaise(paise: Long): String {
            val rupees = paise / 100
            val paiseRemainder = paise % 100
            return if (paiseRemainder == 0L) {
                "\u20B9${formatWithCommas(rupees)}"
            } else {
                "\u20B9${formatWithCommas(rupees)}.${paiseRemainder.toString().padStart(2, '0')}"
            }
        }

        private fun formatWithCommas(amount: Long): String {
            if (amount < 1000) return amount.toString()
            val s = amount.toString()
            val sb = StringBuilder()
            val firstGroup = s.length % 2
            s.forEachIndexed { i, c ->
                if (i > 0 && i == firstGroup) sb.append(',')
                else if (i > firstGroup && (i - firstGroup) % 2 == 0) sb.append(',')
                sb.append(c)
            }
            return sb.toString()
        }
    }
}
