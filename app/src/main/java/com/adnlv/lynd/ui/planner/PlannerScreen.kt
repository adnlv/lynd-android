package com.adnlv.lynd.ui.planner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PlannerScreen(
    viewModel: PlannerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        val selectedTab = uiState.selectedPlannerTab
        if (selectedTab == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Planner",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                PlannerMenuGrid(
                    onTabSelected = { viewModel.selectPlannerTab(it) }
                )

                if (uiState.summaries.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
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
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.selectPlannerTab(null) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Text(
                    text = selectedTab.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
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
                when (selectedTab) {
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
}
