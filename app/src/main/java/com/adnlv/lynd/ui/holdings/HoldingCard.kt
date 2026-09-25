package com.adnlv.lynd.ui.holdings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Badge
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.adnlv.lynd.domain.HoldingItem
import com.adnlv.lynd.ui.components.MetricItem
import com.adnlv.lynd.util.Formatters
import com.adnlv.lynd.util.HapticFeedbackHelper
import kotlinx.coroutines.launch
import java.math.BigDecimal
import kotlin.math.roundToInt

@Composable
fun HoldingCard(
    holding: HoldingItem,
    isRevealed: Boolean,
    isPeeking: Boolean = false,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    onDragStart: (() -> Unit)? = null,
    onDragEnd: (() -> Unit)? = null,
    onDragCancel: (() -> Unit)? = null,
    canSwipe: Boolean = true,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    shape: Shape = RoundedCornerShape(16.dp),
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val buttonGapDp = 2.dp
    val baseButtonWidthDp = 60.dp
    val actionButtonsWidthDp = baseButtonWidthDp * 2 + buttonGapDp * 3
    val actionButtonsWidthPx = with(density) { actionButtonsWidthDp.toPx() }
    val offsetX = remember { Animatable(0f) }
    val slideAwayOffsetX = remember { Animatable(0f) }
    var itemWidthPx by remember { mutableFloatStateOf(0f) }
    var isDeleting by remember { mutableStateOf(false) }
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var hasTriggeredRevealHaptic by remember { mutableStateOf(false) }

    val swipeSpringSpec = spring<Float>(
        dampingRatio = 0.6f,
        stiffness = Spring.StiffnessMediumLow
    )

    LaunchedEffect(isPeeking) {
        if (isDeleting || isRevealed) return@LaunchedEffect
        if (isPeeking) {
            val peekDistance = -actionButtonsWidthPx * 0.6f
            offsetX.animateTo(
                targetValue = peekDistance,
                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
            )
        } else if (offsetX.value != 0f) {
            offsetX.animateTo(
                targetValue = 0f,
                animationSpec = swipeSpringSpec
            )
        }
    }

    LaunchedEffect(isRevealed, isDeleting) {
        if (isDeleting) return@LaunchedEffect
        val target = if (isRevealed) -actionButtonsWidthPx else 0f
        if (offsetX.value != target) {
            offsetX.animateTo(target, animationSpec = swipeSpringSpec)
        }
    }

    val dragDistance = (-offsetX.value).coerceAtLeast(0f)
    val fullSwipeThresholdPx = if (itemWidthPx > 0f) {
        (itemWidthPx * 0.45f).coerceAtLeast(actionButtonsWidthPx * 1.5f)
    } else {
        with(density) { 220.dp.toPx() }
    }
    val maxDragLeftPx = if (itemWidthPx > 0f) itemWidthPx else with(density) { 360.dp.toPx() }

    val (deleteWidthDp, editWidthDp, deleteAlpha) = run {
        if (dragDistance <= actionButtonsWidthPx) {
            val progress = (dragDistance / actionButtonsWidthPx).coerceIn(0f, 1f)
            Triple(
                baseButtonWidthDp * progress,
                baseButtonWidthDp * progress,
                progress
            )
        } else {
            val deepDenominator = (fullSwipeThresholdPx - actionButtonsWidthPx).coerceAtLeast(1f)
            val deepProgress = ((dragDistance - actionButtonsWidthPx) / deepDenominator).coerceIn(0f, 1f)
            val delWidth = if (deepProgress >= 1f) 0.dp else (baseButtonWidthDp * (1f - deepProgress)).coerceAtLeast(0.dp)
            val delAlpha = (1f - deepProgress).coerceIn(0f, 1f)
            val totalRevealedWidthDp = with(density) {
                (dragDistance - with(density) { (buttonGapDp * 2).toPx() }).toDp()
            }.coerceAtLeast(baseButtonWidthDp)
            val spacingDp = if (delWidth > 0.dp) buttonGapDp else 0.dp
            val edWidth = (totalRevealedWidthDp - delWidth - spacingDp).coerceAtLeast(baseButtonWidthDp)
            Triple(delWidth, edWidth, delAlpha)
        }
    }

    val buttonSpacingDp = if (deleteWidthDp > 0.dp) buttonGapDp else 0.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 200,
                    easing = LinearOutSlowInEasing
                )
            )
            .onSizeChanged { itemWidthPx = it.width.toFloat() }
            .offset { IntOffset(slideAwayOffsetX.value.roundToInt(), 0) }
            .clip(shape)
    ) {
        Row(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = buttonGapDp),
            horizontalArrangement = Arrangement.spacedBy(buttonSpacingDp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (deleteWidthDp > 0.dp) {
                Surface(
                    onClick = {
                        if (!isDeleting) {
                            isDeleting = true
                            coroutineScope.launch {
                                val targetOffset = if (itemWidthPx > 0f) -itemWidthPx - with(density) { 32.dp.toPx() } else -1500f
                                slideAwayOffsetX.animateTo(
                                    targetValue = targetOffset,
                                    animationSpec = tween(durationMillis = 250, easing = FastOutLinearInEasing)
                                )
                                onDelete()
                            }
                        }
                    },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(deleteWidthDp)
                        .graphicsLayer { alpha = deleteAlpha }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Holding"
                        )
                    }
                }
            }
            if (editWidthDp > 0.dp) {
                Surface(
                    onClick = onEdit,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(editWidthDp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Holding"
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .clip(shape)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = shape
                )
                .animateContentSize(
                    animationSpec = tween(
                        durationMillis = 200,
                        easing = LinearOutSlowInEasing
                    )
                )
                .pointerInput(actionButtonsWidthPx, isDeleting, canSwipe, fullSwipeThresholdPx, maxDragLeftPx) {
                    if (isDeleting) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragStart = {
                            if (canSwipe) {
                                hasTriggeredRevealHaptic = isRevealed
                                onDragStart?.invoke()
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            if (!canSwipe) return@detectHorizontalDragGestures
                            coroutineScope.launch {
                                val current = offsetX.value
                                val minOffset = -maxDragLeftPx
                                val maxOffset = with(density) { 40.dp.toPx() }
                                val target = (current + dragAmount).coerceIn(minOffset, maxOffset)
                                offsetX.snapTo(target)

                                val currentDragDistance = -target
                                val revealThresholdPx = actionButtonsWidthPx / 3f

                                if (currentDragDistance >= revealThresholdPx) {
                                    if (!hasTriggeredRevealHaptic) {
                                        hasTriggeredRevealHaptic = true
                                        HapticFeedbackHelper.vibrateRevealThreshold(view)
                                    }
                                } else {
                                    if (hasTriggeredRevealHaptic) {
                                        hasTriggeredRevealHaptic = false
                                    }
                                }
                            }
                        },
                        onDragEnd = {
                            onDragEnd?.invoke()
                            coroutineScope.launch {
                                val currentDragDistance = -offsetX.value
                                if (currentDragDistance >= fullSwipeThresholdPx) {
                                    HapticFeedbackHelper.vibratePrimaryAction(view)
                                    onEdit()
                                    offsetX.animateTo(0f, animationSpec = swipeSpringSpec)
                                    onCollapse()
                                } else if (currentDragDistance >= actionButtonsWidthPx / 3) {
                                    onExpand()
                                    offsetX.animateTo(-actionButtonsWidthPx, animationSpec = swipeSpringSpec)
                                } else {
                                    onCollapse()
                                    offsetX.animateTo(0f, animationSpec = swipeSpringSpec)
                                }
                                hasTriggeredRevealHaptic = false
                            }
                        },
                        onDragCancel = {
                            onDragCancel?.invoke()
                            coroutineScope.launch {
                                val currentDragDistance = -offsetX.value
                                if (currentDragDistance >= fullSwipeThresholdPx) {
                                    HapticFeedbackHelper.vibratePrimaryAction(view)
                                    onEdit()
                                    offsetX.animateTo(0f, animationSpec = swipeSpringSpec)
                                    onCollapse()
                                } else if (currentDragDistance >= actionButtonsWidthPx / 3) {
                                    onExpand()
                                    offsetX.animateTo(-actionButtonsWidthPx, animationSpec = swipeSpringSpec)
                                } else {
                                    onCollapse()
                                    offsetX.animateTo(0f, animationSpec = swipeSpringSpec)
                                }
                                hasTriggeredRevealHaptic = false
                            }
                        }
                    )
                }
                .clickable {
                    if (isRevealed) {
                        onCollapse()
                    } else {
                        isExpanded = !isExpanded
                    }
                }
        ) {
            ListItem(
                headlineContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = Formatters.formatDate(holding.purchaseDate),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Badge(
                            containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f),
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                shape = CircleShape
                            )
                        ) {
                            Text(
                                text = "${holding.quantity}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                trailingContent = {
                    Text(
                        text = Formatters.formatAmount(holding.totalPaidAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = tween(
                        durationMillis = 200,
                        easing = LinearOutSlowInEasing
                    )
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = 120,
                        delayMillis = 60
                    )
                ),
                exit = shrinkVertically(
                    animationSpec = tween(
                        durationMillis = 180,
                        easing = FastOutLinearInEasing
                    )
                ) + fadeOut(
                    animationSpec = tween(
                        durationMillis = 120
                    )
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricItem(
                            label = "Price per bond",
                            value = Formatters.formatAmount(holding.pricePerBond),
                            modifier = Modifier.weight(1f)
                        )
                        val displayRate = if (holding.profitPercentage != BigDecimal.ZERO) {
                            holding.profitPercentage
                        } else {
                            holding.couponRate
                        }
                        MetricItem(
                            label = "Coupon rate",
                            value = Formatters.formatPercentage(displayRate),
                            valueColor = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricItem(
                            label = "Total payout",
                            value = Formatters.formatAmount(holding.totalPayoutAmount),
                            valueColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        val isProfitPositive = holding.totalProfitAmount >= BigDecimal.ZERO
                        val profitPrefix = if (isProfitPositive && holding.totalProfitAmount > BigDecimal.ZERO) "+" else ""
                        val profitColor = if (isProfitPositive) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                        MetricItem(
                            label = "Total profit",
                            value = "$profitPrefix${Formatters.formatAmount(holding.totalProfitAmount)}",
                            valueColor = profitColor,
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        )
                    }
                }
            }
        }
    }
}
