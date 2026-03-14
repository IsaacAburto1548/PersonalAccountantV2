package com.example.personalaccountant.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "credit_card_charges")
data class CreditCardCharge(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val description: String,
    val purchaseDate: Long, // Timestamp of purchase
    val billingCycleStart: Long, // Start of billing cycle (4th of month)
    val billingCycleEnd: Long, // End of billing cycle (4th of next month)
    val paymentDueDate: Long, // Payment due date (15th of month after cycle end)
    val status: String, // "PENDING", "PAID"
    val paidAmount: Double = 0.0, // Amount paid towards this charge
    val createdFromFixedRuleId: Int? = null // If auto-generated from fixed rule
)
