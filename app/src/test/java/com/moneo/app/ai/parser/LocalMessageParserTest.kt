package com.moneo.app.ai.parser

import com.moneo.app.domain.model.TransactionType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LocalMessageParserTest {

    private lateinit var parser: LocalMessageParser
    private lateinit var classifier: MerchantCategoryClassifier

    @Before
    fun setup() {
        classifier = MerchantCategoryClassifier()
        parser = LocalMessageParser(classifier)
    }

    @Test
    fun `test UPI payment`() = runBlocking {
        val result = parser.parseMessage("₹450 paid to Swiggy via UPI")
        assertTrue(result.isFinancial)
        assertEquals(45000L, result.transaction?.amountInPaise)
        assertEquals(TransactionType.EXPENSE, result.transaction?.type)
        assertEquals("Swiggy", result.transaction?.merchant)
    }

    @Test
    fun `test Bank debit`() = runBlocking {
        val result = parser.parseMessage("Your account has been debited by ₹1,299")
        assertTrue(result.isFinancial)
        assertEquals(129900L, result.transaction?.amountInPaise)
        assertEquals(TransactionType.EXPENSE, result.transaction?.type)
    }

    @Test
    fun `test Bank credit`() = runBlocking {
        val result = parser.parseMessage("₹5,000 credited to your account")
        assertTrue(result.isFinancial)
        assertEquals(500000L, result.transaction?.amountInPaise)
        assertEquals(TransactionType.INCOME, result.transaction?.type)
    }

    @Test
    fun `test Transfer`() = runBlocking {
        val result = parser.parseMessage("₹10,000 transferred to HDFC")
        assertTrue(result.isFinancial)
        assertEquals(1000000L, result.transaction?.amountInPaise)
        assertEquals(TransactionType.TRANSFER, result.transaction?.type)
        assertEquals("HDFC", result.transaction?.toAccount)
    }

    @Test
    fun `test Refund`() = runBlocking {
        val result = parser.parseMessage("₹1,200 refunded by Amazon")
        assertTrue(result.isFinancial)
        assertEquals(120000L, result.transaction?.amountInPaise)
        assertEquals(TransactionType.REFUND, result.transaction?.type)
        assertEquals("Amazon", result.transaction?.merchant)
    }

    @Test
    fun `test OTP`() = runBlocking {
        val result = parser.parseMessage("Your OTP is 493821")
        assertFalse(result.isFinancial)
    }

    @Test
    fun `test Advertisement`() = runBlocking {
        val result = parser.parseMessage("Get ₹500 cashback on your next purchase")
        assertFalse(result.isFinancial)
    }

    @Test
    fun `test Shipping`() = runBlocking {
        val result = parser.parseMessage("Your Amazon order worth ₹1,299 has shipped")
        assertFalse(result.isFinancial)
    }
}
