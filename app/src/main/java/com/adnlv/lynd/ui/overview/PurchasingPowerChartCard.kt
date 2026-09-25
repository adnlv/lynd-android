package com.adnlv.lynd.ui.overview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adnlv.lynd.domain.PurchasingPowerForecastResult
import com.adnlv.lynd.ui.components.MetricItem
import com.adnlv.lynd.util.Formatters
import java.math.BigDecimal

@Composable
fun PurchasingPowerChartCard(
    forecast: PurchasingPowerForecastResult,
    modifier: Modifier = Modifier
) {
    val nominalColor = MaterialTheme.colorScheme.primary
    val realColor = MaterialTheme.colorScheme.tertiary

    var selectedYear by remember(forecast) {
        mutableStateOf<Int?>(forecast.points.lastOrNull()?.year)
    }

    val selectedPoint = remember(selectedYear, forecast) {
        forecast.points.firstOrNull { it.year == selectedYear } ?: forecast.points.lastOrNull()
    }

    val maxAmount = remember(forecast) {
        forecast.points.maxOfOrNull { it.nominalWealth } ?: BigDecimal.ONE
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
                        text = "Real vs. Nominal Wealth",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Impact of Inflation Over Time",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "${forecast.horizonYears}Y Horizon",
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
                            .background(nominalColor)
                    )
                    Text(
                        text = "Nominal Wealth",
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
                            .background(realColor)
                    )
                    Text(
                        text = "Real Wealth (Today's Money)",
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
                forecast.points.forEach { point ->
                    PurchasingPowerBar(
                        point = point,
                        maxAmount = maxAmount,
                        isSelected = selectedPoint?.year == point.year,
                        nominalColor = nominalColor,
                        realColor = realColor,
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
                        text = "Year ${selectedPoint.year} Breakdown (Cumulative Inflation: ${selectedPoint.cumulativeInflationPercent}%)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricItem(
                            label = "Nominal Wealth",
                            value = "${Formatters.formatAmount(selectedPoint.nominalWealth)} ${forecast.currency}",
                            valueColor = nominalColor
                        )

                        MetricItem(
                            label = "Real Wealth",
                            value = "${Formatters.formatAmount(selectedPoint.realWealth)} ${forecast.currency}",
                            valueColor = realColor
                        )

                        MetricItem(
                            label = "Inflation Drag",
                            value = "-${Formatters.formatAmount(selectedPoint.purchasingPowerLoss)} ${forecast.currency}",
                            valueColor = MaterialTheme.colorScheme.error
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricItem(
                            label = "Real Monthly Payout",
                            value = "${Formatters.formatAmount(selectedPoint.realMonthlyPayout)} ${forecast.currency}",
                            valueColor = realColor
                        )

                        MetricItem(
                            label = "Real Net Growth",
                            value = "${if (selectedPoint.realGrowthPercent >= BigDecimal.ZERO) "+" else ""}${Formatters.formatPercentage(selectedPoint.realGrowthPercent)}",
                            valueColor = if (selectedPoint.beatsInflation) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
