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
import com.adnlv.lynd.domain.IncomeGap
import com.adnlv.lynd.domain.IncomeGapDetector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class OverviewTab {
    OVERVIEW,
    PLANNER
}

data class OverviewUiState(
    val selectedTab: OverviewTab = OverviewTab.OVERVIEW,
    val summaries: List<PortfolioSummary> = emptyList(),
    val totalHoldingsCount: Int = 0,
    val holdings: List<HoldingItem> = emptyList(),
    val cashFlowsByCurrency: Map<String, List<MonthlyCashFlow>> = emptyMap(),
    val yearlyMaturitiesByCurrency: Map<String, List<YearlyMaturity>> = emptyMap(),
    val currencyAllocations: List<CurrencyAllocation> = emptyList(),
    val plannerHorizonMonths: Int = 12,
    val plannerSelectedCurrency: String = "UAH",
    val incomeGaps: List<IncomeGap> = emptyList()
)

class OverviewViewModel(
    private val holdingDao: HoldingDao,
    private val bondDao: BondDao,
    private val payoutDao: PayoutDao
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(OverviewTab.OVERVIEW)
    private val _plannerHorizon = MutableStateFlow(12)
    private val _plannerCurrency = MutableStateFlow("UAH")

    val uiState: StateFlow<OverviewUiState> = combine(
        combine(_selectedTab, _plannerHorizon, _plannerCurrency) { tab, horizon, currency ->
            Triple(tab, horizon, currency)
        },
        holdingDao.getAllHoldings(),
        holdingDao.getPaymentsForHoldings(),
        payoutDao.getAllPayoutRows()
    ) { (selectedTab, plannerHorizon, plannerCurrency), holdingsList, paymentsList, payoutRows ->
        val domainHoldings = PortfolioCalculator.mapHoldingsWithPayments(holdingsList, paymentsList)
        val summaries = PortfolioCalculator.calculateSummaries(domainHoldings)
        val cashFlows = PortfolioCalculator.calculateMonthlyCashFlows(payoutRows)
        val maturities = PortfolioCalculator.calculateYearlyMaturitySchedule(payoutRows)
        val allocations = PortfolioCalculator.calculateCurrencyAllocations(summaries)
        val incomeGaps = IncomeGapDetector.detectGaps(
            payoutRows = payoutRows,
            currency = plannerCurrency,
            monthCount = plannerHorizon
        )

        OverviewUiState(
            selectedTab = selectedTab,
            summaries = summaries,
            totalHoldingsCount = domainHoldings.size,
            holdings = domainHoldings,
            cashFlowsByCurrency = cashFlows,
            yearlyMaturitiesByCurrency = maturities,
            currencyAllocations = allocations,
            plannerHorizonMonths = plannerHorizon,
            plannerSelectedCurrency = plannerCurrency,
            incomeGaps = incomeGaps
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = OverviewUiState()
    )

    fun selectTab(tab: OverviewTab) {
        _selectedTab.value = tab
    }

    fun setPlannerHorizon(months: Int) {
        _plannerHorizon.value = months
    }

    fun setPlannerCurrency(currency: String) {
        _plannerCurrency.value = currency
    }

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
