package com.adnlv.lynd.ui.overview

import androidx.compose.foundation.BorderStroke
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
import com.adnlv.lynd.domain.MonthlyCashFlow
import com.adnlv.lynd.util.Formatters
import java.math.BigDecimal
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CashFlowChartCard(
    cashFlows: List<MonthlyCashFlow>,
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
            val total12MonthsIncome = remember(cashFlows) {
                cashFlows.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.totalAmount) }
            }

            var selectedIndex by remember(cashFlows) {
                val firstNonZeroIndex = cashFlows.indexOfFirst { it.totalAmount > BigDecimal.ZERO }
                mutableStateOf(if (firstNonZeroIndex >= 0) firstNonZeroIndex else 0)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Monthly Cash Flow",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Next 12 months",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total Upcoming",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${Formatters.formatAmount(total12MonthsIncome)} $currency",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(
                    color = MaterialTheme.colorScheme.primary,
                    label = "Coupon"
                )
                LegendItem(
                    color = MaterialTheme.colorScheme.tertiary,
                    label = "Principal"
                )
            }

            if (cashFlows.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No upcoming payments.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val maxMonthlyAmount = remember(cashFlows) {
                    val max = cashFlows.maxOfOrNull { it.totalAmount } ?: BigDecimal.ZERO
                    if (max > BigDecimal.ZERO) max else BigDecimal.ONE
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    cashFlows.forEachIndexed { index, monthFlow ->
                        val isSelected = index == selectedIndex
                        val monthLabel = remember(monthFlow.yearMonth) {
                            monthFlow.yearMonth.month
                                .getDisplayName(TextStyle.SHORT, Locale.getDefault())
                                .filter { it.isLetter() }
                                .take(3)
                        }

                        CashFlowBar(
                            monthFlow = monthFlow,
                            monthLabel = monthLabel,
                            maxAmount = maxMonthlyAmount,
                            isSelected = isSelected,
                            couponColor = MaterialTheme.colorScheme.primary,
                            principalColor = MaterialTheme.colorScheme.tertiary,
                            onClick = { selectedIndex = index },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                val selectedFlow = cashFlows.getOrNull(selectedIndex)
                if (selectedFlow != null) {
                    CashFlowDetailCard(
                        selectedFlow = selectedFlow,
                        currency = currency
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
