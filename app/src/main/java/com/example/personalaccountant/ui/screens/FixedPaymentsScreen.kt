package com.example.personalaccountant.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.personalaccountant.R
import com.example.personalaccountant.data.FixedTransactionRule
import com.example.personalaccountant.ui.components.BottomNavigationBar
import com.example.personalaccountant.ui.components.EmptyStateView
import com.example.personalaccountant.ui.theme.BorderGray
import com.example.personalaccountant.ui.theme.ExpenseRed
import com.example.personalaccountant.ui.theme.IncomeGreen
import com.example.personalaccountant.ui.viewmodel.FixedTransactionUiEvent
import com.example.personalaccountant.ui.viewmodel.FixedTransactionViewModel
import com.example.personalaccountant.utils.formatCurrencyWithSymbol
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixedPaymentsScreen(
    navController: NavController,
    viewModel: FixedTransactionViewModel
) {
    val fixedRules by viewModel.fixedRules.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showAddDialog by remember { mutableStateOf(false) }
    var editingRule by remember { mutableStateOf<FixedTransactionRule?>(null) }

    // Handle UI Events
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is FixedTransactionUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_financify),
                            contentDescription = "Logo",
                            modifier = Modifier.size(40.dp)
                        )
                        Text("Pagos Fijos", color = Color.White, fontWeight = FontWeight.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(navController, "fixed_payments")
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Pago Fijo", tint = Color.White)
            }
        }
    ) { paddingValues ->
        // Sort fixed rules by days until payment (ascending)
        // Overdue payments (negative days) come first, then upcoming payments
        val sortedFixedRules = fixedRules.sortedBy { rule ->
            val (_, _, daysUntil) = getPaymentStatus(rule)
            daysUntil ?: Int.MAX_VALUE // Put items with null daysUntil at the end
        }
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Upcoming Total Summary
            val upcomingTotal = sortedFixedRules.sumOf { rule ->
                val (_, statusText, _) = getPaymentStatus(rule)
                if (statusText == "Próximo" || statusText == "Vencido") rule.baseAmount else 0.0
            }

            if (upcomingTotal > 0) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFFFA726), Color(0xFFFF7043))
                                    )
                                )
                                .padding(24.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Total Próximo a Vencer",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                Text(
                                    text = formatCurrencyWithSymbol(upcomingTotal),
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            if (sortedFixedRules.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "Sin pagos fijos",
                        subtitle = "Configura tus servicios y suscripciones",
                        modifier = Modifier.padding(top = 32.dp)
                    )
                }
            }

            items(sortedFixedRules) { rule ->
                FixedPaymentCard(
                    rule = rule,
                    onEdit = { editingRule = rule },
                    onDelete = { viewModel.deleteRule(rule.id) }
                )
            }
        }
    }

    if (showAddDialog) {
        FixedPaymentDialog(
            rule = null,
            accounts = accounts,
            onDismiss = { showAddDialog = false },
            onSave = { rule ->
                viewModel.addRule(rule)
                showAddDialog = false
            }
        )
    }

    if (editingRule != null) {
        FixedPaymentDialog(
            rule = editingRule,
            accounts = accounts,
            onDismiss = { editingRule = null },
            onSave = { rule ->
                viewModel.updateRule(rule)
                editingRule = null
            }
        )
    }
}

@Composable
fun FixedPaymentCard(
    rule: FixedTransactionRule,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val (statusColor, statusText, daysUntil) = getPaymentStatus(rule)

    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable { onEdit() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = rule.description,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (rule.isCreditCardCharge) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Text(
                        text = rule.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                
                Text(
                    text = formatCurrencyWithSymbol(rule.baseAmount),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = if (rule.type == "INCOME") IncomeGreen else ExpenseRed
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(statusColor.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    if (daysUntil != null) {
                        Text(
                            text = if (daysUntil > 0) "En $daysUntil días" else if (daysUntil == 0) "Hoy" else "${-daysUntil} días de retraso",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixedPaymentDialog(
    rule: FixedTransactionRule?,
    accounts: List<com.example.personalaccountant.data.Account>,
    onDismiss: () -> Unit,
    onSave: (FixedTransactionRule) -> Unit
) {
    var description by remember { mutableStateOf(rule?.description ?: "") }
    var amount by remember { mutableStateOf(rule?.baseAmount?.toString() ?: "") }
    var day by remember { mutableStateOf(rule?.dayOfMonth?.toString() ?: "") }
    var category by remember { mutableStateOf(rule?.category ?: "Gastos fijos") }
    var type by remember { mutableStateOf(rule?.type ?: "EXPENSE") }
    var selectedAccount by remember { mutableStateOf(accounts.firstOrNull { it.id == rule?.accountId } ?: accounts.firstOrNull()) }
    var accountExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var isCreditCard by remember { mutableStateOf(rule?.isCreditCardCharge ?: false) }
    var isIndefinite by remember { mutableStateOf(rule?.durationMonths == null) }
    var durationMonths by remember { mutableStateOf(rule?.durationMonths?.toString() ?: "12") }
    var frequencyType by remember { mutableStateOf(rule?.frequencyType ?: "MONTHLY") }
    var intervalDays by remember { mutableStateOf(rule?.intervalDays?.toString() ?: "7") }

    val categories = listOf("Salarios", "Gastos fijos", "Sinpe Movil", "Gastos Personales", "Mascotas", "Hogar", "Entretenimiento")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f), // Limit to 90% of screen height
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (rule == null) "Nuevo Pago Fijo" else "Editar Pago Fijo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) amount = it },
                    label = { Text("Monto") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                // Frequency Type Section
                Text(
                    text = "Frecuencia de Pago",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = frequencyType == "MONTHLY",
                        onClick = { frequencyType = "MONTHLY" }
                    )
                    Text(
                        text = "Mensual (día específico)",
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Day of Month field (only for MONTHLY)
                if (frequencyType == "MONTHLY") {
                    OutlinedTextField(
                        value = day,
                        onValueChange = { if (it.all { char -> char.isDigit() } && (it.toIntOrNull() ?: 0) <= 31) day = it },
                        label = { Text("Día del Mes (1-31)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = frequencyType == "INTERVAL",
                        onClick = { frequencyType = "INTERVAL" }
                    )
                    Text(
                        text = "Por intervalo (cada X días)",
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Interval Days field (only for INTERVAL)
                if (frequencyType == "INTERVAL") {
                    OutlinedTextField(
                        value = intervalDays,
                        onValueChange = { 
                            if (it.all { char -> char.isDigit() } && (it.toIntOrNull() ?: 0) <= 365) {
                                intervalDays = it
                            }
                        },
                        label = { Text("Cada cuántos días (1-365)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = accountExpanded,
                    onExpandedChange = { accountExpanded = !accountExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedAccount?.name ?: "Seleccionar Cuenta",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cuenta") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = accountExpanded,
                        onDismissRequest = { accountExpanded = false }
                    ) {
                        accounts.forEach { account ->
                            DropdownMenuItem(
                                text = { Text(account.name) },
                                onClick = {
                                    selectedAccount = account
                                    accountExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Credit Card Checkbox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isCreditCard,
                        onCheckedChange = { isCreditCard = it }
                    )
                    Text(
                        text = "Es gasto de tarjeta de crédito 💳",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // Duration Section
                Text(
                    text = "Duración del Gasto Fijo",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isIndefinite,
                        onClick = { isIndefinite = true }
                    )
                    Text(
                        text = "Indefinido",
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = !isIndefinite,
                        onClick = { isIndefinite = false }
                    )
                    Text(text = "Temporal:", modifier = Modifier.padding(end = 8.dp))
                    OutlinedTextField(
                        value = durationMonths,
                        onValueChange = { 
                            if (it.all { char -> char.isDigit() } && (it.toIntOrNull() ?: 0) <= 120) {
                                durationMonths = it
                            }
                        },
                        label = { Text("Meses") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        enabled = !isIndefinite
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            val isValid = description.isNotEmpty() && amount.isNotEmpty() && selectedAccount != null &&
                                    ((frequencyType == "MONTHLY" && day.isNotEmpty()) || 
                                     (frequencyType == "INTERVAL" && intervalDays.isNotEmpty()))
                            
                            if (isValid) {
                                val finalDuration = if (isIndefinite) null else durationMonths.toIntOrNull()
                                val newRule = FixedTransactionRule(
                                    id = rule?.id ?: 0,
                                    type = type,
                                    baseAmount = amount.toDoubleOrNull() ?: 0.0,
                                    dayOfMonth = if (frequencyType == "MONTHLY") day.toIntOrNull() ?: 1 else 1,
                                    description = description,
                                    accountId = selectedAccount!!.id,
                                    category = category,
                                    lastPaidDate = rule?.lastPaidDate,
                                    isCreditCardCharge = isCreditCard,
                                    durationMonths = finalDuration,
                                    startDate = rule?.startDate ?: System.currentTimeMillis(),
                                    timesGenerated = rule?.timesGenerated ?: 0,
                                    frequencyType = frequencyType,
                                    intervalDays = if (frequencyType == "INTERVAL") intervalDays.toIntOrNull() else null,
                                    lastGeneratedDate = rule?.lastGeneratedDate
                                )
                                onSave(newRule)
                            }
                        },
                        enabled = description.isNotEmpty() && amount.isNotEmpty() && selectedAccount != null && 
                                ((frequencyType == "MONTHLY" && day.isNotEmpty()) || 
                                 (frequencyType == "INTERVAL" && intervalDays.isNotEmpty())) &&
                                (isIndefinite || durationMonths.toIntOrNull() != null)
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

private fun getPaymentStatus(rule: FixedTransactionRule): Triple<Color, String, Int?> {
    val calendar = Calendar.getInstance()
    val currentTime = calendar.timeInMillis
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)

    // Calculate next payment date based on frequency type
    val nextPaymentCalendar = Calendar.getInstance()
    
    if (rule.frequencyType == "INTERVAL" && rule.intervalDays != null) {
        // For interval-based payments
        val lastGenerated = rule.lastGeneratedDate ?: rule.startDate
        nextPaymentCalendar.timeInMillis = lastGenerated
        nextPaymentCalendar.add(Calendar.DAY_OF_YEAR, rule.intervalDays)
        
        // If next payment is in the past, keep adding intervals until we get a future date
        while (nextPaymentCalendar.timeInMillis < currentTime) {
            nextPaymentCalendar.add(Calendar.DAY_OF_YEAR, rule.intervalDays)
        }
    } else {
        // For monthly payments (original logic)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        nextPaymentCalendar.set(Calendar.DAY_OF_MONTH, rule.dayOfMonth)
        
        if (currentDay > rule.dayOfMonth) {
            // Next payment is next month
            nextPaymentCalendar.add(Calendar.MONTH, 1)
        }
    }

    val daysUntil = ((nextPaymentCalendar.timeInMillis - currentTime) / (1000 * 60 * 60 * 24)).toInt()

    // Check if paid recently
    val isPaidRecently = if (rule.frequencyType == "INTERVAL" && rule.intervalDays != null) {
        // For interval: check if paid within the current interval period
        rule.lastPaidDate?.let { lastPaid ->
            val daysSinceLastPaid = ((currentTime - lastPaid) / (1000 * 60 * 60 * 24)).toInt()
            daysSinceLastPaid < rule.intervalDays
        } ?: false
    } else {
        // For monthly: check if paid this month (original logic)
        rule.lastPaidDate?.let { lastPaid ->
            val lastPaidCalendar = Calendar.getInstance()
            lastPaidCalendar.timeInMillis = lastPaid
            lastPaidCalendar.get(Calendar.MONTH) == currentMonth && 
            lastPaidCalendar.get(Calendar.YEAR) == currentYear
        } ?: false
    }

    return when {
        isPaidRecently -> Triple(Color(0xFF26A69A), "Pagado", daysUntil)
        daysUntil < 0 -> Triple(Color(0xFFE57373), "Vencido", daysUntil)
        daysUntil <= 7 -> Triple(Color(0xFFFFA726), "Próximo", daysUntil)
        else -> Triple(Color(0xFF26A69A), "Pendiente", daysUntil)
    }
}
