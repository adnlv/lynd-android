package com.adnlv.lynd.ui.payouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.adnlv.lynd.data.db.PayoutDao
import com.adnlv.lynd.domain.PayoutItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.math.BigDecimal

data class PayoutsUiState(
    val availableCurrencies: List<String> = emptyList(),
    val selectedCurrency: String = "",
    val payouts: List<PayoutItem> = emptyList()
)

class PayoutsViewModel(
    payoutDao: PayoutDao
) : ViewModel() {

    private val _selectedCurrency = MutableStateFlow("")
    val selectedCurrency: StateFlow<String> = _selectedCurrency.asStateFlow()

    private val allPayouts = payoutDao.getAllPayoutRows()

    val uiState: StateFlow<PayoutsUiState> = combine(allPayouts, _selectedCurrency) { rows, selectedCurr ->
        val items = rows.map { row ->
            val totalPayout = row.payVal.multiply(BigDecimal.valueOf(row.quantity.toLong()))
            val label = when (row.payType.lowercase()) {
                "coupon", "1" -> "Coupon"
                "redemption", "2" -> "Redemption"
                else -> row.payType.replaceFirstChar { it.uppercase() }
            }
            PayoutItem(
                isin = row.isin,
                bondName = row.bondName.ifBlank { row.isin },
                payDate = row.payDate,
                payType = label,
                payoutAmount = totalPayout,
                currency = row.currency
            )
        }

        val currencies = items.map { it.currency }.distinct()
        val effectiveCurrency = if (selectedCurr in currencies) {
            selectedCurr
        } else {
            currencies.firstOrNull() ?: ""
        }

        val filtered = if (effectiveCurrency.isBlank()) {
            items
        } else {
            items.filter { it.currency.equals(effectiveCurrency, ignoreCase = true) }
        }

        PayoutsUiState(
            availableCurrencies = currencies,
            selectedCurrency = effectiveCurrency,
            payouts = filtered
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PayoutsUiState()
    )

    fun selectCurrency(currency: String) {
        _selectedCurrency.value = currency
    }

    companion object {
        fun provideFactory(payoutDao: PayoutDao): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PayoutsViewModel(payoutDao) as T
                }
            }
    }
}
