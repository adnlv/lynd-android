package com.adnlv.lynd

import com.adnlv.lynd.data.db.BondDao
import com.adnlv.lynd.data.db.BondEntity
import com.adnlv.lynd.data.db.BondPaymentEntity
import com.adnlv.lynd.data.db.SyncMetadataDao
import com.adnlv.lynd.data.db.SyncMetadataEntity
import com.adnlv.lynd.data.network.NbuApiService
import com.adnlv.lynd.data.network.NbuPaymentDto
import com.adnlv.lynd.data.network.NbuRepository
import com.adnlv.lynd.data.network.NbuSecurityDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class FakeNbuApiService : NbuApiService {
    var securitiesResponse: List<NbuSecurityDto> = emptyList()
    var shouldThrow: Boolean = false

    override suspend fun getSecurities(): List<NbuSecurityDto> {
        if (shouldThrow) {
            throw RuntimeException("Network error")
        }
        return securitiesResponse
    }
}

class FakeBondDao : BondDao {
    val bonds = mutableMapOf<String, BondEntity>()
    val payments = mutableListOf<BondPaymentEntity>()
    var prefixesToReturn: List<String> = emptyList()

    override suspend fun upsertBond(bond: BondEntity) {
        bonds[bond.isin] = bond
    }

    override suspend fun upsertBonds(bonds: List<BondEntity>) {
        bonds.forEach { this.bonds[it.isin] = it }
    }

    override suspend fun upsertPayments(payments: List<BondPaymentEntity>) {
        this.payments.addAll(payments)
    }

    override suspend fun deletePaymentsForBond(isin: String) {
        payments.removeAll { it.bondIsin == isin }
    }

    override suspend fun deleteAllPayments() {
        payments.clear()
    }

    override suspend fun getBond(isin: String): BondEntity? {
        return bonds[isin]
    }

    override suspend fun searchBondsByIsin(query: String): List<String> {
        return bonds.keys.filter { it.contains(query, ignoreCase = true) }.sorted()
    }

    override suspend fun getDistinctIsinPrefixes(): List<String> {
        return prefixesToReturn
    }
}

class FakeSyncMetadataDao : SyncMetadataDao {
    var lastSync: Long? = null

    override suspend fun getLastSyncTime(): Long? = lastSync

    override suspend fun upsertSyncTime(metadata: SyncMetadataEntity) {
        lastSync = metadata.lastSyncedAt
    }
}

class NbuRepositoryTest {

    @Test
    fun isDataStale_returnsTrueWhenLastSyncMissing() = runTest {
        val api = FakeNbuApiService()
        val bondDao = FakeBondDao()
        val syncDao = FakeSyncMetadataDao()
        val repository = NbuRepository(api, bondDao, syncDao)

        assertTrue(repository.isDataStale())
    }

    @Test
    fun isDataStale_returnsFalseWhenSyncWithin12Hours() = runTest {
        val api = FakeNbuApiService()
        val bondDao = FakeBondDao()
        val syncDao = FakeSyncMetadataDao().apply {
            lastSync = System.currentTimeMillis() - (1 * 60 * 60 * 1000L) // 1 hour ago
        }
        val repository = NbuRepository(api, bondDao, syncDao)

        assertFalse(repository.isDataStale())
    }

    @Test
    fun isDataStale_returnsTrueWhenSyncOlderThan12Hours() = runTest {
        val api = FakeNbuApiService()
        val bondDao = FakeBondDao()
        val syncDao = FakeSyncMetadataDao().apply {
            lastSync = System.currentTimeMillis() - (13 * 60 * 60 * 1000L) // 13 hours ago
        }
        val repository = NbuRepository(api, bondDao, syncDao)

        assertTrue(repository.isDataStale())
    }

    @Test
    fun syncAllBonds_mapsEntitiesNormalizesPaymentTypesAndSavesSyncTime() = runTest {
        val api = FakeNbuApiService().apply {
            securitiesResponse = listOf(
                NbuSecurityDto(
                    cpcode = "UA4000187348",
                    nominal = 1000.0,
                    aukProc = 12.0,
                    pgsDate = "2026-05-20",
                    valCode = "UAH",
                    emitName = "Gov Bond 2026",
                    payments = listOf(
                        NbuPaymentDto(payDate = "2025-11-20", payType = "1", payVal = 60.0),
                        NbuPaymentDto(payDate = "2026-05-20", payType = "2", payVal = 1060.0)
                    )
                ),
                NbuSecurityDto(cpcode = "   ") // blank ISIN, should be skipped
            )
        }
        val bondDao = FakeBondDao()
        val syncDao = FakeSyncMetadataDao()
        val repository = NbuRepository(api, bondDao, syncDao)

        val result = repository.syncAllBonds()

        assertTrue(result.isSuccess)
        assertEquals(1, bondDao.bonds.size)
        val bond = bondDao.bonds["UA4000187348"]
        assertNotNull(bond)
        assertEquals("Gov Bond 2026", bond?.name)
        assertEquals("UAH", bond?.currency)
        assertEquals(LocalDate.of(2026, 5, 20), bond?.maturityDate)

        assertEquals(2, bondDao.payments.size)
        assertEquals("coupon", bondDao.payments[0].payType)
        assertEquals("redemption", bondDao.payments[1].payType)

        assertNotNull(syncDao.lastSync)
    }

    @Test
    fun syncAllBonds_onNetworkFailure_returnsFailureResult() = runTest {
        val api = FakeNbuApiService().apply { shouldThrow = true }
        val bondDao = FakeBondDao()
        val syncDao = FakeSyncMetadataDao()
        val repository = NbuRepository(api, bondDao, syncDao)

        val result = repository.syncAllBonds()

        assertTrue(result.isFailure)
        assertNull(syncDao.lastSync)
    }

    @Test
    fun getLocalBond_queriesDaoWithTrimmedIsin() = runTest {
        val bondDao = FakeBondDao().apply {
            upsertBond(
                BondEntity(
                    isin = "UA4000187348",
                    name = "Test Bond",
                    currency = "UAH",
                    nominalValue = BigDecimal("1000.00"),
                    couponRate = BigDecimal("10.00"),
                    maturityDate = LocalDate.of(2026, 1, 1)
                )
            )
        }
        val repository = NbuRepository(FakeNbuApiService(), bondDao, FakeSyncMetadataDao())

        val result = repository.getLocalBond("  UA4000187348  ")
        assertNotNull(result)
        assertEquals("UA4000187348", result?.isin)
    }

    @Test
    fun searchMatchingIsins_returnsEmptyForBlankAndDelegatesForQuery() = runTest {
        val bondDao = FakeBondDao().apply {
            upsertBond(BondEntity("UA4000187348", "Bond 1", "UAH", BigDecimal("1000"), BigDecimal.ZERO, LocalDate.now()))
            upsertBond(BondEntity("UA4000200000", "Bond 2", "UAH", BigDecimal("1000"), BigDecimal.ZERO, LocalDate.now()))
        }
        val repository = NbuRepository(FakeNbuApiService(), bondDao, FakeSyncMetadataDao())

        assertTrue(repository.searchMatchingIsins("   ").isEmpty())

        val searchResult = repository.searchMatchingIsins(" 187348 ")
        assertEquals(listOf("UA4000187348"), searchResult)
    }

    @Test
    fun getIsinPrefixes_returnsDaoPrefixesOrDefault() = runTest {
        val bondDao = FakeBondDao()
        val repository = NbuRepository(FakeNbuApiService(), bondDao, FakeSyncMetadataDao())

        assertEquals(listOf("UA4000"), repository.getIsinPrefixes())

        bondDao.prefixesToReturn = listOf("UA4000", "UA4001")
        assertEquals(listOf("UA4000", "UA4001"), repository.getIsinPrefixes())
    }

    @Test
    fun getOrFetchBond_cachedHit_returnsCachedEntityWithoutApiCall() = runTest {
        val api = FakeNbuApiService().apply { shouldThrow = true }
        val bondDao = FakeBondDao().apply {
            upsertBond(BondEntity("UA4000187348", "Cached Bond", "UAH", BigDecimal("1000"), BigDecimal.ZERO, LocalDate.now()))
        }
        val repository = NbuRepository(api, bondDao, FakeSyncMetadataDao())

        val result = repository.getOrFetchBond("UA4000187348")
        assertTrue(result.isSuccess)
        assertEquals("Cached Bond", result.getOrNull()?.name)
    }

    @Test
    fun getOrFetchBond_cacheMiss_fetchesFromApiAndCaches() = runTest {
        val api = FakeNbuApiService().apply {
            securitiesResponse = listOf(
                NbuSecurityDto(
                    cpcode = "UA4000187348",
                    nominal = 1000.0,
                    aukProc = 14.0,
                    pgsDate = "2027-01-15",
                    valCode = "USD",
                    emitName = "Remote Bond",
                    payments = listOf(
                        NbuPaymentDto(payDate = "2027-01-15", payType = "2", payVal = 1000.0)
                    )
                )
            )
        }
        val bondDao = FakeBondDao()
        val repository = NbuRepository(api, bondDao, FakeSyncMetadataDao())

        val result = repository.getOrFetchBond("UA4000187348")
        assertTrue(result.isSuccess)
        val bond = result.getOrNull()
        assertNotNull(bond)
        assertEquals("Remote Bond", bond?.name)
        assertEquals("USD", bond?.currency)

        assertNotNull(bondDao.bonds["UA4000187348"])
        assertEquals(1, bondDao.payments.size)
    }

    @Test
    fun getOrFetchBond_notFoundInApi_returnsFailure() = runTest {
        val api = FakeNbuApiService().apply { securitiesResponse = emptyList() }
        val bondDao = FakeBondDao()
        val repository = NbuRepository(api, bondDao, FakeSyncMetadataDao())

        val result = repository.getOrFetchBond("UA4000999999")
        assertTrue(result.isFailure)
    }
}
