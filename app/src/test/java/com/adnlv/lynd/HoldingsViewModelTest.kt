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
}
