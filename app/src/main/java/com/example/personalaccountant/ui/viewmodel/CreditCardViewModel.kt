package com.example.personalaccountant.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personalaccountant.data.Account
import com.example.personalaccountant.data.CreditCardCharge
import com.example.personalaccountant.data.repository.FinanceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CreditCardViewModel(private val repository: FinanceRepository) : ViewModel() {
    
    // State flows for UI
    val allCharges: StateFlow<List<CreditCardCharge>> = repository.allCreditCardCharges
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val pendingCharges: StateFlow<List<CreditCardCharge>> = repository.pendingCreditCardCharges
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val totalPendingBalance: StateFlow<Double> = repository.totalPendingCreditCardBalance
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )
    
    val accounts: StateFlow<List<Account>> = repository.allAccounts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Add a new credit card charge
    fun addCharge(amount: Double, description: String, purchaseDate: Long) {
        viewModelScope.launch {
            repository.addCreditCardCharge(amount, description, purchaseDate)
        }
    }
    
    // Make a payment towards credit card balance
    fun makePayment(amount: Double, accountId: Int, description: String = "Abono Tarjeta de Crédito") {
        viewModelScope.launch {
            repository.makeCreditCardPayment(amount, accountId, description)
        }
    }
    
    // Delete a charge
    fun deleteCharge(charge: CreditCardCharge) {
        viewModelScope.launch {
            repository.deleteCreditCardCharge(charge)
        }
    }
    
    // Get billing cycle info for display
    fun getBillingCycleInfo(purchaseDate: Long): Pair<Long, Long> {
        return repository.calculateBillingCycle(purchaseDate)
    }
    
    
    // Get payment due date for display
    fun getPaymentDueDate(billingCycleEnd: Long): Long {
        return repository.calculatePaymentDueDate(billingCycleEnd)
    }
    
    // Refresh and generate charges from fixed rules
    fun refreshCharges() {
        viewModelScope.launch {
            repository.generateFixedCreditCardCharges()
        }
    }
    
    // Initialize - generate charges on creation
    init {
        refreshCharges()
    }
}
