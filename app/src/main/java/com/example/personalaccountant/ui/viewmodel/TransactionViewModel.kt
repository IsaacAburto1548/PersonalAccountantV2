package com.example.personalaccountant.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.personalaccountant.data.Account
import com.example.personalaccountant.data.Transaction
import com.example.personalaccountant.data.repository.FinanceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

class TransactionViewModel(private val repository: FinanceRepository) : ViewModel() {

    val accounts = repository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions = repository.recentTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTransaction(
        amount: Double,
        description: String,
        category: String,
        type: String, // "INCOME" or "EXPENSE"
        accountId: Int
    ) {
        viewModelScope.launch {
            val transaction = Transaction(
                type = type,
                amount = amount,
                date = System.currentTimeMillis(),
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
        }
    }

    suspend fun getTransaction(id: Int): Transaction? {
        return repository.getTransactionById(id)
    }
    
    fun addAccount(name: String, initialBalance: Double, type: String) {
        viewModelScope.launch {
            repository.addAccount(Account(name = name, currentBalance = initialBalance, type = type))
        }
    }
}

class TransactionViewModelFactory(private val repository: FinanceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
