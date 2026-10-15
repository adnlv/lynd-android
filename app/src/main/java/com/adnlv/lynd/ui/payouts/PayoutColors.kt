package com.adnlv.lynd.ui.payouts

import androidx.compose.ui.graphics.Color

object PayoutColors {
    val CouponLight = Color(0xFF137333)
    val CouponDark = Color(0xFF81C784)

    val RedemptionLight = Color(0xFF4A148C)
    val RedemptionDark = Color(0xFFCE93D8)

    val TotalAmountLight = Color(0xFF0D47A1)
    val TotalAmountDark = Color(0xFF90CAF9)

    fun couponLabel(isDark: Boolean): Color = if (isDark) CouponDark else CouponLight
    fun couponAmount(isDark: Boolean): Color = if (isDark) CouponDark else CouponLight
    fun redemptionLabel(isDark: Boolean): Color = if (isDark) RedemptionDark else RedemptionLight
    fun redemptionAmount(isDark: Boolean): Color = if (isDark) RedemptionDark else RedemptionLight
    fun totalAmount(isDark: Boolean): Color = if (isDark) TotalAmountDark else TotalAmountLight
}
