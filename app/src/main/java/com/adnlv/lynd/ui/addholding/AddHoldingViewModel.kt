package com.adnlv.lynd.ui.addholding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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

class AddHoldingViewModel(
    private val nbuRepository: NbuRepository,
    private val holdingDao: HoldingDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddHoldingUiState())
    val uiState: StateFlow<AddHoldingUiState> = _uiState.asStateFlow()

    private val _saveSuccessEvent = MutableSharedFlow<Unit>()
    val saveSuccessEvent: SharedFlow<Unit> = _saveSuccessEvent.asSharedFlow()

    private var isinLookupJob: kotlinx.coroutines.Job? = null
    private var isinSearchJob: kotlinx.coroutines.Job? = null

    init {
        loadPrefixes()
        reset()
    }

    private fun loadPrefixes() {
        viewModelScope.launch {
            val prefixes = nbuRepository.getIsinPrefixes()
            _uiState.update { current ->
                val activePrefix = if (prefixes.contains(current.isinPrefix)) {
                    current.isinPrefix
                } else {
                    prefixes.firstOrNull() ?: "UA4000"
                }
                current.copy(
                    availablePrefixes = prefixes,
                    isinPrefix = activePrefix,
                    isin = activePrefix + current.isinNumber
                )
            }
        }
    }

    fun reset() {
        isinLookupJob?.cancel()
        val defaultPrefix = _uiState.value.isinPrefix.ifEmpty { "UA4000" }
        _uiState.update { current ->
            AddHoldingUiState(
                availablePrefixes = current.availablePrefixes.ifEmpty { listOf("UA4000") },
                isinPrefix = defaultPrefix,
                isinNumber = "",
                isin = defaultPrefix
            )
        }
        searchSuggestions(defaultPrefix)
    }

    fun onPrefixDropdownToggled(expanded: Boolean) {
        _uiState.update { it.copy(isPrefixDropdownExpanded = expanded) }
    }

    fun onIsinPrefixChanged(prefix: String) {
        val fullIsin = prefix + _uiState.value.isinNumber
        _uiState.update {
            it.copy(
                isinPrefix = prefix,
                isin = fullIsin,
                isPrefixDropdownExpanded = false,
                fetchState = FetchState.Idle
            )
        }
        searchSuggestions(fullIsin)
        checkAndFetchIsin(fullIsin)
    }

    fun onIsinNumberChanged(number: String) {
        val filtered = number.filter { it.isDigit() }.take(6)
        val fullIsin = _uiState.value.isinPrefix + filtered
        _uiState.update {
            it.copy(
                isinNumber = filtered,
                isin = fullIsin,
                fetchState = FetchState.Idle
            )
        }
        searchSuggestions(fullIsin)
        checkAndFetchIsin(fullIsin)
    }

    private fun checkAndFetchIsin(fullIsin: String) {
        isinLookupJob?.cancel()
        if (fullIsin.length != 12) {
            return
        }

        isinLookupJob = viewModelScope.launch {
            fetchBond(fullIsin)
        }
    }

    fun onIsinSelected(selectedIsin: String) {
        val trimmed = selectedIsin.trim()
        val matchingPrefix = _uiState.value.availablePrefixes.firstOrNull { trimmed.startsWith(it) }
            ?: trimmed.take(6)
        val numberSuffix = trimmed.removePrefix(matchingPrefix)

        _uiState.update {
            it.copy(
                isinPrefix = matchingPrefix,
                isinNumber = numberSuffix,
                isin = trimmed,
                isDropdownExpanded = false,
                fetchState = FetchState.Idle
            )
        }
        isinLookupJob?.cancel()
        isinLookupJob = viewModelScope.launch {
            fetchBond(trimmed)
        }
    }

    fun onDismissDropdown() {
        _uiState.update { it.copy(isDropdownExpanded = false, isPrefixDropdownExpanded = false) }
    }

    fun onIsinFieldTapped() {
        val query = _uiState.value.isin.trim()
        searchSuggestions(query)
    }

    private fun searchSuggestions(query: String) {
        isinSearchJob?.cancel()
        if (query.isBlank() || query.length >= 12) {
            _uiState.update { it.copy(suggestions = emptyList(), isDropdownExpanded = false) }
            return
        }

        isinSearchJob = viewModelScope.launch {
            val results = nbuRepository.searchMatchingIsins(query)
            _uiState.update {
                it.copy(
                    suggestions = results,
                    isDropdownExpanded = results.isNotEmpty()
                )
            }
        }
    }

    fun onQuantityChanged(value: String) {
        _uiState.update { it.copy(quantity = value, quantityError = null) }
    }

    fun incrementQuantity() {
        val current = _uiState.value.quantity.toIntOrNull() ?: 0
        _uiState.update { it.copy(quantity = (current + 1).toString(), quantityError = null) }
    }

    fun decrementQuantity() {
        val current = _uiState.value.quantity.toIntOrNull() ?: 1
        if (current > 1) {
            _uiState.update { it.copy(quantity = (current - 1).toString(), quantityError = null) }
        }
    }

    fun onPriceModeChanged(mode: PriceInputMode) {
        _uiState.update {
            it.copy(
                priceMode = mode,
                priceError = null
            )
        }
    }

    fun onPricePerBondChanged(value: String) {
        _uiState.update {
            it.copy(
                pricePerBond = value,
                totalPrice = if (value.isNotEmpty()) "" else it.totalPrice,
                priceMode = PriceInputMode.PER_BOND,
                priceError = null
            )
        }
    }

    fun onTotalPriceChanged(value: String) {
        _uiState.update {
            it.copy(
                totalPrice = value,
                pricePerBond = if (value.isNotEmpty()) "" else it.pricePerBond,
                priceMode = PriceInputMode.TOTAL,
                priceError = null
            )
        }
    }

    fun onPurchaseDateChanged(date: LocalDate) {
        _uiState.update { it.copy(purchaseDate = date) }
    }

    fun initializeForEdit(
        holdingId: Int,
        isin: String,
        quantity: Int,
        pricePerBond: BigDecimal,
        purchaseDate: LocalDate
    ) {
        val trimmed = isin.trim()
        val matchingPrefix = _uiState.value.availablePrefixes.firstOrNull { trimmed.startsWith(it) }
            ?: trimmed.take(6)
        val numberSuffix = trimmed.removePrefix(matchingPrefix)

        _uiState.update {
            it.copy(
                editingHoldingId = holdingId,
                isinPrefix = matchingPrefix,
                isinNumber = numberSuffix,
                isin = trimmed,
                quantity = quantity.toString(),
                pricePerBond = pricePerBond.toPlainString(),
                totalPrice = pricePerBond.multiply(BigDecimal(quantity)).toPlainString(),
                priceMode = PriceInputMode.PER_BOND,
                purchaseDate = purchaseDate,
                fetchState = FetchState.Idle,
                quantityError = null,
                priceError = null
            )
        }
        isinLookupJob?.cancel()
        if (trimmed.length == 12) {
            isinLookupJob = viewModelScope.launch {
                fetchBond(trimmed)
            }
        }
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

        var hasError = false
        if (state.fetchState !is FetchState.Success) {
            _uiState.update { it.copy(fetchState = FetchState.Error("Please fetch a valid bond first")) }
            hasError = true
        }
        if (quantity == null || quantity <= 0) {
            _uiState.update { it.copy(quantityError = "Quantity must be a positive integer") }
            hasError = true
        }

        var pricePerBond: BigDecimal? = null
        var totalPaid: BigDecimal? = null

        when (state.priceMode) {
            PriceInputMode.PER_BOND -> {
                val parsed = try {
                    BigDecimal(state.pricePerBond)
                } catch (_: Exception) {
                    null
                }
                if (parsed == null || parsed <= BigDecimal.ZERO) {
                    _uiState.update { it.copy(priceError = "Please enter a valid price per bond") }
                    hasError = true
                } else {
                    pricePerBond = parsed
                    if (quantity != null && quantity > 0) {
                        totalPaid = parsed.multiply(BigDecimal(quantity))
                    }
                }
            }
            PriceInputMode.TOTAL -> {
                val parsed = try {
                    BigDecimal(state.totalPrice)
                } catch (_: Exception) {
                    null
                }
                if (parsed == null || parsed <= BigDecimal.ZERO) {
                    _uiState.update { it.copy(priceError = "Please enter a valid total price") }
                    hasError = true
                } else {
                    totalPaid = parsed
                    if (quantity != null && quantity > 0) {
                        pricePerBond = parsed.divide(BigDecimal(quantity), 2, java.math.RoundingMode.HALF_UP)
                    }
                }
            }
        }

        if (hasError || quantity == null || pricePerBond == null || totalPaid == null) {
            return
        }

        viewModelScope.launch {
            val editingId = state.editingHoldingId
            if (editingId != null) {
                holdingDao.updateHolding(
                    HoldingEntity(
                        id = editingId,
                        isin = isin,
                        quantity = quantity,
                        pricePerBond = pricePerBond,
                        totalPaidAmount = totalPaid,
                        purchaseDate = state.purchaseDate
                    )
                )
            } else {
                holdingDao.insertHolding(
                    HoldingEntity(
                        isin = isin,
                        quantity = quantity,
                        pricePerBond = pricePerBond,
                        totalPaidAmount = totalPaid,
                        purchaseDate = state.purchaseDate
                    )
                )
            }
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
