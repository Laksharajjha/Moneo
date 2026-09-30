package com.moneo.app.presentation.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneo.app.ai.model.ParsedFinancialEvent
import com.moneo.app.ai.parser.LocalMessageParser
import com.moneo.app.ai.validation.TransactionValidator
import com.moneo.app.data.local.dao.TransactionDao
import com.moneo.app.domain.usecase.AddTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class InboxUiState(
    val inputText: String = "",
    val isProcessing: Boolean = false,
    val parsedEvents: List<ParsedFinancialEvent> = emptyList(),
    val step: InboxStep = InboxStep.INPUT
)

enum class InboxStep {
    INPUT, REVIEW
}

@HiltViewModel
class InboxViewModel @Inject constructor(
    private val parser: LocalMessageParser,
    private val transactionDao: TransactionDao,
    private val addTransaction: AddTransactionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(InboxUiState())
    val uiState: StateFlow<InboxUiState> = _uiState.asStateFlow()

    fun onInputTextChange(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun analyzeMessages() {
        val text = _uiState.value.inputText
        if (text.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            
            // Split by lines or double newlines to treat as separate messages
            val messages = text.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
            
            val events = mutableListOf<ParsedFinancialEvent>()
            val recentTxs = transactionDao.getAllTransactions().first().filter { 
                it.date == LocalDate.now().toString() || it.date == LocalDate.now().minusDays(1).toString()
            }

            for (msg in messages) {
                val parsed = parser.parseMessage(msg)
                if (parsed.isFinancial && parsed.transaction != null) {
                    val tx = parsed.transaction
                    // Simple duplicate check: same amount and type within last 48 hours
                    val isDuplicate = recentTxs.any {
                        it.amountInPaise == tx.amountInPaise && it.type == tx.type.name
                    }
                    events.add(parsed.copy(isDuplicate = isDuplicate))
                }
            }

            _uiState.update { 
                it.copy(
                    isProcessing = false, 
                    parsedEvents = events,
                    step = InboxStep.REVIEW
                ) 
            }
        }
    }

    fun addSelectedTransactions(eventsToKeep: List<ParsedFinancialEvent>) {
        viewModelScope.launch {
            eventsToKeep.forEach { event ->
                event.transaction?.let { parsed ->
                    val validation = TransactionValidator.validate(parsed)
                    if (validation is TransactionValidator.ValidationResult.Valid) {
                        addTransaction(validation.transaction)
                    }
                }
            }
            _uiState.update { 
                it.copy(
                    inputText = "",
                    parsedEvents = emptyList(),
                    step = InboxStep.INPUT
                ) 
            }
        }
    }

    fun reset() {
        _uiState.update { it.copy(step = InboxStep.INPUT, parsedEvents = emptyList()) }
    }
}
