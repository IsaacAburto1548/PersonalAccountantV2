package com.example.personalaccountant.data.repository

import androidx.room.withTransaction
import com.example.personalaccountant.data.Account
import com.example.personalaccountant.data.AccountDao
import com.example.personalaccountant.data.AppDatabase
import com.example.personalaccountant.data.CreditCardCharge
import com.example.personalaccountant.data.Transaction
import com.example.personalaccountant.data.TransactionDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar

class FinanceRepository(private val database: AppDatabase) {
    private val accountDao = database.accountDao()
    private val transactionDao = database.transactionDao()
    private val fixedTransactionDao = database.fixedTransactionDao()
    private val creditCardChargeDao = database.creditCardChargeDao()

    val allAccounts: Flow<List<Account>> = accountDao.getAllAccounts()
    
    // Calculate total balance from the stream of accounts
    val totalBalance: Flow<Double> = allAccounts.map { accounts ->
        accounts.sumOf { it.currentBalance }
    }

    val recentTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()

    suspend fun addTransaction(transaction: Transaction) {
        database.withTransaction {
            // 1. Insert Transaction
            transactionDao.insert(transaction)

            // 2. Update Account Balance
            val account = accountDao.getAccountById(transaction.accountId)
            if (account != null) {
                val newBalance = if (transaction.type == "INCOME") {
                    account.currentBalance + transaction.amount
                } else {
                    account.currentBalance - transaction.amount
                }
                accountDao.updateBalance(transaction.accountId, newBalance)
            }

            // 3. Check and update fixed payments
            checkAndUpdateFixedPayments(transaction)
        }
    }

    suspend fun getTransactionById(id: Int): Transaction? {
        return transactionDao.getTransactionById(id)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        database.withTransaction {
            // 1. Revert Balance
            val account = accountDao.getAccountById(transaction.accountId)
            if (account != null) {
                // If it was INCOME, we subtract. If EXPENSE, we add back.
                val newBalance = if (transaction.type == "INCOME") {
                    account.currentBalance - transaction.amount
                } else {
                    account.currentBalance + transaction.amount
                }
                accountDao.updateBalance(transaction.accountId, newBalance)
            }

            // 2. Clear fixed payment status if this was a matching payment
            checkAndClearFixedPayments(transaction)

            // 3. Delete Transaction
            transactionDao.delete(transaction)
        }
    }

    suspend fun updateTransaction(oldTransaction: Transaction, newTransaction: Transaction) {
        database.withTransaction {
            // 1. Revert old transaction effect
            val oldAccount = accountDao.getAccountById(oldTransaction.accountId)
            if (oldAccount != null) {
                val revertedBalance = if (oldTransaction.type == "INCOME") {
                    oldAccount.currentBalance - oldTransaction.amount
                } else {
                    oldAccount.currentBalance + oldTransaction.amount
                }
                accountDao.updateBalance(oldTransaction.accountId, revertedBalance)
            }

            // 2. Apply new transaction effect
            // Note: Account might be different if user changed it
            val newAccount = accountDao.getAccountById(newTransaction.accountId)
            if (newAccount != null) {
                val finalBalance = if (newTransaction.type == "INCOME") {
                    newAccount.currentBalance + newTransaction.amount
                } else {
                    newAccount.currentBalance - newTransaction.amount
                }
                accountDao.updateBalance(newTransaction.accountId, finalBalance)
            }

            // 3. Update Transaction Record
            transactionDao.update(newTransaction)
        }
    }
    
    suspend fun addAccount(account: Account) {
        accountDao.insert(account)
    }

    // Fixed Transaction Logic
    
    val upcomingFixedPayments: Flow<List<com.example.personalaccountant.data.MonthlyPaymentInstance>> = 
        fixedTransactionDao.getInstancesByDateRange(System.currentTimeMillis(), System.currentTimeMillis() + 2592000000L) // Next 30 days approx

    suspend fun payFixedInstance(instance: com.example.personalaccountant.data.MonthlyPaymentInstance, accountId: Int, description: String) {
        database.withTransaction {
            // 1. Create Transaction
            val transaction = Transaction(
                type = "EXPENSE",
                amount = instance.amount,
                date = System.currentTimeMillis(),
                description = description,
                category = "Fixed Payment",
                accountId = accountId
            )
            val transactionId = transactionDao.insert(transaction)

            // 2. Update Account Balance
            val account = accountDao.getAccountById(accountId)
            if (account != null) {
                val newBalance = account.currentBalance - instance.amount
                accountDao.updateBalance(accountId, newBalance)
            }
            
            // 3. Update Instance Status
            val updatedInstance = instance.copy(status = "PAID", transactionId = transactionId.toInt())
            fixedTransactionDao.updateInstance(updatedInstance)
        }
    }
    
    suspend fun createFixedRule(rule: com.example.personalaccountant.data.FixedTransactionRule) {
        val ruleId = fixedTransactionDao.insertRule(rule)
        // Generate next instance with the actual rule ID
        val instance = com.example.personalaccountant.data.MonthlyPaymentInstance(
            ruleId = ruleId.toInt(),
            dueDate = System.currentTimeMillis() + (rule.dayOfMonth * 86400000L), // Rough approx
            amount = rule.baseAmount,
            status = "PENDING"
        )
        fixedTransactionDao.insertInstance(instance)
    }

    // Fixed Payments CRUD
    val allFixedRules: Flow<List<com.example.personalaccountant.data.FixedTransactionRule>> = 
        fixedTransactionDao.getAllRules()

    suspend fun updateFixedRule(rule: com.example.personalaccountant.data.FixedTransactionRule) {
        fixedTransactionDao.updateRule(rule)
    }

    suspend fun deleteFixedRule(id: Int) {
        fixedTransactionDao.deleteRule(id)
    }

    // Automatic Payment Detection
    private suspend fun checkAndUpdateFixedPayments(transaction: Transaction) {
        // Get all fixed rules as a list
        val rulesList = fixedTransactionDao.getAllRules().first()
        
        rulesList.forEach { rule ->
            // Match by category and amount (within 5% tolerance)
            val amountMatch = kotlin.math.abs(transaction.amount - rule.baseAmount) <= (rule.baseAmount * 0.05)
            val categoryMatch = transaction.category.equals(rule.category, ignoreCase = true)
            
            if (amountMatch && categoryMatch && transaction.type == rule.type) {
                // Update last paid date
                fixedTransactionDao.updateLastPaidDate(rule.id, transaction.date)
            }
        }
    }

    // Clear fixed payment status when transaction is deleted
    private suspend fun checkAndClearFixedPayments(transaction: Transaction) {
        val rulesList = fixedTransactionDao.getAllRules().first()
        
        rulesList.forEach { rule ->
            // Check if this transaction was the one that marked the payment as paid
            if (rule.lastPaidDate == transaction.date) {
                val amountMatch = kotlin.math.abs(transaction.amount - rule.baseAmount) <= (rule.baseAmount * 0.05)
                val categoryMatch = transaction.category.equals(rule.category, ignoreCase = true)
                
                if (amountMatch && categoryMatch && transaction.type == rule.type) {
                    // Clear the last paid date
                    fixedTransactionDao.updateLastPaidDate(rule.id, 0L)
                }
            }
        }
    }
    
    // ==================== CREDIT CARD MANAGEMENT ====================
    
    val allCreditCardCharges: Flow<List<CreditCardCharge>> = creditCardChargeDao.getAllCharges()
    
    val pendingCreditCardCharges: Flow<List<CreditCardCharge>> = creditCardChargeDao.getPendingCharges()
    
    val totalPendingCreditCardBalance: Flow<Double> = creditCardChargeDao.getTotalPendingBalance().map { it ?: 0.0 }
    
    /**
     * Calculate billing cycle for a given purchase date.
     * Billing cycle is from 4th of one month to 3rd of next month.
     * - Purchases from 4th to end of month: belong to cycle starting on 4th of current month
     * - Purchases from 1st to 3rd: belong to cycle starting on 4th of previous month
     * 
     * Returns Pair(cycleStart, cycleEnd) as timestamps
     */
    fun calculateBillingCycle(purchaseDate: Long): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = purchaseDate
        
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        
        // Determine cycle start
        val cycleStartCalendar = Calendar.getInstance()
        cycleStartCalendar.timeInMillis = purchaseDate
        cycleStartCalendar.set(Calendar.HOUR_OF_DAY, 0)
        cycleStartCalendar.set(Calendar.MINUTE, 0)
        cycleStartCalendar.set(Calendar.SECOND, 0)
        cycleStartCalendar.set(Calendar.MILLISECOND, 0)
        
        if (dayOfMonth >= 4) {
            // Purchase is from 4th onwards, cycle starts on 4th of current month
            cycleStartCalendar.set(Calendar.DAY_OF_MONTH, 4)
        } else {
            // Purchase is from 1st to 3rd, cycle starts on 4th of previous month
            cycleStartCalendar.add(Calendar.MONTH, -1)
            cycleStartCalendar.set(Calendar.DAY_OF_MONTH, 4)
        }
        
        val cycleStart = cycleStartCalendar.timeInMillis
        
        // Cycle end is 3rd of next month (at end of day)
        val cycleEndCalendar = Calendar.getInstance()
        cycleEndCalendar.timeInMillis = cycleStart
        cycleEndCalendar.add(Calendar.MONTH, 1)
        cycleEndCalendar.set(Calendar.DAY_OF_MONTH, 3)
        cycleEndCalendar.set(Calendar.HOUR_OF_DAY, 23)
        cycleEndCalendar.set(Calendar.MINUTE, 59)
        cycleEndCalendar.set(Calendar.SECOND, 59)
        cycleEndCalendar.set(Calendar.MILLISECOND, 999)
        val cycleEnd = cycleEndCalendar.timeInMillis
        
        return Pair(cycleStart, cycleEnd)
    }
    
    /**
     * Calculate payment due date for a billing cycle.
     * Payment is due on the 15th of the SAME month as the cycle end.
     * 
     * Example: Cycle Nov 4 - Dec 3 → Payment due Dec 15
     * 
     * @param billingCycleEnd The end timestamp of the billing cycle
     * @return Timestamp of payment due date (15th of same month as cycle end)
     */
    fun calculatePaymentDueDate(billingCycleEnd: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = billingCycleEnd
        
        // Set to 15th of the same month as cycle end
        calendar.set(Calendar.DAY_OF_MONTH, 15)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        
        return calendar.timeInMillis
    }
    
    /**
     * Add a new credit card charge
     */
    suspend fun addCreditCardCharge(amount: Double, description: String, purchaseDate: Long, createdFromFixedRuleId: Int? = null) {
        val (cycleStart, cycleEnd) = calculateBillingCycle(purchaseDate)
        val dueDate = calculatePaymentDueDate(cycleEnd)
        
        val charge = CreditCardCharge(
            amount = amount,
            description = description,
            purchaseDate = purchaseDate,
            billingCycleStart = cycleStart,
            billingCycleEnd = cycleEnd,
            paymentDueDate = dueDate,
            status = "PENDING",
            paidAmount = 0.0,
            createdFromFixedRuleId = createdFromFixedRuleId
        )
        
        creditCardChargeDao.insert(charge)
    }
    
    /**
     * Make a payment towards credit card balance.
     * This will:
     * 1. Create a CREDIT_PAYMENT transaction
     * 2. Deduct from the account balance
     * 3. Apply payment to pending charges (oldest first)
     * 4. Update charge statuses
     */
    suspend fun makeCreditCardPayment(amount: Double, accountId: Int, description: String = "Abono Tarjeta de Crédito") {
        database.withTransaction {
            // 1. Create transaction
            val transaction = Transaction(
                type = "CREDIT_PAYMENT",
                amount = amount,
                date = System.currentTimeMillis(),
                description = description,
                category = "Abono Tarjeta",
                accountId = accountId,
                creditCardPaymentAmount = amount
            )
            transactionDao.insert(transaction)
            
            // 2. Update account balance (deduct payment amount)
            val account = accountDao.getAccountById(accountId)
            if (account != null) {
                val newBalance = account.currentBalance - amount
                accountDao.updateBalance(accountId, newBalance)
            }
            
            // 3. Apply payment to charges
            applyPaymentToCharges(amount)
        }
    }
    
    /**
     * Apply payment amount to pending charges, starting with oldest first.
     * Marks charges as PAID when fully covered.
     */
    private suspend fun applyPaymentToCharges(paymentAmount: Double) {
        val pendingCharges = creditCardChargeDao.getPendingCharges().first()
        var remainingPayment = paymentAmount
        
        for (charge in pendingCharges) {
            if (remainingPayment <= 0) break
            
            val amountOwed = charge.amount - charge.paidAmount
            val amountToApply = minOf(remainingPayment, amountOwed)
            
            val newPaidAmount = charge.paidAmount + amountToApply
            val newStatus = if (newPaidAmount >= charge.amount) "PAID" else "PENDING"
            
            creditCardChargeDao.updateChargeStatus(charge.id, newStatus, newPaidAmount)
            
            remainingPayment -= amountToApply
        }
    }
    
    /**
     * Delete a credit card charge
     */
    suspend fun deleteCreditCardCharge(charge: CreditCardCharge) {
        creditCardChargeDao.delete(charge)
    }
    
    /**
     * Generate credit card charges from fixed rules.
     * Should be called periodically (e.g., on app startup) to auto-generate charges.
     */
    suspend fun generateFixedCreditCardCharges() {
        val allRules = fixedTransactionDao.getAllRules().first()
        val currentTime = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentTime
        
        for (rule in allRules) {
            // Only process credit card charges
            if (!rule.isCreditCardCharge) continue
            
            // Check if duration has expired
            if (rule.durationMonths != null) {
                val monthsElapsed = rule.timesGenerated
                if (monthsElapsed >= rule.durationMonths) {
                    // Duration expired, skip this rule
                    continue
                }
            }

            // Calculate next payment date based on frequency type
            val nextPaymentCalendar = Calendar.getInstance()
            val targetMonth: Int
            val targetYear: Int
            
            if (rule.frequencyType == "INTERVAL" && rule.intervalDays != null) {
                // For interval-based payments
                val lastGenerated = rule.lastGeneratedDate ?: rule.startDate
                nextPaymentCalendar.timeInMillis = lastGenerated
                nextPaymentCalendar.add(Calendar.DAY_OF_YEAR, rule.intervalDays)
                
                // If next payment is in the past, keep adding intervals until we get a future date
                while (nextPaymentCalendar.timeInMillis < currentTime) {
                    nextPaymentCalendar.add(Calendar.DAY_OF_YEAR, rule.intervalDays)
                }
                
                targetMonth = nextPaymentCalendar.get(Calendar.MONTH)
                targetYear = nextPaymentCalendar.get(Calendar.YEAR)
            } else {
                // For monthly payments (original logic)
                val currentMonth = calendar.get(Calendar.MONTH)
                val currentYear = calendar.get(Calendar.YEAR)
                val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
                
                nextPaymentCalendar.timeInMillis = currentTime
                nextPaymentCalendar.set(Calendar.DAY_OF_MONTH, rule.dayOfMonth)
                
                // If today is past the payment day, the next payment is next month
                if (currentDay > rule.dayOfMonth) {
                    nextPaymentCalendar.add(Calendar.MONTH, 1)
                }
                
                targetMonth = nextPaymentCalendar.get(Calendar.MONTH)
                targetYear = nextPaymentCalendar.get(Calendar.YEAR)
            }
            
            // Calculate days until next payment
            val diffInMillis = nextPaymentCalendar.timeInMillis - currentTime
            val daysUntil = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
            
            // Only generate if it's "Upcoming" (<= 7 days) or overdue (negative days but not too old)
            // We use 7 days as the threshold for "Upcoming"
            if (daysUntil > 7) {
                continue
            }
            
            // Check if we already generated a charge for this target date
            val allCharges = creditCardChargeDao.getAllCharges().first()
            val alreadyGeneratedForTargetDate = allCharges.any { charge ->
                charge.createdFromFixedRuleId == rule.id &&
                Calendar.getInstance().apply {
                    timeInMillis = charge.purchaseDate
                }.let { chargeCalendar ->
                    if (rule.frequencyType == "INTERVAL") {
                        // For interval: check if charge date is close to target date (within 1 day)
                        val chargeDiff = kotlin.math.abs(chargeCalendar.timeInMillis - nextPaymentCalendar.timeInMillis)
                        chargeDiff < (2 * 24 * 60 * 60 * 1000L) // Within 2 days
                    } else {
                        // For monthly: check month and year
                        chargeCalendar.get(Calendar.MONTH) == targetMonth &&
                        chargeCalendar.get(Calendar.YEAR) == targetYear
                    }
                }
            }
            
            if (alreadyGeneratedForTargetDate) {
                // Already generated for this specific due date, skip
                continue
            }
            
            // Generate the charge for the calculated target date
            val chargeDate = nextPaymentCalendar.timeInMillis
            
            addCreditCardCharge(
                amount = rule.baseAmount,
                description = rule.description,
                purchaseDate = chargeDate,
                createdFromFixedRuleId = rule.id
            )
            
            // Increment counter and update lastGeneratedDate
            fixedTransactionDao.incrementTimesGenerated(rule.id)
            
            // Update lastGeneratedDate for interval-based payments
            if (rule.frequencyType == "INTERVAL") {
                fixedTransactionDao.updateLastGeneratedDate(rule.id, chargeDate)
            }
        }
    }
}
