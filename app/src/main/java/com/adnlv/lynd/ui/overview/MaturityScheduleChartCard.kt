package com.adnlv.lynd.ui.overview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adnlv.lynd.domain.YearlyMaturity
import com.adnlv.lynd.util.Formatters
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun MaturityScheduleChartCard(
    maturities: List<YearlyMaturity>,
    currency: String,
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
            val totalPrincipal = remember(maturities) {
                maturities.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.amount) }
            }

            var selectedIndex by remember(maturities) {
                val firstNonZeroIndex = maturities.indexOfFirst { it.amount > BigDecimal.ZERO }
                mutableStateOf(if (firstNonZeroIndex >= 0) firstNonZeroIndex else 0)
            }

            // Header with title and total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Maturity Schedule",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Principal redemption by year",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total Principal",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${Formatters.formatAmount(totalPrincipal)} $currency",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            if (maturities.isEmpty() || totalPrincipal <= BigDecimal.ZERO) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No upcoming maturities.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val maxYearAmount = remember(maturities) {
                    val max = maturities.maxOfOrNull { it.amount } ?: BigDecimal.ZERO
                    if (max > BigDecimal.ZERO) max else BigDecimal.ONE
                }

                // Yearly Bar Chart
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    maturities.forEachIndexed { index, maturity ->
                        val isSelected = index == selectedIndex
                        MaturityYearBar(
                            maturity = maturity,
                            maxAmount = maxYearAmount,
                            isSelected = isSelected,
                            barColor = MaterialTheme.colorScheme.tertiary,
                            onClick = { selectedIndex = index },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Selected Year Details Card
                val selectedMaturity = maturities.getOrNull(selectedIndex)
                if (selectedMaturity != null) {
                    val percentageOfTotal = remember(selectedMaturity.amount, totalPrincipal) {
                        if (totalPrincipal > BigDecimal.ZERO) {
                            selectedMaturity.amount.multiply(BigDecimal("100"))
                                .divide(totalPrincipal, 2, RoundingMode.HALF_UP)
                        } else {
                            BigDecimal.ZERO
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${selectedMaturity.year}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${Formatters.formatAmount(selectedMaturity.amount)} $currency",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Share of Redemptions",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = Formatters.formatPercentage(percentageOfTotal),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MaturityYearBar(
    maturity: YearlyMaturity,
    maxAmount: BigDecimal,
    isSelected: Boolean,
    barColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val heightFraction = remember(maturity.amount, maxAmount) {
        if (maxAmount > BigDecimal.ZERO && maturity.amount > BigDecimal.ZERO) {
            (maturity.amount.toFloat() / maxAmount.toFloat()).coerceIn(0.06f, 1f)
        } else {
            0f
        }
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Bar Column Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            if (heightFraction > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(heightFraction)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(barColor)
                )
            } else {
                // Baseline indicator for empty year
                Box(
                    modifier = Modifier
                        .size(width = 8.dp, height = 2.dp)
                        .background(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(1.dp)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Year Label
        Text(
            text = "${maturity.year}",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) barColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
