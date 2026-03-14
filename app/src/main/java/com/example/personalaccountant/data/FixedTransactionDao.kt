package com.example.personalaccountant.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FixedTransactionDao {
    // Rules
    @Query("SELECT * FROM fixed_transaction_rules")
    fun getAllRules(): Flow<List<FixedTransactionRule>>

    @Insert
    suspend fun insertRule(rule: FixedTransactionRule): Long

    @Update
    suspend fun updateRule(rule: FixedTransactionRule)

    @Query("DELETE FROM fixed_transaction_rules WHERE id = :id")
    suspend fun deleteRule(id: Int)

    @Query("UPDATE fixed_transaction_rules SET lastPaidDate = :date WHERE id = :id")
    suspend fun updateLastPaidDate(id: Int, date: Long)
    
    @Query("UPDATE fixed_transaction_rules SET timesGenerated = timesGenerated + 1 WHERE id = :id")
    suspend fun incrementTimesGenerated(id: Int)
    
    @Query("UPDATE fixed_transaction_rules SET lastGeneratedDate = :date WHERE id = :id")
    suspend fun updateLastGeneratedDate(id: Int, date: Long)

    // Instances
    @Query("SELECT * FROM monthly_payment_instances WHERE dueDate BETWEEN :startDate AND :endDate ORDER BY dueDate ASC")
    fun getInstancesByDateRange(startDate: Long, endDate: Long): Flow<List<MonthlyPaymentInstance>>

    @Insert
    suspend fun insertInstance(instance: MonthlyPaymentInstance)

    @Update
    suspend fun updateInstance(instance: MonthlyPaymentInstance)
    
    @Query("SELECT * FROM monthly_payment_instances WHERE status = 'PENDING' AND dueDate < :currentTime")
    suspend fun getOverduePendingInstances(currentTime: Long): List<MonthlyPaymentInstance>
}
