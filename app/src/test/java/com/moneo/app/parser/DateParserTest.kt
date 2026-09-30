package com.moneo.app.parser

import com.moneo.app.ai.parser.DateParser
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class DateParserTest {

    @Test
    fun `today resolves to today`() {
        assertEquals(LocalDate.now(), DateParser.parse("spent 250 today"))
    }

    @Test
    fun `yesterday resolves correctly`() {
        assertEquals(LocalDate.now().minusDays(1), DateParser.parse("spent 250 yesterday"))
    }

    @Test
    fun `this morning resolves to today`() {
        assertEquals(LocalDate.now(), DateParser.parse("this morning coffee"))
    }

    @Test
    fun `last night resolves to yesterday`() {
        assertEquals(LocalDate.now().minusDays(1), DateParser.parse("last night dinner"))
    }

    @Test
    fun `two days ago resolves correctly`() {
        assertEquals(LocalDate.now().minusDays(2), DateParser.parse("two days ago at Zara"))
    }

    @Test
    fun `N days ago pattern`() {
        assertEquals(LocalDate.now().minusDays(3), DateParser.parse("3 days ago"))
    }

    @Test
    fun `no date keyword defaults to today`() {
        assertEquals(LocalDate.now(), DateParser.parse("Uber 340"))
    }

    @Test
    fun `last friday resolves to previous friday`() {
        val result = DateParser.parse("last Friday spent 500")
        assertEquals(java.time.DayOfWeek.FRIDAY, result.dayOfWeek)
        assertTrue(result.isBefore(LocalDate.now()))
    }
}
