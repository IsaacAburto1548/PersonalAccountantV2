package com.example.personalaccountant.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.personalaccountant.ui.viewmodel.AmortizationSimulatorViewModel
import com.example.personalaccountant.utils.AmortizationCalculator
import com.example.personalaccountant.utils.AmortizationRow
import com.example.personalaccountant.utils.formatCurrencyWithSymbol
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmortizationSimulatorScreen(
    navController: NavController,
    viewModel: AmortizationSimulatorViewModel = hiltViewModel()
) {
    val loanAmountStr by viewModel.loanAmountStr.collectAsStateWithLifecycle()
    val interestRateStr by viewModel.interestRateStr.collectAsStateWithLifecycle()
    val termMonthsStr by viewModel.termMonthsStr.collectAsStateWithLifecycle()
    val monthlySalaryStr by viewModel.monthlySalaryStr.collectAsStateWithLifecycle()

    val loanAmount = loanAmountStr.toDoubleOrNull() ?: 0.0
    val interestRate = (interestRateStr.toDoubleOrNull() ?: 0.0) / 100.0
    val termMonths = termMonthsStr.toIntOrNull() ?: 0
    val monthlySalary = monthlySalaryStr.toDoubleOrNull() ?: 0.0

    val amortizationSchedule = remember(loanAmount, interestRate, termMonths, monthlySalary) {
        AmortizationCalculator.calculateAmortization(
            loanAmount = loanAmount,
            annualInterestRate = interestRate,
            termMonths = termMonths,
            monthlySalary = monthlySalary
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Simulador de Amortización", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Seccion 1: Inputs
            item {
                InputsSection(
                    loanAmountStr = loanAmountStr,
                    onLoanAmountChange = { viewModel.updateLoanAmount(it) },
                    interestRateStr = interestRateStr,
                    onInterestRateChange = { viewModel.updateInterestRate(it) },
                    termMonthsStr = termMonthsStr,
                    onTermMonthsChange = { viewModel.updateTermMonths(it) },
                    monthlySalaryStr = monthlySalaryStr,
                    onMonthlySalaryChange = { viewModel.updateMonthlySalary(it) }
                )
            }

            if (amortizationSchedule.isNotEmpty()) {
                // Seccion 2: Tabla de amortización
                item {
                    Text(
                        text = "Tabla de Amortización",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Mes", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.5f))
                        Text("Cuota", color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                        Text("Interés", color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                        Text("Capital", color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                        Text("Saldo", color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                    }
                }

                items(amortizationSchedule) { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (row.month % 2 == 0) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${row.month}", modifier = Modifier.weight(0.5f), fontSize = 12.sp)
                        Text(formatCurrencyWithSymbol(row.quota), textAlign = TextAlign.End, modifier = Modifier.weight(1f), fontSize = 12.sp)
                        Text(formatCurrencyWithSymbol(row.interestPayment), textAlign = TextAlign.End, modifier = Modifier.weight(1f), fontSize = 12.sp, color = Color(0xFFF44336))
                        Text(formatCurrencyWithSymbol(row.principalPayment), textAlign = TextAlign.End, modifier = Modifier.weight(1f), fontSize = 12.sp, color = Color(0xFF4CAF50))
                        Text(formatCurrencyWithSymbol(row.remainingBalance), textAlign = TextAlign.End, modifier = Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }

                // Seccion 3: Graficos
                item {
                    Text(
                        text = "Composición Mensual de la Cuota",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                    StackedBarChart(
                        data = amortizationSchedule,
                        color1 = Color(0xFFF44336), // Red for Interest
                        color2 = Color(0xFF4CAF50), // Green for Capital
                        legend1 = "Interés",
                        legend2 = "Capital",
                        value1Selector = { it.interestPayment },
                        value2Selector = { it.principalPayment },
                        totalSelector = { it.quota },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    )
                }

                item {
                    Text(
                        text = "Disponibilidad del Salario Mensual",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    StackedBarChart(
                        data = amortizationSchedule,
                        color1 = Color(0xFFFF9800), // Orange for Quota
                        color2 = Color(0xFF2196F3), // Blue for Available Salary
                        legend1 = "Cuota Préstamo",
                        legend2 = "Salario Disponible",
                        value1Selector = { it.quota },
                        value2Selector = { it.availableSalary },
                        totalSelector = { monthlySalary },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun InputsSection(
    loanAmountStr: String,
    onLoanAmountChange: (String) -> Unit,
    interestRateStr: String,
    onInterestRateChange: (String) -> Unit,
    termMonthsStr: String,
    onTermMonthsChange: (String) -> Unit,
    monthlySalaryStr: String,
    onMonthlySalaryChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Variables del Préstamo y Salario",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = loanAmountStr,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) onLoanAmountChange(it) },
                    label = { Text("Monto (₡)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = monthlySalaryStr,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) onMonthlySalaryChange(it) },
                    label = { Text("Salario (₡)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = interestRateStr,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) onInterestRateChange(it) },
                    label = { Text("Tasa Anual (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = termMonthsStr,
                    onValueChange = { if (it.isEmpty() || (it.toIntOrNull() != null && it.length <= 4)) onTermMonthsChange(it) },
                    label = { Text("Plazo (Meses)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
        }
    }
}

@Composable
fun StackedBarChart(
    data: List<AmortizationRow>,
    color1: Color,
    color2: Color,
    legend1: String,
    legend2: String,
    value1Selector: (AmortizationRow) -> Double,
    value2Selector: (AmortizationRow) -> Double,
    totalSelector: (AmortizationRow) -> Double,
    modifier: Modifier = Modifier
) {
    var animationPlayed by remember { mutableStateOf(false) }
    
    LaunchedEffect(data) {
        animationPlayed = false
        kotlinx.coroutines.delay(50)
        animationPlayed = true
    }

    val animationProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "ChartAnimation"
    )

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Legends
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(2.dp)).background(color1))
                Spacer(modifier = Modifier.width(4.dp))
                Text(legend1, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(16.dp))
                Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(2.dp)).background(color2))
                Spacer(modifier = Modifier.width(4.dp))
                Text(legend2, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            }

            if (data.isEmpty()) return@Column

            val maxTotal = data.maxOfOrNull { totalSelector(it) } ?: 1.0
            
            Canvas(modifier = Modifier.fillMaxSize().weight(1f)) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                
                // Show a maximum of 24 bars to avoid overcrowding, if more, space them
                val displayData = if (data.size > 24) {
                    val step = data.size / 24
                    data.filterIndexed { index, _ -> index % step == 0 || index == data.size - 1 }
                } else {
                    data
                }

                val barWidth = (canvasWidth / (displayData.size * 1.5f)).coerceAtMost(40.dp.toPx())
                val spaceBetweenBars = (canvasWidth - (barWidth * displayData.size)) / max(1, displayData.size)

                displayData.forEachIndexed { index, row ->
                    val val1 = value1Selector(row).toFloat()
                    val val2 = value2Selector(row).toFloat()
                    val total = maxTotal.toFloat()

                    val barHeight1 = (val1 / total) * canvasHeight * animationProgress
                    val barHeight2 = (val2 / total) * canvasHeight * animationProgress

                    val startX = (index * (barWidth + spaceBetweenBars)) + spaceBetweenBars / 2

                    // Draw Bottom Bar (Value 1)
                    drawRoundRect(
                        color = color1,
                        topLeft = Offset(startX, canvasHeight - barHeight1),
                        size = Size(barWidth, barHeight1),
                        cornerRadius = CornerRadius(if(barHeight2 <= 0) 4.dp.toPx() else 0f, if(barHeight2 <= 0) 4.dp.toPx() else 0f)
                    )

                    // Draw Top Bar (Value 2)
                    if (barHeight2 > 0) {
                        drawRoundRect(
                            color = color2,
                            topLeft = Offset(startX, canvasHeight - barHeight1 - barHeight2),
                            size = Size(barWidth, barHeight2),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}
