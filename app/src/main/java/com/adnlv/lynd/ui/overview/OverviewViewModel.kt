package com.adnlv.lynd.ui.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.adnlv.lynd.data.TestPortfolioData
import com.adnlv.lynd.data.db.BondDao
import com.adnlv.lynd.data.db.HoldingDao
import com.adnlv.lynd.domain.HoldingItem
import com.adnlv.lynd.domain.PortfolioCalculator
import com.adnlv.lynd.domain.PortfolioSummary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

data class OverviewUiState(
    val summaries: List<PortfolioSummary> = emptyList(),
    val totalHoldingsCount: Int = 0,
    val holdings: List<HoldingItem> = emptyList()
)

class OverviewViewModel(
    private val holdingDao: HoldingDao,
    private val bondDao: BondDao
) : ViewModel() {

    val uiState: StateFlow<OverviewUiState> = combine(
        holdingDao.getAllHoldings(),
        holdingDao.getPaymentsForHoldings()
    ) { holdingsList, paymentsList ->
        val paymentsByIsin = paymentsList.groupBy { it.bondIsin }

        val domainHoldings = holdingsList.map { item ->
            val paymentsForHolding = paymentsByIsin[item.isin].orEmpty()
                .filter { !it.payDate.isBefore(item.purchaseDate) }

            val totalPayout = paymentsForHolding.fold(BigDecimal.ZERO) { acc, payment ->
                acc.add(payment.payVal.multiply(BigDecimal.valueOf(item.quantity.toLong())))
            }

            val profitAmount = totalPayout.subtract(item.totalPaidAmount)

            val profitPercent = if (item.totalPaidAmount > BigDecimal.ZERO) {
                profitAmount.multiply(BigDecimal("100"))
                    .divide(item.totalPaidAmount, 4, RoundingMode.HALF_UP)
            } else {
                BigDecimal.ZERO
            }

            HoldingItem(
                id = item.id,
                isin = item.isin,
                bondName = item.bondName.ifBlank { item.isin },
                quantity = item.quantity,
                pricePerBond = item.pricePerBond,
                totalPaidAmount = item.totalPaidAmount,
                purchaseDate = item.purchaseDate,
                currency = item.currency,
                couponRate = item.couponRate,
                totalPayoutAmount = totalPayout,
                totalProfitAmount = profitAmount,
                profitPercentage = profitPercent
            )
        }

        val summaries = PortfolioCalculator.calculateSummaries(domainHoldings)

        OverviewUiState(
            summaries = summaries,
            totalHoldingsCount = domainHoldings.size,
            holdings = domainHoldings
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = OverviewUiState()
    )

    fun loadTestPortfolio() {
        viewModelScope.launch {
            TestPortfolioData.seed(bondDao, holdingDao)
        }
    }

    companion object {
        fun provideFactory(
            holdingDao: HoldingDao,
            bondDao: BondDao
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return OverviewViewModel(holdingDao, bondDao) as T
                }
            }
    }
}
