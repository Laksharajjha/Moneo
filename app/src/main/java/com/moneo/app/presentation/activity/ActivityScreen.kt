package com.moneo.app.presentation.activity

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.moneo.app.domain.model.TimePeriod
import com.moneo.app.presentation.components.TransactionItem

@Composable
fun ActivityScreen(
    onNavigateToTransaction: (Long) -> Unit,
    viewModel: ActivityViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Text(
            text = "Activity",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 28.dp, top = 24.dp, bottom = 16.dp)
        )

        // Segmented Control
        ActivitySegmentedControl(
            selectedTab = state.selectedTab,
            onTabSelected = viewModel::selectTab,
            modifier = Modifier.padding(horizontal = 28.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        // Time Period filter (Only show if not in INBOX tab)
        if (state.selectedTab != ActivityTab.INBOX) {
            PeriodSelector(
                selectedPeriod = state.selectedPeriod,
                onPeriodSelected = viewModel::selectPeriod
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            if (state.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                }
            } else {
                when (state.selectedTab) {
                    ActivityTab.INBOX -> {
                        if (state.inboxEvents.isEmpty()) {
                            item {
                                EmptyState(message = "No new financial activity detected.")
                            }
                        }
                    }
                    else -> {
                        if (state.transactions.isEmpty()) {
                            item {
                                EmptyState(message = "No transactions found for this period.")
                            }
                        } else {
                            items(state.transactions, key = { it.id }) { transaction ->
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
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivitySegmentedControl(
    selectedTab: ActivityTab,
    onTabSelected: (ActivityTab) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        ActivityTab.entries.forEachIndexed { index, tab ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = ActivityTab.entries.size),
                onClick = { onTabSelected(tab) },
                selected = tab == selectedTab,
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.onSurface,
                    activeContentColor = MaterialTheme.colorScheme.surface,
                    inactiveContainerColor = Color.Transparent,
                    inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(tab.name.lowercase().replaceFirstChar { it.uppercase() })
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
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 48.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
