package com.moneo.app.presentation.ask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneo.app.domain.model.Category
import com.moneo.app.domain.model.Transaction
import com.moneo.app.domain.model.TransactionType
import com.moneo.app.domain.usecase.GetTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ChatMessage(val text: String, val isUser: Boolean)

@HiltViewModel
class AskMoneoViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage("Hi! Ask me anything about your finances.", false))
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    fun sendMessage(query: String) {
        val userMessage = ChatMessage(query, true)
        _messages.value = _messages.value + userMessage
        
        viewModelScope.launch {
            val transactions = getTransactionsUseCase.all().first()
            val answer = processQuery(query, transactions)
            _messages.value = _messages.value + ChatMessage(answer, false)
        }
    }

    private fun processQuery(query: String, transactions: List<Transaction>): String {
        val q = query.lowercase()
        return when {
            q.contains("spend this month") -> {
                val currentMonth = LocalDate.now().month
                val currentYear = LocalDate.now().year
                val sum = transactions.filter {
                    it.type == TransactionType.EXPENSE &&
                    it.date.month == currentMonth &&
                    it.date.year == currentYear
                }.sumOf { it.amountInRupees }
                "You spent ₹${String.format("%.2f", sum)} this month."
            }
            q.contains("spend on food") -> {
                val sum = transactions.filter {
                    it.type == TransactionType.EXPENSE && it.category == Category.FOOD
                }.sumOf { it.amountInRupees }
                "You spent ₹${String.format("%.2f", sum)} on food."
            }
            q.contains("owe me") -> {
                val words = q.split(" ")
                val doesIndex = words.indexOf("does")
                val oweIndex = words.indexOf("owe")
                
                val person = if (doesIndex != -1 && oweIndex != -1 && oweIndex > doesIndex + 1) {
                    words.subList(doesIndex + 1, oweIndex).joinToString(" ").replaceFirstChar { it.uppercase() }
                } else if (q.contains("rahul")) {
                    "Rahul"
                } else {
                    "They"
                }

                val sum = transactions.filter {
                    it.person?.equals(person, ignoreCase = true) == true || 
                    it.description?.contains(person, ignoreCase = true) == true
                }.fold(0.0) { acc, tx ->
                    val isLend = tx.type == TransactionType.EXPENSE || tx.description?.contains("lend", ignoreCase = true) == true
                    val isRepayment = tx.type == TransactionType.INCOME || tx.description?.contains("repayment", ignoreCase = true) == true
                    
                    if (isLend) acc + tx.amountInRupees
                    else if (isRepayment) acc - tx.amountInRupees
                    else acc
                }
                "$person owes you ₹${String.format("%.2f", sum)}."
            }
            q.contains("biggest expense") -> {
                val maxTx = transactions.filter { it.type == TransactionType.EXPENSE }
                    .maxByOrNull { it.amountInPaise }
                if (maxTx != null) {
                    "Your biggest expense was ₹${String.format("%.2f", maxTx.amountInRupees)} at ${maxTx.displayName} on ${maxTx.date}."
                } else {
                    "You have no expenses recorded."
                }
            }
            else -> "I'm not sure how to answer that yet. Try asking about your expenses this month, food expenses, your biggest expense, or how much someone owes you."
        }
    }
}
