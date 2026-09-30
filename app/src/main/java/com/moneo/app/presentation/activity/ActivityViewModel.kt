package com.moneo.app.presentation.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneo.app.domain.model.TimePeriod
import com.moneo.app.domain.model.Transaction
import com.moneo.app.domain.usecase.DeleteTransactionUseCase
import com.moneo.app.domain.usecase.GetTransactionsUseCase
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
    val inboxEvents: List<Any> = emptyList(), // We will replace Any with ParsedFinancialEvent or InboxEvent later
    val isLoading: Boolean = false
)

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val getTransactions: GetTransactionsUseCase,
    private val deleteTransaction: DeleteTransactionUseCase
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(ActivityTab.RECORDED)
    private val _selectedPeriod = MutableStateFlow(TimePeriod.MONTH)
    
    // We will populate inbox events properly in Step 5
    private val _inboxEvents = MutableStateFlow<List<Any>>(emptyList())

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
}
