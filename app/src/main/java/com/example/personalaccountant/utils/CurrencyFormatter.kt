package com.example.personalaccountant.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Format a number with thousand separators (spaces)
 * Examples:
 * - 1250355.50 → "1 250 355.50"
 * - 250000.00 → "250 000.00"
 * - 35000.75 → "35 000.75"
 * - 5000.00 → "5 000.00"
 */
fun formatCurrency(amount: Double): String {
    val symbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = ' '  // Use space as thousand separator
        decimalSeparator = '.'
    }
    
    val formatter = DecimalFormat("#,##0.00", symbols)
    return formatter.format(amount)
}

/**
 * Format currency with currency symbol
 */
fun formatCurrencyWithSymbol(amount: Double): String {
    return "₡${formatCurrency(amount)}"
}
