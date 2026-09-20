package com.adnlv.lynd.ui.overview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adnlv.lynd.domain.CurrencyAllocation
import com.adnlv.lynd.util.Formatters
import java.math.BigDecimal
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CurrencyAllocationCard(
    allocations: List<CurrencyAllocation>,
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
                text = "Currency Allocation",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            val totalCapital = allocations.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.amount) }

            if (allocations.isEmpty() || totalCapital <= BigDecimal.ZERO) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No allocation data available.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val colorMap = mapOf(
                    "UAH" to MaterialTheme.colorScheme.primary,
                    "USD" to MaterialTheme.colorScheme.tertiary,
                    "EUR" to MaterialTheme.colorScheme.secondary
                )
                val textColorMap = mapOf(
                    "UAH" to MaterialTheme.colorScheme.onPrimary,
                    "USD" to MaterialTheme.colorScheme.onTertiary,
                    "EUR" to MaterialTheme.colorScheme.onSecondary
                )
                val fallbackColor = MaterialTheme.colorScheme.outline
                val fallbackTextColor = MaterialTheme.colorScheme.onSurface

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    DonutChart(
                        allocations = allocations,
                        colorMap = colorMap,
                        textColorMap = textColorMap,
                        fallbackColor = fallbackColor,
                        fallbackTextColor = fallbackTextColor,
                        modifier = Modifier.size(200.dp)
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    allocations.forEach { allocation ->
                        val itemColor = colorMap[allocation.currency.uppercase()] ?: fallbackColor
                        CurrencyAllocationRow(
                            allocation = allocation,
                            color = itemColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DonutChart(
    allocations: List<CurrencyAllocation>,
    colorMap: Map<String, Color>,
    textColorMap: Map<String, Color>,
    fallbackColor: Color,
    fallbackTextColor: Color,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier) {
        val strokeWidth = 48.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val arcSize = Size(diameter, diameter)
        val topLeftOffset = Offset(strokeWidth / 2f, strokeWidth / 2f)
        val centerOffset = Offset(size.width / 2f, size.height / 2f)
        val midRadius = diameter / 2f

        var currentAngle = -90f

        allocations.forEach { item ->
            val sweepAngle = (item.percentage.toFloat() / 100f) * 360f
            if (sweepAngle > 0f) {
                val sliceColor = colorMap[item.currency.uppercase()] ?: fallbackColor
                val sliceTextColor = textColorMap[item.currency.uppercase()] ?: fallbackTextColor

                drawArc(
                    color = sliceColor,
                    startAngle = currentAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeftOffset,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )

                if (sweepAngle >= 25f) {
                    val midAngle = currentAngle + (sweepAngle / 2f)
                    val midAngleRad = Math.toRadians(midAngle.toDouble())
                    val labelCenterX = centerOffset.x + (midRadius * cos(midAngleRad)).toFloat()
                    val labelCenterY = centerOffset.y + (midRadius * sin(midAngleRad)).toFloat()

                    val textLayoutResult = textMeasurer.measure(
                        text = "${item.currency}\n${Formatters.formatPercentage(item.percentage)}",
                        style = TextStyle(
                            color = sliceTextColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            lineHeight = 14.sp
                        )
                    )

                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(
                            x = labelCenterX - (textLayoutResult.size.width / 2f),
                            y = labelCenterY - (textLayoutResult.size.height / 2f)
                        )
                    )
                }

                currentAngle += sweepAngle
            }
        }
    }
}

@Composable
private fun CurrencyAllocationRow(
    allocation: CurrencyAllocation,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = allocation.currencyName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = allocation.currency,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "${Formatters.formatAmount(allocation.amount)} ${allocation.currency}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = Formatters.formatPercentage(allocation.percentage),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
