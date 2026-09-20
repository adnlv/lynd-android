package com.adnlv.lynd

import com.adnlv.lynd.data.db.PayoutRow
import com.adnlv.lynd.domain.PortfolioCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class MaturityScheduleCalculatorTest {

    @Test
    fun calculateYearlyMaturitySchedule_returnsEmptyMapWhenNoRows() {
        val startDate = LocalDate.of(2026, 1, 1)
        val result = PortfolioCalculator.calculateYearlyMaturitySchedule(
            payoutRows = emptyList(),
            startDate = startDate
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun calculateYearlyMaturitySchedule_returnsEmptyListWhenNoUpcomingRedemptions() {
        val startDate = LocalDate.of(2026, 1, 1)
        val rows = listOf(
            PayoutRow(
                isin = "UA1",
                bondName = "Bond 1",
                payDate = LocalDate.of(2026, 4, 15),
                payType = "coupon",
                payVal = BigDecimal("50.00"),
                quantity = 10,
                currency = "UAH",
                purchaseDate = LocalDate.of(2025, 12, 1)
            )
        )

        val result = PortfolioCalculator.calculateYearlyMaturitySchedule(
            payoutRows = rows,
            startDate = startDate
        )

        val uahSchedule = result["UAH"] ?: error("UAH missing")
        assertTrue(uahSchedule.isEmpty())
    }

    @Test
    fun calculateYearlyMaturitySchedule_generatesConsecutiveYearsUpToMaxMaturity() {
        val startDate = LocalDate.of(2026, 3, 1)
        val rows = listOf(
            PayoutRow(
                isin = "UA1",
                bondName = "Bond 1",
                payDate = LocalDate.of(2026, 10, 14),
                payType = "redemption",
                payVal = BigDecimal("1000.00"),
                quantity = 5,
                currency = "UAH",
                purchaseDate = LocalDate.of(2026, 1, 1)
            ),
            PayoutRow(
                isin = "UA2",
                bondName = "Bond 2",
                payDate = LocalDate.of(2026, 12, 16),
                payType = "redemption",
                payVal = BigDecimal("1000.00"),
                quantity = 25,
                currency = "UAH",
                purchaseDate = LocalDate.of(2026, 1, 1)
            ),
            PayoutRow(
                isin = "UA3",
                bondName = "Bond 3",
                payDate = LocalDate.of(2029, 10, 12),
                payType = "redemption",
                payVal = BigDecimal("1000.00"),
                quantity = 30,
                currency = "UAH",
                purchaseDate = LocalDate.of(2026, 1, 1)
            )
        )

        val result = PortfolioCalculator.calculateYearlyMaturitySchedule(
            payoutRows = rows,
            startDate = startDate
        )

        val uahSchedule = result["UAH"] ?: error("UAH missing")
        assertEquals(4, uahSchedule.size)

        // 2026
        assertEquals(2026, uahSchedule[0].year)
        assertEquals(BigDecimal("30000.00"), uahSchedule[0].amount)

        // 2027 (gap year)
        assertEquals(2027, uahSchedule[1].year)
        assertEquals(BigDecimal.ZERO, uahSchedule[1].amount)

        // 2028 (gap year)
        assertEquals(2028, uahSchedule[2].year)
        assertEquals(BigDecimal.ZERO, uahSchedule[2].amount)

        // 2029
        assertEquals(2029, uahSchedule[3].year)
        assertEquals(BigDecimal("30000.00"), uahSchedule[3].amount)
    }

    @Test
    fun calculateYearlyMaturitySchedule_ignoresPastAndPrePurchaseRedemptions() {
        val startDate = LocalDate.of(2026, 4, 1)
        val rows = listOf(
            PayoutRow(
                isin = "UA1",
                bondName = "Past Redemption",
                payDate = LocalDate.of(2026, 2, 1),
                payType = "redemption",
                payVal = BigDecimal("1000.00"),
                quantity = 10,
                currency = "UAH",
                purchaseDate = LocalDate.of(2025, 1, 1)
            ),
            PayoutRow(
                isin = "UA2",
                bondName = "Pre-purchase Redemption",
                payDate = LocalDate.of(2026, 5, 1),
                payType = "redemption",
                payVal = BigDecimal("1000.00"),
                quantity = 10,
                currency = "UAH",
                purchaseDate = LocalDate.of(2026, 6, 1)
            ),
            PayoutRow(
                isin = "UA3",
                bondName = "Future Valid Redemption",
                payDate = LocalDate.of(2027, 2, 10),
                payType = "2", // numerical code for redemption
                payVal = BigDecimal("1000.00"),
                quantity = 7,
                currency = "UAH",
                purchaseDate = LocalDate.of(2026, 1, 1)
            )
        )

        val result = PortfolioCalculator.calculateYearlyMaturitySchedule(
            payoutRows = rows,
            startDate = startDate
        )

        val uahSchedule = result["UAH"] ?: error("UAH missing")
        assertEquals(2, uahSchedule.size)

        // 2026: zero upcoming
        assertEquals(2026, uahSchedule[0].year)
        assertEquals(BigDecimal.ZERO, uahSchedule[0].amount)

        // 2027: 7 * 1000 = 7000
        assertEquals(2027, uahSchedule[1].year)
        assertEquals(BigDecimal("7000.00"), uahSchedule[1].amount)
    }

    @Test
    fun calculateYearlyMaturitySchedule_separatesByCurrency() {
        val startDate = LocalDate.of(2026, 1, 1)
        val rows = listOf(
            PayoutRow(
                isin = "UA1",
                bondName = "UAH Bond",
                payDate = LocalDate.of(2026, 6, 1),
                payType = "redemption",
                payVal = BigDecimal("1000.00"),
                quantity = 10,
                currency = "UAH",
                purchaseDate = LocalDate.of(2026, 1, 1)
            ),
            PayoutRow(
                isin = "US1",
                bondName = "USD Bond",
                payDate = LocalDate.of(2027, 3, 1),
                payType = "redemption",
                payVal = BigDecimal("1000.00"),
                quantity = 5,
                currency = "USD",
                purchaseDate = LocalDate.of(2026, 1, 1)
            )
        )

        val result = PortfolioCalculator.calculateYearlyMaturitySchedule(
            payoutRows = rows,
            startDate = startDate
        )

        val uahSchedule = result["UAH"] ?: error("UAH missing")
        val usdSchedule = result["USD"] ?: error("USD missing")

        assertEquals(1, uahSchedule.size)
        assertEquals(2026, uahSchedule[0].year)
        assertEquals(BigDecimal("10000.00"), uahSchedule[0].amount)

        assertEquals(2, usdSchedule.size)
        assertEquals(2026, usdSchedule[0].year)
        assertEquals(BigDecimal.ZERO, usdSchedule[0].amount)
        assertEquals(2027, usdSchedule[1].year)
        assertEquals(BigDecimal("5000.00"), usdSchedule[1].amount)
    }
}
