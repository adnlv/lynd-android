package com.adnlv.lynd.ui.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.adnlv.lynd.data.TestPortfolioData
import com.adnlv.lynd.data.db.BondDao
import com.adnlv.lynd.data.db.HoldingDao
import com.adnlv.lynd.data.db.PayoutDao
import com.adnlv.lynd.domain.ActionableGap
import com.adnlv.lynd.domain.GapMatches
import com.adnlv.lynd.domain.IncomeGap
import com.adnlv.lynd.domain.IncomeGapDetector
import com.adnlv.lynd.domain.PortfolioCalculator
import com.adnlv.lynd.domain.PortfolioSummary
import com.adnlv.lynd.domain.SmartLadderMatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PlannerUiState(
    val summaries: List<PortfolioSummary> = emptyList(),
    val plannerHorizonMonths: Int = 12,
    val plannerSelectedCurrency: String = "UAH",
    val actionableGaps: List<ActionableGap> = emptyList(),
    val isPlannerLoading: Boolean = false,
    val incomeGaps: List<IncomeGap> = emptyList(),
    val ladderMatches: List<GapMatches> = emptyList()
)

class PlannerViewModel(
    private val holdingDao: HoldingDao,
    private val bondDao: BondDao,
    private val payoutDao: PayoutDao
) : ViewModel() {

    private val _plannerHorizon = MutableStateFlow(12)
    private val _plannerCurrency = MutableStateFlow("UAH")

    val uiState: StateFlow<PlannerUiState> = combine(
        combine(_plannerHorizon, _plannerCurrency) { h, c -> h to c },
        holdingDao.getAllHoldings(),
        holdingDao.getPaymentsForHoldings(),
        payoutDao.getAllPayoutRows(),
        combine(bondDao.getAllBonds(), bondDao.getAllPayments()) { bonds, payments ->
            bonds to payments
        }
    ) { (horizon, currency), holdingsList, paymentsList, payoutRows, catalogData ->
        val domainHoldings = PortfolioCalculator.mapHoldingsWithPayments(holdingsList, paymentsList)
        val summaries = PortfolioCalculator.calculateSummaries(domainHoldings)
        val incomeGaps = IncomeGapDetector.detectGaps(
            payoutRows = payoutRows,
            currency = currency,
            monthCount = horizon
        )
        val actionableGaps = SmartLadderMatcher.generateActionableGaps(
            gaps = incomeGaps,
            bonds = catalogData.first,
            payments = catalogData.second
        )
        val ladderMatches = actionableGaps

        PlannerUiState(
            summaries = summaries,
            plannerHorizonMonths = horizon,
            plannerSelectedCurrency = currency,
            actionableGaps = actionableGaps,
            isPlannerLoading = false,
            incomeGaps = incomeGaps,
            ladderMatches = ladderMatches
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlannerUiState()
    )

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
                    return PlannerViewModel(holdingDao, bondDao, payoutDao) as T
                }
            }
    }
}
