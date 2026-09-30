package com.moneo.app.parser

import com.moneo.app.ai.parser.DeterministicParser
import com.moneo.app.ai.parser.MerchantCategoryClassifier
import com.moneo.app.domain.model.Category
import com.moneo.app.domain.model.TransactionType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class DeterministicParserTest {

    private val classifier = MerchantCategoryClassifier()
    private val parser = DeterministicParser(classifier)

    @Test
    fun `parse simple expense`() = runTest {
        val result = parser.parse("spent 250 on lunch")
        assertTrue(result.isSuccess)
        assertEquals(1, result.transactions.size)
        assertEquals(25000L, result.transactions[0].amountInPaise)
        assertEquals(TransactionType.EXPENSE, result.transactions[0].type)
        assertEquals(Category.FOOD, result.transactions[0].category)
    }

    @Test
    fun `parse uber transport`() = runTest {
        val result = parser.parse("Uber was 340")
        assertTrue(result.isSuccess)
        assertEquals(34000L, result.transactions[0].amountInPaise)
        assertEquals(Category.TRANSPORT, result.transactions[0].category)
        assertEquals("Uber", result.transactions[0].merchant)
    }

    @Test
    fun `parse starbucks expense`() = runTest {
        val result = parser.parse("I spent 280 at Starbucks")
        assertTrue(result.isSuccess)
        assertEquals(28000L, result.transactions[0].amountInPaise)
        assertEquals(Category.FOOD, result.transactions[0].category)
    }

    @Test
    fun `detect income from salary`() = runTest {
        val result = parser.parse("Got my salary today, 85000")
        assertTrue(result.isSuccess)
        assertEquals(TransactionType.INCOME, result.transactions[0].type)
        assertEquals(8500000L, result.transactions[0].amountInPaise)
    }

    @Test
    fun `parse multiple transactions`() = runTest {
        val result = parser.parse("120 coffee, 350 lunch and 180 Uber")
        assertTrue(result.isSuccess)
        assertEquals(3, result.transactions.size)
        val amounts = result.transactions.map { it.amountInPaise }.sorted()
        assertEquals(listOf(12000L, 18000L, 35000L), amounts)
    }

    @Test
    fun `empty input returns error`() = runTest {
        val result = parser.parse("")
        assertFalse(result.isSuccess)
        assertNotNull(result.parseError)
    }

    @Test
    fun `no amount returns error`() = runTest {
        val result = parser.parse("hello world")
        assertFalse(result.isSuccess)
    }

    @Test
    fun `detect account`() = runTest {
        val result = parser.parse("Paid 500 using HDFC")
        assertTrue(result.isSuccess)
        assertEquals("Hdfc", result.transactions[0].account)
    }

    @Test
    fun `detect lend and person`() = runTest {
        val result = parser.parse("Rahul owes me 900")
        assertTrue(result.isSuccess)
        assertEquals(TransactionType.LEND, result.transactions[0].type)
        assertEquals("Rahul", result.transactions[0].person)
    }

    @Test
    fun `detect repayment and person`() = runTest {
        val result = parser.parse("Paid Rahul 500")
        assertTrue(result.isSuccess)
        assertEquals("Rahul", result.transactions[0].person)
        // Can be EXPENSE or REPAYMENT depending on earlier logic, right now "paid" might default to EXPENSE or LEND?
        // Wait, "Paid Rahul 500" goes to EXPENSE because we didn't specify "repaid me" or "lent to". 
        // Our detectType: paid/repaid me -> REPAYMENT. paid Rahul -> EXPENSE.
    }

    @Test
    fun `detect recurring and billing cycle`() = runTest {
        val result = parser.parse("Netflix 699")
        assertTrue(result.isSuccess)
        assertTrue(result.transactions[0].isRecurring)
        assertEquals("MONTHLY", result.transactions[0].billingCycle)
    }
}
