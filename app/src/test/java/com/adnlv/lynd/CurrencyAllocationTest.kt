package com.adnlv.lynd

import com.adnlv.lynd.domain.PortfolioCalculator
import com.adnlv.lynd.domain.PortfolioSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class CurrencyAllocationTest {

    @Test
    fun calculateCurrencyAllocations_emptySummaries_returnsEmptyList() {
        val allocations = PortfolioCalculator.calculateCurrencyAllocations(emptyList())
        assertTrue(allocations.isEmpty())
    }

    @Test
    fun calculateCurrencyAllocations_singleCurrency_returns100Percent() {
        val summaries = listOf(
            PortfolioSummary(
                currency = "UAH",
                investedCapital = BigDecimal("10000.00"),
                expectedPayout = BigDecimal("12000.00"),
                totalProfit = BigDecimal("2000.00"),
                averageInterestRate = BigDecimal("15.00")
            )
        )

        val allocations = PortfolioCalculator.calculateCurrencyAllocations(summaries)
        assertEquals(1, allocations.size)
        assertEquals("UAH", allocations[0].currency)
        assertEquals("Ukrainian Hryvnia", allocations[0].currencyName)
        assertEquals(BigDecimal("10000.00"), allocations[0].amount)
        assertEquals(BigDecimal("100.00"), allocations[0].percentage)
    }

    @Test
    fun calculateCurrencyAllocations_multipleCurrencies_calculatesCorrectShares() {
        val summaries = listOf(
            PortfolioSummary(
                currency = "USD",
                investedCapital = BigDecimal("25000.00"),
                expectedPayout = BigDecimal("27000.00"),
                totalProfit = BigDecimal("2000.00"),
                averageInterestRate = BigDecimal("4.00")
            ),
            PortfolioSummary(
                currency = "EUR",
                investedCapital = BigDecimal("25000.00"),
                expectedPayout = BigDecimal("26500.00"),
                totalProfit = BigDecimal("1500.00"),
                averageInterestRate = BigDecimal("3.00")
            ),
            PortfolioSummary(
                currency = "UAH",
                investedCapital = BigDecimal("50000.00"),
                expectedPayout = BigDecimal("60000.00"),
                totalProfit = BigDecimal("10000.00"),
                averageInterestRate = BigDecimal("16.00")
            )
        )

        // Total nominal = 50,000 + 25,000 + 25,000 = 100,000
        // UAH = 50.00%, USD = 25.00%, EUR = 25.00%
        val allocations = PortfolioCalculator.calculateCurrencyAllocations(summaries)

        assertEquals(3, allocations.size)

        // Verifies order: UAH, USD, EUR
        assertEquals("UAH", allocations[0].currency)
        assertEquals("Ukrainian Hryvnia", allocations[0].currencyName)
        assertEquals(BigDecimal("50000.00"), allocations[0].amount)
        assertEquals(BigDecimal("50.00"), allocations[0].percentage)

        assertEquals("USD", allocations[1].currency)
        assertEquals("US Dollar", allocations[1].currencyName)
        assertEquals(BigDecimal("25000.00"), allocations[1].amount)
        assertEquals(BigDecimal("25.00"), allocations[1].percentage)

        assertEquals("EUR", allocations[2].currency)
        assertEquals("Euro", allocations[2].currencyName)
        assertEquals(BigDecimal("25000.00"), allocations[2].amount)
        assertEquals(BigDecimal("25.00"), allocations[2].percentage)
    }

    @Test
    fun calculateCurrencyAllocations_zeroCapital_returnsZeroPercentage() {
        val summaries = listOf(
            PortfolioSummary(
                currency = "UAH",
                investedCapital = BigDecimal.ZERO,
                expectedPayout = BigDecimal.ZERO,
                totalProfit = BigDecimal.ZERO,
                averageInterestRate = BigDecimal.ZERO
            )
        )

        val allocations = PortfolioCalculator.calculateCurrencyAllocations(summaries)
        assertEquals(1, allocations.size)
        assertEquals(BigDecimal.ZERO, allocations[0].percentage)
    }
}
