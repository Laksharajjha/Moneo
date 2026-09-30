package com.moneo.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneo.app.domain.model.TimePeriod
import com.moneo.app.domain.usecase.GetTransactionSummaryUseCase
import com.moneo.app.domain.usecase.GetTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTransactions: GetTransactionsUseCase,
    private val getSummary: GetTransactionSummaryUseCase
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(TimePeriod.TODAY)
    val selectedPeriod: StateFlow<TimePeriod> = _selectedPeriod.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HomeUiState> = _selectedPeriod.flatMapLatest { period ->
        combine(
            getSummary(period),
            getTransactions(period)
        ) { summary, transactions ->
            HomeUiState(
                selectedPeriod = period,
                summary = summary,
                recentTransactions = transactions,
                isLoading = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = HomeUiState(isLoading = true)
    )

    fun selectPeriod(period: TimePeriod) {
        _selectedPeriod.value = period
    }
}
