package com.moneo.app.domain.usecase

import com.moneo.app.domain.model.TimePeriod
import com.moneo.app.domain.model.TransactionSummary
import com.moneo.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTransactionSummaryUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(period: TimePeriod): Flow<TransactionSummary> =
        repository.getSummaryForPeriod(period)
}
