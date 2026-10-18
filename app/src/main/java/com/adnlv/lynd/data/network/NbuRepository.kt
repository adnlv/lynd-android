package com.adnlv.lynd.data.network

import com.adnlv.lynd.data.db.BondDao
import com.adnlv.lynd.data.db.BondEntity
import com.adnlv.lynd.data.db.BondPaymentEntity
import com.adnlv.lynd.data.db.SyncMetadataDao
import com.adnlv.lynd.data.db.SyncMetadataEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NbuRepository(
    private val apiService: NbuApiService,
    private val bondDao: BondDao,
    private val syncMetadataDao: SyncMetadataDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    companion object {
        const val SYNC_INTERVAL_MS: Long = 12 * 60 * 60 * 1000L
        const val DEFAULT_SYNC_METADATA_ID = 1
        const val FALLBACK_ISIN_PREFIX = "UA4000"
    }

    suspend fun isDataStale(): Boolean = withContext(ioDispatcher) {
        val lastSync = syncMetadataDao.getLastSyncTime() ?: return@withContext true
        val currentTime = System.currentTimeMillis()
        (currentTime - lastSync) >= SYNC_INTERVAL_MS
    }

    suspend fun syncAllBonds(): Result<Unit> = withContext(ioDispatcher) {
        try {
            val securities = apiService.getSecurities()
            val bonds = ArrayList<BondEntity>(securities.size)
            val payments = ArrayList<BondPaymentEntity>()

            for (security in securities) {
                val isin = security.cpcode?.trim() ?: continue
                if (isin.isBlank()) continue

                bonds.add(security.toBondEntity(isin))
                payments.addAll(security.payments.toBondPaymentEntities(isin))
            }

            if (bonds.isNotEmpty()) {
                bondDao.upsertBonds(bonds)
            }
            if (payments.isNotEmpty()) {
                bondDao.deleteAllPayments()
                bondDao.upsertPayments(payments)
            }

            syncMetadataDao.upsertSyncTime(
                SyncMetadataEntity(id = DEFAULT_SYNC_METADATA_ID, lastSyncedAt = System.currentTimeMillis())
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLocalBond(isin: String): BondEntity? = withContext(ioDispatcher) {
        bondDao.getBond(isin.trim())
    }

    suspend fun searchMatchingIsins(query: String): List<String> = withContext(ioDispatcher) {
        if (query.isBlank()) {
            emptyList()
        } else {
            bondDao.searchBondsByIsin(query.trim())
        }
    }

    suspend fun getIsinPrefixes(): List<String> = withContext(ioDispatcher) {
        val prefixes = bondDao.getDistinctIsinPrefixes()
        if (prefixes.isNotEmpty()) prefixes else listOf(FALLBACK_ISIN_PREFIX)
    }

    suspend fun getOrFetchBond(isin: String): Result<BondEntity> = withContext(ioDispatcher) {
        val trimmedIsin = isin.trim()
        val cached = bondDao.getBond(trimmedIsin)
        if (cached != null) {
            return@withContext Result.success(cached)
        }

        try {
            val securities = apiService.getSecurities()
            val security = securities.find { it.cpcode?.trim().equals(trimmedIsin, ignoreCase = true) }
                ?: return@withContext Result.failure(IllegalArgumentException("Bond with ISIN $trimmedIsin not found"))

            val bondEntity = security.toBondEntity(trimmedIsin)
            val payments = security.payments.toBondPaymentEntities(trimmedIsin)

            bondDao.upsertBond(bondEntity)
            if (payments.isNotEmpty()) {
                bondDao.deletePaymentsForBond(trimmedIsin)
                bondDao.upsertPayments(payments)
            }

            Result.success(bondEntity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
