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

    @Test
    fun availableCurrencies_prioritizesUahFirst() = runTest {
        val today = LocalDate.now()
        val fakeDao = FakePayoutDao().apply {
            rowsFlow.value = listOf(
                PayoutRow(
                    isin = "EUR1",
                    bondName = "EUR Bond",
                    payDate = today.plusDays(5),
                    payType = "coupon",
                    payVal = BigDecimal("100.00"),
                    quantity = 1,
                    currency = "EUR",
                    purchaseDate = today.minusDays(10)
                ),
                PayoutRow(
                    isin = "USD1",
                    bondName = "USD Bond",
                    payDate = today.plusDays(5),
                    payType = "coupon",
                    payVal = BigDecimal("100.00"),
                    quantity = 1,
                    currency = "USD",
                    purchaseDate = today.minusDays(10)
                ),
                PayoutRow(
                    isin = "UAH1",
                    bondName = "UAH Bond",
                    payDate = today.plusDays(5),
                    payType = "coupon",
                    payVal = BigDecimal("100.00"),
                    quantity = 1,
                    currency = "UAH",
                    purchaseDate = today.minusDays(10)
                )
            )
        }
        val viewModel = PayoutsViewModel(fakeDao)
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect {} }

        assertEquals(listOf("UAH", "EUR", "USD"), viewModel.uiState.value.availableCurrencies)
        assertEquals("UAH", viewModel.uiState.value.selectedCurrency)
        assertEquals(1, viewModel.uiState.value.payouts.size)
        assertEquals("UAH Bond", viewModel.uiState.value.payouts[0].bondName)

        collectJob.cancel()
    }

    @Test
    fun toggleCurrency_supportsMultipleCurrenciesAndFilters() = runTest {
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
                    payDate = today.plusDays(6),
                    payType = "coupon",
                    payVal = BigDecimal("200.00"),
                    quantity = 1,
                    currency = "USD",
                    purchaseDate = today.minusDays(10)
                ),
                PayoutRow(
                    isin = "EU1",
                    bondName = "EUR Bond",
                    payDate = today.plusDays(7),
                    payType = "coupon",
                    payVal = BigDecimal("300.00"),
                    quantity = 1,
                    currency = "EUR",
                    purchaseDate = today.minusDays(10)
                )
            )
        }
        val viewModel = PayoutsViewModel(fakeDao)
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect {} }

        assertEquals(setOf("UAH"), viewModel.uiState.value.selectedCurrencies)
        assertEquals(1, viewModel.uiState.value.payouts.size)

        // Toggle USD on
        viewModel.toggleCurrency("USD")
        assertEquals(setOf("UAH", "USD"), viewModel.uiState.value.selectedCurrencies)
        assertEquals(2, viewModel.uiState.value.payouts.size)

        // Toggle UAH off -> USD remains
        viewModel.toggleCurrency("UAH")
        assertEquals(setOf("USD"), viewModel.uiState.value.selectedCurrencies)
        assertEquals(1, viewModel.uiState.value.payouts.size)
        assertEquals("USD Bond", viewModel.uiState.value.payouts[0].bondName)

        // Cannot deselect last remaining currency
        viewModel.toggleCurrency("USD")
        assertEquals(setOf("USD"), viewModel.uiState.value.selectedCurrencies)
        assertEquals(1, viewModel.uiState.value.payouts.size)

        collectJob.cancel()
    }

    @Test
    fun selectAllCurrencies_selectsAllProvidedCurrencies() = runTest {
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
                    payDate = today.plusDays(6),
                    payType = "coupon",
                    payVal = BigDecimal("200.00"),
                    quantity = 1,
                    currency = "USD",
                    purchaseDate = today.minusDays(10)
                )
            )
        }
        val viewModel = PayoutsViewModel(fakeDao)
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect {} }

        viewModel.selectAllCurrencies(listOf("UAH", "USD"))
        assertEquals(setOf("UAH", "USD"), viewModel.uiState.value.selectedCurrencies)
        assertEquals(2, viewModel.uiState.value.payouts.size)

        collectJob.cancel()
    }

    @Test
    fun dateGroups_singleIsinWithCouponAndRedemption_groupsAccurately() = runTest {
        val today = LocalDate.now()
        val payDate = today.plusDays(5)
        val purchaseDate1 = today.minusDays(20)
        val purchaseDate2 = today.minusDays(10)

        val fakeDao = FakePayoutDao().apply {
            rowsFlow.value = listOf(
                PayoutRow(
                    isin = "UA4000190441",
                    bondName = "Test Bond",
                    payDate = payDate,
                    payType = "coupon",
                    payVal = BigDecimal("50.00"),
                    quantity = 10,
                    currency = "UAH",
                    purchaseDate = purchaseDate1,
                    holdingId = 1
                ),
                PayoutRow(
                    isin = "UA4000190441",
                    bondName = "Test Bond",
                    payDate = payDate,
                    payType = "redemption",
                    payVal = BigDecimal("1000.00"),
                    quantity = 10,
                    currency = "UAH",
                    purchaseDate = purchaseDate1,
                    holdingId = 1
                ),
                PayoutRow(
                    isin = "UA4000190441",
                    bondName = "Test Bond",
                    payDate = payDate,
                    payType = "coupon",
                    payVal = BigDecimal("50.00"),
                    quantity = 5,
                    currency = "UAH",
                    purchaseDate = purchaseDate2,
                    holdingId = 2
                ),
                PayoutRow(
                    isin = "UA4000190441",
                    bondName = "Test Bond",
                    payDate = payDate,
                    payType = "redemption",
                    payVal = BigDecimal("1000.00"),
                    quantity = 5,
                    currency = "UAH",
                    purchaseDate = purchaseDate2,
                    holdingId = 2
                )
            )
        }

        val viewModel = PayoutsViewModel(fakeDao)
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect {} }

        val groups = viewModel.uiState.value.dateGroups
        assertEquals(1, groups.size)

        val group = groups[0]
        assertEquals(payDate, group.date)
        assertTrue(group.isSingleBond)
        assertEquals(setOf(com.adnlv.lynd.domain.PayoutType.REDEMPTION), group.payoutTypes)

        // Total amount for date: (50 * 15) + (1000 * 15) = 750 + 15000 = 15750.00
        assertEquals(1, group.totalAmounts.size)
        assertEquals("UAH", group.totalAmounts[0].currency)
        assertEquals(0, BigDecimal("15750.00").compareTo(group.totalAmounts[0].amount))

        val bond = group.singleBond!!
        assertEquals("UA4000190441", bond.isin)
        assertEquals(15, bond.totalQuantity)
        assertEquals(0, BigDecimal("750.00").compareTo(bond.couponAmount))
        assertEquals(0, BigDecimal("15000.00").compareTo(bond.principalAmount))
        assertEquals(0, BigDecimal("15750.00").compareTo(bond.totalAmount))
        assertEquals(setOf(com.adnlv.lynd.domain.PayoutType.REDEMPTION), bond.payoutTypes)

        // Lot breakdown: Lot 1 (qty 10, amount 10500), Lot 2 (qty 5, amount 5250)
        assertEquals(2, bond.lots.size)
        assertEquals(10, bond.lots[0].quantity)
        assertEquals(purchaseDate1, bond.lots[0].purchaseDate)
        assertEquals(0, BigDecimal("10500.00").compareTo(bond.lots[0].amount))

        assertEquals(5, bond.lots[1].quantity)
        assertEquals(purchaseDate2, bond.lots[1].purchaseDate)
        assertEquals(0, BigDecimal("5250.00").compareTo(bond.lots[1].amount))

        collectJob.cancel()
    }

    @Test
    fun dateGroups_separateCouponAndCombinedRedemptionRows_showsBothChipsInGroupAndRedemptionInCombinedRow() = runTest {
        val today = LocalDate.now()
        val payDate = today.plusDays(5)

        val fakeDao = FakePayoutDao().apply {
            rowsFlow.value = listOf(
                PayoutRow(
                    isin = "UA1",
                    bondName = "Coupon Bond",
                    payDate = payDate,
                    payType = "coupon",
                    payVal = BigDecimal("50.00"),
                    quantity = 2,
                    currency = "UAH",
                    purchaseDate = today.minusDays(10),
                    holdingId = 1
                ),
                PayoutRow(
                    isin = "UA2",
                    bondName = "Combined Bond",
                    payDate = payDate,
                    payType = "coupon",
                    payVal = BigDecimal("40.00"),
                    quantity = 1,
                    currency = "UAH",
                    purchaseDate = today.minusDays(10),
                    holdingId = 2
                ),
                PayoutRow(
                    isin = "UA2",
                    bondName = "Combined Bond",
                    payDate = payDate,
                    payType = "redemption",
                    payVal = BigDecimal("1000.00"),
                    quantity = 1,
                    currency = "UAH",
                    purchaseDate = today.minusDays(10),
                    holdingId = 2
                )
            )
        }

        val viewModel = PayoutsViewModel(fakeDao)
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect {} }

        val groups = viewModel.uiState.value.dateGroups
        assertEquals(1, groups.size)

        val group = groups[0]
        assertEquals(false, group.isSingleBond)
        assertEquals(2, group.bonds.size)
        assertEquals(setOf(com.adnlv.lynd.domain.PayoutType.COUPON, com.adnlv.lynd.domain.PayoutType.REDEMPTION), group.payoutTypes)

        val couponBond = group.bonds.first { it.isin == "UA1" }
        assertEquals(setOf(com.adnlv.lynd.domain.PayoutType.COUPON), couponBond.payoutTypes)

        val combinedBond = group.bonds.first { it.isin == "UA2" }
        assertEquals(setOf(com.adnlv.lynd.domain.PayoutType.REDEMPTION), combinedBond.payoutTypes)

        collectJob.cancel()
    }

    @Test
    fun dateGroups_multipleIsinsOnSameDate_groupsAccurately() = runTest {
        val today = LocalDate.now()
        val payDate = today.plusDays(3)

        val fakeDao = FakePayoutDao().apply {
            rowsFlow.value = listOf(
                PayoutRow(
                    isin = "UA1",
                    bondName = "Bond 1",
                    payDate = payDate,
                    payType = "coupon",
                    payVal = BigDecimal("50.00"),
                    quantity = 2,
                    currency = "UAH",
                    purchaseDate = today.minusDays(10),
                    holdingId = 1
                ),
                PayoutRow(
                    isin = "UA2",
                    bondName = "Bond 2",
                    payDate = payDate,
                    payType = "redemption",
                    payVal = BigDecimal("1000.00"),
                    quantity = 1,
                    currency = "UAH",
                    purchaseDate = today.minusDays(10),
                    holdingId = 2
                )
            )
        }

        val viewModel = PayoutsViewModel(fakeDao)
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect {} }

        val groups = viewModel.uiState.value.dateGroups
        assertEquals(1, groups.size)

        val group = groups[0]
        assertEquals(false, group.isSingleBond)
        assertEquals(2, group.bonds.size)
        assertEquals(setOf(com.adnlv.lynd.domain.PayoutType.COUPON, com.adnlv.lynd.domain.PayoutType.REDEMPTION), group.payoutTypes)

        val bond1 = group.bonds.first { it.isin == "UA1" }
        assertEquals(setOf(com.adnlv.lynd.domain.PayoutType.COUPON), bond1.payoutTypes)
        assertEquals(0, BigDecimal("100.00").compareTo(bond1.totalAmount))

        val bond2 = group.bonds.first { it.isin == "UA2" }
        assertEquals(setOf(com.adnlv.lynd.domain.PayoutType.REDEMPTION), bond2.payoutTypes)
        assertEquals(0, BigDecimal("1000.00").compareTo(bond2.totalAmount))

        collectJob.cancel()
    }

    @Test
    fun dateGroups_multiCurrencyOnSameDate_computesTotalsPerCurrency() = runTest {
        val today = LocalDate.now()
        val payDate = today.plusDays(4)

        val fakeDao = FakePayoutDao().apply {
            rowsFlow.value = listOf(
                PayoutRow(
                    isin = "UA1",
                    bondName = "UAH Bond",
                    payDate = payDate,
                    payType = "coupon",
                    payVal = BigDecimal("100.00"),
                    quantity = 1,
                    currency = "UAH",
                    purchaseDate = today.minusDays(10),
                    holdingId = 1
                ),
                PayoutRow(
                    isin = "US1",
                    bondName = "USD Bond",
                    payDate = payDate,
                    payType = "coupon",
                    payVal = BigDecimal("50.00"),
                    quantity = 2,
                    currency = "USD",
                    purchaseDate = today.minusDays(10),
                    holdingId = 2
                )
            )
        }

        val viewModel = PayoutsViewModel(fakeDao)
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect {} }

        viewModel.selectAllCurrencies(listOf("UAH", "USD"))

        val groups = viewModel.uiState.value.dateGroups
        assertEquals(1, groups.size)

        val group = groups[0]
        assertEquals(2, group.totalAmounts.size)
        val uahTotal = group.totalAmounts.first { it.currency == "UAH" }
        val usdTotal = group.totalAmounts.first { it.currency == "USD" }
        assertEquals(0, BigDecimal("100.00").compareTo(uahTotal.amount))
        assertEquals(0, BigDecimal("100.00").compareTo(usdTotal.amount))

        collectJob.cancel()
    }

    @Test
    fun initialState_defaultsToBothPayoutTypesSelected() = runTest {
        val fakeDao = FakePayoutDao()
        val viewModel = PayoutsViewModel(fakeDao)

        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect {} }

        assertEquals(
            setOf(com.adnlv.lynd.domain.PayoutType.REDEMPTION, com.adnlv.lynd.domain.PayoutType.COUPON),
            viewModel.uiState.value.selectedPayoutTypes
        )

        collectJob.cancel()
    }

    @Test
    fun selectPayoutType_filtersPayoutsAndDateGroupsByType() = runTest {
        val today = LocalDate.now()
        val fakeDao = FakePayoutDao().apply {
            rowsFlow.value = listOf(
                PayoutRow(
                    isin = "UA1",
                    bondName = "Coupon Bond",
                    payDate = today.plusDays(5),
                    payType = "coupon",
                    payVal = BigDecimal("50.00"),
                    quantity = 1,
                    currency = "UAH",
                    purchaseDate = today.minusDays(10)
                ),
                PayoutRow(
                    isin = "UA2",
                    bondName = "Redemption Bond",
                    payDate = today.plusDays(5),
                    payType = "redemption",
                    payVal = BigDecimal("1000.00"),
                    quantity = 1,
                    currency = "UAH",
                    purchaseDate = today.minusDays(10)
                )
            )
        }
        val viewModel = PayoutsViewModel(fakeDao)
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect {} }

        // Initially both are selected
        assertEquals(2, viewModel.uiState.value.payouts.size)
        assertEquals(1, viewModel.uiState.value.dateGroups.size)

        // Select Coupon only
        viewModel.selectPayoutType(com.adnlv.lynd.domain.PayoutType.COUPON)
        assertEquals(setOf(com.adnlv.lynd.domain.PayoutType.COUPON), viewModel.uiState.value.selectedPayoutTypes)
        assertEquals(1, viewModel.uiState.value.payouts.size)
        assertEquals("Coupon Bond", viewModel.uiState.value.payouts[0].bondName)
        assertEquals(setOf(com.adnlv.lynd.domain.PayoutType.COUPON), viewModel.uiState.value.dateGroups[0].payoutTypes)

        // Select Redemption only
        viewModel.selectPayoutType(com.adnlv.lynd.domain.PayoutType.REDEMPTION)
        assertEquals(setOf(com.adnlv.lynd.domain.PayoutType.REDEMPTION), viewModel.uiState.value.selectedPayoutTypes)
        assertEquals(1, viewModel.uiState.value.payouts.size)
        assertEquals("Redemption Bond", viewModel.uiState.value.payouts[0].bondName)
        assertEquals(setOf(com.adnlv.lynd.domain.PayoutType.REDEMPTION), viewModel.uiState.value.dateGroups[0].payoutTypes)

        collectJob.cancel()
    }

    @Test
    fun togglePayoutType_supportsMultiSelectAndPreventsEmptySelection() = runTest {
        val today = LocalDate.now()
        val fakeDao = FakePayoutDao().apply {
            rowsFlow.value = listOf(
                PayoutRow(
                    isin = "UA1",
                    bondName = "Coupon Bond",
                    payDate = today.plusDays(5),
                    payType = "coupon",
                    payVal = BigDecimal("50.00"),
                    quantity = 1,
                    currency = "UAH",
                    purchaseDate = today.minusDays(10)
                ),
                PayoutRow(
                    isin = "UA2",
                    bondName = "Redemption Bond",
                    payDate = today.plusDays(5),
                    payType = "redemption",
                    payVal = BigDecimal("1000.00"),
                    quantity = 1,
                    currency = "UAH",
                    purchaseDate = today.minusDays(10)
                )
            )
        }
        val viewModel = PayoutsViewModel(fakeDao)
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect {} }

        // Start with both types selected
        assertEquals(2, viewModel.uiState.value.payouts.size)

        // Toggle Coupon off -> Redemption remains
        viewModel.togglePayoutType(com.adnlv.lynd.domain.PayoutType.COUPON)
        assertEquals(setOf(com.adnlv.lynd.domain.PayoutType.REDEMPTION), viewModel.uiState.value.selectedPayoutTypes)
        assertEquals(1, viewModel.uiState.value.payouts.size)
        assertEquals("Redemption Bond", viewModel.uiState.value.payouts[0].bondName)

        // Cannot deselect the last remaining type
        viewModel.togglePayoutType(com.adnlv.lynd.domain.PayoutType.REDEMPTION)
        assertEquals(setOf(com.adnlv.lynd.domain.PayoutType.REDEMPTION), viewModel.uiState.value.selectedPayoutTypes)
        assertEquals(1, viewModel.uiState.value.payouts.size)

        // Toggle Coupon back on -> both selected
        viewModel.togglePayoutType(com.adnlv.lynd.domain.PayoutType.COUPON)
        assertEquals(
            setOf(com.adnlv.lynd.domain.PayoutType.REDEMPTION, com.adnlv.lynd.domain.PayoutType.COUPON),
            viewModel.uiState.value.selectedPayoutTypes
        )
        assertEquals(2, viewModel.uiState.value.payouts.size)

        collectJob.cancel()
    }
}
