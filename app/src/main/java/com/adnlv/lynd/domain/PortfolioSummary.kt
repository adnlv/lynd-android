package com.adnlv.lynd.domain

import com.adnlv.lynd.core.AppConstants
import com.adnlv.lynd.data.db.BondPaymentEntity
import com.adnlv.lynd.data.db.HoldingWithBond
import com.adnlv.lynd.data.db.PayoutRow
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth

data class PortfolioSummary(
    val currency: String,
    val investedCapital: BigDecimal,
    val expectedPayout: BigDecimal,
    val totalProfit: BigDecimal,
    val averageInterestRate: BigDecimal
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
                profitAmount.multiply(AppConstants.PERCENTAGE_MULTIPLIER)
                    .divide(item.totalPaidAmount, AppConstants.PERCENTAGE_DECIMAL_SCALE, RoundingMode.HALF_UP)
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
            weightedRateSum.divide(invested, AppConstants.PERCENTAGE_DECIMAL_SCALE, RoundingMode.HALF_UP)
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
        payoutRows: List<PayoutRow>,
        startDate: LocalDate = LocalDate.now(),
        monthCount: Int = AppConstants.DEFAULT_ANALYSIS_HORIZON_MONTHS
    ): Map<String, List<MonthlyCashFlow>> {
        val currencies = payoutRows.map { it.currency }.distinct()
        val startYearMonth = YearMonth.from(startDate)
        val targetMonths = (0 until monthCount).map { startYearMonth.plusMonths(it.toLong()) }

        val upcomingRows = payoutRows.filter {
            !it.payDate.isBefore(startDate) && !it.payDate.isBefore(it.purchaseDate)
        }

        return currencies.associateWith { currency ->
            val currencyRows = upcomingRows.filter { it.currency.equals(currency, ignoreCase = true) }
            val rowsByMonth = currencyRows.groupBy { YearMonth.from(it.payDate) }

            targetMonths.map { ym ->
                val monthRows = rowsByMonth[ym].orEmpty()
                var couponSum = BigDecimal.ZERO
                var principalSum = BigDecimal.ZERO

                for (row in monthRows) {
                    val payout = row.calculateTotalAmount()
                    if (row.isRedemption) {
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
        payoutRows: List<PayoutRow>,
        startDate: LocalDate = LocalDate.now()
    ): Map<String, List<YearlyMaturity>> {
        val currencies = payoutRows.map { it.currency }.distinct()
        val currentYear = startDate.year

        val upcomingRedemptions = payoutRows.filter {
            it.isRedemption && !it.payDate.isBefore(startDate) && !it.payDate.isBefore(it.purchaseDate)
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
                        acc.add(row.calculateTotalAmount())
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
}
