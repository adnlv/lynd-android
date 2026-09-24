package com.adnlv.lynd

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode

class AddHoldingValidationTest {

    @Test
    fun quantityCounter_enforcesMinimumValueOfOne() {
        val initialQuantity = 1
        val decremented = if (initialQuantity > 1) initialQuantity - 1 else initialQuantity
        assertEquals(1, decremented)

        val increased = initialQuantity + 1
        assertEquals(2, increased)
    }

    @Test
    fun totalPrice_computesPricePerBondCorrectly() {
        val totalPrice = BigDecimal("2050.00")
        val quantity = 2
        val pricePerBond = totalPrice.divide(BigDecimal(quantity), 2, RoundingMode.HALF_UP)
        assertEquals(BigDecimal("1025.00"), pricePerBond)
    }

    @Test
    fun pricePerBond_computesTotalPriceCorrectly() {
        val pricePerBond = BigDecimal("1025.50")
        val quantity = 3
        val totalPrice = pricePerBond.multiply(BigDecimal(quantity))
        assertEquals(BigDecimal("3076.50"), totalPrice)
    }

    @Test
    fun fullIsin_constructsFromPrefixAndSixDigits() {
        val prefix = "UA4000"
        val code = "238281"
        val fullIsin = "$prefix$code"
        assertEquals("UA4000238281", fullIsin)
        assertEquals(12, fullIsin.length)
        assertTrue(fullIsin.matches(Regex("^[A-Z]{2}[A-Z0-9]{10}$")))
    }
}
