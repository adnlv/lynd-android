package com.adnlv.lynd.domain

import java.math.BigDecimal
import java.time.LocalDate

data class PayoutItem(
    val isin: String,
    val bondName: String,
    val payDate: LocalDate,
    val payType: String,
    val payoutAmount: BigDecimal,
    val currency: String
)

data class HoldingItem(
    val id: Int,
    val isin: String,
    val bondName: String,
    val quantity: Int,
    val pricePerBond: BigDecimal,
    val totalPaidAmount: BigDecimal,
    val purchaseDate: LocalDate,
    val currency: String = "UAH",
    val couponRate: BigDecimal = BigDecimal.ZERO,
    val totalPayoutAmount: BigDecimal = BigDecimal.ZERO,
    val totalProfitAmount: BigDecimal = BigDecimal.ZERO,
    val profitPercentage: BigDecimal = BigDecimal.ZERO
)

data class HoldingGroup(
    val isin: String,
    val totalQuantity: Int,
    val items: List<HoldingItem>
)

data class MonthlyCashFlow(
    val yearMonth: java.time.YearMonth,
    val couponAmount: BigDecimal,
    val principalAmount: BigDecimal,
    val totalAmount: BigDecimal
)

data class YearlyMaturity(
    val year: Int,
    val amount: BigDecimal
)

