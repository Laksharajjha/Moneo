package com.moneo.app.ai.parser

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/**
 * Parses natural language date expressions relative to the device's local time.
 * All expressions are resolved to a [LocalDate].
 */
object DateParser {

    fun parse(text: String): LocalDate {
        val lower = text.lowercase().trim()
        val today = LocalDate.now()

        return when {
            lower.contains("today") || lower.contains("now") ||
            lower.contains("this morning") || lower.contains("this evening") ||
            lower.contains("tonight") || lower.contains("just now") -> today

            lower.contains("yesterday") || lower.contains("last night") -> today.minusDays(1)

            lower.contains("tomorrow") -> today.plusDays(1)

            Regex("(\\d+)\\s+days?\\s+ago").containsMatchIn(lower) -> {
                val n = Regex("(\\d+)\\s+days?\\s+ago").find(lower)
                    ?.groupValues?.get(1)?.toLongOrNull() ?: 1L
                today.minusDays(n)
            }

            lower.contains("two days ago") -> today.minusDays(2)
            lower.contains("three days ago") -> today.minusDays(3)

            lower.contains("last week") -> today.minusWeeks(1)
            lower.contains("last month") -> today.minusMonths(1)

            Regex("last\\s+(monday|tuesday|wednesday|thursday|friday|saturday|sunday)").containsMatchIn(lower) -> {
                val dayName = Regex("last\\s+(monday|tuesday|wednesday|thursday|friday|saturday|sunday)")
                    .find(lower)?.groupValues?.get(1) ?: return today
                val targetDay = parseDayOfWeek(dayName)
                today.with(TemporalAdjusters.previous(targetDay))
            }

            Regex("on\\s+(monday|tuesday|wednesday|thursday|friday|saturday|sunday)").containsMatchIn(lower) -> {
                val dayName = Regex("on\\s+(monday|tuesday|wednesday|thursday|friday|saturday|sunday)")
                    .find(lower)?.groupValues?.get(1) ?: return today
                val targetDay = parseDayOfWeek(dayName)
                val candidate = today.with(TemporalAdjusters.previousOrSame(targetDay))
                if (candidate == today) today.minusWeeks(1).with(TemporalAdjusters.nextOrSame(targetDay))
                else candidate
            }

            else -> today
        }
    }

    private fun parseDayOfWeek(name: String): DayOfWeek = when (name.lowercase()) {
        "monday" -> DayOfWeek.MONDAY
        "tuesday" -> DayOfWeek.TUESDAY
        "wednesday" -> DayOfWeek.WEDNESDAY
        "thursday" -> DayOfWeek.THURSDAY
        "friday" -> DayOfWeek.FRIDAY
        "saturday" -> DayOfWeek.SATURDAY
        else -> DayOfWeek.SUNDAY
    }
}
