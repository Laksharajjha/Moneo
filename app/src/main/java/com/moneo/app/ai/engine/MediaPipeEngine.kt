package com.moneo.app.ai.engine

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.moneo.app.ai.model.ParseResult
import com.moneo.app.ai.model.ParsedTransaction
import com.moneo.app.domain.model.Category
import com.moneo.app.domain.model.TransactionType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaPipeEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) : LocalAiEngine {

    private val TAG = "MediaPipeEngine"

    // The user needs to push a model like 'gemma-2b-it-gpu-int4.bin' to this path
    // We use getExternalFilesDir so it's accessible without root via file explorer or ADB
    private val modelFile = File(context.getExternalFilesDir(null), "llm/gemma.bin")

    private var llmInference: LlmInference? = null

    private val systemPrompt = """
        You are Moneo, a private offline financial assistant.
        Extract financial events from the user input into strict JSON.
        Classify type as one of: EXPENSE, INCOME, TRANSFER, REFUND, NON_FINANCIAL, UNKNOWN.
        Transfers are money moving between accounts (e.g., Cash to HDFC). Must be TRANSFER.
        Amounts in MINOR units (e.g. 340.00 -> 34000).
        
        Output format exactly:
        {
          "events": [
            {
              "type": "EXPENSE",
              "amountMinor": 34000,
              "currency": "INR",
              "merchant": "Uber",
              "category": "TRANSPORT",
              "date": "2026-10-01",
              "confidence": 0.94
            }
          ]
        }
    """.trimIndent()

    override suspend fun isAvailable(): Boolean = withContext(Dispatchers.IO) {
        if (!modelFile.exists()) {
            Log.w(TAG, "MediaPipe Model not found at: ${modelFile.absolutePath}")
            return@withContext false
        }
        
        if (llmInference == null) {
            try {
                val options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(modelFile.absolutePath)
                    .setMaxTokens(512)
                    .build()
                llmInference = LlmInference.createFromOptions(context, options)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize MediaPipe LlmInference", e)
                return@withContext false
            }
        }
        true
    }

    override suspend fun extractFinancialEvent(input: String): ParseResult = withContext(Dispatchers.IO) {
        if (!isAvailable()) {
            return@withContext ParseResult(emptyList(), input, 0f, "MediaPipe model unavailable")
        }

        try {
            // Gemma prompt formatting
            val prompt = "<start_of_turn>user\n$systemPrompt\n\nUser input: \"$input\"\nReturn JSON only.<end_of_turn>\n<start_of_turn>model\n"
            val response = llmInference?.generateResponse(prompt) ?: ""
            
            val jsonText = response.replace("```json", "").replace("```", "").trim()
            val result = gson.fromJson(jsonText, AiExtractionResponse::class.java)
            
            val transactions = result.events.mapNotNull { it.toParsedTransaction() }
            if (transactions.isEmpty()) {
                ParseResult(emptyList(), input, 0f, "No valid financial events found")
            } else {
                ParseResult(transactions, input, transactions.first().confidence, null)
            }
        } catch (e: JsonSyntaxException) {
            ParseResult(emptyList(), input, 0f, "AI produced invalid JSON")
        } catch (e: Exception) {
            Log.e(TAG, "MediaPipe inference failed", e)
            ParseResult(emptyList(), input, 0f, "Inference failed")
        }
    }

    override suspend fun interpretQuery(query: String): QueryIntent = withContext(Dispatchers.IO) {
        if (!isAvailable()) return@withContext QueryIntent("UNKNOWN", null, null)

        try {
            val queryPrompt = "<start_of_turn>user\nMap this question to intent JSON:\n{\"intent\":\"SPENDING_SUMMARY\",\"category\":\"FOOD\",\"period\":\"THIS_MONTH\"}\n\nQuestion: \"$query\"<end_of_turn>\n<start_of_turn>model\n"
            
            val response = llmInference?.generateResponse(queryPrompt) ?: ""
            val jsonText = response.replace("```json", "").replace("```", "").trim()
            
            gson.fromJson(jsonText, QueryIntent::class.java)
        } catch (e: Exception) {
            QueryIntent("UNKNOWN", null, null)
        }
    }

    private data class AiExtractionResponse(
        val events: List<AiEvent> = emptyList()
    )

    private data class AiEvent(
        val type: String,
        val amountMinor: Long?,
        val currency: String?,
        val merchant: String?,
        val category: String?,
        val sourceAccount: String?,
        val destinationAccount: String?,
        val paymentMethod: String?,
        val date: String?,
        val description: String?,
        val confidence: Float?
    ) {
        fun toParsedTransaction(): ParsedTransaction? {
            if (amountMinor == null || amountMinor <= 0) return null
            val txType = runCatching { TransactionType.valueOf(type.uppercase()) }.getOrElse { return null }
            val txCategory = runCatching { Category.valueOf(category?.uppercase() ?: "OTHER") }.getOrElse { Category.OTHER }
            val txDate = runCatching { LocalDate.parse(date ?: "") }.getOrElse { LocalDate.now() }
            
            return ParsedTransaction(
                type = txType,
                amountInPaise = amountMinor,
                currency = currency ?: "INR",
                merchant = merchant,
                category = txCategory,
                date = txDate,
                description = description,
                account = sourceAccount ?: paymentMethod,
                toAccount = destinationAccount,
                confidence = confidence ?: 0.5f
            )
        }
    }
}
