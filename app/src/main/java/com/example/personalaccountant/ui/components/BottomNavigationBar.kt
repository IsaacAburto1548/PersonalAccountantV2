package com.example.personalaccountant.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.personalaccountant.R

@Composable
fun BottomNavigationBar(
    navController: NavController,
    currentRoute: String,
    onNavigate: ((String) -> Unit)? = null
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = { 
                Icon(
                    painter = painterResource(id = R.drawable.ic_dashboard),
                    contentDescription = "Inicio",
                    modifier = Modifier.size(24.dp)
                ) 
            },
            label = { Text("Inicio") },
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
                    painter = painterResource(id = R.drawable.ic_credit_card),
                    contentDescription = "Tarjeta",
                    modifier = Modifier.size(30.dp)
                ) 
            },
            label = { Text("Tarjeta") },
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
                    painter = painterResource(id = R.drawable.ic_fixed_payments),
                    contentDescription = "Pagos Fijos",
                    modifier = Modifier.size(24.dp)
                ) 
            },
            label = { Text("Pagos") },
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
                    painter = painterResource(id = R.drawable.ic_history),
                    contentDescription = "Historial",
                    modifier = Modifier.size(24.dp)
                ) 
            },
            label = { Text("Historial") },
            selected = currentRoute == "transaction_history",
            onClick = { 
                if (currentRoute != "transaction_history") {
                    onNavigate?.invoke("transaction_history") ?: navController.navigate("transaction_history")
                }
            }
        )
    }
}
