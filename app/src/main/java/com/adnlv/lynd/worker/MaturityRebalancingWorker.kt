package com.adnlv.lynd.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.adnlv.lynd.LyndApplication
import com.adnlv.lynd.R
import com.adnlv.lynd.domain.MaturityAlertEngine
import kotlinx.coroutines.flow.first
import java.math.BigDecimal

class MaturityRebalancingWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? LyndApplication ?: return Result.failure()
        val container = app.container

        val payoutRows = container.database.payoutDao().getAllPayoutRows().first()
        val catalogBonds = container.database.bondDao().getAllBonds().first()

        val currencies = listOf("UAH", "USD", "EUR")
        var triggeredAlertsCount = 0

        for (curr in currencies) {
            val threshold = when (curr) {
                "USD", "EUR" -> BigDecimal("1000")
                else -> BigDecimal("10000")
            }

            val alerts = MaturityAlertEngine.evaluateAlerts(
                payoutRows = payoutRows,
                currency = curr,
                threshold = threshold,
                catalogBonds = catalogBonds
            )

            if (alerts.isNotEmpty()) {
                triggeredAlertsCount += alerts.size
                showNotification(curr, alerts.size)
            }
        }

        return Result.success()
    }

    private fun showNotification(currency: String, alertsCount: Int) {
        val channelId = CHANNEL_ID
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Maturity Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifies about large bond maturities due within 30 days"
            }
            notificationManager.createNotificationChannel(channel)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Maturity Rebalancing Alert")
            .setContentText("$alertsCount large $currency redemption(s) due within 30 days. Lock in yields now.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(currency.hashCode(), notification)
    }

    companion object {
        const val CHANNEL_ID = "maturity_alerts"
        const val WORK_NAME = "MaturityRebalancingWork"
    }
}
