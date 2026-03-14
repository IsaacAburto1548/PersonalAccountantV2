package com.example.personalaccountant.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditCardChargeDao {
    @Query("SELECT * FROM credit_card_charges ORDER BY purchaseDate DESC")
    fun getAllCharges(): Flow<List<CreditCardCharge>>
    
    @Query("SELECT * FROM credit_card_charges WHERE status = :status ORDER BY purchaseDate ASC")
    fun getChargesByStatus(status: String): Flow<List<CreditCardCharge>>
    
    @Query("SELECT * FROM credit_card_charges WHERE status = 'PENDING' ORDER BY purchaseDate ASC")
    fun getPendingCharges(): Flow<List<CreditCardCharge>>
    
    @Query("SELECT SUM(amount - paidAmount) FROM credit_card_charges WHERE status = 'PENDING'")
    fun getTotalPendingBalance(): Flow<Double?>
    
    @Query("SELECT * FROM credit_card_charges WHERE id = :id")
    suspend fun getChargeById(id: Int): CreditCardCharge?
    
    @Insert
    suspend fun insert(charge: CreditCardCharge): Long
    
    @Update
    suspend fun update(charge: CreditCardCharge)
    
    @Delete
    suspend fun delete(charge: CreditCardCharge)
    
    @Query("UPDATE credit_card_charges SET status = :status, paidAmount = :paidAmount WHERE id = :id")
    suspend fun updateChargeStatus(id: Int, status: String, paidAmount: Double)
}
