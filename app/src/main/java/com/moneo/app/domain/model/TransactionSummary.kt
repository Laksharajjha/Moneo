package com.moneo.app.domain.model

data class TransactionSummary(
    val period: TimePeriod,
    val totalExpenseInPaise: Long = 0L,
    val totalIncomeInPaise: Long = 0L,
    val categoryBreakdown: Map<Category, Long> = emptyMap(),
    val transactionCount: Int = 0
) {
    val netInPaise: Long get() = totalIncomeInPaise - totalExpenseInPaise

    fun isEmpty(): Boolean = transactionCount == 0
}
