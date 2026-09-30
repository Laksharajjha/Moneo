package com.moneo.app.presentation.insights

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.moneo.app.ai.parser.AmountParser
import com.moneo.app.domain.model.TimePeriod
import com.moneo.app.domain.model.TransactionType
import com.moneo.app.presentation.home.HomeViewModel
import java.time.LocalDate

@Composable
fun InsightsScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.selectPeriod(TimePeriod.MONTH)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 28.dp, end = 28.dp,
            top = 24.dp, bottom = 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                text = "Insights",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        }

        val summary = state.summary
        val transactions = state.recentTransactions

        if (summary != null) {
            // Net Cash Flow
            item {
                val netFlow = summary.totalIncomeInPaise - summary.totalExpenseInPaise
                val prefix = if (netFlow < 0) "-" else ""
                val absFlow = kotlin.math.abs(netFlow)
                val formatted = prefix + AmountParser.formatPaise(absFlow)
                
                InsightCard(title = "Net Cash Flow") {
                    Text(
                        text = formatted,
                        style = MaterialTheme.typography.displaySmall,
                        color = if (netFlow < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Total Income - Total Expense",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Average Daily Spending
            item {
                val currentDay = LocalDate.now().dayOfMonth
                val avgPaise = if (currentDay > 0) summary.totalExpenseInPaise / currentDay else 0L
                
                InsightCard(title = "Average Daily Spending") {
                    Text(
                        text = AmountParser.formatPaise(avgPaise),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Over $currentDay days this month",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Largest Expense
            item {
                val largestExpense = transactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .maxByOrNull { it.amountInPaise }

                InsightCard(title = "Largest Expense") {
                    if (largestExpense != null) {
                        Text(
                            text = AmountParser.formatPaise(largestExpense.amountInPaise),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = largestExpense.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "No expenses yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Category breakdown
            val breakdown = summary.categoryBreakdown
            if (breakdown.isNotEmpty()) {
                item {
                    InsightCard(title = "By Category") {
                        breakdown.entries.sortedByDescending { it.value }.forEach { (category, amount) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = category.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = AmountParser.formatPaise(amount),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }
        }

        // Subscriptions
        item {
            val subscriptions = transactions.filter { it.isRecurring }
            InsightCard(title = "Recurring / Subscriptions") {
                if (subscriptions.isNotEmpty()) {
                    subscriptions.forEach { sub ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = sub.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = AmountParser.formatPaise(sub.amountInPaise),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            thickness = 0.5.dp
                        )
                    }
                } else {
                    Text(
                        text = "No recurring transactions detected.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun InsightCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(16.dp))
        content()
    }
}
