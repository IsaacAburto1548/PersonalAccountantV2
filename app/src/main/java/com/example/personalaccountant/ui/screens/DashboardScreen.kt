package com.example.personalaccountant.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButtonDefaults
import com.example.personalaccountant.R
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.IconButton
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import android.content.Intent
import android.net.Uri
import com.example.personalaccountant.ui.components.AccountCard
import com.example.personalaccountant.ui.components.BottomNavigationBar
import com.example.personalaccountant.ui.components.SpendingPieChart
import com.example.personalaccountant.ui.components.EmptyStateView
import com.example.personalaccountant.ui.theme.BorderGray
import com.example.personalaccountant.ui.viewmodel.DashboardUiEvent
import com.example.personalaccountant.ui.viewmodel.DashboardViewModel
import com.example.personalaccountant.utils.formatCurrencyWithSymbol
import com.example.personalaccountant.utils.formatDate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import com.example.personalaccountant.ui.theme.ExpenseRed
import com.example.personalaccountant.ui.theme.IncomeGreen
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FloatingActionButtonDefaults.elevation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel
) {
    val totalBalance by viewModel.totalBalance.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val recentTransactions by viewModel.recentTransactions.collectAsStateWithLifecycle()
    val spendingByCategory by viewModel.spendingByCategory.collectAsStateWithLifecycle()
    
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle UI Events
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is DashboardUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is DashboardUiEvent.PdfGenerated -> {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        event.file
                    )
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Abrir Informe"))
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
                        Text("Financify", color = Color.White, fontWeight = FontWeight.Black)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.exportMonthlyReport() }) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Exportar PDF", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(navController, "dashboard")
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_transaction") },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Transacción", tint = Color.White)
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Premium Total Balance Card
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
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                    )
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Saldo Total Disponible",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatCurrencyWithSymbol(totalBalance),
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Cuentas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            items(accounts) { account ->
                AccountCard(account = account)
            }
            
            if (accounts.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "Sin cuentas",
                        subtitle = "Agrega una cuenta para comenzar",
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }

            // Spending pie chart
            if (spendingByCategory.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Gastos por Categoría",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            SpendingPieChart(
                                categories = spendingByCategory,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Recientes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(recentTransactions.take(10)) { transaction ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { navController.navigate("add_transaction?transactionId=${transaction.id}") },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (transaction.type == "INCOME") IncomeGreen.copy(alpha = 0.1f)
                                        else ExpenseRed.copy(alpha = 0.1f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (transaction.type == "INCOME") Icons.Default.Add else Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = if (transaction.type == "INCOME") IncomeGreen else ExpenseRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = transaction.description,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = formatDate(transaction.date),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                        Text(
                            text = "${if (transaction.type == "INCOME") "+" else "-"} ${formatCurrencyWithSymbol(transaction.amount)}",
                            color = if (transaction.type == "INCOME") IncomeGreen else ExpenseRed,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(64.dp)) }
        }
    }
}
