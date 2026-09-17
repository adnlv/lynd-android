package com.adnlv.lynd.data.network

import com.adnlv.lynd.data.db.BondDao
import com.adnlv.lynd.data.db.BondEntity
import com.adnlv.lynd.data.db.BondPaymentEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.time.LocalDate

class NbuRepository(
    private val apiService: NbuApiService,
    private val bondDao: BondDao
) {
    suspend fun getOrFetchBond(isin: String): Result<BondEntity> = withContext(Dispatchers.IO) {
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
                bondDao.upsertPayments(payments)
            }

            Result.success(bondEntity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
