package com.moneo.app.domain.usecase

import com.moneo.app.domain.repository.TransactionRepository
import javax.inject.Inject

class DeleteTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(id: Long): Result<Unit> = runCatching {
        repository.deleteTransaction(id)
    }
}
