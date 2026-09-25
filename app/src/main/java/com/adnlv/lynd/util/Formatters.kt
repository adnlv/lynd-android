package com.adnlv.lynd.util

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object Formatters {
    private val ukrainianSymbols = DecimalFormatSymbols(Locale.forLanguageTag("uk-UA")).apply {
        groupingSeparator = ' '
        decimalSeparator = ','
    }

    private val amountFormat = DecimalFormat("#,##0.00", ukrainianSymbols)
    private val rateFormat = DecimalFormat("#,##0.00", ukrainianSymbols)

    fun formatAmount(amount: BigDecimal): String {
        return amountFormat.format(amount)
    }

    fun formatCouponRate(rate: BigDecimal): String {
        return "${rateFormat.format(rate)}%"
    }

    fun formatPercentage(percentage: BigDecimal): String {
        return "${rateFormat.format(percentage)}%"
    }

    fun formatDate(date: LocalDate, locale: Locale = Locale.getDefault()): String {
        return date.format(DateTimeFormatter.ofPattern("d MMM yyyy", locale))
    }
}
