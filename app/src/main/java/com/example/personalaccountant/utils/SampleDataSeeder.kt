package com.example.personalaccountant.utils

import com.example.personalaccountant.data.Account
import com.example.personalaccountant.data.Transaction
import com.example.personalaccountant.data.FixedTransactionRule
import com.example.personalaccountant.data.repository.FinanceRepository
import java.util.Calendar
import javax.inject.Inject

class SampleDataSeeder @Inject constructor(private val repository: FinanceRepository) {

    suspend fun seedDemoData() {
        // Add Accounts first
        repository.addAccount(Account(name = "Efectivo", currentBalance = 50000.0, type = "CASH"))
        repository.addAccount(Account(name = "Cuenta Principal BAC", currentBalance = 450000.0, type = "CARD"))
        repository.addAccount(Account(name = "Ahorros Emergencia", currentBalance = 1000000.0, type = "SAVINGS"))
        
        val now = System.currentTimeMillis()
        
        // Add Transactions
        // We'll use hardcoded IDs 1 and 2 for simplicity, assuming they are the first ones created
        val sampleTransactions = listOf(
            Transaction(type = "INCOME", amount = 1200000.0, date = getPastDate(5), description = "Salario Mensual", category = "Salarios", accountId = 2),
            Transaction(type = "EXPENSE", amount = 35000.0, date = getPastDate(4), description = "Supermercado", category = "Hogar", accountId = 2),
            Transaction(type = "EXPENSE", amount = 15000.0, date = getPastDate(3), description = "Cena Familiar", category = "Entretenimiento", accountId = 1),
            Transaction(type = "EXPENSE", amount = 20000.0, date = getPastDate(2), description = "Gasolina", category = "Transporte", accountId = 2),
            Transaction(type = "EXPENSE", amount = 5000.0, date = getPastDate(1), description = "Café", category = "Gastos Personales", accountId = 1),
            Transaction(type = "EXPENSE", amount = 12000.0, date = now, description = "Suscripciones Digitales", category = "Entretenimiento", accountId = 2)
        )
        
        sampleTransactions.forEach { repository.addTransaction(it) }
        
        // Add Credit Card Charges
        repository.addCreditCardCharge(85000.0, "Compra en Amazon", getPastDate(10))
        repository.addCreditCardCharge(12000.0, "Netflix", getPastDate(15))
        
        // Add Fixed Rules
        repository.createFixedRule(
            FixedTransactionRule(
                type = "EXPENSE",
                baseAmount = 300000.0,
                dayOfMonth = 1,
                description = "Renta Mensual",
                accountId = 2,
                category = "Gastos fijos"
            )
        )
        repository.createFixedRule(
            FixedTransactionRule(
                type = "EXPENSE",
                baseAmount = 18000.0,
                dayOfMonth = 10,
                description = "Membresía Gimnasio",
                accountId = 2,
                category = "Entretenimiento"
            )
        )
    }
    
    private fun getPastDate(daysAgo: Int): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return cal.timeInMillis
    }
}
