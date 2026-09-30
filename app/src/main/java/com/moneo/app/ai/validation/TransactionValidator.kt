package com.moneo.app.ai.validation

import com.moneo.app.ai.model.ParsedTransaction
import com.moneo.app.domain.model.Transaction
import com.moneo.app.domain.model.TransactionSource
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Validates AI/parser output before it reaches the database.
 *
 * The AI must NEVER write directly to the DB. This validator ensures
 * all parsed transactions meet the required business rules.
 */
object TransactionValidator {

    sealed class ValidationResult {
        data class Valid(val transaction: Transaction) : ValidationResult()
        data class Invalid(val reason: String) : ValidationResult()
    }

    fun validate(parsed: ParsedTransaction): ValidationResult {
        // Amount validation
        if (parsed.amountInPaise <= 0) {
            return ValidationResult.Invalid("Amount must be positive")
        }
        if (parsed.amountInPaise > 10_000_000_00L) { // ₹10 crore cap
            return ValidationResult.Invalid("Amount exceeds maximum allowed value")
        }

        // Date validation
        val maxFutureDate = LocalDate.now().plusDays(1)
        if (parsed.date.isAfter(maxFutureDate)) {
            return ValidationResult.Invalid("Date cannot be more than 1 day in the future")
        }
        val maxPastDate = LocalDate.now().minusYears(10)
        if (parsed.date.isBefore(maxPastDate)) {
            return ValidationResult.Invalid("Date is too far in the past")
        }

        // Confidence check
        if (parsed.confidence < 0.3f) {
            return ValidationResult.Invalid("Confidence too low to auto-accept")
        }

        val now = LocalDateTime.now()
        val transaction = Transaction(
            amountInPaise = parsed.amountInPaise,
            currency = parsed.currency,
            type = parsed.type,
            category = parsed.category,
            merchant = parsed.merchant?.take(100),
            description = parsed.description?.take(200),
            date = parsed.date,
            timestamp = now,
            person = parsed.person?.take(100),
            account = parsed.account?.take(100),
            toAccount = parsed.toAccount?.take(100),
            isRecurring = parsed.isRecurring,
            billingCycle = parsed.billingCycle?.take(50),
            source = TransactionSource.TEXT,
            confidence = parsed.confidence,
            createdAt = now,
            updatedAt = now
        )
        return ValidationResult.Valid(transaction)
    }

    fun validateAll(parsed: List<ParsedTransaction>): List<ValidationResult> =
        parsed.map { validate(it) }
}
