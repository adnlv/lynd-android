package com.adnlv.lynd.domain

import com.adnlv.lynd.data.db.BondEntity
import com.adnlv.lynd.data.db.BondPaymentEntity
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

object SmartLadderMatcher {
    fun generateActionableGaps(
        gaps: List<IncomeGap>,
        bonds: List<BondEntity>,
        payments: List<BondPaymentEntity>,
        today: LocalDate = LocalDate.now()
    ): List<ActionableGap> {
        if (gaps.isEmpty() || bonds.isEmpty()) return emptyList()

        val activeBonds = bonds.filter { it.maturityDate >= today }

        // O(1) Pre-hashing: ISIN -> YearMonth -> List<Payments>
        val paymentsByIsinAndMonth: Map<String, Map<YearMonth, List<BondPaymentEntity>>> =
            payments.filter { it.payDate >= today }
                .groupBy { it.bondIsin }
                .mapValues { (_, bondPayments) ->
                    bondPayments.groupBy { YearMonth.from(it.payDate) }
                }

        val gapMonthsSet = gaps.map { it.yearMonth }.toSet()

        // Calculate multi-gap coverage efficiency
        val coverageCountByIsin = activeBonds.associate { bond ->
            val isinPayments = paymentsByIsinAndMonth[bond.isin].orEmpty()
            bond.isin to isinPayments.keys.count { it in gapMonthsSet }
        }

        return gaps.map { gap ->
            val matches = activeBonds.mapNotNull { bond ->
                if (!bond.currency.equals(gap.currency, ignoreCase = true)) return@mapNotNull null

                val monthPayments = paymentsByIsinAndMonth[bond.isin]?.get(gap.yearMonth).orEmpty()
                if (monthPayments.isEmpty()) return@mapNotNull null

                val hasCoupon = monthPayments.any { it.payType.equals("coupon", ignoreCase = true) }
                val hasRedemption = monthPayments.any { it.payType.equals("redemption", ignoreCase = true) }

                val paymentType = when {
                    hasCoupon && hasRedemption -> LadderPaymentType.COUPON_AND_REDEMPTION
                    hasRedemption -> LadderPaymentType.REDEMPTION
                    else -> LadderPaymentType.COUPON
                }

                // Strictly rely on DB records to prevent double-counting
                val totalAmount = monthPayments.fold(BigDecimal.ZERO) { acc, p -> acc.add(p.payVal) }
                val payDate = monthPayments.minByOrNull { it.payDate }?.payDate ?: bond.maturityDate

                BondLadderMatch(
                    bond = bond,
                    paymentDate = payDate,
                    paymentType = paymentType,
                    paymentAmount = totalAmount,
                    totalGapsCovered = coverageCountByIsin[bond.isin] ?: 1
                )
            }.sortedWith(
                compareByDescending<BondLadderMatch> { it.totalGapsCovered }
                    .thenByDescending { it.bond.couponRate }
                    .thenBy { it.paymentDate }
            )

            ActionableGap(gap, matches)
        }
    }

    fun matchGaps(
        gaps: List<IncomeGap>,
        bonds: List<BondEntity>,
        payments: List<BondPaymentEntity>,
        today: LocalDate = LocalDate.now()
    ): List<GapMatches> = generateActionableGaps(gaps, bonds, payments, today)
}
