package com.adnlv.lynd.ui.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import java.math.BigDecimal

@Composable
fun PlannerSection(
    uiState: OverviewUiState,
    onPlannerTabSelected: (PlannerTab) -> Unit,
    onHorizonSelected: (Int) -> Unit,
    onCurrencySelected: (String) -> Unit,
    onCompoundingHorizonSelected: (Int) -> Unit,
    onCompoundingRateSelected: (BigDecimal) -> Unit,
    onThresholdSelected: (BigDecimal) -> Unit = {},
    onLoadTestPortfolio: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        SecondaryScrollableTabRow(
            selectedTabIndex = uiState.selectedPlannerTab.ordinal,
            edgePadding = 16.dp
        ) {
            PlannerTab.entries.forEach { tab ->
                Tab(
                    selected = uiState.selectedPlannerTab == tab,
                    onClick = { onPlannerTabSelected(tab) },
                    text = { Text(tab.title) }
                )
            }
        }

        if (uiState.summaries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = "No portfolio data yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(onClick = onLoadTestPortfolio) {
                        Text("Load test portfolio")
                    }
                }
            }
        } else {
            when (uiState.selectedPlannerTab) {
                PlannerTab.INCOME_GAPS -> {
                    IncomeGapGroup(
                        uiState = uiState,
                        onHorizonSelected = onHorizonSelected,
                        onCurrencySelected = onCurrencySelected
                    )
                }
                PlannerTab.COMPOUNDING -> {
                    CompoundingGroup(
                        uiState = uiState,
                        onHorizonSelected = onCompoundingHorizonSelected,
                        onCurrencySelected = onCurrencySelected,
                        onRateSelected = onCompoundingRateSelected
                    )
                }
                PlannerTab.PURCHASING_POWER -> {
                    PurchasingPowerGroup(
                        uiState = uiState,
                        onHorizonSelected = onCompoundingHorizonSelected,
                        onCurrencySelected = onCurrencySelected,
                        onRateSelected = onCompoundingRateSelected
                    )
                }
                PlannerTab.MATURITY_REBALANCING -> {
                    MaturityRebalancingGroup(
                        uiState = uiState,
                        onCurrencySelected = onCurrencySelected,
                        onThresholdSelected = onThresholdSelected
                    )
                }
            }
        }
    }
}
