package com.adnlv.lynd.ui.payouts

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adnlv.lynd.ui.theme.LyndGlassDefaults
import com.adnlv.lynd.util.Formatters

private val GroupHeaderHorizontalPadding = 16.dp
private val GroupHeaderVerticalPadding = 6.dp

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

    FilterGroupHeader(title = "Status")

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
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = LyndGlassDefaults.BorderAlpha)
        )
        FilterGroupHeader(title = "Currency")
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

@Composable
private fun FilterGroupHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(
            horizontal = GroupHeaderHorizontalPadding,
            vertical = GroupHeaderVerticalPadding
        )
    )
}
