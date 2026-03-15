package com.example.personalaccountant.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.personalaccountant.R
import com.example.personalaccountant.data.CreditCardCharge
import com.example.personalaccountant.ui.components.BottomNavigationBar
import com.example.personalaccountant.ui.components.ConfirmationDialog
import com.example.personalaccountant.ui.components.EmptyStateView
import com.example.personalaccountant.ui.theme.BorderGray
import com.example.personalaccountant.ui.theme.ExpenseRed
import com.example.personalaccountant.ui.theme.IncomeGreen
import com.example.personalaccountant.ui.viewmodel.CreditCardUiEvent
import com.example.personalaccountant.ui.viewmodel.CreditCardViewModel
import com.example.personalaccountant.utils.formatCurrency
import com.example.personalaccountant.utils.formatCurrencyWithSymbol
import com.example.personalaccountant.utils.formatDate
import com.example.personalaccountant.utils.formatDateShort
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditCardScreen(
    navController: NavController,
    viewModel: CreditCardViewModel
) {
    val allCharges by viewModel.allCharges.collectAsStateWithLifecycle()
    val totalPendingBalance by viewModel.totalPendingBalance.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showAddChargeDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showEditChargeDialog by remember { mutableStateOf(false) }
    var editingCharge by remember { mutableStateOf<CreditCardCharge?>(null) }
    var chargeToDelete by remember { mutableStateOf<CreditCardCharge?>(null) }
    
    // Handle UI Events
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is CreditCardUiEvent.ShowSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = if (event.deletedCharge != null) "Deshacer" else null
                    )
                    if (result == SnackbarResult.ActionPerformed && event.deletedCharge != null) {
                        viewModel.undoDeleteCharge(event.deletedCharge)
                    }
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_financify),
                            contentDescription = "Logo",
                            modifier = Modifier.size(50.dp)
                        )
                        Text("Tarjeta de Crédito", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(navController, "credit_card")
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Premium Debt Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                                    )
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = ExpenseRed,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Deuda Total",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = formatCurrencyWithSymbol(totalPendingBalance),
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            
            // Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showPaymentDialog = true },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Abonar", fontWeight = FontWeight.Bold)
                    }
                    
                    OutlinedButton(
                        onClick = { showAddChargeDialog = true },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Nuevo Cargo", fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            // Charges Header
            item {
                Text(
                    text = "Cargos Pendientes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            // Charges List
            if (allCharges.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "Sin deudas",
                        subtitle = "Tus tarjetas están al día",
                        modifier = Modifier.padding(top = 32.dp)
                    )
                }
            } else {
                items(
                    items = allCharges,
                    key = { it.id }
                ) { charge ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            if (it == SwipeToDismissBoxValue.EndToStart) {
                                viewModel.deleteCharge(charge)
                                true
                            } else false
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(ExpenseRed)
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.White)
                            }
                        },
                        content = {
                            CreditCardChargeItem(
                                charge = charge,
                                onEdit = { 
                                    editingCharge = charge
                                    showEditChargeDialog = true
                                }
                            )
                        }
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
    
    // Add Charge Dialog
    if (showAddChargeDialog) {
        AddChargeDialog(
            onDismiss = { showAddChargeDialog = false },
            onConfirm = { amount, description, date ->
                viewModel.addCharge(amount, description, date)
                showAddChargeDialog = false
            },
            viewModel = viewModel
        )
    }
    
    // Edit Charge Dialog
    if (showEditChargeDialog && editingCharge != null) {
        EditChargeDialog(
            charge = editingCharge!!,
            onDismiss = { 
                showEditChargeDialog = false
                editingCharge = null
            },
            onConfirm = { amount, description, date ->
                // Delete old and create new (simpler than updating)
                viewModel.deleteCharge(editingCharge!!)
                viewModel.addCharge(amount, description, date)
                showEditChargeDialog = false
                editingCharge = null
            },
            viewModel = viewModel
        )
    }
    
    // Delete Confirmation Dialog
    if (chargeToDelete != null) {
        ConfirmationDialog(
            title = "Eliminar Cargo",
            message = "¿Estás seguro de que deseas eliminar este cargo de ${formatCurrencyWithSymbol(chargeToDelete!!.amount)}?",
            confirmButtonText = "Eliminar",
            onConfirm = {
                viewModel.deleteCharge(chargeToDelete!!)
                chargeToDelete = null
            },
            onDismiss = { chargeToDelete = null },
            isDestructive = true
        )
    }
    
    // Make Payment Dialog
    if (showPaymentDialog) {
        MakePaymentDialog(
            accounts = accounts,
            currentBalance = totalPendingBalance,
            onDismiss = { showPaymentDialog = false },
            onConfirm = { amount, accountId, description ->
                viewModel.makePayment(amount, accountId, description)
                showPaymentDialog = false
            }
        )
    }
}

@Composable
fun CreditCardChargeItem(
    charge: CreditCardCharge,
    onEdit: () -> Unit
) {
    val isPaid = charge.status == "PAID"
    val statusColor = if (isPaid) IncomeGreen else Color(0xFFF57C00)
    val containerColor = if (isPaid) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
    
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = charge.description,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Límite: ${formatDate(charge.paymentDueDate)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatCurrencyWithSymbol(charge.amount),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isPaid) IncomeGreen else ExpenseRed
                    )
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(statusColor.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isPaid) "PAGADO" else "PENDIENTE",
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
            
            if (!isPaid && charge.paidAmount > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = (charge.paidAmount / charge.amount).toFloat(),
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = statusColor,
                    trackColor = statusColor.copy(alpha = 0.1f)
                )
                Text(
                    text = "Abonado: ${formatCurrencyWithSymbol(charge.paidAmount)}",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp),
                    color = statusColor
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChargeDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double, String, Long) -> Unit,
    viewModel: CreditCardViewModel
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    
    // Calculate billing cycle info for display
    val (cycleStart, cycleEnd) = viewModel.getBillingCycleInfo(selectedDate)
    val dueDate = viewModel.getPaymentDueDate(cycleEnd)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Cargo de Tarjeta") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Monto") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text("₡") }
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Fecha: ${formatDate(selectedDate)}")
                }
                
                // Billing cycle info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Información del Ciclo",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ciclo: ${formatDateShort(cycleStart)} - ${formatDateShort(cycleEnd)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = "Fecha límite de pago: ${formatDate(dueDate)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()
                    if (amountValue != null && amountValue > 0 && description.isNotBlank()) {
                        onConfirm(amountValue, description, selectedDate)
                    }
                },
                enabled = amount.toDoubleOrNull() != null && amount.toDoubleOrNull()!! > 0 && description.isNotBlank()
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
    
    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { utcMillis ->
                        // Fix UTC offset: DatePicker returns UTC midnight, we need local date
                        val calendar = java.util.Calendar.getInstance()
                        calendar.timeInMillis = utcMillis
                        calendar.set(java.util.Calendar.HOUR_OF_DAY, 12)
                        selectedDate = calendar.timeInMillis
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditChargeDialog(
    charge: CreditCardCharge,
    onDismiss: () -> Unit,
    onConfirm: (Double, String, Long) -> Unit,
    viewModel: CreditCardViewModel
) {
    var amount by remember { mutableStateOf(charge.amount.toString()) }
    var description by remember { mutableStateOf(charge.description) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(charge.purchaseDate) }
    
    // Calculate billing cycle info for display
    val (cycleStart, cycleEnd) = viewModel.getBillingCycleInfo(selectedDate)
    val dueDate = viewModel.getPaymentDueDate(cycleEnd)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Cargo de Tarjeta") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Monto") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text("₡") }
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Fecha: ${formatDate(selectedDate)}")
                }
                
                // Billing cycle info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Información del Ciclo",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ciclo: ${formatDateShort(cycleStart)} - ${formatDateShort(cycleEnd)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = "Fecha límite de pago: ${formatDate(dueDate)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()
                    if (amountValue != null && amountValue > 0 && description.isNotBlank()) {
                        onConfirm(amountValue, description, selectedDate)
                    }
                },
                enabled = amount.toDoubleOrNull() != null && amount.toDoubleOrNull()!! > 0 && description.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
    
    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { utcMillis ->
                        // Fix UTC offset
                        val calendar = java.util.Calendar.getInstance()
                        calendar.timeInMillis = utcMillis
                        calendar.set(java.util.Calendar.HOUR_OF_DAY, 12)
                        selectedDate = calendar.timeInMillis
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MakePaymentDialog(
    accounts: List<com.example.personalaccountant.data.Account>,
    currentBalance: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double, Int, String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("Abono Tarjeta de Crédito") }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: 0) }
    var expanded by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hacer Pago a Tarjeta") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Current balance display
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Saldo Actual",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = formatCurrencyWithSymbol(currentBalance),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Monto a Pagar") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text("₡") }
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Account selector
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = accounts.find { it.id == selectedAccountId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cuenta de Pago") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        accounts.forEach { account ->
                            DropdownMenuItem(
                                text = { Text("${account.name} (${formatCurrencyWithSymbol(account.currentBalance)})") },
                                onClick = {
                                    selectedAccountId = account.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                // Info message
                Text(
                    text = "El pago se aplicará a los cargos más antiguos primero",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()
                    if (amountValue != null && amountValue > 0 && selectedAccountId > 0) {
                        onConfirm(amountValue, selectedAccountId, description)
                    }
                },
                enabled = amount.toDoubleOrNull() != null && amount.toDoubleOrNull()!! > 0 && selectedAccountId > 0
            ) {
                Text("Pagar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
