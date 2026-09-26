package com.adnlv.lynd.ui.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.adnlv.lynd.data.TestPortfolioData
import com.adnlv.lynd.data.db.BondDao
import com.adnlv.lynd.data.db.HoldingDao
import com.adnlv.lynd.data.db.PayoutDao
import com.adnlv.lynd.domain.CurrencyAllocation
import com.adnlv.lynd.domain.HoldingItem
import com.adnlv.lynd.domain.MonthlyCashFlow
import com.adnlv.lynd.domain.PortfolioCalculator
import com.adnlv.lynd.domain.PortfolioSummary
import com.adnlv.lynd.domain.YearlyMaturity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class OverviewUiState(
    val summaries: List<PortfolioSummary> = emptyList(),
    val totalHoldingsCount: Int = 0,
    val holdings: List<HoldingItem> = emptyList(),
    val cashFlowsByCurrency: Map<String, List<MonthlyCashFlow>> = emptyMap(),
    val yearlyMaturitiesByCurrency: Map<String, List<YearlyMaturity>> = emptyMap(),
    val currencyAllocations: List<CurrencyAllocation> = emptyList()
)

class OverviewViewModel(
    private val holdingDao: HoldingDao,
    private val bondDao: BondDao,
    private val payoutDao: PayoutDao
) : ViewModel() {

    val uiState: StateFlow<OverviewUiState> = combine(
        holdingDao.getAllHoldings(),
        holdingDao.getPaymentsForHoldings(),
        payoutDao.getAllPayoutRows()
    ) { holdingsList, paymentsList, payoutRows ->
        val domainHoldings = PortfolioCalculator.mapHoldingsWithPayments(holdingsList, paymentsList)
        val summaries = PortfolioCalculator.calculateSummaries(domainHoldings)
        val cashFlows = PortfolioCalculator.calculateMonthlyCashFlows(payoutRows)
        val maturities = PortfolioCalculator.calculateYearlyMaturitySchedule(payoutRows)
        val allocations = PortfolioCalculator.calculateCurrencyAllocations(summaries)

        OverviewUiState(
            summaries = summaries,
            totalHoldingsCount = domainHoldings.size,
            holdings = domainHoldings,
            cashFlowsByCurrency = cashFlows,
            yearlyMaturitiesByCurrency = maturities,
            currencyAllocations = allocations
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
            bondDao: BondDao,
            payoutDao: PayoutDao
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return OverviewViewModel(holdingDao, bondDao, payoutDao) as T
                }
            }
    }
}
