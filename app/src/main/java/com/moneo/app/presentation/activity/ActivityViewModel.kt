package com.moneo.app.presentation.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneo.app.data.local.dao.InboxEventDao
import com.moneo.app.data.local.entity.InboxEventEntity
import com.moneo.app.domain.model.TimePeriod
import com.moneo.app.ai.parser.TransactionParser
import com.moneo.app.domain.model.Transaction
import com.moneo.app.domain.usecase.DeleteTransactionUseCase
import com.moneo.app.domain.usecase.GetTransactionsUseCase
import com.moneo.app.domain.usecase.AddTransactionUseCase
import com.moneo.app.domain.model.Category
import com.moneo.app.domain.model.TransactionType
import com.moneo.app.domain.model.TransactionSource
import java.time.LocalDate
import java.time.LocalDateTime

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ActivityTab {
    ALL, RECORDED, INBOX
}

data class ActivityUiState(
    val selectedTab: ActivityTab = ActivityTab.RECORDED,
    val selectedPeriod: TimePeriod = TimePeriod.MONTH,
    val transactions: List<Transaction> = emptyList(),
    val inboxEvents: List<InboxEventEntity> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val getTransactions: GetTransactionsUseCase,
    private val deleteTransaction: DeleteTransactionUseCase,
    private val addTransaction: AddTransactionUseCase,
    private val inboxEventDao: InboxEventDao,
    private val transactionParser: TransactionParser
) : ViewModel() {

    // ... (unchanged parts)
    
    private val _selectedTab = MutableStateFlow(ActivityTab.RECORDED)
    private val _selectedPeriod = MutableStateFlow(TimePeriod.MONTH)
    
    private val _inboxEvents = inboxEventDao.getAllEvents()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ActivityUiState> = combine(
        _selectedTab,
        _selectedPeriod,
        _inboxEvents
    ) { tab, period, inbox ->
        Triple(tab, period, inbox)
    }.flatMapLatest { (tab, period, inbox) ->
        getTransactions(period).map { transactions ->
            ActivityUiState(
                selectedTab = tab,
                selectedPeriod = period,
                transactions = transactions,
                inboxEvents = inbox,
                isLoading = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = ActivityUiState(isLoading = true)
    )

    fun selectTab(tab: ActivityTab) {
        _selectedTab.value = tab
    }

    fun selectPeriod(period: TimePeriod) {
        _selectedPeriod.value = period
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            deleteTransaction.invoke(id)
        }
    }

    fun approveInboxEvent(event: InboxEventEntity) {
        viewModelScope.launch {
            val now = LocalDateTime.now()
            val transaction = Transaction(
                amountInPaise = event.amountInPaise,
                currency = event.currency,
                type = TransactionType.valueOf(event.type),
                category = Category.valueOf(event.category),
                merchant = event.merchant,
                description = "From: ${event.sourcePackage}",
                date = LocalDate.parse(event.date),
                timestamp = now,
                person = null,
                account = null,
                toAccount = null,
                isRecurring = false,
                billingCycle = null,
                source = TransactionSource.TEXT, // Or NOTIFICATION
                confidence = event.confidence,
                createdAt = now,
                updatedAt = now
            )
            addTransaction(transaction)
            inboxEventDao.deleteEvent(event.id)
        }
    }

    fun dismissInboxEvent(eventId: Long) {
        viewModelScope.launch {
            inboxEventDao.deleteEvent(eventId)
        }
    }

    fun importMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val result = transactionParser.parse(text)
            if (result.transactions.isNotEmpty()) {
                val tx = result.transactions.first()
                val event = InboxEventEntity(
                    rawText = text,
                    sourcePackage = "Manual Import",
                    timestamp = System.currentTimeMillis(),
                    amountInPaise = tx.amountInPaise,
                    currency = tx.currency,
                    type = tx.type.name,
                    merchant = tx.merchant,
                    category = tx.category.name,
                    date = tx.date.toString(),
                    confidence = tx.confidence
                )
                inboxEventDao.insertEvent(event)
            }
        }
    }
}
