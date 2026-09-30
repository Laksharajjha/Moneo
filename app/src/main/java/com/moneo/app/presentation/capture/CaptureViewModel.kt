package com.moneo.app.presentation.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneo.app.ai.model.ParsedTransaction
import com.moneo.app.ai.parser.DeterministicParser
import com.moneo.app.ai.validation.TransactionValidator
import com.moneo.app.domain.usecase.AddTransactionUseCase
import com.moneo.app.ai.speech.VoiceRecognizer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CaptureViewModel @Inject constructor(
    private val parser: DeterministicParser,
    private val addTransaction: AddTransactionUseCase,
    private val voiceRecognizer: VoiceRecognizer
) : ViewModel() {

    private val _uiState = MutableStateFlow(CaptureUiState())
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            voiceRecognizer.state.collect { voiceState ->
                if (voiceState.isListening) {
                    _uiState.update { 
                        it.copy(
                            step = CaptureStep.LISTENING,
                            inputText = voiceState.partialText.ifEmpty { it.inputText },
                            errorMessage = null
                        ) 
                    }
                } else if (_uiState.value.step == CaptureStep.LISTENING) {
                    if (voiceState.error != null) {
                        _uiState.update { it.copy(step = CaptureStep.ERROR, errorMessage = voiceState.error) }
                    } else if (voiceState.finalText.isNotBlank()) {
                        processText(voiceState.finalText)
                    } else {
                        // Ended without result
                        _uiState.update { it.copy(step = CaptureStep.IDLE) }
                    }
                }
            }
        }
    }

    fun startListening() {
        voiceRecognizer.startListening()
    }

    fun stopListening() {
        voiceRecognizer.stopListening()
    }

    fun onTextInput(text: String) {
        _uiState.update { it.copy(inputText = text, errorMessage = null) }
    }

    fun processText(text: String = _uiState.value.inputText) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(step = CaptureStep.PROCESSING, inputText = text) }
            val result = parser.parse(text)
            when {
                !result.isSuccess -> _uiState.update {
                    it.copy(
                        step = CaptureStep.ERROR,
                        errorMessage = result.parseError ?: "Couldn't understand that. Try: \"Spent ₹250 on lunch\""
                    )
                }
                result.requiresConfirmation -> _uiState.update {
                    it.copy(step = CaptureStep.CONFIRMING, parseResult = result)
                }
                else -> confirmAndSave(result.transactions)
            }
        }
    }

    fun confirmAndSave(transactions: List<ParsedTransaction> = _uiState.value.parseResult?.transactions ?: emptyList()) {
        if (transactions.isEmpty()) return
        viewModelScope.launch {
            var lastId: Long? = null
            var hasError = false
            for (parsed in transactions) {
                when (val validation = TransactionValidator.validate(parsed)) {
                    is TransactionValidator.ValidationResult.Valid -> {
                        val result = addTransaction(validation.transaction)
                        result.onSuccess { lastId = it }
                        result.onFailure { hasError = true }
                    }
                    is TransactionValidator.ValidationResult.Invalid -> {
                        hasError = true
                        _uiState.update { it.copy(step = CaptureStep.ERROR, errorMessage = validation.reason) }
                        return@launch
                    }
                }
            }
            if (!hasError) {
                _uiState.update { it.copy(step = CaptureStep.SAVED, savedTransactionId = lastId) }
            }
        }
    }

    fun reset() {
        _uiState.value = CaptureUiState()
    }
}
