package com.adnlv.lynd

import com.adnlv.lynd.domain.CentralBankRateForecast
import com.adnlv.lynd.domain.RateTrend
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.math.BigDecimal

class CentralBankRateForecastTest {

    @Test
    fun getOutlook_returnsDecreasingTrendForUah() {
        val outlook = CentralBankRateForecast.getOutlook("UAH")
        assertEquals("UAH", outlook.currency)
        assertEquals(RateTrend.DECREASING, outlook.trend)
        assertEquals(BigDecimal("15.5"), outlook.currentRate)
        assertEquals(3, outlook.forecasts.size)
        assertEquals(2025, outlook.forecasts[0].calendarYear)
        assertEquals(BigDecimal("15.5"), outlook.forecasts[0].projectedRate)
    }

    @Test
    fun getOutlook_returnsDecreasingTrendForUsd() {
        val outlook = CentralBankRateForecast.getOutlook("USD")
        assertEquals("USD", outlook.currency)
        assertEquals(RateTrend.DECREASING, outlook.trend)
        assertEquals(BigDecimal("4.50"), outlook.currentRate)
        assertEquals(3, outlook.forecasts.size)
    }

    @Test
    fun getOutlook_returnsDecreasingTrendForEur() {
        val outlook = CentralBankRateForecast.getOutlook("EUR")
        assertEquals("EUR", outlook.currency)
        assertEquals(RateTrend.DECREASING, outlook.trend)
        assertEquals(BigDecimal("3.00"), outlook.currentRate)
        assertEquals(3, outlook.forecasts.size)
    }

    @Test
    fun getRateTrend_returnsCorrectTrend() {
        assertEquals(RateTrend.DECREASING, CentralBankRateForecast.getRateTrend("UAH"))
        assertEquals(RateTrend.DECREASING, CentralBankRateForecast.getRateTrend("USD"))
        assertEquals(RateTrend.DECREASING, CentralBankRateForecast.getRateTrend("EUR"))
    }
}
