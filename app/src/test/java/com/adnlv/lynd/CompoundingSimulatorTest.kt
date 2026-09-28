package com.adnlv.lynd

import com.adnlv.lynd.domain.CompoundingSimulator
import com.adnlv.lynd.domain.MonthlyCashFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.YearMonth

class CompoundingSimulatorTest {

    @Test
    fun calculateSmoothedMonthlyIncome_withCoupons_averagesFirst12Months() {
        val cashFlows = (1..12).map { month ->
            MonthlyCashFlow(
                yearMonth = YearMonth.of(2026, month),
                couponAmount = BigDecimal("1200.00"),
                principalAmount = BigDecimal.ZERO,
                totalAmount = BigDecimal("1200.00")
            )
        }
        val result = CompoundingSimulator.calculateSmoothedMonthlyIncome(
            currencyCashFlows = cashFlows,
            investedCapital = BigDecimal("100000.00"),
            averageRate = BigDecimal("15.00")
        )
        // 1200 * 12 / 12 = 1200.00
        assertEquals(BigDecimal("1200.00"), result)
    }

    @Test
    fun calculateSmoothedMonthlyIncome_withZeroCoupons_fallsBackToInvestedTimesRate() {
        val result = CompoundingSimulator.calculateSmoothedMonthlyIncome(
            currencyCashFlows = emptyList(),
            investedCapital = BigDecimal("100000.00"),
            averageRate = BigDecimal("12.00")
        )
        // 100000 * 12 / 1200 = 1000.00
        assertEquals(BigDecimal("1000.00"), result)
    }

    @Test
    fun calculateSmoothedMonthlyIncome_withZeroCouponsAndZeroCapital_returnsZero() {
        val result = CompoundingSimulator.calculateSmoothedMonthlyIncome(
            currencyCashFlows = emptyList(),
            investedCapital = BigDecimal.ZERO,
            averageRate = BigDecimal.ZERO
        )
        assertEquals(BigDecimal.ZERO, result)
    }

    @Test
    fun simulate_withZeroIncome_returnsZeroProfits() {
        val result = CompoundingSimulator.simulate(
            currency = "UAH",
            investedCapital = BigDecimal("10000.00"),
            monthlyIncome = BigDecimal.ZERO,
            annualRatePercent = BigDecimal("15.00"),
            horizonYears = 3
        )

        assertEquals(3, result.points.size)
        assertEquals(BigDecimal("0.00"), result.finalWithdrawProfit)
        assertEquals(BigDecimal("0.00"), result.finalReinvestProfit)
        assertEquals(BigDecimal("0.00"), result.extraProfit)
        assertEquals(BigDecimal.ZERO, result.percentageGain)
        assertEquals(BigDecimal("10000.00"), result.points.last().withdrawWealth)
        assertEquals(BigDecimal("10000.00"), result.points.last().reinvestWealth)
    }

    @Test
    fun simulate_milestones_andCompoundingAdvantage() {
        val result = CompoundingSimulator.simulate(
            currency = "UAH",
            investedCapital = BigDecimal("100000.00"),
            monthlyIncome = BigDecimal("1000.00"),
            annualRatePercent = BigDecimal("12.00"),
            horizonYears = 5
        )

        assertEquals("UAH", result.currency)
        assertEquals(5, result.points.size)
        assertEquals(5, result.horizonYears)

        // Year 1 (12 months):
        // Withdraw profit = 1000 * 12 = 12000.00
        val p1 = result.points[0]
        assertEquals(1, p1.year)
        assertEquals(BigDecimal("12000.00"), p1.withdrawProfit)
        // With monthly reinvestment, reinvest profit > 12000
        assertTrue(p1.reinvestProfit > p1.withdrawProfit)
        assertTrue(p1.extraProfit > BigDecimal.ZERO)
        assertEquals(p1.reinvestProfit.subtract(p1.withdrawProfit), p1.extraProfit)
        assertEquals(BigDecimal("112000.00"), p1.withdrawWealth)
        assertEquals(BigDecimal("100000.00").add(p1.reinvestProfit), p1.reinvestWealth)

        // Year 5 (60 months):
        val p5 = result.points[4]
        assertEquals(5, p5.year)
        assertEquals(BigDecimal("60000.00"), p5.withdrawProfit)
        assertEquals(result.finalWithdrawProfit, p5.withdrawProfit)
        assertEquals(result.finalReinvestProfit, p5.reinvestProfit)
        assertEquals(result.extraProfit, p5.extraProfit)
        assertTrue(result.percentageGain > BigDecimal.ZERO)
    }

    @Test
    fun simulate_differentHorizons_produceCorrectPointCounts() {
        val r1 = CompoundingSimulator.simulate("USD", BigDecimal.ZERO, BigDecimal("100.00"), BigDecimal("4.00"), 1)
        assertEquals(1, r1.points.size)

        val r3 = CompoundingSimulator.simulate("USD", BigDecimal.ZERO, BigDecimal("100.00"), BigDecimal("4.00"), 3)
        assertEquals(3, r3.points.size)

        val r10 = CompoundingSimulator.simulate("USD", BigDecimal.ZERO, BigDecimal("100.00"), BigDecimal("4.00"), 10)
        assertEquals(10, r10.points.size)
    }
}
