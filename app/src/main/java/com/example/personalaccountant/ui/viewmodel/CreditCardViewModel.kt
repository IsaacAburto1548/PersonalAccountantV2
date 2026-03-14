package com.example.personalaccountant.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personalaccountant.data.Account
import com.example.personalaccountant.data.CreditCardCharge
import com.example.personalaccountant.data.repository.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CreditCardUiEvent {
    data class ShowSnackbar(val message: String, val deletedCharge: CreditCardCharge?) : CreditCardUiEvent()
}

@HiltViewModel
class CreditCardViewModel @Inject constructor(
    private val repository: FinanceRepository
) : ViewModel() {

    val allCharges: StateFlow<List<CreditCardCharge>> = repository.allCreditCardCharges
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingCharges: StateFlow<List<CreditCardCharge>> = repository.pendingCreditCardCharges
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalPendingBalance: StateFlow<Double> = repository.totalPendingCreditCardBalance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val accounts: StateFlow<List<Account>> = repository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiEvent = Channel<CreditCardUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun addCharge(amount: Double, description: String, purchaseDate: Long) {
        viewModelScope.launch {
            repository.addCreditCardCharge(amount, description, purchaseDate)
        }
    }

    fun makePayment(amount: Double, accountId: Int, description: String = "Abono Tarjeta de Crédito") {
        viewModelScope.launch {
            repository.makeCreditCardPayment(amount, accountId, description)
        }
    }

    fun deleteCharge(charge: CreditCardCharge) {
        viewModelScope.launch {
            repository.deleteCreditCardCharge(charge)
            _uiEvent.send(CreditCardUiEvent.ShowSnackbar("Cargo eliminado", charge))
        }
    }

    fun undoDeleteCharge(charge: CreditCardCharge) {
        viewModelScope.launch {
            repository.addCreditCardCharge(charge.amount, charge.description, charge.purchaseDate)
        }
    }

    fun getBillingCycleInfo(purchaseDate: Long): Pair<Long, Long> =
        repository.calculateBillingCycle(purchaseDate)

    fun getPaymentDueDate(billingCycleEnd: Long): Long =
        repository.calculatePaymentDueDate(billingCycleEnd)

    fun refreshCharges() {
        viewModelScope.launch {
            repository.generateFixedCreditCardCharges()
        }
    }

    init {
        refreshCharges()
    }
}
