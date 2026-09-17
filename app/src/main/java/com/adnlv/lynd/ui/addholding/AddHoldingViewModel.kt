package com.adnlv.lynd.ui.addholding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.adnlv.lynd.data.db.BondEntity
import com.adnlv.lynd.data.db.HoldingDao
import com.adnlv.lynd.data.db.HoldingEntity
import com.adnlv.lynd.data.network.NbuRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate

sealed interface FetchState {
    data object Idle : FetchState
    data object Loading : FetchState
    data class Success(val bond: BondEntity) : FetchState
    data class Error(val message: String) : FetchState
}

data class AddHoldingUiState(
    val isin: String = "",
    val quantity: String = "",
    val totalPaidAmount: String = "",
    val purchaseDate: LocalDate = LocalDate.now(),
    val fetchState: FetchState = FetchState.Idle
)

class AddHoldingViewModel(
    private val nbuRepository: NbuRepository,
    private val holdingDao: HoldingDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddHoldingUiState())
    val uiState: StateFlow<AddHoldingUiState> = _uiState.asStateFlow()

    private val _saveSuccessEvent = MutableSharedFlow<Unit>()
    val saveSuccessEvent: SharedFlow<Unit> = _saveSuccessEvent.asSharedFlow()

    fun onIsinChanged(value: String) {
        _uiState.update { it.copy(isin = value, fetchState = FetchState.Idle) }
    }

    fun onQuantityChanged(value: String) {
        _uiState.update { it.copy(quantity = value) }
    }

    fun onTotalPaidAmountChanged(value: String) {
        _uiState.update { it.copy(totalPaidAmount = value) }
    }

    fun onPurchaseDateChanged(date: LocalDate) {
        _uiState.update { it.copy(purchaseDate = date) }
    }

    fun fetchBond() {
        val currentIsin = _uiState.value.isin.trim()
        if (currentIsin.isBlank()) {
            _uiState.update { it.copy(fetchState = FetchState.Error("Please enter an ISIN code")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(fetchState = FetchState.Loading) }
            val bond = nbuRepository.getLocalBond(currentIsin)
            if (bond != null) {
                _uiState.update { it.copy(fetchState = FetchState.Success(bond)) }
            } else {
                _uiState.update {
                    it.copy(fetchState = FetchState.Error("Bond with ISIN $currentIsin not found in local database"))
                }
            }
        }
    }

    fun saveHolding() {
        val state = _uiState.value
        val isin = state.isin.trim()
        val quantity = state.quantity.toIntOrNull()
        val totalPaid = try {
            BigDecimal(state.totalPaidAmount)
        } catch (_: Exception) {
            null
        }

        if (state.fetchState !is FetchState.Success) {
            _uiState.update { it.copy(fetchState = FetchState.Error("Please fetch a valid bond first")) }
            return
        }
        if (quantity == null || quantity <= 0) {
            _uiState.update { it.copy(fetchState = FetchState.Error("Quantity must be a positive integer")) }
            return
        }
        if (totalPaid == null || totalPaid <= BigDecimal.ZERO) {
            _uiState.update { it.copy(fetchState = FetchState.Error("Please enter a valid total paid amount")) }
            return
        }

        viewModelScope.launch {
            holdingDao.insertHolding(
                HoldingEntity(
                    isin = isin,
                    quantity = quantity,
                    totalPaidAmount = totalPaid,
                    purchaseDate = state.purchaseDate
                )
            )
            _saveSuccessEvent.emit(Unit)
        }
    }

    companion object {
        fun provideFactory(
            nbuRepository: NbuRepository,
            holdingDao: HoldingDao
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AddHoldingViewModel(nbuRepository, holdingDao) as T
            }
        }
    }
}
