package com.adnlv.lynd.domain

import com.adnlv.lynd.core.AppConstants
import java.math.BigDecimal
import java.math.RoundingMode

object BondPriceCalculator {

    fun calculatePricePerBond(totalPrice: BigDecimal, quantity: Int): BigDecimal {
        require(quantity > 0) { "Quantity must be greater than zero" }
        return totalPrice.divide(
            BigDecimal.valueOf(quantity.toLong()),
            AppConstants.PRICE_DECIMAL_SCALE,
            RoundingMode.HALF_UP
        )
    }

    fun calculateTotalPrice(pricePerBond: BigDecimal, quantity: Int): BigDecimal {
        require(quantity > 0) { "Quantity must be greater than zero" }
        return pricePerBond.multiply(BigDecimal.valueOf(quantity.toLong()))
            .setScale(AppConstants.PRICE_DECIMAL_SCALE, RoundingMode.HALF_UP)
    }
}
