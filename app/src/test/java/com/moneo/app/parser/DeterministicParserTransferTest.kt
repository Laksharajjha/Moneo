package com.moneo.app.parser

import com.moneo.app.ai.parser.DeterministicParser
import com.moneo.app.ai.parser.MerchantCategoryClassifier
import com.moneo.app.domain.model.TransactionType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DeterministicParserTransferTest {
    private val classifier = MerchantCategoryClassifier()
    private val parser = DeterministicParser(classifier)

    @Test
    fun `parse transfer`() = runTest {
        val result = parser.parse("2000 transferred to HDFC")
        val tx = result.transactions.first()
        assertEquals(TransactionType.TRANSFER, tx.type)
        assertEquals(200000L, tx.amountInPaise)
    }
}
