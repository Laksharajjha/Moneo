package com.moneo.app.presentation.capture

import com.moneo.app.ai.model.ParseResult

enum class CaptureStep {
    IDLE,
    LISTENING,
    PROCESSING,
    CONFIRMING,
    SAVED,
    ERROR
}

data class CaptureUiState(
    val step: CaptureStep = CaptureStep.IDLE,
    val inputText: String = "",
    val parseResult: ParseResult? = null,
    val errorMessage: String? = null,
    val savedTransactionId: Long? = null
)
