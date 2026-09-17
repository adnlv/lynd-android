package com.adnlv.lynd.ui.holdings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.adnlv.lynd.data.db.HoldingDao
import com.adnlv.lynd.data.network.NbuRepository
import com.adnlv.lynd.domain.HoldingItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HoldingsViewModel(
    private val holdingDao: HoldingDao,
    private val nbuRepository: NbuRepository
) : ViewModel() {

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    init {
        checkAndSyncCatalogue()
    }

    private fun checkAndSyncCatalogue() {
        viewModelScope.launch {
            if (nbuRepository.isDataStale()) {
                _isSyncing.value = true
                try {
                    nbuRepository.syncAllBonds()
                } finally {
                    _isSyncing.value = false
                }
            }
        }
    }

    val holdings: StateFlow<List<HoldingItem>> = holdingDao.getAllHoldings()
        .map { list ->
            list.map { item ->
                HoldingItem(
                    id = item.id,
                    isin = item.isin,
                    bondName = item.bondName.ifBlank { item.isin },
                    quantity = item.quantity,
                    totalPaidAmount = item.totalPaidAmount,
                    purchaseDate = item.purchaseDate
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
            holdingDao.deleteHolding(id)
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
