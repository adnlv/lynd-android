package com.adnlv.lynd.domain

import com.adnlv.lynd.data.db.BondEntity
import com.adnlv.lynd.data.db.BondPaymentEntity
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

object SmartLadderMatcher {

    fun matchGaps(
        gaps: List<IncomeGap>,
        bonds: List<BondEntity>,
        payments: List<BondPaymentEntity>,
        today: LocalDate = LocalDate.now()
    ): List<GapMatches> {
        if (gaps.isEmpty() || bonds.isEmpty()) return emptyList()

        val activeBonds = bonds.filter { it.maturityDate >= today }
        val paymentsByIsin = payments.filter { it.payDate >= today }.groupBy { it.bondIsin }

        val gapMonthsSet = gaps.map { it.yearMonth }.toSet()

        // Count how many dry months each bond covers
        val coverageCountByIsin = activeBonds.associate { bond ->
            val bondPayments = paymentsByIsin[bond.isin].orEmpty()
            val paymentMonths = bondPayments.map { YearMonth.from(it.payDate) }.toSet()
            val maturityMonth = YearMonth.from(bond.maturityDate)
            val allPayoutMonths = paymentMonths + maturityMonth
            bond.isin to allPayoutMonths.count { it in gapMonthsSet }
        }

        return gaps.map { gap ->
            val matchesForGap = mutableListOf<BondLadderMatch>()

            for (bond in activeBonds) {
                if (!bond.currency.equals(gap.currency, ignoreCase = true)) continue

                val bondPayments = paymentsByIsin[bond.isin].orEmpty()
                val monthPayments = bondPayments.filter { YearMonth.from(it.payDate) == gap.yearMonth }
                val maturesInMonth = YearMonth.from(bond.maturityDate) == gap.yearMonth

                if (monthPayments.isEmpty() && !maturesInMonth) continue

                val hasCoupon = monthPayments.any { it.payType.equals("coupon", ignoreCase = true) }
                val hasRedemption = maturesInMonth || monthPayments.any { it.payType.equals("redemption", ignoreCase = true) }

                val paymentType = when {
                    hasCoupon && hasRedemption -> LadderPaymentType.COUPON_AND_REDEMPTION
                    hasRedemption -> LadderPaymentType.REDEMPTION
                    else -> LadderPaymentType.COUPON
                }

                val payDate = monthPayments.firstOrNull()?.payDate ?: bond.maturityDate
                val totalAmount = monthPayments.fold(BigDecimal.ZERO) { acc, p -> acc.add(p.payVal) }
                    .let { if (it.compareTo(BigDecimal.ZERO) == 0 && maturesInMonth) bond.nominalValue else it }

                matchesForGap.add(
                    BondLadderMatch(
                        bond = bond,
                        paymentDate = payDate,
                        paymentType = paymentType,
                        paymentAmount = totalAmount,
                        totalGapsCovered = coverageCountByIsin[bond.isin] ?: 1
                    )
                )
            }

            val sortedMatches = matchesForGap.sortedWith(
                compareByDescending<BondLadderMatch> { it.totalGapsCovered }
                    .thenByDescending { it.bond.couponRate }
                    .thenBy { it.paymentDate }
                    .thenBy { it.bond.isin }
            )

            GapMatches(gap = gap, recommendedBonds = sortedMatches)
        }
    }
}
