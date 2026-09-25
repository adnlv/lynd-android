package com.adnlv.lynd.ui.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
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
fun OverviewScreen(
    viewModel: OverviewViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        PrimaryTabRow(
            selectedTabIndex = uiState.selectedTab.ordinal
        ) {
            Tab(
                selected = uiState.selectedTab == OverviewTab.OVERVIEW,
                onClick = { viewModel.selectTab(OverviewTab.OVERVIEW) },
                text = { Text("Overview") }
            )
            Tab(
                selected = uiState.selectedTab == OverviewTab.PLANNER,
                onClick = { viewModel.selectTab(OverviewTab.PLANNER) },
                text = { Text("Planner") }
            )
        }

        when (uiState.selectedTab) {
            OverviewTab.OVERVIEW -> {
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
                            OutlinedButton(
                                onClick = { viewModel.loadTestPortfolio() }
                            ) {
                                Text("Load test portfolio")
                            }
                        }
                    }
                } else {
                    val pagerState = rememberPagerState(pageCount = { uiState.summaries.size })
                    val activeSummary = uiState.summaries.getOrNull(pagerState.currentPage)
                    val activeCurrency = activeSummary?.currency ?: uiState.summaries.firstOrNull()?.currency ?: "UAH"
                    val activeCashFlows = uiState.cashFlowsByCurrency[activeCurrency].orEmpty()
                    val activeMaturities = uiState.yearlyMaturitiesByCurrency[activeCurrency].orEmpty()

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        item {
                            Text(
                                text = "Overview",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }

                        item {
                            PortfolioSummaryCarousel(
                                summaries = uiState.summaries,
                                pagerState = pagerState
                            )
                        }

                        item {
                            CashFlowChartCard(
                                cashFlows = activeCashFlows,
                                currency = activeCurrency,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }

                        item {
                            MaturityScheduleChartCard(
                                maturities = activeMaturities,
                                currency = activeCurrency,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }

                        item {
                            CurrencyAllocationCard(
                                allocations = uiState.currencyAllocations,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                    }
                }
            }
            OverviewTab.PLANNER -> {
                PlannerSection(
                    uiState = uiState,
                    onPlannerTabSelected = { viewModel.selectPlannerTab(it) },
                    onHorizonSelected = { viewModel.setPlannerHorizon(it) },
                    onCurrencySelected = { viewModel.setPlannerCurrency(it) },
                    onCompoundingHorizonSelected = { viewModel.setCompoundingHorizon(it) },
                    onCompoundingRateSelected = { viewModel.setCompoundingRate(it) },
                    onThresholdSelected = { viewModel.setLargeRedemptionThreshold(it) },
                    onLoadTestPortfolio = { viewModel.loadTestPortfolio() }
                )
            }
        }
    }
}
