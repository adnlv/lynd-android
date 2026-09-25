package com.adnlv.lynd.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object MaturityMonitoringScheduler {

    fun scheduleDailyCheck(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val dailyWorkRequest = PeriodicWorkRequestBuilder<MaturityRebalancingWorker>(
            1, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            MaturityRebalancingWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            dailyWorkRequest
        )
    }
}
