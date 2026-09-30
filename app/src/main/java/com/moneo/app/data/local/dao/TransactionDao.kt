package com.moneo.app.data.local.dao

import androidx.room.*
import com.moneo.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    fun getTransactionsBetweenDates(startDate: String, endDate: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE person = :person ORDER BY timestamp DESC")
    fun getTransactionsByPerson(person: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isRecurring = 1 ORDER BY timestamp DESC")
    fun getRecurringTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE account = :account OR toAccount = :account ORDER BY timestamp DESC")
    fun getTransactionsByAccount(account: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getTransactionById(id: Long): Flow<TransactionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("SELECT COALESCE(SUM(amountInPaise), 0) FROM transactions WHERE type = 'EXPENSE' AND date BETWEEN :startDate AND :endDate")
    fun getTotalExpenseBetweenDates(startDate: String, endDate: String): Flow<Long>

    @Query("SELECT COALESCE(SUM(amountInPaise), 0) FROM transactions WHERE type = 'INCOME' AND date BETWEEN :startDate AND :endDate")
    fun getTotalIncomeBetweenDates(startDate: String, endDate: String): Flow<Long>

    @Query("SELECT category, COALESCE(SUM(amountInPaise), 0) as total FROM transactions WHERE type = 'EXPENSE' AND date BETWEEN :startDate AND :endDate GROUP BY category ORDER BY total DESC")
    fun getCategoryTotalsBetweenDates(startDate: String, endDate: String): Flow<List<CategoryTotal>>

    @Query("SELECT COUNT(*) FROM transactions WHERE date BETWEEN :startDate AND :endDate")
    fun getCountBetweenDates(startDate: String, endDate: String): Flow<Int>

    data class CategoryTotal(val category: String, val total: Long)
}
