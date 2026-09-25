package com.adnlv.lynd.domain

import com.adnlv.lynd.data.db.BondEntity
import java.math.BigDecimal
import java.time.LocalDate

enum class LadderPaymentType {
    COUPON,
    REDEMPTION,
    COUPON_AND_REDEMPTION
}

data class BondLadderMatch(
    val bond: BondEntity,
    val paymentDate: LocalDate,
    val paymentType: LadderPaymentType,
    val paymentAmount: BigDecimal,
    val totalGapsCovered: Int
)

data class GapMatches(
    val gap: IncomeGap,
    val recommendedBonds: List<BondLadderMatch>
)
