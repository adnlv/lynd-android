package com.adnlv.lynd.ui.planner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adnlv.lynd.domain.PurchasingPowerForecastResult
import com.adnlv.lynd.ui.components.MetricItem
import com.adnlv.lynd.util.Formatters

@Composable
fun PurchasingPowerMetricCard(
    forecast: PurchasingPowerForecastResult,
    modifier: Modifier = Modifier
) {
    val beatsInflation = forecast.beatsInflation
    val statusContainerColor = if (beatsInflation) {
        Color(0xFFE8F5E9)
    } else {
        Color(0xFFFFF3E0)
    }
    val statusContentColor = if (beatsInflation) {
        Color(0xFF2E7D32)
    } else {
        Color(0xFFE65100)
    }
    val statusLabel = if (beatsInflation) "Beats Inflation" else "Below Inflation"

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Purchasing Power (${forecast.horizonYears} Years)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusContainerColor)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusContentColor
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(
                    label = "Real Wealth (Today's Money)",
                    value = "${Formatters.formatAmount(forecast.realFinalWealth)} ${forecast.currency}",
                    valueColor = MaterialTheme.colorScheme.tertiary
                )

                MetricItem(
                    label = "Real Growth",
                    value = "${if (forecast.realWealthGrowthPercent >= java.math.BigDecimal.ZERO) "+" else ""}${Formatters.formatPercentage(forecast.realWealthGrowthPercent)}",
                    valueColor = if (beatsInflation) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(
                    label = "Nominal Wealth",
                    value = "${Formatters.formatAmount(forecast.nominalFinalWealth)} ${forecast.currency}",
                    valueColor = MaterialTheme.colorScheme.primary
                )

                MetricItem(
                    label = "Avg. Projected Inflation",
                    value = "${forecast.averageInflationRatePercent}%",
                    valueColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(
                    label = "Nominal Monthly Coupon",
                    value = "${Formatters.formatAmount(forecast.nominalMonthlyPayout)} ${forecast.currency}"
                )

                MetricItem(
                    label = "Final Real Monthly Payout",
                    value = "${Formatters.formatAmount(forecast.finalRealMonthlyPayout)} ${forecast.currency}",
                    valueColor = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}
