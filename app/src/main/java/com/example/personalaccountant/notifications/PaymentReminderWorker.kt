package com.example.personalaccountant.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.personalaccountant.R
import com.example.personalaccountant.data.repository.FinanceRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.Calendar

@HiltWorker
class PaymentReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: FinanceRepository
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "payment_reminders"
        const val NOTIFICATION_ID = 1001
    }

    override suspend fun doWork(): Result {
        val charges = repository.allCreditCardCharges.first()
        val pendingCharges = charges.filter { charge: com.example.personalaccountant.data.CreditCardCharge -> charge.status == "PENDING" }
        
        if (pendingCharges.isEmpty()) return Result.success()

        val calendar = Calendar.getInstance()
        val today = calendar.timeInMillis
        
        // Find charges due in the next 3 days
        val dueSoon = pendingCharges.filter { charge: com.example.personalaccountant.data.CreditCardCharge ->
            val diff = charge.paymentDueDate - today
            diff in 0..(3 * 24 * 60 * 60 * 1000)
        }

        if (dueSoon.isNotEmpty()) {
            showNotification(
                "Recordatorio de Pago",
                "Tienes ${dueSoon.size} pagos de tarjeta de crédito próximos a vencer."
            )
        }

        return Result.success()
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recordatorios de Pago",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones para pagos próximos a vencer"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_financify)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
