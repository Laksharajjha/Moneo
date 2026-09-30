package com.moneo.app.ai.parser

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Parses monetary amounts from natural language strings.
 *
 * Handles: 250, ₹250, Rs 250, Rs. 250, 250 rupees, 2.5k, 4.5K,
 *          1 lakh, 1.2 lakh, ₹1,299.50
 *
 * All amounts are returned as Long in paise (minor INR units).
 * ₹250.50 → 25050 paise
 */
object AmountParser {

    private val RUPEE_SYMBOL = "\u20B9"

    // Patterns for amount extraction — ordered by specificity
    private val LAKH_PATTERN = Regex(
        """(?:${RUPEE_SYMBOL}|[Rr]s\.?\s*)?([0-9]+(?:\.[0-9]+)?)\s*[Ll]akh"""
    )
    private val K_PATTERN = Regex(
        """(?:${RUPEE_SYMBOL}|[Rr]s\.?\s*)?([0-9]+(?:\.[0-9]+)?)\s*[Kk]"""
    )
    private val PLAIN_PATTERN = Regex(
        """(?:${RUPEE_SYMBOL}|[Rr]s\.?\s*)?([0-9]{1,3}(?:,[0-9]{2,3})+(?:\.[0-9]{1,2})?|[0-9]+(?:\.[0-9]{1,2})?)\s*(?:[Rr]upees?)?"""
    )

    /**
     * Returns all amounts found in the text, in order of appearance.
     * Each amount is in paise.
     */
    fun parseAll(text: String): List<Long> {
        val results = mutableListOf<Pair<Int, Long>>() // (start index, paise)
        val normalized = text.trim()

        // Try lakh pattern first
        LAKH_PATTERN.findAll(normalized).forEach { match ->
            val value = match.groupValues[1].toDoubleOrNull() ?: return@forEach
            val paise = (BigDecimal(value) * BigDecimal("100000") * BigDecimal("100"))
                .setScale(0, RoundingMode.HALF_UP).toLong()
            results.add(Pair(match.range.first, paise))
        }

        // Try K/thousands pattern
        K_PATTERN.findAll(normalized).forEach { match ->
            val start = match.range.first
            if (results.any { it.first == start }) return@forEach // already matched as lakh
            val value = match.groupValues[1].toDoubleOrNull() ?: return@forEach
            val paise = (BigDecimal(value) * BigDecimal("1000") * BigDecimal("100"))
                .setScale(0, RoundingMode.HALF_UP).toLong()
            results.add(Pair(start, paise))
        }

        // Try plain number pattern
        PLAIN_PATTERN.findAll(normalized).forEach { match ->
            val start = match.range.first
            if (results.any { it.first == start }) return@forEach
            val raw = match.groupValues[1].replace(",", "")
            val value = raw.toDoubleOrNull() ?: return@forEach
            if (value <= 0) return@forEach
            val paise = (BigDecimal(value.toString()) * BigDecimal("100"))
                .setScale(0, RoundingMode.HALF_UP).toLong()
            results.add(Pair(start, paise))
        }

        return results.sortedBy { it.first }.map { it.second }
    }

    /**
     * Returns the first/primary amount found, or null.
     */
    fun parseFirst(text: String): Long? = parseAll(text).firstOrNull()

    /**
     * Formats paise as a display string. e.g., 25050 → ₹250.50
     */
    fun formatPaise(paise: Long): String {
        val rupees = paise / 100L
        val cents = paise % 100L
        val formatted = formatWithIndianCommas(rupees)
        return if (cents == 0L) "\u20B9$formatted"
        else "\u20B9$formatted.${cents.toString().padStart(2, '0')}"
    }

    private fun formatWithIndianCommas(amount: Long): String {
        if (amount < 1000) return amount.toString()
        val s = amount.toString()
        if (s.length <= 3) return s
        // Indian numbering: last 3, then groups of 2
        val last3 = s.takeLast(3)
        val rest = s.dropLast(3)
        val sb = StringBuilder()
        rest.reversed().chunked(2).forEachIndexed { i, chunk ->
            if (i > 0) sb.append(',')
            sb.append(chunk.reversed())
        }
        return sb.toString().reversed() + "," + last3
    }
}
