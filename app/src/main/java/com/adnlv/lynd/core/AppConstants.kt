package com.adnlv.lynd.core

import java.math.BigDecimal

object AppConstants {
    const val DEFAULT_CURRENCY = "UAH"
    const val PRICE_DECIMAL_SCALE = 2
    const val PERCENTAGE_DECIMAL_SCALE = 4
    val PERCENTAGE_MULTIPLIER: BigDecimal = BigDecimal("100")
    val DEFAULT_NOMINAL_VALUE: BigDecimal = BigDecimal("1000.00")
    const val ISIN_TOTAL_LENGTH = 12
    const val ISIN_CODE_LENGTH = 6
    const val ISIN_SEARCH_LIMIT = 50
    const val VIEW_MODEL_STOP_TIMEOUT_MS = 5000L
    const val DEFAULT_ANALYSIS_HORIZON_MONTHS = 12
}
