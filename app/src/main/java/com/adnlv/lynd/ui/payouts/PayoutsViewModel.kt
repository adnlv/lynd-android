package com.adnlv.lynd.ui.payouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.adnlv.lynd.core.AppConstants
import com.adnlv.lynd.data.TestPortfolioData
import com.adnlv.lynd.data.db.BondDao
import com.adnlv.lynd.data.db.HoldingDao
import com.adnlv.lynd.data.db.PayoutDao
import com.adnlv.lynd.data.db.PayoutRow
import com.adnlv.lynd.domain.CurrencyTotal
import com.adnlv.lynd.domain.PayoutBondDetail
import com.adnlv.lynd.domain.PayoutDateGroup
import com.adnlv.lynd.domain.PayoutItem
import com.adnlv.lynd.domain.PayoutLot
import com.adnlv.lynd.domain.PayoutType
import com.adnlv.lynd.domain.calculateTotalAmount
import com.adnlv.lynd.domain.displayTypeLabel
import com.adnlv.lynd.domain.isRedemption
import com.adnlv.lynd.domain.resolvePayoutType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate

enum class PayoutTab {
    UPCOMING,
    RECEIVED
}

data class PayoutsUiState(
    val availableCurrencies: List<String> = emptyList(),
    val selectedCurrencies: Set<String> = setOf(AppConstants.DEFAULT_CURRENCY),
    val availablePayoutTypes: Set<PayoutType> = emptySet(),
    val selectedPayoutTypes: Set<PayoutType> = setOf(PayoutType.REDEMPTION, PayoutType.COUPON),
    val selectedTab: PayoutTab = PayoutTab.UPCOMING,
    val payouts: List<PayoutItem> = emptyList(),
    val dateGroups: List<PayoutDateGroup> = emptyList(),
    val isPortfolioEmpty: Boolean = false
) {
    val selectedCurrency: String get() = selectedCurrencies.firstOrNull() ?: ""
}

private data class PayoutWithPurchase(
    val item: PayoutItem,
    val payoutType: PayoutType,
    val purchaseDate: LocalDate,
    val row: PayoutRow
)

class PayoutsViewModel(
    payoutDao: PayoutDao,
    private val holdingDao: HoldingDao? = null,
    private val bondDao: BondDao? = null
) : ViewModel() {

    private val _selectedCurrencies = MutableStateFlow<Set<String>>(setOf(AppConstants.DEFAULT_CURRENCY))
    val selectedCurrencies: StateFlow<Set<String>> = _selectedCurrencies.asStateFlow()

    private val _selectedPayoutTypes = MutableStateFlow<Set<PayoutType>>(
        setOf(PayoutType.REDEMPTION, PayoutType.COUPON)
    )
    val selectedPayoutTypes: StateFlow<Set<PayoutType>> = _selectedPayoutTypes.asStateFlow()

    private val _selectedTab = MutableStateFlow(PayoutTab.UPCOMING)
    val selectedTab: StateFlow<PayoutTab> = _selectedTab.asStateFlow()

    private val allPayouts = payoutDao.getAllPayoutRows()
    private val allHoldings = holdingDao?.getAllHoldings() ?: flowOf(null)

    val uiState: StateFlow<PayoutsUiState> = combine(
        allPayouts,
        allHoldings,
        _selectedCurrencies,
        _selectedPayoutTypes,
        _selectedTab
    ) { rows, holdings, selectedCurrs, selectedTypes, currentTab ->
        val isPortfolioEmpty = if (holdings != null) holdings.isEmpty() else rows.isEmpty()
        val itemsWithPurchase = mapPayoutRowsToItemsWithPurchase(rows)

        val currencies = itemsWithPurchase.map { it.item.currency }.distinct()
        val orderedCurrencies = orderCurrencies(currencies)
        val effectiveCurrencies = resolveEffectiveCurrencies(orderedCurrencies, selectedCurrs)

        val filtered = itemsWithPurchase.filter { item ->
            val matchesCurrency = effectiveCurrencies.isEmpty() ||
                effectiveCurrencies.any { it.equals(item.item.currency, ignoreCase = true) }
            val matchesType = item.payoutType in selectedTypes
            matchesCurrency && matchesType
        }

        val today = LocalDate.now()
        val matchingCurrencyEntries = itemsWithPurchase.filter { item ->
            effectiveCurrencies.isEmpty() ||
                effectiveCurrencies.any { it.equals(item.item.currency, ignoreCase = true) }
        }

        val tabEntries = when (currentTab) {
            PayoutTab.UPCOMING -> matchingCurrencyEntries.filter { !it.item.payDate.isBefore(today) }
            PayoutTab.RECEIVED -> matchingCurrencyEntries.filter {
                it.item.payDate.isBefore(today) && !it.item.payDate.isBefore(it.purchaseDate)
            }
        }
        val availablePayoutTypes = tabEntries.map { it.payoutType }.toSet()

        val activeEntries = when (currentTab) {
            PayoutTab.UPCOMING -> filtered
                .filter { !it.item.payDate.isBefore(today) }
                .sortedBy { it.item.payDate }
            PayoutTab.RECEIVED -> filtered
                .filter { it.item.payDate.isBefore(today) && !it.item.payDate.isBefore(it.purchaseDate) }
                .sortedByDescending { it.item.payDate }
        }

        val activePayouts = activeEntries.map { it.item }
        val dateGroups = buildDateGroups(activeEntries)

        PayoutsUiState(
            availableCurrencies = orderedCurrencies,
            selectedCurrencies = effectiveCurrencies,
            availablePayoutTypes = availablePayoutTypes,
            selectedPayoutTypes = selectedTypes,
            selectedTab = currentTab,
            payouts = activePayouts,
            dateGroups = dateGroups,
            isPortfolioEmpty = isPortfolioEmpty
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(AppConstants.VIEW_MODEL_STOP_TIMEOUT_MS),
        initialValue = PayoutsUiState()
    )

    private fun mapPayoutRowsToItemsWithPurchase(rows: List<PayoutRow>): List<PayoutWithPurchase> {
        return rows.map { row ->
            PayoutWithPurchase(
                item = PayoutItem(
                    isin = row.isin,
                    bondName = row.bondName.ifBlank { row.isin },
                    payDate = row.payDate,
                    payType = row.displayTypeLabel(),
                    payoutAmount = row.calculateTotalAmount(),
                    currency = row.currency
                ),
                payoutType = row.resolvePayoutType(),
                purchaseDate = row.purchaseDate,
                row = row
            )
        }
    }

    private fun orderCurrencies(currencies: List<String>): List<String> {
        return if (AppConstants.DEFAULT_CURRENCY in currencies) {
            listOf(AppConstants.DEFAULT_CURRENCY) + (currencies - AppConstants.DEFAULT_CURRENCY)
        } else {
            currencies
        }
    }

    private fun resolveEffectiveCurrencies(
        orderedCurrencies: List<String>,
        selectedCurrs: Set<String>
    ): Set<String> {
        val validCurrencies = selectedCurrs.filter { curr ->
            orderedCurrencies.any { it.equals(curr, ignoreCase = true) }
        }.toSet()

        return when {
            validCurrencies.isNotEmpty() -> validCurrencies
            AppConstants.DEFAULT_CURRENCY in orderedCurrencies -> setOf(AppConstants.DEFAULT_CURRENCY)
            orderedCurrencies.isNotEmpty() -> setOf(orderedCurrencies.first())
            else -> emptySet()
        }
    }

    private fun buildDateGroups(activeEntries: List<PayoutWithPurchase>): List<PayoutDateGroup> {
        return activeEntries
            .groupBy { it.item.payDate }
            .map { (payDate, dateEntries) ->
                val dateRows = dateEntries.map { it.row }
                val bonds = buildBondDetails(dateRows)
                val datePayoutTypes = bonds.flatMap { it.payoutTypes }.toSet()

                val totalAmounts = dateRows
                    .groupBy { it.currency }
                    .map { (curr, currRows) ->
                        val sum = currRows.fold(BigDecimal.ZERO) { acc, r ->
                            acc.add(r.calculateTotalAmount())
                        }
                        CurrencyTotal(currency = curr, amount = sum)
                    }

                PayoutDateGroup(
                    date = payDate,
                    totalAmounts = totalAmounts,
                    payoutTypes = datePayoutTypes,
                    bonds = bonds
                )
            }
    }

    private fun buildBondDetails(dateRows: List<PayoutRow>): List<PayoutBondDetail> {
        return dateRows
            .groupBy { it.isin }
            .map { (isin, isinRows) ->
                val firstRow = isinRows.first()
                val bondName = firstRow.bondName.ifBlank { isin }
                val currency = firstRow.currency

                var couponSum = BigDecimal.ZERO
                var principalSum = BigDecimal.ZERO
                val rawPayoutTypes = mutableSetOf<PayoutType>()

                isinRows.forEach { row ->
                    val rowAmount = row.calculateTotalAmount()
                    if (row.isRedemption) {
                        principalSum = principalSum.add(rowAmount)
                        rawPayoutTypes.add(PayoutType.REDEMPTION)
                    } else {
                        couponSum = couponSum.add(rowAmount)
                        rawPayoutTypes.add(PayoutType.COUPON)
                    }
                }

                val bondPayoutTypes = if (PayoutType.REDEMPTION in rawPayoutTypes) {
                    setOf(PayoutType.REDEMPTION)
                } else {
                    rawPayoutTypes
                }

                val lots = isinRows
                    .groupBy { if (it.holdingId > 0) it.holdingId else Pair(it.purchaseDate, it.quantity) }
                    .map { (_, lotRows) ->
                        val sample = lotRows.first()
                        val lotAmount = lotRows.fold(BigDecimal.ZERO) { acc, r ->
                            acc.add(r.calculateTotalAmount())
                        }
                        PayoutLot(
                            holdingId = sample.holdingId,
                            purchaseDate = sample.purchaseDate,
                            quantity = sample.quantity,
                            amount = lotAmount
                        )
                    }
                    .sortedBy { it.purchaseDate }

                val totalQuantity = lots.sumOf { it.quantity }
                val totalAmount = couponSum.add(principalSum)

                PayoutBondDetail(
                    isin = isin,
                    bondName = bondName,
                    totalQuantity = totalQuantity,
                    totalAmount = totalAmount,
                    couponAmount = couponSum,
                    principalAmount = principalSum,
                    currency = currency,
                    payoutTypes = bondPayoutTypes,
                    lots = lots
                )
            }
    }

    fun loadTestPortfolio() {
        val bDao = bondDao ?: return
        val hDao = holdingDao ?: return
        viewModelScope.launch {
            TestPortfolioData.seed(bDao, hDao)
        }
    }

    fun selectCurrency(currency: String) {
        _selectedCurrencies.value = setOf(currency)
    }

    fun toggleCurrency(currency: String) {
        val current = _selectedCurrencies.value
        if (current.contains(currency)) {
            if (current.size > 1) {
                _selectedCurrencies.value = current - currency
            }
        } else {
            _selectedCurrencies.value = current + currency
        }
    }

    fun selectAllCurrencies(currencies: Collection<String>) {
        if (currencies.isNotEmpty()) {
            _selectedCurrencies.value = currencies.toSet()
        }
    }

    fun selectPayoutType(type: PayoutType) {
        _selectedPayoutTypes.value = setOf(type)
    }

    fun togglePayoutType(type: PayoutType) {
        val current = _selectedPayoutTypes.value
        if (current.contains(type)) {
            if (current.size > 1) {
                _selectedPayoutTypes.value = current - type
            }
        } else {
            _selectedPayoutTypes.value = current + type
        }
    }

    fun selectTab(tab: PayoutTab) {
        _selectedTab.value = tab
    }

    companion object {
        fun provideFactory(
            payoutDao: PayoutDao,
            holdingDao: HoldingDao? = null,
            bondDao: BondDao? = null
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PayoutsViewModel(payoutDao, holdingDao, bondDao)
            }
        }
    }
}
