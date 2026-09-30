package com.moneo.app.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.moneo.app.ai.parser.AmountParser
import com.moneo.app.domain.model.Category
import com.moneo.app.domain.model.TimePeriod
import com.moneo.app.domain.model.Transaction
import com.moneo.app.domain.model.TransactionSummary
import com.moneo.app.presentation.components.AmountText
import com.moneo.app.presentation.components.TransactionItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    onNavigateToCapture: () -> Unit,
    onNavigateToTransaction: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = 24.dp, bottom = 120.dp
        ),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Header row: date + settings
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = LocalDate.now().format(
                        DateTimeFormatter.ofPattern("MMMM d", Locale.getDefault())
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(28.dp))
        }

        // Primary amount display
        item {
            PrimaryAmountSection(
                state = state, 
                modifier = Modifier.padding(horizontal = 28.dp)
            )
            Spacer(Modifier.height(36.dp))
        }

        // Period selector chips
        item {
            PeriodSelector(
                selectedPeriod = state.selectedPeriod,
                onPeriodSelected = viewModel::selectPeriod
            )
            Spacer(Modifier.height(36.dp))
        }

        // Category breakdown
        val summary = state.summary
        if (summary != null && summary.categoryBreakdown.isNotEmpty()) {
            item {
                Text(
                    text = "By category",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 28.dp)
                )
                Spacer(Modifier.height(16.dp))
            }
            items(
                items = summary.categoryBreakdown.entries
                    .sortedByDescending { it.value }.take(5),
                key = { it.key.name }
            ) { (category, amountInPaise) ->
                CategoryBreakdownRow(
                    category = category,
                    amountInPaise = amountInPaise,
                    totalExpense = summary.totalExpenseInPaise,
                    modifier = Modifier.padding(horizontal = 28.dp)
                )
            }
            item { Spacer(Modifier.height(36.dp)) }
        }

        // Recent transactions
        if (state.recentTransactions.isNotEmpty()) {
            item {
                Text(
                    text = "Recent",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 28.dp)
                )
                Spacer(Modifier.height(4.dp))
            }
            items(state.recentTransactions, key = { it.id }) { transaction ->
                TransactionItem(
                    transaction = transaction,
                    onClick = { onNavigateToTransaction(transaction.id) },
                    modifier = Modifier.padding(horizontal = 28.dp)
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = 28.dp)
                )
            }
        }

        // Empty state
        if (!state.isLoading && state.recentTransactions.isEmpty()) {
            item {
                Spacer(Modifier.height(48.dp))
                HomeEmptyState(
                    onCaptureClick = onNavigateToCapture,
                    modifier = Modifier.padding(horizontal = 28.dp)
                )
            }
        }
    }
}

@Composable
private fun PrimaryAmountSection(
    state: HomeUiState,
    modifier: Modifier = Modifier
) {
    val amountInPaise = state.summary?.totalExpenseInPaise ?: 0L
    Column(modifier = modifier) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = AmountParser.formatPaise(amountInPaise),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Light
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "spent ${state.selectedPeriod.displayName.lowercase()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if ((state.summary?.totalIncomeInPaise ?: 0L) > 0L) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "+${AmountParser.formatPaise(state.summary!!.totalIncomeInPaise)} income",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PeriodSelector(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 28.dp)
    ) {
        items(TimePeriod.entries, key = { it.name }) { period ->
            val isSelected = period == selectedPeriod
            FilterChip(
                selected = isSelected,
                onClick = { onPeriodSelected(period) },
                label = {
                    Text(
                        text = period.displayName,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.onSurface,
                    selectedLabelColor = MaterialTheme.colorScheme.surface,
                    containerColor = Color.Transparent,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = MaterialTheme.colorScheme.outline,
                    selectedBorderColor = Color.Transparent,
                    borderWidth = 0.5.dp
                )
            )
        }
    }
}

@Composable
private fun CategoryBreakdownRow(
    category: Category,
    amountInPaise: Long,
    totalExpense: Long,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = category.displayName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = AmountParser.formatPaise(amountInPaise),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HomeEmptyState(
    onCaptureClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Nothing here yet.",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Try saying something like:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "\"Spent \u20B9250 on lunch\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "\"Uber was 340\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
