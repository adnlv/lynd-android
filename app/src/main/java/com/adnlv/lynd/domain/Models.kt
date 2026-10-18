package com.adnlv.lynd.domain

import com.adnlv.lynd.core.AppConstants
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

data class PayoutItem(
    val isin: String,
    val bondName: String,
    val payDate: LocalDate,
    val payType: String,
    val payoutAmount: BigDecimal,
    val currency: String
)

enum class PayoutType {
    COUPON,
    REDEMPTION
}

data class PayoutLot(
    val holdingId: Int = 0,
    val purchaseDate: LocalDate,
    val quantity: Int,
    val amount: BigDecimal
)

data class PayoutBondDetail(
    val isin: String,
    val bondName: String,
    val totalQuantity: Int,
    val totalAmount: BigDecimal,
    val couponAmount: BigDecimal,
    val principalAmount: BigDecimal,
    val currency: String,
    val payoutTypes: Set<PayoutType>,
    val lots: List<PayoutLot>
)

data class CurrencyTotal(
    val currency: String,
    val amount: BigDecimal
)

data class PayoutDateGroup(
    val date: LocalDate,
    val totalAmounts: List<CurrencyTotal>,
    val payoutTypes: Set<PayoutType>,
    val bonds: List<PayoutBondDetail>
) {
    val isSingleBond: Boolean get() = bonds.size == 1
    val singleBond: PayoutBondDetail? get() = bonds.singleOrNull()
}

data class HoldingItem(
    val id: Int,
    val isin: String,
    val bondName: String,
    val quantity: Int,
    val pricePerBond: BigDecimal,
    val totalPaidAmount: BigDecimal,
    val purchaseDate: LocalDate,
    val currency: String = AppConstants.DEFAULT_CURRENCY,
    val couponRate: BigDecimal = BigDecimal.ZERO,
    val totalPayoutAmount: BigDecimal = BigDecimal.ZERO,
    val totalProfitAmount: BigDecimal = BigDecimal.ZERO,
    val profitPercentage: BigDecimal = BigDecimal.ZERO
)

data class HoldingGroup(
    val isin: String,
    val currency: String = "",
    val totalQuantity: Int,
    val items: List<HoldingItem>
)

data class MonthlyCashFlow(
    val yearMonth: YearMonth,
    val couponAmount: BigDecimal,
    val principalAmount: BigDecimal,
    val totalAmount: BigDecimal
)

data class YearlyMaturity(
    val year: Int,
    val amount: BigDecimal
)
