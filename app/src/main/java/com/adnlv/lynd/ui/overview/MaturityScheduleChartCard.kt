package com.adnlv.lynd.ui.overview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adnlv.lynd.domain.YearlyMaturity
import com.adnlv.lynd.util.Formatters
import java.math.BigDecimal

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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    maturities.forEachIndexed { index, maturity ->
                        val isSelected = index == selectedIndex
                        MaturityBar(
                            maturity = maturity,
                            maxAmount = maxYearAmount,
                            isSelected = isSelected,
                            barColor = MaterialTheme.colorScheme.tertiary,
                            onClick = { selectedIndex = index },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                val selectedMaturity = maturities.getOrNull(selectedIndex)
                if (selectedMaturity != null) {
                    MaturityDetailCard(
                        selectedMaturity = selectedMaturity,
                        totalPrincipal = totalPrincipal,
                        currency = currency
                    )
                }
            }
        }
    }
}
