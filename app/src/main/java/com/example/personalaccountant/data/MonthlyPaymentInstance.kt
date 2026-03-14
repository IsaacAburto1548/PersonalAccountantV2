package com.example.personalaccountant.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "monthly_payment_instances",
    foreignKeys = [
        ForeignKey(
            entity = FixedTransactionRule::class,
            parentColumns = ["id"],
            childColumns = ["ruleId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Transaction::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("ruleId"), Index("transactionId")]
)
data class MonthlyPaymentInstance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ruleId: Int,
    val dueDate: Long,
    val amount: Double,
    val status: String, // "PENDING", "PAID"
    val transactionId: Int? = null // Link to the actual transaction when paid
)
