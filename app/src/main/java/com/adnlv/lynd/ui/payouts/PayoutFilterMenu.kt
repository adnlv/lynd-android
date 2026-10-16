package com.adnlv.lynd.ui.payouts

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.adnlv.lynd.util.Formatters

@Composable
fun ColumnScope.PayoutFilterMenuContent(
    selectedTab: PayoutTab,
    availableCurrencies: List<String>,
    selectedCurrencies: Set<String>,
    onSelectTab: (PayoutTab) -> Unit,
    onSelectCurrency: (String) -> Unit,
    onSelectAllCurrencies: (Collection<String>) -> Unit,
    onToggleCurrency: (String) -> Unit
) {
    val isAllSelected = availableCurrencies.isNotEmpty() &&
        availableCurrencies.all { it in selectedCurrencies }

    PayoutTab.entries.forEach { tab ->
        val isSelected = selectedTab == tab
        val label = when (tab) {
            PayoutTab.UPCOMING -> "Upcoming"
            PayoutTab.RECEIVED -> "Received"
        }
        DropdownMenuItem(
            text = { Text(label) },
            leadingIcon = {
                RadioButton(
                    selected = isSelected,
                    onClick = null
                )
            },
            onClick = { onSelectTab(tab) }
        )
    }

    if (availableCurrencies.isNotEmpty()) {
        HorizontalDivider()
        if (availableCurrencies.size > 1) {
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
                        val primary = availableCurrencies.firstOrNull { it == "UAH" }
                            ?: availableCurrencies.firstOrNull()
                        if (primary != null) onSelectCurrency(primary)
                    } else {
                        onSelectAllCurrencies(availableCurrencies)
                    }
                }
            )
        }
        availableCurrencies.forEach { currency ->
            val isSelected = currency in selectedCurrencies
            val symbol = Formatters.getCurrencySymbol(currency)
            DropdownMenuItem(
                text = { Text("$symbol  $currency") },
                leadingIcon = {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = null
                    )
                },
                onClick = { onToggleCurrency(currency) }
            )
        }
    }
}
