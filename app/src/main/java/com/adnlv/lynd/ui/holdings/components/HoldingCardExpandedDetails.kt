package com.adnlv.lynd.ui.holdings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.adnlv.lynd.domain.HoldingItem
import com.adnlv.lynd.ui.components.MetricItem
import com.adnlv.lynd.util.Formatters
import java.math.BigDecimal

@Composable
fun HoldingCardExpandedDetails(
    holding: HoldingItem,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricItem(
                label = "Price per bond",
                value = Formatters.formatAmount(holding.pricePerBond),
                modifier = Modifier.weight(1f)
            )
            val displayRate = if (holding.profitPercentage != BigDecimal.ZERO) {
                holding.profitPercentage
            } else {
                holding.couponRate
            }
            MetricItem(
                label = "Coupon rate",
                value = Formatters.formatPercentage(displayRate),
                valueColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricItem(
                label = "Total payout",
                value = Formatters.formatAmount(holding.totalPayoutAmount),
                valueColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            val isProfitPositive = holding.totalProfitAmount >= BigDecimal.ZERO
            val profitPrefix = if (isProfitPositive && holding.totalProfitAmount > BigDecimal.ZERO) "+" else ""
            val profitColor = if (isProfitPositive) {
                MaterialTheme.colorScheme.tertiary
            } else {
                MaterialTheme.colorScheme.error
            }
            MetricItem(
                label = "Total profit",
                value = "$profitPrefix${Formatters.formatAmount(holding.totalProfitAmount)}",
                valueColor = profitColor,
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            )
        }
    }
}
