package com.adnlv.lynd.domain

import java.time.YearMonth

data class IncomeGap(
    val yearMonth: YearMonth,
    val currency: String,
    val consecutiveMonthIndex: Int = 1,
    val totalConsecutiveMonths: Int = 1
)
