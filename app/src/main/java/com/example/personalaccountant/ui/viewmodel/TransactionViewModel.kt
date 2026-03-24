package com.example.personalaccountant.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personalaccountant.data.Account
import com.example.personalaccountant.data.Transaction
import com.example.personalaccountant.data.repository.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import com.example.personalaccountant.data.prefs.PreferenceManager
import kotlinx.coroutines.flow.map
import javax.inject.Inject

sealed class TransactionUiEvent {
    data class ShowSnackbar(val message: String, val deletedTx: Transaction?) : TransactionUiEvent()
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val repository: FinanceRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    val accounts: StateFlow<List<Account>> = repository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search query
    val searchQuery = MutableStateFlow("")

    private val defaultCategories = listOf("Salarios", "Ingresos Personales", "Gastos fijos", "Sinpe Movil", "Gastos Personales", "Mascotas", "Hogar", "Entretenimiento")
    
    val categories: StateFlow<List<String>> = preferenceManager.customCategories
        .map { custom -> (defaultCategories + custom).distinct() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultCategories)

    fun addCustomCategory(category: String) {
        preferenceManager.addCustomCategory(category)
    }

    // Selected month: null = all time
    private val _selectedMonth = MutableStateFlow<Pair<Int, Int>?>(null) // (month, year)
    val selectedMonth: StateFlow<Pair<Int, Int>?> = _selectedMonth

    val transactions: StateFlow<List<Transaction>> = combine<List<Transaction>, String, Pair<Int, Int>?, List<Transaction>>(
        repository.allTransactions,
        searchQuery,
        _selectedMonth
    ) { txs, query, month ->
        var filtered = txs
        if (query.isNotBlank()) {
            filtered = filtered.filter { tx: Transaction ->
                tx.description.contains(query, ignoreCase = true) ||
                tx.category.contains(query, ignoreCase = true)
            }
        }
        if (month != null) {
            filtered = filtered.filter { tx: Transaction ->
                val cal = Calendar.getInstance().apply { timeInMillis = tx.date }
                cal.get(Calendar.MONTH) == month.first && cal.get(Calendar.YEAR) == month.second
            }
        }
        filtered
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiEvent = Channel<TransactionUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun setSearchQuery(query: String) { searchQuery.value = query }
    fun setSelectedMonth(month: Int, year: Int) { _selectedMonth.value = Pair(month, year) }
    fun clearMonthFilter() { _selectedMonth.value = null }

    fun addTransaction(
        amount: Double,
        description: String,
        category: String,
        type: String,
        accountId: Int,
        date: Long
    ) {
        viewModelScope.launch {
            val transaction = Transaction(
                type = type,
                amount = amount,
                date = date,
                description = description,
                category = category,
                accountId = accountId
            )
            repository.addTransaction(transaction)
        }
    }

    fun updateTransaction(
        id: Int,
        amount: Double,
        description: String,
        category: String,
        type: String,
        accountId: Int,
        originalTransaction: Transaction
    ) {
        viewModelScope.launch {
            val newTransaction = originalTransaction.copy(
                amount = amount,
                description = description,
                category = category,
                type = type,
                accountId = accountId
            )
            repository.updateTransaction(originalTransaction, newTransaction)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            _uiEvent.send(TransactionUiEvent.ShowSnackbar("Transacción eliminada", transaction))
        }
    }

    fun undoDeleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.addTransaction(transaction)
        }
    }

    suspend fun getTransaction(id: Int): Transaction? = repository.getTransactionById(id)

    fun addAccount(name: String, initialBalance: Double, type: String) {
        viewModelScope.launch {
            repository.addAccount(Account(name = name, currentBalance = initialBalance, type = type))
        }
    }
}
