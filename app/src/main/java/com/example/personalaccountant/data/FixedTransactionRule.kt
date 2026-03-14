package com.example.personalaccountant.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "fixed_transaction_rules",
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
data class FixedTransactionRule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "INCOME", "EXPENSE"
    val baseAmount: Double,
    val dayOfMonth: Int, // 1-31 (used only when frequencyType = "MONTHLY")
    val description: String,
    val accountId: Int,
    val category: String, // For matching with transactions
    val lastPaidDate: Long? = null, // Timestamp of last payment
    val isCreditCardCharge: Boolean = false, // True if this is a credit card charge
    val durationMonths: Int? = null, // Duration in months (null = indefinite)
    val startDate: Long = System.currentTimeMillis(), // Start date for the fixed rule
    val timesGenerated: Int = 0, // Counter of how many times this has been generated
    val frequencyType: String = "MONTHLY", // "MONTHLY" or "INTERVAL"
    val intervalDays: Int? = null, // Number of days for interval frequency (null if monthly)
    val lastGeneratedDate: Long? = null // Last date when payment was generated (for interval mode)
)
