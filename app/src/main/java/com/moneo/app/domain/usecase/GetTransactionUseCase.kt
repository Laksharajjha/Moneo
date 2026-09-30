package com.moneo.app.domain.usecase

import com.moneo.app.domain.model.Transaction
import com.moneo.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(id: Long): Flow<Transaction?> =
        repository.getTransactionById(id)
}
