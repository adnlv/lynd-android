package com.adnlv.lynd

import com.adnlv.lynd.domain.BondPriceCalculator
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class BondPriceCalculatorTest {

    @Test
    fun calculatePricePerBond_fromTotalPriceAndQuantity() {
        val totalPrice = BigDecimal("3000.00")
        val quantity = 3
        val result = BondPriceCalculator.calculatePricePerBond(totalPrice, quantity)
        assertEquals(BigDecimal("1000.00"), result)
    }

    @Test
    fun calculateTotalPrice_fromPricePerBondAndQuantity() {
        val pricePerBond = BigDecimal("1025.50")
        val quantity = 3
        val result = BondPriceCalculator.calculateTotalPrice(pricePerBond, quantity)
        assertEquals(BigDecimal("3076.50"), result)
    }

    @Test
    fun calculatePricePerBond_roundsHalfUp() {
        val totalPrice = BigDecimal("100.00")
        val quantity = 3
        val result = BondPriceCalculator.calculatePricePerBond(totalPrice, quantity)
        assertEquals(BigDecimal("33.33"), result)

        val totalPriceHalfUp = BigDecimal("10.00")
        val quantityHalfUp = 6
        // 10 / 6 = 1.6666... -> 1.67
        val resultHalfUp = BondPriceCalculator.calculatePricePerBond(totalPriceHalfUp, quantityHalfUp)
        assertEquals(BigDecimal("1.67"), resultHalfUp)
    }

    @Test
    fun calculateTotalPrice_roundsHalfUp() {
        val pricePerBond = BigDecimal("10.555")
        val quantity = 1
        val result = BondPriceCalculator.calculateTotalPrice(pricePerBond, quantity)
        assertEquals(BigDecimal("10.56"), result)
    }

    @Test(expected = IllegalArgumentException::class)
    fun calculatePricePerBond_throwsWhenQuantityIsZero() {
        BondPriceCalculator.calculatePricePerBond(BigDecimal("100.00"), 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun calculatePricePerBond_throwsWhenQuantityIsNegative() {
        BondPriceCalculator.calculatePricePerBond(BigDecimal("100.00"), -1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun calculateTotalPrice_throwsWhenQuantityIsZero() {
        BondPriceCalculator.calculateTotalPrice(BigDecimal("100.00"), 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun calculateTotalPrice_throwsWhenQuantityIsNegative() {
        BondPriceCalculator.calculateTotalPrice(BigDecimal("100.00"), -1)
    }
}
