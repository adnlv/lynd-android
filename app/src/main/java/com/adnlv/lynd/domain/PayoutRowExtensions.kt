package com.adnlv.lynd.domain

import com.adnlv.lynd.data.db.PayoutRow
import com.adnlv.lynd.data.network.NBU_PAY_TYPE_COUPON_CODE
import com.adnlv.lynd.data.network.NBU_PAY_TYPE_REDEMPTION_CODE
import com.adnlv.lynd.data.network.PAYMENT_TYPE_COUPON
import com.adnlv.lynd.data.network.PAYMENT_TYPE_REDEMPTION
import java.math.BigDecimal

val PayoutRow.isRedemption: Boolean
    get() = payType.equals(PAYMENT_TYPE_REDEMPTION, ignoreCase = true) || payType == NBU_PAY_TYPE_REDEMPTION_CODE

fun PayoutRow.resolvePayoutType(): PayoutType {
    return if (isRedemption) PayoutType.REDEMPTION else PayoutType.COUPON
}

fun PayoutRow.displayTypeLabel(): String = when (payType.lowercase()) {
    PAYMENT_TYPE_COUPON, NBU_PAY_TYPE_COUPON_CODE -> "Coupon"
    PAYMENT_TYPE_REDEMPTION, NBU_PAY_TYPE_REDEMPTION_CODE -> "Redemption"
    else -> payType.replaceFirstChar { it.uppercase() }
}

fun PayoutRow.calculateTotalAmount(): BigDecimal {
    return payVal.multiply(BigDecimal.valueOf(quantity.toLong()))
}
