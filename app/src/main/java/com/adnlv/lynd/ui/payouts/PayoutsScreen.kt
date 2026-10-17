package com.adnlv.lynd.ui.payouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adnlv.lynd.ui.icons.FilterList
import com.adnlv.lynd.ui.icons.MaterialSymbols
import com.adnlv.lynd.ui.navigation.BottomBarAction

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
                        onToggleCurrency = viewModel::toggleCurrency
                    )
                }
            )
        )
    }

    val headingText = when (uiState.selectedTab) {
        PayoutTab.UPCOMING -> "Upcoming Payouts"
        PayoutTab.RECEIVED -> "Received Payouts"
    }
    val descriptionText = when (uiState.selectedTab) {
        PayoutTab.UPCOMING -> "Track scheduled coupon payments and bond redemptions."
        PayoutTab.RECEIVED -> "Review completed coupon payments and bond redemptions."
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (uiState.dateGroups.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                    Text(
                        text = headingText,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = descriptionText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "payouts_header") {
                    Column(modifier = Modifier.padding(bottom = 4.dp)) {
                        Text(
                            text = headingText,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = descriptionText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                item(key = "holdings_suggestion") {
                    HoldingsSuggestionCard(
                        onClick = onNavigateToHoldings,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                items(
                    items = uiState.dateGroups,
                    key = { it.date.toString() }
                ) { group ->
                    PayoutCard(
                        group = group,
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}
