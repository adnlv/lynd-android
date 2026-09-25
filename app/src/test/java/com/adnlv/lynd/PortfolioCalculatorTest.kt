package com.adnlv.lynd

import com.adnlv.lynd.data.db.BondPaymentEntity
import com.adnlv.lynd.data.db.HoldingWithBond
import com.adnlv.lynd.domain.PortfolioCalculator
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class PortfolioCalculatorTest {

    @Test
    fun mapHoldingsWithPayments_scalesPayoutByQuantity() {
        val holding = HoldingWithBond(
            id = 1,
            isin = "UA4000187348",
            bondName = "Bond 1",
            quantity = 5,
            pricePerBond = BigDecimal("1000.00"),
            totalPaidAmount = BigDecimal("5000.00"),
            purchaseDate = LocalDate.of(2025, 1, 1),
            currency = "UAH",
            couponRate = BigDecimal("10.00")
        )
        val payment = BondPaymentEntity(
            bondIsin = "UA4000187348",
            payDate = LocalDate.of(2025, 6, 1),
            payType = "coupon",
            payVal = BigDecimal("100.00")
        )

        val result = PortfolioCalculator.mapHoldingsWithPayments(listOf(holding), listOf(payment))

        assertEquals(1, result.size)
        // 100.00 * 5 = 500.00
        assertEquals(BigDecimal("500.00"), result[0].totalPayoutAmount)
    }

    @Test
    fun mapHoldingsWithPayments_filtersPaymentsByPurchaseDate() {
        val purchaseDate = LocalDate.of(2025, 6, 1)
        val holding = HoldingWithBond(
            id = 1,
            isin = "UA4000187348",
            bondName = "Bond 1",
            quantity = 1,
            pricePerBond = BigDecimal("1000.00"),
            totalPaidAmount = BigDecimal("1000.00"),
            purchaseDate = purchaseDate,
            currency = "UAH",
            couponRate = BigDecimal("10.00")
        )
        val paymentBefore = BondPaymentEntity(
            bondIsin = "UA4000187348",
            payDate = LocalDate.of(2025, 5, 31),
            payType = "coupon",
            payVal = BigDecimal("50.00")
        )
        val paymentOnDate = BondPaymentEntity(
            bondIsin = "UA4000187348",
            payDate = LocalDate.of(2025, 6, 1),
            payType = "coupon",
            payVal = BigDecimal("50.00")
        )
        val paymentAfter = BondPaymentEntity(
            bondIsin = "UA4000187348",
            payDate = LocalDate.of(2025, 12, 1),
            payType = "coupon",
            payVal = BigDecimal("1000.00")
        )

        val result = PortfolioCalculator.mapHoldingsWithPayments(
            listOf(holding),
            listOf(paymentBefore, paymentOnDate, paymentAfter)
        )

        assertEquals(1, result.size)
        // 50.00 + 1000.00 = 1050.00
        assertEquals(BigDecimal("1050.00"), result[0].totalPayoutAmount)
    }

    @Test
    fun mapHoldingsWithPayments_calculatesTotalProfitAndPercentage() {
        val holding = HoldingWithBond(
            id = 1,
            isin = "UA4000187348",
            bondName = "Bond 1",
            quantity = 2,
            pricePerBond = BigDecimal("1000.00"),
            totalPaidAmount = BigDecimal("2000.00"),
            purchaseDate = LocalDate.of(2025, 1, 1),
            currency = "UAH",
            couponRate = BigDecimal("10.00")
        )
        val payment = BondPaymentEntity(
            bondIsin = "UA4000187348",
            payDate = LocalDate.of(2025, 12, 1),
            payType = "redemption",
            payVal = BigDecimal("1125.00")
        )

        val result = PortfolioCalculator.mapHoldingsWithPayments(listOf(holding), listOf(payment))

        assertEquals(1, result.size)
        // total payout = 1125.00 * 2 = 2250.00
        assertEquals(BigDecimal("2250.00"), result[0].totalPayoutAmount)
        // total profit = 2250.00 - 2000.00 = 250.00
        assertEquals(BigDecimal("250.00"), result[0].totalProfitAmount)
        // profit percentage = 250 * 100 / 2000 = 12.5000%
        assertEquals(BigDecimal("12.5000"), result[0].profitPercentage)
    }

    @Test
    fun mapHoldingsWithPayments_zeroInvestment_returnsZeroPercentage() {
        val holding = HoldingWithBond(
            id = 1,
            isin = "UA4000187348",
            bondName = "Bond 1",
            quantity = 1,
            pricePerBond = BigDecimal.ZERO,
            totalPaidAmount = BigDecimal.ZERO,
            purchaseDate = LocalDate.of(2025, 1, 1),
            currency = "UAH",
            couponRate = BigDecimal.ZERO
        )
        val payment = BondPaymentEntity(
            bondIsin = "UA4000187348",
            payDate = LocalDate.of(2025, 6, 1),
            payType = "coupon",
            payVal = BigDecimal("50.00")
        )

        val result = PortfolioCalculator.mapHoldingsWithPayments(listOf(holding), listOf(payment))

        assertEquals(1, result.size)
        assertEquals(BigDecimal.ZERO, result[0].profitPercentage)
        assertEquals(BigDecimal("50.00"), result[0].totalProfitAmount)
    }

    @Test
    fun mapHoldingsWithPayments_blankBondName_fallsBackToIsin() {
        val holding = HoldingWithBond(
            id = 1,
            isin = "UA4000187348",
            bondName = "   ",
            quantity = 1,
            pricePerBond = BigDecimal("1000.00"),
            totalPaidAmount = BigDecimal("1000.00"),
            purchaseDate = LocalDate.of(2025, 1, 1),
            currency = "UAH",
            couponRate = BigDecimal.ZERO
        )

        val result = PortfolioCalculator.mapHoldingsWithPayments(listOf(holding), emptyList())

        assertEquals(1, result.size)
        assertEquals("UA4000187348", result[0].bondName)
    }

    @Test
    fun getCurrencyDisplayName_resolvesSupportedAndUnsupportedCurrencies() {
        assertEquals("Ukrainian Hryvnia", PortfolioCalculator.getCurrencyDisplayName("uah"))
        assertEquals("Ukrainian Hryvnia", PortfolioCalculator.getCurrencyDisplayName("UAH"))
        assertEquals("US Dollar", PortfolioCalculator.getCurrencyDisplayName("usd"))
        assertEquals("US Dollar", PortfolioCalculator.getCurrencyDisplayName("USD"))
        assertEquals("Euro", PortfolioCalculator.getCurrencyDisplayName("eur"))
        assertEquals("Euro", PortfolioCalculator.getCurrencyDisplayName("EUR"))
        assertEquals("GBP", PortfolioCalculator.getCurrencyDisplayName("GBP"))
        assertEquals("PLN", PortfolioCalculator.getCurrencyDisplayName("PLN"))
    }
}
