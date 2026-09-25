package com.adnlv.lynd

import com.adnlv.lynd.data.db.PayoutDao
import com.adnlv.lynd.data.db.PayoutRow
import com.adnlv.lynd.ui.payouts.PayoutTab
import com.adnlv.lynd.ui.payouts.PayoutsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class FakePayoutDao : PayoutDao {
    val rowsFlow = MutableStateFlow<List<PayoutRow>>(emptyList())

    override fun getAllPayoutRows(): Flow<List<PayoutRow>> = rowsFlow
}

@OptIn(ExperimentalCoroutinesApi::class)
class PayoutsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initialState_defaultsToUpcomingTab() = runTest {
        val fakeDao = FakePayoutDao()
        val viewModel = PayoutsViewModel(fakeDao)

        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect {} }

        assertEquals(PayoutTab.UPCOMING, viewModel.uiState.value.selectedTab)
        assertTrue(viewModel.uiState.value.payouts.isEmpty())

        collectJob.cancel()
    }

    @Test
    fun selectTab_updatesSelectedTabAndFiltersPayouts() = runTest {
        val today = LocalDate.now()
        val fakeDao = FakePayoutDao().apply {
            rowsFlow.value = listOf(
                PayoutRow(
                    isin = "UA4000187348",
                    bondName = "Upcoming Bond",
                    payDate = today.plusDays(10),
                    payType = "1",
                    payVal = BigDecimal("50.00"),
                    quantity = 2,
                    currency = "UAH",
                    purchaseDate = today.minusDays(30)
                ),
                PayoutRow(
                    isin = "UA4000187348",
                    bondName = "Received Bond",
                    payDate = today.minusDays(5),
                    payType = "coupon",
                    payVal = BigDecimal("50.00"),
                    quantity = 2,
                    currency = "UAH",
                    purchaseDate = today.minusDays(30)
                ),
                PayoutRow(
                    isin = "UA4000187348",
                    bondName = "Historical Bond",
                    payDate = today.minusDays(60),
                    payType = "2",
                    payVal = BigDecimal("1000.00"),
                    quantity = 2,
                    currency = "UAH",
                    purchaseDate = today.minusDays(30)
                )
            )
        }
        val viewModel = PayoutsViewModel(fakeDao)
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect {} }

        // Upcoming tab
        assertEquals(PayoutTab.UPCOMING, viewModel.uiState.value.selectedTab)
        assertEquals(1, viewModel.uiState.value.payouts.size)
        assertEquals("Upcoming Bond", viewModel.uiState.value.payouts[0].bondName)
        assertEquals("Coupon", viewModel.uiState.value.payouts[0].payType)

        // Switch to Received tab
        viewModel.selectTab(PayoutTab.RECEIVED)
        assertEquals(PayoutTab.RECEIVED, viewModel.uiState.value.selectedTab)
        assertEquals(1, viewModel.uiState.value.payouts.size)
        assertEquals("Received Bond", viewModel.uiState.value.payouts[0].bondName)
        assertEquals("Coupon", viewModel.uiState.value.payouts[0].payType)

        // Switch to Historical tab
        viewModel.selectTab(PayoutTab.HISTORICAL)
        assertEquals(PayoutTab.HISTORICAL, viewModel.uiState.value.selectedTab)
        assertEquals(1, viewModel.uiState.value.payouts.size)
        assertEquals("Historical Bond", viewModel.uiState.value.payouts[0].bondName)
        assertEquals("Redemption", viewModel.uiState.value.payouts[0].payType)

        collectJob.cancel()
    }

    @Test
    fun selectCurrency_filtersPayoutsByCurrency() = runTest {
        val today = LocalDate.now()
        val fakeDao = FakePayoutDao().apply {
            rowsFlow.value = listOf(
                PayoutRow(
                    isin = "UA1",
                    bondName = "UAH Bond",
                    payDate = today.plusDays(5),
                    payType = "coupon",
                    payVal = BigDecimal("100.00"),
                    quantity = 1,
                    currency = "UAH",
                    purchaseDate = today.minusDays(10)
                ),
                PayoutRow(
                    isin = "US1",
                    bondName = "USD Bond",
                    payDate = today.plusDays(5),
                    payType = "redemption",
                    payVal = BigDecimal("100.00"),
                    quantity = 1,
                    currency = "USD",
                    purchaseDate = today.minusDays(10)
                )
            )
        }
        val viewModel = PayoutsViewModel(fakeDao)
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect {} }

        // Initially defaults to first currency (UAH)
        assertEquals("UAH", viewModel.uiState.value.selectedCurrency)
        assertEquals(1, viewModel.uiState.value.payouts.size)
        assertEquals("UAH Bond", viewModel.uiState.value.payouts[0].bondName)

        viewModel.selectCurrency("USD")
        assertEquals("USD", viewModel.uiState.value.selectedCurrency)
        assertEquals(1, viewModel.uiState.value.payouts.size)
        assertEquals("USD Bond", viewModel.uiState.value.payouts[0].bondName)

        collectJob.cancel()
    }
}
