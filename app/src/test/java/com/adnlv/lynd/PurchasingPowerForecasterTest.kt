package com.adnlv.lynd

import com.adnlv.lynd.domain.CompoundingSimulator
import com.adnlv.lynd.domain.NbuInflationData
import com.adnlv.lynd.domain.PurchasingPowerForecaster
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class PurchasingPowerForecasterTest {

    @Test
    fun forecast_deflatorCalculations_acrossHorizons() {
        val rates = NbuInflationData.getRatesForCurrency("UAH", 5)
        assertEquals(5, rates.size)
        assertEquals(BigDecimal("8.1"), rates[0])
        assertEquals(BigDecimal("6.5"), rates[1])
        assertEquals(BigDecimal("5.0"), rates[2])

        val sim = CompoundingSimulator.simulate(
            currency = "UAH",
            investedCapital = BigDecimal("100000.00"),
            monthlyIncome = BigDecimal("1000.00"),
            annualRatePercent = BigDecimal("15.00"),
            horizonYears = 5
        )

        val result = PurchasingPowerForecaster.forecast(sim, rates)

        assertEquals(5, result.points.size)
        assertEquals("UAH", result.currency)
        assertEquals(5, result.horizonYears)

        // Year 1 cumulative inflation: (1 + 0.081) - 1 = 8.10%
        val p1 = result.points[0]
        assertEquals(1, p1.year)
        assertEquals(BigDecimal("8.10"), p1.cumulativeInflationPercent)

        // Year 2 cumulative inflation: (1.081 * 1.065) - 1 = 1.151265 - 1 = 15.13%
        val p2 = result.points[1]
        assertEquals(2, p2.year)
        assertEquals(BigDecimal("15.13"), p2.cumulativeInflationPercent)

        // Year 3 cumulative inflation: 1.151265 * 1.05 = 1.20882825 - 1 = 20.88%
        val p3 = result.points[2]
        assertEquals(3, p3.year)
        assertEquals(BigDecimal("20.88"), p3.cumulativeInflationPercent)
    }

    @Test
    fun forecast_realMonthlyPayout_deflatedAgainstInflation() {
        val sim = CompoundingSimulator.simulate(
            currency = "UAH",
            investedCapital = BigDecimal("100000.00"),
            monthlyIncome = BigDecimal("1081.00"),
            annualRatePercent = BigDecimal("15.00"),
            horizonYears = 1
        )
        // Rate is 8.1%, deflator = 1.081. Real payout = 1081 / 1.081 = 1000.00
        val rates = listOf(BigDecimal("8.1"))
        val result = PurchasingPowerForecaster.forecast(sim, rates)

        val p1 = result.points[0]
        assertEquals(BigDecimal("1081.00"), p1.nominalMonthlyPayout)
        assertEquals(BigDecimal("1000.00"), p1.realMonthlyPayout)
        assertEquals(BigDecimal("1000.00"), result.finalRealMonthlyPayout)
    }

    @Test
    fun forecast_realGrowth_whenCouponRateExceedsInflation() {
        val sim = CompoundingSimulator.simulate(
            currency = "UAH",
            investedCapital = BigDecimal("100000.00"),
            monthlyIncome = BigDecimal("1250.00"), // 15% annual on 100k
            annualRatePercent = BigDecimal("15.00"),
            horizonYears = 3
        )
        val rates = listOf(BigDecimal("8.1"), BigDecimal("6.5"), BigDecimal("5.0"))
        val result = PurchasingPowerForecaster.forecast(sim, rates)

        // Real wealth should exceed initial capital
        assertTrue(result.realFinalWealth > result.investedCapital)
        assertTrue(result.realWealthGrowthPercent > BigDecimal.ZERO)
        assertTrue(result.beatsInflation)

        for (point in result.points) {
            assertTrue(point.beatsInflation)
            assertTrue(point.realWealth > BigDecimal("100000.00"))
            assertTrue(point.nominalWealth > point.realWealth)
            assertTrue(point.purchasingPowerLoss > BigDecimal.ZERO)
        }
    }

    @Test
    fun forecast_capitalErosion_whenInflationExceedsCouponRate() {
        // High inflation (25%), low coupon rate (5%)
        val sim = CompoundingSimulator.simulate(
            currency = "UAH",
            investedCapital = BigDecimal("100000.00"),
            monthlyIncome = BigDecimal("416.67"), // 5% annual
            annualRatePercent = BigDecimal("5.00"),
            horizonYears = 3
        )
        val rates = listOf(BigDecimal("25.0"), BigDecimal("25.0"), BigDecimal("25.0"))
        val result = PurchasingPowerForecaster.forecast(sim, rates)

        // In today's money, wealth has eroded below 100,000
        assertTrue(result.realFinalWealth < result.investedCapital)
        assertTrue(result.realWealthGrowthPercent < BigDecimal.ZERO)
        assertFalse(result.beatsInflation)
    }

    @Test
    fun forecast_zeroCapitalAndIncome_returnsZeroValuesSafely() {
        val sim = CompoundingSimulator.simulate(
            currency = "USD",
            investedCapital = BigDecimal.ZERO,
            monthlyIncome = BigDecimal.ZERO,
            annualRatePercent = BigDecimal("4.00"),
            horizonYears = 3
        )
        val rates = listOf(BigDecimal("2.2"), BigDecimal("2.2"), BigDecimal("2.2"))
        val result = PurchasingPowerForecaster.forecast(sim, rates)

        assertTrue(result.nominalMonthlyPayout.compareTo(BigDecimal.ZERO) == 0)
        assertTrue(result.finalRealMonthlyPayout.compareTo(BigDecimal.ZERO) == 0)
        assertTrue(result.nominalFinalWealth.compareTo(BigDecimal.ZERO) == 0)
        assertTrue(result.realFinalWealth.compareTo(BigDecimal.ZERO) == 0)
        assertTrue(result.realWealthGrowthPercent.compareTo(BigDecimal.ZERO) == 0)
        assertTrue(result.beatsInflation)
    }

    @Test
    fun forecast_pointCounts_matchHorizons() {
        val horizons = listOf(1, 3, 5, 10)
        for (h in horizons) {
            val rates = NbuInflationData.getRatesForCurrency("EUR", h)
            val sim = CompoundingSimulator.simulate(
                currency = "EUR",
                investedCapital = BigDecimal("50000.00"),
                monthlyIncome = BigDecimal("150.00"),
                annualRatePercent = BigDecimal("3.20"),
                horizonYears = h
            )
            val result = PurchasingPowerForecaster.forecast(sim, rates)
            assertEquals(h, result.points.size)
            assertEquals(h, result.horizonYears)
        }
    }
}
