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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.personalaccountant.notifications.PaymentReminderWorker
import com.example.personalaccountant.ui.screens.AddTransactionScreen
import com.example.personalaccountant.ui.screens.SplashScreen
import com.example.personalaccountant.ui.screens.MainPagerScreen
import com.example.personalaccountant.ui.theme.PersonalAccountantTheme
import dagger.hilt.android.AndroidEntryPoint
import com.example.personalaccountant.data.repository.FinanceRepository
import com.example.personalaccountant.data.prefs.PreferenceManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import javax.inject.Inject
import java.util.concurrent.TimeUnit
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var repository: FinanceRepository

    @Inject
    lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        requestNotificationPermission()
        schedulePaymentReminders()

        setContent {
            val isDarkMode by preferenceManager.isDarkMode.collectAsState(initial = false)

            PersonalAccountantTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

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
                            SplashScreen(navController)
                        }
                        composable("main_pager") {
                            MainPagerScreen(
                                navController = navController,
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
                            AddTransactionScreen(navController, transactionId)
                        }
                    }
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                registerForActivityResult(ActivityResultContracts.RequestPermission()) {}.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }

    private fun schedulePaymentReminders() {
        val workRequest = PeriodicWorkRequestBuilder<PaymentReminderWorker>(
            24, TimeUnit.HOURS
        ).setInitialDelay(1, TimeUnit.HOURS) // Simple delay for first run
         .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "payment_reminders",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
