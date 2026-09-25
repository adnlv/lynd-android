package com.adnlv.lynd

import com.adnlv.lynd.data.db.BondEntity
import com.adnlv.lynd.data.db.PayoutRow
import com.adnlv.lynd.domain.MaturityAlertEngine
import com.adnlv.lynd.domain.RateTrend
import com.adnlv.lynd.domain.UrgencyLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class MaturityAlertEngineTest {

    private val today = LocalDate.of(2025, 5, 1)

    @Test
    fun evaluateAlerts_includesRedemptionWithin30DaysAboveThreshold() {
        val payoutRow = PayoutRow(
            isin = "UA4000123456",
            bondName = "Bond Maturing Soon",
            payDate = today.plusDays(20),
            payType = "redemption",
            payVal = BigDecimal("1000.00"),
            quantity = 15,
            currency = "UAH",
            purchaseDate = today.minusYears(1)
        )

        val alerts = MaturityAlertEngine.evaluateAlerts(
            payoutRows = listOf(payoutRow),
            currency = "UAH",
            threshold = BigDecimal("10000.00"),
            today = today
        )

        assertEquals(1, alerts.size)
        val alert = alerts[0]
        assertEquals("UA4000123456", alert.isin)
        assertEquals(20L, alert.daysUntilRedemption)
        assertEquals(BigDecimal("15000.00"), alert.principalAmount)
        assertEquals(UrgencyLevel.LOW, alert.urgencyLevel)
        assertEquals(RateTrend.DECREASING, alert.rateTrend)
        assertTrue(alert.instruction.contains("Central bank rates will drop soon"))

        // Monthly loss for 15000 UAH at 15.5%: (15000 * 0.155) / 12 = 193.75
        assertEquals(BigDecimal("193.75"), alert.estimatedMonthlyLoss)
    }

    @Test
    fun evaluateAlerts_excludesRedemptionsBeyond30Days() {
        val payoutRow = PayoutRow(
            isin = "UA4000123456",
            bondName = "Bond Far Ahead",
            payDate = today.plusDays(35),
            payType = "redemption",
            payVal = BigDecimal("1000.00"),
            quantity = 20,
            currency = "UAH",
            purchaseDate = today.minusYears(1)
        )

        val alerts = MaturityAlertEngine.evaluateAlerts(
            payoutRows = listOf(payoutRow),
            currency = "UAH",
            threshold = BigDecimal("10000.00"),
            today = today
        )

        assertEquals(0, alerts.size)
    }

    @Test
    fun evaluateAlerts_excludesRedemptionsBelowThreshold() {
        val payoutRow = PayoutRow(
            isin = "UA4000123456",
            bondName = "Small Bond",
            payDate = today.plusDays(10),
            payType = "redemption",
            payVal = BigDecimal("1000.00"),
            quantity = 5, // 5000 < 10000
            currency = "UAH",
            purchaseDate = today.minusYears(1)
        )

        val alerts = MaturityAlertEngine.evaluateAlerts(
            payoutRows = listOf(payoutRow),
            currency = "UAH",
            threshold = BigDecimal("10000.00"),
            today = today
        )

        assertEquals(0, alerts.size)
    }

    @Test
    fun evaluateAlerts_excludesCoupons() {
        val payoutRow = PayoutRow(
            isin = "UA4000123456",
            bondName = "Coupon Row",
            payDate = today.plusDays(10),
            payType = "coupon",
            payVal = BigDecimal("50000.00"),
            quantity = 1,
            currency = "UAH",
            purchaseDate = today.minusYears(1)
        )

        val alerts = MaturityAlertEngine.evaluateAlerts(
            payoutRows = listOf(payoutRow),
            currency = "UAH",
            threshold = BigDecimal("10000.00"),
            today = today
        )

        assertEquals(0, alerts.size)
    }

    @Test
    fun evaluateAlerts_matchesReplacementBondsWithMaturityOverOneYear() {
        val payoutRow = PayoutRow(
            isin = "UA4000123456",
            bondName = "Maturing Bond",
            payDate = today.plusDays(5),
            payType = "2", // numeric 2 redemption code
            payVal = BigDecimal("1000.00"),
            quantity = 15,
            currency = "UAH",
            purchaseDate = today.minusYears(1)
        )

        val catalogBonds = listOf(
            BondEntity(
                isin = "UA4000SHORT",
                name = "Short Bond",
                currency = "UAH",
                nominalValue = BigDecimal("1000.00"),
                couponRate = BigDecimal("18.00"),
                maturityDate = today.plusMonths(6) // <= 365 days, should be excluded
            ),
            BondEntity(
                isin = "UA4000LONG1",
                name = "Long Bond 1",
                currency = "UAH",
                nominalValue = BigDecimal("1000.00"),
                couponRate = BigDecimal("16.50"),
                maturityDate = today.plusYears(2)
            ),
            BondEntity(
                isin = "UA4000LONG2",
                name = "Long Bond 2",
                currency = "UAH",
                nominalValue = BigDecimal("1000.00"),
                couponRate = BigDecimal("17.00"),
                maturityDate = today.plusYears(3)
            )
        )

        val alerts = MaturityAlertEngine.evaluateAlerts(
            payoutRows = listOf(payoutRow),
            currency = "UAH",
            threshold = BigDecimal("10000.00"),
            catalogBonds = catalogBonds,
            today = today
        )

        assertEquals(1, alerts.size)
        val alert = alerts[0]
        assertEquals(UrgencyLevel.HIGH, alert.urgencyLevel) // 5 days <= 7
        assertEquals(2, alert.replacementSuggestions.size)
        // Sorted by couponRate descending
        assertEquals("UA4000LONG2", alert.replacementSuggestions[0].bond.isin)
        assertEquals("UA4000LONG1", alert.replacementSuggestions[1].bond.isin)
    }

    @Test
    fun buildSummary_aggregatesAlertsCorrectly() {
        val payoutRows = listOf(
            PayoutRow(
                isin = "UA4000BOND1",
                bondName = "Bond 1",
                payDate = today.plusDays(10),
                payType = "redemption",
                payVal = BigDecimal("1000.00"),
                quantity = 10,
                currency = "UAH",
                purchaseDate = today.minusYears(1)
            ),
            PayoutRow(
                isin = "UA4000BOND2",
                bondName = "Bond 2",
                payDate = today.plusDays(25),
                payType = "redemption",
                payVal = BigDecimal("1000.00"),
                quantity = 20,
                currency = "UAH",
                purchaseDate = today.minusYears(1)
            )
        )

        val alerts = MaturityAlertEngine.evaluateAlerts(
            payoutRows = payoutRows,
            currency = "UAH",
            threshold = BigDecimal("10000.00"),
            today = today
        )

        val summary = MaturityAlertEngine.buildSummary(alerts, "UAH")
        assertEquals(2, summary.activeAlertsCount)
        assertEquals(BigDecimal("30000.00"), summary.totalCapitalAtRisk)
        // (30000 * 0.155) / 12 = 387.50
        assertEquals(BigDecimal("387.50"), summary.estimatedMonthlyLoss)
    }
}
