package com.adnlv.lynd.data.network

import com.adnlv.lynd.core.AppConstants
import com.adnlv.lynd.data.db.BondEntity
import com.adnlv.lynd.data.db.BondPaymentEntity
import java.math.BigDecimal
import java.time.LocalDate

const val NBU_PAY_TYPE_COUPON_CODE = "1"
const val NBU_PAY_TYPE_REDEMPTION_CODE = "2"
const val PAYMENT_TYPE_COUPON = "coupon"
const val PAYMENT_TYPE_REDEMPTION = "redemption"

fun NbuSecurityDto.toBondEntity(isin: String): BondEntity {
    return BondEntity(
        isin = isin,
        name = emitName ?: isin,
        currency = valCode ?: AppConstants.DEFAULT_CURRENCY,
        nominalValue = nominal?.let { BigDecimal.valueOf(it) } ?: AppConstants.DEFAULT_NOMINAL_VALUE,
        couponRate = aukProc?.let { BigDecimal.valueOf(it) } ?: BigDecimal.ZERO,
        maturityDate = pgsDate?.let { LocalDate.parse(it) } ?: LocalDate.now()
    )
}

fun List<NbuPaymentDto>?.toBondPaymentEntities(isin: String): List<BondPaymentEntity> {
    return orEmpty().mapNotNull { dto ->
        val payDateStr = dto.payDate ?: return@mapNotNull null
        val type = when (dto.payType) {
            NBU_PAY_TYPE_COUPON_CODE -> PAYMENT_TYPE_COUPON
            NBU_PAY_TYPE_REDEMPTION_CODE -> PAYMENT_TYPE_REDEMPTION
            else -> dto.payType ?: PAYMENT_TYPE_COUPON
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
