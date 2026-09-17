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
    val isin: String = "UA4000",
    val quantity: String = "1",
    val pricePerBond: String = "",
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

    private var isinLookupJob: kotlinx.coroutines.Job? = null

    fun onIsinChanged(value: String) {
        val trimmed = value.trim()
        _uiState.update { it.copy(isin = value, fetchState = FetchState.Idle) }

        isinLookupJob?.cancel()
        if (trimmed.length != 12) {
            return
        }

        isinLookupJob = viewModelScope.launch {
            fetchBond(trimmed)
        }
    }

    fun onQuantityChanged(value: String) {
        _uiState.update { it.copy(quantity = value) }
    }

    fun incrementQuantity() {
        val current = _uiState.value.quantity.toIntOrNull() ?: 0
        _uiState.update { it.copy(quantity = (current + 1).toString()) }
    }

    fun decrementQuantity() {
        val current = _uiState.value.quantity.toIntOrNull() ?: 1
        if (current > 1) {
            _uiState.update { it.copy(quantity = (current - 1).toString()) }
        }
    }

    fun onPricePerBondChanged(value: String) {
        _uiState.update { it.copy(pricePerBond = value) }
    }

    fun onPurchaseDateChanged(date: LocalDate) {
        _uiState.update { it.copy(purchaseDate = date) }
    }

    private suspend fun fetchBond(isinToFetch: String) {
        _uiState.update { it.copy(fetchState = FetchState.Loading) }
        val bond = nbuRepository.getLocalBond(isinToFetch)
        if (bond != null) {
            _uiState.update { it.copy(fetchState = FetchState.Success(bond)) }
        } else {
            _uiState.update {
                it.copy(fetchState = FetchState.Error("Bond with ISIN $isinToFetch not found in local database"))
            }
        }
    }

    fun saveHolding() {
        val state = _uiState.value
        val isin = state.isin.trim()
        val quantity = state.quantity.toIntOrNull()
        val pricePerBond = try {
            BigDecimal(state.pricePerBond)
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
        if (pricePerBond == null || pricePerBond <= BigDecimal.ZERO) {
            _uiState.update { it.copy(fetchState = FetchState.Error("Please enter a valid price per bond")) }
            return
        }

        val totalPaid = pricePerBond.multiply(BigDecimal(quantity))

        viewModelScope.launch {
            holdingDao.insertHolding(
                HoldingEntity(
                    isin = isin,
                    quantity = quantity,
                    pricePerBond = pricePerBond,
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
