package com.example.personalaccountant.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.personalaccountant.R
import com.example.personalaccountant.data.Transaction
import com.example.personalaccountant.ui.components.BottomNavigationBar
import com.example.personalaccountant.ui.components.EmptyStateView
import com.example.personalaccountant.ui.theme.ExpenseRed
import com.example.personalaccountant.ui.theme.IncomeGreen
import com.example.personalaccountant.ui.viewmodel.TransactionUiEvent
import com.example.personalaccountant.ui.viewmodel.TransactionViewModel
import com.example.personalaccountant.utils.formatCurrencyWithSymbol
import com.example.personalaccountant.utils.formatDate
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    navController: NavController,
    viewModel: TransactionViewModel
) {
    val allTransactions by viewModel.transactions.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }

    // Handle UI Events (Delete/Undo)
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is TransactionUiEvent.ShowSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = if (event.deletedTx != null) "Deshacer" else null
                    )
                    if (result == SnackbarResult.ActionPerformed && event.deletedTx != null) {
                        viewModel.undoDeleteTransaction(event.deletedTx)
                    }
                }
            }
        }
    }

    // Month Selector Data
    val months = remember {
        val list = mutableListOf<Pair<Int, Int>>()
        val cal = Calendar.getInstance()
        // Last 12 months
        for (i in 0 until 12) {
            list.add(Pair(cal.get(Calendar.MONTH), cal.get(Calendar.YEAR)))
            cal.add(Calendar.MONTH, -1)
        }
        list
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.primary)) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_financify),
                                contentDescription = "Logo",
                                modifier = Modifier.size(50.dp)
                            )
                            Text("Historial", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White
                    ),
                    actions = {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = Color.White)
                        }
                    }
                )

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Buscar transacción...", color = Color.White.copy(alpha = 0.7f)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                // Month Chip Selector
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedMonth == null,
                            onClick = { viewModel.clearMonthFilter() },
                            label = { Text("Todo") },
                            colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color.White,
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    items(months) { month ->
                        val isSelected = selectedMonth?.first == month.first && selectedMonth?.second == month.second
                        val cal = Calendar.getInstance().apply {
                            set(Calendar.MONTH, month.first)
                            set(Calendar.YEAR, month.second)
                        }
                        val name = SimpleDateFormat("MMM yy", Locale("es", "ES")).format(cal.time)
                        
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setSelectedMonth(month.first, month.second) },
                            label = { Text(name.capitalize()) },
                            colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color.White,
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        },
        bottomBar = {
            BottomNavigationBar(navController, "transaction_history")
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            if (allTransactions.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "No hay transacciones",
                        subtitle = if (searchQuery.isNotEmpty()) "Prueba con otra búsqueda" else "Empieza agregando tus gastos",
                        modifier = Modifier.padding(top = 64.dp)
                    )
                }
            } else {
                items(
                    items = allTransactions,
                    key = { it.id }
                ) { transaction ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            if (it == SwipeToDismissBoxValue.EndToStart) {
                                transactionToDelete = transaction
                                true
                            } else {
                                false
                            }
                        }
                    )

                    LaunchedEffect(dismissState.currentValue) {
                        if (dismissState.currentValue == SwipeToDismissBoxValue.Settled && transactionToDelete != null) {
                            viewModel.deleteTransaction(transactionToDelete!!)
                            transactionToDelete = null // Reset after deletion
                        }
                    }

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = {
                            val color = when (dismissState.dismissDirection) {
                                SwipeToDismissBoxValue.EndToStart -> ExpenseRed
                                else -> Color.Transparent
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(color)
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Eliminar",
                                    tint = Color.White
                                )
                            }
                        },
                        content = {
                            TransactionCard(
                                transaction = transaction,
                                onClick = {
                                    navController.navigate("add_transaction?transactionId=${transaction.id}")
                                }
                            )
                        }
                    )
                }
                
                // Summary section only if month is selected
                if (selectedMonth != null && allTransactions.isNotEmpty()) {
                    item {
                        val income = allTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
                        val expense = allTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                        val credit = allTransactions.filter { it.type == "CREDIT_PAYMENT" }.sumOf { it.amount }
                        val balance = income - expense - credit

                        TransactionSummaryCard(income, expense, credit, balance)
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun TransactionSummaryCard(income: Double, expense: Double, credit: Double, balance: Double) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Resumen del Periodo", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            SummaryRow("Ingresos", income, IncomeGreen)
            SummaryRow("Gastos", -expense, ExpenseRed)
            SummaryRow("Abonos TC", -credit, Color(0xFF9C27B0))
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Balance", fontWeight = FontWeight.Bold)
                Text(
                    formatCurrencyWithSymbol(balance),
                    fontWeight = FontWeight.Bold,
                    color = if (balance >= 0) IncomeGreen else ExpenseRed
                )
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, amount: Double, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(formatCurrencyWithSymbol(amount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = color)
    }
}



@Composable
fun TransactionCard(
    transaction: com.example.personalaccountant.data.Transaction,
    onClick: () -> Unit
) {
    val (backgroundColor, textColor, emoji, typeText) = when (transaction.type) {
        "INCOME" -> Quadruple(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            IncomeGreen,
            "💰",
            "Ingreso"
        )
        "EXPENSE" -> Quadruple(
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
            ExpenseRed,
            "💸",
            "Gasto"
        )
        "CREDIT_PAYMENT" -> Quadruple(
            Color(0xFFF3E5F5),
            Color(0xFF9C27B0),
            "💳",
            "Abono TC"
        )
        else -> Quadruple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "📝",
            "Otro"
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(backgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 20.sp)
                }
                
                Column {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = transaction.category,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text("•", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                        Text(
                            text = formatDate(transaction.date),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }
            
            val amountPrefix = when (transaction.type) {
                "INCOME" -> "+"
                "EXPENSE" -> "-"
                "CREDIT_PAYMENT" -> "-"
                else -> ""
            }
            
            Text(
                text = "$amountPrefix${formatCurrencyWithSymbol(transaction.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )
        }
    }
}

// Helper data class for quadruple
data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
