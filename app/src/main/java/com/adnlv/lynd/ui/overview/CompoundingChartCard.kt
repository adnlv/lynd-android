package com.adnlv.lynd.ui.overview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adnlv.lynd.domain.CompoundingPoint
import com.adnlv.lynd.domain.CompoundingSimulationResult
import com.adnlv.lynd.ui.components.MetricItem
import com.adnlv.lynd.util.Formatters
import java.math.BigDecimal

@Composable
fun CompoundingChartCard(
    simulation: CompoundingSimulationResult,
    modifier: Modifier = Modifier
) {
    val withdrawColor = MaterialTheme.colorScheme.primary
    val reinvestColor = MaterialTheme.colorScheme.tertiary
    val extraColor = MaterialTheme.colorScheme.secondary

    var selectedYear by remember(simulation) {
        mutableStateOf<Int?>(simulation.points.lastOrNull()?.year)
    }

    val selectedPoint = remember(selectedYear, simulation) {
        simulation.points.firstOrNull { it.year == selectedYear } ?: simulation.points.lastOrNull()
    }

    val maxAmount = remember(simulation) {
        simulation.points.maxOfOrNull { it.reinvestProfit } ?: BigDecimal.ONE
    }

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
                Column {
                    Text(
                        text = "Wealth Accumulation",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Spent vs. Reinvested Payouts",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "${simulation.horizonYears}Y Horizon",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(withdrawColor)
                    )
                    Text(
                        text = "Spent Payouts",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(extraColor)
                    )
                    Text(
                        text = "Compound Interest",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Chart area
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                simulation.points.forEach { point ->
                    CompoundingBar(
                        point = point,
                        maxAmount = maxAmount,
                        isSelected = selectedPoint?.year == point.year,
                        withdrawColor = withdrawColor,
                        reinvestColor = reinvestColor,
                        extraColor = extraColor,
                        onClick = { selectedYear = point.year }
                    )
                }
            }

            // Selected milestone details
            if (selectedPoint != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Year ${selectedPoint.year} Milestone",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricItem(
                            label = "Spent Payouts",
                            value = "${Formatters.formatAmount(selectedPoint.withdrawProfit)} ${simulation.currency}",
                            valueColor = withdrawColor
                        )

                        MetricItem(
                            label = "Reinvested Total",
                            value = "${Formatters.formatAmount(selectedPoint.reinvestProfit)} ${simulation.currency}",
                            valueColor = reinvestColor
                        )

                        MetricItem(
                            label = "Compound Bonus",
                            value = "+${Formatters.formatAmount(selectedPoint.extraProfit)} ${simulation.currency}",
                            valueColor = extraColor
                        )
                    }
                }
            }
        }
    }
}
