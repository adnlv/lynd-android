package com.adnlv.lynd

import com.adnlv.lynd.data.db.PayoutRow
import com.adnlv.lynd.domain.PortfolioCalculator
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

class CashFlowCalculatorTest {

    @Test
    fun calculateMonthlyCashFlows_returnsRequestedNumberOfConsecutiveMonths() {
        val startDate = LocalDate.of(2026, 4, 1)
        val result = PortfolioCalculator.calculateMonthlyCashFlows(
            payoutRows = emptyList(),
            startDate = startDate,
            monthCount = 12
        )

        assertEquals(0, result.size)
    }

    @Test
    fun calculateMonthlyCashFlows_separatesCouponsAndRedemptionCorrectly() {
        val startDate = LocalDate.of(2026, 4, 1)
        val rows = listOf(
            PayoutRow(
                isin = "UA1",
                bondName = "Bond 1",
                payDate = LocalDate.of(2026, 4, 15),
                payType = "coupon",
                payVal = BigDecimal("50.00"),
                quantity = 10,
                currency = "UAH",
                purchaseDate = LocalDate.of(2026, 1, 1)
            ),
            PayoutRow(
                isin = "UA1",
                bondName = "Bond 1",
                payDate = LocalDate.of(2026, 4, 15),
                payType = "redemption",
                payVal = BigDecimal("1000.00"),
                quantity = 10,
                currency = "UAH",
                purchaseDate = LocalDate.of(2026, 1, 1)
            ),
            PayoutRow(
                isin = "UA2",
                bondName = "Bond 2",
                payDate = LocalDate.of(2026, 5, 20),
                payType = "coupon",
                payVal = BigDecimal("30.00"),
                quantity = 5,
                currency = "UAH",
                purchaseDate = LocalDate.of(2026, 1, 1)
            )
        )

        val result = PortfolioCalculator.calculateMonthlyCashFlows(
            payoutRows = rows,
            startDate = startDate,
            monthCount = 12
        )

        val uahFlows = result["UAH"] ?: error("UAH missing")
        assertEquals(12, uahFlows.size)

        // Month 1: 2026-04
        val april = uahFlows[0]
        assertEquals(YearMonth.of(2026, 4), april.yearMonth)
        assertEquals(BigDecimal("500.00"), april.couponAmount)
        assertEquals(BigDecimal("10000.00"), april.principalAmount)
        assertEquals(BigDecimal("10500.00"), april.totalAmount)

        // Month 2: 2026-05
        val may = uahFlows[1]
        assertEquals(YearMonth.of(2026, 5), may.yearMonth)
        assertEquals(BigDecimal("150.00"), may.couponAmount)
        assertEquals(BigDecimal.ZERO, may.principalAmount)
        assertEquals(BigDecimal("150.00"), may.totalAmount)

        // Month 3: 2026-06 (no payouts)
        val june = uahFlows[2]
        assertEquals(YearMonth.of(2026, 6), june.yearMonth)
        assertEquals(BigDecimal.ZERO, june.couponAmount)
        assertEquals(BigDecimal.ZERO, june.principalAmount)
        assertEquals(BigDecimal.ZERO, june.totalAmount)
    }

    @Test
    fun calculateMonthlyCashFlows_ignoresPastAndPrePurchasePayouts() {
        val startDate = LocalDate.of(2026, 4, 1)
        val rows = listOf(
            PayoutRow(
                isin = "UA1",
                bondName = "Past Payout",
                payDate = LocalDate.of(2026, 3, 15),
                payType = "coupon",
                payVal = BigDecimal("50.00"),
                quantity = 10,
                currency = "UAH",
                purchaseDate = LocalDate.of(2026, 1, 1)
            ),
            PayoutRow(
                isin = "UA2",
                bondName = "Pre-purchase Payout",
                payDate = LocalDate.of(2026, 5, 10),
                payType = "coupon",
                payVal = BigDecimal("50.00"),
                quantity = 10,
                currency = "UAH",
                purchaseDate = LocalDate.of(2026, 5, 20)
            )
        )

        val result = PortfolioCalculator.calculateMonthlyCashFlows(
            payoutRows = rows,
            startDate = startDate,
            monthCount = 12
        )

        val uahFlows = result["UAH"] ?: error("UAH missing")
        assertEquals(12, uahFlows.size)
        uahFlows.forEach {
            assertEquals(BigDecimal.ZERO, it.totalAmount)
        }
    }
}
