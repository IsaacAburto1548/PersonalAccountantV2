package com.example.personalaccountant.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.personalaccountant.data.FixedTransactionRule
import com.example.personalaccountant.data.repository.FinanceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FixedTransactionViewModel(private val repository: FinanceRepository) : ViewModel() {

    val upcomingPayments = repository.upcomingFixedPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val fixedRules = repository.allFixedRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accounts = repository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addRule(rule: FixedTransactionRule) {
        viewModelScope.launch {
            repository.createFixedRule(rule)
        }
    }

    fun updateRule(rule: FixedTransactionRule) {
        viewModelScope.launch {
            repository.updateFixedRule(rule)
        }
    }

    fun deleteRule(id: Int) {
        viewModelScope.launch {
            repository.deleteFixedRule(id)
        }
    }
}

class FixedTransactionViewModelFactory(private val repository: FinanceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FixedTransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FixedTransactionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
