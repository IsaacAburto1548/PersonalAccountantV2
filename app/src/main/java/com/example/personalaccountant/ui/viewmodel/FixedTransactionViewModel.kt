package com.example.personalaccountant.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personalaccountant.data.FixedTransactionRule
import com.example.personalaccountant.data.repository.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import com.example.personalaccountant.data.prefs.PreferenceManager
import kotlinx.coroutines.flow.map
import javax.inject.Inject

sealed class FixedTransactionUiEvent {
    data class ShowSnackbar(val message: String) : FixedTransactionUiEvent()
}

@HiltViewModel
class FixedTransactionViewModel @Inject constructor(
    private val repository: FinanceRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiEvent = Channel<FixedTransactionUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    val upcomingPayments = repository.upcomingFixedPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val fixedRules = repository.allFixedRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accounts = repository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val defaultCategories = listOf("Salarios", "Gastos fijos", "Sinpe Movil", "Gastos Personales", "Mascotas", "Hogar", "Entretenimiento")

    val categories: kotlinx.coroutines.flow.StateFlow<List<String>> = preferenceManager.customCategories
        .map { custom -> (defaultCategories + custom).distinct() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultCategories)

    fun addCustomCategory(category: String) {
        preferenceManager.addCustomCategory(category)
    }

    fun addRule(rule: FixedTransactionRule) {
        viewModelScope.launch { repository.createFixedRule(rule) }
    }

    fun updateRule(rule: FixedTransactionRule) {
        viewModelScope.launch { repository.updateFixedRule(rule) }
    }

    fun deleteRule(id: Int) {
        viewModelScope.launch { repository.deleteFixedRule(id) }
    }
}
