package com.example.personalaccountant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.personalaccountant.data.Account
import com.example.personalaccountant.data.AppDatabase
import com.example.personalaccountant.data.repository.FinanceRepository
import com.example.personalaccountant.ui.screens.AddTransactionScreen
import com.example.personalaccountant.ui.screens.DashboardScreen
import com.example.personalaccountant.ui.screens.FixedPaymentsScreen
import com.example.personalaccountant.ui.theme.PersonalAccountantTheme
import com.example.personalaccountant.ui.viewmodel.DashboardViewModel
import com.example.personalaccountant.ui.viewmodel.DashboardViewModelFactory
import com.example.personalaccountant.ui.viewmodel.TransactionViewModel
import com.example.personalaccountant.ui.viewmodel.TransactionViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = FinanceRepository(database)

        // Seed initial data if empty
        CoroutineScope(Dispatchers.IO).launch {
            if (repository.allAccounts.first().isEmpty()) {
                repository.addAccount(Account(name = "Efectivo", currentBalance = 0.0, type = "CASH"))
                repository.addAccount(Account(name = "Tarjeta", currentBalance = 0.0, type = "CARD"))
            }
            
            // Generate fixed credit card charges automatically
            repository.generateFixedCreditCardCharges()
        }

        setContent {
            PersonalAccountantTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    // Define animation specs for 200ms transitions
                    val enterTransition = fadeIn(animationSpec = tween(200))
                    val exitTransition = fadeOut(animationSpec = tween(200))
                    
                    NavHost(
                        navController = navController, 
                        startDestination = "splash",
                        enterTransition = { enterTransition },
                        exitTransition = { exitTransition },
                        popEnterTransition = { enterTransition },
                        popExitTransition = { exitTransition }
                    ) {
                        composable("splash") {
                            com.example.personalaccountant.ui.screens.SplashScreen(navController)
                        }
                        composable("main_pager") {
                            com.example.personalaccountant.ui.screens.MainPagerScreen(
                                navController = navController,
                                repository = repository,
                                initialPage = 0
                            )
                        }
                        composable(
                            route = "add_transaction?transactionId={transactionId}",
                            arguments = listOf(navArgument("transactionId") { 
                                type = NavType.IntType
                                defaultValue = -1 
                            })
                        ) { backStackEntry ->
                            val transactionId = backStackEntry.arguments?.getInt("transactionId") ?: -1
                            val viewModel: TransactionViewModel = viewModel(
                                factory = TransactionViewModelFactory(repository)
                            )
                            AddTransactionScreen(navController, viewModel, transactionId)
                        }
                    }
                }
            }
        }
    }
}
