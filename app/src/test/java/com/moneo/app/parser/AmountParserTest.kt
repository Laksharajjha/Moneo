package com.moneo.app.parser

import com.moneo.app.ai.parser.AmountParser
import org.junit.Assert.*
import org.junit.Test

class AmountParserTest {

    @Test
    fun `parse plain integer`() {
        assertEquals(25000L, AmountParser.parseFirst("250"))
    }

    @Test
    fun `parse with rupee symbol`() {
        assertEquals(25000L, AmountParser.parseFirst("\u20B9250"))
    }

    @Test
    fun `parse with Rs prefix`() {
        assertEquals(25000L, AmountParser.parseFirst("Rs 250"))
    }

    @Test
    fun `parse with Rs dot prefix`() {
        assertEquals(25000L, AmountParser.parseFirst("Rs. 250"))
    }

    @Test
    fun `parse k notation lowercase`() {
        assertEquals(250000L, AmountParser.parseFirst("2.5k"))
    }

    @Test
    fun `parse K notation uppercase`() {
        assertEquals(250000L, AmountParser.parseFirst("2.5K"))
    }

    @Test
    fun `parse lakh`() {
        assertEquals(10000000L, AmountParser.parseFirst("1 lakh"))
    }

    @Test
    fun `parse partial lakh`() {
        assertEquals(12000000L, AmountParser.parseFirst("1.2 lakh"))
    }

    @Test
    fun `parse decimal amount`() {
        assertEquals(129950L, AmountParser.parseFirst("\u20B91,299.50"))
    }

    @Test
    fun `parse amount with commas`() {
        assertEquals(150000L, AmountParser.parseFirst("1,500"))
    }

    @Test
    fun `returns null for empty string`() {
        assertNull(AmountParser.parseFirst(""))
    }

    @Test
    fun `returns null for no number`() {
        assertNull(AmountParser.parseFirst("hello world"))
    }

    @Test
    fun `parseAll finds multiple amounts`() {
        val result = AmountParser.parseAll("120 coffee 350 lunch 180 uber")
        assertTrue(result.size >= 2)
        assertTrue(result.contains(12000L))
        assertTrue(result.contains(35000L))
        assertTrue(result.contains(18000L))
    }

    @Test
    fun `format paise to display string`() {
        assertEquals("\u20B9250", AmountParser.formatPaise(25000L))
        assertEquals("\u20B9250.50", AmountParser.formatPaise(25050L))
    }
}
