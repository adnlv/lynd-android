package com.adnlv.lynd.domain

import java.math.BigDecimal

enum class RateTrend {
    DECREASING,
    INCREASING,
    STABLE
}

data class PolicyRateForecast(
    val year: Int,
    val calendarYear: Int,
    val projectedRate: BigDecimal
)

data class CentralBankOutlook(
    val currency: String,
    val currentRate: BigDecimal,
    val forecasts: List<PolicyRateForecast>,
    val trend: RateTrend,
    val summaryNote: String
)

object CentralBankRateForecast {

    private val uahForecasts = listOf(
        PolicyRateForecast(year = 1, calendarYear = 2025, projectedRate = BigDecimal("15.5")),
        PolicyRateForecast(year = 2, calendarYear = 2026, projectedRate = BigDecimal("13.0")),
        PolicyRateForecast(year = 3, calendarYear = 2027, projectedRate = BigDecimal("11.5"))
    )

    private val usdForecasts = listOf(
        PolicyRateForecast(year = 1, calendarYear = 2025, projectedRate = BigDecimal("4.25")),
        PolicyRateForecast(year = 2, calendarYear = 2026, projectedRate = BigDecimal("3.75")),
        PolicyRateForecast(year = 3, calendarYear = 2027, projectedRate = BigDecimal("3.25"))
    )

    private val eurForecasts = listOf(
        PolicyRateForecast(year = 1, calendarYear = 2025, projectedRate = BigDecimal("2.75")),
        PolicyRateForecast(year = 2, calendarYear = 2026, projectedRate = BigDecimal("2.25")),
        PolicyRateForecast(year = 3, calendarYear = 2027, projectedRate = BigDecimal("2.00"))
    )

    fun getOutlook(currency: String): CentralBankOutlook {
        return when (currency.uppercase()) {
            "USD" -> CentralBankOutlook(
                currency = "USD",
                currentRate = BigDecimal("4.50"),
                forecasts = usdForecasts,
                trend = RateTrend.DECREASING,
                summaryNote = "Federal Reserve projects steady rate cuts over the next two years."
            )
            "EUR" -> CentralBankOutlook(
                currency = "EUR",
                currentRate = BigDecimal("3.00"),
                forecasts = eurForecasts,
                trend = RateTrend.DECREASING,
                summaryNote = "European Central Bank expects policy easing as inflation stabilizes."
            )
            else -> CentralBankOutlook(
                currency = "UAH",
                currentRate = BigDecimal("15.5"),
                forecasts = uahForecasts,
                trend = RateTrend.DECREASING,
                summaryNote = "National Bank of Ukraine baseline projects rate cuts to 13.0% and 11.5%."
            )
        }
    }

    fun getRateTrend(currency: String): RateTrend {
        return getOutlook(currency).trend
    }
}
