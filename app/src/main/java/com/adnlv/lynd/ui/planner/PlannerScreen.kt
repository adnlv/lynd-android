package com.adnlv.lynd.ui.planner

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PlannerScreen(
    viewModel: PlannerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        SecondaryScrollableTabRow(
            selectedTabIndex = uiState.selectedPlannerTab.ordinal,
            edgePadding = 16.dp
        ) {
            PlannerTab.entries.forEach { tab ->
                Tab(
                    selected = uiState.selectedPlannerTab == tab,
                    onClick = { viewModel.selectPlannerTab(tab) },
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
                    OutlinedButton(onClick = { viewModel.loadTestPortfolio() }) {
                        Text("Load test portfolio")
                    }
                }
            }
        } else {
            when (uiState.selectedPlannerTab) {
                PlannerTab.INCOME_GAPS -> {
                    IncomeGapGroup(
                        uiState = uiState,
                        onHorizonSelected = { viewModel.setPlannerHorizon(it) },
                        onCurrencySelected = { viewModel.setPlannerCurrency(it) }
                    )
                }
                PlannerTab.COMPOUNDING -> {
                    CompoundingGroup(
                        uiState = uiState,
                        onHorizonSelected = { viewModel.setCompoundingHorizon(it) },
                        onCurrencySelected = { viewModel.setPlannerCurrency(it) },
                        onRateSelected = { viewModel.setCompoundingRate(it) }
                    )
                }
                PlannerTab.PURCHASING_POWER -> {
                    PurchasingPowerGroup(
                        uiState = uiState,
                        onHorizonSelected = { viewModel.setCompoundingHorizon(it) },
                        onCurrencySelected = { viewModel.setPlannerCurrency(it) },
                        onRateSelected = { viewModel.setCompoundingRate(it) }
                    )
                }
                PlannerTab.MATURITY_REBALANCING -> {
                    MaturityRebalancingGroup(
                        uiState = uiState,
                        onCurrencySelected = { viewModel.setPlannerCurrency(it) },
                        onThresholdSelected = { viewModel.setLargeRedemptionThreshold(it) }
                    )
                }
            }
        }
    }
}
