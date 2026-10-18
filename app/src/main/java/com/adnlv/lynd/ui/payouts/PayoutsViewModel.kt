package com.adnlv.lynd.ui.payouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.adnlv.lynd.data.TestPortfolioData
import com.adnlv.lynd.data.db.BondDao
import com.adnlv.lynd.data.db.HoldingDao
import com.adnlv.lynd.data.db.PayoutDao
import com.adnlv.lynd.domain.PayoutItem
import com.adnlv.lynd.domain.PayoutType
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
    val selectedCurrencies: Set<String> = setOf("UAH"),
    val availablePayoutTypes: Set<PayoutType> = emptySet(),
    val selectedPayoutTypes: Set<PayoutType> = setOf(PayoutType.REDEMPTION, PayoutType.COUPON),
    val selectedTab: PayoutTab = PayoutTab.UPCOMING,
    val payouts: List<PayoutItem> = emptyList(),
    val dateGroups: List<com.adnlv.lynd.domain.PayoutDateGroup> = emptyList(),
    val isPortfolioEmpty: Boolean = false
) {
    val selectedCurrency: String get() = selectedCurrencies.firstOrNull() ?: ""
}

class PayoutsViewModel(
    payoutDao: PayoutDao,
    private val holdingDao: HoldingDao? = null,
    private val bondDao: BondDao? = null
) : ViewModel() {

    private val _selectedCurrencies = MutableStateFlow<Set<String>>(setOf("UAH"))
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
        data class PayoutWithPurchase(
            val item: PayoutItem,
            val payoutType: PayoutType,
            val purchaseDate: LocalDate,
            val row: com.adnlv.lynd.data.db.PayoutRow
        )

        val itemsWithPurchase = rows.map { row ->
            val totalPayout = row.payVal.multiply(BigDecimal.valueOf(row.quantity.toLong()))
            val isRedemption = row.payType.equals("redemption", ignoreCase = true) || row.payType == "2"
            val type = if (isRedemption) PayoutType.REDEMPTION else PayoutType.COUPON
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
                payoutType = type,
                purchaseDate = row.purchaseDate,
                row = row
            )
        }

        val currencies = itemsWithPurchase.map { it.item.currency }.distinct()
        val orderedCurrencies = if ("UAH" in currencies) {
            listOf("UAH") + (currencies - "UAH")
        } else {
            currencies
        }

        val validCurrencies = selectedCurrs.filter { curr ->
            orderedCurrencies.any { it.equals(curr, ignoreCase = true) }
        }.toSet()

        val effectiveCurrencies = when {
            validCurrencies.isNotEmpty() -> validCurrencies
            "UAH" in orderedCurrencies -> setOf("UAH")
            orderedCurrencies.isNotEmpty() -> setOf(orderedCurrencies.first())
            else -> emptySet()
        }

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

        val upcomingEntries = filtered
            .filter { !it.item.payDate.isBefore(today) }
            .sortedBy { it.item.payDate }

        val receivedEntries = filtered
            .filter { it.item.payDate.isBefore(today) && !it.item.payDate.isBefore(it.purchaseDate) }
            .sortedByDescending { it.item.payDate }

        val activeEntries = when (currentTab) {
            PayoutTab.UPCOMING -> upcomingEntries
            PayoutTab.RECEIVED -> receivedEntries
        }

        val activePayouts = activeEntries.map { it.item }

        val dateGroups = activeEntries
            .groupBy { it.item.payDate }
            .map { (payDate, dateEntries) ->
                val dateRows = dateEntries.map { it.row }

                val bonds = dateRows
                    .groupBy { it.isin }
                    .map { (isin, isinRows) ->
                        val firstRow = isinRows.first()
                        val bondName = firstRow.bondName.ifBlank { isin }
                        val currency = firstRow.currency

                        var couponSum = BigDecimal.ZERO
                        var principalSum = BigDecimal.ZERO
                        val rawPayoutTypes = mutableSetOf<com.adnlv.lynd.domain.PayoutType>()

                        isinRows.forEach { row ->
                            val rowAmount = row.payVal.multiply(BigDecimal.valueOf(row.quantity.toLong()))
                            val isRedemption = row.payType.equals("redemption", ignoreCase = true) || row.payType == "2"
                            if (isRedemption) {
                                principalSum = principalSum.add(rowAmount)
                                rawPayoutTypes.add(com.adnlv.lynd.domain.PayoutType.REDEMPTION)
                            } else {
                                couponSum = couponSum.add(rowAmount)
                                rawPayoutTypes.add(com.adnlv.lynd.domain.PayoutType.COUPON)
                            }
                        }

                        val bondPayoutTypes = if (com.adnlv.lynd.domain.PayoutType.REDEMPTION in rawPayoutTypes) {
                            setOf(com.adnlv.lynd.domain.PayoutType.REDEMPTION)
                        } else {
                            rawPayoutTypes
                        }

                        val lots = isinRows
                            .groupBy { if (it.holdingId > 0) it.holdingId else Pair(it.purchaseDate, it.quantity) }
                            .map { (_, lotRows) ->
                                val sample = lotRows.first()
                                val lotAmount = lotRows.fold(BigDecimal.ZERO) { acc, r ->
                                    acc.add(r.payVal.multiply(BigDecimal.valueOf(r.quantity.toLong())))
                                }
                                com.adnlv.lynd.domain.PayoutLot(
                                    holdingId = sample.holdingId,
                                    purchaseDate = sample.purchaseDate,
                                    quantity = sample.quantity,
                                    amount = lotAmount
                                )
                            }
                            .sortedBy { it.purchaseDate }

                        val totalQuantity = lots.sumOf { it.quantity }
                        val totalAmount = couponSum.add(principalSum)

                        com.adnlv.lynd.domain.PayoutBondDetail(
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

                val datePayoutTypes = bonds.flatMap { it.payoutTypes }.toSet()

                val totalAmounts = dateRows
                    .groupBy { it.currency }
                    .map { (curr, currRows) ->
                        val sum = currRows.fold(BigDecimal.ZERO) { acc, r ->
                            acc.add(r.payVal.multiply(BigDecimal.valueOf(r.quantity.toLong())))
                        }
                        com.adnlv.lynd.domain.CurrencyTotal(
                            currency = curr,
                            amount = sum
                        )
                    }

                com.adnlv.lynd.domain.PayoutDateGroup(
                    date = payDate,
                    totalAmounts = totalAmounts,
                    payoutTypes = datePayoutTypes,
                    bonds = bonds
                )
            }

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
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PayoutsUiState()
    )

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
