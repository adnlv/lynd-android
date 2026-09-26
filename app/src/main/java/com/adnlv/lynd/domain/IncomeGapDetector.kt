package com.adnlv.lynd.domain

import com.adnlv.lynd.data.db.PayoutRow
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

object IncomeGapDetector {
    fun detectGaps(
        payoutRows: List<PayoutRow>,
        currency: String,
        startDate: LocalDate = LocalDate.now(),
        monthCount: Int = 12
    ): List<IncomeGap> {
        val allCashFlows = PortfolioCalculator.calculateMonthlyCashFlows(
            payoutRows = payoutRows,
            startDate = startDate,
            monthCount = monthCount
        )
        val selectedCashFlows = allCashFlows[currency].orEmpty()

        val startYearMonth = YearMonth.from(startDate)
        val allMonths = (0 until monthCount).map { startYearMonth.plusMonths(it.toLong()) }
        val flowByMonth = selectedCashFlows.associateBy { it.yearMonth }

        val dryMonths = allMonths.filter { ym ->
            val flow = flowByMonth[ym]
            flow == null || flow.totalAmount.compareTo(BigDecimal.ZERO) == 0
        }

        val otherCurrencies = allCashFlows.keys.filter { !it.equals(currency, ignoreCase = true) }
        val crossCoverageByMonth: Map<YearMonth, Map<String, BigDecimal>> = dryMonths.associateWith { ym ->
            val coverage = mutableMapOf<String, BigDecimal>()
            for (otherCurr in otherCurrencies) {
                val otherFlows = allCashFlows[otherCurr].orEmpty()
                val flow = otherFlows.find { it.yearMonth == ym }
                if (flow != null && flow.totalAmount > BigDecimal.ZERO) {
                    coverage[otherCurr] = flow.totalAmount
                }
            }
            coverage
        }

        return computeConsecutiveGaps(dryMonths, currency, crossCoverageByMonth)
    }

    private fun computeConsecutiveGaps(
        dryMonths: List<YearMonth>,
        currency: String,
        crossCoverageByMonth: Map<YearMonth, Map<String, BigDecimal>> = emptyMap()
    ): List<IncomeGap> {
        if (dryMonths.isEmpty()) return emptyList()

        val streaks = mutableListOf<List<YearMonth>>()
        var currentStreak = mutableListOf<YearMonth>()

        for (month in dryMonths) {
            if (currentStreak.isEmpty()) {
                currentStreak.add(month)
            } else {
                val last = currentStreak.last()
                if (last.plusMonths(1) == month) {
                    currentStreak.add(month)
                } else {
                    streaks.add(currentStreak.toList())
                    currentStreak = mutableListOf(month)
                }
            }
        }
        if (currentStreak.isNotEmpty()) {
            streaks.add(currentStreak)
        }

        return streaks.flatMap { streak ->
            streak.mapIndexed { index, ym ->
                IncomeGap(
                    yearMonth = ym,
                    currency = currency,
                    consecutiveMonthIndex = index + 1,
                    totalConsecutiveMonths = streak.size,
                    crossCurrencyCoverage = crossCoverageByMonth[ym].orEmpty()
                )
            }
        }
    }
}
