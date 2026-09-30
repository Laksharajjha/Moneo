package com.moneo.app.domain.usecase

import com.moneo.app.domain.model.TimePeriod
import com.moneo.app.domain.model.Transaction
import com.moneo.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(period: TimePeriod): Flow<List<Transaction>> =
        repository.getTransactionsForPeriod(period)

    fun all(): Flow<List<Transaction>> = repository.getAllTransactions()
}
