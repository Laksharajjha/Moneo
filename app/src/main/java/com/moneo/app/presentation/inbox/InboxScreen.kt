package com.moneo.app.presentation.inbox

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.moneo.app.ai.model.ParsedFinancialEvent
import com.moneo.app.ai.parser.AmountParser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    viewModel: InboxViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(28.dp)
    ) {
        Text(
            text = "Inbox",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Paste financial SMS messages here to bulk track them.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))

        AnimatedContent(
            targetState = state.step,
            label = "InboxStep"
        ) { step ->
            when (step) {
                InboxStep.INPUT -> {
                    Column(Modifier.fillMaxSize()) {
                        OutlinedTextField(
                            value = state.inputText,
                            onValueChange = viewModel::onInputTextChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            placeholder = { Text("Paste messages here...\ne.g. ₹450 paid to Swiggy via UPI\n₹1200 debited from HDFC") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            shape = MaterialTheme.shapes.small
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.analyzeMessages() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = state.inputText.isNotBlank() && !state.isProcessing,
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onSurface,
                                contentColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            if (state.isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Analyze messages", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
                InboxStep.REVIEW -> {
                    ReviewSection(
                        events = state.parsedEvents,
                        onCancel = viewModel::reset,
                        onAdd = viewModel::addSelectedTransactions
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewSection(
    events: List<ParsedFinancialEvent>,
    onCancel: () -> Unit,
    onAdd: (List<ParsedFinancialEvent>) -> Unit
) {
    var selectedEvents by remember { mutableStateOf(events.toSet()) }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCancel) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "${events.size} financial transactions found",
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), thickness = 0.5.dp)
        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(events) { event ->
                val tx = event.transaction
                if (tx != null) {
                    val isSelected = selectedEvents.contains(event)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                        ),
                        onClick = {
                            if (isSelected) selectedEvents -= event else selectedEvents += event
                        },
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { 
                                    if (it) selectedEvents += event else selectedEvents -= event
                                }
                            )
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = AmountParser.formatPaise(tx.amountInPaise),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = tx.merchant ?: tx.type.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (event.isDuplicate) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "Possible duplicate",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), thickness = 0.5.dp)
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { onAdd(selectedEvents.toList()) },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedEvents.isNotEmpty(),
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onSurface,
                contentColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Text("Add ${selectedEvents.size} transactions", style = MaterialTheme.typography.titleMedium)
        }
    }
}
