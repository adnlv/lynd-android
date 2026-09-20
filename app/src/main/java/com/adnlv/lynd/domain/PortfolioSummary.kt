package com.adnlv.lynd.domain

import com.adnlv.lynd.data.db.BondPaymentEntity
import com.adnlv.lynd.data.db.HoldingWithBond
import java.math.BigDecimal
import java.math.RoundingMode

data class PortfolioSummary(
    val currency: String,
    val investedCapital: BigDecimal,
    val expectedPayout: BigDecimal,
    val totalProfit: BigDecimal,
    val averageInterestRate: BigDecimal
)

data class CurrencyAllocation(
    val currency: String,
    val currencyName: String,
    val amount: BigDecimal,
    val percentage: BigDecimal
)

object PortfolioCalculator {
    fun mapHoldingsWithPayments(
        holdingsList: List<HoldingWithBond>,
        paymentsList: List<BondPaymentEntity>
    ): List<HoldingItem> {
        val paymentsByIsin = paymentsList.groupBy { it.bondIsin }

        return holdingsList.map { item ->
            val paymentsForHolding = paymentsByIsin[item.isin].orEmpty()
                .filter { !it.payDate.isBefore(item.purchaseDate) }

            val totalPayout = paymentsForHolding.fold(BigDecimal.ZERO) { acc, payment ->
                acc.add(payment.payVal.multiply(BigDecimal.valueOf(item.quantity.toLong())))
            }

            val profitAmount = totalPayout.subtract(item.totalPaidAmount)

            val profitPercent = if (item.totalPaidAmount > BigDecimal.ZERO) {
                profitAmount.multiply(BigDecimal("100"))
                    .divide(item.totalPaidAmount, 4, RoundingMode.HALF_UP)
            } else {
                BigDecimal.ZERO
            }

            HoldingItem(
                id = item.id,
                isin = item.isin,
                bondName = item.bondName.ifBlank { item.isin },
                quantity = item.quantity,
                pricePerBond = item.pricePerBond,
                totalPaidAmount = item.totalPaidAmount,
                purchaseDate = item.purchaseDate,
                currency = item.currency,
                couponRate = item.couponRate,
                totalPayoutAmount = totalPayout,
                totalProfitAmount = profitAmount,
                profitPercentage = profitPercent
            )
        }
    }

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

    fun calculateMonthlyCashFlows(
        payoutRows: List<com.adnlv.lynd.data.db.PayoutRow>,
        startDate: java.time.LocalDate = java.time.LocalDate.now(),
        monthCount: Int = 12
    ): Map<String, List<MonthlyCashFlow>> {
        val currencies = payoutRows.map { it.currency }.distinct()
        val startYearMonth = java.time.YearMonth.from(startDate)
        val targetMonths = (0 until monthCount).map { startYearMonth.plusMonths(it.toLong()) }

        val upcomingRows = payoutRows.filter {
            !it.payDate.isBefore(startDate) && !it.payDate.isBefore(it.purchaseDate)
        }

        return currencies.associateWith { currency ->
            val currencyRows = upcomingRows.filter { it.currency.equals(currency, ignoreCase = true) }
            val rowsByMonth = currencyRows.groupBy { java.time.YearMonth.from(it.payDate) }

            targetMonths.map { ym ->
                val monthRows = rowsByMonth[ym].orEmpty()
                var couponSum = BigDecimal.ZERO
                var principalSum = BigDecimal.ZERO

                for (row in monthRows) {
                    val payout = row.payVal.multiply(BigDecimal.valueOf(row.quantity.toLong()))
                    val isRedemption = row.payType.equals("redemption", ignoreCase = true) || row.payType == "2"
                    if (isRedemption) {
                        principalSum = principalSum.add(payout)
                    } else {
                        couponSum = couponSum.add(payout)
                    }
                }

                MonthlyCashFlow(
                    yearMonth = ym,
                    couponAmount = couponSum,
                    principalAmount = principalSum,
                    totalAmount = couponSum.add(principalSum)
                )
            }
        }
    }

    fun calculateYearlyMaturitySchedule(
        payoutRows: List<com.adnlv.lynd.data.db.PayoutRow>,
        startDate: java.time.LocalDate = java.time.LocalDate.now()
    ): Map<String, List<YearlyMaturity>> {
        val currencies = payoutRows.map { it.currency }.distinct()
        val currentYear = startDate.year

        val upcomingRedemptions = payoutRows.filter {
            val isRedemption = it.payType.equals("redemption", ignoreCase = true) || it.payType == "2"
            isRedemption && !it.payDate.isBefore(startDate) && !it.payDate.isBefore(it.purchaseDate)
        }

        return currencies.associateWith { currency ->
            val currencyRedemptions = upcomingRedemptions.filter { it.currency.equals(currency, ignoreCase = true) }
            if (currencyRedemptions.isEmpty()) {
                emptyList()
            } else {
                val maxYear = maxOf(currentYear, currencyRedemptions.maxOf { it.payDate.year })
                val redemptionsByYear = currencyRedemptions.groupBy { it.payDate.year }

                (currentYear..maxYear).map { year ->
                    val yearRows = redemptionsByYear[year].orEmpty()
                    val totalYearAmount = yearRows.fold(BigDecimal.ZERO) { acc, row ->
                        acc.add(row.payVal.multiply(BigDecimal.valueOf(row.quantity.toLong())))
                    }
                    YearlyMaturity(
                        year = year,
                        amount = totalYearAmount
                    )
                }
            }
        }
    }

    fun getCurrencyDisplayName(currency: String): String = when (currency.uppercase()) {

        "UAH" -> "Ukrainian Hryvnia"
        "USD" -> "US Dollar"
        "EUR" -> "Euro"
        else -> currency
    }

    fun calculateCurrencyAllocations(summaries: List<PortfolioSummary>): List<CurrencyAllocation> {
        val totalNominal = summaries.fold(BigDecimal.ZERO) { acc, summary ->
            acc.add(summary.investedCapital)
        }

        val preferredOrder = listOf("UAH", "USD", "EUR")
        val sortedSummaries = summaries.sortedWith(
            compareBy(
                { val idx = preferredOrder.indexOf(it.currency.uppercase()); if (idx >= 0) idx else Int.MAX_VALUE },
                { it.currency }
            )
        )

        return sortedSummaries.map { summary ->
            val percentage = if (totalNominal > BigDecimal.ZERO) {
                summary.investedCapital.multiply(BigDecimal("100"))
                    .divide(totalNominal, 2, RoundingMode.HALF_UP)
            } else {
                BigDecimal.ZERO
            }

            CurrencyAllocation(
                currency = summary.currency,
                currencyName = getCurrencyDisplayName(summary.currency),
                amount = summary.investedCapital,
                percentage = percentage
            )
        }
    }
}
