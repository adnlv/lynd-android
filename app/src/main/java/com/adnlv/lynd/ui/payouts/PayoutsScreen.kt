package com.adnlv.lynd.ui.payouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.adnlv.lynd.ui.icons.FilterList
import com.adnlv.lynd.ui.icons.MaterialSymbols
import com.adnlv.lynd.ui.navigation.BottomBarAction
import com.adnlv.lynd.util.Formatters
import java.math.BigDecimal
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PayoutsScreen(
    viewModel: PayoutsViewModel,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    onActionChange: ((BottomBarAction) -> Unit)? = null,
    onNavigateToHoldings: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    SideEffect {
        onActionChange?.invoke(
            BottomBarAction(
                icon = MaterialSymbols.FilterList,
                contentDescription = "Open filter menu",
                activeContentDescription = "Close filter menu",
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
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (uiState.payouts.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                HoldingsSuggestionCard(
                    onClick = onNavigateToHoldings
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
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
            }
        } else {
            val dateFormatter = remember { DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault()) }
            val groupedPayouts = remember(uiState.payouts) {
                uiState.payouts.groupBy { it.payDate }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                item(key = "holdings_suggestion") {
                    HoldingsSuggestionCard(
                        onClick = onNavigateToHoldings,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

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
                            payout = payout
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
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
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

