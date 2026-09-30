package com.moneo.app.ai.engine

import android.content.Context
import android.util.Log
import com.google.ai.edge.aicore.GenerativeModel
import com.google.ai.edge.aicore.generationConfig
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.moneo.app.ai.model.ParseResult
import com.moneo.app.ai.model.ParsedTransaction
import com.moneo.app.domain.model.Category
import com.moneo.app.domain.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class GeminiNanoEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) : LocalAiEngine {

    private val TAG = "GeminiNanoEngine"

    private val systemPrompt = """
        You are the language understanding component of Moneo, a private offline financial assistant.
        Extract financial events from user text or notification text.
        Return ONLY valid structured JSON. Never invent missing financial facts. Never calculate balances. Never perform financial operations.

        Classify type as exactly one of: EXPENSE, INCOME, TRANSFER, REFUND, NON_FINANCIAL, UNKNOWN.
        IMPORTANT TRANSFER RULE: A transfer is money moving between accounts (e.g. "Transferred 2000 from Cash to HDFC"). This MUST be classified as TRANSFER, NEVER as EXPENSE.
        
        Amounts must be in MINOR units (e.g. 340.00 -> 34000).
        
        Output format:
        {
          "events": [
            {
              "type": "EXPENSE",
              "amountMinor": 34000,
              "currency": "INR",
              "merchant": "Uber",
              "category": "TRANSPORT",
              "sourceAccount": null,
              "destinationAccount": null,
              "paymentMethod": null,
              "date": "2026-10-01",
              "description": "Uber ride",
              "confidence": 0.94
            }
          ]
        }
    """.trimIndent()

    override suspend fun isAvailable(): Boolean {
        return try {
            // Safe check: If the device/OS doesn't have the class, it fails cleanly.
            Class.forName("com.google.ai.edge.aicore.GenerativeModel")
            true
        } catch (e: Exception) {
            Log.w(TAG, "AICore / Gemini Nano not available on this device", e)
            false
        }
    }

    override suspend fun extractFinancialEvent(input: String): ParseResult = withContext(Dispatchers.IO) {
        if (!isAvailable()) {
            return@withContext ParseResult(emptyList(), input, 0f, "Local AI unavailable on this device")
        }

        try {
            val config = generationConfig {
                temperature = 0.1f
                topK = 16
            }

            val model = GenerativeModel(
                generationConfig = config
            )

            val prompt = "$systemPrompt\n\nUser input: \"$input\""
            
            val response = model.generateContent(prompt)
            val jsonText = response.text?.replace("```json", "")?.replace("```", "")?.trim() 
                ?: return@withContext ParseResult(emptyList(), input, 0f, "Empty response from AI")
            
            val result = gson.fromJson(jsonText, AiExtractionResponse::class.java)
            
            val transactions = result.events.mapNotNull { it.toParsedTransaction() }
            if (transactions.isEmpty()) {
                ParseResult(emptyList(), input, 0f, "No valid financial events found")
            } else {
                ParseResult(transactions, input, transactions.first().confidence, null)
            }
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Failed to parse JSON from AI", e)
            ParseResult(emptyList(), input, 0f, "AI produced invalid JSON")
        } catch (e: Exception) {
            Log.e(TAG, "AI inference failed", e)
            ParseResult(emptyList(), input, 0f, "AI inference failed: ${e.message}")
        }
    }

    override suspend fun interpretQuery(query: String): QueryIntent = withContext(Dispatchers.IO) {
        if (!isAvailable()) {
            return@withContext QueryIntent("UNKNOWN", null, null)
        }

        try {
            val queryPrompt = """
                You are Moneo's query interpreter. Map the user's natural language question to a structured intent.
                Output ONLY JSON:
                {
                  "intent": "SPENDING_SUMMARY", // or "LARGEST_EXPENSE", "SUBSCRIPTIONS"
                  "category": "FOOD", // or null
                  "period": "THIS_MONTH" // or "LAST_MONTH", "THIS_WEEK", "TODAY", "ALL_TIME"
                }
                
                Question: "$query"
            """.trimIndent()
            
            val config = generationConfig {
                temperature = 0.1f
            }
            val model = GenerativeModel(generationConfig = config)
            val response = model.generateContent(queryPrompt)
            val jsonText = response.text?.replace("```json", "")?.replace("```", "")?.trim() ?: ""
            
            gson.fromJson(jsonText, QueryIntent::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to interpret query", e)
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
