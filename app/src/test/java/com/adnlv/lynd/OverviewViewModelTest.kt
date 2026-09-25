package com.adnlv.lynd

import com.adnlv.lynd.data.db.BondPaymentEntity
import com.adnlv.lynd.data.db.HoldingWithBond
import com.adnlv.lynd.data.db.PayoutRow
import com.adnlv.lynd.ui.overview.OverviewTab
import com.adnlv.lynd.ui.overview.OverviewViewModel
import com.adnlv.lynd.ui.overview.PlannerTab
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class OverviewViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun uiState_combinesHoldingsPaymentsAndPayouts() = runTest {
        val holdingDao = FakeHoldingDao()
        val bondDao = FakeBondDao()
        val payoutDao = FakePayoutDao()

        val today = LocalDate.now()
        val holding = HoldingWithBond(
            id = 1,
            isin = "UA4000187348",
            bondName = "Bond 1",
            quantity = 5,
            pricePerBond = BigDecimal("1000.00"),
            totalPaidAmount = BigDecimal("5000.00"),
            purchaseDate = today.minusMonths(1),
            currency = "UAH",
            couponRate = BigDecimal("15.00")
        )
        val payment = BondPaymentEntity(
            bondIsin = "UA4000187348",
            payDate = today.plusMonths(2),
            payType = "coupon",
            payVal = BigDecimal("75.00")
        )
        val payoutRow = PayoutRow(
            isin = "UA4000187348",
            bondName = "Bond 1",
            payDate = today.plusMonths(2),
            payType = "coupon",
            payVal = BigDecimal("75.00"),
            quantity = 5,
            currency = "UAH",
            purchaseDate = today.minusMonths(1)
        )

        holdingDao.allHoldingsFlow.value = listOf(holding)
        holdingDao.paymentsFlow.value = listOf(payment)
        payoutDao.rowsFlow.value = listOf(payoutRow)

        val viewModel = OverviewViewModel(holdingDao, bondDao, payoutDao)
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect {} }

        val state = viewModel.uiState.value
        assertEquals(1, state.totalHoldingsCount)
        assertEquals(1, state.holdings.size)
        assertEquals(1, state.summaries.size)
        assertEquals("UAH", state.summaries[0].currency)
        assertEquals(BigDecimal("5000.00"), state.summaries[0].investedCapital)

        // Payout = 75.00 * 5 = 375.00
        assertEquals(BigDecimal("375.00"), state.summaries[0].expectedPayout)

        // Cashflows and allocations
        assertTrue(state.cashFlowsByCurrency.containsKey("UAH"))
        assertEquals(1, state.currencyAllocations.size)
        assertEquals("UAH", state.currencyAllocations[0].currency)

        collectJob.cancel()
    }

    @Test
    fun loadTestPortfolio_populatesDao() = runTest {
        val holdingDao = FakeHoldingDao()
        val bondDao = FakeBondDao()
        val payoutDao = FakePayoutDao()

        val viewModel = OverviewViewModel(holdingDao, bondDao, payoutDao)
        viewModel.loadTestPortfolio()

        assertTrue(bondDao.bonds.isNotEmpty())
        assertTrue(holdingDao.holdings.isNotEmpty())
    }

    @Test
    fun selectTab_updatesSelectedTabInUiState() = runTest {
        val holdingDao = FakeHoldingDao()
        val bondDao = FakeBondDao()
        val payoutDao = FakePayoutDao()

        val viewModel = OverviewViewModel(holdingDao, bondDao, payoutDao)
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect {} }

        assertEquals(OverviewTab.OVERVIEW, viewModel.uiState.value.selectedTab)

        viewModel.selectTab(OverviewTab.PLANNER)
        assertEquals(OverviewTab.PLANNER, viewModel.uiState.value.selectedTab)

        viewModel.selectTab(OverviewTab.OVERVIEW)
        assertEquals(OverviewTab.OVERVIEW, viewModel.uiState.value.selectedTab)

        collectJob.cancel()
    }

    @Test
    fun selectPlannerTab_updatesSelectedPlannerTabInUiState() = runTest {
        val holdingDao = FakeHoldingDao()
        val bondDao = FakeBondDao()
        val payoutDao = FakePayoutDao()

        val viewModel = OverviewViewModel(holdingDao, bondDao, payoutDao)
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect {} }

        assertEquals(PlannerTab.INCOME_GAPS, viewModel.uiState.value.selectedPlannerTab)

        viewModel.selectPlannerTab(PlannerTab.LADDER_MATCHER)
        assertEquals(PlannerTab.LADDER_MATCHER, viewModel.uiState.value.selectedPlannerTab)

        viewModel.selectPlannerTab(PlannerTab.INCOME_GAPS)
        assertEquals(PlannerTab.INCOME_GAPS, viewModel.uiState.value.selectedPlannerTab)

        collectJob.cancel()
    }

    @Test
    fun uiState_computesLadderMatchesWhenBondsArePresent() = runTest {
        val holdingDao = FakeHoldingDao()
        val bondDao = FakeBondDao()
        val payoutDao = FakePayoutDao()

        val today = LocalDate.now()
        val bond = com.adnlv.lynd.data.db.BondEntity(
            isin = "UA4000187348",
            name = "Gov Bond Match",
            currency = "UAH",
            nominalValue = BigDecimal("1000.00"),
            couponRate = BigDecimal("15.00"),
            maturityDate = today.plusMonths(3)
        )
        val payment = BondPaymentEntity(
            bondIsin = "UA4000187348",
            payDate = today.plusMonths(2),
            payType = "coupon",
            payVal = BigDecimal("75.00")
        )
        bondDao.allBondsFlow.value = listOf(bond)
        bondDao.allPaymentsFlow.value = listOf(payment)

        val viewModel = OverviewViewModel(holdingDao, bondDao, payoutDao)
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect {} }

        val state = viewModel.uiState.value
        assertEquals(12, state.incomeGaps.size)
        assertEquals(12, state.ladderMatches.size)

        val matchForMonth2 = state.ladderMatches.firstOrNull {
            java.time.YearMonth.from(it.gap.yearMonth) == java.time.YearMonth.from(today.plusMonths(2))
        }
        org.junit.Assert.assertNotNull(matchForMonth2)
        assertEquals(1, matchForMonth2?.recommendedBonds?.size)
        assertEquals("UA4000187348", matchForMonth2?.recommendedBonds?.first()?.bond?.isin)

        collectJob.cancel()
    }

    @Test
    fun setPlannerHorizon_updatesHorizonAndRecalculatesGaps() = runTest {
        val holdingDao = FakeHoldingDao()
        val bondDao = FakeBondDao()
        val payoutDao = FakePayoutDao()

        val viewModel = OverviewViewModel(holdingDao, bondDao, payoutDao)
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect {} }

        assertEquals(12, viewModel.uiState.value.plannerHorizonMonths)
        assertEquals(12, viewModel.uiState.value.incomeGaps.size)

        viewModel.setPlannerHorizon(24)
        assertEquals(24, viewModel.uiState.value.plannerHorizonMonths)
        assertEquals(24, viewModel.uiState.value.incomeGaps.size)

        collectJob.cancel()
    }

    @Test
    fun setPlannerCurrency_updatesCurrencyAndFiltersGaps() = runTest {
        val holdingDao = FakeHoldingDao()
        val bondDao = FakeBondDao()
        val payoutDao = FakePayoutDao()

        val today = LocalDate.now()
        val payoutRow = PayoutRow(
            isin = "US1234567890",
            bondName = "USD Bond",
            payDate = today.plusMonths(1),
            payType = "coupon",
            payVal = BigDecimal("10.00"),
            quantity = 1,
            currency = "USD",
            purchaseDate = today.minusMonths(1)
        )
        payoutDao.rowsFlow.value = listOf(payoutRow)

        val viewModel = OverviewViewModel(holdingDao, bondDao, payoutDao)
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect {} }

        assertEquals("UAH", viewModel.uiState.value.plannerSelectedCurrency)
        // With only USD payout, UAH has 12 dry months
        assertEquals(12, viewModel.uiState.value.incomeGaps.size)

        viewModel.setPlannerCurrency("USD")
        assertEquals("USD", viewModel.uiState.value.plannerSelectedCurrency)
        // USD has 1 payout month, so 11 dry months
        assertEquals(11, viewModel.uiState.value.incomeGaps.size)

        collectJob.cancel()
    }

    @Test
    fun compoundingSimulation_updatesWithHorizonAndRate() = runTest {
        val holdingDao = FakeHoldingDao()
        val bondDao = FakeBondDao()
        val payoutDao = FakePayoutDao()

        val today = LocalDate.now()
        val payoutRow = PayoutRow(
            isin = "UA4000187348",
            bondName = "Bond 1",
            payDate = today.plusMonths(1),
            payType = "coupon",
            payVal = BigDecimal("100.00"),
            quantity = 12,
            currency = "UAH",
            purchaseDate = today.minusMonths(1)
        )
        payoutDao.rowsFlow.value = listOf(payoutRow)

        val viewModel = OverviewViewModel(holdingDao, bondDao, payoutDao)
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect {} }

        val initialState = viewModel.uiState.value
        assertEquals(5, initialState.compoundingHorizonYears)
        org.junit.Assert.assertNotNull(initialState.compoundingSimulation)
        assertEquals(BigDecimal("15.0"), initialState.compoundingSimulation?.annualRatePercent)
        assertEquals(5, initialState.compoundingSimulation?.points?.size)

        viewModel.setCompoundingHorizon(10)
        val state10Y = viewModel.uiState.value
        assertEquals(10, state10Y.compoundingHorizonYears)
        assertEquals(10, state10Y.compoundingSimulation?.points?.size)

        viewModel.setCompoundingRate(BigDecimal("18.0"))
        val stateRate = viewModel.uiState.value
        assertEquals(BigDecimal("18.0"), stateRate.compoundingCustomRate)
        assertEquals(BigDecimal("18.0"), stateRate.compoundingSimulation?.annualRatePercent)

        viewModel.selectPlannerTab(PlannerTab.COMPOUNDING)
        assertEquals(PlannerTab.COMPOUNDING, viewModel.uiState.value.selectedPlannerTab)

        collectJob.cancel()
    }

    @Test
    fun purchasingPowerForecast_updatesWithHorizonAndCurrency() = runTest {
        val holdingDao = FakeHoldingDao()
        val bondDao = FakeBondDao()
        val payoutDao = FakePayoutDao()

        val today = LocalDate.now()
        val payoutRow = PayoutRow(
            isin = "UA4000187348",
            bondName = "Bond 1",
            payDate = today.plusMonths(1),
            payType = "coupon",
            payVal = BigDecimal("100.00"),
            quantity = 12,
            currency = "UAH",
            purchaseDate = today.minusMonths(1)
        )
        payoutDao.rowsFlow.value = listOf(payoutRow)

        val viewModel = OverviewViewModel(holdingDao, bondDao, payoutDao)
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect {} }

        val initialState = viewModel.uiState.value
        org.junit.Assert.assertNotNull(initialState.purchasingPowerForecast)
        assertEquals("UAH", initialState.purchasingPowerForecast?.currency)
        assertEquals(5, initialState.purchasingPowerForecast?.horizonYears)
        assertEquals(5, initialState.purchasingPowerForecast?.points?.size)

        viewModel.selectPlannerTab(PlannerTab.PURCHASING_POWER)
        assertEquals(PlannerTab.PURCHASING_POWER, viewModel.uiState.value.selectedPlannerTab)

        viewModel.setCompoundingHorizon(3)
        val state3Y = viewModel.uiState.value
        assertEquals(3, state3Y.purchasingPowerForecast?.horizonYears)
        assertEquals(3, state3Y.purchasingPowerForecast?.points?.size)

        viewModel.setPlannerCurrency("USD")
        val stateUsd = viewModel.uiState.value
        assertEquals("USD", stateUsd.purchasingPowerForecast?.currency)

        collectJob.cancel()
    }
}

