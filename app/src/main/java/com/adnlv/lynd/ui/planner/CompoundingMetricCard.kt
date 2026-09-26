package com.adnlv.lynd.ui.planner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adnlv.lynd.domain.CompoundingSimulationResult
import com.adnlv.lynd.ui.components.MetricItem
import com.adnlv.lynd.util.Formatters

@Composable
fun CompoundingMetricCard(
    simulation: CompoundingSimulationResult,
    modifier: Modifier = Modifier
) {
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
            Text(
                text = "Simulation Summary (${simulation.horizonYears} Years)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(
                    label = "Monthly Coupon Flow",
                    value = "${Formatters.formatAmount(simulation.smoothedMonthlyIncome)} ${simulation.currency}"
                )

                MetricItem(
                    label = "Reinvestment Rate",
                    value = "${simulation.annualRatePercent}%"
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(
                    label = "Withdrawn Total",
                    value = "${Formatters.formatAmount(simulation.finalWithdrawProfit)} ${simulation.currency}",
                    valueColor = MaterialTheme.colorScheme.onSurfaceVariant
                )

                MetricItem(
                    label = "Reinvested Total",
                    value = "${Formatters.formatAmount(simulation.finalReinvestProfit)} ${simulation.currency}",
                    valueColor = MaterialTheme.colorScheme.tertiary
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(
                    label = "Compound Advantage",
                    value = "+${Formatters.formatAmount(simulation.extraProfit)} ${simulation.currency}",
                    valueColor = MaterialTheme.colorScheme.primary
                )

                MetricItem(
                    label = "Growth Advantage",
                    value = "+${Formatters.formatPercentage(simulation.percentageGain)}",
                    valueColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
