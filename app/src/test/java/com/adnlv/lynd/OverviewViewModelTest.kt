package com.adnlv.lynd

import com.adnlv.lynd.data.db.BondPaymentEntity
import com.adnlv.lynd.data.db.HoldingWithBond
import com.adnlv.lynd.data.db.PayoutRow
import com.adnlv.lynd.ui.overview.OverviewViewModel
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
}
