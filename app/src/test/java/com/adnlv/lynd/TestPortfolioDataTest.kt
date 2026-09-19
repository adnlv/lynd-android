package com.adnlv.lynd

import com.adnlv.lynd.data.TestPortfolioData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class TestPortfolioDataTest {

    @Test
    fun testPortfolioData_containsAllCurrencies() {
        val currencies = TestPortfolioData.bonds.map { it.currency }.toSet()
        assertTrue(currencies.contains("UAH"))
        assertTrue(currencies.contains("USD"))
        assertTrue(currencies.contains("EUR"))
    }

    @Test
    fun testPortfolioData_allHoldingsReferenceValidBonds() {
        val bondIsins = TestPortfolioData.bonds.map { it.isin }.toSet()
        for (holding in TestPortfolioData.holdings) {
            assertTrue("Holding ${holding.isin} not found in bonds", bondIsins.contains(holding.isin))
            assertTrue("Holding quantity must be positive", holding.quantity > 0)
            assertTrue("Holding totalPaidAmount must be positive", holding.totalPaidAmount > BigDecimal.ZERO)
        }
    }

    @Test
    fun testPortfolioData_allPaymentsReferenceValidBonds() {
        val bondIsins = TestPortfolioData.bonds.map { it.isin }.toSet()
        for (payment in TestPortfolioData.payments) {
            assertTrue("Payment bondIsin ${payment.bondIsin} not found in bonds", bondIsins.contains(payment.bondIsin))
            assertTrue("Payment payVal must be positive", payment.payVal > BigDecimal.ZERO)
        }
    }

    @Test
    fun testPortfolioData_hasDifferentQuantities() {
        val quantities = TestPortfolioData.holdings.map { it.quantity }.distinct()
        assertTrue("Portfolio should have varied quantities", quantities.size >= 5)
    }
}
