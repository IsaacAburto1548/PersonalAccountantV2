package com.example.personalaccountant.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import com.example.personalaccountant.utils.formatCurrency
import com.example.personalaccountant.utils.formatCurrencyWithSymbol
import com.example.personalaccountant.utils.formatDate
import com.example.personalaccountant.utils.formatDateShort
import com.example.personalaccountant.ui.components.ConfirmationDialog
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.personalaccountant.R
import com.example.personalaccountant.data.CreditCardCharge
import com.example.personalaccountant.ui.components.BottomNavigationBar
import com.example.personalaccountant.ui.theme.BorderGray
import com.example.personalaccountant.ui.viewmodel.CreditCardViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditCardScreen(
    navController: NavController,
    viewModel: CreditCardViewModel
) {
    val allCharges by viewModel.allCharges.collectAsState()
    val totalPendingBalance by viewModel.totalPendingBalance.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    
    var showAddChargeDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showEditChargeDialog by remember { mutableStateOf(false) }
    var editingCharge by remember { mutableStateOf<CreditCardCharge?>(null) }
    var chargeToDelete by remember { mutableStateOf<CreditCardCharge?>(null) }
    
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
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(navController, "credit_card")
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Total Balance Card (in RED)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE) // Light red background
                    ),
                    border = BorderStroke(2.dp, Color(0xFFD32F2F)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Saldo Total por Pagar",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFD32F2F),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = formatCurrencyWithSymbol(totalPendingBalance),
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F) // Red
                        )
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
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Hacer Pago")
                    }
                    
                    OutlinedButton(
                        onClick = { showAddChargeDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Agregar Cargo")
                    }
                }
            }
            
            // Charges Header
            item {
                Text(
                    text = "Cargos de Tarjeta",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            // Charges List
            if (allCharges.isEmpty()) {
                item {
                    Text(
                        text = "No hay cargos registrados",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                items(allCharges) { charge ->
                    CreditCardChargeItem(
                        charge = charge,
                        onEdit = { 
                            editingCharge = charge
                            showEditChargeDialog = true
                        },
                        onDelete = { chargeToDelete = charge }
                    )
                }
            }
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
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isPaid = charge.status == "PAID"
    
    // Status colors
    val backgroundColor = if (isPaid) Color(0xFFC8E6C9) else Color(0xFFFFF9C4) // Green or Yellow
    val textColor = if (isPaid) Color(0xFF2E7D32) else Color(0xFFF57C00) // Dark green or Orange
    val statusText = if (isPaid) "PAGADO" else "PENDIENTE"
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = charge.description,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Compra: ${formatDate(charge.purchaseDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Pago límite: ${formatDate(charge.paymentDueDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.8f)
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatCurrencyWithSymbol(charge.amount),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .background(textColor, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Show payment progress if partially paid
            if (!isPaid && charge.paidAmount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Pagado: ${formatCurrencyWithSymbol(charge.paidAmount)} / ${formatCurrencyWithSymbol(charge.amount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.7f)
                )
            }
            
            // Billing cycle info
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ciclo: ${formatDateShort(charge.billingCycleStart)} - ${formatDateShort(charge.billingCycleEnd)}",
                style = MaterialTheme.typography.bodySmall,
                color = textColor.copy(alpha = 0.6f)
            )
            
            // Edit and Delete buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onEdit,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Editar", style = MaterialTheme.typography.labelSmall)
                }
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFD32F2F)
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Eliminar", style = MaterialTheme.typography.labelSmall)
                }
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
                        containerColor = Color(0xFFE3F2FD)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Información del Ciclo",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ciclo: ${formatDateShort(cycleStart)} - ${formatDateShort(cycleEnd)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF1976D2)
                        )
                        Text(
                            text = "Fecha límite de pago: ${formatDate(dueDate)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF1976D2),
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
                        containerColor = Color(0xFFE3F2FD)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Información del Ciclo",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ciclo: ${formatDateShort(cycleStart)} - ${formatDateShort(cycleEnd)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF1976D2)
                        )
                        Text(
                            text = "Fecha límite de pago: ${formatDate(dueDate)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF1976D2),
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
                        containerColor = Color(0xFFFFEBEE)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Saldo Actual",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFD32F2F)
                        )
                        Text(
                            text = formatCurrencyWithSymbol(currentBalance),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
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
