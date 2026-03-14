package com.example.personalaccountant.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Format a timestamp to "ddd dd/mmm/yyyy" format
 * Example: "Vie 22/Nov/2024"
 */
fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("EEE dd/MMM/yyyy", Locale("es", "ES"))
    return sdf.format(Date(timestamp))
}

/**
 * Format a timestamp to short date format "dd/MM/yyyy"
 * Example: "22/11/2024"
 */
fun formatDateShort(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))
    return sdf.format(Date(timestamp))
}
