package com.moneo.app.presentation.home

import com.moneo.app.domain.model.TimePeriod
import com.moneo.app.domain.model.Transaction
import com.moneo.app.domain.model.TransactionSummary

data class HomeUiState(
    val selectedPeriod: TimePeriod = TimePeriod.TODAY,
    val summary: TransactionSummary? = null,
    val recentTransactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
