package com.adnlv.lynd.domain

import java.math.BigDecimal

data class InflationYearForecast(
    val yearOffset: Int,
    val calendarYear: Int,
    val ratePercent: BigDecimal
)

object NbuInflationData {
    val UAH_BASELINE = listOf(
        InflationYearForecast(1, 2026, BigDecimal("8.1")),
        InflationYearForecast(2, 2027, BigDecimal("6.5")),
        InflationYearForecast(3, 2028, BigDecimal("5.0")),
        InflationYearForecast(4, 2029, BigDecimal("5.0")),
        InflationYearForecast(5, 2030, BigDecimal("5.0")),
        InflationYearForecast(6, 2031, BigDecimal("5.0")),
        InflationYearForecast(7, 2032, BigDecimal("5.0")),
        InflationYearForecast(8, 2033, BigDecimal("5.0")),
        InflationYearForecast(9, 2034, BigDecimal("5.0")),
        InflationYearForecast(10, 2035, BigDecimal("5.0"))
    )

    fun getRatesForCurrency(currency: String, horizonYears: Int): List<BigDecimal> {
        val baseList = when (currency.uppercase()) {
            "USD" -> List(10) { BigDecimal("2.2") }
            "EUR" -> List(10) { BigDecimal("2.0") }
            else -> UAH_BASELINE.map { it.ratePercent }
        }
        return baseList.take(horizonYears)
    }
}
