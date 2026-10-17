package com.adnlv.lynd.ui.payouts

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.adnlv.lynd.ui.theme.LyndGlassDefaults
import com.adnlv.lynd.util.Formatters

private const val StatusIndicatorPressedScale = 0.90f
private val GroupHeaderHorizontalPadding = 16.dp
private val GroupHeaderVerticalPadding = 6.dp
private val StatusRowHorizontalPadding = 16.dp
private val StatusRowTopPadding = 4.dp
private val StatusRowBottomPadding = 4.dp
private val StatusPillInnerPadding = 2.dp
private val StatusSegmentMinHeight = 32.dp
private val StatusSegmentHorizontalPadding = 12.dp
private val StatusSegmentVerticalPadding = 6.dp
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

    StatusTogglePill(
        selectedTab = selectedTab,
        onSelectTab = onSelectTab,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = StatusRowHorizontalPadding,
                top = StatusRowTopPadding,
                end = StatusRowHorizontalPadding,
                bottom = StatusRowBottomPadding
            )
    )

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

@Composable
private fun StatusTogglePill(
    selectedTab: PayoutTab,
    onSelectTab: (PayoutTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val statusTabs = listOf(
        PayoutTab.UPCOMING to "Upcoming",
        PayoutTab.RECEIVED to "Received"
    )

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(StatusPillInnerPadding)
        ) {
            val segmentWidth = maxWidth / statusTabs.size
            val selectedIndex = statusTabs.indexOfFirst { it.first == selectedTab }

            StatusToggleIndicator(
                segmentWidth = segmentWidth,
                selectedIndex = selectedIndex,
                isPressed = false
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                statusTabs.forEach { (tab, label) ->
                    val isSelected = selectedTab == tab
                    val contentColor = if (isSelected) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = StatusSegmentMinHeight)
                            .clip(CircleShape)
                            .selectable(
                                selected = isSelected,
                                role = Role.RadioButton,
                                onClick = { onSelectTab(tab) }
                            )
                            .padding(
                                horizontal = StatusSegmentHorizontalPadding,
                                vertical = StatusSegmentVerticalPadding
                            )
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = contentColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusToggleIndicator(
    segmentWidth: Dp,
    selectedIndex: Int,
    isPressed: Boolean = false
) {
    val pillOffset by animateDpAsState(
        targetValue = segmentWidth * selectedIndex.coerceAtLeast(0),
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "status_pill_offset"
    )

    val pillScale by animateFloatAsState(
        targetValue = if (isPressed) StatusIndicatorPressedScale else 1f,
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "status_pill_scale"
    )

    Box(
        modifier = Modifier
            .offset { IntOffset(x = pillOffset.roundToPx(), y = 0) }
            .width(segmentWidth)
            .fillMaxHeight()
            .graphicsLayer {
                scaleX = pillScale
                scaleY = pillScale
            }
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = CircleShape
            )
    )
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
