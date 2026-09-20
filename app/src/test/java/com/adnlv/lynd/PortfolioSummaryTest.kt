package com.adnlv.lynd

import com.adnlv.lynd.domain.HoldingItem
import com.adnlv.lynd.domain.PortfolioCalculators
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class PortfolioSummaryTest {

    @Test
    fun calculateSummary_singleHolding_returnsCorrectValues() {
        val holdings = listOf(
            HoldingItem(
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
                profitPercentage = BigDecimal("25.00")
            )
        )

        val summary = PortfolioCalculators.calculateSummary("UAH", holdings)

        assertEquals("UAH", summary.currency)
        assertEquals(BigDecimal("10000.00"), summary.investedCapital)
        assertEquals(BigDecimal("12500.00"), summary.expectedPayout)
        assertEquals(BigDecimal("2500.00"), summary.totalProfit)
        assertEquals(BigDecimal("12.50"), summary.averageInterestRate)
        assertEquals(1, summary.holdingCount)
    }

    @Test
    fun calculateSummary_multipleHoldings_calculatesWeightedAverageInterestRate() {
        // Holding 1: 10,000 UAH @ 10.00%
        // Holding 2: 30,000 UAH @ 20.00%
        // Weighted rate = (10,000 * 10 + 30,000 * 20) / 40,000 = (100,000 + 600,000) / 40,000 = 700,000 / 40,000 = 17.50%
        val holdings = listOf(
            HoldingItem(
                id = 1,
                isin = "UA1",
                bondName = "Bond 1",
                quantity = 10,
                pricePerBond = BigDecimal("1000.00"),
                totalPaidAmount = BigDecimal("10000.00"),
                purchaseDate = LocalDate.of(2026, 1, 1),
                currency = "UAH",
                couponRate = BigDecimal("10.00"),
                totalPayoutAmount = BigDecimal("11000.00"),
                totalProfitAmount = BigDecimal("1000.00"),
                profitPercentage = BigDecimal("10.00")
            ),
            HoldingItem(
                id = 2,
                isin = "UA2",
                bondName = "Bond 2",
                quantity = 30,
                pricePerBond = BigDecimal("1000.00"),
                totalPaidAmount = BigDecimal("30000.00"),
                purchaseDate = LocalDate.of(2026, 1, 1),
                currency = "UAH",
                couponRate = BigDecimal("20.00"),
                totalPayoutAmount = BigDecimal("36000.00"),
                totalProfitAmount = BigDecimal("6000.00"),
                profitPercentage = BigDecimal("20.00")
            )
        )

        val summary = PortfolioCalculators.calculateSummary("UAH", holdings)

        assertEquals(BigDecimal("40000.00"), summary.investedCapital)
        assertEquals(BigDecimal("47000.00"), summary.expectedPayout)
        assertEquals(BigDecimal("7000.00"), summary.totalProfit)
        assertEquals(BigDecimal("17.50"), summary.averageInterestRate)
        assertEquals(2, summary.holdingCount)
    }

    @Test
    fun calculateSummary_emptyHoldings_returnsZeros() {
        val summary = PortfolioCalculators.calculateSummary("UAH", emptyList())

        assertEquals("UAH", summary.currency)
        assertEquals(BigDecimal.ZERO, summary.investedCapital)
        assertEquals(BigDecimal.ZERO, summary.expectedPayout)
        assertEquals(BigDecimal.ZERO, summary.totalProfit)
        assertEquals(BigDecimal.ZERO, summary.averageInterestRate)
        assertEquals(0, summary.holdingCount)
    }

    @Test
    fun calculateSummaries_multipleCurrencies_groupsCorrectly() {
        val holdings = listOf(
            HoldingItem(
                id = 1,
                isin = "UA1",
                bondName = "Bond 1",
                quantity = 10,
                pricePerBond = BigDecimal("1000.00"),
                totalPaidAmount = BigDecimal("10000.00"),
                purchaseDate = LocalDate.of(2026, 1, 1),
                currency = "UAH",
                couponRate = BigDecimal("15.00"),
                totalPayoutAmount = BigDecimal("11500.00"),
                totalProfitAmount = BigDecimal("1500.00"),
                profitPercentage = BigDecimal("15.00")
            ),
            HoldingItem(
                id = 2,
                isin = "US1",
                bondName = "USD Bond",
                quantity = 5,
                pricePerBond = BigDecimal("1000.00"),
                totalPaidAmount = BigDecimal("5000.00"),
                purchaseDate = LocalDate.of(2026, 1, 1),
                currency = "USD",
                couponRate = BigDecimal("4.50"),
                totalPayoutAmount = BigDecimal("5500.00"),
                totalProfitAmount = BigDecimal("500.00"),
                profitPercentage = BigDecimal("10.00")
            )
        )

        val summaries = PortfolioCalculators.calculateSummaries(holdings)

        assertEquals(2, summaries.size)
        val uahSummary = summaries.first { it.currency == "UAH" }
        val usdSummary = summaries.first { it.currency == "USD" }

        assertEquals(BigDecimal("10000.00"), uahSummary.investedCapital)
        assertEquals(BigDecimal("15.00"), uahSummary.averageInterestRate)

        assertEquals(BigDecimal("5000.00"), usdSummary.investedCapital)
        assertEquals(BigDecimal("4.50"), usdSummary.averageInterestRate)
    }
}
