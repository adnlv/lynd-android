package com.adnlv.lynd.domain

import java.math.BigDecimal
import java.math.RoundingMode

data class PurchasingPowerPoint(
    val year: Int,
    val cumulativeInflationPercent: BigDecimal,
    val nominalMonthlyPayout: BigDecimal,
    val realMonthlyPayout: BigDecimal,
    val nominalWealth: BigDecimal,
    val realWealth: BigDecimal,
    val purchasingPowerLoss: BigDecimal,
    val realGrowthPercent: BigDecimal,
    val beatsInflation: Boolean
)

data class PurchasingPowerForecastResult(
    val currency: String,
    val investedCapital: BigDecimal,
    val horizonYears: Int,
    val nominalMonthlyPayout: BigDecimal,
    val finalRealMonthlyPayout: BigDecimal,
    val nominalFinalWealth: BigDecimal,
    val realFinalWealth: BigDecimal,
    val realWealthGrowthPercent: BigDecimal,
    val averageInflationRatePercent: BigDecimal,
    val beatsInflation: Boolean,
    val points: List<PurchasingPowerPoint>
)

object PurchasingPowerForecaster {

    fun forecast(
        compoundingResult: CompoundingSimulationResult,
        inflationRates: List<BigDecimal>
    ): PurchasingPowerForecastResult {
        val horizonYears = compoundingResult.horizonYears
        val investedCapital = compoundingResult.investedCapital
        val nominalMonthly = compoundingResult.smoothedMonthlyIncome

        val points = mutableListOf<PurchasingPowerPoint>()
        var deflator = BigDecimal.ONE // D_t = product(1 + rate_k / 100)
        var totalInflationSum = BigDecimal.ZERO

        for (year in 1..horizonYears) {
            val annualRatePercent = inflationRates.getOrElse(year - 1) { BigDecimal.ZERO }
            totalInflationSum = totalInflationSum.add(annualRatePercent)

            // Rate as decimal: e.g. 8.1% -> 0.081
            val annualRateDecimal = annualRatePercent.divide(BigDecimal(100), 8, RoundingMode.HALF_UP)
            deflator = deflator.multiply(BigDecimal.ONE.add(annualRateDecimal))

            // Cumulative inflation % = (deflator - 1) * 100
            val cumulativeInflationPercent = deflator.subtract(BigDecimal.ONE)
                .multiply(BigDecimal(100))
                .setScale(2, RoundingMode.HALF_UP)

            // Real monthly payout = nominal / deflator
            val realMonthlyPayout = if (deflator > BigDecimal.ZERO) {
                nominalMonthly.divide(deflator, 2, RoundingMode.HALF_UP)
            } else {
                nominalMonthly
            }

            // Milestone point from compoundingResult
            val compoundingPoint = compoundingResult.points.firstOrNull { it.year == year }
            val nominalWealth = compoundingPoint?.reinvestWealth ?: investedCapital

            // Real wealth = nominal wealth / deflator
            val realWealth = if (deflator > BigDecimal.ZERO) {
                nominalWealth.divide(deflator, 2, RoundingMode.HALF_UP)
            } else {
                nominalWealth
            }

            // Purchasing power loss = nominal wealth - real wealth
            val purchasingPowerLoss = nominalWealth.subtract(realWealth).max(BigDecimal.ZERO)

            // Real growth percent = (Real Wealth - Invested Capital) / Invested Capital * 100
            val realGrowthPercent = if (investedCapital > BigDecimal.ZERO) {
                realWealth.subtract(investedCapital)
                    .multiply(BigDecimal(100))
                    .divide(investedCapital, 2, RoundingMode.HALF_UP)
            } else {
                BigDecimal.ZERO
            }

            val beatsInflation = realWealth >= investedCapital

            points.add(
                PurchasingPowerPoint(
                    year = year,
                    cumulativeInflationPercent = cumulativeInflationPercent,
                    nominalMonthlyPayout = nominalMonthly,
                    realMonthlyPayout = realMonthlyPayout,
                    nominalWealth = nominalWealth,
                    realWealth = realWealth,
                    purchasingPowerLoss = purchasingPowerLoss,
                    realGrowthPercent = realGrowthPercent,
                    beatsInflation = beatsInflation
                )
            )
        }

        val lastPoint = points.lastOrNull()
        val finalRealMonthly = lastPoint?.realMonthlyPayout ?: nominalMonthly
        val nominalFinalWealth = lastPoint?.nominalWealth ?: investedCapital
        val realFinalWealth = lastPoint?.realWealth ?: investedCapital
        val realWealthGrowthPercent = lastPoint?.realGrowthPercent ?: BigDecimal.ZERO
        val beatsInflation = lastPoint?.beatsInflation ?: true

        val avgInflation = if (horizonYears > 0) {
            totalInflationSum.divide(BigDecimal(horizonYears), 2, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }

        return PurchasingPowerForecastResult(
            currency = compoundingResult.currency,
            investedCapital = investedCapital,
            horizonYears = horizonYears,
            nominalMonthlyPayout = nominalMonthly,
            finalRealMonthlyPayout = finalRealMonthly,
            nominalFinalWealth = nominalFinalWealth,
            realFinalWealth = realFinalWealth,
            realWealthGrowthPercent = realWealthGrowthPercent,
            averageInflationRatePercent = avgInflation,
            beatsInflation = beatsInflation,
            points = points
        )
    }
}
