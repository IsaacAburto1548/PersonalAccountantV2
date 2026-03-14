package com.example.personalaccountant.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.personalaccountant.data.repository.FinanceRepository
import com.example.personalaccountant.ui.components.BottomNavigationBar
import com.example.personalaccountant.ui.viewmodel.CreditCardViewModel
import com.example.personalaccountant.ui.viewmodel.CreditCardViewModelFactory
import com.example.personalaccountant.ui.viewmodel.DashboardViewModel
import com.example.personalaccountant.ui.viewmodel.DashboardViewModelFactory
import com.example.personalaccountant.ui.viewmodel.FixedTransactionViewModel
import com.example.personalaccountant.ui.viewmodel.FixedTransactionViewModelFactory
import com.example.personalaccountant.ui.viewmodel.TransactionViewModel
import com.example.personalaccountant.ui.viewmodel.TransactionViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainPagerScreen(
    navController: NavController,
    repository: FinanceRepository,
    initialPage: Int = 0
) {
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { 4 }
    )
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            val currentRoute = when (pagerState.currentPage) {
                0 -> "dashboard"
                1 -> "credit_card"
                2 -> "fixed_payments"
                3 -> "transaction_history"
                else -> "dashboard"
            }
            BottomNavigationBar(
                navController = navController,
                currentRoute = currentRoute,
                onNavigate = { route ->
                    val targetPage = when (route) {
                        "dashboard" -> 0
                        "credit_card" -> 1
                        "fixed_payments" -> 2
                        "transaction_history" -> 3
                        else -> 0
                    }
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(targetPage)
                    }
                }
            )
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondBoundsPageCount = 1 // Pre-load adjacent pages
        ) { page ->
            when (page) {
                0 -> {
                    val viewModel: DashboardViewModel = viewModel(
                        factory = DashboardViewModelFactory(repository)
                    )
                    DashboardScreen(navController, viewModel)
                }
                1 -> {
                    val viewModel: CreditCardViewModel = viewModel(
                        factory = CreditCardViewModelFactory(repository)
                    )
                    CreditCardScreen(navController, viewModel)
                }
                2 -> {
                    val viewModel: FixedTransactionViewModel = viewModel(
                        factory = FixedTransactionViewModelFactory(repository)
                    )
                    FixedPaymentsScreen(navController, viewModel)
                }
                3 -> {
                    val viewModel: TransactionViewModel = viewModel(
                        factory = TransactionViewModelFactory(repository)
                    )
                    TransactionHistoryScreen(navController, viewModel)
                }
            }
        }
    }
}
