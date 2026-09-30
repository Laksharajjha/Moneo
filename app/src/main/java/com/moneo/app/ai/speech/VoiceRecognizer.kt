package com.moneo.app.ai.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

data class VoiceState(
    val isListening: Boolean = false,
    val partialText: String = "",
    val finalText: String = "",
    val error: String? = null
)

class VoiceRecognizer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var recognizer: SpeechRecognizer? = null
    
    private val _state = MutableStateFlow(VoiceState())
    val state: StateFlow<VoiceState> = _state.asStateFlow()

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _state.update { it.copy(error = "Speech recognition not available on this device", isListening = false) }
            return
        }

        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _state.update { it.copy(isListening = true, partialText = "", finalText = "", error = null) }
                }

                override fun onBeginningOfSpeech() {}
                
                override fun onRmsChanged(rmsdB: Float) {}
                
                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _state.update { it.copy(isListening = false) }
                }

                override fun onError(error: Int) {
                    val message = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                        SpeechRecognizer.ERROR_NETWORK -> "Network error"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No recognition result matched"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
                        SpeechRecognizer.ERROR_SERVER -> "Error from server"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                        else -> "Didn't catch that"
                    }
                    _state.update { it.copy(isListening = false, error = message) }
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        _state.update { it.copy(finalText = matches[0], isListening = false) }
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        _state.update { it.copy(partialText = matches[0]) }
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        recognizer?.startListening(intent)
    }

    fun stopListening() {
        recognizer?.stopListening()
        _state.update { it.copy(isListening = false) }
    }

    fun destroy() {
        recognizer?.destroy()
        recognizer = null
    }
}
