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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                var menuExpanded by remember { mutableStateOf(false) }
                val isAllSelected = remember(uiState.selectedCurrencies, uiState.availableCurrencies) {
                    uiState.availableCurrencies.isNotEmpty() &&
                        uiState.availableCurrencies.all { it in uiState.selectedCurrencies }
                }

                val filterLabel = remember(uiState.selectedCurrencies, uiState.availableCurrencies) {
                    val count = uiState.selectedCurrencies.size
                    val total = uiState.availableCurrencies.size
                    val symbols = uiState.selectedCurrencies.joinToString(", ") { Formatters.getCurrencySymbol(it) }
                    when {
                        count == 1 -> {
                            val single = uiState.selectedCurrencies.first()
                            "Currency: ${Formatters.getCurrencySymbol(single)} ($single)"
                        }
                        isAllSelected || (total > 0 && count == total) -> "All currencies ($symbols)"
                        else -> "Currencies: $symbols"
                    }
                }

                FilterChip(
                    selected = true,
                    onClick = { menuExpanded = true },
                    label = {
                        Text(
                            text = filterLabel,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Expand currency filter"
                        )
                    }
                )

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Select all") },
                        leadingIcon = {
                            Checkbox(
                                checked = isAllSelected,
                                onCheckedChange = null
                            )
                        },
                        onClick = {
                            if (isAllSelected) {
                                val primary = uiState.availableCurrencies.firstOrNull { it == "UAH" }
                                    ?: uiState.availableCurrencies.firstOrNull()
                                if (primary != null) viewModel.selectCurrency(primary)
                            } else {
                                viewModel.selectAllCurrencies(uiState.availableCurrencies)
                            }
                        }
                    )
                    HorizontalDivider()
                    uiState.availableCurrencies.forEach { currency ->
                        val isSelected = currency in uiState.selectedCurrencies
                        val symbol = Formatters.getCurrencySymbol(currency)
                        DropdownMenuItem(
                            text = { Text("$symbol  $currency") },
                            leadingIcon = {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = null
                                )
                            },
                            onClick = { viewModel.toggleCurrency(currency) }
                        )
                    }
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

                    val dateTotalsByCurrency = payoutsForDate
                        .groupBy { it.currency }
                        .mapValues { (_, items) ->
                            items.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.payoutAmount) }
                        }

                    dateTotalsByCurrency.forEach { (currency, totalAmount) ->
                        item(key = "total_${date}_$currency") {
                            val symbol = Formatters.getCurrencySymbol(currency)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (dateTotalsByCurrency.size > 1) "Total ($currency)" else "Total",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${Formatters.formatAmount(totalAmount)} $symbol",
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
}
