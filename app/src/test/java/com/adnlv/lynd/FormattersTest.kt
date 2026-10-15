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

    @Test
    fun getCurrencySymbol_returnsKnownSymbols() {
        assertEquals("₴", Formatters.getCurrencySymbol("UAH"))
        assertEquals("₴", Formatters.getCurrencySymbol("uah"))
        assertEquals("$", Formatters.getCurrencySymbol("USD"))
        assertEquals("€", Formatters.getCurrencySymbol("EUR"))
        assertEquals("£", Formatters.getCurrencySymbol("GBP"))
        assertEquals("zł", Formatters.getCurrencySymbol("PLN"))
    }

    @Test
    fun getCurrencySymbol_fallsBackForUnknownCurrency() {
        assertEquals("XYZ", Formatters.getCurrencySymbol("XYZ"))
    }

    @Test
    fun formatPayoutAmount_formatsAndRemovesTrailingZeros() {
        assertEquals("100", Formatters.formatPayoutAmount(BigDecimal("100.00")))
        assertEquals("100.5", Formatters.formatPayoutAmount(BigDecimal("100.50")))
        assertEquals("100.25", Formatters.formatPayoutAmount(BigDecimal("100.25")))
        assertEquals("100", Formatters.formatPayoutAmount(BigDecimal("100")))
        assertEquals("100.56", Formatters.formatPayoutAmount(BigDecimal("100.556")))
        assertEquals("100.55", Formatters.formatPayoutAmount(BigDecimal("100.554")))
        assertEquals("0", Formatters.formatPayoutAmount(BigDecimal("0.00")))
    }
}
