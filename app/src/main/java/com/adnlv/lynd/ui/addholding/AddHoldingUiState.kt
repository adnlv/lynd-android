package com.adnlv.lynd.ui.addholding

import com.adnlv.lynd.data.db.BondEntity
import java.time.LocalDate

sealed interface FetchState {
    data object Idle : FetchState
    data object Loading : FetchState
    data class Success(val bond: BondEntity) : FetchState
    data class Error(val message: String) : FetchState
}

enum class PriceInputMode {
    PER_BOND,
    TOTAL
}

data class AddHoldingUiState(
    val editingHoldingId: Int? = null,
    val isinPrefix: String = "UA4000",
    val isinNumber: String = "",
    val availablePrefixes: List<String> = listOf("UA4000"),
    val isin: String = "UA4000",
    val quantity: String = "1",
    val pricePerBond: String = "",
    val totalPrice: String = "",
    val priceMode: PriceInputMode = PriceInputMode.PER_BOND,
    val purchaseDate: LocalDate = LocalDate.now(),
    val fetchState: FetchState = FetchState.Idle,
    val quantityError: String? = null,
    val priceError: String? = null,
    val suggestions: List<String> = emptyList(),
    val isDropdownExpanded: Boolean = false,
    val isPrefixDropdownExpanded: Boolean = false
)
