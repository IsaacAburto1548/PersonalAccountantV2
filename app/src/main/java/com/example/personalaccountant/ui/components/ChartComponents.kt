package com.example.personalaccountant.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.personalaccountant.ui.viewmodel.SpendingCategory
import com.example.personalaccountant.utils.formatCurrencyWithSymbol

private val ChartColors = listOf(
    Color(0xFF6200EE), // Deep Purple
    Color(0xFF03DAC6), // Teal
    Color(0xFFBB86FC), // Light Purple
    Color(0xFF018786), // Dark Teal
    Color(0xFFFF0266), // Pinkish Red
)

@Composable
fun SpendingPieChart(
    categories: List<SpendingCategory>,
    modifier: Modifier = Modifier
) {
    if (categories.isEmpty()) return

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(categories) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(800))
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Gastos por Categoría",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Donut chart
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(120.dp)) {
                    var startAngle = -90f
                    val stroke = Stroke(width = 28f)
                    val inset = 14f
                    val rectSize = size.minDimension - inset * 2

                    categories.forEachIndexed { i, cat ->
                        val sweep = 360f * cat.percentage * animProgress.value
                        drawArc(
                            color = ChartColors[i % ChartColors.size],
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = Size(rectSize, rectSize),
                            style = stroke
                        )
                        startAngle += sweep
                    }
                }
                
                val totalAmount = categories.sumOf { it.amount }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = formatCurrencyWithSymbol(totalAmount),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Legend
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                categories.forEachIndexed { i, cat ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape),
                            color = ChartColors[i % ChartColors.size]
                        ) {}
                        Column {
                            Text(
                                text = cat.category,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = formatCurrencyWithSymbol(cat.amount),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ComparativeBarChart(
    income: Double,
    expense: Double,
    modifier: Modifier = Modifier
) {
    val total = (income + expense).takeIf { it > 0 } ?: 1.0
    val incomePercentage = (income / total).toFloat()
    val expensePercentage = (expense / total).toFloat()

    val animIncome = remember { Animatable(0f) }
    val animExpense = remember { Animatable(0f) }

    LaunchedEffect(income, expense) {
        animIncome.animateTo(incomePercentage, animationSpec = tween(1000, delayMillis = 100))
        animExpense.animateTo(expensePercentage, animationSpec = tween(1000, delayMillis = 300))
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Análisis de Flujo",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Income Bar
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Ingresos", modifier = Modifier.width(70.dp), style = MaterialTheme.typography.labelMedium)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(16.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animIncome.value.coerceIn(0f, 1f))
                        .height(16.dp)
                        .background(com.example.personalaccountant.ui.theme.IncomeGreen, androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                )
            }
            Text(
                text = formatCurrencyWithSymbol(income),
                modifier = Modifier.width(84.dp),
                style = MaterialTheme.typography.labelSmall,
                color = com.example.personalaccountant.ui.theme.IncomeGreen,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Expense Bar
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Egresos", modifier = Modifier.width(70.dp), style = MaterialTheme.typography.labelMedium)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(16.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animExpense.value.coerceIn(0f, 1f))
                        .height(16.dp)
                        .background(com.example.personalaccountant.ui.theme.ExpenseRed, androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                )
            }
            Text(
                text = formatCurrencyWithSymbol(expense),
                modifier = Modifier.width(84.dp),
                style = MaterialTheme.typography.labelSmall,
                color = com.example.personalaccountant.ui.theme.ExpenseRed,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }
    }
}
