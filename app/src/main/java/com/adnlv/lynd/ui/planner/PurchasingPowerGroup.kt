package com.adnlv.lynd.ui.planner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.math.BigDecimal

@Composable
fun PurchasingPowerGroup(
    uiState: PlannerUiState,
    onHorizonSelected: (Int) -> Unit,
    onCurrencySelected: (String) -> Unit,
    onRateSelected: (BigDecimal) -> Unit,
    modifier: Modifier = Modifier
) {
    val availableCurrencies = uiState.summaries.map { it.currency }.distinct()
    val horizonOptions = listOf(1 to "1Y", 3 to "3Y", 5 to "5Y", 10 to "10Y")

    val ratePresets = when (uiState.plannerSelectedCurrency.uppercase()) {
        "USD" -> listOf(BigDecimal("3.0"), BigDecimal("4.0"), BigDecimal("5.0"), BigDecimal("6.0"))
        "EUR" -> listOf(BigDecimal("2.5"), BigDecimal("3.2"), BigDecimal("4.0"), BigDecimal("5.0"))
        else -> listOf(BigDecimal("12.0"), BigDecimal("14.0"), BigDecimal("15.0"), BigDecimal("16.0"), BigDecimal("18.0"))
    }

    val currentRate = uiState.compoundingSimulation?.annualRatePercent ?: BigDecimal("15.0")

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Purchasing Power Forecaster",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Text(
                text = "Adjust projected compounding payouts against official National Bank of Ukraine inflation forecasts to see real future wealth.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Horizon selection
        item {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                horizonOptions.forEachIndexed { index, (years, label) ->
                    SegmentedButton(
                        selected = uiState.compoundingHorizonYears == years,
                        onClick = { onHorizonSelected(years) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = horizonOptions.size)
                    ) {
                        Text(label)
                    }
                }
            }
        }

        // Currency selection chips if multiple available
        if (availableCurrencies.size > 1) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableCurrencies.forEach { currency ->
                        FilterChip(
                            selected = uiState.plannerSelectedCurrency.equals(currency, ignoreCase = true),
                            onClick = { onCurrencySelected(currency) },
                            label = { Text(currency) }
                        )
                    }
                }
            }
        }

        // Prevailing reinvestment rate switch chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Bond Reinvestment Yield",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ratePresets.forEach { rate ->
                        val isSelected = currentRate.compareTo(rate) == 0
                        FilterChip(
                            selected = isSelected,
                            onClick = { onRateSelected(rate) },
                            label = { Text("$rate%") }
                        )
                    }
                }
            }
        }

        // Purchasing power summary metric card
        uiState.purchasingPowerForecast?.let { forecast ->
            item {
                PurchasingPowerMetricCard(forecast = forecast)
            }

            // Dual bars comparison chart card
            item {
                PurchasingPowerChartCard(forecast = forecast)
            }
        }

        // Educational insight card explaining NBU official baseline
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "NBU Inflation Forecasts",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Based on official National Bank of Ukraine baseline forecasts: 8.1% for 2026, 6.5% for 2027, and 5.0% for 2028 onwards. For foreign currencies, standard central bank inflation targets apply (2.0% - 2.2%).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
