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

import com.adnlv.lynd.domain.CompoundingSimulationResult
import com.adnlv.lynd.domain.CompoundingSimulator
import java.math.BigDecimal

enum class OverviewTab {
    OVERVIEW,
    PLANNER
}

enum class PlannerTab(val title: String) {
    INCOME_GAPS("Income Gaps"),
    LADDER_MATCHER("Ladder Matcher"),
    COMPOUNDING("Compounding"),
    PURCHASING_POWER("Purchasing Power")
}

private data class CombinedPlannerState(
    val tab: OverviewTab,
    val plannerTab: PlannerTab,
    val horizon: Int,
    val currency: String,
    val compoundingHorizonYears: Int,
    val compoundingCustomRate: BigDecimal?
)

data class OverviewUiState(
    val selectedTab: OverviewTab = OverviewTab.OVERVIEW,
    val selectedPlannerTab: PlannerTab = PlannerTab.INCOME_GAPS,
    val summaries: List<PortfolioSummary> = emptyList(),
    val totalHoldingsCount: Int = 0,
    val holdings: List<HoldingItem> = emptyList(),
    val cashFlowsByCurrency: Map<String, List<MonthlyCashFlow>> = emptyMap(),
    val yearlyMaturitiesByCurrency: Map<String, List<YearlyMaturity>> = emptyMap(),
    val currencyAllocations: List<CurrencyAllocation> = emptyList(),
    val plannerHorizonMonths: Int = 12,
    val plannerSelectedCurrency: String = "UAH",
    val incomeGaps: List<IncomeGap> = emptyList(),
    val ladderMatches: List<com.adnlv.lynd.domain.GapMatches> = emptyList(),
    val compoundingHorizonYears: Int = 5,
    val compoundingCustomRate: BigDecimal? = null,
    val compoundingSimulation: CompoundingSimulationResult? = null,
    val purchasingPowerForecast: com.adnlv.lynd.domain.PurchasingPowerForecastResult? = null
)

class OverviewViewModel(
    private val holdingDao: HoldingDao,
    private val bondDao: BondDao,
    private val payoutDao: PayoutDao
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(OverviewTab.OVERVIEW)
    private val _selectedPlannerTab = MutableStateFlow(PlannerTab.INCOME_GAPS)
    private val _plannerHorizon = MutableStateFlow(12)
    private val _plannerCurrency = MutableStateFlow("UAH")
    private val _compoundingHorizonYears = MutableStateFlow(5)
    private val _compoundingCustomRate = MutableStateFlow<BigDecimal?>(null)

    val uiState: StateFlow<OverviewUiState> = combine(
        combine(
            combine(_selectedTab, _selectedPlannerTab) { tab, pTab -> tab to pTab },
            combine(_plannerHorizon, _plannerCurrency) { h, c -> h to c },
            combine(_compoundingHorizonYears, _compoundingCustomRate) { ch, cr -> ch to cr }
        ) { (tab, pTab), (horizon, currency), (compHorizon, compRate) ->
            CombinedPlannerState(tab, pTab, horizon, currency, compHorizon, compRate)
        },
        holdingDao.getAllHoldings(),
        holdingDao.getPaymentsForHoldings(),
        payoutDao.getAllPayoutRows(),
        combine(bondDao.getAllBonds(), bondDao.getAllPayments()) { bonds, payments ->
            bonds to payments
        }
    ) { plannerState, holdingsList, paymentsList, payoutRows, catalogData ->
        val domainHoldings = PortfolioCalculator.mapHoldingsWithPayments(holdingsList, paymentsList)
        val summaries = PortfolioCalculator.calculateSummaries(domainHoldings)
        val cashFlows = PortfolioCalculator.calculateMonthlyCashFlows(payoutRows)
        val maturities = PortfolioCalculator.calculateYearlyMaturitySchedule(payoutRows)
        val allocations = PortfolioCalculator.calculateCurrencyAllocations(summaries)
        val incomeGaps = IncomeGapDetector.detectGaps(
            payoutRows = payoutRows,
            currency = plannerState.currency,
            monthCount = plannerState.horizon
        )
        val ladderMatches = com.adnlv.lynd.domain.SmartLadderMatcher.matchGaps(
            gaps = incomeGaps,
            bonds = catalogData.first,
            payments = catalogData.second
        )

        val selectedCurrency = plannerState.currency
        val currencyCashFlows = cashFlows[selectedCurrency].orEmpty()
        val currencySummary = summaries.firstOrNull { it.currency.equals(selectedCurrency, ignoreCase = true) }
        val investedCapital = currencySummary?.investedCapital ?: BigDecimal.ZERO
        val avgInterestRate = currencySummary?.averageInterestRate ?: BigDecimal.ZERO

        val smoothedMonthlyIncome = CompoundingSimulator.calculateSmoothedMonthlyIncome(
            currencyCashFlows = currencyCashFlows,
            investedCapital = investedCapital,
            averageRate = avgInterestRate
        )

        val defaultRate = when (selectedCurrency.uppercase()) {
            "USD" -> BigDecimal("4.0")
            "EUR" -> BigDecimal("3.2")
            else -> BigDecimal("15.0")
        }
        val annualRate = plannerState.compoundingCustomRate ?: defaultRate

        val compoundingSimulation = CompoundingSimulator.simulate(
            currency = selectedCurrency,
            investedCapital = investedCapital,
            monthlyIncome = smoothedMonthlyIncome,
            annualRatePercent = annualRate,
            horizonYears = plannerState.compoundingHorizonYears
        )

        val inflationRates = com.adnlv.lynd.domain.NbuInflationData.getRatesForCurrency(
            currency = selectedCurrency,
            horizonYears = plannerState.compoundingHorizonYears
        )

        val purchasingPowerForecast = com.adnlv.lynd.domain.PurchasingPowerForecaster.forecast(
            compoundingResult = compoundingSimulation,
            inflationRates = inflationRates
        )

        OverviewUiState(
            selectedTab = plannerState.tab,
            selectedPlannerTab = plannerState.plannerTab,
            summaries = summaries,
            totalHoldingsCount = domainHoldings.size,
            holdings = domainHoldings,
            cashFlowsByCurrency = cashFlows,
            yearlyMaturitiesByCurrency = maturities,
            currencyAllocations = allocations,
            plannerHorizonMonths = plannerState.horizon,
            plannerSelectedCurrency = plannerState.currency,
            incomeGaps = incomeGaps,
            ladderMatches = ladderMatches,
            compoundingHorizonYears = plannerState.compoundingHorizonYears,
            compoundingCustomRate = plannerState.compoundingCustomRate,
            compoundingSimulation = compoundingSimulation,
            purchasingPowerForecast = purchasingPowerForecast
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = OverviewUiState()
    )

    fun selectTab(tab: OverviewTab) {
        _selectedTab.value = tab
    }

    fun selectPlannerTab(tab: PlannerTab) {
        _selectedPlannerTab.value = tab
    }

    fun setPlannerHorizon(months: Int) {
        _plannerHorizon.value = months
    }

    fun setPlannerCurrency(currency: String) {
        _plannerCurrency.value = currency
        _compoundingCustomRate.value = null
    }

    fun setCompoundingHorizon(years: Int) {
        _compoundingHorizonYears.value = years
    }

    fun setCompoundingRate(rate: BigDecimal) {
        _compoundingCustomRate.value = rate
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
