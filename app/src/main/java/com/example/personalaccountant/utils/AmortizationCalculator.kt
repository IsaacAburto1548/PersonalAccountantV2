package com.example.personalaccountant.utils

import kotlin.math.pow

data class AmortizationRow(
    val month: Int,
    val quota: Double,
    val interestPayment: Double,
    val principalPayment: Double,
    val remainingBalance: Double,
    val availableSalary: Double
)

object AmortizationCalculator {
    /**
     * Calculates the amortization schedule.
     * @param loanAmount Total amount of the loan
     * @param annualInterestRate Annual interest rate as a decimal (e.g., 0.33 for 33%)
     * @param termMonths Number of months for the loan
     * @param monthlySalary The user's monthly salary
     * @return List of AmortizationRow representing each month of the loan
     */
    fun calculateAmortization(
        loanAmount: Double,
        annualInterestRate: Double,
        termMonths: Int,
        monthlySalary: Double
    ): List<AmortizationRow> {
        val schedule = mutableListOf<AmortizationRow>()
        
        // Handle edge cases
        if (loanAmount <= 0 || termMonths <= 0) return schedule

        val monthlyInterestRate = annualInterestRate / 12.0
        
        // Calculate monthly payment (PMT)
        val quota = if (monthlyInterestRate > 0) {
            (loanAmount * monthlyInterestRate) / (1 - (1 + monthlyInterestRate).pow(-termMonths))
        } else {
            loanAmount / termMonths
        }

        var remainingBalance = loanAmount

        for (month in 1..termMonths) {
            val interestPayment = remainingBalance * monthlyInterestRate
            val principalPayment = quota - interestPayment
            
            // Due to floating point precision, remaining balance might not be exactly 0 at the end
            remainingBalance -= principalPayment
            if (remainingBalance < 0.01) remainingBalance = 0.0

            val availableSalary = monthlySalary - quota

            schedule.add(
                AmortizationRow(
                    month = month,
                    quota = quota,
                    interestPayment = interestPayment,
                    principalPayment = principalPayment,
                    remainingBalance = remainingBalance,
                    availableSalary = availableSalary
                )
            )
        }

        return schedule
    }
}
