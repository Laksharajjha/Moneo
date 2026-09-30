package com.moneo.app.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneo.app.domain.model.Transaction
import com.moneo.app.domain.usecase.GetTransactionUseCase
import com.moneo.app.domain.usecase.DeleteTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTransaction: GetTransactionUseCase,
    private val deleteTransaction: DeleteTransactionUseCase
) : ViewModel() {

    private val transactionId: String = checkNotNull(savedStateHandle["id"])

    val transaction: StateFlow<Transaction?> = flow {
        emitAll(getTransaction(transactionId.toLong()))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = null
    )

    fun delete() {
        viewModelScope.launch {
            deleteTransaction(transactionId.toLong())
        }
    }
}
