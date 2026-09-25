package com.adnlv.lynd.domain

import com.adnlv.lynd.data.db.BondEntity
import java.math.BigDecimal
import java.time.LocalDate

data class ReplacementBondSuggestion(
    val bond: BondEntity,
    val maturityDate: LocalDate,
    val couponRate: BigDecimal,
    val maturityYears: Int
)

data class MaturityAlert(
    val isin: String,
    val bondName: String,
    val currency: String,
    val redemptionDate: LocalDate,
    val daysUntilRedemption: Long,
    val principalAmount: BigDecimal,
    val rateTrend: RateTrend,
    val urgencyLevel: UrgencyLevel,
    val instruction: String,
    val estimatedMonthlyLoss: BigDecimal,
    val replacementSuggestions: List<ReplacementBondSuggestion> = emptyList()
)

enum class UrgencyLevel {
    HIGH,
    MEDIUM,
    LOW
}

data class RebalancingSummary(
    val activeAlertsCount: Int,
    val totalCapitalAtRisk: BigDecimal,
    val estimatedMonthlyLoss: BigDecimal,
    val centralBankOutlook: CentralBankOutlook
)
