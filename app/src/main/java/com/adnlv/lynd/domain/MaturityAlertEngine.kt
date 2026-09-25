package com.adnlv.lynd.domain

import com.adnlv.lynd.data.db.BondEntity
import com.adnlv.lynd.data.db.PayoutRow
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object MaturityAlertEngine {

    fun evaluateAlerts(
        payoutRows: List<PayoutRow>,
        currency: String,
        threshold: BigDecimal,
        catalogBonds: List<BondEntity> = emptyList(),
        today: LocalDate = LocalDate.now()
    ): List<MaturityAlert> {
        val outlook = CentralBankRateForecast.getOutlook(currency)
        val filteredRedemptions = payoutRows.filter { row ->
            row.currency.equals(currency, ignoreCase = true) &&
                isRedemption(row.payType)
        }

        // Group redemptions by bond ISIN and payDate (if multiple holdings exist for the same bond)
        val groupedRedemptions = filteredRedemptions.groupBy { it.isin to it.payDate }

        val alerts = mutableListOf<MaturityAlert>()

        for ((key, rows) in groupedRedemptions) {
            val (isin, payDate) = key
            val daysBetween = ChronoUnit.DAYS.between(today, payDate)
            if (daysBetween !in 0..30) continue

            val totalPrincipal = rows.fold(BigDecimal.ZERO) { acc, r ->
                acc.add(r.payVal.multiply(BigDecimal(r.quantity)))
            }

            if (totalPrincipal < threshold) continue

            val bondName = rows.firstOrNull()?.bondName.orEmpty().ifBlank { isin }

            val urgency = when {
                daysBetween <= 7 -> UrgencyLevel.HIGH
                daysBetween <= 15 -> UrgencyLevel.MEDIUM
                else -> UrgencyLevel.LOW
            }

            val instruction = when (outlook.trend) {
                RateTrend.DECREASING ->
                    "Central bank rates will drop soon. Buy long-term bonds now to lock in high yields before redemption."
                RateTrend.INCREASING ->
                    "Central bank rates will rise. Reinvest in short-term bonds or wait for higher yields."
                RateTrend.STABLE ->
                    "Rates remain stable. Reinvest into your monthly ladder to avoid zero interest in your bank account."
            }

            // Monthly Loss = (Principal * Current Rate / 100) / 12
            val monthlyLoss = totalPrincipal
                .multiply(outlook.currentRate)
                .divide(BigDecimal("100"), 4, RoundingMode.HALF_UP)
                .divide(BigDecimal("12"), 2, RoundingMode.HALF_UP)

            // Match candidate bonds from catalog with maturity > 12 months (365 days)
            val suggestions = catalogBonds
                .filter { it.currency.equals(currency, ignoreCase = true) }
                .filter { ChronoUnit.DAYS.between(today, it.maturityDate) > 365 }
                .sortedWith(
                    compareByDescending<BondEntity> { it.couponRate }
                        .thenBy { it.maturityDate }
                )
                .take(3)
                .map { bond ->
                    val years = ((ChronoUnit.DAYS.between(today, bond.maturityDate) + 180) / 365).toInt().coerceAtLeast(1)
                    ReplacementBondSuggestion(
                        bond = bond,
                        maturityDate = bond.maturityDate,
                        couponRate = bond.couponRate,
                        maturityYears = years
                    )
                }

            alerts.add(
                MaturityAlert(
                    isin = isin,
                    bondName = bondName,
                    currency = currency,
                    redemptionDate = payDate,
                    daysUntilRedemption = daysBetween,
                    principalAmount = totalPrincipal,
                    rateTrend = outlook.trend,
                    urgencyLevel = urgency,
                    instruction = instruction,
                    estimatedMonthlyLoss = monthlyLoss,
                    replacementSuggestions = suggestions
                )
            )
        }

        return alerts.sortedBy { it.daysUntilRedemption }
    }

    fun buildSummary(
        alerts: List<MaturityAlert>,
        currency: String
    ): RebalancingSummary {
        val outlook = CentralBankRateForecast.getOutlook(currency)
        val totalCapital = alerts.fold(BigDecimal.ZERO) { acc, a -> acc.add(a.principalAmount) }
        val totalMonthlyLoss = alerts.fold(BigDecimal.ZERO) { acc, a -> acc.add(a.estimatedMonthlyLoss) }

        return RebalancingSummary(
            activeAlertsCount = alerts.size,
            totalCapitalAtRisk = totalCapital,
            estimatedMonthlyLoss = totalMonthlyLoss,
            centralBankOutlook = outlook
        )
    }

    private fun isRedemption(payType: String): Boolean {
        val normalized = payType.trim().lowercase()
        return normalized == "redemption" || normalized == "2"
    }
}
