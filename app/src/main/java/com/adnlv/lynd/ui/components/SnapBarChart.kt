package com.adnlv.lynd.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.adnlv.lynd.util.HapticFeedbackHelper
import kotlin.math.abs

@Composable
fun <T> SnapBarChart(
    items: List<T>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    barWidthDp: Dp = 48.dp,
    chartHeight: Dp = 140.dp,
    barContent: @Composable (item: T, index: Int, isSelected: Boolean) -> Unit
) {
    val density = LocalDensity.current
    val view = LocalView.current
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val derivedSelectedIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            layoutInfo.visibleItemsInfo
                .filter { it.index > 0 && it.index <= items.size }
                .minByOrNull { abs(it.offset + it.size / 2 - viewportCenter) }
                ?.let { it.index - 1 }
                ?: selectedIndex
        }
    }

    LaunchedEffect(derivedSelectedIndex) {
        if (derivedSelectedIndex != selectedIndex) {
            onSelectedIndexChange(derivedSelectedIndex)
            HapticFeedbackHelper.vibratePageSwitch(view)
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val chartWidthPx = constraints.maxWidth
        val barWidthPx = with(density) { barWidthDp.roundToPx() }
        val sidePaddingPx = (chartWidthPx - barWidthPx) / 2
        val sidePaddingDp = with(density) { sidePaddingPx.coerceAtLeast(0).toDp() }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight),
            state = listState,
            flingBehavior = flingBehavior,
            verticalAlignment = Alignment.Bottom
        ) {
            item { Spacer(modifier = Modifier.width(sidePaddingDp)) }

            itemsIndexed(items) { index, item ->
                val isSelected = index == selectedIndex
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.2f else 0.8f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "barScale"
                )
                val alpha by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.5f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "barAlpha"
                )

                Box(
                    modifier = Modifier
                        .width(barWidthDp)
                        .fillMaxHeight()
                        .graphicsLayer {
                            this.scaleX = scale
                            this.scaleY = scale
                            this.alpha = alpha
                        },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    barContent(item, index, isSelected)
                }
            }

            item { Spacer(modifier = Modifier.width(sidePaddingDp)) }
        }
    }
}
