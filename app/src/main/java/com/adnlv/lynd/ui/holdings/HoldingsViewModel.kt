package com.adnlv.lynd.ui.holdings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.adnlv.lynd.data.db.HoldingDao
import com.adnlv.lynd.data.network.NbuRepository
import com.adnlv.lynd.data.db.HoldingEntity
import com.adnlv.lynd.domain.HoldingItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

class HoldingsViewModel(
    private val holdingDao: HoldingDao,
    private val nbuRepository: NbuRepository
) : ViewModel() {

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError.asStateFlow()

    private var recentlyDeletedHolding: HoldingEntity? = null

    init {
        checkAndSyncCatalogue()
    }

    private fun checkAndSyncCatalogue() {
        viewModelScope.launch {
            if (nbuRepository.isDataStale()) {
                performSync()
            }
        }
    }

    fun retrySync() {
        viewModelScope.launch {
            performSync()
        }
    }

    private suspend fun performSync() {
        if (_isSyncing.value) return
        _isSyncing.value = true
        _syncError.value = null
        try {
            val result = nbuRepository.syncAllBonds()
            result.fold(
                onSuccess = {
                    _syncError.value = null
                },
                onFailure = { error ->
                    _syncError.value = error.localizedMessage ?: "Synchronization failed"
                }
            )
        } catch (e: Exception) {
            _syncError.value = e.localizedMessage ?: "Synchronization failed"
        } finally {
            _isSyncing.value = false
        }
    }

    val holdings: StateFlow<List<HoldingItem>> = combine(
        holdingDao.getAllHoldings(),
        holdingDao.getPaymentsForHoldings()
    ) { holdingsList, paymentsList ->
        val paymentsByIsin = paymentsList.groupBy { it.bondIsin }

        holdingsList.map { item ->
            val paymentsForHolding = paymentsByIsin[item.isin].orEmpty()
                .filter { !it.payDate.isBefore(item.purchaseDate) }

            val totalPayout = paymentsForHolding.fold(BigDecimal.ZERO) { acc, payment ->
                acc.add(payment.payVal.multiply(BigDecimal.valueOf(item.quantity.toLong())))
            }

            val profitAmount = totalPayout.subtract(item.totalPaidAmount)

            val profitPercent = if (item.totalPaidAmount > BigDecimal.ZERO) {
                profitAmount.multiply(BigDecimal("100"))
                    .divide(item.totalPaidAmount, 4, RoundingMode.HALF_UP)
            } else {
                BigDecimal.ZERO
            }

            HoldingItem(
                id = item.id,
                isin = item.isin,
                bondName = item.bondName.ifBlank { item.isin },
                quantity = item.quantity,
                pricePerBond = item.pricePerBond,
                totalPaidAmount = item.totalPaidAmount,
                purchaseDate = item.purchaseDate,
                currency = item.currency,
                couponRate = item.couponRate,
                totalPayoutAmount = totalPayout,
                totalProfitAmount = profitAmount,
                profitPercentage = profitPercent
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun deleteHolding(id: Int) {
        viewModelScope.launch {
            val entity = holdingDao.getHoldingById(id)
            if (entity != null) {
                recentlyDeletedHolding = entity
                holdingDao.deleteHolding(id)
            }
        }
    }

    fun restoreHolding() {
        val holdingToRestore = recentlyDeletedHolding ?: return
        viewModelScope.launch {
            holdingDao.insertHolding(holdingToRestore)
            recentlyDeletedHolding = null
        }
    }

    companion object {
        fun provideFactory(
            holdingDao: HoldingDao,
            nbuRepository: NbuRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HoldingsViewModel(holdingDao, nbuRepository) as T
                }
            }
    }
}
