package com.adnlv.lynd

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class LyndApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        triggerBondSyncIfStale()
        com.adnlv.lynd.worker.MaturityMonitoringScheduler.scheduleDailyCheck(this)
    }

    private fun triggerBondSyncIfStale() {
        applicationScope.launch {
            if (container.nbuRepository.isDataStale()) {
                container.nbuRepository.syncAllBonds()
            }
        }
    }
}
