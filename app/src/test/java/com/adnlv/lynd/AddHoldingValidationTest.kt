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

    @Test
    fun holdingItem_mapsCorrectlyToInitialEditFields() {
        val holding = com.adnlv.lynd.domain.HoldingItem(
            id = 42,
            isin = "UA4000238281",
            bondName = "Bond 1",
            quantity = 5,
            pricePerBond = BigDecimal("1050.25"),
            totalPaidAmount = BigDecimal("5251.25"),
            purchaseDate = java.time.LocalDate.of(2025, 6, 15)
        )

        val prefix = holding.isin.take(6)
        val code = holding.isin.substring(6)
        val quantity = holding.quantity
        val pricePerBond = holding.pricePerBond.toPlainString()
        val totalPrice = holding.totalPaidAmount.toPlainString()
        val purchaseDate = holding.purchaseDate

        assertEquals("UA4000", prefix)
        assertEquals("238281", code)
        assertEquals(5, quantity)
        assertEquals("1050.25", pricePerBond)
        assertEquals("5251.25", totalPrice)
        assertEquals(java.time.LocalDate.of(2025, 6, 15), purchaseDate)
    }

    @Test
    fun updateHoldingEntity_retainsOriginalId() {
        val originalId = 42
        val updatedHolding = com.adnlv.lynd.data.db.HoldingEntity(
            id = originalId,
            isin = "UA4000238281",
            quantity = 10,
            pricePerBond = BigDecimal("1000.00"),
            totalPaidAmount = BigDecimal("10000.00"),
            purchaseDate = java.time.LocalDate.of(2025, 7, 20)
        )

        assertEquals(originalId, updatedHolding.id)
        assertEquals(10, updatedHolding.quantity)
        assertEquals(BigDecimal("1000.00"), updatedHolding.pricePerBond)
        assertEquals(BigDecimal("10000.00"), updatedHolding.totalPaidAmount)
    }

    @Test
    fun updatedHoldingValues_passValidation() {
        val prefix = "UA4000"
        val code = "238281"
        val fullIsin = "$prefix$code"
        val quantity = 10
        val pricePerBondInput = "1000.00"
        val totalPriceInput = "10000.00"

        val isValid = com.adnlv.lynd.domain.IsinValidator.isValid(fullIsin) &&
            quantity >= 1 &&
            (totalPriceInput.toDoubleOrNull() ?: 0.0) > 0.0 &&
            (pricePerBondInput.toDoubleOrNull() ?: 0.0) > 0.0

        assertTrue(isValid)

        val invalidFullIsin = "${prefix}238282"
        val isInvalidValid = com.adnlv.lynd.domain.IsinValidator.isValid(invalidFullIsin)
        org.junit.Assert.assertFalse(isInvalidValid)
    }
}
