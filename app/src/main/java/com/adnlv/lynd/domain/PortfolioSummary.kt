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
    val averageInterestRate: BigDecimal,
    val holdingCount: Int = 0
)

object PortfolioCalculators {

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
        val invested = currencyHoldings.fold(BigDecimal.ZERO) { acc, h -> acc.add(h.totalPaidAmount) }
        val payout = currencyHoldings.fold(BigDecimal.ZERO) { acc, h -> acc.add(h.totalPayoutAmount) }
        val profit = payout.subtract(invested)

        val weightedRateSum = currencyHoldings.fold(BigDecimal.ZERO) { acc, h ->
            acc.add(h.couponRate.multiply(h.totalPaidAmount))
        }

        val avgRate = if (invested > BigDecimal.ZERO) {
            weightedRateSum.divide(invested, 2, RoundingMode.HALF_UP)
        } else if (currencyHoldings.isNotEmpty()) {
            val totalRate = currencyHoldings.fold(BigDecimal.ZERO) { acc, h -> acc.add(h.couponRate) }
            totalRate.divide(BigDecimal.valueOf(currencyHoldings.size.toLong()), 2, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }

        return PortfolioSummary(
            currency = currency,
            investedCapital = invested,
            expectedPayout = payout,
            totalProfit = profit,
            averageInterestRate = avgRate,
            holdingCount = currencyHoldings.size
        )
    }

    fun calculateSummaries(holdings: List<HoldingItem>): List<PortfolioSummary> {
        val currencies = holdings.map { it.currency.uppercase() }.distinct()
        return currencies.map { currency ->
            calculateSummary(currency, holdings)
        }
    }
}
