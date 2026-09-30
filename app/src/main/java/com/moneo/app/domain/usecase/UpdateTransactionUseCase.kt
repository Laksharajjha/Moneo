package com.moneo.app.domain.usecase

import com.moneo.app.domain.model.Transaction
import com.moneo.app.domain.repository.TransactionRepository
import javax.inject.Inject

class UpdateTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction): Result<Unit> = runCatching {
        require(transaction.amountInPaise > 0) { "Amount must be positive" }
        repository.updateTransaction(transaction)
    }
}
