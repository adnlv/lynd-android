package com.adnlv.lynd.ui.holdings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun TimelineIndicator(
    isFirst: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier,
    nodeTopOffset: Dp = 20.dp,
    nodeRadius: Dp = 4.dp,
    lineWidth: Dp = 2.dp
) {
    val nodeColor = MaterialTheme.colorScheme.primary
    val lineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)

    Canvas(
        modifier = modifier
            .width(20.dp)
            .fillMaxHeight()
    ) {
        val centerX = size.width / 2f
        val centerY = nodeTopOffset.toPx()
        val radiusPx = nodeRadius.toPx()
        val strokeWidthPx = lineWidth.toPx()

        if (!isFirst) {
            drawLine(
                color = lineColor,
                start = Offset(centerX, 0f),
                end = Offset(centerX, centerY - radiusPx),
                strokeWidth = strokeWidthPx
            )
        }

        if (!isLast) {
            drawLine(
                color = lineColor,
                start = Offset(centerX, centerY + radiusPx),
                end = Offset(centerX, size.height),
                strokeWidth = strokeWidthPx
            )
        }

        drawCircle(
            color = nodeColor,
            radius = radiusPx,
            center = Offset(centerX, centerY)
        )
    }
}
