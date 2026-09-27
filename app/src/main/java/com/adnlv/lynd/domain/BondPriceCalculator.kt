package com.adnlv.lynd.domain

import java.math.BigDecimal
import java.math.RoundingMode

object BondPriceCalculator {

    fun calculatePricePerBond(totalPrice: BigDecimal, quantity: Int): BigDecimal {
        require(quantity > 0) { "Quantity must be greater than zero" }
        return totalPrice.divide(BigDecimal(quantity), 2, RoundingMode.HALF_UP)
    }

    fun calculateTotalPrice(pricePerBond: BigDecimal, quantity: Int): BigDecimal {
        require(quantity > 0) { "Quantity must be greater than zero" }
        return pricePerBond.multiply(BigDecimal(quantity)).setScale(2, RoundingMode.HALF_UP)
    }
}
