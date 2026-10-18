package com.adnlv.lynd.ui.planner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.text.font.FontWeight
import com.adnlv.lynd.ui.components.LyndEmptyState

@Composable
fun PlannerScreen(
    viewModel: PlannerViewModel,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    onNavigateToHoldings: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (uiState.summaries.isEmpty()) {
            Column(modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 8.dp)) {
                Text(
                    text = "Income Gap Detector",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Identify months without coupon payments to optimize reinvestment.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            LyndEmptyState(
                title = "No income to plan",
                description = "Add bond holdings to identify months without coupon payments and plan cash flow.",
                primaryActionLabel = "Add Holding",
                onPrimaryAction = onNavigateToHoldings,
                onLoadTestPortfolio = { viewModel.loadTestPortfolio() },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        } else {
            IncomeGapGroup(
                uiState = uiState,
                onHorizonSelected = { viewModel.setPlannerHorizon(it) },
                onCurrencySelected = { viewModel.setPlannerCurrency(it) },
                listState = listState
            )
        }
    }
}
