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

/**
 * Format a timestamp to date and time "dd/MM/yyyy HH:mm"
 * Example: "22/11/2024 14:30"
 */
fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MMM/yyyy - HH:mm", Locale("es", "ES"))
    return sdf.format(Date(timestamp))
}

/**
 * Format a timestamp to time only "HH:mm"
 * Example: "14:30"
 */
fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale("es", "ES"))
    return sdf.format(Date(timestamp))
}
