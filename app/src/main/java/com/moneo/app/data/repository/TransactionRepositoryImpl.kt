package com.moneo.app.data.repository

import com.moneo.app.data.local.dao.TransactionDao
import com.moneo.app.data.mapper.toDomain
import com.moneo.app.data.mapper.toEntity
import com.moneo.app.domain.model.Category
import com.moneo.app.domain.model.TimePeriod
import com.moneo.app.domain.model.Transaction
import com.moneo.app.domain.model.TransactionSummary
import com.moneo.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override fun getTransactionsForPeriod(period: TimePeriod): Flow<List<Transaction>> {
        val (startDate, endDate) = period.dateRange()
        return transactionDao
            .getTransactionsBetweenDates(startDate.toString(), endDate.toString())
            .map { list -> list.map { it.toDomain() } }
    }

    override fun getAllTransactions(): Flow<List<Transaction>> =
        transactionDao.getAllTransactions().map { list -> list.map { it.toDomain() } }

    override fun getTransactionById(id: Long): Flow<Transaction?> =
        transactionDao.getTransactionById(id).map { it?.toDomain() }

    override suspend fun addTransaction(transaction: Transaction): Long =
        transactionDao.insertTransaction(transaction.toEntity())

    override suspend fun updateTransaction(transaction: Transaction) =
        transactionDao.updateTransaction(transaction.toEntity())

    override suspend fun deleteTransaction(id: Long) =
        transactionDao.deleteTransactionById(id)

    override fun getSummaryForPeriod(period: TimePeriod): Flow<TransactionSummary> {
        val (startDate, endDate) = period.dateRange()
        val start = startDate.toString()
        val end = endDate.toString()
        return combine(
            transactionDao.getTotalExpenseBetweenDates(start, end),
            transactionDao.getTotalIncomeBetweenDates(start, end),
            transactionDao.getCategoryTotalsBetweenDates(start, end),
            transactionDao.getCountBetweenDates(start, end)
        ) { expense, income, categoryTotals, count ->
            val breakdown = categoryTotals.associate { ct ->
                Category.valueOf(ct.category) to ct.total
            }
            TransactionSummary(
                period = period,
                totalExpenseInPaise = expense,
                totalIncomeInPaise = income,
                categoryBreakdown = breakdown,
                transactionCount = count
            )
        }
    }
}

fun TimePeriod.dateRange(): Pair<LocalDate, LocalDate> {
    val today = LocalDate.now()
    return when (this) {
        TimePeriod.TODAY -> Pair(today, today)
        TimePeriod.YESTERDAY -> Pair(today.minusDays(1), today.minusDays(1))
        TimePeriod.WEEK -> Pair(today.minusDays(6), today)
        TimePeriod.MONTH -> Pair(today.withDayOfMonth(1), today)
        TimePeriod.YEAR -> Pair(today.withDayOfYear(1), today)
    }
}
