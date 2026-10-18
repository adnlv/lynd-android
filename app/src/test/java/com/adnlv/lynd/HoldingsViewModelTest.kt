package com.adnlv.lynd

import com.adnlv.lynd.data.db.BondEntity
import com.adnlv.lynd.data.db.BondPaymentEntity
import com.adnlv.lynd.data.db.HoldingDao
import com.adnlv.lynd.data.db.HoldingEntity
import com.adnlv.lynd.data.db.HoldingWithBond
import com.adnlv.lynd.data.network.NbuRepository
import com.adnlv.lynd.ui.holdings.HoldingsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class FakeHoldingDao : HoldingDao {
    val holdings = mutableMapOf<Int, HoldingEntity>()
    val allHoldingsFlow = MutableStateFlow<List<HoldingWithBond>>(emptyList())
    val paymentsFlow = MutableStateFlow<List<BondPaymentEntity>>(emptyList())
    private var nextId = 1

    override suspend fun insertHolding(holding: HoldingEntity) {
        val id = if (holding.id == 0) nextId++ else holding.id
        holdings[id] = holding.copy(id = id)
    }

    override suspend fun updateHolding(holding: HoldingEntity) {
        holdings[holding.id] = holding
    }

    override suspend fun deleteHolding(id: Int) {
        holdings.remove(id)
    }

    override suspend fun getHoldingById(id: Int): HoldingEntity? = holdings[id]

    override fun getAllHoldings(): Flow<List<HoldingWithBond>> = allHoldingsFlow

    override fun getPaymentsForHoldings(): Flow<List<BondPaymentEntity>> = paymentsFlow
}

@OptIn(ExperimentalCoroutinesApi::class)
class HoldingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun init_loadsIsinPrefixesWithUA4000First() = runTest {
        val holdingDao = FakeHoldingDao()
        val bondDao = FakeBondDao().apply {
            prefixesToReturn = listOf("UA5000", "UA4000", "UA6000")
        }
        val repository = NbuRepository(FakeNbuApiService(), bondDao, FakeSyncMetadataDao().apply {
            lastSync = System.currentTimeMillis() // not stale
        })

        val viewModel = HoldingsViewModel(holdingDao, repository, bondDao)

        assertEquals(listOf("UA4000", "UA5000", "UA6000"), viewModel.isinPrefixes.value)
    }

    @Test
    fun init_staleData_triggersSync() = runTest {
        val holdingDao = FakeHoldingDao()
        val bondDao = FakeBondDao()
        val syncDao = FakeSyncMetadataDao() // lastSync is null -> stale
        val apiService = FakeNbuApiService()
        val repository = NbuRepository(
            apiService = apiService,
            bondDao = bondDao,
            syncMetadataDao = syncDao,
            ioDispatcher = mainDispatcherRule.testDispatcher
        )

        val viewModel = HoldingsViewModel(holdingDao, repository, bondDao)

        assertNotNull(syncDao.lastSync)
        assertNull(viewModel.syncError.value)
    }

    @Test
    fun holdingCrud_saveUpdateDeleteAndRestore() = runTest {
        val holdingDao = FakeHoldingDao()
        val bondDao = FakeBondDao()
        val repository = NbuRepository(FakeNbuApiService(), bondDao, FakeSyncMetadataDao().apply {
            lastSync = System.currentTimeMillis()
        })
        val viewModel = HoldingsViewModel(holdingDao, repository, bondDao)

        val entity = HoldingEntity(
            id = 1,
            isin = "UA4000187348",
            quantity = 5,
            pricePerBond = BigDecimal("1000.00"),
            totalPaidAmount = BigDecimal("5000.00"),
            purchaseDate = LocalDate.of(2025, 1, 1)
        )

        var saved = false
        viewModel.saveHolding(entity) { saved = true }
        assertTrue(saved)
        assertEquals(5, holdingDao.holdings[1]?.quantity)

        val updatedEntity = entity.copy(quantity = 10)
        var updated = false
        viewModel.updateHolding(updatedEntity) { updated = true }
        assertTrue(updated)
        assertEquals(10, holdingDao.holdings[1]?.quantity)

        // Delete holding
        viewModel.deleteHolding(1)
        assertNull(holdingDao.holdings[1])

        // Restore holding
        viewModel.restoreHolding()
        assertEquals(10, holdingDao.holdings[1]?.quantity)
    }

    @Test
    fun searchBonds_delegatesToBondDao() = runTest {
        val holdingDao = FakeHoldingDao()
        val bondDao = FakeBondDao().apply {
            upsertBond(BondEntity("UA4000187348", "Bond 1", "UAH", BigDecimal("1000"), BigDecimal.ZERO, LocalDate.now()))
        }
        val repository = NbuRepository(FakeNbuApiService(), bondDao, FakeSyncMetadataDao().apply {
            lastSync = System.currentTimeMillis()
        })
        val viewModel = HoldingsViewModel(holdingDao, repository, bondDao)

        val result = viewModel.searchBonds("187348")
        assertEquals(listOf("UA4000187348"), result)
    }

    @Test
    fun searchBonds_withPrefixAndCodeQuery_filtersAndSorts() = runTest {
        val holdingDao = FakeHoldingDao()
        val bondDao = FakeBondDao().apply {
            upsertBond(BondEntity("UA4000187348", "Bond 1", "UAH", BigDecimal("1000"), BigDecimal.ZERO, LocalDate.now()))
            upsertBond(BondEntity("UA4000187000", "Bond 2", "UAH", BigDecimal("1000"), BigDecimal.ZERO, LocalDate.now()))
            upsertBond(BondEntity("UA4000999999", "Bond 3", "UAH", BigDecimal("1000"), BigDecimal.ZERO, LocalDate.now()))
            upsertBond(BondEntity("UA5000187348", "Bond 4", "UAH", BigDecimal("1000"), BigDecimal.ZERO, LocalDate.now()))
        }
        val repository = NbuRepository(FakeNbuApiService(), bondDao, FakeSyncMetadataDao().apply {
            lastSync = System.currentTimeMillis()
        })
        val viewModel = HoldingsViewModel(holdingDao, repository, bondDao)

        val result = viewModel.searchBonds("UA4000", "187")
        assertEquals(listOf("UA4000187000", "UA4000187348"), result)
    }

    @Test
    fun getBond_delegatesToBondDao() = runTest {
        val holdingDao = FakeHoldingDao()
        val bondDao = FakeBondDao().apply {
            upsertBond(BondEntity("UA4000187348", "Bond 1", "USD", BigDecimal("1000"), BigDecimal.ZERO, LocalDate.now()))
        }
        val repository = NbuRepository(FakeNbuApiService(), bondDao, FakeSyncMetadataDao().apply {
            lastSync = System.currentTimeMillis()
        })
        val viewModel = HoldingsViewModel(holdingDao, repository, bondDao)

        val bond = viewModel.getBond("UA4000187348")
        assertEquals("USD", bond?.currency)
    }

    @Test
    fun groupedHoldings_sortsByIsinAndIgnoresPurchaseDate() = runTest {
        val holdingDao = FakeHoldingDao()
        val bondDao = FakeBondDao()
        val repository = NbuRepository(FakeNbuApiService(), bondDao, FakeSyncMetadataDao().apply {
            lastSync = System.currentTimeMillis()
        })
        val viewModel = HoldingsViewModel(holdingDao, repository, bondDao)

        val itemA = HoldingWithBond(
            id = 1,
            isin = "UA4000200000",
            bondName = "Bond B",
            quantity = 2,
            pricePerBond = BigDecimal("1000.00"),
            totalPaidAmount = BigDecimal("2000.00"),
            purchaseDate = LocalDate.of(2026, 10, 1),
            currency = "UAH",
            couponRate = BigDecimal.ZERO
        )
        val itemB = HoldingWithBond(
            id = 2,
            isin = "UA4000100000",
            bondName = "Bond A",
            quantity = 5,
            pricePerBond = BigDecimal("1000.00"),
            totalPaidAmount = BigDecimal("5000.00"),
            purchaseDate = LocalDate.of(2026, 1, 1),
            currency = "UAH",
            couponRate = BigDecimal.ZERO
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.groupedHoldings.collect {}
        }

        holdingDao.allHoldingsFlow.value = listOf(itemA, itemB)

        val groups = viewModel.groupedHoldings.value
        assertEquals(2, groups.size)
        assertEquals("UA4000100000", groups[0].isin)
        assertEquals("UA4000200000", groups[1].isin)
    }

    @Test
    fun groupedHoldings_orderRemainsStableWhenHoldingRemoved() = runTest {
        val holdingDao = FakeHoldingDao()
        val bondDao = FakeBondDao()
        val repository = NbuRepository(FakeNbuApiService(), bondDao, FakeSyncMetadataDao().apply {
            lastSync = System.currentTimeMillis()
        })
        val viewModel = HoldingsViewModel(holdingDao, repository, bondDao)

        val bond1 = HoldingWithBond(
            id = 1,
            isin = "UA4000100000",
            bondName = "Bond 1",
            quantity = 1,
            pricePerBond = BigDecimal("1000.00"),
            totalPaidAmount = BigDecimal("1000.00"),
            purchaseDate = LocalDate.of(2026, 1, 1),
            currency = "UAH",
            couponRate = BigDecimal.ZERO
        )
        val bond2 = HoldingWithBond(
            id = 2,
            isin = "UA4000200000",
            bondName = "Bond 2",
            quantity = 2,
            pricePerBond = BigDecimal("1000.00"),
            totalPaidAmount = BigDecimal("2000.00"),
            purchaseDate = LocalDate.of(2026, 5, 1),
            currency = "UAH",
            couponRate = BigDecimal.ZERO
        )
        val bond3 = HoldingWithBond(
            id = 3,
            isin = "UA4000300000",
            bondName = "Bond 3",
            quantity = 3,
            pricePerBond = BigDecimal("1000.00"),
            totalPaidAmount = BigDecimal("3000.00"),
            purchaseDate = LocalDate.of(2026, 3, 1),
            currency = "UAH",
            couponRate = BigDecimal.ZERO
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.groupedHoldings.collect {}
        }

        holdingDao.allHoldingsFlow.value = listOf(bond1, bond2, bond3)
        assertEquals(listOf("UA4000100000", "UA4000200000", "UA4000300000"), viewModel.groupedHoldings.value.map { it.isin })

        holdingDao.allHoldingsFlow.value = listOf(bond1, bond3)
        assertEquals(listOf("UA4000100000", "UA4000300000"), viewModel.groupedHoldings.value.map { it.isin })
    }
}
