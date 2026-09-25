package com.adnlv.lynd

import com.adnlv.lynd.data.db.PayoutRow
import com.adnlv.lynd.domain.IncomeGapDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

class IncomeGapDetectorTest {

    private val startDate = LocalDate.of(2026, 1, 15)

    private fun createPayoutRow(
        payDate: LocalDate,
        currency: String = "UAH",
        payVal: BigDecimal = BigDecimal("50.00"),
        quantity: Int = 10,
        payType: String = "coupon",
        purchaseDate: LocalDate = LocalDate.of(2025, 1, 1)
    ): PayoutRow {
        return PayoutRow(
            isin = "UA4000123456",
            bondName = "Test Bond",
            payDate = payDate,
            payType = payType,
            payVal = payVal,
            quantity = quantity,
            currency = currency,
            purchaseDate = purchaseDate
        )
    }

    @Test
    fun detectGaps_identifiesDryMonthsIn12MonthHorizon() {
        // Payouts only in Jan 2026, Mar 2026, and Nov 2026
        val rows = listOf(
            createPayoutRow(payDate = LocalDate.of(2026, 1, 20)),
            createPayoutRow(payDate = LocalDate.of(2026, 3, 10)),
            createPayoutRow(payDate = LocalDate.of(2026, 11, 5))
        )

        val gaps = IncomeGapDetector.detectGaps(
            payoutRows = rows,
            currency = "UAH",
            startDate = startDate,
            monthCount = 12
        )

        // 12 months total: Jan to Dec 2026.
        // Payout months: Jan, Mar, Nov (3 months).
        // Dry months: Feb, Apr, May, Jun, Jul, Aug, Sep, Oct, Dec (9 months).
        assertEquals(9, gaps.size)

        // Feb 2026 is isolated streak of 1 month
        val febGap = gaps.find { it.yearMonth == YearMonth.of(2026, 2) }
        assertEquals(1, febGap?.consecutiveMonthIndex)
        assertEquals(1, febGap?.totalConsecutiveMonths)

        // Apr-Oct 2026 is consecutive streak of 7 months
        val aprGap = gaps.find { it.yearMonth == YearMonth.of(2026, 4) }
        assertEquals(1, aprGap?.consecutiveMonthIndex)
        assertEquals(7, aprGap?.totalConsecutiveMonths)

        val octGap = gaps.find { it.yearMonth == YearMonth.of(2026, 10) }
        assertEquals(7, octGap?.consecutiveMonthIndex)
        assertEquals(7, octGap?.totalConsecutiveMonths)
    }

    @Test
    fun detectGaps_identifiesDryMonthsIn24MonthHorizon() {
        val rows = listOf(
            createPayoutRow(payDate = LocalDate.of(2026, 1, 20))
        )

        val gaps = IncomeGapDetector.detectGaps(
            payoutRows = rows,
            currency = "UAH",
            startDate = startDate,
            monthCount = 24
        )

        // 24 months total, 1 with payout, 23 dry months
        assertEquals(23, gaps.size)
        val firstGap = gaps.first()
        val lastGap = gaps.last()

        assertEquals(YearMonth.of(2026, 2), firstGap.yearMonth)
        assertEquals(1, firstGap.consecutiveMonthIndex)
        assertEquals(23, firstGap.totalConsecutiveMonths)

        assertEquals(YearMonth.of(2027, 12), lastGap.yearMonth)
        assertEquals(23, lastGap.consecutiveMonthIndex)
        assertEquals(23, lastGap.totalConsecutiveMonths)
    }

    @Test
    fun detectGaps_returnsEmptyListWhenAllMonthsHavePayouts() {
        val rows = (0 until 12).map { offset ->
            createPayoutRow(payDate = startDate.plusMonths(offset.toLong()))
        }

        val gaps = IncomeGapDetector.detectGaps(
            payoutRows = rows,
            currency = "UAH",
            startDate = startDate,
            monthCount = 12
        )

        assertTrue(gaps.isEmpty())
    }

    @Test
    fun detectGaps_calculatesConsecutiveStreaksCorrectly() {
        // Payouts in Jan (month 0), Apr (month 3)
        // Dry months: Feb (month 1), Mar (month 2) -> streak of 2
        // Dry months: May-Dec -> streak of 8
        val rows = listOf(
            createPayoutRow(payDate = LocalDate.of(2026, 1, 20)),
            createPayoutRow(payDate = LocalDate.of(2026, 4, 10))
        )

        val gaps = IncomeGapDetector.detectGaps(
            payoutRows = rows,
            currency = "UAH",
            startDate = startDate,
            monthCount = 12
        )

        assertEquals(10, gaps.size)

        val feb = gaps[0]
        assertEquals(YearMonth.of(2026, 2), feb.yearMonth)
        assertEquals(1, feb.consecutiveMonthIndex)
        assertEquals(2, feb.totalConsecutiveMonths)

        val mar = gaps[1]
        assertEquals(YearMonth.of(2026, 3), mar.yearMonth)
        assertEquals(2, mar.consecutiveMonthIndex)
        assertEquals(2, mar.totalConsecutiveMonths)

        val may = gaps[2]
        assertEquals(YearMonth.of(2026, 5), may.yearMonth)
        assertEquals(1, may.consecutiveMonthIndex)
        assertEquals(8, may.totalConsecutiveMonths)
    }
}
