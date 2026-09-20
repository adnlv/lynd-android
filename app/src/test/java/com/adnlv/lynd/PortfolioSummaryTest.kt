package com.adnlv.lynd

import com.adnlv.lynd.domain.HoldingItem
import com.adnlv.lynd.domain.PortfolioCalculator
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class PortfolioSummaryTest {

    @Test
    fun calculateSummary_emptyHoldings_returnsZeroes() {
        val summary = PortfolioCalculator.calculateSummary("UAH", emptyList())

        assertEquals("UAH", summary.currency)
        assertEquals(BigDecimal.ZERO, summary.investedCapital)
        assertEquals(BigDecimal.ZERO, summary.expectedPayout)
        assertEquals(BigDecimal.ZERO, summary.totalProfit)
        assertEquals(BigDecimal.ZERO, summary.averageInterestRate)
    }

    @Test
    fun calculateSummary_singleHolding_returnsExactMetrics() {
        val holding = HoldingItem(
            id = 1,
            isin = "UA4000187348",
            bondName = "Bond 1",
            quantity = 10,
            pricePerBond = BigDecimal("1000.00"),
            totalPaidAmount = BigDecimal("10000.00"),
            purchaseDate = LocalDate.of(2026, 1, 1),
            currency = "UAH",
            couponRate = BigDecimal("12.50"),
            totalPayoutAmount = BigDecimal("12500.00"),
            totalProfitAmount = BigDecimal("2500.00"),
            profitPercentage = BigDecimal("25.0000")
        )

        val summary = PortfolioCalculator.calculateSummary("UAH", listOf(holding))

        assertEquals(BigDecimal("10000.00"), summary.investedCapital)
        assertEquals(BigDecimal("12500.00"), summary.expectedPayout)
        assertEquals(BigDecimal("2500.00"), summary.totalProfit)
        assertEquals(BigDecimal("12.5000"), summary.averageInterestRate)
    }

    @Test
    fun calculateSummary_multipleHoldings_calculatesCapitalWeightedAverageRate() {
        // Holding 1: 10,000 UAH @ 10.00%
        val holding1 = HoldingItem(
            id = 1,
            isin = "UA1",
            bondName = "Bond 1",
            quantity = 10,
            pricePerBond = BigDecimal("1000.00"),
            totalPaidAmount = BigDecimal("10000.00"),
            purchaseDate = LocalDate.of(2026, 1, 1),
            currency = "UAH",
            couponRate = BigDecimal("10.00"),
            totalPayoutAmount = BigDecimal("11000.00")
        )
        // Holding 2: 30,000 UAH @ 14.00%
        val holding2 = HoldingItem(
            id = 2,
            isin = "UA2",
            bondName = "Bond 2",
            quantity = 30,
            pricePerBond = BigDecimal("1000.00"),
            totalPaidAmount = BigDecimal("30000.00"),
            purchaseDate = LocalDate.of(2026, 1, 1),
            currency = "UAH",
            couponRate = BigDecimal("14.00"),
            totalPayoutAmount = BigDecimal("34200.00")
        )

        // Weighted avg rate: (10,000 * 10 + 30,000 * 14) / 40,000 = (100,000 + 420,000) / 40,000 = 520,000 / 40,000 = 13.00%
        val summary = PortfolioCalculator.calculateSummary("UAH", listOf(holding1, holding2))

        assertEquals(BigDecimal("40000.00"), summary.investedCapital)
        assertEquals(BigDecimal("45200.00"), summary.expectedPayout)
        assertEquals(BigDecimal("5200.00"), summary.totalProfit)
        assertEquals(BigDecimal("13.0000"), summary.averageInterestRate)
    }

    @Test
    fun calculateSummaries_multipleCurrencies_groupsCorrectly() {
        val holdingUah = HoldingItem(
            id = 1,
            isin = "UA1",
            bondName = "UAH Bond",
            quantity = 1,
            pricePerBond = BigDecimal("1000.00"),
            totalPaidAmount = BigDecimal("1000.00"),
            purchaseDate = LocalDate.of(2026, 1, 1),
            currency = "UAH",
            couponRate = BigDecimal("15.00"),
            totalPayoutAmount = BigDecimal("1150.00")
        )
        val holdingUsd = HoldingItem(
            id = 2,
            isin = "US1",
            bondName = "USD Bond",
            quantity = 2,
            pricePerBond = BigDecimal("1000.00"),
            totalPaidAmount = BigDecimal("2000.00"),
            purchaseDate = LocalDate.of(2026, 1, 1),
            currency = "USD",
            couponRate = BigDecimal("4.50"),
            totalPayoutAmount = BigDecimal("2090.00")
        )

        val summaries = PortfolioCalculator.calculateSummaries(listOf(holdingUah, holdingUsd))

        assertEquals(2, summaries.size)
        val uahSummary = summaries.first { it.currency == "UAH" }
        assertEquals(BigDecimal("1000.00"), uahSummary.investedCapital)
        assertEquals(BigDecimal("150.00"), uahSummary.totalProfit)

        val usdSummary = summaries.first { it.currency == "USD" }
        assertEquals(BigDecimal("2000.00"), usdSummary.investedCapital)
        assertEquals(BigDecimal("90.00"), usdSummary.totalProfit)
    }
}
