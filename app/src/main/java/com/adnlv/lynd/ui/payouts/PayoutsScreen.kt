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
import androidx.compose.material3.HorizontalDivider
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
import com.adnlv.lynd.util.Formatters
import java.math.BigDecimal
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
                        label = { Text(Formatters.getCurrencySymbol(currency)) }
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
            val currencySymbol = remember(uiState.selectedCurrency, uiState.payouts) {
                val currency = uiState.selectedCurrency.ifBlank {
                    uiState.payouts.firstOrNull()?.currency.orEmpty()
                }
                Formatters.getCurrencySymbol(currency)
            }

            val isDark = androidx.compose.foundation.isSystemInDarkTheme()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                groupedPayouts.forEach { (date, payoutsForDate) ->
                    item(key = "header_$date") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp, bottom = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                            Text(
                                text = date.format(dateFormatter),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
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
                    item(key = "total_$date") {
                        val dateTotal = remember(payoutsForDate) {
                            payoutsForDate.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.payoutAmount) }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${Formatters.formatAmount(dateTotal)} $currencySymbol",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = PayoutColors.totalAmount(isDark)
                            )
                        }
                    }
                }
            }
        }
    }
}
