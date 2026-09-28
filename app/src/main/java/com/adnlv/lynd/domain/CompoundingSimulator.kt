package com.adnlv.lynd.domain

import java.math.BigDecimal
import java.math.RoundingMode

data class CompoundingPoint(
    val year: Int,
    val withdrawProfit: BigDecimal,
    val reinvestProfit: BigDecimal,
    val extraProfit: BigDecimal,
    val withdrawWealth: BigDecimal,
    val reinvestWealth: BigDecimal
)

data class CompoundingSimulationResult(
    val currency: String,
    val investedCapital: BigDecimal,
    val smoothedMonthlyIncome: BigDecimal,
    val annualRatePercent: BigDecimal,
    val horizonYears: Int,
    val finalWithdrawProfit: BigDecimal,
    val finalReinvestProfit: BigDecimal,
    val extraProfit: BigDecimal,
    val percentageGain: BigDecimal,
    val points: List<CompoundingPoint>
)

object CompoundingSimulator {

    fun calculateSmoothedMonthlyIncome(
        currencyCashFlows: List<MonthlyCashFlow>,
        investedCapital: BigDecimal,
        averageRate: BigDecimal
    ): BigDecimal {
        val total12MonthsCoupons = currencyCashFlows.take(12).fold(BigDecimal.ZERO) { acc, flow ->
            acc.add(flow.couponAmount)
        }
        if (total12MonthsCoupons > BigDecimal.ZERO) {
            return total12MonthsCoupons.divide(BigDecimal(12), 2, RoundingMode.HALF_UP)
        }
        if (investedCapital > BigDecimal.ZERO && averageRate > BigDecimal.ZERO) {
            return investedCapital.multiply(averageRate)
                .divide(BigDecimal(1200), 2, RoundingMode.HALF_UP)
        }
        return BigDecimal.ZERO
    }

    fun simulate(
        currency: String,
        investedCapital: BigDecimal,
        monthlyIncome: BigDecimal,
        annualRatePercent: BigDecimal,
        horizonYears: Int
    ): CompoundingSimulationResult {
        val totalMonths = horizonYears * 12
        val monthlyRate = annualRatePercent.divide(BigDecimal(1200), 8, RoundingMode.HALF_UP)

        var reinvestPool = BigDecimal.ZERO
        val points = mutableListOf<CompoundingPoint>()

        for (m in 1..totalMonths) {
            val interestEarned = reinvestPool.multiply(monthlyRate)
            reinvestPool = reinvestPool.add(interestEarned).add(monthlyIncome)

            if (m % 12 == 0) {
                val year = m / 12
                val withdrawProfit = monthlyIncome.multiply(BigDecimal(m))
                val extraProfit = reinvestPool.subtract(withdrawProfit).max(BigDecimal.ZERO)
                points.add(
                    CompoundingPoint(
                        year = year,
                        withdrawProfit = withdrawProfit.setScale(2, RoundingMode.HALF_UP),
                        reinvestProfit = reinvestPool.setScale(2, RoundingMode.HALF_UP),
                        extraProfit = extraProfit.setScale(2, RoundingMode.HALF_UP),
                        withdrawWealth = investedCapital.add(withdrawProfit).setScale(2, RoundingMode.HALF_UP),
                        reinvestWealth = investedCapital.add(reinvestPool).setScale(2, RoundingMode.HALF_UP)
                    )
                )
            }
        }

        val lastPoint = points.lastOrNull()
        val finalWithdrawProfit = lastPoint?.withdrawProfit ?: BigDecimal.ZERO
        val finalReinvestProfit = lastPoint?.reinvestProfit ?: BigDecimal.ZERO
        val finalExtraProfit = lastPoint?.extraProfit ?: BigDecimal.ZERO
        val percentageGain = if (finalWithdrawProfit > BigDecimal.ZERO) {
            finalExtraProfit.multiply(BigDecimal(100)).divide(finalWithdrawProfit, 2, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }

        return CompoundingSimulationResult(
            currency = currency,
            investedCapital = investedCapital,
            smoothedMonthlyIncome = monthlyIncome,
            annualRatePercent = annualRatePercent,
            horizonYears = horizonYears,
            finalWithdrawProfit = finalWithdrawProfit,
            finalReinvestProfit = finalReinvestProfit,
            extraProfit = finalExtraProfit,
            percentageGain = percentageGain,
            points = points
        )
    }
}
