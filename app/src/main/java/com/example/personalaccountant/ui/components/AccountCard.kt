package com.example.personalaccountant.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.personalaccountant.data.Account
import com.example.personalaccountant.ui.theme.ExpenseRed
import com.example.personalaccountant.ui.theme.IncomeGreen
import com.example.personalaccountant.utils.formatCurrencyWithSymbol

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AccountCard(
    account: Account,
    onDelete: () -> Unit = {},
    onEdit: () -> Unit = {},
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onHighlight: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

    val icon = when (account.type) {
        "CASH" -> Icons.Default.AccountBalanceWallet
        "CARD" -> Icons.Default.CreditCard
        "SAVINGS" -> Icons.Default.Savings
        else -> Icons.Default.AccountBalanceWallet
    }

    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { if (isSelectionMode) onHighlight() else { /* Could navigate to details */ } },
                    onLongClick = { onHighlight() }
                ),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (isSelectionMode) {
                        androidx.compose.material3.Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onHighlight() },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = account.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = when (account.type) {
                                "CASH" -> "Efectivo"
                                "CARD" -> "Tarjeta"
                                "SAVINGS" -> "Ahorros"
                                else -> account.type
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatCurrencyWithSymbol(account.currentBalance),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (account.currentBalance >= 0) IncomeGreen else ExpenseRed
                    )
                }
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Editar") },
                onClick = {
                    showMenu = false
                    onEdit()
                },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text("Eliminar", color = ExpenseRed) },
                onClick = {
                    showMenu = false
                    onDelete()
                },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = ExpenseRed) }
            )
        }
    }
}
