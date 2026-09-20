package com.adnlv.lynd

import com.adnlv.lynd.util.Formatters
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Locale

class FormattersTest {

    @Test
    fun formatAmount_formatsUkrainianGroupingAndDecimals() {
        val amount = BigDecimal("10000.00")
        val formatted = Formatters.formatAmount(amount)
        assertEquals("10 000,00", formatted)
    }

    @Test
    fun formatAmount_formatsSmallAmount() {
        val amount = BigDecimal("25.50")
        val formatted = Formatters.formatAmount(amount)
        assertEquals("25,50", formatted)
    }

    @Test
    fun formatCouponRate_formatsPercentageWithTwoDecimalPlaces() {
        val rate = BigDecimal("15.5")
        val formatted = Formatters.formatCouponRate(rate)
        assertEquals("15,50%", formatted)

        val integerRate = BigDecimal("10")
        assertEquals("10,00%", Formatters.formatPercentage(integerRate))
    }

    @Test
    fun formatDate_formatsTextualUkDate() {
        val date = LocalDate.of(2026, 9, 15)
        val formatted = Formatters.formatDate(date, Locale.forLanguageTag("uk-UA"))
        assertEquals("15 вер. 2026", formatted)
    }
}
