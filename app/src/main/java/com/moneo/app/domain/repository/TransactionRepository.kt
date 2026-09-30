package com.moneo.app.domain.repository

import com.moneo.app.domain.model.TimePeriod
import com.moneo.app.domain.model.Transaction
import com.moneo.app.domain.model.TransactionSummary
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getTransactionsForPeriod(period: TimePeriod): Flow<List<Transaction>>
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getTransactionById(id: Long): Flow<Transaction?>
    suspend fun addTransaction(transaction: Transaction): Long
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(id: Long)
    fun getSummaryForPeriod(period: TimePeriod): Flow<TransactionSummary>
}
