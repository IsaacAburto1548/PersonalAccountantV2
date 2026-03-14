package com.example.personalaccountant.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("accountId")]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "INCOME", "EXPENSE", "CREDIT_PAYMENT"
    val amount: Double,
    val date: Long, // Timestamp
    val description: String,
    val category: String,
    val accountId: Int,
    val creditCardPaymentAmount: Double? = null // Amount applied to credit card (for CREDIT_PAYMENT type)
)
