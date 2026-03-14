package com.example.personalaccountant.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personalaccountant.data.Account
import com.example.personalaccountant.data.Transaction
import com.example.personalaccountant.data.repository.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import com.example.personalaccountant.utils.PdfExporter
import java.io.File
import java.util.Calendar
import javax.inject.Inject

sealed class DashboardUiEvent {
    data class ShowSnackbar(val message: String) : DashboardUiEvent()
    data class PdfGenerated(val file: File) : DashboardUiEvent()
}

data class SpendingCategory(val category: String, val amount: Double, val percentage: Float)

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(
        val totalBalance: Double,
        val accounts: List<Account>,
        val recentTransactions: List<Transaction>,
        val spendingByCategory: List<SpendingCategory>
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: FinanceRepository,
    private val pdfExporter: PdfExporter
) : ViewModel() {

    private val _uiEvent = Channel<DashboardUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    val totalBalance: StateFlow<Double> = repository.totalBalance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val accounts: StateFlow<List<Account>> = repository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentTransactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val spendingByCategory: StateFlow<List<SpendingCategory>> = repository.allTransactions
        .map { transactions ->
            val expenses = transactions.filter { it.type == "EXPENSE" }
            val total = expenses.sumOf { it.amount }
            if (total == 0.0) return@map emptyList()
            expenses
                .groupBy { it.category }
                .map { (category, txs) ->
                    val amount = txs.sumOf { it.amount }
                    SpendingCategory(category, amount, (amount / total).toFloat())
                }
                .sortedByDescending { it.amount }
                .take(5)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
    private val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    val chartData: StateFlow<Map<String, Double>> = repository.allTransactions.map { transactions ->
        val monthTxs = transactions.filter { tx ->
            val cal = Calendar.getInstance().apply { timeInMillis = tx.date }
            cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
        }
        val categoryTotals = monthTxs.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { tx -> tx.amount } }
        categoryTotals
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun exportMonthlyReport() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, java.util.Locale("es", "ES"))
            val year = calendar.get(Calendar.YEAR)
            
            val txs = repository.allTransactions.first() 
            val accs = repository.allAccounts.first()
            
            val file = pdfExporter.generateMonthlyReport(txs, accs, monthName ?: "Actual", year)
            if (file != null) {
                _uiEvent.send(DashboardUiEvent.PdfGenerated(file))
            } else {
                _uiEvent.send(DashboardUiEvent.ShowSnackbar("Error al generar el PDF"))
            }
        }
    }

    fun seedDataIfEmpty(
        onDone: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            // Seed logic moved here from MainActivity CoroutineScope
            if (repository.allAccounts.first().isEmpty()) {
                repository.addAccount(Account(name = "Efectivo", currentBalance = 0.0, type = "CASH"))
                repository.addAccount(Account(name = "Tarjeta", currentBalance = 0.0, type = "CARD"))
            }
            repository.generateFixedCreditCardCharges()
            onDone?.invoke()
        }
    }
}
