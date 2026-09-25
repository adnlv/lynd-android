package com.adnlv.lynd.data.network

import com.adnlv.lynd.data.db.BondDao
import com.adnlv.lynd.data.db.BondEntity
import com.adnlv.lynd.data.db.BondPaymentEntity
import com.adnlv.lynd.data.db.SyncMetadataDao
import com.adnlv.lynd.data.db.SyncMetadataEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.time.LocalDate

class NbuRepository(
    private val apiService: NbuApiService,
    private val bondDao: BondDao,
    private val syncMetadataDao: SyncMetadataDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    companion object {
        const val SYNC_INTERVAL_MS: Long = 12 * 60 * 60 * 1000L // 12 hours
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

                val bond = BondEntity(
                    isin = isin,
                    name = security.emitName ?: isin,
                    currency = security.valCode ?: "UAH",
                    nominalValue = security.nominal?.let { BigDecimal.valueOf(it) } ?: BigDecimal("1000.00"),
                    couponRate = security.aukProc?.let { BigDecimal.valueOf(it) } ?: BigDecimal.ZERO,
                    maturityDate = security.pgsDate?.let { LocalDate.parse(it) } ?: LocalDate.now()
                )
                bonds.add(bond)

                security.payments.orEmpty().mapNotNullTo(payments) { dto ->
                    val payDateStr = dto.payDate ?: return@mapNotNullTo null
                    val type = when (dto.payType) {
                        "1" -> "coupon"
                        "2" -> "redemption"
                        else -> dto.payType ?: "coupon"
                    }
                    val payVal = dto.payVal?.let { BigDecimal.valueOf(it) } ?: BigDecimal.ZERO
                    BondPaymentEntity(
                        bondIsin = isin,
                        payDate = LocalDate.parse(payDateStr),
                        payType = type,
                        payVal = payVal
                    )
                }
            }

            if (bonds.isNotEmpty()) {
                bondDao.upsertBonds(bonds)
            }
            if (payments.isNotEmpty()) {
                bondDao.deleteAllPayments()
                bondDao.upsertPayments(payments)
            }

            syncMetadataDao.upsertSyncTime(
                SyncMetadataEntity(id = 1, lastSyncedAt = System.currentTimeMillis())
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
        if (prefixes.isNotEmpty()) prefixes else listOf("UA4000")
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

            val bondEntity = BondEntity(
                isin = trimmedIsin,
                name = security.emitName ?: trimmedIsin,
                currency = security.valCode ?: "UAH",
                nominalValue = security.nominal?.let { BigDecimal.valueOf(it) } ?: BigDecimal("1000.00"),
                couponRate = security.aukProc?.let { BigDecimal.valueOf(it) } ?: BigDecimal.ZERO,
                maturityDate = security.pgsDate?.let { LocalDate.parse(it) } ?: LocalDate.now()
            )

            val payments = security.payments.orEmpty().mapNotNull { dto ->
                val payDateStr = dto.payDate ?: return@mapNotNull null
                val type = when (dto.payType) {
                    "1" -> "coupon"
                    "2" -> "redemption"
                    else -> dto.payType ?: "coupon"
                }
                val payVal = dto.payVal?.let { BigDecimal.valueOf(it) } ?: BigDecimal.ZERO
                BondPaymentEntity(
                    bondIsin = trimmedIsin,
                    payDate = LocalDate.parse(payDateStr),
                    payType = type,
                    payVal = payVal
                )
            }

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
