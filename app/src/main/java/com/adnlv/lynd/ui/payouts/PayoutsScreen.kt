package com.adnlv.lynd.ui.payouts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PayoutsScreen(
    viewModel: PayoutsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            val navBarContainerColor = MaterialTheme.colorScheme.surfaceContainer
            PayoutTab.entries.forEachIndexed { index, tab ->
                val isSelected = tab == uiState.selectedTab
                val label = when (tab) {
                    PayoutTab.UPCOMING -> "Upcoming"
                    PayoutTab.RECEIVED -> "Received"
                    PayoutTab.HISTORICAL -> "Historical"
                }
                SegmentedButton(
                    selected = isSelected,
                    onClick = { viewModel.selectTab(tab) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = PayoutTab.entries.size
                    ),
                    border = BorderStroke(0.dp, Color.Transparent),
                    colors = SegmentedButtonDefaults.colors(
                        inactiveContainerColor = navBarContainerColor
                    ),
                    icon = {},
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }
        }

        if (uiState.availableCurrencies.size > 1) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.availableCurrencies) { currency ->
                    FilterChip(
                        selected = currency.equals(uiState.selectedCurrency, ignoreCase = true),
                        onClick = { viewModel.selectCurrency(currency) },
                        label = { Text(currency) }
                    )
                }
            }
        }

        if (uiState.payouts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val emptyMessage = when (uiState.selectedTab) {
                    PayoutTab.UPCOMING -> "No upcoming payouts found."
                    PayoutTab.RECEIVED -> "No received payouts found."
                    PayoutTab.HISTORICAL -> "No historical payouts found."
                }
                Text(
                    text = emptyMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val dateFormatter = remember { DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault()) }
            val groupedPayouts = remember(uiState.payouts) {
                uiState.payouts.groupBy { it.payDate }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                groupedPayouts.forEach { (date, payoutsForDate) ->
                    item(key = "header_$date") {
                        val totalSum = remember(payoutsForDate) {
                            payoutsForDate.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.payoutAmount) }
                                .setScale(2, RoundingMode.HALF_UP)
                                .toPlainString()
                        }
                        val currency = payoutsForDate.firstOrNull()?.currency.orEmpty()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = date.format(dateFormatter),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$totalSum $currency",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    items(
                        items = payoutsForDate,
                        key = { "${it.isin}_${it.payDate}_${it.payType}_${it.payoutAmount}" }
                    ) { payout ->
                        PayoutCard(
                            payout = payout,
                            tab = uiState.selectedTab
                        )
                    }
                }
            }
        }
    }
}
