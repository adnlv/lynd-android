package com.adnlv.lynd.ui.payouts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.adnlv.lynd.ui.theme.LyndGlassDefaults
import com.adnlv.lynd.util.Formatters

private val GroupHeaderHorizontalPadding = 16.dp
private val GroupHeaderVerticalPadding = 6.dp
private val StatusRowHorizontalPadding = 16.dp
private val StatusRowTopPadding = 4.dp
private val StatusRowBottomPadding = 4.dp
private val CurrencyRowHorizontalPadding = 16.dp
private val CurrencyRowTopPadding = 4.dp
private val CurrencyRowBottomPadding = 4.dp
private val CurrencyPillSpacing = 8.dp
private val CurrencyPillMinWidth = 48.dp
private val CurrencyPillMinHeight = 36.dp
private val CurrencyPillHorizontalPadding = 16.dp
private val CurrencyPillVerticalPadding = 8.dp

@Composable
fun ColumnScope.PayoutFilterMenuContent(
    selectedTab: PayoutTab,
    availableCurrencies: List<String>,
    selectedCurrencies: Set<String>,
    onSelectTab: (PayoutTab) -> Unit,
    onSelectCurrency: (String) -> Unit,
    onToggleCurrency: (String) -> Unit
) {
    FilterGroupHeader(title = "Status")

    val statusTabs = listOf(
        PayoutTab.UPCOMING to "Upcoming",
        PayoutTab.RECEIVED to "Received"
    )
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = StatusRowHorizontalPadding,
                top = StatusRowTopPadding,
                end = StatusRowHorizontalPadding,
                bottom = StatusRowBottomPadding
            )
    ) {
        statusTabs.forEachIndexed { index, (tab, label) ->
            val isSelected = selectedTab == tab
            SegmentedButton(
                selected = isSelected,
                onClick = { onSelectTab(tab) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = statusTabs.size
                ),
                icon = {}
            ) {
                Text(
                    text = label,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }

    if (availableCurrencies.size > 1) {
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = LyndGlassDefaults.BorderAlpha)
        )
        FilterGroupHeader(title = "Currency")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = CurrencyRowHorizontalPadding,
                    top = CurrencyRowTopPadding,
                    end = CurrencyRowHorizontalPadding,
                    bottom = CurrencyRowBottomPadding
                ),
            horizontalArrangement = Arrangement.spacedBy(
                space = CurrencyPillSpacing,
                alignment = Alignment.CenterHorizontally
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            availableCurrencies.forEach { currency ->
                val isSelected = currency in selectedCurrencies
                val symbol = Formatters.getCurrencySymbol(currency)
                CurrencySymbolPill(
                    symbol = symbol,
                    currencyCode = currency,
                    selected = isSelected,
                    onClick = { onSelectCurrency(currency) },
                    onLongClick = { onToggleCurrency(currency) }
                )
            }
        }
    }
}

private const val GroupHeaderAlpha = 0.6f

@Composable
private fun FilterGroupHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = GroupHeaderAlpha),
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = GroupHeaderHorizontalPadding,
                vertical = GroupHeaderVerticalPadding
            )
    )
}

@Composable
private fun CurrencySymbolPill(
    symbol: String,
    currencyCode: String,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val border = if (selected) {
        null
    } else {
        BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        )
    }

    Surface(
        modifier = modifier
            .defaultMinSize(minWidth = CurrencyPillMinWidth, minHeight = CurrencyPillMinHeight)
            .clip(CircleShape)
            .combinedClickable(
                role = Role.Checkbox,
                onClick = onClick,
                onLongClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                    onLongClick()
                }
            )
            .semantics {
                contentDescription = currencyCode
                this.selected = selected
            },
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
        border = border
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(
                horizontal = CurrencyPillHorizontalPadding,
                vertical = CurrencyPillVerticalPadding
            )
        ) {
            Text(
                text = symbol,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
