package com.adnlv.lynd

import com.adnlv.lynd.data.db.BondEntity
import com.adnlv.lynd.data.db.BondPaymentEntity
import com.adnlv.lynd.domain.IncomeGap
import com.adnlv.lynd.domain.LadderPaymentType
import com.adnlv.lynd.domain.SmartLadderMatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

class SmartLadderMatcherTest {

    private val baseDate = LocalDate.of(2026, 1, 1)

    @Test
    fun matchGaps_emptyInputs_returnsEmptyList() {
        val result = SmartLadderMatcher.matchGaps(emptyList(), emptyList(), emptyList(), baseDate)
        assertTrue(result.isEmpty())
    }

    @Test
    fun matchGaps_matchesBondPayingCouponInDryMonth() {
        val gap = IncomeGap(
            yearMonth = YearMonth.of(2026, 3),
            currency = "UAH"
        )
        val bond = BondEntity(
            isin = "UA4000187348",
            name = "Gov Bond 2027",
            currency = "UAH",
            nominalValue = BigDecimal("1000.00"),
            couponRate = BigDecimal("15.00"),
            maturityDate = LocalDate.of(2027, 3, 15)
        )
        val payment = BondPaymentEntity(
            bondIsin = "UA4000187348",
            payDate = LocalDate.of(2026, 3, 15),
            payType = "coupon",
            payVal = BigDecimal("75.00")
        )

        val matches = SmartLadderMatcher.matchGaps(
            gaps = listOf(gap),
            bonds = listOf(bond),
            payments = listOf(payment),
            today = baseDate
        )

        assertEquals(1, matches.size)
        val recommended = matches[0].recommendedBonds
        assertEquals(1, recommended.size)
        assertEquals("UA4000187348", recommended[0].bond.isin)
        assertEquals(LadderPaymentType.COUPON, recommended[0].paymentType)
        assertEquals(LocalDate.of(2026, 3, 15), recommended[0].paymentDate)
        assertEquals(BigDecimal("75.00"), recommended[0].paymentAmount)
        assertEquals(1, recommended[0].totalGapsCovered)
    }

    @Test
    fun matchGaps_matchesBondMaturingInDryMonth() {
        val gap = IncomeGap(
            yearMonth = YearMonth.of(2026, 5),
            currency = "UAH"
        )
        val bond = BondEntity(
            isin = "UA4000187348",
            name = "Gov Bond 2026",
            currency = "UAH",
            nominalValue = BigDecimal("1000.00"),
            couponRate = BigDecimal("12.00"),
            maturityDate = LocalDate.of(2026, 5, 20)
        )
        val redemptionPayment = BondPaymentEntity(
            bondIsin = "UA4000187348",
            payDate = LocalDate.of(2026, 5, 20),
            payType = "redemption",
            payVal = BigDecimal("1000.00")
        )

        val matches = SmartLadderMatcher.generateActionableGaps(
            gaps = listOf(gap),
            bonds = listOf(bond),
            payments = listOf(redemptionPayment),
            today = baseDate
        )

        assertEquals(1, matches.size)
        val recommended = matches[0].recommendedBonds
        assertEquals(1, recommended.size)
        assertEquals(LadderPaymentType.REDEMPTION, recommended[0].paymentType)
        assertEquals(LocalDate.of(2026, 5, 20), recommended[0].paymentDate)
        assertEquals(BigDecimal("1000.00"), recommended[0].paymentAmount)
    }

    @Test
    fun matchGaps_strictlySumsPayValWithoutInjectingNominal() {
        val gap = IncomeGap(
            yearMonth = YearMonth.of(2026, 5),
            currency = "UAH"
        )
        val bond = BondEntity(
            isin = "UA4000187348",
            name = "Gov Bond 2026",
            currency = "UAH",
            nominalValue = BigDecimal("1000.00"),
            couponRate = BigDecimal("12.00"),
            maturityDate = LocalDate.of(2026, 5, 20)
        )
        val couponPayment = BondPaymentEntity(
            bondIsin = "UA4000187348",
            payDate = LocalDate.of(2026, 5, 20),
            payType = "coupon",
            payVal = BigDecimal("60.00")
        )

        val matches = SmartLadderMatcher.generateActionableGaps(
            gaps = listOf(gap),
            bonds = listOf(bond),
            payments = listOf(couponPayment),
            today = baseDate
        )

        val recommended = matches[0].recommendedBonds
        assertEquals(BigDecimal("60.00"), recommended[0].paymentAmount)
    }

    @Test
    fun matchGaps_doesNotDropPaymentsForMultipleBondsInSameMonth() {
        val gap = IncomeGap(
            yearMonth = YearMonth.of(2026, 6),
            currency = "UAH"
        )
        val bond1 = BondEntity(
            isin = "UA_BOND_1",
            name = "Bond 1",
            currency = "UAH",
            nominalValue = BigDecimal("1000.00"),
            couponRate = BigDecimal("14.00"),
            maturityDate = LocalDate.of(2027, 6, 1)
        )
        val bond2 = BondEntity(
            isin = "UA_BOND_2",
            name = "Bond 2",
            currency = "UAH",
            nominalValue = BigDecimal("1000.00"),
            couponRate = BigDecimal("16.00"),
            maturityDate = LocalDate.of(2027, 6, 10)
        )
        val p1 = BondPaymentEntity(
            bondIsin = "UA_BOND_1",
            payDate = LocalDate.of(2026, 6, 5),
            payType = "coupon",
            payVal = BigDecimal("70.00")
        )
        val p2 = BondPaymentEntity(
            bondIsin = "UA_BOND_2",
            payDate = LocalDate.of(2026, 6, 15),
            payType = "coupon",
            payVal = BigDecimal("80.00")
        )

        val matches = SmartLadderMatcher.generateActionableGaps(
            gaps = listOf(gap),
            bonds = listOf(bond1, bond2),
            payments = listOf(p1, p2),
            today = baseDate
        )

        assertEquals(1, matches.size)
        assertEquals(2, matches[0].recommendedBonds.size)
        // Bond 2 has higher couponRate (16.00 vs 14.00), so sorted first
        assertEquals("UA_BOND_2", matches[0].recommendedBonds[0].bond.isin)
        assertEquals("UA_BOND_1", matches[0].recommendedBonds[1].bond.isin)
    }

    @Test
    fun matchGaps_excludesPastPaymentsAndExpiredBonds() {
        val gap = IncomeGap(
            yearMonth = YearMonth.of(2025, 12),
            currency = "UAH"
        )
        val expiredBond = BondEntity(
            isin = "UA4000000001",
            name = "Past Bond",
            currency = "UAH",
            nominalValue = BigDecimal("1000.00"),
            couponRate = BigDecimal("10.00"),
            maturityDate = LocalDate.of(2025, 12, 10)
        )
        val pastPayment = BondPaymentEntity(
            bondIsin = "UA4000000001",
            payDate = LocalDate.of(2025, 12, 10),
            payType = "coupon",
            payVal = BigDecimal("50.00")
        )

        val matches = SmartLadderMatcher.matchGaps(
            gaps = listOf(gap),
            bonds = listOf(expiredBond),
            payments = listOf(pastPayment),
            today = baseDate
        )

        assertEquals(1, matches.size)
        assertTrue(matches[0].recommendedBonds.isEmpty())
    }

    @Test
    fun matchGaps_computesMultiGapCoverageCount() {
        val gap1 = IncomeGap(yearMonth = YearMonth.of(2026, 3), currency = "UAH")
        val gap2 = IncomeGap(yearMonth = YearMonth.of(2026, 9), currency = "UAH")

        val bondMulti = BondEntity(
            isin = "UA_MULTI",
            name = "Multi Gap Bond",
            currency = "UAH",
            nominalValue = BigDecimal("1000.00"),
            couponRate = BigDecimal("10.00"),
            maturityDate = LocalDate.of(2027, 3, 1)
        )
        val bondSingle = BondEntity(
            isin = "UA_SINGLE",
            name = "Single Gap Bond",
            currency = "UAH",
            nominalValue = BigDecimal("1000.00"),
            couponRate = BigDecimal("18.00"),
            maturityDate = LocalDate.of(2027, 3, 1)
        )

        val payments = listOf(
            BondPaymentEntity(bondIsin = "UA_MULTI", payDate = LocalDate.of(2026, 3, 10), payType = "coupon", payVal = BigDecimal("50.00")),
            BondPaymentEntity(bondIsin = "UA_MULTI", payDate = LocalDate.of(2026, 9, 10), payType = "coupon", payVal = BigDecimal("50.00")),
            BondPaymentEntity(bondIsin = "UA_SINGLE", payDate = LocalDate.of(2026, 3, 15), payType = "coupon", payVal = BigDecimal("90.00"))
        )

        val matches = SmartLadderMatcher.matchGaps(
            gaps = listOf(gap1, gap2),
            bonds = listOf(bondSingle, bondMulti),
            payments = payments,
            today = baseDate
        )

        val gap1Matches = matches.first { it.gap == gap1 }.recommendedBonds
        assertEquals(2, gap1Matches.size)
        // Multi covers 2 gaps, single covers 1 gap; multi sorted first despite lower coupon rate
        assertEquals("UA_MULTI", gap1Matches[0].bond.isin)
        assertEquals(2, gap1Matches[0].totalGapsCovered)
        assertEquals("UA_SINGLE", gap1Matches[1].bond.isin)
        assertEquals(1, gap1Matches[1].totalGapsCovered)

        val gap2Matches = matches.first { it.gap == gap2 }.recommendedBonds
        assertEquals(1, gap2Matches.size)
        assertEquals("UA_MULTI", gap2Matches[0].bond.isin)
    }

    @Test
    fun matchGaps_filtersOutBondsWithMismatchedCurrencies() {
        val gap = IncomeGap(yearMonth = YearMonth.of(2026, 3), currency = "USD")
        val uahBond = BondEntity(
            isin = "UA4000187348",
            name = "UAH Bond",
            currency = "UAH",
            nominalValue = BigDecimal("1000.00"),
            couponRate = BigDecimal("15.00"),
            maturityDate = LocalDate.of(2027, 3, 15)
        )
        val payment = BondPaymentEntity(
            bondIsin = "UA4000187348",
            payDate = LocalDate.of(2026, 3, 15),
            payType = "coupon",
            payVal = BigDecimal("75.00")
        )

        val matches = SmartLadderMatcher.matchGaps(
            gaps = listOf(gap),
            bonds = listOf(uahBond),
            payments = listOf(payment),
            today = baseDate
        )

        assertEquals(1, matches.size)
        assertTrue(matches[0].recommendedBonds.isEmpty())
    }
}
