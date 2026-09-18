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
import java.time.LocalDate

enum class PayoutTab {
    UPCOMING,
    RECEIVED,
    HISTORICAL
}

data class PayoutsUiState(
    val availableCurrencies: List<String> = emptyList(),
    val selectedCurrency: String = "",
    val selectedTab: PayoutTab = PayoutTab.UPCOMING,
    val payouts: List<PayoutItem> = emptyList()
)

class PayoutsViewModel(
    payoutDao: PayoutDao
) : ViewModel() {

    private val _selectedCurrency = MutableStateFlow("")
    val selectedCurrency: StateFlow<String> = _selectedCurrency.asStateFlow()

    private val _selectedTab = MutableStateFlow(PayoutTab.UPCOMING)
    val selectedTab: StateFlow<PayoutTab> = _selectedTab.asStateFlow()

    private val allPayouts = payoutDao.getAllPayoutRows()

    val uiState: StateFlow<PayoutsUiState> = combine(allPayouts, _selectedCurrency, _selectedTab) { rows, selectedCurr, currentTab ->
        data class PayoutWithPurchase(val item: PayoutItem, val purchaseDate: LocalDate)

        val itemsWithPurchase = rows.map { row ->
            val totalPayout = row.payVal.multiply(BigDecimal.valueOf(row.quantity.toLong()))
            val label = when (row.payType.lowercase()) {
                "coupon", "1" -> "Coupon"
                "redemption", "2" -> "Redemption"
                else -> row.payType.replaceFirstChar { it.uppercase() }
            }
            PayoutWithPurchase(
                item = PayoutItem(
                    isin = row.isin,
                    bondName = row.bondName.ifBlank { row.isin },
                    payDate = row.payDate,
                    payType = label,
                    payoutAmount = totalPayout,
                    currency = row.currency
                ),
                purchaseDate = row.purchaseDate
            )
        }

        val currencies = itemsWithPurchase.map { it.item.currency }.distinct()
        val effectiveCurrency = if (selectedCurr in currencies) {
            selectedCurr
        } else {
            currencies.firstOrNull() ?: ""
        }

        val filtered = if (effectiveCurrency.isBlank()) {
            itemsWithPurchase
        } else {
            itemsWithPurchase.filter { it.item.currency.equals(effectiveCurrency, ignoreCase = true) }
        }

        val today = LocalDate.now()
        val upcomingItems = filtered
            .filter { !it.item.payDate.isBefore(today) }
            .sortedBy { it.item.payDate }
            .map { it.item }

        val receivedItems = filtered
            .filter { it.item.payDate.isBefore(today) && !it.item.payDate.isBefore(it.purchaseDate) }
            .sortedByDescending { it.item.payDate }
            .map { it.item }

        val historicalItems = filtered
            .filter { it.item.payDate.isBefore(it.purchaseDate) }
            .sortedByDescending { it.item.payDate }
            .map { it.item }

        val activePayouts = when (currentTab) {
            PayoutTab.UPCOMING -> upcomingItems
            PayoutTab.RECEIVED -> receivedItems
            PayoutTab.HISTORICAL -> historicalItems
        }

        PayoutsUiState(
            availableCurrencies = currencies,
            selectedCurrency = effectiveCurrency,
            selectedTab = currentTab,
            payouts = activePayouts
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PayoutsUiState()
    )

    fun selectCurrency(currency: String) {
        _selectedCurrency.value = currency
    }

    fun selectTab(tab: PayoutTab) {
        _selectedTab.value = tab
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
