package com.example.personalaccountant.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>>

    @Insert
    suspend fun insert(transaction: Transaction): Long

    @androidx.room.Update
    suspend fun update(transaction: Transaction)

    @androidx.room.Delete
    suspend fun delete(transaction: Transaction)
    
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Int): Transaction?
    
    @Query("DELETE FROM transactions WHERE date < :timestamp")
    suspend fun deleteTransactionsOlderThan(timestamp: Long)
}
