package com.example.personalaccountant.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts")
    fun getAllAccounts(): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: Int): Account?

    @Insert
    suspend fun insert(account: Account)

    @Update
    suspend fun update(account: Account)

    @Query("UPDATE accounts SET currentBalance = :newBalance WHERE id = :accountId")
    suspend fun updateBalance(accountId: Int, newBalance: Double)

    @androidx.room.Delete
    suspend fun delete(account: Account)
}
