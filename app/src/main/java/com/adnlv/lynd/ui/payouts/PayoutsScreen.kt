package com.adnlv.lynd.ui.payouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adnlv.lynd.ui.navigation.BottomBarAction
import com.adnlv.lynd.util.Formatters
import java.math.BigDecimal
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PayoutsScreen(
    viewModel: PayoutsViewModel,
    modifier: Modifier = Modifier,
    onActionChange: ((BottomBarAction) -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    SideEffect {
        onActionChange?.invoke(
            BottomBarAction(
                icon = Icons.Default.FilterList,
                label = "Filter",
                contentDescription = "Open filter menu",
                menuContent = {
                    PayoutFilterMenuContent(
                        selectedTab = uiState.selectedTab,
                        availableCurrencies = uiState.availableCurrencies,
                        selectedCurrencies = uiState.selectedCurrencies,
                        onSelectTab = viewModel::selectTab,
                        onSelectCurrency = viewModel::selectCurrency,
                        onSelectAllCurrencies = viewModel::selectAllCurrencies,
                        onToggleCurrency = viewModel::toggleCurrency
                    )
                }
            )
        )
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
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

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
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
                                    .padding(top = 8.dp, bottom = 4.dp),
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
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
