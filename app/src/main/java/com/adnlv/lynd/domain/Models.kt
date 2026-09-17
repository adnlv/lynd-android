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
    val purchaseDate: LocalDate
)
