package com.example.personalaccountant.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun BottomNavigationBar(
    navController: NavController,
    currentRoute: String,
    onNavigate: ((String) -> Unit)? = null
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            icon = { 
                Icon(
                    imageVector = Icons.Default.PieChart,
                    contentDescription = "Inicio"
                ) 
            },
            label = { Text("Inicio", fontWeight = if (currentRoute == "dashboard") FontWeight.Bold else FontWeight.Normal) },
            selected = currentRoute == "dashboard",
            onClick = { 
                if (currentRoute != "dashboard") {
                    onNavigate?.invoke("dashboard") ?: navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            }
        )
        NavigationBarItem(
            icon = { 
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = "Tarjeta"
                ) 
            },
            label = { Text("Tarjeta", fontWeight = if (currentRoute == "credit_card") FontWeight.Bold else FontWeight.Normal) },
            selected = currentRoute == "credit_card",
            onClick = { 
                if (currentRoute != "credit_card") {
                    onNavigate?.invoke("credit_card") ?: navController.navigate("credit_card")
                }
            }
        )
        NavigationBarItem(
            icon = { 
                Icon(
                    imageVector = Icons.Default.EventRepeat,
                    contentDescription = "Pagos Fijos"
                ) 
            },
            label = { Text("Pagos", fontWeight = if (currentRoute == "fixed_payments") FontWeight.Bold else FontWeight.Normal) },
            selected = currentRoute == "fixed_payments",
            onClick = { 
                if (currentRoute != "fixed_payments") {
                    onNavigate?.invoke("fixed_payments") ?: navController.navigate("fixed_payments")
                }
            }
        )
        NavigationBarItem(
            icon = { 
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                    contentDescription = "Historial"
                ) 
            },
            label = { Text("Historial", fontWeight = if (currentRoute == "transaction_history") FontWeight.Bold else FontWeight.Normal) },
            selected = currentRoute == "transaction_history",
            onClick = { 
                if (currentRoute != "transaction_history") {
                    onNavigate?.invoke("transaction_history") ?: navController.navigate("transaction_history")
                }
            }
        )
    }
}
