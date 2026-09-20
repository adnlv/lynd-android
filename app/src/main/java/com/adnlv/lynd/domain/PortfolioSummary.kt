package com.adnlv.lynd.domain

import java.math.BigDecimal
import java.math.RoundingMode

data class PortfolioSummary(
    val currency: String,
    val investedCapital: BigDecimal,
    val expectedPayout: BigDecimal,
    val totalProfit: BigDecimal,
    val averageInterestRate: BigDecimal
)

object PortfolioCalculator {
    fun calculateSummary(currency: String, holdings: List<HoldingItem>): PortfolioSummary {
        val currencyHoldings = holdings.filter { it.currency.equals(currency, ignoreCase = true) }
        val invested = currencyHoldings.fold(BigDecimal.ZERO) { acc, item ->
            acc.add(item.totalPaidAmount)
        }
        val expected = currencyHoldings.fold(BigDecimal.ZERO) { acc, item ->
            acc.add(item.totalPayoutAmount)
        }
        val profit = expected.subtract(invested)

        val weightedRateSum = currencyHoldings.fold(BigDecimal.ZERO) { acc, item ->
            acc.add(item.couponRate.multiply(item.totalPaidAmount))
        }

        val avgRate = if (invested > BigDecimal.ZERO) {
            weightedRateSum.divide(invested, 4, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }

        return PortfolioSummary(
            currency = currency,
            investedCapital = invested,
            expectedPayout = expected,
            totalProfit = profit,
            averageInterestRate = avgRate
        )
    }

    fun calculateSummaries(holdings: List<HoldingItem>): List<PortfolioSummary> {
        val currencies = holdings.map { it.currency }.distinct()
        return currencies.map { currency ->
            calculateSummary(currency, holdings)
        }
    }
}
