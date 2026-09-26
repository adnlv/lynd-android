package com.adnlv.lynd

import com.adnlv.lynd.data.db.BondPaymentEntity
import com.adnlv.lynd.data.db.PayoutRow
import com.adnlv.lynd.ui.planner.PlannerTab
import com.adnlv.lynd.ui.planner.PlannerViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class PlannerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun selectPlannerTab_updatesSelectedPlannerTabInUiState() = runTest {
        val holdingDao = FakeHoldingDao()
        val bondDao = FakeBondDao()
        val payoutDao = FakePayoutDao()

        val viewModel = PlannerViewModel(holdingDao, bondDao, payoutDao)
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect {} }

        assertEquals(PlannerTab.INCOME_GAPS, viewModel.uiState.value.selectedPlannerTab)

        viewModel.selectPlannerTab(PlannerTab.COMPOUNDING)
        assertEquals(PlannerTab.COMPOUNDING, viewModel.uiState.value.selectedPlannerTab)

        viewModel.selectPlannerTab(PlannerTab.PURCHASING_POWER)
        assertEquals(PlannerTab.PURCHASING_POWER, viewModel.uiState.value.selectedPlannerTab)

        viewModel.selectPlannerTab(PlannerTab.MATURITY_REBALANCING)
        assertEquals(PlannerTab.MATURITY_REBALANCING, viewModel.uiState.value.selectedPlannerTab)

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

        val viewModel = PlannerViewModel(holdingDao, bondDao, payoutDao)
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect {} }

        val state = viewModel.uiState.value
        assertEquals(12, state.incomeGaps.size)
        assertEquals(12, state.actionableGaps.size)

        val matchForMonth2 = state.actionableGaps.firstOrNull {
            java.time.YearMonth.from(it.gap.yearMonth) == java.time.YearMonth.from(today.plusMonths(2))
        }
        assertNotNull(matchForMonth2)
        assertEquals(1, matchForMonth2?.recommendedBonds?.size)
        assertEquals("UA4000187348", matchForMonth2?.recommendedBonds?.first()?.bond?.isin)

        collectJob.cancel()
    }

    @Test
    fun setPlannerHorizon_updatesHorizonAndRecalculatesGaps() = runTest {
        val holdingDao = FakeHoldingDao()
        val bondDao = FakeBondDao()
        val payoutDao = FakePayoutDao()

        val viewModel = PlannerViewModel(holdingDao, bondDao, payoutDao)
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

        val viewModel = PlannerViewModel(holdingDao, bondDao, payoutDao)
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

        val viewModel = PlannerViewModel(holdingDao, bondDao, payoutDao)
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect {} }

        val initialState = viewModel.uiState.value
        assertEquals(5, initialState.compoundingHorizonYears)
        assertNotNull(initialState.compoundingSimulation)
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

        val viewModel = PlannerViewModel(holdingDao, bondDao, payoutDao)
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect {} }

        val initialState = viewModel.uiState.value
        assertNotNull(initialState.purchasingPowerForecast)
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

    @Test
    fun maturityAlerts_updatesWithPlannerTabAndThreshold() = runTest {
        val holdingDao = FakeHoldingDao()
        val bondDao = FakeBondDao()
        val payoutDao = FakePayoutDao()

        val today = LocalDate.now()
        val payoutRow = PayoutRow(
            isin = "UA4000187348",
            bondName = "Bond Maturing",
            payDate = today.plusDays(15),
            payType = "redemption",
            payVal = BigDecimal("1000.00"),
            quantity = 20,
            currency = "UAH",
            purchaseDate = today.minusYears(1)
        )
        payoutDao.rowsFlow.value = listOf(payoutRow)

        val viewModel = PlannerViewModel(holdingDao, bondDao, payoutDao)
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect {} }

        viewModel.selectPlannerTab(PlannerTab.MATURITY_REBALANCING)
        val state = viewModel.uiState.value
        assertEquals(PlannerTab.MATURITY_REBALANCING, state.selectedPlannerTab)
        assertEquals(1, state.maturityAlerts.size)
        assertEquals("UA4000187348", state.maturityAlerts[0].isin)
        assertEquals(BigDecimal("20000.00"), state.maturityAlerts[0].principalAmount)
        assertEquals(1, state.rebalancingSummary?.activeAlertsCount)

        viewModel.setLargeRedemptionThreshold(BigDecimal("30000"))
        val stateUpdated = viewModel.uiState.value
        assertEquals(BigDecimal("30000"), stateUpdated.largeRedemptionThreshold)
        assertEquals(0, stateUpdated.maturityAlerts.size)

        collectJob.cancel()
    }

    @Test
    fun loadTestPortfolio_populatesDao() = runTest {
        val holdingDao = FakeHoldingDao()
        val bondDao = FakeBondDao()
        val payoutDao = FakePayoutDao()

        val viewModel = PlannerViewModel(holdingDao, bondDao, payoutDao)
        viewModel.loadTestPortfolio()

        assertTrue(bondDao.bonds.isNotEmpty())
        assertTrue(holdingDao.holdings.isNotEmpty())
    }
}
