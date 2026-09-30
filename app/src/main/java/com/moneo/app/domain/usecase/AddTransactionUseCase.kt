package com.moneo.app.domain.usecase

import com.moneo.app.domain.model.Transaction
import com.moneo.app.domain.repository.TransactionRepository
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction): Result<Long> = runCatching {
        require(transaction.amountInPaise > 0) { "Amount must be positive" }
        repository.addTransaction(transaction)
    }
}
