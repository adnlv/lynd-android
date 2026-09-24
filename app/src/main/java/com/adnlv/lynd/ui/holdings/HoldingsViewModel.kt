package com.adnlv.lynd.ui.holdings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.adnlv.lynd.data.TestPortfolioData
import com.adnlv.lynd.data.db.BondDao
import com.adnlv.lynd.data.db.HoldingDao
import com.adnlv.lynd.data.network.NbuRepository
import com.adnlv.lynd.data.db.HoldingEntity
import com.adnlv.lynd.domain.HoldingGroup
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
    private val nbuRepository: NbuRepository,
    private val bondDao: BondDao
) : ViewModel() {

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError.asStateFlow()

    private val _isinPrefixes = MutableStateFlow<List<String>>(listOf("UA4000"))
    val isinPrefixes: StateFlow<List<String>> = _isinPrefixes.asStateFlow()

    private var recentlyDeletedHolding: HoldingEntity? = null

    init {
        checkAndSyncCatalogue()
        loadIsinPrefixes()
    }

    private fun loadIsinPrefixes() {
        viewModelScope.launch {
            try {
                val prefixes = bondDao.getDistinctIsinPrefixes()
                if (prefixes.isNotEmpty()) {
                    val defaultPrefix = "UA4000"
                    val orderedPrefixes = if (prefixes.contains(defaultPrefix)) {
                        listOf(defaultPrefix) + prefixes.filter { it != defaultPrefix }
                    } else {
                        prefixes
                    }
                    _isinPrefixes.value = orderedPrefixes
                }
            } catch (_: Exception) {
            }
        }
    }

    suspend fun searchBonds(query: String): List<String> {
        return bondDao.searchBondsByIsin(query)
    }

    fun saveHolding(holding: HoldingEntity, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            holdingDao.insertHolding(holding)
            onComplete?.invoke()
        }
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
        com.adnlv.lynd.domain.PortfolioCalculator.mapHoldingsWithPayments(holdingsList, paymentsList)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val groupedHoldings: StateFlow<List<HoldingGroup>> = holdings
        .map { list ->
            list.groupBy { it.isin }
                .map { (isin, items) ->
                    HoldingGroup(
                        isin = isin,
                        currency = items.firstOrNull()?.currency ?: "",
                        totalQuantity = items.sumOf { it.quantity },
                        items = items
                    )
                }
        }
        .stateIn(
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

    fun loadTestPortfolio() {
        viewModelScope.launch {
            TestPortfolioData.seed(bondDao, holdingDao)
        }
    }

    companion object {
        fun provideFactory(
            holdingDao: HoldingDao,
            nbuRepository: NbuRepository,
            bondDao: BondDao
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HoldingsViewModel(holdingDao, nbuRepository, bondDao) as T
                }
            }
    }
}
