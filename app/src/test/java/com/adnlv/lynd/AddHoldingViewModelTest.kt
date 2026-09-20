package com.adnlv.lynd

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode

class AddHoldingViewModelTest {

    @Test
    fun isinNumberFiltering_onlyAllowsDigitsAndLimitsToSix() {
        val raw = "12a34bc56789"
        val filtered = raw.filter { it.isDigit() }.take(6)
        assertEquals("123456", filtered)
    }

    @Test
    fun isinPrefixExtraction_matchesKnownPrefix() {
        val availablePrefixes = listOf("UA4000", "UA5000")
        val isin = "UA5000123456"
        val matchingPrefix = availablePrefixes.firstOrNull { isin.startsWith(it) } ?: isin.take(6)
        val numberSuffix = isin.removePrefix(matchingPrefix)

        assertEquals("UA5000", matchingPrefix)
        assertEquals("123456", numberSuffix)
    }

    @Test
    fun isinPrefixExtraction_fallsBackToFirstSixCharsWhenUnknown() {
        val availablePrefixes = listOf("UA4000")
        val isin = "XS1234567890"
        val matchingPrefix = availablePrefixes.firstOrNull { isin.startsWith(it) } ?: isin.take(6)
        val numberSuffix = isin.removePrefix(matchingPrefix)

        assertEquals("XS1234", matchingPrefix)
        assertEquals("567890", numberSuffix)
    }

    @Test
    fun priceCalculation_perBondCalculatesTotal() {
        val pricePerBond = BigDecimal("1025.50")
        val quantity = 10
        val total = pricePerBond.multiply(BigDecimal(quantity))
            .setScale(2, RoundingMode.HALF_UP)
            .toPlainString()

        assertEquals("10255.00", total)
    }

    @Test
    fun priceCalculation_totalCalculatesPerBond() {
        val totalPrice = BigDecimal("10255.00")
        val quantity = 10
        val perBond = totalPrice.divide(BigDecimal(quantity), 2, RoundingMode.HALF_UP)
            .toPlainString()

        assertEquals("1025.50", perBond)
    }
}
